<h1>DragonDraws</h1>
<h2>Description</h2>
DragonDraws is a multiplayer drawing game based on websockets.<br>
Gameplay demostration is shown in this <a href="http://youtube.com">video</a>.<br>

<h2>Downloads</h2>
Download <a href="https://docs.google.com/file/d/0B8J0LS_KpoJweUJ4RmxIVzJTYWM/edit">WAR file</a>.<br>
Download completed <a href="https://docs.google.com/file/d/0B8J0LS_KpoJwMkZmT0FDdUFSa28/edit">Jetty</a> with integrated application. 

<h2>Technical information</h2>
Server side is written on Java. It runs on Jetty 8.x and uses Jetty 8 Websocket API.<br>
Client side is written on JavaScript and uses Jquery and modified <a href="http://www.websanova.com/plugins/websanova/paint">Websanova paint plugin</a> based on HTML5 canvas.

<h2>Server requirments and running</h2>
JRE 6+<br>
Jetty 8.x<br>
To run application in Eclipse IDE, you can use Jetty WTP Adaptor plugin.<br>
If you downloaded completed Jetty package then:<br>
1. Unpack archive<br>
2. Port 8080 must be free(you can config this in etc/jetty.xml). Use <b>java -jar start.jar</b> to run server.<br>
3. Open browser and go to <a href="http://localhost:8080">http://localhost:8080</a><br>

<h2>Browser support</h2>
Browser must support websockets(RFC 6455) and HTML5 canvas.<br>
For correct application work is recommended to use latest versions of Chrome, Firefox, Safari, Opera.

<h2>Important sources</h2>
<a href="https://github.com/Nirland/DragonDraws/blob/master/DragonDraws/src/org/nirland/websocket/servlet/SocketServlet.java">SocketServlet.java</a> contains application settings as WebInitParams.<br>
<a href="https://github.com/Nirland/DragonDraws/blob/master/DragonDraws/WebContent/words.txt">words.txt</a> is a simple text file which contains words base. 
