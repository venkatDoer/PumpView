/*
 * Created by JFormDesigner on Mon Aug 27 16:47:29 IST 2012
 */

package doer.pv;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

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
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.TitledBorder;

import com.toedter.calendar.JDateChooser;

import doer.io.Database;
import doer.io.MyResultSet;
import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

/**
 * @author VENKATESAN SELVARAJ
 */
public class FileOpen extends JDialog {
	public FileOpen(Frame owner) {
		super(owner);
		initProg = true;
		initComponents();
		mainFormRef = (PumpView) owner;
		pumpType = Configuration.OP_PUMP_TYPE.get(Configuration.LAST_USED_STATION).isEmpty() ? Configuration.LAST_USED_PUMP_TYPE : Configuration.OP_PUMP_TYPE.get(Configuration.LAST_USED_STATION);
		custInit();
		initProg = false;
		// load existing test list
		loadExistingTest();
	}

	private void cmdCancelActionPerformed() {
		thisWindowClosing();
		this.setVisible(false);
	}

	private void cmdOpenActionPerformed() {
		loadExistingTest();
		if (result == 0) {
			JOptionPane.showMessageDialog(this, "No records found to open.\nChange your filter criteria and try again.");
			return;
		}
		mainFormRef.openFile("select * from " + Configuration.READING_DETAIL + " " + filterText + " order by test_date, pump_slno, rowid", true, false);
		this.setVisible(false);
	}

	private void thisWindowClosing() {
	}

	private void cmbTypeActionPerformed() {
		loadExistingTest();
	}

	private void cmbLineActionPerformed() {
		loadExistingTest();
	}

	private void txtSlNoFromFocusLost() {
		loadExistingTest();
	}

	private void txtSlNoToFocusLost() {
		loadExistingTest();
	}

	private void fromDtPropertyChange() {
		loadExistingTest();
	}

	private void toDtPropertyChange() {
		loadExistingTest();
	}

	private void cmbRemarksActionPerformed() {
		loadExistingTest();
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		panel2 = new JPanel();
		pnlFilter = new JPanel();
		label1 = new JLabel();
		cmbType = new JComboBox();
		label2 = new JLabel();
		label8 = new JLabel();
		fromDt = new JDateChooser();
		label9 = new JLabel();
		toDt = new JDateChooser();
		label4 = new JLabel();
		label10 = new JLabel();
		txtSlNoFrom = new JTextField();
		label11 = new JLabel();
		txtSlNoTo = new JTextField();
		label5 = new JLabel();
		cmbRemarks = new JComboBox();
		label3 = new JLabel();
		cmbLine = new JComboBox();
		lblResult = new JLabel();
		cmdOpen = new JButton();
		cmdCancel = new JButton();

		//======== this ========
		setTitle("Doer PumpViewPro: File Open");
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
			{10, TableLayout.PREFERRED, 10},
			{10, TableLayout.PREFERRED}}));
		((TableLayout)contentPane.getLayout()).setHGap(5);
		((TableLayout)contentPane.getLayout()).setVGap(5);

		//======== panel2 ========
		{
			panel2.setBorder(new TitledBorder(null, "Existing Tests", TitledBorder.LEADING, TitledBorder.TOP,
				new Font("Arial", Font.BOLD, 14), Color.blue));
			panel2.setFocusable(false);
			panel2.setLayout(new TableLayout(new double[][] {
				{TableLayout.PREFERRED, TableLayout.PREFERRED},
				{TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, 5}}));
			((TableLayout)panel2.getLayout()).setHGap(5);
			((TableLayout)panel2.getLayout()).setVGap(5);

			//======== pnlFilter ========
			{
				pnlFilter.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlFilter.setBorder(new TitledBorder("Filter"));
				pnlFilter.setLayout(new TableLayout(new double[][] {
					{TableLayout.PREFERRED, TableLayout.PREFERRED, 150, TableLayout.PREFERRED, 150},
					{TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED}}));
				((TableLayout)pnlFilter.getLayout()).setHGap(5);
				((TableLayout)pnlFilter.getLayout()).setVGap(5);

				//---- label1 ----
				label1.setText("Pump Type");
				label1.setFont(new Font("Arial", Font.PLAIN, 14));
				pnlFilter.add(label1, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmbType ----
				cmbType.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				cmbType.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cmbTypeActionPerformed();
					}
				});
				pnlFilter.add(cmbType, new TableLayoutConstraints(2, 0, 4, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label2 ----
				label2.setText("Date (DD-MM-YYYY)");
				label2.setFont(new Font("Arial", Font.PLAIN, 14));
				pnlFilter.add(label2, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label8 ----
				label8.setText("From");
				label8.setFont(new Font("Arial", Font.BOLD, 14));
				pnlFilter.add(label8, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- fromDt ----
				fromDt.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				fromDt.setDateFormatString("dd-MM-yyyy");
				fromDt.addPropertyChangeListener(new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent e) {
						fromDtPropertyChange();
					}
				});
				pnlFilter.add(fromDt, new TableLayoutConstraints(2, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label9 ----
				label9.setText("To");
				label9.setFont(new Font("Arial", Font.BOLD, 14));
				pnlFilter.add(label9, new TableLayoutConstraints(3, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- toDt ----
				toDt.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				toDt.setDateFormatString("dd-MM-yyyy");
				toDt.addPropertyChangeListener(new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent e) {
						toDtPropertyChange();
					}
				});
				pnlFilter.add(toDt, new TableLayoutConstraints(4, 1, 4, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label4 ----
				label4.setText("Pump Serial Number");
				label4.setFont(new Font("Arial", Font.PLAIN, 14));
				pnlFilter.add(label4, new TableLayoutConstraints(0, 2, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label10 ----
				label10.setText("From");
				label10.setFont(new Font("Arial", Font.BOLD, 14));
				pnlFilter.add(label10, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- txtSlNoFrom ----
				txtSlNoFrom.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				txtSlNoFrom.addFocusListener(new FocusAdapter() {
					@Override
					public void focusLost(FocusEvent e) {
						txtSlNoFromFocusLost();
					}
				});
				pnlFilter.add(txtSlNoFrom, new TableLayoutConstraints(2, 2, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label11 ----
				label11.setText("To");
				label11.setFont(new Font("Arial", Font.BOLD, 14));
				pnlFilter.add(label11, new TableLayoutConstraints(3, 2, 3, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- txtSlNoTo ----
				txtSlNoTo.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				txtSlNoTo.addFocusListener(new FocusAdapter() {
					@Override
					public void focusLost(FocusEvent e) {
						txtSlNoToFocusLost();
					}
				});
				pnlFilter.add(txtSlNoTo, new TableLayoutConstraints(4, 2, 4, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label5 ----
				label5.setText("Remarks");
				label5.setFont(new Font("Arial", Font.PLAIN, 14));
				pnlFilter.add(label5, new TableLayoutConstraints(0, 3, 0, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmbRemarks ----
				cmbRemarks.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				cmbRemarks.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cmbRemarksActionPerformed();
					}
				});
				pnlFilter.add(cmbRemarks, new TableLayoutConstraints(2, 3, 4, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label3 ----
				label3.setText("Assembly Line");
				label3.setFont(new Font("Arial", Font.PLAIN, 14));
				pnlFilter.add(label3, new TableLayoutConstraints(0, 4, 1, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmbLine ----
				cmbLine.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				cmbLine.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cmbLineActionPerformed();
					}
				});
				pnlFilter.add(cmbLine, new TableLayoutConstraints(2, 4, 2, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			panel2.add(pnlFilter, new TableLayoutConstraints(0, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblResult ----
			lblResult.setText("0 Record(s) Found");
			lblResult.setFont(new Font("Arial", Font.BOLD, 14));
			lblResult.setForeground(Color.blue);
			panel2.add(lblResult, new TableLayoutConstraints(0, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdOpen ----
			cmdOpen.setText("<html>Open&nbsp;&nbsp<font size=-2>[Enter]</html>");
			cmdOpen.setFont(new Font("Arial", Font.PLAIN, 14));
			cmdOpen.setIcon(new ImageIcon(getClass().getResource("/img/open.PNG")));
			cmdOpen.setBackground(new Color(236, 233, 213));
			cmdOpen.setToolTipText("Click on this to open tests based on above selection criteria");
			cmdOpen.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					cmdOpenActionPerformed();
				}
			});
			panel2.add(cmdOpen, new TableLayoutConstraints(0, 2, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdCancel ----
			cmdCancel.setText("<html>Cancel&nbsp;&nbsp<font size=-2>[Esc]</html>");
			cmdCancel.setFont(new Font("Arial", Font.PLAIN, 14));
			cmdCancel.setIcon(new ImageIcon(getClass().getResource("/img/exit.PNG")));
			cmdCancel.setToolTipText("Click on this to close this window");
			cmdCancel.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					cmdCancelActionPerformed();
				}
			});
			panel2.add(cmdCancel, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		}
		contentPane.add(panel2, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel panel2;
	private JPanel pnlFilter;
	private JLabel label1;
	private JComboBox cmbType;
	private JLabel label2;
	private JLabel label8;
	private JDateChooser fromDt;
	private JLabel label9;
	private JDateChooser toDt;
	private JLabel label4;
	private JLabel label10;
	private JTextField txtSlNoFrom;
	private JLabel label11;
	private JTextField txtSlNoTo;
	private JLabel label5;
	private JComboBox cmbRemarks;
	private JLabel label3;
	private JComboBox cmbLine;
	private JLabel lblResult;
	private JButton cmdOpen;
	private JButton cmdCancel;
	// JFormDesigner - End of variables declaration  //GEN-END:variables

	// custom code - begin
	private void custInit() {
		// associate func keys
		associateFunctionKeys();
		
		// other ini
		MyResultSet res = null;
		db = Database.getInstance();
		try {
			
			// load existing pump type and choose the default one
			res = db.executeQuery("select type from " + Configuration.PUMPTYPE);
			//cmbType.addItem("ALL");
			while (res.next()) {
				cmbType.addItem(res.getString("type"));
			}
			
			if (pumpType.isEmpty()) {
				cmbType.setSelectedIndex(0);
			}
			else {
				cmbType.setSelectedItem(pumpType);
			}
			
			// load existing lines
			cmbLine.addItem("ALL");
			for (int i=1; i<=Integer.parseInt(Configuration.NUMBER_OF_LINES); i++) {
				cmbLine.addItem("Line " + i);
			}
			cmbLine.setSelectedItem(Configuration.LINE_NAME);
			
			//load existing unique remarks
			cmbRemarks.addItem("ALL");
			cmbRemarks.addItem("EMPTY");
			cmbRemarks.addItem("NON EMPTY");
			res = db.executeQuery("select distinct(remarks) as remark from " + Configuration.READING_DETAIL + " where remarks <> ''");
			while (res.next()) {
				cmbRemarks.addItem(res.getString("remark"));
			}
			
		} catch (SQLException se) {
			if (se.getMessage() != null) {
				JOptionPane.showMessageDialog(this, "DB Error:" + se.getMessage());
			}
			return;
		} 
	}
	
	private void loadExistingTest() {
		// load available tests based on filter selection
		if (initProg) {
			return;
		}
		
		filterText = "";
		result = 0;
		String typeFilter = "";
		String dateFilter = "";
		String snoFilter = "";
		String lineFilter = "";
		String remFilter = "";
		String filterTitle = "Filter [None]";
		if ( cmbType.getSelectedIndex() >= 0) {
			typeFilter = "pump_type_id=(select pump_type_id from " + Configuration.PUMPTYPE + " where type='" + cmbType.getSelectedItem().toString() + "')";
		}
		
		if (fromDt.getDate() != null && toDt.getDate() != null) {
			if (!fromDt.getDate().toString().isEmpty() && !toDt.getDate().toString().isEmpty()) {
			dateFilter = "test_date between '" + dbDtFormat.format(fromDt.getDate()) + "' and '" + dbDtFormat.format(toDt.getDate()) + "'";
			}
		}
		
		if (!txtSlNoFrom.getText().isEmpty() && !txtSlNoTo.getText().isEmpty()) {
			snoFilter = "pump_slno between '" + txtSlNoFrom.getText() + "' and '" + txtSlNoTo.getText() + "'";
		}
		
		if ( cmbLine.getSelectedIndex() > 0) {
			lineFilter = "line='" + cmbLine.getSelectedItem().toString() + "'";
		}
		
		if ( cmbRemarks.getSelectedIndex() > 0) {
			if ( cmbRemarks.getSelectedIndex() == 1) {
				remFilter = "remarks = ''";
			} else if ( cmbRemarks.getSelectedIndex() == 2) {
					remFilter = "remarks <> ''";
			} else {
				remFilter = "remarks='" + cmbRemarks.getSelectedItem().toString() + "'";
			}
		}
		
		if (!typeFilter.isEmpty() || !dateFilter.isEmpty() || !snoFilter.isEmpty() || !lineFilter.isEmpty() || !remFilter.isEmpty()) {
			if (!typeFilter.isEmpty()) {
				filterText = typeFilter;
			}
			
			if (!dateFilter.isEmpty()) {
				if (filterText.isEmpty()) {
					filterText = dateFilter;
				} else {
					filterText += " and " + dateFilter;
				}
			}
			
			if (!snoFilter.isEmpty()) {
				if (filterText.isEmpty()) {
					filterText = snoFilter;
				} else {
					filterText += " and " + snoFilter;
				}
			}
			
			if (!lineFilter.isEmpty()) {
				if (filterText.isEmpty()) {
					filterText = lineFilter;
				} else {
					filterText += " and " + lineFilter;
				}
			}
			
			if (!remFilter.isEmpty()) {
				if (filterText.isEmpty()) {
					filterText = remFilter;
				} else {
					filterText += " and " + remFilter;
				}
			}
			
			filterText = " where " + filterText;
			filterTitle = "Filter [" + filterText + "]";
		}
		
		pnlFilter.setBorder(new TitledBorder(null, filterTitle, TitledBorder.LEADING, TitledBorder.TOP,
				new Font("Arial", Font.BOLD, 12), Color.blue));
		pnlFilter.setToolTipText(filterTitle);
		
		try {
			MyResultSet res = db.executeQuery("select count(distinct(pump_slno)) as tot from " + Configuration.READING_DETAIL + " " + filterText);
			if (res.next()) {
				result = res.getLong("tot");
			}
				
		} catch (Exception se) {
			se.printStackTrace();
			if (se.getMessage() != null) {
				JOptionPane.showMessageDialog(this, "Error loading the list:" + se.getMessage());
			}
		}
		lblResult.setText(result + " Test(s) Found");
	}
	
	private void associateFunctionKeys() {
		// associate enter for choose
		String CHOOSE_ACTION_KEY = "chooseAction";
		Action chooseAction = new AbstractAction() {
		      public void actionPerformed(ActionEvent actionEvent) {
		        cmdOpenActionPerformed();
		      }
		    };
		KeyStroke entr = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		InputMap chooseInputMap = cmdOpen.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		chooseInputMap.put(entr, CHOOSE_ACTION_KEY);
		ActionMap chooseActionMap = cmdOpen.getActionMap();
		chooseActionMap.put(CHOOSE_ACTION_KEY, chooseAction);
		cmdOpen.setActionMap(chooseActionMap);
		
		// associate Esc for exit
		String CLOSE_ACTION_KEY = "closeAction";
		Action closeAction = new AbstractAction() {
		      public void actionPerformed(ActionEvent actionEvent) {
		        cmdCancelActionPerformed();
		      }
		    };
	    KeyStroke esc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		InputMap closeInputMap = cmdCancel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		closeInputMap.put(esc, CLOSE_ACTION_KEY);
		ActionMap closeActionMap = cmdCancel.getActionMap();
		closeActionMap.put(CLOSE_ACTION_KEY, closeAction);
		cmdCancel.setActionMap(closeActionMap);
	}
	
	private Database db = null;
	private String pumpType = ""; 
	private SimpleDateFormat reqDtFormat = new SimpleDateFormat("dd-MM-yyyy");
	private SimpleDateFormat dbDtFormat = new SimpleDateFormat("yyyy-MM-dd");
	private PumpView mainFormRef;
	private String filterText = "";
	private long result = 0;
	private boolean initProg = false;
	
	// custom code - end
}
