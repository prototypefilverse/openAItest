<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.sql.Connection, java.sql.PreparedStatement, java.sql.ResultSet, com.example.DatabaseConnection" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>履歴表示</title>
    <link rel="stylesheet" type="text/css" href="styles/styles.css">
</head>
<body>
    <div class="container">
        <h2>AI応答履歴</h2>

        <table border="1" cellpadding="10" cellspacing="0">
            <thead>
                <tr>
                    <th>日時</th>
                    <th>プロンプト</th>
                    <th>AIの応答</th>
                </tr>
            </thead>
            <tbody>
                <%
                    Connection dbConnection = null;
                    PreparedStatement stmt = null;
                    ResultSet rs = null;
                    try {
                        dbConnection = DatabaseConnection.getConnection();
                        String sql = "SELECT prompt, response, created_at FROM prompt_history ORDER BY created_at DESC";
                        stmt = dbConnection.prepareStatement(sql);
                        rs = stmt.executeQuery();
                        
                        while (rs.next()) {
                            String prompt = rs.getString("prompt");
                            String aiResponse = rs.getString("response");
                            String createdAt = rs.getString("created_at");
                %>
                <tr>
                    <td><%= createdAt %></td>
                    <td><%= prompt %></td>
                    <td><%= aiResponse %></td>
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

        <a href="index.jsp">戻る</a>
    </div>
</body>
</html>
