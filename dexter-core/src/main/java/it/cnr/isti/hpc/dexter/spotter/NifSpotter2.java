package it.cnr.isti.hpc.dexter.spotter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import it.cnr.isti.hpc.dexter.common.Document;
import it.cnr.isti.hpc.dexter.common.Field;
import it.cnr.isti.hpc.dexter.entity.Entity;
import it.cnr.isti.hpc.dexter.entity.EntityMatch;
import it.cnr.isti.hpc.dexter.entity.EntityMatchList;
import it.cnr.isti.hpc.dexter.label.IdHelper;
import it.cnr.isti.hpc.dexter.label.IdHelperFactory;
import it.cnr.isti.hpc.dexter.spot.Spot;
import it.cnr.isti.hpc.dexter.spot.SpotMatch;
import it.cnr.isti.hpc.dexter.spot.SpotMatchList;
import it.cnr.isti.hpc.dexter.util.DexterLocalParams;
import it.cnr.isti.hpc.dexter.util.DexterParams;
import it.cnr.isti.hpc.structure.LRUCache;
import jHakt.Utils.NifMention;
import jHakt.Utils.ProcessingText;

public class NifSpotter2 extends AbstractSpotter implements Spotter 
{
	private static LRUCache<String, Spot> cache;

	DexterParams params = DexterParams.getInstance();
	
	public NifSpotter2()
	{
		int cachesize = params.getCacheSize("spotter");
		cache = new LRUCache<String, Spot>(cachesize);
	}

	@Override
	public SpotMatchList match(DexterLocalParams localParams, Document document) 
	{
		SpotMatchList matches = new SpotMatchList();
		for (Iterator<Field> fields = document.getFields(); fields.hasNext(); )
		{
			Field field = fields.next();
			
			//Now we use methods in ProcessingText to check if there is an error in Gerbil entity mentions (for D2KB only!)
			ArrayList<String> splitDoc = ProcessingText.splitNifD2kbDoc(field);
			String splitTextDoc = splitDoc.get(0);
			String splitMentions = splitDoc.get(1);
			Set<NifMention> mentions = ProcessingText.extractMentions(splitMentions);
			ProcessingText.checkMentions(splitTextDoc, mentions);
		
			for (Iterator<NifMention> it = mentions.iterator(); it.hasNext(); )
			{
				NifMention nif = it.next();
				String mention = nif.getMention();
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
					//spotMention = spotRepo.getSpot(mention);
					IdHelper helper = IdHelperFactory.getStdIdHelper();
					Set<Integer> ids = helper.getIds(mention);
					spotMention = getSpotFromIds(mention, ids);
					cache.put(mention, spotMention);
				}
				
				//Se non ha trovato nulla andiamo avanti
				if(spotMention == null)
					continue;
		
				//Creiamo lo spotmatch per questa menzione
				SpotMatch match = new SpotMatch(spotMention, field);
				EntityMatchList eml = getEntityMatchList(match);
				match.setEntities(eml);
				match.setStart(nif.getStart());
				match.setEnd(nif.getEnd());
				
				matches.add(match);
			}
		}
		
		return matches;
		
	}

	@Override
	public void init(DexterParams dexterParams, DexterLocalParams defaultModuleParams) 
	{
		// TODO Auto-generated method stub
		
	}

	private Spot getSpotFromIds(String mention, Set<Integer> ids)
	{
		//VALUE 10 has no meaning here
		
		List<Entity> entities = new ArrayList<Entity>();
		for(int id: ids)
		{
			Entity temp = new Entity(id, 10);
			entities.add(temp);
		}
		
		Spot s = new Spot(mention, entities, 10, 10);
		
		return s;
		
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
