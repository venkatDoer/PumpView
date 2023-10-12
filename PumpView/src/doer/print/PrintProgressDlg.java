package doer.print;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

//print progress pop up
public class PrintProgressDlg {
	
	JDialog dlgPrint;
	boolean cancelPrint = false;
	JLabel lblMsg;
	JButton cmdCancel;
	Timer tmrPrint;
	
	public PrintProgressDlg() {
		
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
