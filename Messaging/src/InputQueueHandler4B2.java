
public class InputQueueHandler4B2 extends Thread
{
	public void run( )
	{
		try
		{
			while(true)
			{
				if(BrokerTwo.inQueue.size()>0)
				{
					//read one msg from the inQueue
					String readMsg = BrokerTwo.inQueue.get(0);
					//delete the already read msg from the inQueue
					BrokerTwo.inQueue.remove(0);
					//msgType is the first element of msg
					String msgType = readMsg.split(",")[0];
					if(msgType.equals("SRT"))
						BrokerTwo.inQueueMsgExecutionSRT(readMsg);
					else if(msgType.equals("PRT"))
						BrokerTwo.inQueueMsgExecutionPRT(readMsg);
					else if(msgType.equals("SEARCH"))
						BrokerTwo.inQueueMsgExecutionSEARCH(readMsg);
				}//END: main if inside while loop
				else
					Thread.sleep(1000);			
			}//END: while loop		
		}
		catch(Exception e){e.printStackTrace( );}
	}//END: run()
}
