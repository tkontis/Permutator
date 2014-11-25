package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

/**
 * @author Athanasios Kontis
 */
public abstract class LetterFrame extends JFrame {

	private class PageChanger implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {			
			if (e.getSource() == nextPage && currentPage < LAST_PAGE) {				
				currentPage++;				
				if (currentPage == LAST_PAGE) nextPage.setEnabled(false);
				if (!prevPage.isEnabled()) prevPage.setEnabled(true);
			}
			else if (e.getSource() == prevPage && currentPage > FIRST_PAGE) {				
				currentPage--;
				if (currentPage == FIRST_PAGE) prevPage.setEnabled(false);
				if (!nextPage.isEnabled()) nextPage.setEnabled(true);	
			}
			populateMap();
			pageLabel.setText(labels.getString("pageLabel") + " " + currentPage);	// renew the page label to show current page
		}
	}
	private class SelectLetterListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			String newText = selLettersTF.getText() + " "+ ((JButton)e.getSource()).getText();
			selLettersTF.setText(newText);
		}
	}
	private static final long serialVersionUID = 1L;	
	protected final int FIRST_PAGE, LAST_PAGE;
	protected final int TABLE_ROWS, TABLE_COLS, LETTERS_PER_PAGE;
	private BundleUtf8 labels;	
	protected Locale currentLocale;
	private JButton nextPage, prevPage, ok, cancel;
	private JButton[] letterButtons;
	private SelectLetterListener selectLetterHandler = new SelectLetterListener();
	private JLabel selLettersLabel, pageLabel;			
	private JTextField selLettersTF;
	private JPanel map;
	private int currentPage;
	
	protected List<Character> letterList;

	JTextField target;

	/** Constructor needs a string reference for the respective key in the resourceBundle and a locale instance to choose the appropriate language 
	 * @param frameTitle Key to the resourceBundle reference for the title of this window 
	 * @param locale A locale instance which sets the appropriate language preference for this window */
	protected LetterFrame(String frameTitle, Locale locale) {
		super();

		// Set the locale to grab the appropriate resource bundle
		currentLocale = locale == null ? new Locale("en") : locale;
		labels = BundleUtf8.getBundle("LabelsBundle", currentLocale);
		super.setTitle(labels.getString(frameTitle));
		
		// Initialize constants
		FIRST_PAGE = 1;
		TABLE_COLS = TABLE_ROWS = 10;
		LETTERS_PER_PAGE = TABLE_ROWS * TABLE_COLS;
		
		// Store the letter list that will back the map
		populateLetterList();
		
		// Set the current page and the last page respectively 
		currentPage = FIRST_PAGE;
		LAST_PAGE = FIRST_PAGE + letterList.size() / LETTERS_PER_PAGE;	
		
		nextPage = new JButton(labels.getString("nextPageLabel") + " >");
		prevPage = new JButton("< " + labels.getString("prevPageLabel"));
		prevPage.setEnabled(false);
		if (letterList.size() <= LETTERS_PER_PAGE) nextPage.setEnabled(false);
		
		PageChanger changer = new PageChanger();
		nextPage.addActionListener(changer);
		prevPage.addActionListener(changer);
		
		// ok button
		ok = new JButton(labels.getString("okBtn"));
		ok.addActionListener(new ActionListener() {			
			@Override public void actionPerformed(ActionEvent e) {
				// retrieve selected characters and clear spaces
				String selected = selLettersTF.getText().replaceAll("\\s+", "");				
				RecentMap.updateRecent(selected);	// update preferences
				// update the target textfield from the caller jframe
				if (target != null) target.setText(selected);
				dispose();	// dispose current frame
			}
		});
		// cancel button
		cancel = new JButton(labels.getString("cancelBtn"));
		cancel.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) {dispose();}});
		
		selLettersLabel = new JLabel(labels.getString("selSymbolsLabel") + ":");
		pageLabel = new JLabel(labels.getString("pageLabel") + " " + currentPage);
		pageLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		selLettersTF = new JTextField(20);
		selLettersTF.setEditable(false);
		selLettersTF.setToolTipText(labels.getString("selSymbolsTooltip"));
		selLettersTF.setBackground(Color.WHITE);
		selLettersTF.addMouseListener(new MouseAdapter() {			
			@Override
			public void mouseClicked(MouseEvent e) {				
				if (e.getButton() == MouseEvent.BUTTON1) {
					selLettersTF.selectAll(); selLettersTF.copy();
				}
				else if (e.getButton() == MouseEvent.BUTTON3) {
					selLettersTF.setText("");
				}
			}
		});
		
		// Design the map holding the button grid
		map = new JPanel(new GridLayout(TABLE_ROWS, TABLE_COLS, 3, 3));
		letterButtons = new JButton[LETTERS_PER_PAGE];	// create the button array
	
		// populate map buttons for current page
		map.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		populateMap();

		// navigation
		JPanel nav = new JPanel();
		Dimension navButtonSize = new Dimension(Math.max(prevPage.getPreferredSize().width, nextPage.getPreferredSize().width), 30);
		prevPage.setPreferredSize(navButtonSize);
		nextPage.setPreferredSize(navButtonSize);
		nav.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		nav.setLayout(new BoxLayout(nav, BoxLayout.X_AXIS));		
		nav.add(prevPage);
		nav.add(Box.createHorizontalGlue());
		nav.add(pageLabel);
		nav.add(Box.createHorizontalGlue());		
		nav.add(nextPage);		
		
		// controls layout
		JPanel controls = new JPanel();
		controls.setBorder(BorderFactory.createEmptyBorder(2, 5, 5, 5));
		controls.setLayout(new BoxLayout(controls, BoxLayout.X_AXIS));
		controls.add(selLettersLabel);
		controls.add(Box.createHorizontalStrut(5));
		controls.add(Box.createHorizontalGlue());
		controls.add(selLettersTF);
		controls.add(Box.createHorizontalGlue());
		controls.add(Box.createHorizontalStrut(5));
		controls.add(ok);
		controls.add(Box.createHorizontalStrut(5));
		controls.add(cancel);
		
		BoxLayout boxLayout = new BoxLayout(getContentPane(), BoxLayout.Y_AXIS);
		setLayout(boxLayout);
		add(map);
		add(new JSeparator());
		add(nav);
		add(controls);
		
		// Initialize window		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		pack();
		setVisible(true);
		setResizable(false);
	}
	
	/** Abstract method which is implemented by subclasses LetterMap and RecentMap */
	protected abstract void populateLetterList();
	
	/** Populates the buttons in the mapPanel and copies a sublist for current page, from the whole range of available characters in letterList */
	private void populateMap() {
		if (letterList.isEmpty()) return;
		int firstIndex = (currentPage-1) * LETTERS_PER_PAGE;
		int lastIndex = currentPage * LETTERS_PER_PAGE - 1;
		if (lastIndex >= letterList.size()) 
			lastIndex = firstIndex + letterList.size() % (LETTERS_PER_PAGE*currentPage) - 1;
		List<Character> letterSubList = letterList.subList(firstIndex, lastIndex+1);
		map.removeAll();
		for (int btnIndex=0; btnIndex < letterSubList.size(); btnIndex++) {			
			// instantiate map buttons			
			letterButtons[btnIndex] = new JButton(letterSubList.get(btnIndex).toString());
			letterButtons[btnIndex].setRolloverEnabled(true);			
			letterButtons[btnIndex].addActionListener(selectLetterHandler);
			map.add(letterButtons[btnIndex]);
		}
		map.validate();
	}
}

