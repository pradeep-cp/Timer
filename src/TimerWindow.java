import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;import java.net.URL;

import java.util.Calendar;
import java.util.TimerTask;

import javax.swing.*; 

import com.sun.jna.WString;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.WNDCLASSEX;
import com.sun.jna.platform.win32.WinUser.WindowProc;
import com.sun.jna.platform.win32.Wtsapi32;
import com.sun.jna.platform.win32.WinUser.MSG;

public class TimerWindow extends TimerTask implements ActionListener {
	/* common variables*/
	JLabel lbFirstLogin,lbFirstLoginTime,lbLogout,lbLogoutTime,lbToday,lbTodayTime,lbWeekLog,lbWeekLogTime;
	JButton btnPause,btnResume,btnSetMaxLog;
	JMenuBar menuBar;
	JMenu menu;
	JMenuItem menuitem;	
	TimerLib objTL;
	Calendar objLogout ;
	Calendar objTodayLog ;	
	GridLayout gl = new GridLayout();
	TimerDB tdb;
	SystemTray tray = null;	
	URL url = getClass().getResource("images/Timer.png");
	Image image = Toolkit.getDefaultToolkit().getImage(url);
	TrayIcon trayIcon = new TrayIcon(image, "Timer", new PopupMenu("Timer"));
	
	/* final variables section */
	final static Calendar objFirstLogin = Calendar.getInstance();
	final int time_second = 1;
	final int time_millis = 1000;
	
	/* static variable section */
	static JFrame frm;
	static int log_hours = 8;	
	static boolean stop_timer = false;
	static long time_stop_secs = 0;
	static int log_table_current_week_id = 0;
	static long week_time_mills = 0;
		
	
	TimerWindow(){
		frm = new JFrame();
		lbFirstLogin = new JLabel("First Login");
		lbFirstLoginTime = new JLabel();
		lbLogout = new JLabel("Logout");
		lbLogoutTime = new JLabel();
		lbToday = new JLabel("Today's Log");
		lbTodayTime = new JLabel();
		lbWeekLog = new JLabel("Week log");
		lbWeekLogTime = new JLabel();		
		
		btnPause = new JButton("Pause");
		btnResume = new JButton("Resume");
		btnSetMaxLog = new JButton("Set Maximum Log hours");
		btnPause.addActionListener(this);
		btnResume.addActionListener(this);
		btnSetMaxLog.addActionListener(this);
		btnResume.setEnabled(false);		
		btnSetMaxLog.setPreferredSize(new Dimension(10, 10));
		
		objLogout = (Calendar) objFirstLogin.clone();
		objTodayLog = Calendar.getInstance();
		objTodayLog.set(Calendar.HOUR_OF_DAY,0);
		objTodayLog.set(Calendar.MINUTE,0);
		objTodayLog.set(Calendar.SECOND,0);
		
		menuBar = new JMenuBar();
		menu = new JMenu("Pause-Resume Log");	
		
		//set logout time
		objLogout.add(Calendar.HOUR_OF_DAY,log_hours);	
		
		/* thread for window lock/unlock listener */
		Thread th = new Thread(new Runnable(){			
			public void run(){
				new WorkstationLockListening();
			}
		});		
		th.start();
	}
	
	
	public void run(){
		//set today's log
		if(!stop_timer){
			objTodayLog.add(Calendar.SECOND,time_second);
			week_time_mills += time_millis;
			
			//show time increment
			lbTodayTime.setText(objTL.getTimeAsString(objTodayLog, "HH:mm:ss"));			
			lbWeekLogTime.setText(objTL.prepareWeekLogFromMills(week_time_mills));
		}else{
			time_stop_secs += 1;
		}
		
		if((objTodayLog.get(Calendar.HOUR)==log_hours) && (objTodayLog.get(Calendar.MINUTE)==0) && (objTodayLog.get(Calendar.SECOND))==0){
			
			JOptionPane.showMessageDialog(null, log_hours + " hours completed" , "Timer Info" , JOptionPane.INFORMATION_MESSAGE);			
			trayIcon.displayMessage("Timer Info", log_hours + " hours completed", TrayIcon.MessageType.INFO);
		}
						
	}
	
	public void init(){
		
		
		/* set library object */
		createSupportClassObjects();	
		
		menuBar.add(menu);
		frm.setJMenuBar(menuBar);
		
		/* set frame components */
		setFrameComponents();		
		
		frm.addWindowListener(new WindowAdapter(){			
			@Override
			public void windowIconified(WindowEvent e) {				
				if (SystemTray.isSupported()) {
					tray = SystemTray.getSystemTray();					
					try {
						tray.add(trayIcon);
						frm.setVisible(false);						
					} catch (AWTException e1) {						
					}
				}
				
			}
			
			public void windowClosing(WindowEvent e) {
				
				if(JOptionPane.showConfirmDialog (null, "Are you sure you want to close timer?","Timer Confirm",JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
					recordLastLogin();
					System.exit(0);				
				}
			}

						
		});
		
		trayIcon.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e){
				
				frm.setVisible(true);
				if(tray != null){
					SystemTray.getSystemTray().remove(trayIcon);					
					tray = null;
				}	
				
			}
		});		
		
		frm.setTitle("Welcome " + objTL.getSystemName().toUpperCase());		
		frm.pack();
		frm.setAlwaysOnTop(true);
		frm.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frm.setResizable(false);
		frm.setVisible(true);

	}
	
	public void addComponent(Component c1,Component c2,Container c){
		c.add(c1);
		c.add(c2);
	}
	
	public void setFrameComponents(){
		
		/* configure grid layout */
		gl.setRows(5);
		gl.setColumns(1);		
		
		/* configure grid layout to jframe */
		frm.getContentPane().setLayout(gl);
		frm.setMinimumSize(new Dimension(300,200));
		
		/* add items to frame */		
		addComponent(lbFirstLogin,lbFirstLoginTime,frm.getContentPane());
		lbFirstLoginTime.setText(objTL.getTimeAsString(objFirstLogin, "hh:mm:ss a"));
		
		addComponent(lbLogout,lbLogoutTime,frm.getContentPane());
		lbLogoutTime.setText(objTL.getTimeAsString(objLogout, "hh:mm:ss a"));
		
		addComponent(lbToday,lbTodayTime,frm.getContentPane());
		lbTodayTime.setText(objTL.getTimeAsString(objTodayLog, "HH:mm:ss"));		
		
		addComponent(lbWeekLog,lbWeekLogTime,frm.getContentPane());
		try {
			week_time_mills = objTL.getWeekLog();
			lbWeekLogTime.setText(objTL.prepareWeekLogFromMills(week_time_mills));
		} catch (Exception e) {
			
		}		
		
		addComponent(btnPause,btnResume,frm.getContentPane());	

	}
	
	public void createSupportClassObjects(){
		objTL = new TimerLib();
		tdb = new TimerDB();

	}
	
	public void actionPerformed(ActionEvent e){		
		String curr_button = ((JButton) e.getSource()).getActionCommand();
				
		if(curr_button.contains("Pause")){
			pauseTimer();		
						
		}else if(curr_button.contains("Resume")){
			resumeTimer();
		}
	}	
	
	public void pauseTimer(){
		stop_timer = true;
		time_stop_secs = 0;	
		
		btnPause.setEnabled(false);
		btnResume.setEnabled(true);
	}
	
	public void resumeTimer(){
		Calendar objCurrentTime = null;
		String pause_time, resume_time;
		
		stop_timer = false;		
		
		//add pause time to logout
		objLogout.add(Calendar.SECOND, (int)time_stop_secs);
		lbLogoutTime.setText(objTL.getTimeAsString(objLogout, "hh:mm:ss a"));
		
		/* set resume time */
		objCurrentTime = Calendar.getInstance();
		resume_time = objTL.getTimeAsString(objCurrentTime, "hh:mm:ss a");
		
		/* set pause time */
		objCurrentTime.add(Calendar.SECOND,-1*(int)time_stop_secs);
		pause_time = objTL.getTimeAsString(objCurrentTime, "hh:mm:ss a");
		
		
		menuitem = new JMenuItem(pause_time + "-" + resume_time);
		menu.add(menuitem);			
		
		btnPause.setEnabled(true);
		btnResume.setEnabled(false);

	}
	
	public void recordLastLogin(){
		String FilePath = System.getenv("SystemDrive") + "//" + "OfficeTimerLog" + "//" + "WeekLog_" + objTL.getLogWeekDetails("year") + ".txt";
				
		objTL.writeToFile(FilePath, String.valueOf(week_time_mills));
		 
	}	
	
	public class WorkstationLockListening implements WindowProc
	{

	    /**
	     * Instantiates a new win32 window test.
	     */
	    public WorkstationLockListening()
	    {
	        // define new window class
	        final WString windowClass = new WString("TimerMain");
	        final HMODULE hInst = Kernel32.INSTANCE.GetModuleHandle("");

	        WNDCLASSEX wClass = new WNDCLASSEX();
	        wClass.hInstance = hInst;
	        wClass.lpfnWndProc = WorkstationLockListening.this;
	        wClass.lpszClassName = windowClass;

	        // register window class
	        User32.INSTANCE.RegisterClassEx(wClass);	       

	        // create new window
	        final HWND hWnd = User32.INSTANCE.CreateWindowEx(User32.WS_EX_TOPMOST, windowClass, "'TimeTracker hidden helper window to catch Windows events", 0, 0, 0, 0, 0, null, // WM_DEVICECHANGE contradicts parent=WinUser.HWND_MESSAGE
	                null, hInst, null);

	        Wtsapi32.INSTANCE.WTSRegisterSessionNotification(hWnd, Wtsapi32.NOTIFY_FOR_THIS_SESSION);

	        MSG msg = new MSG();
	        while (User32.INSTANCE.GetMessage(msg, hWnd, 0, 0) != 0)
	        {
	            User32.INSTANCE.TranslateMessage(msg);
	            User32.INSTANCE.DispatchMessage(msg);
	        }

	           
	    }

	    /*
	     * (non-Javadoc)
	     * 
	     * @see com.sun.jna.platform.win32.User32.WindowProc#callback(com.sun.jna.platform .win32.WinDef.HWND, int, com.sun.jna.platform.win32.WinDef.WPARAM, com.sun.jna.platform.win32.WinDef.LPARAM)
	     */
	    public LRESULT callback(HWND hwnd, int uMsg, WPARAM wParam, LPARAM lParam)
	    {
	        switch (uMsg)
	        {
	            case WinUser.WM_DESTROY:
	            {
	                User32.INSTANCE.PostQuitMessage(0);
	                return new LRESULT(0);
	            }
	            case WinUser.WM_SESSION_CHANGE:
	            {
	                this.onSessionChange(wParam, lParam);
	                return new LRESULT(0);
	            }
	            default:
	                return User32.INSTANCE.DefWindowProc(hwnd, uMsg, wParam, lParam);
	        }
	    }

	    /**
	     * Gets the last error.
	     * 
	     * @return the last error
	     */
	    public int getLastError()
	    {
	        int rc = Kernel32.INSTANCE.GetLastError();

	        if (rc != 0)
	            System.out.println("error: " + rc);

	        return rc;
	    }

	    /**
	     * On session change.
	     * 
	     * @param wParam
	     *            the w param
	     * @param lParam
	     *            the l param
	     */
	    protected void onSessionChange(WPARAM wParam, LPARAM lParam)
	    {
	        switch (wParam.intValue())
	        {
	            case Wtsapi32.WTS_SESSION_LOCK:
	            {
	                this.onMachineLocked(lParam.intValue());
	                break;
	            }
	            case Wtsapi32.WTS_SESSION_UNLOCK:
	            {
	                this.onMachineUnlocked(lParam.intValue());
	                break;
	            }	            
	        }
	    }

	    /**
	     * On machine locked.
	     * 
	     * @param sessionId
	     *            the session id
	     */
	    protected void onMachineLocked(int sessionId)
	    {
	        pauseTimer();
	    }

	    /**
	     * On machine unlocked.
	     * 
	     * @param sessionId
	     *            the session id
	     */
	    protected void onMachineUnlocked(int sessionId)
	    {
	        resumeTimer();
	    }
	}


	
}
