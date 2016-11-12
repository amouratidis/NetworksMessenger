// Contains User Information stored server-side
public final class Constants
{
	// Constants for Read/Write Access
	public static final int NOBODY  = -1;
	public static final int ANYBODY = -1;

	// Used when accessing the Array of Users
	public static final int USER_USERNAME    = 0;
	public static final int USER_PASSWORD    = 1;
	public static final int USER_READ_LEVEL  = 2;
	public static final int USER_WRITE_LEVEL = 3;

	// The Array of Users stored on the server
	// { "USERNAME", "PASSWORD", "READLEVEL", "WRITELEVEL" }
	public static final String[][] USERINFO =
	{
		{ "Fred",  "CalgaryFlames", "0", "0" },
		{ "Amy",   "1992",          "0", "0" },
		{ "Aaron", "123",           "1", "1" },
		{ "Lisa",  "music",         "1", "1" },
		{ "Adam",  "456789",        "1", "1" },
		{ "ADMIN",  "ADMIN",        "2", "2" }
	};
	
	// Used when accessing the Array of Channels
	public static final int CHAN_READ_LEVEL  = 0;
	public static final int CHAN_WRITE_LEVEL = 1;

	// The Array of Channels stored on the server
	// { "READLEVEL", "WRITELEVEL" }
	public static final int[][] CHANNELS =
	{
		{ 0, 0 },
		{ 0, 1 },
		{ 1, 2 }
	};
}