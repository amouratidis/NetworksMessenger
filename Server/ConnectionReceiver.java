import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.*;

public class ConnectionReceiver extends Thread
{
	private Socket m_TCPSocket;
	private int m_ConnectionID;
	private int m_UserID;
	private boolean bTerminated;
	private MessageRouter m_MessageRouter;
	
	// Thread starting point
	public void run()
	{	
		try 
		{
            // Open input and output streams (TCP)
            DataInputStream tcpInBuffer  = new DataInputStream( m_TCPSocket.getInputStream() );
					
            // As long as we receive data, echo that message to the console			
            while (!bTerminated)
			{			
				// Prepare for the incoming data
				int iLength  = tcpInBuffer.readInt();
				int iHeader  = tcpInBuffer.readInt();			
				byte[] bData = new byte[iLength];

				// Read the incoming data
				tcpInBuffer.readFully( bData, 0, iLength );
				
				System.out.println( "Received Data - Header: " + iHeader );
				
				// If we received a login packet, extract the UserID before routing
				if( iHeader == HeaderType.HEADER_LOGIN )
				{
					String sData     = new String( bData );
					String sUsername = sData.substring( 0, sData.indexOf( ' ' ) );
					int iUserID      = DatabaseAccess.getUserID( sUsername );
					
					// If the user was found, update
					if( iUserID != 0 )
					{
						m_UserID = iUserID;
						m_MessageRouter.updateUserID( m_ConnectionID, m_UserID );
					}
				}

				// Create a packet with the received data
				Packet receivedPacket = new Packet( m_UserID, iHeader, bData );
				
				// Format the packet so its send able
				SendablePacket sendablePacket = PacketGenerator.formatPacket( receivedPacket );
				
				// Route the send able packet
				m_MessageRouter.route( sendablePacket );
			}
            
            // Close the connections (TCP)
            m_TCPSocket.close();
        }   
        catch (IOException e) 
		{
            // Connection with this user has been lost, create a fake logout packet
			byte[] bStatus = ByteBuffer.allocate(4).putInt( HeaderType.STATUS_OFFLINE ).array();
			byte[] bUserID = ByteBuffer.allocate(4).putInt( m_UserID ).array();
			byte[] bData   = new byte[bUserID.length + bStatus.length];
			
			System.arraycopy( bStatus, 0, bData, 0,              bStatus.length );
			System.arraycopy( bUserID, 0, bData, bStatus.length, bUserID.length );
			
			// Create a fake received packet
			Packet receivedPacket = new Packet( m_UserID, HeaderType.HEADER_STATUS, bData );
				
			// Format the packet so its send able
			SendablePacket sendablePacket = PacketGenerator.formatPacket( receivedPacket );
				
			// Route the send able packet
			m_MessageRouter.route( sendablePacket );
        }
    }

	
	// Class Constructor, start the thread here
    ConnectionReceiver( Socket socket, int iID, MessageRouter router )
	{
		m_TCPSocket     = socket;
		m_ConnectionID  = iID;
		bTerminated     = false;
		m_MessageRouter = router;
		
        this.start();
    }
}