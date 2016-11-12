import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

// Used by Each Client - Handles sending and receiving of files
public class FileManager
{
    private static final String FOLDER_NAME = "Files";	

	
	// Used to Setup and begin saving the file for each user involved
	public static void receiveFile( int iSenderID, int iReceiverID, String sFileName, byte[] bFileData )
	{
	    // Get the users
		User sender   = DatabaseAccess.getUser( iSenderID );
		User receiver = DatabaseAccess.getUser( iReceiverID );
		
		// Create the FilePath for each user
		String sSenderPath   = FOLDER_NAME + "\\" + sender.getName();
		String sReceiverPath = FOLDER_NAME + "\\" + receiver.getName();
		
		// Save the File for each user
		saveFile( sSenderPath,   sFileName, bFileData );
		saveFile( sReceiverPath, sFileName, bFileData );
	}
	
	
	// Used to save a file at a specific FilePath
	private static void saveFile( String sFilePath, String sFileName, byte[] bFileData )
	{	
        boolean bCreated = false;
        int iFileNum     = 0;
		String sFullPath = sFilePath + "\\" + sFileName;
		
        File newFile;	
		
		try
		{		
			// Create all the intermediate directories
			Path filePath = Paths.get( sFullPath );
			Files.createDirectories( filePath.getParent() );
		
			// Loop until we create the new empty file
			while( !bCreated )
			{
				newFile = new File( sFullPath );

				// If the File already exists, increment the filenum and create a new filepath
				if( newFile.exists() )
				{
					iFileNum++;
					sFullPath = sFilePath + "\\" + iFileNum + sFileName;
				}
				else
				{
					bCreated = true;
					newFile.createNewFile();
				}
			}

			// Create the output stream to the file
			FileOutputStream fileOutStream = new FileOutputStream( sFullPath );

			// Write to the file and close the stream        
			fileOutStream.write( bFileData );
			fileOutStream.close();
		}
		catch( Exception e )
		{
			System.out.println( "Unable to save file" );
		} 
	}
}
