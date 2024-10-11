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

@WebServlet("/updateStatus")
public class UpdateStatusServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
    	// エンコーディング設定
        request.setCharacterEncoding("UTF-8");
    	
        String taskId = request.getParameter("taskId");
        String newStatus = request.getParameter("status");

        try (Connection dbConnection = DatabaseConnection.getConnection()) {
            String sql = "UPDATE tasks SET status = ? WHERE id = ?";
            try (PreparedStatement stmt = dbConnection.prepareStatement(sql)) {
                stmt.setString(1, newStatus);
                stmt.setInt(2, Integer.parseInt(taskId));
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // タスク一覧にリダイレクト
        response.sendRedirect("index.jsp");
    }
}
