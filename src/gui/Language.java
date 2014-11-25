package gui;

import java.util.Locale;

enum Language {
	ENGLISH ("english", "english", "en"),
	SWEDISH ("svenska", "swedish", "sv"),
	GREEK   ("ελληνικά", "greek", "el"),
	RUSSIAN ("русский", "russian", "ru");
	private String nativeLang, interLang;
	String iso;
	Language(String res, String desc, String iso) {
		nativeLang = res; 
		interLang = desc; 
		this.iso = iso;
	}
	Locale getLocale() {
		return new Locale(iso);
	}
	String getEnglishName() {return interLang;}
	String getNativeName() {return nativeLang;}
	static String[] getISOnames () {
		String[] isoNames = new String[Language.values().length]; int i =0; 
		for (Language lang : Language.values()) {
			isoNames[i++] = lang.iso;
		}
		return isoNames;
	}
	static Language nameOf(String isoName) {
		for (Language l : Language.values())
			if(l.iso.equals((new Locale(isoName)).getLanguage()))
				return l;
		return null;
	}
	@Override public String toString() {return String.format("%s (%s)", nativeLang, interLang);}
}