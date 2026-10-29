<%-- エラー画面 (web.xml の error-page) --%>
<%@ page isErrorPage="true" %>
<%@ include file="/WEB-INF/jsp/parts/header.jspf" %>
<c:set var="status" value="${requestScope['jakarta.servlet.error.status_code']}" />
<div class="card empty">
  <c:choose>
    <c:when test="${status == 404}">ページが見つかりません</c:when>
    <c:when test="${status == 403}"><c:out value="${requestScope['jakarta.servlet.error.message']}" /></c:when>
    <c:otherwise>サーバーエラーが発生しました (${status})</c:otherwise>
  </c:choose>
  <p><a href="${ctx}/">ホームへ戻る</a></p>
</div>
<%@ include file="/WEB-INF/jsp/parts/footer.jspf" %>
