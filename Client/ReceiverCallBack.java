interface ReceiverCallBack
{
	void receiveMessage( int iConvoID, String sMessage );
	void receiveLogin( int iUserID, int iAccessLevel, int iWriteLevel ); 
	void receiveConvo( int iConvoID, String[] sUsers );
	void receiveJoin( int iConvoID, int iReadLevel, int iWriteLevel );
	void receiveFile( String sFilename, byte[] bFileData );
	void receiveStatus( int iStatusType, String sUser );
	void receiveLog( int iConvoID, String sLog );
}