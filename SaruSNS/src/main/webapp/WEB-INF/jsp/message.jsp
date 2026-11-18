<%-- 「見つかりません」などのメッセージだけの画面 --%>
<%@ include file="/WEB-INF/jsp/parts/header.jspf" %>
<div class="card empty">
  <c:out value="${message}" />
  <c:if test="${not empty hint}"><p class="muted"><c:out value="${hint}" /></p></c:if>
</div>
<%@ include file="/WEB-INF/jsp/parts/footer.jspf" %>
