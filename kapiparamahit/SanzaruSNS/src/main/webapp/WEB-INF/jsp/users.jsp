<%-- ユーザー一覧 --%>
<%@ include file="/WEB-INF/jsp/parts/header.jspf" %>
<h1>ユーザー</h1>
<c:forEach var="u" items="${users}">
  <div class="card row">
    <a href="${ctx}/user?name=${fn:escapeXml(u.username)}">
      <div class="avatar" style="background:hsl(${u.avatarHue} 55% 48%)"><c:out value="${u.initial}" /></div>
    </a>
    <div class="spacer" style="min-width:0">
      <div class="row" style="gap:6px;flex-wrap:wrap">
        <a href="${ctx}/user?name=${fn:escapeXml(u.username)}" style="font-weight:700;color:var(--text)"><c:out value="${u.displayName}" /></a>
        <span class="type-badge ${u.type.key}" title="${u.type.desc}">${u.type.emoji} ${u.type.label}</span>
      </div>
      <div class="muted">@<c:out value="${u.username}" /></div>
      <c:if test="${not empty u.bio}">
        <c:choose>
          <c:when test="${u.id == me.id or me.type.canSee('text')}"><div style="font-size:14px;overflow-wrap:anywhere"><c:out value="${u.bio}" /></div></c:when>
          <c:otherwise><div class="muted">🙊 自己紹介は読めません</div></c:otherwise>
        </c:choose>
      </c:if>
    </div>
    <c:if test="${u.id != me.id}">
      <form method="post" action="${ctx}/follow">
        <input type="hidden" name="csrf" value="${csrf}">
        <input type="hidden" name="name" value="${fn:escapeXml(u.username)}">
        <input type="hidden" name="back" value="${fn:escapeXml(back)}">
        <button type="submit" class="${u.followedByViewer ? 'secondary' : ''}">${u.followedByViewer ? 'フォロー中' : 'フォロー'}</button>
      </form>
    </c:if>
  </div>
</c:forEach>
<c:if test="${empty users}"><div class="card empty">ユーザーがいません</div></c:if>
<%@ include file="/WEB-INF/jsp/parts/footer.jspf" %>
