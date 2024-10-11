<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.sql.Connection" %>
<%@ page import="java.sql.PreparedStatement" %>
<%@ page import="java.sql.ResultSet" %>
<%@ page import="java.sql.SQLException" %>
<%@ page import="com.example.DatabaseConnection" %>  
    
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Task Manager</title>
    <link rel="stylesheet" type="text/css" href="styles/styles.css">
</head>
<body>
    <div class="main-container">
        <div class="task-input-container">
            <h2>タスク管理アプリ</h2>
            <form action="${pageContext.request.contextPath}/addTask" method="post"> 
                <label for="title">タスク名:</label>
                <input type="text" id="title" name="title" required>
                <label for="description">詳細:</label>
                <textarea id="description" name="description" required></textarea>
                <label for="due_date">期限:</label>
                <input type="datetime-local" id="due_date" name="due_date" required>
                <input type="submit" value="タスク追加">
            </form>
        </div>

        <div class="task-list-container">
            <h3>タスク一覧</h3>
            <table>
                <thead>
                    <tr>
                        <th>タスク名</th>
                        <th>詳細</th>
                        <th>期限</th>
                        <th>ステータス</th>
                        <th>アクション</th>
                    </tr>
                </thead>
                <tbody>
                    <%
                        Connection dbConnection = null;
                        PreparedStatement stmt = null;
                        ResultSet rs = null;
                        try {
                            dbConnection = DatabaseConnection.getConnection();
                            String sql = "SELECT id, title, description, due_date, status FROM tasks ORDER BY created_at DESC";
                            stmt = dbConnection.prepareStatement(sql);
                            rs = stmt.executeQuery();
                            
                            while (rs.next()) {
                                int id = rs.getInt("id");
                                String title = rs.getString("title");
                                String description = rs.getString("description");
                                String dueDate = rs.getString("due_date");
                                String status = rs.getString("status");
                    %>
                    <tr>
                        <td><%= title %></td>
                        <td><%= description %></td>
                        <td><%= dueDate %></td>
                        <td>
                          <form action="${pageContext.request.contextPath}/updateStatus" method="post" style="margin:0;">
                           <input type="hidden" name="taskId" value="<%= id %>">
                            <select name="status" onchange="this.form.submit()">
                              <option value="予定" <%= "予定".equals(status) ? "selected" : "" %>>予定</option>
                              <option value="実行中" <%= "実行中".equals(status) ? "selected" : "" %>>実行中</option>
                              <option value="完了" <%= "完了".equals(status) ? "selected" : "" %>>完了</option>
                            </select>
                          </form>
                        </td>
                        <td>
                          <form action="${pageContext.request.contextPath}/deleteTask" method="post" style="margin:0;">
                            <input type="hidden" name="taskId" value="<%= id %>">
                            <button type="submit" class="delete-btn">削除</button>
                         </form>
                       </td>
                    </tr>
                    <%
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            if (rs != null) try { rs.close(); } catch (Exception e) {}
                            if (stmt != null) try { stmt.close(); } catch (Exception e) {}
                            if (dbConnection != null) try { dbConnection.close(); } catch (Exception e) {}
                        }
                    %>
                </tbody>
            </table>
        </div>
    </div>
</body>
</html>


