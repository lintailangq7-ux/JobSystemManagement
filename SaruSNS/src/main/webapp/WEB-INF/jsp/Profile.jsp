<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" import="java.util.*"%>
<%
    // 投稿の表示データ(仮)。件数を増やすとスクロールで流れます
    String[] accountNames = { "アカウント名", "アカウント名", "アカウント名", "アカウント名", "アカウント名","アカウント名" };
    int[] accountRule     = { 1, 1, 1, 1, 1 ,1};
    String[] accountIds   = { "アカウントID", "アカウントID", "アカウントID", "アカウントID", "アカウントID","アカウントID" };
    boolean[] accountFollow = { true, true, true, true, true,true };  // フォロー中のアカウントか(仮)
    int followNum =0;
    int followerNum =0;
    // 各投稿の投稿日時(accountNames と同じ順番・同じ件数にする)
    String[] postDates = {
        "2026-10-27 09:00", "2026-10-29 08:30", "2026-10-25 18:10",
        "2026-10-29 07:45", "2026-10-28 12:00", "2026-10-26 21:15"
    };



    String good ="いいね";
    String follow="フォロー中";
    String follower="フォロワー";
    String editProfile="プロフィール編集";
    String postLabel="ポスト";
    String bioLink="青わ猫";                    // 青色で表示する部分(仮)
    String bioText="文字が見えません";           // プロフィール文(仮)
    String ruleNotString ="ここには文がありますが、あなたは読めません";
    String ruleNotImage ="ここには画像がありますが、あなたは見えません";
    String ruleNotAudio ="ここには音声がありますが、あなたは聞こえません";
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>プロフィール</title>
<style>
    :root {
        --toolbar-width: 180px;
        --line: #000;
        --text: #000;
        --bg: #fff;
        --accent: #1a9ad6;   /* アカウントIDの色 */
        --gap: 16px;
        --search-height: 64px;   /* 戻るボタンの行の高さ */
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

    /* ---------- プロフィール上部 ---------- */
    .profile {
        padding: 0 16px 12px;
        border-bottom: 1px solid var(--line);
    }
    /* アイコン+アカウント名(左) と プロフィール編集ボタン(右) */
    .profile-top {
        display: flex;
        align-items: center;
        justify-content: space-between;
    }
    .profile-user {
        display: flex;
        flex-direction: column;
        align-items: flex-start;
        gap: 4px;
    }
    .MainAvatar {
        flex: none;
        width: 96px; height: 96px;
        border-radius: 50%;
        background: #4caf32;      /* 緑 */
        border: 1px solid var(--line);
    }
    .profile-name { font-size: 13px; margin: 0; font-weight: normal; }
    .profile-id { font-size: 12px; color: var(--accent); }

    .edit-btn {
        border: 2px solid #000;
        border-radius: 999px;
        background: var(--bg);
        padding: 8px 18px;
        font-size: 13px;
        font-weight: bold;
        cursor: pointer;
        margin-right: 24px;
    }

    /* プロフィール文 */
    .profile-bio {
        display: flex;
        align-items: center;
        gap: 6px;
        margin-top: 10px;
        font-size: 13px;
    }
    .profile-bio svg { flex: none; }
    .profile-bio .link { color: var(--accent); }

    /* フォロー中 / フォロワー */
    .profile-follow {
        display: flex;
        gap: 18px;
        margin-top: 12px;
        font-size: 13px;
    }
    .profile-follow b { margin-right: 4px; }

    /* 「ポスト」の見出し */
    .profile-post-label {
        margin-top: 14px;
        font-size: 13px;
    }

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
        </button>
    </div>

    <!-- プロフィール上部 -->
    <section class="profile">
        <div class="profile-top">
            <div class="profile-user">
                <div class="MainAvatar"></div>
                <h2 class="profile-name"><%= accountNames[1] %></h2>
            </div>
            <button type="button" class="edit-btn"><%= editProfile %></button>
        </div>

        <!-- プロフィール文(小さなアイコン+青い文字+本文) -->
        <div class="profile-bio">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#c33" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                <circle cx="12" cy="12" r="9"/>
                <path d="M8 12h8M12 8v8"/>
            </svg>
            <span class="link"><%= bioLink %></span>
            <span><%= bioText %></span>
        </div>

        <!-- フォロー中 / フォロワー -->
        <div class="profile-follow">
            <span><b><%= followNum %></b><%= follow %></span>
            <span><b><%= followerNum %></b><%= follower %></span>
        </div>

        <div class="profile-post-label"><%= postLabel %></div>
    </section>

    <% for (int i = 0; i < accountNames.length; i++) { %>
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
            </div>
        </div>
    </article>
    <% } %>

</div>

</body>
</html>
