/*
 * Created by JFormDesigner on Thu Apr 22 13:00:42 PDT 2010
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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import doer.io.Database;
import doer.io.MyResultSet;
import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

/**
 * @author venkatesan selvaraj
 */
public class PumpType extends JDialog {
	
	// CUSTOM FUNCTIONS - BEGIN
	
	public PumpType(Frame owner, String lastTypeId, String lastType) {
		super(owner);
		if (owner != null) {
			mainFormRef = (PumpView) owner;
		}
		lastPumpTypeId = lastTypeId;
		lastPumpType = lastType;
		initComponents();
		customInit();
		loadPumpCat();
		loadPumpTypes();
		// select and focus the default record
		tblTypeList.getSelectionModel().setSelectionInterval(0, defSelRow);
		tblTypeList.scrollRectToVisible(tblTypeList.getCellRect(defSelRow, 0, true));
	}

	public PumpType(Dialog owner) {
		super(owner);
		initComponents();
	}

	private void associateFunctionKeys() {
		// associate enter for choose
		String CHOOSE_ACTION_KEY = "chooseAction";
		Action chooseAction = new AbstractAction() {
		      public void actionPerformed(ActionEvent actionEvent) {
		        cmdChooseActionPerformed();
		      }
		    };
		KeyStroke entr = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		InputMap chooseInputMap = cmdChoose.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		chooseInputMap.put(entr, CHOOSE_ACTION_KEY);
		ActionMap chooseActionMap = cmdChoose.getActionMap();
		chooseActionMap.put(CHOOSE_ACTION_KEY, chooseAction);
		cmdChoose.setActionMap(chooseActionMap);
		
		// associate Esc for exit
		String CLOSE_ACTION_KEY = "closeAction";
		Action closeAction = new AbstractAction() {
		      public void actionPerformed(ActionEvent actionEvent) {
		        cmdCloseActionPerformed();
		      }
		    };
	    KeyStroke esc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		InputMap closeInputMap = cmdClose.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		closeInputMap.put(esc, CLOSE_ACTION_KEY);
		ActionMap closeActionMap = cmdClose.getActionMap();
		closeActionMap.put(CLOSE_ACTION_KEY, closeAction);
		cmdClose.setActionMap(closeActionMap);
	}
	
	// function to load existing pump types
	private void loadPumpCat() {
		// open db and read existing pump types
		try {
			db = Database.getInstance();
			
			// load distinct categories
			MyResultSet res = db.executeQuery("select distinct(category) as category from " + Configuration.PUMPTYPE);

			while (res.next()) {
				cmbCat.addItem(res.getString("category"));
			}
		} catch (Exception sqle) {
			JOptionPane.showMessageDialog(new JDialog(), "Error loading pump categories:" + sqle.getMessage());
			return;
		}
	}
	
	private void loadPumpTypes () {
		try {
			
			String filterText = txtSearch.getText().trim().isEmpty() ? "" : " where lower(type) like '%" + txtSearch.getText().trim().toLowerCase() + "%' or lower(desc) like '%" + txtSearch.getText().trim().toLowerCase() + "%'";
			// fetch and load the result
			MyResultSet res = db.executeQuery("select * from " + Configuration.PUMPTYPE + filterText);
			DefaultTableModel defModel = (DefaultTableModel) tblTypeList.getModel();
			while(defModel.getRowCount() > 0) {
				defModel.removeRow(0);
			}
			int curRow = -1;
			
			while (res.next()) {
				// add the pump type into the grid and ignore its features
				++curRow;
				defModel.addRow( new Object[] {"",""});
				tblTypeList.setValueAt(res.getString("type"), curRow, 0);
				tblTypeList.setValueAt(res.getString("desc"), curRow, 1);
				if (lastPumpType.equals(tblTypeList.getValueAt(curRow, 0))) {
					defSelRow = curRow;
				}
			}
		} catch (Exception sqle) {
			JOptionPane.showMessageDialog(new JDialog(), "Error loading pump types:" + sqle.getMessage());
			sqle.printStackTrace();
			return;
		}
	}
	
	// CUSTOM FUNCTIONS - BEGIN
	
	private void cmdAddActionPerformed() {
		// add the new type
		String curType = txtType.getText().trim();
		String curDesc = txtDesc.getText().trim();
		if ( curType.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please enter valid pump type to add");
			txtType.requestFocusInWindow();
			return;
		}

		// check for duplication
		for(int i=0; i<tblTypeList.getRowCount(); i++) {
			if (curType.toLowerCase().equals(tblTypeList.getValueAt(i, 0).toString().toLowerCase().trim())) {
				JOptionPane.showMessageDialog(this, "Pump '" + curType + "' already exist. Please use different pump type name and try again\nTIP:You may clear all fields by clicking on clear button before adding new pump");
				return;
			}
		}
		
		// required field validation
		if (!mandatoryCheck(pnlFeat)) {
			return;
		}
		
		try {
			// no duplicate - so add it
			String curCt = cmbCat.getSelectedItem().toString().trim();
			String curNonISIModel = chkISIModel.isSelected() ? "Y" : "N";
			String curDelSize = txtDelSize.getText().trim();
			String curSucSize = txtSucSize.getText().trim();
			// ISI handling
			String curMotEff = "";
			String curEff = "";
			String curSP = "";
			String curSL = "";
			//if(!Configuration.LAST_USED_ISSTD.startsWith("IS 8472:")) { // 8034,14220,9079
				curMotEff = txtMotEff.getText().trim();
				curEff = txtEff.getText().trim();
			//} else { // only for 8472
			if (Configuration.LAST_USED_ISSTD.startsWith("IS 8472:")) {
				curSP = txtSP.getText().trim();
				curSL = txtSL.getText().trim();
			}
			String curConn = cmbCon.getSelectedItem().toString();
			String curHdUnit = cmbHdUnit.getSelectedItem().toString();
			String curHead = txtHead.getText().trim();
			String curMotIp = txtMotIp.getText().trim();
			String curDisUnit = cmbDisUnit.getSelectedItem().toString();
			if (cmbDisUnit.getSelectedItem().toString().contains("sup")) {
				curDisUnit = cmbDisUnit.getSelectedItem().toString().substring(6,cmbDisUnit.getSelectedItem().toString().length()-7);
			}
			
			String curDis = txtDis.getText().trim();
			
			String curDisL = txtDisL.getText().trim();;
			String curDisH = txtDisH.getText().trim();
		
			String curHeadL = txtHeadL.getText().trim();
			String curHeadH = txtHeadH.getText().trim();
			String curGauge = txtGauge.getText().trim();
			
			String curMotType = txtMotType.getText().trim();
			String curKW = txtKw.getText();
			String curHP = txtHp.getText();
			
			String curVolts = txtVolts.getText();
			// construct list of other volts
			String curOtherVolts = "";
			String curOtherVoltsDis = chkMV.isSelected()?"Y":"N";
			if (cmbVolts.getItemCount() > 0) {
				for(int i=0; i<cmbVolts.getItemCount(); i++) {
					curOtherVolts += cmbVolts.getItemAt(i) + ",";
				}
				curOtherVolts = curOtherVolts.substring(0, curOtherVolts.length()-1);
			}
			
			String curPhase = cmbPhase.getSelectedItem().toString();
			String curAmps = txtAmps.getText();
			String curSpeed = txtSpeed.getText();
			String curFreq = txtFreq.getText();
			
			String curBrSz = txtBrSz.getText().trim();
			String curNoOfStag = txtNoOfStag.getText().trim();
			String curIns = cmbIns.getSelectedItem().toString();
			
			String curPresSz = txtPreSize.getText().trim();
			String curDLWL = txtDLWL.getText().trim();
			String curSub = txtSub.getText().trim();
			String curOpPres = txtOprPres.getText().trim();
			
			String curPole = txtPoles.getText().trim();
			String curCapRate = txtCapRat.getText().trim();
			String curCapVolt = txtCapVol.getText().trim();
			
			String curRecentPumpSno = lblRecPumpSNo.getText().trim();
			String curRecentMotSno = lblRecMotSNo.getText().trim();
		
			String curTypeTestFreq = chosenTypeTestFreq;
			String curAutoValveType = chosenAutoValveType;
			
			// append into pump table
			db.executeUpdate("insert into " + Configuration.PUMPTYPE + "(type, desc, delivery_size , suction_size , mot_eff , head , mot_ip , " + 
							"discharge_unit , discharge , discharge_low , discharge_high , overall_eff , head_low , head_high , gauge_distance , " +
							"mot_type , kw , hp , " +
							"volts , phase , amps , speed , freq , " +
							"conn , head_unit , bore_size , no_of_stage , " +
							"recent_pump_sno, recent_motor_sno, " +
							"non_isi_model , category , ins_class , other_volts , other_volts_disabled , self_priming_time , suction_lift , "+
							"pres_size , dlwl , submergence , min_op_pres, no_of_poles, cap_rating, cap_volt, type_test_freq, auto_valve_type) values ('" + curType + "','" + curDesc + "','" + curDelSize + "','" + curSucSize + "','" + curMotEff + "','" + 
					curHead + "','" + curMotIp + "','" + curDisUnit + "','" + curDis + "','" + curDisL + "','" + curDisH + "','" +  
					curEff + "','" + curHeadL + "','" + curHeadH + "','" + curGauge + "','" + 
					curMotType + "','" + curKW + "','" + curHP + "','" + 
					curVolts + "','" + curPhase + "','" + curAmps + "','" + curSpeed + "','" + curFreq + "','" +
					curConn + "','" + curHdUnit + "','" + curBrSz + "','" + curNoOfStag + "','" + 
					curRecentPumpSno + "','" + curRecentMotSno + "','" +
					curNonISIModel + "','" + curCt + "','" + curIns + "','" + curOtherVolts +"','" + 
					curOtherVoltsDis +"','" + curSP +"','" + curSL +"','" +
					curPresSz +"','" + curDLWL +"','" + curSub + "','" + curOpPres + "','" + curPole + "','" + curCapRate + "','" + curCapVolt + "','" + curTypeTestFreq + "','" + curAutoValveType +"')");
		
			
			// recent pump type id
			MyResultSet res = db.executeQuery("select seq from sqlite_sequence where name='" + Configuration.PUMPTYPE + "'");

			String recentPumpId = "";
			if (res.next()) {
				recentPumpId = res.getString("seq");
			}
			// head settings
			int i = 0;
			for(String key : testListAll.keySet()) {
				db.executeUpdate("insert into " + Configuration.TESTNAMES_ALL + " values ('" + recentPumpId + "','" + key + "','" + testListAll.get(key) + "',''," + i + ")");
				++i;
			}
			i = 0;
			for(String key : testListRt.keySet()) {
				db.executeUpdate("insert into " + Configuration.TESTNAMES_ROUTINE + "(pump_type_id,code,seq) values ('" + recentPumpId + "','" + key + "'," + i + ")");
				++i;
			}
			i = 0;
			for(String key : testListType.keySet()) {
				db.executeUpdate("insert into " + Configuration.TESTNAMES_TYPE + "(pump_type_id,code,seq) values ('" + recentPumpId + "','" + key + "'," + i + ")");
				++i;
			}
			
			// routine test limits
			db.executeUpdate("insert into " + Configuration.ROUTINE_LIMITS + "(pump_type_id, code) values ('" + recentPumpId + "', 'FO')"); 
			db.executeUpdate("insert into " + Configuration.ROUTINE_LIMITS + "(pump_type_id, code) values ('" + recentPumpId + "', 'DP')"); 
			db.executeUpdate("insert into " + Configuration.ROUTINE_LIMITS + "(pump_type_id, code) values ('" + recentPumpId + "', 'SO')"); 
			
			// add the category into combo box if not exist already
			i=1;
			for (i=1; i<cmbCat.getItemCount(); i++) {
				if (curCt.equals(cmbCat.getItemAt(i))) {
					break;
				}
			}
			if (i==cmbCat.getItemCount()) { // not found, hence add it
				cmbCat.addItem(curCt);
			}
			
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(this, "Please enter valid values for all fields of pump features");
			txtMotType.requestFocusInWindow();
			return;
		} catch (Exception sqle) {
			sqle.printStackTrace();
			JOptionPane.showMessageDialog(new JDialog(), "Error inserting new pump type:" + sqle.getMessage());
			return;
		}
		
		// add into list
		DefaultTableModel defModel = (DefaultTableModel) tblTypeList.getModel();
		defModel.addRow( new Object[] {null});
		tblTypeList.setValueAt(curType, tblTypeList.getRowCount()-1, 0);
		tblTypeList.setValueAt(curDesc, tblTypeList.getRowCount()-1, 1);
		tblTypeList.getSelectionModel().setSelectionInterval(tblTypeList.getRowCount()-1, tblTypeList.getRowCount()-1);
		tblTypeList.scrollRectToVisible(tblTypeList.getCellRect(tblTypeList.getRowCount()-1, 0, true));
		
		JOptionPane.showMessageDialog(this, "Added successfully");
	}

	private void cmdUpdateActionPerformed() {
		// update the selected type
		String selType = null;
		if ( tblTypeList.getSelectedRow() < 0) {
			JOptionPane.showMessageDialog(this, "Please select an existing pump type from the list before updating it"); 
			return;
		}
		else {
			selType = tblTypeList.getValueAt(tblTypeList.getSelectedRow(), 0).toString();
		}
		
		String curType = txtType.getText().trim();
		if ( curType.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please enter a pump type before updating"); 
			return;
		}
		
		int response = JOptionPane.showConfirmDialog(this, "Do you want to update pump type '" + selType + "'?");
		if (response != 0) {
			JOptionPane.showMessageDialog(this, "Update is cancelled");
			return;
		}
		
		// check for duplication
		for(int i=0; i<tblTypeList.getRowCount(); i++) {
			if (curType.toLowerCase().equals(tblTypeList.getValueAt(i, 0).toString().toLowerCase().trim()) && i != tblTypeList.getSelectedRow()) {
				JOptionPane.showMessageDialog(this, "Pump '" + curType + "' already exist. Please use different pump type name and try again\nTIP:You may clear all fields by clicking on clear button before adding new pump");
				return;
			}
		}
		
		try {
			// mandatory check
			if(!mandatoryCheck(pnlFeat)) {
				return;
			}
			
			// check for pump type change
			if (!selType.equals(curType))
			{
				MyResultSet res = db.executeQuery("select count(distinct(pump_slno)) as tot from " + Configuration.READING_DETAIL + " where pump_type_id='" + selPumpTypeId + "'");
				if (res.next()) {
					if (res.getLong("tot") > 0) {
						response = JOptionPane.showConfirmDialog(this, "WARNING: " + res.getLong("tot") + " tests were performed for pump type '" + selType + "'\n" +
													"Updating the pump type will also update corresponding tests to '" + curType +"', do you want to continue?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE); 
						if (response != 0) {
							JOptionPane.showMessageDialog(this, "Update is cancelled");
							return;
						}
					}
				}
			}
			
			// check for rated voltage change
			if (!selRatedV.equals(txtVolts.getText()) ) {
				MyResultSet res = db.executeQuery("select count(distinct(pump_slno)) as tot from " + Configuration.READING_DETAIL + " where pump_type_id='" + selPumpTypeId + "' and rated_volt = '"+ selRatedV +"'");
				if (res.next()) {
					if (res.getLong("tot") > 0) {
						response = JOptionPane.showConfirmDialog(this, "WARNING: " + res.getLong("tot") + " tests were performed under rated voltage '"+ selRatedV +"' for this pump type\n" +
													"Updating the rated voltage will also update corresponding tests to new voltage '" + txtVolts.getText() +"', do you want to continue?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE); 
						if (response != 0) {
							JOptionPane.showMessageDialog(this, "Update is cancelled");
							return;
						}
					}
				}
			}
			
			// 1. update the pump type table
			this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			String curDesc = txtDesc.getText().trim();
			String curCt = cmbCat.getSelectedItem().toString().trim();
			String curNonISIModel = chkISIModel.isSelected() ? "Y" : "N";
			String curDelSize = txtDelSize.getText().trim();
			String curSucSize = txtSucSize.getText().trim();
			
			// ISI handling
			String curEff = "";
			String curMotEff = "";
			String curSP = "";
			String curSL = "";
			//if(!Configuration.LAST_USED_ISSTD.startsWith("IS 8472:")) { // 8034,14220,9079
				curMotEff = txtMotEff.getText().trim();
				curEff = txtEff.getText().trim();
			//} else { // only for 8472
			if (Configuration.LAST_USED_ISSTD.startsWith("IS 8472:")) {
				curSP = txtSP.getText().trim();
				curSL = txtSL.getText().trim();
			}
			
			String curConn = cmbCon.getSelectedItem().toString();
			String curHdUnit = cmbHdUnit.getSelectedItem().toString();
			String curHead = txtHead.getText().trim();
			String curMotIp = txtMotIp.getText().trim();
			String curDisUnit = cmbDisUnit.getSelectedItem().toString();
			if (cmbDisUnit.getSelectedItem().toString().contains("sup")) {
				curDisUnit = cmbDisUnit.getSelectedItem().toString().substring(6,cmbDisUnit.getSelectedItem().toString().length()-7);
			}
			
			String curDis = txtDis.getText().trim();
			
			String curDisL = txtDisL.getText().trim();
			String curDisH = txtDisH.getText().trim();
			
			String curHeadL = txtHeadL.getText().trim();
			String curHeadH = txtHeadH.getText().trim();
			String curGauge = txtGauge.getText().trim();
			
			String curMotType = txtMotType.getText();
			String curKW = txtKw.getText().trim();
			String curHP = txtHp.getText().trim();
			
			String curVolts = txtVolts.getText();
			// construct list of other volts
			String curOtherVolts = "";
			String curOtherVoltsDis = chkMV.isSelected()?"Y":"N";
			if (cmbVolts.getItemCount() > 0) {
				for(int i=0; i<cmbVolts.getItemCount(); i++) {
					curOtherVolts += cmbVolts.getItemAt(i) + ",";
				}
				curOtherVolts = curOtherVolts.substring(0, curOtherVolts.length()-1);
			}
			
			String curPhase = cmbPhase.getSelectedItem().toString();
			String curAmps = txtAmps.getText().trim();
			String curSpeed = txtSpeed.getText();
			String curFreq = txtFreq.getText().trim();
			
			String curBrSz = txtBrSz.getText().trim();
			String curNoOfStag = txtNoOfStag.getText().trim();
			String curIns = cmbIns.getSelectedItem().toString();
			
			String curPresSz = txtPreSize.getText().trim();
			String curDLWL = txtDLWL.getText().trim();
			String curSub = txtSub.getText().trim();
			String curOpPres = txtOprPres.getText().trim();
			
			String curPole = txtPoles.getText().trim();
			String curCapRate = txtCapRat.getText().trim();
			String curCapVolt = txtCapVol.getText().trim();
			
			String curRecentPumpSno = lblRecPumpSNo.getText().trim();
			String curRecentMotSno = lblRecMotSNo.getText().trim();
			
			db.executeUpdate("update " + Configuration.PUMPTYPE + " set " + "type='" + curType + "', desc='" + curDesc + "', delivery_size='" + curDelSize + "', suction_size='" + curSucSize + "', mot_eff='" + curMotEff + "', head='" + curHead + "', mot_ip='" +
					curMotIp + "', discharge_unit='" + curDisUnit + "', discharge='" + curDis +  "', discharge_low='" + curDisL  + "', discharge_high='" + curDisH + "', overall_eff='" + curEff + 
					"', head_low='" + curHeadL + "', head_high='" + curHeadH + "', gauge_distance='" + curGauge +
					"', mot_type='" + curMotType + "', kw='" + curKW + "', hp='" + curHP + "', volts='" + 
					curVolts + "', phase='" + curPhase + "', amps='" + curAmps + "', speed='" + curSpeed + "', freq='" + curFreq +
					"', conn='" + curConn + "', head_unit='" + curHdUnit + "', bore_size='" + curBrSz + "', no_of_stage='" + curNoOfStag + 
					"', recent_pump_sno='" + curRecentPumpSno + "', recent_motor_sno='" + curRecentMotSno + "', non_isi_model='" + curNonISIModel + "', category='" + curCt + "', ins_class='" + curIns + "', other_volts='" + curOtherVolts +
					"', other_volts_disabled='" + curOtherVoltsDis +"', self_priming_time='" + curSP + "', suction_lift='" + curSL + 
					"', pres_size='" + curPresSz + "', dlwl='" + curDLWL + "', submergence='" + curSub +"', min_op_pres='" + curOpPres + 
					"', no_of_poles='" + curPole +"', cap_rating='" + curCapRate +"', cap_volt='" + curCapVolt + "' where pump_type_id='" + selPumpTypeId +"'");
		
			
			// add the category into combo box if not exist already
			int i=1;
			for (i=1; i<cmbCat.getItemCount(); i++) {
				if (curCt.equals(cmbCat.getItemAt(i))) {
					break;
				}
			}
			if (i==cmbCat.getItemCount()) { // not found, hence add it
				cmbCat.addItem(curCt);
			}
			
			// 3. update reading detail for voltage
			if (!selRatedV.equals(txtVolts.getText())) {
				db.executeUpdate("update " + Configuration.READING_DETAIL + " set rated_volt='" + txtVolts.getText()  + "' where pump_type_id='" + selPumpTypeId + "' and rated_volt='" + selRatedV +"'"); 
			}
			
			// update the list
			tblTypeList.setValueAt(curType, tblTypeList.getSelectedRow(), 0);
			tblTypeList.setValueAt(curDesc, tblTypeList.getSelectedRow(), 1);
			updatePumpType("update");
			JOptionPane.showMessageDialog(this, "Updated successfully");
		} catch (NumberFormatException ne)  {
			JOptionPane.showMessageDialog(this, "Please enter valid numbers for all required fields");
		} catch (Exception sqle) {
			JOptionPane.showMessageDialog(new JDialog(), "Error updating pump type:" + sqle.getMessage());
			sqle.printStackTrace();
		} finally {
			this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
		
	}

	private void cmdDelActionPerformed() {
		// delete the selected type
		String selType = null;
		if ( tblTypeList.getSelectedRow() < 0) {
			JOptionPane.showMessageDialog(this, "Please select an existing pump type from the list before deleting it"); 
			return;
		}
		else {
			selType = tblTypeList.getValueAt(tblTypeList.getSelectedRow(), 0).toString();
		}
	
		int response = JOptionPane.showConfirmDialog(this, "Do you want to delete pump type '" + selType + "'?");
		if (response != 0) {
			JOptionPane.showMessageDialog(this, "Delete is cancelled");
			return;
		}
		
		// check for readings 
		try {
			MyResultSet res = db.executeQuery("select count(distinct(pump_slno)) as tot from " + Configuration.READING_DETAIL + " where pump_type_id='" + selPumpTypeId + "'");
			if (res.next()) {
				if (res.getLong("tot") > 0) {
					response = JOptionPane.showConfirmDialog(this, "WARNING: " + res.getLong("tot") + " tests were performed for pump type '" + selType + "'\n" +
												"Deleting the pump type will also delete corresponding tests, do you want to continue?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE); 
					if (response != 0) {
						JOptionPane.showMessageDialog(this, "Delete is cancelled");
						return;
					}
				}
			}
			
					
			// 1. delete the record from detail
			db.executeUpdate("delete from " + Configuration.READING_DETAIL + " where pump_type_id='" + selPumpTypeId + "'");
			db.executeUpdate("delete from " + Configuration.TESTNAMES_ALL + " where pump_type_id='" + selPumpTypeId + "'");
			db.executeUpdate("delete from " + Configuration.TESTNAMES_ROUTINE + " where pump_type_id='" + selPumpTypeId + "'");
			db.executeUpdate("delete from " + Configuration.TESTNAMES_TYPE + " where pump_type_id='" + selPumpTypeId + "'");
			db.executeUpdate("delete from " + Configuration.ROUTINE_LIMITS + " where pump_type_id='" + selPumpTypeId + "'");
			db.executeUpdate("delete from " + Configuration.MOT_EFF_DATA_POINTS + " where pump_type_id='" + selPumpTypeId + "'");
			db.executeUpdate("delete from " + Configuration.NON_ISI_PERF_TOLERANCE + " where pump_type_id='" + selPumpTypeId + "'");
			
			// 2. delete the record from master
			db.executeUpdate("delete from " + Configuration.PUMPTYPE + " where pump_type_id='" + selPumpTypeId + "'");
							
		} catch (Exception sqle) {
			JOptionPane.showMessageDialog(new JDialog(), "Error deleting the pump type:" + sqle.getMessage());
			return;
		}
		
		// update the list
		int selRow = tblTypeList.getSelectedRow();
		for(int i=tblTypeList.getSelectedRow(); i<tblTypeList.getRowCount()-1; i++) {
			tblTypeList.setValueAt(tblTypeList.getValueAt(i+1, 0), i, 0);	
			tblTypeList.setValueAt(tblTypeList.getValueAt(i+1, 1), i, 1);	
		}
		// delete last row
		DefaultTableModel defModel = (DefaultTableModel) tblTypeList.getModel();
		defModel.removeRow(tblTypeList.getRowCount()-1);
		// focus previous row
		if (tblTypeList.getRowCount() > 0) {
			if (selRow == tblTypeList.getRowCount()) {
				tblTypeList.setRowSelectionInterval(selRow-1, selRow-1);
				txtType.setText(tblTypeList.getValueAt(selRow-1, 0).toString());
			} else {
				tblTypeList.setRowSelectionInterval(selRow, selRow);
				txtType.setText(tblTypeList.getValueAt(selRow, 0).toString());
			}
			if (selRow != 0) {
				tblTypeList.getSelectionModel().setSelectionInterval(selRow-1, selRow-1);
			}
		} else {
			cmdClearActionPerformed();
		}
		JOptionPane.showMessageDialog(this, "Deleted successfully");
	}

	private void cmdCloseActionPerformed() {
		thisWindowClosing(null);
		this.setVisible(false);
	}
	
	public void updatePumpType (String action) {
		// set the pump type and features in main form 
		if (action.equals("update")) {
			// update the main form pump parameters if the pump just updated is the one already chosen in main form
			mainFormRef.setPumpType(selPumpTypeId, false, false, action); // also clear it if change in rated voltage 
		} else if (!Configuration.LAST_USED_PUMP_TYPE_ID.equals(selPumpTypeId)) { // choose
			mainFormRef.setPumpType(selPumpTypeId, true, false, action);
		}
	}

	private void cmdChooseActionPerformed() {
		// delete the selected type
		if ( tblTypeList.getSelectedRow() < 0) {
			JOptionPane.showMessageDialog(this, "Please select an existing pump type from the list to choose it"); 
			return;
		}
		this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		updatePumpType("choose");		
		this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		this.setVisible(false);
	}

	private void cmdClearActionPerformed() {
		// clear form data
		clearPanel(pnlPump);
		clearPanel(pnlFeat);
	}

	private void tblTypeListKeyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			cmdChooseActionPerformed();
		}
	}

	private void thisWindowClosing(WindowEvent e) {
	}

	private void cmbDisUnitItemStateChanged() {
		if (cmbDisUnit.getSelectedItem().toString().contains("sup")) {
			lblDis.setText("<html>" + "Discharge (" + cmbDisUnit.getSelectedItem().toString().substring(6,cmbDisUnit.getSelectedItem().toString().length()-7) + ")</html>");
		} else {
			lblDis.setText("Discharge (" + cmbDisUnit.getSelectedItem().toString() + ")");
		}
	}

	private Boolean mandatoryCheck(JPanel pnl) {
		JTextField txtFld = new JTextField();
		for(Component comp : pnl.getComponents()) {
			if (comp instanceof JTextField) {
				txtFld = (JTextField) comp;
				if (txtFld.getCaretColor().equals(Color.darkGray) && txtFld.isVisible() && txtFld.isEnabled()) {
					if(txtFld.getText().trim().isEmpty()) {
						JOptionPane.showMessageDialog(this, "Please enter value of " + txtFld.getToolTipText());
						txtFld.requestFocusInWindow();
						return false;
					}
				}
			}
		}
		return true;
	}
	
	private void clearPanel(JPanel pnl) {
		for(Component comp : pnl.getComponents()) {
			if (comp instanceof JTextField) {
				((JTextField) comp).setText("");
			} else if (comp instanceof JComboBox) {
				((JComboBox) comp).setSelectedIndex(0);
			}
		}
	}

	private void cmbHdUnitActionPerformed() {
		lblGauge.setText("Gauge Distance (" + cmbHdUnit.getSelectedItem().toString() +")");
		lblHead.setText("Head (" + cmbHdUnit.getSelectedItem().toString() +")");
	}

	private void cmdAddVActionPerformed() {
		if (cmbVolts.getSelectedItem().toString().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please enter value of Voltage");
			cmbVolts.requestFocusInWindow();
			return;
		}
		// add into combo box if not exist already
		Boolean vExist = false;
		for (int i=0; i<cmbVolts.getItemCount(); i++) {
			if (cmbVolts.getSelectedItem().toString().equals(cmbVolts.getItemAt(i))) {
				vExist = true;
				break;
			}
		}
		if (!vExist) {
			cmbVolts.addItem(cmbVolts.getSelectedItem().toString());
			JOptionPane.showMessageDialog(this, "Voltage added successfully");
		} else {
			JOptionPane.showMessageDialog(this, "Please enter new Voltage to add");
			cmbVolts.requestFocusInWindow();
		}
	}

	private void cmdDelVActionPerformed() {
		if (cmbVolts.getSelectedItem().toString().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please select a Voltage to delete");
			cmbVolts.requestFocusInWindow();
			return;
		}
		
		// check for detail records for this rated voltage
		try {
			MyResultSet res = db.executeQuery("select count(distinct(pump_slno)) as tot from " + Configuration.READING_DETAIL + " where pump_type_id='" + selPumpTypeId + "' and rated_volt = '"+ cmbVolts.getSelectedItem().toString() +"'");
			if (res.next()) {
				if (res.getLong("tot") > 0) {
					JOptionPane.showMessageDialog(this, "ERROR: Unable to delete as " + res.getLong("tot") + " tests were performed under rated voltage '"+ cmbVolts.getSelectedItem().toString() +"' for this pump type\n" +
												"Those tests need to be deleted before deleting this rated voltage", "Error", JOptionPane.ERROR_MESSAGE); 
					return;
				}
			}
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(this, "Error deleting voltage from the list:" + e.getMessage());
		}
					
		// delete from the list
		cmbVolts.removeItem(cmbVolts.getSelectedItem());
		if (cmbVolts.getItemCount() > 0) {
			cmbVolts.setSelectedIndex(0);
		} else {
			cmbVolts.setSelectedItem("");
		}
		JOptionPane.showMessageDialog(this, "Voltage deleted successfully");
	}

	public void loadTestHeads () {
		MyResultSet res = null;
		try {
			// load test head settings
			testListAll.clear();
			res = db.executeQuery("select * from " + Configuration.TESTNAMES_ALL + " where pump_type_id='" + selPumpTypeId + "' order by seq");
			while (res.next()) {
				testListAll.put(res.getString("code"), res.getString("name"));
			}
			
			testListRt.clear();
			res = db.executeQuery("select a.*, b.name from " + Configuration.TESTNAMES_ROUTINE + " a join " + Configuration.TESTNAMES_ALL + " b on b.pump_type_id=a.pump_type_id and b.code=a.code where a.pump_type_id='" + selPumpTypeId + "' order by a.seq");
			while (res.next()) {
				testListRt.put(res.getString("code"), res.getString("name"));
			}
			
			testListType.clear();
			res = db.executeQuery("select a.*, b.name from " + Configuration.TESTNAMES_TYPE + " a join " + Configuration.TESTNAMES_ALL + " b on b.pump_type_id=a.pump_type_id and b.code=a.code where a.pump_type_id='" + selPumpTypeId + "' order by a.seq");
			while (res.next()) {
				testListType.put(res.getString("code"), res.getString("name"));
			}
		} catch (SQLException sqle) {
			JOptionPane.showMessageDialog(new JDialog(), "Error reading the pump type:" + sqle.getMessage());
		}
	}

	private void cmdTestSetActionPerformed() {
		TestHeads dlgTestHead = new TestHeads(this, selPumpTypeId, selPumpType);
		dlgTestHead.setVisible(true);
	}

	private void txtSearchKeyReleased() {
		// refresh the pump list with new filter
		loadPumpTypes();
	}

	private void findPrevPumpNo(Boolean isMotorNoSame) {
		String nextSno = txtNextPumpNo.getText();
		String lastUsedSNo = Configuration.findPrevNo(nextSno);
		lblRecPumpSNo.setText(lastUsedSNo);
		if (isMotorNoSame) {
			txtNextMotNo.setText(nextSno);
			lblRecMotSNo.setText(lastUsedSNo);
		}
	}
	
	private void findPrevMotorNo() {
		String nextSno = txtNextMotNo.getText();
		String lastUsedSNo = Configuration.findPrevNo(nextSno);
		lblRecMotSNo.setText(lastUsedSNo);
	}
	
	private void findNextPumpNo(Boolean isMotorNoSame) {
		String lastUsedSNo = lblRecPumpSNo.getText();
		String nextSno = Configuration.findNextNo(lastUsedSNo);
		txtNextPumpNo.setText(nextSno);
		if (isMotorNoSame) {
			txtNextMotNo.setText(nextSno);
			lblRecMotSNo.setText(lastUsedSNo);
		}
	}
	
	private void findNextMotorNo() {
		String lastUsedSNo = lblRecMotSNo.getText();
		String nextSno = Configuration.findNextNo(lastUsedSNo);
		txtNextMotNo.setText(nextSno);
	}

	private void txtNextPumpNoKeyReleased() {
		findPrevPumpNo(true);
	}
	private void txtNextMotNoKeyReleased() {
		findPrevMotorNo();
	}

	private void cmdRoutineLimitsActionPerformed() {
		RoutineLimits dlgRoutine = new RoutineLimits(this, selPumpTypeId, selPumpType, cmbDisUnit.getSelectedItem().toString());
		dlgRoutine.setVisible(true);
	}

	private void cmdMotEffActionPerformed() {
		MotorEffDataPoints dlgMotEff = new MotorEffDataPoints(this, selPumpTypeId, selPumpType, cmbDisUnit.getSelectedItem().toString());
		dlgMotEff.setVisible(true);
	}

	private void chkISIModelActionPerformed() {
		cmdNonISITol.setEnabled(chkISIModel.isSelected());
	}

	private void cmdNonISITolActionPerformed() {
		NonISITolerance dlgNonISITol = new NonISITolerance(this, selPumpTypeId);
		dlgNonISITol.setVisible(true);
	}

	private void cmbPhase() {
		Boolean tmpSingPh = cmbPhase.getSelectedItem().toString().equals("Single");
		lblCapRate.setEnabled(tmpSingPh);
		lblCapVolt.setEnabled(tmpSingPh);
		txtCapRat.setEnabled(tmpSingPh);
		txtCapVol.setEnabled(tmpSingPh);
	}

	
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		pnlHead = new JPanel();
		label5 = new JLabel();
		txtSearch = new JTextField();
		scrlTypeList = new JScrollPane();
		tblTypeList = new JTable();
		cmdChoose = new JButton();
		pnlPump = new JPanel();
		panel3 = new JPanel();
		label8 = new JLabel();
		txtType = new JTextField();
		label1 = new JLabel();
		txtDesc = new JTextField();
		panel2 = new JPanel();
		label7 = new JLabel();
		label19 = new JLabel();
		label20 = new JLabel();
		label21 = new JLabel();
		label22 = new JLabel();
		label23 = new JLabel();
		label2 = new JLabel();
		cmbCat = new JComboBox();
		chkISIModel = new JCheckBox();
		cmdNonISITol = new JButton();
		pnlFeat = new JPanel();
		label16 = new JLabel();
		txtMotType = new JTextField();
		label4 = new JLabel();
		cmbPhase = new JComboBox();
		lblSOHd2 = new JLabel();
		cmbCon = new JComboBox();
		label79 = new JLabel();
		txtKw = new JTextField();
		label80 = new JLabel();
		txtHp = new JTextField();
		label24 = new JLabel();
		txtPoles = new JTextField();
		lblIns = new JLabel();
		cmbIns = new JComboBox();
		label10 = new JLabel();
		txtVolts = new JTextField();
		label11 = new JLabel();
		txtAmps = new JTextField();
		lblMotIp = new JLabel();
		txtMotIp = new JTextField();
		lblMotEff = new JLabel();
		txtMotEff = new JTextField();
		label17 = new JLabel();
		txtFreq = new JTextField();
		label13 = new JLabel();
		txtSpeed = new JTextField();
		lblCapRate = new JLabel();
		txtCapRat = new JTextField();
		lblCapVolt = new JLabel();
		txtCapVol = new JTextField();
		separator1 = new JSeparator();
		lblSucSize = new JLabel();
		txtSucSize = new JTextField();
		label78 = new JLabel();
		txtDelSize = new JTextField();
		lblGauge = new JLabel();
		txtGauge = new JTextField();
		lblPresSize = new JLabel();
		txtPreSize = new JTextField();
		label6 = new JLabel();
		cmbHdUnit = new JComboBox();
		lblHead = new JLabel();
		txtHead = new JTextField();
		lblHdLow = new JLabel();
		txtHeadL = new JTextField();
		lblHdHigh = new JLabel();
		txtHeadH = new JTextField();
		label3 = new JLabel();
		cmbDisUnit = new JComboBox();
		lblDis = new JLabel();
		txtDis = new JTextField();
		label15 = new JLabel();
		txtDisL = new JTextField();
		label18 = new JLabel();
		txtDisH = new JTextField();
		lblSP = new JLabel();
		txtSP = new JTextField();
		lblSL = new JLabel();
		txtSL = new JTextField();
		lblBrSz = new JLabel();
		txtBrSz = new JTextField();
		lblNoOfStag = new JLabel();
		txtNoOfStag = new JTextField();
		lblDLWL = new JLabel();
		txtDLWL = new JTextField();
		lblSub = new JLabel();
		txtSub = new JTextField();
		lblOprPress = new JLabel();
		txtOprPres = new JTextField();
		lblOverallEff = new JLabel();
		txtEff = new JTextField();
		cmdTestSet = new JButton();
		cmdRoutineLimits = new JButton();
		cmdMotEff = new JButton();
		pnlMV = new JPanel();
		label12 = new JLabel();
		cmbVolts = new JComboBox();
		cmdAddV = new JButton();
		cmdDelV = new JButton();
		chkMV = new JCheckBox();
		pnlSNo = new JPanel();
		lblPumpSNo = new JLabel();
		lblRecPumpSNo = new JLabel();
		lblPumpSNoNext = new JLabel();
		txtNextPumpNo = new JTextField();
		lblMsno = new JLabel();
		lblRecMotSNo = new JLabel();
		lblMotSNoNext = new JLabel();
		txtNextMotNo = new JTextField();
		cmdAdd = new JButton();
		cmdUpdate = new JButton();
		cmdDel = new JButton();
		cmdClose = new JButton();

		//======== this ========
		setTitle("Doer PumpView: Choose Pump");
		setFont(new Font("Arial", Font.PLAIN, 12));
		setModal(true);
		setResizable(false);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				thisWindowClosing(e);
			}
		});
		Container contentPane = getContentPane();
		contentPane.setLayout(new TableLayout(new double[][] {
			{5, 400, TableLayout.FILL, TableLayout.FILL, 5},
			{5, TableLayout.PREFERRED, 5}}));
		((TableLayout)contentPane.getLayout()).setHGap(5);
		((TableLayout)contentPane.getLayout()).setVGap(5);

		//======== pnlHead ========
		{
			pnlHead.setBorder(new TitledBorder(null, "Existing Pump Models [All Assembly Lines]", TitledBorder.LEADING, TitledBorder.TOP,
				new Font("Arial", Font.BOLD, 14), Color.blue));
			pnlHead.setFocusable(false);
			pnlHead.setLayout(new TableLayout(new double[][] {
				{TableLayout.PREFERRED, TableLayout.FILL},
				{5, 27, TableLayout.FILL, TableLayout.PREFERRED}}));
			((TableLayout)pnlHead.getLayout()).setHGap(5);
			((TableLayout)pnlHead.getLayout()).setVGap(5);

			//---- label5 ----
			label5.setText("Search");
			label5.setFont(new Font("Arial", Font.PLAIN, 14));
			pnlHead.add(label5, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- txtSearch ----
			txtSearch.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 16));
			txtSearch.setToolTipText("Type here the model you wish to search");
			txtSearch.addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent e) {
					txtSearchKeyReleased();
				}
			});
			pnlHead.add(txtSearch, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//======== scrlTypeList ========
			{
				scrlTypeList.setToolTipText("List of existing motor types");

				//---- tblTypeList ----
				tblTypeList.setModel(new DefaultTableModel(
					new Object[][] {
						{null, null},
					},
					new String[] {
						"Pump Model", "Description"
					}
				) {
					boolean[] columnEditable = new boolean[] {
						false, false
					};
					@Override
					public boolean isCellEditable(int rowIndex, int columnIndex) {
						return columnEditable[columnIndex];
					}
				});
				tblTypeList.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
				tblTypeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				tblTypeList.addKeyListener(new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent e) {
						tblTypeListKeyPressed(e);
					}
				});
				scrlTypeList.setViewportView(tblTypeList);
			}
			pnlHead.add(scrlTypeList, new TableLayoutConstraints(0, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdChoose ----
			cmdChoose.setText("Choose Selected Pump   [Enter]");
			cmdChoose.setFont(new Font("Arial", Font.PLAIN, 14));
			cmdChoose.setIcon(new ImageIcon(getClass().getResource("/img/choose.PNG")));
			cmdChoose.setToolTipText("Click on this to choose the selected pump for your test");
			cmdChoose.setRolloverSelectedIcon(null);
			cmdChoose.setDefaultCapable(false);
			cmdChoose.addActionListener(e -> cmdChooseActionPerformed());
			pnlHead.add(cmdChoose, new TableLayoutConstraints(0, 3, 1, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		}
		contentPane.add(pnlHead, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

		//======== pnlPump ========
		{
			pnlPump.setBorder(new TitledBorder(null, "Pump Details", TitledBorder.CENTER, TitledBorder.TOP,
				new Font("Arial", Font.BOLD, 14), Color.blue));
			pnlPump.setFocusable(false);
			pnlPump.setLayout(new TableLayout(new double[][] {
				{TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL},
				{5, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, TableLayout.PREFERRED}}));
			((TableLayout)pnlPump.getLayout()).setHGap(5);
			((TableLayout)pnlPump.getLayout()).setVGap(5);

			//======== panel3 ========
			{
				panel3.setLayout(new TableLayout(new double[][] {
					{134, 122, 300, 155, TableLayout.FILL},
					{TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED}}));
				((TableLayout)panel3.getLayout()).setHGap(5);
				((TableLayout)panel3.getLayout()).setVGap(5);

				//---- label8 ----
				label8.setText("Pump Model / Code");
				label8.setFont(new Font("Arial", Font.PLAIN, 14));
				panel3.add(label8, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- txtType ----
				txtType.setFont(new Font("Microsoft Sans Serif", Font.BOLD, 16));
				txtType.setToolTipText("Pump model or product code of the pump");
				txtType.setCaretColor(Color.darkGray);
				panel3.add(txtType, new TableLayoutConstraints(1, 0, 2, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label1 ----
				label1.setText("<html>Description <font size=-2>(Optional)</font></html>");
				label1.setFont(new Font("Arial", Font.PLAIN, 14));
				panel3.add(label1, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- txtDesc ----
				txtDesc.setFont(new Font("Microsoft Sans Serif", Font.BOLD, 14));
				txtDesc.setToolTipText("Full name of the pump or description of the product");
				panel3.add(txtDesc, new TableLayoutConstraints(1, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//======== panel2 ========
				{
					panel2.setBackground(Color.lightGray);
					panel2.setBorder(new LineBorder(Color.yellow, 2, true));
					panel2.setLayout(new TableLayout(new double[][] {
						{TableLayout.FILL, 71, TableLayout.PREFERRED, TableLayout.FILL},
						{5, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, 5}}));
					((TableLayout)panel2.getLayout()).setHGap(2);
					((TableLayout)panel2.getLayout()).setVGap(2);

					//---- label7 ----
					label7.setText("Green");
					label7.setFont(new Font("Arial", Font.BOLD, 12));
					label7.setForeground(new Color(0x009933));
					label7.setHorizontalAlignment(SwingConstants.CENTER);
					panel2.add(label7, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label19 ----
					label19.setText("Guaranteed Performance Values");
					label19.setFont(new Font("Arial", Font.ITALIC, 12));
					label19.setForeground(Color.white);
					panel2.add(label19, new TableLayoutConstraints(2, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

					//---- label20 ----
					label20.setText("*");
					label20.setFont(new Font("Arial", Font.BOLD, 12));
					label20.setHorizontalAlignment(SwingConstants.CENTER);
					panel2.add(label20, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

					//---- label21 ----
					label21.setText("Optional Fields");
					label21.setFont(new Font("Arial", Font.ITALIC, 12));
					label21.setForeground(Color.white);
					panel2.add(label21, new TableLayoutConstraints(2, 2, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

					//---- label22 ----
					label22.setText("disabled");
					label22.setFont(new Font("Arial", Font.BOLD, 12));
					label22.setHorizontalAlignment(SwingConstants.CENTER);
					label22.setForeground(Color.darkGray);
					panel2.add(label22, new TableLayoutConstraints(1, 3, 1, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

					//---- label23 ----
					label23.setText("Not applicable for chosen IS Std");
					label23.setFont(new Font("Arial", Font.ITALIC, 12));
					label23.setForeground(Color.white);
					panel2.add(label23, new TableLayoutConstraints(2, 3, 2, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));
				}
				panel3.add(panel2, new TableLayoutConstraints(3, 0, 4, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label2 ----
				label2.setText("<html>Category <font size=-2>(Optional)</font></html>");
				label2.setFont(new Font("Arial", Font.PLAIN, 14));
				panel3.add(label2, new TableLayoutConstraints(0, 2, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmbCat ----
				cmbCat.setFont(new Font("Microsoft Sans Serif", Font.BOLD, 14));
				cmbCat.setToolTipText("Category of the pump like Sewage Pump, etc");
				cmbCat.setEditable(true);
				panel3.add(cmbCat, new TableLayoutConstraints(1, 2, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- chkISIModel ----
				chkISIModel.setText("Non-ISI Model");
				chkISIModel.setFont(new Font("Arial", Font.PLAIN, 12));
				chkISIModel.setToolTipText("Check this if this pump type is a non-ISI model (may be a trial model) and has to be ignored while reporting to ISI");
				chkISIModel.addActionListener(e -> chkISIModelActionPerformed());
				panel3.add(chkISIModel, new TableLayoutConstraints(1, 3, 1, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmdNonISITol ----
				cmdNonISITol.setText("Non-ISI Performance Tolerance");
				cmdNonISITol.setFont(new Font("Arial", Font.PLAIN, 14));
				cmdNonISITol.setToolTipText("Configure performance tolerance values. Default is as of ISI standard if not configured.");
				cmdNonISITol.setMnemonic('N');
				cmdNonISITol.addActionListener(e -> cmdNonISITolActionPerformed());
				panel3.add(cmdNonISITol, new TableLayoutConstraints(2, 3, 2, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			pnlPump.add(panel3, new TableLayoutConstraints(0, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//======== pnlFeat ========
			{
				pnlFeat.setBorder(new TitledBorder(null, "Pump Features And Guaranteed Performance Values", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
					new Font("Arial", Font.BOLD, 12)));
				pnlFeat.setLayout(new TableLayout(new double[][] {
					{130, 70, 130, 70, 140, 70, 150, 70},
					{TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, 10, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED}}));
				((TableLayout)pnlFeat.getLayout()).setHGap(5);
				((TableLayout)pnlFeat.getLayout()).setVGap(5);

				//---- label16 ----
				label16.setText("Motor Model");
				label16.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlFeat.add(label16, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtMotType ----
				txtMotType.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				txtMotType.setBackground(Color.white);
				txtMotType.setCaretColor(Color.darkGray);
				txtMotType.setToolTipText("Motor Model");
				pnlFeat.add(txtMotType, new TableLayoutConstraints(1, 0, 3, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label4 ----
				label4.setText("Phase");
				label4.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlFeat.add(label4, new TableLayoutConstraints(4, 0, 4, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- cmbPhase ----
				cmbPhase.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				cmbPhase.addActionListener(e -> cmbPhase());
				pnlFeat.add(cmbPhase, new TableLayoutConstraints(5, 0, 5, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblSOHd2 ----
				lblSOHd2.setText("Connection");
				lblSOHd2.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlFeat.add(lblSOHd2, new TableLayoutConstraints(6, 0, 6, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- cmbCon ----
				cmbCon.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				cmbCon.setEditable(true);
				pnlFeat.add(cmbCon, new TableLayoutConstraints(7, 0, 7, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label79 ----
				label79.setText("KW");
				label79.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlFeat.add(label79, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtKw ----
				txtKw.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				txtKw.setBackground(Color.white);
				txtKw.setCaretColor(Color.darkGray);
				txtKw.setToolTipText("KW");
				pnlFeat.add(txtKw, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label80 ----
				label80.setText("HP");
				label80.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlFeat.add(label80, new TableLayoutConstraints(2, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtHp ----
				txtHp.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				txtHp.setBackground(Color.white);
				txtHp.setCaretColor(Color.darkGray);
				txtHp.setToolTipText("HP");
				pnlFeat.add(txtHp, new TableLayoutConstraints(3, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label24 ----
				label24.setText("Number Of Poles");
				label24.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlFeat.add(label24, new TableLayoutConstraints(4, 1, 4, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtPoles ----
				txtPoles.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				txtPoles.setBackground(Color.white);
				txtPoles.setCaretColor(Color.darkGray);
				txtPoles.setToolTipText("Number Of Poles");
				pnlFeat.add(txtPoles, new TableLayoutConstraints(5, 1, 5, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblIns ----
				lblIns.setText("Insulation Class");
				lblIns.setFont(new Font("Arial", Font.PLAIN, 12));
				lblIns.setEnabled(false);
				pnlFeat.add(lblIns, new TableLayoutConstraints(6, 1, 6, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- cmbIns ----
				cmbIns.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				cmbIns.setEnabled(false);
				cmbIns.setEditable(true);
				pnlFeat.add(cmbIns, new TableLayoutConstraints(7, 1, 7, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label10 ----
				label10.setText("Voltage (V)");
				label10.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlFeat.add(label10, new TableLayoutConstraints(0, 2, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtVolts ----
				txtVolts.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				txtVolts.setBackground(Color.white);
				txtVolts.setCaretColor(Color.darkGray);
				txtVolts.setToolTipText("HP");
				pnlFeat.add(txtVolts, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label11 ----
				label11.setText("Max Current (A)");
				label11.setFont(new Font("Arial", Font.BOLD, 14));
				label11.setForeground(new Color(0x009933));
				pnlFeat.add(label11, new TableLayoutConstraints(2, 2, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtAmps ----
				txtAmps.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				txtAmps.setBackground(Color.white);
				txtAmps.setCaretColor(Color.darkGray);
				txtAmps.setToolTipText("Max Current");
				txtAmps.setForeground(Color.blue);
				pnlFeat.add(txtAmps, new TableLayoutConstraints(3, 2, 3, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblMotIp ----
				lblMotIp.setText("Input Power (kW)");
				lblMotIp.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlFeat.add(lblMotIp, new TableLayoutConstraints(4, 2, 4, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtMotIp ----
				txtMotIp.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				txtMotIp.setToolTipText("Motor Input");
				txtMotIp.setCaretColor(Color.darkGray);
				pnlFeat.add(txtMotIp, new TableLayoutConstraints(5, 2, 5, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblMotEff ----
				lblMotEff.setText("Motor Efficiency (%)");
				lblMotEff.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlFeat.add(lblMotEff, new TableLayoutConstraints(6, 2, 6, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtMotEff ----
				txtMotEff.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				txtMotEff.setToolTipText("Motor Efficiency");
				txtMotEff.setCaretColor(Color.darkGray);
				pnlFeat.add(txtMotEff, new TableLayoutConstraints(7, 2, 7, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label17 ----
				label17.setText("Frequency (Hz)");
				label17.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlFeat.add(label17, new TableLayoutConstraints(0, 3, 0, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- txtFreq ----
				txtFreq.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				txtFreq.setBackground(Color.white);
				txtFreq.setCaretColor(Color.darkGray);
				txtFreq.setToolTipText("Frequency");
				pnlFeat.add(txtFreq, new TableLayoutConstraints(1, 3, 1, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label13 ----
				label13.setText("Speed (rpm)");
				label13.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlFeat.add(label13, new TableLayoutConstraints(2, 3, 2, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtSpeed ----
				txtSpeed.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				txtSpeed.setBackground(Color.white);
				txtSpeed.setCaretColor(Color.darkGray);
				txtSpeed.setToolTipText("Speed");
				pnlFeat.add(txtSpeed, new TableLayoutConstraints(3, 3, 3, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblCapRate ----
				lblCapRate.setText("Capacitor Rating (uF)");
				lblCapRate.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlFeat.add(lblCapRate, new TableLayoutConstraints(4, 3, 4, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtCapRat ----
				txtCapRat.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				txtCapRat.setBackground(Color.white);
				txtCapRat.setCaretColor(Color.darkGray);
				txtCapRat.setToolTipText("Capacitor Rating");
				pnlFeat.add(txtCapRat, new TableLayoutConstraints(5, 3, 5, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblCapVolt ----
				lblCapVolt.setText("Capacitor Voltage (V)");
				lblCapVolt.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlFeat.add(lblCapVolt, new TableLayoutConstraints(6, 3, 6, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtCapVol ----
				txtCapVol.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				txtCapVol.setBackground(Color.white);
				txtCapVol.setCaretColor(Color.darkGray);
				txtCapVol.setToolTipText("Capacitor Voltage");
				pnlFeat.add(txtCapVol, new TableLayoutConstraints(7, 3, 7, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				pnlFeat.add(separator1, new TableLayoutConstraints(0, 4, 7, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- lblSucSize ----
				lblSucSize.setText("Suction Size (mm)");
				lblSucSize.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlFeat.add(lblSucSize, new TableLayoutConstraints(0, 5, 0, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtSucSize ----
				txtSucSize.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				txtSucSize.setCaretColor(Color.darkGray);
				txtSucSize.setToolTipText("Suction Size");
				pnlFeat.add(txtSucSize, new TableLayoutConstraints(1, 5, 1, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label78 ----
				label78.setText("Delivery Size (mm)");
				label78.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlFeat.add(label78, new TableLayoutConstraints(2, 5, 2, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtDelSize ----
				txtDelSize.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				txtDelSize.setCaretColor(Color.darkGray);
				txtDelSize.setToolTipText("Delivery Size");
				pnlFeat.add(txtDelSize, new TableLayoutConstraints(3, 5, 3, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblGauge ----
				lblGauge.setText("Gauge Distance (m)");
				lblGauge.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlFeat.add(lblGauge, new TableLayoutConstraints(4, 5, 4, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtGauge ----
				txtGauge.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				txtGauge.setToolTipText("Gauge Distance");
				txtGauge.setCaretColor(Color.darkGray);
				pnlFeat.add(txtGauge, new TableLayoutConstraints(5, 5, 5, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblPresSize ----
				lblPresSize.setText("Pressure Size (mm)");
				lblPresSize.setFont(new Font("Arial", Font.PLAIN, 12));
				lblPresSize.setEnabled(false);
				pnlFeat.add(lblPresSize, new TableLayoutConstraints(6, 5, 6, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtPreSize ----
				txtPreSize.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				txtPreSize.setToolTipText("Gauge Distance");
				txtPreSize.setCaretColor(Color.darkGray);
				txtPreSize.setEnabled(false);
				pnlFeat.add(txtPreSize, new TableLayoutConstraints(7, 5, 7, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label6 ----
				label6.setText("Head Unit");
				label6.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlFeat.add(label6, new TableLayoutConstraints(0, 6, 0, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- cmbHdUnit ----
				cmbHdUnit.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				cmbHdUnit.addActionListener(e -> cmbHdUnitActionPerformed());
				pnlFeat.add(cmbHdUnit, new TableLayoutConstraints(1, 6, 1, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblHead ----
				lblHead.setText("Head ()");
				lblHead.setFont(new Font("Arial", Font.BOLD, 14));
				lblHead.setForeground(new Color(0x009933));
				pnlFeat.add(lblHead, new TableLayoutConstraints(2, 6, 2, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtHead ----
				txtHead.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				txtHead.setCaretColor(Color.darkGray);
				txtHead.setToolTipText("Head");
				txtHead.setForeground(Color.blue);
				pnlFeat.add(txtHead, new TableLayoutConstraints(3, 6, 3, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblHdLow ----
				lblHdLow.setText("Head Range (Low)");
				lblHdLow.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlFeat.add(lblHdLow, new TableLayoutConstraints(4, 6, 4, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtHeadL ----
				txtHeadL.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				txtHeadL.setCaretColor(Color.darkGray);
				txtHeadL.setToolTipText("Head Range (Low)");
				pnlFeat.add(txtHeadL, new TableLayoutConstraints(5, 6, 5, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblHdHigh ----
				lblHdHigh.setText("Head Range (High)");
				lblHdHigh.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlFeat.add(lblHdHigh, new TableLayoutConstraints(6, 6, 6, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtHeadH ----
				txtHeadH.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				txtHeadH.setCaretColor(Color.darkGray);
				txtHeadH.setToolTipText("Head Range (High)");
				pnlFeat.add(txtHeadH, new TableLayoutConstraints(7, 6, 7, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label3 ----
				label3.setText("Discharge Unit");
				label3.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlFeat.add(label3, new TableLayoutConstraints(0, 7, 0, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- cmbDisUnit ----
				cmbDisUnit.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				cmbDisUnit.addItemListener(e -> cmbDisUnitItemStateChanged());
				pnlFeat.add(cmbDisUnit, new TableLayoutConstraints(1, 7, 1, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblDis ----
				lblDis.setText("Discharge ()");
				lblDis.setFont(new Font("Arial", Font.BOLD, 14));
				lblDis.setForeground(new Color(0x009933));
				pnlFeat.add(lblDis, new TableLayoutConstraints(2, 7, 2, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- txtDis ----
				txtDis.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				txtDis.setCaretColor(Color.darkGray);
				txtDis.setToolTipText("Discharge");
				txtDis.setForeground(Color.blue);
				pnlFeat.add(txtDis, new TableLayoutConstraints(3, 7, 3, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label15 ----
				label15.setText("<html>Discharge At Low Head<sup>*</sup></html>");
				label15.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlFeat.add(label15, new TableLayoutConstraints(4, 7, 4, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtDisL ----
				txtDisL.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				txtDisL.setToolTipText("Discharge Range (Low) (Optional)");
				pnlFeat.add(txtDisL, new TableLayoutConstraints(5, 7, 5, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label18 ----
				label18.setText("<html>Discharge At High Head<sup>*</sup></hmtl>");
				label18.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlFeat.add(label18, new TableLayoutConstraints(6, 7, 6, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtDisH ----
				txtDisH.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				txtDisH.setToolTipText("Discharge Range (High) (Optional)");
				pnlFeat.add(txtDisH, new TableLayoutConstraints(7, 7, 7, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblSP ----
				lblSP.setText("Self Priming Time (sec)");
				lblSP.setFont(new Font("Arial", Font.PLAIN, 12));
				lblSP.setEnabled(false);
				pnlFeat.add(lblSP, new TableLayoutConstraints(0, 8, 0, 8, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtSP ----
				txtSP.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				txtSP.setToolTipText("Self Priming Time");
				txtSP.setCaretColor(Color.darkGray);
				txtSP.setEnabled(false);
				pnlFeat.add(txtSP, new TableLayoutConstraints(1, 8, 1, 8, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblSL ----
				lblSL.setText("Suction Lift (m)");
				lblSL.setFont(new Font("Arial", Font.PLAIN, 12));
				lblSL.setEnabled(false);
				pnlFeat.add(lblSL, new TableLayoutConstraints(2, 8, 2, 8, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtSL ----
				txtSL.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				txtSL.setToolTipText("Suction Lift");
				txtSL.setCaretColor(Color.darkGray);
				txtSL.setEnabled(false);
				pnlFeat.add(txtSL, new TableLayoutConstraints(3, 8, 3, 8, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblBrSz ----
				lblBrSz.setText("Min Bore Size (mm)");
				lblBrSz.setFont(new Font("Arial", Font.PLAIN, 12));
				lblBrSz.setEnabled(false);
				pnlFeat.add(lblBrSz, new TableLayoutConstraints(4, 8, 4, 8, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtBrSz ----
				txtBrSz.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				txtBrSz.setToolTipText("Minimum Bore Size");
				txtBrSz.setEnabled(false);
				pnlFeat.add(txtBrSz, new TableLayoutConstraints(5, 8, 5, 8, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblNoOfStag ----
				lblNoOfStag.setText("No. Of Stages");
				lblNoOfStag.setFont(new Font("Arial", Font.PLAIN, 12));
				lblNoOfStag.setEnabled(false);
				pnlFeat.add(lblNoOfStag, new TableLayoutConstraints(6, 8, 6, 8, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtNoOfStag ----
				txtNoOfStag.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				txtNoOfStag.setToolTipText("No. Of Stages");
				txtNoOfStag.setEnabled(false);
				pnlFeat.add(txtNoOfStag, new TableLayoutConstraints(7, 8, 7, 8, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblDLWL ----
				lblDLWL.setText("DLWL (m)");
				lblDLWL.setFont(new Font("Arial", Font.BOLD, 12));
				lblDLWL.setEnabled(false);
				lblDLWL.setForeground(new Color(0x009900));
				pnlFeat.add(lblDLWL, new TableLayoutConstraints(0, 9, 0, 9, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtDLWL ----
				txtDLWL.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				txtDLWL.setToolTipText("DLWL");
				txtDLWL.setCaretColor(Color.darkGray);
				txtDLWL.setEnabled(false);
				pnlFeat.add(txtDLWL, new TableLayoutConstraints(1, 9, 1, 9, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblSub ----
				lblSub.setText("Submergence (m)");
				lblSub.setFont(new Font("Arial", Font.PLAIN, 12));
				lblSub.setEnabled(false);
				pnlFeat.add(lblSub, new TableLayoutConstraints(2, 9, 2, 9, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtSub ----
				txtSub.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				txtSub.setToolTipText("Submergence");
				txtSub.setCaretColor(Color.darkGray);
				txtSub.setEnabled(false);
				pnlFeat.add(txtSub, new TableLayoutConstraints(3, 9, 3, 9, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblOprPress ----
				lblOprPress.setText("<html>Min. Opr. Pres. (kg/cm<sup>2</sup>)</html>");
				lblOprPress.setFont(new Font("Arial", Font.PLAIN, 12));
				lblOprPress.setEnabled(false);
				pnlFeat.add(lblOprPress, new TableLayoutConstraints(4, 9, 4, 9, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtOprPres ----
				txtOprPres.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				txtOprPres.setCaretColor(Color.darkGray);
				txtOprPres.setEnabled(false);
				txtOprPres.setToolTipText("Min. Operating Pressure");
				pnlFeat.add(txtOprPres, new TableLayoutConstraints(5, 9, 5, 9, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblOverallEff ----
				lblOverallEff.setText("Overall Efficiency (%)");
				lblOverallEff.setFont(new Font("Arial", Font.BOLD, 14));
				lblOverallEff.setForeground(new Color(0x009933));
				pnlFeat.add(lblOverallEff, new TableLayoutConstraints(6, 9, 6, 9, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtEff ----
				txtEff.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				txtEff.setCaretColor(Color.darkGray);
				txtEff.setToolTipText("Overall Efficiency");
				txtEff.setForeground(Color.blue);
				pnlFeat.add(txtEff, new TableLayoutConstraints(7, 9, 7, 9, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmdTestSet ----
				cmdTestSet.setText("Test Head Settings");
				cmdTestSet.setFont(new Font("Arial", Font.PLAIN, 14));
				cmdTestSet.setToolTipText("Configure list of heads tested for this model");
				cmdTestSet.setMnemonic('H');
				cmdTestSet.addActionListener(e -> cmdTestSetActionPerformed());
				pnlFeat.add(cmdTestSet, new TableLayoutConstraints(0, 10, 1, 10, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmdRoutineLimits ----
				cmdRoutineLimits.setText("Routine Test Lower and Upper Limits");
				cmdRoutineLimits.setFont(new Font("Arial", Font.PLAIN, 14));
				cmdRoutineLimits.setMnemonic('R');
				cmdRoutineLimits.setToolTipText("Configure set values (lower & upper limits) for routine tests");
				cmdRoutineLimits.addActionListener(e -> cmdRoutineLimitsActionPerformed());
				pnlFeat.add(cmdRoutineLimits, new TableLayoutConstraints(2, 10, 4, 10, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmdMotEff ----
				cmdMotEff.setText("Motor Efficiency Data Points");
				cmdMotEff.setFont(new Font("Arial", Font.PLAIN, 14));
				cmdMotEff.setToolTipText("Configure various motor inputs and corresponding outputs to derive pump efficiency");
				cmdMotEff.setMnemonic('M');
				cmdMotEff.addActionListener(e -> cmdMotEffActionPerformed());
				pnlFeat.add(cmdMotEff, new TableLayoutConstraints(5, 10, 7, 10, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			pnlPump.add(pnlFeat, new TableLayoutConstraints(0, 3, 3, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//======== pnlMV ========
			{
				pnlMV.setBorder(new TitledBorder(null, "Multi Voltage Test [Note: Required only if this pump is also tested in different voltages than rated voltage]", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
					new Font("Arial", Font.BOLD, 12)));
				pnlMV.setLayout(new TableLayout(new double[][] {
					{130, 90, 60, 60, TableLayout.FILL},
					{TableLayout.PREFERRED}}));
				((TableLayout)pnlMV.getLayout()).setHGap(5);
				((TableLayout)pnlMV.getLayout()).setVGap(5);

				//---- label12 ----
				label12.setText("<html>Low / High Voltages<sup>*</sup</html>");
				label12.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlMV.add(label12, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- cmbVolts ----
				cmbVolts.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				cmbVolts.setEditable(true);
				cmbVolts.setToolTipText("Add if pump is tested in any other voltages than rated voltages like low voltage, high voltage, etc... (Optional)");
				pnlMV.add(cmbVolts, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmdAddV ----
				cmdAddV.setText("+");
				cmdAddV.setMargin(new Insets(2, 8, 2, 8));
				cmdAddV.addActionListener(e -> cmdAddVActionPerformed());
				pnlMV.add(cmdAddV, new TableLayoutConstraints(2, 0, 2, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmdDelV ----
				cmdDelV.setText("-");
				cmdDelV.setMargin(new Insets(2, 8, 2, 8));
				cmdDelV.addActionListener(e -> cmdDelVActionPerformed());
				pnlMV.add(cmdDelV, new TableLayoutConstraints(3, 0, 3, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- chkMV ----
				chkMV.setText("Disabled Temporarily");
				chkMV.setFont(new Font("Arial", Font.PLAIN, 12));
				chkMV.setToolTipText("Check this if testing for multiple voltages need to be skipped temporarily");
				pnlMV.add(chkMV, new TableLayoutConstraints(4, 0, 4, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			pnlPump.add(pnlMV, new TableLayoutConstraints(0, 5, 3, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//======== pnlSNo ========
			{
				pnlSNo.setBorder(new TitledBorder(null, "Serial Number", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
					new Font("Arial", Font.BOLD, 12)));
				pnlSNo.setLayout(new TableLayout(new double[][] {
					{130, 227, 86, 227},
					{TableLayout.PREFERRED, TableLayout.PREFERRED}}));
				((TableLayout)pnlSNo.getLayout()).setHGap(5);
				((TableLayout)pnlSNo.getLayout()).setVGap(5);

				//---- lblPumpSNo ----
				lblPumpSNo.setText("Recent Pump SNo.");
				lblPumpSNo.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlSNo.add(lblPumpSNo, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblRecPumpSNo ----
				lblRecPumpSNo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
				lblRecPumpSNo.setOpaque(true);
				lblRecPumpSNo.setBackground(Color.lightGray);
				pnlSNo.add(lblRecPumpSNo, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblPumpSNoNext ----
				lblPumpSNoNext.setText("Next Will Be");
				lblPumpSNoNext.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlSNo.add(lblPumpSNoNext, new TableLayoutConstraints(2, 0, 2, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- txtNextPumpNo ----
				txtNextPumpNo.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				txtNextPumpNo.setToolTipText("Serial number of last tested pump");
				txtNextPumpNo.addKeyListener(new KeyAdapter() {
					@Override
					public void keyReleased(KeyEvent e) {
						txtNextPumpNoKeyReleased();
					}
				});
				pnlSNo.add(txtNextPumpNo, new TableLayoutConstraints(3, 0, 3, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblMsno ----
				lblMsno.setText("Recent Motor SNo.");
				lblMsno.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlSNo.add(lblMsno, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- lblRecMotSNo ----
				lblRecMotSNo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
				lblRecMotSNo.setOpaque(true);
				lblRecMotSNo.setBackground(Color.lightGray);
				pnlSNo.add(lblRecMotSNo, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblMotSNoNext ----
				lblMotSNoNext.setText("Next Will Be");
				lblMotSNoNext.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlSNo.add(lblMotSNoNext, new TableLayoutConstraints(2, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- txtNextMotNo ----
				txtNextMotNo.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				txtNextMotNo.setToolTipText("Serial number of last tested pump");
				txtNextMotNo.addKeyListener(new KeyAdapter() {
					@Override
					public void keyReleased(KeyEvent e) {
						txtNextMotNoKeyReleased();
					}
				});
				pnlSNo.add(txtNextMotNo, new TableLayoutConstraints(3, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			pnlPump.add(pnlSNo, new TableLayoutConstraints(0, 7, 3, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdAdd ----
			cmdAdd.setText("Add");
			cmdAdd.setFont(new Font("Arial", Font.PLAIN, 14));
			cmdAdd.setIcon(new ImageIcon(getClass().getResource("/img/Add.png")));
			cmdAdd.setToolTipText("Click on this to add new pump model and its features that you have entered above");
			cmdAdd.setMnemonic('A');
			cmdAdd.addActionListener(e -> cmdAddActionPerformed());
			pnlPump.add(cmdAdd, new TableLayoutConstraints(0, 8, 0, 8, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdUpdate ----
			cmdUpdate.setText("Update");
			cmdUpdate.setFont(new Font("Arial", Font.PLAIN, 14));
			cmdUpdate.setIcon(new ImageIcon(getClass().getResource("/img/save.PNG")));
			cmdUpdate.setToolTipText("Click on this to update selected pump model with the details you have entered above");
			cmdUpdate.setMnemonic('U');
			cmdUpdate.addActionListener(e -> cmdUpdateActionPerformed());
			pnlPump.add(cmdUpdate, new TableLayoutConstraints(1, 8, 1, 8, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdDel ----
			cmdDel.setText("Delete");
			cmdDel.setFont(new Font("Arial", Font.PLAIN, 14));
			cmdDel.setIcon(new ImageIcon(getClass().getResource("/img/delete.PNG")));
			cmdDel.setToolTipText("Click on this to delete the selected pump model");
			cmdDel.setMnemonic('D');
			cmdDel.addActionListener(e -> cmdDelActionPerformed());
			pnlPump.add(cmdDel, new TableLayoutConstraints(2, 8, 2, 8, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdClose ----
			cmdClose.setText("<html>Close&nbsp;&nbsp<font size=-2>[Esc]</html>");
			cmdClose.setFont(new Font("Arial", Font.PLAIN, 14));
			cmdClose.setIcon(new ImageIcon(getClass().getResource("/img/exit.PNG")));
			cmdClose.setToolTipText("Click on this to close this window");
			cmdClose.addActionListener(e -> cmdCloseActionPerformed());
			pnlPump.add(cmdClose, new TableLayoutConstraints(3, 8, 3, 8, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		}
		contentPane.add(pnlPump, new TableLayoutConstraints(2, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel pnlHead;
	private JLabel label5;
	private JTextField txtSearch;
	private JScrollPane scrlTypeList;
	private JTable tblTypeList;
	private JButton cmdChoose;
	private JPanel pnlPump;
	private JPanel panel3;
	private JLabel label8;
	private JTextField txtType;
	private JLabel label1;
	private JTextField txtDesc;
	private JPanel panel2;
	private JLabel label7;
	private JLabel label19;
	private JLabel label20;
	private JLabel label21;
	private JLabel label22;
	private JLabel label23;
	private JLabel label2;
	private JComboBox cmbCat;
	private JCheckBox chkISIModel;
	private JButton cmdNonISITol;
	private JPanel pnlFeat;
	private JLabel label16;
	private JTextField txtMotType;
	private JLabel label4;
	private JComboBox cmbPhase;
	private JLabel lblSOHd2;
	private JComboBox cmbCon;
	private JLabel label79;
	private JTextField txtKw;
	private JLabel label80;
	private JTextField txtHp;
	private JLabel label24;
	private JTextField txtPoles;
	private JLabel lblIns;
	private JComboBox cmbIns;
	private JLabel label10;
	private JTextField txtVolts;
	private JLabel label11;
	private JTextField txtAmps;
	private JLabel lblMotIp;
	private JTextField txtMotIp;
	private JLabel lblMotEff;
	private JTextField txtMotEff;
	private JLabel label17;
	private JTextField txtFreq;
	private JLabel label13;
	private JTextField txtSpeed;
	private JLabel lblCapRate;
	private JTextField txtCapRat;
	private JLabel lblCapVolt;
	private JTextField txtCapVol;
	private JSeparator separator1;
	private JLabel lblSucSize;
	private JTextField txtSucSize;
	private JLabel label78;
	private JTextField txtDelSize;
	private JLabel lblGauge;
	private JTextField txtGauge;
	private JLabel lblPresSize;
	private JTextField txtPreSize;
	private JLabel label6;
	private JComboBox cmbHdUnit;
	private JLabel lblHead;
	private JTextField txtHead;
	private JLabel lblHdLow;
	private JTextField txtHeadL;
	private JLabel lblHdHigh;
	private JTextField txtHeadH;
	private JLabel label3;
	private JComboBox cmbDisUnit;
	private JLabel lblDis;
	private JTextField txtDis;
	private JLabel label15;
	private JTextField txtDisL;
	private JLabel label18;
	private JTextField txtDisH;
	private JLabel lblSP;
	private JTextField txtSP;
	private JLabel lblSL;
	private JTextField txtSL;
	private JLabel lblBrSz;
	private JTextField txtBrSz;
	private JLabel lblNoOfStag;
	private JTextField txtNoOfStag;
	private JLabel lblDLWL;
	private JTextField txtDLWL;
	private JLabel lblSub;
	private JTextField txtSub;
	private JLabel lblOprPress;
	private JTextField txtOprPres;
	private JLabel lblOverallEff;
	private JTextField txtEff;
	private JButton cmdTestSet;
	private JButton cmdRoutineLimits;
	private JButton cmdMotEff;
	private JPanel pnlMV;
	private JLabel label12;
	private JComboBox cmbVolts;
	private JButton cmdAddV;
	private JButton cmdDelV;
	private JCheckBox chkMV;
	private JPanel pnlSNo;
	private JLabel lblPumpSNo;
	private JLabel lblRecPumpSNo;
	private JLabel lblPumpSNoNext;
	private JTextField txtNextPumpNo;
	private JLabel lblMsno;
	private JLabel lblRecMotSNo;
	private JLabel lblMotSNoNext;
	private JTextField txtNextMotNo;
	private JButton cmdAdd;
	private JButton cmdUpdate;
	private JButton cmdDel;
	private JButton cmdClose;
	// JFormDesigner - End of variables declaration  //GEN-END:variables

	// CUSTOM CODE - BEGIN
	private void customInit() {
		// CUSTOM CODE - BEGIN
		tblTypeList.setTableHeader(null);
		tblTypeList.setRowHeight(20);
		SelectionListener listener = new SelectionListener(tblTypeList, this);
		tblTypeList.getSelectionModel().addListSelectionListener(listener);
		tblTypeList.getColumnModel().getSelectionModel().addListSelectionListener(listener);
		scrlTypeList.setViewportView(tblTypeList);
		
		// adding list of conn
		cmbCon.addItem("");
		cmbCon.addItem("CSR");
		cmbCon.addItem("CSCR");
		cmbCon.addItem("Delta");
		cmbCon.addItem("Star");
		cmbCon.addItem("Star Delta");
		cmbCon.setSelectedIndex(0);
		
		// adding list of insulation classes
		cmbIns.addItem("");
		cmbIns.addItem("A");
		cmbIns.addItem("B");
		cmbIns.addItem("F");
		cmbIns.addItem("H");
		cmbIns.setSelectedIndex(0);
				
		// adding list of discharge units
		cmbDisUnit.addItem("lps");
		cmbDisUnit.addItem("lpm");
		cmbDisUnit.addItem("lph");
		cmbDisUnit.addItem("gps");
		cmbDisUnit.addItem("gpm");
		cmbDisUnit.addItem("gph");
		cmbDisUnit.addItem("<html>m<sup>3</sup>ph</html>");
		cmbDisUnit.setSelectedIndex(0);
		
		// adding list of head units
		cmbHdUnit.addItem("m");
		cmbHdUnit.addItem("ft");
		cmbHdUnit.addItem("bar");
		cmbHdUnit.setSelectedIndex(0);
		
		// default phase
		cmbPhase.addItem("Single");
		cmbPhase.addItem("Three");
		cmbPhase.setSelectedIndex(0);
		
		// ISI handling
		txtEff.setEnabled(true);
		lblSP.setEnabled(false);
		txtSP.setEnabled(false);
		lblSL.setEnabled(false);
		txtSL.setEnabled(false);
		
		if (Configuration.LAST_USED_ISSTD.startsWith("IS 8472:")) {
			lblMotIp.setForeground(new Color(0, 153, 51));
			lblMotIp.setFont(new Font("Arial", Font.BOLD, 14));
			txtMotIp.setForeground(Color.blue);
			lblSP.setEnabled(true);
			txtSP.setEnabled(true);
			lblSL.setEnabled(true);
			txtSL.setEnabled(true);
		}
		
		if(Configuration.LAST_USED_ISSTD.startsWith("IS 8472:") || Configuration.LAST_USED_ISSTD.startsWith("IS 9079:") || Configuration.LAST_USED_ISSTD.startsWith("IS 14220:")) {
			lblIns.setEnabled(true);
			cmbIns.setEnabled(true);
		} else {
			lblIns.setEnabled(false);
			cmbIns.setEnabled(false);
		}
		
		if(Configuration.LAST_USED_ISSTD.startsWith("IS 8034:") || Configuration.LAST_USED_ISSTD.startsWith("IS 14220:")) {
			lblSucSize.setEnabled(false);
			txtSucSize.setEnabled(false);
			lblBrSz.setEnabled(true);
			txtBrSz.setEnabled(true);
			lblNoOfStag.setEnabled(true);
			txtNoOfStag.setEnabled(true);
		} else {
			lblSucSize.setEnabled(true);
			txtSucSize.setEnabled(true);
			lblBrSz.setEnabled(false);
			txtBrSz.setEnabled(false);
			lblNoOfStag.setEnabled(false);
			txtNoOfStag.setEnabled(false);
		}
		
		if (Configuration.LAST_USED_ISSTD.startsWith("IS 12225")) {
			lblOverallEff.setEnabled(false);
			txtEff.setEnabled(false);
			lblPresSize.setEnabled(true);
			txtPreSize.setEnabled(true);
			lblDLWL.setEnabled(true);
			txtDLWL.setEnabled(true);
			lblSub.setEnabled(true);
			txtSub.setEnabled(true);
			lblOprPress.setEnabled(true);
			txtOprPres.setEnabled(true);
			
			lblHdLow.setText("DLWL Range (Low)");
			lblHdHigh.setText("DLWL Range (High)");
		} else {
			lblPresSize.setEnabled(false);
			txtPreSize.setEnabled(false);
			lblDLWL.setEnabled(false);
			txtDLWL.setEnabled(false);
			lblSub.setEnabled(false);
			txtSub.setEnabled(false);
			lblOprPress.setEnabled(false);
			txtOprPres.setEnabled(false);
		}
		
		// motor eff is required if pump eff needs to be derived in report or for certain IS
		if (Configuration.REP_SHOW_PUMP_EFF.equals("1") || Configuration.LAST_USED_ISSTD.startsWith("IS 8034:") || Configuration.LAST_USED_ISSTD.startsWith("IS 14220:") || Configuration.LAST_USED_ISSTD.startsWith("IS 6595:")) {
			lblMotEff.setEnabled(true);
			txtMotEff.setEnabled(true);
			cmdMotEff.setEnabled(true);
		} else {
			lblMotEff.setEnabled(false);
			txtMotEff.setEnabled(false);
			cmdMotEff.setEnabled(false);
		}
		
		// disable sno section if required
		if (Configuration.IS_SNO_GLOBAL.equals("YES") || Configuration.IS_BARCODE_USED.equals("YES")) {
				pnlSNo.setBackground(Color.lightGray);
				lblPumpSNoNext.setVisible(false);
				lblMotSNoNext.setVisible(false);
				txtNextPumpNo.setVisible(false);
				txtNextMotNo.setVisible(false);
			if (Configuration.IS_BARCODE_USED.equals("YES")) {
				pnlSNo.setBorder(new TitledBorder(null, "Serial Number [Note: This section is not applicable as serial number is scanned from barcode]", TitledBorder.LEADING, TitledBorder.TOP,
						new Font("Arial", Font.BOLD, 12), Color.black));
			} else {
				pnlSNo.setBorder(new TitledBorder(null, "Serial Number [Note: This section is not applicable as serial number is generated globally]", TitledBorder.LEADING, TitledBorder.TOP,
						new Font("Arial", Font.BOLD, 12), Color.black));
			}
		} else {
			findNextPumpNo(false);
			findNextMotorNo();
		}
		
		// disable buttons if user does not have pump access
		if (Configuration.USER_HAS_PUMP_ACCESS.equals("0")) {
			cmdAdd.setEnabled(false);
			cmdUpdate.setEnabled(false);
		}
		// disable delete if user does not have admin access
		if (Configuration.USER_IS_ADMIN.equals("0")) {
			cmdDel.setEnabled(false);
		}
		
		if (Configuration.IS_MULTI_DB.equals("YES")) {
			pnlHead.setBorder(new TitledBorder(null, "Existing Pump Models [ASSEMBLY LINE:" + Configuration.LINE_NAME + "]", TitledBorder.CENTER, TitledBorder.TOP,
				new Font("Arial", Font.BOLD, 14), Color.blue));
		}
		
		associateFunctionKeys();
	}
	
	// function to set command button label based on mot eff data points configuration
	public void refreshMotEffDataPointStatus() {
		MyResultSet res;
		try {
			res = db.executeQuery("select * from " + Configuration.MOT_EFF_DATA_POINTS + " where pump_type_id='" + selPumpTypeId + "'");
			if (res.isRowsAvail()) {
				cmdMotEff.setText("<html>Motor Efficiency Data Points<style='color:green;'><font size=-1> [configured]</html>");
			} else {
				cmdMotEff.setText("<html>Motor Efficiency Data Points<style='color:orange;'><font size=-1> [pending]</html>");
			}
		} catch (SQLException e) {
			// ignore
		}
	}
	
	class SelectionListener implements ListSelectionListener {
		  JTable table;
		  JDialog pr;

		  SelectionListener(JTable table, PumpType parent) {
		    this.table = table;
		    this.pr = parent;
		    
		  }
		  public void valueChanged(ListSelectionEvent le) {
			  try {
				  String selType = table.getValueAt(table.getSelectedRow(), 0).toString();
				  
			// fetch corresponding record for the table
				try {
					MyResultSet res = db.executeQuery("select * from " + Configuration.PUMPTYPE + " where type='" + selType + "'");
					
					if (res.next()) {
						selPumpTypeId = res.getString("pump_type_id");
						selPumpType = res.getString("type");
						txtType.setText(res.getString("type"));
						cmbCat.setSelectedItem(res.getString("category"));
						txtDesc.setText(res.getString("desc"));
						chkISIModel.setSelected(res.getString("non_isi_model").equals("Y"));
						chkISIModelActionPerformed();
						txtDelSize.setText(res.getString("delivery_size"));
						txtSucSize.setText(res.getString("suction_size"));
						txtMotEff.setText(res.getString("mot_eff"));
						cmbCon.setSelectedItem(res.getString("conn"));
						cmbHdUnit.setSelectedItem(res.getString("head_unit"));
						txtHead.setText(res.getString("head"));
						txtMotIp.setText(res.getString("mot_ip"));
						cmbDisUnit.setSelectedItem(res.getString("discharge_unit"));
						if (res.getString("discharge_unit").contains("sup")) {
							cmbDisUnit.setSelectedItem("<html>" + res.getString("discharge_unit") + "</html>");
						}
						txtDis.setText(res.getString("discharge"));
						txtDisL.setText(res.getString("discharge_low"));
						txtDisH.setText(res.getString("discharge_high"));
						txtEff.setText(res.getString("overall_eff"));
						txtHeadL.setText(res.getString("head_low"));
						txtHeadH.setText(res.getString("head_high"));
						txtGauge.setText(res.getString("gauge_distance"));
						
						txtMotType.setText(res.getString("mot_type"));
						txtKw.setText(res.getString("kw"));
						txtHp.setText(res.getString("hp"));
						txtVolts.setText(res.getString("volts"));
						selRatedV = txtVolts.getText();
						// add available volts
						cmbVolts.removeAllItems();
						if (!res.getString("other_volts").isEmpty()) {
							for(String tmpV : res.getString("other_volts").split(",")) {
								cmbVolts.addItem(tmpV);
							}
							cmbVolts.setSelectedIndex(0);
						}
						chkMV.setSelected(res.getString("other_volts_disabled").equals("Y"));
						cmbPhase.setSelectedItem(res.getString("phase"));
						txtAmps.setText(res.getString("amps"));
						txtSpeed.setText(res.getString("speed"));
						txtFreq.setText(res.getString("freq"));
						
						txtBrSz.setText(res.getString("bore_size"));
						txtNoOfStag.setText(res.getString("no_of_stage"));
						cmbIns.setSelectedItem(res.getString("ins_class"));
						
						txtSP.setText(res.getString("self_priming_time"));
						txtSL.setText(res.getString("suction_lift"));
						
						txtPreSize.setText(res.getString("pres_size"));
						txtDLWL.setText(res.getString("dlwl"));
						txtSub.setText(res.getString("submergence"));
						txtOprPres.setText(res.getString("min_op_pres"));
						
						txtPoles.setText(res.getString("no_of_poles"));
						txtCapRat.setText(res.getString("cap_rating"));
						txtCapVol.setText(res.getString("cap_volt"));
						
						lblRecPumpSNo.setText(res.getString("recent_pump_sno").isEmpty() ? "0" : res.getString("recent_pump_sno"));
						lblRecMotSNo.setText(res.getString("recent_motor_sno").isEmpty() ? "0" : res.getString("recent_motor_sno"));
						
						if (Configuration.IS_SNO_GLOBAL.equals("NO") && Configuration.IS_BARCODE_USED.equals("NO")) {
							findNextPumpNo(false);
							findNextMotorNo();
						}
						
						// load test heads
						loadTestHeads();
						
						chosenTypeTestFreq = res.getString("type_test_freq");
						chosenAutoValveType = res.getString("auto_valve_type");
						
						// status of data points configuration
						if (cmdMotEff.isEnabled()) {
							refreshMotEffDataPointStatus();
						}
					}
				} catch (Exception sqle) {
					sqle.printStackTrace();
					JOptionPane.showMessageDialog(new JDialog(), "Error reading the pump type:" + sqle.getMessage());
					return;
				}
			  } catch (ArrayIndexOutOfBoundsException ex) {
				  // just ignore it
			  }
		  }
	}
	// other variables
	private PumpView mainFormRef = null;
	private String lastPumpTypeId = "";
	private String lastPumpType = "";
	private String selRatedV = "";
	private String selPumpTypeId = "";
	private String selPumpType = "";
	
	private Database db = null;
	private Integer defSelRow = 0;
	private String chosenTypeTestFreq = "20";
	private String chosenAutoValveType = "P";
	
	private LinkedHashMap<String, String> testListAll = new LinkedHashMap<String, String>();
	private LinkedHashMap<String, String> testListRt = new LinkedHashMap<String, String>();
	private LinkedHashMap<String, String> testListType = new LinkedHashMap<String, String>();
	
	// CUSTOM CODE - END

}
