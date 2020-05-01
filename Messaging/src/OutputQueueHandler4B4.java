
public class OutputQueueHandler4B4 extends Thread
{
	public void run( )
	{
		try
		{
			while(true)
			{
				if(BrokerFour.outQueue.size()>0)
				{
					//read one msg from the outQueue
					String readMsg = BrokerFour.outQueue.get(0);
					//then delete the msg after reading
					BrokerFour.outQueue.remove(0);
					String msgType = readMsg.split(",")[0];
					if(msgType.equals("SRT"))
						BrokerFour.outQueueMsgForwardSRT(readMsg);
					else if(msgType.equals("PRT"))
						BrokerFour.outQueueMsgForwardPRT(readMsg);
					else if(msgType.equals("SEARCH"))
						BrokerFour.outQueueMsgForwardSEARCH(readMsg);
				}
				else
					Thread.sleep(1000);			
			}//END: while loop			
		}
		catch(Exception e){e.printStackTrace( );}
	}//END: run()
}
