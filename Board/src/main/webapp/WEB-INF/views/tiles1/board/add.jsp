<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%
	String ctxPath = request.getContextPath();
%>

<style type="text/css">


</style>

<script type="text/javascript">

	$(document).ready(function() {

		<%-- === #166. 스마트 에디터 구현 시작 === --%>
	       //전역변수
	       var obj = [];
	       
	       //스마트에디터 프레임생성
	       nhn.husky.EZCreator.createInIFrame({
	           oAppRef: obj,
	           elPlaceHolder: "content",
	           sSkinURI: "<%= ctxPath%>/resources/smarteditor/SmartEditor2Skin.html",
	           htParams : {
	               // 툴바 사용 여부 (true:사용/ false:사용하지 않음)
	               bUseToolbar : true,            
	               // 입력창 크기 조절바 사용 여부 (true:사용/ false:사용하지 않음)
	               bUseVerticalResizer : true,    
	               // 모드 탭(Editor | HTML | TEXT) 사용 여부 (true:사용/ false:사용하지 않음)
	               bUseModeChanger : true,
	           }
	       });
	       <%-- === 스마트 에디터 구현 끝 === --%>
		
		// 글쓰기 버튼 클릭 시 event 발생
		$("button#btnWrite").click(function() {
			// 유효성 검사 : 해당 하는 것들을 다 채우도록 해라.
			
		 <%-- === 스마트 에디터 구현 시작 === --%>
          	//id가 content인 textarea에 에디터에서 대입
           	obj.getById["content"].exec("UPDATE_CONTENTS_FIELD", []);
         <%-- === 스마트 에디터 구현 끝 === --%>
			
			// 글제목 유효성 검사
			const subject = $("input#subject").val().trim();
			if(subject == "") {
				alert("글제목을 입력하세요!!");
				return;
			}		
			
		<%--	
			// 글내용 유효성 검사(스마트에디터 사용 안할 때 사용한다.)
			const content = $("textarea#content").val().trim();
			if(content == "") {
				alert("글내용을 입력하세요!!");
				return;
			}		
		--%>
		
		<%-- === 스마트에디터 구현 시작 === --%>
        //스마트에디터 사용시 무의미하게 생기는 p태그 제거
         var contentval = $("textarea#content").val();
             
          // === 확인용 ===
          // alert(contentval); // content에 내용을 아무것도 입력치 않고 쓰기할 경우 알아보는것.
          // "<p>&nbsp;</p>" 이라고 나온다.
          
          // 스마트에디터 사용시 무의미하게 생기는 p태그 제거하기전에 먼저 유효성 검사를 하도록 한다.
          // 글내용 유효성 검사 
          if(contentval == "" || contentval == "<p>&nbsp;</p>") {
             alert("글내용을 입력하세요!!");
             return;
          }
          
          // 스마트에디터 사용시 무의미하게 생기는 p태그 제거하기
          contentval = $("textarea#content").val().replace(/<p><br><\/p>/gi, "<br>"); //<p><br></p> -> <br>로 변환
      	/*    
                  대상문자열.replace(/찾을 문자열/gi, "변경할 문자열");
          ==> 여기서 꼭 알아야 될 점은 나누기(/)표시안에 넣는 찾을 문자열의 따옴표는 없어야 한다는 점입니다. 
                            그리고 뒤의 gi는 다음을 의미합니다.

             g : 전체 모든 문자열을 변경 global
             i : 영문 대소문자를 무시, 모두 일치하는 패턴 검색 ignore
      	*/    
          contentval = contentval.replace(/<\/p><p>/gi, "<br>"); //</p><p> -> <br>로 변환  
          contentval = contentval.replace(/(<\/p><br>|<p><br>)/gi, "<br><br>"); //</p><br>, <p><br> -> <br><br>로 변환
          contentval = contentval.replace(/(<p>|<\/p>)/gi, ""); //<p> 또는 </p> 모두 제거시
      
          $("textarea#content").val(contentval);
       
          // alert(contentval);
      	<%-- === 스마트에디터 구현 끝 === --%>
		
			// 글암호 유효성 검사
			const pw = $("input#pw").val();	<%-- 글암호는 공백 입력이 가능하므로 .trim() 을 생략한다. --%>
			if(pw == "") {
				alert("글암호를 입력하세요!!");
				return;
			}		
			
			// form 을 전송(submit) 한다.
			const frm = document.addFrm;
			frm.method = "POST";
			frm.action = "<%= ctxPath%>/addEnd.action";
			frm.submit();
			
		});
		
	}); // end of $(document).ready(function() {}-----------------------------

</script>

<div style="display: flex;">
<div style="margin: auto; padding-left: 3%;">

<%-- <h2 style="margin-bottom: 30px;">글쓰기</h2> --%>

<%-- 원글쓰기인 경우 --%>
<c:if test="${requestScope.fk_seq eq ''}">
	<h2 style="margin-bottom: 30px;">글쓰기</h2>
</c:if>

<%-- 답변글 쓰기인 경우 --%>
<c:if test="${requestScope.fk_seq ne ''}">
	<h2 style="margin-bottom: 30px;">답변글쓰기</h2>
</c:if>

<!-- <form name="addFrm"> -->
<%-- === #149. 파일첨부하기 === 
     먼저 위의 <form name="addFrm"> 을 주석처리한 이후에 아래와 같이 해야한다.
   enctype="multipart/form-data" 를 해주어야만 파일첨부가 되어진다.     
--%>
<form name="addFrm" enctype="multipart/form-data">
<%-- 부트스트랩 사용 --%>
	<table style="width: 1024px" class="table table-bordered">
		<tr>
			<th style="width: 15%; background-color: #DDDDDD">성명</th>
			<td>	<%-- value 에서 name 은 getXXX (BoardVO) --%> <%-- name 에는 컬럼명을 쓴다. --%>
				<input type="hidden" name="fk_userid" value="${sessionScope.loginuser.userid}" readonly/>
				<input type="text" name="name" value="${sessionScope.loginuser.name}" readonly/>
			</td>
		</tr>
		
		<tr>
			<th style="width: 15%; background-color: #DDDDDD">제목</th>
			<td>
				<%-- 원글쓰기인 경우 --%>
				<c:if test="${requestScope.fk_seq eq ''}">
					<input type="text" name="subject" id="subject" size="100" />
				</c:if>
				
				<%-- 답변글쓰기인 경우 --%>
				<c:if test="${requestScope.fk_seq ne ''}">
					<input type="text" name="subject" id="subject" size="100" value="${requestScope.subject}" readonly />
				</c:if>
			</td>
		</tr>	
		
		<tr>
			<th style="width: 15%; background-color: #DDDDDD">내용</th>
			<td>	<%-- 15% 에서 남은 85% 를 다 쓴다는 것. (textarea 의 100%) --%>
				<textarea style="width: 100%; height: 612px;" name="content" id="content" ></textarea>
			</td>
		</tr>		
		
		<%--  === #150. 파일첨부 타입 추가하기 === --%>
		<tr>
			<th style="width: 15%; background-color: #DDDDDD">파일첨부</th>
			<td>
				<input type="file" name="attach" id="attach" />
			</td>
		</tr>	
		
		<tr>
			<th style="width: 15%; background-color: #DDDDDD">글암호</th>
			<td>
				<input type="password" name="pw" id="pw" />
			</td>
		</tr>							
	</table>
	
	<%-- === #143. 답변글쓰기가 추가된 경우 시작 === --%>
	<input type="hidden" name="fk_seq" value="${requestScope.fk_seq}" />
	<input type="hidden" name="groupno" value="${requestScope.groupno}" />
	<input type="hidden" name="depthno" value="${requestScope.depthno}" />
	<%-- === 답변글쓰기가 추가된 경우 끝 		=== --%>
	
	
	<%-- form 을 보내야 하므로 버튼을 만든다. --%>
	<div style="margin: 20px;">
		<button type="button" class="btn btn-secondary btn-sm mr-3" id="btnWrite">글쓰기</button>
		<button type="button" class="btn btn-secondary btn-sm" onclick="javascript:history.back()">취소</button>
	</div>
	
</form>
</div>
</div>