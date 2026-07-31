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

	   /* ============ 「…」ボタン（指導一覧と同じデザイン） ============ */
	   .more-btn {
	       width: 26px;
	       height: 26px;
	       background: #ddd;
	       border: 1px solid #999;
	       color: #333;
	       font-size: 14px;
	       font-weight: bold;
	       line-height: 1;
	       cursor: pointer;
	       border-radius: 3px;
	   }
	   .more-btn:hover {
	       background: #ccc;
	   }
	   .action-col {
	       text-align: center;
	   }

	   /* ============ 右クリック風メニュー（指導一覧と同じデザイン） ============ */
	   .ctx-menu {
	       position: fixed;
	       background: #fff;
	       border: 1px solid #999;
	       box-shadow: 0 2px 6px rgba(0,0,0,0.25);
	       min-width: 100px;
	       padding: 0;
	       font-size: 13px;
	       z-index: 1000;
	       display: none;
	       overflow: hidden;
	   }
	   .ctx-menu button {
	       display: block;
	       width: 100%;
	       text-align: center;
	       padding: 10px 12px;
	       border: none;
	       font-size: 14px;
	       font-weight: bold;
	       cursor: pointer;
	       color: #fff;
	   }
	   .ctx-menu button[data-action="edit"] {
	       background: #29ABE2;
	   }
	   .ctx-menu button[data-action="edit"]:hover {
	       background: #1f8fc0;
	   }
	   .ctx-menu button[data-action="delete"] {
	       background: #d9001b;
	   }
	   .ctx-menu button[data-action="delete"]:hover {
	       background: #b40016;
	   }

	   /* ============ 削除確認モーダル（指導一覧と同じデザイン） ============ */
	   .modal-overlay {
	       display: none;
	       position: fixed;
	       top: 0; left: 0; right: 0; bottom: 0;
	       background: rgba(0,0,0,0.4);
	       z-index: 2000;
	       align-items: center;
	       justify-content: center;
	   }
	   .modal-overlay.open {
	       display: flex;
	   }
	   .modal-box {
	       background: #fff;
	       border: 1px solid #999;
	       width: 320px;
	       padding: 20px;
	       box-shadow: 0 4px 12px rgba(0,0,0,0.3);
	   }
	   .modal-box h3 {
	       margin: 0 0 12px;
	       font-size: 15px;
	       border-left: 4px solid #d9001b;
	       padding-left: 8px;
	   }
	   .modal-box p {
	       font-size: 13px;
	       color: #333;
	       margin: 0 0 20px;
	   }
	   .modal-box .target-name {
	       font-weight: bold;
	       color: #003366;
	   }
	   .modal-buttons {
	       display: flex;
	       justify-content: flex-end;
	       gap: 8px;
	   }
	   .modal-buttons button {
	       font-size: 13px;
	       padding: 6px 16px;
	       border: none;
	       cursor: pointer;
	   }
	   .btn-cancel {
	       background: #ccc;
	       color: #333;
	   }
	   .btn-delete {
	       background: #d9001b;
	       color: #fff;
	       font-weight: bold;
	   }
</style>
</head>
<body>

 <div class="top-area">
<button class="back-button" onclick="location.href='<%= request.getContextPath() %>/ListofEmployment'">◀</button>

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
			<th class="action-col"></th>
		</tr>
		
	</thead>
	<tbody id="companyTable">
	<%
List<Company> list = (List<Company>)request.getAttribute("companyList");

if(list != null){

    for(Company c : list){
%>
	
	<tr data-id="<%= c.getId() %>">
			    <td><%= c.getId() %></td>
			    <td class="name-cell"><a href="<%= request.getContextPath() %>/ListofExamStudents?companyId=<%= c.getId() %>"><%= c.getName() %></a></td>
				<td><%= c.getAddress() %></td>
				<td><%= c.getTel() %></td>
				<td><%= c.getMail() %></td>
				<td><%= c.getJobtype() %></td>

<<<<<<< HEAD
			  
=======

		

				    <div class="menu" style="display:none;">
				      <button type="button" onclick="location.href='<%= request.getContextPath() %>/CompanyEdit?companyId=<%= c.getId() %>'">変更</button><br>
				      <button type="button"  onclick="if(confirm('「<%= c.getName() %>」削除しますか？')){location.href='<%=request.getContextPath()%>/CompanyDelete?companyId=<%= c.getId() %>';}">削除</button>
				    </div>
				  
>>>>>>> branch 'main' of git@github.com:lintailangq7-ux/JobSystemManagement.git

<<<<<<< HEAD

=======
>>>>>>> branch 'main' of git@github.com:lintailangq7-ux/JobSystemManagement.git
			    <td class="action-col">
					<button type="button" class="more-btn" data-row="<%= c.getId() %>">&hellip;</button>

				 </td>
				</tr>
				<%
    }
}

%>
				</tbody>

</table>

<<<<<<< HEAD
<button class="add-button" type="button" onclick="location.href='/CompanyUpdate'">登録</button>

<button class="add-button" type="button" onclick="location.href='CompanyUpdateServlet'">登録</button>
=======
<button class="add-button" type="button" onclick="location.href='CompanyUpdate'">登録</button>
>>>>>>> branch 'main' of git@github.com:lintailangq7-ux/JobSystemManagement.git

<!-- 右クリック風メニュー -->
<div class="ctx-menu" id="ctxMenu">
    <button data-action="edit">変更</button>
    <button data-action="delete">削除</button>
</div>

<!-- 「変更」はformでGET送信して遷移する -->
<form id="editForm" action="<%= request.getContextPath() %>/CompanyEditServlet" method="get">
    <input type="hidden" name="companyId" id="editCompanyId" value="">
</form>

<!-- 「削除」もformでPOST送信する -->
<form id="deleteForm" action="<%= request.getContextPath() %>/CompanyDeleteServlet" method="post">
    <input type="hidden" name="companyId" id="deleteCompanyId" value="">
</form>

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

<script>
const menu = document.getElementById('ctxMenu');
const modalOverlay = document.getElementById('modalOverlay');
const modalTargetName = document.getElementById('modalTargetName');
const editForm = document.getElementById('editForm');
const editCompanyId = document.getElementById('editCompanyId');
const deleteForm = document.getElementById('deleteForm');
const deleteCompanyId = document.getElementById('deleteCompanyId');
let currentRow = null;
let currentTr = null;

// 「…」ボタンをクリックしたらメニュー表示
document.querySelectorAll('.more-btn').forEach(btn => {
    btn.addEventListener('click', (e) => {
        e.stopPropagation();
        currentRow = btn.dataset.row;
        currentTr = btn.closest('tr');

        const rect = btn.getBoundingClientRect();
        menu.style.display = 'block';

        const menuWidth = menu.offsetWidth;
        let left = rect.right - menuWidth;
        if (left < 4) left = rect.left;

        menu.style.left = left + 'px';
        menu.style.top = (rect.bottom + 4) + 'px';
    });
});

// メニュー項目をクリックしたときの処理
document.querySelectorAll('.ctx-menu button').forEach(item => {
    item.addEventListener('click', () => {
        menu.style.display = 'none';
        const action = item.dataset.action;

        if (action === 'delete') {
            // 削除 → 確認モーダルを表示
            const nameCell = currentTr ? currentTr.querySelector('.name-cell') : null;
            const name = nameCell ? nameCell.textContent.trim() : '';
            modalTargetName.textContent = '企業ID ' + currentRow + (name ? '（' + name + '）' : '');
            modalOverlay.classList.add('open');
        } else if (action === 'edit') {
            // 変更 → 隠しformに企業IDをセットしてGET送信で編集画面へ
            editCompanyId.value = currentRow;
            editForm.submit();
        }
    });
});

// モーダル：キャンセル
document.getElementById('modalCancel').addEventListener('click', () => {
    modalOverlay.classList.remove('open');
});

// モーダル：削除確定 → サーバーにPOST送信してDBから削除
document.getElementById('modalConfirm').addEventListener('click', () => {
    if (currentRow) {
        deleteCompanyId.value = currentRow;
        deleteForm.submit();
    }
});

// モーダルの背景クリックでも閉じる
modalOverlay.addEventListener('click', (e) => {
    if (e.target === modalOverlay) {
        modalOverlay.classList.remove('open');
    }
});

// メニュー外をクリックしたら閉じる
document.addEventListener('click', () => {
    menu.style.display = 'none';
});
</script>

</body>
</html>
