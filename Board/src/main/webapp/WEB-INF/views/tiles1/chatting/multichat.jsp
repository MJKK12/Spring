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
		
		// 채팅방 배경화면 색깔 바꾸기
		$("div#mycontent").css({"background-color":"#cce0ff"});	
		// div#mycontent 는  /Board/src/main/webapp/WEB-INF/tiles/layout/layout-tiles1.jsp 파일의 내용에 들어 있는 <div id="mycontent"> 이다.
		
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
	   // alert("웹소켓 연결됨 !!");
	   		$("div#chatStatus").text("정보: 웹소켓에 연결이 성공됨");   	
		/*	
	   		messageObj = {};	// 초기화
	   		messageObj.message = "채팅방에<span style='color:red;'> 입장</span> 했습니다.";		// JS 에서 key 값 (VO 에서 message 를 가져온다.)
	   		messageObj.type = "all";
	   		messageObj.to = "all";
	   	*/
	   	// 또는 (위와 같음)
	   	// key 값 : value 
	   	// 자바스크립트의 객체(아래에서 messageObj)와 자바의 객체(Message VO)는 서로 호환이 된다.
	   		messageObj = { message : "채팅방에<span style='color:red;'> 입장</span> 했습니다."
	   					 , type : "all"
	   					 , to : "all" };	// 자바스크립트에서 객체의 데이터값 초기화
	   		
	   		// messageObj 를 웹소켓에 보낸다.
	   		// JSON 모양이지만 String 타입으로 바꾼 것이다.
	   		websocket.send(JSON.stringify(messageObj));	// web 은 문자열로 바꿔서 보내야 함. (그냥 객체를 보낼 수 없기 때문이다. web 은 default 가 문자열.)
	   													// JSON.stringify(자바객체) 는 자바객체를 JSON 표기법의 문자열(string)로 변환한다
            /*
               	 JSON.stringify({});                  // '{}'						(문자열 ''로 바뀐다.)
		         JSON.stringify(true);                // 'true'
		         JSON.stringify('foo');               // '"foo"'
		         JSON.stringify([1, 'false', false]); // '[1,"false",false]'
		         JSON.stringify({ x: 5 });            // '{"x":5}'
            */           
            
	   };// end of websocket.onopen = function() {})-------------------------------
	   
	   // send 를 했으면 받아와야 한다. (발신이 됐으면 수신을 해오도록 하자.)
	   // === 메시지 수신 시 콜백함수 정의하기 === //
	   websocket.onmessage = function(event) {
	   		
		// event.data 는 수신된 메세지 이다. 즉, 지금은「김민정 이순신 엄정화 」 이다.      (↓ index 값 끝-1)
	          if(event.data.substr(0,1)=="「" && event.data.substr(event.data.length-1)=="」") { 
	             $("div#connectingUserList").html(event.data);
	          }
	          else {
	          // event.data 는 수신받은 채팅 문자이다.
	               $("div#chatMessage").append(event.data);
	               $("div#chatMessage").append("<br/>");
	               $("div#chatMessage").scrollTop(99999999);	// 메세지 보여준 후 스크롤을 맨 밑으로 보낸다.
	          }       	   
	   };
	   
	   
	   	// === 웹소켓 연결해제시 콜백함수 정의하기 === //
	   websocket.onclose = function () {
		// 채팅방나가기 버튼 클릭 시 연결 해제되도록 한다.
		// WebsocketEchoHandler 에서 응답해서 연결이 해제되도록 하면 된다.
		// === 클라이언트가 웹소켓서버와의 연결을 끊을때 작업 처리하기 ===
		// onclick() 을 클릭하면 아예 url 이 바뀌게 되는 것임. 이때 WebsocketEchoHandler 의 afterConnectionClosed 메소드가 응답하도록 한다.
		// 접속된 명단에서 다시 연결을 끊은 사용자 이름을 없애줘야 한다. 
		
		
	   };	   
	  	
	 	// === 메시지 입력 후 엔터하기 === //
		$("input#message").keyup(function(key){
			if(key.keyCode == 13) {
				// 엔터를 쳤을 때 input#btnSendMessage 부분을 클릭해라.
				$("input#btnSendMessage").click();
			}
		});
	   
	   
	   	// === 메시지 보내기 === //
		let isOnlyOneDialog = false;	// 귓속말 여부. true 이면 귓속말, false 이면 모두에게 공개되는 말이다.
		$("input#btnSendMessage").click(function() {
			// 메시지보내기 버튼을 클릭하면 함수를 실행한다.
			
			if( $("input#message").val() != "" ) {
				// 메시지 내용을 입력했을 때
				// ==== 자바스크립트에서 replace를 replaceAll 처럼 사용하기 ====
                // 자바스크립트에서 replaceAll 은 없다.
                // 정규식을 이용하여 대상 문자열에서 모든 부분을 수정해 줄 수 있다.
                // 수정할 부분의 앞뒤에 슬래시를 하고 뒤에 gi 를 붙이면 replaceAll 과 같은 결과를 볼 수 있다.		
                let messageVal = $("input#message").val();
                messageVal = messageVal.replace(/<script/gi, "&lt;script");	// 내용물에 () 안의 내용이 있다면 바꾸도록 한다. (let 변수)
				// 스크립트 공격을 막으려고 한 것이다.
				
				// 항상 script 에서 객체로 만든 다음에 바꿔주도록 한다. (messageVO 를 참고한다.)
				<%--
				messageObj = {message : messageVal		// input 태그에 사용자가 입력한 것.
                			, type : "all",
                			, to : "all"};
				--%>                
                // 또는
				// 항상 script 에서 객체로 만든 다음에 바꿔주도록 한다. (messageVO 를 참고한다.)
				messageObj = {};               		// 자바스크립트 객체 생성함.
				messageObj.message = messageVal;	
				messageObj.type = "all";			
				messageObj.to = "all";				
				
				const to = $("input#to").val();
				if( to != "" ) {
					messageObj.type = "one";			
					messageObj.to = to;			// 특정한 getId 에 보내주겠다.				
				}
				
				websocket.send(JSON.stringify(messageObj));	// 이제 messageObj를 websocket에 보내도록 하자.
				// JSON.stringify() 는 값을 그 값을 나타내는 JSON 표기법의 문자열로 변환한다.
				
				// 위에서 자신이 보낸 메시지를 웹소켓을 보낸 다음에 자신이 보낸 메시지 내용을 웹페이지에 보여지도록 한다.
                const now = new Date();
                let ampm = "오전 ";					// 아래의 오전, 오후에서 바뀌므로 let 로 한다.
                let hours = now.getHours();
                
                if(hours > 12) {					// 24시간 체제, 12시보다 클 경우 "오후"
                   hours = hours - 12;				// ex. 14-12 = 2시 (오후)
                   ampm = "오후 ";
                }
                
                if(hours == 0) {
                   hours = 12;
                }
                
                if(hours == 12) {
                   ampm = "오후 ";
                }
                
                let minutes = now.getMinutes();
              	if(minutes < 10) {
                   minutes = "0"+minutes;
              	}
              
                const currentTime = ampm + hours + ":" + minutes; 
				
                if(isOnlyOneDialog == false) {		// 귓속말이 아닌 경우 // 밑으로 계속 대화내용이 쌓이기 때문에 append 를 사용한다. // 내가 쓴글은 오른쪽 정렬, 상대방은 왼쪽 정렬
                	$("div#chatMessage").append("<div style='background-color: #ffff80; display: inline-block; max-width: 60%; float: right; padding: 7px; border-radius: 15%; word-break: break-all;'>" + messageVal + "</div> <div style='display: inline-block; float: right; padding: 20px 5px 0 0; font-size: 7pt;'>"+currentTime+"</div> <div style='clear: both;'>&nbsp;</div>");
                 																																							/* word-break: break-all; 은 공백없이 영어로만 되어질 경우 해당구역을 빠져나가므로 이것을 막기위해서 사용한다. */
                }
                else {	// 귓속말인 경우 (비공개 대화, isOnlyOneDialog == true), 글자색을 빨간색으로 한다. // 밑으로 계속 대화내용이 쌓이기 때문에 append 를 사용한다. // 내가 쓴글은 오른쪽 정렬, 상대방은 왼쪽 정렬
                	$("div#chatMessage").append("<div style='background-color: #ffff80; display: inline-block; max-width: 60%; float: right; padding: 7px; border-radius: 15%; word-break: break-all; color: red;'>" + messageVal + "</div> <div style='display: inline-block; float: right; padding: 20px 5px 0 0; font-size: 7pt;'>"+currentTime+"</div> <div style='clear: both;'>&nbsp;</div>");	
																																											/* word-break: break-all; 은 공백없이 영어로만 되어질 경우 해당구역을 빠져나가므로 이것을 막기위해서 사용한다. */              	
				}
                
                $("div#chatMessage").scrollTop(999999999);
                
                $("input#message").val("");
                $("input#message").focus();
			}
			
			
		});
		// websocket.send 를 했으므로 응답을 해줘야 한다. (웹소켓에코핸들러.java 클래스에서 응답한다.)
		////////////////////////////////////////////////////////////////////////////
		
		// 귀속말 대화끊기 클릭 시, 모든 사람과 대화하겠다는 말이다.
		// 귀속말 대화끊기 버튼은 처음에는 보이지 않도록 한다.
		$("button#btnAllDialog").hide();
		
		
		// 아래는 귓속말(1:1 대화)을 위해서 대화를 나누는 상대방의 이름을 클릭하면 상대방이름의 웹소켓id 를 알아와서 input태그인 귓속말대상웹소켓.getId()에 입력하도록 하는 것.	
		$(document).on("click", ".loginuserName", function(){	// java 단에서 class 에 있는 이름 불러오기
			/* class loginuserName 은 
		       com.spring.chatting.websockethandler.WebsocketEchoHandler 의 
		       public void handleTextMessage(WebSocketSession wsession, TextMessage message) 메소드내에
		       191번 라인에 기재해두었음.
	     	*/	
	     	
	     	const ws_id = $(this).prev().text();				// <span style='font-weight:bold; cursor:pointer;' class='loginuserName'>" +loginuser.getName()+ "</span> 의 앞에있는
	     //	alert(ws_id);										// <span style=''>"+wsession.getId()+"</span> 를 말한다.
			$("input#to").val(ws_id);
	     	
	     	$("span#privateWho").text($(this).text());			// ♡ 귓속말대상 : 이순신 (이런식으로 값이 들어가게 된다.)
			$("button#btnAllDialog").show();					// 메시지 끊기 버튼을 보여준다.
			$("input#message").css({'background-color':'black', 'color':'white'});
			$("input#message").attr("placeholder", "귓속말 메시지 내용");		// 속성값
			
			isOnlyOneDialog = true;		// 귓속말 대화 (1:1, 비밀대화)임을 지정한다.
			
		});	
		
		// 귓속말 대화 끊기 버튼을 클릭한 경우에는 전체 대상으로 채팅하겠다는 말이다. (비밀대화가 X)
		$("button#btnAllDialog").click(function(){
			$("input#to").val("");			// to 값을 없앤다.
			$("span#privateWho").text("");	// ♡귓속말대상 :___ 뒤의 이름을 없앤다.
			$("input#message").css({'background-color':'', 'color':''});		// css 를 원래대로 원상복구 한다.
			$("input#message").attr("placeholder", "메시지 내용");					// placeholder 를 원상복구 한다. "" 로 써도 원래대로 돌아감.
			$(this).hide();					// 자신(this)인 $("button#btnAllDialog") 을 hide()한다.			
			
			isOnlyOneDialog = false;	// 전체대화임을 알려줘야 한다. (귓속말이 아닌 모두에게 공개되는 대화임을 지정한다.)
		});


		
	   
	});// end of $(document).ready(function() {})---------------------------------


</script>

<div class="container-fluid">
<div class="row">
<div class="col-md-10 offset-md-1">
   <div id="chatStatus"></div>
   <div class="my-3">
   - 상대방의 대화내용이 검정색으로 보이면 채팅에 참여한 모두에게 보여지는 것입니다.<br>
   - 상대방의 대화내용이 <span style="color: red;">붉은색</span>으로 보이면 나에게만 보여지는 1:1 귓속말 입니다.<br>
   - 1:1 채팅(귓속말)을 하시려면 예를 들어, 채팅시 보이는 [이순신]대화내용 에서 이순신을 클릭하시면 됩니다.
   </div>
   <input type="hidden" id="to" placeholder="귓속말대상웹소켓.getId()"/>
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