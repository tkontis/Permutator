package permutator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class PermutatorTest {

	static String[] simpleEN = {"a", "ab", "abc", "abcd", "scarf", "abcdef", "combine", "abcdefgh"},
					simpleGR = {"ο", "οι", "ενα", "τρια", "πεντα", "αλεπου", "πρασινο", "εμπιστος"},
					trickyEN = {"aa", "aaa", "tree", "again", "terror", "aggregate", "committee"};
	static Integer[] simpleENkey = {1, 2, 6, 24, 120, 720, 5040, 40320},	// all factorials from 1! to 8!
				 	 simpleGRkey = {1, 2, 6, 24, 120, 720, 5040, 40320},
				 	 trickyENkey = {1, 1, 12, 60, 120, 15120, 45360};	
	static Permutator[] simpleENperm,
						simpleGRperm,
						trickyENperm;	// permutations where the number of permutable things equals to the number of available positions
	static String[][] simpleENresults0_3 = {
		{"a"}, {"ab","ba"}, {"abc","acb","bac","bca","cab","cba"}, 
		{"abcd","abdc","acbd","acdb","adbc","adcb","bacd","badc","bcad","bcda","bdac","bdca","cbad","cbda","cabd","cadb","cdba","cdab","dabc","dacb","dbac","dbca","dcab","dcba"}
	};
	static String[][] trickyENresults0_2 = {
		{"aa"},{"aaa"},{"eert","eret","erte","eetr","etre","eter","reet","rete","rtee","teer","tere","tree"}
	};
	static List<Permutator[]> plist = new ArrayList<>(3);
	static List<String[]> slist = new ArrayList<>(3);
	static List<Integer[]> ilist = new ArrayList<>(3);
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {		
		simpleENperm = new Permutator[simpleEN.length];		
		for (int i=0, len = simpleEN.length; i<len; i++) {simpleENperm[i] = new Permutator(simpleEN[i]);}
		
		simpleGRperm = new Permutator[simpleGR.length];
		for (int i=0, len = simpleGR.length; i<len; i++) {simpleGRperm[i] = new Permutator(simpleGR[i]);}
		
		trickyENperm = new Permutator[trickyEN.length];
		for (int i=0, len = trickyEN.length; i<len; i++) {trickyENperm[i] = new Permutator(trickyEN[i]);}		
		
		plist.add(0, simpleENperm);
		plist.add(1, simpleGRperm);
		plist.add(2, trickyENperm);		
		slist.add(0, simpleEN);
		slist.add(1, simpleGR);
		slist.add(2, trickyEN);
		ilist.add(0, simpleENkey);
		ilist.add(1, simpleGRkey);
		ilist.add(2, trickyENkey);		
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {}

	@Before
	public void setUp() throws Exception {}

	@After
	public void tearDown() throws Exception {}


	@Test
	public final void testGetPositions() {		
		for (int i=0; i < plist.size(); i++) {
			for (int j=0; j < plist.get(i).length; j++) {			
				assertEquals(slist.get(i)[j].length(), plist.get(i)[j].getPositions());			
			}
		}
	}

	@Test
	public final void testPermutate() {		
		Set<String> expectedResults, actualResults;
		int current = 0, progress = 0;
		for (String[] resultsArr : simpleENresults0_3) {			
			expectedResults = new HashSet<String>(Arrays.asList(resultsArr));
			actualResults  = simpleENperm[current].getResults();
			progress = simpleENperm[current++].getProgress();						
			assertTrue(expectedResults.equals(actualResults));	// test actual results
			assertEquals(actualResults.size(), progress);		// test progressIndicator
		}
		current = 0; progress = 0;
		for (String[] resultsArr : trickyENresults0_2) {			
			expectedResults = new HashSet<String>(Arrays.asList(resultsArr));
			actualResults  = trickyENperm[current].getResults();
			progress = trickyENperm[current++].getProgress();
			assertTrue(expectedResults.equals(actualResults));	// test actual results
			assertEquals(actualResults.size(), progress);		// test progressIndicator
		}
		Permutator p = new Permutator("justice");
		p.addPropertyChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals("progressIndicator"))
					System.out.print(evt.getNewValue() + " ");
			}
		});
	}

	@Test
	public final void getTotalPerms() {
		for (int i=0; i < plist.size(); i++) {
			for (int j=0; j < plist.get(i).length; j++) {
				int expected = ilist.get(i)[j].intValue(), 
					actual = plist.get(i)[j].getTotalPerms();
				assertEquals(String.format("ilist.get(%d)[%d].intValue() should return %d, but got %d",i,j, expected, actual), expected, actual);	
			}
		}
	}
	
	@Rule public ExpectedException exception = ExpectedException.none();
	@SuppressWarnings("unused")
	@Test
	public final void testEquals() {
		Permutator p1 = new Permutator("lamina");	// 6 letters equal to p2 p3
		Permutator p2 = new Permutator("animal");	// 6 letters equal to p1 p3
		Permutator p3 = new Permutator("mainal");	// 6 letters equal to p1 p2
		Permutator p4 = new Permutator("lumina");	// 6 letters almost similar to p1,p2,p3 but not equal		
		
		// testing symmetry
		assertTrue(p1.equals(p2));
		assertTrue(p2.equals(p1));
		// testing transitivity
		assertTrue(p1.equals(p3));
		assertTrue(p2.equals(p3));
		// testing against a slightly altered string
		assertFalse(p1.equals(p4));
		assertFalse(p2.equals(p4));
		assertFalse(p3.equals(p4));
		// test of empty permutators
		exception.expect(IllegalArgumentException.class);
		Permutator p5 = new Permutator("");			// empty argument, should throw exception
		Permutator p6 = new Permutator("!@#");		// evaluates to empty argument, should throw exception
	}
}
