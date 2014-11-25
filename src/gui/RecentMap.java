package gui;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.prefs.Preferences;

/**
 * @author Athanasios Kontis
 */
public class RecentMap extends LetterFrame {

	private static final long serialVersionUID = 1L;
	static Preferences prefs = Preferences.userNodeForPackage(RecentMap.class);
	
	public RecentMap(Locale locale) {
		super("recentMapTitle", locale);		
	}

	@Override
	protected void populateLetterList() {		
		char[] recent = getRecent().toCharArray();
		letterList = new ArrayList<Character>(recent.length);
		if (recent.length > 0)
			for (char c : recent) {
				letterList.add(c);
			}		
	}
	
	/** get recently used characters from local storage file */
	static String getRecent() {		
		return prefs.get("recent", "");		
	}
	
	static void updateRecent(String newEntries) {
		Set<Character> recentSet = new TreeSet<>();
		char[] oldEntriesArr = getRecent().toCharArray();		
		char[] newEntriesArr = newEntries.replaceAll("\\s+", "").toCharArray();
		for (char newEntry : newEntriesArr) {
			recentSet.add(new Character(newEntry));
		}
		for (char oldEntry : oldEntriesArr) {
			if (recentSet.size() > 100) break;
			recentSet.add(new Character(oldEntry));
		}		
		StringBuilder sb = new StringBuilder(recentSet.size());
		for (Character c : recentSet) sb.append(c);		
		prefs.put("recent", sb.toString());
	}

	public static void main(String[] args) {
		LetterFrame recent = new RecentMap(null);
		recent.setDefaultCloseOperation(EXIT_ON_CLOSE);
		recent.setLocationRelativeTo(null);
		recent.pack();
		recent.setVisible(true);
		recent.setResizable(false);
	}
}
