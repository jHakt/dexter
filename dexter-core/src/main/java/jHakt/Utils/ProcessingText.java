package jHakt.Utils;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import it.cnr.isti.hpc.dexter.common.Field;

/**
 * This class handles some preprocessing steps of the nif document sent by GERBIL. It can also convert the
 * characters non ascii in a string in ascii char.
 * 
 * @author Giovanni Luca Izzi
 *
 */
public class ProcessingText 
{
	private static final String PLAIN_ASCII =
		      "AaEeIiOoUu"    // grave
		    + "AaEeIiOoUuYy"  // acute
		    + "AaEeIiOoUuYy"  // circumflex
		    + "AaOoNn"        // tilde
		    + "AaEeIiOoUuYy"  // umlaut
		    + "Aa"            // ring
		    + "Cc"            // cedilla
		    + "OoUu";         // double acute
		    

	private static final String UNICODE =
		     "\u00C0\u00E0\u00C8\u00E8\u00CC\u00EC\u00D2\u00F2\u00D9\u00F9"
		    + "\u00C1\u00E1\u00C9\u00E9\u00CD\u00ED\u00D3\u00F3\u00DA\u00FA\u00DD\u00FD"
		    + "\u00C2\u00E2\u00CA\u00EA\u00CE\u00EE\u00D4\u00F4\u00DB\u00FB\u0176\u0177"
		    + "\u00C3\u00E3\u00D5\u00F5\u00D1\u00F1"
		    + "\u00C4\u00E4\u00CB\u00EB\u00CF\u00EF\u00D6\u00F6\u00DC\u00FC\u0178\u00FF"
		    + "\u00C5\u00E5"
		    + "\u00C7\u00E7"
		    + "\u0150\u0151\u0170\u0171";
		  
		  
	public static ArrayList<String> splitNifD2kbDoc(Field field)
	{
		ArrayList<String> splitDocument = new ArrayList<String>();
		String nifDocument = field.getValue();
		
		String split[] = nifDocument.split("&&");
		for(String s: split)
		{
			System.out.println(s);
			splitDocument.add(s);
		}
		
		return splitDocument;
		
	}
	
	public static Set<NifMention> extractMentions(String mentions)
	{
		Set<NifMention> nifMentions = new TreeSet<NifMention>();
		
		String[] arr = mentions.split(";");
		for (String str : arr)
		{
			String[] arr2 = str.split(":");
			String mention = arr2[0];
			String s1 = arr2[1];
			
			String[] arr3 = s1.split(",");
			String s2 = arr3[0];
			String s3 = arr3[1];
			int start = (new Integer(s2)).intValue();
			int end = (new Integer(s3)).intValue();
			
			nifMentions.add(new NifMention(mention, start, end));
			
		}
		
		return nifMentions;
		
	}
	
	public static void checkMentions(String document, Set<NifMention> mentions)
	{
		for(NifMention mention: mentions)
		{
			//boolean different = false;
			String strMention = mention.getMention();
			
			//System.out.println("Mention before analysis: " + strMention);
			
			//int length = strMention.length();
			int start = mention.getStart();
			//int end = mention.getEnd();
			
			String strTrim = strMention.trim();
			int lengthTrim = strTrim.length();
			
			int startTrim;
			//The mention contains spaces at the beginning?
			if(strMention.charAt(0) == strTrim.charAt(0))
				startTrim = start;
			else
			{
				//different = true;
				int i = 0;
				for(char c: strMention.toCharArray())
				{
					if(c != ' ')
						break;
					
					i++;
				}
				
				startTrim = start + i;
			}
			
			int endTrim = startTrim + lengthTrim-1;
			
			//The mention contains some spaces at the end?
			//if(end != endTrim)
				//different = true;
			
			//There is at least one character to the left
			if(startTrim != 0 && Character.isLetter(document.charAt(startTrim-1)))
			{
				int i = 2;
				while( (startTrim - i != 0) && Character.isLetter(document.charAt(startTrim - i)) )
					i++;
				
				startTrim = startTrim - i + 1;
			}
			
			int docLen = document.length();
			if(endTrim <= docLen-1)
			{
				//At the end we have an invalid char
				if(!Character.isLetterOrDigit(document.charAt(endTrim)))
				{
					int i = 1;
					while( !Character.isLetterOrDigit(document.charAt(endTrim - i)) )
						i++;
					
					endTrim = endTrim - i;
				}
				else if( (endTrim + 1) <= docLen-1 && Character.isLetterOrDigit(document.charAt(endTrim + 1)) ) //There is still at least one char
				{
					int i = 2;
					while( (endTrim + i) <= docLen-1 && Character.isLetterOrDigit(document.charAt(endTrim + i)) )
						i++;
					
					endTrim = endTrim + i - 1;
				}
				
			}
			
			String newMention = document.substring(startTrim, endTrim+1).toLowerCase();
			
			/*
			System.out.println("Mention after analysis: " + newMention);
			System.out.println("Orginial Start: " + start);
			System.out.println("Original End: " + end);
			System.out.println("Start: " + startTrim);
			System.out.println("End: " + endTrim);
			*/
			
			//Dexter is based on Ascii mention, so here we handle this situation
			newMention = convertNonAscii(newMention);
			
			//Mention = A. Del Piero -> this spot won't be able to retrieve anything so we will eliminate "A."
			if( newMention.length() >= 3 && Character.isLetter(newMention.charAt(0)) && newMention.charAt(1) == '.' && newMention.charAt(2) == ' ' )
				newMention = newMention.substring(3);
			
			//for the other cases: If the string cointains some '.' that identify an acronym
			newMention = newMention.replaceAll("\\.", "");
			
			newMention = newMention.replaceAll("\\(", "");
			
			mention.setMention(newMention);
			
		}
		
		
	}
	
	// remove accentued from a string and replace with ascii equivalent
	public static String convertNonAscii(String s) 
	{
	    if (s == null) 
	    	return null;
	    
	    StringBuilder sb = new StringBuilder();
	    int n = s.length();
	    
	    for (int i = 0; i < n; i++) 
	    {
	        char c = s.charAt(i);
	        int pos = UNICODE.indexOf(c);
	       
	        if (pos > -1)
	          sb.append(PLAIN_ASCII.charAt(pos));
	        else 
	          sb.append(c);
	     }
	    
	     return sb.toString();
	  }
	
	public static void main(String[] args)
	{
		//String docNif = "BASKETBALL - INTERNATIONAL TOURNAMENT RESULT. BELGRADE 1996-08-30 Result in an international basketball tournament on Friday : Red Star (Yugoslavia) beat Dinamo (Russia) 92-90 (halftime 47-47)&&basketball:0,10;belgrade:46,54;dinamo:154,160;red star:127,135;yugoslavia:137,147;russia:162,168;";
		//String docNif = "The player;A. Del Piero is a Juventus player.&&a. del piero:11,23;";
		String docNif = "Late bond market prices. LONDON 1996-08-30 This is how major world bond markets were trading in late European business on Friday. GERMANY - Bunds extended losses, flirting with session lows after falling victim to sharply higher U.S. economic data which revived fears that interest rates may soon turn higher. The September Bund future on the London International "
				+ "Financial Futures and Options Exchange (LIFFE) was trading at 97.18, down 0.20 from Thursday's settlement price. BRITAIN - Gilts struggled off the day's lows but ended 10/32 down on the day. A sharp plunge in U.S. Treasuries after a shock rise in the Chicago PMI pulled gilts lower, but traders said the market was nervous anyway ahead of August MO data and the PMI survey due "
				+ "on Monday. The September long gilt future on LIFFE was trading at 107-2/32, down 8/32 from Thursday's settlement price. FRANCE - Bond and PIBOR futures ended the day higher despite much stronger than expected U.S. data. The September notional bond future on the MATIF in Paris settled at 123.14, up 0.04 from Thursday's settlement price. ITALY - Bond futures held to easier levels "
				+ "in late afternoon after the sharp drop in Treasuries, but a resilient lira helped limit BTP losses. The September bond future on LIFFE was trading at 115.45, down 0.13 from Thursday's settlement price. UNITED STATES - Prices of U.S. Treasury securities were trading sharply lower near midday after a surprisingly strong Chicago Purchasing Managers ' report shook the markets ahead "
				+ "of the long Labour Day weekend. The September Treasury bond future on the Chicago Board of Trade was trading at 107-11/32, down 26/32 from Thursday's settlement price. The long bond was quoted to yield 7.12 percent. JAPAN - Yield for benchmark 182nd cash bond fell on buy-backs following weaker-than-expected industrial output data, which convinced traders the BOJ would not raise "
				+ "interest rates soon. Japanese Goverment Bonds futures which closed before the output data, lost much of day's gains as Tokyo stock prices recovered from the day's low.In after hours trading the September future on LIFFE was trading at 122.53, up 0.26 from Thursday's settlement price on the Tokyo Stock Exchange. EUROBONDS - Primary market activity was sharply lower, as players wound "
				+ "down ahead of Monday's U.S. Labour Day holiday and next week's U.S. employment data. NSW Treasury launched a A$ 100 million three-year discount bond aimed at Japanese investors. DNIB issued a 275 million Norwegian crown bond, which was pre-placed with a European institution. DNIB also set a 110 million guilder step-up bond. Next week Kansai Electric Power and Kansai International Airport are likely to launch 10-year dollar deals.&&"
				+ "labour day:2297,2307;dnib:2545,2549;labour day:1515,1525;u.s.:950,954;u.s. treasury:1350,1363;u.s. treasuries:573,588;u.s.:2292,2296;kansai electric power:2605,2626;kansai international airport:2631,2659;tokyo:2003,2008;liffe:404,409;france:861,867;chicago board of trade:1577,1599;japan:1719,1724;u.s.:229,233;a$:2378,2380;boj:1864,1867;london international financial futures and options "
				+ "exchange:343,402;united states:1324,1337;paris:1012,1017;japanese goverment bonds:1905,1929;italy:1079,1084;european:101,109;london:25,31;liffe:1251,1256;liffe:2098,2103;britain:477,484;u.s.:2332,2336;japanese:2427,2435;chicago pmi:615,626;treasury:1549,1557;dnib:2447,2451;chicago purchasing managers:1442,1469;germany:130,137;nsw treasury:2354,2366;eurobonds:2197,2206;norwegian:2473,2482;"
				+ "treasuries:1164,1174;liffe:786,791;european:2523,2531;tokyo stock exchange:2175,2195;";
		Field f = new Field("q1", docNif);
		ArrayList<String> splitDoc = splitNifD2kbDoc(f);
		String document = splitDoc.get(0);
		String splitNif = splitDoc.get(1);
	
		
		Set<NifMention> mentions = extractMentions(splitNif);
		
		for(NifMention mention: mentions)
		{
			System.out.println(mention.getMention());
		}
		
		checkMentions(document, mentions);
		
		for(NifMention mention: mentions)
		{
			System.out.println(mention.getMention());
		}
		
		String test = "U.S. Open";
		System.out.println(test.replaceAll("\\.", ""));
		System.out.println(convertNonAscii("\u0026\u0026"));
		
	}
	
}
