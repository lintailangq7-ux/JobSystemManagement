<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@	page import="java.time.format.DateTimeFormatter,
            DAO.StudentDetailDAO, model.StudentDetail,
            model.GuidanceDetail, model.ModelStudent,
            model.EmploymentChukan, java.util.List,
            model.StudentChukan, model.CompanyChukan" %>
    <%
     StudentDetail detail = (StudentDetail) session.getAttribute("detail");
    
    
    
    %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>指導登録</title>

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

/* ホバー */
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

<button class="backButton" onclick="history.back()"></button>

<div class="title">

指導登録

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
<td><%= detail.getStudent().getName() %></td>
</tr>

<tr>
<td>クラス</td>
<td><%= detail.getStudent().getClassName()%></td>
</tr>

<tr>
<%
	    int idx = 1;
	    for(StudentChukan Sc : detail.getStudent().getGakuseiChukanList()){
	%>
	    <tr>
	        <td class="header">志望業種<%= idx %></td>
	        <td><%= (Sc.getKibouShokushu() != null) ? Sc.getKibouShokushu() : "-" %></td>
	    </tr>
	<%
	        idx++;
	    }
	%>
<tr>
<td>内定状況</td>
			<%
			boolean naitei = false;

			for (GuidanceDetail Ed : detail.getGuidanceList()) {
				if (Ed.getNaiteiKakutei() == 1) {
					naitei = true;
					break; // 1件見つかったら終了
				}
			}
			%>

				<td><%= naitei ? "内" : "未" %></td>
		        
</tr>

</table>

</div>

<!-- 右側 -->

<div class="right">

<form action="EmploymentNewServlet" method="Post">

<input type="hidden"name="studentNo"value="<%  detail.getStudent().getGakusekiNo();%>">

<table class="formTable">

<tr>

<td>企業ID</td>

<td>

<input type="text"name="companyId"value="${guidance.companyId}">

</td>

</tr>

<tr>

<td>企業名</td>

<td>

<input type="text"name="companyName"value="${guidance.companyName}">

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

<input type="text"name="submitStatus"value="${guidance.submitStatus}">

</td>

</tr>

<tr>

<td>試験内容</td>

<td>

<input type="text"name="exam"value="${guidance.exam}">

</td>

</tr>

<tr>

<td>試験日時</td>

<td>

<input type="text"name="examDate"value="${guidance.examDate}">

</td>

</tr>


<tr>

<td>備考</td>

<td>

<textarea name="memo">${guidance.memo}</textarea>

</td>

</tr>

</table>

<div class="buttonArea">

<button class="submitButton">

登録

</button>

</div>

</form>

</div>

</div>

</div>

</body>
</html>