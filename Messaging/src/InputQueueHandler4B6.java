
public class InputQueueHandler4B6 extends Thread
{
	public void run( )
	{
		try
		{
			while(true)
			{
				if(BrokerSix.inQueue.size()>0)
				{
					//read one msg from the inQueue
					String readMsg = BrokerSix.inQueue.get(0);
					//delete the already read msg from the inQueue
					BrokerSix.inQueue.remove(0);
					//msgType is the first element of msg
					String msgType = readMsg.split(",")[0];
					if(msgType.equals("SRT"))
						BrokerSix.inQueueMsgExecutionSRT(readMsg);
					else if(msgType.equals("PRT"))
						BrokerSix.inQueueMsgExecutionPRT(readMsg);
					else if(msgType.equals("SEARCH"))
						BrokerSix.inQueueMsgExecutionSEARCH(readMsg);
				}//END: main if inside while loop
				else
					Thread.sleep(1000);			
			}//END: while loop	
		}
		catch(Exception e){e.printStackTrace( );}
	}
}
