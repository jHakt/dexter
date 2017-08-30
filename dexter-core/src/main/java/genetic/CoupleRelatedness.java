package genetic;

public class CoupleRelatedness implements Comparable<CoupleRelatedness>
{
	Gene gene1;
	Gene gene2;
	double relatedness;
	
	CoupleRelatedness(Gene gene1, Gene gene2, double relatedness)
	{
		this.gene1 = gene1;
		this.gene2 = gene2;
		this.relatedness = relatedness;
	}

	Gene getGene1() {
		return gene1;
	}

	Gene getGene2() {
		return gene2;
	}

	double getRelatedness() {
		return relatedness;
	}
	
	boolean geneInCouple(Gene g)
	{
		if(g.equals(this.gene1) || g.equals(this.gene2))
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
}
