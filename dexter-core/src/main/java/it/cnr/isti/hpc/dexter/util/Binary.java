package it.cnr.isti.hpc.dexter.util;

import java.util.ArrayList;
import java.util.List;

public class Binary 
{
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
	
	public static int bitForInt(int decimal)
	{
		String binS = Integer.toBinaryString(decimal);
		
		return binS.length();
	}
	
}
