package com.spring.board.common;


import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Component;

// ==== #185. Spring Scheduler(스프링 스케줄러5) ==== //
// === Spring Scheduler(스프링 스케줄러)를 사용한 email 발송하기 (특정시간에 발송) ===
// === email 을 보내주는 클래스를 생성해야 한다.
// @Controller , @Service , @Repository 애노테이션을 사용하는 클래스는
// @Controller , @Service , @Repository 애노테이션 속에 @Component 기능이 이미 포함되어져 있으므로
// 굳이 @Component 를 또 다시 기술할 필요가 없다. 저 세가지 중에 해당하지 않으면 아래 GoogleMail 위에 @Component 를 써준다.

@Component
public class GoogleMail {	//  bean 으로 올라와야 한다. (컨트롤러,서비스,모델도 아니므로 bean 으로 올려보내줘야 한다.)

	// ==== 먼저 오라클에서 tbl_reservation 테이블을 생성해야 한다. ====
    // ==== Spring Scheduler(스프링 스케줄러)를 사용한 email 발송하기 예제 ==== //
    public void sendmail_Reservation(String recipient, String emailContents)
           throws Exception {
           
           // 1. 정보를 담기 위한 객체
           Properties prop = new Properties(); 
           
           // 2. SMTP 서버의 계정 설정
              //    Google Gmail 과 연결할 경우 Gmail 의 email 주소를 지정 
           prop.put("mail.smtp.user", "test220324@gmail.com");
               
           
           // 3. SMTP 서버 정보 설정
           //    Google Gmail 인 경우  smtp.gmail.com
           prop.put("mail.smtp.host", "smtp.gmail.com");
                
           
           prop.put("mail.smtp.port", "465");
           prop.put("mail.smtp.starttls.enable", "true");
           prop.put("mail.smtp.auth", "true");
           prop.put("mail.smtp.debug", "true");
           prop.put("mail.smtp.socketFactory.port", "465");
           prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
           prop.put("mail.smtp.socketFactory.fallback", "false");
           
           prop.put("mail.smtp.ssl.enable", "true");
           prop.put("mail.smtp.ssl.trust", "smtp.gmail.com");
             
           
           Authenticator smtpAuth = new MySMTPAuthenticator();
           Session ses = Session.getInstance(prop, smtpAuth);
              
           // 메일을 전송할 때 상세한 상황을 콘솔에 출력한다.
           ses.setDebug(true);
                   
           // 메일의 내용을 담기 위한 객체생성
           MimeMessage msg = new MimeMessage(ses);

           // 제목 설정
           String subject = "localhost:9090/board/ 방문 예약일자를 알려드립니다. ";
           msg.setSubject(subject);
                   
           // 보내는 사람의 메일주소
           String sender = "test220324@gmail.com";
           Address fromAddr = new InternetAddress(sender);
           msg.setFrom(fromAddr);
                   
           // 받는 사람의 메일주소
           Address toAddr = new InternetAddress(recipient);
           msg.addRecipient(Message.RecipientType.TO, toAddr);
                   
           // 메시지 본문의 내용과 형식, 캐릭터 셋 설정
           msg.setContent("<div style='font-size:14pt;'>"+emailContents+"</div>", "text/html;charset=UTF-8");
                   
           // 메일 발송하기
           Transport.send(msg);
           
        }// end of sendmail_Reservation(String recipient, String emailContents)-------------------       	
	
}
