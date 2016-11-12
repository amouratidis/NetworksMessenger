// Used to store packet information in one place
public class Packet
{
	protected int    m_SenderID;
	protected int    m_HeaderType;
	protected byte[] m_Data;
	
	// Constructor for low detail packets
	Packet( int iSender, int iHeader, byte[] bData )
	{
		m_SenderID   = iSender;
		m_HeaderType = iHeader;
		m_Data       = bData;
	}
	
	// Returns the userID who sent the packet
	public int getSender()
	{
		return m_SenderID;
	}
	
	
	// Returns the header of the packet
	public int getHeader()
	{
		return m_HeaderType;
	}
	
	
	// Returns the data of the packet
	public byte[] getData()
	{
		return m_Data;
	}
}