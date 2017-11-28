package it.cnr.isti.hpc.dexter.disambiguation;

import java.util.ArrayList;
import java.util.Random;

import ganel.Chromosome;
import ganel.Gene;
import ganel.Population;
import it.cnr.isti.hpc.dexter.entity.EntityMatch;
import it.cnr.isti.hpc.dexter.entity.EntityMatchList;
import it.cnr.isti.hpc.dexter.spot.SpotMatch;
import it.cnr.isti.hpc.dexter.spot.SpotMatchList;
import it.cnr.isti.hpc.dexter.util.Binary;
import it.cnr.isti.hpc.dexter.util.DexterLocalParams;
import it.cnr.isti.hpc.dexter.util.DexterParams;

/**
 * Un disambiguatore che implementa l'algoritmo GANEL, un algortimo genetico per il task di entity linking.
 * 
 * <br><br>GANEL: <br><br>
 * <strong>begin</strong><br>
 * &emsp;	Set t = 0, probCross = 0.8, probMut = 0.6, maxGen = 35, gen = 0 <br>
 * &emsp;	Set ra = 1, rb = 0 <br>
 * &emsp;	P(t) <- Construct initial population <br>
 * &emsp;	Calculate Annealed Fitness <br>
 * &emsp;	<strong>while</strong> ( gen < maxGen ) <strong>do</strong> <br>
 * &emsp;&emsp;				P(t+1) <- Create a new empty population <br>
 * &emsp;&emsp;				Put first two best chromosomes from P(t) in P(t+1) <br>
 * &emsp;&emsp;				Select 'size (P(t)) - 2 chromosomes from P(t)' <br>
 * &emsp;&emsp;				Add these chromosomes to the mating pool <br>
 * &emsp;&emsp;				<strong>while</strong> ( mating pool is not empty ) <strong>do</strong> <br>
 * &emsp;&emsp;&emsp;							Choose two chromosomes from mating pool <br>
 * &emsp;&emsp;&emsp;							Apply crossover operator <br>
 * &emsp;&emsp;&emsp;							Apply mutation operator <br>
 * &emsp;&emsp;							Put offsprings in P(t+1) <br>
 * &emsp;&emsp;				ra <- ra - 1 / maxGen <br>
 * &emsp;&emsp;				rb <- rb + 1 / maxGen <br>
 * &emsp;&emsp;				probMut <- probMut - 0.5 / maxGen <br>
 * &emsp;&emsp;				P(t) <- P(t+1) <br>
 * &emsp;&emsp;				t <- t + 1 <br>
 * &emsp;	Take the best chromosome from the final population <br>
 * &emsp;	Validate this chromosome as solution end <br>
 * <strong>end</strong>
 *  
 * @author Giovanni Izzi 
 *
 */
public class GeneticDisambiguator implements Disambiguator 
{
	/**
	 * Il mating pool.
	 */
	private ArrayList<Chromosome> matingPool = new ArrayList<Chromosome>();
	
	/**
	 * All'inzio ordina la SpotMatchList sml e poi usa l'algoritmo GANEL per generare la soluzione.
	 * 
	 * @return EntityMatchList che rappresenta la soluzione generata da GANEL.
	 */
	@Override
	public EntityMatchList disambiguate(DexterLocalParams requestParams, SpotMatchList sml) 
	{
		
		int smlSize = sml.size();
		
		//EntityMatchList eml = new EntityMatchList();
		/*
		 * Parte iniziale: ordino tutte le entità candidate per ogni spot (menzione)
		 * in base a uno score/probabilità a priori.
		 */
		for (SpotMatch sm : sml)
		{
			EntityMatchList order = sm.getEntities();
			order.sort();
		}
		
		EntityMatchList eml = null;
		if ( !(smlSize == 1) )
		{
			// Bisogna costruire la popolazione iniziale
			Population gen_0 = constructPopulation(sml);
			Population prev = gen_0;
		
			//Parametri dell'algoritmo
			double probCross = 0.8;
			double probMut = 0.6;
			int gen = 0;
			int maxGen = 35;
			double ra = 1;
			double rb = 0;
			//double threshold = 0.6;
			int dimMaxPop = gen_0.getMaxPopDim();
			 
			//fitness migliore nelle ultime due generazioni
			//double bestFit1 = 0.0;
			//double bestFit2 = 0.0;
		
			//Per la popolazione iniziale
			calculateAnnealedFitness(prev, ra, rb);
			prev.best();
			
			//bestFit1 = prev.getBestFitness();
		
			//Effettuiamo una stampa della popolazione iniziale
			//System.out.println("Generazione: " + gen + "\n" + gen_0);
		
			//L'algoritmo termina quando si raggiunge il numero massimo di generazioni oppure 
			//quando si supera un criterio di soglia.
			while(gen < maxGen) //|| threshold <= prev.getBestFitness()))
			{	
				Population gen_i = new Population(dimMaxPop);
			
				//La nuova popolazione è vuota, dalla precedente estraggo i due migliori individui e li inserisco
				Chromosome best1 = prev.getBestChromosome();
				gen_i.addToPopulation(best1);
			
				Chromosome best2 = prev.getSecondBestChromosome();
				gen_i.addToPopulation(best2);
			
				//System.out.println("Popolazione Generazione " + (gen + 1) + "Parziale: ");
				//System.out.println(gen_i);
			
				selection(prev, dimMaxPop-2); // -2 perché ho aggiunto i due migliori della popolazione precedente
				while (matingPool.size() > 0)
				{
					ArrayList<Chromosome> offspring = crossover(probCross, matingPool.size());
					mutation(offspring, probMut);
				
					//Aggiungiamo alla popolazione i due individui
					gen_i.addToPopulation(offspring.get(0));
					gen_i.addToPopulation(offspring.get(1));
				}	
			
				//Calcola la fitness
				ra -= (1 / maxGen);
				rb += (1 / maxGen);
				probMut -= (0.5 / maxGen);
				calculateAnnealedFitness(gen_i, ra, rb);
				gen_i.best();
		
				//LA generazione appena creata è la precendente nel prossimo ciclo
				prev = gen_i;		
				gen++;

				//Stampiamo la popolazione della generazione del ciclo successivo
				//System.out.println("Generazione: " + gen + "\n" + prev);
			}
		
			Chromosome bestC = prev.getBestChromosome();
			//Avvolorare eml con le informazioni contenute nel miglior cromosoma finale
			eml = validate(bestC, sml);
		}
		else
		{
			SpotMatch sm = sml.get(0);
			EntityMatchList candidate = sm.getEntities();
			EntityMatch mostProb = candidate.get(0);
			mostProb.setScore(0.5);
			eml = new EntityMatchList();
			eml.add(mostProb);
		}
		
		return eml;
	}


	@Override
	public void init(DexterParams dexterParams, DexterLocalParams defaultModuleParams) 
	{
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Metodo privato che costruisce la popolazione iniziale. La dimensione della popolazione &egrave; adattiva, 
	 * &egrave; impostata al doppio della dimensione dell'insieme di entit&agrave; candidate pi&ugrave; grande, 
	 * possiede come limite superiore il valore 100.
	 * 
	 * @param sml SpotMatchList che contiene tutte le informazioni sugli spot trovati precedentemente.
	 * @return La popolazione iniziale appena costruita.
	 */
	private Population constructPopulation(SpotMatchList sml)
	{
		ArrayList<Integer> dimBinGenes = new ArrayList<Integer>();
		int max = 0;
		
		//Calcoliamo le dimensioni binarie di ogni gene
		for (SpotMatch sm : sml)
		{
			EntityMatchList eml = sm.getEntities();
			//Codifichiamo in binario gli indici che appunto vanno da 0 a size-1, vedi giù
			int sizeEml = eml.size();
			
			//Max
			if(max < sizeEml)
			{
				max = sizeEml;
			}
			
			int sizeBin = Binary.bitForInt(sizeEml-1);
			dimBinGenes.add(sizeBin);
		}
		
		//GRANDEZZA POPOLAZIONE ADATTIVA, se > 100 viene settata a 100 (caso Raro)
		int sizePop = max * 2;
		if (sizePop > 100)
			sizePop = 100;
		
		Population population = new Population(sizePop);
		//int sizeMax = population.getMaxPopDim();
		
		for(int i = 0; i < sizePop; i++)
		{
			Chromosome c = new Chromosome(i, sml, dimBinGenes);
			c.createRandom();
			population.addToPopulation(c);
		}
		
		return population;
	}
	
	/**
	 * Calcola l'annealed fitness della popolazione (della precedente generazione) data in input.
	 * 
	 * @param prevGen Popolazione sulla quale calcolare l'annealed fitness.
	 * @param ra Parametro ra dell'annealed fitness.
	 * @param rb Parametro rb dell'annealed fitness.
	 */
	private void calculateAnnealedFitness(Population prevGen, double ra, double rb)
	{
		prevGen.calculateAnnealedFitness(ra, rb);
	}
	
	/**
	 * Operatore di selezione. Seleziona 'dimMaxPop' cromosomi dalla popolazione e li aggiunge al mating pool. 
	 * Ogni elemento pu&ograve; essere selezionato pi&ugrave; volte.
	 * 
	 * @param prevGen Popolazione alla quale si vuole applicare l'operatore di selezione.
	 * @param dimMaxPop Numero di elementi da selezionare.
	 */
	private void selection(Population prevGen, int dimMaxPop)
	{
		double sumAnnealed = 0.0;
		for (Chromosome c : prevGen)
		{
			sumAnnealed += c.getAnnealedFitness();
		}
		
		Random random = new Random();
		
		while (matingPool.size() < dimMaxPop)
		{
			double rand = random.nextDouble() * sumAnnealed;
			double r = 0.0;
			for (Chromosome c : prevGen)
			{
				r += c.getAnnealedFitness();
				if(rand <= r)
				{
					matingPool.add(c);
					break;
				}
			}
		}
		
	}
	
	/**
	 * Operatore di crossover. Seleziona due elementi dal mating pool e effettua il crossover richiamando il 
	 * metodo crossover della classe Population. Se il crossover non viene effettuato ritorna una copia dei 
	 * due cromosomi scelti.
	 * 
	 * @param probCross Tasso di crossover.
	 * @param matingPoolSize Dimensione del matin pool.
	 * @return un ArrayList che rappresenta la prole generata dal crossover.
	 * @see Population
	 */
	private ArrayList<Chromosome> crossover(double probCross, int matingPoolSize)
	{
		Random random = new Random();
		ArrayList<Chromosome> offspring;
			
		int rand1 = random.nextInt(matingPoolSize);
			
		int rand2 = random.nextInt(matingPoolSize);
		while(rand2 == rand1)
		{
			rand2 = random.nextInt(matingPoolSize);
		}

		if (rand1 < rand2)
			rand2--;
			
			
		Chromosome parent1 = matingPool.remove(rand1);
		Chromosome parent2 = matingPool.remove(rand2);
			
		double cross = random.nextDouble();
		if (cross <= probCross)
		{
			//CrossOver
			offspring = Population.crossover(parent1, parent2);
		}
		else
		{		
			offspring = new ArrayList<Chromosome>();
			offspring.add(parent1);
			offspring.add(parent2);
		}
			
		return offspring;
		
	}

	/**
	 * Operatore di mutazione. Chiama il metodo mutation della classe Population.
	 * 
	 * @param offspring Prole generata dall'operatore di crossover.
	 * @param probMutation Tasso di mutazione.
	 * @see Population
	 */
	private void mutation(ArrayList<Chromosome> offspring, double probMutation)
	{
		Population.mutation(offspring, probMutation);
	}
	
	/**
	 * Avvalora il miglior cromosoma presente nell'ultima generazione come soluzione, recuperando per ogni gene 
	 * l'entit&agrave; candidata in base al valore contenuto dal gene e impostando lo score di quest'ultima pari 
	 * alla average fitness del gene.
	 * 
	 * @param best Miglior cromosoma presente nell'ultima generazione.
	 * @param sml SpotMatchList che contiene tutte le informazioni sugli spot.
	 * @return un EntityMatchList che rappresenta la soluzione generata.
	 */
	private EntityMatchList validate(Chromosome best, SpotMatchList sml)
	{
		
		EntityMatchList eml = new EntityMatchList();
		int index = 0;
		
		for (Gene g : best)
		{
			int entityIndex = g.getValue();
			SpotMatch sm = sml.get(index);
			EntityMatchList candidate = sm.getEntities();
			EntityMatch emChosen = candidate.get(entityIndex);
			emChosen.setScore(g.getAverageFitness());
			eml.add(emChosen);
			
			index++;
		}
		
		return eml;
	}
	
}
