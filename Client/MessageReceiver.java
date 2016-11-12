import java.io.*;
import java.net.*;
import java.util.*; 

public class MessageReceiver extends Thread
{
	private Socket m_TCPSocket;
	private DataInputStream m_InputStream;
    private ReceiverCallBack m_ReceiverCallBack;

	
	// Thread starting point
	public void run()
	{	
        try 
		{
            // Open input stream (TCP)
            m_InputStream = new DataInputStream( m_TCPSocket.getInputStream() );

            // Set the terminated flag
            boolean bTerminated = false;
					
            // Loop as long as we're not terminated			
            while (!bTerminated)
			{			
				// Extract the Length and Header
                int iLength = m_InputStream.readInt();
				int iHeader = m_InputStream.readInt();
				
                // Extract the byte Data
				byte[] bData = new byte[iLength];
				m_InputStream.readFully( bData, 0, iLength );

                // Route the data based on the header
				switch( iHeader )
                {
                    // Treat the data as a Message
                    case( HeaderType.HEADER_MESSAGE ):
                        receiveMessage( bData );                        

                        break;

                    // Treat the data as a Login Confirmation
                    case( HeaderType.HEADER_LOGIN ):
                        receiveLogin( bData );

                        break;

                    // Treat the data as a Conversation Request
                    case( HeaderType.HEADER_CONVO ):
                        receiveConvo( bData );

                        break;  

					// Treat the data as a Join request
					case( HeaderType.HEADER_JOIN ):
						receiveJoin( bData );
						
						break;
						
					// Treat the data as a sent file
					case( HeaderType.HEADER_FILE ):
						receiveFile( bData );
						
						break;
						
					// Treat the data as a status message
					case( HeaderType.HEADER_STATUS ):
						receiveStatus( bData );
						
						break;
						
					// Treat the data as a conversation login
					case( HeaderType.HEADER_LOG ):
						receiveLog( bData );
						
						break;
                }
			}
            
            // Close the connections (TCP)
            m_TCPSocket.close();
        }   
        catch (IOException e) 
		{
            System.out.println( "MessageReceiver Failure" );
			System.out.println( e.toString() );
        }
    }

	
	// Class Constructor, start the thread here
    MessageReceiver( Socket s, ReceiverCallBack rcb )
	{
		m_TCPSocket        = s;
        m_ReceiverCallBack = rcb;
		
        this.start();
    }


    // Converts the byte data into a message
    private void receiveMessage( byte[] bData )
    {
		// Get the Conversation ID first
		int iConvoID = (int)( ( ( bData[0] & 0xFF ) << 24 ) | ( ( bData[1] & 0xFF ) << 16 ) | ( ( bData[2] & 0xFF ) << 8 ) | ( bData[3] & 0xFF ) );        

        // Extract the Message from the byte data
        String sMessage = new String( Arrays.copyOfRange( bData, 4, bData.length ) );

        // Send the CallBack
        m_ReceiverCallBack.receiveMessage( iConvoID, sMessage );
    }


    // Converts the byte data into login information
    private void receiveLogin( byte[] bData )
    {
        // Extract the login result
		int iUserID      = (int)( ( ( bData[0] & 0xFF ) << 24 ) | ( ( bData[1] & 0xFF ) << 16 ) | ( ( bData[2] & 0xFF ) << 8 ) | ( bData[3] & 0xFF ) );        
		int iAccessLevel = (int)( ( ( bData[4] & 0xFF ) << 24 ) | ( ( bData[5] & 0xFF ) << 16 ) | ( ( bData[6] & 0xFF ) << 8 ) | ( bData[7] & 0xFF ) );        
		int iWriteLevel  = (int)( ( ( bData[8] & 0xFF ) << 24 ) | ( ( bData[9] & 0xFF ) << 16 ) | ( ( bData[10] & 0xFF ) << 8 ) | ( bData[11] & 0xFF ) );        
		
        // Send the CallBack        
        m_ReceiverCallBack.receiveLogin( iUserID, iAccessLevel, iWriteLevel );
    }


    // Converts the byte data into a conversation request
    private void receiveConvo( byte[] bData )
    {
		// Get the Conversation ID first
		int iConvoID = (int)( ( ( bData[0] & 0xFF ) << 24 ) | ( ( bData[1] & 0xFF ) << 16 ) | ( ( bData[2] & 0xFF ) << 8 ) | ( bData[3] & 0xFF ) );        

        // Send the CallBack
        m_ReceiverCallBack.receiveConvo( iConvoID, new String[]{"TestPerson1", "TestPerson2"});        
    }
	
	
	// Converts the byte data into a join request
    private void receiveJoin( byte[] bData )
    {
		// Get the Conversation ID first
		int iConvoID = (int)( ( ( bData[0] & 0xFF ) << 24 ) | ( ( bData[1] & 0xFF ) << 16 ) | ( ( bData[2] & 0xFF ) << 8 ) | ( bData[3] & 0xFF ) );        
	
		// Get the Access levels
		int iReadLevel  = (int)( ( ( bData[4] & 0xFF ) << 24 ) | ( ( bData[5] & 0xFF ) << 16 ) | ( ( bData[6] & 0xFF ) << 8 ) | ( bData[7] & 0xFF ) );        
		int iWriteLevel = (int)( ( ( bData[8] & 0xFF ) << 24 ) | ( ( bData[9] & 0xFF ) << 16 ) | ( ( bData[10] & 0xFF ) << 8 ) | ( bData[11] & 0xFF ) );        
		
        // Send the CallBack
        m_ReceiverCallBack.receiveJoin( iConvoID, iReadLevel, iWriteLevel );        
    }
	
	
	// Converts the byte data into a file
	private void receiveFile( byte[] bData )
	{
		// Extract the FileName length
		int iNameLen     = (int)( ( ( bData[4] & 0xFF ) << 24 ) | ( ( bData[5] & 0xFF ) << 16 ) | ( ( bData[6] & 0xFF ) << 8 ) | ( bData[7] & 0xFF ) );        
		String sName     = new String( Arrays.copyOfRange( bData, 8, 8 + iNameLen ) );
		byte[] bFileData = Arrays.copyOfRange( bData, 8 + iNameLen, bData.length );
	
		m_ReceiverCallBack.receiveFile( sName, bFileData );
	}
	
	
	// Converts the byte data into a status message
	private void receiveStatus( byte[] bData )
	{
		// Extract the Status Type and the User involved
		int iStatusType = (int)( ( ( bData[0] & 0xFF ) << 24 ) | ( ( bData[1] & 0xFF ) << 16 ) | ( ( bData[2] & 0xFF ) << 8 ) | ( bData[3] & 0xFF ) );        
		String sUser    = new String( Arrays.copyOfRange( bData, 4, bData.length ) );
	
		m_ReceiverCallBack.receiveStatus( iStatusType, sUser );
	}
	
	
	// Converts the byte data into a conversation log
	private void receiveLog( byte[] bData )
	{
		// Extract the Status Type and the User involved
		int iConvoID = (int)( ( ( bData[0] & 0xFF ) << 24 ) | ( ( bData[1] & 0xFF ) << 16 ) | ( ( bData[2] & 0xFF ) << 8 ) | ( bData[3] & 0xFF ) );        
		String sLog  = new String( Arrays.copyOfRange( bData, 4, bData.length ) );
	
		m_ReceiverCallBack.receiveLog( iConvoID, sLog );
	}
	
}
