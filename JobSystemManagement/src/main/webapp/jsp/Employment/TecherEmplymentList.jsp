<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"
         import="java.time.format.DateTimeFormatter,
                 DAO.StudentDetailDAO, model.StudentDetail,
                 model.GuidanceDetail, model.ModelStudent,
                 model.EmploymentChukan,java.util.List,
                 model.StudentChukan, model.CompanyChukan" %>
<!DOCTYPE html>
<html lang="ja">
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

<h1>指導情報一覧</h1>
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
<%
	// ここで先にデータを取得し、選考状況（EmploymentChukan）の最大件数を求めておく。
	// この最大件数を元にヘッダーの列数を動的に決める。
	List<StudentDetail> detail = (List<StudentDetail>) session.getAttribute("detail");
	DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("M/d");

	int maxExamCount = 0;
	if (detail != null) {
		for (StudentDetail d : detail) {
			if (d.getGuidanceList() != null) {
				for (GuidanceDetail g : d.getGuidanceList()) {
					if (g.getExamHistory() != null && g.getExamHistory().size() > maxExamCount) {
						maxExamCount = g.getExamHistory().size();
					}
				}
			}
		}
	}
%>
<table border="1">
	<thead>
		<tr>
			<th>指導ID	</th>
			<th>氏名</th>
			<% for (int i = 0; i < maxExamCount; i++) { %>
				<th>選考状況<%= i + 1 %></th>
			<% } %>
			<th>備考</th>
		</tr>
		
	</thead>
	<tbody id="companyTable">
<%
    for(StudentDetail d : detail){
    	for(GuidanceDetail g :d.getGuidanceList()){
%>
	<tr>
			    <td><%= g.getShidoId() %></td>
			    <td><a href="<%= request.getContextPath() %>/ListofExamStudents?companyId=<%= d.getStudent().getGakusekiNo() %>"><%= g.getCompany().getKaishaName() %></a></td>
				<td><%= g.getCompany().getKaishaName() %></td>
				<td><%= d.getStudent().getName() %></td>
				
				<%
					// 選考状況：最大件数分の列を必ず出力し、データが無い分は空セルで埋める
					for (int i = 0; i < maxExamCount; i++) {
						if (examList != null && i < examList.size()) {
				%>
							<td><%= examList.get(i).getShikenNaiyo() %></td>
				<%
						} else {
				%>
							<td>&nbsp;</td>
				<%
						}
					}
				%>
				 
				<td><%= g.getCompany().getEmail() %></td>
				<td><%for(CompanyChukan c :g.getCompany().getCompanyChukanList()){  %>
				 	<%= c.getBoshuShokushu() %>・
				 	<%} %>
				</td>
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

