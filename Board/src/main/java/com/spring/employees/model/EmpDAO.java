package com.spring.employees.model;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class EmpDAO implements InterEmpDAO {

	@Resource
	private SqlSessionTemplate sqlsession_2;
	
	@Resource
	private SqlSessionTemplate sqlsession;


	// === employees 테이블에서 근무중인 사원들의 부서번호 가져오기
	@Override
	public List<String> deptIdList() {
		List<String> deptIdList = sqlsession_2.selectList("hr.deptIdList");	// where 절 없이 모두 다 읽어옴.
		return deptIdList;
	}


	// employess 테이블에서 조건에 만족하는 사원들을 조회한다.(가져오기)
	@Override
	public List<Map<String, String>> empList(Map<String, Object> paraMap) {
		List<Map<String, String>> empList = sqlsession_2.selectList("hr.empList", paraMap);
		return empList;
	}


	// === employees 테이블에서 부서명별 인원수 및 퍼센티지 가져오기
	@Override
	public List<Map<String, String>> employeeCntByDeptname() {
		List<Map<String, String>> deptnamePercentageList = sqlsession_2.selectList("hr.employeeCntByDeptname");
		// 파라미터는 없으므로 생략.
		return deptnamePercentageList;
	}


	// === employees 테이블에서 성별 인원수 및 퍼센티지 가져오기 === //
	@Override
	public List<Map<String, String>> employeeCntByGender() {
		List<Map<String, String>> genderPercentageList = sqlsession_2.selectList("hr.employeeCntByGender");
		// 파라미터는 없으므로 생략.
		return genderPercentageList;
	}


	// === 차트 그리기(Ajax) 특정 부서명에 근무하는 직원들의 성별 인원수 및 퍼센티지 가져오기 === //
	@Override
	public List<Map<String, String>> genderCntSpecialDeptname(Map<String, String> paraMap) {
		List<Map<String, String>> genderPercentageList = sqlsession_2.selectList("hr.genderCntSpecialDeptname", paraMap);
		return genderPercentageList;
	}


	// 인사관리 페이지에 접속한 페이지URL, 사용자ID, 접속IP주소, 접속시간을 기록으로 DB에 insert 하도록 한다.
	@Override
	public void insert_accessTime(Map<String, String> paraMap) {
		sqlsession.insert("board.insert_accessTime", paraMap);
	}
	
}
