/***********************************************************************
Copyright 2018 Stefanie Ververs, University of Lübeck

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
/***********************************************************************/
package de.uzl.itcr.mimic2fhir.tools;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * Provide RxNorm-Lookup for NDC (National Drug Code) and GSN (Generic Sequence Number)
 * Singleton: Use getInstance to get working object
 * @author Stefanie Ververs
 *
 */
public class RxNormLookup {

	private HashMap<String, List<RxNormConcept>> rdxLookupNdc;
	private HashMap<String,  List<RxNormConcept>> rdxLookupGsn;
	
	protected RxNormLookup() {
		rdxLookupNdc = new HashMap<String, List<RxNormConcept>>();
		rdxLookupGsn = new HashMap<String, List<RxNormConcept>>();
	}
	
	/**
	 * Get RxNormConcepts for a NDC
	 * @param ndc National Drug Code
	 * @return List of RxNorm-Concept
	 */
	public  List<RxNormConcept> getRxNormForNdc(String ndc) {
		if(rdxLookupNdc.containsKey(ndc)) {
			return rdxLookupNdc.get(ndc);
		}else {
			 List<RxNormConcept> rdxNorm = findRxNormForNdc(ndc);
			if(rdxNorm != null) {
				rdxLookupNdc.put(ndc, rdxNorm);
				return rdxNorm;
			}
		}
		return null;
	}

	/**
	 * Get RxNormConcepts for a GSN
	 * @param gsn Generic Sequence Number
	 * @return List of RxNorm-Concept
	 */
	public  List<RxNormConcept> getRxNormForGsn(String gsn) {
		if(rdxLookupGsn.containsKey(gsn)) {
			return rdxLookupGsn.get(gsn);
		}else {
			 List<RxNormConcept> rdxNorm = findRxNormForGsn(gsn);
			if(rdxNorm != null) {
				rdxLookupGsn.put(gsn, rdxNorm);
				return rdxNorm;
			}
		}
		return null;
	}
	
	private List<RxNormConcept> findRxNormForGsn(String gsn) {
		String url = "https://rxnav.nlm.nih.gov/REST/rxcui.json?idtype=GCN_SEQNO&id=" + gsn;
		return findRxNorm(url);
	}
	
	private List<RxNormConcept> findRxNormForNdc(String ndc) {
		String url = "https://rxnav.nlm.nih.gov/REST/rxcui.json?idtype=NDC&id=" + ndc;
		return findRxNorm(url);
	}
	
	private List<RxNormConcept> findRxNorm(String url) {
		//use of RxNorm REST API https://rxnav.nlm.nih.gov/REST
		
		CloseableHttpClient httpclient = HttpClients.createDefault();

		List<RxNormConcept> rxNormList = new ArrayList<RxNormConcept>();
		
		HttpGet httpGet = new HttpGet(url);
		
		CloseableHttpResponse response = null;
		try {		
			//GET 
			response = httpclient.execute(httpGet);
		    	    
			//Response -> JSON Object
			HttpEntity entity = response.getEntity();
			String jsonResponse = EntityUtils.toString(entity);
			InputStream is = new ByteArrayInputStream( jsonResponse.getBytes() );
			
			JsonReader jsonReader = Json.createReader(is);
			JsonObject respObject = jsonReader.readObject();
			jsonReader.close();
			
			JsonArray ids = respObject.getJsonObject("idGroup").getJsonArray("rxnormId");
			if(ids != null && !ids.isEmpty()) {
				for(JsonString rxNorm : ids.getValuesAs(JsonString.class)) {
					RxNormConcept rc = new RxNormConcept();
					rc.setCui(rxNorm.getString());
					//get Name: Separate Call
					rc.setName(getNameForCui(rc.getCui()));
					rxNormList.add(rc);
				}
			}
			
		    EntityUtils.consume(entity);
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		finally {
			if(response != null) {
				try {
					response.close();
				}
				catch(Exception ex)
				{}
			}
		}
		return rxNormList;

	}

	private String getNameForCui(String cui) {
		CloseableHttpClient httpclient = HttpClients.createDefault();

		String name = null;
		String url = "https://rxnav.nlm.nih.gov/REST/rxcui/" + cui + "/property.json?propName=RxNorm%20Name";
		HttpGet httpGet = new HttpGet(url);
		
		CloseableHttpResponse response = null;
		try {		
			//GET 
			response = httpclient.execute(httpGet);
		    	    
			//Response -> JSON Object
			HttpEntity entity = response.getEntity();
			String jsonResponse = EntityUtils.toString(entity);
			InputStream is = new ByteArrayInputStream( jsonResponse.getBytes() );
			
			JsonReader jsonReader = Json.createReader(is);
			JsonObject respObject = jsonReader.readObject();
			jsonReader.close();
			
		    name = respObject.getJsonObject("propConceptGroup").getJsonArray("propConcept").get(0).asJsonObject().getString("propValue");
		    
		    EntityUtils.consume(entity);
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		finally {
			if(response != null) {
				try {
					response.close();
				}
				catch(Exception ex)
				{}
			}
		}
		return name;
	}

	private static RxNormLookup instance = null;

	/**
	 * Singleton-Pattern: Get object reference to work with
	 * @return
	 */
	public static RxNormLookup getInstance() {
		if(instance == null) {
			instance = new RxNormLookup();
		}
		return instance;
	}
}
