/*
 * Created by JFormDesigner on Thu Mar 03 12:48:47 IST 2022
 */

package doer.pv;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

import doer.io.Database;
import doer.io.MyResultSet;
import info.clearthought.layout.*;

/**
 * @author VENKATESAN SELVARAJ
 */
public class RoutineLimits extends JDialog {
	public RoutineLimits(Frame owner) {
		super(owner);
		initComponents();
	}

	public RoutineLimits(Dialog owner, String pumpTypeId, String pumpType, String disUnit) {
		super(owner);
		frmMain = (PumpType) owner;
		initComponents();
		associateFunctionKeys();
		
		curPumpTypeId = pumpTypeId;
		
		if (disUnit.contains("sup")) {
			lblFODis.setText("<html>" + "Full Open Discharge (" + disUnit.substring(6,disUnit.length()-7) + ")</html>");
			lblDPDis.setText("<html>" + "Duty Point Discharge (" + disUnit.substring(6,disUnit.length()-7) + ")</html>");
			lblSODis.setText("<html>" + "Shut. Pres. Discharge (" + disUnit.substring(6,disUnit.length()-7) + ")</html>");
		} else {
			lblFODis.setText("Full Open Discharge (" + disUnit + ")");
			lblDPDis.setText("Duty Point Discharge (" + disUnit + ")");
			lblSODis.setText("Shut. Pres. Discharge (" + disUnit + ")");
		}
		loadSettings();
		
		// set the panel header
		pnlHead.setBorder(new TitledBorder(null, "Routine Test Lower and Upper Limits [" + pumpType + "]", TitledBorder.CENTER, TitledBorder.TOP,
				new Font("Arial", Font.BOLD, 14), Color.blue));
	}

	private void loadSettings() {
		try {
			db = Database.getInstance();
			
			MyResultSet res = db.executeQuery("select * from " + Configuration.ROUTINE_LIMITS + " where pump_type_id='" + curPumpTypeId + "'");
			while (res.next()) {
				if (res.getString("code").equals("FO")) {
					txtFOHdLL.setText(res.getString("head_ll"));
					txtFOHdUL.setText(res.getString("head_ul"));
					txtFODisLL.setText(res.getString("discharge_ll"));
					txtFODisUL.setText(res.getString("discharge_ul"));
					txtFOCurLL.setText(res.getString("current_ll"));
					txtFOCurUL.setText(res.getString("current_ul"));
					txtFOPowLL.setText(res.getString("power_ll"));
					txtFOPowUL.setText(res.getString("power_ul"));
				} else if (res.getString("code").equals("DP")) {
					txtDPHdLL.setText(res.getString("head_ll"));
					txtDPHdUL.setText(res.getString("head_ul"));
					txtDPDisLL.setText(res.getString("discharge_ll"));
					txtDPDisUL.setText(res.getString("discharge_ul"));
					txtDPCurLL.setText(res.getString("current_ll"));
					txtDPCurUL.setText(res.getString("current_ul"));
					txtDPPowLL.setText(res.getString("power_ll"));
					txtDPPowUL.setText(res.getString("power_ul"));
				} else if (res.getString("code").equals("SO")) {
					txtSOHdLL.setText(res.getString("head_ll"));
					txtSOHdUL.setText(res.getString("head_ul"));
					txtSODisLL.setText(res.getString("discharge_ll"));
					txtSODisUL.setText(res.getString("discharge_ul"));
					txtSOCurLL.setText(res.getString("current_ll"));
					txtSOCurUL.setText(res.getString("current_ul"));
					txtSOPowLL.setText(res.getString("power_ll"));
					txtSOPowUL.setText(res.getString("power_ul"));
				}
			}
		} catch (Exception sqle) {
			JOptionPane.showMessageDialog(new JDialog(), "Error loading limits:" + sqle.getMessage());
			return;
		}
	}
	
	private void thisWindowClosing() {
	}

	private void cmdSaveActionPerformed() {
		// validate
		JTextField txtFld = new JTextField();
		Float tmpVal = 0F;
		for(Component comp : pnlLimits.getComponents()) {
			if (comp instanceof JTextField) {
				txtFld = (JTextField) comp;
				if (!txtFld.getText().trim().isEmpty()) {
					try {
						tmpVal = Float.valueOf(txtFld.getText().trim());
					} catch (NumberFormatException ne) {
						JOptionPane.showMessageDialog(this, "Please enter valid number");
						txtFld.requestFocusInWindow();
						return;
					}
				}
			}
		}
		
		// save
		try {
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			// FO
			db.executeUpdate("update " + Configuration.ROUTINE_LIMITS + " set " +
			"head_ll='" + txtFOHdLL.getText().trim() + "', head_ul='" + txtFOHdUL.getText().trim() + "', discharge_ll='" + txtFODisLL.getText().trim() + "', discharge_ul='" + txtFODisUL.getText().trim() + "'," +
			"current_ll='" + txtFOCurLL.getText().trim() + "', current_ul='" + txtFOCurUL.getText().trim() + "', power_ll='" + txtFOPowLL.getText().trim() + "', power_ul='" + txtFOPowUL.getText().trim() + "'" +
			"where pump_type_id='" + curPumpTypeId + "' and code='FO'");
			
			// DP
			db.executeUpdate("update " + Configuration.ROUTINE_LIMITS + " set " +
			"head_ll='" + txtDPHdLL.getText().trim() + "', head_ul='" + txtDPHdUL.getText().trim() + "', discharge_ll='" + txtDPDisLL.getText().trim() + "', discharge_ul='" + txtDPDisUL.getText().trim() + "'," +
			"current_ll='" + txtDPCurLL.getText().trim() + "', current_ul='" + txtDPCurUL.getText().trim() + "', power_ll='" + txtDPPowLL.getText().trim() + "', power_ul='" + txtDPPowUL.getText().trim() + "'" +
			"where pump_type_id='" + curPumpTypeId + "' and code='DP'");
						
			// SO
			db.executeUpdate("update " + Configuration.ROUTINE_LIMITS + " set " +
			"head_ll='" + txtSOHdLL.getText().trim() + "', head_ul='" + txtSOHdUL.getText().trim() + "', discharge_ll='" + txtSODisLL.getText().trim() + "', discharge_ul='" + txtSODisUL.getText().trim() + "'," +
			"current_ll='" + txtSOCurLL.getText().trim() + "', current_ul='" + txtSOCurUL.getText().trim() + "', power_ll='" + txtSOPowLL.getText().trim() + "', power_ul='" + txtSOPowUL.getText().trim() + "'" +
			"where pump_type_id='" + curPumpTypeId + "' and code='SO'");
			
			// refresh main form
			frmMain.updatePumpType("update");
			
			JOptionPane.showMessageDialog(this, "Changes are saved successfully!"); 
		} catch (Exception sqle) {
			JOptionPane.showMessageDialog(new JDialog(), "Error updating limits:" + sqle.getMessage());
			return;
		} finally {
			this.setCursor(Cursor.getDefaultCursor());
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

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		pnlHead = new JPanel();
		cmdSave = new JButton();
		cmdExit = new JButton();
		pnlLimits = new JPanel();
		label9 = new JLabel();
		separator3 = new JSeparator();
		lblFOHd = new JLabel();
		txtFOHdLL = new JTextField();
		txtFOHdUL = new JTextField();
		lblFODis = new JLabel();
		txtFODisLL = new JTextField();
		txtFODisUL = new JTextField();
		lblFOCur = new JLabel();
		txtFOCurLL = new JTextField();
		txtFOCurUL = new JTextField();
		lblFOPower = new JLabel();
		txtFOPowLL = new JTextField();
		txtFOPowUL = new JTextField();
		label10 = new JLabel();
		separator4 = new JSeparator();
		lblDPHd = new JLabel();
		txtDPHdLL = new JTextField();
		txtDPHdUL = new JTextField();
		lblDPDis = new JLabel();
		txtDPDisLL = new JTextField();
		txtDPDisUL = new JTextField();
		lblDPCur = new JLabel();
		txtDPCurLL = new JTextField();
		txtDPCurUL = new JTextField();
		lblDPPower = new JLabel();
		txtDPPowLL = new JTextField();
		txtDPPowUL = new JTextField();
		label11 = new JLabel();
		separator5 = new JSeparator();
		lblSOHd = new JLabel();
		txtSOHdLL = new JTextField();
		txtSOHdUL = new JTextField();
		lblSODis = new JLabel();
		txtSODisLL = new JTextField();
		txtSODisUL = new JTextField();
		lblSOCur = new JLabel();
		txtSOCurLL = new JTextField();
		txtSOCurUL = new JTextField();
		lblSOPower = new JLabel();
		txtSOPowLL = new JTextField();
		txtSOPowUL = new JTextField();

		//======== this ========
		setTitle("Doer PumpView: Routine Test Limits");
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
			{5, TableLayout.FILL, 5}}));
		((TableLayout)contentPane.getLayout()).setHGap(5);
		((TableLayout)contentPane.getLayout()).setVGap(5);

		//======== pnlHead ========
		{
			pnlHead.setBorder(new TitledBorder(null, "Routine Test Lower and Upper Limits", TitledBorder.CENTER, TitledBorder.TOP,
				new Font("Arial", Font.BOLD, 14), Color.blue));
			pnlHead.setFocusable(false);
			pnlHead.setLayout(new TableLayout(new double[][] {
				{TableLayout.FILL, TableLayout.FILL},
				{TableLayout.PREFERRED, 5, TableLayout.PREFERRED}}));
			((TableLayout)pnlHead.getLayout()).setHGap(5);
			((TableLayout)pnlHead.getLayout()).setVGap(5);

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
			pnlHead.add(cmdExit, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//======== pnlLimits ========
			{
				pnlLimits.setLayout(new TableLayout(new double[][] {
					{TableLayout.PREFERRED, 50, 50, 165, 50, 50, TableLayout.PREFERRED, 50, 50, TableLayout.PREFERRED, 50, 50},
					{TableLayout.PREFERRED, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, TableLayout.PREFERRED}}));
				((TableLayout)pnlLimits.getLayout()).setHGap(5);
				((TableLayout)pnlLimits.getLayout()).setVGap(5);

				//---- label9 ----
				label9.setText("Full Open (FO) Test");
				label9.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 12));
				label9.setHorizontalAlignment(SwingConstants.LEFT);
				label9.setVerticalAlignment(SwingConstants.TOP);
				pnlLimits.add(label9, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				pnlLimits.add(separator3, new TableLayoutConstraints(1, 0, 11, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- lblFOHd ----
				lblFOHd.setText("Full Open Head (m)");
				lblFOHd.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlLimits.add(lblFOHd, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtFOHdLL ----
				txtFOHdLL.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				pnlLimits.add(txtFOHdLL, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- txtFOHdUL ----
				txtFOHdUL.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				pnlLimits.add(txtFOHdUL, new TableLayoutConstraints(2, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblFODis ----
				lblFODis.setText("Full Open Discharge");
				lblFODis.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlLimits.add(lblFODis, new TableLayoutConstraints(3, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtFODisLL ----
				txtFODisLL.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				pnlLimits.add(txtFODisLL, new TableLayoutConstraints(4, 1, 4, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- txtFODisUL ----
				txtFODisUL.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				pnlLimits.add(txtFODisUL, new TableLayoutConstraints(5, 1, 5, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblFOCur ----
				lblFOCur.setText("Full Open Current (A)");
				lblFOCur.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlLimits.add(lblFOCur, new TableLayoutConstraints(6, 1, 6, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtFOCurLL ----
				txtFOCurLL.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				pnlLimits.add(txtFOCurLL, new TableLayoutConstraints(7, 1, 7, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- txtFOCurUL ----
				txtFOCurUL.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				pnlLimits.add(txtFOCurUL, new TableLayoutConstraints(8, 1, 8, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblFOPower ----
				lblFOPower.setText("Full Open Power (kW)");
				lblFOPower.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlLimits.add(lblFOPower, new TableLayoutConstraints(9, 1, 9, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtFOPowLL ----
				txtFOPowLL.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				pnlLimits.add(txtFOPowLL, new TableLayoutConstraints(10, 1, 10, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- txtFOPowUL ----
				txtFOPowUL.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				pnlLimits.add(txtFOPowUL, new TableLayoutConstraints(11, 1, 11, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label10 ----
				label10.setText("Duty Point Test");
				label10.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 12));
				label10.setHorizontalAlignment(SwingConstants.LEFT);
				label10.setVerticalAlignment(SwingConstants.TOP);
				pnlLimits.add(label10, new TableLayoutConstraints(0, 3, 0, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				pnlLimits.add(separator4, new TableLayoutConstraints(1, 3, 11, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- lblDPHd ----
				lblDPHd.setText("Duty Point Head (m)");
				lblDPHd.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlLimits.add(lblDPHd, new TableLayoutConstraints(0, 4, 0, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtDPHdLL ----
				txtDPHdLL.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				pnlLimits.add(txtDPHdLL, new TableLayoutConstraints(1, 4, 1, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- txtDPHdUL ----
				txtDPHdUL.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				pnlLimits.add(txtDPHdUL, new TableLayoutConstraints(2, 4, 2, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblDPDis ----
				lblDPDis.setText("Duty Point Discharge");
				lblDPDis.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlLimits.add(lblDPDis, new TableLayoutConstraints(3, 4, 3, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtDPDisLL ----
				txtDPDisLL.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				pnlLimits.add(txtDPDisLL, new TableLayoutConstraints(4, 4, 4, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- txtDPDisUL ----
				txtDPDisUL.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				pnlLimits.add(txtDPDisUL, new TableLayoutConstraints(5, 4, 5, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblDPCur ----
				lblDPCur.setText("Duty Point Current (A)");
				lblDPCur.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlLimits.add(lblDPCur, new TableLayoutConstraints(6, 4, 6, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtDPCurLL ----
				txtDPCurLL.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				pnlLimits.add(txtDPCurLL, new TableLayoutConstraints(7, 4, 7, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- txtDPCurUL ----
				txtDPCurUL.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				pnlLimits.add(txtDPCurUL, new TableLayoutConstraints(8, 4, 8, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblDPPower ----
				lblDPPower.setText("Duty Point Power (kW)");
				lblDPPower.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlLimits.add(lblDPPower, new TableLayoutConstraints(9, 4, 9, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtDPPowLL ----
				txtDPPowLL.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				pnlLimits.add(txtDPPowLL, new TableLayoutConstraints(10, 4, 10, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- txtDPPowUL ----
				txtDPPowUL.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				pnlLimits.add(txtDPPowUL, new TableLayoutConstraints(11, 4, 11, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label11 ----
				label11.setText("Shutoff Pres. (SO) Test");
				label11.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 12));
				label11.setHorizontalAlignment(SwingConstants.LEFT);
				label11.setVerticalAlignment(SwingConstants.TOP);
				pnlLimits.add(label11, new TableLayoutConstraints(0, 6, 0, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				pnlLimits.add(separator5, new TableLayoutConstraints(1, 6, 11, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- lblSOHd ----
				lblSOHd.setText("Shut. Pres. Head (m)");
				lblSOHd.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlLimits.add(lblSOHd, new TableLayoutConstraints(0, 7, 0, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtSOHdLL ----
				txtSOHdLL.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				pnlLimits.add(txtSOHdLL, new TableLayoutConstraints(1, 7, 1, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- txtSOHdUL ----
				txtSOHdUL.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				pnlLimits.add(txtSOHdUL, new TableLayoutConstraints(2, 7, 2, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblSODis ----
				lblSODis.setText("Shut. Pres. Discharge");
				lblSODis.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlLimits.add(lblSODis, new TableLayoutConstraints(3, 7, 3, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtSODisLL ----
				txtSODisLL.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				pnlLimits.add(txtSODisLL, new TableLayoutConstraints(4, 7, 4, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- txtSODisUL ----
				txtSODisUL.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				pnlLimits.add(txtSODisUL, new TableLayoutConstraints(5, 7, 5, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblSOCur ----
				lblSOCur.setText("Shut. Pres. Current (A)");
				lblSOCur.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlLimits.add(lblSOCur, new TableLayoutConstraints(6, 7, 6, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtSOCurLL ----
				txtSOCurLL.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				pnlLimits.add(txtSOCurLL, new TableLayoutConstraints(7, 7, 7, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- txtSOCurUL ----
				txtSOCurUL.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				pnlLimits.add(txtSOCurUL, new TableLayoutConstraints(8, 7, 8, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblSOPower ----
				lblSOPower.setText("Shut. Pres. Power (kW)");
				lblSOPower.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlLimits.add(lblSOPower, new TableLayoutConstraints(9, 7, 9, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtSOPowLL ----
				txtSOPowLL.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				pnlLimits.add(txtSOPowLL, new TableLayoutConstraints(10, 7, 10, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- txtSOPowUL ----
				txtSOPowUL.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				pnlLimits.add(txtSOPowUL, new TableLayoutConstraints(11, 7, 11, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			pnlHead.add(pnlLimits, new TableLayoutConstraints(0, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
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
	
	
	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel pnlHead;
	private JButton cmdSave;
	private JButton cmdExit;
	private JPanel pnlLimits;
	private JLabel label9;
	private JSeparator separator3;
	private JLabel lblFOHd;
	private JTextField txtFOHdLL;
	private JTextField txtFOHdUL;
	private JLabel lblFODis;
	private JTextField txtFODisLL;
	private JTextField txtFODisUL;
	private JLabel lblFOCur;
	private JTextField txtFOCurLL;
	private JTextField txtFOCurUL;
	private JLabel lblFOPower;
	private JTextField txtFOPowLL;
	private JTextField txtFOPowUL;
	private JLabel label10;
	private JSeparator separator4;
	private JLabel lblDPHd;
	private JTextField txtDPHdLL;
	private JTextField txtDPHdUL;
	private JLabel lblDPDis;
	private JTextField txtDPDisLL;
	private JTextField txtDPDisUL;
	private JLabel lblDPCur;
	private JTextField txtDPCurLL;
	private JTextField txtDPCurUL;
	private JLabel lblDPPower;
	private JTextField txtDPPowLL;
	private JTextField txtDPPowUL;
	private JLabel label11;
	private JSeparator separator5;
	private JLabel lblSOHd;
	private JTextField txtSOHdLL;
	private JTextField txtSOHdUL;
	private JLabel lblSODis;
	private JTextField txtSODisLL;
	private JTextField txtSODisUL;
	private JLabel lblSOCur;
	private JTextField txtSOCurLL;
	private JTextField txtSOCurUL;
	private JLabel lblSOPower;
	private JTextField txtSOPowLL;
	private JTextField txtSOPowUL;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
