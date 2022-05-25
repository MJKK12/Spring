package com.spring.board.service;

import java.util.*;

import com.spring.board.model.*;

public interface InterBoardService {

	int test_insert(); 	// DB 에 insert 할 수 있는 메소드

	List<TestVO> test_select();		// DB 에서 select 할 수 있는 메소드

	int test_insert(Map<String, String> paraMap);	// 메소드의 오버로딩 (첫번째 test_insert() 와 이름은 같지만 파라미터가 다르다.)

	int test_insert(TestVO vo);	// 위의 paraMap 을 넣은 것과는 달리 vo 를 넣었다.

	//////////////////////////////////////////////////////
	// ** 게시판 만들기 ** //
	
	// 시작페이지에서 메인이미지를 보여주는 것 (캐러셀)
	List<String> getImgfilenameList();

	// 로그인 처리하기 
	MemberVO getLoginMember(Map<String, String> paraMap);

	// 글쓰기(파일 첨부가 없는 글쓰기) ==
	int add(BoardVO boardvo);

	// 페이징 처리를 안한 검색어가 없는 전체 글목록 보여주기 === //
	List<BoardVO> boardListNoSearch();

	// 글 조회수 증가와 함께 글 1개를 조회해주는 것
	BoardVO getView(Map<String, String> paraMap);

	// 글 조회수 증가는 없고 단순히 글 1개 조회만을 해주는 것
	BoardVO getViewWithNoAddCount(Map<String, String> paraMap);

	// 글 1개 수정하기
	int edit(BoardVO boardvo);

	// 글 1개 삭제하기
	int del(Map<String, String> paraMap);

	// 댓글 쓰기 (transction 처리, 3개가 동시에 돌아가야 하기 때문에 Exception 처리를 해준다.)
	// // Transction 처리는 Service 에서 하기 때문에 Exception 처리를 한다.
	int addComment(CommentVO commentvo) throws Throwable;

	// 원게시물에 달린 댓글을 조회해오기 (Ajax 로 처리)
	List<CommentVO> getCommentList(String parentSeq);

	// BoardAOP 클래스에서 사용하는 것으로 특정 회원에게 특정 점수만큼 포인트를 증가하기 위한 것
	void pointPlus(Map<String, String> paraMap);

	// 페이징 처리를 안 한 검색어가 있는 전체 글목록 보여주기 
	List<BoardVO> boardListSearch(Map<String, String> paraMap);

	// 검색어 입력 시 자동글 완성하기
	List<String> wordSearchShow(Map<String, String> paraMap);

	// 총 게시물 건수(totalCount) - 검색이 있을 때와 검색이 없을 때로 나뉜다.
	int getTotalCount(Map<String, String> paraMap);

	// 페이징 처리한 글목록 가져오기(검색이 있든지, 검색이 없든지 모두 다 포함한 것이다.)
	List<BoardVO> boardListSearchWithPaging(Map<String, String> paraMap);

	// 원게시물(부모글)에 딸린 댓글들을 페이징 처리해서 조회해오기(Ajax 로 처리) 
	List<CommentVO> getCommentListPaging(Map<String, String> paraMap);

	// 원게시물(부모글)에 딸린 댓글의 totalPage 를 알아오기 (Ajax 로 처리) 
	int getCommentTotalPage(Map<String, String> paraMap);

	// 글쓰기(파일 첨부가 있는 글쓰기) 
	int add_withFile(BoardVO boardvo);

	// 댓글 1개 조회하기 (첨부파일이 있는 댓글)
	CommentVO getCommentOne(String seq);		

	// ==== #183. Spring Scheduler(스프링 스케줄러3)
	// 스프링 스케줄러는 service 에서 해주도록 한다.
	// ==== Spring Scheduler 를 사용하여 특정 URL 사이트로 연결하기 ====
	// !!!<주의>!!! 스프링 스케줄러로 사용되는 메소드는 반드시 파라미터가 없어야 한다. *********
	void branchTimeAlarm();				   // 파라미터를 넣지 않는다.!!!!

	// === Spring Scheduler 를 사용하여 email 발송하기 === 
    // <주의> 스케줄러로 사용되어지는 메소드는 반드시 파라미터가 없어야 한다.!!!!
    // 매일 새벽 4시 마다 고객이 예약한 2일전에 고객에게 예약이 있다는 e메일을 자동 발송 하도록 하는 예제를 만들어 본다. 
    // 고객들의 email 주소는 List<String(e메일주소)> 으로 만들면 된다.
    // 또는 e메일 자동 발송 대신에 휴대폰 문자를 자동 발송하는 것도 가능하다.     
    void reservationEmailSending() throws Exception;	// 메일 발송 실패 시 Exception 을 thorws 하겠다.
    
	
	
}
