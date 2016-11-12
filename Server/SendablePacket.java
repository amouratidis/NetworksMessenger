import java.util.*;

public class SendablePacket extends Packet
{
	private List<Integer> m_Users;

	// Constructor for high detail packets
	SendablePacket( int iSender, int iHeader, byte[] bData, List<Integer> iUsers )
	{
		// Create the base packet
		super( iSender, iHeader, bData );
	
		// Set the SendablePacket specific variable
		m_Users = iUsers;
	}
	
	
	// Returns the list of users to send to
	public List<Integer> getUsers()
	{
		return m_Users;
	}
}