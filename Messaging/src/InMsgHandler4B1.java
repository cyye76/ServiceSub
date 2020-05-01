import java.io.BufferedReader;

class InMsgHandler4B1 extends Thread
{
	String nbIndex;//neighbor index
	InMsgHandler4B1(String nbIndex)
	{ 
		this.nbIndex = nbIndex; 
	}
	public void run( )
	{
		try
		{
			BufferedReader br = new BufferedReader(BrokerOne.neighbors.get(nbIndex).is);
			String msg;
			while(true)
			{	
				if(br.ready())//if next msg is ready for receiving
				{
					msg = br.readLine();
					BrokerOne.inQueue.add(msg);
				}
				else
					Thread.sleep(1000);	
			}			
		}
		catch(Exception e){e.printStackTrace( );}
	}
}