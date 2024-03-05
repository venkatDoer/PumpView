package doer.pv;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import doer.io.Database;
import doer.io.MyResultSet;

/**
 * @author Venkatesan Selvaraj @ Doer
 */

// class to work on application configuration values
public class Configuration {

	// global configuration parameters with its default values
	// params which are stored and managed using a config table
	// GLOBAL
	public static String LINE_NAME = "Line 1";
	public static String LICENCEE_NAME = "DOER PUMPS (PVT) LIMITED";
	public static String LICENCEE_ADR_1 = "1234 STREET";
	public static String LICENCEE_ADR_2 = "CITY - 123 456.";
	public static String LICENCEE_ISI_REF_LIST = "";
	public static String LICENCEE_ISI_REF = "";
	public static String NUMBER_OF_LINES = "1";
	public static String ISSTD_LIST = "8472-1989,8472-2002";
	public static String LAST_USED_ISSTD = "8472";
	public static boolean IS_ISSTD_CHANGED = false;
	public static String REG_CODE = ""; 
	public static String APP_DB_NAME = ""; 
	public static String IS_REMOTE_DB = "NO"; 
	public static boolean IS_TRIAL_ON = false; 
	public static String SPEED_TESTER_TYPE = "S";
	public static Double CUR_PIPE_CONST = 0.0;
	public static String IS_PERF_BASED_ON_GRAPH_FOR_ROUTINE = "NO";
	public static String IS_MULTI_DB = "NO";
	public static String APP_SINGLE_DB_NAME = "";
	public static String APP_DB_NAME_LIST = "";
	
	// DEVICES
	public static String LAST_USED_STATION = "Station1";
	public static HashMap<String, String> OP_PRESSURE_VAC_OUTPUT = new HashMap<String, String>();
	public static HashMap<String, String> OP_FLOW_OUTPUT = new HashMap<String, String>();
	public static HashMap<String, String> OP_PUMP_TYPE = new HashMap<String, String>();
	public static String LAST_USED_PRES_CTRL_PORT = "";
	public static Integer NUMBER_OF_STATION = 1;
	
	// PUMP PARAMS
	public static String LAST_USED_PUMP_TYPE_ID = "1";
	public static String LAST_USED_PUMP_TYPE = "SAMPLE PUMP MODEL";
	public static String LAST_USED_PUMP_DESC = "";
	
	// CAPTURE
	public static String TEST_MODE = "A";
	public static String MULTI_STATION_MODE = "NO";
	public static String LAST_USED_SIGNAL_METHOD = "K";
	
	public static String IS_SNO_GLOBAL= "YES";
	public static String LAST_USED_PUMP_SNO = "00000";
	public static String LAST_USED_MOTOR_SNO = "00000";
	public static String IS_BARCODE_USED = "NO";
	public static String IS_MODEL_FROM_BARCODE = "NO";
	public static String MODEL_START_POS_IN_BARCODE = "1";
	public static String MODEL_END_POS_IN_BARCODE = "5";
	public static String HEAD_ADJUSTMENT = "M";
	public static String IS_DB_LOCKED = "0";
	public static String IS_CONFIRM_SNO = "NO";
	
	// OPTIONAL TESTS
	public static String IS_SP_TEST_ON = "0";

	// BACKUP
	public static String LAST_USED_BACKUP_DURATION = "7";
	public static String LAST_USED_BACKUP_LOCATION = "";
	public static String LAST_BACKUP_DATE = "NA";
	public static String NEXT_BACKUP_DATE = "";
	
	// REPORTS
	public static String REP_SHOW_APP_WMARK = "1";
	public static String REP_SHOW_CASING_TEST = "1";
	public static String REP_CASTING_TEST_MIN = "30 Secs";
	public static String REP_SHOW_NOTES = "0";
	public static String REP_NOTES_HEADING = "Note:";
	public static String REP_NOTES_TEXT = "Your note goes here";
	public static String REP_SHOW_TESTER_NAME = "1";
	public static String REP_SHOW_VERIFIED_BY = "1";
	public static String REP_SHOW_VERIFIED_BY_NAME = "";
	public static String REP_SHOW_APPROVED_BY = "1";
	public static String REP_SHOW_APPROVED_BY_NAME = "";
	public static String REP_SHOW_PUMP_EFF = "0";
	public static String REP_SHOW_MOT_EFF = "1";
	public static String REP_SHOW_CUST_REP = "0";
	public static String REP_SHOW_MI_FOR_8472 = "1";
	
	public static String REP_COLOR_OUTLINE = "0";
	public static String REP_COLOR_HEADER = "0";
	public static String REP_COLOR_LABEL = "0";
	public static String REP_COLOR_DECLARED = "0";
	public static String REP_COLOR_ACTUAL = "255"; // blue
	
	// ISI perf tolerance
	public static Float ISI_PERF_HEAD_LL = 0F;
	public static Float ISI_PERF_HEAD_UL = 99999F;
	public static Float ISI_PERF_DIS_LL = 0F;
	public static Float ISI_PERF_DIS_UL = 99999F;
	public static Float ISI_PERF_CUR_LL = 0F;
	public static Float ISI_PERF_CUR_UL = 99999F;
	public static Float ISI_PERF_POW_LL = 0F;
	public static Float ISI_PERF_POW_UL = 99999F;
	public static Float ISI_PERF_EFF_LL = 0F;
	public static Float ISI_PERF_EFF_UL = 99999F;
	
	// other global params
	public static String APP_DIR = "c:";
	public static String APP_AUTHOR = "Doer";
	public static String APP_VERSION = "PumpView V 2.0";
	public static String CONFIG_DIR = "\\config";
	public static String CONFIG_FILE_LIC = "\\app.lic";
	public static String DATA_DIR = "\\data";
	//public static Long MAXIMUM_ALLOWED_READINGS = 9223372036854775806L;
	
	public static Float CUR_SUC_LIFT = 0F;
	public static String USER = "";
	public static String USER_IS_ADMIN = "0";
	public static String USER_HAS_PUMP_ACCESS = "0";
	public static String USER_HAS_MODIFY_ACCESS = "0";
	public static boolean APP_DEBUG_MODE = false;
	
	public static String JDBC_DRIVER = "org.sqlite.JDBC";
	public static String DB_NAME = "\\pumpview.db";
	public static String JDBC_NAME = "jdbc:sqlite";
	public static String DB_URL = "";
	public static boolean IS_VFD = false;
	
	// app ISI specific table names	
	public static String PUMPTYPE = "PUMPTYPE";
	public static String TESTNAMES_ALL = "TESTNAMES_ALL";
	public static String TESTNAMES_ROUTINE = "TESTNAMES_ROUTINE";
	public static String TESTNAMES_TYPE = "TESTNAMES_TYPE";
	public static String ROUTINE_LIMITS = "ROUTINE_LIMITS";
	public static String MOT_EFF_DATA_POINTS = "MOT_EFF_DATA_POINTS";
	public static String NON_ISI_PERF_TOLERANCE = "NON_ISI_PERF_TOLERANCE";
	public static String OUTPUT = "OUTPUT";
	public static String READING_DETAIL = "READING_DETAIL";
	public static String CUSTOM_REPORT = "CUSTOM_REPORT";
	public static String OPTIONAL_TEST = "OPTIONAL_TEST";
	
	// constructor
	public Configuration() {
	}
	
	// initialize
	public static boolean initialize() {
		// create config table if the software runs first time
		try {
			if (APP_DB_NAME.isEmpty()) {
				APP_DB_NAME = APP_DIR + DATA_DIR + DB_NAME;
				
				// create DB directory if it does not exist (may happen when software runs first time)
				File dbDir = new File(APP_DIR + DATA_DIR);
				if (!dbDir.exists()) {
					dbDir.mkdir();
				}
				APP_SINGLE_DB_NAME = APP_DB_NAME;
			} else {
				// check for db file name, if it does not exist alert and use default one
				File dbFile = new File(APP_DB_NAME);
				if (!dbFile.exists()) {
					JOptionPane.showMessageDialog(new JDialog(), "Unable to find last used database file:" + APP_DB_NAME + "; Hence, using the default one");
					APP_DB_NAME = APP_DIR + DATA_DIR + DB_NAME;
				} 
			}
			DB_URL = JDBC_NAME + ":" + APP_DB_NAME;
			
			Database db = Database.getInstance();
			
			MyResultSet res = null;
			boolean needInsert = false;
			try {
				res = db.executeQuery("select * from CONFIG where line='" + LINE_NAME +"'");
			} catch (SQLException se) {
				if (se.getMessage().contains("no such table")) {
					db.executeUpdate("create table CONFIG (line text, name text, value text)");
					needInsert = true;
				}
			}
			if (res == null || !res.isRowsAvail()) {
				needInsert = true;
			}
			
			if (needInsert) {
				db.executeUpdate("insert into CONFIG values ('" + LINE_NAME + "','LICENCEE_NAME','" + LICENCEE_NAME + "')");
				db.executeUpdate("insert into CONFIG values ('" + LINE_NAME + "','LICENCEE_ADR_1','" + LICENCEE_ADR_1 + "')");
				db.executeUpdate("insert into CONFIG values ('" + LINE_NAME + "','LICENCEE_ADR_2','" + LICENCEE_ADR_2 + "')");
				db.executeUpdate("insert into CONFIG values ('" + LINE_NAME + "','LICENCEE_ISI_REF_LIST','" + LICENCEE_ISI_REF_LIST + "')");
				db.executeUpdate("insert into CONFIG values ('" + LINE_NAME + "','LICENCEE_ISI_REF','" + LICENCEE_ISI_REF + "')");
				db.executeUpdate("insert into CONFIG values ('" + LINE_NAME + "','NUMBER_OF_LINES','" + NUMBER_OF_LINES + "')");
				db.executeUpdate("insert into CONFIG values ('" + LINE_NAME + "','ISSTD_LIST','" + ISSTD_LIST + "')");
				db.executeUpdate("insert into CONFIG values ('" + LINE_NAME + "','LAST_USED_ISSTD','" + LAST_USED_ISSTD + "')");
				db.executeUpdate("insert into CONFIG values ('" + LINE_NAME + "','APP_DB_NAME','" + APP_DB_NAME  + "')");
				db.executeUpdate("insert into CONFIG values ('" + LINE_NAME + "','LAST_USED_STATION','" + LAST_USED_STATION + "')");
				db.executeUpdate("insert into CONFIG values ('" + LINE_NAME + "','LAST_USED_PRES_CTRL_PORT','" + LAST_USED_PRES_CTRL_PORT + "')");
				db.executeUpdate("insert into CONFIG values ('" + LINE_NAME + "','LAST_USED_PUMP_TYPE_ID','" + LAST_USED_PUMP_TYPE_ID + "')");
				db.executeUpdate("insert into CONFIG values ('" + LINE_NAME + "','LAST_USED_PUMP_TYPE','" + LAST_USED_PUMP_TYPE + "')");
				db.executeUpdate("insert into CONFIG values ('" + LINE_NAME + "','TEST_MODE','" + TEST_MODE + "')");
				db.executeUpdate("insert into CONFIG values ('" + LINE_NAME + "','IS_SP_TEST_ON','" + IS_SP_TEST_ON + "')");
				db.executeUpdate("insert into CONFIG values ('" + LINE_NAME + "','HEAD_ADJUSTMENT','" + HEAD_ADJUSTMENT + "')");
				db.executeUpdate("insert into CONFIG values ('" + LINE_NAME + "','IS_DB_LOCKED','" + IS_DB_LOCKED + "')");
				db.executeUpdate("insert into CONFIG values ('" + LINE_NAME + "','MULTI_STATION_MODE','" + MULTI_STATION_MODE + "')");
				db.executeUpdate("insert into CONFIG values ('" + LINE_NAME + "','LAST_USED_BACKUP_DURATION','" + LAST_USED_BACKUP_DURATION  + "')");
				LAST_USED_BACKUP_LOCATION = APP_DIR + "\\backup";
				db.executeUpdate("insert into CONFIG values ('" + LINE_NAME + "','LAST_USED_BACKUP_LOCATION','" + LAST_USED_BACKUP_LOCATION + "')");
				db.executeUpdate("insert into CONFIG values ('" + LINE_NAME + "','LAST_BACKUP_DATE','" + LAST_BACKUP_DATE + "')");
				db.executeUpdate("insert into CONFIG values ('" + LINE_NAME + "','NEXT_BACKUP_DATE','" + NEXT_BACKUP_DATE + "')");
				db.executeUpdate("insert into CONFIG values ('" + LINE_NAME + "','REP_SHOW_APP_WMARK','" + REP_SHOW_APP_WMARK + "')");
				db.executeUpdate("insert into CONFIG values ('" + LINE_NAME + "','REP_SHOW_CASING_TEST','" + REP_SHOW_CASING_TEST + "')");
				db.executeUpdate("insert into CONFIG values ('" + LINE_NAME + "','REP_SHOW_NOTES','" + REP_SHOW_NOTES + "')");
				db.executeUpdate("insert into CONFIG values ('" + LINE_NAME + "','REP_NOTES_HEADING','" + REP_NOTES_HEADING + "')");
				db.executeUpdate("insert into CONFIG values ('" + LINE_NAME + "','REP_NOTES_TEXT','" + REP_NOTES_TEXT + "')");
				db.executeUpdate("insert into CONFIG values ('" + LINE_NAME + "','REP_SHOW_TESTER_NAME','" + REP_SHOW_TESTER_NAME + "')");
				db.executeUpdate("insert into CONFIG values ('" + LINE_NAME + "','REP_SHOW_VERIFIED_BY','" + REP_SHOW_VERIFIED_BY + "')");
				db.executeUpdate("insert into CONFIG values ('" + LINE_NAME + "','REP_SHOW_VERIFIED_BY_NAME','" + REP_SHOW_VERIFIED_BY_NAME + "')");
				db.executeUpdate("insert into CONFIG values ('" + LINE_NAME + "','REP_SHOW_APPROVED_BY','" + REP_SHOW_APPROVED_BY + "')");
				db.executeUpdate("insert into CONFIG values ('" + LINE_NAME + "','REP_SHOW_APPROVED_BY_NAME','" + REP_SHOW_APPROVED_BY_NAME + "')");
				db.executeUpdate("insert into CONFIG values ('" + LINE_NAME + "','REP_SHOW_PUMP_EFF','" + REP_SHOW_PUMP_EFF + "')");
				db.executeUpdate("insert into CONFIG values ('" + LINE_NAME + "','REP_SHOW_CUST_REP','" + REP_SHOW_CUST_REP + "')");
				db.executeUpdate("insert into CONFIG values ('" + LINE_NAME + "','REP_SHOW_MI_FOR_8472','" + REP_SHOW_MI_FOR_8472 + "')");
				db.executeUpdate("insert into CONFIG values ('" + LINE_NAME + "','REP_COLOR_OUTLINE','" + REP_COLOR_OUTLINE + "')");
				db.executeUpdate("insert into CONFIG values ('" + LINE_NAME + "','REP_COLOR_HEADER','" + REP_COLOR_HEADER + "')");
				db.executeUpdate("insert into CONFIG values ('" + LINE_NAME + "','REP_COLOR_LABEL','" + REP_COLOR_LABEL + "')");
				db.executeUpdate("insert into CONFIG values ('" + LINE_NAME + "','REP_COLOR_DECLARED','" + REP_COLOR_DECLARED + "')");
				db.executeUpdate("insert into CONFIG values ('" + LINE_NAME + "','REP_COLOR_ACTUAL','" + REP_COLOR_ACTUAL + "')");
			}
		} catch (SQLException sqle) {
			JOptionPane.showMessageDialog(new JDialog(), "Error creating config table:" + sqle.getMessage() + "\nDB Name:" + APP_DB_NAME);
			return false;
		}
		return true;
	}
	
	// function to load configurations from config file and set other globals
	public static boolean loadConfigValues() {
		// open db and load the config values;
		try {
			Database db = Database.getInstance();
			
			// fetch and load the result
			MyResultSet res = db.executeQuery("select * from CONFIG where line='" + LINE_NAME +"'");
			
			String configName = "";
			String configValue = "";
			
			while (res.next()) {
				configName = res.getString("name");
				configValue = res.getString("value");
				System.out.println(configName + ":" + configValue);
				try {
					Configuration.class.getField(configName).set(null, configValue);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(new JDialog(), "Error loading config:" + e.getMessage());
					continue;
				}
			}
		} catch (Exception sqle) {
			JOptionPane.showMessageDialog(new JDialog(), "Error loading config:" + sqle.getMessage());
			return false;
		}
		return true;
	}
	
	// function to save set of config values
	public static void saveConfigValues(String ... names) {
		try {
			Database db = Database.getInstance();
			
			String qry = null;
			for (int i=0; i<names.length; i++) {
				qry = "update CONFIG set value='" + Configuration.class.getField(names[i]).get(null) + "' where line = '" + LINE_NAME + "' and name='" + names[i] + "'";
				if (db.executeUpdate(qry) == 0) {
					// config. entry is missed, probably due to upgrade. Hence, insert it
					db.executeUpdate("insert into CONFIG values ('" + LINE_NAME + "','" + names[i] + "','" + Configuration.class.getField(names[i]).get(null) + "')");
				} else {
					// System.out.println(qry);
				}
			}
		} catch (Exception sqle) {
			JOptionPane.showMessageDialog(new JDialog(), "Error updating config:" + sqle.getMessage());
			return;
		}
	}
	
	// function to save config values that are common across assembly lines
	public static void saveCommonConfigValues(String ... names) {
		try {
			Database db = Database.getInstance();
			String qry = null;
			for (int i=0; i<names.length; i++) {
				qry = "update CONFIG set value='" + Configuration.class.getField(names[i]).get(null) + "' where name='" + names[i] + "'";
				if (db.executeUpdate(qry) == 0) {
					// config. entry is missed, probably due to upgrade. Hence, insert it
					db.executeUpdate("insert into CONFIG values ('" + LINE_NAME + "','" + names[i] + "','" + Configuration.class.getField(names[i]).get(null) + "')");
				}
			}
		} catch (Exception sqle) {
			JOptionPane.showMessageDialog(new JDialog(), "Error updating config:" + sqle.getMessage());
			return;
		}
	}
	
	// function to set and get next SNo
	public static HashMap<String, String> setAndGetSNo(String pumpTypeId) {
		Database db = Database.getInstance();
		String nextPumpSNo = "";
		String nextMotSNo = "";
		HashMap<String, String> snoPair = new HashMap<String, String>();
		try {
			// wait for your turn and mark your turn
			while(db.executeUpdate("update CONFIG set value='1' where name='IS_DB_LOCKED'") == 0) {
				// continue
			}
			// auto generate based on global sno or model specific sno
			if (Configuration.IS_SNO_GLOBAL.equals("YES")) {
				// global snos
				nextPumpSNo = findNextNo(Configuration.LAST_USED_PUMP_SNO);
				nextMotSNo = findNextNo(Configuration.LAST_USED_MOTOR_SNO);
			} else {
				// model specific snos
				MyResultSet res = db.executeQuery("select recent_pump_sno, recent_motor_sno from " + Configuration.PUMPTYPE + " where pump_type_id=" + pumpTypeId);
				if (res.next()) {
					nextPumpSNo = findNextNo(res.getString("recent_pump_sno"));
					nextMotSNo = findNextNo(res.getString("recent_motor_sno"));
				}
			}
			
			// confirm sno before assigning if configured so
			if (Configuration.IS_CONFIRM_SNO.equals("YES")) {
				String tmpPumpSNo = null;
				String tmpMotorSNo = null;
				while (tmpPumpSNo == null || tmpMotorSNo == null || tmpPumpSNo.trim().isEmpty() || tmpMotorSNo.trim().isEmpty()) {
					tmpPumpSNo = JOptionPane.showInputDialog("Confirm Pump SNo.", nextPumpSNo);
					tmpMotorSNo = JOptionPane.showInputDialog("Confirm Motor SNo.", tmpPumpSNo);
				}
				nextPumpSNo = tmpPumpSNo;
				nextMotSNo = tmpMotorSNo;
			}
			
			// update it back with new no
			if (!Configuration.IS_SNO_GLOBAL.equals("YES")) {
				db.executeUpdate("update " + Configuration.PUMPTYPE + " set recent_pump_sno='" + nextPumpSNo + "', recent_motor_sno='" + nextMotSNo + "' where pump_type_id=" + pumpTypeId); 
			}
			Configuration.LAST_USED_PUMP_SNO = nextPumpSNo;
			Configuration.LAST_USED_MOTOR_SNO = nextMotSNo;
			Configuration.saveCommonConfigValues("LAST_USED_PUMP_SNO", "LAST_USED_MOTOR_SNO");
			
			// release the lock as I am done with my turn
			db.executeUpdate("update CONFIG set value='0' where name='IS_DB_LOCKED'");
			
			snoPair.put("pump_sno", nextPumpSNo);
			snoPair.put("motor_sno", nextMotSNo);
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(new JDialog(), "Error setting serial numbers: " + e.getMessage());
		}
		return snoPair;
	}
	
	// find next number
	public static String findNextNo(String lastUsedSNo) {
		String nextSno = "";
		Long lastNoPlusOne = 0L;
		
		// 1.split the string to numbers and strings
		String[] lstStr = lastUsedSNo.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
		// 2. increment the last instance of number by 1
		for (int i=lstStr.length-1; i>= 0; i--) {
			try {
				lastNoPlusOne = Long.parseLong(lstStr[i]) + 1;
				DecimalFormat formatNumber = new DecimalFormat(lstStr[i].replaceAll("\\d","0"));
				lstStr[i] = formatNumber.format(lastNoPlusOne);
				break;
			} catch (NumberFormatException e) {
				continue;
			}
		}
		// 3. join the numbers
		for(int i=0; i<lstStr.length; i++) {
			nextSno += lstStr[i];
		}
		if (lastNoPlusOne == 0) { // unable to find numbers in given string
			nextSno += "1"; // default
		}
		return nextSno;
	}
	
	// find prev number
	public static String findPrevNo(String lastUsedSNo) {
		String nextSno = "";
		Long lastNoMinusOne = 0L;
		
		// 1.split the string to numbers and strings
		String[] lstStr = lastUsedSNo.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
		// 2. increment the last instance of number by 1
		for (int i=lstStr.length-1 ; i>= 0; i--) {
			try {
				lastNoMinusOne = Long.parseLong(lstStr[i]) - 1;
				lastNoMinusOne = lastNoMinusOne > 0 ? lastNoMinusOne : 0;
				DecimalFormat formatNumber = new DecimalFormat(lstStr[i].replaceAll("\\d","0"));
				lstStr[i] = formatNumber.format(lastNoMinusOne);
				break;
			} catch (NumberFormatException e) {
				continue;
			}
		}
		// 3. join the numbers
		for(int i=0; i<lstStr.length; i++) {
			nextSno += lstStr[i];
		}
		return nextSno;
	}
	
	// find potential sno from given string
	public static String parseSNo(String lastUsedSNo) {
		String sno = "0";
		Long tmpNo = 0L;
		// 1.split the string to numbers and strings
		String[] lstStr = lastUsedSNo.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
		// 2. find the last instance of number
		for (int i=lstStr.length-1; i>= 0; i--) {
			try {
				tmpNo = Long.parseLong(lstStr[i]);
				DecimalFormat formatNumber = new DecimalFormat(lstStr[i].replaceAll("\\d","0"));
				sno = formatNumber.format(tmpNo);
				break;
			} catch (NumberFormatException e) {
				continue;
			}
		}
		return sno;
	}
	
	// function to take a back up of data base
	public static void backupData(String bkDir) throws Exception {
		// create back up dir if not exist
		File dirBkDir = new File(bkDir);
		
		if (!dirBkDir.exists()) {;
			boolean res = (new File(bkDir)).mkdir();
			if (!res) {
				throw new Exception ("Error: Unable to create backup folder:" + bkDir);
			}
		}
		
		// take a backup of db
		String sourceFile = APP_DB_NAME;
		java.util.Date today = Calendar.getInstance().getTime();
		SimpleDateFormat reqDtFormat = new SimpleDateFormat("yyyyMMdd");
		String timeStamp = reqDtFormat.format(today);
		String destFile = bkDir + "\\" + "pumpviewpro" + timeStamp + ".db";
		
		String cmd = "cmd.exe /c echo F | xcopy /YV  \"" + sourceFile + "\" \"" + destFile + "\"";
		Process p = Runtime.getRuntime().exec(cmd);
		BufferedReader er = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String tmpStr = "";
		String filesError = "";
		
		// check for any error or output
		Thread.sleep(1000);
		if (er.ready()) {
			while ((tmpStr = er.readLine()) != null) {
				filesError += tmpStr + "\n";
			}
			if (!filesError.isEmpty()) {
				er.close();
				in.close();
				p.destroy();
				throw new Exception("Error while taking backup of " + sourceFile + ":" + filesError);
			}
		}
		// ignore the output if any
		Thread.sleep(1000);
		if (in.ready()) {
			while ((tmpStr = in.readLine()) != null) {
				filesError += tmpStr + "\n";
			}
		}
		
		
		er.close();
		in.close();
		p.destroy();
		
		// retain only recent five backups, remove rest of them
		File[] files = dirBkDir.listFiles();

		Arrays.sort(files, new Comparator<File>(){
			public int compare(File f1, File f2)
			{
				return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
			} 
		});
		
		int retainCopy = 5;
		for(int i=files.length-retainCopy-1; i>=0; i--) {
			files[i].delete();
		}
	}
}
