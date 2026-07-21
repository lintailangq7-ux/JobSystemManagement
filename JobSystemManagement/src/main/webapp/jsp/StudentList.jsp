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


<div class="header-row">
	  <div class="title-box">
	    <button class="title-arrow"onclick="location.href='https://teams.microsoft.com/v2/';">
	      <svg viewBox="0 0 24 24"><polygon points="18,2 18,22 4,12" fill="#FFE600"/></svg>
	    </button>
	    <div class="title-text">学生情報一覧</div>
	  </div>

<button class="backButton" onclick="history.back()"></button>

<div class="title">

<<<<<<< HEAD
指導変更
=======
	<div class="table-wrapper">
	<table>
		<tr>
			<th>学籍番号</th>
			<th>クラス</th>
			<th>番号</th>
			<th>氏名</th>
			<th>性別</th>
			<th>あっせん状況</th>
			<th>在籍状況</th>
			<th>希望地域</th>
			<th>希望業種1</th>
			<th>希望業種2</th>
			<th>希望業種3</th>
			<th>備考</th>
			<th>　</th>
		</tr>

		
	<% for(ModelStudent SD : StuList){ %>
		<tr data-id=<%= SD.getGakusekiNo()%>>
			<td class="rowhead"><%=SD.getGakusekiNo() %></td>
			<td><%=SD.getClassName() %></td>
			<td><%=SD.getAttendanceNo() %></td>
			<td class="name-cell"><%=SD.getName() %></td>
			<%String Sei = SD.getSeibetsu();
			  if(Sei.equals("F")){ %>
			  	<td>女</td>
			<% }else if(Sei.equals("M")){%>
				 <td>男</td>
			<% }else if(Sei.equals("X")){%>
				 <td>未</td>
			 <%} %>	
			 <%
			   	int assen = SD.getAssen();
			 	if(assen == 1){%>
			 	<td>あっせん中</td>	
			 <%}else if(assen ==2){ %>
			 	<td>辞退</td>	
			 <%} %>
			 <%
			   int zai = SD.getZaisekiJokyo();
			   if(zai == 1){%>
			 	<td>在学</td>
			 <% }else if(zai ==2){ %>
			 	<td>卒業</td>
			 <% }else if(zai ==3){ %>
			 	<td>退学</td>
			 <% }else if(zai ==4){ %>
				<td>留年</td>
			<%}%>
		
			<td><%=SD.getKenNaiGaiKibo() %></td>
			<%
			  List<StudentChukan> chukanList = SD.getGakuseiChukanList();
			    for (int i = 0; i <= 2; i++) { 
			        if (chukanList != null && i < chukanList.size()) {
			        	%>
			        	        <td><%= chukanList.get(i).getKibouShokushu() %></td>
			        	<%
			        	        } else {
			        	%>
			        	        <td>-</td>
			        	   <%} %>
			<%} %>
			<td><%=SD.getBiko() %></td>
			<td><button class="more-btn" data-row=<%=SD.getGakusekiNo() %>>&hellip;</button></td>
		</tr>
		<%} %>
	</table>
	</div>

	<div class="ctx-menu" id="ctxMenu">
	  <button data-action="edit">変更</button>
	  <button data-action="delete">削除</button>
  	</div>

	<!-- 削除確認モーダル -->
	<div class="modal-overlay" id="modalOverlay">
	  <div class="modal-box">
	    <h3>削除の確認</h3>
	    <p><span class="target-name" id="modalTargetName"></span> を削除します。<br>この操作は元に戻せません。</p>
	    <div class="modal-buttons">
	      <button class="btn-cancel" id="modalCancel">キャンセル</button>
	      <button class="btn-delete" id="modalConfirm">削除する</button>
	    </div>
	  </div>
	</div>

	<br>
	<div class="footer-row">
	<button class="register" onclick="location.href='https://teams.microsoft.com/v2/';">登録</button>
	</div>
>>>>>>> branch 'main' of git@github.com:lintailangq7-ux/JobSystemManagement.git

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