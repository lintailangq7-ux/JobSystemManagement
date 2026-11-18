<%-- ホーム / みんなの投稿 --%>
<%@ include file="/WEB-INF/jsp/parts/header.jspf" %>
<h1><c:out value="${title}" /></h1>
<%@ include file="/WEB-INF/jsp/parts/composer.jspf" %>
<c:forEach var="p" items="${posts}">
  <%@ include file="/WEB-INF/jsp/parts/postCard.jspf" %>
</c:forEach>
<c:if test="${empty posts}">
  <div class="card empty"><c:out value="${emptyText}" /></div>
</c:if>
<%@ include file="/WEB-INF/jsp/parts/footer.jspf" %>
