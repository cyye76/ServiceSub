import java.io.BufferedReader;

class InMsgHandler extends Thread
{
	String nbIndex;//neighbor index
	InMsgHandler(String nbIndex)
	{ 
		this.nbIndex = nbIndex; 
	}
	public void run( )
	{
		try
		{
			BufferedReader br = new BufferedReader(Broker.neighbors.get(nbIndex).is);
			String msg;
			while(true)
			{	
				if(br.ready())//if next msg is ready for receiving
				{
					msg = br.readLine();
					Broker.inQueue.add(msg);
				}
				else
					Thread.sleep(1000);	
			}//END: while loop			
		}//END: try case
		catch(Exception e){e.printStackTrace( );}
	}//END: run() method
}//END: InMsgHandler class