<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>ホーム</title>
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



</style>
</head>
<body>

</body>
</html>