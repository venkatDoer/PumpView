/*
 * Created by JFormDesigner on Thu Oct 04 11:41:41 IST 2012
 */

package doer.pv;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import javax.swing.*;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import doer.io.Database;
import doer.io.MyResultSet;
import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;
import jssc.SerialPortList;

/**
 * @author VENKATESAN SELVARAJ
 */
public class DeviceSettings extends JDialog {
	private String protocol = null; 

	public DeviceSettings(Frame owner) {
		super(owner);
		frmMain = (PumpView) owner;
		initComponents();
		customInit();
	}

	public DeviceSettings(Dialog owner) {
		super(owner);
		initComponents();
	}


	private void cmdSaveActionPerformed() {
		
		int res = JOptionPane.showConfirmDialog(this, "Save the changes?");
		if ( res != 0 ) {
			return;
		}
		this.setCursor(waitCursor);
		
		// save calibration details
		try {
			String calDt = "";
			String dueDt = "";
			String idList = "";
			
			for (int i=0; i<tblCal.getRowCount(); i++) {
				if (tblCal.getValueAt(i, 0) != null) {
					if (!tblCal.getValueAt(i, 0).toString().trim().isEmpty()) {
						idList += tblCal.getValueAt(i, 0).toString().trim() + ",";
						try {
							if (!tblCal.getValueAt(i, 5).toString().isEmpty()) {
								calDt = dbDtFormat.format(reqDtFormat.parse(tblCal.getValueAt(i, 5).toString()));
							}
							if (!tblCal.getValueAt(i, 6).toString().isEmpty()) {
								dueDt = dbDtFormat.format(reqDtFormat.parse(tblCal.getValueAt(i, 6).toString()));
							}
							
							db.executeUpdate("insert into CALIBRATION values('" + Configuration.LINE_NAME + "', '" + tblCal.getValueAt(i, 0).toString() + "', '" + tblCal.getValueAt(i, 1).toString() + "', '" + tblCal.getValueAt(i, 2).toString() + "', '" + 
									tblCal.getValueAt(i, 3).toString() + "', '" + tblCal.getValueAt(i, 4).toString() + "', '" + 
									calDt + "', '" + dueDt + "', '" + tblCal.getValueAt(i, 7).toString() + "', " + (tblCal.getValueAt(i, 8).toString().equals("true") ? 1 : 0) + ")");
							
						} catch (SQLException se) {
							if (se.getMessage().contains("unique")) {
								db.executeUpdate("update CALIBRATION set ins_name='" + tblCal.getValueAt(i, 1).toString() + "', make='" + tblCal.getValueAt(i, 2).toString() + "', model='" + tblCal.getValueAt(i, 3).toString() + "', sno='" + 
										tblCal.getValueAt(i, 4).toString() + "', cal_date='" +  
										calDt + "', due_date='" + dueDt + "', agency='" + tblCal.getValueAt(i, 7).toString() + "', reminder=" + (tblCal.getValueAt(i, 8).toString().equals("true") ? 1 : 0) + " where line='" + Configuration.LINE_NAME + "' and ins_id='" + tblCal.getValueAt(i, 0).toString() + "'");
								System.out.println("update CALIBRATION set ins_name='" + tblCal.getValueAt(i, 1).toString() + "', make='" + tblCal.getValueAt(i, 2).toString() + "', model='" + tblCal.getValueAt(i, 3).toString() + "', sno='" + 
										tblCal.getValueAt(i, 4).toString() + "', cal_date='" +  
										calDt + "', due_date='" + dueDt + "', agency='" + tblCal.getValueAt(i, 7).toString() + "', reminder=" + (tblCal.getValueAt(i, 8).toString().equals("true") ? 1 : 0) + " where line='" + Configuration.LINE_NAME + "' and ins_id='" + tblCal.getValueAt(i, 0).toString() + "'");
							} else {
								throw se;
							}
						} catch (ParseException e) {
							if (calDt.isEmpty()) {
								JOptionPane.showMessageDialog(this, "Invalid Calibrated Date for Instrument ID:" + tblCal.getValueAt(i, 0).toString() + "\nExpected date format:DD-MM-YYYY");
							} else {
								JOptionPane.showMessageDialog(this, "Invalid Calibration Due Date for Instrument ID:" + tblCal.getValueAt(i, 0).toString() + "\nExpected date format:DD-MM-YYYY");
							}
							return;
						}
						calDt = "";
						dueDt = "";
					}
				}
			}
			// clean up deleted records if any
			if (!idList.isEmpty()) {
				idList = idList.substring(0, idList.length()-1);
			}
			db.executeUpdate("delete from CALIBRATION where line='" + Configuration.LINE_NAME + "' and ins_id not in (" + idList + ")");
		} catch (Exception se) {
			this.setCursor(defCursor);
			JOptionPane.showMessageDialog(this, "Error saving calibration details:" + se.getMessage());
			return;
		}
		this.setCursor(defCursor);
		JOptionPane.showMessageDialog(this, "Changes are saved successfully!"); 
	}

	private void cmdExitActionPerformed() {
		thisWindowClosing();
		this.setVisible(false);
	}

	private void thisWindowClosing() {
	}

	private void jtrStationValueChanged() {
		if (jtrStation.getSelectionPath() != null ) {
			if (jtrStation.getSelectionPath().getPathCount() == 3) { // device level
				String stationId = jtrStation.getSelectionPath().getPathComponent(1).toString();
				String devNm = jtrStation.getSelectionPath().getLastPathComponent().toString();
				pnlDev.setBorder(new TitledBorder(null, "Device Communication Settings [STATION:" + stationId + "  DEVICE:" + devNm + "]", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
						new Font("Arial", Font.BOLD, 12), Color.blue));
				
				// load device details
				try {
					MyResultSet res = db.executeQuery("select * from DEVICE where line='" + Configuration.LINE_NAME + "' and station_no='" + stationId +"' and dev_name='" + devNm + "'" );
					
					if (res.next()) {
						protocol = res.getString("comm_protocol");
						txtId.setText(res.getString("dev_adr"));
						cmbPort.setSelectedIndex(0);
						cmbPort.setSelectedItem(res.getString("dev_port"));
						
						if (cmbPort.getSelectedIndex() <= 0 && !res.getString("dev_port").isEmpty()) {
							Boolean exist = false;
							for(int j=0; j<cmbPort.getItemCount(); j++) {
								if (cmbPort.getItemAt(j).equals(res.getString("dev_port") + " (Invalid)")) {
									exist = true;
									break;
								}
							}
							if (!exist) {
								cmbPort.addItem(res.getString("dev_port") + " (Invalid)");
							}
							cmbPort.setSelectedItem(res.getString("dev_port") + " (Invalid)");
						}
						chkCommon.setSelected(res.getBoolean("is_common_port"));
						
						if (res.getString("dev_type").equals("S")) {
							cmbDevTye.setSelectedItem("Serial");
						} else if (res.getString("dev_type").equals("HID")) {
								cmbDevTye.setSelectedItem("HID");
						} else {
							cmbDevTye.setSelectedItem("Modbus");
						}
						cmbBaud.setSelectedItem(res.getString("baud_rt"));
						cmbDB.setSelectedItem(res.getString("data_bits"));
						cmbSB.setSelectedItem(res.getString("stop_bits"));
						cmbParity.setSelectedIndex(res.getInt("parity"));
						cmbWC.setSelectedIndex(res.getInt("wc"));
						cmbEnd.setSelectedItem(res.getString("endianness"));
						txtIpCmd.setText(res.getString("ip_cmd"));
						chkNotApp.setSelected(!res.getBoolean("is_in_use"));
						txtIPAdd.setText(res.getString("ip_address"));
						txtIPPort.setText(res.getString("ip_port"));

						chkNotAppActionPerformed();
						if (protocol.equals("RTU")) {
							rtu();
						}else {
							tcpip();
						}
					}
					
					
					// load parameters belong to this device
					DefaultTableModel defMod = (DefaultTableModel) tblDevCfg.getModel();
					while (tblDevCfg.getRowCount() > 0) {
						defMod.removeRow(0);
					}
					
					res = db.executeQuery("select * from DEVICE_PARAM where dev_id=(select dev_id from DEVICE where line='" + Configuration.LINE_NAME + "' and station_no='" + stationId +"' and dev_name='" + devNm + "') order by rowid" );
					int i=0;
					while (res.next()) {
						defMod.addRow(new String[] {"","","","","",""});
						tblDevCfg.setValueAt(res.getString("param_name"), i, 0);
						tblDevCfg.setValueAt(res.getString("conv_factor"), i, 1);
						tblDevCfg.setValueAt(res.getString("format_text"), i, 2);
						tblDevCfg.setValueAt(res.getString("param_adr"), i, 3);
						tblDevCfg.setValueAt(res.getString("reg_type"), i, 4);
						tblDevCfg.setValueAt(res.getString("remark"), i, 5);
						++i;
					}
				} catch (Exception se) {
					JOptionPane.showMessageDialog(this, "Error loading device config:" + se.getMessage());
				}
			}
		}
	}

	private void cmdAddStationActionPerformed() {
		// add new station
		int res = JOptionPane.showConfirmDialog(this, "Are you sure you want to add new station with default devices?");
		if (res == 0) {
			this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			DefaultTreeModel trModel = (DefaultTreeModel) jtrStation.getModel();
			DefaultMutableTreeNode trRt = (DefaultMutableTreeNode) trModel.getRoot();
			if (trRt.getChildCount() > 0) {
				frmMain.checkDevSet("Station" + (Integer.valueOf(trRt.getLastChild().toString().substring(trRt.getLastChild().toString().length()-1))+1));
			} else {
				frmMain.checkDevSet("Station1");
			}
			
			// reload tree
			loadStationList();
			scrlTree.setViewportView(jtrStation);
			
			refreshMainForm();
			
			this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}

	private void cmdDelStationActionPerformed() {
		// delete selected station
		if (jtrStation.getSelectionPath() != null ) {
			if (jtrStation.getSelectionPath().getPathCount() == 2) {
				String selOp = jtrStation.getSelectionPath().getPathComponent(1).toString();
				if (selOp.equals("Station1")) {
					JOptionPane.showMessageDialog(this, "Sorry, it is not possible to delete default station 'Station1'");
					return;
				}
				int res = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete station '" + selOp +"' and corresponding devices?");
				if (res == 0) {
					int resReset = -1;
					if (Configuration.LAST_USED_STATION.equals(selOp)) {
						resReset = JOptionPane.showConfirmDialog(this, "WARNING: You are deleting the station which is currently chosen for performing your test, this action will reset to 'Station1'.\nAre your sure, you want to continue?");
					}
					if (resReset == -1 || resReset == 0) {
						this.setCursor(waitCursor);
						try {
							db.executeUpdate("delete from DEVICE_PARAM where dev_id in (select dev_id from DEVICE where line='" + Configuration.LINE_NAME + "' and station_no = '" + selOp + "')");
							db.executeUpdate("delete from DEVICE where line='" + Configuration.LINE_NAME + "' and station_no = '" + selOp +"'");
							db.executeUpdate("delete from " + Configuration.OUTPUT + " where line='" + Configuration.LINE_NAME + "' and station_no = '" + selOp +"'");
							// reload tree
							loadStationList();
							scrlTree.setViewportView(jtrStation);
							
							// reset station to default station in case user is deleting any station currently in use
							if(resReset == 0) {
								Configuration.LAST_USED_STATION = "Station1";
								Configuration.saveConfigValues("LAST_USED_STATION");
								frmMain.setLRStationCaption();
							}
							
							refreshMainForm();
							
						} catch (SQLException se) {
							JOptionPane.showMessageDialog(this, "Error deleting station:" + se.getMessage());
						}
						this.setCursor(defCursor);
					} // if resReset ==
				} // if res == 
			} else {
				JOptionPane.showMessageDialog(this, "Please select a station to delete");
			}
		} else {
			JOptionPane.showMessageDialog(this, "Please select a station to delete");
		}
	}

	private void cmdSaveDevActionPerformed() {
		// save the changes
		if (rtuRadio.isSelected()||ipRadio.isSelected()) {
			if (jtrStation.getSelectionPath() != null) {
				this.setCursor(waitCursor);
				String stationId = jtrStation.getSelectionPath().getPathComponent(1).toString();
				String devNm = jtrStation.getSelectionPath().getLastPathComponent().toString();
				String stFilter =  chkCommon.isSelected() ? "" : " and station_no='" + stationId + "'";
				ArrayList<String> paramList = new ArrayList<String>();
				if (rtuRadio.isSelected()) {
					//protocol
					protocol = "RTU";
				}else {
					protocol = "TCP";
				}
				try {
					// master
					// 1. dev id unique for this device
					db.executeUpdate("update DEVICE set dev_adr='" + txtId.getText().trim() + 
						"' where dev_name='" + devNm + "' and line='" + Configuration.LINE_NAME + "' and station_no='" + stationId + "'");
					// 2. port is common for all stations of this device or unique for each station
					db.executeUpdate("update DEVICE set dev_port='" + (cmbPort.getSelectedItem().toString().contains("(Invalid)")?cmbPort.getSelectedItem().toString().substring(0, cmbPort.getSelectedItem().toString().indexOf("(Invalid)")-1): cmbPort.getSelectedItem().toString()) + 
							"', dev_type='" + (cmbDevTye.getSelectedIndex()==0?"S":cmbDevTye.getSelectedIndex()==1?"M":"HID") + "', baud_rt=" + cmbBaud.getSelectedItem().toString() + ", data_bits=" + cmbDB.getSelectedItem().toString() + ", stop_bits=" + cmbSB.getSelectedItem().toString() + ", parity=" + cmbParity.getSelectedIndex() + ", wc=" + cmbWC.getSelectedIndex() + ", endianness='" + cmbEnd.getSelectedItem().toString()  + 
							"', fc=0, ip_cmd = '" + txtIpCmd.getText().trim() + "', is_in_use = '" + (chkNotApp.isSelected() ? "false" : "true") + "', is_common_port = '" + (chkCommon.isSelected() ? "true" : "false") + "',comm_protocol = '" + protocol + "',ip_address = '" + txtIPAdd.getText().trim() + "',ip_port = '" + txtIPPort.getText().trim() +
							"' where dev_name='" + devNm + "' and line='" + Configuration.LINE_NAME + "'" + stFilter);
					// detail
					for(int i=0; i < tblDevCfg.getRowCount(); i++) {
						if (!tblDevCfg.getValueAt(i, 0).toString().trim().isEmpty()) {
							try {
								paramList.add(tblDevCfg.getValueAt(i, 0).toString().trim());
								db.executeUpdate("insert into DEVICE_PARAM(dev_id, param_name, param_adr, conv_factor, format_text, reg_type, remark) values((select dev_id from DEVICE where dev_name='" + devNm + "' and line='" + Configuration.LINE_NAME + "' and station_no='" + stationId + "'),'" + tblDevCfg.getValueAt(i, 0).toString().trim() + "','" + 
										tblDevCfg.getValueAt(i, 3).toString().trim() + "','" + tblDevCfg.getValueAt(i, 1).toString().trim() + "','" + tblDevCfg.getValueAt(i, 2).toString().trim() + "','" + tblDevCfg.getValueAt(i, 4).toString().trim() + "','" + tblDevCfg.getValueAt(i, 5).toString().trim() +"')");
							} catch (SQLException se) {
								if (se.getMessage().contains("not unique")) {
									db.executeUpdate("update DEVICE_PARAM set param_adr='" + tblDevCfg.getValueAt(i, 3).toString().trim() + "', conv_factor='" + tblDevCfg.getValueAt(i, 1).toString().trim() + "', format_text='" + tblDevCfg.getValueAt(i, 2).toString().trim()  + "', reg_type='" + tblDevCfg.getValueAt(i, 4).toString().trim() + "', remark='" + tblDevCfg.getValueAt(i, 5).toString().trim() + 
											"' where param_name='" + tblDevCfg.getValueAt(i, 0).toString().trim() + 
											"' and dev_id=(select dev_id from DEVICE where dev_name='" + devNm + "' and line='" + Configuration.LINE_NAME + "' and station_no='" + stationId + "')");
									
								} else {
									throw (se);
								}
							}
						}
					}
					// clean-up deleted params
					db.executeUpdate("delete from DEVICE_PARAM where param_name not in (" + paramList.toString().replace("[", "'").replace("]", "'").replace(", ", "','") + ")" + 
							" and dev_id=(select dev_id from DEVICE where dev_name='" + devNm + "' and line='" + Configuration.LINE_NAME + "' and station_no='" + stationId + "')");
				} catch (SQLException se) {
					JOptionPane.showMessageDialog(this, "Error updating device config:" + se.getMessage());
				}
				
				refreshMainForm();
				
				
				this.setCursor(defCursor);
				JOptionPane.showMessageDialog(this, "Changes saved successfully");
			} else {
				JOptionPane.showMessageDialog(this, "No device selected to save");
			}
		}
	}
	
	private void refreshMainForm () {
		// refresh main form
		System.out.println("refreshing tests");
		frmMain.stopThreads();
		frmMain.loadStations();
		frmMain.startThreads();
		System.out.println("done refreshing tests");
	}

	private void cmbDevTyeItemStateChanged() {
		if (cmbDevTye.getSelectedItem().toString().equals("Serial")) {
			cmbWC.setSelectedItem("NA");
			txtId.setText("NA");
			cmbEnd.setSelectedItem("NA");
			txtIpCmd.setEnabled(true);
		} else {
			cmbWC.setSelectedItem("2");
			txtId.setText("1");
			cmbEnd.setSelectedItem("MSB First");
			txtIpCmd.setEnabled(false);
		}
	}

	private void cmdAddActionPerformed() {
		((DefaultTableModel)tblDevCfg.getModel()).addRow(new String[] {"","","","","",""});
		tblDevCfg.setRowSelectionInterval(tblDevCfg.getRowCount()-1, tblDevCfg.getRowCount()-1);
		tblDevCfg.editCellAt(tblDevCfg.getRowCount()-1, 0);
		tblDevCfg.requestFocus();
	}

	private void cmdDelActionPerformed() {
		if (tblDevCfg.getSelectedRow() >= 0) {
			((DefaultTableModel)tblDevCfg.getModel()).removeRow(tblDevCfg.getSelectedRow());
		} else {
			JOptionPane.showMessageDialog(this, "No parameter is selected to delete");
		}
		tblDevCfg.requestFocus();
	}

	private void chkNotAppActionPerformed() {
		if (chkNotApp.isSelected()) {
			tblDevCfg.setBackground(Color.lightGray);
			pnlInUse.setBackground(Color.red);
			tblDevCfg.setEnabled(false);
		} else {
			tblDevCfg.setBackground(Color.white);
			pnlInUse.setBackground(this.getBackground());
			tblDevCfg.setEnabled(true);
		}
	}

	private void cmdResetActionPerformed() {
		// warn and proceed
		if (jtrStation.getSelectionPath() != null) {
			String stationId = jtrStation.getSelectionPath().getPathComponent(1).toString();
			String devNm = jtrStation.getSelectionPath().getLastPathComponent().toString();
			if (JOptionPane.showConfirmDialog(this, "WARNING: This will reset '" + devNm + "' of '" + stationId + "' with its original settings.\nAre your sure, you want to continue?","Reset Device?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == 0) {
				try {
					frmMain.setDevSettings(stationId, devNm);
					jtrStationValueChanged();
					JOptionPane.showMessageDialog(this, "Device settings were reset successfully");
				} catch (SQLException se) {
					se.printStackTrace();
					JOptionPane.showMessageDialog(this, "Error resetting device settings:" + se.getMessage());
				}
			}
		} else {
			JOptionPane.showMessageDialog(this, "No device selected to reset");
		}
	}

	private void cmbWCActionPerformed() {
		if (cmbWC.getSelectedItem().toString().equals("2")) {
			cmbEnd.setSelectedItem("MSB First");
		} else {
			cmbEnd.setSelectedItem("NA");	
		}
	}

	private void rtu(){
		rtuRadio.setSelected(true);
		if(ipRadio.isSelected()) 
		{
			ipRadio.setSelected(false);
			
		}
		txtIPAdd.setEnabled(false);		
		txtIPPort.setEnabled(false);
		
		cmbPort.setEnabled(true);
		cmbBaud.setEnabled(true);
		cmbDevTye.setEnabled(true);
		
		cmbParity.setEnabled(true);
		cmbDB.setEnabled(true);
		cmbSB.setEnabled(true);
	}
	
	private void tcpip() {
		ipRadio.setSelected(true);
		if(rtuRadio.isSelected()) 
		{
			rtuRadio.setSelected(false);
			
		}
		txtIPAdd.setEnabled(true);		
		txtIPPort.setEnabled(true);
		
		cmbPort.setEnabled(false);
		cmbBaud.setEnabled(false);
		cmbDevTye.setEnabled(false);
		txtIpCmd.setEnabled(false);
		cmbParity.setEnabled(false);
		cmbDB.setEnabled(false);
		cmbSB.setEnabled(false);
	}
	
	private void ipRadio(ActionEvent e) {
		// TODO add your code here
		tcpip();
	}

	private void rtuRadio(ActionEvent e) {
		// TODO add your code here
		rtu();
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		pnlHead = new JPanel();
		panel4 = new JPanel();
		scrlTree = new JScrollPane();
		jtrStation = new JTree();
		cmdAddStation = new JButton();
		cmdDelStation = new JButton();
		pnlDev = new JPanel();
		panel6 = new JPanel();
		pnlInUse = new JPanel();
		chkNotApp = new JCheckBox();
		chkCommon = new JCheckBox();
		ipRadio = new JRadioButton();
		rtuRadio = new JRadioButton();
		label82 = new JLabel();
		cmbPort = new JComboBox();
		label10 = new JLabel();
		txtIPAdd = new JTextField();
		label11 = new JLabel();
		txtIPPort = new JTextField();
		label81 = new JLabel();
		txtId = new JTextField();
		label9 = new JLabel();
		cmbWC = new JComboBox<>();
		label23 = new JLabel();
		cmbEnd = new JComboBox<>();
		label5 = new JLabel();
		cmbBaud = new JComboBox<>();
		label83 = new JLabel();
		cmbDevTye = new JComboBox<>();
		label84 = new JLabel();
		txtIpCmd = new JTextField();
		label7 = new JLabel();
		cmbParity = new JComboBox<>();
		label6 = new JLabel();
		cmbDB = new JComboBox<>();
		label8 = new JLabel();
		cmbSB = new JComboBox<>();
		panel7 = new JPanel();
		scrlDevCfg = new JScrollPane();
		tblDevCfg = new JTable();
		cmdAdd = new JButton();
		cmdDel = new JButton();
		cmdSaveDev = new JButton();
		cmdReset = new JButton();
		panel1 = new JPanel();
		scrollPane1 = new JScrollPane();
		tblCal = new JTable();
		cmdSave = new JButton();

		//======== this ========
		setTitle("Doer PumpView: Device Settings");
		setFont(new Font("Arial", Font.PLAIN, 12));
		setModal(true);
		setResizable(false);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				thisWindowClosing();
			}
		});
		Container contentPane = getContentPane();
		contentPane.setLayout(new TableLayout(new double[][] {
			{5, TableLayout.FILL, 5},
			{5, 567, TableLayout.PREFERRED, 5}}));
		((TableLayout)contentPane.getLayout()).setHGap(5);
		((TableLayout)contentPane.getLayout()).setVGap(5);

		//======== pnlHead ========
		{
			pnlHead.setBorder(new TitledBorder(null, "Device Settings", TitledBorder.CENTER, TitledBorder.TOP,
				new Font("Arial", Font.BOLD, 14), Color.blue));
			pnlHead.setFocusable(false);
			pnlHead.setLayout(new TableLayout(new double[][] {
				{273, TableLayout.FILL},
				{540}}));
			((TableLayout)pnlHead.getLayout()).setHGap(5);
			((TableLayout)pnlHead.getLayout()).setVGap(5);

			//======== panel4 ========
			{
				panel4.setBorder(new TitledBorder(null, "Stations", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
					new Font("Arial", Font.BOLD, 12), Color.blue));
				panel4.setLayout(new TableLayout(new double[][] {
					{TableLayout.FILL, TableLayout.FILL},
					{TableLayout.FILL, 30}}));
				((TableLayout)panel4.getLayout()).setHGap(5);
				((TableLayout)panel4.getLayout()).setVGap(5);

				//======== scrlTree ========
				{

					//---- jtrStation ----
					jtrStation.setFont(new Font("Arial", Font.PLAIN, 14));
					jtrStation.addTreeSelectionListener(e -> jtrStationValueChanged());
					scrlTree.setViewportView(jtrStation);
				}
				panel4.add(scrlTree, new TableLayoutConstraints(0, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmdAddStation ----
				cmdAddStation.setText("Add Station");
				cmdAddStation.setFont(new Font("Arial", Font.PLAIN, 14));
				cmdAddStation.setToolTipText("Click this to add new station along with devices");
				cmdAddStation.setMnemonic('A');
				cmdAddStation.addActionListener(e -> cmdAddStationActionPerformed());
				panel4.add(cmdAddStation, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmdDelStation ----
				cmdDelStation.setText("Delete Station");
				cmdDelStation.setFont(new Font("Arial", Font.PLAIN, 14));
				cmdDelStation.setToolTipText("Click this to delete currently selected station");
				cmdDelStation.setMnemonic('D');
				cmdDelStation.addActionListener(e -> cmdDelStationActionPerformed());
				panel4.add(cmdDelStation, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			pnlHead.add(panel4, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//======== pnlDev ========
			{
				pnlDev.setBorder(new TitledBorder(null, "Device Communication Settings", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
					new Font("Arial", Font.BOLD, 12), Color.blue));
				pnlDev.setLayout(new TableLayout(new double[][] {
					{TableLayout.FILL, TableLayout.FILL},
					{TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED}}));
				((TableLayout)pnlDev.getLayout()).setHGap(5);
				((TableLayout)pnlDev.getLayout()).setVGap(5);

				//======== panel6 ========
				{
					panel6.setLayout(new TableLayout(new double[][] {
						{TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL},
						{TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED}}));
					((TableLayout)panel6.getLayout()).setHGap(5);
					((TableLayout)panel6.getLayout()).setVGap(5);

					//======== pnlInUse ========
					{
						pnlInUse.setBorder(LineBorder.createBlackLineBorder());
						pnlInUse.setLayout(new TableLayout(new double[][] {
							{TableLayout.PREFERRED},
							{TableLayout.PREFERRED}}));
						((TableLayout)pnlInUse.getLayout()).setHGap(5);
						((TableLayout)pnlInUse.getLayout()).setVGap(5);

						//---- chkNotApp ----
						chkNotApp.setText("Device Is Not Used");
						chkNotApp.setFont(new Font("Arial", Font.PLAIN, 12));
						chkNotApp.setOpaque(false);
						chkNotApp.addActionListener(e -> chkNotAppActionPerformed());
						pnlInUse.add(chkNotApp, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.CENTER, TableLayoutConstraints.FULL));
					}
					panel6.add(pnlInUse, new TableLayoutConstraints(0, 0, 5, 0, TableLayoutConstraints.CENTER, TableLayoutConstraints.FULL));

					//---- chkCommon ----
					chkCommon.setText("Port of this device is common for all stations");
					chkCommon.setFont(new Font("Arial", Font.PLAIN, 12));
					chkCommon.addActionListener(e -> chkNotAppActionPerformed());
					panel6.add(chkCommon, new TableLayoutConstraints(1, 1, 4, 1, TableLayoutConstraints.CENTER, TableLayoutConstraints.FULL));

					//---- ipRadio ----
					ipRadio.setText("IP Communication");
					ipRadio.setFont(new Font("Arial", Font.PLAIN, 12));
					ipRadio.addActionListener(e -> ipRadio(e));
					panel6.add(ipRadio, new TableLayoutConstraints(1, 2, 2, 2, TableLayoutConstraints.CENTER, TableLayoutConstraints.FULL));

					//---- rtuRadio ----
					rtuRadio.setText("RTU Communication");
					rtuRadio.setFont(new Font("Arial", Font.PLAIN, 12));
					rtuRadio.addActionListener(e -> rtuRadio(e));
					panel6.add(rtuRadio, new TableLayoutConstraints(3, 2, 4, 2, TableLayoutConstraints.CENTER, TableLayoutConstraints.FULL));

					//---- label82 ----
					label82.setText("Port");
					label82.setFont(new Font("Arial", Font.PLAIN, 12));
					panel6.add(label82, new TableLayoutConstraints(0, 3, 0, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

					//---- cmbPort ----
					cmbPort.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
					cmbPort.setToolTipText("Port name where this instrument is connected.");
					panel6.add(cmbPort, new TableLayoutConstraints(1, 3, 1, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label10 ----
					label10.setText("IP Address");
					label10.setFont(new Font("Arial", Font.PLAIN, 12));
					panel6.add(label10, new TableLayoutConstraints(2, 3, 2, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- txtIPAdd ----
					txtIPAdd.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
					txtIPAdd.setToolTipText("Instrument identification/address");
					panel6.add(txtIPAdd, new TableLayoutConstraints(3, 3, 3, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label11 ----
					label11.setText("IP Port");
					label11.setFont(new Font("Arial", Font.PLAIN, 12));
					panel6.add(label11, new TableLayoutConstraints(4, 3, 4, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- txtIPPort ----
					txtIPPort.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
					txtIPPort.setToolTipText("Instrument identification/address");
					txtIPPort.setText("2200");
					panel6.add(txtIPPort, new TableLayoutConstraints(5, 3, 5, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label81 ----
					label81.setText("Device Address");
					label81.setFont(new Font("Arial", Font.PLAIN, 12));
					panel6.add(label81, new TableLayoutConstraints(0, 4, 0, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

					//---- txtId ----
					txtId.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
					txtId.setToolTipText("Instrument identification/address");
					panel6.add(txtId, new TableLayoutConstraints(1, 4, 1, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label9 ----
					label9.setText("Word Count");
					label9.setFont(new Font("Arial", Font.PLAIN, 12));
					panel6.add(label9, new TableLayoutConstraints(2, 4, 2, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- cmbWC ----
					cmbWC.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
					cmbWC.setModel(new DefaultComboBoxModel<>(new String[] {
						"NA",
						"1",
						"2"
					}));
					cmbWC.setSelectedIndex(2);
					cmbWC.addActionListener(e -> cmbWCActionPerformed());
					panel6.add(cmbWC, new TableLayoutConstraints(3, 4, 3, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label23 ----
					label23.setText("Endianness");
					label23.setFont(new Font("Arial", Font.PLAIN, 12));
					panel6.add(label23, new TableLayoutConstraints(4, 4, 4, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- cmbEnd ----
					cmbEnd.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
					cmbEnd.setModel(new DefaultComboBoxModel<>(new String[] {
						"NA",
						"MSB First",
						"LSB First"
					}));
					cmbEnd.setSelectedIndex(1);
					panel6.add(cmbEnd, new TableLayoutConstraints(5, 4, 5, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label5 ----
					label5.setText("Baud Rate");
					label5.setFont(new Font("Arial", Font.PLAIN, 12));
					panel6.add(label5, new TableLayoutConstraints(0, 5, 0, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- cmbBaud ----
					cmbBaud.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
					cmbBaud.setModel(new DefaultComboBoxModel<>(new String[] {
						"110",
						"300",
						"1200",
						"2400",
						"4800",
						"9600",
						"19200",
						"38400",
						"57600",
						"115200",
						"230400",
						"460800",
						"921600"
					}));
					cmbBaud.setSelectedIndex(5);
					panel6.add(cmbBaud, new TableLayoutConstraints(1, 5, 1, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label83 ----
					label83.setText("Deivce Type");
					label83.setFont(new Font("Arial", Font.PLAIN, 12));
					panel6.add(label83, new TableLayoutConstraints(2, 5, 2, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

					//---- cmbDevTye ----
					cmbDevTye.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
					cmbDevTye.setToolTipText("Port name where this instrument is connected. This is common for all outputs of this device.");
					cmbDevTye.setModel(new DefaultComboBoxModel<>(new String[] {
						"Serial",
						"Modbus",
						"HID"
					}));
					cmbDevTye.setSelectedIndex(1);
					cmbDevTye.addItemListener(e -> cmbDevTyeItemStateChanged());
					panel6.add(cmbDevTye, new TableLayoutConstraints(3, 5, 3, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label84 ----
					label84.setText("Input Command");
					label84.setFont(new Font("Arial", Font.PLAIN, 12));
					panel6.add(label84, new TableLayoutConstraints(4, 5, 4, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

					//---- txtIpCmd ----
					txtIpCmd.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
					txtIpCmd.setToolTipText("Input command for serial device");
					panel6.add(txtIpCmd, new TableLayoutConstraints(5, 5, 5, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label7 ----
					label7.setText("Parity");
					label7.setFont(new Font("Arial", Font.PLAIN, 12));
					panel6.add(label7, new TableLayoutConstraints(0, 6, 0, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- cmbParity ----
					cmbParity.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
					cmbParity.setModel(new DefaultComboBoxModel<>(new String[] {
						"None",
						"Odd",
						"Even",
						"Mark",
						"Space"
					}));
					panel6.add(cmbParity, new TableLayoutConstraints(1, 6, 1, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label6 ----
					label6.setText("Data Bits");
					label6.setFont(new Font("Arial", Font.PLAIN, 12));
					panel6.add(label6, new TableLayoutConstraints(2, 6, 2, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- cmbDB ----
					cmbDB.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
					cmbDB.setModel(new DefaultComboBoxModel<>(new String[] {
						"5",
						"6",
						"7",
						"8"
					}));
					cmbDB.setSelectedIndex(3);
					panel6.add(cmbDB, new TableLayoutConstraints(3, 6, 3, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label8 ----
					label8.setText("Stop Bits");
					label8.setFont(new Font("Arial", Font.PLAIN, 12));
					panel6.add(label8, new TableLayoutConstraints(4, 6, 4, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- cmbSB ----
					cmbSB.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
					cmbSB.setModel(new DefaultComboBoxModel<>(new String[] {
						"1",
						"2"
					}));
					panel6.add(cmbSB, new TableLayoutConstraints(5, 6, 5, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				}
				pnlDev.add(panel6, new TableLayoutConstraints(0, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//======== panel7 ========
				{
					panel7.setLayout(new TableLayout(new double[][] {
						{TableLayout.FILL, TableLayout.FILL},
						{TableLayout.FILL, 30}}));
					((TableLayout)panel7.getLayout()).setHGap(5);
					((TableLayout)panel7.getLayout()).setVGap(5);

					//======== scrlDevCfg ========
					{
						scrlDevCfg.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
						scrlDevCfg.setBackground(Color.white);

						//---- tblDevCfg ----
						tblDevCfg.setModel(new DefaultTableModel(
							new Object[][] {
								{null, null, null, null, null, null},
								{null, null, null, null, null, null},
								{null, null, null, null, null, null},
								{null, null, null, null, null, null},
							},
							new String[] {
								"Parameter", "Conv. Formula", "Format", "Register", "Register Type", "Remark"
							}
						));
						{
							TableColumnModel cm = tblDevCfg.getColumnModel();
							cm.getColumn(0).setMinWidth(125);
							cm.getColumn(0).setMaxWidth(125);
							cm.getColumn(0).setPreferredWidth(125);
							cm.getColumn(1).setMinWidth(80);
							cm.getColumn(1).setMaxWidth(80);
							cm.getColumn(1).setPreferredWidth(80);
							cm.getColumn(2).setMinWidth(50);
							cm.getColumn(2).setMaxWidth(50);
							cm.getColumn(2).setPreferredWidth(50);
							cm.getColumn(3).setMinWidth(50);
							cm.getColumn(3).setMaxWidth(50);
							cm.getColumn(3).setPreferredWidth(50);
							cm.getColumn(4).setMinWidth(80);
							cm.getColumn(4).setMaxWidth(80);
							cm.getColumn(4).setPreferredWidth(80);
						}
						tblDevCfg.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
						tblDevCfg.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
						tblDevCfg.setBorder(null);
						tblDevCfg.setToolTipText("List of reading parameters and corresponding registers");
						scrlDevCfg.setViewportView(tblDevCfg);
					}
					panel7.add(scrlDevCfg, new TableLayoutConstraints(0, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- cmdAdd ----
					cmdAdd.setText("Add Parameter");
					cmdAdd.setFont(new Font("Arial", Font.PLAIN, 12));
					cmdAdd.setMnemonic('P');
					cmdAdd.setToolTipText("Click to add new parameter to this device");
					cmdAdd.addActionListener(e -> cmdAddActionPerformed());
					panel7.add(cmdAdd, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- cmdDel ----
					cmdDel.setText("Delete Parameter");
					cmdDel.setFont(new Font("Arial", Font.PLAIN, 12));
					cmdDel.setMnemonic('R');
					cmdDel.setToolTipText("Click to delete selected parameter");
					cmdDel.addActionListener(e -> cmdDelActionPerformed());
					panel7.add(cmdDel, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				}
				pnlDev.add(panel7, new TableLayoutConstraints(0, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmdSaveDev ----
				cmdSaveDev.setText("Save Device Settings");
				cmdSaveDev.setFont(new Font("Arial", Font.PLAIN, 14));
				cmdSaveDev.setMnemonic('S');
				cmdSaveDev.setToolTipText("Click this to save changes made for above device");
				cmdSaveDev.setMargin(new Insets(2, 5, 2, 5));
				cmdSaveDev.setIcon(new ImageIcon(getClass().getResource("/img/save.PNG")));
				cmdSaveDev.addActionListener(e -> cmdSaveDevActionPerformed());
				pnlDev.add(cmdSaveDev, new TableLayoutConstraints(0, 2, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmdReset ----
				cmdReset.setText("Reset to original settings");
				cmdReset.setFont(new Font("Arial", Font.PLAIN, 14));
				cmdReset.setToolTipText("Click to reset the device to its original settings");
				cmdReset.addActionListener(e -> cmdResetActionPerformed());
				pnlDev.add(cmdReset, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			pnlHead.add(pnlDev, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		}
		contentPane.add(pnlHead, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

		//======== panel1 ========
		{
			panel1.setBorder(new TitledBorder(null, "Calibration Record   [Date Format: DD-MM-YYYY]", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION,
				new Font("Arial", Font.BOLD, 14), Color.blue));
			panel1.setLayout(new TableLayout(new double[][] {
				{60, TableLayout.FILL, 75, 75, 60, 90, 90, 80, 60, 21},
				{85, TableLayout.PREFERRED}}));
			((TableLayout)panel1.getLayout()).setHGap(1);
			((TableLayout)panel1.getLayout()).setVGap(5);

			//======== scrollPane1 ========
			{
				scrollPane1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

				//---- tblCal ----
				tblCal.setModel(new DefaultTableModel(
					new Object[][] {
						{null, null, null, null, null, null, null, null, null},
						{null, null, null, null, null, null, null, null, null},
						{null, null, null, null, null, null, null, null, null},
						{null, null, null, null, null, null, null, null, null},
						{null, null, null, null, null, null, null, null, null},
						{null, null, null, null, null, null, null, null, null},
						{null, null, null, null, null, null, null, null, null},
					},
					new String[] {
						"ID", "Instrument Name", "Make", "Model", "SNo.", "Calib. Date", "Due Date", "Agency", "Reminder On/Off"
					}
				) {
					Class<?>[] columnTypes = new Class<?>[] {
						String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, Boolean.class
					};
					@Override
					public Class<?> getColumnClass(int columnIndex) {
						return columnTypes[columnIndex];
					}
				});
				{
					TableColumnModel cm = tblCal.getColumnModel();
					cm.getColumn(0).setResizable(false);
					cm.getColumn(0).setPreferredWidth(60);
					cm.getColumn(1).setResizable(false);
					cm.getColumn(1).setPreferredWidth(184);
					cm.getColumn(2).setResizable(false);
					cm.getColumn(2).setPreferredWidth(75);
					cm.getColumn(3).setResizable(false);
					cm.getColumn(3).setPreferredWidth(75);
					cm.getColumn(4).setResizable(false);
					cm.getColumn(4).setPreferredWidth(60);
					cm.getColumn(5).setResizable(false);
					cm.getColumn(5).setPreferredWidth(90);
					cm.getColumn(6).setResizable(false);
					cm.getColumn(6).setPreferredWidth(90);
					cm.getColumn(7).setResizable(false);
					cm.getColumn(7).setPreferredWidth(80);
					cm.getColumn(8).setResizable(false);
					cm.getColumn(8).setPreferredWidth(60);
				}
				tblCal.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
				tblCal.setToolTipText("Calibration record of all instruments of this panel");
				scrollPane1.setViewportView(tblCal);
			}
			panel1.add(scrollPane1, new TableLayoutConstraints(0, 0, 9, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdSave ----
			cmdSave.setText("Save Calibration Record");
			cmdSave.setFont(new Font("Arial", Font.PLAIN, 14));
			cmdSave.setIcon(new ImageIcon(getClass().getResource("/img/save.PNG")));
			cmdSave.setToolTipText("Click this to save changes made above for calibration record");
			cmdSave.setMnemonic('C');
			cmdSave.addActionListener(e -> cmdSaveActionPerformed());
			panel1.add(cmdSave, new TableLayoutConstraints(0, 1, 9, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		}
		contentPane.add(panel1, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel pnlHead;
	private JPanel panel4;
	private JScrollPane scrlTree;
	private JTree jtrStation;
	private JButton cmdAddStation;
	private JButton cmdDelStation;
	private JPanel pnlDev;
	private JPanel panel6;
	private JPanel pnlInUse;
	private JCheckBox chkNotApp;
	private JCheckBox chkCommon;
	private JRadioButton ipRadio;
	private JRadioButton rtuRadio;
	private JLabel label82;
	private JComboBox cmbPort;
	private JLabel label10;
	private JTextField txtIPAdd;
	private JLabel label11;
	private JTextField txtIPPort;
	private JLabel label81;
	private JTextField txtId;
	private JLabel label9;
	private JComboBox<String> cmbWC;
	private JLabel label23;
	private JComboBox<String> cmbEnd;
	private JLabel label5;
	private JComboBox<String> cmbBaud;
	private JLabel label83;
	private JComboBox<String> cmbDevTye;
	private JLabel label84;
	private JTextField txtIpCmd;
	private JLabel label7;
	private JComboBox<String> cmbParity;
	private JLabel label6;
	private JComboBox<String> cmbDB;
	private JLabel label8;
	private JComboBox<String> cmbSB;
	private JPanel panel7;
	private JScrollPane scrlDevCfg;
	private JTable tblDevCfg;
	private JButton cmdAdd;
	private JButton cmdDel;
	private JButton cmdSaveDev;
	private JButton cmdReset;
	private JPanel panel1;
	private JScrollPane scrollPane1;
	private JTable tblCal;
	private JButton cmdSave;
	// JFormDesigner - End of variables declaration  //GEN-END:variables

	// custom code begin
	
	private void customInit() {
		// set the panel header
		pnlHead.setBorder(new TitledBorder(null, "Device Settings [ASSEMBLY LINE:" + Configuration.LINE_NAME + "]", TitledBorder.CENTER, TitledBorder.TOP,
				new Font("Arial", Font.BOLD, 14), Color.blue));
		
		// load available stations
		loadStationList();
		
		// hook table renderer for highlighting due dates
		tblCal.getColumnModel().getColumn(6).setCellRenderer(new MyTableCellRender());
		
		// load list of available serial ports into port list
		cmbPort.addItem("");
		
		String[] portNames = SerialPortList.getPortNames();
		for(int i=0; i<portNames.length; i++) {
			cmbPort.addItem(portNames[i]);
		}
		
		// load calibration details
		int curRow = 0;
		try {
			// fetch and load the result
			MyResultSet res = null;
			Boolean needInsert = false;
			try {
				res = db.executeQuery("select * from CALIBRATION where line='" + Configuration.LINE_NAME + "' order by rowid");
			} catch (SQLException sqle) {
				if (sqle.getMessage().contains("no such table")) {
					// create table and insert defaults
						db.executeUpdate("create table CALIBRATION (line text, ins_id integer primary key autoincrement, ins_name text, make text, model text, sno text, cal_date date, due_date date, agency text, reminder boolean, unique (line, ins_name))");
						needInsert = true;
				} else {
					throw sqle;
				}
			}
			if (res == null || !res.isRowsAvail()) {
				needInsert = true;
			}
				
			if (needInsert) {
				db.executeUpdate("insert into CALIBRATION(line, ins_name, make, model, sno, cal_date, due_date, agency, reminder) select line, dev_name,'', '', '', '', '', '', '1' from DEVICE where line='" + Configuration.LINE_NAME + "' group by dev_name order by rowid");
				// reload
				res = db.executeQuery("select * from CALIBRATION where line ='" + Configuration.LINE_NAME + "' order by rowid");
			}
			
		
			while (res.next()) {
				tblCal.setValueAt(res.getString("ins_id"), curRow, 0);
				tblCal.setValueAt(res.getString("ins_name"), curRow, 1);
				tblCal.setValueAt(res.getString("make"), curRow, 2);
				tblCal.setValueAt(res.getString("model"), curRow, 3);
				tblCal.setValueAt(res.getString("sno"), curRow, 4);
				try {
					// default the date first
					tblCal.setValueAt("", curRow, 5);
					tblCal.setValueAt("", curRow, 6);
					// set dates from db
					tblCal.setValueAt(reqDtFormat.format(dbDtFormat.parse(res.getString("cal_date"))), curRow, 5);
					tblCal.setValueAt(reqDtFormat.format(dbDtFormat.parse(res.getString("due_date"))), curRow, 6);
				} catch (ParseException e) {
					// ignore it
				}
				tblCal.setValueAt(res.getString("agency"), curRow, 7);
				tblCal.setValueAt(res.getBoolean("reminder"), curRow, 8);
				++curRow;
			}
		} catch (Exception e) {
				JOptionPane.showMessageDialog(this, "Error reading calibration details:" + e.getMessage());
				return;
		}
		
		// fill rest of rows with empty values to avoid null pointer exception later
		for (int i=curRow; i < tblCal.getRowCount(); i++) {
			for (int j=0; j < tblCal.getColumnCount()-1; j++) {
				tblCal.setValueAt("", i, j);
			}
			tblCal.setValueAt(false, i, 8);
		}
	}
	
	private void loadStationList() {
		DefaultMutableTreeNode trRt = new DefaultMutableTreeNode("Available Stations");
		try {
			db = Database.getInstance();			
			MyResultSet res = null;
			res = db.executeQuery("select * from DEVICE where line ='" + Configuration.LINE_NAME + "' order by station_no, rowid");
			String curStation = "";
			String curDev = "";
			DefaultMutableTreeNode trStation = null;
			while (res.next()) {
				if (!curStation.equals(res.getString("station_no"))) {
					curStation = res.getString("station_no");
					trStation = new DefaultMutableTreeNode(curStation);
					trRt.add(trStation);
				}
				if (!curDev.equals(res.getString("dev_name"))) {
					curDev = res.getString("dev_name");
					trStation.add(new DefaultMutableTreeNode(curDev));
				}
			}
		} catch (SQLException se) {
			JOptionPane.showMessageDialog(this, "Error loading available stations:" + se.getMessage());
		}
		DefaultTreeModel trMod = (DefaultTreeModel) jtrStation.getModel();
		trMod.setRoot(trRt);
	}
  
	
	class MyTableCellRender extends DefaultTableCellRenderer {  
		   
		public Component getTableCellRendererComponent(  
			JTable table, Object value, boolean isSelected, 
			boolean hasFocus, int row, int col) {
			     super.getTableCellRendererComponent(
			                      table,  value, isSelected, hasFocus, row, col);
	
			     setIcon(null);
			     if (table.getModel().getValueAt(row, 6) != null && table.getModel().getValueAt(row, 8).toString().equals("true")) {
				 String dueDt =  table.getModel().getValueAt(row, 6).toString();
				 if (!dueDt.isEmpty()) {
					 // highlight due dates
					try {
						dueCal.setTime(reqDtFormat.parse(dueDt));
					} catch (ParseException e) {
						return this;
					}
					dueCal.add(Calendar.DATE, -7);
					if (cal.compareTo(dueCal) >= 0) {
						setIcon(new ImageIcon(getClass().getResource("/img/bell.PNG")));
					}
				 }
			}
			return this;
		}
	}
	
	// variable declaration
	private Database db = null;
	private SimpleDateFormat reqDtFormat = new SimpleDateFormat("dd-MM-yyyy");
	private SimpleDateFormat dbDtFormat = new SimpleDateFormat("yyyy-MM-dd");
	Calendar cal = Calendar.getInstance();
	Calendar dueCal = Calendar.getInstance();
	
	private Cursor waitCursor = new Cursor(Cursor.WAIT_CURSOR);
	private Cursor defCursor = new Cursor(Cursor.DEFAULT_CURSOR);
	
	PumpView frmMain = null;
	
	// custom code end
}
