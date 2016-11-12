import java.util.*;
import java.util.concurrent.Semaphore;

public class ConversationManager implements ConversationCallBack
{
	List<Conversation> m_ConversationList;
	MessageManager m_MessageManager;
	Semaphore m_Semaphore;
	
	
	// Constructor
	ConversationManager( MessageManager messageManager )
	{
		// Create Class Variables
		m_ConversationList = new ArrayList<Conversation>();
		m_MessageManager   = messageManager;
		m_Semaphore        = new Semaphore( 1, true );
	}
	
	
	// Closes all conversations - Callbacks will cleanup the list - Thread Safe
	public void closeAll()
	{
		List<Conversation> convoList = getConversationList();
	
		// Loop through the set of conversations and close all
		for( int iConvoNum = 0; iConvoNum < convoList.size(); iConvoNum++ )
			convoList.get( iConvoNum ).close();
			
		// Empty the Conversation List
		try
		{
			m_Semaphore.acquire();
			
			try
			{
				m_ConversationList = new ArrayList<Conversation>();
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
	
	
	
	// Privately used to create a new conversation and at it to the list
	public void createConversation( int iConversationID, int iWriteLevel )
	{
		// First check for a duplicate - If found, bring to front
		if( !bringConvoToFront( iConversationID ) )
		{		
			// Create the new conversation
			Conversation newConvo = new Conversation( iConversationID, iWriteLevel, this, m_MessageManager );
			
			// Now add the conversation to our list
			addConversation( newConvo );
		}
	}
	
	
	// Add a Conversation to the List - Thead Safe
	private void addConversation( Conversation newConvo )
	{
		try
		{
			m_Semaphore.acquire();
			
			try
			{
				// Add the new conversation safely
				m_ConversationList.add( newConvo );
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
	
	
	// Brings a conversation to the front if it exists
	private boolean bringConvoToFront( int iConvoID )
	{
		List<Conversation> convoList = getConversationList();
		Conversation convo;
		
		// Loop through the set of conversations
		for( int iConvoNum = 0; iConvoNum < convoList.size(); iConvoNum++ )
		{
			convo = convoList.get( iConvoNum );
			 
			// If we find the correct window, bring it to the front
			if( convo.getConversationID() == iConvoID )
			{
				convo.bringToFront();
				
				// Return found
				return true;
			}
		}
		
		// Return not found
		return false;
	} 
	
	
	// Passes a given message on to the correct conversation - Thread Safe
	public void addMessage( int iConvoID, String sMessage )
	{
		try
		{
			m_Semaphore.acquire();
		
			try
			{
                // Loop through the list of conversations
                for( int iConvoIndex = 0; iConvoIndex < m_ConversationList.size(); iConvoIndex++ )
                {
                    // Check the ID of each Conversation
                    if( m_ConversationList.get( iConvoIndex ).getConversationID() == iConvoID )
                    {
                        m_ConversationList.get( iConvoIndex ).addMessage( sMessage );
  
                        break;            
                    }
                }
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
	
		
	// Passes a given log on to the correct conversation - Thread Safe
	public void addLog( int iConvoID, String sMessage )
	{
		try
		{
			m_Semaphore.acquire();
		
			try
			{
                // Loop through the list of conversations
                for( int iConvoIndex = 0; iConvoIndex < m_ConversationList.size(); iConvoIndex++ )
                {
                    // Check the ID of each Conversation
                    if( m_ConversationList.get( iConvoIndex ).getConversationID() == iConvoID )
                    {
                        m_ConversationList.get( iConvoIndex ).addLog( sMessage );
  
                        break;            
                    }
                }
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
	
	
	// Get the list of Conversations - Thread Safe
	public List<Conversation> getConversationList()
	{
		try
		{
			m_Semaphore.acquire();
			
			try
			{
				return m_ConversationList;
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
        
        return null;
	}
	
	
	// Callback function to close a conversation window gracefully
	public void closeConversation( Conversation closedConversation )
	{
		try
		{
			m_Semaphore.acquire();
			
			try
			{
				m_ConversationList.remove( closedConversation );
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
}