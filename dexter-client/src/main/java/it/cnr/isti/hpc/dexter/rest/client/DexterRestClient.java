/**
 *  Copyright 2013 Diego Ceccarelli
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
 *  Copyright 2013 Diego Ceccarelli
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package it.cnr.isti.hpc.dexter.rest.client;

import it.cnr.isti.hpc.dexter.common.ArticleDescription;
import it.cnr.isti.hpc.dexter.common.Document;
import it.cnr.isti.hpc.dexter.common.Field;
import it.cnr.isti.hpc.dexter.common.FlatDocument;
import it.cnr.isti.hpc.dexter.common.MultifieldDocument;
import it.cnr.isti.hpc.dexter.rest.domain.AnnotatedDocument;
import it.cnr.isti.hpc.dexter.rest.domain.EntityRelatedness;
import it.cnr.isti.hpc.dexter.rest.domain.SpottedDocument;
import it.cnr.isti.hpc.dexter.rest.domain.Tagmeta;
import it.cnr.isti.hpc.net.FakeBrowser;
import it.cnr.isti.hpc.structure.LRUCache;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.jersey.api.client.Client;

/**
 * Allows to perform annotation calling the Dexter Rest Service
 * 
 * @author Diego Ceccarelli <diego.ceccarelli@isti.cnr.it>
 * 
 *         Created on Oct 29, 2013
 */
// TODO rewrite using the jersey-client

public class DexterRestClient 
{

	private final URI server;
	private final FakeBrowser browser;

	private final Client client = Client.create();

	private static LRUCache<EntityRelatedness, EntityRelatedness> relatednessCache = new LRUCache<EntityRelatedness, EntityRelatedness>(
			1000);

	private Boolean wikinames = false;

	Map<String, String> params = new HashMap<String, String>();

	private String spotter = null;
	
	private String disambiguator = null;

	public double linkProbability = -1;

	public double minConfidence = 0.5;

	private static Gson gson = new Gson();

	private static final Logger logger = LoggerFactory.getLogger(DexterRestClient.class);

	/**
	 * Istanciates a Rest client, that invocates the rest service provided by a
	 * Dexter server.
	 * 
	 * @param server
	 *            the url of the rest service
	 */
	public DexterRestClient(String server) throws URISyntaxException 
	{
		this(new URI(server));

	}

	/**
	 * Istanciates a Rest client, that invocates the rest service provided by a
	 * Dexter server.
	 * 
	 * @param server
	 *            the url of the rest service
	 */
	public DexterRestClient(URI server) 
	{
		this.server = server;
		browser = new FakeBrowser();
	}

	public AnnotatedDocument annotate(String text) 
	{
		return annotate(new FlatDocument(text));
	}

	/**
	 * Performs the entity linking on a given text, annotating maximum 5
	 * entities.
	 * 
	 * @param text
	 *            the text to annotate
	 * @returns an annotated document, containing the annotated text, and a list
	 *          entities detected.
	 */
	public AnnotatedDocument annotate(Document doc) { return annotate(doc, -1); }

	/**
	 * Performs the entity linking on a given text, annotating maximum n
	 * entities.
	 * 
	 * @param text
	 *            the text to annotate
	 * @param n
	 *            the maximum number of entities to annotate
	 * @returns an annotated document, containing the annotated text, and a list
	 *          entities detected.
	 */
	public AnnotatedDocument annotate(Document doc, int n) 
	{
		String text = "";
		String json = "";
		Tagmeta.DocumentFormat format = Tagmeta.DocumentFormat.TEXT;
		if (doc instanceof FlatDocument) 
		{
			//text = URLEncoder.encode(doc.getContent());
			try 
			{
				text = URLEncoder.encode(doc.getContent(), "UTF-8");
			} 
			catch (UnsupportedEncodingException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} 
		else if (doc instanceof MultifieldDocument) 
		{
			text = gson.toJson(doc);
			format = Tagmeta.DocumentFormat.JSON;
		}

		StringBuilder sb = new StringBuilder(paramsToRequest());
		sb.append("text=").append(URLEncoder.encode(text));

		// String url = "/annotate?" + paramsToRequest() + "&text=" + text;
		if (linkProbability > 0)
			sb.append("&lp=").append(linkProbability);

		if (n > 0) 
		{
			sb.append("&n=").append(n);
		}
		
		if (spotter != null)
		{
			sb.append("&spt=").append(spotter);
		}

		if (disambiguator != null) 
		{
			sb.append("&dsb=").append(disambiguator);
		}

		if (wikinames) 
		{
			sb.append("&wn=true");
		}
		sb.append("&min-conf=").append(minConfidence);

		sb.append("&format=").append(format);

		// if (wikinames) {
		// url += "&wn=true";
		// }
		try 
		{
			// if (n > 0) {
			// url += "&n=" + n;
			// }
			// System.out.println(sb.toString());
			json = postQuery("annotate", sb.toString());
		} 
		catch (IOException e) 
		{
			logger.error("cannot call the rest api {}", e.toString());
			return null;
		}
		AnnotatedDocument adoc = gson.fromJson(json, AnnotatedDocument.class);
		return adoc;
	}

	public SpottedDocument spot(String text) 
	{
		return spot(new FlatDocument(text));
	}

	public String getSpotter() { return spotter; }
	public void setSpotter(String spotter) { this.spotter = spotter; }
	
	public String getDisambiguator() { return disambiguator; }

	public void setDisambiguator(String disambiguator) 
	{
		this.disambiguator = disambiguator;
	}

	/**
	 * It only performs the first step of the entity linking process, i.e., find
	 * all the mentions that could refer to an entity.
	 * 
	 * @param text
	 *            the text to spot
	 * @return all the spots detected in the text together with their link
	 *         probability. For each spot it also returns the list of candidate
	 *         entities associated with it, together with their commonness.
	 */
	public SpottedDocument spot(Document doc) 
	{
		String text = null;
		Tagmeta.DocumentFormat format = Tagmeta.DocumentFormat.TEXT;
		
		if (doc instanceof FlatDocument) 
		{
			text = URLEncoder.encode(doc.getContent());
		} 
		else if (doc instanceof MultifieldDocument) 
		{
			text = URLEncoder.encode(gson.toJson(doc));
			format = Tagmeta.DocumentFormat.JSON;
		}

		String json = "";
		StringBuilder sb = new StringBuilder("text=").append(text);
		if (linkProbability > 0)
			sb.append("&lp=").append(linkProbability);
		if (wikinames) 
		{
			sb.append("&wn=true");
		}
		sb.append("&format=" + format);
		// System.out.println(sb.toString());
		try 
		{
			json = postQuery("spot", sb.toString());
		} 
		catch (IOException e) 
		{
			logger.error("cannot call the rest api {}", e.toString());
			return null;
		}
		SpottedDocument sdoc = gson.fromJson(json, SpottedDocument.class);
		return sdoc;
	}

	/**
	 * Given the Wiki-id of an entity, returns a snippet containing some
	 * sentences that describe the entity.
	 * 
	 * @param id
	 *            the Wiki-id of the entity
	 * @returns a short description of the entity represented by the Wiki-id
	 */
	public ArticleDescription getDesc(int id) 
	{

		String json = "";
		try 
		{
			json = browser.fetchAsUTF8String(
					server.toString() + "/get-desc?id=" + id).toString();
		} 
		catch (IOException e) 
		{
			logger.error("cannot call the rest api {}", e.toString());
			return null;
		}
		ArticleDescription ad = gson.fromJson(json, ArticleDescription.class);
		return ad;
	}

	/**
	 * Given an entity, returns the entities that link to the given entity
	 * 
	 * @param id
	 *            the Wiki-id of the entity
	 * @returns the entities that link to the given entity
	 */
	public ArticleDescription getSourceEntities(int entityId) 
	{

		String json = "";
		try 
		{
			StringBuffer sb = new StringBuffer(server.toString()
					+ "/get-source-entities");
			sb.append("?id=" + entityId);
			sb.append("&wn=" + String.valueOf(wikinames));
			json = browser.fetchAsUTF8String(sb.toString()).toString();
		} 
		catch (IOException e) 
		{
			logger.error("cannot call the rest api {}", e.toString());
			return null;
		}
		ArticleDescription ad = gson.fromJson(json, ArticleDescription.class);
		return ad;
	}

	/**
	 * Given an entity, returns the entities linked by given entity
	 * 
	 * @param id
	 *            the Wiki-id of the entity
	 * @returns the entities linked by the given entity
	 */
	public ArticleDescription getTargetEntities(int entityId) 
	{

		String json = "";
		try 
		{
			StringBuffer sb = new StringBuffer(server.toString()
					+ "/get-target-entities");
			sb.append("?id=" + entityId);
			sb.append("&wn=" + String.valueOf(wikinames));
			json = browser.fetchAsUTF8String(sb.toString()).toString();
		} 
		catch (IOException e) 
		{
			logger.error("cannot call the rest api {}", e.toString());
			return null;
		}
		ArticleDescription ad = gson.fromJson(json, ArticleDescription.class);
		return ad;
	}

	/**
	 * Given an entity, returns the entities linked by given entity
	 * 
	 * @param id
	 *            the Wiki-id of the entity
	 * @returns the entities linked by the given entity
	 */
	public EntityRelatedness relatedness(int entityId1, int entityId2,
			String rel) 
	{

		String json = "";
		try 
		{
			StringBuffer sb = new StringBuffer(server.toString()
					+ "/relatedness");
			sb.append("?e1=" + entityId1);
			sb.append("&e2=" + entityId2);
			sb.append("&rel=" + rel);
			sb.append("&wn=" + String.valueOf(wikinames));
			json = browser.fetchAsUTF8String(sb.toString()).toString();
		} 
		catch (IOException e) 
		{
			logger.error("cannot call the rest api {}", e.toString());
			return null;
		}
		EntityRelatedness relatedness = gson.fromJson(json,
				EntityRelatedness.class);
		return relatedness;
	}

	/**
	 * Given the Wiki-id entity label (the title, or a redirect), the wiki-id of
	 * the entity
	 * 
	 * @param title
	 *            the label or a redirect title of the entity.
	 * @returns the wiki-id of the entity
	 */
	public int getId(String title) 
	{
		title = URLEncoder.encode(title);

		String json = "";
		try 
		{
			String url = server.toString() + "/get-id?title="
					+ URLEncoder.encode(title, "UTF-8");
			logger.info("featch url {} ", url);
			json = browser.fetchAsUTF8String(url);

		} 
		catch (IOException e) 
		{
			logger.error("cannot call the rest api {}", e.toString());
			return -1;
		}
		ArticleDescription ad = gson.fromJson(json, ArticleDescription.class);
		return ad.getId();
	}

	private String postQuery(String restcall, String params) throws IOException 
	{
		HttpURLConnection con = (HttpURLConnection) new URL(server.toString()
				+ "/" + restcall).openConnection();

		// add reuqest header
		con.setRequestMethod("POST");
		// System.out.println("params = " + params);
		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(params);
		wr.flush();
		wr.close();

		int responseCode = con.getResponseCode();

		BufferedReader in = new BufferedReader(new InputStreamReader(
				con.getInputStream(), "UTF-8"));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) 
		{
			response.append(inputLine);
		}
		in.close();
		return response.toString();
	}

	public void addParams(String name, String value) 
	{
		params.put(name, value);
	}

	private String paramsToRequest() 
	{
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> p : params.entrySet()) 
		{
			sb.append(p.getKey()).append('=');
			sb.append(URLEncoder.encode(p.getValue()));
			sb.append('&');
		}
		return sb.toString();
	}

	public Boolean getWikinames() { return wikinames; }

	public double getLinkProbability() { return linkProbability; }

	public void setLinkProbability(double linkProbability) 
	{
		this.linkProbability = linkProbability;
	}

	public void setWikinames(Boolean wikinames) 
	{
		this.wikinames = wikinames;
	}

	public List<Integer> getChildCategories(String title) 
	{
		return getChildCategories(this.getId(title));

	}

	public List<Integer> getChildCategories(int categoryWikiId) 
	{
		String json = "";
		try 
		{
			json = browser.fetchAsString(
					server.toString() + "/get-child-categories?wid="
							+ categoryWikiId).toString();
		} 
		catch (IOException e) 
		{
			logger.error("cannot call the rest api {}", e.toString());
			return Collections.emptyList();
		}
		List<Integer> categories = gson.fromJson(json, List.class);
		return categories;

	}

	public List<Integer> getParentCategories(String title) 
	{
		return getParentCategories(this.getId(title));

	}

	public List<Integer> getParentCategories(int categoryWikiId) 
	{
		String json = "";
		try 
		{
			json = browser.fetchAsString(
					server.toString() + "/get-parent-categories?wid="
							+ categoryWikiId).toString();
		} 
		catch (IOException e) 
		{
			logger.error("cannot call the rest api {}", e.toString());
			return Collections.emptyList();
		}
		List<Integer> categories = gson.fromJson(json, List.class);
		return categories;

	}

	public List<Integer> getEntityCategories(String title) 
	{
		return getEntityCategories(this.getId(title));

	}

	public List<Integer> getEntityCategories(int entityId) 
	{
		String json = "";
		try 
		{
			json = browser.fetchAsString(
					server.toString() + "/get-belonging-entities?wid="
							+ entityId).toString();
		} 
		catch (IOException e) 
		{
			logger.error("cannot call the rest api {}", e.toString());
			return Collections.emptyList();
		}
		List<Integer> categories = gson.fromJson(json, List.class);
		return categories;
	}

	public List<Integer> getBelongingEntities(String title) 
	{
		return getBelongingEntities(this.getId(title));

	}

	public List<Integer> getBelongingEntities(int entityId) 
	{
		String json = "";
		try 
		{
			json = browser.fetchAsString(
					server.toString() + "/get-entity-categories?wid="
							+ entityId).toString();
		} 
		catch (IOException e) 
		{
			logger.error("cannot call the rest api {}", e.toString());
			return Collections.emptyList();
		}
		List<Integer> categories = gson.fromJson(json, List.class);
		return categories;
	}

	public static void main(String[] args) throws URISyntaxException 
	{
		DexterRestClient client = new DexterRestClient("http://localhost:8080/dexter-webapp/api/rest");
		client.setLinkProbability(1);

		// AnnotatedDocument ad = client
		// .annotate("Dexter is an American television drama series which debuted on Showtime on October 1, 2006. The series centers on Dexter Morgan (Michael C. Hall), a blood spatter pattern analyst for the fictional Miami Metro Police Department (based on the real life Miami-Dade Police Department) who also leads a secret life as a serial killer. Set in Miami, the show's first season was largely based on the novel Darkly Dreaming Dexter, the first of the Dexter series novels by Jeff Lindsay. It was adapted for television by screenwriter James Manos, Jr., who wrote the first episode. ");
		// System.out.println(gson.toJson(ad));
		// SpottedDocument sd = client
		// .spot("Dexter is an American television drama series which debuted on Showtime on October 1, 2006. The series centers on Dexter Morgan (Michael C. Hall), a blood spatter pattern analyst for the fictional Miami Metro Police Department (based on the real life Miami-Dade Police Department) who also leads a secret life as a serial killer. Set in Miami, the show's first season was largely based on the novel Darkly Dreaming Dexter, the first of the Dexter series novels by Jeff Lindsay. It was adapted for television by screenwriter James Manos, Jr., who wrote the first episode. ");
		// System.out.println(gson.toJson(sd));

		MultifieldDocument document = new MultifieldDocument();
		//document.addField(new Field("q1",
			//"On this day 24 years ago Maradona scored his infamous Hand of God goal against England in the quarter-final of the 1986"));
		//document.addField(new Field("q1", "maradona:25,33;hand of god goal:54,70;england:79,86;"));
		
		document.addField(new Field("q1", "hurricane:16,25;desire:73,79;carter:50,56;dylan:0,5;"));
		//document.addField(new Field("q1", "oakland:557,564;cincinnati:1000,1010;texas:715,720;central division:367,383;boston:294,300;chicago:1023,1030;western division:1067,1083;chicago:1216,1223;colorado:1344,1352;philadelphia:912,924;montreal:1259,1267;american league eastern division:207,239;seattle:775,782;houston:955,962;national league eastern division:783,815;kansas city:477,488;baltimore:762,771;texas:519,524;toronto:317,324;san diego:1246,1255;st louis:1356,1364;pittsburgh:1043,1053;pittsburgh:1307,1317;major league baseball:78,99;philadelphia:1283,1295;colorado:1130,1138;detroit:342,349;milwaukee:692,701;minnesota:679,688;montreal:847,855;los angeles:1106,1117;cleveland:702,711;cleveland:384,393;california:733,743;san diego:1084,1093;houston:1296,1303;florida:869,876;st louis:975,983;milwaukee:454,463;new york:721,729;atlanta:827,834;new york:251,259;new york:1335,1343;seattle:537,544;san francisco:1151,1164;new york:890,898;atlanta:1205,1212;baltimore:272,281;toronto:671,678;florida:1224,1231;san francisco:1318,1331;major league:11,23;oakland:754,761;western division:502,518;boston:744,750;new york:58,66;kansas city:637,648;central division:938,954;chicago:406,413;minnesota:427,436;cincinnati:1235,1245;chicago:660,667;los angeles:1268,1279;california:582,592;detroit:652,659;"));
		//document.addField(new Field("q1", "irish:310,315;republic of ireland:510,529;charlton:417,425;jack charlton:77,90;charlton:699,707;ireland:274,281;charlton:20,28;bobby:1381,1386;germany:1094,1101;charlton:843,851;england:1238,1245;1966 world cup:1327,1341;ireland:640,647;englishman:167,177;charlton:220,228;englishman:9,19;ireland:125,132;dublin:59,65;irish:1113,1118;ireland:860,867;european:1062,1070;ireland:991,998;world cup:1017,1026;dick spring:353,364;peggy:248,253;irishman:49,57;leeds united:1199,1211;england:1139,1146;"));
	    //document.addField(new Field("q2", "While Apple is an electronics company, Mango is a clothing one and Orange is a communication one."));

		//document.addField(new Field("q3", "pablo neruda"));

		//document.addField(new Field("q4", "van gogh"));
		
		//document.addField(new Field("q4", "Angelina, her father Jon, and her partner Brad never played together in the same movie."));
		
		//document.addField(new Field("q5", "Del Piero is a Juventus' player."));
		//document.addField(new Field("qTest", "Dexter is an American television drama series which debuted on Showtime on October 1, 2006. "
			//	+ "The series centers on Dexter Morgan (Michael C. Hall), a blood spatter pattern analyst for the fictional Miami Metro Police Department "
			//	+ "(based on the real life Miami-Dade Police Department) who also leads a secret life as a serial killer. Set in Miami, the show's first season was largely based "
			 //   + "on the novel Darkly Dreaming Dexter, the first of the Dexter series novels by Jeff Lindsay. It was adapted for television by screenwriter James Manos, Jr., who wrote the first episode. "));
		

		//client.setDisambiguator("tagme");
		//document.addField(new Field("q", "Michael Jordan is an American retired professional basketball player, businessman, and principal owner and chairman of the Charlotte Hornets."));
		//document.addField(new Field ("q6", "President Barack Obama met Angela Merkel in Berlin, yesterday"));
		
		//MODIFICA
		client.setSpotter("nif");
		client.setDisambiguator("genetic");
		
		
		client.setLinkProbability(0.03);
		client.setWikinames(true);

		AnnotatedDocument sd = client.annotate(document);
		System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(sd));

		// // FIXME belonging entities does not work, probably I changed
		// something
		// // in the rest api.
		// System.out.println("maradona wid = " + client.getId("maradona"));
		// ArticleDescription desc = client.getDesc(5981816);
		//
		// System.out.println(desc);
		// System.out.println("categories " +
		// client.getBelongingEntities(74253));

	}

	public double getMinConfidence() { return minConfidence; }

	public void setMinConfidence(double minConfidence) 
	{
		this.minConfidence = minConfidence;
	}
	
}
