
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>さんさるトーク!! ログイン</title>


</head>

<body>

<div class="container">

    <p class="site-title">さんさるトーク!!</p>

    <div class="icon-area">
        <span class="icon-text">icon</span>
    </div>

    <h1>ログイン</h1>

    <form class="login-form" action="#" method="post">

        <div class="email-field">
            <label class="form-label" for="email">
                メールアドレス
            </label>

            <input
                type="email"
                id="email"
                name="email"
                class="input-box"
                autocomplete="email"
                required>
        </div>

        <div class="password-field">
            <label class="form-label" for="password">
                パスワード
            </label>

            <input
                    type="password"
                    id="password"
                    name="password"
                    class="input-box"
                    autocomplete="current-password"
                    required>

                
            </div>
        </div>

        <button type="submit" class="login-button">
            ログイン
        </button>

        <div class="links">
            <a href="forgotPassword.jsp">
                パスワードを忘れた方
            </a>

            <a href="register.jsp">
                アカウントを作成する
            </a>
        </div>

    </form>

</div>

<script>
// パスワードの表示・非表示を切り替える
const passwordInput = document.getElementById("password");
const toggleButton = document.getElementById("togglePassword");

toggleButton.addEventListener("click", function() {
    if (passwordInput.type === "password") {
        passwordInput.type = "text";
        toggleButton.setAttribute("aria-label", "パスワードを隠す");
        toggleButton.setAttribute("aria-pressed", "true");
    } else {
        passwordInput.type = "password";
        toggleButton.setAttribute("aria-label", "パスワードを表示");
        toggleButton.setAttribute("aria-pressed", "false");
    }
});
</script>

</body>
</html>
