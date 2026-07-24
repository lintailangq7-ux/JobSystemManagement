<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    <%@ page import="model.ModelStudent,model.StudentChukan, java.util.List, java.util.ArrayList" %>
<%
 	ModelStudent Sdata = (ModelStudent)session.getAttribute("Sdata");
	List<ModelStudent> StuList = (List<ModelStudent>)request.getAttribute("StuList");
	String emg = (String) request.getAttribute("emg");
	String keyword = (String) request.getAttribute("keyword");
	
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>学生一覧</title>
<style>
  body {
    font-family: "MS PGothic", "Meiryo", sans-serif;
    background: #ffffff;
    padding: 20px;
  }
  .container {
    max-width: 900px;
    position: relative;
  }

  /* header */
  .header-row {
    display: flex;
    align-items: center;
    margin-bottom: 20px;
  }
  .title-box {
    display: flex;
    align-items: center;
    border: 1px solid #999;
    width: 300px;
    height: 60px;
  }
  .title-arrow {
    width: 60px;
    height: 100%;
    background: #29ABE2;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .title-arrow svg { width: 30px; height: 30px; }
  .title-text {
    font-size: 20px;
    font-weight: bold;
    margin-left: 14px;
  }

  .search-area {
    margin-left: 20px;
  }

  .search-box {
    display: inline-block;
    border: 2px solid #29abe2;
    border-radius: 20px;
    padding: 4px 8px;
  }

  .search-box input {
    border: none;
    outline: none;
    background: transparent;
    font-size: 14px;
  }

  .search-btn {
    border: none;
    background: transparent;
    cursor: pointer;
    font-size: 16px;
  }

  /* スクロール関係div=""で使う */
  .table-wrapper {
    max-height: 440px;
    overflow-y: auto;
    border: 1px solid #999;
  }

  /* table */
  table {
    border-collapse: collapse;
    width: 100%;
  }
  th {
    background: #1f5fa8;
    color: #fff;
    font-size: 13px;
    padding: 6px 4px;
    border: 1px solid #999;
    position: sticky;
    top: 0;
    z-index: 1;
  }
  td {
    border: 1px solid #999;
    font-size: 13px;
    padding: 6px 4px;
    text-align: center;
    height: 26px;
  }
  td.rowhead {
    background: #29ABE2;
    color: #003366;
    font-weight: bold;
  }
  tr.empty td.rowhead {
    background: #29ABE2;
  }
  .btn-more {
    background: #999;
    color: #fff;
    border: none;
    padding: 3px 10px;
    font-size: 12px;
    cursor: pointer;
  }

  /* scroll arrow */
  .scroll-arrow {
    position: absolute;
    right: -70px;
    top: 90px;
    width: 40px;
    height: 400px;
    background: #ddd;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    align-items: center;
    padding: 10px 0;
    box-sizing: border-box;
  }

  .footer-row {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }
  button.register {
    background: #d9001b;
    color: #fff;
    font-weight: bold;
    font-size: 16px;
    padding: 10px 30px;
    border: none;
    cursor: pointer;
  }

  /* ...ボタン */
  .more-btn {
    width: 22px;
    height: 22px;
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

  /* 右クリック風メニュー */
  .ctx-menu {
    position: fixed;
    background: #fff;
    border: 1px solid #999;
    box-shadow: 0 2px 6px rgba(0,0,0,0.25);
    min-width: 140px;
    padding: 4px 0;
    font-size: 13px;
    z-index: 1000;
    display: none;
  }
  .ctx-menu button {
    display: block;
    width: 100%;
    text-align: left;
    padding: 6px 12px;
    background: none;
    border: none;
    font-size: 13px;
    cursor: pointer;
    color: #222;
  }
  .ctx-menu button:hover {
    background: #1f5fa8;
    color: #fff;
  }

  /* 削除確認モーダル */
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

    // 「戻る」でキャッシュから復元された場合はダイアログを表示しない
    window.addEventListener('pageshow', function(event) {
        if (event.persisted) {
            const dialog = document.getElementById('okDialog');
            if (dialog.open) {
                dialog.close();
            }
        }
    });
</script>

<div class="container">

<div class="header-row">
	  <div class="title-box">
	    <button class="title-arrow"onclick="location.href='https://teams.microsoft.com/v2/';">
	      <svg viewBox="0 0 24 24"><polygon points="18,2 18,22 4,12" fill="#FFE600"/></svg>
	    </button>
	    <div class="title-text">学生情報一覧</div>
	  </div>

	  <div class="search-area">
	    <form action="<%= request.getContextPath() %>/StudentSearchServlet" method="get">
	      <div class="search-box">
	        <input type="text" name="keyword" placeholder="氏名で検索" value="<%= keyword != null ? keyword : "" %>">
	        <button type="submit" class="search-btn">🔍</button>
	      </div>
	    </form>
	  </div>
	</div>


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
			<td>辞退</td>
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

	<!-- 「変更」はformでPOST送信して遷移する（隠しinputに学籍番号をセットしてsubmit） -->
	<form id="editForm" action="StudentCenageSevlet" method="post">
	  <input type="hidden" name="gakusekiNo" id="editGakusekiNo" value="">
	</form>

	<!-- 「削除」もformでPOST送信する -->
	<form id="deleteForm" action="StudentDeleteServlet" method="post">
	  <input type="hidden" name="gakusekiNo" id="deleteGakusekiNo" value="">
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

	<br>
	<div class="footer-row">
	<form action ="StudentNewSevlet" method="get">
	<button class="register" type="submit">登録</button>
	</form>
	</div>

</div>

<script>
  const menu = document.getElementById('ctxMenu');
  const modalOverlay = document.getElementById('modalOverlay');
  const modalTargetName = document.getElementById('modalTargetName');
  const editForm = document.getElementById('editForm');
  const editGakusekiNo = document.getElementById('editGakusekiNo');
  const deleteForm = document.getElementById('deleteForm');
  const deleteGakusekiNo = document.getElementById('deleteGakusekiNo');
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
        const name = currentTr ? currentTr.querySelector('.name-cell').textContent : '';
        modalTargetName.textContent = '学籍番号 ' + currentRow + '（' + name + '）';
        modalOverlay.classList.add('open');
      } else if (action === 'edit') {
        // 変更 → 隠しformに学籍番号をセットしてPOST送信で編集画面へ
        editGakusekiNo.value = currentRow;
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
      deleteGakusekiNo.value = currentRow;
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