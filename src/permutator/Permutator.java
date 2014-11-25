package permutator;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/* A Permutation is an ordered Combination. The Permutator object encapsulates the necessary information to calculate all possible permutations 
 * that can be formed given a random group of characters. The group of characters need not be a set (unique items). For example, a valid group is
 * {a, a, b, c, c}. However, when the characters are not unique the multiplicity of each character in the group is taken into consideration and 
 * reduces the total number of permutations, compared to that of another equal-size collection of unique characters. */
public class Permutator {

	// members
	private int totalPerms = 0, positions = 0, progressIndicator = 0;
	private PropertyChangeSupport pcs;
	List<Character> thingsList; String things;	
	Set<String> results;
	protected static Map<String, Permutator> calculated = new HashMap<>(20);
	
	// constructors
	public Permutator( String s ) {this(s.toCharArray());}
	public Permutator ( char[] things) {
		setPermutables (things);
		positions = thingsList.size();		
		if (calculated.containsKey(things)) {		
			Permutator storedCopy = calculated.get(things); 
			this.totalPerms = storedCopy.totalPerms;
			this.results = storedCopy.results; 
		}
		else {			
			predictTotalPerms();
		}
		pcs = new PropertyChangeSupport(this);
	}
	
	// setters getters
	@SuppressWarnings("unchecked")
	private void setPermutables(char[] permutables) {		
		this.thingsList = (permutables==null) ? (ArrayList<Character>) Collections.EMPTY_LIST: new ArrayList<Character>(permutables.length);		
		for (char permutable : permutables) {
			if (Character.isLetterOrDigit(permutable)) {
				thingsList.add(Character.valueOf(permutable));
			}
		}
		if (thingsList.isEmpty()) throw new IllegalArgumentException("Make sure that `permutables` evaluates to a non-empty acceptable string");
		Collections.sort(thingsList);
		StringBuilder thingsBuilder = new StringBuilder(10);		
		for (Character c : thingsList) {thingsBuilder.append(c);}		
	}
	
	public List<Character> getPermutables() {return thingsList;}
	public int getPositions() {return positions;}	
	public int getTotalPerms() {return totalPerms;}
	public Set<String> getResults() {
		if (results == null) permutate();
		return Collections.unmodifiableSet(results);
	}
	private void increaseProgress() {
		int oldValue = progressIndicator;
		int newValue = ++progressIndicator;
		pcs.firePropertyChange("progressIndicator", oldValue, newValue);
	}
	public int getProgress() {return progressIndicator;}
	public void addPropertyChangeListener(PropertyChangeListener listener) {pcs.addPropertyChangeListener(listener);}
	public void removePropertyChangeListener(PropertyChangeListener listener) {pcs.removePropertyChangeListener(listener);}
	
	// logic methods
	/** Initiates the costly calculation of permutations for the given array of characters */
	private void permutate() {
		assert (positions > 0 && positions <= thingsList.size());
		results = new HashSet<>(totalPerms);
		results.addAll(recursiveHelper(new ArrayDeque<>(thingsList)));	
		calculated.put(things, this);
	}
	
	private Set<String> recursiveHelper(Deque<Character> deque) {
		HashSet<String> recResults = new HashSet<>(totalPerms);
		switch (deque.size()) {
		case 1:
			recResults.add(deque.peek().toString()); increaseProgress();
			break;
		default:			
			Set<Character> unique = new HashSet<>(deque);	// marked chars as used during iteration
			for (Character current : unique) {
				deque.remove(current);
				recResults.addAll(merge(current, recursiveHelper(deque)));
				deque.add(current);
			}
		}		
		return recResults;
	}
	
	private Collection<String> merge (Character headChar , Collection<String> headlessStrings) {
		Collection<String> merged = new ArrayList<>(headlessStrings.size());
		for (String headlessString : headlessStrings) {			
			merged.add(headChar.toString().concat(headlessString));			
		}
		return merged;
	}
	
	/** Uses the respective factorial formula to calculate the number of total permutations without actually calculating them per se. */
	private void predictTotalPerms() {
		int multiplicity = 1, 	// default multiplicity is 1 for each permutable thing
			product = 1;		// the total product of every multiplicity's factorial (where multiplicity>1)		
		Set<Character> uniqueThings = new HashSet<>(thingsList);
		for (Character thing : uniqueThings) {
			multiplicity = Collections.frequency(thingsList, thing);			
			if (multiplicity > 1) {
				product *= factorial(multiplicity);
			}
		}
		int noRepPerms = 1;	// no repetition permutations (the number of permutations should all things be unique)
		if (positions == thingsList.size() || positions == thingsList.size() -1) {
			noRepPerms = factorial(thingsList.size());
		}	
		totalPerms = noRepPerms / product;	// we must divide with the calculated product to get the total permutations taking into account possible multiplicities > 1		
	}
	
	/** Calculates the factorial of an integer n */
	private int factorial(int n) {
		if (n<0) {
            throw new IllegalArgumentException("arg must be an int equal or greater than 0");
        }
        else if (n<=1) {
            return 1;
        }
        else {
            int result = 1;
            for (int i = 2; i <= n; i++) result *= i;
            return result;
        }
	}

	public String getStringifiedResults() {
		if (results == null) permutate();
		int buffer = getTotalPerms() * (getPositions() - 1);	// a prediction on the total length of the result string		
		StringBuffer sb = new StringBuffer(buffer);
		for (String s : results) sb.append(s).append(" ");
		return sb.toString().replaceFirst("\\s+$", "");
	}
	
	// Overridden methods from Object
	@Override public int hashCode() {
		int result = 17;		
		result = 31 * result + positions;
		int thingsHash = 13;
		for (char thing: thingsList) {thingsHash = 7 * thingsHash + (int) thing;}
		result = 31 * result + thingsHash;
		return result;
	}	
	@Override public boolean equals (Object o) {		
		if (!(o instanceof Permutator)) return false;	
		Permutator p = (Permutator) o;		
		return positions == p.positions && thingsList.containsAll(p.thingsList);
	}
	@Override public String toString(){
		return String.format("{n:%d, r:%d, total:%d}", thingsList.size(), positions, totalPerms);		
	}
}
