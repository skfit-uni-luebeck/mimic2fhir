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

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.Bundle.BundleType;
import org.hl7.fhir.dstu3.model.Bundle.HTTPVerb;

/**
 * Handles bundle operations
 * @author Stefanie Ververs
 *
 */
public class BundleControl {
	private Bundle transactionBundle;
	private int numberOfResorces= 0;
	private int internalBundleNumber = 0;
	
	/**
	 * creates a new transaction bundle
	 */
	public BundleControl() {
		//new Bundle
		transactionBundle = new Bundle();
		transactionBundle.setType(BundleType.TRANSACTION);
		internalBundleNumber = 1;
	}	
	
	/**
	 * Number of resources currently present in bundle
	 * @return number of resources
	 */
	public int getNumberOfResorces() {
		return numberOfResorces;
	}
	
	/**
	 * Internal bundle number (how often bundle "reset"?) 
	 * @return internal bundle number
	 */
	public int getInternalBundleNumber() {
		return internalBundleNumber;
	}
	
	/**
	 * Reset internal bundle number to 1
	 */
	public void resetInternalBundleNumber() {
		internalBundleNumber = 1;
	}

	/**
	 * Reset bundle for "new" bundle with zero resources
	 */
	public void resetBundle() {
		transactionBundle = new Bundle();
		transactionBundle.setType(BundleType.TRANSACTION);
		numberOfResorces = 0;
		internalBundleNumber++;
	}
	
	/**
	 * Get the current bundle
	 * @return current bundle
	 */
	public Bundle getTransactionBundle() {
		return transactionBundle;
	}

	/**
	 * Add fhir resource without UUID to current bundle
	 * @param rToAdd fhir-resource to add
	 */
	public void addResourceToBundle(Resource rToAdd)
	{		
		transactionBundle.addEntry()
		   .setResource(rToAdd)
		   .getRequest()
		      .setUrl(rToAdd.fhirType())
		      .setMethod(HTTPVerb.POST);
		
		numberOfResorces++;

	}
	
	/**
	 * Add fhir resource with UUID to current bundle
	 * @param rToAdd fhir-resource to add
	 */
	public void addUUIDResourceToBundle(Resource rToAdd){
		transactionBundle.addEntry()
		   .setFullUrl(rToAdd.getId())
		   .setResource(rToAdd)
		   .getRequest()
		      .setUrl(rToAdd.fhirType())
		      .setMethod(HTTPVerb.POST);
		
		numberOfResorces++;
		
	}
	
	/**
	 * Conditional Create:
	 * Add fhir resource with UUID to current bundle and set condition (create if none exist)
	 * @param rToAdd fhir-resource to add
	 * @param condition search-condition to match 
	 */
	public void addUUIDResourceWithConditionToBundle(Resource rToAdd, String condition) {
		transactionBundle.addEntry()
		   .setFullUrl(rToAdd.getId())
		   .setResource(rToAdd)
		   .getRequest()
		      .setUrl(rToAdd.fhirType())
		      .setIfNoneExist(condition)
		      .setMethod(HTTPVerb.POST);
		
		numberOfResorces++;
	}
}
