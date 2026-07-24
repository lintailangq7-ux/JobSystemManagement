<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="model.ModelStudent, model.StudentChukan, java.util.List" %>
<%
    ModelStudent student = (ModelStudent) request.getAttribute("student");
    String emg = (String) request.getAttribute("emg");

    List<StudentChukan> chukanList = (student != null) ? student.getGakuseiChukanList() : null;
    String job1 = (chukanList != null && chukanList.size() > 0) ? chukanList.get(0).getKibouShokushu() : "";
    String job2 = (chukanList != null && chukanList.size() > 1) ? chukanList.get(1).getKibouShokushu() : "";
    String job3 = (chukanList != null && chukanList.size() > 2) ? chukanList.get(2).getKibouShokushu() : "";
%>
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

.titleArea{
    display:flex;
    align-items:center;
    margin-bottom:30px;
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

.backButton:active{
    transform:translateY(2px);
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
    <p><%=emg%></p>
    <button onclick="document.getElementById('okDialog').close()">OK</button>
</dialog>

<script>
    window.onload = function() {
        <% if (emg != null && !emg.isEmpty()) { %>
            document.getElementById('okDialog').showModal();
        <% } %>
    };
</script>

<div class="container">

<div class="titleArea">

<button type="button"
class="backButton"
onclick="history.back()"
title="戻る">
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
<input type="text" name="className" value="<%=student.getClassName()%>">

</td>

<td>

<label><span class="must">*</span> 希望地域</label><br>
<select name="area">
    <option value="M" <%=student.getKenNaiGaiKibo().equals("M") ? "selected" : ""%>>県内</option>
    <option value="F" <%=student.getKenNaiGaiKibo().equals("F") ? "selected" : ""%>>県外</option>
    <option value="X" <%=student.getKenNaiGaiKibo().equals("X") ? "selected" : ""%>>その他</option>
</select>

</td>

</tr>

<tr>

<td>

<label><span class="must">*</span> 出席番号</label><br>
<input type="text" name="attendanceNo" value="<%=student.getAttendanceNo()%>">

</td>

<td>

<label>希望職種</label><br>
<input type="text" name="job1" value="<%=job1%>">

</td>

</tr>

<tr>

<td>

<label><span class="must">*</span> 学籍番号</label><br>
<input type="text" name="studentNo"
value="<%=student.getGakusekiNo()%>" readonly>

</td>

<td>

<label>希望職種2</label><br>
<input type="text" name="job2" value="<%=job2%>">

</td>

</tr>

<tr>

<td>

<label><span class="must">*</span> 氏名</label><br>
<input type="text" name="studentName"
value="<%=student.getName()%>">

</td>

<td>

<label>希望職種3</label><br>
<input type="text" name="job3" value="<%=job3%>">

</td>

</tr>

<tr>

<td>

<label><span class="must">*</span> 性別</label><br>

<select name="sex">
    <option value="M" <%=student.getSeibetsu().equals("M") ? "selected" : ""%>>男</option>
    <option value="F" <%=student.getSeibetsu().equals("F") ? "selected" : ""%>>女</option>
    <option value="X" <%=student.getSeibetsu().equals("X") ? "selected" : ""%>>どちらでもない</option>
</select>

</td>

<td rowspan="3">

<label>備考</label><br>

<textarea name="memo"><%=student.getBiko()%></textarea>

</td>

</tr>

<tr>

<td>

<label><span class="must">*</span> あっせん状況</label><br>

<select name="status">
    <option value="1" <%=student.getAssen()==1 ? "selected" : ""%>>継続</option>
    <option value="2" <%=student.getAssen()==2 ? "selected" : ""%>>辞退</option>
</select>

</td>

</tr>

<tr>

<td>

<label><span class="must">*</span> 在籍状況</label><br>

<select name="schoolStatus">
    <option value="1" <%=student.getZaisekiJokyo()==1 ? "selected" : ""%>>在籍</option>
    <option value="2" <%=student.getZaisekiJokyo()==2 ? "selected" : ""%>>卒業</option>
    <option value="3" <%=student.getZaisekiJokyo()==3 ? "selected" : ""%>>休学</option>
    <option value="4" <%=student.getZaisekiJokyo()==4 ? "selected" : ""%>>退学</option>
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