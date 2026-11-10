<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>指導情報登録</title>
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
</head>
<body>

    <form action="saveShidou" method="post">
        <label>学籍番号:</label>
        <input type="text" name="gakusekiNo" required><br><br>

        <!-- 企業検索部分 -->
        <label>企業名:</label>
        <input type="text" id="companySearch" placeholder="企業名を入力..." autocomplete="off">
        <input type="hidden" id="companyId" name="kigyouId"> <!-- 実際送信するID -->

        <div id="companyList" style="border:1px solid #ccc; max-height:200px; overflow-y:auto; display:none;"></div>

        <br>
        <input type="submit" value="登録">
    </form>

<script>
$(document).ready(function() {
    let timer = null;

    $("#companySearch").on("keyup", function() {
        clearTimeout(timer);
        
        const keyword = $(this).val().trim();
        
        if (keyword.length < 1) {
            $("#companyList").hide();
            return;
        }

        timer = setTimeout(function() {
            $.ajax({
                url: "<%= request.getContextPath() %>/CompanySearchServlet",  // ServletのURL
                type: "GET",
                data: { keyword: keyword },
                dataType: "json",
                success: function(data) {
                    let html = "";
                    if (data.length === 0) {
                        html = "<div>該当する企業がありません</div>";
                    } else {
                        data.forEach(function(company) {
                            html += `
                                <div class="company-item" data-id="${company.kigyouId}" 
                                     style="padding:8px; cursor:pointer;">
                                    ${company.kigyouMe} (${company.jigyousho})
                                </div>`;
                        });
                    }
                    $("#companyList").html(html).show();
                }
            });
        }, 300); // 300ms待機（リアルタイム感を出す）
    });

    // 候補をクリックしたとき
    $(document).on("click", ".company-item", function() {
        const id = $(this).data("id");
        const name = $(this).text().trim();
        
        $("#companySearch").val(name);
        $("#companyId").val(id);
        $("#companyList").hide();
    });

    // 入力欄以外をクリックしたら候補を閉じる
    $(document).click(function(e) {
        if (!$(e.target).closest("#companySearch, #companyList").length) {
            $("#companyList").hide();
        }
    });
});
</script>

</body>
</html>