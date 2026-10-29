
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>さんさるトーク!! ログイン</title>

<style>
* {
    box-sizing: border-box;
}

body {
    margin: 0;
    padding: 0;
    font-family: "Meiryo", sans-serif;
    background: #ffffff;
    color: #000;
}

/* 画面全体 */
.container {
    width: 400px;
    max-width: 100%;
    min-height: 675px;
    margin: 0 auto;
    padding: 25px 0 40px;

    /* 外側の枠線を削除 */
    border: none;

    /* 外側の丸い枠線も削除 */
    border-radius: 0;

    overflow: hidden;
    position: relative;
}

/* タイトル */
.site-title {
    text-align: center;
    font-size: 18px;
    font-weight: normal;
    margin: 0 0 28px;
}

/* アイコン部分 */
.icon-area {
    width: 205px;
    height: 130px;
    margin: 0 auto 19px;
    background-color: #4ca632;
    border: 2px solid #315c26;
    border-radius: 50%;
    position: relative;
}

/* アイコン内の文字 */
.icon-text {
    position: absolute;
    top: 20px;
    left: 37px;
    color: white;
    font-size: 12px;
}

/* ログイン見出し */
h1 {
    text-align: center;
    font-size: 32px;
    margin: 0 0 14px;
    line-height: 1.3;
}

/* 入力フォーム */
.login-form {
    width: 290px;
    max-width: calc(100% - 40px);
    margin: 0 auto;
}

/* ラベル */
.form-label {
    display: block;
    font-size: 17px;
    margin: 0 0 5px;
}

/* 入力欄 */
.input-box {
    width: 100%;
    height: 33px;
    border: 2px solid #17394a;
    border-radius: 0;
    background: white;
    font-size: 16px;
    padding: 4px 7px;
    outline: none;
}

.input-box:focus {
    border-color: #4ca632;
}

/* メールアドレス欄の下 */
.email-field {
    margin-bottom: 15px;
}

/* パスワード欄 */
.password-wrapper {
    position: relative;
    width: 100%;
}

.password-wrapper .input-box {
    padding-right: 42px;
}

/* 目のボタン */
.eye-button {
    position: absolute;
    right: 0;
    top: 0;
    width: 39px;
    height: 33px;
    border: none;
    background: transparent;
    padding: 3px;
    cursor: pointer;
}

/* 目の形 */
.eye-icon {
    display: block;
    width: 34px;
    height: 25px;
}

/* ログインボタン */
.login-button {
    display: block;
    width: 100%;
    height: 29px;
    margin-top: 25px;
    border: none;
    border-radius: 0;
    background-color: #ffff24;
    color: #ff0000;
    font-size: 13px;
    cursor: pointer;
}

.login-button:hover {
    background-color: #f2f200;
}

/* リンク */
.links {
    margin-top: 20px;
}

.links a {
    display: block;
    width: fit-content;
    color: #5799ed;
    text-decoration: none;
    font-size: 14px;
    margin-bottom: 13px;
}

.links a:hover {
    text-decoration: underline;
}

/* スマートフォン対応 */
@media screen and (max-width: 390px) {
    .container {
        width: 100%;
    }
}
</style>
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

            <div class="password-wrapper">
                <input
                    type="password"
                    id="password"
                    name="password"
                    class="input-box"
                    autocomplete="current-password"
                    required>

                <button
                    type="button"
                    class="eye-button"
                    id="togglePassword"
                    aria-label="パスワードを表示"
                    aria-pressed="false">

                    <svg class="eye-icon"
                         viewBox="0 0 36 26"
                         xmlns="http://www.w3.org/2000/svg">

                        <ellipse cx="18" cy="13"
                                 rx="17" ry="12"
                                 fill="#000000"/>

                        <ellipse cx="18" cy="13"
                                 rx="9" ry="8"
                                 fill="#ffffff"/>

                        <circle cx="18" cy="13"
                                r="5"
                                fill="#000000"/>

                    </svg>
                </button>
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
