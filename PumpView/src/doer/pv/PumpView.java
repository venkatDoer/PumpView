package doer.pv;
/*
 * Created by JFormDesigner on Fri Nov 30 14:28:53 IST 2012
 */

import java.awt.*;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EventObject;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYDrawableAnnotation;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;

import doer.io.Database;
import doer.io.DeviceModbusReader;
import doer.io.DeviceSerialReader;
import doer.io.MyResultSet;
import doer.io.Station;
import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

/**
 * @author VENKATESAN SELVARAJ @ Doer Automation
 */
public class PumpView extends JFrame {
	public PumpView() {
		initComponents();
		customInit();
	}

	private void cmdSaveActionPerformed() {
		try {
			if (JOptionPane.showConfirmDialog(this, "Do you want to save entire table?" +
					"\n'Yes' if have changed readings belong to multiple pump\n" +
					"'No' to save readings of only chosen pump", "Save", JOptionPane.YES_NO_OPTION) == 0) {
				// save entire table
				saveChanges(0, tblTestEntry.getRowCount()-1);
			} else {
				// save only current pump
				Integer interval[] = findRowsInterval();
				saveChanges(interval[0], interval[1]);
			}
			
			// refresh result preview
			try {
				if (!tblTestEntry.getValueAt(tblTestEntry.getSelectedRow(), 0).toString().trim().isEmpty()) {
					refreshResultPreview("Station" + tblTestEntry.getValueAt(tblTestEntry.getSelectedRow(), 0).toString(), tblTestEntry.getValueAt(tblTestEntry.getSelectedRow(), 5).toString(), tblTestEntry.getValueAt(tblTestEntry.getSelectedRow(), 1).toString(), tblTestEntry.getValueAt(tblTestEntry.getSelectedRow(), 3).toString());
				}
			} catch (Exception ge) {
				ge.printStackTrace();
				logError("Warning: Unable to refresh graph for " + tblTestEntry.getValueAt(tblTestEntry.getSelectedRow(), 5).toString() + ": Insufficient values to plot the graph");
			}
		} catch (Exception e) {
			changeApplicationStatus("SAVE FAILED");
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, e.getMessage());
		}
		focusSignalText();
	}
	
	// function to find rows of given sno
	private Integer[] findRowsInterval() {
		Integer cr = tblTestEntry.getSelectedRow();
		Integer interval[] = {cr, cr};
		if (cr >= 0) {
			String slno = tblTestEntry.getValueAt(cr, 5).toString();
			
			// go back and find out start row
			for(int i=cr-1; cr >= 0; i--) {
				if (!tblTestEntry.getValueAt(i, 5).toString().equals(slno)) {
					interval[0] = i+1;
					break;
				}
			}
			// go down and find out last row
			for(int i=cr+1; cr < tblTestEntry.getRowCount(); i++) {
				if (!tblTestEntry.getValueAt(i, 5).toString().equals(slno)) {
					interval[1] = i-1;
					break;
				}
			}
		}
		return interval;
	}

	private void cmdOpenActionPerformed() {
		if (!saved) {
			int res = JOptionPane.showConfirmDialog(this, "Do you want to close current test and open another one?");
			if ( res != 0 ) {
				return;
			}
		}
		saved = false;
		
		FileOpen dlgOpen = new FileOpen(this);
		dlgOpen.setVisible(true);
		focusSignalText();
	}

	private void cmdCaptureActionPerformed() {
		try {
			tblTestEntry.getDefaultEditor(Object.class).stopCellEditing(); // come out table in case
			Station curStation = null;
			int curRow = 0;
			// manual entry, no capture
			if (Configuration.TEST_MODE.equals("M")) {
				curStation = stationList.get(Configuration.LAST_USED_STATION);
				curRow = findNextEmptyRow();
				setNextTest(curStation);
				// set date and test number and save it
				setCaptureDateAndSNo(curStation, curRow);
				saveChanges(curRow, curRow);
					
				changeApplicationStatus("NEW TEST ENTRY");
				tblTestEntry.setColumnSelectionInterval(7, 7);
				tblTestEntry.setEditingColumn(7);
				tblTestEntry.requestFocusInWindow();
			
			} else {
				// capture current reading and overwrite the test
				curRow = tblTestEntry.getSelectedRow();
				if (tblTestEntry.getValueAt(curRow, 0).toString().isEmpty()) {
					JOptionPane.showMessageDialog(this, "Please choose a test to recapture the reading");
					return;
				} else if (JOptionPane.showConfirmDialog(this, "Are you sure, you want to recapture the readings for head '" + tblTestEntry.getValueAt(curRow, 2).toString() + "' of pump '" + tblTestEntry.getValueAt(curRow, 5).toString() + "'?\nWARNING: This will overwrite old reading", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == 0) {
					curStation = stationList.get("Station" + tblTestEntry.getValueAt(curRow, 0).toString());
					capture(curStation, curRow);
				} else {
					// no action
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void cmdUndoActionPerformed() {
		
		int curRow = tblTestEntry.getSelectedRow();
		if (curRow < 0 ) {
			JOptionPane.showMessageDialog(this, "Nothing to remove.\nPlease select a reading to be removed");
			return;
		}
		
		if (tblTestEntry.getValueAt(curRow, 0).toString().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Selected row does not have any reading in it.\nPlease select a reading to be removed");
			return;
		}
		int selRows[] = tblTestEntry.getSelectedRows();
		String tmpMsg = selRows.length > 1 ? ("readings from 'row " + (selRows[0]+1) + " to " + (selRows[selRows.length-1]+1)) : ("reading at 'row " + (selRows[0]+1));
		int response = JOptionPane.showConfirmDialog(this, "Are you sure, you want to delete selected " + tmpMsg  + "'?\nWARNING: This will permanently delete this reading from database");
		if (response == 0) {
			// remove one or more selected rows
			String tmpCond = "";
			try {
				for(int i=0; i<selRows.length; i++) {
					tmpCond = Configuration.IS_SNO_GLOBAL.equals("YES") ? "" : ("pump_type_id = '" + stationList.get("Station" + tblTestEntry.getValueAt(selRows[i], 0).toString()).curPumpTypeId + "' and ");
					db.executeUpdate("delete from " + Configuration.READING_DETAIL + " where " + tmpCond + "pump_slno='" + tblTestEntry.getValueAt(selRows[i], 5).toString().trim() + "' and test_name_code='" + tblTestEntry.getValueAt(selRows[i], 2).toString().trim() + "' and rated_volt='" + tblTestEntry.getValueAt(selRows[i], 3).toString().trim() +"'");
					// clear the reading
					for(int j=0; j < tblTestEntry.getColumnCount(); j++) {
						tblTestEntry.setValueAt("", selRows[i], j);
					}
				}
			} catch (SQLException se){
				logError("Error removing reading:" + se.getMessage());
				return;
			}
			
			// get ready for next reading
			tblTestEntry.setRowSelectionInterval(curRow, curRow);
			changeApplicationStatus("READING REMOVED");
		}
	}
	

	private void tglAutoActionPerformed() {
		isAutoCaptureOn = tglAuto.isSelected();
		
		if (isAutoCaptureOn) {
			if (stationList.get(Configuration.LAST_USED_STATION).curPumpType.isEmpty()) {
				JOptionPane.showMessageDialog(this, "Please choose a pump model before starting the test");
				tglAuto.setSelected(false);
				isAutoCaptureOn = false;
				return;
			}
			
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			
			// load recent test details of all stations
			try {
				for(int i=1; i<=stationList.size(); i++) {
					loadRecentTestDetails(stationList.get("Station" + i));
				}
			} catch (Exception e) {
				// ignore
			}

			// start auto capture
			tblTestEntry.getDefaultEditor(Object.class).stopCellEditing(); // come out table in case
			tglAuto.setBackground(Color.GREEN);
			tglAuto.setText("<html><body align='center'>Auto Capture<br><font color=rgb(0,102,0)>ON</font><br><font size=-2>[F12]</html>");
			
			stopAutoCaptureRequested = false;
			threadAutoCaptureList.clear();
			if (Configuration.MULTI_STATION_MODE.equals("YES")) {
				// start thread for each station synchronously
				for(int i=1; i<=Configuration.NUMBER_OF_STATION; i++) {
					threadAutoCaptureList.add( new Thread(new AutoCaptureMS("Station" + i)));
					threadAutoCaptureList.get(i-1).start();
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// ignore
					}
				}
			} else {
				// start thread for chosen station
				System.out.println("starting station " + Configuration.LAST_USED_STATION);
				threadAutoCaptureList.add( new Thread(new AutoCaptureMS(Configuration.LAST_USED_STATION)));
				threadAutoCaptureList.get(0).start();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// ignore
				}
			}
			
			// disable required buttons
			cmdCapture.setEnabled(false);
			cmdUndo.setEnabled(false);
			cmdResetValve.setEnabled(false);
			cmdResetValve.setVisible(Configuration.HEAD_ADJUSTMENT.equals("A"));
		} else {
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			
			stopAutoCaptureRequested = true;
			if (threadAutoCapture != null) {
				threadAutoCapture.interrupt();
				/*while(threadAutoCapture.isAlive()) {
					// wait the thread is goes off
					logError("Auto capture:Waiting as thread is alive", true);
				}*/
			}
			
			// stop multi threading also if any
			for(int i=0; i< threadAutoCaptureList.size(); i++) {
				if (threadAutoCaptureList.get(i) != null) {
					threadAutoCaptureList.get(i).interrupt();
					/*while(threadAutoCaptureList.get(i).isAlive()) {
						// wait until previous thread stops if any
						logError("Auto capture:Waiting as thread is alive", true);
					}*/
				}
			}
			
			changeApplicationStatus("IDLE");
			
			tglAuto.setSelected(false);
			tglAuto.setBackground(Color.RED);
			tglAuto.setText("<html><body align='center'>Auto Capture<br><font color='red'>OFF</font><br><font size=-2>[F12]</html>");
			txtSignal.setBackground(Color.darkGray);
			txtSignal.setText("");
			tglAuto.repaint();
			
			isAutoCaptureOn = false;
			
			// enable required buttons
			cmdCapture.setEnabled(true);
			cmdUndo.setEnabled(true);
			cmdResetValve.setEnabled(true);
			
		}
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}
	
	// function to convert the discharge from lps to other unit
	private String convertDischarge(Float dis, String unit) {
		String cDis = "";
		
		// 1. unit
		if(unit.startsWith("g")) { // liter to gallon
			dis = dis * 0.26417F;
		} else if(unit.contains("sup")) {// meter cubed
			dis = dis * 0.001F;
		}
		
		// 2. time
		if(unit.endsWith("s")) {
			cDis = dotTwo.format(dis);
		} else if(unit.endsWith("m")) {
			dis = dis * 60;
			cDis = dotOne.format(dis);
		} else if(unit.contains("sup")) { // mcph
			dis = dis * 3600;
			cDis = dotTwo.format(dis);
		} else if(unit.endsWith("h")) {
			dis = dis * 3600;
			cDis = dotZero.format(dis);
		}
		
		return cDis;
	}
	
	private Integer convertDischargeForHMI(Float dis, String unit) {
		Integer cDis = 0;
		
		// 1. unit
		if(unit.startsWith("g")) { // liter to gallon
			dis = dis * 0.26417F;
		} else if(unit.contains("sup")) {// meter cubed
			dis = dis * 0.001F;
		}
		
		// 2. time
		if(unit.endsWith("s")) {
			cDis = (int) (Float.valueOf(dotTwo.format(dis)) * 100);
		} else if(unit.endsWith("m")) {
			dis = dis * 60;
			cDis = (int) (Float.valueOf(dotOne.format(dis)) * 10);
		} else if(unit.contains("sup")) { // mcph
			dis = dis * 3600;
			cDis = (int) (Float.valueOf(dotTwo.format(dis)) * 100);
		} else if(unit.endsWith("h")) {
			dis = dis * 3600;
			cDis = (int) (Float.valueOf(dotZero.format(dis)) * 1);
		}
		
		return cDis;
	}
	
	// function to convert the head from m to other unit
	private String convertHead(Float hd, String unit) {
		String cHd = "";
		
		if(unit.equals("ft")) { // to feet
			hd = hd * 3.28084F;
			cHd = dotZero.format(hd);
		} else if(unit.equals("bar")) {// to bar
			hd = hd * 0.0980638F;
			cHd = dotTwo.format(hd);
		} else { // just format the default one mwc
			cHd = dotTwo.format(hd);
		}
		
		return cHd;
	}
	
	private Integer convertHeadForHMI(Float hd, String unit) {
		Integer cHd = 0;
		
		if(unit.equals("ft")) { // to feet
			hd = hd * 3.28084F;
			cHd = (int) (Float.valueOf(dotZero.format(hd)) * 1);
		} else if(unit.equals("bar")) {// to bar
			hd = hd * 0.0980638F;
			cHd = (int) (Float.valueOf(dotTwo.format(hd)) * 100);
		} else { // just format the default one mwc
			cHd = (int) (Float.valueOf(dotTwo.format(hd)) * 100);
		}
		
		return cHd;
	}

	private void cmdDeviceCfgActionPerformed() {
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		DeviceSettings dlgDevSet = new DeviceSettings(this);
		dlgDevSet.setVisible(true);
		this.setCursor(Cursor.getDefaultCursor());
		
		focusSignalText();
	}

	
	private void cmdUserActionPerformed() {
		UserManagement dlgUser = new UserManagement(this);
		dlgUser.setVisible(true);
		
		focusSignalText();
	}

	private void cmdOtherActionPerformed() {
		OtherSettings dlgOther = new OtherSettings(this);
		dlgOther.setVisible(true);
		
		focusSignalText();
	}
	
	private void cmdCaptureCfgActionPerformed() {
		TestSettings dlgCapture = new TestSettings(this);
		dlgCapture.setVisible(true);
		
		focusSignalText();
	}

	private void thisWindowClosing() {
		// close db & dev comm before exit
		try {
			// close connections with devices if open already
			stopThreads();
			for(int i=1; i<=stationList.size(); i++) {
				stationList.get("Station"+i).closeDevices();
			}
			stationList.clear();
			
			// close db connection
			db.closeInstance();
						
		} catch (Exception se) {
			// ignore
		}
		
		System.exit(0);
	}

	private void cmdPumpActionPerformed() {
		PumpType dlgPump = new PumpType(this, Configuration.LAST_USED_PUMP_TYPE_ID, Configuration.LAST_USED_PUMP_TYPE);
		dlgPump.setVisible(true);
		
		focusSignalText();
	}

	private void lblLogoMouseReleased() {
		ContactUs dlgContact = new ContactUs(this);
		dlgContact.setVisible(true);
	}

	private void cmdReportsActionPerformed() {
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		String recentPumpNo = tblTestEntry.getValueAt(tblTestEntry.getSelectedRow(), 5).toString();
		if (recentPumpNo.isEmpty()) {
			recentPumpNo = stationList.get(Configuration.LAST_USED_STATION).lastUsedPumpSNo;
		}
		
		Calendar recentPumpDt = Calendar.getInstance();
		try {
			recentPumpDt.setTime(reqDtFormat.parse(tblTestEntry.getValueAt(tblTestEntry.getSelectedRow(), 4).toString()));
		} catch (ParseException e) {
			// ignore
		}
		
		Reports dlgRpt = new Reports(this,stationList.get(Configuration.LAST_USED_STATION).curPumpType, stationList.get(Configuration.LAST_USED_STATION).curPumpTypeId, recentPumpNo, recentPumpDt, isFlowRange);
		dlgRpt.setVisible(true);
		this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		
		focusSignalText();
	}

	private void lstErrorMouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
			JDialog dlg = new JDialog();
			dlg.setSize(1000,500);
			dlg.setTitle("Error Log: PumpViewPro");
			dlg.setLocationRelativeTo(this);
			JScrollPane scrl = new JScrollPane();
			JTextArea txt = new JTextArea();
			DefaultListModel model = (DefaultListModel) lstError.getModel();
			String err = "";
			for (int i=0; i<model.getSize(); i++) {
				err += model.getElementAt(i) + "\n";
			}
			txt.setText(err);
			scrl.setViewportView(txt);
			dlg.add(scrl);
			dlg.setVisible(true);
		}
		focusSignalText();
	}

	private void lstErrorKeyPressed(KeyEvent e) {
		if (e.getKeyChar() == KeyEvent.VK_DELETE) {
			if (JOptionPane.showConfirmDialog(this, "Sure, clear the error list?") == 0) {
				DefaultListModel model = (DefaultListModel) lstError.getModel();
				model.clear();
				errLogModel.addElement("Description is shown here while an error occurs; Double click to expand, Delete key to clear.");
				lstError.setSelectedIndex(0);
			}
		}
		focusSignalText();
	}

	private void tblTestEntryKeyTyped(KeyEvent e) {
		if (isAutoCaptureOn && Configuration.LAST_USED_SIGNAL_METHOD.equals("K")) {
			if(e.getKeyChar() == KeyEvent.VK_SPACE) {
				tblTestEntry.getDefaultEditor(Object.class).stopCellEditing(); // come out table in case
				txtSignal.requestFocusInWindow();
				txtSignal.setText(txtSignal.getText() + ".");
			}
		}
	}

	private void cmdOutputActionPerformed() {
		ChooseOutput dlgOp = new ChooseOutput(this);
		dlgOp.setVisible(true);
		focusSignalText();
	}

	private void pnlGraphMouseClicked() {
		cmdReportsActionPerformed();
	}

	private void lblResBulbMouseClicked() {
		cmdReportsActionPerformed();
	}

	private void lblResFMouseClicked() {
		cmdReportsActionPerformed();
	}

	private void lblResHMouseClicked() {
		cmdReportsActionPerformed();
	}

	private void lblResPMouseClicked() {
		cmdReportsActionPerformed();
	}

	private void lblResEMouseClicked() {
		cmdReportsActionPerformed();
	}

	private void lblResCMouseClicked() {
		cmdReportsActionPerformed();
	}

	private void cmdResetValveActionPerformed() {
		if(JOptionPane.showConfirmDialog(this, "Sure, you want to reset the valve to FO condition?") == 0) {
			try {
				stationList.get(Configuration.LAST_USED_STATION).writeParamValue("Valve Control Register", isSOFirst ? minV : maxV);
				Integer curSec = 1;
				while (curSec <= valvePerfTime) {
					txtSignal.setText("Resetting valve [" + curSec + "/" + valvePerfTime + " sec]");
					Thread.sleep(1000);
					curSec++;
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(new JDialog(), "Error resetting valve control register");
			}
		}
	}


	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		pnlHead = new JPanel();
		lblComp = new JLabel();
		pnlLive = new JPanel();
		lblLR = new JLabel();
		pnlLROPList = new JPanel();
		pnlTop1 = new JPanel();
		lbllblSHead = new JLabel();
		lbllblDHead = new JLabel();
		lblLiveSHead = new JLabel();
		lblLiveDHead = new JLabel();
		lblFlowLive = new JLabel();
		label10 = new JLabel();
		lblLiveFlow = new JLabel();
		lblLiveSpeed = new JLabel();
		pnlBot = new JPanel();
		label19 = new JLabel();
		label21 = new JLabel();
		lblLiveVolt = new JLabel();
		lblLiveAmps = new JLabel();
		label24 = new JLabel();
		label23 = new JLabel();
		lblLiveWatt = new JLabel();
		lblLiveFreq = new JLabel();
		pnlPump = new JPanel();
		cmdPump = new JButton();
		label2 = new JLabel();
		lblPumpType = new JLabel();
		label5 = new JLabel();
		lblMotType = new JLabel();
		label46 = new JLabel();
		lblMotRt = new JLabel();
		label54 = new JLabel();
		lblAmps = new JLabel();
		lblHdUnit = new JLabel();
		lblHead = new JLabel();
		label4 = new JLabel();
		lblGauge = new JLabel();
		label49 = new JLabel();
		lblPhase = new JLabel();
		lblMotIpL = new JLabel();
		lblMotIp = new JLabel();
		lblDisUnit = new JLabel();
		lblDis = new JLabel();
		label3 = new JLabel();
		lblPipeSize = new JLabel();
		label48 = new JLabel();
		lblVolts = new JLabel();
		lblMotEffL = new JLabel();
		lblMotEff = new JLabel();
		lblHdRgUnit = new JLabel();
		lblHeadRange = new JLabel();
		lblOverAllEffL = new JLabel();
		lblEff = new JLabel();
		pnlSet = new JPanel();
		cmdCaptureCfg = new JButton();
		cmdDeviceCfg = new JButton();
		cmdUser = new JButton();
		cmdOther = new JButton();
		pnlRes = new JPanel();
		lblResSNo = new JLabel();
		pnlResH = new JPanel();
		lblResBulb = new JLabel();
		lblResF = new JLabel();
		lblResH = new JLabel();
		lblResP = new JLabel();
		lblResE = new JLabel();
		lblResC = new JLabel();
		pnlGraph = new JPanel();
		pnlOp = new JPanel();
		cmdOutput = new JButton();
		pnlStationList = new JPanel();
		lblStation1 = new JLabel();
		lblStatus1 = new JLabel();
		pnlTestHead = new JPanel();
		pnlControl = new JPanel();
		lblManual = new JLabel();
		tglAuto = new JToggleButton();
		cmdResetValve = new JButton();
		cmdCapture = new JButton();
		cmdUndo = new JButton();
		cmdSave = new JButton();
		cmdOpen = new JButton();
		cmdReports = new JButton();
		tabTest = new JTabbedPane();
		pnlTest = new JPanel();
		label124 = new JLabel();
		label122 = new JLabel();
		label123 = new JLabel();
		label121 = new JLabel();
		label136 = new JLabel();
		label125 = new JLabel();
		label133 = new JLabel();
		lblTestSHead = new JLabel();
		lblTestSHeadU = new JLabel();
		lblTestDHead = new JLabel();
		lblTestDHeadU = new JLabel();
		lblTestFlow = new JLabel();
		lblFlowUnit = new JLabel();
		lblTestV = new JLabel();
		label137 = new JLabel();
		lblTestCur = new JLabel();
		label138 = new JLabel();
		lblTestPow = new JLabel();
		label139 = new JLabel();
		label132 = new JLabel();
		label1 = new JLabel();
		label144 = new JLabel();
		label141 = new JLabel();
		label142 = new JLabel();
		label143 = new JLabel();
		label140 = new JLabel();
		scrlTest = new JScrollPane();
		tblTestEntry = new JTable();
		pnlOptTest = new JPanel();
		lblOpTestSNo = new JLabel();
		panel2 = new JPanel();
		label51 = new JLabel();
		txtSPSec = new JTextField();
		label52 = new JLabel();
		txtSPSuc = new JTextField();
		label53 = new JLabel();
		pnlFoot = new JPanel();
		lblStatus = new JLabel();
		lblLogo = new JLabel();
		scrollPane1 = new JScrollPane();
		errLogModel = new DefaultListModel();
		lstError = new JList(errLogModel);
		txtSignal = new JTextField();
		lblDBRemote = new JLabel();

		//======== this ========
		setFont(new Font("Arial", Font.PLAIN, 12));
		setTitle("Doer PumpView");
		setIconImage(new ImageIcon(getClass().getResource("/img/app_logo.png")).getImage());
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				thisWindowClosing();
			}
		});
		Container contentPane = getContentPane();
		contentPane.setLayout(new TableLayout(new double[][] {
			{TableLayout.FILL, TableLayout.PREFERRED, 300},
			{TableLayout.PREFERRED, 115, TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED}}));
		((TableLayout)contentPane.getLayout()).setHGap(5);
		((TableLayout)contentPane.getLayout()).setVGap(2);

		//======== pnlHead ========
		{
			pnlHead.setLayout(new TableLayout(new double[][] {
				{133, TableLayout.FILL},
				{30}}));

			//---- lblComp ----
			lblComp.setText("ABC LTD, BANGALORE - 560066");
			lblComp.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 22));
			lblComp.setForeground(Color.white);
			lblComp.setHorizontalAlignment(SwingConstants.CENTER);
			lblComp.setFocusable(false);
			lblComp.setBackground(Color.darkGray);
			lblComp.setOpaque(true);
			pnlHead.add(lblComp, new TableLayoutConstraints(0, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		}
		contentPane.add(pnlHead, new TableLayoutConstraints(0, 0, 2, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

		//======== pnlLive ========
		{
			pnlLive.setBorder(new LineBorder(Color.darkGray, 2));
			pnlLive.setBackground(Color.darkGray);
			pnlLive.setLayout(new TableLayout(new double[][] {
				{145, 145},
				{40, 40, 2, TableLayout.FILL, 2, TableLayout.FILL}}));
			((TableLayout)pnlLive.getLayout()).setHGap(5);

			//---- lblLR ----
			lblLR.setText("Device Live Reading (Output 1)");
			lblLR.setFont(new Font("Arial", Font.PLAIN, 16));
			lblLR.setHorizontalAlignment(SwingConstants.CENTER);
			lblLR.setOpaque(true);
			lblLR.setBackground(Color.darkGray);
			lblLR.setForeground(Color.orange);
			pnlLive.add(lblLR, new TableLayoutConstraints(0, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//======== pnlLROPList ========
			{
				pnlLROPList.setBackground(Color.darkGray);
				pnlLROPList.setLayout(new TableLayout(new double[][] {
					{TableLayout.FILL},
					{TableLayout.FILL}}));
				((TableLayout)pnlLROPList.getLayout()).setHGap(2);
				((TableLayout)pnlLROPList.getLayout()).setVGap(2);
			}
			pnlLive.add(pnlLROPList, new TableLayoutConstraints(0, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//======== pnlTop1 ========
			{
				pnlTop1.setBackground(Color.darkGray);
				pnlTop1.setLayout(new TableLayout(new double[][] {
					{TableLayout.FILL, TableLayout.FILL},
					{TableLayout.PREFERRED, TableLayout.FILL, 25, TableLayout.FILL}}));
				((TableLayout)pnlTop1.getLayout()).setHGap(2);

				//---- lbllblSHead ----
				lbllblSHead.setText("<html>Suc. Head<font size=-1> (mwc)</html>");
				lbllblSHead.setFont(new Font("Arial", Font.PLAIN, 22));
				lbllblSHead.setHorizontalAlignment(SwingConstants.CENTER);
				lbllblSHead.setBackground(Color.lightGray);
				lbllblSHead.setOpaque(true);
				pnlTop1.add(lbllblSHead, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lbllblDHead ----
				lbllblDHead.setText("<html>Del. Head<font size=-1> (mwc)</html>");
				lbllblDHead.setFont(new Font("Arial", Font.PLAIN, 22));
				lbllblDHead.setHorizontalAlignment(SwingConstants.CENTER);
				lbllblDHead.setBackground(Color.lightGray);
				lbllblDHead.setOpaque(true);
				pnlTop1.add(lbllblDHead, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblLiveSHead ----
				lblLiveSHead.setText("-");
				lblLiveSHead.setFont(new Font("Consolas", Font.PLAIN, 42));
				lblLiveSHead.setHorizontalAlignment(SwingConstants.CENTER);
				lblLiveSHead.setBackground(Color.darkGray);
				lblLiveSHead.setOpaque(true);
				lblLiveSHead.setForeground(Color.orange);
				lblLiveSHead.setBorder(new LineBorder(Color.lightGray, 2));
				pnlTop1.add(lblLiveSHead, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblLiveDHead ----
				lblLiveDHead.setText("-");
				lblLiveDHead.setFont(new Font("Consolas", Font.PLAIN, 42));
				lblLiveDHead.setHorizontalAlignment(SwingConstants.CENTER);
				lblLiveDHead.setBackground(Color.darkGray);
				lblLiveDHead.setOpaque(true);
				lblLiveDHead.setForeground(Color.orange);
				lblLiveDHead.setBorder(new LineBorder(Color.lightGray, 2));
				pnlTop1.add(lblLiveDHead, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblFlowLive ----
				lblFlowLive.setText("<html>Flow<font size=-1> (lps)</html>");
				lblFlowLive.setFont(new Font("Arial", Font.PLAIN, 22));
				lblFlowLive.setHorizontalAlignment(SwingConstants.CENTER);
				lblFlowLive.setBackground(Color.lightGray);
				lblFlowLive.setOpaque(true);
				pnlTop1.add(lblFlowLive, new TableLayoutConstraints(0, 2, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label10 ----
				label10.setText("<html>Speed<font size=-1> (rpm)</html>");
				label10.setFont(new Font("Arial", Font.PLAIN, 22));
				label10.setHorizontalAlignment(SwingConstants.CENTER);
				label10.setBackground(Color.lightGray);
				label10.setOpaque(true);
				pnlTop1.add(label10, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblLiveFlow ----
				lblLiveFlow.setText("-");
				lblLiveFlow.setFont(new Font("Consolas", Font.PLAIN, 42));
				lblLiveFlow.setHorizontalAlignment(SwingConstants.CENTER);
				lblLiveFlow.setBackground(Color.darkGray);
				lblLiveFlow.setOpaque(true);
				lblLiveFlow.setForeground(Color.orange);
				lblLiveFlow.setBorder(new LineBorder(Color.lightGray, 2));
				pnlTop1.add(lblLiveFlow, new TableLayoutConstraints(0, 3, 0, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblLiveSpeed ----
				lblLiveSpeed.setText("-");
				lblLiveSpeed.setFont(new Font("Consolas", Font.PLAIN, 42));
				lblLiveSpeed.setHorizontalAlignment(SwingConstants.CENTER);
				lblLiveSpeed.setBackground(Color.darkGray);
				lblLiveSpeed.setOpaque(true);
				lblLiveSpeed.setForeground(Color.orange);
				lblLiveSpeed.setBorder(new LineBorder(Color.lightGray, 2));
				pnlTop1.add(lblLiveSpeed, new TableLayoutConstraints(1, 3, 1, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			pnlLive.add(pnlTop1, new TableLayoutConstraints(0, 3, 1, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//======== pnlBot ========
			{
				pnlBot.setBackground(Color.darkGray);
				pnlBot.setLayout(new TableLayout(new double[][] {
					{TableLayout.FILL, TableLayout.FILL},
					{TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED, TableLayout.FILL}}));
				((TableLayout)pnlBot.getLayout()).setHGap(2);

				//---- label19 ----
				label19.setText("<html>Voltage<font size=-1> (V)</html>");
				label19.setFont(new Font("Arial", Font.PLAIN, 22));
				label19.setHorizontalAlignment(SwingConstants.CENTER);
				label19.setBackground(Color.lightGray);
				label19.setOpaque(true);
				pnlBot.add(label19, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label21 ----
				label21.setText("<html>Current<font size=-1> (A)</html>");
				label21.setFont(new Font("Arial", Font.PLAIN, 22));
				label21.setHorizontalAlignment(SwingConstants.CENTER);
				label21.setBackground(Color.lightGray);
				label21.setOpaque(true);
				pnlBot.add(label21, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblLiveVolt ----
				lblLiveVolt.setText("-");
				lblLiveVolt.setFont(new Font("Consolas", Font.PLAIN, 40));
				lblLiveVolt.setHorizontalAlignment(SwingConstants.CENTER);
				lblLiveVolt.setBackground(Color.darkGray);
				lblLiveVolt.setOpaque(true);
				lblLiveVolt.setForeground(Color.orange);
				lblLiveVolt.setBorder(new LineBorder(Color.lightGray, 2));
				pnlBot.add(lblLiveVolt, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblLiveAmps ----
				lblLiveAmps.setText("-");
				lblLiveAmps.setFont(new Font("Consolas", Font.PLAIN, 40));
				lblLiveAmps.setHorizontalAlignment(SwingConstants.CENTER);
				lblLiveAmps.setBackground(Color.darkGray);
				lblLiveAmps.setOpaque(true);
				lblLiveAmps.setForeground(Color.orange);
				lblLiveAmps.setBorder(new LineBorder(Color.lightGray, 2));
				pnlBot.add(lblLiveAmps, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label24 ----
				label24.setText("<html>Power<font size=-1> (kW)</html>");
				label24.setFont(new Font("Arial", Font.PLAIN, 22));
				label24.setHorizontalAlignment(SwingConstants.CENTER);
				label24.setBackground(Color.lightGray);
				label24.setOpaque(true);
				pnlBot.add(label24, new TableLayoutConstraints(0, 2, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label23 ----
				label23.setText("<html>Freq.<font size=-1> (Hz)</html>");
				label23.setFont(new Font("Arial", Font.PLAIN, 22));
				label23.setHorizontalAlignment(SwingConstants.CENTER);
				label23.setBackground(Color.lightGray);
				label23.setOpaque(true);
				pnlBot.add(label23, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblLiveWatt ----
				lblLiveWatt.setText("-");
				lblLiveWatt.setFont(new Font("Consolas", Font.PLAIN, 40));
				lblLiveWatt.setHorizontalAlignment(SwingConstants.CENTER);
				lblLiveWatt.setBackground(Color.darkGray);
				lblLiveWatt.setOpaque(true);
				lblLiveWatt.setForeground(Color.orange);
				lblLiveWatt.setBorder(new LineBorder(Color.lightGray, 2));
				pnlBot.add(lblLiveWatt, new TableLayoutConstraints(0, 3, 0, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblLiveFreq ----
				lblLiveFreq.setText("-");
				lblLiveFreq.setFont(new Font("Consolas", Font.PLAIN, 40));
				lblLiveFreq.setHorizontalAlignment(SwingConstants.CENTER);
				lblLiveFreq.setBackground(Color.darkGray);
				lblLiveFreq.setOpaque(true);
				lblLiveFreq.setForeground(Color.orange);
				lblLiveFreq.setBorder(new LineBorder(Color.lightGray, 2));
				pnlBot.add(lblLiveFreq, new TableLayoutConstraints(1, 3, 1, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			pnlLive.add(pnlBot, new TableLayoutConstraints(0, 5, 1, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		}
		contentPane.add(pnlLive, new TableLayoutConstraints(2, 3, 2, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

		//======== pnlPump ========
		{
			pnlPump.setBorder(new TitledBorder(null, "Pump Details", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
				new Font("Arial", Font.BOLD, 14), Color.blue));
			pnlPump.setFont(new Font("Arial", Font.PLAIN, 12));
			pnlPump.setLayout(new TableLayout(new double[][] {
				{110, TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL},
				{TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL}}));
			((TableLayout)pnlPump.getLayout()).setHGap(5);
			((TableLayout)pnlPump.getLayout()).setVGap(2);

			//---- cmdPump ----
			cmdPump.setText("<html><body align=\"center\">Choose Pump<br><font size=-2>[F11]</html>");
			cmdPump.setFont(new Font("Arial", Font.PLAIN, 16));
			cmdPump.setIcon(new ImageIcon(getClass().getResource("/img/pump.PNG")));
			cmdPump.setBackground(new Color(0xece9d8));
			cmdPump.setToolTipText("Click on this to choose a type of pump you are going to test");
			cmdPump.setOpaque(false);
			cmdPump.setIconTextGap(2);
			cmdPump.setMargin(new Insets(2, 10, 2, 10));
			cmdPump.addActionListener(e -> cmdPumpActionPerformed());
			pnlPump.add(cmdPump, new TableLayoutConstraints(0, 0, 0, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- label2 ----
			label2.setText("Pump Model");
			label2.setFont(new Font("Arial", Font.BOLD, 16));
			pnlPump.add(label2, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblPumpType ----
			lblPumpType.setFont(new Font("Arial", Font.BOLD, 16));
			lblPumpType.setBackground(Color.lightGray);
			lblPumpType.setBorder(new LineBorder(Color.lightGray, 1, true));
			lblPumpType.setHorizontalTextPosition(SwingConstants.CENTER);
			lblPumpType.setHorizontalAlignment(SwingConstants.CENTER);
			lblPumpType.setForeground(new Color(0x003366));
			pnlPump.add(lblPumpType, new TableLayoutConstraints(2, 0, 4, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- label5 ----
			label5.setText("Motor Model");
			label5.setFont(new Font("Arial", Font.BOLD, 16));
			pnlPump.add(label5, new TableLayoutConstraints(5, 0, 5, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblMotType ----
			lblMotType.setFont(new Font("Arial", Font.BOLD, 16));
			lblMotType.setBackground(Color.lightGray);
			lblMotType.setForeground(new Color(0x003366));
			lblMotType.setHorizontalAlignment(SwingConstants.CENTER);
			lblMotType.setBorder(new LineBorder(Color.lightGray, 1, true));
			pnlPump.add(lblMotType, new TableLayoutConstraints(6, 0, 8, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- label46 ----
			label46.setText("KW/HP");
			label46.setFont(new Font("Arial", Font.BOLD, 12));
			label46.setOpaque(true);
			pnlPump.add(label46, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblMotRt ----
			lblMotRt.setFont(new Font("Arial", Font.BOLD, 14));
			lblMotRt.setOpaque(true);
			lblMotRt.setForeground(new Color(0x003366));
			pnlPump.add(lblMotRt, new TableLayoutConstraints(2, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- label54 ----
			label54.setText("Max Current (A)");
			label54.setFont(new Font("Arial", Font.BOLD, 12));
			label54.setOpaque(true);
			label54.setForeground(new Color(0x009933));
			pnlPump.add(label54, new TableLayoutConstraints(3, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblAmps ----
			lblAmps.setFont(new Font("Arial", Font.BOLD, 14));
			lblAmps.setOpaque(true);
			lblAmps.setForeground(Color.blue);
			pnlPump.add(lblAmps, new TableLayoutConstraints(4, 1, 4, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblHdUnit ----
			lblHdUnit.setText("Head ()");
			lblHdUnit.setFont(new Font("Arial", Font.BOLD, 12));
			lblHdUnit.setOpaque(true);
			lblHdUnit.setForeground(new Color(0x009933));
			pnlPump.add(lblHdUnit, new TableLayoutConstraints(5, 1, 5, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblHead ----
			lblHead.setFont(new Font("Arial", Font.BOLD, 14));
			lblHead.setOpaque(true);
			lblHead.setForeground(Color.blue);
			pnlPump.add(lblHead, new TableLayoutConstraints(6, 1, 6, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- label4 ----
			label4.setText("Guage Dist (m)");
			label4.setFont(new Font("Arial", Font.BOLD, 12));
			label4.setOpaque(true);
			pnlPump.add(label4, new TableLayoutConstraints(7, 1, 7, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblGauge ----
			lblGauge.setFont(new Font("Arial", Font.BOLD, 14));
			lblGauge.setOpaque(true);
			lblGauge.setForeground(new Color(0x003366));
			pnlPump.add(lblGauge, new TableLayoutConstraints(8, 1, 8, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- label49 ----
			label49.setText("Phase");
			label49.setFont(new Font("Arial", Font.BOLD, 12));
			label49.setOpaque(true);
			pnlPump.add(label49, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblPhase ----
			lblPhase.setFont(new Font("Arial", Font.BOLD, 14));
			lblPhase.setOpaque(true);
			lblPhase.setForeground(new Color(0x003366));
			pnlPump.add(lblPhase, new TableLayoutConstraints(2, 2, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblMotIpL ----
			lblMotIpL.setText("Input Power (kW)");
			lblMotIpL.setFont(new Font("Arial", Font.BOLD, 12));
			lblMotIpL.setOpaque(true);
			pnlPump.add(lblMotIpL, new TableLayoutConstraints(3, 2, 3, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblMotIp ----
			lblMotIp.setFont(new Font("Arial", Font.BOLD, 14));
			lblMotIp.setOpaque(true);
			lblMotIp.setForeground(new Color(0x003366));
			pnlPump.add(lblMotIp, new TableLayoutConstraints(4, 2, 4, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblDisUnit ----
			lblDisUnit.setText("Discharge ()");
			lblDisUnit.setFont(new Font("Arial", Font.BOLD, 12));
			lblDisUnit.setOpaque(true);
			lblDisUnit.setForeground(new Color(0x009933));
			pnlPump.add(lblDisUnit, new TableLayoutConstraints(5, 2, 5, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblDis ----
			lblDis.setFont(new Font("Arial", Font.BOLD, 14));
			lblDis.setOpaque(true);
			lblDis.setForeground(Color.blue);
			pnlPump.add(lblDis, new TableLayoutConstraints(6, 2, 6, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- label3 ----
			label3.setText("Pipe Size (mm)");
			label3.setFont(new Font("Arial", Font.BOLD, 12));
			label3.setOpaque(true);
			pnlPump.add(label3, new TableLayoutConstraints(7, 2, 7, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblPipeSize ----
			lblPipeSize.setFont(new Font("Arial", Font.BOLD, 14));
			lblPipeSize.setOpaque(true);
			lblPipeSize.setForeground(new Color(0x003366));
			pnlPump.add(lblPipeSize, new TableLayoutConstraints(8, 2, 8, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- label48 ----
			label48.setText("Voltage (V)");
			label48.setFont(new Font("Arial", Font.BOLD, 12));
			label48.setOpaque(true);
			pnlPump.add(label48, new TableLayoutConstraints(1, 3, 1, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblVolts ----
			lblVolts.setFont(new Font("Arial", Font.BOLD, 14));
			lblVolts.setOpaque(true);
			lblVolts.setForeground(new Color(0x003366));
			pnlPump.add(lblVolts, new TableLayoutConstraints(2, 3, 2, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblMotEffL ----
			lblMotEffL.setText("Motor Eff. (%)");
			lblMotEffL.setFont(new Font("Arial", Font.BOLD, 12));
			lblMotEffL.setOpaque(true);
			pnlPump.add(lblMotEffL, new TableLayoutConstraints(3, 3, 3, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblMotEff ----
			lblMotEff.setFont(new Font("Arial", Font.BOLD, 14));
			lblMotEff.setOpaque(true);
			lblMotEff.setForeground(new Color(0x003366));
			pnlPump.add(lblMotEff, new TableLayoutConstraints(4, 3, 4, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblHdRgUnit ----
			lblHdRgUnit.setText("Head Range");
			lblHdRgUnit.setFont(new Font("Arial", Font.BOLD, 12));
			lblHdRgUnit.setOpaque(true);
			lblHdRgUnit.setForeground(new Color(0x009933));
			pnlPump.add(lblHdRgUnit, new TableLayoutConstraints(5, 3, 5, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblHeadRange ----
			lblHeadRange.setFont(new Font("Arial", Font.BOLD, 14));
			lblHeadRange.setOpaque(true);
			lblHeadRange.setForeground(Color.blue);
			pnlPump.add(lblHeadRange, new TableLayoutConstraints(6, 3, 6, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblOverAllEffL ----
			lblOverAllEffL.setText("Overall Eff. (%)");
			lblOverAllEffL.setFont(new Font("Arial", Font.BOLD, 12));
			lblOverAllEffL.setOpaque(true);
			lblOverAllEffL.setForeground(new Color(0x009933));
			pnlPump.add(lblOverAllEffL, new TableLayoutConstraints(7, 3, 7, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblEff ----
			lblEff.setFont(new Font("Arial", Font.BOLD, 14));
			lblEff.setOpaque(true);
			lblEff.setForeground(Color.blue);
			pnlPump.add(lblEff, new TableLayoutConstraints(8, 3, 8, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		}
		contentPane.add(pnlPump, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

		//======== pnlSet ========
		{
			pnlSet.setBorder(new TitledBorder(null, "Settings", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
				new Font("Arial", Font.BOLD, 14), Color.blue));
			pnlSet.setLayout(new TableLayout(new double[][] {
				{80, 80},
				{TableLayout.FILL, TableLayout.FILL}}));
			((TableLayout)pnlSet.getLayout()).setHGap(5);
			((TableLayout)pnlSet.getLayout()).setVGap(5);

			//---- cmdCaptureCfg ----
			cmdCaptureCfg.setText("Test");
			cmdCaptureCfg.setFont(new Font("Arial", Font.PLAIN, 16));
			cmdCaptureCfg.setMnemonic('T');
			cmdCaptureCfg.setToolTipText("Click on this to configure capture settings like valve/barcode port, timeout delays, etc...");
			cmdCaptureCfg.setIcon(null);
			cmdCaptureCfg.addActionListener(e -> cmdCaptureCfgActionPerformed());
			pnlSet.add(cmdCaptureCfg, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdDeviceCfg ----
			cmdDeviceCfg.setText("Device");
			cmdDeviceCfg.setFont(new Font("Arial", Font.PLAIN, 16));
			cmdDeviceCfg.setMnemonic('D');
			cmdDeviceCfg.setToolTipText("Click on this to configure device id, port, etc...");
			cmdDeviceCfg.setIcon(null);
			cmdDeviceCfg.setMargin(new Insets(2, 2, 2, 2));
			cmdDeviceCfg.addActionListener(e -> cmdDeviceCfgActionPerformed());
			pnlSet.add(cmdDeviceCfg, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdUser ----
			cmdUser.setText("Users");
			cmdUser.setFont(new Font("Arial", Font.PLAIN, 16));
			cmdUser.setMnemonic('U');
			cmdUser.setToolTipText("Click on this add/update/delete users");
			cmdUser.addActionListener(e -> cmdUserActionPerformed());
			pnlSet.add(cmdUser, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdOther ----
			cmdOther.setText("Others");
			cmdOther.setFont(new Font("Arial", Font.PLAIN, 16));
			cmdOther.setMnemonic('O');
			cmdOther.setToolTipText("Click on this to configure other settings like report, automatic backup etc...");
			cmdOther.addActionListener(e -> cmdOtherActionPerformed());
			pnlSet.add(cmdOther, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		}
		contentPane.add(pnlSet, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

		//======== pnlRes ========
		{
			pnlRes.setBorder(new TitledBorder(null, "Result Preview", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
				new Font("Arial", Font.BOLD, 14), Color.blue));
			pnlRes.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			pnlRes.setLayout(new TableLayout(new double[][] {
				{90, TableLayout.FILL},
				{25, 135}}));

			//---- lblResSNo ----
			lblResSNo.setHorizontalAlignment(SwingConstants.CENTER);
			lblResSNo.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 16));
			lblResSNo.setBackground(Color.darkGray);
			lblResSNo.setOpaque(true);
			lblResSNo.setForeground(Color.cyan);
			pnlRes.add(lblResSNo, new TableLayoutConstraints(0, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//======== pnlResH ========
			{
				pnlResH.setLayout(new TableLayout(new double[][] {
					{TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL},
					{TableLayout.FILL, 30}}));
				((TableLayout)pnlResH.getLayout()).setHGap(1);
				((TableLayout)pnlResH.getLayout()).setVGap(1);

				//---- lblResBulb ----
				lblResBulb.setIcon(new ImageIcon(getClass().getResource("/img/idle.png")));
				lblResBulb.setHorizontalAlignment(SwingConstants.CENTER);
				lblResBulb.setOpaque(true);
				lblResBulb.setBackground(Color.gray);
				lblResBulb.setToolTipText("Click on this to see full report");
				lblResBulb.setText("IDLE");
				lblResBulb.setFont(new Font("Arial", Font.BOLD, 12));
				lblResBulb.setHorizontalTextPosition(SwingConstants.CENTER);
				lblResBulb.setVerticalTextPosition(SwingConstants.BOTTOM);
				lblResBulb.setIconTextGap(1);
				lblResBulb.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						lblResBulbMouseClicked();
					}
				});
				pnlResH.add(lblResBulb, new TableLayoutConstraints(0, 0, 4, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblResF ----
				lblResF.setText("F");
				lblResF.setHorizontalAlignment(SwingConstants.CENTER);
				lblResF.setFont(new Font("Arial", Font.BOLD, 14));
				lblResF.setOpaque(true);
				lblResF.setBackground(Color.gray);
				lblResF.setToolTipText("Flow");
				lblResF.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						lblResFMouseClicked();
					}
				});
				pnlResH.add(lblResF, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblResH ----
				lblResH.setText("H");
				lblResH.setHorizontalAlignment(SwingConstants.CENTER);
				lblResH.setFont(new Font("Arial", Font.BOLD, 14));
				lblResH.setOpaque(true);
				lblResH.setBackground(Color.gray);
				lblResH.setToolTipText("Head");
				lblResH.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						lblResHMouseClicked();
					}
				});
				pnlResH.add(lblResH, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblResP ----
				lblResP.setText("P");
				lblResP.setHorizontalAlignment(SwingConstants.CENTER);
				lblResP.setFont(new Font("Arial", Font.BOLD, 14));
				lblResP.setOpaque(true);
				lblResP.setBackground(Color.gray);
				lblResP.setToolTipText("Power");
				lblResP.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						lblResPMouseClicked();
					}
				});
				pnlResH.add(lblResP, new TableLayoutConstraints(2, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblResE ----
				lblResE.setText("E");
				lblResE.setHorizontalAlignment(SwingConstants.CENTER);
				lblResE.setFont(new Font("Arial", Font.BOLD, 14));
				lblResE.setOpaque(true);
				lblResE.setBackground(Color.gray);
				lblResE.setToolTipText("Efficiency");
				lblResE.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						lblResEMouseClicked();
					}
				});
				pnlResH.add(lblResE, new TableLayoutConstraints(3, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblResC ----
				lblResC.setText("C");
				lblResC.setHorizontalAlignment(SwingConstants.CENTER);
				lblResC.setFont(new Font("Arial", Font.BOLD, 14));
				lblResC.setOpaque(true);
				lblResC.setBackground(Color.gray);
				lblResC.setToolTipText("Current");
				lblResC.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						lblResCMouseClicked();
					}
				});
				pnlResH.add(lblResC, new TableLayoutConstraints(4, 1, 4, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			pnlRes.add(pnlResH, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//======== pnlGraph ========
			{
				pnlGraph.setBackground(Color.white);
				pnlGraph.setAutoscrolls(true);
				pnlGraph.setBorder(new LineBorder(Color.lightGray));
				pnlGraph.setEnabled(false);
				pnlGraph.setToolTipText("Click on this to see full report");
				pnlGraph.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						pnlGraphMouseClicked();
					}
				});
				pnlGraph.setLayout(new TableLayout(new double[][] {
					{TableLayout.FILL},
					{TableLayout.FILL}}));
				((TableLayout)pnlGraph.getLayout()).setHGap(1);
				((TableLayout)pnlGraph.getLayout()).setVGap(1);
			}
			pnlRes.add(pnlGraph, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		}
		contentPane.add(pnlRes, new TableLayoutConstraints(2, 1, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

		//======== pnlOp ========
		{
			pnlOp.setBorder(new TitledBorder(null, "Test Station & Output", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
				new Font("Arial", Font.BOLD, 14), Color.blue));
			pnlOp.setLayout(new TableLayout(new double[][] {
				{107, TableLayout.FILL},
				{TableLayout.PREFERRED}}));
			((TableLayout)pnlOp.getLayout()).setHGap(5);

			//---- cmdOutput ----
			cmdOutput.setText("<html><body align=\"center\">Station1<font size=-1><br>[PR1 FL1]</html>");
			cmdOutput.setFont(new Font("Arial", Font.BOLD, 16));
			cmdOutput.setIconTextGap(0);
			cmdOutput.setMargin(new Insets(2, 10, 2, 10));
			cmdOutput.setMnemonic('O');
			cmdOutput.setToolTipText("Click on this to change output settings");
			cmdOutput.addActionListener(e -> cmdOutputActionPerformed());
			pnlOp.add(cmdOutput, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//======== pnlStationList ========
			{
				pnlStationList.setBackground(Color.darkGray);
				pnlStationList.setBorder(new LineBorder(Color.darkGray));
				pnlStationList.setLayout(new TableLayout(new double[][] {
					{TableLayout.FILL},
					{TableLayout.FILL, TableLayout.FILL}}));
				((TableLayout)pnlStationList.getLayout()).setHGap(1);
				((TableLayout)pnlStationList.getLayout()).setVGap(1);

				//---- lblStation1 ----
				lblStation1.setText("1");
				lblStation1.setHorizontalAlignment(SwingConstants.CENTER);
				lblStation1.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 16));
				lblStation1.setBackground(Color.gray);
				lblStation1.setOpaque(true);
				pnlStationList.add(lblStation1, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblStatus1 ----
				lblStatus1.setText("IDLE");
				lblStatus1.setFont(new Font("Arial", Font.BOLD, 14));
				lblStatus1.setHorizontalTextPosition(SwingConstants.LEADING);
				lblStatus1.setHorizontalAlignment(SwingConstants.CENTER);
				lblStatus1.setBackground(Color.gray);
				lblStatus1.setOpaque(true);
				pnlStationList.add(lblStatus1, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			pnlOp.add(pnlStationList, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		}
		contentPane.add(pnlOp, new TableLayoutConstraints(0, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

		//======== pnlTestHead ========
		{
			pnlTestHead.setBorder(null);
			pnlTestHead.setLayout(new TableLayout(new double[][] {
				{TableLayout.PREFERRED, TableLayout.FILL},
				{TableLayout.FILL}}));
			((TableLayout)pnlTestHead.getLayout()).setHGap(5);
			((TableLayout)pnlTestHead.getLayout()).setVGap(5);

			//======== pnlControl ========
			{
				pnlControl.setLayout(new TableLayout(new double[][] {
					{111},
					{TableLayout.FILL, TableLayout.PREFERRED, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL}}));
				((TableLayout)pnlControl.getLayout()).setHGap(5);
				((TableLayout)pnlControl.getLayout()).setVGap(2);

				//---- lblManual ----
				lblManual.setText("Manual Entry Mode");
				lblManual.setFont(new Font("Arial", Font.PLAIN, 11));
				lblManual.setHorizontalAlignment(SwingConstants.CENTER);
				lblManual.setBackground(new Color(0x009933));
				lblManual.setOpaque(true);
				lblManual.setForeground(Color.white);
				lblManual.setBorder(new LineBorder(new Color(0x006633)));
				pnlControl.add(lblManual, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- tglAuto ----
				tglAuto.setFont(new Font("Arial", Font.PLAIN, 16));
				tglAuto.setBackground(Color.red);
				tglAuto.setToolTipText("Turn on/off the auto capture");
				tglAuto.setHorizontalTextPosition(SwingConstants.CENTER);
				tglAuto.setVerticalTextPosition(SwingConstants.TOP);
				tglAuto.setText("<html><body align='center'>Auto Capture<br><font color='red'>OFF</font><br><font size=-2>[F12]</html>");
				tglAuto.setBorderPainted(false);
				tglAuto.addActionListener(e -> tglAutoActionPerformed());
				pnlControl.add(tglAuto, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmdResetValve ----
				cmdResetValve.setText("Reset Valve");
				cmdResetValve.setFont(new Font("Arial", Font.PLAIN, 14));
				cmdResetValve.setMnemonic('R');
				cmdResetValve.setToolTipText("Click on this add/update/delete users");
				cmdResetValve.addActionListener(e -> cmdResetValveActionPerformed());
				pnlControl.add(cmdResetValve, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmdCapture ----
				cmdCapture.setText("<html><body align=\"center\">Capture <font size=-2>[F8]</html>");
				cmdCapture.setFont(new Font("Arial", Font.PLAIN, 16));
				cmdCapture.setIcon(new ImageIcon(getClass().getResource("/img/add_m.png")));
				cmdCapture.setBackground(new Color(0xece9d8));
				cmdCapture.setToolTipText("Click on this to recapture and overwrite chosen record with current device readings");
				cmdCapture.setHorizontalTextPosition(SwingConstants.CENTER);
				cmdCapture.setVerticalTextPosition(SwingConstants.BOTTOM);
				cmdCapture.setOpaque(false);
				cmdCapture.setMargin(new Insets(2, 10, 2, 10));
				cmdCapture.setInheritsPopupMenu(true);
				cmdCapture.addActionListener(e -> cmdCaptureActionPerformed());
				pnlControl.add(cmdCapture, new TableLayoutConstraints(0, 2, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmdUndo ----
				cmdUndo.setText("<html><body align=\"center\">Remove <font size=-2>[F9]</html>");
				cmdUndo.setFont(new Font("Arial", Font.PLAIN, 16));
				cmdUndo.setIcon(new ImageIcon(getClass().getResource("/img/delete_m.png")));
				cmdUndo.setBackground(new Color(0xece9d8));
				cmdUndo.setToolTipText("Click on this to delete currently selected record");
				cmdUndo.setHorizontalTextPosition(SwingConstants.CENTER);
				cmdUndo.setVerticalTextPosition(SwingConstants.BOTTOM);
				cmdUndo.setOpaque(false);
				cmdUndo.setMargin(new Insets(2, 10, 2, 10));
				cmdUndo.addActionListener(e -> cmdUndoActionPerformed());
				pnlControl.add(cmdUndo, new TableLayoutConstraints(0, 3, 0, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmdSave ----
				cmdSave.setText("<html><body align=\"center\">Save <font size=-2>[F4]</html>");
				cmdSave.setFont(new Font("Arial", Font.PLAIN, 16));
				cmdSave.setIcon(new ImageIcon(getClass().getResource("/img/save_m.png")));
				cmdSave.setBackground(new Color(0xece9d5));
				cmdSave.setToolTipText("Click on this to save current test data");
				cmdSave.setHorizontalTextPosition(SwingConstants.CENTER);
				cmdSave.setVerticalTextPosition(SwingConstants.BOTTOM);
				cmdSave.setOpaque(false);
				cmdSave.setMargin(new Insets(2, 10, 2, 10));
				cmdSave.addActionListener(e -> cmdSaveActionPerformed());
				pnlControl.add(cmdSave, new TableLayoutConstraints(0, 4, 0, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmdOpen ----
				cmdOpen.setText("<html><body align=\"center\">Open <font size=-2>[F5]</html>");
				cmdOpen.setFont(new Font("Arial", Font.PLAIN, 16));
				cmdOpen.setIcon(new ImageIcon(getClass().getResource("/img/open.PNG")));
				cmdOpen.setBackground(new Color(0xece9d5));
				cmdOpen.setToolTipText("Click on this to open existing pump tests");
				cmdOpen.setVerticalTextPosition(SwingConstants.BOTTOM);
				cmdOpen.setHorizontalTextPosition(SwingConstants.CENTER);
				cmdOpen.setOpaque(false);
				cmdOpen.setMargin(new Insets(2, 10, 2, 10));
				cmdOpen.addActionListener(e -> cmdOpenActionPerformed());
				pnlControl.add(cmdOpen, new TableLayoutConstraints(0, 5, 0, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmdReports ----
				cmdReports.setText("<html><body align=\"center\">Reports <font size=-2>[F6]</html>");
				cmdReports.setFont(new Font("Arial", Font.PLAIN, 16));
				cmdReports.setIcon(new ImageIcon(getClass().getResource("/img/Report.png")));
				cmdReports.setBackground(new Color(0xece9d5));
				cmdReports.setToolTipText("Click on this to print current or existing test report");
				cmdReports.setVerticalTextPosition(SwingConstants.BOTTOM);
				cmdReports.setHorizontalTextPosition(SwingConstants.CENTER);
				cmdReports.setOpaque(false);
				cmdReports.setMargin(new Insets(2, 10, 2, 10));
				cmdReports.addActionListener(e -> cmdReportsActionPerformed());
				pnlControl.add(cmdReports, new TableLayoutConstraints(0, 6, 0, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			pnlTestHead.add(pnlControl, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//======== tabTest ========
			{
				tabTest.setFont(new Font("Arial", Font.PLAIN, 14));

				//======== pnlTest ========
				{
					pnlTest.setBackground(Color.lightGray);
					pnlTest.setLayout(new TableLayout(new double[][] {
						{30, 30, 30, 30, 80, 95, 95, 45, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, 55, 18},
						{30, 25, TableLayout.FILL}}));
					((TableLayout)pnlTest.getLayout()).setHGap(1);
					((TableLayout)pnlTest.getLayout()).setVGap(1);

					//---- label124 ----
					label124.setText("Test");
					label124.setBackground(Color.darkGray);
					label124.setHorizontalAlignment(SwingConstants.CENTER);
					label124.setFont(new Font("Arial", Font.BOLD, 14));
					label124.setForeground(Color.white);
					label124.setOpaque(true);
					pnlTest.add(label124, new TableLayoutConstraints(1, 0, 3, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label122 ----
					label122.setText("Date");
					label122.setBackground(Color.darkGray);
					label122.setHorizontalAlignment(SwingConstants.CENTER);
					label122.setFont(new Font("Arial", Font.BOLD, 14));
					label122.setForeground(Color.white);
					label122.setOpaque(true);
					pnlTest.add(label122, new TableLayoutConstraints(4, 0, 4, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label123 ----
					label123.setText("Pump SNo.");
					label123.setBackground(Color.darkGray);
					label123.setHorizontalAlignment(SwingConstants.CENTER);
					label123.setFont(new Font("Arial", Font.BOLD, 14));
					label123.setForeground(Color.white);
					label123.setOpaque(true);
					pnlTest.add(label123, new TableLayoutConstraints(5, 0, 5, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label121 ----
					label121.setText("Motor SNo.");
					label121.setBackground(Color.darkGray);
					label121.setHorizontalAlignment(SwingConstants.CENTER);
					label121.setFont(new Font("Arial", Font.BOLD, 14));
					label121.setForeground(Color.white);
					label121.setOpaque(true);
					pnlTest.add(label121, new TableLayoutConstraints(6, 0, 6, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label136 ----
					label136.setText("Test #");
					label136.setBackground(Color.darkGray);
					label136.setHorizontalAlignment(SwingConstants.CENTER);
					label136.setFont(new Font("Arial", Font.BOLD, 14));
					label136.setForeground(Color.white);
					label136.setOpaque(true);
					pnlTest.add(label136, new TableLayoutConstraints(7, 0, 7, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label125 ----
					label125.setText("Speed");
					label125.setBackground(Color.gray);
					label125.setHorizontalAlignment(SwingConstants.CENTER);
					label125.setFont(new Font("Arial", Font.BOLD, 14));
					label125.setForeground(Color.white);
					label125.setOpaque(true);
					pnlTest.add(label125, new TableLayoutConstraints(8, 0, 8, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label133 ----
					label133.setText("(rpm)");
					label133.setBackground(Color.gray);
					label133.setHorizontalAlignment(SwingConstants.CENTER);
					label133.setFont(new Font("Arial", Font.BOLD, 12));
					label133.setForeground(Color.white);
					label133.setOpaque(true);
					pnlTest.add(label133, new TableLayoutConstraints(8, 1, 8, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblTestSHead ----
					lblTestSHead.setText("Suc. Hd");
					lblTestSHead.setBackground(Color.gray);
					lblTestSHead.setHorizontalAlignment(SwingConstants.CENTER);
					lblTestSHead.setFont(new Font("Arial", Font.BOLD, 14));
					lblTestSHead.setForeground(Color.white);
					lblTestSHead.setOpaque(true);
					pnlTest.add(lblTestSHead, new TableLayoutConstraints(9, 0, 9, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblTestSHeadU ----
					lblTestSHeadU.setText("()");
					lblTestSHeadU.setBackground(Color.gray);
					lblTestSHeadU.setHorizontalAlignment(SwingConstants.CENTER);
					lblTestSHeadU.setFont(new Font("Arial", Font.BOLD, 12));
					lblTestSHeadU.setForeground(Color.white);
					lblTestSHeadU.setOpaque(true);
					pnlTest.add(lblTestSHeadU, new TableLayoutConstraints(9, 1, 9, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblTestDHead ----
					lblTestDHead.setText("Del. Hd");
					lblTestDHead.setBackground(Color.gray);
					lblTestDHead.setHorizontalAlignment(SwingConstants.CENTER);
					lblTestDHead.setFont(new Font("Arial", Font.BOLD, 14));
					lblTestDHead.setForeground(Color.white);
					lblTestDHead.setOpaque(true);
					pnlTest.add(lblTestDHead, new TableLayoutConstraints(10, 0, 10, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblTestDHeadU ----
					lblTestDHeadU.setText("()");
					lblTestDHeadU.setBackground(Color.gray);
					lblTestDHeadU.setHorizontalAlignment(SwingConstants.CENTER);
					lblTestDHeadU.setFont(new Font("Arial", Font.BOLD, 12));
					lblTestDHeadU.setForeground(Color.white);
					lblTestDHeadU.setOpaque(true);
					pnlTest.add(lblTestDHeadU, new TableLayoutConstraints(10, 1, 10, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblTestFlow ----
					lblTestFlow.setText("Flow");
					lblTestFlow.setBackground(Color.gray);
					lblTestFlow.setHorizontalAlignment(SwingConstants.CENTER);
					lblTestFlow.setFont(new Font("Arial", Font.BOLD, 14));
					lblTestFlow.setForeground(Color.white);
					lblTestFlow.setOpaque(true);
					pnlTest.add(lblTestFlow, new TableLayoutConstraints(11, 0, 11, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblFlowUnit ----
					lblFlowUnit.setText("()");
					lblFlowUnit.setBackground(Color.gray);
					lblFlowUnit.setHorizontalAlignment(SwingConstants.CENTER);
					lblFlowUnit.setFont(new Font("Arial", Font.BOLD, 12));
					lblFlowUnit.setForeground(Color.white);
					lblFlowUnit.setOpaque(true);
					pnlTest.add(lblFlowUnit, new TableLayoutConstraints(11, 1, 11, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblTestV ----
					lblTestV.setText("Volt.");
					lblTestV.setBackground(Color.gray);
					lblTestV.setHorizontalAlignment(SwingConstants.CENTER);
					lblTestV.setFont(new Font("Arial", Font.BOLD, 14));
					lblTestV.setForeground(Color.white);
					lblTestV.setOpaque(true);
					pnlTest.add(lblTestV, new TableLayoutConstraints(12, 0, 12, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label137 ----
					label137.setText("(V)");
					label137.setBackground(Color.gray);
					label137.setHorizontalAlignment(SwingConstants.CENTER);
					label137.setFont(new Font("Arial", Font.BOLD, 12));
					label137.setForeground(Color.white);
					label137.setOpaque(true);
					pnlTest.add(label137, new TableLayoutConstraints(12, 1, 12, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblTestCur ----
					lblTestCur.setText("Cur.");
					lblTestCur.setBackground(Color.gray);
					lblTestCur.setHorizontalAlignment(SwingConstants.CENTER);
					lblTestCur.setFont(new Font("Arial", Font.BOLD, 14));
					lblTestCur.setForeground(Color.white);
					lblTestCur.setOpaque(true);
					pnlTest.add(lblTestCur, new TableLayoutConstraints(13, 0, 13, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label138 ----
					label138.setText("(A)");
					label138.setBackground(Color.gray);
					label138.setHorizontalAlignment(SwingConstants.CENTER);
					label138.setFont(new Font("Arial", Font.BOLD, 12));
					label138.setForeground(Color.white);
					label138.setOpaque(true);
					pnlTest.add(label138, new TableLayoutConstraints(13, 1, 13, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblTestPow ----
					lblTestPow.setText("Power");
					lblTestPow.setBackground(Color.gray);
					lblTestPow.setHorizontalAlignment(SwingConstants.CENTER);
					lblTestPow.setFont(new Font("Arial", Font.BOLD, 14));
					lblTestPow.setForeground(Color.white);
					lblTestPow.setOpaque(true);
					pnlTest.add(lblTestPow, new TableLayoutConstraints(14, 0, 14, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label139 ----
					label139.setText("(kW)");
					label139.setBackground(Color.gray);
					label139.setHorizontalAlignment(SwingConstants.CENTER);
					label139.setFont(new Font("Arial", Font.BOLD, 12));
					label139.setForeground(Color.white);
					label139.setOpaque(true);
					pnlTest.add(label139, new TableLayoutConstraints(14, 1, 14, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label132 ----
					label132.setText("Freq.");
					label132.setBackground(Color.gray);
					label132.setHorizontalAlignment(SwingConstants.CENTER);
					label132.setFont(new Font("Arial", Font.BOLD, 14));
					label132.setForeground(Color.white);
					label132.setOpaque(true);
					pnlTest.add(label132, new TableLayoutConstraints(15, 0, 15, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label1 ----
					label1.setText("Remark");
					label1.setBackground(Color.gray);
					label1.setForeground(Color.white);
					label1.setFont(new Font("Arial", Font.BOLD, 14));
					label1.setOpaque(true);
					label1.setHorizontalAlignment(SwingConstants.CENTER);
					pnlTest.add(label1, new TableLayoutConstraints(16, 0, 16, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label144 ----
					label144.setText("St");
					label144.setBackground(Color.darkGray);
					label144.setHorizontalAlignment(SwingConstants.CENTER);
					label144.setFont(new Font("Arial", Font.BOLD, 14));
					label144.setForeground(Color.yellow);
					label144.setOpaque(true);
					pnlTest.add(label144, new TableLayoutConstraints(0, 0, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label141 ----
					label141.setText("Type");
					label141.setBackground(Color.darkGray);
					label141.setHorizontalAlignment(SwingConstants.CENTER);
					label141.setFont(new Font("Arial", Font.BOLD, 9));
					label141.setForeground(Color.white);
					label141.setOpaque(true);
					pnlTest.add(label141, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label142 ----
					label142.setText("Name");
					label142.setBackground(Color.darkGray);
					label142.setHorizontalAlignment(SwingConstants.CENTER);
					label142.setFont(new Font("Arial", Font.BOLD, 9));
					label142.setForeground(Color.white);
					label142.setOpaque(true);
					pnlTest.add(label142, new TableLayoutConstraints(2, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label143 ----
					label143.setText("Rt. V");
					label143.setBackground(Color.darkGray);
					label143.setHorizontalAlignment(SwingConstants.CENTER);
					label143.setFont(new Font("Arial", Font.BOLD, 9));
					label143.setForeground(Color.white);
					label143.setOpaque(true);
					pnlTest.add(label143, new TableLayoutConstraints(3, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label140 ----
					label140.setText("(Hz)");
					label140.setBackground(Color.gray);
					label140.setHorizontalAlignment(SwingConstants.CENTER);
					label140.setFont(new Font("Arial", Font.BOLD, 12));
					label140.setForeground(Color.white);
					label140.setOpaque(true);
					pnlTest.add(label140, new TableLayoutConstraints(15, 1, 15, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//======== scrlTest ========
					{
						scrlTest.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

						//---- tblTestEntry ----
						tblTestEntry.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
						tblTestEntry.setModel(new DefaultTableModel(
							new Object[][] {
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							},
							new String[] {
								null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
							}
						) {
							Class<?>[] columnTypes = new Class<?>[] {
								Object.class, String.class, Object.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class
							};
							boolean[] columnEditable = new boolean[] {
								true, false, false, false, true, true, true, true, true, true, true, true, true, true, true, true, true
							};
							@Override
							public Class<?> getColumnClass(int columnIndex) {
								return columnTypes[columnIndex];
							}
							@Override
							public boolean isCellEditable(int rowIndex, int columnIndex) {
								return columnEditable[columnIndex];
							}
						});
						{
							TableColumnModel cm = tblTestEntry.getColumnModel();
							cm.getColumn(0).setMinWidth(30);
							cm.getColumn(0).setMaxWidth(30);
							cm.getColumn(0).setPreferredWidth(30);
							cm.getColumn(1).setResizable(false);
							cm.getColumn(1).setMinWidth(30);
							cm.getColumn(1).setMaxWidth(30);
							cm.getColumn(1).setPreferredWidth(30);
							cm.getColumn(2).setResizable(false);
							cm.getColumn(2).setMinWidth(31);
							cm.getColumn(2).setMaxWidth(31);
							cm.getColumn(2).setPreferredWidth(31);
							cm.getColumn(3).setResizable(false);
							cm.getColumn(3).setMinWidth(32);
							cm.getColumn(3).setMaxWidth(32);
							cm.getColumn(3).setPreferredWidth(32);
							cm.getColumn(4).setMinWidth(81);
							cm.getColumn(4).setMaxWidth(81);
							cm.getColumn(4).setPreferredWidth(81);
							cm.getColumn(5).setMinWidth(96);
							cm.getColumn(5).setMaxWidth(96);
							cm.getColumn(5).setPreferredWidth(96);
							cm.getColumn(6).setMinWidth(96);
							cm.getColumn(6).setMaxWidth(96);
							cm.getColumn(6).setPreferredWidth(96);
							cm.getColumn(7).setMinWidth(46);
							cm.getColumn(7).setMaxWidth(46);
							cm.getColumn(7).setPreferredWidth(46);
							cm.getColumn(16).setMinWidth(55);
							cm.getColumn(16).setMaxWidth(55);
							cm.getColumn(16).setPreferredWidth(55);
						}
						tblTestEntry.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
						tblTestEntry.setRowHeight(24);
						tblTestEntry.setGridColor(Color.lightGray);
						tblTestEntry.setVerifyInputWhenFocusTarget(false);
						tblTestEntry.setOpaque(false);
						tblTestEntry.addKeyListener(new KeyAdapter() {
							@Override
							public void keyTyped(KeyEvent e) {
								tblTestEntryKeyTyped(e);
							}
						});
						scrlTest.setViewportView(tblTestEntry);
					}
					pnlTest.add(scrlTest, new TableLayoutConstraints(0, 2, 17, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				}
				tabTest.addTab("Tests ", pnlTest);

				//======== pnlOptTest ========
				{
					pnlOptTest.setLayout(new TableLayout(new double[][] {
						{TableLayout.FILL},
						{TableLayout.PREFERRED, TableLayout.PREFERRED}}));
					((TableLayout)pnlOptTest.getLayout()).setHGap(5);
					((TableLayout)pnlOptTest.getLayout()).setVGap(5);

					//---- lblOpTestSNo ----
					lblOpTestSNo.setText("Pump SNo. ");
					lblOpTestSNo.setBackground(Color.darkGray);
					lblOpTestSNo.setHorizontalAlignment(SwingConstants.CENTER);
					lblOpTestSNo.setFont(new Font("Arial", Font.BOLD, 14));
					lblOpTestSNo.setForeground(Color.white);
					lblOpTestSNo.setOpaque(true);
					pnlOptTest.add(lblOpTestSNo, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//======== panel2 ========
					{
						panel2.setBorder(new TitledBorder(null, "Self Priming Test", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
							new Font("Arial", Font.BOLD, 12), Color.blue));
						panel2.setLayout(new TableLayout(new double[][] {
							{TableLayout.PREFERRED, 50, TableLayout.PREFERRED, 50, TableLayout.PREFERRED},
							{TableLayout.PREFERRED}}));
						((TableLayout)panel2.getLayout()).setHGap(5);
						((TableLayout)panel2.getLayout()).setVGap(5);

						//---- label51 ----
						label51.setText("Self Priming Time");
						label51.setFont(new Font("Arial", Font.BOLD, 12));
						label51.setOpaque(true);
						panel2.add(label51, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- txtSPSec ----
						txtSPSec.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
						panel2.add(txtSPSec, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- label52 ----
						label52.setText("Seconds at ");
						label52.setFont(new Font("Arial", Font.BOLD, 12));
						label52.setOpaque(true);
						panel2.add(label52, new TableLayoutConstraints(2, 0, 2, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- txtSPSuc ----
						txtSPSuc.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
						panel2.add(txtSPSuc, new TableLayoutConstraints(3, 0, 3, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- label53 ----
						label53.setText("Meters Suction Lift");
						label53.setFont(new Font("Arial", Font.BOLD, 12));
						label53.setOpaque(true);
						panel2.add(label53, new TableLayoutConstraints(4, 0, 4, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
					}
					pnlOptTest.add(panel2, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				}
				tabTest.addTab("Add-on Tests", pnlOptTest);
			}
			pnlTestHead.add(tabTest, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		}
		contentPane.add(pnlTestHead, new TableLayoutConstraints(0, 3, 1, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

		//======== pnlFoot ========
		{
			pnlFoot.setBorder(null);
			pnlFoot.setBackground(Color.darkGray);
			pnlFoot.setLayout(new TableLayout(new double[][] {
				{100, TableLayout.FILL, 200, 149, 149},
				{1, 35, TableLayout.PREFERRED, 5}}));
			((TableLayout)pnlFoot.getLayout()).setHGap(5);
			((TableLayout)pnlFoot.getLayout()).setVGap(1);

			//---- lblStatus ----
			lblStatus.setText("IDLE");
			lblStatus.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 20));
			lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
			lblStatus.setHorizontalTextPosition(SwingConstants.CENTER);
			lblStatus.setBackground(Color.darkGray);
			lblStatus.setForeground(Color.yellow);
			lblStatus.setFocusable(false);
			lblStatus.setToolTipText("Displays the current status of this application");
			lblStatus.setAlignmentY(0.0F);
			lblStatus.setOpaque(true);
			pnlFoot.add(lblStatus, new TableLayoutConstraints(1, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblLogo ----
			lblLogo.setIcon(new ImageIcon(getClass().getResource("/img/doer_logo.png")));
			lblLogo.setFocusable(false);
			lblLogo.setIconTextGap(5);
			lblLogo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
			lblLogo.setToolTipText("Click on this to know about the software and contact details");
			lblLogo.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseReleased(MouseEvent e) {
					lblLogoMouseReleased();
				}
			});
			pnlFoot.add(lblLogo, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.CENTER, TableLayoutConstraints.FULL));

			//======== scrollPane1 ========
			{
				scrollPane1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
				scrollPane1.setBorder(null);

				//---- lstError ----
				lstError.setForeground(Color.pink);
				lstError.setToolTipText("List of recent capture errors. Double click to expand, Delete key to clear.");
				lstError.setVisibleRowCount(2);
				lstError.setSelectionBackground(Color.darkGray);
				lstError.setSelectionForeground(Color.pink);
				lstError.setBackground(Color.darkGray);
				lstError.setBorder(null);
				lstError.setFont(new Font("Arial", Font.PLAIN, 11));
				lstError.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						lstErrorMouseClicked(e);
					}
				});
				lstError.addKeyListener(new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent e) {
						lstErrorKeyPressed(e);
					}
				});
				scrollPane1.setViewportView(lstError);
			}
			pnlFoot.add(scrollPane1, new TableLayoutConstraints(3, 1, 4, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- txtSignal ----
			txtSignal.setFont(new Font("Arial", Font.BOLD, 17));
			txtSignal.setForeground(Color.white);
			txtSignal.setHorizontalAlignment(SwingConstants.CENTER);
			txtSignal.setBackground(Color.darkGray);
			txtSignal.setBorder(null);
			txtSignal.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
			txtSignal.setCaretColor(Color.lightGray);
			txtSignal.setDisabledTextColor(Color.white);
			pnlFoot.add(txtSignal, new TableLayoutConstraints(1, 2, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblDBRemote ----
			lblDBRemote.setText("Remote DB");
			lblDBRemote.setFont(new Font("Arial", Font.PLAIN, 12));
			lblDBRemote.setHorizontalAlignment(SwingConstants.CENTER);
			lblDBRemote.setOpaque(true);
			lblDBRemote.setBackground(Color.orange);
			lblDBRemote.setMinimumSize(new Dimension(134, 15));
			lblDBRemote.setMaximumSize(new Dimension(134, 15));
			lblDBRemote.setForeground(Color.darkGray);
			pnlFoot.add(lblDBRemote, new TableLayoutConstraints(0, 2, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));
		}
		contentPane.add(pnlFoot, new TableLayoutConstraints(0, 4, 2, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		pack();
		setLocationRelativeTo(null);
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel pnlHead;
	private JLabel lblComp;
	private JPanel pnlLive;
	private JLabel lblLR;
	private JPanel pnlLROPList;
	private JPanel pnlTop1;
	private JLabel lbllblSHead;
	private JLabel lbllblDHead;
	private JLabel lblLiveSHead;
	private JLabel lblLiveDHead;
	private JLabel lblFlowLive;
	private JLabel label10;
	private JLabel lblLiveFlow;
	private JLabel lblLiveSpeed;
	private JPanel pnlBot;
	private JLabel label19;
	private JLabel label21;
	private JLabel lblLiveVolt;
	private JLabel lblLiveAmps;
	private JLabel label24;
	private JLabel label23;
	private JLabel lblLiveWatt;
	private JLabel lblLiveFreq;
	private JPanel pnlPump;
	private JButton cmdPump;
	private JLabel label2;
	private JLabel lblPumpType;
	private JLabel label5;
	private JLabel lblMotType;
	private JLabel label46;
	private JLabel lblMotRt;
	private JLabel label54;
	private JLabel lblAmps;
	private JLabel lblHdUnit;
	private JLabel lblHead;
	private JLabel label4;
	private JLabel lblGauge;
	private JLabel label49;
	private JLabel lblPhase;
	private JLabel lblMotIpL;
	private JLabel lblMotIp;
	private JLabel lblDisUnit;
	private JLabel lblDis;
	private JLabel label3;
	private JLabel lblPipeSize;
	private JLabel label48;
	private JLabel lblVolts;
	private JLabel lblMotEffL;
	private JLabel lblMotEff;
	private JLabel lblHdRgUnit;
	private JLabel lblHeadRange;
	private JLabel lblOverAllEffL;
	private JLabel lblEff;
	private JPanel pnlSet;
	private JButton cmdCaptureCfg;
	private JButton cmdDeviceCfg;
	private JButton cmdUser;
	private JButton cmdOther;
	private JPanel pnlRes;
	private JLabel lblResSNo;
	private JPanel pnlResH;
	private JLabel lblResBulb;
	private JLabel lblResF;
	private JLabel lblResH;
	private JLabel lblResP;
	private JLabel lblResE;
	private JLabel lblResC;
	private JPanel pnlGraph;
	private JPanel pnlOp;
	private JButton cmdOutput;
	private JPanel pnlStationList;
	private JLabel lblStation1;
	private JLabel lblStatus1;
	private JPanel pnlTestHead;
	private JPanel pnlControl;
	private JLabel lblManual;
	private JToggleButton tglAuto;
	private JButton cmdResetValve;
	private JButton cmdCapture;
	private JButton cmdUndo;
	private JButton cmdSave;
	private JButton cmdOpen;
	private JButton cmdReports;
	private JTabbedPane tabTest;
	private JPanel pnlTest;
	private JLabel label124;
	private JLabel label122;
	private JLabel label123;
	private JLabel label121;
	private JLabel label136;
	private JLabel label125;
	private JLabel label133;
	private JLabel lblTestSHead;
	private JLabel lblTestSHeadU;
	private JLabel lblTestDHead;
	private JLabel lblTestDHeadU;
	private JLabel lblTestFlow;
	private JLabel lblFlowUnit;
	private JLabel lblTestV;
	private JLabel label137;
	private JLabel lblTestCur;
	private JLabel label138;
	private JLabel lblTestPow;
	private JLabel label139;
	private JLabel label132;
	private JLabel label1;
	private JLabel label144;
	private JLabel label141;
	private JLabel label142;
	private JLabel label143;
	private JLabel label140;
	private JScrollPane scrlTest;
	private JTable tblTestEntry;
	private JPanel pnlOptTest;
	private JLabel lblOpTestSNo;
	private JPanel panel2;
	private JLabel label51;
	private JTextField txtSPSec;
	private JLabel label52;
	private JTextField txtSPSuc;
	private JLabel label53;
	private JPanel pnlFoot;
	private JLabel lblStatus;
	private JLabel lblLogo;
	private JScrollPane scrollPane1;
	private JList lstError;
	private JTextField txtSignal;
	private JLabel lblDBRemote;
	// JFormDesigner - End of variables declaration  //GEN-END:variables

	// CUSTOM CODE - BEGIN
	private void customInit() {
		// db
		db = Database.getInstance();
		
		// do cosmetic stuffs
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		if (Configuration.IS_TRIAL_ON) {
			Configuration.APP_VERSION += "     [Trial Version]";
		}
		setTitle(Configuration.APP_AUTHOR + " " + Configuration.APP_VERSION  + "     [User: " + Configuration.USER + "]" + "     [Line: " + Configuration.LINE_NAME + "]" + "     [" + Configuration.LAST_USED_ISSTD + "]");
		
		// set last used values
		lblComp.setText(Configuration.LICENCEE_NAME);
		
		// set default text to error log
		errLogModel.addElement("Description is shown here while an error occurs; Double click to expand, Delete key to clear.");
		lstError.setSelectedIndex(0);
		
		lblDBRemote.setVisible(Configuration.IS_REMOTE_DB.equals("YES"));
				
		try {
			// create db tables if software run first time
			try {
				db.executeQuery("select * from " + Configuration.READING_DETAIL + "");
			} catch (SQLException se) {
				if (se.getMessage().contains("no such table")) {
					// pump master tables
					initPumpTables();
					
					// device and output tables
					initDevTables();
					
					// reading tables
					db.executeUpdate("create table " + Configuration.READING_DETAIL + " (test_type text, test_name_code text, pump_type_id integer, test_date date, pump_slno text, mot_slno text, test_slno integer, " +
							"speed integer, shead real, dhead real, flow real, " +
							"volt real, current real, power real, freq real, " +
							"remarks text, line text, user text, rated_volt text, station_no text, primary key (pump_type_id, pump_slno, rated_volt, test_name_code), foreign key(pump_type_id) references " + Configuration.PUMPTYPE + "(pump_type_id))");
				
					db.executeUpdate("create table " + Configuration.OPTIONAL_TEST + " (pump_type_id text, pump_slno text, rated_volt text, test_code text, val_1 text, val_2 text, val_3 text, val_4 text, val_5 text, " +
							"primary key (pump_type_id, pump_slno, rated_volt), foreign key(pump_type_id) references " + Configuration.PUMPTYPE + "(pump_type_id))");
					
					// report tables
					db.executeUpdate("CREATE TABLE " + Configuration.CUSTOM_REPORT + " (pump_type_id text, pump_slno text, cr_main text, cr_aux text, cr_tmp text, duty text, con text, suc_bed text, ir_val text, sp_time text, " +
							"tr_v text, tr_a text, tr_p text, tr_s text, tr_f text, tr_t text, tr_bt text, tr_rt text, " +
							"hr_main text, hr_aux text, hr_tmp text, wt_main text, wt_aux text, st_tmp text, " +
							"remark text, cap text, rated_volt text, " +
							"primary key (pump_type_id, pump_slno, rated_volt), foreign key(pump_type_id) references " + Configuration.PUMPTYPE + "(pump_type_id))");
							
				} else {
					throw se;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "DB Error:" + e.getMessage());
			return;
		}
		
		// ISI handling
		if (Configuration.LAST_USED_ISSTD.startsWith("IS 8034:") || Configuration.LAST_USED_ISSTD.startsWith("IS 14220:")) { // vacuum not req
			lbllblSHead.setVisible(false);
			lblLiveSHead.setVisible(false);
			pnlTop1.add(lbllblDHead, new TableLayoutConstraints(0, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			pnlTop1.add(lblLiveDHead, new TableLayoutConstraints(0, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			
			// test result table - remove vacuum from both header & table 
			TableLayout tblLy = (TableLayout) pnlTest.getLayout();
			tblLy.deleteColumn(8);
			tblTestEntry.removeColumn(tblTestEntry.getColumnModel().getColumn(9));
			vacColOffset = 0;
		} else if (Configuration.LAST_USED_ISSTD.startsWith("IS 12225")) {
			lblTestSHead.setText("E Head");
			lbllblSHead.setText("<html>E. Head<font size=-1> (mwc)</html>");
		}
		
		
		if(Configuration.LAST_USED_ISSTD.startsWith("IS 8472:") || Configuration.LAST_USED_ISSTD.startsWith("IS 12225")) { // ISI handling
			Configuration.IS_SP_TEST_ON = "1";
			lblMotIpL.setForeground(new Color(0, 153, 51));
			lblMotIp.setForeground(Color.BLUE);
		} else {
			Configuration.IS_SP_TEST_ON = "0";
		}
		
		if(Configuration.LAST_USED_ISSTD.startsWith("IS 12225")) {
			lblResF.setText("D");
			lblOverAllEffL.setEnabled(false);
			lblEff.setEnabled(false);
			lblEff.setText("-");
		}
		
		// pump performance tolerance
		// ISI handling
		if(Configuration.LAST_USED_ISSTD.startsWith("IS 8472:")) {
			Configuration.ISI_PERF_HEAD_LL = 90F;
			Configuration.ISI_PERF_DIS_LL = 90F;
			Configuration.ISI_PERF_CUR_UL = 107F;
			Configuration.ISI_PERF_POW_UL = 110F;
			Configuration.ISI_PERF_EFF_LL = 95.5F;
		} else { // 8034,14220,9079,6595 
			Configuration.ISI_PERF_HEAD_LL = 96F;
			Configuration.ISI_PERF_DIS_LL = 93F;
			Configuration.ISI_PERF_CUR_LL = 75F;
			Configuration.ISI_PERF_CUR_UL = 107F;
			Configuration.ISI_PERF_EFF_LL = 95.5F;
		}
				
		// load test mode
		loadTestMode();
		
		// load dev config
		checkDevSet(Configuration.LAST_USED_STATION);
		
		// load stations
		loadStations();
		setLRStationCaption();

		// show/hide add-on tests
		showHideOpTest();
		
		// others
		associateFunctionKeys();
		
		// load last used pump type
		if (Configuration.IS_ISSTD_CHANGED) {
			try {
				MyResultSet res = db.executeQuery("select pump_type_id from " + Configuration.READING_DETAIL + " order by rowid desc limit 1");
				if (res.isRowsAvail()) {
					Configuration.LAST_USED_PUMP_TYPE_ID = res.getString("pump_type_id");
				} else {
					Configuration.LAST_USED_PUMP_TYPE_ID = "1";
					Configuration.LAST_USED_PUMP_TYPE = "SAMPLE PUMP MODEL";
					Configuration.LAST_USED_PUMP_DESC = "";
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, "Error finding last used pump: " + e.getMessage());
			}
		}
		setPumpType(Configuration.LAST_USED_PUMP_TYPE_ID, false, true, "choose");
		
		// remove table header and set table properties
		tblTestEntry.setTableHeader(null);
		scrlTest.setViewportView(tblTestEntry);
		tblTestEntry.setDefaultRenderer(Object.class, new MyTableCellRender());
		tblTestEntry.getSelectionModel().addListSelectionListener(new SelectionListener(tblTestEntry));
		
		// disable save if user does not have modify access
		if (Configuration.USER_HAS_MODIFY_ACCESS.equals("0")) {
			cmdSave.setEnabled(false);
		}
		
		// clear test table with strings
		try {
			clearForm();
		} catch (Exception e) {
			e.printStackTrace();
			// ignore
		}
		// create a label belongs to result preview
		lblNA.setFont(new Font("Arial", Font.ITALIC, 10));
		lblNA.setHorizontalAlignment(SwingConstants.CENTER);
		
		// load last ten readings by default
		if (Configuration.NUMBER_OF_STATION > 1 && Configuration.MULTI_STATION_MODE.equals("YES")) {
			openFile("select * from (select * from " + Configuration.READING_DETAIL + " order by rowid desc limit 10) order by test_date,pump_slno,rated_volt,test_slno", false, true);
		} else {
			openFile("select * from (select a.*, b.type from " + Configuration.READING_DETAIL + " a join " + Configuration.PUMPTYPE + " b on b.pump_type_id=a.pump_type_id where a.line='" + Configuration.LINE_NAME + "' and a.pump_type_id='" + stationList.get(Configuration.LAST_USED_STATION).curPumpTypeId + "' order by a.rowid desc limit 10) order by test_date,pump_slno,rated_volt, test_slno", false, true);
		}
		
		checkReminders();
		
		// start threads
		startThreads();
		
	}
	
	// function to check reminders at start up (like calibration, backup, etc...)
	private void checkReminders() {
		//1. check for calibration reminders
			MyResultSet res = null;
			try {
				Calendar nextWeek = Calendar.getInstance();
				nextWeek.add(Calendar.DATE, +7);
				
				String curDt = dbDtFormat.format(nextWeek.getTime());
				res = db.executeQuery("select * from CALIBRATION where line='" + Configuration.LINE_NAME + "' and due_date != '' and due_date <= '" + curDt + "' and reminder=1");
				String calDue = "";
				String curLine = "";
				String formatStr = "%-11s%-40s%-11s%s";
				while (res.next()) {
					curLine = String.format(formatStr, res.getString("ins_id"), res.getString("ins_name"), reqDtFormat.format(dbDtFormat.parse(res.getString("due_date"))), res.getString("agency"));
					calDue += curLine + "\n";
				}
				if (!calDue.isEmpty()) {
					calDue = String.format(formatStr,"ID", "INSTRUMENT NAME", "DUE DATE", "AGENCY") + "\n" + String.format(formatStr, "==", "===============", "========", "======") + "\n" + calDue;
					JTextArea msgTxt = new JTextArea();
					msgTxt.setText("Calibration Reminder\n\n" + calDue);
					msgTxt.setEditable(false);
					msgTxt.setBackground(this.getBackground());
					JOptionPane.showMessageDialog(this, msgTxt);
					
					// set reminder icon
					cmdDeviceCfg.setIcon(new ImageIcon(getClass().getResource("/img/bell.PNG")));
				} else {
					cmdDeviceCfg.setIcon(null);
				}
			} catch (ParseException pe) {
				// ignore
			} catch (Exception se) {
				if (!se.getMessage().contains("no such table")) {
					JOptionPane.showMessageDialog(this, "Error checking for calibration reminders:" + se.getMessage());
				}
			}
			
			//2. check for automatic backup
			Calendar cal = Calendar.getInstance();
			try {
				cal.setTime(reqDtFormat.parse(reqDtFormat.format(cal.getTime())));
				if (!Configuration.NEXT_BACKUP_DATE.isEmpty()) {
					Calendar nextCal =Calendar.getInstance();
					nextCal.setTime(reqDtFormat.parse(Configuration.NEXT_BACKUP_DATE));
					if (cal.compareTo(nextCal) >= 0) {
						// backup required today
						int resp = JOptionPane.showConfirmDialog(this, "Now it is due for automatic backup, do you want to perform it now?\n'Yes' to do it now, 'No/Cancel' to do it later");
						if (resp != 0) {
							return;
						} else {
							changeApplicationStatus("BACKING UP THE DATA...");
							lblStatus.update(lblStatus.getGraphics());
							Configuration.backupData(Configuration.LAST_USED_BACKUP_LOCATION);
							Configuration.LAST_BACKUP_DATE = reqDtFormat.format(cal.getTime());
							changeApplicationStatus("AUTOMATIC BACKUP COMPLETED");
						}
					} else {
						return;
					}
				}
			} catch (Exception e) {
					logError(e.getMessage());
					changeApplicationStatus("AUTOMATIC BACKUP FAILED");
					return;
			}
			
			// set next back up date (when either last automatic back completed successfully or the software runs first time)
			cal.add(Calendar.DATE, Integer.valueOf(Configuration.LAST_USED_BACKUP_DURATION));
			Configuration.NEXT_BACKUP_DATE = reqDtFormat.format(cal.getTime());
			Configuration.saveConfigValues("NEXT_BACKUP_DATE", "LAST_BACKUP_DATE");
	}
	
	// function to log error
	private void logError(String msg) {
		logError(msg, false);
	}
	
	private void logError(String msg, boolean debug) {
		logTime = Calendar.getInstance().getTime();
		//debug = true;
		//Configuration.APP_DEBUG_MODE = true;
		if (debug) {
			if (Configuration.APP_DEBUG_MODE) {
				errLogModel.add(errLogModel.getSize()-1, reqDtFormat.format(logTime) + " " + reqTmFormat.format(logTime) + ":" + "DEBUG:" + msg);
			}
		} else {
			errLogModel.add(errLogModel.getSize()-1, reqDtFormat.format(logTime) + " " + reqTmFormat.format(logTime) + ":" + msg);
		}
		
		lstError.ensureIndexIsVisible(errLogModel.getSize()-1);
	}
	
	// function to change the status label which is displayed at the bottom
	private void changeApplicationStatus(String status) {
		lblStatus.setText(status);
		lblStatus.repaint();
	}
	
	// funtion to focefully request focus into signal text when needed
	private void focusSignalText() {
		if (isAutoCaptureOn && (Configuration.LAST_USED_SIGNAL_METHOD.equals("K") || Configuration.IS_BARCODE_USED.equals("YES"))) {
			txtSignal.requestFocusInWindow();
		}
	}
		
	private void clearForm() throws Exception{
		// clear the test table
		for (int r=0; r<tblTestEntry.getRowCount(); r++) {
			for (int c=0; c<tblTestEntry.getColumnCount(); c++) {
				tblTestEntry.setValueAt("", r, c);
			}
		}
		
		tblTestEntry.setRowSelectionInterval(0, 0);
		tblTestEntry.scrollRectToVisible(tblTestEntry.getCellRect(0, 0, true));
		
		// clear result preview
		refreshResultPreview(Configuration.LAST_USED_STATION, "", "", "");
	}
	
	private void associateFunctionKeys() {
		
		// associate F1 for help
		/* String HELP_ACTION_KEY = "helpAction";
		Action helpAction = new AbstractAction() {
		      public void actionPerformed(ActionEvent actionEvent) {
		        cmdHelpActionPerformed();
		      }
		    };
		KeyStroke f1 = KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0);
		InputMap helpInputMap = cmdHelp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		helpInputMap.put(f1, HELP_ACTION_KEY);
		ActionMap helpActionMap = cmdHelp.getActionMap();
		helpActionMap.put(HELP_ACTION_KEY, helpAction);
		cmdHelp.setActionMap(helpActionMap);*/
		
		// associate f4 for save
		String SAVE_ACTION_KEY = "saveAction";
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
		cmdSave.setActionMap(saveActionMap);
		
		// associate f5 for open
		String OPEN_ACTION_KEY = "openAction";
		Action openAction = new AbstractAction() {
		      public void actionPerformed(ActionEvent actionEvent) {
		        cmdOpenActionPerformed();
		      }
		    };
		KeyStroke f5 = KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0);
		InputMap openInputMap = cmdOpen.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		openInputMap.put(f5, OPEN_ACTION_KEY);
		ActionMap openActionMap = cmdOpen.getActionMap();
		openActionMap.put(OPEN_ACTION_KEY, openAction);
		cmdOpen.setActionMap(openActionMap);
		
		// associate f6 for print
		String PRINT_ACTION_KEY = "printAction";
		Action printAction = new AbstractAction() {
		      public void actionPerformed(ActionEvent actionEvent) {
		        cmdReportsActionPerformed();
		      }
		    };
		KeyStroke f6 = KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0);
		InputMap printInputMap = cmdReports.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		printInputMap.put(f6, PRINT_ACTION_KEY);
		ActionMap printActionMap = cmdReports.getActionMap();
		printActionMap.put(PRINT_ACTION_KEY, printAction);
		cmdReports.setActionMap(printActionMap);
		
		// associate f8 for capture
		String CAPTURE_ACTION_KEY = "captureAction";
		Action captureAction = new AbstractAction() {
		      public void actionPerformed(ActionEvent actionEvent) {
		        cmdCaptureActionPerformed();
		      }
		    };
		KeyStroke f8 = KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0);
		InputMap captureInputMap = cmdCapture.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		captureInputMap.put(f8, CAPTURE_ACTION_KEY);
		ActionMap captureActionMap = cmdCapture.getActionMap();
		captureActionMap.put(CAPTURE_ACTION_KEY, captureAction);
		cmdCapture.setActionMap(captureActionMap);
		
		// associate f9 for undo
		String UNDO_ACTION_KEY = "undoAction";
		Action undoAction = new AbstractAction() {
		      public void actionPerformed(ActionEvent actionEvent) {
		        cmdUndoActionPerformed();
		      }
		    };
		KeyStroke f9 = KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0);
		InputMap undoInputMap = cmdUndo.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		undoInputMap.put(f9, UNDO_ACTION_KEY);
		ActionMap undoActionMap = cmdUndo.getActionMap();
		undoActionMap.put(UNDO_ACTION_KEY, undoAction);
		cmdUndo.setActionMap(undoActionMap);
		
		// associate F10 for exit
		/* String CLOSE_ACTION_KEY = "closeAction";
		Action closeAction = new AbstractAction() {
		      public void actionPerformed(ActionEvent actionEvent) {
		        cmdExitActionPerformed();
		      }
		    };
		KeyStroke f10 = KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0);
		InputMap closeInputMap = cmdExit.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		closeInputMap.put(f10, CLOSE_ACTION_KEY);
		ActionMap closeActionMap = cmdExit.getActionMap();
		closeActionMap.put(CLOSE_ACTION_KEY, closeAction);
		cmdExit.setActionMap(closeActionMap); */
		
		// associate F11 for pump
		String PUMP_ACTION_KEY = "pumpAction";
		Action pumpAction = new AbstractAction() {
		      public void actionPerformed(ActionEvent actionEvent) {
		        cmdPumpActionPerformed();
		      }
		    };
		KeyStroke f11 = KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0);
		InputMap pumpInputMap = cmdPump.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		pumpInputMap.put(f11, PUMP_ACTION_KEY);
		ActionMap pumpActionMap = cmdPump.getActionMap();
		pumpActionMap.put(PUMP_ACTION_KEY, pumpAction);
		cmdPump.setActionMap(pumpActionMap);
		
		// associate F12 for auto capture
		String AUTO_ACTION_KEY = "autoAction";
		Action autoAction = new AbstractAction() {
		      public void actionPerformed(ActionEvent actionEvent) {
		    	tglAuto.setSelected(!tglAuto.isSelected());
		        tglAutoActionPerformed();
		      }
		    };
		KeyStroke f12 = KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0);
		InputMap autoInputMap = tglAuto.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		autoInputMap.put(f12, AUTO_ACTION_KEY);
		ActionMap autoActionMap = tglAuto.getActionMap();
		autoActionMap.put(AUTO_ACTION_KEY, autoAction);
		tglAuto.setActionMap(autoActionMap);
		
		
		// remove function key bindings from test entry table
		DefaultCellEditor editor = new DefaultCellEditor(new JTextField()) {
			@Override
		    public boolean isCellEditable(EventObject e) {
		        if (e instanceof KeyEvent) {
		            return startWithKeyEvent((KeyEvent) e);
		        }
		        return super.isCellEditable(e);
		    }
			private boolean startWithKeyEvent(KeyEvent e) {
		        // check modifiers as needed, this here is just a quick example
		        if (e.getKeyCode() >= KeyEvent.VK_F1 && e.getKeyCode() <= KeyEvent.VK_F24) {
		            return false;
		        }    
		        return true;
		    }
		};
		tblTestEntry.setDefaultEditor(Object.class, editor);
	}
	
	//function to show/hide addon tests
	public void showHideOpTest() {
		tabTest.setEnabledAt(1, Configuration.IS_SP_TEST_ON.equals("1"));
	}
	
	// function to load number of stations
	public void loadStations() {
		try {
			Configuration.NUMBER_OF_STATION = 0;
			Configuration.OP_PRESSURE_VAC_OUTPUT.clear();
			Configuration.OP_FLOW_OUTPUT.clear();
			Configuration.OP_PUMP_TYPE.clear();
			String tmpStation = "";
			DeviceModbusReader.clearList();
			DeviceSerialReader.clearList();
			
			// close connections with devices if open already
			for(int i=1; i<=stationList.size(); i++) {
				try {
					stationList.get("Station"+i).closeDevices();
					System.out.println("Closed devices of Station " + i);
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
			}
			
			// load stations one by one along with corresponding pump attributes and devices
			stationList.clear();
			stationLabelList.clear();
			statusLabelList.clear();
			
			TableLayout tmpLayout = (TableLayout) pnlStationList.getLayout();
			while(tmpLayout.getNumColumn()>0) {
				tmpLayout.deleteColumn(0);
			}
			
			MyResultSet res = db.executeQuery("select b.station_no as is_op_config, b.*, a.station_no as station_no, c.type from DEVICE a left join " + Configuration.OUTPUT + " b on b.station_no=a.station_no and b.line=a.line left join " + Configuration.PUMPTYPE + " c on c.pump_type_id=b.pump_type_id  where a.line='" + Configuration.LINE_NAME +"' group by a.station_no");
			devInitialized = true;
			Configuration.NUMBER_OF_STATION = res.getLength();
			Integer idx = 1;
			String defOp = "";
			
			while (res.next()) {
				tmpStation = res.getString("station_no");
				
				// construct a station
				Station objStation = new Station(tmpStation);
				
				if (!res.getString("is_op_config").isEmpty()) {
					// global variables for showing station status labels
					Configuration.OP_PRESSURE_VAC_OUTPUT.put(tmpStation, res.getString("pres_op"));
					Configuration.OP_FLOW_OUTPUT.put(tmpStation, res.getString("flow_op"));
					Configuration.OP_PUMP_TYPE.put(tmpStation, res.getString("type"));
					
					// 0. chosen pump and output of this station
					objStation.curPumpType = res.getString("type");
					objStation.curPumpTypeId = res.getString("pump_type_id");
					objStation.curPumpDesc = res.getString("desc");
					
					objStation.curPrOPSt = res.getString("pres_op").substring(0,res.getString("pres_op").length()-2);
					objStation.curFlowOPSt = res.getString("flow_op").substring(0,res.getString("flow_op").length()-2);
					objStation.curPrOP = res.getString("pres_op").substring(res.getString("pres_op").length()-1);
					objStation.curFlowOP = res.getString("flow_op").substring(res.getString("flow_op").length()-1);
				} else {
					// set up the station if software is running first time for this ISI
					defOp = tmpStation + ":A";
					db.executeUpdate("insert into " + Configuration.OUTPUT + "(line, station_no, pres_op, flow_op, pump_type_id) values ('" + Configuration.LINE_NAME + "', '" + tmpStation + "', '" + defOp + "', '" + defOp + "',1)");
					
					// default values 
					
					// global variables for showing station status labels
					Configuration.OP_PRESSURE_VAC_OUTPUT.put(tmpStation, defOp);
					Configuration.OP_FLOW_OUTPUT.put(tmpStation, defOp);
					Configuration.OP_PUMP_TYPE.put(tmpStation, "SAMPLE PUMP MODEL");
					
					// 0. chosen pump and output of this station
					objStation.curPumpType = "SAMPLE PUMP MODEL";
					objStation.curPumpTypeId = "1";
					objStation.curPumpDesc = "";
					
					objStation.curPrOPSt = defOp.substring(0,defOp.length()-2);
					objStation.curFlowOPSt = objStation.curPrOPSt;
					objStation.curPrOP = defOp.substring(defOp.length()-1);
					objStation.curFlowOP = objStation.curPrOP;
				}
				
				// 1. add status labels
				JLabel lblStation = new JLabel();
				lblStation.setText(idx.toString() + " [" + Configuration.OP_PUMP_TYPE.get("Station" + idx) + "]");
				lblStation.setHorizontalAlignment(SwingConstants.CENTER);
				lblStation.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 16));
				lblStation.setBackground(Color.gray);
				lblStation.setOpaque(true);
				stationLabelList.add(lblStation);
				
				JLabel lblStatus = new JLabel();
				lblStatus.setText("IDLE");
				lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
				lblStatus.setFont(new Font("Arial", Font.BOLD, 14));
				lblStatus.setBackground(Color.gray);
				lblStatus.setOpaque(true);
				statusLabelList.add(lblStatus);
				
				tmpLayout.insertColumn(idx-1, TableLayoutConstraints.FILL);
				pnlStationList.add(lblStation, new TableLayoutConstraints(idx-1, 0, idx-1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				pnlStationList.add(lblStatus, new TableLayoutConstraints(idx-1, 1, idx-1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				
				// 2.load recent test details of this station
				loadRecentTestDetails(objStation);
				
				// 3. initialize devices of this station
				if (Configuration.TEST_MODE.equals("A")) {
					try {
						objStation.refreshDevices();
						objStation.initDevices();
						// set suction lift to capture self priming time
						try {
							objStation.writeParamValue("Set Suction Lift", (int) (Configuration.CUR_SUC_LIFT * 10));
						} catch (Exception e) {
							System.out.println("Unable to set suction lift, ignoring it...");
							// ignore as legacy system does not have automated self priming time capture 
						}
					} catch (Exception e) {
						logError(e.getMessage());
						devInitialized = false;	
					}
				}
				stationList.put(tmpStation, objStation);
				++idx;
			}
		} catch (Exception se) {
			JOptionPane.showMessageDialog(this, "Error loading station:" + se.getMessage());
			se.printStackTrace();
		}
		
		if (!devInitialized) {
			changeApplicationStatus("DEVICE COMMUNICATION FAILED ");
		}
			
		// need to add only it was removed in this session, otherwise adding labels are just enough
		if (opWasMadeOne) {
			this.getContentPane().add(pnlStationList, new TableLayoutConstraints(0, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		}
		
		// update the routine limit labels of all stations
		// updateRoutineLimits();
					
		// live reading labels
		int r = 0;
		TableLayout tmpLayout = (TableLayout) pnlLROPList.getLayout();
		while(tmpLayout.getNumColumn()>0) {
			tmpLayout.deleteColumn(0);
		}
		while(tmpLayout.getNumRow()>1) {
			tmpLayout.deleteRow(0);
		}
		tglLRList.clear();
		for(Integer i=1; i<=Configuration.NUMBER_OF_STATION; i++) {
			final int idx = i-1;
			JToggleButton tglLR = new JToggleButton();
			tglLR.setText(i.toString());
			tglLR.setIconTextGap(0);
			tglLR.setMargin(new Insets(2, 2, 2, 2));
			tglLR.setFont(new Font("Arial", Font.BOLD, 16));
			tglLR.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					tglLRList.get(curLiveSt).setSelected(false);
					tglLRList.get(idx).setSelected(true);
					curLiveSt = idx;
					strLiveSt = "Station" + (idx+1);
					lblLR.setText("Device Live Reading (" + strLiveSt + ")");
					focusSignalText();
				}
			});
			tglLRList.add(tglLR);
			tmpLayout.insertColumn(i-1, TableLayoutConstraints.FILL);
			if (i > 10) {
				r = 1;
				tmpLayout.insertRow(1, TableLayoutConstraints.FILL);
			} else {
				r = 0;
			}
			pnlLROPList.add(tglLR, new TableLayoutConstraints(i-1, r, i-1, r, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		}
		if (opWasMadeOne) {
			pnlLive.add(pnlLROPList, new TableLayoutConstraints(0, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		}
		pnlLROPList.updateUI();
	}
		
	// function to set live reading station button caption
	public void setLRStationCaption() {
		try {
			cmdOutput.setText("<html><body align='center'>" + Configuration.LAST_USED_STATION + "<br><font size=-1>[PR " + Configuration.OP_PRESSURE_VAC_OUTPUT.get(Configuration.LAST_USED_STATION).substring(7) + "&nbsp;FL " + Configuration.OP_FLOW_OUTPUT.get(Configuration.LAST_USED_STATION).substring(7) + "]</html>");
			lblLR.setText("Device Live Reading (" + Configuration.LAST_USED_STATION + ")");
			tglLRList.get(curLiveSt).setSelected(false);
			curLiveSt = Integer.valueOf(Configuration.LAST_USED_STATION.substring(7))-1;
			strLiveSt = "Station" + (curLiveSt+1);
			tglLRList.get(curLiveSt).setSelected(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// function to load test mode
	public void loadTestMode() {
		if(Configuration.TEST_MODE.equals("M")) {
			tglAuto.setVisible(false);
			lblManual.setVisible(true);
			tglAuto.setEnabled(false);
			// tglAuto.setText("<html><body align='center'>Auto Capture<br><font color='gray'>DISABLED</font><br><font size=-2>[F12]</html>");
			cmdCapture.setText("<html><body align=\"center\">New <font size=-2>[F8]</html>");
			cmdCapture.setToolTipText("Click on this to start typing new reading entry");
		} else if (!isAutoCaptureOn){
			lblManual.setVisible(false);
			tglAuto.setVisible(true);
			tglAuto.setEnabled(true);
			// tglAuto.setText("<html><body align='center'>Auto Capture<br><font color='red'>OFF</font><br><font size=-2>[F12]</html>");
			cmdCapture.setText("<html><body align=\"center\">Capture <font size=-2>[F8]</html>");
			cmdCapture.setToolTipText("Click on this to recapture and overwrite chosen record with current device readings");
		}
	}

	// function to load test types in the order configured
	public void loadTestTypes() {
		
	}
	
	// function to load recent test details of available stations
	public void loadRecentTestDetails(Station objStation) throws SQLException {
		MyResultSet res2 = null;
		MyResultSet res3 = null;
		
		res2 = db.executeQuery("select a.*, b.* from " + Configuration.PUMPTYPE + " a join " + Configuration.OUTPUT + " b on b.pump_type_id=a.pump_type_id where a.pump_type_id='" + objStation.curPumpTypeId + "' and b.station_no='" + objStation.stationName + "' and b.line='" + Configuration.LINE_NAME + "'");
		if (res2.next()) {
			objStation.curPumpPhase = res2.getString("phase");
			objStation.lastUsedPumpSNo = res2.getString("recent_pump_sno");
			objStation.lastUsedMotSNo = res2.getString("recent_motor_sno");
			objStation.nextTestType = res2.getString("recent_test_type");
			objStation.nextTestCode = res2.getString("recent_test_code");
			if (!objStation.nextTestType.isEmpty()) {
				objStation.lastUsedTestSNo = objStation.nextTestType.equals("T") ? objStation.testListType.indexOf(objStation.nextTestCode) : objStation.testListRoutine.indexOf(objStation.nextTestCode);
				objStation.lastUsedTestSNo += 1;
			}
			
			objStation.curHdUnit = res2.getString("head_unit");
			objStation.curPumpFlowUnit = res2.getString("discharge_unit");
			objStation.curTypeTestFreq = res2.getInt("type_test_freq");
			objStation.curAutoValveType = res2.getString("auto_valve_type");
			
			// multi voltage handling
			objStation.voltList.clear();
			if (!res2.getString("other_volts").isEmpty() && res2.getString("other_volts_disabled").equals("N")) { 
				for(String tmpV : res2.getString("other_volts").split(",")) {
					objStation.voltList.add(tmpV);
				}
			}
			if (objStation.voltList.indexOf(res2.getString("volts")) < 0) {
				objStation.voltList.add(res2.getString("volts"));
			}
			
			// get ready with recently tested rated volt
			res3 = db.executeQuery("select rated_volt from " + Configuration.READING_DETAIL + " where pump_type_id='"+ objStation.curPumpTypeId +"' and pump_slno='" + objStation.lastUsedPumpSNo + "' and mot_slno='" + objStation.lastUsedMotSNo + "' order by rowid desc");
			if (res3.next()) {
				objStation.nextRatedVolt = res3.getString("rated_volt");
				objStation.noRatedVolt = objStation.voltList.indexOf(objStation.nextRatedVolt);
				objStation.noRatedVolt = objStation.noRatedVolt == -1 ? 0 : objStation.noRatedVolt;
			} else { // it seems no tests performed yet for this pump type, so start with first
				/*objStation.nextRatedVolt = "";
				objStation.noRatedVolt = -1;*/
				objStation.noRatedVolt = 0;
				objStation.nextRatedVolt = objStation.voltList.get(objStation.noRatedVolt);
			}
			
			// set declared head in HMI if its remote station mode
			if (objStation.isParamExist("Set Declared Head")) {
				try {
					objStation.writeParamValue("Set Declared Head", (int) (res2.getFloat("head") * 10));
				} catch (Exception e) {
					// ignore
				}
			} else if (Configuration.MULTI_STATION_MODE.equals("YES")) {
				System.out.println("Warning: Remote station mode is on - Unable to set test details in HMI. Ignoring it...");
			}
			
			// load head settings 
			loadHeadDetails(objStation);
		}
	}
	
	// function to init pump master tables
	private void initPumpTables() throws Exception {
		String curType = "SAMPLE PUMP MODEL";
		String curDesc = "";
		String curDelSize = "25";
		String curSucSize = (!Configuration.LAST_USED_ISSTD.startsWith("IS 8034:") && !Configuration.LAST_USED_ISSTD.startsWith("IS 14220:")) ? "25" : "";
		String curBrSize = (Configuration.LAST_USED_ISSTD.startsWith("IS 8034:") || Configuration.LAST_USED_ISSTD.startsWith("IS 14220:")) ? "80" : "";
		String curNoOfStage = (Configuration.LAST_USED_ISSTD.startsWith("IS 8034:") || Configuration.LAST_USED_ISSTD.startsWith("IS 14220:")) ? "4" : "";
		
		String curMotEff = (Configuration.LAST_USED_ISSTD.startsWith("IS 8034:") || Configuration.LAST_USED_ISSTD.startsWith("IS 14220:") || Configuration.LAST_USED_ISSTD.startsWith("IS 6595:")) ? "65" : "";
		String curHead = "15.00";
		String curPumpIp = "0.800";
		String curDisUnit = "lps";
		String curDis = "0.22";
		String curDisL = "";
		String curDisH = "";
		String curEff = "37";
		String curHeadL = "11.00";
		String curHeadH = "19.00";
		String curGauge = "0.00";
		
		String curMotType = "SAMPLE MOTOR MODEL";
		String curKW = "0.370";
		String curHP = "0.5";
		String curVolts = "240";
		String curOtherVolts = "";
		String curOtherVoltsDis = "N";
		String curPhase = "Single";
		String curAmps = "2.700";
		String curSpeed = "2880";
		String curFreq = "50";
		
		String curRecentPumpSno = "";
		String curRecentMotSno = "";
		
		String curNonISIModel = "N";
		String curCt = "";
		String curIns = "B";
		String curSP = Configuration.LAST_USED_ISSTD.startsWith("IS 8472") ? "180" : "";
		String curSL = Configuration.LAST_USED_ISSTD.startsWith("IS 8472") ? "4" : "";;
		
		String curPresSz = "";
		String curDLWL = "";
		String curSub = "";
		String curOpPres = "";
		
		String curPole = "4";
		String curCapRate = "16";
		String curCapVolt = "450";
		
		String curTypeTestFreq = "20";
		
		// 1. create pump type table and insert its default row
		db.executeUpdate("create table " + Configuration.PUMPTYPE + " (pump_type_id integer primary key autoincrement, type text, desc text, delivery_size text, suction_size text, mot_eff text, head text, mot_ip text, " + 
				"discharge_unit text, discharge text, discharge_low text, discharge_high text, overall_eff text, head_low text, head_high text, gauge_distance text, " +
				"mot_type text, kw text, hp text, " +
				"volts text, phase text, amps text, speed text, freq text, " +
				"conn text, head_unit text, bore_size text, no_of_stage text, " +
				"recent_pump_sno text, recent_motor_sno text, " +
				"non_isi_model text, category text, ins_class text, other_volts text, other_volts_disabled text, self_priming_time text, suction_lift text, "+
				"pres_size text, dlwl text, submergence text, min_op_pres text, no_of_poles text, cap_rating text, cap_volt text, type_test_freq text, auto_valve_type text default 'P')");

		db.executeUpdate("insert into " + Configuration.PUMPTYPE + "(type, desc, delivery_size , suction_size , mot_eff , head , mot_ip , " + 
				"discharge_unit , discharge , discharge_low , discharge_high , overall_eff , head_low , head_high , gauge_distance , " +
				"mot_type , kw , hp , " +
				"volts , phase , amps , speed , freq , " +
				"conn , head_unit , bore_size , no_of_stage , " +
				"recent_pump_sno , recent_motor_sno , " +
				"non_isi_model , category , ins_class , other_volts , other_volts_disabled , self_priming_time , suction_lift , "+
				"pres_size , dlwl , submergence , min_op_pres, no_of_poles, cap_rating, cap_volt, type_test_freq) values ('" + curType + "','" + curDesc + "','" + curDelSize + "','" + curSucSize + "','" + curMotEff + "','" + 
				curHead + "','" + curPumpIp + "','" + curDisUnit + "','" + curDis + "','" + curDisL + "','" + curDisH + "','" + 
				curEff + "','" + curHeadL + "','" + curHeadH + "','" + curGauge + "','" + 
				curMotType + "','" + curKW + "','" + curHP + "','" + 
				curVolts + "','" + curPhase + "','" + curAmps + "','" + curSpeed + "','" + curFreq + "','" +
				"CSR','m','" + curBrSize + "','" + curNoOfStage + "'," + 				
				"'" + curRecentPumpSno + "','" + curRecentMotSno + "','" + 
				curNonISIModel + "','" + curCt + "', '" + curIns + "', '" + 
				curOtherVolts + "', '" + curOtherVoltsDis +"', '" + curSP +"', '" + curSL + "', '" +
				curPresSz + "', '" + curDLWL + "', '" + curSub + "', '" + curOpPres + "', '" + curPole + "', '" + curCapRate + "', '" + curCapVolt + "', '" + curTypeTestFreq + "')");
	
		// 2. test head settings
		db.executeUpdate("create table " + Configuration.TESTNAMES_ALL + " (pump_type_id integer, code text, name text, head real, seq number, primary key (pump_type_id, code) , foreign key(pump_type_id) references " + Configuration.PUMPTYPE + "(pump_type_id))");
		db.executeUpdate("create table " + Configuration.TESTNAMES_ROUTINE + " (pump_type_id integer, code text, seq number, auto_valve_value real, auto_valve_tol real, auto_valve_learnt_pos real, primary key (pump_type_id, code), foreign key(pump_type_id) references " + Configuration.PUMPTYPE + "(pump_type_id))");
		db.executeUpdate("create table " + Configuration.TESTNAMES_TYPE + " (pump_type_id integer, code text, seq number, auto_valve_value real, auto_valve_tol real, auto_valve_learnt_pos real, primary key (pump_type_id, code), foreign key(pump_type_id) references " + Configuration.PUMPTYPE + "(pump_type_id))");
		
		LinkedHashMap<String, String> testListMap = new LinkedHashMap<String, String>();
		testListMap.put("FO", "FULL OPEN TEST");
		testListMap.put("H1", "HEAD 1");
		testListMap.put("H2", "HEAD 2");
		testListMap.put("H3", "HEAD 3");
		testListMap.put("H4", "HEAD 4");
		testListMap.put("H5", "HEAD 5");
		testListMap.put("SO", "SHUT OFF PRESSURE TEST");
		//testListMap.put("SS", "SHUT OFF SUCTION TEST");
		
		int i = 0;
		for(String key : testListMap.keySet()) {
			db.executeUpdate("insert into " + Configuration.TESTNAMES_ALL + " values ('1','" + key + "','" + testListMap.get(key) + "',''," + i + ")");
			
			if (i==7) { // skip last one as its not used by default in both routine & type
				continue;
			}
			if (i == 0 || i == 3 || i == 6) { // fo, duty point, so test go to routine by default
				db.executeUpdate("insert into " + Configuration.TESTNAMES_ROUTINE + " values ('1','" + key + "'," + i + ",'','','')");
			}
			db.executeUpdate("insert into " + Configuration.TESTNAMES_TYPE + " values ('1','" + key + "'," + i + ",'','','')");
			++i;
		}
		
		// 3. routine test limits
		db.executeUpdate("create table " + Configuration.ROUTINE_LIMITS + "(pump_type_id integer, code text, head_ll real, head_ul real, discharge_ll real, discharge_ul real, current_ll real, current_ul real, power_ll real, power_ul real, primary key (pump_type_id, code), foreign key(pump_type_id) references " + Configuration.PUMPTYPE + "(pump_type_id))");
		db.executeUpdate("insert into " + Configuration.ROUTINE_LIMITS + "(pump_type_id, code) values (1, 'FO')"); 
		db.executeUpdate("insert into " + Configuration.ROUTINE_LIMITS + "(pump_type_id, code) values (1, 'DP')"); 
		db.executeUpdate("insert into " + Configuration.ROUTINE_LIMITS + "(pump_type_id, code) values (1, 'SO')"); 
		
		// 4. motor eff data points
		db.executeUpdate("create table " + Configuration.MOT_EFF_DATA_POINTS + "(pump_type_id integer, ip_kw real, op_kw real, eff real, foreign key(pump_type_id) references " + Configuration.PUMPTYPE + "(pump_type_id))");
		
		// 5. non isi perf tolerance
		db.executeUpdate("create table " + Configuration.NON_ISI_PERF_TOLERANCE + "(pump_type_id integer, head_ll real, head_ul real, discharge_ll real, discharge_ul real, current_ll real, current_ul real, power_ll real, power_ul real, eff_ll real, eff_ul real, primary key (pump_type_id), foreign key(pump_type_id) references " + Configuration.PUMPTYPE + "(pump_type_id))");
		
		// 5. for storing stations and corresponding outputs and pump model 
		db.executeUpdate("create table IF NOT EXISTS " + Configuration.OUTPUT + "(line text, station_no text, pres_op text, flow_op text, pump_type_id integer, recent_pump_sno text, recent_motor_sno text, recent_test_type text, recent_test_code text, foreign key(pump_type_id) references " + Configuration.PUMPTYPE + "(pump_type_id))");
	}
	
	// function to set the chosen pump from pump code
	public Boolean detectPumpType(String pumpCode, Station curStation) {
		System.out.println("setting pump code " + pumpCode + ", will overwrite station " + curStation.stationName);
		try {
			MyResultSet res = db.executeQuery("select * from " + Configuration.PUMPTYPE + " where type='" + pumpCode + "'");

			if (res.next()) {
				String curPumpTypeId = res.getString("pump_type_id");
				String curPumpType = res.getString("type");
				String curPumpDesc = res.getString("desc");
				String curDisLow = res.getString("discharge_low");
				String curDisHigh = res.getString("discharge_high");
				
				if (res.getString("non_isi_model").equals("Y")) {
					lblPumpType.setText("<html>" + res.getString("type") + " <font size=-2 color=red>(Non-ISI Model)</font></html>");
				} else {
					lblPumpType.setText(res.getString("type"));
				}
				// ISI handling
				if (Configuration.LAST_USED_ISSTD.startsWith("IS 12225")) {
					lblPipeSize.setText(res.getString("suction_size") + " X " + res.getString("pres_size") + " X " + res.getString("delivery_size"));
				} else if (!(Configuration.LAST_USED_ISSTD.startsWith("IS 8034:") || Configuration.LAST_USED_ISSTD.startsWith("IS 14220:"))) {
					lblPipeSize.setText(res.getString("delivery_size") + " X " + res.getString("suction_size"));
				} else {
					lblPipeSize.setText(res.getString("delivery_size"));
				}
				if (Configuration.LAST_USED_ISSTD.startsWith("IS 8034:") || Configuration.LAST_USED_ISSTD.startsWith("IS 14220:") || Configuration.LAST_USED_ISSTD.startsWith("IS 6595:") || Configuration.REP_SHOW_PUMP_EFF.equals("1")) {
					lblMotEff.setText(res.getString("mot_eff"));
				} else {
					lblMotEff.setText("NA");
				}
				lblHdUnit.setText("Head (" + res.getString("head_unit") + ")");
				if (Configuration.LAST_USED_ISSTD.startsWith("IS 12225")) {
					lblHdRgUnit.setText("DLWL Range (" + res.getString("head_unit") + ")");
					lbllblSHead.setText("<html>E. Head<font size=-1> (" + res.getString("head_unit") + ")</html>");
				} else {
					lblHdRgUnit.setText("Head Range (" + res.getString("head_unit") + ")");
					lbllblSHead.setText("<html>Suc. Head<font size=-1> (" + res.getString("head_unit") + ")</html>");
				}
				lbllblDHead.setText("<html>Del. Head<font size=-1> (" + res.getString("head_unit") + ")</html>");
				lblTestSHeadU.setText("(" + res.getString("head_unit") + ")");
				lblTestDHeadU.setText("(" + res.getString("head_unit") + ")");
				
				lblHead.setText(res.getString("head"));
				lblMotIp.setText(res.getString("mot_ip"));
				lblDisUnit.setText("<html>Discharge (" + res.getString("discharge_unit") + ")</html>");
				lblFlowUnit.setText("<html>(" + res.getString("discharge_unit") + ")</html>");
				lblFlowLive.setText("<html>Flow<font size=-1> (" + res.getString("discharge_unit") + ")</html>");
				
				lblDis.setText(res.getString("discharge"));
				lblEff.setText(res.getString("overall_eff"));
				lblHeadRange.setText(res.getString("head_low") + " / " + res.getString("head_high"));
				lblGauge.setText(res.getString("gauge_distance"));
				
				lblMotType.setText(res.getString("mot_type"));
				lblMotRt.setText(res.getString("kw") + " / " + res.getString("hp"));
				lblVolts.setText(res.getString("volts") + (res.getString("other_volts").isEmpty() || res.getString("other_volts_disabled").equals("Y") ?"":" [" + res.getString("other_volts") +"]"));
				
				lblPhase.setText(res.getString("phase"));
				lblAmps.setText(res.getString("amps"));
				
				curStation.curPumpTypeId = res.getString("pump_type_id");
				curStation.curPumpType = res.getString("type");
				curStation.curPumpDesc = res.getString("desc");
				curStation.curPumpPhase = res.getString("phase");
				Configuration.OP_PUMP_TYPE.put(curStation.stationName, res.getString("type"));
				
				// update the pump type label
				stationLabelList.get(curStation.stationIdx).setText((curStation.stationIdx + 1) + " [" + curStation.curPumpType + "]");
				
				// update current station with this model
				try {
					db.executeUpdate("update " + Configuration.OUTPUT + " set pump_type_id='" + res.getString("pump_type_id") + "', recent_pump_sno='', recent_motor_sno='', recent_test_type='', recent_test_code='' where station_no='" + curStation.stationName + "' and line='" + Configuration.LINE_NAME + "'");
				} catch (SQLException se) {
					JOptionPane.showMessageDialog(this, "Error updating model of this station:" + se.getMessage());
					return false;
				}
				
				// load recent test details
				loadRecentTestDetails(curStation);
				
				// update the routine limit labels if required
				// updateRoutineLimits();
				
				// set suction lift in case of any change
				if(Configuration.LAST_USED_ISSTD.startsWith("IS 8472:")) {
					Configuration.CUR_SUC_LIFT = res.getFloat("suction_lift");
					if (txtSPSec.getText().isEmpty()) {
						txtSPSuc.setText(Configuration.CUR_SUC_LIFT.toString());
					}
					
					// set suction lift to capture self priming time
					try {
						curStation.writeParamValue("Set Suction Lift", (int) (Configuration.CUR_SUC_LIFT * 10));
					} catch (Exception e) {
						System.out.println("Unable to set suction lift, ignoring it...");
						// ignore as legacy system does not have automated self priming time capture 
					}
				}
				
				// result preview (default or for given low & high head range)
				if ((curDisLow.isEmpty() || curDisLow.equals("0")) && (curDisHigh.isEmpty() || curDisHigh.equals("0"))) {
					isFlowRange = false;
				} else {
					isFlowRange = true;
				}
				
				Configuration.LAST_USED_PUMP_TYPE_ID = curPumpTypeId;
				Configuration.LAST_USED_PUMP_TYPE = curPumpType;
				Configuration.LAST_USED_PUMP_DESC = curPumpDesc;
				Configuration.saveConfigValues("LAST_USED_PUMP_TYPE_ID", "LAST_USED_PUMP_TYPE", "LAST_USED_PUMP_DESC");
			} else {
				JOptionPane.showMessageDialog(this, "Unable find the pump model using the scanned code: " + pumpCode);
				return false;
			}
			
		} catch (Exception se) {
			JOptionPane.showMessageDialog(this, "Error while setting pump type:" + se.getMessage());
			se.printStackTrace();
		}
		return true;
	}

	
	// function to set the chosen pump type & its feature
	public void setPumpType(String pumpTypeId, boolean clearForm, boolean isAppInit, String action) {
		// fetch the record from table and set the params
		try {
			MyResultSet res = null;
			
			res = db.executeQuery("select * from " + Configuration.PUMPTYPE + " where pump_type_id='" + pumpTypeId + "'");
			
			if (!res.isRowsAvail()) {
				// it seems the last used model is not available for some reason, hence, select first type as default
				res = db.executeQuery("select * from " + Configuration.PUMPTYPE + " order by pump_type_id limit 1");
			}

			if (res.next()) {
				String curPumpTypeId = res.getString("pump_type_id");
				String prevPumpTypeId = Configuration.LAST_USED_PUMP_TYPE_ID;
				Boolean isNonISIModel = res.getString("non_isi_model").equals("Y");
				if (isAppInit || action.equals("choose") || (curPumpTypeId.equals(prevPumpTypeId) && action.equals("update"))) {
					// update name plate details being displayed as chosen pump
					String curPumpType = res.getString("type");
					String curPumpDesc = res.getString("desc");
					String curDisLow = res.getString("discharge_low");
					String curDisHigh = res.getString("discharge_high");
					
					if (isNonISIModel) {
						lblPumpType.setText("<html>" + res.getString("type") + " <font size=-2 color=red>(Non-ISI Model)</font></html>");
					} else {
						lblPumpType.setText(res.getString("type"));
					}
					// ISI handling
					if (Configuration.LAST_USED_ISSTD.startsWith("IS 12225")) {
						lblPipeSize.setText(res.getString("suction_size") + " X " + res.getString("pres_size") + " X " + res.getString("delivery_size"));
					} else if (!(Configuration.LAST_USED_ISSTD.startsWith("IS 8034:") || Configuration.LAST_USED_ISSTD.startsWith("IS 14220:"))) {
						lblPipeSize.setText(res.getString("delivery_size") + " X " + res.getString("suction_size"));
					} else {
						lblPipeSize.setText(res.getString("delivery_size"));
					}
					if (Configuration.LAST_USED_ISSTD.startsWith("IS 8034:") || Configuration.LAST_USED_ISSTD.startsWith("IS 14220:") || Configuration.LAST_USED_ISSTD.startsWith("IS 6595:") || Configuration.REP_SHOW_PUMP_EFF.equals("1")) {
						lblMotEff.setText(res.getString("mot_eff"));
					} else {
						lblMotEff.setText("NA");
					}
					lblHdUnit.setText("Head (" + res.getString("head_unit") + ")");
					if (Configuration.LAST_USED_ISSTD.startsWith("IS 12225")) {
						lblHdRgUnit.setText("DLWL Range (" + res.getString("head_unit") + ")");
						lbllblSHead.setText("<html>E. Head<font size=-1> (" + res.getString("head_unit") + ")</html>");
					} else {
						lblHdRgUnit.setText("Head Range (" + res.getString("head_unit") + ")");
						lbllblSHead.setText("<html>Suc. Head<font size=-1> (" + res.getString("head_unit") + ")</html>");
					}
					lbllblDHead.setText("<html>Del. Head<font size=-1> (" + res.getString("head_unit") + ")</html>");
					lblTestSHeadU.setText("(" + res.getString("head_unit") + ")");
					lblTestDHeadU.setText("(" + res.getString("head_unit") + ")");
					
					lblHead.setText(res.getString("head"));
					lblMotIp.setText(res.getString("mot_ip"));
					lblDisUnit.setText("<html>Discharge (" + res.getString("discharge_unit") + ")</html>");
					lblFlowUnit.setText("<html>(" + res.getString("discharge_unit") + ")</html>");
					lblFlowLive.setText("<html>Flow<font size=-1> (" + res.getString("discharge_unit") + ")</html>");
					
					lblDis.setText(res.getString("discharge"));
					lblEff.setText(res.getString("overall_eff"));
					lblHeadRange.setText(res.getString("head_low") + " / " + res.getString("head_high"));
					lblGauge.setText(res.getString("gauge_distance"));
					
					lblMotType.setText(res.getString("mot_type"));
					lblMotRt.setText(res.getString("kw") + " / " + res.getString("hp"));
					lblVolts.setText(res.getString("volts") + (res.getString("other_volts").isEmpty() || res.getString("other_volts_disabled").equals("Y") ?"":" [" + res.getString("other_volts") +"]"));
					
					lblPhase.setText(res.getString("phase"));
					lblAmps.setText(res.getString("amps"));
					
					// result preview (default or for given low & high head range)
					if ((curDisLow.isEmpty() || curDisLow.equals("0")) && (curDisHigh.isEmpty() || curDisHigh.equals("0"))) {
						isFlowRange = false;
					} else {
						isFlowRange = true;
					}
					
					// global variables
					Configuration.LAST_USED_PUMP_TYPE_ID = curPumpTypeId;
					Configuration.LAST_USED_PUMP_TYPE = curPumpType;
					Configuration.LAST_USED_PUMP_DESC = curPumpDesc;
					Configuration.saveConfigValues("LAST_USED_PUMP_TYPE_ID", "LAST_USED_PUMP_TYPE", "LAST_USED_PUMP_DESC");
				
					// load performance tolerance
					Boolean isToUseISI = true;
					if (isNonISIModel) {
						try {
							MyResultSet resTol = db.executeQuery("select * from " + Configuration.NON_ISI_PERF_TOLERANCE + " where pump_type_id='" + curPumpTypeId + "'");
							if (resTol.next()) {
								isToUseISI = false;
								tolHeadMin = resTol.getFloat("head_ll");
								tolHeadMax = resTol.getFloat("head_ul") > 0 ? resTol.getFloat("head_ul") : 99999F;
								tolDisMin = resTol.getFloat("discharge_ll");
								tolDisMax = resTol.getFloat("discharge_ul") > 0 ? resTol.getFloat("discharge_ul") : 99999F;
								tolCurMin = resTol.getFloat("current_ll");
								tolCurMax = resTol.getFloat("current_ul") > 0 ? resTol.getFloat("current_ul") : 99999F;
								tolPowMin = resTol.getFloat("power_ll");
								tolPowMax = resTol.getFloat("power_ul") > 0 ? resTol.getFloat("power_ul") : 99999F;
								tolEffMin = resTol.getFloat("eff_ll");
								tolEffMax = resTol.getFloat("eff_ul") > 0 ? resTol.getFloat("eff_ul") : 99999F;
							}
						} catch (Exception sqle) {
							JOptionPane.showMessageDialog(new JDialog(), "Error loading tolerance:" + sqle.getMessage());
							return;
						}
					}
					if (isToUseISI) {
						tolHeadMin = Configuration.ISI_PERF_HEAD_LL;
						tolHeadMax = Configuration.ISI_PERF_HEAD_UL;
						tolDisMin = Configuration.ISI_PERF_DIS_LL;
						tolDisMax = Configuration.ISI_PERF_DIS_UL;
						tolCurMin = Configuration.ISI_PERF_CUR_LL;
						tolCurMax = Configuration.ISI_PERF_CUR_UL;
						tolPowMin = Configuration.ISI_PERF_POW_LL;
						tolPowMax = Configuration.ISI_PERF_POW_UL;
						tolEffMin = Configuration.ISI_PERF_EFF_LL;
						tolEffMax = Configuration.ISI_PERF_EFF_UL;
					}
					tolHeadMin /= 100;
					tolHeadMax /= 100;
					tolDisMin /= 100;
					tolDisMax /= 100;
					tolCurMin /= 100;
					tolCurMax /= 100;
					tolPowMin /= 100;
					tolPowMax /= 100;
					tolEffMin /= 100;
					tolEffMax /= 100;
				}
				
				// update all outputs using this model
				if (!isAppInit) {
					// 1.db update
					if (action.equals("choose")) {
						try {
							db.executeUpdate("update " + Configuration.OUTPUT + " set pump_type_id='" + res.getString("pump_type_id") + "', recent_pump_sno='', recent_motor_sno='', recent_test_type='', recent_test_code='' where pump_type_id='" + prevPumpTypeId + "' and line='" + Configuration.LINE_NAME + "'");
						} catch (SQLException se) {
							JOptionPane.showMessageDialog(this, "Error choosing the pump:" + se.getMessage());
						}
					}
					
					// 2. memory update
					Station curStation = null;
					Boolean isStationUpdated = false;
					for(int i=1; i<=stationList.size(); i++) {
						curStation = stationList.get("Station"+i);
						if (!curStation.curPumpTypeId.equals(curPumpTypeId) || (curStation.curPumpTypeId.equals(prevPumpTypeId) && action.equals("choose")) || (curStation.curPumpTypeId.equals(pumpTypeId) && action.equals("update"))) {
							isStationUpdated = true;
							
							curStation.curPumpTypeId = res.getString("pump_type_id");
							curStation.curPumpType = res.getString("type");
							curStation.curPumpDesc = res.getString("desc");
							curStation.curPumpPhase = res.getString("phase");
							curStation.curOtherVoltsDis = res.getString("other_volts_disabled");
							Configuration.OP_PUMP_TYPE.put(curStation.stationName, res.getString("type"));
							
							// write parameter value to the VFD Drive
							if(Configuration.IS_VFD) {
								
								try {
									curStation.writeParamValue("VDF_Voltage", (Integer.parseInt(lblVolts.getText()))*10);
									curStation.writeParamValue("VDF_Frequency", (Integer.parseInt(res.getString("freq")))*100);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							
							// update the pump type label
							stationLabelList.get(i-1).setText(i + " [" + curStation.curPumpType + "]");
							
							// refresh recent test details
							loadRecentTestDetails(curStation);
							
							// set suction lift in case of any change
							if(Configuration.LAST_USED_ISSTD.startsWith("IS 8472:")) {
								Configuration.CUR_SUC_LIFT = res.getFloat("suction_lift");
								if (txtSPSec.getText().isEmpty()) {
									txtSPSuc.setText(Configuration.CUR_SUC_LIFT.toString());
								}
								
								// set suction lift to capture self priming time
								try {
									curStation.writeParamValue("Set Suction Lift", (int) (Configuration.CUR_SUC_LIFT * 10));
								} catch (Exception e) {
									System.out.println("Unable to set suction lift, ignoring it...");
									// ignore as legacy system does not have automated self priming time capture 
								}
							}
						}
					}
					
					// update the stations which is using this model
					if (isStationUpdated) {
						// restart auto capture to ensure fresh tests
						if (Configuration.TEST_MODE.equals("A")) {
							stopAutoCapture();
							startAutoCapture();
						}
					}
				}
			}
			
			if (clearForm) {
				clearForm();
			}
		} catch (Exception se) {
			JOptionPane.showMessageDialog(this, "Error while setting pump type:" + se.getMessage());
			se.printStackTrace();
			return;
		}
	}
	
	private void updateRoutineLimits() {
		// hide routine test limit rows if routine test is not performed in any of stations
		TableLayout tmpLayout = (TableLayout) pnlStationList.getLayout();
		Boolean showIt = false;
		Station curStation = null;
		for(int i=0; i<stationList.size(); i++) {
			curStation = stationList.get("Station" + (i+1));
			if (curStation.curTypeTestFreq != 1 && curStation.testListRoutine.size() > 0) {
				showIt = true;
				break;
			}
		}
		if (tmpLayout.getNumRow() > 2) {
			tmpLayout.deleteRow(2);
		}
		
		if (showIt) {
			tmpLayout.insertRow(2, TableLayoutConstraints.FILL);
			MyResultSet res = null;
			String tmpTestCode = "";
			Integer colIdx = 0;
			for(int i=0; i<stationList.size(); i++) {
				// for all stations (show routine test limits if applicable for the station)
				try {
					curStation = stationList.get("Station"+ (i+1));
					res = db.executeQuery("select * from " + Configuration.ROUTINE_LIMITS + " where pump_type_id='" + curStation.curPumpTypeId +"' order by case code when 'FO' then 0 when 'DP' then 1 when 'SO' then 2 end");
					colIdx = curStation.stationIdx;
					String detStr = "";
					if (curStation.curTypeTestFreq != 1 && curStation.testListRoutine.size() > 0) {
						while (res.next()) {
							tmpTestCode = res.getString("code");
							detStr += "<font color='white'>" + tmpTestCode + " <font color='aqua'>HEAD <font color='lime'>" + res.getString("head_ll") + " - " + res.getString("head_ul") + ", <font color='aqua'>FLOW <font color='lime'>" + res.getString("discharge_ll") + " - " + res.getString("discharge_ul") + ", <font color='aqua'>CUR <font color='lime'>" + res.getString("current_ll") + " - " + res.getString("current_ul") + ", <font color='aqua'>POW <font color='lime'>" + res.getString("power_ll") + " - " + res.getString("power_ul") + "<br/>";
						}
						JLabel lblRtLimit = new JLabel();
						lblRtLimit.setText("<html>" + detStr + "</html>");
						lblRtLimit.setHorizontalAlignment(SwingConstants.CENTER);
						lblRtLimit.setFont(new Font("Arial", Font.BOLD, 12));
						lblRtLimit.setBackground(Color.gray);
						lblRtLimit.setOpaque(true);
						pnlStationList.add(lblRtLimit, new TableLayoutConstraints(colIdx, 2, colIdx, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
					} else {
						JLabel lblRtLimit = new JLabel();
						lblRtLimit.setText("No routine tests, only type tests are enabled");
						lblRtLimit.setHorizontalAlignment(SwingConstants.CENTER);
						lblRtLimit.setForeground(Color.white);
						lblRtLimit.setFont(new Font("Arial", Font.BOLD, 12));
						lblRtLimit.setBackground(Color.darkGray);
						lblRtLimit.setOpaque(true);
						pnlStationList.add(lblRtLimit, new TableLayoutConstraints(colIdx, 2, colIdx, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
					}
				} catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(this, "Error while loading routine test limits:" + e.getMessage());
				}
			}
		}
		
		pnlStationList.revalidate();
	}
	
	private void loadHeadDetails(Station curStation) {
		try {
			curStation.testListRoutine.clear();
			curStation.testListType.clear();
			curStation.testList.clear();
			curStation.testListFromName.clear();
			
			// load routine tests
			MyResultSet res1 = db.executeQuery("select a.code as code,b.name as name,b.head as head from " + Configuration.TESTNAMES_ROUTINE + " a, " + Configuration.TESTNAMES_ALL + " b where a.code=b.code and a.pump_type_id=b.pump_type_id and a.pump_type_id='" + curStation.curPumpTypeId +"' order by a.seq");
			
			while (res1.next()) {
				curStation.testListRoutine.add(res1.getString("code"));
				curStation.testList.put(res1.getString("code"), res1.getString("name"));
				curStation.testListFromName.put(res1.getString("name"), res1.getString("code"));
				curStation.testListHead.put(res1.getString("code"), res1.getFloat("head"));
			}
			
			// load type tests
			res1 = db.executeQuery("select a.code as code,b.name as name,b.head as head from " + Configuration.TESTNAMES_TYPE + " a, " + Configuration.TESTNAMES_ALL + " b where a.code=b.code and a.pump_type_id=b.pump_type_id and a.pump_type_id='" + curStation.curPumpTypeId +"' order by a.seq");
			while (res1.next()) {
				curStation.testListType.add(res1.getString("code"));
				if (!curStation.testList.containsKey(res1.getString("name"))) {
					curStation.testList.put( res1.getString("code"), res1.getString("name"));
					curStation.testListFromName.put(res1.getString("name"), res1.getString("code"));
					curStation.testListHead.put(res1.getString("code"), res1.getFloat("head"));
				}
			}
			
			// refresh the station status label if required
			if (curStation.lastUsedTestSNo > 0) {
				if ((curStation.nextTestType.equals("T") && curStation.lastUsedTestSNo < curStation.testListType.size()) || (curStation.nextTestType.equals("R") && curStation.lastUsedTestSNo < curStation.testListRoutine.size())) {
					statusLabelList.get(curStation.stationIdx).setText("IN PROGRESS [" + (curStation.lastUsedTestSNo + "/" + String.valueOf(curStation.nextTestType.equals("T") ? curStation.testListType.size() : curStation.testListRoutine.size())) + "]");
				}
			}
			
			// keep global flag for valve control
			if ((curStation.testListRoutine.size() > 0 && curStation.testListRoutine.get(0).equals("SO")) || (curStation.testListType.size() > 0 && curStation.testListType.get(0).equals("SO"))) {
				isSOFirst = true;
			} else {
				isSOFirst = false;
			}
			
		} catch (SQLException se) {
			JOptionPane.showMessageDialog(this, "Error while loading test head details:" + se.getMessage());
		}
	}
	
	// function to find next empty row in the reading list
	private int findNextEmptyRow() {
		int r = 0;
		// add new row if table is full already
		if (!tblTestEntry.getValueAt(tblTestEntry.getRowCount() - 1, 0).toString().isEmpty()) {
			r = tblTestEntry.getRowCount();
			addRowIfReq(r);
		} else {
			// empty row from bottom of the table
			for(int i=tblTestEntry.getRowCount()-1; i>0; i--) {
				if(tblTestEntry.getValueAt(i, 0).toString().isEmpty() && !tblTestEntry.getValueAt(i-1, 0).toString().isEmpty()) {
					r = i;
					break;
				}
			}
		}
		return r;
	}
	
	// function to add row if required
	private void addRowIfReq(int curRow) {
		// add an empty row if required
		if (curRow >= tblTestEntry.getRowCount()) {
			// add a new row
			DefaultTableModel defModel = (DefaultTableModel) tblTestEntry.getModel();
			defModel.addRow( new Object[] {"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""});
			scrlTest.getVerticalScrollBar().setValue(scrlTest.getVerticalScrollBar().getMaximum() + 10);
		}
		tblTestEntry.update(tblTestEntry.getGraphics());
		tblTestEntry.setRowSelectionInterval(curRow, curRow);
		tblTestEntry.scrollRectToVisible(tblTestEntry.getCellRect(curRow, 0, true));
	}
	
	// function to set capture date and sno
	private void setCaptureDateAndSNo(Station curStation, int row) throws Exception {
		int curRow = row;
		tblTestEntry.setRowSelectionInterval(curRow, curRow);
		tblTestEntry.scrollRectToVisible(tblTestEntry.getCellRect(curRow, 0, true));
		// set date & reading number
		
		//date
		java.util.Date today = Calendar.getInstance().getTime();
		SimpleDateFormat reqDtFormat = new SimpleDateFormat("dd-MM-yyyy");
		tblTestEntry.setValueAt(reqDtFormat.format(today), curRow, 4);
		
		// test type & name
		if (!curStation.reTest) {
			tblTestEntry.setValueAt(curStation.stationName.substring(7), curRow, 0);
			tblTestEntry.setValueAt(curStation.nextTestType, curRow, 1);
			tblTestEntry.setValueAt(curStation.nextTestCode, curRow, 2);
			tblTestEntry.setValueAt(curStation.nextRatedVolt, curRow, 3);
			
			// pump sno & motor sno
			if (curStation.nextNewSet) {
				if (!curStation.noChangeSNo) {
					if (!curStation.scannedPumpSNo.isEmpty()) {
						// last scanned barcode
						curStation.lastUsedPumpSNo = curStation.scannedPumpSNo;
						curStation.lastUsedMotSNo = curStation.scannedPumpSNo;
					} else {
						// auto generate
						HashMap<String, String> snoPair = Configuration.setAndGetSNo(curStation.curPumpTypeId);
						curStation.lastUsedPumpSNo = snoPair.get("pump_sno");
						curStation.lastUsedMotSNo = snoPair.get("motor_sno");
					}
				}
				// test sno
				curStation.lastUsedTestSNo = 1;
			/*} else if (curStation.reTest) {
				if (!tblTestEntry.getValueAt(curRow, 7).toString().trim().isEmpty()) {
					// retain the test number being retest
					curStation.lastUsedTestSNo = Integer.valueOf(tblTestEntry.getValueAt(curRow, 7).toString().trim());
				} else if (curStation.nextNewSet) {
					curStation.lastUsedTestSNo = 1;
				} else {
					curStation.lastUsedTestSNo = curStation.lastUsedTestSNo + 1;
				} */
			} else {
				curStation.lastUsedTestSNo = curStation.lastUsedTestSNo + 1;
			}
			
			// set the value in the table
			tblTestEntry.setValueAt(curStation.lastUsedPumpSNo, curRow, 5);
			tblTestEntry.setValueAt(curStation.lastUsedMotSNo, curRow, 6);
			tblTestEntry.setValueAt(curStation.lastUsedTestSNo, curRow, 7);
		}
			
		// set sno, test and total test in hmi for remote station mode if required
		if (curStation.isParamExist("Set SNo")) {
			try {
				String noPart = curStation.lastUsedPumpSNo;
				if (Configuration.IS_BARCODE_USED.equals("YES") && Configuration.IS_MODEL_FROM_BARCODE.equals("YES")) {
					noPart = noPart.replaceAll(curStation.curPumpType, "");
				}
				noPart = Configuration.parseSNo(noPart);
				if (noPart.length() > 4) {
					noPart = noPart.substring(noPart.length()-4);
				}
				curStation.writeParamValue("Set SNo", Integer.valueOf(noPart));
				curStation.writeParamValue("Set Current Test", Integer.valueOf(curStation.lastUsedTestSNo));
				curStation.writeParamValue("Set Total Test", curStation.nextTestType.equals("T") ? curStation.testListType.size() : curStation.testListRoutine.size());
			} catch (Exception e) {
				// ignore
			}
		} else if (Configuration.MULTI_STATION_MODE.equals("YES")) {
			System.out.println("Warning: Remote station mode is on - Unable to set test details in HMI. Ignoring it...");
		}
	}
	
	// function to next test to be performed based on last test performed
	private boolean setNextTest(Station curStation) {
		String lastType = "";
		String lastCode = "";
		curStation.nextNewSet = false;
		curStation.noChangeSNo = false;
		
		lastType = curStation.nextTestType;
		lastCode = curStation.nextTestCode;
		totLastRoutineTest = findTotLastRoutineTest(curStation.curPumpTypeId);
		if (lastType.isEmpty()) {
			if (!curStation.testListRoutine.isEmpty() && (curStation.curTypeTestFreq != 1 || curStation.curTypeTestFreq == 0)) {
				curStation.nextTestType = "R";
				curStation.nextTestCode = curStation.testListRoutine.get(0);
			} else {
				curStation.nextTestType = "T";
				curStation.nextTestCode = curStation.testListType.get(0);
			}
			curStation.nextNewSet = true;
		} else if (lastType.equals("R")) {
			if (curStation.testListRoutine.isEmpty()) {  // may happen when user empties routine tests in between auto capture
				curStation.nextNewSet = true;
				curStation.nextTestType = "T";
				curStation.nextTestCode = curStation.testListType.get(0);
			} else {
				System.out.println("last test code " + lastCode);
				if (curStation.testListRoutine.indexOf(lastCode) < curStation.testListRoutine.size()-1 ) {
					System.out.println(curStation.stationName + ", index:" + curStation.testListRoutine.indexOf(lastCode) + ",size:" + (curStation.testListRoutine.size()-1));
					curStation.nextTestType = "R";
					curStation.nextTestCode = curStation.testListRoutine.get(curStation.testListRoutine.indexOf(lastCode)+1);
				} else if (totLastRoutineTest >= curStation.curTypeTestFreq-1 && curStation.curTypeTestFreq != 0) {
					curStation.nextNewSet = true;
					System.out.println("no of type tests " + curStation.testListType.size());
					if (!curStation.testListType.isEmpty()) {
						curStation.nextTestType = "T";
						curStation.nextTestCode = curStation.testListType.get(0);
					} else {
						curStation.nextTestType = "R";
						curStation.nextTestCode = curStation.testListRoutine.get(0);
					}
				} else { //continue with routine test
					curStation.nextNewSet = true;
					System.out.println(curStation.stationName + ", next new test:" + curStation.nextNewSet);
					curStation.nextTestType = "R";
					curStation.nextTestCode = curStation.testListRoutine.get(0);
				}
			}
		} else { // type test
			if (curStation.testListType.isEmpty()) {  // may happen when user empties type tests in between auto capture
				curStation.nextNewSet = true;
				curStation.nextTestType = "R";
				curStation.nextTestCode = curStation.testListRoutine.get(0);
			} else {
				if (curStation.testListType.indexOf(lastCode) < curStation.testListType.size()-1) {
					curStation.nextTestType = "T";
					curStation.nextTestCode = curStation.testListType.get(curStation.testListType.indexOf(lastCode)+1);
				} else {
					curStation.nextNewSet = true;
					if (!curStation.testListRoutine.isEmpty() && curStation.curTypeTestFreq != 1) {
						curStation.nextTestType = "R";
						curStation.nextTestCode = curStation.testListRoutine.get(0);
					} else {
						curStation.nextTestType = "T";
						curStation.nextTestCode = curStation.testListType.get(0);
					}
				}
			}
		}
		// multi voltage handling
		if (curStation.nextNewSet) {
			if (curStation.voltList.size() > 1 && curStation.curOtherVoltsDis.equals("N")) {
				if (curStation.noRatedVolt < curStation.voltList.size()-1) {
					curStation.noRatedVolt = curStation.noRatedVolt+1;
					curStation.noChangeSNo = true;
					// repeat the last test (routine/type) for next voltage
					if (!lastType.isEmpty()) { // first time it will be empty
						if (lastType.equals("R")) {
							curStation.nextTestType = "R";
							curStation.nextTestCode = curStation.testListRoutine.get(0);
						} else {
							curStation.nextTestType = "T";
							curStation.nextTestCode = curStation.testListType.get(0);
						}
					}
				} else {
					curStation.noRatedVolt = 0;
					curStation.noChangeSNo = false;
				}
			} else {
				curStation.noRatedVolt = 0;
			}
		}
		curStation.nextRatedVolt = curStation.voltList.get(curStation.noRatedVolt);
		
		return curStation.nextNewSet;
	}
	
	// function to find total number of last type test performed
	private int findTotLastRoutineTest(String pumpTypeId) {
		// browse thru last 100 records and find number of routine tests
		int tot = 0;
		try {
			MyResultSet res = db.executeQuery("select pump_slno from " + Configuration.READING_DETAIL + " where pump_type_id ='" + pumpTypeId +"' and test_type = 'R' and " + 
											"rowid > (select case when max(rowid)>0 then max(rowid) else 0 end from " + Configuration.READING_DETAIL + " where pump_type_id ='" + pumpTypeId + "' and test_type = 'T' ) group by pump_slno");
			while(res.next()) {
				tot++;
			}
						
		} catch (SQLException se) {
			JOptionPane.showMessageDialog(this, "Error finding total number of recent type tests:" + se.getMessage());
		}
		return tot;
	}
		
	private void capture(Station curStation, int curRow) throws Exception{
		// 0. clear the pre-captured readings if any
		for (int i=8; i<tblTestEntry.getColumnCount(); i++) {
			tblTestEntry.setValueAt("", curRow, i);
		}
		
		dataReadError = false;

		int k = 8;
			
		// 1. speed
		try {
			tblTestEntry.setValueAt(curStation.devSpeedData.get("Speed"), curRow, k++); // speed Note:format of magtrol response R1+#####
		} catch (Exception ne) {
			// set an error flag
			dataReadError = true;
		}
		
		// 2. pressure, vacuum, flow
		try {
			Float pres = 0F;
			Float vac = 0F;
			Float flw = 0F;
			
			k = 9;
			pres = Math.abs(Float.valueOf(curStation.devHeadData.get("Pressure")));
			vac = Math.abs(Float.valueOf(curStation.devHeadData.get("Vacuum")));
			flw = Float.valueOf(curStation.devHeadData.get("Flow"));
			
			// ISI handling
			if (!(Configuration.LAST_USED_ISSTD.startsWith("IS 8034:") || Configuration.LAST_USED_ISSTD.startsWith("IS 14220:"))) { // vacuum not available
				tblTestEntry.setValueAt(convertHead(vac, curStation.curHdUnit), curRow, k++); // vacuum [sHead] / ejector head in case of 12225
			}
			tblTestEntry.setValueAt(convertHead(pres, curStation.curHdUnit), curRow, k++); // pressure [dhead]  
			tblTestEntry.setValueAt(convertDischarge(flw, curStation.curPumpFlowUnit), curRow, k++);
		} catch (Exception ne) {
			// set an error flag
			dataReadError = true;
		}
			
		// 3. power
		try {
			// ISI handling
			if (Configuration.LAST_USED_ISSTD.startsWith("IS 8034:") || Configuration.LAST_USED_ISSTD.startsWith("IS 14220:")) { // vacuum not available
				k = 11;
			} else {
				k = 12;
			}
			tblTestEntry.setValueAt(curStation.devPowerData.get("Voltage"), curRow,  k++); // voltage
			tblTestEntry.setValueAt(curStation.devPowerData.get("Current"), curRow,  k++); // current
			tblTestEntry.setValueAt(curStation.devPowerData.get("Power"), curRow,  k++); // watt
			tblTestEntry.setValueAt(curStation.devPowerData.get("Frequency"), curRow,  k++); // frequency
		} catch (Exception ne) {
			// set an error flag
			dataReadError = true;
		}
		
		// turn off signal receipt after a while
		txtSignal.setBackground(Color.darkGray);
		txtSignal.setText("");
		
		boolean sqlError = false; 
		
		// save the changes
		sqlError = saveChanges(curRow, curRow);
		
		// refresh result preview
		if (!tblTestEntry.getValueAt(curRow, 0).toString().trim().isEmpty()) {
			refreshResultPreview(curStation.stationName, curStation.lastUsedPumpSNo, curStation.nextTestType, curStation.nextRatedVolt);
			// save again the changes to store result
			try {
				db.executeUpdate("update " + Configuration.READING_DETAIL + " set remarks = '" +  tblTestEntry.getValueAt(curRow, (15 + vacColOffset)) + "' where pump_type_id ='" + curStation.curPumpTypeId + "' and pump_slno= '" + tblTestEntry.getValueAt(curRow, 5) +"' and test_name_code = '" + tblTestEntry.getValueAt(curRow, 2) + "' and rated_volt = '" + tblTestEntry.getValueAt(curRow, 3) + "'");
			} catch (SQLException se) {
				sqlError = true;
			}
		}
		
		if (sqlError || dataReadError) {
			logError(curStation.stationName + ": CAPTURE FAILED");
		}
	}
	
	private boolean saveChanges(int fromRow, int toRow) throws Exception {
		changeApplicationStatus("SAVING CURRENT READING...");
		lblStatus.update(lblStatus.getGraphics());
		Boolean sqlError = false;
		String testType = "";
		String testName = "";
		String tmpSHead = "";
		Station curStation = null;
		Boolean isNewTest = false;
				
		for(int i=fromRow; i<=toRow; i++) {
			// insert or update detail reading
			if (tblTestEntry.getValueAt(i, 1).toString().trim().isEmpty()) {
				continue;
			}
			testType = tblTestEntry.getValueAt(i, 1).toString().trim();
			if (testType.isEmpty()) {
				testType = "T"; // manual if left empty
			}
			testName = tblTestEntry.getValueAt(i, 2).toString().trim();
			if (testName.isEmpty()) {
				testName = "M"; // manual if left empty
			}
			
			curStation = stationList.get("Station" + tblTestEntry.getValueAt(i, 0).toString());
			isNewTest = false;
			try {
				// ISI handling
				if (Configuration.LAST_USED_ISSTD.startsWith("IS 8034:") || Configuration.LAST_USED_ISSTD.startsWith("IS 14220:")) { // vacuum not available
					db.executeUpdate("insert into " + Configuration.READING_DETAIL + " values ('" + testType + "', '" + testName + "', '"+ curStation.curPumpTypeId + "','" + dbDtFormat.format(reqDtFormat.parse(tblTestEntry.getValueAt(i, 4).toString().trim())) + "','" + tblTestEntry.getValueAt(i, 5) + "','" + tblTestEntry.getValueAt(i, 6) + "','" + tblTestEntry.getValueAt(i, 7) + "',"+
							   "'" + tblTestEntry.getValueAt(i, 8) + "','','" + tblTestEntry.getValueAt(i, 9) + "','" + tblTestEntry.getValueAt(i, 10) + "','" + tblTestEntry.getValueAt(i, 11) + "'," + 
							   "'" + tblTestEntry.getValueAt(i, 12) + "','" + tblTestEntry.getValueAt(i, 13) + "','" + tblTestEntry.getValueAt(i, 14) + "','" + tblTestEntry.getValueAt(i, 15) + "'," +
							   "'" + Configuration.LINE_NAME + "','" + Configuration.USER + "','" + tblTestEntry.getValueAt(i, 3) + "','" + tblTestEntry.getValueAt(i, 0) + "')");
				} else {
					db.executeUpdate("insert into " + Configuration.READING_DETAIL + " values ('" + testType + "', '" + testName + "', '"+ curStation.curPumpTypeId + "','" + dbDtFormat.format(reqDtFormat.parse(tblTestEntry.getValueAt(i, 4).toString().trim())) + "','" + tblTestEntry.getValueAt(i, 5) + "','" + tblTestEntry.getValueAt(i, 6) + "','" + tblTestEntry.getValueAt(i, 7) + "',"+
							   "'" + tblTestEntry.getValueAt(i, 8) + "','" + tblTestEntry.getValueAt(i, 9) + "','" + tblTestEntry.getValueAt(i, 10) + "','" + tblTestEntry.getValueAt(i, 11) + "'," + 
							   "'" + tblTestEntry.getValueAt(i, 12) + "','" + tblTestEntry.getValueAt(i, 13) + "','" + tblTestEntry.getValueAt(i, 14) + "','" + tblTestEntry.getValueAt(i, 15) + "'," +
							   "'" + tblTestEntry.getValueAt(i, 16) + "','" + Configuration.LINE_NAME + "','" + Configuration.USER + "','" + tblTestEntry.getValueAt(i, 3) + "','" + tblTestEntry.getValueAt(i, 0) + "')");
				}
				isNewTest = true;
			} catch (SQLException se) {
				try {
					if (se.getMessage().contains("not unique")) {
						// update the reading as it already exist
						// ISI handling
						if (Configuration.LAST_USED_ISSTD.startsWith("IS 8034:") || Configuration.LAST_USED_ISSTD.startsWith("IS 14220:")) { // vacuum not available
							db.executeUpdate("update " + Configuration.READING_DETAIL + " set test_date ='" + dbDtFormat.format(reqDtFormat.parse(tblTestEntry.getValueAt(i, 4).toString().trim())) + "', mot_slno = '" + tblTestEntry.getValueAt(i, 6) + "', " +
									"speed = '" + tblTestEntry.getValueAt(i, 8) + "', shead = '', dhead = '" + tblTestEntry.getValueAt(i, 9) + "', flow = '" + tblTestEntry.getValueAt(i, 10) + "', volt = '" + tblTestEntry.getValueAt(i, 11) + "', " +
									"current = '" + tblTestEntry.getValueAt(i, 12) + "', power = '" + tblTestEntry.getValueAt(i, 13) + "', freq = '" + tblTestEntry.getValueAt(i, 14) +  "'," +
									"remarks = '" +  tblTestEntry.getValueAt(i, 15) + "', line = '" + Configuration.LINE_NAME + "', user = '" + Configuration.USER + "' where pump_type_id ='" + curStation.curPumpTypeId + "' and pump_slno= '" + tblTestEntry.getValueAt(i, 5) +"' and test_name_code = '" + tblTestEntry.getValueAt(i, 2) + "' and rated_volt = '" + tblTestEntry.getValueAt(i, 3) + "'");
						} else {
							db.executeUpdate("update " + Configuration.READING_DETAIL + " set test_date ='" + dbDtFormat.format(reqDtFormat.parse(tblTestEntry.getValueAt(i, 4).toString().trim())) + "', mot_slno = '" + tblTestEntry.getValueAt(i, 6) + "', " +
									"speed = '" + tblTestEntry.getValueAt(i, 8) + "', shead = '" + tblTestEntry.getValueAt(i, 9) + "', dhead = '" + tblTestEntry.getValueAt(i, 10) + "', flow = '" + tblTestEntry.getValueAt(i, 11) + "', volt = '" + tblTestEntry.getValueAt(i, 12) + "', " +
									"current = '" + tblTestEntry.getValueAt(i, 13) + "', power = '" + tblTestEntry.getValueAt(i, 14) + "', freq = '" + tblTestEntry.getValueAt(i, 15) +  "'," +
									"remarks = '" +  tblTestEntry.getValueAt(i, 16) + "', line = '" + Configuration.LINE_NAME + "', user = '" + Configuration.USER + "' where pump_type_id ='" + curStation.curPumpTypeId + "' and pump_slno= '" + tblTestEntry.getValueAt(i, 5) +"' and test_name_code = '" + tblTestEntry.getValueAt(i, 2) + "'  and rated_volt = '" + tblTestEntry.getValueAt(i, 3) + "'");
						}
					} else {
						throw se;
					}
				} catch (SQLException sqle) {
					logError("Failed inserting reading for pump slno " + tblTestEntry.getValueAt(i, 5).toString().trim() + ":" + sqle.getMessage());
					sqlError = true;
				}
			}
		} // for
		// save addon tests
		int curRow = tblTestEntry.getSelectedRow();
		if (Configuration.IS_SP_TEST_ON.equals("1") && curRow >= 0 && !tblTestEntry.getValueAt(curRow, 0).toString().isEmpty()) {
			curStation = stationList.get("Station" + tblTestEntry.getValueAt(curRow, 0).toString());
			try {
				db.executeUpdate("insert into " + Configuration.OPTIONAL_TEST + " (pump_type_id, pump_slno, rated_volt, test_code, val_1, val_2) values ('" + curStation.curPumpTypeId + "', '" + tblTestEntry.getValueAt(curRow, 5).toString().trim() + "','" + tblTestEntry.getValueAt(curRow, 3).toString().trim() + "','SP','" + txtSPSec.getText().toString().trim() + "','" + txtSPSuc.getText().toString().trim() + "')");
			} catch (SQLException se) {
				try {
					if (se.getMessage().contains("not unique")) {
						// update the reading as it already exist
						db.executeUpdate("update " + Configuration.OPTIONAL_TEST + " set val_1 ='" + txtSPSec.getText().toString().trim() + "', val_2 = '" + txtSPSuc.getText().toString().trim() + 
								"' where pump_type_id ='" + curStation.curPumpTypeId + "' and pump_slno= '" + tblTestEntry.getValueAt(curRow, 5).toString().trim() +"' and rated_volt = '" + tblTestEntry.getValueAt(curRow, 3).toString().trim() + "' and test_code='SP'");
					} else {
						throw se;
					}
				} catch (SQLException sqle) {
					logError("Failed saving optional test for pump slno " + tblTestEntry.getValueAt(curRow, 5).toString().trim() + ":" + sqle.getMessage());
					sqlError = true;
				}
			}
		}
		
		// update last used sno in pump type table
		try {
			if (!reTest && isNewTest) { // no need to update in case of retest case
				db.executeUpdate("update " + Configuration.PUMPTYPE + " set recent_pump_sno = '" + curStation.lastUsedPumpSNo + "', recent_motor_sno = '" + curStation.lastUsedMotSNo + "'" + 
						"where pump_type_id='" + curStation.curPumpTypeId + "'");
				db.executeUpdate("update " + Configuration.OUTPUT + " set pump_type_id='"+ curStation.curPumpTypeId + "', recent_pump_sno = '" + curStation.lastUsedPumpSNo + "', recent_motor_sno = '" + curStation.lastUsedMotSNo + 
						"', recent_test_type = '" + curStation.nextTestType + "', recent_test_code = '" + curStation.nextTestCode + "'" +
						"where station_no='" + curStation.stationName + "' and line='" + Configuration.LINE_NAME + "'");
			}
		} catch (SQLException se) {
			logError("Failed updating recently used sno for corresponding pump type: " + se.getMessage());
			sqlError = true;
		}
				
		if (sqlError) {
			changeApplicationStatus("SAVE FAILED");
		} else {
			changeApplicationStatus("CHANGES ARE SAVED");
			saved=true;
		}
		
		return sqlError;
		
	}
	
	// function to open a test file
	public void openFile(String queryText, boolean clearTable, Boolean isAppInit) {
		
		try {
			// clear test tables
			if (clearTable) {
				clearForm();
			}
			
			// open and set reading detail
			MyResultSet res = db.executeQuery(queryText);
			if (res.isRowsAvail()) {
				
				int r = 0;
				int c = 0;
				String tmpPumpType = "";
				while (res.next()) {
					c = 0;
					tblTestEntry.setValueAt(res.getString("station_no"), r, c++);
					tblTestEntry.setValueAt(res.getString("test_type"), r, c++);
					tblTestEntry.setValueAt(res.getString("test_name_code"), r, c++);
					tblTestEntry.setValueAt(res.getString("rated_volt"), r, c++);
					
					tblTestEntry.setValueAt(reqDtFormat.format(dbDtFormat.parse(res.getString("test_date"))), r, c++);
					tblTestEntry.setValueAt(res.getString("pump_slno"), r, c++);
					tblTestEntry.setValueAt(res.getString("mot_slno"), r, c++);
					tblTestEntry.setValueAt(res.getString("test_slno"), r, c++);
					
					tblTestEntry.setValueAt(res.getInt("speed"), r, c++);
					// ISI handling
					if (!(Configuration.LAST_USED_ISSTD.startsWith("IS 8034:") || Configuration.LAST_USED_ISSTD.startsWith("IS 14220:"))) {
						tblTestEntry.setValueAt(res.getFloat("shead"), r, c++);
					}
					tblTestEntry.setValueAt(res.getFloat("dhead"), r, c++);
					tblTestEntry.setValueAt(formatFlow(res.getFloat("flow")), r, c++);
					tblTestEntry.setValueAt(dotOne.format(res.getFloat("volt")), r, c++);
					tblTestEntry.setValueAt(dotTwo.format(res.getFloat("current")), r, c++);
					tblTestEntry.setValueAt(dotThree.format(res.getFloat("power")), r, c++);
					tblTestEntry.setValueAt(dotTwo.format(res.getFloat("freq")), r, c++);
					tblTestEntry.setValueAt(res.getString("remarks"), r, c++);
					
					tmpPumpType = res.getString("pump_type_id");
					
					++r;
					if (r == tblTestEntry.getRowCount()) {
						// add a new row
						DefaultTableModel defModel = (DefaultTableModel) tblTestEntry.getModel();
						defModel.addRow( new Object[] {"", "", "", "", "", "", "", "", "", "", "", "","", "",""});
					}
					
				}
				// set last pump type
				if (stationList.size() <= 1 && !tmpPumpType.isEmpty() && !stationList.get(Configuration.LAST_USED_STATION).curPumpTypeId.equals(tmpPumpType)) {
					setPumpType(tmpPumpType, false, isAppInit, "choose");
				}
				
				// add new row at the end if required
				tblTestEntry.setRowSelectionInterval(r, r);
				tblTestEntry.scrollRectToVisible(tblTestEntry.getCellRect(r,0,true));
				
				tblTestEntry.requestFocusInWindow();
			}
			
		} catch (Exception se) {
			JOptionPane.showMessageDialog(this, "Error opening existing test:" + se.getMessage());
			se.printStackTrace();
			changeApplicationStatus("FILE OPEN FAILED");
			return;
		}
	}
	
	private String formatFlow(Float flw) {
		String curDisUnit = lblFlowUnit.getText();
		String dFlow = "0";
		// format
		if(curDisUnit.contains("lps")) {
			dFlow = dotTwo.format(flw);
		} else if(curDisUnit.contains("lpm")) {
			dFlow = dotOne.format(flw);
		} else if(curDisUnit.contains("sup")) { // mcph
			dFlow = dotTwo.format(flw);
		} else if(curDisUnit.contains("lph)")) {
			dFlow = dotZero.format(flw);
		}
		return dFlow;
	}
	
	// function to perform auto capture
	
	private void startAutoCapture() {
		tglAuto.setSelected(true);
		tglAutoActionPerformed();
	}
	
	private void stopAutoCapture() {
		tglAuto.setSelected(false);
		tglAutoActionPerformed();
	}
	
	// function to start device live reading thread
	private void startLiveReading() {
		stopLiveRequested = false;
		threadLiveRead = new Thread(liveReading);
		threadLiveRead.start();
	}
	
	private void stopLiveReading() {
		stopLiveRequested = true;
		if (threadLiveRead != null) {
			threadLiveRead.interrupt();
			while(threadLiveRead.isAlive()) {
				// wait until the thread dies completely
				logError("Live Reading:Waiting as the thread is alive", true);
			}
		}
	}
	
	public void stopThreads() {
		stopLiveReading();
		stopAutoCapture();
	}
	
	public void startThreads() {
		if (Configuration.TEST_MODE.equals("A")) {
			startLiveReading();
			startAutoCapture();
		}
	}
	
	private void initDevTables() throws Exception {
		// 1. for device info
		db.executeUpdate("CREATE TABLE IF NOT EXISTS DEVICE (dev_id integer primary key autoincrement, line text, station_no text, dev_name text, dev_adr text, dev_port text, dev_type text, baud_rt integer, data_bits integer, stop_bits integer, parity integer, wc integer, endianness text, fc integer, ip_cmd text, is_in_use text, is_common_port text, comm_protocol text, ip_address text, ip_port text, unique (line, station_no, dev_name));");
		db.executeUpdate("CREATE TABLE IF NOT EXISTS DEVICE_PARAM (dev_id integer, param_name text, param_adr text, conv_factor text, format_text text, reg_type text, remark text, unique (dev_id, param_name), foreign key(dev_id) references DEVICE(dev_id))");
	}
	
	// check & create device setting table if needed
	public void checkDevSet(String stationId) {
		try {
			MyResultSet res = null;
			Boolean insertNeeded = false;
			
			res = db.executeQuery("select count(*) as tot from DEVICE where line='" + Configuration.LINE_NAME + "' and station_no='" + stationId + "'");
			
			if (res.next()) {
				if (res.getInt("tot") == 0) {
					insertNeeded = true;
				}
			}
			
			if (insertNeeded) {
				// DEVICES & DEVICE PARAMS
				setDevSettings(stationId, "");
			}
		} catch (SQLException se) {
			se.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error loading device settings:" + se.getMessage());
		}
	}
	
	public void setDevSettings(String stationId, String devName) throws SQLException {
		String devAdr = stationId.substring(stationId.length()-1);
		Integer tmpId = 0;
		MyResultSet res = null;
		
		if (devName.isEmpty() || devName.equals("Grindex Meter with HMI")) {
			db.executeUpdate("delete from DEVICE where line='" + Configuration.LINE_NAME + "' and station_no='" + stationId + "' and dev_name='Grindex Meter with HMI'");
			db.executeUpdate("insert into DEVICE(line, station_no, dev_name, dev_adr, dev_port, dev_type, baud_rt, data_bits, stop_bits, parity, wc, endianness, fc, ip_cmd, is_in_use, is_common_port, comm_protocol, ip_address, ip_port) values ('" + Configuration.LINE_NAME + "', '" + stationId + "', 'Grindex Meter with HMI', '1', '', 'M', 19200, 8, 2, 0, 2, 'MSB First', 0, '', 'true', 'true', 'RTU', '', '2200')");
			res = db.executeQuery("select seq from sqlite_sequence where name='DEVICE'");
			if (res.next()) {
				tmpId = res.getInt("seq");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type) values (" + tmpId + ", 'Pressure A', '11', '', '#0.00', 'Holding Float')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type) values (" + tmpId + ", 'Vacuum A', '41', '*10', '#0.00', 'Holding Float')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type) values (" + tmpId + ", 'Flow A', '31', '', '#0.00', 'Holding Float')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type) values (" + tmpId + ", 'Pressure B', '11', '', '#0.00', 'Holding Float')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type) values (" + tmpId + ", 'Vacuum B', '41', '*10', '#0.00', 'Holding Float')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type) values (" + tmpId + ", 'Flow B', '31', '', '#0.00', 'Holding Float')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type) values (" + tmpId + ", 'Voltage', '75', '', '#0.0', 'Input Float')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type) values (" + tmpId + ", 'Current', '79', '', '#0.00', 'Input Float')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type) values (" + tmpId + ", 'Power', '81', '/1000', '#0.000', 'Input Float')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type) values (" + tmpId + ", 'Frequency', '69', '', '#0.00', 'Input Float')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type) values (" + tmpId + ", 'Voltage 3 Ph', '63', '', '#0.0', 'Input Float')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type) values (" + tmpId + ", 'Current 3 Ph', '65', '', '#0.00', 'Input Float')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type) values (" + tmpId + ", 'Power 3 Ph', '67', '/1000', '#0.000', 'Input Float')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type) values (" + tmpId + ", 'Frequency 3 Ph', '69', '', '#0.00', 'Input Float')");
			}
		}
		
		if (devName.isEmpty() || devName.equals("PLC")) {
			db.executeUpdate("delete from DEVICE where line='" + Configuration.LINE_NAME + "' and station_no='" + stationId + "' and dev_name='PLC'");
			db.executeUpdate("insert into DEVICE(line, station_no, dev_name, dev_adr, dev_port, dev_type, baud_rt, data_bits, stop_bits, parity, wc, endianness, fc, ip_cmd, is_in_use, is_common_port, comm_protocol, ip_address, ip_port) values ('" + Configuration.LINE_NAME + "', '" + stationId + "', 'PLC', '1', '', 'M', 19200, 8, 2, 0, 2, 'LSB First', 0, '', 'false', 'true', 'RTU', '', '2200')");
			res = db.executeQuery("select seq from sqlite_sequence where name='DEVICE'");
			if (res.next()) {
				tmpId = res.getInt("seq");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type, remark) values (" + tmpId + ", 'Pressure A', '25', '/10', '#0.00', 'Input', 'Delete if Grindex meter used')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type, remark) values (" + tmpId + ", 'Vacuum A', '35', '/10', '#0.00', 'Input', 'Delete if Grindex meter used')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type, remark) values (" + tmpId + ", 'Flow A', '45', '', '#', 'Input', 'Delete if Grindex meter used')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type, remark) values (" + tmpId + ", 'Pressure B', '25', '/10', '#0.00', 'Input', 'Delete if Grindex meter used')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type, remark) values (" + tmpId + ", 'Vacuum B', '35', '/10', '#0.00', 'Input', 'Delete if Grindex meter used')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type, remark) values (" + tmpId + ", 'Flow B', '55', '', '#', 'Input', 'Delete if Grindex meter used')");
				// optional parameters
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type, remark) values (" + tmpId + ", 'Self Priming Time', '-1', '/10', '#', 'Input', 'For IS8472 only')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type, remark) values (" + tmpId + ", 'Set Suction Lift', '-1', '', '', 'Holding', 'For IS8472 only')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type, remark) values (" + tmpId + ", 'Capture Signal Coil', '-1', '', '', 'Coil', 'For panel with push button')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type, remark) values (" + tmpId + ", 'Test Off Coil', '-1', '', '', 'Coil', 'For panel with automatic valve control only')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type, remark) values (" + tmpId + ", 'Station On Coil', '-1', '', '', 'Coil', 'For remote station with dedicated HMI only')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type, remark) values (" + tmpId + ", 'Set Result', '-1', '', '', 'Holding', 'For remote station with dedicated HMI only')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type, remark) values (" + tmpId + ", 'Set SNo', '-1', '', '', 'Holding', 'For remote station with dedicated HMI only')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type, remark) values (" + tmpId + ", 'Set Current Test', '-1', '', '', 'Holding', 'For remote station with dedicated HMI only')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type, remark) values (" + tmpId + ", 'Set Total Test', '-1', '', '', 'Holding', 'For remote station with dedicated HMI only')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type, remark) values (" + tmpId + ", 'Set Declared Head', '-1', '', '', 'Holding', 'For remote station with dedicated HMI only')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type, remark) values (" + tmpId + ", 'Valve Control Register', '-1', '', '', 'Holding', 'For automated valve control')");
			}
		}
		
		if (devName.isEmpty() || devName.equals("Power Meter")) {
			db.executeUpdate("delete from DEVICE where line='" + Configuration.LINE_NAME + "' and station_no='" + stationId + "' and dev_name='Power Meter'");
			db.executeUpdate("insert into DEVICE(line, station_no, dev_name, dev_adr, dev_port, dev_type, baud_rt, data_bits, stop_bits, parity, wc, endianness, fc, ip_cmd, is_in_use, is_common_port, comm_protocol, ip_address, ip_port) values ('" + Configuration.LINE_NAME + "', '" + stationId + "', 'Power Meter', '" + devAdr + "', '', 'M', 9600, 8, 1, 0, 2, 'MSB First', 0, '', 'true', 'true', 'RTU', '', '2200')");
			res = db.executeQuery("select seq from sqlite_sequence where name='DEVICE'");
			if (res.next()) {
				tmpId = res.getInt("seq");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type) values (" + tmpId + ", 'Current', '4096', '', '#0.00', 'Holding Float')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type) values (" + tmpId + ", 'Voltage', '4098', '', '#0.0', 'Holding Float')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type) values (" + tmpId + ", 'Power', '4104', '/1000', '#0.000', 'Holding Float')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type) values (" + tmpId + ", 'Frequency', '4152', '', '#0.00', 'Holding Float')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type) values (" + tmpId + ", 'Current 3 Ph', '4186', '', '#0.00', 'Holding Float')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type) values (" + tmpId + ", 'Voltage 3 Ph', '4190', '', '#0.0', 'Holding Float')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type) values (" + tmpId + ", 'Power 3 Ph', '4194', '/1000', '#0.000', 'Holding Float')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type) values (" + tmpId + ", 'Frequency 3 Ph', '4200', '', '#0.00', 'Holding Float')");
			}
		}
		
		if (devName.isEmpty() || devName.equals("HIOKI Power Meter")) {
			db.executeUpdate("delete from DEVICE where line='" + Configuration.LINE_NAME + "' and station_no='" + stationId + "' and dev_name='HIOKI Power Meter'");
			db.executeUpdate("insert into DEVICE(line, station_no, dev_name, dev_adr, dev_port, dev_type, baud_rt, data_bits, stop_bits, parity, wc, endianness, fc, ip_cmd, is_in_use, is_common_port, comm_protocol, ip_address, ip_port) values ('" + Configuration.LINE_NAME + "', '" + stationId + "', 'HIOKI Power Meter', 'NA', '', 'S', 9600, 8, 1, 0, 0, '', 0, '', 'false', 'true', 'RTU', '', '2200')");
			res = db.executeQuery("select seq from sqlite_sequence where name='DEVICE'");
			if (res.next()) {
				tmpId = res.getInt("seq");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type) values (" + tmpId + ", 'Current', 'NA', '', '#0.00', 'NA')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type) values (" + tmpId + ", 'Voltage', 'NA', '', '#0.0', 'NA')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type) values (" + tmpId + ", 'Power', 'NA', '/1000', '#0.000', 'NA')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type) values (" + tmpId + ", 'Frequency', 'NA', '', '#0.00', 'NA')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type) values (" + tmpId + ", 'Current 3 Ph', 'NA', '', '#0.00', 'NA')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type) values (" + tmpId + ", 'Voltage 3 Ph', 'NA', '', '#0.0', 'NA')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type) values (" + tmpId + ", 'Power 3 Ph', 'NA', '/1000', '#0.000', 'NA')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type) values (" + tmpId + ", 'Frequency 3 Ph', 'NA', '', '#0.00', 'NA')");
			}
		}
		
		if (devName.isEmpty() || devName.equals("Speed Tester")) {
			db.executeUpdate("delete from DEVICE where line='" + Configuration.LINE_NAME + "' and station_no='" + stationId + "' and dev_name='Speed Tester'");
			db.executeUpdate("insert into DEVICE(line, station_no, dev_name, dev_adr, dev_port, dev_type, baud_rt, data_bits, stop_bits, parity, wc, endianness, fc, ip_cmd, is_in_use, is_common_port, comm_protocol, ip_address, ip_port) values ('" + Configuration.LINE_NAME + "', '" + stationId + "', 'Speed Tester', 'NA', '', 'S', 2400, 8, 1, 0, 0, '', 0, 'R1', 'true', 'false', 'RTU', '', '2200')"); // magtrol
			res = db.executeQuery("select seq from sqlite_sequence where name='DEVICE'");
			if (res.next()) {
				tmpId = res.getInt("seq");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type) values (" + tmpId + ", 'Speed', 'NA', '', '#', 'NA')");
			}
		}
		
		if (devName.isEmpty() || devName.equals("AAROHI Meter")) {
			db.executeUpdate("delete from DEVICE where line='" + Configuration.LINE_NAME + "' and station_no='" + stationId + "' and dev_name='AAROHI Meter'");
			db.executeUpdate("insert into DEVICE(line, station_no, dev_name, dev_adr, dev_port, dev_type, baud_rt, data_bits, stop_bits, parity, wc, endianness, fc, ip_cmd, is_in_use, is_common_port, comm_protocol, ip_address, ip_port) values ('" + Configuration.LINE_NAME + "', '" + stationId + "', 'AAROHI Meter', 'NA', '', 'S', 9600, 8, 1, 0, 0, '', 0, '', 'false', 'false', 'RTU', '', '2200')");
			res = db.executeQuery("select seq from sqlite_sequence where name='DEVICE'");
			if (res.next()) {
				tmpId = res.getInt("seq");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type) values (" + tmpId + ", 'Voltage', 'NA', '', '#0.0', 'NA')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type) values (" + tmpId + ", 'Current', 'NA', '', '#0.00', 'NA')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type) values (" + tmpId + ", 'Power', 'NA', '/1000', '#0.000', 'NA')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type) values (" + tmpId + ", 'Frequency', 'NA', '', '#0.00', 'NA')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type) values (" + tmpId + ", 'Current 3 Ph', 'NA', '', '#0.00', 'NA')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type) values (" + tmpId + ", 'Voltage 3 Ph', 'NA', '', '#0.0', 'NA')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type) values (" + tmpId + ", 'Power 3 Ph', 'NA', '/1000', '#0.000', 'NA')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type) values (" + tmpId + ", 'Frequency 3 Ph', 'NA', '', '#0.00', 'NA')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type) values (" + tmpId + ", 'Speed', 'NA', '', '#', 'NA')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type) values (" + tmpId + ", 'Pressure A', 'NA', '', '#0.00', 'NA')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type) values (" + tmpId + ", 'Vacuum A', 'NA', '', '#', 'NA')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type) values (" + tmpId + ", 'Flow A', 'NA', '', '#0.00', 'NA')");
			}
		}
		
		if (devName.isEmpty() || devName.equals("FREHNIG Flow Meter")) {
			db.executeUpdate("delete from DEVICE where line='" + Configuration.LINE_NAME + "' and station_no='" + stationId + "' and dev_name='FREHNIG Flow Meter'");
			db.executeUpdate("insert into DEVICE(line, station_no, dev_name, dev_adr, dev_port, dev_type, baud_rt, data_bits, stop_bits, parity, wc, endianness, fc, ip_cmd, is_in_use, is_common_port, comm_protocol, ip_address, ip_port) values ('" + Configuration.LINE_NAME + "', '" + stationId + "', 'FREHNIG Flow Meter', 'NA', '', 'S', 9600, 8, 1, 0, 0, '', 0, '1', 'false', 'false', 'RTU', '', '2200')");
			res = db.executeQuery("select seq from sqlite_sequence where name='DEVICE'");
			if (res.next()) {
				tmpId = res.getInt("seq");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type) values (" + tmpId + ", 'Flow A', 'NA', '', '#0.00', 'NA')");
			}
		}
		
		if (devName.isEmpty() || devName.equals("VFD")) {
			db.executeUpdate("delete from DEVICE where line='" + Configuration.LINE_NAME + "' and station_no='" + stationId + "' and dev_name='VFD'");
			db.executeUpdate("insert into DEVICE(line, station_no, dev_name, dev_adr, dev_port, dev_type, baud_rt, data_bits, stop_bits, parity, wc, endianness, fc, ip_cmd, is_in_use, is_common_port, comm_protocol, ip_address, ip_port) values ('" + Configuration.LINE_NAME + "', '" + stationId + "', 'VFD', 'NA', '', 'M', 9600, 8, 1, 0, 0, '', 0, '', 'false', 'true', 'RTU', '', '2200')");
			res = db.executeQuery("select seq from sqlite_sequence where name='DEVICE'");
			if (res.next()) {
				tmpId = res.getInt("seq");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type) values (" + tmpId + ", 'VDF_Voltage', 'NA', '', '#0.0', 'NA')");
				db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type) values (" + tmpId + ", 'VDF_Frequency', 'NA', '', '#0.00', 'NA')");
			}
		}
	}
	
	// function to initialize pressure controller
	/* public void initializePressCont() {
		try {
			if (presCtrlr != null) {
				try {
					presCtrlr.close();
				} catch (Exception e) {
					// ignore
				}
			}
			presCtrlr = new DevicePresCont();
			presCtrlr.initialize(Configuration.LAST_USED_PRES_CTRL_PORT, 1);
			presContPortError = false;
		} catch (Exception e) {
			presContPortError = true;
			logError("Error initializing pressure controller:" + e.getMessage());
			changeApplicationStatus("DEVICE COMMUNICATION FAILED");
		}
		presContInitialized = true;
	} */
	
	// function to fetch current readings from connected devices
	private synchronized void readHeadReadings(Station curStation) throws Exception {
		try {
			curStation.devHeadData.put("Pressure", stationList.get(curStation.curPrOPSt).readParamValue("Pressure " + curStation.curPrOP));
			curStation.devHeadData.put("Vacuum", stationList.get(curStation.curPrOPSt).readParamValue("Vacuum " + curStation.curPrOP));
			Thread.sleep(500);
			curStation.devHeadData.put("Flow", stationList.get(curStation.curFlowOPSt).readParamValue("Flow " + curStation.curFlowOP));	
		} catch (Exception e) {
			curStation.devHeadData.clear();
			logError(curStation.stationName + ": Error reading head params: " + e.getMessage());
			throw e;
		}
	}
	
	private synchronized void readPowerReadings(Station curStation) throws Exception {
		try {
			String threePhStr = curStation.curPumpPhase.equals("Three") ? " 3 Ph" : "";
			curStation.devPowerData.put("Current", curStation.readParamValue("Current" + threePhStr));
			curStation.devPowerData.put("Voltage", curStation.readParamValue("Voltage" + threePhStr));
			curStation.devPowerData.put("Power", curStation.readParamValue("Power" + threePhStr));
			curStation.devPowerData.put("Frequency", curStation.readParamValue("Frequency" + threePhStr));
		} catch (Exception e) {
			curStation.devPowerData.clear();
			e.printStackTrace();
			logError(curStation.stationName + ": Error reading power params: " + e.getMessage());
			throw e;
		}
	}
	
	private synchronized void readSpeedReadings(Station curStation) throws Exception {
		try {
			curStation.devSpeedData.put("Speed", curStation.readParamValue("Speed"));
		} catch (Exception e) {
			curStation.devSpeedData.clear();
			if (!e.getMessage().contains("not found")) {
				logError(curStation.stationName + ": Error reading speed param: " + e.getMessage());
			}
			throw e;
		}
	}
	
	// function to set result
	private void setTestStatus(String result, Station curStation, Integer curSelRow, String testType) {
		if (result.equals("READY")) {
			stationLabelList.get(curStation.stationIdx).setForeground(clrOPOn);
			statusLabelList.get(curStation.stationIdx).setForeground(Color.orange);
			statusLabelList.get(curStation.stationIdx).setText(result);
			txtSignal.setForeground(Color.orange);
		} else if (result.equals("PASS")) {
			lblResBulb.setIcon(imgPassBulb);
			lblResBulb.setText(result);
			if ((testType.equals("R") && Configuration.IS_PERF_BASED_ON_GRAPH_FOR_ROUTINE.equals("NO")) || tblTestEntry.getValueAt(curSelRow, 2).toString().startsWith("S")) {
				tblTestEntry.setValueAt(result, curSelRow, tblTestEntry.getColumnCount()-1);
			}
			if (curStation.lastUsedPumpSNo.equals(tblTestEntry.getValueAt(curSelRow, 5).toString().trim())) {
				stationLabelList.get(curStation.stationIdx).setBackground(clrTestPass);
			} else {
				stationLabelList.get(curStation.stationIdx).setBackground(Color.gray);
			}
		} else if (result.equals("FAIL")) {
			lblResBulb.setIcon(imgFailBulb);
			lblResBulb.setText(result);
			if ((testType.equals("R") && Configuration.IS_PERF_BASED_ON_GRAPH_FOR_ROUTINE.equals("NO")) || tblTestEntry.getValueAt(curSelRow, 2).toString().startsWith("S")) {
				tblTestEntry.setValueAt(result, curSelRow, tblTestEntry.getColumnCount()-1);
			}
			if (curStation.lastUsedPumpSNo.equals(tblTestEntry.getValueAt(curSelRow, 5).toString().trim())) {
				stationLabelList.get(curStation.stationIdx).setBackground(Color.red);
			} else {
				stationLabelList.get(curStation.stationIdx).setBackground(Color.gray);	
			}
		} else if (result.equals("IN PROGRESS")) {
			lblResBulb.setIcon(imgIdleBulb);
			lblResBulb.setText(result);
			stationLabelList.get(curStation.stationIdx).setBackground(Color.gray);
			statusLabelList.get(curStation.stationIdx).setForeground(Color.yellow);
			txtSignal.setForeground(Color.white);
			// result labels
			lblResH.setBackground(Color.gray);
			lblResF.setBackground(Color.gray);
			lblResC.setBackground(Color.gray);
			lblResP.setBackground(Color.gray);
			lblResE.setBackground(Color.gray);
		} else if (result.equals("IDLE")) {
			lblResBulb.setIcon(imgIdleBulb);
			lblResBulb.setText(result);
			stationLabelList.get(curStation.stationIdx).setBackground(Color.gray);
			stationLabelList.get(curStation.stationIdx).setForeground(Color.black);
			statusLabelList.get(curStation.stationIdx).setForeground(Color.black);
			statusLabelList.get(curStation.stationIdx).setText(result);
			// result labels
			lblResH.setBackground(Color.gray);
			lblResF.setBackground(Color.gray);
			lblResC.setBackground(Color.gray);
			lblResP.setBackground(Color.gray);
			lblResE.setBackground(Color.gray);
		} else if (result.equals("ERROR")) {
			lblResBulb.setIcon(imgIdleBulb);
			lblResBulb.setText(result);
			// result labels
			lblResH.setBackground(Color.gray);
			lblResF.setBackground(Color.gray);
			lblResC.setBackground(Color.gray);
			lblResP.setBackground(Color.gray);
			lblResE.setBackground(Color.gray);
		}
		
		// test running status
		curStation.testInProg = result.equals("IN PROGRESS");
		
		// set the status in remote HMI as well for remote station mode
		if (curStation.isParamExist("Set Result")) {
			try {
				curStation.writeParamValue("Set Result", result.equals("PASS") ? 3 : result.equals("FAIL") ? 2 : 1);
			} catch (Exception e) {
				// ignore
			}
		} else if (Configuration.MULTI_STATION_MODE.equals("YES")) {
			System.out.println("Warning: Remote station mode is on - Unable to set test result in HMI. Ignoring it...");
		}
	}
	
	// function to add graph preview
	private void refreshResultPreview(String station, String slno, String testType, String testV) {
		if (station.isEmpty() || !stationList.containsKey(station)) {
			JOptionPane.showMessageDialog(this, "Station of this test is currently not exist", "Invalid station", JOptionPane.WARNING_MESSAGE);
			return;
		}
		Station curStation = stationList.get(station);
		lastGraphSNo = slno;
		lastGraphRV = testV;
		defDisUnit = curStation.curPumpFlowUnit;
		defHdUnit = curStation.curHdUnit;
		// remove if the chart is already exist
		try {
			pnlGraph.remove(0);
		} catch (ArrayIndexOutOfBoundsException ae) {
			// ignore it
		}
		
		if (slno.isEmpty()) {
			// graph
			pnlGraph.add(lblNA, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			pnlGraph.revalidate();
			return;
		}
		// show slno in title
		lblResSNo.setText(slno + " [" + (testType.equals("R") ? "Routine" : "Type") + "]");
		
		Double rtD = 0.0D;
		Double rtH = 0.0D;
		Double rtP = 0.0D;
		Double rtE = 0.0D;
		Double rtC = 0.0D;
		
		MyResultSet res = null;
		MyResultSet res2 = null; 
		Integer curSelRow = tblTestEntry.getSelectedRow();
		
		//1. SIMPLE ROUTINE TEST - NO GRAPH & JUST CALCULATE RESULT BASED ROUTINE LIMIT SET
		if (testType.equals("R") && Configuration.IS_PERF_BASED_ON_GRAPH_FOR_ROUTINE.equals("NO")) {
			try {
				pnlGraph.add(lblNA, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				pnlGraph.update(pnlGraph.getGraphics());
				
				lblResP.setEnabled(true);
				lblResP.setToolTipText("Power");
				lblResE.setBackground(Color.gray);
				lblResE.setEnabled(false); // eff is not applicable for routine tests
				lblResE.setToolTipText("Efficiency not applicable for routine test");
				
				if (!tblTestEntry.getValueAt(curSelRow, 0).toString().trim().isEmpty()) {
					String curTestNm = tblTestEntry.getValueAt(curSelRow, 2).toString();
					Double gDisL = 0D;
					Double gHdL = 0D;
					Double gMotIPL = 0D;
					Double gAmpL = 0D;
					Double gDisU = 0D;
					Double gHdU = 0D;
					Double gMotIPU = 0D;
					Double gAmpU = 0D;
					res2 = db.executeQuery("select * from " + Configuration.ROUTINE_LIMITS + " where pump_type_id = '" + curStation.curPumpTypeId + "' and code='" + (curTestNm.startsWith("H") ? "DP" : curTestNm) + "'");
					if (res2.next()) {
						// guaranteed values
						gHdL = Double.valueOf(deriveHead(curStation, res2.getDouble("head_ll")));
						gDisL = Double.valueOf(deriveFlow(curStation, res2.getDouble("discharge_ll")));
						gAmpL = res2.getDouble("current_ll");
						gMotIPL = res2.getDouble("power_ll");
						gHdU = Double.valueOf(deriveHead(curStation, res2.getDouble("head_ul")));
						gDisU = Double.valueOf(deriveFlow(curStation, res2.getDouble("discharge_ul")));
						gAmpU = res2.getDouble("current_ul");
						gMotIPU = res2.getDouble("power_ul");
						
						// do perf cal & compare with guaranteed values
						perfCalc.doCalculate(lastGraphSNo,  curStation.curPumpTypeId, false, lastGraphRV, testType, curTestNm); // second param - rated frequency
						rtH = Double.valueOf(deriveHead(curStation, perfCalc.getRatedTotHead(0)));
						rtD = Double.valueOf(deriveFlow(curStation, perfCalc.getRatedDischarge(0)));
						rtP = perfCalc.getRatedMotorIP(0);
						rtC = perfCalc.getCurrent(0); 
					} else {
						// just pass it
						gHdL = rtH;
						gDisL = rtD;
						gMotIPL = rtP;
						gAmpL = rtC;
						gHdU = rtH;
						gDisU = rtD;
						gMotIPU = rtP;
						gAmpU = rtC;
					}
						
					String flowRes = rtD < gDisL || rtD > gDisU ? "Fail" : "Pass";
					String headRes = rtH < gHdL || rtH > gHdU ? "Fail" : "Pass";
					String motIpRes = rtP < gMotIPL || rtP > gMotIPU ? "Fail" : "Pass";
					String curRes = rtC < gAmpL || rtC > gAmpU ? "Fail" : "Pass";
					
					if (flowRes.equals("Fail") || headRes.equals("Fail") || motIpRes.equals("Fail") || curRes.equals("Fail")) {
						// red bulb
						setTestStatus("FAIL", curStation, curSelRow, testType);
						routAutoTestFailed = true;
					} else {
						// green bulb
						setTestStatus("PASS", curStation, curSelRow, testType);
					}
					
					// result labels
					lblResH.setBackground(headRes.equals("Fail")?Color.red:clrTestPass);
					lblResF.setBackground(flowRes.equals("Fail")?Color.red:clrTestPass);
					lblResC.setBackground(curRes.equals("Fail")?Color.red:clrTestPass);
					lblResP.setBackground(motIpRes.equals("Fail")?Color.red:clrTestPass);
				} // non empty test
			} catch (Exception e) {
				e.printStackTrace();
				logError("Warning: Unable to calculate result for " + slno + ": Insufficient values");
				setTestStatus("ERROR", curStation, 0, "");
			}
			return;
		} else { // type test or routine test with performance based on graph
		
			//2. OR (GRAPH & CALCULATE RESULT BASED ON THAT)
			
			lblResE.setEnabled(true);
			lblResE.setToolTipText("Efficiency");
			if (!Configuration.LAST_USED_ISSTD.startsWith("IS 8472:") || Configuration.REP_SHOW_MI_FOR_8472.equals("0")) {
				// motor input based result is not applicable
				lblResP.setEnabled(false);
				lblResP.setToolTipText("Motor input power is not applicable for type test of this IS");
			} else {
				lblResP.setToolTipText("Power");
				lblResP.setEnabled(true);
			}
			
			// 1. CHART
			JFreeChart chart = ChartFactory.createXYLineChart("", curStation.curPumpFlowUnit.contains("sup")?"Discharge (m�ph)":"Discharge (" + curStation.curPumpFlowUnit + ")", null, null, PlotOrientation.VERTICAL, true, true, false);
			XYPlot p = chart.getXYPlot();
			
			try {
				// retrieve head, pi and cur values and create corresponding data sets
				res = db.executeQuery("select * from " + Configuration.READING_DETAIL + " where pump_type_id='" + curStation.curPumpTypeId + "' and pump_slno='" + lastGraphSNo + "' and rated_volt = '" + lastGraphRV +"' order by test_name_code");
				if (res.isRowsAvail()) {
					
					// do perf calculation at rated freq 
					
					perfCalc.doCalculate(lastGraphSNo,  curStation.curPumpTypeId, false, lastGraphRV); // second param - rated frequency
					
					res2 = db.executeQuery("select * from " + Configuration.PUMPTYPE + " where pump_type_id = '" + res.getString("pump_type_id") + "'");
					res2.next();
					
					// create data set for all axis
					XYSeries srH = new XYSeries("TH");
					XYSeries srP = new XYSeries("IP");
					XYSeries srE = new XYSeries("O.A Eff.");
					XYSeries srC = new XYSeries("Cur.");
					Double actDis = 0.0D;
					Double actTH = 0.0D;
					Double actMI = 0.0D;
					Double actE = 0.0D;
					
					Double actDisL = 0.0D;
					Double actTHL = 0.0D;
					Double actMIL = 0.0D;
					Double actEL = 0.0D;
					Double actDisH = 0.0D;
					Double actTHH = 0.0D;
					Double actMIH = 0.0D;
					Double actEH = 0.0D;
					
					Double actC = 0.0D;
					
					int totRec = 0;
					Double upHead = 0D;
					Double upMi=0D;
					Double upE=0D;
					Double upCur=0D;
					
					while (res.next()) {
						rtH = Double.valueOf(deriveHead(curStation, perfCalc.getRatedTotHead(totRec)));
						rtP = perfCalc.getRatedMotorIP(totRec);
						rtE = perfCalc.getRatedOE(totRec); 
						rtC = res.getDouble("current");
						rtD = Double.valueOf(deriveFlow(curStation, perfCalc.getRatedDischarge(totRec)));
		
						srH.add(rtD, rtH);
						srP.add(rtD, rtP);
						srE.add(rtD, rtE);
						srC.add(rtD, rtC);
						
						// for range calc
						upHead = upHead < rtH ? rtH : upHead;
						upMi = upMi < rtP ? rtP : upMi;
						upE = upE < rtE ? rtE : upE;
						upCur = upCur < rtC ? rtC : upCur;
						
						++totRec;
					}
					XYSeriesCollection dsetH = new XYSeriesCollection(srH);
					XYSeriesCollection dsetP = new XYSeriesCollection(srP);
					XYSeriesCollection dsetE = new XYSeriesCollection(srE);
					XYSeriesCollection dsetC = new XYSeriesCollection(srC);
					
					// calculate upper bound range for head, pi, c
					upHead = Math.ceil(upHead+(upHead/10));
					upMi = Math.ceil(upMi+(upMi/30));
					upE = Math.ceil(upE+(upE/30));
					upCur = Math.ceil(upCur+(upCur/30));
		
					// add head axis
					NumberAxis axisHead = new NumberAxis("Total Head (" + curStation.curHdUnit + ")     1 DIV=" + deriveHead(curStation, upHead/100, true));
					axisHead.setLabelPaint(clrHead);
					axisHead.setAxisLinePaint(clrHead);
					Range rgHd = new Range(0.0, upHead);
					axisHead.setRange(rgHd);
					axisHead.setTickUnit(new NumberTickUnit(upHead/10));
					axisHead.setMinorTickCount(10);
					p.setRangeAxis(0, axisHead);
					p.setRangeAxisLocation(0, AxisLocation.TOP_OR_LEFT);
					XYItemRenderer rndrHd = new XYSplineRenderer(10);
					rndrHd.setPaint(clrHead);
					p.setRenderer(0, rndrHd);
					p.setDataset(0, dsetH);
				    p.mapDatasetToRangeAxis(0, 0);
					
					// add pi axis
				    Integer ra = 1;
				    NumberAxis axisMi = new NumberAxis("Input Power(kW)     1 DIV=" + dotThree.format(upMi/100));
				    XYItemRenderer rndrMi = new XYSplineRenderer(10);
				    if((Configuration.LAST_USED_ISSTD.startsWith("IS 8472:") || Configuration.LAST_USED_ISSTD.startsWith("IS 12225")) && Configuration.REP_SHOW_MI_FOR_8472.equals("1")) { // ISI handling
						axisMi.setLabelPaint(clrMI);
						axisMi.setAxisLinePaint(clrMI);
						Range rgMi = new Range(0.0, upMi);
						axisMi.setRange(rgMi);
						axisMi.setTickUnit(new NumberTickUnit(upMi/10));
						axisMi.setMinorTickCount(10);
						p.setRangeAxis(ra, axisMi);
						p.setRangeAxisLocation(ra, AxisLocation.TOP_OR_LEFT);
						rndrMi.setPaint(clrMI);
						p.setRenderer(ra, rndrMi);
				        p.setDataset(ra, dsetP);
				        p.mapDatasetToRangeAxis(ra, ra);
				        ra++;
				    }
			        
				    // add eff axis
				    NumberAxis axisE = new NumberAxis("O.A Eff. (%)     1 DIV=" + dotTwo.format(upE/100)); 
					
					axisE.setLabelPaint(clrE);
					axisE.setAxisLinePaint(clrE);
					Range rgE = new Range(0.0, upE);
					axisE.setRange(rgE);
					axisE.setTickUnit(new NumberTickUnit(upE/10));
					axisE.setMinorTickCount(10);
					p.setRangeAxis(ra, axisE);
					p.setRangeAxisLocation(ra, AxisLocation.TOP_OR_LEFT);
					XYItemRenderer rndrE = new XYSplineRenderer(10);
					rndrE.setPaint(clrE);
					p.setRenderer(ra, rndrE);
			        p.setDataset(ra, dsetE);
			        p.mapDatasetToRangeAxis(ra, ra);
			        ra++;
			        
			        // add cur axis
			        NumberAxis axisCur = new NumberAxis("Current (A)     1 DIV=" + dotTwo.format(upCur/100));
			        axisCur.setLabelPaint(clrCur);
			        axisCur.setAxisLinePaint(clrCur);
			        Range rgCur = new Range(0.0, upCur);
			        axisCur.setRange(rgCur);
			        axisCur.setTickUnit(new NumberTickUnit(upCur/10));
			        axisCur.setMinorTickCount(10);
					p.setRangeAxis(ra, axisCur);
					p.setRangeAxisLocation(ra, AxisLocation.TOP_OR_LEFT);
					XYItemRenderer rndrCur = new XYSplineRenderer(10);
					rndrCur.setPaint(clrCur);
					
					p.setRenderer(ra, rndrCur);
			        p.setDataset(ra, dsetC);
			        p.mapDatasetToRangeAxis(ra, ra);
			        
			        // hide axis as they are not required in preview due to space constraints
			        axisHead.setVisible(false);
			        axisMi.setVisible(false);
			        axisE.setVisible(false);
			        axisCur.setVisible(false);
			        
			        // 2. MARK THE POINTS AND DERIVE TEST RESULT
			        if ( (testType.equals("T") && totRec == curStation.testListType.size()) || (testType.equals("R") && totRec == curStation.testListRoutine.size()) || (lastGraphSNo != curStation.lastUsedPumpSNo)) { 
				        float[] dashes = {5.0F, 5.0F};
				        Stroke dashPen = new BasicStroke(1.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0F, dashes, 0.0F);
				        CustLine il = new CustLine();
				        
				        Double gDis = 0.0D;
				        Double gHd = 0.0D;
				        
				        Double gDisL = 0.0D;
				        Double gDisH = 0.0D;
				        
				        Double gHdL = 0.0D;
				        Double gHdH = 0.0D;
				        
			        	// 1. guaranteed discharge vs guaranteed TH
				        gDis = Double.valueOf(deriveFlow(curStation, res2.getDouble("discharge")));
				        gHd = Double.valueOf(deriveHead(curStation, res2.getDouble("head")));
				        
				        XYLineAnnotation lnGDvsGTH1 = new XYLineAnnotation(0.0D, gHd, gDis, gHd, dashPen,Color.RED);
				        XYLineAnnotation lnGDvsGTH2 = new XYLineAnnotation(gDis, 0.0D, gDis, gHd, dashPen,Color.RED);
				        rndrHd.addAnnotation(lnGDvsGTH1);
				        rndrHd.addAnnotation(lnGDvsGTH2);
				        
				        // 2. actual discharge vs actual TH
				        
				        // intersection of actual discharge vs actual TH via slope (m1) of guaranteed values
				        CustLine gLine = new CustLine(0D,0D,srH.getMaxX(),srH.getMaxY());
				        Double gslope = gHd/gDis;
				        Double ix = 0.0D;
				        Double iy = 0.0D;
				        for(int i=0; i<srH.getItemCount()-1; i++) {
				        	il = findIntersection(gLine, new CustLine(srH.getX(i).doubleValue(),srH.getY(i).doubleValue(),srH.getX(i+1).doubleValue(),srH.getY(i+1).doubleValue()),gslope, null);
				        	if (il != null) {
				        		ix = il.getX2();
				        		iy = il.getY2();
				        		break;
				        	}
				        }
				        
				        // intersection line
				        XYLineAnnotation lnADvsATHcut = new XYLineAnnotation(0.0D, 0.0D, ix, iy, new BasicStroke(1.0F),Color.RED);
				        rndrHd.addAnnotation(lnADvsATHcut);
				        // pointing lines
				        XYLineAnnotation lnADvsATH1 = new XYLineAnnotation(0.0D, iy, ix, iy, new BasicStroke(),Color.RED);
				        XYLineAnnotation lnADvsATH2 = new XYLineAnnotation(ix, 0.0D, ix, iy, new BasicStroke(),Color.RED);
				        rndrHd.addAnnotation(lnADvsATH1);
				        rndrHd.addAnnotation(lnADvsATH2);
				        XYPointerAnnotation pntADvsATH = new XYPointerAnnotation("",0,iy,0);
				        pntADvsATH.setTipRadius(0);
				        pntADvsATH.setArrowLength(20);
				        pntADvsATH.setArrowPaint(Color.RED);
				        XYPointerAnnotation pntADvsATH2 = new XYPointerAnnotation("",ix,0,4.71);
				        pntADvsATH2.setTipRadius(0);
				        pntADvsATH2.setArrowLength(20);
				        pntADvsATH2.setArrowPaint(Color.RED);
				        rndrHd.addAnnotation(pntADvsATH);
				        rndrHd.addAnnotation(pntADvsATH2);
				        
				        // update actual labels
				        actDis = ix;
				        actTH = iy;
				        
				        // 3. motor input cut by actual head
				        CustLine ADvsAHLine = new CustLine(ix,0.0D,ix,srH.getMaxY()+500); // 500 just increases scope of base line, no effect for normal cases
				        if((Configuration.LAST_USED_ISSTD.startsWith("IS 8472:") || Configuration.LAST_USED_ISSTD.startsWith("IS 12225")) && Configuration.REP_SHOW_MI_FOR_8472.equals("1")) { // ISI handling
							    
					        Double miix = 0.0D;
					        Double miiy = 0.0D;
					        for(int i=0; i<srP.getItemCount()-1; i++) {
					        	il = findIntersection(ADvsAHLine, new CustLine(srP.getX(i).doubleValue(),srP.getY(i).doubleValue(),srP.getX(i+1).doubleValue(),srP.getY(i+1).doubleValue()),null, null);
					        	if (il != null) {
					        		miix = il.getX2();
					        		miiy = il.getY2();
					        		break;
					        	}
					        }
					        XYDrawableAnnotation mrkATHvsMI = new XYDrawableAnnotation(miix,miiy,12,12,new CircleDrawer(Color.red, new BasicStroke(1.0F), null));
					        rndrMi.addAnnotation(mrkATHvsMI);
					        
					        // update actual label
					        actMI = miiy;
				        }
				        
					    // 3.1 OA Eff cut by actual head
				        Double eix = 0.0D;
				        Double eiy = 0.0D;
				        for(int i=0; i<srE.getItemCount()-1; i++) {
				        	il = findIntersection(ADvsAHLine, new CustLine(srE.getX(i).doubleValue(),srE.getY(i).doubleValue(),srE.getX(i+1).doubleValue(),srE.getY(i+1).doubleValue()),null, null);
				        	if (il != null) {
				        		eix = il.getX2();
				        		eiy = il.getY2();
				        		break;
				        	}
				        }
				        XYDrawableAnnotation mrkATHvsEff = new XYDrawableAnnotation(eix,eiy,12,12,new CircleDrawer(Color.red, new BasicStroke(1.0F), null));
				        rndrE.addAnnotation(mrkATHvsEff);
				     
				        // update actual label
				        actE = eiy;
					        
				        // 4. max current between guaranteed head range (common for both kind of graph views)
				        // 4.1 low head range cut current
				        CustLine lowRangeLine;
				        gHdL = Double.valueOf(deriveHead(curStation, res2.getDouble("head_low")));
				        if (gHdL<srH.getMinY()) {
				        	lowRangeLine = new CustLine(0D,srH.getMinY(),srH.getMaxX(),srH.getMinY());
				        } else {
				        	lowRangeLine = new CustLine(0D,gHdL,srH.getMaxX(),gHdL);
				        }
				        
				        Double lhix = 0.0D;
				        Double lhiy = 0.0D;
				        for(int i=0; i<srH.getItemCount()-1; i++) {
				        	il = findIntersection(lowRangeLine, new CustLine(srH.getX(i).doubleValue(),srH.getY(i).doubleValue(),srH.getX(i+1).doubleValue(),srH.getY(i+1).doubleValue()),null, null);
				        	if (il != null) {
				        		lhix = il.getX2();
				        		lhiy = il.getY2();
				        		break;
				        	}
				        }
				        XYDrawableAnnotation mrkLHvsCur = new XYDrawableAnnotation(lhix,lhiy,12,12,new CircleDrawer(Color.red, new BasicStroke(1.0F), null));
				        rndrHd.addAnnotation(mrkLHvsCur);
				        
				        CustLine lowRangeCutLine = new CustLine(lhix,0.0D,lhix,srH.getMaxY());
				        Double lhcix = 0.0D;
				        Double lhciy = 0.0D;
				        int curIndex1 = 0;
				        for(int i=0; i<srC.getItemCount()-1; i++) {
				        	il = findIntersection(lowRangeCutLine, new CustLine(srC.getX(i).doubleValue(),srC.getY(i).doubleValue(),srC.getX(i+1).doubleValue(),srC.getY(i+1).doubleValue()),null, null);
				        	if (il != null) {
				        		lhcix = il.getX2();
				        		lhciy = il.getY2();
				        		curIndex1 = i+1;
				        		break;
				        	}
				        }
				        XYDrawableAnnotation mrkLHCutvsCur = new XYDrawableAnnotation(lhcix,lhciy,12,12,new CircleDrawer(Color.red, new BasicStroke(1.0F), null));
				        rndrCur.addAnnotation(mrkLHCutvsCur);
				        
				        
				        // 4.2 high head range cut current
				        gHdH = Double.valueOf(deriveHead(curStation, res2.getDouble("head_high")));
				        CustLine hiRangeLine = new CustLine(0D,gHdH,srH.getMaxX(),gHdH);
				        Double hhix = 0.0D;
				        Double hhiy = 0.0D;
				        for(int i=0; i<srH.getItemCount()-1; i++) {
				        	il = findIntersection(hiRangeLine, new CustLine(srH.getX(i).doubleValue(),srH.getY(i).doubleValue(),srH.getX(i+1).doubleValue(),srH.getY(i+1).doubleValue()),null, null);
				        	if (il != null) {
				        		hhix = il.getX2();
				        		hhiy = il.getY2();
				        		break;
				        	}
				        }
				        XYDrawableAnnotation mrkHHvsCur = new XYDrawableAnnotation(hhix,hhiy,12,12,new CircleDrawer(Color.red, new BasicStroke(1.0F), null));
				        rndrHd.addAnnotation(mrkHHvsCur);
				        
				        CustLine hiRangeCutLine = new CustLine(hhix,0.0D,hhix,srH.getMaxY());
				        Double hhcix = 0.0D;
				        Double hhciy = 0.0D;
				        int curIndex2 = 0;
				        for(int i=0; i<srC.getItemCount()-1; i++) {
				        	il = findIntersection(hiRangeCutLine, new CustLine(srC.getX(i).doubleValue(),srC.getY(i).doubleValue(),srC.getX(i+1).doubleValue(),srC.getY(i+1).doubleValue()),null, null);
				        	if (il != null) {
				        		hhcix = il.getX2();
				        		hhciy = il.getY2();
				        		curIndex2 = i+1;
				        		break;
				        	}
				        }
				        XYDrawableAnnotation mrkHHCutvsCur = new XYDrawableAnnotation(hhcix,hhciy,12,12,new CircleDrawer(Color.red, new BasicStroke(1.0F), null));
				        rndrCur.addAnnotation(mrkHHCutvsCur);
				        
				        // 4.3 find max current between low and high cuts
				        Double maxCur = 0.0D;
				        if(curIndex1 < curIndex2) {
				        	for(int i=curIndex1; i <= curIndex2; i++) {
				        		maxCur = Math.max(maxCur, srC.getY(i).doubleValue());
				        	}
				        } else {
				        	for(int i=curIndex1; i >= curIndex2; i--) {
				        		maxCur = Math.max(maxCur, srC.getY(i).doubleValue());
				        	}
				        }
				        
				        maxCur = Math.max(maxCur, Math.max(lhciy, hhciy)); // this line is enough to find max current, but above loops make sure to find max curr in case there is a peak betweek low & hi cut.
				        
				        // update actual label
				        actC = maxCur;
		
				        // test result
				        
				        String flowRes = "";
						String headRes = "";
						String motIpRes = "";
						String effRes = "";
						String curRes = "";
						
						flowRes = ((actDis > (gDis*tolDisMax)) || (actDis < (gDis*tolDisMin))) ? "Fail" : "Pass";
						headRes = ((actTH > (gHd*tolHeadMax)) || (actTH < (gHd*tolHeadMin))) ? "Fail" : "Pass";
						if (Configuration.LAST_USED_ISSTD.startsWith("IS 8472:")) {
							motIpRes = ((actMI > (res2.getDouble("mot_ip")*tolPowMax)) || (actMI < (res2.getDouble("mot_ip")*tolPowMin))) ? "Fail" : "Pass"; 
						}
						curRes = ((actC > (res2.getDouble("amps")*tolCurMax)) || (actC < (res2.getDouble("amps")*tolCurMin))) ? "Fail" : "Pass";
						effRes = ((actE > (res2.getDouble("overall_eff")*tolEffMax)) || (actE < (res2.getDouble("overall_eff")*tolEffMin))) ? "Fail" : "Pass";
						
						if (flowRes.equals("Fail") || headRes.equals("Fail") || motIpRes.equals("Fail") || effRes.equals("Fail") || curRes.equals("Fail")) {
							setTestStatus("FAIL", curStation, curSelRow, testType);
						} else {
							setTestStatus("PASS", curStation, curSelRow, testType);
						}
						// result labels
						lblResH.setBackground(headRes.equals("Fail")?Color.red:clrTestPass);
						lblResF.setBackground(flowRes.equals("Fail")?Color.red:clrTestPass);
						lblResC.setBackground(curRes.equals("Fail")?Color.red:clrTestPass);
						lblResP.setBackground(motIpRes.equals("Fail")?Color.red:(motIpRes.equals("Pass")?clrTestPass:Color.gray));
						lblResE.setBackground(effRes.equals("Fail")?Color.red:clrTestPass);
					}
					
					// other chart look & feel
					chart.getLegend().setPosition(RectangleEdge.RIGHT);
					p.getDomainAxis().setLabelFont(p.getRangeAxis().getLabelFont());
					p.getDomainAxis().setTickLabelFont(p.getRangeAxis().getTickLabelFont());
					p.setDomainGridlinesVisible(true);
					p.setRangeGridlinesVisible(true);
					p.setBackgroundPaint(Color.green);
					p.setBackgroundAlpha(0.10F);
					p.setDomainGridlinePaint(Color.DARK_GRAY);
					p.setRangeGridlinePaint(Color.DARK_GRAY);
					
					p.setDomainMinorGridlinesVisible(true);
					p.setRangeMinorGridlinesVisible(true);
					p.setDomainMinorGridlinePaint(Color.LIGHT_GRAY);
					p.setRangeMinorGridlinePaint(Color.LIGHT_GRAY);
					
					p.getDomainAxis().setMinorTickMarksVisible(true);
					p.getDomainAxis().setMinorTickCount(10);
					p.getRangeAxis().setMinorTickMarksVisible(true);
					ra = 1;
					if((Configuration.LAST_USED_ISSTD.startsWith("IS 8472:") || Configuration.LAST_USED_ISSTD.startsWith("IS 12225")) && Configuration.REP_SHOW_MI_FOR_8472.equals("1")) { // ISI handling
						p.getRangeAxis(ra++).setMinorTickMarksVisible(true);
					}
					p.getRangeAxis(ra++).setMinorTickMarksVisible(true);
					p.getRangeAxis(ra).setMinorTickMarksVisible(true);
					
					p.getRenderer(0).setSeriesToolTipGenerator(0,new XYToolTipGenerator() {
						@Override
						public String generateToolTip(XYDataset arg0, int arg1, int arg2) {
							return "(Discharge:"+ ((defDisUnit.endsWith("s") || defDisUnit.contains("sup"))?dotTwo.format(arg0.getXValue(arg1, arg2)):defDisUnit.endsWith("m")?dotOne.format(arg0.getXValue(arg1, arg2)):dotZero.format(arg0.getXValue(arg1, arg2))) + ", TH:" + deriveHead(curStation,Double.valueOf(String.valueOf(arg0.getYValue(arg1, arg2))), true) + ")";
						}
					});
					ra = 1;
					if((Configuration.LAST_USED_ISSTD.startsWith("IS 8472:") || Configuration.LAST_USED_ISSTD.startsWith("IS 12225")) && Configuration.REP_SHOW_MI_FOR_8472.equals("1")) { // ISI handling
						p.getRenderer(ra++).setSeriesToolTipGenerator(0,new XYToolTipGenerator() {
							@Override
							public String generateToolTip(XYDataset arg0, int arg1, int arg2) {
								return "(Discharge:"+ ((defDisUnit.endsWith("s") || defDisUnit.contains("sup"))?dotTwo.format(arg0.getXValue(arg1, arg2)):defDisUnit.endsWith("m")?dotOne.format(arg0.getXValue(arg1, arg2)):dotZero.format(arg0.getXValue(arg1, arg2))) + ", IP:" + dotThree.format(arg0.getYValue(arg1, arg2)) + ")";
							}
						});
					}
					p.getRenderer(ra++).setSeriesToolTipGenerator(0,new XYToolTipGenerator() {
						@Override
						public String generateToolTip(XYDataset arg0, int arg1, int arg2) {
							return "(Discharge:"+ ((defDisUnit.endsWith("s") || defDisUnit.contains("sup"))?dotTwo.format(arg0.getXValue(arg1, arg2)):defDisUnit.endsWith("m")?dotOne.format(arg0.getXValue(arg1, arg2)):dotZero.format(arg0.getXValue(arg1, arg2))) + ", Eff.:" + dotTwo.format(arg0.getYValue(arg1, arg2)) + ")";
						}
					});
					p.getRenderer(ra).setSeriesToolTipGenerator(0,new XYToolTipGenerator() {
						@Override
						public String generateToolTip(XYDataset arg0, int arg1, int arg2) {
							return "(Discharge:"+ ((defDisUnit.endsWith("s")||defDisUnit.contains("sup"))?dotTwo.format(arg0.getXValue(arg1, arg2)):defDisUnit.endsWith("m")?dotOne.format(arg0.getXValue(arg1, arg2)):dotZero.format(arg0.getXValue(arg1, arg2))) + ", Cur.:" + dotTwo.format(arg0.getYValue(arg1, arg2)) + ")";
						}
					});
					
					// add chart
					ChartPanel chPanel = new ChartPanel(chart);
					chPanel.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							cmdReportsActionPerformed();
						}
					});
					chart.getTitle().setFont(new Font("Arial", Font.BOLD, 16));
					pnlGraph.add(chPanel, new TableLayoutConstraints(0,0,0,0,TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				} // res 2
			} catch (Exception se) {
				se.printStackTrace();
				logError("Warning: Unable to refresh graph for " + slno + ": Insufficient values to plot the graph");
				setTestStatus("ERROR", curStation, 0, "");
			} // try
		}
		pnlRes.revalidate();
	}
	
	// function to find and return the intersection of two lines - for result preview
	private CustLine findIntersection(CustLine line1, CustLine line2, Double m1, Double m2)
	{
		Double dx;
		Double dy;
		Double ix;
		Double iy;
		Double c1 = 0.0D;
		Double c2 = 0.0D;
		Boolean l1_vertical = false;
		
		dx = line1.getX2() - line1.getX1();
		dy = line1.getY2() - line1.getY1();
		
		// formula of line => y = mx + c
		if (m1 == null) {
			if (dx == 0) { // vertical line 1
				m1 = 0.0D;
				c1 = 0.0D;
				l1_vertical = true;
			} else {
				m1 = dy/dx;
				c1 = line1.getY1() - m1 * line1.getX1();
			}
		}
		
		
		dx = line2.getX2() - line2.getX1();
		dy = line2.getY2() - line2.getY1();
		if(m2 == null) {
			m2 = dy/dx;
		}
		c2 = line2.getY1() - m2 * line2.getX1(); 
		
		
		if((m1 - m2) == 0)
			return null;
		else
		{
			if (!l1_vertical) {
				ix = (c2 - c1) / (m1 - m2);
				iy = m1 * ix + c1;
			} else { // vertical line => x is constant, y is derived using the m & c of other line 
				ix = line1.getX1();
				iy = m2 * ix + c2;
			}
		}
		
		
		// bound check
		if(isPointInBox(line1.getX1(),line1.getY1(),line1.getX2(),line1.getY2(),ix,iy) && isPointInBox(line2.getX1(),line2.getY1(),line2.getX2(), line2.getY2(),ix,iy))
		{
			CustLine il = new CustLine(0.0D,0.0D,ix,iy);
			return il;
		} else {
			return null;
		}
	}

	private boolean isPointInBox(Double x1, Double y1, Double x2, Double y2, Double ix, Double iy)
	{
		Double left, top, right, bottom;
		
		if(x1 < x2) {
			left = x1;
			right = x2;
		} else {
			left = x2;
			right = x1;
		}
	
		if(y1 < y2) {
			top = y1;
			bottom = y2;
		} else {
			top = y2;
			bottom = y1;
		}
	
		if((ix+0.01) >= left && (ix-0.01) <= right && (iy+0.01) >= top && (iy-0.01) <= bottom ) {
			return true; 
		} else { 
			return false;
		}
	}
	
	// function to convert the flow based on user choice  - for result preview
	private String deriveFlow(Station curStation, Double flw) {
		String dFlow = "";
		if (!defDisUnit.equals(curStation.curPumpFlowUnit)) { // do conversion as user's choice
			// 1.unit
			if(defDisUnit.startsWith("l")) { // l to g or l to m3
				if(curStation.curPumpFlowUnit.startsWith("g")) {
					flw = flw * 0.26417F;
				} else if(curStation.curPumpFlowUnit.contains("sup")) {
					flw = flw * 0.001F;
				}
			}
			if(defDisUnit.startsWith("g")) { // g to l or g to m3
				if(curStation.curPumpFlowUnit.startsWith("l")) {
					flw = flw / 0.26417F;
				} else if(curStation.curPumpFlowUnit.contains("sup")) {
					flw = flw / 0.26417F * 0.001F;
				}
			}
			if(defDisUnit.contains("sup")) { // m3 to l or m3 to g
				if(curStation.curPumpFlowUnit.startsWith("l")) {
					flw = flw / 0.001F;
				} else if(curStation.curPumpFlowUnit.startsWith("g")) {
					flw = flw / 0.001F * 0.26417F;
				}
			}
			
			// 2.time
			if(defDisUnit.endsWith("s")) {  // s to m or s to h
				if(curStation.curPumpFlowUnit.endsWith("m")) {
					flw = flw * 60;
				} else if (curStation.curPumpFlowUnit.endsWith("h")) {
					flw = flw * 3600;
				}
			}
			if(defDisUnit.endsWith("m")) {  // m to s or m to h
				if(curStation.curPumpFlowUnit.endsWith("s")) {
					flw = flw / 60;
				} else if (curStation.curPumpFlowUnit.endsWith("h")) {
					flw = flw * 60;
				}
			}
			if(defDisUnit.endsWith("h")) {  // h to s or h to m
				if(curStation.curPumpFlowUnit.endsWith("s")) {
					flw = flw / 3600;
				} else if (curStation.curPumpFlowUnit.endsWith("m")) {
					flw = flw / 60;
				}
			}
		}
		
		// format
		if(curStation.curPumpFlowUnit.endsWith("s")) {
			dFlow = dotTwo.format(flw);
		} else if(curStation.curPumpFlowUnit.endsWith("m")) {
			dFlow = dotOne.format(flw);
		} else if(curStation.curPumpFlowUnit.contains("sup")) { // mcph
			dFlow = dotTwo.format(flw);
		} else if(curStation.curPumpFlowUnit.endsWith("h")) {
			dFlow = dotZero.format(flw);
		}
		
		return dFlow;
	}
		
	// function to convert the head based on user choice  - for result preview
	private String deriveHead(Station curStation, Double hd) {
		return deriveHead(curStation, hd, false);
	}
	private String deriveHead(Station curStation, Double hd, boolean formatOnly) {
		String dHd = "";
		if (!formatOnly && !defHdUnit.equals(curStation.curHdUnit)) { // do conversion as user's choice
			// 1.unit
			if(defHdUnit.equals("m")) { // m to ft or m to bar
				if(curStation.curHdUnit.equals("ft")) {
					hd = hd * 3.28084F;
				} else {
					hd = hd * 0.0980638F;
				}
			}
			if(defHdUnit.equals("ft")) { // ft to mt or ft to bar
				if(curStation.curHdUnit.equals("m")) {
					hd = hd / 3.28084F;
				} else {
					hd = (hd / 3.28084F) * 0.0980638F;
				}
			}
			if(defHdUnit.equals("bar")) { // bar to m or bar to ft
				if(curStation.curHdUnit.equals("m")) {
					hd = hd / 0.0980638F;
				} else {
					hd = (hd / 0.0980638F) * 3.28084F;
				}
			}
		}
		
		// format
		if(curStation.curHdUnit.equals("m")) { 
			dHd = dotTwo.format(hd);
		} else if(curStation.curHdUnit.equals("bar")) {
			dHd = dotTwo.format(hd);
		} else { // feet
			dHd = dotZero.format(hd);
		}
		
		return dHd;
	}
	
	
	// multi station capture thread
	public class AutoCaptureMS implements Runnable {
		
		private String station = "Station1";
		private Station curStation = null;
		
		public AutoCaptureMS(String station) {
			this.station = station;
			this.curStation = stationList.get(station);
		}
		
		public void run() {
			Integer len = 0;
			String signalReadData = "";
			
			// for valve control
			Float curVal = 0F;
			Float desVal = 0F;
			Float desTol = 0F;
			String curUom = "";
			Integer curV = isSOFirst ? minV : maxV;
			Boolean isSlowStep = false;
			Boolean wasLearntPos = false;
			Boolean valveAtRightPlace = false;
			Boolean isExtraReadingReq = false;
			String curValveType = "";
			Integer curNoTests = 0;
			Integer valveWaitTime = 500;
			Integer noOfRead = 0;
			Integer noOfAttempt = 4;
			Integer curTime = 0;
			String tmpTblName = "";
			
			while (true) { // run till interrupted
				try {
					// wait till the station is ON depends on signal method
					if (Configuration.MULTI_STATION_MODE.equals("YES")) {
						changeApplicationStatus("REMOTE STATION MODE ON");
						txtSignal.setText("Please check status of individual stations above");
						do { 
							// wait till the station is on
							Thread.sleep(500);
							// System.out.println("value of on coil " + curStation.readParamValue("Station On Coil") );
							// System.out.println("value of signal coil " + curStation.readParamValue("Capture Signal Coil") );
						} while (!Boolean.valueOf(curStation.readParamValue("Station On Coil")));
					}
					
					setTestStatus("READY", curStation, 0, "");
					
					do { // perform tests one by one as long as the station is on
						holdOnForValveCapture = false; // for auto valve capture
						if (!curStation.reTest) {
							setNextTest(curStation);
						} else {
							curStation.reTest = false;
						}
						// indication for new tests
						if (curStation.nextNewSet) {
							setTestStatus("READY", curStation, 0, "");
							if (!curStation.noChangeSNo) {
								curStation.scannedPumpSNo = "";
							}
						} else {
							setTestStatus("IN PROGRESS", curStation, 0, "");
						}
						// wait for capture based on signal method
						lblResBulb.setIcon(imgIdleBulb);
						if (Configuration.LAST_USED_SIGNAL_METHOD.equals("P")) {
							if (Configuration.MULTI_STATION_MODE.equals("NO")) {
								if (curStation.nextNewSet && !curStation.noChangeSNo) {
									if (Configuration.IS_BARCODE_USED.equals("YES")) {
										changeApplicationStatus("READY FOR NEXT PUMP - WAITING FOR SCANNER SIGNAL");
										if (Configuration.HEAD_ADJUSTMENT.equals("A")) {
											// here
											txtSignal.setText("Connect next pump and scan barcode to perform tests one by one []");
										} else {
											txtSignal.setText("Connect next pump and scan barcode []");
										}
										txtSignal.setEditable(true);
										txtSignal.requestFocusInWindow();
										len = txtSignal.getText().length()-1;
										txtSignal.setSelectionStart(len);
										do { 
											Thread.sleep(1000);
											signalReadData = txtSignal.getText().trim().substring(len, txtSignal.getText().trim().length()-1).trim();
										} while (signalReadData.isEmpty());
										txtSignal.setText("Signal - Received");
										curStation.scannedPumpSNo = signalReadData;
										// choose model from the barcode if opted
										if (Configuration.IS_MODEL_FROM_BARCODE.equals("YES")) {
											String tmpModel = signalReadData.substring(Integer.valueOf(Configuration.MODEL_START_POS_IN_BARCODE)-1, Integer.valueOf(Configuration.MODEL_END_POS_IN_BARCODE));
											if (!tmpModel.equals(curStation.curPumpType)) {
												if (!detectPumpType(tmpModel, curStation)) {
													curStation.nextTestType = "";
													continue; // abort the test, prompt to scan another number
												}
											}
										}
										// set the details in remote HMI
										if (curStation.isParamExist("Set SNo")) {
											try {
												String noPart = curStation.scannedPumpSNo;
												if (Configuration.IS_BARCODE_USED.equals("YES") && Configuration.IS_MODEL_FROM_BARCODE.equals("YES")) {
													noPart = noPart.replaceAll(curStation.curPumpType, "");
												}
												noPart = Configuration.parseSNo(noPart);
												if (noPart.length() > 4) {
													noPart = noPart.substring(noPart.length()-4);
												}
												curStation.writeParamValue("Set SNo", Integer.valueOf(noPart));
												curStation.writeParamValue("Set Current Test", 0);
												curStation.writeParamValue("Set Total Test", curStation.nextTestType.equals("T") ? curStation.testListType.size() : curStation.testListRoutine.size());
												curStation.writeParamValue("Set Result", 1);
											} catch (Exception e) {
												// ignore
											}
										} else if (Configuration.MULTI_STATION_MODE.equals("YES")) {
											System.out.println("Warning: Remote station mode is on - Unable to set test details in HMI. Ignoring it...");
										}
										changeApplicationStatus("READY FOR NEXT PUMP - BARCODE SCANNED");
										if (Configuration.HEAD_ADJUSTMENT.equals("A")) {
											txtSignal.setText("Press <TEST ON> button to perform tests one by one...");
										} else {
											txtSignal.setText("Press capture button once desired head [" + curStation.nextTestCode + "] is set...");
										}
									} else {
										changeApplicationStatus("READY FOR NEXT PUMP - WAITING FOR PANEL SIGNAL");
										if (Configuration.HEAD_ADJUSTMENT.equals("A")) {
											txtSignal.setText("Connect next pump and press <TEST ON> button to perform tests one by one...");
										} else {
											txtSignal.setText("Connect next pump and press capture button once desired head [" + curStation.nextTestCode + "] is set...");
										}
									}
								} else {
									if (curStation.noChangeSNo) {
										changeApplicationStatus("[SET NEXT VOLTAGE] TEST IN PROGRESS - WAITING FOR PANEL SIGNAL");
										if (Configuration.HEAD_ADJUSTMENT.equals("A")) {
											txtSignal.setText("Set next voltage [" + curStation.nextRatedVolt + "] and press <TEST ON> button to perform tests one by one...");
										} else {
											txtSignal.setText("Set next voltage [" + curStation.nextRatedVolt + "] and press capture button once desired head [" + curStation.nextTestCode + "] is set...");
										}
									} else {
										if (Configuration.HEAD_ADJUSTMENT.equals("A")) {
											changeApplicationStatus("TEST IN PROGRESS - AUTOMATED VALVE CONTROL");
										} else {
											changeApplicationStatus("TEST IN PROGRESS - WAITING FOR PANEL SIGNAL");
											txtSignal.setText("Press capture button once desired head [" + curStation.nextTestCode + "] is set...");
										}
									}
								}
							}
							if ((Configuration.HEAD_ADJUSTMENT.equals("M") || (Configuration.HEAD_ADJUSTMENT.equals("A") && curStation.nextNewSet)) || (Configuration.IS_BARCODE_USED.equals("YES") && (curStation.nextNewSet && !curStation.noChangeSNo))) {
								// wait for push button
								do {
									Thread.sleep(500);
								} while (!Boolean.valueOf(curStation.readParamValue("Capture Signal Coil")));
								// switch the signal off
								curStation.writeParamValue("Capture Signal Coil", false);
								System.out.println("switched off - value of signal coil " + curStation.readParamValue("Capture Signal Coil") );
							}
						} else {
							if (curStation.nextNewSet && !curStation.noChangeSNo) {
								if (Configuration.IS_BARCODE_USED.equals("YES")) {
									changeApplicationStatus("READY FOR NEXT PUMP - WAITING FOR SCANNER SIGNAL");
									if (Configuration.HEAD_ADJUSTMENT.equals("A")) {
										// here
										txtSignal.setText("Connect next pump and scan barcode to perform tests one by one []");
									} else {
										txtSignal.setText("Connect next pump and scan barcode once desired head [" + curStation.nextTestCode + "] is set []");
									}
								} else {
									changeApplicationStatus("READY FOR NEXT PUMP - WAITING FOR KEYBOARD SIGNAL");
									if (Configuration.HEAD_ADJUSTMENT.equals("A")) {
										txtSignal.setText("Connect next pump and press space bar to perform tests one by one...");
									} else {
										txtSignal.setText("Connect next pump and press space bar once desired head [" + curStation.nextTestCode + "] is set...");
									}
								}
							} else {
								if (curStation.noChangeSNo) {
									changeApplicationStatus("[SET NEXT VOLTAGE] TEST IN PROGRESS - WAITING FOR KEYBOARD SIGNAL");
									if (Configuration.HEAD_ADJUSTMENT.equals("A")) {
										txtSignal.setText("Set next voltage [" + curStation.nextRatedVolt + "] and press space bar to perform tests one by one...");
									} else {
										txtSignal.setText("Set next voltage [" + curStation.nextRatedVolt + "] and press space bar once desired head [" + curStation.nextTestCode + "] is set...");
									}
								} else {
									if (Configuration.HEAD_ADJUSTMENT.equals("A")) {
										changeApplicationStatus("TEST IN PROGRESS - AUTOMATED VALVE CONTROL");
									} else {
										changeApplicationStatus("TEST IN PROGRESS - WAITING FOR KEYBOARD SIGNAL");
										txtSignal.setText("Press space bar once desired head [" + curStation.nextTestCode + "] is set...");
									}
								}
							}
							if ((Configuration.HEAD_ADJUSTMENT.equals("M") || (Configuration.HEAD_ADJUSTMENT.equals("A") && curStation.nextNewSet)) || (Configuration.IS_BARCODE_USED.equals("YES") && (curStation.nextNewSet && !curStation.noChangeSNo))) {
								txtSignal.setEditable(true);
								txtSignal.requestFocusInWindow();
								len = txtSignal.getText().length()-1;
								txtSignal.setSelectionStart(len);
								do { 
									Thread.sleep(1000);
									signalReadData = txtSignal.getText().trim().substring(len, txtSignal.getText().trim().length()-1);
									signalReadData = Configuration.IS_BARCODE_USED.equals("YES") && (curStation.nextNewSet && !curStation.noChangeSNo) ? signalReadData.trim() : signalReadData; // avoid spaces in scanned number if any
								} while (signalReadData.isEmpty());
								txtSignal.setText("Signal - Received");
								Thread.sleep(1000);
								if (Configuration.IS_BARCODE_USED.equals("YES") && (curStation.nextNewSet && !curStation.noChangeSNo)) {
									curStation.scannedPumpSNo = signalReadData;
									// choose model from the barcode if opted
									if (Configuration.IS_MODEL_FROM_BARCODE.equals("YES")) {
										String tmpModel = signalReadData.substring(Integer.valueOf(Configuration.MODEL_START_POS_IN_BARCODE)-1, Integer.valueOf(Configuration.MODEL_END_POS_IN_BARCODE));
										if (!tmpModel.equals(curStation.curPumpType)) {
											if (!detectPumpType(tmpModel, curStation)) {
												curStation.nextTestType = "";
												continue; // abort the test, prompt to scan another number
											}
										}
									}
								}
							}
						}
						
						// check for retest of pump
						int curRow = tblTestEntry.getSelectedRow();
						if ((Configuration.IS_BARCODE_USED.equals("YES") && curStation.nextNewSet && !curStation.noChangeSNo) || (!tblTestEntry.getValueAt(curRow, 5).toString().trim().isEmpty() && !curStation.reTest)) {
							
							String tmpSNo = Configuration.IS_BARCODE_USED.equals("YES") ? curStation.scannedPumpSNo : tblTestEntry.getValueAt(curRow, 5).toString().trim();
							MyResultSet res = db.executeQuery("select * from " + Configuration.READING_DETAIL + " where pump_slno = '" + tmpSNo + "'");
							
							if (res.next()) {
								int response = JOptionPane.showConfirmDialog(new JDialog(), "This pump (SNo. " + tmpSNo + ") was already tested on " + reqDtFormat.format(dbDtFormat.parse(res.getString("test_date"))) + 
											", do you want to retest it?\n'Yes' to retest\n'No' to consider as new test\n'Cancel' to abort this test\n" +
													"WARNING: Retesting the pump will overwrite old test with new readings including test date");
								if (response == 0 && !tblTestEntry.getValueAt(curRow, 0).toString().isEmpty()) {
									// initiate retest of current test
									curStation.reTest = true;
									/*
									if (Configuration.IS_BARCODE_USED.equals("YES")) {
										curStation.lastUsedPumpSNo = tmpSNo;
										curStation.lastUsedMotSNo = tmpSNo;
									} */
									/*} else {
										curStation.lastUsedPumpSNo = tmpSNo;
										curStation.lastUsedMotSNo = tblTestEntry.getValueAt(curRow, 6).toString();
										curStation.nextTestType = tblTestEntry.getValueAt(curRow, 1).toString();
										curStation.nextTestCode = tblTestEntry.getValueAt(curRow, 2).toString();
										curStation.nextRatedVolt = tblTestEntry.getValueAt(curRow, 3).toString();
									}*/
								} else if (response == 1) {
									// consider as a new test
									curRow = findNextEmptyRow();
								} else {
									// abort this test
									curStation.nextTestType = "";
									break;
								}
							}
						}
						
						/* if (Configuration.IS_BARCODE_USED.equals("YES") && !tblTestEntry.getValueAt(curRow, 5).toString().trim().isEmpty()) {
							// next suitable row in case user has kept the pointer in non empty row
							curRow = findNextEmptyRow();
						} */
						
						// set date and test number
						setCaptureDateAndSNo(curStation, curRow);
						
						// indicate the receipt of signal
						statusLabelList.get(curStation.stationIdx).setText("IN PROGRESS [" + (curStation.lastUsedTestSNo + "/" + String.valueOf(curStation.nextTestType.equals("T") ? curStation.testListType.size() : curStation.testListRoutine.size())) + "]");
						
						isExtraReadingReq = Configuration.NUMBER_OF_STATION > 1 || !station.equals(strLiveSt);
						
						// set the valve in case of automatic valve control (BEING VALVE CONTROL)
						if (Configuration.HEAD_ADJUSTMENT.equals("A")) {
							changeApplicationStatus("TEST IN PROGRESS - AUTOMATED VALVE CONTROL");
							txtSignal.setForeground(Color.white);
							
							System.out.println("Current test is " + curStation.nextTestCode);
							txtSignal.setText("Setting valve for [" + curStation.nextTestCode + "]...");
							
							curValveType = curStation.curAutoValveType.equals("H") ? "Pressure" : "Flow"; 
							curNoTests = curStation.nextTestType.equals("R") ? curStation.testListRoutine.size() : curStation.testListType.size();
							stepV = Math.max((maxV / (curNoTests * 10)), 30);
							tmpTblName = curStation.nextTestType.equals("R") ? Configuration.TESTNAMES_ROUTINE : Configuration.TESTNAMES_TYPE;
							
							if (!curStation.nextTestCode.equals("SO")) {
								// get desired value at this head
								MyResultSet tmpRes = null;
								try {
									tmpRes = db.executeQuery("select * from " + tmpTblName + " where pump_type_id='" + curStation.curPumpTypeId + "' and code='" + curStation.nextTestCode + "'");
									if (tmpRes.next() && !tmpRes.getString("auto_valve_value").isEmpty()) {
										desVal = tmpRes.getFloat("auto_valve_value");
										desTol = desVal * tmpRes.getFloat("auto_valve_tol")/100;
									} else {
										JOptionPane.showMessageDialog(new JDialog(), "The desired head/flow values for valve control are not configured yet for this pump model.\nPlease configure the same before proceeding with the test", "Error", JOptionPane.ERROR_MESSAGE);
										stopAutoCapture();
										return;
									}
								} catch (Exception e) {
									JOptionPane.showMessageDialog(new JDialog(), e.getMessage());
									break;
								}
								
								// load valve based on either percentage or fixed head/flow based
								isSlowStep = false;
								wasLearntPos = false;
								valveAtRightPlace = false;
								noOfAttempt = 4;
								
								if (curStation.curAutoValveType.equals("P")) { // 1.percentage based
									curV = (int) (maxV * desVal/100); // X% 
									// write voltage and wait for a while
									txtSignal.setText("Reaching desired head [" + dotOne.format(desVal) + "%]");
									try {
										System.out.println("Writing valve control register:" + curV);
										stationList.get(curStation.curFlowOPSt).writeParamValue("Valve Control Register", curV);
									} catch (Exception e) {
										JOptionPane.showMessageDialog(new JDialog(), "Error setting valve control register");
										continue;
									}
									Thread.sleep(curStation.nextTestType.equals("R") ? 15000 : 7000);
									valveAtRightPlace = true;
								} else { // 2.fixed head/flow
									if (tmpRes.getFloat("auto_valve_learnt_pos") > 0) { //  next point with last learnt position
										curV = (int) (tmpRes.getFloat("auto_valve_learnt_pos")/1);
										if (curStation.lastUsedTestSNo == 1) {
											// load closer, not at exact point
											if (isSOFirst) {
												curV -= (curV * 3 / 100);
											} else {
												curV += (curV * 3 / 100);
											}
										}
										isSlowStep = true;
										wasLearntPos = true;
										System.out.println("Directly loading the last learnt position:" + curV);
									} else { // next point with relative position
										if (isSOFirst) {
											curV += stepV;
										} else {
											if (curStation.lastUsedTestSNo == 1) {
												curV = maxV; // FO position
											} else { 
												curV -= stepV;
											}
										}
									}
									
									// load valve until desired value is set
									while(curV <= (maxV + 100) && curV >= (minV - 100)) {
										// write voltage and wait for a while
										try {
											System.out.println("Writing valve control register:" + curV);
											stationList.get(curStation.curFlowOPSt).writeParamValue("Valve Control Register", curV);
											// wait for a while if its first reading and less number of heads
											if (curStation.lastUsedTestSNo == 1 && wasLearntPos && curNoTests <= 3) {
												noOfAttempt=30;
											} else {
												noOfAttempt=2;
											}
										} catch (Exception e) {
											JOptionPane.showMessageDialog(new JDialog(), "Error setting valve control register");
											break;
										}
										
										// read current reading and compare with desired values (multiple repetitions)
										noOfRead = 0;
										while (noOfRead < noOfAttempt) { // 4 attempt = 1 sec / 30 attempt = 15 sec
											Thread.sleep(valveWaitTime); 
											if (isExtraReadingReq) {
												try {
													readHeadReadings(curStation);
												} catch (Exception e) {
													// ignore
												}
												try {
													readPowerReadings(curStation);
												} catch (Exception e) {
													// ignore
												}
												try {
													readSpeedReadings(curStation);
												} catch (Exception e) {
													// ignore
												}
											} // else make use of live reading
											try {
												curVal = Math.abs(Float.valueOf(curStation.devHeadData.get(curValveType)));
											} catch (Exception e) {
												JOptionPane.showMessageDialog(new JDialog(), "Capture Error: Unable to capture the value for '" + curValveType + "'\nPlease check device communication settings", "Error", JOptionPane.ERROR_MESSAGE);
												stopAutoCapture();
												return;
											}
											if (curStation.curAutoValveType.equals("H")) {
												curVal = Float.valueOf(convertHead(curVal, curStation.curHdUnit));
												curUom = curStation.curHdUnit;
											} else {
												curVal = Float.valueOf(convertDischarge(curVal, curStation.curPumpFlowUnit));
												curUom = curStation.curPumpFlowUnit;
											}
											
											System.out.println(curStation.nextTestCode + ":" + " Target " + curValveType + ":" + desVal + " Tolerance:" + desTol + " Current mV:" + curV + " Current " + curValveType + ":" + curVal + curUom);
											
											txtSignal.setText("Reaching desired " + curValveType.toLowerCase() + "[CUR:" + dotOne.format(curVal) + curUom + ", TARGET:" + dotOne.format(desVal) + curUom + "]");
											if (curVal >= (desVal - desTol) && curVal <= (desVal + desTol)) {
												valveAtRightPlace = true;
												break;
											}
											
											// slow it a bit near to the reading
											if (!isSlowStep) {
												if (isSOFirst) {
													if ((curStation.curAutoValveType.equals("H") && curVal < (desVal * 0.8)) || (curStation.curAutoValveType.equals("F") && curVal > (desVal * 0.8))) {
														isSlowStep = true;
														System.out.println("Reaching closer, hence, going to load slowly");
													}
												} else {
													if ((curStation.curAutoValveType.equals("H") && curVal > (desVal * 0.8)) || (curStation.curAutoValveType.equals("F") && curVal < (desVal * 0.8))) {
														isSlowStep = true;
														System.out.println("Reaching closer, hence, going to load slowly");
													}
												}
												if (isSlowStep) {
													break;
												}
											}
											
											noOfRead++;
										}
										if (valveAtRightPlace) {
											txtSignal.setText("Reached desired " + curValveType.toLowerCase() + ". Stabilizing flow...");
											System.out.println("Right place, breaking after waiting for a sec");
											Thread.sleep(1000);
											break;
										}
										// else go with next position
										
										if ((curStation.curAutoValveType.equals("H") && curVal > desVal) || (curStation.curAutoValveType.equals("F") && curVal < desVal)) {
											curV += isSlowStep ? slowStepV : stepV;
											System.out.println("Increasing (by " + (isSlowStep ? slowStepV : stepV) + ") voltage to " + curV);
										} else {
											curV -= isSlowStep ? slowStepV : stepV;
											System.out.println("Decreasing (by " + (isSlowStep ? slowStepV : stepV) + ") voltage to " + curV);
										}
										wasLearntPos = false; // earlier learnt position did not work in first try if any
									} // while
								} // fixed head/flow
							} else { // SO head
								// directly close the valve, wait for a while and take reading
								curV = minV;
								try {
									System.out.println("Writing valve control register to directly shut-off value:" + curV);
									stationList.get(curStation.curFlowOPSt).writeParamValue("Valve Control Register", curV);
								} catch (Exception e) {
									JOptionPane.showMessageDialog(new JDialog(), "Error setting valve control register");
									break;
								}
								// wait till flow goes to 0
								curTime = 0;
								do {
									Thread.sleep(valveWaitTime);
									// read current reading and compare with desired values
									if (isExtraReadingReq) {
										try {
											readHeadReadings(curStation);
										} catch (Exception e) {
											// ignore
										}
										try {
											readPowerReadings(curStation);
										} catch (Exception e) {
											// ignore
										}
										try {
											readSpeedReadings(curStation);
										} catch (Exception e) {
											// ignore
										}
									} // else make use of live reading
									
									try {	
										curVal = Float.valueOf(dotOne.format(Float.valueOf(curStation.devHeadData.get("Flow"))));
										curVal = Float.valueOf(convertDischarge(curVal, curStation.curPumpFlowUnit));
										curUom = curStation.curPumpFlowUnit;
									} catch (Exception e) {
										JOptionPane.showMessageDialog(new JDialog(), "Capture Error: Unable to capture the value for 'Flow'\nPlease check device communication settings", "Error", JOptionPane.ERROR_MESSAGE);
										stopAutoCapture();
										return;
									}
									
									System.out.println(curStation.nextTestCode + ":" + " Target Flow:0 Current mV:" + curV + " Current Flow:" + curVal + curUom);
									
									txtSignal.setText("Reaching desired flow " + "[CUR:" + dotOne.format(curVal) + curUom + ", TARGET:0" + curUom + "]");
									
									curTime++;
								} while (curVal > 0 && curTime <= valveSOTime);
								if (curVal <= 0) {
									Thread.sleep(3000);
								}
								valveAtRightPlace = true;
								// go ahead and capture current readings
								holdOnForValveCapture = true;
							}
							
						} // else, go ahead and just capture the readings set for manual head (END OF VALVE CONTROL)
						
						// read current readings from all the connected devices
						if (isExtraReadingReq && !holdOnForValveCapture) {
							System.out.println("Freash capture...");
							try {
								readHeadReadings(curStation);
							} catch (Exception e) {
								// ignore
							}
							try {
								readPowerReadings(curStation);
							} catch (Exception e) {
								// ignore
							}
							try {
								readSpeedReadings(curStation);
							} catch (Exception e) {
								// ignore
							}
							
						} // else - make use of last read readings as part of live reading
						
						// capture the reading
						capture(curStation, curRow);
						
						++curRow;
						addRowIfReq(curRow);
						
						if (Configuration.HEAD_ADJUSTMENT.equals("A")) {
							if (valveAtRightPlace) {
								// update the newly learnt position to speed up upcoming iterations
								db.executeUpdate("update " + tmpTblName + " set auto_valve_learnt_pos=" + curV + " where pump_type_id='" + curStation.curPumpTypeId + "' and code='" + curStation.nextTestCode + "'");
							} else {
								// tried maximum load, desired reading could not be found
								JOptionPane.showMessageDialog(new JDialog(), ("Unable to reach desired " + curValveType.toLowerCase() + " '" + dotOne.format(desVal) + curUom + "' for test '" + curStation.nextTestCode + "'") + "\nChoose OK to continue with next reading", "Warning", JOptionPane.WARNING_MESSAGE);
								logError("WARNING: Unable to reach desired " + curValveType.toLowerCase() + " " + dotOne.format(desVal) + curUom +  " for " + curStation.nextTestType + "," + curStation.nextTestCode);
								
							}
							
							// switch off the test and reset the valve to FO/SO in case end of the test
							if (curStation.lastUsedTestSNo == (curStation.nextTestType.equals("R") ? curStation.testListRoutine.size() : curStation.testListType.size())) {
								try {
									changeApplicationStatus("TEST COMPLETED");
									// switch off the test
									curStation.writeParamValue("Test Off Coil", true);
									// reset the coil
									Thread.sleep(1000);
									curStation.writeParamValue("Test Off Coil", false);
									
									// reset the valve
									curV = isSOFirst ? minV : maxV; // SO or FO
									stationList.get(curStation.curFlowOPSt).writeParamValue("Valve Control Register", curV);
									
									Integer curSec = 1;
									while (curSec <= valvePerfTime) {
										txtSignal.setText("Resetting valve [" + curSec + "/" + valvePerfTime + " sec]");
										Thread.sleep(1000);
										curSec++;
									}
								} catch (Exception e) {
									JOptionPane.showMessageDialog(new JDialog(), "Error resetting valve control register");
									e.printStackTrace();
									continue;
								}
							}
						}
						
					} while (true);
					
				} catch (InterruptedException e) {
					setTestStatus("IDLE", curStation, 0, "");
					break;
				} catch (Exception e) {
					// just continue after few seconds
					try {
						e.printStackTrace();
						Thread.sleep(1000);
						break;
					} catch (Exception se) {
						JOptionPane.showMessageDialog(new JDialog(), "Error in Auto Capture: " + se.getMessage() + "\nChoose OK to continue", "Error", JOptionPane.ERROR_MESSAGE );
					}
					setTestStatus("IDLE", curStation, 0, "");
					e.printStackTrace();
				}
			}
		}	
	};
	
	// device live reading thread
	Runnable liveReading = new Runnable() {
		public void run() {
			logError("Live Reading:Turned On", true);
			
			isLiveReadingOn = true;

			// read current readings from all the connected devices periodically
			try {
				int ms=500;
				Float pres = 0F;
				Float flw = 0F;
				Float vac = 0F;
				HashMap<String, String> tmpReadData = new HashMap<String, String>();
				Station curStation = null;
				String errParam = "";
				while (!stopLiveRequested) {
					if (holdOnForValveCapture) {
						continue; // wait for a moment till auto valve captures
					}
					Thread.sleep(ms);
					
					curStation = stationList.get(strLiveSt);
					
			
					// update the display in software and/or connected HMIs if remote station mode
					/* if (Configuration.MULTI_STATION_MODE.equals("YES")) {
						// read current reading for all stations
						for(int i=1; i<=stationList.size(); i++) {
							readCurrentReadings("Station"+i);
							curStation = stationList.get("Station"+i);
							tmpReadData = curStation.devReadData;
							System.out.println("Live reading of station " + ("Station"+i) + " is " + curStation.devReadData.size());
							// set readings into HMI of this station
							try {
								pres = Float.valueOf(tmpReadData.get("Pressure " + curStation.curPrOP));
								vac = Float.valueOf(tmpReadData.get("Vacuum "+ curStation.curPrOP));
								flw = Float.valueOf(tmpReadData.get("Flow "+ curStation.curFlowOP));
								vac = vac < 0 ? vac * -1 : vac;
								curStation.writeParamValue("Set Pressure", convertHeadForHMI(pres, stationList.get(strLiveSt).curHdUnit));
								curStation.writeParamValue("Set Vacuum", convertHeadForHMI(vac, stationList.get(strLiveSt).curHdUnit));
								curStation.writeParamValue("Set Flow", convertDischargeForHMI(flw, stationList.get(strLiveSt).curPumpFlowUnit));
							} catch (Exception e) {
								//  e.printStackTrace();
								logError("Error setting pump params to remote HMI");
							}
							
							try {
								threePhStr = curStation.curPumpPhase.equals("Three") ? " 3 Ph" : "";
								System.out.println("Voltage is " + (int) (Float.valueOf(tmpReadData.get("Voltage" + threePhStr)) * 10));
								curStation.writeParamValue("Set Voltage", (int) (Float.valueOf(tmpReadData.get("Voltage" + threePhStr)) * 10));
								curStation.writeParamValue("Set Current", (int) (Float.valueOf(tmpReadData.get("Current" + threePhStr)) * 100));
								curStation.writeParamValue("Set Power", (int) (Float.valueOf(tmpReadData.get("Power" + threePhStr)) * 1000));
								curStation.writeParamValue("Set Frequency", (int) (Float.valueOf(tmpReadData.get("Frequency" + threePhStr)) * 100));
							} catch (Exception e) {
								//  e.printStackTrace();
								logError("Error setting power params to remote HMI");
							}
							
							try {
								curStation.writeParamValue("Set Speed", Integer.valueOf(tmpReadData.get("Speed")));
							} catch (Exception e) {
								//  e.printStackTrace();
								logError("Error setting speed to remote HMI");
							}
						}
					} else {
						// read current reading for chosen station
						readCurrentReadings(strLiveSt);
						threePhStr = stationList.get(strLiveSt).curPumpPhase.equals("Three") ? " 3 Ph" : "";
					} */
					
					/* uncomment this if above is commented */
					pres = 0F;
					flw = 0F;
					vac = 0F;
					try {
						// 1. pressure, vac, flow
						readHeadReadings(curStation);
						pres = Math.abs(Float.valueOf(curStation.devHeadData.get("Pressure")));
						vac = Math.abs(Float.valueOf(curStation.devHeadData.get("Vacuum")));
						flw = Float.valueOf(curStation.devHeadData.get("Flow"));
						lblLiveDHead.setText(convertHead(pres, curStation.curHdUnit));
						lblLiveSHead.setText(convertHead(vac, curStation.curHdUnit));
						lblLiveFlow.setText(convertDischarge(flw, curStation.curPumpFlowUnit));
					} catch (Exception e) {
						lblLiveDHead.setText("-");
						lblLiveSHead.setText("-");
						lblLiveFlow.setText("-");
					}
					
					try {
						// 2. power
						readPowerReadings(curStation);
						lblLiveAmps.setText(curStation.devPowerData.get("Current"));
						lblLiveVolt.setText(curStation.devPowerData.get("Voltage"));
						lblLiveWatt.setText(curStation.devPowerData.get("Power"));
						lblLiveFreq.setText(curStation.devPowerData.get("Frequency"));
					} catch (Exception e) {
						lblLiveVolt.setText("-");
						lblLiveAmps.setText("-");
						lblLiveWatt.setText("-");
						lblLiveFreq.setText("-");
					}
					
					try {
						// 3. speed
						readSpeedReadings(curStation);
						lblLiveSpeed.setText(curStation.devSpeedData.get("Speed"));
					} catch (Exception e) {
						lblLiveSpeed.setText("-");
					}
				} // while
			} catch (InterruptedException ie) {
				logError("Live Reading:Interrupted", true);
			} catch (Exception e) {
				logError("Error:Live Reading:" + e.getMessage());
				e.printStackTrace();
				// restart
				startLiveReading();
			}
			
			logError("Live Reading:Turned Off", true);
			
			lblLiveSHead.setText("-");
			lblLiveDHead.setText("-");
			lblLiveFlow.setText("-");
			lblLiveSpeed.setText("-");
			lblLiveVolt.setText("-");
			lblLiveAmps.setText("-");
			lblLiveWatt.setText("-");
			lblLiveFreq.setText("-");
			isLiveReadingOn = false;
		}
	};
	
	// class to hold 4 points of a line (for result preview)
	private class CustLine {
		private Double x1=0.0D;
		private Double y1=0.0D;
		private Double x2=0.0D;
		private Double y2=0.0D;
		
		CustLine() {
			
		}
		CustLine(Double x1, Double y1, Double x2, Double y2) {
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
		}
		public Double getX1() {
			return x1;
		}
		public Double getY1() {
			return y1;
		}
		public Double getX2() {
			return x2;
		}
		public Double getY2() {
			return y2;
		}
	}
	
	// class to customize table look & feel
	class MyTableCellRender extends DefaultTableCellRenderer { 
		private Color aboveRowColor = null;
		public Component getTableCellRendererComponent(  
				JTable table, Object value, boolean isSelected, 
				boolean hasFocus, int row, int col) {
				     super.getTableCellRendererComponent(
				                      table,  value, isSelected, hasFocus, row, col);
				     if (col < 4) {
				    	 if (hasFocus) {
					    	 setBackground(Color.DARK_GRAY); 
					     } else {
					    	 setBackground(Color.GRAY);
					     }
				     } else if (hasFocus) {
				    	 setBackground(selGridColor);
				     } else if (!isSelected) {
				    	 if (!table.getModel().getValueAt(row, 1).toString().isEmpty()) {
				    		 // set alternate colors between two type tests
				    		 if (row > 0) {
				    			 aboveRowColor = getTableCellRendererComponent(table,  value, isSelected, hasFocus, row - 1, 5).getBackground();
				    			 if (table.getModel().getValueAt(row, 5).toString().equals(table.getModel().getValueAt(row - 1, 5).toString())) {
				    				 setBackground(aboveRowColor); 
				    			 } else {
				    				 setBackground(aboveRowColor.equals(typeTestColor1) ? typeTestColor2 : typeTestColor1);
				    			 }
				    		 } else {
				    			 setBackground(typeTestColor1);
				    		 }
				    		 
				    	 } else {
				    		 setBackground(Color.WHITE);
				    	 }
					 }
			return this;
			}
	}
	
	class SelectionListener implements ListSelectionListener {
		  JTable table = null;
		  int r = 0;
		  int c = 0;
		  MyResultSet res = null;
		  
		  SelectionListener(JTable table) {
		    this.table = table;
		  }
		  public void valueChanged(ListSelectionEvent le) {
			  String curSNo = table.getValueAt(table.getSelectedRow(), 5).toString();
			  String curTest = table.getValueAt(table.getSelectedRow(), 1).toString();
			  
			  // change the pump sno label in addon tests & load the value
			  if (Configuration.IS_SP_TEST_ON.equals("1")) {
				  txtSPSec.setText("");
				  txtSPSuc.setText(Configuration.CUR_SUC_LIFT.toString());
				  if (!curSNo.isEmpty()) {
					  try { 
						  lblOpTestSNo.setText("Pump SNo. " + curSNo);
						  
						  String curRtV = table.getValueAt(table.getSelectedRow(), 3).toString();
						  String station = "Station" + table.getValueAt(table.getSelectedRow(), 0).toString();
						  MyResultSet  res = db.executeQuery("select * from " + Configuration.OPTIONAL_TEST + " where pump_type_id='" + stationList.get(station).curPumpTypeId + "' and pump_slno='" + curSNo + "' and rated_volt='" + curRtV +"'");
						  while (res.next()) {
							  if (res.getString("test_code").equals("SP")) {
								  txtSPSec.setText(res.getString("val_1"));
								  txtSPSuc.setText(res.getString("val_2"));
							  }
						  }
					  } catch(SQLException se) {
						  logError("Error loading the optional test for " + curSNo + ":" + se.getMessage());
					  }
				  }
			  }
			  
			  // refresh result 
			  if ((lastGraphSNo.equals(curSNo) && curTest.equals("T")) || curSNo.isEmpty()) {
				  return;
			  } else {
				  if (tblTestEntry.getValueAt(tblTestEntry.getSelectedRow(), tblTestEntry.getColumnCount()-1).toString().trim().equals("LEAKAGE")) {
						lblResBulb.setIcon(imgFailBulb);
						lblResBulb.setText("FAIL");
						lblResH.setBackground(Color.gray);
						lblResF.setBackground(Color.gray);
						lblResC.setBackground(Color.gray);
						lblResP.setBackground(Color.gray);
						stationLabelList.get(Integer.valueOf(tblTestEntry.getValueAt(table.getSelectedRow(), 0).toString())-1).setBackground(Color.red);
				  } else if (!tblTestEntry.getValueAt(tblTestEntry.getSelectedRow(), 0).toString().trim().isEmpty()) {
					  refreshResultPreview("Station" + tblTestEntry.getValueAt(table.getSelectedRow(), 0), curSNo, curTest, tblTestEntry.getValueAt(table.getSelectedRow(), 3).toString());
				  }
			  }
		  }
	}
	
	// variables
	private DecimalFormat dotOne = new DecimalFormat("#0.0");
	private DecimalFormat dotTwo = new DecimalFormat("#0.00");
	private DecimalFormat dotThree = new DecimalFormat("#0.000");
	private DecimalFormat dotZero = new DecimalFormat("#");
	private DecimalFormat fourDigit = new DecimalFormat("0000");
	//private Integer defCurRow = 0;
	private boolean saved = true;
	private Color signalColor = new Color(51,204,51);
	private Color selGridColor = new Color(0,51,153);
	private Color typeTestColor1 = new Color(204,255,255);
	private Color typeTestColor2 = new Color(204,204,255);
	
	private Thread threadAutoCapture = null;
	private Thread threadStatus = null;
	private ArrayList<Thread> threadAutoCaptureList = new ArrayList<Thread>();
	private Boolean isAutoCaptureOn = false;
	private Thread threadLiveRead = null;
	public boolean isLiveReadingOn = false; 
	private boolean stopLiveRequested = false;
	private boolean stopAutoCaptureRequested = false;
	private boolean holdOnForValveCapture = false;
	
	private DefaultListModel errLogModel = null; 
	private SimpleDateFormat reqDtFormat = new SimpleDateFormat("dd-MM-yyyy");
	private SimpleDateFormat reqTmFormat = new SimpleDateFormat("HH:mm:ss");
	private SimpleDateFormat dbDtFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	private Database db = null;
	
	private Integer totLastRoutineTest = 0;
	
	String lastManualTest = "";
	Boolean reTest = false;
	


	// for live reading (TBD: need to optimize)
	HashMap<String, String> liveReadData = new HashMap<String, String>();
	
	java.util.Date logTime = null;
	
	private Color clrHead = new Color(0,102,204);
	private Color clrMI = new Color(225,0,113);
	private Color clrE = new Color(128,0,0);
	private Color clrCur = new Color(0,153,0);
	private Color clrOPOn = new Color(255,255,0);
	Color clrTestPass = new Color(51, 204, 0);
	String defDisUnit = "";
	String defHdUnit = "";
	String lastGraphSNo = "";
	String lastGraphRV = "";
	ImageIcon imgFailBulb = new ImageIcon(getClass().getResource("/img/fail.PNG"));
	ImageIcon imgPassBulb = new ImageIcon(getClass().getResource("/img/pass.PNG"));
	ImageIcon imgIdleBulb = new ImageIcon(getClass().getResource("/img/idle.PNG"));
	// ImageIcon imgProgBulb = new ImageIcon(getClass().getResource("/img/progress.PNG"));
	JLabel lblNA = new JLabel("Graph not applicable for routine test");
	Calculate perfCalc = new Calculate();
	Boolean routAutoTestFailed = false;
	Boolean isFlowRange = false;
	Boolean opWasMadeOne = false;
	Boolean dataReadError = false;
	Boolean devInitialized = false;
	Boolean isSOFirst = false;
	Integer vacColOffset = 1;
	
	HashMap<String, Station> stationList = new HashMap<String, Station>();
	int curLiveSt = 0;
	String strLiveSt = "Station1";
	int stationIdx = 0;
	ArrayList<JLabel> stationLabelList = new ArrayList<JLabel>();
	ArrayList<JLabel> statusLabelList = new ArrayList<JLabel>();
	ArrayList<JToggleButton> tglLRList = new ArrayList<JToggleButton>();
	
	// perf tolerance
	private Float tolDisMin = 0F;
	private Float tolDisMax = 0F;
	private Float tolHeadMin = 0F;
	private Float tolHeadMax = 0F;
	private Float tolPowMin = 0F;
	private Float tolPowMax = 0F;
	private Float tolCurMin = 0F;
	private Float tolCurMax = 0F;
	private Float tolEffMin = 0F;
	private Float tolEffMax = 0F;
	
	// for valve control
	Integer minV = 200;
	Integer maxV = 1000;
	Integer stepV = 50;
	Integer slowStepV = 3;
	Integer valvePerfTime = 20;
	Integer valveSOTime = 20;
	
	// CUSTOM CODE - END
}
