<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="model.StudentList" %>

<%
String companyName = (String) request.getAttribute("companyName");
List<StudentList> studentList =
        (List<StudentList>) request.getAttribute("studentList");
%>


<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>受験学生一覧</title>
<style>
body{
    margin:0;
    padding:0;
    background:#ffffff;
    font-family:"Yu Gothic","Meiryo",sans-serif;
}

/* ヘッダー */
.top-area{
    display:flex;
	    justify-content:flex-start;
	    align-items:center;
	    margin-bottom:10px;
		gap:10px;
		
}
.top-area h1{
    position:relative;
    margin:0;
    font-size:30px;
    padding:0;
    font-weight:normal;
}

/* 戻るボタン */
.back-botton{
    width: 40px;
		height: 40px;
		border: 1px solid #4aa3df;
	    background: #4aa3df;
		color: #fff35c;
		font-size: 24px;
		cursor: pointer;
		padding: 0;
}



/* 企業名 */
.company{
    padding:0 ;
    height:auto;
    line-height:55px;
    text-align:center;
    border:none;
    border-radius:40px;
    color:#006fb7;
    font-size:20px;
    font-weight:bold;
    margin-left:50px;/*下に移動*/
    width:auto;/*文字に合わせる*/
    white-space:nowrap;/*改行しない*/
     flex-shrink: 0;
     
    
}

/* テーブル全体 */
.table-area{
	width: 90%;
    margin-top:20px;
    display:flex;
    justify-content:center;
}

/* テーブル */
table{
    border-collapse:collapse;
    width:100%;
	table-layout: fixed;
}

/* ヘッダー */
th{
    background:#006fb7;
    color:white;
    border:2px solid #222;
    font-size:20px;
    height:45px;
}

/* データ */
td{
    border:2px solid #222;
    text-align:center;
    font-size:18px;
    height:45px;
}

/* ボタン列 */
.btn-col{
    width:60px;
}

/* 詳細ボタン */
.detail-btn{
    width:55px;
    height:40px;
    border-radius:8px;
    background:#bdb7b7;
    border:1px solid #444;
    color:white;
    font-size:26px;
    cursor:pointer;
}

/* スクロールバー */
.scroll{
    position:absolute;
    right:90px;
    top:115px;
    width:28px;
    height:420px;
    background:#777;
    display:flex;
    flex-direction:column;
    align-items:center;
}

.scroll button{
    width:100%;
    height:40px;
    background:#777;
    color:white;
    border:none;
    font-size:20px;
    cursor:pointer;
}

.bar{
    flex:1;
    width:8px;
    background:white;
    margin:5px 0;
}
.class-no{
	width:20%;
}
.number{
	width:20%;
}
.name{
	width:40%;
}
.button-col{
	width:15%;
}

</style>
</head>

<body>
<div class="top-area">

    <!-- 戻るボタン -->
    <button class="back-botton"
        onclick="location.href='<%=request.getContextPath()%>/ListofCompanies'">
        ◀
    </button>

    <!-- タイトル -->
    
       <h1> 受験学生一覧</h1>
    

    <!-- 企業名 -->
    <div class="company">
        <%= companyName %>
    </div>

</div>

<div class="table-area">

<table>

    <thead>
        <tr>
            <th class="class-no">クラス番号</th>
            <th class="number">出席番号</th>
            <th class="name">氏名</th>
            <th class="button-col">詳細</th>
        </tr>
    </thead>

    <tbody>

    <%
    if (studentList != null) {
        for (StudentList s : studentList) {
    %>

        <tr>
            <td><%= s.getClassNo() %></td>
            <td><%= s.getAttendanceNo() %></td>
            <td><%= s.getStudentName() %></td>
            <td class="menu-cell">
					
				    <button type="button" class="detail-btn"onclick="toggleActions(this)">詳細</button>
				    <div class="menu" style="display:none;">
				      <button type="button" onclick="alert('企業情報を変更しますか')">変更</button><br>
				      <button type="button" onclick="alert('企業情報を削除しますか')">削除</button>
				    </div>
				  
				 </td>
        </tr>

    <%
        }
    }
    %>

    </tbody>

</table>

</div>
</body>
</html>