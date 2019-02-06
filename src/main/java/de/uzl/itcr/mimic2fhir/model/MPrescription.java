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
import org.hl7.fhir.dstu3.model.Medication;
import org.hl7.fhir.dstu3.model.Medication.MedicationIngredientComponent;

import ca.uhn.fhir.model.primitive.IdDt;
import de.uzl.itcr.mimic2fhir.tools.RxNormConcept;
import de.uzl.itcr.mimic2fhir.tools.RxNormLookup;

import org.hl7.fhir.dstu3.model.MedicationAdministration;
import org.hl7.fhir.dstu3.model.MedicationAdministration.MedicationAdministrationDosageComponent;
import org.hl7.fhir.dstu3.model.MedicationAdministration.MedicationAdministrationStatus;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.SimpleQuantity;

/**
 * Represents one row in mimiciii.prescriptions
 * @author Stefanie Ververs
 *
 */
public class MPrescription {
	private Date start;
	private Date end;
	private String drugtype;
	private String drug;
	private String drugNamePoe;
	private String drugNameGeneric;
	private String formularyDrugCd;
	//Generic Sequence Number
	private String gsn;
	//National Drug Code
	private String ndc;
	private String prodStrength;
	private String doseValRx;
	private String doseUnitRx;
	private String formValDisp;
	private String formUnitDisp;
	
	private String route;

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public String getDrugtype() {
		return drugtype;
	}

	public void setDrugtype(String drugtype) {
		this.drugtype = drugtype;
	}

	public String getDrug() {
		return drug;
	}

	public void setDrug(String drug) {
		this.drug = drug;
	}

	public String getDrugNamePoe() {
		return drugNamePoe;
	}

	public void setDrugNamePoe(String drugNamePoe) {
		this.drugNamePoe = drugNamePoe;
	}

	public String getDrugNameGeneric() {
		return drugNameGeneric;
	}

	public void setDrugNameGeneric(String drugNameGeneric) {
		this.drugNameGeneric = drugNameGeneric;
	}

	public String getFormularyDrugCd() {
		return formularyDrugCd;
	}

	public void setFormularyDrugCd(String formularyDrugCd) {
		this.formularyDrugCd = formularyDrugCd;
	}

	public String getGsn() {
		return gsn;
	}

	public void setGsn(String gsn) {
		this.gsn = gsn;
	}

	public String getNdc() {
		return ndc;
	}

	public void setNdc(String ndc) {
		this.ndc = ndc;
	}

	public String getProdStrength() {
		return prodStrength;
	}

	public void setProdStrength(String prodStrength) {
		this.prodStrength = prodStrength;
	}

	public String getDoseValRx() {
		return doseValRx;
	}

	public void setDoseValRx(String doseValRx) {
		this.doseValRx = doseValRx;
	}

	public String getDoseUnitRx() {
		return doseUnitRx;
	}

	public void setDoseUnitRx(String doseUnitRx) {
		this.doseUnitRx = doseUnitRx;
	}

	public String getFormValDisp() {
		return formValDisp;
	}

	public void setFormValDisp(String formValDisp) {
		this.formValDisp = formValDisp;
	}

	public String getFormUnitDisp() {
		return formUnitDisp;
	}

	public void setFormUnitDisp(String formUnitDisp) {
		this.formUnitDisp = formUnitDisp;
	}

	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		this.route = route;
	}
	
	/**
	 * Create FHIR-"Medication"-resource
	 * @return FHIR-Medication
	 */
	public Medication getFhirMedication() {
		Medication m = new Medication();
			
		//RxNorm
		List<RxNormConcept> rxNorm = null;
		String existingCode = null;
		if(this.ndc != null && this.ndc.compareTo("0") != 0) {
			//we do have a NDC:
			existingCode = this.ndc;
			rxNorm = RxNormLookup.getInstance().getRxNormForNdc(this.ndc);
		}
		
		if(rxNorm == null && this.gsn != null) {
			//no result for ndc, but gsn -> try again
			if(existingCode == null) {
				existingCode = this.gsn;
			}
			
			String[] gsnSingles = this.gsn.split(" ");
			//Multiple GSN-Codes possible - take all..
			rxNorm = new ArrayList<RxNormConcept>();
			for(String gsnSingle : gsnSingles)
			{
				rxNorm.addAll(RxNormLookup.getInstance().getRxNormForGsn(gsnSingle.trim()));
			}
		}
		
		CodeableConcept cc = new CodeableConcept();
		if(rxNorm != null && !rxNorm.isEmpty()) {
			for(RxNormConcept rx : rxNorm) {
				cc.addCoding().setSystem("http://www.nlm.nih.gov/research/umls/rxnorm").setCode(rx.getCui()).setDisplay(rx.getName()); 
			}
		}
		else {
			if((rxNorm == null || rxNorm.isEmpty()) && this.formularyDrugCd != null) {
				//seem to be some mnemonic codes 
				if(existingCode == null) {
					existingCode = this.formularyDrugCd;
				}
				cc.addCoding().setCode(this.formularyDrugCd); //maybe add a system
			}else {
				if(existingCode == null) {
					existingCode = this.drug  + "(Text Only)";
				}
				cc.setText(this.drug);
				cc.addCoding().setCode(existingCode);
			}
		}
		cc.setText(this.drug);
		m.setCode(cc);
			
		//ingredient --> prod strength?
		if(this.getProdStrength() != null) {
			CodeableConcept ci = new CodeableConcept();
			ci.setText(this.getProdStrength());
			m.addIngredient(new MedicationIngredientComponent(ci));
		}
		
		m.setId(IdDt.newRandomUuid());
		
		return m;
	}
	
	/**
	 * Create FHIR-"MedicationAdministration"-resource from this data
	 * @param patId Patient-FHIR-Resource-Id
	 * @param encId Encounter-FHIR-Resource-Id
	 * @return FHIR-MedicationAdministration
	 */
	public MedicationAdministration getFhirMedAdministration(String patId, String encId, int seqNum) {
		MedicationAdministration ma = new MedicationAdministration();
		
		ma.addIdentifier().setSystem("http://www.imi-mimic.de/prescriptions").setValue(encId + "_" + seqNum);
		
		ma.setStatus(MedicationAdministrationStatus.COMPLETED);

		ma.setSubject(new Reference(patId));
		ma.setContext(new Reference(encId));
		
		ma.setEffective(new Period().setEnd(end).setStart(start));
		
		MedicationAdministrationDosageComponent mad = new MedicationAdministrationDosageComponent();
		
		if(this.route != null) {
			CodeableConcept route = new CodeableConcept();
			switch(this.route) {
				case "IV":
				case "IV BOLUS":
				case "IV DRIP":
				case "IVPCA":
				case "IVS":
				case "PB":
					route.addCoding().setSystem("http://snomed.info/sct").setDisplay("Intravenous route").setCode("47625008");
					break;
				case "PO":
				case "PO/OG":
				case "ORAL":
				case "PO OR ENTERAL TUBE":
					route.addCoding().setSystem("http://snomed.info/sct").setDisplay("Oral Route").setCode("26643006");
					break;
				case "PO/NG":
				case "NG/OG":
				case "NG":
					route.addCoding().setSystem("http://snomed.info/sct").setDisplay("Nasogastric route").setCode("127492001");
					break;
				case "PR":
				case "RECTAL":
					route.addCoding().setSystem("http://snomed.info/sct").setDisplay("Per rectum").setCode("37161004");
					break;	
				case "INTRAPERICARDIAL":
					route.addCoding().setSystem("http://snomed.info/sct").setDisplay("Intrapericardial route").setCode("445771006");
					break;	
				case "RIGHT EYE":
				case "LEFT EYE":
				case "BOTH EYES":
				case "OS":
				case "OD":
				case "OU":
					route.addCoding().setSystem("http://snomed.info/sct").setDisplay("Ophthalmic route").setCode("54485002");
					break;
				case "SC":
				case "SUBCUT":
					route.addCoding().setSystem("http://snomed.info/sct").setDisplay("Subcutaneous route").setCode("34206005");
					break;
				case "IH":
				case "AERO":
				case "INHALATION":
				case "NEB":
					route.addCoding().setSystem("http://snomed.info/sct").setDisplay("Respiratory tract route").setCode("447694001");
					break;
				case "ID":
					route.addCoding().setSystem("http://snomed.info/sct").setDisplay("Intradermal use").setCode("372464004");
					break;
				case "LEFT EAR":
				case "RIGHT EAR":
				case "BOTH EARS":
					route.addCoding().setSystem("http://snomed.info/sct").setDisplay("Otic route").setCode("10547007");
					break;
				case "IC":
					route.addCoding().setSystem("http://snomed.info/sct").setDisplay("Intracardiac use").setCode("372460008");
					break;
				case "IN":
				case "NAS":
				case "NU":
					route.addCoding().setSystem("http://snomed.info/sct").setDisplay("Nasal route").setCode("46713006");
					break;
				case "IM":
					route.addCoding().setSystem("http://snomed.info/sct").setDisplay("Intramuscular route").setCode("78421000");
					break;	
				case "BUCCAL":
				case "BU":
					route.addCoding().setSystem("http://snomed.info/sct").setDisplay("Buccal route").setCode("54471007");
					break;	
				case "TP": //topic
					route.addCoding().setSystem("http://snomed.info/sct").setDisplay("Topical route").setCode("6064005");
					break;
				case "ED":
					route.addCoding().setSystem("http://snomed.info/sct").setDisplay("Epidural route").setCode("404820008");
					break;
				case "TD":
					route.addCoding().setSystem("http://snomed.info/sct").setDisplay("Transdermal route	").setCode("45890007");
					break;
				case "IT":
					route.addCoding().setSystem("http://snomed.info/sct").setDisplay("Intrathecal route").setCode("72607000");
					break;
				case "SL":
					route.addCoding().setSystem("http://snomed.info/sct").setDisplay("Sublingual route").setCode("37839007");
					break;
				case "G TUBE":
					route.addCoding().setSystem("http://snomed.info/sct").setDisplay("Gastrostomy route").setCode("127490009");
					break;
				case "VG":
					route.addCoding().setSystem("http://snomed.info/sct").setDisplay("Per vagina").setCode("16857009");
					break;
				case "IP":
					route.addCoding().setSystem("http://snomed.info/sct").setDisplay("Intraperitoneal route").setCode("38239002");
					break;
				case "J TUBE":
					route.addCoding().setSystem("http://snomed.info/sct").setDisplay("Jejunostomy route").setCode("127491008");
					break;
				case "ET":
					route.addCoding().setSystem("http://snomed.info/sct").setDisplay("Intratracheal route").setCode("404818005");
					break;
				default:
					route.setText(this.route);
					break;
			}
			mad.setRoute(route);
		}
	
		//Dosage:
		//Create text if possible:
		String doseText = "";
		if(this.getDoseValRx() != null && this.getDoseUnitRx() != null) {
			doseText = this.getDoseValRx() + " " + this.getDoseUnitRx();
		}
		
		if(this.getFormValDisp() != null && this.getFormUnitDisp() != null) {
			if(doseText.length() > 0) {
				doseText += " (" + this.getFormValDisp() + " " + this.getFormUnitDisp() + ")";
			}else
			{
				doseText = this.getFormValDisp() + " " + this.getFormUnitDisp();
			}
		}
		
		if(doseText.length() > 0) {
			mad.setText(doseText);
		}
		
		//doseVal -> 30, doseUnit -> mg;
		//Amount of medication per dose -> given at one event
		double sqValue = 0.0;
		try {
			sqValue = Double.parseDouble(this.getDoseValRx());
			mad.setDose((SimpleQuantity) new SimpleQuantity().setValue(sqValue).setUnit(this.getDoseUnitRx()));
		}catch(NumberFormatException nfe) {
			
		}
		catch(NullPointerException xpe) {
			
		}
		//rate -> Speed
		ma.setDosage(mad);
		
		return ma;
	}
	
	
}
