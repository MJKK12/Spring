package com.spring.board.common;

import javax.servlet.http.HttpServletRequest;

public class MyUtil {

	// *** '?' 다음의 데이터까지 포함한 현재 URL 주소를 알려주는 메소드를 생성 *** //	
	public static String getCurrnetURL(HttpServletRequest request) {
		
	// 만약 웹브라우저 주소 입력란(주소창)에 아래와 같이 입력되었다면(searchType 은 name 이고, '유'로 검색한 것)
	//	http://localhost:9090/MyMVC/member/memberList.up?currentShowPageNo=3&sizePerPage=10&searchType=name&searchword=유
		
		String currentURL = request.getRequestURL().toString();	// 즉, memberList.up? 뒤에 와야할 데이터는 불러오지 않는다.
		// 		http://localhost:9090/MyMVC/member/memberList.up
		
		String queryString = request.getQueryString();	// getQueryString 은 memberList.up? 뒤에 와야할 데이터를 넣는다.
		// 	    currentShowPageNo=3&sizePerPage=10&searchType=name&searchword=유 (GET 방식)	// POST 방식은 물음표 X
		// 		null 	(POST 방식일 경우)
		
		if(queryString != null) {	// GET 방식일 경우 
			currentURL += "?" + queryString;	// currentURL 이 바뀐다. 즉 currentURL 의 '?' 뒤의 데이터를 붙여주는 것이다.
		//	http://localhost:9090/MyMVC/member/memberList.up?currentShowPageNo=3&sizePerPage=10&searchType=name&searchword=유						
		}
		
		String ctxPath = request.getContextPath();
		//	   /MyMVC	
		
		int beginIndex = currentURL.indexOf(ctxPath) + ctxPath.length();	// ctxPath 가 시작하는 값(index)
		//	27		   =		21(http://localhost:9090/) + 6 (MyMVC/)
		
		currentURL = currentURL.substring(beginIndex);
		//	/member/memberList.up?currentShowPageNo=3&sizePerPage=10&searchType=name&searchword=유	
		
		return currentURL;
	}
}
