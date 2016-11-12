import java.io.*;
import java.net.*;

public class ConnectionManager extends Thread
{
	private ServerSocket m_TCPServer;
	
	
	// Thread starting point
	public void run()
	{	
		int iNextID = 0;
	
		// Create our Message Router
		MessageRouter router = new MessageRouter();
	
		// Create the Public Channels
		for( int iChannel = 0; iChannel < Constants.CHANNELS.length; iChannel++ )
		{
			PacketGenerator.addConversation( iChannel, null, Constants.CHANNELS[iChannel][0], Constants.CHANNELS[iChannel][1], ( iChannel + ".txt" ) );
			Logger.createLogFile( iChannel );
		}
	
		try
		{
			// Loop forever accepting connections
			while( true )
			{
				// Create a new socket
				Socket tcpSocket = new Socket();
				
				// Wait for a connection on the socket
				tcpSocket = m_TCPServer.accept();
				
				// Create a new thread that listens on that socket
				ConnectionReceiver cReceiver = new ConnectionReceiver( tcpSocket, iNextID, router );
				ConnectionSender   cSender   = new ConnectionSender( tcpSocket, iNextID );
				
				router.addSender( cSender );
				
				// Increment the next ID used (for testing, should be replaced with a better structure)
				iNextID++;
			}
		}
		catch( IOException e )
		{
			System.out.println( "Unable to create new connection " + iNextID );
		}
    }

	
	// Class Constructor, start the thread here
    ConnectionManager( ServerSocket tcpServer )
	{
		m_TCPServer = tcpServer;
		
        this.start();
    }

}