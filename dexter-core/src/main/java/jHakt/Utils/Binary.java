package jHakt.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe che gestisce la conversione da decimale a binario e viceversa.
 * 
 * @author Giovanni Izzi
 *
 */
public class Binary 
{
	/**
	 * Trasforma il numero decimale in input in binario. Questo numero binario viene restituito 
	 * sotto forma di arraylist di Integer.
	 * 
	 * @param value Numero decimale da convertire in binario.
	 * @return un ArrayList di Integer che contiene la rappresentazione binaria del numero decimale in input.
	 */
	public static ArrayList<Integer> intToBinary(int value)
	{
		ArrayList<Integer> binary = new ArrayList<Integer>();
		String binString = Integer.toBinaryString(value);
		
		for (int i = 0; i < binString.length(); i++)
		{
			char c = binString.charAt(i);
			Integer temp = new Integer(c);
			
			if(temp == 49)
				temp = new Integer(1);
			else
				temp = new Integer(0);
			
			binary.add(temp);	
		}
		
		return binary;
		
	}
	
	/**
	 * Converte un binario in numero decimale.
	 * 
	 * @param binary Numero binario da convertire in decimale.
	 * @return un int che rappresenta il valore decimale del binario in input.
	 */
	public static int listBinToInt(List<Integer> binary)
	{
		int binValue = 0;
		
		int size = binary.size() - 1;
		for (Integer t : binary)
		{
			binValue += (t * Math.pow(2, size));
			size--;
		}
		
		return binValue;
	}
	
	/**
	 * Converte un numero binario, memorizzato come String, in decimale.
	 * 
	 * @param binary Numero binario da convertire in decimale.
	 * @return un int che rappresenta il valore decimale del binario in input.
	 */
	public static int stringBinToInt(String binary)
	{
		int binValue = 0;
		int esp = binary.length() - 1;
		int size = esp + 1;
		
		for(int i = 0; i < size; i++)
		{
			char c = binary.charAt(i);
			Integer temp = new Integer(c);
			
			if(temp == 49)
				temp = new Integer(1);
			else
				temp = new Integer(0);
			
			binValue += (temp * Math.pow(2, esp));
			esp--;
			
		}
		
		return binValue;
	}
	
	/**
	 * Restituisce il numero di bit necessari per convertire il numero decimale in input.
	 * 
	 * @param decimal Numero decimale per il quale si vuole sapere il numero di bit necessari per la conversione in binario.
	 * @return un int che rappresenta il numero di bit necessari per rappresentare il numero decimale in input in binario.
	 */
	public static int bitForInt(int decimal)
	{
		String binS = Integer.toBinaryString(decimal);
		
		return binS.length();
	}
	
}
