<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" type="text/css" href="css/processStyle.css" />
<link rel="stylesheet" type="text/css" href="css/jquery.loadmask.css" />
<script type="text/javascript" src="js/jquery-1.9.1.min.js"></script>
<script type="text/javascript" src="js/modernizr-1.5.min.js"></script>
<script type='text/javascript' src="js/jquery.loadmask.js"></script>

<title>Start Daemon</title>
<script type="text/javascript">
	$(document).ready(function() {		 
		var checkLogin = "<%=session.getAttribute("LOGIN_ERROR") %>";
		if("SUCESS" != checkLogin) {
			location.href='index.jsp';
			return;
		}
		$.ajax({
			url : "/ssangyongweb/SYMCTimerServlet",
			/*data:param,*/
			type : "POST",
			dataType : "json",
			success : function(data) {
				//[SR140723-053][20140723] shcho, 타 차종 Migration으로 인한 기존 배치 서비스 불필요 (VPM I/F VehPart Damon, VPM I/F VPM Validate Damon)
				for ( var i = 0; i < 2; i++) {
					$("#StatusTd_"+i).text(data["Status_"+i]);
	                $("#StartTimeTd_"+i).text(data["StartTime_"+i]);
	                $("#EndTimeTd_"+i).text(data["EndTime_"+i]);
	                $("#ExcuteTimeTd_"+i).text(data["ExcuteTime_"+i]);
	                if("" != data["EndTime_"+i]) {
	                    if("" != data.DownloadUrl_0) {
	                        var downUrl = "/ssangyongweb/" + data["DownloadUrl_"+i];
	                        downloadLogFile("#ExcuteTimeTd_"+i, downUrl);   
	                    }
	                }
	                if("End" == data["Status_"+i]) {
	                	$("#Button_"+i).show();	
	                }
				}
			},
			error : function(jqXHR, textStatus, errorThrown) {
				alert("웹서버 에러입니다. 관리자에게 문의하세요.");
			}
		});		
		
		$("#deleteButton").click(function() {
		    if (confirm("삭제하시겠습니까?") == true){    //확인
		    	$.ajax({
	                url : "/ssangyongweb/SYMCTimerServlet",
	                data:{'method':'deleteAllLog'},
	                type : "POST",
	                dataType : "json",
	                success : function(data) {          
	                    alert(data.status);
	                },
	                error : function(jqXHR, textStatus, errorThrown) {
	                    alert("웹서버 에러입니다. 관리자에게 문의하세요.");
	                }
                });	           		     	  
			} else {   //취소
			  return;
			}	
		});
		
		// BUTTON ACTION
		$("#Button_0").click(function() {
            $("#Button_0").hide();
            execute("eciDeamon"); 
        });
        
        $("#Button_1").click(function() {
            $("#Button_1").hide();
            execute("ecoDeamon"); 
        });
        
        $("#Button_2").click(function() {
            $("#Button_2").hide();
            execute("vehPartDamon");
        });
        
        $("#Button_3").click(function() {
            $("#Button_3").hide();
            execute("vpmValidateDamon");
        });
	});
	
	function downloadLogFile(tag, url) {
		$(tag).wrapInner('<a href="#" onclick="Popup=window.open(\'' + url + '\',\'Popup\',\'toolbar=no,location=no,status=no,menubar=no,scrollbars=yes,resizable=yes, width=1024,height=768,left=430,top=23\'); return false;" \\>');
	}
	
	function execute(damonName) {
		$("#container").mask("Waiting...");
		$.ajax({
            url : "/ssangyongweb/SYMCTimerServlet",
            data:{'method':'execute', 'damonName':damonName},
            type : "POST",
            dataType : "json",
            success : function(data) {          
            	$("#container").unmask();
                alert(data.status);
                location.reload();
            },
            error : function(jqXHR, textStatus, errorThrown) {
            	$("#container").unmask();
            	alert("웹서버 에러입니다. 관리자에게 문의하세요.");
            }
        });
	}
</script>
</head>
<body>
	<div id="container">
		<div id="main">
			<header>
			<div id="logo">
			</div>
			</header>
			<div id="site_content">
				<h2>웹 스케쥴러 관리</h2>
				<p><font color="red">*주의 </font><br> 관리자만 실행 하시고 여러번 실행금지입니다.</p>				
				<table style="width: 100%; border-spacing: 0;">
					<tr>
						<th>스케쥴러명(Damon Name)</th>
						<th>상태</th>
						<th>최근 실행 시작 시간</th>
						<th>최근 실행 종료 시간</th>
						<th>소요시간(초.0f)</th>
						<th></th>
					</tr>			
                    <%  for(int i=0; i < 2; i++) {      %>
                    <tr>
                    <%      if(i == 0) {                %>  
                                <td>ECI Damon</td>
                            <%} else if(i == 1) {       %>
                                <td>ECO Damon</td>                                                        
                    <%     }                           %>                     
	                   <td id="StatusTd_<%=i%>">-</td>
	                   <td id="StartTimeTd_<%=i%>">-</td>
	                   <td id="EndTimeTd_<%=i%>">-</td>
	                   <td id="ExcuteTimeTd_<%=i%>">-</td>
	                   <td><input class="button" style="display:none;" type="button" id="Button_<%=i%>" name="Button_<%=i%>"+ value="실행" /></td>
                    </tr>                     
                    <%  } %>                    					
				</table>
				<p align="right"><input class="button" type="button" id="deleteButton" name="deleteButton" value="로그파일 전체 삭제" /></p>
			</div>
		</div>
</body>
</html>