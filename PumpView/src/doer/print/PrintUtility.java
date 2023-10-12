package doer.print;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import javax.swing.RepaintManager;

/**
 * @author Venkatesan Selvaraj @ Embetec
 */

// class which has the utility functions to print the content of a swing component 
public class PrintUtility implements Printable{
	
	// variable declaration
	private Component compToBePrinted;
	private PrintRequestAttributeSet aset; 
	private PrinterJob pJob;
	
	// print component function
	public boolean printComponent(Component compToBePrinted) throws PrinterException {
		this.compToBePrinted = compToBePrinted;
		return print();
	}
	
	public boolean initialize() {
		pJob = PrinterJob.getPrinterJob();
		pJob.setPrintable(this);
		boolean yesPrint = pJob.printDialog();
		return yesPrint;
	}
	
	// print function
	public boolean print() throws PrinterException {
		if (pJob == null) {
			pJob = PrinterJob.getPrinterJob();
			pJob.setPrintable(this);
			boolean yesPrint = pJob.printDialog();
			if (yesPrint) {
				pJob.print();
			} else {
				return false;
			}
		} else { // for print all 
			pJob.print();
		}
		return true;
	}
	
	// actual print function
	public int print(Graphics g, PageFormat pf, int pageIndex) {
		Graphics2D g2 = (Graphics2D) g;
		
		if (pageIndex != 0 ) {
			return NO_SUCH_PAGE;
		} else {
			// print the component
			setDoubleBuffering(compToBePrinted, false);
			// shift graphic to line up with beginning of printable region
			g2.translate(pf.getImageableX(), pf.getImageableY());
			g2.scale(0.615, 0.565);
			// set the color
			g2.setBackground(Color.white);
			g2.setColor(Color.black);	
			// repaint the page for printing
			compToBePrinted.paint(g2);
			// enable double buffering incase somebody else want it
			setDoubleBuffering(compToBePrinted, true);
			return PAGE_EXISTS;
		}
	}
	
	public static void setDoubleBuffering(Component c, boolean status) {
		RepaintManager curMgr = RepaintManager.currentManager(c);
		curMgr.setDoubleBufferingEnabled(status);
	}
}
