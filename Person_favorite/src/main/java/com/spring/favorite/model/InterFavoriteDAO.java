package com.spring.favorite.model;

import java.sql.SQLException;
import java.util.List;

public interface InterFavoriteDAO {

	// 개인성향을 입력(insert)해주는 추상메소드(미완성메소드)
	int personFavoriteRegister(PersonFavoriteVO vo) throws SQLException;

	// tbl_person_interest 테이블에 저장되어진 모든 행들을 select 해주는 추상메소드(미완성메소드) 
	List<PersonFavoriteVO> personFavoriteSelectAll() throws SQLException;

	// tbl_person_interest 테이블에 저장되어진 특정 1개 행만 select 해주는 추상메소드(미완성메소드) 
	PersonFavoriteVO personFavoriteDetail(String seq) throws SQLException;

	// tbl_person_interest 테이블에 특정 1개 행만 delete 해주는 추상메소드(미완성메소드)  
	int personFavoriteDelete(String seq) throws SQLException;

	// tbl_person_interest 테이블에 특정 1개 행만 update 해주는 추상메소드(미완성메소드) 
	int personFavoriteUpdateEnd(PersonFavoriteVO vo) throws SQLException;

}
