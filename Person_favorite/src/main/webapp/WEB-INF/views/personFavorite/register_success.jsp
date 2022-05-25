<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%
	String ctxPath = request.getContextPath();
    // favorite 
%>    
    
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>개인성향 입력한 결과가 성공인 경우에 보여주는 페이지</title>

<script type="text/javascript">

	// Function declaration
	function goSelect() {
		location.href="<%= ctxPath%>/personFavoriteSelectAll.sist";
	}// end of function goSelect()-------------------

</script>

</head>
<body>
	<h2>개인성향 입력 성공</h2>
	<br>
	<button onclick="goSelect()">입력결과 조회하기</button>
</body>
</html>