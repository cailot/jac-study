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
</head>
<body>
	<div class="container-fluid d-flex h-100 flex-column">
		<div class="row justify-content-center align-items-center" >
			<tiles:insertAttribute name="body" />
		</div>
	</div>
	<!-- Loading Spinner -->
	<div class="modal fade" id="loading-spinner" data-backdrop="static" data-keyboard="false" tabindex="-1">
		<div class="modal-dialog modal-dialog-centered">
			<div class="modal-content loading-spinner-content">
				<div class="modal-body text-center p-5">
					<div class="spinner-border text-primary" role="status" style="width: 4rem; height: 4rem;">
						<span class="sr-only">Loading...</span>
					</div>
					<div id="loading-message" class="mt-4 text-primary h4"></div>
				</div>
			</div>
		</div>
	</div>

	<!-- Global Right-Click Protection -->
	<script>
	$(document).ready(function() {
		// Disable right-click context menu globally
		$(document).on('contextmenu', function(e) {
			e.preventDefault();
			return false;
		});
		
		// Disable common developer tool keyboard shortcuts
		$(document).on('keydown', function(e) {
			// Disable F12 (Developer Tools)
			if (e.keyCode === 123) {
				e.preventDefault();
				return false;
			}
			// Disable Ctrl+Shift+I (Developer Tools)
			if (e.ctrlKey && e.shiftKey && e.keyCode === 73) {
				e.preventDefault();
				return false;
			}
			// Disable Ctrl+Shift+C (Inspect Element)
			if (e.ctrlKey && e.shiftKey && e.keyCode === 67) {
				e.preventDefault();
				return false;
			}
			// Disable Ctrl+U (View Source)
			if (e.ctrlKey && e.keyCode === 85) {
				e.preventDefault();
				return false;
			}
			// Disable Ctrl+Shift+J (Console)
			if (e.ctrlKey && e.shiftKey && e.keyCode === 74) {
				e.preventDefault();
				return false;
			}
		});
		
		// Disable text selection and dragging (optional)
		$(document).on('selectstart dragstart', function(e) {
			e.preventDefault();
			return false;
		});
	});
	</script>
</body>
</html>
