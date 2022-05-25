package com.spring.chatting.websockethandler;

import com.google.gson.Gson;

//==== #199. 웹채팅 관련9 ==== //

public class MessageVO {

	private String message;
	private String type;	// type 이 all 이면 전체에게 채팅 메시지를 보낸다.
	private String to;		// 특정 웹소켓 ID // 누구와 비밀대화를 할 것인지 알아야 한다. (비밀대화의 대상)

	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getTo() {
		return to;
	}
	
	public void setTo(String to) {
		this.to = to;
	}
	
	////////////////////////////////////////////////////////////////
	
	public static MessageVO convertMessage(String source) {
		
		Gson gson = new Gson();
		
		MessageVO messagevo = gson.fromJson(source, MessageVO.class);		// MessageVO 로 바꿔야 한다. (java 에서 써야하기 때문이다.)
											// source 는 JSON 형태로 되어진 문자열
											// gson.fromJson(source, MessageVO.class); 은 
											// JSON 형태로 되어진 문자열 source를 실제 MessageVO 객체로 변환해준다. (호환이 모두 가능하다.)
		return messagevo;
	}	
	
	
}
