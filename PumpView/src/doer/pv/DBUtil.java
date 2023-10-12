/*
 * Created by JFormDesigner on Fri Sep 12 11:47:37 IST 2014
 */

package doer.pv;

import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.*;
import info.clearthought.layout.*;

/**
 * @author VENKATESAN SELVARAJ
 */
public class DBUtil extends JDialog {
	public DBUtil(Frame owner) {
		super(owner);
		initComponents();
	}

	public DBUtil(Dialog owner) {
		super(owner);
		initComponents();
	}

	private void button1ActionPerformed() {
		int resp = JOptionPane.showConfirmDialog(this, "Are you sure you want to execute the above query?");
		if (resp != 0) {
			return;
		}
		
		// execute the query
		try {
			Connection conn = DriverManager.getConnection(Configuration.DB_URL);
			Statement stmt = conn.createStatement();
			stmt.setQueryTimeout(30);
			ResultSet res = null;
			
			int st = 0;
			int end = 0;
			String qry = "";
			do {
				end = txtQry.getText().indexOf(";", st);
				try {
					qry = txtQry.getText().substring(st, end).trim();
				} catch (Exception e) {
					break;
				}
				JOptionPane.showMessageDialog(this, "Executing:\n" + qry);
				if (qry.startsWith("select") || qry.startsWith("pragma")) {
					res = stmt.executeQuery(qry);
						int i = 1;
						while(res.next()) {
							// values
							while(true) {
								try {
									txtRes.append(res.getString(i) + "|");
									++i;
								}catch (SQLException se) {
									break;
								}
							}
							txtRes.append("\n");
							i=1;
						}
				} else {
					stmt.executeUpdate(qry);
				}
				st = end+1;
			} while (st < txtQry.getText().length());
			
			stmt.close();
			conn.close();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Error:" + e.getMessage());
		}
	}

	private void button2ActionPerformed() {
		this.setVisible(false);
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		scrollPane1 = new JScrollPane();
		txtQry = new JTextArea();
		button1 = new JButton();
		button2 = new JButton();
		scrollPane2 = new JScrollPane();
		txtRes = new JTextArea();

		//======== this ========
		setTitle("DB Utility");
		Container contentPane = getContentPane();
		contentPane.setLayout(new TableLayout(new double[][] {
			{450, 450},
			{175, 35, 175}}));
		((TableLayout)contentPane.getLayout()).setHGap(5);
		((TableLayout)contentPane.getLayout()).setVGap(5);

		//======== scrollPane1 ========
		{
			scrollPane1.setViewportView(txtQry);
		}
		contentPane.add(scrollPane1, new TableLayoutConstraints(0, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

		//---- button1 ----
		button1.setText("Execute");
		button1.setFont(new Font("Arial", Font.PLAIN, 14));
		button1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				button1ActionPerformed();
			}
		});
		contentPane.add(button1, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

		//---- button2 ----
		button2.setText("Cancel");
		button2.setFont(new Font("Arial", Font.PLAIN, 14));
		button2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				button2ActionPerformed();
			}
		});
		contentPane.add(button2, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

		//======== scrollPane2 ========
		{

			//---- txtRes ----
			txtRes.setBackground(Color.darkGray);
			txtRes.setForeground(Color.orange);
			txtRes.setFont(new Font("Monospaced", Font.BOLD, 13));
			scrollPane2.setViewportView(txtRes);
		}
		contentPane.add(scrollPane2, new TableLayoutConstraints(0, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JScrollPane scrollPane1;
	private JTextArea txtQry;
	private JButton button1;
	private JButton button2;
	private JScrollPane scrollPane2;
	private JTextArea txtRes;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
