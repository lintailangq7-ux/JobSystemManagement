<%-- ログイン / 新規登録 (mode で切り替える) --%>
<%@ include file="/WEB-INF/jsp/parts/header.jspf" %>
<c:set var="reg" value="${mode == 'register'}" />
<div class="card intro">
  <h1 style="margin:0 0 4px">三猿SNS</h1>
  <p style="margin:0">このSNSのアカウントは3種類。投稿は誰でも自由にできますが、見られないものとコメントで使えないものがあります。</p>
  <ul>
    <li><b>🙈 見猿</b>：画像が見えず、コメントに画像を貼れません</li>
    <li><b>🙊 言わ猿</b>：文字が見えず、コメントに文字を打てません</li>
    <li><b>🙉 聞か猿</b>：音が聞こえず、コメントに音声を投稿できません</li>
  </ul>
</div>

<form class="card" method="post" action="${ctx}/login">
  <input type="hidden" name="csrf" value="${csrf}">
  <input type="hidden" name="mode" value="${reg ? 'register' : 'login'}">
  <h1>${reg ? '新規登録' : 'ログイン'}</h1>
  <c:if test="${reg}">
    <p class="muted">登録すると 🙈見猿・🙊言わ猿・🙉聞か猿 のどれかがランダムで割り振られます。</p>
  </c:if>

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
    <button type="submit">${reg ? '登録する' : 'ログイン'}</button>
    <div class="spacer"></div>
    <c:choose>
      <c:when test="${reg}"><a href="${ctx}/login">アカウントをお持ちの方はログイン</a></c:when>
      <c:otherwise><a href="${ctx}/login?mode=register">アカウントを作成する</a></c:otherwise>
    </c:choose>
  </div>
</form>
<%@ include file="/WEB-INF/jsp/parts/footer.jspf" %>
