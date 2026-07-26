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
    margin:0;
}

.container{
    width:1200px;
    margin:20px 0 20px 20px;
}

.titleArea{
    display:flex;
    align-items:center;
    margin-bottom:20px;
}

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

.title{
    font-size:34px;
    font-weight:bold;
    margin-left:20px;
}

.main{
    display:flex;
    gap:70px;
    align-items:flex-start;
}

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

.right{
    flex:1;
}

.formTable{
    border-collapse:collapse;
}

.formTable td{
    padding:5px 10px 5px 5px;
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

<% if (request.getAttribute("errorMessage") != null) { %>
<div style="color:red; font-weight:bold; margin-bottom:10px;"><%= request.getAttribute("errorMessage") %></div>
<% } %>

<div class="titleArea">

<button class="backButton" onclick="history.back()"></button>

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
<td>${student.name}</td>
</tr>

<tr>
<td>クラス</td>
<td>${student.className}</td>
</tr>

<tr>
<td>学籍番号</td>
<td>
${student.gakusekiNo}
</td>
</tr>

<tr>
<td>内定状況</td>
<td>${employment.naiteiKakutei == 1 ? '内' : '未'}</td>
</tr>

</table>

</div>

<!-- 右側 -->

<div class="right">

<form action="<%= "edit".equals(request.getAttribute("mode")) ? "EmploymentCangeServlet" : "EmploymentNewServlet" %>" method="post">

<input type="hidden"
name="studentNo"
value="${student.gakusekiNo}">

<input type="hidden"
name="shidoId"
value="${shidoId}">

<table class="formTable">

<tr>

<td>企業ID</td>

<td>

<input type="text"
name="companyId"
value="${company.id}">

</td>

</tr>

<tr>

<td>企業名</td>

<td>

<input type="text"
name="companyName"
value="${company.name}">

</td>

</tr>

<tr>

<td>試験会場</td>

<td>

<input type="text"
name="place"
value="${chukan.shikenKaijo}">

</td>

</tr>

<tr>

<td>提出状況</td>

<td>

<input type="text"
name="submitStatus"
value="${chukan.teishutsuShoruiJokyo == 1 ? '済' : ''}">

</td>

</tr>

<tr>

<td>試験内容</td>

<td>

<input type="text"
name="exam"
value="${chukan.shikenNaiyo}">

</td>

</tr>

<tr>

<td>試験日時</td>

<td>

<input type="text"
name="examDate"
value="${chukan.shikenNichiji}">

</td>

</tr>

<tr>

<td>内定確定</td>

<td>

<input type="text"
name="offerStatus"
value="${employment.naiteiKakutei == 1 ? '内' : ''}">

</td>

</tr>

<tr>

<td>内定承諾日</td>

<td>

<input type="text"
name="acceptDate"
value="${employment.naiteiKakuteiBi}">

</td>

</tr>

<tr>

<td>備考</td>

<td>

<textarea
name="memo">${employment.biko}</textarea>

</td>

</tr>

</table>

<div class="buttonArea">

<button class="submitButton">

<%= "edit".equals(request.getAttribute("mode")) ? "変更" : "追加" %>

</button>

</div>

</form>

</div>

</div>

</div>

</body>
</html>