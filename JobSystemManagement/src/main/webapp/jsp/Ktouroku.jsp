<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
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

</style>

</head>

<body>

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
               name="companyName">

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
               name="mail">

    </div>

    <div class="row">

        <label>採用実績</label>

        <select name="result">

            <option value="あり">あり</option>

            <option value="なし">なし</option>

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