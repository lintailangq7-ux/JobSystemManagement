<%-- 投稿の詳細とコメント --%>
<%@ include file="/WEB-INF/jsp/parts/header.jspf" %>
<div class="row" style="margin-bottom:12px">
  <a class="button secondary" href="javascript:history.back()">← 戻る</a>
  <h1 style="margin:0">投稿</h1>
</div>
<c:set var="p" value="${post}" />
<%@ include file="/WEB-INF/jsp/parts/postCard.jspf" %>
<c:set var="replyPost" value="${post}" />
<%@ include file="/WEB-INF/jsp/parts/composer.jspf" %>
<h1 style="font-size:16px">コメント ${fn:length(replies)}件</h1>
<c:forEach var="p" items="${replies}">
  <%@ include file="/WEB-INF/jsp/parts/postCard.jspf" %>
</c:forEach>
<c:if test="${empty replies}"><div class="card empty">まだコメントはありません。</div></c:if>
<%@ include file="/WEB-INF/jsp/parts/footer.jspf" %>
