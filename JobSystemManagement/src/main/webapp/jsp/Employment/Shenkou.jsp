<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, model.EmploymentChukan" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>指導変更</title>
<style>
body { font-family: Meiryo; background: #fff; margin: 0; }
.container { width: 1200px; margin: 20px 0 20px 20px; }
.titleArea { display: flex; align-items: center; margin-bottom: 20px; }
.backButton {
    width: 48px; height: 48px; border: 2px solid #444;
    background: #58c8ff; cursor: pointer; position: relative; padding: 0; overflow: hidden;
}
.backButton::after {
    content: ""; position: absolute; left: 8px; top: 0;
    width: 30px; height: 100%; background: #fff34d;
    clip-path: polygon(100% 0, 0 50%, 100% 100%);
}
.title { font-size: 34px; font-weight: bold; margin-left: 20px; }
.main { display: flex; gap: 70px; align-items: flex-start; }
.left { width: 260px; }
.infoTable { width: 100%; border-collapse: collapse; }
.infoTable th { border: 2px solid black; background: #f3f3f3; height: 40px; }
.infoTable td { border: 2px solid black; text-align: center; height: 42px; }
.right { flex: 1; }
.formTable { border-collapse: collapse; }
.formTable td { padding: 5px 10px 5px 5px; vertical-align: top; }
label { font-weight: bold; }
input[type=text], input[type=datetime-local] {
    width: 280px; height: 36px; text-align: center;
    border: 2px solid black; font-size: 16px;
}
textarea { width: 430px; height: 120px; border: 2px solid black; font-size: 16px; }

/* 試験情報テーブル */
.exam-table {
    width: 100%;
    border-collapse: collapse;
    margin: 10px 0;
}
.exam-table th {
    background: #0d6fb8;
    color: #fff;
    border: 1px solid #000;
    padding: 8px;
    font-size: 14px;
}
.exam-table td {
    border: 1px solid #000;
    padding: 6px;
    text-align: center;
}
.exam-table input[type=text],
.exam-table input[type=datetime-local] {
    width: 140px;
    height: 32px;
    font-size: 14px;
}
.exam-table select {
    width: 80px;
    height: 32px;
    font-size: 14px;
}
.btn-add-exam {
    background: #28a745;
    color: #fff;
    border: none;
    padding: 8px 16px;
    border-radius: 6px;
    font-size: 14px;
    cursor: pointer;
    margin-bottom: 8px;
}
.btn-add-exam:hover { background: #218838; }
.btn-del-exam {
    background: #dc3545;
    color: #fff;
    border: none;
    width: 32px;
    height: 32px;
    border-radius: 4px;
    cursor: pointer;
    font-size: 16px;
}
.btn-del-exam:hover { background: #c82333; }

.buttonArea { text-align: right; margin-top: 20px; }
.submitButton {
    width: 110px; height: 60px; background: red; color: white;
    border: none; border-radius: 10px; font-size: 24px;
    font-weight: bold; cursor: pointer;
}
</style>
</head>
<body>
<div class="container">

<% if (request.getAttribute("errorMessage") != null) { %>
<div style="color:red; font-weight:bold; margin-bottom:10px;"><%= request.getAttribute("errorMessage") %></div>
<% } %>

<div class="titleArea">
    <button class="backButton" type="button" onclick="location.href='<%= request.getContextPath() %>/ListofEmployment'"></button>
    <div class="title">指導変更</div>
</div>

<div class="main">
    <!-- 左側 生徒情報 -->
    <div class="left">
        <table class="infoTable">
            <tr><th colspan="2">生徒情報</th></tr>
            <tr><td>名前</td><td>${student.name}</td></tr>
            <tr><td>クラス</td><td>${student.className}</td></tr>
            <tr><td>学籍番号</td><td>${student.gakusekiNo}</td></tr>
            <tr><td>内定状況</td><td>${employment.naiteiKakutei == 1 ? '内' : '未'}</td></tr>
        </table>
    </div>

    <!-- 右側 入力フォーム -->
    <div class="right">
        <form action="<%= "edit".equals(request.getAttribute("mode")) ? "EmploymentCangeServlet" : "EmploymentNewServlet" %>" method="post" id="mainForm">

            <input type="hidden" name="studentNo" value="${student.gakusekiNo}">
            <input type="hidden" name="shidoId" value="${shidoId}">

            <table class="formTable">
                <!-- 1. 企業ID -->
                <tr>
                    <td><label>企業ID</label></td>
                    <td><input type="text" name="companyId" value="${company.id}"></td>
                </tr>

                <!-- 2. 企業名 -->
                <tr>
                    <td><label>企業名</label></td>
                    <td><input type="text" name="companyName" value="${company.name}"></td>
                </tr>

                <!-- 3. 試験情報（複数） -->
                <tr>
                    <td><label>試験情報</label></td>
                    <td>
                        <button type="button" class="btn-add-exam" onclick="addExamRow()">＋ 試験を追加</button>

                        <table class="exam-table" id="examTable">
                            <thead>
                                <tr>
                                    <th>選考状況</th>
                                    <th>試験会場</th>
                                    <th>試験日時</th>
                                    <th>提出状況</th>
                                    <th>削除</th>
                                </tr>
                            </thead>
                            <tbody id="examBody">
                            <%
                                List<EmploymentChukan> examList = (List<EmploymentChukan>) request.getAttribute("examList");
                                if (examList != null && !examList.isEmpty()) {
                                    for (int i = 0; i < examList.size(); i++) {
                                        EmploymentChukan ex = examList.get(i);
                            %>
                                <tr>
                                    <td>
                                        <input type="hidden" name="examId" value="<%= ex.getShikenId() %>">
                                        <input type="text" name="examContent" value="<%= ex.getShikenNaiyo() != null ? ex.getShikenNaiyo() : "" %>">
                                    </td>
                                    <td>
                                        <input type="text" name="examPlace" value="<%= ex.getShikenKaijo() != null ? ex.getShikenKaijo() : "" %>">
                                    </td>
                                    <td>
                                        <input type="text" name="examDateTime" value="<%= ex.getShikenNichiji() != null ? ex.getShikenNichiji() : "" %>" placeholder="2025-09-20 10:00">
                                    </td>
                                    <td>
                                        <select name="examSubmit">
                                            <option value="1" <%= ex.getTeishutsuShoruiJokyo() == 1 ? "selected" : "" %>>済</option>
                                            <option value="0" <%= ex.getTeishutsuShoruiJokyo() != 1 ? "selected" : "" %>>未</option>
                                        </select>
                                    </td>
                                    <td>
                                        <button type="button" class="btn-del-exam" onclick="removeExamRow(this)">×</button>
                                    </td>
                                </tr>
                            <%
                                    }
                                } else {
                                    // 1行目の空行
                            %>
                                <tr>
                                    <td>
                                        <input type="hidden" name="examId" value="">
                                        <input type="text" name="examContent" value="">
                                    </td>
                                    <td><input type="text" name="examPlace" value=""></td>
                                    <td><input type="text" name="examDateTime" value="" placeholder="2025-09-20 10:00"></td>
                                    <td>
                                        <select name="examSubmit">
                                            <option value="1">済</option>
                                            <option value="0" selected>未</option>
                                        </select>
                                    </td>
                                    <td>
                                        <button type="button" class="btn-del-exam" onclick="removeExamRow(this)">×</button>
                                    </td>
                                </tr>
                            <%
                                }
                            %>
                            </tbody>
                        </table>
                    </td>
                </tr>

                <!-- 4. 内定確定 -->
                <tr>
                    <td><label>内定確定</label></td>
                    <td>
                        <select name="offerStatus" style="width:120px; height:36px; font-size:16px;">
                            <option value="1" ${employment.naiteiKakutei == 1 ? "selected" : ""}>内</option>
                            <option value="0" ${employment.naiteiKakutei != 1 ? "selected" : ""}>未</option>
                        </select>
                    </td>
                </tr>

                <!-- 5. 内定承諾日 -->
                <tr>
                    <td><label>内定承諾日</label></td>
                    <td>
                        <input type="text" name="acceptDate" value="${employment.naiteiKakuteiBi}" placeholder="2025-10-15">
                    </td>
                </tr>

                <!-- 6. 備考 -->
                <tr>
                    <td><label>備考(100文字)</label></td>
                    <td>
                        <textarea name="memo">${employment.biko}</textarea>
                    </td>
                </tr>
            </table>

            <div class="buttonArea">
                <button type="submit" class="submitButton">
                    <%= "edit".equals(request.getAttribute("mode")) ? "変更" : "追加" %>
                </button>
            </div>
        </form>
    </div>
</div>
</div>

<script>
// 試験行を追加
function addExamRow() {
    const tbody = document.getElementById('examBody');
    const tr = document.createElement('tr');
    tr.innerHTML = `
        <td>
            <input type="hidden" name="examId" value="">
            <input type="text" name="examContent" value="">
        </td>
        <td><input type="text" name="examPlace" value=""></td>
        <td><input type="text" name="examDateTime" value="" placeholder="2025-09-20 10:00"></td>
        <td>
            <select name="examSubmit">
                <option value="1">済</option>
                <option value="0" selected>未</option>
            </select>
        </td>
        <td>
            <button type="button" class="btn-del-exam" onclick="removeExamRow(this)">×</button>
        </td>
    `;
    tbody.appendChild(tr);
}

// 試験行を削除
function removeExamRow(btn) {
    const tbody = document.getElementById('examBody');
    if (tbody.rows.length <= 1) {
        alert('最低1行は必要です');
        return;
    }
    btn.closest('tr').remove();
}
</script>
</body>
</html>