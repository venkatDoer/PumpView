/*
 * Created by JFormDesigner on Tue Jul 26 16:03:21 IST 2022
 */

package doer.pv;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import com.jgoodies.forms.factories.*;
import info.clearthought.layout.*;

/**
 * @author VENKATESAN SELVARAJ
 */
public class PumpViewMDI extends JFrame {
	public PumpViewMDI() {
		initComponents();
	}

	private void lblLogoMouseReleased() {
		// TODO add your code here
	}

	private void lblResBulbMouseClicked() {
		// TODO add your code here
	}

	private void lblResFMouseClicked() {
		// TODO add your code here
	}

	private void lblResHMouseClicked() {
		// TODO add your code here
	}

	private void lblResPMouseClicked() {
		// TODO add your code here
	}

	private void lblResEMouseClicked() {
		// TODO add your code here
	}

	private void lblResCMouseClicked() {
		// TODO add your code here
	}

	private void pnlGraphMouseClicked() {
		// TODO add your code here
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		lblComp = new JLabel();
		pnlOuter = new JPanel();
		pnlMain = new JPanel();
		pnlSt1 = new JPanel();
		pnlModel1 = new JPanel();
		button1 = new JButton();
		pnlLive1 = new JPanel();
		pnlTest1 = new JPanel();
		scrlTest1 = new JScrollPane();
		tblTest1 = new JTable();
		pnlPreview1 = new JPanel();
		pnlResH = new JPanel();
		lblResBulb = new JLabel();
		lblResF = new JLabel();
		lblResH = new JLabel();
		lblResP = new JLabel();
		lblResE = new JLabel();
		lblResC = new JLabel();
		pnlGraph = new JPanel();
		pnlReading1 = new JPanel();
		scrlReadings1 = new JScrollPane();
		tblReadings1 = new JTable();
		cmdCapture1 = new JButton();
		cmdReset1 = new JButton();
		cmdSave1 = new JButton();
		cmdDel1 = new JButton();
		lblStatus1 = new JLabel();
		pnlButtons = new JPanel();
		pnlSettings = new JPanel();
		button2 = new JButton();
		button3 = new JButton();
		button4 = new JButton();
		button5 = new JButton();
		pnlOptions = new JPanel();
		button6 = new JButton();
		button7 = new JButton();
		button8 = new JButton();

		//======== this ========
		Container contentPane = getContentPane();
		contentPane.setLayout(new TableLayout(new double[][] {
			{TableLayout.FILL, TableLayout.PREFERRED},
			{TableLayout.PREFERRED, TableLayout.FILL}}));

		//---- lblComp ----
		lblComp.setText("ABC LTD, BANGALORE - 560066");
		lblComp.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 20));
		lblComp.setForeground(Color.white);
		lblComp.setHorizontalAlignment(SwingConstants.CENTER);
		lblComp.setFocusable(false);
		lblComp.setBackground(Color.darkGray);
		lblComp.setOpaque(true);
		contentPane.add(lblComp, new TableLayoutConstraints(0, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

		//======== pnlOuter ========
		{
			pnlOuter.setLayout(new TableLayout(new double[][] {
				{TableLayout.FILL},
				{TableLayout.FILL}}));
			((TableLayout)pnlOuter.getLayout()).setHGap(5);
			((TableLayout)pnlOuter.getLayout()).setVGap(5);

			//======== pnlMain ========
			{
				pnlMain.setLayout(new TableLayout(new double[][] {
					{TableLayout.FILL},
					{TableLayout.FILL}}));
				((TableLayout)pnlMain.getLayout()).setHGap(5);
				((TableLayout)pnlMain.getLayout()).setVGap(5);

				//======== pnlSt1 ========
				{
					pnlSt1.setBorder(new TitledBorder(null, "Station 1", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
						new Font("Arial", Font.BOLD, 13), Color.blue));
					pnlSt1.setLayout(new TableLayout(new double[][] {
						{TableLayout.FILL, TableLayout.FILL, TableLayout.FILL},
						{TableLayout.PREFERRED, TableLayout.FILL, 25}}));
					((TableLayout)pnlSt1.getLayout()).setHGap(3);
					((TableLayout)pnlSt1.getLayout()).setVGap(3);

					//======== pnlModel1 ========
					{
						pnlModel1.setBorder(new TitledBorder(null, "Model", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
							new Font("Arial", Font.BOLD, 13)));
						pnlModel1.setLayout(new TableLayout(new double[][] {
							{TableLayout.FILL, TableLayout.PREFERRED},
							{TableLayout.PREFERRED}}));
						((TableLayout)pnlModel1.getLayout()).setHGap(5);
						((TableLayout)pnlModel1.getLayout()).setVGap(5);

						//---- button1 ----
						button1.setText("Choose...");
						button1.setMargin(new Insets(2, 4, 2, 4));
						button1.setFont(new Font("Arial", Font.PLAIN, 12));
						pnlModel1.add(button1, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
					}
					pnlSt1.add(pnlModel1, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//======== pnlLive1 ========
					{
						pnlLive1.setBorder(new TitledBorder(null, "Live Reading", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
							new Font("Arial", Font.BOLD, 13)));
						pnlLive1.setLayout(new TableLayout(new double[][] {
							{TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL},
							{TableLayout.PREFERRED}}));
						((TableLayout)pnlLive1.getLayout()).setHGap(5);
						((TableLayout)pnlLive1.getLayout()).setVGap(5);
					}
					pnlSt1.add(pnlLive1, new TableLayoutConstraints(1, 0, 2, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//======== pnlTest1 ========
					{
						pnlTest1.setBorder(new TitledBorder(null, "Recent Tests", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
							new Font("Arial", Font.BOLD, 13)));
						pnlTest1.setLayout(new TableLayout(new double[][] {
							{TableLayout.FILL, TableLayout.FILL, TableLayout.FILL},
							{TableLayout.FILL, TableLayout.FILL, TableLayout.FILL}}));
						((TableLayout)pnlTest1.getLayout()).setHGap(5);
						((TableLayout)pnlTest1.getLayout()).setVGap(5);

						//======== scrlTest1 ========
						{
							scrlTest1.setBorder(null);

							//---- tblTest1 ----
							tblTest1.setModel(new DefaultTableModel(
								new Object[][] {
									{null, null, null, null, null},
									{null, null, null, null, null},
								},
								new String[] {
									"Type", "Pump SNo.", "Motor SNo.", "Result", "Remark"
								}
							) {
								Class<?>[] columnTypes = new Class<?>[] {
									String.class, String.class, String.class, String.class, String.class
								};
								boolean[] columnEditable = new boolean[] {
									false, true, true, true, true
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
								TableColumnModel cm = tblTest1.getColumnModel();
								cm.getColumn(0).setMinWidth(35);
								cm.getColumn(0).setMaxWidth(35);
								cm.getColumn(0).setPreferredWidth(35);
								cm.getColumn(3).setMinWidth(50);
								cm.getColumn(3).setMaxWidth(50);
								cm.getColumn(3).setPreferredWidth(50);
							}
							tblTest1.setBorder(null);
							tblTest1.setFont(new Font("Arial", Font.BOLD, 16));
							tblTest1.setRowHeight(20);
							tblTest1.setIntercellSpacing(new Dimension(3, 3));
							scrlTest1.setViewportView(tblTest1);
						}
						pnlTest1.add(scrlTest1, new TableLayoutConstraints(0, 0, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//======== pnlPreview1 ========
						{
							pnlPreview1.setBorder(new TitledBorder(null, "Result Preview [Pump SNo. XXX]", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
								new Font("Arial", Font.BOLD, 12)));
							pnlPreview1.setLayout(new TableLayout(new double[][] {
								{TableLayout.FILL, TableLayout.FILL},
								{TableLayout.FILL, TableLayout.FILL}}));
							((TableLayout)pnlPreview1.getLayout()).setHGap(5);
							((TableLayout)pnlPreview1.getLayout()).setVGap(5);

							//======== pnlResH ========
							{
								pnlResH.setLayout(new TableLayout(new double[][] {
									{TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL},
									{TableLayout.FILL, 25}}));
								((TableLayout)pnlResH.getLayout()).setHGap(1);
								((TableLayout)pnlResH.getLayout()).setVGap(1);

								//---- lblResBulb ----
								lblResBulb.setIcon(new ImageIcon(getClass().getResource("/img/idle.png")));
								lblResBulb.setHorizontalAlignment(SwingConstants.CENTER);
								lblResBulb.setOpaque(true);
								lblResBulb.setBackground(Color.gray);
								lblResBulb.setToolTipText("Click on this to see full report");
								lblResBulb.setText("IDLE");
								lblResBulb.setFont(new Font("Arial", Font.BOLD, 12));
								lblResBulb.setHorizontalTextPosition(SwingConstants.CENTER);
								lblResBulb.setVerticalTextPosition(SwingConstants.BOTTOM);
								lblResBulb.setIconTextGap(1);
								lblResBulb.addMouseListener(new MouseAdapter() {
									@Override
									public void mouseClicked(MouseEvent e) {
										lblResBulbMouseClicked();
									}
								});
								pnlResH.add(lblResBulb, new TableLayoutConstraints(0, 0, 4, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblResF ----
								lblResF.setText("F");
								lblResF.setHorizontalAlignment(SwingConstants.CENTER);
								lblResF.setFont(new Font("Arial", Font.BOLD, 14));
								lblResF.setOpaque(true);
								lblResF.setBackground(Color.gray);
								lblResF.setToolTipText("Flow");
								lblResF.addMouseListener(new MouseAdapter() {
									@Override
									public void mouseClicked(MouseEvent e) {
										lblResFMouseClicked();
									}
								});
								pnlResH.add(lblResF, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblResH ----
								lblResH.setText("H");
								lblResH.setHorizontalAlignment(SwingConstants.CENTER);
								lblResH.setFont(new Font("Arial", Font.BOLD, 14));
								lblResH.setOpaque(true);
								lblResH.setBackground(Color.gray);
								lblResH.setToolTipText("Head");
								lblResH.addMouseListener(new MouseAdapter() {
									@Override
									public void mouseClicked(MouseEvent e) {
										lblResHMouseClicked();
									}
								});
								pnlResH.add(lblResH, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblResP ----
								lblResP.setText("P");
								lblResP.setHorizontalAlignment(SwingConstants.CENTER);
								lblResP.setFont(new Font("Arial", Font.BOLD, 14));
								lblResP.setOpaque(true);
								lblResP.setBackground(Color.gray);
								lblResP.setToolTipText("Power");
								lblResP.addMouseListener(new MouseAdapter() {
									@Override
									public void mouseClicked(MouseEvent e) {
										lblResPMouseClicked();
									}
								});
								pnlResH.add(lblResP, new TableLayoutConstraints(2, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblResE ----
								lblResE.setText("E");
								lblResE.setHorizontalAlignment(SwingConstants.CENTER);
								lblResE.setFont(new Font("Arial", Font.BOLD, 14));
								lblResE.setOpaque(true);
								lblResE.setBackground(Color.gray);
								lblResE.setToolTipText("Efficiency");
								lblResE.addMouseListener(new MouseAdapter() {
									@Override
									public void mouseClicked(MouseEvent e) {
										lblResEMouseClicked();
									}
								});
								pnlResH.add(lblResE, new TableLayoutConstraints(3, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblResC ----
								lblResC.setText("C");
								lblResC.setHorizontalAlignment(SwingConstants.CENTER);
								lblResC.setFont(new Font("Arial", Font.BOLD, 14));
								lblResC.setOpaque(true);
								lblResC.setBackground(Color.gray);
								lblResC.setToolTipText("Current");
								lblResC.addMouseListener(new MouseAdapter() {
									@Override
									public void mouseClicked(MouseEvent e) {
										lblResCMouseClicked();
									}
								});
								pnlResH.add(lblResC, new TableLayoutConstraints(4, 1, 4, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
							}
							pnlPreview1.add(pnlResH, new TableLayoutConstraints(0, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

							//======== pnlGraph ========
							{
								pnlGraph.setBackground(Color.white);
								pnlGraph.setAutoscrolls(true);
								pnlGraph.setBorder(new LineBorder(Color.lightGray));
								pnlGraph.setEnabled(false);
								pnlGraph.setToolTipText("Click on this to see full report");
								pnlGraph.addMouseListener(new MouseAdapter() {
									@Override
									public void mouseClicked(MouseEvent e) {
										pnlGraphMouseClicked();
									}
								});
								pnlGraph.setLayout(new TableLayout(new double[][] {
									{TableLayout.FILL},
									{TableLayout.FILL}}));
								((TableLayout)pnlGraph.getLayout()).setHGap(1);
								((TableLayout)pnlGraph.getLayout()).setVGap(1);
							}
							pnlPreview1.add(pnlGraph, new TableLayoutConstraints(0, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
						}
						pnlTest1.add(pnlPreview1, new TableLayoutConstraints(2, 0, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//======== pnlReading1 ========
						{
							pnlReading1.setBorder(new TitledBorder(null, "Test Readings [Pump SNo. XX]", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
								new Font("Arial", Font.BOLD, 12)));
							pnlReading1.setLayout(new TableLayout(new double[][] {
								{TableLayout.FILL, TableLayout.PREFERRED},
								{TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL}}));
							((TableLayout)pnlReading1.getLayout()).setHGap(5);
							((TableLayout)pnlReading1.getLayout()).setVGap(5);

							//======== scrlReadings1 ========
							{
								scrlReadings1.setBorder(null);

								//---- tblReadings1 ----
								tblReadings1.setModel(new DefaultTableModel(
									new Object[][] {
										{null, null, null, null, null, null, null, null, null, null},
										{null, null, null, null, null, null, null, null, null, null},
										{null, null, null, null, null, null, null, null, null, null},
										{null, null, null, null, null, null, null, null, null, null},
										{null, null, null, null, null, null, null, null, null, null},
										{null, null, null, null, null, null, null, null, null, null},
									},
									new String[] {
										"Rt. V", "Test", "Suc. Head (m)", "Del. Head (m)", "Flow (lps)", "Voltage (V)", "Current (A)", "Power (kW)", "Freq. (Hz)", "Speed (rpm)"
									}
								) {
									Class<?>[] columnTypes = new Class<?>[] {
										String.class, Object.class, String.class, String.class, String.class, String.class, Object.class, Object.class, Object.class, Object.class
									};
									boolean[] columnEditable = new boolean[] {
										false, true, true, true, true, true, true, true, true, true
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
									TableColumnModel cm = tblReadings1.getColumnModel();
									cm.getColumn(0).setMinWidth(50);
									cm.getColumn(0).setMaxWidth(50);
									cm.getColumn(0).setPreferredWidth(50);
									cm.getColumn(1).setMinWidth(50);
									cm.getColumn(1).setMaxWidth(50);
									cm.getColumn(1).setPreferredWidth(50);
								}
								tblReadings1.setBorder(null);
								tblReadings1.setFont(new Font("Arial", Font.BOLD, 16));
								tblReadings1.setRowHeight(20);
								tblReadings1.setIntercellSpacing(new Dimension(3, 3));
								scrlReadings1.setViewportView(tblReadings1);
							}
							pnlReading1.add(scrlReadings1, new TableLayoutConstraints(0, 0, 0, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

							//---- cmdCapture1 ----
							cmdCapture1.setText("Capture");
							cmdCapture1.setMargin(new Insets(2, 4, 2, 4));
							cmdCapture1.setFont(new Font("Arial", Font.PLAIN, 12));
							pnlReading1.add(cmdCapture1, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

							//---- cmdReset1 ----
							cmdReset1.setText("Retest");
							cmdReset1.setMargin(new Insets(2, 4, 2, 4));
							cmdReset1.setFont(new Font("Arial", Font.PLAIN, 12));
							pnlReading1.add(cmdReset1, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

							//---- cmdSave1 ----
							cmdSave1.setText("Save");
							cmdSave1.setMargin(new Insets(2, 4, 2, 4));
							cmdSave1.setFont(new Font("Arial", Font.PLAIN, 12));
							pnlReading1.add(cmdSave1, new TableLayoutConstraints(1, 3, 1, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

							//---- cmdDel1 ----
							cmdDel1.setText("Delete");
							cmdDel1.setMargin(new Insets(2, 4, 2, 4));
							cmdDel1.setFont(new Font("Arial", Font.PLAIN, 12));
							pnlReading1.add(cmdDel1, new TableLayoutConstraints(1, 4, 1, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
						}
						pnlTest1.add(pnlReading1, new TableLayoutConstraints(0, 2, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
					}
					pnlSt1.add(pnlTest1, new TableLayoutConstraints(0, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblStatus1 ----
					lblStatus1.setText("STATUS");
					lblStatus1.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 16));
					lblStatus1.setHorizontalAlignment(SwingConstants.CENTER);
					lblStatus1.setOpaque(true);
					lblStatus1.setBackground(Color.gray);
					lblStatus1.setForeground(Color.yellow);
					pnlSt1.add(lblStatus1, new TableLayoutConstraints(0, 2, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				}
				pnlMain.add(pnlSt1, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			pnlOuter.add(pnlMain, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		}
		contentPane.add(pnlOuter, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

		//======== pnlButtons ========
		{
			pnlButtons.setLayout(new TableLayout(new double[][] {
				{75},
				{TableLayout.FILL, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL}}));
			((TableLayout)pnlButtons.getLayout()).setHGap(5);
			((TableLayout)pnlButtons.getLayout()).setVGap(5);

			//======== pnlSettings ========
			{
				pnlSettings.setBorder(new TitledBorder(null, "Settings", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
					new Font("Arial", Font.BOLD, 13), Color.blue));
				pnlSettings.setLayout(new TableLayout(new double[][] {
					{TableLayout.FILL},
					{TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED}}));
				((TableLayout)pnlSettings.getLayout()).setHGap(5);
				((TableLayout)pnlSettings.getLayout()).setVGap(5);

				//---- button2 ----
				button2.setText("Test");
				button2.setMargin(new Insets(2, 4, 2, 4));
				button2.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlSettings.add(button2, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- button3 ----
				button3.setText("Device");
				button3.setMargin(new Insets(2, 4, 2, 4));
				button3.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlSettings.add(button3, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- button4 ----
				button4.setText("Users");
				button4.setMargin(new Insets(2, 4, 2, 4));
				button4.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlSettings.add(button4, new TableLayoutConstraints(0, 2, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- button5 ----
				button5.setText("Others");
				button5.setMargin(new Insets(2, 4, 2, 4));
				button5.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlSettings.add(button5, new TableLayoutConstraints(0, 3, 0, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			pnlButtons.add(pnlSettings, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//======== pnlOptions ========
			{
				pnlOptions.setBorder(new TitledBorder(null, "Options", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
					new Font("Arial", Font.BOLD, 13), Color.blue));
				pnlOptions.setForeground(Color.blue);
				pnlOptions.setLayout(new TableLayout(new double[][] {
					{TableLayout.FILL},
					{TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED}}));
				((TableLayout)pnlOptions.getLayout()).setHGap(5);
				((TableLayout)pnlOptions.getLayout()).setVGap(5);

				//---- button6 ----
				button6.setText("Report");
				button6.setMargin(new Insets(2, 4, 2, 4));
				button6.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlOptions.add(button6, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- button7 ----
				button7.setText("Help");
				button7.setMargin(new Insets(2, 4, 2, 4));
				button7.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlOptions.add(button7, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- button8 ----
				button8.setText("Exit");
				button8.setMargin(new Insets(2, 4, 2, 4));
				button8.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlOptions.add(button8, new TableLayoutConstraints(0, 2, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			pnlButtons.add(pnlOptions, new TableLayoutConstraints(0, 2, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		}
		contentPane.add(pnlButtons, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		setSize(1000, 715);
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JLabel lblComp;
	private JPanel pnlOuter;
	private JPanel pnlMain;
	private JPanel pnlSt1;
	private JPanel pnlModel1;
	private JButton button1;
	private JPanel pnlLive1;
	private JPanel pnlTest1;
	private JScrollPane scrlTest1;
	private JTable tblTest1;
	private JPanel pnlPreview1;
	private JPanel pnlResH;
	private JLabel lblResBulb;
	private JLabel lblResF;
	private JLabel lblResH;
	private JLabel lblResP;
	private JLabel lblResE;
	private JLabel lblResC;
	private JPanel pnlGraph;
	private JPanel pnlReading1;
	private JScrollPane scrlReadings1;
	private JTable tblReadings1;
	private JButton cmdCapture1;
	private JButton cmdReset1;
	private JButton cmdSave1;
	private JButton cmdDel1;
	private JLabel lblStatus1;
	private JPanel pnlButtons;
	private JPanel pnlSettings;
	private JButton button2;
	private JButton button3;
	private JButton button4;
	private JButton button5;
	private JPanel pnlOptions;
	private JButton button6;
	private JButton button7;
	private JButton button8;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
