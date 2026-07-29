<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
    String emg = (String) request.getAttribute("emg");
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>企業登録</title>

<style>

body{
    font-family:"Meiryo";
    background:#f5f5f5;
}

.container{
    width:900px;
    margin:30px auto;
}

/*======================
 タイトル
=======================*/
.titleArea{
    display:flex;
    align-items:center;
    margin-bottom:60px;
}

.titleArea h2{
    margin-left:15px;
    font-size:32px;
}

/*======================
 戻るボタン
=======================*/
.backButton{
    width:48px;
    height:48px;
    border:2px solid #444;
    background:#58c8ff;
    cursor:pointer;
    position:relative;
    overflow:hidden;
}

.backButton::after{
    content:"";
    position:absolute;
    left:8px;
    top:0;
    width:30px;
    height:100%;
    background:#fff34d;
    clip-path:polygon(100% 0,0 50%,100% 100%);
}

.backButton:hover{
    filter:brightness(0.95);
}

.backButton:active{
    transform:translateY(2px);
}

/*======================
 入力エリア
=======================*/

.formArea{
    width:500px;
    margin:0 auto;
}

.row{
    display:flex;
    align-items:center;
    margin-bottom:28px;
}

.row label{
    width:120px;
    font-size:22px;
}

.row input[type=text]{
    width:300px;
    height:38px;
    font-size:20px;
    border:none;
    border-bottom:2px solid black;
    background:transparent;
    outline:none;
}

.row select{
    width:300px;
    height:45px;
    font-size:20px;
}

/*======================
 登録ボタン
=======================*/

.buttonArea{
    text-align:right;
    margin-top:70px;
}

.submitButton{

    width:100px;
    height:70px;

    background:red;
    color:white;

    border:none;

    border-radius:15px;

    font-size:28px;

    cursor:pointer;

}

.submitButton:hover{
    background:#d10000;
}

/*======================
 エラー表示ダイアログ
=======================*/
dialog {
    border: none;
    border-radius: 8px;
    padding: 24px 30px;
    box-shadow: 0 4px 20px rgba(0,0,0,0.2);
    min-width: 280px;
    text-align: center;
}
dialog::backdrop {
    background: rgba(0,0,0,0.5);
}
dialog p {
    margin: 0 0 20px 0;
    font-size: 15px;
    color: #333;
    white-space: pre-line;
}
dialog button {
    padding: 6px 28px;
    background: #2b6cb0;
    color: #fff;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 14px;
}
dialog button:hover {
    background: #245a94;
}

</style>

</head>

<body>

<dialog id="okDialog">
    <p><%= emg %></p>
    <button onclick="document.getElementById('okDialog').close()">OK</button>
</dialog>

<script>
    window.onload = function() {
        <% if (emg != null && !emg.isEmpty()) { %>
            document.getElementById('okDialog').showModal();
        <% } %>
    };

    // 「戻る」でキャッシュから復元された場合はダイアログを表示しない
    window.addEventListener('pageshow', function(event) {
        if (event.persisted) {
            const dialog = document.getElementById('okDialog');
            if (dialog.open) {
                dialog.close();
            }
        }
    });
</script>

<div class="container">

    <div class="titleArea">

        <button class="backButton"
                type="button"
                onclick="history.back()">
        </button>

        <h2>企業登録</h2>

    </div>

<form action="CompanyRegisterServlet" method="post">

<div class="formArea">

    <div class="row">

        <label>企業名</label>

        <input type="text"
               name="name">

    </div>

    <div class="row">

        <label>住所</label>

        <input type="text"
               name="address">

    </div>

    <div class="row">

        <label>電話番号</label>

        <input type="text"
               name="tel">

    </div>

    <div class="row">

        <label>メールアドレス</label>

        <input type="text"
               name="email">

    </div>

    <div class="row">

        <label>勤務地</label>

        <input type="text"
               name="kinmuchi">

    </div>

    <div class="row">

        <label>採用実績</label>

        <select name="saiyoJisseki">

            <option value="1">あり</option>

            <option value="0">なし</option>

        </select>

    </div>

</div>

<div class="buttonArea">

    <button class="submitButton"
            type="submit">

        登録

    </button>

</div>

</form>

</div>

</body>
</html>
