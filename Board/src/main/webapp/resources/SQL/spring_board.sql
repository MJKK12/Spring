------ ***** spring 기초 ***** ------

show user;
-- USER이(가) "MYMVC_USER"입니다.

create table spring_test
(no         number
,name       varchar2(100)
,writeday   date default sysdate
);

select *
from spring_test;


select no, name, to_char(writeday, 'yyyy-mm-dd hh24:mi:ss') AS writeday  
from spring_test
order by writeday desc;

-----------------------------------------------------------------------

select * from tab;

select * 
from TBL_MAIN_IMAGE;

select imgfilename 
from TBL_MAIN_IMAGE
order by imgno desc;


select * 
from tbl_member;

desc tbl_member;

SELECT userid, name, email, mobile, postcode, address, detailaddress, extraaddress, gender  
     , birthyyyy, birthmm, birthdd, coin, point, registerday, pwdchangegap  
     , nvl(lastlogingap, trunc( months_between(sysdate, registerday) ) ) AS lastlogingap 
FROM 
 ( 
  select userid, name, email, mobile, postcode, address, detailaddress, extraaddress, gender 
        , substr(birthday,1,4) AS birthyyyy, substr(birthday,6,2) AS birthmm, substr(birthday,9) AS birthdd  
        , coin, point, to_char(registerday, 'yyyy-mm-dd') AS registerday 
        , trunc( months_between(sysdate, lastpwdchangedate) ) AS pwdchangegap 
  from tbl_member 
  where status = 1 and userid = 'kimmj' and pwd = '9695b88a59a1610320897fa84cb7e144cc51f2984520efb77111d94b402a8382' 
 ) M 
CROSS JOIN 
(
 select trunc( months_between(sysdate, max(logindate)) ) AS lastlogingap
 from tbl_loginhistory
 where fk_userid = 'kimmj'
) H;


select *
from tbl_loginhistory
order by logindate desc;



---------------------------------------------------------------------------------------------------


desc tbl_member;


SELECT userid, name, email, mobile, postcode, address, detailaddress, extraaddress, gender  
     , birthyyyy, birthmm, birthdd, coin, point, registerday, pwdchangegap  
     , nvl(lastlogingap, trunc( months_between(sysdate, registerday) ) ) AS lastlogingap 
FROM 
 ( 
  select userid, name, email, mobile, postcode, address, detailaddress, extraaddress, gender 
        , substr(birthday,1,4) AS birthyyyy, substr(birthday,6,2) AS birthmm, substr(birthday,9) AS birthdd  
        , coin, point, to_char(registerday, 'yyyy-mm-dd') AS registerday 
        , trunc( months_between(sysdate, lastpwdchangedate) ) AS pwdchangegap 
  from tbl_member 
  where status = 1 and userid = 'kimmj' and pwd = '9695b88a59a1610320897fa84cb7e144cc51f2984520efb77111d94b402a8382' 
 ) M 
CROSS JOIN 
(
 select trunc( months_between(sysdate, max(logindate)) ) AS lastlogingap
 from tbl_loginhistory
 where fk_userid = 'kimmj'
) H;

 select *
 from tbl_loginhistory
 where fk_userid = 'kimmj'
 order by logindate desc;
 
 update tbl_loginhistory set logindate = add_months(logindate, 13)    
 where fk_userid = 'kimmj';
 
 commit;
 
 
 
select * 
from tbl_member
where userid = 'kimmj';

-- 4개월 전으로 비밀번호 변경날짜 바꾸기
update tbl_member set lastpwdchangedate = add_months(lastpwdchangedate, -4)
where userid = 'kimmj';

commit;

-- 원상복구
update tbl_member set lastpwdchangedate = add_months(lastpwdchangedate, 4)
where userid = 'kimmj';

commit;

update tbl_member set idle = 0
where userid = 'kimmj';

commit;



    ------- **** spring 게시판(답변글쓰기가 없고, 파일첨부도 없는) 글쓰기 **** -------

show user;
-- USER이(가) "MYMVC_USER"입니다.    
    
    
desc tbl_member;


-----------------------------------------------------------------------------------------
-- 스프링 게시판 테이블 만들기

create table tbl_board
(seq         number                not null    -- 글번호
,fk_userid   varchar2(20)          not null    -- 사용자ID
,name        varchar2(20)          not null    -- 글쓴이 
,subject     Nvarchar2(200)        not null    -- 글제목
,content     Nvarchar2(2000)       not null    -- 글내용   -- clob (최대 4GB까지 허용) 
,pw          varchar2(20)          not null    -- 글암호
,readCount   number default 0      not null    -- 글조회수
,regDate     date default sysdate  not null    -- 글쓴시간
,status      number(1) default 1   not null    -- 글삭제여부   1:사용가능한 글,  0:삭제된글
,constraint PK_tbl_board_seq primary key(seq)
,constraint FK_tbl_board_fk_userid foreign key(fk_userid) references tbl_member(userid)
,constraint CK_tbl_board_status check( status in(0,1) )
);
-- Table TBL_BOARD이(가) 생성되었습니다.


create sequence boardSeq
start with 1
increment by 1
nomaxvalue
nominvalue
nocycle
nocache;
-- Sequence BOARDSEQ이(가) 생성되었습니다.


select *
from tbl_board
order by seq desc;

update tbl_board set subject = '호호호'
                   , content = '헤헤헤'
where seq = '2' and pw = 'ㄴㄹㄴㅇㄹㄴㅇㄹㄴㅇ';          -- 글 번호는 2번인데, 암호가 틀리면 '0'개 행이 update 됨. (Update 가 되지 않은 것이다.)
-- 즉, 글 수정할 때 이전에 글 작성시 입력한 글암호가 맞아야 수정이 가능하도록 한다. ('1' 개 행이 update)
-- 0이면 update 가 되지 않은 것이고, 1이면 update 된 것이다.

select seq, fk_userid, name, subject  
     , readcount, to_char(regDate, 'yyyy-mm-dd hh24:mi:ss') as regDate
from tbl_board
where status = 1
order by seq desc;



select seq, fk_userid, name, subject, content, readCount
     , to_char(regDate, 'yyyy-mm-dd hh24:mi:ss') as regDate
from tbl_board
where status = 1 and seq = '1';


-- seq 에 숫자가 아닌 문자가 들어감.
select seq, fk_userid, name, subject, content, readCount
     , to_char(regDate, 'yyyy-mm-dd hh24:mi:ss') as regDate
from tbl_board
where status = 1 and seq = 'ㄴㄹㄴㄹㄴㅇㄹㄴㅇ';


-- 인라인 뷰를 사용한다.


select previousseq, previoussubject
     , seq, fk_userid, name, subject, content, readCount, regDate
     , nextseq, nextsubject
from
(
    select lag(seq,1) over(order by seq desc) AS previousseq            
         , lag(subject,1) over(order by seq desc) AS previoussubject                                         
         , seq, fk_userid, name, subject, content, readCount
         , to_char(regDate, 'yyyy-mm-dd hh24:mi:ss') as regDate
         , lead(seq,1) over(order by seq desc) AS nextseq
         , lead(subject,1) over(order by seq desc)AS nextsubject
    from tbl_board
    where status = 1
) V
where V.seq = '2'


-----------------------------------------------------------------------------------------------

   ----- **** 댓글 게시판 **** -----

/* 
  댓글쓰기(tbl_comment 테이블)를 성공하면 원게시물(tbl_board 테이블)에
  댓글의 갯수(1씩 증가)를 알려주는 컬럼 commentCount 을 추가하겠다. 
*/
drop table tbl_board purge;

create table tbl_board
(seq           number                not null    -- 글번호
,fk_userid     varchar2(20)          not null    -- 사용자ID
,name          varchar2(20)          not null    -- 글쓴이 
,subject       Nvarchar2(200)        not null    -- 글제목
,content       Nvarchar2(2000)       not null    -- 글내용   -- clob (최대 4GB까지 허용) 
,pw            varchar2(20)          not null    -- 글암호
,readCount     number default 0      not null    -- 글조회수
,regDate       date default sysdate  not null    -- 글쓴시간
,status        number(1) default 1   not null    -- 글삭제여부   1:사용가능한 글,  0:삭제된글
,commentCount  number default 0      not null    -- 댓글의 개수
,constraint PK_tbl_board_seq primary key(seq)
,constraint FK_tbl_board_fk_userid foreign key(fk_userid) references tbl_member(userid)
,constraint CK_tbl_board_status check( status in(0,1) )
);
-- Table TBL_BOARD이(가) 생성되었습니다.

drop sequence boardSeq;

create sequence boardSeq
start with 1
increment by 1
nomaxvalue
nominvalue
nocycle
nocache;
-- Sequence BOARDSEQ이(가) 생성되었습니다.


----- **** 댓글 테이블 생성 **** -----
create table tbl_comment
(seq           number               not null   -- 댓글번호
,fk_userid     varchar2(20)         not null   -- 사용자ID
,name          varchar2(20)         not null   -- 성명
,content       varchar2(1000)       not null   -- 댓글내용
,regDate       date default sysdate not null   -- 작성일자
,parentSeq     number               not null   -- 원게시물 글번호
,status        number(1) default 1  not null   -- 글삭제여부
                                               -- 1 : 사용가능한 글,  0 : 삭제된 글
                                               -- 댓글은 원글이 삭제되면 자동적으로 삭제되어야 한다.
,constraint PK_tbl_comment_seq primary key(seq)
,constraint FK_tbl_comment_userid foreign key(fk_userid) references tbl_member(userid)
,constraint FK_tbl_comment_parentSeq foreign key(parentSeq) references tbl_board(seq) on delete cascade
,constraint CK_tbl_comment_status check( status in(1,0) ) 
);
-- Table TBL_COMMENT이(가) 생성되었습니다.


create sequence commentSeq
start with 1
increment by 1
nomaxvalue
nominvalue
nocycle
nocache;
-- Sequence COMMENTSEQ이(가) 생성되었습니다.

select *
from tbl_comment
order by seq desc;

select *
from tbl_board
order by seq desc;


-- ==== Transaction 처리를 위한 시나리오 만들기 ==== --
---- 회원들이 게시판에 글쓰기를 하면 글작성 1건당 POINT 를 100점을 준다.
---- 회원들이 게시판에서 댓글쓰기를 하면 댓글작성 1건당 POINT 를 50점을 준다.
---- 그런데 일부러 POINT 는 300을 초과할 수 없다.

select *
from tbl_member;

update tbl_member set point = 0;

commit;


-- tbl_member 테이블에 POINT 컬럼에 Check 제약을 추가한다.

alter table tbl_member
add constraint CK_tbl_member_point check( point between 0 and 300 );
-- Table TBL_MEMBER이(가) 변경되었습니다.

update tbl_member set point = 301
where userid = 'kimmj';
/*
오류 보고 -
ORA-02290: check constraint (MYMVC_USER.CK_TBL_MEMBER_POINT) violated
*/

update tbl_member set point = 300
where userid = 'kimmj';

commit;
-- 커밋 완료.


-- 댓글 쓰기 시, 아래의 세 테이블에 데이터 값이 들어간다. (1set 이다.)
select *
from tbl_comment
order by seq desc;

select *
from tbl_board

select userid, point
from tbl_member
where userid = 'eomjh';


select userid, point
from tbl_member
where userid = 'kimmj';


--- *** transaction 처리를 위해서 일부러 만들어 두었던 포인트 체크제약을 없애겠다. *** ---
--- *** tbl_member 테이블에 존재하는 제약조건 조회하기 *** ---
select *
from user_constraints
where table_name = 'TBL_MEMBER';

alter table tbl_member
drop constraint CK_TBL_MEMBER_POINT;
-- Table TBL_MEMBER이(가) 변경되었습니다.


-- 1번글에 대한 댓글
select name, content, to_char(regDate, 'yyyy-mm-dd hh24:mi:ss') AS regDate 
from tbl_comment
where status = '1' and parentSeq = '1'
order by seq;


-- 2번글에 대한 댓글
select name, content, to_char(regDate, 'yyyy-mm-dd hh24:mi:ss') AS regDate 
from tbl_comment
where status = '1' and parentSeq = '2'
order by seq;


-- 검색할 때 대문자를 입력하든, 소문자를 입력하는 결과값은 소문자로 나오도록 한다. (lower)
select *
from tbl_board
where lower(subject) like '%'||lower('koReA')||'%';




insert into tbl_board(seq, fk_userid, name, subject, content, pw, readCount, regDate, status)
values(boardSeq.nextval, 'kimmj', '김민정', '즐거운 하루 되세요~~', '오늘도 늘 행복하게~~', '1234', default, default, default);

insert into tbl_board(seq, fk_userid, name, subject, content, pw, readCount, regDate, status)
values(boardSeq.nextval, 'emojh', '엄정화', '오늘도 즐거운 수업을 합시다', '기분이 좋은 하루 되세요^^', '1234', default, default, default);

insert into tbl_board(seq, fk_userid, name, subject, content, pw, readCount, regDate, status)
values(boardSeq.nextval, 'kimmj', '김민정', '기분좋은 날 안녕하신가요?', '모두 반갑습니다', '1234', default, default, default);

insert into tbl_board(seq, fk_userid, name, subject, content, pw, readCount, regDate, status)
values(boardSeq.nextval, 'kimmj', '김민정', '모두들 즐거이 퇴근하세요 안녕~~', '건강이 최고 입니다.', '1234', default, default, default);

insert into tbl_board(seq, fk_userid, name, subject, content, pw, readCount, regDate, status)
values(boardSeq.nextval, 'eomjh', '엄정화', 'java가 재미 있나요?', '궁금합니다. java가 뭔지요?', '1234', default, default, default);

insert into tbl_board(seq, fk_userid, name, subject, content, pw, readCount, regDate, status)
values(boardSeq.nextval, 'leess', '이순신', '프로그램은 JAVA 가 쉬운가요?', 'java에 대해 궁금합니다', '1234', default, default, default);

insert into tbl_board(seq, fk_userid, name, subject, content, pw, readCount, regDate, status)
values(boardSeq.nextval, 'kimmj', '김민정', 'JSP 가 뭔가요?', '웹페이지를 작성하려고 합니다.', '1234', default, default, default);

insert into tbl_board(seq, fk_userid, name, subject, content, pw, readCount, regDate, status)
values(boardSeq.nextval, 'eomjh', '엄정화', 'Korea VS Japan 라이벌 축구대결', '많은 시청 바랍니다.', '1234', default, default, default);

insert into tbl_board(seq, fk_userid, name, subject, content, pw, readCount, regDate, status)
values(boardSeq.nextval, 'leess', '이순신', '날씨가 많이 쌀쌀합니다.', '건강에 유의하세요~~', '1234', default, default, default);

commit;

select *
from tbl_board
order by seq desc;


-- distinct 와 order by 는 함께 쓸 수 없기 때문에, distinct 와 order by 를 함께 쓰고자 한다면 order by 에 select 에 있는 컬럼을 넣으면 된다.
select distinct name
from tbl_board
where status = 1
and lower(name) like '%'|| lower('정') ||'%'
order by name asc;


select subject
from tbl_board
where status = 1
and lower(subject) like '%'|| lower('ja') ||'%'
order by seq desc


-- 검색이 있을 때 where lower() ~~~, 검색이 없을 때는 where 절 까지만
select count(*)
from tbl_board
where status = 1
and lower(subject) like '%' || lower('J') || '%'



select count(*)
from tbl_board
where status = 1
and lower(subject) like '%'|| lower('') ||'%'
order by seq desc

select seq, fk_userid, name, subject, readCount, regDate, commentCount
from 
(
select row_number() over(order by seq desc) AS rno,
       seq, fk_userid, name, subject, readCount
       , to_char(regDate, 'yyyy-mm-dd hh24:mi:ss') AS regDate
       , commentCount
from tbl_board
where status = 1
and lower(subject) like '%'|| lower('J') || '%'
) V
where rno between 1 and 3





select previousseq, previoussubject
     , seq, fk_userid, name, subject, content, readCount, regDate, pw
     , nextseq, nextsubject
       from
       (
           select lag(seq,1) over(order by seq desc) AS previousseq            
                , lag(subject,1) over(order by seq desc) AS previoussubject                                         
                , seq, fk_userid, name, subject, content, readCount
                , to_char(regDate, 'yyyy-mm-dd hh24:mi:ss') as regDate
                , pw
                , lead(seq,1) over(order by seq desc) AS nextseq
                , lead(subject,1) over(order by seq desc)AS nextsubject
           from tbl_board
           where status = 1
           and lower(subject) like '%'|| lower('Ja') || '%'
       ) V
where V.seq = 11


----------------------------------- 
-- 댓글 읽어오기

select name, content, regDate
from 
(
select row_number() over(order by seq desc) AS rno, name, content, to_char(regDate, 'yyyy-mm-dd hh24:mi:ss') as regDate
from tbl_comment
where status = 1 and parentSeq = 14
) V
where rno between 1 and 3;      -- 댓글 1 페이지

----- 4.3333 페이지를 올림하기 위해 (5) ceil 사용
select ceil(count(*)/3)
from tbl_comment
where status = 1 and parentSeq = 14




------------------------------------------------------------------------------
---- 계층형 쿼리 (답변형 게시판)
---- **** 댓글 및 답변글 및 파일첨부 **** ----


---- **** 답변글쓰기는 일반회원은 불가하며 직원(관리자)들만 답변글쓰기가 가능하도록 한다. **** ----


-- *** tbl_member 테이블에 gradelevel 이라는 컬럼을 추가하겠다. *** --
alter table tbl_member
add gradelevel number default 1;

-- *** 직원(관리자)들에게는 gradelevel 컬럼의 값을 10 으로 부여하겠다. gradelevel 컬럼의 값이 10 인 직원들만 답변글쓰기가 가능하다 *** --
update tbl_member set gradelevel = 10
where userid in('admin','kimmj');

commit;

select *
from tbl_member;



drop table tbl_comment purge;
drop sequence commentSeq;
drop table tbl_board purge;
drop sequence boardSeq;


---------------- 답변형 글쓰기를 위한 테이블 게시판 만들기 (파일첨부 포함)
create table tbl_board
(seq           number                not null    -- 글번호
,fk_userid     varchar2(20)          not null    -- 사용자ID
,name          varchar2(20)          not null    -- 글쓴이 
,subject       Nvarchar2(200)        not null    -- 글제목
,content       Nvarchar2(2000)       not null    -- 글내용   
,pw            varchar2(20)          not null    -- 글암호
,readCount     number default 0      not null    -- 글조회수
,regDate       date default sysdate  not null    -- 글쓴시간
,status        number(1) default 1   not null    -- 글삭제여부   1:사용가능한 글,  0:삭제된글
,commentCount  number default 0      not null    -- 댓글의 개수 

,groupno       number                not null    -- 답변글쓰기에 있어서 그룹번호 
                                                 -- 원글(부모글)과 답변글은 동일한 groupno 를 가진다.
                                                 -- 답변글이 아닌 원글(부모글)인 경우 groupno 의 값은 groupno 컬럼의 최대값(max)+1 로 한다.

,fk_seq        number default 0      not null    -- fk_seq 컬럼은 절대로 foreign key가 아니다.!!!!!!
                                                 -- fk_seq 컬럼은 자신의 글(답변글)에 있어서 
                                                 -- 원글(부모글)이 누구인지에 대한 정보값이다.
                                                 -- 답변글쓰기에 있어서 답변글이라면 fk_seq 컬럼의 값은 
                                                 -- 원글(부모글)의 seq 컬럼의 값을 가지게 되며,
                                                 -- 답변글이 아닌 원글일 경우 0 을 가지도록 한다.

,depthno       number default 0      not null    -- 답변글쓰기에 있어서 답변글 이라면
                                                 -- 원글(부모글)의 depthno + 1 을 가지게 되며,
                                                 -- 답변글이 아닌 원글일 경우 0 을 가지도록 한다.  (한칸 들려쓰기)

,fileName      varchar2(255)                     -- WAS(톰캣)에 저장될 파일명(2020120809271535243254235235234.png)                                       
,orgFilename   varchar2(255)                     -- 진짜 파일명(강아지.png)  // 사용자가 파일을 업로드 하거나 파일을 다운로드 할때 사용되어지는 파일명 
,fileSize      number                            -- 파일크기  

,constraint PK_tbl_board_seq primary key(seq)
,constraint FK_tbl_board_fk_userid foreign key(fk_userid) references tbl_member(userid)
,constraint CK_tbl_board_status check( status in(0,1) )
);
-- Table TBL_BOARD이(가) 생성되었습니다.


create sequence boardSeq
start with 1
increment by 1
nomaxvalue
nominvalue
nocycle
nocache;
-- Sequence BOARDSEQ이(가) 생성되었습니다.


create table tbl_comment
(seq           number               not null   -- 댓글번호
,fk_userid     varchar2(20)         not null   -- 사용자ID
,name          varchar2(20)         not null   -- 성명
,content       varchar2(1000)       not null   -- 댓글내용
,regDate       date default sysdate not null   -- 작성일자
,parentSeq     number               not null   -- 원게시물 글번호
,status        number(1) default 1  not null   -- 글삭제여부
                                               -- 1 : 사용가능한 글,  0 : 삭제된 글
                                               -- 댓글은 원글이 삭제되면 자동적으로 삭제되어야 한다.
,constraint PK_tbl_comment_seq primary key(seq)
,constraint FK_tbl_comment_userid foreign key(fk_userid) references tbl_member(userid)
,constraint FK_tbl_comment_parentSeq foreign key(parentSeq) references tbl_board(seq) on delete cascade
,constraint CK_tbl_comment_status check( status in(1,0) ) 
);
-- Table TBL_COMMENT이(가) 생성되었습니다.

create sequence commentSeq
start with 1
increment by 1
nomaxvalue
nominvalue
nocycle
nocache;
-- Sequence COMMENTSEQ이(가) 생성되었습니다.


desc tbl_board;

-- 데이터를 많이 넣기 위해 아래와 같이 실행한다.

begin
    for i in 1..100 loop
        insert into tbl_board(seq, fk_userid, name, subject, content, pw, readCount, regDate, status, groupno)
        values(boardSeq.nextval, 'leess', '이순신', '이순신 입니다'||i, '안녕하세요? 이순신'|| i ||' 입니다.', '1234', default, default, default, i);
    end loop;
end;
-- PL/SQL 프로시저가 성공적으로 완료되었습니다.


begin
    for i in 101..200 loop
        insert into tbl_board(seq, fk_userid, name, subject, content, pw, readCount, regDate, status, groupno)
        values(boardSeq.nextval, 'eomjh', '엄정화', '엄정화 입니다'||i, '안녕하세요? 엄정화'|| i ||' 입니다.', '1234', default, default, default, i);
    end loop;
end;
-- PL/SQL 프로시저가 성공적으로 완료되었습니다.

commit;


select *
from tbl_board
order by seq desc;

update tbl_board set subject = '문의드립니다. 자바가 무엇인가요?'
where seq = 198

commit;

-- 계층형 답변시 필요한 groupno 의 최댓값
select nvl(max(groupno), 0)
from tbl_board;		

select seq, fk_userid, name, subject, readCount, regDate, commentCount
from 
(
select row_number() over(order by seq desc) AS rno,
       seq, fk_userid, name, subject, readCount
       , to_char(regDate, 'yyyy-mm-dd hh24:mi:ss') AS regDate
       , commentCount
from tbl_board
where status = 1
-- and lower(subject) like '%'|| lower('정화') || '%'
) V
where rno between '1' and '10';	


-- 계층형 쿼리 (답변글을 원글과 짝지어야 한다. 답변글번호인 202번을 원글인 198번과 연결지어야 한다.)
-- 같은 그룹이라면 seq 에 asc (문의글 먼저 올라온 것이므로 문의글-답변글순)
select seq, fk_userid, name, subject, readCount, regDate, commentCount,
       groupno, fk_seq, depthno     
from
(
    select rownum AS rno,
           seq, fk_userid, name, subject, readCount, regDate, commentCount
           , groupno, fk_seq, depthno
    from
    (
        select seq, fk_userid, name, subject, readCount
               , to_char(regDate, 'yyyy-mm-dd hh24:mi:ss') AS regDate
               , commentCount
               , groupno, fk_seq, depthno
        from tbl_board
        where status = 1
     -- and lower(subject) like '%'|| lower('정화') || '%'
        start with fk_seq = 0
        connect by prior seq = fk_seq
        order SIBLINGS by groupno desc, seq asc
    ) V
) T
where rno between '1' and '10';  -- 1 페이지





select *
from tbl_board
order by seq desc;



---- **** 댓글쓰기에 파일첨부까지 한 것 **** ---- (기존 파일첨부 없던 댓글테이블에 3개 컬럼 추가해주기)
alter table tbl_comment
add fileName varchar2(255);     -- WAS(톰캣)에 저장될 파일명(2020120809271535243254235235234.png)   

alter table tbl_comment
add orgFilename varchar2(255);  -- 진짜 파일명(강아지.png)  // 사용자가 파일을 업로드 하거나 파일을 다운로드 할때 사용되어지는 파일명 

alter table tbl_comment
add fileSize number;            -- 파일크기 

-- Table TBL_COMMENT이(가) 변경되었습니다.

select *              
from tbl_comment
where name = '엄정화';


select seq, name, content, to_char(regDate, 'yyyy-mm-dd hh24:mi:ss') AS regDate
     , nvl(fileName,' ') AS fileName
     , nvl(orgFilename, ' ') AS orgFilename
     , nvl(to_char(fileSize), ' ') AS fileSize 
from tbl_comment
where parentseq = 201


select * 
from tbl_comment
order by seq;


------------------------------------------------------------------------------
-- >> tbl_board 테이블에서 content 컬럼의 데이터타입을 CLOB 타입으로 변경하기 << --

create table tbl_board_copy
as
select *
from tbl_board;
-- Table TBL_BOARD_COPY이(가) 생성되었습니다.

desc tbl_board;
-- CONTENT      NOT NULL NVARCHAR2(2000)

-- >> CLOB(4GB 까지 저장 가능한 데이터 타입) 타입을 가지는 새로운 컬럼 추가하기 << --
alter table tbl_board
add imsi_content clob;
-- Table TBL_BOARD이(가) 변경되었습니다.


-- >> 데이터를 복사하기 << -- (content 에 있는 내용들이 imsi_content 로 넘어감)
update tbl_board set imsi_content = content;
-- 210개 행 이(가) 업데이트되었습니다.

commit;
-- 커밋 완료.

-- >> 기존 컬럼 삭제하기 << --
alter table tbl_board 
drop column content;
-- Table TBL_BOARD이(가) 변경되었습니다.


-- >> 새로 추가한 컬럼의 이름을 변경하기 << --
alter table tbl_board
rename column imsi_content to content;
-- Table TBL_BOARD이(가) 변경되었습니다.


-- >> 새로 추가한 컬럼에 NOT NULL 로 설정하기 << --
alter table tbl_board
modify content not null;
-- Table TBL_BOARD이(가) 변경되었습니다.

desc tbl_board;
-- CONTENT      NOT NULL CLOB 

select *
from tbl_board
order by seq desc;

select seq, content, length(content)
from tbl_board
order by seq desc;


delete from tbl_board
where seq = 212;

commit;






------------- >>>>>>>> 일정관리(풀캘린더) 시작 <<<<<<<< -------------

-- *** 캘린더 대분류(내캘린더, 사내캘린더  분류) ***
create table tbl_calendar_large_category 
(lgcatgono   number(3) not null      -- 캘린더 대분류 번호
,lgcatgoname varchar2(50) not null   -- 캘린더 대분류 명
,constraint PK_tbl_calendar_large_category primary key(lgcatgono)
);
-- Table TBL_CALENDAR_LARGE_CATEGORY이(가) 생성되었습니다.

insert into tbl_calendar_large_category(lgcatgono, lgcatgoname)
values(1, '내캘린더');

insert into tbl_calendar_large_category(lgcatgono, lgcatgoname)
values(2, '사내캘린더');

commit;
-- 커밋 완료.

select * 
from tbl_calendar_large_category;


-- *** 캘린더 소분류 *** 
-- (예: 내캘린더중 점심약속, 내캘린더중 저녁약속, 내캘린더중 운동, 내캘린더중 휴가, 내캘린더중 여행, 내캘린더중 출장 등등) 
-- (예: 사내캘린더중 플젝주제선정, 사내캘린더중 플젝요구사항, 사내캘린더중 DB모델링, 사내캘린더중 플젝코딩, 사내캘린더중 PPT작성, 사내캘린더중 플젝발표 등등) 
create table tbl_calendar_small_category 
(smcatgono    number(8) not null      -- 캘린더 소분류 번호
,fk_lgcatgono number(3) not null      -- 캘린더 대분류 번호
,smcatgoname  varchar2(400) not null  -- 캘린더 소분류 명
,fk_userid    varchar2(40) not null   -- 캘린더 소분류 작성자 유저아이디
,constraint PK_tbl_calendar_small_category primary key(smcatgono)
,constraint FK_small_category_fk_lgcatgono foreign key(fk_lgcatgono) 
            references tbl_calendar_large_category(lgcatgono) on delete cascade
,constraint FK_small_category_fk_userid foreign key(fk_userid) references tbl_member(userid)            
);
-- Table TBL_CALENDAR_SMALL_CATEGORY이(가) 생성되었습니다.


create sequence seq_smcatgono
start with 1
increment by 1
nomaxvalue
nominvalue
nocycle
nocache;
-- Sequence SEQ_SMCATGONO이(가) 생성되었습니다.


select *
from tbl_calendar_small_category
order by smcatgono desc;


-- *** 캘린더 일정 *** 
create table tbl_calendar_schedule 
(scheduleno    number                 -- 일정관리 번호
,startdate     date                   -- 시작일자
,enddate       date                   -- 종료일자
,subject       varchar2(400)          -- 제목
,color         varchar2(50)           -- 색상
,place         varchar2(200)          -- 장소
,joinuser      varchar2(4000)         -- 공유자   
,content       varchar2(4000)         -- 내용   
,fk_smcatgono  number(8)              -- 캘린더 소분류 번호
,fk_lgcatgono  number(3)              -- 캘린더 대분류 번호
,fk_userid     varchar2(40) not null  -- 캘린더 일정 작성자 유저아이디
,constraint PK_schedule_scheduleno primary key(scheduleno)
,constraint FK_schedule_fk_smcatgono foreign key(fk_smcatgono) 
            references tbl_calendar_small_category(smcatgono) on delete cascade
,constraint FK_schedule_fk_lgcatgono foreign key(fk_lgcatgono) 
            references tbl_calendar_large_category(lgcatgono) on delete cascade   
,constraint FK_schedule_fk_userid foreign key(fk_userid) references tbl_member(userid) 
);
-- Table TBL_CALENDAR_SCHEDULE이(가) 생성되었습니다.

create sequence seq_scheduleno
start with 1
increment by 1
nomaxvalue
nominvalue
nocycle
nocache;
-- Sequence SEQ_SCHEDULENO이(가) 생성되었습니다.

select *
from tbl_calendar_schedule 
order by scheduleno desc;

commit;

-- 일정 상세 보기
select SD.scheduleno
     , to_char(SD.startdate,'yyyy-mm-dd hh24:mi') as startdate
     , to_char(SD.enddate,'yyyy-mm-dd hh24:mi') as enddate  
     , SD.subject
     , SD.color
     , nvl(SD.place,'-') as place
     , nvl(SD.joinuser,'공유자가 없습니다.') as joinuser
     , nvl(SD.content,'') as content
     , SD.fk_smcatgono
     , SD.fk_lgcatgono
     , SD.fk_userid
     , M.name
     , SC.smcatgoname
from tbl_calendar_schedule SD 
JOIN tbl_member M
ON SD.fk_userid = M.userid
JOIN tbl_calendar_small_category SC
ON SD.fk_smcatgono = SC.smcatgono
where SD.scheduleno = 21;

------------- >>>>>>>> 일정관리(풀캘린더) 끝 <<<<<<<< -------------





------------ ============> 인사관리 <============ -------------
show user;
-- USER이(가) "HR"입니다.


select distinct nvl(department_id, -9999) as department_id           -- 부서번호 겹치는 것이 여러번 나올 필요 없으므로 distinct 사용
from employees
order by department_id asc




select E.department_id, D.department_name, E.employee_id, 
       E.first_name || ' ' || E.last_name AS fullname,
       to_char(E.hire_date, 'yyyy-mm-dd') AS hire_date,
       nvl(E.salary + E.salary*E.commission_pct, E.salary) AS monthsal,
       func_gender(E.jubun) AS gender,
       func_age(E.jubun) AS age 
from employees E left join departments D         -- kimberly 때문에 left join 씀.
on E.department_id = D.department_id
where 1=1   -- 껍데기(where)만 붙어있을 뿐이다. 107개 행이 그대로 나온다. where 절이 중복으로 나올 수 없으므로 껍데기 용도로 where 절을 쓴다.
-- and nvl(E.department_id,-9999) in(-9999,20,40,70,100)   -- 남녀 선택무시하고 부서번호 선택해서만 보고 싶다.
and func_gender(E.jubun) = '여'      -- 부서번호 무시하고 선택한 성별만 본다.
order by E.department_id, E.employee_id;        -- 동일한 부서번호라면 사원번호별로 오름차순.



-- where nvl(E.department_id,-9999) in(-9999,20,40,70,100)   -- 부서번호 (실제 테이블에 -9999가 없기 때문에 동일하게 부서번호에 nvl 해준다.)
-- where func_gender(E.jubun) = '여'                      -- 경우의수 (남자 or 여자만 선택, 둘다 선택)
where nvl(E.department_id,-9999) in(-9999,20,40,70,100)   -- 부서번호 (실제 테이블에 -9999가 없기 때문에 동일하게 부서번호에 nvl 해준다.)
      and 
      func_gender(E.jubun) = '남'
order by E.department_id, E.employee_id;        -- 동일한 부서번호라면 사원번호별로 오름차순.



-- 	employees 테이블에서 부서명별 인원수 및 퍼센티지 가져오기 --
-- 부서번호가 null 인 kimberly 도 나와야 하므로 left join 사용
select nvl(D.department_name,'부서없음') as department_name
     , count(*) AS cnt
     , round( count(*)/(select count(*) from employees) * 100, 2 ) AS percentage  -- 소수부 둘째자리까지 보이고 반올림(round)
from employees E left join departments D
on E.department_id = D.department_id
group by D.department_name
order by cnt desc, department_name asc;


select nvl(D.department_name,'부서없음') as department_name
 , count(*) AS cnt
 , round( count(*)/(select count(*) from employees) * 100, 2 ) AS percentage
from employees E left join departments D
on E.department_id = D.department_id
group by D.department_name
order by cnt desc, department_name asc


select CASE WHEN substr(jubun, 7, 1) IN('1','3') THEN '남' ELSE '여' END AS gender
     , count(*)
from employees

-- 성별 인원수
SELECT gender , COUNT(*) AS cnt
FROM    -- GENDER 라는 column 을 가진 테이블.!!!
(
SELECT CASE WHEN substr(jubun,7,1) IN ('1','3') THEN '남' ELSE '여' END AS gender -- GENDER 가 원래 테이블에 없어서 만들었다!! 없으면, 만들어라.
FROM employees
) V
GROUP BY gender




-- 특정부서명에 근무하는 직원들에 대해서 인원수 및 퍼센티지 가져오기
select func_gender(jubun) as gender
     , count(*) as cnt
     , round(count(*)/(select count(*) from employees)*100, 2) as percentage
from employees E left join departments D
on E.department_id = D.department_id
where D.department_name = 'Shipping'
group by func_gender(jubun)                  -- 성별 구하는 함수를 미리 만들어놓음.
order by gender


select func_gender(jubun) as gender
     , count(*) as cnt
     , round(count(*)/(select count(*) from employees)*100, 2) as percentage
from employees E left join departments D
on E.department_id = D.department_id
where D.department_name is null              -- == !=null 이 아니라 is null 이런식으로 해야 한다.
group by func_gender(jubun)                  -- 성별 구하는 함수를 미리 만들어놓음.
order by gender

--> null 일때와 아닐 때를 구분하기 위해 if 문을 mapper 에 넣어주도록 한다.



-- 스프링 스케줄러를 하기 위해서는 MYMVC_USER 로 바꿔준다. (hr 에는 memeber 테이블이 없기 때문이다.)
show user;
-- USER이(가) "MYMVC_USER"입니다.

desc tbl_member;

------ ==== Spring Scheduler(스프링 스케줄러)를 사용한 email 자동 발송하기 ==== ------
desc tbl_member;

-- 1. 예약테이블 생성하기
create table tbl_reservation
(reservationSeq    number        not null
,fk_userid         varchar2(40)  not null
,reservationDate   date          not null
,mailSendCheck     number default 0 not null  -- 메일발송 했으면 1, 메일발송을 안했으면 0 으로 한다. (메일발송 처리 됐을 때 update 한다.)
,constraint PK_tbl_reservation primary key(reservationSeq)
,constraint FK_tbl_reservation foreign key(fk_userid) references tbl_member(userid)
,constraint CK_tbl_reservation check(mailSendCheck in(0,1))
);
-- Table TBL_RESERVATION이(가) 생성되었습니다.


-- 예약번호 시퀀스 생성하기
create sequence seq_reservation
start with 1
increment by 1
nomaxvalue
nominvalue
nocycle
nocache;
-- Sequence SEQ_RESERVATION이(가) 생성되었습니다.


select *
from tbl_member;

-- 이순신 메일을 존재하는 이메일로 바꾸기 위해 update 함. (실제 메일을 받는지 여부 확인하기 위함.)
select userid,email
from tbl_member
where userid in ('kimmj','leess')

update tbl_member set email = (select email from tbl_member where userid = 'kimmj')
where userid = 'leess';
-- 1 행 이(가) 업데이트되었습니다.

commit;
-- 커밋 완료.


select to_char(sysdate, 'yyyy-mm-dd') AS 오늘날짜
from dual; -- 2022-05-10

insert into tbl_reservation(reservationSeq, fk_userid, reservationDate)
values(seq_reservation.nextval, 'kimmj', to_date('2022-05-12 13:00','yyyy-mm-dd hh24:mi') );

insert into tbl_reservation(reservationSeq, fk_userid, reservationDate)
values(seq_reservation.nextval, 'leess', to_date('2022-05-12 14:00','yyyy-mm-dd hh24:mi') );

insert into tbl_reservation(reservationSeq, fk_userid, reservationDate)
values(seq_reservation.nextval, 'kimmj', to_date('2022-05-13 11:00','yyyy-mm-dd hh24:mi') );

insert into tbl_reservation(reservationSeq, fk_userid, reservationDate)
values(seq_reservation.nextval, 'leess', to_date('2022-05-13 15:00','yyyy-mm-dd hh24:mi') );

commit;
-- 커밋 완료.


select reservationSeq, fk_userid, 
       to_char(reservationDate, 'yyyy-mm-dd hh24:mi:ss') as reservationDate, 
       mailSendCheck
from tbl_reservation
order by reservationSeq desc;



-- *** 오늘로 부터 2일(이틀) 뒤에 예약되어진 회원들을 조회하기 *** --
-- 이틀 뒤에 올 회원들에게 메일을 보낼 것이다. ('이틀뒤에 예약되어 있으니 방문 바랍니다.' 이런식으로 보내겠다.)
select R.reservationSeq, M.userid, M.name, M.email, 
       to_char(R.reservationDate,'yyyy-mm-dd hh24:mi') as reservationDate
from tbl_member M join tbl_reservation R
on M.userid = R.fk_userid
where R.mailSendCheck = 0
and to_char(reservationDate, 'yyyy-mm-dd') = to_char(sysdate+2, 'yyyy-mm-dd');

/*
  update tbl_reservation set mailSendCheck = 1
  where reservationSeq IN ('1','2');
*/



-- ***** Around Advice 를 위해서 만든 테이블임 ***** --
-- 누가 언제 어느페이지에 들어갔는지를 보기 위해 table 생성함.
create table tbl_empManger_accessTime
(seqAccessTime   number
,pageUrl         varchar2(150) not null
,fk_userid       varchar2(40) not null
,clientIP        varchar2(30) not null
,accessTime      varchar2(20) default sysdate not null
,constraint PK_tbl_empManger_accessTime primary key(seqAccessTime)
,constraint FK_tbl_empManger_accessTime foreign key(fk_userid) references tbl_member(userid)
);
-- Table TBL_EMPMANGER_ACCESSTIME이(가) 생성되었습니다.

create sequence seq_seqAccessTime
start with 1
increment by 1
nomaxvalue
nominvalue
nocycle
nocache;
-- Sequence SEQ_SEQACCESSTIME이(가) 생성되었습니다.

select * 
from tbl_empManger_accessTime
order by seqAccessTime desc;









