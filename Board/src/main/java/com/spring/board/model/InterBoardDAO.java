package com.spring.board.model;

import java.util.List;
import java.util.Map;

public interface InterBoardDAO {

	int test_insert();	// spring_test 테이블에 insert 하기 (오라클)

	List<TestVO> test_select();	// spring_test 테이블에 select 하기 (오라클)

	// view 단의 form 태그에서 입력받은 값을 spring_test 테이블에 insert 하기 (파라미터가 Map)
	int test_insert(Map<String, String> paraMap);

	// view 단의 form 태그에서 입력받은 값을 spring_test 테이블에 insert 하기 (파라미터가 VO)
	int test_insert(TestVO vo);


	//////////////////////////////////////////////////////
	// ** 게시판 만들기 ** //
	
	// 시작페이지에서 메인이미지를 보여주는 것 (캐러셀) 
	List<String> getImgfilenameList();

	// 로그인 처리하기 
	MemberVO getLoginMember(Map<String, String> paraMap);

	// tbl_member 테이블의 idle 컬럼의 값을 1로 변경하기 
	int updateIdle(String string);

	// 글쓰기(파일 첨부가 없는 글쓰기). 
	int add(BoardVO boardvo);

	// 페이징 처리를 안한 검색어가 없는 전체 글목록 보여주기
	List<BoardVO> boardListNoSearch();

	// ** 글 1개 조회하기와 글 조회수 1 증가하기는 한 세트이다. **
	// 글 1개 조회하기
	BoardVO getView(Map<String, String> paraMap);
	// 글 조회수 1 증가 하기
	void setAddReadCount(String seq);

	// 글 1개 수정하기
	int edit(BoardVO boardvo);

	// 글 1개 삭제하기
	int del(Map<String, String> paraMap);

	// 아래 3개는 1 set 이다.
	////////////////////////////////////////////////////////////////////////////////////
	// throws Throwable 은 이미 service 단에서 해주고 있기 때문에 써주지 않아도 된다.
	int addComment(CommentVO commentvo);		// 댓글 쓰기 (tbl_comment 테이블에 insert)
	int updateCommentCount(String parentSeq);	// tbl_board 테이블에 commentCount 컬럼이 1증가(update)
	int updateMemberPoint(Map<String, String> paraMap);		// tbl_member(회원) 테이블의 point 컬럼의 값을 (회원의 포인트를) 50 증가하겠다. (Update)
	////////////////////////////////////////////////////////////////////////////////////

	// === 원게시물에 달린 댓글을 조회해오기
	List<CommentVO> getCommentList(String parentSeq);

	// BoardAOP 클래스에서 사용하는 것으로 특정 회원에게 특정 점수만큼 포인트를 증가하기 위한 것	
	void pointPlus(Map<String, String> paraMap);

	// === 페이징 처리를 안 한 검색어가 있는 전체 글목록 보여주기 
	List<BoardVO> boardListSearch(Map<String, String> paraMap);

	// 검색어 입력 시 자동글 완성하기
	List<String> wordSearchShow(Map<String, String> paraMap);

	// == 총 게시물 건수(totalCount) - 검색이 있을 때와 검색이 없을 때로 나뉜다.
	int getTotalCount(Map<String, String> paraMap);

	// 페이징 처리한 글목록 가져오기(검색이 있든지, 검색이 없든지 모두 다 포함한 것이다.)
	List<BoardVO> boardListSearchWithPaging(Map<String, String> paraMap);
	
	// 원게시물(부모글)에 딸린 댓글들을 페이징 처리해서 조회해오기(Ajax 로 처리) 
	List<CommentVO> getCommentListPaging(Map<String, String> paraMap);

	// 원게시물(부모글)에 딸린 댓글의 totalPage 를 알아오기 (Ajax 로 처리) === 
	int getCommentTotalPage(Map<String, String> paraMap);
	
	// tbl_board 테이블에서 groupno 컬럼의 최대값 알아오기
	int getGroupnoMax();

	// 글쓰기(첨부파일이 있는 경우) 
	int add_withFile(BoardVO boardvo);

	// 댓글 1개 조회하기 (첨부파일이 있는 댓글)
	CommentVO getCommentOne(String seq);

	// ** 아래의 두개는 1set 이다.
	// === Spring Scheduler(스프링 스케줄러)를 사용한 email 발송하기 (특정시간에 발송) ===
	List<Map<String, String>> getReservationList();
	void updateMailSendCheck(Map<String, String[]> paraMap);	// 이메일을 발송한 행은 발송했다는 표시해주기 (즉, DB 에서 update 해준다.)

	
}
