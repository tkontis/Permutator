package gui;

import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import permutator.Permutator;

public class PermutateTask extends SwingWorker<String, Permutator> {

	Permutator p;
	ActionEvent initiator;	
	private BundleUtf8 labels;	
	private MainPanel main;
	
	public PermutateTask(ActionEvent e, BundleUtf8 labels, MainPanel mainPanel) {
		super();
		initiator = e;
		main = mainPanel;
		this.labels = labels;
	}

	@Override
	protected String doInBackground() throws Exception {		
		setProgress(0);
		main.getParent().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		main.selectedTF.setEnabled(false);
		main.resultsArea.setEnabled(false);		
		main.permutateBtn.setEnabled(false);	// disable triggering component
		main.exportBtn.setEnabled(false);
		String things = main.selectedTF.getText();
		p = new Permutator(things);
		final int totalPerms = p.getTotalPerms();		
		p.addPropertyChangeListener(new PropertyChangeListener() {			
			@Override public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals("progressIndicator")) {
					double newValue = ((Integer) evt.getNewValue()).doubleValue();					
					setProgress((int) newValue / totalPerms * 100);
					try {
						Thread.sleep((int)Math.random()*5);
					}
					catch (InterruptedException ex){
						String title = labels.getString("errorTitle"), 
							   msg = labels.getString("interEx")  + "\n" + ex.getCause();						
						int msgType = JOptionPane.ERROR_MESSAGE;
						main.resultsArea.setText( "" );
						JOptionPane.showMessageDialog(null, msg, title, msgType);						
					}
				}
			}
		});
		main.results = p;		
		return p.getStringifiedResults();
	}
	
	@Override
	public void done() {
		Toolkit.getDefaultToolkit().beep();
		main.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		main.selectedTF.setEnabled(true);
		main.resultsArea.setEnabled(true);		
		main.permutateBtn.setEnabled(true);	// enable triggering component
		main.exportBtn.setEnabled(true);
		main.summaryLbl.setText(String.format("%s: %d", labels.getString("summaryLbl"), p.getTotalPerms()));
		String title = labels.getString("successTitle");
		String msg = labels.getString("successMsg");
		int msgType = JOptionPane.INFORMATION_MESSAGE; 
		try {
			main.resultsArea.setText( get() );
			main.resultsArea.setEnabled(true);
		}
		catch(InterruptedException ex) {
			title = labels.getString("errorTitle");
			msg = labels.getString("interEx")  + "\n" + ex.getCause();
			main.resultsArea.setText( "" );
			msgType = JOptionPane.ERROR_MESSAGE;
		}
		catch(ExecutionException ex) {
			title = labels.getString("errorTitle");
			msg = labels.getString("execEx") + "\n" + ex.getCause();
			main.resultsArea.setText( "" );
			msgType = JOptionPane.ERROR_MESSAGE;
		}
		finally {
			JOptionPane.showMessageDialog(null, msg, title, msgType);
		}
	}
}
