<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>投稿検索</title>
<style>
  body {
    margin: 0;
    padding: 20px;
    font-family: sans-serif;
  }
  .search-bar {
    display: flex;
    align-items: center;
    gap: 12px;
  }
  .back-btn {
    background: none;
    border: none;
    font-size: 40px;
    line-height: 1;
    cursor: pointer;
    padding: 0;
  }
  .search-box {
    flex: 1;
    height: 40px;
    padding: 0 12px;
    font-size: 16px;
    border: 1px solid #000;
    border-radius: 6px;
    box-sizing: border-box;
  }
</style>
</head>
<body>

	<div class="search-bar">
		<!-- 戻る -->
		 <button type="button" class="back-btn" onclick="history.back();">&larr;</button>
		
	</div>
</body>
</html>