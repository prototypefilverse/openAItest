package com.example;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/addTask")
public class AddTaskServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // リクエストのエンコーディング設定
        request.setCharacterEncoding("UTF-8");

        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String dueDate = request.getParameter("due_date");
        // dueDate に格納される値が正しいかどうか確認
        System.out.println("Due Date: " + dueDate);  


        // データベース接続とタスクの追加処理
        try (Connection dbConnection = DatabaseConnection.getConnection()) {
            if (dbConnection != null) {
            	String sql = "INSERT INTO tasks (title, description, due_date) VALUES (?, ?, ?)";
                try (PreparedStatement stmt = dbConnection.prepareStatement(sql)) {
                    stmt.setString(1, title);
                    stmt.setString(2, description);
                    // java.sql.Timestamp を使ってフォーマットを変換
                    stmt.setTimestamp(3, java.sql.Timestamp.valueOf(dueDate.replace("T", " ") + ":00"));
                    stmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // タスク一覧にリダイレクト
        response.sendRedirect("index.jsp");
    }
}
