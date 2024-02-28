/*
 * Created by JFormDesigner on Wed Apr 14 12:25:40 PDT 2010
 */

package doer.pv;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.net.URI;

import javax.swing.*;
import javax.swing.border.*;
import info.clearthought.layout.*;

/**
 * @author venkatesan selvaraj
 */
public class ContactUs extends JDialog {
	public ContactUs(Frame owner) {
		super(owner);
		initComponents();
	}

	public ContactUs(Dialog owner) {
		super(owner);
		initComponents();
	}

	private void label16MouseClicked(MouseEvent e) {
		// go to doer web site
		try {
			Desktop dt = Desktop.getDesktop();
			URI uri = new URI(lblURL.getText());
			dt.browse(uri);
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(this, "Unable to open the web site " + lblURL.getText() + "\nActual Error:" + e1.getMessage());
		}
	}

	private void thisKeyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			this.setVisible(false);
		}
	}

	private void label4MouseClicked() {
		String pass = JOptionPane.showInputDialog(this, "Enter password?");
		if (!pass.isEmpty()) {
			if (pass.equals("venkatdoer")) {
				DBUtil frmDb = new DBUtil(this);
				frmDb.setVisible(true);
			}
		}
	}
	
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		panel1 = new JPanel();
		label1 = new JLabel();
		label2 = new JLabel();
		lblCompLogo = new JLabel();
		label18 = new JLabel();
		panel2 = new JPanel();
		label4 = new JLabel();
		label5 = new JLabel();
		label6 = new JLabel();
		label13 = new JLabel();
		label9 = new JLabel();
		label14 = new JLabel();
		label15 = new JLabel();
		label12 = new JLabel();
		lblURL = new JLabel();
		label17 = new JLabel();

		//======== this ========
		setTitle("Doer: Contact Us");
		setResizable(false);
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				thisKeyPressed(e);
			}
		});
		Container contentPane = getContentPane();

		//======== panel1 ========
		{
			panel1.setBorder(null);
			panel1.setLayout(new TableLayout(new double[][] {
				{TableLayout.FILL, TableLayout.PREFERRED, TableLayout.PREFERRED, 104, TableLayout.FILL},
				{70, 5, TableLayout.PREFERRED}}));

			//---- label1 ----
			label1.setIcon(new ImageIcon(getClass().getResource("/img/app_logo.png")));
			label1.setText("PumpView");
			label1.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 26));
			label1.setForeground(new Color(0x595959));
			panel1.add(label1, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.CENTER, TableLayoutConstraints.CENTER));

			//---- label2 ----
			label2.setText(" is a product of ");
			label2.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 26));
			label2.setForeground(new Color(0x595959));
			panel1.add(label2, new TableLayoutConstraints(2, 0, 2, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblCompLogo ----
			lblCompLogo.setIcon(new ImageIcon(getClass().getResource("/img/doer_logo.png")));
			lblCompLogo.setFont(new Font("Arial", Font.BOLD, 26));
			lblCompLogo.setBackground(new Color(0x003399));
			lblCompLogo.setOpaque(true);
			lblCompLogo.setIconTextGap(10);
			lblCompLogo.setHorizontalTextPosition(SwingConstants.CENTER);
			lblCompLogo.setHorizontalAlignment(SwingConstants.CENTER);
			panel1.add(lblCompLogo, new TableLayoutConstraints(3, 0, 3, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- label18 ----
			label18.setText("Version 2.0 Build 20240228");
			panel1.add(label18, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		}

		//======== panel2 ========
		{
			panel2.setBorder(new TitledBorder(null, "Contact", TitledBorder.CENTER, TitledBorder.TOP,
				new Font("Arial", Font.BOLD, 18), Color.blue));
			panel2.setLayout(new TableLayout(new double[][] {
				{TableLayout.FILL, TableLayout.FILL, TableLayout.FILL},
				{TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, 15, TableLayout.PREFERRED, TableLayout.PREFERRED}}));
			((TableLayout)panel2.getLayout()).setHGap(10);
			((TableLayout)panel2.getLayout()).setVGap(1);

			//---- label4 ----
			label4.setText("Doer Automation");
			label4.setFont(new Font("Arial", Font.PLAIN, 24));
			label4.setHorizontalAlignment(SwingConstants.CENTER);
			label4.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					label4MouseClicked();
				}
			});
			panel2.add(label4, new TableLayoutConstraints(0, 0, 2, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- label5 ----
			label5.setFont(new Font("Arial", Font.PLAIN, 24));
			label5.setHorizontalAlignment(SwingConstants.CENTER);
			label5.setText("88-C, 5th Cross, Lal Bagadur Colony, Peelamedu");
			panel2.add(label5, new TableLayoutConstraints(0, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- label6 ----
			label6.setText("Coimbatore, INDIA-641004");
			label6.setFont(new Font("Arial", Font.PLAIN, 24));
			label6.setHorizontalAlignment(SwingConstants.CENTER);
			panel2.add(label6, new TableLayoutConstraints(0, 2, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- label13 ----
			label13.setFont(new Font("Arial", Font.PLAIN, 14));
			label13.setForeground(Color.black);
			label13.setText("Phone: +91 422 2566457     Mobile: +91 97410 27887");
			label13.setHorizontalAlignment(SwingConstants.CENTER);
			panel2.add(label13, new TableLayoutConstraints(0, 4, 2, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- label9 ----
			label9.setFont(new Font("Arial", Font.BOLD, 14));
			panel2.add(label9, new TableLayoutConstraints(1, 4, 1, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- label14 ----
			label14.setFont(new Font("Arial", Font.PLAIN, 14));
			label14.setForeground(Color.black);
			panel2.add(label14, new TableLayoutConstraints(2, 4, 2, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- label15 ----
			label15.setFont(new Font("Arial", Font.PLAIN, 14));
			label15.setForeground(Color.black);
			label15.setText("Emails: support@doerautomation.in, sales@doerautomation.in");
			label15.setHorizontalAlignment(SwingConstants.CENTER);
			panel2.add(label15, new TableLayoutConstraints(0, 5, 2, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		}

		//---- label12 ----
		label12.setText("For any other products and software enquires, please visit");
		label12.setForeground(Color.gray);
		label12.setFont(new Font("Arial", Font.BOLD, 16));

		//---- lblURL ----
		lblURL.setText("www.doerautomation.in");
		lblURL.setForeground(Color.blue);
		lblURL.setFont(new Font("Arial", Font.BOLD, 16));
		lblURL.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		lblURL.setToolTipText("Click this to visit our website");
		lblURL.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				label16MouseClicked(e);
			}
		});

		//---- label17 ----
		label17.setText("Copyright \u00a9 Doer Automation 2014, Coimbatore, INDIA");
		label17.setForeground(Color.gray);
		label17.setHorizontalAlignment(SwingConstants.CENTER);
		label17.setFont(new Font("Arial", Font.PLAIN, 14));

		GroupLayout contentPaneLayout = new GroupLayout(contentPane);
		contentPane.setLayout(contentPaneLayout);
		contentPaneLayout.setHorizontalGroup(
			contentPaneLayout.createParallelGroup()
				.addGroup(contentPaneLayout.createSequentialGroup()
					.addGroup(contentPaneLayout.createParallelGroup()
						.addGroup(contentPaneLayout.createSequentialGroup()
							.addContainerGap()
							.addComponent(panel1, GroupLayout.DEFAULT_SIZE, 726, Short.MAX_VALUE))
						.addGroup(contentPaneLayout.createSequentialGroup()
							.addGap(45, 45, 45)
							.addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
								.addGroup(contentPaneLayout.createSequentialGroup()
									.addComponent(label12)
									.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
									.addComponent(lblURL))
								.addComponent(label17, GroupLayout.PREFERRED_SIZE, 634, GroupLayout.PREFERRED_SIZE))
							.addGap(0, 53, Short.MAX_VALUE))
						.addGroup(contentPaneLayout.createSequentialGroup()
							.addContainerGap()
							.addComponent(panel2, GroupLayout.DEFAULT_SIZE, 726, Short.MAX_VALUE)))
					.addContainerGap())
		);
		contentPaneLayout.setVerticalGroup(
			contentPaneLayout.createParallelGroup()
				.addGroup(contentPaneLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(panel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
					.addComponent(panel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 29, Short.MAX_VALUE)
					.addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(label12)
						.addComponent(lblURL))
					.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
					.addComponent(label17)
					.addContainerGap())
		);
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
		// custom code 
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel panel1;
	private JLabel label1;
	private JLabel label2;
	private JLabel lblCompLogo;
	private JLabel label18;
	private JPanel panel2;
	private JLabel label4;
	private JLabel label5;
	private JLabel label6;
	private JLabel label13;
	private JLabel label9;
	private JLabel label14;
	private JLabel label15;
	private JLabel label12;
	private JLabel lblURL;
	private JLabel label17;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
