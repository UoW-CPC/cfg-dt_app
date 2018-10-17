<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page session="false"%>
<html>
<head>
<title>CFG Data Transfer</title>
<link href="<c:url value="/css/dtStyle.css" />" rel="stylesheet">
</head>
<body>
	<div id="web_container">
		<div id="web_header">
			<h1>Data Transfer Test Client</h1>
			<p> This page serves as the home page of data transfer component. This page is not secured and therefore can be seen with (or without) user authentication.</p>
			<hr>
		</div>
		<div id="web_maincontent">
			<P></p>
			<a href="/dtApp/dashboard">Login (Or register)</a>
		</div>
	</div>
</body>
</html>
