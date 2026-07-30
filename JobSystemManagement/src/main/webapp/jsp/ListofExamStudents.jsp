<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="model.StudentList" %>

<%
    String companyName = (String) request.getAttribute("companyName");
    List<StudentList> studentList = (List<StudentList>) request.getAttribute("studentList");
%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>受験学生一覧</title>
<style>
* {
    box-sizing: border-box;
}
body {
    margin: 0;
    font-family: "MS PGothic", "Meiryo", sans-serif;
    background: #fff;
}

/* ==============================
   ヘッダー
============================== */
.top-area {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px 20px;
    border-bottom: 2px solid #ccc;
}
.back-button {
    width: 40px;
    height: 40px;
    border: 1px solid #4aa3df;
    background: #4aa3df;
    color: #fff35c;
    font-size: 22px;
    cursor: pointer;
    padding: 0;
    flex-shrink: 0;
}
.back-button:hover {
    filter: brightness(0.95);
}
.page-title {
    font-size: 26px;
    font-weight: bold;
    margin: 0;
}
.company-name {
    margin-left: 20px;
    padding: 6px 20px;
    border: 2px solid #0d6fb8;
    border-radius: 20px;
    color: #0d6fb8;
    font-size: 18px;
    font-weight: bold;
    white-space: nowrap;
}

/* ==============================
   テーブル
============================== */
.table-area {
    padding: 20px;
}
.table-wrapper {
    border: 1px solid #000;
    overflow: auto;
    max-height: calc(100vh - 120px);
}
table {
    width: 100%;
    border-collapse: collapse;
    table-layout: fixed;
}
th {
    background: #0d6fb8;
    color: #fff;
    padding: 10px;
    border: 1px solid #000;
    font-size: 15px;
    position: sticky;
    top: 0;
    z-index: 1;
}
td {
    border: 1px solid #000;
    padding: 10px;
    font-size: 14px;
    text-align: center;
}
th.class-no, td.class-no { width: 25%; }
th.number, td.number     { width: 25%; }
th.name, td.name         { width: 50%; }

/* データなし */
.empty-msg {
    text-align: center;
    padding: 40px;
    color: #666;
    font-size: 16px;
}
</style>
</head>
<body>

<div class="top-area">
    <button type="button" class="back-button" onclick="history.back()" title="戻る">◀</button>
    <h1 class="page-title">受験学生一覧</h1>
    <div class="company-name"><%= companyName != null ? companyName : "-" %></div>
</div>

<div class="table-area">
    <div class="table-wrapper">
        <table>
            <thead>
                <tr>
                    <th class="class-no">クラス</th>
                    <th class="number">出席番号</th>
                    <th class="name">氏名</th>
                </tr>
            </thead>
            <tbody>
            <%
                if (studentList != null && !studentList.isEmpty()) {
                    for (StudentList s : studentList) {
            %>
                <tr>
                    <td class="class-no"><%= s.getClassNo() %></td>
                    <td class="number"><%= s.getAttendanceNo() %></td>
                    <td class="name"><%= s.getStudentName() %></td>
                </tr>
            <%
                    }
                } else {
            %>
                <tr>
                    <td colspan="3" class="empty-msg">該当する学生がいません</td>
                </tr>
            <%
                }
            %>
            </tbody>
        </table>
    </div>
</div>

</body>
</html>