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

import java.util.Date;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Quantity;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.dstu3.model.Observation.ObservationStatus;

/**
 * Represents one row in mimiciii.chartevents
 * @author Stefanie Ververs
 *
 */
public class MChartevent {
	 //Rekord-Datum
	 private Date recordDate;
	 
	 //CareGiver
	 private int careGiverId;
	 
	 //Type
	 private String measurementType; 
	 
	 //Value + ValueNum
	 private String value;

	//Unit
	 private String unit;
	 
	 private double numValue;
	 
	 private boolean hasNumVal;
	 
	 public boolean hasNumVal() {
		return hasNumVal;
	}

	public void setHasNumVal(boolean hasNumVal) {
		this.hasNumVal = hasNumVal;
	}

	public Date getRecordDate() {
		return recordDate;
	}

	public void setRecordDate(Date recordDate) {
		this.recordDate = recordDate;
	}

	public int getCareGiverId() {
		return careGiverId;
	}

	public void setCareGiverId(int careGiverId) {
		this.careGiverId = careGiverId;
	}

	public String getMeasurementType() {
		return measurementType;
	}

	public void setMeasurementType(String measurementType) {
		this.measurementType = measurementType;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public double getNumValue() {
		return numValue;
	}

	public void setNumValue(double numValue) {
		this.hasNumVal = true;
		this.numValue = numValue;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}
	
	/**
	 * Create FHIR-"Observation"-resource from this data
	 * @param patId Patient-FHIR-Resource-Id
	 * @param encId Encounter-FHIR-Resource-Id
	 * @return FHIR-Observation
	 */
	public Observation getFhirObservation(String patId, String encId) {
		Observation observation = new Observation();
		
		observation.setStatus(ObservationStatus.FINAL);
		
		//Expect all chartevents to be vital signs
		observation.addCategory().addCoding().setSystem("http://hl7.org/fhir/observation-category").setCode("vital_signs").setDisplay("Vital Signs");		
		
		//Type of Observation
		//D_Items in Mimic doesn't relate the measurement types to any coding system or terminology
		// => Representation as plain text
		CodeableConcept cc = new CodeableConcept();
		cc.setText(this.getMeasurementType());
		observation.setCode(cc);
		
		//Pat-Reference
		observation.setSubject(new Reference(patId));
		
		//Enc-Reference
		observation.setContext(new Reference(encId));
		
		//Record-Date
		observation.setEffective(new DateTimeType(this.getRecordDate()));
		
		//Performer will be set later
		
		//Actual result
		if(this.hasNumVal()) {
			Quantity value = new Quantity();
			value.setValue(this.getNumValue());
			value.setUnit(this.getUnit());
			
			observation.setValue(value);
		}
		else
		{
			observation.setValue(new StringType(this.getValue()));
			//no units in data 
		}
		return observation;
	}
}
