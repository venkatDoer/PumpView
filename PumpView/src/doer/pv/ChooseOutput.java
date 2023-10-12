/*
 * Created by JFormDesigner on Sun Apr 06 13:10:36 IST 2014
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
import java.util.ArrayList;

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
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.border.TitledBorder;

import doer.io.Database;
import doer.io.MyResultSet;
import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

/**
 * @author VENKATESAN SELVARAJ
 */
public class ChooseOutput extends JDialog {
	public ChooseOutput(Frame owner) {
		super(owner);
		initComponents();
		custInit();
	}

	public ChooseOutput(Dialog owner) {
		super(owner);
		initComponents();
	}

	private void cmdExitActionPerformed() {
		this.setVisible(false);
	}

	private void cmdChooseActionPerformed() {
		// save the changes
		cmdSaveActionPerformed(true);
		((PumpView)this.getParent()).setLRStationCaption();
		this.setVisible(false);
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

	// custom initialization
	private void custInit() {
		initInProgress = true;
		if (Configuration.NUMBER_OF_STATION == 1) {
			pnlOP.remove(pnlPump);
		}
		// load available outputs & select the current one by default
		try {
			Database db = Database.getInstance();
			
			MyResultSet res = null;
			// load available pump types
			res = db.executeQuery("select type from " + Configuration.PUMPTYPE + " order by type");
			while(res.next()) {
				cmbPump.addItem(res.getString("type"));
			}
			
			// load available stations
			res = db.executeQuery("select station_no from " + Configuration.OUTPUT + " where line='" + Configuration.LINE_NAME + "' order by station_no");
			String curStation = "";
			while(res.next()) {
				curStation = res.getString("station_no");
				cmbStation.addItem(curStation);
				// outputs
				cmbPresOp.addItem(curStation + ":A");
				cmbPresOp.addItem(curStation + ":B");
				cmbFlowOp.addItem(curStation + ":A");
				cmbFlowOp.addItem(curStation + ":B");
			}
			
			
		} catch (SQLException se) {
			JOptionPane.showMessageDialog(this, "Error loading the output list:" + se.getMessage());
		}
		associateFunctionKeys();
		initInProgress = false;
		// select default values
		cmbStation.setSelectedItem(Configuration.LAST_USED_STATION);
		
	}
	
	private void cmdSaveActionPerformed() {
		cmdSaveActionPerformed(false);
	}

	private void cmdSaveActionPerformed(boolean fromChoose) {
		// update config
		try {
			Database db = Database.getInstance();
			
			db.executeUpdate("update " + Configuration.OUTPUT + " set pres_op ='" +  cmbPresOp.getSelectedItem().toString() + "', flow_op ='" + cmbFlowOp.getSelectedItem().toString() + 
					"', pump_type_id = (select pump_type_id from " + Configuration.PUMPTYPE + " where type='" + cmbPump.getSelectedItem().toString() + 
					"') where line='" + Configuration.LINE_NAME + "' and station_no = '" + cmbStation.getSelectedItem().toString() + "'");
			
			// pumpList.set(cmbStation.getSelectedIndex(), pumpType);
			if (!fromChoose) {
				JOptionPane.showMessageDialog(this, "Changes are saved successfully!"); 
			}
		} catch (SQLException se) {
			JOptionPane.showMessageDialog(this, "Error updating output:" + se.getMessage());
		}
		
		// choose it
		String tmpSt = cmbStation.getSelectedItem().toString();
		String tmpPr = cmbPresOp.getSelectedItem().toString();
		String tmpFl = cmbFlowOp.getSelectedItem().toString();
		
		// update global configs
		Boolean configChanged = false;
		if (fromChoose) {
			if (!Configuration.LAST_USED_STATION.equals(tmpSt)) {
				Configuration.LAST_USED_STATION = tmpSt;
				Configuration.saveConfigValues("LAST_USED_STATION");
				configChanged = true;
			}
		}
		
		if (!Configuration.OP_PRESSURE_VAC_OUTPUT.get(tmpSt).equals(tmpPr) || !Configuration.OP_FLOW_OUTPUT.get(tmpSt).equals(tmpFl)) {
			Configuration.OP_PRESSURE_VAC_OUTPUT.put(tmpSt, tmpPr);
			Configuration.OP_FLOW_OUTPUT.put(tmpSt, tmpFl);
			configChanged = true;
		}
		
		if (!Configuration.OP_PUMP_TYPE.get(tmpSt).equals(cmbPump.getSelectedItem().toString())){
			Configuration.OP_PUMP_TYPE.put(tmpSt, Configuration.LAST_USED_PUMP_TYPE);
			configChanged = true;
		}
		
		// update main form if any value changed
		if (configChanged) {
			this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			System.out.println("refreshed");
			try {
				// reflect the changes in main form
				((PumpView)this.getParent()).stopThreads();
				((PumpView)this.getParent()).loadStations();
				((PumpView)this.getParent()).startThreads();
			} catch (Exception e) {
				System.out.println("Error resetting output");
				e.printStackTrace();
			} finally {
				this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		} else {
			System.out.println("not refreshed");
		}
	}

	private void cmbStationActionPerformed() {
		if (!initInProgress) {
			// outputs earlier chosen for this station
			cmbPresOp.setSelectedItem(Configuration.OP_PRESSURE_VAC_OUTPUT.get(cmbStation.getSelectedItem().toString()));
			cmbFlowOp.setSelectedItem(Configuration.OP_FLOW_OUTPUT.get(cmbStation.getSelectedItem().toString()));
			if (Configuration.OP_PUMP_TYPE.get(cmbStation.getSelectedItem().toString()).isEmpty()) {
				cmbPump.setSelectedIndex(0);
			} else {
				cmbPump.setSelectedItem(Configuration.OP_PUMP_TYPE.get(cmbStation.getSelectedItem().toString()));
			}
		}
	}

	private void cmbPresOpActionPerformed() {
		cmbFlowOp.setSelectedItem(cmbPresOp.getSelectedItem());
	}
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		pnlOP = new JPanel();
		cmbStation = new JComboBox();
		panel2 = new JPanel();
		label1 = new JLabel();
		cmbPresOp = new JComboBox();
		label2 = new JLabel();
		cmbFlowOp = new JComboBox();
		pnlPump = new JPanel();
		cmbPump = new JComboBox();
		panel1 = new JPanel();
		cmdChoose = new JButton();
		cmdSave = new JButton();
		cmdExit = new JButton();

		//======== this ========
		setTitle("Doer PumpView: Choose Station");
		setModal(true);
		Container contentPane = getContentPane();
		contentPane.setLayout(new TableLayout(new double[][] {
			{5, TableLayout.FILL, TableLayout.FILL, 5},
			{5, TableLayout.FILL, TableLayout.PREFERRED, 5}}));
		((TableLayout)contentPane.getLayout()).setHGap(5);
		((TableLayout)contentPane.getLayout()).setVGap(5);

		//======== pnlOP ========
		{
			pnlOP.setBorder(new TitledBorder(null, "Choose Station", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION,
				new Font("Arial", Font.BOLD, 14), Color.blue));
			pnlOP.setLayout(new TableLayout(new double[][] {
				{TableLayout.FILL, TableLayout.FILL},
				{TableLayout.FILL, 35, TableLayout.FILL, TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED}}));
			((TableLayout)pnlOP.getLayout()).setHGap(5);
			((TableLayout)pnlOP.getLayout()).setVGap(5);

			//---- cmbStation ----
			cmbStation.setFont(new Font("Arial", Font.PLAIN, 16));
			cmbStation.addActionListener(e -> cmbStationActionPerformed());
			pnlOP.add(cmbStation, new TableLayoutConstraints(0, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//======== panel2 ========
			{
				panel2.setBorder(new TitledBorder(null, "Output of chosen station", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
					new Font("Arial", Font.BOLD, 12), Color.blue));
				panel2.setLayout(new TableLayout(new double[][] {
					{TableLayout.FILL, TableLayout.FILL},
					{TableLayout.PREFERRED, TableLayout.PREFERRED}}));
				((TableLayout)panel2.getLayout()).setHGap(5);
				((TableLayout)panel2.getLayout()).setVGap(5);

				//---- label1 ----
				label1.setText("Pressure / Vacuum Output");
				label1.setFont(new Font("Arial", Font.PLAIN, 14));
				panel2.add(label1, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmbPresOp ----
				cmbPresOp.setFont(new Font("Arial", Font.PLAIN, 16));
				cmbPresOp.addActionListener(e -> cmbPresOpActionPerformed());
				panel2.add(cmbPresOp, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label2 ----
				label2.setText("Flow Output");
				label2.setFont(new Font("Arial", Font.PLAIN, 14));
				panel2.add(label2, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmbFlowOp ----
				cmbFlowOp.setFont(new Font("Arial", Font.PLAIN, 16));
				panel2.add(cmbFlowOp, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			pnlOP.add(panel2, new TableLayoutConstraints(0, 3, 1, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//======== pnlPump ========
			{
				pnlPump.setBorder(new TitledBorder(null, "Pump model of chosen station", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
					new Font("Arial", Font.BOLD, 12), Color.blue));
				pnlPump.setLayout(new TableLayout(new double[][] {
					{TableLayout.FILL, TableLayout.FILL},
					{TableLayout.PREFERRED}}));
				((TableLayout)pnlPump.getLayout()).setHGap(5);
				((TableLayout)pnlPump.getLayout()).setVGap(5);

				//---- cmbPump ----
				cmbPump.setFont(new Font("Arial", Font.PLAIN, 16));
				pnlPump.add(cmbPump, new TableLayoutConstraints(0, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			pnlOP.add(pnlPump, new TableLayoutConstraints(0, 5, 1, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		}
		contentPane.add(pnlOP, new TableLayoutConstraints(1, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

		//======== panel1 ========
		{
			panel1.setLayout(new TableLayout(new double[][] {
				{TableLayout.FILL, TableLayout.FILL, TableLayout.FILL},
				{TableLayout.PREFERRED}}));
			((TableLayout)panel1.getLayout()).setHGap(5);
			((TableLayout)panel1.getLayout()).setVGap(5);

			//---- cmdChoose ----
			cmdChoose.setText("<html>Choose</html>");
			cmdChoose.setFont(new Font("Arial", Font.PLAIN, 14));
			cmdChoose.setIcon(new ImageIcon(getClass().getResource("/img/choose.PNG")));
			cmdChoose.setToolTipText("Click on this to choose the selected station");
			cmdChoose.setRolloverSelectedIcon(null);
			cmdChoose.setDefaultCapable(false);
			cmdChoose.addActionListener(e -> cmdChooseActionPerformed());
			panel1.add(cmdChoose, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdSave ----
			cmdSave.setText("Update");
			cmdSave.setFont(new Font("Arial", Font.PLAIN, 14));
			cmdSave.setIcon(new ImageIcon(getClass().getResource("/img/save.png")));
			cmdSave.setToolTipText("Click on this to update the selected station");
			cmdSave.setRolloverSelectedIcon(null);
			cmdSave.setDefaultCapable(false);
			cmdSave.addActionListener(e -> cmdSaveActionPerformed());
			panel1.add(cmdSave, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdExit ----
			cmdExit.setText("<html>Close&nbsp;&nbsp<font size=-2>[Esc]</html>");
			cmdExit.setFont(new Font("Arial", Font.PLAIN, 14));
			cmdExit.setIcon(new ImageIcon(getClass().getResource("/img/exit.PNG")));
			cmdExit.setToolTipText("Click on this to close this window");
			cmdExit.addActionListener(e -> cmdExitActionPerformed());
			panel1.add(cmdExit, new TableLayoutConstraints(2, 0, 2, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		}
		contentPane.add(panel1, new TableLayoutConstraints(1, 2, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel pnlOP;
	private JComboBox cmbStation;
	private JPanel panel2;
	private JLabel label1;
	private JComboBox cmbPresOp;
	private JLabel label2;
	private JComboBox cmbFlowOp;
	private JPanel pnlPump;
	private JComboBox cmbPump;
	private JPanel panel1;
	private JButton cmdChoose;
	private JButton cmdSave;
	private JButton cmdExit;
	// JFormDesigner - End of variables declaration  //GEN-END:variables

	// custom variables - begin
	boolean initInProgress = false;
	ArrayList<String> pumpList = new ArrayList<String>();
	// custom variables - end
}
