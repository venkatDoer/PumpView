/*
 * Created by JFormDesigner on Wed Nov 30 10:06:49 IST 2022
 */

package doer.pv;

import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.text.DecimalFormat;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;

import doer.io.Database;
import doer.io.MyResultSet;
import info.clearthought.layout.*;

/**
 * @author VENKATESAN SELVARAJ
 */
public class NonISITolerance extends JDialog {
	public NonISITolerance(Frame owner) {
		super(owner);
		initComponents();
	}
	
	public NonISITolerance(Dialog owner, String pumpTypeId) {
		super(owner);
		frmMain = (PumpType) owner;
		initComponents();
		associateFunctionKeys();
		
		curPumpTypeId = pumpTypeId;
		
		// isi handling
		if (!Configuration.LAST_USED_ISSTD.startsWith("IS 8472:")) {
			txtPowLL.setEnabled(false);
			txtPowUL.setEnabled(false);
			lblPower.setEnabled(false);
		}
		
		loadSettings();
		
	}
	
	private void loadSettings() {
		try {
			db = Database.getInstance();
			
			MyResultSet res = db.executeQuery("select * from " + Configuration.NON_ISI_PERF_TOLERANCE + " where pump_type_id='" + curPumpTypeId + "'");
			if (res.next()) {
				txtHdLL.setText(res.getString("head_ll"));
				txtHdUL.setText(res.getString("head_ul"));
				txtDisLL.setText(res.getString("discharge_ll"));
				txtDisUL.setText(res.getString("discharge_ul"));
				txtCurLL.setText(res.getString("current_ll"));
				txtCurUL.setText(res.getString("current_ul"));
				txtPowLL.setText(res.getString("power_ll"));
				txtPowUL.setText(res.getString("power_ul"));
				txtEffLL.setText(res.getString("eff_ll"));
				txtEffUL.setText(res.getString("eff_ul"));
			} else {
				setISIDefPerfValues();
			}
		} catch (Exception sqle) {
			JOptionPane.showMessageDialog(this, "Error loading tolerance:" + sqle.getMessage());
			return;
		}
	}
	
	private void setISIDefPerfValues() {
		// ISI default
		txtHdLL.setText(Configuration.ISI_PERF_HEAD_LL > 0 ? Configuration.ISI_PERF_HEAD_LL.toString() : "");
		txtHdUL.setText(Configuration.ISI_PERF_HEAD_UL < 99999 ?Configuration.ISI_PERF_HEAD_UL.toString() : "");
		txtDisLL.setText(Configuration.ISI_PERF_DIS_LL > 0 ?Configuration.ISI_PERF_DIS_LL.toString() : "");
		txtDisUL.setText(Configuration.ISI_PERF_DIS_UL < 99999 ?Configuration.ISI_PERF_DIS_UL.toString() : "");
		txtCurLL.setText(Configuration.ISI_PERF_CUR_LL > 0 ?Configuration.ISI_PERF_CUR_LL.toString() : "");
		txtCurUL.setText(Configuration.ISI_PERF_CUR_UL < 99999 ?Configuration.ISI_PERF_CUR_UL.toString() : "");
		txtPowLL.setText(Configuration.ISI_PERF_POW_LL > 0 ?Configuration.ISI_PERF_POW_LL.toString() : "");
		txtPowUL.setText(Configuration.ISI_PERF_POW_UL < 99999 ?Configuration.ISI_PERF_POW_UL.toString() : "");
		txtEffLL.setText(Configuration.ISI_PERF_EFF_LL > 0 ?Configuration.ISI_PERF_EFF_LL.toString() : "");
		txtEffUL.setText(Configuration.ISI_PERF_EFF_UL < 99999 ?Configuration.ISI_PERF_EFF_UL.toString() : "");
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
				
		try {
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			
			db.executeUpdate("insert into " + Configuration.NON_ISI_PERF_TOLERANCE + "(pump_type_id, head_ll, head_ul, discharge_ll, discharge_ul, current_ll, current_ul, power_ll, power_ul, eff_ll, eff_ul) values ('" + 
					   curPumpTypeId + "', '" + txtHdLL.getText().trim() + "', '"+ txtHdUL.getText().trim() + "','" + txtDisLL.getText().trim() + "','" + txtDisUL.getText().trim() + "','" + 
					   txtCurLL.getText().trim() + "','" + txtCurUL.getText().trim() + "','" + txtPowLL.getText().trim() + "','" + txtPowUL.getText().trim() + "','" + txtEffLL.getText().trim() + "','" + txtEffUL.getText().trim() + "')");
		} catch (SQLException se) {
			try {
				if (se.getMessage().contains("must be unique")) {
					db.executeUpdate("update " + Configuration.NON_ISI_PERF_TOLERANCE + " set " + 
							   "head_ll='" + txtHdLL.getText().trim() + "', head_ul='"+ txtHdUL.getText().trim() + "',discharge_ll='" + txtDisLL.getText().trim() + "',discharge_ul='" + txtDisUL.getText().trim() + "',current_ll='" + 
							   txtCurLL.getText().trim() + "',current_ul='" + txtCurUL.getText().trim() + "',power_ll='" + txtPowLL.getText().trim() + "',power_ul='" + txtPowUL.getText().trim() + "',eff_ll='" + txtEffLL.getText().trim() + "',eff_ul='" + txtEffUL.getText().trim() + "' where pump_type_id='" + curPumpTypeId +"'");
				} else {
					throw se;
				}
			} catch (SQLException sqle) {
				JOptionPane.showMessageDialog(this, "Failed updating tolerance value:" + sqle.getMessage());
				this.setCursor(Cursor.getDefaultCursor());
				return;
			}
		} 
		// refresh main form
		frmMain.updatePumpType("update");
		this.setCursor(Cursor.getDefaultCursor());
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

	private void thisWindowClosing() {
		// TODO add your code here
	}

	private void cmdExitActionPerformed() {
		this.setVisible(false);
		thisWindowClosing();
	}

	private void cmdResetActionPerformed() {
		setISIDefPerfValues();
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		pnlHead = new JPanel();
		cmdSave = new JButton();
		cmdExit = new JButton();
		pnlLimits = new JPanel();
		label1 = new JLabel();
		label10 = new JLabel();
		label12 = new JLabel();
		lblDis = new JLabel();
		txtDisLL = new JTextField();
		txtDisUL = new JTextField();
		lblHd = new JLabel();
		txtHdLL = new JTextField();
		txtHdUL = new JTextField();
		lblPower = new JLabel();
		txtPowLL = new JTextField();
		txtPowUL = new JTextField();
		lblEff = new JLabel();
		txtEffLL = new JTextField();
		txtEffUL = new JTextField();
		lblCur = new JLabel();
		txtCurLL = new JTextField();
		txtCurUL = new JTextField();
		cmdReset = new JButton();

		//======== this ========
		setTitle("Doer PumpView: Performance Tolerance");
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
			{5, 352, 5},
			{5, TableLayout.FILL, 5}}));
		((TableLayout)contentPane.getLayout()).setHGap(5);
		((TableLayout)contentPane.getLayout()).setVGap(5);

		//======== pnlHead ========
		{
			pnlHead.setBorder(new TitledBorder(null, "Performance Tolerance For Non-ISI Pump", TitledBorder.CENTER, TitledBorder.TOP,
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
					{TableLayout.FILL, TableLayout.PREFERRED, TableLayout.PREFERRED},
					{50, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED}}));
				((TableLayout)pnlLimits.getLayout()).setHGap(5);
				((TableLayout)pnlLimits.getLayout()).setVGap(5);

				//---- label1 ----
				label1.setText("<html>If required, customize below perfromance tolerance to derive the test result accordingly in performance report. Otherwise, default tolerance as per ISI std will be considered.</html>");
				label1.setFont(new Font("Arial", Font.ITALIC, 12));
				label1.setForeground(Color.blue);
				pnlLimits.add(label1, new TableLayoutConstraints(0, 0, 2, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label10 ----
				label10.setText("Minimum %");
				label10.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 12));
				label10.setHorizontalAlignment(SwingConstants.LEFT);
				label10.setVerticalAlignment(SwingConstants.TOP);
				pnlLimits.add(label10, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label12 ----
				label12.setText("Maximum %");
				label12.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 12));
				label12.setHorizontalAlignment(SwingConstants.LEFT);
				label12.setVerticalAlignment(SwingConstants.TOP);
				pnlLimits.add(label12, new TableLayoutConstraints(2, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblDis ----
				lblDis.setText("Discharge");
				lblDis.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlLimits.add(lblDis, new TableLayoutConstraints(0, 2, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtDisLL ----
				txtDisLL.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				pnlLimits.add(txtDisLL, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- txtDisUL ----
				txtDisUL.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				pnlLimits.add(txtDisUL, new TableLayoutConstraints(2, 2, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblHd ----
				lblHd.setText("Total Head");
				lblHd.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlLimits.add(lblHd, new TableLayoutConstraints(0, 3, 0, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtHdLL ----
				txtHdLL.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				pnlLimits.add(txtHdLL, new TableLayoutConstraints(1, 3, 1, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- txtHdUL ----
				txtHdUL.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				pnlLimits.add(txtHdUL, new TableLayoutConstraints(2, 3, 2, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblPower ----
				lblPower.setText("Input Power");
				lblPower.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlLimits.add(lblPower, new TableLayoutConstraints(0, 4, 0, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtPowLL ----
				txtPowLL.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				pnlLimits.add(txtPowLL, new TableLayoutConstraints(1, 4, 1, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- txtPowUL ----
				txtPowUL.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				pnlLimits.add(txtPowUL, new TableLayoutConstraints(2, 4, 2, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblEff ----
				lblEff.setText("Overall Efficiency");
				lblEff.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlLimits.add(lblEff, new TableLayoutConstraints(0, 5, 0, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtEffLL ----
				txtEffLL.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				pnlLimits.add(txtEffLL, new TableLayoutConstraints(1, 5, 1, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- txtEffUL ----
				txtEffUL.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				pnlLimits.add(txtEffUL, new TableLayoutConstraints(2, 5, 2, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblCur ----
				lblCur.setText("Max Current");
				lblCur.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlLimits.add(lblCur, new TableLayoutConstraints(0, 6, 0, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtCurLL ----
				txtCurLL.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				pnlLimits.add(txtCurLL, new TableLayoutConstraints(1, 6, 1, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- txtCurUL ----
				txtCurUL.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				pnlLimits.add(txtCurUL, new TableLayoutConstraints(2, 6, 2, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmdReset ----
				cmdReset.setText("Restore Default");
				cmdReset.setFont(new Font("Arial", Font.PLAIN, 12));
				cmdReset.setToolTipText("Configure list of heads tested for this model");
				cmdReset.setMnemonic('H');
				cmdReset.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cmdResetActionPerformed();
					}
				});
				pnlLimits.add(cmdReset, new TableLayoutConstraints(1, 7, 2, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			pnlHead.add(pnlLimits, new TableLayoutConstraints(0, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		}
		contentPane.add(pnlHead, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel pnlHead;
	private JButton cmdSave;
	private JButton cmdExit;
	private JPanel pnlLimits;
	private JLabel label1;
	private JLabel label10;
	private JLabel label12;
	private JLabel lblDis;
	private JTextField txtDisLL;
	private JTextField txtDisUL;
	private JLabel lblHd;
	private JTextField txtHdLL;
	private JTextField txtHdUL;
	private JLabel lblPower;
	private JTextField txtPowLL;
	private JTextField txtPowUL;
	private JLabel lblEff;
	private JTextField txtEffLL;
	private JTextField txtEffUL;
	private JLabel lblCur;
	private JTextField txtCurLL;
	private JTextField txtCurUL;
	private JButton cmdReset;
	// JFormDesigner - End of variables declaration  //GEN-END:variables


	// custom variables
	private String curPumpTypeId = "";
	private Database db = null;
	PumpType frmMain = null;

}
