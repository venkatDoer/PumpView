/*
 * Created by JFormDesigner on Fri Nov 30 11:29:38 IST 2012
 */

package doer.pv;

import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.sql.SQLException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import doer.io.Database;
import doer.io.Encrypt;
import doer.io.MyResultSet;
import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;


/**
 * @author VENKATESAN SELVARAJ
 */
public class Login extends JDialog {
	public Login(Frame owner) {
		super(owner);
		initComponents();
	}

	public Login(Dialog owner) {
		super(owner);
		initComponents();
	}

	// CUSTOM FUNCTIONS - BEGIN
	
	private void associateFunctionKeys() {
		// associate enter for choose
		String CHOOSE_ACTION_KEY = "loginAction";
		Action loginAction = new AbstractAction() {
		      public void actionPerformed(ActionEvent actionEvent) {
		        cmdLoginActionPerformed();
		      }
		    };
		KeyStroke entr = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		InputMap loginInputMap = cmdLogin.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		loginInputMap.put(entr, CHOOSE_ACTION_KEY);
		ActionMap loginActionMap = cmdLogin.getActionMap();
		loginActionMap.put(CHOOSE_ACTION_KEY, loginAction);
		cmdLogin.setActionMap(loginActionMap);
		
		// associate F10 for exit
		String CANCEL_ACTION_KEY = "cancelAction";
		Action cancelAction = new AbstractAction() {
		      public void actionPerformed(ActionEvent actionEvent) {
		        cmdCancelActionPerformed();
		      }
		    };
		KeyStroke esc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		InputMap cancelInputMap = cmdCancel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		cancelInputMap.put(esc, CANCEL_ACTION_KEY);
		ActionMap cancelActionMap = cmdCancel.getActionMap();
		cancelActionMap.put(CANCEL_ACTION_KEY, cancelAction);
		cmdCancel.setActionMap(cancelActionMap);
	}
	
	// CUSTOM FUNCTIONS - END

	private void cmdCancelActionPerformed() {
		System.exit(0);
	}

	private void cmdLoginActionPerformed() {
		// validate entry
		if (txtName.getText().trim().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please enter an user name");
			txtName.requestFocus();
			return;
		}
		if (txtPass.getText().trim().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please enter password");
			txtPass.requestFocus();
			return;
		}
		
		if (cmbLine.getSelectedIndex() < 0) {
			JOptionPane.showMessageDialog(this, "Please choose your assembly line");
			cmbLine.requestFocus();
			return;
		}
		
		// validate user
		String curName = "";
		String curPass = "";
		String curAdmin = "";
		String curPumpAccess = "";
		String curModifyAccess = "";
		String selLine = cmbLine.getSelectedItem().toString();
		String selIS = cmbIS.getSelectedItem().toString();
		
		try {
			Database db = Database.getInstance();
			
			// fetch and load the result
			MyResultSet res = null;
			try {
				res = db.executeQuery("select * from USER where name='" + txtName.getText().trim() + "'");
			} catch (SQLException se) {
				if (se.getMessage().contains("no such table")) {
					// seems the software is run first time
					// create the config table and insert config params
					curName = "admin";
					curPass = Encrypt.encrypt("doer");
					curAdmin = Encrypt.encrypt("1");
					curPumpAccess = Encrypt.encrypt("1");
					curModifyAccess = Encrypt.encrypt("1");
					
					db.executeUpdate("create table USER (name text primary key, password text, admin text, pump_access text, modify_access text)");
					db.executeUpdate("insert into USER values ('" + curName + "','" + curPass + "','" + curAdmin + "','" + curPumpAccess + "','" + curModifyAccess + "')");
					res = db.executeQuery("select * from USER where name='" + txtName.getText().trim() + "'");
				}
			}
			
			if (res.next()) {
				curName = res.getString("name");
				curPass = Encrypt.decrypt(res.getString("password"));
				curAdmin = Encrypt.decrypt(res.getString("admin"));
				curPumpAccess = Encrypt.decrypt(res.getString("pump_access"));
				curModifyAccess = Encrypt.decrypt(res.getString("modify_access"));
			}
		} catch (Exception sqle) {
			JOptionPane.showMessageDialog(new JDialog(), "DB Error:" + sqle.getMessage());
			return;
		}
		
		if (curName.equals(txtName.getText().trim()) && curPass.equals((txtPass.getText().trim()))) {
			// user found
			// refresh config based on chosen line
			Configuration.LINE_NAME = selLine;
			Configuration.loadConfigValues();
			// open main screen if valid
			Configuration.USER = curName;
			Configuration.USER_IS_ADMIN = curAdmin;
			Configuration.USER_HAS_PUMP_ACCESS = curPumpAccess;
			Configuration.USER_HAS_MODIFY_ACCESS = curModifyAccess;
			if (!Configuration.LAST_USED_ISSTD.equals(selIS)) {
				Configuration.LAST_USED_ISSTD = selIS;
				Configuration.saveConfigValues("LAST_USED_ISSTD");
				Configuration.IS_ISSTD_CHANGED = true;
			}
			
			// set ISI ref based on ISSTD selection
			try {
				if(!Configuration.LICENCEE_ISI_REF_LIST.isEmpty()) {
					String[] refList = Configuration.LICENCEE_ISI_REF_LIST.split(",");
					if (refList.length > 0) {
						Configuration.LICENCEE_ISI_REF = refList[cmbIS.getSelectedIndex()];
					} else {
						Configuration.LICENCEE_ISI_REF = "";
					}
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				// ignore
			}
			
			// ISI handling - below one line changes fate (ISI) of this application
			String tblSuffix = "_" + selIS.substring(selIS.indexOf(" ")+1, selIS.indexOf(":"));
			
			// pick the tables based on ISI
			//Configuration.CALIBRATION - no change as calibration records grouped by assembly line, not per ISI
			//Configuration.CONFIG - no change as system has one config table, not for every ISI
			Configuration.PUMPTYPE += tblSuffix;
			Configuration.TESTNAMES_ALL += tblSuffix;
			Configuration.TESTNAMES_ROUTINE += tblSuffix;
			Configuration.TESTNAMES_TYPE += tblSuffix;
			Configuration.ROUTINE_LIMITS += tblSuffix;
			Configuration.NON_ISI_PERF_TOLERANCE += tblSuffix;
			Configuration.MOT_EFF_DATA_POINTS += tblSuffix;
			Configuration.READING_DETAIL += tblSuffix;
			Configuration.CUSTOM_REPORT += tblSuffix;
			Configuration.OPTIONAL_TEST += tblSuffix;
			Configuration.OUTPUT += tblSuffix;
			//Configuration.USER - no change as system has one user config
			this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			this.setVisible(false);
			// main window
			PumpView frmMain = new PumpView();
			this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			frmMain.setVisible(true);
		} else {
			JOptionPane.showMessageDialog(this, "Invalid user name or password\nNote: Both user name & password are case sensitive");
		}
	}

	private void initComponents() {
		// custom code begin
		// set windows theme
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.err.println("Unable to set theme: SystemLookAndFeel");
		}
		// custom code end
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		label42 = new JLabel();
		label1 = new JLabel();
		txtName = new JTextField();
		label2 = new JLabel();
		txtPass = new JPasswordField();
		separator1 = new JSeparator();
		label3 = new JLabel();
		cmbLine = new JComboBox();
		label4 = new JLabel();
		cmbIS = new JComboBox();
		lblVer = new JLabel();
		cmdCancel = new JButton();
		cmdLogin = new JButton();

		//======== this ========
		setTitle("Doer PumpView: Login");
		setFont(new Font("Arial", Font.PLAIN, 12));
		setResizable(false);
		Container contentPane = getContentPane();
		contentPane.setLayout(new TableLayout(new double[][] {
			{5, 150, 10, TableLayout.PREFERRED, 160, 5},
			{5, TableLayout.PREFERRED, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5}}));
		((TableLayout)contentPane.getLayout()).setHGap(5);
		((TableLayout)contentPane.getLayout()).setVGap(5);

		//---- label42 ----
		label42.setIcon(new ImageIcon(getClass().getResource("/img/doer_logo.png")));
		label42.setFocusable(false);
		label42.setHorizontalAlignment(SwingConstants.CENTER);
		label42.setOpaque(true);
		label42.setBackground(new Color(0x003399));
		contentPane.add(label42, new TableLayoutConstraints(1, 1, 1, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

		//---- label1 ----
		label1.setText("User Name");
		label1.setFont(new Font("Arial", Font.PLAIN, 14));
		contentPane.add(label1, new TableLayoutConstraints(3, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

		//---- txtName ----
		txtName.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 16));
		contentPane.add(txtName, new TableLayoutConstraints(4, 1, 4, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

		//---- label2 ----
		label2.setText("Password");
		label2.setFont(new Font("Arial", Font.PLAIN, 14));
		contentPane.add(label2, new TableLayoutConstraints(3, 2, 3, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

		//---- txtPass ----
		txtPass.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 16));
		txtPass.setNextFocusableComponent(cmdLogin);
		contentPane.add(txtPass, new TableLayoutConstraints(4, 2, 4, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		contentPane.add(separator1, new TableLayoutConstraints(3, 3, 4, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

		//---- label3 ----
		label3.setText("Assembly Line");
		label3.setFont(new Font("Arial", Font.PLAIN, 14));
		contentPane.add(label3, new TableLayoutConstraints(3, 4, 3, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

		//---- cmbLine ----
		cmbLine.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 16));
		contentPane.add(cmbLine, new TableLayoutConstraints(4, 4, 4, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

		//---- label4 ----
		label4.setText("IS Standard");
		label4.setFont(new Font("Arial", Font.PLAIN, 14));
		contentPane.add(label4, new TableLayoutConstraints(3, 5, 3, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

		//---- cmbIS ----
		cmbIS.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 16));
		contentPane.add(cmbIS, new TableLayoutConstraints(4, 5, 4, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

		//---- lblVer ----
		lblVer.setText("Version");
		lblVer.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 14));
		lblVer.setForeground(new Color(0x003399));
		lblVer.setIcon(null);
		lblVer.setHorizontalAlignment(SwingConstants.CENTER);
		contentPane.add(lblVer, new TableLayoutConstraints(1, 7, 1, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

		//---- cmdCancel ----
		cmdCancel.setText("<html>Cancel&nbsp;&nbsp;<font size=-2>[Esc]</font></html>");
		cmdCancel.setFont(new Font("Arial", Font.PLAIN, 14));
		cmdCancel.setIcon(new ImageIcon(getClass().getResource("/img/exit.PNG")));
		cmdCancel.setToolTipText("Click on this to cancel user login and close this window");
		cmdCancel.setNextFocusableComponent(txtName);
		cmdCancel.addActionListener(e -> cmdCancelActionPerformed());
		contentPane.add(cmdCancel, new TableLayoutConstraints(3, 7, 3, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

		//---- cmdLogin ----
		cmdLogin.setText("<html>Login&nbsp;&nbsp;<font size=-2>[Enter]</font></html>");
		cmdLogin.setFont(new Font("Arial", Font.PLAIN, 14));
		cmdLogin.setIcon(new ImageIcon(getClass().getResource("/img/login.PNG")));
		cmdLogin.setToolTipText("Click on this to login after entering the user name and password");
		cmdLogin.setNextFocusableComponent(cmdCancel);
		cmdLogin.addActionListener(e -> cmdLoginActionPerformed());
		contentPane.add(cmdLogin, new TableLayoutConstraints(4, 7, 4, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	
		// custom code begin
		lblVer.setText(Configuration.APP_VERSION);
		associateFunctionKeys();
		
		// load licensed number of lines
		for(int i=1; i <= Integer.parseInt(Configuration.NUMBER_OF_LINES); i++) {
			cmbLine.addItem("Line " + i);
		}
		
		// no default line when more number of lines is enabled to ensure user is choosing correct line
		if (Integer.parseInt(Configuration.NUMBER_OF_LINES) > 1) {
			cmbLine.setSelectedIndex(-1);
		} else {
			cmbIS.setSelectedItem(Configuration.LAST_USED_ISSTD);
		}
		
		// load licensed IS standards
		String[] ISList = Configuration.ISSTD_LIST.split(",");
		for(int i=0; i < ISList.length; i++) {
			cmbIS.addItem(ISList[i]);
		}
		
		// custom code end
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JLabel label42;
	private JLabel label1;
	private JTextField txtName;
	private JLabel label2;
	private JPasswordField txtPass;
	private JSeparator separator1;
	private JLabel label3;
	private JComboBox cmbLine;
	private JLabel label4;
	private JComboBox cmbIS;
	private JLabel lblVer;
	private JButton cmdCancel;
	private JButton cmdLogin;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
	
}
