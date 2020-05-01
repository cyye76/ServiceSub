
public class InputQueueHandler extends Thread
{
	public void run( )
	{
		try
		{
			while(true)
			{
				if(Broker.inQueue.size()>0)
				{
					//read one msg from the inQueue
					String readMsg = Broker.inQueue.get(0);
					//delete the already read msg from the inQueue
					Broker.inQueue.remove(0);
					//msgType is the first element of msg
					String msgType = readMsg.split(",")[0];
					if(msgType.equals("SRT"))
						Broker.inQueueMsgExecutionSRT(readMsg);
					else if(msgType.equals("PRT"))
						Broker.inQueueMsgExecutionPRT(readMsg);
					else if(msgType.equals("SEARCH"))
						Broker.inQueueMsgExecutionSEARCH(readMsg);
				}//END: main if inside while loop
				else
					Thread.sleep(1000);			
			}//END: while loop	
		}//END: try case
		catch(Exception e){e.printStackTrace( );}
	}
}
