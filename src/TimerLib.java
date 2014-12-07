import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;


public class TimerLib {
	
	public String prepareTimerName(){
		String TimerName = "";
		
		try{
			
			TimerName = InetAddress.getLocalHost().getHostName();
			
		}catch(Exception e){
			
			//TODO COMMON ERROR HANDLING
		}
		
		return TimerName;
		
	}
	
	public String getTimeAsString(Calendar cal,String Format){
		String Time = "";
		
		SimpleDateFormat sdf = new SimpleDateFormat(Format);
		Time = sdf.format(cal.getTime());
		
		return Time;
	}	
	
	public int getLogWeekDetails(String item_type){
		 Calendar sDateCalendar = new GregorianCalendar();
		 int retVal = 0;
		 switch(item_type){
		 	case "year":
		 		retVal = sDateCalendar.get(Calendar.WEEK_OF_YEAR);
		 		break;
		 	case "day":
		 		retVal = sDateCalendar.get(Calendar.DAY_OF_WEEK);
		 		break;
		 }
		
		 return retVal;
	}
	
	public String prepareWeekLogFromMills(long millis){
		
		return String.format("%02d:%02d:%02d", 
				TimeUnit.MILLISECONDS.toHours(millis),
				TimeUnit.MILLISECONDS.toMinutes(millis) -  
				TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)), 
				TimeUnit.MILLISECONDS.toSeconds(millis) - 
				TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
		
	}
	
	public void createTimerFileFolder(){
		int currentWeek = getLogWeekDetails("year");
		int preWeek = currentWeek - 1;
		String folderName = "OfficeTimerLog";
		String weekLogFileName = "WeekLog_" + currentWeek + ".txt";
		String preWeekLogFileName = "WeekLog_" + preWeek + ".txt";		
		String fileFolderPath = System.getenv("SystemDrive") + "\\" + folderName;
		
		//create folder/		
		File theDir = new File(fileFolderPath);
		
		// if the directory does not exist, create it
		if (!theDir.exists()) {		
		
			try{
			    theDir.mkdir();			   
			 } catch(Exception se){				
			 }        
		 
		}
		
		// create weeklog text file		
		try {
			 
		      File file = new File(fileFolderPath + "\\" + weekLogFileName);
		      
		      if (file.createNewFile()){
		    	  File file1 = new File(fileFolderPath + "\\" + preWeekLogFileName);
		    	  if(file1.exists())
		    		  file1.delete();
		      }
	 
	    } catch (IOException e) {		     
		}		
		
	}
	
	public void writeToFile(String FileFullPath,String content){
		
		File file = new File(FileFullPath);
		
		FileWriter fw;
		try {
			
			fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();
			
		} catch (IOException e) {
			
		}
		
	}
	
	public long getWeekLog(){
		int currentWeek = getLogWeekDetails("year");
		String folderName = "OfficeTimerLog";
		String weekLogFileName = "WeekLog_" + currentWeek + ".txt";		
		String fileFolderPath = System.getenv("SystemDrive") + "\\" + folderName + "\\" + weekLogFileName;
		long retVal = 0;
		
		BufferedReader br = null;
		 
		try {
 
			String sCurrentLine;
 
			br = new BufferedReader(new FileReader(fileFolderPath));
 
			while ((sCurrentLine = br.readLine()) != null) {
				retVal = Long.parseLong(sCurrentLine);
			}
 
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
 
		
		return retVal;
	}
	
	public String getSystemName(){
		String computername = "";
		
		try {
			computername = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			
		}
		
		return computername;
	}
	
}
