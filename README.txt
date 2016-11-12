Program:
This is a Multi-Client/Single-Server Messaging program. Users may set up conversations between each other and have them logged, files may also be sent.
The database of users can be seen below.
Follow "Usage" to run.

Compile and Run:

To compile, use the javac *.java command in each folder.

First, run the server and provide it a port number to use.
( ex: java MessengerServer 3333 )

Next, run any number of clients and provide them the IP of the server, and the port number of the server.
( ex: java MessengerClient 127.0.0.1 3333 )


--- --- ---


Usage (SERVER):

- Ensure that the MySQL Database is running in the background and is ready to connect.
- Start the Server Program*
- Move on to starting up Clients

* If the Program responds with a Database Init Failure, the Server will not work as intended.


--- --- ---


Usage (CLIENT):

Log into the client using one of the UserName - Password combinations listed below:

	Users( UserName, Password, Read Level, Write Level )
	"Fred",  "CalgaryFlames", Read = 0, Write = 0
	"Amy",   "1992",          Read = 0, Write = 0
	"Aaron", "123",           Read = 1, Write = 1
	"Lisa",  "music",         Read = 1, Write = 1
	"Adam",  "456789",        Read = 1, Write = 1

	Read/Write details are used later.
	
	
--- ---


Commands:

	/help                      - Shows this help menu
	/exit                      - Exits the program
	/logout                    - Logs the current user out 
	/chat    <USERNAME ..>     - Create a conversation with the specified Users
	/join    <CONVOID>         - Attempts to join the specified Conversation
	/send    <USERID FILEPATH> - Sends the given file to the given user
	/add     <USERID>          - Adds a friend to your friends list
	/remove  <USERID>          - Removes a friend from your friends list
	/list                      - Lists all online friends

	
--- ---	
	
	
Chat:

	- Creates a private conversation with Users listed
	- If Users in the list do not exist, an error will be displayed
	- If some Users in the list exist and some do not, a Conversation with the correct Users will be made
	- If the Users exist but are offline, the Conversation is created (Other Users can use /join)
	- Every User will have sufficient Read/Write levels in a private conversation
	
	
--- ---


Joining:

	Can be used to join a public channel (Listed below)
	Can be used to join a disconnected private conversation*
	
	If the User does not have sufficient Read Access  - An error will be displayed
	If the User does not have sufficient Write Access - Conversation will open with no Input Field
	
	Channels( Channel ID, Read Level, Write Level ):
	0, Read = 0, Write = 0
	1, Read = 0, Write = 1
	2, Read = 1, Write = 2
		
		
	* Requires knowledge of the Conversations ID
	
	
--- ---


Add / Remove:

	- Cannot Add duplicates
	- Cannot Remove users that aren't on the friends list


--- ---


Sending Files:

	- Files Sent will be saved on the Server for each User (Server\Files\USERNAME)
	- Sent Files must exist in the Client Directory
	- Files received will be saved in the Client\Files Directory
	- A test file has been supplied in each Client program "FileToSend.txt"


--- ---


Example:

TERMINAL 2 : Start Server program ( ex: java MessengerServer 3333 ) 
TERMINAL 0 : Start Client 0 ( ex: java MessengerClient 127.0.0.1 3333 )
TERMINAL 1 : Start Client 1 ( ex: java MessengerClient 127.0.0.1 3333 )
TERMINAL 0 : Log client in (Username: Aaron, Password: 123)
TERMINAL 1 : Log client in (Username: Amy, Password: 1992)
TERMINAL 1 : Type /chat Aaron on Client 1 to start a Conversation with Client 0
TERMINAL 0 : Client 0 has Conversation window opened
TERMINAL 1 : Client 1 has Conversation window opened
-- Conversation Occurs --
TERMINAL 0 : Client 0 exits the chat.
TERMINAL 1 : Client 1 tabs back to the console window.
TERMINAL 0 : Type /help to view command list
TERMINAL 0 : Type /exit on Client 0 to end the test
TERMINAL 1 : Type /exit on Client 1 to end the test
TERMINAL 2 : Use ctrl + c on the server to close
