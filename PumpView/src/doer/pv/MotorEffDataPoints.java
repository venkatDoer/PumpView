/*
 * Created by JFormDesigner on Thu Mar 03 12:48:47 IST 2022
 */

package doer.pv;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.sql.SQLException;
import java.text.DecimalFormat;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

import doer.io.Database;
import doer.io.MyResultSet;
import info.clearthought.layout.*;

/**
 * @author VENKATESAN SELVARAJ
 */
public class MotorEffDataPoints extends JDialog {
	public MotorEffDataPoints(Frame owner) {
		super(owner);
		initComponents();
		tblMotEff.putClientProperty("terminateEditOnFocusLost", true);
	}

	public MotorEffDataPoints(Dialog owner, String pumpTypeId, String pumpType, String disUnit) {
		super(owner);
		frmMain = (PumpType) owner;
		initComponents();
		associateFunctionKeys();
		
		curPumpTypeId = pumpTypeId;
		
		loadSettings();
		
		// set the panel header
		pnlHead.setBorder(new TitledBorder(null, "Motor Efficiency Data Points [" + pumpType + "]", TitledBorder.CENTER, TitledBorder.TOP,
				new Font("Arial", Font.BOLD, 14), Color.blue));
	}

	private void loadSettings() {
		try {
			db = Database.getInstance();
			
			MyResultSet res = db.executeQuery("select * from " + Configuration.MOT_EFF_DATA_POINTS + " where pump_type_id='" + curPumpTypeId + "'");
			Integer i = 0;
			DefaultTableModel defModel = (DefaultTableModel) tblMotEff.getModel();
			while (res.next()) {
				if (tblMotEff.getRowCount() == i) {
					defModel.addRow(new Object[] {"","",""});
				}
				tblMotEff.setValueAt(dotThree.format(res.getFloat("ip_kw")), i, 0);
				tblMotEff.setValueAt(dotThree.format(res.getFloat("op_kw")), i, 1);
				tblMotEff.setValueAt(dotTwo.format(res.getFloat("eff")), i, 2);
				++i;
			}
		} catch (Exception sqle) {
			JOptionPane.showMessageDialog(new JDialog(), "Error loading data points:" + sqle.getMessage());
			return;
		}
	}
	
	private void thisWindowClosing() {
	}

	private void cmdSaveActionPerformed() {
		if (tblMotEff.getCellEditor() != null) {
			tblMotEff.getCellEditor().stopCellEditing();
		}
		// save
		int i = 0;
		try {
			// delete existing config and insert new
			db.executeUpdate("delete from " + Configuration.MOT_EFF_DATA_POINTS + " where pump_type_id='" + curPumpTypeId + "'");
			for(i=0; i<tblMotEff.getRowCount(); i++) {
				if (tblMotEff.getValueAt(i, 0) != null && !tblMotEff.getValueAt(i, 0).toString().isEmpty()) {
					db.executeUpdate("insert into " + Configuration.MOT_EFF_DATA_POINTS + "(pump_type_id,ip_kw,op_kw,eff) values ('" + curPumpTypeId + "','" + tblMotEff.getValueAt(i, 0).toString() + "','" + tblMotEff.getValueAt(i, 1).toString() + "','" + tblMotEff.getValueAt(i, 2).toString() + "')");
				}
			}
			JOptionPane.showMessageDialog(this, "Changes are saved successfully!");
			frmMain.refreshMotEffDataPointStatus();
		} catch (NullPointerException ne) {
			JOptionPane.showMessageDialog(this, "Please enter valid power values");
			tblMotEff.setRowSelectionInterval(i, i);
			tblMotEff.requestFocus();
			return;
		} catch (SQLException sqle) {
			JOptionPane.showMessageDialog(new JDialog(), "Error updating data points:" + sqle.getMessage());
			return;
		}
	}

	private void cmdExitActionPerformed() {
		this.setVisible(false);
		thisWindowClosing();
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

	private void cmdAddActionPerformed() {
		if (tblMotEff.getCellEditor() != null) {
			tblMotEff.getCellEditor().stopCellEditing();
		}
		int selRow = tblMotEff.getSelectedRow();
		DefaultTableModel defModel = (DefaultTableModel) tblMotEff.getModel();
		if (selRow >= 0) {
			// insert on top of chosen row
			defModel.insertRow(selRow, new Object[] {"", "", ""});
		} else {
			// add a row at the end
			defModel.addRow(new Object[] {"", "", ""});
			selRow = tblMotEff.getRowCount() - 1;
		}
		tblMotEff.setRowSelectionInterval(selRow, selRow);
	}

	private void cmdDelActionPerformed() {
		if (tblMotEff.getCellEditor() != null) {
			tblMotEff.getCellEditor().stopCellEditing();
		}
		int selRow = tblMotEff.getSelectedRow();
		if (selRow < 0) {
			JOptionPane.showMessageDialog(this, "Please choose a record to delete");
			return;
		}
		if (JOptionPane.showConfirmDialog(this, "Sure, you want to delete the selected record?", "Confirm", JOptionPane.YES_NO_OPTION) == 0) {
			DefaultTableModel defModel = (DefaultTableModel) tblMotEff.getModel();
			defModel.removeRow(selRow);
		}
	}

	private void tblMotEffPropertyChange() {
		int selRow = tblMotEff.getSelectedRow();
		// calculate eff
		if (selRow >= 0) {
			Float eff = 0F;
			try {
				Float ip = Float.valueOf(tblMotEff.getValueAt(selRow, 0).toString());
				Float op = Float.valueOf(tblMotEff.getValueAt(selRow, 1).toString());
				eff = op / ip * 100;
			} catch (Exception ne) {
				// ignore
			}
			tblMotEff.setValueAt(dotTwo.format(eff), selRow, 2);
		}
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		pnlHead = new JPanel();
		scrollPane1 = new JScrollPane();
		tblMotEff = new JTable();
		panel1 = new JPanel();
		cmdAdd = new JButton();
		cmdDel = new JButton();
		label1 = new JLabel();
		cmdSave = new JButton();
		cmdExit = new JButton();

		//======== this ========
		setTitle("Doer PumpView: Motor Efficiency Data Points");
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
			{5, 400, 5},
			{5, TableLayout.PREFERRED, 5}}));
		((TableLayout)contentPane.getLayout()).setHGap(5);
		((TableLayout)contentPane.getLayout()).setVGap(5);

		//======== pnlHead ========
		{
			pnlHead.setBorder(new TitledBorder(null, "Motor Efficiency Data Points", TitledBorder.CENTER, TitledBorder.TOP,
				new Font("Arial", Font.BOLD, 14), Color.blue));
			pnlHead.setFocusable(false);
			pnlHead.setLayout(new TableLayout(new double[][] {
				{TableLayout.FILL, TableLayout.FILL, TableLayout.PREFERRED},
				{224, 64, TableLayout.PREFERRED}}));
			((TableLayout)pnlHead.getLayout()).setHGap(5);
			((TableLayout)pnlHead.getLayout()).setVGap(5);

			//======== scrollPane1 ========
			{

				//---- tblMotEff ----
				tblMotEff.setModel(new DefaultTableModel(
					new Object[][] {
						{null, null, null},
						{null, null, null},
						{null, null, null},
						{null, null, null},
						{null, null, null},
						{null, null, null},
						{null, null, null},
						{null, null, null},
						{null, null, null},
					},
					new String[] {
						"Motor IP Power (kW)", "Motor OP Power (kW)", "Motor Eff. (%)"
					}
				) {
					Class<?>[] columnTypes = new Class<?>[] {
						String.class, String.class, String.class
					};
					boolean[] columnEditable = new boolean[] {
						true, true, false
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
					TableColumnModel cm = tblMotEff.getColumnModel();
					cm.getColumn(0).setResizable(false);
					cm.getColumn(1).setResizable(false);
					cm.getColumn(2).setResizable(false);
				}
				tblMotEff.setFont(new Font("SansSerif", Font.PLAIN, 14));
				tblMotEff.setRowHeight(20);
				tblMotEff.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
				tblMotEff.addPropertyChangeListener(new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent e) {
						tblMotEffPropertyChange();
					}
				});
				scrollPane1.setViewportView(tblMotEff);
			}
			pnlHead.add(scrollPane1, new TableLayoutConstraints(0, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//======== panel1 ========
			{
				panel1.setLayout(new TableLayout(new double[][] {
					{TableLayout.PREFERRED},
					{TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL}}));
				((TableLayout)panel1.getLayout()).setHGap(5);
				((TableLayout)panel1.getLayout()).setVGap(5);

				//---- cmdAdd ----
				cmdAdd.setText("+");
				cmdAdd.setFont(new Font("Arial", Font.BOLD, 14));
				cmdAdd.setToolTipText("Insert a row");
				cmdAdd.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cmdAddActionPerformed();
					}
				});
				panel1.add(cmdAdd, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmdDel ----
				cmdDel.setText("-");
				cmdDel.setFont(new Font("Arial", Font.BOLD, 14));
				cmdDel.setToolTipText("Delete the selected row");
				cmdDel.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cmdDelActionPerformed();
					}
				});
				panel1.add(cmdDel, new TableLayoutConstraints(0, 2, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			pnlHead.add(panel1, new TableLayoutConstraints(2, 0, 2, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- label1 ----
			label1.setText("<html>NOTES:<br>1. This needs to be configured only if pump efficiency is required in test report.<br>2. If pump efficiency is required and this is not configured, pump efficiency will be derived based on fixed motor efficiency declared under name plate details.</html>");
			pnlHead.add(label1, new TableLayoutConstraints(0, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdSave ----
			cmdSave.setText("Save");
			cmdSave.setFont(new Font("Arial", Font.PLAIN, 14));
			cmdSave.setIcon(new ImageIcon(getClass().getResource("/img/save.PNG")));
			cmdSave.setToolTipText("Click on this to save the changes");
			cmdSave.setMnemonic('S');
			cmdSave.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					cmdSaveActionPerformed();
				}
			});
			pnlHead.add(cmdSave, new TableLayoutConstraints(0, 2, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdExit ----
			cmdExit.setText("<html>Close&nbsp;&nbsp<font size=-2>[Esc]</html>");
			cmdExit.setFont(new Font("Arial", Font.PLAIN, 14));
			cmdExit.setIcon(new ImageIcon(getClass().getResource("/img/exit.PNG")));
			cmdExit.setToolTipText("Click on this to close this window");
			cmdExit.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					cmdExitActionPerformed();
				}
			});
			pnlHead.add(cmdExit, new TableLayoutConstraints(1, 2, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		}
		contentPane.add(pnlHead, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}
	
	// custom variables
	private String curPumpTypeId = "";
	private Database db = null;
	PumpType frmMain = null;
	
	private DecimalFormat dotTwo = new DecimalFormat("#0.00");
	private DecimalFormat dotThree = new DecimalFormat("#0.000");
	
	
	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel pnlHead;
	private JScrollPane scrollPane1;
	private JTable tblMotEff;
	private JPanel panel1;
	private JButton cmdAdd;
	private JButton cmdDel;
	private JLabel label1;
	private JButton cmdSave;
	private JButton cmdExit;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
