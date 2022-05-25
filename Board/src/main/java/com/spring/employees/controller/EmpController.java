package com.spring.employees.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.spring.board.common.MyUtil;
import com.spring.employees.service.InterEmpService;

@Controller
public class EmpController {

	@Autowired
	private InterEmpService service;
	
	// === #175. 다중 체크박스를 사용시 sql 문에서 in 절을 처리하는 예제 === //
	@RequestMapping(value = "/emp/empList.action")		
	public String empmanager_employeeInfoView(HttpServletRequest request, HttpServletResponse response) {
		
	//	getCurrentURL(request);	// 로그인 또는 로그아웃을 했을 때 현재 보이던 그 페이지로 그대로 돌아가기 위한 메소드 호출
	//  로그인과 관계 없으므로 주석처리 한다.
		
		// employees 테이블에서 근무중인 사원들의 부서번호 가져오기
		List<String> deptIdList = service.deptIdList();
		
		String str_DeptId = request.getParameter("str_DeptId");	// 보고싶은 부서 번호를 받아온다.
	//	System.out.println("**** 확인용 str_DeptId :" + str_DeptId);
		// 아래와 같이 경우의수가 나뉜다. (부서 선택하지 않았을 때 / 선택했을 때)
		// 확인용 : str_DeptId => 				(체크박스에서 하나도 선택하지 않았을 때)
		// 확인용 : str_DeptId => "20,40"			문자열로 들어온다.
		// 확인용 : str_DeptId => "-9999,20,40"		
		// **** 확인용 str_DeptId : 10,90,100
		/*
		 **** 확인용 str_DeptId :null (기본값)
		 */

		String gender = request.getParameter("gender");			// 보고싶은 성별을 받아온다.
	//	System.out.println("**** 확인용 gender:" + gender);
		/*
		// 아래와 같이 경우의수가 나뉜다. (성별 선택하지 않았을 때 / 선택했을 때)
		 	**** 확인용 gender: 여
		 	**** 확인용 gender:null (기본값)
		 */
		
		// 성별은 모두 보고자 하고, 부서번호는 선택했을 때
		/*
			 **** 확인용 str_DeptId :-9999,10,20
			 **** 확인용 gender:			 
			 **** 확인용 str_DeptId :10,20
			 **** 확인용 gender:남
		 */
		
		Map<String, Object> paraMap = new HashMap<>();		
		// String 과 String[] 을 모두 받기 위해 value 자리에 Object 를 사용한다.
		// 위에서 받아온 부서번호와 성별을 Map 에 담아서 DB 에 보내주도록 하자.
		// *** 받아온 배열값을 그대로 DB 로 보내는 것이 아니라 과정을 한번 더 거쳐야 한다!!!
		// in 절을 사용할 것이다. Spring 의 Mybatis 에서 사용하는데, in 절을 사용하려면 "-9999,20,40" 값이 통째로 들어가는 것이 아니라,
		// 리스트 아니면 배열로 바꿔야 Mapper 에서 인식할 수 있다.
		// 1. 문자열을 쪼개야 한다. (Split)
		// "문자열".split("구분자") ===	★★★★★★★★★ 매우 자주쓰임 ★★★★★★★★★ (특히 ,를 자주 쓴다.)
		//        "문자열"을 "구분자"로 잘라서 String 타입의 배열로 돌려주는 것이다. (split, 쪼갠다!)
		// foodArr.length => 0
		// ==> 0 이 나옴. 즉 . 단독만으로는 구분자로 인식을 못함.
		// split 사용시 구분자로 . | / 등 특수문자를 사용하려고 할 경우에는 구분자로 인식을 못할 경우가 많으므로
		// 구분자 앞에 \\ 를 붙이거나 구분자를 [] 로 씌워주면 된다. 즉, [구분자] 이렇게 말이다.
		
		// 부서번호
		if(str_DeptId != null && !"".equals(str_DeptId)) {
			// 배열값이 null 도 아니고, ""도 아니라면 in절을 쓰기 위해 배열로 바꿔준 후 맵에 담는다.
			String[] arr_DeptId = str_DeptId.split("\\,");		//  '\\' 를 구분자 앞에 써주도록 한다. (습관적으로 쓰도록 한다.)
			// 문자열을 String 타입의 배열로 쪼갰다.
			paraMap.put("arr_DeptId",arr_DeptId);		// String 타입의 배열이기 때문에 Map 에 들어갈 수 없다.
		
			// view 단 페이지에서 사용자가 체크한 부서번호 값을 그대로 유지시키기 위함.
			request.setAttribute("str_DeptId", str_DeptId);		// 배열이 아닌 넘어온 문자열을 넘겨주겠다.
		}
		
		
		// 성별
		if(gender != null && !"".equals(gender)) {
			// 배열값이 null 도 아니고, ""도 아니라면 in절을 쓰기 위해 배열로 바꿔준다.
			paraMap.put("gender", gender);
			
			// view 단 페이지에서 사용자가 체크한 성별 값을 그대로 유지시키기 위함.
			request.setAttribute("gender", gender);		// 배열이 아닌 넘어온 문자열을 넘겨주겠다.
		}
		
		
		// VO 를 만들지 않았으니 Map 을 쓰겠다. (Map 과 VO 쓰임구분 잘 기억해두기, select 에서 map 자주 사용)
		// employess 테이블에서 조건에 만족하는 사원들을 조회한다.(가져오기)
		List<Map<String, String>> empList = service.empList(paraMap);		// map 에 사용자가 선택한 부서번호, 성별이 담긴 form 태그 내용을 보내주자.
		
		request.setAttribute("deptIdList", deptIdList);	// view 단에 보내주자.
		request.setAttribute("empList", empList);		// view 단에 보내주자. (map 에 넣어옴)
						
		return "emp/empList.tiles2";
		// /WEB-INF/views/tiles2/emp/empList.jsp 파일을 생성한다.
	}

	
	// === #176. Excel 파일로 다운받기 예제 === //
	@RequestMapping(value = "/excel/downloadExcelFile.action", method = {RequestMethod.POST})	
	public String downloadExcelFile(HttpServletRequest request, Model model) {	// Model model 은 ModelAndView 가 아니라 Model 이기때문에 저장소 역할만 하는 것임. 
	
		String str_DeptId = request.getParameter("str_DeptId");	// 보고싶은 부서 번호를 받아온다.
	//	System.out.println("**** 확인용 str_DeptId :" + str_DeptId);
		// 아래와 같이 경우의수가 나뉜다. (부서 선택하지 않았을 때 / 선택했을 때)
		// 확인용 : str_DeptId => 				(체크박스에서 하나도 선택하지 않았을 때)
		// 확인용 : str_DeptId => "20,40"			문자열로 들어온다.
		// 확인용 : str_DeptId => "-9999,20,40"		
		// **** 확인용 str_DeptId : 10,90,100
		/*
		 **** 확인용 str_DeptId :null (기본값)
		 */

		String gender = request.getParameter("gender");			// 보고싶은 성별을 받아온다.
	//	System.out.println("**** 확인용 gender:" + gender);
		/*
		// 아래와 같이 경우의수가 나뉜다. (성별 선택하지 않았을 때 / 선택했을 때)
		 	**** 확인용 gender: 여
		 	**** 확인용 gender:null (기본값)
		 */
		
		// 성별은 모두 보고자 하고, 부서번호는 선택했을 때
		/*
			 **** 확인용 str_DeptId :-9999,10,20
			 **** 확인용 gender:			 
			 **** 확인용 str_DeptId :10,20
			 **** 확인용 gender:남
		 */
		
		Map<String, Object> paraMap = new HashMap<>();		
		// String 과 String[] 을 모두 받기 위해 value 자리에 Object 를 사용한다.
		// 위에서 받아온 부서번호와 성별을 Map 에 담아서 DB 에 보내주도록 하자.
		// *** 받아온 배열값을 그대로 DB 로 보내는 것이 아니라 과정을 한번 더 거쳐야 한다!!!
		// in 절을 사용할 것이다. Spring 의 Mybatis 에서 사용하는데, in 절을 사용하려면 "-9999,20,40" 값이 통째로 들어가는 것이 아니라,
		// 리스트 아니면 배열로 바꿔야 Mapper 에서 인식할 수 있다.
		// 1. 문자열을 쪼개야 한다. (Split)
		// "문자열".split("구분자") ===	★★★★★★★★★ 매우 자주쓰임 ★★★★★★★★★ (특히 ,를 자주 쓴다.)
		//        "문자열"을 "구분자"로 잘라서 String 타입의 배열로 돌려주는 것이다. (split, 쪼갠다!)
		// foodArr.length => 0
		// ==> 0 이 나옴. 즉 . 단독만으로는 구분자로 인식을 못함.
		// split 사용시 구분자로 . | / 등 특수문자를 사용하려고 할 경우에는 구분자로 인식을 못할 경우가 많으므로
		// 구분자 앞에 \\ 를 붙이거나 구분자를 [] 로 씌워주면 된다. 즉, [구분자] 이렇게 말이다.
		
		// 부서번호
		if(str_DeptId != null && !"".equals(str_DeptId)) {
			// 배열값이 null 도 아니고, ""도 아니라면 in절을 쓰기 위해 배열로 바꿔준 후 맵에 담는다.
			String[] arr_DeptId = str_DeptId.split("\\,");		//  '\\' 를 구분자 앞에 써주도록 한다. (습관적으로 쓰도록 한다.)
			// 문자열을 String 타입의 배열로 쪼갰다.
			paraMap.put("arr_DeptId",arr_DeptId);		// String 타입의 배열이기 때문에 Map 에 들어갈 수 없다.
		
		}
		
		
		// 성별
		if(gender != null && !"".equals(gender)) {
			// 배열값이 null 도 아니고, ""도 아니라면 in절을 쓰기 위해 배열로 바꿔준다.
			paraMap.put("gender", gender);
			
		}
		
		
		// VO 를 만들지 않았으니 Map 을 쓰겠다. (Map 과 VO 쓰임구분 잘 기억해두기, select 에서 map 자주 사용)
		// employess 테이블에서 조건에 만족하는 사원들을 조회한다.(가져오기)
		List<Map<String, String>> empList = service.empList(paraMap);		// map 에 사용자가 선택한 부서번호, 성별이 담긴 form 태그 내용을 보내주자.

		// 위까지는 DB 에서 조회해온 것이다. 이 조회해온 결과물을 excel 파일로 만들자.		
		// web페이지에 보여주는 것이 아니라 다운로드를 받는 것이다.
		
		
		
		// ***** === 조회결과물인 empList 를 가지고 엑셀 시트 생성하기 === ***** //
		// Excel파일로저장한결과.png 이미지를 참고하자.
	    // 시트를 생성하고, 행을 생성하고, 셀을 생성하고, 셀안에 내용을 넣어주면 된다.
		SXSSFWorkbook workbook = new SXSSFWorkbook();		// POI (ms오피스 관련 라이브러리, 자바에서 읽고 쓸 수 있음)
		// workbook = 엑셀파일
		
		// 시트 생성
		SXSSFSheet sheet = workbook.createSheet("HR사원정보");	// 엑셀 파일 아래에 보이는 것
		
		// 시트 열 너비 설정(열 너비까지 잡아줘야 한다.) 여기서 열이 8개라고 하면, 8개 행에 대해 열 너비 설정한다.
		workbook.createCellStyle();
		sheet.setColumnWidth(0, 2000);	// sheet 는 컬럼이 0번째 부터 시작한다.
		sheet.setColumnWidth(1, 4000);
	    sheet.setColumnWidth(2, 2000);
	    sheet.setColumnWidth(3, 4000);
	    sheet.setColumnWidth(4, 3000);
	    sheet.setColumnWidth(5, 2000);
	    sheet.setColumnWidth(6, 1500);
	    sheet.setColumnWidth(7, 1500);		
	    
	    // 행의 위치를 나타내는 변수 (n번째 행은 어떤 내용이 들어와야 하는지 다 잡아줘야 한다. 0 번째부터 시작 (맨윗칸부터 하겠다.))
	    int rowLocation = 0;
	    
	    
		////////////////////////////////////////////////////////////////////////////////////////
		// CellStyle 정렬하기(Alignment)
		// CellStyle 객체를 생성하여 Alignment 세팅하는 메소드를 호출해서 인자값을 넣어준다.
		// 아래는 HorizontalAlignment(가로)와 VerticalAlignment(세로)를 모두 가운데 정렬 시켰다.
		CellStyle mergeRowStyle = workbook.createCellStyle();				// 행병합
		mergeRowStyle.setAlignment(HorizontalAlignment.CENTER);				// 가로기준 정렬 - center로 함 ( HorizontalAlign ), 가운데 정렬
		mergeRowStyle.setVerticalAlignment(VerticalAlignment.CENTER);		// 세로기준 정렬 - center로 함 ( VerticalAlign ), 가운데 정렬
		// import org.apache.poi.ss.usermodel.VerticalAlignment 으로 해야함.	    
	    
		CellStyle headerStyle = workbook.createCellStyle();					// 노란색 부서번호 | 부서명 |  << 이부분 디자인 
		headerStyle.setAlignment(HorizontalAlignment.CENTER);
		headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
	    
		// CellStyle 배경색(ForegroundColor)만들기
        // setFillForegroundColor 메소드에 IndexedColors Enum인자를 사용한다.
        // setFillPattern은 해당 색을 어떤 패턴으로 입힐지를 정한다.
		mergeRowStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());		// IndexedColors.DARK_BLUE.getIndex() 는 색상(남색)의 인덱스값을 리턴시켜준다.
		mergeRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);					// solid 는 실선
		
		headerStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());		// header 부분 배경색
		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);					// IndexedColors.LIGHT_YELLOW.getIndex() 는 연한노랑의 인덱스값을 리턴시켜준다.
		
		// CellStyle 천단위 쉼표, 금액 (엑셀 다운로드 시 월급 3자리 마다 , 찍기)
        CellStyle moneyStyle = workbook.createCellStyle();
        moneyStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0"));
		
		
		// Cell 폰트(Font) 설정하기
        // 폰트 적용을 위해 POI 라이브러리의 Font 객체를 생성해준다.
        // 해당 객체의 세터를 사용해 폰트를 설정해준다. 대표적으로 글씨체, 크기, 색상, 굵기만 설정한다.
        // 이후 CellStyle의 setFont 메소드를 사용해 인자로 폰트를 넣어준다.
		Font mergeRowFont = workbook.createFont();				// font의 import org.apache.poi.ss.usermodel.Font; 으로 한다.
		mergeRowFont.setFontName("나눔고딕");
		mergeRowFont.setFontHeight((short)500);					// 500 은 기본이 int 이기 때문에 setFontHeight에 맞춰서 short 로 강제형변환 한다.
		mergeRowFont.setColor(IndexedColors.WHITE.getIndex());	// 글자색 흰색
		mergeRowFont.setBold(true);								// 글자크기 굵게/안굵게 (true/false) , boolean 타입
		
		mergeRowStyle.setFont(mergeRowFont);					// mergeRowFont 에 적용한 것들을 mergeRowStyle (행)에 적용해주겠다.
		
		
		// CellStyle 테두리 Border
        // 테두리는 각 셀마다 상하좌우 모두 설정해준다.
        // setBorderTop, Bottom, Left, Right 메소드와 인자로 POI라이브러리의 BorderStyle 인자를 넣어서 적용한다.
		headerStyle.setBorderTop(BorderStyle.THICK);		// 	(테두리를 두껍게)
		headerStyle.setBorderBottom(BorderStyle.THICK);		// 	(테두리를 두껍게)
		headerStyle.setBorderLeft(BorderStyle.THIN);		// 	(테두리를 얇게)
		headerStyle.setBorderRight(BorderStyle.THIN);		// 	(테두리를 얇게)
		
		
		// Cell Merge 셀 병합시키기
        /* 셀병합은 시트의 addMergeRegion 메소드에 CellRangeAddress 객체를 인자로 하여 병합시킨다.
           CellRangeAddress 생성자의 인자로(시작 행, 끝 행, 시작 열, 끝 열) 순서대로 넣어서 병합시킬 범위를 정한다. 배열처럼 시작은 0부터이다.  
        */
        // 병합할 행 만들기
		Row mergeRow = sheet.createRow(rowLocation);		// rowLocation 에 지정된 수에 (n번째) 행을 하나 만든다. 엑셀에서 행의 시작은 0부터 시작한다. (사진에서 행이 1로 시작할지라도, 0번째부터 시작한다.)
		
		// 병합할 행에 "우리회사 사원정보" 로 셀을 만들어 셀에 스타일을 주기
	    // 8개를 병합해야 하므로 for 문 돌린다
		for(int i=0; i<8; i++) {
			Cell cell = mergeRow.createCell(i);		// cell 을 8개 만큼 만든다.
			cell.setCellStyle(mergeRowStyle);
			cell.setCellValue("우리회사 사원정보");		// cell 마다 value 값을 다 넣어준다.
		}// end of for-----------------------------------------
		
		// 셀 병합하기 (각각 다 만든다음에 합쳐주도록 한다.)
		sheet.addMergedRegion(new CellRangeAddress(rowLocation, rowLocation, 0, 7)); // 시작 행, 끝 행, 시작 열, 끝 열 
        ////////////////////////////////////////////////////////////////////////////////////////////////
		// 행, 열 모두 0부터 시작한다. (자릿수는 1부터 시작하지만 위치값은 0부터 시작이다. 위치값을 알면 병합할 수 있다.)
		
		// 헤더 행 생성
        Row headerRow = sheet.createRow(++rowLocation); // 엑셀에서 행의 시작은 0 부터 시작한다.
                                                        // ++rowLocation는 전위연산자임. (0번째 행 만들고 그 다음행에 만들어야 하기 때문에 전위연산자 사용한다.)
        												// 후위연산자가 아님 (주의!)
		// 해당 열의 첫번째 열 셀 생성
        Cell headerCell = headerRow.createCell(0);	// 0번째 열에 셀 생성, 엑셀에서 열의 시작은 0부터 시작한다.
        headerCell.setCellValue("부서번호");
        headerCell.setCellStyle(headerStyle);
        
		// 해당 열의 두번째 열 셀 생성
        headerCell = headerRow.createCell(1);	// 1번째 열에 셀 생성, 엑셀에서 열의 시작은 0부터 시작한다.
        headerCell.setCellValue("부서명");
        headerCell.setCellStyle(headerStyle);
        
		// 해당 열의 세번째 열 셀 생성
        headerCell = headerRow.createCell(2);	// 2번째 열에 셀 생성, 엑셀에서 열의 시작은 0부터 시작한다.
        headerCell.setCellValue("사원번호");
        headerCell.setCellStyle(headerStyle);

		// 해당 열의 네번째 열 셀 생성
        headerCell = headerRow.createCell(3);	// 3번째 열에 셀 생성, 엑셀에서 열의 시작은 0부터 시작한다.
        headerCell.setCellValue("사원명");
        headerCell.setCellStyle(headerStyle);

		// 해당 열의 다섯번째 열 셀 생성
        headerCell = headerRow.createCell(4);	// 4번째 열에 셀 생성, 엑셀에서 열의 시작은 0부터 시작한다.
        headerCell.setCellValue("입사일자");
        headerCell.setCellStyle(headerStyle);

		// 해당 열의 여섯번째 열 셀 생성
        headerCell = headerRow.createCell(5);	// 5번째 열에 셀 생성, 엑셀에서 열의 시작은 0부터 시작한다.
        headerCell.setCellValue("월급");
        headerCell.setCellStyle(headerStyle);

		// 해당 열의 일곱번째 열 셀 생성
        headerCell = headerRow.createCell(6);	// 6번째 열에 셀 생성, 엑셀에서 열의 시작은 0부터 시작한다.
        headerCell.setCellValue("성별");
        headerCell.setCellStyle(headerStyle);

		// 해당 열의 여덟번째 열 셀 생성
        headerCell = headerRow.createCell(7);	// 7번째 열에 셀 생성, 엑셀에서 열의 시작은 0부터 시작한다.
        headerCell.setCellValue("나이");        
        headerCell.setCellStyle(headerStyle);
        
        // DB 에서 HR 사원정보 가져와서 뿌려주자.
        // ==== HR사원정보 내용에 해당하는 행 및 셀 생성하기 ==== //
        Row bodyRow = null;
        Cell bodyCell = null;
       

        for(int i=0; i<empList.size(); i++) {		// 받아온 사원정보(empList) 만큼 행을 만들어줘야 하므로 for 문을 돌린다.
        	
        	Map<String, String> empMap = empList.get(i);		// 현재 empList 는 Map 이기 때문에 그대로 가져온다.
        	
        	// Mapper 에 property 로 key 값을 설정해두었다. 
        	
        	// 행 생성하기
        	bodyRow = sheet.createRow(i + (rowLocation+1));	// 이미 rowLocation 는 위에서 0 을 거쳐서 전위연산(++) 거쳐서 1인 상태인데, 2번행에서 출발해야 하므로 +1을 또 해준다.
        	
        	// 데이터 부서번호를 표시
        	bodyCell = bodyRow.createCell(0);
        	bodyCell.setCellValue(empMap.get("department_id"));
        	
        	// 데이터 부서명을 표시 
        	bodyCell = bodyRow.createCell(1);
        	bodyCell.setCellValue(empMap.get("department_name"));
        	
        	// 데이터 사원번호를 표시 
        	bodyCell = bodyRow.createCell(2);
        	bodyCell.setCellValue(empMap.get("employee_id"));
 
        	// 데이터 사원명을 표시 
        	bodyCell = bodyRow.createCell(3);
        	bodyCell.setCellValue(empMap.get("fullname"));
 
        	// 데이터 입사일자를 표시 
        	bodyCell = bodyRow.createCell(4);
        	bodyCell.setCellValue(empMap.get("hire_date"));
        	 
        	// 사원번호는 숫자로 되어 있지만 문자이고, 월급은 숫자로 되어있다 (셀 왼쪽 위의 초록색 표시 있을 때 문자임을 알 수 있다.)
        	// 즉 월급/나이를 int(숫자) 로 다시 바꿔주자. (Mapper 에서 javaType 이 String 으로 되어있으므로 Controller 에서 다시 int 로 바꿔주는 것이다.)       	 
        	// 데이터 월급을 표시 
        	bodyCell = bodyRow.createCell(5);
        	bodyCell.setCellValue(Integer.parseInt(empMap.get("monthsal")) ); // 즉 월급/나이를 int(숫자) 로 다시 바꿔주자.
        	bodyCell.setCellStyle(moneyStyle); // 천단위 쉼표, 금액
        	
        	// 데이터 성별 표시 
        	bodyCell = bodyRow.createCell(6);
        	bodyCell.setCellValue(empMap.get("gender"));
 
        	// 데이터 나이를 표시 
        	bodyCell = bodyRow.createCell(7);
        	bodyCell.setCellValue(Integer.parseInt(empMap.get("age")) );	// 즉 월급/나이를 int(숫자) 로 다시 바꿔주자.
      	
        	
        }// end of for---------------------------------------
        
        // 저장소에 담을때 원래는  addObject 였지만 model 은 .addAttribute 로 한다. (엑셀 파일(workBook)을 담도록 하자.), sheet 를 현재 1개만 만들어옴
        // 현재 파일명이 한글로 되어있으므로 깨짐 방지를 위해 (영어는 괜찮음) 아래와 같이 파일명이 한국어임을 알려준다.       
        model.addAttribute("locale", Locale.KOREA);		// 파일명이 한국어임을 알려준다.
        model.addAttribute("workbook", workbook);
        model.addAttribute("workbookName", "HR사원정보");		// 다운 받을 때 다운받아와야 할 파일명을 알아야 한다. (sheet 이름이 아니므로 주의하자.)
       
		return "excelDownloadView";		// return 엔 내 마음대로 쓸 수 없고, servlet-context.xml 파일 참고. (접두어/접미어가 아니다. 엑셀파일을 다운로드 해줘야하는구나 라고 보는 것이다.)
		//  "excelDownloadView" 은 
	    //  /webapp/WEB-INF/spring/appServlet/servlet-context.xml 파일에서
	    //  뷰리졸버 0 순위로 기술된 bean 의 id 값이다.    (#19 번을 참고한다.)
		//  0순위가 excelDownloadView, 다음이 1순위인 tiles , 그다음 2순위가 접두어/접미어 이다.
		// com.spring.excel.ExcelDownloadView 경로로 이동해서 보도록 한다.
	}
	
	// === #177. 차트(그래프)를 보여주는 예제(View단) === //
	// BoardAOP 에 설정해두었다. (주업무 - 관리자만 보도록 하겠다. (gradelevel > 10)
	@RequestMapping(value = "/emp/chart.action")	
	public String empmanager_chart(HttpServletRequest request) {
		// view 단만 보여주므로 파라미터 필요 없다.
		return "emp/chart.tiles2";	
	}

	// === #178. 차트 그리기(Ajax) 부서명별 인원수 및 퍼센티지 가져오기 === //
	@ResponseBody		// json, Ajax 를 사용하므로 추가해준다.
	@RequestMapping(value = "/chart/employeeCntByDeptname.action", produces="text/plain;charset=UTF-8")	
	public String employeeCntByDeptname() {
		
		// 복수개를 받아오므로 List 타입, VO 를 만들지 않았으므로 Map 에 담는다.
		List<Map<String, String>> deptnamePercentageList = service.employeeCntByDeptname();
		
		// DB 에서 select한 복수개들의 List 들을 가져온 것에 성공한 후 gson 을 사용한다. (원래는 json을 사용했다. json 사용해도 상관 없음)
		//  import 할 때  com.google.gson.Gson;
		JsonArray jsonArr = new JsonArray();
		
		for(Map<String, String> map : deptnamePercentageList) {			
			JsonObject jsonObj = new JsonObject();	// import 할 때  com.google.gson.Gson;
			jsonObj.addProperty("department_name", map.get("department_name"));
			jsonObj.addProperty("cnt", map.get("cnt"));
			jsonObj.addProperty("percentage", map.get("percentage"));
			
			jsonArr.add(jsonObj);		// .put 이 아니라 .add 를 사용한다. (json 은 .put이다.)
		}// end of for-----------------------------------------
		
		// 또는 return gson.toJson(jsonArr);		// jsonArr 를 Json 으로 바꾼다.
		 return new Gson().toJson(jsonArr);		// jsonArr 를 Json 으로 바꾼다.
		 
		 /* 아래와 같이 url에 주소 입력 시 결과값이 출력됨. (cnt 가 큰 순서대로 출력되게끔 한다.)
			[{"department_name":"Shipping","cnt":"45","percentage":"42.06"},{"department_name":"Sales","cnt":"34","percentage":"31.78"},{"department_name":"Finance","cnt":"6","percentage":"5.61"},{"department_name":"Purchasing","cnt":"6","percentage":"5.61"}
			,{"department_name":"IT","cnt":"5","percentage":"4.67"},{"department_name":"Executive","cnt":"3","percentage":"2.8"},{"department_name":"Accounting","cnt":"2","percentage":"1.87"},{"department_name":"Marketing","cnt":"2","percentage":"1.87"}
			,{"department_name":"Administration","cnt":"1","percentage":".93"},{"department_name":"Human Resources","cnt":"1","percentage":".93"},{"department_name":"Public Relations","cnt":"1","percentage":".93"},{"department_name":"부서없음","cnt":"1","percentage":".93"}]
		 */
	}	

	
	// === #179. 차트 그리기(Ajax) 성별 인원수 및 퍼센티지 가져오기 === //
	@ResponseBody		// json, Ajax 를 사용하므로 추가해준다.
	@RequestMapping(value = "/chart/employeeCntByGender.action", produces="text/plain;charset=UTF-8")	
	public String employeeCntByGender() {
		
		// 복수개를 받아오므로 List 타입, VO 를 만들지 않았으므로 Map 에 담는다.
		List<Map<String, String>> GenderPercentageList = service.employeeCntByGender();
		
		// DB 에서 select한 복수개들의 List 들을 가져온 것에 성공한 후 gson 을 사용한다. (원래는 json을 사용했다. json 사용해도 상관 없음)
		//  import 할 때  com.google.gson.Gson; (JSONObject 와 다름. 대소문자 구분!!)
		JsonArray jsonArr = new JsonArray();
		
		for(Map<String, String> map : GenderPercentageList) {			
			JsonObject jsonObj = new JsonObject();	// import 할 때  com.google.gson.Gson;
			jsonObj.addProperty("gender", map.get("gender"));
			jsonObj.addProperty("cnt", map.get("cnt"));
			jsonObj.addProperty("percentage", map.get("percentage"));
			
			jsonArr.add(jsonObj);		// .put 이 아니라 .add 를 사용한다. (json 은 .put이다.)
		}// end of for-----------------------------------------
		
		// 또는 return gson.toJson(jsonArr);		// jsonArr 를 Json 으로 바꾼다.
		 return new Gson().toJson(jsonArr);		// jsonArr 를 Json 으로 바꾼다.
		 
		 /* 아래와 같이 url에 주소 입력 시 결과값이 출력됨. 
			[{"gender":"여","cnt":"51","percentage":"47.66"},{"gender":"남","cnt":"56","percentage":"52.34"}]
		 */
	}		

	
	// === #180. 차트 그리기(Ajax) 특정 부서명에 근무하는 직원들의 성별 인원수 및 퍼센티지 가져오기 === //
	@ResponseBody		// json, Ajax 를 사용하므로 추가해준다.
	@RequestMapping(value = "/chart/genderCntSpecialDeptname.action", produces="text/plain;charset=UTF-8")	
	public String genderCntSpecialDeptname(HttpServletRequest request) {
		
		String depthname = request.getParameter("deptname");	// 내가 알고자 하는 부서명을 ajax 에서 받아온다.
		Map<String, String> paraMap = new HashMap<>();
		paraMap.put("depthname", depthname);
		
		// sql문 실행시 행이 남/여로 복수개로 나오게 된다. --> 그러므로 return 타입이 List 이다.
		// VO 를 만들지 않았으므로 Map 을 만들도록 한다.
		List<Map<String,String>> genderPercentageList = service.genderCntSpecialDeptname(paraMap);

		// DB 에서 select한 복수개들의 List 들을 가져온 것에 성공한 후 gson 을 사용한다. (원래는 json을 사용했다. json 사용해도 상관 없음)
		//  import 할 때  com.google.gson.Gson; Gson 을 사용한다.
		JsonArray jsonArr = new JsonArray();
		
		for(Map<String, String> map : genderPercentageList) {			
			JsonObject jsonObj = new JsonObject();	// import 할 때  com.google.gson.Gson;
			jsonObj.addProperty("gender", map.get("gender"));
			jsonObj.addProperty("cnt", map.get("cnt"));
			jsonObj.addProperty("percentage", map.get("percentage"));
			
			jsonArr.add(jsonObj);		// .put 이 아니라 .add 를 사용한다. (json 은 .put이다.)
		}// end of for-----------------------------------------
		 
		 /* 아래와 같이 url에 주소 입력 시 결과값이 출력됨. (cnt 가 큰 순서대로 출력되게끔 한다.)
		  * http://localhost:9090/board/chart/genderCntSpecialDeptname.action?deptname=Shipping 입력했을 때 (Shipping 부서)
			[{"gender":"남","cnt":"23","percentage":"21.5"}
			,{"gender":"여","cnt":"22","percentage":"20.56"}]
		 */		
		
		// 또는 return gson.toJson(jsonArr);		// jsonArr 를 Json 으로 바꾼다.
		 return new Gson().toJson(jsonArr);		// jsonArr 를 Json 으로 바꾼다.
	}
	
	
	// === #200. 기상청 공공데이터(오픈데이터)를 가져와서 날씨정보 보여주기 === //
	@RequestMapping(value = "/opendata/weatherXML.action", method = {RequestMethod.GET})	// default 가 GET.
	public String weatherXML() {
		
		return "opendata/weatherXML";		// 접두어/접미어 기억하기
		// /Board/src/main/webapp/WEB-INF/views/opendata/weatherXML.jsp 파일을 생성한다.
	}
	
	//////////////////////////////////////////////////////
	@ResponseBody
	@RequestMapping(value="/opendata/weatherXMLtoJSON.action", method= {RequestMethod.POST}, produces="text/plain;charset=UTF-8") 
	public String weatherXMLtoJSON(HttpServletRequest request) { 
	
		String str_jsonObjArr = request.getParameter("str_jsonObjArr");
		/*  확인용
		//   System.out.println(str_jsonObjArr);
		//  [{"locationName":"속초","ta":"2.4"},{"locationName":"북춘천","ta":"-2.3"},{"locationName":"철원","ta":"-2.0"},{"locationName":"동두천","ta":"-0.7"},{"locationName":"파주","ta":"-1.2"},{"locationName":"대관령","ta":"-3.0"},{"locationName":"춘천","ta":"-1.6"},{"locationName":"백령도","ta":"1.1"},{"locationName":"북강릉","ta":"3.4"},{"locationName":"강릉","ta":"4.3"},{"locationName":"동해","ta":"4.1"},{"locationName":"서울","ta":"-0.3"},{"locationName":"인천","ta":"-0.2"},{"locationName":"원주","ta":"-2.2"},{"locationName":"울릉도","ta":"4.2"},{"locationName":"수원","ta":"1.4"},{"locationName":"영월","ta":"-4.5"},{"locationName":"충주","ta":"-3.0"},{"locationName":"서산","ta":"2.5"},{"locationName":"울진","ta":"3.9"},{"locationName":"청주","ta":"-0.7"},{"locationName":"대전","ta":"2.9"},{"locationName":"추풍령","ta":"3.2"},{"locationName":"안동","ta":"-2.3"},{"locationName":"상주","ta":"1.5"},{"locationName":"포항","ta":"4.7"},{"locationName":"군산","ta":"2.6"},{"locationName":"대구","ta":"1.6"},{"locationName":"전주","ta":"5.7"},{"locationName":"울산","ta":"4.0"},{"locationName":"창원","ta":"4.4"},{"locationName":"광주","ta":"2.8"},{"locationName":"부산","ta":"4.3"},{"locationName":"통영","ta":"5.7"},{"locationName":"목포","ta":"4.5"},{"locationName":"여수","ta":"5.6"},{"locationName":"흑산도","ta":"7.8"},{"locationName":"완도","ta":"7.3"},{"locationName":"고창","ta":"3.5"},{"locationName":"순천","ta":"5.3"},{"locationName":"홍성","ta":"1.7"},{"locationName":"제주","ta":"8.5"},{"locationName":"고산","ta":"9.1"},{"locationName":"성산","ta":"7.5"},{"locationName":"서귀포","ta":"8.8"},{"locationName":"진주","ta":"3.5"},{"locationName":"강화","ta":"-0.9"},{"locationName":"양평","ta":"-1.3"},{"locationName":"이천","ta":"-2.0"},{"locationName":"인제","ta":"-0.5"},{"locationName":"홍천","ta":"-2.6"},{"locationName":"태백","ta":"-1.5"},{"locationName":"정선군","ta":"-1.9"},{"locationName":"제천","ta":"-4.4"},{"locationName":"보은","ta":"-1.2"},{"locationName":"천안","ta":"-1.0"},{"locationName":"보령","ta":"3.6"},{"locationName":"부여","ta":"0.6"},{"locationName":"금산","ta":"3.7"},{"locationName":"세종","ta":"-0.8"},{"locationName":"부안","ta":"5.8"},{"locationName":"임실","ta":"1.6"},{"locationName":"정읍","ta":"5.8"},{"locationName":"남원","ta":"0.1"},{"locationName":"장수","ta":"1.4"},{"locationName":"고창군","ta":"4.3"},{"locationName":"영광군","ta":"4.2"},{"locationName":"김해시","ta":"4.1"},{"locationName":"순창군","ta":"1.0"},{"locationName":"북창원","ta":"5.9"},{"locationName":"양산시","ta":"3.9"},{"locationName":"보성군","ta":"5.1"},{"locationName":"강진군","ta":"4.4"},{"locationName":"장흥","ta":"4.9"},{"locationName":"해남","ta":"6.2"},{"locationName":"고흥","ta":"5.4"},{"locationName":"의령군","ta":"5.7"},{"locationName":"함양군","ta":"4.9"},{"locationName":"광양시","ta":"5.4"},{"locationName":"진도군","ta":"7.0"},{"locationName":"봉화","ta":"-3.3"},{"locationName":"영주","ta":"-4.3"},{"locationName":"문경","ta":"-3.1"},{"locationName":"청송군","ta":"0.5"},{"locationName":"영덕","ta":"4.7"},{"locationName":"의성","ta":"-0.6"},{"locationName":"구미","ta":"2.3"},{"locationName":"영천","ta":"3.4"},{"locationName":"경주시","ta":"4.5"},{"locationName":"거창","ta":"0.8"},{"locationName":"합천","ta":"0.7"},{"locationName":"밀양","ta":"1.1"},{"locationName":"산청","ta":"4.2"},{"locationName":"거제","ta":"5.8"},{"locationName":"남해","ta":"6.2"}]  
		*/
		//   return str_jsonObjArr;  -- 지역 96개 모두 차트에 그리기에는 너무 많으므로 아래처럼 작업을 하여 지역을  21개(String[] locationArr 임)로 줄여서 나타내기로 하겠다.
		
		str_jsonObjArr = str_jsonObjArr.substring(1, str_jsonObjArr.length()-1);
		
		String[] arr_str_jsonObjArr = str_jsonObjArr.split("\\},");
		
		for(int i=0; i<arr_str_jsonObjArr.length; i++) {
			arr_str_jsonObjArr[i] += "}";	//,"ta":"15.7"} 부분에서 '}' 부분 += 함.
		}
		
		/*  확인용
		for(String jsonObj : arr_str_jsonObjArr) {
			System.out.println(jsonObj);
		}
		
		*/ // jsonObj 객체 하나하나가 나온다.
		// {"locationName":"속초","ta":"15.7"}
		// {"locationName":"북춘천","ta":"24.9"}
		// {"locationName":"철원","ta":"23.8"}
		// {"locationName":"동두천","ta":"26.3"}
		// {"locationName":"파주","ta":"25.5"}
		// {"locationName":"대관령","ta":"10.8"}
		// {"locationName":"춘천","ta":"26.7"}
		// {"locationName":"백령도","ta":"13.8"}
		// ........ 등등  
		// {"locationName":"밀양","ta":"24.7"}
		// {"locationName":"산청","ta":"24.2"}
		// {"locationName":"거제","ta":"21.0"}
		// {"locationName":"남해","ta":"22.7"}
		
		
		String[] locationArr = {"서울","인천","수원","춘천","강릉","청주","홍성","대전","안동","포항","대구","전주","울산","부산","창원","여수","광주","목포","제주","울릉도","백령도"};
		String result = "[";
		
		for(String jsonObj : arr_str_jsonObjArr) {
		
		for(int i=0; i<locationArr.length; i++) {
			//  if( jsonObj.indexOf(locationArr[i]) >= 0 ) { // 북춘천,춘천,북강릉,강릉,북창원,창원이 있으므로  if(jsonObj.indexOf(locationArr[i]) >= 0) { 을 사용하지 않음 
				if( jsonObj.substring(jsonObj.indexOf(":")+2, jsonObj.indexOf(",")-1).equals(locationArr[i]) ) { 
					result += jsonObj+",";  // [{"locationName":"춘천","ta":"26.7"},{"locationName":"백령도","ta":"13.8"}, ..... {"locationName":"제주","ta":"18.9"}, 
					break;
				}
			}
		}// end of for------------------------------
		
			result = result.substring(0, result.length()-1);  // [{"locationName":"춘천","ta":"26.7"},{"locationName":"백령도","ta":"13.8"}, ..... {"locationName":"제주","ta":"18.9"}
			result = result + "]";                            // [{"locationName":"춘천","ta":"26.7"},{"locationName":"백령도","ta":"13.8"}, ..... {"locationName":"제주","ta":"18.9"}]
			
			/*  확인용
			System.out.println(result);
			// [{"locationName":"춘천","ta":"26.7"},{"locationName":"백령도","ta":"13.8"},{"locationName":"강릉","ta":"18.4"},{"locationName":"서울","ta":"27.7"},{"locationName":"인천","ta":"23.8"},{"locationName":"울릉도","ta":"19.2"},{"locationName":"수원","ta":"26.5"},{"locationName":"청주","ta":"26.8"},{"locationName":"대전","ta":"26.4"},{"locationName":"안동","ta":"24.3"},{"locationName":"포항","ta":"19.4"},{"locationName":"대구","ta":"22.7"},{"locationName":"전주","ta":"26.3"},{"locationName":"울산","ta":"20.7"},{"locationName":"창원","ta":"21.9"},{"locationName":"광주","ta":"25.6"},{"locationName":"부산","ta":"22.0"},{"locationName":"목포","ta":"23.2"},{"locationName":"여수","ta":"23.0"},{"locationName":"홍성","ta":"25.0"},{"locationName":"제주","ta":"18.9"}]
			
			*/
		return result;
	}

	 /*
	    @ExceptionHandler 에 대해서..... (GET방식일때 유저가 URL 에 장난치는 것 방지하기)
	    ==> 어떤 컨트롤러내에서 발생하는 익셉션이 있을시 익셉션 처리를 해주려고 한다면
	        @ExceptionHandler 어노테이션을 적용한 메소드를 구현해주면 된다
	         
	       컨트롤러내에서 @ExceptionHandler 어노테이션을 적용한 메소드가 존재하면, 
	       스프링은 익셉션 발생시 @ExceptionHandler 어노테이션을 적용한 메소드가 처리해준다.
	       따라서, 컨트롤러에 발생한 익셉션을 직접 처리하고 싶다면 @ExceptionHandler 어노테이션을 적용한 메소드를 구현해주면 된다.
	 */
	 @ExceptionHandler(java.lang.Throwable.class)	// exception 이나 error 가 발생되면 작동함. 에러 처리시 꼭 @ExceptionHandler 를 넣도록 하자!!
	 public void handleThrowable(Throwable e, HttpServletRequest request, HttpServletResponse response) {
	    
	    e.printStackTrace(); // 콘솔에 에러메시지 나타내기
	    
	    try {
	       // *** 웹브라우저에 출력하기 시작 *** //
	       
	       // HttpServletResponse response 객체는 넘어온 데이터를 조작해서 결과물을 나타내고자 할때 쓰인다. 
	       response.setContentType("text/html; charset=UTF-8");
	       
	       PrintWriter out = response.getWriter();   // out 은 웹브라우저에 기술하는 대상체라고 생각하자.
	       
	       out.println("<html>");
	       out.println("<head><title>오류메시지 출력하기</title></head>");
	       out.println("<body>");
	       out.println("<h1>오류발생</h1>");
	       
	  //   out.printf("<div><span style='font-weight: bold;'>오류메시지</span><br><span style='color: red;'>%s</span></div>", e.getMessage());
	       
	       String ctxPath = request.getContextPath();
	       
	       out.println("<div><img src='"+ctxPath+"/resources/images/error.gif'/></div>");
	       out.printf("<div style='margin: 20px; color: blue; font-weight: bold; font-size: 26pt;'>%s</div>", "장난금지");
	       out.println("<a href='"+ctxPath+"/index.action'>홈페이지로 가기</a>");
	       out.println("</body>");
	       out.println("</html>");
	       
	       // *** 웹브라우저에 출력하기 끝 *** //
	    } catch (IOException e1) {
	       e1.printStackTrace();
	    }
	    
	 }
	
	
	//////////////////////////////////////////////////////////////////////////////////////////

	// === 로그인 또는 로그아웃을 했을 때 현재 보이던 그 페이지로 그대로 돌아가기 위한 메소드 생성 === //
	public void getCurrentURL(HttpServletRequest request) {
		HttpSession session = request.getSession();
		session.setAttribute("goBackURL", MyUtil.getCurrnetURL(request));	// myUtil 에서 만들어 놓은 getCurrnetURL 메소드를 가져온다.
		// myUtil 의 return 값인 currentURL이 넘어오게 된다.	
	}

	//////////////////////////////////////////////////////////////////////////////////////////	
}
