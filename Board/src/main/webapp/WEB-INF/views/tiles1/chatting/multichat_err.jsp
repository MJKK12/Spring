<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>


<!--  ==== #195. 웹채팅 관련5 (localhost 가 아니라 IP 를 정확하게 알아와야 한다.) -->

<script type="text/javascript">

//=== !!! WebSocket 통신 프로그래밍은 HTML5 표준으로써 자바스크립트로 작성하는 것이다. !!! === //
// WebSocket(웹소켓)은 웹 서버로 소켓을 연결한 후 데이터를 주고 받을 수 있도록 만든 HTML5 표준이다.
// 그런데 이러한 WebSocket(웹소켓)은 HTTP 프로토콜로 소켓 연결을 하기 때문에 웹 브라우저가 이 기능을 지원하지 않으면 사용할 수 없다. 
/*
   >> 소켓(Socket)이란? 
  - 어떤 통신프로그램이 네트워크상에서 데이터를 송수신할 수 있도록 연결해주는 연결점으로써 
    IP Address와 port 번호의 조합으로 이루어진다. 
        또한 어떤 하나의 통신프로그램은 하나의 소켓(Socket)만을 가지는 것이 아니라 
        동일한 프로토콜, 동일한 IP Address, 동일한 port 번호를 가지는 수십개 혹은 수만 개의 소켓(Socket)을 가질 수 있다.
	* 클라이언트는 채팅서버가 아니라 , 다른 기기
   	* 내 IP 는 192.168.0.4:9090
   =================================================================================================  
          클라이언트  소켓(Socket)                            			     서버  소켓(Socket)
      211.238.142.70:7942 ◎------------------------------------------◎  211.238.142.72:9090
      
           클라이언트는 서버인 211.238.142.72:9090 소켓으로 클라이언트 자신의 정보인 211.238.142.70:7942 을 
           보내어 연결을 시도하여 연결이 이루어지면 서버는 클라이언트의 소켓인 211.238.142.70:7942 으로 데이터를 보내면서 통신이 이루어진다.
    ================================================================================================== 
           
            소켓(Socket)은 데이터를 통신할 수 있도록 해주는 연결점이기 때문에 통신할 두 프로그램(Client, Server) 모두에 소켓이 생성되야 한다.

  	Server는 특정 포트와 연결된 소켓(Server 소켓)을 가지고 서버 컴퓨터 상에서 동작하게 되는데, 
  	이 Server는 소켓을 통해 Cilent측 소켓의 연결 요청이 있을 때까지 기다리고 있다(Listening 한다 라고도 표현함).
  	Client 소켓에서 연결요청을 하면(올바른 port로 들어왔을 때) Server 소켓이 허락을 하여 통신을 할 수 있도록 연결(connection)되는 것이다.
*/

	$(document).ready(function() {
		// 채팅을 하기 위해서는 webPage 가 떠야 한다. (Websocket 을 한다.)
		
		const url = window.location.host; // 웹브라우저의 주소창의 포트까지 가져온다.
		//	alert("url : " + url);
		//  결과값 url : 192.168.0.4:9090
		
		// 웹소켓 통신은 http:// 로 시작하는 것이 아니라, ws:// 로 시작한다.
		const pathname = window.location.pathname;// '/' 부터 오른쪽에 있는 모든 경로를 알아온다.
		//	alert("pathname : " + pathname);
		//	결과값 pathname : /board/chatting/multichat.action (view 단 페이지)
		
		//	ws://192.168.0.4:9090/chatting/multichatstart.action	// echoHandler 가 응답을 해주게 된다.
		
		const appCtx = pathname.substring(0,pathname.lastIndexOf("/"));		// 마지막에 오는 '/' 앞까지 자르도록 한다.
		// /board/chatting/ 만큼만 읽어온다.						// "전체 문자열".lastIndexOf("검사할 문자"); 
		//	alert("appCtx : " + appCtx);
		//	결과값 appCtx : /board/chatting
		
		const root = url + appCtx;
		//	alert("root : " + root);
		//	결과값 root : 192.168.0.4:9090/board/chatting 
		
		const wsUrl = "ws://"+root+"/multichatstart.action";		// 웹소켓 URL
		// 192.168.0.4:9090/board/chatting/multichatstart.action
		// 웹소켓 통신을 하기 위해서는 http:// 을 사용하는 것이 아니라 ws:// 를 사용해야 한다.
		// "/multichatstart.action" 에 대한 것은  websocketContext.xml 의 <websocket:mapping /> 에 path 에 나와있다.
		// /WEB-INF/spring/config/websocketContext.xml (경로 참고)
		
		const websocket = new WebSocket(wsUrl);
	//	즉, const websocket = new WebSocket("ws://192.168.0.4:9090/board/chatting/multichatstart.action"); 이다.
		
	// >> ====== !!중요!! Javascript WebSocket 이벤트 정리 ====== << //
	   /*   -------------------------------------
	                     이벤트 종류             설명
	        -------------------------------------
	           onopen        WebSocket 연결
	           onmessage     메시지 수신
	           onerror       전송 에러 발생
	           onclose       WebSocket 연결 해제
	   */   	
	   
	   let messageObj = {}; // 자바스크립트 객체를 생성한다.
	   
	   // === 웹소켓에 최초로 연결이 되었을 경우에 실행되는 콜백함수 정의 하기 === //
	   websocket.onopen = function() {
		alert("웹소켓 연결됨 !!");
	   	
	       	
		   
	   };
	   
	   
	   
	   
		
	});// end of $(document).ready(function() {})---------------------------------


</script>

<div class="container-fluid">
<div class="row">
<div class="col-md-10 offset-md-1">
   <div id="chatStatus"></div>
   <div class="my-3">
   - 상대방의 대화내용이 검정색으로 보이면 채팅에 참여한 모두에게 보여지는 것입니다.<br>
   - 상대방의 대화내용이 <span style="color: red;">붉은색</span>으로 보이면 나에게만 보여지는 1:1 귓속말 입니다.<br>
   - 1:1 채팅(귓속말)을 하시려면 예를 들어, 채팅시 보이는 172.30.1.45[이순신] ▶ ㅎㅎㅎ 에서 이순신을 클릭하시면 됩니다.
   </div>
   <input type="text" id="to" placeholder="귓속말대상IP주소"/>
   <br/>
   	♡ 귓속말대상 : <span id="privateWho" style="font-weight: bold; color: red;"></span>
   <br>
   <button type="button" id="btnAllDialog" class="btn btn-secondary btn-sm">귀속말대화끊기</button>
   <br><br>
   	현재접속자명단:<br/>
   <div id="connectingUserList" style=" max-height: 100px; overFlow: auto;"></div>
   
   <div id="chatMessage" style="max-height: 500px; overFlow: auto; margin: 20px 0;"></div>

   <input type="text"   id="message" class="form-control" placeholder="메시지 내용"/>
   <input type="button" id="btnSendMessage" class="btn btn-success btn-sm my-3" value="메시지보내기" />
   <input type="button" class="btn btn-danger btn-sm my-3 mx-3" onClick="javascript:location.href='<%=request.getContextPath() %>/index.action'" value="채팅방나가기" />
</div>
</div>
</div>

