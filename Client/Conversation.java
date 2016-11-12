import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.concurrent.Semaphore;

// Used to visually display a conversation window
public class Conversation
{
	// UI Variables
	JFrame m_Window;
	JPanel m_ContentPanel;
	JPanel m_OutputPanel;
	JPanel m_InputPanel;
	JScrollPane m_ScrollPane;
	JTextArea m_OutputMessages;
	JTextField m_InputMessage;

	// Class Variables
	int m_ConversationID;
	int m_WriteLevel;
	ConversationCallBack m_ConversationCallBack;
	MessageManager m_MessageManager;
	Semaphore m_Semaphore;
	
	
	// Constructor
	Conversation( int iConversationID, int iWriteLevel, ConversationCallBack callBack, MessageManager manager )
	{
		// Save Class Variables
		m_ConversationID       = iConversationID;
		m_WriteLevel           = iWriteLevel;
		m_ConversationCallBack = callBack;
		m_MessageManager       = manager;
		m_Semaphore            = new Semaphore( 1, true );	
		
		// Set up the UI
		initializeUI();  
		
		// Request the log
		m_MessageManager.sendLog( m_ConversationID );
		
		// Send a joined message
		String sMessage = "\n" + User.getUsername() + " has joined the conversation\n";
	
		// Send the message
		m_MessageManager.sendMessage( sMessage, m_ConversationID );
	}
	
	
	// Closes the window
	public void close()
	{
		m_Window.dispose();
	}
	
	
	// Returns the Conversation ID
	public int getConversationID()
	{
		return m_ConversationID;
	}
	
	
	// Bring this window to the front
	public void bringToFront()
	{
		m_Window.toFront();
        m_Window.repaint();
	}
	
	
	// Adds a new message to the output text - Thread Safe
	public void addMessage( String sMessage )
	{
		try
		{
			m_Semaphore.acquire();
			
			try
			{
				// Be safe, make sure we have a UI element
				if( ( m_OutputMessages == null ) || ( m_ScrollPane == null ) )
					return;

				// Put the new message on a new line, then create a gap
				m_OutputMessages.append( sMessage );
				
				// Get the most updated scrollbar
				m_Window.validate();
				
				// Set the scroll bar to the bottom
				m_ScrollPane.getVerticalScrollBar().setValue( m_ScrollPane.getVerticalScrollBar().getMaximum() );
			} 
			finally
			{
				m_Semaphore.release();
			}
		} 
		catch( InterruptedException ie )
		{
			// TODO - Report Interruption
		}
	}
	
	
	// Adds the log to the output text - Thread Safe
	public void addLog( String sLog )
	{
		try
		{
			m_Semaphore.acquire();
			
			try
			{
				// Be safe, make sure we have a UI element
				if( ( m_OutputMessages == null ) || ( m_ScrollPane == null ) )
					return;
					
				// Put the log at the top of the text area, then add an empty line
				m_OutputMessages.insert( sLog, 0 );
				
				// Get the most updated scrollbar
				m_Window.validate();
				
				// Set the scroll bar to the bottom
				m_ScrollPane.getVerticalScrollBar().setValue( m_ScrollPane.getVerticalScrollBar().getMaximum() );
			} 
			finally
			{
				m_Semaphore.release();
			}
		} 
		catch( InterruptedException ie )
		{
			// TODO - Report Interruption
		}
	}
	
	
	// -- UI SetUp Functions -- //
	
	
	// Initialize UI Components
	private void initializeUI()
	{		
		// First, initialize the output panel - Receives messages
		initializeOutputPanel();
		
		// Initialize the Input Panel
		initializeInputPanel();
		
		// Initialize the Window last
		initializeWindow();
	}

	
	// Initializes Output UI Components
	private void initializeOutputPanel()
	{
		// Create the TextArea that receives messages
		m_OutputMessages = new JTextArea( 30, 40 );
		m_OutputMessages.setMargin( new Insets( 10, 10, 10, 10 ) );
		m_OutputMessages.setEditable( false );
		m_OutputMessages.setFocusable( false );
		m_OutputMessages.setLineWrap( true );
		
		// Create the scroll area
		m_ScrollPane = new JScrollPane( m_OutputMessages );
		m_ScrollPane.setHorizontalScrollBarPolicy( ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
	
		// Create the output panel
		m_OutputPanel = new JPanel( new BorderLayout() );
		m_OutputPanel.setLayout( new BoxLayout( m_OutputPanel, BoxLayout.LINE_AXIS ) );
	
		// Add our components
		m_OutputPanel.add( m_ScrollPane );
	}

	
	// Initializes Input UI Components
	private void initializeInputPanel()
	{
		// Create the input panel
		m_InputPanel = new JPanel();
		m_InputPanel.setLayout( new BoxLayout( m_InputPanel, BoxLayout.LINE_AXIS ) );
		
		// Create out message input area
		m_InputMessage = new JTextField( 40 );
			
		// Create the send event
		m_InputMessage.addActionListener( new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				// Get the message
				String sMessage = m_InputMessage.getText();
				
                // Format the message
                sMessage = "\n" + User.getUsername() + ":\n" + sMessage + "\n";

				// Clear the Text Box
				m_InputMessage.setText( "" );
			
				// Send the message
				m_MessageManager.sendMessage( sMessage, m_ConversationID );
			}
		});
		
		// Add our components
		m_InputPanel.add( m_InputMessage );
	}
	
	
	// Initializes Window UI components
	private void initializeWindow()
	{		
		// Create the content panel
		m_ContentPanel = new JPanel();
		m_ContentPanel.setLayout( new BoxLayout( m_ContentPanel, BoxLayout.PAGE_AXIS ) );
		m_ContentPanel.add( m_OutputPanel );
		m_ContentPanel.add( Box.createVerticalStrut( 15 ) );
		
		// Hide the input panel if the user doesn't have a high enough write level
		if( User.getWriteLevel() >= m_WriteLevel )
			m_ContentPanel.add( m_InputPanel );
	
		// Create the Window
		m_Window = new JFrame( "Conversation - " + m_ConversationID );
		
		// Set up the Window Listener events
		m_Window.addWindowListener( new WindowAdapter()
		{			
			// On Window Close
			public void windowClosing( WindowEvent e )
			{				
				// End the Conversation gracefully here
				m_ConversationCallBack.closeConversation( Conversation.this );
			}
						
			// On Window Open
			public void windowOpened( WindowEvent e )
			{
				// The window should default to the input text box
				m_InputMessage.requestFocus();
			}
        });
	
		// Add the content panel
		m_Window.add( m_ContentPanel );
		
		// Pack the window
		m_Window.pack();
		
		// Set the window location
		m_Window.setLocationRelativeTo( null );	
		
		// Show the window
		m_Window.setVisible( true );
	}
}
