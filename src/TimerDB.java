import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class TimerDB{	
	static Connection c = null;
	TimerLib objTL = new TimerLib();
	String connector = "jdbc:sqlite:timer.db";
	
	TimerDB(){
		try{
			Class.forName("org.sqlite.JDBC");
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	public void createTables() throws SQLException{
		 Statement stmt = null;
		 String sql = "";
		 
		  try {
			c = DriverManager.getConnection(connector); 
			stmt = c.createStatement();
			
			sql =  "CREATE TABLE if not exists TIMERLOG(ID INTEGER PRIMARY KEY AUTOINCREMENT"					
					+ ",first_login_date_time TEXT"
					+ ",logout_time TEXT"
					+ ",week_of_year INT"
					+ ",day_of_week INT) ";
			
			stmt.execute(sql);
			
			sql = 	 "CREATE TABLE if not exists PAUSERESUMELOG(ID INTEGER PRIMARY KEY AUTOINCREMENT"					
					+ ",timer_log_id INT"
					+ ",pause_time TEXT"
					+ ",resume_time TEXT)";
								
			stmt.execute(sql);	
					
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			stmt.close();
			c.close();
		}
		 
	}
	
	public void setTableDetails(String sql) throws SQLException{
		
		 Statement stmt = null;
		 
		try{
			
			c = DriverManager.getConnection(connector); 
			stmt = c.createStatement();
			
			stmt.executeUpdate(sql);			
			
		} catch( Exception e){
			
			e.printStackTrace();
		} finally {
			stmt.close();
			c.close();
		}
		
	}
	
	public int getCurrentWeekRecordId() throws SQLException{
		
		 Statement stmt = null;
		 ResultSet rs = null;
		 int record_id = 0;
		 
		 try{
			 
			 c = DriverManager.getConnection(connector); 
			 stmt = c.createStatement();
			 
			 rs = stmt.executeQuery( "SELECT ID FROM TIMERLOG WHERE week_of_year = " + objTL.getLogWeekDetails("year") + " and day_of_week = " + objTL.getLogWeekDetails("day") );
			 
			 while( rs.next()){
				 record_id = rs.getInt("ID");
			 }
			 
			 rs.close();
			 
			 
		 } catch( Exception e){
			 
			 e.printStackTrace();
		 } 	finally{
			 stmt.close();
			 c.close();
		 }
		 
		 return record_id;
		
	}
	
	public long getWeekLog() throws SQLException{
		
		 Statement stmt = null;
		 ResultSet rs = null;
		 Calendar objFirstLogin = Calendar.getInstance(),objLogout = Calendar.getInstance();
		 String login_time = null,logout_time = null;
		 SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		 long millis = 0;
		 
		 
		 
		 try{
			 
			 c = DriverManager.getConnection(connector); 
			 stmt = c.createStatement();
			 
			 rs = stmt.executeQuery( "SELECT first_login_date_time, logout_time  FROM TIMERLOG WHERE week_of_year = " + objTL.getLogWeekDetails("year") );
			 
			 while( rs.next()){
				 login_time = rs.getString("first_login_date_time");
				 logout_time = rs.getString("logout_time");
				 				 
				objFirstLogin.setTime(sdf.parse(login_time));				
				objLogout.setTime(sdf.parse(logout_time));
				
				if(!logout_time.equals("00:00:00"))				
					millis  = millis + (objLogout.getTimeInMillis() - objFirstLogin.getTimeInMillis());			
				
			 }
			 
			 rs.close();
			 
			 
		 } catch( Exception e){
			 
			 e.printStackTrace();
		 } finally{
			 stmt.close();
			 c.close();
		 }
	
		return millis;
	}	
	
	public void clearTables() throws SQLException{

		 Statement stmt = null;
		 
		 
		 try{		 

			 c = DriverManager.getConnection(connector); 
			 stmt = c.createStatement();
			 
			 stmt.execute("drop table if exists TimerLOG");
			 stmt.execute("drop table if exists PAUSERESUMELOG");	
			 

		 } catch( Exception e){
			 e.printStackTrace();
		 } finally{
			 stmt.close();
			 c.close();
		 }
	}
	
	public boolean isTableCreated() throws SQLException{
		boolean retVal = true;
		
		 Statement stmt = null;
		 ResultSet rs = null;
		 
		 try{		 

			 c = DriverManager.getConnection(connector); 
			 stmt = c.createStatement();
			 
			 rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='TimerLOG'");
			 
			 if(!rs.next()){
				 retVal = false;
			 }

			 rs.close();			

		 } catch( Exception e){
			 e.printStackTrace();
		 } finally{
			 stmt.close();
			 c.close();
		 }

		
		return retVal;
	}
	
	public void deleteRecordBeforeTwomonths()  throws SQLException{
		Calendar sDateCalendar = new GregorianCalendar();
		int week_num = 0;
		Statement stmt = null;
		ResultSet rs = null;
		
		sDateCalendar.add(Calendar.MONTH, -2);
		week_num = sDateCalendar.get(Calendar.WEEK_OF_YEAR);
		
		try{
			
			c = DriverManager.getConnection(connector); 
			stmt = c.createStatement();
			
			 rs = stmt.executeQuery("SELECT ID FROM TimerLOG WHERE week_of_year BETWEEN " + week_num + " AND " + objTL.getLogWeekDetails("year"));
			 
			 while(rs.next()){
				 stmt.execute("DELETE FROM PAUSERESUMELOG WHERE timer_log_id = " + rs.getInt("ID"));
				 stmt.execute("DELETE FROM TimerLOG WHERE ID = " + rs.getInt("ID"));
			 }
			
			
		} catch(Exception e){
			 e.printStackTrace();
		}
		finally{
			 rs.close();
			 stmt.close();
			 c.close();
		 }
		
	}
	
}
