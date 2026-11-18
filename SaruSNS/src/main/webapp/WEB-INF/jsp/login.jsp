<%-- ログイン / 新規登録 (mode で切り替える) --%>
<%@ include file="/WEB-INF/jsp/parts/header.jspf" %>
<c:set var="reg" value="${mode == 'register'}" />


<form class="card" method="post" action="${ctx}/login">

  <h1>ログイン</h1>


  <label for="username">ユーザー名（英数字と_、3〜20文字）</label>
  <input id="username" name="username" value="${fn:escapeXml(username)}" autocomplete="username" placeholder="yamada_taro" autofocus>
  <c:if test="${reg}">
    <label for="displayName">表示名（任意）</label>
    <input id="displayName" name="displayName" value="${fn:escapeXml(displayName)}" placeholder="山田太郎">
  </c:if>
  <label for="password">パスワード（8文字以上）</label>
  <input id="password" name="password" type="password" autocomplete="${reg ? 'new-password' : 'current-password'}">

  <p class="error"><c:out value="${error}" /></p>
  <div class="row" style="margin-top:8px">
    <button type="submit">ログイン</button>
    <div class="spacer"></div>
 
  </div>
</form>
<%@ include file="/WEB-INF/jsp/parts/footer.jspf" %>
