
public class OutputQueueHandler extends Thread
{
	public void run( )
	{
		try
		{
			while(true)
			{
				if(Broker.outQueue.size()>0)
				{
					//read one msg from the outQueue
					String readMsg = Broker.outQueue.get(0);
					//then delete the msg after reading
					Broker.outQueue.remove(0);
					String msgType = readMsg.split(",")[0];
					if(msgType.equals("SRT"))
						Broker.outQueueMsgForwardSRT(readMsg);
					else if(msgType.equals("PRT"))
						Broker.outQueueMsgForwardPRT(readMsg);
					else if(msgType.equals("SEARCH"))
						Broker.outQueueMsgForwardSEARCH(readMsg);
				}
				else
					Thread.sleep(1000);			
			}//END: while loop			
		}//END: try case
		catch(Exception e){e.printStackTrace( );}
	}//END: run()
}
