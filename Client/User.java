import java.util.*; 

// Static User class
public class User
{
	// User Defining Variables
	private static int m_UserID      = 0;
	private static String m_UserName = "NO USER"; 
	private static int m_AccessLevel = 0;
	private static int m_WriteLevel  = 0;

	// Friends list
	// TODO: Decide on the correct data structure
	private static List<Object> m_FriendsList = new ArrayList<Object>();
	

	// Saves variables
	public static void setUser( int iUserID, String sUserName, int iAccessLevel, int iWriteLevel )
	{
		m_UserID      = iUserID;
		m_UserName    = sUserName;
		m_AccessLevel = iAccessLevel;
		m_WriteLevel  = iWriteLevel;
	}
	
	
	// Resets variables
	public static void resetUser()
	{
		m_UserID      = 0;
		m_UserName    = "NO USER"; 
		m_AccessLevel = 0;
	}
	
	
	// Gets the Users ID
	public static int getUserID()
	{
		return m_UserID;
	}
	
	
	// Gets the Users name
	public static String getUsername()
	{
		return m_UserName;
	}
	
	
	// Gets the access level for the current user
	public static int getAccessLevel()
	{
		return m_AccessLevel;
	}
	
	
	// Get the write level for the current user
	public static int getWriteLevel()
	{
		return m_WriteLevel;
	}
}