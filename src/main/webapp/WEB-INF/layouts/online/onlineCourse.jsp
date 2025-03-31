<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<sec:authorize access="isAuthenticated()">
<sec:authentication var="role" property='principal.authorities'/>
<sec:authentication var="id" property="principal.username"/>
<sec:authentication var="firstName" property="principal.firstName"/>
<sec:authentication var="lastName" property="principal.lastName"/>
	<script>
		var role = '${role}';
		var numericGrade = role.replace(/[\[\]]/g, ''); // replace '[' & ']' with an empty string
		var studentId = '${id}';
		var firstName = '${firstName}';
		var lastName = '${lastName}';
		var academicYear;
    	var academicWeek;
		var academicSet;
		// Variables to store the current video being watched
		var watchingId = 0;
		// var watchingUrl = '';
		// var watchingTime = '';

	</script>
</sec:authorize>

<script>
$(function() {
	// to get the academic year and week
	$.ajax({
		url : '${pageContext.request.contextPath}/class/academy',
		method: "GET",
		success: function(response) {
			// save the response into the variable
			academicYear = response[0];
			academicWeek = response[1];
			academicSet = academicWeek-1;
			// update online url
			getOnlineLive(studentId, academicYear, academicWeek);
		},
		error: function(jqXHR, textStatus, errorThrown) {
			console.log('Error : ' + errorThrown);
		}
	});
	// initialise state list when loading
	listState('#editState');
    listBranch('#editBranch');
	listGrade('#editGrade');

    // save end time before closing broswer
	window.addEventListener('beforeunload', function (event) {
		$.ajax({
			url: '${pageContext.request.contextPath}/elearning/endWatch/' + studentId + '/' + watchingId, // Backend endpoint to handle the log
			type: 'GET',
			success: function(response) {
				console.log(response);
			},
			error: function(xhr, status, error) {
				console.error('Error logging action: ', error);
			}
		});
		// Display a confirmation dialog (optional, may not work in all browsers)
		event.preventDefault(); // Chrome requires returnValue to be set
		event.returnValue = ''; // This prompts a standard confirmation dialog
	});
	
});

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//		Retrieve Student Info
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
function retrieveStudentInfo() {
	$.ajax({
		url : '${pageContext.request.contextPath}/online/get/' + studentId,
		type : 'GET',
		success : function(student) {
			$('#editStudentModal').modal('show');
			// Update display info
			//  console.log(student);
			$("#editId").val(student.id);
			$("#editFirstName").val(student.firstName);
			$("#editLastName").val(student.lastName);
			$("#editEmail1").val(student.email1);
			$("#editEmail2").val(student.email2);
			$("#editRelation1").val(student.relation1);
			$("#editRelation2").val(student.relation2);
			$("#editAddress").val(student.address);
			$("#editContact1").val(student.contactNo1);
			$("#editContact2").val(student.contactNo2);
			$("#editState").val(student.state);
			$("#editBranch").val(student.branch);
			$("#editGrade").val(student.grade);
			$("#editGender").val(student.gender);
            var regDate = formatDate(student.registerDate);
			$("#editRegisterDate").val(regDate);
		},
		error : function(xhr, status, error) {
			console.log('Error : ' + error);
		}
	});
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////
// 			Update password
////////////////////////////////////////////////////////////////////////////////////////////////////////
function updatePassword() {
	var id = $("#editId").val();
	var newPwd = $("#newPassword").val();
	var confirmPwd = $("#confirmPassword").val();
	//warn if Id is empty
	if (id == '') {
		$('#warning-alert .modal-body').text('Please search student record before updating');
		$('#warning-alert').modal('toggle');
		return;
	}
	// warn if newPwd or confirmPwd is empty
	if (newPwd == '' || confirmPwd == '') {
		$('#warning-alert .modal-body').text('Please enter new password and confirm password');
		$('#warning-alert').modal('toggle');
		return;
	}
	//warn if newPwd is not same as confirmPwd
	if(newPwd != confirmPwd){
		$('#warning-alert .modal-body').text('New password and confirm password are not the same');
		$('#warning-alert').modal('toggle');
		return;
	}
	// send query to controller
	$.ajax({
		url : '${pageContext.request.contextPath}/online/updatePassword/' + id + '/' + confirmPwd,
		type : 'PUT',
		success : function(data) {
			$('#success-alert .modal-body').html('<b>Password</b> is now updated');
			$('#success-alert').modal('toggle');
			// clear fields
			clearPassword();
			// close modal
			$('#editStudentModal').modal('toggle');
		},
		error : function(xhr, status, error) {
			console.log('Error : ' + error);
		}
		
	}); 
}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//		Retrieve Online Session Info
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
function getOnlineLive(studentId, year, week) {
	$.ajax({
		url : '${pageContext.request.contextPath}/elearning/getLive/' + studentId + '/' + year + '/' + week,
		type : 'GET',
		success : function(data) {
			$.each(data, function(index, live) {
				// console.log("Live : " + index);
			    // console.log(live);
				var url = live.address;
				// Create a new session element
				var sessionElement = $('<div id="onlineLesson'+index+'" class="onlineLesson alert alert-info"></div>');
                sessionElement.attr('data-video-url', url);
                sessionElement.append('<p id="liveBlock'+index+'" class="m-1">Online Live Weekly Lesson<strong> Set</strong> <span id="academicWeek'+index+'">'+ (week-1) +'</span> - <i>' + live.title+ '</i> (' +
                    '<span id="onlineLessonDayTitle'+index+'" name="onlineLessonDayTitle'+index+'">' + dayName(live.day) + '</span>, ' +
                    '<span id="onlineLessonStartTitle'+index+'" name="onlineLessonStartTitle'+index+'">' + live.startTime + '</span> ~ ' +
                    '<span id="onlineLessonEndTitle'+index+'" name="onlineLessonEndTitle'+index+'">' + live.endTime + '</span>&nbsp;&nbsp;' +
                    '<i id="micIcon'+index+'" name="micIcon'+index+'" class="bi bi-mic-fill text-secondary h5" title="Live"></i>)' +
                    '&nbsp;<i id="livePlayIcon'+index+'" class="bi bi-caret-right-square text-primary" title="Play Video"></i>' + 
					'<span id="onlineLessonId'+index+'" name="onlineLessonId'+index+'" style="visibility: hidden;">' + live.id + '</span></p>');
                // Append the session element to the container
                $('#liveBlocks').append(sessionElement);
				// Add event listener to the newly created element
                sessionElement.on('click', handleOnlineLessonClick);
               // check recorded session
				determineLiveOrRecordedLesson(index);
			});
		},
		error : function(xhr, status, error) {
			console.log('Error : ' + error);
		}
	});
}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//		Retrieve Recorded Session Info
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
function getRecordedSession(studentId, year, week, set, day, before) {
	$.ajax({
        url: '${pageContext.request.contextPath}/elearning/getRecord/' + studentId + '/' + year + '/' + week + '/' + set,
        type: 'GET',
        success: function(data) {
            $.each(data, function(index, record) {
				// check if day is same as multiple records returned
                if(dayName(record.day) == day){
					// console.log("Record : " + index);
					// console.log(record);
					var url = record.address;
					// Create a new session element
					var sessionElement = $('<div id="recordLesson'+index+'"class="recordLesson alert alert-primary" style="pointer-events: auto; cursor: pointer;"></div>');
					sessionElement.attr('data-video-url', url);
					sessionElement.append('<p id="recordBlock' + index + '" class="m-1">Recorded Weekly Lesson <strong>Set</strong> <span id="recordWeek' + index + '"></span> - <i>' + record.title +'</i> (' +
						'<span id="recordLessonDayTitle' + index + '" name="recordLessonDayTitle' + index + '"></span>) ' +
						'<i id="recordPlayIcon' + index + '" class="bi bi-caret-right-square text-primary" title="Play Video"></i>' +
						'<span id="recordLessonId'+index+'" name="recordLessonId'+index+'" style="visibility: hidden;">' + record.id + '</span></p>');
					// Append the session element to the container
					$('#recordBlocks').append(sessionElement);
					// Add event listener to the newly created element
					sessionElement.on('click', handleRecordLessonClick);
					// Update the elements here after they have been appended
					//before ? $("#recordWeek" + index).text(set-1) : $("#recordWeek" + index).text(set);
					$("#recordWeek" + index).text(record.week);
					before ? $("#recordLessonDayTitle" + index).text('Available until this ' + dayName(record.day) + ', ' + record.startTime) : $("#recordLessonDayTitle" + index).text('Available until next ' + dayName(record.day) + ', ' + record.startTime ) ;
				}
			});
        },
        error: function(xhr, status, error) {
            console.log('Error : ' + error);
        }
    });
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////
// 			Display grade
////////////////////////////////////////////////////////////////////////////////////////////////////////
function displayGrade() {
	var numericPart = role.replace(/[\[\]]/g, ''); // replace '[' & ']' with an empty string
	var grade = gradeName(numericPart);
	return grade;
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////
// 			Clear password fields
////////////////////////////////////////////////////////////////////////////////////////////////////////
function clearPassword() {
	$("#newPassword").val('');
	$("#confirmPassword").val('');
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////
// 			Determine Online live/ Recorded lession
////////////////////////////////////////////////////////////////////////////////////////////////////////
function determineLiveOrRecordedLesson(index) {
    // get the current date & time
	var now = new Date();
	// get the time from the elements
    var lessonDay = $('#onlineLessonDayTitle' + index).text();
    var lessonStart = $('#onlineLessonStartTitle' + index).text();
    var lessonEnd = $('#onlineLessonEndTitle' + index).text();
	// convert the times to Date objects for comparison
    var lessonStartDate = getTimeForDayAndTime(lessonDay, lessonStart);
    var lessonEndDate = getTimeForDayAndTime(lessonDay, lessonEnd);

	// Add 18 hours to lessonEndDate as the lesson will be updated at 1 pm next day
	lessonEndDate.setHours(lessonEndDate.getHours() + 18);

    if (now.getTime() >= lessonStartDate && now.getTime() <= lessonEndDate) { // ON AIR
        console.log("Onair");
		// 1. turn on mic
        $('#micIcon' + index).removeClass('text-secondary').addClass('text-danger');
		$('#onlineLesson'+index).css({'pointer-events': 'auto', 'cursor': 'pointer'});
    	// 2. show last week recorded lesson - to fill the gap between live updated time at next day 1 pm
		getRecordedSession(studentId, academicYear, academicWeek, academicSet - 1, lessonDay, false);	
    } else if (now.getTime() < lessonStartDate) { // BEFORE AIR
        console.log("Before Onair");
		// 1. disable live lesson
        var liveBlock = $('#onlineLesson' + index);
        liveBlock.removeClass('alert-info').addClass('alert-secondary');
        var liveIcon = $('#livePlayIcon' + index);
        liveIcon.removeClass('text-primary').addClass('text-secondary');
        liveBlock.attr('data-video-url', '');
        liveBlock.css('pointer-events', 'none');
        liveBlock.css('cursor', 'not-allowed');
		var selectedDay = $('#onlineLessonDayTitle' + index).text();
		// 2. get recorded lesson
        getRecordedSession(studentId, academicYear, academicWeek, academicSet - 1, selectedDay, true);
    } else { // AFTER AIR
        console.log("After onair");
		// 1. disable live lesson
        var liveBlock = $('#onlineLesson' + index);
        liveBlock.removeClass('alert-info').addClass('alert-secondary');
        var liveIcon = $('#livePlayIcon' + index);
        liveIcon.removeClass('text-primary').addClass('text-secondary');
        liveBlock.attr('data-video-url', '');
        liveBlock.css('pointer-events', 'none');
        liveBlock.css('cursor', 'not-allowed');
		var selectedDay = $('#onlineLessonDayTitle' + index).text();
        // 2. get recorded lesson
		getRecordedSession(studentId, academicYear, academicWeek, academicSet, selectedDay, false);
    }
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////
// 			Get time for live onair comparison
////////////////////////////////////////////////////////////////////////////////////////////////////////
function getTimeForDayAndTime(day, time) {
    // Create an array of days to map them to corresponding indices
    const daysOfWeek = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];
    // Get the current date
    const currentDate = new Date();
    // Find the index of the specified day
    const dayIndex = daysOfWeek.indexOf(day);
    if (dayIndex === -1) {
        console.error("Invalid day provided");
        return null;
    }
    // Calculate the difference between the current day and the specified day
    var dayOnMondayBase = currentDate.getDay();
    dayOnMondayBase = (dayOnMondayBase === 0) ? 6 : dayOnMondayBase - 1;
    let dayDifference = dayIndex - dayOnMondayBase;
    currentDate.setDate(currentDate.getDate() + dayDifference);
    // Parse the time string and set the hours and minutes
    const [hours, minutes] = time.split(':');
    currentDate.setHours(parseInt(hours, 10), parseInt(minutes, 10), 0, 0);
    // Return the Date object
    return currentDate;
}
/////////////////////////////////////////////////////////////////////////////////////////////////////////
// 			Click on Live section
////////////////////////////////////////////////////////////////////////////////////////////////////////
function handleOnlineLessonClick(event) {
	const onlineLesson = event.currentTarget;
	// set the videoUrl to the hidden input field
	document.getElementById("realtimeVideoUrl").value = onlineLesson.getAttribute('data-video-url');
	// Get the value of onlineLessonDayTitle
	const dayElement = onlineLesson.querySelector('[id^="onlineLessonDayTitle"]');
	const dayValue = dayElement.textContent;
	const startElement = onlineLesson.querySelector('[id^="onlineLessonStartTitle"]');
	const startValue = startElement.textContent;
	const endElement = onlineLesson.querySelector('[id^="onlineLessonEndTitle"]');
	const endValue = endElement.textContent;
	const idElement = onlineLesson.querySelector('[id^="onlineLessonId"]');
	const idValue = idElement.textContent;
	// set the value of onlineLessonDayTitle to onlineLessonDay
	document.getElementById("onlineLessonDay").textContent = dayValue;
	document.getElementById("onlineLessonStart").textContent = startValue;
	document.getElementById("onlineLessonEnd").textContent = endValue;
	document.getElementById("realtimeVideoId").value = idValue;
	// Show confirmation dialog before calling handleLessonClick
	$('#onleLessonWarning').modal('show');
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////
// 			Click on Live section
////////////////////////////////////////////////////////////////////////////////////////////////////////
function handleRecordLessonClick(event) {
	const recordLesson = event.currentTarget;
	// set the videoUrl to the hidden input field
	document.getElementById("recordVideoUrl").value = recordLesson.getAttribute('data-video-url');
	// get the value of onlineLessonDayTitle
	const dayElement = recordLesson.querySelector('[id^="recordLessonDayTitle"]');
	const dayValue = dayElement.textContent;
	document.getElementById("recordLessonDay").textContent = dayValue;
	// get id value
	const idElement = recordLesson.querySelector('[id^="recordLessonId"]');
	const idValue = idElement.textContent;
	document.getElementById("recordVideoId").value = idValue;
	// Show confirmation dialog before calling handleLessonClick
	$('#recordLessonWarning').modal('show');
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////
// 			Play video
////////////////////////////////////////////////////////////////////////////////////////////////////////
function displayMedia(videoUrl) {
	if(watchingId!=null && watchingId!=0){
		console.log('>>>>>>>>> STOP >>>>>>>>>>>>>> ' + watchingId);
		$.ajax({
			url: '${pageContext.request.contextPath}/elearning/endWatch/' + studentId + '/' + watchingId, // Backend endpoint to handle the log
			type: 'GET',
			success: function(response) {
				console.log(response);
			},
			error: function(xhr, status, error) {
				console.error('Error logging action: ', error);
			}
		});
	}
	// remove iframe initial background
	document.getElementById('lessonVideo').style.background = 'none';
	// get the videoUrl from the hidden input field
	const videoAddress = document.getElementById(videoUrl).value;
	// set the video URL as the iframe's src attribute
	document.getElementById('lessonVideo').setAttribute('src', videoAddress);
	// show the video by setting the iframe's display to block
	document.getElementById('lessonVideo').style.display = 'block';
	// Hide the media warning modal
	$('#onleLessonWarning').modal('hide');
	$('#recordLessonWarning').modal('hide');
}


///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//	Track Video Play & Stop action
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
function startVideoTimer(id) {
    // set the watchingId to the current video being watched
	watchingId = id;
	console.log('>>>>>>>>> Start >>>>>>>>>>>>>> ' + watchingId);
	// trigger timestamp to server
	$.ajax({
		url: '${pageContext.request.contextPath}/elearning/startWatch/' + studentId + '/' + id, // Backend endpoint to handle the log
		type: 'GET',
		success: function(response) {
			console.log(response);
		},
		error: function(xhr, status, error) {
			console.error('Error logging action: ', error);
		}
	});	
}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//	Login Activity & Link to Connected Class
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
function accessConnectedClass() {
	// login activity store
	$.ajax({
		url: '${pageContext.request.contextPath}/elearning/checkLogin/' + studentId, 
		type: 'GET'
	});	
    const url = '${pageContext.request.contextPath}/connected/lesson';
    window.open(url, '_blank');
}

</script>    

<style>

span#studentName:hover {
	cursor: pointer;
}

.custom-icon {
font-size: 2rem; /* Adjust the size as needed */
}

/* Style for an additional container element */
.iframe-container {
	margin: 5px; /* Adjust the margin as needed */
}

/* Style for the iframe */
#lessonVideo {
	width: 1000px;
	height: 450px;
	border: none;
	background: url('${pageContext.request.contextPath}/image/lecture.jpg') center center no-repeat;
	background-size: 60%;
}

/* Elevated Card / Box Look */
.card, .alert, .modal-content {
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12); /* deeper, smoother shadow */
  transition: box-shadow 0.3s ease, transform 0.3s ease;
}

/* Subtle lift on hover */
.card:hover, .alert:hover {
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.onlineLesson:hover, .recordLesson:hover {
  transform: scale(1.01);
  box-shadow: 0 4px 12px rgba(0, 123, 255, 0.2);
  transition: all 0.3s ease;
  cursor: pointer;
}

.btn-connected-class {
  background: linear-gradient(to right, #3d5afe, #536dfe);
  color: white;
  font-weight: 600;
  border: none;
  padding: 12px 28px;
  font-size: 1rem;
  border-radius: 50px;
  box-shadow: 0 6px 15px rgba(61, 90, 254, 0.3);
  transition: all 0.3s ease;
}


.btn-connected-class:hover {
  background: linear-gradient(to right, #ffeb3b, #fff176); /* yellow gradient */
  color: #2c3e50;
  box-shadow: 0 8px 20px rgba(255, 235, 59, 0.4);
  transform: translateY(-2px);
}

</style>

<div class="container-fluid pl-0 pr-0" style="background-image: url('${pageContext.request.contextPath}/image/online-background.jpg'); background-size: cover;">
	<sec:authorize access="isAuthenticated()">
		<div class="card-body jae-background-color" style="display: flex; align-items: center; justify-content: space-between; padding: 0.2rem;">
			<div class="content-container">
				<span class="card-text text-warning font-weight-bold font-italic h5" style="margin-left: 25px;" id="studentName" onclick="clearPassword();retrieveStudentInfo()">${firstName} ${lastName}</span>
				<span style="color: white;">&nbsp;&nbsp;(</span>
				<span class="card-text" id="studentGrade" name="studentGrade" style="color: white;"></span>
				<span style="color: white;">)  </span>
				<script>document.getElementById("studentGrade").textContent = displayGrade();</script>
			</div>
			<div class="card-body jae-background-color text-center" style="padding: 1rem;">
				<img src="${pageContext.request.contextPath}/image/logo.png" style="filter: brightness(0) invert(1);width:65px;" >
				&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="text-light align-middle h2">Jac-eLearning Student Lecture</span>           
			</div>
			<div>
				<div style="display: flex; align-items: center; margin-top: 5px;">
					<table>
						<tr>
							<td>
								<span class="text-white">
									<c:set var="now" value="<%= new java.util.Date() %>" />
									Logged at <fmt:formatDate value="${now}" pattern="dd/MM/yyyy HH:mm" />
								</span>
							</td>
							<td>
								<form:form action="${pageContext.request.contextPath}/online/logout" method="POST" id="logout" style="margin-bottom: 0px;">
									<button class="btn mr-1"><i class="bi bi-power custom-icon text-warning" title="Log Out"></i></button>
								</form:form>				
							</td>
						</tr>
					</table>
				</div>
			</div>
		</div>
	</sec:authorize>
	
	<div class="container py-2">
        <div class="card shadow-sm rounded-4">
            <div class="card-body text-center" style="padding-bottom: 0px;">
                <h3 class="card-title text-primary mt-2">Welcome to Jac-eLearning!</h3>
                <p class="text-muted">Your personal hub for live and recorded online lessons at James An College.</p>
                <div class="my-1">
                    <!-- <img src="${pageContext.request.contextPath}/image/live.png" alt="Online Class" style="width: 200px;"> -->
					<div class="iframe-container" style="display: flex; justify-content: center; align-items: center;">
						<iframe id="lessonVideo" src="" allow="autoplay; encrypted-media" allowfullscreen></iframe>
					</div>
                </div>
            </div>
        </div>
    </div>

	<div class="parent-container" style="display: flex; justify-content: center;">
		<div class="card-body" style="max-width: 80%; margin: auto;">
			<div id="liveBlocks">
			</div>
			<div id="recordBlocks">
			</div>
			<div class="text-right">
 				<button class="btn btn-connected-class" onclick="accessConnectedClass()">
					<i class="bi bi-box-arrow-in-right me-2"></i>&nbsp;&nbsp;&nbsp;Access To Connected Class
				</button>
			</div>
		</div>
	</div>	
</div>

<!-- Realtime Video Warning Modal -->
<div class="modal fade" id="onleLessonWarning" tabindex="-1" role="dialog" aria-labelledby="exampleModalCenterTitle" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered" role="document">
        <div class="modal-content" style="border: 2px solid #ffc107; border-radius: 10px;">
            <div class="modal-header bg-warning" style="display: block;">
				<p style="text-align: center; margin-bottom: 0;"><span style="font-size:18px"><strong>James An College Year <span class="text-danger" id="onlineGrade" name="onlineGrade"></span></strong></span></p>
				<script>
					document.getElementById("onlineGrade").textContent = displayGrade();       
				</script>
			</div>
            <div class="modal-body">
                <div style="text-align: center; margin-bottom: 20px;">
                    <img src="${pageContext.request.contextPath}/image/live.png" style="width: 150px; height: 150px; border-radius: 5%;">
                </div>
                <!-- Add your warning message or content here -->
                <p ><strong>Live Online Class Time:</strong> Every <span id="onlineLessonDay" name="onlineLessonDay"></span>, <span id="onlineLessonStart" name="onlineLessonStart"></span> - <span id="onlineLessonEnd" name="onlineLessonEnd"></span></p>
                <ol>
                    <li>Each set should be completed prior to the 'online class'.</li>
                    <li><span class="text-danger"><strong>Do not turn on your Camera.</strong></span></li>
                    <li>
                        You can ask a question to the teacher if necessary. But please do not bring up irrelevant
                        topics or send dubious and unnecessary content. Anyone who does not respect the online
                        etiquette may be removed from the class at teacher or Head Office's discretion.
                    </li>
                    <li>
                        Please change your name to <strong>&#39;Full Name - JAC Branch&#39;</strong>, e.g. Ava Lee - Braybrook
                        <br>
                        - You can change your name before joining the class or 'rename' yourself after joining.</li>
                        
                    </li>
                    <li>
                        Please note JAC <span class="text-primary"><strong>&#39;Connected Class&#39; </strong></span> is still available for extra coverage.
                    </li>
                </ol>
            </div>
            <input type="hidden" id="realtimeVideoUrl" name="realtimeVideoUrl" value="">
			<input type="hidden" id="realtimeVideoId" name="realtimeVideoId" value="">
            <div class="modal-footer">
				<button type="button" class="btn btn-primary" id="agreeMediaWarning" onclick="displayMedia('realtimeVideoUrl'); startVideoTimer(document.getElementById('realtimeVideoId').value)">I agree</button>
            	<button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
            </div>
        </div>
    </div>
</div>

<!-- Record Video Warning Modal -->
<div class="modal fade" id="recordLessonWarning" tabindex="-1" role="dialog" aria-labelledby="exampleModalCenterTitle" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered" role="document">
        <div class="modal-content" style="border: 2px solid #ffc107; border-radius: 10px;">
            <div class="modal-header bg-warning" style="display: block;">
				<p style="text-align: center; margin-bottom: 0;"><span style="font-size:18px"><strong>James An College Year <span class="text-danger" id="recordGrade" name="recordGrade"></span></strong></span></p>
				<script>
					document.getElementById("recordGrade").textContent = displayGrade();       
				</script>
			</div>
            <div class="modal-body">
                <div style="text-align: center; margin-bottom: 20px;">
                    <img src="${pageContext.request.contextPath}/image/recorded.png" style="width: 150px; height: 150px; border-radius: 5%;">
                </div>
                <!-- Add your warning message or content here -->
                <p><strong>Online Class : </strong><span id="recordLessonDay" name="recordLessonDay"></span></p>
				<p>
					Please note that the recorded video will be accessible for a duration of <span class="text-danger text-uppercase"><strong>A WEEK.</strong></span><br>This availability extends until the commencement of the subsequent online live lesson, ensuring you have adequate time to review the content at your convenience.
				</p>
            </div>
            <input type="hidden" id="recordVideoUrl" name="recordVideoUrl" value="">
			<input type="hidden" id="recordVideoId" name="recordVideoId" value="">
            <div class="modal-footer">
				<button type="button" class="btn btn-primary" id="agreeMediaWarning" onclick="displayMedia('recordVideoUrl'); startVideoTimer(document.getElementById('recordVideoId').value)">I agree</button>
            	<button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
            </div>
        </div>
    </div>
</div>
<h6 class="text-center" style="position: fixed; bottom: 0; width: 100%;">
	2015 - <%=new java.util.Date().getYear() + 1900%>&copy;&nbsp; All rights reserved.&nbsp;&nbsp;
	<div class="copyright-font-color">James An College</div>
</h6>

 <!-- Edit Form Dialogue -->
 <div class="modal fade" id="editStudentModal" tabindex="-1" role="dialog" aria-labelledby="modalEditLabel" aria-hidden="true">	
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-body">
				<section class="fieldset rounded border-primary">
					<header class="text-primary font-weight-bold">Student Information</header>
						<form id="studentEdit">
						<div class="form-row mt-2">
							<div class="col-md-4">
								<label for="editState" class="label-form">State</label>
                                <select class="form-control" id="editState" name="editState" disabled>
								</select>
							</div>
							<div class="col-md-5">
								<label for="editBranch" class="label-form">Branch</label> 
								<select class="form-control" id="editBranch" name="editBranch" disabled>
								</select>
							</div>
							<div class="col-md-3">
								<label for="editRegisterDate" class="label-form">Registration</label> 
								<input type="text" class="form-control" id="editRegisterDate" name="editRegisterDate" readonly>
							</div>
						</div>	
						<div class="form-row mt-2">
							<div class="col-md-3">
								<label for="editId" class="label-form">ID:</label> <input type="text" class="form-control" id="editId" name="editId" readonly>
							</div>
							<div class="col-md-4">
								<label for="editFirstName" class="label-form">First Name:</label> <input type="text" class="form-control" id="editFirstName" name="editFirstName" readonly>
							</div>
							<div class="col-md-3">
								<label for="editLastName" class="label-form">Last Name:</label> <input type="text" class="form-control" id="editLastName" name="editLastName" readonly>
							</div>
							<div class="col-md-2">
								<label for="editGrade" class="label-form">Grade</label> <select class="form-control" id="editGrade" name="editGrade" disabled>
								</select>
							</div>
						</div>
						<div class="form-row mt-2">
							<div class="col-md-3">
								<label for="editGender" class="label-form">Gender</label> <select class="form-control" id="editGender" name="editGender" disabled>
									<option value="male">Male</option>
									<option value="female">Female</option>
								</select>
							</div>
							<div class="col-md-9">
								<label for="editAddress" class="label-form">Address</label> <input type="text" class="form-control" id="editAddress" name="editAddress" readonly>
							</div>
						</div>
					
						<div class="form-row">
							<div class="col-md-12 mt-4">
								<section class="fieldset rounded" style="padding: 10px;">
									<header class="label-form" style="font-size: 0.9rem!important;">Main Contact</header>
								<div class="row">
									<div class="col-md-8">
										<input type="text" class="form-control" id="editContact1" name="editContact1" readonly>
									</div>
									<div class="col-md-4">
										<select class="form-control" id="editRelation1" name="editRelation1" disabled>
											<option value="mother">Mother</option>
											<option value="father">Father</option>
											<option value="sibling">Sibling</option>
											<option value="other">Other</option>
										</select>
									</div>	
								</div>
								<div class="row mt-2">
									<div class="col-md-12">
										<input type="text" class="form-control" id="editEmail1" name="editEmail1" placeholder="Email" readonly>
									</div>
								</div>
								</section>
							</div>
						</div>
						<div class="form-row">
							<div class="col-md-12 mt-4">
								<section class="fieldset rounded" style="padding: 10px;">
									<header class="label-form" style="font-size: 0.9rem!important;">Sub Contact</header>
								<div class="row">
									<div class="col-md-8">
										<input type="text" class="form-control" id="editContact2" name="editContact2" readonly>
									</div>
									<div class="col-md-4">
										<select class="form-control" id="editRelation2" name="editRelation2" disabled>
											<option value="mother">Mother</option>
											<option value="father">Father</option>
											<option value="sibling">Sibling</option>
											<option value="other">Other</option>
										</select>
									</div>	
								</div>
								<div class="row mt-2">
									<div class="col-md-12">
										<input type="text" class="form-control" id="editEmail2" name="editEmail2" readonly>
									</div>
								</div>
								</section>
							</div>
						</div>
						<div class="form-row">
							<div class="col-md-12 mt-4">
								<section class="fieldset rounded" style="padding: 10px; background-color:beige;">
									<header class="label-form" style="font-size: 0.9rem!important;">Password Reset</header>
								<div class="row">
									<div class="col-md-5">
										<label>New Password</label>
									</div>
									<div class="col-md-7">
										<input type="password" class="form-control" id="newPassword" name="newPassword">
									</div>
								</div>
								<div class="row mt-2">
									<div class="col-md-5">
										<label>Confirm Password</label>
									</div>
									<div class="col-md-7">
										<input type="password" class="form-control" id="confirmPassword" name="confirmPassword">
									</div>
								</div>
								</section>
							</div>
						</div>
					</form>					
					<div class="d-flex justify-content-end">
						<button type="submit" class="btn btn-primary" onclick="updatePassword()">Update Password</button>&nbsp;&nbsp;
						<button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
					</div>
				</section>
			</div>
		</div>
	</div>
</div>

<!-- Success Alert -->
<div id="success-alert" class="modal fade">
	<div class="modal-dialog">
		<div class="alert alert-block alert-success alert-dialog-display jae-border-success">
			<i class="fa fa-check-circle fa-2x"></i>&nbsp;&nbsp;<div class="modal-body"></div>
			<a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>
		</div>
	</div>
</div>

<!-- Warning Alert -->
<div id="warning-alert" class="modal fade">
	<div class="modal-dialog">
		<div class="alert alert-block alert-warning alert-dialog-display jae-border-warning">
			<i class="fa fa-exclamation-circle fa-2x"></i>&nbsp;&nbsp;<div class="modal-body"></div>
			<a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>
		</div>
	</div>
</div>

