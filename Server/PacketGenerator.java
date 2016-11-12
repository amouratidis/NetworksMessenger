import java.nio.*;
import java.util.*;
import java.util.concurrent.Semaphore;

public class PacketGenerator
{
	private static Semaphore m_ConvoSemaphore = new Semaphore( 1, true );
	private static List<Conversation> m_ConversationList = new ArrayList<Conversation>();

	private static int m_NextConvo = 4;

	// Public Packet Generation method
	public static SendablePacket formatPacket( Packet receivedPacket )
	{
		SendablePacket returnPacket = null;
	
		// Route the data to the correct function
		switch( receivedPacket.getHeader() )
		{
			// Packet is a Message
			case ( HeaderType.HEADER_MESSAGE ):
				returnPacket = createMessagePacket( receivedPacket );			
				break;
				
			// Packet is part of a file
			case ( HeaderType.HEADER_FILE ):
				returnPacket = createFilePacket( receivedPacket );		
				break;
				
			// Packet is a login request
			case ( HeaderType.HEADER_LOGIN ):
				returnPacket = createLoginPacket( receivedPacket );			
				break;
				
			// Packet is a Conversation request
			case ( HeaderType.HEADER_CONVO ):
				returnPacket = createConvoPacket( receivedPacket );			
				break;
				
			// Packet is a Join request
			case ( HeaderType.HEADER_JOIN ):
				returnPacket = createJoinPacket( receivedPacket );			
				break;
				
			// Packet is a Status message
			case ( HeaderType.HEADER_STATUS ):
				returnPacket = createStatusPacket( receivedPacket );			
				break;
				
			// Packet is a Log request
			case ( HeaderType.HEADER_LOG ):
				returnPacket = createLogPacket( receivedPacket );			
				break;
				
			// Unknown packet, return null
			default:
				break;			
		}
		
		return returnPacket;
	}
	
	
	// Internal Message Packet Generator
	private static SendablePacket createMessagePacket( Packet messagePacket )
	{
		// Get the list of conversations
		List<Conversation> convoList = getConversations();
	
		// Get the Message Data
		byte[] bData    = messagePacket.getData();
		byte[] bMessage = Arrays.copyOfRange( bData, 4, bData.length );
	
		// Get the Conversation ID
		int iConversationID = (int)( ( ( bData[0] & 0xFF ) << 24 ) | ( ( bData[1] & 0xFF ) << 16 ) | ( ( bData[2] & 0xFF ) << 8 ) | ( bData[3] & 0xFF ) );	
		
		// Now redistribute to the users involved
		List<Integer> iUserList = new ArrayList<Integer>();
		
		// Loop through the set of conversations active
		for( int iConversation = 0; iConversation < convoList.size(); iConversation++ )
		{
			// Find the matching conversation
			if( convoList.get( iConversation ).getID() == iConversationID )
			{
				iUserList = convoList.get( iConversation ).getUsers();
				
				String sLogFile = convoList.get( iConversation ).getLogFile();
				
				// Pass the message data to the logger
				Logger.addToLog( sLogFile, bMessage );
			}
		}
		
		// Create and return the new packet
		return new SendablePacket( messagePacket.getSender(), messagePacket.getHeader(), bData, iUserList );
	}
	
	
    // Internal File Packet Generator
	private static SendablePacket createFilePacket( Packet filePacket )
	{
		// Get the list of conversations
		List<Conversation> convoList = getConversations();
		
		// Get the Message Data
		byte[] bData = filePacket.getData();
	
		// Get the User ID
		int iUserID = (int)( ( ( bData[0] & 0xFF ) << 24 ) | ( ( bData[1] & 0xFF ) << 16 ) | ( ( bData[2] & 0xFF ) << 8 ) | ( bData[3] & 0xFF ) );	
		
		// Get the File Data
		int iNameLen     = (int)( ( ( bData[4] & 0xFF ) << 24 ) | ( ( bData[5] & 0xFF ) << 16 ) | ( ( bData[6] & 0xFF ) << 8 ) | ( bData[7] & 0xFF ) );      
		String sFileName = new String( Arrays.copyOfRange( bData, 8, 8 + iNameLen ) );
		byte[] bFileData = Arrays.copyOfRange( bData, 8 + iNameLen, bData.length );
		
		// Locally save files for both users
		FileManager.receiveFile( filePacket.getSender(), iUserID, sFileName, bFileData );
		
		// Now redistribute to the users involved
		List<Integer> iUserList = new ArrayList<Integer>();
		
		// Add the user to the list
		iUserList.add( iUserID );
		
		// Create and return the new packet
		return new SendablePacket( filePacket.getSender(), filePacket.getHeader(), bData, iUserList );
	}
	
	
	// Internal Login Packet Generator
	private static SendablePacket createLoginPacket( Packet loginPacket )
	{
		// Convert the Data into a string
		String sData = new String( loginPacket.getData() );
		
		// Parse the UserName and Password
		String sUsername = sData.substring( 0, sData.indexOf( ' ' ) );
		String sPassword = sData.substring( sData.indexOf( ' ' ) + 1, sData.length() );
		
		// Create the list of users that will receive the packet
		List<Integer> iUserList = new ArrayList<Integer>();
		
		// Attempt to get the user from the DB
		User newUser = DatabaseAccess.getUser( sUsername, sPassword );
	
		int iUserID     = newUser.getID();
		int iReadLevel  = newUser.getReadLevel();
		int iWriteLevel = newUser.getWriteLevel();
		
		// Format the byte data
		byte[] bUserID   = ByteBuffer.allocate(4).putInt( iUserID ).array();
		byte[] bReadLevel  = ByteBuffer.allocate(4).putInt( iReadLevel ).array();
		byte[] bWriteLevel = ByteBuffer.allocate(4).putInt( iWriteLevel ).array();
		
		// Create the Access byte data
		byte[] bAccess = new byte[ bReadLevel.length + bWriteLevel.length ];		
		System.arraycopy( bReadLevel,  0, bAccess, 0,                 bReadLevel.length  );
		System.arraycopy( bWriteLevel, 0, bAccess, bReadLevel.length, bWriteLevel.length );
		
		// Join the Conversation and Access data together
		byte[] bResponse = new byte[ bUserID.length + bAccess.length ];
		System.arraycopy( bUserID, 0, bResponse, 0,              bUserID.length );
		System.arraycopy( bAccess, 0, bResponse, bUserID.length, bAccess.length  );
		
		
		iUserList.add( loginPacket.getSender() );
		
		// Create the new login packet
		return new SendablePacket( loginPacket.getSender(), loginPacket.getHeader(), bResponse, iUserList );
	}
	
	
	// Internal Conversation Packet Generator
	private static SendablePacket createConvoPacket( Packet conversationPacket )
	{
		// Get the Users data
		byte[] bData = conversationPacket.getData();
		
		// Find the Number of users
		String sCommaUsers     = new String( bData );
		List<String> sUserList = new ArrayList<String>();
		boolean bCommaFound    = true;
		
		while( bCommaFound )
		{
			// Check if we have another Comma
			bCommaFound = ( sCommaUsers.indexOf( ',' ) != -1 );
			String sNextUser = "";
			
			if( bCommaFound )
			{
				// Parse the User
				sNextUser = sCommaUsers.substring( 0, sCommaUsers.indexOf( ',' ) ).trim();
				
				// Remove the User from the String
				sCommaUsers = sCommaUsers.substring( sCommaUsers.indexOf( ',' ) + 1, sCommaUsers.length() );
			}
			else
			{
				sNextUser = sCommaUsers.trim();
			}
			
			// Add the Next User and continue
			sUserList.add( sNextUser );
		}
		
		// Convert the String array into an array of IDs
		List<Integer> iUserList = new ArrayList<Integer>();
		
		int iUsersFound = 0;
		for( int iUser = 0; iUser < sUserList.size(); iUser++ )
		{
			// Get the UserID from the DB
			int iUserID = DatabaseAccess.getUserID( sUserList.get( iUser ) );
		
			// Check that we found a user
			if( iUserID != 0 )
			{
				iUserList.add( iUserID );
				iUsersFound++;
			}
		}
			
		if( iUsersFound <= 1 )
		{
			byte[] bConvID = ByteBuffer.allocate(4).putInt( -1 ).array();
			
			return new SendablePacket( conversationPacket.getSender(), conversationPacket.getHeader(), bConvID, iUserList );	
		}	
		
		// Using the Users found - Create/Find the log file
		String sLogName = Logger.createLogFile( iUserList );
		
		// Give the conversation a new ID
		int iConversationID = m_NextConvo;
		m_NextConvo++;
		
		// Add the conversation - ( -1 = Nobody can join, 0 = Anybody can talk )
		addConversation( iConversationID, iUserList, Constants.NOBODY, Constants.ANYBODY, sLogName );
		
		// Format the byte data
		byte[] bConvID   = ByteBuffer.allocate(4).putInt( iConversationID ).array();
	/*	byte[] bAccess   = ByteBuffer.allocate(4).putInt( iAccessLevel ).array();
		byte[] bConversation = new byte[ bConvID.length + bAccess.length ];
		
		System.arraycopy( bConvID, 0, bConversation, 0,              bConvID.length );
		System.arraycopy( bAccess, 0, bConversation, bConvID.length, bAccess.length ); */
		
		return new SendablePacket( conversationPacket.getSender(), conversationPacket.getHeader(), bConvID, iUserList );	
	}
	
	
	// Internal Conversation Packet Generator
	private static SendablePacket createJoinPacket( Packet joinPacket )
	{
		// Get the data
		byte[] bData = joinPacket.getData();
	
		// Get the Conversation ID
		int iConversationID = (int)( ( ( bData[0] & 0xFF ) << 24 ) | ( ( bData[1] & 0xFF ) << 16 ) | ( ( bData[2] & 0xFF ) << 8 ) | ( bData[3] & 0xFF ) );	
		int iAccessLevel    = (int)( ( ( bData[4] & 0xFF ) << 24 ) | ( ( bData[5] & 0xFF ) << 16 ) | ( ( bData[6] & 0xFF ) << 8 ) | ( bData[7] & 0xFF ) );	
	
		// Get the list of conversations
		List<Conversation> convoList = getConversations();
	
		// Set the default Read/Write Level
		int iReadLevel  = -1;
		int iWriteLevel = -1;
	
		// Check if the user has access
		for( int iConversation = 0; iConversation < convoList.size(); iConversation++ )
		{
			// Find the matching conversation
			if( convoList.get( iConversation ).getID() == iConversationID )
			{
				// Get the Access Levels
				iReadLevel  = convoList.get( iConversation ).getReadLevel();
				iWriteLevel = convoList.get( iConversation ).getWriteLevel();
				
				// Add the users if they have high enough access
				if( iAccessLevel >= iReadLevel )
					convoList.get( iConversation ).addUser( joinPacket.getSender() );
					
				// If the user was already in the conversation - Edit the read/write levels
				if( convoList.get( iConversation ).hasUser( joinPacket.getSender() ) )
				{
					iReadLevel  = 0;
					iWriteLevel = 0;
				}
					
				break;
			}
		}
	
		// Format the User List
		List<Integer> iUserList = new ArrayList<Integer>();
		iUserList.add( joinPacket.getSender() );
			
		// Format the byte data
		byte[] bConvoID    = ByteBuffer.allocate(4).putInt( iConversationID ).array();
		byte[] bReadLevel  = ByteBuffer.allocate(4).putInt( iReadLevel ).array();
		byte[] bWriteLevel = ByteBuffer.allocate(4).putInt( iWriteLevel ).array();
		
		// Create the Access byte data
		byte[] bAccess = new byte[ bReadLevel.length + bWriteLevel.length ];		
		System.arraycopy( bReadLevel,  0, bAccess, 0,                 bReadLevel.length  );
		System.arraycopy( bWriteLevel, 0, bAccess, bReadLevel.length, bWriteLevel.length );
		
		// Join the Conversation and Access data together
		byte[] bJoin = new byte[ bConvoID.length + bAccess.length ];
		System.arraycopy( bConvoID, 0, bJoin, 0,               bConvoID.length );
		System.arraycopy( bAccess,  0, bJoin, bConvoID.length, bAccess.length  );
		
		// Create the new SendAble Packet and return it
		return new SendablePacket( joinPacket.getSender(), joinPacket.getHeader(), bJoin, iUserList );	
	}
	
	
	// Internal Conversation Packet Generator
	private static SendablePacket createStatusPacket( Packet statusPacket )
	{
		// Get the data
		byte[] bData = statusPacket.getData();
	
		// Get the Status Type and the User ID
		int iStatus = (int)( ( ( bData[0] & 0xFF ) << 24 ) | ( ( bData[1] & 0xFF ) << 16 ) | ( ( bData[2] & 0xFF ) << 8 ) | ( bData[3] & 0xFF ) );	
		int iUserID = (int)( ( ( bData[4] & 0xFF ) << 24 ) | ( ( bData[5] & 0xFF ) << 16 ) | ( ( bData[6] & 0xFF ) << 8 ) | ( bData[7] & 0xFF ) );	
	
		System.out.println( "Status Received: " + iStatus );
	
		// Create an empty list of users
		List<Integer> iUserList = new ArrayList<Integer>();
		
		// Create an empty status string to be filled
		String sStatus = "";
	
		// Get the list of users
		if( ( iStatus == HeaderType.STATUS_ONLINE ) || ( iStatus == HeaderType.STATUS_OFFLINE ) )
		{
			// First, update our list of users online/offline
			if( iStatus == HeaderType.STATUS_ONLINE )
				OnlineUsers.addUser( iUserID );
			else
				OnlineUsers.removeUser( iUserID );
		
			// This status message needs to be sent to all friends
			List<User> friendsList = DatabaseAccess.getFriendsList( iUserID );
			
			// For each friend, add them to the User list
			for( int iFriend = 0; iFriend < friendsList.size(); iFriend++ )
				iUserList.add( friendsList.get( iFriend ).getID() );
			
			// Set the status to the User logging in/out
			User userChanging = DatabaseAccess.getUser( iUserID );
			
			// Set the status to the changing users name
			sStatus = userChanging.getName();
		}
		else if( ( iStatus == HeaderType.STATUS_ADD ) || ( iStatus == HeaderType.STATUS_REMOVE ) )
		{	
			if( iStatus == HeaderType.STATUS_ADD )
			{
				// First get the friend and user information
				User user   = DatabaseAccess.getUser( statusPacket.getSender() );
				User friend = DatabaseAccess.getUser( iUserID );
				
				// Now attempt to add the user - Return confirmation on success
				if( DatabaseAccess.addUser( user.getID(), user.getName(), friend.getID(), friend.getName() ) )
					iUserList.add( statusPacket.getSender() );
			}
			else
			{
				// Now attempt to remove the user - Return confirmation on success
				if( DatabaseAccess.removeUser( statusPacket.getSender(), iUserID ) )
					iUserList.add( statusPacket.getSender() );
			}
			
			// Get the friend to use in the status update
			User friend = DatabaseAccess.getUser( iUserID );
			
			// Set the status to the User being added/removed
			sStatus = friend.getName();
		}
		else
		{
			// Return to the sender
			iUserList.add( statusPacket.getSender() );
			
			// First, get the list of online users
			List<Integer> onlineUsers = OnlineUsers.getUsers();	
			
			// Then get the set of friends
			List<User> friendsOf = DatabaseAccess.getFriendsOf( iUserID );

			for( int iFriend = 0; iFriend < friendsOf.size(); iFriend++ )
			{
				// Check if the friend is online
				if( onlineUsers.indexOf( friendsOf.get( iFriend ).getID() ) != -1 )
					sStatus = sStatus + friendsOf.get( iFriend ).getName() + "\n";			
			}
		}
			
		// Reformat the packet
		byte[] bType   = ByteBuffer.allocate(4).putInt( iStatus ).array();
		byte[] bName   = sStatus.getBytes();
		byte[] bStatus = new byte[ bType.length + bName.length];

		// Join the Type and User data together
		System.arraycopy( bType, 0, bStatus, 0,            bType.length );
		System.arraycopy( bName, 0, bStatus, bType.length, bName.length );
		
		// Create the new SendAble Packet and return it
		return new SendablePacket( statusPacket.getSender(), statusPacket.getHeader(), bStatus, iUserList );	
	}


	// Internal Log packet generator
	private static SendablePacket createLogPacket( Packet logPacket )
	{
		// Get the data
		byte[] bData = logPacket.getData();
	
		// Get the Conversation ID
		int iConversationID = (int)( ( ( bData[0] & 0xFF ) << 24 ) | ( ( bData[1] & 0xFF ) << 16 ) | ( ( bData[2] & 0xFF ) << 8 ) | ( bData[3] & 0xFF ) );	
		
		// Get the list of conversations
		List<Conversation> convoList = getConversations();
	
		// Create an empty log
		String sLog = "";
	
		// Find the correct conversation
		for( int iConversation = 0; iConversation < convoList.size(); iConversation++ )
		{
			// Find the matching conversation
			if( convoList.get( iConversation ).getID() == iConversationID )
			{
				// Get the Log Name
				String sLogFile = convoList.get( iConversation ).getLogFile();
				
				// Extract the log from the file
				sLog = Logger.getLogFile( sLogFile );
				
				break;
			}
		}
	
		// Format the User List
		List<Integer> iUserList = new ArrayList<Integer>();
		iUserList.add( logPacket.getSender() );
			
		// Format the byte data
		byte[] bConvoID = ByteBuffer.allocate(4).putInt( iConversationID ).array();
		byte[] bLog     = sLog.getBytes();
		
		// Create the byte data
		byte[] bPacket = new byte[ bConvoID.length + bLog.length ];		
		System.arraycopy( bConvoID,  0, bPacket, 0,               bConvoID.length  );
		System.arraycopy( bLog,      0, bPacket, bConvoID.length, bLog.length );
		
		// Create the new SendAble Packet and return it
		return new SendablePacket( logPacket.getSender(), logPacket.getHeader(), bPacket, iUserList );	
	}

		
	/* -- Semaphore based function for the ConversationList, these are thread safe -- */
	
	
	// Used to create and add a new conversation to the ConversationList
	public static void addConversation( int iConversationID, List<Integer> iUsers, int iReadLevel, int iWriteLevel, String sLogFile )
	{
		try
		{
			m_ConvoSemaphore.acquire();
			
			try
			{
				Conversation newConvo = new Conversation( iConversationID, iUsers, iReadLevel, iWriteLevel, sLogFile );
				m_ConversationList.add( newConvo );
			} 
			finally
			{
				m_ConvoSemaphore.release();
			}
		} 
		catch(InterruptedException ie)
		{
			// TODO - Report Interruption
		}
	}
	
	
	// Thread Safe - Returns the list of Conversations
	private static List<Conversation> getConversations()
	{
		try
		{
			m_ConvoSemaphore.acquire();
			
			try
			{
				return m_ConversationList;
			} 
			finally
			{
				m_ConvoSemaphore.release();
			}
		} 
		catch(InterruptedException ie)
		{
			// TODO - Report Interruption
		}
		
		return null;
	}	
}