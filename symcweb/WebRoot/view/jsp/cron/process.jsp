<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
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
		var checkLogin = "${sessionScope.CONFIRM}";
		if("Y" != checkLogin) {
			//location.href='login.do';
			return;
		}
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
		/**
		$(":button").each(function(index) {
            $(this).click(function() {
                alert(index);
            });
        });
		**/
	});

	function downloadLogFile(tag, url) {
		$(tag).wrapInner('<a href="#" onclick="Popup=window.open(\'' + url + '\',\'Popup\',\'toolbar=no,location=no,status=no,menubar=no,scrollbars=yes,resizable=yes, width=1024,height=768,left=430,top=23\'); return false;" \\>');
	}

	function pauseCron(trrigerId) {
		if (confirm(trrigerId + " 스케쥴러를 중단하시겠습니까?") != true){    //확인
			   return;
		}
		$("#container").mask("Waiting...");
        $.ajax({
            url : "setPauseProcess.do",
            data:{'id':trrigerId},
            type : "POST",
            dataType : "json",
            success : function(data) {
                $("#container").unmask();
                location.reload();
            },
            error : function(jqXHR, textStatus, errorThrown) {
                $("#container").unmask();
                alert("웹서버 에러입니다. 관리자에게 문의하세요.");
            }
        });
	}

	function resumeCron(trrigerId) {
		if (confirm(trrigerId + " 스케쥴러를 대기 상태로 하시겠습니까?") != true){    //확인
            return;
        }
        $("#container").mask("Waiting...");
        $.ajax({
            url : "setResumeProcess.do",
            data:{'id':trrigerId},
            type : "POST",
            dataType : "json",
            success : function(data) {
                $("#container").unmask();
                location.reload();
            },
            error : function(jqXHR, textStatus, errorThrown) {
                $("#container").unmask();
                alert("웹서버 에러입니다. 관리자에게 문의하세요.");
            }
        });
    }

	function setExceuteNowProcess(trrigerId) {
        if (confirm(trrigerId + " 스케쥴러를 강제 실행 하시겠습니까?") != true){    //확인
            return;
        }
        $("#container").mask("Waiting...");
        $.ajax({
            url : "setExceuteNowProcess.do",
            data:{'id':trrigerId},
            type : "POST",
            dataType : "json",
            success : function(data) {
                $("#container").unmask();
                if(data.status == 'runningPass') {
                	alert('JOB 실행 중이므로 실행 하지못하였습니다.');
                }
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
				<table id="schTable" style="width: 100%; border-spacing: 0;">
					<tr>
					    <th>트리거명</th>
						<th>스케쥴러명</th>
						<th>상태</th>
						<th>최근 실행 시작 시간</th>
						<th>최근 실행 종료 시간</th>
						<th>소요시간(초.0f)</th>
						<th>다음 실행 예정 시간</th>
						<th>로그파일크기</th>
						<th>상태 변경</th>
						<th>강제실행</th>
					</tr>
                    <c:forEach var="task" items="${TASK_LIST}">
	                    <tr>
	                       <td><c:out value="${task.triggerId}"/></td>
	                       <td><c:out value="${task.description}"/></td>
	                       <td id="StatusTd_<c:out value="${task.triggerId}"/>">
	                           <c:if test="${task.isExecute != 'true'}">
                                    <c:if test="${task.status == 0}">
                                                대기중
                                    </c:if>
                                    <c:if test="${task.status == 1}">
                                                스케쥴중단
                                    </c:if>
                               </c:if>
                               <c:if test="${task.isExecute == 'true'}">
                                    JOB 실행 중
                               </c:if>
	                       </td>
	                       <td id="StartTimeTd_<c:out value="${task.triggerId}"/>"><c:out value="${task.startTime}"/></td>
	                       <td id="EndTimeTd_<c:out value="${task.triggerId}"/>">
	                           <c:if test="${task.endTime == '-'}">
	                               <c:out value="${task.endTime}"/>
	                           </c:if>
	                           <c:if test="${task.endTime != '-'}">
                                <a href="logFileDownload.do?id=${task.triggerId}"><c:out value="${task.endTime}"/></a>
                               </c:if>
	                           <!-- <c:out value="${task.endTime}"/> -->
	                       </td>
	                       <td id="ExcuteTimeTd_<c:out value="${task.triggerId}"/>"><c:out value="${task.delayTime}"/></td>
	                       <td id="NextFireTimeTimeTd_<c:out value="${task.triggerId}"/>"><c:out value="${task.nextFireTime}"/></td>
	                       <!-- [20151216][ymjang] 로그파일사이즈 항목 추가 -->
	                       <td id="LogFileSize_<c:out value="${task.triggerId}"/>"><c:out value="${task.logFileSize}"/></td>
	                       <td>
	                           <c:if test="${task.status == 0}">
	                               <input class="button" type="button" id="Pause_Button_<c:out value="${task.triggerId}"/>" name="Pause_Button_<c:out value="${task.triggerId}"/>" value="중단"  onClick="pauseCron('${task.triggerId}'); return false;"/>
	                           </c:if>
	                           <c:if test="${task.status == 1}">
                                   <input class="button" type="button" id="Resume_Button_<c:out value="${task.triggerId}"/>" name="Resume_Button_<c:out value="${task.triggerId}"/>" value="대기"  onClick="resumeCron('${task.triggerId}'); return false;"/>
                               </c:if>
	                       </td>
	                       <td>
	                           <c:if test="${task.isExecute != 'true'}">
	                               <input class="button" type="button" id="Button_<c:out value="${task.triggerId}"/>" name="Button_<c:out value="${task.triggerId}"/>" value="강제실행" onClick="setExceuteNowProcess('${task.triggerId}'); return false;"/>
	                           </c:if>
	                       </td>
	                    </tr>
                    </c:forEach>
				</table>
				<!--
				<p align="right"><input class="button" type="button" id="deleteButton" name="deleteButton" value="로그파일 전체 삭제" /></p>
				-->
			</div>
		</div>
	</div>
</body>
</html>