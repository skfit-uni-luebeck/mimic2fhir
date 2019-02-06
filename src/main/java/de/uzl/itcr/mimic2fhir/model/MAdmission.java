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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Encounter.EncounterHospitalizationComponent;
import org.hl7.fhir.dstu3.model.Encounter.EncounterStatus;
import org.hl7.fhir.dstu3.model.Medication;
import org.hl7.fhir.dstu3.model.MedicationAdministration;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Procedure;
import org.hl7.fhir.dstu3.model.Reference;

import ca.uhn.fhir.model.primitive.IdDt;

/**
 * Represents one row (and references) in mimiciii.admissions
 * @author Stefanie Ververs
 *
 */
public class MAdmission {
	
	public MAdmission() {
		diagnoses = new ArrayList<MDiagnose>();
		procedures = new ArrayList<MProcedure>();
		events = new ArrayList<MChartevent>();
		labevents = new ArrayList<MLabevent>();
		noteevents = new ArrayList<MNoteevent>();
		prescriptions = new ArrayList<MPrescription>();
		transfers = new ArrayList<MTransfer>();
	}
	
	private String admissionId;
	private String maritalStatus;
	private String language;
	private String religion;
	
	private Date admissionTime;
	private Date dischargeTime;
	private String admissionType;
	private String dischargeLocation;
	private String admissionLocation;
	
	private List<MChartevent> events;
	private List<MLabevent> labevents;
	private List<MNoteevent> noteevents;
	
	private List<MDiagnose> diagnoses;
	private List<MProcedure> procedures;
	
	private List<MPrescription> prescriptions;
	
	private List<MTransfer> transfers;
	
	public List<MTransfer> getTransfers(){
		return transfers;
	}
	
	public void addTransfer(MTransfer proc) {
		transfers.add(proc);
	}
	
	public List<MPrescription> getPrescriptions(){
		return prescriptions;
	}
	
	public void addPrescription(MPrescription proc) {
		prescriptions.add(proc);
	}
	
	public List<MProcedure> getProcedures(){
		return procedures;
	}
	
	public void addProcedure(MProcedure proc) {
		procedures.add(proc);
	}
	
	public List<MDiagnose> getDiagnoses() {
		return diagnoses;
	}
	public void addDiagnose(MDiagnose diag) {
		diagnoses.add(diag);
	}
	
	public List<MChartevent> getEvents() {
		return events;
	}
	public void addEvent(MChartevent event) {
		events.add(event);
	}
	
	public List<MLabevent> getLabEvents() {
		return labevents;
	}
	public void addLabEvent(MLabevent event) {
		labevents.add(event);
	}
	
	public List<MNoteevent> getNoteevents() {
		return noteevents;
	}
	public void addNoteEvent(MNoteevent event) {
		noteevents.add(event);
	}
	
	public String getAdmissionId() {
		return admissionId;
	}
	public void setAdmissionId(String admissionId) {
		this.admissionId = admissionId;
	}
	
	public String getMaritalStatus() {
		return maritalStatus;
	}
	public void setMaritalStatus(String maritalStatus) {
		this.maritalStatus = maritalStatus;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getReligion() {
		return religion;
	}
	public void setReligion(String religion) {
		this.religion = religion;
	}
	
	public String getAdmissionLocation() {
		return admissionLocation;
	}
	public void setAdmissionLocation(String admissionLocation) {
		this.admissionLocation = admissionLocation;
	}
	public String getDischargeLocation() {
		return dischargeLocation;
	}
	public void setDischargeLocation(String dischargeLocation) {
		this.dischargeLocation = dischargeLocation;
	}
	public String getAdmissionType() {
		return admissionType;
	}
	public void setAdmissionType(String admissionType) {
		this.admissionType = admissionType;
	}
	public Date getAdmissionTime() {
		return admissionTime;
	}
	public void setAdmissionTime(Date admissionTime) {
		this.admissionTime = admissionTime;
	}
	public Date getDischargeTime() {
		return dischargeTime;
	}
	public void setDischargeTime(Date dischargeTime) {
		this.dischargeTime = dischargeTime;
	}
	
	/**
	 * Create all FHIR-"Condition"s for each diagnose
	 * @param patId Patient-FHIR-Resource-Id
	 * @return List with all resources
	 */
	public List<Condition> createFhirConditionsFromMimic(String patId) {
		
		List<Condition> conditions = new ArrayList<Condition>();		
			for(MDiagnose d : diagnoses) {
				conditions.add(d.getFhirCondition(patId, getAdmissionId()));
			}
		return conditions;
	}
	
	/**
	 * Create the FHIR-"Encounter"-resource for the mimic admission
	 * @param patId Patient-FHIR-Resource-Id
	 * @return FHIR-Encounter
	 */
	public Encounter createFhirEncounterFromMimic(String patId) {
		
			Encounter enc = new Encounter();
			
			//Id
			enc.addIdentifier().setSystem("http://www.imi-mimic.de/encs").setValue(getAdmissionId());
			
			//Patient
			enc.setSubject(new Reference(patId));
			
			//Period
			enc.setPeriod(new Period().setStart(getAdmissionTime()).setEnd(getDischargeTime()));
			
			//all admissions are finished
			enc.setStatus(EncounterStatus.FINISHED); 
			
			//AdmissionType -> Class
			//‘ELECTIVE’, ‘URGENT’, ‘NEWBORN’ or ‘EMERGENCY’
			switch(admissionType) {
				case "ELECTIVCE":
					enc.setClass_(new Coding().setCode("IMP").setSystem("http://hl7.org/fhir/v3/ActCode").setDisplay("inpatient encounter"));
					break;
				case "URGENT":
					enc.setClass_(new Coding().setCode("ACUTE").setSystem("http://hl7.org/fhir/v3/ActCode").setDisplay("inpatient acute"));
					break;
				case "EMERGENCY":
					enc.setClass_(new Coding().setCode("EMER").setSystem("http://hl7.org/fhir/v3/ActCode").setDisplay("emergency"));
					break;
				case "NEWBORN":
					enc.setClass_(new Coding().setCode("NEWB").setSystem("http://hl7.org/fhir/v3/ActCode").setDisplay("newborn"));
					break;
			}
			
			//Discharge Location
			EncounterHospitalizationComponent ehc = new EncounterHospitalizationComponent();
			CodeableConcept discharge = new CodeableConcept();
			switch(getDischargeLocation()) {
				case "HOME":
				case "HOME WITH HOME IV PROVIDR":
				case "HOME HEALTH CARE":
					discharge.addCoding().setSystem("http://hl7.org/fhir/discharge-disposition").setCode("home").setDisplay("Home");
					break;
				case "HOSPICE-MEDICAL FACILITY":
				case "HOSPICE-HOME":
					discharge.addCoding().setSystem("http://hl7.org/fhir/discharge-disposition").setCode("hosp").setDisplay("Hospice");
					break;
				case "REHAB/DISTINCT PART HOSP":
					discharge.addCoding().setSystem("http://hl7.org/fhir/discharge-disposition").setCode("rehab").setDisplay("Rehabilitation");
					break;
				case "DISC-TRAN CANCER/CHLDRN H":
				case "OTHER FACILITY":
				case "DISC-TRAN TO FEDERAL HC":
				case "SHORT TERM HOSPITAL":
				case "ICF":
					discharge.addCoding().setSystem("http://hl7.org/fhir/discharge-disposition").setCode("other-hcf").setDisplay("Other healthcare facility");
					break;
				case "DISCH-TRAN TO PSYCH HOSP":
					discharge.addCoding().setSystem("http://hl7.org/fhir/discharge-disposition").setCode("psy").setDisplay("Psychiatric hospital");
					break;
				case "DEAD/EXPIRED":
					discharge.addCoding().setSystem("http://hl7.org/fhir/discharge-disposition").setCode("exp").setDisplay("Expired");
					break;
				case "LEFT AGAINST MEDICAL ADVI":
					discharge.addCoding().setSystem("http://hl7.org/fhir/discharge-disposition").setCode("aadvice").setDisplay("Left against advice");
					break;
				case "LONG TERM CARE HOSPITAL":
					discharge.addCoding().setSystem("http://hl7.org/fhir/discharge-disposition").setCode("long").setDisplay("Long-term care");
					break;
				case "SNF":
				case "SNF-MEDICAID ONLY CERTIF":
					discharge.addCoding().setSystem("http://hl7.org/fhir/discharge-disposition").setCode("snf").setDisplay("Skilled nursing factory");
					break;
				default:
					discharge.addCoding().setSystem("http://hl7.org/fhir/discharge-disposition").setCode("oth").setDisplay("Other");
					break;
				
			}
			ehc.setDischargeDisposition(discharge);
			
			//Admit Source from Admission location
			CodeableConcept cal = new CodeableConcept();
			
			switch(this.admissionLocation) {
				case "PHYS REFERRAL/NORMAL DELI":
				case "HMO REFERRAL/SICK":		
					cal.addCoding().setCode("mp").setDisplay("Medical Practitioner/physician referral").setSystem("http://hl7.org/fhir/admit-source");
					break;
				case "TRSF WITHIN THIS FACILITY":
					cal.addCoding().setCode("other").setDisplay("Other").setSystem("http://hl7.org/fhir/admit-source");
					break;
				case "TRANSFER FROM SKILLED NUR":
					cal.addCoding().setCode("nursing").setDisplay("From nursing home").setSystem("http://hl7.org/fhir/admit-source");
					break;
				case "** INFO NOT AVAILABLE **":
					cal.setText("Not available");
					break;
				case "CLINIC REFERRAL/PREMATURE":
				case "TRANSFER FROM HOSP/EXTRAM":
					cal.addCoding().setCode("hosp-trans").setDisplay("Transferred from other hospital").setSystem("http://hl7.org/fhir/admit-source");
					break;
				case "TRANSFER FROM OTHER HEALT":
					cal.addCoding().setCode("other").setDisplay("Other").setSystem("http://hl7.org/fhir/admit-source");
					break;
				case "EMERGENCY ROOM ADMIT":
					cal.addCoding().setCode("emd").setDisplay("From accident/emergency department").setSystem("http://hl7.org/fhir/admit-source");
					break;
			}
			ehc.setAdmitSource(cal);
			enc.setHospitalization(ehc);
				

			// Give the encounter a temporary UUID so that other resources in
			// the transaction can refer to it
			enc.setId(IdDt.newRandomUuid());
			
			return enc;
	}
	
	/**
	 * Create all FHIR-"Observation"s for this encounter (Chartevents)
	 * @param patId Patient-FHIR-Resource-Id
	 * @param encId Encounter-FHIR-Resource-Id
	 * @return List with all FHIR Observations
	 */
	public List<Observation> createFhirObservationsFromMimic(String patId, String encId){
		List<Observation> obs = new ArrayList<Observation>();
		
		for(MChartevent event : this.events) {
			obs.add(event.getFhirObservation(patId, encId));
		}
		
		return obs;
	}
	
	/**
	 * Create all FHIR-"Observation"s for this encounter (Labevents)
	 * @param patId Patient-FHIR-Resource-Id
	 * @param encId Encounter-FHIR-Resource-Id
	 * @return List with all FHIR Observations
	 */
	public List<Observation> createFhirLabObservationsFromMimic(String patId, String encId){
		List<Observation> obs = new ArrayList<Observation>();
		
		for(MLabevent event : this.labevents) {
			obs.add(event.getFhirObservation(patId, encId));
		}
		
		return obs;
	}
	
	
	/**
	 * Create all FHIR-"Observation"s for this encounter (Noteevents)
	 * @param patId Patient-FHIR-Resource-Id
	 * @param encId Encounter-FHIR-Resource-Id
	 * @return List with all FHIR Observations
	 */
	public List<Observation> createFhirNoteObservationsFromMimic(String patId, String encId){
		List<Observation> obs = new ArrayList<Observation>();
		
		for(MNoteevent event : this.noteevents) {
			Observation observation = event.getFhirObservation(patId, encId);
			
			obs.add(observation);
		}
		
		return obs;
	}
	
	/**
	 * Create all FHIR-"Procedure"s for each Procedure for this mimic admission
	 * @param patId Patient-FHIR-Resource-Id
	 * @return List with all FHIR-Procedures
	 */
	public List<Procedure> createFhirProceduresFromMimic(String patId) {
		
		List<Procedure> procedures = new ArrayList<Procedure>();		
			for(MProcedure p : this.procedures) {			
				procedures.add(p.getFhirProcedure(patId, getAdmissionId()));
			}
		return procedures;
	}
	
	/**
	 * Create all FHIR-"Medication"s for this encounter
	 * @return List with all FHIR Medication
	 */
	public List<Medication> createFhirMedicationsFromMimic() {
		
		List<Medication> medications = new ArrayList<Medication>();		
			for(MPrescription p : this.prescriptions) {			
				medications.add(p.getFhirMedication());
			}
		return medications;
	}

	/**
	 * Create all FHIR-"MedicationAdministration"s for this encounter
	 * @param patId Patient-FHIR-Resource-Id
	 * @param encId Encounter-FHIR-Resource-Id
	 * @return List with all MedicationAdministration
	 */
	public List<MedicationAdministration> createFhirMedAdminsFromMimic(String patId, String encId) {
	
		List<MedicationAdministration> administrations = new ArrayList<MedicationAdministration>();	
	
		int index = 0;
		for(MPrescription p : this.prescriptions) {			
			administrations.add(p.getFhirMedAdministration(patId, encId, index++));
		}
		return administrations;
	}
}
