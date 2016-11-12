import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.Semaphore;

public class ConnectionSender extends Thread
{
	private Socket m_TCPSocket;
	private int iConnectionID;
	private boolean bTerminated;
	private Queue<Packet> m_PacketQueue;
	private Semaphore m_Semaphore;
	private DataOutputStream m_TCPOutBuffer;
	private int m_UserID;


	// Thread starting point
	public void run()
	{	
		try 
		{
            // As long as we receive data, echo that data back to the client.			
            while (!bTerminated)
			{			
				sendPackets();
				sleep( 100 );
			}
            
            // Close the connections (TCP)
            m_TCPSocket.close();
        }   
        catch( Exception e ) 
		{
            System.out.println( e );
        }
    }

	
	// Class Constructor, start the thread here
    ConnectionSender( Socket socket, int iID )
	{
		m_TCPSocket    = socket;
		iConnectionID  = iID;
		bTerminated    = false;
		m_Semaphore    = new Semaphore( 1, true );	
		m_PacketQueue  = new LinkedList<Packet>();
		m_UserID       = -1;
		
		try
		{
			m_TCPOutBuffer = new DataOutputStream( m_TCPSocket.getOutputStream() );
		}
		catch( IOException E )
		{
			// Unable to set up the Sender
		}
		finally
		{
			this.start();
		}
    }
	
	
	// Sets the terminate flag to end this thread gracefully
	public void terminate()
	{
		bTerminated = true;
	}
	
	
	// Gets the Connection ID that this sender uses (This will never change)
	public int getConnectionID()
	{
		return iConnectionID;
	}
	
	
	// Gets the User ID that this sender uses (This will change when a new user logs in)
	public int getUserID()
	{
		return m_UserID;
	}	
	
	
	// Sets the User ID that this sender uses (This will change when a new user logs in)
	public void setUserID( int iUserID )
	{
		m_UserID = iUserID;
	}
	
	/* -- Semaphore based function for the PacketQueue, these are thread safe -- */
	
	
	// Externally used to add a new Packets to the send queue
	public void addToQueue( Packet newPacket )
	{
		try
		{
			m_Semaphore.acquire();
			
			try
			{
				m_PacketQueue.add( newPacket );
			} 
			finally
			{
				m_Semaphore.release();
			}
		} 
		catch( InterruptedException ie )
		{
			// TODO - Report Interruption
		}
	}
	
	
	// Internally used to get the queue of Packets and send them all
	private void sendPackets()
	{
		try
		{
			m_Semaphore.acquire();
			
			try
			{
				for( int iMessage = 0; iMessage < m_PacketQueue.size(); iMessage++ )
				{
                    int iHeader  = m_PacketQueue.element().getHeader();
					byte[] bData = m_PacketQueue.element().getData();
		
					// Send the packet
                    m_TCPOutBuffer.writeInt( bData.length );
					m_TCPOutBuffer.writeInt( iHeader );	
					m_TCPOutBuffer.write( bData, 0, bData.length );
					
					// Remove the Packet from the queue
					m_PacketQueue.remove();
				}			
			}
			catch( Exception e )
			{
				// Writing error
			}
			finally
			{
				m_Semaphore.release();
			}
		} 
		catch( InterruptedException ie )
		{
			// TODO - Report Interruption
		}
	}
}