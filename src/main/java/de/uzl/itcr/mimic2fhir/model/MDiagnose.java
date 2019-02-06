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
package de.uzl.itcr.mimic2fhir.model;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.Reference;

import ca.uhn.fhir.model.primitive.IdDt;

/**
 * Represents one diagnose in diagnoses_icd joined with d_icd_diagnoses
 * @author Stefanie Ververs
 *
 */
public class MDiagnose {
	private String icd9Code;
	private String shortTitle;
	private String longTitle;
	private int seqNumber;

	public int getSeqNumber() {
		return seqNumber;
	}
	public void setSeqNumber(int seqNumber) {
		this.seqNumber = seqNumber;
	}
	public String getIcd9Code() {
		return icd9Code;
	}
	public void setIcd9Code(String icd9Code) {
		this.icd9Code = icd9Code;
	}
	public String getShortTitle() {
		return shortTitle;
	}
	public void setShortTitle(String shortTitle) {
		this.shortTitle = shortTitle;
	}
	public String getLongTitle() {
		return longTitle;
	}
	public void setLongTitle(String longTitle) {
		this.longTitle = longTitle;
	}
	
	/**
	 * Create FHIR-"Condition"-Resource for this data
	 * @param patId Patient-FHIR-Resource-Id
	 * @param encId Encounter-Resource-Id
	 * @return FHIR-Condition
	 */
	public Condition getFhirCondition(String patId, String encId) {
		Condition cond = new Condition();
		
		//Patient
		cond.setSubject(new Reference(patId));
		
		//Identifier
		cond.addIdentifier().setSystem("http://www.imi-mimic.de/diags").setValue(encId + "_" + this.seqNumber);
		
		//Context -> Encounter  
		//cond.setContext(new Reference(encId));
		
		//Diagnose itself (Code + Text)
		CodeableConcept diagnoseCode = new CodeableConcept();
		diagnoseCode.addCoding().setSystem("http://hl7.org/fhir/sid/icd-9-cm").setCode(this.getIcd9Code())
						.setDisplay(this.getLongTitle());
		
		cond.setCode(diagnoseCode);
		
		// Give the condition a temporary UUID so that other resources in
		// the transaction can refer to it
		cond.setId(IdDt.newRandomUuid());
		
		return cond;
	}
}
