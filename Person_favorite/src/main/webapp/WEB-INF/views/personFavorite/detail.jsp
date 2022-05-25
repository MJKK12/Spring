<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>    

<%
	String ctxPath = request.getContextPath();
    //     /favorite
%> 
    
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>개별 회원의 성향 결과</title>

<meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
<link rel="stylesheet" type="text/css" href="<%= ctxPath%>/resources/bootstrap-4.6.0-dist/css/bootstrap.min.css">

<style type="text/css">

	span#colorbox {
		display: inline-block;
		width: 30px;
		height: 30px;
		margin-left: 20px;
		position: relative;
		top: 8px;
		background-color: ${requestScope.map.vo.color};
	} 

</style>

<script type="text/javascript" src="<%= ctxPath%>/resources/js/jquery-3.3.1.min.js"></script>
<script type="text/javascript" src="<%= ctxPath%>/resources/bootstrap-4.6.0-dist/js/bootstrap.bundle.min.js" ></script> 

<script type="text/javascript">

	$(document).ready(function(){
		$("span#colorbox").css('background-color','${requestScope.psdto.color}');
	});

	
	// Function declaration
	function deletePersonFavorite() {
		
		var bool = confirm("정말로 삭제하시겠습니까?");
		
		if(bool) {
			var frm = document.delFrm;
		//	console.log("확인용 삭제할 회원번호 seq => " + frm.seq.value);
			
		    frm.action = "personFavoriteDelete.sist";
		    frm.method = "post";
		    frm.submit();
		}
	}
	
	
	function updatePersonFavorite(seq) {
		
		var frm = document.updateFrm;
		
		frm.action = "personFavoriteUpdate.sist";
		frm.method = "post";
		frm.submit();
	}
	
</script> 

</head>

<body class="py-3">
  <div class="container">
	
	<div class="card">
	  <div class="card-header"> <%-- .card-header에는 카드 머리말이 들어갑니다. --%>
	    <p class="h2 text-center">
	    	<span class="text-primary">${requestScope.map.vo.name}</span>&nbsp;<small>님의 개인성향 결과</small>
	    </p>
	  </div>
	  
	  <div class="card-body"> <%-- .card-body에는 카드 내용이 들어갑니다. --%>
		<div class="row mx-4 my-3 border border-top-0 border-left-0 border-right-0">
		   	 <div class="col-md-3 py-2">회원번호</div>
		  	 <div class="col-md-9 py-2"><span class="h5">${requestScope.map.vo.seq}</span></div>
		</div>
		
		<div class="row mx-4 my-3 border border-top-0 border-left-0 border-right-0">
		   	 <div class="col-md-3 py-2">성명</div>
		  	 <div class="col-md-9 py-2"><span class="h5">${requestScope.map.vo.name}</span></div>
		</div>
		
		<div class="row mx-4 my-3 border border-top-0 border-left-0 border-right-0">
		   	 <div class="col-md-3 py-2">학력</div>
		  	 <div class="col-md-9 py-2"><span class="h5">${requestScope.map.vo.school}</span></div>
		</div>
		
		<div class="row mx-4 my-3 border border-top-0 border-left-0 border-right-0">
		   	 <div class="col-md-3 py-2">색상</div>
		  	 <div class="col-md-9 py-2">
		  	 <c:choose>
				<c:when test="${requestScope.map.vo.color eq 'red'}"><span class="h5">빨강</span></c:when> 
				<c:when test="${requestScope.map.vo.color eq 'blue'}"><span class="h5">파랑</span></c:when>
				<c:when test="${requestScope.map.vo.color eq 'green'}"><span class="h5">초록</span></c:when>
				<c:when test="${requestScope.map.vo.color eq 'yellow'}"><span class="h5">노랑</span></c:when> 
				<c:otherwise><span class="h5">기타</span></c:otherwise>
			 </c:choose> 
			 <span id="colorbox" class="rounded-circle"></span>
		  	 </div>
		</div>
		
		<div class="row mx-4 my-3 border border-top-0 border-left-0 border-right-0">
		   	 <div class="col-md-3 py-2">음식</div>
		  	 <div class="col-md-9 py-2"><span class="h5">${requestScope.map.vo.food}</span></div>
		</div>
		
		<div class="row mx-4 my-3 border border-top-0 border-left-0 border-right-0">
		   	 <div class="col-md-3 py-2">음식이미지 파일명</div>
		  	 <div class="col-md-9 py-2"><span class="h5">${requestScope.map.foodImgFileName}</span></div>
		</div>
		
		<div class="row mx-4 my-3 border border-top-0 border-left-0 border-right-0">
		   	 <div class="col-md-3 py-2">음식사진</div>
		  	 <div class="col-md-9 py-2">
			  	 <c:if test="${not empty requestScope.map.foodImgFileName}">
				    <c:forTokens var="imgFileName" items="${requestScope.map.foodImgFileName}" delims=",">  
				    	<img src="<%= request.getContextPath()%>/resources/images/${imgFileName}" class="img-fluid rounded mr-1" width="130" /> 
				    </c:forTokens>
			     </c:if>
			    
			     <c:if test="${empty requestScope.map.foodImgFileName}">
			    	<span class="h5">좋아하는 음식이 없습니다.</span>
			     </c:if>
		  	 </div>
		</div>
	  </div>
	  
	  <div class="card-footer"> <!-- .card-footer에는 카드 꼬리말이 들어갑니다. -->
		 <div class="row" style="position: relative; top:10px;">	
		    <div class="col-md-3">
		       <p class="text-center"><a class="btn btn-info" href="personFavoriteSelectAll.sist" role="button">목록보기1</a></p>
		    </div>
		    <div class="col-md-3">
		       <p class="text-center"><button type="button" class="btn btn-success" onclick="javascript:location.href='personFavoriteSelectAll.sist'">목록보기2</button></p> 
		    </div>
		    <div class="col-md-3">
		       <p class="text-center"><button type="button" class="btn btn-danger"  onclick="deletePersonFavorite()">삭제하기</button></p>
		    </div>
		    <div class="col-md-3">
		       <p class="text-center"><button type="button" class="btn btn-primary" onclick="updatePersonFavorite('${requestScope.map.vo.seq}')">내정보수정하기</button></p> 
		    </div>
		 </div>
	  </div>
	
	</div>

 </div>	
  
  <%-- POST 방식을 통해 회원을 삭제하기 위하여 화면에는 보이지 않는 form 태그를 만든다. --%>
  <form name="delFrm">
  	<input type="hidden" name="seq" value="${requestScope.map.vo.seq}"/>
  	<input type="hidden" name="name" value="${requestScope.map.vo.name}"/>
  </form>	
  
  <form name="updateFrm">
  	<input type="hidden" name="seq" value="${requestScope.map.vo.seq}"/>
  </form>
	
</body>
</html>





