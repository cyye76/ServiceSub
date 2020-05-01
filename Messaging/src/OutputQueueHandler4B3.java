
public class OutputQueueHandler4B3 extends Thread
{
	public void run( )
	{
		try
		{
			while(true)
			{
				if(BrokerThree.outQueue.size()>0)
				{
					//read one msg from the outQueue
					String readMsg = BrokerThree.outQueue.get(0);
					//then delete the msg after reading
					BrokerThree.outQueue.remove(0);
					String msgType = readMsg.split(",")[0];
					if(msgType.equals("SRT"))
						BrokerThree.outQueueMsgForwardSRT(readMsg);
					else if(msgType.equals("PRT"))
						BrokerThree.outQueueMsgForwardPRT(readMsg);
					else if(msgType.equals("SEARCH"))
						BrokerThree.outQueueMsgForwardSEARCH(readMsg);
				}
				else
					Thread.sleep(1000);			
			}//END: while loop			
		}
		catch(Exception e){e.printStackTrace( );}
	}//END: run()
}
