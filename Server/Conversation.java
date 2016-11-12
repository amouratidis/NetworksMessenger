import java.util.*;
import java.util.concurrent.Semaphore;

// Used to store information on an individual conversation
public class Conversation
{
	private int m_ConversationID;
	private List<Integer> m_Users;
	private int m_ReadLevel;
	private int m_WriteLevel;
	private Semaphore m_ConvoSemaphore;
	private String m_LogFile;
	
	
	// Creates a Conversation and saves class variables
	Conversation( int iConvoID, List<Integer> iUsers, int iReadLevel, int iWriteLevel, String sLogName )
	{
		m_ConversationID = iConvoID;
		m_Users          = iUsers;
		m_ReadLevel      = iReadLevel;
		m_WriteLevel     = iWriteLevel;
		m_ConvoSemaphore = new Semaphore( 1, true );
		m_LogFile        = sLogName;
		
		// If we were given no user list - create an empty one
		if( m_Users == null )
			m_Users = new ArrayList<Integer>();
	}
	
	
	// Returns the ID of the Conversation
	public int getID()
	{
		return m_ConversationID;
	}
	
	
	// Returns the access level required to read from this conversation
	public int getReadLevel()
	{
		return m_ReadLevel;
	}
	
	
	// Returns the access level required to write in this conversation
	public int getWriteLevel()
	{
		return m_WriteLevel;
	}
	
	
	// Returns the Log FileName
	public String getLogFile()
	{
		return m_LogFile;
	}
	
	
	// -- Semaphore based functions -- //
	
	
	// Add a new user to the Conversation
	public void addUser( int iUserID )
	{
		try
		{
			m_ConvoSemaphore.acquire();
			
			try
			{
				// Don't add duplicate users
				if( m_Users.indexOf( new Integer( iUserID ) ) != -1 )
					return;
				
				m_Users.add( iUserID );
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
	

	// Returns the Users in the conversation
	public List<Integer> getUsers()
	{
		try
		{
			m_ConvoSemaphore.acquire();
			
			try
			{
				return m_Users;
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
	
	
	// Checks if the Conversation contains a specified User
	public boolean hasUser( int iUserID )
	{
		try
		{
			m_ConvoSemaphore.acquire();
			
			try
			{
				for( int iUser = 0; iUser < m_Users.size(); iUser++ )
				{
					if( m_Users.get( iUser ) == iUserID )
						return true;
				}
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
		
		return false;	
	}
}