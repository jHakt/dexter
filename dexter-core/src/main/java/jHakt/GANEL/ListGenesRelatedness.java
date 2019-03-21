package jHakt.GANEL;

import java.util.ArrayList;

public class ListGenesRelatedness 
{
	private ArrayList<BestRelPairs> genesRelatedness;
	
	public ListGenesRelatedness(int numGenes)
	{
		genesRelatedness = new ArrayList<BestRelPairs>();
		for(int i = 0; i < numGenes; i++)
		{
			BestRelPairs temp = new BestRelPairs();
			genesRelatedness.add(temp);
		}
	}

	public ArrayList<BestRelPairs> getGenesRelatedness() 
	{
		return genesRelatedness;
	}

	public void setGenesRelatedness(ArrayList<BestRelPairs> genesRelatedness) 
	{
		this.genesRelatedness = genesRelatedness;
	}
	
	public void updateIfBest(BestRelPairs pairsGeneI, int posI)
	{
		BestRelPairs pairsI = genesRelatedness.get(posI);
		pairsI.update(pairsGeneI);
	}
	
	public String toString()
	{
		String s = "";
		
		int i = 0;
		for(BestRelPairs brp: genesRelatedness)
		{
			s += "Pos " + i + "\n";
			s += brp.toString();
			
			i++;
		}
		
		return s;
	}
	
}
