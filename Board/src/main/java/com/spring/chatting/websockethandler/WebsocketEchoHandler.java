package com.spring.chatting.websockethandler;

import java.util.*;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.spring.board.model.MemberVO;

// ==== #198. 웹채팅 관련8 ==== //

public class WebsocketEchoHandler extends TextWebSocketHandler {

	// === 웹소켓 서버에 연결한 클라이언트 사용자들을 저장하는 리스트 === //
	// 배열은 크기가 한정되어 있으므로, 무한대의 크기를 가진 리스트를 사용해야 한다. (클라이언트 기록 저장하는 리스트를 한정해놓으면 안된다.)
	private List<WebSocketSession> connectedUsers = new ArrayList<>();	// list 에 차곡차곡 쌓아두도록 한다.
		
	
	// extends TextWebSocketHandler 를 통해 TextWebSocketHandler과 관련된 것들을 상속받은 것이다.
	public void init() throws Exception {}
	// 시작(init) 메소드(method) ==> init-method
	// websocketContext.xml 에서 init-method 참고 (#196번)
	// 일단 껍데기만 만들어 놓은 것이다.
	
	// === 클라이언트가 웹소켓서버에 연결했을때의 작업 처리하기 ===
    /*
       afterConnectionEstablished(WebSocketSession wsession) 메소드는 
              클라이언트가 웹소켓서버에 연결이 되어지면 자동으로 실행되는 메소드로서
       WebSocket 연결이 열리고 사용이 준비될 때 호출되어지는(실행되어지는) 메소드이다.
    */
	
	// 응답해주도록 한다. (afterConnectionEstablished 연결이 된 후에)
	@Override
	public void afterConnectionEstablished(WebSocketSession wsession) throws Exception {
		// TextWebSocketHandler 에 웹소켓통신을 위해 만들어져 있는 것이다.
		// 만들어져 있는 것을 내가 재정의(@override) 해서 사용하면 되는 것이다.
		// >>> 파라미터 WebSocketSession wsession 은 웹소켓 서버에 접속한 클라이언트 사용자이다.
		// 클라이언트 사용자는 한마디로 다른 기기라고 생각하면 된다. (다른 휴대폰 or 컴퓨터)
		
		// 웹소켓서버에 접속한 클라이언트의 IP Address 얻어오기 ( 누가 접속했는지 한번 보도록 하자. )
        /*
          STS 메뉴의 
          Run --> Run Configuration 
              --> Arguments 탭
              --> VM arguments 속에 맨 뒤에
              --> 한칸 띄우고 -Djava.net.preferIPv4Stack=true 
                          을 추가한다.  
        */
		
	//  System.out.println("====> 웹채팅확인용 : " + wsession.getId() + "님이 접속했습니다.");
        // ====> 웹채팅확인용 : 0님이 접속했습니다. 
        // ====> 웹채팅확인용 : 1님이 접속했습니다.
        // wsession.getId() 는 자동증가되는 고유한 숫자로 나옴.
        
	//  System.out.println("====> 웹채팅확인용 : " + "연결 컴퓨터명 : " + wsession.getRemoteAddress().getHostName());
        // ====> 웹채팅확인용 : 연결 컴퓨터명 : DESKTOP-8UUB40D
        
    //  System.out.println("====> 웹채팅확인용 : " + "연결 컴퓨터명 : " + wsession.getRemoteAddress().getAddress().getHostName());
        // ====> 웹채팅확인용 : 연결 컴퓨터명 : DESKTOP-8UUB40D
        
    //  System.out.println("====> 웹채팅확인용 : " + "연결 IP : " + wsession.getRemoteAddress().getAddress().getHostAddress()); 
        // ====> 웹채팅확인용 : 연결 IP : 192.168.0.4	
		
		// 들어온 클라이언트가 누구인지 저장을 해두어야 한다. (위에서 list 에 저장되도록 필드를 만들어 두었다.)
		connectedUsers.add(wsession);	// 클라이언트인 wsession 을 List 에 차곡차곡 저장해두자.
		
		// 로그인한 사람의 이름을 알아와야 한다.
		// WebSocketSession 은 우리가 평소에 쓰던 HttpSession 이 아니다. (원래는 HttpSession 에 저장되어 있는 것이다.)
		// 로그인한 사용자의 HttpSession 에 저장된 기록을 가져와야 한다.
		
		///// ===== 웹소켓 서버에 접속시 접속자 명단(이름)을 알려주기 위한 것 시작 ===== /////
        // Spring에서 WebSocket 사용시 먼저 HttpSession에 저장된 값들을 읽어와서 사용하기
		/*
           	먼저 /webapp/WEB-INF/spring/config/websocketContext.xml 파일에서 (환경설정 파일로 이동한다.)
          	websocket:handlers 태그안에 websocket:handshake-interceptors에
            HttpSessionHandshakeInterceptor를 추가하면 WebSocketHandler 클래스를 사용하기 전에 
                        먼저 HttpSession에 저장되어진 값들을 읽어 들여 WebSocketHandler 클래스에서 사용할 수 있도록 처리해준다. 
			*interceptors : HttpSession 에서 먼저 가로채서 저장한 값을 읽어서, (aop 의 before, advice 와 흡사하다. 주업무를 먼저 실행해준다.)
							WebSocketHandler에서 사용할 수 있도록 처리
							즉, com.spring.chatting.websockethandler.WebsocketEchoHandler 에 가기 전에 HttpSession 에서 가로채서 그 session 을
							읽어올 수 있도록 한다.
		*/
		
		String connectingUserName = "「";	// 자음'ㄴ' 의 특수문자
        // connectedUsers 는 웹소켓session 이다. 쌓인 wsession 을 connectedUsers 에서 하나씩 꺼낸다.
        for (WebSocketSession webSocketSession : connectedUsers) {
            Map<String, Object> map = webSocketSession.getAttributes();	// httpSession 에 있는 값들을 get 해온다.
            // 받아온 value 값이 어떤 타입인지 모르기 때문에 object 로 받아온 것이다. 
            /*
               webSocketSession.getAttributes(); 은 
               HttpSession에 setAttribute("키",오브젝트); 되어 저장되어진 값들을 읽어오는 것으로써,
               										     리턴값은  "키",오브젝트로 이루어진 Map<String, Object> 으로 받아온다.
            */ 
           MemberVO loginuser = (MemberVO)map.get("loginuser");  // Object 타입에서 MemberVO 타입으로 casting 했다.
           // "loginuser" 은 HttpSession에 저장된 키 값으로 로그인 되어진 사용자이다.
            
           connectingUserName += loginuser.getName()+" "; 
        }// end of for--------------------
        
        connectingUserName += "」";
		
    //  System.out.println("확인용 connectingUserName : " + connectingUserName);
	//	확인용 connectingUserName : 「김민정 이순신 엄정화 」	
		
        for(WebSocketSession webSocketSession : connectedUsers) {
        	// 각각의 webSocketSession 마다 connectedUsers 를 보낸다.
        	webSocketSession.sendMessage(new TextMessage(connectingUserName));	// 접속자 명단인 connectingUserName 을 보내주도록 한다.
        								// String 타입은 new TextMessage() 로 바꿔서 보내줘야 한다.     	
        }// end of for-------------------------------------
		
		///// ===== 웹소켓 서버에 접속시 접속자 명단(이름)을 알려주기 위한 것 끝 ===== /////

	}
	
	// === 클라이언트가 웹소켓 서버로 메시지를 보냈을때의 Send 이벤트를 처리하기 ===
    /*
       handleTextMessage(WebSocketSession wsession, TextMessage message) 메소드는 
                 클라이언트가 웹소켓서버로 메시지를 전송했을 때 자동으로 호출되는(실행되는) 메소드이다.
                 첫번째 파라미터  WebSocketSession 은  메시지를 보낸 클라이언트임.
               두번째 파라미터  TextMessage 은  메시지의 내용임.
     */	
	@Override
	public void handleTextMessage(WebSocketSession wsession, TextMessage message) throws Exception {
		
		// >>> 파라미터 WebSocketSession wsession은  웹소켓서버에 접속한 클라이언트임. <<<
        // >>> 파라미터 TextMessage message 은  클라이언트 사용자가 웹소켓서버로 보낸 웹소켓 메시지임. <<<
        
        // Spring에서 WebSocket 사용시 먼저 HttpSession에 저장된 값들을 읽어와서 사용하기
        /*
                     먼저 /webapp/WEB-INF/spring/config/websocketContext.xml 파일에서
           websocket:handlers 태그안에 websocket:handshake-interceptors에
           HttpSessionHandshakeInterceptor를 추가해주면 
           WebSocketHandler 클래스를 사용하기 전에, 
                      먼저 HttpSession에 저장되어진 값들을 읽어 들여, WebSocketHandler 클래스에서 사용할 수 있도록 처리해준다. 
        */ 
		
		// Intercept 해왔으므로 session 을 가져올 수 있다. (HttpSession) // websocketContext.xml 참고
		Map<String, Object> map = wsession.getAttributes();
        MemberVO loginuser = (MemberVO)map.get("loginuser");  
        // "loginuser" 은 HttpSession에 저장된 키 값으로 로그인 되어진 사용자이다.
        
//      System.out.println("====> 웹채팅확인용 : 로그인ID : " + loginuser.getUserid());
        // ====> 웹채팅확인용 : 로그인ID : kimmj
        
        // Java 에서는 VO를 써야 한다. (문자열을 객체로 바꿔줘야 한다.) --> MemberVO클래스의 convertMessage(String source) 참고
        // pom.xml 의 #192번에서 String 을 자바 객체로 변환가능하게 해주는 라이브러리를 참고한다.
        // gson이 꼭 필요하다.
        MessageVO messageVO = MessageVO.convertMessage(message.getPayload());		// TextMessage 는 String 이 아니기 때문에 바꿔줘야한다. --> TextMessage 를 String 으로 바꿨다.
        // message.getPayload() return 타입을 MessageVO 로 한다.       
        /* 
		        파라미터 message 는  클라이언트 사용자가 웹소켓서버로 보낸 웹소켓 메시지임
			message.getPayload() 은  클라이언트 사용자가 보낸 웹소켓 메시지를 String 타입으로 바꾸어주는 것이다.
			/Board/src/main/webapp/WEB-INF/views/tiles1/chatting/multichat.jsp 파일에서 
		       클라이언트가 보내준 메시지는 JSON 형태를 뛴 문자열(String) 이므로 이 문자열을 Gson을 사용하여 MessageVO 형태의 객체로 변환시켜서 가져온다.
       */
        
         //  System.out.println("~~~~ 확인용 messagVO.getMessage() => " + messageVO.getMessage());
         // ~~~~ 확인용 messageVO.getMessage() => 채팅방에 <span style='color: red;'>입장</span>했습니다
        
         //  System.out.println("~~~~ 확인용 messagVO.getType() => " + messageVO.getType());
         // ~~~~ 확인용 messageVO.getType() => all 또는 one
        
         //  System.out.println("~~~~ 확인용 messagVO.getTo() => " + messageVO.getTo());
         // ~~~~ 확인용 messageVO.getTo() => all 또는 1a(getId()값)
                
        Date now = new Date(); // 현재시각 
        String currentTime = String.format("%tp %tl:%tM",now,now,now); 
        // %tp              오전, 오후를 출력 
        // %tl              시간을 1~12 으로 출력 (12시간 체제)
        // %tM              분을 00~59 으로 출력
        
        for(WebSocketSession webSocketSession : connectedUsers) {
        	// 채팅서버에 들어온 모든 사람들(connectedUsers)을 webSocketSession 에 담는다.
        	
        	if("all".equals(messageVO.getType())) {
        		// 넘어온 messageVO.getType() 값이 all 이라면,
        		// 채팅할 대상이 "전체" 일 경우
                // 메시지를 ***자기자신을 뺀*** 나머지 모든 사용자들에게 메시지를 보냄.
        		// wsession 가 메세지를 보낸이(나), webSocketSession 가 채팅서버에 접속한 모든 클라이언트
        		if(!wsession.getId().equals(webSocketSession.getId())) {
        			// 나를 뺀 나머지 일때만, 메세지를 보내겠다.
        			// wsession 은 메시지를 보낸 클라이언트임.
                    // webSocketSession 은 웹소켓서버에 연결된 모든 클라이언트중 하나임.
                    // wsession.getId() 와  webSocketSession.getId() 는 자동증가되는 고유한 값으로 나옴 
        			// 웹소켓에 있는 사람이 누구인지 구분할 고유한 값인 id가 반드시 필요하다. (.getId 같은 경우 고유하다.)
        			webSocketSession.sendMessage(
        					new TextMessage("<span style='display:none'>"+wsession.getId()+"</span>&nbsp;[<span style='font-weight:bold; cursor:pointer;' class='loginuserName'>" +loginuser.getName()+ "</span>]<br><div style='background-color: white; display: inline-block; max-width: 60%; padding: 7px; border-radius: 15%; word-break: break-all;'>"+ messageVO.getMessage() +"</div> <div style='display: inline-block; padding: 20px 0 0 5px; font-size: 7pt;'>"+currentTime+"</div> <div>&nbsp;</div>" )
        					);	
        		}
        		
        	}
        	
        	else {
        		// jsp 에서 .send() 를 했으면 응답을 해줘야 한다.
				// 넘어온 messageVO.getType() 값이 "all" 이 아니라면,
        		// 채팅할 대상이 "전체"가 아닌 특정대상(귓속말 대상 웹소켓.getId()임)일 경우 
                String ws_id = webSocketSession.getId();	// 웹소켓 아이디
                			// webSocketSession 은 웹소켓서버에 연결한 모든 클라이언트중 하나이며, 그 클라이언트의 웹소켓의 고유한 id 값을 알아오는 것임.  
        		
                if(messageVO.getTo().equals(ws_id)) {
                	// messageVO.getTo() : 특정 클라이언트의 아이디
                	// messageVO.getTo() 는 클라이언트가 보내온 [ 귓속말대상웹소켓.getId() ] 임.
 
        			webSocketSession.sendMessage(
        					new TextMessage("<span style='display:none'>"+wsession.getId()+"</span>&nbsp;[<span style='font-weight:bold; cursor:pointer;' class='loginuserName'>" +loginuser.getName()+ "</span>]<br><div style='background-color: white; display: inline-block; max-width: 60%; padding: 7px; border-radius: 15%; word-break: break-all; color: red;'>"+ messageVO.getMessage() +"</div> <div style='display: inline-block; padding: 20px 0 0 5px; font-size: 7pt;'>"+currentTime+"</div> <div>&nbsp;</div>" )
																																																																													/* word-break: break-all; 은 공백없이 영어로만 되어질 경우 해당구역을 빠져나가므로 이것을 막기위해서 사용한다. */
  					);	
                	
                	break;		// 지금의 특정대상(지금은 귓속말대상 웹소켓id)은 1개이므로 
                    			// 특정대상(지금은 귓속말대상 웹소켓id 임)에게만 메시지를 보내고  break;를 한다.
                }
        		
			}
        	
        }// end of for(WebSocketSession webSocketSession : connectedUsers) {}-------------------
        
	}
	
   	/*
		afterConnectionClosed(WebSocketSession session, CloseStatus status) 메소드는 
     	클라이언트가 연결을 끊었을 때 
     	즉, WebSocket 연결이 닫혔을 때(채팅페이지가 닫히거나 채팅페이지에서 다른 페이지로 이동되는 경우) 자동으로 호출되어지는(실행되어지는) 메소드이다.
    */   
	@Override
    public void afterConnectionClosed(WebSocketSession wsession, CloseStatus status) throws Exception {
        // 파라미터 WebSocketSession wsession 은 연결을 끊은 웹소켓 클라이언트임. (끊고자 하는 웹소켓이다.)
        // 파라미터 CloseStatus 은 웹소켓 클라이언트의 연결 상태.
       
       Map<String, Object> map = wsession.getAttributes();
       MemberVO loginuser = (MemberVO)map.get("loginuser");	// 끊고자 하는 애가 누구인지 알아와야 한다. (이순신인지 엄정화인지 알아와야 한다.)
       
       connectedUsers.remove(wsession);		//	(connectedUsers 에 쌓여있는 사용자를 remove 해주도록 한다.)
       // 웹소켓 서버에 연결되어진 클라이언트 목록에서 연결은 끊은 클라이언트는 삭제시킨다.
       // 연결을 끊은 본인한테는 보여줄 필요 없고, 나머지 채팅방에 있는 사람들에게만 보여주면 된다.
       
        for (WebSocketSession webSocketSession : connectedUsers) {
           
           // 퇴장했다라는 메시지를 자기자신을 뺀 나머지 모든 사용자들에게 메시지를 보여주도록 한다.
           // 빠져나간 사람의 id != 남아있는 사람의 id 일때, 세션을 끊은 본인 외에 나머지 모든 사용자들에게 메시지를 보여주도록 한다.
           // 남은 사람들에게 연결을 끊은 사람이 퇴장했다고 메세지를 보여준 후, 이를 view 단에 띄워주도록 한다.
           if (!wsession.getId().equals(webSocketSession.getId())) { 
                webSocketSession.sendMessage(
                   new TextMessage("[<span style='font-weight:bold;'>" +loginuser.getName()+ "</span>]" + "님이 <span style='color: red;'>퇴장</span>했습니다.")
                ); 
            }
        }// end of for------------------------------------------
       
 //     System.out.println("====> 웹채팅확인용 : 웹세션ID " + wsession.getId() + "이 퇴장했습니다.");
        
        
        
        
        ///// ===== 접속을 끊을시 접속자명단을 알려주기 위한 것 시작 ===== ///// 
        // 명단을 새로 갱신해줌. (나간 사람을 명단에서 보이지 않게 한다.)
        String connectingUserName = "「";
        
        for (WebSocketSession webSocketSession : connectedUsers) {
            Map<String, Object> map2 = webSocketSession.getAttributes();
            MemberVO loginuser2 = (MemberVO)map2.get("loginuser");  
           // "loginuser" 은 HttpSession에 저장된 키 값으로 로그인 되어진 사용자이다.
   
            connectingUserName += loginuser2.getName()+" "; 
        }// end of for------------------------------------------
        
        connectingUserName += "」";
        
        for (WebSocketSession webSocketSession : connectedUsers) {
             webSocketSession.sendMessage(new TextMessage(connectingUserName));
        }// end of for------------------------------------------
        ///// ===== 접속을 끊을시 접속자명단을 알려주기 위한 것 끝 ===== /////
        
    }
	
	
	
}
