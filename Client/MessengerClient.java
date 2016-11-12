import java.io.*;
import java.net.*;
import java.util.*; 

public class MessengerClient implements ReceiverCallBack
{ 
	private Socket m_TCPSocket;
	private MessageManager m_MessageManager;
	private MessageReceiver m_MessageReceiver;
    private ConversationManager m_ConversationManager;
    private FileManager m_FileManager;
    private int m_UserID;
	private int m_AccessLevel;
	private int m_WriteLevel;
	private boolean m_LoggedIn;
	private boolean m_LogInReceived;
		   	   
	final String COMMAND_HELP    = "/help";
	final String COMMAND_EXIT    = "/exit";
	final String COMMAND_LOGOUT  = "/logout";
	final String COMMAND_CHAT    = "/chat";
	final String COMMAND_JOIN    = "/join";
	final String COMMAND_SEND    = "/send";
	final String COMMAND_ADD     = "/add";
	final String COMMAND_REMOVE  = "/remove";
	final String COMMAND_FRIENDS = "/list";
	
	
	final String[] COMMAND_HELP_LIST =
	{
		"/help                      - Shows this help menu", 
		"/exit                      - Exits the program", 
		"/logout                    - Logs the current user out", 
		"/chat    <USERNAME ..>     - Create a conversation with the specified Users", 
		"/join    <CONVOID>         - Attempts to join the specified Conversation",
		"/send    <USERID FILEPATH> - Sends the given file to the given user",
		"/add     <USERID>          - Adds a friend to your friends list",
		"/remove  <USERID>          - Removes a friend from your friends list",
		"/list                      - Shows friends list"
	};

	
    public static void main( String args[] ) throws Exception 
    { 
		// Make sure we get the correct amount of arguments
        if (args.length != 2)
        {
            System.out.println("Usage: MessengerClient <Server IP> <Server Port>");
            System.exit(1);
        }

        // Now create the class
        MessengerClient client = new MessengerClient( args );
    }
	
	
    // Class Constructor
    MessengerClient( String[] args ) throws Exception
    {
        // Initialize a client socket connection to the server
        m_TCPSocket = new Socket(args[0], Integer.parseInt(args[1])); 

		// Set User Variables
		m_UserID      = -1;
		m_AccessLevel = -1;
		
		// Initialize Managers and Handlers
		m_MessageManager      = new MessageManager( m_TCPSocket );
		m_MessageReceiver     = new MessageReceiver( m_TCPSocket, this );
        m_ConversationManager = new ConversationManager( m_MessageManager );
		m_FileManager         = new FileManager( m_MessageManager );
		m_LogInReceived       = false;
		
        // Initialize user input stream 
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in)); 

		// Initialize loop variables
		String sInputLine = ""; 
		
		// Loop Forever
		while( true )
		{
			// Wait for the user to login
			m_LoggedIn = Login( inFromUser );
					
			// Get user input and send to the server - Loop until logged out
			while( m_LoggedIn )
			{
				System.out.print("Messenger Client Console: ");
				sInputLine = inFromUser.readLine(); 
				
				// Parse and Check the Command
				List<String> sArgs = ParseCommand( sInputLine );
				CheckCommand( sArgs );
			}
		}
    }


	// Loops waiting for the User to login to the system
	private boolean Login( BufferedReader inFromUser )
	{
		try
		{
			// Force the user to login first
			while( true )
			{	
				// Get Username details from the User
				String sUsername = "";
				System.out.print("Username: ");
				sUsername = inFromUser.readLine(); 
				
				// Get Password details from the User
				String sPassword = "";
				System.out.print("Password: ");
				sPassword = inFromUser.readLine(); 
					
				// Send the login information to the server and wait for a response
				m_MessageManager.sendLogin( sUsername, sPassword );
					
				// Wait until the receiver gets a login packet
				while( !m_LogInReceived )
					Thread.sleep( 50 );
				
				// Reset the login check variable
				m_LogInReceived = false;
					
				// If login is good, break out of this loop, otherwise show error message
				if( m_UserID > 0 )
				{
					// Set the new user
					User.setUser( m_UserID, sUsername, m_AccessLevel, m_WriteLevel );
					
					break;
				}
				else
				{
					System.out.println( "Username or Password was incorrect" );
				}
			}
		}
		catch( Exception e )
		{
			// Something failed, just return false
			return false;
		}
		
		// Send a login success packet
		m_MessageManager.sendStatus( HeaderType.STATUS_ONLINE, User.getUserID() );
		
		// Fall through means success
		return true;
	}
	
	
	// Parses the Command into a list of arguments
	private List<String> ParseCommand( String sMessage )
	{
		List<String> sArgs = new ArrayList<String>();
		
		// Parse the command
		sMessage = sMessage.trim();

		// Loop to catch the command and all arguments
		while( sMessage.indexOf( ' ' ) != -1 )
		{	
			String sArg = "";
		
			sArg = sMessage.substring( 0, sMessage.indexOf( ' ' ) );
	
			sArgs.add( sArg );
			
			sMessage = sMessage.substring( sMessage.indexOf( ' ' ), sMessage.length() );
			sMessage = sMessage.trim();
		}
		
		// Add the last argument/command
		sArgs.add( sMessage );
		
		return sArgs;
	}
	
	
	// Checks the Command of the Message, do different things based on the type
	private void CheckCommand( List<String> sArgs )
	{
		// Parse the Command out of the Args
		String sCommand = sArgs.get( 0 );
	
		switch( sCommand )
		{
			// Show the help menu
			case( COMMAND_HELP ):
			{
				// Loop through each Help line and print it
				for( int iCounter = 0; iCounter < COMMAND_HELP_LIST.length; iCounter++ )
					System.out.println( COMMAND_HELP_LIST[iCounter] );
			
				break;
			}
			// Exit the Program
			case( COMMAND_EXIT ):
			{
				System.out.println( "Exiting Program" );
				
				// Close the socket
				try
				{
					m_TCPSocket.close();
				}
				catch( Exception e )
				{
					// TODO: Handle this?
				}
				
				System.exit(0);
				
				break;
			}
			// Log the current User out
			case( COMMAND_LOGOUT ):
			{
				// First close all conversations
				m_ConversationManager.closeAll();
			
				// Set our logged-in variable to false
				m_LoggedIn = false;
				
				// Send a logout packet to the server
				m_MessageManager.sendStatus( HeaderType.STATUS_OFFLINE, User.getUserID() );
				
				// Reset the User
				User.resetUser();
	
				break;
			}
			case( COMMAND_CHAT ):
			{
				// Create a new conversation window with these people
				if( sArgs.size() >= 2 )
				{
					// Get the Users as a string
					String[] sUsers = new String[sArgs.size() - 1];
				
					// Loop through the arguments, get each user
					for( int iUser = 1; iUser < sArgs.size(); iUser++ )
						sUsers[iUser - 1] = sArgs.get( iUser );
						
					// Send the Conversation Request
					m_MessageManager.sendConversation( sUsers );
				}
				else
				{
					String sError = "Usage: /chat <USERNAME ..>";
					displayMessage( sError );
				}
				
				break;
			}	
			// Attempt to Join the given conversation
			case( COMMAND_JOIN ):
			{
				if( sArgs.size() == 2 )
				{
					// Parse the ConversationID
					// TODO: Do this Safely
					int iConvoID = Integer.parseInt( sArgs.get( 1 ) );
				
					// Send the Join request
					m_MessageManager.sendJoin( iConvoID, m_AccessLevel );
				}
				else
				{
					String sError = "Usage: /join <CONVOID>";
					displayMessage( sError );
				}
				
				break;
			}
			// Attempt to send a given file to a given user
			case( COMMAND_SEND ):
			{
				if( sArgs.size() == 3 )
				{
					// Parse the UserID
					// TODO: Do this Safely
					int iUserID = Integer.parseInt( sArgs.get( 1 ) );
				
					// Send the file
					m_FileManager.sendFile( iUserID, sArgs.get( 2 ) );
				}
				else
				{
					String sError = "Usage: /send <USERID FILEPATH>";
					displayMessage( sError );
				}
				
				break;
			}
			// Attempt to add a given user
			case( COMMAND_ADD ):
			{
				if( sArgs.size() == 2 )
				{
					// Parse the UserID
					// TODO: Do this Safely
					int iFriendID = Integer.parseInt( sArgs.get( 1 ) );
				
					// Send a add request to the server
					m_MessageManager.sendStatus( HeaderType.STATUS_ADD, iFriendID );
				}
				else
				{
					String sError = "Usage: /add <USERID>";
					displayMessage( sError );
				}

				break;
			}
			// Attempt to remove a given friend
			case( COMMAND_REMOVE ):
			{
				if( sArgs.size() == 2 )
				{
					// Parse the UserID
					// TODO: Do this Safely
					int iFriendID = Integer.parseInt( sArgs.get( 1 ) );
				
					// Send a remove request to the server
					m_MessageManager.sendStatus( HeaderType.STATUS_REMOVE, iFriendID );
				}
				else
				{
					String sError = "Usage: /remove <USERID>";
					displayMessage( sError );
				}
				
				break;
			}
			// Print off the current friends Online
			case( COMMAND_FRIENDS ):
			{
				// Send a friends list request to the server
				m_MessageManager.sendStatus( HeaderType.STATUS_FRIENDS, User.getUserID() );
			
				break;
			}	
			// No matching command found
			default:
			{
				String sError = "Unknown Command: <" + sArgs.get(0) + ">\nFor a list of commands, use </help>";
				displayMessage( sError );
			}
		}
	}
	
	
	// -- Callback Functions -- //


	// Received a Message - Pass to the correct conversation
	public void receiveMessage( int iConvoID, String sMessage )
	{
        // Pass the Message to the Conversation Manager        
        m_ConversationManager.addMessage( iConvoID, sMessage );
	}


	// Received a Login Packet - Check Status
	public void receiveLogin( int iUserID, int iAccessLevel, int iWriteLevel )
	{
		// Check if we're already logged in
		if( m_LoggedIn )
			return;
	
		// Set the Login Status
		m_UserID      = iUserID;
		m_AccessLevel = iAccessLevel;
		m_WriteLevel  = iWriteLevel;
		
		// Let the Main Thread know we've received a packet
		m_LogInReceived = true;
	}


	// Received a Conversation Request - Generate the new window
	public void receiveConvo( int iConvoID, String[] sUsers )
	{
        if( iConvoID == -1 )
		{
			String sError = "No Users Found";
			displayMessage( sError );
			
			return;
		}
		
        // Tell the Conversation Manager to create a new Conversation        
        m_ConversationManager.createConversation( iConvoID, 0 );	
	}
	
	
	// Received a Join request - Check Access then create the new window
	public void receiveJoin( int iConvoID, int iReadLevel, int iWriteLevel )
	{
		// Check that we're allowed to join
		if( iReadLevel == -1 )
		{
			String sError = "Access to Conversation " + iConvoID + " is restricted.";
			displayMessage( sError );
			
			return;
		}
			
		// If we have a high enough Reading Level, create the window
		if( User.getAccessLevel() < iReadLevel )
		{
			String sError = "Access to Conversation " + iConvoID + " is restricted.";
			displayMessage( sError );
			
			return;	
		}
		
		// Fall through means create the window
		m_ConversationManager.createConversation( iConvoID, iWriteLevel );	
	}
	
	
	// Received a file, save it if possible
	public void receiveFile( String sFilename, byte[] bFileData )
	{
		// Redirect to the FileManager to save	
		String sFileMessage = "New File Received: " + m_FileManager.saveFile( sFilename, bFileData );
		
		// Tell the user a file has been received
		displayMessage( sFileMessage );
	}
	
	
	// A User related status message has been received, display appropriately
	public void receiveStatus( int iStatusType, String sStatus )
	{
		// Only show messages if we're logged in
		if( m_LoggedIn )
		{
			// Display a message based on the Status Type
			switch( iStatusType )
			{
				// The given user has logged on
				case( HeaderType.STATUS_ONLINE ):
					displayMessage( sStatus + " has logged on." );
				
					break;
					
				// The given user has logged off
				case( HeaderType.STATUS_OFFLINE ):
					displayMessage( sStatus + " has logged off." );
				
					break;
					
				// Friend added to friends list
				case( HeaderType.STATUS_ADD ):
					displayMessage( sStatus + " has been added to friends list." );
					
					break;
					
				// Friend removed from friends list
				case( HeaderType.STATUS_REMOVE ):
					displayMessage( sStatus + " has been removed from friends list." );
				
					break;
				
				case( HeaderType.STATUS_FRIENDS ):
					displayMessage( "Friends Online:\n" + sStatus );
					
					break;
			}
		}
	}
	
	
	// Received a Log - Pass to the correct conversation
	public void receiveLog( int iConvoID, String sLog )
	{
        // Check that we have a log to given
		if( sLog.compareTo( "" ) == 0 )
			return;
		
		// Pass the Message to the Conversation Manager        
        m_ConversationManager.addLog( iConvoID, sLog );
	}
	
	
	// Displays a message in the chat log
	private void displayMessage( String sMessage )
	{
		// First display the message
		System.out.println( sMessage );
		
		// SetUp the next line
		System.out.print( "Messenger Client Console: " );
	}
} 
