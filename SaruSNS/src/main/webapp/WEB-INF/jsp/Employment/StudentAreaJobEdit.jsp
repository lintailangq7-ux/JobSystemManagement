<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="model.ModelStudent, model.StudentChukan, java.util.List" %>
<%
    ModelStudent student = (ModelStudent) request.getAttribute("student");
    String errorMessage = (String) request.getAttribute("errorMessage");

    List<StudentChukan> chukanList = (student != null) ? student.getGakuseiChukanList() : null;
    String job1 = (chukanList != null && chukanList.size() > 0) ? chukanList.get(0).getKibouShokushu() : "";
    String job2 = (chukanList != null && chukanList.size() > 1) ? chukanList.get(1).getKibouShokushu() : "";
    String job3 = (chukanList != null && chukanList.size() > 2) ? chukanList.get(2).getKibouShokushu() : "";

    String currentArea = (student != null && student.getKenNaiGaiKibo() != null)
            ? student.getKenNaiGaiKibo() : "";
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>学生情報変更（希望地域・希望職種）</title>

<style>
body {
    font-family: Meiryo;
    background: #ffffff;
}

.container {
    width: 700px;
    margin: 20px auto;
}

.titleArea {
    display: flex;
    align-items: center;
    margin-bottom: 30px;
}

.backButton {
    width: 48px;
    height: 48px;
    border: 2px solid #444;
    background: #58c8ff;
    cursor: pointer;
    position: relative;
    padding: 0;
    overflow: hidden;
}

.backButton::after {
    content: "";
    position: absolute;
    left: 8px;
    top: 0;
    width: 30px;
    height: 100%;
    background: #fff34d;
    clip-path: polygon(100% 0, 0 50%, 100% 100%);
}

.backButton:hover {
    filter: brightness(0.95);
}

.title {
    font-size: 30px;
    font-weight: bold;
    margin-left: 20px;
}

table {
    width: 100%;
    border-spacing: 20px 15px;
}

label {
    font-weight: bold;
}

.must {
    color: red;
}

input[type=text] {
    width: 280px;
    height: 40px;
    text-align: center;
    font-size: 20px;
    border: 2px solid black;
}

select {
    width: 280px;
    height: 45px;
    text-align: center;
    font-size: 20px;
    border: 2px solid black;
}

.buttonArea {
    text-align: right;
    margin-top: 20px;
}

.submitButton {
    width: 120px;
    height: 55px;
    background: red;
    color: white;
    border: none;
    border-radius: 10px;
    font-size: 24px;
    font-weight: bold;
    cursor: pointer;
}

.submitButton:hover {
    background: #cc0000;
}

.note {
    color: #666;
    font-size: 13px;
    margin-bottom: 15px;
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
    <p><%= (errorMessage != null) ? errorMessage : "" %></p>
    <button onclick="document.getElementById('okDialog').close()">OK</button>
</dialog>

<script>
    window.onload = function() {
        <% if (errorMessage != null && !errorMessage.isEmpty()) { %>
            document.getElementById('okDialog').showModal();
        <% } %>
    };
</script>

<div class="container">

<div class="titleArea">
    <button type="button"
            class="backButton"
            onclick="location.href='<%= request.getContextPath() %>/ListofEmployment'"
            title="戻る">
    </button>
    <div class="title">学生情報変更（希望地域・希望職種）</div>
</div>

<p class="note">この画面では「希望地域」「希望職種」のみ変更できます。氏名やクラスなどその他の情報を変更したい場合は、先生に連絡してください。</p>

<form action="<%= request.getContextPath() %>/StudentAreaJobUpdateServlet" method="post">

<table>

<tr>
    <td>
        <label><span class="must">*</span> 希望地域</label><br>
        <select name="area">
            <option value="県内" <%= "県内".equals(currentArea) ? "selected" : "" %>>県内</option>
            <option value="県外" <%= "県外".equals(currentArea) ? "selected" : "" %>>県外</option>
            <option value="その他" <%= "その他".equals(currentArea) ? "selected" : "" %>>その他</option>
        </select>
    </td>
</tr>

<tr>
    <td>
        <label>希望職種1</label><br>
        <input type="text" name="job1" value="<%= job1 %>">
    </td>
</tr>

<tr>
    <td>
        <label>希望職種2</label><br>
        <input type="text" name="job2" value="<%= job2 %>">
    </td>
</tr>

<tr>
    <td>
        <label>希望職種3</label><br>
        <input type="text" name="job3" value="<%= job3 %>">
    </td>
</tr>

</table>

<div class="buttonArea">
    <button class="submitButton">変更</button>
</div>

</form>

</div>

</body>
</html>
