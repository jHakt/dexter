package it.cnr.isti.hpc.dexter.spotter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import it.cnr.isti.hpc.dexter.common.Document;
import it.cnr.isti.hpc.dexter.common.Field;
import it.cnr.isti.hpc.dexter.entity.Entity;
import it.cnr.isti.hpc.dexter.entity.EntityMatch;
import it.cnr.isti.hpc.dexter.entity.EntityMatchList;
import it.cnr.isti.hpc.dexter.spot.Spot;
import it.cnr.isti.hpc.dexter.spot.SpotMatch;
import it.cnr.isti.hpc.dexter.spot.SpotMatchList;
import it.cnr.isti.hpc.dexter.spot.repo.SpotRepository;
import it.cnr.isti.hpc.dexter.spot.repo.SpotRepositoryFactory;
import it.cnr.isti.hpc.dexter.util.DexterLocalParams;
import it.cnr.isti.hpc.dexter.util.DexterParams;
import it.cnr.isti.hpc.structure.LRUCache;

public class NifSpotter extends AbstractSpotter implements Spotter
{

	/**
	 * Logger for this class
	 */
	//private static final Logger logger = LoggerFactory
		//	.getLogger(NifSpotter.class);

	private static LRUCache<String, Spot> cache;

	DexterParams params = DexterParams.getInstance();

	SpotRepository spotRepo;
	
	public NifSpotter()
	{
		int cachesize = params.getCacheSize("spotter");
		cache = new LRUCache<String, Spot>(cachesize);
		SpotRepositoryFactory factory = new SpotRepositoryFactory();
		spotRepo = factory.getStdInstance();
	}
	
	
	@Override
	public SpotMatchList match(DexterLocalParams localParams, Document document) 
	{
		SpotMatchList matches = new SpotMatchList();
		for (Iterator<Field> fields = document.getFields(); fields.hasNext(); )
		{
			Field field = fields.next();
			List<NifMention> mentions = extractMention(field);
		
			for (Iterator<NifMention> it = mentions.iterator(); it.hasNext(); )
			{
				NifMention nif = it.next();
				String mention = nif.mention;
				Spot spotMention;
				
				//Vediamo se è presente nella cache altrimenti lo aggiungiamo
				if (cache.containsKey(mention)) 
				{
					// hit in cache
					spotMention = cache.get(mention);
					if (spotMention != null) 
					{
						spotMention = (Spot)spotMention.clone();
					}
				} 
				else 
				{
					spotMention = spotRepo.getSpot(mention);
					cache.put(mention, spotMention);
				}
				
				//Se non ha trovato nulla andiamo avanti
				if(spotMention == null)
					continue;
		
				//Creiamo lo spotmatch per questa menzione
				SpotMatch match = new SpotMatch(spotMention, field);
				EntityMatchList eml = getEntityMatchList(match);
				match.setEntities(eml);
				match.setStart(nif.start);
				match.setEnd(nif.end);
				
				matches.add(match);
			}
			
		}
		
		/*
		for (SpotMatch sm : matches)
		{
			Spot m = sm.getSpot();
			System.out.println(m);
		}
		*/
		
		return matches;
		
	}

	@Override
	public void init(DexterParams dexterParams, DexterLocalParams defaultModuleParams) 
	{
		// TODO Auto-generated method stub
		
	}
	
	private class NifMention
	{
		private String mention;
		private int start;
		private int end;
		
		NifMention(String mention, int start, int end)
		{
			this.mention = mention;
			this.start = start;
			this.end = end;
		}
		
	}
	
	private List<NifMention> extractMention(Field field)
	{
		List<NifMention> nMentions = new ArrayList<NifMention>();
		
		String s = field.getValue();
		String[] arr = s.split(";");
		for (String str : arr)
		{
			String[] arr2 = str.split(":");
			String mention = arr2[0];
			String s1 = arr2[1];
			
			String[] arr3 = s1.split(",");
			String s2 = arr3[0];
			String s3 = arr3[1];
			int start = (new Integer(s2)).intValue();
			int end = (new Integer(s3)).intValue();
			
			nMentions.add(new NifMention(mention, start, end));
			
		}
		
		return nMentions;
		
	}
	
	private EntityMatchList getEntityMatchList(SpotMatch spot) 
	{

		//DIVERSO DA QUELLO IN ENTITYRANKER perché non filtra nulla
		
		EntityMatchList eml = new EntityMatchList();
		EntityMatch match = null;
		
		for (Entity e : spot.getSpot().getEntities()) 
		{
			match = new EntityMatch(e, spot.getEntityCommonness(e), spot);
			eml.add(match);
		}
		
		return eml;
	}

}
