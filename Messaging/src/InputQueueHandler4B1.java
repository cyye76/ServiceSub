
public class InputQueueHandler4B1 extends Thread
{
	public void run( )
	{
		try
		{
			while(true)
			{
				if(BrokerOne.inQueue.size()>0)
				{
					//read one msg from the inQueue
					String readMsg = BrokerOne.inQueue.get(0);
					//delete the already read msg from the inQueue
					BrokerOne.inQueue.remove(0);
					//msgType is the first element of msg
					String msgType = readMsg.split(",")[0];
					if(msgType.equals("SRT"))
						BrokerOne.inQueueMsgExecutionSRT(readMsg);
					else if(msgType.equals("PRT"))
						BrokerOne.inQueueMsgExecutionPRT(readMsg);
					else if(msgType.equals("SEARCH"))
						BrokerOne.inQueueMsgExecutionSEARCH(readMsg);
				}//END: main if inside while loop
				else
					Thread.sleep(1000);			
			}//END: while loop	
		}
		catch(Exception e){e.printStackTrace( );}
	}
}
