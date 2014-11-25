package gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import permutator.Permutator;

/** 
 * @author Athanasios Kontis 
 * @category desktop utility 
 */
public class MainPanel extends JPanel {

	private static final long serialVersionUID = -756080828335715562L;
	JLabel selectedLbl, resultsLbl, summaryLbl;
	JButton recentBtn, chooserBtn, permutateBtn, exportBtn;
	private JComboBox<Language> languageCombo;
	JTextArea resultsArea;
	private JScrollPane resultsScroller;	
	JTextField selectedTF;
	private static BundleUtf8 labels;
	JProgressBar progressBar;	
	private LetterFrame recentMap, letterMap;
	private static Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(MainPanel.class);
	Permutator results;
	private Action showRecentCharsAction, showCharMapAction, permutateAction, exportAction;
	private JFrame hostWindow;
	
	public MainPanel (JFrame hostWindow, Locale locale) {
		// Set the locale to grab the appropriate resource bundle
		setLocale(locale);
		if (hostWindow == null) throw new NullPointerException("Reference to host window cannot be null");
		this.hostWindow = hostWindow; // saves a reference to the host window (containing the MainPanel)
		labels = BundleUtf8.getBundle("LabelsBundle", locale);		
		init(); // initialize components
	}
	
	private void init() {
		// The main frame's layout manager
		GridBagLayout gbl = new GridBagLayout(); 
		setLayout(gbl);
		
		// Initialize results TextArea
		resultsArea = new JTextArea();
		resultsArea.setEditable(false);
		resultsArea.setEnabled(false);
		resultsArea.setWrapStyleWord(true);
		resultsArea.setLineWrap(true);
		resultsScroller = new JScrollPane(resultsArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);		
		
		// Initialize Selected Chars TextField
		selectedTF = new JTextField();
		selectedTF.setToolTipText(labels.getString("selectedTFtooltip"));
				
		// Initialize Labels
		selectedLbl = new JLabel(labels.getString("selectedLbl"));
		selectedLbl.setLabelFor(selectedTF);
				
		resultsLbl = new JLabel(labels.getString("resultsLbl"));
		resultsLbl.setLabelFor(resultsArea);
		
		summaryLbl = new JLabel(labels.getString("summaryLbl"));
		
		// Initialize buttons
		showRecentCharsAction = new RecentCharsAction();
		showCharMapAction = new CharMapAction();
		permutateAction = new PermutateAction(this);
		exportAction = new ExportAction();
		recentBtn =  new JButton( showRecentCharsAction );
		chooserBtn = new JButton( showCharMapAction );
		permutateBtn = new JButton( permutateAction );		
		exportBtn = new JButton( exportAction );
		exportBtn.setEnabled(false);
		
		// Initialize language ComboBox selector
		Language[] availableLanguages = Language.values(); 
		languageCombo = new JComboBox<>(availableLanguages);
		languageCombo.setEditable(false);
		languageCombo.setAlignmentX(JFrame.CENTER_ALIGNMENT);
		int currentLanguage = Arrays.asList(availableLanguages).indexOf(Language.nameOf(getLocale().getLanguage()));
		languageCombo.setSelectedItem(availableLanguages[ currentLanguage ] );
		languageCombo.addItemListener(new ItemListener() {
			@Override public void itemStateChanged(ItemEvent e) {				
				if (e.getSource() instanceof JComboBox && e.getStateChange() == ItemEvent.SELECTED){
					@SuppressWarnings("unchecked")
					JComboBox<Language> combo = (JComboBox<Language>) e.getSource();				
					Language selLanguage = (Language)combo.getSelectedItem();
					prefs.put("language", selLanguage.iso);					
					createAndShowUI(selLanguage.getLocale());
					hostWindow.dispose();
				}
			}
		});
				
		// Initialize TextFields
		selectedTF = new JTextField();
		selectedTF.setDropMode(DropMode.INSERT);		
		selectedTF.setAction(permutateAction);		

		// Initialize Progress bar
		progressBar = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);
		progressBar.setStringPainted(true);
		progressBar.setValue(0);		
		
		// Add Components to mainPanel		
		add(selectedLbl, Placement.SINGLE_LINER.buildConstraints(0, 0, 2, 1));
		add(selectedTF, Placement.SINGLE_LINER.buildConstraints(0, 1, 2, 1));
		add(chooserBtn, Placement.LEFT_COL.buildConstraints(0, 2, 1, 1));
		add(recentBtn, Placement.RIGHT_COL.buildConstraints(1, 2, 1, 1));		
		add(permutateBtn, Placement.SINGLE_LINER.buildConstraints(0, 3, 2, 1));
		add(resultsLbl, Placement.SINGLE_LINER.buildConstraints(0, 4, 2, 1));		
		add(resultsScroller, Placement.AREA.buildConstraints(0, 5, 2, 7));
		add(summaryLbl, Placement.SINGLE_LINER.buildConstraints(0, 12, 2, 1));
		add(exportBtn, Placement.LEFT_COL.buildConstraints(0, 13, 1, 1));
		add(languageCombo, Placement.RIGHT_COL.buildConstraints(1, 13, 1, 1));
		add(progressBar, Placement.LAST_LINE.buildConstraints(0, 14, 2, 1));
	}
	
	enum Placement {		
		SINGLE_LINER (1, 7, GridBagConstraints.HORIZONTAL, new Insets(5,5,1,5), 5, 3),
		LEFT_COL (1, 5, GridBagConstraints.BOTH, new Insets(5, 5, 5, 0), 5, 1),
		RIGHT_COL (1, 5, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 1),
		AREA (1, 400, GridBagConstraints.BOTH, new Insets(1,5,3,5), 5, 70),
		LAST_LINE (1, 20, GridBagConstraints.BOTH, new Insets(10,5,5,5), 5, 7)
		;
		private int weightx,weighty, fill, ipadx, ipady;
		private Insets insets;
		Placement(int wx,int wy,int f,Insets in, int px, int py) {weightx=wx;weighty=wy;fill=f;insets=in;ipadx=px;ipady=py;}
		GridBagConstraints buildConstraints(int gridx, int gridy, int gridwidth, int gridheight) {			
			int anchor = GridBagConstraints.LINE_START;
			return new GridBagConstraints(gridx, gridy, gridwidth, gridheight, weightx, weighty, anchor, fill, insets, ipadx, ipady);
		}
	}
	
	/* ACTIONS */
	class RecentCharsAction extends AbstractAction {		
		private static final long serialVersionUID = 5025772986788011151L;

		RecentCharsAction() {
			super(labels.getString("showRecentCharsAction"));			
			putValue(AbstractAction.SHORT_DESCRIPTION, labels.getString("showRecentCharsDesc"));
			putValue(AbstractAction.MNEMONIC_KEY, KeyEvent.VK_R);
		}
		@Override public void actionPerformed(ActionEvent e) {
			if (recentMap==null) recentMap = new RecentMap(getLocale());
			recentMap.setVisible(true);
			recentMap.target = selectedTF;
		}
	}
	class CharMapAction extends AbstractAction {		
		private static final long serialVersionUID = -4093915042326651489L;
		CharMapAction() {
			super(labels.getString("showCharMapAction"));
			putValue(AbstractAction.SHORT_DESCRIPTION, labels.getString("showCharMapDesc"));
			putValue(AbstractAction.MNEMONIC_KEY, KeyEvent.VK_M);
		}
		@Override public void actionPerformed(ActionEvent e) {
			if (letterMap==null) letterMap = new LetterMap(getLocale());
			letterMap.setVisible(true);
			letterMap.target = selectedTF;
		}
	}	
	class PermutateAction extends AbstractAction {		
		private static final long serialVersionUID = 5770636700719471360L;
		private MainPanel mainPanel;
		PermutateAction(MainPanel instance) {
			super(labels.getString("permutateAction"));
			putValue(AbstractAction.SHORT_DESCRIPTION, labels.getString("permutateDesc"));
			putValue(AbstractAction.MNEMONIC_KEY, KeyEvent.VK_P);
			mainPanel = instance;
		}
		@Override public void actionPerformed(ActionEvent e) {			
			if (selectedTF.getText().isEmpty()) return;
			// Start the background task of calculating the permutations
			progressBar.setValue(0); // reset the progress bar
			PermutateTask task = new PermutateTask(e, labels, mainPanel);
			task.execute();
			task.addPropertyChangeListener(new PropertyChangeListener() {				
				@Override public void propertyChange(PropertyChangeEvent evt) {
					if (evt.getPropertyName() == "progress") {
						progressBar.setValue((int) evt.getNewValue());
					}					
				}
			});
		}
	}
	class ExportAction extends AbstractAction {
		private static final long serialVersionUID = 9132018264657766580L;
		ExportAction() {
			super(labels.getString("exportAction"));
			String desc = labels.getString("exportDesc");
			putValue(AbstractAction.SHORT_DESCRIPTION, desc);
			putValue(AbstractAction.MNEMONIC_KEY, KeyEvent.VK_E);
		}
		@Override public void actionPerformed(ActionEvent e) {
			String permutables = Arrays.toString(results.getPermutables().toArray(new Character[results.getPermutables().size()]));
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");			
			String filename = String.format("%2$s - permutations of %1$s - .txt", permutables, sdf.format(new Date()));
			String newLine = System.getProperty("line.separator");
			try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename, false),"UTF-8"))){				
				out.append("Characters permutated: " + permutables).append(newLine);	// 1st line	
				out.append("Total permutations calculated: " + results.getTotalPerms()).append(newLine);			// 2nd line
				out.append( results.getStringifiedResults() ).append(newLine); 								// append results				
				JOptionPane.showMessageDialog(null, labels.getString("exportCompleteMsg"), labels.getString("opCompleteTitle"), JOptionPane.INFORMATION_MESSAGE);				
			} catch (IOException ioException) {
				JOptionPane.showMessageDialog(null, labels.getString("exportIncompleteMsg"), labels.getString("opFailedTitle"), JOptionPane.ERROR_MESSAGE);
				ioException.printStackTrace();
			}
		}
	}
	
	static private void createAndShowUI(final Locale locale){
		SwingUtilities.invokeLater(new Runnable() {
			@Override public void run(){
				JFrame app = new JFrame("Permutator");
				JPanel mainPanel = new MainPanel(app, locale);
				app.setContentPane(mainPanel);		
				app.pack();
				app.setMinimumSize(app.getSize());
				app.setLocationRelativeTo(null);		
				app.setVisible(true);		
				app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			}
		});
	}
	/**
	 * @param args First argument, @args[0], is expected to be a string argument for the Locale class (e.g. "el_GR")  
	 */
	public static void main(String[] args) {		
		String defaultLanguage;
		List<String> isoNames = Arrays.asList(new String[]{"en","el","sv","ru"});
		if (args.length > 0 && isoNames.contains(args[0]))
			defaultLanguage = args[0];		
		else
			defaultLanguage = prefs.get("language", "en");
		final Locale instanceLocale = new Locale(defaultLanguage);		
		createAndShowUI(instanceLocale);		
	}

}
