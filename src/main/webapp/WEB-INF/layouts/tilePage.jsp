<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<html lang="en-US">
<head>
<title><tiles:getAsString name="title" /></title>
<!-- Favicon -->
<link rel="icon" href="${pageContext.request.contextPath}/image/favicon.ico" type="image/x-icon">

<link href="${pageContext.request.contextPath}/css/jae.css" rel="stylesheet" type="text/css"/>

<!-- Bootstrap CSS -->
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/bootstrap-4.3.1.min.css"/>	
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/jquery-ui-1.12.1.css"/>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/jquery-ui.theme.min.css">

<script type="text/javascript" src="${pageContext.request.contextPath}/js/jae.js"></script>
<script src="${pageContext.request.contextPath}/js/jquery-3.6.0.min.js"></script>
<script src="${pageContext.request.contextPath}/js/jquery-ui-1.13.2.min.js"></script>
<script src="${pageContext.request.contextPath}/js/bootstrap-4.3.1.min.js"></script>
<script src="${pageContext.request.contextPath}/js/bootstrap.bundle-4.5.3.min.js"></script>	
<!-- Bootstrap Icons -->
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/bootstrap-icons.min.css"/>
<!-- Google Icons -->
<link rel="stylesheet" href="https://fonts.googleapis.com/icon?family=Material+Icons">
<script>
	$(function() {
		// Enable Bootstrap tooltips
		$('[data-toggle="tooltip"]').tooltip();
	});
</script>
</head>
<body>
	<div class="container-fluid d-flex h-100 flex-column">
		<div class="row">
			<tiles:insertAttribute name="menu" />
		</div>
		<div class="row justify-content-center">
			<tiles:insertAttribute name="body" />
		</div>
		<footer class="mt-auto">
			<div class="row" style="padding: 15px 20px;">
				<div class="col-12 text-center" >
					2015 - <%=new java.util.Date().getYear() + 1900%> &copy; All rights reserved.
					<div class="copyright-font-color">James An College</div> 
				</div>
			</div>
		</footer>
	</div>
</body>
</html>