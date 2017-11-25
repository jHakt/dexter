package genetic;

import java.util.ArrayList;
import java.util.Random;

import it.cnr.isti.hpc.dexter.util.Binary;

public class Gene implements Cloneable
{
	/**
	 * Posizione all'interno del cromosoma.
	 */
	private int pos;
	
	/**
	 * Valore del gene. Un intero che rappresenta l'entità di mappatura scelta.
	 */
	private int value;
	
	private int maxValue;
	
	/**
	 * Rappresentazione in binario di value.
	 */
	private ArrayList<Integer> binary;
	
	private int maxDimBin;
	
	/**
	 * Fitness medio di questo gene, calcolata come media delle coppie di correlazione dove compare questo gene.
	 */
	private double averageFitness;
	
	public Gene(int pos, int value, int maxValue, int dimMaxBin)
	{
		this.pos = pos;
		this.value = value;
		this.maxValue = maxValue;
		this.maxDimBin = dimMaxBin;
		this.binary = Binary.intToBinary(value);
		
		//Se mancano delle cifre a sinistra viene completato
		if(binary.size() < maxDimBin)
			autoCompleteBinary();
		
	}
	
	public Gene(int pos) { this.pos = pos; }
	
	//Private for copy
	private Gene() { }

	//Getters and setters
	public double getAverageFitness() { return averageFitness; }

	void setAverageFitness(double averageFitness) 
	{
		this.averageFitness = averageFitness;
	}

	public int getPos() { return pos; }

	public int getValue() { return value; }

	public void setValue(int value) { this.value = value; }
	
	ArrayList<Integer> getBinary() { return binary; }
	
	void setBinary(ArrayList<Integer> binary) 
	{ 
		this.binary = binary; 
		
		if(this.binary.size() < maxDimBin)
			autoCompleteBinary();
		
	}
	
	//CROSSOver tra geni
	static void crossoverGenes(Gene gene1, Gene gene2)
	{
		ArrayList<Integer> binG1 = gene1.binary;
		ArrayList<Integer> binG2 = gene2.binary;
		Random random = new Random();
		
		//Nuovi binari
		ArrayList<Integer> bin1 = new ArrayList<Integer>();
		ArrayList<Integer> bin2 = new ArrayList<Integer>();
		
		int sizeBin = binG1.size();
		//int crossType = random.nextInt(2);
		
		//CrossOver Standard uniforme
		/*
		if (crossType == 0)
		{
		*/
			for( int i = 0; i < sizeBin; i++ )
			{
				Integer n1 = binG1.get(i);
				Integer n2 = binG2.get(i);
			
				int rand = random.nextInt(2);
				if (rand == 0)
				{
					bin1.add(n1);
					bin2.add(n2);
				}
				else
				{
					bin1.add(n2);
					bin2.add(n1);
				}
			
			}
		/*}
		else
		{
			//Nuovo CrossOver
			
			for( int i = 0; i < sizeBin; i++ )
			{
				Integer n1 = binG1.get(i);
				Integer n2 = binG2.get(i);
				
				if (n1 == 0 && n2 == 1)
				{
					n1 = new Integer(random.nextInt(2));
					n2 = new Integer(random.nextInt(2));
				}
				else if (n1 == 1 && n2 == 0)
				{
					n1 = new Integer(1);
					n2 = new Integer(1);
				}
				else if (n1 == 0 && n2 == 0)
				{
					int rN1 = random.nextInt(2);
					
					if (rN1 == 1)
					{
						n1 = new Integer(rN1);
					}
					else
					{
						n2 = new Integer(random.nextInt(2));
					}
				}
				else
				{
					//Caso in cui entrambi 1
					n1 = new Integer(0);
					n2 = new Integer(0);
				}
				
				bin1.add(n1);
				bin2.add(n2);

			}
			
		} */
		
		//Seconda Modifica CrossOver
		int binDec = Binary.listBinToInt(bin1);
		if(binDec > gene1.maxValue)
		{
			//binDec = gene1.maxValue;
			//System.out.println("\n MaxValue: " + gene1.maxValue + "\n");
			int rand = random.nextInt(2);
			if (gene1.maxValue == 0)
				binDec = gene1.maxValue;
			else if (rand == 0)
				binDec = gene1.maxValue;
			else
				binDec = random.nextInt(gene1.maxValue);
			
			bin1 = Binary.intToBinary(binDec);
				
		}
		gene1.setBinary(bin1);
		gene1.setValue(binDec);
		
		binDec = Binary.listBinToInt(bin2);
		if(binDec > gene2.maxValue)
		{
			//binDec = gene2.maxValue;
			//System.out.println("\n MaxValue: " + gene1.maxValue + "\n");
			int rand = random.nextInt(2);
			if (gene2.maxValue == 0)
				binDec = gene1.maxValue;
			else if (rand == 0)
				binDec = gene2.maxValue;
			else
				binDec = random.nextInt(gene2.maxValue);
			
			bin2 = Binary.intToBinary(binDec);
		}
		gene2.setBinary(bin2);
		gene2.setValue(binDec);
		
	}
	
	//Mutazione di un gene, tolto lo static
	void mutationGene()
	{
		//Mutazione randomica per ogni bit
		Random random = new Random();
		for( int i = 0; i < maxDimBin; i++ )
		{
			int rand = random.nextInt(2);
			binary.set(i, rand);
		}
		
		//Aggiorniamo il valore intero dopo la mutazione
		int newValue = Binary.listBinToInt(binary);
		if(newValue > maxValue)
		{
			newValue = maxValue;
			binary = Binary.intToBinary(newValue);
		}
		this.setValue(newValue);
	}
	
	private void autoCompleteBinary()
	{
		int size = binary.size();
		while(size < maxDimBin)
		{
			//Aggiunge in pos 0, l'intero 0
			binary.add(0, 0);
			size++;
		}
		
	}
	

	public boolean equals(Object o)
	{
		Gene g = (Gene) o;
		if (this == g)
			return true;
		if(this.pos == g.pos && this.value == g.value)
			return true;
		
		return false;
	}
	
	@Override
	public String toString()
	{
		return "Gene in pos: " + pos + "\nValore: " + value + "\nAverageFitness: " + averageFitness + "\n";
	}
	
	/**
	 * Makes a deep copy of this Gene.
	 */
	@Override
	public Object clone()
	{
		Gene g = new Gene();
		g.pos = this.pos;
		g.value = this.value;
		g.maxValue = this.maxValue;
		g.maxDimBin = this.maxDimBin;
		g.binary = new ArrayList<Integer>();
		
		for (int i = 0; i < maxDimBin; i++)
		{
			Integer old = this.binary.get(i);
			Integer n = new Integer(old.intValue());
			g.binary.add(n);
		}
				
		return g;
	}

}
