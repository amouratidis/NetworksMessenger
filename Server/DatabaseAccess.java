import java.util.*; 
import java.sql.*;

public class DatabaseAccess
{
	private static Connection m_Connection;
	private static Statement m_Statement;
	private static ResultSet m_ResultSet;
	
	private static final String DRIVERS  = "com.mysql.jdbc.Driver";
	private static final String DATABASE = "jdbc:mysql://localhost:9323/messenger";
	private static final String USERNAME = "MessengerServer";
	private static final String PASSWORD = "CPSC441g38";

	
	// Connect to the database, if this fails - exit
	public static boolean initDatabase()
	{
		try
		{
			// Register the correct drivers
			Class.forName( DRIVERS );
			
			// Attempt to connect to the database
			m_Connection = DriverManager.getConnection( DATABASE, USERNAME, PASSWORD );
			
			// Create the Statement object
			m_Statement = m_Connection.createStatement();
			
			// Inform user
			System.out.println( "DATABASE CONNECTED" );
			
			return true;
		}
		catch( Exception e )
		{
			// TODO: We should figure out what to do in this case
			System.out.println( "DATABASE SETUP FAILED" );
			System.out.println( e.toString() );
		}
		
		return false;
	}
	
	
	// Used to close access to the database gracefully
	public static void terminate()
	{
		try
		{
			// Close the Result Set
			m_ResultSet.close();
			
			// Close the Statement
			m_Statement.close();
			
			// Close the Connection
			m_Connection.close();
			
			// Inform user
			System.out.println( "DATABASE CLOSED" );
		}
		catch( Exception e )
		{
			System.out.println( "DATABASE CLOSE FAILED" );
		}
	}
	
	
	// -- Database accessing functions used by the Server -- //
	
	
	// Adds a user to a friends list
	public static boolean addUser( int iUserID, String sUsername, int iFriendID, String sFriendName )
	{
		try
		{
			// Create the SQL Statement
			String sQuery = "INSERT INTO Friends(UserID, UserName, FriendID, FriendName)";
			sQuery       += " VALUES(" + iUserID + ", ";
			sQuery       += "'" + sUsername + "', ";
			sQuery       += iFriendID + ", ";
			sQuery       += "'" + sFriendName + "');";
			
			// Execute the statement
			m_Statement.executeUpdate( sQuery );
			
			// Fall through means success
			return true;
		}
		catch( SQLException e )
		{
			// If something fails, return null, it's up to the caller to check for this
			System.out.println( e.toString() );
		}
		
		return false;
	}
	
	
	// Removes a user to a friends list
	public static boolean removeUser( int iUserID, int iFriendID )
	{
		try
		{
			// Create the SQL Statement
			String sQuery = "DELETE FROM Friends";
			sQuery       += " WHERE UserID = " + iUserID;
			sQuery       += " AND FriendID = " + iFriendID;
			
			// Execute the statement
			m_Statement.executeUpdate( sQuery );
			
			// Fall through means success
			return true;
		}
		catch( SQLException e )
		{
			// If something fails, return null, it's up to the caller to check for this
			System.out.println( e.toString() );
		}
		
		return false;
	}
		
	
	// Used by the Server to get User Information from the DB
	public static User getUser( int iUserID )
	{
		try
		{
			// Create the SQL Statement
			String sQuery = "SELECT *";
			sQuery       += " FROM user";
			sQuery       += " WHERE UserID = " + iUserID;
			
			// Get the result from the DB
			m_ResultSet = m_Statement.executeQuery( sQuery );
			
			String sUsername = "";
			int iReadLevel   = 0;
			int iWriteLevel  = 0;
			
			// Set pointer to the first record
			if( m_ResultSet.first() )
			{
				// Process the result
				sUsername   = m_ResultSet.getString( "UserName" );
				iReadLevel  = m_ResultSet.getInt( "Readlevel" );
				iWriteLevel = m_ResultSet.getInt( "Writelevel" );
			}
			
			// Create and return the User object
			return new User( iUserID, sUsername, iReadLevel, iWriteLevel );
		}
		catch( SQLException e )
		{
			// If something fails, return null, it's up to the caller to check for this
			System.out.println( e.toString() );
			return null;
		}
	}
	
	
	// Used by the Server to get User Information from the DB
	public static User getUser( String sUsername, String sPassword )
	{
		try
		{
			// Create the SQL Statement
			String sQuery = "SELECT *";
			sQuery       += " FROM user";
			sQuery       += " WHERE UserName = '" + sUsername + "'";
			sQuery	     += " AND Password = '" + sPassword + "'";  
			
			// Get the result from the DB
			m_ResultSet = m_Statement.executeQuery( sQuery );
			
			int iUserID     = 0;
			int iReadLevel  = 0;
 			int iWriteLevel = 0;
			
			// Set pointer to the first record
			if( m_ResultSet.first() )
			{
				// Process the result
				iUserID     = m_ResultSet.getInt( "UserID" );
				iReadLevel  = m_ResultSet.getInt( "Readlevel" );
				iWriteLevel = m_ResultSet.getInt( "Writelevel" );
			}
			
			// Create and return the User object
			return new User( iUserID, sUsername, iReadLevel, iWriteLevel );
		}
		catch( SQLException e )
		{
			// If something fails, return null, it's up to the caller to check for this
			System.out.println( e.toString() );
			return null;
		}
	}
	
	
	// Used by the Server to get User Information from the DB
	public static int getUserID( String sUsername )
	{
		try
		{
			// Create the SQL Statement
			String sQuery = "SELECT *";
			sQuery       += " FROM user";
			sQuery       += " WHERE UserName = '" + sUsername + "'";
			
			// Get the result from the DB
			m_ResultSet = m_Statement.executeQuery( sQuery );
			
			int iUserID = 0;
			
			// Set pointer to the first record
			if( m_ResultSet.first() )
			{
				// Process the result
				iUserID = m_ResultSet.getInt( "UserID" );
			}	
			// Return the UserID
			return iUserID;
		}
		catch( SQLException e )
		{
			// If something fails, return null, it's up to the caller to check for this
			System.out.println( e.toString() );
			return 0;
		}
	}
	
	
	// Used by the Server to get a FriendsList from the DB
	public static List<User> getFriendsList( int iUserID )
	{
		try
		{
			// Create the SQL Statement
			String sQuery = "SELECT *";
			sQuery       += " FROM friends";
			sQuery       += " WHERE UserID = " + iUserID; 
			
			// Get the result from the DB
			m_ResultSet = m_Statement.executeQuery( sQuery );
			
			// Set pointer to the first record
			m_ResultSet.first();
			
			// Process the result
			List<User> friendsList = new ArrayList<User>();	
			
			// Loop for all records
			do
			{
				// Get the Friend ID and Name from each record
				int    iFriendID   = m_ResultSet.getInt( "FriendID" );
				String sFriendName = m_ResultSet.getString( "FriendName" );

				// Create a user for each friend and add to our list
				friendsList.add( new User( iFriendID, sFriendName, 0, 0 ) );
			}
			while( m_ResultSet.next() );
			
			return friendsList;
		}
		catch( SQLException e )
		{
			// If something fails, return null, it's up to the caller to check for this
			System.out.println( e.toString() );
			return null;
		}
	}
	
	
	// Used by the Server to get a FriendsList from the DB
	public static List<User> getFriendsOf( int iFriendID )
	{
		try
		{
			// Create the SQL Statement
			String sQuery = "SELECT *";
			sQuery       += " FROM friends";
			sQuery       += " WHERE FriendID = " + iFriendID; 
			
			// Get the result from the DB
			m_ResultSet = m_Statement.executeQuery( sQuery );
			
			// Set pointer to the first record
			m_ResultSet.first();
			
			// Process the result
			List<User> friendsList = new ArrayList<User>();	
			
			// Loop for all records
			do
			{
				// Get the User ID and Name from each record
				int    iUserID   = m_ResultSet.getInt( "UserID" );
				String sUserName = m_ResultSet.getString( "UserName" );

				// Create a user for each friend and add to our list
				friendsList.add( new User( iUserID, sUserName, 0, 0 ) );
			} 
			while( m_ResultSet.next() );
			
			return friendsList;
		}
		catch( SQLException e )
		{
			// If something fails, return null, it's up to the caller to check for this
			System.out.println( e.toString() );
			return null;
		}
	}
	
	
	/*
	// Used by the Server to get the filename for a conversation history from the DB
	public String getHistory( int iConvoID )
	{
		try
		{
			// Create the SQL Statement
			String sQuery = "SELECT *";
			sQuery       += " FROM history";
			sQuery       += " WHERE ConvoID = " + iConvoID; 
			
			// Get the result from the DB
			m_ResultSet = m_Statement.executeQuery( sQuery );
			
			// Set pointer to the first record
			m_ResultSet.first();
			
			// Process the result
			String sFilename = m_ResultSet.getString( "Filename" );
			
			// Return the Filename of the history
			return sFilename;
		}
		catch( SQLException e )
		{
			// If something fails, return null, it's up to the caller to check for this
			return null;
		}
	}
	
	
	// Used by the Server to get a list of public Channels from the DB
	public List<Conversation> getPublicChannels()
	{
		try
		{
			// Create the SQL Statement
			String sQuery = "SELECT *";
			sQuery       += " FROM publicChannels";
			
			// Get the result from the DB
			m_ResultSet = m_Statement.executeQuery( sQuery );
			
			// Set pointer to the first record
			m_ResultSet.first();
			
			// Process the result
			List<Conversation> channelList = new ArrayList<Conversation>();	
			
			// Loop for all records
			while( m_ResultSet.next() )
			{
				// Get the Channel ID and Read/Write level
				int    iChannelID   = m_ResultSet.getInt( "ChannelID" );
				String sChannelName = m_ResultSet.getString( "ChannelName" );
				int    iReadLevel   = 0;
				int    iWriteLevel  = 0;

				// Create a Conversation for each record
				channelList.add( new Conversation( iChannelID, null, iReadLevel, iWriteLevel ) );
			}
			
			return channelList;
		}
		catch( SQLException e )
		{
			// If something fails, return null, it's up to the caller to check for this
			return null;
		}
	}
	

	
	// Used by the Server to get a list of Subscribed Channels from the DB
/*	public List<Channel> getSubbedChannels( int iUserID )
	{
		try
		{
			// Create the SQL Statement
			String sQuery = "SELECT *";
			sQuery       += " FROM subbedChannels";
			sQuery       += " WHERE UserID = " + iUserID; 
			
			// Get the result from the DB
			m_ResultSet = m_Statement.executeQuery( sQuery );
			
			// Set pointer to the first record
			m_ResultSet.first();
			
			// Process the result
			List<Channel> channelList = new ArrayList<Channel>();	
			
			// Loop for all records
			while( m_ResultSet.next() )
			{
				// Get the Channel ID and Name from each record
				int    iChannelID   = m_ResultSet.getInt( "ChannelID" );
				String sChannelName = m_ResultSet.getString( "ChannelName" );

				// Create a Channel for each record
				channelList.add( new Channel( iChannelID, sChannelName ) );
			}
			
			return channelList;
		}
		catch( SQLException e )
		{
			// If something fails, return null, it's up to the caller to check for this
			return null;
		}
	} 
	
	
	// Used by the Server to get all files related to a User from the DB
	public List<String> getFiles( int iUserID )
	{
		try
		{
			// Create the SQL Statement
			String sQuery = "SELECT *";
			sQuery       += " FROM files";
			sQuery       += " WHERE UserID = " + iUserID; 
			
			// Get the result from the DB
			m_ResultSet = m_Statement.executeQuery( sQuery );
			
			// Process the result
			List<String> fileList = new ArrayList<String>();	
			
			// Loop for all records
			while( m_ResultSet.next() )
			{
				// Get the Filename from each record
				String sFilename = m_ResultSet.getString( "Filename" );

				// For each record, add the Filename
				fileList.add( sFilename );
			}
			
			return fileList;
		}
		catch( SQLException e )
		{
			// If something fails, return null, it's up to the caller to check for this
			return null;
		}
	}
	
	
	// Used by the Server to get all sent files for a specific User from the DB
	public List<String> getSentFiles( int iUserID )
	{
		try
		{
			// Create the SQL Statement
			String sQuery = "SELECT *";
			sQuery       += " FROM files";
			sQuery       += " WHERE UserID = " + iUserID;
			sQuery       += " AND Status = SENT";
			
			// Get the result from the DB
			m_ResultSet = m_Statement.executeQuery( sQuery );
			
			// Process the result
			List<String> fileList = new ArrayList<String>();	
			
			// Loop for all records
			while( m_ResultSet.next() )
			{
				// Get the Filename from each record
				String sFilename = m_ResultSet.getString( "Filename" );

				// For each record, add the Filename
				fileList.add( sFilename );
			}
			
			return fileList;
		}
		catch( SQLException e )
		{
			// If something fails, return null, it's up to the caller to check for this
			return null;
		}
	}
	
	
	// Used by the Server to get all received files for a specific User from the DB
	public List<String> getReceivedFiles( int iUserID )
	{
		try
		{
			// Create the SQL Statement
			String sQuery = "SELECT *";
			sQuery       += " FROM files";
			sQuery       += " WHERE UserID = " + iUserID;
			sQuery       += " AND Status = RECEIVED";
			
			// Get the result from the DB
			m_ResultSet = m_Statement.executeQuery( sQuery );
			
			// Process the result
			List<String> fileList = new ArrayList<String>();	
			
			// Loop for all records
			while( m_ResultSet.next() )
			{
				// Get the Filename from each record
				String sFilename = m_ResultSet.getString( "Filename" );

				// For each record, add the Filename
				fileList.add( sFilename );
			}
			
			return fileList;
		}
		catch( SQLException e )
		{
			// If something fails, return null, it's up to the caller to check for this
			return null;
		}
	}	*/
}