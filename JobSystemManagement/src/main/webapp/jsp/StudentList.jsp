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
* {
    box-sizing: border-box;
}
body {
    margin: 0;
    font-family: "MS PGothic", "Meiryo", sans-serif;
}

/* ==============================
   全体レイアウト（左サイドバー＋メイン）
============================== */
.page-layout {
    display: flex;
    height: 100vh;
}

/* ==============================
   左サイドバー
============================== */
.sidebar {
    width: 220px;
    flex-shrink: 0;
    border-right: 2px solid #ccc;
    padding: 20px 16px;
    display: flex;
    flex-direction: column;
    gap: 20px;
}
.sidebar-header {
    display: flex;
    align-items: center;
    gap: 8px;
}
.back-button {
    width: 36px;
    height: 36px;
    border: 1px solid #4aa3df;
    background: #4aa3df;
    color: #fff35c;
    font-size: 20px;
    cursor: pointer;
    padding: 0;
    flex-shrink: 0;
}
.page-title-box {
    border: 2px solid #000;
    padding: 8px 10px;
    font-size: 16px;
    font-weight: bold;
    flex: 1;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

/* ==============================
   メインエリア
============================== */
.main-content {
    flex: 1;
    padding: 20px;
    overflow: hidden;
    display: flex;
    flex-direction: column;
}
.search-area {
    display: flex;
    justify-content: flex-end;
    padding: 0 0 10px 0;
}
.search-box {
    position: relative;
    display: flex;
    justify-content: flex-end;
}
.search-box input {
    width: 250px;
    height: 30px;
    border: 3px solid #5b9bd5;
    border-radius: 20px;
    font-size: 16px;
    outline: none;
    padding-left: 10px;
}
.search-btn {
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

/* テーブルを画面内でスクロールさせるための枠 */
.table-wrapper {
    flex: 1;
    overflow-y: auto;
    border: 1px solid #000;
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
    position: sticky;
    top: 0;
    z-index: 1;
    font-size: 13px;
}
td {
    border: 1px solid #000;
    padding: 8px;
    font-size: 14px;
    text-align: center;
}
td.rowhead {
    background: #29ABE2;
    color: #003366;
    font-weight: bold;
}
td:last-child {
    text-align: center;
}

/* 「...」ボタン */
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

/* 右クリック風メニュー */
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

/* 追加（登録）ボタン */
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
    font-size: 18px;
    font-weight: bold;
    cursor: pointer;
}
.add-button:hover {
    background: #d40000;
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

<div class="page-layout">

    <!-- ============ 左サイドバー ============ -->
    <aside class="sidebar">
        <div class="sidebar-header">
            <button class="back-button" title="ひとつ前の画面に遷移" onclick="location.href='<%= request.getContextPath() %>/ListofEmployment'">◀</button>
            <div class="page-title-box">学生情報一覧</div>
        </div>
    </aside>

    <!-- ============ メインエリア ============ -->
    <main class="main-content">

        <div class="search-area">
            <form action="<%= request.getContextPath() %>/StudentSearchServlet" method="get">
                <div class="search-box">
                    <input type="text" name="keyword" placeholder="氏名で検索" value="<%= keyword != null ? keyword : "" %>">
                    <button type="submit" class="search-btn">🔍</button>
                </div>
            </form>
        </div>

        <div class="table-wrapper">
        <table>
            <thead>
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
            </thead>
            <tbody>
				<% for(ModelStudent SD : StuList){ %>
				<tr data-id="<%= SD.getGakusekiNo() %>">
				    <td class="rowhead"><%= SD.getGakusekiNo() %></td>
				    <td><%= SD.getClassName() %></td>
				    <td><%= SD.getAttendanceNo() %></td>
				    <td class="name-cell"><%= SD.getName() %></td>

				    <!-- 性別（必ず1つのtdを出力） -->
				    <td>
				    <%
				        String Sei = SD.getSeibetsu();
				        if ("F".equals(Sei)) {
				            out.print("女");
				        } else if ("M".equals(Sei)) {
				            out.print("男");
				        } else if ("X".equals(Sei)) {
				            out.print("未");
				        } else {
				            out.print("-");
				        }
				    %>
				    </td>

				    <!-- あっせん状況（必ず1つのtdを出力） -->
				    <td>
				    <%
				        int assen = SD.getAssen();
				        if (assen == 1) {
				            out.print("あっせん中");
				        } else if (assen == 2) {
				            out.print("辞退");
				        } else {
				            out.print("-");   // 0 やその他
				        }
				    %>
				    </td>

				    <!-- 在籍状況（必ず1つのtdを出力） -->
				    <td>
				    <%
				        int zai = SD.getZaisekiJokyo();
				        if (zai == 1) {
				            out.print("在学");
				        } else if (zai == 2) {
				            out.print("卒業");
				        } else if (zai == 3) {
				            out.print("退学");
				        } else if (zai == 4) {
				            out.print("留年");
				        } else {
				            out.print("-");   // 0 やその他（休学など）
				        }
				    %>
				    </td>

				    <td><%= SD.getKenNaiGaiKibo() != null ? SD.getKenNaiGaiKibo() : "-" %></td>

				    <!-- 希望業種1〜3 -->
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
				    <%
				            }
				        }
				    %>

				    <td><%= (SD.getBiko() != null) ? SD.getBiko() : "-" %></td>
				    <td><button class="more-btn" data-row="<%= SD.getGakusekiNo() %>">&hellip;</button></td>
				</tr>
				<% } %>
            </tbody>
        </table>
        </div>

    </main>
</div>

<button class="add-button" title="学生登録" onclick="location.href='StudentNewSevlet'">登録</button>

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
y