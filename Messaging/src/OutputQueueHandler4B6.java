
public class OutputQueueHandler4B6 extends Thread
{
	public void run( )
	{
		try
		{
			while(true)
			{
				if(BrokerSix.outQueue.size()>0)
				{
					//read one msg from the outQueue
					String readMsg = BrokerSix.outQueue.get(0);
					//then delete the msg after reading
					BrokerSix.outQueue.remove(0);
					String msgType = readMsg.split(",")[0];
					if(msgType.equals("SRT"))
						BrokerSix.outQueueMsgForwardSRT(readMsg);
					else if(msgType.equals("PRT"))
						BrokerSix.outQueueMsgForwardPRT(readMsg);
					else if(msgType.equals("SEARCH"))
						BrokerSix.outQueueMsgForwardSEARCH(readMsg);
				}
				else
					Thread.sleep(1000);			
			}//END: while loop			
		}
		catch(Exception e){e.printStackTrace( );}
	}//END: run()
}
