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
 * Represents one row in mimiciii.labevents
 * @author Stefanie Ververs
 *
 */
public class MLabevent {
	//Rekord-Datum
		 private Date acquisitionDate;
		 
		 //Type
		 private String measurementType; 
		 
		 //Value + ValueNum
		 private String value;
		 
		 private double numValue;
		 
		 private boolean hasNumVal;
		 
		 private boolean abnormal;
		 
		 private String fluid;
		 
		 private String loinc;
		 
		 public String getFluid() {
			return fluid;
		}

		public void setFluid(String fluid) {
			this.fluid = fluid;
		}

		public String getLoinc() {
			return loinc;
		}

		public void setLoinc(String loinc) {
			this.loinc = loinc;
		}

		public boolean isAbnormal() {
			return abnormal;
		}

		public void setAbnormal(boolean abnormal) {
			this.abnormal = abnormal;
		}

		public boolean hasNumVal() {
			return hasNumVal;
		}

		public void setHasNumVal(boolean hasNumVal) {
			this.hasNumVal = hasNumVal;
		}

		//Unit
		 private String unit;

		public Date getAcquisitionDate() {
			return acquisitionDate;
		}

		public void setAcquisitionDate(Date recordDate) {
			this.acquisitionDate = recordDate;
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
		 * Create FHIR-"Observation" resources for this data
 		 * @param patId Patient-FHIR-Resource-Id
 		 * @param encId Encounter-FHIR-Resource-Id
		 * @return FHIR-Observation
		 */
		public Observation getFhirObservation(String patId, String encId) {
			Observation observation = new Observation();
			
			observation.setStatus(ObservationStatus.FINAL);
			
			//all laboratory
			observation.addCategory().addCoding().setSystem("http://hl7.org/fhir/observation-category").setCode("laboratory").setDisplay("Laboratory");		
			
			CodeableConcept cc = new CodeableConcept();
			//Type of Observation
			if(this.getLoinc() != null) {
				cc.addCoding().setSystem("http://loinc.org").setCode(this.getLoinc());
				cc.setText(this.getMeasurementType());
			}
			else {
				//Representation as plain text if no loinc code available
				cc.setText(this.getMeasurementType());
			}
			observation.setCode(cc);
			
			
			//Pat-Reference
			observation.setSubject(new Reference(patId));
			
			//Enc-Reference
			observation.setContext(new Reference(encId));
			
			//Record-Date
			observation.setEffective(new DateTimeType(this.getAcquisitionDate()));
			
			//Performer is not available
			
			//Actual result
			if(this.hasNumVal()) {
				Quantity value = new Quantity();
				value.setValue(this.getNumValue());
				value.setUnit(this.getUnit());
				
				observation.setValue(value);
			}
			else
			{
				String value = this.getValue();
				//Unit added with "(<unit>)"
				if(this.getUnit() != null && this.getUnit().length() > 0) {
					value += " (" + this.getUnit() + ")";
				}
				observation.setValue(new StringType(value));
			}
			
			//Interpretation (from "flag")
			if(this.isAbnormal()) {
				cc = new CodeableConcept();
				cc.addCoding().setSystem("http://hl7.org/fhir/v2/0078").setCode("A").setDisplay("Abnormal");
				observation.setInterpretation(cc);
			}
			
			return observation;
		}
}
