package doer.pv;

import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.event.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import javax.swing.*;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import doer.io.Database;
import doer.io.MyResultSet;
import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

public class TestHeads extends JDialog {
	public TestHeads(JDialog owner, String pumpTypeId, String pumpType) {
		super(owner);
		frmMain = (PumpType) owner;
		curPumpTypeId = pumpTypeId;
		
		initComponents();
		loadSettings();
		
		// set the panel header
		pnlHead.setBorder(new TitledBorder(null, "Test Head Settings [" + pumpType + "]", TitledBorder.CENTER, TitledBorder.TOP,
				new Font("Arial", Font.BOLD, 14), Color.blue));
	}
	
	public TestHeads(Dialog owner) {
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

	// function to load settings
	private void loadSettings() {
		try {
			db = Database.getInstance();
			
			String order = " order by a.seq";
			MyResultSet res = db.executeQuery("select * from " + Configuration.TESTNAMES_ALL + " a where a.pump_type_id='" + curPumpTypeId + "'" + order);
		
			int r=0;
			if (res.isRowsAvail()) {
				DefaultTableModel defModel = (DefaultTableModel) tblTestAll.getModel();
				while (res.next()) {
					defModel.addRow( new Object[] {"", "", ""});
					tblTestAll.setValueAt(res.getString("code"),r,0);
					tblTestAll.setValueAt(res.getString("name"),r,1);
					if (tblTestAll.getColumnCount() == 3) {
						tblTestAll.setValueAt(res.getString("head").isEmpty()?"Unknown":dotTwo.format(res.getFloat("head")),r,2);
					}
					r++;
				}
			}
			
			// routine test
			res = db.executeQuery("select a.code as code,b.name as name,a.auto_valve_value as auto_valve_value,a.auto_valve_tol as auto_valve_tol from " + Configuration.TESTNAMES_ROUTINE + " a join " + Configuration.TESTNAMES_ALL + " b on b.pump_type_id=a.pump_type_id and b.code = a.code where a.pump_type_id = '" + curPumpTypeId + "'" + order);

			r=0;
			if (res.isRowsAvail()) {
				DefaultTableModel defModel = (DefaultTableModel) tblTestRt.getModel();
				while (tblTestRt.getRowCount() > 0) {
					defModel.removeRow(0);
				}
				while (res.next()) {
					defModel.addRow( new Object[] {"", "", "", ""});
					tblTestRt.setValueAt(res.getString("code"),r,0);
					tblTestRt.setValueAt(res.getString("name"),r,1);
					if (Configuration.HEAD_ADJUSTMENT.equals("A")) {
						tblTestRt.setValueAt(res.getString("auto_valve_value"),r,2);
						tblTestRt.setValueAt(res.getString("auto_valve_tol"),r,3);
					}
					r++;
				}
				oldRtCount = r--;
			}
			
			// type test
			res = db.executeQuery("select a.code as code,b.name as name,a.auto_valve_value as auto_valve_value,a.auto_valve_tol as auto_valve_tol from " + Configuration.TESTNAMES_TYPE + " a join " + Configuration.TESTNAMES_ALL + " b on b.pump_type_id=a.pump_type_id and b.code = a.code where a.pump_type_id = '" + curPumpTypeId + "'" + order);
			r=0;
			if (res.isRowsAvail()) {
				DefaultTableModel defModel = (DefaultTableModel) tblTestType.getModel();
				while (tblTestType.getRowCount() > 0) {
					defModel.removeRow(0);
				}
				while (res.next()) {
					defModel.addRow( new Object[] {"", "", "", ""});
					tblTestType.setValueAt(res.getString("code"),r,0);
					tblTestType.setValueAt(res.getString("name"),r,1);
					if (Configuration.HEAD_ADJUSTMENT.equals("A")) {
						tblTestType.setValueAt(res.getString("auto_valve_value"),r,2);
						tblTestType.setValueAt(res.getString("auto_valve_tol"),r,3);
					}
					r++;
				}
				oldTypeCount = r--;
			}
			
			// others
			res = db.executeQuery("select type_test_freq, auto_valve_type, discharge_unit, head_unit from " + Configuration.PUMPTYPE + " where pump_type_id='" + curPumpTypeId + "'");
			if (res.next()) {
				// type test freq
				txtTypeInterval.setText(res.getString("type_test_freq"));
				
				// auto valve control
				if (Configuration.HEAD_ADJUSTMENT.equals("A")) {
					curAutoValveType = res.getString("auto_valve_type");
					if (curAutoValveType.equals("H")) {
						optFixedHead.setSelected(true);
					} else if (curAutoValveType.equals("F")) {
						optFixedFlow.setSelected(true);
					} else {
						optFixedPer.setSelected(true);
					}
					curDisUnit = res.getString("discharge_unit");
					curHeadUnit = res.getString("head_unit");
					optFixedHeadActionPerformed();
				} else {
					pnlValve.setVisible(false);
					// remove unwanted columns
					JTableHeader tmpHdr = tblTestRt.getTableHeader();
					tmpHdr.getColumnModel().removeColumn(tmpHdr.getColumnModel().getColumn(2));
					tmpHdr.getColumnModel().removeColumn(tmpHdr.getColumnModel().getColumn(2));
					tmpHdr = tblTestType.getTableHeader();
					tmpHdr.getColumnModel().removeColumn(tmpHdr.getColumnModel().getColumn(2));
					tmpHdr.getColumnModel().removeColumn(tmpHdr.getColumnModel().getColumn(2));
				}
			}
		} catch (Exception sqle) {
			JOptionPane.showMessageDialog(new JDialog(), "Error loading test head details:" + sqle.getMessage());
			sqle.printStackTrace();
			return;
		}
	}
	
	// function to update the config values
	private void saveSettings() {
		
		// save the changes in test names and its sequences if any
		ArrayList<String> codeList = new ArrayList<String>();
		try {
			for (int i=0; i<tblTestAll.getRowCount(); i++) {
				try {
					if (!tblTestAll.getValueAt(i, 0).toString().isEmpty()) {
						codeList.add(tblTestAll.getValueAt(i, 0).toString());
						db.executeUpdate("insert into " + Configuration.TESTNAMES_ALL + "(pump_type_id, code, name, head, seq) values('" + curPumpTypeId + "','" + tblTestAll.getValueAt(i, 0).toString() + "','" + tblTestAll.getValueAt(i, 1).toString() + "',''," + i + ")");
					}
				} catch (SQLException se) {
					if (se.getMessage().contains("not unique")) {
						db.executeUpdate("update " + Configuration.TESTNAMES_ALL + " set name='" + tblTestAll.getValueAt(i, 1).toString() + "',seq=" + i + " where pump_type_id='" + curPumpTypeId + "' and code='" + tblTestAll.getValueAt(i, 0).toString() + "'");
					} else {
						throw se;
					}
				}
			}
			db.executeUpdate("delete from " + Configuration.TESTNAMES_ALL + " where pump_type_id='" + curPumpTypeId + "' and code not in (" + strJoin(codeList.toArray(), ",") + ")");
			
			codeList.clear();
			newRtCount = 0;
			for (int i=0; i<tblTestRt.getRowCount(); i++) {
				try {
					if (!tblTestRt.getValueAt(i, 0).toString().isEmpty()) {
						newRtCount++;
						codeList.add(tblTestRt.getValueAt(i, 0).toString());
						if (Configuration.HEAD_ADJUSTMENT.equals("A")) {
							db.executeUpdate("insert into " + Configuration.TESTNAMES_ROUTINE + "(pump_type_id, code, seq, auto_valve_value, auto_valve_tol) values('" + curPumpTypeId + "','" + tblTestRt.getValueAt(i, 0).toString() + "','" +  i + "','" + tblTestRt.getValueAt(i, 2).toString() + "','" + tblTestRt.getValueAt(i, 3).toString() + "')");
						} else {
							db.executeUpdate("insert into " + Configuration.TESTNAMES_ROUTINE + "(pump_type_id, code, seq) values('" + curPumpTypeId + "','" + tblTestRt.getValueAt(i, 0).toString() + "','" +  i + "')");
						}
					}
				} catch (SQLException se) {
					if (se.getMessage().contains("not unique")) {
						if (Configuration.HEAD_ADJUSTMENT.equals("A")) {
							db.executeUpdate("update " + Configuration.TESTNAMES_ROUTINE + " set seq=" + i + ",auto_valve_value='" + tblTestRt.getValueAt(i, 2).toString() + "',auto_valve_tol='" + tblTestRt.getValueAt(i, 3).toString() + "' where pump_type_id='" + curPumpTypeId + "' and code='" + tblTestRt.getValueAt(i, 0).toString() + "'");
						} else {
							db.executeUpdate("update " + Configuration.TESTNAMES_ROUTINE + " set seq=" + i + " where pump_type_id='" + curPumpTypeId + "' and code='" + tblTestRt.getValueAt(i, 0).toString() + "'");
						}
					} else {
						throw se;
					}
				}
			}
			db.executeUpdate("delete from " + Configuration.TESTNAMES_ROUTINE + " where pump_type_id='" + curPumpTypeId + "' and code not in (" + strJoin(codeList.toArray(), ",") + ")");
			
			codeList.clear();
			newTypeCount = 0;
			for (int i=0; i<tblTestType.getRowCount(); i++) {
				try {
					if (!tblTestType.getValueAt(i, 0).toString().isEmpty()) {
						newTypeCount++;
						codeList.add(tblTestType.getValueAt(i, 0).toString());
						if (Configuration.HEAD_ADJUSTMENT.equals("A")) {
							db.executeUpdate("insert into " + Configuration.TESTNAMES_TYPE + "(pump_type_id, code, seq, auto_valve_value, auto_valve_tol) values('" + curPumpTypeId + "','" + tblTestType.getValueAt(i, 0).toString() + "','" +  i + "','" + tblTestType.getValueAt(i, 2).toString() + "','" + tblTestType.getValueAt(i, 3).toString() + "')");
						} else {
							db.executeUpdate("insert into " + Configuration.TESTNAMES_TYPE + "(pump_type_id, code, seq)  values('" + curPumpTypeId + "','" + tblTestType.getValueAt(i, 0).toString() + "'," + i + ")");
						}
					}
				} catch (SQLException se) {
					if (se.getMessage().contains("not unique")) {
						if (Configuration.HEAD_ADJUSTMENT.equals("A")) {
							db.executeUpdate("update " + Configuration.TESTNAMES_TYPE + " set seq=" + i + ",auto_valve_value='" + tblTestType.getValueAt(i, 2).toString() + "',auto_valve_tol='" + tblTestType.getValueAt(i, 3).toString() + "' where pump_type_id='" + curPumpTypeId + "' and code='" + tblTestType.getValueAt(i, 0).toString() + "'");
						} else {
							db.executeUpdate("update " + Configuration.TESTNAMES_TYPE + " set seq=" + i + " where pump_type_id='" + curPumpTypeId + "' and code='" + tblTestType.getValueAt(i, 0).toString() + "'");
						}
					} else {
						throw se;
					}
				}
			}
			db.executeUpdate("delete from " + Configuration.TESTNAMES_TYPE + " where pump_type_id='" + curPumpTypeId + "' and code not in (" + strJoin(codeList.toArray(), ",") + ")");
			
			// save the type test freq & valve control type
			String tmpControl = optFixedHead.isSelected() ? "H" : optFixedFlow.isSelected() ? "F" : "P";
			db.executeUpdate("update " + Configuration.PUMPTYPE + " set type_test_freq='" + txtTypeInterval.getText().trim() + "', auto_valve_type='" + tmpControl +"' where pump_type_id = '" + curPumpTypeId + "'");
			
		} catch (Exception sqle) {
			sqle.printStackTrace();
				JOptionPane.showMessageDialog(this, "Error updating test head details:" + sqle.getMessage());
		}
		// refresh the list in pump type form
		frmMain.loadTestHeads();
		frmMain.updatePumpType("update");
	}

	private void cmdSaveActionPerformed() {
		// validate
		if (tblTestRt.getRowCount() == 0 && tblTestType.getRowCount() == 0) {
			JOptionPane.showMessageDialog(this, "Please select one or more routine or type tests from available tests. Both list should not be empty");
			return;
		}
		try {
			if (Integer.valueOf(txtTypeInterval.getText()) < 0) {
				throw new NumberFormatException();
			}
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(this, "Please enter valid number for type test interval", "Error", JOptionPane.ERROR_MESSAGE);
			txtTypeInterval.requestFocusInWindow();
			return;
		}
		
		if (tblTestRt.getRowCount() == 0 && Integer.valueOf(txtTypeInterval.getText()) <= 0) {
			JOptionPane.showMessageDialog(this, "The value of type test interval has to be grater than 0 as routine test list is empty", "Error", JOptionPane.ERROR_MESSAGE);
			txtTypeInterval.requestFocusInWindow();
			return;
		}
		
		if (tblTestRt.getRowCount() == 0 && Integer.valueOf(txtTypeInterval.getText()) > 1) {
			JOptionPane.showMessageDialog(this, "No routine test is configured, but type test is configured as 1 in every " + txtTypeInterval.getText() + " pumps.\n" +
											"Which means routine tests are performed in different assembly line. Hence their serial numbers will be skipped in this line.", "Alert", JOptionPane.WARNING_MESSAGE);
		}
		
		if (tblTestType.getRowCount() == 0 && Integer.valueOf(txtTypeInterval.getText()) != 0) {
			JOptionPane.showMessageDialog(this, "The value of type test interval has to be 0 as type test list is empty", "Error", JOptionPane.ERROR_MESSAGE);
			txtTypeInterval.requestFocusInWindow();
			return;
		}
		
		// confirm
		int res = JOptionPane.showConfirmDialog(this, "Save the changes?\nWARNING: Any changes made in test order will be effective immediately");
		if ( res != 0 ) {
			return;
		}
		
		this.setCursor(waitCursor);
		
		saveSettings();
		
		this.setCursor(defCursor);
		JOptionPane.showMessageDialog(this, "Changes are saved successfully!"); 
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

	private void cmdExitActionPerformed() {
		this.setVisible(false);
		thisWindowClosing();
	}

	private void cmdRightActionPerformed() {
		int sourceRow = tblTestAll.getSelectedRow();
		if (sourceRow < 0) {
			JOptionPane.showMessageDialog(this, "Please select a test from list of available tests");
			return;
		}
		DefaultTableModel defModel = (DefaultTableModel) tblTestRt.getModel();
		defModel.addRow( new Object[] {"", ""});
		
		// shift the content to give insert feeling
		int selRow = tblTestRt.getSelectedRow();
		if (selRow < 0) {
			selRow = tblTestRt.getRowCount()-1;
		} else {
			for(int i=tblTestRt.getRowCount()-1; i>selRow; i--) {
				tblTestRt.setValueAt(tblTestRt.getValueAt(i-1,0), i, 0);
				tblTestRt.setValueAt(tblTestRt.getValueAt(i-1,1), i, 1);
			}
		}
				
		tblTestRt.setValueAt(tblTestAll.getValueAt(sourceRow, 0).toString(),selRow, 0);
		tblTestRt.setValueAt(tblTestAll.getValueAt(sourceRow, 1).toString(),selRow, 1);
		
	}

	private void cmdLeftActionPerformed() {
		int sourceRow = tblTestRt.getSelectedRow();
		if (sourceRow < 0) {
			JOptionPane.showMessageDialog(this, "Please select a routine test to remove");
			return;
		}
		DefaultTableModel defModel = (DefaultTableModel) tblTestRt.getModel();
		defModel.removeRow(sourceRow);
	}

	private void thisWindowClosing() {
	}

	private void cmdRight2ActionPerformed() {
		int sourceRow = tblTestAll.getSelectedRow();
		if (sourceRow < 0) {
			JOptionPane.showMessageDialog(this, "Please select a test from list of available tests");
			return;
		}
		DefaultTableModel defModel = (DefaultTableModel) tblTestType.getModel();
		defModel.addRow( new Object[] {"", ""});
		
		// shift the content to give insert feeling
		int selRow = tblTestType.getSelectedRow();
		if (selRow < 0) {
			selRow = tblTestType.getRowCount()-1;
		} else {
			for(int i=tblTestType.getRowCount()-1; i>selRow; i--) {
				tblTestType.setValueAt(tblTestType.getValueAt(i-1,0), i, 0);
				tblTestType.setValueAt(tblTestType.getValueAt(i-1,1), i, 1);
			}
		}
		tblTestType.setValueAt(tblTestAll.getValueAt(sourceRow, 0).toString(),selRow, 0);
		tblTestType.setValueAt(tblTestAll.getValueAt(sourceRow, 1).toString(),selRow, 1);
	}

	private void cmdLeft2ActionPerformed() {
		int sourceRow = tblTestType.getSelectedRow();
		if (sourceRow < 0) {
			JOptionPane.showMessageDialog(this, "Please select a type test to remove");
			return;
		}
		DefaultTableModel defModel = (DefaultTableModel) tblTestType.getModel();
		defModel.removeRow(sourceRow);
	}

	private void cmdAddRowActionPerformed() {
		DefaultTableModel defModel = (DefaultTableModel) tblTestAll.getModel();
		defModel.addRow( new Object[] {"", "",""});
		
		// shift the content to give insert feeling
		int selRow = tblTestAll.getSelectedRow();
		if (selRow < 0) {
			selRow = tblTestAll.getRowCount()-1;
		} else {
			for(int i=tblTestAll.getRowCount()-1; i>selRow; i--) {
				tblTestAll.setValueAt(tblTestAll.getValueAt(i-1,0), i, 0);
				tblTestAll.setValueAt(tblTestAll.getValueAt(i-1,1), i, 1);
				if (tblTestAll.getColumnCount() == 3) { // head
					tblTestAll.setValueAt(tblTestAll.getValueAt(i-1,2), i, 2);
				}
			}
			tblTestAll.setValueAt("", selRow, 0);
			tblTestAll.setValueAt("", selRow, 1);
			if (tblTestAll.getColumnCount() == 3) { // head
				tblTestAll.setValueAt("", selRow, 2);
			}
		}
		tblTestAll.setRowSelectionInterval(selRow, selRow);
		tblTestAll.requestFocusInWindow();
	}

	private void cmdRemRowActionPerformed() {
		int selRow = tblTestAll.getSelectedRow();
		if(selRow < 0) {
			JOptionPane.showMessageDialog(this, "Select a test from above list to remove");
		} else {
			if (tblTestAll.getValueAt(selRow, 0).toString().equals("FO") || tblTestAll.getValueAt(selRow, 0).toString().equals("SO")) {
				JOptionPane.showMessageDialog(this, "Sorry, it is not possible remove two default tests (full open and shut-off)");
			} else {
				int res = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove this test?\nWARNING: This will also remove the test from routine & type tests list once saved");
				if (res == 0) {
					DefaultTableModel defModel = (DefaultTableModel) tblTestAll.getModel();
					defModel.removeRow(selRow);
				}
			}
		}
	}

	private void cmdRevTestActionPerformed() {
		// reverse the test order
		String tmpCol0 = "";
		String tmpCol1 = "";
		String tmpCol2 = "";
		// 1. all
		int rows = tblTestAll.getRowCount();
		for(int i=0; i<rows/2; i++) {
			tmpCol0 = tblTestAll.getValueAt(i, 0).toString();
			tmpCol1 = tblTestAll.getValueAt(i, 1).toString();
			tblTestAll.setValueAt(tblTestAll.getValueAt(rows-1-i, 0), i, 0);
			tblTestAll.setValueAt(tblTestAll.getValueAt(rows-1-i, 1), i, 1);
			
			tblTestAll.setValueAt(tmpCol0, rows-1-i, 0);
			tblTestAll.setValueAt(tmpCol1, rows-1-i, 1);
			
			if (tblTestAll.getColumnCount() == 3) {
				tmpCol2 = tblTestAll.getValueAt(i, 2).toString();
				tblTestAll.setValueAt(tblTestAll.getValueAt(rows-1-i, 2), i, 2);
				tblTestAll.setValueAt(tmpCol2, rows-1-i, 2);
			}
		}
		// 2. routine
		rows = tblTestRt.getRowCount();
		for(int i=0; i<rows/2; i++) {
			tmpCol0 = tblTestRt.getValueAt(i, 0).toString();
			tmpCol1 = tblTestRt.getValueAt(i, 1).toString();
			tblTestRt.setValueAt(tblTestRt.getValueAt(rows-1-i, 0), i, 0);
			tblTestRt.setValueAt(tblTestRt.getValueAt(rows-1-i, 1), i, 1);
			
			tblTestRt.setValueAt(tmpCol0, rows-1-i, 0);
			tblTestRt.setValueAt(tmpCol1, rows-1-i, 1);
		}
		
		// 3. type
		rows = tblTestType.getRowCount();
		for(int i=0; i<rows/2; i++) {
			tmpCol0 = tblTestType.getValueAt(i, 0).toString();
			tmpCol1 = tblTestType.getValueAt(i, 1).toString();
			tblTestType.setValueAt(tblTestType.getValueAt(rows-1-i, 0), i, 0);
			tblTestType.setValueAt(tblTestType.getValueAt(rows-1-i, 1), i, 1);
			
			tblTestType.setValueAt(tmpCol0, rows-1-i, 0);
			tblTestType.setValueAt(tmpCol1, rows-1-i, 1);
		}
		orderChanged = true;
	}

	private void optFixedHeadActionPerformed() {
		// change the heading of tables
		String curSel = optFixedHead.isSelected() ? "Head (" + curHeadUnit + ")*" : optFixedFlow.isSelected() ? "Flow (" + curDisUnit + ")*" : "Head %*";
		JTableHeader tmpHdr = tblTestRt.getTableHeader();
		tmpHdr.getColumnModel().getColumn(2).setHeaderValue(curSel);
		
		DefaultTableModel tmpModel = (DefaultTableModel) tblTestRt.getModel();
		tmpHdr.update(tmpHdr.getGraphics());
		
		tmpHdr = tblTestType.getTableHeader();
		tmpHdr.getColumnModel().getColumn(2).setHeaderValue(curSel);
		tmpModel = (DefaultTableModel) tblTestType.getModel();
		tmpHdr.update(tmpHdr.getGraphics());
		
	}

	private void optFixedFlowActionPerformed() {
		optFixedHeadActionPerformed();
	}

	private void optFixedPerActionPerformed() {
		optFixedHeadActionPerformed();
		// auto populate the percentages
		
		Float headPerFactor = 100F/(tblTestRt.getRowCount()-1);
		int r=0;
		for(int i=tblTestRt.getRowCount()-1; i>=0; i--) {
			tblTestRt.setValueAt((int)(headPerFactor * i), r, 2);
			tblTestRt.setValueAt('0', r, 3);
			++r;
		}
		headPerFactor = 100F/(tblTestType.getRowCount()-1);
		r=0;
		for(int i=tblTestType.getRowCount()-1; i>=0; i--) {
			tblTestType.setValueAt((int)(headPerFactor * i), r, 2);
			tblTestType.setValueAt('0', r, 3);
			r++;
		}
	}


	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		pnlHead = new JPanel();
		pnlMain = new JPanel();
		label19 = new JLabel();
		label20 = new JLabel();
		scrollPane1 = new JScrollPane();
		tblTestAll = new JTable();
		cmdRight = new JButton();
		scrollPane2 = new JScrollPane();
		tblTestRt = new JTable();
		cmdLeft = new JButton();
		pnlValve = new JPanel();
		optFixedHead = new JRadioButton();
		optFixedFlow = new JRadioButton();
		optFixedPer = new JRadioButton();
		label2 = new JLabel();
		label21 = new JLabel();
		cmdRight2 = new JButton();
		scrollPane3 = new JScrollPane();
		tblTestType = new JTable();
		cmdLeft2 = new JButton();
		cmdAddRow = new JButton();
		cmdRemRow = new JButton();
		cmdRevTest = new JButton();
		lblTimeOut2 = new JLabel();
		txtTypeInterval = new JTextField();
		lblTimeOut3 = new JLabel();
		label1 = new JLabel();
		cmdSave = new JButton();
		cmdExit = new JButton();

		//======== this ========
		setTitle("Doer PumpView: Test Head Settings");
		setFont(new Font("Arial", Font.PLAIN, 12));
		setResizable(false);
		setModal(true);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				thisWindowClosing();
			}
		});
		Container contentPane = getContentPane();
		contentPane.setLayout(new TableLayout(new double[][] {
			{5, TableLayout.FILL, 5},
			{5, 536, 5}}));
		((TableLayout)contentPane.getLayout()).setHGap(5);
		((TableLayout)contentPane.getLayout()).setVGap(5);

		//======== pnlHead ========
		{
			pnlHead.setBorder(new TitledBorder(null, "Test Head Settings", TitledBorder.CENTER, TitledBorder.TOP,
				new Font("Arial", Font.BOLD, 14), Color.blue));
			pnlHead.setFocusable(false);
			pnlHead.setLayout(new TableLayout(new double[][] {
				{330, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL},
				{TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED}}));
			((TableLayout)pnlHead.getLayout()).setHGap(5);
			((TableLayout)pnlHead.getLayout()).setVGap(5);

			//======== pnlMain ========
			{
				pnlMain.setLayout(new TableLayout(new double[][] {
					{5, 90, 90, 122, 50, 125, 45, 176},
					{TableLayout.MINIMUM, 40, 40, 1, TableLayout.MINIMUM, TableLayout.MINIMUM, 81, 80, TableLayout.PREFERRED, TableLayout.PREFERRED}}));
				((TableLayout)pnlMain.getLayout()).setHGap(5);
				((TableLayout)pnlMain.getLayout()).setVGap(5);

				//---- label19 ----
				label19.setText("Available Tests");
				label19.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 12));
				label19.setIcon(null);
				label19.setHorizontalAlignment(SwingConstants.CENTER);
				label19.setForeground(Color.blue);
				label19.setOpaque(true);
				pnlMain.add(label19, new TableLayoutConstraints(1, 0, 3, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- label20 ----
				label20.setText("Routine Test");
				label20.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 12));
				label20.setIcon(null);
				label20.setHorizontalAlignment(SwingConstants.CENTER);
				label20.setForeground(Color.blue);
				label20.setOpaque(true);
				pnlMain.add(label20, new TableLayoutConstraints(5, 0, 7, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//======== scrollPane1 ========
				{

					//---- tblTestAll ----
					tblTestAll.setModel(new DefaultTableModel(
						new Object[][] {
						},
						new String[] {
							"Code", "Test Name"
						}
					));
					{
						TableColumnModel cm = tblTestAll.getColumnModel();
						cm.getColumn(0).setMinWidth(70);
						cm.getColumn(0).setMaxWidth(70);
						cm.getColumn(0).setPreferredWidth(70);
						cm.getColumn(1).setMinWidth(240);
						cm.getColumn(1).setMaxWidth(240);
						cm.getColumn(1).setPreferredWidth(240);
					}
					tblTestAll.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
					tblTestAll.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					tblTestAll.setRowHeight(17);
					tblTestAll.setGridColor(Color.lightGray);
					scrollPane1.setViewportView(tblTestAll);
				}
				pnlMain.add(scrollPane1, new TableLayoutConstraints(1, 1, 3, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmdRight ----
				cmdRight.setText("->");
				cmdRight.setFont(new Font("Tahoma", Font.BOLD, 12));
				cmdRight.setToolTipText("Copy the selected test to routine test list");
				cmdRight.addActionListener(e -> cmdRightActionPerformed());
				pnlMain.add(cmdRight, new TableLayoutConstraints(4, 1, 4, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//======== scrollPane2 ========
				{

					//---- tblTestRt ----
					tblTestRt.setModel(new DefaultTableModel(
						new Object[][] {
						},
						new String[] {
							"Code", "Test Name", "Head*", "+/- %"
						}
					) {
						boolean[] columnEditable = new boolean[] {
							false, false, true, true
						};
						@Override
						public boolean isCellEditable(int rowIndex, int columnIndex) {
							return columnEditable[columnIndex];
						}
					});
					{
						TableColumnModel cm = tblTestRt.getColumnModel();
						cm.getColumn(0).setMinWidth(40);
						cm.getColumn(0).setPreferredWidth(40);
						cm.getColumn(1).setMinWidth(175);
						cm.getColumn(1).setPreferredWidth(175);
					}
					tblTestRt.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
					tblTestRt.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					tblTestRt.setRowHeight(17);
					tblTestRt.setGridColor(Color.lightGray);
					tblTestRt.setForeground(Color.gray);
					scrollPane2.setViewportView(tblTestRt);
				}
				pnlMain.add(scrollPane2, new TableLayoutConstraints(5, 1, 7, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmdLeft ----
				cmdLeft.setText("<-");
				cmdLeft.setFont(new Font("Tahoma", Font.BOLD, 12));
				cmdLeft.setToolTipText("Remove the selected routine test from the list");
				cmdLeft.addActionListener(e -> cmdLeftActionPerformed());
				pnlMain.add(cmdLeft, new TableLayoutConstraints(4, 2, 4, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//======== pnlValve ========
				{
					pnlValve.setBorder(new TitledBorder(null, "Automatic Valve Control", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
						new Font("Arial", Font.BOLD, 12)));
					pnlValve.setLayout(new TableLayout(new double[][] {
						{TableLayout.FILL, TableLayout.FILL, TableLayout.PREFERRED},
						{TableLayout.PREFERRED, TableLayout.PREFERRED}}));
					((TableLayout)pnlValve.getLayout()).setHGap(5);
					((TableLayout)pnlValve.getLayout()).setVGap(5);

					//---- optFixedHead ----
					optFixedHead.setText("Fixed Head");
					optFixedHead.setFont(new Font("Arial", Font.PLAIN, 12));
					optFixedHead.addActionListener(e -> optFixedHeadActionPerformed());
					pnlValve.add(optFixedHead, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- optFixedFlow ----
					optFixedFlow.setText("Fixed Flow");
					optFixedFlow.setFont(new Font("Arial", Font.PLAIN, 12));
					optFixedFlow.addActionListener(e -> optFixedFlowActionPerformed());
					pnlValve.add(optFixedFlow, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- optFixedPer ----
					optFixedPer.setText("Head Range %");
					optFixedPer.setFont(new Font("Arial", Font.PLAIN, 12));
					optFixedPer.addActionListener(e -> optFixedPerActionPerformed());
					pnlValve.add(optFixedPer, new TableLayoutConstraints(2, 0, 2, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label2 ----
					label2.setText("<html>* For SO test, system will shut-off the value to achive \"0 Flow\" by default. Any set value defined by users will be ignored. ");
					label2.setFont(new Font("Arial", Font.ITALIC, 11));
					pnlValve.add(label2, new TableLayoutConstraints(0, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				}
				pnlMain.add(pnlValve, new TableLayoutConstraints(5, 4, 7, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label21 ----
				label21.setText("Type Test");
				label21.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 12));
				label21.setIcon(null);
				label21.setHorizontalAlignment(SwingConstants.CENTER);
				label21.setForeground(Color.blue);
				label21.setOpaque(true);
				pnlMain.add(label21, new TableLayoutConstraints(5, 5, 7, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- cmdRight2 ----
				cmdRight2.setText("->");
				cmdRight2.setFont(new Font("Tahoma", Font.BOLD, 12));
				cmdRight2.setToolTipText("Copy the selected test to type test list");
				cmdRight2.addActionListener(e -> cmdRight2ActionPerformed());
				pnlMain.add(cmdRight2, new TableLayoutConstraints(4, 6, 4, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//======== scrollPane3 ========
				{

					//---- tblTestType ----
					tblTestType.setModel(new DefaultTableModel(
						new Object[][] {
						},
						new String[] {
							"Code", "Test Name", "Head*", "+/- %"
						}
					) {
						boolean[] columnEditable = new boolean[] {
							false, false, true, true
						};
						@Override
						public boolean isCellEditable(int rowIndex, int columnIndex) {
							return columnEditable[columnIndex];
						}
					});
					{
						TableColumnModel cm = tblTestType.getColumnModel();
						cm.getColumn(0).setMinWidth(40);
						cm.getColumn(0).setPreferredWidth(40);
						cm.getColumn(1).setMinWidth(175);
						cm.getColumn(1).setPreferredWidth(175);
					}
					tblTestType.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
					tblTestType.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					tblTestType.setRowHeight(17);
					tblTestType.setGridColor(Color.lightGray);
					tblTestType.setForeground(Color.gray);
					scrollPane3.setViewportView(tblTestType);
				}
				pnlMain.add(scrollPane3, new TableLayoutConstraints(5, 6, 7, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmdLeft2 ----
				cmdLeft2.setText("<-");
				cmdLeft2.setFont(new Font("Tahoma", Font.BOLD, 12));
				cmdLeft2.setToolTipText("Remove the selected type test from the list");
				cmdLeft2.addActionListener(e -> cmdLeft2ActionPerformed());
				pnlMain.add(cmdLeft2, new TableLayoutConstraints(4, 7, 4, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmdAddRow ----
				cmdAddRow.setText("Insert");
				cmdAddRow.setFont(new Font("Arial", Font.PLAIN, 12));
				cmdAddRow.setMnemonic('I');
				cmdAddRow.setToolTipText("Add an empty row to enter new test");
				cmdAddRow.addActionListener(e -> cmdAddRowActionPerformed());
				pnlMain.add(cmdAddRow, new TableLayoutConstraints(1, 8, 1, 9, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmdRemRow ----
				cmdRemRow.setText("Remove");
				cmdRemRow.setFont(new Font("Arial", Font.PLAIN, 12));
				cmdRemRow.setMnemonic('A');
				cmdRemRow.setToolTipText("Remove currently selected test");
				cmdRemRow.addActionListener(e -> cmdRemRowActionPerformed());
				pnlMain.add(cmdRemRow, new TableLayoutConstraints(2, 8, 2, 9, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmdRevTest ----
				cmdRevTest.setText("Reverse Order");
				cmdRevTest.setFont(new Font("Arial", Font.PLAIN, 12));
				cmdRevTest.setMnemonic('A');
				cmdRevTest.setToolTipText("Remove currently selected test");
				cmdRevTest.addActionListener(e -> cmdRevTestActionPerformed());
				pnlMain.add(cmdRevTest, new TableLayoutConstraints(3, 8, 3, 9, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblTimeOut2 ----
				lblTimeOut2.setText(" Perform Type Test Once In Every");
				lblTimeOut2.setFont(new Font("Arial", Font.PLAIN, 12));
				lblTimeOut2.setIcon(null);
				pnlMain.add(lblTimeOut2, new TableLayoutConstraints(4, 8, 5, 8, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtTypeInterval ----
				txtTypeInterval.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				pnlMain.add(txtTypeInterval, new TableLayoutConstraints(6, 8, 6, 8, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblTimeOut3 ----
				lblTimeOut3.setText("Pumps");
				lblTimeOut3.setFont(new Font("Arial", Font.PLAIN, 12));
				lblTimeOut3.setIcon(null);
				pnlMain.add(lblTimeOut3, new TableLayoutConstraints(7, 8, 7, 8, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- label1 ----
				label1.setText("<html>EXAMPLES:<br/>Enter 20 for 19 routine tests followed by 1 type test<br/>Enter 1 if all tests needed to be type test<br/>Enter 0 if all tests needed to be routine test</html>");
				label1.setFont(new Font("Arial", Font.ITALIC, 11));
				pnlMain.add(label1, new TableLayoutConstraints(4, 9, 7, 9, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			pnlHead.add(pnlMain, new TableLayoutConstraints(0, 0, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdSave ----
			cmdSave.setText("Save");
			cmdSave.setFont(new Font("Arial", Font.PLAIN, 14));
			cmdSave.setIcon(new ImageIcon(getClass().getResource("/img/save.PNG")));
			cmdSave.setToolTipText("Click on this to save the changes");
			cmdSave.setMnemonic('S');
			cmdSave.addActionListener(e -> cmdSaveActionPerformed());
			pnlHead.add(cmdSave, new TableLayoutConstraints(0, 2, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdExit ----
			cmdExit.setText("<html>Close&nbsp;&nbsp<font size=-2>[Esc]</html>");
			cmdExit.setFont(new Font("Arial", Font.PLAIN, 14));
			cmdExit.setIcon(new ImageIcon(getClass().getResource("/img/exit.PNG")));
			cmdExit.setToolTipText("Click on this to close this window");
			cmdExit.addActionListener(e -> cmdExitActionPerformed());
			pnlHead.add(cmdExit, new TableLayoutConstraints(1, 2, 3, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		}
		contentPane.add(pnlHead, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		pack();
		setLocationRelativeTo(getOwner());

		//---- buttonGroup1 ----
		ButtonGroup buttonGroup1 = new ButtonGroup();
		buttonGroup1.add(optFixedHead);
		buttonGroup1.add(optFixedFlow);
		buttonGroup1.add(optFixedPer);
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
		
		// CUSTOM CODE - BEGIN
		associateFunctionKeys();
		// CUSTOM CODE - END
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel pnlHead;
	private JPanel pnlMain;
	private JLabel label19;
	private JLabel label20;
	private JScrollPane scrollPane1;
	private JTable tblTestAll;
	private JButton cmdRight;
	private JScrollPane scrollPane2;
	private JTable tblTestRt;
	private JButton cmdLeft;
	private JPanel pnlValve;
	private JRadioButton optFixedHead;
	private JRadioButton optFixedFlow;
	private JRadioButton optFixedPer;
	private JLabel label2;
	private JLabel label21;
	private JButton cmdRight2;
	private JScrollPane scrollPane3;
	private JTable tblTestType;
	private JButton cmdLeft2;
	private JButton cmdAddRow;
	private JButton cmdRemRow;
	private JButton cmdRevTest;
	private JLabel lblTimeOut2;
	private JTextField txtTypeInterval;
	private JLabel lblTimeOut3;
	private JLabel label1;
	private JButton cmdSave;
	private JButton cmdExit;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
	// CUSTOM CODE - BEGIN
	String[] testTypes;
	DefaultListModel lstModelPrim = null;
	DefaultListModel lstModelSec = null;
	
	Boolean orgIsPrim = false;
	String curSel = "";
	int oldRtCount = 0;
	int oldTypeCount = 0;
	int newRtCount = 0;
	int newTypeCount = 0;
	
	PumpType frmMain = null;
	String curPumpTypeId = "";
	String curAutoValveType = "";
	String curDisUnit = "";
	String curHeadUnit = "";
	
	private Database db = null;
	
	private Cursor waitCursor = new Cursor(Cursor.WAIT_CURSOR);
	private Cursor defCursor = new Cursor(Cursor.DEFAULT_CURSOR);
	
	private DecimalFormat dotTwo = new DecimalFormat("#0.00");
	private boolean orderChanged = false;
	// CUSTOM CODE - END
}
