// Contains public access to all our message header types
public final class HeaderType
{
	// Integers used to define the type of packet
	public final static int HEADER_UNSPECIFIED = 0;
	public final static int HEADER_MESSAGE     = 1;
	public final static int HEADER_FILE        = 2;
	public final static int HEADER_LOGIN       = 3;
	public final static int HEADER_CONVO       = 4;
	public final static int HEADER_JOIN        = 5;
	public final static int HEADER_STATUS      = 6;
	public final static int HEADER_LOG         = 7;
	
	// Integers used to define the type of status packet
	public final static int STATUS_ONLINE  = 0;
	public final static int STATUS_OFFLINE = 1;
	public final static int STATUS_ADD     = 2;
	public final static int STATUS_REMOVE  = 3;
	public final static int STATUS_FRIENDS = 4;
}
