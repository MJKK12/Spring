use was_db;
-- Was_db을(를) 성공했습니다.

select user();
-- admin@59.14.10.161

select database();
-- was_db

select *
from information_schema.schemata
where schema_name = 'was_db';
/* 
------------------------------------------------------------------------------------------------------------------------
catalog_name    schema_name     default_character_set_name      default_collation_name  sql_path    default_encryption
------------------------------------------------------------------------------------------------------------------------
def	            was_db	        utf8mb4	                        utf8mb4_0900_ai_ci		(null)      NO
*/

-------------------------------------------------------------------------------------------
-- MySQL 버전확인하기 --
select version();
-- 8.0.23

show variables like '%version%'; -- 조금 더 자세한 정보들을 확인
/*
Variable_name                                                    Value                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
---------------------------------------------------------------- ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- 
admin_tls_version                                                TLSv1,TLSv1.1,TLSv1.2,TLSv1.3                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    
immediate_server_version                                         999999                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           
innodb_version                                                   8.0.23                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           
original_server_version                                          999999                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           
protocol_version                                                 10                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               
slave_type_conversions                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
tls_version                                                      TLSv1,TLSv1.1,TLSv1.2,TLSv1.3                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    
version                                                          8.0.23                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           
version_comment                                                  Source distribution                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
version_compile_machine                                          x86_64                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           
version_compile_os                                               Linux                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
version_compile_zlib                                             1.2.11
*/
----------------------------------------------------------------------------------------------

/*
  Oracle(오라클) 데이터베이스에서는 기본적으로 sequence 객체를 사용하여, 기본키로 설정하는 경우가 많다.
  생성하는 방법도 쉽고, 사용하는 방법도 쉬운 오라클 시퀀스 객체에 비해 mysql은 다소 어려운 감이 있다.
  오라클 데이터베이스에 익숙해진 상태에서, mysql로 똑같은 기능을 구현하려니 많이 답답했다.
  이것저것 구글링 검색을 통해 찾아냈다
*/
/* ==== MySQL 시퀀스 기본 생성 예 ==== */
﻿
/* === 1. 먼저 시퀀스 사용 테이블인 seq_mysql 테이블 생성한다 === */
-- DROP TABLE seq_mysql;
CREATE TABLE seq_mysql(
seq_currval       BIGINT UNSIGNED  NOT NULL,
seq_name          VARCHAR(50) NOT NULL,
PRIMARY KEY(seq_name)
)
COMMENT='시퀀스 마스터'
ENGINE=MyISAM;
-- Table SEQ_MYSQL이(가) 생성되었습니다.

/* === 2.시퀀스(auto_increment)를 위한 스토어드 함수 만들기
       get_seq 이라는 함수가 존재한다라면 get_seq 라는 함수를 삭제한다. === */
DROP FUNCTION IF EXISTS get_seq;
/*
FUNCTION was_db.get_seq does not exist

Function FUNCTION이(가) 삭제되었습니다.
*/

CREATE FUNCTION get_seq(p_seq_name VARCHAR(50) CHARSET UTF8)
 RETURNS BIGINT UNSIGNED
 MODIFIES SQL DATA
 SQL SECURITY INVOKER
 DETERMINISTIC
BEGIN
 INSERT INTO seq_mysql
  SET seq_name=IFNULL(p_seq_name,'DEFAULT'), seq_currval=(@v_current_value:=1)
 ON DUPLICATE KEY
  UPDATE seq_currval=(@v_current_value:=seq_currval+1);
 RETURN @v_current_value;
END ;;
-- Function GET_SEQ이(가) 컴파일되었습니다.

/* === 3.시퀀스 생성하기 === */
SELECT get_seq('boardSeq');
-- 1

SELECT get_seq('boardSeq');
-- 2

SELECT get_seq('boardSeq');
-- 3
------------------------------------------------------------------------------


create table tbl_timeTest
(name          varchar(20)
,currentTime_1 datetime default now()
,currentTime_2 timestamp default now()
) comment '시간을 나타내는 datetime 와 timestamp 차이점 알아보기';
-- Table TBL_TIMETEST이(가) 생성되었습니다.

insert into tbl_timeTest(name)
values('현재일시');
-- 기본적으로 MySQL은 자동 커밋 모드가 활성화된 상태로 실행하므로 rollback 을 사용하여 취소할 수 없다.
-- rollback 을 하려면 START TRANSACTION 문을 사용해야 한다.

select * from tbl_timeTest;
/*
 name       currentTime_1           currentTime_2
 --------------------------------------------------------
 현재일시   	2021-12-19T06:06:16	    2021-12-19 06:06:16.0
 
-- 날짜를 나타내는 타입은 datetime 과 timestamp 가 존재한다.
-- 두가지의 차이점은 
-- datetime 은 time_zone 이 변경되더라도 적용되지 않아 시간이 변경되지 않는 반면에 
-- timestamp 은 time_zone 이 변경되면 시간도 변경이 되어진다는 것이다. 
-- 참조 사이트 : https://nesoy.github.io/articles/2020-02/mysql-datetime-timestamp
 
   currentTime_2 컬럼의 값이 한국시간(2021-12-19 15:06:16)으로 나타나지 않고 
   2021-12-19 06:06:16.0 으로 나타난다. 
   그래서 time_zone 을 한국시간으로 변경하는 작업을 해야함.
*/

select @@global.time_zone, @@session.time_zone;
/*
----------------------------------------
@@global.time_zone   @@session.time_zone
----------------------------------------
UTC	                 UTC
*/

SET GLOBAL time_zone='Asia/Seoul';
/*
명령의 157 행에서 시작하는 중 오류 발생 -
SET GLOBAL
오류 보고 -
You have an error in your SQL syntax; check the manual that corresponds to your MySQL server version for the right syntax to use near '' at line 1

Time_zone='Asia/Seoul'을(를) 성공했습니다.
*/

SET time_zone='Asia/Seoul';
-- Time_zone='Asia/Seoul'을(를) 성공했습니다.


select @@global.time_zone, @@session.time_zone;
/*
----------------------------------------
@@global.time_zone   @@session.time_zone
----------------------------------------
UTC	                 Asia/Seoul
*/

create table tbl_person_interest
(seq          int         not null auto_increment  comment '시퀀스 일련번호'
,name         varchar(20) not null                 comment '성명'
,school       varchar(20) not null                 comment '학력'
,color        varchar(20) not null                 comment '좋아하는색상'
,food         varchar(80) null                     comment '좋아하는음식'         
,registerday  timestamp   default now()            comment '등록일시'
,constraint PK_tbl_person_interest primary key(seq)
) comment '개인성향 테이블';
/* 위에서 생성한 시퀀스 get_seq('시퀀스명') 을 사용할 수도 있지만 대신에 
   int 타입의 컬럼명에 auto_increment 을 사용하면 1부터 시작하여 1씩 자동 증가하게 되어진다. */

-- Table TBL_PERSON_INTEREST이(가) 생성되었습니다.

insert into tbl_person_interest(name, school, color, food)
values('이순신','대졸','red','짜장면,짬뽕,탕수육');
-- 1 행 이(가) 삽입되었습니다.

insert into tbl_person_interest(name, school, color, food)
values('엄정화','대학원졸','blue','짬뽕,탕수육,양장피');
-- 1 행 이(가) 삽입되었습니다.

insert into tbl_person_interest(name, school, color, food)
values('서강준','대졸','yellow','짜장면,양장피,팔보채');
-- 1 행 이(가) 삽입되었습니다.

select * from tbl_person_interest;
/*
1	이순신	대졸	    red	    짜장면,짬뽕,탕수육	    2021-12-19 16:31:25.0
2	엄정화	대학원졸	blue	짬뽕,탕수육,양장피	    2021-12-19 16:31:30.0
3	서강준	대졸	    yellow	짜장면,양장피,팔보채	2021-12-19 16:41:12.0
4	나쌍용	대졸	    red	    짜장면,탕수육,양장피	2021-12-19 20:40:40.0

--  time_zone 변경으로 인해 registerday 컬럼의 값이 한국시간(2021-12-19 16:31:25.0)으로 나타난다.
*/

-- === *** DB 전체 테이블 코멘트 조회 *** === --
/*
SELECT table_name, table_comment
FROM information_schema.tables
WHERE table_schema = 'DB 이름' AND table_name = '테이블 이름';
*/

SELECT table_name, table_comment
FROM information_schema.tables
WHERE table_schema = 'was_db'; 
/*
------------------------------------------
TABLE_NAME              TABLE_COMMENT
-------------------------------------------
seq_mysql	            시퀀스 마스터
tbl_person_interest	    개인성향 테이블
tbl_timeTest	        시간을 나타내는 datetime 와 timestamp 차이점 알아보기
*/


-- === *** DB 특정 테이블 코멘트 조회 *** === --
/*
SELECT table_name, table_comment
FROM information_schema.tables
WHERE table_schema = 'DB 이름' AND table_name = '테이블 이름';
*/
SELECT table_name, table_comment
FROM information_schema.tables
WHERE table_schema = 'was_db' AND table_name = 'tbl_person_interest';
/*
------------------------------------------
TABLE_NAME              TABLE_COMMENT
-------------------------------------------
tbl_person_interest	    개인성향 테이블
*/ 


-- === *** DB 전체 테이블 컬럼 코멘트 조회 *** === --
/*
SELECT table_name, column_name, column_comment
FROM information_schema.columns
WHERE table_schema = 'DB 이름';
*/
SELECT table_name, column_name, column_comment
FROM information_schema.columns
WHERE table_schema = 'was_db';
/*
-------------------------------------------------------
table_name              column_name     column_comment
-------------------------------------------------------
seq_mysql	            seq_currval	
seq_mysql	            seq_name	
tbl_person_interest	    color	        좋아하는색상
tbl_person_interest	    food	        좋아하는음식
tbl_person_interest	    name	        성명
tbl_person_interest	    registerday	    등록일시
tbl_person_interest	    school	        학력
tbl_person_interest	    seq	            시퀀스 일련번호
tbl_timeTest	        currentTime_1	
tbl_timeTest	        currentTime_2	
tbl_timeTest	        name	
*/

-- === *** DB 특정 테이블 컬럼 코멘트 조회 *** === --
/*
SELECT table_name, column_name, column_comment
FROM information_schema.columns
WHERE table_schema = 'DB 이름' AND table_name = '테이블 이름';
*/
SELECT table_name, column_name, column_comment
FROM information_schema.columns
WHERE table_schema = 'was_db' AND table_name = 'tbl_person_interest';
/*
-------------------------------------------------------
table_name              column_name     column_comment
-------------------------------------------------------
tbl_person_interest	    color	        좋아하는색상
tbl_person_interest	    food	        좋아하는음식
tbl_person_interest	    name	        성명
tbl_person_interest	    registerday	    등록일시
tbl_person_interest	    school	        학력
tbl_person_interest	    seq	            시퀀스 일련번호
*/

select seq, name, school, color, food, registerday 
from tbl_person_interest 
order by seq desc;

---------------------------------------------------------
-- 현재 시간
select now();
-- 2021-12-21T20:32:44

select now() AS "현재시간"
     , date_add(now(), interval 1 second) AS "현재 시간에 1초 더하기"
     , date_add(now(), interval 1 minute) AS "현재 시간에 1분 더하기"
     , date_add(now(), interval 1 hour) AS "현재 시간에 1시간 더하기"
     , date_add(now(), interval 1 day) AS "현재 시간에 1일 더하기"
     , date_add(now(), interval 1 month) AS "현재 시간에 1달 더하기"
     , date_add(now(), interval 1 year) AS "현재 시간에 1년 더하기";
     
select now() AS "현재시간"
     , date_add(now(), interval -1 second) AS "현재 시간에 1초 빼기"
     , date_add(now(), interval -1 minute) AS "현재 시간에 1분 빼기"
     , date_add(now(), interval -1 hour) AS "현재 시간에 1시간 빼기"
     , date_add(now(), interval -1 day) AS "현재 시간에 1일 빼기"
     , date_add(now(), interval -1 month) AS "현재 시간에 1달 빼기"
     , date_add(now(), interval -1 year) AS "현재 시간에 1년 빼기";  


-- MySQL 제약조건(CONSTRAINT) 종류 --
/*
    NOT NULL
    UNIQUE
    PRIMARY KEY
    FOREIGN KEY
    CHECK
    DEFAULT
*/

-- 제약조건(constraint) 확인 하기 --
select * 
from information_schema.table_constraints
where table_name = 'tbl_person_interest';


-- 컬럼에 DEFAULT 제약 조건을 설정하는 문법
/*
[문법]
1. ALTER TABLE 테이블이름
   MODIFY COLUMN 컬럼명 컬럼명타입 DEFAULT 기본값

2. ALTER TABLE 테이블이름
   ALTER 컬럼명 SET DEFAULT 기본값
*/


