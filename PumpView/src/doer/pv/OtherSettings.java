/*
 * Created by JFormDesigner on Thu Nov 08 12:01:44 IST 2012
 */

package doer.pv;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import org.jfree.ui.FilesystemFilter;

import doer.lic.LicenseFile;

import info.clearthought.layout.*;
import jxl.read.biff.CompoundFile;

/**
 * @author VENKATESAN SELVARAJ
 */
public class OtherSettings extends JDialog {
	// custom variables
	HashMap<String, String> refList = new HashMap<String, String>();
	
	public OtherSettings(Frame owner) {
		super(owner);
		initComponents();
		customInit();
	}

	public OtherSettings(Dialog owner) {
		super(owner);
		initComponents();
	}

	private void cmdSaveActionPerformed() {
		// save the changes
		int res = JOptionPane.showConfirmDialog(this, "Save the changes?");
		if ( res != 0 ) {
			return;
		}
		this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		
		// reports
		Configuration.REP_SHOW_APP_WMARK = chkWMark.isSelected()?"1":"0";
		Configuration.REP_SHOW_TESTER_NAME = chkTester.isSelected()?"1":"0";
		Configuration.REP_SHOW_VERIFIED_BY = chkVerifBy.isSelected()?"1":"0";
		Configuration.REP_SHOW_VERIFIED_BY_NAME = txtNameVerify.getText();
		Configuration.REP_SHOW_APPROVED_BY = chkApprovBy.isSelected()?"1":"0";
		Configuration.REP_SHOW_APPROVED_BY_NAME = txtNameApprove.getText();
		Configuration.REP_SHOW_CASING_TEST = chkCasing.isSelected()?"1":"0";
		Configuration.REP_CASTING_TEST_MIN = txtCastingMin.getText();
		Configuration.REP_SHOW_MOT_EFF = chkMotEff.isSelected()?"1":"0";
		String tmpOldPE = Configuration.REP_SHOW_PUMP_EFF;
		Configuration.REP_SHOW_PUMP_EFF = chkPumpEff.isSelected()?"1":"0";
		Configuration.REP_SHOW_CUST_REP = chkCustRep.isSelected()?"1":"0";
		Configuration.REP_SHOW_NOTES = chkNote.isSelected()?"1":"0";
		Configuration.REP_NOTES_HEADING = txtNoteHead.getText();
		Configuration.REP_NOTES_TEXT = txtNoteText.getText();
		Configuration.REP_SHOW_MI_FOR_8472 = chkMIFor8472.isSelected()?"1":"0";
		Configuration.saveConfigValues("REP_SHOW_APP_WMARK","REP_SHOW_TESTER_NAME","REP_SHOW_VERIFIED_BY","REP_SHOW_VERIFIED_BY_NAME","REP_SHOW_APPROVED_BY","REP_SHOW_APPROVED_BY_NAME","REP_SHOW_CASING_TEST","REP_CASTING_TEST_MIN", "REP_SHOW_MOT_EFF", "REP_SHOW_PUMP_EFF", "REP_SHOW_CUST_REP", "REP_SHOW_NOTES", "REP_NOTES_HEADING", "REP_NOTES_TEXT", "REP_SHOW_MI_FOR_8472");
		
		// isi ref no.
		// construct new list & save it in both db & license file if it got changed
		String newLicRefList = "";
		for(int i=0; i< cmbIS.getItemCount(); i++) {
			newLicRefList += (refList.containsKey(cmbIS.getItemAt(i))?refList.get(cmbIS.getItemAt(i)):"") + ",";
		}
		if (newLicRefList.length()>1) {
			newLicRefList = newLicRefList.substring(0, newLicRefList.length()-1); // remove , at the end
		} else {
			newLicRefList = "";
		}
		if (!newLicRefList.equals(Configuration.LICENCEE_ISI_REF_LIST)) {
			Configuration.LICENCEE_ISI_REF_LIST = newLicRefList;
			Configuration.LICENCEE_ISI_REF = refList.get(Configuration.LAST_USED_ISSTD);
			Configuration.saveCommonConfigValues("LICENCEE_ISI_REF_LIST","LICENCEE_ISI_REF");
			
			try {
				LicenseFile lFile = new LicenseFile(Configuration.APP_DIR + Configuration.CONFIG_DIR + Configuration.CONFIG_FILE_LIC);
				lFile.setISRefNo(newLicRefList);
				lFile.rewriteFile();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, "Error updating license file:" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		
		// automatic backup
		Configuration.LAST_USED_BACKUP_LOCATION = txtBkLoc.getText();
		Configuration.saveCommonConfigValues("LAST_USED_BACKUP_LOCATION");
		
		if (!cmbDuration.getSelectedItem().toString().equals(Configuration.LAST_USED_BACKUP_DURATION)) {
			Configuration.LAST_USED_BACKUP_DURATION = cmbDuration.getSelectedItem().toString();
			
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, cmbDuration.getSelectedIndex()+1);
			Date dt = cal.getTime();
			
			Configuration.NEXT_BACKUP_DATE = reqDtFormat.format(dt);
			lblNext.setText(Configuration.NEXT_BACKUP_DATE);
			
			Configuration.saveCommonConfigValues("LAST_USED_BACKUP_DURATION", "NEXT_BACKUP_DATE");
		}
		
		//db name
		String dbMsg = "";
		String tmpDbList = "";
		
		String tmpIsMultiDB = Configuration.IS_MULTI_DB;
		Configuration.IS_MULTI_DB = optSingle.isSelected() ? "NO" : "YES";
		Integer noOfLines = Integer.valueOf(Configuration.NUMBER_OF_LINES);
		String tmpDBLoc = "";
		
		
		if (!tmpIsMultiDB.equals(Configuration.IS_MULTI_DB) || !Configuration.APP_SINGLE_DB_NAME.equals(txtDB.getText()) || !Configuration.APP_DB_NAME_LIST.equals(tmpDbList)) {
			if (noOfLines > 1) {
				for(int i=0; i<noOfLines; i++) {
					tmpDBLoc = ((JTextField)pnlMulti.getComponent(i*3+1)).getText().trim();
					if (!tmpDBLoc.isEmpty()) {
						tmpDbList += tmpDBLoc;
						if (i < noOfLines-1) {
							tmpDbList += ",";
						}
					} else {
						this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
						JOptionPane.showMessageDialog(this, "Please choose valid database for 'Line " + (i+1) + "'", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
			}
			try {
				Configuration.APP_SINGLE_DB_NAME = txtDB.getText();
				Configuration.APP_DB_NAME_LIST = tmpDbList;
				dbMsg = "\nNote:Database change will be effective only up on relogin to application";
				LicenseFile lFile = new LicenseFile(Configuration.APP_DIR + Configuration.CONFIG_DIR + Configuration.CONFIG_FILE_LIC);
				lFile.setDbName(Configuration.APP_SINGLE_DB_NAME);
				lFile.setIsMultiDb(Configuration.IS_MULTI_DB);
				lFile.setDbNameList(Configuration.APP_DB_NAME_LIST);
				lFile.rewriteFile();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, "Error updating DB name:" + e.getMessage());
			}
		}
		
		this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		JOptionPane.showMessageDialog(this, "Changes are saved successfully!" + dbMsg, "Message", dbMsg.isEmpty() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE); 
		
		// warn about dependancy when enabling pump eff
		if (!tmpOldPE.equals(Configuration.REP_SHOW_PUMP_EFF) && Configuration.REP_SHOW_PUMP_EFF.equals("1")) {
			JOptionPane.showMessageDialog(this, "Wait! As you have enabled pump efficiency, please ensure to configure 'motor efficiency' in choose pump screen", "Warning", JOptionPane.WARNING_MESSAGE); 
		}
	}

	private void cmdExitActionPerformed() {
		this.setVisible(false);
	}

	private void cmdBackupActionPerformed() {
		// on demand back up
		try {
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			Configuration.backupData(txtBkLoc.getText());
			Configuration.LAST_BACKUP_DATE = reqDtFormat.format(Calendar.getInstance().getTime());
			Configuration.saveCommonConfigValues("LAST_BACKUP_DATE");
			lblDate.setText(Configuration.LAST_BACKUP_DATE);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage());
		}
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	private void cmdChangeActionPerformed() {
		JFileChooser fileDlg = new JFileChooser();
		fileDlg.setFileSelectionMode(1); // directories only
		fileDlg.setDialogTitle("Choose backup folder");
		fileDlg.showOpenDialog(this);
		File selFile = fileDlg.getSelectedFile();
		if (selFile == null) {
			return;
		} else {
			txtBkLoc.setText(selFile.getAbsolutePath());
			
		}
	}

	private void cmdChangeDBActionPerformed() {
		JOptionPane.showMessageDialog (this, "WARNING:Changing current database will affect configuration and test data (after relogin) based on new database you choose");
		JFileChooser fileDlg = new JFileChooser();
		fileDlg.setFileFilter(new FilesystemFilter("db", "PumpViewPro Database (*.db)"));
		fileDlg.setDialogTitle("Choose Database");
		fileDlg.showOpenDialog(this);
		File selFile = fileDlg.getSelectedFile();
		if (selFile == null) {
			return;
		} else if (!selFile.getName().endsWith(".db")){
			JOptionPane.showMessageDialog(this, "Invalid database file. Please try again!","Error", JOptionPane.ERROR_MESSAGE);
		} else {
			txtDB.setText(selFile.getAbsolutePath());
		}
	}
	
	private void changeDB(Integer idx) {
		JOptionPane.showMessageDialog (this, "WARNING:Changing current database will affect configuration and test data (after relogin) based on new database you choose");
		JFileChooser fileDlg = new JFileChooser();
		fileDlg.setFileFilter(new FilesystemFilter("db", "PumpViewPro Database (*.db)"));
		fileDlg.setDialogTitle("Choose Database");
		fileDlg.showOpenDialog(this);
		File selFile = fileDlg.getSelectedFile();
		if (selFile == null) {
			return;
		} else if (!selFile.getName().endsWith(".db")){
			JOptionPane.showMessageDialog(this, "Invalid database file. Please try again!","Error", JOptionPane.ERROR_MESSAGE);
		} else {
			((JTextField) pnlMulti.getComponent(idx-1)).setText(selFile.getAbsolutePath());
		}
	}

	private void cmbISActionPerformed() {
		txtISIRef.setText(refList.get(cmbIS.getSelectedItem().toString()));
	}

	private void txtISIRefFocusLost() {
		refList.put(cmbIS.getSelectedItem().toString(), txtISIRef.getText().trim());
	}

	private void chkNoteActionPerformed() {
		if (chkNote.isSelected()) {
			lblNoteHead.setEnabled(true);
			lblNoteText.setEnabled(true);
			txtNoteHead.setEnabled(true);
			txtNoteText.setEnabled(true);
		} else {
			lblNoteHead.setEnabled(false);
			lblNoteText.setEnabled(false);
			txtNoteHead.setEnabled(false);
			txtNoteText.setEnabled(false);		
		}
	}

	private void chkPumpEffItemStateChanged() {
		if (Configuration.LAST_USED_ISSTD.startsWith("IS 8034:") || Configuration.LAST_USED_ISSTD.startsWith("IS 14220:") || Configuration.LAST_USED_ISSTD.startsWith("IS 6595:")) {
			chkMotEff.setEnabled(false);
			chkMotEff.setSelected(true);
		} else {
			if (chkPumpEff.isSelected()) {
				chkMotEff.setEnabled(true);
				chkMotEff.setSelected(Configuration.REP_SHOW_MOT_EFF.equals("1"));
			} else {
				chkMotEff.setEnabled(false);
				chkMotEff.setSelected(false);
			}
		}
	}

	private void optSingle() {
		if (optSingle.isSelected()) {
			pnlSingle.setEnabled(true);
			pnlMulti.setEnabled(false);
			lblDB.setEnabled(true);
			txtDB.setEnabled(true);
			cmdChangeDB.setEnabled(true);
			for(int i=0; i<pnlMulti.getComponentCount(); i++) {
				pnlMulti.getComponent(i).setEnabled(false);
			}
		} else {
			pnlSingle.setEnabled(false);
			pnlMulti.setEnabled(true);
			lblDB.setEnabled(false);
			txtDB.setEnabled(false);
			cmdChangeDB.setEnabled(false);
			for(int i=0; i<pnlMulti.getComponentCount(); i++) {
				pnlMulti.getComponent(i).setEnabled(true);
			}
		}
	}

	private void optMultipleStateChanged() {
		optSingle();
	}

	private void chkVerifBy(ActionEvent e) {
		if(chkVerifBy.isSelected()) {
			txtNameVerify.setEditable(true);
		}else {
			txtNameVerify.setEditable(false);
		}
	}

	private void chkApprovBy(ActionEvent e) {
		if(chkApprovBy.isSelected()) {
			txtNameApprove.setEditable(true);
		}else {
			txtNameApprove.setEditable(false);
		}
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		tabSet = new JTabbedPane();
		pnlRep = new JPanel();
		chkWMark = new JCheckBox();
		chkCasing = new JCheckBox();
		txtCastingMin = new JTextField();
		chkNote = new JCheckBox();
		chkPumpEff = new JCheckBox();
		chkCustRep = new JCheckBox();
		chkMotEff = new JCheckBox();
		chkMIFor8472 = new JCheckBox();
		lblNoteHead = new JLabel();
		txtNoteHead = new JTextField();
		lblNoteText = new JLabel();
		txtNoteText = new JTextField();
		separator1 = new JSeparator();
		chkTester = new JCheckBox();
		chkVerifBy = new JCheckBox();
		txtNameVerify = new JTextField();
		chkApprovBy = new JCheckBox();
		txtNameApprove = new JTextField();
		separator2 = new JSeparator();
		label5 = new JLabel();
		txtISIRef = new JTextField();
		label4 = new JLabel();
		cmbIS = new JComboBox();
		label13 = new JLabel();
		pnlBackup = new JPanel();
		label6 = new JLabel();
		cmbDuration = new JComboBox();
		label8 = new JLabel();
		label7 = new JLabel();
		label9 = new JLabel();
		txtBkLoc = new JTextField();
		cmdChange = new JButton();
		label10 = new JLabel();
		lblDate = new JLabel();
		cmdBackup = new JButton();
		label11 = new JLabel();
		lblNext = new JLabel();
		pnlDB = new JPanel();
		pnlOpt = new JPanel();
		optSingle = new JRadioButton();
		optMultiple = new JRadioButton();
		pnlSingle = new JPanel();
		lblDB = new JLabel();
		txtDB = new JTextField();
		cmdChangeDB = new JButton();
		pnlMulti = new JPanel();
		cmdSave = new JButton();
		cmdExit = new JButton();

		//======== this ========
		setTitle("Doer PumpView: Other Settings");
		setFont(new Font("Arial", Font.PLAIN, 12));
		setModal(true);
		setResizable(false);
		Container contentPane = getContentPane();
		contentPane.setLayout(new TableLayout(new double[][] {
			{10, TableLayout.FILL, TableLayout.FILL, 10},
			{10, TableLayout.FILL, TableLayout.PREFERRED, 5}}));
		((TableLayout)contentPane.getLayout()).setHGap(5);
		((TableLayout)contentPane.getLayout()).setVGap(5);

		//======== tabSet ========
		{
			tabSet.setFont(new Font("Arial", Font.PLAIN, 14));

			//======== pnlRep ========
			{
				pnlRep.setBorder(new TitledBorder(null, "Report []", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION,
					new Font("Arial", Font.BOLD, 14), Color.blue));
				pnlRep.setLayout(new TableLayout(new double[][] {
					{5, TableLayout.FILL, 101, TableLayout.PREFERRED, TableLayout.FILL, TableLayout.FILL},
					{5, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, 3, TableLayout.PREFERRED, TableLayout.PREFERRED, 5, TableLayout.PREFERRED}}));
				((TableLayout)pnlRep.getLayout()).setHGap(5);
				((TableLayout)pnlRep.getLayout()).setVGap(5);

				//---- chkWMark ----
				chkWMark.setText("Show Application Watermark");
				chkWMark.setFont(new Font("Arial", Font.BOLD, 12));
				pnlRep.add(chkWMark, new TableLayoutConstraints(1, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- chkCasing ----
				chkCasing.setText("Show Hydrostatic Pressure Test");
				chkCasing.setFont(new Font("Arial", Font.BOLD, 12));
				pnlRep.add(chkCasing, new TableLayoutConstraints(3, 1, 4, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- txtCastingMin ----
				txtCastingMin.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
				txtCastingMin.setToolTipText("ISI registration license number of particular ISI standard");
				txtCastingMin.addFocusListener(new FocusAdapter() {
					@Override
					public void focusLost(FocusEvent e) {
						txtISIRefFocusLost();
					}
				});
				pnlRep.add(txtCastingMin, new TableLayoutConstraints(5, 1, 5, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- chkNote ----
				chkNote.setText("Show Note");
				chkNote.setFont(new Font("Arial", Font.BOLD, 12));
				chkNote.addActionListener(e -> chkNoteActionPerformed());
				pnlRep.add(chkNote, new TableLayoutConstraints(1, 2, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- chkPumpEff ----
				chkPumpEff.setText("Show Pump Efficiency In Performance Report");
				chkPumpEff.setFont(new Font("Arial", Font.BOLD, 12));
				chkPumpEff.addItemListener(e -> chkPumpEffItemStateChanged());
				pnlRep.add(chkPumpEff, new TableLayoutConstraints(3, 2, 5, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- chkCustRep ----
				chkCustRep.setText("Show Custom Report Tab");
				chkCustRep.setFont(new Font("Arial", Font.BOLD, 12));
				pnlRep.add(chkCustRep, new TableLayoutConstraints(1, 3, 2, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- chkMotEff ----
				chkMotEff.setText("Show Motor Efficiency In Name Plate Details");
				chkMotEff.setFont(new Font("Arial", Font.BOLD, 12));
				pnlRep.add(chkMotEff, new TableLayoutConstraints(3, 3, 5, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- chkMIFor8472 ----
				chkMIFor8472.setText("Show Motor Input (kW) In Graph & Result For 8472");
				chkMIFor8472.setFont(new Font("Arial", Font.BOLD, 12));
				chkMIFor8472.addActionListener(e -> chkNoteActionPerformed());
				pnlRep.add(chkMIFor8472, new TableLayoutConstraints(3, 4, 5, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblNoteHead ----
				lblNoteHead.setText("Note Heading");
				lblNoteHead.setFont(new Font("Arial", Font.BOLD, 12));
				pnlRep.add(lblNoteHead, new TableLayoutConstraints(1, 5, 1, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- txtNoteHead ----
				txtNoteHead.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
				txtNoteHead.addFocusListener(new FocusAdapter() {
					@Override
					public void focusLost(FocusEvent e) {
						txtISIRefFocusLost();
					}
				});
				pnlRep.add(txtNoteHead, new TableLayoutConstraints(2, 5, 4, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblNoteText ----
				lblNoteText.setText("Note Text");
				lblNoteText.setFont(new Font("Arial", Font.BOLD, 12));
				pnlRep.add(lblNoteText, new TableLayoutConstraints(1, 6, 1, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- txtNoteText ----
				txtNoteText.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
				txtNoteText.addFocusListener(new FocusAdapter() {
					@Override
					public void focusLost(FocusEvent e) {
						txtISIRefFocusLost();
					}
				});
				pnlRep.add(txtNoteText, new TableLayoutConstraints(2, 6, 4, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				pnlRep.add(separator1, new TableLayoutConstraints(1, 7, 5, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- chkTester ----
				chkTester.setText("Show Tester Name In Tested By");
				chkTester.setFont(new Font("Arial", Font.BOLD, 12));
				pnlRep.add(chkTester, new TableLayoutConstraints(1, 8, 2, 8, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- chkVerifBy ----
				chkVerifBy.setText("Show Verified By With Name");
				chkVerifBy.setFont(new Font("Arial", Font.BOLD, 12));
				chkVerifBy.addActionListener(e -> chkVerifBy(e));
				pnlRep.add(chkVerifBy, new TableLayoutConstraints(3, 8, 4, 8, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- txtNameVerify ----
				txtNameVerify.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
				txtNameVerify.setToolTipText("ISI registration license number of particular ISI standard");
				txtNameVerify.addFocusListener(new FocusAdapter() {
					@Override
					public void focusLost(FocusEvent e) {
						txtISIRefFocusLost();
					}
				});
				pnlRep.add(txtNameVerify, new TableLayoutConstraints(5, 8, 5, 8, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- chkApprovBy ----
				chkApprovBy.setText("Show Approved By With Name");
				chkApprovBy.setFont(new Font("Arial", Font.BOLD, 12));
				chkApprovBy.addActionListener(e -> chkApprovBy(e));
				pnlRep.add(chkApprovBy, new TableLayoutConstraints(3, 9, 4, 9, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- txtNameApprove ----
				txtNameApprove.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
				txtNameApprove.setToolTipText("ISI registration license number of particular ISI standard");
				txtNameApprove.addFocusListener(new FocusAdapter() {
					@Override
					public void focusLost(FocusEvent e) {
						txtISIRefFocusLost();
					}
				});
				pnlRep.add(txtNameApprove, new TableLayoutConstraints(5, 9, 5, 9, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				pnlRep.add(separator2, new TableLayoutConstraints(1, 10, 5, 10, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label5 ----
				label5.setText("ISI License Number");
				label5.setFont(new Font("Arial", Font.BOLD, 12));
				pnlRep.add(label5, new TableLayoutConstraints(1, 11, 1, 11, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- txtISIRef ----
				txtISIRef.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
				txtISIRef.setToolTipText("ISI registration license number of particular ISI standard");
				txtISIRef.addFocusListener(new FocusAdapter() {
					@Override
					public void focusLost(FocusEvent e) {
						txtISIRefFocusLost();
					}
				});
				pnlRep.add(txtISIRef, new TableLayoutConstraints(2, 11, 2, 11, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label4 ----
				label4.setText("For ISI Standard");
				label4.setFont(new Font("Arial", Font.BOLD, 12));
				pnlRep.add(label4, new TableLayoutConstraints(3, 11, 3, 11, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmbIS ----
				cmbIS.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
				cmbIS.addActionListener(e -> cmbISActionPerformed());
				pnlRep.add(cmbIS, new TableLayoutConstraints(4, 11, 4, 11, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label13 ----
				label13.setText("[All Assembly Lines]");
				label13.setFont(new Font("Arial", Font.BOLD, 12));
				label13.setForeground(Color.blue);
				pnlRep.add(label13, new TableLayoutConstraints(5, 11, 5, 11, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			tabSet.addTab("Reports", pnlRep);

			//======== pnlBackup ========
			{
				pnlBackup.setBorder(new TitledBorder(null, "Automatic Backup [All Assembly Lines]", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION,
					new Font("Arial", Font.BOLD, 14), Color.blue));
				pnlBackup.setLayout(new TableLayout(new double[][] {
					{5, TableLayout.PREFERRED, 157, 67, TableLayout.FILL, 145, 5},
					{5, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, 5}}));
				((TableLayout)pnlBackup.getLayout()).setHGap(5);
				((TableLayout)pnlBackup.getLayout()).setVGap(5);

				//---- label6 ----
				label6.setText("Backup Test Data And Configuration For Every");
				label6.setFont(new Font("Arial", Font.BOLD, 12));
				label6.setIcon(null);
				pnlBackup.add(label6, new TableLayoutConstraints(1, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- cmbDuration ----
				cmbDuration.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
				cmbDuration.setBackground(Color.white);
				cmbDuration.setToolTipText("Duration of automatic backup");
				pnlBackup.add(cmbDuration, new TableLayoutConstraints(3, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- label8 ----
				label8.setText("Days");
				label8.setFont(new Font("Arial", Font.BOLD, 12));
				label8.setIcon(null);
				pnlBackup.add(label8, new TableLayoutConstraints(4, 1, 4, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- label7 ----
				label7.setText("Note: Only recent five backups will be retained at any point of time");
				label7.setFont(new Font("Arial", Font.PLAIN, 12));
				label7.setIcon(null);
				pnlBackup.add(label7, new TableLayoutConstraints(1, 2, 4, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- label9 ----
				label9.setText("Backup Location");
				label9.setFont(new Font("Arial", Font.BOLD, 12));
				label9.setIcon(null);
				pnlBackup.add(label9, new TableLayoutConstraints(1, 3, 1, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtBkLoc ----
				txtBkLoc.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
				txtBkLoc.setBorder(new LineBorder(Color.lightGray));
				txtBkLoc.setEditable(false);
				txtBkLoc.setBackground(new Color(0xf7f7f7));
				txtBkLoc.setToolTipText("A folder location where backup files are stored");
				pnlBackup.add(txtBkLoc, new TableLayoutConstraints(2, 3, 4, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmdChange ----
				cmdChange.setText("Change...");
				cmdChange.setFont(new Font("Arial", Font.PLAIN, 12));
				cmdChange.setToolTipText("Click on this to change the backup location");
				cmdChange.addActionListener(e -> cmdChangeActionPerformed());
				pnlBackup.add(cmdChange, new TableLayoutConstraints(5, 3, 5, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label10 ----
				label10.setText("Last Backup Date");
				label10.setFont(new Font("Arial", Font.BOLD, 12));
				label10.setIcon(null);
				pnlBackup.add(label10, new TableLayoutConstraints(1, 4, 1, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- lblDate ----
				lblDate.setText("Backup Date");
				lblDate.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
				lblDate.setIcon(null);
				pnlBackup.add(lblDate, new TableLayoutConstraints(2, 4, 2, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- cmdBackup ----
				cmdBackup.setText("Take A Backup Now");
				cmdBackup.setFont(new Font("Arial", Font.PLAIN, 12));
				cmdBackup.setToolTipText("Click on this to take a back on demand. This does not affect scheduled automatic backups");
				cmdBackup.addActionListener(e -> cmdBackupActionPerformed());
				pnlBackup.add(cmdBackup, new TableLayoutConstraints(5, 4, 5, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label11 ----
				label11.setText("Next Backup Date");
				label11.setFont(new Font("Arial", Font.BOLD, 12));
				label11.setIcon(null);
				pnlBackup.add(label11, new TableLayoutConstraints(1, 5, 1, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- lblNext ----
				lblNext.setText("Backup Date");
				lblNext.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
				lblNext.setIcon(null);
				pnlBackup.add(lblNext, new TableLayoutConstraints(2, 5, 2, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));
			}
			tabSet.addTab("Backup", pnlBackup);

			//======== pnlDB ========
			{
				pnlDB.setBorder(new TitledBorder(null, "Database", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION,
					new Font("Arial", Font.BOLD, 14), Color.blue));
				pnlDB.setLayout(new TableLayout(new double[][] {
					{5, TableLayout.FILL, 5},
					{5, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED}}));
				((TableLayout)pnlDB.getLayout()).setHGap(5);
				((TableLayout)pnlDB.getLayout()).setVGap(5);

				//======== pnlOpt ========
				{
					pnlOpt.setLayout(new TableLayout(new double[][] {
						{TableLayout.FILL, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL},
						{TableLayout.PREFERRED}}));
					((TableLayout)pnlOpt.getLayout()).setHGap(5);
					((TableLayout)pnlOpt.getLayout()).setVGap(5);

					//---- optSingle ----
					optSingle.setText("Single Database");
					optSingle.setFont(new Font("Arial", Font.BOLD, 12));
					optSingle.setSelected(true);
					optSingle.addActionListener(e -> optSingle());
					pnlOpt.add(optSingle, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- optMultiple ----
					optMultiple.setText("Multiple Databases");
					optMultiple.setFont(new Font("Arial", Font.BOLD, 12));
					optMultiple.addChangeListener(e -> optMultipleStateChanged());
					pnlOpt.add(optMultiple, new TableLayoutConstraints(2, 0, 2, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				}
				pnlDB.add(pnlOpt, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//======== pnlSingle ========
				{
					pnlSingle.setBorder(new TitledBorder(null, "Single Database For All Lines", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
						new Font("Arial", Font.BOLD, 12), Color.blue));
					pnlSingle.setLayout(new TableLayout(new double[][] {
						{95, TableLayout.FILL, TableLayout.PREFERRED},
						{TableLayout.PREFERRED}}));
					((TableLayout)pnlSingle.getLayout()).setHGap(5);
					((TableLayout)pnlSingle.getLayout()).setVGap(5);

					//---- lblDB ----
					lblDB.setText("Database Path");
					lblDB.setFont(new Font("Arial", Font.BOLD, 12));
					lblDB.setIcon(null);
					pnlSingle.add(lblDB, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

					//---- txtDB ----
					txtDB.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
					txtDB.setBorder(new LineBorder(Color.lightGray));
					txtDB.setEditable(false);
					txtDB.setBackground(new Color(0xf7f7f7));
					pnlSingle.add(txtDB, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- cmdChangeDB ----
					cmdChangeDB.setText("Change...");
					cmdChangeDB.setFont(new Font("Arial", Font.PLAIN, 12));
					cmdChangeDB.addActionListener(e -> cmdChangeDBActionPerformed());
					pnlSingle.add(cmdChangeDB, new TableLayoutConstraints(2, 0, 2, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				}
				pnlDB.add(pnlSingle, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//======== pnlMulti ========
				{
					pnlMulti.setBorder(new TitledBorder(null, "Multiple Databases (Seperate Database For Each Line)", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
						new Font("Arial", Font.BOLD, 12), Color.blue));
					pnlMulti.setLayout(new TableLayout(new double[][] {
						{95, TableLayout.FILL, TableLayout.PREFERRED},
						{}}));
					((TableLayout)pnlMulti.getLayout()).setHGap(5);
					((TableLayout)pnlMulti.getLayout()).setVGap(5);
				}
				pnlDB.add(pnlMulti, new TableLayoutConstraints(1, 3, 1, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			tabSet.addTab("Database", pnlDB);
		}
		contentPane.add(tabSet, new TableLayoutConstraints(1, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

		//---- cmdSave ----
		cmdSave.setText("Save");
		cmdSave.setFont(new Font("Arial", Font.PLAIN, 14));
		cmdSave.setIcon(new ImageIcon(getClass().getResource("/img/save.PNG")));
		cmdSave.setToolTipText("Click on this to save the changes made above in all tabs");
		cmdSave.setMnemonic('S');
		cmdSave.addActionListener(e -> cmdSaveActionPerformed());
		contentPane.add(cmdSave, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

		//---- cmdExit ----
		cmdExit.setText("<html>Close&nbsp;&nbsp<font size=-2>[Esc]</html>");
		cmdExit.setFont(new Font("Arial", Font.PLAIN, 14));
		cmdExit.setIcon(new ImageIcon(getClass().getResource("/img/exit.PNG")));
		cmdExit.setToolTipText("Click on this to close this window");
		cmdExit.addActionListener(e -> cmdExitActionPerformed());
		contentPane.add(cmdExit, new TableLayoutConstraints(2, 2, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		pack();
		setLocationRelativeTo(getOwner());

		//---- buttonGroup2 ----
		ButtonGroup buttonGroup2 = new ButtonGroup();
		buttonGroup2.add(optSingle);
		buttonGroup2.add(optMultiple);
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JTabbedPane tabSet;
	private JPanel pnlRep;
	private JCheckBox chkWMark;
	private JCheckBox chkCasing;
	private JTextField txtCastingMin;
	private JCheckBox chkNote;
	private JCheckBox chkPumpEff;
	private JCheckBox chkCustRep;
	private JCheckBox chkMotEff;
	private JCheckBox chkMIFor8472;
	private JLabel lblNoteHead;
	private JTextField txtNoteHead;
	private JLabel lblNoteText;
	private JTextField txtNoteText;
	private JSeparator separator1;
	private JCheckBox chkTester;
	private JCheckBox chkVerifBy;
	private JTextField txtNameVerify;
	private JCheckBox chkApprovBy;
	private JTextField txtNameApprove;
	private JSeparator separator2;
	private JLabel label5;
	private JTextField txtISIRef;
	private JLabel label4;
	private JComboBox cmbIS;
	private JLabel label13;
	private JPanel pnlBackup;
	private JLabel label6;
	private JComboBox cmbDuration;
	private JLabel label8;
	private JLabel label7;
	private JLabel label9;
	private JTextField txtBkLoc;
	private JButton cmdChange;
	private JLabel label10;
	private JLabel lblDate;
	private JButton cmdBackup;
	private JLabel label11;
	private JLabel lblNext;
	private JPanel pnlDB;
	private JPanel pnlOpt;
	private JRadioButton optSingle;
	private JRadioButton optMultiple;
	private JPanel pnlSingle;
	private JLabel lblDB;
	private JTextField txtDB;
	private JButton cmdChangeDB;
	private JPanel pnlMulti;
	private JButton cmdSave;
	private JButton cmdExit;
	// JFormDesigner - End of variables declaration  //GEN-END:variables

	// custom variables and functions
	private SimpleDateFormat reqDtFormat = new SimpleDateFormat("dd-MM-yyyy");
	
	private void customInit() {
		associateFunctionKeys();
		
		// reports
		pnlRep.setBorder(new TitledBorder(null, "Report Settings [ASSEMBLY LINE:" + Configuration.LINE_NAME + "]", TitledBorder.CENTER, TitledBorder.TOP,
				new Font("Arial", Font.BOLD, 14), Color.blue));
		if (Configuration.IS_TRIAL_ON) {
			chkWMark.setEnabled(false);
		}
		
		/*if (Configuration.LAST_USED_ISSTD.startsWith("IS 8472:")) {
			chkPumpEff.setEnabled(false);
		} else { */
			chkPumpEff.setSelected(Configuration.REP_SHOW_PUMP_EFF.equals("1"));
		//}
			
		chkPumpEffItemStateChanged();
			
		chkWMark.setSelected(Configuration.REP_SHOW_APP_WMARK.equals("1"));
		chkTester.setSelected(Configuration.REP_SHOW_TESTER_NAME.equals("1"));
		chkVerifBy.setSelected(Configuration.REP_SHOW_VERIFIED_BY.equals("1"));
		txtNameVerify.setText(Configuration.REP_SHOW_VERIFIED_BY_NAME);
		chkApprovBy.setSelected(Configuration.REP_SHOW_APPROVED_BY.equals("1"));
		txtNameApprove.setText(Configuration.REP_SHOW_APPROVED_BY_NAME);
		chkCasing.setSelected(Configuration.REP_SHOW_CASING_TEST.equals("1"));
		txtCastingMin.setText(Configuration.REP_CASTING_TEST_MIN);
		chkCustRep.setSelected(Configuration.REP_SHOW_CUST_REP.equals("1"));
		chkNote.setSelected(Configuration.REP_SHOW_NOTES.equals("1"));
		txtNoteHead.setText(Configuration.REP_NOTES_HEADING);
		txtNoteText.setText(Configuration.REP_NOTES_TEXT);
		chkMIFor8472.setSelected(Configuration.REP_SHOW_MI_FOR_8472.equals("1"));
		chkNoteActionPerformed();
		
		if(chkApprovBy.isSelected()) {
			txtNameApprove.setEditable(true);
		}else {
			txtNameApprove.setEditable(false);
		}
		if(chkVerifBy.isSelected()) {
			txtNameVerify.setEditable(true);
		}else {
			txtNameVerify.setEditable(false);
		}
		
		// set ISI ref based on ISSTD selection
		String[]isList = Configuration.ISSTD_LIST.split(",");
		for(int i=0; i<isList.length; i++) {
			cmbIS.addItem(isList[i]);
		}
		cmbIS.setSelectedItem(Configuration.LAST_USED_ISSTD);
		
		String[]licList = Configuration.LICENCEE_ISI_REF_LIST.split(",");
		for(int i=0; i<licList.length; i++) {
			refList.put(isList[i], licList[i]);
		}
		txtISIRef.setText(Configuration.LICENCEE_ISI_REF);
		
		if (!Configuration.USER_IS_ADMIN.equals("1")) {
			txtISIRef.setEnabled(false);
		}
		// load duration
		for (int i=1; i<=90;i++) {
			cmbDuration.addItem(i);
		}
		cmbDuration.setSelectedItem(Integer.valueOf(Configuration.LAST_USED_BACKUP_DURATION));
		
		
		txtBkLoc.setText(Configuration.LAST_USED_BACKUP_LOCATION);
		txtBkLoc.setCaretPosition(0);
		lblDate.setText(Configuration.LAST_BACKUP_DATE);
		lblNext.setText(Configuration.NEXT_BACKUP_DATE);
		
		// db
		txtDB.setText(Configuration.APP_SINGLE_DB_NAME);
		txtDB.setCaretPosition(0);
		
		Integer noOfLines = Integer.valueOf(Configuration.NUMBER_OF_LINES);
		if (noOfLines > 1) {
			TableLayout tmpLayout = (TableLayout) pnlMulti.getLayout();
			String dbNameList[] = Configuration.APP_DB_NAME_LIST.split(",");
			// construct line items for every line and populate db path
			for(int i=0; i<noOfLines; i++) {
				tmpLayout.insertRow(i, TableLayout.FILL);
				//---- lblLine ----
				JLabel lblLine = new JLabel();
				lblLine.setText("Line " + (i+1) + " Database");
				lblLine.setFont(new Font("Arial", Font.BOLD, 12));
				lblLine.setIcon(null);
				pnlMulti.add(lblLine, new TableLayoutConstraints(0, i, 0, i, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtLine ----
				JTextField txtLine = new JTextField();
				txtLine.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
				txtLine.setBorder(new LineBorder(Color.lightGray));
				txtLine.setEditable(false);
				txtLine.setBackground(new Color(0xf7f7f7));
				pnlMulti.add(txtLine, new TableLayoutConstraints(1, i, 1, i, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmdLine ----
				JButton cmdLine = new JButton();
				cmdLine.setText("Change...");
				cmdLine.setFont(new Font("Arial", Font.PLAIN, 12));
				final Integer idx = i*3+2;
				cmdLine.addActionListener(e -> changeDB(idx));
				pnlMulti.add(cmdLine, new TableLayoutConstraints(2, i, 2, i, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				try {
					txtLine.setText(dbNameList[i]);
				} catch (Exception e) {
					txtLine.setText("");
				}
			}
			optSingle.setSelected(false);
		} else {
			// hide fields related to multi db
			pnlOpt.setVisible(false);
			pnlMulti.setVisible(false);
			pnlSingle.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
					new Font("Arial", Font.BOLD, 12), Color.blue));
		}
		if (Configuration.IS_MULTI_DB.equals("YES")) {
			optSingle.setSelected(false);
			optMultiple.setSelected(true);
			pnlBackup.setBorder(new TitledBorder(null, "Automatic Backup [ASSEMBLY LINE:" + Configuration.LINE_NAME + "]", TitledBorder.CENTER, TitledBorder.TOP,
					new Font("Arial", Font.BOLD, 14), Color.blue));
		} else {
			optSingle.setSelected(true);
			optMultiple.setSelected(false);
		}
		optSingle();
	}
	
private void associateFunctionKeys() {
		
		// associate f4 for save
		/*String SAVE_ACTION_KEY = "saveAction";
		Action saveAction = new AbstractAction() {
		      public void actionPerformed(ActionEvent actionEvent) {
		        cmdSaveActionPerformed();
		      }
		    };
		KeyStroke f4 = KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0);
		InputMap saveInputMap = cmdSave.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		saveInputMap.put(f4, SAVE_ACTION_KEY);
		ActionMap saveActionMap = cmdSave.getActionMap();
		saveActionMap.put(SAVE_ACTION_KEY, saveAction);
		cmdSave.setActionMap(saveActionMap);*/
		
		// associate Esc for exit
		String CLOSE_ACTION_KEY = "closeAction";
		Action closeAction = new AbstractAction() {
		      public void actionPerformed(ActionEvent actionEvent) {
		        cmdExitActionPerformed();
		      }
		    };
		KeyStroke esc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		InputMap closeInputMap = cmdExit.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		closeInputMap.put(esc, CLOSE_ACTION_KEY);
		ActionMap closeActionMap = cmdExit.getActionMap();
		closeActionMap.put(CLOSE_ACTION_KEY, closeAction);
		cmdExit.setActionMap(closeActionMap);
	}
}
