package doer.pv;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.jfree.ui.about.Licences;

import net.wimpi.modbus.procimg.Register;
import doer.io.Database;
import doer.io.Encrypt;
import doer.io.MyResultSet;
import doer.lic.LicenseFile;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// set windows theme
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(new JDialog(), "Unable to set theme: SystemLookAndFeel", "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}
				
		// DB class registration
		try {
			Class.forName(Configuration.JDBC_DRIVER);
		} catch (ClassNotFoundException e) {
			JOptionPane.showMessageDialog(new JDialog(), "DB Error:" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}
		
		// check for valid license
		try {
			Configuration.APP_DIR = (new File(".")).getCanonicalPath();
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(new JDialog(), "Unable to get current directory of the this application:" + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}
		
		String licFileName = Configuration.APP_DIR + Configuration.CONFIG_DIR + Configuration.CONFIG_FILE_LIC;
		
		LicenseFile lFile = null;
		try {
			lFile = new LicenseFile(licFileName);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(new JDialog(), e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}
		
		// set global values
		Configuration.LICENCEE_NAME = lFile.getCustName();
		Configuration.LICENCEE_ADR_1 = lFile.getCustAdr1();
		Configuration.LICENCEE_ADR_2 = lFile.getCustAdr2();
		Configuration.LICENCEE_ISI_REF_LIST = lFile.getISRefNo();
		Configuration.NUMBER_OF_LINES = lFile.getLicCount();
		Configuration.ISSTD_LIST = lFile.getLicISIs();
		Configuration.IS_TRIAL_ON = lFile.getTrialInfo().startsWith("YES:")?true:false;
		Configuration.REG_CODE = lFile.getRegCode();
		Configuration.APP_DB_NAME = lFile.getDbName();
		
		if (Configuration.APP_DB_NAME.startsWith("X:")) {
			Configuration.IS_REMOTE_DB = "YES";
		} else {
			Configuration.IS_REMOTE_DB = "NO";
		}
		
		boolean goReg = false;
		if (Configuration.REG_CODE.isEmpty()) {
			goReg = true;
		} else if (Configuration.IS_TRIAL_ON) {
			String lastEval = lFile.getTrialInfo().substring(lFile.getTrialInfo().indexOf(":")+1);
			int lastEvalDays = Integer.valueOf(lastEval.substring(0,lastEval.indexOf(":")));
			String lastEvalDt = lastEval.substring(lastEval.indexOf(":")+1);
			java.util.Date today = Calendar.getInstance().getTime();
			SimpleDateFormat reqDtFormat = new SimpleDateFormat("dd-MM-yyyy");
			String curDt = reqDtFormat.format(today);
			int evalPr = 30;
			int alertPr = 5;
			
			if (lastEvalDt.isEmpty() & Configuration.REG_CODE.isEmpty()) { // first time run
				goReg = true;
			} else { // check availability of trials
				if (!lastEvalDt.isEmpty() && !lastEvalDt.equals(curDt)){
					++lastEvalDays;
				}
				int availEv = evalPr-lastEvalDays;
				
				int res = -1;
				if (availEv <= 0) {
					res = 1; // to show exp message
				} else if (availEv <= alertPr) {
					res = JOptionPane.showConfirmDialog(new JDialog(), "You are using trail version of " + Configuration.APP_AUTHOR + " " + Configuration.APP_VERSION + "\n'Yes' to activate the full version\n'No' to evaluate the software (" + availEv + " " + (availEv>1?"days":"day") + " left of " + evalPr + " days trial period)", "Trial Alert", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				} else {
					res = 1;
				}
				
				if (res == 1) { // evaluation
					if (lastEvalDays >= evalPr) { // trial expired
						int res2 = JOptionPane.showConfirmDialog(new JDialog(), "Trail period (" + evalPr + " days) of the software was expired on " + lastEvalDt + ".\nDo you want to activate to full version now?", "Trail Alert", JOptionPane.YES_NO_OPTION);
						if (res2 == 0) {
							goReg = true;
							Configuration.IS_TRIAL_ON = false;
						} else {
							System.exit(0);
						}
					} else { // try for another day
						lastEvalDt = curDt;
						lFile.setTrialInfo("YES:" + lastEvalDays + ":" + lastEvalDt);
						try {
							lFile.rewriteFile();
						} catch (Exception e) {
							JOptionPane.showMessageDialog(new JDialog(), "Error continue evaluating the software, please contact Doer for support", "Error", JOptionPane.ERROR_MESSAGE);
							System.exit(0);
						}
						// continue with trial
					}
				} else if (res == 0){
					goReg = true;
					Configuration.IS_TRIAL_ON = false;
				} else {
					System.exit(0);
				}
			}
		}
		
		// software registration
		if (goReg) {
			// register
			String cmd[] = {"Register.exe", Configuration.APP_AUTHOR + " " + Configuration.APP_VERSION, (Configuration.IS_TRIAL_ON?"T":"F")};
			try {
				Process p = Runtime.getRuntime().exec(cmd);
				p.waitFor();
				System.exit(0);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(new JDialog(), "Error initializing software registration wizard:" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				System.exit(-1);
			}
		} else { // validate registration code
			try {
				String macAdr = findMACAddress();
			
				String uStr = Configuration.APP_AUTHOR + " " + Configuration.APP_VERSION + Configuration.LICENCEE_NAME + Configuration.LICENCEE_ADR_1 + ", " + Configuration.LICENCEE_ADR_2 + macAdr + (Configuration.IS_TRIAL_ON?"T":"F");
				uStr = uStr.replace(' ', '_');
				uStr = Encrypt.encryptMD5(uStr);
				
				if (!uStr.equals(Configuration.REG_CODE)) {
					JOptionPane.showMessageDialog(new JDialog(), "Software license file is corrupted or this computer is not same where software was originally registered\nPlease reinstall the software or contact Doer for support", "Error", JOptionPane.ERROR_MESSAGE);
					System.exit(-1);
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(new JDialog(), e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				System.exit(-1);
			}
		}
		
		// after successful validation of reg code or trial continuation
		
		// intialize config & update based on license file
		if (!Configuration.initialize()) {
			System.exit(-1);
		}
		Configuration.saveCommonConfigValues("LICENCEE_NAME", "LICENCEE_ADR_1", "LICENCEE_ADR_2", "LICENCEE_ISI_REF_LIST", "NUMBER_OF_LINES", "ISSTD_LIST", "APP_DB_NAME");
					
		//debug mode
		if (args.length > 0) {
			Configuration.APP_DEBUG_MODE = args[0].equals("1") ? true : false;
		}
		
		// open login if valid license exist
		Login frmLogin = new Login(new JFrame());
		frmLogin.setVisible(true);
	}
	
	private static String findMACAddress() throws Exception{
		try {
			String macAdr = "";
			Process p = Runtime.getRuntime().exec("getmac /V /NH /FO CSV");
			BufferedReader bufIn = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String strLine = "";
			while((strLine = bufIn.readLine()) != null) {
				if (strLine.startsWith("\"Local Area Connection\"") || strLine.startsWith("\"Ethernet\"")) {
					macAdr = (strLine.split(",")[2]).replace("\"", "");
					break;
				}
			}
			return macAdr;
		} catch (Exception e) {
			throw new Exception("Error finding MAC address of your computer:" + e.getMessage());
		}
	}
}
