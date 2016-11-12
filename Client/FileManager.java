import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

// Used by Each Client - Handles sending and receiving of files
public class FileManager
{
    private final String FOLDER_NAME = "Files";	


    MessageManager m_MessageManager;

	
	// Get the MessageManager when created
	FileManager( MessageManager mm )
	{
		m_MessageManager = mm;

        // Check if we need to create a folder to hold the files
        File folder = new File( FOLDER_NAME );

        if( !folder.exists() )
            folder.mkdir();
	}


	// -- SENDING FILES -- //


	// Publicly used to Send a known file
    public void sendFile( int iToUserID, String sFilePath )
    {
		// Create the Path object and use it to read the file
		Path filePath    = Paths.get( sFilePath );
		byte[] bFileData = readFile( filePath );

		// If we have no data, do nothing
		if( bFileData == null )
			return;

		// Extract the FileName
		String sFileName = filePath.getFileName().toString();
			
		// Now we can send the data directly
		m_MessageManager.sendFile( iToUserID, sFileName, bFileData );
    }

	
	// Internally used to read a known file and return the data in a byte array
	private byte[] readFile( Path filePath )
	{
		byte[] bFileData = null;

		try
		{		
			bFileData = Files.readAllBytes( filePath );	
		}
		catch( Exception e )
		{
			System.out.println( "Unable to read file" );
		} 

		return bFileData;
	}


	// -- RECEIVING FILES -- //

	
	// Used to create and save a file (Currently saved in Files directory)
	public String saveFile( String sFileName, byte[] bFileData )
	{
        String sFilePath  = FOLDER_NAME + "\\" + sFileName;

        boolean bCreated = false;
        int iFileNum     = 0;

        File newFile;

		try
		{		
			// Loop until we create the new empty file
			while( !bCreated )
			{
				newFile = new File( sFilePath );

				// If the File already exists, increment the filenum and create a new filepath
				if( newFile.exists() )
				{
					iFileNum++;
					sFilePath = FOLDER_NAME + "\\" + iFileNum + sFileName;
				}
				else
				{
					bCreated = true;
					newFile.createNewFile();
				}
			}

			// Create the output stream to the file
			FileOutputStream fileOutStream = new FileOutputStream( sFilePath );

			// Write to the file and close the stream        
			fileOutStream.write( bFileData );
			fileOutStream.close();
		}
		catch( Exception e )
		{
			System.out.println( "Unable to save file" );
		} 
		
		return sFilePath;
	}
}
