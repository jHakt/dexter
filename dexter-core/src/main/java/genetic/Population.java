package genetic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import it.cnr.isti.hpc.dexter.spot.SpotMatchList;

/**
 * Modella l'entit&agrave; Popolazione dell'algoritmo genetico. Una popolazione &egrave; costituita da un certo 
 * numero di individui (cromosomi) che rappresentano le soluzioni candidate per il problema in esame.
 * 
 * @author Giovanni Izzi
 *
 */
public class Population implements Iterable<Chromosome>
{
	/** 
	 * Lista che contiene tutti i membri della popolazione.
	 */
	private ArrayList<Chromosome> population;
	
	/**
	 * Dimensione della popolazione. Di default 50.
	 */
	private int maxPopDim;
	
	/**
	 * Dimensione attuale della popolazione
	 */
	private int popDim;
	
	/**
	 * Miglior cromosoma di questa popolazione.
	 */
	private Chromosome bestChromosome;
	
	/**
	 * Secondo miglior cromosoma di questa popolazione.
	 */
	private Chromosome secondBestChromosome;
	
	/**
	 * Miglior fintess associata al miglior cromosoma di questa popolazione.
	 */
	private double bestFitness;
	
	/**
	 * Costruttore di default. Imposta la dimensione massima della popolaziona a 50 individui.
	 */
	public Population()
	{
		population = new ArrayList<Chromosome>();
		maxPopDim = 50;
		popDim = 0;
	}
	
	/**
	 * Costruttore parametrizzato. Permette di usare una popolazione di dimensione differente rispetto a quella di default.
	 * 
	 * @param dim Dimensione della popolazione.
	 */
	public Population(int dim)
	{
		population = new ArrayList<Chromosome>();
		maxPopDim = dim;
		popDim = 0;
	}
	
	/**
	 * Aggiunge il cromosoma in input in questa popolazione.
	 * 
	 * @param c Cromosoma che si vuole aggiungere in questa popolazione.
	 */
	public void addToPopulation(Chromosome c)
	{
		if (popDim < maxPopDim)
		{
			population.add(c);
			popDim++;
		}
		
	}
	
	/**
	 * Rimuove il cromosoma in input da questa popolazione.
	 * 
	 * @param c Cromosoma che si vuole rimuovere dalla popolazione.
	 */
	public void removeFromPopulation(Chromosome c)
	{
		if (!population.isEmpty())
		{
			population.remove(c);
			popDim--;
		}
		
	}
	
	//Getters
	public int getMaxPopDim() { return maxPopDim; }

	public int getPopDim() { return popDim; }
	
	public Chromosome getBestChromosome() { return bestChromosome; }

	public Chromosome getSecondBestChromosome() {
		return secondBestChromosome;
	}

	public double getBestFitness() { return bestFitness; }

	@Override
	public Iterator<Chromosome> iterator() 
	{
		return population.iterator();
	}
		
	@Override
	public String toString()
	{
		String s = "";
			
		for (Chromosome c : population)
		{
			s += "Chromosome " + c.getId() + "\n";
			s += c.toString();
			s += "\n";
				
		}
		
		if (bestChromosome != null)
		{
			s += "Best Chromosome: " + bestChromosome.getId() + "\n";
			s += "Best Fitness: " + bestFitness + "\n\n";
		}
			
		return s;
	}
		
	
	//SERIE DI METODI PER CALCOLARE LE FITNESS
	/**
	 * Metodo privato di supporto al metodo calculateAnnealedFitness. 
	 * Calcola il rank di ogni cromosoma presente in questa popolazione.
	 */
	private void calculateRank()
	{
		this.population.sort(null);
		int i = 1;
		for (Chromosome c : population)
		{
			c.setRank(i);
			i++;
		}
		
		calcNormRank();
		
	}
	
	/**
	 * Metodo privato di supporto al metodo calculateAnnealedFitness. Normalizza il rank di ogni 
	 * cromosoma calcolato dal metodo calculateRank.
	 */
	private void calcNormRank()
	{
		double sumRank = 0;
		for (Chromosome c : this.population)
		{
			sumRank += c.getRank();
		}
		
		for (Chromosome c : this.population)
		{
			double norm = c.getRank() / sumRank;
			c.setNormRank(norm);
		}
		
	}
	
	/*
	public void updateFitness(List<Chromosome> offspring)
	{
		//this.setFitness(calculateFitness());
		
		for (Chromosome c : population)
		{
			double fitness = c.calculateFitness();
			c.setFitness(fitness);
		}
		calcNormFitness();
		
		
		
	} */
	
	/**
	 * Metodo privato di supporto al metodo calculateAnnealedFitness. Normalizza la fitness 
	 * di ogni cromosoma presente in questa popolazione.
	 */
	private void calcNormFitness()
	{
		double sumFitness = 0.0;
		for (Chromosome c : this.population)
		{
			sumFitness += c.getFitness();
		}
		
		for (Chromosome c : this.population)
		{
			double norm = c.getFitness() / sumFitness;
			if (Double.isNaN(norm))
			{
				norm = 0;
			}
			c.setNormFitness(norm);
		}
		
	}
	
	/**
	 * Calcola la annealedFitness di ogni cromosoma presente in questa popolazione. La fitness annealed si calcola 
	 * con la seguente formula:<br><br> 
	 * FW = FitnessRank * ra + Fitness * rb<br><br> 
	 * Dove:<br> 
	 * ra = ha come valore iniziale "1" e viene diminuto ad ogni generazione di 1/maxGen<br> 
	 * rb = ha come valore iniziale "0" e viene incrementato ad ogni generazione di 1/maxGen
	 * 
	 * @param ra Parametro della fitness annealed.
	 * @param rb Parametro della fitness annealed.
	 */
	public void calculateAnnealedFitness(double ra, double rb)
	{
		//this.calculateFitness();
		this.calcNormFitness();
		this.calculateRank();
		
		for (Chromosome c : population)
		{
			double annealedFit = c.getNormRank() * ra + c.getNormFitness() * rb;
			c.setAnnealedFitness(annealedFit);
		}
		
	}
	
	// FINE METODI PER CALCOLARE FITNESS
	/**
	 * Applica l'operatore di crossover ai due cromosomi in input. Scorre i due cromosomi e 
	 * applica l'operatore di crossover ai geni corrispondenti. Per ulteriori informazioni vedere la classe Gene.
	 * 
	 * @param parent1 Primo cromosoma genitore.
	 * @param parent2 Secondo cromosoma genitore.
	 * @return un ArrayList che contiene la prole generata da questo operatore.
	 * @see Gene
	 */
	public static ArrayList<Chromosome> crossover(Chromosome parent1, Chromosome parent2)
	{
		ArrayList<Chromosome> newOffspring = new ArrayList<Chromosome>();
		//Random random = new Random();
		
		//CAMBIAMENTO, inserimento random
		//Altro cambiamento, geni con rappresentazione binaria
		
		//I geni dei genitori
		ArrayList<Gene> genesP1 = parent1.getGenes();
		ArrayList<Gene> genesP2 = parent2.getGenes();
		SpotMatchList sml = parent1.getSpotMatchList();
		ArrayList<Integer> dimBinGenes = parent1.getDimBinGenes();
		
		//i due nuovi figli da costruire
		Chromosome off1 = new Chromosome(parent1.getId(), sml, dimBinGenes);
		Chromosome off2 = new Chromosome(parent2.getId(), sml, dimBinGenes);
		
		//I geni dei figli, vuoti all'inizio
		ArrayList<Gene> genesOff1 = off1.getGenes();
		ArrayList<Gene> genesOff2 = off2.getGenes();
		
		//La size è uguale per entrambi
		int sizeGenes = genesP1.size();
		for (int i = 0; i < sizeGenes; i++)
		{
			Gene old1 = genesP1.get(i);
			Gene old2 = genesP2.get(i);
			
			Gene new1 = (Gene) old1.clone();
			Gene new2 = (Gene) old2.clone();
			
			Gene.crossoverGenes(new1, new2);
			genesOff1.add(new1);
			genesOff2.add(new2);
			
		}
		
		newOffspring.add(off1);
		newOffspring.add(off2);
		
		return newOffspring;
	}
	
	/**
	 * Operatore di mutazione. Viene applicato in accordo ad un numero random generato. Vengono modificati 
	 * solo i geni la cui average fitness &egrave; minore di 0,5. Per ulteriori dettagli vedere la classe Gene.
	 * 
	 * @param offspring Prole generata dall'operatore di crossover.
	 * @param probMutation Tasso di mutazione.
	 * @see Gene
	 */
	public static void mutation(ArrayList<Chromosome> offspring, double probMutation)
	{
		Random random = new Random();
		boolean change = false;
		
		double rand = random.nextDouble();
		if (rand <= probMutation)
		{
			//Viene mutato il primo elemento di offspring
			Chromosome off1 = offspring.get(0);
			for (Gene g : off1)
			{
				if (g.getAverageFitness() < 0.5)
				{
					change = true;
					/*
					int pos = g.getPos();
					SpotMatch sm = sml.get(pos);
					EntityMatchList eml = sm.getEntities();
					int valueRandom = random.nextInt(eml.size());
					
					g.setValue(valueRandom);
					*/
					g.mutationGene();
				}
			}
			
			if(change == true)
			{
				off1.setFitness(off1.calculateFitness());
			}
			
		}
		
		change = false;
		rand = random.nextDouble();
		if (rand <= probMutation)
		{
			//Viene mutato il secondo elemento di offspring
			Chromosome off2 = offspring.get(1);
			for (Gene g : off2)
			{
				if (g.getAverageFitness() < 0.5)
				{
					change = true;
					g.mutationGene();
				}
			}
			
			if (change == true)
			{
				off2.setFitness(off2.calculateFitness());
			}
		}
		
	}
	
	/**
	 * Ritrova nella popolazione i due migliori cromosomi.
	 */
	public void best()
	{
		int popSize = population.size();
		this.bestChromosome = population.get(popSize - 1);
		this.bestFitness = bestChromosome.getFitness();
		this.secondBestChromosome = population.get(popSize - 2); 	
	}
	
}
