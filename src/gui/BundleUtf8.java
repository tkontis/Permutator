package gui;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class BundleUtf8 {
	
	private Locale locale;
	private Map<String, String> bundleMap;
	private ResourceBundle utf8Bundle;
	
	/** 
	 * The purpose of this class is to overcome the difficulty presented by having to store resource bundles
	 * in ISO8859-1 encoding. This class receives a UTF8 encoded resource bundle file reads its entries and 
	 * recodes them in ISO8859-1 format.  
	 */
	private BundleUtf8(String utf8BundleName, Locale l) {		
		assert(locale!=null);
		assert(utf8BundleName!=null);
		setLocale( l );
		recodeBundle(utf8BundleName);
						
	}

	private void recodeBundle(String utf8BundleName) throws NullPointerException, MissingResourceException {
		utf8Bundle = ResourceBundle.getBundle(utf8BundleName, locale);
		if (bundleMap==null) bundleMap = new HashMap<>(100);
		else if (!bundleMap.isEmpty()) bundleMap.clear();
		String isoValue = "";
		for (String key : utf8Bundle.keySet()) {
			try {
				isoValue = new String(utf8Bundle.getString(key).getBytes("iso-8859-1"), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				isoValue = utf8Bundle.getString(key);	// retrieves the value in its original encoding if recoding fails
				e.printStackTrace();
			}
			finally {
				bundleMap.put(key, isoValue);
			}
		}
	}
	public static BundleUtf8 getBundle(String utf8BundleName, Locale l) {		
		return new BundleUtf8(utf8BundleName, l);
	}
	
	/**
	 * @param key The key of the map which points to the iso8859_1 recoded value  
	 * @return The ISO8859-1 stored value from the associated map entry 
	 **/
	public String getString(String key) {	
		if (key==null) throw new NullPointerException("The key cannot be null");		
		return bundleMap.get(key);
	}
	
	public boolean containsKey(String key) {return bundleMap.containsKey(key);}
	public boolean containsValue(String value) {return bundleMap.containsValue(value);}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}
	public void setLocale(String language) {
		// default language is set to English if the parameter is not contained in the list of ISO accepted 2-letter language codes
		if (!Arrays.asList(Locale.getISOLanguages()).contains(language)) {
			language = "en";	
		}
		locale = new Locale(language);
	}
}
