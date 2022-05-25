<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<style type="text/css">
	.highcharts-figure,
	.highcharts-data-table table {
	    min-width: 320px;
	    max-width: 800px;
	    margin: 1em auto;
	}
	
	.highcharts-data-table table {
	    font-family: Verdana, sans-serif;
	    border-collapse: collapse;
	    border: 1px solid #ebebeb;
	    margin: 10px auto;
	    text-align: center;
	    width: 100%;
	    max-width: 500px;
	}
	
	.highcharts-data-table caption {
	    padding: 1em 0;
	    font-size: 1.2em;
	    color: #555;
	}
	
	.highcharts-data-table th {
	    font-weight: 600;
	    padding: 0.5em;
	}
	
	.highcharts-data-table td,
	.highcharts-data-table th,
	.highcharts-data-table caption {
	    padding: 0.5em;
	}
	
	.highcharts-data-table thead tr,
	.highcharts-data-table tr:nth-child(even) {
	    background: #f8f8f8;
	}
	
	.highcharts-data-table tr:hover {
	    background: #f1f7ff;
	}
	
	input[type="number"] {
	    min-width: 50px;
	}

   div#table_container table {width: 100%}
   div#table_container th, div#table_container td {border: solid 1px gray; text-align: center;} 
   div#table_container th {background-color: #595959; color: white;}


   
</style>

<!-- 부서명별 또는 성별 차트만들기 -->
<script src="https://code.highcharts.com/highcharts.js"></script>
<script src="https://code.highcharts.com/modules/exporting.js"></script>
<script src="https://code.highcharts.com/modules/export-data.js"></script>
<script src="https://code.highcharts.com/modules/accessibility.js"></script>

<!-- 부서명별 및 성별 인원통계 차트만들기 (위의 script 에서 추가됨) -->
<script src="https://code.highcharts.com/modules/data.js"></script>
<script src="https://code.highcharts.com/modules/drilldown.js"></script>

<!-- figure 는 div 와 같지만, figure 속에는 차트나 이미지가 온다. -->

<div style="display: flex;">   
<div style="width: 80%; min-height: 1100px; margin:auto; ">

   <h2 style="margin: 50px 0;">HR 사원 통계정보(차트)</h2>
   
   <form name="searchFrm" style="margin: 20px 0 50px 0; ">
      <select name="searchType" id="searchType" style="height: 30px;">
         <option value="">통계선택하세요</option>
         <option value="deptname">부서명별 인원통계</option>
         <option value="gender">성별 인원통계</option>
         <option value="deptnameGender">부서명별 성별 인원통계</option>
      </select>
   </form>
   
   <div id="chart_container"></div>
   <div id="table_container" style="margin: 40px 0 0 0;"></div>

</div>
</div>


<script type="text/javascript">

	$(document).ready(function() {
		
		$("select#searchType").bind("change", function() {
			func_choice($(this).val());
			// $(this).val() 은 "" 또는 "deptname" 또는 "gender" 또는 "deptnameGender" 이다.
		});
		
	});// end of $(document).ready(function() {})-----------------------

	// Function declaration
	function func_choice(searchType) {
		
		switch (searchType) {
		case "":				// 통계선택하세요를 선택한 경우 - chart 와 table 을 empty 상태로 만든다.
			$("div#chart_container").empty();
			$("div#table_container").empty();
			$("div.highcharts-data-table").empty();
			break;

		case "deptname":		// 부서명별 인원통계를 선택한 경우

			$("div#table_container").empty();
			$("div.highcharts-data-table").empty();
		
			$.ajax({
				// data 를 JSON 형식으로 가져온다. (부서name 별 사원의 명 수를 구하겠다.)
				url:"<%= request.getContextPath()%>/chart/employeeCntByDeptname.action",
				dataType:"JSON",		// where 절 보낼 것이 없기 때문에 data 는 필요 없다.
				success:function(json) {
					
						let resultArr = [];			// resultArr 는 배열
						
						// 배열속에 가져온 정보를 담는다. (Controller 에서 가져온 배열의 길이만큼 반복 돌린다.)
						for(let i=0; i<json.length; i++) {
							
							let obj;
							
							if(i==0) {
									obj = {	<%-- 퍼센티지는 숫자이므로 Number() 를 사용한다. --%>
								            name: json[i].department_name,
								            y: Number(json[i].percentage),
								            sliced: true,
								            selected: true
								    };
								
							}
							else {
									obj = {	<%-- 퍼센티지는 숫자이므로 Number() 를 사용한다. --%>
								            name: json[i].department_name,
								            y: Number(json[i].percentage),	
						          	};
							}
							
							<%-- 배열속에 객체를 넣자. --%>
							resultArr.push(obj)
							
						}// end of for-------------------------------
					
					////////////////////////////////////////////////////////////////////////
					// Data를 가져오도록 한다.
						Highcharts.chart('chart_container', {
					    chart: {
					        plotBackgroundColor: null,
					        plotBorderWidth: null,
					        plotShadow: false,
					        type: 'pie'
					    },
					    title: {
					        text: '우리회사 부서명별 인원통계'
					    },
					    tooltip: {
					        pointFormat: '{series.name}: <b>{point.percentage:.2f}%</b>'
					    },
					    accessibility: {
					        point: {
					            valueSuffix: '%'
					        }
					    },
					    plotOptions: {
					        pie: {
					            allowPointSelect: true,
					            cursor: 'pointer',
					            dataLabels: {
					                enabled: true,
					                format: '<b>{point.name}</b>: {point.percentage:.2f} %'
					            }
					        }
					    },
					    series: [{
					        name: '인원비율',
					        colorByPoint: true,
					        data: resultArr
					        	
					    }]
					});
					////////////////////////////////////////////////////////////////////////
					
					let html = "<table>";
						html += "<tr>" +
									"<th>부서명</th>" +
									"<th>인원수</th>" +
									"<th>퍼센티지</th>" +
								"</tr>";
					
					$.each(json, function(index, item) {
						html += "<tr>" +
									"<td>"+ item.department_name +"</td>" +
									"<td>"+ item.cnt +"</td>" +
									"<td>"+ Number(item.percentage) +"</td>" +
								"</tr>";						
					});
					
					html += "</table>";
					
					$("div#table_container").html(html)					
				},
				error: function(request, status, error){
	                alert("code: "+request.status+"\n"+"message: "+request.responseText+"\n"+"error: "+error);
	             }
			});
		
		
			break;

		case "gender":			// 성별 인원통계를 선택한 경우

			$("div#table_container").empty();
			$("div.highcharts-data-table").empty();
											
			$.ajax({
				// data 를 JSON 형식으로 가져온다. (성별 사원의 명 수를 구하겠다.)
				url:"<%= request.getContextPath()%>/chart/employeeCntByGender.action",
				dataType:"JSON",		// where 절 보낼 것이 없기 때문에 data 는 필요 없다.
				success:function(json) {
					
						let resultArr = [];			// resultArr 는 배열
						
						// 배열속에 가져온 정보를 담는다. (Controller 에서 가져온 배열의 길이만큼 반복 돌린다.)
						for(let i=0; i<json.length; i++) {
							
							let obj;
							
							if(i==0) {
									obj = {	<%-- 퍼센티지는 숫자이므로 Number() 를 사용한다. --%>
								            name: json[i].gender,
								            y: Number(json[i].percentage),
								            sliced: true,
								            selected: true
								    };
								
							}
							else {
									obj = {	<%-- 퍼센티지는 숫자이므로 Number() 를 사용한다. --%>
								            name: json[i].gender,
								            y: Number(json[i].percentage),	
						          	};
							}
							
							<%-- 배열속에 객체를 넣자. --%>
							resultArr.push(obj)
							
						}// end of for-------------------------------
					
					////////////////////////////////////////////////////////////////////////
					// Data를 가져오도록 한다.
						Highcharts.chart('chart_container', {
					    chart: {
					        plotBackgroundColor: null,
					        plotBorderWidth: null,
					        plotShadow: false,
					        type: 'pie'
					    },
					    title: {
					        text: '우리회사 성별 인원통계'
					    },
					    tooltip: {
					        pointFormat: '{series.name}: <b>{point.percentage:.2f}%</b>'
					    },
					    accessibility: {
					        point: {
					            valueSuffix: '%'
					        }
					    },
					    plotOptions: {
					        pie: {
					            allowPointSelect: true,
					            cursor: 'pointer',
					            dataLabels: {
					                enabled: true,
					                format: '<b>{point.name}</b>: {point.percentage:.2f} %'
					            }
					        }
					    },
					    series: [{
					        name: '인원비율',
					        colorByPoint: true,
					        data: resultArr
					        	
					    }]
					});
					////////////////////////////////////////////////////////////////////////
					
					let html = "<table>";
						html += "<tr>" +
									"<th>성별</th>" +
									"<th>인원수</th>" +
									"<th>퍼센티지</th>" +
								"</tr>";
					
					$.each(json, function(index, item) {
						html += "<tr>" +
									"<td>"+ item.gender +"</td>" +
									"<td>"+ item.cnt +"</td>" +
									"<td>"+ Number(item.percentage) +"</td>" +
								"</tr>";						
					});
					
					html += "</table>";
					
					$("div#table_container").html(html)					
				},
				error: function(request, status, error){
	                alert("code: "+request.status+"\n"+"message: "+request.responseText+"\n"+"error: "+error);
	             }
			});
				
			break;

		case "deptnameGender":	// 부서명별 성별 인원통계를 선택한 경우

			$.ajax({
				// data 를 JSON 형식으로 가져온다. (성별 사원의 명 수를 구하겠다.)
				url:"<%= request.getContextPath()%>/chart/employeeCntByDeptname.action",
				dataType:"JSON",		// where 절 보낼 것이 없기 때문에 data 는 필요 없다.
				success:function(json_1) {

					$("div#table_container").empty();
					$("div.highcharts-data-table").empty();
										
					let deptnameArr = [];	// 부서명별 인원수 퍼센티지 객체배열
					
					$.each(json_1, function(index, item) {	// 배열속에 넣자. ()안의 값들을 배열 안에 넣은 것이다.
						deptnameArr.push({name: item.department_name,
			                    		  y: Number(item.percentage),
			                    		  drilldown: item.department_name
			                			});		
					});// end of $.each(json, function(index, item) {}--------------------------------
					
					let genderArr = [];		// 특정 부서명에 근무하는 직원들의 성별 인원수 퍼센티지 객체 배열
					
					$.each(json_1, function(index_1, item_1){
						// 특정부서별로 남녀별 인원을 구하겠다. (json 배열 만큼 반복문(each) 돌린다.)
						$.ajax({
							url:"<%= request.getContextPath()%>/chart/genderCntSpecialDeptname.action",
							data:{"deptname":item_1.department_name},	// 어떤 부서에 있는 남녀인지를 보내줘야 함 (where 절)
							dataType:"JSON",
							success:function(json_2) {
								
								let subArr = [];
								
								// 반복문을 돌리면서 Controller 에서 받아온 값을 하나씩 돌린다.
								$.each(json_2, function(index_2, item_2) {
									// subArr 배열속에 아래와 같은 형태로 들어간다. 넣어주도록 한다.
									subArr.push([item_2.gender,
												 Number(item_2.percentage)]);	// Number 는 반드시 써주도록 한다.
								});// end of $.each(json_2, function(index_2, item_2) {})------------
								
								// genderArr 배열에 넣어주도록 한다.
								genderArr.push({name: item_1.department_name,
												id: item_1.department_name,	
												data:subArr});
								
							},
							error: function(request, status, error){
				                alert("code: "+request.status+"\n"+"message: "+request.responseText+"\n"+"error: "+error);
				            }							
							
						});
						
					});// end of $.each(json, function(index, item){})--------------------
					
					
					
					
				////////////////////////////////////////////////////////////////////////
				// Create the chart (차트그리기)
				Highcharts.chart('chart_container', {
				    chart: {
				        type: 'column'
				    },
				    title: {
				        align: 'left',
				        text: '부서명별 남녀 비율'
				    },
				    subtitle: {
				    <%--  
				    	align: 'left',
				        text: 'Click the columns to view versions. Source: <a href="http://statcounter.com" target="_blank">statcounter.com</a>'
				    --%>
				    },
				    accessibility: {
				        announceNewData: {
				            enabled: true
				        }
				    },
				    xAxis: {
				        type: 'category'
				    },
				    yAxis: {
				        title: {
				            text: '구성비율(%)'
				        }
				
				    },
				    legend: {
				        enabled: false
				    },
				    plotOptions: {
				        series: {
				            borderWidth: 0,
				            dataLabels: {
				                enabled: true,
				                format: '{point.y:.2f}%'
				            }
				        }
				    },
				
				    tooltip: {
				        headerFormat: '<span style="font-size:11px">{series.name}</span><br>',
				        pointFormat: '<span style="color:{point.color}">{point.name}</span>: <b>{point.y:.2f}%</b> of total<br/>'
				    },
				
				    series: [
				        {
				            name: "Browsers",
				            colorByPoint: true,
				            data: deptnameArr		// **** 위에서 구한 값을 대입시킴. 부서명별 인원수 퍼센티지 객체 배열 ****//
				            
				           /* 
				            data: [
				                {
				                    name: "Chrome",
				                    y: 62.74,
				                    drilldown: "Chrome"
				                },
				                {
				                    name: "Firefox",
				                    y: 10.57,
				                    drilldown: "Firefox"
				                },
				                {
				                    name: "Internet Explorer",
				                    y: 7.23,
				                    drilldown: "Internet Explorer"
				                },
				                {
				                    name: "Safari",
				                    y: 5.58,
				                    drilldown: "Safari"
				                },
				                {
				                    name: "Edge",
				                    y: 4.02,
				                    drilldown: "Edge"
				                },
				                {
				                    name: "Opera",
				                    y: 1.92,
				                    drilldown: "Opera"
				                },
				                {
				                    name: "Other",
				                    y: 7.62,
				                    drilldown: null
				                }
				            ]
				        */
				        }
				    ],
				    drilldown: {
				        breadcrumbs: {
				            position: {
				                align: 'right'
				            }
				        },
				        series: genderArr	// **** 위에서 구한 값을 대입시킴. 특정 부서명에 근무하는 직원들의 성별 인원수 퍼센티지 객체 배열 ****//
				        /*
				        series: [
				            {
				                name: "Chrome",
				                id: "Chrome",
				                data: [
				                    [
				                        "v65.0",
				                        0.1
				                    ],
				                    [
				                        "v64.0",
				                        1.3
				                    ],
				                    [
				                        "v63.0",
				                        53.02
				                    ],
				                    [
				                        "v62.0",
				                        1.4
				                    ],
				                    [
				                        "v61.0",
				                        0.88
				                    ],
				                    [
				                        "v60.0",
				                        0.56
				                    ],
				                    [
				                        "v59.0",
				                        0.45
				                    ],
				                    [
				                        "v58.0",
				                        0.49
				                    ],
				                    [
				                        "v57.0",
				                        0.32
				                    ],
				                    [
				                        "v56.0",
				                        0.29
				                    ],
				                    [
				                        "v55.0",
				                        0.79
				                    ],
				                    [
				                        "v54.0",
				                        0.18
				                    ],
				                    [
				                        "v51.0",
				                        0.13
				                    ],
				                    [
				                        "v49.0",
				                        2.16
				                    ],
				                    [
				                        "v48.0",
				                        0.13
				                    ],
				                    [
				                        "v47.0",
				                        0.11
				                    ],
				                    [
				                        "v43.0",
				                        0.17
				                    ],
				                    [
				                        "v29.0",
				                        0.26
				                    ]
				                ]
				            },
				            {
				                name: "Firefox",
				                id: "Firefox",
				                data: [
				                    [
				                        "v58.0",
				                        1.02
				                    ],
				                    [
				                        "v57.0",
				                        7.36
				                    ],
				                    [
				                        "v56.0",
				                        0.35
				                    ],
				                    [
				                        "v55.0",
				                        0.11
				                    ],
				                    [
				                        "v54.0",
				                        0.1
				                    ],
				                    [
				                        "v52.0",
				                        0.95
				                    ],
				                    [
				                        "v51.0",
				                        0.15
				                    ],
				                    [
				                        "v50.0",
				                        0.1
				                    ],
				                    [
				                        "v48.0",
				                        0.31
				                    ],
				                    [
				                        "v47.0",
				                        0.12
				                    ]
				                ]
				            },
				            {
				                name: "Internet Explorer",
				                id: "Internet Explorer",
				                data: [
				                    [
				                        "v11.0",
				                        6.2
				                    ],
				                    [
				                        "v10.0",
				                        0.29
				                    ],
				                    [
				                        "v9.0",
				                        0.27
				                    ],
				                    [
				                        "v8.0",
				                        0.47
				                    ]
				                ]
				            },
				            {
				                name: "Safari",
				                id: "Safari",
				                data: [
				                    [
				                        "v11.0",
				                        3.39
				                    ],
				                    [
				                        "v10.1",
				                        0.96
				                    ],
				                    [
				                        "v10.0",
				                        0.36
				                    ],
				                    [
				                        "v9.1",
				                        0.54
				                    ],
				                    [
				                        "v9.0",
				                        0.13
				                    ],
				                    [
				                        "v5.1",
				                        0.2
				                    ]
				                ]
				            },
				            {
				                name: "Edge",
				                id: "Edge",
				                data: [
				                    [
				                        "v16",
				                        2.6
				                    ],
				                    [
				                        "v15",
				                        0.92
				                    ],
				                    [
				                        "v14",
				                        0.4
				                    ],
				                    [
				                        "v13",
				                        0.1
				                    ]
				                ]
				            },
				            {
				                name: "Opera",
				                id: "Opera",
				                data: [
				                    [
				                        "v50.0",
				                        0.96
				                    ],
				                    [
				                        "v49.0",
				                        0.82
				                    ],
				                    [
				                        "v12.1",
				                        0.14
				                    ]
				                ]
				            }
				        ]
				      */
				    }
				});
				////////////////////////////////////////////////////////////////////////
				
				},
				error: function(request, status, error){
	                alert("code: "+request.status+"\n"+"message: "+request.responseText+"\n"+"error: "+error);
	             }
			});		
		
			break;
		
		}
		
	}//end of function func_choice(searchType) {})-----------------
	
</script>


