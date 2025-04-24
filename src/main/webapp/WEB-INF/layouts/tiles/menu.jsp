<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="grade" value="" />
<sec:authorize access="isAuthenticated()">
	<sec:authentication var="role" property='principal.authorities'/>
	<sec:authentication var="id" property="principal.username"/>
	<sec:authentication var="firstName" property="principal.firstName"/>
	<sec:authentication var="lastName" property="principal.lastName"/>
	<c:set var="grade" value="${role}" />
	<script>
		var role = '${role}';
		var numericGrade = role.replace(/[\[\]]/g, ''); // replace '[' & ']' with an empty string
		var studentId = '${id}';
		var firstName = '${firstName}';
		var lastName = '${lastName}';
		// Determine if numericGrade is a number
		var isStudent = !isNaN(+numericGrade);
		// console.log('numericGrade : ' + numericGrade); // Logs true if numericGrade is a number, otherwise false
	</script>
</sec:authorize>

<style>
  	.dropdown-toggle::after {
		display: none;
	}
	.dropdown:hover .dropdown-menu {
		display: block;
	}
	.nav-link-white {
		color: white !important;
	}
	/* Fixed styles for menu visibility */
	.navbar {
		width: 100%;
		display: flex;
		align-items: center;
	}
	.navbar_logo {
		padding: 0 20px;
	}
	.navbar-collapse {
		display: flex !important;
		flex: 1;
	}
	.navbar-nav {
		display: flex;
		flex-direction: row;
		align-items: center;
		justify-content: center;
		width: 100%;
	}
	.nav-item {
		padding: 0 15px;
		white-space: nowrap;
	}
	.nav-link {
		display: flex;
		align-items: center;
		color: white !important;
	}
	.custom-icon {
		margin-right: 5px;
	}
	.navbar_icon {
		margin-left: auto;
		display: flex;
		align-items: center;
	}
	.h4 {
		margin: 0;
		font-size: 1.2rem;
	}
</style>

<script>
$(function() {
	// initialise state list when loading
	listState('#editState');
    listBranch('#editBranch');
	listGrade('#editGrade');
});

/////////////////////////////////////////////////////////////////////////////////////////////////////////
// 			Display grade
////////////////////////////////////////////////////////////////////////////////////////////////////////
function displayGrade() {
	if(role === '[Administrator]') return 'Administrator';
	if(role === '[Staff]') return 'Staff';
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
</script>

<div class="container-fluid jae-header pt-3 pb-3">
    <nav class="navbar navbar-expand-lg navbar-light">
        <div class="navbar_logo">
            <a href="${pageContext.request.contextPath}/connected/lesson">
                <img src="${pageContext.request.contextPath}/image/logo-cc.png" title="JAC Connected Class" style="filter: brightness(0) invert(1);width:50px;" >
            </a>
        </div>
        <div class="navbar-collapse">
            <ul class="navbar-nav">
				<!-- Homework -->
                <c:choose>
                    <c:when test="${grade == '[1]' || grade == '[2]' || grade == '[3]' || grade == '[4]' || grade == '[5]' || grade == '[6]' || grade == '[7]' || grade == '[8]' || grade == '[9]' || grade == '[19]'}">
                        <li class="nav-item dropdown">
                            <a class="nav-link dropdown-toggle" href="#" id="homeworkDropdown" role="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                                <i class="bi bi-pencil-square custom-icon"></i>
                                <span class="h4">Homework</span>
                            </a>
                            <div class="dropdown-menu" aria-labelledby="homeworkDropdown">
                                <a class="dropdown-item" href="${pageContext.request.contextPath}/connected/engHomework">English Homework</a>
                                <a class="dropdown-item" href="${pageContext.request.contextPath}/connected/mathHomework">Mathematics Homework</a>
                                <c:if test="${grade == '[2]' || grade == '[3]' || grade == '[4]' || grade == '[5]'}">
                                    <a class="dropdown-item" href="${pageContext.request.contextPath}/connected/writeHomework">Writing Homework</a>
                                </c:if>
                                <a class="dropdown-item" href="${pageContext.request.contextPath}/connected/shortAnswer">Short Answer</a>
                            </div>
                        </li>
                    </c:when>
                    <c:otherwise>
                        <li class="nav-item">    
                            <i class="bi bi-pencil-square custom-icon"></i>
                            <span class="h4 text-white">Homework</span>
                        </li>
                    </c:otherwise>
                </c:choose>
                <!-- Extra Materials -->
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/connected/extraMaterial">
                        <i class="bi bi-pencil-square custom-icon"></i>
                        <span class="h4">Extra Materials</span>
                    </a>
                </li>
				<!-- Practice -->                
				<c:choose>
					<c:when test="${grade == '[1]' || grade == '[2]' || grade == '[3]' || grade == '[4]' || grade == '[5]' || grade == '[6]' || grade == '[7]' || grade == '[8]' || grade == '[9]' || grade == '[11]' || grade == '[12]'}">
						<li class="nav-item dropdown">    
							<a class="nav-link dropdown-toggle" href="#" id="practiceDropdown" role="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
								<i class="bi bi-pencil-square custom-icon"></i>
								<span class="h4">Practice</span>
							</a>
							<div class="dropdown-menu" aria-labelledby="practiceDropdown">
								<!-- Mega Practice submenu -->
								<c:if test="${grade == '[1]' ||grade == '[2]' || grade == '[3]' || grade == '[4]' || grade == '[5]'}">
									<a class="dropdown-item" href="${pageContext.request.contextPath}/connected/practice/mega">Mega Practice</a>
								</c:if>
								<!-- Revision submenu -->
								<c:if test="${grade == '[6]' ||grade == '[7]' || grade == '[8]' || grade == '[9]'}">
									<a class="dropdown-item" href="${pageContext.request.contextPath}/connected/practice/revision">Revision Practice</a>
								</c:if>
								<!-- Naplan submenu -->
								<c:if test="${grade == '[2]' || grade == '[4]' || grade == '[6]' || grade == '[8]'}">
									<a class="dropdown-item" href="${pageContext.request.contextPath}/connected/practice/naplan">NAPLAN</a>
								</c:if>
								<!-- Acer & Edu submenu -->
								<c:if test="${grade == '[11]' ||grade == '[12]'}">
									<a class="dropdown-item" href="${pageContext.request.contextPath}/connected/practice/acer">ACER Practice</a>
									<a class="dropdown-item" href="${pageContext.request.contextPath}/connected/practice/edu">EDU Practice</a>
								</c:if>
							</div>
						</li>
					</c:when>
					<c:otherwise>
						<li class="nav-item">    
							<i class="bi bi-pencil-square custom-icon"></i>
							<span class="h4 text-white">Practice</span>
						</li>
					</c:otherwise>
				</c:choose>
                <!-- Test -->
                <li class="nav-item dropdown">
                    <a class="nav-link dropdown-toggle" href="#" id="testDropdown" role="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                        <i class="bi bi-pencil-square custom-icon"></i>
                        <span class="h4">Test</span>
                    </a>
                    <div class="dropdown-menu" aria-labelledby="testDropdown">
                        <!-- Mega Test submenu -->
                        <c:if test="${grade == '[1]' ||grade == '[2]' || grade == '[3]' || grade == '[4]' || grade == '[5]'}">
                            <a class="dropdown-item" href="${pageContext.request.contextPath}/connected/test/mega">Mega Test</a>
                        </c:if>
                        <c:if test="${grade == '[1]' ||grade == '[2]' || grade == '[3]' || grade == '[4]' || grade == '[5]'}">
                            <a class="dropdown-item" href="${pageContext.request.contextPath}/connected/test/megaExplanation">Mega Test Explanation</a>
                        </c:if>
                        <!-- Revision submenu -->
                        <c:if test="${grade == '[6]' ||grade == '[7]' || grade == '[8]' || grade == '[9]'}">
                            <a class="dropdown-item" href="${pageContext.request.contextPath}/connected/test/revision">Revision Test</a>
                        </c:if>
                        <c:if test="${grade == '[6]' ||grade == '[7]' || grade == '[8]' || grade == '[9]'}">
                            <a class="dropdown-item" href="${pageContext.request.contextPath}/connected/test/revisionExplanation">Revision Test Explanation</a>
                        </c:if>
                        <!-- Edu submenu : TT6, JMSS -->
                        <c:if test="${grade == '[11]' || grade == '[19]'}">
                            <a class="dropdown-item" href="${pageContext.request.contextPath}/connected/test/edu">EDU Test</a>
                        </c:if>
                        <!-- Acer submenu : TT6, TT8 -->
                        <c:if test="${grade == '[11]' ||grade == '[12]'}">
                            <a class="dropdown-item" href="${pageContext.request.contextPath}/connected/test/acer">ACER Test</a>
                        </c:if>
                        <!-- Test Result submenu -->
                        <!-- OMR Result -->
                        <a id="recentResultLink" class="dropdown-item" href="${pageContext.request.contextPath}/result/download-pdf/" download="TestResult.pdf">Recent Result</a>
                        <script>
                            document.addEventListener("DOMContentLoaded", function () {
                                // Update the href attribute of the link
                                var recentResultLink = document.getElementById("recentResultLink");
                                recentResultLink.href += studentId;
                                console.log(recentResultLink.href);
                            });
                        </script>
                    </div>
                </li>
                <!-- Jac-eLearning -->
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/online/lesson">
                        <i class="bi bi-pencil-square custom-icon"></i>
                        <span class="h4">Jac-eLearning</span>
                    </a>
                </li>
            </ul>
        </div>
        <ul class="navbar_icon" style="margin: 0; padding: 0;">
            <sec:authorize access="isAuthenticated()">
                <div class="card-body jae-background-color text-right" style="display: flex; align-items: center; justify-content: space-between; padding: 0;">
                    <div class="text-center">
                        <span class="card-text text-warning font-weight-bold font-italic h5" style="cursor: pointer;" id="studentName" onclick="if(isStudent) { clearPassword(); retrieveStudentInfo(); }">${firstName} ${lastName}</span>
                        <span class="text-white">&nbsp;&nbsp;[</span>
                        <span class="card-text h5" id="studentGrade" name="studentGrade" style="color: white;"></span>
                        <span class="text-white;">]&nbsp;&nbsp;</span>
                        <script>document.getElementById("studentGrade").textContent = displayGrade();</script>
                    </div>
                    <form:form action="${pageContext.request.contextPath}/connected/logout" method="POST" id="logout" style="margin: 0; display: flex; align-items: center;">
                        <button class="btn" style="padding: 0 20px 0 0;"><i class="bi bi-power custom-icon text-warning" title="Log Out"></i></button>
                    </form:form>
                </div>
            </sec:authorize> 
        </ul>
    </nav>
</div>
 
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

