<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>指導変更</title>

<style>

body{
    font-family:Meiryo;
    background:#fff;
}

.container{
    width:1200px;
    margin:20px auto;
}

.titleArea{
    display:flex;
    align-items:center;
    margin-bottom:20px;
}

.backButton{

    width:110px;
    height:45px;
    background:#5dc9ff;
    color:white;
    border:2px solid #000;
    font-size:18px;
    cursor:pointer;
}

.title{

    font-size:34px;
    font-weight:bold;
    margin-left:20px;
}

.main{

    display:flex;
    gap:50px;
}

/******** 左側 ********/

.left{

    width:260px;
}

.infoTable{

    width:100%;
    border-collapse:collapse;
}

.infoTable th{

    border:2px solid black;
    background:#f3f3f3;
    height:40px;
}

.infoTable td{

    border:2px solid black;
    text-align:center;
    height:42px;
}

/******** 右側 ********/

.right{

    flex:1;
}

.formTable{

    border-collapse:collapse;
}

.formTable td{

    padding:5px;
}

label{

    font-weight:bold;
}

input[type=text]{

    width:300px;
    height:38px;
    text-align:center;
    border:2px solid black;
    font-size:18px;
}

textarea{

    width:430px;
    height:160px;
    border:2px solid black;
    font-size:18px;
}

.buttonArea{

    text-align:right;
    margin-top:20px;
}

.submitButton{

    width:110px;
    height:60px;
    background:red;
    color:white;
    border:none;
    border-radius:10px;
    font-size:24px;
    font-weight:bold;
    cursor:pointer;
}

</style>

</head>
<body>

<div class="container">

<div class="titleArea">

<button class="backButton"
onclick="history.back()">

← 戻る

</button>

<div class="title">

指導変更

</div>

</div>

<div class="main">

<!-- 左側 生徒情報 -->

<div class="left">

<table class="infoTable">

<tr>
<th colspan="2">
生徒情報
</th>
</tr>

<tr>
<td>名前</td>
<td>${student.studentName}</td>
</tr>

<tr>
<td>クラス</td>
<td>${student.className}</td>
</tr>

<tr>
<td>希望職種</td>
<td>
${student.job1}
${student.job2}
${student.job3}
</td>
</tr>

<tr>
<td>内定状況</td>
<td>${student.offerStatus}</td>
</tr>

</table>

</div>

<!-- 右側 -->

<div class="right">

<form action="GuidanceUpdateServlet" method="post">

<input type="hidden"
name="studentNo"
value="${student.studentNo}">

<table class="formTable">

<tr>

<td>企業ID</td>

<td>

<input type="text"
name="companyId"
value="${guidance.companyId}">

</td>

</tr>

<tr>

<td>企業名</td>

<td>

<input type="text"
name="companyName"
value="${guidance.companyName}">

</td>

</tr>

<tr>

<td>試験会場</td>

<td>

<input type="text"
name="place"
value="${guidance.place}">

</td>

</tr>

<tr>

<td>提出状況</td>

<td>

<input type="text"
name="submitStatus"
value="${guidance.submitStatus}">

</td>

</tr>

<tr>

<td>試験内容</td>

<td>

<input type="text"
name="exam"
value="${guidance.exam}">

</td>

</tr>

<tr>

<td>試験日時</td>

<td>

<input type="text"
name="examDate"
value="${guidance.examDate}">

</td>

</tr>

<tr>

<td>内定確定</td>

<td>

<input type="text"
name="offerStatus"
value="${guidance.offerStatus}">

</td>

</tr>

<tr>

<td>内定承諾日</td>

<td>

<input type="text"
name="acceptDate"
value="${guidance.acceptDate}">

</td>

</tr>

<tr>

<td>備考</td>

<td>

<textarea
name="memo">${guidance.memo}</textarea>

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

</div>

</div>

</body>
</html>