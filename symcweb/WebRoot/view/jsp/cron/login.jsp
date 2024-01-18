<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<!--[if lt IE 7]> <html class="lt-ie9 lt-ie8 lt-ie7" lang="en"> <![endif]-->
<!--[if IE 7]> <html class="lt-ie9 lt-ie8" lang="en"> <![endif]-->
<!--[if IE 8]> <html class="lt-ie9" lang="en"> <![endif]-->
<!--[if gt IE 8]><!-->
<html lang="en">
<!--<![endif]-->
<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
<title>Admin Service</title>
<link rel="stylesheet" href="css/style.css">
<!--[if lt IE 9]><script src="//html5shim.googlecode.com/svn/trunk/html5.js"></script><![endif]-->
</head>
<body>
	<form method="post" action="checkLogin.do" class="login">
		<p>
			<label for="login">ID :</label> <input type="text" name="id"
				id="id" value="">
		</p>

		<p>
			<label for="password">Password:</label> <input type="password"
				name="passwd" id="passwd" value="">
		</p>

		<p class="login-submit">
			<input type="hidden" name="method" value="login">
			<button type="submit" class="login-button">Login</button>
		</p>		
	</form>

	<section class="about">
		<p class="about-author">
			&copy; 2012&ndash;2013 SYMC New PLM
		</p>
	</section>
</body>
</html>
