<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>      

<%
	String ctxPath = request.getContextPath();
        // favorite
%>     
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>개인성향 모든 정보 출력 페이지</title>

<meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
<link rel="stylesheet" type="text/css" href="<%= ctxPath%>/resources/bootstrap-4.6.0-dist/css/bootstrap.min.css">

<style type="text/css">
	
	tbody > tr > td:nth-child(1) > span  { 
	   color: blue; 
	   display: none; 	
	}
	
	div.container > table > tbody > tr:hover {
		cursor: pointer;
	}

</style>

<script type="text/javascript" src="<%= ctxPath%>/resources/js/jquery-3.3.1.min.js"></script>
<script type="text/javascript" src="<%= ctxPath%>/resources/bootstrap-4.6.0-dist/js/bootstrap.bundle.min.js" ></script>

<script type="text/javascript">

	$(document).ready(function(){
		
		$("tbody > tr").click(function(){
		 // alert("호호");
		    
		    var $target = $(event.target); // <td>태그이다.
		 // console.log("확인용 $target.html() => " + $target.html() );
		   
		    var seq = $target.parent().find("span").text();
		 // console.log("확인용 seq => " + seq);
		    
		    location.href="personFavoriteDetail.sist?seq="+seq;
		});
		
	});

</script>

</head>
<body>
	<div class="container my-5">
		<h3>개인성향 모든 정보 출력 페이지</h3>
		
		<table class="table table-hover">
		    <thead>
		    	<tr>
		    		<th>성명</th>
		    		<th>학력</th>
		    		<th>색상</th>
		    		<th>음식</th>
		    		<th>등록일자</th>
		    	</tr>
		    </thead>
		    
		    <tbody>
		    	<c:if test="${not empty requestScope.list}">
		    		<c:forEach var="vo" items="${requestScope.list}"> 
		    			<tr>
		    				<td><span>${vo.seq}</span>${vo.name}</td>
		    				<td>${vo.school}</td>
		    				<td>${vo.color}</td>
		    				<td>${vo.food}</td>
		    				<td>${vo.registerday}</td>
		    			</tr>
		    		</c:forEach>
		    	</c:if>
		    	
		    	<c:if test="${empty requestScope.list}">
		    		<span style="color: red;">데이터가 존재하지 않습니다</span>
		    	</c:if>
		    </tbody>
		</table>   
	</div>
	
	<div style="width: 80%; margin: 0 auto;">
		<p class="text-center">
			<button type="button" class="btn btn-info" onclick="javascript:location.href='index.sist'">개인성향 입력페이지로 가기</button> 
		</p>
	</div>
</body>
</html>