<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"
         import="java.time.format.DateTimeFormatter,
                 DAO.StudentDetailDAO, model.StudentDetail,
                 model.GuidanceDetail, model.ModelStudent,
                 model.EmploymentChukan,
                 model.StudentChukan, model.CompanyChukan" %>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>指導一覧</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="../../css/Employment.css"> <!-- CSSファイルの読み込み -->
    <style>
        /* 必要最小限のJSP固有スタイル */
        body { background-color: #f0f0f0; padding: 20px; }
    </style>
</head>
<body> 
<%
	StudentDetail detail = (StudentDetail) session.getAttribute("detail");
    DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("M/d");
%>


</body>
    <div class="main-container">
        <!-- 左側：生徒情報 -->
        <div class="student-info">
            <div class="title-box">指導一覧</div>
            
            <table class="student-table">
                <tr>
                    <td class="header">氏名</td>
                    <td><%= detail.getStudent().getName() %></td>
                </tr>
                <tr>
                    <td class="header">クラス</td>
                    <td><%= detail.getStudent().getClassName()%></td>
                </tr>
                <tr>
                    <td class="header">番号</td>
                    <td class="number"><%= detail.getStudent().getAttendanceNo()%></td>
                </tr>
                <tr>
                    <td class="header">性別</td>
                    <td><%= detail.getStudent().getSeibetsu()%></td>
                </tr>
            </table>

            <br>
			
            <table class="student-table">
            <%for(StudentChukan Sc:  detail.getStudent().getGakuseiChukanList()){ %>
                <tr>
                    <td class="header">志望業種</td>
                    <td><%= Sc.getKibouShokushu() %></td>
                </tr>
             <%} %>
                <tr>
                    <td class="header">志望地域</td>
                    <td><%= detail.getStudent().getKenNaiGaiKibo()%></td>
                </tr>
                <tr>
                    <td class="header">内定状況</td>
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

            <div class="remarks-box">備考</div>
        </div>

        <!-- 右側：指導一覧 -->
        
        <div class="guidance-area">
            <div class="table-container">
                <div class="table-wrapper">
                    <table class="guidance-table">
                        <thead>
                            <tr>
                                <th>指導ID</th>
                                <th>企業名</th>
                                <th>選考状況</th>
                                <th>試験日時</th>
                                <th>業種</th>
                                <th>備考</th>
                                <th class="action-col"></th>
                            </tr>
                        </thead>
                        <tbody>
                        	<%for(GuidanceDetail Gu:  detail.getGuidanceList()){ %>
                            <tr>
                                <td class="id-cell"><%= Gu.getShidoId() %></td>
                                <td><%= Gu.getCompany().getKaishaName() %></td>
                                <td><%= Gu.getLatestExam().getShikenNaiyo() %></td>
                                <td><%= Gu.getLatestExam().getShikenNichiji() %></td>
                                
                                <td>
								<%
								for(CompanyChukan Mc : Gu.getCompany().getCompanyChukanList()){
								%>
   									 <%= Mc.getBoshuShokushu() %>・<br>
								<%
								}
								%>
								</td>
                                
                                <td><%= Gu.getBiko() %></td>
                                <td class="action-col">
                                    <div class="dropdown-wrap">
                                        <button type="button" class="btn-dots" onclick="toggleMenu(this)">⋯</button>
                                        <div class="dropdown-menu-custom">
                                            <button type="button" class="btn-change" 
        											onclick="onChange('<%= Gu.getShidoId() %>')">変更</button>
                                            <button type="button" class="btn-delete" onclick="onDelete('ID01')">削除</button>
              							</div>
              						</div>
              					</td>
              				</tr>
              				<%} %>
                        </tbody>
                    </table>
            	</div>S


	</div>
	   	<div class="button-group">
       		<button class="btn-add" onclick="location.href='addGuidance.jsp'">追加</button>
        	<button class="btn-add" onclick="location.href='../../ListofCompanies'">企業一覧</button>
        	<button class="btn-add" onclick="location.href='activityReport.jsp'">活動報告書</button>
    	</div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // 開いているメニューを閉じる
        function closeAllMenus(except) {
            document.querySelectorAll('.dropdown-menu-custom.open').forEach(function (menu) {
                if (menu !== except) {
                    menu.classList.remove('open');
                }
            });
        }

        // ⋯ボタン押下でメニューの開閉を切り替える
        function toggleMenu(btn) {
            var menu = btn.nextElementSibling;
            var isOpen = menu.classList.contains('open');
            closeAllMenus(menu);
            menu.classList.toggle('open', !isOpen);
        }

        // メニュー外クリックで閉じる
        document.addEventListener('click', function (e) {
            if (!e.target.closest('.dropdown-wrap')) {
                closeAllMenus();
            }
        });

     // 「変更」ボタン処理
        function onChange(shidoId) {
            closeAllMenus();
            // またはサーブレットを使う場合：
             window.location.href = 'GuidanceCenageSevlet?action=edit&shidoId=' + shidoId;
        }

        // 「削除」ボタン処理（参考）
        function onDelete(shidoId) {
            closeAllMenus();
            if (confirm(shidoId + ' を削除しますか？')) {
                window.location.href = 'GuidanceServlet?action=delete&shidoId=' + shidoId;
                // または POST で削除したい場合は form を用意して submit
            }
        }
    </script>
</body>
</html>
