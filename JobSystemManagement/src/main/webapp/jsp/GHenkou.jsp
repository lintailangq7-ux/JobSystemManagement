<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>情報変更</title>

<style>

body{
    font-family: Meiryo;
    background:#ffffff;
}

.container{
    width:1100px;
    margin:20px auto;
}

/* タイトル */
.titleArea{
    display:flex;
    align-items:center;
    margin-bottom:30px;
}

.backButton{

    width:110px;
    height:45px;
    background:#5cc6ff;
    color:white;
    font-size:18px;
    border:2px solid #000;
    cursor:pointer;
}

.title{
    font-size:34px;
    font-weight:bold;
    margin-left:20px;
}

table{
    width:100%;
    border-spacing:25px 15px;
}

label{
    font-weight:bold;
}

.must{
    color:red;
}

input[type=text]{

    width:320px;
    height:40px;
    text-align:center;
    font-size:22px;
    border:2px solid black;
}

select{

    width:320px;
    height:45px;
    text-align:center;
    font-size:22px;
    border:2px solid black;
}

textarea{

    width:350px;
    height:180px;
    border:2px solid black;
    font-size:18px;
}

.buttonArea{

    text-align:right;
    margin-top:20px;
}

.submitButton{

    width:120px;
    height:55px;
    background:red;
    color:white;
    border:none;
    border-radius:10px;
    font-size:24px;
    font-weight:bold;
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
onclick="history.back()">

 戻る

</button>

<div class="title">

情報変更

</div>

</div>

<form action="StudentUpdateServlet" method="post">

<table>

<tr>

<td>

<label><span class="must">*</span> クラス</label><br>
<input type="text" name="className" value="${student.className}">

</td>

<td>

<label><span class="must">*</span> 希望地域</label><br>
<input type="text" name="area" value="${student.area}">

</td>

</tr>

<tr>

<td>

<label><span class="must">*</span> 出席番号</label><br>
<input type="text" name="attendanceNo" value="${student.attendanceNo}">

</td>

<td>

<label>希望職種</label><br>
<input type="text" name="job1" value="${student.job1}">

</td>

</tr>

<tr>

<td>

<label><span class="must">*</span> 学籍番号</label><br>
<input type="text" name="studentNo"
value="${student.studentNo}" readonly>

</td>

<td>

<label>希望職種2</label><br>
<input type="text" name="job2" value="${student.job2}">

</td>

</tr>

<tr>

<td>

<label><span class="must">*</span> 氏名</label><br>
<input type="text" name="studentName"
value="${student.studentName}">

</td>

<td>

<label>希望職種3</label><br>
<input type="text" name="job3" value="${student.job3}">

</td>

</tr>

<tr>

<td>

<label><span class="must">*</span> 性別</label><br>

<select name="sex">

<option ${student.sex=="男"?"selected":""}>男</option>
<option ${student.sex=="女"?"selected":""}>女</option>
<option ${student.sex=="どちらでもない"?"selected":""}>どちらでもない</option>

</select>

</td>

<td rowspan="3">

<label>備考</label><br>

<textarea name="memo">${student.memo}</textarea>

</td>

</tr>

<tr>

<td>

<label><span class="must">*</span> あっせん状況</label><br>

<select name="status">

<option ${student.status=="継続"?"selected":""}>継続</option>
<option ${student.status=="辞退"?"selected":""}>辞退</option>

</select>

</td>

</tr>

<tr>

<td>

<label><span class="must">*</span> 在籍状況</label><br>

<select name="schoolStatus">

<option ${student.schoolStatus=="在籍"?"selected":""}>在籍</option>
<option ${student.schoolStatus=="卒業"?"selected":""}>卒業</option>
<option ${student.schoolStatus=="休学"?"selected":""}>休学</option>
<option ${student.schoolStatus=="退学"?"selected":""}>退学</option>

</select>

</td>

</tr>

</table>

<div class="buttonArea">

<button class="submitButton">

変更

</button>

</div>

</form>

</div>

</body>
</html>