import java.io.*;
import java.util.*;
import java.util.Collections;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class Logger
{
	private final static String LOG_LOCATION = "Logs\\";

	// Parse the user list into a file string, check if we have a log with that name
	public static String createLogFile( List<Integer> iUsers )
	{
		// Check that we have enough users - This should never happen
		if( iUsers.size() < 1 )
			return "";
	
		// First sort the UserIDList
		Collections.sort( iUsers );
		
		// Create a unique string based on the entries
		String sFileName = Integer.toString( iUsers.get( 0 ) );
		
		for( int iNextUser = 1; iNextUser < iUsers.size(); iNextUser++ )
			sFileName = sFileName + "-" + iUsers.get( iNextUser );
			
		// Give the file an extension
		sFileName = sFileName + ".txt";
		
		// Attempt to create an empty version of the file if it does not already exist
		File logFile = new File( LOG_LOCATION + sFileName );
	
		try
		{
			if( !logFile.exists() )
				logFile.createNewFile();
		}
		catch( Exception e )
		{
			// Problem creating the file - return no file path
			System.out.println( "Unable to create log file - " + sFileName );
			System.out.println( e.toString() );
			return "";
		}
		
		// Now return the FileName
		return sFileName;
	}

	
	// Using a public chat number, create a log file for it
	public static String createLogFile( int iPublicNumber )
	{	
		// Give the file an extension
		String sFileName = iPublicNumber + ".txt";
		
		System.out.println( "Log File Name: " + sFileName );
		
		// Attempt to create an empty version of the file if it does not already exist
		File logFile = new File( LOG_LOCATION + sFileName );
	
		try
		{
			if( !logFile.exists() )
				logFile.createNewFile();
		}
		catch( Exception e )
		{
			// Problem creating the file - return no file path
			System.out.println( "Unable to create log file - " + sFileName );
			System.out.println( e.toString() );
			return "";
		}
		
		// Now return the FileName
		return sFileName;
	}
	
	
	
	// Retrieve the LogFile as a single string
	public static String getLogFile( String sFileName )
	{
		// Check if we're given an empty string
		if( sFileName.compareTo( "" ) == 0 )
			return "";
	
		byte[] bLogData = null;
		Path filePath   = Paths.get( LOG_LOCATION + sFileName );
			
		try
		{		
			// Read the Log File
			bLogData = Files.readAllBytes( filePath );	
		}
		catch( Exception e )
		{
			System.out.println( "Unable to read LogFile" );
			return "";
		} 

		// Return the Log data as a string
		return new String( bLogData );
	}
	
	
	// Add a Message to a specified LogFile
	public static void addToLog( String sFileName, byte[] bMessage )
	{
		// Find the filepath
		Path filePath = Paths.get( LOG_LOCATION + sFileName );
	
		try
		{
			Files.write( filePath, bMessage, StandardOpenOption.APPEND );
		}
		catch (IOException e)
		{
			System.out.println( "Unable to add to LogFile" );
		}
	}
}