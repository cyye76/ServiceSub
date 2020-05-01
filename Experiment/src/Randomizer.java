import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

public class Randomizer
{

     public static void main(String []args)
     {
        int numOfEntry = 40;//total entry in a file
        int numOfPub = 20;//total pub in a overlay network
        int numOfSub = 20;//total sub in a overlay network
        int counterPubIndex = 0;//counter for maintaining pubIndex upto numOfPub
        int counterSubIndex = 0;//counter for maintaining subIndex upto numOfSub
        String[] pubORsub = {"P","S"};
        String[] taskList = {"t1","t2","t3","t4","t5","t6","t7","t8","t9","t10"};
        String[] brokerList = {"B1","B2","B3","B4","B5","B6","B7"}; 
        
        ArrayList<String> listOfPub = new ArrayList<String>();
        int lengthOfPub = 5;
        int lengthOfSub = 5;
        Random randomPub = new Random();
        for(int p = 0;p<numOfPub;p++)
        {
        	String str = "";
        	int in = (randomPub.nextInt(lengthOfPub))+1;
	        for(int j=0;j<in;j++)
            {
               str += "," + taskList[randomPub.nextInt(taskList.length)];
            }
	        listOfPub.add(str);
        }
		try
		{
			//write dataset into dataset.txt
			String dsFileName = "dataset.txt";
			String dsFileNameWithFullPath = getFileNameWithFullPath(dsFileName);
			File dsFile = new File(dsFileNameWithFullPath);
			PrintWriter out = new PrintWriter(new FileWriter(dsFile));
		
	        Random random = new Random();
	        int index = 0;
            out.println("WAIT,1");
	        for(int i = 0;i<numOfEntry;i++)
	        {
	            //input type and serial number of entry
	            out.print("IN," + (i+1) +",");
	            //responsible broker of entry
	            index = random.nextInt(brokerList.length);
	            out.print(brokerList[index] +",");
	            //pubIndex or subIndex of entry
	            index = random.nextInt(pubORsub.length);
	            //START: if: pub
	            if(pubORsub[index].equals("P") && numOfPub > counterPubIndex)
	            {
	                out.print("SRT," + pubORsub[index] + (counterPubIndex+1));
	                out.print(listOfPub.get(counterPubIndex++));
	            }//END: if: pub
	            //START: else if: sub
	            else if(pubORsub[index].equals("S") && numOfSub > counterSubIndex)
	            {
	                out.print("PRT," + pubORsub[index] + (counterSubIndex+1));
	                counterSubIndex++;
	                int inLength = (random.nextInt(lengthOfSub))+1;
	                for(int z=0;z<inLength;z++)
                    {
                       out.print(listOfPub.get(random.nextInt(listOfPub.size())));
                    }
	            }//END: else if: sub
	            else
	            {
	                if(numOfPub <= counterPubIndex)
	                {
	                    out.print("PRT," + pubORsub[1] + (counterSubIndex+1));
	                    counterSubIndex++;
		                int inLength = (random.nextInt(lengthOfSub))+1;
		                for(int z=0;z<inLength;z++)
	                    {
	                       out.print(listOfPub.get(random.nextInt(listOfPub.size())));
	                    }
	                }
	                else if(numOfSub <= counterSubIndex)
	                {
	                    out.print("SRT," + pubORsub[0] + (counterPubIndex+1));
		                out.print(listOfPub.get(counterPubIndex++));
	                }
	            }//END: else    
	            out.println();
	        }//END: for loop
	        //initial waiting time for a broker
            out.println("OUT," + (numOfEntry+1));
	        out.close();//close printWriter
	        System.out.println("Dataset generated successfully!!!");
	        /*for(int k = 0; k < brokerList.length; k++)
	        {
	        	String dsFileName4Broker = "dataset" + brokerList[k] + ".txt";
				String dsFileName4BrokerWithFullPath = getFileNameWithFullPath(dsFileName4Broker);
				File dsFile4Broker = new File(dsFileName4BrokerWithFullPath);
				//dsFile.renameTo(dsFile4Broker);

				System.out.println("Dataset generated successfully for " + brokerList[k]);
	        }*/
		}//END: try case
        catch(IOException e){e.printStackTrace();}
     }//END: main() method
     
 	//START: getFileNameWithFullPath()*****************************************************
 	//This method return the file name with full path
     public static String getFileNameWithFullPath(String fileName)
 	{
 		String fileNameWithFullPath = "";
 		String workingDir = System.getProperty("user.dir");
     	String your_os = System.getProperty("os.name").toLowerCase();
     	//System.out.println(workingDir +" and "+ your_os);
     	if(your_os.indexOf("win") >= 0)
     	{
     		fileNameWithFullPath = workingDir + "\\textfile\\" + fileName;
     	}
     	else if(your_os.indexOf( "nix") >=0 || your_os.indexOf( "nux") >=0)
     	{
     		fileNameWithFullPath = workingDir + "/textfile/" + fileName;
     	}
     	else
     	{
     		fileNameWithFullPath = workingDir + "{others}" + fileName;
     	}
     	return fileNameWithFullPath;
 	}//END: getFileNameWithFullPath()
 	//END: getFileNameWithFullPath()*****************************************************
     
}//END: class RandonGenerator