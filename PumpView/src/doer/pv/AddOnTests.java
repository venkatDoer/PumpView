/*
 * Created by JFormDesigner on Sun Jan 22 19:18:52 IST 2017
 */

package doer.pv;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import info.clearthought.layout.*;

/**
 * @author VENKATESAN SELVARAJ
 */
public class AddOnTests extends JDialog {
	public AddOnTests(Frame owner) {
		super(owner);
		frmMain = (PumpView) owner;
		initComponents();
		customInit();
	}

	public AddOnTests(Dialog owner) {
		super(owner);
		initComponents();
		customInit();
	}

	private void cmdSaveActionPerformed() {
		Configuration.IS_SP_TEST_ON = chkSP.isSelected() ? "1":"0";
		Configuration.saveConfigValues("IS_SP_TEST_ON");
		frmMain.showHideOpTest();
		JOptionPane.showMessageDialog(this, "Changes are saved successfully!"); 
	}

	private void cmdExitActionPerformed() {
		this.setVisible(false);
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		pnlTitle = new JPanel();
		chkSP = new JCheckBox();
		cmdSave = new JButton();
		cmdExit = new JButton();

		//======== this ========
		setTitle("Doer PumpViewPro: Add-on Tests");
		setFont(new Font("Arial", Font.PLAIN, 12));
		setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		setResizable(false);
		Container contentPane = getContentPane();
		contentPane.setLayout(new TableLayout(new double[][] {
			{10, TableLayout.FILL, TableLayout.FILL, 10},
			{10, TableLayout.FILL, TableLayout.PREFERRED, 10}}));
		((TableLayout)contentPane.getLayout()).setHGap(5);
		((TableLayout)contentPane.getLayout()).setVGap(5);

		//======== pnlTitle ========
		{
			pnlTitle.setBorder(new TitledBorder(null, "Add-on Tests [Line ]", TitledBorder.CENTER, TitledBorder.TOP,
				new Font("Arial", Font.BOLD, 14), Color.blue));
			pnlTitle.setFocusable(false);
			pnlTitle.setLayout(new TableLayout(new double[][] {
				{TableLayout.FILL},
				{TableLayout.PREFERRED}}));
			((TableLayout)pnlTitle.getLayout()).setHGap(5);
			((TableLayout)pnlTitle.getLayout()).setVGap(5);

			//---- chkSP ----
			chkSP.setText("Self Priming Test");
			chkSP.setFont(new Font("Arial", Font.BOLD, 12));
			pnlTitle.add(chkSP, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		}
		contentPane.add(pnlTitle, new TableLayoutConstraints(1, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

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
		contentPane.add(cmdSave, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

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
		contentPane.add(cmdExit, new TableLayoutConstraints(2, 2, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel pnlTitle;
	private JCheckBox chkSP;
	private JButton cmdSave;
	private JButton cmdExit;
	// JFormDesigner - End of variables declaration  //GEN-END:variables

	private void customInit() {
		// set the panel header
		pnlTitle.setBorder(new TitledBorder(null, "Optional Tests [" + Configuration.LINE_NAME + "]", TitledBorder.CENTER, TitledBorder.TOP,
				new Font("Arial", Font.BOLD, 14), Color.blue));
		
		// load settings
		chkSP.setSelected(Configuration.IS_SP_TEST_ON.equals("1"));
		
		associateFunctionKeys();
	}

	private void associateFunctionKeys() {
			
			// associate f4 for save
		/*String SAVE_ACTION_KEY = "saveAction";
		Action saveAction = new AbstractAction() {
		      public void actionPerformed(ActionEvent actionEvent) {
		        cmdSaveActionPerformed();
		      }
		    };
		KeyStroke f4 = KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0);
		InputMap saveInputMap = cmdSave.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		saveInputMap.put(f4, SAVE_ACTION_KEY);
		ActionMap saveActionMap = cmdSave.getActionMap();
		saveActionMap.put(SAVE_ACTION_KEY, saveAction);
		cmdSave.setActionMap(saveActionMap);*/
		
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

	// custom variables
	PumpView frmMain = null;
}
