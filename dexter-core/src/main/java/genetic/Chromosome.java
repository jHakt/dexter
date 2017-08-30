package genetic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import it.cnr.isti.hpc.dexter.entity.Entity;
import it.cnr.isti.hpc.dexter.entity.EntityMatch;
import it.cnr.isti.hpc.dexter.entity.EntityMatchList;
import it.cnr.isti.hpc.dexter.relatedness.MilneRelatedness;
import it.cnr.isti.hpc.dexter.relatedness.MilneRelatedness2;
import it.cnr.isti.hpc.dexter.spot.SpotMatch;
import it.cnr.isti.hpc.dexter.spot.SpotMatchList;

public class Chromosome implements Comparable<Chromosome>, Iterable<Gene>
{
	/** 
	 * Identificativo univoco del cromosoma.
	 */
	private int id;
	
	/**
	 * Ogni posizione rappresenta l'entità di mapping scelta per la corrispondente menzione.
	 */
	private ArrayList<Gene> genes;
	
	/**
	 * Fitness del cromosoma.
	 */
	private double fitness;
	private double normFitness;
	
	private int rank;
	private double normRank;
	
	private double annealedFitness;

	private SpotMatchList spotMatchList;
	
	/**
	 * Contiene la dimensione del vettore binario di ogni gene.
	 */
	private ArrayList<Integer> dimBinGenes;

	public Chromosome(int id, SpotMatchList sml, ArrayList<Integer> dimGenes)
	{
		this.id = id;
		this.genes = new ArrayList<Gene>();
		this.spotMatchList = sml;
		this.dimBinGenes = dimGenes;
		/*
		int smlSize = sml.size();
		Random rand = new Random();
		for( int i = 0; i < smlSize; i++)
		{
			
			if(this.id == 0)
			{
				genes.add(new Gene(i, 0));
			}
			else
			{
				SpotMatch spot = sml.get(i);
				EntityMatchList eml = spot.getEntities();
				int emlSize = eml.size();
				int genNum = rand.nextInt(emlSize);
				genes.add(new Gene(i, genNum));				
			}
				
		}
		*/
		
		//this.setFitness(calculateFitness());
	}
	
	public void createRandom()
	{
		if (this.genes.size() != 0)
			return;
		
		int smlSize = spotMatchList.size();
		Random rand = new Random();
		for( int i = 0; i < smlSize; i++ )
		{
			int dimBinMax = dimBinGenes.get(i);
			SpotMatch spot = spotMatchList.get(i);
			EntityMatchList eml = spot.getEntities();
			int emlSize = eml.size();
			
			if(this.id == 0)
			{
				genes.add(new Gene(i, 0, emlSize-1, dimBinMax));
			}
			else
			{	
				int genNum = rand.nextInt(emlSize);
				genes.add(new Gene(i, genNum, emlSize-1, dimBinMax));				
			}
				
		}
		
		this.fitness = calculateFitness();
		
	}
	
	public double calculateFitness()
	{
		double fit = 0.0;
		ArrayList<CoupleRelatedness> listRel = new ArrayList<CoupleRelatedness>();
		
		int numGenes = genes.size();
		for( int i = 0; i < numGenes-1; i++ )
		{
			//int genI = genes.get(i);
			Gene genI = genes.get(i);
			SpotMatch spotM = spotMatchList.get(i);
			EntityMatchList candidates = spotM.getEntities();
			//EntityMatch chooseG = candidates.get(genI);
			EntityMatch chooseG = candidates.get(genI.getValue());
			Entity entityI = chooseG.getEntity();
		
			for (int j = i+1; j < numGenes; j++)
			{
				//int genJ = genes.get(j);
				Gene genJ = genes.get(j);
				SpotMatch spotMJ = spotMatchList.get(j);
				EntityMatchList candidateJ = spotMJ.getEntities();
				//EntityMatch chooseGJ = candidateJ.get(genJ);
				EntityMatch chooseGJ = candidateJ.get(genJ.getValue());
				Entity entityJ = chooseGJ.getEntity();
				
				//Istanziamo la classe che calcola la correlazione
				MilneRelatedness2 relIJ = new MilneRelatedness2();
				relIJ.set(entityI.getId(), entityJ.getId());
				double relatedness = relIJ.getScore();
				fit += relatedness;
				
				CoupleRelatedness cr = new CoupleRelatedness(genI, genJ, relatedness);
				listRel.add(cr);
			}
			
			double avFitnessGeneI = calculateFitnessGene(genI, listRel, numGenes);
			genI.setAverageFitness(avFitnessGeneI);
			
		}
		
		//Calcolare la fitness per l'ultimo gene
		Gene last = genes.get(numGenes-1);
		double lastAvFitness = calculateFitnessGene(last, listRel, numGenes);
		last.setAverageFitness(lastAvFitness);
		
		
		return fit;
	}
	
	private double calculateFitnessGene(Gene g, List<CoupleRelatedness> listRel, int numGenes)
	{
		//CAMBIAMENTO
		/*
		 * La fitness media di un gene, che stima la sua bontà, non viene più calcolata come media di tutte le
		 * coppie di correlazione in cui quel gene compare. Questo perché in un testo con molte entità, se la maggior parte
		 * sono sbagliate possono abbassare drasticamente la fitness del gene (che magari è anche una soluzione),
		 * non permettendo la sua successiva annotazione, oppure favorendo un suo cambiamento nel caso di mutazione.
		 */
		
		int divisor = 3;
		int numBestRel = numGenes / divisor;
		
		//Caso limite
		if (numBestRel == 0)
			numBestRel = 1;
		
		ArrayList<CoupleRelatedness> best = new ArrayList<CoupleRelatedness>();
		double fit = 0.0;
		
		for (Iterator<CoupleRelatedness> it = listRel.iterator(); it.hasNext();)
		{
			CoupleRelatedness temp = it.next();
			if (temp.geneInCouple(g))
			{
				best.add(temp);
			}
		}
		
		//Ordiniamo la lista di coppie di correlazione in base alla correlazione.
		best.sort(null);
		for (int i = 0; i < numBestRel; i++)
		{
			CoupleRelatedness temp = best.get(i);
			fit += temp.relatedness;
		}
		
		return fit / numBestRel;
	}
	
	//GETTERS AND SETTERS
	public int getId() { return id; }
	
	public SpotMatchList getSpotMatchList() { return spotMatchList; }
	
	public ArrayList<Gene> getGenes() { return genes; }
	
	public double getAnnealedFitness() { return annealedFitness; }

	public void setAnnealedFitness(double annealedFitness) 
	{
		this.annealedFitness = annealedFitness;
	}
	
	public double getFitness() { return fitness; }

	public void setFitness(double fitness) { this.fitness = fitness; }

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}
	

	public double getNormRank() {
		return normRank;
	}

	public void setNormRank(double normRank) {
		this.normRank = normRank;
	}

	public double getNormFitness() {
		return normFitness;
	}

	public void setNormFitness(double normFitness) {
		this.normFitness = normFitness;
	}

	ArrayList<Integer> getDimBinGenes() { return dimBinGenes; }

	@Override
	public boolean equals(Object o)
	{
		Chromosome c = (Chromosome) o;
		
		if(c.getId() == this.id)
			return true;
		
		return false;
		
	}

	@Override
	public int compareTo(Chromosome o) 
	{
		/*
		if( this.annealedFitness < o.annealedFitness)
			return -1;
		else if (this.annealedFitness > o.annealedFitness)
			return 1;
		
		return 0;
		*/
		
		if (this.fitness < o.fitness)
			return -1;
		else if (this.fitness > o.fitness)
			return 1;
		
		return 0;
		
	}

	@Override
	public Iterator<Gene> iterator() 
	{
		// TODO Auto-generated method stub
		return genes.iterator();
	}
	
	@Override
	public String toString()
	{
		String s = "";
		
		for (Gene g : genes)
		{
			s += g.toString();
		}
		
		s += "\n" + "Fitness: " + fitness + "\t" + "NormFitness: " + normFitness + "\n";
		s += "Rank: " + rank + "\t" + "NormRank: " + normRank + "\n";
		s += "AnnealedFitness: " + annealedFitness + "\n";
		
		return s;
	}

}
