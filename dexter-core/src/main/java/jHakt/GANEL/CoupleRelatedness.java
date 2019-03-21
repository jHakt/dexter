package jHakt.GANEL;

/**
 * Modella l'entit&agrave; coppia di correlazione (e1, e2), dove "e" &egrave; un'entit&agrave; candidata.
 * 
 * @author Giovanni Izzi
 *
 */
public class CoupleRelatedness implements Comparable<CoupleRelatedness>
{
	/**
	 * Primo gene della coppia.
	 */
	Gene gene1;
	
	/**
	 * Secondo gene della coppia.
	 */
	Gene gene2;
	
	/**
	 * ID di wikipedia dell'entit&agrave; associato al gene1.
	 */
	int id1;
	
	/**
	 * ID di wikipedia dell'entit&agrave; associata al gene2.
	 */
	int id2;
	
	/**
	 * Correlazione tra gene1 e gene2.
	 */
	double relatedness; 
	
	/**
	 * Costruttore di classe, avvalora gli attributi di classe.
	 * 
	 * @param gene1 Primo gene nella coppia.
	 * @param gene2 Secondo gene nella coppia.
	 * @param id1 ID di wikipeedia del primo gene.
	 * @param id2 ID di wikipedia del secondo gene.
	 * @param relatedness Correlazione tra gene1 e gene2.
	 */
	CoupleRelatedness(Gene gene1, Gene gene2, int id1, int id2, double relatedness)
	{
		this.gene1 = gene1;
		this.gene2 = gene2;
		this.id1 = id1;
		this.id2 = id2;
		this.relatedness = relatedness;
	}
	
	/**
	 * Costruttore di classe, utile per la secondo funzione di correlazione.
	 * 
	 * @param gene1 Primo gene nella coppia.
	 * @param gene2 Secondo gene nella coppia.
	 * @param id1 ID di wikipeedia del primo gene.
	 * @param id2 ID di wikipedia del secondo gene.
	 */
	CoupleRelatedness(Gene gene1, Gene gene2, int id1, int id2)
	{
		this.gene1 = gene1;
		this.gene2 = gene2;
		this.id1 = id1;
		this.id2 = id2;
	}

	Gene getGene1() { return gene1; }

	Gene getGene2() { return gene2; }

	public double getRelatedness() { return relatedness; }
	
	void setRelatedness(double relatedness)
	{
		this.relatedness = relatedness;
	}
	
	//Change: A gene is in a couple only if it is a gene1, otherwise we will consider two times the correlation
	boolean geneInCouple(Gene g)
	{
		//if(g.equals(this.gene1) || g.equals(this.gene2))
		if(g.equals(this.gene1))
			return true;
		
		return false;
	}

	@Override
	public int compareTo(CoupleRelatedness o) 
	{
		if (this.relatedness > o.relatedness)
			return -1;
		else if (this.relatedness < o.relatedness)
			return 1;
		
		return 0;
	
	}
	
	public String toString()
	{
		String s = "Couple:\n";
		
		s += "Gene1 pos: " + gene1.getPos() + " id: " + this.id1 + "\n";
		s += "Gene2 pos: " + gene2.getPos() + " id: " + this.id2 + "\n";
		
		s += "\n";
		
		return s;
		
	}
	
}
