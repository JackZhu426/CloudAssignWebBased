<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Download</title>
</head>
<body>
<p style="font-size: 20px; color: crimson">${requestScope.status}</p>
<hr>
<a href="download?filename=${requestScope.userPasscode}_result.txt" style="font-size: 20px">Download the file</a>
<hr>
<a href="index.jsp" style="font-size: 20px">Click to re-upload the file</a>
</body>
</html>
