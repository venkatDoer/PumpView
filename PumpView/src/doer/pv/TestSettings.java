package doer.pv;

import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;

import javax.swing.*;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.KeyStroke;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

public class TestSettings extends JDialog {
	public TestSettings(Frame owner) {
		super(owner);
		frmMain = (PumpView) owner;
		initComponents();
		loadSettings();
		
		// set the panel header
		pnlTest.setBorder(new TitledBorder(null, "Test Settings [ASSEMBLY LINE:" + Configuration.LINE_NAME + "]", TitledBorder.CENTER, TitledBorder.TOP,
				new Font("Arial", Font.BOLD, 14), Color.blue));
	}
	
	public TestSettings(Dialog owner) {
		super(owner);
		initComponents();
	}
	
	public static String strJoin(Object[] objects, String sSep) {
	    StringBuilder sbStr = new StringBuilder();
	    for (int i = 0, il = objects.length; i < il; i++) {
	        if (i > 0)
	            sbStr.append(sSep);
	        	sbStr.append("'"+objects[i].toString()+"'");
	    }
	    return sbStr.toString();
	}

	// function to load existing settings
	private void loadSettings() {
		optAutomated.setSelected(Configuration.TEST_MODE.equals("A"));
		chkMulti.setSelected(Configuration.MULTI_STATION_MODE.equals("YES"));
		optManual.setSelected(Configuration.TEST_MODE.equals("M"));
		optSigKey.setSelected(Configuration.LAST_USED_SIGNAL_METHOD.equals("K"));
		optSigPush.setSelected(Configuration.LAST_USED_SIGNAL_METHOD.equals("P"));
		optSNoAuto.setSelected(Configuration.IS_BARCODE_USED.equals("NO"));
		optSNoGlobal.setSelected(Configuration.IS_SNO_GLOBAL.equals("YES"));
		optSNoModelSpec.setSelected(Configuration.IS_SNO_GLOBAL.equals("NO"));
		optSNoBarCode.setSelected(Configuration.IS_BARCODE_USED.equals("YES"));
		txtRecentPumpNo.setText(Configuration.LAST_USED_PUMP_SNO);
		txtRecentMotNo.setText(Configuration.LAST_USED_MOTOR_SNO);
		chkSNo.setSelected(Configuration.IS_CONFIRM_SNO.equals("YES"));
		
		chkPumpModel.setSelected(Configuration.IS_MODEL_FROM_BARCODE.equals("YES"));
		txtModelStartPos.setText(Configuration.MODEL_START_POS_IN_BARCODE);
		txtModelEndPos.setText(Configuration.MODEL_END_POS_IN_BARCODE);
		
		optRtResLimit.setSelected(Configuration.IS_PERF_BASED_ON_GRAPH_FOR_ROUTINE.equals("NO"));
		optRtResGraph.setSelected(Configuration.IS_PERF_BASED_ON_GRAPH_FOR_ROUTINE.equals("YES"));
		
		optHeadAuto.setSelected(Configuration.HEAD_ADJUSTMENT.equals("A"));
		optHeadMan.setSelected(Configuration.HEAD_ADJUSTMENT.equals("M"));
		findNextPumpNo(false);
		findNextMotorNo();
		
		optManualItemStateChanged();
		optSigKeyItemStateChanged();
		optSNoAutoItemStateChanged();
		chkBarcodeItemStateChanged();
		optSNoGlobalItemStateChanged();
	}
	
	// function to update the config values
	private void saveSettings() {
		Configuration.TEST_MODE = optAutomated.isSelected() ? "A":"M";
		Configuration.MULTI_STATION_MODE = chkMulti.isSelected() ? "YES":"NO";
		System.out.println(Configuration.MULTI_STATION_MODE);
		if (optSigKey.isSelected()) {
			Configuration.LAST_USED_SIGNAL_METHOD = "K";
		} else if (optSigPush.isSelected()) {
			Configuration.LAST_USED_SIGNAL_METHOD = "P";
		}
		Configuration.IS_SNO_GLOBAL = optSNoGlobal.isSelected() ? "YES" : "NO";
		Configuration.LAST_USED_PUMP_SNO = txtRecentPumpNo.getText().trim();
		Configuration.LAST_USED_MOTOR_SNO = txtRecentMotNo.getText().trim();
		Configuration.IS_CONFIRM_SNO = chkSNo.isSelected() ? "YES" : "NO";
		Configuration.IS_BARCODE_USED = optSNoBarCode.isSelected() ? "YES": "NO";
		Configuration.IS_MODEL_FROM_BARCODE = chkPumpModel.isSelected() ? "YES": "NO";
		try {
			Configuration.MODEL_START_POS_IN_BARCODE = Integer.valueOf(txtModelStartPos.getText().toString()).toString();
			Configuration.MODEL_END_POS_IN_BARCODE = Integer.valueOf(txtModelEndPos.getText().toString()).toString();
		} catch (NumberFormatException ne) {
			JOptionPane.showMessageDialog(this, "Enter valid numbers for start and end position of model");
			txtModelStartPos.requestFocus();
			return;
		}
		Configuration.IS_PERF_BASED_ON_GRAPH_FOR_ROUTINE = optRtResGraph.isSelected() ? "YES":"NO";
		Configuration.HEAD_ADJUSTMENT = optHeadAuto.isSelected() ? "A":"M";
		
		Configuration.saveConfigValues("TEST_MODE", "MULTI_STATION_MODE", "LAST_USED_SIGNAL_METHOD", "IS_SNO_GLOBAL", "LAST_USED_PUMP_SNO", "LAST_USED_MOTOR_SNO", "IS_CONFIRM_SNO", "IS_BARCODE_USED", "IS_MODEL_FROM_BARCODE", "MODEL_START_POS_IN_BARCODE", "MODEL_END_POS_IN_BARCODE", "HEAD_ADJUSTMENT", "IS_PERF_BASED_ON_GRAPH_FOR_ROUTINE");
	}
	
	private void findNextPumpNo(Boolean isMotorNoSame) {
		String lastUsedSNo = txtRecentPumpNo.getText();
		String nextSno = Configuration.findNextNo(lastUsedSNo);
		lblNextPumpSNo.setText(nextSno);
		if (isMotorNoSame) {
			txtRecentMotNo.setText(lastUsedSNo);
			lblNextMotSNo.setText(nextSno);
		}
	}
	
	private void findNextMotorNo() {
		String lastUsedSNo = txtRecentMotNo.getText();
		String nextSno = Configuration.findNextNo(lastUsedSNo);
		lblNextMotSNo.setText(nextSno);
	}

	private void cmdSaveActionPerformed() {
		// confirm
		int res = JOptionPane.showConfirmDialog(this, "Save the changes?\nWARNING: Any changes made in test order will be effective immediately");
		if ( res != 0 ) {
			return;
		}
		
		this.setCursor(waitCursor);
		
		saveSettings();
		// update main screen
		frmMain.stopThreads();
		frmMain.loadTestMode();
		frmMain.startThreads();
		this.setCursor(defCursor);
		JOptionPane.showMessageDialog(this, "Changes are saved successfully!"); 
	}
	
	private void associateFunctionKeys() {
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

	private void cmdExitActionPerformed() {
		this.setVisible(false);
	}

	private void optSNoAutoItemStateChanged() {
		Boolean isAuto = optSNoAuto.isSelected();
		pnlSNoAuto.setBackground(isAuto ? this.getBackground() : Color.lightGray);
		pnlSNoBarcode.setBackground(!isAuto ? this.getBackground() : Color.lightGray);
		optSNoGlobal.setEnabled(isAuto);
		optSNoModelSpec.setEnabled(isAuto);
		txtRecentPumpNo.setEnabled(isAuto && optSNoGlobal.isSelected());
		txtRecentMotNo.setEnabled(isAuto && optSNoGlobal.isSelected());
		lblNextPumpSNo.setEnabled(isAuto && optSNoGlobal.isSelected());
		lblNextMotSNo.setEnabled(isAuto && optSNoGlobal.isSelected());
		chkSNo.setEnabled(isAuto);
		chkPumpModel.setEnabled(!isAuto);
		txtModelStartPos.setEnabled(!isAuto && chkPumpModel.isSelected());
		txtModelEndPos.setEnabled(!isAuto && chkPumpModel.isSelected());
	}

	private void chkBarcodeItemStateChanged() {
		txtModelStartPos.setEnabled(chkPumpModel.isSelected());
		txtModelEndPos.setEnabled(chkPumpModel.isSelected());
	}

	private void txtRecentPumpNoKeyReleased() {
		findNextPumpNo(true);
	}

	private void txtRecentMotNoKeyReleased() {
		findNextMotorNo();
	}

	private void optAutomatedItemStateChanged() {
		optManualItemStateChanged();
	}

	private void optManualItemStateChanged() {
		boolean testAuto = optAutomated.isSelected();
		chkMulti.setEnabled(testAuto && !optSigKey.isSelected() && Configuration.NUMBER_OF_STATION > 1);
		optHeadAuto.setEnabled(testAuto);
		optHeadMan.setEnabled(testAuto);
		
		optSigKey.setEnabled(testAuto);
		optSigPush.setEnabled(testAuto);
		optSNoBarCode.setEnabled(testAuto);
		chkPumpModel.setEnabled(testAuto && optSNoBarCode.isSelected());
	}

	private void optSigKeyItemStateChanged() {
		if (optSigKey.isSelected() || Configuration.NUMBER_OF_STATION == 1) {
			chkMulti.setSelected(false);
			chkMulti.setEnabled(false);
		} else {
			chkMulti.setEnabled(true);
		}
	}

	private void optSNoGlobalItemStateChanged() {
		boolean testAuto = optSNoGlobal.isSelected();
		txtRecentPumpNo.setEnabled(testAuto);
		txtRecentMotNo.setEnabled(testAuto);
		lblPumpSNo.setEnabled(testAuto);
		lblMsno.setEnabled(testAuto);
		lblPumpSNoNext.setEnabled(testAuto);
		lblNextPumpSNo.setEnabled(testAuto);
		lblMotSNoNext.setEnabled(testAuto);
		lblNextMotSNo.setEnabled(testAuto);
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		pnlTest = new JPanel();
		panel1 = new JPanel();
		optManual = new JRadioButton();
		optAutomated = new JRadioButton();
		panel2 = new JPanel();
		optSigKey = new JRadioButton();
		optSigPush = new JRadioButton();
		chkMulti = new JCheckBox();
		panel3 = new JPanel();
		optSNoAuto = new JRadioButton();
		optSNoBarCode = new JRadioButton();
		pnlSNoAuto = new JPanel();
		optSNoGlobal = new JRadioButton();
		optSNoModelSpec = new JRadioButton();
		lblPumpSNo = new JLabel();
		txtRecentPumpNo = new JTextField();
		lblPumpSNoNext = new JLabel();
		lblNextPumpSNo = new JLabel();
		lblMsno = new JLabel();
		txtRecentMotNo = new JTextField();
		lblMotSNoNext = new JLabel();
		lblNextMotSNo = new JLabel();
		chkSNo = new JCheckBox();
		pnlSNoBarcode = new JPanel();
		chkPumpModel = new JCheckBox();
		lblModelPos = new JLabel();
		txtModelStartPos = new JTextField();
		lblModelPos2 = new JLabel();
		txtModelEndPos = new JTextField();
		pnlAuto = new JPanel();
		optHeadAuto = new JRadioButton();
		optHeadMan = new JRadioButton();
		cmdSave = new JButton();
		cmdExit = new JButton();
		pnlRt = new JPanel();
		optRtResLimit = new JRadioButton();
		optRtResGraph = new JRadioButton();

		//======== this ========
		setTitle("Doer PumpView: Test Settings");
		setFont(new Font("Arial", Font.PLAIN, 12));
		setResizable(false);
		setModal(true);
		Container contentPane = getContentPane();
		contentPane.setLayout(new TableLayout(new double[][] {
			{5, TableLayout.FILL, 5},
			{5, TableLayout.PREFERRED, 5}}));
		((TableLayout)contentPane.getLayout()).setHGap(5);
		((TableLayout)contentPane.getLayout()).setVGap(5);

		//======== pnlTest ========
		{
			pnlTest.setBorder(new TitledBorder(null, "Test Settings", TitledBorder.CENTER, TitledBorder.TOP,
				new Font("Arial", Font.BOLD, 14), Color.blue));
			pnlTest.setFocusable(false);
			pnlTest.setLayout(new TableLayout(new double[][] {
				{TableLayout.FILL, TableLayout.FILL},
				{TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED}}));
			((TableLayout)pnlTest.getLayout()).setHGap(5);
			((TableLayout)pnlTest.getLayout()).setVGap(5);

			//======== panel1 ========
			{
				panel1.setBorder(new TitledBorder(null, "Test Mode", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
					new Font("Arial", Font.BOLD, 12)));
				panel1.setLayout(new TableLayout(new double[][] {
					{113, TableLayout.FILL},
					{TableLayout.PREFERRED}}));
				((TableLayout)panel1.getLayout()).setHGap(5);
				((TableLayout)panel1.getLayout()).setVGap(5);

				//---- optManual ----
				optManual.setText("Manual Entry");
				optManual.setFont(new Font("Arial", Font.PLAIN, 12));
				optManual.addItemListener(e -> optManualItemStateChanged());
				panel1.add(optManual, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- optAutomated ----
				optAutomated.setText("Automated");
				optAutomated.setFont(new Font("Arial", Font.PLAIN, 12));
				optAutomated.addItemListener(e -> optAutomatedItemStateChanged());
				panel1.add(optAutomated, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			pnlTest.add(panel1, new TableLayoutConstraints(0, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//======== panel2 ========
			{
				panel2.setBorder(new TitledBorder(null, "Test Start And Capture Signal", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
					new Font("Arial", Font.BOLD, 12)));
				panel2.setLayout(new TableLayout(new double[][] {
					{113, TableLayout.FILL},
					{TableLayout.PREFERRED, TableLayout.PREFERRED}}));
				((TableLayout)panel2.getLayout()).setHGap(5);
				((TableLayout)panel2.getLayout()).setVGap(5);

				//---- optSigKey ----
				optSigKey.setText("Keyboard");
				optSigKey.setFont(new Font("Arial", Font.PLAIN, 12));
				optSigKey.addItemListener(e -> optSigKeyItemStateChanged());
				panel2.add(optSigKey, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- optSigPush ----
				optSigPush.setText("Push Button");
				optSigPush.setFont(new Font("Arial", Font.PLAIN, 12));
				panel2.add(optSigPush, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- chkMulti ----
				chkMulti.setText("Remote-stations with dedicated HMIs (Test different stations simultaneously)");
				chkMulti.setFont(new Font("Arial", Font.PLAIN, 12));
				panel2.add(chkMulti, new TableLayoutConstraints(0, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			pnlTest.add(panel2, new TableLayoutConstraints(0, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//======== panel3 ========
			{
				panel3.setBorder(new TitledBorder(null, "Serial Number", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
					new Font("Arial", Font.BOLD, 12)));
				panel3.setLayout(new TableLayout(new double[][] {
					{TableLayout.FILL, TableLayout.PREFERRED},
					{TableLayout.PREFERRED, TableLayout.PREFERRED}}));
				((TableLayout)panel3.getLayout()).setHGap(5);
				((TableLayout)panel3.getLayout()).setVGap(5);

				//---- optSNoAuto ----
				optSNoAuto.setText("Auto Generation");
				optSNoAuto.setFont(new Font("Arial", Font.PLAIN, 12));
				optSNoAuto.addItemListener(e -> optSNoAutoItemStateChanged());
				panel3.add(optSNoAuto, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- optSNoBarCode ----
				optSNoBarCode.setText("Barcode Scanner");
				optSNoBarCode.setFont(new Font("Arial", Font.PLAIN, 12));
				panel3.add(optSNoBarCode, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//======== pnlSNoAuto ========
				{
					pnlSNoAuto.setBorder(new TitledBorder(""));
					pnlSNoAuto.setLayout(new TableLayout(new double[][] {
						{TableLayout.PREFERRED, TableLayout.FILL},
						{TableLayout.PREFERRED, TableLayout.PREFERRED, 20, TableLayout.PREFERRED, 20, TableLayout.PREFERRED}}));
					((TableLayout)pnlSNoAuto.getLayout()).setHGap(5);
					((TableLayout)pnlSNoAuto.getLayout()).setVGap(5);

					//---- optSNoGlobal ----
					optSNoGlobal.setText("Global");
					optSNoGlobal.setFont(new Font("Arial", Font.PLAIN, 12));
					optSNoGlobal.setToolTipText("Choose this if pump sno format is standard for all models");
					optSNoGlobal.setOpaque(false);
					optSNoGlobal.addItemListener(e -> optSNoGlobalItemStateChanged());
					pnlSNoAuto.add(optSNoGlobal, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- optSNoModelSpec ----
					optSNoModelSpec.setText("Model Specific");
					optSNoModelSpec.setFont(new Font("Arial", Font.PLAIN, 12));
					optSNoModelSpec.setToolTipText("Choose this if pump sno format is based on pump model");
					optSNoModelSpec.setOpaque(false);
					pnlSNoAuto.add(optSNoModelSpec, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblPumpSNo ----
					lblPumpSNo.setText("Recent Pump SNo.");
					lblPumpSNo.setFont(new Font("Arial", Font.PLAIN, 12));
					pnlSNoAuto.add(lblPumpSNo, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- txtRecentPumpNo ----
					txtRecentPumpNo.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
					txtRecentPumpNo.setToolTipText("Serial number of last tested pump");
					txtRecentPumpNo.addKeyListener(new KeyAdapter() {
						@Override
						public void keyReleased(KeyEvent e) {
							txtRecentPumpNoKeyReleased();
						}
					});
					pnlSNoAuto.add(txtRecentPumpNo, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblPumpSNoNext ----
					lblPumpSNoNext.setText("Next Will Be");
					lblPumpSNoNext.setFont(new Font("Arial", Font.PLAIN, 12));
					pnlSNoAuto.add(lblPumpSNoNext, new TableLayoutConstraints(0, 2, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblNextPumpSNo ----
					lblNextPumpSNo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
					lblNextPumpSNo.setOpaque(true);
					lblNextPumpSNo.setBackground(Color.lightGray);
					pnlSNoAuto.add(lblNextPumpSNo, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblMsno ----
					lblMsno.setText("Recent Motor SNo.");
					lblMsno.setFont(new Font("Arial", Font.PLAIN, 12));
					pnlSNoAuto.add(lblMsno, new TableLayoutConstraints(0, 3, 0, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

					//---- txtRecentMotNo ----
					txtRecentMotNo.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
					txtRecentMotNo.setToolTipText("Serial number of last tested motor");
					txtRecentMotNo.addKeyListener(new KeyAdapter() {
						@Override
						public void keyReleased(KeyEvent e) {
							txtRecentMotNoKeyReleased();
						}
					});
					pnlSNoAuto.add(txtRecentMotNo, new TableLayoutConstraints(1, 3, 1, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblMotSNoNext ----
					lblMotSNoNext.setText("Next Will Be");
					lblMotSNoNext.setFont(new Font("Arial", Font.PLAIN, 12));
					pnlSNoAuto.add(lblMotSNoNext, new TableLayoutConstraints(0, 4, 0, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblNextMotSNo ----
					lblNextMotSNo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
					lblNextMotSNo.setOpaque(true);
					lblNextMotSNo.setBackground(Color.lightGray);
					pnlSNoAuto.add(lblNextMotSNo, new TableLayoutConstraints(1, 4, 1, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- chkSNo ----
					chkSNo.setText("Confirm SNo. Before Assigning To A Test");
					chkSNo.setFont(new Font("Arial", Font.PLAIN, 12));
					chkSNo.setMnemonic('C');
					chkSNo.setToolTipText("Enable this in case you want to review the serial numbers before assigning to a test");
					pnlSNoAuto.add(chkSNo, new TableLayoutConstraints(0, 5, 1, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				}
				panel3.add(pnlSNoAuto, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//======== pnlSNoBarcode ========
				{
					pnlSNoBarcode.setBorder(new TitledBorder(""));
					pnlSNoBarcode.setLayout(new TableLayout(new double[][] {
						{TableLayout.PREFERRED, TableLayout.FILL},
						{TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED}}));
					((TableLayout)pnlSNoBarcode.getLayout()).setHGap(5);
					((TableLayout)pnlSNoBarcode.getLayout()).setVGap(5);

					//---- chkPumpModel ----
					chkPumpModel.setText("Detect Pump Model From Barcode");
					chkPumpModel.setFont(new Font("Arial", Font.PLAIN, 12));
					chkPumpModel.setOpaque(false);
					chkPumpModel.addItemListener(e -> chkBarcodeItemStateChanged());
					pnlSNoBarcode.add(chkPumpModel, new TableLayoutConstraints(0, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblModelPos ----
					lblModelPos.setText("Starting Charecter Position");
					lblModelPos.setFont(new Font("Arial", Font.PLAIN, 12));
					pnlSNoBarcode.add(lblModelPos, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- txtModelStartPos ----
					txtModelStartPos.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 12));
					pnlSNoBarcode.add(txtModelStartPos, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblModelPos2 ----
					lblModelPos2.setText("Ending Charecter Position");
					lblModelPos2.setFont(new Font("Arial", Font.PLAIN, 12));
					pnlSNoBarcode.add(lblModelPos2, new TableLayoutConstraints(0, 2, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- txtModelEndPos ----
					txtModelEndPos.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 12));
					pnlSNoBarcode.add(txtModelEndPos, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				}
				panel3.add(pnlSNoBarcode, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			pnlTest.add(panel3, new TableLayoutConstraints(0, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//======== pnlAuto ========
			{
				pnlAuto.setBorder(new TitledBorder(null, "Head Adjustment", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
					new Font("Arial", Font.BOLD, 12)));
				pnlAuto.setLayout(new TableLayout(new double[][] {
					{TableLayout.PREFERRED, TableLayout.PREFERRED},
					{TableLayout.PREFERRED}}));
				((TableLayout)pnlAuto.getLayout()).setHGap(5);
				((TableLayout)pnlAuto.getLayout()).setVGap(5);

				//---- optHeadAuto ----
				optHeadAuto.setText("Automated Valve Control");
				optHeadAuto.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlAuto.add(optHeadAuto, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- optHeadMan ----
				optHeadMan.setText("Manual");
				optHeadMan.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlAuto.add(optHeadMan, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			pnlTest.add(pnlAuto, new TableLayoutConstraints(0, 4, 1, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdSave ----
			cmdSave.setText("Save");
			cmdSave.setFont(new Font("Arial", Font.PLAIN, 14));
			cmdSave.setIcon(new ImageIcon(getClass().getResource("/img/save.PNG")));
			cmdSave.setToolTipText("Click on this to save the changes");
			cmdSave.setMnemonic('S');
			cmdSave.addActionListener(e -> cmdSaveActionPerformed());
			pnlTest.add(cmdSave, new TableLayoutConstraints(0, 5, 0, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdExit ----
			cmdExit.setText("<html>Close&nbsp;&nbsp<font size=-2>[Esc]</html>");
			cmdExit.setFont(new Font("Arial", Font.PLAIN, 14));
			cmdExit.setIcon(new ImageIcon(getClass().getResource("/img/exit.PNG")));
			cmdExit.setToolTipText("Click on this to close this window");
			cmdExit.addActionListener(e -> cmdExitActionPerformed());
			pnlTest.add(cmdExit, new TableLayoutConstraints(1, 5, 1, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//======== pnlRt ========
			{
				pnlRt.setBorder(new TitledBorder(null, "Routine Test Result", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
					new Font("Arial", Font.BOLD, 12)));
				pnlRt.setLayout(new TableLayout(new double[][] {
					{TableLayout.PREFERRED, TableLayout.PREFERRED},
					{TableLayout.PREFERRED}}));
				((TableLayout)pnlRt.getLayout()).setHGap(5);
				((TableLayout)pnlRt.getLayout()).setVGap(5);

				//---- optRtResLimit ----
				optRtResLimit.setText("Based On Limits Declared In Pump Master");
				optRtResLimit.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlRt.add(optRtResLimit, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- optRtResGraph ----
				optRtResGraph.setText("Based On Performance Graph");
				optRtResGraph.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlRt.add(optRtResGraph, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			pnlTest.add(pnlRt, new TableLayoutConstraints(0, 3, 1, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		}
		contentPane.add(pnlTest, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		pack();
		setLocationRelativeTo(getOwner());

		//---- buttonGroup1 ----
		ButtonGroup buttonGroup1 = new ButtonGroup();
		buttonGroup1.add(optManual);
		buttonGroup1.add(optAutomated);

		//---- buttonGroup2 ----
		ButtonGroup buttonGroup2 = new ButtonGroup();
		buttonGroup2.add(optSigKey);
		buttonGroup2.add(optSigPush);

		//---- buttonGroup4 ----
		ButtonGroup buttonGroup4 = new ButtonGroup();
		buttonGroup4.add(optSNoAuto);
		buttonGroup4.add(optSNoBarCode);

		//---- buttonGroup5 ----
		ButtonGroup buttonGroup5 = new ButtonGroup();
		buttonGroup5.add(optSNoGlobal);
		buttonGroup5.add(optSNoModelSpec);

		//---- buttonGroup3 ----
		ButtonGroup buttonGroup3 = new ButtonGroup();
		buttonGroup3.add(optHeadAuto);
		buttonGroup3.add(optHeadMan);

		//---- buttonGroup6 ----
		ButtonGroup buttonGroup6 = new ButtonGroup();
		buttonGroup6.add(optRtResLimit);
		buttonGroup6.add(optRtResGraph);
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
		
		// CUSTOM CODE - BEGIN
		associateFunctionKeys();
		// CUSTOM CODE - END
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel pnlTest;
	private JPanel panel1;
	private JRadioButton optManual;
	private JRadioButton optAutomated;
	private JPanel panel2;
	private JRadioButton optSigKey;
	private JRadioButton optSigPush;
	private JCheckBox chkMulti;
	private JPanel panel3;
	private JRadioButton optSNoAuto;
	private JRadioButton optSNoBarCode;
	private JPanel pnlSNoAuto;
	private JRadioButton optSNoGlobal;
	private JRadioButton optSNoModelSpec;
	private JLabel lblPumpSNo;
	private JTextField txtRecentPumpNo;
	private JLabel lblPumpSNoNext;
	private JLabel lblNextPumpSNo;
	private JLabel lblMsno;
	private JTextField txtRecentMotNo;
	private JLabel lblMotSNoNext;
	private JLabel lblNextMotSNo;
	private JCheckBox chkSNo;
	private JPanel pnlSNoBarcode;
	private JCheckBox chkPumpModel;
	private JLabel lblModelPos;
	private JTextField txtModelStartPos;
	private JLabel lblModelPos2;
	private JTextField txtModelEndPos;
	private JPanel pnlAuto;
	private JRadioButton optHeadAuto;
	private JRadioButton optHeadMan;
	private JButton cmdSave;
	private JButton cmdExit;
	private JPanel pnlRt;
	private JRadioButton optRtResLimit;
	private JRadioButton optRtResGraph;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
	// CUSTOM CODE - BEGIN
	String[] testTypes;
	
	Boolean orgIsPrim = false;
	String curSel = "";
	int oldRtCount = 0;
	int oldTypeCount = 0;
	int newRtCount = 0;
	int newTypeCount = 0;
	
	PumpView frmMain = null;
	
	private Cursor waitCursor = new Cursor(Cursor.WAIT_CURSOR);
	private Cursor defCursor = new Cursor(Cursor.DEFAULT_CURSOR);
	
	// CUSTOM CODE - END
}
