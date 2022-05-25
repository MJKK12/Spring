package com.spring.favorite.service;

import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spring.favorite.model.InterFavoriteDAO;
import com.spring.favorite.model.PersonFavoriteVO;


@Service
public class FavoriteService implements InterFavoriteService {

	@Autowired
	private InterFavoriteDAO dao;

	@Override
	public int personFavoriteRegister(PersonFavoriteVO vo) throws SQLException {
		int n = dao.personFavoriteRegister(vo);
		return n;
	}

	@Override
	public List<PersonFavoriteVO> personFavoriteSelectAll() throws SQLException {
		List<PersonFavoriteVO> list = dao.personFavoriteSelectAll();
		return list;
	}

	@Override
	public PersonFavoriteVO personFavoriteDetail(String seq) throws SQLException {
		PersonFavoriteVO vo = dao.personFavoriteDetail(seq);
		return vo;
	}

	@Override
	public int personFavoriteDelete(String seq) throws SQLException {
		int n = dao.personFavoriteDelete(seq);
		return n;
	}

	@Override
	public int personFavoriteUpdateEnd(PersonFavoriteVO vo) throws SQLException {
		int n = dao.personFavoriteUpdateEnd(vo);
		return n;
	}

	
	
	
}
