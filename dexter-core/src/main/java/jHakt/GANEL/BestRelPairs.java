package jHakt.GANEL;

import java.util.ArrayList;

public class BestRelPairs 
{
	private ArrayList<CoupleRelatedness> bestPairs;
	private double avgBestRelatedness;
	
	public BestRelPairs()
	{
		bestPairs = new ArrayList<CoupleRelatedness>();
		avgBestRelatedness = 0.0;
	}

	public ArrayList<CoupleRelatedness> getBestPairs() 
	{
		return bestPairs;
	}

	void setBestPairs(ArrayList<CoupleRelatedness> bestPairs) 
	{
		this.bestPairs = bestPairs;
	}

	public double getBestRelatedness() 
	{
		return avgBestRelatedness;
	}

	public void setBestRelatedness(double bestRelatedness) 
	{
		this.avgBestRelatedness = bestRelatedness;
	}
	
	public void addPair(CoupleRelatedness couple)
	{
		bestPairs.add(couple);
	}
	
	public void update(BestRelPairs brp)
	{
		if(brp.avgBestRelatedness > this.avgBestRelatedness)
		{
			this.bestPairs = brp.bestPairs;
			this.avgBestRelatedness = brp.avgBestRelatedness;
		}
		
	}
	
	public String toString()
	{
		String s = "Best Pairs:\n";
		
		for(CoupleRelatedness cr: bestPairs)
		{
			s += cr.toString();
		}
		
		return s;
	}

}
