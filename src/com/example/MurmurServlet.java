package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet("/murmur")
public class MurmurServlet extends HttpServlet {

    private static final String API_KEY = System.getenv("OPENAI_API_KEY");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/plain; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        // 直近5件の履歴を取得
        String recentHistory = getRecentHistory();

        // 直近5件の履歴を引数にしてOpenAI APIに「ぼやき」をリクエスト
        String murmur = getMurmurFromOpenAI(recentHistory);
        response.getWriter().write(murmur);
    }

    // 直近5件の履歴を取得する関数
    private String getRecentHistory() {
        StringBuilder history = new StringBuilder();
        try (Connection dbConnection = DatabaseConnection.getConnection()) {
            if (dbConnection != null) {
                String sql = "SELECT TOP 5 prompt, response FROM prompt_history ORDER BY created_at DESC";
                try (PreparedStatement stmt = dbConnection.prepareStatement(sql);
                     ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        history.append("ユーザー: ").append(rs.getString("prompt")).append("\n");
                        history.append("AI: ").append(rs.getString("response")).append("\n");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history.toString();
    }
   
    // 直近5件の履歴と命令文をOpenAI APIに投げて返してもらう関数
    private String getMurmurFromOpenAI(String history) throws IOException {
        String murmurPrompt = "以下の会話履歴を読んで、その内容に影響されつつも、直接的には触れない感じで、中身のないぼやきを、20〜30文字程度でぼやいてください。:\n" + history;

        URL url = new URL("https://api.openai.com/v1/chat/completions");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
        connection.setDoOutput(true);

        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("model", "gpt-3.5-turbo");
        JSONArray messages = new JSONArray();
        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", murmurPrompt);
        messages.put(message);
        jsonRequest.put("messages", messages);
        String jsonInputString = jsonRequest.toString();

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        StringBuilder responseContent = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                responseContent.append(line.trim());
            }
        }

        JSONObject jsonResponse = new JSONObject(responseContent.toString());
        return jsonResponse.getJSONArray("choices")
                           .getJSONObject(0)
                           .getJSONObject("message")
                           .getString("content");
    }
}
