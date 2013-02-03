<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%	
	if (session.getAttribute("user") == null) {		
		request.getRequestDispatcher("/login").forward(request, response);
		return;
	}
%>
<!DOCTYPE html>
<html>
<head>
<link href="images/dragon.ico" rel="shortcut icon" type="image/x-icon" />

<!-- No Cache -->
<meta http-equiv="cache-control" content="max-age=0" />
<meta http-equiv="cache-control" content="no-cache" />
<meta http-equiv="expires" content="0" />
<meta http-equiv="expires" content="Tue, 01 Jan 1980 1:00:00 GMT" />
<meta http-equiv="pragma" content="no-cache" />

<title>DragonDraws</title>
<!-- jQuery -->
<script type="text/javascript" src="paint/inc/jquery.1.7.1.min.js"></script>
<script type="text/javascript" src="paint/inc/jquery.ui.core.min.js"></script>
<script type="text/javascript" src="paint/inc/jquery.ui.widget.min.js"></script>
<script type="text/javascript" src="paint/inc/jquery.ui.mouse.min.js"></script>
<script type="text/javascript" src="paint/inc/jquery.ui.draggable.min.js"></script>

<!-- wColorPicker -->
<link rel="Stylesheet" type="text/css"
	href="paint/inc/wColorPicker.1.2.min.css" />
<script type="text/javascript" src="paint/inc/wColorPicker.1.2.min.js"></script>

<!-- wPaint -->
<link rel="Stylesheet" type="text/css" href="paint/wPaint.1.3.css" />
<script type="text/javascript" src="paint/wPaint.1.3.js"></script>

<!-- Client -->
<script type="text/javascript">		
		var socketHost = "ws://" +"<%=request.getServerName()+":"+
									  request.getServerPort()+
									  request.getContextPath()%>"+ "/socket";		
</script>
<script type="text/javascript" src="client.js"></script>
<link rel="Stylesheet" type="text/css" href="client.css" />

</head>

<!-- CONTENT -->
<body>
	<!-- MAIN UI -->	
	<div id="content">			
		<div id="uiheader" class="uiblock">
			<div id="gameinfo">
				<span id="timerdesc">Time left:&nbsp;</span><span id="timer"></span>
				<span id="worddesc">Word:&nbsp;</span><span id="word"></span>
			</div>
			<div id="gameend" style="display:none;">				
				<span id="gameresult">Game over!&nbsp;</span>						
			</div>
			<div id="gameui">
				<a href="#" class="newgameBtn">New game</a>
				<a href="#" class="saveimageBtn">Save image</a>
			</div>
			<span style="clear:both;"></span>
		</div>			
		<div id="paint" class="uiblock" style="width: 640px; height: 480px; position: relative;"></div>
		<div id="ui">
			<div id="users" class="uiblock">
				<table id="results"></table>
			</div>
			<div id="chat">
				<div id="messages" class="uiblock"></div>
				<div id="uinput">
					<input id="messageInput" type="text" class="uiblock"/>
				</div>
			</div>
		</div>	
		<br style="clear:both;"/>			
		<div id="ping">
			 Latency:&nbsp;<span id="latency"></span>			 		 			 	 		 	  	
		</div>	
		<div id="savedImage" style="display:none;">
			<img id="canvasImage" src=""/>
		</div>				
	</div>
	
	<!-- MATCHMAKING UI -->
	<div id="dummy">
		<img src="images/textlogo.png"/>
		<img src="images/dragonlogo.png"/>
		<div style="padding-top: 15px;">
			<div id="preloader" class="uiblock">
				<h1>Searching other players...</h1>
				<img src="images/preloader.gif"/>				
				<p>Players online: <span id="usercount">1</span></p>
			</div>
		</div>
	</div>	
</body>
</html>