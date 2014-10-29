import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
	JSpinner spinner;
	SpinnerNumberModel spinNumber;
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
		
		spinNumber = new SpinnerNumberModel(log_hours, 1, 24, 1);
		spinner = new JSpinner(spinNumber);
		
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
		
		
		/* insert first log time and date */		
		 String sql;
		 
		 sql = "INSERT INTO TIMERLOG(first_login_date_time,week_of_year,day_of_week,logout_time) VALUES("
		 		+ "'" + objTL.getTimeAsString(objFirstLogin,"hh:mm:ss") + "'"
		 		+ ","
		 		+  objTL.getLogWeekDetails("year")
		 		+ ","
		 		+ objTL.getLogWeekDetails("day")
		 		+ ","
		 		+"'00:00:00'"
		 		+ ")";		
		 
		 try {
			tdb.setTableDetails(sql);
		} catch (SQLException e3) {
			// TODO Auto-generated catch block
			//e3.printStackTrace();
		}
		 
		 /* set current week id
		  * NOTE:save table primary key value
		  *  
		  *  */
		try {
			log_table_current_week_id = tdb.getCurrentWeekRecordId();
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			//e2.printStackTrace();
		}		
		
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
				recordLastLogin();				
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
		
		frm.pack();
		frm.setAlwaysOnTop(true);
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
		frm.setMinimumSize(new Dimension(500,200));
		
		/* add items to frame */		
		addComponent(lbFirstLogin,lbFirstLoginTime,frm.getContentPane());
		lbFirstLoginTime.setText(objTL.getTimeAsString(objFirstLogin, "hh:mm:ss a"));
		
		addComponent(lbLogout,lbLogoutTime,frm.getContentPane());
		lbLogoutTime.setText(objTL.getTimeAsString(objLogout, "hh:mm:ss a"));
		
		addComponent(lbToday,lbTodayTime,frm.getContentPane());
		lbTodayTime.setText(objTL.getTimeAsString(objTodayLog, "HH:mm:ss"));		
		
		addComponent(lbWeekLog,lbWeekLogTime,frm.getContentPane());
		try {
			week_time_mills = tdb.getWeekLog();
			lbWeekLogTime.setText(objTL.prepareWeekLogFromMills(week_time_mills));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		
		//addComponent(spinner,btnSetMaxLog,frm.getContentPane());
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
		}else if(curr_button.contains("Set Maximum Log hours")){
			int max_log = (int) spinner.getValue();
			objLogout = (Calendar) objFirstLogin.clone();
			objLogout.add(Calendar.HOUR_OF_DAY,max_log);
			lbLogoutTime.setText(objTL.getTimeAsString(objLogout, "hh:mm:ss a"));
			log_hours = max_log;
		}
	}
	
	public void addMenuItem() throws SQLException{
		 Statement stmt = null;
		 ResultSet rs = null;
		 
		 try{
			 
			 menu.removeAll();
			 
			 TimerDB.c =  DriverManager.getConnection(tdb.connector); 
			 stmt = TimerDB.c.createStatement();
			 
			 rs = stmt.executeQuery( "SELECT pause_time,resume_time FROM PAUSERESUMELOG WHERE timer_log_id = " + log_table_current_week_id );
			 
			 while( rs.next()){
				menuitem = new JMenuItem(rs.getString("pause_time") + "-" + rs.getString("resume_time"));
				menu.add(menuitem);				
			 }
			 
			 rs.close();
			
			 
		 } catch( Exception e){
		 } finally{
			 stmt.close();
			 TimerDB.c.close();
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
		String sql, pause_time, resume_time;
		
		stop_timer = false;
		
		//subtract today's log pause time
		objTodayLog.add(Calendar.SECOND,-1 * (int)time_stop_secs);
		
		//add logout pause time
		objLogout.add(Calendar.SECOND, (int)time_stop_secs);
		lbLogoutTime.setText(objTL.getTimeAsString(objLogout, "hh:mm:ss a"));
		
		//subtract from week log
		week_time_mills -=  (int)time_stop_secs * time_millis;
		
		/* set resume time */
		objCurrentTime = Calendar.getInstance();
		resume_time = objTL.getTimeAsString(objCurrentTime, "hh:mm:ss a");
		
		/* set pause time */
		objCurrentTime.add(Calendar.SECOND,-1*(int)time_stop_secs);
		pause_time = objTL.getTimeAsString(objCurrentTime, "hh:mm:ss a");
		
		sql = "INSERT INTO PAUSERESUMELOG(timer_log_id,pause_time,resume_time) VALUES("
				 + log_table_current_week_id 
				 + ","
				 + "'" + pause_time + "'"
				 +","
				 + "'" + resume_time + "'"
				 + ")";	
		
		try {
			tdb.setTableDetails(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		
		try {
			addMenuItem();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}	
		
		btnPause.setEnabled(true);
		btnResume.setEnabled(false);

	}
	
	public void recordLastLogin(){
		String sql;
		
		Calendar objCurrentInstance = Calendar.getInstance();
		 
		sql = "UPDATE TIMERLOG SET logout_time = " 
				+ "'" + objTL.getTimeAsString(objCurrentInstance, "hh:mm:ss") + "'"
				+ "WHERE ID = "
				+ log_table_current_week_id;
		 				
		 
		 try {
			tdb.setTableDetails(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		 
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
