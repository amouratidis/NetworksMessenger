import java.util.*;

public class User
{
	int m_UserID;
	String m_Username;
	int m_ReadLevel;
	int m_WriteLevel;
	
	
	// Class Constructor
	User( int iUserID, String sUsername, int iReadLevel, int iWriteLevel )
	{
		m_UserID     = iUserID;
		m_Username   = sUsername;
		m_ReadLevel  = iReadLevel;
		m_WriteLevel = iWriteLevel;
	}
	
	
	// Externally used to get the UserID
	public int getID()
	{
		return m_UserID;
	}
	
	
	// Externally used to get the Username
	public String getName()
	{
		return m_Username;
	}
	
	
	// Externally used to get the ReadLevel
	public int getReadLevel()
	{
		return m_ReadLevel;
	}
	
	
	public int getWriteLevel()
	{
		return m_WriteLevel;
	}
}