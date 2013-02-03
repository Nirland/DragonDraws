/**
 * Client application based on websockets.  
 * 
 * @author Nirland
 */

//Websocket connection implementation.
var Connection = {
    socket : null,
    host: socketHost,     
    
    pingInterval: 5*1000, 
    pingTimer : null,
    pingLastTime: null,
        
    init: function(){    	
    	if (!("WebSocket" in window)){
    		alert("WebSockets not supported! Test this application in other browsers, like Chrome, Firefox, Safari!");
    		return;
    	}
    	
    	this.socket = new WebSocket(this.host);
	    
        this.socket.onopen = function(){                    	
            Connection.pingTimer = setInterval(Connection.ping, Connection.pingInterval);
        };

        this.socket.onmessage = function(message){
            data = JSON.parse(message.data);
            Command.handle(data);
	    };

        this.socket.onclose = function(){
            Connection.close();
        };                
    },

    send: function(message){
        this.socket.send(JSON.stringify(message));
    },

    close: function(){        
        if (Game.gameTimer != null){
        	Game.clear();
        }
        clearInterval(Connection.pingTimer);
        document.cookie = 'JSESSIONID' + '=; expires=Thu, 01 Jan 1970 00:00:01 GMT;';
        window.location = window.location.pathname.replace("index.jsp", "login");        
    },
    
    ping: function(){    	
    	Connection.send(Command.build("PING"));
    	Connection.pingLastTime = new Date().getTime();
    }    
    
};

//Handle server commands.
var Command = {
	list : [ "PING", 	          
	         "CANVAS",
	         "CHAT",
	         "GETUSERS",
	         "GETUSERCOUNT",
	         "TIMEOUT",
	         "ENTERQUEUE"
	         ],

	build : function(com, params) {
		if ($.inArray(com, this.list) == -1) {
			return false;
		} else {
			return {
				command : com,
				params : params ? params : null
			};
		}
	},

	handle : function(data) {
		if (this["handle" + data.command]) {
			this["handle" + data.command](data.params);
		} else {
			console.log("Unknown command: " + data.command + 
						" Params: " + JSON.stringify(data.params));
		}		
	},

	handleCANVAS : function(params) {
		if (!Game.testOfTime()){
			return;
		}
		
		var canvas = $("#paint").wPaint.canvas;		
		var settings = params.settings;
		var e = params.e;
		e.pageX += $(canvas.canvas).offset().left;
		e.pageY += $(canvas.canvas).offset().top;
		var event = params.event;
		var tmpsettings = canvas.settings;
		if (settings != undefined) {
			canvas.settings = settings;
		}
		canvas.callFunc(e, canvas, event);
		canvas.settings = tmpsettings;		
	},
	
	handleFINDROOM: function(params) {
		hideUI();
		Game.started = false;
		Game.disableDrawing();
		Game.disableChat();
		Game.clear();		
		Game.uid = params.uid;		
		Connection.send(Command.build("GETUSERCOUNT"));  
	}, 
	
	handleCREATEROOM: function(params) {		
		showUI();		
		Game.roundLength = parseInt(params.round);
		Connection.send(Command.build("GETUSERS"));
	},
	
	handleUSERLIST: function(params) {
		if (!params.users){
			return;
		}
		$("#results").html("");
		$.each(params.users, function(index, value){
			var style = "user";
			if (value.uid.trim() == Game.uid.trim()){
				style = "myself";					
			}			
			$("#results").append('<tr id="' + value.uid + '"  class="' + style +'">' 
					   + '<td class="drawer"></td>' 
					   + '<td class="name">'+ value.uname + '</td>' 
					   + '<td class="points">0</td>'
					   + '</tr>');
		});
		Game.showDrawer();
		Game.started = true;
	},
	
	handleUSERCOUNT: function(params) {
		$("#usercount").html(params.count);
	},
	
	handleCHATMESSAGE: function(params) {
		$("#messages").append('<div class="message">'
							  + '<span class="chattime">['+ params.time+ ']</span> '
							  + '<span class="chatname">' + params.uname + ':</span> '
							  + '<span class="chattext">' + params.message + '</span>'    
							  +' </div>');
		$("#messages").scrollTop($("#messages").get(0).scrollHeight);		
	},
	
	handleSTARTROUND: function(params) {
		Game.showInfo();		
		Game.enableDrawing();
		Game.disableChat();
		Game.showWord(params.word);		
		Game.drawer = Game.uid;
		Game.init();
		if (Game.started){
			Game.showDrawer();
		}		
	},
	
	handleJOINROUND: function(params) {		
		Game.showInfo();		
		Game.disableDrawing();
		Game.enableChat();
		Game.hideWord();
		Game.drawer = params.drawer;		
		Game.init();
		if (Game.started){
			Game.showDrawer();
		}		
	},
	
	handleENDROUND: function(params) {		
		Game.disableDrawing();		
		Game.updateResults(params.results);
	},
	
	handleSKIPROUND: function(params) {	
		//reserved for future
	},
	
	handleENDGAME: function(params) {
		Game.disableDrawing();		
		Game.clear();					
		Game.enableChat();	
		Game.hideInfo();
		Game.drawResults(params.results);
	},
	
	handleCRASHGAME: function(params) {
		Game.disableDrawing();
		Game.clear();		
		Game.disableChat();
		Game.hideInfo();		
	},
	
	handlePONG: function(params) {
		var latency = new Date().getTime() - Connection.pingLastTime;
		$("#latency").html(latency + " ms");
	},

	handleCONNECT: function(params) {
		Connection.send(Command.build("GETUSERCOUNT"));
	},
	
	handleDISCONNECT: function(params) {
		$("#"+params.uid).remove();
		$("#messages").append('<div class="message">'
						  	+ "User " + params.uname 
						  	+ ' leave room</div>');
		$("#messages").scrollTop($("#messages").get(0).scrollHeight);
	}
	
};

//Game and ui logic.
var Game = {
	uid: null,
	roundLength: null,
	drawer: null,
	gameTimer: null,
	seconds: null,
	started: false,
	chatting: false,
	roundStartsAt: null,
	MAX_RESULTS: 5,	
		
	init: function(){		
		this.clear();
		this.seconds = parseInt(this.roundLength);		
		this.gameTimer = setInterval(Game.updateTimer, 1000);
		this.roundStartsAt = new Date().getTime(); 
	},

	clear: function(){
		if (Game.gameTimer != null){
			clearInterval(Game.gameTimer);
			Game.gameTimer = null;
		}
		Game.clearDrawer();	
		Game.clearCanvas();
		if (!Game.started){
			Game.clearChat();
		}		
	},
	
	updateTimer: function(){
		$("#timer").html(Game.seconds);		
		if (Game.seconds <= 0){ 
			Connection.send(Command.build("TIMEOUT"));
			Game.disableDrawing();
			Game.disableChat();			
		}
		Game.seconds--;
	},
		
	updateResults: function(results){
		if (!results){
			return false;
		}		
		$.each(results, function(index, value){
			$("tr[id="+value.uid+"] td.points").html(value.points);			
		});
	},
	
	drawResults: function(results){
		var tmpsettings = $("#paint").wPaint.canvas.settings;
								
		var length = results.length > Game.MAX_RESULTS ? Game.MAX_RESULTS : results.length;  
		
		var ctx = $("#paint").wPaint.canvas.ctx;				
		
		var bgImage = new Image();
		bgImage.src = "images/bgresults.png";
		bgImage.onload = function(){
			ctx.drawImage(bgImage, 0, 0);
			ctx.shadowOffsetX = 3;
			ctx.shadowOffsetY = 3;
			ctx.shadowBlur = 7;	
			    
			var startX = Math.round($($("#paint").wPaint.canvas.canvas).width() * 0.15);
			var startY = Math.round($($("#paint").wPaint.canvas.canvas).height() * 0.35);
			var iterateX = Math.round($($("#paint").wPaint.canvas.canvas).width() * 0.55);;
			var iterateY = Math.round($($("#paint").wPaint.canvas.canvas).height() * 0.1);;
			    
			var hX = Math.round($($("#paint").wPaint.canvas.canvas).width() * 0.40);
			var hY = Math.round($($("#paint").wPaint.canvas.canvas).height() * 0.25);
			ctx.font = "italic 30pt Arial";
			ctx.fillStyle = "#014691";
			ctx.strokeStyle = "#014691";
			ctx.shadowColor = "#3d80e5";
			ctx.fillText("Score", hX, hY);
			    
			ctx.lineWidth = 2;
			ctx.font = "bold 30pt Arial";
			for(var i = 0; i < length; i++){			
				var name = (i + 1) + ". " + results[i].uname;			
				ctx.strokeStyle = "#014691";
				ctx.shadowColor = "#3d80e5";
				ctx.strokeText(name, startX, startY + i * iterateY);
					
				var points =results[i].points;			
				ctx.strokeStyle = "#fd0100";
				ctx.shadowColor = "#7f1436";
				ctx.strokeText(points, startX + iterateX, startY + i * iterateY);
			}
			
			$("#paint").wPaint.canvas.settings = tmpsettings;
			ctx.shadowOffsetX = 0;
		    ctx.shadowOffsetY = 0;
			ctx.shadowBlur = 0;
		};		
	},
	
	showDrawer: function(){						
		$("tr[id="+this.drawer+"] td.drawer").html('<img src="images/brush.png"/>');		
	},
	
	clearDrawer: function(){
		$("td.drawer").html("");
	},
	
	clearCanvas: function(){
		var canvas = $("#paint").wPaint.canvas;
		var position = $(canvas.canvas).position(); 		
		canvas.ctx.clearRect(position.left, position.top, 
				$(canvas.canvas).width() + position.left, $(canvas.canvas).height() + position.top);	
		canvas.ctxTemp.clearRect(position.left, position.top, 
				$(canvas.canvas).width() + position.left, $(canvas.canvas).height() + position.top);
	},
	
	clearChat: function(){
		$("#messages").html("");
	},
	
	showWord: function(word){
		$("#worddesc").show();
		$("#word").html(word);
	},
	
	hideWord: function(){
		$("#worddesc").hide();
		$("#word").html("");
	},
	
	enableChat: function(){		
		this.chatting = true;				
	},
	
	disableChat: function(){
		this.chatting = false;		
	},
	
	enableDrawing: function(){		
		menuPositionFix();
		$("#paint").wPaint.menu.show();		
		$("#paint").wPaint.canvas.disabled = false;
	},

	disableDrawing: function() {
		$("#paint").wPaint.menu.hide();
		$("#paint").wPaint.canvas.draw = false;
		$("#paint").wPaint.canvas.disabled = true;
	},
	
	showInfo: function(){
		$("#gameinfo").show();
		$("#gameend").hide();
	},
	
	hideInfo: function(){
		$("#gameinfo").hide();
		$("#gameend").show();
	},
	
	testOfTime: function(){
		var timeDiff = new Date().getTime() - Game.roundStartsAt;
		return (timeDiff >= 500)? true : false;
	}
};


//Init application and bind functions.
$(document).ready(function() {	
	
	Connection.init();			
	
	$("#paint").wPaint({
		drawDown : function(e, element) {
			Connection.send(Command.build("CANVAS", {
				settings : element.settings,
				event : "Down",
				e : {
					pageX : e.pageX - $(element.canvas).offset().left,
					pageY : e.pageY - $(element.canvas).offset().top
				}
			}));
		},

		drawMove : function(e, element) {
			Connection.send(Command.build("CANVAS", {
				settings : element.settings,
				event : "Move",
				e : {
					pageX : e.pageX - $(element.canvas).offset().left,
					pageY : e.pageY - $(element.canvas).offset().top
				}
			}));						
		},

		drawUp : function(e, element) {
			Connection.send(Command.build("CANVAS", {
				settings : element.settings,
				event : "Up",
				e : {
					pageX : e.pageX - $(element.canvas).offset().left,
					pageY : e.pageY - $(element.canvas).offset().top
				}
			}));
		}
	});
	
	
	$("#messageInput").keypress(function(event){
		if (event.which == 13 && Game.chatting){	
			if ($("#messageInput").val().length > 0){
				Connection.send(Command.build("CHAT", {message: $("#messageInput").val(),
													   timer: Game.seconds}));
				$("#messageInput").val("");				
			}			
		}
	});
		
	$(".newgameBtn").click(function(event){
		event.preventDefault();
		Connection.send(Command.build("ENTERQUEUE"));
	});
	
	$(".saveimageBtn").click(function(event){
		event.preventDefault();
		var imageData = $("#paint").wPaint("image");
		$("#canvasImage").attr('src', imageData);
		$("#savedImage").show();
	});
	
	
	hideUI();	
	Game.disableDrawing();
	Game.disableChat();
});


//some utility functions
function hideUI(){	
	$("#content").hide();
	$("#savedImage").hide();
	$("#dummy").show();
}

function showUI(){
	$("#content").show();	
	$("#dummy").hide();	
}

function menuPositionFix(){
	$($("#paint").wPaint.menu).css("left", $("#paint").offset().left + 5);	
	$($("#paint").wPaint.menu).css("top", $("#paint").offset().top + 5);
}