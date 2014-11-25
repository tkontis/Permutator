package gui;

import java.util.ArrayList;
import java.util.Locale;

/**
 * @author Athanasios Kontis
 */
public class LetterMap extends LetterFrame {

	private static final long serialVersionUID = 1L;
	
	LetterMap (Locale locale) {
		super("letterMapTitle", locale);
	}
	
	@Override
	protected void populateLetterList() {
		letterList = new ArrayList<Character>(Character.MAX_CODE_POINT - Character.MIN_CODE_POINT);			
		Character curSymbol;
		for (int i=Character.MIN_CODE_POINT; i < Character.MAX_CODE_POINT; i++) {
			if (Character.isLetterOrDigit(i)) {
				curSymbol = new Character((char)i);
				letterList.add(curSymbol);
			}
		}
	}

	public static void main(String[] args) {
		LetterFrame letters = new LetterMap(null);
		letters.setDefaultCloseOperation(EXIT_ON_CLOSE);
		letters.setLocationRelativeTo(null);
		letters.pack();
		letters.setVisible(true);
		letters.setResizable(false);
	} 
}
