
public class InputQueueHandler4B5 extends Thread
{
	public void run( )
	{
		try
		{
			while(true)
			{
				if(BrokerFive.inQueue.size()>0)
				{
					//read one msg from the inQueue
					String readMsg = BrokerFive.inQueue.get(0);
					//delete the already read msg from the inQueue
					BrokerFive.inQueue.remove(0);
					//msgType is the first element of msg
					String msgType = readMsg.split(",")[0];
					if(msgType.equals("SRT"))
						BrokerFive.inQueueMsgExecutionSRT(readMsg);
					else if(msgType.equals("PRT"))
						BrokerFive.inQueueMsgExecutionPRT(readMsg);
					else if(msgType.equals("SEARCH"))
						BrokerFive.inQueueMsgExecutionSEARCH(readMsg);
				}//END: main if inside while loop
				else
					Thread.sleep(1000);			
			}//END: while loop	
		}
		catch(Exception e){e.printStackTrace( );}
	}
}
