package com.spring.favorite.model;

import java.sql.SQLException;
import java.util.List;

import javax.annotation.Resource;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;


@Repository 
public class FavoriteDAO implements InterFavoriteDAO {

	@Resource
	private SqlSessionTemplate sqlsession;

	@Override
	public int personFavoriteRegister(PersonFavoriteVO vo) throws SQLException {
		int n = sqlsession.insert("favorite.personFavoriteRegister", vo);
		return n;
	}

	@Override
	public List<PersonFavoriteVO> personFavoriteSelectAll() throws SQLException {
		List<PersonFavoriteVO> list = sqlsession.selectList("favorite.personFavoriteSelectAll");
		return list;
	}

	@Override
	public PersonFavoriteVO personFavoriteDetail(String seq) throws SQLException {
		PersonFavoriteVO vo = sqlsession.selectOne("favorite.personFavoriteDetail", seq);
		return vo;
	}

	@Override
	public int personFavoriteDelete(String seq) throws SQLException {
		int n = sqlsession.delete("favorite.personFavoriteDelete", seq);
		return n;
	}

	@Override
	public int personFavoriteUpdateEnd(PersonFavoriteVO vo) throws SQLException {
		int n = sqlsession.update("favorite.personFavoriteUpdateEnd", vo);
		return n;
	} 
	
}
