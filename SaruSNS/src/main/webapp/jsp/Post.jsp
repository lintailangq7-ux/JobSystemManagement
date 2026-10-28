<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>投稿作成</title>
 <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/post.css">
</head>
<body>
 <header>
        <button type="button" onclick="history.back()">←</button>
        <h1>投稿作成</h1>
        <button type="submit" form="postForm">投稿する</button>
    </header>

    <main>
        <div class="account-info">
            <img
                class="account-icon"
                src="${pageContext.request.contextPath}/images/default-icon.png"
                alt="プロフィール画像">

            <span class="account-name">
                <c:out value="${loginUser.accountName}" />
            </span>
        </div>

        <form id="postForm" action="${pageContext.request.contextPath}/post"
              method="post" enctype="multipart/form-data">

            <label for="visibility">公開範囲</label>
            <select id="visibility" name="visibility">
                <option value="public">全体に公開</option>
                <option value="followers">フォロワーのみ</option>
                <option value="private">自分のみ</option>
            </select>

            <textarea id="postText" name="text"
                      placeholder="今何をしていますか？"></textarea>

            <label for="imageInput">画像を追加</label>
            <input type="file" id="imageInput" name="images"
                   accept="image/*" multiple>

            <label for="audioInput">音声を追加</label>
            <input type="file" id="audioInput" name="audios"
                   accept="audio/*" multiple>

            <input type="text" id="hashtags" name="hashtags"
                   placeholder="#ハッシュタグ（任意）">

        </form>
    </main>

    <script src="${pageContext.request.contextPath}/js/post.js"></script>
</body>
</html>