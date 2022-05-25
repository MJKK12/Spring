package com.spring.favorite.controller;

import java.sql.SQLException;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.spring.favorite.model.PersonFavoriteVO;
import com.spring.favorite.service.InterFavoriteService;


@Component
@Controller
public class FavoriteController {

	@Autowired 
	private InterFavoriteService service;
	
	
	@RequestMapping(value="/index.sist")
	public ModelAndView index(ModelAndView mav) {
		
		mav.setViewName("main/index");
		//   /WEB-INF/views/main/index.jsp 파일을 생성한다.
		
		return mav;
	}
	

	@RequestMapping(value="/personFavoriteRegister.sist", method= {RequestMethod.POST}) 
	public ModelAndView personFavoriteRegister(ModelAndView mav, HttpServletRequest request) {
		
		try {
			String name = request.getParameter("name");
			String school = request.getParameter("school");
			String color = request.getParameter("color");
			String[] food_arr = request.getParameterValues("food");
			
			String food = "없음";
			if(food_arr != null) {
				food = String.join(",", food_arr);
			}
			
			PersonFavoriteVO vo = new PersonFavoriteVO();
			vo.setName(name);
			vo.setSchool(school);
			vo.setColor(color);
			vo.setFood(food);
			
			int n = service.personFavoriteRegister(vo);
			if(n==1) {
				mav.setViewName("personFavorite/register_success");
			}
			else {
				mav.setViewName("error/failMsg");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			mav.setViewName("error/failMsg");
		}
			
		return mav;
	}
	
	
	@RequestMapping(value="/personFavoriteSelectAll.sist")
	public ModelAndView personFavoriteSelectAll(ModelAndView mav) {
		
		try {
			List<PersonFavoriteVO> list = service.personFavoriteSelectAll(); 
			mav.addObject("list", list);
			mav.setViewName("personFavorite/selectAll");
		} catch (SQLException e) {
			e.printStackTrace();
			mav.setViewName("error/failMsg");
		}
		
		return mav;
	}
	
	
	@RequestMapping(value="/personFavoriteDetail.sist")
	public ModelAndView personFavoriteDetail(ModelAndView mav, HttpServletRequest request) {
		
		String seq = request.getParameter("seq");
		
		try {
			PersonFavoriteVO vo = service.personFavoriteDetail(seq);
			
			String foodImgFileName = null;
				
			if(vo.getFood() != null) {
					
				String[] food_arr = vo.getFood().split("\\,");
					
				StringBuilder sb = new StringBuilder();
					
				for(int i=0; i<food_arr.length; i++) {
						
					switch (food_arr[i]) {
						case "짜장면":
							sb.append("jjm.png");
							break;
							
						case "짬뽕":
							sb.append("jjbong.png");
							break;
							
						case "탕수육":
							sb.append("tangsy.png");
							break;
							
						case "양장피":
							sb.append("yang.png");
							break;
							
						case "팔보채":
							sb.append("palbc.png");
							break;	
					}// end of switch--------------
						
					if(i < food_arr.length-1) {
						sb.append(",");
					}
						
				}// end of for----------------------
					
				foodImgFileName = sb.toString();
			}
						
			Map<String, Object> map = new HashMap<>();
			map.put("vo", vo);
			map.put("foodImgFileName", foodImgFileName);
			
			mav.addObject("map", map);
			mav.setViewName("personFavorite/detail");
		} catch (SQLException e) {
			e.printStackTrace();
			mav.setViewName("error/failMsg");
		}
		return mav;
	}
	
	
	@RequestMapping(value="/personFavoriteDelete.sist", method= {RequestMethod.POST}) 
	public ModelAndView personFavoriteDelete(ModelAndView mav, HttpServletRequest request) {
		
		try {
			String seq = request.getParameter("seq");
			String name = request.getParameter("name");
			
			int n = service.personFavoriteDelete(seq);
			
			if(n==1) {
				String delInfo = "개인성향번호 "+seq+"번 "+name+" 님의 개인성향 정보를 삭제했습니다.";
				
				mav.addObject("delInfo", delInfo);
				mav.setViewName("personFavorite/delete_success");
			}
			else {
				mav.setViewName("error/failMsg");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			mav.setViewName("error/failMsg");
		}
			
		return mav;
	}
	
	
	
	@RequestMapping(value="/personFavoriteUpdate.sist", method= {RequestMethod.POST}) 
	public ModelAndView personFavoriteUpdate(ModelAndView mav, HttpServletRequest request) {
		
		try {
			String seq = request.getParameter("seq");
			
			PersonFavoriteVO vo = service.personFavoriteDetail(seq); 
			if(vo != null) {
				mav.addObject("vo", vo);
				mav.setViewName("personFavorite/update_form");
			}
			else {
				mav.setViewName("error/failMsg");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			mav.setViewName("error/failMsg");
		}
			
		return mav;
	}
	
	
	
	@RequestMapping(value="/personFavoriteUpdateEnd.sist", method= {RequestMethod.POST}) 
	public String personFavoriteUpdateEnd(HttpServletRequest request) {
		
		String pageName = "";
		
		try {
			String seq = request.getParameter("seq");
			String name = request.getParameter("name");
			String school = request.getParameter("school");
			String color = request.getParameter("color");
			String[] food_arr = request.getParameterValues("food");
			
			String food = "없음";
			if(food_arr != null) {
				food = String.join(",", food_arr);
			}
			
			PersonFavoriteVO vo = new PersonFavoriteVO();
			vo.setSeq(Integer.parseInt(seq));
			vo.setName(name);
			vo.setSchool(school);
			vo.setColor(color);
			vo.setFood(food);
			
			int n = service.personFavoriteUpdateEnd(vo);
			
			if(n==1) {
				return "redirect:/personFavoriteDetail.sist?seq="+seq; 
			}
			else {
				pageName = "error/failMsg";
			}
		} catch (SQLException e) {
			e.printStackTrace();
			pageName = "error/failMsg";
		}
			
		return pageName;
	}
	
}
