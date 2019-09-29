<%@ page language="java" contentType="text/html; charset=utf-8"
         pageEncoding="utf-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <%--  import js in case it is needed  --%>
    <script src="js/jquery-3.3.1.min.js"></script>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>Cloud Upload</title>
</head>
<body>
<form method="post" action="upload" enctype="multipart/form-data">
    Choose a file:
    <input type="file" name="uploadFile" width="200px">
    <input type="submit" value="Upload it!">
</form>
<hr>
<p>Download the file: </p>
<form method="post" action="codeCheck">
    <input type="text" name="userPassword">
    <input type="submit" value="Check your code">
</form>
<hr>
<%-- cancel the request --%>
<form action="cancelExec" method="post">
    <p style="color: crimson">please input your code and your file name with suffix (e.g. hello.jar)</p>
    code:  <input type="text" name="code">
    filename:  <input type="text" name="filename">
    <input type="submit" value="Cancel your request" style="font-size: 20px">
</form>
</body>
</html>
