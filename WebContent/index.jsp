<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <title>OpenAI API Test</title>
</head>
<body>
    <h2>文章を丁寧な言葉遣いにしてくれるWEBアプリ</h2>
    <p>OpenAIのAPIの実装を試すだけのページ。「（投稿された）文章を丁寧な言葉遣いに変換してください」と指示しています。</p>
    <form action="openaiChat" method="post">
        <label for="prompt">入力:</label>
        <textarea id="prompt" name="prompt" rows="4" cols="50"></textarea>
        <br><br>
        <input type="submit" value="送信">
    </form>
</body>
</html>
