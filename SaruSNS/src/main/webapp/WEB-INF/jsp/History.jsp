<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" import="java.util.*"%>
<%
    // 投稿の表示データ(仮)。件数を増やすとスクロールで流れます
    String[] accountNames = { "アカウント名", "アカウント名", "アカウント名", "アカウント名", "アカウント名","アカウント名" };
    int[] accountRule     = { 1, 2, 3, 1, 3 ,0};
    String[] accountIds   = { "アカウントID", "アカウントID", "アカウントID", "アカウントID", "アカウントID","アカウントID" };
    boolean[] accountFollow = { true, false, true, false, true,true };  // フォロー中のアカウントか(仮)

    // 各投稿の投稿日時(accountNames と同じ順番・同じ件数にする)
    String[] postDates = {
        "2026-10-27 09:00", "2026-10-29 08:30", "2026-10-25 18:10",
        "2026-10-29 07:45", "2026-10-28 12:00", "2026-10-26 21:15"
    };

    // 0,1,2,... の番号を、日付の新しい順に並べ替える(order[0] が一番新しい投稿の番号)
    Integer[] order = new Integer[postDates.length];
    for (int k = 0; k < order.length; k++) order[k] = k;

    final String[] dates = postDates;   // 比較用(匿名クラスから使うため final にする)
    Arrays.sort(order, new Comparator<Integer>() {
        public int compare(Integer a, Integer b) {
            return dates[b].compareTo(dates[a]);   // a と b を逆にすると古い順
        }
    });

    String good ="いいね";
    String LookHistiry = "閲覧";
    String GoodHistory = "いいね";
    String BookMark= "ブックマーク";
    
    String ruleNotString ="ここには文がありますが、あなたは読めません";
    String ruleNotImage ="ここには画像がありますが、あなたは見えません";
    String ruleNotAudio ="ここには音声がありますが、あなたは聞こえません";
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>履歴</title>
<style>
    :root {
        --toolbar-width: 180px;
        --line: #000;
        --text: #000;
        --bg: #fff;
        --accent: #1a9ad6;   /* アカウントIDの色・タブ下線の色 */
        --gap: 16px;
        --search-height: 64px;   /* 戻るボタンの行の高さ(タブをこの下に張り付かせる) */
    }

    * { box-sizing: border-box; }
    html, body { margin: 0; height: 100%; }
    body {
        font-family: sans-serif;
        color: var(--text);
        background: var(--bg);
    }

    .toolbar {
        position: fixed;
        top: 8px; bottom: 8px;
        left: calc((100% - var(--total-width)) / 2);   /* ツールバー+投稿の列をまとめて中央へ */
        width: var(--toolbar-width);
        border: 1px solid var(--line);
        border-radius: 24px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: var(--bg);
        z-index: 40;
    }
    .toolbar span {
        writing-mode: vertical-rl;
        font-size: 28px;
        letter-spacing: 0.4em;
    }

    /* ツールバー+投稿の列を、ひとまとまりで画面の中央に配置 */
    :root {
        --column-width: 600px;      /* 投稿の列の幅 */
        --column-gap: 16px;         /* ツールバーと列のすき間 */
        --total-width: calc(var(--toolbar-width) + var(--column-gap) + var(--column-width));
    }
    .main {
        width: var(--column-width);
        margin-left: calc((100% - var(--total-width)) / 2 + var(--toolbar-width) + var(--column-gap));
        border-left: 1px solid #ddd;
        border-right: 1px solid #ddd;
        min-height: 100%;
    }
    /* 画面が狭いときは左に寄せる */
    @media (max-width: 820px) {
        .toolbar { left: 8px; }
        .main {
            width: auto;
            margin: 0 var(--gap) 0 calc(var(--toolbar-width) + 24px);
        }
    }

    /* ---------- 戻るボタンの行：スクロールしても上に張り付く ---------- */
    .search-row {
        position: sticky;
        top: 0;
        z-index: 20;
        display: flex;
        align-items: center;
        gap: var(--gap);
        height: var(--search-height);
        padding: 12px 8px;
        background: var(--bg);
    }
    .back-btn {
        background: none; border: none; padding: 0; cursor: pointer;
        width: 56px; height: 40px; flex: none;
    }

    /* ---------- タブ(閲覧 / いいね / ブックマーク):戻るボタンの下に張り付く ---------- */
    .tabs {
        position: sticky;
        top: var(--search-height);
        z-index: 10;
        display: flex;
        width: 100%;
        border-bottom: 1px solid #ccc;
        background: var(--bg);
    }
    .tab {
        flex: 1;
        position: relative;
        border: none;
        background: #eee;
        padding: 14px 0 18px;
        font-size: 14px;
        cursor: pointer;
    }
    .tab.active { background: var(--bg); }
    .tab .bar {
        display: none;
        position: absolute;
        left: 50%; bottom: 6px;
        width: 56px; height: 5px;
        border-radius: 3px;
        background: var(--accent);
        transform: translateX(-50%);
    }
    .tab.active .bar { display: block; }

    .panel { display: none; }
    .panel.active { display: block; }

    /* ---------- 投稿1件 ---------- */
    .post {
        display: flex;
        gap: 12px;
        width: 100%;
        padding: 12px;
        border-bottom: 1px solid var(--line);
    }
    .avatar {
        flex: none;
        width: 44px; height: 44px;
        border-radius: 50%;
        background: #f00;
        border: 1px solid var(--line);
    }
    .post-body { flex: 1; min-width: 0; }

    .post-head { display: flex; gap: 16px; font-size: 13px; margin-bottom: 4px; }
    .post-head .id { color: var(--accent); }

    .post-image {
        width: 100%;
        aspect-ratio: 21 / 10;   /* 幅が広がっても元(420px時)の縦横比を保つ */
        border: 2px solid #123;
        border-radius: 28px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 28px;
        overflow: hidden;
    }
    .post-image img { width: 100%; height: 100%; object-fit: cover; }

    .post-audio {
        margin-top: 12px;
        border: 2px solid #123;
        border-radius: 24px;
        padding: 4px 8px;
    }
    .post-audio audio { width: 100%; height: 32px; display: block; }

    .post-actions { display: flex; gap: 20px; margin-top: 14px; }
    .post-actions button {
        background: none; border: none; padding: 0; cursor: pointer;
    }
    /* 削除ボタン(赤) */
    .post-actions .delete-btn svg { stroke: #d00; }
    .post-actions .delete-btn:hover svg { stroke: #a00; }
</style>
</head>
<body>

<!-- ツールバー(仮) -->
<nav class="toolbar"><span>ツールバー</span></nav>

<div class="main">
    <!-- 戻るボタン(スクロールで追従) -->
    <div class="search-row">
        <button type="button" class="back-btn" aria-label="戻る">
            <svg viewBox="0 0 56 40" width="56" height="40">
                <path d="M52 20H8M22 5L6 20l16 15" fill="none" stroke="#000"
                      stroke-width="7" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
        </button><h2>履歴</h2>
    </div>

    <!-- タブ(3つ → パネルも3つ) -->
    <div class="tabs">
        <button type="button" class="tab active" data-target="panel-0"><%= LookHistiry %><span class="bar"></span></button>
        <button type="button" class="tab" data-target="panel-1"><%= GoodHistory %><span class="bar"></span></button>
        <button type="button" class="tab" data-target="panel-2"><%= BookMark %><span class="bar"></span></button>
    </div>


    <!-- p=0:閲覧 / p=1:いいね(日付の新しい順) / p=2:ブックマーク(とりあえず投稿を表示) -->
    <% for (int p = 0; p < 3; p++) { %>
    <div class="panel<%= (p == 0) ? " active" : "" %>" id="panel-<%= p %>">

    <% for (int n = 0; n < accountNames.length; n++) { %>
    <% int i = (p == 1) ? order[n] : n; %>   <%-- p==1 のときだけ日付順 --%>
    <article class="post">
        <div class="avatar"></div>

        <div class="post-body">
            <div class="post-head">
                <span><%= accountNames[i] %></span>
                <span class="id"><%= accountIds[i] %></span>
                <span><%= postDates[i] %></span>
            </div>

            <% if (accountRule[i] == 1) { %>
                <!-- 画像(画像がある場合は <img src="..."> に差し替え) -->
                <div class="post-audio"><%=ruleNotString %></div>
                <div class="post-image">画像</div>
                <!-- 音声 -->
                <div class="post-audio">
                    <audio controls>
                        <source src="<%= request.getContextPath() %>/audio/sample.mp3" type="audio/mpeg">
                    </audio>
                </div>
            <% } else if (accountRule[i] == 2) { %>
                            <a>
君が代は
千代に 八千代に
さざれ石の
いわおとなりて
こけのむすまで</a>
                <div class="post-image">画像</div>
                <div class="post-audio"><%=ruleNotAudio %></div>
            <% } else if (accountRule[i] == 3) { %>
                            <a>
君が代は
千代に 八千代に
さざれ石の
いわおとなりて
こけのむすまで</a>
                <div class="post-audio"><%=ruleNotImage%></div>
                <div class="post-audio">
                    <audio controls>
                        <source src="<%= request.getContextPath() %>/audio/sample.mp3" type="audio/mpeg">
                    </audio>
                </div>
            <% } else if (accountRule[i] == 0) { %>
                            <a>
君が代は
千代に 八千代に
さざれ石の
いわおとなりて
こけのむすまで</a>
                <div class="post-image">画像</div>
                <div class="post-audio">
                    <audio controls>
                        <source src="<%= request.getContextPath() %>/audio/sample.mp3" type="audio/mpeg">
                    </audio>
                </div>
            <% } %>

            <!-- いいね・コメント・保存 -->
            <div class="post-actions">
                <button type="button" aria-label="<%= good %>">
                    <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="#333" stroke-width="1.6">
                        <path d="M12 21s-8-5.2-8-11a4.5 4.5 0 0 1 8-2.8A4.5 4.5 0 0 1 20 10c0 5.800-8 11-8 11z"/>
                    </svg>
                </button>
                <button type="button" aria-label="コメント">
                    <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="#333" stroke-width="1.6">
                        <path d="M12 4C7 4 3 7.300 3 11.500c0 2 1 3.800 2.600 5.100L4.500 20l4-1.700c1 .3 2.300.5 3.500.5 5 0 9-3.300 9-7.500S17 4 12 4z"/>
                    </svg>
                </button>
                <button type="button" aria-label="保存">
                    <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="#333" stroke-width="1.6">
                        <path d="M6 3h12v18l-6-4.500L6 21z"/>
                    </svg>
                </button>
                <% if (p == 2) { %>
                <!-- 削除ボタン(赤いゴミ箱):ブックマークのパネルだけに表示 -->
                <button type="button" class="delete-btn" aria-label="削除">
                    <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="#d00" stroke-width="1.6"
                         stroke-linecap="round" stroke-linejoin="round">
                        <path d="M4 7h16M9 7V4h6v3M6 7l1 13h10l1-13M10 11v6M14 11v6"/>
                    </svg>
                </button>
                <% } %>
            </div>
        </div>
    </article>
    <% } %>

    </div>
    <% } %>

</div>

<script>
    // タブの切り替え
    var tabs = document.querySelectorAll('.tab');
    var panels = document.querySelectorAll('.panel');
    tabs.forEach(function (tab) {
        tab.addEventListener('click', function () {
            tabs.forEach(function (t) { t.classList.remove('active'); });
            panels.forEach(function (p) { p.classList.remove('active'); });
            tab.classList.add('active');
            document.getElementById(tab.dataset.target).classList.add('active');
        });
    });
</script>
</body>
</html>
