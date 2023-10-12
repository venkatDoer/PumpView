/*
 * Created by JFormDesigner on Mon Nov 23 10:21:20 IST 2015
 */

package doer.pv;

import java.awt.*;
import java.awt.event.*;
import java.sql.Savepoint;

import javax.swing.*;
import javax.swing.border.*;
import info.clearthought.layout.*;

/**
 * @author VENKATESAN SELVARAJ
 */
public class ReportSettings extends JDialog {
	public ReportSettings(Frame owner) {
		super(owner);
		initComponents();
	}

	public ReportSettings(Dialog owner) {
		super(owner);
		initComponents();
		customInit();
	}

	private void cmdSaveActionPerformed() {
		
		Configuration.REP_COLOR_OUTLINE = String.valueOf(lblOutline.getBackground().getRGB());
		Configuration.REP_COLOR_HEADER = String.valueOf(lblHeader.getBackground().getRGB());
		Configuration.REP_COLOR_LABEL = String.valueOf(lblLabel.getBackground().getRGB());
		Configuration.REP_COLOR_DECLARED = String.valueOf(lblDeclared.getBackground().getRGB());
		Configuration.REP_COLOR_ACTUAL = String.valueOf(lblActual.getBackground().getRGB());
		
		Configuration.saveCommonConfigValues("REP_COLOR_OUTLINE","REP_COLOR_HEADER","REP_COLOR_LABEL","REP_COLOR_DECLARED","REP_COLOR_ACTUAL");
		JOptionPane.showMessageDialog(this, "Changes saved successfully, please close and reopen the reports page");
	}

	private void cmdExitActionPerformed() {
		this.setVisible(false);
		// reset colors in reports
		((Reports)this.getParent()).resetRepColors();
	}

	// CUSTOM CODE BEGIN
	private void customInit() {
		// load color components with default colors
		lblOutline.setBackground(new Color(Integer.valueOf(Configuration.REP_COLOR_OUTLINE)));
		lblHeader.setBackground(new Color(Integer.valueOf(Configuration.REP_COLOR_HEADER)));
		lblLabel.setBackground(new Color(Integer.valueOf(Configuration.REP_COLOR_LABEL)));
		lblDeclared.setBackground(new Color(Integer.valueOf(Configuration.REP_COLOR_DECLARED)));
		lblActual.setBackground(new Color(Integer.valueOf(Configuration.REP_COLOR_ACTUAL)));
	}

	private void lblOutlineMouseClicked() {
		Color clrNew = JColorChooser.showDialog(this, "Choose Color", lblOutline.getBackground());
		if (clrNew != null) {
			lblOutline.setBackground(clrNew);
		}
	}

	private void lblHeaderMouseClicked() {
		Color clrNew = JColorChooser.showDialog(this, "Choose Color", lblHeader.getBackground());
		if (clrNew != null) {
			lblHeader.setBackground(clrNew);
		}
	}

	private void lblLabelMouseClicked() {
		Color clrNew = JColorChooser.showDialog(this, "Choose Color", lblLabel.getBackground());
		if (clrNew != null) {
			lblLabel.setBackground(clrNew);
		}
	}

	private void lblDeclaredMouseClicked() {
		Color clrNew = JColorChooser.showDialog(this, "Choose Color", lblDeclared.getBackground());
		if (clrNew != null) {
			lblDeclared.setBackground(clrNew);
		}
	}

	private void lblActualMouseClicked() {
		Color clrNew = JColorChooser.showDialog(this, "Choose Color", lblActual.getBackground());
		if (clrNew != null) {
			lblActual.setBackground(clrNew);
		}
	}

	private void cmdExitMouseClicked() {
		
	}

	private void cmdReset1ActionPerformed() {
		lblOutline.setBackground(Color.black);
	}

	private void cmdReset2ActionPerformed() {
		lblHeader.setBackground(Color.black);
	}

	private void cmdReset3ActionPerformed() {
		lblLabel.setBackground(Color.black);
	}

	private void cmdReset4ActionPerformed() {
		lblDeclared.setBackground(Color.black);
	}

	private void cmdReset5ActionPerformed() {
		lblActual.setBackground(Color.blue);
	}
	
	
	// VARIABLES
	
	// CUSTOM CODE END
	
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		pnlClr = new JPanel();
		label9 = new JLabel();
		lblOutline = new JLabel();
		cmdReset1 = new JButton();
		label5 = new JLabel();
		lblHeader = new JLabel();
		cmdReset2 = new JButton();
		label6 = new JLabel();
		lblLabel = new JLabel();
		cmdReset3 = new JButton();
		label7 = new JLabel();
		lblDeclared = new JLabel();
		cmdReset4 = new JButton();
		label8 = new JLabel();
		lblActual = new JLabel();
		cmdReset5 = new JButton();
		cmdSave = new JButton();
		cmdExit = new JButton();

		//======== this ========
		setTitle("Doer PumpView: Report Settings");
		setFont(new Font("Arial", Font.PLAIN, 12));
		setModal(true);
		setResizable(false);
		Container contentPane = getContentPane();
		contentPane.setLayout(new TableLayout(new double[][] {
			{10, TableLayout.FILL, 10},
			{10, TableLayout.PREFERRED, 5}}));
		((TableLayout)contentPane.getLayout()).setHGap(5);
		((TableLayout)contentPane.getLayout()).setVGap(5);

		//======== pnlClr ========
		{
			pnlClr.setBorder(new TitledBorder(null, "Color Settings [All Assembly Lines]", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION,
				new Font("Arial", Font.BOLD, 14), Color.blue));
			pnlClr.setLayout(new TableLayout(new double[][] {
				{TableLayout.FILL, TableLayout.FILL, TableLayout.FILL},
				{TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, 5, TableLayout.PREFERRED}}));
			((TableLayout)pnlClr.getLayout()).setHGap(5);
			((TableLayout)pnlClr.getLayout()).setVGap(5);

			//---- label9 ----
			label9.setText("Outline");
			label9.setFont(new Font("Arial", Font.BOLD, 12));
			pnlClr.add(label9, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblOutline ----
			lblOutline.setOpaque(true);
			lblOutline.setBackground(Color.black);
			lblOutline.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			lblOutline.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					lblOutlineMouseClicked();
				}
			});
			pnlClr.add(lblOutline, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdReset1 ----
			cmdReset1.setText("Reset");
			cmdReset1.setFont(new Font("Arial", Font.PLAIN, 12));
			cmdReset1.setToolTipText("Reset to default outline color");
			cmdReset1.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					cmdReset1ActionPerformed();
				}
			});
			pnlClr.add(cmdReset1, new TableLayoutConstraints(2, 0, 2, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- label5 ----
			label5.setText("Report Header");
			label5.setFont(new Font("Arial", Font.BOLD, 12));
			pnlClr.add(label5, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblHeader ----
			lblHeader.setOpaque(true);
			lblHeader.setBackground(Color.black);
			lblHeader.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			lblHeader.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					lblHeaderMouseClicked();
				}
			});
			pnlClr.add(lblHeader, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdReset2 ----
			cmdReset2.setText("Reset");
			cmdReset2.setFont(new Font("Arial", Font.PLAIN, 12));
			cmdReset2.setToolTipText("Reset to default header color");
			cmdReset2.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					cmdReset2ActionPerformed();
				}
			});
			pnlClr.add(cmdReset2, new TableLayoutConstraints(2, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- label6 ----
			label6.setText("Labels");
			label6.setFont(new Font("Arial", Font.BOLD, 12));
			pnlClr.add(label6, new TableLayoutConstraints(0, 2, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblLabel ----
			lblLabel.setOpaque(true);
			lblLabel.setBackground(Color.black);
			lblLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			lblLabel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					lblLabelMouseClicked();
				}
			});
			pnlClr.add(lblLabel, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdReset3 ----
			cmdReset3.setText("Reset");
			cmdReset3.setFont(new Font("Arial", Font.PLAIN, 12));
			cmdReset3.setToolTipText("Reset to default label color");
			cmdReset3.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					cmdReset3ActionPerformed();
				}
			});
			pnlClr.add(cmdReset3, new TableLayoutConstraints(2, 2, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- label7 ----
			label7.setText("Declared Values");
			label7.setFont(new Font("Arial", Font.BOLD, 12));
			pnlClr.add(label7, new TableLayoutConstraints(0, 3, 0, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblDeclared ----
			lblDeclared.setOpaque(true);
			lblDeclared.setBackground(Color.black);
			lblDeclared.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			lblDeclared.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					lblDeclaredMouseClicked();
				}
			});
			pnlClr.add(lblDeclared, new TableLayoutConstraints(1, 3, 1, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdReset4 ----
			cmdReset4.setText("Reset");
			cmdReset4.setFont(new Font("Arial", Font.PLAIN, 12));
			cmdReset4.setToolTipText("Reset to default declared values color");
			cmdReset4.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					cmdReset4ActionPerformed();
				}
			});
			pnlClr.add(cmdReset4, new TableLayoutConstraints(2, 3, 2, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- label8 ----
			label8.setText("Actual Values");
			label8.setFont(new Font("Arial", Font.BOLD, 12));
			pnlClr.add(label8, new TableLayoutConstraints(0, 4, 0, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblActual ----
			lblActual.setOpaque(true);
			lblActual.setBackground(Color.black);
			lblActual.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			lblActual.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					lblActualMouseClicked();
				}
			});
			pnlClr.add(lblActual, new TableLayoutConstraints(1, 4, 1, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdReset5 ----
			cmdReset5.setText("Reset");
			cmdReset5.setFont(new Font("Arial", Font.PLAIN, 12));
			cmdReset5.setToolTipText("Reset to default actual values color");
			cmdReset5.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					cmdReset5ActionPerformed();
				}
			});
			pnlClr.add(cmdReset5, new TableLayoutConstraints(2, 4, 2, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdSave ----
			cmdSave.setText("Save");
			cmdSave.setFont(new Font("Arial", Font.PLAIN, 14));
			cmdSave.setIcon(new ImageIcon(getClass().getResource("/img/save.PNG")));
			cmdSave.setToolTipText("Click on this to save the changes made above in all tabs");
			cmdSave.setMnemonic('S');
			cmdSave.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					cmdSaveActionPerformed();
				}
			});
			pnlClr.add(cmdSave, new TableLayoutConstraints(0, 6, 1, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

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
			pnlClr.add(cmdExit, new TableLayoutConstraints(2, 6, 2, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		}
		contentPane.add(pnlClr, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		pack();
		setLocationRelativeTo(null);
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel pnlClr;
	private JLabel label9;
	private JLabel lblOutline;
	private JButton cmdReset1;
	private JLabel label5;
	private JLabel lblHeader;
	private JButton cmdReset2;
	private JLabel label6;
	private JLabel lblLabel;
	private JButton cmdReset3;
	private JLabel label7;
	private JLabel lblDeclared;
	private JButton cmdReset4;
	private JLabel label8;
	private JLabel lblActual;
	private JButton cmdReset5;
	private JButton cmdSave;
	private JButton cmdExit;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
