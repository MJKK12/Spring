package com.spring.board.model;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

//=== #32. DAO 선언 === 
@Repository	// @component 가 포함되어 있는 것이다. 자동적으로 bean 으로 올라간다.
public class BoardDAO implements InterBoardDAO {

   // === #33. 의존객체 주입하기(DI: Dependency Injection) ===
   // >>> 의존 객체 자동 주입(Automatic Dependency Injection)은
   //     스프링 컨테이너가 자동적으로 의존 대상 객체를 찾아서 해당 객체에 필요한 의존객체를 주입하는 것을 말한다. 
   //     단, 의존객체는 스프링 컨테이너속에 bean 으로 등록되어 있어야 한다. 

   //     의존 객체 자동 주입(Automatic Dependency Injection)방법 3가지 
   //     1. @Autowired ==> Spring Framework에서 지원하는 어노테이션이다. 
   //                       스프링 컨테이너에 담겨진 의존객체를 주입할때 타입을 찾아서 연결(의존객체주입)한다.
   
   //     2. @Resource  ==> Java 에서 지원하는 어노테이션이다. (JDK)
   //                       스프링 컨테이너에 담겨진 의존객체를 주입할때 필드명(이름)을 찾아서 연결(의존객체주입)한다.
   
   //     3. @Inject    ==> Java 에서 지원하는 어노테이션이다. (JDK) @Autowired 와 기능은 똑같지만 JAVA 인 JDK 에 속해있다.
   //                       스프링 컨테이너에 담겨진 의존객체를 주입할때 타입을 찾아서 연결(의존객체주입)한다.   
	
/*
	// 이제 DAO 에서 Spring 은 Connection 을 얻어올 필요가 없다.
	// root-context.xml 파일을 참조한다.
	@Autowired	// bean 에 올라간것 중 SqlSessionTemplate 에 들어간 것을 넣어준다.
	private SqlSessionTemplate abc;		// @Autowired 로 인해 abc 는 null 이 아니게됨. abc 는  myBatis로  DB 이다.
										// SqlSessionTemplate : DBCP 연결 , abc 에는 sqlsession 이 들어와 있는 것이다.
	// Type 에 따라 Spring 컨테이너가 알아서 root-context.xml 에 생성된 org.mybatis.spring.SqlSessionTemplate 의 bean 을  abc 에 주입시켜준다. 
    // 그러므로 abc 는 null 이 아니다.
*/
	
	@Resource
	private SqlSessionTemplate sqlsession;	// SqlSessionTemplate 클래스에 id 가 무엇인지? (의존객체)
											// 로컬 DB mymvc_user 에 연결
	// Type 에 따라 Spring 컨테이너가 알아서 root-context.xml 에 생성된 org.mybatis.spring.SqlSessionTemplate 의  sqlsession bean 을  sqlsession 에 주입시켜준다. 
    // 그러므로 sqlsession 는 null 이 아니다.
	
	@Resource
	private SqlSessionTemplate sqlsession_2;	// 로컬 DB hr 에 연결
	// Type 에 따라 Spring 컨테이너가 알아서 root-context.xml 에 생성된 org.mybatis.spring.SqlSessionTemplate 의  sqlsession bean 을  sqlsession_2 에 주입시켜준다. 
    // 그러므로 sqlsession_2 는 null 이 아니다.
	
	// spring_test 테이블에 insert 하기 (오라클)
	@Override
	public int test_insert() {

	//	int n = abc.insert("board.test_insert");	// 파라미터에는 String 이 들어가고, 리턴타입은 int 이다. 

		int n = sqlsession.insert("board.test_insert");			// 정상일 시 1을 return 한다.

		int n_2 = sqlsession_2.insert("board.test_insert");		// 정상일 시 1을 return 한다.
		
		// board 라는 namespace 에 있는 id 값을 넣어준다. (insert 인데, sql문이 () 안에 있는 것이다.)
		// insert 성공 시, 한 행이 들어옴. (int)		
		// "" 안에는 namespace 가 들어온다 (board.xml 참고 - mapper 패키지), xml 파일들 중에 namespace 가 board 인 것이 어떤 것인지?
		// id 값이 test_insert 인 것
		// () 안에는 sql 문이 어디있는지 알려달라 → root-context.xml 의 classpath 를 참고한다.
		// int n 은 service 단으로 return 해준다.
		return n*n_2;		// 1 return.
	}


	// spring_test 테이블에 select 하기 (오라클)
	@Override
	public List<TestVO> test_select() {

		List<TestVO> testvoList = sqlsession.selectList("board.test_select");	// namespace(board.xml의 namespace).메소드명
		// board.xml 로 이동해서 select sql 문을 만들자.
		
		return testvoList;	// service 단에 넘기자.
	}


	// view 단의 form 태그에서 입력받은 값을 spring_test 테이블에 insert 하기 (파라미터가 Map)
	@Override
	public int test_insert(Map<String, String> paraMap) {
		// DB 에 가야 한다. (sqlSession) ID 는 고유해야 한다. 위에 이미 board.test_insert 가 있으므로 _map 을 추가해줌으로써 id 중복을 방지한다.
		int n = sqlsession.insert("board.test_insert_map", paraMap);	// "mymvc_user" 사용자의 DB 에 넣어주어야 한다.
		// namespace 가 board 인 mapper 로 이동해서 sql 문을 작성하자.
		return n;
	}


	// view 단의 form 태그에서 입력받은 값을 spring_test 테이블에 insert 하기 (파라미터가 VO)
	@Override
	public int test_insert(TestVO vo) {
		int n = sqlsession.insert("board.test_insert_vo", vo);	// 파라미터가 있으므로 ( , ) 형태를 사용한다.		
		return n;
	}
	

	//////////////////////////////////////////////////////
	// ** 게시판 만들기 ** //
	
	// === #38. 시작페이지에서 메인이미지를 보여주는 것 (캐러셀) === 
	
	@Override
	public List<String> getImgfilenameList() {

		List<String> imgfilenameList = sqlsession.selectList("board.getImgfilenameList");	// 모두 다 읽어오기 때문에 where 절 필요 없음. (parameter 필요 없음)
		
		return imgfilenameList;
	}


	// === #46.로그인 처리하기 === // (로그인 처리와 idle 은 한 세트)
	@Override
	public MemberVO getLoginMember(Map<String, String> paraMap) {
		// 암호는 암호화가 된다.
		MemberVO loginuser = sqlsession.selectOne("board.getLoginMember", paraMap);		// 로그인한 유저의 ID 는 고유하다. (One), paraMap 속에 userid 와 pwd 가 있다. 
		
		return loginuser;
	}
	// === tbl_member 테이블의 idle 컬럼의 값을 1로 변경하기 === //
	@Override
	public int updateIdle(String userid) {

		int n = sqlsession.update("board.updateIdle", userid);
		
		return n;
	}


	// ==== #56. 글쓰기(파일 첨부가 없는 글쓰기) ====
	@Override
	public int add(BoardVO boardvo) {

		int n = sqlsession.insert("board.add", boardvo);
		
		return n;
	}


	// === #60. 페이징 처리를 안한 검색어가 없는 전체 글목록 보여주기 === //
	@Override
	public List<BoardVO> boardListNoSearch() {

		List<BoardVO> boardList = sqlsession.selectList("board.boardListNoSearch");	// 파라미터 없이 모두 읽어오기.
		
		return boardList;
	}


	// ** 글 1개 조회하기와 글 조회수 1 증가하기는 한 세트이다. **
	// === #64. 글 1개 조회하기
	@Override
	public BoardVO getView(Map<String, String> paraMap) {
		BoardVO boardvo = sqlsession.selectOne("board.getView", paraMap);
		return boardvo;
	}

	// === #65. 글 조회수 1 증가 하기 (1만 증가시켜주면 되므로 return 값이 없는 void.)
	@Override
	public void setAddReadCount(String seq) {
		sqlsession.update("board.setAddReadCount", seq);		
	}



	// == #74. 글 1개 수정하기
	@Override
	public int edit(BoardVO boardvo) {
		int n = sqlsession.update("board.edit", boardvo);		
		return n;
	}


	// == #79. 글 1개 삭제하기
	@Override
	public int del(Map<String, String> paraMap) {
		int n = sqlsession.delete("board.del", paraMap);		
		return n;
	}


	// 아래 3 개는 1 set (댓글 테이블 댓글 쓰기- 댓글 테이블 댓글수 증가- 회원테이블 point 증가)
	// == #86. 댓글 쓰기 (tbl_comment 테이블에 insert)	
	@Override
	public int addComment(CommentVO commentvo) {
		int n = sqlsession.insert("board.addComment", commentvo);
		return n;
	}


	// == #87.-1 tbl_board 테이블에 commentCount 컬럼이 1증가(update)
	@Override
	public int updateCommentCount(String parentSeq) {
		int n = sqlsession.update("board.updateCommentCount", parentSeq);
		return n;
	}

	// == #87.-2 tbl_member(회원) 테이블의 point 컬럼의 값을 (회원의 포인트를) 50 증가하겠다. (Update)
	@Override
	public int updateMemberPoint(Map<String, String> paraMap) {
		int n = sqlsession.update("board.updateMemberPoint", paraMap);
		return n;
	}


	// === #92. 원게시물에 달린 댓글을 조회해오기
	@Override
	public List<CommentVO> getCommentList(String parentSeq) {
		// 파라미터가 있는 select
		List<CommentVO> commentList = sqlsession.selectList("board.getCommentList", parentSeq);	
		
		return commentList;
	}


	// == #99. BoardAOP 클래스에서 사용하는 것으로 특정 회원에게 특정 점수만큼 포인트를 증가하기 위한 것
	@Override
	public void pointPlus(Map<String, String> paraMap) {

		sqlsession.update("board.pointPlus", paraMap);
		// return 타입 없이 (void) 한다.
	}


	// == #104. 페이징 처리를 안 한 검색어가 있는 전체 글목록 보여주기 
	@Override
	public List<BoardVO> boardListSearch(Map<String, String> paraMap) {
		List<BoardVO> boardList = sqlsession.selectList("board.boardListSearch", paraMap);
		return boardList;
	}


	// == #110. 검색어 입력 시 자동글 완성하기 5
	@Override
	public List<String> wordSearchShow(Map<String, String> paraMap) {
		List<String> wordList = sqlsession.selectList("board.wordSearchShow", paraMap);
		return wordList;
	}


	// == # 116. 총 게시물 건수(totalCount) - 검색이 있을 때와 검색이 없을 때로 나뉜다.
	@Override
	public int getTotalCount(Map<String, String> paraMap) {
		int n = sqlsession.selectOne("board.getTotalCount", paraMap);	// count 는 행의 갯수가 1개이므로 selectOne 이다.
		return n;
	}


	// == # 119. 페이징 처리한 글목록 가져오기(검색이 있든지, 검색이 없든지 모두 다 포함한 것이다.)
	@Override
	public List<BoardVO> boardListSearchWithPaging(Map<String, String> paraMap) {

		List<BoardVO> boardList = sqlsession.selectList("board.boardListSearchWithPaging", paraMap);
		
		return boardList;
	}


	// === #130. 원게시물(부모글)에 딸린 댓글들을 페이징 처리해서 조회해오기(Ajax 로 처리) === 
	@Override
	public List<CommentVO> getCommentListPaging(Map<String, String> paraMap) {
		List<CommentVO> commentList = sqlsession.selectList("board.getCommentListPaging", paraMap);		
		return commentList;
	}


	// === #134. 원게시물(부모글)에 딸린 댓글의 totalPage 를 알아오기 (Ajax 로 처리) === 
	@Override
	public int getCommentTotalPage(Map<String, String> paraMap) {
		int totalPage = sqlsession.selectOne("board.getCommentTotalPage", paraMap);
		return totalPage;
	}


	// === #145. tbl_board 테이블에서 groupno 컬럼의 최대값 알아오기 === //
	@Override
	public int getGroupnoMax() {
		int maxgroupno = sqlsession.selectOne("board.getGroupnoMax");	// 최대값만 알아오면 되므로 파라미터 x
		return maxgroupno;
	}


	// === #158. 글쓰기(파일 첨부가 있는 글쓰기) ==
	@Override
	public int add_withFile(BoardVO boardvo) {
		int n = sqlsession.insert("board.add_withFile", boardvo);
		return n;
	}

	
	// === #173. 댓글 1개 조회하기 (첨부파일이 있는 댓글)
	@Override
	public CommentVO getCommentOne(String seq) {
		CommentVO commentvo = sqlsession.selectOne("board.getCommentOne", seq);
		return commentvo;
	}

	// ** 아래의 두개는 1set 이다.**
  	//==== #189. Spring Scheduler(스프링 스케줄러9) ==== //
  	//=== Spring Scheduler(스프링 스케줄러)를 사용한 email 발송하기 (특정시간에 발송) ===	   
	@Override
	public List<Map<String, String>> getReservationList() {
		List<Map<String, String>> reservationList = sqlsession.selectList("board.getReservationList");
		return reservationList;
	}

	// 이메일을 발송한 행은 발송했다는 표시해주기 (즉, DB 에서 update 해준다.)	
	@Override
	public void updateMailSendCheck(Map<String, String[]> paraMap) {
		sqlsession.update("board.updateMailSendCheck", paraMap);
	}

}
