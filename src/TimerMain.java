import java.util.Timer;

public class TimerMain  {

	public static void main(String[] args) {
		
		
		TimerLib obj = new TimerLib();
		obj.createTimerFileFolder();
		
		final TimerWindow TW = new TimerWindow();
		TW.init();
		
		Runtime.getRuntime().addShutdownHook(new Thread(){			
			public void run(){
				TW.recordLastLogin();				
			}						
		});
		
		/* thread for timer increment by seconds */
		Thread thTim = new Thread(new Runnable(){
			public void run(){
				/* call timer in each 1 second */
				Timer tim = new Timer();
				tim.scheduleAtFixedRate(TW, 0, 1000 );	
			}
		});
		
		thTim.start();	
	}

}

