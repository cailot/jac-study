<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<html>
<head>
<title><tiles:getAsString name="title" /></title>
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
<style>
    body {
        display: flex;
        flex-direction: column;
        min-height: 100vh;
        background-color: #f8f9fa;
        margin: 0;
    }
    .header, .footer {
        background-color: #2d398e;
        color: white;
        padding: 1rem;
        text-align: center;
        position: fixed;
        width: 100%;
        z-index: 1000;
    }
    .header {
        top: 0;
    }
    .footer {
        bottom: 0;
    }
    .content {
        flex: 1;
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 1rem;
        margin-top: 60px; /* Height of the header */
        margin-bottom: 60px; /* Height of the footer */
    }
</style>
</head>
<body>
    <div class="header">
        <img src="${pageContext.request.contextPath}/image/logo.png" style="filter: brightness(0) invert(1);width:45px;" class="mr-3"/><span class="h4">James An College Victoria</span>
    </div>

    <div class="content">
        <tiles:insertAttribute name="body" />
    </div>

    <div class="footer">
        James An College Victoria Head Office: 16A 77-79 Ashley St. Braybrook VIC 3019 Australia<br>
        <i class="bi bi-telephone"></i> (03) 9362 0051 &nbsp;&nbsp;&nbsp;<i class="bi bi-envelope"></i> assessment@jamesancollegevic.com.au<br>
        2015 - <%= new java.util.Date().getYear() + 1900 %>&copy;&nbsp; All rights reserved.&nbsp;&nbsp;
    </div>
</body>
</html>