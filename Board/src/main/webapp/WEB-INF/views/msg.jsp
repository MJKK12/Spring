<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>


<script type="text/javascript">

	alert("${requestScope.message}");			// 메시지 출력하기
	location.href = "${requestScope.loc}"; 		// JS 에서 페이지 이동 (해당 페이지로 가라는 뜻)

	 // Action에서 수정이 이루어진 후 메세지를 띄운다.!  수정이 완료되고 나서 팝업창이 닫혀야 한다. --> msg.jsp에서 self.close() 추가    
	 // 수정이 된 후 session 값이 바뀐 것이다. 그러나 수정된 후 팝업창이 닫히고 새로고침을 하지 않으면 이름이 자동으로 update 가 되지 않는다.
	 // 따라서 수정 후 팝업창이 닫히고 홈페이지에서 바로 바뀐 이름으로 자동 새로고침 적용되어야 한다.
	
	self.close();	// 팝업창 닫기
	opener.location.reload(true);	// 부모창 새로고침
//	opener.history.go(0);			// 부모창 새로고침
		
	/*   
       location.href="javascript:history.go(-2);";  // 이전이전 페이지로 이동 
       location.href="javascript:history.go(-1);";  // 이전 페이지로 이동
       location.href="javascript:history.go(0);";   // 현재 페이지로 이동(==새로고침) 캐시에서 읽어옴.
       location.href="javascript:history.go(1);";   // 다음 페이지로 이동.
       
       location.href="javascript:history.back();";       // 이전 페이지로 이동 
       location.href="javascript:location.reload(true)"; // 현재 페이지로 이동(==새로고침) 서버에 가서 다시 읽어옴. 
       location.href="javascript:history.forward();";    // 다음 페이지로 이동.
   */
   
</script>