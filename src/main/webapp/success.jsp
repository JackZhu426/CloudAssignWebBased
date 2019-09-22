<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Success</title>
    <%--  import js in case it is needed  --%>
    <script src="js/jquery-3.3.1.min.js"></script>
</head>
<body>
<p>Successfully uploaded!</p>
<hr/>
<p>${requestScope.resultOfJar}</p>
<hr/>
<a href="index.jsp" style="font-size: 20px">Click to re-upload the file</a>
</body>
</html>
