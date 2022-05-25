<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%
   String ctxPath = request.getContextPath();
    //     /board 
%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">

<title>Insert title here</title>

<style type="text/css">
	table,th,td {
		border: solid 1px gray;
		border-collapse: collapse;
	}
</style>

<script type="text/javascript" src="<%= ctxPath%>/resources/js/jquery-3.6.0.min.js"></script>
<script type="text/javascript">

	$(document).ready(function(){
		
		func_ajaxselect();	// 문서가 로딩되자마자 DB 에서 select 해온 결과물을 보여준다. (읽어온 결과물들)
		
		$("button#btnOK").click(function(){	// 또는 화살표 함수 사용하기
			
			const no = $("input#no").val(); 
			const name = $("input#name").val();
			
			if(no.trim() == "" || name.trim() == "") {
				alert("번호와 성명 모두 입력하세요!!");
				return;
			}
			// 올바르게 번호, 성명을 입력했으면 보내주자
			$.ajax({
				url:"<%= ctxPath%>/test/ajax_insert.action",
				type:"POST",
				data:{"no":no,
					  "name":name},	// 객체모양으로 보낸다.
				dataType:"json",
				success:function(json) {	// 성공시 입력한 내용을 보낸다. {"n":1}
					if(json.n == 1) {		// DB에 insert 가 성공함.!!
						func_ajaxselect();
						$("input#no").val("");		// 결과물을 보여주고 input 창을 비워준다.
						$("input#name").val("");	// 결과물을 보여주고 input 창을 비워준다.
					}
				},
				error: function(request, status, error){
		               alert("code: "+request.status+"\n"+"message: "+request.responseText+"\n"+"error: "+error);
		        }
					  
			});
			
		});
		
	});// end of $(document).ready(function(){})----------------------
	
	
	// function declaration
	function func_ajaxselect() {
		// select 해온 것을 뿌려주자.
		$.ajax({
			url:"<%= ctxPath%>/test/ajax_select.action",	
			dataType:"json",			// where 절 없이 모두 다 select 로 읽어온다. (그러므로 data 가 필요 없음), type 안썼으므로 기본인 GET 방식
			success:function(json){
				/*
					[{"no":"101","name":"차은우","writeday":"2022-04-19 15:33:15"},{"no":"501","name":"똘똘이","writeday":"2022-04-19 15:32:26"},{"no":"401","name":"엄정화","writeday":"2022-04-19 14:48:22"},{"no":"301","name":"이순신","writeday":"2022-04-19 14:38:08"},{"no":"201","name":"김민정","writeday":"2022-04-19 14:33:38"},{"no":"101","name":"차은우","writeday":"2022-04-19 11:24:44"},{"no":"101","name":"차은우","writeday":"2022-04-19 11:15:29"},{"no":"101","name":"김민정","writeday":"2022-04-19 10:37:33"},{"no":"101","name":"김민정","writeday":"2022-04-19 10:20:39"}]
				*/
				
				let html = "<table>"+
							  "<tr>"+
						         "<th>번호</th>"+
						         "<th>입력번호</th>"+
						         "<th>성명</th>"+
						         "<th>작성일자</th>"+
						      "</tr>";		
						      
				$.each(json, function(index,item) {
					html += "<tr>"+
								"<td>"+(index+1)+"</td>"+
								"<td>"+item.no+"</td>"+
								"<td>"+item.name+"</td>"+
								"<td>"+item.writeday+"</td>"+
							"</tr>";
				});	
				
				html += "</table>";
				
				$("div#view").html(html);	// view 단에 보여준다.
				
			},
			error: function(request, status, error){
	            alert("code: "+request.status+"\n"+"message: "+request.responseText+"\n"+"error: "+error);
			}
		});
		
		
	}// end of function func_ajaxselect()--------------------------------
	
</script>

</head>
<body>
	<h2>Ajax 연습</h2>
	<p>
	      안녕하세요? <br>
	      여기는 /board/test/test_form_3.action 페이지 입니다.
	</p>
	      번호 : <input type="text" id="no" /><br>
	      이름 : <input type="text" id="name" /><br>
	   <button type="button" id="btnOK">확인</button> 
	   <button type="reset"  id="btnCancel">취소</button>
	   <br><br>
	
	<div id="view"></div>	<%-- select 된 정보를 바로 아래에 뿌린다. --%>
	
	   
	   
</body>
</html>