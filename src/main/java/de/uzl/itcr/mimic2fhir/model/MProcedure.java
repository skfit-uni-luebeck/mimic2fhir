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
import org.hl7.fhir.dstu3.model.Procedure;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Procedure.ProcedureStatus;

import ca.uhn.fhir.model.primitive.IdDt;

/**
 * Represents one row in mimiciii.procedures_icd joined with d_icd_procedures
 * @author Stefanie Ververs
 *
 */
public class MProcedure {
	private String icd9Code;
	private String shortTitle;
	private String longTitle;
	private int seqNumber;
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
	public int getSeqNumber() {
		return seqNumber;
	}
	public void setSeqNumber(int seqNumber) {
		this.seqNumber = seqNumber;
	}
	
	/**
	 * Create FHIR-"Procedure" from this data
	 * @param patId Patient-FHIR-Resource-Id
	 * @return FHIR-Procedure
	 */
	public Procedure getFhirProcedure(String patId, String encId) {
		Procedure proc = new Procedure();
		
		//Patient
		proc.setSubject(new Reference(patId));
		
		//Identifier
		proc.addIdentifier().setSystem("http://www.imi-mimic.de/procs").setValue(encId + "_" + this.seqNumber);
		
		//Context -> Encounter  
		//cond.setContext(new Reference(encId));
		
		//State
		proc.setStatus(ProcedureStatus.COMPLETED);
		
		//Procedure itself (Code + Text)
		CodeableConcept procedureCode = new CodeableConcept();
		procedureCode.addCoding().setSystem("http://hl7.org/fhir/sid/icd-9-cm").setCode(this.getIcd9Code())
						.setDisplay(this.getLongTitle());
		
		proc.setCode(procedureCode);
		
		// Give the procedure a temporary UUID so that other resources in
		// the transaction can refer to it
		proc.setId(IdDt.newRandomUuid());
		
		return proc;
	}
}
