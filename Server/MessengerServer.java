import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;

public class MessengerServer
{
    private static boolean bTerminated;
	private static ServerSocket m_TCPServer;
	
    public static void main(String args[])
	{	
		// We require 1 argument to continue
		if (args.length != 1)
        {
            System.out.println("Usage: TCPServer <Listening Port>");
            System.exit(1);
        }
	
		// Set terminated to false
		bTerminated = false;
        
		try 
		{
            m_TCPServer = new ServerSocket( Integer.parseInt(args[0]) );
        }
        catch (IOException e) 
		{
            System.out.println(e);
        }
		
		// Create and wait for the database to be connected
		if( DatabaseAccess.initDatabase() )
		{	
			// After we check that we have a socket - Make a thread for accepting connections
			ConnectionManager connectionManager = new ConnectionManager( m_TCPServer );
		}
    }
}