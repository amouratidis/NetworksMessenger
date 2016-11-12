import java.util.*;
import java.util.concurrent.Semaphore;


public class OnlineUsers
{
	private static List<Integer> m_UsersOnline = new ArrayList<Integer>();
	private static Semaphore m_UserSemaphore   = new Semaphore( 1, true );
	
	
	// Add a user to our list
	public static void addUser( int iUserID )
	{
		try
		{
			m_UserSemaphore.acquire();
			
			try
			{
				m_UsersOnline.add( iUserID );
			} 
			finally
			{
				m_UserSemaphore.release();
			}
		} 
		catch(InterruptedException ie)
		{
			// TODO - Report Interruption
		}
	}
	
	
	// Remove a user from our list
	public static void removeUser( int iUserID )
	{
		try
		{
			m_UserSemaphore.acquire();
			
			try
			{
				m_UsersOnline.remove( Integer.valueOf( iUserID ) );
			} 
			finally
			{
				m_UserSemaphore.release();
			}
		} 
		catch(InterruptedException ie)
		{
			// TODO - Report Interruption
		}
	}
	
	
	// Returns the list of online users
	public static List<Integer> getUsers()
	{
		try
		{
			m_UserSemaphore.acquire();
			
			try
			{
				return m_UsersOnline;
			} 
			finally
			{
				m_UserSemaphore.release();
			}
		} 
		catch(InterruptedException ie)
		{
			// TODO - Report Interruption
		}
		
		return null;
	}
}