<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page session="false"%>
<!DOCTYPE html>
<html>
<head>
<head>
<title>CFG Data Transfer</title>
<link href="<c:url value="/css/dtStyle.css" />" rel="stylesheet">
</head>
</head>
<body>
<div id="web_container">
		<jsp:useBean id="controller" class="cf.cpc.uow.controller.DashboardController" scope="request"/>
		
		<div id="web_header">
			<h1>Data Transfer Test Dashboard {${idToken.preferredUsername}}</h1>
			<p> This page serves as the dash board of data transfer component. This page is secured and can only be opened once user is authenticated.</p>
			<hr>
		</div>
		<div id="web_maincontent">
			<form action="logout" method="post">
				<input type="submit" value="Logout (${idToken.preferredUsername})">
			</form>
			<br>
			<h4>${displayMessage}</h4>
			<h3>Access Token:</h3> 
			<p>${accessToken}</p>
			
			<h3>ID Token: </h3> 
			<p>${idTokenString}</p>
			
			<h3>Refresh Token: </h3> 
			<p>${refreshTokenString}</p>
			
			<h3>User Information (API call): </h3> 
			<ol>
				<c:forEach items="${userInfo}" var="info">
					<li>${info}</li>							
				</c:forEach>
			</ol>
			
			<h3>Token Verification (Locally):</h3>
			<ol>
				<c:forEach items="${claimsList}" var="claim">
					<li>${claim}</li>							
				</c:forEach>
			</ol>
			<h3>Token Verification (API call):</h3>
			<ol>
				<c:forEach items="${apiClaimsList}" var="claim">
					<li>${claim}</li>							
				</c:forEach>
			</ol>
			
			<h3> Digital Marketplace token string:</h3> 
			<p> ${dmTokenString}</p>
			
			<h3> Digital Marketplace claims: (Verified locally)</h3>
			<ol>
				<c:forEach items="${dmClaimsList}" var="dmClaim">
					<li>${dmClaim}</li>							
				</c:forEach>
			</ol>
			<h3> Digital Marketplace claims: (Verified using API)</h3>
			<ol>
				<c:forEach items="${dmAPIClaimsList}" var="dmAPIClaim">
					<li>${dmAPIClaim}</li>							
				</c:forEach>
			</ol>
		</div>
	</div>
</body>
</html>