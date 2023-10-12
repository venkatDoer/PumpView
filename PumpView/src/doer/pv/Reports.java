/*
 * Created by JFormDesigner on Fri Jan 13 13:27:06 EST 2012
 */

package doer.pv;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.PrinterException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYDrawableAnnotation;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;

import com.toedter.calendar.JDateChooser;

import doer.io.Database;
import doer.io.MyResultSet;
import doer.io.Parameter;
import doer.print.PrintUtility;
import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;


/**
 * @author VENKATESAN SELVARAJ
 */
public class Reports extends JDialog {
	
	public Reports(Frame owner, String curPump, String curPumpId, String recentPump, Calendar recentPumpDt, Boolean graphView) {
		super(owner);
		curPumpType = curPump;
		curPumpTypeId = curPumpId;
		recPumpNo = recentPump;
		recPumpDt = recentPumpDt;
		initInProgress = true;
		initComponents();
		//chkDisR.setSelected(graphView);
		customInit();
	}


	private void cmdCloseActionPerformed() {
		thisWindowClosing();
		this.setVisible(false);
	}

	private void cmdPrintActionPerformed() {
		// print current page
		try {
			
			PrintUtility pu = new PrintUtility();
			if (!pu.initialize()) {
				return;
			}
			
			PrintProgressDlg prgDlg = new PrintProgressDlg();
			
			prgDlg.showMessage("Printing current page");
			if (prgDlg.getCancelPrint()) {
				JOptionPane.showMessageDialog(this, "Print aborted!");
				return;
			}
			
			// else continue with printing
			
			switch(tabReport.getSelectedIndex()) {
			case 0:
				pu.printComponent(pnlPrint);
				break;
			case 1:
				pu.printComponent(pnlPrint2);
				break;
			case 2:
				pu.printComponent(pnlPrint3);
				break;
			case 3:
				switch(tabISI.getSelectedIndex()) {
				case 0:
					pu.printComponent(pnlPrint41);
					break;
				case 1:
					pu.printComponent(pnlPrint42);
					break;
				case 2:
					pu.printComponent(pnlPrint43);
					break;
				}
				break;
			case 4:
				pu.printComponent(pnlPrint5);
				break;
			case 5:
				//setCustomInputColor(Color.WHITE);
				pu.printComponent(pnlPrint6);
				//setCustomInputColor(new Color(204, 255, 255));
				break;
			}
			
			JOptionPane.showMessageDialog(this, "Print completed");
				
		} catch (PrinterException e) {
			JOptionPane.showMessageDialog(this, "Printer error occured while printing this document.\nActual Error:" + e.getMessage());
		}
	}

	private void cmdRefreshActionPerformed() {
		if (tblTest.getRowCount() == 0) {
			JOptionPane.showMessageDialog(this, "No records found to preview.\nChange your filter criteria and try again.");
			clearTable();
			return;
		}
		// construct test list
		String tmpPumpList = "";
		pumpList.clear();
		curPage = 1;
		int j = 0;
		for (int i=0; i<tblTest.getRowCount(); i++) {
			if (tblTest.getValueAt(i, 0).toString().equals("true")) {
				if (curMVPump) {
					tmpPumpList += "'" + tblTest.getValueAt(i, 1).toString().substring(0, tblTest.getValueAt(i, 1).toString().indexOf("[")-1) + "',";
				} else {
					tmpPumpList += "'" + tblTest.getValueAt(i, 1) + "',";
				}
				pumpList.put(j++,  tblTest.getValueAt(i, 1).toString());
			}
		}
		
		if (pumpList.isEmpty()) {
			JOptionPane.showMessageDialog(this, "No pumps selected to preview.\nSelect any pumps and try again.");
			clearTable();
			return;
		} 
		tmpPumpList = tmpPumpList.substring(0,tmpPumpList.length()-1);
		pumpFilterText = filterText + " and pump_slno in(" + tmpPumpList + ")";
		
		// reset below so that the report loads with new filter when the corresponding tab clicked next time
		/*for(int i=0; i<reportLoadedFirstTime.length; i++) {
			reportLoadedFirstTime[i] = false;
		}*/
		
		// refresh header
		setReportHeader();
		
		// refresh current tab
		tabReport.requestFocusInWindow();
		tabReportStateChanged();
	}

	private void cmdNextActionPerformed() {
		switch(tabReport.getSelectedIndex()) {
		case 0: // register report
			gotoPageRegRep(++curPageRegRep);
			break;
		case 1: // test result
			loadTestResReport(++curPage);
			break;
		case 2: // test graph
			loadTestGraph(++curPage);
			break;
		case 3: // isi report
			switch (tabISI.getSelectedIndex()) {
			case 0:
				loadISIReport(++curPage);
				break;
			case 1:
				gotoPageISIType(++curPageISIType);
				break;
			case 2:
				gotoPageISIMaxMin(++curPageISIMaxMin);
				break;
			}
			break;
		case 5: // custom report
			loadCustomReport(++curPage);
			break;
		}
	}

	private void cmdPrevActionPerformed() {
		switch(tabReport.getSelectedIndex()) {
		case 0: // register report
			gotoPageRegRep(--curPageRegRep);
			break;
		case 1: // test result
			loadTestResReport(--curPage);
			break;
		case 2: // test graph
			loadTestGraph(--curPage);
			break;
		case 3: // isi report
			switch (tabISI.getSelectedIndex()) {
			case 0:
				loadISIReport(--curPage);
				break;
			case 1:
				gotoPageISIType(--curPageISIType);
				break;
			case 2:
				gotoPageISIMaxMin(--curPageISIMaxMin);
				break;
			}
			break;
		case 5: // custom report
			loadCustomReport(--curPage);
			break;
		}
	}

	private void cmdFirstActionPerformed() {
		curPage = 1;
		switch(tabReport.getSelectedIndex()) {
		case 0: // register report
			curPageRegRep = 1;
			gotoPageRegRep(curPageRegRep);
			break;
		case 1: // test result
			loadTestResReport(curPage);
			break;
		case 2: // test graph
			loadTestGraph(curPage);
			break;
		case 3: // test graph
			switch (tabISI.getSelectedIndex()) {
			case 0:
				loadISIReport(curPage);
				break;
			case 1:
				curPageISIType = 1;
				gotoPageISIType(curPageISIType);
				break;
			case 2:
				curPageISIMaxMin = 1;
				gotoPageISIMaxMin(curPageISIMaxMin);
				break;
			}
			break;
		case 5: // custom report
			loadCustomReport(curPage);
			break;
		}
	}

	private void cmdLastActionPerformed() {
		curPage = totAvailPages;
		switch(tabReport.getSelectedIndex()) {
		case 0: // register report
			curPageRegRep = totAvailPagesRegRep;
			gotoPageRegRep(curPageRegRep);
			break;
		case 1: // test result
			loadTestResReport(curPage);
			break;
		case 2: // test graph
			loadTestGraph(curPage);
			break;
		case 3: // isi report
			switch (tabISI.getSelectedIndex()) {
			case 0:
				loadISIReport(curPage);
				break;
			case 1:
				curPageISIType = totAvailPagesISIType;
				gotoPageISIType(curPageISIType);
				break;
			case 2:
				curPageISIMaxMin = totAvailPagesISIMaxMin;
				gotoPageISIMaxMin(curPageISIMaxMin);
				break;
			}
			break;
		case 5: // custom report
			//setCustomInputColor(Color.WHITE);
			loadCustomReport(curPage);
			break;
		}
	}
	
	private void cmdPrintAllActionPerformed() {
		// print all pages by going through one by one and reset the user to the page where they were
		try {
			
			PrintUtility pu = new PrintUtility();
			if (!pu.initialize()) {
				return;
			}
			
			PrintProgressDlg prgDlg = new PrintProgressDlg();
			
			switch(tabReport.getSelectedIndex()) {
			case 0:
				for(int i=1; i<=totAvailPagesRegRep; i++) {
					gotoPageRegRep(i);
					
					prgDlg.showMessage("Printing page " + i + " of " + totAvailPagesRegRep);
					if (prgDlg.getCancelPrint()) {
						break;
					} else {
						pu.printComponent(pnlPrint);
					}
				}
				gotoPageRegRep(curPageRegRep); 
				break;
			case 1:
				for(int i=1; i<=totAvailPages; i++) {
					loadTestResReport(i);
					
					prgDlg.showMessage("Printing page " + i + " of " + totAvailPages);
					if (prgDlg.getCancelPrint()) {
						break;
					} else {
						pu.printComponent(pnlPrint2);
					}
				}
				loadTestResReport(curPage); 
				break;
			case 2:
				for(int i=1; i<=totAvailPages; i++) {
					loadTestGraph(i);
					
					prgDlg.showMessage("Printing page " + i + " of " + totAvailPages);
					if (prgDlg.getCancelPrint()) {
						break;
					} else {
						pu.printComponent(pnlPrint3);
					}
				}
				loadTestGraph(curPage); 
				break;
			case 3:
				switch (tabISI.getSelectedIndex()) {
				case 0:
					for(int i=1; i<=totAvailPages; i++) {
						loadISIReport(i);

						prgDlg.showMessage("Printing page " + i + " of " + totAvailPages);
						if (prgDlg.getCancelPrint()) {
							break;
						} else {
							pu.printComponent(pnlPrint41);
						}
					}
					loadISIReport(curPage); 
					break;
				case 1:
					for(int i=1; i<=totAvailPagesISIType; i++) {
						gotoPageISIType(i);
						
						prgDlg.showMessage("Printing page " + i + " of " + totAvailPagesISIType);
						if (prgDlg.getCancelPrint()) {
							break;
						} else {
							pu.printComponent(pnlPrint42);
						}
					}
					gotoPageISIType(curPageISIType); 
					break;
				case 2:
					for(int i=1; i<=totAvailPagesISIMaxMin; i++) {
						gotoPageISIMaxMin(i);
						
						prgDlg.showMessage("Printing page " + i + " of " + totAvailPagesISIMaxMin);
						if (prgDlg.getCancelPrint()) {
							break;
						} else {
							pu.printComponent(pnlPrint43);
						}
					}
					gotoPageISIMaxMin(curPageISIMaxMin); 
					break;
				}
				break;
			case 4:
				prgDlg.showMessage("Printing page 1 of 1");
				if (prgDlg.getCancelPrint()) {
					break;
				} else {
					pu.printComponent(pnlPrint5);
				}
				break;
				
			case 5:
				for(int i=1; i<=totAvailPages; i++) {
					loadCustomReport(i);
					
					prgDlg.showMessage("Printing page " + i + " of " + totAvailPages);
					if (prgDlg.getCancelPrint()) {
						break;
					} else {
						//setCustomInputColor(Color.WHITE);
						pu.printComponent(pnlPrint6);
					}
				}
				loadCustomReport(curPage); 
				break;
			}
			
			if (prgDlg.getCancelPrint()) {
				JOptionPane.showMessageDialog(this, "Print aborted!");
			} else {
				JOptionPane.showMessageDialog(this, "Print completed");
			}
			
		} catch (PrinterException e) {
			JOptionPane.showMessageDialog(this, "Printer error occured while printing this document.\nActual Error:" + e.getMessage());
		}
	}

	private void cmbTypeActionPerformed() {
		if (initInProgress) {
			return;
		}
		
		if (pumpTypeNonISIList.indexOf(cmbType.getSelectedItem().toString()) >= 0) {
			lblNonISIFilter.setVisible(true);
		} else {
			lblNonISIFilter.setVisible(false);
		}

		// multi voltage handling
		TableLayout tblLayout = (TableLayout) pnlFilter.getLayout();
		if (pumpTypeMVList.indexOf(cmbType.getSelectedItem().toString()) >= 0) {
			curMVPump = true;
			chkRtV.setVisible(true);
		} else {
			curMVPump = false;
			chkRtV.setVisible(false);
		}
		
		curPumpType = cmbType.getSelectedItem().toString();
		curPumpTypeId = pumpTypeIdList.get(curPumpType);
		curPumpCat = pumpCatList.get(curPumpType);
		curRtV = pumpRtVList.get(curPumpType);
		initInProgress = true; // to avoid result to refresh
		defDisUnit = pumpDisUnit.get(curPumpType);
		curDisUnit = defDisUnit;
		if (defDisUnit.contains("sup")) {
			cmbDisUnit.setSelectedItem("<html>" + defDisUnit + "</html>");
		} else {
			cmbDisUnit.setSelectedItem(defDisUnit);
		}
		lblDisUnit.setText("<html>Pump's Default is " + defDisUnit + "</html>");
		
		defHdUnit = pumpHdUnit.get(curPumpType);
		curHdUnit = defHdUnit;
		cmbHdUnit.setSelectedItem(defHdUnit);
		lblHdUnit.setText("<html>Pump's Default is " + defHdUnit + "</html>");
		
		// load performance tolerance
		Boolean isToUseISI = true;
		if (lblNonISIFilter.isVisible()) {
			try {
				MyResultSet res = db.executeQuery("select * from " + Configuration.NON_ISI_PERF_TOLERANCE + " where pump_type_id='" + curPumpTypeId + "'");
				if (res.next()) {
					isToUseISI = false;
					tolHeadMin = res.getFloat("head_ll");
					tolHeadMax = res.getFloat("head_ul") > 0 ? res.getFloat("head_ul") : 99999F;
					tolDisMin = res.getFloat("discharge_ll");
					tolDisMax = res.getFloat("discharge_ul") > 0 ? res.getFloat("discharge_ul") : 99999F;
					tolCurMin = res.getFloat("current_ll");
					tolCurMax = res.getFloat("current_ul") > 0 ? res.getFloat("current_ul") : 99999F;
					tolPowMin = res.getFloat("power_ll");
					tolPowMax = res.getFloat("power_ul") > 0 ? res.getFloat("power_ul") : 99999F;
					tolEffMin = res.getFloat("eff_ll");
					tolEffMax = res.getFloat("eff_ul") > 0 ? res.getFloat("eff_ul") : 99999F;
				}
			} catch (Exception sqle) {
				JOptionPane.showMessageDialog(new JDialog(), "Error loading tolerance:" + sqle.getMessage());
				return;
			}
		}
		if (isToUseISI) {
			tolHeadMin = Configuration.ISI_PERF_HEAD_LL;
			tolHeadMax = Configuration.ISI_PERF_HEAD_UL;
			tolDisMin = Configuration.ISI_PERF_DIS_LL;
			tolDisMax = Configuration.ISI_PERF_DIS_UL;
			tolCurMin = Configuration.ISI_PERF_CUR_LL;
			tolCurMax = Configuration.ISI_PERF_CUR_UL;
			tolPowMin = Configuration.ISI_PERF_POW_LL;
			tolPowMax = Configuration.ISI_PERF_POW_UL;
			tolEffMin = Configuration.ISI_PERF_EFF_LL;
			tolEffMax = Configuration.ISI_PERF_EFF_UL;
		}
		tolHeadMin /= 100;
		tolHeadMax /= 100;
		tolDisMin /= 100;
		tolDisMax /= 100;
		tolCurMin /= 100;
		tolCurMax /= 100;
		tolPowMin /= 100;
		tolPowMax /= 100;
		tolEffMin /= 100;
		tolEffMax /= 100;
		
		initInProgress = false;
		refreshTestList();
	}

	private void cmbLineActionPerformed() {
		refreshTestList();
	}

	private void txtSlNoFromFocusLost() {
		refreshTestList();
	}

	private void txtSlNoToFocusLost() {
		refreshTestList();
	}

	private void fromDtPropertyChange() {
		refreshTestList();
	}

	private void toDtPropertyChange() {
		refreshTestList();
	}
	
	private void clearTable() {
		// clear the table hardly
		pumpList.clear(); // pump no. which is not exist, returns 0 records
		filterText = " where 1 = 2"; // always return 0 records
		pumpFilterText = " where 1 = 2"; // always return 0 records
		refreshReport();
	}
	
	private void associateFunctionKeys() {
		// associate f5 for open
		String REFRESH_ACTION_KEY = "refreshAction";
		Action refreshAction = new AbstractAction() {
		      public void actionPerformed(ActionEvent actionEvent) {
		        cmdRefreshActionPerformed();
		      }
		    };
		KeyStroke f5 = KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0);
		InputMap refreshInputMap = cmdRefresh.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		refreshInputMap.put(f5, REFRESH_ACTION_KEY);
		ActionMap refreshActionMap = cmdRefresh.getActionMap();
		refreshActionMap.put(REFRESH_ACTION_KEY, refreshAction);
		cmdRefresh.setActionMap(refreshActionMap);
		
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

	private void thisWindowClosing() {
	}

	private void optTypeActionPerformed() {
		refreshTestList();
	}

	private void optRtActionPerformed() {
		refreshTestList();
	}

	private void optBothActionPerformed() {
		refreshTestList();
	}

	private void cmbRemarksActionPerformed() {
		refreshTestList();
	}

	private void cmdSelActionPerformed() {
		for(int i=0; i<tblTest.getRowCount(); i++) {
			tblTest.setValueAt(true, i, 0);
		}
	}

	private void cmdClearActionPerformed() {
		for(int i=0; i<tblTest.getRowCount(); i++) {
			tblTest.setValueAt(false, i, 0);
		}
	}

	private void tabReportStateChanged() {
		if (initInProgress) {
			return;
		}
		refreshCount();
		enableDisableButtons();
		refreshReport();
	}

	private void chkQvHActionPerformed() {
		loadCompareGraph();
	}

	private void chkQvMIActionPerformed() {
		loadCompareGraph();
	}

	private void chkQvCActionPerformed() {
		loadCompareGraph();
	}

	private void tabISIStateChanged() {
		if (initInProgress) {
			return;
		}
		refreshReport();
	}

	private void cmbDisUnitActionPerformed() {
		if (!initInProgress) {
			curDisUnit = cmbDisUnit.getSelectedItem().toString();
			if (cmbDisUnit.getSelectedItem().toString().contains("sup")) {
				curDisUnit = cmbDisUnit.getSelectedItem().toString().substring(6,cmbDisUnit.getSelectedItem().toString().length()-7);
			}
			// refresh current tab
			tabReportStateChanged();
		}
	}

	private void chkDisRActionPerformed() {
		// refresh current tab
		tabReportStateChanged();
	}
	
	private void addScrollPane() {
		{

			//======== printArea ========
			{
				printArea.setBorder(new LineBorder(Color.blue, 2));
				printArea.setBackground(Color.white);
				printArea.setLayout(new TableLayout(new double[][] {
					{900},
					{1450}}));

				//======== pnlPrint ========
				{
					pnlPrint.setBorder(null);
					pnlPrint.setBackground(Color.white);
					pnlPrint.setLayout(new TableLayout(new double[][] {
						{40, 120, TableLayout.FILL, 120, 15},
						{35, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, TableLayout.PREFERRED, 70, TableLayout.FILL, TableLayout.PREFERRED}}));

					//---- lblIS ----
					lblIS.setText("IS:");
					lblIS.setHorizontalAlignment(SwingConstants.CENTER);
					lblIS.setFont(new Font("Arial", Font.BOLD, 10));
					lblIS.setName("label");
					pnlPrint.add(lblIS, new TableLayoutConstraints(3, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblCustLogo ----
					lblCustLogo.setHorizontalAlignment(SwingConstants.CENTER);
					pnlPrint.add(lblCustLogo, new TableLayoutConstraints(1, 1, 1, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblCompName ----
					lblCompName.setText("Company Name");
					lblCompName.setFont(new Font("Arial", Font.BOLD, 16));
					lblCompName.setHorizontalAlignment(SwingConstants.CENTER);
					lblCompName.setName("header");
					pnlPrint.add(lblCompName, new TableLayoutConstraints(2, 1, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblISILogo ----
					lblISILogo.setIcon(new ImageIcon(getClass().getResource("/img/isi_logo.PNG")));
					lblISILogo.setHorizontalAlignment(SwingConstants.CENTER);
					lblISILogo.setVerticalAlignment(SwingConstants.TOP);
					lblISILogo.setFont(new Font("Arial", Font.PLAIN, 12));
					lblISILogo.setIconTextGap(0);
					pnlPrint.add(lblISILogo, new TableLayoutConstraints(3, 2, 3, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblCompAdr ----
					lblCompAdr.setText("Address Line 1 and Line 2");
					lblCompAdr.setFont(new Font("Arial", Font.BOLD, 12));
					lblCompAdr.setHorizontalAlignment(SwingConstants.CENTER);
					lblCompAdr.setName("header");
					pnlPrint.add(lblCompAdr, new TableLayoutConstraints(2, 3, 2, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblTitle ----
					lblTitle.setText("PUMP TEST REGISTER");
					lblTitle.setFont(new Font("Arial", Font.BOLD, 14));
					lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
					lblTitle.setName("header");
					pnlPrint.add(lblTitle, new TableLayoutConstraints(2, 5, 2, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblISIRefNo ----
					lblISIRefNo.setHorizontalAlignment(SwingConstants.CENTER);
					lblISIRefNo.setFont(new Font("Arial", Font.BOLD, 10));
					lblISIRefNo.setText("ISI Ref No.");
					lblISIRefNo.setName("label");
					pnlPrint.add(lblISIRefNo, new TableLayoutConstraints(3, 4, 3, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label6 ----
					label6.setText("PUMP TYPE:");
					label6.setFont(new Font("Arial", Font.BOLD, 12));
					label6.setName("label");
					pnlPrint.add(label6, new TableLayoutConstraints(1, 7, 1, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblPumpType ----
					lblPumpType.setText("type");
					lblPumpType.setFont(new Font("Arial", Font.PLAIN, 12));
					lblPumpType.setName("actual");
					pnlPrint.add(lblPumpType, new TableLayoutConstraints(2, 7, 2, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblPage ----
					lblPage.setText("Page 1 of 1");
					lblPage.setFont(new Font("Arial", Font.PLAIN, 12));
					lblPage.setHorizontalAlignment(SwingConstants.RIGHT);
					lblPage.setName("label");
					pnlPrint.add(lblPage, new TableLayoutConstraints(3, 7, 3, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//======== pnlPrime ========
					{
						pnlPrime.setBackground(Color.black);
						pnlPrime.setAutoscrolls(true);
						pnlPrime.setBorder(LineBorder.createBlackLineBorder());
						pnlPrime.setName("outline");
						pnlPrime.setLayout(new TableLayout(new double[][] {
							{65, 74, 74, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, 60},
							{TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL}}));
						((TableLayout)pnlPrime.getLayout()).setHGap(1);
						((TableLayout)pnlPrime.getLayout()).setVGap(1);

						//---- label122 ----
						label122.setText("Date");
						label122.setBackground(Color.white);
						label122.setHorizontalAlignment(SwingConstants.CENTER);
						label122.setFont(new Font("Arial", Font.BOLD, 11));
						label122.setOpaque(true);
						label122.setBorder(null);
						label122.setName("label");
						pnlPrime.add(label122, new TableLayoutConstraints(0, 0, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- label123 ----
						label123.setText("Pump SNo");
						label123.setBackground(Color.white);
						label123.setHorizontalAlignment(SwingConstants.CENTER);
						label123.setFont(new Font("Arial", Font.BOLD, 11));
						label123.setOpaque(true);
						label123.setBorder(null);
						label123.setName("label");
						pnlPrime.add(label123, new TableLayoutConstraints(1, 0, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- label228 ----
						label228.setText("Motor SNo");
						label228.setBackground(Color.white);
						label228.setHorizontalAlignment(SwingConstants.CENTER);
						label228.setFont(new Font("Arial", Font.BOLD, 11));
						label228.setOpaque(true);
						label228.setBorder(null);
						label228.setName("label");
						pnlPrime.add(label228, new TableLayoutConstraints(2, 0, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- label2 ----
						label2.setText("Rated V");
						label2.setBackground(Color.white);
						label2.setFont(new Font("Arial", Font.BOLD, 11));
						label2.setOpaque(true);
						label2.setHorizontalAlignment(SwingConstants.CENTER);
						pnlPrime.add(label2, new TableLayoutConstraints(3, 0, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- label20 ----
						label20.setText("Test No");
						label20.setFont(new Font("Arial", Font.BOLD, 11));
						label20.setOpaque(true);
						label20.setBackground(Color.white);
						label20.setHorizontalAlignment(SwingConstants.CENTER);
						label20.setBorder(null);
						label20.setName("label");
						pnlPrime.add(label20, new TableLayoutConstraints(4, 0, 4, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- label8 ----
						label8.setText("Type");
						label8.setOpaque(true);
						label8.setFont(new Font("Arial", Font.BOLD, 11));
						label8.setBackground(Color.white);
						label8.setHorizontalAlignment(SwingConstants.CENTER);
						label8.setBorder(null);
						label8.setName("label");
						pnlPrime.add(label8, new TableLayoutConstraints(5, 0, 5, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- label9 ----
						label9.setText("Name");
						label9.setBackground(Color.white);
						label9.setOpaque(true);
						label9.setFont(new Font("Arial", Font.BOLD, 11));
						label9.setHorizontalAlignment(SwingConstants.CENTER);
						label9.setBorder(null);
						label9.setName("label");
						pnlPrime.add(label9, new TableLayoutConstraints(6, 0, 6, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- label126 ----
						label126.setText("Speed");
						label126.setBackground(Color.white);
						label126.setHorizontalAlignment(SwingConstants.CENTER);
						label126.setFont(new Font("Arial", Font.BOLD, 11));
						label126.setOpaque(true);
						label126.setAutoscrolls(true);
						label126.setBorder(null);
						label126.setName("label");
						pnlPrime.add(label126, new TableLayoutConstraints(7, 0, 7, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- label1 ----
						label1.setText("Remark");
						label1.setBackground(Color.white);
						label1.setFont(new Font("Arial", Font.BOLD, 11));
						label1.setOpaque(true);
						label1.setHorizontalAlignment(SwingConstants.CENTER);
						label1.setBorder(null);
						label1.setName("label");
						pnlPrime.add(label1, new TableLayoutConstraints(15, 0, 15, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- label155 ----
						label155.setText("S. Head");
						label155.setBackground(Color.white);
						label155.setHorizontalAlignment(SwingConstants.CENTER);
						label155.setFont(new Font("Arial", Font.BOLD, 11));
						label155.setOpaque(true);
						label155.setAutoscrolls(true);
						label155.setBorder(null);
						label155.setName("label");
						pnlPrime.add(label155, new TableLayoutConstraints(8, 0, 8, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- label129 ----
						label129.setText("D. Head");
						label129.setBackground(Color.white);
						label129.setHorizontalAlignment(SwingConstants.CENTER);
						label129.setFont(new Font("Arial", Font.BOLD, 11));
						label129.setOpaque(true);
						label129.setAutoscrolls(true);
						label129.setBorder(null);
						label129.setName("label");
						pnlPrime.add(label129, new TableLayoutConstraints(9, 0, 9, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- label130 ----
						label130.setText("Flow");
						label130.setBackground(Color.white);
						label130.setHorizontalAlignment(SwingConstants.CENTER);
						label130.setFont(new Font("Arial", Font.BOLD, 11));
						label130.setOpaque(true);
						label130.setAutoscrolls(true);
						label130.setBorder(null);
						label130.setName("label");
						pnlPrime.add(label130, new TableLayoutConstraints(10, 0, 10, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- label131 ----
						label131.setText("Voltage");
						label131.setBackground(Color.white);
						label131.setHorizontalAlignment(SwingConstants.CENTER);
						label131.setFont(new Font("Arial", Font.BOLD, 11));
						label131.setOpaque(true);
						label131.setAutoscrolls(true);
						label131.setBorder(null);
						label131.setName("label");
						pnlPrime.add(label131, new TableLayoutConstraints(11, 0, 11, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- label132 ----
						label132.setText("Current");
						label132.setBackground(Color.white);
						label132.setHorizontalAlignment(SwingConstants.CENTER);
						label132.setFont(new Font("Arial", Font.BOLD, 11));
						label132.setOpaque(true);
						label132.setAutoscrolls(true);
						label132.setBorder(null);
						label132.setName("label");
						pnlPrime.add(label132, new TableLayoutConstraints(12, 0, 12, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- label128 ----
						label128.setText("Power");
						label128.setBackground(Color.white);
						label128.setHorizontalAlignment(SwingConstants.CENTER);
						label128.setFont(new Font("Arial", Font.BOLD, 11));
						label128.setOpaque(true);
						label128.setAutoscrolls(true);
						label128.setBorder(null);
						label128.setName("label");
						pnlPrime.add(label128, new TableLayoutConstraints(13, 0, 13, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- label154 ----
						label154.setText("Freq.");
						label154.setBackground(Color.white);
						label154.setHorizontalAlignment(SwingConstants.CENTER);
						label154.setFont(new Font("Arial", Font.BOLD, 11));
						label154.setOpaque(true);
						label154.setAutoscrolls(true);
						label154.setBorder(null);
						label154.setName("label");
						pnlPrime.add(label154, new TableLayoutConstraints(14, 0, 14, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- label159 ----
						label159.setText("rpm");
						label159.setBackground(Color.white);
						label159.setHorizontalAlignment(SwingConstants.CENTER);
						label159.setFont(new Font("Arial", Font.BOLD, 11));
						label159.setOpaque(true);
						label159.setAutoscrolls(true);
						label159.setBorder(null);
						label159.setName("label");
						pnlPrime.add(label159, new TableLayoutConstraints(7, 1, 7, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- lblSHd1 ----
						lblSHd1.setText("mwc");
						lblSHd1.setBackground(Color.white);
						lblSHd1.setHorizontalAlignment(SwingConstants.CENTER);
						lblSHd1.setFont(new Font("Arial", Font.BOLD, 11));
						lblSHd1.setOpaque(true);
						lblSHd1.setAutoscrolls(true);
						lblSHd1.setBorder(null);
						lblSHd1.setName("label");
						pnlPrime.add(lblSHd1, new TableLayoutConstraints(8, 1, 8, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- lblDHd1 ----
						lblDHd1.setText("mwc");
						lblDHd1.setBackground(Color.white);
						lblDHd1.setHorizontalAlignment(SwingConstants.CENTER);
						lblDHd1.setFont(new Font("Arial", Font.BOLD, 11));
						lblDHd1.setOpaque(true);
						lblDHd1.setAutoscrolls(true);
						lblDHd1.setBorder(null);
						lblDHd1.setName("label");
						pnlPrime.add(lblDHd1, new TableLayoutConstraints(9, 1, 9, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- lblDis1 ----
						lblDis1.setText("unit");
						lblDis1.setBackground(Color.white);
						lblDis1.setHorizontalAlignment(SwingConstants.CENTER);
						lblDis1.setFont(new Font("Arial", Font.BOLD, 11));
						lblDis1.setOpaque(true);
						lblDis1.setAutoscrolls(true);
						lblDis1.setBorder(null);
						lblDis1.setName("label");
						pnlPrime.add(lblDis1, new TableLayoutConstraints(10, 1, 10, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- label139 ----
						label139.setText("V");
						label139.setBackground(Color.white);
						label139.setHorizontalAlignment(SwingConstants.CENTER);
						label139.setFont(new Font("Arial", Font.BOLD, 11));
						label139.setOpaque(true);
						label139.setAutoscrolls(true);
						label139.setBorder(null);
						label139.setName("label");
						pnlPrime.add(label139, new TableLayoutConstraints(11, 1, 11, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- label140 ----
						label140.setText("A");
						label140.setBackground(Color.white);
						label140.setHorizontalAlignment(SwingConstants.CENTER);
						label140.setFont(new Font("Arial", Font.BOLD, 11));
						label140.setOpaque(true);
						label140.setAutoscrolls(true);
						label140.setBorder(null);
						label140.setName("label");
						pnlPrime.add(label140, new TableLayoutConstraints(12, 1, 12, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- label134 ----
						label134.setText("kW");
						label134.setBackground(Color.white);
						label134.setHorizontalAlignment(SwingConstants.CENTER);
						label134.setFont(new Font("Arial", Font.BOLD, 11));
						label134.setOpaque(true);
						label134.setAutoscrolls(true);
						label134.setBorder(null);
						label134.setName("label");
						pnlPrime.add(label134, new TableLayoutConstraints(13, 1, 13, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- label161 ----
						label161.setText("Hz");
						label161.setBackground(Color.white);
						label161.setHorizontalAlignment(SwingConstants.CENTER);
						label161.setFont(new Font("Arial", Font.BOLD, 11));
						label161.setOpaque(true);
						label161.setAutoscrolls(true);
						label161.setBorder(null);
						label161.setName("label");
						pnlPrime.add(label161, new TableLayoutConstraints(14, 1, 14, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- tblRes ----
						tblRes.setModel(new DefaultTableModel(
							new Object[][] {
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
								{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							},
							new String[] {
								null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
							}
						) {
							Class<?>[] columnTypes = new Class<?>[] {
								String.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class
							};
							@Override
							public Class<?> getColumnClass(int columnIndex) {
								return columnTypes[columnIndex];
							}
						});
						{
							TableColumnModel cm = tblRes.getColumnModel();
							cm.getColumn(0).setMinWidth(65);
							cm.getColumn(0).setMaxWidth(65);
							cm.getColumn(0).setPreferredWidth(65);
							cm.getColumn(1).setMinWidth(75);
							cm.getColumn(1).setPreferredWidth(75);
							cm.getColumn(2).setMinWidth(75);
							cm.getColumn(2).setPreferredWidth(75);
							cm.getColumn(15).setMinWidth(60);
							cm.getColumn(15).setMaxWidth(60);
							cm.getColumn(15).setPreferredWidth(60);
						}
						tblRes.setFont(new Font("Arial", Font.PLAIN, 11));
						tblRes.setRowHeight(17);
						tblRes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
						tblRes.setGridColor(Color.lightGray);
						tblRes.setBorder(new LineBorder(Color.white));
						tblRes.setRowSelectionAllowed(false);
						tblRes.setAutoscrolls(false);
						tblRes.setFocusable(false);
						tblRes.setEnabled(false);
						tblRes.setIntercellSpacing(new Dimension(5, 1));
						tblRes.setName("outline");
						pnlPrime.add(tblRes, new TableLayoutConstraints(0, 2, 15, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
					}
					pnlPrint.add(pnlPrime, new TableLayoutConstraints(1, 8, 3, 8, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//======== pnlBot ========
					{
						pnlBot.setBackground(Color.white);
						pnlBot.setName("outline");
						pnlBot.setBorder(LineBorder.createBlackLineBorder());
						pnlBot.setLayout(new TableLayout(new double[][] {
							{TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL},
							{TableLayout.FILL}}));

						//---- lblTestedBy ----
						lblTestedBy.setText("Tested By");
						lblTestedBy.setFont(new Font("Arial", Font.PLAIN, 13));
						lblTestedBy.setHorizontalAlignment(SwingConstants.CENTER);
						lblTestedBy.setName("label");
						lblTestedBy.setVerticalAlignment(SwingConstants.BOTTOM);
						pnlBot.add(lblTestedBy, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- label102 ----
						label102.setText("Approved By");
						label102.setFont(new Font("Arial", Font.PLAIN, 13));
						label102.setHorizontalAlignment(SwingConstants.CENTER);
						label102.setName("label");
						label102.setVerticalAlignment(SwingConstants.BOTTOM);
						pnlBot.add(label102, new TableLayoutConstraints(3, 0, 3, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- lblVerBy14 ----
						lblVerBy14.setText("Verified By");
						lblVerBy14.setFont(new Font("Arial", Font.PLAIN, 13));
						lblVerBy14.setHorizontalAlignment(SwingConstants.CENTER);
						lblVerBy14.setName("label");
						lblVerBy14.setVerticalAlignment(SwingConstants.BOTTOM);
						pnlBot.add(lblVerBy14, new TableLayoutConstraints(2, 0, 2, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
					}
					pnlPrint.add(pnlBot, new TableLayoutConstraints(1, 9, 3, 9, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblAppVer ----
					lblAppVer.setText("Doer PumpViewPro");
					lblAppVer.setFont(new Font("Arial", Font.PLAIN, 10));
					lblAppVer.setHorizontalAlignment(SwingConstants.RIGHT);
					lblAppVer.setForeground(Color.gray);
					pnlPrint.add(lblAppVer, new TableLayoutConstraints(2, 11, 3, 11, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				}
				printArea.add(pnlPrint, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			scrollPane1.setViewportView(printArea);
		}
	}
	
	private void createObjects() {
		pnlOpt = new JPanel();
		pnlFilter = new JPanel();
		scrlDev = new JScrollPane();
		tblTest = new JTable();
		cmdRefresh = new JButton();
		cmdSel = new JButton();
		cmdClear = new JButton();
		lblResult = new JLabel();
		menuBar1 = new JMenuBar();
		menFilter = new JMenu();
		optRt = new JRadioButtonMenuItem();
		optType = new JRadioButtonMenuItem();
		optBoth = new JRadioButtonMenuItem();
		chkRtV = new JCheckBoxMenuItem();
		pnlAdFilter = new JPanel();
		label11 = new JLabel();
		lblNonISIFilter = new JLabel();
		cmbType = new JComboBox();
		label12 = new JLabel();
		label13 = new JLabel();
		fromDt = new JDateChooser();
		label14 = new JLabel();
		toDt = new JDateChooser();
		label15 = new JLabel();
		label16 = new JLabel();
		txtSlNoFrom = new JTextField();
		label17 = new JLabel();
		txtSlNoTo = new JTextField();
		label5 = new JLabel();
		cmbRemarks = new JComboBox();
		label18 = new JLabel();
		cmbLine = new JComboBox();
		pnlViewOpt = new JPanel();
		label76 = new JLabel();
		optRndrLine = new JRadioButton();
		optRndrSm = new JRadioButton();
		label71 = new JLabel();
		cmbHdUnit = new JComboBox();
		lblHdUnit = new JLabel();
		label66 = new JLabel();
		cmbDisUnit = new JComboBox();
		lblDisUnit = new JLabel();
		label70 = new JLabel();
		optSpeed = new JRadioButton();
		optFreq = new JRadioButton();
		chkDisR = new JCheckBox();
		chkCat = new JCheckBox();
		cmdSet = new JButton();
		cmdExp = new JButton();
		cmdPrint = new JButton();
		cmdPrintAll = new JButton();
		cmdClose = new JButton();
		printContainer = new JPanel();
		pnlBut = new JPanel();
		cmdFirst = new JButton();
		cmdPrev = new JButton();
		cmdNext = new JButton();
		cmdLast = new JButton();
		tabReport = new JTabbedPane();
		scrollPane1 = new JScrollPane();
		printArea = new JPanel();
		pnlPrint = new JPanel();
		lblIS = new JLabel();
		lblCustLogo = new JLabel();
		lblCompName = new JLabel();
		lblISILogo = new JLabel();
		lblCompAdr = new JLabel();
		lblTitle = new JLabel();
		lblISIRefNo = new JLabel();
		label6 = new JLabel();
		lblPumpType = new JLabel();
		lblPage = new JLabel();
		pnlPrime = new JPanel();
		label122 = new JLabel();
		label123 = new JLabel();
		label228 = new JLabel();
		label2 = new JLabel();
		label20 = new JLabel();
		label8 = new JLabel();
		label9 = new JLabel();
		label126 = new JLabel();
		label1 = new JLabel();
		label155 = new JLabel();
		label129 = new JLabel();
		label130 = new JLabel();
		label131 = new JLabel();
		label132 = new JLabel();
		label128 = new JLabel();
		label154 = new JLabel();
		label159 = new JLabel();
		lblSHd1 = new JLabel();
		lblDHd1 = new JLabel();
		lblDis1 = new JLabel();
		label139 = new JLabel();
		label140 = new JLabel();
		label134 = new JLabel();
		label161 = new JLabel();
		tblRes = new JTable();
		pnlBot = new JPanel();
		lblTestedBy = new JLabel();
		label102 = new JLabel();
		lblVerBy14 = new JLabel();
		lblAppVer = new JLabel();
		scrollPane2 = new JScrollPane();
		printArea2 = new JPanel();
		pnlPrint2 = new JPanel();
		lblIS2 = new JLabel();
		lblCustLogo2 = new JLabel();
		lblCompName2 = new JLabel();
		lblISILogo2 = new JLabel();
		lblCompAdr2 = new JLabel();
		lblTitle2 = new JLabel();
		lblISIRefNo2 = new JLabel();
		lblPage2 = new JLabel();
		pnlRep2 = new JPanel();
		pnlTop2 = new JPanel();
		label10 = new JLabel();
		lblPumpType2 = new JLabel();
		label32 = new JLabel();
		lblMRt2 = new JLabel();
		label26 = new JLabel();
		lblFreq2 = new JLabel();
		lbllblPSize2 = new JLabel();
		lblPSz2 = new JLabel();
		label42 = new JLabel();
		lblPSno2 = new JLabel();
		label36 = new JLabel();
		lblV2 = new JLabel();
		label194 = new JLabel();
		lblSpeed2 = new JLabel();
		lblHdLbl2 = new JLabel();
		lblHead2 = new JLabel();
		label34 = new JLabel();
		lblPh2 = new JLabel();
		label38 = new JLabel();
		lblCur2 = new JLabel();
		label203 = new JLabel();
		lblPoles2 = new JLabel();
		lblFlowRtLabel = new JLabel();
		lblFlow2 = new JLabel();
		label51 = new JLabel();
		lblCon2 = new JLabel();
		lbllblMI2 = new JLabel();
		lblMI2 = new JLabel();
		lbllblGDis2 = new JLabel();
		lblGDis2 = new JLabel();
		lblHdRgLbl2 = new JLabel();
		lblHeadR2 = new JLabel();
		lbllblMotType2 = new JLabel();
		lblMotType2 = new JLabel();
		lbllblMSno2 = new JLabel();
		lblMSno2 = new JLabel();
		lbllblMotEff2 = new JLabel();
		lblMotEff2 = new JLabel();
		lbllblOE2 = new JLabel();
		lblOEff2 = new JLabel();
		lbllblIns2 = new JLabel();
		lblIns2 = new JLabel();
		lbllblPipeConst2 = new JLabel();
		lblPipeConst2 = new JLabel();
		lbllblSPTime2 = new JLabel();
		lblSPTime2 = new JLabel();
		lbllblBrSz2 = new JLabel();
		lblBrSz2 = new JLabel();
		lbllblCapRat2 = new JLabel();
		lblCapRate2 = new JLabel();
		lbllblCapVolt2 = new JLabel();
		lblCapVolt2 = new JLabel();
		lbllblNoOfStg2 = new JLabel();
		lblNoStg2 = new JLabel();
		pnlPrime2 = new JPanel();
		label143 = new JLabel();
		label144 = new JLabel();
		label127 = new JLabel();
		lbllblHead1 = new JLabel();
		label133 = new JLabel();
		label135 = new JLabel();
		lblTitSHd2 = new JLabel();
		lblRem2 = new JLabel();
		lblPerfHz2 = new JLabel();
		label335 = new JLabel();
		label336 = new JLabel();
		lblTitVHC2 = new JLabel();
		lblTitTotHd2 = new JLabel();
		label156 = new JLabel();
		label136 = new JLabel();
		label141 = new JLabel();
		label142 = new JLabel();
		lblTitFlow2 = new JLabel();
		lblTitTotRtHd2 = new JLabel();
		lblTitMI2 = new JLabel();
		label173 = new JLabel();
		label153 = new JLabel();
		lblSHd2 = new JLabel();
		lblDHd2 = new JLabel();
		lblVHd2 = new JLabel();
		lblTotHd2 = new JLabel();
		lblFlowUnitS2 = new JLabel();
		label145 = new JLabel();
		label146 = new JLabel();
		label147 = new JLabel();
		lblFlowUnitT2 = new JLabel();
		lblTotHdT2 = new JLabel();
		lblMITitUnit2 = new JLabel();
		tblRes2 = new JTable();
		pnlBot2 = new JPanel();
		lblCas2 = new JLabel();
		lblGC2 = new JLabel();
		lblMaxDis2 = new JLabel();
		lblGCText2 = new JLabel();
		lblWith2 = new JLabel();
		lblDt2 = new JLabel();
		lblTestedBy2 = new JLabel();
		lblVerBy13 = new JLabel();
		label23 = new JLabel();
		lbllblDt2 = new JLabel();
		pnlOptTest2 = new JPanel();
		lbllblSP2 = new JLabel();
		lblSP2 = new JLabel();
		lblAppVer2 = new JLabel();
		scrollPane3 = new JScrollPane();
		printArea3 = new JPanel();
		lblRtWarn3 = new JLabel();
		pnlPrint3 = new JPanel();
		lblIS3 = new JLabel();
		lblCustLogo3 = new JLabel();
		lblCompName3 = new JLabel();
		lblISILogo3 = new JLabel();
		lblCompAdr3 = new JLabel();
		lblTitle3 = new JLabel();
		lblISIRefNo3 = new JLabel();
		lblPage3 = new JLabel();
		pnlGraph3 = new JPanel();
		pnlBot3 = new JPanel();
		label33 = new JLabel();
		lblPumpType3 = new JLabel();
		label61 = new JLabel();
		lblPSno3 = new JLabel();
		label62 = new JLabel();
		lblDt3 = new JLabel();
		lblHdR3 = new JLabel();
		lblHeadR3 = new JLabel();
		lbllblPSize3 = new JLabel();
		lblPSz3 = new JLabel();
		lbllblFreq3 = new JLabel();
		lblFreq3 = new JLabel();
		pnlBotRes3 = new JPanel();
		label49 = new JLabel();
		label57 = new JLabel();
		label58 = new JLabel();
		label59 = new JLabel();
		lblFlowUnit3 = new JLabel();
		lblFlow3 = new JLabel();
		lblFlowAct3 = new JLabel();
		lblFlowRes3 = new JLabel();
		lblHd3 = new JLabel();
		lblHead3 = new JLabel();
		lblHeadAct3 = new JLabel();
		lblHeadRes3 = new JLabel();
		lbllblMI3 = new JLabel();
		lbllblEff3 = new JLabel();
		lblMotIp3 = new JLabel();
		lblMotIpAct3 = new JLabel();
		lblMotIpRes3 = new JLabel();
		lbllblMaxC3 = new JLabel();
		lblEff3 = new JLabel();
		lblCur3 = new JLabel();
		lblEffAct3 = new JLabel();
		lblCurAct3 = new JLabel();
		lblEffRes3 = new JLabel();
		lblCurRes3 = new JLabel();
		pnlBotRes3R = new JPanel();
		lblFlowUnit3RL2 = new JLabel();
		lblFlowUnit3RL3 = new JLabel();
		label227 = new JLabel();
		lblFlowUnit3RL = new JLabel();
		lblHd3RL = new JLabel();
		lblFlowUnit3RH = new JLabel();
		lblHdRH = new JLabel();
		label232 = new JLabel();
		label233 = new JLabel();
		label234 = new JLabel();
		label237 = new JLabel();
		lbllblEff3R1 = new JLabel();
		lbllblMI3R1 = new JLabel();
		lbllblEff3R2 = new JLabel();
		lbllblMI3R2 = new JLabel();
		lblFlow3RL = new JLabel();
		lblHead3RL = new JLabel();
		lblEff3RL = new JLabel();
		lblMotIp3RL = new JLabel();
		lblFlow3RH = new JLabel();
		lblHead3RH = new JLabel();
		lblEff3RH = new JLabel();
		lblMotIp3RH = new JLabel();
		lblCur3R = new JLabel();
		lblFlowAct3RL = new JLabel();
		lblHeadAct3RL = new JLabel();
		lblEffAct3RL = new JLabel();
		lblMotIpAct3RL = new JLabel();
		lblFlowAct3RH = new JLabel();
		lblHeadAct3RH = new JLabel();
		lblEffAct3RH = new JLabel();
		lblMotIpAct3RH = new JLabel();
		lblCurAct3R = new JLabel();
		lblFlowRes3RL = new JLabel();
		lblHeadRes3RL = new JLabel();
		lblEffRes3RL = new JLabel();
		lblMotIpRes3RL = new JLabel();
		lblFlowRes3RH = new JLabel();
		lblHeadRes3RH = new JLabel();
		lblEffRes3RH = new JLabel();
		lblMotIpRes3RH = new JLabel();
		lblCurRes3R = new JLabel();
		pnlBotBot3 = new JPanel();
		lblTestedBy3 = new JLabel();
		label103 = new JLabel();
		lblVerBy12 = new JLabel();
		lblAppVer3 = new JLabel();
		pnlRep4 = new JPanel();
		tabISI = new JTabbedPane();
		scrollPane41 = new JScrollPane();
		printArea41 = new JPanel();
		lblRtWarn41 = new JLabel();
		pnlPrint41 = new JPanel();
		lblCustLogo41 = new JLabel();
		lblCompName41 = new JLabel();
		lblIS41 = new JLabel();
		lblISILogo41 = new JLabel();
		lblCompAdr41 = new JLabel();
		lblTitle41 = new JLabel();
		lblISIRefNo41 = new JLabel();
		lblPage41 = new JLabel();
		pnlTop41 = new JPanel();
		label25 = new JLabel();
		lblPumpType41 = new JLabel();
		label37 = new JLabel();
		lblMRt41 = new JLabel();
		label52 = new JLabel();
		lblFreq41 = new JLabel();
		lbllblPSize41 = new JLabel();
		lblPSz41 = new JLabel();
		label43 = new JLabel();
		lblPSno41 = new JLabel();
		label50 = new JLabel();
		lblV41 = new JLabel();
		label224 = new JLabel();
		lblSpeed41 = new JLabel();
		lblHdLbl41 = new JLabel();
		lblHead41 = new JLabel();
		label39 = new JLabel();
		lblPh41 = new JLabel();
		label53 = new JLabel();
		lblCur41 = new JLabel();
		label204 = new JLabel();
		lblPoles41 = new JLabel();
		lblFlowRtLabel41 = new JLabel();
		lblFlow41 = new JLabel();
		label160 = new JLabel();
		lblCon41 = new JLabel();
		lbllblTopMI41 = new JLabel();
		lblMI41 = new JLabel();
		lbllblGDis41 = new JLabel();
		lblGDis41 = new JLabel();
		lblHdRglbl41 = new JLabel();
		lblHeadR41 = new JLabel();
		lbllblMotType41 = new JLabel();
		lblMotType41 = new JLabel();
		lbllblMSno41 = new JLabel();
		lblMSno41 = new JLabel();
		lbllblMotEff41 = new JLabel();
		lblMotEff41 = new JLabel();
		lbllblOE41 = new JLabel();
		lblOEff41 = new JLabel();
		lbllblIns41 = new JLabel();
		lblIns41 = new JLabel();
		lbllblPipeConst41 = new JLabel();
		lblPipeConst41 = new JLabel();
		lbllblSPTime41 = new JLabel();
		lblSPTime41 = new JLabel();
		lbllblBrSz41 = new JLabel();
		lblBrSz41 = new JLabel();
		lbllblCapRat41 = new JLabel();
		lblCapRate41 = new JLabel();
		lbllblCapVolt41 = new JLabel();
		lblCapVolt41 = new JLabel();
		lbllblNoStg41 = new JLabel();
		lblNoStg41 = new JLabel();
		pnlPrime41 = new JPanel();
		label149 = new JLabel();
		label150 = new JLabel();
		label225 = new JLabel();
		label56 = new JLabel();
		label151 = new JLabel();
		label152 = new JLabel();
		lblTitSHd41 = new JLabel();
		lblRem41 = new JLabel();
		lblPerfHz41 = new JLabel();
		label338 = new JLabel();
		label339 = new JLabel();
		lblTitVHC41 = new JLabel();
		lblTitTotHd41 = new JLabel();
		label157 = new JLabel();
		label158 = new JLabel();
		label162 = new JLabel();
		label164 = new JLabel();
		lblTitFlow41 = new JLabel();
		label178 = new JLabel();
		lblTitMI41 = new JLabel();
		label180 = new JLabel();
		label226 = new JLabel();
		lblSHd41 = new JLabel();
		lblDHd41 = new JLabel();
		lblVHd41 = new JLabel();
		lblTotHd41 = new JLabel();
		lblFlowUnitS41 = new JLabel();
		label166 = new JLabel();
		label167 = new JLabel();
		label168 = new JLabel();
		lblFlowUnitT41 = new JLabel();
		lblTotHdT41 = new JLabel();
		lblMITitUnit41 = new JLabel();
		tblRes41 = new JTable();
		pnlGraph41 = new JPanel();
		pnlBot41 = new JPanel();
		lblCas41 = new JLabel();
		lblGC41 = new JLabel();
		lblMaxDis41 = new JLabel();
		lblGCText41 = new JLabel();
		lblWith41 = new JLabel();
		lblVerBy10 = new JLabel();
		lblDt41 = new JLabel();
		lblTestedBy41 = new JLabel();
		lblVerBy11 = new JLabel();
		label67 = new JLabel();
		lbllblDt41 = new JLabel();
		pnlBotTwo41 = new JPanel();
		label68 = new JLabel();
		lblPumpType41_2 = new JLabel();
		label73 = new JLabel();
		lblPSno41_2 = new JLabel();
		label74 = new JLabel();
		lblDt41_2 = new JLabel();
		lblHdR41_2 = new JLabel();
		lblHeadR41_2 = new JLabel();
		lbllblPSize41_2 = new JLabel();
		lblPSz41_2 = new JLabel();
		lbllblFreq41_2 = new JLabel();
		lblFreq41_2 = new JLabel();
		pnlBotRes41 = new JPanel();
		label69 = new JLabel();
		lblFlowUnit41 = new JLabel();
		lblHd41 = new JLabel();
		lbllblMI41 = new JLabel();
		lbllblEff41 = new JLabel();
		lbllblMaxC41 = new JLabel();
		label75 = new JLabel();
		lblFlow41_2 = new JLabel();
		lblHead41_2 = new JLabel();
		lblMotIp41_2 = new JLabel();
		lblEff41_2 = new JLabel();
		lblCur41_2 = new JLabel();
		label78 = new JLabel();
		lblFlowAct41 = new JLabel();
		lblHeadAct41 = new JLabel();
		lblMotIpAct41 = new JLabel();
		lblEffAct41 = new JLabel();
		lblCurAct41 = new JLabel();
		label80 = new JLabel();
		lblFlowRes41 = new JLabel();
		lblHeadRes41 = new JLabel();
		lblMotIpRes41 = new JLabel();
		lblEffRes41 = new JLabel();
		lblCurRes41 = new JLabel();
		pnlBotRes41R = new JPanel();
		lblLH41 = new JLabel();
		lblHH41 = new JLabel();
		label235 = new JLabel();
		lblFlowUnit41RL = new JLabel();
		lblHd41RL = new JLabel();
		lblFlowUnit41RH = new JLabel();
		lblHd41RH = new JLabel();
		label244 = new JLabel();
		label245 = new JLabel();
		label246 = new JLabel();
		label248 = new JLabel();
		lbllblEff41R1 = new JLabel();
		lbllblMI41R1 = new JLabel();
		lbllblEff41R2 = new JLabel();
		lbllblMI41R2 = new JLabel();
		lblFlow41RL = new JLabel();
		lblHead41RL = new JLabel();
		lblEff41RL = new JLabel();
		lblMotIp41RL = new JLabel();
		lblFlow41RH = new JLabel();
		lblHead41RH = new JLabel();
		lblEff41RH = new JLabel();
		lblMotIp41RH = new JLabel();
		lblCur41R = new JLabel();
		lblFlowAct41RL = new JLabel();
		lblHeadAct41RL = new JLabel();
		lblEffAct41RL = new JLabel();
		lblMotIpAct41RL = new JLabel();
		lblFlowAct41RH = new JLabel();
		lblHeadAct41RH = new JLabel();
		lblEffAct41RH = new JLabel();
		lblMotIpAct41RH = new JLabel();
		lblCurAct41R = new JLabel();
		lblFlowRes41RL = new JLabel();
		lblHeadRes41RL = new JLabel();
		lblEffRes41RL = new JLabel();
		lblMotIpRes41RL = new JLabel();
		lblFlowRes41RH = new JLabel();
		lblHeadRes41RH = new JLabel();
		lblEffRes41RH = new JLabel();
		lblMotIpRes41RH = new JLabel();
		lblCurRes41R = new JLabel();
		pnlOptTest41 = new JPanel();
		lblCas42 = new JLabel();
		lblSP42 = new JLabel();
		lblAppVer41 = new JLabel();
		scrollPane42 = new JScrollPane();
		printArea42 = new JPanel();
		pnlPrint42 = new JPanel();
		lblCustLogo42 = new JLabel();
		lblCompName42 = new JLabel();
		lblIS42 = new JLabel();
		lblISILogo42 = new JLabel();
		lblCompAdr42 = new JLabel();
		lblTitle42 = new JLabel();
		lblISIRefNo42 = new JLabel();
		lblPage42 = new JLabel();
		pnlTop42 = new JPanel();
		label60 = new JLabel();
		lblPumpType42 = new JLabel();
		label86 = new JLabel();
		lblDt42 = new JLabel();
		pnlPrime42 = new JPanel();
		label186 = new JLabel();
		label187 = new JLabel();
		lblTypeCount42 = new JLabel();
		label342 = new JLabel();
		lblDis42 = new JLabel();
		lblHd42 = new JLabel();
		lbllblMI42 = new JLabel();
		lbllblEff42 = new JLabel();
		label190 = new JLabel();
		lblFlow42 = new JLabel();
		lblHead42 = new JLabel();
		lblMotIp42 = new JLabel();
		lblEff42 = new JLabel();
		lblMC42 = new JLabel();
		tblRes42 = new JTable();
		pnlBot42 = new JPanel();
		label96 = new JLabel();
		lblTotTest42 = new JLabel();
		label97 = new JLabel();
		lblTotTypeTest42 = new JLabel();
		lblTestedBy42 = new JLabel();
		lblVerBy9 = new JLabel();
		label98 = new JLabel();
		lblAppVer42 = new JLabel();
		pnlRep43 = new JPanel();
		pnlPumpOpt = new JPanel();
		optAllPump = new JRadioButton();
		optSelPump = new JRadioButton();
		lblNonISI = new JLabel();
		scrollPane43 = new JScrollPane();
		printArea43 = new JPanel();
		pnlPrint43 = new JPanel();
		lblCustLogo43 = new JLabel();
		lblCompName43 = new JLabel();
		lblIS43 = new JLabel();
		lblISILogo43 = new JLabel();
		lblCompAdr43 = new JLabel();
		lblTitle43 = new JLabel();
		lblISIRefNo43 = new JLabel();
		lblTitle431 = new JLabel();
		lblPage43 = new JLabel();
		pnlTop43 = new JPanel();
		label87 = new JLabel();
		lblDt43 = new JLabel();
		pnlPrime43 = new JPanel();
		label188 = new JLabel();
		lblDis43 = new JLabel();
		lblHd43 = new JLabel();
		lbllblMI43 = new JLabel();
		lbllblEff43 = new JLabel();
		label191 = new JLabel();
		label352 = new JLabel();
		label348 = new JLabel();
		label349 = new JLabel();
		label350 = new JLabel();
		label351 = new JLabel();
		label353 = new JLabel();
		label354 = new JLabel();
		label355 = new JLabel();
		label356 = new JLabel();
		label358 = new JLabel();
		label359 = new JLabel();
		label360 = new JLabel();
		label361 = new JLabel();
		label362 = new JLabel();
		label363 = new JLabel();
		label371 = new JLabel();
		label372 = new JLabel();
		label373 = new JLabel();
		label374 = new JLabel();
		label375 = new JLabel();
		label364 = new JLabel();
		label365 = new JLabel();
		label366 = new JLabel();
		label367 = new JLabel();
		label368 = new JLabel();
		tblRes43 = new JTable();
		pnlBot43 = new JPanel();
		lblTestedBy43 = new JLabel();
		label101 = new JLabel();
		lblVerBy8 = new JLabel();
		lblAppVer43 = new JLabel();
		pnlRep5 = new JPanel();
		pnlFactor = new JPanel();
		chkQvH = new JCheckBox();
		chkQvMI = new JCheckBox();
		chkQvOE = new JCheckBox();
		chkQvC = new JCheckBox();
		scrollPane5 = new JScrollPane();
		printArea5 = new JPanel();
		lblRtWarn5 = new JLabel();
		pnlPrint5 = new JPanel();
		lblCustLogo5 = new JLabel();
		lblCompName5 = new JLabel();
		lblIS5 = new JLabel();
		lblISILogo5 = new JLabel();
		lblCompAdr5 = new JLabel();
		lblTitle5 = new JLabel();
		lblISIRefNo5 = new JLabel();
		label4 = new JLabel();
		lblPumpType5 = new JLabel();
		lblPage5 = new JLabel();
		pnlGraph5 = new JPanel();
		pnlBot5 = new JPanel();
		lblTestedBy5 = new JLabel();
		lblVerBy7 = new JLabel();
		label104 = new JLabel();
		lblAppVer5 = new JLabel();
		pnlRep6 = new JPanel();
		cmdSaveCustom = new JButton();
		scrollPane6 = new JScrollPane();
		printArea6 = new JPanel();
		pnlPrint6 = new JPanel();
		lblIS6 = new JLabel();
		lblCustLogo6 = new JLabel();
		lblCompName6 = new JLabel();
		lblISILogo6 = new JLabel();
		lblCompAdr6 = new JLabel();
		lblTitle6 = new JLabel();
		lblISIRefNo6 = new JLabel();
		lblPage6 = new JLabel();
		pnlTop6 = new JPanel();
		label83 = new JLabel();
		lblPumpType6 = new JLabel();
		label91 = new JLabel();
		lblPh6 = new JLabel();
		label92 = new JLabel();
		lblPSz6 = new JLabel();
		label111 = new JLabel();
		lblNoStage6 = new JLabel();
		label85 = new JLabel();
		lblPSno6 = new JLabel();
		label105 = new JLabel();
		label89 = new JLabel();
		lblMRt6 = new JLabel();
		label95 = new JLabel();
		lblFreq6 = new JLabel();
		label110 = new JLabel();
		lblSpeed6 = new JLabel();
		label112 = new JLabel();
		lblMotType6 = new JLabel();
		label90 = new JLabel();
		lblDt6 = new JLabel();
		label117 = new JLabel();
		label118 = new JLabel();
		label119 = new JLabel();
		label94 = new JLabel();
		lblV6 = new JLabel();
		lblHdDuty6 = new JLabel();
		lblHead6 = new JLabel();
		lblHdRg6 = new JLabel();
		lblHeadR6 = new JLabel();
		label113 = new JLabel();
		lblMotEff6 = new JLabel();
		label115 = new JLabel();
		txtCap6 = new JTextField();
		txtCRMain6 = new JTextField();
		txtCRAux6 = new JTextField();
		txtCRTemp6 = new JTextField();
		label99 = new JLabel();
		lblCur6 = new JLabel();
		lblFlowRtLabel6 = new JLabel();
		lblFlow6 = new JLabel();
		lblFlowRtLabel62 = new JLabel();
		lblDisR6 = new JLabel();
		label114 = new JLabel();
		txtDuty6 = new JTextField();
		label116 = new JLabel();
		txtCon6 = new JTextField();
		label214 = new JLabel();
		label215 = new JLabel();
		label216 = new JLabel();
		label93 = new JLabel();
		lblMotIp6 = new JLabel();
		lblHdSO6 = new JLabel();
		lblSHHd6 = new JLabel();
		label121 = new JLabel();
		lblSHCur6 = new JLabel();
		label124 = new JLabel();
		lblSHPow6 = new JLabel();
		label212 = new JLabel();
		label213 = new JLabel();
		label125 = new JLabel();
		txtSBed6 = new JTextField();
		panel2 = new JPanel();
		label84 = new JLabel();
		label218 = new JLabel();
		label217 = new JLabel();
		txtIRTest = new JTextField();
		label219 = new JLabel();
		txtSPTime = new JTextField();
		label220 = new JLabel();
		pnlPrime6 = new JPanel();
		label189 = new JLabel();
		label221 = new JLabel();
		label106 = new JLabel();
		label138 = new JLabel();
		label193 = new JLabel();
		label192 = new JLabel();
		lblPerfHz6 = new JLabel();
		label343 = new JLabel();
		label357 = new JLabel();
		label107 = new JLabel();
		label369 = new JLabel();
		label370 = new JLabel();
		label195 = new JLabel();
		label196 = new JLabel();
		label197 = new JLabel();
		label198 = new JLabel();
		label199 = new JLabel();
		label200 = new JLabel();
		label201 = new JLabel();
		label7 = new JLabel();
		label29 = new JLabel();
		label222 = new JLabel();
		lblHdS6 = new JLabel();
		lblHdD6 = new JLabel();
		lblHdHC6 = new JLabel();
		lblHdVHC6 = new JLabel();
		lblHdT6 = new JLabel();
		lblFlowUnitS6 = new JLabel();
		label207 = new JLabel();
		label208 = new JLabel();
		label209 = new JLabel();
		label202 = new JLabel();
		lblFlowUnitS62 = new JLabel();
		lblHdRT6 = new JLabel();
		label211 = new JLabel();
		label223 = new JLabel();
		label46 = new JLabel();
		tblRes6 = new JTable();
		label256 = new JLabel();
		pnlBot6_2 = new JPanel();
		lblTestedBy6 = new JLabel();
		lblVerBy6 = new JLabel();
		label109 = new JLabel();
		pnlBot6_1 = new JPanel();
		label249 = new JLabel();
		label250 = new JLabel();
		label251 = new JLabel();
		label252 = new JLabel();
		label258 = new JLabel();
		label253 = new JLabel();
		label267 = new JLabel();
		label268 = new JLabel();
		label255 = new JLabel();
		label254 = new JLabel();
		label271 = new JLabel();
		label263 = new JLabel();
		label260 = new JLabel();
		label264 = new JLabel();
		label261 = new JLabel();
		label265 = new JLabel();
		label262 = new JLabel();
		label266 = new JLabel();
		label269 = new JLabel();
		label270 = new JLabel();
		txtV6 = new JTextField();
		txtA6 = new JTextField();
		txtP6 = new JTextField();
		txtS6 = new JTextField();
		txtF6 = new JTextField();
		txtH6 = new JTextField();
		txtBT6 = new JTextField();
		txtRT6 = new JTextField();
		txtM61 = new JTextField();
		txtA61 = new JTextField();
		txtT61 = new JTextField();
		txtM62 = new JTextField();
		txtA62 = new JTextField();
		txtT62 = new JTextField();
		label257 = new JLabel();
		txtRem = new JTextField();
		lblAppVer6 = new JLabel();

		//======== this ========
		setTitle("Doer PumpView: Reports");
		setFont(new Font("Arial", Font.PLAIN, 12));
		setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		setResizable(false);
		setMinimumSize(new Dimension(300, 300));
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				thisWindowClosing();
			}
		});
		contentPane = getContentPane();
		contentPane.setLayout(new TableLayout(new double[][] {
			{405, TableLayout.FILL},
			{TableLayout.FILL}}));
		((TableLayout)contentPane.getLayout()).setHGap(3);
		((TableLayout)contentPane.getLayout()).setVGap(5);

		//======== pnlOpt ========
		{
			pnlOpt.setBorder(new TitledBorder(null, "Print Options", TitledBorder.LEADING, TitledBorder.TOP,
				new Font("Arial", Font.BOLD, 14), Color.blue));
			pnlOpt.setFocusable(false);
			pnlOpt.setLayout(new TableLayout(new double[][] {
				{TableLayout.FILL, TableLayout.FILL},
				{TableLayout.FILL, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, 0}}));
			((TableLayout)pnlOpt.getLayout()).setHGap(5);
			((TableLayout)pnlOpt.getLayout()).setVGap(5);

			//======== pnlFilter ========
			{
				pnlFilter.setBorder(new TitledBorder(null, "Test List", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
					new Font("Arial", Font.BOLD, 12), Color.blue));
				pnlFilter.setLayout(new TableLayout(new double[][] {
					{60, 53, 39, 29, 74, TableLayout.FILL},
					{TableLayout.FILL, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED}}));
				((TableLayout)pnlFilter.getLayout()).setHGap(5);
				((TableLayout)pnlFilter.getLayout()).setVGap(5);

				//======== scrlDev ========
				{

					//---- tblTest ----
					tblTest.setModel(new DefaultTableModel(
						new Object[][] {
						},
						new String[] {
							"Select", "Pump SNo."
						}
					) {
						Class<?>[] columnTypes = new Class<?>[] {
							Boolean.class, Object.class
						};
						boolean[] columnEditable = new boolean[] {
							true, false
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
						TableColumnModel cm = tblTest.getColumnModel();
						cm.getColumn(0).setResizable(false);
						cm.getColumn(0).setMaxWidth(50);
						cm.getColumn(0).setPreferredWidth(59);
						cm.getColumn(1).setResizable(false);
					}
					tblTest.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					tblTest.setFont(new Font("SansSerif", Font.PLAIN, 13));
					tblTest.setShowVerticalLines(false);
					tblTest.setSelectionBackground(new Color(255, 255, 204));
					tblTest.setSelectionForeground(Color.black);
					scrlDev.setViewportView(tblTest);
				}
				pnlFilter.add(scrlDev, new TableLayoutConstraints(0, 0, 4, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmdRefresh ----
				cmdRefresh.setText("<html><body align=\"center\">Refresh<br>Preview<font size=-2><br>[F5]</html>");
				cmdRefresh.setFont(new Font("Arial", Font.PLAIN, 14));
				cmdRefresh.setIcon(new ImageIcon(getClass().getResource("/img/refresh.PNG")));
				cmdRefresh.setBackground(new Color(236, 233, 213));
				cmdRefresh.setToolTipText("Click on this to refresh print preview for selected tests");
				cmdRefresh.setHorizontalTextPosition(SwingConstants.CENTER);
				cmdRefresh.setVerticalTextPosition(SwingConstants.BOTTOM);
				cmdRefresh.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cmdRefreshActionPerformed();
					}
				});
				pnlFilter.add(cmdRefresh, new TableLayoutConstraints(5, 0, 5, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmdSel ----
				cmdSel.setText("Select All");
				cmdSel.setFont(new Font("Arial", Font.PLAIN, 12));
				cmdSel.setToolTipText("To select all test in below table");
				cmdSel.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cmdSelActionPerformed();
					}
				});
				pnlFilter.add(cmdSel, new TableLayoutConstraints(5, 1, 5, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmdClear ----
				cmdClear.setText("Clear All");
				cmdClear.setFont(new Font("Arial", Font.PLAIN, 12));
				cmdClear.setToolTipText("To clear all selected testes in below table");
				cmdClear.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cmdClearActionPerformed();
					}
				});
				pnlFilter.add(cmdClear, new TableLayoutConstraints(5, 2, 5, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblResult ----
				lblResult.setText("0 Test(s) Found");
				lblResult.setFont(new Font("Arial", Font.BOLD, 14));
				lblResult.setForeground(Color.blue);
				pnlFilter.add(lblResult, new TableLayoutConstraints(0, 3, 2, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//======== menuBar1 ========
				{
					menuBar1.setBorder(null);
					menuBar1.setBorderPainted(false);

					//======== menFilter ========
					{
						menFilter.setText("Filter                  \u25bc");
						menFilter.setFont(new Font("Arial", Font.PLAIN, 12));
						menFilter.setBackground(new Color(51, 153, 255));
						menFilter.setHorizontalTextPosition(SwingConstants.LEADING);
						menFilter.setBorder(null);

						//---- optRt ----
						optRt.setText("Routine Test Only");
						optRt.setFont(new Font("Arial", Font.PLAIN, 12));
						optRt.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								optRtActionPerformed();
							}
						});
						menFilter.add(optRt);

						//---- optType ----
						optType.setText("Type Test Only");
						optType.setFont(new Font("Arial", Font.PLAIN, 12));
						optType.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								optTypeActionPerformed();
							}
						});
						menFilter.add(optType);

						//---- optBoth ----
						optBoth.setText("Both Routine & Type Tests");
						optBoth.setFont(new Font("Arial", Font.PLAIN, 12));
						optBoth.setSelected(true);
						optBoth.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								optBothActionPerformed();
							}
						});
						menFilter.add(optBoth);
						menFilter.addSeparator();

						//---- chkRtV ----
						chkRtV.setText("Select Tests Performed Under Rated Voltage Only");
						chkRtV.setFont(new Font("Arial", Font.PLAIN, 12));
						menFilter.add(chkRtV);
					}
					menuBar1.add(menFilter);
				}
				pnlFilter.add(menuBar1, new TableLayoutConstraints(5, 3, 5, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			pnlOpt.add(pnlFilter, new TableLayoutConstraints(0, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//======== pnlAdFilter ========
			{
				pnlAdFilter.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlAdFilter.setBorder(new TitledBorder(null, "Advanced Filter", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
					new Font("Arial", Font.BOLD, 12), Color.blue));
				pnlAdFilter.setLayout(new TableLayout(new double[][] {
					{TableLayout.PREFERRED, TableLayout.PREFERRED, 95, TableLayout.PREFERRED, 95},
					{TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED}}));
				((TableLayout)pnlAdFilter.getLayout()).setHGap(5);
				((TableLayout)pnlAdFilter.getLayout()).setVGap(5);

				//---- label11 ----
				label11.setText("Pump Type");
				label11.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlAdFilter.add(label11, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblNonISIFilter ----
				lblNonISIFilter.setText("Non-ISI");
				lblNonISIFilter.setHorizontalAlignment(SwingConstants.LEFT);
				lblNonISIFilter.setForeground(Color.red);
				pnlAdFilter.add(lblNonISIFilter, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmbType ----
				cmbType.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 12));
				cmbType.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cmbTypeActionPerformed();
					}
				});
				pnlAdFilter.add(cmbType, new TableLayoutConstraints(2, 0, 4, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label12 ----
				label12.setText("Date (DD-MM-YYYY)");
				label12.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlAdFilter.add(label12, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label13 ----
				label13.setText("From");
				label13.setFont(new Font("Arial", Font.BOLD, 12));
				pnlAdFilter.add(label13, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- fromDt ----
				fromDt.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 12));
				fromDt.setDateFormatString("dd-MM-yyyy");
				fromDt.addPropertyChangeListener(new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent e) {
						fromDtPropertyChange();
					}
				});
				pnlAdFilter.add(fromDt, new TableLayoutConstraints(2, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label14 ----
				label14.setText("To");
				label14.setFont(new Font("Arial", Font.BOLD, 12));
				pnlAdFilter.add(label14, new TableLayoutConstraints(3, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- toDt ----
				toDt.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 12));
				toDt.setDateFormatString("dd-MM-yyyy");
				toDt.addPropertyChangeListener(new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent e) {
						toDtPropertyChange();
					}
				});
				pnlAdFilter.add(toDt, new TableLayoutConstraints(4, 1, 4, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label15 ----
				label15.setText("Pump Serial Number");
				label15.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlAdFilter.add(label15, new TableLayoutConstraints(0, 2, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label16 ----
				label16.setText("From");
				label16.setFont(new Font("Arial", Font.BOLD, 12));
				pnlAdFilter.add(label16, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- txtSlNoFrom ----
				txtSlNoFrom.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 12));
				txtSlNoFrom.addFocusListener(new FocusAdapter() {
					@Override
					public void focusLost(FocusEvent e) {
						txtSlNoFromFocusLost();
					}
				});
				pnlAdFilter.add(txtSlNoFrom, new TableLayoutConstraints(2, 2, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label17 ----
				label17.setText("To");
				label17.setFont(new Font("Arial", Font.BOLD, 12));
				pnlAdFilter.add(label17, new TableLayoutConstraints(3, 2, 3, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- txtSlNoTo ----
				txtSlNoTo.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 12));
				txtSlNoTo.addFocusListener(new FocusAdapter() {
					@Override
					public void focusLost(FocusEvent e) {
						txtSlNoToFocusLost();
					}
				});
				pnlAdFilter.add(txtSlNoTo, new TableLayoutConstraints(4, 2, 4, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label5 ----
				label5.setText("Remarks");
				label5.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlAdFilter.add(label5, new TableLayoutConstraints(0, 3, 0, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmbRemarks ----
				cmbRemarks.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 12));
				cmbRemarks.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cmbRemarksActionPerformed();
					}
				});
				pnlAdFilter.add(cmbRemarks, new TableLayoutConstraints(2, 3, 4, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label18 ----
				label18.setText("Assembly Line");
				label18.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlAdFilter.add(label18, new TableLayoutConstraints(0, 4, 1, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmbLine ----
				cmbLine.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 12));
				cmbLine.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cmbLineActionPerformed();
					}
				});
				pnlAdFilter.add(cmbLine, new TableLayoutConstraints(2, 4, 2, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			pnlOpt.add(pnlAdFilter, new TableLayoutConstraints(0, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//======== pnlViewOpt ========
			{
				pnlViewOpt.setBorder(new TitledBorder(null, "View Options", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
					new Font("Arial", Font.BOLD, 12), Color.blue));
				pnlViewOpt.setLayout(new TableLayout(new double[][] {
					{104, 53, 80, TableLayout.FILL},
					{TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED}}));
				((TableLayout)pnlViewOpt.getLayout()).setHGap(5);
				((TableLayout)pnlViewOpt.getLayout()).setVGap(5);

				//---- label76 ----
				label76.setText("Graph Rendering");
				label76.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlViewOpt.add(label76, new TableLayoutConstraints(0, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- optRndrLine ----
				optRndrLine.setText("Line");
				optRndrLine.setFont(new Font("Arial", Font.PLAIN, 12));
				optRndrLine.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						optRndrLineActionPerformed();
					}
				});
				pnlViewOpt.add(optRndrLine, new TableLayoutConstraints(2, 0, 2, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- optRndrSm ----
				optRndrSm.setText("Smooth Curve");
				optRndrSm.setFont(new Font("Arial", Font.PLAIN, 12));
				optRndrSm.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						optRndrSmActionPerformed();
					}
				});
				pnlViewOpt.add(optRndrSm, new TableLayoutConstraints(3, 0, 3, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label71 ----
				label71.setText("Head Unit");
				label71.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlViewOpt.add(label71, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmbHdUnit ----
				cmbHdUnit.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 12));
				cmbHdUnit.setToolTipText("Converts the head into selected unit only for reports");
				cmbHdUnit.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cmbHdUnitActionPerformed();
					}
				});
				pnlViewOpt.add(cmbHdUnit, new TableLayoutConstraints(2, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblHdUnit ----
				lblHdUnit.setText("(Declared is '')");
				lblHdUnit.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlViewOpt.add(lblHdUnit, new TableLayoutConstraints(3, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label66 ----
				label66.setText("Discharge Unit");
				label66.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlViewOpt.add(label66, new TableLayoutConstraints(0, 2, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmbDisUnit ----
				cmbDisUnit.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 12));
				cmbDisUnit.setToolTipText("Converts the discharge into selected unit only for reports");
				cmbDisUnit.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cmbDisUnitActionPerformed();
					}
				});
				pnlViewOpt.add(cmbDisUnit, new TableLayoutConstraints(2, 2, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblDisUnit ----
				lblDisUnit.setText("(Declared is '')");
				lblDisUnit.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlViewOpt.add(lblDisUnit, new TableLayoutConstraints(3, 2, 3, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label70 ----
				label70.setText("Rated Performance Against");
				label70.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlViewOpt.add(label70, new TableLayoutConstraints(0, 3, 1, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- optSpeed ----
				optSpeed.setText("Speed");
				optSpeed.setFont(new Font("Arial", Font.PLAIN, 12));
				optSpeed.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						optSpeedActionPerformed();
					}
				});
				pnlViewOpt.add(optSpeed, new TableLayoutConstraints(2, 3, 2, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- optFreq ----
				optFreq.setText("Frequency");
				optFreq.setFont(new Font("Arial", Font.PLAIN, 12));
				optFreq.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						optFreqActionPerformed();
					}
				});
				pnlViewOpt.add(optFreq, new TableLayoutConstraints(3, 3, 3, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- chkDisR ----
				chkDisR.setText("Show Graph For Discharge Range In Given Head Range");
				chkDisR.setFont(new Font("Arial", Font.PLAIN, 12));
				chkDisR.setToolTipText("Plots the graph for discharge in both low and high heads");
				chkDisR.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						chkDisRActionPerformed();
					}
				});
				pnlViewOpt.add(chkDisR, new TableLayoutConstraints(0, 4, 3, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- chkCat ----
				chkCat.setText("Show Pump Category In Report Header");
				chkCat.setFont(new Font("Arial", Font.PLAIN, 12));
				chkCat.setToolTipText("Show Pump Category In Report Header");
				chkCat.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						chkCatActionPerformed();
					}
				});
				pnlViewOpt.add(chkCat, new TableLayoutConstraints(0, 5, 3, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmdSet ----
				cmdSet.setText("Color Settings...");
				cmdSet.setFont(new Font("Arial", Font.PLAIN, 12));
				cmdSet.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cmdSetActionPerformed();
					}
				});
				pnlViewOpt.add(cmdSet, new TableLayoutConstraints(0, 6, 3, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			pnlOpt.add(pnlViewOpt, new TableLayoutConstraints(0, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdExp ----
			cmdExp.setText("Export as Excel file");
			cmdExp.setFont(new Font("Arial", Font.PLAIN, 14));
			cmdExp.setToolTipText("Click on this to print current page");
			cmdExp.setMnemonic('E');
			cmdExp.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					cmdExpActionPerformed();
				}
			});
			pnlOpt.add(cmdExp, new TableLayoutConstraints(0, 3, 1, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdPrint ----
			cmdPrint.setText("Print Current Page");
			cmdPrint.setFont(new Font("Arial", Font.PLAIN, 14));
			cmdPrint.setIcon(new ImageIcon(getClass().getResource("/img/print.PNG")));
			cmdPrint.setToolTipText("Click on this to print current page");
			cmdPrint.setMnemonic('P');
			cmdPrint.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					cmdPrintActionPerformed();
				}
			});
			pnlOpt.add(cmdPrint, new TableLayoutConstraints(0, 4, 0, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdPrintAll ----
			cmdPrintAll.setText("Print All");
			cmdPrintAll.setFont(new Font("Arial", Font.PLAIN, 14));
			cmdPrintAll.setIcon(new ImageIcon(getClass().getResource("/img/printall.PNG")));
			cmdPrintAll.setToolTipText("Click on this to print all pages");
			cmdPrintAll.setMnemonic('A');
			cmdPrintAll.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					cmdPrintAllActionPerformed();
				}
			});
			pnlOpt.add(cmdPrintAll, new TableLayoutConstraints(1, 4, 1, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdClose ----
			cmdClose.setText("Close");
			cmdClose.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					cmdCloseActionPerformed();
				}
			});
			pnlOpt.add(cmdClose, new TableLayoutConstraints(0, 5, 1, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		}
		contentPane.add(pnlOpt, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
	}
	// end of create  objects

	private void createObjects2() {


		//======== printArea ========
		{
			printArea.setBorder(new LineBorder(Color.blue, 2));
			printArea.setBackground(Color.white);
			printArea.setLayout(new TableLayout(new double[][] {
				{900},
				{1450}}));

			//======== pnlPrint ========
			{
				pnlPrint.setBorder(null);
				pnlPrint.setBackground(Color.white);
				pnlPrint.setLayout(new TableLayout(new double[][] {
					{40, 120, TableLayout.FILL, 120, 15},
					{35, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, TableLayout.PREFERRED, 70, TableLayout.FILL, TableLayout.PREFERRED}}));

				//---- lblIS ----
				lblIS.setText("IS:");
				lblIS.setHorizontalAlignment(SwingConstants.CENTER);
				lblIS.setFont(new Font("Arial", Font.BOLD, 10));
				lblIS.setName("label");
				pnlPrint.add(lblIS, new TableLayoutConstraints(3, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblCustLogo ----
				lblCustLogo.setHorizontalAlignment(SwingConstants.CENTER);
				pnlPrint.add(lblCustLogo, new TableLayoutConstraints(1, 1, 1, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblCompName ----
				lblCompName.setText("Company Name");
				lblCompName.setFont(new Font("Arial", Font.BOLD, 16));
				lblCompName.setHorizontalAlignment(SwingConstants.CENTER);
				lblCompName.setName("header");
				pnlPrint.add(lblCompName, new TableLayoutConstraints(2, 1, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblISILogo ----
				lblISILogo.setIcon(new ImageIcon(getClass().getResource("/img/isi_logo.PNG")));
				lblISILogo.setHorizontalAlignment(SwingConstants.CENTER);
				lblISILogo.setVerticalAlignment(SwingConstants.TOP);
				lblISILogo.setFont(new Font("Arial", Font.PLAIN, 12));
				lblISILogo.setIconTextGap(0);
				pnlPrint.add(lblISILogo, new TableLayoutConstraints(3, 2, 3, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblCompAdr ----
				lblCompAdr.setText("Address Line 1 and Line 2");
				lblCompAdr.setFont(new Font("Arial", Font.BOLD, 12));
				lblCompAdr.setHorizontalAlignment(SwingConstants.CENTER);
				lblCompAdr.setName("header");
				pnlPrint.add(lblCompAdr, new TableLayoutConstraints(2, 3, 2, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblTitle ----
				lblTitle.setText("PUMP TEST REGISTER");
				lblTitle.setFont(new Font("Arial", Font.BOLD, 14));
				lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
				lblTitle.setName("header");
				pnlPrint.add(lblTitle, new TableLayoutConstraints(2, 5, 2, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblISIRefNo ----
				lblISIRefNo.setHorizontalAlignment(SwingConstants.CENTER);
				lblISIRefNo.setFont(new Font("Arial", Font.BOLD, 10));
				lblISIRefNo.setText("ISI Ref No.");
				lblISIRefNo.setName("label");
				pnlPrint.add(lblISIRefNo, new TableLayoutConstraints(3, 4, 3, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label6 ----
				label6.setText("PUMP TYPE:");
				label6.setFont(new Font("Arial", Font.BOLD, 12));
				label6.setName("label");
				pnlPrint.add(label6, new TableLayoutConstraints(1, 7, 1, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblPumpType ----
				lblPumpType.setText("type");
				lblPumpType.setFont(new Font("Arial", Font.PLAIN, 12));
				lblPumpType.setName("actual");
				pnlPrint.add(lblPumpType, new TableLayoutConstraints(2, 7, 2, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblPage ----
				lblPage.setText("Page 1 of 1");
				lblPage.setFont(new Font("Arial", Font.PLAIN, 12));
				lblPage.setHorizontalAlignment(SwingConstants.RIGHT);
				lblPage.setName("label");
				pnlPrint.add(lblPage, new TableLayoutConstraints(3, 7, 3, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//======== pnlPrime ========
				{
					pnlPrime.setBackground(Color.black);
					pnlPrime.setAutoscrolls(true);
					pnlPrime.setBorder(LineBorder.createBlackLineBorder());
					pnlPrime.setName("outline");
					pnlPrime.setLayout(new TableLayout(new double[][] {
						{65, 74, 74, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, 60},
						{TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL}}));
					((TableLayout)pnlPrime.getLayout()).setHGap(1);
					((TableLayout)pnlPrime.getLayout()).setVGap(1);

					//---- label122 ----
					label122.setText("Date");
					label122.setBackground(Color.white);
					label122.setHorizontalAlignment(SwingConstants.CENTER);
					label122.setFont(new Font("Arial", Font.BOLD, 11));
					label122.setOpaque(true);
					label122.setBorder(null);
					label122.setName("label");
					pnlPrime.add(label122, new TableLayoutConstraints(0, 0, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label123 ----
					label123.setText("Pump SNo");
					label123.setBackground(Color.white);
					label123.setHorizontalAlignment(SwingConstants.CENTER);
					label123.setFont(new Font("Arial", Font.BOLD, 11));
					label123.setOpaque(true);
					label123.setBorder(null);
					label123.setName("label");
					pnlPrime.add(label123, new TableLayoutConstraints(1, 0, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label228 ----
					label228.setText("Motor SNo");
					label228.setBackground(Color.white);
					label228.setHorizontalAlignment(SwingConstants.CENTER);
					label228.setFont(new Font("Arial", Font.BOLD, 11));
					label228.setOpaque(true);
					label228.setBorder(null);
					label228.setName("label");
					pnlPrime.add(label228, new TableLayoutConstraints(2, 0, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label2 ----
					label2.setText("Rated V");
					label2.setBackground(Color.white);
					label2.setFont(new Font("Arial", Font.BOLD, 11));
					label2.setOpaque(true);
					label2.setHorizontalAlignment(SwingConstants.CENTER);
					pnlPrime.add(label2, new TableLayoutConstraints(3, 0, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label20 ----
					label20.setText("Test No");
					label20.setFont(new Font("Arial", Font.BOLD, 11));
					label20.setOpaque(true);
					label20.setBackground(Color.white);
					label20.setHorizontalAlignment(SwingConstants.CENTER);
					label20.setBorder(null);
					label20.setName("label");
					pnlPrime.add(label20, new TableLayoutConstraints(4, 0, 4, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label8 ----
					label8.setText("Type");
					label8.setOpaque(true);
					label8.setFont(new Font("Arial", Font.BOLD, 11));
					label8.setBackground(Color.white);
					label8.setHorizontalAlignment(SwingConstants.CENTER);
					label8.setBorder(null);
					label8.setName("label");
					pnlPrime.add(label8, new TableLayoutConstraints(5, 0, 5, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label9 ----
					label9.setText("Name");
					label9.setBackground(Color.white);
					label9.setOpaque(true);
					label9.setFont(new Font("Arial", Font.BOLD, 11));
					label9.setHorizontalAlignment(SwingConstants.CENTER);
					label9.setBorder(null);
					label9.setName("label");
					pnlPrime.add(label9, new TableLayoutConstraints(6, 0, 6, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label126 ----
					label126.setText("Speed");
					label126.setBackground(Color.white);
					label126.setHorizontalAlignment(SwingConstants.CENTER);
					label126.setFont(new Font("Arial", Font.BOLD, 11));
					label126.setOpaque(true);
					label126.setAutoscrolls(true);
					label126.setBorder(null);
					label126.setName("label");
					pnlPrime.add(label126, new TableLayoutConstraints(7, 0, 7, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label1 ----
					label1.setText("Remark");
					label1.setBackground(Color.white);
					label1.setFont(new Font("Arial", Font.BOLD, 11));
					label1.setOpaque(true);
					label1.setHorizontalAlignment(SwingConstants.CENTER);
					label1.setBorder(null);
					label1.setName("label");
					pnlPrime.add(label1, new TableLayoutConstraints(15, 0, 15, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label155 ----
					label155.setText("S. Head");
					label155.setBackground(Color.white);
					label155.setHorizontalAlignment(SwingConstants.CENTER);
					label155.setFont(new Font("Arial", Font.BOLD, 11));
					label155.setOpaque(true);
					label155.setAutoscrolls(true);
					label155.setBorder(null);
					label155.setName("label");
					pnlPrime.add(label155, new TableLayoutConstraints(8, 0, 8, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label129 ----
					label129.setText("D. Head");
					label129.setBackground(Color.white);
					label129.setHorizontalAlignment(SwingConstants.CENTER);
					label129.setFont(new Font("Arial", Font.BOLD, 11));
					label129.setOpaque(true);
					label129.setAutoscrolls(true);
					label129.setBorder(null);
					label129.setName("label");
					pnlPrime.add(label129, new TableLayoutConstraints(9, 0, 9, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label130 ----
					label130.setText("Flow");
					label130.setBackground(Color.white);
					label130.setHorizontalAlignment(SwingConstants.CENTER);
					label130.setFont(new Font("Arial", Font.BOLD, 11));
					label130.setOpaque(true);
					label130.setAutoscrolls(true);
					label130.setBorder(null);
					label130.setName("label");
					pnlPrime.add(label130, new TableLayoutConstraints(10, 0, 10, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label131 ----
					label131.setText("Voltage");
					label131.setBackground(Color.white);
					label131.setHorizontalAlignment(SwingConstants.CENTER);
					label131.setFont(new Font("Arial", Font.BOLD, 11));
					label131.setOpaque(true);
					label131.setAutoscrolls(true);
					label131.setBorder(null);
					label131.setName("label");
					pnlPrime.add(label131, new TableLayoutConstraints(11, 0, 11, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label132 ----
					label132.setText("Current");
					label132.setBackground(Color.white);
					label132.setHorizontalAlignment(SwingConstants.CENTER);
					label132.setFont(new Font("Arial", Font.BOLD, 11));
					label132.setOpaque(true);
					label132.setAutoscrolls(true);
					label132.setBorder(null);
					label132.setName("label");
					pnlPrime.add(label132, new TableLayoutConstraints(12, 0, 12, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label128 ----
					label128.setText("Power");
					label128.setBackground(Color.white);
					label128.setHorizontalAlignment(SwingConstants.CENTER);
					label128.setFont(new Font("Arial", Font.BOLD, 11));
					label128.setOpaque(true);
					label128.setAutoscrolls(true);
					label128.setBorder(null);
					label128.setName("label");
					pnlPrime.add(label128, new TableLayoutConstraints(13, 0, 13, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label154 ----
					label154.setText("Freq.");
					label154.setBackground(Color.white);
					label154.setHorizontalAlignment(SwingConstants.CENTER);
					label154.setFont(new Font("Arial", Font.BOLD, 11));
					label154.setOpaque(true);
					label154.setAutoscrolls(true);
					label154.setBorder(null);
					label154.setName("label");
					pnlPrime.add(label154, new TableLayoutConstraints(14, 0, 14, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label159 ----
					label159.setText("rpm");
					label159.setBackground(Color.white);
					label159.setHorizontalAlignment(SwingConstants.CENTER);
					label159.setFont(new Font("Arial", Font.BOLD, 11));
					label159.setOpaque(true);
					label159.setAutoscrolls(true);
					label159.setBorder(null);
					label159.setName("label");
					pnlPrime.add(label159, new TableLayoutConstraints(7, 1, 7, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblSHd1 ----
					lblSHd1.setText("mwc");
					lblSHd1.setBackground(Color.white);
					lblSHd1.setHorizontalAlignment(SwingConstants.CENTER);
					lblSHd1.setFont(new Font("Arial", Font.BOLD, 11));
					lblSHd1.setOpaque(true);
					lblSHd1.setAutoscrolls(true);
					lblSHd1.setBorder(null);
					lblSHd1.setName("label");
					pnlPrime.add(lblSHd1, new TableLayoutConstraints(8, 1, 8, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblDHd1 ----
					lblDHd1.setText("mwc");
					lblDHd1.setBackground(Color.white);
					lblDHd1.setHorizontalAlignment(SwingConstants.CENTER);
					lblDHd1.setFont(new Font("Arial", Font.BOLD, 11));
					lblDHd1.setOpaque(true);
					lblDHd1.setAutoscrolls(true);
					lblDHd1.setBorder(null);
					lblDHd1.setName("label");
					pnlPrime.add(lblDHd1, new TableLayoutConstraints(9, 1, 9, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblDis1 ----
					lblDis1.setText("unit");
					lblDis1.setBackground(Color.white);
					lblDis1.setHorizontalAlignment(SwingConstants.CENTER);
					lblDis1.setFont(new Font("Arial", Font.BOLD, 11));
					lblDis1.setOpaque(true);
					lblDis1.setAutoscrolls(true);
					lblDis1.setBorder(null);
					lblDis1.setName("label");
					pnlPrime.add(lblDis1, new TableLayoutConstraints(10, 1, 10, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label139 ----
					label139.setText("V");
					label139.setBackground(Color.white);
					label139.setHorizontalAlignment(SwingConstants.CENTER);
					label139.setFont(new Font("Arial", Font.BOLD, 11));
					label139.setOpaque(true);
					label139.setAutoscrolls(true);
					label139.setBorder(null);
					label139.setName("label");
					pnlPrime.add(label139, new TableLayoutConstraints(11, 1, 11, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label140 ----
					label140.setText("A");
					label140.setBackground(Color.white);
					label140.setHorizontalAlignment(SwingConstants.CENTER);
					label140.setFont(new Font("Arial", Font.BOLD, 11));
					label140.setOpaque(true);
					label140.setAutoscrolls(true);
					label140.setBorder(null);
					label140.setName("label");
					pnlPrime.add(label140, new TableLayoutConstraints(12, 1, 12, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label134 ----
					label134.setText("kW");
					label134.setBackground(Color.white);
					label134.setHorizontalAlignment(SwingConstants.CENTER);
					label134.setFont(new Font("Arial", Font.BOLD, 11));
					label134.setOpaque(true);
					label134.setAutoscrolls(true);
					label134.setBorder(null);
					label134.setName("label");
					pnlPrime.add(label134, new TableLayoutConstraints(13, 1, 13, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label161 ----
					label161.setText("Hz");
					label161.setBackground(Color.white);
					label161.setHorizontalAlignment(SwingConstants.CENTER);
					label161.setFont(new Font("Arial", Font.BOLD, 11));
					label161.setOpaque(true);
					label161.setAutoscrolls(true);
					label161.setBorder(null);
					label161.setName("label");
					pnlPrime.add(label161, new TableLayoutConstraints(14, 1, 14, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- tblRes ----
					tblRes.setModel(new DefaultTableModel(
						new Object[][] {
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
						},
						new String[] {
							null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
						}
					) {
						Class<?>[] columnTypes = new Class<?>[] {
							String.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class
						};
						@Override
						public Class<?> getColumnClass(int columnIndex) {
							return columnTypes[columnIndex];
						}
					});
					{
						TableColumnModel cm = tblRes.getColumnModel();
						cm.getColumn(0).setMinWidth(65);
						cm.getColumn(0).setMaxWidth(65);
						cm.getColumn(0).setPreferredWidth(65);
						cm.getColumn(1).setMinWidth(75);
						cm.getColumn(1).setPreferredWidth(75);
						cm.getColumn(2).setMinWidth(75);
						cm.getColumn(2).setPreferredWidth(75);
						cm.getColumn(15).setMinWidth(60);
						cm.getColumn(15).setMaxWidth(60);
						cm.getColumn(15).setPreferredWidth(60);
					}
					tblRes.setFont(new Font("Arial", Font.PLAIN, 11));
					tblRes.setRowHeight(17);
					tblRes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					tblRes.setGridColor(Color.lightGray);
					tblRes.setBorder(new LineBorder(Color.white));
					tblRes.setRowSelectionAllowed(false);
					tblRes.setAutoscrolls(false);
					tblRes.setFocusable(false);
					tblRes.setEnabled(false);
					tblRes.setIntercellSpacing(new Dimension(5, 1));
					tblRes.setName("outline");
					pnlPrime.add(tblRes, new TableLayoutConstraints(0, 2, 15, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				}
				pnlPrint.add(pnlPrime, new TableLayoutConstraints(1, 8, 3, 8, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//======== pnlBot ========
				{
					pnlBot.setBackground(Color.white);
					pnlBot.setName("outline");
					pnlBot.setBorder(LineBorder.createBlackLineBorder());
					pnlBot.setLayout(new TableLayout(new double[][] {
						{TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL},
						{TableLayout.FILL}}));

					//---- lblTestedBy ----
					lblTestedBy.setText("Tested By");
					lblTestedBy.setFont(new Font("Arial", Font.PLAIN, 13));
					lblTestedBy.setHorizontalAlignment(SwingConstants.CENTER);
					lblTestedBy.setName("label");
					lblTestedBy.setVerticalAlignment(SwingConstants.BOTTOM);
					pnlBot.add(lblTestedBy, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label102 ----
					label102.setText("Approved By");
					label102.setFont(new Font("Arial", Font.PLAIN, 13));
					label102.setHorizontalAlignment(SwingConstants.CENTER);
					label102.setName("label");
					label102.setVerticalAlignment(SwingConstants.BOTTOM);
					pnlBot.add(label102, new TableLayoutConstraints(3, 0, 3, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblVerBy14 ----
					lblVerBy14.setText("Verified By");
					lblVerBy14.setFont(new Font("Arial", Font.PLAIN, 13));
					lblVerBy14.setHorizontalAlignment(SwingConstants.CENTER);
					lblVerBy14.setName("label");
					lblVerBy14.setVerticalAlignment(SwingConstants.BOTTOM);
					pnlBot.add(lblVerBy14, new TableLayoutConstraints(2, 0, 2, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				}
				pnlPrint.add(pnlBot, new TableLayoutConstraints(1, 9, 3, 9, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblAppVer ----
				lblAppVer.setText("Doer PumpViewPro");
				lblAppVer.setFont(new Font("Arial", Font.PLAIN, 10));
				lblAppVer.setHorizontalAlignment(SwingConstants.RIGHT);
				lblAppVer.setForeground(Color.gray);
				pnlPrint.add(lblAppVer, new TableLayoutConstraints(2, 11, 3, 11, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			printArea.add(pnlPrint, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		}
		scrollPane1.setViewportView(printArea);
	}
	
	private void cmdSaveCustomActionPerformed() {
		String pumpNo = "";
		String tmpRtVolt = "";
		if (curMVPump) {
			pumpNo = pumpList.get(curPage-1).substring(0, pumpList.get(curPage-1).indexOf("[")-1);
			tmpRtVolt = pumpList.get(curPage-1).substring(pumpList.get(curPage-1).indexOf("[")+1,pumpList.get(curPage-1).length()-2);
		} else {
			pumpNo = pumpList.get(curPage-1);
			tmpRtVolt = curRtV;
		}
		
		// save changes
		try {
			db.executeUpdate("insert into " + Configuration.CUSTOM_REPORT + " values ('" + curPumpTypeId + "', '" + pumpNo + "', '" + txtCRMain6.getText() + "', '"+ txtCRAux6.getText() + "', '" + txtCRTemp6.getText() + "','" + txtDuty6.getText() + "','" + txtCon6.getText() + "','" + txtSBed6.getText() + "','" + txtIRTest.getText() + "','" + txtSPTime.getText() + "',"+
					   "'" + txtV6.getText() + "','" + txtA6.getText() + "','" + txtP6.getText() + "','" + txtS6.getText() + "','" + txtF6.getText() + "','" + txtH6.getText() + "','" + txtBT6.getText() + "','" + txtRT6.getText() + "'," + 
					   "'" + txtM61.getText() + "','" + txtA61.getText() + "','" + txtT61.getText() + "','" + txtM62.getText() + "','" + txtA62.getText() + "','" + txtT62.getText() + "','" + txtRem.getText() + "','" + txtCap6.getText() +"', '" + tmpRtVolt +"')");
		} catch (SQLException se) {
			try {
				if (se.getMessage().contains("not unique")) {
					// update the reading as it already exist
					db.executeUpdate("update " + Configuration.CUSTOM_REPORT + " set cap = '" + txtCap6.getText() + "',cr_main ='" + txtCRMain6.getText() + "', cr_aux ='" + txtCRAux6.getText() + "', cr_tmp ='" + txtCRTemp6.getText() + "', duty ='" + txtDuty6.getText() + "', con ='" + txtCon6.getText() + "', suc_bed ='" + txtSBed6.getText() + "', ir_val ='" + txtIRTest.getText() + "', sp_time ='" + txtSPTime.getText() + "'," + 
							"tr_v ='" + txtV6.getText() + "', tr_a ='" + txtA6.getText() + "', tr_p ='" + txtP6.getText() + "', tr_s ='" + txtS6.getText() + "', tr_f ='" + txtF6.getText() + "', tr_t ='" + txtH6.getText() + "', tr_bt ='" + txtBT6.getText() + "', tr_rt ='" + txtRT6.getText() + "'," +
							"hr_main ='" + txtM61.getText() + "', hr_aux ='" + txtA61.getText() + "', hr_tmp ='" + txtT61.getText() + "', wt_main ='" + txtM62.getText() + "', wt_aux ='" + txtA62.getText() + "', st_tmp ='" + txtT62.getText() + "'," +
							"remark ='" + txtRem.getText() + "' where pump_type_id ='" + curPumpTypeId + "' and pump_slno= '" + pumpNo + "' and rated_volt = '" + tmpRtVolt +"'");
				} else {
					throw se;
				}
			} catch (SQLException sqle) {
				sqle.printStackTrace();
				JOptionPane.showMessageDialog(this, "Failed saving custom report:" + sqle.getMessage());
			}
		}
	}

	private void cmbHdUnitActionPerformed() {
		if (!initInProgress) {
			curHdUnit = cmbHdUnit.getSelectedItem().toString();
			// refresh current tab
			tabReportStateChanged();
		}
	}

	private void optSpeedActionPerformed() {
		if (!initInProgress) {
			isRatedSpeed = optSpeed.isSelected();
			// refresh current tab
			tabReportStateChanged();
		}
		
	}

	private void optFreqActionPerformed() {
		optSpeedActionPerformed();
	}

	private void optRndrLineActionPerformed() {
		optRndrSmActionPerformed();
	}

	private void optRndrSmActionPerformed() {
		if (!initInProgress) {
			isSmoothLine = optRndrSm.isSelected();
			// refresh current tab
			tabReportStateChanged();
		}
	}

	private void cmdSetActionPerformed() {
		JDialog dlgSet = new ReportSettings(this);
		dlgSet.setVisible(true);
	}

	private void chkCatActionPerformed() {
		setReportTitle();
	}

	private void optAllPumpActionPerformed() {
		if (pumpTypeNonISIList.size() > 0) {
			lblNonISI.setVisible(true);
		}
		Boolean isiRef = !Configuration.LICENCEE_ISI_REF.isEmpty();
		lblIS43.setVisible(isiRef);
		lblISILogo43.setVisible(isiRef);
		lblISIRefNo43.setVisible(isiRef);
		loadISIMaxMin();
	}

	private void optSelPumpActionPerformed() {
		lblNonISI.setVisible(false);
		if (curPumpIsNonISI) {
			lblIS43.setVisible(false);
			lblISILogo43.setVisible(false);
			lblISIRefNo43.setVisible(false);
		}
		loadISIMaxMin();
	}

	private void chkRtVActionPerformed() {
		refreshTestList();
	}

	private void chkQvOEActionPerformed() {
		loadCompareGraph();
	}

	private void cmdExpActionPerformed() {
		// export current report as csv file
		SimpleDateFormat dtFormat = new SimpleDateFormat("yyyyMMddHms");
		String fileName = cmbType.getSelectedItem().toString().replaceAll("[\\s\\\\/\\.]+", "_") + "_" + dtFormat.format(new Date()) + ".xls";
		
		// prompt user to choose folder
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setSelectedFile(new File(fileName));
		if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			// save to file
			WritableWorkbook w = null;
			try {
				// excel workbook
				w = Workbook.createWorkbook(file);
				WritableSheet s = w.createSheet(file.getName(), 0);
				
				// file header
				WritableCellFormat cFormat = new WritableCellFormat();
				cFormat.setAlignment(Alignment.CENTRE);
				cFormat.setFont(new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD));
				String tmpDisUnit = curDisUnit.contains("sup") ? "m^3ph" : curDisUnit;
				String[] colHeader = null;
				if(!(Configuration.LAST_USED_ISSTD.startsWith("IS 8034:") || Configuration.LAST_USED_ISSTD.startsWith("IS 14220:"))) {
					colHeader = new String[] {"Date", "Pump SNo.", "Motor SNo.", "Model", "Rated V", "Test No", "Type", "Name", "Speed (rmp)", 
							"S. Head (" + curHdUnit + ")", "D.Head (" + curHdUnit + ")", "Flow (" + tmpDisUnit + ")", "Voltage (V)", "Current (A)", "Power (kW)", "Freq. (Hz)", "Remark"};
				} else {
					colHeader = new String[] {"Date", "Pump SNo.", "Motor SNo.", "Model", "Rated V", "Test No", "Type", "Name", "Speed (rmp)", "D.Head (" + curHdUnit + ")", "Flow (" + tmpDisUnit + ")", "Voltage (V)", "Current (A)", "Power (kW)", "Freq. (Hz)", "Remark"};
				}
				// ISI handling
				
				for (int i=0; i < colHeader.length; i++) {
					s.addCell(new Label(i, 0, colHeader[i], cFormat));
					s.mergeCells(i, 0, i, 1);
				}
				
				/* body */
				String lastDt = "";
				String lastSlno = "";
				String lastMotSlno = "";
				String lastModel = curPumpType;
				
				int ir = 0;
				int totCol = tblRes.getColumnCount();
				for(int idx=1; idx<=totAvailPagesRegRep; idx++) {
					gotoPageRegRep(idx);
					System.out.println(tblRes.getRowCount());
					for(int i=0; i<tblRes.getRowCount(); i++) {
						for(int j=0; j<tblRes.getColumnCount(); j++) {
							// fixed values for single test
							if (j > 0 && j < 3) {
								if (j == 1) {
									s.addCell(new Label(j, 2+ir, lastSlno));
								} else if (j == 2) {
									s.addCell(new Label(j, 2+ir, lastMotSlno));
									s.addCell(new Label(j+1, 2+ir, lastModel));
								}
								continue;
							}
							if (j == 0 && !tblRes.getValueAt(i, j).toString().isEmpty()) {
								lastDt = tblRes.getValueAt(i, 0).toString();
								lastSlno = tblRes.getValueAt(i, 1).toString();
								lastMotSlno = tblRes.getValueAt(i, 2).toString();
								s.addCell(new Label(j, 2+ir, lastDt));
								continue;
							} else { // detail of test
								if (j == 0) {
									s.addCell(new Label(j, 2+ir, lastDt));
								}
								try {
									if ((j > 6 && j < totCol - 1)  || j == 3 || j == 4) {
										s.addCell(new Number(j + 1, 2+ir, Double.valueOf(tblRes.getValueAt(i, j).toString())));
									} else {
										s.addCell(new Label(j+1, 2+ir, tblRes.getValueAt(i, j).toString()));
									}
								} catch (Exception ce) {
									s.addCell(new Label(j+1, 2+ir, ""));
								}
							}
						}
						ir++;
					}
				}
				// go back to chosen page
				gotoPageRegRep(curPageRegRep); 
				
				w.write();
				JOptionPane.showMessageDialog(this, "Export completed");
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, "Error while exporting report:" + e.getMessage());
				e.printStackTrace();
			} finally {
				if (w != null) {
					try {
						w.close();
					} catch (Exception e) {
						JOptionPane.showMessageDialog(this, "Error while exporting report:" + e.getMessage());
					}
				}
			}
		}
	}

	private void initComponents() {
		createObjects();
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		

		//======== printContainer ========
		{
			printContainer.setBorder(new TitledBorder(null, "Print Preview", TitledBorder.LEADING, TitledBorder.TOP,
				new Font("Arial", Font.BOLD, 14), Color.blue));
			printContainer.setFocusable(false);
			printContainer.setLayout(new TableLayout(new double[][] {
				{TableLayout.FILL},
				{TableLayout.PREFERRED, TableLayout.FILL}}));
			((TableLayout)printContainer.getLayout()).setHGap(5);
			((TableLayout)printContainer.getLayout()).setVGap(5);

			//======== pnlBut ========
			{
				pnlBut.setLayout(new TableLayout(new double[][] {
					{TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL},
					{TableLayout.PREFERRED}}));
				((TableLayout)pnlBut.getLayout()).setHGap(5);
				((TableLayout)pnlBut.getLayout()).setVGap(5);

				//---- cmdFirst ----
				cmdFirst.setIcon(new ImageIcon(getClass().getResource("/img/first.PNG")));
				cmdFirst.setToolTipText("Go to first page");
				cmdFirst.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cmdFirstActionPerformed();
					}
				});
				pnlBut.add(cmdFirst, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmdPrev ----
				cmdPrev.setIcon(new ImageIcon(getClass().getResource("/img/prev.PNG")));
				cmdPrev.setToolTipText("Go to previous page");
				cmdPrev.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cmdPrevActionPerformed();
					}
				});
				pnlBut.add(cmdPrev, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmdNext ----
				cmdNext.setIcon(new ImageIcon(getClass().getResource("/img/next.PNG")));
				cmdNext.setToolTipText("Go to next page");
				cmdNext.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cmdNextActionPerformed();
					}
				});
				pnlBut.add(cmdNext, new TableLayoutConstraints(2, 0, 2, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmdLast ----
				cmdLast.setIcon(new ImageIcon(getClass().getResource("/img/last.PNG")));
				cmdLast.setToolTipText("Go to last page");
				cmdLast.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cmdLastActionPerformed();
					}
				});
				pnlBut.add(cmdLast, new TableLayoutConstraints(3, 0, 3, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			printContainer.add(pnlBut, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//======== tabReport ========
			{
				tabReport.setFont(new Font("Arial", Font.PLAIN, 14));
				tabReport.setOpaque(true);
				tabReport.addChangeListener(new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						tabReportStateChanged();
					}
				});

				//======== scrollPane1 ========
				{
					createObjects2();
				}
				tabReport.addTab("1. Reading Register", scrollPane1);

				//======== scrollPane2 ========
				{

					//======== printArea2 ========
					{
						printArea2.setBorder(new LineBorder(Color.blue, 2));
						printArea2.setBackground(Color.white);
						printArea2.setLayout(new TableLayout(new double[][] {
							{900},
							{TableLayout.FILL}}));

						//======== pnlPrint2 ========
						{
							pnlPrint2.setBorder(null);
							pnlPrint2.setBackground(Color.white);
							pnlPrint2.setLayout(new TableLayout(new double[][] {
								{40, 120, TableLayout.FILL, 120, 15},
								{35, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED}}));

							//---- lblIS2 ----
							lblIS2.setText("IS:");
							lblIS2.setHorizontalAlignment(SwingConstants.CENTER);
							lblIS2.setFont(new Font("Arial", Font.BOLD, 10));
							lblIS2.setName("label");
							pnlPrint2.add(lblIS2, new TableLayoutConstraints(3, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

							//---- lblCustLogo2 ----
							lblCustLogo2.setHorizontalAlignment(SwingConstants.CENTER);
							pnlPrint2.add(lblCustLogo2, new TableLayoutConstraints(1, 1, 1, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

							//---- lblCompName2 ----
							lblCompName2.setText("Company Name");
							lblCompName2.setFont(new Font("Arial", Font.BOLD, 16));
							lblCompName2.setHorizontalAlignment(SwingConstants.CENTER);
							lblCompName2.setName("header");
							pnlPrint2.add(lblCompName2, new TableLayoutConstraints(2, 1, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

							//---- lblISILogo2 ----
							lblISILogo2.setHorizontalAlignment(SwingConstants.CENTER);
							lblISILogo2.setIcon(new ImageIcon(getClass().getResource("/img/isi_logo.PNG")));
							lblISILogo2.setVerticalAlignment(SwingConstants.TOP);
							lblISILogo2.setIconTextGap(0);
							pnlPrint2.add(lblISILogo2, new TableLayoutConstraints(3, 2, 3, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

							//---- lblCompAdr2 ----
							lblCompAdr2.setText("Address Line 1 and Line 2");
							lblCompAdr2.setFont(new Font("Arial", Font.BOLD, 12));
							lblCompAdr2.setHorizontalAlignment(SwingConstants.CENTER);
							lblCompAdr2.setName("header");
							pnlPrint2.add(lblCompAdr2, new TableLayoutConstraints(2, 3, 2, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

							//---- lblTitle2 ----
							lblTitle2.setText("PUMP TEST");
							lblTitle2.setFont(new Font("Arial", Font.BOLD, 14));
							lblTitle2.setHorizontalAlignment(SwingConstants.CENTER);
							lblTitle2.setName("header");
							pnlPrint2.add(lblTitle2, new TableLayoutConstraints(2, 5, 2, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

							//---- lblISIRefNo2 ----
							lblISIRefNo2.setHorizontalAlignment(SwingConstants.CENTER);
							lblISIRefNo2.setFont(new Font("Arial", Font.BOLD, 10));
							lblISIRefNo2.setText("ISI Ref No.");
							lblISIRefNo2.setName("label");
							pnlPrint2.add(lblISIRefNo2, new TableLayoutConstraints(3, 4, 3, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

							//---- lblPage2 ----
							lblPage2.setText("Page 1 of 1");
							lblPage2.setFont(new Font("Arial", Font.PLAIN, 12));
							lblPage2.setHorizontalAlignment(SwingConstants.RIGHT);
							lblPage2.setName("label");
							pnlPrint2.add(lblPage2, new TableLayoutConstraints(3, 7, 3, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

							//======== pnlRep2 ========
							{
								pnlRep2.setBorder(LineBorder.createBlackLineBorder());
								pnlRep2.setBackground(Color.black);
								pnlRep2.setName("outline");
								pnlRep2.setLayout(new TableLayout(new double[][] {
									{TableLayout.FILL},
									{TableLayout.PREFERRED, 1, TableLayout.PREFERRED, 1, TableLayout.PREFERRED, TableLayout.PREFERRED}}));

								//======== pnlTop2 ========
								{
									pnlTop2.setBorder(null);
									pnlTop2.setBackground(Color.white);
									pnlTop2.setFont(new Font("Arial", Font.BOLD, 12));
									pnlTop2.setName("outline");
									pnlTop2.setLayout(new TableLayout(new double[][] {
										{5, TableLayout.PREFERRED, 135, TableLayout.PREFERRED, 70, TableLayout.PREFERRED, 55, TableLayout.PREFERRED, TableLayout.FILL},
										{5, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, 5}}));
									((TableLayout)pnlTop2.getLayout()).setHGap(7);
									((TableLayout)pnlTop2.getLayout()).setVGap(7);

									//---- label10 ----
									label10.setText("PUMP TYPE");
									label10.setFont(new Font("Arial", Font.BOLD, 12));
									label10.setName("label");
									pnlTop2.add(label10, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblPumpType2 ----
									lblPumpType2.setText("type");
									lblPumpType2.setFont(new Font("Arial", Font.PLAIN, 12));
									lblPumpType2.setName("declared");
									pnlTop2.add(lblPumpType2, new TableLayoutConstraints(2, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label32 ----
									label32.setText("RATING (KW/HP)");
									label32.setFont(new Font("Arial", Font.BOLD, 12));
									label32.setName("label");
									pnlTop2.add(label32, new TableLayoutConstraints(3, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblMRt2 ----
									lblMRt2.setText("mrt");
									lblMRt2.setFont(new Font("Arial", Font.PLAIN, 12));
									lblMRt2.setName("declared");
									pnlTop2.add(lblMRt2, new TableLayoutConstraints(4, 1, 4, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label26 ----
									label26.setText("FREQUENCY (Hz)");
									label26.setFont(new Font("Arial", Font.BOLD, 12));
									label26.setName("label");
									pnlTop2.add(label26, new TableLayoutConstraints(5, 1, 5, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblFreq2 ----
									lblFreq2.setText("freq");
									lblFreq2.setFont(new Font("Arial", Font.PLAIN, 12));
									lblFreq2.setName("declared");
									pnlTop2.add(lblFreq2, new TableLayoutConstraints(6, 1, 6, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lbllblPSize2 ----
									lbllblPSize2.setText("PIPE SIZE (S X D) (mm)");
									lbllblPSize2.setFont(new Font("Arial", Font.BOLD, 12));
									lbllblPSize2.setName("label");
									pnlTop2.add(lbllblPSize2, new TableLayoutConstraints(7, 1, 7, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblPSz2 ----
									lblPSz2.setText("psize");
									lblPSz2.setFont(new Font("Arial", Font.PLAIN, 12));
									lblPSz2.setName("declared");
									pnlTop2.add(lblPSz2, new TableLayoutConstraints(8, 1, 8, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label42 ----
									label42.setText("PUMP SNO.");
									label42.setFont(new Font("Arial", Font.BOLD, 12));
									label42.setName("label");
									pnlTop2.add(label42, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblPSno2 ----
									lblPSno2.setText("pslno");
									lblPSno2.setFont(new Font("Arial", Font.PLAIN, 12));
									lblPSno2.setName("declared");
									pnlTop2.add(lblPSno2, new TableLayoutConstraints(2, 2, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label36 ----
									label36.setText("VOLTAGE (V)");
									label36.setFont(new Font("Arial", Font.BOLD, 12));
									label36.setName("label");
									pnlTop2.add(label36, new TableLayoutConstraints(3, 2, 3, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblV2 ----
									lblV2.setText("v");
									lblV2.setFont(new Font("Arial", Font.PLAIN, 12));
									lblV2.setName("declared");
									pnlTop2.add(lblV2, new TableLayoutConstraints(4, 2, 4, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label194 ----
									label194.setText("SPEED (rpm)");
									label194.setFont(new Font("Arial", Font.BOLD, 12));
									label194.setName("label");
									pnlTop2.add(label194, new TableLayoutConstraints(5, 2, 5, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblSpeed2 ----
									lblSpeed2.setText("speed");
									lblSpeed2.setFont(new Font("Arial", Font.PLAIN, 12));
									lblSpeed2.setName("declared");
									pnlTop2.add(lblSpeed2, new TableLayoutConstraints(6, 2, 6, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblHdLbl2 ----
									lblHdLbl2.setText("HEAD (m)");
									lblHdLbl2.setFont(new Font("Arial", Font.BOLD, 12));
									lblHdLbl2.setName("label");
									pnlTop2.add(lblHdLbl2, new TableLayoutConstraints(7, 2, 7, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblHead2 ----
									lblHead2.setText("head");
									lblHead2.setFont(new Font("Arial", Font.PLAIN, 12));
									lblHead2.setName("declared");
									pnlTop2.add(lblHead2, new TableLayoutConstraints(8, 2, 8, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label34 ----
									label34.setText("PHASE");
									label34.setFont(new Font("Arial", Font.BOLD, 12));
									label34.setName("label");
									pnlTop2.add(label34, new TableLayoutConstraints(1, 3, 1, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblPh2 ----
									lblPh2.setText("ph");
									lblPh2.setFont(new Font("Arial", Font.PLAIN, 12));
									lblPh2.setName("declared");
									pnlTop2.add(lblPh2, new TableLayoutConstraints(2, 3, 2, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label38 ----
									label38.setText("MAX. CURRENT (A)");
									label38.setFont(new Font("Arial", Font.BOLD, 12));
									label38.setName("label");
									pnlTop2.add(label38, new TableLayoutConstraints(3, 3, 3, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblCur2 ----
									lblCur2.setText("a");
									lblCur2.setFont(new Font("Arial", Font.PLAIN, 12));
									lblCur2.setName("declared");
									pnlTop2.add(lblCur2, new TableLayoutConstraints(4, 3, 4, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label203 ----
									label203.setText("NO. OF POLES");
									label203.setFont(new Font("Arial", Font.BOLD, 12));
									label203.setName("label");
									pnlTop2.add(label203, new TableLayoutConstraints(5, 3, 5, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblPoles2 ----
									lblPoles2.setText("poles");
									lblPoles2.setFont(new Font("Arial", Font.PLAIN, 12));
									lblPoles2.setName("declared");
									pnlTop2.add(lblPoles2, new TableLayoutConstraints(6, 3, 6, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblFlowRtLabel ----
									lblFlowRtLabel.setText("DISCHARGE (unit)");
									lblFlowRtLabel.setFont(new Font("Arial", Font.BOLD, 12));
									lblFlowRtLabel.setName("label");
									pnlTop2.add(lblFlowRtLabel, new TableLayoutConstraints(7, 3, 7, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblFlow2 ----
									lblFlow2.setText("flow");
									lblFlow2.setFont(new Font("Arial", Font.PLAIN, 12));
									lblFlow2.setName("declared");
									pnlTop2.add(lblFlow2, new TableLayoutConstraints(8, 3, 8, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label51 ----
									label51.setText("CONNECTION");
									label51.setFont(new Font("Arial", Font.BOLD, 12));
									label51.setName("label");
									pnlTop2.add(label51, new TableLayoutConstraints(1, 4, 1, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblCon2 ----
									lblCon2.setText("con");
									lblCon2.setFont(new Font("Arial", Font.PLAIN, 12));
									lblCon2.setName("declared");
									pnlTop2.add(lblCon2, new TableLayoutConstraints(2, 4, 2, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lbllblMI2 ----
									lbllblMI2.setText("MOTOR IP (kW)");
									lbllblMI2.setFont(new Font("Arial", Font.BOLD, 12));
									lbllblMI2.setName("label");
									pnlTop2.add(lbllblMI2, new TableLayoutConstraints(3, 4, 3, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblMI2 ----
									lblMI2.setText("mi");
									lblMI2.setFont(new Font("Arial", Font.PLAIN, 12));
									lblMI2.setName("declared");
									pnlTop2.add(lblMI2, new TableLayoutConstraints(4, 4, 4, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lbllblGDis2 ----
									lbllblGDis2.setText("GAUGE DIST. (m)");
									lbllblGDis2.setFont(new Font("Arial", Font.BOLD, 12));
									lbllblGDis2.setName("label");
									pnlTop2.add(lbllblGDis2, new TableLayoutConstraints(5, 4, 5, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblGDis2 ----
									lblGDis2.setText("gd");
									lblGDis2.setFont(new Font("Arial", Font.PLAIN, 12));
									lblGDis2.setName("declared");
									pnlTop2.add(lblGDis2, new TableLayoutConstraints(6, 4, 6, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblHdRgLbl2 ----
									lblHdRgLbl2.setText("HEAD RANGE (m)");
									lblHdRgLbl2.setFont(new Font("Arial", Font.BOLD, 12));
									lblHdRgLbl2.setName("label");
									pnlTop2.add(lblHdRgLbl2, new TableLayoutConstraints(7, 4, 7, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblHeadR2 ----
									lblHeadR2.setText("headrange");
									lblHeadR2.setFont(new Font("Arial", Font.PLAIN, 12));
									lblHeadR2.setName("declared");
									pnlTop2.add(lblHeadR2, new TableLayoutConstraints(8, 4, 8, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lbllblMotType2 ----
									lbllblMotType2.setText("MOTOR TYPE");
									lbllblMotType2.setFont(new Font("Arial", Font.BOLD, 12));
									lbllblMotType2.setName("label");
									pnlTop2.add(lbllblMotType2, new TableLayoutConstraints(1, 5, 1, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblMotType2 ----
									lblMotType2.setText("type");
									lblMotType2.setFont(new Font("Arial", Font.PLAIN, 12));
									lblMotType2.setName("declared");
									pnlTop2.add(lblMotType2, new TableLayoutConstraints(2, 5, 2, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lbllblMSno2 ----
									lbllblMSno2.setText("MOTOR SNO.");
									lbllblMSno2.setFont(new Font("Arial", Font.BOLD, 12));
									lbllblMSno2.setName("label");
									pnlTop2.add(lbllblMSno2, new TableLayoutConstraints(3, 5, 3, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblMSno2 ----
									lblMSno2.setText("mslno");
									lblMSno2.setFont(new Font("Arial", Font.PLAIN, 12));
									lblMSno2.setName("declared");
									pnlTop2.add(lblMSno2, new TableLayoutConstraints(4, 5, 4, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lbllblMotEff2 ----
									lbllblMotEff2.setText("MOTOR EFF. (%)");
									lbllblMotEff2.setFont(new Font("Arial", Font.BOLD, 12));
									lbllblMotEff2.setName("label");
									pnlTop2.add(lbllblMotEff2, new TableLayoutConstraints(5, 5, 5, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblMotEff2 ----
									lblMotEff2.setText("eff");
									lblMotEff2.setFont(new Font("Arial", Font.PLAIN, 12));
									lblMotEff2.setName("declared");
									pnlTop2.add(lblMotEff2, new TableLayoutConstraints(6, 5, 6, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lbllblOE2 ----
									lbllblOE2.setText("OVERALL EFF. (%)");
									lbllblOE2.setFont(new Font("Arial", Font.BOLD, 12));
									lbllblOE2.setName("label");
									pnlTop2.add(lbllblOE2, new TableLayoutConstraints(7, 5, 7, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblOEff2 ----
									lblOEff2.setText("eff");
									lblOEff2.setFont(new Font("Arial", Font.PLAIN, 12));
									lblOEff2.setName("declared");
									pnlTop2.add(lblOEff2, new TableLayoutConstraints(8, 5, 8, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lbllblIns2 ----
									lbllblIns2.setText("INSUL. CLASS");
									lbllblIns2.setFont(new Font("Arial", Font.BOLD, 12));
									lbllblIns2.setName("label");
									pnlTop2.add(lbllblIns2, new TableLayoutConstraints(1, 6, 1, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblIns2 ----
									lblIns2.setText("ins");
									lblIns2.setFont(new Font("Arial", Font.PLAIN, 12));
									lblIns2.setName("declared");
									pnlTop2.add(lblIns2, new TableLayoutConstraints(2, 6, 2, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lbllblPipeConst2 ----
									lbllblPipeConst2.setText("PIPE CONST. (m)");
									lbllblPipeConst2.setFont(new Font("Arial", Font.BOLD, 12));
									lbllblPipeConst2.setName("label");
									pnlTop2.add(lbllblPipeConst2, new TableLayoutConstraints(3, 6, 3, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblPipeConst2 ----
									lblPipeConst2.setText("pc");
									lblPipeConst2.setFont(new Font("Arial", Font.PLAIN, 12));
									lblPipeConst2.setName("declared");
									pnlTop2.add(lblPipeConst2, new TableLayoutConstraints(4, 6, 4, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lbllblSPTime2 ----
									lbllblSPTime2.setText("SELF PRIM. TIME (sec)");
									lbllblSPTime2.setFont(new Font("Arial", Font.BOLD, 12));
									lbllblSPTime2.setName("label");
									pnlTop2.add(lbllblSPTime2, new TableLayoutConstraints(5, 6, 5, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblSPTime2 ----
									lblSPTime2.setText("sp");
									lblSPTime2.setFont(new Font("Arial", Font.PLAIN, 12));
									lblSPTime2.setName("declared");
									pnlTop2.add(lblSPTime2, new TableLayoutConstraints(6, 6, 6, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lbllblBrSz2 ----
									lbllblBrSz2.setText("MIN BORE SZ. (mm)");
									lbllblBrSz2.setFont(new Font("Arial", Font.BOLD, 12));
									lbllblBrSz2.setName("label");
									pnlTop2.add(lbllblBrSz2, new TableLayoutConstraints(7, 6, 7, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblBrSz2 ----
									lblBrSz2.setText("br");
									lblBrSz2.setFont(new Font("Arial", Font.PLAIN, 12));
									lblBrSz2.setName("declared");
									pnlTop2.add(lblBrSz2, new TableLayoutConstraints(8, 6, 8, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lbllblCapRat2 ----
									lbllblCapRat2.setText("CAP. RATING (uF)");
									lbllblCapRat2.setFont(new Font("Arial", Font.BOLD, 12));
									lbllblCapRat2.setName("label");
									pnlTop2.add(lbllblCapRat2, new TableLayoutConstraints(1, 7, 1, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblCapRate2 ----
									lblCapRate2.setText("caprate");
									lblCapRate2.setFont(new Font("Arial", Font.PLAIN, 12));
									lblCapRate2.setName("declared");
									pnlTop2.add(lblCapRate2, new TableLayoutConstraints(2, 7, 2, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lbllblCapVolt2 ----
									lbllblCapVolt2.setText("CAP. VOLTAGE (V)");
									lbllblCapVolt2.setFont(new Font("Arial", Font.BOLD, 12));
									lbllblCapVolt2.setName("label");
									pnlTop2.add(lbllblCapVolt2, new TableLayoutConstraints(3, 7, 3, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblCapVolt2 ----
									lblCapVolt2.setText("capv");
									lblCapVolt2.setFont(new Font("Arial", Font.PLAIN, 12));
									lblCapVolt2.setName("declared");
									pnlTop2.add(lblCapVolt2, new TableLayoutConstraints(4, 7, 4, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lbllblNoOfStg2 ----
									lbllblNoOfStg2.setText("NO. OF STAGES");
									lbllblNoOfStg2.setFont(new Font("Arial", Font.BOLD, 12));
									lbllblNoOfStg2.setName("label");
									pnlTop2.add(lbllblNoOfStg2, new TableLayoutConstraints(5, 7, 5, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblNoStg2 ----
									lblNoStg2.setText("stz");
									lblNoStg2.setFont(new Font("Arial", Font.PLAIN, 12));
									lblNoStg2.setName("declared");
									pnlTop2.add(lblNoStg2, new TableLayoutConstraints(6, 7, 6, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
								}
								pnlRep2.add(pnlTop2, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//======== pnlPrime2 ========
								{
									pnlPrime2.setBackground(Color.black);
									pnlPrime2.setAutoscrolls(true);
									pnlPrime2.setName("outline");
									pnlPrime2.setLayout(new TableLayout(new double[][] {
										{34, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL},
										{20, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL}}));
									((TableLayout)pnlPrime2.getLayout()).setHGap(2);
									((TableLayout)pnlPrime2.getLayout()).setVGap(1);

									//---- label143 ----
									label143.setText("<html>Test<br>SNo.</html>");
									label143.setBackground(Color.white);
									label143.setHorizontalAlignment(SwingConstants.CENTER);
									label143.setFont(new Font("Arial", Font.BOLD, 11));
									label143.setOpaque(true);
									label143.setName("label");
									pnlPrime2.add(label143, new TableLayoutConstraints(0, 0, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label144 ----
									label144.setText("Freq.");
									label144.setBackground(Color.white);
									label144.setHorizontalAlignment(SwingConstants.CENTER);
									label144.setFont(new Font("Arial", Font.BOLD, 11));
									label144.setOpaque(true);
									label144.setName("label");
									pnlPrime2.add(label144, new TableLayoutConstraints(1, 0, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label127 ----
									label127.setText("Speed");
									label127.setOpaque(true);
									label127.setBackground(Color.white);
									label127.setFont(new Font("Arial", Font.BOLD, 11));
									label127.setHorizontalAlignment(SwingConstants.CENTER);
									label127.setName("label");
									pnlPrime2.add(label127, new TableLayoutConstraints(2, 0, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lbllblHead1 ----
									lbllblHead1.setText("HEAD");
									lbllblHead1.setBackground(Color.white);
									lbllblHead1.setOpaque(true);
									lbllblHead1.setFont(new Font("Arial", Font.BOLD, 11));
									lbllblHead1.setHorizontalAlignment(SwingConstants.CENTER);
									lbllblHead1.setName("label");
									pnlPrime2.add(lbllblHead1, new TableLayoutConstraints(3, 0, 6, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label133 ----
									label133.setText("FLOW");
									label133.setBackground(Color.white);
									label133.setHorizontalAlignment(SwingConstants.CENTER);
									label133.setFont(new Font("Arial", Font.BOLD, 11));
									label133.setOpaque(true);
									label133.setAutoscrolls(true);
									label133.setName("label");
									pnlPrime2.add(label133, new TableLayoutConstraints(7, 0, 7, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label135 ----
									label135.setText("MOTOR POWER INPUTS");
									label135.setBackground(Color.white);
									label135.setHorizontalAlignment(SwingConstants.CENTER);
									label135.setFont(new Font("Arial", Font.BOLD, 11));
									label135.setOpaque(true);
									label135.setAutoscrolls(true);
									label135.setName("label");
									pnlPrime2.add(label135, new TableLayoutConstraints(8, 0, 10, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblTitSHd2 ----
									lblTitSHd2.setText("<html><body align=\"center\">Suction Head</body></html>");
									lblTitSHd2.setBackground(Color.white);
									lblTitSHd2.setHorizontalAlignment(SwingConstants.CENTER);
									lblTitSHd2.setFont(new Font("Arial", Font.BOLD, 11));
									lblTitSHd2.setOpaque(true);
									lblTitSHd2.setName("label");
									pnlPrime2.add(lblTitSHd2, new TableLayoutConstraints(3, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblRem2 ----
									lblRem2.setText("Remark");
									lblRem2.setBackground(Color.white);
									lblRem2.setFont(new Font("Arial", Font.BOLD, 10));
									lblRem2.setOpaque(true);
									lblRem2.setHorizontalAlignment(SwingConstants.CENTER);
									lblRem2.setName("label");
									pnlPrime2.add(lblRem2, new TableLayoutConstraints(14, 0, 14, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblPerfHz2 ----
									lblPerfHz2.setText("Performance At Rated Freq X Hz");
									lblPerfHz2.setBackground(Color.white);
									lblPerfHz2.setHorizontalAlignment(SwingConstants.CENTER);
									lblPerfHz2.setFont(new Font("Arial", Font.BOLD, 11));
									lblPerfHz2.setOpaque(true);
									lblPerfHz2.setAutoscrolls(true);
									lblPerfHz2.setName("actual");
									pnlPrime2.add(lblPerfHz2, new TableLayoutConstraints(11, 0, 13, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label335 ----
									label335.setText("<html><body align=\"center\">Suction Gauge Reading</body></html>");
									label335.setBackground(Color.white);
									label335.setHorizontalAlignment(SwingConstants.CENTER);
									label335.setFont(new Font("Arial", Font.BOLD, 11));
									label335.setOpaque(true);
									label335.setBorder(null);
									pnlPrime2.add(label335, new TableLayoutConstraints(3, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label336 ----
									label336.setText("<html><body align=\"center\">Delivery Head</body></html>");
									label336.setBackground(Color.white);
									label336.setHorizontalAlignment(SwingConstants.CENTER);
									label336.setFont(new Font("Arial", Font.BOLD, 11));
									label336.setOpaque(true);
									label336.setName("label");
									pnlPrime2.add(label336, new TableLayoutConstraints(4, 1, 4, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblTitVHC2 ----
									lblTitVHC2.setText("<html><body align=\"center\">Velocity Head Corr.</body></html>");
									lblTitVHC2.setBackground(Color.white);
									lblTitVHC2.setHorizontalAlignment(SwingConstants.CENTER);
									lblTitVHC2.setFont(new Font("Arial", Font.BOLD, 11));
									lblTitVHC2.setOpaque(true);
									lblTitVHC2.setName("label");
									pnlPrime2.add(lblTitVHC2, new TableLayoutConstraints(5, 1, 5, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblTitTotHd2 ----
									lblTitTotHd2.setText("<html><body align=\"center\">Total Head</body></html>");
									lblTitTotHd2.setBackground(Color.white);
									lblTitTotHd2.setHorizontalAlignment(SwingConstants.CENTER);
									lblTitTotHd2.setFont(new Font("Arial", Font.BOLD, 11));
									lblTitTotHd2.setOpaque(true);
									lblTitTotHd2.setName("label");
									pnlPrime2.add(lblTitTotHd2, new TableLayoutConstraints(6, 1, 6, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label156 ----
									label156.setText("<html><body align=\"center\">Flow</body></html>");
									label156.setBackground(Color.white);
									label156.setHorizontalAlignment(SwingConstants.CENTER);
									label156.setFont(new Font("Arial", Font.BOLD, 11));
									label156.setOpaque(true);
									label156.setAutoscrolls(true);
									label156.setName("label");
									pnlPrime2.add(label156, new TableLayoutConstraints(7, 1, 7, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label136 ----
									label136.setText("Voltage");
									label136.setBackground(Color.white);
									label136.setHorizontalAlignment(SwingConstants.CENTER);
									label136.setFont(new Font("Arial", Font.BOLD, 11));
									label136.setOpaque(true);
									label136.setAutoscrolls(true);
									label136.setName("label");
									pnlPrime2.add(label136, new TableLayoutConstraints(8, 1, 8, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label141 ----
									label141.setText("Current");
									label141.setBackground(Color.white);
									label141.setHorizontalAlignment(SwingConstants.CENTER);
									label141.setFont(new Font("Arial", Font.BOLD, 11));
									label141.setOpaque(true);
									label141.setAutoscrolls(true);
									label141.setName("label");
									pnlPrime2.add(label141, new TableLayoutConstraints(9, 1, 9, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label142 ----
									label142.setText("Power");
									label142.setBackground(Color.white);
									label142.setHorizontalAlignment(SwingConstants.CENTER);
									label142.setFont(new Font("Arial", Font.BOLD, 11));
									label142.setOpaque(true);
									label142.setAutoscrolls(true);
									label142.setName("label");
									pnlPrime2.add(label142, new TableLayoutConstraints(10, 1, 10, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblTitFlow2 ----
									lblTitFlow2.setText("Flow");
									lblTitFlow2.setBackground(Color.white);
									lblTitFlow2.setHorizontalAlignment(SwingConstants.CENTER);
									lblTitFlow2.setFont(new Font("Arial", Font.BOLD, 11));
									lblTitFlow2.setOpaque(true);
									lblTitFlow2.setAutoscrolls(true);
									lblTitFlow2.setName("label");
									pnlPrime2.add(lblTitFlow2, new TableLayoutConstraints(11, 1, 11, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblTitTotRtHd2 ----
									lblTitTotRtHd2.setText("<html><body align=\"center\">Total Head</body></html>");
									lblTitTotRtHd2.setBackground(Color.white);
									lblTitTotRtHd2.setHorizontalAlignment(SwingConstants.CENTER);
									lblTitTotRtHd2.setFont(new Font("Arial", Font.BOLD, 11));
									lblTitTotRtHd2.setOpaque(true);
									lblTitTotRtHd2.setAutoscrolls(true);
									lblTitTotRtHd2.setName("label");
									pnlPrime2.add(lblTitTotRtHd2, new TableLayoutConstraints(12, 1, 12, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblTitMI2 ----
									lblTitMI2.setText("<html><body align=\"center\">Motor<br>Input</body></html>");
									lblTitMI2.setBackground(Color.white);
									lblTitMI2.setHorizontalAlignment(SwingConstants.CENTER);
									lblTitMI2.setFont(new Font("Arial", Font.BOLD, 11));
									lblTitMI2.setOpaque(true);
									lblTitMI2.setAutoscrolls(true);
									lblTitMI2.setName("label");
									pnlPrime2.add(lblTitMI2, new TableLayoutConstraints(13, 1, 13, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label173 ----
									label173.setText("Hz");
									label173.setBackground(Color.white);
									label173.setHorizontalAlignment(SwingConstants.CENTER);
									label173.setFont(new Font("Arial", Font.BOLD, 11));
									label173.setOpaque(true);
									label173.setAutoscrolls(true);
									label173.setName("label");
									pnlPrime2.add(label173, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label153 ----
									label153.setText("rpm");
									label153.setOpaque(true);
									label153.setBackground(Color.white);
									label153.setFont(new Font("Arial", Font.BOLD, 11));
									label153.setHorizontalAlignment(SwingConstants.CENTER);
									label153.setName("label");
									pnlPrime2.add(label153, new TableLayoutConstraints(2, 2, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblSHd2 ----
									lblSHd2.setText("mwc");
									lblSHd2.setBackground(Color.white);
									lblSHd2.setHorizontalAlignment(SwingConstants.CENTER);
									lblSHd2.setFont(new Font("Arial", Font.BOLD, 11));
									lblSHd2.setOpaque(true);
									lblSHd2.setAutoscrolls(true);
									lblSHd2.setName("label");
									pnlPrime2.add(lblSHd2, new TableLayoutConstraints(3, 2, 3, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblDHd2 ----
									lblDHd2.setText("mwc");
									lblDHd2.setBackground(Color.white);
									lblDHd2.setHorizontalAlignment(SwingConstants.CENTER);
									lblDHd2.setFont(new Font("Arial", Font.BOLD, 11));
									lblDHd2.setOpaque(true);
									lblDHd2.setAutoscrolls(true);
									lblDHd2.setName("label");
									pnlPrime2.add(lblDHd2, new TableLayoutConstraints(4, 2, 4, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblVHd2 ----
									lblVHd2.setText("mtrs");
									lblVHd2.setBackground(Color.white);
									lblVHd2.setHorizontalAlignment(SwingConstants.CENTER);
									lblVHd2.setFont(new Font("Arial", Font.BOLD, 11));
									lblVHd2.setOpaque(true);
									lblVHd2.setAutoscrolls(true);
									lblVHd2.setName("label");
									pnlPrime2.add(lblVHd2, new TableLayoutConstraints(5, 2, 5, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblTotHd2 ----
									lblTotHd2.setText("mtrs");
									lblTotHd2.setBackground(Color.white);
									lblTotHd2.setHorizontalAlignment(SwingConstants.CENTER);
									lblTotHd2.setFont(new Font("Arial", Font.BOLD, 11));
									lblTotHd2.setOpaque(true);
									lblTotHd2.setAutoscrolls(true);
									lblTotHd2.setName("label");
									pnlPrime2.add(lblTotHd2, new TableLayoutConstraints(6, 2, 6, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblFlowUnitS2 ----
									lblFlowUnitS2.setText("unit");
									lblFlowUnitS2.setBackground(Color.white);
									lblFlowUnitS2.setHorizontalAlignment(SwingConstants.CENTER);
									lblFlowUnitS2.setFont(new Font("Arial", Font.BOLD, 11));
									lblFlowUnitS2.setOpaque(true);
									lblFlowUnitS2.setAutoscrolls(true);
									lblFlowUnitS2.setName("label");
									pnlPrime2.add(lblFlowUnitS2, new TableLayoutConstraints(7, 2, 7, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label145 ----
									label145.setText("V");
									label145.setBackground(Color.white);
									label145.setHorizontalAlignment(SwingConstants.CENTER);
									label145.setFont(new Font("Arial", Font.BOLD, 11));
									label145.setOpaque(true);
									label145.setAutoscrolls(true);
									label145.setName("label");
									pnlPrime2.add(label145, new TableLayoutConstraints(8, 2, 8, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label146 ----
									label146.setText("A");
									label146.setBackground(Color.white);
									label146.setHorizontalAlignment(SwingConstants.CENTER);
									label146.setFont(new Font("Arial", Font.BOLD, 11));
									label146.setOpaque(true);
									label146.setAutoscrolls(true);
									label146.setName("label");
									pnlPrime2.add(label146, new TableLayoutConstraints(9, 2, 9, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label147 ----
									label147.setText("kW");
									label147.setBackground(Color.white);
									label147.setHorizontalAlignment(SwingConstants.CENTER);
									label147.setFont(new Font("Arial", Font.BOLD, 11));
									label147.setOpaque(true);
									label147.setAutoscrolls(true);
									label147.setName("label");
									pnlPrime2.add(label147, new TableLayoutConstraints(10, 2, 10, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblFlowUnitT2 ----
									lblFlowUnitT2.setText("unit");
									lblFlowUnitT2.setBackground(Color.white);
									lblFlowUnitT2.setHorizontalAlignment(SwingConstants.CENTER);
									lblFlowUnitT2.setFont(new Font("Arial", Font.BOLD, 11));
									lblFlowUnitT2.setOpaque(true);
									lblFlowUnitT2.setAutoscrolls(true);
									lblFlowUnitT2.setName("label");
									pnlPrime2.add(lblFlowUnitT2, new TableLayoutConstraints(11, 2, 11, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblTotHdT2 ----
									lblTotHdT2.setText("mtrs");
									lblTotHdT2.setBackground(Color.white);
									lblTotHdT2.setHorizontalAlignment(SwingConstants.CENTER);
									lblTotHdT2.setFont(new Font("Arial", Font.BOLD, 11));
									lblTotHdT2.setOpaque(true);
									lblTotHdT2.setAutoscrolls(true);
									lblTotHdT2.setName("label");
									pnlPrime2.add(lblTotHdT2, new TableLayoutConstraints(12, 2, 12, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblMITitUnit2 ----
									lblMITitUnit2.setText("kW");
									lblMITitUnit2.setBackground(Color.white);
									lblMITitUnit2.setHorizontalAlignment(SwingConstants.CENTER);
									lblMITitUnit2.setFont(new Font("Arial", Font.BOLD, 11));
									lblMITitUnit2.setOpaque(true);
									lblMITitUnit2.setAutoscrolls(true);
									lblMITitUnit2.setName("label");
									pnlPrime2.add(lblMITitUnit2, new TableLayoutConstraints(13, 2, 13, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- tblRes2 ----
									tblRes2.setModel(new DefaultTableModel(
										new Object[][] {
											{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
											{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
											{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
											{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
											{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
											{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
											{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
											{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
											{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
											{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
										},
										new String[] {
											null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
										}
									) {
										Class<?>[] columnTypes = new Class<?>[] {
											String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class
										};
										@Override
										public Class<?> getColumnClass(int columnIndex) {
											return columnTypes[columnIndex];
										}
									});
									{
										TableColumnModel cm = tblRes2.getColumnModel();
										cm.getColumn(0).setMinWidth(35);
										cm.getColumn(0).setMaxWidth(35);
										cm.getColumn(0).setPreferredWidth(35);
									}
									tblRes2.setFont(new Font("Arial", Font.PLAIN, 12));
									tblRes2.setRowHeight(25);
									tblRes2.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
									tblRes2.setGridColor(Color.lightGray);
									tblRes2.setRowSelectionAllowed(false);
									tblRes2.setAutoscrolls(false);
									tblRes2.setFocusable(false);
									tblRes2.setEnabled(false);
									tblRes2.setIntercellSpacing(new Dimension(3, 1));
									tblRes2.setName("outline");
									pnlPrime2.add(tblRes2, new TableLayoutConstraints(0, 3, 14, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
								}
								pnlRep2.add(pnlPrime2, new TableLayoutConstraints(0, 2, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//======== pnlBot2 ========
								{
									pnlBot2.setBackground(Color.white);
									pnlBot2.setName("outline");
									pnlBot2.setLayout(new TableLayout(new double[][] {
										{5, 35, 320, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL},
										{5, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5}}));
									((TableLayout)pnlBot2.getLayout()).setHGap(5);
									((TableLayout)pnlBot2.getLayout()).setVGap(5);

									//---- lblCas2 ----
									lblCas2.setText("Hydrostatic Pressure Test:");
									lblCas2.setFont(new Font("Arial", Font.BOLD, 12));
									lblCas2.setName("label");
									pnlBot2.add(lblCas2, new TableLayoutConstraints(1, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblGC2 ----
									lblGC2.setText("               Note:");
									lblGC2.setFont(new Font("Arial", Font.BOLD, 12));
									lblGC2.setName("label");
									pnlBot2.add(lblGC2, new TableLayoutConstraints(3, 1, 5, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblMaxDis2 ----
									lblMaxDis2.setText("Maximum Discharge Pressure");
									lblMaxDis2.setFont(new Font("Arial", Font.PLAIN, 12));
									lblMaxDis2.setName("actual");
									pnlBot2.add(lblMaxDis2, new TableLayoutConstraints(1, 2, 3, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblGCText2 ----
									lblGCText2.setText("               Your note goes here");
									lblGCText2.setFont(new Font("Arial", Font.PLAIN, 12));
									lblGCText2.setName("actual");
									pnlBot2.add(lblGCText2, new TableLayoutConstraints(3, 2, 5, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblWith2 ----
									lblWith2.setText("Withstood this pressure by 2 minutes.");
									lblWith2.setFont(new Font("Arial", Font.PLAIN, 12));
									lblWith2.setName("actual");
									pnlBot2.add(lblWith2, new TableLayoutConstraints(1, 3, 2, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblDt2 ----
									lblDt2.setText("Dt");
									lblDt2.setFont(new Font("Arial", Font.PLAIN, 12));
									lblDt2.setName("label");
									pnlBot2.add(lblDt2, new TableLayoutConstraints(2, 5, 2, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblTestedBy2 ----
									lblTestedBy2.setText("Tested By");
									lblTestedBy2.setFont(new Font("Arial", Font.PLAIN, 13));
									lblTestedBy2.setHorizontalAlignment(SwingConstants.CENTER);
									lblTestedBy2.setName("label");
									pnlBot2.add(lblTestedBy2, new TableLayoutConstraints(3, 5, 3, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblVerBy13 ----
									lblVerBy13.setText("Verified By");
									lblVerBy13.setFont(new Font("Arial", Font.PLAIN, 13));
									lblVerBy13.setHorizontalAlignment(SwingConstants.CENTER);
									lblVerBy13.setName("label");
									pnlBot2.add(lblVerBy13, new TableLayoutConstraints(4, 5, 4, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label23 ----
									label23.setText("Approved By");
									label23.setFont(new Font("Arial", Font.PLAIN, 13));
									label23.setHorizontalAlignment(SwingConstants.CENTER);
									label23.setName("label");
									pnlBot2.add(label23, new TableLayoutConstraints(5, 5, 5, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lbllblDt2 ----
									lbllblDt2.setText("Date:");
									lbllblDt2.setFont(new Font("Arial", Font.BOLD, 13));
									lbllblDt2.setHorizontalAlignment(SwingConstants.CENTER);
									lbllblDt2.setName("label");
									pnlBot2.add(lbllblDt2, new TableLayoutConstraints(1, 5, 1, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
								}
								pnlRep2.add(pnlBot2, new TableLayoutConstraints(0, 5, 0, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//======== pnlOptTest2 ========
								{
									pnlOptTest2.setBackground(Color.white);
									pnlOptTest2.setLayout(new TableLayout(new double[][] {
										{5, TableLayout.PREFERRED},
										{5, TableLayout.PREFERRED, TableLayout.PREFERRED}}));
									((TableLayout)pnlOptTest2.getLayout()).setHGap(5);
									((TableLayout)pnlOptTest2.getLayout()).setVGap(5);

									//---- lbllblSP2 ----
									lbllblSP2.setText("Self Priming Test:");
									lbllblSP2.setFont(new Font("Arial", Font.BOLD, 12));
									lbllblSP2.setName("label");
									pnlOptTest2.add(lbllblSP2, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblSP2 ----
									lblSP2.setFont(new Font("Arial", Font.PLAIN, 12));
									lblSP2.setName("actual");
									pnlOptTest2.add(lblSP2, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
								}
								pnlRep2.add(pnlOptTest2, new TableLayoutConstraints(0, 4, 0, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
							}
							pnlPrint2.add(pnlRep2, new TableLayoutConstraints(1, 8, 3, 8, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

							//---- lblAppVer2 ----
							lblAppVer2.setText("Doer PumpViewPro");
							lblAppVer2.setFont(new Font("Arial", Font.PLAIN, 10));
							lblAppVer2.setHorizontalAlignment(SwingConstants.RIGHT);
							lblAppVer2.setForeground(Color.gray);
							pnlPrint2.add(lblAppVer2, new TableLayoutConstraints(2, 9, 3, 9, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
						}
						printArea2.add(pnlPrint2, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
					}
					scrollPane2.setViewportView(printArea2);
				}
				tabReport.addTab("2. Performance Report", scrollPane2);

				//======== scrollPane3 ========
				{

					//======== printArea3 ========
					{
						printArea3.setBorder(new LineBorder(Color.blue, 2));
						printArea3.setBackground(Color.white);
						printArea3.setLayout(new TableLayout(new double[][] {
							{900},
							{TableLayout.PREFERRED, 1450}}));

						//---- lblRtWarn3 ----
						lblRtWarn3.setText("For routine tests, performance graph and result dervied from graph may not be reliable due to less number of test heads");
						lblRtWarn3.setFont(new Font("Arial", Font.PLAIN, 12));
						lblRtWarn3.setHorizontalAlignment(SwingConstants.CENTER);
						lblRtWarn3.setBorder(new LineBorder(Color.orange, 2, true));
						lblRtWarn3.setOpaque(true);
						lblRtWarn3.setBackground(Color.yellow);
						lblRtWarn3.setForeground(Color.red);
						printArea3.add(lblRtWarn3, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//======== pnlPrint3 ========
						{
							pnlPrint3.setBorder(null);
							pnlPrint3.setBackground(Color.white);
							pnlPrint3.setLayout(new TableLayout(new double[][] {
								{40, 120, TableLayout.FILL, TableLayout.FILL, 120, 15},
								{35, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 500, TableLayout.PREFERRED, 70, TableLayout.FILL, TableLayout.PREFERRED, TableLayout.PREFERRED}}));

							//---- lblIS3 ----
							lblIS3.setText("IS:");
							lblIS3.setFont(new Font("Arial", Font.BOLD, 10));
							lblIS3.setHorizontalAlignment(SwingConstants.CENTER);
							lblIS3.setName("label");
							pnlPrint3.add(lblIS3, new TableLayoutConstraints(4, 1, 4, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

							//---- lblCustLogo3 ----
							lblCustLogo3.setHorizontalAlignment(SwingConstants.CENTER);
							pnlPrint3.add(lblCustLogo3, new TableLayoutConstraints(1, 1, 1, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

							//---- lblCompName3 ----
							lblCompName3.setText("Company Name");
							lblCompName3.setFont(new Font("Arial", Font.BOLD, 16));
							lblCompName3.setHorizontalAlignment(SwingConstants.CENTER);
							lblCompName3.setName("header");
							pnlPrint3.add(lblCompName3, new TableLayoutConstraints(2, 1, 3, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

							//---- lblISILogo3 ----
							lblISILogo3.setIcon(new ImageIcon(getClass().getResource("/img/isi_logo.PNG")));
							lblISILogo3.setVerticalAlignment(SwingConstants.TOP);
							lblISILogo3.setHorizontalAlignment(SwingConstants.CENTER);
							lblISILogo3.setIconTextGap(0);
							pnlPrint3.add(lblISILogo3, new TableLayoutConstraints(4, 2, 4, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

							//---- lblCompAdr3 ----
							lblCompAdr3.setText("Address Line 1 and Line 2");
							lblCompAdr3.setFont(new Font("Arial", Font.BOLD, 12));
							lblCompAdr3.setHorizontalAlignment(SwingConstants.CENTER);
							lblCompAdr3.setName("header");
							pnlPrint3.add(lblCompAdr3, new TableLayoutConstraints(2, 3, 3, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

							//---- lblTitle3 ----
							lblTitle3.setText("PUMP TEST GRAPH");
							lblTitle3.setFont(new Font("Arial", Font.BOLD, 14));
							lblTitle3.setHorizontalAlignment(SwingConstants.CENTER);
							lblTitle3.setName("header");
							pnlPrint3.add(lblTitle3, new TableLayoutConstraints(2, 5, 3, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

							//---- lblISIRefNo3 ----
							lblISIRefNo3.setText("ISI Ref No.");
							lblISIRefNo3.setHorizontalAlignment(SwingConstants.CENTER);
							lblISIRefNo3.setFont(new Font("Arial", Font.BOLD, 10));
							lblISIRefNo3.setIconTextGap(0);
							lblISIRefNo3.setName("label");
							pnlPrint3.add(lblISIRefNo3, new TableLayoutConstraints(4, 4, 4, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

							//---- lblPage3 ----
							lblPage3.setText("Page 1 of 1");
							lblPage3.setFont(new Font("Arial", Font.PLAIN, 12));
							lblPage3.setHorizontalAlignment(SwingConstants.RIGHT);
							lblPage3.setName("label");
							pnlPrint3.add(lblPage3, new TableLayoutConstraints(4, 7, 4, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

							//======== pnlGraph3 ========
							{
								pnlGraph3.setBackground(Color.white);
								pnlGraph3.setAutoscrolls(true);
								pnlGraph3.setBorder(LineBorder.createBlackLineBorder());
								pnlGraph3.setEnabled(false);
								pnlGraph3.setName("outline");
								pnlGraph3.setLayout(new TableLayout(new double[][] {
									{TableLayout.FILL},
									{TableLayout.FILL}}));
								((TableLayout)pnlGraph3.getLayout()).setHGap(1);
								((TableLayout)pnlGraph3.getLayout()).setVGap(1);
							}
							pnlPrint3.add(pnlGraph3, new TableLayoutConstraints(1, 8, 4, 8, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

							//======== pnlBot3 ========
							{
								pnlBot3.setBorder(LineBorder.createBlackLineBorder());
								pnlBot3.setBackground(Color.black);
								pnlBot3.setFont(new Font("Arial", Font.BOLD, 12));
								pnlBot3.setName("outline");
								pnlBot3.setLayout(new TableLayout(new double[][] {
									{105, 90, 138, 84},
									{45, 25, 25, 25}}));
								((TableLayout)pnlBot3.getLayout()).setHGap(2);
								((TableLayout)pnlBot3.getLayout()).setVGap(2);

								//---- label33 ----
								label33.setText(" PUMP TYPE");
								label33.setFont(new Font("Arial", Font.BOLD, 12));
								label33.setOpaque(true);
								label33.setHorizontalAlignment(SwingConstants.LEFT);
								label33.setBackground(Color.white);
								label33.setName("label");
								pnlBot3.add(label33, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblPumpType3 ----
								lblPumpType3.setText(" type");
								lblPumpType3.setFont(new Font("Arial", Font.PLAIN, 12));
								lblPumpType3.setOpaque(true);
								lblPumpType3.setHorizontalAlignment(SwingConstants.LEFT);
								lblPumpType3.setBackground(Color.white);
								lblPumpType3.setName("declared");
								pnlBot3.add(lblPumpType3, new TableLayoutConstraints(1, 0, 3, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- label61 ----
								label61.setText(" PUMP SL. NO.");
								label61.setFont(new Font("Arial", Font.BOLD, 12));
								label61.setOpaque(true);
								label61.setHorizontalAlignment(SwingConstants.LEFT);
								label61.setBackground(Color.white);
								label61.setName("label");
								pnlBot3.add(label61, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblPSno3 ----
								lblPSno3.setText(" pslno");
								lblPSno3.setFont(new Font("Arial", Font.PLAIN, 12));
								lblPSno3.setOpaque(true);
								lblPSno3.setHorizontalAlignment(SwingConstants.LEFT);
								lblPSno3.setBackground(Color.white);
								lblPSno3.setName("actual");
								pnlBot3.add(lblPSno3, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- label62 ----
								label62.setText(" DATE");
								label62.setFont(new Font("Arial", Font.BOLD, 12));
								label62.setOpaque(true);
								label62.setHorizontalAlignment(SwingConstants.LEFT);
								label62.setBackground(Color.white);
								label62.setName("label");
								pnlBot3.add(label62, new TableLayoutConstraints(2, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblDt3 ----
								lblDt3.setText(" dt");
								lblDt3.setFont(new Font("Arial", Font.PLAIN, 12));
								lblDt3.setOpaque(true);
								lblDt3.setHorizontalAlignment(SwingConstants.LEFT);
								lblDt3.setBackground(Color.white);
								lblDt3.setName("actual");
								pnlBot3.add(lblDt3, new TableLayoutConstraints(3, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblHdR3 ----
								lblHdR3.setText(" HEAD RANGE (m)");
								lblHdR3.setFont(new Font("Arial", Font.BOLD, 12));
								lblHdR3.setOpaque(true);
								lblHdR3.setHorizontalAlignment(SwingConstants.LEFT);
								lblHdR3.setBackground(Color.white);
								lblHdR3.setName("label");
								pnlBot3.add(lblHdR3, new TableLayoutConstraints(0, 2, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblHeadR3 ----
								lblHeadR3.setText(" headrange");
								lblHeadR3.setFont(new Font("Arial", Font.PLAIN, 12));
								lblHeadR3.setOpaque(true);
								lblHeadR3.setHorizontalAlignment(SwingConstants.LEFT);
								lblHeadR3.setBackground(Color.white);
								lblHeadR3.setName("declared");
								pnlBot3.add(lblHeadR3, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lbllblPSize3 ----
								lbllblPSize3.setText(" PIPE SIZE (S X D) (mm)");
								lbllblPSize3.setFont(new Font("Arial", Font.BOLD, 12));
								lbllblPSize3.setOpaque(true);
								lbllblPSize3.setHorizontalAlignment(SwingConstants.LEFT);
								lbllblPSize3.setBackground(Color.white);
								lbllblPSize3.setName("label");
								pnlBot3.add(lbllblPSize3, new TableLayoutConstraints(2, 2, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblPSz3 ----
								lblPSz3.setText(" psize");
								lblPSz3.setFont(new Font("Arial", Font.PLAIN, 12));
								lblPSz3.setOpaque(true);
								lblPSz3.setHorizontalAlignment(SwingConstants.LEFT);
								lblPSz3.setBackground(Color.white);
								lblPSz3.setName("declared");
								pnlBot3.add(lblPSz3, new TableLayoutConstraints(3, 2, 3, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lbllblFreq3 ----
								lbllblFreq3.setText(" FREQUENCY (Hz)");
								lbllblFreq3.setFont(new Font("Arial", Font.BOLD, 12));
								lbllblFreq3.setOpaque(true);
								lbllblFreq3.setHorizontalAlignment(SwingConstants.LEFT);
								lbllblFreq3.setBackground(Color.white);
								lbllblFreq3.setName("label");
								pnlBot3.add(lbllblFreq3, new TableLayoutConstraints(0, 3, 0, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblFreq3 ----
								lblFreq3.setText(" freq");
								lblFreq3.setFont(new Font("Arial", Font.PLAIN, 12));
								lblFreq3.setOpaque(true);
								lblFreq3.setHorizontalAlignment(SwingConstants.LEFT);
								lblFreq3.setBackground(Color.white);
								lblFreq3.setName("declared");
								pnlBot3.add(lblFreq3, new TableLayoutConstraints(1, 3, 3, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
							}
							pnlPrint3.add(pnlBot3, new TableLayoutConstraints(1, 9, 4, 9, TableLayoutConstraints.LEFT, TableLayoutConstraints.FULL));

							//======== pnlBotRes3 ========
							{
								pnlBotRes3.setBackground(Color.black);
								pnlBotRes3.setBorder(LineBorder.createBlackLineBorder());
								pnlBotRes3.setName("outline");
								pnlBotRes3.setLayout(new TableLayout(new double[][] {
									{TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL},
									{45, 25, 25, 25}}));
								((TableLayout)pnlBotRes3.getLayout()).setHGap(2);
								((TableLayout)pnlBotRes3.getLayout()).setVGap(2);

								//---- label49 ----
								label49.setText(" Duty Point");
								label49.setFont(new Font("Arial", Font.BOLD, 12));
								label49.setOpaque(true);
								label49.setHorizontalAlignment(SwingConstants.LEFT);
								label49.setBackground(Color.white);
								label49.setName("label");
								pnlBotRes3.add(label49, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- label57 ----
								label57.setText(" Guarantd.");
								label57.setFont(new Font("Arial", Font.BOLD, 12));
								label57.setOpaque(true);
								label57.setHorizontalAlignment(SwingConstants.LEFT);
								label57.setBackground(Color.white);
								label57.setName("label");
								pnlBotRes3.add(label57, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- label58 ----
								label58.setText(" Actual");
								label58.setFont(new Font("Arial", Font.BOLD, 12));
								label58.setOpaque(true);
								label58.setHorizontalAlignment(SwingConstants.LEFT);
								label58.setBackground(Color.white);
								label58.setName("label");
								pnlBotRes3.add(label58, new TableLayoutConstraints(0, 2, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- label59 ----
								label59.setText(" Results");
								label59.setFont(new Font("Arial", Font.BOLD, 12));
								label59.setOpaque(true);
								label59.setHorizontalAlignment(SwingConstants.LEFT);
								label59.setBackground(Color.white);
								label59.setName("label");
								pnlBotRes3.add(label59, new TableLayoutConstraints(0, 3, 0, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblFlowUnit3 ----
								lblFlowUnit3.setText("Q (unit)");
								lblFlowUnit3.setFont(new Font("Arial", Font.BOLD, 12));
								lblFlowUnit3.setOpaque(true);
								lblFlowUnit3.setHorizontalAlignment(SwingConstants.CENTER);
								lblFlowUnit3.setBackground(Color.white);
								lblFlowUnit3.setName("label");
								pnlBotRes3.add(lblFlowUnit3, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblFlow3 ----
								lblFlow3.setText("flow");
								lblFlow3.setFont(new Font("Arial", Font.PLAIN, 12));
								lblFlow3.setOpaque(true);
								lblFlow3.setHorizontalAlignment(SwingConstants.CENTER);
								lblFlow3.setBackground(Color.white);
								lblFlow3.setName("declared");
								pnlBotRes3.add(lblFlow3, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblFlowAct3 ----
								lblFlowAct3.setText("flow");
								lblFlowAct3.setFont(new Font("Arial", Font.PLAIN, 12));
								lblFlowAct3.setOpaque(true);
								lblFlowAct3.setHorizontalAlignment(SwingConstants.CENTER);
								lblFlowAct3.setBackground(Color.white);
								lblFlowAct3.setName("actual");
								pnlBotRes3.add(lblFlowAct3, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblFlowRes3 ----
								lblFlowRes3.setText("flow");
								lblFlowRes3.setFont(new Font("Arial", Font.PLAIN, 12));
								lblFlowRes3.setOpaque(true);
								lblFlowRes3.setHorizontalAlignment(SwingConstants.CENTER);
								lblFlowRes3.setBackground(Color.white);
								lblFlowRes3.setName("actual");
								pnlBotRes3.add(lblFlowRes3, new TableLayoutConstraints(1, 3, 1, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblHd3 ----
								lblHd3.setText("TH (m)");
								lblHd3.setFont(new Font("Arial", Font.BOLD, 12));
								lblHd3.setOpaque(true);
								lblHd3.setHorizontalAlignment(SwingConstants.CENTER);
								lblHd3.setBackground(Color.white);
								lblHd3.setName("label");
								pnlBotRes3.add(lblHd3, new TableLayoutConstraints(2, 0, 2, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblHead3 ----
								lblHead3.setText("head");
								lblHead3.setFont(new Font("Arial", Font.PLAIN, 12));
								lblHead3.setOpaque(true);
								lblHead3.setHorizontalAlignment(SwingConstants.CENTER);
								lblHead3.setBackground(Color.white);
								lblHead3.setName("declared");
								pnlBotRes3.add(lblHead3, new TableLayoutConstraints(2, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblHeadAct3 ----
								lblHeadAct3.setText("head");
								lblHeadAct3.setFont(new Font("Arial", Font.PLAIN, 12));
								lblHeadAct3.setOpaque(true);
								lblHeadAct3.setHorizontalAlignment(SwingConstants.CENTER);
								lblHeadAct3.setBackground(Color.white);
								lblHeadAct3.setName("actual");
								pnlBotRes3.add(lblHeadAct3, new TableLayoutConstraints(2, 2, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblHeadRes3 ----
								lblHeadRes3.setText("head");
								lblHeadRes3.setFont(new Font("Arial", Font.PLAIN, 12));
								lblHeadRes3.setOpaque(true);
								lblHeadRes3.setHorizontalAlignment(SwingConstants.CENTER);
								lblHeadRes3.setBackground(Color.white);
								lblHeadRes3.setName("actual");
								pnlBotRes3.add(lblHeadRes3, new TableLayoutConstraints(2, 3, 2, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lbllblMI3 ----
								lbllblMI3.setText("MI (kW)");
								lbllblMI3.setFont(new Font("Arial", Font.BOLD, 12));
								lbllblMI3.setOpaque(true);
								lbllblMI3.setHorizontalAlignment(SwingConstants.CENTER);
								lbllblMI3.setBackground(Color.white);
								lbllblMI3.setName("label");
								pnlBotRes3.add(lbllblMI3, new TableLayoutConstraints(3, 0, 3, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lbllblEff3 ----
								lbllblEff3.setText("O.A Eff. (%)");
								lbllblEff3.setFont(new Font("Arial", Font.BOLD, 12));
								lbllblEff3.setOpaque(true);
								lbllblEff3.setHorizontalAlignment(SwingConstants.CENTER);
								lbllblEff3.setBackground(Color.white);
								lbllblEff3.setName("label");
								pnlBotRes3.add(lbllblEff3, new TableLayoutConstraints(4, 0, 4, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblMotIp3 ----
								lblMotIp3.setText("mi");
								lblMotIp3.setFont(new Font("Arial", Font.PLAIN, 12));
								lblMotIp3.setOpaque(true);
								lblMotIp3.setHorizontalAlignment(SwingConstants.CENTER);
								lblMotIp3.setBackground(Color.white);
								lblMotIp3.setName("declared");
								pnlBotRes3.add(lblMotIp3, new TableLayoutConstraints(3, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblMotIpAct3 ----
								lblMotIpAct3.setText("mi");
								lblMotIpAct3.setFont(new Font("Arial", Font.PLAIN, 12));
								lblMotIpAct3.setOpaque(true);
								lblMotIpAct3.setHorizontalAlignment(SwingConstants.CENTER);
								lblMotIpAct3.setBackground(Color.white);
								lblMotIpAct3.setName("actual");
								pnlBotRes3.add(lblMotIpAct3, new TableLayoutConstraints(3, 2, 3, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblMotIpRes3 ----
								lblMotIpRes3.setText("mi");
								lblMotIpRes3.setFont(new Font("Arial", Font.PLAIN, 12));
								lblMotIpRes3.setOpaque(true);
								lblMotIpRes3.setHorizontalAlignment(SwingConstants.CENTER);
								lblMotIpRes3.setBackground(Color.white);
								lblMotIpRes3.setName("actual");
								pnlBotRes3.add(lblMotIpRes3, new TableLayoutConstraints(3, 3, 3, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lbllblMaxC3 ----
								lbllblMaxC3.setText("<html><body align=\"center\">I-Max in Head Range (A)</body></html>");
								lbllblMaxC3.setFont(new Font("Arial", Font.BOLD, 12));
								lbllblMaxC3.setOpaque(true);
								lbllblMaxC3.setHorizontalAlignment(SwingConstants.CENTER);
								lbllblMaxC3.setBackground(Color.white);
								lbllblMaxC3.setName("label");
								pnlBotRes3.add(lbllblMaxC3, new TableLayoutConstraints(5, 0, 5, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblEff3 ----
								lblEff3.setText("eff");
								lblEff3.setFont(new Font("Arial", Font.PLAIN, 12));
								lblEff3.setOpaque(true);
								lblEff3.setHorizontalAlignment(SwingConstants.CENTER);
								lblEff3.setBackground(Color.white);
								lblEff3.setName("declared");
								pnlBotRes3.add(lblEff3, new TableLayoutConstraints(4, 1, 4, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblCur3 ----
								lblCur3.setText("a");
								lblCur3.setFont(new Font("Arial", Font.PLAIN, 12));
								lblCur3.setOpaque(true);
								lblCur3.setHorizontalAlignment(SwingConstants.CENTER);
								lblCur3.setBackground(Color.white);
								lblCur3.setName("declared");
								pnlBotRes3.add(lblCur3, new TableLayoutConstraints(5, 1, 5, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblEffAct3 ----
								lblEffAct3.setText("eff");
								lblEffAct3.setFont(new Font("Arial", Font.PLAIN, 12));
								lblEffAct3.setOpaque(true);
								lblEffAct3.setHorizontalAlignment(SwingConstants.CENTER);
								lblEffAct3.setBackground(Color.white);
								lblEffAct3.setName("actual");
								pnlBotRes3.add(lblEffAct3, new TableLayoutConstraints(4, 2, 4, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblCurAct3 ----
								lblCurAct3.setText("a");
								lblCurAct3.setFont(new Font("Arial", Font.PLAIN, 12));
								lblCurAct3.setOpaque(true);
								lblCurAct3.setHorizontalAlignment(SwingConstants.CENTER);
								lblCurAct3.setBackground(Color.white);
								lblCurAct3.setName("actual");
								pnlBotRes3.add(lblCurAct3, new TableLayoutConstraints(5, 2, 5, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblEffRes3 ----
								lblEffRes3.setText("eff");
								lblEffRes3.setFont(new Font("Arial", Font.PLAIN, 12));
								lblEffRes3.setOpaque(true);
								lblEffRes3.setHorizontalAlignment(SwingConstants.CENTER);
								lblEffRes3.setBackground(Color.white);
								lblEffRes3.setName("actual");
								pnlBotRes3.add(lblEffRes3, new TableLayoutConstraints(4, 3, 4, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblCurRes3 ----
								lblCurRes3.setText("a");
								lblCurRes3.setFont(new Font("Arial", Font.PLAIN, 12));
								lblCurRes3.setOpaque(true);
								lblCurRes3.setHorizontalAlignment(SwingConstants.CENTER);
								lblCurRes3.setBackground(Color.white);
								lblCurRes3.setName("actual");
								pnlBotRes3.add(lblCurRes3, new TableLayoutConstraints(5, 3, 5, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
							}
							pnlPrint3.add(pnlBotRes3, new TableLayoutConstraints(3, 9, 4, 9, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

							//======== pnlBotRes3R ========
							{
								pnlBotRes3R.setBackground(Color.black);
								pnlBotRes3R.setBorder(LineBorder.createBlackLineBorder());
								pnlBotRes3R.setName("outline");
								pnlBotRes3R.setVisible(false);
								pnlBotRes3R.setLayout(new TableLayout(new double[][] {
									{TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL},
									{20, 23, 25, 25, 25}}));
								((TableLayout)pnlBotRes3R.getLayout()).setHGap(2);
								((TableLayout)pnlBotRes3R.getLayout()).setVGap(2);

								//---- lblFlowUnit3RL2 ----
								lblFlowUnit3RL2.setText("Low Head");
								lblFlowUnit3RL2.setFont(new Font("Arial", Font.BOLD, 12));
								lblFlowUnit3RL2.setOpaque(true);
								lblFlowUnit3RL2.setHorizontalAlignment(SwingConstants.CENTER);
								lblFlowUnit3RL2.setBackground(Color.white);
								lblFlowUnit3RL2.setName("label");
								pnlBotRes3R.add(lblFlowUnit3RL2, new TableLayoutConstraints(1, 0, 4, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblFlowUnit3RL3 ----
								lblFlowUnit3RL3.setText("High Head");
								lblFlowUnit3RL3.setFont(new Font("Arial", Font.BOLD, 12));
								lblFlowUnit3RL3.setOpaque(true);
								lblFlowUnit3RL3.setHorizontalAlignment(SwingConstants.CENTER);
								lblFlowUnit3RL3.setBackground(Color.white);
								lblFlowUnit3RL3.setName("label");
								pnlBotRes3R.add(lblFlowUnit3RL3, new TableLayoutConstraints(5, 0, 8, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- label227 ----
								label227.setText(" Duty Pt.");
								label227.setFont(new Font("Arial", Font.BOLD, 10));
								label227.setOpaque(true);
								label227.setHorizontalAlignment(SwingConstants.LEFT);
								label227.setBackground(Color.white);
								label227.setName("label");
								pnlBotRes3R.add(label227, new TableLayoutConstraints(0, 0, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblFlowUnit3RL ----
								lblFlowUnit3RL.setText("Q (unit)");
								lblFlowUnit3RL.setFont(new Font("Arial", Font.BOLD, 10));
								lblFlowUnit3RL.setOpaque(true);
								lblFlowUnit3RL.setHorizontalAlignment(SwingConstants.CENTER);
								lblFlowUnit3RL.setBackground(Color.white);
								lblFlowUnit3RL.setName("label");
								pnlBotRes3R.add(lblFlowUnit3RL, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblHd3RL ----
								lblHd3RL.setText("TH (m)");
								lblHd3RL.setFont(new Font("Arial", Font.BOLD, 10));
								lblHd3RL.setOpaque(true);
								lblHd3RL.setHorizontalAlignment(SwingConstants.CENTER);
								lblHd3RL.setBackground(Color.white);
								lblHd3RL.setName("label");
								pnlBotRes3R.add(lblHd3RL, new TableLayoutConstraints(2, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblFlowUnit3RH ----
								lblFlowUnit3RH.setText("Q (unit)");
								lblFlowUnit3RH.setFont(new Font("Arial", Font.BOLD, 10));
								lblFlowUnit3RH.setOpaque(true);
								lblFlowUnit3RH.setHorizontalAlignment(SwingConstants.CENTER);
								lblFlowUnit3RH.setBackground(Color.white);
								lblFlowUnit3RH.setName("label");
								pnlBotRes3R.add(lblFlowUnit3RH, new TableLayoutConstraints(5, 1, 5, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblHdRH ----
								lblHdRH.setText("TH (m)");
								lblHdRH.setFont(new Font("Arial", Font.BOLD, 10));
								lblHdRH.setOpaque(true);
								lblHdRH.setHorizontalAlignment(SwingConstants.CENTER);
								lblHdRH.setBackground(Color.white);
								lblHdRH.setName("label");
								pnlBotRes3R.add(lblHdRH, new TableLayoutConstraints(6, 1, 6, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- label232 ----
								label232.setText(" Grntd.");
								label232.setFont(new Font("Arial", Font.BOLD, 10));
								label232.setOpaque(true);
								label232.setHorizontalAlignment(SwingConstants.LEFT);
								label232.setBackground(Color.white);
								label232.setName("label");
								pnlBotRes3R.add(label232, new TableLayoutConstraints(0, 2, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- label233 ----
								label233.setText(" Actual");
								label233.setFont(new Font("Arial", Font.BOLD, 10));
								label233.setOpaque(true);
								label233.setHorizontalAlignment(SwingConstants.LEFT);
								label233.setBackground(Color.white);
								label233.setName("label");
								pnlBotRes3R.add(label233, new TableLayoutConstraints(0, 3, 0, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- label234 ----
								label234.setText(" Results");
								label234.setFont(new Font("Arial", Font.BOLD, 10));
								label234.setOpaque(true);
								label234.setHorizontalAlignment(SwingConstants.LEFT);
								label234.setBackground(Color.white);
								label234.setName("label");
								pnlBotRes3R.add(label234, new TableLayoutConstraints(0, 4, 0, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- label237 ----
								label237.setText("<html><body align=\"center\">I-Max (A)</html>");
								label237.setFont(new Font("Arial", Font.BOLD, 10));
								label237.setOpaque(true);
								label237.setHorizontalAlignment(SwingConstants.CENTER);
								label237.setBackground(Color.white);
								label237.setName("label");
								pnlBotRes3R.add(label237, new TableLayoutConstraints(9, 0, 9, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lbllblEff3R1 ----
								lbllblEff3R1.setText("OE (%)");
								lbllblEff3R1.setFont(new Font("Arial", Font.BOLD, 10));
								lbllblEff3R1.setOpaque(true);
								lbllblEff3R1.setHorizontalAlignment(SwingConstants.CENTER);
								lbllblEff3R1.setBackground(Color.white);
								lbllblEff3R1.setName("label");
								pnlBotRes3R.add(lbllblEff3R1, new TableLayoutConstraints(3, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lbllblMI3R1 ----
								lbllblMI3R1.setText("MI (kW)");
								lbllblMI3R1.setFont(new Font("Arial", Font.BOLD, 10));
								lbllblMI3R1.setOpaque(true);
								lbllblMI3R1.setHorizontalAlignment(SwingConstants.CENTER);
								lbllblMI3R1.setBackground(Color.white);
								lbllblMI3R1.setName("label");
								pnlBotRes3R.add(lbllblMI3R1, new TableLayoutConstraints(4, 1, 4, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lbllblEff3R2 ----
								lbllblEff3R2.setText("OE (%)");
								lbllblEff3R2.setFont(new Font("Arial", Font.BOLD, 10));
								lbllblEff3R2.setOpaque(true);
								lbllblEff3R2.setHorizontalAlignment(SwingConstants.CENTER);
								lbllblEff3R2.setBackground(Color.white);
								lbllblEff3R2.setName("label");
								pnlBotRes3R.add(lbllblEff3R2, new TableLayoutConstraints(7, 1, 7, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lbllblMI3R2 ----
								lbllblMI3R2.setText("MI (kW)");
								lbllblMI3R2.setFont(new Font("Arial", Font.BOLD, 10));
								lbllblMI3R2.setOpaque(true);
								lbllblMI3R2.setHorizontalAlignment(SwingConstants.CENTER);
								lbllblMI3R2.setBackground(Color.white);
								lbllblMI3R2.setName("label");
								pnlBotRes3R.add(lbllblMI3R2, new TableLayoutConstraints(8, 1, 8, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblFlow3RL ----
								lblFlow3RL.setText("flow");
								lblFlow3RL.setFont(new Font("Arial", Font.PLAIN, 12));
								lblFlow3RL.setOpaque(true);
								lblFlow3RL.setHorizontalAlignment(SwingConstants.CENTER);
								lblFlow3RL.setBackground(Color.white);
								lblFlow3RL.setName("declared");
								pnlBotRes3R.add(lblFlow3RL, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblHead3RL ----
								lblHead3RL.setText("head");
								lblHead3RL.setFont(new Font("Arial", Font.PLAIN, 12));
								lblHead3RL.setOpaque(true);
								lblHead3RL.setHorizontalAlignment(SwingConstants.CENTER);
								lblHead3RL.setBackground(Color.white);
								lblHead3RL.setName("declared");
								pnlBotRes3R.add(lblHead3RL, new TableLayoutConstraints(2, 2, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblEff3RL ----
								lblEff3RL.setText("eff");
								lblEff3RL.setFont(new Font("Arial", Font.PLAIN, 12));
								lblEff3RL.setOpaque(true);
								lblEff3RL.setHorizontalAlignment(SwingConstants.CENTER);
								lblEff3RL.setBackground(Color.white);
								lblEff3RL.setName("declared");
								pnlBotRes3R.add(lblEff3RL, new TableLayoutConstraints(3, 2, 3, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblMotIp3RL ----
								lblMotIp3RL.setText("mi");
								lblMotIp3RL.setFont(new Font("Arial", Font.PLAIN, 12));
								lblMotIp3RL.setOpaque(true);
								lblMotIp3RL.setHorizontalAlignment(SwingConstants.CENTER);
								lblMotIp3RL.setBackground(Color.white);
								lblMotIp3RL.setName("declared");
								pnlBotRes3R.add(lblMotIp3RL, new TableLayoutConstraints(4, 2, 4, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblFlow3RH ----
								lblFlow3RH.setText("flow");
								lblFlow3RH.setFont(new Font("Arial", Font.PLAIN, 12));
								lblFlow3RH.setOpaque(true);
								lblFlow3RH.setHorizontalAlignment(SwingConstants.CENTER);
								lblFlow3RH.setBackground(Color.white);
								lblFlow3RH.setName("declared");
								pnlBotRes3R.add(lblFlow3RH, new TableLayoutConstraints(5, 2, 5, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblHead3RH ----
								lblHead3RH.setText("head");
								lblHead3RH.setFont(new Font("Arial", Font.PLAIN, 12));
								lblHead3RH.setOpaque(true);
								lblHead3RH.setHorizontalAlignment(SwingConstants.CENTER);
								lblHead3RH.setBackground(Color.white);
								lblHead3RH.setName("declared");
								pnlBotRes3R.add(lblHead3RH, new TableLayoutConstraints(6, 2, 6, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblEff3RH ----
								lblEff3RH.setText("eff");
								lblEff3RH.setFont(new Font("Arial", Font.PLAIN, 12));
								lblEff3RH.setOpaque(true);
								lblEff3RH.setHorizontalAlignment(SwingConstants.CENTER);
								lblEff3RH.setBackground(Color.white);
								lblEff3RH.setName("declared");
								pnlBotRes3R.add(lblEff3RH, new TableLayoutConstraints(7, 2, 7, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblMotIp3RH ----
								lblMotIp3RH.setText("mi");
								lblMotIp3RH.setFont(new Font("Arial", Font.PLAIN, 12));
								lblMotIp3RH.setOpaque(true);
								lblMotIp3RH.setHorizontalAlignment(SwingConstants.CENTER);
								lblMotIp3RH.setBackground(Color.white);
								lblMotIp3RH.setName("declared");
								pnlBotRes3R.add(lblMotIp3RH, new TableLayoutConstraints(8, 2, 8, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblCur3R ----
								lblCur3R.setText("a");
								lblCur3R.setFont(new Font("Arial", Font.PLAIN, 12));
								lblCur3R.setOpaque(true);
								lblCur3R.setHorizontalAlignment(SwingConstants.CENTER);
								lblCur3R.setBackground(Color.white);
								lblCur3R.setName("declared");
								pnlBotRes3R.add(lblCur3R, new TableLayoutConstraints(9, 2, 9, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblFlowAct3RL ----
								lblFlowAct3RL.setText("flow");
								lblFlowAct3RL.setFont(new Font("Arial", Font.PLAIN, 12));
								lblFlowAct3RL.setOpaque(true);
								lblFlowAct3RL.setHorizontalAlignment(SwingConstants.CENTER);
								lblFlowAct3RL.setBackground(Color.white);
								lblFlowAct3RL.setName("actual");
								pnlBotRes3R.add(lblFlowAct3RL, new TableLayoutConstraints(1, 3, 1, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblHeadAct3RL ----
								lblHeadAct3RL.setText("head");
								lblHeadAct3RL.setFont(new Font("Arial", Font.PLAIN, 12));
								lblHeadAct3RL.setOpaque(true);
								lblHeadAct3RL.setHorizontalAlignment(SwingConstants.CENTER);
								lblHeadAct3RL.setBackground(Color.white);
								lblHeadAct3RL.setName("actual");
								pnlBotRes3R.add(lblHeadAct3RL, new TableLayoutConstraints(2, 3, 2, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblEffAct3RL ----
								lblEffAct3RL.setText("eff");
								lblEffAct3RL.setFont(new Font("Arial", Font.PLAIN, 12));
								lblEffAct3RL.setOpaque(true);
								lblEffAct3RL.setHorizontalAlignment(SwingConstants.CENTER);
								lblEffAct3RL.setBackground(Color.white);
								lblEffAct3RL.setName("actual");
								pnlBotRes3R.add(lblEffAct3RL, new TableLayoutConstraints(3, 3, 3, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblMotIpAct3RL ----
								lblMotIpAct3RL.setText("mi");
								lblMotIpAct3RL.setFont(new Font("Arial", Font.PLAIN, 12));
								lblMotIpAct3RL.setOpaque(true);
								lblMotIpAct3RL.setHorizontalAlignment(SwingConstants.CENTER);
								lblMotIpAct3RL.setBackground(Color.white);
								lblMotIpAct3RL.setName("actual");
								pnlBotRes3R.add(lblMotIpAct3RL, new TableLayoutConstraints(4, 3, 4, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblFlowAct3RH ----
								lblFlowAct3RH.setText("flow");
								lblFlowAct3RH.setFont(new Font("Arial", Font.PLAIN, 12));
								lblFlowAct3RH.setOpaque(true);
								lblFlowAct3RH.setHorizontalAlignment(SwingConstants.CENTER);
								lblFlowAct3RH.setBackground(Color.white);
								lblFlowAct3RH.setName("actual");
								pnlBotRes3R.add(lblFlowAct3RH, new TableLayoutConstraints(5, 3, 5, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblHeadAct3RH ----
								lblHeadAct3RH.setText("head");
								lblHeadAct3RH.setFont(new Font("Arial", Font.PLAIN, 12));
								lblHeadAct3RH.setOpaque(true);
								lblHeadAct3RH.setHorizontalAlignment(SwingConstants.CENTER);
								lblHeadAct3RH.setBackground(Color.white);
								lblHeadAct3RH.setName("actual");
								pnlBotRes3R.add(lblHeadAct3RH, new TableLayoutConstraints(6, 3, 6, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblEffAct3RH ----
								lblEffAct3RH.setText("eff");
								lblEffAct3RH.setFont(new Font("Arial", Font.PLAIN, 12));
								lblEffAct3RH.setOpaque(true);
								lblEffAct3RH.setHorizontalAlignment(SwingConstants.CENTER);
								lblEffAct3RH.setBackground(Color.white);
								lblEffAct3RH.setName("actual");
								pnlBotRes3R.add(lblEffAct3RH, new TableLayoutConstraints(7, 3, 7, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblMotIpAct3RH ----
								lblMotIpAct3RH.setText("mi");
								lblMotIpAct3RH.setFont(new Font("Arial", Font.PLAIN, 12));
								lblMotIpAct3RH.setOpaque(true);
								lblMotIpAct3RH.setHorizontalAlignment(SwingConstants.CENTER);
								lblMotIpAct3RH.setBackground(Color.white);
								lblMotIpAct3RH.setName("actual");
								pnlBotRes3R.add(lblMotIpAct3RH, new TableLayoutConstraints(8, 3, 8, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblCurAct3R ----
								lblCurAct3R.setText("a");
								lblCurAct3R.setFont(new Font("Arial", Font.PLAIN, 12));
								lblCurAct3R.setOpaque(true);
								lblCurAct3R.setHorizontalAlignment(SwingConstants.CENTER);
								lblCurAct3R.setBackground(Color.white);
								lblCurAct3R.setName("actual");
								pnlBotRes3R.add(lblCurAct3R, new TableLayoutConstraints(9, 3, 9, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblFlowRes3RL ----
								lblFlowRes3RL.setText("flow");
								lblFlowRes3RL.setFont(new Font("Arial", Font.PLAIN, 12));
								lblFlowRes3RL.setOpaque(true);
								lblFlowRes3RL.setHorizontalAlignment(SwingConstants.CENTER);
								lblFlowRes3RL.setBackground(Color.white);
								lblFlowRes3RL.setName("actual");
								pnlBotRes3R.add(lblFlowRes3RL, new TableLayoutConstraints(1, 4, 1, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblHeadRes3RL ----
								lblHeadRes3RL.setText("head");
								lblHeadRes3RL.setFont(new Font("Arial", Font.PLAIN, 12));
								lblHeadRes3RL.setOpaque(true);
								lblHeadRes3RL.setHorizontalAlignment(SwingConstants.CENTER);
								lblHeadRes3RL.setBackground(Color.white);
								lblHeadRes3RL.setName("actual");
								pnlBotRes3R.add(lblHeadRes3RL, new TableLayoutConstraints(2, 4, 2, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblEffRes3RL ----
								lblEffRes3RL.setText("eff");
								lblEffRes3RL.setFont(new Font("Arial", Font.PLAIN, 12));
								lblEffRes3RL.setOpaque(true);
								lblEffRes3RL.setHorizontalAlignment(SwingConstants.CENTER);
								lblEffRes3RL.setBackground(Color.white);
								lblEffRes3RL.setName("actual");
								pnlBotRes3R.add(lblEffRes3RL, new TableLayoutConstraints(3, 4, 3, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblMotIpRes3RL ----
								lblMotIpRes3RL.setText("mi");
								lblMotIpRes3RL.setFont(new Font("Arial", Font.PLAIN, 12));
								lblMotIpRes3RL.setOpaque(true);
								lblMotIpRes3RL.setHorizontalAlignment(SwingConstants.CENTER);
								lblMotIpRes3RL.setBackground(Color.white);
								lblMotIpRes3RL.setName("actual");
								pnlBotRes3R.add(lblMotIpRes3RL, new TableLayoutConstraints(4, 4, 4, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblFlowRes3RH ----
								lblFlowRes3RH.setText("flow");
								lblFlowRes3RH.setFont(new Font("Arial", Font.PLAIN, 12));
								lblFlowRes3RH.setOpaque(true);
								lblFlowRes3RH.setHorizontalAlignment(SwingConstants.CENTER);
								lblFlowRes3RH.setBackground(Color.white);
								lblFlowRes3RH.setName("actual");
								pnlBotRes3R.add(lblFlowRes3RH, new TableLayoutConstraints(5, 4, 5, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblHeadRes3RH ----
								lblHeadRes3RH.setText("head");
								lblHeadRes3RH.setFont(new Font("Arial", Font.PLAIN, 12));
								lblHeadRes3RH.setOpaque(true);
								lblHeadRes3RH.setHorizontalAlignment(SwingConstants.CENTER);
								lblHeadRes3RH.setBackground(Color.white);
								lblHeadRes3RH.setName("actual");
								pnlBotRes3R.add(lblHeadRes3RH, new TableLayoutConstraints(6, 4, 6, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblEffRes3RH ----
								lblEffRes3RH.setText("eff");
								lblEffRes3RH.setFont(new Font("Arial", Font.PLAIN, 12));
								lblEffRes3RH.setOpaque(true);
								lblEffRes3RH.setHorizontalAlignment(SwingConstants.CENTER);
								lblEffRes3RH.setBackground(Color.white);
								lblEffRes3RH.setName("actual");
								pnlBotRes3R.add(lblEffRes3RH, new TableLayoutConstraints(7, 4, 7, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblMotIpRes3RH ----
								lblMotIpRes3RH.setText("mi");
								lblMotIpRes3RH.setFont(new Font("Arial", Font.PLAIN, 12));
								lblMotIpRes3RH.setOpaque(true);
								lblMotIpRes3RH.setHorizontalAlignment(SwingConstants.CENTER);
								lblMotIpRes3RH.setBackground(Color.white);
								lblMotIpRes3RH.setName("actual");
								pnlBotRes3R.add(lblMotIpRes3RH, new TableLayoutConstraints(8, 4, 8, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblCurRes3R ----
								lblCurRes3R.setText("a");
								lblCurRes3R.setFont(new Font("Arial", Font.PLAIN, 12));
								lblCurRes3R.setOpaque(true);
								lblCurRes3R.setHorizontalAlignment(SwingConstants.CENTER);
								lblCurRes3R.setBackground(Color.white);
								lblCurRes3R.setName("actual");
								pnlBotRes3R.add(lblCurRes3R, new TableLayoutConstraints(9, 4, 9, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
							}
							pnlPrint3.add(pnlBotRes3R, new TableLayoutConstraints(3, 9, 4, 9, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

							//======== pnlBotBot3 ========
							{
								pnlBotBot3.setBackground(Color.white);
								pnlBotBot3.setBorder(LineBorder.createBlackLineBorder());
								pnlBotBot3.setName("outline");
								pnlBotBot3.setLayout(new TableLayout(new double[][] {
									{TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL},
									{10, 20, 20}}));
								((TableLayout)pnlBotBot3.getLayout()).setHGap(5);
								((TableLayout)pnlBotBot3.getLayout()).setVGap(5);

								//---- lblTestedBy3 ----
								lblTestedBy3.setText("Tested By");
								lblTestedBy3.setFont(new Font("Arial", Font.PLAIN, 13));
								lblTestedBy3.setHorizontalAlignment(SwingConstants.CENTER);
								lblTestedBy3.setName("label");
								pnlBotBot3.add(lblTestedBy3, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- label103 ----
								label103.setText("Approved By");
								label103.setFont(new Font("Arial", Font.PLAIN, 13));
								label103.setHorizontalAlignment(SwingConstants.CENTER);
								label103.setName("label");
								pnlBotBot3.add(label103, new TableLayoutConstraints(3, 2, 3, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblVerBy12 ----
								lblVerBy12.setText("Verified By");
								lblVerBy12.setFont(new Font("Arial", Font.PLAIN, 13));
								lblVerBy12.setHorizontalAlignment(SwingConstants.CENTER);
								lblVerBy12.setName("label");
								pnlBotBot3.add(lblVerBy12, new TableLayoutConstraints(2, 2, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
							}
							pnlPrint3.add(pnlBotBot3, new TableLayoutConstraints(1, 10, 4, 10, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

							//---- lblAppVer3 ----
							lblAppVer3.setText("Doer PumpViewPro");
							lblAppVer3.setFont(new Font("Arial", Font.PLAIN, 10));
							lblAppVer3.setHorizontalAlignment(SwingConstants.RIGHT);
							lblAppVer3.setForeground(Color.gray);
							pnlPrint3.add(lblAppVer3, new TableLayoutConstraints(3, 12, 4, 12, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
						}
						printArea3.add(pnlPrint3, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
					}
					scrollPane3.setViewportView(printArea3);
				}
				tabReport.addTab("3. Result Graph", scrollPane3);

				//======== pnlRep4 ========
				{
					pnlRep4.setLayout(new TableLayout(new double[][] {
						{TableLayout.FILL},
						{TableLayout.FILL}}));
					((TableLayout)pnlRep4.getLayout()).setHGap(5);
					((TableLayout)pnlRep4.getLayout()).setVGap(5);

					//======== tabISI ========
					{
						tabISI.setBorder(null);
						tabISI.setFont(new Font("Arial", Font.PLAIN, 12));
						tabISI.addChangeListener(new ChangeListener() {
							@Override
							public void stateChanged(ChangeEvent e) {
								tabISIStateChanged();
							}
						});

						//======== scrollPane41 ========
						{

							//======== printArea41 ========
							{
								printArea41.setBorder(new LineBorder(Color.blue, 2));
								printArea41.setBackground(Color.white);
								printArea41.setLayout(new TableLayout(new double[][] {
									{900},
									{TableLayout.PREFERRED, 1450}}));

								//---- lblRtWarn41 ----
								lblRtWarn41.setText("For routine tests, performance graph and result dervied from graph may not be reliable due to less number of test heads");
								lblRtWarn41.setFont(new Font("Arial", Font.PLAIN, 12));
								lblRtWarn41.setHorizontalAlignment(SwingConstants.CENTER);
								lblRtWarn41.setBorder(new LineBorder(Color.orange, 2, true));
								lblRtWarn41.setOpaque(true);
								lblRtWarn41.setBackground(Color.yellow);
								lblRtWarn41.setForeground(Color.red);
								printArea41.add(lblRtWarn41, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//======== pnlPrint41 ========
								{
									pnlPrint41.setBorder(null);
									pnlPrint41.setBackground(Color.white);
									pnlPrint41.setLayout(new TableLayout(new double[][] {
										{40, 120, TableLayout.FILL, TableLayout.FILL, 120, 15},
										{35, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, 500, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED}}));

									//---- lblCustLogo41 ----
									lblCustLogo41.setHorizontalAlignment(SwingConstants.CENTER);
									pnlPrint41.add(lblCustLogo41, new TableLayoutConstraints(1, 1, 1, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblCompName41 ----
									lblCompName41.setText("Company Name");
									lblCompName41.setFont(new Font("Arial", Font.BOLD, 16));
									lblCompName41.setHorizontalAlignment(SwingConstants.CENTER);
									lblCompName41.setName("header");
									pnlPrint41.add(lblCompName41, new TableLayoutConstraints(2, 1, 3, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblIS41 ----
									lblIS41.setText("IS:");
									lblIS41.setHorizontalAlignment(SwingConstants.CENTER);
									lblIS41.setFont(new Font("Arial", Font.BOLD, 10));
									lblIS41.setName("label");
									pnlPrint41.add(lblIS41, new TableLayoutConstraints(4, 1, 4, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblISILogo41 ----
									lblISILogo41.setIcon(new ImageIcon(getClass().getResource("/img/isi_logo.PNG")));
									lblISILogo41.setHorizontalAlignment(SwingConstants.CENTER);
									lblISILogo41.setVerticalAlignment(SwingConstants.TOP);
									lblISILogo41.setIconTextGap(0);
									pnlPrint41.add(lblISILogo41, new TableLayoutConstraints(4, 2, 4, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblCompAdr41 ----
									lblCompAdr41.setText("Address Line 1 and Line 2");
									lblCompAdr41.setFont(new Font("Arial", Font.BOLD, 12));
									lblCompAdr41.setHorizontalAlignment(SwingConstants.CENTER);
									lblCompAdr41.setName("header");
									pnlPrint41.add(lblCompAdr41, new TableLayoutConstraints(2, 3, 3, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblTitle41 ----
									lblTitle41.setText("PUMP TEST ISI REPORT");
									lblTitle41.setFont(new Font("Arial", Font.BOLD, 14));
									lblTitle41.setHorizontalAlignment(SwingConstants.CENTER);
									lblTitle41.setName("header");
									pnlPrint41.add(lblTitle41, new TableLayoutConstraints(2, 5, 3, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblISIRefNo41 ----
									lblISIRefNo41.setText("ISI Ref No.");
									lblISIRefNo41.setHorizontalAlignment(SwingConstants.CENTER);
									lblISIRefNo41.setFont(new Font("Arial", Font.BOLD, 10));
									lblISIRefNo41.setName("label");
									pnlPrint41.add(lblISIRefNo41, new TableLayoutConstraints(4, 4, 4, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblPage41 ----
									lblPage41.setText("Page 1 of 1");
									lblPage41.setFont(new Font("Arial", Font.PLAIN, 12));
									lblPage41.setHorizontalAlignment(SwingConstants.RIGHT);
									lblPage41.setName("label");
									pnlPrint41.add(lblPage41, new TableLayoutConstraints(4, 7, 4, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//======== pnlTop41 ========
									{
										pnlTop41.setBorder(LineBorder.createBlackLineBorder());
										pnlTop41.setBackground(Color.white);
										pnlTop41.setFont(new Font("Arial", Font.BOLD, 12));
										pnlTop41.setName("outline");
										pnlTop41.setLayout(new TableLayout(new double[][] {
											{5, TableLayout.PREFERRED, 130, TableLayout.PREFERRED, 70, TableLayout.PREFERRED, 55, TableLayout.PREFERRED, TableLayout.FILL},
											{5, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, 5}}));
										((TableLayout)pnlTop41.getLayout()).setHGap(7);
										((TableLayout)pnlTop41.getLayout()).setVGap(7);

										//---- label25 ----
										label25.setText("PUMP TYPE");
										label25.setFont(new Font("Arial", Font.BOLD, 12));
										label25.setName("label");
										pnlTop41.add(label25, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblPumpType41 ----
										lblPumpType41.setText("type");
										lblPumpType41.setFont(new Font("Arial", Font.PLAIN, 12));
										lblPumpType41.setName("declared");
										pnlTop41.add(lblPumpType41, new TableLayoutConstraints(2, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- label37 ----
										label37.setText("RATING (KW/HP)");
										label37.setFont(new Font("Arial", Font.BOLD, 12));
										label37.setName("label");
										pnlTop41.add(label37, new TableLayoutConstraints(3, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblMRt41 ----
										lblMRt41.setText("mrt");
										lblMRt41.setFont(new Font("Arial", Font.PLAIN, 12));
										lblMRt41.setName("declared");
										pnlTop41.add(lblMRt41, new TableLayoutConstraints(4, 1, 4, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- label52 ----
										label52.setText("FREQUENCY (Hz)");
										label52.setFont(new Font("Arial", Font.BOLD, 12));
										label52.setName("label");
										pnlTop41.add(label52, new TableLayoutConstraints(5, 1, 5, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblFreq41 ----
										lblFreq41.setText("freq");
										lblFreq41.setFont(new Font("Arial", Font.PLAIN, 12));
										lblFreq41.setName("declared");
										pnlTop41.add(lblFreq41, new TableLayoutConstraints(6, 1, 6, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lbllblPSize41 ----
										lbllblPSize41.setText("PIPE SIZE (S X D) (mm)");
										lbllblPSize41.setFont(new Font("Arial", Font.BOLD, 12));
										lbllblPSize41.setName("label");
										pnlTop41.add(lbllblPSize41, new TableLayoutConstraints(7, 1, 7, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblPSz41 ----
										lblPSz41.setText("psize");
										lblPSz41.setFont(new Font("Arial", Font.PLAIN, 12));
										lblPSz41.setName("declared");
										pnlTop41.add(lblPSz41, new TableLayoutConstraints(8, 1, 8, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- label43 ----
										label43.setText("PUMP SNO.");
										label43.setFont(new Font("Arial", Font.BOLD, 12));
										label43.setName("label");
										pnlTop41.add(label43, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblPSno41 ----
										lblPSno41.setText("slno");
										lblPSno41.setFont(new Font("Arial", Font.PLAIN, 12));
										lblPSno41.setName("declared");
										pnlTop41.add(lblPSno41, new TableLayoutConstraints(2, 2, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- label50 ----
										label50.setText("VOLTAGE (V)");
										label50.setFont(new Font("Arial", Font.BOLD, 12));
										label50.setName("label");
										pnlTop41.add(label50, new TableLayoutConstraints(3, 2, 3, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblV41 ----
										lblV41.setText("v");
										lblV41.setFont(new Font("Arial", Font.PLAIN, 12));
										lblV41.setName("declared");
										pnlTop41.add(lblV41, new TableLayoutConstraints(4, 2, 4, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- label224 ----
										label224.setText("SPEED (rpm)");
										label224.setFont(new Font("Arial", Font.BOLD, 12));
										label224.setName("label");
										pnlTop41.add(label224, new TableLayoutConstraints(5, 2, 5, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblSpeed41 ----
										lblSpeed41.setText("speed");
										lblSpeed41.setFont(new Font("Arial", Font.PLAIN, 12));
										lblSpeed41.setName("declared");
										pnlTop41.add(lblSpeed41, new TableLayoutConstraints(6, 2, 6, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblHdLbl41 ----
										lblHdLbl41.setText("HEAD (m)");
										lblHdLbl41.setFont(new Font("Arial", Font.BOLD, 12));
										lblHdLbl41.setName("label");
										pnlTop41.add(lblHdLbl41, new TableLayoutConstraints(7, 2, 7, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblHead41 ----
										lblHead41.setText("head");
										lblHead41.setFont(new Font("Arial", Font.PLAIN, 12));
										lblHead41.setName("declared");
										pnlTop41.add(lblHead41, new TableLayoutConstraints(8, 2, 8, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- label39 ----
										label39.setText("PHASE");
										label39.setFont(new Font("Arial", Font.BOLD, 12));
										label39.setName("label");
										pnlTop41.add(label39, new TableLayoutConstraints(1, 3, 1, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblPh41 ----
										lblPh41.setText("ph");
										lblPh41.setFont(new Font("Arial", Font.PLAIN, 12));
										lblPh41.setName("declared");
										pnlTop41.add(lblPh41, new TableLayoutConstraints(2, 3, 2, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- label53 ----
										label53.setText("MAX. CURRENT (A)");
										label53.setFont(new Font("Arial", Font.BOLD, 12));
										label53.setName("label");
										pnlTop41.add(label53, new TableLayoutConstraints(3, 3, 3, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblCur41 ----
										lblCur41.setText("a");
										lblCur41.setFont(new Font("Arial", Font.PLAIN, 12));
										lblCur41.setName("declared");
										pnlTop41.add(lblCur41, new TableLayoutConstraints(4, 3, 4, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- label204 ----
										label204.setText("NO. OF POLES");
										label204.setFont(new Font("Arial", Font.BOLD, 12));
										label204.setName("label");
										pnlTop41.add(label204, new TableLayoutConstraints(5, 3, 5, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblPoles41 ----
										lblPoles41.setText("poles");
										lblPoles41.setFont(new Font("Arial", Font.PLAIN, 12));
										lblPoles41.setName("declared");
										pnlTop41.add(lblPoles41, new TableLayoutConstraints(6, 3, 6, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblFlowRtLabel41 ----
										lblFlowRtLabel41.setText("DISCHARGE (unit)");
										lblFlowRtLabel41.setFont(new Font("Arial", Font.BOLD, 12));
										lblFlowRtLabel41.setName("label");
										pnlTop41.add(lblFlowRtLabel41, new TableLayoutConstraints(7, 3, 7, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblFlow41 ----
										lblFlow41.setText("flow");
										lblFlow41.setFont(new Font("Arial", Font.PLAIN, 12));
										lblFlow41.setName("declared");
										pnlTop41.add(lblFlow41, new TableLayoutConstraints(8, 3, 8, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- label160 ----
										label160.setText("CONNECTION");
										label160.setFont(new Font("Arial", Font.BOLD, 12));
										label160.setName("label");
										pnlTop41.add(label160, new TableLayoutConstraints(1, 4, 1, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblCon41 ----
										lblCon41.setText("con");
										lblCon41.setFont(new Font("Arial", Font.PLAIN, 12));
										lblCon41.setName("declared");
										pnlTop41.add(lblCon41, new TableLayoutConstraints(2, 4, 2, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lbllblTopMI41 ----
										lbllblTopMI41.setText("MOTOR IP (kW)");
										lbllblTopMI41.setFont(new Font("Arial", Font.BOLD, 12));
										lbllblTopMI41.setName("label");
										pnlTop41.add(lbllblTopMI41, new TableLayoutConstraints(3, 4, 3, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblMI41 ----
										lblMI41.setText("mi");
										lblMI41.setFont(new Font("Arial", Font.PLAIN, 12));
										lblMI41.setName("declared");
										pnlTop41.add(lblMI41, new TableLayoutConstraints(4, 4, 4, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lbllblGDis41 ----
										lbllblGDis41.setText("GAUGE DIST. (m)");
										lbllblGDis41.setFont(new Font("Arial", Font.BOLD, 12));
										lbllblGDis41.setName("label");
										pnlTop41.add(lbllblGDis41, new TableLayoutConstraints(5, 4, 5, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblGDis41 ----
										lblGDis41.setText("gg");
										lblGDis41.setFont(new Font("Arial", Font.PLAIN, 12));
										lblGDis41.setName("declared");
										pnlTop41.add(lblGDis41, new TableLayoutConstraints(6, 4, 6, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblHdRglbl41 ----
										lblHdRglbl41.setText("HEAD RANGE (m)");
										lblHdRglbl41.setFont(new Font("Arial", Font.BOLD, 12));
										lblHdRglbl41.setName("label");
										pnlTop41.add(lblHdRglbl41, new TableLayoutConstraints(7, 4, 7, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblHeadR41 ----
										lblHeadR41.setText("headrange");
										lblHeadR41.setFont(new Font("Arial", Font.PLAIN, 12));
										lblHeadR41.setName("declared");
										pnlTop41.add(lblHeadR41, new TableLayoutConstraints(8, 4, 8, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lbllblMotType41 ----
										lbllblMotType41.setText("MOTOR TYPE");
										lbllblMotType41.setFont(new Font("Arial", Font.BOLD, 12));
										lbllblMotType41.setName("label");
										pnlTop41.add(lbllblMotType41, new TableLayoutConstraints(1, 5, 1, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblMotType41 ----
										lblMotType41.setText("type");
										lblMotType41.setFont(new Font("Arial", Font.PLAIN, 12));
										lblMotType41.setName("declared");
										pnlTop41.add(lblMotType41, new TableLayoutConstraints(2, 5, 2, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lbllblMSno41 ----
										lbllblMSno41.setText("MOTOR SNO.");
										lbllblMSno41.setFont(new Font("Arial", Font.BOLD, 12));
										lbllblMSno41.setName("label");
										pnlTop41.add(lbllblMSno41, new TableLayoutConstraints(3, 5, 3, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblMSno41 ----
										lblMSno41.setText("mslno");
										lblMSno41.setFont(new Font("Arial", Font.PLAIN, 12));
										lblMSno41.setName("declared");
										pnlTop41.add(lblMSno41, new TableLayoutConstraints(4, 5, 4, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lbllblMotEff41 ----
										lbllblMotEff41.setText("MOTOR EFF. (%)");
										lbllblMotEff41.setFont(new Font("Arial", Font.BOLD, 12));
										lbllblMotEff41.setName("label");
										pnlTop41.add(lbllblMotEff41, new TableLayoutConstraints(5, 5, 5, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblMotEff41 ----
										lblMotEff41.setText("eff");
										lblMotEff41.setFont(new Font("Arial", Font.PLAIN, 12));
										lblMotEff41.setName("declared");
										pnlTop41.add(lblMotEff41, new TableLayoutConstraints(6, 5, 6, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lbllblOE41 ----
										lbllblOE41.setText("OVERALL EFF. (%)");
										lbllblOE41.setFont(new Font("Arial", Font.BOLD, 12));
										lbllblOE41.setName("label");
										pnlTop41.add(lbllblOE41, new TableLayoutConstraints(7, 5, 7, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblOEff41 ----
										lblOEff41.setText("eff");
										lblOEff41.setFont(new Font("Arial", Font.PLAIN, 12));
										lblOEff41.setName("declared");
										pnlTop41.add(lblOEff41, new TableLayoutConstraints(8, 5, 8, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lbllblIns41 ----
										lbllblIns41.setText("INSUL. CLASS");
										lbllblIns41.setFont(new Font("Arial", Font.BOLD, 12));
										lbllblIns41.setName("label");
										pnlTop41.add(lbllblIns41, new TableLayoutConstraints(1, 6, 1, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblIns41 ----
										lblIns41.setText("ins");
										lblIns41.setFont(new Font("Arial", Font.PLAIN, 12));
										lblIns41.setName("declared");
										pnlTop41.add(lblIns41, new TableLayoutConstraints(2, 6, 2, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lbllblPipeConst41 ----
										lbllblPipeConst41.setText("PIPE CONST. (m)");
										lbllblPipeConst41.setFont(new Font("Arial", Font.BOLD, 12));
										lbllblPipeConst41.setName("label");
										pnlTop41.add(lbllblPipeConst41, new TableLayoutConstraints(3, 6, 3, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblPipeConst41 ----
										lblPipeConst41.setText("pc");
										lblPipeConst41.setFont(new Font("Arial", Font.PLAIN, 12));
										lblPipeConst41.setName("declared");
										pnlTop41.add(lblPipeConst41, new TableLayoutConstraints(4, 6, 4, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lbllblSPTime41 ----
										lbllblSPTime41.setText("SELF PRIM. TIME (sec)");
										lbllblSPTime41.setFont(new Font("Arial", Font.BOLD, 12));
										lbllblSPTime41.setName("label");
										pnlTop41.add(lbllblSPTime41, new TableLayoutConstraints(5, 6, 5, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblSPTime41 ----
										lblSPTime41.setText("sp");
										lblSPTime41.setFont(new Font("Arial", Font.PLAIN, 12));
										lblSPTime41.setName("declared");
										pnlTop41.add(lblSPTime41, new TableLayoutConstraints(6, 6, 6, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lbllblBrSz41 ----
										lbllblBrSz41.setText("MIN. BORE SZ. (mm)");
										lbllblBrSz41.setFont(new Font("Arial", Font.BOLD, 12));
										lbllblBrSz41.setName("label");
										pnlTop41.add(lbllblBrSz41, new TableLayoutConstraints(7, 6, 7, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblBrSz41 ----
										lblBrSz41.setText("br");
										lblBrSz41.setFont(new Font("Arial", Font.PLAIN, 12));
										lblBrSz41.setName("declared");
										pnlTop41.add(lblBrSz41, new TableLayoutConstraints(8, 6, 8, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lbllblCapRat41 ----
										lbllblCapRat41.setText("CAP. RATING (uF)");
										lbllblCapRat41.setFont(new Font("Arial", Font.BOLD, 12));
										lbllblCapRat41.setName("label");
										pnlTop41.add(lbllblCapRat41, new TableLayoutConstraints(1, 7, 1, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblCapRate41 ----
										lblCapRate41.setText("caprate");
										lblCapRate41.setFont(new Font("Arial", Font.PLAIN, 12));
										lblCapRate41.setName("declared");
										pnlTop41.add(lblCapRate41, new TableLayoutConstraints(2, 7, 2, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lbllblCapVolt41 ----
										lbllblCapVolt41.setText("CAP. VOLTAGE (V)");
										lbllblCapVolt41.setFont(new Font("Arial", Font.BOLD, 12));
										lbllblCapVolt41.setName("label");
										pnlTop41.add(lbllblCapVolt41, new TableLayoutConstraints(3, 7, 3, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblCapVolt41 ----
										lblCapVolt41.setText("capv");
										lblCapVolt41.setFont(new Font("Arial", Font.PLAIN, 12));
										lblCapVolt41.setName("declared");
										pnlTop41.add(lblCapVolt41, new TableLayoutConstraints(4, 7, 4, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lbllblNoStg41 ----
										lbllblNoStg41.setText("NO. OF STAGES");
										lbllblNoStg41.setFont(new Font("Arial", Font.BOLD, 12));
										lbllblNoStg41.setName("label");
										pnlTop41.add(lbllblNoStg41, new TableLayoutConstraints(5, 7, 5, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblNoStg41 ----
										lblNoStg41.setText("stz");
										lblNoStg41.setFont(new Font("Arial", Font.PLAIN, 12));
										lblNoStg41.setName("declared");
										pnlTop41.add(lblNoStg41, new TableLayoutConstraints(6, 7, 6, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
									}
									pnlPrint41.add(pnlTop41, new TableLayoutConstraints(1, 8, 4, 8, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//======== pnlPrime41 ========
									{
										pnlPrime41.setBackground(Color.black);
										pnlPrime41.setAutoscrolls(true);
										pnlPrime41.setBorder(LineBorder.createBlackLineBorder());
										pnlPrime41.setName("outline");
										pnlPrime41.setLayout(new TableLayout(new double[][] {
											{34, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL},
											{20, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL}}));
										((TableLayout)pnlPrime41.getLayout()).setHGap(2);
										((TableLayout)pnlPrime41.getLayout()).setVGap(2);

										//---- label149 ----
										label149.setText("<html>Test<br>SNo.</html>");
										label149.setBackground(Color.white);
										label149.setHorizontalAlignment(SwingConstants.CENTER);
										label149.setFont(new Font("Arial", Font.BOLD, 11));
										label149.setOpaque(true);
										label149.setBorder(null);
										label149.setName("label");
										pnlPrime41.add(label149, new TableLayoutConstraints(0, 0, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- label150 ----
										label150.setText("Freq.");
										label150.setBackground(Color.white);
										label150.setHorizontalAlignment(SwingConstants.CENTER);
										label150.setFont(new Font("Arial", Font.BOLD, 11));
										label150.setOpaque(true);
										label150.setBorder(null);
										label150.setName("label");
										pnlPrime41.add(label150, new TableLayoutConstraints(1, 0, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- label225 ----
										label225.setText("Speed");
										label225.setFont(new Font("Arial", Font.BOLD, 11));
										label225.setHorizontalAlignment(SwingConstants.CENTER);
										label225.setOpaque(true);
										label225.setBackground(Color.white);
										label225.setName("label");
										pnlPrime41.add(label225, new TableLayoutConstraints(2, 0, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- label56 ----
										label56.setText("HEAD");
										label56.setBackground(Color.white);
										label56.setOpaque(true);
										label56.setFont(new Font("Arial", Font.BOLD, 11));
										label56.setHorizontalAlignment(SwingConstants.CENTER);
										label56.setBorder(null);
										label56.setName("label");
										pnlPrime41.add(label56, new TableLayoutConstraints(3, 0, 6, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- label151 ----
										label151.setText("FLOW");
										label151.setBackground(Color.white);
										label151.setHorizontalAlignment(SwingConstants.CENTER);
										label151.setFont(new Font("Arial", Font.BOLD, 11));
										label151.setOpaque(true);
										label151.setAutoscrolls(true);
										label151.setBorder(null);
										label151.setName("label");
										pnlPrime41.add(label151, new TableLayoutConstraints(7, 0, 7, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- label152 ----
										label152.setText("MOTOR POWER INPUTS");
										label152.setBackground(Color.white);
										label152.setHorizontalAlignment(SwingConstants.CENTER);
										label152.setFont(new Font("Arial", Font.BOLD, 11));
										label152.setOpaque(true);
										label152.setAutoscrolls(true);
										label152.setBorder(null);
										label152.setName("label");
										pnlPrime41.add(label152, new TableLayoutConstraints(8, 0, 10, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblTitSHd41 ----
										lblTitSHd41.setText("<html><body align=\"center\">Suction Head</body></html>");
										lblTitSHd41.setBackground(Color.white);
										lblTitSHd41.setHorizontalAlignment(SwingConstants.CENTER);
										lblTitSHd41.setFont(new Font("Arial", Font.BOLD, 11));
										lblTitSHd41.setOpaque(true);
										lblTitSHd41.setBorder(null);
										lblTitSHd41.setName("label");
										pnlPrime41.add(lblTitSHd41, new TableLayoutConstraints(3, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblRem41 ----
										lblRem41.setText("Remark");
										lblRem41.setBackground(Color.white);
										lblRem41.setFont(new Font("Arial", Font.BOLD, 10));
										lblRem41.setOpaque(true);
										lblRem41.setHorizontalAlignment(SwingConstants.CENTER);
										lblRem41.setBorder(null);
										lblRem41.setName("label");
										pnlPrime41.add(lblRem41, new TableLayoutConstraints(14, 0, 14, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblPerfHz41 ----
										lblPerfHz41.setText("Performance At Rated Freq X Hz");
										lblPerfHz41.setBackground(Color.white);
										lblPerfHz41.setHorizontalAlignment(SwingConstants.CENTER);
										lblPerfHz41.setFont(new Font("Arial", Font.BOLD, 11));
										lblPerfHz41.setOpaque(true);
										lblPerfHz41.setAutoscrolls(true);
										lblPerfHz41.setBorder(null);
										lblPerfHz41.setName("actual");
										pnlPrime41.add(lblPerfHz41, new TableLayoutConstraints(11, 0, 13, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- label338 ----
										label338.setText("<html><body align=\"center\">Suction Gauge Reading</body></html>");
										label338.setBackground(Color.white);
										label338.setHorizontalAlignment(SwingConstants.CENTER);
										label338.setFont(new Font("Arial", Font.BOLD, 11));
										label338.setOpaque(true);
										label338.setBorder(null);
										pnlPrime41.add(label338, new TableLayoutConstraints(3, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- label339 ----
										label339.setText("<html><body align=\"center\">Delivery Head</body></html>");
										label339.setBackground(Color.white);
										label339.setHorizontalAlignment(SwingConstants.CENTER);
										label339.setFont(new Font("Arial", Font.BOLD, 11));
										label339.setOpaque(true);
										label339.setBorder(null);
										label339.setName("label");
										pnlPrime41.add(label339, new TableLayoutConstraints(4, 1, 4, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblTitVHC41 ----
										lblTitVHC41.setText("<html><body align=\"center\">Velocity Head Corr.</body></html>");
										lblTitVHC41.setBackground(Color.white);
										lblTitVHC41.setHorizontalAlignment(SwingConstants.CENTER);
										lblTitVHC41.setFont(new Font("Arial", Font.BOLD, 11));
										lblTitVHC41.setOpaque(true);
										lblTitVHC41.setBorder(null);
										lblTitVHC41.setName("label");
										pnlPrime41.add(lblTitVHC41, new TableLayoutConstraints(5, 1, 5, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblTitTotHd41 ----
										lblTitTotHd41.setText("<html><body align=\"center\">Total Head</body></html>");
										lblTitTotHd41.setBackground(Color.white);
										lblTitTotHd41.setHorizontalAlignment(SwingConstants.CENTER);
										lblTitTotHd41.setFont(new Font("Arial", Font.BOLD, 11));
										lblTitTotHd41.setOpaque(true);
										lblTitTotHd41.setBorder(null);
										lblTitTotHd41.setName("label");
										pnlPrime41.add(lblTitTotHd41, new TableLayoutConstraints(6, 1, 6, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- label157 ----
										label157.setText("<html><body align=\"center\">Flow</body></html>");
										label157.setBackground(Color.white);
										label157.setHorizontalAlignment(SwingConstants.CENTER);
										label157.setFont(new Font("Arial", Font.BOLD, 11));
										label157.setOpaque(true);
										label157.setAutoscrolls(true);
										label157.setBorder(null);
										label157.setName("label");
										pnlPrime41.add(label157, new TableLayoutConstraints(7, 1, 7, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- label158 ----
										label158.setText("Voltage");
										label158.setBackground(Color.white);
										label158.setHorizontalAlignment(SwingConstants.CENTER);
										label158.setFont(new Font("Arial", Font.BOLD, 11));
										label158.setOpaque(true);
										label158.setAutoscrolls(true);
										label158.setBorder(null);
										label158.setName("label");
										pnlPrime41.add(label158, new TableLayoutConstraints(8, 1, 8, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- label162 ----
										label162.setText("Current");
										label162.setBackground(Color.white);
										label162.setHorizontalAlignment(SwingConstants.CENTER);
										label162.setFont(new Font("Arial", Font.BOLD, 11));
										label162.setOpaque(true);
										label162.setAutoscrolls(true);
										label162.setBorder(null);
										label162.setName("label");
										pnlPrime41.add(label162, new TableLayoutConstraints(9, 1, 9, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- label164 ----
										label164.setText("Power");
										label164.setBackground(Color.white);
										label164.setHorizontalAlignment(SwingConstants.CENTER);
										label164.setFont(new Font("Arial", Font.BOLD, 11));
										label164.setOpaque(true);
										label164.setAutoscrolls(true);
										label164.setBorder(null);
										label164.setName("label");
										pnlPrime41.add(label164, new TableLayoutConstraints(10, 1, 10, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblTitFlow41 ----
										lblTitFlow41.setText("Flow");
										lblTitFlow41.setBackground(Color.white);
										lblTitFlow41.setHorizontalAlignment(SwingConstants.CENTER);
										lblTitFlow41.setFont(new Font("Arial", Font.BOLD, 11));
										lblTitFlow41.setOpaque(true);
										lblTitFlow41.setAutoscrolls(true);
										lblTitFlow41.setBorder(null);
										lblTitFlow41.setName("label");
										pnlPrime41.add(lblTitFlow41, new TableLayoutConstraints(11, 1, 11, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- label178 ----
										label178.setText("<html><body align=\"center\">Total Head</body></html>");
										label178.setBackground(Color.white);
										label178.setHorizontalAlignment(SwingConstants.CENTER);
										label178.setFont(new Font("Arial", Font.BOLD, 11));
										label178.setOpaque(true);
										label178.setAutoscrolls(true);
										label178.setBorder(null);
										label178.setName("label");
										pnlPrime41.add(label178, new TableLayoutConstraints(12, 1, 12, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblTitMI41 ----
										lblTitMI41.setText("<html><body align=\"center\">Motor<br>Input</body></html>");
										lblTitMI41.setBackground(Color.white);
										lblTitMI41.setHorizontalAlignment(SwingConstants.CENTER);
										lblTitMI41.setFont(new Font("Arial", Font.BOLD, 11));
										lblTitMI41.setOpaque(true);
										lblTitMI41.setAutoscrolls(true);
										lblTitMI41.setBorder(null);
										lblTitMI41.setName("label");
										pnlPrime41.add(lblTitMI41, new TableLayoutConstraints(13, 1, 13, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- label180 ----
										label180.setText("Hz");
										label180.setBackground(Color.white);
										label180.setHorizontalAlignment(SwingConstants.CENTER);
										label180.setFont(new Font("Arial", Font.BOLD, 11));
										label180.setOpaque(true);
										label180.setAutoscrolls(true);
										label180.setBorder(null);
										label180.setName("label");
										pnlPrime41.add(label180, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- label226 ----
										label226.setText("rpm");
										label226.setFont(new Font("Arial", Font.BOLD, 11));
										label226.setHorizontalAlignment(SwingConstants.CENTER);
										label226.setOpaque(true);
										label226.setBackground(Color.white);
										label226.setName("label");
										pnlPrime41.add(label226, new TableLayoutConstraints(2, 2, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblSHd41 ----
										lblSHd41.setText("mwc");
										lblSHd41.setBackground(Color.white);
										lblSHd41.setHorizontalAlignment(SwingConstants.CENTER);
										lblSHd41.setFont(new Font("Arial", Font.BOLD, 11));
										lblSHd41.setOpaque(true);
										lblSHd41.setAutoscrolls(true);
										lblSHd41.setBorder(null);
										lblSHd41.setName("label");
										pnlPrime41.add(lblSHd41, new TableLayoutConstraints(3, 2, 3, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblDHd41 ----
										lblDHd41.setText("mwc");
										lblDHd41.setBackground(Color.white);
										lblDHd41.setHorizontalAlignment(SwingConstants.CENTER);
										lblDHd41.setFont(new Font("Arial", Font.BOLD, 11));
										lblDHd41.setOpaque(true);
										lblDHd41.setAutoscrolls(true);
										lblDHd41.setBorder(null);
										lblDHd41.setName("label");
										pnlPrime41.add(lblDHd41, new TableLayoutConstraints(4, 2, 4, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblVHd41 ----
										lblVHd41.setText("mtrs");
										lblVHd41.setBackground(Color.white);
										lblVHd41.setHorizontalAlignment(SwingConstants.CENTER);
										lblVHd41.setFont(new Font("Arial", Font.BOLD, 11));
										lblVHd41.setOpaque(true);
										lblVHd41.setAutoscrolls(true);
										lblVHd41.setBorder(null);
										lblVHd41.setName("label");
										pnlPrime41.add(lblVHd41, new TableLayoutConstraints(5, 2, 5, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblTotHd41 ----
										lblTotHd41.setText("mtrs");
										lblTotHd41.setBackground(Color.white);
										lblTotHd41.setHorizontalAlignment(SwingConstants.CENTER);
										lblTotHd41.setFont(new Font("Arial", Font.BOLD, 11));
										lblTotHd41.setOpaque(true);
										lblTotHd41.setAutoscrolls(true);
										lblTotHd41.setBorder(null);
										lblTotHd41.setName("label");
										pnlPrime41.add(lblTotHd41, new TableLayoutConstraints(6, 2, 6, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblFlowUnitS41 ----
										lblFlowUnitS41.setText("unit");
										lblFlowUnitS41.setBackground(Color.white);
										lblFlowUnitS41.setHorizontalAlignment(SwingConstants.CENTER);
										lblFlowUnitS41.setFont(new Font("Arial", Font.BOLD, 11));
										lblFlowUnitS41.setOpaque(true);
										lblFlowUnitS41.setAutoscrolls(true);
										lblFlowUnitS41.setBorder(null);
										lblFlowUnitS41.setName("label");
										pnlPrime41.add(lblFlowUnitS41, new TableLayoutConstraints(7, 2, 7, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- label166 ----
										label166.setText("V");
										label166.setBackground(Color.white);
										label166.setHorizontalAlignment(SwingConstants.CENTER);
										label166.setFont(new Font("Arial", Font.BOLD, 11));
										label166.setOpaque(true);
										label166.setAutoscrolls(true);
										label166.setBorder(null);
										label166.setName("label");
										pnlPrime41.add(label166, new TableLayoutConstraints(8, 2, 8, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- label167 ----
										label167.setText("A");
										label167.setBackground(Color.white);
										label167.setHorizontalAlignment(SwingConstants.CENTER);
										label167.setFont(new Font("Arial", Font.BOLD, 11));
										label167.setOpaque(true);
										label167.setAutoscrolls(true);
										label167.setBorder(null);
										label167.setName("label");
										pnlPrime41.add(label167, new TableLayoutConstraints(9, 2, 9, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- label168 ----
										label168.setText("kW");
										label168.setBackground(Color.white);
										label168.setHorizontalAlignment(SwingConstants.CENTER);
										label168.setFont(new Font("Arial", Font.BOLD, 11));
										label168.setOpaque(true);
										label168.setAutoscrolls(true);
										label168.setBorder(null);
										label168.setName("label");
										pnlPrime41.add(label168, new TableLayoutConstraints(10, 2, 10, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblFlowUnitT41 ----
										lblFlowUnitT41.setText("unit");
										lblFlowUnitT41.setBackground(Color.white);
										lblFlowUnitT41.setHorizontalAlignment(SwingConstants.CENTER);
										lblFlowUnitT41.setFont(new Font("Arial", Font.BOLD, 11));
										lblFlowUnitT41.setOpaque(true);
										lblFlowUnitT41.setAutoscrolls(true);
										lblFlowUnitT41.setBorder(null);
										lblFlowUnitT41.setName("label");
										pnlPrime41.add(lblFlowUnitT41, new TableLayoutConstraints(11, 2, 11, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblTotHdT41 ----
										lblTotHdT41.setText("mtrs");
										lblTotHdT41.setBackground(Color.white);
										lblTotHdT41.setHorizontalAlignment(SwingConstants.CENTER);
										lblTotHdT41.setFont(new Font("Arial", Font.BOLD, 11));
										lblTotHdT41.setOpaque(true);
										lblTotHdT41.setAutoscrolls(true);
										lblTotHdT41.setBorder(null);
										lblTotHdT41.setName("label");
										pnlPrime41.add(lblTotHdT41, new TableLayoutConstraints(12, 2, 12, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblMITitUnit41 ----
										lblMITitUnit41.setText("kW");
										lblMITitUnit41.setBackground(Color.white);
										lblMITitUnit41.setHorizontalAlignment(SwingConstants.CENTER);
										lblMITitUnit41.setFont(new Font("Arial", Font.BOLD, 11));
										lblMITitUnit41.setOpaque(true);
										lblMITitUnit41.setAutoscrolls(true);
										lblMITitUnit41.setBorder(null);
										lblMITitUnit41.setName("label");
										pnlPrime41.add(lblMITitUnit41, new TableLayoutConstraints(13, 2, 13, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- tblRes41 ----
										tblRes41.setModel(new DefaultTableModel(
											new Object[][] {
												{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
												{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
												{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
												{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
												{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
												{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
												{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
												{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
												{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
												{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
											},
											new String[] {
												null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
											}
										) {
											Class<?>[] columnTypes = new Class<?>[] {
												String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class
											};
											@Override
											public Class<?> getColumnClass(int columnIndex) {
												return columnTypes[columnIndex];
											}
										});
										{
											TableColumnModel cm = tblRes41.getColumnModel();
											cm.getColumn(0).setMinWidth(35);
											cm.getColumn(0).setMaxWidth(35);
											cm.getColumn(0).setPreferredWidth(35);
										}
										tblRes41.setFont(new Font("Arial", Font.PLAIN, 12));
										tblRes41.setRowHeight(25);
										tblRes41.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
										tblRes41.setGridColor(Color.lightGray);
										tblRes41.setBorder(new LineBorder(Color.white));
										tblRes41.setRowSelectionAllowed(false);
										tblRes41.setAutoscrolls(false);
										tblRes41.setFocusable(false);
										tblRes41.setEnabled(false);
										tblRes41.setName("outline");
										tblRes41.setIntercellSpacing(new Dimension(3, 1));
										pnlPrime41.add(tblRes41, new TableLayoutConstraints(0, 3, 14, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
									}
									pnlPrint41.add(pnlPrime41, new TableLayoutConstraints(1, 9, 4, 9, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//======== pnlGraph41 ========
									{
										pnlGraph41.setBackground(Color.white);
										pnlGraph41.setAutoscrolls(true);
										pnlGraph41.setBorder(LineBorder.createBlackLineBorder());
										pnlGraph41.setName("outline");
										pnlGraph41.setLayout(new TableLayout(new double[][] {
											{TableLayout.FILL},
											{TableLayout.FILL}}));
										((TableLayout)pnlGraph41.getLayout()).setHGap(1);
										((TableLayout)pnlGraph41.getLayout()).setVGap(1);
									}
									pnlPrint41.add(pnlGraph41, new TableLayoutConstraints(1, 10, 4, 10, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//======== pnlBot41 ========
									{
										pnlBot41.setBackground(Color.white);
										pnlBot41.setBorder(LineBorder.createBlackLineBorder());
										pnlBot41.setName("outline");
										pnlBot41.setLayout(new TableLayout(new double[][] {
											{5, 36, 320, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL},
											{5, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5}}));
										((TableLayout)pnlBot41.getLayout()).setHGap(5);
										((TableLayout)pnlBot41.getLayout()).setVGap(5);

										//---- lblCas41 ----
										lblCas41.setText("Hydrostatic Pressure Test:");
										lblCas41.setFont(new Font("Arial", Font.BOLD, 12));
										lblCas41.setName("label");
										pnlBot41.add(lblCas41, new TableLayoutConstraints(1, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblGC41 ----
										lblGC41.setText("               Note:");
										lblGC41.setFont(new Font("Arial", Font.BOLD, 12));
										lblGC41.setName("label");
										pnlBot41.add(lblGC41, new TableLayoutConstraints(3, 1, 5, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblMaxDis41 ----
										lblMaxDis41.setText("Maximum Discharge Pressure");
										lblMaxDis41.setFont(new Font("Arial", Font.PLAIN, 12));
										lblMaxDis41.setName("actual");
										pnlBot41.add(lblMaxDis41, new TableLayoutConstraints(1, 2, 3, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblGCText41 ----
										lblGCText41.setText("               Your note goes here");
										lblGCText41.setFont(new Font("Arial", Font.PLAIN, 12));
										lblGCText41.setName("actual");
										pnlBot41.add(lblGCText41, new TableLayoutConstraints(3, 2, 5, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblWith41 ----
										lblWith41.setText("Withstood this pressure by 2 minutes.");
										lblWith41.setFont(new Font("Arial", Font.PLAIN, 12));
										lblWith41.setName("actual");
										pnlBot41.add(lblWith41, new TableLayoutConstraints(1, 3, 2, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblVerBy10 ----
										lblVerBy10.setText("Verified By");
										lblVerBy10.setFont(new Font("Arial", Font.PLAIN, 13));
										lblVerBy10.setHorizontalAlignment(SwingConstants.CENTER);
										pnlBot41.add(lblVerBy10, new TableLayoutConstraints(0, 6, 0, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblDt41 ----
										lblDt41.setText("Dt");
										lblDt41.setFont(new Font("Arial", Font.PLAIN, 12));
										lblDt41.setName("label");
										pnlBot41.add(lblDt41, new TableLayoutConstraints(2, 5, 2, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblTestedBy41 ----
										lblTestedBy41.setText("Tested By");
										lblTestedBy41.setFont(new Font("Arial", Font.PLAIN, 13));
										lblTestedBy41.setHorizontalAlignment(SwingConstants.CENTER);
										lblTestedBy41.setName("label");
										pnlBot41.add(lblTestedBy41, new TableLayoutConstraints(3, 5, 3, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblVerBy11 ----
										lblVerBy11.setText("Verified By");
										lblVerBy11.setFont(new Font("Arial", Font.PLAIN, 13));
										lblVerBy11.setHorizontalAlignment(SwingConstants.CENTER);
										lblVerBy11.setName("label");
										pnlBot41.add(lblVerBy11, new TableLayoutConstraints(4, 5, 4, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- label67 ----
										label67.setText("Approved By");
										label67.setFont(new Font("Arial", Font.PLAIN, 13));
										label67.setHorizontalAlignment(SwingConstants.CENTER);
										label67.setName("label");
										pnlBot41.add(label67, new TableLayoutConstraints(5, 5, 5, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lbllblDt41 ----
										lbllblDt41.setText("Date:");
										lbllblDt41.setFont(new Font("Arial", Font.BOLD, 13));
										lbllblDt41.setHorizontalAlignment(SwingConstants.CENTER);
										lbllblDt41.setName("label");
										pnlBot41.add(lbllblDt41, new TableLayoutConstraints(1, 5, 1, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
									}
									pnlPrint41.add(pnlBot41, new TableLayoutConstraints(1, 13, 4, 13, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//======== pnlBotTwo41 ========
									{
										pnlBotTwo41.setBorder(LineBorder.createBlackLineBorder());
										pnlBotTwo41.setBackground(Color.black);
										pnlBotTwo41.setFont(new Font("Arial", Font.BOLD, 12));
										pnlBotTwo41.setName("outline");
										pnlBotTwo41.setLayout(new TableLayout(new double[][] {
											{105, 90, 138, 84},
											{45, 25, 25, 25}}));
										((TableLayout)pnlBotTwo41.getLayout()).setHGap(2);
										((TableLayout)pnlBotTwo41.getLayout()).setVGap(2);

										//---- label68 ----
										label68.setText(" PUMP TYPE");
										label68.setFont(new Font("Arial", Font.BOLD, 12));
										label68.setOpaque(true);
										label68.setHorizontalAlignment(SwingConstants.LEFT);
										label68.setBackground(Color.white);
										label68.setName("label");
										pnlBotTwo41.add(label68, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblPumpType41_2 ----
										lblPumpType41_2.setText(" type");
										lblPumpType41_2.setFont(new Font("Arial", Font.PLAIN, 12));
										lblPumpType41_2.setOpaque(true);
										lblPumpType41_2.setHorizontalAlignment(SwingConstants.LEFT);
										lblPumpType41_2.setBackground(Color.white);
										lblPumpType41_2.setName("declared");
										pnlBotTwo41.add(lblPumpType41_2, new TableLayoutConstraints(1, 0, 3, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- label73 ----
										label73.setText(" PUMP SL. NO.");
										label73.setFont(new Font("Arial", Font.BOLD, 12));
										label73.setOpaque(true);
										label73.setHorizontalAlignment(SwingConstants.LEFT);
										label73.setBackground(Color.white);
										label73.setName("label");
										pnlBotTwo41.add(label73, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblPSno41_2 ----
										lblPSno41_2.setText(" pslno");
										lblPSno41_2.setFont(new Font("Arial", Font.PLAIN, 12));
										lblPSno41_2.setOpaque(true);
										lblPSno41_2.setHorizontalAlignment(SwingConstants.LEFT);
										lblPSno41_2.setBackground(Color.white);
										lblPSno41_2.setName("actual");
										pnlBotTwo41.add(lblPSno41_2, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- label74 ----
										label74.setText(" DATE");
										label74.setFont(new Font("Arial", Font.BOLD, 12));
										label74.setOpaque(true);
										label74.setHorizontalAlignment(SwingConstants.LEFT);
										label74.setBackground(Color.white);
										label74.setName("label");
										pnlBotTwo41.add(label74, new TableLayoutConstraints(2, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblDt41_2 ----
										lblDt41_2.setText(" dt");
										lblDt41_2.setFont(new Font("Arial", Font.PLAIN, 12));
										lblDt41_2.setOpaque(true);
										lblDt41_2.setHorizontalAlignment(SwingConstants.LEFT);
										lblDt41_2.setBackground(Color.white);
										lblDt41_2.setName("actual");
										pnlBotTwo41.add(lblDt41_2, new TableLayoutConstraints(3, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblHdR41_2 ----
										lblHdR41_2.setText(" HEAD RANGE (m)");
										lblHdR41_2.setFont(new Font("Arial", Font.BOLD, 12));
										lblHdR41_2.setOpaque(true);
										lblHdR41_2.setHorizontalAlignment(SwingConstants.LEFT);
										lblHdR41_2.setBackground(Color.white);
										lblHdR41_2.setName("label");
										pnlBotTwo41.add(lblHdR41_2, new TableLayoutConstraints(0, 2, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblHeadR41_2 ----
										lblHeadR41_2.setText(" headrange");
										lblHeadR41_2.setFont(new Font("Arial", Font.PLAIN, 12));
										lblHeadR41_2.setOpaque(true);
										lblHeadR41_2.setHorizontalAlignment(SwingConstants.LEFT);
										lblHeadR41_2.setBackground(Color.white);
										lblHeadR41_2.setName("declared");
										pnlBotTwo41.add(lblHeadR41_2, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lbllblPSize41_2 ----
										lbllblPSize41_2.setText(" PIPE SIZE (S X D) (mm)");
										lbllblPSize41_2.setFont(new Font("Arial", Font.BOLD, 12));
										lbllblPSize41_2.setOpaque(true);
										lbllblPSize41_2.setHorizontalAlignment(SwingConstants.LEFT);
										lbllblPSize41_2.setBackground(Color.white);
										lbllblPSize41_2.setName("label");
										pnlBotTwo41.add(lbllblPSize41_2, new TableLayoutConstraints(2, 2, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblPSz41_2 ----
										lblPSz41_2.setText(" psize");
										lblPSz41_2.setFont(new Font("Arial", Font.PLAIN, 12));
										lblPSz41_2.setOpaque(true);
										lblPSz41_2.setHorizontalAlignment(SwingConstants.LEFT);
										lblPSz41_2.setBackground(Color.white);
										lblPSz41_2.setName("declared");
										pnlBotTwo41.add(lblPSz41_2, new TableLayoutConstraints(3, 2, 3, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lbllblFreq41_2 ----
										lbllblFreq41_2.setText(" FREQUENCY (Hz)");
										lbllblFreq41_2.setFont(new Font("Arial", Font.BOLD, 12));
										lbllblFreq41_2.setOpaque(true);
										lbllblFreq41_2.setHorizontalAlignment(SwingConstants.LEFT);
										lbllblFreq41_2.setBackground(Color.white);
										lbllblFreq41_2.setName("label");
										pnlBotTwo41.add(lbllblFreq41_2, new TableLayoutConstraints(0, 3, 0, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblFreq41_2 ----
										lblFreq41_2.setText(" freq");
										lblFreq41_2.setFont(new Font("Arial", Font.PLAIN, 12));
										lblFreq41_2.setOpaque(true);
										lblFreq41_2.setHorizontalAlignment(SwingConstants.LEFT);
										lblFreq41_2.setBackground(Color.white);
										lblFreq41_2.setName("declared");
										pnlBotTwo41.add(lblFreq41_2, new TableLayoutConstraints(1, 3, 3, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
									}
									pnlPrint41.add(pnlBotTwo41, new TableLayoutConstraints(1, 11, 4, 11, TableLayoutConstraints.LEFT, TableLayoutConstraints.FULL));

									//======== pnlBotRes41 ========
									{
										pnlBotRes41.setBackground(Color.black);
										pnlBotRes41.setBorder(LineBorder.createBlackLineBorder());
										pnlBotRes41.setName("outline");
										pnlBotRes41.setLayout(new TableLayout(new double[][] {
											{TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL},
											{45, 25, 25, 25}}));
										((TableLayout)pnlBotRes41.getLayout()).setHGap(2);
										((TableLayout)pnlBotRes41.getLayout()).setVGap(2);

										//---- label69 ----
										label69.setText(" Duty Point");
										label69.setFont(new Font("Arial", Font.BOLD, 12));
										label69.setOpaque(true);
										label69.setHorizontalAlignment(SwingConstants.LEFT);
										label69.setBackground(Color.white);
										label69.setName("label");
										pnlBotRes41.add(label69, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblFlowUnit41 ----
										lblFlowUnit41.setText("Q (unit)");
										lblFlowUnit41.setFont(new Font("Arial", Font.BOLD, 12));
										lblFlowUnit41.setOpaque(true);
										lblFlowUnit41.setHorizontalAlignment(SwingConstants.CENTER);
										lblFlowUnit41.setBackground(Color.white);
										lblFlowUnit41.setName("label");
										pnlBotRes41.add(lblFlowUnit41, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblHd41 ----
										lblHd41.setText("TH (m)");
										lblHd41.setFont(new Font("Arial", Font.BOLD, 12));
										lblHd41.setOpaque(true);
										lblHd41.setHorizontalAlignment(SwingConstants.CENTER);
										lblHd41.setBackground(Color.white);
										lblHd41.setName("label");
										pnlBotRes41.add(lblHd41, new TableLayoutConstraints(2, 0, 2, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lbllblMI41 ----
										lbllblMI41.setText("MI (kW)");
										lbllblMI41.setFont(new Font("Arial", Font.BOLD, 12));
										lbllblMI41.setOpaque(true);
										lbllblMI41.setHorizontalAlignment(SwingConstants.CENTER);
										lbllblMI41.setBackground(Color.white);
										lbllblMI41.setName("label");
										pnlBotRes41.add(lbllblMI41, new TableLayoutConstraints(3, 0, 3, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lbllblEff41 ----
										lbllblEff41.setText("O.A Eff. (%)");
										lbllblEff41.setFont(new Font("Arial", Font.BOLD, 12));
										lbllblEff41.setOpaque(true);
										lbllblEff41.setHorizontalAlignment(SwingConstants.CENTER);
										lbllblEff41.setBackground(Color.white);
										lbllblEff41.setName("label");
										pnlBotRes41.add(lbllblEff41, new TableLayoutConstraints(4, 0, 4, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lbllblMaxC41 ----
										lbllblMaxC41.setText("<html><body align=\"center\">I-Max in Head Range (A)</body></html>");
										lbllblMaxC41.setFont(new Font("Arial", Font.BOLD, 12));
										lbllblMaxC41.setOpaque(true);
										lbllblMaxC41.setHorizontalAlignment(SwingConstants.CENTER);
										lbllblMaxC41.setBackground(Color.white);
										lbllblMaxC41.setName("label");
										pnlBotRes41.add(lbllblMaxC41, new TableLayoutConstraints(5, 0, 5, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- label75 ----
										label75.setText(" Guarantd.");
										label75.setFont(new Font("Arial", Font.BOLD, 12));
										label75.setOpaque(true);
										label75.setHorizontalAlignment(SwingConstants.LEFT);
										label75.setBackground(Color.white);
										label75.setName("label");
										pnlBotRes41.add(label75, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblFlow41_2 ----
										lblFlow41_2.setText("flow");
										lblFlow41_2.setFont(new Font("Arial", Font.PLAIN, 12));
										lblFlow41_2.setOpaque(true);
										lblFlow41_2.setHorizontalAlignment(SwingConstants.CENTER);
										lblFlow41_2.setBackground(Color.white);
										lblFlow41_2.setName("declared");
										pnlBotRes41.add(lblFlow41_2, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblHead41_2 ----
										lblHead41_2.setText("head");
										lblHead41_2.setFont(new Font("Arial", Font.PLAIN, 12));
										lblHead41_2.setOpaque(true);
										lblHead41_2.setHorizontalAlignment(SwingConstants.CENTER);
										lblHead41_2.setBackground(Color.white);
										lblHead41_2.setName("declared");
										pnlBotRes41.add(lblHead41_2, new TableLayoutConstraints(2, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblMotIp41_2 ----
										lblMotIp41_2.setText("mi");
										lblMotIp41_2.setFont(new Font("Arial", Font.PLAIN, 12));
										lblMotIp41_2.setOpaque(true);
										lblMotIp41_2.setHorizontalAlignment(SwingConstants.CENTER);
										lblMotIp41_2.setBackground(Color.white);
										lblMotIp41_2.setName("declared");
										pnlBotRes41.add(lblMotIp41_2, new TableLayoutConstraints(3, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblEff41_2 ----
										lblEff41_2.setText("eff");
										lblEff41_2.setFont(new Font("Arial", Font.PLAIN, 12));
										lblEff41_2.setOpaque(true);
										lblEff41_2.setHorizontalAlignment(SwingConstants.CENTER);
										lblEff41_2.setBackground(Color.white);
										lblEff41_2.setName("declared");
										pnlBotRes41.add(lblEff41_2, new TableLayoutConstraints(4, 1, 4, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblCur41_2 ----
										lblCur41_2.setText("a");
										lblCur41_2.setFont(new Font("Arial", Font.PLAIN, 12));
										lblCur41_2.setOpaque(true);
										lblCur41_2.setHorizontalAlignment(SwingConstants.CENTER);
										lblCur41_2.setBackground(Color.white);
										lblCur41_2.setName("declared");
										pnlBotRes41.add(lblCur41_2, new TableLayoutConstraints(5, 1, 5, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- label78 ----
										label78.setText(" Actual");
										label78.setFont(new Font("Arial", Font.BOLD, 12));
										label78.setOpaque(true);
										label78.setHorizontalAlignment(SwingConstants.LEFT);
										label78.setBackground(Color.white);
										label78.setName("label");
										pnlBotRes41.add(label78, new TableLayoutConstraints(0, 2, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblFlowAct41 ----
										lblFlowAct41.setText("flow");
										lblFlowAct41.setFont(new Font("Arial", Font.PLAIN, 12));
										lblFlowAct41.setOpaque(true);
										lblFlowAct41.setHorizontalAlignment(SwingConstants.CENTER);
										lblFlowAct41.setBackground(Color.white);
										lblFlowAct41.setName("actual");
										pnlBotRes41.add(lblFlowAct41, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblHeadAct41 ----
										lblHeadAct41.setText("head");
										lblHeadAct41.setFont(new Font("Arial", Font.PLAIN, 12));
										lblHeadAct41.setOpaque(true);
										lblHeadAct41.setHorizontalAlignment(SwingConstants.CENTER);
										lblHeadAct41.setBackground(Color.white);
										lblHeadAct41.setName("actual");
										pnlBotRes41.add(lblHeadAct41, new TableLayoutConstraints(2, 2, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblMotIpAct41 ----
										lblMotIpAct41.setText("mi");
										lblMotIpAct41.setFont(new Font("Arial", Font.PLAIN, 12));
										lblMotIpAct41.setOpaque(true);
										lblMotIpAct41.setHorizontalAlignment(SwingConstants.CENTER);
										lblMotIpAct41.setBackground(Color.white);
										lblMotIpAct41.setName("actual");
										pnlBotRes41.add(lblMotIpAct41, new TableLayoutConstraints(3, 2, 3, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblEffAct41 ----
										lblEffAct41.setText("eff");
										lblEffAct41.setFont(new Font("Arial", Font.PLAIN, 12));
										lblEffAct41.setOpaque(true);
										lblEffAct41.setHorizontalAlignment(SwingConstants.CENTER);
										lblEffAct41.setBackground(Color.white);
										lblEffAct41.setName("actual");
										pnlBotRes41.add(lblEffAct41, new TableLayoutConstraints(4, 2, 4, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblCurAct41 ----
										lblCurAct41.setText("a");
										lblCurAct41.setFont(new Font("Arial", Font.PLAIN, 12));
										lblCurAct41.setOpaque(true);
										lblCurAct41.setHorizontalAlignment(SwingConstants.CENTER);
										lblCurAct41.setBackground(Color.white);
										lblCurAct41.setName("actual");
										pnlBotRes41.add(lblCurAct41, new TableLayoutConstraints(5, 2, 5, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- label80 ----
										label80.setText(" Results");
										label80.setFont(new Font("Arial", Font.BOLD, 12));
										label80.setOpaque(true);
										label80.setHorizontalAlignment(SwingConstants.LEFT);
										label80.setBackground(Color.white);
										label80.setName("label");
										pnlBotRes41.add(label80, new TableLayoutConstraints(0, 3, 0, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblFlowRes41 ----
										lblFlowRes41.setText("flow");
										lblFlowRes41.setFont(new Font("Arial", Font.PLAIN, 12));
										lblFlowRes41.setOpaque(true);
										lblFlowRes41.setHorizontalAlignment(SwingConstants.CENTER);
										lblFlowRes41.setBackground(Color.white);
										lblFlowRes41.setName("actual");
										pnlBotRes41.add(lblFlowRes41, new TableLayoutConstraints(1, 3, 1, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblHeadRes41 ----
										lblHeadRes41.setText("head");
										lblHeadRes41.setFont(new Font("Arial", Font.PLAIN, 12));
										lblHeadRes41.setOpaque(true);
										lblHeadRes41.setHorizontalAlignment(SwingConstants.CENTER);
										lblHeadRes41.setBackground(Color.white);
										lblHeadRes41.setName("actual");
										pnlBotRes41.add(lblHeadRes41, new TableLayoutConstraints(2, 3, 2, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblMotIpRes41 ----
										lblMotIpRes41.setText("mi");
										lblMotIpRes41.setFont(new Font("Arial", Font.PLAIN, 12));
										lblMotIpRes41.setOpaque(true);
										lblMotIpRes41.setHorizontalAlignment(SwingConstants.CENTER);
										lblMotIpRes41.setBackground(Color.white);
										lblMotIpRes41.setName("actual");
										pnlBotRes41.add(lblMotIpRes41, new TableLayoutConstraints(3, 3, 3, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblEffRes41 ----
										lblEffRes41.setText("eff");
										lblEffRes41.setFont(new Font("Arial", Font.PLAIN, 12));
										lblEffRes41.setOpaque(true);
										lblEffRes41.setHorizontalAlignment(SwingConstants.CENTER);
										lblEffRes41.setBackground(Color.white);
										lblEffRes41.setName("actual");
										pnlBotRes41.add(lblEffRes41, new TableLayoutConstraints(4, 3, 4, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblCurRes41 ----
										lblCurRes41.setText("a");
										lblCurRes41.setFont(new Font("Arial", Font.PLAIN, 12));
										lblCurRes41.setOpaque(true);
										lblCurRes41.setHorizontalAlignment(SwingConstants.CENTER);
										lblCurRes41.setBackground(Color.white);
										lblCurRes41.setName("actual");
										pnlBotRes41.add(lblCurRes41, new TableLayoutConstraints(5, 3, 5, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
									}
									pnlPrint41.add(pnlBotRes41, new TableLayoutConstraints(3, 11, 4, 11, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//======== pnlBotRes41R ========
									{
										pnlBotRes41R.setBackground(Color.black);
										pnlBotRes41R.setBorder(LineBorder.createBlackLineBorder());
										pnlBotRes41R.setName("outline");
										pnlBotRes41R.setVisible(false);
										pnlBotRes41R.setLayout(new TableLayout(new double[][] {
											{TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.PREFERRED, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL},
											{20, 23, 25, 25, 25}}));
										((TableLayout)pnlBotRes41R.getLayout()).setHGap(2);
										((TableLayout)pnlBotRes41R.getLayout()).setVGap(2);

										//---- lblLH41 ----
										lblLH41.setText("Low Head");
										lblLH41.setFont(new Font("Arial", Font.BOLD, 10));
										lblLH41.setOpaque(true);
										lblLH41.setHorizontalAlignment(SwingConstants.CENTER);
										lblLH41.setBackground(Color.white);
										lblLH41.setName("label");
										pnlBotRes41R.add(lblLH41, new TableLayoutConstraints(1, 0, 4, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblHH41 ----
										lblHH41.setText("High Head");
										lblHH41.setFont(new Font("Arial", Font.BOLD, 10));
										lblHH41.setOpaque(true);
										lblHH41.setHorizontalAlignment(SwingConstants.CENTER);
										lblHH41.setBackground(Color.white);
										lblHH41.setName("label");
										pnlBotRes41R.add(lblHH41, new TableLayoutConstraints(5, 0, 8, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- label235 ----
										label235.setText(" Duty Pt.");
										label235.setFont(new Font("Arial", Font.BOLD, 10));
										label235.setOpaque(true);
										label235.setHorizontalAlignment(SwingConstants.LEFT);
										label235.setBackground(Color.white);
										label235.setName("label");
										pnlBotRes41R.add(label235, new TableLayoutConstraints(0, 0, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblFlowUnit41RL ----
										lblFlowUnit41RL.setText("Q (unit)");
										lblFlowUnit41RL.setFont(new Font("Arial", Font.BOLD, 10));
										lblFlowUnit41RL.setOpaque(true);
										lblFlowUnit41RL.setHorizontalAlignment(SwingConstants.CENTER);
										lblFlowUnit41RL.setBackground(Color.white);
										lblFlowUnit41RL.setName("label");
										pnlBotRes41R.add(lblFlowUnit41RL, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblHd41RL ----
										lblHd41RL.setText("TH (m)");
										lblHd41RL.setFont(new Font("Arial", Font.BOLD, 10));
										lblHd41RL.setOpaque(true);
										lblHd41RL.setHorizontalAlignment(SwingConstants.CENTER);
										lblHd41RL.setBackground(Color.white);
										lblHd41RL.setName("label");
										pnlBotRes41R.add(lblHd41RL, new TableLayoutConstraints(2, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblFlowUnit41RH ----
										lblFlowUnit41RH.setText("Q (unit)");
										lblFlowUnit41RH.setFont(new Font("Arial", Font.BOLD, 10));
										lblFlowUnit41RH.setOpaque(true);
										lblFlowUnit41RH.setHorizontalAlignment(SwingConstants.CENTER);
										lblFlowUnit41RH.setBackground(Color.white);
										lblFlowUnit41RH.setName("label");
										pnlBotRes41R.add(lblFlowUnit41RH, new TableLayoutConstraints(5, 1, 5, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblHd41RH ----
										lblHd41RH.setText("TH (m)");
										lblHd41RH.setFont(new Font("Arial", Font.BOLD, 10));
										lblHd41RH.setOpaque(true);
										lblHd41RH.setHorizontalAlignment(SwingConstants.CENTER);
										lblHd41RH.setBackground(Color.white);
										lblHd41RH.setName("label");
										pnlBotRes41R.add(lblHd41RH, new TableLayoutConstraints(6, 1, 6, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- label244 ----
										label244.setText(" Grntd.");
										label244.setFont(new Font("Arial", Font.BOLD, 10));
										label244.setOpaque(true);
										label244.setHorizontalAlignment(SwingConstants.LEFT);
										label244.setBackground(Color.white);
										label244.setName("label");
										pnlBotRes41R.add(label244, new TableLayoutConstraints(0, 2, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- label245 ----
										label245.setText(" Actual");
										label245.setFont(new Font("Arial", Font.BOLD, 10));
										label245.setOpaque(true);
										label245.setHorizontalAlignment(SwingConstants.LEFT);
										label245.setBackground(Color.white);
										label245.setName("label");
										pnlBotRes41R.add(label245, new TableLayoutConstraints(0, 3, 0, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- label246 ----
										label246.setText(" Results");
										label246.setFont(new Font("Arial", Font.BOLD, 10));
										label246.setOpaque(true);
										label246.setHorizontalAlignment(SwingConstants.LEFT);
										label246.setBackground(Color.white);
										label246.setName("label");
										pnlBotRes41R.add(label246, new TableLayoutConstraints(0, 4, 0, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- label248 ----
										label248.setText("<html><body align=\"center\">I-Max (A)</html>");
										label248.setFont(new Font("Arial", Font.BOLD, 10));
										label248.setOpaque(true);
										label248.setHorizontalAlignment(SwingConstants.CENTER);
										label248.setBackground(Color.white);
										label248.setName("label");
										pnlBotRes41R.add(label248, new TableLayoutConstraints(9, 0, 9, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lbllblEff41R1 ----
										lbllblEff41R1.setText("OE (%)");
										lbllblEff41R1.setFont(new Font("Arial", Font.BOLD, 10));
										lbllblEff41R1.setOpaque(true);
										lbllblEff41R1.setHorizontalAlignment(SwingConstants.CENTER);
										lbllblEff41R1.setBackground(Color.white);
										lbllblEff41R1.setName("label");
										pnlBotRes41R.add(lbllblEff41R1, new TableLayoutConstraints(3, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lbllblMI41R1 ----
										lbllblMI41R1.setText("MI (kW)");
										lbllblMI41R1.setFont(new Font("Arial", Font.BOLD, 10));
										lbllblMI41R1.setOpaque(true);
										lbllblMI41R1.setHorizontalAlignment(SwingConstants.CENTER);
										lbllblMI41R1.setBackground(Color.white);
										lbllblMI41R1.setName("label");
										pnlBotRes41R.add(lbllblMI41R1, new TableLayoutConstraints(4, 1, 4, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lbllblEff41R2 ----
										lbllblEff41R2.setText("OE (%)");
										lbllblEff41R2.setFont(new Font("Arial", Font.BOLD, 10));
										lbllblEff41R2.setOpaque(true);
										lbllblEff41R2.setHorizontalAlignment(SwingConstants.CENTER);
										lbllblEff41R2.setBackground(Color.white);
										lbllblEff41R2.setName("label");
										pnlBotRes41R.add(lbllblEff41R2, new TableLayoutConstraints(7, 1, 7, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lbllblMI41R2 ----
										lbllblMI41R2.setText("MI (kW)");
										lbllblMI41R2.setFont(new Font("Arial", Font.BOLD, 10));
										lbllblMI41R2.setOpaque(true);
										lbllblMI41R2.setHorizontalAlignment(SwingConstants.CENTER);
										lbllblMI41R2.setBackground(Color.white);
										lbllblMI41R2.setName("label");
										pnlBotRes41R.add(lbllblMI41R2, new TableLayoutConstraints(8, 1, 8, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblFlow41RL ----
										lblFlow41RL.setText("flow");
										lblFlow41RL.setFont(new Font("Arial", Font.PLAIN, 12));
										lblFlow41RL.setOpaque(true);
										lblFlow41RL.setHorizontalAlignment(SwingConstants.CENTER);
										lblFlow41RL.setBackground(Color.white);
										lblFlow41RL.setName("declared");
										pnlBotRes41R.add(lblFlow41RL, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblHead41RL ----
										lblHead41RL.setText("head");
										lblHead41RL.setFont(new Font("Arial", Font.PLAIN, 12));
										lblHead41RL.setOpaque(true);
										lblHead41RL.setHorizontalAlignment(SwingConstants.CENTER);
										lblHead41RL.setBackground(Color.white);
										lblHead41RL.setName("declared");
										pnlBotRes41R.add(lblHead41RL, new TableLayoutConstraints(2, 2, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblEff41RL ----
										lblEff41RL.setText("eff");
										lblEff41RL.setFont(new Font("Arial", Font.PLAIN, 12));
										lblEff41RL.setOpaque(true);
										lblEff41RL.setHorizontalAlignment(SwingConstants.CENTER);
										lblEff41RL.setBackground(Color.white);
										lblEff41RL.setName("declared");
										pnlBotRes41R.add(lblEff41RL, new TableLayoutConstraints(3, 2, 3, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblMotIp41RL ----
										lblMotIp41RL.setText("mi");
										lblMotIp41RL.setFont(new Font("Arial", Font.PLAIN, 12));
										lblMotIp41RL.setOpaque(true);
										lblMotIp41RL.setHorizontalAlignment(SwingConstants.CENTER);
										lblMotIp41RL.setBackground(Color.white);
										lblMotIp41RL.setName("declared");
										pnlBotRes41R.add(lblMotIp41RL, new TableLayoutConstraints(4, 2, 4, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblFlow41RH ----
										lblFlow41RH.setText("flow");
										lblFlow41RH.setFont(new Font("Arial", Font.PLAIN, 12));
										lblFlow41RH.setOpaque(true);
										lblFlow41RH.setHorizontalAlignment(SwingConstants.CENTER);
										lblFlow41RH.setBackground(Color.white);
										lblFlow41RH.setName("declared");
										pnlBotRes41R.add(lblFlow41RH, new TableLayoutConstraints(5, 2, 5, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblHead41RH ----
										lblHead41RH.setText("head");
										lblHead41RH.setFont(new Font("Arial", Font.PLAIN, 12));
										lblHead41RH.setOpaque(true);
										lblHead41RH.setHorizontalAlignment(SwingConstants.CENTER);
										lblHead41RH.setBackground(Color.white);
										lblHead41RH.setName("declared");
										pnlBotRes41R.add(lblHead41RH, new TableLayoutConstraints(6, 2, 6, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblEff41RH ----
										lblEff41RH.setText("eff");
										lblEff41RH.setFont(new Font("Arial", Font.PLAIN, 12));
										lblEff41RH.setOpaque(true);
										lblEff41RH.setHorizontalAlignment(SwingConstants.CENTER);
										lblEff41RH.setBackground(Color.white);
										lblEff41RH.setName("declared");
										pnlBotRes41R.add(lblEff41RH, new TableLayoutConstraints(7, 2, 7, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblMotIp41RH ----
										lblMotIp41RH.setText("mi");
										lblMotIp41RH.setFont(new Font("Arial", Font.PLAIN, 12));
										lblMotIp41RH.setOpaque(true);
										lblMotIp41RH.setHorizontalAlignment(SwingConstants.CENTER);
										lblMotIp41RH.setBackground(Color.white);
										lblMotIp41RH.setName("declared");
										pnlBotRes41R.add(lblMotIp41RH, new TableLayoutConstraints(8, 2, 8, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblCur41R ----
										lblCur41R.setText("a");
										lblCur41R.setFont(new Font("Arial", Font.PLAIN, 12));
										lblCur41R.setOpaque(true);
										lblCur41R.setHorizontalAlignment(SwingConstants.CENTER);
										lblCur41R.setBackground(Color.white);
										lblCur41R.setName("declared");
										pnlBotRes41R.add(lblCur41R, new TableLayoutConstraints(9, 2, 9, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblFlowAct41RL ----
										lblFlowAct41RL.setText("flow");
										lblFlowAct41RL.setFont(new Font("Arial", Font.PLAIN, 12));
										lblFlowAct41RL.setOpaque(true);
										lblFlowAct41RL.setHorizontalAlignment(SwingConstants.CENTER);
										lblFlowAct41RL.setBackground(Color.white);
										lblFlowAct41RL.setName("actual");
										pnlBotRes41R.add(lblFlowAct41RL, new TableLayoutConstraints(1, 3, 1, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblHeadAct41RL ----
										lblHeadAct41RL.setText("head");
										lblHeadAct41RL.setFont(new Font("Arial", Font.PLAIN, 12));
										lblHeadAct41RL.setOpaque(true);
										lblHeadAct41RL.setHorizontalAlignment(SwingConstants.CENTER);
										lblHeadAct41RL.setBackground(Color.white);
										lblHeadAct41RL.setName("actual");
										pnlBotRes41R.add(lblHeadAct41RL, new TableLayoutConstraints(2, 3, 2, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblEffAct41RL ----
										lblEffAct41RL.setText("eff");
										lblEffAct41RL.setFont(new Font("Arial", Font.PLAIN, 12));
										lblEffAct41RL.setOpaque(true);
										lblEffAct41RL.setHorizontalAlignment(SwingConstants.CENTER);
										lblEffAct41RL.setBackground(Color.white);
										lblEffAct41RL.setName("actual");
										pnlBotRes41R.add(lblEffAct41RL, new TableLayoutConstraints(3, 3, 3, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblMotIpAct41RL ----
										lblMotIpAct41RL.setText("mi");
										lblMotIpAct41RL.setFont(new Font("Arial", Font.PLAIN, 12));
										lblMotIpAct41RL.setOpaque(true);
										lblMotIpAct41RL.setHorizontalAlignment(SwingConstants.CENTER);
										lblMotIpAct41RL.setBackground(Color.white);
										lblMotIpAct41RL.setName("actual");
										pnlBotRes41R.add(lblMotIpAct41RL, new TableLayoutConstraints(4, 3, 4, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblFlowAct41RH ----
										lblFlowAct41RH.setText("flow");
										lblFlowAct41RH.setFont(new Font("Arial", Font.PLAIN, 12));
										lblFlowAct41RH.setOpaque(true);
										lblFlowAct41RH.setHorizontalAlignment(SwingConstants.CENTER);
										lblFlowAct41RH.setBackground(Color.white);
										lblFlowAct41RH.setName("actual");
										pnlBotRes41R.add(lblFlowAct41RH, new TableLayoutConstraints(5, 3, 5, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblHeadAct41RH ----
										lblHeadAct41RH.setText("head");
										lblHeadAct41RH.setFont(new Font("Arial", Font.PLAIN, 12));
										lblHeadAct41RH.setOpaque(true);
										lblHeadAct41RH.setHorizontalAlignment(SwingConstants.CENTER);
										lblHeadAct41RH.setBackground(Color.white);
										lblHeadAct41RH.setName("actual");
										pnlBotRes41R.add(lblHeadAct41RH, new TableLayoutConstraints(6, 3, 6, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblEffAct41RH ----
										lblEffAct41RH.setText("eff");
										lblEffAct41RH.setFont(new Font("Arial", Font.PLAIN, 12));
										lblEffAct41RH.setOpaque(true);
										lblEffAct41RH.setHorizontalAlignment(SwingConstants.CENTER);
										lblEffAct41RH.setBackground(Color.white);
										lblEffAct41RH.setName("actual");
										pnlBotRes41R.add(lblEffAct41RH, new TableLayoutConstraints(7, 3, 7, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblMotIpAct41RH ----
										lblMotIpAct41RH.setText("mi");
										lblMotIpAct41RH.setFont(new Font("Arial", Font.PLAIN, 12));
										lblMotIpAct41RH.setOpaque(true);
										lblMotIpAct41RH.setHorizontalAlignment(SwingConstants.CENTER);
										lblMotIpAct41RH.setBackground(Color.white);
										lblMotIpAct41RH.setName("actual");
										pnlBotRes41R.add(lblMotIpAct41RH, new TableLayoutConstraints(8, 3, 8, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblCurAct41R ----
										lblCurAct41R.setText("a");
										lblCurAct41R.setFont(new Font("Arial", Font.PLAIN, 12));
										lblCurAct41R.setOpaque(true);
										lblCurAct41R.setHorizontalAlignment(SwingConstants.CENTER);
										lblCurAct41R.setBackground(Color.white);
										lblCurAct41R.setName("actual");
										pnlBotRes41R.add(lblCurAct41R, new TableLayoutConstraints(9, 3, 9, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblFlowRes41RL ----
										lblFlowRes41RL.setText("flow");
										lblFlowRes41RL.setFont(new Font("Arial", Font.PLAIN, 12));
										lblFlowRes41RL.setOpaque(true);
										lblFlowRes41RL.setHorizontalAlignment(SwingConstants.CENTER);
										lblFlowRes41RL.setBackground(Color.white);
										lblFlowRes41RL.setName("actual");
										pnlBotRes41R.add(lblFlowRes41RL, new TableLayoutConstraints(1, 4, 1, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblHeadRes41RL ----
										lblHeadRes41RL.setText("head");
										lblHeadRes41RL.setFont(new Font("Arial", Font.PLAIN, 12));
										lblHeadRes41RL.setOpaque(true);
										lblHeadRes41RL.setHorizontalAlignment(SwingConstants.CENTER);
										lblHeadRes41RL.setBackground(Color.white);
										lblHeadRes41RL.setName("actual");
										pnlBotRes41R.add(lblHeadRes41RL, new TableLayoutConstraints(2, 4, 2, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblEffRes41RL ----
										lblEffRes41RL.setText("eff");
										lblEffRes41RL.setFont(new Font("Arial", Font.PLAIN, 12));
										lblEffRes41RL.setOpaque(true);
										lblEffRes41RL.setHorizontalAlignment(SwingConstants.CENTER);
										lblEffRes41RL.setBackground(Color.white);
										lblEffRes41RL.setName("actual");
										pnlBotRes41R.add(lblEffRes41RL, new TableLayoutConstraints(3, 4, 3, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblMotIpRes41RL ----
										lblMotIpRes41RL.setText("mi");
										lblMotIpRes41RL.setFont(new Font("Arial", Font.PLAIN, 12));
										lblMotIpRes41RL.setOpaque(true);
										lblMotIpRes41RL.setHorizontalAlignment(SwingConstants.CENTER);
										lblMotIpRes41RL.setBackground(Color.white);
										lblMotIpRes41RL.setName("actual");
										pnlBotRes41R.add(lblMotIpRes41RL, new TableLayoutConstraints(4, 4, 4, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblFlowRes41RH ----
										lblFlowRes41RH.setText("flow");
										lblFlowRes41RH.setFont(new Font("Arial", Font.PLAIN, 12));
										lblFlowRes41RH.setOpaque(true);
										lblFlowRes41RH.setHorizontalAlignment(SwingConstants.CENTER);
										lblFlowRes41RH.setBackground(Color.white);
										lblFlowRes41RH.setName("actual");
										pnlBotRes41R.add(lblFlowRes41RH, new TableLayoutConstraints(5, 4, 5, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblHeadRes41RH ----
										lblHeadRes41RH.setText("head");
										lblHeadRes41RH.setFont(new Font("Arial", Font.PLAIN, 12));
										lblHeadRes41RH.setOpaque(true);
										lblHeadRes41RH.setHorizontalAlignment(SwingConstants.CENTER);
										lblHeadRes41RH.setBackground(Color.white);
										lblHeadRes41RH.setName("actual");
										pnlBotRes41R.add(lblHeadRes41RH, new TableLayoutConstraints(6, 4, 6, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblEffRes41RH ----
										lblEffRes41RH.setText("eff");
										lblEffRes41RH.setFont(new Font("Arial", Font.PLAIN, 12));
										lblEffRes41RH.setOpaque(true);
										lblEffRes41RH.setHorizontalAlignment(SwingConstants.CENTER);
										lblEffRes41RH.setBackground(Color.white);
										lblEffRes41RH.setName("actual");
										pnlBotRes41R.add(lblEffRes41RH, new TableLayoutConstraints(7, 4, 7, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblMotIpRes41RH ----
										lblMotIpRes41RH.setText("mi");
										lblMotIpRes41RH.setFont(new Font("Arial", Font.PLAIN, 12));
										lblMotIpRes41RH.setOpaque(true);
										lblMotIpRes41RH.setHorizontalAlignment(SwingConstants.CENTER);
										lblMotIpRes41RH.setBackground(Color.white);
										lblMotIpRes41RH.setName("actual");
										pnlBotRes41R.add(lblMotIpRes41RH, new TableLayoutConstraints(8, 4, 8, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblCurRes41R ----
										lblCurRes41R.setText("a");
										lblCurRes41R.setFont(new Font("Arial", Font.PLAIN, 12));
										lblCurRes41R.setOpaque(true);
										lblCurRes41R.setHorizontalAlignment(SwingConstants.CENTER);
										lblCurRes41R.setBackground(Color.white);
										lblCurRes41R.setName("actual");
										pnlBotRes41R.add(lblCurRes41R, new TableLayoutConstraints(9, 4, 9, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
									}
									pnlPrint41.add(pnlBotRes41R, new TableLayoutConstraints(3, 11, 4, 11, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//======== pnlOptTest41 ========
									{
										pnlOptTest41.setBackground(Color.white);
										pnlOptTest41.setBorder(LineBorder.createBlackLineBorder());
										pnlOptTest41.setLayout(new TableLayout(new double[][] {
											{5, TableLayout.FILL},
											{5, TableLayout.PREFERRED, TableLayout.PREFERRED}}));
										((TableLayout)pnlOptTest41.getLayout()).setHGap(5);
										((TableLayout)pnlOptTest41.getLayout()).setVGap(5);

										//---- lblCas42 ----
										lblCas42.setText("Self Priming Test:");
										lblCas42.setFont(new Font("Arial", Font.BOLD, 12));
										lblCas42.setName("label");
										pnlOptTest41.add(lblCas42, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblSP42 ----
										lblSP42.setFont(new Font("Arial", Font.PLAIN, 12));
										lblSP42.setName("actual");
										pnlOptTest41.add(lblSP42, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
									}
									pnlPrint41.add(pnlOptTest41, new TableLayoutConstraints(1, 12, 4, 12, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblAppVer41 ----
									lblAppVer41.setText("Doer PumpViewProc");
									lblAppVer41.setFont(new Font("Arial", Font.PLAIN, 10));
									lblAppVer41.setHorizontalAlignment(SwingConstants.RIGHT);
									lblAppVer41.setForeground(Color.gray);
									pnlPrint41.add(lblAppVer41, new TableLayoutConstraints(3, 15, 4, 15, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
								}
								printArea41.add(pnlPrint41, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
							}
							scrollPane41.setViewportView(printArea41);
						}
						tabISI.addTab("Test Report", scrollPane41);

						//======== scrollPane42 ========
						{

							//======== printArea42 ========
							{
								printArea42.setBorder(new LineBorder(Color.blue, 2));
								printArea42.setBackground(Color.white);
								printArea42.setLayout(new TableLayout(new double[][] {
									{900},
									{1450}}));

								//======== pnlPrint42 ========
								{
									pnlPrint42.setBorder(null);
									pnlPrint42.setBackground(Color.white);
									pnlPrint42.setLayout(new TableLayout(new double[][] {
										{40, 100, TableLayout.FILL, 100, 15},
										{35, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, 75, TableLayout.FILL, TableLayout.PREFERRED}}));

									//---- lblCustLogo42 ----
									lblCustLogo42.setHorizontalAlignment(SwingConstants.CENTER);
									pnlPrint42.add(lblCustLogo42, new TableLayoutConstraints(1, 1, 1, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblCompName42 ----
									lblCompName42.setText("Company Name");
									lblCompName42.setFont(new Font("Arial", Font.BOLD, 16));
									lblCompName42.setHorizontalAlignment(SwingConstants.CENTER);
									lblCompName42.setName("header");
									pnlPrint42.add(lblCompName42, new TableLayoutConstraints(2, 1, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblIS42 ----
									lblIS42.setText("IS:");
									lblIS42.setHorizontalAlignment(SwingConstants.CENTER);
									lblIS42.setFont(new Font("Arial", Font.BOLD, 10));
									lblIS42.setName("label");
									pnlPrint42.add(lblIS42, new TableLayoutConstraints(3, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblISILogo42 ----
									lblISILogo42.setIcon(new ImageIcon(getClass().getResource("/img/isi_logo.PNG")));
									lblISILogo42.setHorizontalAlignment(SwingConstants.CENTER);
									lblISILogo42.setVerticalAlignment(SwingConstants.TOP);
									lblISILogo42.setIconTextGap(0);
									pnlPrint42.add(lblISILogo42, new TableLayoutConstraints(3, 2, 3, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblCompAdr42 ----
									lblCompAdr42.setText("Address Line 1 and Line 2");
									lblCompAdr42.setFont(new Font("Arial", Font.BOLD, 12));
									lblCompAdr42.setHorizontalAlignment(SwingConstants.CENTER);
									lblCompAdr42.setName("header");
									pnlPrint42.add(lblCompAdr42, new TableLayoutConstraints(2, 3, 2, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblTitle42 ----
									lblTitle42.setText("PUMP PERFORMANCE TEST ISI REPORT");
									lblTitle42.setFont(new Font("Arial", Font.BOLD, 14));
									lblTitle42.setHorizontalAlignment(SwingConstants.CENTER);
									lblTitle42.setName("header");
									pnlPrint42.add(lblTitle42, new TableLayoutConstraints(2, 5, 2, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblISIRefNo42 ----
									lblISIRefNo42.setText("ISI Ref No.");
									lblISIRefNo42.setHorizontalAlignment(SwingConstants.CENTER);
									lblISIRefNo42.setFont(new Font("Arial", Font.BOLD, 10));
									lblISIRefNo42.setName("label");
									pnlPrint42.add(lblISIRefNo42, new TableLayoutConstraints(3, 4, 3, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblPage42 ----
									lblPage42.setText("Page 1 of 1");
									lblPage42.setFont(new Font("Arial", Font.PLAIN, 12));
									lblPage42.setHorizontalAlignment(SwingConstants.RIGHT);
									lblPage42.setName("label");
									pnlPrint42.add(lblPage42, new TableLayoutConstraints(3, 7, 3, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//======== pnlTop42 ========
									{
										pnlTop42.setBorder(LineBorder.createBlackLineBorder());
										pnlTop42.setBackground(Color.white);
										pnlTop42.setFont(new Font("Arial", Font.BOLD, 12));
										pnlTop42.setName("outline");
										pnlTop42.setLayout(new TableLayout(new double[][] {
											{5, TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED, TableLayout.PREFERRED, 5},
											{30}}));
										((TableLayout)pnlTop42.getLayout()).setHGap(7);
										((TableLayout)pnlTop42.getLayout()).setVGap(7);

										//---- label60 ----
										label60.setText("PUMP TYPE");
										label60.setFont(new Font("Arial", Font.BOLD, 12));
										label60.setName("label");
										pnlTop42.add(label60, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblPumpType42 ----
										lblPumpType42.setText("type");
										lblPumpType42.setFont(new Font("Arial", Font.PLAIN, 12));
										lblPumpType42.setName("actual");
										pnlTop42.add(lblPumpType42, new TableLayoutConstraints(2, 0, 2, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- label86 ----
										label86.setText("PERIOD");
										label86.setFont(new Font("Arial", Font.BOLD, 12));
										label86.setName("label");
										pnlTop42.add(label86, new TableLayoutConstraints(3, 0, 3, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblDt42 ----
										lblDt42.setText("dt");
										lblDt42.setFont(new Font("Arial", Font.PLAIN, 12));
										lblDt42.setName("actual");
										pnlTop42.add(lblDt42, new TableLayoutConstraints(4, 0, 5, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
									}
									pnlPrint42.add(pnlTop42, new TableLayoutConstraints(2, 8, 2, 8, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//======== pnlPrime42 ========
									{
										pnlPrime42.setBackground(Color.black);
										pnlPrime42.setAutoscrolls(true);
										pnlPrime42.setBorder(LineBorder.createBlackLineBorder());
										pnlPrime42.setName("outline");
										pnlPrime42.setLayout(new TableLayout(new double[][] {
											{TableLayout.FILL, 150, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL},
											{25, 43, 25, TableLayout.FILL}}));
										((TableLayout)pnlPrime42.getLayout()).setHGap(2);
										((TableLayout)pnlPrime42.getLayout()).setVGap(2);

										//---- label186 ----
										label186.setText("Date");
										label186.setBackground(Color.white);
										label186.setHorizontalAlignment(SwingConstants.CENTER);
										label186.setFont(new Font("Arial", Font.BOLD, 12));
										label186.setOpaque(true);
										label186.setBorder(null);
										label186.setName("label");
										pnlPrime42.add(label186, new TableLayoutConstraints(0, 0, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- label187 ----
										label187.setText("Pump Serial No.");
										label187.setBackground(Color.white);
										label187.setHorizontalAlignment(SwingConstants.CENTER);
										label187.setFont(new Font("Arial", Font.BOLD, 12));
										label187.setOpaque(true);
										label187.setBorder(null);
										label187.setName("label");
										pnlPrime42.add(label187, new TableLayoutConstraints(1, 0, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblTypeCount42 ----
										lblTypeCount42.setText("ONE FOR EVERY X PUMPS");
										lblTypeCount42.setBackground(Color.white);
										lblTypeCount42.setOpaque(true);
										lblTypeCount42.setFont(new Font("Arial", Font.BOLD, 12));
										lblTypeCount42.setHorizontalAlignment(SwingConstants.CENTER);
										lblTypeCount42.setBorder(null);
										lblTypeCount42.setName("label");
										pnlPrime42.add(lblTypeCount42, new TableLayoutConstraints(3, 0, 7, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- label342 ----
										label342.setText("<html><body align=\"center\">Hydrostatic Test</body></html>");
										label342.setBackground(Color.white);
										label342.setHorizontalAlignment(SwingConstants.CENTER);
										label342.setFont(new Font("Arial", Font.BOLD, 12));
										label342.setOpaque(true);
										label342.setBorder(null);
										label342.setName("label");
										pnlPrime42.add(label342, new TableLayoutConstraints(2, 0, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblDis42 ----
										lblDis42.setText("<html><body align=\"center\">Discharge<br>(unit)</body></html>");
										lblDis42.setBackground(Color.white);
										lblDis42.setHorizontalAlignment(SwingConstants.CENTER);
										lblDis42.setFont(new Font("Arial", Font.BOLD, 12));
										lblDis42.setOpaque(true);
										lblDis42.setBorder(null);
										lblDis42.setName("label");
										pnlPrime42.add(lblDis42, new TableLayoutConstraints(3, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblHd42 ----
										lblHd42.setText("<html><body align=\"center\">Total Head<br>(m)</body></html>");
										lblHd42.setBackground(Color.white);
										lblHd42.setHorizontalAlignment(SwingConstants.CENTER);
										lblHd42.setFont(new Font("Arial", Font.BOLD, 12));
										lblHd42.setOpaque(true);
										lblHd42.setBorder(null);
										lblHd42.setName("label");
										pnlPrime42.add(lblHd42, new TableLayoutConstraints(4, 1, 4, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lbllblMI42 ----
										lbllblMI42.setText("<html><body align=\"center\">Motor Input<br>(kW)</body></html>");
										lbllblMI42.setBackground(Color.white);
										lbllblMI42.setHorizontalAlignment(SwingConstants.CENTER);
										lbllblMI42.setFont(new Font("Arial", Font.BOLD, 12));
										lbllblMI42.setOpaque(true);
										lbllblMI42.setBorder(null);
										lbllblMI42.setName("label");
										pnlPrime42.add(lbllblMI42, new TableLayoutConstraints(5, 1, 5, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lbllblEff42 ----
										lbllblEff42.setText("<html><body align=\"center\">O.A Eff.<br>(%)</body></html>");
										lbllblEff42.setBackground(Color.white);
										lbllblEff42.setHorizontalAlignment(SwingConstants.CENTER);
										lbllblEff42.setFont(new Font("Arial", Font.BOLD, 12));
										lbllblEff42.setOpaque(true);
										lbllblEff42.setBorder(null);
										lbllblEff42.setName("label");
										pnlPrime42.add(lbllblEff42, new TableLayoutConstraints(6, 1, 6, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- label190 ----
										label190.setText("<html><body align=\"center\">Max Current<br>(A)</body></html>");
										label190.setBackground(Color.white);
										label190.setHorizontalAlignment(SwingConstants.CENTER);
										label190.setFont(new Font("Arial", Font.BOLD, 12));
										label190.setOpaque(true);
										label190.setAutoscrolls(true);
										label190.setBorder(null);
										label190.setName("label");
										pnlPrime42.add(label190, new TableLayoutConstraints(7, 1, 7, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblFlow42 ----
										lblFlow42.setText("flow");
										lblFlow42.setFont(new Font("Arial", Font.BOLD, 12));
										lblFlow42.setOpaque(true);
										lblFlow42.setHorizontalAlignment(SwingConstants.CENTER);
										lblFlow42.setBackground(Color.white);
										lblFlow42.setName("declared");
										pnlPrime42.add(lblFlow42, new TableLayoutConstraints(3, 2, 3, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblHead42 ----
										lblHead42.setText("th");
										lblHead42.setFont(new Font("Arial", Font.BOLD, 12));
										lblHead42.setOpaque(true);
										lblHead42.setHorizontalAlignment(SwingConstants.CENTER);
										lblHead42.setBackground(Color.white);
										lblHead42.setName("declared");
										pnlPrime42.add(lblHead42, new TableLayoutConstraints(4, 2, 4, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblMotIp42 ----
										lblMotIp42.setText("mi");
										lblMotIp42.setFont(new Font("Arial", Font.BOLD, 12));
										lblMotIp42.setOpaque(true);
										lblMotIp42.setHorizontalAlignment(SwingConstants.CENTER);
										lblMotIp42.setBackground(Color.white);
										lblMotIp42.setName("declared");
										pnlPrime42.add(lblMotIp42, new TableLayoutConstraints(5, 2, 5, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblEff42 ----
										lblEff42.setText("eff");
										lblEff42.setFont(new Font("Arial", Font.BOLD, 12));
										lblEff42.setOpaque(true);
										lblEff42.setHorizontalAlignment(SwingConstants.CENTER);
										lblEff42.setBackground(Color.white);
										lblEff42.setName("declared");
										pnlPrime42.add(lblEff42, new TableLayoutConstraints(6, 2, 6, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblMC42 ----
										lblMC42.setText("mc");
										lblMC42.setFont(new Font("Arial", Font.BOLD, 12));
										lblMC42.setOpaque(true);
										lblMC42.setHorizontalAlignment(SwingConstants.CENTER);
										lblMC42.setBackground(Color.white);
										lblMC42.setName("declared");
										pnlPrime42.add(lblMC42, new TableLayoutConstraints(7, 2, 7, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- tblRes42 ----
										tblRes42.setModel(new DefaultTableModel(
											new Object[][] {
												{null, null, null, null, null, null, null, null},
											},
											new String[] {
												null, null, null, null, null, null, null, null
											}
										) {
											Class<?>[] columnTypes = new Class<?>[] {
												String.class, String.class, Object.class, String.class, String.class, String.class, String.class, Object.class
											};
											@Override
											public Class<?> getColumnClass(int columnIndex) {
												return columnTypes[columnIndex];
											}
										});
										{
											TableColumnModel cm = tblRes42.getColumnModel();
											cm.getColumn(1).setMinWidth(150);
											cm.getColumn(1).setMaxWidth(150);
											cm.getColumn(1).setPreferredWidth(150);
										}
										tblRes42.setFont(new Font("Arial", Font.PLAIN, 12));
										tblRes42.setRowHeight(22);
										tblRes42.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
										tblRes42.setGridColor(Color.lightGray);
										tblRes42.setBorder(new LineBorder(Color.white));
										tblRes42.setRowSelectionAllowed(false);
										tblRes42.setAutoscrolls(false);
										tblRes42.setFocusable(false);
										tblRes42.setEnabled(false);
										tblRes42.setIntercellSpacing(new Dimension(5, 5));
										tblRes42.setName("outline");
										pnlPrime42.add(tblRes42, new TableLayoutConstraints(0, 3, 7, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
									}
									pnlPrint42.add(pnlPrime42, new TableLayoutConstraints(2, 9, 2, 9, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//======== pnlBot42 ========
									{
										pnlBot42.setBackground(Color.white);
										pnlBot42.setBorder(LineBorder.createBlackLineBorder());
										pnlBot42.setName("outline");
										pnlBot42.setLayout(new TableLayout(new double[][] {
											{5, TableLayout.PREFERRED, 64, 50, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL},
											{10, 20, 20}}));
										((TableLayout)pnlBot42.getLayout()).setHGap(5);
										((TableLayout)pnlBot42.getLayout()).setVGap(5);

										//---- label96 ----
										label96.setText("Number of Pumps Produced");
										label96.setFont(new Font("Arial", Font.BOLD, 12));
										label96.setName("label");
										pnlBot42.add(label96, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblTotTest42 ----
										lblTotTest42.setText("type");
										lblTotTest42.setFont(new Font("Arial", Font.PLAIN, 12));
										lblTotTest42.setName("actual");
										pnlBot42.add(lblTotTest42, new TableLayoutConstraints(2, 1, 6, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- label97 ----
										label97.setText("Number of Pumps Tested");
										label97.setFont(new Font("Arial", Font.BOLD, 12));
										label97.setName("label");
										pnlBot42.add(label97, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblTotTypeTest42 ----
										lblTotTypeTest42.setText("type");
										lblTotTypeTest42.setFont(new Font("Arial", Font.PLAIN, 12));
										lblTotTypeTest42.setName("actual");
										pnlBot42.add(lblTotTypeTest42, new TableLayoutConstraints(2, 2, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblTestedBy42 ----
										lblTestedBy42.setText("Tested By");
										lblTestedBy42.setFont(new Font("Arial", Font.PLAIN, 13));
										lblTestedBy42.setHorizontalAlignment(SwingConstants.CENTER);
										lblTestedBy42.setName("label");
										pnlBot42.add(lblTestedBy42, new TableLayoutConstraints(3, 2, 4, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblVerBy9 ----
										lblVerBy9.setText("Verified By");
										lblVerBy9.setFont(new Font("Arial", Font.PLAIN, 13));
										lblVerBy9.setHorizontalAlignment(SwingConstants.CENTER);
										lblVerBy9.setName("label");
										pnlBot42.add(lblVerBy9, new TableLayoutConstraints(5, 2, 5, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- label98 ----
										label98.setText("Approved By");
										label98.setFont(new Font("Arial", Font.PLAIN, 13));
										label98.setHorizontalAlignment(SwingConstants.CENTER);
										label98.setName("label");
										pnlBot42.add(label98, new TableLayoutConstraints(6, 2, 6, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
									}
									pnlPrint42.add(pnlBot42, new TableLayoutConstraints(2, 10, 2, 10, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblAppVer42 ----
									lblAppVer42.setText("Doer PumpViewPro");
									lblAppVer42.setFont(new Font("Arial", Font.PLAIN, 10));
									lblAppVer42.setHorizontalAlignment(SwingConstants.RIGHT);
									lblAppVer42.setForeground(Color.gray);
									pnlPrint42.add(lblAppVer42, new TableLayoutConstraints(2, 12, 3, 12, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
								}
								printArea42.add(pnlPrint42, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
							}
							scrollPane42.setViewportView(printArea42);
						}
						tabISI.addTab("Type Test Report", scrollPane42);

						//======== pnlRep43 ========
						{
							pnlRep43.setLayout(new TableLayout(new double[][] {
								{TableLayout.PREFERRED, TableLayout.FILL},
								{TableLayout.PREFERRED, TableLayout.FILL}}));
							((TableLayout)pnlRep43.getLayout()).setHGap(5);
							((TableLayout)pnlRep43.getLayout()).setVGap(5);

							//======== pnlPumpOpt ========
							{
								pnlPumpOpt.setBorder(new TitledBorder(null, "Pump Type Selection", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
									new Font("Arial", Font.BOLD, 12), Color.blue));
								pnlPumpOpt.setLayout(new TableLayout(new double[][] {
									{TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED},
									{TableLayout.PREFERRED}}));
								((TableLayout)pnlPumpOpt.getLayout()).setHGap(5);
								((TableLayout)pnlPumpOpt.getLayout()).setVGap(5);

								//---- optAllPump ----
								optAllPump.setText("All Pump Types");
								optAllPump.setFont(new Font("Arial", Font.PLAIN, 12));
								optAllPump.setSelected(true);
								optAllPump.addActionListener(new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent e) {
										optAllPumpActionPerformed();
									}
								});
								pnlPumpOpt.add(optAllPump, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- optSelPump ----
								optSelPump.setText("Currently Selected Pump Type Only");
								optSelPump.setFont(new Font("Arial", Font.PLAIN, 12));
								optSelPump.addActionListener(new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent e) {
										optSelPumpActionPerformed();
									}
								});
								pnlPumpOpt.add(optSelPump, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblNonISI ----
								lblNonISI.setText("Note: Non-ISI Pump Models are ignored");
								lblNonISI.setFont(new Font("Arial", Font.PLAIN, 11));
								lblNonISI.setForeground(Color.red);
								pnlPumpOpt.add(lblNonISI, new TableLayoutConstraints(3, 0, 3, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
							}
							pnlRep43.add(pnlPumpOpt, new TableLayoutConstraints(0, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

							//======== scrollPane43 ========
							{

								//======== printArea43 ========
								{
									printArea43.setBorder(new LineBorder(Color.blue, 2));
									printArea43.setBackground(Color.white);
									printArea43.setLayout(new TableLayout(new double[][] {
										{1350},
										{900}}));

									//======== pnlPrint43 ========
									{
										pnlPrint43.setBorder(null);
										pnlPrint43.setBackground(Color.white);
										pnlPrint43.setLayout(new TableLayout(new double[][] {
											{40, 120, TableLayout.FILL, 125, 15},
											{35, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 10, TableLayout.PREFERRED, TableLayout.PREFERRED, 70, TableLayout.FILL, TableLayout.PREFERRED}}));

										//---- lblCustLogo43 ----
										lblCustLogo43.setHorizontalAlignment(SwingConstants.CENTER);
										pnlPrint43.add(lblCustLogo43, new TableLayoutConstraints(1, 1, 1, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblCompName43 ----
										lblCompName43.setText("Company Name");
										lblCompName43.setFont(new Font("Arial", Font.BOLD, 16));
										lblCompName43.setHorizontalAlignment(SwingConstants.CENTER);
										lblCompName43.setName("header");
										pnlPrint43.add(lblCompName43, new TableLayoutConstraints(2, 1, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblIS43 ----
										lblIS43.setText("IS:");
										lblIS43.setHorizontalAlignment(SwingConstants.CENTER);
										lblIS43.setFont(new Font("Arial", Font.BOLD, 10));
										lblIS43.setName("label");
										pnlPrint43.add(lblIS43, new TableLayoutConstraints(3, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblISILogo43 ----
										lblISILogo43.setIcon(new ImageIcon(getClass().getResource("/img/isi_logo.PNG")));
										lblISILogo43.setHorizontalAlignment(SwingConstants.CENTER);
										lblISILogo43.setVerticalAlignment(SwingConstants.TOP);
										lblISILogo43.setIconTextGap(0);
										pnlPrint43.add(lblISILogo43, new TableLayoutConstraints(3, 2, 3, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblCompAdr43 ----
										lblCompAdr43.setText("Address Line 1 and Line 2");
										lblCompAdr43.setFont(new Font("Arial", Font.BOLD, 12));
										lblCompAdr43.setHorizontalAlignment(SwingConstants.CENTER);
										lblCompAdr43.setName("header");
										pnlPrint43.add(lblCompAdr43, new TableLayoutConstraints(2, 3, 2, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblTitle43 ----
										lblTitle43.setText("PUMP PERFORMANCE TEST ISI REPORT");
										lblTitle43.setFont(new Font("Arial", Font.BOLD, 14));
										lblTitle43.setHorizontalAlignment(SwingConstants.CENTER);
										lblTitle43.setName("header");
										pnlPrint43.add(lblTitle43, new TableLayoutConstraints(2, 5, 2, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblISIRefNo43 ----
										lblISIRefNo43.setText("ISI Ref No.");
										lblISIRefNo43.setHorizontalAlignment(SwingConstants.CENTER);
										lblISIRefNo43.setFont(new Font("Arial", Font.BOLD, 10));
										lblISIRefNo43.setName("label");
										pnlPrint43.add(lblISIRefNo43, new TableLayoutConstraints(3, 4, 3, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblTitle431 ----
										lblTitle431.setText("Type Wise Results For Maximum & Minimum In Actual Values");
										lblTitle431.setFont(new Font("Arial", Font.BOLD, 14));
										lblTitle431.setHorizontalAlignment(SwingConstants.CENTER);
										lblTitle431.setName("header");
										pnlPrint43.add(lblTitle431, new TableLayoutConstraints(2, 7, 2, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblPage43 ----
										lblPage43.setText("Page 1 of 1");
										lblPage43.setFont(new Font("Arial", Font.PLAIN, 12));
										lblPage43.setHorizontalAlignment(SwingConstants.RIGHT);
										lblPage43.setName("label");
										pnlPrint43.add(lblPage43, new TableLayoutConstraints(3, 7, 3, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//======== pnlTop43 ========
										{
											pnlTop43.setBorder(LineBorder.createBlackLineBorder());
											pnlTop43.setBackground(Color.white);
											pnlTop43.setFont(new Font("Arial", Font.BOLD, 12));
											pnlTop43.setName("outline");
											pnlTop43.setLayout(new TableLayout(new double[][] {
												{540, TableLayout.PREFERRED, TableLayout.FILL},
												{30}}));
											((TableLayout)pnlTop43.getLayout()).setHGap(7);
											((TableLayout)pnlTop43.getLayout()).setVGap(7);

											//---- label87 ----
											label87.setText("PERIOD");
											label87.setFont(new Font("Arial", Font.BOLD, 13));
											label87.setName("label");
											pnlTop43.add(label87, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

											//---- lblDt43 ----
											lblDt43.setText("dt");
											lblDt43.setFont(new Font("Arial", Font.PLAIN, 13));
											lblDt43.setName("actual");
											pnlTop43.add(lblDt43, new TableLayoutConstraints(2, 0, 2, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
										}
										pnlPrint43.add(pnlTop43, new TableLayoutConstraints(1, 9, 3, 9, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//======== pnlPrime43 ========
										{
											pnlPrime43.setBackground(Color.black);
											pnlPrime43.setAutoscrolls(true);
											pnlPrime43.setBorder(LineBorder.createBlackLineBorder());
											pnlPrime43.setName("outline");
											pnlPrime43.setLayout(new TableLayout(new double[][] {
												{110, TableLayout.FILL, TableLayout.FILL, 65, TableLayout.FILL, 65, TableLayout.FILL, TableLayout.FILL, 65, TableLayout.FILL, 65, TableLayout.FILL, TableLayout.FILL, 65, TableLayout.FILL, 65, TableLayout.FILL, TableLayout.FILL, 65, TableLayout.FILL, 65, TableLayout.FILL, TableLayout.FILL, 65, TableLayout.FILL, 65},
												{35, 30, TableLayout.FILL}}));
											((TableLayout)pnlPrime43.getLayout()).setHGap(2);
											((TableLayout)pnlPrime43.getLayout()).setVGap(2);

											//---- label188 ----
											label188.setText("Pump Type");
											label188.setBackground(Color.white);
											label188.setHorizontalAlignment(SwingConstants.CENTER);
											label188.setFont(new Font("Arial", Font.BOLD, 12));
											label188.setOpaque(true);
											label188.setBorder(null);
											label188.setName("label");
											pnlPrime43.add(label188, new TableLayoutConstraints(0, 0, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

											//---- lblDis43 ----
											lblDis43.setText("<html><body align=\"center\">Discharge<br>(unit)</body></html>");
											lblDis43.setBackground(Color.white);
											lblDis43.setHorizontalAlignment(SwingConstants.CENTER);
											lblDis43.setFont(new Font("Arial", Font.BOLD, 12));
											lblDis43.setOpaque(true);
											lblDis43.setBorder(null);
											lblDis43.setName("label");
											pnlPrime43.add(lblDis43, new TableLayoutConstraints(1, 0, 5, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

											//---- lblHd43 ----
											lblHd43.setText("<html><body align=\"center\">Total Head<br>(m)</body></html>");
											lblHd43.setBackground(Color.white);
											lblHd43.setHorizontalAlignment(SwingConstants.CENTER);
											lblHd43.setFont(new Font("Arial", Font.BOLD, 12));
											lblHd43.setOpaque(true);
											lblHd43.setBorder(null);
											lblHd43.setName("label");
											pnlPrime43.add(lblHd43, new TableLayoutConstraints(6, 0, 10, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

											//---- lbllblMI43 ----
											lbllblMI43.setText("<html><body align=\"center\">Motor Input<br>(kW)</body></html>");
											lbllblMI43.setBackground(Color.white);
											lbllblMI43.setHorizontalAlignment(SwingConstants.CENTER);
											lbllblMI43.setFont(new Font("Arial", Font.BOLD, 12));
											lbllblMI43.setOpaque(true);
											lbllblMI43.setBorder(null);
											lbllblMI43.setName("label");
											pnlPrime43.add(lbllblMI43, new TableLayoutConstraints(11, 0, 15, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

											//---- lbllblEff43 ----
											lbllblEff43.setText("<html><body align=\"center\">O.A Eff.<br>(%)</body></html>");
											lbllblEff43.setBackground(Color.white);
											lbllblEff43.setHorizontalAlignment(SwingConstants.CENTER);
											lbllblEff43.setFont(new Font("Arial", Font.BOLD, 12));
											lbllblEff43.setOpaque(true);
											lbllblEff43.setBorder(null);
											lbllblEff43.setName("label");
											pnlPrime43.add(lbllblEff43, new TableLayoutConstraints(16, 0, 20, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

											//---- label191 ----
											label191.setText("<html><body align=\"center\">Max Current<br>(A)</body></html>");
											label191.setBackground(Color.white);
											label191.setHorizontalAlignment(SwingConstants.CENTER);
											label191.setFont(new Font("Arial", Font.BOLD, 12));
											label191.setOpaque(true);
											label191.setAutoscrolls(true);
											label191.setBorder(null);
											label191.setName("label");
											pnlPrime43.add(label191, new TableLayoutConstraints(21, 0, 25, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

											//---- label352 ----
											label352.setText("Grntd.");
											label352.setBackground(Color.white);
											label352.setHorizontalAlignment(SwingConstants.CENTER);
											label352.setFont(new Font("Arial", Font.BOLD, 10));
											label352.setOpaque(true);
											label352.setBorder(null);
											label352.setName("label");
											pnlPrime43.add(label352, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

											//---- label348 ----
											label348.setText("Max");
											label348.setBackground(Color.white);
											label348.setHorizontalAlignment(SwingConstants.CENTER);
											label348.setFont(new Font("Arial", Font.BOLD, 10));
											label348.setOpaque(true);
											label348.setBorder(null);
											label348.setName("label");
											pnlPrime43.add(label348, new TableLayoutConstraints(2, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

											//---- label349 ----
											label349.setText("Pump SNo");
											label349.setBackground(Color.white);
											label349.setHorizontalAlignment(SwingConstants.CENTER);
											label349.setFont(new Font("Arial", Font.BOLD, 10));
											label349.setOpaque(true);
											label349.setBorder(null);
											label349.setName("label");
											pnlPrime43.add(label349, new TableLayoutConstraints(3, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

											//---- label350 ----
											label350.setText("Min");
											label350.setBackground(Color.white);
											label350.setHorizontalAlignment(SwingConstants.CENTER);
											label350.setFont(new Font("Arial", Font.BOLD, 10));
											label350.setOpaque(true);
											label350.setBorder(null);
											label350.setName("label");
											pnlPrime43.add(label350, new TableLayoutConstraints(4, 1, 4, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

											//---- label351 ----
											label351.setText("Pump SNo");
											label351.setBackground(Color.white);
											label351.setHorizontalAlignment(SwingConstants.CENTER);
											label351.setFont(new Font("Arial", Font.BOLD, 10));
											label351.setOpaque(true);
											label351.setBorder(null);
											label351.setName("label");
											pnlPrime43.add(label351, new TableLayoutConstraints(5, 1, 5, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

											//---- label353 ----
											label353.setText("Grntd");
											label353.setBackground(Color.white);
											label353.setHorizontalAlignment(SwingConstants.CENTER);
											label353.setFont(new Font("Arial", Font.BOLD, 10));
											label353.setOpaque(true);
											label353.setBorder(null);
											label353.setName("label");
											pnlPrime43.add(label353, new TableLayoutConstraints(6, 1, 6, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

											//---- label354 ----
											label354.setText("Max");
											label354.setBackground(Color.white);
											label354.setHorizontalAlignment(SwingConstants.CENTER);
											label354.setFont(new Font("Arial", Font.BOLD, 10));
											label354.setOpaque(true);
											label354.setBorder(null);
											label354.setName("label");
											pnlPrime43.add(label354, new TableLayoutConstraints(7, 1, 7, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

											//---- label355 ----
											label355.setText("Pump SNo");
											label355.setBackground(Color.white);
											label355.setHorizontalAlignment(SwingConstants.CENTER);
											label355.setFont(new Font("Arial", Font.BOLD, 10));
											label355.setOpaque(true);
											label355.setBorder(null);
											label355.setName("label");
											pnlPrime43.add(label355, new TableLayoutConstraints(8, 1, 8, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

											//---- label356 ----
											label356.setText("Min");
											label356.setBackground(Color.white);
											label356.setHorizontalAlignment(SwingConstants.CENTER);
											label356.setFont(new Font("Arial", Font.BOLD, 10));
											label356.setOpaque(true);
											label356.setBorder(null);
											label356.setName("label");
											pnlPrime43.add(label356, new TableLayoutConstraints(9, 1, 9, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

											//---- label358 ----
											label358.setText("Pump SNo");
											label358.setBackground(Color.white);
											label358.setHorizontalAlignment(SwingConstants.CENTER);
											label358.setFont(new Font("Arial", Font.BOLD, 10));
											label358.setOpaque(true);
											label358.setBorder(null);
											label358.setName("label");
											pnlPrime43.add(label358, new TableLayoutConstraints(10, 1, 10, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

											//---- label359 ----
											label359.setText("Grntd");
											label359.setBackground(Color.white);
											label359.setHorizontalAlignment(SwingConstants.CENTER);
											label359.setFont(new Font("Arial", Font.BOLD, 10));
											label359.setOpaque(true);
											label359.setBorder(null);
											label359.setName("label");
											pnlPrime43.add(label359, new TableLayoutConstraints(11, 1, 11, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

											//---- label360 ----
											label360.setText("Max");
											label360.setBackground(Color.white);
											label360.setHorizontalAlignment(SwingConstants.CENTER);
											label360.setFont(new Font("Arial", Font.BOLD, 10));
											label360.setOpaque(true);
											label360.setBorder(null);
											label360.setName("label");
											pnlPrime43.add(label360, new TableLayoutConstraints(12, 1, 12, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

											//---- label361 ----
											label361.setText("Pump SNo");
											label361.setBackground(Color.white);
											label361.setHorizontalAlignment(SwingConstants.CENTER);
											label361.setFont(new Font("Arial", Font.BOLD, 10));
											label361.setOpaque(true);
											label361.setBorder(null);
											label361.setName("label");
											pnlPrime43.add(label361, new TableLayoutConstraints(13, 1, 13, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

											//---- label362 ----
											label362.setText("Min");
											label362.setBackground(Color.white);
											label362.setHorizontalAlignment(SwingConstants.CENTER);
											label362.setFont(new Font("Arial", Font.BOLD, 10));
											label362.setOpaque(true);
											label362.setBorder(null);
											label362.setName("label");
											pnlPrime43.add(label362, new TableLayoutConstraints(14, 1, 14, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

											//---- label363 ----
											label363.setText("Pump SNo");
											label363.setBackground(Color.white);
											label363.setHorizontalAlignment(SwingConstants.CENTER);
											label363.setFont(new Font("Arial", Font.BOLD, 10));
											label363.setOpaque(true);
											label363.setBorder(null);
											label363.setName("label");
											pnlPrime43.add(label363, new TableLayoutConstraints(15, 1, 15, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

											//---- label371 ----
											label371.setText("Grntd");
											label371.setBackground(Color.white);
											label371.setHorizontalAlignment(SwingConstants.CENTER);
											label371.setFont(new Font("Arial", Font.BOLD, 10));
											label371.setOpaque(true);
											label371.setBorder(null);
											label371.setName("label");
											pnlPrime43.add(label371, new TableLayoutConstraints(16, 1, 16, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

											//---- label372 ----
											label372.setText("Max");
											label372.setBackground(Color.white);
											label372.setHorizontalAlignment(SwingConstants.CENTER);
											label372.setFont(new Font("Arial", Font.BOLD, 10));
											label372.setOpaque(true);
											label372.setBorder(null);
											label372.setName("label");
											pnlPrime43.add(label372, new TableLayoutConstraints(17, 1, 17, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

											//---- label373 ----
											label373.setText("Pump SNo");
											label373.setBackground(Color.white);
											label373.setHorizontalAlignment(SwingConstants.CENTER);
											label373.setFont(new Font("Arial", Font.BOLD, 10));
											label373.setOpaque(true);
											label373.setBorder(null);
											label373.setName("label");
											pnlPrime43.add(label373, new TableLayoutConstraints(18, 1, 18, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

											//---- label374 ----
											label374.setText("Min");
											label374.setBackground(Color.white);
											label374.setHorizontalAlignment(SwingConstants.CENTER);
											label374.setFont(new Font("Arial", Font.BOLD, 10));
											label374.setOpaque(true);
											label374.setBorder(null);
											label374.setName("label");
											pnlPrime43.add(label374, new TableLayoutConstraints(19, 1, 19, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

											//---- label375 ----
											label375.setText("Pump SNo");
											label375.setBackground(Color.white);
											label375.setHorizontalAlignment(SwingConstants.CENTER);
											label375.setFont(new Font("Arial", Font.BOLD, 10));
											label375.setOpaque(true);
											label375.setBorder(null);
											label375.setName("label");
											pnlPrime43.add(label375, new TableLayoutConstraints(20, 1, 20, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

											//---- label364 ----
											label364.setText("Grntd");
											label364.setBackground(Color.white);
											label364.setHorizontalAlignment(SwingConstants.CENTER);
											label364.setFont(new Font("Arial", Font.BOLD, 10));
											label364.setOpaque(true);
											label364.setBorder(null);
											label364.setName("label");
											pnlPrime43.add(label364, new TableLayoutConstraints(21, 1, 21, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

											//---- label365 ----
											label365.setText("Max");
											label365.setBackground(Color.white);
											label365.setHorizontalAlignment(SwingConstants.CENTER);
											label365.setFont(new Font("Arial", Font.BOLD, 10));
											label365.setOpaque(true);
											label365.setBorder(null);
											label365.setName("label");
											pnlPrime43.add(label365, new TableLayoutConstraints(22, 1, 22, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

											//---- label366 ----
											label366.setText("Pump SNo");
											label366.setBackground(Color.white);
											label366.setHorizontalAlignment(SwingConstants.CENTER);
											label366.setFont(new Font("Arial", Font.BOLD, 10));
											label366.setOpaque(true);
											label366.setBorder(null);
											label366.setName("label");
											pnlPrime43.add(label366, new TableLayoutConstraints(23, 1, 23, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

											//---- label367 ----
											label367.setText("Min");
											label367.setBackground(Color.white);
											label367.setHorizontalAlignment(SwingConstants.CENTER);
											label367.setFont(new Font("Arial", Font.BOLD, 10));
											label367.setOpaque(true);
											label367.setBorder(null);
											label367.setName("label");
											pnlPrime43.add(label367, new TableLayoutConstraints(24, 1, 24, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

											//---- label368 ----
											label368.setText("Pump SNo");
											label368.setBackground(Color.white);
											label368.setHorizontalAlignment(SwingConstants.CENTER);
											label368.setFont(new Font("Arial", Font.BOLD, 10));
											label368.setOpaque(true);
											label368.setBorder(null);
											label368.setName("label");
											pnlPrime43.add(label368, new TableLayoutConstraints(25, 1, 25, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

											//---- tblRes43 ----
											tblRes43.setModel(new DefaultTableModel(
												new Object[][] {
													{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
												},
												new String[] {
													null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
												}
											) {
												Class<?>[] columnTypes = new Class<?>[] {
													Object.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, Object.class, Object.class, Object.class, Object.class, Object.class
												};
												@Override
												public Class<?> getColumnClass(int columnIndex) {
													return columnTypes[columnIndex];
												}
											});
											{
												TableColumnModel cm = tblRes43.getColumnModel();
												cm.getColumn(0).setMinWidth(110);
												cm.getColumn(0).setMaxWidth(110);
												cm.getColumn(0).setPreferredWidth(110);
												cm.getColumn(3).setMinWidth(65);
												cm.getColumn(3).setMaxWidth(65);
												cm.getColumn(3).setPreferredWidth(65);
												cm.getColumn(5).setMinWidth(65);
												cm.getColumn(5).setMaxWidth(65);
												cm.getColumn(5).setPreferredWidth(65);
												cm.getColumn(8).setMinWidth(65);
												cm.getColumn(8).setMaxWidth(65);
												cm.getColumn(8).setPreferredWidth(65);
												cm.getColumn(10).setMinWidth(65);
												cm.getColumn(10).setMaxWidth(65);
												cm.getColumn(10).setPreferredWidth(65);
												cm.getColumn(13).setMinWidth(65);
												cm.getColumn(13).setMaxWidth(65);
												cm.getColumn(13).setPreferredWidth(65);
												cm.getColumn(15).setMinWidth(65);
												cm.getColumn(15).setMaxWidth(65);
												cm.getColumn(15).setPreferredWidth(65);
												cm.getColumn(18).setMinWidth(65);
												cm.getColumn(18).setMaxWidth(65);
												cm.getColumn(18).setPreferredWidth(65);
												cm.getColumn(20).setMinWidth(65);
												cm.getColumn(20).setMaxWidth(65);
												cm.getColumn(20).setPreferredWidth(65);
												cm.getColumn(23).setMinWidth(65);
												cm.getColumn(23).setMaxWidth(65);
												cm.getColumn(23).setPreferredWidth(65);
												cm.getColumn(25).setMinWidth(65);
												cm.getColumn(25).setMaxWidth(65);
												cm.getColumn(25).setPreferredWidth(65);
											}
											tblRes43.setFont(new Font("Arial", Font.PLAIN, 10));
											tblRes43.setRowHeight(25);
											tblRes43.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
											tblRes43.setGridColor(Color.lightGray);
											tblRes43.setBorder(new LineBorder(Color.white));
											tblRes43.setRowSelectionAllowed(false);
											tblRes43.setAutoscrolls(false);
											tblRes43.setFocusable(false);
											tblRes43.setEnabled(false);
											tblRes43.setRowMargin(0);
											tblRes43.setName("outline");
											pnlPrime43.add(tblRes43, new TableLayoutConstraints(0, 2, 25, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
										}
										pnlPrint43.add(pnlPrime43, new TableLayoutConstraints(1, 10, 3, 10, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//======== pnlBot43 ========
										{
											pnlBot43.setBackground(Color.white);
											pnlBot43.setBorder(LineBorder.createBlackLineBorder());
											pnlBot43.setName("outline");
											pnlBot43.setLayout(new TableLayout(new double[][] {
												{TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL},
												{10, 20, 20}}));
											((TableLayout)pnlBot43.getLayout()).setHGap(5);
											((TableLayout)pnlBot43.getLayout()).setVGap(5);

											//---- lblTestedBy43 ----
											lblTestedBy43.setText("Tested By");
											lblTestedBy43.setFont(new Font("Arial", Font.PLAIN, 13));
											lblTestedBy43.setHorizontalAlignment(SwingConstants.CENTER);
											lblTestedBy43.setName("label");
											pnlBot43.add(lblTestedBy43, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

											//---- label101 ----
											label101.setText("Approved By");
											label101.setFont(new Font("Arial", Font.PLAIN, 13));
											label101.setHorizontalAlignment(SwingConstants.CENTER);
											label101.setName("label");
											pnlBot43.add(label101, new TableLayoutConstraints(3, 2, 3, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

											//---- lblVerBy8 ----
											lblVerBy8.setText("Verified By");
											lblVerBy8.setFont(new Font("Arial", Font.PLAIN, 13));
											lblVerBy8.setHorizontalAlignment(SwingConstants.CENTER);
											lblVerBy8.setName("label");
											pnlBot43.add(lblVerBy8, new TableLayoutConstraints(2, 2, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
										}
										pnlPrint43.add(pnlBot43, new TableLayoutConstraints(1, 11, 3, 11, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

										//---- lblAppVer43 ----
										lblAppVer43.setText("Doer PumpViewPro");
										lblAppVer43.setFont(new Font("Arial", Font.PLAIN, 10));
										lblAppVer43.setHorizontalAlignment(SwingConstants.RIGHT);
										lblAppVer43.setForeground(Color.gray);
										pnlPrint43.add(lblAppVer43, new TableLayoutConstraints(2, 13, 3, 13, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
									}
									printArea43.add(pnlPrint43, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
								}
								scrollPane43.setViewportView(printArea43);
							}
							pnlRep43.add(scrollPane43, new TableLayoutConstraints(0, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
						}
						tabISI.addTab("Maximum & Minimum Report", pnlRep43);
					}
					pnlRep4.add(tabISI, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				}
				tabReport.addTab("4. ISI Reports", pnlRep4);

				//======== pnlRep5 ========
				{
					pnlRep5.setLayout(new TableLayout(new double[][] {
						{TableLayout.FILL},
						{TableLayout.PREFERRED, TableLayout.FILL}}));

					//======== pnlFactor ========
					{
						pnlFactor.setBorder(new TitledBorder(null, "Comparison Factor", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
							new Font("Arial", Font.BOLD, 12), Color.blue));
						pnlFactor.setLayout(new TableLayout(new double[][] {
							{TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED},
							{TableLayout.PREFERRED}}));
						((TableLayout)pnlFactor.getLayout()).setHGap(5);
						((TableLayout)pnlFactor.getLayout()).setVGap(5);

						//---- chkQvH ----
						chkQvH.setText("Discharge Vs Total Head (Q Vs H)");
						chkQvH.setFont(new Font("Arial", Font.PLAIN, 12));
						chkQvH.setSelected(true);
						chkQvH.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								chkQvHActionPerformed();
							}
						});
						pnlFactor.add(chkQvH, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- chkQvMI ----
						chkQvMI.setText("Discharge Vs Motor Input (Q Vs MI)");
						chkQvMI.setFont(new Font("Arial", Font.PLAIN, 12));
						chkQvMI.setSelected(true);
						chkQvMI.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								chkQvMIActionPerformed();
							}
						});
						pnlFactor.add(chkQvMI, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- chkQvOE ----
						chkQvOE.setText("Discharge Vs O.A Eff. (Q Vs OE)");
						chkQvOE.setFont(new Font("Arial", Font.PLAIN, 12));
						chkQvOE.setSelected(true);
						chkQvOE.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								chkQvOEActionPerformed();
							}
						});
						pnlFactor.add(chkQvOE, new TableLayoutConstraints(2, 0, 2, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- chkQvC ----
						chkQvC.setText("Discharge Vs Current (Q Vs A)");
						chkQvC.setFont(new Font("Arial", Font.PLAIN, 12));
						chkQvC.setSelected(true);
						chkQvC.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								chkQvCActionPerformed();
							}
						});
						pnlFactor.add(chkQvC, new TableLayoutConstraints(3, 0, 3, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
					}
					pnlRep5.add(pnlFactor, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//======== scrollPane5 ========
					{

						//======== printArea5 ========
						{
							printArea5.setBorder(new LineBorder(Color.blue, 2));
							printArea5.setBackground(Color.white);
							printArea5.setLayout(new TableLayout(new double[][] {
								{900},
								{TableLayout.PREFERRED, 1450}}));

							//---- lblRtWarn5 ----
							lblRtWarn5.setText("For routine tests, performance graph and result dervied from graph may not be reliable due to less number of test heads");
							lblRtWarn5.setFont(new Font("Arial", Font.PLAIN, 12));
							lblRtWarn5.setHorizontalAlignment(SwingConstants.CENTER);
							lblRtWarn5.setBorder(new LineBorder(Color.orange, 2, true));
							lblRtWarn5.setOpaque(true);
							lblRtWarn5.setBackground(Color.yellow);
							lblRtWarn5.setForeground(Color.red);
							printArea5.add(lblRtWarn5, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

							//======== pnlPrint5 ========
							{
								pnlPrint5.setBorder(null);
								pnlPrint5.setBackground(Color.white);
								pnlPrint5.setLayout(new TableLayout(new double[][] {
									{40, 120, TableLayout.FILL, 120, 15},
									{35, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 500, TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED}}));

								//---- lblCustLogo5 ----
								lblCustLogo5.setHorizontalAlignment(SwingConstants.CENTER);
								pnlPrint5.add(lblCustLogo5, new TableLayoutConstraints(1, 1, 1, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblCompName5 ----
								lblCompName5.setText("Company Name");
								lblCompName5.setFont(new Font("Arial", Font.BOLD, 16));
								lblCompName5.setHorizontalAlignment(SwingConstants.CENTER);
								lblCompName5.setName("header");
								pnlPrint5.add(lblCompName5, new TableLayoutConstraints(2, 1, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblIS5 ----
								lblIS5.setText("IS:");
								lblIS5.setFont(new Font("Arial", Font.BOLD, 10));
								lblIS5.setHorizontalAlignment(SwingConstants.CENTER);
								lblIS5.setName("label");
								pnlPrint5.add(lblIS5, new TableLayoutConstraints(3, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblISILogo5 ----
								lblISILogo5.setIcon(new ImageIcon(getClass().getResource("/img/isi_logo.PNG")));
								lblISILogo5.setVerticalAlignment(SwingConstants.TOP);
								lblISILogo5.setHorizontalAlignment(SwingConstants.CENTER);
								lblISILogo5.setIconTextGap(0);
								pnlPrint5.add(lblISILogo5, new TableLayoutConstraints(3, 2, 3, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblCompAdr5 ----
								lblCompAdr5.setText("Address Line 1 and Line 2");
								lblCompAdr5.setFont(new Font("Arial", Font.BOLD, 12));
								lblCompAdr5.setHorizontalAlignment(SwingConstants.CENTER);
								lblCompAdr5.setName("header");
								pnlPrint5.add(lblCompAdr5, new TableLayoutConstraints(2, 3, 2, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblTitle5 ----
								lblTitle5.setText("GRAPH COMPARISON");
								lblTitle5.setFont(new Font("Arial", Font.BOLD, 14));
								lblTitle5.setHorizontalAlignment(SwingConstants.CENTER);
								lblTitle5.setName("header");
								pnlPrint5.add(lblTitle5, new TableLayoutConstraints(2, 5, 2, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblISIRefNo5 ----
								lblISIRefNo5.setText("ISI Ref No.");
								lblISIRefNo5.setHorizontalAlignment(SwingConstants.CENTER);
								lblISIRefNo5.setFont(new Font("Arial", Font.BOLD, 10));
								lblISIRefNo5.setName("label");
								pnlPrint5.add(lblISIRefNo5, new TableLayoutConstraints(3, 4, 3, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- label4 ----
								label4.setText("PUMP TYPE:");
								label4.setFont(new Font("Arial", Font.BOLD, 12));
								label4.setName("label");
								pnlPrint5.add(label4, new TableLayoutConstraints(1, 7, 1, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblPumpType5 ----
								lblPumpType5.setText("type");
								lblPumpType5.setFont(new Font("Arial", Font.PLAIN, 12));
								lblPumpType5.setName("actual");
								pnlPrint5.add(lblPumpType5, new TableLayoutConstraints(2, 7, 2, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblPage5 ----
								lblPage5.setText("Page 1 of 1");
								lblPage5.setFont(new Font("Arial", Font.PLAIN, 12));
								lblPage5.setHorizontalAlignment(SwingConstants.RIGHT);
								lblPage5.setName("label");
								pnlPrint5.add(lblPage5, new TableLayoutConstraints(3, 7, 3, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//======== pnlGraph5 ========
								{
									pnlGraph5.setBackground(Color.white);
									pnlGraph5.setAutoscrolls(true);
									pnlGraph5.setBorder(LineBorder.createBlackLineBorder());
									pnlGraph5.setEnabled(false);
									pnlGraph5.setName("outline");
									pnlGraph5.setLayout(new TableLayout(new double[][] {
										{TableLayout.FILL},
										{TableLayout.FILL}}));
									((TableLayout)pnlGraph5.getLayout()).setHGap(1);
									((TableLayout)pnlGraph5.getLayout()).setVGap(1);
								}
								pnlPrint5.add(pnlGraph5, new TableLayoutConstraints(1, 8, 3, 8, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//======== pnlBot5 ========
								{
									pnlBot5.setBackground(Color.white);
									pnlBot5.setBorder(LineBorder.createBlackLineBorder());
									pnlBot5.setName("outline");
									pnlBot5.setLayout(new TableLayout(new double[][] {
										{TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL},
										{10, 20, 20}}));
									((TableLayout)pnlBot5.getLayout()).setHGap(5);
									((TableLayout)pnlBot5.getLayout()).setVGap(5);

									//---- lblTestedBy5 ----
									lblTestedBy5.setText("Tested By");
									lblTestedBy5.setFont(new Font("Arial", Font.PLAIN, 13));
									lblTestedBy5.setHorizontalAlignment(SwingConstants.CENTER);
									lblTestedBy5.setName("label");
									pnlBot5.add(lblTestedBy5, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblVerBy7 ----
									lblVerBy7.setText("Verified By");
									lblVerBy7.setFont(new Font("Arial", Font.PLAIN, 13));
									lblVerBy7.setHorizontalAlignment(SwingConstants.CENTER);
									lblVerBy7.setName("label");
									pnlBot5.add(lblVerBy7, new TableLayoutConstraints(2, 2, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label104 ----
									label104.setText("Approved By");
									label104.setFont(new Font("Arial", Font.PLAIN, 13));
									label104.setHorizontalAlignment(SwingConstants.CENTER);
									label104.setName("label");
									pnlBot5.add(label104, new TableLayoutConstraints(3, 2, 3, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
								}
								pnlPrint5.add(pnlBot5, new TableLayoutConstraints(1, 9, 3, 9, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblAppVer5 ----
								lblAppVer5.setText("Doer PumpViewPro");
								lblAppVer5.setFont(new Font("Arial", Font.PLAIN, 10));
								lblAppVer5.setHorizontalAlignment(SwingConstants.RIGHT);
								lblAppVer5.setForeground(Color.gray);
								pnlPrint5.add(lblAppVer5, new TableLayoutConstraints(2, 11, 3, 11, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
							}
							printArea5.add(pnlPrint5, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
						}
						scrollPane5.setViewportView(printArea5);
					}
					pnlRep5.add(scrollPane5, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				}
				tabReport.addTab("5. Graphs Comparison", pnlRep5);

				//======== pnlRep6 ========
				{
					pnlRep6.setLayout(new TableLayout(new double[][] {
						{TableLayout.FILL},
						{TableLayout.PREFERRED, TableLayout.FILL}}));
					((TableLayout)pnlRep6.getLayout()).setHGap(5);
					((TableLayout)pnlRep6.getLayout()).setVGap(5);

					//---- cmdSaveCustom ----
					cmdSaveCustom.setText("Save Changes");
					cmdSaveCustom.setFont(new Font("Arial", Font.PLAIN, 14));
					cmdSaveCustom.setIcon(new ImageIcon(getClass().getResource("/img/save.png")));
					cmdSaveCustom.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							cmdSaveCustomActionPerformed();
						}
					});
					pnlRep6.add(cmdSaveCustom, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//======== scrollPane6 ========
					{
						scrollPane6.setAutoscrolls(true);

						//======== printArea6 ========
						{
							printArea6.setBorder(new LineBorder(Color.blue, 2));
							printArea6.setBackground(Color.white);
							printArea6.setLayout(new TableLayout(new double[][] {
								{1225},
								{910, 20}}));

							//======== pnlPrint6 ========
							{
								pnlPrint6.setBorder(null);
								pnlPrint6.setBackground(Color.white);
								pnlPrint6.setLayout(new TableLayout(new double[][] {
									{40, 120, TableLayout.FILL, 125, 15},
									{35, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, TableLayout.PREFERRED, 10, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 10, TableLayout.PREFERRED, TableLayout.PREFERRED, 10, TableLayout.PREFERRED, 70, TableLayout.PREFERRED, TableLayout.PREFERRED}}));

								//---- lblIS6 ----
								lblIS6.setText("IS:");
								lblIS6.setHorizontalAlignment(SwingConstants.CENTER);
								lblIS6.setFont(new Font("Arial", Font.BOLD, 10));
								lblIS6.setName("label");
								pnlPrint6.add(lblIS6, new TableLayoutConstraints(3, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblCustLogo6 ----
								lblCustLogo6.setHorizontalAlignment(SwingConstants.CENTER);
								pnlPrint6.add(lblCustLogo6, new TableLayoutConstraints(1, 1, 1, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblCompName6 ----
								lblCompName6.setText("Company Name");
								lblCompName6.setFont(new Font("Arial", Font.BOLD, 16));
								lblCompName6.setHorizontalAlignment(SwingConstants.CENTER);
								pnlPrint6.add(lblCompName6, new TableLayoutConstraints(2, 1, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblISILogo6 ----
								lblISILogo6.setHorizontalAlignment(SwingConstants.CENTER);
								lblISILogo6.setIcon(new ImageIcon(getClass().getResource("/img/isi_logo.PNG")));
								lblISILogo6.setVerticalAlignment(SwingConstants.TOP);
								lblISILogo6.setIconTextGap(0);
								pnlPrint6.add(lblISILogo6, new TableLayoutConstraints(3, 2, 3, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblCompAdr6 ----
								lblCompAdr6.setText("Address Line 1 and Line 2");
								lblCompAdr6.setFont(new Font("Arial", Font.BOLD, 12));
								lblCompAdr6.setHorizontalAlignment(SwingConstants.CENTER);
								pnlPrint6.add(lblCompAdr6, new TableLayoutConstraints(2, 3, 2, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblTitle6 ----
								lblTitle6.setText("PUMP TEST");
								lblTitle6.setFont(new Font("Arial", Font.BOLD, 14));
								lblTitle6.setHorizontalAlignment(SwingConstants.CENTER);
								pnlPrint6.add(lblTitle6, new TableLayoutConstraints(2, 5, 2, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblISIRefNo6 ----
								lblISIRefNo6.setHorizontalAlignment(SwingConstants.CENTER);
								lblISIRefNo6.setFont(new Font("Arial", Font.BOLD, 10));
								lblISIRefNo6.setText("ISI Ref No.");
								lblISIRefNo6.setName("label");
								pnlPrint6.add(lblISIRefNo6, new TableLayoutConstraints(3, 4, 3, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblPage6 ----
								lblPage6.setText("Page 1 of 1");
								lblPage6.setFont(new Font("Arial", Font.PLAIN, 12));
								lblPage6.setHorizontalAlignment(SwingConstants.RIGHT);
								pnlPrint6.add(lblPage6, new TableLayoutConstraints(3, 7, 3, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//======== pnlTop6 ========
								{
									pnlTop6.setBorder(LineBorder.createBlackLineBorder());
									pnlTop6.setBackground(Color.lightGray);
									pnlTop6.setFont(new Font("Arial", Font.BOLD, 12));
									pnlTop6.setLayout(new TableLayout(new double[][] {
										{100, 115, 100, TableLayout.FILL, 100, 95, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL},
										{25, 25, 25, 25, 25}}));
									((TableLayout)pnlTop6.getLayout()).setHGap(1);
									((TableLayout)pnlTop6.getLayout()).setVGap(1);

									//---- label83 ----
									label83.setText("  Model");
									label83.setFont(new Font("Arial", Font.BOLD, 13));
									label83.setBackground(Color.white);
									label83.setOpaque(true);
									pnlTop6.add(label83, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblPumpType6 ----
									lblPumpType6.setText("type");
									lblPumpType6.setFont(new Font("Arial", Font.PLAIN, 12));
									lblPumpType6.setBackground(Color.white);
									lblPumpType6.setOpaque(true);
									lblPumpType6.setBorder(new LineBorder(Color.white, 5));
									pnlTop6.add(lblPumpType6, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label91 ----
									label91.setText("  Phase");
									label91.setFont(new Font("Arial", Font.BOLD, 13));
									label91.setBackground(Color.white);
									label91.setOpaque(true);
									pnlTop6.add(label91, new TableLayoutConstraints(2, 0, 2, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblPh6 ----
									lblPh6.setText("ph");
									lblPh6.setFont(new Font("Arial", Font.PLAIN, 12));
									lblPh6.setBackground(Color.white);
									lblPh6.setOpaque(true);
									lblPh6.setBorder(new LineBorder(Color.white, 5));
									pnlTop6.add(lblPh6, new TableLayoutConstraints(3, 0, 3, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label92 ----
									label92.setText("  Pipe Sz. (mm)");
									label92.setFont(new Font("Arial", Font.BOLD, 13));
									label92.setBackground(Color.white);
									label92.setOpaque(true);
									pnlTop6.add(label92, new TableLayoutConstraints(4, 0, 4, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblPSz6 ----
									lblPSz6.setText("psize");
									lblPSz6.setFont(new Font("Arial", Font.PLAIN, 12));
									lblPSz6.setBackground(Color.white);
									lblPSz6.setOpaque(true);
									lblPSz6.setBorder(new LineBorder(Color.white, 5));
									pnlTop6.add(lblPSz6, new TableLayoutConstraints(5, 0, 5, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label111 ----
									label111.setText("  No. Of Stg.");
									label111.setFont(new Font("Arial", Font.BOLD, 13));
									label111.setBackground(Color.white);
									label111.setOpaque(true);
									pnlTop6.add(label111, new TableLayoutConstraints(6, 0, 6, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblNoStage6 ----
									lblNoStage6.setText("nstage");
									lblNoStage6.setFont(new Font("Arial", Font.PLAIN, 12));
									lblNoStage6.setBackground(Color.white);
									lblNoStage6.setOpaque(true);
									lblNoStage6.setBorder(new LineBorder(Color.white, 5));
									pnlTop6.add(lblNoStage6, new TableLayoutConstraints(7, 0, 7, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label85 ----
									label85.setText("  Pump Sl. No.");
									label85.setFont(new Font("Arial", Font.BOLD, 13));
									label85.setBackground(Color.white);
									label85.setOpaque(true);
									pnlTop6.add(label85, new TableLayoutConstraints(8, 0, 8, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblPSno6 ----
									lblPSno6.setText("pslno");
									lblPSno6.setFont(new Font("Arial", Font.PLAIN, 12));
									lblPSno6.setBackground(Color.white);
									lblPSno6.setOpaque(true);
									lblPSno6.setBorder(new LineBorder(Color.white, 5));
									pnlTop6.add(lblPSno6, new TableLayoutConstraints(9, 0, 9, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label105 ----
									label105.setText("Cold Resistance (Ohms)");
									label105.setFont(new Font("Arial", Font.BOLD, 15));
									label105.setBackground(Color.white);
									label105.setOpaque(true);
									label105.setHorizontalTextPosition(SwingConstants.RIGHT);
									label105.setHorizontalAlignment(SwingConstants.CENTER);
									pnlTop6.add(label105, new TableLayoutConstraints(10, 0, 12, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label89 ----
									label89.setText("  KW/HP");
									label89.setFont(new Font("Arial", Font.BOLD, 13));
									label89.setBackground(Color.white);
									label89.setOpaque(true);
									pnlTop6.add(label89, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblMRt6 ----
									lblMRt6.setText("mrt");
									lblMRt6.setFont(new Font("Arial", Font.PLAIN, 12));
									lblMRt6.setBackground(Color.white);
									lblMRt6.setOpaque(true);
									lblMRt6.setBorder(new LineBorder(Color.white, 5));
									pnlTop6.add(lblMRt6, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label95 ----
									label95.setText("  Freq. (Hz)");
									label95.setFont(new Font("Arial", Font.BOLD, 13));
									label95.setBackground(Color.white);
									label95.setOpaque(true);
									pnlTop6.add(label95, new TableLayoutConstraints(2, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblFreq6 ----
									lblFreq6.setText("freq");
									lblFreq6.setFont(new Font("Arial", Font.PLAIN, 12));
									lblFreq6.setBackground(Color.white);
									lblFreq6.setOpaque(true);
									lblFreq6.setBorder(new LineBorder(Color.white, 5));
									pnlTop6.add(lblFreq6, new TableLayoutConstraints(3, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label110 ----
									label110.setText("  Seed (rpm)");
									label110.setFont(new Font("Arial", Font.BOLD, 13));
									label110.setBackground(Color.white);
									label110.setOpaque(true);
									pnlTop6.add(label110, new TableLayoutConstraints(4, 1, 4, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblSpeed6 ----
									lblSpeed6.setText("speed");
									lblSpeed6.setFont(new Font("Arial", Font.PLAIN, 12));
									lblSpeed6.setBackground(Color.white);
									lblSpeed6.setOpaque(true);
									lblSpeed6.setBorder(new LineBorder(Color.white, 5));
									pnlTop6.add(lblSpeed6, new TableLayoutConstraints(5, 1, 5, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label112 ----
									label112.setText("  Motor Type");
									label112.setFont(new Font("Arial", Font.BOLD, 13));
									label112.setBackground(Color.white);
									label112.setOpaque(true);
									pnlTop6.add(label112, new TableLayoutConstraints(6, 1, 6, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblMotType6 ----
									lblMotType6.setText("mtype");
									lblMotType6.setFont(new Font("Arial", Font.PLAIN, 12));
									lblMotType6.setBackground(Color.white);
									lblMotType6.setOpaque(true);
									lblMotType6.setBorder(new LineBorder(Color.white, 5));
									pnlTop6.add(lblMotType6, new TableLayoutConstraints(7, 1, 7, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label90 ----
									label90.setText("  Date");
									label90.setFont(new Font("Arial", Font.BOLD, 13));
									label90.setBackground(Color.white);
									label90.setOpaque(true);
									pnlTop6.add(label90, new TableLayoutConstraints(8, 1, 8, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblDt6 ----
									lblDt6.setText("dt");
									lblDt6.setFont(new Font("Arial", Font.PLAIN, 12));
									lblDt6.setBackground(Color.white);
									lblDt6.setOpaque(true);
									lblDt6.setBorder(new LineBorder(Color.white, 5));
									pnlTop6.add(lblDt6, new TableLayoutConstraints(9, 1, 9, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label117 ----
									label117.setText("Main");
									label117.setFont(new Font("Arial", Font.BOLD, 13));
									label117.setBackground(Color.white);
									label117.setOpaque(true);
									label117.setHorizontalAlignment(SwingConstants.CENTER);
									pnlTop6.add(label117, new TableLayoutConstraints(10, 1, 10, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label118 ----
									label118.setText("Aux");
									label118.setFont(new Font("Arial", Font.BOLD, 13));
									label118.setBackground(Color.white);
									label118.setOpaque(true);
									label118.setHorizontalAlignment(SwingConstants.CENTER);
									pnlTop6.add(label118, new TableLayoutConstraints(11, 1, 11, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label119 ----
									label119.setText("Temp");
									label119.setFont(new Font("Arial", Font.BOLD, 13));
									label119.setBackground(Color.white);
									label119.setOpaque(true);
									label119.setHorizontalAlignment(SwingConstants.CENTER);
									pnlTop6.add(label119, new TableLayoutConstraints(12, 1, 12, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label94 ----
									label94.setText("  Voltage (V)");
									label94.setFont(new Font("Arial", Font.BOLD, 13));
									label94.setBackground(Color.white);
									label94.setOpaque(true);
									pnlTop6.add(label94, new TableLayoutConstraints(0, 2, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblV6 ----
									lblV6.setText("v");
									lblV6.setFont(new Font("Arial", Font.PLAIN, 12));
									lblV6.setBackground(Color.white);
									lblV6.setOpaque(true);
									lblV6.setBorder(new LineBorder(Color.white, 5));
									pnlTop6.add(lblV6, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblHdDuty6 ----
									lblHdDuty6.setText("  Duty Head (m)");
									lblHdDuty6.setFont(new Font("Arial", Font.BOLD, 13));
									lblHdDuty6.setBackground(Color.white);
									lblHdDuty6.setOpaque(true);
									pnlTop6.add(lblHdDuty6, new TableLayoutConstraints(2, 2, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblHead6 ----
									lblHead6.setText("head");
									lblHead6.setFont(new Font("Arial", Font.PLAIN, 12));
									lblHead6.setBackground(Color.white);
									lblHead6.setOpaque(true);
									lblHead6.setBorder(new LineBorder(Color.white, 5));
									pnlTop6.add(lblHead6, new TableLayoutConstraints(3, 2, 3, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblHdRg6 ----
									lblHdRg6.setText("  Hd. Range (m)");
									lblHdRg6.setFont(new Font("Arial", Font.BOLD, 13));
									lblHdRg6.setBackground(Color.white);
									lblHdRg6.setOpaque(true);
									pnlTop6.add(lblHdRg6, new TableLayoutConstraints(4, 2, 4, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblHeadR6 ----
									lblHeadR6.setText("hr");
									lblHeadR6.setFont(new Font("Arial", Font.PLAIN, 12));
									lblHeadR6.setBackground(Color.white);
									lblHeadR6.setOpaque(true);
									lblHeadR6.setBorder(new LineBorder(Color.white, 5));
									pnlTop6.add(lblHeadR6, new TableLayoutConstraints(5, 2, 5, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label113 ----
									label113.setText("  Eff %");
									label113.setFont(new Font("Arial", Font.BOLD, 13));
									label113.setBackground(Color.white);
									label113.setOpaque(true);
									pnlTop6.add(label113, new TableLayoutConstraints(6, 2, 6, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblMotEff6 ----
									lblMotEff6.setText("me");
									lblMotEff6.setFont(new Font("Arial", Font.PLAIN, 12));
									lblMotEff6.setBackground(Color.white);
									lblMotEff6.setOpaque(true);
									lblMotEff6.setBorder(new LineBorder(Color.white, 5));
									pnlTop6.add(lblMotEff6, new TableLayoutConstraints(7, 2, 7, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label115 ----
									label115.setText("  Capacitor");
									label115.setFont(new Font("Arial", Font.BOLD, 13));
									label115.setBackground(Color.white);
									label115.setOpaque(true);
									pnlTop6.add(label115, new TableLayoutConstraints(8, 2, 8, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- txtCap6 ----
									txtCap6.setBorder(new LineBorder(Color.white, 5));
									txtCap6.setBackground(Color.white);
									txtCap6.setFont(new Font("Arial", Font.PLAIN, 12));
									pnlTop6.add(txtCap6, new TableLayoutConstraints(9, 2, 9, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- txtCRMain6 ----
									txtCRMain6.setBorder(new LineBorder(Color.white, 5));
									txtCRMain6.setHorizontalAlignment(SwingConstants.CENTER);
									txtCRMain6.setBackground(Color.white);
									txtCRMain6.setFont(new Font("Arial", Font.PLAIN, 12));
									pnlTop6.add(txtCRMain6, new TableLayoutConstraints(10, 2, 10, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- txtCRAux6 ----
									txtCRAux6.setBorder(new LineBorder(Color.white, 5));
									txtCRAux6.setHorizontalAlignment(SwingConstants.CENTER);
									txtCRAux6.setBackground(Color.white);
									txtCRAux6.setFont(new Font("Arial", Font.PLAIN, 12));
									pnlTop6.add(txtCRAux6, new TableLayoutConstraints(11, 2, 11, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- txtCRTemp6 ----
									txtCRTemp6.setBorder(new LineBorder(Color.white, 5));
									txtCRTemp6.setHorizontalAlignment(SwingConstants.CENTER);
									txtCRTemp6.setBackground(Color.white);
									txtCRTemp6.setFont(new Font("Arial", Font.PLAIN, 12));
									pnlTop6.add(txtCRTemp6, new TableLayoutConstraints(12, 2, 12, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label99 ----
									label99.setText("  Current (A)");
									label99.setFont(new Font("Arial", Font.BOLD, 13));
									label99.setBackground(Color.white);
									label99.setOpaque(true);
									pnlTop6.add(label99, new TableLayoutConstraints(0, 3, 0, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblCur6 ----
									lblCur6.setText("a");
									lblCur6.setFont(new Font("Arial", Font.PLAIN, 12));
									lblCur6.setBackground(Color.white);
									lblCur6.setOpaque(true);
									lblCur6.setBorder(new LineBorder(Color.white, 5));
									pnlTop6.add(lblCur6, new TableLayoutConstraints(1, 3, 1, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblFlowRtLabel6 ----
									lblFlowRtLabel6.setText("  Duty Disc (unit)");
									lblFlowRtLabel6.setFont(new Font("Arial", Font.BOLD, 13));
									lblFlowRtLabel6.setBackground(Color.white);
									lblFlowRtLabel6.setOpaque(true);
									pnlTop6.add(lblFlowRtLabel6, new TableLayoutConstraints(2, 3, 2, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblFlow6 ----
									lblFlow6.setText("flow");
									lblFlow6.setFont(new Font("Arial", Font.PLAIN, 12));
									lblFlow6.setBackground(Color.white);
									lblFlow6.setOpaque(true);
									lblFlow6.setBorder(new LineBorder(Color.white, 5));
									pnlTop6.add(lblFlow6, new TableLayoutConstraints(3, 3, 3, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblFlowRtLabel62 ----
									lblFlowRtLabel62.setText("  Dis Range (unit)");
									lblFlowRtLabel62.setFont(new Font("Arial", Font.BOLD, 13));
									lblFlowRtLabel62.setBackground(Color.white);
									lblFlowRtLabel62.setOpaque(true);
									pnlTop6.add(lblFlowRtLabel62, new TableLayoutConstraints(4, 3, 4, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblDisR6 ----
									lblDisR6.setText("fr");
									lblDisR6.setFont(new Font("Arial", Font.PLAIN, 12));
									lblDisR6.setBackground(Color.white);
									lblDisR6.setOpaque(true);
									lblDisR6.setBorder(new LineBorder(Color.white, 5));
									pnlTop6.add(lblDisR6, new TableLayoutConstraints(5, 3, 5, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label114 ----
									label114.setText("  Duty");
									label114.setFont(new Font("Arial", Font.BOLD, 13));
									label114.setBackground(Color.white);
									label114.setOpaque(true);
									pnlTop6.add(label114, new TableLayoutConstraints(6, 3, 6, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- txtDuty6 ----
									txtDuty6.setBorder(new LineBorder(Color.white, 5));
									txtDuty6.setFont(new Font("Arial", Font.PLAIN, 12));
									pnlTop6.add(txtDuty6, new TableLayoutConstraints(7, 3, 7, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label116 ----
									label116.setText("  Connection");
									label116.setFont(new Font("Arial", Font.BOLD, 13));
									label116.setBackground(Color.white);
									label116.setOpaque(true);
									pnlTop6.add(label116, new TableLayoutConstraints(8, 3, 8, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- txtCon6 ----
									txtCon6.setBorder(new LineBorder(Color.white, 5));
									txtCon6.setBackground(Color.white);
									txtCon6.setFont(new Font("Arial", Font.PLAIN, 12));
									pnlTop6.add(txtCon6, new TableLayoutConstraints(9, 3, 9, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label214 ----
									label214.setFont(new Font("Arial", Font.BOLD, 12));
									label214.setBackground(Color.white);
									label214.setOpaque(true);
									pnlTop6.add(label214, new TableLayoutConstraints(10, 3, 10, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label215 ----
									label215.setFont(new Font("Arial", Font.BOLD, 12));
									label215.setBackground(Color.white);
									label215.setOpaque(true);
									pnlTop6.add(label215, new TableLayoutConstraints(11, 3, 11, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label216 ----
									label216.setFont(new Font("Arial", Font.BOLD, 12));
									label216.setBackground(Color.white);
									label216.setOpaque(true);
									pnlTop6.add(label216, new TableLayoutConstraints(12, 3, 12, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label93 ----
									label93.setText("  Motor IP (kW)");
									label93.setFont(new Font("Arial", Font.BOLD, 13));
									label93.setBackground(Color.white);
									label93.setOpaque(true);
									pnlTop6.add(label93, new TableLayoutConstraints(0, 4, 0, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblMotIp6 ----
									lblMotIp6.setText("mi");
									lblMotIp6.setFont(new Font("Arial", Font.PLAIN, 12));
									lblMotIp6.setBackground(Color.white);
									lblMotIp6.setOpaque(true);
									lblMotIp6.setBorder(new LineBorder(Color.white, 5));
									pnlTop6.add(lblMotIp6, new TableLayoutConstraints(1, 4, 1, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblHdSO6 ----
									lblHdSO6.setText("  SO Head (m)");
									lblHdSO6.setFont(new Font("Arial", Font.BOLD, 13));
									lblHdSO6.setBackground(Color.white);
									lblHdSO6.setOpaque(true);
									pnlTop6.add(lblHdSO6, new TableLayoutConstraints(2, 4, 2, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblSHHd6 ----
									lblSHHd6.setText("sh hd");
									lblSHHd6.setFont(new Font("Arial", Font.PLAIN, 12));
									lblSHHd6.setBackground(Color.white);
									lblSHHd6.setOpaque(true);
									lblSHHd6.setBorder(new LineBorder(Color.white, 5));
									pnlTop6.add(lblSHHd6, new TableLayoutConstraints(3, 4, 3, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label121 ----
									label121.setText("  SO Current (A)");
									label121.setFont(new Font("Arial", Font.BOLD, 13));
									label121.setBackground(Color.white);
									label121.setOpaque(true);
									pnlTop6.add(label121, new TableLayoutConstraints(4, 4, 4, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblSHCur6 ----
									lblSHCur6.setText("sh cur");
									lblSHCur6.setFont(new Font("Arial", Font.PLAIN, 12));
									lblSHCur6.setBackground(Color.white);
									lblSHCur6.setOpaque(true);
									lblSHCur6.setBorder(new LineBorder(Color.white, 5));
									pnlTop6.add(lblSHCur6, new TableLayoutConstraints(5, 4, 5, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label124 ----
									label124.setText("  SO P (kW)");
									label124.setFont(new Font("Arial", Font.BOLD, 13));
									label124.setBackground(Color.white);
									label124.setOpaque(true);
									pnlTop6.add(label124, new TableLayoutConstraints(6, 4, 6, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblSHPow6 ----
									lblSHPow6.setText("sh pow");
									lblSHPow6.setFont(new Font("Arial", Font.PLAIN, 12));
									lblSHPow6.setBackground(Color.white);
									lblSHPow6.setOpaque(true);
									lblSHPow6.setBorder(new LineBorder(Color.white, 5));
									pnlTop6.add(lblSHPow6, new TableLayoutConstraints(7, 4, 7, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label212 ----
									label212.setFont(new Font("Arial", Font.BOLD, 12));
									label212.setBackground(Color.white);
									label212.setOpaque(true);
									pnlTop6.add(label212, new TableLayoutConstraints(8, 4, 8, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label213 ----
									label213.setFont(new Font("Arial", Font.BOLD, 12));
									label213.setBackground(Color.white);
									label213.setOpaque(true);
									pnlTop6.add(label213, new TableLayoutConstraints(9, 4, 9, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label125 ----
									label125.setText("  Suction Static Bed (m)");
									label125.setFont(new Font("Arial", Font.BOLD, 13));
									label125.setBackground(Color.white);
									label125.setOpaque(true);
									pnlTop6.add(label125, new TableLayoutConstraints(10, 4, 11, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- txtSBed6 ----
									txtSBed6.setBorder(new LineBorder(Color.white, 5));
									txtSBed6.setBackground(Color.white);
									txtSBed6.setFont(new Font("Arial", Font.PLAIN, 12));
									pnlTop6.add(txtSBed6, new TableLayoutConstraints(12, 4, 12, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
								}
								pnlPrint6.add(pnlTop6, new TableLayoutConstraints(1, 8, 3, 8, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//======== panel2 ========
								{
									panel2.setBackground(Color.white);
									panel2.setLayout(new TableLayout(new double[][] {
										{5, 120, 75, 100, 146, 75},
										{25, 25, 10, 25}}));
									((TableLayout)panel2.getLayout()).setHGap(1);
									((TableLayout)panel2.getLayout()).setVGap(1);

									//---- label84 ----
									label84.setText("1. Insulation Resistance Test");
									label84.setFont(new Font("Arial", Font.BOLD, 15));
									label84.setBackground(Color.white);
									label84.setOpaque(true);
									panel2.add(label84, new TableLayoutConstraints(1, 0, 2, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label218 ----
									label218.setText("2. Self Priming Test");
									label218.setFont(new Font("Arial", Font.BOLD, 15));
									label218.setBackground(Color.white);
									label218.setOpaque(true);
									panel2.add(label218, new TableLayoutConstraints(4, 0, 5, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label217 ----
									label217.setText("  IR Val. (MOhms)");
									label217.setFont(new Font("Arial", Font.BOLD, 13));
									label217.setBackground(Color.white);
									label217.setOpaque(true);
									label217.setBorder(new LineBorder(Color.lightGray));
									panel2.add(label217, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- txtIRTest ----
									txtIRTest.setBorder(new LineBorder(Color.lightGray));
									txtIRTest.setBackground(Color.white);
									panel2.add(txtIRTest, new TableLayoutConstraints(2, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label219 ----
									label219.setText("  Self Prim. Time (sec)");
									label219.setFont(new Font("Arial", Font.BOLD, 13));
									label219.setBackground(Color.white);
									label219.setOpaque(true);
									label219.setBorder(new LineBorder(Color.lightGray));
									panel2.add(label219, new TableLayoutConstraints(4, 1, 4, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- txtSPTime ----
									txtSPTime.setBorder(new LineBorder(Color.lightGray));
									txtSPTime.setBackground(Color.white);
									panel2.add(txtSPTime, new TableLayoutConstraints(5, 1, 5, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label220 ----
									label220.setText("3. Discharge Test");
									label220.setFont(new Font("Arial", Font.BOLD, 15));
									label220.setBackground(Color.white);
									label220.setOpaque(true);
									panel2.add(label220, new TableLayoutConstraints(1, 3, 3, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
								}
								pnlPrint6.add(panel2, new TableLayoutConstraints(1, 10, 3, 10, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//======== pnlPrime6 ========
								{
									pnlPrime6.setBackground(Color.black);
									pnlPrime6.setAutoscrolls(true);
									pnlPrime6.setBorder(LineBorder.createBlackLineBorder());
									pnlPrime6.setLayout(new TableLayout(new double[][] {
										{TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL},
										{TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL}}));
									((TableLayout)pnlPrime6.getLayout()).setHGap(1);
									((TableLayout)pnlPrime6.getLayout()).setVGap(1);

									//---- label189 ----
									label189.setText("Test SNo.");
									label189.setBackground(Color.white);
									label189.setHorizontalAlignment(SwingConstants.CENTER);
									label189.setFont(new Font("Arial", Font.BOLD, 12));
									label189.setOpaque(true);
									label189.setBorder(null);
									pnlPrime6.add(label189, new TableLayoutConstraints(0, 0, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label221 ----
									label221.setText("Speed");
									label221.setBackground(Color.white);
									label221.setHorizontalAlignment(SwingConstants.CENTER);
									label221.setFont(new Font("Arial", Font.BOLD, 12));
									label221.setOpaque(true);
									label221.setBorder(null);
									pnlPrime6.add(label221, new TableLayoutConstraints(1, 0, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label106 ----
									label106.setText("HEAD");
									label106.setBackground(Color.white);
									label106.setOpaque(true);
									label106.setFont(new Font("Arial", Font.BOLD, 12));
									label106.setHorizontalAlignment(SwingConstants.CENTER);
									label106.setBorder(null);
									pnlPrime6.add(label106, new TableLayoutConstraints(2, 0, 6, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label138 ----
									label138.setText("FLOW");
									label138.setBackground(Color.white);
									label138.setHorizontalAlignment(SwingConstants.CENTER);
									label138.setFont(new Font("Arial", Font.BOLD, 12));
									label138.setOpaque(true);
									label138.setAutoscrolls(true);
									label138.setBorder(null);
									pnlPrime6.add(label138, new TableLayoutConstraints(7, 0, 7, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label193 ----
									label193.setText("POWER");
									label193.setBackground(Color.white);
									label193.setHorizontalAlignment(SwingConstants.CENTER);
									label193.setFont(new Font("Arial", Font.BOLD, 12));
									label193.setOpaque(true);
									label193.setAutoscrolls(true);
									label193.setBorder(null);
									pnlPrime6.add(label193, new TableLayoutConstraints(8, 0, 11, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label192 ----
									label192.setText("Frequency");
									label192.setBackground(Color.white);
									label192.setHorizontalAlignment(SwingConstants.CENTER);
									label192.setFont(new Font("Arial", Font.BOLD, 12));
									label192.setOpaque(true);
									label192.setBorder(null);
									pnlPrime6.add(label192, new TableLayoutConstraints(11, 1, 11, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblPerfHz6 ----
									lblPerfHz6.setText("Performance At Rated Freq X Hz");
									lblPerfHz6.setBackground(Color.white);
									lblPerfHz6.setHorizontalAlignment(SwingConstants.CENTER);
									lblPerfHz6.setFont(new Font("Arial", Font.BOLD, 12));
									lblPerfHz6.setOpaque(true);
									lblPerfHz6.setAutoscrolls(true);
									lblPerfHz6.setBorder(null);
									pnlPrime6.add(lblPerfHz6, new TableLayoutConstraints(12, 0, 16, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label343 ----
									label343.setText("<html><body align=\"center\">Suction Head</body></html>");
									label343.setBackground(Color.white);
									label343.setHorizontalAlignment(SwingConstants.CENTER);
									label343.setFont(new Font("Arial", Font.BOLD, 12));
									label343.setOpaque(true);
									label343.setBorder(null);
									pnlPrime6.add(label343, new TableLayoutConstraints(2, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label357 ----
									label357.setText("<html><body align=\"center\">Delivery Head</body></html>");
									label357.setBackground(Color.white);
									label357.setHorizontalAlignment(SwingConstants.CENTER);
									label357.setFont(new Font("Arial", Font.BOLD, 12));
									label357.setOpaque(true);
									label357.setBorder(null);
									pnlPrime6.add(label357, new TableLayoutConstraints(3, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label107 ----
									label107.setText("<html><body align=\"center\">Head<br>Correction</body></html>");
									label107.setOpaque(true);
									label107.setBackground(Color.white);
									label107.setFont(new Font("Arial", Font.BOLD, 12));
									pnlPrime6.add(label107, new TableLayoutConstraints(4, 1, 4, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label369 ----
									label369.setText("<html><body align=\"center\">Velocity Head Corr.</body></html>");
									label369.setBackground(Color.white);
									label369.setHorizontalAlignment(SwingConstants.CENTER);
									label369.setFont(new Font("Arial", Font.BOLD, 12));
									label369.setOpaque(true);
									label369.setBorder(null);
									pnlPrime6.add(label369, new TableLayoutConstraints(5, 1, 5, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label370 ----
									label370.setText("<html><body align=\"center\">Total Head</body></html>");
									label370.setBackground(Color.white);
									label370.setHorizontalAlignment(SwingConstants.CENTER);
									label370.setFont(new Font("Arial", Font.BOLD, 12));
									label370.setOpaque(true);
									label370.setBorder(null);
									pnlPrime6.add(label370, new TableLayoutConstraints(6, 1, 6, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label195 ----
									label195.setText("<html><body align=\"center\">Discharge</body></html>");
									label195.setBackground(Color.white);
									label195.setHorizontalAlignment(SwingConstants.CENTER);
									label195.setFont(new Font("Arial", Font.BOLD, 12));
									label195.setOpaque(true);
									label195.setAutoscrolls(true);
									label195.setBorder(null);
									pnlPrime6.add(label195, new TableLayoutConstraints(7, 1, 7, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label196 ----
									label196.setText("Voltage");
									label196.setBackground(Color.white);
									label196.setHorizontalAlignment(SwingConstants.CENTER);
									label196.setFont(new Font("Arial", Font.BOLD, 12));
									label196.setOpaque(true);
									label196.setAutoscrolls(true);
									label196.setBorder(null);
									pnlPrime6.add(label196, new TableLayoutConstraints(8, 1, 8, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label197 ----
									label197.setText("Current");
									label197.setBackground(Color.white);
									label197.setHorizontalAlignment(SwingConstants.CENTER);
									label197.setFont(new Font("Arial", Font.BOLD, 12));
									label197.setOpaque(true);
									label197.setAutoscrolls(true);
									label197.setBorder(null);
									pnlPrime6.add(label197, new TableLayoutConstraints(9, 1, 9, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label198 ----
									label198.setText("Power");
									label198.setBackground(Color.white);
									label198.setHorizontalAlignment(SwingConstants.CENTER);
									label198.setFont(new Font("Arial", Font.BOLD, 12));
									label198.setOpaque(true);
									label198.setAutoscrolls(true);
									label198.setBorder(null);
									pnlPrime6.add(label198, new TableLayoutConstraints(10, 1, 10, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label199 ----
									label199.setText("Flow Rate");
									label199.setBackground(Color.white);
									label199.setHorizontalAlignment(SwingConstants.CENTER);
									label199.setFont(new Font("Arial", Font.BOLD, 12));
									label199.setOpaque(true);
									label199.setAutoscrolls(true);
									label199.setBorder(null);
									pnlPrime6.add(label199, new TableLayoutConstraints(12, 1, 12, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label200 ----
									label200.setText("<html><body align=\"center\">Total Head</body></html>");
									label200.setBackground(Color.white);
									label200.setHorizontalAlignment(SwingConstants.CENTER);
									label200.setFont(new Font("Arial", Font.BOLD, 12));
									label200.setOpaque(true);
									label200.setAutoscrolls(true);
									label200.setBorder(null);
									pnlPrime6.add(label200, new TableLayoutConstraints(13, 1, 13, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label201 ----
									label201.setText("<html><body align=\"center\">Motor<br>Input</body></html>");
									label201.setBackground(Color.white);
									label201.setHorizontalAlignment(SwingConstants.CENTER);
									label201.setFont(new Font("Arial", Font.BOLD, 12));
									label201.setOpaque(true);
									label201.setAutoscrolls(true);
									label201.setBorder(null);
									pnlPrime6.add(label201, new TableLayoutConstraints(14, 1, 14, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label7 ----
									label7.setText("Overall Eff.");
									label7.setBackground(Color.white);
									label7.setFont(new Font("Arial", Font.BOLD, 12));
									label7.setOpaque(true);
									label7.setHorizontalAlignment(SwingConstants.CENTER);
									label7.setBorder(null);
									pnlPrime6.add(label7, new TableLayoutConstraints(15, 1, 15, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label29 ----
									label29.setText("Pump Eff.");
									label29.setBackground(Color.white);
									label29.setFont(new Font("Arial", Font.BOLD, 12));
									label29.setOpaque(true);
									label29.setHorizontalAlignment(SwingConstants.CENTER);
									label29.setBorder(null);
									pnlPrime6.add(label29, new TableLayoutConstraints(16, 1, 16, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label222 ----
									label222.setText("rpm");
									label222.setBackground(Color.white);
									label222.setHorizontalAlignment(SwingConstants.CENTER);
									label222.setFont(new Font("Arial", Font.BOLD, 12));
									label222.setOpaque(true);
									label222.setAutoscrolls(true);
									label222.setBorder(null);
									pnlPrime6.add(label222, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblHdS6 ----
									lblHdS6.setText("mwc");
									lblHdS6.setBackground(Color.white);
									lblHdS6.setHorizontalAlignment(SwingConstants.CENTER);
									lblHdS6.setFont(new Font("Arial", Font.BOLD, 12));
									lblHdS6.setOpaque(true);
									lblHdS6.setAutoscrolls(true);
									lblHdS6.setBorder(null);
									pnlPrime6.add(lblHdS6, new TableLayoutConstraints(2, 2, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblHdD6 ----
									lblHdD6.setText("mwc");
									lblHdD6.setBackground(Color.white);
									lblHdD6.setHorizontalAlignment(SwingConstants.CENTER);
									lblHdD6.setFont(new Font("Arial", Font.BOLD, 12));
									lblHdD6.setOpaque(true);
									lblHdD6.setAutoscrolls(true);
									lblHdD6.setBorder(null);
									pnlPrime6.add(lblHdD6, new TableLayoutConstraints(3, 2, 3, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblHdHC6 ----
									lblHdHC6.setText("m");
									lblHdHC6.setOpaque(true);
									lblHdHC6.setBackground(Color.white);
									lblHdHC6.setFont(new Font("Arial", Font.BOLD, 12));
									lblHdHC6.setHorizontalAlignment(SwingConstants.CENTER);
									pnlPrime6.add(lblHdHC6, new TableLayoutConstraints(4, 2, 4, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblHdVHC6 ----
									lblHdVHC6.setText("m");
									lblHdVHC6.setBackground(Color.white);
									lblHdVHC6.setHorizontalAlignment(SwingConstants.CENTER);
									lblHdVHC6.setFont(new Font("Arial", Font.BOLD, 12));
									lblHdVHC6.setOpaque(true);
									lblHdVHC6.setAutoscrolls(true);
									lblHdVHC6.setBorder(null);
									pnlPrime6.add(lblHdVHC6, new TableLayoutConstraints(5, 2, 5, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblHdT6 ----
									lblHdT6.setText("m");
									lblHdT6.setBackground(Color.white);
									lblHdT6.setHorizontalAlignment(SwingConstants.CENTER);
									lblHdT6.setFont(new Font("Arial", Font.BOLD, 12));
									lblHdT6.setOpaque(true);
									lblHdT6.setAutoscrolls(true);
									lblHdT6.setBorder(null);
									pnlPrime6.add(lblHdT6, new TableLayoutConstraints(6, 2, 6, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblFlowUnitS6 ----
									lblFlowUnitS6.setText("unit");
									lblFlowUnitS6.setBackground(Color.white);
									lblFlowUnitS6.setHorizontalAlignment(SwingConstants.CENTER);
									lblFlowUnitS6.setFont(new Font("Arial", Font.BOLD, 12));
									lblFlowUnitS6.setOpaque(true);
									lblFlowUnitS6.setAutoscrolls(true);
									lblFlowUnitS6.setBorder(null);
									pnlPrime6.add(lblFlowUnitS6, new TableLayoutConstraints(7, 2, 7, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label207 ----
									label207.setText("V");
									label207.setBackground(Color.white);
									label207.setHorizontalAlignment(SwingConstants.CENTER);
									label207.setFont(new Font("Arial", Font.BOLD, 12));
									label207.setOpaque(true);
									label207.setAutoscrolls(true);
									label207.setBorder(null);
									pnlPrime6.add(label207, new TableLayoutConstraints(8, 2, 8, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label208 ----
									label208.setText("A");
									label208.setBackground(Color.white);
									label208.setHorizontalAlignment(SwingConstants.CENTER);
									label208.setFont(new Font("Arial", Font.BOLD, 12));
									label208.setOpaque(true);
									label208.setAutoscrolls(true);
									label208.setBorder(null);
									pnlPrime6.add(label208, new TableLayoutConstraints(9, 2, 9, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label209 ----
									label209.setText("kW");
									label209.setBackground(Color.white);
									label209.setHorizontalAlignment(SwingConstants.CENTER);
									label209.setFont(new Font("Arial", Font.BOLD, 12));
									label209.setOpaque(true);
									label209.setAutoscrolls(true);
									label209.setBorder(null);
									pnlPrime6.add(label209, new TableLayoutConstraints(10, 2, 10, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label202 ----
									label202.setText("Hz");
									label202.setBackground(Color.white);
									label202.setHorizontalAlignment(SwingConstants.CENTER);
									label202.setFont(new Font("Arial", Font.BOLD, 12));
									label202.setOpaque(true);
									label202.setAutoscrolls(true);
									label202.setBorder(null);
									pnlPrime6.add(label202, new TableLayoutConstraints(11, 2, 11, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblFlowUnitS62 ----
									lblFlowUnitS62.setText("unit");
									lblFlowUnitS62.setBackground(Color.white);
									lblFlowUnitS62.setHorizontalAlignment(SwingConstants.CENTER);
									lblFlowUnitS62.setFont(new Font("Arial", Font.BOLD, 12));
									lblFlowUnitS62.setOpaque(true);
									lblFlowUnitS62.setAutoscrolls(true);
									lblFlowUnitS62.setBorder(null);
									pnlPrime6.add(lblFlowUnitS62, new TableLayoutConstraints(12, 2, 12, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblHdRT6 ----
									lblHdRT6.setText("mtrs");
									lblHdRT6.setBackground(Color.white);
									lblHdRT6.setHorizontalAlignment(SwingConstants.CENTER);
									lblHdRT6.setFont(new Font("Arial", Font.BOLD, 12));
									lblHdRT6.setOpaque(true);
									lblHdRT6.setAutoscrolls(true);
									lblHdRT6.setBorder(null);
									pnlPrime6.add(lblHdRT6, new TableLayoutConstraints(13, 2, 13, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label211 ----
									label211.setText("kW");
									label211.setBackground(Color.white);
									label211.setHorizontalAlignment(SwingConstants.CENTER);
									label211.setFont(new Font("Arial", Font.BOLD, 12));
									label211.setOpaque(true);
									label211.setAutoscrolls(true);
									label211.setBorder(null);
									pnlPrime6.add(label211, new TableLayoutConstraints(14, 2, 14, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label223 ----
									label223.setText("%");
									label223.setBackground(Color.white);
									label223.setHorizontalAlignment(SwingConstants.CENTER);
									label223.setFont(new Font("Arial", Font.BOLD, 12));
									label223.setOpaque(true);
									label223.setAutoscrolls(true);
									label223.setBorder(null);
									pnlPrime6.add(label223, new TableLayoutConstraints(15, 2, 15, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label46 ----
									label46.setText("%");
									label46.setBackground(Color.white);
									label46.setFont(new Font("Arial", Font.BOLD, 12));
									label46.setOpaque(true);
									label46.setHorizontalAlignment(SwingConstants.CENTER);
									label46.setBorder(null);
									pnlPrime6.add(label46, new TableLayoutConstraints(16, 2, 16, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- tblRes6 ----
									tblRes6.setModel(new DefaultTableModel(
										new Object[][] {
											{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
											{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
											{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
											{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
											{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
											{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
											{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
											{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
											{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
											{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
										},
										new String[] {
											null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
										}
									) {
										Class<?>[] columnTypes = new Class<?>[] {
											String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, Object.class
										};
										@Override
										public Class<?> getColumnClass(int columnIndex) {
											return columnTypes[columnIndex];
										}
									});
									tblRes6.setFont(new Font("Arial", Font.PLAIN, 12));
									tblRes6.setRowHeight(25);
									tblRes6.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
									tblRes6.setGridColor(Color.lightGray);
									tblRes6.setBorder(new LineBorder(Color.white));
									tblRes6.setRowSelectionAllowed(false);
									tblRes6.setAutoscrolls(false);
									tblRes6.setFocusable(false);
									tblRes6.setEnabled(false);
									tblRes6.setIntercellSpacing(new Dimension(10, 1));
									pnlPrime6.add(tblRes6, new TableLayoutConstraints(0, 3, 16, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
								}
								pnlPrint6.add(pnlPrime6, new TableLayoutConstraints(1, 12, 3, 12, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- label256 ----
								label256.setText("4. Temperature Rise Test");
								label256.setFont(new Font("Arial", Font.BOLD, 15));
								label256.setBackground(Color.white);
								label256.setOpaque(true);
								pnlPrint6.add(label256, new TableLayoutConstraints(1, 14, 2, 14, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//======== pnlBot6_2 ========
								{
									pnlBot6_2.setBackground(Color.white);
									pnlBot6_2.setBorder(null);
									pnlBot6_2.setLayout(new TableLayout(new double[][] {
										{TableLayout.FILL, TableLayout.FILL, TableLayout.FILL},
										{TableLayout.FILL, 22}}));
									((TableLayout)pnlBot6_2.getLayout()).setHGap(5);
									((TableLayout)pnlBot6_2.getLayout()).setVGap(5);

									//---- lblTestedBy6 ----
									lblTestedBy6.setText("Tested By");
									lblTestedBy6.setFont(new Font("Arial", Font.PLAIN, 13));
									lblTestedBy6.setHorizontalAlignment(SwingConstants.CENTER);
									pnlBot6_2.add(lblTestedBy6, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblVerBy6 ----
									lblVerBy6.setText("Verified By");
									lblVerBy6.setFont(new Font("Arial", Font.PLAIN, 13));
									lblVerBy6.setHorizontalAlignment(SwingConstants.CENTER);
									pnlBot6_2.add(lblVerBy6, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label109 ----
									label109.setText("Approved By");
									label109.setFont(new Font("Arial", Font.PLAIN, 13));
									label109.setHorizontalAlignment(SwingConstants.CENTER);
									pnlBot6_2.add(label109, new TableLayoutConstraints(2, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
								}
								pnlPrint6.add(pnlBot6_2, new TableLayoutConstraints(1, 18, 3, 18, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//======== pnlBot6_1 ========
								{
									pnlBot6_1.setBorder(LineBorder.createBlackLineBorder());
									pnlBot6_1.setBackground(Color.lightGray);
									pnlBot6_1.setFont(new Font("Arial", Font.BOLD, 12));
									pnlBot6_1.setLayout(new TableLayout(new double[][] {
										{TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL},
										{25, 25, 25}}));
									((TableLayout)pnlBot6_1.getLayout()).setHGap(1);
									((TableLayout)pnlBot6_1.getLayout()).setVGap(1);

									//---- label249 ----
									label249.setText("Voltage (V)");
									label249.setFont(new Font("Arial", Font.BOLD, 13));
									label249.setBackground(Color.white);
									label249.setOpaque(true);
									label249.setHorizontalAlignment(SwingConstants.CENTER);
									pnlBot6_1.add(label249, new TableLayoutConstraints(0, 0, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label250 ----
									label250.setText("Current (A)");
									label250.setFont(new Font("Arial", Font.BOLD, 13));
									label250.setBackground(Color.white);
									label250.setOpaque(true);
									label250.setHorizontalAlignment(SwingConstants.CENTER);
									pnlBot6_1.add(label250, new TableLayoutConstraints(1, 0, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label251 ----
									label251.setText("Power (kW)");
									label251.setFont(new Font("Arial", Font.BOLD, 13));
									label251.setBackground(Color.white);
									label251.setOpaque(true);
									label251.setHorizontalAlignment(SwingConstants.CENTER);
									pnlBot6_1.add(label251, new TableLayoutConstraints(2, 0, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label252 ----
									label252.setText("Speed (rpm)");
									label252.setFont(new Font("Arial", Font.BOLD, 13));
									label252.setBackground(Color.white);
									label252.setOpaque(true);
									label252.setHorizontalAlignment(SwingConstants.CENTER);
									pnlBot6_1.add(label252, new TableLayoutConstraints(3, 0, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label258 ----
									label258.setText("Freq. (Hz)");
									label258.setFont(new Font("Arial", Font.BOLD, 13));
									label258.setBackground(Color.white);
									label258.setOpaque(true);
									label258.setHorizontalAlignment(SwingConstants.CENTER);
									pnlBot6_1.add(label258, new TableLayoutConstraints(4, 0, 4, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label253 ----
									label253.setText("Total Head");
									label253.setFont(new Font("Arial", Font.BOLD, 13));
									label253.setBackground(Color.white);
									label253.setOpaque(true);
									label253.setHorizontalAlignment(SwingConstants.CENTER);
									pnlBot6_1.add(label253, new TableLayoutConstraints(5, 0, 5, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label267 ----
									label267.setText("<html>Body Temp (<sup>o</sup>C)</html>");
									label267.setFont(new Font("Arial", Font.BOLD, 13));
									label267.setBackground(Color.white);
									label267.setOpaque(true);
									label267.setHorizontalAlignment(SwingConstants.CENTER);
									pnlBot6_1.add(label267, new TableLayoutConstraints(6, 0, 6, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label268 ----
									label268.setText("<html>Room Temp (<sup>o</sup>C)</html>");
									label268.setFont(new Font("Arial", Font.BOLD, 13));
									label268.setBackground(Color.white);
									label268.setOpaque(true);
									label268.setHorizontalAlignment(SwingConstants.CENTER);
									pnlBot6_1.add(label268, new TableLayoutConstraints(7, 0, 7, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label255 ----
									label255.setText("Hot Resistance (Ohms)");
									label255.setFont(new Font("Arial", Font.BOLD, 15));
									label255.setBackground(Color.white);
									label255.setOpaque(true);
									label255.setHorizontalTextPosition(SwingConstants.RIGHT);
									label255.setHorizontalAlignment(SwingConstants.CENTER);
									pnlBot6_1.add(label255, new TableLayoutConstraints(8, 0, 10, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label254 ----
									label254.setText("Hot Resistance (Ohms)");
									label254.setFont(new Font("Arial", Font.BOLD, 15));
									label254.setBackground(Color.white);
									label254.setOpaque(true);
									label254.setHorizontalTextPosition(SwingConstants.RIGHT);
									label254.setHorizontalAlignment(SwingConstants.CENTER);
									pnlBot6_1.add(label254, new TableLayoutConstraints(8, 0, 10, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label271 ----
									label271.setText("<html>Winding Temp (<sup>o</sup>C)</html>");
									label271.setFont(new Font("Arial", Font.BOLD, 15));
									label271.setBackground(Color.white);
									label271.setOpaque(true);
									label271.setHorizontalAlignment(SwingConstants.CENTER);
									pnlBot6_1.add(label271, new TableLayoutConstraints(11, 0, 13, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label263 ----
									label263.setText("Main");
									label263.setFont(new Font("Arial", Font.BOLD, 13));
									label263.setBackground(Color.white);
									label263.setOpaque(true);
									label263.setHorizontalAlignment(SwingConstants.CENTER);
									pnlBot6_1.add(label263, new TableLayoutConstraints(8, 1, 8, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label260 ----
									label260.setText("Main");
									label260.setFont(new Font("Arial", Font.BOLD, 13));
									label260.setBackground(Color.white);
									label260.setOpaque(true);
									label260.setHorizontalAlignment(SwingConstants.CENTER);
									pnlBot6_1.add(label260, new TableLayoutConstraints(8, 1, 8, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label264 ----
									label264.setText("Aux");
									label264.setFont(new Font("Arial", Font.BOLD, 13));
									label264.setBackground(Color.white);
									label264.setOpaque(true);
									label264.setHorizontalAlignment(SwingConstants.CENTER);
									pnlBot6_1.add(label264, new TableLayoutConstraints(9, 1, 9, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label261 ----
									label261.setText("Aux");
									label261.setFont(new Font("Arial", Font.BOLD, 13));
									label261.setBackground(Color.white);
									label261.setOpaque(true);
									label261.setHorizontalAlignment(SwingConstants.CENTER);
									pnlBot6_1.add(label261, new TableLayoutConstraints(9, 1, 9, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label265 ----
									label265.setText("Temp");
									label265.setFont(new Font("Arial", Font.BOLD, 13));
									label265.setBackground(Color.white);
									label265.setOpaque(true);
									label265.setHorizontalAlignment(SwingConstants.CENTER);
									pnlBot6_1.add(label265, new TableLayoutConstraints(10, 1, 10, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label262 ----
									label262.setText("Temp");
									label262.setFont(new Font("Arial", Font.BOLD, 13));
									label262.setBackground(Color.white);
									label262.setOpaque(true);
									label262.setHorizontalAlignment(SwingConstants.CENTER);
									pnlBot6_1.add(label262, new TableLayoutConstraints(10, 1, 10, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label266 ----
									label266.setText("Main");
									label266.setFont(new Font("Arial", Font.BOLD, 13));
									label266.setBackground(Color.white);
									label266.setOpaque(true);
									label266.setHorizontalAlignment(SwingConstants.CENTER);
									pnlBot6_1.add(label266, new TableLayoutConstraints(11, 1, 11, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label269 ----
									label269.setText("Aux");
									label269.setFont(new Font("Arial", Font.BOLD, 13));
									label269.setBackground(Color.white);
									label269.setOpaque(true);
									label269.setHorizontalAlignment(SwingConstants.CENTER);
									pnlBot6_1.add(label269, new TableLayoutConstraints(12, 1, 12, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label270 ----
									label270.setText("Temp");
									label270.setFont(new Font("Arial", Font.BOLD, 13));
									label270.setBackground(Color.white);
									label270.setOpaque(true);
									label270.setHorizontalAlignment(SwingConstants.CENTER);
									pnlBot6_1.add(label270, new TableLayoutConstraints(13, 1, 13, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- txtV6 ----
									txtV6.setBorder(new LineBorder(Color.white, 5));
									txtV6.setBackground(Color.white);
									txtV6.setFont(new Font("Arial", Font.PLAIN, 12));
									pnlBot6_1.add(txtV6, new TableLayoutConstraints(0, 2, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- txtA6 ----
									txtA6.setBorder(new LineBorder(Color.white, 5));
									txtA6.setBackground(Color.white);
									txtA6.setFont(new Font("Arial", Font.PLAIN, 12));
									pnlBot6_1.add(txtA6, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- txtP6 ----
									txtP6.setBorder(new LineBorder(Color.white, 5));
									txtP6.setBackground(Color.white);
									txtP6.setFont(new Font("Arial", Font.PLAIN, 12));
									pnlBot6_1.add(txtP6, new TableLayoutConstraints(2, 2, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- txtS6 ----
									txtS6.setBorder(new LineBorder(Color.white, 5));
									txtS6.setBackground(Color.white);
									txtS6.setFont(new Font("Arial", Font.PLAIN, 12));
									pnlBot6_1.add(txtS6, new TableLayoutConstraints(3, 2, 3, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- txtF6 ----
									txtF6.setBorder(new LineBorder(Color.white, 5));
									txtF6.setBackground(Color.white);
									txtF6.setFont(new Font("Arial", Font.PLAIN, 12));
									pnlBot6_1.add(txtF6, new TableLayoutConstraints(4, 2, 4, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- txtH6 ----
									txtH6.setBorder(new LineBorder(Color.white, 5));
									txtH6.setBackground(Color.white);
									txtH6.setFont(new Font("Arial", Font.PLAIN, 12));
									pnlBot6_1.add(txtH6, new TableLayoutConstraints(5, 2, 5, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- txtBT6 ----
									txtBT6.setBorder(new LineBorder(Color.white, 5));
									txtBT6.setBackground(Color.white);
									txtBT6.setFont(new Font("Arial", Font.PLAIN, 12));
									pnlBot6_1.add(txtBT6, new TableLayoutConstraints(6, 2, 6, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- txtRT6 ----
									txtRT6.setBorder(new LineBorder(Color.white, 5));
									txtRT6.setBackground(Color.white);
									txtRT6.setFont(new Font("Arial", Font.PLAIN, 12));
									pnlBot6_1.add(txtRT6, new TableLayoutConstraints(7, 2, 7, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- txtM61 ----
									txtM61.setBorder(new LineBorder(Color.white, 5));
									txtM61.setHorizontalAlignment(SwingConstants.CENTER);
									txtM61.setBackground(Color.white);
									txtM61.setFont(new Font("Arial", Font.PLAIN, 12));
									pnlBot6_1.add(txtM61, new TableLayoutConstraints(8, 2, 8, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- txtA61 ----
									txtA61.setBorder(new LineBorder(Color.white, 5));
									txtA61.setHorizontalAlignment(SwingConstants.CENTER);
									txtA61.setBackground(Color.white);
									txtA61.setFont(new Font("Arial", Font.PLAIN, 12));
									pnlBot6_1.add(txtA61, new TableLayoutConstraints(9, 2, 9, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- txtT61 ----
									txtT61.setBorder(new LineBorder(Color.white, 5));
									txtT61.setHorizontalAlignment(SwingConstants.CENTER);
									txtT61.setBackground(Color.white);
									txtT61.setFont(new Font("Arial", Font.PLAIN, 12));
									pnlBot6_1.add(txtT61, new TableLayoutConstraints(10, 2, 10, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- txtM62 ----
									txtM62.setBorder(new LineBorder(Color.white, 5));
									txtM62.setHorizontalAlignment(SwingConstants.CENTER);
									txtM62.setBackground(Color.white);
									txtM62.setFont(new Font("Arial", Font.PLAIN, 12));
									pnlBot6_1.add(txtM62, new TableLayoutConstraints(11, 2, 11, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- txtA62 ----
									txtA62.setBorder(new LineBorder(Color.white, 5));
									txtA62.setHorizontalAlignment(SwingConstants.CENTER);
									txtA62.setBackground(Color.white);
									txtA62.setFont(new Font("Arial", Font.PLAIN, 12));
									pnlBot6_1.add(txtA62, new TableLayoutConstraints(12, 2, 12, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- txtT62 ----
									txtT62.setBorder(new LineBorder(Color.white, 5));
									txtT62.setHorizontalAlignment(SwingConstants.CENTER);
									txtT62.setBackground(Color.white);
									txtT62.setFont(new Font("Arial", Font.PLAIN, 12));
									pnlBot6_1.add(txtT62, new TableLayoutConstraints(13, 2, 13, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
								}
								pnlPrint6.add(pnlBot6_1, new TableLayoutConstraints(1, 15, 3, 15, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- label257 ----
								label257.setText(" Remarks");
								label257.setFont(new Font("Arial", Font.BOLD, 15));
								label257.setBackground(Color.white);
								label257.setOpaque(true);
								pnlPrint6.add(label257, new TableLayoutConstraints(1, 17, 1, 17, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- txtRem ----
								txtRem.setBorder(new LineBorder(Color.white, 5));
								txtRem.setBackground(Color.white);
								txtRem.setFont(new Font("Arial", Font.PLAIN, 12));
								pnlPrint6.add(txtRem, new TableLayoutConstraints(2, 17, 2, 17, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblAppVer6 ----
								lblAppVer6.setText("Doer PumpViewPro");
								lblAppVer6.setFont(new Font("Arial", Font.PLAIN, 10));
								lblAppVer6.setHorizontalAlignment(SwingConstants.RIGHT);
								lblAppVer6.setForeground(Color.gray);
								pnlPrint6.add(lblAppVer6, new TableLayoutConstraints(2, 19, 3, 20, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
							}
							printArea6.add(pnlPrint6, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
						}
						scrollPane6.setViewportView(printArea6);
					}
					pnlRep6.add(scrollPane6, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				}
				tabReport.addTab("6. Custom Report", pnlRep6);
			}
			printContainer.add(tabReport, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		}
		contentPane.add(printContainer, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		setSize(1370, 730);
		setLocationRelativeTo(getOwner());

		//---- buttonGroup1 ----
		ButtonGroup buttonGroup1 = new ButtonGroup();
		buttonGroup1.add(optRt);
		buttonGroup1.add(optType);
		buttonGroup1.add(optBoth);

		//---- buttonGroup2 ----
		ButtonGroup buttonGroup2 = new ButtonGroup();
		buttonGroup2.add(optRndrLine);
		buttonGroup2.add(optRndrSm);

		//---- buttonGroup3 ----
		ButtonGroup buttonGroup3 = new ButtonGroup();
		buttonGroup3.add(optSpeed);
		buttonGroup3.add(optFreq);
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}
	
	// custom code - begin
	private void customInit() {
		// associate func keys
		associateFunctionKeys();
		
		// set screen size
		this.setMinimumSize(new Dimension((int)this.getSize().getWidth(), (int)java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getHeight()));
		
		// software water mark
		if (Configuration.REP_SHOW_APP_WMARK.equals("1") || Configuration.IS_TRIAL_ON) {
			lblAppVer.setText(Configuration.APP_AUTHOR + " " + Configuration.APP_VERSION);
			lblAppVer2.setText(Configuration.APP_AUTHOR + " " + Configuration.APP_VERSION);
			lblAppVer3.setText(Configuration.APP_AUTHOR + " " + Configuration.APP_VERSION);
			lblAppVer41.setText(Configuration.APP_AUTHOR + " " + Configuration.APP_VERSION);
			lblAppVer42.setText(Configuration.APP_AUTHOR + " " + Configuration.APP_VERSION);
			lblAppVer43.setText(Configuration.APP_AUTHOR + " " + Configuration.APP_VERSION);
			lblAppVer5.setText(Configuration.APP_AUTHOR + " " + Configuration.APP_VERSION);
			lblAppVer6.setText(Configuration.APP_AUTHOR + " " + Configuration.APP_VERSION);
		} else {
			lblAppVer.setVisible(false);
			lblAppVer2.setVisible(false);
			lblAppVer3.setVisible(false);
			lblAppVer41.setVisible(false);
			lblAppVer42.setVisible(false);
			lblAppVer5.setVisible(false);
			lblAppVer6.setVisible(false);
		}
		
		// verified by
		boolean isVis = Configuration.REP_SHOW_VERIFIED_BY.equals("1");
		lblVerBy6.setVisible(isVis);
		lblVerBy7.setVisible(isVis);
		lblVerBy8.setVisible(isVis);
		lblVerBy9.setVisible(isVis);
		lblVerBy10.setVisible(isVis);
		lblVerBy11.setVisible(isVis);
		lblVerBy12.setVisible(isVis);
		lblVerBy13.setVisible(isVis);
		lblVerBy14.setVisible(isVis);
		
		// optional tests
		if (Configuration.IS_SP_TEST_ON.equals("0")) {
			pnlRep2.getLayout().removeLayoutComponent(pnlOptTest2);
			pnlPrint41.getLayout().removeLayoutComponent(pnlOptTest41);
		}
		// casing test
		isVis = Configuration.REP_SHOW_CASING_TEST.equals("1");
		lblCas2.setVisible(isVis);
		lblMaxDis2.setVisible(isVis);
		lblWith2.setVisible(false); // not required as its combined in above one line
		lblCas41.setVisible(isVis);
		lblMaxDis41.setVisible(isVis);
		lblWith41.setVisible(false); // not required as its combined in above one line
		// lblWith2.setText("Withstood this pressure by " + Configuration.REP_CASTING_TEST_MIN);
		// lblWith41.setText(lblWith2.getText());
		
		// other notes
		if (Configuration.REP_SHOW_NOTES.equals("1")) {
			lblGC2.setText("                " + Configuration.REP_NOTES_HEADING);
			lblGCText2.setText("                " + Configuration.REP_NOTES_TEXT);
			lblGC41.setText("                " + Configuration.REP_NOTES_HEADING);
			lblGCText41.setText("                " + Configuration.REP_NOTES_TEXT);
		} else {
			lblGC2.setVisible(false);
			lblGCText2.setVisible(false);
			lblGC41.setVisible(false);
			lblGCText41.setVisible(false);
		}

		// set mnemonic for tabs
		tabReport.setMnemonicAt(0, '1');
		tabReport.setMnemonicAt(1, '2');
		tabReport.setMnemonicAt(2, '3');
		tabReport.setMnemonicAt(3, '4');
		tabReport.setMnemonicAt(4, '5');
		if (Configuration.REP_SHOW_CUST_REP.equals("0")) {
			tabReport.remove(5);
		} else {
			tabReport.setMnemonicAt(5, '6');
		}
		
		// align test tables' content to right
		DefaultTableCellRenderer cRdr = new DefaultTableCellRenderer();
		cRdr.setHorizontalAlignment(JLabel.RIGHT);
		tblRes.setDefaultRenderer(Object.class, cRdr);
		tblRes42.setDefaultRenderer(Object.class, cRdr);
		tblRes43.setDefaultRenderer(Object.class, cRdr);
		tblRes6.setDefaultRenderer(Object.class, cRdr);
		
		// for highlighting rated values
		cRdr = new MyTableCellRender();
		cRdr.setHorizontalAlignment(JLabel.RIGHT);
		tblRes2.setDefaultRenderer(Object.class, cRdr);
		tblRes41.setDefaultRenderer(Object.class, cRdr);
		
		// adding list of discharge units & set default one
		cmbDisUnit.addItem("lps");
		cmbDisUnit.addItem("lpm");
		cmbDisUnit.addItem("lph");
		cmbDisUnit.addItem("gps");
		cmbDisUnit.addItem("gpm");
		cmbDisUnit.addItem("gph");
		cmbDisUnit.addItem("<html>m<sup>3</sup>ph</html>");
		
		// adding list of heads
		cmbHdUnit.addItem("m");
		cmbHdUnit.addItem("ft");
		cmbHdUnit.addItem("bar");
					
		MyResultSet res = null;
		try {
			
			db = Database.getInstance();
			
			// load existing pump type and choose the default one
			pumpTypeIdList.clear();
			pumpTypeNonISIList.clear();
			pumpDisUnit.clear();
			pumpHdUnit.clear();
			pumpCatList.clear();
			pumpTypeMVList.clear();
			pumpRtVList.clear();
			res = db.executeQuery("select pump_type_id, type, recent_pump_sno, volts, other_volts, discharge_unit, head_unit, non_isi_model, category from " + Configuration.PUMPTYPE);
			while (res.next()) {
				cmbType.addItem(res.getString("type"));
				if (res.getString("non_isi_model").equals("Y")) {
					pumpTypeNonISIList.add(res.getString("type"));
				}
				if (!res.getString("other_volts").isEmpty()) {
					pumpTypeMVList.add(res.getString("type"));
				}
				
				curDisUnit = res.getString("discharge_unit");
				curHdUnit = res.getString("head_unit");
				curPumpCat = res.getString("category");
				curRtV = res.getString("volts");
				pumpTypeIdList.put(res.getString("type"), res.getString("pump_type_id"));
				pumpDisUnit.put(res.getString("type"), curDisUnit);
				pumpHdUnit.put(res.getString("type"), curHdUnit);
				pumpCatList.put(res.getString("type"), curPumpCat);
				pumpRtVList.put(res.getString("type"), curRtV);
			}
			
			// below label belongs to max/min report
			if (pumpTypeNonISIList.size() > 0) {
				lblNonISI.setVisible(true);
			} else {
				lblNonISI.setVisible(false);
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
			
			// load list of default tests
			optBoth.setSelected(true);
			Calendar cal = Calendar.getInstance();
			toDt.setDate(cal.getTime());
			// by default last seven days test reported when no particular pump selected for report
			cal.add(Calendar.DATE, -7);
			if (cal.compareTo(recPumpDt) > 0) {
				fromDt.setDate(recPumpDt.getTime());
			} else {
				pnlFilter.setBorder(new TitledBorder(null, "Test List [Last one week tests are displayed by default]", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
						new Font("Arial", Font.BOLD, 12), Color.blue));
				fromDt.setDate(cal.getTime());
			}
			
			initInProgress = false;
			
			// set default pump type
			if (curPumpType.isEmpty()) {
				cmbType.setSelectedIndex(0);
			} else {
				cmbType.setSelectedItem(curPumpType);
			}
			curPumpType = cmbType.getSelectedItem().toString();
			curPumpTypeId = pumpTypeIdList.get(curPumpType);
			curPumpCat = pumpCatList.get(curPumpType);
			
			// set default discharge unit
			defDisUnit = pumpDisUnit.get(curPumpType);
			curDisUnit = defDisUnit;
			if (defDisUnit.contains("sup")) {
				cmbDisUnit.setSelectedItem("<html>" + defDisUnit + "</html>");
			} else {
				cmbDisUnit.setSelectedItem(defDisUnit);
			}
			lblDisUnit.setText("<html>Pump's Default is " + defDisUnit + "</html>");
			
			// set default head unit
			defHdUnit = pumpHdUnit.get(curPumpType);
			curHdUnit = defHdUnit;
			cmbHdUnit.setSelectedItem(defHdUnit);
			lblHdUnit.setText("<html>Pump's Default is " + defHdUnit + "</html>");
					
			// default rated factor
			optFreq.setSelected(true);
			
			// default graph rendering
			optRndrSm.setSelected(true);
			
			setReportHeader();
			refreshTestList();
			resetRepColors();
			handleISI();
			
			// show the graph of recently tested pump by default
			int p = 1;
			for (String val : pumpList.values()) {
				if (val.equals(recPumpNo)) {
					curPage = p;
					break;
				}
				p++;
			}
			
			tabReport.setSelectedIndex(3);
			tabReportStateChanged();
			
			// hide custom report
			
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(this, "Error:" + e.getMessage());
			return;
		} 
		
		enableDisableButtons();
	}
	
	// function to change the layout of reports based on chosen ISI
	private void handleISI() {
		// ISI handling
		
		/* 1. NAME PLATE DETAILS ACCORDING TO ISI */
		
		if(Configuration.LAST_USED_ISSTD.startsWith("IS 8034:") || Configuration.LAST_USED_ISSTD.startsWith("IS 14220:")) {
			// self priming time not required
			lbllblSPTime2.setVisible(false);
			lblSPTime2.setVisible(false);
			lbllblSPTime41.setVisible(false);
			lblSPTime41.setVisible(false);
						
			// insul class not required for 8034, hence replace it with no of stages
			if (Configuration.LAST_USED_ISSTD.startsWith("IS 8034:")) {
				lbllblIns2.setVisible(false);
				lblIns2.setVisible(false);
				lbllblIns41.setVisible(false);
				lblIns41.setVisible(false);
				pnlTop2.add(lbllblNoOfStg2, new TableLayoutConstraints(1, 6, 1, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				pnlTop2.add(lblNoStg2, new TableLayoutConstraints(2, 6, 2, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				pnlTop41.add(lbllblNoStg41, new TableLayoutConstraints(1, 6, 1, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				pnlTop41.add(lblNoStg41, new TableLayoutConstraints(2, 6, 2, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				// move min boresize left as self priming time not required
				pnlTop2.add(lbllblBrSz2, new TableLayoutConstraints(5, 6, 5, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				pnlTop2.add(lblBrSz2, new TableLayoutConstraints(6, 6, 6, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				pnlTop41.add(lbllblBrSz41, new TableLayoutConstraints(5, 6, 5, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				pnlTop41.add(lblBrSz41, new TableLayoutConstraints(6, 6, 6, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			} else {
				// show no of stage in place self priming time
				pnlTop2.add(lbllblNoOfStg2, new TableLayoutConstraints(5, 6, 5, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				pnlTop2.add(lblNoStg2, new TableLayoutConstraints(6, 6, 6, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				pnlTop41.add(lbllblNoStg41, new TableLayoutConstraints(5, 6, 5, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				pnlTop41.add(lblNoStg41, new TableLayoutConstraints(6, 6, 6, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			
		} else { // 8472, 9079, 6595
			
			// hide unwanted header params (belongs to only 8034 & 14220)
			if (!Configuration.LAST_USED_ISSTD.startsWith("IS 6595:") && !(Configuration.REP_SHOW_PUMP_EFF.equals("1") && Configuration.REP_SHOW_MOT_EFF.equals("1"))) {
				lbllblMotType2.setVisible(false);
				lblMotType2.setVisible(false);
				lbllblMotType41.setVisible(false);
				lblMotType41.setVisible(false);
				lbllblMSno2.setVisible(false);
				lblMSno2.setVisible(false);
				lbllblMSno41.setVisible(false);
				lblMSno41.setVisible(false);
				// show over all eff in place of min bore size as the row where over all eff is going to be deleted
				pnlTop2.add(lbllblOE2, new TableLayoutConstraints(7, 6, 7, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				pnlTop2.add(lblOEff2, new TableLayoutConstraints(8, 6, 8, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				pnlTop41.add(lbllblOE41, new TableLayoutConstraints(7, 6, 7, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				pnlTop41.add(lblOEff41, new TableLayoutConstraints(8, 6, 8, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				((TableLayout)pnlTop2.getLayout()).deleteRow(5);
				((TableLayout)pnlTop41.getLayout()).deleteRow(5);
			}
			
			lbllblBrSz2.setVisible(false);
			lblBrSz2.setVisible(false);
			lbllblBrSz41.setVisible(false);
			lblBrSz41.setVisible(false);
			lbllblNoOfStg2.setVisible(false);
			lblNoStg2.setVisible(false);
			lbllblNoStg41.setVisible(false);
			lblNoStg41.setVisible(false);
			
			// self priming time not required for 9079, 6595
			if (Configuration.LAST_USED_ISSTD.startsWith("IS 9079:") || Configuration.LAST_USED_ISSTD.startsWith("IS 6595:")) {
				lbllblSPTime2.setVisible(false);
				lblSPTime2.setVisible(false);
				lbllblSPTime41.setVisible(false);
				lblSPTime41.setVisible(false);
			}
			// move cap rating a row above to fill in available space for 6595
			if (Configuration.LAST_USED_ISSTD.startsWith("IS 6595:")) {
				pnlTop2.add(lbllblCapRat2, new TableLayoutConstraints(5, 6, 5, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				pnlTop2.add(lblCapRate2, new TableLayoutConstraints(6, 6, 6, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				pnlTop41.add(lbllblCapRat41, new TableLayoutConstraints(5, 6, 5, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				pnlTop41.add(lblCapRate41, new TableLayoutConstraints(6, 6, 6, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				
				pnlTop2.add(lbllblCapVolt2, new TableLayoutConstraints(7, 6, 7, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				pnlTop2.add(lblCapVolt2, new TableLayoutConstraints(8, 6, 8, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				pnlTop41.add(lbllblCapVolt41, new TableLayoutConstraints(7, 6, 7, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				pnlTop41.add(lblCapVolt41, new TableLayoutConstraints(8, 6, 8, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				((TableLayout)pnlTop2.getLayout()).deleteRow(6);
				((TableLayout)pnlTop41.getLayout()).deleteRow(6);
			}
		}
		
		// show/hide mot eff as required
		if (Configuration.LAST_USED_ISSTD.startsWith("IS 8034:") || Configuration.LAST_USED_ISSTD.startsWith("IS 14220:") || Configuration.LAST_USED_ISSTD.startsWith("IS 6595:") || (Configuration.REP_SHOW_PUMP_EFF.equals("1") && Configuration.REP_SHOW_MOT_EFF.equals("1"))) {
			lbllblMotEff2.setVisible(true);
			lblMotEff2.setVisible(true);
			lbllblMotEff41.setVisible(true);
			lblMotEff41.setVisible(true);
		} else {
			lbllblMotEff2.setVisible(false);
			lblMotEff2.setVisible(false);
			lbllblMotEff41.setVisible(false);
			lblMotEff41.setVisible(false);
		}
		
		/* 2. TEST TABLE ACCRODING TO ISI */

		
		if (Configuration.LAST_USED_ISSTD.startsWith("IS 12225")) { // to be completed
			// create more fields required

			// 1. performance report
			TableLayout tblLayout = (TableLayout) pnlPrime2.getLayout();
			
			// column title changes
			lblTitSHd2.setText("<html><body align=\"center\">Ejector Head</body></html>");
			lblTitVHC2.setText("<html><body align=\"center\">DLWL<br>(EH+6+GD)</body></html>");
			lblTitTotHd2.setText("<html><body align=\"center\">Total Head<br>(DH+GD)</body></html>");
			
			// changes in rated values section
			lblTitFlow2.setText("<html><body align=\"center\">DLWL</body></html>");
			lblFlowUnitT2.setText(curHdUnit);
			lblTitMI2.setText("<html><body align=\"center\">Flow</body></html>");
			lblMITitUnit2.setText(curDisUnit);
			
			// add new column
			tblLayout.removeLayoutComponent(lblRem2);
			tblLayout.insertColumn(15, TableLayout.FILL);
			pnlPrime2.add(lblPerfHz2, new TableLayoutConstraints(11, 0, 14, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			
			JLabel lblMI = new JLabel();
			lblMI.setText("<html><body align=\"center\">Input Power</body></html>");
			lblMI.setBackground(Color.white);
			lblMI.setForeground(clrLabel);
			lblMI.setHorizontalAlignment(SwingConstants.CENTER);
			lblMI.setFont(new Font("Arial", Font.BOLD, 11));
			lblMI.setOpaque(true);
			lblMI.setBorder(null);
			pnlPrime2.add(lblMI, new TableLayoutConstraints(14, 1, 14, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			
			JLabel lblMIU = new JLabel();
			lblMIU.setText("kW");
			lblMIU.setBackground(Color.white);
			lblMIU.setForeground(clrLabel);
			lblMIU.setHorizontalAlignment(SwingConstants.CENTER);
			lblMIU.setFont(new Font("Arial", Font.BOLD, 11));
			lblMIU.setOpaque(true);
			lblMIU.setBorder(null);
			pnlPrime2.add(lblMIU, new TableLayoutConstraints(14, 2, 14, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			
			JLabel lblRem =new JLabel();
			lblRem.setText("Remark");
			lblRem.setBackground(Color.white);
			lblRem.setForeground(clrLabel);
			lblRem.setFont(new Font("Arial", Font.BOLD, 11));
			lblRem.setOpaque(true);
			lblRem.setHorizontalAlignment(SwingConstants.CENTER);
			lblRem.setBorder(null);
			pnlPrime2.add(lblRem, new TableLayoutConstraints(15, 0, 15, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			
			pnlPrime2.add(tblRes2, new TableLayoutConstraints(0, 3, 15, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			
			tblRes2.setModel(new DefaultTableModel(
					new Object[][] {
						{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
					},
					new String[] {
						null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
					}
				) {
					Class<?>[] columnTypes = new Class<?>[] {
						String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class
					};
					@Override
					public Class<?> getColumnClass(int columnIndex) {
						return columnTypes[columnIndex];
					}
				});
				{
					TableColumnModel cm = tblRes2.getColumnModel();
					cm.getColumn(0).setMinWidth(34);
					cm.getColumn(0).setMaxWidth(34);
					cm.getColumn(0).setPreferredWidth(34);
				}
				
			// 2. ISI report
			tblLayout = (TableLayout) pnlPrime41.getLayout();
			
			// column title changes
			lblTitSHd41.setText("<html><body align=\"center\">Ejector Head</body></html>");
			lblTitVHC41.setText("<html><body align=\"center\">DLWL<br>(EH+6+GD)</body></html>");
			lblTitTotHd41.setText("<html><body align=\"center\">Total Head<br>(DH+GD)</body></html>");
			
			// changes in rated values section
			lblTitFlow41.setText("<html><body align=\"center\">DLWL</body></html>");
			lblFlowUnitT41.setText(curHdUnit);
			lblTitMI41.setText("<html><body align=\"center\">Flow</body></html>");
			lblMITitUnit41.setText(curDisUnit);
			
			// add new column
			tblLayout.removeLayoutComponent(lblRem41);
			tblLayout.insertColumn(15, TableLayout.FILL);
			pnlPrime41.add(lblPerfHz41, new TableLayoutConstraints(11, 0, 14, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			
			pnlPrime41.add(lblMI, new TableLayoutConstraints(14, 1, 14, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			
			pnlPrime41.add(lblMIU, new TableLayoutConstraints(14, 2, 14, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			
			pnlPrime41.add(lblRem, new TableLayoutConstraints(15, 0, 15, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			
			pnlPrime41.add(tblRes41, new TableLayoutConstraints(0, 3, 15, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			
			tblRes41.setModel(new DefaultTableModel(
					new Object[][] {
						{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
					},
					new String[] {
						null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
					}
				) {
					Class<?>[] columnTypes = new Class<?>[] {
						String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class
					};
					@Override
					public Class<?> getColumnClass(int columnIndex) {
						return columnTypes[columnIndex];
					}
				});
				{
					TableColumnModel cm = tblRes2.getColumnModel();
					cm.getColumn(0).setMinWidth(34);
					cm.getColumn(0).setMaxWidth(34);
					cm.getColumn(0).setPreferredWidth(34);
				}
				
			// 2.2 ISI report (graph)
			lblFlowUnit41.setText("DLWL (" + curHdUnit + ")");
			lbllblMaxC41.setText("<html><body align=\"center\">I-Max in DLWL Range (A)</body></html>");
			lblFlowUnit41RL.setText("DLWL (" + curHdUnit + ")");
			lblFlowUnit41RH.setText("DLWL (" + curHdUnit + ")");
			
			// 3. graph
			lblFlowUnit3.setText("DLWL (" + curHdUnit + ")");
			lbllblMaxC3.setText("<html><body align=\"center\">I-Max in DLWL Range (A)</body></html>");
			lblFlowUnit3RL.setText("DLWL (" + curHdUnit + ")");
			lblFlowUnit3RH.setText("DLWL (" + curHdUnit + ")");
				
		} else {	
			// create more fields required

			// 1. performance report
			TableLayout tblLayout = (TableLayout) pnlPrime2.getLayout();
			tblLayout.removeLayoutComponent(lblRem2);
			tblLayout.insertColumn(15, TableLayout.FILL);
			tblLayout.insertColumn(16, TableLayout.FILL);
			if (Configuration.REP_SHOW_PUMP_EFF.equals("1")) {
				tblLayout.insertColumn(17, TableLayout.FILL);
				pnlPrime2.add(lblPerfHz2, new TableLayoutConstraints(11, 0, 16, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			} else {
				pnlPrime2.add(lblPerfHz2, new TableLayoutConstraints(11, 0, 15, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			
			JLabel lblPumpOp = new JLabel();
			lblPumpOp.setText("<html><body align=\"center\">Pump<br>Output</body></html>");
			lblPumpOp.setBackground(Color.white);
			lblPumpOp.setForeground(clrLabel);
			lblPumpOp.setHorizontalAlignment(SwingConstants.CENTER);
			lblPumpOp.setFont(new Font("Arial", Font.BOLD, 11));
			lblPumpOp.setOpaque(true);
			lblPumpOp.setBorder(null);
			pnlPrime2.add(lblPumpOp, new TableLayoutConstraints(14, 1, 14, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			
			JLabel lblPumpOpU = new JLabel();
			lblPumpOpU.setText("kW");
			lblPumpOpU.setBackground(Color.white);
			lblPumpOpU.setForeground(clrLabel);
			lblPumpOpU.setHorizontalAlignment(SwingConstants.CENTER);
			lblPumpOpU.setFont(new Font("Arial", Font.BOLD, 11));
			lblPumpOpU.setOpaque(true);
			lblPumpOpU.setBorder(null);
			pnlPrime2.add(lblPumpOpU, new TableLayoutConstraints(14, 2, 14, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			
			JLabel lblOE = new JLabel();
			lblOE.setText("<html><body align=\"center\">Overall<br>Eff.</body></html>");
			lblOE.setBackground(Color.white);
			lblOE.setForeground(clrLabel);
			lblOE.setHorizontalAlignment(SwingConstants.CENTER);
			lblOE.setFont(new Font("Arial", Font.BOLD, 11));
			lblOE.setOpaque(true);
			lblOE.setBorder(null);
			pnlPrime2.add(lblOE, new TableLayoutConstraints(15, 1, 15, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			
			JLabel lblOEU = new JLabel();
			lblOEU.setText("%");
			lblOEU.setBackground(Color.white);
			lblOEU.setForeground(clrLabel);
			lblOEU.setHorizontalAlignment(SwingConstants.CENTER);
			lblOEU.setFont(new Font("Arial", Font.BOLD, 11));
			lblOEU.setOpaque(true);
			lblOEU.setBorder(null);
			pnlPrime2.add(lblOEU, new TableLayoutConstraints(15, 2, 15, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			
			int rc = 16;
			if (Configuration.REP_SHOW_PUMP_EFF.equals("1")) {
				JLabel lblPE = new JLabel();
				lblPE.setText("<html><body align=\"center\">Pump<br>Eff.</body></html>");
				lblPE.setBackground(Color.white);
				lblPE.setForeground(clrLabel);
				lblPE.setHorizontalAlignment(SwingConstants.CENTER);
				lblPE.setFont(new Font("Arial", Font.BOLD, 11));
				lblPE.setOpaque(true);
				lblPE.setBorder(null);
				pnlPrime2.add(lblPE, new TableLayoutConstraints(rc, 1, rc, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				
				JLabel lblPEU = new JLabel();
				lblPEU.setText("%");
				lblPEU.setBackground(Color.white);
				lblPEU.setForeground(clrLabel);
				lblPEU.setHorizontalAlignment(SwingConstants.CENTER);
				lblPEU.setFont(new Font("Arial", Font.BOLD, 11));
				lblPEU.setOpaque(true);
				lblPEU.setBorder(null);
				pnlPrime2.add(lblPEU, new TableLayoutConstraints(rc, 2, rc, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				++rc;
			}
			JLabel lblRem =new JLabel();
			lblRem.setText("Remark");
			lblRem.setBackground(Color.white);
			lblRem.setForeground(clrLabel);
			lblRem.setFont(new Font("Arial", Font.BOLD, 11));
			lblRem.setOpaque(true);
			lblRem.setHorizontalAlignment(SwingConstants.CENTER);
			lblRem.setBorder(null);
			pnlPrime2.add(lblRem, new TableLayoutConstraints(rc, 0, rc, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			
			pnlPrime2.add(tblRes2, new TableLayoutConstraints(0, 3, rc, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			if (Configuration.REP_SHOW_PUMP_EFF.equals("1")) {
				tblRes2.setModel(new DefaultTableModel(
						new Object[][] {
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
						},
						new String[] {
							null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
						}
					) {
						Class<?>[] columnTypes = new Class<?>[] {
							String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class
						};
						@Override
						public Class<?> getColumnClass(int columnIndex) {
							return columnTypes[columnIndex];
						}
					});
					{
						TableColumnModel cm = tblRes2.getColumnModel();
						cm.getColumn(0).setMinWidth(34);
						cm.getColumn(0).setMaxWidth(34);
						cm.getColumn(0).setPreferredWidth(34);
					}
			} else {
				tblRes2.setModel(new DefaultTableModel(
						new Object[][] {
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
						},
						new String[] {
							null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
						}
					) {
						Class<?>[] columnTypes = new Class<?>[] {
							String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class
						};
						@Override
						public Class<?> getColumnClass(int columnIndex) {
							return columnTypes[columnIndex];
						}
					});
					{
						TableColumnModel cm = tblRes2.getColumnModel();
						cm.getColumn(0).setMinWidth(34);
						cm.getColumn(0).setMaxWidth(34);
						cm.getColumn(0).setPreferredWidth(34);
					}
			}
				
				
			// 2.1 ISI report (Report)
			tblLayout = (TableLayout) pnlPrime41.getLayout();
			tblLayout.removeLayoutComponent(lblRem41);
			tblLayout.insertColumn(15, TableLayout.FILL);
			tblLayout.insertColumn(16, TableLayout.FILL);
			if (Configuration.REP_SHOW_PUMP_EFF.equals("1")) {
				tblLayout.insertColumn(17, TableLayout.FILL);
				pnlPrime41.add(lblPerfHz41, new TableLayoutConstraints(11, 0, 16, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			} else {
				pnlPrime41.add(lblPerfHz41, new TableLayoutConstraints(11, 0, 15, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			
			JLabel lblPumpOp41 = new JLabel();
			lblPumpOp41.setText("<html><body align=\"center\">Pump<br>Output</body></html>");
			lblPumpOp41.setBackground(Color.white);
			lblPumpOp41.setForeground(clrLabel);
			lblPumpOp41.setHorizontalAlignment(SwingConstants.CENTER);
			lblPumpOp41.setFont(new Font("Arial", Font.BOLD, 11));
			lblPumpOp41.setOpaque(true);
			lblPumpOp41.setBorder(null);
			pnlPrime41.add(lblPumpOp41, new TableLayoutConstraints(14, 1, 14, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			
			JLabel lblPumpOpU41 = new JLabel();
			lblPumpOpU41.setText("kW");
			lblPumpOpU41.setBackground(Color.white);
			lblPumpOpU41.setForeground(clrLabel);
			lblPumpOpU41.setHorizontalAlignment(SwingConstants.CENTER);
			lblPumpOpU41.setFont(new Font("Arial", Font.BOLD, 11));
			lblPumpOpU41.setOpaque(true);
			lblPumpOpU41.setBorder(null);
			pnlPrime41.add(lblPumpOpU41, new TableLayoutConstraints(14, 2, 14, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			
			JLabel lblOE41 = new JLabel();
			lblOE41.setText("<html><body align=\"center\">Overall<br>Eff.</body></html>");
			lblOE41.setBackground(Color.white);
			lblOE41.setForeground(clrLabel);
			lblOE41.setHorizontalAlignment(SwingConstants.CENTER);
			lblOE41.setFont(new Font("Arial", Font.BOLD, 11));
			lblOE41.setOpaque(true);
			lblOE41.setBorder(null);
			pnlPrime41.add(lblOE41, new TableLayoutConstraints(15, 1, 15, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			
			JLabel lblOEU41 = new JLabel();
			lblOEU41.setText("%");
			lblOEU41.setBackground(Color.white);
			lblOEU41.setForeground(clrLabel);
			lblOEU41.setHorizontalAlignment(SwingConstants.CENTER);
			lblOEU41.setFont(new Font("Arial", Font.BOLD, 11));
			lblOEU41.setOpaque(true);
			lblOEU41.setBorder(null);
			pnlPrime41.add(lblOEU41, new TableLayoutConstraints(15, 2, 15, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			
			rc = 16;
			if (Configuration.REP_SHOW_PUMP_EFF.equals("1")) {
				JLabel lblPE41 = new JLabel();
				lblPE41.setText("<html><body align=\"center\">Pump<br>Eff.</body></html>");
				lblPE41.setBackground(Color.white);
				lblPE41.setForeground(clrLabel);
				lblPE41.setHorizontalAlignment(SwingConstants.CENTER);
				lblPE41.setFont(new Font("Arial", Font.BOLD, 11));
				lblPE41.setOpaque(true);
				lblPE41.setBorder(null);
				pnlPrime41.add(lblPE41, new TableLayoutConstraints(rc, 1, rc, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				
				JLabel lblPEU41 = new JLabel();
				lblPEU41.setText("%");
				lblPEU41.setBackground(Color.white);
				lblPEU41.setForeground(clrLabel);
				lblPEU41.setHorizontalAlignment(SwingConstants.CENTER);
				lblPEU41.setFont(new Font("Arial", Font.BOLD, 11));
				lblPEU41.setOpaque(true);
				lblPEU41.setBorder(null);
				pnlPrime41.add(lblPEU41, new TableLayoutConstraints(rc, 2, rc, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				++rc;
			}
			JLabel lblRem41 =new JLabel();
			lblRem41.setText("Remark");
			lblRem41.setBackground(Color.white);
			lblRem41.setForeground(clrLabel);
			lblRem41.setFont(new Font("Arial", Font.BOLD, 11));
			lblRem41.setOpaque(true);
			lblRem41.setHorizontalAlignment(SwingConstants.CENTER);
			lblRem41.setBorder(null);
			pnlPrime41.add(lblRem41, new TableLayoutConstraints(rc, 0, rc, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			
			pnlPrime41.add(tblRes41, new TableLayoutConstraints(0, 3, rc, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			if (Configuration.REP_SHOW_PUMP_EFF.equals("1")) {
				tblRes41.setModel(new DefaultTableModel(
						new Object[][] {
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
						},
						new String[] {
							null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
						}
					) {
						Class<?>[] columnTypes = new Class<?>[] {
							String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class
						};
						@Override
						public Class<?> getColumnClass(int columnIndex) {
							return columnTypes[columnIndex];
						}
					});
					{
						TableColumnModel cm = tblRes41.getColumnModel();
						cm.getColumn(0).setMinWidth(35);
						cm.getColumn(0).setMaxWidth(35);
						cm.getColumn(0).setPreferredWidth(35);
					}
			} else {
				tblRes41.setModel(new DefaultTableModel(
						new Object[][] {
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
						},
						new String[] {
							null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
						}
					) {
						Class<?>[] columnTypes = new Class<?>[] {
							String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class
						};
						@Override
						public Class<?> getColumnClass(int columnIndex) {
							return columnTypes[columnIndex];
						}
					});
					{
						TableColumnModel cm = tblRes41.getColumnModel();
						cm.getColumn(0).setMinWidth(35);
						cm.getColumn(0).setMaxWidth(35);
						cm.getColumn(0).setPreferredWidth(35);
					}
			}
		}
				
		if (!Configuration.LAST_USED_ISSTD.startsWith("IS 8472:") || Configuration.REP_SHOW_MI_FOR_8472.equals("0")) {
			// Remove motor input from reports as its only applicable for 8472
			
			// 2.2 ISI report (graph)
			((TableLayout)pnlBotRes41.getLayout()).deleteColumn(2);
			((TableLayout)pnlBotRes41R.getLayout()).deleteColumn(3);
			((TableLayout)pnlBotRes41R.getLayout()).deleteColumn(6);
			
			// 3. graph
			((TableLayout)pnlBotRes3.getLayout()).deleteColumn(2);
			((TableLayout)pnlBotRes3R.getLayout()).deleteColumn(3);
			((TableLayout)pnlBotRes3R.getLayout()).deleteColumn(6);
			
			// 4. compare graph
			// ((TableLayout)pnlFactor.getLayout()).removeLayoutComponent(chkQvMI);
			
			// 5. ISI - type test
			((TableLayout)pnlPrime42.getLayout()).deleteColumn(4);
			tblRes42.setModel(new DefaultTableModel(
					new Object[][] {
						{null, null, null, null, null, null, null},
					},
					new String[] {
						null, null, null, null, null, null, null
					}
				) {
					Class<?>[] columnTypes = new Class<?>[] {
						String.class, String.class, Object.class, String.class, String.class, String.class, String.class
					};
					@Override
					public Class<?> getColumnClass(int columnIndex) {
						return columnTypes[columnIndex];
					}
				});
				{
					TableColumnModel cm = tblRes42.getColumnModel();
					cm.getColumn(0).setMinWidth(75);
					cm.getColumn(1).setMinWidth(150);
					cm.getColumn(1).setMaxWidth(150);
					cm.getColumn(1).setPreferredWidth(150);
				}
				
			// 6. ISI - max/min test
				//((TableLayout)pnlPrime43.getLayout()).removeLayoutComponent(lbllblMI43);
				((TableLayout)pnlPrime43.getLayout()).deleteColumn(10);
				((TableLayout)pnlPrime43.getLayout()).deleteColumn(10);	
				((TableLayout)pnlPrime43.getLayout()).deleteColumn(10);	
				((TableLayout)pnlPrime43.getLayout()).deleteColumn(10);	
				((TableLayout)pnlPrime43.getLayout()).deleteColumn(10);
				tblRes43.setModel(new DefaultTableModel(
						new Object[][] {
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
						},
						new String[] {
							null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
						}
					) {
						Class<?>[] columnTypes = new Class<?>[] {
							Object.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class
						};
						@Override
						public Class<?> getColumnClass(int columnIndex) {
							return columnTypes[columnIndex];
						}
					});
					{
						TableColumnModel cm = tblRes43.getColumnModel();
						cm.getColumn(0).setMinWidth(110);
						cm.getColumn(0).setMaxWidth(110);
						cm.getColumn(0).setPreferredWidth(110);
						cm.getColumn(3).setMinWidth(65);
						cm.getColumn(3).setMaxWidth(65);
						cm.getColumn(3).setPreferredWidth(65);
						cm.getColumn(5).setMinWidth(65);
						cm.getColumn(5).setMaxWidth(65);
						cm.getColumn(5).setPreferredWidth(65);
						cm.getColumn(8).setMinWidth(65);
						cm.getColumn(8).setMaxWidth(65);
						cm.getColumn(8).setPreferredWidth(65);
						cm.getColumn(10).setMinWidth(65);
						cm.getColumn(10).setMaxWidth(65);
						cm.getColumn(10).setPreferredWidth(65);
						cm.getColumn(13).setMinWidth(65);
						cm.getColumn(13).setMaxWidth(65);
						cm.getColumn(13).setPreferredWidth(65);
						cm.getColumn(15).setMinWidth(65);
						cm.getColumn(15).setMaxWidth(65);
						cm.getColumn(15).setPreferredWidth(65);
						cm.getColumn(18).setMinWidth(65);
						cm.getColumn(18).setMaxWidth(65);
						cm.getColumn(18).setPreferredWidth(65);
						cm.getColumn(20).setMinWidth(65);
						cm.getColumn(20).setMaxWidth(65);
						cm.getColumn(20).setPreferredWidth(65);
					}
		}
		
		if(Configuration.LAST_USED_ISSTD.startsWith("IS 8034:") || Configuration.LAST_USED_ISSTD.startsWith("IS 14220:")) {
			// vacuum (shead) not required
			//1. test register
			((TableLayout)pnlPrime.getLayout()).deleteColumn(7);
			tblRes.removeColumn(tblRes.getColumnModel().getColumn(8));
			
			//2. perf report
			((TableLayout)pnlPrime2.getLayout()).deleteColumn(2);
			pnlPrime2.add(lbllblHead1, new TableLayoutConstraints(3, 0, 5, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			tblRes2.removeColumn(tblRes2.getColumnModel().getColumn(3));
			lbllblPSize2.setText("DELIVERY SIZE (mm)");
			lbllblPSize3.setText("DELIVERY SIZE (mm)"); // graph
			
			//3.1 ISI report - test report
			((TableLayout)pnlPrime41.getLayout()).deleteColumn(2);
			pnlPrime41.add(label56, new TableLayoutConstraints(3, 0, 5, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			tblRes41.removeColumn(tblRes41.getColumnModel().getColumn(3));
			lbllblPSize41.setText("DELIVERY SIZE (mm)");
			lbllblPSize41_2.setText("DELIVERY SIZE (mm)");
			
			// custom report
			//((TableLayout)pnlPrime6.getLayout()).deleteColumn(2);
			//tblRes6.removeColumn(tblRes6.getColumnModel().getColumn(2));
		} 
	}
	
	private void refreshTestList() {
		if (initInProgress) {
			return;
		}
		setFilterText();
		try {
			DefaultTableModel defModel = (DefaultTableModel) tblTest.getModel();
			while (tblTest.getRowCount() > 0) {
				defModel.removeRow(0);
			}
			
			pumpList.clear();
			String tmpV = "";
			MyResultSet res = db.executeQuery("select pump_slno, rated_volt from " + Configuration.READING_DETAIL + " " + filterText + " group by pump_slno, rated_volt order by pump_slno");
			int r = 0;
			while (res.next()) {
				defModel.addRow(new Object[]{null});
				tblTest.setValueAt(true, r, 0);
				if (curMVPump) {
					tmpV = " [" + res.getString("rated_volt") + "V]";
				}
				tblTest.setValueAt(res.getString("pump_slno") + tmpV, r, 1);
				pumpList.put(r++, res.getString("pump_slno") + tmpV);
			}
			lblResult.setText(r + " Test(s) Found");
		} catch (SQLException se) {
			JOptionPane.showMessageDialog(this, "Error refreshing the test list:" + se.getMessage());
		}
	}
	
	private void setFilterText() {
		filterText = "";
		filterTextNoType = "";
		String typeFilter = "";
		String testFilter = "";
		String dateFilter = "";
		String snoFilter = "";
		String lineFilter = "";
		String remFilter = "";
		String rtVFilter = "";
		
		String filterTitle = "Filter [None]";
		if ( cmbType.getSelectedIndex() >= 0) {
			typeFilter = "pump_type_id='" + pumpTypeIdList.get(cmbType.getSelectedItem().toString()) + "'";
		}
		
		if (!optBoth.isSelected()) {
			if (optRt.isSelected()) {
				testFilter = "test_type='R'";
			} else {
				testFilter = "test_type='T'";
			}
		}
		
		if (chkRtV.isSelected() && curMVPump) {
			rtVFilter = " rated_volt = '" + curRtV +"'";
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
		
		if (!typeFilter.isEmpty() || !testFilter.isEmpty() || !dateFilter.isEmpty() || !snoFilter.isEmpty() || !lineFilter.isEmpty() || !remFilter.isEmpty() || !rtVFilter.isEmpty()) {
			if (!typeFilter.isEmpty()) {
				filterText = typeFilter;
			}
			
			if (!testFilter.isEmpty()) {
				if (filterText.isEmpty()) {
					filterText = testFilter;
				} else {
					filterText += " and " + testFilter;
				}
			}
			
			if (!rtVFilter.isEmpty()) {
				if (filterText.isEmpty()) {
					filterText = rtVFilter;
				} else {
					filterText += " and " + rtVFilter;
				}
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
			pumpFilterText = filterText;
			filterTextNoType = filterText.substring(filterText.indexOf(typeFilter)+typeFilter.length());
			//filterTitle = "Advanced Filter [" + filterText + "]";
		}
		
		/*pnlAdFilter.setBorder(new TitledBorder(null, filterTitle, TitledBorder.LEADING, TitledBorder.TOP,
				new Font("Arial", Font.BOLD, 12), Color.blue));
		pnlAdFilter.setToolTipText(filterTitle);*/
	}
	private void gotoPageRegRep(int page) {
		// clear the table
		DefaultTableModel defModel = (DefaultTableModel) tblRes.getModel();
		while (tblRes.getRowCount() > 0) {
			defModel.removeRow(0);
		}
		
		long toRow = (long) (page * totRowsPerPage);
		long fromRow = (long) (toRow - (totRowsPerPage) + 1);
		
		try {
			MyResultSet res = db.executeQuery("select * from TEMP_REPORT where rowid between " + fromRow + " and " + toRow);
			
			// add primary tests
			String usr = "";
			if (res.isRowsAvail()) {
				int r=0;
				int c=0;
				int tsno = 1;
				String prevSNo = "";
				while (res.next()) {
					c = 0;
					defModel.addRow( new Object[] {"","","","","","","","","","","","","","",""});
					if (!prevSNo.equals(res.getString("pump_slno"))) {
						tblRes.setValueAt(reqDtFormat.format(dbDtFormat.parse(res.getString("test_date"))), r, c++);
						tblRes.setValueAt(res.getString("pump_slno"), r, c++);
						tblRes.setValueAt(res.getString("mot_slno"), r, c++);
						prevSNo = res.getString("pump_slno");
						tsno = 1;
					} else {
						c+=3;
					}
					tblRes.setValueAt(res.getString("rated_volt"), r, c++);
					tblRes.setValueAt(tsno++, r, c++);
					tblRes.setValueAt(res.getString("test_type"), r, c++);
					tblRes.setValueAt(res.getString("test_name_code"), r, c++);
					
					tblRes.setValueAt(res.getString("speed"), r, c++);
					// ISI handling
					if (!(Configuration.LAST_USED_ISSTD.startsWith("IS 8034:") || Configuration.LAST_USED_ISSTD.startsWith("IS 14220:"))) {
						tblRes.setValueAt(deriveHead(res.getDouble("shead")), r, c++);
					}
					tblRes.setValueAt(deriveHead(res.getDouble("dhead")), r, c++);
					tblRes.setValueAt(deriveFlow(res.getDouble("flow")), r, c++);
					
					tblRes.setValueAt(dotOne.format(res.getFloat("volt")), r, c++);
					tblRes.setValueAt(dotTwo.format(res.getFloat("current")), r, c++);
					tblRes.setValueAt(dotThree.format(res.getFloat("power")), r, c++);
					tblRes.setValueAt(dotTwo.format(res.getFloat("freq")), r, c++);
					
					tblRes.setValueAt(res.getString("remarks"), r, c++);
					usr = res.getString("user"); // overwrite with latest user
					++r;
				}
				if (Configuration.REP_SHOW_TESTER_NAME.equals("1")) {
					lblTestedBy.setText("Tested By [" + usr.toUpperCase() + "]"); 
				}
			}
		} catch (Exception sqle) {
			JOptionPane.showMessageDialog(this, "Failed to load current page\nError:" + sqle.getMessage());
			return;
		}
		
		if (totAvailPagesRegRep > 0) {
			lblPage.setText("Page " + page + " of " + totAvailPagesRegRep);
		} else {
			lblPage.setText("No records found");
		}
		
		enableDisableButtons();
		setPanelTitle("", "");
		return;
	}
	
	// function to convert the flow based on user choice
	private String deriveFlow(Double flw) {
		String dFlow = "";
		if (!defDisUnit.equals(curDisUnit)) { // do conversion as user's choice
			// 1.unit
			if(defDisUnit.startsWith("l")) { // l to g or l to m3
				if(curDisUnit.startsWith("g")) {
					flw = flw * 0.26417F;
				} else if(curDisUnit.contains("sup")) {
					flw = flw * 0.001F;
				}
			}
			if(defDisUnit.startsWith("g")) { // g to l or g to m3
				if(curDisUnit.startsWith("l")) {
					flw = flw / 0.26417F;
				} else if(curDisUnit.contains("sup")) {
					flw = flw / 0.26417F * 0.001F;
				}
			}
			if(defDisUnit.contains("sup")) { // m3 to l or m3 to g
				if(curDisUnit.startsWith("l")) {
					flw = flw / 0.001F;
				} else if(curDisUnit.startsWith("g")) {
					flw = flw / 0.001F * 0.26417F;
				}
			}
			
			// 2.time
			if(defDisUnit.endsWith("s")) {  // s to m or s to h
				if(curDisUnit.endsWith("m")) {
					flw = flw * 60;
				} else if (curDisUnit.endsWith("h")) {
					flw = flw * 3600;
				}
			}
			if(defDisUnit.endsWith("m")) {  // m to s or m to h
				if(curDisUnit.endsWith("s")) {
					flw = flw / 60;
				} else if (curDisUnit.endsWith("h")) {
					flw = flw * 60;
				}
			}
			if(defDisUnit.endsWith("h")) {  // h to s or h to m
				if(curDisUnit.endsWith("s")) {
					flw = flw / 3600;
				} else if (curDisUnit.endsWith("m")) {
					flw = flw / 60;
				}
			}
		}
		
		// format
		if(curDisUnit.endsWith("s")) {
			dFlow = dotTwo.format(flw);
		} else if(curDisUnit.endsWith("m")) {
			dFlow = dotOne.format(flw);
		} else if(curDisUnit.contains("sup")) { // mcph
			dFlow = dotTwo.format(flw);
		} else if(curDisUnit.endsWith("h")) {
			dFlow = dotZero.format(flw);
		}
		
		return dFlow;
	}
	
	// function to convert the head based on user choice
	private String deriveHead(Double hd) {
		return deriveHead(hd, false, false);
	}
	private String deriveHead(Double hd, boolean formatOnly) {
		return deriveHead(hd, formatOnly, false);
	}
	private String deriveHead(Double hd, boolean formatOnly, boolean pipeConst) {
		String dHd = "";
		if (!formatOnly && !defHdUnit.equals(curHdUnit)) { // do conversion as user's choice
			// 1.unit
			if(defHdUnit.equals("m")) { // m to ft or m to bar
				if(curHdUnit.equals("ft")) {
					hd = hd * 3.28084F;
				} else {
					hd = hd * 0.0980638F;
				}
			}
			if(defHdUnit.equals("ft")) { // ft to mt or ft to bar
				if(curHdUnit.equals("m")) {
					hd = hd / 3.28084F;
				} else {
					hd = (hd / 3.28084F) * 0.0980638F;
				}
			}
			if(defHdUnit.equals("bar")) { // bar to m or bar to ft
				if(curHdUnit.equals("m")) {
					hd = hd / 0.0980638F;
				} else {
					hd = (hd / 0.0980638F) * 3.28084F;
				}
			}
		}
		
		// format
		if (pipeConst) {
			dHd = dotFour.format(hd);
		} else {
			/*if(curHdUnit.equals("m")) { 
				dHd = dotTwo.format(hd);
			} else if(curHdUnit.equals("bar")) {
				dHd = dotTwo.format(hd);
			} else { // feet
				dHd = dotZero.format(hd);
			}*/
			dHd = dotTwo.format(hd);
		}
		return dHd;
	}
	
	private void gotoPageISIType(int page) {
		// clear the table
		DefaultTableModel defModel = (DefaultTableModel) tblRes42.getModel();
		while (tblRes42.getRowCount() > 0) {
			defModel.removeRow(0);
		}
		
		long toRow = (long) (page * totRowsPerPageISIType);
		long fromRow = (long) (toRow - (totRowsPerPageISIType) + 1);
		
		try {
			MyResultSet res = db.executeQuery("select * from TEMP_REPORT_ISI_TYPE where rowid between " + fromRow + " and " + toRow);

			String pumpNo = "";
			String tmpRtVolt = "";
			if (res.isRowsAvail()) {
				int r=0;
				int ra=0;
				Double actVal[] = {0.0,0.0,0.0,0.0,0.0};
				while (res.next()) {
					defModel.addRow( new Object[] {"","","","","","",""});
					tblRes42.setValueAt(reqDtFormat.format(dbDtFormat.parse(res.getString("test_date"))), r, 0);
					if (curMVPump) {
						tblRes42.setValueAt(res.getString("pump_slno") + " [" + res.getString("rated_volt") +"]", r, 1);
					} else {
						tblRes42.setValueAt(res.getString("pump_slno"), r, 1);
					}
					tblRes42.setValueAt("Withstood", r, 2);
					pumpNo = res.getString("pump_slno");
					tmpRtVolt = res.getString("rated_volt");
					
					// calculate actual and show it
					actVal = findActualValues(pumpNo, curPumpTypeId, tmpRtVolt);
					
					if (curDisUnit.endsWith("s") || curDisUnit.contains("sup")) {
						tblRes42.setValueAt(dotTwo.format(actVal[0]), r, 3);
					} else if (curDisUnit.endsWith("m")) {
						tblRes42.setValueAt(dotOne.format(actVal[0]), r, 3);
					} else {
						tblRes42.setValueAt(dotZero.format(actVal[0]), r, 3);
					}
					// TODO: OE in type test report
					tblRes42.setValueAt(dotTwo.format(actVal[1]), r, 4);
					ra = 5;
					if(Configuration.LAST_USED_ISSTD.startsWith("IS 8472:") && Configuration.REP_SHOW_MI_FOR_8472.equals("1")) { // ISI handling
						tblRes42.setValueAt(dotThree.format(actVal[2]), r, ra++);
					}
					tblRes42.setValueAt(dotTwo.format(actVal[3]), r, ra++);
					tblRes42.setValueAt(dotThree.format(actVal[4]), r, ra);
					
					++r;
				}
				
				// footer
				if (Configuration.REP_SHOW_TESTER_NAME.equals("1")) {
					MyResultSet res3 = db.executeQuery("select user from " + Configuration.READING_DETAIL + " where pump_type_id='" + curPumpTypeId + "' and pump_slno = '" + pumpNo + "' and rated_volt='" + tmpRtVolt +"'");
					if (!pumpNo.isEmpty() && res3.next()) {
						lblTestedBy42.setText("Tested By [" + res3.getString("user").toUpperCase() + "]");
					}
				}
				
			}
		} catch (Exception sqle) {
			JOptionPane.showMessageDialog(this, "Failed to load current page\nError:" + sqle.getMessage());
			sqle.printStackTrace();
			return;
		}
		
		if (totAvailPagesISIType > 0) {
			lblPage42.setText("Page " + page + " of " + totAvailPagesISIType);
		} else {
			lblPage42.setText("No records found");
		}
		
		enableDisableButtons();
		setPanelTitle("", "");
		return;
	}
	
	private void enableDisableButtons()
	{
		long cPage = 0;
		long tPage = 0;
		
		if(tabReport.getSelectedIndex() == 0) { // register rep
			cPage = curPageRegRep;
			tPage = totAvailPagesRegRep;
		} else if(tabReport.getSelectedIndex() == 3 && tabISI.getSelectedIndex() == 1) { // isi - type test
				cPage = curPageISIType;
				tPage = totAvailPagesISIType;
		} else if(tabReport.getSelectedIndex() == 3 && tabISI.getSelectedIndex() == 2) { // isi - max/min test
			cPage = curPageISIMaxMin;
			tPage = totAvailPagesISIMaxMin;
		} else if (tabReport.getSelectedIndex() == 4) { // graph
			cPage = curPageCompare;
			tPage = totAvailPagesCompare;
		} else {
			cPage = curPage;
			tPage = totAvailPages;
		}
		
		if (cPage == tPage || tPage == 0) {
			cmdNext.setEnabled(false);
			cmdLast.setEnabled(false);
		}
		else if (!cmdNext.isEnabled()) {
			cmdNext.setEnabled(true);
			cmdLast.setEnabled(true);
		}
		
		if (cPage == 1) {
			cmdPrev.setEnabled(false);
			cmdFirst.setEnabled(false);
		}
		else if (!cmdPrev.isEnabled()){
			cmdPrev.setEnabled(true);
			cmdFirst.setEnabled(true);
		}
	}
	
	private void refreshCount() {
		// set page count based on report selection
		try {
			if (tabReport.getSelectedIndex() == 0) {
				// register report
				totAvailPagesRegRep = 0;
				MyResultSet res = db.executeQuery("select count(*) as tot from " + Configuration.READING_DETAIL + " " + pumpFilterText);
				if (res.next()) {
					int result = res.getInt("tot");
					totAvailPagesRegRep =  (int) Math.ceil(result/totRowsPerPage);
				}
			} else if(tabReport.getSelectedIndex() == 3 && tabISI.getSelectedIndex() == 1) { // isi - type test
				totAvailPagesISIType = 0;
				MyResultSet res = db.executeQuery("select count(distinct(pump_slno)) as tot from " + Configuration.READING_DETAIL + " " + pumpFilterText + " and test_type='T'");
				if (res.next()) {
					int result = res.getInt("tot");
					totAvailPagesISIType =  (int) Math.ceil(result/totRowsPerPageISIType);
				}
			} else if(tabReport.getSelectedIndex() == 3 && tabISI.getSelectedIndex() == 2) { // isi - max/min test
				totAvailPagesISIMaxMin = 0;
				String pTypes = " where non_isi_model = 'N'";
				
				if (optSelPump.isSelected()) {
					pTypes = " where type = '" + cmbType.getSelectedItem().toString() + "'";
				}
				
				MyResultSet res = db.executeQuery("select count(type) as tot from " + Configuration.PUMPTYPE + pTypes);
				if (res.next()) {
					int result = res.getInt("tot");
					totAvailPagesISIMaxMin =  (int) Math.ceil(result/totRowsPerPageISIMaxMin);
				}
			} else if (tabReport.getSelectedIndex() == 4) { // compare graph
				if (pumpList.isEmpty()) {
					totAvailPagesCompare = 0;
				} else {
					totAvailPagesCompare = 1;
				}
			} else { // others
				if (pumpList.isEmpty()) {
					totAvailPages = 0;
				} else {
					totAvailPages = pumpList.size();
				}
			}
		} catch (Exception se) {
			JOptionPane.showMessageDialog(this, "Error setting up page numbers:" + se.getMessage());
		}
		
	}
	
	private void refreshReport() {
		refreshCount();
		switch(tabReport.getSelectedIndex()) {
		case 0: // simple register report
			loadRegisterReport();
			break;
		case 1: // test result
			loadTestResReport(curPage);
			break;
		case 2: // test graph
			loadTestGraph(curPage);
			break;
		case 3: // ISI reports
			tabISI.requestFocusInWindow();
			switch (tabISI.getSelectedIndex()) {
			case 0: // test report
				loadISIReport(curPage);
				break;
			case 1: // type test report
				loadISITypeTest();
				break;
			case 2: // max/min
				loadISIMaxMin();
				break;
			}
			break;
		case 4: // compare graphs
			loadCompareGraph();
			break;
		case 5: // custom report
			loadCustomReport(curPage);
			break;
		}
		// export option is currently only available for test register
		cmdExp.setEnabled(tabReport.getSelectedIndex() == 0);
	}
	
	// function to reset report colors
	public void resetRepColors() {
		// load colors
		clrOutline = new Color(Integer.valueOf(Configuration.REP_COLOR_OUTLINE));
		clrHeader = new Color(Integer.valueOf(Configuration.REP_COLOR_HEADER));
		clrLabel = new Color(Integer.valueOf(Configuration.REP_COLOR_LABEL));
		clrDeclared = new Color(Integer.valueOf(Configuration.REP_COLOR_DECLARED));
		clrActual = new Color(Integer.valueOf(Configuration.REP_COLOR_ACTUAL));
		
		// reset colors of all reports
		resetCompColors(pnlPrint);
		pnlPrint.revalidate();
		resetCompColors(pnlPrint2);
		pnlPrint2.revalidate();
		resetCompColors(pnlPrint3);
		pnlPrint3.revalidate();
		resetCompColors(pnlPrint41);
		pnlPrint41.revalidate();
		resetCompColors(pnlPrint42);
		pnlPrint42.revalidate();
		resetCompColors(pnlPrint43);
		pnlPrint43.revalidate();
		resetCompColors(pnlPrint5);
		pnlPrint5.revalidate();
		resetCompColors(pnlPrint6);
		pnlPrint6.revalidate();
	}
	private void resetCompColors(Container comp) {
		for (Component c: comp.getComponents()) {
			if (c.getName() != null) {
	 			// outline
				try {
				if (c.getName().equals("outline")) {
					if (c instanceof JPanel) {
						if (((JPanel)c).getBorder() != null) {
							((JPanel)c).setBorder(BorderFactory.createLineBorder(clrOutline, 1));
						}
						if (c.getBackground().equals(Color.black)) {
							c.setBackground(clrOutline);
						}
					} else if (c instanceof JTable) {
						//((JTable)c).setBorder(BorderFactory.createLineBorder(clrOutline, 1));
						((JTable)c).setGridColor(clrOutline);
					}
				}
				} catch (Exception e) {
					System.out.println("comp error:" + c.getName());
				}
				// header
				if (c.getName().equals("header")) {
					c.setForeground(clrHeader);
				}
				// labels
				if (c.getName().equals("label")) {
					c.setForeground(clrLabel);
				}
				// declared values
				if (c.getName().equals("declared")) {
					c.setForeground(clrDeclared);
				}
				// actual values
				if (c.getName().equals("actual")) {
					c.setForeground(clrActual);
				}
				
			}
			// next component (recursive)
			if (c instanceof Container) {
				resetCompColors((Container) c);
			}
		}
	}
	
	// function to set report title as it is dynamic based on IS
	private void setReportTitle() {
		String tit = "PUMP";
		String isStr = "";
		if (chkCat.isSelected()) {
			tit = curPumpCat.toUpperCase();
		} else {
			if (Configuration.LAST_USED_ISSTD.contains("8472")) {
				tit = "SELF PRIMING PUMP";
			} else if (Configuration.LAST_USED_ISSTD.contains("9079")) {
				tit = "MONOBLOCK PUMP";
			} else if (Configuration.LAST_USED_ISSTD.contains("8034")) {
				tit = "SUBMERSIBLE PUMP";
			} else if (Configuration.LAST_USED_ISSTD.contains("6595")) {
				tit = "CENTRIFUGAL PUMP";
			}
		}
		
		curPumpIsNonISI = lblNonISIFilter.isVisible();
		if (curPumpIsNonISI) {
			isStr = "";
		} else {
			//isStr = " AS PER " + Configuration.LAST_USED_ISSTD.substring(0,Configuration.LAST_USED_ISSTD.indexOf(":"));
			isStr = " AS PER " + Configuration.LAST_USED_ISSTD;
		}
		
		// register report
		lblTitle.setText(tit + " TEST REGISTER");
		//lblTitle.update(lblTitle.getGraphics());
				
		// test result
		lblTitle2.setText(tit + " PERFORMANCE TEST REPORT" + isStr);
		//lblTitle2.update(lblTitle2.getGraphics());
		
		// graph
		lblTitle3.setText(tit + " PERFORMANCE TEST GRAPH" + isStr);
		
		// isi 1 - test report
		lblTitle41.setText(tit + " PERFORMANCE TEST REPORT" + isStr);
		
		// isi 2 - type test report
		lblTitle42.setText(tit + " PERFORMANCE TEST REPORT" + isStr);
		
		// isi 3 - min/max report
		lblTitle43.setText(tit + " PERFORMANCE TEST REPORT" + isStr);

		// compare graph
		lblTitle5.setText("GRAPH COMPARISON (" + tit + ")");
		
		// custom report
		lblTitle6.setText("TYPE TEST REPORT" + isStr);
	}
	
	// function to set report header of all reports
	private void setReportHeader() {
		setReportTitle();
		
		// register report
		lblCompName.setText(Configuration.LICENCEE_NAME);
		lblCompAdr.setText(Configuration.LICENCEE_ADR_1 + ", " + Configuration.LICENCEE_ADR_2);
				
		// test result
		lblCompName2.setText(Configuration.LICENCEE_NAME);
		lblCompAdr2.setText(Configuration.LICENCEE_ADR_1 + ", " + Configuration.LICENCEE_ADR_2);
		
		// graph
		lblCompName3.setText(Configuration.LICENCEE_NAME);
		lblCompAdr3.setText(Configuration.LICENCEE_ADR_1 + ", " + Configuration.LICENCEE_ADR_2);
		
		// isi 1 - test report
		lblCompName41.setText(Configuration.LICENCEE_NAME);
		lblCompAdr41.setText(Configuration.LICENCEE_ADR_1 + ", " + Configuration.LICENCEE_ADR_2);
		
		// isi 2 - type test report
		lblCompName42.setText(Configuration.LICENCEE_NAME);
		lblCompAdr42.setText(Configuration.LICENCEE_ADR_1 + ", " + Configuration.LICENCEE_ADR_2);
		
		// isi 3 - min/max report
		lblCompName43.setText(Configuration.LICENCEE_NAME);
		lblCompAdr43.setText(Configuration.LICENCEE_ADR_1 + ", " + Configuration.LICENCEE_ADR_2);

		// compare graph
		lblCompName5.setText(Configuration.LICENCEE_NAME);
		lblCompAdr5.setText(Configuration.LICENCEE_ADR_1 + ", " + Configuration.LICENCEE_ADR_2);
		
		// custom report
		lblCompName6.setText(Configuration.LICENCEE_NAME);
		lblCompAdr6.setText(Configuration.LICENCEE_ADR_1 + ", " + Configuration.LICENCEE_ADR_2);
		
		// company logo & isi logos
		try {
			lblCustLogo.setIcon(new ImageIcon(Configuration.APP_DIR + "/img/company_logo.png"));
			lblCustLogo2.setIcon(new ImageIcon(Configuration.APP_DIR + "/img/company_logo.png"));
			lblCustLogo3.setIcon(new ImageIcon(Configuration.APP_DIR + "/img/company_logo.png"));
			lblCustLogo41.setIcon(new ImageIcon(Configuration.APP_DIR + "/img/company_logo.png"));
			lblCustLogo42.setIcon(new ImageIcon(Configuration.APP_DIR + "/img/company_logo.png"));
			lblCustLogo43.setIcon(new ImageIcon(Configuration.APP_DIR + "/img/company_logo.png"));
			lblCustLogo5.setIcon(new ImageIcon(Configuration.APP_DIR + "/img/company_logo.png"));
			lblCustLogo6.setIcon(new ImageIcon(Configuration.APP_DIR + "/img/company_logo.png"));
		} catch (Exception ie) {
			// ignore
		}
		
		Boolean isiRef = false;
		if (Configuration.LICENCEE_ISI_REF != null) {
			 isiRef = !Configuration.LICENCEE_ISI_REF.isEmpty();
		}
		
		if (isiRef) {
			
			// need some update here
			// String strIS = Configuration.LAST_USED_ISSTD.substring(0,Configuration.LAST_USED_ISSTD.indexOf(":")).replace(" ",":");
			String strIS = Configuration.LAST_USED_ISSTD;
			
			lblIS.setText(strIS);
			lblISIRefNo.setText(Configuration.LICENCEE_ISI_REF);
			
			lblIS2.setText(strIS);
			lblISIRefNo2.setText(Configuration.LICENCEE_ISI_REF);
			
			lblIS3.setText(strIS);
			lblISIRefNo3.setText(Configuration.LICENCEE_ISI_REF);
			
			lblIS41.setText(strIS);
			lblISIRefNo41.setText(Configuration.LICENCEE_ISI_REF);
			
			lblIS42.setText(strIS);
			lblISIRefNo42.setText(Configuration.LICENCEE_ISI_REF);
			
			lblIS43.setText(strIS);
			lblISIRefNo43.setText(Configuration.LICENCEE_ISI_REF);
			
			lblIS5.setText(strIS);
			lblISIRefNo5.setText(Configuration.LICENCEE_ISI_REF);
			
			lblIS6.setText(strIS);
			lblISIRefNo6.setText(Configuration.LICENCEE_ISI_REF);
		}
		
		lblIS.setVisible(isiRef && !curPumpIsNonISI);
		lblISILogo.setVisible(isiRef && !curPumpIsNonISI);
		lblISIRefNo.setVisible(isiRef && !curPumpIsNonISI);
		lblIS2.setVisible(isiRef && !curPumpIsNonISI);
		lblISILogo2.setVisible(isiRef && !curPumpIsNonISI);
		lblISIRefNo2.setVisible(isiRef && !curPumpIsNonISI);
		lblIS3.setVisible(isiRef && !curPumpIsNonISI);
		lblISILogo3.setVisible(isiRef && !curPumpIsNonISI);
		lblISIRefNo3.setVisible(isiRef && !curPumpIsNonISI);
		
		lblIS41.setVisible(isiRef && !curPumpIsNonISI);
		lblISILogo41.setVisible(isiRef && !curPumpIsNonISI);
		lblISIRefNo41.setVisible(isiRef && !curPumpIsNonISI);
		lblIS42.setVisible(isiRef && !curPumpIsNonISI);
		lblISILogo42.setVisible(isiRef && !curPumpIsNonISI);
		lblISIRefNo42.setVisible(isiRef && !curPumpIsNonISI);
		lblIS43.setVisible(isiRef);
		lblISILogo43.setVisible(isiRef);
		lblISIRefNo43.setVisible(isiRef);
		
		lblIS5.setVisible(isiRef && !curPumpIsNonISI);
		lblISILogo5.setVisible(isiRef && !curPumpIsNonISI);
		lblISIRefNo5.setVisible(isiRef && !curPumpIsNonISI);
		
		lblIS6.setVisible(isiRef && !curPumpIsNonISI);
		lblISILogo6.setVisible(isiRef && !curPumpIsNonISI);
		lblISIRefNo6.setVisible(isiRef && !curPumpIsNonISI);
	}
	
	private void setPanelTitle(String pumpNos, String testedV) {
		// set panel title with cur pump number
		String titleText = "Print Preview";
		if (pumpNos != null && !pumpNos.isEmpty()) {
			if (curMVPump) {
				titleText = "Print Preview [Pump SNo: " + pumpNos + " Voltage: " + testedV +"]";
			} else {
				titleText = "Print Preview [Pump SNo: " + pumpNos + "]";
			}
		}
		printContainer.setBorder(new TitledBorder(null, titleText, TitledBorder.LEADING, TitledBorder.TOP,
				new Font("Arial", Font.BOLD, 14), Color.blue));
	}
	
	private void loadRegisterReport() {
		try {
			// load static values
			lblDis1.setText("<html>" + curDisUnit + "</html>");
			lblSHd1.setText(curHdUnit);
			lblDHd1.setText(curHdUnit);
			
			// drop temporary table and reconstruct it based on current filter
			db.executeUpdate("drop table if exists TEMP_REPORT");
			
			lblPumpType.setText(cmbType.getSelectedItem().toString());
			
			String qryString = "select * from " + Configuration.READING_DETAIL + " " + pumpFilterText + " order by test_date, pump_slno, rated_volt, test_name_code";
		
			qryString = "create table TEMP_REPORT as " + qryString;
			db.executeUpdate(qryString);

		} catch (Exception sqle) {
			JOptionPane.showMessageDialog(this, "Failed to refresh the report\nError:" + sqle.getMessage());
			return;
		}
		
		// load first page as a default
		curPageRegRep = 1;
		gotoPageRegRep(curPageRegRep);
	}
	
	private void loadTestResReport(int page) {
		
		try {
			addTestResult(page, tabReport.getSelectedIndex());
		} catch (Exception e) {
			if (e.getMessage().contains("MyResultSet closed")) {
				// ignore this as this will happen when cleartable called
			} else {
				JOptionPane.showMessageDialog(this, "Error loading test result:" + e.getMessage());
				e.printStackTrace();
				return;
			}
		}
		
		if (totAvailPages > 0) {
			lblPage2.setText("Page " + page + " of " + totAvailPages);
		} else {
			lblPage2.setText("No records found");
		}
		
		enableDisableButtons();
		return;
	}
	
	private void loadTestGraph(int page) {

		try {
			addTestGraph(page, tabReport.getSelectedIndex());
		} catch (Exception e) {
			if (e.getMessage().contains("MyResultSet closed")) {
				// ignore this as this will happen when cleartable called
			} else {
				JOptionPane.showMessageDialog(this, "Error loading graph: Insufficient values");
				return;
			}
		}
		
		if (totAvailPages > 0) {
			lblPage3.setText("Page " + page + " of " + totAvailPages);
		} else {
			lblPage3.setText("No records found");
		}
		
		enableDisableButtons();
		return;
	}
	
	private void loadISIReport(int page) {
		
		// add test result & graph
		try {
			addTestResult(page, tabReport.getSelectedIndex());
			addTestGraph(page, tabReport.getSelectedIndex());
		} catch (Exception e) {
			if (e.getMessage() != null && e.getMessage().contains("MyResultSet closed")) {
				// ignore this as this will happen when cleartable called
			} else {
				JOptionPane.showMessageDialog(this, "Error loading test result:" + e.getMessage());
				e.printStackTrace();
				return;
			}
		}
		
		if (totAvailPages > 0) {
			lblPage41.setText("Page " + page + " of " + totAvailPages);
		} else {
			lblPage41.setText("No records found");
		}
		
		enableDisableButtons();
		return;
	}
	
	private void loadISITypeTest() {
		try {
			// drop temporary table and reconstruct it based on current filter
			db.executeUpdate("drop table if exists TEMP_REPORT_ISI_TYPE");
			
			// set static values
			lblPumpType42.setText(cmbType.getSelectedItem().toString());
			lblDt42.setText(reqDtFormat.format(fromDt.getDate()) + " TO " + reqDtFormat.format(toDt.getDate()));
			
			// guaranteed values
			String tmpRtV = "";
			MyResultSet res = db.executeQuery("select * from " + Configuration.PUMPTYPE + " where type = '" + cmbType.getSelectedItem().toString() + "'");
			if (res.next()) {
				lblTypeCount42.setText("ONE FOR EVERY " + res.getString("type_test_freq") + " PUMPS");
				lblFlow42.setText(deriveFlow(res.getDouble("discharge")));
				lblHd42.setText("<html><body align=\"center\">Total Head<br>(" + curHdUnit +")</body></html>");
				lblHead42.setText(deriveHead(res.getDouble("head")));
				if(Configuration.LAST_USED_ISSTD.startsWith("IS 8472:") && Configuration.REP_SHOW_MI_FOR_8472.equals("1")) { // ISI handling
					lblMotIp42.setText(res.getString("mot_ip"));
				}
				lblEff42.setText(res.getString("overall_eff"));
				
				lblMC42.setText(res.getString("amps"));
				lblDis42.setText("<html><body align=\"center\">Discharge<br>(" + curDisUnit + ")</body></html>");
				tmpRtV = res.getString("volts");
			}
			
			res = db.executeQuery("select count(distinct(pump_slno)) as tot from " + Configuration.READING_DETAIL + " " + pumpFilterText );
			if (res.next()) {
				lblTotTest42.setText(res.getString("tot"));
			}
			
			// desire records only
			String qryString = "select pump_slno, max(test_date)  as test_date, rated_volt from " + Configuration.READING_DETAIL + " " + pumpFilterText + " and test_type = 'T' and rated_volt = '" + tmpRtV + "' group by pump_slno order by test_date, pump_slno, rated_volt";
			qryString = "create table TEMP_REPORT_ISI_TYPE as " + qryString;
			db.executeUpdate(qryString);
			
			MyResultSet res2 = db.executeQuery("select count(distinct(pump_slno)) as tot from TEMP_REPORT_ISI_TYPE");
			if (res2.next()) {
				lblTotTypeTest42.setText(res2.getString("tot"));
			}

		} catch (Exception sqle) {
			JOptionPane.showMessageDialog(this, "Failed to refresh the report\nError:" + sqle.getMessage());
			return;
		}
		
		// load first page as a default
		curPageISIType = 1;
		gotoPageISIType(curPageISIType);
	}
	
	private void loadISIMaxMin() {
		try {
			// set static values
			lblDt43.setText(reqDtFormat.format(fromDt.getDate()) + " TO " + reqDtFormat.format(toDt.getDate()));
			
			// select pump types
			String pTypes = " where non_isi_model = 'N'";
			
			if (optSelPump.isSelected()) {
				pTypes = " where type = '" + cmbType.getSelectedItem().toString() + "'";
			}
			
			pumpTypeList.clear();
			pumpTypeRtVList.clear();
			MyResultSet res = db.executeQuery("select type, volts from " + Configuration.PUMPTYPE + pTypes);
			if (res.isRowsAvail()) {
				while (res.next()) {
					pumpTypeList.add(res.getString("type"));
					pumpTypeRtVList.put(res.getString("type"), res.getString("volts"));
				}
			}
			lblHd43.setText("<html><body align=\"center\">Total Head<br>(" + curHdUnit +")</body></html>");
			// decide to display the discharge unit on top or left
			res = db.executeQuery("select distinct(discharge_unit) from " + Configuration.PUMPTYPE + pTypes );
			if (res.isRowsAvail()) {
				int c = 0;
				while(res.next()) {
					++c;
				}
				if (c > 1 && !defDisUnit.equals(curDisUnit)) {
					mixedDisUnit = true;
					lblDis43.setText("<html><body align=\"center\">Discharge</body></html>");
				} else {
					mixedDisUnit = false;
					lblDis43.setText("<html><body align=\"center\">Discharge<br>(" + curDisUnit + ")</body></html>");
				}
			}
		} catch (Exception sqle) {
			JOptionPane.showMessageDialog(this, "Failed to refresh the report\nError:" + sqle.getMessage());
			return;
		}
		
		// load first page as a default
		curPageISIMaxMin = 1;
		gotoPageISIMaxMin(curPageISIMaxMin);
	}
	
	private void gotoPageISIMaxMin(int page) {
			// clear the table
			DefaultTableModel defModel = (DefaultTableModel) tblRes43.getModel();
			while (tblRes43.getRowCount() > 0) {
				defModel.removeRow(0);
			}
			
			int toRow = (int) (page * totRowsPerPageISIMaxMin);
			int fromRow = (int) (toRow - (totRowsPerPageISIMaxMin) + 1);
			
			try {
				MyResultSet res = null;
				MyResultSet res2 = null;
				String pType = "";
				String tmpRtV = "";
				String flowUnitStr = "";
				String flowUnit = "";
				String pumpNo = "";
				int r=0;
				int ra=0;
				for (int c=fromRow; c<=toRow && c <= pumpTypeList.size(); c++) {
					
					pType = pumpTypeIdList.get(pumpTypeList.get(c-1));
					tmpRtV = pumpTypeRtVList.get(pumpTypeList.get(c-1));
					
					defModel.addRow( new Object[] {"","","","","","","","","","","","","","","","","","","","",""});
					tblRes43.setValueAt(pumpTypeList.get(c-1), r, 0);
					
					// guaranteed
					res2 = db.executeQuery("select * from " + Configuration.PUMPTYPE + " where pump_type_id = '" + pType + "'");
					if (res2.next()) {
						if (mixedDisUnit) {
							flowUnit = res2.getString("discharge_unit");
							flowUnitStr = " (" + flowUnit + ")";
						} else {
							flowUnit = curDisUnit;
							flowUnitStr = "";
						}
						
						tblRes43.setValueAt("<html>" + deriveFlow(res2.getDouble("discharge")) + flowUnitStr + "</html>", r, 1);
						tblRes43.setValueAt(deriveHead(res2.getDouble("head")), r, 6);
						ra=11;
						if(Configuration.LAST_USED_ISSTD.startsWith("IS 8472:") && Configuration.REP_SHOW_MI_FOR_8472.equals("1")) { // ISI handling
							tblRes43.setValueAt(dotThree.format(res2.getDouble("mot_ip")), r, ra);
							ra+=5;
						}
						tblRes43.setValueAt(dotTwo.format(res2.getDouble("overall_eff")), r, ra);
						ra += 5;
						tblRes43.setValueAt(dotTwo.format(res2.getDouble("amps")), r, ra);
					}
					
					// select all pump snos with in cur pump type
					res = db.executeQuery("select distinct(pump_slno) as pump_slno from " + Configuration.READING_DETAIL + " where pump_type_id = '" + pType + "' " + filterTextNoType + " and test_type='T' and rated_volt = '" + tmpRtV + "' order by 1");
					if (res.isRowsAvail()) {
						Double actVal[] = {0.0,0.0,0.0,0.0,0.0};
						pumpNo = "";
						
						Double maxDis = 0.0;
						Double maxHd = 0.0;
						Double maxMI = 0.0; // will be used only for 8472 and 12225
						Double maxE = 0.0;
						Double maxCur = 0.0;
						String maxDisPump = "";
						String maxHdPump = "";
						String maxMIPump = ""; // will be used only for 8472 and 12225
						String maxEPump = "";
						String maxCurPump = "";
						
						Double minDis = 999999999.0;
						Double minHd = 999999999.0;
						Double minMI = 999999999.0; // will be used only for 8472 and 12225
						Double minE = 999999999.0;
						Double minCur = 999999999.0;
						String minDisPump = "";
						String minHdPump = "";
						String minMIPump = ""; // will be used only for 8472 and 12225
						String minEPump = "";
						String minCurPump = "";
						
						while (res.next()) {
							// calculate actual and sort it
							pumpNo = res.getString("pump_slno");
							actVal = findActualValues(pumpNo, pType, tmpRtV);
							// max
							if (actVal[0] > maxDis) {
								maxDis = actVal[0];
								maxDisPump = pumpNo;
							}
							if (actVal[1] > maxHd) {
								maxHd = actVal[1];
								maxHdPump = pumpNo;
							}
							if (actVal[2] > maxMI) {
								maxMI = actVal[2];
								maxMIPump = pumpNo;
							}
							if (actVal[3] > maxE) {
								maxE = actVal[3];
								maxEPump = pumpNo;
							}
							if (actVal[4] > maxCur) {
								maxCur = actVal[4];
								maxCurPump = pumpNo;
							}
							
							// min
							if (actVal[0] < minDis) {
								minDis = actVal[0];
								minDisPump = pumpNo;
							}
							if (actVal[1] < minHd) {
								minHd = actVal[1];
								minHdPump = pumpNo;
							}
							if (actVal[2] < minMI) {
								minMI = actVal[2];
								minMIPump = pumpNo;
							}
							if (actVal[3] < minE) {
								minE = actVal[3];
								minEPump = pumpNo;
							}
							if (actVal[4] < minCur) {
								minCur = actVal[4];
								minCurPump = pumpNo;
							}
							
						} // while
						
						// set the max/min actuals
						if (!minDisPump.isEmpty()) { // true when max/min values were not set due to unavailability of readings
							if (flowUnit.endsWith("s") || flowUnit.contains("sup")) {
								tblRes43.setValueAt(dotTwo.format(maxDis), r, 2);
								tblRes43.setValueAt(dotTwo.format(minDis), r, 4);
							} else if (flowUnit.endsWith("m")) {
								tblRes43.setValueAt(dotOne.format(maxDis), r, 2);
								tblRes43.setValueAt(dotOne.format(minDis), r, 4);
							} else {
								tblRes43.setValueAt(dotZero.format(maxDis), r, 2);
								tblRes43.setValueAt(dotZero.format(minDis), r, 4);
							}
							
							tblRes43.setValueAt(maxDisPump, r, 3);
							tblRes43.setValueAt(minDisPump, r, 5);
							
							tblRes43.setValueAt(deriveHead(maxHd, true), r, 7);
							tblRes43.setValueAt(maxHdPump, r, 8);
							tblRes43.setValueAt(deriveHead(minHd, true), r, 9);
							tblRes43.setValueAt(minHdPump, r, 10);
							ra = 12;
							if(Configuration.LAST_USED_ISSTD.startsWith("IS 8472:") && Configuration.REP_SHOW_MI_FOR_8472.equals("1")) { // ISI handling
								tblRes43.setValueAt(dotThree.format(maxMI), r, ra++);
								tblRes43.setValueAt(maxMIPump, r, ra++);
								tblRes43.setValueAt(dotThree.format(minMI), r, ra++);
								tblRes43.setValueAt(minMIPump, r, ra);
								ra+=2;
							}
							tblRes43.setValueAt(dotTwo.format(maxE), r, ra++);
							tblRes43.setValueAt(maxEPump, r, ra++);
							tblRes43.setValueAt(dotTwo.format(minE), r, ra++);
							tblRes43.setValueAt(minEPump, r, ra);
							ra+=2;
							
							tblRes43.setValueAt(dotTwo.format(maxCur), r, ra++);
							tblRes43.setValueAt(maxCurPump, r, ra++);
							tblRes43.setValueAt(dotTwo.format(minCur), r, ra++);
							tblRes43.setValueAt(minCurPump, r, ra);
						}
					}
					++r;
				}// for next pump type
				
				// footer
				if (Configuration.REP_SHOW_TESTER_NAME.equals("1")) {
					MyResultSet res3 = db.executeQuery("select user from " + Configuration.READING_DETAIL + " where pump_type_id='" + curPumpTypeId+ "' and pump_slno = '" + pumpNo + "' and rated_volt='" + tmpRtV +"'");
					if (!pumpNo.isEmpty() && res3.next()) {
						lblTestedBy43.setText("Tested By [" + res3.getString("user").toUpperCase() + "]");
					}
				}
			} catch (Exception sqle) {
				JOptionPane.showMessageDialog(this, "Failed to load current page\nError:" + sqle.getMessage());
				sqle.printStackTrace();
				return;
			}
			
			if (totAvailPagesISIMaxMin > 0) {
				lblPage42.setText("Page " + page + " of " + totAvailPagesISIMaxMin);
			} else {
				lblPage42.setText("No records found");
			}
			
			enableDisableButtons();
			setPanelTitle("", "");
			return;
	}
	
	private void loadCompareGraph() {
		lblPumpType5.setText(cmbType.getSelectedItem().toString());
		// add graph
		try {
			// remove if the chart is already exist
			try {
				pnlGraph5.remove(0);
			} catch (ArrayIndexOutOfBoundsException ae) {
				// ignore it
			}
			
			// construct data set based on comparison factor selection
			JFreeChart chart = ChartFactory.createXYLineChart("", curDisUnit.contains("sup")?"Discharge (m^3ph)":"Discharge (" + curDisUnit + ")", null, null, PlotOrientation.VERTICAL, true, true, false); // workaround on unit
			XYPlot p = chart.getXYPlot();
			
			String pumpNo = "";
			String tmpRtV = "";
			MyResultSet res = null;
			
			Double rtD = 0.0D;
			Double rtH = 0.0D;
			Double rtP = 0.0D;
			Double rtE = 0.0D;
			Double rtC = 0.0D;
			Double upHead = 0.0D;
			Double upMi = 0D;
			Double upE = 0D;
			Double upCur = 0.0D;
			int totRec = 0;
			int idx = 0;
			
			XYSeriesCollection dsetH = new XYSeriesCollection();
			XYSeriesCollection dsetP = new XYSeriesCollection();
			XYSeriesCollection dsetE = new XYSeriesCollection();
			XYSeriesCollection dsetC = new XYSeriesCollection();
			
			printArea5.remove(lblRtWarn5);
			for(int i=0; i<pumpList.size(); i++) {
				if (curMVPump) {
					pumpNo = pumpList.get(i).substring(0, pumpList.get(i).indexOf("[")-1);
					tmpRtV = pumpList.get(i).substring(pumpList.get(i).indexOf("[")+1,pumpList.get(i).length()-2);
				} else {
					pumpNo = pumpList.get(i);
					tmpRtV = curRtV;
				}
				
				// retrieve head, pi and cur values and create corresponding data sets
				res = db.executeQuery("select * from " + Configuration.READING_DETAIL + " where pump_type_id='" + curPumpTypeId + "' and pump_slno='" + pumpNo + "' and rated_volt ='" + tmpRtV +"' order by test_name_code");
				
				if (res.isRowsAvail()) {
					
					// display warning if any of the test being compared is a routine test
					if (res.getString("test_type").equals("R")) {
						printArea5.add(lblRtWarn5, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
					}
					
					// footer
					if (Configuration.REP_SHOW_TESTER_NAME.equals("1")) {
						lblTestedBy5.setText("Tested By [" + res.getString("user").toUpperCase() + "]"); // overwrite with last tested user
					}
					
					// do perf calculation at rated freq 
					Calculate perfCalc = new Calculate();
					perfCalc.doCalculate(pumpNo,  curPumpTypeId, isRatedSpeed, tmpRtV);
					
					// create data set for all axis
					XYSeries srH = new XYSeries("TH(" + pumpList.get(i) + ")");
					XYSeries srP = new XYSeries("IP(" + pumpList.get(i) + ")"); // will be used only for 8472 and 12225
					XYSeries srE = new XYSeries("Eff(" + pumpList.get(i) + ")");
					XYSeries srC = new XYSeries("Cur(" + pumpList.get(i) + ")");
					
					totRec = 0;
					while (res.next()) {
						rtH = Double.valueOf(deriveHead(perfCalc.getRatedTotHead(totRec)));
						rtP = perfCalc.getRatedMotorIP(totRec); // will be used only for 8472 and 12225
						rtE = perfCalc.getRatedOE(totRec);
						rtC = res.getDouble("current");
						rtD = Double.valueOf(deriveFlow(perfCalc.getRatedDischarge(totRec)));
						
						srH.add(rtD, rtH);
						srP.add(rtD, rtP); // will be used only for 8472 and 12225
						srE.add(rtD, rtE);
						srC.add(rtD, rtC);
						
						// for range calc
						upHead = upHead < rtH ? rtH : upHead;
						upMi = upMi < rtP ? rtP : upMi; // will be used only for 8472 and 12225
						upE = upE < rtE ? rtE : upE;
						upCur = upCur < rtC ? rtC : upCur;
						
						++totRec;
					}
					
					dsetH.addSeries(srH);
					dsetP.addSeries(srP); // will be used only for 8472 and 12225
					dsetE.addSeries(srE);
					dsetC.addSeries(srC);
				}
			} // for
			
			// calculate upper bound range for head, pi, c
			upHead = Math.ceil(upHead+(upHead/10));
			upMi = Math.ceil(upMi+(upMi/30)); // will be used only for 8472 and 12225
			upE = Math.ceil(upE+(upE/30));
			upCur = Math.ceil(upCur+(upCur/30));
			
			// add head axis
			if (chkQvH.isSelected()) {
				NumberAxis axisHead = new NumberAxis("Total Head (" + curHdUnit + ")     1 DIV=" + deriveHead(upHead/100, true));
				axisHead.setLabelPaint(clrHead);
				axisHead.setAxisLinePaint(clrHead);
				Range rgHd = new Range(0.0, upHead);
				axisHead.setRange(rgHd);
				axisHead.setTickUnit(new NumberTickUnit(upHead/10));
				axisHead.setMinorTickCount(10);
				p.setRangeAxis(idx, axisHead);
				p.setRangeAxisLocation(idx, AxisLocation.TOP_OR_LEFT);
				XYItemRenderer rndrHd = null;
				if (isSmoothLine) {
					rndrHd = new XYSplineRenderer(10);
				} else {
					rndrHd = new DefaultXYItemRenderer();
				}
				rndrHd.setPaint(clrHead);
				p.setRenderer(idx, rndrHd);
				p.setDataset(idx, dsetH);
			    p.mapDatasetToRangeAxis(idx, idx);
			    ++idx;
			}
			
			// add mi axis
			if (chkQvMI.isSelected()) {
				NumberAxis axisMi;
				axisMi = new NumberAxis("Input Power (kW)     1 DIV=" + dotThree.format(upMi/100));
				axisMi.setLabelPaint(clrMI);
				axisMi.setAxisLinePaint(clrMI);
				Range rgMi = new Range(0.0, upMi);
				axisMi.setRange(rgMi);
				axisMi.setTickUnit(new NumberTickUnit(upMi/10));
				axisMi.setMinorTickCount(10);
				p.setRangeAxis(idx, axisMi);
				p.setRangeAxisLocation(idx, AxisLocation.TOP_OR_LEFT);
				XYItemRenderer rndrMi = null;
				if (isSmoothLine) {
					rndrMi = new XYSplineRenderer(10);
				} else {
					rndrMi = new DefaultXYItemRenderer();
				}
				rndrMi.setPaint(clrMI);
				p.setRenderer(idx, rndrMi);
		        p.setDataset(idx, dsetP);
		        p.mapDatasetToRangeAxis(idx, idx);
		        ++idx;
			}
			
			// add eff axis
			if (chkQvOE.isSelected()) {
				NumberAxis axisE;
				axisE = new NumberAxis("O.A Eff. (%)     1 DIV=" + dotTwo.format(upE/100));
				
				axisE.setLabelPaint(clrE);
				axisE.setAxisLinePaint(clrE);
				Range rgE = new Range(0.0, upE);
				axisE.setRange(rgE);
				axisE.setTickUnit(new NumberTickUnit(upE/10));
				axisE.setMinorTickCount(10);
				p.setRangeAxis(idx, axisE);
				p.setRangeAxisLocation(idx, AxisLocation.TOP_OR_LEFT);
				XYItemRenderer rndrE = null;
				if (isSmoothLine) {
					rndrE = new XYSplineRenderer(10);
				} else {
					rndrE = new DefaultXYItemRenderer();
				}
				rndrE.setPaint(clrE);
				p.setRenderer(idx, rndrE);
		        p.setDataset(idx, dsetE);
		        p.mapDatasetToRangeAxis(idx, idx);
		        ++idx;
			}
			
	        // add cur axis
			if (chkQvC.isSelected()) {
		        NumberAxis axisCur = new NumberAxis("Current (A)     1 DIV=" + dotTwo.format(upCur/100));
		        axisCur.setLabelPaint(clrCur);
		        axisCur.setAxisLinePaint(clrCur);
		        Range rgCur = new Range(0.0, upCur);
		        axisCur.setRange(rgCur);
		        axisCur.setTickUnit(new NumberTickUnit(upCur/10));
		        axisCur.setMinorTickCount(10);
				p.setRangeAxis(idx, axisCur);
				p.setRangeAxisLocation(idx, AxisLocation.TOP_OR_LEFT);
				XYItemRenderer rndrCur = null;
				if (isSmoothLine) {
					rndrCur = new XYSplineRenderer(10);
				} else {
					rndrCur = new DefaultXYItemRenderer();
				}
				rndrCur.setPaint(clrCur);
				p.setRenderer(idx, rndrCur);
		        p.setDataset(idx, dsetC);
		        p.mapDatasetToRangeAxis(idx, idx);
			}
			
	        // other chart look & feel
			chart.getLegend().setPosition(RectangleEdge.RIGHT);
			p.getDomainAxis().setLabelFont(p.getRangeAxis().getLabelFont());
			p.getDomainAxis().setTickLabelFont(p.getRangeAxis().getTickLabelFont());
			p.setDomainGridlinesVisible(true);
			p.setRangeGridlinesVisible(true);
			p.setBackgroundPaint(Color.green);
			p.setBackgroundAlpha(0.10F);
			p.setDomainGridlinePaint(Color.DARK_GRAY);
			p.setRangeGridlinePaint(Color.DARK_GRAY);
			p.setDomainMinorGridlinesVisible(true);
			p.setRangeMinorGridlinesVisible(true);
			p.setDomainMinorGridlinePaint(Color.LIGHT_GRAY);
			p.setRangeMinorGridlinePaint(Color.LIGHT_GRAY);
			
			p.getDomainAxis().setMinorTickMarksVisible(true);
			p.getDomainAxis().setMinorTickCount(10);
			
			for(int i=0; i < p.getRangeAxisCount(); i++) {
				p.getRangeAxis(i).setMinorTickMarksVisible(true);
				p.getRenderer(i).setSeriesToolTipGenerator(0,new XYToolTipGenerator() {
					@Override
					public String generateToolTip(XYDataset arg0, int arg1, int arg2) {
						return "(X:"+ ((curDisUnit.endsWith("lps") || curDisUnit.contains("sup")) ?dotTwo.format(arg0.getXValue(arg1, arg2)):curDisUnit.endsWith("m")?dotOne.format(arg0.getXValue(arg1, arg2)):dotZero.format(arg0.getXValue(arg1, arg2))) + ", Y:" + dotThree.format(arg0.getYValue(arg1, arg2)) + ")";
					}
				});
			}
			
			// add chart
			ChartPanel chPanel = new ChartPanel(chart);
			chart.getTitle().setFont(new Font("Arial", Font.BOLD, 16));
			pnlGraph5.add(chPanel, new TableLayoutConstraints(0,0,0,0,TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			pnlGraph5.revalidate();
		} catch (Exception e) {
			e.printStackTrace();
			if (e.getMessage().contains("MyResultSet closed")) {
				// ignore this as this will happen when cleartable called
			} else {
				JOptionPane.showMessageDialog(this, "Error loading graph: Insufficient values");
				return;
			}
		}
		
		if (totAvailPagesCompare > 0) {
			lblPage5.setText("Page 1 of 1");
		} else {
			lblPage5.setText("No records found");
		}
		
		enableDisableButtons();
		setPanelTitle("", "");
		return;
	}
	
	private void loadCustomReport(int page) {
		try {
			String pumpNo = "";
			String tmpRtVolt = "";
			
			if (curMVPump) {
				pumpNo = pumpList.get(page-1).substring(0, pumpList.get(page-1).indexOf("[")-1);
				tmpRtVolt = pumpList.get(page-1).substring(pumpList.get(page-1).indexOf("[")+1,pumpList.get(page-1).length()-2);
			} else {
				pumpNo = pumpList.get(page-1);
				tmpRtVolt = curRtV;
			}
			
			// clear result table
			DefaultTableModel defModel = (DefaultTableModel) tblRes6.getModel();
			while (tblRes6.getRowCount() > 0) {
				defModel.removeRow(0);
			}
					
			MyResultSet res = db.executeQuery("select * from " + Configuration.READING_DETAIL + " where pump_type_id='" + curPumpTypeId + "' and pump_slno='" + pumpNo + "' and rated_volt = '" + tmpRtVolt +"' order by test_name_code");
			
			if (res.isRowsAvail()) {
				// top labels
				String flowUnit = curDisUnit;
				Double corHead = 0.0D;
				MyResultSet res2 = db.executeQuery("select * from " + Configuration.PUMPTYPE + " where pump_type_id = '" + res.getString("pump_type_id") + "'");
				if (res2.next()) {
					lblPumpType6.setText(res2.getString("type"));
					lblHdDuty6.setText("  Duty Head (" + curHdUnit + ")");
					lblHdSO6.setText("  SO Head (" + curHdUnit + ")");
					lblHdRg6.setText("  Hd. Range (" + curHdUnit + ")");
					lblNoStage6.setText(res2.getString("no_of_stage"));
					txtCon6.setText(res2.getString("conn"));
					lblHead6.setText(deriveHead(res2.getDouble("head")));
					lblFlowRtLabel6.setText("<html>&nbsp;&nbsp;Duty Disc (" + flowUnit + ")</html>");
					lblFlow6.setText(res2.getString("discharge").isEmpty()?"-":deriveFlow(res2.getDouble("discharge")));
					lblFlowRtLabel62.setText("<html>&nbsp;&nbsp;Di Range (" + flowUnit + ")</html>");
					lblDisR6.setText((res2.getString("discharge_low").isEmpty()?"-":deriveFlow(res2.getDouble("discharge_low"))) + " / " + (res2.getString("discharge_high").isEmpty()?"-":deriveFlow(res2.getDouble("discharge_high"))));
					lblMotIp6.setText(res2.getString("mot_ip"));
					lblFreq6.setText(res2.getString("freq"));
					lblSpeed6.setText(res2.getString("speed"));
					lblHeadR6.setText(res2.getString("head_low") + " / " + res2.getString("head_high"));
					lblMRt6.setText(res2.getString("kw"));
					lblPh6.setText(res2.getString("phase"));
					lblV6.setText(res2.getString("volts"));
					lblCur6.setText(res2.getString("amps"));
					lblPSno6.setText(res.getString("pump_slno"));
					lblDt6.setText(reqDtFormat.format(dbDtFormat.parse(res.getString("test_date"))));
					// ISI handling
					if (Configuration.LAST_USED_ISSTD.startsWith("IS 8034:") || Configuration.LAST_USED_ISSTD.startsWith("IS 14220:")) {
						lblPSz6.setText(res2.getString("delivery_size"));
					} else {
						lblPSz6.setText(res2.getString("suction_size") + " X " + res2.getString("delivery_size"));
					}
					//lblNoStage6.setText(res2.getString("no_of_stage"));
					lblMotType6.setText(res2.getString("mot_type"));
					lblMotEff6.setText(res2.getString("mot_eff").isEmpty()?"-":res2.getString("mot_eff"));
					
					lblSHHd6.setText(deriveHead(res2.getDouble("so_head")));
					lblSHCur6.setText(res2.getString("so_current"));
					lblSHPow6.setText(res2.getString("so_power"));
					
					//reading header labels
					if (isRatedSpeed) {
						lblPerfHz6.setText("Perf. At Rated Speed " + res2.getString("speed") + " rpm");
					} else {
						lblPerfHz6.setText("Perf. At Rated Freq. " + res2.getString("freq") + " Hz");
					}
					lblHdS6.setText(curHdUnit);
					lblHdD6.setText(curHdUnit);
					lblHdHC6.setText(curHdUnit);
					lblHdVHC6.setText(curHdUnit);
					lblHdT6.setText(curHdUnit);
					lblHdRT6.setText(curHdUnit);
					lblFlowUnitS6.setText("<html>" + flowUnit + "</html>");
					lblFlowUnitS62.setText("<html>" + flowUnit + "</html>");
					
					corHead = res2.getDouble("gauge_distance");
				}
				
				// readings
				
				// calculate performance upfront
				Calculate perfCal = new Calculate();
				perfCal.doCalculate(pumpNo,  curPumpTypeId, isRatedSpeed, tmpRtVolt);

				// populate reading
				int r=0;
				int c=0;
				Double maxDis = 0.0D;
				String usr = "";
				int tsno = 1;
				while (res.next()) {
					c = 0;
					defModel.addRow( new Object[] {"","","","","","","","","","","","","","","","",""});
					tblRes6.setValueAt(tsno++, r, c++);
					
					tblRes6.setValueAt(dotZero.format(res.getDouble("speed")), r, c++);
					// ISI handling
					if(!(Configuration.LAST_USED_ISSTD.startsWith("IS 8034:") || Configuration.LAST_USED_ISSTD.startsWith("IS 14220:"))) {
						tblRes6.setValueAt(deriveHead(res.getDouble("shead")), r, c++);
					} else {
						tblRes6.setValueAt("-", r, c++);
					}
					tblRes6.setValueAt(deriveHead(res.getDouble("dhead")), r, c++);
					maxDis = Math.max(maxDis, Double.valueOf(deriveHead(res.getDouble("dhead"))));
					tblRes6.setValueAt(deriveHead(corHead), r, c++);
					tblRes6.setValueAt(deriveHead(perfCal.getVHC(r)), r, c++);
					tblRes6.setValueAt(deriveHead(res.getDouble("shead")+res.getDouble("dhead")+corHead+perfCal.getVHC(r)), r, c++);
					
					tblRes6.setValueAt(deriveFlow(res.getDouble("flow")), r, c++);
					
					tblRes6.setValueAt(dotOne.format(res.getFloat("volt")), r, c++);
					tblRes6.setValueAt(dotTwo.format(res.getFloat("current")), r, c++);
					tblRes6.setValueAt(dotThree.format(res.getFloat("power")), r, c++);
					tblRes6.setValueAt(dotTwo.format(res.getFloat("freq")), r, c++);
					
					// performance at rated freq
					tblRes6.setValueAt(deriveFlow(perfCal.getRatedDischarge(r)), r, c++);
					tblRes6.setValueAt(deriveHead(perfCal.getRatedTotHead(r)), r, c++);
					tblRes6.setValueAt(dotThree.format(perfCal.getRatedMotorIP(r)), r, c++);
					tblRes6.setValueAt(dotTwo.format(perfCal.getRatedOE(r)), r, c++);
					/*if (Configuration.LAST_USED_ISSTD.startsWith("IS 8472:")) { // ISI handling
						tblRes6.setValueAt("-", r, c++);
					} else { */
						tblRes6.setValueAt(dotTwo.format(perfCal.getRatedPE(r)), r, c++);
					//}
					
					++r;
					
					// footer
					usr = res.getString("user"); // overwrite with last tested user
					
				}
				if (Configuration.REP_SHOW_TESTER_NAME.equals("1")) {
					lblTestedBy6.setText("Tested By [" + usr.toUpperCase() + "]");
				}
				
				// custom input
				MyResultSet res3 = db.executeQuery("select * from " + Configuration.CUSTOM_REPORT + " where pump_type_id = '" + curPumpTypeId + "' and pump_slno='" + pumpNo + "' and rated_volt = '" + tmpRtVolt +"'");
				
				//setCustomInputColor(new Color(204, 255, 255));
				txtCap6.setText("");
				txtCRMain6.setText("");
				txtCRAux6.setText("");
				txtCRTemp6.setText("");
				txtDuty6.setText("");
				txtCon6.setText("");
				txtSBed6.setText("");
				txtIRTest.setText("");
				txtSPTime.setText("");
				txtV6.setText("");
				txtA6.setText("");
				txtP6.setText("");
				txtS6.setText("");
				txtF6.setText("");
				txtH6.setText("");
				txtBT6.setText("");
				txtRT6.setText("");
				txtM61.setText("");
				txtA61.setText("");
				txtT61.setText("");
				txtM62.setText("");
				txtA62.setText("");
				txtT62.setText("");
				txtRem.setText("");
				if (res3.next()) {
					txtCap6.setText(res3.getString("cap"));
					txtCRMain6.setText(res3.getString("cr_main"));
					txtCRAux6.setText(res3.getString("cr_aux"));
					txtCRTemp6.setText(res3.getString("cr_tmp"));
					txtDuty6.setText(res3.getString("duty"));
					txtCon6.setText(res3.getString("con"));
					txtSBed6.setText(res3.getString("suc_bed"));
					txtIRTest.setText(res3.getString("ir_val"));
					txtSPTime.setText(res3.getString("sp_time"));
					txtV6.setText(res3.getString("tr_v"));
					txtA6.setText(res3.getString("tr_a"));
					txtP6.setText(res3.getString("tr_p"));
					txtS6.setText(res3.getString("tr_s"));
					txtF6.setText(res3.getString("tr_f"));
					txtH6.setText(res3.getString("tr_t"));
					txtBT6.setText(res3.getString("tr_bt"));
					txtRT6.setText(res3.getString("tr_rt"));
					txtM61.setText(res3.getString("hr_main"));
					txtA61.setText(res3.getString("hr_aux"));
					txtT61.setText(res3.getString("hr_tmp"));
					txtM62.setText(res3.getString("wt_main"));
					txtA62.setText(res3.getString("wt_aux"));
					txtT62.setText(res3.getString("st_tmp"));
					txtRem.setText(res3.getString("remark"));
				}
			}
			if (txtRem.getText().isEmpty()) {
				txtRem.setText("PASS");
			}
			setPanelTitle(pumpNo, tmpRtVolt);
		} catch (Exception e) {
			if (e.getMessage().contains("MyResultSet closed") || e.getMessage().contains("no such table")) {
				// ignore this as this will happen when clear table called
			} else {
				JOptionPane.showMessageDialog(this, "Error loading test result:" + e.getMessage());
				return;
			}
		}
		
		if (totAvailPages > 0) {
			lblPage6.setText("Page " + page + " of " + totAvailPages);
		} else {
			lblPage6.setText("No records found");
		}
		
		enableDisableButtons();
		return;
	}
	
	private void setCustomInputColor(Color bg) {
		txtCap6.setBackground(bg);
		txtCRMain6.setBackground(bg);
		txtCRAux6.setBackground(bg);
		txtCRTemp6.setBackground(bg);
		txtDuty6.setBackground(bg);
		txtCon6.setBackground(bg);
		txtSBed6.setBackground(bg);
		txtIRTest.setBackground(bg);
		txtSPTime.setBackground(bg);
		txtV6.setBackground(bg);
		txtA6.setBackground(bg);
		txtP6.setBackground(bg);
		txtS6.setBackground(bg);
		txtF6.setBackground(bg);
		txtH6.setBackground(bg);
		txtBT6.setBackground(bg);
		txtRT6.setBackground(bg);
		txtM61.setBackground(bg);
		txtA61.setBackground(bg);
		txtT61.setBackground(bg);
		txtM62.setBackground(bg);
		txtA62.setBackground(bg);
		txtT62.setBackground(bg);
		txtRem.setBackground(bg);
		pnlPrint6.revalidate();
	}
	
	private Double[] findActualValues(String pumpNo, String pType, String testV) throws Exception {
		Double[] actVal = {0.0,0.0,0.0,0.0,0.0};  // discharge, head, mi & current
		
		try {
			MyResultSet res = db.executeQuery("select * from " + Configuration.READING_DETAIL + " where pump_type_id='" + pType + "' and pump_slno='" + pumpNo + "' and rated_volt ='" + testV +"' order by test_name_code");
			if (res.isRowsAvail()) {
				MyResultSet res2 = db.executeQuery("select * from " + Configuration.PUMPTYPE + " where pump_type_id = '" + pType + "'");
				
				// do perf calculation at rated freq 
				Calculate perfCalc = new Calculate();
				perfCalc.doCalculate(pumpNo,  pType, isRatedSpeed, testV);
				
				// create data set for all axis
				XYSeries srH = new XYSeries("TH");
				XYSeries srP = new XYSeries("IP"); // will be used only for 8472 and 12225
				XYSeries srE = new XYSeries("OE");
				XYSeries srC = new XYSeries("Cur.");
				
				Double rtD = 0.0D;
				Double rtH = 0.0D;
				Double rtP = 0.0D;
				Double rtE = 0.0D;
				Double rtC = 0.0D;
				
				int totRec = 0;
				while (res.next()) {
					rtH = Double.valueOf(deriveHead(perfCalc.getRatedTotHead(totRec)));
					rtP = perfCalc.getRatedMotorIP(totRec); // will be used only for 8472 and 12225
					rtE = perfCalc.getRatedOE(totRec);
					rtC = res.getDouble("current");
				
					rtD = Double.valueOf(deriveFlow(perfCalc.getRatedDischarge(totRec)));
					srH.add(rtD, rtH);
					srP.add(rtD, rtP);
					srE.add(rtD, rtE); // will be used only for 8472 and 12225
					srC.add(rtD, rtC);
				
					++totRec;
				}
				
				Double gDis = Double.valueOf(deriveFlow(res2.getDouble("discharge")));
				Double gHd = Double.valueOf(deriveHead(res2.getDouble("head")));
				 
				// intersection of actual discharge vs actual TH via slope (m1) of guaranteed values
				CustLine gLine = new CustLine(0D,0D,srH.getMaxX(),srH.getMaxY());
				Double gslope = gHd/gDis;
				CustLine il = new CustLine();
				Double ix = 0.0D;
				Double iy = 0.0D;
				for(int i=0; i<srH.getItemCount()-1; i++) {
					il = findIntersection(gLine, new CustLine(srH.getX(i).doubleValue(),srH.getY(i).doubleValue(),srH.getX(i+1).doubleValue(),srH.getY(i+1).doubleValue()),gslope, null);
					if (il != null) {
						ix = il.getX2();
						iy = il.getY2();
						break;
					}
				}
				
				// update actual labels
				actVal[0] = ix; // act discharge
				actVal[1] = iy; // act th
				
				// 3. motor input cut by actual head - will be used only for 8472 and 12225
				CustLine ADvsAHLine = new CustLine(ix,0.0D,ix,srH.getMaxY()+10); // 10 just to increase scope of base line, no effect on normal cases
				Double miix = 0.0D;
				Double miiy = 0.0D;
				for(int i=0; i<srP.getItemCount()-1; i++) {
					il = findIntersection(ADvsAHLine, new CustLine(srP.getX(i).doubleValue(),srP.getY(i).doubleValue(),srP.getX(i+1).doubleValue(),srP.getY(i+1).doubleValue()),null, null);
					if (il != null) {
						miix = il.getX2();
						miiy = il.getY2();
						break;
					}
				}
				// update actual label
				actVal[2] = miiy;
				
				// 3.1 OA eff cut by actual head
				Double eix = 0.0D;
				Double eiy = 0.0D;
				for(int i=0; i<srE.getItemCount()-1; i++) {
					il = findIntersection(ADvsAHLine, new CustLine(srE.getX(i).doubleValue(),srE.getY(i).doubleValue(),srE.getX(i+1).doubleValue(),srE.getY(i+1).doubleValue()),null, null);
					if (il != null) {
						eix = il.getX2();
						eiy = il.getY2();
						break;
					}
				}
				// update actual label
				actVal[3] = eiy;
				
				
				// 4. max current between guaranteed head range
				// 4.1 low head range cut current
				CustLine lowRangeLine;
				Double gHdL = Double.valueOf(deriveHead(res2.getDouble("head_low")));
		        if (gHdL<srH.getMinY()) {
		        	lowRangeLine = new CustLine(0D,srH.getMinY(),srH.getMaxX(),srH.getMinY());
		        } else {
		        	lowRangeLine = new CustLine(0D,gHdL,srH.getMaxX(),gHdL);
		        }
				Double lhix = 0.0D;
				Double lhiy = 0.0D;
				for(int i=0; i<srH.getItemCount()-1; i++) {
					il = findIntersection(lowRangeLine, new CustLine(srH.getX(i).doubleValue(),srH.getY(i).doubleValue(),srH.getX(i+1).doubleValue(),srH.getY(i+1).doubleValue()),null, null);
					if (il != null) {
						lhix = il.getX2();
						lhiy = il.getY2();
						break;
					}
				}
				
				CustLine lowRangeCutLine = new CustLine(lhix,0.0D,lhix,srH.getMaxY());
				Double lhcix = 0.0D;
				Double lhciy = 0.0D;
				int curIndex1 = 0;
				for(int i=0; i<srC.getItemCount()-1; i++) {
					il = findIntersection(lowRangeCutLine, new CustLine(srC.getX(i).doubleValue(),srC.getY(i).doubleValue(),srC.getX(i+1).doubleValue(),srC.getY(i+1).doubleValue()),null, null);
					if (il != null) {
						lhcix = il.getX2();
						lhciy = il.getY2();
						curIndex1 = i+1;
						break;
					}
				}
				
				// 4.2 high head range cut current
				Double gHdH = Double.valueOf(deriveHead(res2.getDouble("head_high")));
				CustLine hiRangeLine = new CustLine(0D,gHdH,srH.getMaxX(),gHdH);
		        Double hhix = 0.0D;
		        Double hhiy = 0.0D;
		        for(int i=0; i<srH.getItemCount()-1; i++) {
		        	il = findIntersection(hiRangeLine, new CustLine(srH.getX(i).doubleValue(),srH.getY(i).doubleValue(),srH.getX(i+1).doubleValue(),srH.getY(i+1).doubleValue()),null, null);
		        	if (il != null) {
		        		hhix = il.getX2();
		        		hhiy = il.getY2();
		        		break;
		        	}
		        }
		        
		        CustLine hiRangeCutLine = new CustLine(hhix,0.0D,hhix,srH.getMaxY());
		        Double hhcix = 0.0D;
		        Double hhciy = 0.0D;
		        int curIndex2 = 0;
		        for(int i=0; i<srC.getItemCount()-1; i++) {
		        	il = findIntersection(hiRangeCutLine, new CustLine(srC.getX(i).doubleValue(),srC.getY(i).doubleValue(),srC.getX(i+1).doubleValue(),srC.getY(i+1).doubleValue()),null, null);
		        	if (il != null) {
		        		hhcix = il.getX2();
		        		hhciy = il.getY2();
		        		curIndex2 = i+1;
		        		break;
		        	}
		        }
		        
				 // 4.3 find max current between low and high cuts
				Double maxCur = 0.0D;
				if(curIndex1 < curIndex2) {
					for(int i=curIndex1; i <= curIndex2; i++) {
						maxCur = Math.max(maxCur, srC.getY(i).doubleValue());
					}
				} else {
					for(int i=curIndex1; i >= curIndex2; i--) {
						maxCur = Math.max(maxCur, srC.getY(i).doubleValue());
					}
				}
			
				maxCur = Math.max(maxCur, Math.max(lhciy, hhciy)); // this line is enough to find max current, but above loops make sure to find max curr in case there is a peak betweek low & hi cut.
			
				// update actual label
				actVal[4] = maxCur;
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			//throw (new Exception("Error calculating actual performance:" + e.getMessage()));
		}
		return actVal;
	}
	
	private void addTestResult(int page, int idx) throws Exception {
		String pumpNo = "";
		String tmpRtVolt = "";
		if (curMVPump) {
			pumpNo = pumpList.get(page-1).substring(0, pumpList.get(page-1).indexOf("[")-1);
			tmpRtVolt = pumpList.get(page-1).substring(pumpList.get(page-1).indexOf("[")+1,pumpList.get(page-1).length()-2); 
		} else {
			pumpNo = pumpList.get(page-1);
			tmpRtVolt = curRtV;
		}
		
		JTable tblTestRes = new JTable();
		JLabel lblUser = new JLabel();
		if (idx == 1) {
			tblTestRes = tblRes2;
			lblUser = lblTestedBy2;
		} else {
			tblTestRes = tblRes41;
			lblUser = lblTestedBy41;
		}
		// result table
		DefaultTableModel defModel = (DefaultTableModel) tblTestRes.getModel();
		while (tblTestRes.getRowCount() > 0) {
			defModel.removeRow(0);
		}
		
		MyResultSet res = db.executeQuery("select * from " + Configuration.READING_DETAIL + " where pump_type_id='" + curPumpTypeId + "' and pump_slno='" + pumpNo + "' and rated_volt = '" + tmpRtVolt +"' order by test_name_code");
		
		if (res.isRowsAvail()) {
			// top labels
			String flowUnit = "";
			Double corHead = 0.0D;
			MyResultSet res2 = db.executeQuery("select * from " + Configuration.PUMPTYPE + " where pump_type_id = '" + res.getString("pump_type_id") + "'");
			if (res2.next()) {
				if (idx == 1) {
					lblPumpType2.setText(res2.getString("type"));
					lblMotType2.setText(res2.getString("mot_type"));
					lblCon2.setText(res2.getString("conn"));
					lblHead2.setText(deriveHead(res2.getDouble("head")));
					flowUnit = curDisUnit;
					lblFlowRtLabel.setText("<html>DISCHARGE (" + flowUnit + ")</html>");
					lblHdLbl2.setText("HEAD (" + curHdUnit + ")");
					lblHdRgLbl2.setText("HEAD RANGE (" + curHdUnit + ")");
					lblFlow2.setText(deriveFlow(res2.getDouble("discharge")));
					lblFreq2.setText(res2.getString("freq"));
					lblSpeed2.setText(res2.getString("speed"));
					lblHeadR2.setText(deriveHead(res2.getDouble("head_low")) + " / " + deriveHead(res2.getDouble("head_high")));
					lblMSno2.setText(res.getString("mot_slno"));
					lblMRt2.setText(res2.getString("kw") + " / " + res2.getString("hp"));
					lblPh2.setText(res2.getString("phase"));
					
					// remove last row in header belongs to capacitor if its 3 phase
					Boolean enCap = res2.getString("phase").equals("Single");
					lbllblCapRat2.setVisible(enCap);
					lblCapRate2.setVisible(enCap);
					lbllblCapVolt2.setVisible(enCap);
					lblCapVolt2.setVisible(enCap);
					
					lblV2.setText(res.getString("rated_volt"));
					lblCur2.setText(res2.getString("amps"));
					lblMI2.setText(res2.getString("mot_ip"));
					lbllblGDis2.setText("GAUGE DIST. (" + curHdUnit + ")");
					lblGDis2.setText(deriveHead(res2.getDouble("gauge_distance")));
					lbllblPipeConst2.setText("PIPE CONST. (" + curHdUnit + ")");
					lblPipeConst2.setText(""); // set below
					
					lblPSno2.setText(res.getString("pump_slno"));
					lblDt2.setText(reqDtFormat.format(dbDtFormat.parse(res.getString("test_date"))));
					// ISI handling
					if (Configuration.LAST_USED_ISSTD.startsWith("IS 8034:") || Configuration.LAST_USED_ISSTD.startsWith("IS 14220:")) {
						lblPSz2.setText(res2.getString("delivery_size"));
						lblBrSz2.setText(res2.getString("bore_size"));
						lblNoStg2.setText(res2.getString("no_of_stage"));
					} else {
						lblPSz2.setText(res2.getString("suction_size") + " X " + res2.getString("delivery_size"));
					}
					lblMotEff2.setText(res2.getString("mot_eff"));
					lblOEff2.setText(res2.getString("overall_eff"));
					
					if(Configuration.LAST_USED_ISSTD.startsWith("IS 8472:") || Configuration.LAST_USED_ISSTD.startsWith("IS 9079:") || Configuration.LAST_USED_ISSTD.startsWith("IS 14220:")) {
						lblIns2.setText(res2.getString("ins_class"));
					}
					
					if (Configuration.LAST_USED_ISSTD.startsWith("IS 8472:")) {
						lblSPTime2.setText(res2.getString("self_priming_time"));
					}
					
					lblPoles2.setText(res2.getString("no_of_poles"));
					lblCapRate2.setText(res2.getString("cap_rating"));
					lblCapVolt2.setText(res2.getString("cap_volt"));
					
					//reading header labels
					if (isRatedSpeed) {
						lblPerfHz2.setText("Perf. At Rated Speed " + res2.getString("speed") + " rpm");
					} else {
						lblPerfHz2.setText("Perf. At Rated Freq. " + res2.getString("freq") + " Hz");
					}
					lblFlowUnitS2.setText("<html>" + flowUnit + "</html>");
					if (!Configuration.LAST_USED_ISSTD.startsWith("IS 12225")) {
						lblFlowUnitT2.setText("<html>" + flowUnit + "</html>"); // set in isi handling for 12225
					}
					lblSHd2.setText(curHdUnit);
					lblDHd2.setText(curHdUnit);
					lblVHd2.setText(curHdUnit);
					lblTotHd2.setText(curHdUnit);
					lblTotHdT2.setText(curHdUnit);
					
					
				} else {
					lblPumpType41.setText(res2.getString("type"));
					lblMotType41.setText(res2.getString("mot_type"));
					lblCon41.setText(res2.getString("conn"));
					lblHead41.setText(deriveHead(res2.getDouble("head")));
					flowUnit = curDisUnit;
					lblFlowRtLabel41.setText("<html>DISCHARGE (" + flowUnit + ")</html>");
					lblHdLbl41.setText("HEAD (" + curHdUnit + ")");
					lblHdRglbl41.setText("HEAD RANGE (" + curHdUnit + ")");
					lblFlow41.setText(deriveFlow(res2.getDouble("discharge")));
					lblFreq41.setText(res2.getString("freq"));
					lblSpeed41.setText(res2.getString("speed"));
					lblHeadR41.setText(deriveHead(res2.getDouble("head_low")) + " / " + deriveHead(res2.getDouble("head_high")));
					lblMSno41.setText(res.getString("mot_slno"));
					lblMRt41.setText(res2.getString("kw") + " / " + res2.getString("hp"));
					lblPh41.setText(res2.getString("phase"));
					
					// remove last row in header belongs to capacitor if its 3 phase
					Boolean enCap = res2.getString("phase").equals("Single");
					lbllblCapRat41.setVisible(enCap);
					lblCapRate41.setVisible(enCap);
					lbllblCapVolt41.setVisible(enCap);
					lblCapVolt41.setVisible(enCap);
					
					lblV41.setText(res.getString("rated_volt"));
					lblCur41.setText(res2.getString("amps"));
					lblMI41.setText(res2.getString("mot_ip"));
					lbllblGDis41.setText("GAUGE DIST. (" + curHdUnit + ")");
					lblGDis41.setText(deriveHead(res2.getDouble("gauge_distance")));
					lbllblPipeConst41.setText("PIPE CONST. (" + curHdUnit + ")");
					lblPipeConst41.setText(""); // set below
					
					lblPSno41.setText(res.getString("pump_slno"));
					lblDt41.setText(reqDtFormat.format(dbDtFormat.parse(res.getString("test_date"))));
					// ISI handling
					if (Configuration.LAST_USED_ISSTD.startsWith("IS 8034:") || Configuration.LAST_USED_ISSTD.startsWith("IS 14220:")) {
						lblPSz41.setText(res2.getString("delivery_size"));
						lblBrSz41.setText(res2.getString("bore_size"));
						lblNoStg41.setText(res2.getString("no_of_stage"));
					} else {
						lblPSz41.setText(res2.getString("suction_size") + " X " + res2.getString("delivery_size"));
					}
					lblMotEff41.setText(res2.getString("mot_eff"));
					lblOEff41.setText(res2.getString("overall_eff"));
					
					if(Configuration.LAST_USED_ISSTD.startsWith("IS 8472:") || Configuration.LAST_USED_ISSTD.startsWith("IS 9079:") || Configuration.LAST_USED_ISSTD.startsWith("IS 14220:")) {
						lblIns41.setText(res2.getString("ins_class"));
					}
					
					if (Configuration.LAST_USED_ISSTD.startsWith("IS 8472:")) {
						lblSPTime41.setText(res2.getString("self_priming_time"));
					}
					
					lblPoles41.setText(res2.getString("no_of_poles"));
					lblCapRate41.setText(res2.getString("cap_rating"));
					lblCapVolt41.setText(res2.getString("cap_volt"));
					
					//reading header labels
					if (isRatedSpeed) {
						lblPerfHz41.setText("Perf. At Rated Speed " + res2.getString("speed") + " rpm");
					} else {
						lblPerfHz41.setText("Perf. At Rated Freq. " + res2.getString("freq") + " Hz");
					}
					lblFlowUnitS41.setText("<html>" + flowUnit + "</html>");
					if (!Configuration.LAST_USED_ISSTD.startsWith("IS 12225")) {
						lblFlowUnitT41.setText("<html>" + flowUnit + "</html>"); // set in isi handling for 12225
					}
					lblSHd41.setText(curHdUnit);
					lblDHd41.setText(curHdUnit);
					lblVHd41.setText(curHdUnit);
					lblTotHd41.setText(curHdUnit);
					lblTotHdT41.setText(curHdUnit);
				}
				corHead = res2.getDouble("gauge_distance");
			}
			
			// readings
			
			// calculate performance upfront
			Calculate perfCal = new Calculate();
			perfCal.doCalculate(pumpNo,  curPumpTypeId, isRatedSpeed, tmpRtVolt);

			// header labels after calc
			lblPipeConst2.setText(deriveHead(Configuration.CUR_PIPE_CONST,false,true));
			lblPipeConst41.setText(deriveHead(Configuration.CUR_PIPE_CONST,false,true));
			
			// populate reading
			int r=0;
			int c=0;
			Double maxPres = 0.0D;
			int tsno = 1;
			try {
				while (res.next()) {
					c=0;
					defModel.addRow( new Object[] {"","","","","","","","","","","","","","",""});
					tblTestRes.setValueAt(tsno++, r, c++);
					
					tblTestRes.setValueAt(dotTwo.format(res.getFloat("freq")), r, c++);
					tblTestRes.setValueAt(res.getString("speed"), r, c++);
					// ISI handling
					if(!(Configuration.LAST_USED_ISSTD.startsWith("IS 8034:") || Configuration.LAST_USED_ISSTD.startsWith("IS 14220:"))) {
						tblTestRes.setValueAt(deriveHead(res.getDouble("shead")), r, c++);
					}
					tblTestRes.setValueAt(deriveHead(res.getDouble("dhead")), r, c++);
					tblTestRes.setValueAt(deriveHead(perfCal.getVHC(r)), r, c++);
					tblTestRes.setValueAt(deriveHead(res.getDouble("shead")+res.getDouble("dhead")+corHead+perfCal.getVHC(r)), r, c++);
					
					tblTestRes.setValueAt(deriveFlow(res.getDouble("flow")), r, c++);
					
					tblTestRes.setValueAt(dotOne.format(res.getFloat("volt")), r, c++);
					tblTestRes.setValueAt(dotTwo.format(res.getFloat("current")), r, c++);
					tblTestRes.setValueAt(dotThree.format(res.getFloat("power")), r, c++);
					
					// performance at rated freq
					tblTestRes.setValueAt(deriveFlow(perfCal.getRatedDischarge(r)), r, c++);
					tblTestRes.setValueAt(deriveHead(perfCal.getRatedTotHead(r)), r, c++);
					maxPres = Math.max(maxPres, Double.valueOf(deriveHead(perfCal.getRatedTotHead(r))));
					tblTestRes.setValueAt(dotThree.format(perfCal.getRatedMotorIP(r)), r, c++);
					
					// ISI handling
					//if(!Configuration.LAST_USED_ISSTD.startsWith("IS 8472:")) { // 8034,14220,9079,6595
						tblTestRes.setValueAt(dotThree.format(perfCal.getRatedPumpOP(r)), r, c++);
						tblTestRes.setValueAt(dotTwo.format(perfCal.getRatedOE(r)), r, c++);
						if (Configuration.REP_SHOW_PUMP_EFF.equals("1")) {
							tblTestRes.setValueAt(dotTwo.format(perfCal.getRatedPE(r)), r, c++);
						}
						if (res.getString("remarks").equals("PASS") || res.getString("remarks").equals("FAIL")) {
							tblTestRes.setValueAt("", r, c++);
						} else {
							tblTestRes.setValueAt(res.getString("remarks"), r, c++);
						}
						
					/*} else { // 8472
						tblTestRes.setValueAt(res.getString("remarks"), r, c++);
					}*/
					
					if (Configuration.REP_SHOW_TESTER_NAME.equals("1")) {
						lblUser.setText("Tested By [" + res.getString("user").toUpperCase() + "]"); // overwrite with latest user
					}
					++r;
					
				}
			} catch (Exception e) {
				throw e;
			}
			
			// optional tests
			if (Configuration.IS_SP_TEST_ON.equals("1")) {
				MyResultSet  resOp = db.executeQuery("select * from " + Configuration.OPTIONAL_TEST + " where pump_type_id='" + curPumpTypeId + "' and pump_slno='" + pumpNo + "' and rated_volt='" + tmpRtVolt +"'");
				  while (resOp.next()) {
					  if (resOp.getString("test_code").equals("SP")) {
						  String tmpStr = "Self Priming Time " + resOp.getString("val_1") + " Seconds At " + resOp.getString("val_2") + " Meters Suction Lift";
						  lblSP2.setText(tmpStr);
						  lblSP42.setText(tmpStr);
					  }
				  }
			}
			
			// casing pressure test
			String strTmpUnit = curHdUnit.equals("m") ? "mwc" : curHdUnit;
			
			
			// casing pressure test
			if(Configuration.LAST_USED_ISSTD.startsWith("IS 8472:")) { 
				if (idx == 1) {
					lblMaxDis2.setText("Withstood 1.5 Times Of Working Pressure (" + (deriveHead(res2.getDouble("head") * 1.5)) + " " + strTmpUnit + ") for " + Configuration.REP_CASTING_TEST_MIN);
				} else {
					lblMaxDis41.setText("Withstood 1.5 Times Of Working Pressure (" + (deriveHead(res2.getDouble("head") * 1.5)) + " " + strTmpUnit + ") for " + Configuration.REP_CASTING_TEST_MIN);
				}	
			} else { // 8034,14220,9079,6595
				if (idx == 1) {
					lblMaxDis2.setText("Withstood 1.5 Times Of Maximum Pressure (" + dotTwo.format(maxPres * 1.5) + " " + strTmpUnit + ") for " + Configuration.REP_CASTING_TEST_MIN);
				} else {
					lblMaxDis41.setText("Withstood 1.5 Times Of Maximum Pressure (" + dotTwo.format(maxPres * 1.5) + " " + strTmpUnit + ") for " + Configuration.REP_CASTING_TEST_MIN);
				}
			}
			
			
		}
		setPanelTitle(pumpNo, tmpRtVolt);
	}
	
	private void addTestGraph(int page, int idx) throws Exception {
		String testDt = "";
		String pumpNo = "";
		String tmpRtVolt = "";
		if (curMVPump) {
			pumpNo = pumpList.get(page-1).substring(0, pumpList.get(page-1).indexOf("[")-1);
			tmpRtVolt = pumpList.get(page-1).substring(pumpList.get(page-1).indexOf("[")+1,pumpList.get(page-1).length()-2);
		} else {
			pumpNo = pumpList.get(page-1);
			tmpRtVolt = curRtV;
		}
		
		
		// graph is reused in two reports
		JPanel pnlGraph = new JPanel();
		JLabel lblUser = new JLabel();
		if (idx == 2) {
			pnlGraph = pnlGraph3;
			lblUser = lblTestedBy3;
		} else {
			pnlGraph = pnlGraph41;
			lblUser = lblTestedBy41;
		}
	
		// show result based on selection
		boolean isFlowRange = chkDisR.isSelected();
		if (idx == 2) {
			pnlBotRes3R.setVisible(isFlowRange);
			pnlBotRes3.setVisible(!isFlowRange);
		} else {
			pnlBotRes41R.setVisible(isFlowRange);
			pnlBotRes41.setVisible(!isFlowRange);
		}
		
		// remove if the chart is already exist
		try {
			pnlGraph.remove(0);
		} catch (ArrayIndexOutOfBoundsException ae) {
			// ignore it
		}
		
		// 1. CHART
		JFreeChart chart = ChartFactory.createXYLineChart("", curDisUnit.contains("sup")?"Discharge (m^3ph)":"Discharge (" + curDisUnit + ")", null, null, PlotOrientation.VERTICAL, true, true, false);
		XYPlot p = chart.getXYPlot();
		
		// retrieve head, pi and cur values and create corresponding data sets
		MyResultSet res = db.executeQuery("select * from " + Configuration.READING_DETAIL + " where pump_type_id='" + curPumpTypeId + "' and pump_slno='" + pumpNo + "' and rated_volt = '" + tmpRtVolt +"' order by test_name_code");
		if (res.isRowsAvail()) {
			MyResultSet res2 = db.executeQuery("select * from " + Configuration.PUMPTYPE + " where pump_type_id = '" + res.getString("pump_type_id") + "'");
			
			// display warning if its a routine test
			if (res.getString("test_type").equals("R")) {
				if (idx == 2) {
					printArea3.add(lblRtWarn3, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				} else {
					printArea41.add(lblRtWarn41, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				}
			} else {
				printArea3.remove(lblRtWarn3);
				printArea41.remove(lblRtWarn41);
			}
			
			// do perf calculation at rated freq 
			Calculate perfCalc = new Calculate();
			perfCalc.doCalculate(pumpNo, curPumpTypeId, isRatedSpeed, tmpRtVolt);
			
			testDt = reqDtFormat.format(dbDtFormat.parse(res.getString("test_date")));
			pumpNo = res.getString("pump_slno"); // get unquoted version for later use
			
			// create data set for all axis
			XYSeries srH = new XYSeries("TH");
			XYSeries srP = new XYSeries("IP"); // will be used only for 8472 and 12225
			XYSeries srE = new XYSeries("O.A Eff."); 
			XYSeries srC = new XYSeries("Cur.");
			Double rtD = 0.0D;
			Double rtH = 0.0D;
			Double rtP = 0.0D; // will be used only for 8472 and 12225
			Double rtE = 0.0D;
			Double rtC = 0.0D;
			Double actDis = 0.0D;
			Double actTH = 0.0D;
			Double actMI = 0.0D; // will be used only for 8472 and 12225
			Double actE = 0.0D;
			
			Double actDisL = 0.0D;
			Double actTHL = 0.0D;
			Double actMIL = 0.0D; // will be used only for 8472 and 12225
			Double actEL = 0.0D;
			Double actDisH = 0.0D;
			Double actTHH = 0.0D;
			Double actMIH = 0.0D; // will be used only for 8472 and 12225
			Double actEH = 0.0D;
			
			Double actC = 0.0D;
			
			int totRec = 0;
			String testUser = "";
			Double upHead = 0D;
			Double upMi=0D; // will be used only for 8472 and 12225
			Double upE=0D;
			Double upCur=0D;
			
			try {
				while (res.next()) {
					rtH = Double.valueOf(deriveHead(perfCalc.getRatedTotHead(totRec)));
					rtP = perfCalc.getRatedMotorIP(totRec); // will be used only for 8472 and 12225
					rtE = perfCalc.getRatedOE(totRec); 
					rtC = res.getDouble("current");
					rtD = Double.valueOf(deriveFlow(perfCalc.getRatedDischarge(totRec)));
	
					srH.add(rtD, rtH);
					srP.add(rtD, rtP); // will be used only for 8472 and 12225
					srE.add(rtD, rtE);
					srC.add(rtD, rtC);
					
					// for range calc
					upHead = upHead < rtH ? rtH : upHead;
					upMi = upMi < rtP ? rtP : upMi; // will be used only for 8472 and 12225
					upE = upE < rtE ? rtE : upE;
					upCur = upCur < rtC ? rtC : upCur;
					
					++totRec;
					testUser = res.getString("user");
				}
			} catch (Exception e) {
				throw e;
			}
			XYSeriesCollection dsetH = new XYSeriesCollection(srH);
			XYSeriesCollection dsetP = new XYSeriesCollection(srP); // will be used only for 8472 and 12225
			XYSeriesCollection dsetE = new XYSeriesCollection(srE);
			XYSeriesCollection dsetC = new XYSeriesCollection(srC);
			
			// calculate upper bound range for head, pi, c
			upHead = Math.ceil(upHead+(upHead/10));
			upMi = Math.ceil(upMi+(upMi/30)); // will be used only for 8472 and 12225
			upE = Math.ceil(upE+(upE/30));
			upCur = Math.ceil(upCur+(upCur/30));

			// add head axis
			NumberAxis axisHead = new NumberAxis("Total Head (" + curHdUnit + ")     1 DIV=" + deriveHead(upHead/100, true));
			axisHead.setLabelPaint(clrHead);
			axisHead.setAxisLinePaint(clrHead);
			Range rgHd = new Range(0.0, upHead);
			axisHead.setRange(rgHd);
			axisHead.setTickUnit(new NumberTickUnit(upHead/10));
			axisHead.setMinorTickCount(10);
			p.setRangeAxis(0, axisHead);
			p.setRangeAxisLocation(0, AxisLocation.TOP_OR_LEFT);
			XYItemRenderer rndrHd = null;
			if (isSmoothLine) {
				rndrHd = new XYSplineRenderer(10);
			} else {
				rndrHd = new DefaultXYItemRenderer();
			}
			rndrHd.setPaint(clrHead);
			p.setRenderer(0, rndrHd);
			p.setDataset(0, dsetH);
		    p.mapDatasetToRangeAxis(0, 0);
		    
		    Integer ra=1;
		    
		    // add mi axis
		    NumberAxis axisMi;
		    XYItemRenderer rndrMi = null;
		    if(Configuration.LAST_USED_ISSTD.startsWith("IS 8472:") && Configuration.REP_SHOW_MI_FOR_8472.equals("1")) { // ISI handling
			    axisMi = new NumberAxis("Input Power (kW)     1 DIV=" + dotThree.format(upMi/100));
				
				axisMi.setLabelPaint(clrMI);
				axisMi.setAxisLinePaint(clrMI);
				Range rgMi = new Range(0.0, upMi);
				axisMi.setRange(rgMi);
				axisMi.setTickUnit(new NumberTickUnit(upMi/10));
				axisMi.setMinorTickCount(10);
				p.setRangeAxis(ra, axisMi);
				p.setRangeAxisLocation(ra, AxisLocation.TOP_OR_LEFT);
				
				if (isSmoothLine) {
					rndrMi = new XYSplineRenderer(10);
				} else {
					rndrMi = new DefaultXYItemRenderer();
				}
				rndrMi.setPaint(clrMI);
				p.setRenderer(ra, rndrMi);
		        p.setDataset(ra, dsetP);
		        p.mapDatasetToRangeAxis(ra, ra);
		        ra++;
		    }
			
			// add eff axis
		    NumberAxis axisE;
		    axisE = new NumberAxis("O.A Eff. (%)     1 DIV=" + dotTwo.format(upE/100)); 
			
			axisE.setLabelPaint(clrE);
			axisE.setAxisLinePaint(clrE);
			Range rgE = new Range(0.0, upE);
			axisE.setRange(rgE);
			axisE.setTickUnit(new NumberTickUnit(upE/10));
			axisE.setMinorTickCount(10);
			p.setRangeAxis(ra, axisE);
			p.setRangeAxisLocation(ra, AxisLocation.TOP_OR_LEFT);
			XYItemRenderer rndrE = null;
			if (isSmoothLine) {
				rndrE = new XYSplineRenderer(10);
			} else {
				rndrE = new DefaultXYItemRenderer();
			}
			rndrE.setPaint(clrE);
			p.setRenderer(ra, rndrE);
	        p.setDataset(ra, dsetE);
	        p.mapDatasetToRangeAxis(ra, ra);
	        ra++;
	        
	        // add cur axis
	        NumberAxis axisCur = new NumberAxis("Current (A)     1 DIV=" + dotTwo.format(upCur/100));
	        axisCur.setLabelPaint(clrCur);
	        axisCur.setAxisLinePaint(clrCur);
	        Range rgCur = new Range(0.0, upCur);
	        axisCur.setRange(rgCur);
	        axisCur.setTickUnit(new NumberTickUnit(upCur/10));
	        axisCur.setMinorTickCount(10);
			p.setRangeAxis(ra, axisCur);
			p.setRangeAxisLocation(ra, AxisLocation.TOP_OR_LEFT);
			XYItemRenderer rndrCur = null;
			if (isSmoothLine) {
				rndrCur = new XYSplineRenderer(10);
			} else {
				rndrCur = new DefaultXYItemRenderer();
			}
			rndrCur.setPaint(clrCur);
			
			p.setRenderer(ra, rndrCur);
	        p.setDataset(ra, dsetC);
	        p.mapDatasetToRangeAxis(ra, ra);
	        
	        // mark the points
	        float[] dashes = {5.0F, 5.0F};
	        Stroke dashPen = new BasicStroke(1.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0F, dashes, 0.0F);
	        CustLine il = new CustLine();
	        
	        Double gDis = 0.0D;
	        Double gHd = 0.0D;
	        
	        Double gDisL = 0.0D;
	        Double gDisH = 0.0D;
	        
	        Double gHdL = 0.0D;
	        Double gHdH = 0.0D;
	        
	        if (!isFlowRange) { // default graph view 
	        
	        	// 1. guaranteed discharge vs guaranteed TH
		        gDis = Double.valueOf(deriveFlow(res2.getDouble("discharge")));
		        gHd = Double.valueOf(deriveHead(res2.getDouble("head")));
		        
		        XYLineAnnotation lnGDvsGTH1 = new XYLineAnnotation(0.0D, gHd, gDis, gHd, dashPen,Color.RED);
		        XYLineAnnotation lnGDvsGTH2 = new XYLineAnnotation(gDis, 0.0D, gDis, gHd, dashPen,Color.RED);
		        rndrHd.addAnnotation(lnGDvsGTH1);
		        rndrHd.addAnnotation(lnGDvsGTH2);
		        
		        // 2. actual discharge vs actual TH
		        
		        // intersection of actual discharge vs actual TH via slope (m1) of guaranteed values
		        CustLine gLine = new CustLine(0D,0D,srH.getMaxX(),srH.getMaxY());
		        Double gslope = gHd/gDis;
		        Double ix = 0.0D;
		        Double iy = 0.0D;
		        for(int i=0; i<srH.getItemCount()-1; i++) {
		        	il = findIntersection(gLine, new CustLine(srH.getX(i).doubleValue(),srH.getY(i).doubleValue(),srH.getX(i+1).doubleValue(),srH.getY(i+1).doubleValue()),gslope, null);
		        	if (il != null) {
		        		ix = il.getX2();
		        		iy = il.getY2();
		        		break;
		        	}
		        }
		        
		        // intersection line
		        XYLineAnnotation lnADvsATHcut = new XYLineAnnotation(0.0D, 0.0D, ix, iy, new BasicStroke(1.0F),Color.RED);
		        rndrHd.addAnnotation(lnADvsATHcut);
		        // pointing lines
		        XYLineAnnotation lnADvsATH1 = new XYLineAnnotation(0.0D, iy, ix, iy, new BasicStroke(),Color.RED);
		        XYLineAnnotation lnADvsATH2 = new XYLineAnnotation(ix, 0.0D, ix, iy, new BasicStroke(),Color.RED);
		        rndrHd.addAnnotation(lnADvsATH1);
		        rndrHd.addAnnotation(lnADvsATH2);
		        XYPointerAnnotation pntADvsATH = new XYPointerAnnotation("",0,iy,0);
		        pntADvsATH.setTipRadius(0);
		        pntADvsATH.setArrowLength(20);
		        pntADvsATH.setArrowPaint(Color.RED);
		        XYPointerAnnotation pntADvsATH2 = new XYPointerAnnotation("",ix,0,4.71);
		        pntADvsATH2.setTipRadius(0);
		        pntADvsATH2.setArrowLength(20);
		        pntADvsATH2.setArrowPaint(Color.RED);
		        rndrHd.addAnnotation(pntADvsATH);
		        rndrHd.addAnnotation(pntADvsATH2);
		        
		        // update actual labels
		        if (idx == 2) {
		        	
		        	if (curDisUnit.endsWith("s") || curDisUnit.contains("sup")) {
		        		lblFlowAct3.setText(dotTwo.format(ix));
					} else if (curDisUnit.endsWith("m")) {
						lblFlowAct3.setText(dotOne.format(ix));
					} else {
						lblFlowAct3.setText(dotZero.format(ix));
					}
		        	lblHeadAct3.setText(deriveHead(iy, true));
		        } else {
		        	if (curDisUnit.endsWith("s") || curDisUnit.contains("sup")) {
		        		lblFlowAct41.setText(dotTwo.format(ix));
					} else if (curDisUnit.endsWith("m")) {
						lblFlowAct41.setText(dotOne.format(ix));
					} else {
						lblFlowAct41.setText(dotZero.format(ix));
					}
		        	lblHeadAct41.setText(deriveHead(iy, true));
		        }
		        actDis = ix;
		        actTH = iy;
		        
		        // 3. motor input cut by actual head
		        CustLine ADvsAHLine = new CustLine(ix,0.0D,ix,srH.getMaxY()+500); // 500 just increases scope of base line, no effect for normal cases
		        if(Configuration.LAST_USED_ISSTD.startsWith("IS 8472:") && Configuration.REP_SHOW_MI_FOR_8472.equals("1")) { // ISI handling
				    Double miix = 0.0D;
			        Double miiy = 0.0D;
			        for(int i=0; i<srP.getItemCount()-1; i++) {
			        	il = findIntersection(ADvsAHLine, new CustLine(srP.getX(i).doubleValue(),srP.getY(i).doubleValue(),srP.getX(i+1).doubleValue(),srP.getY(i+1).doubleValue()),null, null);
			        	if (il != null) {
			        		miix = il.getX2();
			        		miiy = il.getY2();
			        		break;
			        	}
			        }
			        XYDrawableAnnotation mrkATHvsMI = new XYDrawableAnnotation(miix,miiy,12,12,new CircleDrawer(Color.red, new BasicStroke(1.0F), null));
			        rndrMi.addAnnotation(mrkATHvsMI);
			        
			        // update actual label
		        	if (idx == 2) {
			        	lblMotIpAct3.setText(dotThree.format(miiy));
			        } else {
			        	lblMotIpAct41.setText(dotThree.format(miiy));
			        }
			        actMI = miiy;
		        }
		        
		     // 3.1 motor input/OA Eff cut by actual head
		        Double eix = 0.0D;
		        Double eiy = 0.0D;
		        for(int i=0; i<srE.getItemCount()-1; i++) {
		        	il = findIntersection(ADvsAHLine, new CustLine(srE.getX(i).doubleValue(),srE.getY(i).doubleValue(),srE.getX(i+1).doubleValue(),srE.getY(i+1).doubleValue()),null, null);
		        	if (il != null) {
		        		eix = il.getX2();
		        		eiy = il.getY2();
		        		break;
		        	}
		        }
		        XYDrawableAnnotation mrkATHvsE = new XYDrawableAnnotation(eix,eiy,12,12,new CircleDrawer(Color.red, new BasicStroke(1.0F), null));
		        rndrE.addAnnotation(mrkATHvsE);
		        
		        // update actual label
				if (idx == 2) {
		        	lblEffAct3.setText(dotTwo.format(eiy));
		        } else {
		        	lblEffAct41.setText(dotTwo.format(eiy));
		        } 
		        actE = eiy;
	        } else { // graph view - flow range
	        	
		        // 1.1 guaranteed discharge at guaranteed TH low
		        gDisL = Double.valueOf(deriveFlow(res2.getDouble("discharge_low")));
		        gHdL = Double.valueOf(deriveHead(res2.getDouble("head_low")));
		        
		        XYLineAnnotation lnGDLvsGTHL1 = new XYLineAnnotation(0.0D, gHdL, gDisL, gHdL, dashPen,Color.MAGENTA);
		        XYLineAnnotation lnGDLvsGTHL2 = new XYLineAnnotation(gDisL, 0.0D, gDisL, gHdL, dashPen,Color.MAGENTA);
		        rndrHd.addAnnotation(lnGDLvsGTHL1);
		        rndrHd.addAnnotation(lnGDLvsGTHL2);
		        
		        // 1.2 guaranteed discharge at guaranteed TH high
		        gDisH = Double.valueOf(deriveFlow(res2.getDouble("discharge_high")));
		        gHdH = Double.valueOf(deriveHead(res2.getDouble("head_high")));
		        
		        XYLineAnnotation lnGDHvsGTHH1 = new XYLineAnnotation(0.0D, gHdH, gDisH, gHdH, dashPen, Color.MAGENTA);
		        XYLineAnnotation lnGDHvsGTHH2 = new XYLineAnnotation(gDisH, 0.0D, gDisH, gHdH, dashPen, Color.MAGENTA);
		        rndrHd.addAnnotation(lnGDHvsGTHH1);
		        rndrHd.addAnnotation(lnGDHvsGTHH2);
		        
		        // 2.1 actual discharge at actual TH low
		        
		        // intersection of actual discharge at actual TH low via slope (m1) of guaranteed values
		        CustLine gLineL = new CustLine(0D,0D,srH.getMaxX(),srH.getMaxY());
		        Double gslope = gHdL/gDisL;
		        Double ix = 0.0D;
		        Double iy = 0.0D;
		        for(int i=0; i<srH.getItemCount()-1; i++) {
		        	il = findIntersection(gLineL, new CustLine(srH.getX(i).doubleValue(),srH.getY(i).doubleValue(),srH.getX(i+1).doubleValue(),srH.getY(i+1).doubleValue()),gslope, null);
		        	if (il != null) {
		        		ix = il.getX2();
		        		iy = il.getY2();
		        		break;
		        	}
		        }
		        
		        // intersection line
		        XYLineAnnotation lnADvsATHcutL = new XYLineAnnotation(0.0D, 0.0D, ix, iy, new BasicStroke(1.0F),Color.MAGENTA);
		        rndrHd.addAnnotation(lnADvsATHcutL);
		        // pointing lines
		        XYLineAnnotation lnADvsATH1L = new XYLineAnnotation(0.0D, iy, ix, iy, new BasicStroke(),Color.MAGENTA);
		        XYLineAnnotation lnADvsATH2L = new XYLineAnnotation(ix, 0.0D, ix, iy, new BasicStroke(),Color.MAGENTA);
		        rndrHd.addAnnotation(lnADvsATH1L);
		        rndrHd.addAnnotation(lnADvsATH2L);
		        XYPointerAnnotation pntADvsATHL = new XYPointerAnnotation("",0,iy,0);
		        pntADvsATHL.setTipRadius(0);
		        pntADvsATHL.setArrowLength(20);
		        pntADvsATHL.setArrowPaint(Color.MAGENTA);
		        XYPointerAnnotation pntADvsATH2L = new XYPointerAnnotation("",ix,0,4.71);
		        pntADvsATH2L.setTipRadius(0);
		        pntADvsATH2L.setArrowLength(20);
		        pntADvsATH2L.setArrowPaint(Color.MAGENTA);
		        rndrHd.addAnnotation(pntADvsATHL);
		        rndrHd.addAnnotation(pntADvsATH2L);
		        
		        // update actual labels
		        if (idx == 2) {
		        	if (curDisUnit.endsWith("s") || curDisUnit.contains("sup")) {
		        		lblFlowAct3RL.setText(dotTwo.format(ix));
					} else if (curDisUnit.endsWith("m")) {
						lblFlowAct3RL.setText(dotOne.format(ix));
					} else {
						lblFlowAct3RL.setText(dotZero.format(ix));
					}
		        	lblHeadAct3RL.setText(deriveHead(iy, true));
		        } else {
		        	if (curDisUnit.endsWith("s") || curDisUnit.contains("sup")) {
		        		lblFlowAct41RL.setText(dotTwo.format(ix));
					} else if (curDisUnit.endsWith("m")) {
						lblFlowAct41RL.setText(dotOne.format(ix));
					} else {
						lblFlowAct41RL.setText(dotZero.format(ix));
					}
		        	lblHeadAct41RL.setText(deriveHead(iy, true));
		        }
		        actDisL = ix;
		        actTHL = iy;
		        
		        // 2.2 actual discharge at actual TH high
		        
		        // intersection of actual discharge at actual TH high via slope (m1) of guaranteed values
		        CustLine gLineH = new CustLine(0D,0D,srH.getMaxX(),srH.getMaxY());
		        gslope = gHdH/gDisH;
		        ix = 0.0D;
		        iy = 0.0D;
		        for(int i=0; i<srH.getItemCount()-1; i++) {
		        	il = findIntersection(gLineH, new CustLine(srH.getX(i).doubleValue(),srH.getY(i).doubleValue(),srH.getX(i+1).doubleValue(),srH.getY(i+1).doubleValue()),gslope, null);
		        	if (il != null) {
		        		ix = il.getX2();
		        		iy = il.getY2();
		        		break;
		        	}
		        }
		        
		        // intersection line
		        XYLineAnnotation lnADvsATHcutH = new XYLineAnnotation(0.0D, 0.0D, ix, iy, new BasicStroke(1.0F),Color.MAGENTA);
		        rndrHd.addAnnotation(lnADvsATHcutH);
		        // pointing lines
		        XYLineAnnotation lnADvsATH1H = new XYLineAnnotation(0.0D, iy, ix, iy, new BasicStroke(),Color.MAGENTA);
		        XYLineAnnotation lnADvsATH2H = new XYLineAnnotation(ix, 0.0D, ix, iy, new BasicStroke(),Color.MAGENTA);
		        rndrHd.addAnnotation(lnADvsATH1H);
		        rndrHd.addAnnotation(lnADvsATH2H);
		        XYPointerAnnotation pntADvsATHH = new XYPointerAnnotation("",0,iy,0);
		        pntADvsATHH.setTipRadius(0);
		        pntADvsATHH.setArrowLength(20);
		        pntADvsATHH.setArrowPaint(Color.MAGENTA);
		        XYPointerAnnotation pntADvsATH2H = new XYPointerAnnotation("",ix,0,4.71);
		        pntADvsATH2H.setTipRadius(0);
		        pntADvsATH2H.setArrowLength(20);
		        pntADvsATH2H.setArrowPaint(Color.MAGENTA);
		        rndrHd.addAnnotation(pntADvsATHH);
		        rndrHd.addAnnotation(pntADvsATH2H);
		        
		        // update actual labels
		        if (idx == 2) {
		        	if (curDisUnit.endsWith("s") || curDisUnit.contains("sup")) {
		        		lblFlowAct3RH.setText(dotTwo.format(ix));
					} else if (curDisUnit.endsWith("m")) {
						lblFlowAct3RH.setText(dotOne.format(ix));
					} else {
						lblFlowAct3RH.setText(dotZero.format(ix));
					}
		        	lblHeadAct3RH.setText(deriveHead(iy, true));
		        } else {
		        	if (curDisUnit.endsWith("s") || curDisUnit.contains("sup")) {
		        		lblFlowAct41RH.setText(dotTwo.format(ix));
					} else if (curDisUnit.endsWith("m")) {
						lblFlowAct41RH.setText(dotOne.format(ix));
					} else {
						lblFlowAct41RH.setText(dotZero.format(ix));
					}
		        	lblHeadAct41RH.setText(deriveHead(iy, true));
		        }
		        actDisH = ix;
		        actTHH = iy;
		        
		        // 3.1 motor input cut by actual head low
		        CustLine ADvsAHLine = new CustLine(actDisL,0.0D,actDisL,srH.getMaxY()+10); // 10 just increases scope of base line, no effect for normal cases
		        if(Configuration.LAST_USED_ISSTD.startsWith("IS 8472:") && Configuration.REP_SHOW_MI_FOR_8472.equals("1")) { // ISI handling
				    Double miix = 0.0D;
			        Double miiy = 0.0D;
			        for(int i=0; i<srP.getItemCount()-1; i++) {
			        	il = findIntersection(ADvsAHLine, new CustLine(srP.getX(i).doubleValue(),srP.getY(i).doubleValue(),srP.getX(i+1).doubleValue(),srP.getY(i+1).doubleValue()),null, null);
			        	if (il != null) {
			        		miix = il.getX2();
			        		miiy = il.getY2();
			        		break;
			        	}
			        }
			        XYDrawableAnnotation mrkATHvsMIL = new XYDrawableAnnotation(miix,miiy,12,12,new CircleDrawer(Color.MAGENTA, new BasicStroke(1.0F), null));
			        rndrMi.addAnnotation(mrkATHvsMIL);
			        
			        // update actual label
		        	if (idx == 2) {
			        	lblMotIpAct3RL.setText(dotThree.format(miiy));
			        } else {
			        	lblMotIpAct41RL.setText(dotThree.format(miiy));
			        }
			        
			        actMIL = miiy;
			        
			        // 3.2 motor input cut by actual head high
			        ADvsAHLine = new CustLine(actDisH,0.0D,actDisH,srH.getMaxY()+10); // 10 just increases scope of base line, no effect for normal cases
			        miix = 0.0D;
			        miiy = 0.0D;
			        for(int i=0; i<srP.getItemCount()-1; i++) {
			        	il = findIntersection(ADvsAHLine, new CustLine(srP.getX(i).doubleValue(),srP.getY(i).doubleValue(),srP.getX(i+1).doubleValue(),srP.getY(i+1).doubleValue()),null, null);
			        	if (il != null) {
			        		miix = il.getX2();
			        		miiy = il.getY2();
			        		break;
			        	}
			        }
			        XYDrawableAnnotation mrkATHvsMIH = new XYDrawableAnnotation(miix,miiy,12,12,new CircleDrawer(Color.MAGENTA, new BasicStroke(1.0F), null));
			        rndrMi.addAnnotation(mrkATHvsMIH);
			        
			        // update actual label
		        	if (idx == 2) {
			        	lblMotIpAct3RH.setText(dotThree.format(miiy));
			        } else {
			        	lblMotIpAct41RH.setText(dotThree.format(miiy));
			        }
			        
			        actMIH = miiy;
		        }
		        
		        // 4.1 OA Eff cut by actual head low
		        Double eix = 0.0D;
		        Double eiy = 0.0D;
		        ADvsAHLine = new CustLine(actDisL,0.0D,actDisL,srH.getMaxY()+10); // 10 just increases scope of base line, no effect for normal cases
		        for(int i=0; i<srE.getItemCount()-1; i++) {
		        	il = findIntersection(ADvsAHLine, new CustLine(srE.getX(i).doubleValue(),srE.getY(i).doubleValue(),srE.getX(i+1).doubleValue(),srE.getY(i+1).doubleValue()),null, null);
		        	if (il != null) {
		        		eix = il.getX2();
		        		eiy = il.getY2();
		        		break;
		        	}
		        }
		        XYDrawableAnnotation mrkATHvsEL = new XYDrawableAnnotation(eix,eiy,12,12,new CircleDrawer(Color.MAGENTA, new BasicStroke(1.0F), null));
		        rndrE.addAnnotation(mrkATHvsEL);
		        
		        // update actual label
				if (idx == 2) {
		        	lblEffAct3RL.setText(dotTwo.format(eiy));
		        } else {
		        	lblEffAct41RL.setText(dotTwo.format(eiy));
		        } 
		        
		        actEL = eiy;
		        
		        // 4.2 OA Eff cut by actual head high
		        ADvsAHLine = new CustLine(actDisH,0.0D,actDisH,srH.getMaxY()+10); // 10 just increases scope of base line, no effect for normal cases
		        eix = 0.0D;
		        eiy = 0.0D;
		        for(int i=0; i<srE.getItemCount()-1; i++) {
		        	il = findIntersection(ADvsAHLine, new CustLine(srE.getX(i).doubleValue(),srE.getY(i).doubleValue(),srE.getX(i+1).doubleValue(),srE.getY(i+1).doubleValue()),null, null);
		        	if (il != null) {
		        		eix = il.getX2();
		        		eiy = il.getY2();
		        		break;
		        	}
		        }
		        XYDrawableAnnotation mrkATHvsEH = new XYDrawableAnnotation(eix,eiy,12,12,new CircleDrawer(Color.MAGENTA, new BasicStroke(1.0F), null));
		        rndrE.addAnnotation(mrkATHvsEH);
		        
		        // update actual label
				if (idx == 2) {
		        	lblEffAct3RH.setText(dotTwo.format(eiy));
		        } else {
		        	lblEffAct41RH.setText(dotTwo.format(eiy));
		        } 
		        
		        actEH = eiy;
	        } // graph view choice end
	        
	        // 4. max current between guaranteed head range (common for both kind of graph views)
	        // 4.1 low head range cut current
	        CustLine lowRangeLine;
	        gHdL = Double.valueOf(deriveHead(res2.getDouble("head_low")));
	        if (gHdL<srH.getMinY()) {
	        	lowRangeLine = new CustLine(0D,srH.getMinY(),srH.getMaxX(),srH.getMinY());
	        } else {
	        	lowRangeLine = new CustLine(0D,gHdL,srH.getMaxX(),gHdL);
	        }
	        
	        Double lhix = 0.0D;
	        Double lhiy = 0.0D;
	        for(int i=0; i<srH.getItemCount()-1; i++) {
	        	il = findIntersection(lowRangeLine, new CustLine(srH.getX(i).doubleValue(),srH.getY(i).doubleValue(),srH.getX(i+1).doubleValue(),srH.getY(i+1).doubleValue()),null, null);
	        	if (il != null) {
	        		lhix = il.getX2();
	        		lhiy = il.getY2();
	        		break;
	        	}
	        }
	        XYDrawableAnnotation mrkLHvsCur = new XYDrawableAnnotation(lhix,lhiy,12,12,new CircleDrawer(Color.red, new BasicStroke(1.0F), null));
	        rndrHd.addAnnotation(mrkLHvsCur);
	        
	        CustLine lowRangeCutLine = new CustLine(lhix,0.0D,lhix,srH.getMaxY());
	        Double lhcix = 0.0D;
	        Double lhciy = 0.0D;
	        int curIndex1 = 0;
	        for(int i=0; i<srC.getItemCount()-1; i++) {
	        	il = findIntersection(lowRangeCutLine, new CustLine(srC.getX(i).doubleValue(),srC.getY(i).doubleValue(),srC.getX(i+1).doubleValue(),srC.getY(i+1).doubleValue()),null, null);
	        	if (il != null) {
	        		lhcix = il.getX2();
	        		lhciy = il.getY2();
	        		curIndex1 = i+1;
	        		break;
	        	}
	        }
	        XYDrawableAnnotation mrkLHCutvsCur = new XYDrawableAnnotation(lhcix,lhciy,12,12,new CircleDrawer(Color.red, new BasicStroke(1.0F), null));
	        rndrCur.addAnnotation(mrkLHCutvsCur);
	        
	        
	        // 4.2 high head range cut current
	        gHdH = Double.valueOf(deriveHead(res2.getDouble("head_high")));
	        CustLine hiRangeLine = new CustLine(0D,gHdH,srH.getMaxX(),gHdH);
	        Double hhix = 0.0D;
	        Double hhiy = 0.0D;
	        for(int i=0; i<srH.getItemCount()-1; i++) {
	        	il = findIntersection(hiRangeLine, new CustLine(srH.getX(i).doubleValue(),srH.getY(i).doubleValue(),srH.getX(i+1).doubleValue(),srH.getY(i+1).doubleValue()),null, null);
	        	if (il != null) {
	        		hhix = il.getX2();
	        		hhiy = il.getY2();
	        		break;
	        	}
	        }
	        XYDrawableAnnotation mrkHHvsCur = new XYDrawableAnnotation(hhix,hhiy,12,12,new CircleDrawer(Color.red, new BasicStroke(1.0F), null));
	        rndrHd.addAnnotation(mrkHHvsCur);
	        
	        CustLine hiRangeCutLine = new CustLine(hhix,0.0D,hhix,srH.getMaxY());
	        Double hhcix = 0.0D;
	        Double hhciy = 0.0D;
	        int curIndex2 = 0;
	        for(int i=0; i<srC.getItemCount()-1; i++) {
	        	il = findIntersection(hiRangeCutLine, new CustLine(srC.getX(i).doubleValue(),srC.getY(i).doubleValue(),srC.getX(i+1).doubleValue(),srC.getY(i+1).doubleValue()),null, null);
	        	if (il != null) {
	        		hhcix = il.getX2();
	        		hhciy = il.getY2();
	        		curIndex2 = i+1;
	        		break;
	        	}
	        }
	        XYDrawableAnnotation mrkHHCutvsCur = new XYDrawableAnnotation(hhcix,hhciy,12,12,new CircleDrawer(Color.red, new BasicStroke(1.0F), null));
	        rndrCur.addAnnotation(mrkHHCutvsCur);
	        
	        // 4.3 find max current between low and high cuts
	        Double maxCur = 0.0D;
	        if(curIndex1 < curIndex2) {
	        	for(int i=curIndex1; i <= curIndex2; i++) {
	        		maxCur = Math.max(maxCur, srC.getY(i).doubleValue());
	        	}
	        } else {
	        	for(int i=curIndex1; i >= curIndex2; i--) {
	        		maxCur = Math.max(maxCur, srC.getY(i).doubleValue());
	        	}
	        }
	        
	        maxCur = Math.max(maxCur, Math.max(lhciy, hhciy)); // this line is enough to find max current, but above loops make sure to find max curr in case there is a peak betweek low & hi cut.
	        
	        // update actual label
	        if (idx == 2) {
	        	if (!isFlowRange) {
	        		lblCurAct3.setText(dotTwo.format(maxCur));
	        	} else {
	        		lblCurAct3R.setText(dotTwo.format(maxCur));
	        	}
	        } else {
	        	if (!isFlowRange) {
	        		lblCurAct41.setText(dotTwo.format(maxCur));
	        	} else {
	        		lblCurAct41R.setText(dotTwo.format(maxCur));
	        	}
	        	
	        }
	        actC = maxCur;

	        // other chart look & feel
	        
			chart.getLegend().setPosition(RectangleEdge.RIGHT);
			p.getDomainAxis().setLabelFont(p.getRangeAxis().getLabelFont());
			p.getDomainAxis().setTickLabelFont(p.getRangeAxis().getTickLabelFont());
			p.setDomainGridlinesVisible(true);
			p.setRangeGridlinesVisible(true);
			p.setBackgroundPaint(Color.green);
			p.setBackgroundAlpha(0.10F);
			p.setDomainGridlinePaint(Color.DARK_GRAY);
			p.setRangeGridlinePaint(Color.DARK_GRAY);
			
			p.setDomainMinorGridlinesVisible(true);
			p.setRangeMinorGridlinesVisible(true);
			p.setDomainMinorGridlinePaint(Color.LIGHT_GRAY);
			p.setRangeMinorGridlinePaint(Color.LIGHT_GRAY);
			
			p.getDomainAxis().setMinorTickMarksVisible(true);
			p.getDomainAxis().setMinorTickCount(10);
			p.getRangeAxis().setMinorTickMarksVisible(true);
			ra = 1;
			if(Configuration.LAST_USED_ISSTD.startsWith("IS 8472:") && Configuration.REP_SHOW_MI_FOR_8472.equals("1")) { // ISI handling
				p.getRangeAxis(ra++).setMinorTickMarksVisible(true);
			}
			p.getRangeAxis(ra++).setMinorTickMarksVisible(true);
			p.getRangeAxis(ra).setMinorTickMarksVisible(true);
			
			p.getRenderer(0).setSeriesToolTipGenerator(0,new XYToolTipGenerator() {
				@Override
				public String generateToolTip(XYDataset arg0, int arg1, int arg2) {
					return "(Discharge:"+ ((curDisUnit.endsWith("s") || curDisUnit.contains("sup"))?dotTwo.format(arg0.getXValue(arg1, arg2)):curDisUnit.endsWith("m")?dotOne.format(arg0.getXValue(arg1, arg2)):dotZero.format(arg0.getXValue(arg1, arg2))) + ", TH:" + deriveHead(Double.valueOf(String.valueOf(arg0.getYValue(arg1, arg2))), true) + ")";
				}
			});
			ra = 1;
			if(Configuration.LAST_USED_ISSTD.startsWith("IS 8472:") && Configuration.REP_SHOW_MI_FOR_8472.equals("1")) { // ISI handling
				p.getRenderer(ra).setSeriesToolTipGenerator(0,new XYToolTipGenerator() {
					@Override
					public String generateToolTip(XYDataset arg0, int arg1, int arg2) {
						return "(Discharge:"+ ((curDisUnit.endsWith("s") || curDisUnit.contains("sup"))?dotTwo.format(arg0.getXValue(arg1, arg2)):curDisUnit.endsWith("m")?dotOne.format(arg0.getXValue(arg1, arg2)):dotZero.format(arg0.getXValue(arg1, arg2))) + ", IP:" + dotThree.format(arg0.getYValue(arg1, arg2)) + ")";
					}
				});
				ra++;
			}
			p.getRenderer(ra).setSeriesToolTipGenerator(0,new XYToolTipGenerator() {
				@Override
				public String generateToolTip(XYDataset arg0, int arg1, int arg2) {
					return "(Discharge:"+ ((curDisUnit.endsWith("s") || curDisUnit.contains("sup"))?dotTwo.format(arg0.getXValue(arg1, arg2)):curDisUnit.endsWith("m")?dotOne.format(arg0.getXValue(arg1, arg2)):dotZero.format(arg0.getXValue(arg1, arg2))) + ", Eff.:" + dotTwo.format(arg0.getYValue(arg1, arg2)) + ")";
				}
			});
			ra++;
			p.getRenderer(ra).setSeriesToolTipGenerator(0,new XYToolTipGenerator() {
				@Override
				public String generateToolTip(XYDataset arg0, int arg1, int arg2) {
					return "(Discharge:"+ ((curDisUnit.endsWith("s")||curDisUnit.contains("sup"))?dotTwo.format(arg0.getXValue(arg1, arg2)):curDisUnit.endsWith("m")?dotOne.format(arg0.getXValue(arg1, arg2)):dotZero.format(arg0.getXValue(arg1, arg2))) + ", Cur.:" + dotTwo.format(arg0.getYValue(arg1, arg2)) + ")";
				}
			});
			
			// add chart
			ChartPanel chPanel = new ChartPanel(chart);
			chart.getTitle().setFont(new Font("Arial", Font.BOLD, 16));
			
			// mouse click event to show data point at given location
			/* chPanel.addChartMouseListener(new ChartMouseListener() {
				
				@Override
				public void chartMouseMoved(ChartMouseEvent event) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void chartMouseClicked(ChartMouseEvent event) {
					int x = event.getTrigger().getX();
					int y = event.getTrigger().getY();
					JOptionPane.showMessageDialog(new JFrame(), "I am clicked at x:" + event.getTrigger().getX() + " y:" + event.getTrigger().getY());
					JOptionPane.showMessageDialog(new JDialog(), "x:" + p.getRangeCrosshairValue() + " yval:" + p.getDomainCrosshairValue());
				}
			}); */
			
			// chart to swing panel
			pnlGraph.add(chPanel, new TableLayoutConstraints(0,0,0,0,TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			pnlGraph.revalidate();

			// 2. TEST RESULT
			if (res2.next()) {
				// general, ie common for both kind of graph views
				if (idx == 2) {
					lblPumpType3.setText(" " + res2.getString("type"));
					lblPSno3.setText(" " + pumpNo);
					lblHdR3.setText("HEAD RANGE (" + curHdUnit + ")");
					lblHeadR3.setText(" " + deriveHead(res2.getDouble("head_low")) + " / " + deriveHead(res2.getDouble("head_high")));
					if (isRatedSpeed) {
						lbllblFreq3.setText(" SPEED (rpm)");
						lblFreq3.setText(" " + res2.getString("speed"));
					} else {
						lbllblFreq3.setText(" FREQUENCY (Hz)");
						lblFreq3.setText(" " + res2.getString("freq"));
					}
					lblDt3.setText(" " + testDt);
					// ISI handling
					if (Configuration.LAST_USED_ISSTD.startsWith("IS 8034:") || Configuration.LAST_USED_ISSTD.startsWith("IS 14220:")) {
						lblPSz3.setText(" " + res2.getString("delivery_size"));
					} else {
						lblPSz3.setText(" " + res2.getString("suction_size") + " X " + res2.getString("delivery_size"));
					}
					
				} else {
					lblPumpType41_2.setText(" " + res2.getString("type"));
					lblPSno41_2.setText(" " + pumpNo);
					lblHdR41_2.setText("HEAD RANGE (" + curHdUnit + ")");
					lblHeadR41_2.setText(" " + deriveHead(res2.getDouble("head_low")) + " / " + deriveHead(res2.getDouble("head_high")));
					if (isRatedSpeed) {
						lbllblFreq41_2.setText(" SPEED (rpm)");
						lblFreq41_2.setText(" " + res2.getString("speed"));
					} else {
						lbllblFreq41_2.setText(" FREQUENCY (Hz)");
						lblFreq41_2.setText(" " + res2.getString("freq"));
					}
					lblDt41_2.setText(" " + testDt);
					// ISI handling
					if (Configuration.LAST_USED_ISSTD.startsWith("IS 8034:") || Configuration.LAST_USED_ISSTD.startsWith("IS 14220:")) {
						lblPSz41_2.setText(" " + res2.getString("delivery_size"));
					}else {
						lblPSz41_2.setText(" " + res2.getString("suction_size") + " X " + res2.getString("delivery_size"));
					}
					
				}

				// derive results and apply based on flow range view or just normal view
				if (!isFlowRange) { // default graph view
					String flowRes = "";
					String headRes = "";
					String motIpRes = "";
					String effRes = "";
					String curRes = "";
					
					flowRes = ((actDis > (gDis*tolDisMax)) || (actDis < (gDis*tolDisMin))) ? "Fail" : "Pass";
					headRes = ((actTH > (gHd*tolHeadMax)) || (actTH < (gHd*tolHeadMin))) ? "Fail" : "Pass";
					if (Configuration.LAST_USED_ISSTD.startsWith("IS 8472:")) {
						motIpRes = ((actMI > (res2.getDouble("mot_ip")*tolPowMax)) || (actMI < (res2.getDouble("mot_ip")*tolPowMin))) ? "Fail" : "Pass"; 
					}
					curRes = ((actC > (res2.getDouble("amps")*tolCurMax)) || (actC < (res2.getDouble("amps")*tolCurMin))) ? "Fail" : "Pass";
					effRes = ((actE > (res2.getDouble("overall_eff")*tolEffMax)) || (actE < (res2.getDouble("overall_eff")*tolEffMin))) ? "Fail" : "Pass";
					
					if (idx == 2) {
						// guaranteed
						if (!Configuration.LAST_USED_ISSTD.startsWith("IS 12225")) {
							lblFlowUnit3.setText("<html>Q (" + curDisUnit + ")</html>"); //set in isi handling for 12225
						}
						lblFlow3.setText(deriveFlow(res2.getDouble("discharge")));
						lblHd3.setText("TH (" + curHdUnit + ")");
						lblHead3.setText(deriveHead(res2.getDouble("head")));
						if(Configuration.LAST_USED_ISSTD.startsWith("IS 8472:") && Configuration.REP_SHOW_MI_FOR_8472.equals("1")) { // ISI handling
							lblMotIp3.setText(res2.getString("mot_ip"));
							// pass/fail label
							lblMotIpRes3.setText(motIpRes);
						}
						lblEff3.setText(res2.getString("overall_eff"));
						lblCur3.setText(res2.getString("amps"));
						
						// actual
						// updated above whenever they are calculated while populating the chart

						// pass/fail labels
						lblFlowRes3.setText(flowRes);
						lblHeadRes3.setText(headRes);
						lblEffRes3.setText(effRes);
						lblCurRes3.setText(curRes);
						
					} else {
						// guaranteed
						if (!Configuration.LAST_USED_ISSTD.startsWith("IS 12225")) {
							lblFlowUnit41.setText("<html>Q (" + curDisUnit + ")</html>"); // set in isi handling for 12225
						}
						lblFlow41_2.setText(deriveFlow(res2.getDouble("discharge")));
						lblHd41.setText("TH (" + curHdUnit + ")");
						lblHead41_2.setText(deriveHead(res2.getDouble("head")));
						if(Configuration.LAST_USED_ISSTD.startsWith("IS 8472:") && Configuration.REP_SHOW_MI_FOR_8472.equals("1")) { // ISI handling
							lblMotIp41_2.setText(res2.getString("mot_ip"));
							// pass/fail label
							lblMotIpRes41.setText(motIpRes);
						}
						lblEff41_2.setText(res2.getString("overall_eff"));
						lblCur41_2.setText(res2.getString("amps"));
						
						// actual
						// updated above whenever they are calculated while populating the chart
						
						// pass/fail labels
						lblFlowRes41.setText(flowRes);
						lblHeadRes41.setText(headRes);
						lblEffRes41.setText(effRes);
						lblCurRes41.setText(curRes);
					}
					
				} else { // graph view - flow range
					
					String flowResL = "";
					String headResL = "";
					String motIpResL = "";
					String effResL = "";
					
					flowResL = ((actDisL > (gDisL*tolDisMax)) || (actDisL < (gDisL*tolDisMin))) ? "Fail" : "Pass";
					headResL = ((actTHL > (gHdL*tolHeadMax)) || (actTHL < (gHdL*tolHeadMin))) ? "Fail" : "Pass";
					if (Configuration.LAST_USED_ISSTD.startsWith("IS 8472:")) {
						motIpResL = ((actMIL > (res2.getDouble("mot_ip")*tolPowMax)) || (actMIL < (res2.getDouble("mot_ip")*tolPowMin))) ? "Fail" : "Pass"; 
					}
					effResL = ((actEL > (res2.getDouble("overall_eff")*tolEffMax)) || (actEL < (res2.getDouble("overall_eff")*tolEffMin))) ? "Fail" : "Pass";
					
					String flowResH = "";
					String headResH = "";
					String motIpResH = "";
					String effResH = "";
					String curRes =  "";
					
					flowResH = ((actDisH > (gDisH*tolDisMax)) || (actDisH < (gDisH*tolDisMin))) ? "Fail" : "Pass";
					headResH = ((actTHH > (gHdH*tolHeadMax)) || (actTHH < (gHdH*tolHeadMin))) ? "Fail" : "Pass";
					if (Configuration.LAST_USED_ISSTD.startsWith("IS 8472:")) {
						motIpResH = ((actMIH > (res2.getDouble("mot_ip")*tolPowMax)) || (actMIH < (res2.getDouble("mot_ip")*tolPowMin))) ? "Fail" : "Pass"; 
					}
					effResH = ((actEH > (res2.getDouble("overall_eff")*tolEffMax)) || (actEH < (res2.getDouble("overall_eff")*tolEffMin))) ? "Fail" : "Pass";
					curRes = ((actC > (res2.getDouble("amps")*tolCurMax)) || (actC < (res2.getDouble("amps")*tolCurMin))) ? "Fail" : "Pass";
					
					if (idx == 2) {
						// guaranteed
						if (!Configuration.LAST_USED_ISSTD.startsWith("IS 12225")) {
							lblFlowUnit3RL.setText("<html>Q (" + curDisUnit + ")</html>"); // set in isi handling for 12225
						}
						
						lblFlow3RL.setText(deriveFlow(res2.getDouble("discharge_low")));
						lblHd3RL.setText("TH (" + curHdUnit + ")");
						lblHead3RL.setText(deriveHead(res2.getDouble("head_low")));
						
						if (!Configuration.LAST_USED_ISSTD.startsWith("IS 12225")) {
							lblFlowUnit3RH.setText("<html>Q (" + curDisUnit + ")</html>"); // set in isi handling for 12225
						}
						
						lblFlow3RH.setText(deriveFlow(res2.getDouble("discharge_high")));
						lblHdRH.setText("TH (" + curHdUnit + ")");
						lblHead3RH.setText(deriveHead(res2.getDouble("head_high")));
						
						if(Configuration.LAST_USED_ISSTD.startsWith("IS 8472:") && Configuration.REP_SHOW_MI_FOR_8472.equals("1")) { // ISI handling
							lblMotIp3RL.setText(res2.getString("mot_ip"));
							lblMotIp3RH.setText(res2.getString("mot_ip"));
							// pass/fail labels
							lblMotIpRes3RL.setText(motIpResL);
							lblMotIpRes3RH.setText(motIpResH);
						}
						lblEff3RL.setText(res2.getString("overall_eff"));
						lblEff3RH.setText(res2.getString("overall_eff"));
						
						lblCur3R.setText(res2.getString("amps"));
						
						// actual
						// updated above whenever they are calculated while populating the chart
						
						// pass/fail labels
						lblFlowRes3RL.setText(flowResL);
						lblHeadRes3RL.setText(headResL);
						lblEffRes3RL.setText(effResL);
						lblFlowRes3RH.setText(flowResH);
						lblHeadRes3RH.setText(headResH);
						lblEffRes3RH.setText(effResH);
						
						lblCurRes3R.setText(curRes);
						
					} else {
						// guaranteed
						if (!Configuration.LAST_USED_ISSTD.startsWith("IS 12225")) {
							lblFlowUnit41RL.setText("<html>Q (" + curDisUnit + ")</html>"); //set in isi handling for 12225
						}
						lblFlow41RL.setText(deriveFlow(res2.getDouble("discharge_low")));
						lblHd41RL.setText("TH (" + curHdUnit + ")");
						lblHead41RL.setText(deriveHead(res2.getDouble("head_low")));
						
						if (!Configuration.LAST_USED_ISSTD.startsWith("IS 12225")) {
							lblFlowUnit41RH.setText("<html>Q (" + curDisUnit + ")</html>"); //set in isi handling for 12225
						}
						lblFlow41RH.setText(deriveFlow(res2.getDouble("discharge_high")));
						lblHd41RH.setText("TH (" + curHdUnit + ")");
						lblHead41RH.setText(deriveHead(res2.getDouble("head_high")));
						
						if(Configuration.LAST_USED_ISSTD.startsWith("IS 8472:") && Configuration.REP_SHOW_MI_FOR_8472.equals("1")) { // ISI handling
							lblMotIp41RL.setText(res2.getString("mot_ip"));
							lblMotIp41RH.setText(res2.getString("mot_ip"));
							// pass/fail labels
							lblMotIpRes41RL.setText(motIpResL);
							lblMotIpRes41RH.setText(motIpResH);
						}
						lblEff41RL.setText(res2.getString("overall_eff"));
						lblEff41RH.setText(res2.getString("overall_eff"));
						
						lblCur41R.setText(res2.getString("amps"));
						
						// actual
						// updated above whenever they are calculated while populating the chart
						
						// pass/fail labels
						lblFlowRes41RL.setText(flowResL);
						lblHeadRes41RL.setText(headResL);
						lblEffRes41RL.setText(effResL);
						lblFlowRes41RH.setText(flowResH);
						lblHeadRes41RH.setText(headResH);
						lblEffRes41RH.setText(effResH);
						
						lblCurRes41R.setText(curRes);
					} 
				} // flow range
			} // res 2
			// footer
			if (Configuration.REP_SHOW_TESTER_NAME.equals("1")) {
				lblUser.setText("Tested By [" + testUser.toUpperCase() + "]"); 
			}
		} // res 1
		pnlGraph.revalidate();
		setPanelTitle(pumpNo, tmpRtVolt);
	}
	
	// function to find and return the intersection of two lines
	private CustLine findIntersection(CustLine line1, CustLine line2, Double m1, Double m2)
	{
		Double dx;
		Double dy;
		Double ix;
		Double iy;
		Double c1 = 0.0D;
		Double c2 = 0.0D;
		Boolean l1_vertical = false;
		
		dx = line1.getX2() - line1.getX1();
		dy = line1.getY2() - line1.getY1();
		
		// formula of line => y = mx + c
		if (m1 == null) {
			if (dx == 0) { // vertical line 1
				m1 = 0.0D;
				c1 = 0.0D;
				l1_vertical = true;
			} else {
				m1 = dy/dx;
				c1 = line1.getY1() - m1 * line1.getX1();
			}
		}
		
		
		dx = line2.getX2() - line2.getX1();
		dy = line2.getY2() - line2.getY1();
		if(m2 == null) {
			m2 = dy/dx;
		}
		c2 = line2.getY1() - m2 * line2.getX1(); 
		
		
		if((m1 - m2) == 0)
			return null;
		else
		{
			if (!l1_vertical) {
				ix = (c2 - c1) / (m1 - m2);
				iy = m1 * ix + c1;
			} else { // vertical line => x is constant, y is derived using the m & c of other line 
				ix = line1.getX1();
				iy = m2 * ix + c2;
			}
		}
		
		
		// bound check
		if(isPointInBox(line1.getX1(),line1.getY1(),line1.getX2(),line1.getY2(),ix,iy) && isPointInBox(line2.getX1(),line2.getY1(),line2.getX2(), line2.getY2(),ix,iy))
		{
			CustLine il = new CustLine(0.0D,0.0D,ix,iy);
			return il;
		} else {
			return null;
		}
	}

	private boolean isPointInBox(Double x1, Double y1, Double x2, Double y2, Double ix, Double iy)
	{
		Double left, top, right, bottom;
		
		if(x1 < x2) {
			left = x1;
			right = x2;
		} else {
			left = x2;
			right = x1;
		}
	
		if(y1 < y2) {
			top = y1;
			bottom = y2;
		} else {
			top = y2;
			bottom = y1;
		}
	
		if((ix+0.01) >= left && (ix-0.01) <= right && (iy+0.01) >= top && (iy-0.01) <= bottom ) {
			return true; 
		} else { 
			return false;
		}
	}

	// class to hold 4 points of a line
	private class CustLine {
		private Double x1=0.0D;
		private Double y1=0.0D;
		private Double x2=0.0D;
		private Double y2=0.0D;
		
		CustLine() {
			
		}
		CustLine(Double x1, Double y1, Double x2, Double y2) {
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
		}
		public Double getX1() {
			return x1;
		}
		public Double getY1() {
			return y1;
		}
		public Double getX2() {
			return x2;
		}
		public Double getY2() {
			return y2;
		}
	}

	class MyTableCellRender extends DefaultTableCellRenderer {  
		public Component getTableCellRendererComponent(  
				JTable table, Object value, boolean isSelected, 
				boolean hasFocus, int row, int col) {
				     super.getTableCellRendererComponent(
				                      table,  value, isSelected, hasFocus, row, col);
				     // ISI handling
				     if(Configuration.LAST_USED_ISSTD.startsWith("IS 8034:")) { 
					     if (col >= 10 && col < table.getColumnCount()-1) {
						    	 setForeground(clrActual); 
						 } else {
							 setForeground(Color.black);
						 }
				     } else { // 8472,14220,9079
				    	 if (col >= 11 && col < table.getColumnCount()-1) {
						     setForeground(clrActual); 
						 } else {
							 setForeground(Color.black);
						 } 
				     }
			return this;
			}
	}
	
	// print progress pop up
	class PrintProgressDlg {
		
		JDialog dlgPrint;
		boolean cancelPrint = false;
		JLabel lblMsg;
		JButton cmdCancel;
		Timer tmrPrint;
		
		PrintProgressDlg() {
			
			dlgPrint = new JDialog();
			lblMsg = new JLabel("");
			cmdCancel = new JButton("Cancel");
			
			dlgPrint.setTitle("Print");
			dlgPrint.setModal(true);
			dlgPrint.setResizable(false);
			dlgPrint.setLocationRelativeTo(null);
			dlgPrint.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			
			JPanel pnlMsg = new JPanel(new GridLayout(2,1,5,5));
			pnlMsg.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
			
			cmdCancel.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					cmdCancelActionPerformed();
				}
			});
			
			pnlMsg.add(lblMsg);
			pnlMsg.add(cmdCancel);
	
			dlgPrint.setContentPane(pnlMsg);
	
			tmrPrint = new Timer(1000, new AbstractAction() {
			    @Override
			    public void actionPerformed(ActionEvent ae) {
			        dlgPrint.dispose();
			    }
			});
			tmrPrint.setRepeats(false);
		}
		
		public void showMessage(String msg) {
			cancelPrint = false;
			lblMsg.setText(msg);
			dlgPrint.pack();
			tmrPrint.start();
			dlgPrint.setVisible(true);
		}
		
		public boolean getCancelPrint() {
			return cancelPrint;
		}
		
		private void cmdCancelActionPerformed() {
			if (JOptionPane.showConfirmDialog(null, "Do you want to cancel printing?", "Print", JOptionPane.YES_NO_OPTION) == 0) {
				cancelPrint = true;
			}
		}
	}
	// custom code - end

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel pnlOpt;
	private JPanel pnlFilter;
	private JScrollPane scrlDev;
	private JTable tblTest;
	private JButton cmdRefresh;
	private JButton cmdSel;
	private JButton cmdClear;
	private JLabel lblResult;
	private JMenuBar menuBar1;
	private JMenu menFilter;
	private JRadioButtonMenuItem optRt;
	private JRadioButtonMenuItem optType;
	private JRadioButtonMenuItem optBoth;
	private JCheckBoxMenuItem chkRtV;
	private JPanel pnlAdFilter;
	private JLabel label11;
	private JLabel lblNonISIFilter;
	private JComboBox cmbType;
	private JLabel label12;
	private JLabel label13;
	private JDateChooser fromDt;
	private JLabel label14;
	private JDateChooser toDt;
	private JLabel label15;
	private JLabel label16;
	private JTextField txtSlNoFrom;
	private JLabel label17;
	private JTextField txtSlNoTo;
	private JLabel label5;
	private JComboBox cmbRemarks;
	private JLabel label18;
	private JComboBox cmbLine;
	private JPanel pnlViewOpt;
	private JLabel label76;
	private JRadioButton optRndrLine;
	private JRadioButton optRndrSm;
	private JLabel label71;
	private JComboBox cmbHdUnit;
	private JLabel lblHdUnit;
	private JLabel label66;
	private JComboBox cmbDisUnit;
	private JLabel lblDisUnit;
	private JLabel label70;
	private JRadioButton optSpeed;
	private JRadioButton optFreq;
	private JCheckBox chkDisR;
	private JCheckBox chkCat;
	private JButton cmdSet;
	private JButton cmdExp;
	private JButton cmdPrint;
	private JButton cmdPrintAll;
	private JButton cmdClose;
	private JPanel printContainer;
	private JPanel pnlBut;
	private JButton cmdFirst;
	private JButton cmdPrev;
	private JButton cmdNext;
	private JButton cmdLast;
	private JTabbedPane tabReport;
	private JScrollPane scrollPane1;
	private JPanel printArea;
	private JPanel pnlPrint;
	private JLabel lblIS;
	private JLabel lblCustLogo;
	private JLabel lblCompName;
	private JLabel lblISILogo;
	private JLabel lblCompAdr;
	private JLabel lblTitle;
	private JLabel lblISIRefNo;
	private JLabel label6;
	private JLabel lblPumpType;
	private JLabel lblPage;
	private JPanel pnlPrime;
	private JLabel label122;
	private JLabel label123;
	private JLabel label228;
	private JLabel label2;
	private JLabel label20;
	private JLabel label8;
	private JLabel label9;
	private JLabel label126;
	private JLabel label1;
	private JLabel label155;
	private JLabel label129;
	private JLabel label130;
	private JLabel label131;
	private JLabel label132;
	private JLabel label128;
	private JLabel label154;
	private JLabel label159;
	private JLabel lblSHd1;
	private JLabel lblDHd1;
	private JLabel lblDis1;
	private JLabel label139;
	private JLabel label140;
	private JLabel label134;
	private JLabel label161;
	private JTable tblRes;
	private JPanel pnlBot;
	private JLabel lblTestedBy;
	private JLabel label102;
	private JLabel lblVerBy14;
	private JLabel lblAppVer;
	private JScrollPane scrollPane2;
	private JPanel printArea2;
	private JPanel pnlPrint2;
	private JLabel lblIS2;
	private JLabel lblCustLogo2;
	private JLabel lblCompName2;
	private JLabel lblISILogo2;
	private JLabel lblCompAdr2;
	private JLabel lblTitle2;
	private JLabel lblISIRefNo2;
	private JLabel lblPage2;
	private JPanel pnlRep2;
	private JPanel pnlTop2;
	private JLabel label10;
	private JLabel lblPumpType2;
	private JLabel label32;
	private JLabel lblMRt2;
	private JLabel label26;
	private JLabel lblFreq2;
	private JLabel lbllblPSize2;
	private JLabel lblPSz2;
	private JLabel label42;
	private JLabel lblPSno2;
	private JLabel label36;
	private JLabel lblV2;
	private JLabel label194;
	private JLabel lblSpeed2;
	private JLabel lblHdLbl2;
	private JLabel lblHead2;
	private JLabel label34;
	private JLabel lblPh2;
	private JLabel label38;
	private JLabel lblCur2;
	private JLabel label203;
	private JLabel lblPoles2;
	private JLabel lblFlowRtLabel;
	private JLabel lblFlow2;
	private JLabel label51;
	private JLabel lblCon2;
	private JLabel lbllblMI2;
	private JLabel lblMI2;
	private JLabel lbllblGDis2;
	private JLabel lblGDis2;
	private JLabel lblHdRgLbl2;
	private JLabel lblHeadR2;
	private JLabel lbllblMotType2;
	private JLabel lblMotType2;
	private JLabel lbllblMSno2;
	private JLabel lblMSno2;
	private JLabel lbllblMotEff2;
	private JLabel lblMotEff2;
	private JLabel lbllblOE2;
	private JLabel lblOEff2;
	private JLabel lbllblIns2;
	private JLabel lblIns2;
	private JLabel lbllblPipeConst2;
	private JLabel lblPipeConst2;
	private JLabel lbllblSPTime2;
	private JLabel lblSPTime2;
	private JLabel lbllblBrSz2;
	private JLabel lblBrSz2;
	private JLabel lbllblCapRat2;
	private JLabel lblCapRate2;
	private JLabel lbllblCapVolt2;
	private JLabel lblCapVolt2;
	private JLabel lbllblNoOfStg2;
	private JLabel lblNoStg2;
	private JPanel pnlPrime2;
	private JLabel label143;
	private JLabel label144;
	private JLabel label127;
	private JLabel lbllblHead1;
	private JLabel label133;
	private JLabel label135;
	private JLabel lblTitSHd2;
	private JLabel lblRem2;
	private JLabel lblPerfHz2;
	private JLabel label335;
	private JLabel label336;
	private JLabel lblTitVHC2;
	private JLabel lblTitTotHd2;
	private JLabel label156;
	private JLabel label136;
	private JLabel label141;
	private JLabel label142;
	private JLabel lblTitFlow2;
	private JLabel lblTitTotRtHd2;
	private JLabel lblTitMI2;
	private JLabel label173;
	private JLabel label153;
	private JLabel lblSHd2;
	private JLabel lblDHd2;
	private JLabel lblVHd2;
	private JLabel lblTotHd2;
	private JLabel lblFlowUnitS2;
	private JLabel label145;
	private JLabel label146;
	private JLabel label147;
	private JLabel lblFlowUnitT2;
	private JLabel lblTotHdT2;
	private JLabel lblMITitUnit2;
	private JTable tblRes2;
	private JPanel pnlBot2;
	private JLabel lblCas2;
	private JLabel lblGC2;
	private JLabel lblMaxDis2;
	private JLabel lblGCText2;
	private JLabel lblWith2;
	private JLabel lblDt2;
	private JLabel lblTestedBy2;
	private JLabel lblVerBy13;
	private JLabel label23;
	private JLabel lbllblDt2;
	private JPanel pnlOptTest2;
	private JLabel lbllblSP2;
	private JLabel lblSP2;
	private JLabel lblAppVer2;
	private JScrollPane scrollPane3;
	private JPanel printArea3;
	private JLabel lblRtWarn3;
	private JPanel pnlPrint3;
	private JLabel lblIS3;
	private JLabel lblCustLogo3;
	private JLabel lblCompName3;
	private JLabel lblISILogo3;
	private JLabel lblCompAdr3;
	private JLabel lblTitle3;
	private JLabel lblISIRefNo3;
	private JLabel lblPage3;
	private JPanel pnlGraph3;
	private JPanel pnlBot3;
	private JLabel label33;
	private JLabel lblPumpType3;
	private JLabel label61;
	private JLabel lblPSno3;
	private JLabel label62;
	private JLabel lblDt3;
	private JLabel lblHdR3;
	private JLabel lblHeadR3;
	private JLabel lbllblPSize3;
	private JLabel lblPSz3;
	private JLabel lbllblFreq3;
	private JLabel lblFreq3;
	private JPanel pnlBotRes3;
	private JLabel label49;
	private JLabel label57;
	private JLabel label58;
	private JLabel label59;
	private JLabel lblFlowUnit3;
	private JLabel lblFlow3;
	private JLabel lblFlowAct3;
	private JLabel lblFlowRes3;
	private JLabel lblHd3;
	private JLabel lblHead3;
	private JLabel lblHeadAct3;
	private JLabel lblHeadRes3;
	private JLabel lbllblMI3;
	private JLabel lbllblEff3;
	private JLabel lblMotIp3;
	private JLabel lblMotIpAct3;
	private JLabel lblMotIpRes3;
	private JLabel lbllblMaxC3;
	private JLabel lblEff3;
	private JLabel lblCur3;
	private JLabel lblEffAct3;
	private JLabel lblCurAct3;
	private JLabel lblEffRes3;
	private JLabel lblCurRes3;
	private JPanel pnlBotRes3R;
	private JLabel lblFlowUnit3RL2;
	private JLabel lblFlowUnit3RL3;
	private JLabel label227;
	private JLabel lblFlowUnit3RL;
	private JLabel lblHd3RL;
	private JLabel lblFlowUnit3RH;
	private JLabel lblHdRH;
	private JLabel label232;
	private JLabel label233;
	private JLabel label234;
	private JLabel label237;
	private JLabel lbllblEff3R1;
	private JLabel lbllblMI3R1;
	private JLabel lbllblEff3R2;
	private JLabel lbllblMI3R2;
	private JLabel lblFlow3RL;
	private JLabel lblHead3RL;
	private JLabel lblEff3RL;
	private JLabel lblMotIp3RL;
	private JLabel lblFlow3RH;
	private JLabel lblHead3RH;
	private JLabel lblEff3RH;
	private JLabel lblMotIp3RH;
	private JLabel lblCur3R;
	private JLabel lblFlowAct3RL;
	private JLabel lblHeadAct3RL;
	private JLabel lblEffAct3RL;
	private JLabel lblMotIpAct3RL;
	private JLabel lblFlowAct3RH;
	private JLabel lblHeadAct3RH;
	private JLabel lblEffAct3RH;
	private JLabel lblMotIpAct3RH;
	private JLabel lblCurAct3R;
	private JLabel lblFlowRes3RL;
	private JLabel lblHeadRes3RL;
	private JLabel lblEffRes3RL;
	private JLabel lblMotIpRes3RL;
	private JLabel lblFlowRes3RH;
	private JLabel lblHeadRes3RH;
	private JLabel lblEffRes3RH;
	private JLabel lblMotIpRes3RH;
	private JLabel lblCurRes3R;
	private JPanel pnlBotBot3;
	private JLabel lblTestedBy3;
	private JLabel label103;
	private JLabel lblVerBy12;
	private JLabel lblAppVer3;
	private JPanel pnlRep4;
	private JTabbedPane tabISI;
	private JScrollPane scrollPane41;
	private JPanel printArea41;
	private JLabel lblRtWarn41;
	private JPanel pnlPrint41;
	private JLabel lblCustLogo41;
	private JLabel lblCompName41;
	private JLabel lblIS41;
	private JLabel lblISILogo41;
	private JLabel lblCompAdr41;
	private JLabel lblTitle41;
	private JLabel lblISIRefNo41;
	private JLabel lblPage41;
	private JPanel pnlTop41;
	private JLabel label25;
	private JLabel lblPumpType41;
	private JLabel label37;
	private JLabel lblMRt41;
	private JLabel label52;
	private JLabel lblFreq41;
	private JLabel lbllblPSize41;
	private JLabel lblPSz41;
	private JLabel label43;
	private JLabel lblPSno41;
	private JLabel label50;
	private JLabel lblV41;
	private JLabel label224;
	private JLabel lblSpeed41;
	private JLabel lblHdLbl41;
	private JLabel lblHead41;
	private JLabel label39;
	private JLabel lblPh41;
	private JLabel label53;
	private JLabel lblCur41;
	private JLabel label204;
	private JLabel lblPoles41;
	private JLabel lblFlowRtLabel41;
	private JLabel lblFlow41;
	private JLabel label160;
	private JLabel lblCon41;
	private JLabel lbllblTopMI41;
	private JLabel lblMI41;
	private JLabel lbllblGDis41;
	private JLabel lblGDis41;
	private JLabel lblHdRglbl41;
	private JLabel lblHeadR41;
	private JLabel lbllblMotType41;
	private JLabel lblMotType41;
	private JLabel lbllblMSno41;
	private JLabel lblMSno41;
	private JLabel lbllblMotEff41;
	private JLabel lblMotEff41;
	private JLabel lbllblOE41;
	private JLabel lblOEff41;
	private JLabel lbllblIns41;
	private JLabel lblIns41;
	private JLabel lbllblPipeConst41;
	private JLabel lblPipeConst41;
	private JLabel lbllblSPTime41;
	private JLabel lblSPTime41;
	private JLabel lbllblBrSz41;
	private JLabel lblBrSz41;
	private JLabel lbllblCapRat41;
	private JLabel lblCapRate41;
	private JLabel lbllblCapVolt41;
	private JLabel lblCapVolt41;
	private JLabel lbllblNoStg41;
	private JLabel lblNoStg41;
	private JPanel pnlPrime41;
	private JLabel label149;
	private JLabel label150;
	private JLabel label225;
	private JLabel label56;
	private JLabel label151;
	private JLabel label152;
	private JLabel lblTitSHd41;
	private JLabel lblRem41;
	private JLabel lblPerfHz41;
	private JLabel label338;
	private JLabel label339;
	private JLabel lblTitVHC41;
	private JLabel lblTitTotHd41;
	private JLabel label157;
	private JLabel label158;
	private JLabel label162;
	private JLabel label164;
	private JLabel lblTitFlow41;
	private JLabel label178;
	private JLabel lblTitMI41;
	private JLabel label180;
	private JLabel label226;
	private JLabel lblSHd41;
	private JLabel lblDHd41;
	private JLabel lblVHd41;
	private JLabel lblTotHd41;
	private JLabel lblFlowUnitS41;
	private JLabel label166;
	private JLabel label167;
	private JLabel label168;
	private JLabel lblFlowUnitT41;
	private JLabel lblTotHdT41;
	private JLabel lblMITitUnit41;
	private JTable tblRes41;
	private JPanel pnlGraph41;
	private JPanel pnlBot41;
	private JLabel lblCas41;
	private JLabel lblGC41;
	private JLabel lblMaxDis41;
	private JLabel lblGCText41;
	private JLabel lblWith41;
	private JLabel lblVerBy10;
	private JLabel lblDt41;
	private JLabel lblTestedBy41;
	private JLabel lblVerBy11;
	private JLabel label67;
	private JLabel lbllblDt41;
	private JPanel pnlBotTwo41;
	private JLabel label68;
	private JLabel lblPumpType41_2;
	private JLabel label73;
	private JLabel lblPSno41_2;
	private JLabel label74;
	private JLabel lblDt41_2;
	private JLabel lblHdR41_2;
	private JLabel lblHeadR41_2;
	private JLabel lbllblPSize41_2;
	private JLabel lblPSz41_2;
	private JLabel lbllblFreq41_2;
	private JLabel lblFreq41_2;
	private JPanel pnlBotRes41;
	private JLabel label69;
	private JLabel lblFlowUnit41;
	private JLabel lblHd41;
	private JLabel lbllblMI41;
	private JLabel lbllblEff41;
	private JLabel lbllblMaxC41;
	private JLabel label75;
	private JLabel lblFlow41_2;
	private JLabel lblHead41_2;
	private JLabel lblMotIp41_2;
	private JLabel lblEff41_2;
	private JLabel lblCur41_2;
	private JLabel label78;
	private JLabel lblFlowAct41;
	private JLabel lblHeadAct41;
	private JLabel lblMotIpAct41;
	private JLabel lblEffAct41;
	private JLabel lblCurAct41;
	private JLabel label80;
	private JLabel lblFlowRes41;
	private JLabel lblHeadRes41;
	private JLabel lblMotIpRes41;
	private JLabel lblEffRes41;
	private JLabel lblCurRes41;
	private JPanel pnlBotRes41R;
	private JLabel lblLH41;
	private JLabel lblHH41;
	private JLabel label235;
	private JLabel lblFlowUnit41RL;
	private JLabel lblHd41RL;
	private JLabel lblFlowUnit41RH;
	private JLabel lblHd41RH;
	private JLabel label244;
	private JLabel label245;
	private JLabel label246;
	private JLabel label248;
	private JLabel lbllblEff41R1;
	private JLabel lbllblMI41R1;
	private JLabel lbllblEff41R2;
	private JLabel lbllblMI41R2;
	private JLabel lblFlow41RL;
	private JLabel lblHead41RL;
	private JLabel lblEff41RL;
	private JLabel lblMotIp41RL;
	private JLabel lblFlow41RH;
	private JLabel lblHead41RH;
	private JLabel lblEff41RH;
	private JLabel lblMotIp41RH;
	private JLabel lblCur41R;
	private JLabel lblFlowAct41RL;
	private JLabel lblHeadAct41RL;
	private JLabel lblEffAct41RL;
	private JLabel lblMotIpAct41RL;
	private JLabel lblFlowAct41RH;
	private JLabel lblHeadAct41RH;
	private JLabel lblEffAct41RH;
	private JLabel lblMotIpAct41RH;
	private JLabel lblCurAct41R;
	private JLabel lblFlowRes41RL;
	private JLabel lblHeadRes41RL;
	private JLabel lblEffRes41RL;
	private JLabel lblMotIpRes41RL;
	private JLabel lblFlowRes41RH;
	private JLabel lblHeadRes41RH;
	private JLabel lblEffRes41RH;
	private JLabel lblMotIpRes41RH;
	private JLabel lblCurRes41R;
	private JPanel pnlOptTest41;
	private JLabel lblCas42;
	private JLabel lblSP42;
	private JLabel lblAppVer41;
	private JScrollPane scrollPane42;
	private JPanel printArea42;
	private JPanel pnlPrint42;
	private JLabel lblCustLogo42;
	private JLabel lblCompName42;
	private JLabel lblIS42;
	private JLabel lblISILogo42;
	private JLabel lblCompAdr42;
	private JLabel lblTitle42;
	private JLabel lblISIRefNo42;
	private JLabel lblPage42;
	private JPanel pnlTop42;
	private JLabel label60;
	private JLabel lblPumpType42;
	private JLabel label86;
	private JLabel lblDt42;
	private JPanel pnlPrime42;
	private JLabel label186;
	private JLabel label187;
	private JLabel lblTypeCount42;
	private JLabel label342;
	private JLabel lblDis42;
	private JLabel lblHd42;
	private JLabel lbllblMI42;
	private JLabel lbllblEff42;
	private JLabel label190;
	private JLabel lblFlow42;
	private JLabel lblHead42;
	private JLabel lblMotIp42;
	private JLabel lblEff42;
	private JLabel lblMC42;
	private JTable tblRes42;
	private JPanel pnlBot42;
	private JLabel label96;
	private JLabel lblTotTest42;
	private JLabel label97;
	private JLabel lblTotTypeTest42;
	private JLabel lblTestedBy42;
	private JLabel lblVerBy9;
	private JLabel label98;
	private JLabel lblAppVer42;
	private JPanel pnlRep43;
	private JPanel pnlPumpOpt;
	private JRadioButton optAllPump;
	private JRadioButton optSelPump;
	private JLabel lblNonISI;
	private JScrollPane scrollPane43;
	private JPanel printArea43;
	private JPanel pnlPrint43;
	private JLabel lblCustLogo43;
	private JLabel lblCompName43;
	private JLabel lblIS43;
	private JLabel lblISILogo43;
	private JLabel lblCompAdr43;
	private JLabel lblTitle43;
	private JLabel lblISIRefNo43;
	private JLabel lblTitle431;
	private JLabel lblPage43;
	private JPanel pnlTop43;
	private JLabel label87;
	private JLabel lblDt43;
	private JPanel pnlPrime43;
	private JLabel label188;
	private JLabel lblDis43;
	private JLabel lblHd43;
	private JLabel lbllblMI43;
	private JLabel lbllblEff43;
	private JLabel label191;
	private JLabel label352;
	private JLabel label348;
	private JLabel label349;
	private JLabel label350;
	private JLabel label351;
	private JLabel label353;
	private JLabel label354;
	private JLabel label355;
	private JLabel label356;
	private JLabel label358;
	private JLabel label359;
	private JLabel label360;
	private JLabel label361;
	private JLabel label362;
	private JLabel label363;
	private JLabel label371;
	private JLabel label372;
	private JLabel label373;
	private JLabel label374;
	private JLabel label375;
	private JLabel label364;
	private JLabel label365;
	private JLabel label366;
	private JLabel label367;
	private JLabel label368;
	private JTable tblRes43;
	private JPanel pnlBot43;
	private JLabel lblTestedBy43;
	private JLabel label101;
	private JLabel lblVerBy8;
	private JLabel lblAppVer43;
	private JPanel pnlRep5;
	private JPanel pnlFactor;
	private JCheckBox chkQvH;
	private JCheckBox chkQvMI;
	private JCheckBox chkQvOE;
	private JCheckBox chkQvC;
	private JScrollPane scrollPane5;
	private JPanel printArea5;
	private JLabel lblRtWarn5;
	private JPanel pnlPrint5;
	private JLabel lblCustLogo5;
	private JLabel lblCompName5;
	private JLabel lblIS5;
	private JLabel lblISILogo5;
	private JLabel lblCompAdr5;
	private JLabel lblTitle5;
	private JLabel lblISIRefNo5;
	private JLabel label4;
	private JLabel lblPumpType5;
	private JLabel lblPage5;
	private JPanel pnlGraph5;
	private JPanel pnlBot5;
	private JLabel lblTestedBy5;
	private JLabel lblVerBy7;
	private JLabel label104;
	private JLabel lblAppVer5;
	private JPanel pnlRep6;
	private JButton cmdSaveCustom;
	private JScrollPane scrollPane6;
	private JPanel printArea6;
	private JPanel pnlPrint6;
	private JLabel lblIS6;
	private JLabel lblCustLogo6;
	private JLabel lblCompName6;
	private JLabel lblISILogo6;
	private JLabel lblCompAdr6;
	private JLabel lblTitle6;
	private JLabel lblISIRefNo6;
	private JLabel lblPage6;
	private JPanel pnlTop6;
	private JLabel label83;
	private JLabel lblPumpType6;
	private JLabel label91;
	private JLabel lblPh6;
	private JLabel label92;
	private JLabel lblPSz6;
	private JLabel label111;
	private JLabel lblNoStage6;
	private JLabel label85;
	private JLabel lblPSno6;
	private JLabel label105;
	private JLabel label89;
	private JLabel lblMRt6;
	private JLabel label95;
	private JLabel lblFreq6;
	private JLabel label110;
	private JLabel lblSpeed6;
	private JLabel label112;
	private JLabel lblMotType6;
	private JLabel label90;
	private JLabel lblDt6;
	private JLabel label117;
	private JLabel label118;
	private JLabel label119;
	private JLabel label94;
	private JLabel lblV6;
	private JLabel lblHdDuty6;
	private JLabel lblHead6;
	private JLabel lblHdRg6;
	private JLabel lblHeadR6;
	private JLabel label113;
	private JLabel lblMotEff6;
	private JLabel label115;
	private JTextField txtCap6;
	private JTextField txtCRMain6;
	private JTextField txtCRAux6;
	private JTextField txtCRTemp6;
	private JLabel label99;
	private JLabel lblCur6;
	private JLabel lblFlowRtLabel6;
	private JLabel lblFlow6;
	private JLabel lblFlowRtLabel62;
	private JLabel lblDisR6;
	private JLabel label114;
	private JTextField txtDuty6;
	private JLabel label116;
	private JTextField txtCon6;
	private JLabel label214;
	private JLabel label215;
	private JLabel label216;
	private JLabel label93;
	private JLabel lblMotIp6;
	private JLabel lblHdSO6;
	private JLabel lblSHHd6;
	private JLabel label121;
	private JLabel lblSHCur6;
	private JLabel label124;
	private JLabel lblSHPow6;
	private JLabel label212;
	private JLabel label213;
	private JLabel label125;
	private JTextField txtSBed6;
	private JPanel panel2;
	private JLabel label84;
	private JLabel label218;
	private JLabel label217;
	private JTextField txtIRTest;
	private JLabel label219;
	private JTextField txtSPTime;
	private JLabel label220;
	private JPanel pnlPrime6;
	private JLabel label189;
	private JLabel label221;
	private JLabel label106;
	private JLabel label138;
	private JLabel label193;
	private JLabel label192;
	private JLabel lblPerfHz6;
	private JLabel label343;
	private JLabel label357;
	private JLabel label107;
	private JLabel label369;
	private JLabel label370;
	private JLabel label195;
	private JLabel label196;
	private JLabel label197;
	private JLabel label198;
	private JLabel label199;
	private JLabel label200;
	private JLabel label201;
	private JLabel label7;
	private JLabel label29;
	private JLabel label222;
	private JLabel lblHdS6;
	private JLabel lblHdD6;
	private JLabel lblHdHC6;
	private JLabel lblHdVHC6;
	private JLabel lblHdT6;
	private JLabel lblFlowUnitS6;
	private JLabel label207;
	private JLabel label208;
	private JLabel label209;
	private JLabel label202;
	private JLabel lblFlowUnitS62;
	private JLabel lblHdRT6;
	private JLabel label211;
	private JLabel label223;
	private JLabel label46;
	private JTable tblRes6;
	private JLabel label256;
	private JPanel pnlBot6_2;
	private JLabel lblTestedBy6;
	private JLabel lblVerBy6;
	private JLabel label109;
	private JPanel pnlBot6_1;
	private JLabel label249;
	private JLabel label250;
	private JLabel label251;
	private JLabel label252;
	private JLabel label258;
	private JLabel label253;
	private JLabel label267;
	private JLabel label268;
	private JLabel label255;
	private JLabel label254;
	private JLabel label271;
	private JLabel label263;
	private JLabel label260;
	private JLabel label264;
	private JLabel label261;
	private JLabel label265;
	private JLabel label262;
	private JLabel label266;
	private JLabel label269;
	private JLabel label270;
	private JTextField txtV6;
	private JTextField txtA6;
	private JTextField txtP6;
	private JTextField txtS6;
	private JTextField txtF6;
	private JTextField txtH6;
	private JTextField txtBT6;
	private JTextField txtRT6;
	private JTextField txtM61;
	private JTextField txtA61;
	private JTextField txtT61;
	private JTextField txtM62;
	private JTextField txtA62;
	private JTextField txtT62;
	private JLabel label257;
	private JTextField txtRem;
	private JLabel lblAppVer6;
	// JFormDesigner - End of variables declaration  //GEN-END:variables

	// custom code - begin
	private double totRowsPerPage = 75.0;
	private double totRowsPerPageISIType = 50.0;
	private double totRowsPerPageISIMaxMin = 22.0;
 	private int totAvailPagesRegRep = 0;
	private int curPageRegRep=1;
/*	private int totAvailPagesTestRep = 0;
	private int curPageTestRep=1;
	private int totAvailPagesTestGraph = 0;
	private int curPageTestGraph=1;
	private int totAvailPagesISIReport = 0;
	private int curPageISIReport=1;*/
	
	private int curPage=1;
	private int totAvailPages = 0;

	private int totAvailPagesISIType = 0;
	private int curPageISIType=1;
	
	private int totAvailPagesISIMaxMin = 0;
	private int curPageISIMaxMin=1;
		
	private int totAvailPagesCompare = 0;
	private int curPageCompare=1;
	
	//private boolean reportLoadedFirstTime[] = {false,false,false,false,false,false};
	private boolean initInProgress = false;
	private boolean curPumpIsNonISI = false;
	private boolean isRatedSpeed = false;
	private boolean isSmoothLine = true;
	
	private ArrayList<Component> pnlPrints = new ArrayList<Component>();
	private ArrayList<String> pumpTypeList = new ArrayList<String>();
	private HashMap<String, String> pumpTypeRtVList = new HashMap<String, String>();
	private ArrayList<String> pumpTypeNonISIList = new ArrayList<String>();
	private ArrayList<String> pumpTypeMVList = new ArrayList<String>();
	private LinkedHashMap <String, String> pumpDisUnit = new LinkedHashMap<String, String>();
	private LinkedHashMap <String, String> pumpHdUnit = new LinkedHashMap<String, String>();
	private LinkedHashMap <String, String> pumpCatList = new LinkedHashMap<String, String>();
	private LinkedHashMap <String, String> pumpRtVList = new LinkedHashMap<String, String>();
	private LinkedHashMap <String, String> pumpTypeIdList = new LinkedHashMap<String, String>();
	
	private Database db = null;
	private Statement stmt = null;
	private Statement stmt2 = null;
	private Statement stmtAct1 = null;
	private Statement stmtAct2 = null;
	private SimpleDateFormat reqDtFormat = new SimpleDateFormat("dd-MM-yyyy");
	private SimpleDateFormat dbDtFormat = new SimpleDateFormat("yyyy-MM-dd");
	private String filterText = "";
	private String filterTextNoType = "";
	private String pumpFilterText = "";
	private LinkedHashMap <Integer, String> pumpList = new LinkedHashMap<Integer, String>();
	
	
	private LinkedHashMap <String, Integer> testListSeqFromCode = new LinkedHashMap<String, Integer>();
	private String curPumpType = "";
	private String curPumpTypeId = "";
	private String curPumpCat = "";
	private String curDisUnit = "";
	private String defDisUnit = "";
	private String curHdUnit = "";
	private String defHdUnit = "";
	private String recPumpNo = "";
	private Calendar recPumpDt = null;
	private boolean mixedDisUnit = false; // used for max/min report
	private boolean curMVPump = false;
	private String curRtV = "";
	
	private DecimalFormat dotZero = new DecimalFormat("#");
	private DecimalFormat dotOne = new DecimalFormat("#0.0");
	private DecimalFormat dotTwo = new DecimalFormat("#0.00");
	private DecimalFormat dotThree = new DecimalFormat("#0.000");
	private DecimalFormat dotFour = new DecimalFormat("#0.0000");
	
	private Color clrHead = new Color(0,102,204);
	private Color clrMI = new Color(225,0,113);
	private Color clrE = new Color(128,0,0);
	private Color clrCur = new Color(0,153,0);
	
	private Color clrOutline = null;
	private Color clrHeader = null;
	private Color clrLabel = null;
	private Color clrDeclared = null;
	private Color clrActual = null;
	
	private Container contentPane = null;
	
	// perf tolerance
	private Float tolDisMin = 0F;
	private Float tolDisMax = 0F;
	private Float tolHeadMin = 0F;
	private Float tolHeadMax = 0F;
	private Float tolPowMin = 0F;
	private Float tolPowMax = 0F;
	private Float tolCurMin = 0F;
	private Float tolCurMax = 0F;
	private Float tolEffMin = 0F;
	private Float tolEffMax = 0F;
	// custom code - end
}

