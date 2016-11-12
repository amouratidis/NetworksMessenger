import java.util.*;
import java.util.concurrent.Semaphore;

public class MessageRouter
{
	private List<ConnectionSender> m_SenderList;
	private Semaphore m_SenderSemaphore;
	
	
	// Class Constructor
    MessageRouter()
	{
		m_SenderList       = new ArrayList<ConnectionSender>();
		m_SenderSemaphore  = new Semaphore( 1, true );
    }
	
	
	// Re-Routes the given message to the correct senders
	public void route( SendablePacket packet )
	{
		// Get the List of Senders
		List<ConnectionSender> senderList = getSenders();
		
		// Get the List of Users
		List<Integer> iUserList = packet.getUsers();
	
		System.out.println( "\nSending to " + iUserList.size() + "Users" );
	
		// For each user in our list, check the list of senders for the matching one
		for( int iUser = 0; iUser < iUserList.size(); iUser++ )
		{
			for( int iSender = 0; iSender < senderList.size(); iSender++ )
			{				
				// If the user is matched with a sender, add to the message queue
				if( senderList.get( iSender ).getUserID() == iUserList.get( iUser ) )
				{
					senderList.get( iSender ).addToQueue( packet );
					
					System.out.println( "\nSending to Sender: " + iSender );
					System.out.println( "Sending to User:   " + iUserList.get( iUser ) );
				}
			}
		}
	}
	
	
	public void updateUserID( int iConnectionID, int iUserID )
	{
		// Get the List of Senders
		List<ConnectionSender> senderList = getSenders();
		
		// Look for the matching Sender
		for( int iSender = 0; iSender < senderList.size(); iSender++ )
		{
			// Update the UserID on the matching Sender
			if( senderList.get( iSender ).getConnectionID() == iConnectionID )
				senderList.get( iSender ).setUserID( iUserID );
		}
	}
	
	/*
	// Used internally when a chat message is received 
	private void routeMessage( Packet messagePacket )
	{
		// Get the Message Data
		byte[] bData = messagePacket.getData();
	
		// Get the Conversation ID
		int iConversationID = (int)( ( ( bData[0] & 0xFF ) << 24 ) | ( ( bData[1] & 0xFF ) << 16 ) | ( ( bData[2] & 0xFF ) << 8 ) | ( bData[3] & 0xFF ) );	
		
		// Get the Conversation List
		List<Conversation> convoList = getConversations();
		
		// Now redistribute to the users involved
		int[] iUserList = {};
		
		// Loop through the set of conversations active
		for( int iConversation = 0; iConversation < convoList.size(); iConversation++ )
		{
			// Find the matching conversation
			if( convoList.get( iConversation ).getID() == iConversationID )
				iUserList = convoList.get( iConversation ).getUsers();
		}
		
		// Exit if we found no matching conversation
		if( iUserList.length == 0 )
			return;
		
		// For each user in our list, check the list of senders for the matching one
		for( int iUser = 0; iUser < iUserList.length; iUser++ )
		{
			for( int iSender = 0; iSender < m_SenderList.size(); iSender++ )
			{
				// Make sure we don't return the message to the sender
				if( m_SenderList.get( iSender ).getConnectionID() == messagePacket.getSender() )
					continue;
				
				// If the user is matched with a sender, add to the message queue
				if( m_SenderList.get( iSender ).getUserID() == iUserList[iUser] )
					m_SenderList.get( iSender ).addToQueue( messagePacket );
			}
		}
	}


    // Used internally when a conversation request is received
    private void routeConvo( Packet conversationPacket )
    {
		// Get the list of users
		byte[] bData    = conversationPacket.getData();
		int iUserCount  = ( bData.length / 4 );
		int[] iUserList = new int[iUserCount];
		
		// For each user in our data, add to our array
		for( int iUser = 0; iUser < iUserCount; iUser++ )
			iUserList[iUser] = (int)( ( ( bData[(iUser * 4 ) + 0] & 0xFF ) << 24 ) | ( ( bData[(iUser * 4 ) + 1] & 0xFF ) << 16 ) | ( ( bData[(iUser * 4 ) + 2] & 0xFF ) << 8 ) | ( bData[(iUser * 4 ) + 3] & 0xFF ) );
		
		// Exit if we found no users
		if( iUserList.length == 0 )
			return;
		
		// TODO: Get the ConversationID and access level from the DB
		int iConversationID = 0;
		int iAccessLevel    = 0;
		
		// Add the conversation
		addConversation( iConversationID, iUserList, iAccessLevel );
		
		// Format the Conversation ID
		byte[] bConversation = ByteBuffer.allocate(4).putInt( iConversationID ).array();

		byte[] bConvID   = ByteBuffer.allocate(4).putInt( iConversationID ).array();
		byte[] bAccess   = ByteBuffer.allocate(4).putInt( iAccessLevel ).array();
		byte[] bConversation = new byte[ bConvID.length + bAccess.length ];
		
		System.arraycopy( bConvID, 0, bConversation, 0,              bConvID.length );
		System.arraycopy( bAccess, 0, bConversation, bConvID.length, bAccess.length );
		
		// Form the new Conversation packet
		Packet newpacket = new Packet( conversationPacket.getSender(), HeaderType.HEADER_CONVO, bConversation );
			
		// For each user in our list, check the list of senders for the matching one
		for( int iUser = 0; iUser < iUserList.length; iUser++ )
		{
			for( int iSender = 0; iSender < m_SenderList.size(); iSender++ )
			{
				// If the user is matched with a sender, add to the message queue
				if( m_SenderList.get( iSender ).getUserID() == iUserList[iUser] )
					m_SenderList.get( iSender ).addToQueue( newpacket );
			}
		}
    }
	
	
	// Used internally when a login request is received
    private void routeLogin( Packet loginPacket )
    {
    	// Get the list of users (Use the function to guarantee thread safety)
		List<ConnectionSender> senders = getSenders();

        // Loop through the set of senders and return the login packet
		for( int iSender = 0; iSender < senders.size(); iSender++ )
		{		
			// Check that the user still exists
			if( senders.get( iSender ) == null )
				continue;
				
			// Only send a login packet back to the user who sent it
			if( senders.get( iSender ).getConnectionID() != loginPacket.getSender() )
				continue;
				
			// First, tell this sender the new user that has logged index
			byte[] bData = loginPacket.getData();
			senders.get( iSender ).setUserID( (int)( ( ( bData[0] & 0xFF ) << 24 ) | ( ( bData[1] & 0xFF ) << 16 ) | ( ( bData[2] & 0xFF ) << 8 ) | ( bData[3] & 0xFF ) ) );
				
			// Fall through means we're good to send to this user
			senders.get( iSender ).addToQueue( loginPacket );
		}	
    }	*/
	
	/* -- Semaphore based function for the ConnectionList, these are thread safe -- */
	
	
	// Thread Safe - Returns the list of Senders - Used internally only
	private List<ConnectionSender> getSenders()
	{
		try
		{
			m_SenderSemaphore.acquire();
			
			try
			{
				return m_SenderList;
			} 
			finally
			{
				m_SenderSemaphore.release();
			}
		} 
		catch(InterruptedException ie)
		{
			// TODO - Report Interruption
		}
		
		return null;
	}
	
	
	// Thread Safe - Removes a sender from the list
	public void removeSender( int iIndex )
	{
		try
		{
			m_SenderSemaphore.acquire();
			
			try
			{
				ConnectionSender mSender = m_SenderList.get( iIndex );
			
				m_SenderList.remove( iIndex );
				
				mSender.terminate();
			} 
			finally
			{
				m_SenderSemaphore.release();
			}
		} 
		catch(InterruptedException ie)
		{
			// TODO - Report Interruption
		}
	}
	
	
	// Thread Safe - Adds a sender to the list
	public void addSender( ConnectionSender newSender )
	{
		try
		{
			m_SenderSemaphore.acquire();
			
			try
			{
				m_SenderList.add( newSender );
			} 
			finally
			{
				m_SenderSemaphore.release();
			}
		} 
		catch(InterruptedException ie)
		{
			// TODO - Report Interruption
		}
	}
}