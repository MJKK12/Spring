<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>특정 개인성향 정보삭제 성공 메시지 출력하기</title>
</head>
<body>
	<h3>${requestScope.delInfo}</h3>
	<br>
	<button type="button" onclick="javascript:location.href='personFavoriteSelectAll.sist'">개인성향 전체목록 보기</button> 
</body>
</html>