<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>学生登録</title>

<style>

body{
    font-family: "Meiryo";
    background:#f5f5f5;
}

.container{
    width:900px;
    margin:30px auto;
}

/* タイトル部分 */
.titleArea{
    display:flex;
    align-items:center;
    margin-bottom:30px;
}

.titleArea h2{
    margin:0 0 0 15px;
    font-size:32px;
}

/* 戻るボタン */
.backButton{
    width:48px;
    height:48px;
    border:2px solid #444;
    background:#58c8ff;
    cursor:pointer;
    position:relative;
    padding:0;
    overflow:hidden;
}

/* 黄色の三角 */
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

/* マウスを乗せた時 */
.backButton:hover{
    filter:brightness(0.95);
}

/* 押した時 */
.backButton:active{
    transform:translateY(2px);
}
}

table{
    width:100%;
    border-spacing:15px;
}

td{
    vertical-align:top;
    padding-right:80px;
}

label{
    font-weight:bold;
}

input[type=text],select{
    width:300px;
    height:40px;
    font-size:18px;
    text-align:center;
}

textarea{
    width:350px;
    height:180px;
    font-size:18px;
}

.buttonArea{
    text-align:right;
    margin-top:20px;
}

/* 登録ボタン */
.submitButton{
    width:120px;
    height:50px;
    background:red;
    color:white;
    border:none;
    font-size:20px;
    border-radius:8px;
    cursor:pointer;
}

.submitButton:hover{
    background:#cc0000;
}

</style>

</head>

<body>

<div class="container">

    <div class="titleArea">

        <button type="button"
                class="backButton"
                onclick="history.back()"
                title="戻る">
        </button>

        <h2>学生登録</h2>

    </div>

<form action="StudentServlet" method="post">

<table>

<tr>

<td>

<label>クラス</label><br>
<input type="text" name="className">

</td>

<td>

<label>希望地域</label><br>
<input type="text" name="area">

</td>

</tr>

<tr>

<td>

<label>出席番号</label><br>
<input type="text" name="attendanceNo">

</td>

<td>

<label>希望職種</label><br>
<input type="text" name="job1">

</td>

</tr>

<tr>

<td>

<label>学籍番号</label><br>
<input type="text" name="studentNo">

</td>

<td>

<label>希望職種2</label><br>
<input type="text" name="job2">

</td>

</tr>

<tr>

<td>

<label>氏名</label><br>
<input type="text" name="studentName">

</td>

<td>

<label>希望職種3</label><br>
<input type="text" name="job3">

</td>

</tr>

<tr>

<td>

<label>性別</label><br>

<select name="sex">
    <option>男</option>
    <option>女</option>
    <option>どちらでもない</option>
</select>

</td>

<td rowspan="3">

<label>備考</label><br>

<textarea name="memo"></textarea>

</td>

</tr>

<tr>

<td>

<label>あっせん状況</label><br>

<select name="status">
    <option>継続</option>
    <option>辞退</option>
</select>

</td>

</tr>

<tr>

<td>

<label>在籍状況</label><br>

<select name="schoolStatus">
    <option>在籍</option>
    <option>卒業d</option>
    <option>休学</option>
    <option>退学</option>
</select>

</td>

</tr>

</table>

<div class="buttonArea">

<button type="submit" class="submitButton">
登録
</button>

</div>

</form>

</div>

</body>
</html>