import java.net.InetAddress;
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
	
}
