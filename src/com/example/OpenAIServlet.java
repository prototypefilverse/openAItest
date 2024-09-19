package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

        // 丁寧な言葉遣いに変換する指示を追加
        String fullPrompt = "次の文章を丁寧な言葉遣いに変換してください: " + prompt;

        // OpenAI APIにリクエストを送信
        URL url = new URL("https://api.openai.com/v1/chat/completions");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
        connection.setDoOutput(true);

        // JSONリクエストの作成
        String jsonInputString = "{ \"model\": \"gpt-3.5-turbo\", \"messages\": [{\"role\": \"user\", \"content\": \"" + fullPrompt + "\"}] }";

        // リクエストボディを送信
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // レスポンスを取得
        int responseCode = connection.getResponseCode();
        StringBuilder responseContent = new StringBuilder();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    responseContent.append(line.trim());
                }
            }
        } else {
            responseContent.append("Error: ").append(responseCode);
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

     // テキストとして結果を返す
        response.getWriter().write(resultText);
    }

}

