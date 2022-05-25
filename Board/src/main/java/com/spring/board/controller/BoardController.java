package com.spring.board.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.spring.board.common.FileManager;
import com.spring.board.common.MyUtil;
import com.spring.board.common.Sha256;
import com.spring.board.model.*;
import com.spring.board.service.*;

/*
   사용자 웹브라우저 요청(View)  ==> DispatcherServlet ==> @Controller 클래스 <==>> Service단(핵심업무로직단, business logic단) <==>> Model단[Repository](DAO, DTO) <==>> myBatis <==>> DB(오라클)           
   (http://...  *.action)                                  |                                                                                                                              
    ↑                                                View Resolver
    |                                                      ↓
    |                                                View단(.jsp 또는 Bean명)
    -------------------------------------------------------| 
   
   사용자(클라이언트)가 웹브라우저에서 http://localhost:9090/board/test_insert.action 을 실행하면
   배치서술자인 web.xml 에 기술된 대로  org.springframework.web.servlet.DispatcherServlet 이 작동된다.
  DispatcherServlet 은 bean 으로 등록된 객체중 controller 빈을 찾아서  URL값이 "/test_insert.action" 으로
   매핑된 메소드를 실행시키게 된다.                                               
  Service(서비스)단 객체를 업무 로직단(비지니스 로직단)이라고 부른다.
  Service(서비스)단 객체가 하는 일은 Model단에서 작성된 데이터베이스 관련 여러 메소드들 중 관련있는것들만을 모아 모아서
   하나의 트랜잭션 처리 작업이 이루어지도록 만들어주는 객체이다.
   여기서 업무라는 것은 데이터베이스와 관련된 처리 업무를 말하는 것으로 Model 단에서 작성된 메소드를 말하는 것이다.
   이 서비스 객체는 @Controller 단에서 넘겨받은 어떤 값을 가지고 Model 단에서 작성된 여러 메소드를 호출하여 실행되어지도록 해주는 것이다.
   실행되어진 결과값을 @Controller 단으로 넘겨준다.
*/

// === #30. 컨트롤러 선언 ===
@Component
/*
 * XML에서 빈을 만드는 대신에 클래스명 앞에 @Component 어노테이션을 적어주면 해당 클래스는 bean으로 자동 등록된다. 그리고
 * bean의 이름(첫글자는 소문자)은 해당 클래스명이 된다. 즉, 여기서 bean의 이름은 boardController 이 된다.
 * 여기서는 @Controller 를 사용하므로 @Component 기능이 이미 있으므로 @Component를 명기하지 않아도
 * BoardController 는 bean 으로 등록되어 스프링컨테이너가 자동적으로 관리해준다.
 */
@Controller // Bean 기능 + Controller 기능 ( @Component 를 빼도, 안빼도 무방하다. )
public class BoardController {

	// === #35. 의존객체 주입하기(DI: Dependency Injection) ===
	// ※ 의존객체주입(DI : Dependency Injection)
	// ==> 스프링 프레임워크는 객체를 관리해주는 컨테이너를 제공해주고 있다.
	// 스프링 컨테이너는 bean으로 등록되어진 BoardController 클래스 객체가 사용되어질때,
	// BoardController 클래스의 인스턴스 객체변수(의존객체)인 BoardService service 에
	// 자동적으로 bean 으로 등록되어 생성되어진 BoardService service 객체를
	// BoardController 클래스의 인스턴스 변수 객체로 사용되어지게끔 넣어주는 것을 의존객체주입(DI : Dependency
	// Injection)이라고 부른다.
	// 이것이 바로 IoC(Inversion of Control == 제어의 역전) 인 것이다.
	// 즉, 개발자가 인스턴스 변수 객체를 필요에 의해 생성해주던 것에서 탈피하여 스프링은 컨테이너에 객체를 담아 두고,
	// 필요할 때에 컨테이너로부터 객체를 가져와 사용할 수 있도록 하고 있다.
	// 스프링은 객체의 생성 및 생명주기를 관리할 수 있는 기능을 제공하고 있으므로, 더이상 개발자에 의해 객체를 생성 및 소멸하도록 하지 않고
	// 객체 생성 및 관리를 스프링 프레임워크가 가지고 있는 객체 관리기능을 사용하므로 Inversion of Control == 제어의 역전
	// 이라고 부른다.
	// 그래서 스프링 컨테이너를 IoC 컨테이너라고도 부른다.

	// IOC(Inversion of Control) 란 ?
	// ==> 스프링 프레임워크는 사용하고자 하는 객체를 빈형태로 이미 만들어 두고서 컨테이너(Container)에 넣어둔후
	// 필요한 객체사용시 컨테이너(Container)에서 꺼내어 사용하도록 되어있다.
	// 이와 같이 객체 생성 및 소멸에 대한 제어권을 개발자가 하는것이 아니라 스프링 Container 가 하게됨으로써
	// 객체에 대한 제어역할이 개발자에게서 스프링 Container로 넘어가게 됨을 뜻하는 의미가 제어의 역전
	// 즉, IOC(Inversion of Control) 이라고 부른다.

	// === 느슨한 결합 ===
	// 스프링 컨테이너가 BoardController 클래스 객체에서 BoardService 클래스 객체를 사용할 수 있도록
	// 만들어주는 것을 "느슨한 결합" 이라고 부른다.
	// 느스한 결합은 BoardController 객체가 메모리에서 삭제되더라도 BoardService service 객체는 메모리에서 동시에
	// 삭제되는 것이 아니라 남아 있다.

	// ===> 단단한 결합(개발자가 인스턴스 변수 객체를 필요에 의해서 생성해주던 것)
	// private InterBoardService service = new BoardService();
	// ===> BoardController 객체가 메모리에서 삭제 되어지면 BoardService service 객체는 멤버변수(필드)이므로
	// 메모리에서 자동적으로 삭제되어진다.

	// #### 의존객체 목록 #### //
	// Spring 은 항상 Service 가 필요하다! (Controller 는 service를 의존객체로 한다.)
	@Autowired // Type에 따라 알아서 Bean 을 주입해준다. (service 를 null 로 만들지 않음.)
	private InterBoardService service; // 필요할 땐 사용하고, 필요하지 않을땐 사용하지 않기 (느슨한 결합)

	// === #155. 파일업로드 및 다운로드를 해주는 FileManager 클래스 의존객체 주입하기(DI : Dependency
	// Injection) ===
	@Autowired // Type에 따라 알아서 Bean 을 주입해준다.
	private FileManager fileManager; // type (FileManager) 만 맞으면 다 주입해준다.

	// ========= ***** Spring 기초 시작 ***** ========= //
	@RequestMapping(value = "/test/test_insert.action") // URL, 절대경로 contextPath 인 board 뒤의 것들을 가져온다. (확장자.java 와
														// 확장자.xml 은 그 앞에 contextPath 가 빠져있는 것이다.)
	public String test_insert(HttpServletRequest request) {

		int n = service.test_insert(); // service 에 요청한다.(보낸다), return 타입은 int 인 1이 될 것이다.

		String message = ""; // 메세지를 view 단에 보내준다.

		if (n == 1) {
			message = "데이터 입력 성공!!";
		} else {
			message = "데이터 입력 실패!!";
		}

		request.setAttribute("message", message); // message 를 request 영역에 저장해둔 것.
		request.setAttribute("n", n); // n 을 request 영역에 저장해둔 것.

		return "/test/test_insert"; // return 타입이 String, view 단을 표시해준다.
		// /WEB-INF/views/test/test_insert.jsp 페이지를 만들어야 한다.
		// 접두어(prefix) 접미어(suffix)
	}

	// select 하기
	@RequestMapping(value = "/test/test_select.action") // URL, 절대경로 contextPath 인 board 뒤의 것들을 가져온다. (확장자.java 와
														// 확장자.xml 은 그 앞에 contextPath 가 빠져있는 것이다.)
	public String test_select(HttpServletRequest request) {

		List<TestVO> testvoList = service.test_select(); // service 는 컨트롤러의 의존객체.

		request.setAttribute("testvoList", testvoList);

		return "test/test_select";
		// WEB-INF/views/test/test_select.jsp 페이지를 만들어야 한다. (view단)
	}

	// form 태그 (method : GET / POST)
//	@RequestMapping(value = "/test/test_form.action", method = {RequestMethod.GET})		// 오로지 GET 방식만 허락해준다.
//	@RequestMapping(value = "/test/test_form.action", method = {RequestMethod.POST})	// 오로지 POST 방식만 허락해준다.
	@RequestMapping(value = "/test/test_form.action") // 기본은 GET 방식 및 POST 방식 둘 모두를 허락해준다.
	public String test_form(HttpServletRequest request) {

		String method = request.getMethod();

		if ("GET".equalsIgnoreCase(method)) { // GET 방식이라면
			return "test/test_form"; // get 방식이라면, view 단 페이지를 띄운다. (jsp 파일을 보여준다.)
			// WEB-INF/views/test/test_form.jsp 페이지를 만들어야 한다. (view단)

		} else { // POST 방식이라면
			String no = request.getParameter("no");
			String name = request.getParameter("name");

			// form태그로 넘어온 것을 map 에 넣어서 DB 로 보내자. (Spring 은 Map 으로 항상 보낸다.)
			Map<String, String> paraMap = new HashMap<>();
			paraMap.put("no", no);
			paraMap.put("name", name);

			// Service 로 보낸다. 의존객체인 Service 를 가져오자.
			int n = service.test_insert(paraMap); // form 태그에 입력한 것을 insert 해줘야 한다.
													// 메소드의 '오버로딩' : 기존에 test_insert() 가 있지만, 파라미터가 다르다. 여기서는 paraMap 을
													// 넣어야 한다. (form 태그에서 넘어온 no,name 을 담은 paraMap 을 넣어줘야 한다.)

			if (n == 1) { // 1 이 나왔을때 (정상적으로 insert 됨) , insert 가 됐을 때 select 페이지로 이동하자. (insert 된 목록을
							// 보여주기 위해)
				return "redirect:/test/test_select.action"; // 페이지 이동 , view 단으로 보내는 것이 아니라, select 페이지로 가라는 뜻. (페이지 이동)
				// /test/test_select.action 이 페이지로 redirect(페이지 이동) 하라는 말이다.
			} else {
				return "redirect:/test/test_form.action";
				// /test/test_form.action 이 페이지로 redirect(페이지 이동) 하라는 말이다.
			}

		}

	}

	/////////////////////////////////////////////////////////////////////////////
	@RequestMapping(value = "/test/test_form_vo.action") // 기본은 GET 방식 및 POST 방식 둘 모두를 허락해준다.
	public String test_form_vo(HttpServletRequest request, TestVO vo) {

		String method = request.getMethod();

		if ("GET".equalsIgnoreCase(method)) { // GET 방식이라면
			return "test/test_form_vo"; // get 방식이라면, view 단 페이지를 띄운다. (jsp 파일을 보여준다.)
			// WEB-INF/views/test/test_form_vo.jsp 페이지를 만들어야 한다. (view단)

		} else { // POST 방식이라면
			// getParameter 를 따로 해오지 않음. form 태그에 있는 no,name 이 내가 쓰고자 하는 vo와 같은 이름을 가지고 있다.
			// (대소문자까지 같음)
			// 그렇기 때문에 getParameter 하지 않아도 결과가 나온다.
			// Service 로 보낸다. 의존객체인 Service 를 가져오자.
			int n = service.test_insert(vo); // form 태그에 입력한 것을 insert 해줘야 한다.
												// 메소드의 '오버로딩' : 기존에 test_insert() 가 있지만, 파라미터가 다르다. 여기서는 vo 을 넣어야 한다.
												// (form 태그에서 넘어온 no,name 을 담은 vo 을 넣어줘야 한다.)

			if (n == 1) { // 1 이 나왔을때 (정상적으로 insert 됨) , insert 가 됐을 때 select 페이지로 이동하자. (insert 된 목록을
							// 보여주기 위해)
				return "redirect:/test/test_select.action"; // 페이지 이동 , view 단으로 보내는 것이 아니라, select 페이지로 가라는 뜻. (페이지 이동)
				// /test/test_select.action 이 페이지로 redirect(페이지 이동) 하라는 말이다.
			} else {
				return "redirect:/test/test_form.action";
				// /test/test_form.action 이 페이지로 redirect(페이지 이동) 하라는 말이다.
			}

		}

	}
	/////////////////////////////////////////////////////////////////////////////

	// 똑같은 URL 이지만 GET 방식 / POST 방식
	@RequestMapping(value = "/test/test_form_2.action", method = { RequestMethod.GET }) // 오로지 GET 방식만 허락해준다.
	public String test_form_2() {

		return "test/test_form_2"; // get 방식이라면, view 단 페이지를 띄운다. (jsp 파일을 보여준다.)
		// WEB-INF/views/test/test_form_2.jsp 페이지를 만들어야 한다. (view단)

	}

	@RequestMapping(value = "/test/test_form_2.action", method = { RequestMethod.POST }) // 오로지 POST 방식만 허락해준다.
	// POST 방식은 form 태그에서 getParameter 받아와야 하므로 request 파라미터를 넣는다.
	public String test_form_2(HttpServletRequest request) {

		String no = request.getParameter("no");
		String name = request.getParameter("name");

		// form태그로 넘어온 것을 map 에 넣어서 DB 로 보내자. (Spring 은 Map 으로 항상 보낸다.)
		Map<String, String> paraMap = new HashMap<>();
		paraMap.put("no", no);
		paraMap.put("name", name);

		// Service 로 보낸다. 의존객체인 Service 를 가져오자.
		int n = service.test_insert(paraMap); // form 태그에 입력한 것을 insert 해줘야 한다.
												// 메소드의 '오버로딩' : 기존에 test_insert() 가 있지만, 파라미터가 다르다. 여기서는 paraMap 을 넣어야
												// 한다. (form 태그에서 넘어온 no,name 을 담은 paraMap 을 넣어줘야 한다.)

		if (n == 1) { // 1 이 나왔을때 (정상적으로 insert 됨) , insert 가 됐을 때 select 페이지로 이동하자. (insert 된 목록을
						// 보여주기 위해)
			return "redirect:/test/test_select.action"; // 페이지 이동 , view 단으로 보내는 것이 아니라, select 페이지로 가라는 뜻. (페이지 이동)
			// /test/test_select.action 이 페이지로 redirect(페이지 이동) 하라는 말이다.
		} else {
			return "redirect:/test/test_form.action";
			// /test/test_form.action 이 페이지로 redirect(페이지 이동) 하라는 말이다.
		}
	}

	// ====== AJAX 연습 시작 ====== //
	// 똑같은 URL 이지만 GET 방식 / POST 방식
	@RequestMapping(value = "/test/test_form_3.action", method = { RequestMethod.GET }) // 오로지 GET 방식만 허락해준다.
	public String test_form_3() {

		return "test/test_form_3"; // get 방식이라면, view 단 페이지를 띄운다. (jsp 파일을 보여준다.)
		// WEB-INF/views/test/test_form_3.jsp 페이지를 만들어야 한다. (view단)

	}

	/*
	 * @ResponseBody 란? 메소드에 @ResponseBody Annotation이 되어 있으면 return 되는 값은 View 단
	 * 페이지를 통해서 출력되는 것이 아니라 return 되어지는 값 그 자체를 웹브라우저에 바로 직접 쓰여지게 하는 것이다. 일반적으로 JSON
	 * 값을 Return 할때 많이 사용된다.
	 */
	@ResponseBody
	@RequestMapping(value = "/test/ajax_insert.action", method = { RequestMethod.POST }) // 오로지 POST 방식만 허락해준다.
	public String ajax_insert(HttpServletRequest request) {
		// aJax 로 넘겨준 key 값들을 받아오자. (test_form_3.jsp 에서 가져온다.)
		String no = request.getParameter("no");
		String name = request.getParameter("name");

		// DB 로 보내주자.
		// aJax로 넘어온 것을 map 에 넣어서 DB 로 보내자. (Spring 은 Map 으로 항상 보낸다.)
		Map<String, String> paraMap = new HashMap<>();
		paraMap.put("no", no);
		paraMap.put("name", name);

		int n = service.test_insert(paraMap); // form 태그에 입력한 것을 insert 해줘야 한다.

		// json 형식으로 바꿔줘야 한다.
		JSONObject jsonObj = new JSONObject(); // {} 이런식으로 나오게 된다.
		jsonObj.put("n", n); // {"n":1} // key 값이 1로 정상적으로 나오게 된다.
		// view 단 페이지에 찍어주자.

		return jsonObj.toString(); // 결과물이 문자열로 나옴. "{"n":1}" 결과물을 그대로 웹페이지에 보여주는 것이다.
		// 즉, view 단페이지인 .jsp 를 쓰는 것이 아니다.
		// 이를 위해 @ResponseBody 를 사용한다. --> return 된 값 자체를 웹브라우저에 보여준다.

	}

	/*
	 * @ResponseBody 란? 메소드에 @ResponseBody Annotation이 되어 있으면 return 되는 값은 View 단
	 * 페이지를 통해서 출력되는 것이 아니라 return 되어지는 값 그 자체를 웹브라우저에 바로 직접 쓰여지게 하는 것이다. 일반적으로 JSON
	 * 값을 Return 할때 많이 사용된다.
	 * 
	 * >>> 스프링에서 json 또는 gson을 사용한 ajax 구현시 데이터를 화면에 출력해 줄때 한글로 된 데이터가 '?'로 출력되어 한글이
	 * 깨지는 현상이 있다. 이것을 해결하는 방법은 @RequestMapping 어노테이션의 속성 중
	 * produces="text/plain;charset=UTF-8" 를 사용하면 응답 페이지에 대한 UTF-8 인코딩이 가능하여 한글 깨짐을
	 * 방지 할 수 있다. <<<
	 */
	@ResponseBody
	@RequestMapping(value = "/test/ajax_select.action", method = {
			RequestMethod.GET }, produces = "text/plain;charset=UTF-8") // 오로지 GET 방식만 허락해준다.
	public String ajax_select() {
		// data 가 넘어온 것이 없으므로, request 파라미터를 없애준다. (select 이므로 넘어온 것이 없다. service 에 다
		// 만들어 놓음.)
		List<TestVO> testvoList = service.test_select();

		// return 해줄 행이 복수개이다. (그러므로 Array 를 사용.)
		JSONArray jsonArr = new JSONArray(); // []

		// DB 에 insert 된 것이 하나도 없다면 .size() 를 했을 때 nullPointerException 이 발생한다.
		// nullPointerException 이 발생하지 않도록 != null 로 해준다.
		if (testvoList != null) {

			for (TestVO vo : testvoList) {
				JSONObject jsonObj = new JSONObject(); // {} {}
				jsonObj.put("no", vo.getNo()); // {"no":"101"} {"no":"102"}
				jsonObj.put("name", vo.getIrum()); // {"no":"101","name":"이순신"} {"no":"102","name":"엄정화"}
				jsonObj.put("writeday", vo.getWriteday()); // {"no":"101","name":"이순신","writeday":"2022-04-19 15:20:30"}
															// {"no":"102","name":"엄정화","writeday":"2022-04-19
															// 15:20:30"}

				// jsonArr 에 담아준다.
				jsonArr.put(jsonObj); // [{"no":"101","name":"이순신","writeday":"2022-04-19
										// 15:20:30"},{"no":"102","name":"엄정화","writeday":"2022-04-19 15:20:30"}]
			} // end of for------------------------

		}

		// jsonArr.put(jsonObj); 한 것을 웹페이지에 보여주자.
		return jsonArr.toString(); // String 으로 바꿔준다. "[{"no":"101","name":"이순신","writeday":"2022-04-19
									// 15:20:30"},{"no":"102","name":"엄정화","writeday":"2022-04-19 15:20:30"}]"
		// view 단을 보여주는 것이 아니라 그대로 보여주기 위해서 @ResponseBody 를 쓴다.
		// @ResponseBody 를 통해 [] 인 jsonArr 에 담아준다. (주로 json 에서 많이 쓴다.)

	}

	// === return 타입을 String 대신에 ModelAndView 를 사용해보겠습니다. === //
	@RequestMapping(value = "/test/test_form_vo_modelandview.action") // 기본은 GET 방식 및 POST 방식 둘 모두를 허락해준다.
	public ModelAndView test_form(HttpServletRequest request, TestVO vo, ModelAndView mav) {

		String method = request.getMethod();

		if ("GET".equalsIgnoreCase(method)) { // GET 방식이라면
			mav.setViewName("test/test_form_vo_modelandview");
			// get 방식이라면, view 단 페이지의 파일명 지정하기
			// WEB-INF/views/test/test_form_vo_modelandview.jsp 페이지를 만들어야 한다. (viewResolver,
			// 접두어/접미어)

		} else { // POST 방식이라면 ( 여기서는 getParameter 대신 vo 를 사용하겠다. )

			// Service 로 보낸다. 의존객체인 Service 를 가져오자.
			int n = service.test_insert(vo); // form 태그에 입력한 것을 DB에 insert 해줘야 한다.
												// 메소드의 '오버로딩' : 기존에 test_insert() 가 있지만, 파라미터가 다르다. 여기서는 paraMap 을 넣어야
												// 한다. (form 태그에서 넘어온 no,name 을 담은 paraMap 을 넣어줘야 한다.)

			if (n == 1) { // 1 이 나왔을때 (정상적으로 insert 됨) , insert 가 됐을 때 select 페이지로 이동하자. (insert 된 목록을
							// 보여주기 위해)
				mav.setViewName("redirect:/test/test_select_modelandview.action"); // 결과물 insert 하고 페이지이동
				// 페이지 이동 , view 단으로 보내는 것이 아니라, select 페이지로 가라는 뜻. (페이지 이동)
				// /test/test_select_modelandview.action 이 페이지로 redirect(페이지 이동) 하라는 말이다.
			} else {
				mav.setViewName("redirect:/test/test_form_vo_modelandview.action");
				// /test/test_form_vo_modelandview.action 이 페이지로 redirect(페이지 이동) 하라는 말이다.
			}

		}

		return mav;

	}

	// select 하기
	@RequestMapping(value = "/test/test_select_modelandview.action") // URL, 절대경로 contextPath 인 board 뒤의 것들을 가져온다.
																		// (확장자.java 와 확장자.xml 은 그 앞에 contextPath 가 빠져있는
																		// 것이다.)
	public ModelAndView test_select_modelandview(ModelAndView mav) { // 메소드의 오버로딩 (이름은 같지만, 파라미터가 다름)
		// Model 은 저장소 역할을 하면서 View 단도 알려준다.
		List<TestVO> testvoList = service.test_select(); // service 는 컨트롤러의 의존객체.

		mav.addObject("testvoList", testvoList); // request 영역에 넣어주는 것, request.setAttribute("testvoList", testvoList);
													// 와 같다.
		mav.setViewName("/test/test_select_modelandview"); // 결과물을 view 단 페이지로 넘겨주자.
		// WEB-INF/views/test/test_select_modelandview.jsp 페이지를 만들어야 한다. (view단)

		return mav;
	}

	// ========= ***** Spring 기초 끝 ***** ========= //

	///////// ========= tiles 연습 시작 ========= /////////

	// *** tiles 연습 1

	@RequestMapping(value = "/test/tiles_test_1.action") // URL, 절대경로 contextPath 인 board 뒤의 것들을 가져온다. (확장자.java 와
															// 확장자.xml 은 그 앞에 contextPath 가 빠져있는 것이다.)
	public String tiles_test_1() {

		return "tiles_test_1.tiles1"; // String 타입이므로 return 도 String.
		// /WEB-INF/views/tiles1/tiles_test_1.jsp 페이지를 만들어야 한다.
		// /WEB-INF/views/tiles1/{1}.jsp
	}

	@RequestMapping(value = "/test/tiles_test_2.action") // URL, 절대경로 contextPath 인 board 뒤의 것들을 가져온다. (확장자.java 와
															// 확장자.xml 은 그 앞에 contextPath 가 빠져있는 것이다.)
	public String tiles_test_2() {

		return "test/tiles_test_2.tiles1"; // String 타입이므로 return 도 String.
		// /WEB-INF/views/tiles1/test/tiles_test_2.jsp 페이지를 만들어야 한다.
		// /WEB-INF/views/tiles1/{1}/{2}.jsp
	}

	@RequestMapping(value = "/test/tiles_test_3.action") // URL, 절대경로 contextPath 인 board 뒤의 것들을 가져온다. (확장자.java 와
															// 확장자.xml 은 그 앞에 contextPath 가 빠져있는 것이다.)
	public String tiles_test_3() {

		return "test/sample/tiles_test_3.tiles1"; // String 타입이므로 return 도 String.
		// /WEB-INF/views/tiles1/test/sample/tiles_test_3.jsp 페이지를 만들어야 한다.
		// /WEB-INF/views/tiles1/{1}/{2}/{3}.jsp
	}

	// *** tiles 연습 2

	@RequestMapping(value = "/test/tiles_test_4.action") // URL, 절대경로 contextPath 인 board 뒤의 것들을 가져온다. (확장자.java 와
															// 확장자.xml 은 그 앞에 contextPath 가 빠져있는 것이다.)
	public ModelAndView tiles_test_4(ModelAndView mav) {

		mav.setViewName("tiles_test_4.tiles2");
		// /WEB-INF/views/tiles2/tiles_test_4.jsp 페이지를 만들어야 한다.
		// /WEB-INF/views/tiles2/{1}.jsp

		return mav; // String 타입이므로 return 도 String.
	}

	@RequestMapping(value = "/test/tiles_test_5.action") // URL, 절대경로 contextPath 인 board 뒤의 것들을 가져온다. (확장자.java 와
															// 확장자.xml 은 그 앞에 contextPath 가 빠져있는 것이다.)
	public ModelAndView tiles_test_5(ModelAndView mav) {

		mav.setViewName("test/tiles_test_5.tiles2");
		// /WEB-INF/views/tiles2/test/tiles_test_5.jsp 페이지를 만들어야 한다.
		// /WEB-INF/views/tiles2/{1}/{2}.jsp

		return mav; // String 타입이므로 return 도 String.
	}

	@RequestMapping(value = "/test/tiles_test_6.action") // URL, 절대경로 contextPath 인 board 뒤의 것들을 가져온다. (확장자.java 와
															// 확장자.xml 은 그 앞에 contextPath 가 빠져있는 것이다.)
	public ModelAndView tiles_test_6(ModelAndView mav) {

		mav.setViewName("test/sample/tiles_test_6.tiles2");
		// /WEB-INF/views/tiles2/test/sample/tiles_test_6.jsp 페이지를 만들어야 한다.
		// /WEB-INF/views/tiles2/{1}/{2}/{3}.jsp

		return mav; // String 타입이므로 return 도 String.
	}

	///////// ========= tiles 연습 끝 ========= /////////

	///////// ========= 게시판 시작 ========= /////////

	// === #36. 메인 페이지 요청 === //
	@RequestMapping(value = "/index.action") // URL, 절대경로 contextPath 인 board 뒤의 것들을 가져온다. (확장자.java 와 확장자.xml 은 그 앞에
												// contextPath 가 빠져있는 것이다.)
	public ModelAndView index(ModelAndView mav, HttpServletRequest request) {

		getCurrentURL(request); // 로그인 또는 로그아웃을 했을 때 현재 보이던 그 페이지로 그대로 돌아가기 위한 메소드 호출

		// DB 에 가서 읽어온다. service 에 가서 요청한다.
		List<String> imgfilenameList = service.getImgfilenameList(); // 캐러셀 이미지를 받아오기 위함.

		mav.addObject("imgfilenameList", imgfilenameList); // 결과물을 request 영역에 담는다.
		mav.setViewName("main/index.tiles1"); // view 단 페이지로 보내준다. (tiles1 에는 header, footer 만 있다.)
		// tiles-layout.xml 에 가서 view 페이지를 참고해온다. /WEB-INF/views/tiles1/{1}/{2}.jsp
		// /WEB-INF/views/tiles1/main/index.jsp 파일을 생성한다.

		return mav;
	}

	// === #40. 로그인 폼 페이지 요청 === //
	// 로그인 form 페이지 이므로 GET 방식
	@RequestMapping(value = "/login.action", method = { RequestMethod.GET }) // URL, 절대경로 contextPath 인 board 뒤의 것들을
																				// 가져온다. (확장자.java 와 확장자.xml 은 그 앞에
																				// contextPath 가 빠져있는 것이다.)
	public ModelAndView login(ModelAndView mav) {

		mav.setViewName("login/loginform.tiles1");
		// /WEB-INF/views/tiles1/login/loginform.jsp 파일을 생성한다.

		return mav;
	}

	// === #41. 로그인 처리하기 === //
	// 사용자가 로그인 시 아이디/비번을 입력할 때는 POST 방식으로 해줘야 한다.
	@RequestMapping(value = "/loginEnd.action", method = { RequestMethod.POST }) // URL, 절대경로 contextPath 인 board 뒤의 것들을
																					// 가져온다. (확장자.java 와 확장자.xml 은 그 앞에
																					// contextPath 가 빠져있는 것이다.)
	public ModelAndView loginEnd(ModelAndView mav, HttpServletRequest request) {

		// 사용자가 입력한 ID 와 PW 를 받아와야 한다.
		String userid = request.getParameter("userid");
		String pwd = request.getParameter("pwd");

		// DB 로 이동하자. (PW 암호화가 필요하다.)
		// DB 에 넣을 때 Map 에 넣는다.
		Map<String, String> paraMap = new HashMap<>();
		paraMap.put("userid", userid);
		paraMap.put("pwd", Sha256.encrypt(pwd)); // 입력받은 패스워드를 단방향으로 암호화 시켜준다.

		// service 에 넘기자.
		MemberVO loginuser = service.getLoginMember(paraMap); // 뭘 불러올 것인지? paraMap 에 담은 것을 불러온다.

		// service 에서 넘어온 값을 받자.
		if (loginuser == null) { // 사용자가 입력한 아이디 값 != DB 에 저장된 아이디 값 ( DB 에 없다면 null, 존재하지 않는 아이디 or 틀린 아이디 )
									// 사용자가 입력한 비밀번호 != DB 에 저장된 비밀번호 ( DB 에 없다면 null, 존재하지 않는 비밀번호 or 틀린 비밀번호)
			String message = "아이디 또는 암호가 틀립니다";
			String loc = "javascript:history.back()";

			mav.addObject("message", message);
			mav.addObject("loc", loc);

			mav.setViewName("msg"); // alert 만 띄울 것이므로 tiles 가 없다.
			// 접두어, 접미어 (servlet.context.xml)
			// /WEB-INF/views/msg.jsp 파일을 생성한다.
		} else { // 아이디와 암호가 존재하는 경우

			if (loginuser.getIdle() == 1) { // 로그인 한지 1년이 경과한 경우
				// 휴면처리가 되었는지 확인

				// 자바스크립트의 alert 에서는 \n이 줄바꿈, 자바에서는 \\n 가 줄바꿈.
				String message = "로그인을 한지 1년이 지나서 휴면상태로 전환되었습니다. \\n관리자에게 문의 바랍니다.";
				String loc = request.getContextPath() + "/index.action";
				// 원래는 위와같이 /index.action 이 아니라, 휴면인 계정을 풀어주는 페이지로 연결해주어야 한다.

				mav.addObject("message", message);
				mav.addObject("loc", loc);
				mav.setViewName("msg"); // alert 만 띄울 것이므로 tiles 가 없다.
				// 접두어, 접미어 (servlet.context.xml)
				// /WEB-INF/views/msg.jsp 파일을 생성한다.
			} else { // 로그인 한지 1년 이내인 경우
				// 정상적으로 로그인 됐을 경우, session 에 넣어준다. (휴면이 아닐 경우)
				HttpSession session = request.getSession();
				// 메모리에 생성되어있는 session 을 불러오는 것이다.

				// session 에 loginuser 를 넣어주자.
				session.setAttribute("loginuser", loginuser);
				// session(세션)에 로그인 되어진 사용자 정보인 loginuser 을 키이름을 "loginuser" 으로 저장시켜두는 것이다.

				if (loginuser.isRequirePwdChange() == true) { // type 이 boolean 이라면 get 으로 불러오는 것이 아니라 is 로 불러온다.
					// 암호를 마지막으로 변경한 것이 3개월이 경과한 경우
					String message = "비밀번호를 변경하신지 3개월이 경과되었습니다. \\n암호를 변경하시는 것을 추천합니다.";
					String loc = request.getContextPath() + "/index.action";
					// 원래는 위와같이 /index.action 이 아니라, 사용자의 암호를 변경해주는 페이지로 연결해주어야 한다.

					mav.addObject("message", message);
					mav.addObject("loc", loc);
					mav.setViewName("msg"); // alert 만 띄울 것이므로 tiles 가 없다.

				}

				else {
					// 암호를 마지막으로 변경한 것이 3개월이 경과하지 않은 경우 (3개월 이내)
					// DB 의 lastpwdchangedate

					// 로그인을 해야만 접근할 수 있는 페이지에 로그인을 하지 않은 상태에서 접근을 시도한 경우
					// "먼저 로그인을 하세요!!" 라는 메시지를 받고서 사용자가 로그인을 성공했다라면
					// 화면에 보여주는 페이지는 시작페이지로 가는 것이 아니라
					// 조금전 사용자가 시도하였던 로그인을 해야만 접근할 수 있는 페이지로 가기 위한 것이다.
					String goBackURL = (String) session.getAttribute("goBackURL"); // BoardAOP 에서 setAttribute로 넘긴
																					// goBackURL.

					if (goBackURL != null) { // session 정보에 머물렀던 URL 이 있다면, ( 올바르게 로그인 되었을 경우 원래 머물렀던 페이지로 이동.)
						mav.setViewName("redirect:" + goBackURL);
						session.removeAttribute(goBackURL); // session 에서 반드시 제거해주어야 한다. (이미 해당 url 로 이동했기 때문에.)
					}

					else {
						mav.setViewName("redirect:/index.action"); // session 정보에 이전에 머물렀던 URL 이 없다면 index 페이지로 간다.
					}

				}
			}

		}

		return mav;
	}

	// == #50. 로그아웃 처리하기 == //
	@RequestMapping(value = "/logout.action") // URL, 절대경로 contextPath 인 board 뒤의 것들을 가져온다. (확장자.java 와 확장자.xml 은 그 앞에
												// contextPath 가 빠져있는 것이다.)
	public ModelAndView logout(ModelAndView mav, HttpServletRequest request) {

		/*
		 * // 로그아웃 시, 시작페이지로 돌아가는 것이다. (추후에는 머물렀던 페이지 그대로 유지할 예정) HttpSession session =
		 * request.getSession(); session.invalidate();
		 * 
		 * String message = "로그아웃 되었습니다."; String loc =
		 * request.getContextPath()+"/index.action";
		 * 
		 * mav.addObject("message", message); mav.addObject("loc", loc);
		 * mav.setViewName("msg"); // 접두어&접미어 view 단 페이지 // /WEB-INF/views/msg.jsp
		 * 
		 * return mav;
		 * 
		 */

		// 로그아웃 시, 현재 봤던 그 페이지로 돌아가기 위한 것이다.
		HttpSession session = request.getSession();

		String goBackURL = (String) session.getAttribute("goBackURL");

		session.invalidate(); // 변수 goBackURL 에 기억해주고 session 지움.

		String message = "로그아웃 되었습니다.";

		String loc = "";
		if (goBackURL != null) { // goBackURL 에 현재 봤던 페이지가 남아있다면
			loc = request.getContextPath() + goBackURL; // 로그아웃 후에도 현재 봤던 페이지로 이동한다. (예를 들어 @RequestMapping(value =
														// "/add.action") 페이지)
		} else { // goBackURL 에 현재 봤던 페이지가 남아있지 않다면
			loc = request.getContextPath() + "/index.action"; // index 페이지로 돌아간다.
		}

		mav.addObject("message", message);
		mav.addObject("loc", loc);
		mav.setViewName("msg");
		// 접두어&접미어 view 단 페이지
		// /WEB-INF/views/msg.jsp

		return mav;
	}

	// == #51. 게시판 글쓰기 폼페이지 요청 == //
	// form 태그가 나와야 함.
	@RequestMapping(value = "/add.action") // URL, 절대경로 contextPath 인 board 뒤의 것들을 가져온다. (확장자.java 와 확장자.xml 은 그 앞에
											// contextPath 가 빠져있는 것이다.)
	public ModelAndView requiredLogin_add(HttpServletRequest request, HttpServletResponse response, ModelAndView mav) {

		// getCurrentURL(request); // 로그인 또는 로그아웃을 했을 때 현재 보이던 그 페이지로 그대로 돌아가기 위한 메소드 호출

		// === #142. 답변글쓰기가 추가된 경우 시작 === //
		String fk_seq = request.getParameter("fk_seq");
		String groupno = request.getParameter("groupno");
		String depthno = request.getParameter("depthno");
		String subject = "[답변] " + request.getParameter("subject"); // 답변글쓰기를 눌렀을 때, [답변] 이 제목에 저절로 달릴 수 있도록 한다.
		/*
		 * view.jsp 에서 "답변글쓰기" 를 할때 글제목에 [ 또는 ] 이 들어간 경우 아래와 같은 오류가 발생한다.
		 * 
		 * HTTP 상태 400 – 잘못된 요청 메시지 요청 타겟에서 유효하지 않은 문자가 발견되었습니다. 유효한 문자들은 RFC 7230과 RFC
		 * 3986에 정의되어 있습니다.
		 * 
		 * 해결책은 톰캣의 C:\apache-tomcat-9.0.55\conf\server.xml 에서 <Connector port="9090"
		 * URIEncoding="UTF-8" protocol="HTTP/1.1" connectionTimeout="20000"
		 * redirectPort="8443" /> 에 가서 <Connector port="9090" URIEncoding="UTF-8"
		 * protocol="HTTP/1.1" connectionTimeout="20000" redirectPort="8443"
		 * relaxedQueryChars="[]()^|&quot;" />
		 * 
		 * 와 같이 relaxedQueryChars="[]()^|&quot;" 을 추가해주면 된다.
		 */

		// 일반글쓰기가 아니라 답변글쓰기를 클릭했을 때만 fk_seq 가 넘어간다.
		if (fk_seq == null) { // 일반글쓰기라면 (답변글쓰기가 아니라면)
			fk_seq = ""; // 그냥 원글쓰기로 하도록 한다.
		}

		mav.addObject("fk_seq", fk_seq);
		mav.addObject("groupno", groupno);
		mav.addObject("depthno", depthno);
		mav.addObject("subject", subject);

		// === 답변글쓰기가 추가된 경우 끝 === //

		mav.setViewName("board/add.tiles1");
		// /WEB-INF/views/tiles1/board/add.jsp 파일을 생성한다.

		return mav;
	}

	// == #54. 게시판 글쓰기 완료 요청 == // (DB 에 글쓴 내용을 보내자)
	@RequestMapping(value = "/addEnd.action", method = { RequestMethod.POST }) // 오로지 POST 방식만 받는다.
//	public ModelAndView addEnd(ModelAndView mav, BoardVO boardvo) {	<== After Advice 를 사용하기 전 (공통의 관심사인 AOP, 글쓰기 시 포인트 UPDATE)
//	public ModelAndView pointPlus_addEnd(Map<String, String> paraMap, ModelAndView mav, BoardVO boardvo) {	// <== After Advice 를 사용하기
	public ModelAndView pointPlus_addEnd(Map<String, String> paraMap, ModelAndView mav, BoardVO boardvo,
			MultipartHttpServletRequest mrequest) { // <== After Advice 를 사용하기 및 파일 첨부하기
		// 굳이 new HashMap 할 필요 없이 파라미터 안에 넣어도 된다.

		// VO 를 쓸 때에는 getParameter 를 쓸 필요가 없다.
		/*
		 * form 태그의 name 명과 BoardVO 의 필드명이 같다라면 request.getParameter("form 태그의 name명");
		 * 을 사용하지 않더라도 자동적으로 BoardVO boardvo 에 set 되어진다. (xml 파일에서 일일이 set 을 해주지 않아도
		 * 된다.)
		 */

		/*
		 * === #151. 파일첨부가 된 글쓰기 이므로 먼저 위의 public ModelAndView
		 * pointPlus_addEnd(Map<String,String> paraMap, ModelAndView mav, BoardVO
		 * boardvo) { 을 주석처리 한 이후에 아래와 같이 한다. MultipartHttpServletRequest mrequest 를
		 * 사용하기 위해서는 먼저
		 * /Board/src/main/webapp/WEB-INF/spring/appServlet/servlet-context.xml 에서 #21
		 * 파일업로드 및 파일다운로드에 필요한 의존객체 설정하기 를 해두어야 한다.
		 */
		/*
		 * 웹페이지에 요청 form이 enctype="multipart/form-data" 으로 되어있어서 Multipart 요청(파일처리 요청)이
		 * 들어올때 컨트롤러에서는 HttpServletRequest 대신 MultipartHttpServletRequest 인터페이스를 사용해야
		 * 한다. MultipartHttpServletRequest 인터페이스는 HttpServletRequest 인터페이스와
		 * MultipartRequest 인터페이스를 상속받고있다. 즉, 웹 요청 정보를 얻기 위한 getParameter()와 같은 메소드와
		 * Multipart(파일처리) 관련 메소드를 모두 사용가능하다.
		 */

		// === 사용자가 쓴 글에 파일이 첨부되어 있는 것인지, 아니면 파일첨부가 안된것인지 구분을 지어주어야 한다. ===
		// === #153. !!! 첨부파일이 있는 경우 작업 시작 !!! ===
		MultipartFile attach = boardvo.getAttach(); // attach 가 첨부파일 이다. (실제 파일)

		if (!attach.isEmpty()) { // 내용물이 없으면 true, 있으면 false
			// attach(첨부파일)가 비어 있지 않으면 (즉, 첨부 파일이 있는 경우라면) 업로드 해야 한다.
			/*
			 * 1. 사용자가 보낸 첨부파일을 WAS(톰캣)의 특정 폴더에 저장해주어야 한다. >>> 파일이 업로드 되어질 특정 경로(폴더)지정해주기
			 * 우리는 WAS의 webapp/resources/files 라는 폴더로 지정해준다. 조심할 것은 Package Explorer 에서
			 * files 라는 폴더를 만드는 것이 아니다.
			 */
			// WAS 의 webapp 의 절대경로를 알아와야 한다.
			HttpSession session = mrequest.getSession();
			String root = session.getServletContext().getRealPath("/"); // /webapp 이다.
			// System.out.println("**** 확인용 webapp 의 절대경로 "+ root);
			// **** 확인용 webapp 의 절대경로
			// C:\NCS\workspace(spring)\.metadata\.plugins\org.eclipse.wst.server.core\tmp0\wtpwebapps\Board\

			String path = root + "resources" + File.separator + "files"; // window('\') 나 linux 는 '/' 이다.
			/*
			 * File.separator 는 운영체제에서 사용하는 폴더와 파일의 구분자이다. 운영체제가 Windows 이라면 File.separator
			 * 는 "\" 이고, 운영체제가 UNIX, Linux 이라면 File.separator 는 "/" 이다.
			 */

			// path 가 첨부파일이 저장될 WAS(톰캣)의 폴더가 된다. --> path 에 파일을 업로드 한다.
			// System.out.println("**** 확인용 path 의 절대경로 "+ path);
			// **** 확인용 path 의 절대경로
			// C:\NCS\workspace(spring)\.metadata\.plugins\org.eclipse.wst.server.core\tmp0\wtpwebapps\Board\resources\files

			/*
			 * 2. 파일 첨부를 위한 변수의 설정 및 값을 초기화 한 후 파일 올리기
			 */
			String newFileName = "";
			// WAS(톰캣)의 디스크에 저장될 파일명

			// 내용물을 읽어오도록 하자. // 배열단위로 올라가야 한다. (1byte 씩 올라가면 안됨..)
			byte[] bytes = null; // bytes 가 null 이라면 내용물이 없다는 것이다.
			// 첨부파일의 내용물을 담는 것, return 타입은 byte 의 배열

			long fileSize = 0; // 첨부파일의 크기

			try {
				bytes = attach.getBytes(); // 파일에서 내용물을 꺼내오자. 파일을 올렸을 때 깨진파일이 있다면 (입출력이 안된다!!) 그때 Exception 을 thorws 한다.
				// 첨부파일의 내용물을 읽어오는 것. 그 다음, 첨부한 파일의 파일명을 알아와야 DB 에 넣을 수가 있다. 그러므로 파일명을 알아오도록 하자.
				// 즉 파일을 올리고 성공해야 - 내용물을 읽어올 수 있고 - 파일명을 알아와서 DB 에 넣을 수가 있다.

				String originalFilename = attach.getOriginalFilename(); //
				// attach.getOriginalFilename() 이 첨부파일의 파일명(예: 강아지.png) 이다.
				// System.out.println("***** 확인용 originalFilename : " + originalFilename);
				// ***** 확인용 originalFilename : ScheduleService.java

				// 의존객체인 FileManager 를 불러온다. (String 타입으로 return 함.)
				// 리턴값 : 서버에 저장된 새로운 파일명(예: 2022042912181535243254235235234.png)
				newFileName = fileManager.doFileUpload(bytes, originalFilename, path);
				// 첨부되어진 파일을 업로드 하도록 하는 것이다.

				// System.out.println("*** 확인용 newFileName : "+newFileName);
				// *** 확인용 newFileName : 202204291228131018404583262100.jpg
				// *** 확인용 newFileName : 202204291232341018665636132600.jpg
				// 같은 1.jpg 파일을 올려도 위와 같은 이름으로 파일이 업로드 된다.

				// 파일을 받아와야지만 service 에 보낼 수가 있다. (DB 에 보내주도록 하자.)

				/*
				 * 3. BoardVO boardvo 에 fileName 값과 orgFilename 값과 fileSize 값을 넣어주기 (form 태그에
				 * name="attach" 하나밖에 없으므로 자동적으로 set 되는 것이 아니다. 그러므로 이름을 따로 set해줘야 한다.)
				 */
				boardvo.setFileName(newFileName);
				// WAS(톰캣)에 저장될 파일명(2022042912181535243254235235234.png)
				boardvo.setOrgFilename(originalFilename);
				// 게시판 페이지에서 첨부된 파일(강아지.png)을 보여줄 때 사용.
				// 또한 사용자가 파일을 다운로드 할때 사용되어지는 파일명으로 사용.
				fileSize = attach.getSize(); // 첨부파일의 크기 (단위는 byte 임)
				boardvo.setFileSize(String.valueOf(fileSize)); // 첨부파일의 size 를 알아오기 위함. (vo 에 String 으로 해왔기 때문에 바꿔준다.)

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		// === #154. !!! 첨부파일이 있는 경우 작업 끝 !!! ===

		// service 에 보내주자.
		// int n = service.add(boardvo); // 파일 첨부가 없는 글쓰기.

		// === #156. 파일첨부가 있는 글쓰기 또는 파일첨부가 없는 글쓰기로 나뉘어서 service 호출하기 === //
		// 먼저 위의 int n = service.add(boardvo); 부분을 주석처리 하고서 아래와 같이 한다.

		// insert 가 올바르게 됐다면,(n==1) 글을 썼기 때문에, 글 내용을 봐야한다. (/list.action 으로 가도록 한다.)
		int n = 0;

		// 첨부파일이 있는 경우와 없는 경우로 나눈다.
		if (attach.isEmpty()) {
			// 첨부파일이 없는 경우
			n = service.add(boardvo);
		} else {
			// 첨부파일이 있는 경우 (첨부파일이 있는 경우의 service - DAO - 연결을 통해 DB 에 넣어주자.)
			n = service.add_withFile(boardvo);
		}

		if (n == 1) {
			mav.setViewName("redirect:/list.action");
			// /list.action 페이지로 redirect(페이지 이동) 한다는 말이다. 즉, 글목록으로 이동한다.
		} else {
			mav.setViewName("board/error/add_error.tiles1");
			// /WEB-INF/views/tiles1/board/error/add_error.jsp 파일을 생성한다.
		}

		// === #96. After Advice 를 사용하기 === //
		// 글쓰기를 한 이후에 회원의 포인트를 100 점 증가 (DB 에서 update 해줘야 한다.)
		// 어떤 사용자(userid) 의 포인트를 증가할 것인가? (where = "userid"), HashMap 을 사용하자.
		// == After Advice 를 사용하기 위해 파라미터를 생성하는 것임 ==
		paraMap.put("fk_userid", boardvo.getFk_userid()); // boardvo 에서 꺼내오자.
		paraMap.put("point", "100"); // point 에 100점을 주도록 하겠다.
		/////////////////////////////////////////////////////////////////////////////////////

		return mav;
	}

	// == #58. 글목록 보기 페이지 요청 == //
	@RequestMapping(value = "/list.action") // URL, 절대경로 contextPath 인 board 뒤의 것들을 가져온다. (확장자.java 와 확장자.xml 은 그 앞에
											// contextPath 가 빠져있는 것이다.)
	public ModelAndView list(ModelAndView mav, HttpServletRequest request) {

		getCurrentURL(request); // 로그인 또는 로그아웃을 했을 때 현재 보이던 그 페이지로 그대로 돌아가기 위한 메소드 호출

		List<BoardVO> boardList = null; // DB 에서 복수개의 글을 읽어와야 한다.

		//////////////////////////////////////////////////////
		// === #69. 글조회수(readCount)증가 (DML문 update)는
		// 반드시 목록보기에 와서 해당 글제목을 클릭했을 경우에만 증가되고,
		// 웹브라우저에서 새로고침(F5)을 했을 경우에는 증가가 되지 않도록 해야 한다.
		// 이것을 하기 위해서는 session 을 사용하여 처리하면 된다.
		HttpSession session = request.getSession();
		session.setAttribute("readCountPermission", "yes"); // 정상적인 경로로 들어왔을 때, 조회수 증가를 허용한다. (새로고침 시 무한조회수 증가 방지)
		/*
		 * session 에 "readCountPermission" 키값으로 저장된 value값은 "yes" 이다. session 에
		 * "readCountPermission" 키값에 해당하는 value값 "yes"를 얻으려면 반드시 웹브라우저에서 주소창에
		 * "/list.action" 이라고 입력해야만 얻어올 수 있다.
		 */
		//////////////////////////////////////////////////////

		// === 페이징 처리를 안 한 검색어가 없는 전체 글목록 보여주기 === //
		// boardList = service.boardListNoSearch();

		// === #102. 페이징 처리를 한 검색어가 있는 전체 글목록 보여주기 === //
		/*
		 * String searchType = request.getParameter("searchType"); String searchWord =
		 * request.getParameter("searchWord");
		 * 
		 * if(searchType == null) { // 글제목, 글쓴이 부분이 null 일때 searchType = ""; }
		 * 
		 * if(searchWord == null) { // 검색 부분이 null 일때 searchWord = ""; }
		 * 
		 * // DB 로 보내기 위해 Map 을 만든다. Map<String, String> paraMap = new HashMap<>();
		 * paraMap.put("searchType", searchType); paraMap.put("searchWord", searchWord);
		 * 
		 * boardList = service.boardListSearch(paraMap);
		 * 
		 * //////////////////////////////////////////////////////////////////////////
		 * 
		 * // 아래는 검색대상 컬럼과 검색어를 유지시키기 위한 것이다. // view 단에 보여주자. 검색 후에 검색했던 내용을 남길 수 있도록
		 * 한다. (받은 값을 유지 시켜준다.) if(!"".equals(searchType) && !"".equals(searchWord)) {
		 * // searchType 과 searchWord 모두 "" 가 아닐 경우. (어떤 값이 입력된 것임)
		 * mav.addObject("paraMap", paraMap); // 어차피 map 에 있으니 searchType 과 searchWord
		 * 일일이 넘기지 않아도 된다. }
		 */

		//////////////////////////////////////////////////////////////////////////

		// === #114. 페이징 처리를 한 검색어가 있는 전체 글목록 보여주기 시작 === //
		/*
		 * 페이징 처리를 통한 글목록 보여주기는 예를 들어 3페이지의 내용을 보고자 한다라면 검색을 할 경우는 아래와 같이
		 * list.action?searchType=subject&searchWord=안녕&currentShowPageNo=3 와 같이 해주어야
		 * 한다. 또는 검색이 없는 전체를 볼때는 아래와 같이 list.action 또는
		 * list.action?searchType=&searchWord=&currentShowPageNo=3 또는
		 * list.action?searchType=subject&searchWord=&currentShowPageNo=3 와 같이 해주어야 한다.
		 */

		String searchType = request.getParameter("searchType");
		String searchWord = request.getParameter("searchWord");
		String str_currentShowPageNo = request.getParameter("currentShowPageNo");

		// 즉, searchType 에는 글제목, 내용 이렇게 두 가지 타입이 있는데 이 외의 것들이 입력되면 "" 로 처리하겠다.
		// (searchType=subject 이 아니라 searchType=ㄴㄹㅇㄹㄴㅇㄹㄴㅇㄹ 이런식일 때)
		if (searchType == null || (!"subject".equals(searchType)) && (!"name".equals(searchType))) {
			// 글제목, 글쓴이 부분이 null 일 때 (장난 쳤을 경우 그냥 모두 다 보여주도록 한다.)
			searchType = "";
		}

		if (searchWord == null || "".equals(searchWord) || searchWord.trim().isEmpty()) {
			// 검색 부분이 null 일때, 아예 입력하지 않았을 때, 오로지 공백으로만 이루어져 있을 때!! (장난 쳤을 경우 그냥 모두 다 보여주도록
			// 한다.)
			searchWord = "";
		}

		// DB 로 보내기 위해 Map 을 만든다. (검색타입 / 검색한 것을 넣어주고 DB 로 보내자.)
		Map<String, String> paraMap = new HashMap<>();
		paraMap.put("searchType", searchType);
		paraMap.put("searchWord", searchWord);

		// 먼저 총 게시물 건수(totalCount) 를 구해와야 한다.
		// 총 게시물 건수(totalCount) 는 검색조건이 있을 때와 없을 때로 나뉘어진다.
		int totalCount = 0; // 총 게시물 건수
		int sizePerPage = 10; // 한 페이지당 보여줄 게시물 건수
		int currentShowPageNo = 0; // 현재 보여주는 페이지 번호로서, 초기치로는 1페이지로 설정한다.
		int totalPage = 0; // 총 페이지수(웹브라우저 상에서 보여줄 총 페이지 개수, 페이지바)

		int startRno = 0; // 시작 행번호
		int endRno = 0; // 끝 행번호(rno)

		// 총 게시물 건수(totalCount)
		totalCount = service.getTotalCount(paraMap);
		// System.out.println("*** 확인용 totalCount : " +totalCount);

		// 만약에 총 게시물 건수(totalCount) 가 127개 라면
		// 총 페이지수(totalPage)는 13 개가 되어야 한다. ==> 오라클에서 ceil 또는 java 에서 math.ceil 한다.
		totalPage = (int) Math.ceil((double) totalCount / sizePerPage);
		// (double)127/10 ==> 12.7 ==> Math.ceil(12.7) ==> 13.0 ==> (int)13.0 ==> 13
		// (double)120/10 ==> 12.0 ==> Math.ceil(12.0) ==> 12.0 ==> (int)12.0 ==> 12

		if (str_currentShowPageNo == null) {
			// 페이지바를 거치치 않은 것이라면, 게시판에 보여지는 초기화면
			currentShowPageNo = 1; // 페이지바를 거치지 않았다면 1페이지를 보여주도록 하겠다.
		} else { // 사용자가 주소창에 페이지 넘버를 12.7 이런식으로 넣었을 때 방지, 넘버를 쓰지 않고 페이지 넘버에 ㄴㄻㄴㅇㄹ 씀으로써 장난쳤을 때.
			try { // Integer.parseInt 를 씀으로써 정수만 들어올 수 있도록, 그 외에는 Exception 처리를 해서 1페이지로 돌아가도록 한다.
				currentShowPageNo = Integer.parseInt(str_currentShowPageNo);
				if (currentShowPageNo < 1 || currentShowPageNo > totalPage) { // int 는 맞지만 유저가 장난쳤을 때
					currentShowPageNo = 1; // 0페이지 이하는 존재하지 않는다 || 전체 페이지수보다 더 큰 숫자를 입력했을 때
				}
			} catch (NumberFormatException e) {
				currentShowPageNo = 1;
			}

		}
		// **** 가져올 게시글의 범위를 구한다.(공식임!!!) ****
		/*
		 * currentShowPageNo startRno endRno
		 * -------------------------------------------- 1 page ===> 1 10 2 page ===> 11
		 * 20 3 page ===> 21 30 4 page ===> 31 40 ...... ... ...
		 */

		startRno = ((currentShowPageNo - 1) * sizePerPage) + 1;
		endRno = startRno + sizePerPage - 1;

		paraMap.put("startRno", String.valueOf(startRno));
		paraMap.put("endRno", String.valueOf(endRno));

		boardList = service.boardListSearchWithPaging(paraMap);
		// 페이징 처리한 글목록 가져오기(검색이 있든지, 검색이 없든지 모두 다 포함한 것이다.)

		// 아래는 검색대상 컬럼과 검색어를 유지시키기 위한 것이다.
		// view 단에 보여주자. 검색 후에 검색했던 내용을 select 태그와 input 태그에 남길 수 있도록 한다. (받은 값을 유지
		// 시켜준다.)
		if (!"".equals(searchType) && !"".equals(searchWord)) {
			// searchType 과 searchWord 모두 "" 가 아닐 경우. (어떤 값이 입력된 것임)
			mav.addObject("paraMap", paraMap); // 어차피 map 에 있으니 searchType 과 searchWord 일일이 넘기지 않아도 된다.
			// request 영역에 담아서 view 단 페이지에 보내준다.
		}

		// == #121. 페이지바 만들기 == //
		int blockSize = 10;
		// blockSize 는 1개 블럭(토막) 당 보여지는 페이지번호의 개수이다.
		/*
		 * 1 2 3 4 5 6 7 8 9 10 [다음][마지막] -- 1개블럭 [맨처음][이전] 11 12 13 14 15 16 17 18 19
		 * 20 [다음][마지막] -- 1개블럭 [맨처음][이전] 21 22 23
		 */

		int loop = 1;
		/*
		 * loop는 1부터 증가하여 1개 블럭을 이루는 페이지번호의 개수[ 지금은 10개(== blockSize) ] 까지만 증가하는 용도이다.
		 */
		int pageNo = ((currentShowPageNo - 1) / blockSize) * blockSize + 1;
		// *** !! 공식이다. !! *** //

		/*
		 * 1 2 3 4 5 6 7 8 9 10 -- 첫번째 블럭의 페이지번호 시작값(pageNo)은 1 이다. 11 12 13 14 15 16 17
		 * 18 19 20 -- 두번째 블럭의 페이지번호 시작값(pageNo)은 11 이다. 21 22 23 24 25 26 27 28 29 30
		 * -- 세번째 블럭의 페이지번호 시작값(pageNo)은 21 이다.
		 * 
		 * currentShowPageNo pageNo ---------------------------------- 1 1 = ((1 -
		 * 1)/10) * 10 + 1 2 1 = ((2 - 1)/10) * 10 + 1 3 1 = ((3 - 1)/10) * 10 + 1 4 1 5
		 * 1 6 1 7 1 8 1 9 1 10 1 = ((10 - 1)/10) * 10 + 1
		 * 
		 * 11 11 = ((11 - 1)/10) * 10 + 1 12 11 = ((12 - 1)/10) * 10 + 1 13 11 = ((13 -
		 * 1)/10) * 10 + 1 14 11 15 11 16 11 17 11 18 11 19 11 20 11 = ((20 - 1)/10) *
		 * 10 + 1
		 * 
		 * 21 21 = ((21 - 1)/10) * 10 + 1 22 21 = ((22 - 1)/10) * 10 + 1 23 21 = ((23 -
		 * 1)/10) * 10 + 1 .. .. 29 21 30 21 = ((30 - 1)/10) * 10 + 1
		 */

		String pageBar = "<ul style='list-style:none;'>";
		String url = "list.action"; // 상대경로

		// === [맨처음][이전] 만들기 === //
		if (pageNo != 1) {
			pageBar += "<li style='display:inline-block; width:70px; font-size:12pt;'><a href='" + url + "?searchType="
					+ searchType + "&searchWord=" + searchWord + "&currentShowPageNo=1'>[맨처음]</a></li>";
			pageBar += "<li style='display:inline-block; width:50px; font-size:12pt;'><a href='" + url + "?searchType="
					+ searchType + "&searchWord=" + searchWord + "&currentShowPageNo=" + (pageNo - 1)
					+ "'>[이전]</a></li>";
		}

		while (!(loop > blockSize || pageNo > totalPage)) { // blockSize 에 따라 달라진다. (2개 블럭, 10개 블럭 ....)
			// loop 가 blockSize 보다 더 클때 또는 pageNo 가 totalPage 보다 더 클때만 ++ 후 빠져나온다.

			if (pageNo == currentShowPageNo) { // 페이지 번호 == 내가 보고자 하는 페이지 번호
				pageBar += "<li style='display:inline-block; width:30px; font-size:12pt; border:solid 1px gray; color:red; padding:2px 4px; '>"
						+ pageNo + "</li>";
			} else { // 페이지 번호 != 내가 보고자 하는 페이지 번호
				pageBar += "<li style='display:inline-block; width:30px; font-size:12pt;'><a href='" + url
						+ "?searchType=" + searchType + "&searchWord=" + searchWord + "&currentShowPageNo=" + pageNo
						+ "'>" + pageNo + "</a></li>";
			}

			loop++;
			pageNo++;

		} // end of while--------------------------------------------------------

		// === [다음][마지막] 만들기 === //
		if (pageNo <= totalPage) {
			pageBar += "<li style='display:inline-block; width:50px; font-size:12pt;'><a href='" + url + "?searchType="
					+ searchType + "&searchWord=" + searchWord + "&currentShowPageNo=" + pageNo + "'>[다음]</a></li>";
			pageBar += "<li style='display:inline-block; width:70px; font-size:12pt;'><a href='" + url + "?searchType="
					+ searchType + "&searchWord=" + searchWord + "&currentShowPageNo=" + totalPage + "'>[마지막]</a></li>";
		}

		pageBar += "</ul>";

		mav.addObject("pageBar", pageBar); // request 영역에 pageBar를 담아서 view 단에 넘겨주자.

		// === #123. 페이징 처리되어진 후 특정 글제목을 클릭하여 상세내용을 본 이후
		// 사용자가 목록보기 버튼을 클릭했을때 돌아갈 페이지를 알려주기 위해
		// 현재 페이지 주소를 뷰단으로 넘겨준다.
		String goBackURL = MyUtil.getCurrnetURL(request);
		// System.out.println("*** 확인용 goBackURL : "+goBackURL);
		/*
		 *** 확인용 goBackURL : /list.action 확인용 goBackURL : /list.action?searchType=
		 * searchWord=%20 currentShowPageNo=2 확인용 goBackURL :
		 * /list.action?searchType=subject searchWord=j 확인용 goBackURL :
		 * /list.action?searchType=subject searchWord=j%20 currentShowPageNo=2
		 */
		mav.addObject("goBackURL", goBackURL.replaceAll("&", " ")); // view 단에 넘겨주자. & 을 " " 로 바꿔준 결과값들.
		// === 페이징 처리를 한 검색어가 있는 전체 글목록 보여주기 끝 === //
		///////////////////////////////////////////////////////////////////////////////////////////

		mav.addObject("boardList", boardList); // view 단에 넘겨주자.
		mav.setViewName("board/list.tiles1");
		// /WEB-INF/views/tiles1/board/list.jsp 파일을 생성한다.

		return mav;
	}

	// == #62. 글 1개를 보여주는 페이지 요청 == //
	@RequestMapping(value = "/view.action") // URL, 절대경로 contextPath 인 board 뒤의 것들을 가져온다. (확장자.java 와 확장자.xml 은 그 앞에
											// contextPath 가 빠져있는 것이다.)
	public ModelAndView view(ModelAndView mav, HttpServletRequest request) {

		getCurrentURL(request); // 로그인 또는 로그아웃을 했을 때 현재 보이던 그 페이지로 그대로 돌아가기 위한 메소드 호출

		// list.jsp 에서 seq 를 넘겨주었으므로 getParameter 를 해와야 한다. (?seq="+seq; 형식으로 넘겨옴.)
		// 조회하고자 하는 글번호 받아오기
		String seq = request.getParameter("seq");

		// 검색이 있을 때 이전글, 다음글에도 검색에어 해당하는 이전글, 다음글 목록만 보여주도록 한다.
		// 글목록에서 검색된 글내용일 경우 이전글제목, 다음글제목은 검색된 결과물 내의 이전글과 다음글이 나오도록 하기 위한 것이다.
		// 이때 searchType 과 searchName 을 받아오도록 해야한다.
		String searchType = request.getParameter("searchType");
		String searchWord = request.getParameter("searchWord");

		// 검색타입(searchType)과 검색어(searchWord)를 입력하지 않았을 경우에 대비해서 (null) 아래와 같이 작성한다.
		// (안전하게 해두기!)
		if (searchType == null) {
			searchType = "";
		}

		if (searchWord == null) {
			searchWord = "";
		}

		// Mapper 로 view 단에서 넘어온 검색타입과 검색어를 보내주기 위해 Map 에 담도록 하자. (아래 try~catch)

		// === #125. 페이징 처리되어진 후 특정 글제목을 클릭하여 상세내용을 본 이후
		// 사용자가 목록보기 버튼을 클릭했을때 돌아갈 페이지를 알려주기 위해
		// 현재 페이지 주소를 뷰단으로 넘겨준다.

		String gobackURL = request.getParameter("gobackURL"); // http://localhost:9090/board/view.action?seq=1&gobackURL=/list.action?searchType=subject&searchWord=j%20&currentShowPageNo=2
		// System.out.println("*** 확인용 gobackURL : " + gobackURL); // view 단에서 gobackURL
		// 넘어오는지 체크
		// *** 확인용 gobackURL : /list.action
		// *** 확인용 gobackURL : /list.action?searchType= , 뒷부분이 잘려서 나옴
		// *** 확인용 gobackURL : /list.action?searchType=subject
		// *** 확인용 gobackURL : /list.action?searchType=subject

		// 123 번에서 & 을 " " 로 replace 한 것을 다시 " "에서 &로 바꿔주자. contains(); 는 boolean 타입.(특정
		// 문자가 포함되어 있는지/아닌지)
		if (gobackURL != null && gobackURL.contains(" ")) { // gobackURL 에서 공백(" ")이 들어가 있는지 아닌지
			gobackURL = gobackURL.replaceAll(" ", "&");

		}
		// System.out.println("*** 확인용 goBackURL : "+goBackURL);
		/*
		 *** 확인용 goBackURL : /list.action 확인용 goBackURL :
		 * /list.action?searchType=&searchWord=%20&currentShowPageNo=2 확인용 goBackURL :
		 * /list.action?searchType=subject&searchWord=j 확인용 goBackURL :
		 * /list.action?searchType=subject&searchWord=j%20&currentShowPageNo=2
		 */

		// System.out.println("~~~~ view 의 searchType : " + searchType);
		// System.out.println("~~~~ view 의 searchWord : " + searchWord);
		// System.out.println("~~~~ view 의 gobackURL : " + gobackURL);

		mav.addObject("gobackURL", gobackURL); // " " 을 "&" 을 바꾼 것을 다시 view 단으로 보낸다.

		// === 125번 작업 끝 === //
		//////////////////////////////////////////////////////////////////////////////////////////

		try {

			Integer.parseInt(seq); // 사용자가 seq=? 의 ? 부분에 숫자가 아니라 문자를 입력했을 때 Exception 처리를 하도록 한다.

			// 추후에 글 내용 1개 조회 뿐만 아니라 검색도 해야하므로 Map 에 담도록 한다.
			Map<String, String> paraMap = new HashMap<>();
			paraMap.put("seq", seq);

			// Mapper 로 view 단에서 넘어온 검색타입과 검색어를 보내주기 위해 Map 에 담아서 보내준다.
			paraMap.put("searchType", searchType);
			paraMap.put("searchWord", searchWord);

			// Map 에 담은 검색타입과 검색어를 View 단으로 보내준다. (paraMap을 아예 보낸다.)
			mav.addObject("paraMap", paraMap); // view.jsp 에서 이전글제목 및 다음글제목 클릭시 사용하기 위해서 view 단에 보내주도록 한다.

			// 글 1개만 보여주는 것으로 끝나는 것이 아니라, 조회수가 증가해야 한다. (로그인여부, 내가 쓴 글 인지에 따라 조회수 증가여부 결정)
			HttpSession session = request.getSession();
			MemberVO loginuser = (MemberVO) session.getAttribute("loginuser");

			String login_userid = null; // 로그인을 하지 않았을 때 기본값 null
			if (loginuser != null) {
				// 사용자가 로그인을 했다면 로그인한 사용자의 ID 값을 얻어온다.
				login_userid = loginuser.getUserid(); // 로그인한 사용자의 ID 값
				// login_userid 변수는 로그인 된 사용자의 userid 이다.
			}
			paraMap.put("login_userid", login_userid);
			// == #68. *** 중요 *** (새로고침 시 무한조회수 증가 방지) == //
			// 글1개를 보여주는 페이지 요청은 select 와 함께
			// DML문(지금은 글조회수 증가인 update문)이 포함되어져 있다.
			// 이럴경우 웹브라우저에서 페이지 새로고침(F5)을 했을때 DML문이 실행되어
			// 매번 글조회수 증가가 발생한다.
			// 그래서 우리는 웹브라우저에서 페이지 새로고침(F5)을 했을때는
			// 단순히 select만 해주고 DML문(지금은 글조회수 증가인 update문)은
			// 실행하지 않도록 해주어야 한다. !!! === //

			// 위의 글목록보기 #69. 에서 session.setAttribute("readCountPermission", "yes"); 해두었다.
			BoardVO boardvo = null;
			if ("yes".equals(session.getAttribute("readCountPermission"))) {
				// 세션 정보와 같은지? (글목록보기를 클릭한 다음에 특정글을 조회해온 경우이다.)
				// 즉 위의 /list.action 를 거친 후 /view.action 로 왔을 경우이다.

				// DB 에 가서 글 내용을 얻어오자. (내가 읽고 싶은 글 번호와 글을 읽는 사용자(id)가 누구인지 알고싶다.(paraMap 에 담아
				// 놨음))
				boardvo = service.getView(paraMap); // 글 조회수 증가와 함께 글 1개를 조회해주는 것

				session.removeAttribute("readCountPermission"); // 세션을 없애야 한다.
				// 항상 #58.글목록보기 를 거쳐야 session 을 가져올 수 있다.
				// ** 중요함 ** 반드시 session 에 저장된 readCountPermission 을 삭제한다.
			} else {
				// 웹브라우저에서 새로고침(F5) 을 클릭한 경우이다.
				// 즉 위의 /list.action 를 거친 후 /view.action 로 **오지 않았을 경우**

				boardvo = service.getViewWithNoAddCount(paraMap);
				// 글 조회수 증가는 없고 단순히 글 1개 조회만을 해주는 것

			}

			mav.addObject("boardvo", boardvo); // request 영역에 담아줘야 한다. (view 단으로 보낸다.)
			// System.out.println("확인용 boardvo" + boardvo.getRegDate());

		} catch (NumberFormatException e) { // seq= 뒤에 숫자 외에 문자를 집어넣게 되면 exception 처리를 한다.
			// exception 처리 된 후에 ${empty requestScope.boardvo} 가 되고, 존재하지 않는 게시글입니다. 라는 문구를
			// 뜨게 한다.
		}

		mav.setViewName("board/view.tiles1");
		// view.jsp 로 이동
		return mav;
	}

	// ==== #71. 글 수정하는 페이지 요청
	// 글 수정은 로그인 해야만 할 수 있다. (PointCut(주업무) 대상, BoardAOP 에서 Pointcut 에 설정해둔대로 메소드명을
	// 짓는다.)
	@RequestMapping(value = "/edit.action") // URL, 절대경로 contextPath 인 board 뒤의 것들을 가져온다. (확장자.java 와 확장자.xml 은 그 앞에
											// contextPath 가 빠져있는 것이다.)
	public ModelAndView requiredLogin_edit(HttpServletRequest request, HttpServletResponse response, ModelAndView mav) {

		// 글 수정해야할 글번호 가져오기
		String seq = request.getParameter("seq");

		// 글 수정해야할 글 1개 내용을 가져오기.
		// 검색 기능을 위해 Map 에 담자.
		Map<String, String> paraMap = new HashMap<>();
		paraMap.put("seq", seq); // 수정해야할 글번호를 담는다.

		// 내가 쓴글만 수정해야 하므로, 글쓴이가 누구인지 알아야 한다.
		//////////////////////////////////
		paraMap.put("searchType", "");
		paraMap.put("searchWord", "");
		//////////////////////////////////

		BoardVO boardvo = service.getViewWithNoAddCount(paraMap); // 이미 글 내용을 가져온 것임. (글 내용 가져올 때 조회수 증가하지 않고 조회만 하도록 하는
																	// 메소드를 가져온다.)
		// 글조회수(readCount) 증가 없이 단순히 글 1개만 조회해주는 것이다.

		// 다른사람의 글을 수정 불가하다.
		HttpSession session = request.getSession();
		MemberVO loginuser = (MemberVO) session.getAttribute("loginuser");

		if (!loginuser.getUserid().equals(boardvo.getFk_userid())) {
			// 로그인한 사용자의 id 와 글을 작성한 사람의 id가 같지 않을 때 (둘다 String 이므로 equals() 로 한다.)
			String message = "다른 사용자의 글은 수정이 불가합니다.";
			String loc = "javascript:history.back()";

			mav.addObject("message", message);
			mav.addObject("loc", loc);
			mav.setViewName("msg");
		} else {
			// 로그인한 사용자 == 작성자 (자신의 글을 수정할 경우)
			// 가져온 1개 글을 글 수정할 form 이 있는 view 단으로 보낸다.

			mav.addObject("boardvo", boardvo); // view 단에 뿌려줄 내용들
			mav.setViewName("board/edit.tiles1"); // view 단으로 보내줘야 하므로 tiles 사용.
		}

		return mav;
	}

	// ==== #72. 글 수정 페이지 완료하기 (DB 에 UPDATE)
	@RequestMapping(value = "/editEnd.action", method = { RequestMethod.POST }) // URL, 절대경로 contextPath 인 board 뒤의 것들을
																				// 가져온다. (확장자.java 와 확장자.xml 은 그 앞에
																				// contextPath 가 빠져있는 것이다.)
	public ModelAndView editEnd(ModelAndView mav, BoardVO boardvo, HttpServletRequest request) {

		/*
		 * 글 암호가 같은지 다른지를 봐야 한다. (수정 전/후의 글암호) 글 수정을 하려면 원본글의 글암호와 수정시 입력한 암호가 일치할 때만 글
		 * 수정이 가능하도록 해야한다.
		 */

		int n = service.edit(boardvo); // boardvo 도 보내야 한다. (글제목,내용,암호,작성자 모두 포함되어있음)
		// return 타입은 0 아니면 1이므로 int 이다.
		// n 이 1이라면 정상적으로 글수정이 된 것이고
		// n이 0 이라면 글 수정에 필요한 글암호가 틀린 경우이다.
		// 오라클 SQL 에서 직접 입력해본다.

		if (n == 0) {
			// 글 수정에 필요한 글암호가 틀린 경우
			mav.addObject("message", "암호가 일치하지 않아 글 수정에 실패했습니다.");
			mav.addObject("loc", "javascript:history.back()");
		} else {
			// 정상적으로 글수정이 된 것 (본인이 쓴 글을 조회할 수 있도록 한다.)
			mav.addObject("message", "글 수정에 성공했습니다!!");
			mav.addObject("loc", request.getContextPath() + "/view.action?seq=" + boardvo.getSeq()); // 내가 쓴 글을 보여주도록
																										// 해야한다.
		}

		mav.setViewName("msg");

		return mav;

	}

	// ==== #76. 글 삭제 페이지 요청하기
	// 글 삭제도 로그인이 되어 있어야 한다. (PointCut(주업무) 대상)
	@RequestMapping(value = "/del.action") // URL, 절대경로 contextPath 인 board 뒤의 것들을 가져온다. (확장자.java 와 확장자.xml 은 그 앞에
											// contextPath 가 빠져있는 것이다.)
	public ModelAndView requiredLogin_del(HttpServletRequest request, HttpServletResponse response, ModelAndView mav) {

		// 삭제해야할 글번호 가져오기 (view 단에서 넘어옴)
		String seq = request.getParameter("seq");

		// 삭제 해야할 글 1개 내용 가져와서 로그인한 사람이 쓴 글이라면 글삭제가 가능하지만,
		// 다른 사람이 쓴 글은 삭제할 수 없도록 한다. (삭제 불가)
		// 검색 기능을 위해 Map 에 담자.
		Map<String, String> paraMap = new HashMap<>();
		paraMap.put("seq", seq); // 삭제 해야할 글번호를 담는다.

		// 내가 쓴글만 삭제해야 하므로, 글쓴이가 누구인지 알아야 한다.
		//////////////////////////////////
		paraMap.put("searchType", "");
		paraMap.put("searchWord", "");
		//////////////////////////////////

		BoardVO boardvo = service.getViewWithNoAddCount(paraMap); // 이미 글 내용을 가져온 것임. (글 내용 가져올 때 조회수 증가하지 않고 조회만 하도록 하는
																	// 메소드를 가져온다.)
		// 글조회수(readCount) 증가 없이 단순히 글 1개만 조회해주는 것이다.

		// 다른 사람의 글은 삭제 불가하다.
		HttpSession session = request.getSession();
		MemberVO loginuser = (MemberVO) session.getAttribute("loginuser");

		if (!loginuser.getUserid().equals(boardvo.getFk_userid())) {
			// 로그인한 사용자의 id 와 글을 작성한 사람의 id가 같지 않을 때 (둘다 String 이므로 equals() 로 한다.)
			String message = "다른 사용자의 글은 삭제가 불가합니다.";
			String loc = "javascript:history.back()";

			mav.addObject("message", message);
			mav.addObject("loc", loc);
			mav.setViewName("msg");
		} else {
			// 로그인한 사용자 == 작성자 (자신의 글을 삭제할 경우)
			// 가져온 1개 글을 삭제, 글 작성시 입력해준 글암호와 일치하는지 여부를 알아보도록 암호를 입력받아주는 페이지(del.jsp)를 띄우도록
			// 한다.

			mav.addObject("pw", boardvo.getPw()); // view 단에 뿌려줄 내용들 (원래 글 암호(DB 에 있음)를 view 단에 넘겨줘야 한다.)
			mav.addObject("seq", seq); // view 단에 뿌려줄 내용들 (삭제할 글번호도 view 단에 보내줘야 한다.)
			mav.setViewName("board/del.tiles1"); // view 단으로 보내줘야 하므로 tiles 사용.
		}

		return mav;
	}

	// ==== #77. 글 삭제 페이지 완료하기 (DB 에서 DELETE)
	@RequestMapping(value = "/delEnd.action", method = { RequestMethod.POST }) // URL, 절대경로 contextPath 인 board 뒤의 것들을
																				// 가져온다. (확장자.java 와 확장자.xml 은 그 앞에
																				// contextPath 가 빠져있는 것이다.)
	public ModelAndView requiredLogin_delEnd(HttpServletRequest request, HttpServletResponse response,
			ModelAndView mav) {

		String seq = request.getParameter("seq");

		Map<String, String> paraMap = new HashMap<>();
		paraMap.put("seq", seq); // 파일첨부를 위해 map 을 사용한 것 (추후 할 예정)

		/////////////////////////////////////////////////////////////////////////////////////
		// ==== #164. 파일첨부가 된 글이라면 글 삭제시 먼저 첨부파일을 삭제해주어야 한다. ==== //
		paraMap.put("searchType", "");
		paraMap.put("searchWord", "");

		BoardVO boardvo = service.getViewWithNoAddCount(paraMap); // 오류가 일어나지 않게끔 매퍼에 있는 searchType 과 searchWord 를 써줘야
																	// 한다. (필요가 없어도)
		String fileName = boardvo.getFileName();

		if (fileName != null && !"".equals(fileName)) {
			// 첨부파일이 저장되어 있는 WAS(톰캣)의 디스크 경로명을 알아와야만 다운로드를 해줄수 있다.
			// 이 경로는 우리가 파일첨부를 위해서 /addEnd.action 에서 설정해두었던 경로와 똑같아야 한다.
			// WAS 의 webapp 의 절대경로를 알아와야 한다.
			HttpSession session = request.getSession();
			String root = session.getServletContext().getRealPath("/"); // /webapp 이다.
			// System.out.println("**** 확인용 webapp 의 절대경로 "+ root);
			// **** 확인용 webapp 의 절대경로
			// C:\NCS\workspace(spring)\.metadata\.plugins\org.eclipse.wst.server.core\tmp0\wtpwebapps\Board\

			String path = root + "resources" + File.separator + "files"; // window('\') 나 linux 는 '/' 이다.
			/*
			 * File.separator 는 운영체제에서 사용하는 폴더와 파일의 구분자이다. 운영체제가 Windows 이라면 File.separator
			 * 는 "\" 이고, 운영체제가 UNIX, Linux 이라면 File.separator 는 "/" 이다.
			 */

			paraMap.put("path", path); // 삭제해야할 파일이 저장된 경로
			paraMap.put("fileName", fileName); // 삭제해야할 파일명

		}
		// ==== 파일첨부가 된 글이라면 글 삭제시 먼저 첨부파일을 삭제해주어야 한다. 끝 ==== //

		/////////////////////////////////////////////////////////////////////////////////////

		int n = service.del(paraMap); // 1 행 삭제. int 가 return 된다.

		if (n == 1) {
			mav.addObject("message", "글 삭제에 성공했습니다!!");
			mav.addObject("loc", request.getContextPath() + "/list.action"); // 내가 쓴 글을 보여주도록 해야한다.
		} else {
			mav.addObject("message", "글 삭제에 실패했습니다!!");
			mav.addObject("loc", "javascript:history.back()"); // 내가 쓴 글을 보여주도록 해야한다.
		}

		mav.setViewName("msg");

		return mav;
	}

	// 이전글, 다음글 클릭시 조회수 증가하도록 한다.
	@RequestMapping(value = "/view_2.action") // URL, 절대경로 contextPath 인 board 뒤의 것들을 가져온다. (확장자.java 와 확장자.xml 은 그 앞에
												// contextPath 가 빠져있는 것이다.)
	public ModelAndView view_2(ModelAndView mav, HttpServletRequest request) {

		getCurrentURL(request); // 로그인 또는 로그아웃을 했을 때 현재 보이던 그 페이지로 그대로 돌아가기 위한 메소드 호출

		// list.jsp 에서 seq 를 넘겨주었으므로 getParameter 를 해와야 한다. (?seq="+seq; 형식으로 넘겨옴.)
		// 조회하고자 하는 글번호 받아오기
		String seq = request.getParameter("seq");

		String searchType = request.getParameter("searchType");
		String searchWord = request.getParameter("searchWord");
		String gobackURL = request.getParameter("gobackURL");
		/*
		 * System.out.println("~~~~ view2 의 searchType : " + searchType);
		 * System.out.println("~~~~ view2 의 searchWord : " + searchWord);
		 * System.out.println("~~~~ view2 의 gobackURL : " + gobackURL);
		 */
		HttpSession session = request.getSession();
		session.setAttribute("readCountPermission", "yes"); // 정상적인 경로로 들어왔을 때, 조회수 증가를 허용한다. (새로고침 시 무한조회수 증가 방지)

		try {
			searchWord = URLEncoder.encode(searchWord, "UTF-8"); // 한글이 웹브라우저 주소창에서 사용되어질때 한글이 ? 처럼 안깨지게 하려고 하는 것임.
			gobackURL = URLEncoder.encode(gobackURL, "UTF-8"); // 한글이 웹브라우저 주소창에서 사용되어질때 한글이 ? 처럼 안깨지게 하려고 하는 것임.
			/*
			 * System.out.println("~~~~ view2 의 URLEncoder.encode(searchWord, \"UTF-8\") : "
			 * + searchWord);
			 * System.out.println("~~~~ view2 의 URLEncoder.encode(gobackURL, \"UTF-8\") : "
			 * + gobackURL);
			 * 
			 * System.out.println(URLDecoder.decode(searchWord, "UTF-8")); // URL인코딩 되어진 한글을
			 * 원래 한글모양으로 되돌려 주는 것임. System.out.println(URLDecoder.decode(gobackURL,
			 * "UTF-8")); // URL인코딩 되어진 한글을 원래 한글모양으로 되돌려 주는 것임.
			 */
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		// 위에서 UTF-8 로 바꿔준 것을 redirect 해줘야 한다. (그냥 보내버리게 되면 ?? 처리가 되기 때문이다.)
		mav.setViewName("redirect:/view.action?seq=" + seq + "&searchType=" + searchType + "&searchWord=" + searchWord
				+ "&gobackURL=" + gobackURL); // view.action 을 거쳐서 온다. (주소창에는 view_2 가 나오지 않는다.)
		// view_action 을 거쳐서 조회를 하므로 조회수 증가가 되고, 주소창에는 view_2.action 이 나타나지 않는다.

		return mav;
	}

	/*
	 * @ResponseBody 란? 메소드에 @ResponseBody Annotation이 되어 있으면 return 되는 값은 View 단
	 * 페이지를 통해서 출력되는 것이 아니라 return 되어지는 값 그 자체를 웹브라우저에 바로 직접 쓰여지게 하는 것이다. 일반적으로 JSON
	 * 값을 Return 할때 많이 사용된다.
	 * 
	 * >>> 스프링에서 json 또는 gson을 사용한 ajax 구현시 데이터를 화면에 출력해 줄때 한글로 된 데이터가 '?'로 출력되어 한글이
	 * 깨지는 현상이 있다. 이것을 해결하는 방법은 @RequestMapping 어노테이션의 속성 중
	 * produces="text/plain;charset=UTF-8" 를 사용하면 응답 페이지에 대한 UTF-8 인코딩이 가능하여 한글 깨짐을
	 * 방지 할 수 있다. <<<
	 */

	// ==== #84. 댓글 쓰기 (Ajax 로 처리)
	@ResponseBody // JSON 값을 웹페이지에 그냥 보여주기만 하면 되기 때문에 @ResponseBody 를 쓴다.
	@RequestMapping(value = "/addComment.action", method = {
			RequestMethod.POST }, produces = "text/plain;charset=UTF-8") // URL, 절대경로 contextPath 인 board 뒤의 것들을 가져온다.
																			// (확장자.java 와 확장자.xml 은 그 앞에 contextPath 가
																			// 빠져있는 것이다.)
	public String addComment(CommentVO commentvo) { // JSON 을 그대로 웹페이지에 보여줘야 하기 때문에 return 타입은 String 이다. ModelAndView 가
													// 아니다.
		// 댓글쓰기에 첨부파일이 없는 경우
		// view 단에서 보내오는 data 가 commentVO 에 있는 것과 같기 때문에 form 태그에 있는 값이 자동적으로 commentvo
		// 에 set 된다.
		// getParameter 한 값들이 commentvo 에 들어온다. (DB 에 보내줘야 한다.)

		// Transction 처리는 Service 에서 한다.
		int n = 0;

		try {
			n = service.addComment(commentvo);
		} catch (Throwable e) {
			e.printStackTrace();
		}

		// 댓글쓰기(insert) 및 원게시물(tbl_board 테이블)에 댓글의 개수 증가(update 1씩 증가)하기
		// 이어서 회원의 포인트를 50점을 증가하도록 한다. (tbl_member 테이블에 point 컬럼의 값을 50 증가하도록 update
		// 한다.)

		JSONObject jsonObj = new JSONObject();
		jsonObj.put("n", n);
		jsonObj.put("name", commentvo.getName());

		return jsonObj.toString(); // "{"n":1,"name":엄정화}" 또는 "{"n":0,"name":"김민정"}" jsonObj 에 put 한 결과들을 웹페이지에 그대로
									// 찍어주겠다.

	}

	// ==== #90. 원게시물에 달린 댓글을 조회해오기 (Ajax 로 처리)
	@ResponseBody
	@RequestMapping(value = "/readComment.action", method = {
			RequestMethod.GET }, produces = "text/plain;charset=UTF-8") // URL, 절대경로 contextPath 인 board 뒤의 것들을 가져온다.
																		// (확장자.java 와 확장자.xml 은 그 앞에 contextPath 가 빠져있는
																		// 것이다.)
	// 바로 찍어버리므로 리턴타입은 ModelAndView 가 아니라 String 이다.
	public String readComment(HttpServletRequest request) {
		// 받아올 글이 1개이므로 vo 를 쓸 필요가 없다. (json 으로 보내온 것을 받는다.)
		String parentSeq = request.getParameter("parentSeq");

		// 파라미터는 where 절인 parentSeq 이다. 복수개의 댓글을 읽어주므로 List 로 받는다.
		List<CommentVO> commentList = service.getCommentList(parentSeq);

		// JSON 으로 바꿔주자.
		JSONArray jsonArr = new JSONArray();

		if (commentList != null) {
			// DB 에서 가져온 댓글이 null 이 아니라면 (댓글 개수가 1개 이상이라면)
			// DB 에 select 된 것이 하나도 없다면 .size() 를 했을 때 nullPointerException 이 발생한다.
			// nullPointerException 이 발생하지 않도록 != null 로 해준다.

			for (CommentVO cmtvo : commentList) {

				JSONObject jsonObj = new JSONObject();
				// 댓글에 보여주고자 하는 것만 (view 단에) put 한다.
				jsonObj.put("name", cmtvo.getName()); // 댓글 작성자 (이름)
				jsonObj.put("content", cmtvo.getContent()); // 댓글 내용
				jsonObj.put("regDate", cmtvo.getRegDate()); // 댓글 작성일자

				jsonArr.put(jsonObj); // jsonArr에 넣고 view 단에서 보여주도록 한다.
			} // end of for------------------------------

		}

		return jsonArr.toString();

	}

	// === #108. 검색어 입력 시 자동글 완성하기 3 ===
	@ResponseBody
	@RequestMapping(value = "/wordSearchShow.action", method = {
			RequestMethod.GET }, produces = "text/plain;charset=UTF-8") // URL, 절대경로 contextPath 인 board 뒤의 것들을 가져온다.
																		// (확장자.java 와 확장자.xml 은 그 앞에 contextPath 가 빠져있는
																		// 것이다.)
	public String wordSearchShow(HttpServletRequest request) {

		String searchType = request.getParameter("searchType");
		String searchWord = request.getParameter("searchWord");

		Map<String, String> paraMap = new HashMap<>();
		paraMap.put("searchType", searchType);
		paraMap.put("searchWord", searchWord);

		List<String> wordList = service.wordSearchShow(paraMap); // paraMap 을 담아서 보낸다.

		JSONArray jsonArr = new JSONArray(); // 복수개로 받기 때문에 JSONArray , [] 타입

		if (wordList != null) {
			for (String word : wordList) {
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("word", word);

				jsonArr.put(jsonObj);
			} // end of for------------------------------------------------------------
		}

		return jsonArr.toString();
	}

	// === #128. 원게시물(부모글)에 딸린 댓글들을 페이징 처리해서 조회해오기(Ajax 로 처리) ===
	@ResponseBody
	@RequestMapping(value = "/commentList.action", method = {
			RequestMethod.GET }, produces = "text/plain;charset=UTF-8") // URL, 절대경로 contextPath 인 board 뒤의 것들을 가져온다.
																		// (확장자.java 와 확장자.xml 은 그 앞에 contextPath 가 빠져있는
																		// 것이다.)
	public String commentList(HttpServletRequest request) {

		String parentSeq = request.getParameter("parentSeq"); // 몇번글에 대한 것인지?
		String currentShowPageNo = request.getParameter("currentShowPageNo"); // 몇페이지를 볼 것인지?

		if (currentShowPageNo == null) {
			currentShowPageNo = "1"; // currentShowPageNo 가 null 일때, 1page 를 보여주도록 하겠다.
		}

		int sizePerPage = 5; // 한 페이지당 5개의 댓글을 보여줄 것이다. (원하는 대로 수 조정 가능)

		/*
		 * currentShowPage startRno endRno ------------------------------------------
		 * 1Page ==> 1 5 2Page ==> 6 10 3Page ==> 11 15 4Page ==> 16 20 .....
		 */

		int startRno = ((Integer.parseInt(currentShowPageNo) - 1) * sizePerPage) + 1;
		int endRno = startRno + sizePerPage - 1;

		Map<String, String> paraMap = new HashMap<>();
		paraMap.put("parentSeq", parentSeq);
		paraMap.put("startRno", String.valueOf(startRno));
		paraMap.put("endRno", String.valueOf(endRno));

		List<CommentVO> commentList = service.getCommentListPaging(paraMap);

		JSONArray jsonArr = new JSONArray(); // []

		if (commentList != null) {
			// 댓글이 있을때에만
			for (CommentVO cmtvo : commentList) {
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("content", cmtvo.getContent()); // 댓글내용
				jsonObj.put("name", cmtvo.getName()); // 댓글작성자이름
				jsonObj.put("regDate", cmtvo.getRegDate()); // 댓글 작성일자

				// 댓글 읽어오기에 있어서 첨부파일 기능을 넣은 경우 시작 (첨부파일이 없으면 할 필요 없다.)
				jsonObj.put("seq", cmtvo.getSeq()); // 댓글번호
				jsonObj.put("fileName", cmtvo.getFileName()); // 댓글내용
				jsonObj.put("orgFilename", cmtvo.getOrgFilename());
				jsonObj.put("fileSize", cmtvo.getFileSize());
				// 댓글 읽어오기에 있어서 첨부파일 기능을 넣은 경우 끝(첨부파일이 없으면 할 필요 없다.)

				jsonArr.put(jsonObj);
			} // end of for---------------------------

		}

		return jsonArr.toString(); // 담은 내용들을 String 으로 바꾸자.
	}

	// === #132. 원게시물(부모글)에 딸린 댓글의 totalPage 를 알아오기 (Ajax 로 처리) ===
	@ResponseBody
	@RequestMapping(value = "/getCommentTotalPage.action", method = { RequestMethod.GET }) // 숫자이므로 ,
																							// produces="text/plain;charset=UTF-8"
																							// 는 굳이 있을 필요가 없다.
	public String getCommentTotalPage(HttpServletRequest request) {

		String parentSeq = request.getParameter("parentSeq"); // 몇번글에 대한 것인지?
		String sizePerPage = request.getParameter("sizePerPage"); // 몇번글에 대한 것인지?

		Map<String, String> paraMap = new HashMap<>();
		paraMap.put("parentSeq", parentSeq);
		paraMap.put("sizePerPage", sizePerPage);

		// 원글 글번호(parentSeq)에 해당하는 댓글의 totalPage 수 알아오기
		int totalPage = service.getCommentTotalPage(paraMap);

		JSONObject jsonObj = new JSONObject();
		jsonObj.put("totalPage", totalPage); // {"totalPage":5}
		// aJax 로 보냄

		return jsonObj.toString();
	}

	// === #163. 첨부파일 다운로드 받기 ===
	@RequestMapping(value = "/download.action") // method 는 생략되었으므로 GET 방식
	public void requiredLogin_download(HttpServletRequest request, HttpServletResponse response) {
		// 파일 다운로드만 받기 때문에 return 타입은 없다.
		// 원글에 대한 첨부파일 다운로드 이므로, 원글번호를 알아와야 한다.
		// 로그인 하지 않은 사용자가 url 에 주소를 입력하고 바로 들어올 수도 있다. (파일 다운로드는 로그인 해야만 다운받을 수 있다. 로그인을
		// 했는지 안했는지 검사한다.)
		// 따라서 before,after 를 통해 로그인 유무를 검사해야한다.
		String seq = request.getParameter("seq"); // 첨부파일이 있는 글번호

		/*
		 * 첨부파일이 있는 글번호에서 202204291419371025088801698800.jpg 처럼 이러한 fileName 값을 DB 에서
		 * 가져와야 한다. 또한 orgFilename 값도 DB 에서 가져와야 한다.
		 */

		// service 에 보내기 위해 Map 을 하나 만들자. (글번호에 대한 seq 만 알아오면 되기 때문에 검색 search 관련은 value
		// 에는 ""를 써준다.)
		Map<String, String> paraMap = new HashMap<>();
		paraMap.put("searchType", "");
		paraMap.put("searchWord", "");
		paraMap.put("seq", seq);

		// HttpServletResponse response 객체는 넘어온 데이터를 조작해서 결과물을 나타내고자 할 때 쓰인다. (웹에 보여주도록
		// 하겠다.)
		response.setContentType("text/html; charset=UTF-8"); // content 타입을 셋팅한다.
		PrintWriter out = null;
		// out 은 웹브라우저에 기술하는 대상체라고 생각하자.

		try {
			Integer.parseInt(seq); // 글번호 자리에 숫자만 들어와야 함. url에 ~~~seq?=ㄱㄴㅇㄹㄴ 는 안됨
			BoardVO boardvo = service.getViewWithNoAddCount(paraMap); // return 타입은 boardVO (글 1개를 얻어옴)

			// 글은 존재하는데 파일이 존재하지 않는 경우(파일명이 존재하지 않는 것-->파일이 존재하지 않음)
			if (boardvo == null || (boardvo != null && boardvo.getFileName() == null)) {
				out = response.getWriter();
				// out 은 웹브라우저에 기술하는 대상체라고 생각하자.

				out.println(
						"<script type='text/javascript'>alert('존재하지 않는 글번호이거나 첨부파일이 없으므로 파일 다운로드가 불가합니다.'); history.back(); <script>");
				return; // 종료

			} else {
				// 모두 정상인 경우 (올바르게 된 경우. 로그인 한 상태에서 게시물 클릭 후 파일다운로드를 하려고 한다.- 정상적인 루트로 다운로드를 하려고
				// 한다. boardvo != null )
				// 우선 disk 에 먼저 가서 파일명을 알아온다
				String fileName = boardvo.getFileName();
				// 202204291419371025088801698800.jpg 이것이 바로 WAS(톰캣) 디스크에 저장된 파일명이다.

				String orgFilename = boardvo.getOrgFilename(); // 다운로드를 받을 때에는 orgName 으로 받아야 한다. (숫자로 된 파일명을 다운받을 순
																// 없으니..)
				// 쉐보레계기판.jpg

				// 첨부파일이 저장되어 있는 WAS(톰캣)의 디스크 경로명을 알아와야만 다운로드를 해줄수 있다.
				// 이 경로는 우리가 파일첨부를 위해서 /addEnd.action 에서 설정해두었던 경로와 똑같아야 한다.
				// WAS 의 webapp 의 절대경로를 알아와야 한다.
				HttpSession session = request.getSession();
				String root = session.getServletContext().getRealPath("/"); // /webapp 이다.
				// System.out.println("**** 확인용 webapp 의 절대경로 "+ root);
				// **** 확인용 webapp 의 절대경로
				// C:\NCS\workspace(spring)\.metadata\.plugins\org.eclipse.wst.server.core\tmp0\wtpwebapps\Board\

				String path = root + "resources" + File.separator + "files"; // window('\') 나 linux 는 '/' 이다.
				/*
				 * File.separator 는 운영체제에서 사용하는 폴더와 파일의 구분자이다. 운영체제가 Windows 이라면 File.separator
				 * 는 "\" 이고, 운영체제가 UNIX, Linux 이라면 File.separator 는 "/" 이다.
				 */

				// path 가 첨부파일이 저장될 WAS(톰캣)의 폴더가 된다. --> path 에 파일을 업로드 한다.
				// System.out.println("**** 확인용 path 의 절대경로 "+ path);
				// **** 확인용 path 의 절대경로
				// C:\NCS\workspace(spring)\.metadata\.plugins\org.eclipse.wst.server.core\tmp0\wtpwebapps\Board\resources\files

				// **** file 다운로드 하기 **** // 경로명을 알아왔으니 파일을 다운로드 받자.
				// fileManager 에서 파일다운로드 하기 부분을 참고하자. (의존객체 DI)
				boolean flag = false;
				flag = fileManager.doFileDownload(fileName, orgFilename, path, response);
				// fileName : 저장된 파일명, orgFilename : 다운로드 받을 때 필요 , path : 저장된 경로, response :
				// 파라미터에 존재)
				// 파일 다운로드 성공시 flag는 true, 실패하면 flag는 false를 가진다.

				if (!flag) {
					// 다운로드가 실패할 경우 메시지를 띄워준다. (파일이 깨졌든지 등등..)
					out = response.getWriter();
					// out 은 웹브라우저에 기술하는 대상체라고 생각하자.

					out.println("<script type='text/javascript'>alert('파일 다운로드에 실패했습니다.'); history.back(); <script>");
				}

			}

		} catch (NumberFormatException | IOException e) {
			// 숫자 이외의 것이 들어왔을 때 대비해서 예외처리 / 입출력 예외처리
			try {
				out = response.getWriter();
				// out 은 웹브라우저에 기술하는 대상체라고 생각하자.

				out.println("<script type='text/javascript'>alert('파일 다운로드가 불가합니다.'); history.back(); <script>");
			} catch (IOException e1) {
				e.printStackTrace();
			}
		}

	}

	// === #168. 스마트에디터. 드래그앤드롭을 사용한 다중사진 파일 업로드 === //
	// attach_photo.js 파일의 337번 라인 참고할 것
	@RequestMapping(value = "/image/multiplePhotoUpload.action", method = { RequestMethod.POST }) // 사진 첨부는 POST 방식으로 할
																									// 것.
	public void multiplePhotoUpload(HttpServletRequest request, HttpServletResponse response) {
		/*
		 * 1. 사용자가 보낸 파일을 WAS(톰캣)의 특정 폴더에 저장해주어야 한다. >>>> 파일이 업로드 되어질 특정 경로(폴더)지정해주기
		 * 
		 * 우리는 WAS 의 webapp/resources/photo_upload 라는 폴더로 지정해준다.
		 */
		// WAS 의 webapp 의 절대경로를 알아와야 한다.
		HttpSession session = request.getSession();
		String root = session.getServletContext().getRealPath("/"); // /webapp 이다.

		String path = root + "resources" + File.separator + "photo_upload";
		// path 는 첨부파일들을 저장할 WAS(톰캣)의 폴더가 된다.

		// System.out.println("**** 확인용 path 의 절대경로 "+ path);
		// **** 확인용 path 의 절대경로
		// C:\NCS\workspace(spring)\.metadata\.plugins\org.eclipse.wst.server.core\tmp0\wtpwebapps\Board\resources\photo_upload

		File dir = new File(path);
		if (!dir.exists()) {
			// 해당 폴더가 없다면 만들도록 한다.
			dir.mkdirs(); // s 까지 붙는 것은 sub 폴더까지 만들라는 것.
		}

		try {

			// 해당 경로에 파일을 올리도록 하자.
			String filename = request.getHeader("file-name"); // 파일명(문자열)을 받는다 - 일반 원본파일명
			// 네이버 스마트에디터를 사용한 파일업로드시 싱글파일업로드와는 다르게 멀티파일업로드는 파일명이 header 속에 담겨져 넘어오게 되어있다.

			/*
			 * [참고] HttpServletRequest의 getHeader() 메소드를 통해 클라이언트 사용자의 정보를 알아올 수 있다.
			 * 
			 * request.getHeader("referer"); // 접속 경로(이전 URL)
			 * request.getHeader("user-agent"); // 클라이언트 사용자의 시스템 정보
			 * request.getHeader("User-Agent"); // 클라이언트 브라우저 정보
			 * request.getHeader("X-Forwarded-For"); // 클라이언트 ip 주소
			 * request.getHeader("host"); // Host 네임 예: 로컬 환경일 경우 ==> localhost:9090
			 */

			// System.out.println("**** 확인용 filename : " + filename );
			// **** 확인용 filename : k5_1.png
			// **** 확인용 filename :
			// berkelekle%EB%8B%A8%EA%B0%80%EB%9D%BC%ED%8F%AC%EC%9D%B8%ED%8A%B803.jpg (한글은
			// UTF-8로 변경되어 나온다.)

			InputStream is = request.getInputStream(); // is 는 스마트에디터를 사용하여 사진 첨부하기 된 이미지 파일이다.

			String newFilename = fileManager.doFileUpload(is, filename, path);
			// 디스크에 파일만 올라간 상태이다. 이제 웹페이지에 보여주도록 해야한다.

			String ctxPath = request.getContextPath(); // /board

			// 파일이 올라간 곳을 잡아주도록 한다.
			String strURL = "";
			strURL += "&bNewLine=true&sFileName=" + newFilename; // photo_upload 에 올라간 파일명
			strURL += "&sWidth=50"; // width 는 본인이 원하는 대로 설정
			strURL += "&sFileURL=" + ctxPath + "/resources/photo_upload/" + newFilename; // 절대경로(/board)/본인이 업로드 한
																							// 폴더명/업로드된 파일명

			// 웹브라우저상에 사진 이미지를 쓰기 (파일을 업로드한 후 웹페이지에 보여주도록 하자.)
			PrintWriter out = response.getWriter();
			out.print(strURL);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// ==== #170. 파일첨부가 있는 댓글쓰기 (Ajax 로 처리)
	@ResponseBody // JSON 값을 웹페이지에 그냥 보여주기만 하면 되기 때문에 @ResponseBody 를 쓴다.
	@RequestMapping(value = "/addComment_withAttach.action", method = {
			RequestMethod.POST }, produces = "text/plain;charset=UTF-8") // URL, 절대경로 contextPath 인 board 뒤의 것들을 가져온다.
																			// (확장자.java 와 확장자.xml 은 그 앞에 contextPath 가
																			// 빠져있는 것이다.)
	public String addComment_withAttach(CommentVO commentvo, MultipartHttpServletRequest mrequest) { // JSON 을 그대로 웹페이지에
																										// 보여줘야 하기 때문에
																										// return 타입은
																										// String 이다.
																										// ModelAndView
																										// 가 아니다.
		// 댓글쓰기에 첨부파일이 있는 경우
		// view 단에서 보내오는 data 가 commentVO 에 있는 것과 같기 때문에 form 태그에 있는 값이 자동적으로 commentvo
		// 에 set 된다.
		// getParameter 한 값들이 commentvo 에 들어온다. (DB 에 보내줘야 한다.)
		// 파일첨부가 있으므로 MultipartHttpServletRequest 가 필요하다.

		// ==== !!!! 첨부파일 올리기 시작 !!!! ==== //
		MultipartFile attach = commentvo.getAttach(); // attach 가 첨부파일 이다. (실제 파일)

		if (!attach.isEmpty()) { // 내용물이 없으면 true, 있으면 false
			// attach(첨부파일)가 비어 있지 않으면 (즉, 첨부 파일이 있는 경우라면) 업로드 해야 한다.
			/*
			 * 1. 사용자가 보낸 첨부파일을 WAS(톰캣)의 특정 폴더에 저장해주어야 한다. >>> 파일이 업로드 되어질 특정 경로(폴더)지정해주기
			 * 우리는 WAS의 webapp/resources/files 라는 폴더로 지정해준다. 조심할 것은 Package Explorer 에서
			 * files 라는 폴더를 만드는 것이 아니다.
			 */
			// WAS 의 webapp 의 절대경로를 알아와야 한다.
			HttpSession session = mrequest.getSession();
			String root = session.getServletContext().getRealPath("/"); // /webapp 이다.
			// System.out.println("**** 확인용 webapp 의 절대경로 "+ root);
			// **** 확인용 webapp 의 절대경로
			// C:\NCS\workspace(spring)\.metadata\.plugins\org.eclipse.wst.server.core\tmp0\wtpwebapps\Board\

			String path = root + "resources" + File.separator + "files"; // window('\') 나 linux 는 '/' 이다.
			/*
			 * File.separator 는 운영체제에서 사용하는 폴더와 파일의 구분자이다. 운영체제가 Windows 이라면 File.separator
			 * 는 "\" 이고, 운영체제가 UNIX, Linux 이라면 File.separator 는 "/" 이다.
			 */

			// path 가 첨부파일이 저장될 WAS(톰캣)의 폴더가 된다. --> path 에 파일을 업로드 한다.
			// System.out.println("**** 확인용 path 의 절대경로 "+ path);
			// **** 확인용 path 의 절대경로
			// C:\NCS\workspace(spring)\.metadata\.plugins\org.eclipse.wst.server.core\tmp0\wtpwebapps\Board\resources\files

			/*
			 * 2. 파일 첨부를 위한 변수의 설정 및 값을 초기화 한 후 파일 올리기
			 */
			String newFileName = "";
			// WAS(톰캣)의 디스크에 저장될 파일명

			// 내용물을 읽어오도록 하자. // 배열단위로 올라가야 한다. (1byte 씩 올라가면 안됨..)
			byte[] bytes = null; // bytes 가 null 이라면 내용물이 없다는 것이다.
			// 첨부파일의 내용물을 담는 것, return 타입은 byte 의 배열

			long fileSize = 0; // 첨부파일의 크기

			try {
				bytes = attach.getBytes(); // 파일에서 내용물을 꺼내오자. 파일을 올렸을 때 깨진파일이 있다면 (입출력이 안된다!!) 그때 Exception 을 thorws 한다.
				// 첨부파일의 내용물을 읽어오는 것. 그 다음, 첨부한 파일의 파일명을 알아와야 DB 에 넣을 수가 있다. 그러므로 파일명을 알아오도록 하자.
				// 즉 파일을 올리고 성공해야 - 내용물을 읽어올 수 있고 - 파일명을 알아와서 DB 에 넣을 수가 있다.

				String originalFilename = attach.getOriginalFilename();
				// attach.getOriginalFilename() 이 첨부파일의 파일명(예: 강아지.png) 이다.
				// System.out.println("***** 확인용 originalFilename : " + originalFilename);
				// ***** 확인용 originalFilename : ScheduleService.java

				// 의존객체인 FileManager 를 불러온다. (String 타입으로 return 함.)
				// 리턴값 : 서버에 저장된 새로운 파일명(예: 2022042912181535243254235235234.png)
				newFileName = fileManager.doFileUpload(bytes, originalFilename, path);
				// 첨부되어진 파일을 업로드 하도록 하는 것이다.

				// System.out.println("*** 확인용 newFileName : "+newFileName);
				// *** 확인용 newFileName : 202204291228131018404583262100.jpg
				// *** 확인용 newFileName : 202204291232341018665636132600.jpg
				// 같은 1.jpg 파일을 올려도 위와 같은 이름으로 파일이 업로드 된다.

				// 파일을 받아와야지만 service 에 보낼 수가 있다. (DB 에 보내주도록 하자.)

				/*
				 * 3. CommentVO commentvo 에 fileName 값과 orgFilename 값과 fileSize 값을 넣어주기 (form
				 * 태그에 name="attach" 하나밖에 없으므로 자동적으로 set 되는 것이 아니다. 그러므로 이름을 따로 set해줘야 한다.)
				 */
				commentvo.setFileName(newFileName);
				// WAS(톰캣)에 저장될 파일명(2022042912181535243254235235234.png)
				commentvo.setOrgFilename(originalFilename);
				// 게시판 페이지에서 첨부된 파일(강아지.png)을 보여줄 때 사용.
				// 또한 사용자가 파일을 다운로드 할때 사용되어지는 파일명으로 사용.
				fileSize = attach.getSize(); // 첨부파일의 크기 (단위는 byte 임)
				commentvo.setFileSize(String.valueOf(fileSize)); // 첨부파일의 size 를 알아오기 위함. (vo 에 String 으로 해왔기 때문에 바꿔준다.)

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		// ==== !!!! 첨부파일 올리기 끝 !!!! ==== //

		// Transaction 처리는 Service 에서 한다.
		int n = 0;

		try {
			n = service.addComment(commentvo);
		} catch (Throwable e) {
			e.printStackTrace();
		}

		// 댓글쓰기(insert) 및 원게시물(tbl_board 테이블)에 댓글의 개수 증가(update 1씩 증가)하기
		// 이어서 회원의 포인트를 50점을 증가하도록 한다. (tbl_member 테이블에 point 컬럼의 값을 50 증가하도록 update
		// 한다.)

		JSONObject jsonObj = new JSONObject();
		jsonObj.put("n", n);
		jsonObj.put("name", commentvo.getName());

		return jsonObj.toString(); // "{"n":1,"name":엄정화}" 또는 "{"n":0,"name":"김민정"}" jsonObj 에 put 한 결과들을 웹페이지에 그대로
									// 찍어주겠다.

	}

	// ==== #171. 파일첨부가 있는 댓글쓰기에서 파일 다운로드 받기 ==== //
	@RequestMapping(value = "/downloadComment.action") // method 는 생략되었으므로 GET 방식
	public void requiredLogin_downloadComment(HttpServletRequest request, HttpServletResponse response) {
		// 파일 다운로드만 받기 때문에 return 타입은 없다.
		// 원글에 대한 첨부파일 다운로드 이므로, 원글번호를 알아와야 한다.
		// 로그인 하지 않은 사용자가 url 에 주소를 입력하고 바로 들어올 수도 있다. (파일 다운로드는 로그인 해야만 다운받을 수 있다. 로그인을
		// 했는지 안했는지 검사한다.)
		// 따라서 before,after 를 통해 로그인 유무를 검사해야한다.
		String seq = request.getParameter("seq"); // 첨부파일이 있는 글번호

		/*
		 * 첨부파일이 있는 글번호에서 202204291419371025088801698800.jpg 처럼 이러한 fileName 값을 DB 에서
		 * 가져와야 한다. 또한 orgFilename 값도 DB 에서 가져와야 한다.
		 */

		// HttpServletResponse response 객체는 넘어온 데이터를 조작해서 결과물을 나타내고자 할 때 쓰인다. (웹에 보여주도록
		// 하겠다.)
		response.setContentType("text/html; charset=UTF-8"); // content 타입을 셋팅한다.
		PrintWriter out = null;
		// out 은 웹브라우저에 기술하는 대상체라고 생각하자.

		try {
			Integer.parseInt(seq); // 글번호 자리에 숫자만 들어와야 함. url에 ~~~seq?=ㄱㄴㅇㄹㄴ 는 안됨
			CommentVO commentvo = service.getCommentOne(seq); // 댓글 1개만 읽어온다.

			// 글은 존재하는데 파일이 존재하지 않는 경우(파일명이 존재하지 않는 것-->파일이 존재하지 않음)
			if (commentvo == null || (commentvo != null && commentvo.getFileName() == null)) {
				out = response.getWriter();
				// out 은 웹브라우저에 기술하는 대상체라고 생각하자.

				out.println(
						"<script type='text/javascript'>alert('존재하지 않는 댓글번호이거나 첨부파일이 없으므로 파일 다운로드가 불가합니다.'); history.back(); <script>");
				return; // 종료

			} else {
				// 모두 정상인 경우 (올바르게 된 경우. 로그인 한 상태에서 게시물 클릭 후 파일다운로드를 하려고 한다.- 정상적인 루트로 다운로드를 하려고
				// 한다. boardvo != null )
				// 우선 disk 에 먼저 가서 파일명을 알아온다
				String fileName = commentvo.getFileName();
				// 202204291419371025088801698800.jpg 이것이 바로 WAS(톰캣) 디스크에 저장된 파일명이다.

				String orgFilename = commentvo.getOrgFilename(); // 다운로드를 받을 때에는 orgName 으로 받아야 한다. (숫자로 된 파일명을 다운받을 순
																	// 없으니..)
				// 쉐보레계기판.jpg

				// 첨부파일이 저장되어 있는 WAS(톰캣)의 디스크 경로명을 알아와야만 다운로드를 해줄수 있다.
				// 이 경로는 우리가 파일첨부를 위해서 /addEnd.action 에서 설정해두었던 경로와 똑같아야 한다.
				// WAS 의 webapp 의 절대경로를 알아와야 한다.
				HttpSession session = request.getSession();
				String root = session.getServletContext().getRealPath("/"); // /webapp 이다.
				// System.out.println("**** 확인용 webapp 의 절대경로 "+ root);
				// **** 확인용 webapp 의 절대경로
				// C:\NCS\workspace(spring)\.metadata\.plugins\org.eclipse.wst.server.core\tmp0\wtpwebapps\Board\

				String path = root + "resources" + File.separator + "files"; // window('\') 나 linux 는 '/' 이다.
				/*
				 * File.separator 는 운영체제에서 사용하는 폴더와 파일의 구분자이다. 운영체제가 Windows 이라면 File.separator
				 * 는 "\" 이고, 운영체제가 UNIX, Linux 이라면 File.separator 는 "/" 이다.
				 */

				// path 가 첨부파일이 저장될 WAS(톰캣)의 폴더가 된다. --> path 에 파일을 업로드 한다.
				// System.out.println("**** 확인용 path 의 절대경로 "+ path);
				// **** 확인용 path 의 절대경로
				// C:\NCS\workspace(spring)\.metadata\.plugins\org.eclipse.wst.server.core\tmp0\wtpwebapps\Board\resources\files

				// **** file 다운로드 하기 **** // 경로명을 알아왔으니 파일을 다운로드 받자.
				// fileManager 에서 파일다운로드 하기 부분을 참고하자. (의존객체 DI)
				boolean flag = false;
				flag = fileManager.doFileDownload(fileName, orgFilename, path, response);
				// fileName : 저장된 파일명, orgFilename : 다운로드 받을 때 필요 , path : 저장된 경로, response :
				// 파라미터에 존재)
				// 파일 다운로드 성공시 flag는 true, 실패하면 flag는 false를 가진다.

				if (!flag) {
					// 다운로드가 실패할 경우 메시지를 띄워준다. (파일이 깨졌든지 등등..)
					out = response.getWriter();
					// out 은 웹브라우저에 기술하는 대상체라고 생각하자.

					out.println("<script type='text/javascript'>alert('파일 다운로드에 실패했습니다.'); history.back(); <script>");
				}

			}

		} catch (NumberFormatException | IOException e) {
			// 숫자 이외의 것이 들어왔을 때 대비해서 예외처리 / 입출력 예외처리
			try {
				out = response.getWriter();
				// out 은 웹브라우저에 기술하는 대상체라고 생각하자.

				out.println("<script type='text/javascript'>alert('파일 다운로드가 불가합니다.'); history.back(); <script>");
			} catch (IOException e1) {
				e.printStackTrace();
			}
		}

	}

	// ==== #182. Spring Scheduler(스프링 스케줄러2)를 사용하여 특정 URL 사이트로 연결하기
	@RequestMapping(value = "/branchTimeAlarm.action")
	public ModelAndView branchTimeAlarm(ModelAndView mav, HttpServletRequest request) {

		// alert 만 띄우도록 한다.
		String message = "12시 50분입니다. 점심 맛있게 드세요!";
		String loc = request.getContextPath() + "/index.action";

		mav.addObject("message", message);
		mav.addObject("loc", loc);

		// alert 만 띄울 것이므로 tiles 가 아니다.
		mav.setViewName("msg"); // /WEB-INF/views/msg.jsp

		return mav;
	}

	// ==== #194. 웹채팅 관련4 (localhost 가 아니라 IP 를 정확하게 알아와야 한다.)
	@RequestMapping(value = "/chatting/multichat.action", method = { RequestMethod.GET }) // 맨 처음에는 대화를 해야하기 때문에 GET
																							// 방식으로 한다.
	public String requiredLogin_multichat(HttpServletRequest request, HttpServletResponse response) {
		// 채팅을 하면 ModelandView 도 괜찮지만, String 을 사용해도 괜찮다.
		// 채팅은 로그인 한 사람만 가능하도록 한다. (직원만 사용 가능) --> boardAOP 사용한다.

		return "chatting/multichat.tiles1";
	}

	
	///////////////////// === 인터셉터 연습 시작 === ///////////////////////////////////////////////
	@RequestMapping(value="/anyone/anyone_a.action")
	public String anyone_a() {
	
	return "interceptor_test/anyone/anyone_a.tiles1";
	}
	
	@RequestMapping(value="/anyone/anyone_b.action")
	public String anyone_b() {
	
	return "interceptor_test/anyone/anyone_b.tiles1";
	}
	
	@RequestMapping(value="/member_only/member_a.action")
	public String member_a() {
	
	return "interceptor_test/member/member_a.tiles1";
	}
	
	@RequestMapping(value="/member_only/member_b.action")
	public String member_b() {
	
	return "interceptor_test/member/member_b.tiles1";
	}
	
	@RequestMapping(value="/special_member/special_member_a.action")
	public String special_member_a() {
	
	return "interceptor_test/special_member/special_member_a.tiles1";
	}
	
	@RequestMapping(value="/special_member/special_member_b.action")
	public String special_member_b() {
	
	return "interceptor_test/special_member/special_member_b.tiles1";
	}
	///////////////////// === 인터셉터 연습 끝 === ///////////////////////////////////////////////
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////

	// === 로그인 또는 로그아웃을 했을 때 현재 보이던 그 페이지로 그대로 돌아가기 위한 메소드 생성 === //
	public void getCurrentURL(HttpServletRequest request) {
		HttpSession session = request.getSession();
		session.setAttribute("goBackURL", MyUtil.getCurrnetURL(request)); // myUtil 에서 만들어 놓은 getCurrnetURL 메소드를 가져온다.
		// myUtil 의 return 값인 currentURL이 넘어오게 된다.

	}

	///////////////////////////////////////////////////////////////////////////////////////////////

}
