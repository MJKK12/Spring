package com.spring.board.service;

import java.awt.Desktop;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.spring.board.common.AES256;
import com.spring.board.common.FileManager;
import com.spring.board.common.GoogleMail;
import com.spring.board.model.*;

//=== #31. Service 선언 === 
//트랜잭션 처리를 담당하는곳 , 업무를 처리하는 곳, 비지니스(Business)단
@Service // DB 의 트랜잭션 처리를 할 수 있다.
public class BoardService implements InterBoardService {

	// === #34. 의존객체 주입하기(DI: Dependency Injection) ===
	// DAO 필드를 하나 만들도록 한다.
	@Autowired	// Spring Container BoardDAO 가 bean 으로 올라가도록 해준다. Type에 따라 알아서 Bean 을 주입해준다.
	private InterBoardDAO dao;	// null 이 아니도록 바꿔준다. 
	// Type 에 따라 Spring 컨테이너가 알아서 bean 으로 등록된 com.spring.board.model.BoardDAO 의 bean 을  dao 에 주입시켜준다. 
    // 그러므로 dao 는 null 이 아니다.	

	
	// === #45. 양방향 암호화 알고리즘인 AES256 를 사용하여 복호화 하기 위한 클래스 의존객체 주입하기(DI: Dependency Injection) ===
	@Autowired
	private AES256 aes;	// service 에서 사용하기 위해 넣어준 것이다. (servlet-context.xml 에 bean 등록 해놓음.)
	// Type 에 따라 Spring 컨테이너가 알아서 bean 으로 등록된 com.spring.board.common.AES256 의 bean 을  aes 에 주입시켜준다. 
    // 그러므로 aes 는 null 이 아니다.
	// com.spring.board.common.AES256 의 bean 은 /webapp/WEB-INF/spring/appServlet/servlet-context.xml 파일에서 bean 으로 등록시켜주었음.  
	
	@Autowired	// Spring Container BoardDAO 가 bean 으로 올라가도록 해준다. Type에 따라 알아서 Bean 을 주입해준다.
	private FileManager filemanager;	// null 이 아니도록 바꿔준다. FileManager 가 @Component 로써 bean 으로 올라가있기 때문에 쓸 수 있다.
    
	//==== #187. Spring Scheduler(스프링 스케줄러7) ==== //
	//=== Spring Scheduler(스프링 스케줄러)를 사용한 email 발송하기 (특정시간에 발송) ===	      
    // bean 으로 된 GoogleMail 을 @Autowired 해준다.
	@Autowired	// Spring Container BoardDAO 가 bean 으로 올라가도록 해준다. Type에 따라 알아서 Bean 을 주입해준다.
	private GoogleMail mail;	// null 이 아니도록 바꿔준다. FileManager 가 @Component 로써 bean 으로 올라가있기 때문에 쓸 수 있다.
    
	
	// insert 메소드
	@Override
	public int test_insert() {	// DB 에 넣어줘야 한다.
		int n = dao.test_insert();	// DB 에 insert 해줘야 한다. n 을 서비스단에 넘겨준다.
		// 넘어온 n 값을 Controller 에 넘겨준다.
		return n;
	}

	// select 메소드
	@Override
	public List<TestVO> test_select() {

		// 의존객체인 dao 로 간다. (위의 @Autowired 를 참고한다.)
		List<TestVO> testvoList = dao.test_select();
		
		return testvoList;
	}

	// 메소드의 오버로딩 (첫번째 test_insert() 와 이름은 같지만 파라미터가 다르다.)
	@Override
	public int test_insert(Map<String, String> paraMap) {

		// form 태그에서 입력받은 것을 insert 해주고자 한다.
		int n = dao.test_insert(paraMap);	// 의존객체인 dao
		
		return n;
	}


	@Override
	public int test_insert(TestVO vo) {
		int n = dao.test_insert(vo);	// 의존객체인 dao
		return n;
	}

	//////////////////////////////////////////////////////////////////////////
	
	// ** 게시판 만들기 ** //
	
	// === #37. 시작페이지에서 메인이미지를 보여주는 것 (캐러셀) === 
	@Override
	public List<String> getImgfilenameList() {

		// DAO 로 이동하자. (의존객체 dao 사용)
		List<String> imgfilenameList = dao.getImgfilenameList();
		
		return imgfilenameList;
	}

	// #42. 로그인 처리하기 
	@Override
	public MemberVO getLoginMember(Map<String, String> paraMap) {
		
		 MemberVO loginuser = dao.getLoginMember(paraMap);	// return 값은 한 사람.
		// === #48. aes 의존객체를 사용하여 로그인 되어진 사용자(loginuser)의 이메일 값을 복호화 하도록 한다. === 
	    //          또한 암호변경 메시지와 휴면처리 유무 메시지를 띄우도록 업무처리를 하도록 한다.
		// 			DB 에 암호화 되어있는 것을 복호화 해야 한다. (AES256 이 필요하다.)
		// 			이메일, 휴대폰은 복호화 해서 넘겨줘야 한다.
		
		// DB 테이블에서 읽어온 값을 생각하면서 코드를 짤 것.
		if(loginuser != null && loginuser.getPwdchangegap() >= 3 ) {		// 이메일, 핸드폰번호가 존재할 때에만 복호화
			// 마지막으로 암호를 변경한 날짜가 현재시각으로부터 3개월이 지났으면
			loginuser.setRequirePwdChange(true);	// MemberVO 참고, Mapper(board.xml) 에서 select 된 것 그대로 set 을 해왔기 때문에, Serviece 에서 get 을 해온것이다.
			// 로그인시 암호를 변경하라는 alert 를 띄우도록 한다.
			
		}
		
		if(loginuser != null && loginuser.getLastlogingap() >= 12) {	// 마지막으로 로그인 한지가 언제인지
			// 마지막으로 로그인한 시각이 현재 시각으로부터 1년이 지났으면 휴면으로 지정한다.
			loginuser.setIdle(1);	// default 가 0인데, 휴면계정이므로 1로 바꾼다.
			// idle 을 1로 바꾸었으니, DB 에서 0 --> 1 로 update 해줘야 한다.
			
			// === tbl_member 테이블의 idle 컬럼의 값을 1로 변경하기 === //
			int n = dao.updateIdle(paraMap.get("userid"));	// 고유한 값인 userid 가 넘어가야 한다. (Controller 에서 가져온다.)
			
		}
		
		if(loginuser != null) {
			// 로그인을 했다면, 암호화 된 상태로 email 이 DB 에 담겨있다.
			String email = "";		
			 try {
				email = aes.decrypt(loginuser.getEmail());		// 암호화 되어있는 email 을 복호화 한다.	
			} catch (UnsupportedEncodingException | GeneralSecurityException e) {
				e.printStackTrace();
			}	
			 
			 loginuser.setEmail(email);		// dao 를 통해 가져온 email 이 DB 에서는 암호화 되어있는 것을 다시 복호화해서 email 에 넣어서 보내준 후 Controller 로 보낸다.
		}
		
		return loginuser;
	}

	// ==== #55. 글쓰기(파일 첨부가 없는 글쓰기) ====
	@Override
	public int add(BoardVO boardvo) {

		// 원글쓰기인지 답변글쓰기인지 구분해야 한다.
		// === #144. 글쓰기가 원글쓰기인지 아니면 답변글쓰기인지를 구분하여 
		//           tbl_board 테이블에 insert 를 해주어야 한다.
		//           원글쓰기 이라면 tbl_board 테이블의 groupno 컬럼의 값은 
		//           groupno 컬럼의 최대값(max)+1 로 해서 insert 해야하고,
		//           답변글쓰기 이라면 넘겨받은 값(boardvo)을 그대로 insert 해주어야 한다.
		
		// === 원글쓰기인지, 답변글쓰기인지 구분하기 시작 === //
		if( "".equals(boardvo.getFk_seq() ) ) {
			// 원글쓰기인 경우
			// groupno 컬럼의 값은 groupno 컬럼의 최대값(max)+1 로 해야한다.
			int groupno = dao.getGroupnoMax() + 1;
			boardvo.setGroupno(String.valueOf(groupno));
		}		
		// === 원글쓰기인지, 답변글쓰기인지 구분하기 끝 === //
		
		int n = dao.add(boardvo);
		return n;
	}

	// === #59. 페이징 처리를 안한 검색어가 없는 전체 글목록 보여주기 === //
	@Override
	public List<BoardVO> boardListNoSearch() {

		List<BoardVO> boardList = dao.boardListNoSearch();
		
		return boardList;
	}

	// === #63. 글 조회수 증가와 함께 글 1개를 조회해주는 것
	// ** 글 1개 조회하기와 글 조회수 1 증가하기는 한 세트이다. **
	// 먼저 로그인을 한 상태에서 다른 사람의 글을 조회할 경우 글 조회수 컬럼의 값을 1을 증가 시켜야한다.
	@Override
	public BoardVO getView(Map<String, String> paraMap) {

		BoardVO boardvo = dao.getView(paraMap);	// DB 에 paraMap 을 주어서 요청한다. (글 1개 조회하기)
		
		// 글 1개 조회시 글쓴이가 누군지 view 단에 찍힌다.
		// 경우의수를 나눈다. (로그인을 하지 않고 && 글쓴이 == 글읽는이 조회수 증가 X, 로그인을 했으며 해당 글을 읽는 사람과 글쓴이가 다를경우 조회수 증가 O)
		
		String login_userid = paraMap.get("login_userid");	
		// paraMap.get("login_userid") 은 로그인을 한 상태라면 로그인한 사용자의 userid 이고,
		// 로그인을 하지 않은 상태라면 paraMap.get("login_userid") 은 null 이다.
		
		if(login_userid != null && 
		   boardvo != null &&
		   !login_userid.equals(boardvo.getFk_userid())) {
		// 글조회수 증가는 로그인을 한 상태에서 다른사람의 글을 읽을때만 증가(update) 하도록 한다.
		
			dao.setAddReadCount(boardvo.getSeq());	// 글 조회수 1 증가 하기 (게시판VO 에서 seq 를 가져온다.)
			boardvo = dao.getView(paraMap);			// 글 1개 조회하기 (즉, 글 1개를 가지고 온 다음에, Map 에 담긴 login 된 userid 를 가져와서 조회하기)
		}
		
		return boardvo;
	}

	// == #70. 글 조회수 증가는 없고 단순히 글 1개 조회만을 해주는 것
	@Override
	public BoardVO getViewWithNoAddCount(Map<String, String> paraMap) {

		BoardVO boardvo = dao.getView(paraMap);	// 단순히 그냥 글 1개만 읽어오는 것임. (DAO 에 메소드를 만들어 놓았다.)
		
		return boardvo;
	}


	// == #73. 글 1개 수정하기
	@Override
	public int edit(BoardVO boardvo) {

		int n = dao.edit(boardvo);
		
		return n;
	}

	// == #78. 글 1개 삭제하기
	@Override
	public int del(Map<String, String> paraMap) {
		
		int n = dao.del(paraMap);	
		
		// ==== #165. 파일첨부가 된 글이라면 글 삭제시 먼저 첨부파일을 삭제해주어야 한다. ==== //
		if(n==1) {
			String path = paraMap.get("path");				// 삭제해야할 파일이 저장된 경로를 paraMap에서 get 해오기
			String fileName = paraMap.get("fileName");		// 삭제해야할 파일명을 get paraMap에서 해오기
		
			if(fileName != null && !"".equals(fileName)) {
				try {
					filemanager.doFileDelete(fileName, path);	// FileManager 를 사용하기 위해 맨 상단에 FileManager를  bean 으로 올려주도록 하자.	
				} catch (Exception e) {
					e.printStackTrace();
				} 
			}
			
		}
		
		//////////////////////////////////////////////////////////////////////
		
		return n;
	}

	// == #85. 댓글쓰기 (Transction 처리)  - 3개의 DML 발생
	// tbl_comment 테이블에 insert 된 다음에 
	// tbl_board 테이블에 commentCount 컬럼이 1증가(update) 하도록 요청한다.
	// 이어서 회원의 포인트를 50점을 증가하도록 한다. (update)
	// 즉, 2개이상의 DML 처리를 해야하므로 Transaction 처리를 해야 한다. (여기서는 3개의 DML 처리가 일어남)
	// >>>>> 트랜잭션처리를 해야할 메소드에 @Transactional 어노테이션을 설정하면 된다. 
	// rollbackFor={Throwable.class} 은 롤백을 해야할 범위를 말하는데 Throwable.class 은 error 및 exception 을 포함한 최상위 루트이다. 
	// 즉, 해당 메소드 실행시 발생하는 모든 error 및 exception 에 대해서 롤백을 하겠다는 말이다.	
	@Override
	@Transactional(propagation=Propagation.REQUIRED, isolation=Isolation.READ_COMMITTED, rollbackFor= {Throwable.class}) 
	public int addComment(CommentVO commentvo) throws Throwable {

		int n=0, m=0, result=0;
		
		// 한개라도 실패 시, rollback 처리 한다. (rollbackFor= {Throwable.class})
		
		n = dao.addComment(commentvo); 	// 댓글 쓰기 (tbl_comment 테이블에 insert)
		//	System.out.println("확인용 n : " + n);
		// 확인용 n : 1

		
		if(n==1) {
			m = dao.updateCommentCount(commentvo.getParentSeq()); // tbl_board 테이블에 commentCount(댓글 갯수) 컬럼이 1증가(update)
			// 원게시물의 글번호인 parentSeq 를 가져온다. (where 절에 있어야 한다.)
			//	System.out.println("확인용 m : " + m);
			//	확인용 m : 1
			
		}
		
		if(m==1) {
			// tbl_member(회원) 테이블의 point 컬럼의 값을 (회원의 포인트를) 50 증가하겠다. (Update)
			Map<String, String> paraMap = new HashMap<>();
			paraMap.put("userid", commentvo.getFk_userid());
			paraMap.put("point", "50");
			
			result = dao.updateMemberPoint(paraMap);
			//	System.out.println("확인용 result : " + result);
			//	확인용 result : 1			
		}
		
		return result;
	}

	// === 원게시물에 달린 댓글을 조회해오기
	@Override
	public List<CommentVO> getCommentList(String parentSeq) {

		List<CommentVO> commentList = dao.getCommentList(parentSeq);
		
		return commentList;
	}

	// == #98. BoardAOP 클래스에서 사용하는 것으로 특정 회원에게 특정 점수만큼 포인트를 증가하기 위한 것
	@Override
	public void pointPlus(Map<String, String> paraMap) {

		dao.pointPlus(paraMap);
		
	}

	// == #103. 페이징 처리를 안 한 검색어가 있는 전체 글목록 보여주기 
	@Override
	public List<BoardVO> boardListSearch(Map<String, String> paraMap) {

		List<BoardVO> boardList = dao.boardListSearch(paraMap);
		
		return boardList;
	}

	// == #109. 검색어 입력 시 자동글 완성하기 4
	@Override
	public List<String> wordSearchShow(Map<String, String> paraMap) {

		List<String> wordList = dao.wordSearchShow(paraMap);
		
		return wordList;
	}

	// == # 115. 총 게시물 건수(totalCount) - 검색이 있을 때와 검색이 없을 때로 나뉜다.
	@Override
	public int getTotalCount(Map<String, String> paraMap) {
		
		int n = dao.getTotalCount(paraMap);		// return 타입은 갯수로 나오게 된다.
		
		return n;
	}

	// == # 118. 페이징 처리한 글목록 가져오기(검색이 있든지, 검색이 없든지 모두 다 포함한 것이다.)
	@Override
	public List<BoardVO> boardListSearchWithPaging(Map<String, String> paraMap) {

		List<BoardVO> boardList = dao.boardListSearchWithPaging(paraMap);
		
		return boardList;
	}

	// === #129. 원게시물(부모글)에 딸린 댓글들을 페이징 처리해서 조회해오기(Ajax 로 처리) === 
	@Override
	public List<CommentVO> getCommentListPaging(Map<String, String> paraMap) {

		List<CommentVO> commentList = dao.getCommentListPaging(paraMap);
		
		return commentList;
	}

	// === #133. 원게시물(부모글)에 딸린 댓글의 totalPage 를 알아오기 (Ajax 로 처리) === 
	@Override
	public int getCommentTotalPage(Map<String, String> paraMap) {

		int totalPage = dao.getCommentTotalPage(paraMap);
		
		return totalPage;
	}


	// === #157. 글쓰기(파일 첨부가 있는 글쓰기) ==
	@Override
	public int add_withFile(BoardVO boardvo) {
		
		// 글쓰기가 원글쓰기인지 아니면 답변글쓰기인지를 구분하여 
	    // tbl_board 테이블에 insert 를 해주어야 한다.
	    // 원글쓰기 이라면 tbl_board 테이블의 groupno 컬럼의 값은 
	    // groupno 컬럼의 최대값(max)+1 로 해서 insert 해야하고,
	    // 답변글쓰기 이라면 넘겨받은 값(boardvo)을 그대로 insert 해주어야 한다.		
		
		// 원글쓰기인지 답변글쓰기인지 구분해야 한다.
		// === #144. 글쓰기가 원글쓰기인지 아니면 답변글쓰기인지를 구분하여 
		//           tbl_board 테이블에 insert 를 해주어야 한다.
		//           원글쓰기 이라면 tbl_board 테이블의 groupno 컬럼의 값은 
		//           groupno 컬럼의 최대값(max)+1 로 해서 insert 해야하고,
		//           답변글쓰기 이라면 넘겨받은 값(boardvo)을 그대로 insert 해주어야 한다.
		
		// === 원글쓰기인지, 답변글쓰기인지 구분하기 시작 === //
		if( "".equals(boardvo.getFk_seq() ) ) {
			// 원글쓰기인 경우
			// groupno 컬럼의 값은 groupno 컬럼의 최대값(max)+1 로 해야한다.
			int groupno = dao.getGroupnoMax() + 1;
			boardvo.setGroupno(String.valueOf(groupno));
		}		
		// === 원글쓰기인지, 답변글쓰기인지 구분하기 끝 === //
		
		int n = dao.add_withFile(boardvo);	// 첨부파일이 있는 경우
		return n;
	}


	// === #172. 댓글 1개 조회하기 (첨부파일이 있는 댓글)
	@Override
	public CommentVO getCommentOne(String seq) {
		CommentVO commentvo = dao.getCommentOne(seq);
		return commentvo;
	}
	

	// ==== #184. Spring Scheduler(스프링 스케줄러4)는 서비스단에서 작업을 하는 것이다.

	/*
	   스케줄은 3가지 종류  cron, fixedDelay, fixedRate 가 있다. 
	   
	   @Scheduled(cron="0 0 0 * * ?") 
	   cron 스케줄에 따라서 일정기간에 시작한다. 매일 자정마다 (00:00:00)에 실행한다.
	   
	   >>> cron 표기법 <<<
	   
	   문자열의 좌측부터 우측까지 아래처럼 의미가 부여되고 각 항목은 공백 문자로 구분한다.
	   
	   순서는 초 분 시 일 월 요일명 이다. 0 50 12 * * ? (12 시 50분 0 초 / 모든 일, 모든월, 모든요일)
	   ----------------------------------------------------------------------------------------------------------------    
	   의미             초               분              시             일                         월             요일명                                                                   년도
	   ----------------------------------------------------------------------------------------------------------------
	   사용가능한   0~59     0~59     0~23      1~31           1~12      1~7 (1=>일요일, 2=>월요일, ... 7=>토요일)     1970 ~ 2099 
	   값           - * /    - * /    - * /     - * ? / L W    - * /     - * ? / L #
	   
	   * 는 모든 수를 의미.
	   
	   ? 는 해당 항목을 사용하지 않음.  
	   일에서 ?를 사용하면 월중 날짜를 지정하지 않음. 요일명에서 ?를 사용하면 주중 요일을 지정하지 않음.
	   
	   - 는 기간을 설정. 시에서 10-12이면 10시, 11시, 12시에 동작함.
	   분에서 57-59이면 57분, 58분, 59분에 동작함.
	   
	   , 는 특정 시간을 지정함. 요일명에서 2,4,6 은 월,수,금에만 동작함.
	   
	   / 는 시작시간과 반복 간격 설정함. 초위치에 0/15로 설정하면 0초에 시작해서 15초 간격으로 동작함. 
	   분위치에 5/10으로 설정하면 5분에 시작해서 10분 간격으로 동작함.
	   
	   L 는 마지막 기간에 동작하는 것으로 일과 요일명에서만 사용함. 일위치에 사용하면 해당월의 마지막 날에 동작함.
	   요일명에 사용하면 토요일에 동작함.
	   
	   W 는 가장 가까운 평일에 동작하는 것으로 일에서만 사용함.  일위치에 15W로 설정하면 15일이 토요일이면 가장 가까운 14일 금요일에 동작함.
	   일위치에 15W로 설정하고 15일이 일요일이면 16일에 동작함.
	   일위치에 15W로 설정하고 15일이 평일이면 15일에 동작함.
	   
	   LW 는 L과 W의 조합.                             그달의 마지막 평일에 동작함.
	   
	   # 는 몇 번째 주와 요일 설정함. 요일명에서만 사용함.    요일명위치에 6#3이면 3번째 주 금요일에 동작함.
	   요일명위치에 4#2이면 2번째 주 수요일에 동작함.
	   
	   
	   ※ cron 스케줄 사용 예
	   0 * * * * *             ==> 매 0초마다 실행(즉, 1분마다 실행함)
	   
	   * 0 * * * *             ==> 매 0분마다 실행(즉, 1시간마다 실행함)
	   
	   0 * 14 * * *            ==> 14시에 0분~59분까지 1분 마다 실행
	   
	   * 10,50 * * * *         ==> 매 10분, 50분 마다 실행
	   , : 여러 값 지정 구분에 사용 
	   
	   0 0/10 14 * * *         ==> 14시 0분 부터 시작하여 10분 간격으로 실행(즉, 6번 실행함)
	   / : 초기값과 증가치 설정에 사용
	   * 
	   0 0/10 14,18 * * *      ==> 14시 0분 부터 시작하여 10분 간격으로 실행(6번 실행함) 그리고 
	   ==> 18시 0분 부터 시작하여 10분 간격으로 실행(6번 실행함)
	   / : 초기값과 증가치 설정에 사용 
	   , : 여러 값 지정 구분에 사용 
	   
	   0 0 12 * * *            ==> 매일 12(정오)시에 실행
	   0 15 10 * * *           ==> 매일 오전 10시 15분에 실행
	   0 0 14 * * *            ==> 매일 14시에 실행
	   
	   0 0 0/6 * * *        ==> 매일 0시 6시 12시 18시 마다 실행
	   - : 범위 지정에 사용  / : 초기값과 증가치 설정에 사용
	   
	   0 0/5 14-18 * * *    ==> 매일 14시 부터 18시에 시작해서 0분 부터 매 5분간격으로 실행
	   / : 증가치 설정에 사용
	   
	   0 0-5 14 * * *          ==> 매일 14시 0분 부터 시작해서 14시 5분까지 1분마다 실행   
	   - : 범위 지정에 사용
	   
	   0 0 8 * * 2-6           ==> 평일 08:00 실행 (월,화,수,목,금)  
	   
	   0 0 10 * * 1,7          ==> 토,일 10:00 실행 (토,일) 
	   
	   0 0/5 14 * * ?          ==> 아무요일, 매월, 매일 14:00부터 14:05분까지 매분 0초 실행 (6번 실행됨)
	   
	   0 15 10 ? * 6L          ==> 매월 마지막 금요일 아무날의 10:15:00에 실행
	   
	   0 15 10 15 * ?          ==> 아무요일, 매월 15일 10:15:00에 실행 
	   
	   * /1 * * * *            ==> 매 1분마다 실행
	   
	   * /10 * * * *           ==> 매 10분마다 실행 
	   
	   
	   >>> fixedDelay <<<
	   이전에 실행된 task의 종료시간으로부터 정의된 시간만큼 지난 후 다음에 task를 실행함. 단위는 밀리초임.
	   @Scheduled(fixedDelay=1000)
	   
	   >>> fixedRate <<<
	   이전에 실행된 task의 시작 시간으로부터 정의된 시간만큼 지난 후 다음 task를 실행함. 단위는 밀리초임.
	   @Scheduled(fixedRate=1000)

	*/   		

	
	// ==== Spring Scheduler(스프링 스케줄러)를 사용하여 특정 URL 사이트로 연결하기
	@Override
//	@Scheduled(cron="0 50 12 * * *")
//	@Scheduled(cron="0 * * * * *")		// 0초가 될떄마다 실행한다. (즉, 1분이 지나아먄 0초가 된다. == 매 1분마다)
	public void branchTimeAlarm() {
		  // !!! <주의> !!!
	      // 스케줄러로 사용되어지는 메소드는 반드시 파라미터는 없어야 한다.!!!!!
	      
	      // == 현재 시각을 나타내기 ==
	      Calendar currentDate = Calendar.getInstance(); // 현재날짜와 시간을 얻어온다.
	      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	      String currentTime = dateFormat.format(currentDate.getTime());
	      
	      System.out.println("현재시각 => " + currentTime);
	      
	      // !!!! === 특정 사이트의 웹페이지를 보여주기위해 기본브라우저를 띄운다.  === !!!!
	      // 지금은 크롬 웹브라우저를 쓰고 있다.
	      // 조심해야할 것은 http:// 를 주소에 꼭 붙여야 한다.
	      // 즉, 특정 사이트 웹페이지를 실행시키는 것이다.
	      try { 
	          Desktop.getDesktop().browse(new URI("http://localhost:9090/board/branchTimeAlarm.action")); 
	          // WAS 컴퓨터에서만 특정 웹페이지를 실행시켜주는 것이지, WAS에 접속한 다른 클라이언트 컴퓨터에서는 특정 웹사이트를 실행시켜주지 않는다.
	       } catch (IOException e) { 
	          e.printStackTrace(); 
	       } catch (URISyntaxException e) {
	          e.printStackTrace(); 
	       }

	}


  	//==== #188. Spring Scheduler(스프링 스케줄러8) ==== //
  	//=== Spring Scheduler(스프링 스케줄러)를 사용한 email 발송하기 (특정시간에 발송) ===	      
	// <주의> 스케줄러로 사용되어지는 메소드는 반드시 파라미터가 없어야 한다.!!!!
    // 매일 새벽 4시 마다 고객이 예약한 2일전에 고객에게 예약이 있다는 e메일을 자동 발송 하도록 하는 예제를 만들어 본다. 
    // 고객들의 email 주소는 List<String(e메일주소)> 으로 만들면 된다.
    // 또는 e메일 자동 발송 대신에 휴대폰 문자를 자동 발송하는 것도 가능하다. 
	@Override
//	@Scheduled(cron="0 0 4 * * *")
//	@Scheduled(cron="0 36 14 * * *")		// 0초가 될떄마다 실행한다. (즉, 1분이 지나아먄 0초가 된다. == 매 1분마다)
	public void reservationEmailSending() throws Exception{
		  // !!! <주의> !!!
	      
	      // == 현재 시각을 나타내기 ==
	      Calendar currentDate = Calendar.getInstance(); // 현재날짜와 시간을 얻어온다.
	      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	      String currentTime = dateFormat.format(currentDate.getTime());
	      
	      System.out.println("현재시각 => " + currentTime);

	      // === email(이메일)을 발송할 회원이 누구인지 알아와야 한다. (DB 에서 알아와야 한다.)
	      // VO 를 안만들어왔으므로 List<> 에 VO 대신 Map<String, String> 을 넣는다.
	      List<Map<String, String>> reservationList = dao.getReservationList();
	      
	      // *** 이메일 발송하기 *** //
	      // 메일을 발송하기 위해서는 예약한 회원들의 목록이 존재해야 하고 (reservationList.size() > 0), null 이 아니어야 한다.
	      if(reservationList != null && reservationList.size() > 0) {
	    	  
	    	  // 예약번호를 담는 배열 (즉 DB 에서 조회된 예약번호만큼 배열에 넣어주도록 한다.)
	    	  String[] arr_reservationSeq = new String[reservationList.size()];
	    	  // String[] arr_reservationSeq 을 생성하는 이유는 
	          // e메일 발송 후 tbl_reservation 테이블의 mailSendCheck 컬럼의 값을 0 에서 1로 update 하기 위한 용도로 
	          // update 되어질 예약번호를 기억하기 위한 것임.
	    	  
	    	  
	    	  // 메일 발송했으면 메일 발송했다고 표시를 따로 해줘야 한다. (Update 를 해주도록 한다.)
	    	  // 따라서 발송한 예약메일의 status 를 default값에서 변경해주어야 한다.
	    	  for(int i=0; i<reservationList.size(); i++) {
	    		  // 예약한 회원목록들의 size 만큼 이메일을 보내주면 된다.
	    		  
	    		  // List 에 담겨져 있는 것에서 가져온 것(get), 여기서 reservationList 는 Map 이다. ID 값은 mapper 에 저장되어 있다. (key 값은 mapper 에서 property 로 설정해두었다.)
	    		  String emailContents = "사용자 ID: " + reservationList.get(i).get("USERID") + "<br/> 예약자명: " + reservationList.get(i).get("NAME") +"님의 방문 예약일은 <span style='color:red;'>" + reservationList.get(i).get("RESERVATIONDATE") + "</span> 입니다.";
	    		  // 의존객체인 mail 가져온다. DB 에서 암호화 되어져 있으니 다시 복호화(aes.decrypt) 시킨 이메일로 보내주어야 하기 떄문에
	    		  // 이메일을 복호화 해주도록 한다.
	    		  mail.sendmail_Reservation(aes.decrypt(reservationList.get(i).get("EMAIL")), emailContents);
	    		  
	    		  // map 인 reservationList.get(i) 에서 mapper 에서 set 한 key 값인 RESERVATIONSEQ 를 get (.get("RESERVATIONSEQ")) 해온다.
	    		  // 메일을 담아주고-보내는 과정을 반복한다.
	    		  arr_reservationSeq[i] = reservationList.get(i).get("RESERVATIONSEQ");
	    		  
	    	  }//end of for-----------------------------------------------------
	    	  
	    	  // 메일을 담아주고 - 보내는 과정을 반복(for문)하는 과정을 끝낸 후,
	    	  // 이메일을 발송한 행은 발송했다는 표시해주기 (즉, DB 에서 update 해준다.)
	    	  // arr_reservationSeq 을 map 에 담아오자. (DB 에서 update 한 것) / arr_reservationSeq 는 String[] 타입이므로 map<,> value 에 String[] 타입
	    	  Map<String, String[]> paraMap = new HashMap<String, String[]>();
	    	  paraMap.putIfAbsent("arr_reservationSeq", arr_reservationSeq);		// put과 기능은 같은 것임
	    	  /*
	             Map<String, String> map = new HashMap<>();
	             
	             map.put("a","안녕");
	             map.put("b","잘들어가");		  // map.put( , ) 은 key 값을 덮어씌운다. 
	             
	             map.put("a","건강해");         // map.get("a") ==> "건강해"
	             map.putIfAbsent("b","또보자"); // map.get("b") ==> "잘들어가" // 즉 IfAbsent : if만약에 key값이 absent 라면 put 해준다는 것 . 즉 putIfAbsent 는 key 값이 없을때에만 put 해준다는 것이다.
	           */
	    	  
	    	  dao.updateMailSendCheck(paraMap);		   // 이메일을 발송한 행은 발송했다는 표시해주기 (즉, DB 에서 update 해준다.)

	    	  
	      }// end of if-----------------------------------------------------
	       
	      
	}	
	
      
	
	
}
