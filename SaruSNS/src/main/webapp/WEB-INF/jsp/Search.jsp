<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    // ============================================================
    // 【書き換えポイント1】画面に出す文字・データはここにまとめる
    // ============================================================
    String placeholderText = "テキスト検索";
    String imageSearchLabel1 = "ここで";
    String imageSearchLabel2 = "画像検索";
    String voiceSearchLabel1 = "マイクで";
    String voiceSearchLabel2 = "音声検索";
    String candidateTitle = "検索画像候補";

    // 入力中に出る履歴(type: text=文字 / voice=音声 / image=画像)
    // 見かけだけの仮データ。後で差し替える
    String[][] history = {
        // { type, 左の小さい文字(音声の時間など), 表示する文字 }
        { "text",  "",     "イタリアンレストラン" },
        { "voice", "1:20", "音声" },
        { "image", "",     "画像" }
    };

    // 検索画像候補(今は仮の色。後で画像パスに差し替える)
    // 例: "images/ramen.png" のように画像パスを入れて、下の <img> の行を有効にする
    String[] candidates = {
        "#5b4a8a", "#5a9ad6", "#4a4a7a",
        "#8a5a7a", "#2f3b6b", "#6ab0e0",
        "#7a6a4a", "#4a7a6a", "#a06a5a"
    };
%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>検索画面</title>
<style>
    /* ========================================================
       【書き換えポイント2】サイズ・色はここだけ変えればOK
       ======================================================== */
    :root {
        --toolbar-width: 180px;   /* ツールバーの幅 */
        --line: #000;             /* 枠線の色 */
        --text: #000;
        --bg: #fff;
        --gap: 16px;
    }

    * { box-sizing: border-box; }
    html, body { margin: 0; height: 100%; }
    body {
        font-family: sans-serif;
        color: var(--text);
        background: var(--bg);
    }

    /* ---------- ツールバー(仮)：スクロールしても動かない ---------- */
    .toolbar {
        position: fixed;
        top: 8px; left: 8px; bottom: 8px;
        width: var(--toolbar-width);
        border: 1px solid var(--line);
        border-radius: 24px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: var(--bg);
        z-index: 20;
    }
    .toolbar span {
        writing-mode: vertical-rl;   /* 縦書き */
        font-size: 28px;
        letter-spacing: 0.4em;
    }

    /* ---------- メイン領域 ---------- */
    .main {
        margin-left: calc(var(--toolbar-width) + 24px);
        padding-right: var(--gap);
    }

    /* ---------- 検索バー：スクロールしても上に張り付く ---------- */
    .search-row {
        position: sticky;
        top: 0;
        z-index: 10;
        display: flex;
        align-items: center;
        gap: var(--gap);
        padding: 12px 0;
        background: var(--bg);
    }
    .back-btn {
        background: none; border: none; padding: 0; cursor: pointer;
        width: 56px; height: 40px; flex: none;
    }
    .search-box {
        flex: 1;
        display: flex;
        align-items: center;
        gap: 8px;
        border: 1px solid var(--line);
        border-radius: 6px;
        padding: 4px 8px;
    }
    .search-box input {
        flex: 1; border: none; outline: none;
        font-size: 18px; background: transparent;
    }
    .search-box svg { flex: none; }

    /* ---------- 入力中の履歴リスト(▼ボタンと一覧) ---------- */
    .search-wrap { position: relative; flex: 1; }
    .search-wrap .search-box { width: 100%; }
    .toggle-btn {
        flex: none;
        width: 44px; height: 36px;
        border: 1px solid var(--line);
        background: var(--bg);
        padding: 0; cursor: pointer;
        display: none;                 /* 文字が入ったときだけ表示 */
    }
    .has-text .toggle-btn { display: block; }
    .history {
        display: none;
        position: absolute;
        top: 100%; left: 48px; right: 48px;
        background: var(--bg);
        list-style: none;
        margin: 0; padding: 0;
        z-index: 30;
    }
    .history.open { display: block; }
    .history li {
        display: flex;
        align-items: center;
        gap: 10px;
        height: 34px;
        border-bottom: 1px solid var(--line);
        font-weight: bold;
        font-size: 16px;
    }
    .history .mark { width: 44px; display: flex; align-items: center; gap: 4px; flex: none; }
    .history .mark small { font-size: 11px; }
    .history .thumb {
        width: 28px; height: 32px;
        background: linear-gradient(#2f2f66, #8a5a7a 55%, #3a4a6a);
    }

    /* ---------- 画像検索 / 音声検索ボタン(見かけだけ) ---------- */
    .mode-row {
        display: flex;
        gap: 4px;
        margin-top: 4px;
    }
    .mode-btn {
        flex: 1;
        border: 1px solid var(--line);
        border-radius: 32px;
        background: var(--bg);
        padding: 16px 8px;
        text-align: center;
        font-weight: bold;
        cursor: pointer;
        font-size: 16px;
        line-height: 1.8;
    }
    .mode-btn .icon { height: 100px; display: flex; align-items: center; justify-content: center; }
    .mode-thumb {
        width: 86px; height: 100px;
        background: linear-gradient(#2f2f66, #8a5a7a 55%, #3a4a6a);
    }
    .mode-image-icon{
	        background-image: url('<%= request.getContextPath() %>/images/マイク.png');
	        width: 86px; height: 100px;
    
    }

    /* ---------- 検索画像候補(ここがスクロールで流れる部分) ---------- */
    .candidate-title { font-weight: bold; margin: 24px 0 8px 12px; }
    .candidate-grid {
        display: grid;
        grid-template-columns: repeat(3, 1fr);
        gap: 12px;
        padding: 0 12px 24px;
    }
    .candidate {
        aspect-ratio: 140 / 150;
        border-radius: 2px;
        overflow: hidden;
    }
    .candidate img { width: 100%; height: 100%; object-fit: cover; display: block; }
</style>
</head>
<body>

<!-- ツールバー(仮) -->
<nav class="toolbar"><span>ツールバー</span></nav>

<div class="main">

    <!-- 検索バー(スクロールで追従)  ※検索実行はしないので form は使わない -->
    <div class="search-row">
        <button type="button" class="back-btn" aria-label="戻る">
            <svg viewBox="0 0 56 40" width="56" height="40">
                <path d="M52 20H8M22 5L6 20l16 15" fill="none" stroke="#000"
                      stroke-width="7" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
        </button>
        <div class="search-wrap" id="searchWrap">
            <div class="search-box">
                <svg viewBox="0 0 40 40" width="36" height="36" aria-hidden="true">
                    <circle cx="16" cy="16" r="11" fill="none" stroke="#000" stroke-width="3.5"/>
                    <path d="M24 24l12 12" stroke="#000" stroke-width="4" stroke-linecap="round"/>
                </svg>
                <input type="text" id="searchInput" placeholder="<%= placeholderText %>" autocomplete="off">
                <!-- ▼ボタン：文字が入ると出る。押すと履歴を開閉 -->
                <button type="button" class="toggle-btn" id="toggleBtn" aria-label="履歴を開く">
                    <svg viewBox="0 0 40 34" width="36" height="30"><path d="M2 2h36L20 32z" fill="#000"/></svg>
                </button>
            </div>

            <!-- 履歴リスト(見かけだけ：選んでも何も起きない) -->
            <ul class="history" id="historyList">
                <% for (String[] h : history) { %>
                <li>
                    <span class="mark">
                    <% if ("text".equals(h[0])) { %>
                        <svg viewBox="0 0 40 40" width="26" height="26" aria-hidden="true">
                            <circle cx="16" cy="16" r="11" fill="none" stroke="#000" stroke-width="3.5"/>
                            <path d="M24 24l12 12" stroke="#000" stroke-width="4" stroke-linecap="round"/>
                        </svg>
                    <% } else if ("voice".equals(h[0])) { %>
                        <svg viewBox="0 0 80 100" width="18" height="24" aria-hidden="true">
                            <rect x="27" y="4" width="26" height="52" rx="13" fill="#000"/>
                            <path d="M10 42a30 30 0 0 0 60 0M40 72v18M22 92h36" fill="none"
                                  stroke="#000" stroke-width="9" stroke-linecap="round"/>
                        </svg>
                        <small><%= h[1] %></small>
                    <% } else { %>
                        <span class="thumb"></span>
                    <% } %>
                    </span>
                    <span><%= h[2] %></span>
                </li>
                <% } %>
            </ul>
        </div>
    </div>

    <!-- 画像検索 / 音声検索(見かけだけ：クリックしても何も起きない) -->
    <div class="mode-row">
        <button type="button" class="mode-btn">
            <div class="icon"><div class="mode-image-icon"></div></div>
            <%= imageSearchLabel1 %><br><%= imageSearchLabel2 %>
        </button>
        <button type="button" class="mode-btn">
            <div class="icon">
                <svg viewBox="0 0 80 100" width="80" height="100" aria-hidden="true">
                    <rect x="27" y="4" width="26" height="52" rx="13" fill="#000"/>
                    <path d="M10 42a30 30 0 0 0 60 0M40 72v18M22 92h36" fill="none"
                          stroke="#000" stroke-width="7" stroke-linecap="round"/>
                </svg>
            </div>
            <%= voiceSearchLabel1 %><br><%= voiceSearchLabel2 %>
        </button>
    </div>

    <!-- 検索画像候補 -->
    <div class="candidate-title"><%= candidateTitle %></div>
    <div class="candidate-grid">
        <% for (int i = 0; i < candidates.length; i++) { %>
            <div class="candidate" style="background:<%= candidates[i] %>">
                <%-- 画像に差し替えるとき：background の色指定をやめて、下の行を有効にする --%>
                <%-- <img src="<%= candidates[i] %>" alt="検索画像候補<%= i + 1 %>"> --%>
            </div>
        <% } %>
    </div>

</div>

<script>
    // 入力したら▼と履歴を出す／空に戻したら隠す(見かけだけの動き)
    (function () {
        var wrap  = document.getElementById("searchWrap");
        var input = document.getElementById("searchInput");
        var btn   = document.getElementById("toggleBtn");
        var list  = document.getElementById("historyList");

        input.addEventListener("input", function () {
            var hasText = input.value.length > 0;
            wrap.classList.toggle("has-text", hasText);
            list.classList.toggle("open", hasText);
        });

        btn.addEventListener("click", function () {
            list.classList.toggle("open");
        });

        // 検索欄の外をクリックしたら履歴を閉じる
        document.addEventListener("click", function (e) {
            if (!wrap.contains(e.target)) list.classList.remove("open");
        });
    })();
</script>

</body>
</html>
