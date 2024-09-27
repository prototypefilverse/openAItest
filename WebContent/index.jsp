<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>OpenAI API Test</title>
    <link rel="stylesheet" type="text/css" href="styles/styles.css">

    <script>
    async function callOpenAI() {
        const prompt = document.getElementById('prompt').value;
        const responseElement = document.getElementById('response');

        responseElement.innerHTML = '応答を待っています...';  // 初期メッセージ
        responseElement.classList.remove('show');  // 応答表示の初期化

        const response = await fetch('openaiChat', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: 'prompt=' + encodeURIComponent(prompt)
        });

        if (response.ok) {
            const result = await response.text();
            responseElement.innerHTML = result;
            responseElement.classList.add('show');  // アニメーションで表示
        } else {
            responseElement.innerHTML = 'エラーが発生しました。';
            responseElement.classList.add('show');  // エラーもアニメーションで表示
        }
    }

    </script>

</head>

<body>
    <div class="container">
        <h2>AIと会話できるアプリ</h2>
        <p>10回前の会話まで覚えています。</p>
        <form onsubmit="event.preventDefault(); callOpenAI();">
            <label for="prompt">文章を入力してください:</label>
            <textarea id="prompt" name="prompt"></textarea>
            <input type="submit" value="AIに送る">
        </form>
        <div id="responseContainer">
         <h3>応答</h3>
         <div id="response"></div>
        </div>
        <a href="history.jsp">履歴を見る</a>
    </div>
</body>
</html>
