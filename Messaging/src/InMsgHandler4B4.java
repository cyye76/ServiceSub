import java.io.BufferedReader;

class InMsgHandler4B4 extends Thread
{
	String nbIndex;//neighbor index
	InMsgHandler4B4(String nbIndex)
	{ 
		this.nbIndex = nbIndex; 
	}
	public void run( )
	{
		try
		{
			BufferedReader br = new BufferedReader(BrokerFour.neighbors.get(nbIndex).is);
			String msg;
			while(true)
			{	
				if(br.ready())//if next msg is ready for receiving
				{
					msg = br.readLine();
					BrokerFour.inQueue.add(msg);
				}
				else
					Thread.sleep(1000);	
			}			
		}
		catch(Exception e){e.printStackTrace( );}
	}
}