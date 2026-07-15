<%@ page import="java.util.List" %>
<%@ page import="model.Company" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>企業一覧</title>
<style>
	.top-area{
	    display:flex;
	    justify-content:flex-start;
	    align-items:center;
	    margin-bottom:10px;
		gap:10px;
		
	}

	
	.top-area h1{
		position: relative;
	    margin:0;
	    font-size:30px;
	    padding:0;
	    font-weight:normal;
		
		
		
	}
	.back-button{
		width: 40px;
		height: 40px;
		border: 1px solid #4aa3df;
	    background: #4aa3df;
		color: #fff35c;
		font-size: 24px;
		cursor: pointer;
		padding: 0;
	}
	



	.search-area{
	    display: flex;
	    justify-content: flex-end;  /* 右寄せ */
	    /*margin-bottom: 20px;*/
		padding: 10px;
		box-sizing: border-box;
		max-width: 100%;
	}
	.search-box{
		position: relative;
		width: 200px;
		display: flex; 
		justify-content: flex-end;
	}
	.search-box input{
	    width:250px;
	    height: 30px;
	    border: 3px solid #5b9bd5;
	    border-radius: 20px;
	    font-size: 18px;
	    outline: none;
		padding-left: 10px;
	}
	.search-btn{
	    position: absolute;
	    top: 50%;
	    right: 10px;
	    transform: translateY(-50%);
	    width: 30px;
	    height: 30px;
	    border: none;
	    background: transparent;
	    cursor: pointer;
	    font-size: 18px;

	    
	}
	
	table {
	     width: 100%;
	     border-collapse: collapse;
	   }
	   th {
	     background: #0d6fb8;
	     color: #fff;
	     padding: 10px;
	     border: 1px solid #000;
	   }
	   td:last-child{
		text-align: center;
	   }
	   .add-button {
	     position: fixed;
	     right: 30px;
	     bottom: 30px;
	     width: 100px;
	     height: 60px;
	     background: #ff0000;
	     color: #fff;
	     border: none;
	     border-radius: 12px;
	     font-size: 24px;
	     cursor: pointer;
	   }
	   .detail-btn {
	       width: 40px;
	       height:32px;
		   font-size: 12px;
		   letter-spacing: -1px;
		   line-height: 30px;
		   text-align: center;
	   }
	   .menu-cell {
	     position: relative;
	   }

	   .menu {
		display: none;
        position: absolute; 
		top: 0; 
		right: calc(100% + 10px); 
		background: #fff; 
		border: 1px solid #ccc; 
		gap:0;
	   }

	   .menu button {
	     display: block;
	     width: 80px;
		 white-space: nowrap;
		 writing-mode: horizontal-tb;
		 margin:0;
	   }
</style>
</head>
<body>

 <div class="top-area">
<button class="back-button" onclick="location.href='/jsp/Login.jsp'">◀</button>

<h1>企業一覧</h1>
</div>
<div class="search-area">
<form action="<%= request.getContextPath() %>/ListofCompanies" method="get">
 <div class="search-box">
<input type="text" name="keyword" placeholder="企業名">

<button type="submit" class="search-btn">
🔍
</button>
</div>
</form>
</div>
<table border="1">
	<thead>
		<tr>
			<th>ID</th>
			<th>企業名</th>
			<th>住所</th>
			<th>TEL</th>
			<th>メールアドレス</th>
			<th>募集職種</th>
			<th>詳細</th>
		</tr>
		
	</thead>
	<tbody id="companyTable">
	<%
List<Company> list = (List<Company>)request.getAttribute("companyList");

if(list != null){

    for(Company c : list){
%>
	
	
	<tr>
			    <td><%= c.getId() %></td>
			    <td><%= c.getName() %></td>
				<td><%= c.getAddress() %></td>
				<td><%= c.getTel() %></td>
				<td><%= c.getMail() %></td>
				<td><%= c.getJobtype() %></td>
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
<button class="add-button" type="button" onclick="location.href=''">追加</button>
<script>
function toggleActions(btn) {
    const actions = btn.nextElementSibling;

    if(actions.style.display === "none"){
        actions.style.display = "block";
    }else{
        actions.style.display = "none";
    }
}
</script>

</body>
</html>