
public class OutputQueueHandler4B2 extends Thread
{
	public void run( )
	{
		try
		{
			while(true)
			{
				if(BrokerTwo.outQueue.size()>0)
				{
					//read one msg from the outQueue
					String readMsg = BrokerTwo.outQueue.get(0);
					//then delete the msg after reading
					BrokerTwo.outQueue.remove(0);
					String msgType = readMsg.split(",")[0];
					if(msgType.equals("SRT"))
						BrokerTwo.outQueueMsgForwardSRT(readMsg);
					else if(msgType.equals("PRT"))
						BrokerTwo.outQueueMsgForwardPRT(readMsg);
					else if(msgType.equals("SEARCH"))
						BrokerTwo.outQueueMsgForwardSEARCH(readMsg);
				}
				else
					Thread.sleep(1000);			
			}//END: while loop			
		}
		catch(Exception e){e.printStackTrace( );}
	}//END: run()
}
