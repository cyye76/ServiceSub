
public class OutputQueueHandler4B5 extends Thread
{
	public void run( )
	{
		try
		{
			while(true)
			{
				if(BrokerFive.outQueue.size()>0)
				{
					//read one msg from the outQueue
					String readMsg = BrokerFive.outQueue.get(0);
					//then delete the msg after reading
					BrokerFive.outQueue.remove(0);
					String msgType = readMsg.split(",")[0];
					if(msgType.equals("SRT"))
						BrokerFive.outQueueMsgForwardSRT(readMsg);
					else if(msgType.equals("PRT"))
						BrokerFive.outQueueMsgForwardPRT(readMsg);
					else if(msgType.equals("SEARCH"))
						BrokerFive.outQueueMsgForwardSEARCH(readMsg);
				}
				else
					Thread.sleep(1000);			
			}//END: while loop			
		}
		catch(Exception e){e.printStackTrace( );}
	}//END: run()
}
