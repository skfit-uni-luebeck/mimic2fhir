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
package de.uzl.itcr.mimic2fhir.work;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import org.hl7.fhir.dstu3.model.Bundle;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.narrative.DefaultThymeleafNarrativeGenerator;
import ca.uhn.fhir.rest.client.apache.GZipContentInterceptor;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;

/**
 * Communication and Functions for and with FHIR
 * @author Stefanie Ververs
 *
 */
public class FHIRComm {
	private FhirContext ctx;
	private IGenericClient client;
	
	private Config configuration;
	
	/**
	 * Create new Fhir-Context with config-Object
	 * @param config config-Object
	 */
	public FHIRComm(Config config) {
		this.configuration = config;
		ctx = FhirContext.forDstu3();
		
		// Use the narrative generator
		ctx.setNarrativeGenerator(new DefaultThymeleafNarrativeGenerator());
		client = ctx.newRestfulGenericClient(configuration.getFhirServer());	
		
		if(this.configuration.isAuthRequired())
		{			 
			//Authorization
			BearerTokenAuthInterceptor authInterceptor = new BearerTokenAuthInterceptor(this.configuration.getToken());
			client.registerInterceptor(authInterceptor);
		}
				 
		// Set how long to block for individual read/write operations (in ms)
		ctx.getRestfulClientFactory().setSocketTimeout(1500 * 1000);
		
		//Gzip output content
		client.registerInterceptor(new GZipContentInterceptor());
	}
	
	/**
	 * Print bundle as xml to console 
	 * @param transactionBundle bundle to print
	 */
	public void printBundleAsXml(Bundle transactionBundle) {
		System.out.println(getBundleAsString(transactionBundle));
						
	}
	
	/**
	 * Save FHIR-Ressource-Bundle as xml to location specified in Config
	 * @param number Number of bundle. Use 0, if no number in file name wanted ("bundle.xml")
	 * @param transactionBundle bundle to print to file
	 */
	public void printBundleAsXmlToFile(String number, Bundle transactionBundle) {
		try {
			String xml = getBundleAsString(transactionBundle);
			
			String fullFilePath;
			if(!number.equals("0")) {
				fullFilePath = configuration.getFhirxmlFilePath() + "\\bundle" + number + ".xml";
			}
			else{
				fullFilePath = configuration.getFhirxmlFilePath() + "\\bundle.xml";
			}
			
			Path path = Paths.get(fullFilePath);
		    byte[] strToBytes = xml.getBytes();
	 
		    //Write xml as file
			Files.write(path, strToBytes);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
		
	
	
	/**
	 * Send complete bundle to fhir-server
	 * @param transactionBundle bundle to push to server
	 */
	public void bundleToServer(Bundle transactionBundle)
	{
		Bundle resp = client.transaction().withBundle(transactionBundle).execute();
		
		//Log response 
		//writeToFile(ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(resp));
	}
	
	private void writeToFile(String text) {
		try {
			
			String fullFilePath = configuration.getFhirxmlFilePath() + "\\log" + new Date().getTime() +".xml";
			
			Path path = Paths.get(fullFilePath);
		    byte[] strToBytes = text.getBytes();
	 
		    //Write xml as file
			Files.write(path, strToBytes);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	/**
	 * Get a bundle as xml string representation
	 * @param bundle bundle to transform into a string
	 * @return bundle xml string
	 */
	public String getBundleAsString(Bundle bundle) {
		return ctx.newXmlParser()
				.setPrettyPrint(true)
				.encodeResourceToString(bundle);
	}
	
	/**
	 * Parse xml string to bundle
	 * @param bundle bundle as xml string
	 * @return bundle as Bundle
	 */
	public Bundle getBundleFromString(String bundle) {
		return (Bundle) ctx.newXmlParser()
				.setPrettyPrint(true)
				.parseResource(bundle);
	}

}
