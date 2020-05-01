
public class OutputQueueHandler4B1 extends Thread
{
	public void run( )
	{
		try
		{
			while(true)
			{
				if(BrokerOne.outQueue.size()>0)
				{
					//read one msg from the outQueue
					String readMsg = BrokerOne.outQueue.get(0);
					//then delete the msg after reading
					BrokerOne.outQueue.remove(0);
					String msgType = readMsg.split(",")[0];
					if(msgType.equals("SRT"))
						BrokerOne.outQueueMsgForwardSRT(readMsg);
					else if(msgType.equals("PRT"))
						BrokerOne.outQueueMsgForwardPRT(readMsg);
					else if(msgType.equals("SEARCH"))
						BrokerOne.outQueueMsgForwardSEARCH(readMsg);
				}
				else
					Thread.sleep(1000);			
			}//END: while loop			
		}
		catch(Exception e){e.printStackTrace( );}
	}//END: run()
}
