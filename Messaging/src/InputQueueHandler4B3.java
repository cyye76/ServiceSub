
public class InputQueueHandler4B3 extends Thread
{
	public void run( )
	{
		try
		{
			while(true)
			{
				if(BrokerThree.inQueue.size()>0)
				{
					//read one msg from the inQueue
					String readMsg = BrokerThree.inQueue.get(0);
					//delete the already read msg from the inQueue
					BrokerThree.inQueue.remove(0);
					//msgType is the first element of msg
					String msgType = readMsg.split(",")[0];
					if(msgType.equals("SRT"))
						BrokerThree.inQueueMsgExecutionSRT(readMsg);
					else if(msgType.equals("PRT"))
						BrokerThree.inQueueMsgExecutionPRT(readMsg);
					else if(msgType.equals("SEARCH"))
						BrokerThree.inQueueMsgExecutionSEARCH(readMsg);
				}//END: main if inside while loop
				else
					Thread.sleep(1000);			
			}//END: while loop	
		}
		catch(Exception e){e.printStackTrace( );}
	}
}
