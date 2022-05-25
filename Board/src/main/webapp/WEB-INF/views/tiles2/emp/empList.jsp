<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<style type="text/css">

table#emptbl {
      width: 100%;
   }
   
   table#emptbl th, table#emptbl td {
      border: solid 1px gray;
      border-collapse: collapse;
   }
   
   table#emptbl th {
      text-align: center;
      background-color: #ccc;
   }
   
   form {
      margin: 0 0 30px 0;
   }

</style>

<script type="text/javascript">

	$(document).ready(function() {
		
		// 검색하기 버튼 클릭 시 함수 실행한다.
		$("button#btnSearch").click(function() {
			
			// 배열만든 후 배열을 JOIN 한다. ( , , ) 형태로 나오게끔 한다.
			const arrDeptId = new Array();	<%-- 또는  = []; 모두 사용 가능 --%>
			
			<%-- 라디오, 체크박스는 무조건 name 이 같아야 한다. checkbox 는 복수개로 배열이다.(list) --%>
			$("input:checkbox[name=deptId]").each(function(index, item){
				// 체크박스의 체크 유무를 검사해야 한다. (item 이 각 체크박스를 가리킨다.) / 사용자가 체크박스 어떤것에 체크했는지 보자.
				const bool = $(item).prop("checked");		// 체크박스의 유무 검사
			//	또는
			// 	const bool = $(item).is(":checked");		
				
				if(bool == true) {	// 어차피 true 일 때만 움직이므로 == true 는 생략해도 된다.
					// 체크박스에 체크가 되었으면 arrDeptId에 쌓자.
					arrDeptId.push($(item).val());		// 체크박스에 담겨져있는 value 값을 담자.
				}
				
			});
			
			// 부서번호를 알아오자.
			const str_DeptId = arrDeptId.join();		// 배열.join();, 원래는 합칠때 ("-")를 사용했음. 쓰지 않으면 ','가 default 이다.
		//	console.log("확인용 : str_DeptId => " + str_DeptId);
			// 확인용 : str_DeptId => 				(체크박스에서 하나도 선택하지 않았을 때)
			// 확인용 : str_DeptId => 20,40
			// 확인용 : str_DeptId => -9999,20,40
		
			const frm = document.searchFrm;
			frm.str_DeptId.value = str_DeptId;			// 사용자가 선택한 부서번호(input 태그) 및 성별(select 태그)이 무엇인지 다 보내야 한다. (in 절(오라클)을 쓰기 위함이다.)
														// form 내에 input 태그 만들어서 hidden 타입에 심어서 보내주도록 하자. name 이 str_DeptId / value 값 참고
			
														
			frm.method = "POST";							// POST 방식으로 해도 무방하다. (action 단에서 GET/POST 방식으로 둘다 받아주는지 아닌지 확인 해야함.)
			frm.action = "empList.action";				// 상대경로 ('/' 는 빼도록 한다.)
			frm.submit();
		
		});
		
		// 체크박스에 부서번호 및 성별을 체크 후 제출한 다음에, 사용자가 선택했던 값들 그대로 유지시키기 (문자열로 넘어온 것, 배열이 X)
		// === 체크박스 유지시키기 시작 === //
		const str_DeptId = "${requestScope.str_DeptId}";		// JS 이기 때문에 "" 를 써준다.
	//	console.log(str_DeptId);	// "-9999,20,40,70"
		
		if(str_DeptId != "") {
			// 체크박스에 체크 했다면 ("" 가 아니라면) 그 체크한 값을 그대로 유지해준다.
			// 배열로 바꿔서 다시 값을 하나하나 넣어주도록 한다.
			const arr_DeptId = str_DeptId.split(",");			// JS 는 \\ 가 없다. java에서만 쓰이던 것이다.
			// [-9999,20,40,70]
			$("input:checkbox[name=deptId]").each(function(index,item) {
				// 부서없음 부터 110번 부서번호까지 하나하나 반복문 돌리면서 사용자가 선택한 값이랑 비교한다.
				// input 체크박스의 값들과 사용자가 선택한 부서번호 하나하나의 값들이 같을 때, 체크박스에 체크를 유지하도록 한다.
				// 모든 체크박스에서 value 값이 무엇인지 찾자. value 값과 사용자가 선택한 값이 같으면 체크박스에 값을 유지해준다.
				for(let i=0; i<arr_DeptId.length; i++) {
					// 체크한 박스중에서 [] 안에 들어있는 값이 있나 반복문 돌려서 체크해보는 것임.
					if($(item).val() == arr_DeptId[i]) {		// 같을 경우 체크박스를 유지하도록 한다.
						$(item).prop("checked",true);
						break;			// 일반 for 문, 반복 빠져나온다. (return; 이랑 구분할것)
					}				
				}// end of for---------------------------------
			});
		}
		
		// === 체크박스 유지시키기 끝 === //
		
		// === 성별 유지시키기 시작 === //
		const gender = "${requestScope.gender}";
	//	console.log(gender);
		if(gender != "") {
			$("select#gender").val(gender);
		}
		// === 성별 유지시키기 끝 === //
		

		// === Excel 파일로 다운받기 시작 ( Excel 파일로 저장 버튼을 클릭했을 때 ) === //		
		$("button#btnExcel").click(function() {
			// 배열만든 후 배열을 JOIN 한다. ( , , ) 형태로 나오게끔 한다.
			const arrDeptId = new Array();	<%-- 또는  = []; 모두 사용 가능 --%>
			
			<%-- 라디오, 체크박스는 무조건 name 이 같아야 한다. checkbox 는 복수개로 배열이다.(list) --%>
			$("input:checkbox[name=deptId]").each(function(index, item){
				// 체크박스의 체크 유무를 검사해야 한다. (item 이 각 체크박스를 가리킨다.) / 사용자가 체크박스 어떤것에 체크했는지 보자.
				const bool = $(item).prop("checked");		// 체크박스의 유무 검사
			//	또는
			// 	const bool = $(item).is(":checked");		
				
				if(bool == true) {	// 어차피 true 일 때만 움직이므로 == true 는 생략해도 된다.
					// 체크박스에 체크가 되었으면 arrDeptId에 쌓자.
					arrDeptId.push($(item).val());		// 체크박스에 담겨져있는 value 값을 담자.
				}
				
			});
			
			// 부서번호를 알아오자.
			const str_DeptId = arrDeptId.join();		// 배열.join();, 원래는 합칠때 ("-")를 사용했음. 쓰지 않으면 ','가 default 이다.
		//	console.log("확인용 : str_DeptId => " + str_DeptId);
			// 확인용 : str_DeptId => 				(체크박스에서 하나도 선택하지 않았을 때)
			// 확인용 : str_DeptId => 20,40
			// 확인용 : str_DeptId => -9999,20,40
		
			const frm = document.searchFrm;
			frm.str_DeptId.value = str_DeptId;			// 사용자가 선택한 부서번호(input 태그) 및 성별(select 태그)이 무엇인지 다 보내야 한다. (in 절(오라클)을 쓰기 위함이다.)
														// form 내에 input 태그 만들어서 hidden 타입에 심어서 보내주도록 하자. name 이 str_DeptId / value 값 참고
																	
			frm.method = "POST";		// 파일이기 때문에 반드시 POST 방식으로 한다.
			frm.action = "<%= request.getContextPath()%>/excel/downloadExcelFile.action";				// 상대경로 ('/' 는 빼도록 한다.), 엑셀파일로 다운받게끔 해주는 url 로 이동한다. (DB 에서 모두 가져온 결과물을 EXCEL 파일로 바꾼 후 다운로드 받는다.)
			frm.submit();
		});
		
		// === Excel 파일로 다운받기 끝 ( Excel 파일로 저장 버튼을 클릭했을 때 ) === //
		
	});// end of  $(document).ready(function() {})----------------

</script>

<div style="display: flex; margin-bottom: 50px;">   
<div style="width: 80%; min-height: 1100px; margin:auto; ">

	<h2 style="margin: 50px 0;">HR 사원정보 조회하기</h2>

	<form name="searchFrm">
		<c:if test="${not empty requestScope.deptIdList}">	<%-- 또는 ${requestScope.deptIdList != null}, 그러나 list 이고 size 가 0 이면 null 이 아니기 때문에 둘다 쓰려면 not empty 를 쓰는 것이 좋다. --%>
			<span style="display: inline-block; width: 150px; font-weight: bold;">부서번호선택</span>
			<c:forEach var="deptId" items="${requestScope.deptIdList}" varStatus="status">
				<label for="${status.index}">
					<c:if test="${deptId == -9999}">부서없음</c:if>
					<c:if test="${deptId != -9999}">${deptId}</c:if>
				</label>	
				<input type="checkbox" name="deptId" id="${status.index}" value="${deptId}" />&nbsp;&nbsp;		<%-- checkbox 안에 value 값이 다 들어가 있는 것이다. --%>
				<%-- id 는 고유해야 하므로 status.index 사용한다. --%>
			</c:forEach>
		</c:if>
		
		<input type="hidden" name="str_DeptId" />
		
		<select name="gender" id="gender" style="height: 30px; width: 120px; margin: 10px 30px 0 0;">
	         <option value="">성별선택</option>
	         <option>남</option>
	         <option>여</option>
      	</select>
		<button type="button" class="btn btn-secondary btn-sm" id="btnSearch">검색하기</button>
      	&nbsp;&nbsp;
      	<button type="button" class="btn btn-secondary btn-sm" id="btnExcel">Excel파일로저장</button>		
	</form>
	
	<table id="emptbl">
	      <thead>
	         <tr>
	            <th>부서번호</th>
	            <th>부서명</th>
	            <th>사원번호</th>
	            <th>사원명</th>
	            <th>입사일자</th>
	            <th>월급</th>
	            <th>성별</th>
	            <th>나이</th>
	         </tr>
	      </thead>
	      
	      <tbody>
	      	<c:if test="${not empty requestScope.empList}">
	      		<c:forEach var="map" items="${requestScope.empList}">
		         <tr>
		         	<%-- hr.xml(mapper) 에서 property에 key 값을 설정해놨다. --%>
		         	<%-- text-align의 default 는 left 이다. --%>
		            <td style="text-align: center;">${map.department_id}</td>
		            <td>${map.department_name}</td>
		            <td style="text-align: center;">${map.employee_id}</td>
		            <td>${map.fullname}</td>
		            <td style="text-align: center;">${map.hire_date}</td>
		            <td style="text-align: right;"><fmt:formatNumber value="${map.monthsal}" pattern="#,###" /></td>
		            <td style="text-align: center;">${map.gender}</td>
		            <td style="text-align: center;">${map.age}</td>
		         </tr>	      			
	      		</c:forEach>
	      	</c:if>
	      </tbody>
	</table>	
</div>
</div>