<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.util.List, java.util.ArrayList, java.util.Map" %>
<%
	// ==== 実データに差し替える想定のダミー値 ====
	// クラス名
	String className = (String) request.getAttribute("ClassName");
	if (className == null) className = "S3A1";

	// 男女・合計人数
	Integer maleCount   = (Integer) request.getAttribute("MaleCount");
	Integer femaleCount = (Integer) request.getAttribute("FemaleCount");
	if (maleCount == null)   maleCount = 30;
	if (femaleCount == null) femaleCount = 10;
	int totalCount = maleCount + femaleCount;

	// 年
	Integer targetYear = (Integer) request.getAttribute("TargetYear");
	if (targetYear == null) targetYear = 2026;

	// 月一覧（ドロップダウンリストで表示する月、新しい順）
	List<Integer> monthList = (List<Integer>) request.getAttribute("MonthList");
	if (monthList == null) {
		monthList = new ArrayList<Integer>();
		monthList.add(6);
		monthList.add(5);
		monthList.add(4);
		monthList.add(3);
		monthList.add(2);
	}

	// 現在選択されている月
	Integer selectedMonth = (Integer) request.getAttribute("SelectedMonth");
	if (selectedMonth == null) selectedMonth = 2;

	// 各月に紐づく「日付範囲」の候補（ドロップダウンを開くと出てくる中身）
	Map<Integer, List<String>> dateRangeMap = (Map<Integer, List<String>>) request.getAttribute("DateRangeMap");
	if (dateRangeMap == null) {
		dateRangeMap = new java.util.LinkedHashMap<Integer, List<String>>();
		for (Integer m : monthList) {
			List<String> ranges = new ArrayList<String>();
			ranges.add("1日〜3日");
			ranges.add("4日〜8日");
			ranges.add("9日〜15日");
			ranges.add("16日〜末日");
			dateRangeMap.put(m, ranges);
		}
	}

	// 選択中の日付範囲
	String selectedRange = (String) request.getAttribute("SelectedRange");
	if (selectedRange == null) selectedRange = "4日〜8日";

	// 内定状況テーブル（男・女・合計）
	Integer naiteiMale = (Integer) request.getAttribute("NaiteiMale");
	Integer naiteiFemale = (Integer) request.getAttribute("NaiteiFemale");
	Integer miteiMale = (Integer) request.getAttribute("MiteiMale");
	Integer miteiFemale = (Integer) request.getAttribute("MiteiFemale");
	if (naiteiMale == null)   naiteiMale = 10;
	if (naiteiFemale == null) naiteiFemale = 10;
	if (miteiMale == null)    miteiMale = 20;
	if (miteiFemale == null)  miteiFemale = 0;
	int naiteiTotal = naiteiMale + naiteiFemale;
	int miteiTotal  = miteiMale + miteiFemale;
	int naiteiRitsuMale   = maleCount   == 0 ? 0 : Math.round(naiteiMale   * 100f / maleCount);
	int naiteiRitsuFemale = femaleCount == 0 ? 0 : Math.round(naiteiFemale * 100f / femaleCount);
	int naiteiRitsuTotal  = totalCount  == 0 ? 0 : Math.round(naiteiTotal * 100f / totalCount);
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>活動報告</title>
<style>
  * { box-sizing: border-box; }
  body {
    font-family: "MS PGothic", "Meiryo", sans-serif;
    background: #ffffff;
    padding: 20px;
    color: #000;
  }
  .container {
    max-width: 900px;
    margin: 0 auto;
    position: relative;
  }

  /* ===== ヘッダー ===== */
  .header-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    flex-wrap: wrap;
    gap: 16px;
    margin-bottom: 22px;
  }

  /* タイトルの横の▲は「前の画面に戻る」ボタン */
  .title-box {
    display: flex;
    align-items: center;
    border: 1px solid #7fd3e8;
    border-radius: 6px;
    padding: 8px 14px;
    gap: 8px;
  }
  .back-btn {
    border: none;
    background: none;
    padding: 0;
    cursor: pointer;
    width: 0; height: 0;
    border-top: 10px solid transparent;
    border-bottom: 10px solid transparent;
    border-right: 14px solid #29b6d8;
  }
  .back-btn:hover { border-right-color: #1f93b3; }
  .title-text {
    font-size: 20px;
  }

  .class-label {
    text-align: center;
    font-size: 11px;
    color: #333;
  }
  .class-value {
    font-size: 22px;
    font-weight: bold;
    margin-top: 2px;
  }

  .summary-table {
    border-collapse: collapse;
    font-size: 16px;
  }
  .summary-table td {
    border: 1px solid #999;
    padding: 8px 18px;
    text-align: center;
  }
  .male-head   { background: #29b6d8; color: #fff; font-weight: bold; }
  .female-head { background: #e02020; color: #fff; font-weight: bold; }
  .total-head  { background: #f5a800; color: #fff; font-weight: bold; }

  /* ===== 本体 ===== */
  .body-row {
    display: flex;
    gap: 40px;
    flex-wrap: wrap;
  }

  /* 左：年 + 月のドロップダウンリスト */
  .selector-col {
    width: 130px;
    flex-shrink: 0;
  }
  .year-label {
    font-size: 16px;
    margin-bottom: 8px;
  }

  .month-item { margin-bottom: 6px; }

  .month-toggle {
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    border: 1px solid #7fd3e8;
    border-radius: 4px;
    padding: 8px 10px;
    font-size: 16px;
    background: #fff;
    cursor: pointer;
    font-family: inherit;
  }
  .month-toggle.active {
    border: 2px solid #29b6d8;
  }
  .month-toggle .arrow {
    position: absolute;
    right: -14px;
    top: 50%;
    width: 0; height: 0;
    border-left: 9px solid transparent;
    border-right: 9px solid transparent;
    border-top: 12px solid #ffe000;
    transform: translateY(-50%) rotate(0deg);
    transition: transform 0.15s ease;
  }
  /* ドロップダウンを開いている間は矢印を上向きに */
  .month-toggle[aria-expanded="true"] .arrow {
    transform: translateY(-50%) rotate(180deg);
  }

  /* 開いたときに下の月を押し下げる通常のドロップダウンリスト（position:absoluteにしない） */
  .month-submenu {
    list-style: none;
    margin: 4px 0 0;
    padding: 0;
    background: #fff;
    border: 1px solid #7fd3e8;
    border-radius: 4px;
    box-shadow: 0 3px 6px rgba(0,0,0,0.12);
    overflow: hidden;
    max-height: 0;
    display: block;
    visibility: hidden;
    transition: max-height 0.18s ease;
  }
  .month-submenu.open {
    max-height: 260px;
    visibility: visible;
  }
  .month-submenu li a,
  .month-submenu li button {
    display: block;
    width: 100%;
    text-align: center;
    padding: 8px 8px;
    font-size: 13px;
    background: #fff;
    border: none;
    border-top: 1px solid #eef6f9;
    color: #1f5f74;
    cursor: pointer;
    font-family: inherit;
  }
  .month-submenu li:first-child a,
  .month-submenu li:first-child button { border-top: none; }
  .month-submenu li a:hover,
  .month-submenu li button:hover { background: #eaf7fb; }
  .month-submenu li.current a,
  .month-submenu li.current button {
    background: #29b6d8;
    color: #fff;
    font-weight: bold;
  }

  /* 右：日付範囲 + 内定状況テーブル */
  .right-col { flex: 1; min-width: 320px; }

  .date-range-box {
    display: inline-block;
    border: 1px solid #7fd3e8;
    border-radius: 6px;
    padding: 10px 20px;
    font-size: 18px;
    margin-bottom: 14px;
  }

  table.main-table {
    border-collapse: collapse;
    width: 100%;
    font-size: 16px;
  }
  table.main-table th, table.main-table td {
    border: 1px solid #999;
    padding: 10px 14px;
    text-align: center;
  }
  table.main-table th {
    background: #f2f2f2;
    font-weight: normal;
  }

  @media (max-width: 640px) {
    .selector-col { width: 100%; }
  }
</style>
</head>
<body>
<div class="container">

  <div class="header-row">
    <div class="title-box">
      <button type="button" class="back-btn" onclick="history.back()" title="前の画面に戻る" aria-label="前の画面に戻る"></button>
      <span class="title-text">活動報告</span>
    </div>

    <div class="class-label">
      クラス
      <div class="class-value"><%= className %></div>
    </div>

    <table class="summary-table">
      <tr>
        <td class="male-head">男</td>
        <td><%= maleCount %>人</td>
        <td class="female-head">女</td>
        <td><%= femaleCount %>人</td>
      </tr>
      <tr>
        <td class="total-head" colspan="2">合計</td>
        <td colspan="2"><%= totalCount %>人</td>
      </tr>
    </table>
  </div>

  <div class="body-row">

    <!-- 月選択：▼をクリックすると開くドロップダウンリスト（開くと下の月が押し下げられる） -->
    <div class="selector-col">
      <div class="year-label"><%= targetYear %>年</div>

      <% for (Integer m : monthList) {
           boolean isActive = m.equals(selectedMonth);
           List<String> ranges = (dateRangeMap != null) ? dateRangeMap.get(m) : null;
      %>
      <div class="month-item">
        <button type="button"
                class="month-toggle<%= isActive ? " active" : "" %>"
                aria-expanded="false"
                data-month="<%= m %>">
          <%= m %>月
          <span class="arrow"></span>
        </button>
        <% if (ranges != null && !ranges.isEmpty()) { %>
        <ul class="month-submenu">
          <% for (String r : ranges) { %>
          <li class="<%= r.equals(selectedRange) && isActive ? "current" : "" %>">
            <button type="button" class="range-select" data-month="<%= m %>" data-range="<%= r %>"><%= r %></button>
          </li>
          <% } %>
        </ul>
        <% } %>
      </div>
      <% } %>
    </div>

    <!-- 選択中の日付範囲 + 内定状況テーブル -->
    <div class="right-col">
      <div class="date-range-box"><%= selectedRange %></div>

      <table class="main-table">
        <thead>
          <tr>
            <th></th>
            <th>男</th>
            <th>女</th>
            <th>合計</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <th>内定者</th>
            <td><%= naiteiMale %>人</td>
            <td><%= naiteiFemale %>人</td>
            <td><%= naiteiTotal %>人</td>
          </tr>
          <tr>
            <th>未定</th>
            <td><%= miteiMale %>人</td>
            <td><%= miteiFemale %>人</td>
            <td><%= miteiTotal %>人</td>
          </tr>
          <tr>
            <th>内定率</th>
            <td><%= naiteiRitsuMale %>%</td>
            <td><%= naiteiRitsuFemale %>%</td>
            <td><%= naiteiRitsuTotal %>%</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>

</div>

<script>
  // 月ボタン：クリックでドロップダウンリストを開閉（同時に開くのは1つだけ）
  document.querySelectorAll('.month-toggle').forEach(btn => {
    btn.addEventListener('click', () => {
      const submenu = btn.parentElement.querySelector('.month-submenu');
      const willOpen = !btn.classList.contains('open');

      // 他の開いているメニューを閉じる
      document.querySelectorAll('.month-toggle.open').forEach(other => {
        if (other !== btn) {
          other.classList.remove('open');
          other.setAttribute('aria-expanded', 'false');
          const os = other.parentElement.querySelector('.month-submenu');
          if (os) os.classList.remove('open');
        }
      });

      btn.classList.toggle('open', willOpen);
      btn.setAttribute('aria-expanded', willOpen ? 'true' : 'false');
      if (submenu) submenu.classList.toggle('open', willOpen);

      if (!submenu) {
        // 中身（日付範囲）が無い月は、そのままその月を選択してサーバーへ
        location.href = 'ActivityReportServlet?month=' + btn.dataset.month;
      }
    });
  });

  // 日付範囲を選択したら、その月・範囲でサーバーへ
  document.querySelectorAll('.range-select').forEach(btn => {
    btn.addEventListener('click', () => {
      const month = btn.dataset.month;
      const range = btn.dataset.range;
      location.href = 'ActivityReportServlet?month=' + encodeURIComponent(month)
                     + '&range=' + encodeURIComponent(range);
    });
  });
</script>
</body>
</html>
