package it.cnr.isti.hpc.dexter.disambiguation;

import java.util.ArrayList;
import java.util.Random;
import genetic.Chromosome;
import genetic.Gene;
import genetic.Population;
import it.cnr.isti.hpc.dexter.entity.EntityMatch;
import it.cnr.isti.hpc.dexter.entity.EntityMatchList;
import it.cnr.isti.hpc.dexter.spot.SpotMatch;
import it.cnr.isti.hpc.dexter.spot.SpotMatchList;
import it.cnr.isti.hpc.dexter.util.Binary;
import it.cnr.isti.hpc.dexter.util.DexterLocalParams;
import it.cnr.isti.hpc.dexter.util.DexterParams;

/**
 * A disambiguator that utilize a genetic algorithm.
 *  
 * @author Giovanni Izzi 
 *
 */
public class GeneticDisambiguator implements Disambiguator 
{
	private ArrayList<Chromosome> matingPool = new ArrayList<Chromosome>();
	
	
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
	
	private void calculateAnnealedFitness(Population prevGen, double ra, double rb)
	{
		prevGen.calculateAnnealedFitness(ra, rb);
	}
	
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

	private void mutation(ArrayList<Chromosome> offspring, double probMutation)
	{
		Population.mutation(offspring, probMutation);
	}
	
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
