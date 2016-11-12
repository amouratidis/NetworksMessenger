import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.file.*;
import java.nio.ByteBuffer;

// Handles all sending of packets through the (currently) single TCP connection
public class MessageManager
{
    private Socket           m_TCPSocket;
	private DataOutputStream m_TCPOutBuffer;


	MessageManager( Socket socket )
	{
		// Initialize a server socket and a client socket for the server (TCP)
		try
		{
			m_TCPSocket    = socket;
			m_TCPOutBuffer = new DataOutputStream( m_TCPSocket.getOutputStream() );
		}
		catch( Exception e )
		{
			System.out.println( "Message Manager setup failure" );
		}
	}

	
	// Internally used to build and send the packets
	private void send( byte[] bData, int iHeader )
	{
		// Before sending the Message. Send the header, then the data length
		try
		{	
			// First the Message info
			m_TCPOutBuffer.writeInt( bData.length );
			m_TCPOutBuffer.writeInt( iHeader );

			// Now the data
			m_TCPOutBuffer.write( bData, 0, bData.length );
		}
		catch( Exception e )
		{
			System.out.println( "PacketType " + iHeader + " - Failed to send" );
		}
	}
	
	
	// Internally used to combine a set of arrays (Order is important!)
	private byte[] combineArrays( byte[][] bArrays, int iLength )
	{
		// Create our data array
		byte[] bData = new byte[iLength];
		
		// Use a Byte Buffer to create the final array
		ByteBuffer dataBuffer = ByteBuffer.wrap( bData );
		
		// For each array in our set, add it onto the current end of bData
		for( int iNextArray = 0; iNextArray < bArrays.length; iNextArray++ )
			dataBuffer.put( bArrays[iNextArray] );
			
		return bData;
	}
	
	
	// Sends a User-to-User Message with a conversationID included
	public void sendMessage( String sMessage, int iConversationID )
	{		
		// Put the conversationID into a Data array
		byte[] bConvoID = ByteBuffer.allocate(4).putInt( iConversationID ).array();
	
		// Put the Message into a Data Array
		byte[] bMessage = sMessage.getBytes();
		
		// Create the set of Arrays
		byte[][] bArrays = { bConvoID, bMessage };
				
		// Create our data array
		int iDataLen = bConvoID.length + bMessage.length;
		byte[] bData = combineArrays( bArrays, iDataLen );
		
		// Finally, send the data
		send( bData, HeaderType.HEADER_MESSAGE );
	}
	
	
	// Sends a file to a given user
	public void sendFile( int iToUser, String sFilename, byte[] bFileData )
	{
		// Create the byte arrays
		byte[] bUser       = ByteBuffer.allocate(4).putInt( iToUser ).array();
		byte[] bName       = sFilename.getBytes();
		byte[] bNameLength = ByteBuffer.allocate(4).putInt( bName.length ).array();
	
		// Create the set of Arrays
		byte[][] bArrays = { bUser, bNameLength, bName, bFileData };
				
		// Create our data array
		int iDataLen = bUser.length + bNameLength.length + bName.length + bFileData.length;
		byte[] bData = combineArrays( bArrays, iDataLen );
		
		// Finally, send the data
		send( bData, HeaderType.HEADER_FILE );
	}
	
	
	// Sends the given Login information to the server
	public void sendLogin( String sUsername, String sPassword )
	{
		// Separate the Username and Password with a space - Neither string will have a space in it
		byte[] bData = ( sUsername + " " + sPassword ).getBytes();
		
		// Finally, send the data
		send( bData, HeaderType.HEADER_LOGIN );	
	}

    
    // Sends a conversation request with a given user
    public void sendConversation( String[] sUsers )
    {
		// Convert the String array into a single string - Comma Separated
		String sCommaUsers = User.getUsername();
		
		// Loop through the array to break it apart
		for( int iUser = 0; iUser < sUsers.length; iUser++ )
			sCommaUsers = sCommaUsers + "," + sUsers[iUser];
			
        // Generate the data array
        byte[] bData = sCommaUsers.getBytes();

		// Finally, send the data
		send( bData, HeaderType.HEADER_CONVO );	
    }
	
	
	// Sends a Join request with a given ConversationID
    public void sendJoin( int iConvoID, int iAccessLevel )
    {
		// Put the data into separate byte arrays then combine
		byte[] bConvoID = ByteBuffer.allocate(4).putInt( iConvoID ).array();
		byte[] bAccess  = ByteBuffer.allocate(4).putInt( iAccessLevel ).array();
		
		// Create the set of Arrays
		byte[][] bArrays = { bConvoID, bAccess };
				
		// Create our data array
		int iDataLen = bConvoID.length + bAccess.length;
		byte[] bData = combineArrays( bArrays, iDataLen );
	
		// Finally, send the data
		send( bData, HeaderType.HEADER_JOIN );	
    }
	
	
	// Sends a Join request with a given ConversationID
    public void sendStatus( int iStatusType, int iUserID )
    {
		// Put the data into separate byte arrays then combine
		byte[] bStatus = ByteBuffer.allocate(4).putInt( iStatusType ).array();
		byte[] bUserID = ByteBuffer.allocate(4).putInt( iUserID ).array();
		
		// Create the set of Arrays
		byte[][] bArrays = { bStatus, bUserID };
				
		// Create our data array
		int iDataLen = bStatus.length + bUserID.length;
		byte[] bData = combineArrays( bArrays, iDataLen );
	
		// Finally, send the data
		send( bData, HeaderType.HEADER_STATUS );	
    }
	
	
	// Sends a Log request with a given ConversationID
    public void sendLog( int iConvoID )
    {
		// Put the conversation into a byte array
		byte[] bData = ByteBuffer.allocate(4).putInt( iConvoID ).array();
	
		// Finally, send the data
		send( bData, HeaderType.HEADER_LOG );	
    }
}