<%-- プロフィール --%>
<%@ include file="/WEB-INF/jsp/parts/header.jspf" %>
<div class="card">
  <div class="profile-head">
    <div class="avatar lg" style="background:hsl(${user.avatarHue} 55% 48%)"><c:out value="${user.initial}" /></div>
    <div class="spacer" style="min-width:0">
      <h1 style="margin:0"><c:out value="${user.displayName}" /></h1>
      <div class="muted">@<c:out value="${user.username}" /> · ${user.createdDate}に登録</div>
      <div style="margin-top:6px">
        <span class="type-badge ${user.type.key}" title="${user.type.desc}">${user.type.emoji} ${user.type.label}</span>
        <span class="muted">${user.type.desc}</span>
      </div>
      <c:if test="${not empty user.bio}">
        <c:choose>
          <c:when test="${user.id == me.id or me.type.canSee('text')}"><div style="margin:8px 0 0;white-space:pre-wrap;overflow-wrap:anywhere"><c:out value="${user.bio}" /></div></c:when>
          <c:otherwise><div class="muted" style="margin-top:8px">🙊 自己紹介は読めません</div></c:otherwise>
        </c:choose>
      </c:if>
      <div class="profile-stats">
        <span><b>${user.postCount}</b> 投稿</span>
        <span><b>${user.followingCount}</b> フォロー</span>
        <span><b>${user.followerCount}</b> フォロワー</span>
      </div>
    </div>
    <c:choose>
      <c:when test="${user.id != me.id}">
        <form method="post" action="${ctx}/follow">
          <input type="hidden" name="csrf" value="${csrf}">
          <input type="hidden" name="name" value="${fn:escapeXml(user.username)}">
          <input type="hidden" name="back" value="${fn:escapeXml(back)}">
          <button type="submit" class="${user.followedByViewer ? 'secondary' : ''}">${user.followedByViewer ? 'フォロー中' : 'フォロー'}</button>
        </form>
      </c:when>
      <c:when test="${not editing}">
        <a class="button secondary" href="${ctx}/user?name=${fn:escapeXml(user.username)}&amp;edit=1">プロフィール編集</a>
      </c:when>
    </c:choose>
  </div>

  <c:if test="${editing}">
    <form method="post" action="${ctx}/user" style="margin-top:12px;border-top:1px solid var(--border);padding-top:4px">
      <input type="hidden" name="csrf" value="${csrf}">
      <label for="displayName">表示名</label>
      <input id="displayName" name="displayName" value="${fn:escapeXml(user.displayName)}" maxlength="50">
      <label for="bio">自己紹介（160文字まで）</label>
      <textarea id="bio" name="bio" maxlength="160"><c:out value="${user.bio}" /></textarea>
      <div class="row" style="margin-top:8px">
        <button type="submit">保存</button>
        <a class="button secondary" href="${ctx}/user?name=${fn:escapeXml(user.username)}">キャンセル</a>
      </div>
    </form>
  </c:if>
</div>
<c:forEach var="p" items="${posts}">
  <%@ include file="/WEB-INF/jsp/parts/postCard.jspf" %>
</c:forEach>
<c:if test="${empty posts}"><div class="card empty">まだ投稿がありません。</div></c:if>
<%@ include file="/WEB-INF/jsp/parts/footer.jspf" %>
