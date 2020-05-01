
public class InputQueueHandler4B4 extends Thread
{
	public void run( )
	{
		try
		{
			while(true)
			{
				if(BrokerFour.inQueue.size()>0)
				{
					//read one msg from the inQueue
					String readMsg = BrokerFour.inQueue.get(0);
					//delete the already read msg from the inQueue
					BrokerFour.inQueue.remove(0);
					//msgType is the first element of msg
					String msgType = readMsg.split(",")[0];
					if(msgType.equals("SRT"))
						BrokerFour.inQueueMsgExecutionSRT(readMsg);
					else if(msgType.equals("PRT"))
						BrokerFour.inQueueMsgExecutionPRT(readMsg);
					else if(msgType.equals("SEARCH"))
						BrokerFour.inQueueMsgExecutionSEARCH(readMsg);
				}//END: main if inside while loop
				else
					Thread.sleep(1000);			
			}//END: while loop	
		}
		catch(Exception e){e.printStackTrace( );}
	}
}
