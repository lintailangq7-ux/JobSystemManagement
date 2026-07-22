<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.util.*" %>
<%@ page import="model.ModelStudent, model.ModelEmployment" %>
<%
	// ==== ReportSevlet から渡される想定 ====
	// request.setAttribute("list", list);       ← List<ModelEmployment>（ReportLogic.execute()）
	// request.setAttribute("StuList", StuList);  ← List<ModelStudent>（StudentLogic.execute()）
	//
	// MonthWeekMap / SelectedMonth は、月選択UI用に別途渡す想定（未実装ならダミー値で代用）。
	// request.setAttribute("MonthWeekMap", monthWeekMap); ← LinkedHashMap<String,List<String>>
	// request.setAttribute("SelectedMonth", "6月");
	// クラス名は、後で実データに合わせて追記してください。

	// 月 → 週リスト（あっせん報告の対象週）
	@SuppressWarnings("unchecked")
	LinkedHashMap<String, List<String>> monthWeekMap =
			(LinkedHashMap<String, List<String>>) request.getAttribute("MonthWeekMap");
	if (monthWeekMap == null) {
		monthWeekMap = new LinkedHashMap<String, List<String>>();
		monthWeekMap.put("6月", Arrays.asList("4日～8日", "11日～15日", "18日～31日"));
		monthWeekMap.put("5月", Arrays.asList("7日～11日", "14日～18日", "21日～31日"));
		monthWeekMap.put("4月", Arrays.asList("1日～5日", "8日～12日", "15日～30日"));
		monthWeekMap.put("3月", Arrays.asList("2日～6日", "9日～13日", "16日～31日"));
		monthWeekMap.put("2月", Arrays.asList("2日～6日", "9日～13日", "16日～28日"));
	}

	// 選択中の月：URLパラメータ(?month=6月)があれば優先。無ければrequest属性、それも無ければデフォルト
	String selectedMonth = request.getParameter("month");
	if (selectedMonth == null) selectedMonth = (String) request.getAttribute("SelectedMonth");
	if (selectedMonth == null) selectedMonth = "6月";

	// 選択中の月を数値化（"6月" → 6）
	int targetMonth = Integer.parseInt(selectedMonth.replace("月", ""));

	// 選択中の週（例："4日～8日"）。指定があれば、対象月内の日付範囲でさらに絞り込む
	String selectedWeek = request.getParameter("week");
	Integer weekStartDay = null, weekEndDay = null;
	if (selectedWeek != null && selectedWeek.contains("～")) {
		try {
			String[] parts = selectedWeek.replace("日", "").split("～");
			weekStartDay = Integer.parseInt(parts[0].trim());
			weekEndDay   = Integer.parseInt(parts[1].trim());
		} catch (Exception e) {
			// 想定外の形式なら週指定は無視して月だけで絞り込む
			weekStartDay = null;
			weekEndDay = null;
		}
	}

	// 学生一覧（男女人数の集計元）
	@SuppressWarnings("unchecked")
	List<ModelStudent> stuList = (List<ModelStudent>) request.getAttribute("StuList");
	if (stuList == null) stuList = new ArrayList<ModelStudent>();

	// 就職（内定）情報一覧。ModelEmploymentは gakusekiNo を直接持っているので、
	// 学籍番号ごとにグルーピングして使う
	@SuppressWarnings("unchecked")
	List<ModelEmployment> employmentList = (List<ModelEmployment>) request.getAttribute("list");
	if (employmentList == null) employmentList = new ArrayList<ModelEmployment>();

	Map<Integer, List<ModelEmployment>> employmentMap = new HashMap<Integer, List<ModelEmployment>>();
	for (ModelEmployment em : employmentList) {
		List<ModelEmployment> emList = employmentMap.get(em.getGakusekiNo());
		if (emList == null) {
			emList = new ArrayList<ModelEmployment>();
			employmentMap.put(em.getGakusekiNo(), emList);
		}
		emList.add(em);
	}

	int maleTotal = 0, femaleTotal = 0;
	int naiteiMale = 0, naiteiFemale = 0;

	for (ModelStudent sd : stuList) {
		String sei = sd.getSeibetsu();
		boolean isMale   = "M".equals(sei);
		boolean isFemale = "F".equals(sei);
		if (isMale)   maleTotal++;
		if (isFemale) femaleTotal++;

		// その学生の就職情報（複数件）の中に、選択月（・週）時点で「内定確定」しているものが
		// 1件でもあれば、その学生を内定者としてカウントする
		List<ModelEmployment> emList = employmentMap.get(sd.getGakusekiNo());
		boolean naiteiKakutei = false;
		if (emList != null) {
			for (ModelEmployment em : emList) {
				if (em != null && em.getNaiteiKakutei() == 2 && em.getNaiteiKakuteiBi() != null) {
					int kakuteiMonth = em.getNaiteiKakuteiBi().getMonthValue();
					// 選択月「以前」に確定した内定を、その月時点の内定としてカウント（累計）
					if (kakuteiMonth <= targetMonth) {
						boolean withinWeek = true;
						// 週が指定されている場合は、対象月内の確定分のみ日付範囲でさらに絞り込む
						if (kakuteiMonth == targetMonth && weekStartDay != null && weekEndDay != null) {
							int day = em.getNaiteiKakuteiBi().getDayOfMonth();
							withinWeek = (day >= weekStartDay && day <= weekEndDay);
						}
						if (withinWeek) {
							naiteiKakutei = true;
							break;
						}
					}
				}
			}
		}
		if (naiteiKakutei) {
			if (isMale)   naiteiMale++;
			if (isFemale) naiteiFemale++;
		}
	}

	int grandTotal   = maleTotal + femaleTotal;
	int miteiMale    = maleTotal   - naiteiMale;
	int miteiFemale  = femaleTotal - naiteiFemale;
	int naiteiTotal  = naiteiMale + naiteiFemale;
	int miteiTotal   = miteiMale  + miteiFemale;
	int rateMale     = maleTotal   == 0 ? 0 : Math.round(naiteiMale   * 100f / maleTotal);
	int rateFemale   = femaleTotal == 0 ? 0 : Math.round(naiteiFemale * 100f / femaleTotal);
	int rateTotal    = grandTotal  == 0 ? 0 : Math.round(naiteiTotal  * 100f / grandTotal);
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>活動報告</title>
<style>
  body {
    font-family: "MS PGothic", "Meiryo", sans-serif;
    background: #ffffff;
    padding: 20px;
  }
  .container {
    max-width: 1100px;
    position: relative;
    zoom: 1.35; /* Chrome/Edge向け拡大表示。position:fixedの座標計算には影響しない */
  }

  /* ===== header（元のJSPのタイトルボックスを流用） ===== */
  .header-row {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    margin-bottom: 24px;
  }
  .title-box {
    display: flex;
    align-items: center;
    border: 1px solid #999;
    width: 220px;
    height: 60px;
  }
  .title-arrow {
    width: 60px;
    height: 100%;
    background: #29ABE2;
    border: none;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
  }
  .title-arrow svg { width: 30px; height: 30px; }
  .title-text {
    font-size: 20px;
    font-weight: bold;
    margin-left: 14px;
  }

  .right-block {
    display: flex;
    flex-direction: column;
    align-items: center;
  }

  .class-info {
    text-align: center;
    margin-bottom: 6px;
  }
  .class-label {
    font-size: 12px;
    color: #333;
  }
  .class-name {
    font-size: 22px;
    font-weight: bold;
  }

  /* ===== 男女合計テーブル ===== */
  .stats-table {
    border-collapse: collapse;
  }
  .stats-table td {
    border: 1px solid #999;
    font-size: 15px;
    padding: 8px 16px;
    text-align: center;
    height: 20px;
  }
  .badge-male   { background: #29ABE2; color: #fff; font-weight: bold; }
  .badge-female { background: #d9001b; color: #fff; font-weight: bold; }
  .badge-total  { background: #f5a623; color: #fff; font-weight: bold; }

  /* ===== 本体 2カラム ===== */
  .main-row {
    display: flex;
    gap: 24px;
    align-items: flex-start;
  }

  /* 年 + 月リスト */
  .year-block {
    width: 150px;
  }
  .year-label {
    font-size: 15px;
    margin-bottom: 8px;
  }
  .month-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
  .month-item {
    display: flex;
    align-items: center;
    border: 2px solid #29abe2;
    border-radius: 4px;
    overflow: hidden;
    height: 34px;
    background: #fff;
  }
  .month-item.selected {
    border-color: #1f5fa8;
    box-shadow: 0 0 0 1px #1f5fa8 inset;
  }
  .month-item span {
    flex: 1;
    padding-left: 10px;
    font-size: 15px;
    font-weight: bold;
  }
  .month-triangle {
    width: 34px;
    height: 100%;
    background: #fff;
    border: none;
    border-left: 2px solid #29abe2;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
  }
  .month-triangle svg { width: 16px; height: 16px; }

  /* 右側コンテンツ（利用可能な幅の中央に配置） */
  .content-block {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
  }
  .current-box {
    display: inline-block;
    border: 1px solid #999;
    padding: 10px 24px;
    font-size: 16px;
    font-weight: bold;
    white-space: nowrap;
    margin-bottom: 12px;
  }

  /* ===== テーブル（元JSPのth/tdを流用） ===== */
  table.report-table {
    border-collapse: collapse;
    width: 100%;
    max-width: 600px;
  }
  table.report-table th {
    background: #1f5fa8;
    color: #fff;
    font-size: 13px;
    padding: 8px 4px;
    border: 1px solid #999;
  }
  table.report-table td {
    border: 1px solid #999;
    font-size: 14px;
    padding: 8px 4px;
    text-align: center;
    height: 26px;
  }
  table.report-table td.rowhead {
    background: #29ABE2;
    color: #003366;
    font-weight: bold;
  }

  /* ===== 週選択ポップアップ（元JSPの ctx-menu を流用したデザイン） ===== */
  .week-popup {
    position: fixed;
    background: #fff;
    border: 1px solid #999;
    box-shadow: 0 2px 6px rgba(0,0,0,0.25);
    min-width: 140px;
    z-index: 1000;
    display: none;
  }
  .week-popup .week-header {
    background: #fff;
    border-bottom: 1px solid #999;
    text-align: center;
    font-weight: bold;
    font-size: 14px;
    padding: 6px 0;
  }
  .week-popup button {
    display: block;
    width: 100%;
    text-align: center;
    padding: 8px 12px;
    background: none;
    border: none;
    border-bottom: 1px solid #ccc;
    font-size: 13px;
    cursor: pointer;
    color: #222;
  }
  .week-popup button:last-child { border-bottom: none; }
  .week-popup button:hover {
    background: #1f5fa8;
    color: #fff;
  }
</style>
</head>
<body>

<div class="container">

  <div class="header-row">
    <div class="title-box">
      <button class="title-arrow" onclick="history.back()">
        <svg viewBox="0 0 24 24"><polygon points="18,2 18,22 4,12" fill="#FFE600"/></svg>
      </button>
      <div class="title-text">活動報告</div>
    </div>

    <div class="right-block">
      <div class="class-info">
        <div class="class-label">クラス</div>
        <!-- TODO: 実データに差し替え（クラス名） -->
        <div class="class-name">S3A1</div>
      </div>

      <table class="stats-table">
        <tr>
          <td class="badge-male">男</td>
          <td><%= maleTotal %>人</td>
          <td class="badge-female">女</td>
          <td><%= femaleTotal %>人</td>
        </tr>
        <tr>
          <td class="badge-total" colspan="2">合計</td>
          <td colspan="2"><%= grandTotal %>人</td>
        </tr>
      </table>
    </div>
  </div>

  <div class="main-row">

    <!-- 年 ＋ 月リスト -->
    <div class="year-block">
      <!-- TODO: 実データに差し替え（対象年） -->
      <div class="year-label">2026年</div>
      <div class="month-list">
      <% for (String month : monthWeekMap.keySet()) {
             boolean isSelected = month.equals(selectedMonth);
      %>
        <div class="month-item<%= isSelected ? " selected" : "" %>" data-month="<%= month %>">
          <span><%= month %></span>
          <button type="button" class="month-triangle" data-month="<%= month %>">
            <svg viewBox="0 0 24 24"><polygon points="4,7 20,7 12,18" fill="#FFE600"/></svg>
          </button>
        </div>
      <% } %>
      </div>
    </div>

    <!-- 内定状況 -->
    <div class="content-block">
      <div class="current-box" id="currentBox"><%= selectedMonth %><%= selectedWeek != null ? " " + selectedWeek : "" %>現在</div>

      <table class="report-table">
        <tr>
          <th></th>
          <th>男</th>
          <th>女</th>
          <th>合計</th>
        </tr>
        <tr>
          <td class="rowhead">内定者</td>
          <td id="naiteiMale"><%= naiteiMale %>人</td>
          <td id="naiteiFemale"><%= naiteiFemale %>人</td>
          <td id="naiteiTotal"><%= naiteiTotal %>人</td>
        </tr>
        <tr>
          <td class="rowhead">未定</td>
          <td id="miteiMale"><%= miteiMale %>人</td>
          <td id="miteiFemale"><%= miteiFemale %>人</td>
          <td id="miteiTotal"><%= miteiTotal %>人</td>
        </tr>
        <tr>
          <td class="rowhead">内定率</td>
          <td id="rateMale"><%= rateMale %>%</td>
          <td id="rateFemale"><%= rateFemale %>%</td>
          <td id="rateTotal"><%= rateTotal %>%</td>
        </tr>
      </table>
    </div>


  </div>
</div>

<!-- 週選択ポップアップ -->
<div class="week-popup" id="weekPopup">
  <div class="week-header" id="weekPopupHeader"></div>
</div>

<script>
  // ===== JSPが出力した「月→週リスト」をJSに渡す =====
  var monthWeekMap = {
  <%
    boolean first = true;
    for (Map.Entry<String, List<String>> entry : monthWeekMap.entrySet()) {
        if (!first) { %>,<% }
        first = false;
  %>
    "<%= entry.getKey() %>": [
    <%
      List<String> weeks = entry.getValue();
      for (int i = 0; i < weeks.size(); i++) {
          if (i > 0) { %>,<% }
    %>"<%= weeks.get(i) %>"<%
      }
    %>]
  <% } %>
  };

  var popup = document.getElementById('weekPopup');
  var popupHeader = document.getElementById('weekPopupHeader');

  // 「▽」ボタンをクリックしたら、その月の週リストをポップアップ表示
  document.querySelectorAll('.month-triangle').forEach(function (btn) {
    btn.addEventListener('click', function (e) {
      e.stopPropagation();
      var month = btn.dataset.month;
      var weeks = monthWeekMap[month] || [];

      // 中身を作り直す
      popupHeader.textContent = month;
      var buttons = popup.querySelectorAll('button.week-item');
      buttons.forEach(function (b) { b.remove(); });

      weeks.forEach(function (w) {
        var b = document.createElement('button');
        b.type = 'button';
        b.className = 'week-item';
        b.textContent = w;
        b.addEventListener('click', function () {
          popup.style.display = 'none';
          selectMonth(month, w);
        });
        popup.appendChild(b);
      });

      var rect = btn.getBoundingClientRect();
      popup.style.left = rect.left + 'px';
      popup.style.top = (rect.bottom + 4) + 'px';
      popup.style.display = 'block';
    });
  });

  // 月そのものをクリックしても選択状態にする
  document.querySelectorAll('.month-item span').forEach(function (span) {
    span.addEventListener('click', function () {
      var item = span.closest('.month-item');
      selectMonth(item.dataset.month, null);
    });
  });

  function selectMonth(month, week) {
    // ReportSevletに月（・週）をパラメータで渡して再読み込みし、
    // サーバー側で内定者数などを再集計した最新データを表示する
    var url = 'ReportSevlet?month=' + encodeURIComponent(month);
    if (week) {
      url += '&week=' + encodeURIComponent(week);
    }
    location.href = url;
  }

  // ポップアップ外クリックで閉じる
  document.addEventListener('click', function (e) {
    if (!popup.contains(e.target)) {
      popup.style.display = 'none';
    }
  });
</script>

</body>
</html>
