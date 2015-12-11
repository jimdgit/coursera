package document;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import org.hamcrest.Matcher;

/** 
 * A naive implementation of the Document abstract class. 
 * @author UC San Diego Intermediate Programming MOOC team
 */
public class BasicDocument extends Document 
{
	final static boolean debug = false;
	/** Create a new BasicDocument object
	 * 
	 * @param text The full text of the Document.
	 */
	public BasicDocument(String text)
	{
		super(text);
	}
	
	
	/**
	 * Get the number of words in the document.
	 * "Words" are defined as contiguous strings of alphabetic characters
	 * i.e. any upper or lower case characters a-z or A-Z
	 * 
	 * @return The number of words in the document.
	 */
	@Override
	public int getNumWords()
	{
		//TODO: Implement this method.  See the Module 1 support videos 
	    // if you need help.
		List<String> sl;
		sl = getTokens("[a-zA-Z]+");
        return sl.size();
	}
	
	/**
	 * Get the number of sentences in the document.
	 * Sentences are defined as contiguous strings of characters ending in an 
	 * end of sentence punctuation (. ! or ?) or the last contiguous set of 
	 * characters in the document, even if they don't end with a punctuation mark.
	 * 
	 * @return The number of sentences in the document.
	 */
	@Override
	public int getNumSentences()
	{
		List<String> sl;
		sl = getTokens("[^!.?]+");
	    //TODO: Implement this method.  See the Module 1 support videos 
        // if you need help.
        return sl.size();
	}
	
	/**
	 * Get the number of syllables in the document.
	 * Words are defined as above.  Syllables are defined as:
	 * a contiguous sequence of vowels, except for a lone "e" at the 
	 * end of a word if the word has another set of contiguous vowels, 
	 * makes up one syllable.   y is considered a vowel.
	 * @return The number of syllables in the document.
	 */
	@Override
	public int getNumSyllables()
	{
	    //TODO: Implement this method.  See the Module 1 support videos 
        // if you need help.
		List<String> sl;
		int sys = 0;
		sl = getTokens("[a-zA-Z]+"); // get all the words.
		for(String s : sl)
		{
			sys += countSyllables(s);
		}
        return sys;
        
	}
	public int countSyllables(String word)
	{

	ArrayList<String> tokens = new ArrayList<String>();
	
	// Break the word into 'not vowels' and  'vowels' and 'not vowels'
	Pattern p = Pattern.compile("[^aeiouy]*[aeiouy]+[^aeiouy]*");
	word = word.toLowerCase();
	Matcher m = p.matcher(word);
	
	while (m.find()) {
	    tokens.add(m.group());
	}
	
	if( debug )	{
		System.out.print("["  +word + "]  : ");

		for( String s : tokens){
			System.out.print(s + " ");
		}
	}
	if(tokens.size() > 1 && tokens.lastIndexOf("e") ==  ( tokens.size() - 1 )  )
	{
		tokens.remove(0);
	}
	if( debug )
		System.out.println(" " + tokens.size());
	return tokens.size(); 

	}
	
	/* The main method for testing this class. 
	 * You are encouraged to add your own tests.  */
	public static void main(String[] args)
	{
	
		testCase(new BasicDocument("This is a test.  How many???  "
		        + "Senteeeeeeeeeences are here... there should be 5!  Right?"),
				16, 13, 5);
		testCase(new BasicDocument(""), 0, 0, 0);
		testCase(new BasicDocument("sentence, with, lots, of, commas.!  "
		        + "(And some poaren)).  The output is: 7.5."), 15, 11, 4);
		testCase(new BasicDocument("many???  Senteeeeeeeeeences are"), 6, 3, 2);
		testCase(new BasicDocument("Here is a series of test sentences. Your program should "
				+ "find 3 sentences, 33 words, and 49 syllables. Not every word will have "
				+ "the correct amount of syllables (example, for example), "
				+ "but most of them will."), 49, 33, 3);
		testCase(new BasicDocument("Segue"), 2, 1, 1);
		testCase(new BasicDocument("Sentence"), 2, 1, 1);
		testCase(new BasicDocument("Sentences?!"), 3, 1, 1);
		testCase(new BasicDocument("Lorem ipsum dolor sit amet, qui ex choro quodsi moderatius, nam dolores explicari forensibus ad."),
		         32, 15, 1);
		
		
	}
	
}
