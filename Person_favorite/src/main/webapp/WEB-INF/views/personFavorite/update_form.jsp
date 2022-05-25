<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%
    String ctxPath = request.getContextPath();
    //     /favorite
%>    
    
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>개인성향 정보 수정 하기</title>

<style type="text/css">
	div#container {
     /* border: solid 1px red; */
    	width: 80%;
    	margin: 50px auto;
    }
    
    fieldset {
    	width: 50%;
    }
    
	ul {list-style: none;}
	
    li {line-height: 200%;}
</style>

<script type="text/javascript" src="<%= ctxPath%>/resources/js/jquery-3.3.1.min.js"></script>

<script type="text/javascript">

	$(document).ready(function(){
		
		// 1. 성명 입력해주기
		$("input#name").val("${requestScope.vo.name}");
		
		// 2. 학력 입력해주기
		$("select#school").val("${requestScope.vo.school}");
				
		// 3. 색상 입력해주기
		var userChoiceColor = "${requestScope.vo.color}";
	//	console.log("확인용 userChoiceColor => " + userChoiceColor);
	//  확인용 userChoiceColor => red
	<%--
		$("input:radio[name=color]").each(function(index, item){
	 //	console.log("확인용 value => " + $(item).prop("value") );
		/*
		        확인용 value => red
		        확인용 value => blue
		        확인용 value => green
		        확인용 value => yellow
		*/
			if( $(item).prop("value") == userChoiceColor ) {
				$(item).prop("checked", true);
				return false; // for 문의 break; 와 같은 뜻이다. 
			}
		}); 
	  --%>	
	  // 또는
		$("input:radio[id="+userChoiceColor+"]").prop("checked", true);
		
		// 4. 음식 입력해주기
		var userChoiceFood = "${requestScope.vo.food}";
	//	console.log("확인용 userChoiceFood => " + userChoiceFood );
		// 확인용 userChoiceFood => "짜짱면,탕수육,팔보채"
		// 확인용 userChoiceFood => "없음"
		
		if(userChoiceFood != "없음"){
			var arrFood = userChoiceFood.split(",");
			//  arrFood ==> ["짜장면","탕수육","팔보채"];
			
			arrFood.forEach(function(food){
			 <%--	
				$("input:checkbox[name=food]").each(function(index, item){
					if( food == $(item).prop("value") ) {
						$(item).prop("checked", true);
						return false;
					} 
				});// end of $("input:checkbox[name=food]").each()--------- 
			  --%>	
			  // 또는
			   $("input:checkbox[value="+food+"]").prop("checked", true); 	
			});// end of arrFood.forEach()------------
		}
		
		
		// === 유효성 검사하기 시작 === //
		$("form[name=updateFrm]").submit(function(){
			
			var nameLength = $("input#name").val().trim().length;
			
			if(nameLength == 0) {
				alert("성명을 입력하세요!!");
				return false;  // submit 을 하지 않고 종료한다.
			}
			
			var schoolVal = $("select#school").val();
			
			if(schoolVal == "") {
				alert("학력을 선택하세요!!");
				return false;  // submit 을 하지 않고 종료한다.
			}
			
			var colorLength = $("input:radio[name=color]:checked").length;
			
			if(colorLength == 0) {
				alert("좋아하는 색상을 선택하세요!!");
				return false;  // submit 을 하지 않고 종료한다.
			}
			
		/*	
			var foodLength = $("input:checkbox[name=food]:checked").length;
			
			if(foodLength == 0) {
				alert("좋아하는 음식을 선택하세요!!");
				return false;  // submit 을 하지 않고 종료한다.
			}
		*/	
			
		});
		// === 유효성 검사하기 끝 === //
		
	}); // end of $(document).ready(function(){})-----------

</script>

</head>
<body>

  <div id="container">
	  <form name="updateFrm" action="<%= ctxPath%>/personFavoriteUpdateEnd.sist" method="post">
		<fieldset>
			<legend>${requestScope.vo.name}님 성향 정보 수정 하기</legend>
			<ul>
				<li>
					<input type="hidden" name="seq" value="${requestScope.vo.seq}" readonly /> 
					<label for="name">성명</label>
					<input type="text" name="name" id="name" placeholder="성명입력"/> 
				</li>
				<li>
					<label for="school">학력</label>
					<select name="school" id="school">
						<option value="">선택하세요</option>
						<option value="고졸">고졸</option>
						<option value="초대졸">초대졸</option>
						<option value="대졸">대졸</option>
						<option value="대학원졸">대학원졸</option>
					</select>
				</li>
				<li>
					<label for="">좋아하는 색상</label>
					<div>
						<label for="red">빨강</label>
						<input type="radio" name="color" id="red" value="red" />
						
						<label for="blue">파랑</label>
						<input type="radio" name="color" id="blue" value="blue" />
						
						<label for="green">초록</label>
						<input type="radio" name="color" id="green" value="green" />
						
						<label for="yellow">노랑</label>
						<input type="radio" name="color" id="yellow" value="yellow" />
					</div>
				</li>
				<li>
					<label for="">좋아하는 음식(다중선택)</label>
					<div>
					    <label for="food1">짜장면</label>
						<input type="checkbox" name="food" id="food1" value="짜장면" />
						&nbsp;
						
						<label for="food2">짬뽕</label>
						<input type="checkbox" name="food" id="food2" value="짬뽕" />
						&nbsp;
						
						<label for="food3">탕수육</label>
						<input type="checkbox" name="food" id="food3" value="탕수육" />
						&nbsp;
						
						<label for="food4">양장피</label>
						<input type="checkbox" name="food" id="food4" value="양장피" />
						&nbsp;
						
						<label for="food5">팔보채</label>
						<input type="checkbox" name="food" id="food5" value="팔보채" />
					</div>
				</li>
				<li>
					<input type="submit" value="수정완료" />
					<input type="button" onclick="javascript:location.href='personFavoriteDetail.sist?seq=${requestScope.vo.seq}'" value="수정취소" /> 
				</li>
			</ul>
		</fieldset>
	  </form>
  </div>

</body>
</html>