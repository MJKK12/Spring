package com.spring.board.common;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

//==== #186. Spring Scheduler(스프링 스케줄러6) ==== //
//=== Spring Scheduler(스프링 스케줄러)를 사용한 email 발송하기 (특정시간에 발송) ===
//=== GoogleMail 을 사용할 수 있도록 Google 이메일 계정 및 암호를 입력하기
public class MySMTPAuthenticator extends Authenticator {

	@Override
	   public PasswordAuthentication getPasswordAuthentication() {
	      
	      // Gmail 의 경우 @gmail.com 을 제외한 아이디만 입력한다.
	      return new PasswordAuthentication("test220324","dkoshgiayuliyrok"); 	// (gmail 계정명, google 앱 비밀번호)
	      // "dkoshgiayuliyrok" 은 Google 에 로그인하기 위한 앱비밀번호이다.
	}	
	
}
