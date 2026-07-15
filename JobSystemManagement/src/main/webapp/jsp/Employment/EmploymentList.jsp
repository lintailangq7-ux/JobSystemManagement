<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"
         import="java.time.format.DateTimeFormatter,
                 DAO.StudentDetailDAO, model.StudentDetail,
                 model.GuidanceDetail, model.EmploymentChukan,
                 model.StudentChukan" %>
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
	String gakusekiNo = (String) session.getAttribute("userId");
    StudentDetailDAO dao = new StudentDetailDAO();
    StudentDetail detail = dao.findByGakusekiNo(gakusekiNo);
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
            <%for(StudentChukan Sc:  detail.getStudent().getStudentChukanList()){ %>
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
                                <th>業種</th>
                                <th>備考</th>
                                <th class="action-col"></th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td class="id-cell">ID01</td>
                                <td>KCSシステムズ</td>
                                <td>6/3　企業説明会</td>
                                <td>SE・PG</td>
                                <td></td>
                                <td class="action-col">
                                    <div class="dropdown-wrap">
                                        <button type="button" class="btn-dots" onclick="toggleMenu(this)">⋯</button>
                                        <div class="dropdown-menu-custom">
                                            <button type="button" class="btn-change" onclick="onChange('ID01')">変更</button>
                                            <button type="button" class="btn-delete" onclick="onDelete('ID01')">削除</button>
                                        </div>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td class="id-cell">ID02</td>
                                <td>西依工業株式会社</td>
                                <td>6/5　二次試験</td>
                                <td>製造</td>
                                <td></td>
                                <td class="action-col">
                                    <div class="dropdown-wrap">
                                        <button type="button" class="btn-dots" onclick="toggleMenu(this)">⋯</button>
                                        <div class="dropdown-menu-custom">
                                            <button type="button" class="btn-change" onclick="onChange('ID02')">変更</button>
                                            <button type="button" class="btn-delete" onclick="onDelete('ID02')">削除</button>
                                        </div>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td class="id-cell">ID03</td>
                                <td>中隈ネットワーク株式会社</td>
                                <td>6/10　四次面接</td>
                                <td>保守</td>
                                <td></td>
                                <td class="action-col">
                                    <div class="dropdown-wrap">
                                        <button type="button" class="btn-dots" onclick="toggleMenu(this)">⋯</button>
                                        <div class="dropdown-menu-custom">
                                            <button type="button" class="btn-change" onclick="onChange('ID03')">変更</button>
                                            <button type="button" class="btn-delete" onclick="onDelete('ID03')">削除</button>
                                        </div>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td class="id-cell">ID04</td>
                                <td>真鍋運輸</td>
                                <td class="red-text">〜6/9 書類提出期限</td>
                                <td>運輸</td>
                                <td></td>
                                <td class="action-col">
                                    <div class="dropdown-wrap">
                                        <button type="button" class="btn-dots" onclick="toggleMenu(this)">⋯</button>
                                        <div class="dropdown-menu-custom">
                                            <button type="button" class="btn-change" onclick="onChange('ID04')">変更</button>
                                            <button type="button" class="btn-delete" onclick="onDelete('ID04')">削除</button>
                                        </div>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td class="id-cell">ID05</td>
                                <td>三原情報システム</td>
                                <td>見送り</td>
                                <td>CG</td>
                                <td></td>
                                <td class="action-col">
                                    <div class="dropdown-wrap">
                                        <button type="button" class="btn-dots" onclick="toggleMenu(this)">⋯</button>
                                        <div class="dropdown-menu-custom">
                                            <button type="button" class="btn-change" onclick="onChange('ID05')">変更</button>
                                            <button type="button" class="btn-delete" onclick="onDelete('ID05')">削除</button>
                                        </div>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td class="id-cell">ID06</td>
                                <td>株式会社すえひろ</td>
                                <td>内定</td>
                                <td>その他</td>
                                <td></td>
                                <td class="action-col">
                                    <div class="dropdown-wrap">
                                        <button type="button" class="btn-dots" onclick="toggleMenu(this)">⋯</button>
                                        <div class="dropdown-menu-custom">
                                            <button type="button" class="btn-change" onclick="onChange('ID06')">変更</button>
                                            <button type="button" class="btn-delete" onclick="onDelete('ID06')">削除</button>
                                        </div>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td class="id-cell">ID07</td><td></td><td></td><td></td><td></td>
                                <td class="action-col">
                                    <div class="dropdown-wrap">
                                        <button type="button" class="btn-dots" onclick="toggleMenu(this)">⋯</button>
                                        <div class="dropdown-menu-custom">
                                            <button type="button" class="btn-change" onclick="onChange('ID07')">変更</button>
                                            <button type="button" class="btn-delete" onclick="onDelete('ID07')">削除</button>
                                        </div>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td class="id-cell">ID08</td><td></td><td></td><td></td><td></td>
                                <td class="action-col">
                                    <div class="dropdown-wrap">
                                        <button type="button" class="btn-dots" onclick="toggleMenu(this)">⋯</button>
                                        <div class="dropdown-menu-custom">
                                            <button type="button" class="btn-change" onclick="onChange('ID08')">変更</button>
                                            <button type="button" class="btn-delete" onclick="onDelete('ID08')">削除</button>
                                        </div>
                                    </div>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>

            <button class="btn-add">追加</button>
        </div>
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

        // 「変更」ボタン処理（必要に応じて画面遷移やAPI呼び出しに差し替えてください）
        function onChange(id) {
            closeAllMenus();
            console.log('変更: ' + id);
            // 例: window.location.href = 'edit?id=' + id;
        }

        // 「削除」ボタン処理（必要に応じて確認ダイアログやAPI呼び出しに差し替えてください）
        function onDelete(id) {
            closeAllMenus();
            if (confirm(id + ' を削除しますか？')) {
                console.log('削除: ' + id);
                // 例: window.location.href = 'delete?id=' + id;
            }
        }
    </script>
</body>
</html>
