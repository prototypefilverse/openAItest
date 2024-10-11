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


@WebServlet("/openaiChat")
public class OpenAIServlet extends HttpServlet {

	private static final String API_KEY = System.getenv("OPENAI_API_KEY");

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // リクエストとレスポンスのエンコーディングを設定
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        String prompt = request.getParameter("prompt");
        
        // OpenAI API キーの確認
        if (API_KEY == null || API_KEY.isEmpty()) {
            response.getWriter().write("Error: API key is missing.");
            return;
        }

        // ここで直近の履歴をデータベースから取得
        String recentHistory = getRecentHistory();

        // プロンプトに履歴を追加
        String fullPrompt = "以下は過去の会話履歴です。\n" + recentHistory + "\nこれに基づいて次の質問に答えてください: " + prompt;

        // OpenAI APIにリクエストを送信
        URL url = new URL("https://api.openai.com/v1/chat/completions");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
        connection.setDoOutput(true);

        // JSONリクエストの作成
        // String jsonInputString = "{ \"model\": \"gpt-3.5-turbo\", \"messages\": [{\"role\": \"user\", \"content\": \"" + fullPrompt + "\"}] }";
        // JSONリクエストの作成　修正案: エスケープや文字列の組み立てが問題になることがあるため、JSONオブジェクトを明示的に作成
        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("model", "gpt-3.5-turbo");
        JSONArray messages = new JSONArray();
        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", fullPrompt);
        messages.put(message);
        jsonRequest.put("messages", messages);
        String jsonInputString = jsonRequest.toString();
        
        // APIリクエストのログ確認用
        System.out.println("Sending JSON Request: " + jsonInputString);

        // リクエストボディを送信
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // レスポンスを取得
        int responseCode = connection.getResponseCode();
        StringBuilder responseContent = new StringBuilder();
        // OpenAI APIからのレスポンスが成功（HTTP 200 OK）であるかどうかを確認
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    responseContent.append(line.trim());
                }
             }
          // エラーハンドリング  
          } else {
            response.getWriter().write("Error: OpenAI API responded with code " + responseCode);
            return;
          }

        // JSONレスポンスを解析
        String resultText = "No response";
        if (responseCode == HttpURLConnection.HTTP_OK) {
            JSONObject jsonResponse = new JSONObject(responseContent.toString());
            resultText = jsonResponse.getJSONArray("choices")
                                    .getJSONObject(0)
                                    .getJSONObject("message")
                                    .getString("content");
         }
        
        // データベースにプロンプトと応答を保存
        try (Connection dbConnection = DatabaseConnection.getConnection()) {
            if (dbConnection != null) {
                String sql = "INSERT INTO prompt_history (prompt, response) VALUES (?, ?)";
                try (PreparedStatement stmt = dbConnection.prepareStatement(sql)) {
                    stmt.setString(1, prompt);
                    stmt.setString(2, resultText);
                    stmt.executeUpdate();
                }
             // データベースに接続できないエラーハンドリング   
             } else {
                response.getWriter().write("Error: Database connection failed.");
                return;
            }
            
         } catch (SQLException e) {
            response.getWriter().write("Error: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        
        // 結果をクライアントに送信
        response.getWriter().write(resultText);
   }
    
    // 直近の会話履歴を取得するメソッドを追加
    private String getRecentHistory() {
        StringBuilder history = new StringBuilder();
        try (Connection dbConnection = DatabaseConnection.getConnection()) {
            if (dbConnection != null) {
                String sql = "SELECT TOP 10 prompt, response FROM prompt_history ORDER BY created_at DESC";
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
}
