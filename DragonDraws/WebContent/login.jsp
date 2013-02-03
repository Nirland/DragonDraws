<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<link href="images/dragon.ico" rel="shortcut icon" type="image/x-icon" />
<title>DragonDraws</title>
<style>
div#content{		
	width: 705px;
	margin: 0 auto;
	text-align: center;
}

div#uiform{
	padding: 5px;
	font-family: Arial;
	font-size: 14pt;
	font-weight: bold;
	color: #014691;
	width: 500px;
	margin: 0 auto;
}

#uname{
	padding: 3px;
	font-family: Times New Roman;
	font-size: 12pt;	
	background-color: #a8dff7;
	color: #014691;			
 	height: 22px;
	width: 150px;	
	margin: 0px;
}

#uname:focus {
	background-color: #d1defa;
	border: 2px solid #fcf0bf;
}

.uiblock{
	background-color: #ffffff; 
	border: solid 2px #014691;	
	border-radius: 10px;
	-webkit-border-radius: 10px; 
 	-moz-border-radius: 10px;
}

.button {	
	background-color:#d1defa;
	-moz-border-radius:5px;
	-webkit-border-radius:5px;
	border-radius:5px;
	border:1px solid #014691;
	display:inline-block;
	color:#014691;
	font-family:Arial;
	font-size:12pt;
	font-weight:bold;
	padding:5px 20px;
	text-decoration:none;
	text-shadow:1px 1px 0px #ffffff;
}
.button:hover {
	background-color:#1e62d0;
	color:#ffffff;
	border:1px solid #014691;
	text-shadow:1px 1px 0px #014691;
}

</style>
</head>
<body>
	<div id="content">
		<img src="images/textlogo.png"/>
		<img src="images/dragonlogo.png"/>
		<div style="padding-top: 15px;">
			<div id="uiform" class="uiblock">
				<form action="<%=application.getContextPath()+"/login"%>" method="POST">
					<span id="label">Enter your name</span>
					<input type="text" id="uname" name="uname" class="uiblock"/>
					<input type="submit" id="submitted" name="submitted" class="button" value="Let's draw!"/> 
				</form>
			</div>
		</div>
	</div>		
</body>
</html>