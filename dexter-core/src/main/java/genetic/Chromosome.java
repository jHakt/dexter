package genetic;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import info.debatty.java.stringsimilarity.JaroWinkler;
import info.debatty.java.stringsimilarity.NormalizedLevenshtein;
import it.cnr.isti.hpc.dexter.entity.Entity;
import it.cnr.isti.hpc.dexter.entity.EntityMatch;
import it.cnr.isti.hpc.dexter.entity.EntityMatchList;
import it.cnr.isti.hpc.dexter.label.IdHelper;
import it.cnr.isti.hpc.dexter.label.IdHelperFactory;
import it.cnr.isti.hpc.dexter.relatedness.MilneRelatedness;
import it.cnr.isti.hpc.dexter.relatedness.MilneRelatedness2;
import it.cnr.isti.hpc.dexter.spot.Spot;
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
	
	private IdHelper helper = IdHelperFactory.getStdIdHelper();

	public Chromosome(int id, SpotMatchList sml, ArrayList<Integer> dimGenes)
	{
		this.id = id;
		this.genes = new ArrayList<Gene>();
		this.spotMatchList = sml;
		this.dimBinGenes = dimGenes;
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
		ArrayList<CoupleRelatedness> listRel = allCouple();
		
		int numGenes = genes.size();
		for (Gene g : genes)
		{
			fit += calculateFitnessGene(g, listRel, numGenes);
		}
		
		return fit;
	}
	
	private ArrayList<CoupleRelatedness> allCouple()
	{
		ArrayList<CoupleRelatedness> listRel = new ArrayList<CoupleRelatedness>();
		
		int numGenes = genes.size();
		String json = "[";
		for( int i = 0; i < numGenes-1; i++ )
		{
			Gene genI = genes.get(i);
			SpotMatch spotM = spotMatchList.get(i);
			//Spot sI = spotM.getSpot();
			//String mention1 = sI.getMention();
			EntityMatchList candidates = spotM.getEntities();
			EntityMatch chooseG = candidates.get(genI.getValue());
			Entity entityI = chooseG.getEntity();
		
			for (int j = i+1; j < numGenes; j++)
			{
				Gene genJ = genes.get(j);
				SpotMatch spotMJ = spotMatchList.get(j);
				//Spot sJ = spotMJ.getSpot();
				//String mention2 = sJ.getMention();
				EntityMatchList candidateJ = spotMJ.getEntities();
				EntityMatch chooseGJ = candidateJ.get(genJ.getValue());
				Entity entityJ = chooseGJ.getEntity();
				
				//Istanziamo la classe che calcola la correlazione
				//MilneRelatedness2 relIJ = new MilneRelatedness2();
				int id1 = entityI.getId();
				int id2 = entityJ.getId();
				//relIJ.set(id1, id2);
				//double relatedness = relIJ.getScore();
				
				//2
				//MilneRelatedness2 relJI = new MilneRelatedness2();
				//relJI.set(id2, id1);
				//double relatedness2 = relJI.getScore();
				
				
				//Similarity
				//JaroWinkler jw = new JaroWinkler();
				//NormalizedLevenshtein l = new NormalizedLevenshtein();
				//double distance1 = jw.similarity(mention1, wikiname1);
				//double distance2 = jw.similarity(mention2, wikiname2);
				//double distance1 = 1 - l.distance(mention1, wikiname1);
				//double distance2 = 1 - l.distance(mention2, wikiname2);
				
				//new double
				//double relSim = (relatedness + distance1 + distance2) / 3;
				
				//CoupleRelatedness cr = new CoupleRelatedness(genI, genJ, id1, id2, relatedness);
				CoupleRelatedness cr = new CoupleRelatedness(genI, genJ, id1, id2);
				listRel.add(cr);
				
				//CoupleRelatedness cr2 = new CoupleRelatedness(genJ, genI, id2, id1, relatedness2);
				CoupleRelatedness cr2 = new CoupleRelatedness(genJ, genI, id2, id1);
				listRel.add(cr2);
			}
		}
		
		
		String json1 = json.substring(0, json.length()-1);
		json1 = json1 + "]";
		
		//Richiesta POST
		try
		{
			URL url = new URL("http://193.204.187.35:9002/nesim/batchsim");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			//String s = "[\"9343\",\"343221\",\"343\",\"33221\",\"93432\",\"34321\",\"432\",\"5645\"]";
			wr.writeBytes(json1);
			wr.flush();
			wr.close();
		
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
			String inputLine = in.readLine();
			String subInput = inputLine.substring(1, inputLine.length()-1);
    		String [] arrInput = subInput.split(",");
    		
    		int i = 0;
    		for (String d : arrInput)
    		{
    			double temp = Double.parseDouble(d);
    			CoupleRelatedness cTemp = listRel.get(i);
    			cTemp.setRelatedness(temp);
    			
    			i++;
    		}
		}
		catch (Exception ex) 
		{
			Logger.getLogger(Chromosome.class.getName()).log(Level.SEVERE, null, ex);
	    }
		
		
		return listRel;
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
		//Modifica numBest al più 2.
		if (numBestRel >= 2)
			numBestRel = 2;
			
		
		ArrayList<CoupleRelatedness> best = new ArrayList<CoupleRelatedness>();
		//Solo per le coppie migliori dove appare il gene.
		double fitBestG = 0.0;
		
		//Per tutte le coppie di correlazioni dove appare il gene.
		double fitG = 0.0;
		
		for (Iterator<CoupleRelatedness> it = listRel.iterator(); it.hasNext();)
		{
			CoupleRelatedness temp = it.next();
			if (temp.geneInCouple(g))
			{
				best.add(temp);
				fitG += temp.getRelatedness();
			}
		}
		
		//Ordiniamo la lista di coppie di correlazione in ordine decrescente in base alla correlazione.
		best.sort(null);
		
		for (int i = 0; i < numBestRel; i++)
		{
			CoupleRelatedness temp = best.get(i);
			fitBestG += temp.relatedness;
		}
		
		double avFit = fitBestG / numBestRel;
		g.setAverageFitness(avFit);

		return fitG;
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

	public ArrayList<Integer> getDimBinGenes() { return dimBinGenes; }

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
