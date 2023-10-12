/*
 * Created by JFormDesigner on Wed Jan 11 16:22:31 EST 2012
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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import doer.io.Database;
import doer.io.Encrypt;
import doer.io.MyResultSet;
import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

/**
 * @author VENKATESAN SELVARAJ
 */
public class UserManagement extends JDialog {
	public UserManagement(Frame owner) {
		super(owner);
		initComponents();
	}

	public UserManagement(Dialog owner) {
		super(owner);
		initComponents();
	}

	private void cmdAddActionPerformed() {
		// add the new type
		String curName = txtName.getText().trim();
		
		if ( curName.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please enter valid user name to add");
			txtName.requestFocusInWindow();
			return;
		}

		if ( txtPass.getText().trim().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please enter valid password");
			txtPass.requestFocusInWindow();
			return;
		}
		
		if ( !txtPass.getText().trim().equals(txtPass2.getText().trim())) {
			JOptionPane.showMessageDialog(this, "Re-entered password does not match with the password");
			txtPass2.requestFocusInWindow();
			return;
		}
		
		// check for duplication and add it if not exist already
		try {
			db.executeUpdate("insert into USER values ('" + curName + "','" + Encrypt.encrypt(txtPass.getText()) + "','" + (optAdmin.isSelected() ? Encrypt.encrypt("1") : Encrypt.encrypt("0")) + "','" + 
					(optPump.isSelected() ? Encrypt.encrypt("1") : Encrypt.encrypt("0")) + "','" + (optModify.isSelected() ? Encrypt.encrypt("1") : Encrypt.encrypt("0")) + "')");
		} catch (Exception sqle) {
			if (sqle.getMessage().contains("not unique")) {
				JOptionPane.showMessageDialog(this, "User '" + curName + "' is already exist. Please use different name and try again");
				txtName.setText("");
				txtPass.setText("");
				txtPass2.setText("");
				optAdmin.setSelected(false);
				optPump.setSelected(false);
				optModify.setSelected(false);
				txtName.requestFocus();
			} else {
				JOptionPane.showMessageDialog(this, "Failed adding new user '" + curName + "'\nDB Error:" + sqle.getMessage());
			}
			return;
		}
		
		// add into list
		DefaultTableModel defModel = (DefaultTableModel) tblUserList.getModel();
		defModel.addRow( new Object[] {null});
		tblUserList.setValueAt(curName, tblUserList.getRowCount()-1, 0);
		tblUserList.getSelectionModel().setSelectionInterval(tblUserList.getRowCount()-1, tblUserList.getRowCount()-1);
	}

	private void cmdUpdateActionPerformed() {
		// update the selected user
		String selName = null;
		if ( tblUserList.getSelectedRow() < 0) {
			JOptionPane.showMessageDialog(this, "Please select an existing user from the list before updating it"); 
			return;
		}
		else {
			selName = tblUserList.getValueAt(tblUserList.getSelectedRow(), 0).toString();
		}
		
		String curName = txtName.getText().trim();
		if ( curName.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please enter an user name before updating"); 
			return;
		}
		
		if ( txtPass.getText().trim().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please enter valid password");
			txtPass.requestFocusInWindow();
			return;
		}
		
		if ( !txtPass.getText().trim().equals(txtPass2.getText().trim())) {
			JOptionPane.showMessageDialog(this, "Re-entered password does not match with the password");
			txtPass2.requestFocusInWindow();
			return;
		}
		
		int response = JOptionPane.showConfirmDialog(this, "Do you want to update user '" + selName + "'?");
		if (response != 0) {
			return;
		}
		// update the user
		this.setCursor(waitCursor);
		try {
			// check for atleast one admin
			MyResultSet res = db.executeQuery("select count(*) as tot from USER where admin='" + Encrypt.encrypt("1") + "'");
			res.next();
			if (res.getInt("tot") == 1 ) {
				res = db.executeQuery("select count(*) as tot from USER where name='" + curName + "' and admin='" + Encrypt.encrypt("1") + "'");
				res.next();
				if (res.getInt("tot") == 1 && !optAdmin.isSelected()) {
					JOptionPane.showMessageDialog(this, "The user you are currently updating is the only administrator for this application.\nSo, you can not deselect the administrator option. Hence selecting the option automatically.");
					optAdmin.setSelected(true);
				}	
			}
			
			// update it
			db.executeUpdate("update USER set name='" + curName + "',password='" + Encrypt.encrypt(txtPass.getText()) + "',admin='" + (optAdmin.isSelected() ? Encrypt.encrypt("1") : Encrypt.encrypt("0")) + "'," +
					"pump_access='" + (optPump.isSelected() ? Encrypt.encrypt("1") : Encrypt.encrypt("0")) + "',modify_access='" + (optModify.isSelected() ? Encrypt.encrypt("1") : Encrypt.encrypt("0")) + "' where name='" + selName +"'");
		} catch (Exception sqle) {
			if (sqle.getMessage().contains("not unique")) {
				JOptionPane.showMessageDialog(this, "User '" + curName + "' is already exist. Please use different name and try again");
			} else {
				JOptionPane.showMessageDialog(this, "Failed updating user '" + selName + "'\nDB Error:" + sqle.getMessage());
			}
			this.setCursor(defCursor);
			return;
		}
		
		// update the list
		tblUserList.setValueAt(curName, tblUserList.getSelectedRow(), 0);
		
		// update the global values if the user being updated is same as the logged in user
		if (curName.equals(Configuration.USER)) {
			Configuration.USER_IS_ADMIN = optAdmin.isSelected() ? "1": "0";
			Configuration.USER_HAS_PUMP_ACCESS = optPump.isSelected() ? "1" : "0";
			Configuration.USER_HAS_MODIFY_ACCESS = optModify.isSelected() ? "1" : "0";
		}
		
		this.setCursor(defCursor);
		JOptionPane.showMessageDialog(this, "Changes are saved successfully!");
		
	}

	private void cmdDelActionPerformed() {
		// delete the selected user
		
		if (tblUserList.getRowCount() == 1) {
			JOptionPane.showMessageDialog(this, "It is not possible to delete all the users as at least one user must be exist to login to this application");
			return;
		}
		
		String selName = null;
		if ( tblUserList.getSelectedRow() < 0) {
			JOptionPane.showMessageDialog(this, "Please select an existing user from the list before deleting it"); 
			return;
		}
		else {
			selName = tblUserList.getValueAt(tblUserList.getSelectedRow(), 0).toString();
		}
	
		int response = JOptionPane.showConfirmDialog(this, "Do you want to delete user '" + selName + "'?");
		if (response != 0) {
			return;
		}
	
		// update the db
		try {
			db.executeUpdate("delete from USER where name='" + selName +"'");
		} catch (Exception sqle) {
			JOptionPane.showMessageDialog(this, "Failed deleting user '" + selName + "'\nDB Error:" + sqle.getMessage());
			return;
		}
		
		// update the list
		int selRow = tblUserList.getSelectedRow();
		for(int i=tblUserList.getSelectedRow(); i<tblUserList.getRowCount()-1; i++) {
			tblUserList.setValueAt(tblUserList.getValueAt(i+1, 0), i, 0);	
		}
		// delete last row
		DefaultTableModel defModel = (DefaultTableModel) tblUserList.getModel();
		defModel.removeRow(tblUserList.getRowCount()-1);
		// focus previous row
		if (tblUserList.getRowCount() > 0) {
			if (selRow == tblUserList.getRowCount()) {
				tblUserList.setRowSelectionInterval(selRow-1, selRow-1);
				txtName.setText(tblUserList.getValueAt(selRow-1, 0).toString());
			} else {
				tblUserList.setRowSelectionInterval(selRow, selRow);
				txtName.setText(tblUserList.getValueAt(selRow, 0).toString());
			}
			if (selRow != 0) {
				tblUserList.getSelectionModel().setSelectionInterval(selRow-1, selRow-1);
			}
		} else {
			txtName.setText("");
			txtPass.setText("");
			optAdmin.setSelected(false);
			optPump.setSelected(false);
			optModify.setSelected(false);
		}
	}

	private void cmdCloseActionPerformed() {
		thisWindowClosing(null);
		this.setVisible(false);
	}

	private void thisWindowClosing(WindowEvent e) {
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		panel1 = new JPanel();
		scrlUserList = new JScrollPane();
		tblUserList = new JTable();
		panel2 = new JPanel();
		label1 = new JLabel();
		txtName = new JTextField();
		label2 = new JLabel();
		txtPass = new JPasswordField();
		label3 = new JLabel();
		txtPass2 = new JPasswordField();
		optAdmin = new JCheckBox();
		optPump = new JCheckBox();
		optModify = new JCheckBox();
		cmdAdd = new JButton();
		cmdDel = new JButton();
		cmdUpdate = new JButton();
		cmdClose = new JButton();

		//======== this ========
		setTitle("Doer PumpView: User Management");
		setFont(new Font("Arial", Font.PLAIN, 12));
		setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		setResizable(false);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				thisWindowClosing(e);
			}
		});
		Container contentPane = getContentPane();
		contentPane.setLayout(new TableLayout(new double[][] {
			{10, TableLayout.PREFERRED, TableLayout.PREFERRED, 10},
			{10, TableLayout.PREFERRED, 10}}));
		((TableLayout)contentPane.getLayout()).setHGap(5);
		((TableLayout)contentPane.getLayout()).setVGap(5);

		//======== panel1 ========
		{
			panel1.setBorder(new TitledBorder(null, "Users [All Assembly Lines]", TitledBorder.LEADING, TitledBorder.TOP,
				new Font("Arial", Font.BOLD, 14), Color.blue));
			panel1.setFocusable(false);
			panel1.setLayout(new TableLayout(new double[][] {
				{234},
				{225}}));
			((TableLayout)panel1.getLayout()).setHGap(5);
			((TableLayout)panel1.getLayout()).setVGap(5);

			//======== scrlUserList ========
			{
				scrlUserList.setToolTipText("List of existing users");

				//---- tblUserList ----
				tblUserList.setModel(new DefaultTableModel(
					new Object[][] {
					},
					new String[] {
						null
					}
				) {
					boolean[] columnEditable = new boolean[] {
						false
					};
					@Override
					public boolean isCellEditable(int rowIndex, int columnIndex) {
						return columnEditable[columnIndex];
					}
				});
				{
					TableColumnModel cm = tblUserList.getColumnModel();
					cm.getColumn(0).setResizable(false);
				}
				tblUserList.setFont(new Font("SansSerif", Font.PLAIN, 14));
				tblUserList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				tblUserList.setToolTipText("List of existing users; click on an user to see corresponding details");
				scrlUserList.setViewportView(tblUserList);
			}
			panel1.add(scrlUserList, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		}
		contentPane.add(panel1, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

		//======== panel2 ========
		{
			panel2.setBorder(new TitledBorder(null, "User Details", TitledBorder.CENTER, TitledBorder.TOP,
				new Font("Arial", Font.BOLD, 14), Color.blue));
			panel2.setFocusable(false);
			panel2.setLayout(new TableLayout(new double[][] {
				{TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED},
				{TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED}}));
			((TableLayout)panel2.getLayout()).setHGap(5);
			((TableLayout)panel2.getLayout()).setVGap(5);

			//---- label1 ----
			label1.setText("User Name");
			label1.setFont(new Font("Arial", Font.PLAIN, 14));
			panel2.add(label1, new TableLayoutConstraints(0, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- txtName ----
			txtName.setFont(new Font("SansSerif", Font.PLAIN, 14));
			txtName.setToolTipText("The name of the user");
			panel2.add(txtName, new TableLayoutConstraints(2, 0, 3, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- label2 ----
			label2.setText("Password");
			label2.setFont(new Font("Arial", Font.PLAIN, 14));
			panel2.add(label2, new TableLayoutConstraints(0, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- txtPass ----
			txtPass.setToolTipText("Password for the user");
			txtPass.setFont(new Font("SansSerif", Font.PLAIN, 14));
			panel2.add(txtPass, new TableLayoutConstraints(2, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- label3 ----
			label3.setText("Re-enter Password");
			label3.setFont(new Font("Arial", Font.PLAIN, 14));
			panel2.add(label3, new TableLayoutConstraints(0, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- txtPass2 ----
			txtPass2.setToolTipText("Re-type the password in case of changing the existing password. This should match the password entered above always");
			txtPass2.setFont(new Font("SansSerif", Font.PLAIN, 14));
			panel2.add(txtPass2, new TableLayoutConstraints(2, 2, 3, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- optAdmin ----
			optAdmin.setText("Administrator");
			optAdmin.setFont(new Font("Arial", Font.PLAIN, 14));
			optAdmin.setToolTipText("Select this if this user can access user management screen");
			panel2.add(optAdmin, new TableLayoutConstraints(2, 3, 3, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- optPump ----
			optPump.setText("Pump Type Administration");
			optPump.setFont(new Font("Arial", Font.PLAIN, 14));
			optPump.setToolTipText("Select this if this user add, update & delete pumps");
			panel2.add(optPump, new TableLayoutConstraints(2, 4, 3, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- optModify ----
			optModify.setText("Can Modify Test Readings");
			optModify.setFont(new Font("Arial", Font.PLAIN, 14));
			optModify.setToolTipText("Selece this if user can modify captured test readings");
			panel2.add(optModify, new TableLayoutConstraints(2, 5, 3, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdAdd ----
			cmdAdd.setText("Add");
			cmdAdd.setFont(new Font("Arial", Font.PLAIN, 14));
			cmdAdd.setIcon(new ImageIcon(getClass().getResource("/img/Add.png")));
			cmdAdd.setToolTipText("Click on this to add new user that you have entered above");
			cmdAdd.setMnemonic('A');
			cmdAdd.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					cmdAddActionPerformed();
				}
			});
			panel2.add(cmdAdd, new TableLayoutConstraints(0, 6, 0, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdDel ----
			cmdDel.setText("Delete");
			cmdDel.setFont(new Font("Arial", Font.PLAIN, 14));
			cmdDel.setIcon(new ImageIcon(getClass().getResource("/img/delete.PNG")));
			cmdDel.setToolTipText("Click on this to delete the selected user");
			cmdDel.setMnemonic('D');
			cmdDel.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					cmdDelActionPerformed();
				}
			});
			panel2.add(cmdDel, new TableLayoutConstraints(1, 6, 1, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdUpdate ----
			cmdUpdate.setText("Update");
			cmdUpdate.setFont(new Font("Arial", Font.PLAIN, 14));
			cmdUpdate.setIcon(new ImageIcon(getClass().getResource("/img/save.PNG")));
			cmdUpdate.setToolTipText("Click on this to update selected user with the changes you have made above");
			cmdUpdate.setMnemonic('U');
			cmdUpdate.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					cmdUpdateActionPerformed();
				}
			});
			panel2.add(cmdUpdate, new TableLayoutConstraints(2, 6, 2, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdClose ----
			cmdClose.setText("<html>Close&nbsp;&nbsp<font size=-2>[Esc]</html>");
			cmdClose.setFont(new Font("Arial", Font.PLAIN, 14));
			cmdClose.setIcon(new ImageIcon(getClass().getResource("/img/exit.PNG")));
			cmdClose.setToolTipText("Click on this to close this window");
			cmdClose.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					cmdCloseActionPerformed();
				}
			});
			panel2.add(cmdClose, new TableLayoutConstraints(3, 6, 3, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		}
		contentPane.add(panel2, new TableLayoutConstraints(2, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents\
		
		// custom code - begin
		customInit();
		// custom code -end
	}

	private void customInit() {
		
		// db initialization
		db = Database.getInstance();
		
		// load existing users
		tblUserList.setTableHeader(null);
		tblUserList.setRowHeight(20);
		SelectionListener listener = new SelectionListener(tblUserList, this);
		tblUserList.getSelectionModel().addListSelectionListener(listener);
		tblUserList.getColumnModel().getSelectionModel().addListSelectionListener(listener);
		scrlUserList.setViewportView(tblUserList);
		refreshUserList();
		
		// select and focus the current user record by default
		if (tblUserList.getRowCount() > 0) {
			tblUserList.getSelectionModel().setSelectionInterval(curUserRow, curUserRow);
		}
		
		associateFunctionKeys();
		
		// disable buttons based on access
		if (Configuration.USER_IS_ADMIN.equals("0")) {
			this.remove(panel1);
			this.setSize(this.getWidth()-230, this.getHeight());
			txtName.setEnabled(false);
			cmdAdd.setVisible(false);
			cmdDel.setVisible(false);
			optAdmin.setEnabled(false);
			optPump.setEnabled(false);
			optModify.setEnabled(false);
		}
	}
	
	private void refreshUserList() {
		try {
			// add all available devices
			MyResultSet res = null;
			res = db.executeQuery("select * from USER");
		
			int  curRow = 0;
			while (res.next()) {
				DefaultTableModel defModel = (DefaultTableModel) tblUserList.getModel();
				defModel.addRow( new Object[] {null});
				tblUserList.setValueAt(res.getString("name"), curRow, 0);
				if (Configuration.USER.equals(res.getString("name"))) {
					curUserRow = curRow;
				}
				++curRow;
			}
			
		} catch (Exception sqle) {
			JOptionPane.showMessageDialog(this, "Failed loading existing users\nDB Error:" + sqle.getMessage());
			return;
		}
	}
	
	class SelectionListener implements ListSelectionListener {
		  JTable table;
		  JDialog pr;

		  SelectionListener(JTable table, UserManagement parent) {
		    this.table = table;
		    this.pr = parent;
		  }
		  public void valueChanged(ListSelectionEvent e) {
			  try {
				  String selName = table.getValueAt(table.getSelectedRow(), 0).toString();
				  
					// fetch corresponding record for the db
					try {
						// fetch and load the result
						MyResultSet res = null;
						res = db.executeQuery("select * from USER where name='" + selName + "'");
						
						if (res.next()) {
							txtName.setText(res.getString("name"));
							txtPass.setText(Encrypt.decrypt(res.getString("password")));
							optAdmin.setSelected(Encrypt.decrypt(res.getString("admin")).equalsIgnoreCase("1") ? true : false);
							optPump.setSelected(Encrypt.decrypt(res.getString("pump_access")).equalsIgnoreCase("1") ? true : false);
							optModify.setSelected(Encrypt.decrypt(res.getString("modify_access")).equalsIgnoreCase("1") ? true : false);
							txtPass2.setText(txtPass.getText());
						}
					} catch (Exception sqle) {
						JOptionPane.showMessageDialog(pr, "Failed loading user details for " + selName + "\nDB Error:" + sqle.getMessage());
						return;
					}
			  } catch (ArrayIndexOutOfBoundsException ex) {
				  // just ignore it
			  }
		  }
	}
	
	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel panel1;
	private JScrollPane scrlUserList;
	private JTable tblUserList;
	private JPanel panel2;
	private JLabel label1;
	private JTextField txtName;
	private JLabel label2;
	private JPasswordField txtPass;
	private JLabel label3;
	private JPasswordField txtPass2;
	private JCheckBox optAdmin;
	private JCheckBox optPump;
	private JCheckBox optModify;
	private JButton cmdAdd;
	private JButton cmdDel;
	private JButton cmdUpdate;
	private JButton cmdClose;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
	
	// custom code - begin
	private Database db = null;
	private Integer curUserRow = 0;
	
	private Cursor waitCursor = new Cursor(Cursor.WAIT_CURSOR);
	private Cursor defCursor = new Cursor(Cursor.DEFAULT_CURSOR);
	
	private void associateFunctionKeys() {
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
	// custom code -end
}
