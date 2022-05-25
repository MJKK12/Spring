package com.spring.board.aop;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.spring.board.common.MyUtil;
import com.spring.board.model.MemberVO;
import com.spring.board.service.InterBoardService;
import com.spring.employees.service.InterEmpService;

// === #53. 공통관심사 클래스(Aspect 클래스) 생성하기 === //
// AOP(Aspect Oriented Programming, 관점 지향 프로그래밍)
@Aspect		// 공통관심사 클래스(Aspect 클래스) 로 등록된다.
@Component	// bean 으로 등록된다.
public class BoardAOP {

	// ===== Before Advice(보조업무) 만들기 ====== // 
	/*
	       주업무(<예: 글쓰기, 글수정, 댓글쓰기 직원목록조회 등등>)를 실행하기 앞서서  
	       이러한 주업무들은 먼저 로그인을 해야만 사용가능한 작업이므로
	       주업무에 대한 보조업무<예: 로그인 유무검사> 객체로 로그인 여부를 체크하는
	       관심 클래스(Aspect 클래스)를 생성하여 포인트컷(주업무)과 어드바이스(보조업무)를 생성하여
	       동작하도록 만들겠다.
	*/   
	
	// === Pointcut(주업무)을 설정해야 한다. === //
	// Pointcut 이란 공통관심사<예: 로그인 유무검사>를 필요로 하는 메소드를 말한다.
	// cut 는 잘라온다는 말임.
	@Pointcut("execution(public * com.spring..*Controller.requiredLogin_*(..) )")	// 해당 메소드가 어디있는지 본다. (#51. 게시판 글쓰기 참고)
	public void requiredLogin() {}
	// requiredLogin_() 메소드가 @Pointcut("execution(public * com.spring..*Controller.requiredLogin_*(..) )") 를 가리키고 있는 주업무이다.
	
	// return 타입은 아무거나 * (ModelAndView 또는 String..)
	// com.spring 인 이유 : bean 으로 올라가기 위해서 base-package 에 "com.spring.*" 로 해두었다. (servlet.context.xml 참고)
	// .. 의 의미 : 그 다음엔 무엇이든 와도 상관 없다.(오지 않아도 상관 없다.)
	// ~~~~ * Controller : 클래스명 뒤에는 Controller 로 붙는 것들이 온다. (ex: BoardController, EmpController)
	// requiredLogin_*  : requiredLogin_* 로 시작하는 메소드

	
	// ===== Before Advice(공통관심사, 보조업무) 만들기 ====== // 
	@Before("requiredLogin()")			// 주 업무를 하기 전에(@Before) ex)login 했는지 체크한다.
	private void logincheck(JoinPoint joinpoint) {			// 로그인 유무 검사를 하는 메소드 작성하기
		// 메소드를 가지고 와야 한다.
		// JoinPoint joinpoint는 포인트컷 되어진 주업무의 메소드이다.
		
		// 로그인유무를 확인하기 위해서는 request 를 통해 session 을 얻어와야 한다.
		HttpServletRequest request = (HttpServletRequest) joinpoint.getArgs()[0]; 		// 주업무에 있는 parameter 를 가져온다. getArgs() 는 return 타입이 배열이다.
																						// 주업무 메소드의 첫번째 파라미터를 얻어오는 것이다. 타입이 배열이므로 (HttpServletRequest) 캐스팅
		HttpServletResponse response = (HttpServletResponse) joinpoint.getArgs()[1];	// 주업무 메소드의 두번째 파라미터를 얻어오는 것이다. 타입이 배열이므로 (HttpServletResponse) 캐스팅

		
		HttpSession session = request.getSession();
		if(session.getAttribute("loginuser") == null) {
			String message = "먼저 로그인을 해주세요!";
			String loc = request.getContextPath()+"/login.action";
			
			request.setAttribute("message", message);
			request.setAttribute("loc", loc);

			// >>> 로그인 성공 후 로그인 하기 전 페이지로 돌아가는 작업 만들기 <<< // (로그인 하라는 문구 나온 후, session 에 url(클릭한 action 페이지) 을 넣어둔 것이다.)
			// === 현재 페이지의 주소(URL) 알아오기 === // Action 단 실행 전에 먼저 현재 페이지 주소를 알아온 것이다.
			String url = MyUtil.getCurrnetURL(request);
			session.setAttribute("goBackURL", url);	// session 에 url 정보를 저장시켜둔다.
			
			// 여기서는 return 타입을 void 로 해두었고, Controller 가 아니기 때문에 return "" 이런식으로 할 수 없다.
			// 따라서 Dispatcher 를 해준다.
			RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/msg.jsp");	// view 단이 어딘지 알려주도록 한다.
			try {
				dispatcher.forward(request, response);	// Controller 에도 동일하게 HttpServletResponse response 를 추가한다.
			} catch (ServletException | IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	// ===== #97. After Advice(보조업무) 만들기 ====== // 
	/*
	       주업무(<예: 글쓰기, 제품구매 등등>)를 실행한 다음에  
	       회원의 포인트를 특정점수(예: 100점, 200점, 300점) 증가해 주는 것이 공통의 관심사(보조업무)라고 보자.
	       관심 클래스(Aspect 클래스)를 생성하여 포인트컷(주업무)과 어드바이스(보조업무)를 생성하여
	       동작하도록 만들겠다.
	*/   	
	// === Pointcut(주업무)을 설정해야 한다. === //
	// Pointcut 이란 공통관심사<예: 로그인 유무검사>를 필요로 하는 메소드를 말한다.
	@Pointcut("execution(public * com.spring..*Controller.pointPlus_*(..) )")	// 해당 메소드가 어디있는지 본다. (#51. 게시판 글쓰기 참고)
	public void pointPlus() {}
		// pointPlus 업무를 시작한다.
		// DB 에 가기 위해서는 service 가 필요하다. (의존객체인 service)
	
	@Autowired	// bean 객체 이름을 넣어줘야 하기 때문에 @Resource 를 쓸 수 없다.
	private InterBoardService service;
	
	// === After Advice(공통관심사, 보조업무)를 구현한다. === //
	// After Advice : 주업무가 끝난 후 실행한다.
	// 회원의 포인트를 특정점수(예: 100점, 200점, 300점) 만큼 증가시키는 메소드 생성하기	
	@SuppressWarnings("unchecked")	//  앞으로는 노란줄 경고표시를 하지 말라는 뜻이다.
	@After("pointPlus()")	// 끝난 다음에 (After)
	public void pointPlus(JoinPoint joinpoint) {
		// JoinPoint joinpoint는 포인트컷 되어진 주업무의 메소드이다.

		Map<String, String> paraMap = (Map<String, String>) joinpoint.getArgs()[0]; 		// 주업무에 있는 parameter 를 가져온다. getArgs() 는 return 타입이 배열이다.
		// 주업무 메소드의 첫번째 파라미터를 받아오는 것이다.
		
		service.pointPlus(paraMap);
		
	}
	
	// ===== Around Advice(보조업무) 만들기 ====== //
   /*
         Before ----> 보조업무1
                  주업무 
         After  ----> 보조업무2
         
              보조업무1 + 보조업무2 을 실행하도록 해주는 것이 Around Advice 이다. 
    */
    // == Pointcut(주업무) 을 생성한다. ==
    //    Pointcut 이란 공통관심사를 필요로 하는 메소드를 말한다.
    // Pointcut 생성시 public 은 생략가능하다. 접근제한자를 생략하면 public 이 있는것으로 본다.
    // 왜냐하면 외부에서 특정메소드에 접근을 해야 하므로 접근제한자는 무조건  public 이어야 하기 때문이다.

    @Pointcut("execution(String com.spring..EmpController.empmanager_*(..))")
    public void empmanager() {}
    
    @Autowired
    private InterEmpService empservice;
    
    // == Around Advice(공통관심사, 보조업무)를 구현한다.
    // - 직원관리와 관련된 주업무를 실행하는 데 있어서 권한이 있는지(로그인 되어진 사용자의 gradelevel 값이 10)를 알아보는것을 보조업무로 보겠다. 
    // - 인사관리 페이지에 접속한 이후에, 인사관리 페이지에 접속한 페이지URL, 사용자ID, 접속IP주소, 접속시간을 기록으로 DB에 insert 하도록 한다.
    @Around("empmanager()")
    public String checkAuthority(ProceedingJoinPoint joinPoint) {
       /* Around Advice 에서는 ProceedingJoinPoint joinPoint 가
             포인트컷 되어진 주업무의 메소드이다. */
       
       String viewPage = null;
       
       // ============== 보조업무1 ============== //
       // - 직원관리와 관련된 주업무를 실행하는 데 있어서 권한이 있는지(로그인 되어진 사용자의 gradelevel 값이 10)를 알아보는것을 보조업무로 보겠다.
       HttpServletRequest request = (HttpServletRequest) joinPoint.getArgs()[0];
       HttpSession session = request.getSession();
       MemberVO loginuser = (MemberVO) session.getAttribute("loginuser");
       
       if(loginuser == null || loginuser.getGradelevel() < 10) {
         // 로그인 하지 않거나 로그인 되어진 사용자의 gradelevel 값이 10 미만 인 경우
         // 주업무를 실행하지 않고 "접근할 수 있는 권한이 없습니다." 라는 alert 메시지만 보이도록 하겠다. 
          
          String message = "접근할 수 있는 권한이 없습니다.";
          String loc = "javascript:history.back()";
          
          request.setAttribute("message", message);
          request.setAttribute("loc", loc);
          
          viewPage = "msg";
       }
       
       //////////////////////////////////////////////////////////////////////
       else {
           // 로그인 되어진 사용자의 gradelevel 값이 10 이상 인 경우
          
          try {
              viewPage = (String) joinPoint.proceed(); 
              // !!!! 중요!!!! 주업무 메소드가 실행되는 것이다. !!!!!
              
              System.out.println("viewPage => " + viewPage);
              // viewPage => emp/empList.tiles2 
              // viewPage => emp/chart.tiles2
            /* 
                 joinPoint.proceed(); 은 
                 Pointcut 에 설정된 주업무 메소드를 실행하라는 것이다.
                 지금은  String com.spring..*Controller.empmanager_*(..) 이다.
                  
                Object org.aspectj.lang.ProceedingJoinPoint.proceed() throws Throwable
                 Throwable 클래스는 예외처리를 할 수 있는 최상위 클래스이다. 
                 Throwable 클래스의 자식 클래스가 Exception 과 Error 클래스이다.
                   
                     그리고 joinPoint.proceed()메소드의 리턴값은 Object 이다.
                 이를 통해 Aspect 로 연결된 Original Method(주업무 메소드, 지금은 *Controller 클래스의 메소드명이 empmanager_ 으로 시작하는 메소드임)의 리턴값을  
                 형변환(캐스팅)을 통하여 받을수 있다.
               */
          } catch (Throwable e) {
            e.printStackTrace();
          } finally {
             
             // ============== 보조업무2 ============== //
             // - 인사관리 페이지에 접속한 이후에, 인사관리 페이지에 접속한 페이지URL, 사용자ID, 접속IP주소, 접속시간을 기록으로 DB에 insert 하도록 한다.
             String fk_userid = loginuser.getUserid();
             String clientIp = request.getRemoteAddr(); // 클라이언트의 IP 주소를 알아오는 것 
                          
             Date now = new Date(); // 현재시각 
             SimpleDateFormat sdfrmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
             String accessTime = sdfrmt.format(now);
             
             Map<String,String> paraMap = new HashMap<>();
             paraMap.put("pageUrl", request.getContextPath()+MyUtil.getCurrnetURL(request));
             paraMap.put("fk_userid", fk_userid);
             paraMap.put("clientIp", clientIp);
             paraMap.put("accessTime", accessTime);
             
             empservice.insert_accessTime(paraMap);
          } 
       }
       
       return viewPage;
      /* 
         return viewPage; 의 뜻은 
           주업무 메소드인 public String com.spring..*Controller.empmanager_*(..) 쪽으로 넘겨준다는 것이다.
           그러므로 주업무 메소드의 리턴타입이 String 이므로 viewPage 타입도 String 이어야 하는 것이다.
       */
    }
	
	
	
}
