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
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.dstu3.model.HumanName.NameUse;
import org.hl7.fhir.dstu3.model.Patient;

import ca.uhn.fhir.model.primitive.IdDt;

/**
 * FHIR-Patient with data from mimic3, one row in mimiciii.patients
 * @author Stefanie Ververs
 *
 */
public class MPatient {
	
	public MPatient() {
		admissions = new ArrayList<MAdmission>();
	}
	
	public void addAdmission(MAdmission adm) {
		admissions.add(adm);
	}
	
	private List<MAdmission> admissions;
	
	public List<MAdmission> getAdmissions() {
		return admissions;
	}

	private String patientSubjectId;
	public String getPatientSubjectId() {
		return patientSubjectId;
	}

	public void setPatientSubjectId(String patientSubjectId) {
		this.patientSubjectId = patientSubjectId;
	}

	public Date getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public Date getDeathDate() {
		return deathDate;
	}

	public void setDeathDate(Date deathDate) {
		this.deathDate = deathDate;
	}

	private Date birthDate;
	private String gender;
	private Date deathDate;
	
	/**
	 * Create FHIR-"Patient"-resource from this data
	 * @return FHIR-Patient
	 */
	public Patient createFhirFromMimic() {
		Patient pMimic = new Patient();
		
		//ID:
		pMimic.addIdentifier().setSystem("http://www.imi-mimic.de/patients").setValue(patientSubjectId);

		//Name : Patient_ID
		pMimic.addName().setUse(NameUse.OFFICIAL).setFamily("Patient_" + patientSubjectId);
		
		//Date of Birth
		pMimic.setBirthDate(birthDate);
		
		//Date of Death
		if(deathDate != null) {
			pMimic.setDeceased(new DateTimeType(deathDate));
		}
		
		//Gender
		switch(gender) {
			case "M":
				pMimic.setGender(AdministrativeGender.MALE);
				break;
			case "F":
				pMimic.setGender(AdministrativeGender.FEMALE);
				break;
			default:
				pMimic.setGender(AdministrativeGender.UNKNOWN);
		}
		
		if(admissions.size() > 0) {
			//from first admission
			MAdmission firstAdm = admissions.get(0);
			
			//Marital Status - 
			CodeableConcept cc = new CodeableConcept();
			
			if(firstAdm.getMaritalStatus() != null){
				switch(firstAdm.getMaritalStatus()) {
					case "MARRIED":
						cc.addCoding().setCode("M").setSystem("http://hl7.org/fhir/v3/MaritalStatus").setDisplay("Married");
						break;
					case "SINGLE":
						cc.addCoding().setCode("S").setSystem("http://hl7.org/fhir/v3/MaritalStatus").setDisplay("Never Married");
							break;
					case "WIDOWED":
						cc.addCoding().setCode("W").setSystem("http://hl7.org/fhir/v3/MaritalStatus").setDisplay("Widowed");
							break;
					case "DIVORCED":
						cc.addCoding().setCode("D").setSystem("http://hl7.org/fhir/v3/MaritalStatus").setDisplay("Divorced");
						break;
					case "SEPARATED":
						cc.addCoding().setCode("L").setSystem("http://hl7.org/fhir/v3/MaritalStatus").setDisplay("Legally Separated");
						break;
					default:
						cc.addCoding().setCode("UNK").setSystem("http://hl7.org/fhir/v3/MaritalStatus").setDisplay("Unknown");
				}
				pMimic.setMaritalStatus(cc);	
			}
			
			//Language
			if(firstAdm.getLanguage() != null) {
				CodeableConcept lc = new CodeableConcept();
				//Languages sometimes guessed - no dictionary or something in mimic..
				switch(firstAdm.getLanguage()) {
				case "*DUT":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("nl").setDisplay("Dutch");
					break;
				case "URDU":
				case "*URD":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("ur").setDisplay("Urdu");
					break;
				case "*NEP":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("ne").setDisplay("Nepali");
					break;
				case "TAGA":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("tl").setDisplay("Tagalog");
					break;
				case "*TOY":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("toy").setDisplay("Topoiyo");
					break;
				case "*RUS":
				case "RUSS":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("ru").setDisplay("Russian");
					break;
				case "ENGL":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("en").setDisplay("English");
					break;
				case "*ARM":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("hy").setDisplay("Armenian");
					break;
				case "CANT":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("yue").setDisplay("Cantonese");
					break;
				case "LAOT":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("tyl").setDisplay("Thu Lao");
					break;
				case "*MOR":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("mor").setDisplay("Moro");
					break;
				case "*FUL":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("ff").setDisplay("Fulah");
					break;
				case "*ROM":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("ro").setDisplay("Romanian");
					break;
				case "*TOI":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("toi").setDisplay("Tonga");
					break;
				case "BENG":
				case "*BEN":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("bn").setDisplay("Bengali");
					break;
				case "**TO":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("to").setDisplay("Tonga");
					break;
				case "PERS":
				case "*PER":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("fa").setDisplay("Persian");
					break;
				case "*TEL":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("te").setDisplay("Telugu");
					break;
				case "*YID":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("ji").setDisplay("Yiddish");
					break;
				case "*CDI":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("cdi").setDisplay("Chodri");
					break;
				case "JAPA":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("jp").setDisplay("Japanese");
					break;
				case "ALBA":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("sq").setDisplay("Albanian");
					break;
				case "ARAB":
				case "*ARA":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("ar").setDisplay("Arabic");
					break;
				case "ITAL":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("it").setDisplay("Italian");
					break;
				case "*TAM":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("taq").setDisplay("Tamasheq");
					break;
				case "*SPA":
				case "SPAN":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("es").setDisplay("Spanish");
					break;
				case "*BOS":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("bs").setDisplay("Bosnian");
					break;
				case "*AMH":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("am").setDisplay("Amharic");
					break;
				case "SOMA":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("so").setDisplay("Somali");
					break;
				case "CAPE":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("cap").setDisplay("Chipaya");
					break;
				case "*PUN":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("pa").setDisplay("Punjabi");
					break;
				case "POLI":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("pl").setDisplay("Polish");
					break;
				case "*CHI":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("zh").setDisplay("Chinese");
					break;
				case "*BUR":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("my").setDisplay("Burmese");
					break;
				case "*CAN":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("can").setDisplay("Chambri");
					break;
				case "*YOR":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("yox").setDisplay("Yoron");
					break;
				case "*KHM":
				case "CAMB":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("km").setDisplay("Central Khmer");
					break;
				case "AMER":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("en").setDisplay("English");
					break;
				case "*LIT":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("lt").setDisplay("Lithuanian");
					break;
				case "*IBO":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("ibn").setDisplay("Ibino");
					break;
				case "KORE":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("ko").setDisplay("Korean");
					break;
				case "*FIL":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("fil").setDisplay("Filipino");
					break;
				case "THAI":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("th").setDisplay("Thai");
					break;
				case "**SH":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("sh").setDisplay("Serbo-Croatian");
					break;
				case "FREN":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("fr").setDisplay("French");
					break;
				case "*FAR":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("far").setDisplay("Fataleka");
					break;
				case "*CRE":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("crp").setDisplay("Creoles and pidgins");
					break;
				case "HIND":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("hi").setDisplay("Hindi");
					break;
				case "*HUN":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("hu").setDisplay("Hungarian");
					break;
				case "ETHI":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("eth").setDisplay("Ethiopian Sign Language");
					break;
				case "VIET":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("vi").setDisplay("Vietnamese");
					break;
				case "*MAN":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("man").setDisplay("Mandingo");
					break;
				case "GERM":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("de").setDisplay("German");
					break;
				case "*PHI":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("phi").setDisplay("Philippine languages");
					break;
				case "TURK":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("tr").setDisplay("Turkish");
					break;
				case "*DEA":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("mjl").setDisplay("Mandeali");
					break;
				case "PTUN":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("ptu").setDisplay("Bambam");
					break;
				case "GREE":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("el").setDisplay("Modern Greek");
					break;
				case "MAND":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("cmn").setDisplay("Mandarin Chinese");
					break;
				case "HAIT":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("ht").setDisplay("Haitian");
					break;
				case "SERB":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("sr").setDisplay("Serbian");
					break;
				case "*BUL":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("bg").setDisplay("Bulgarian");
					break;
				case "*LEB":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("leb").setDisplay("Lala-Bisa");
					break;
				case "*GUJ":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("gu").setDisplay("Gujarati");
					break;
				case "PORT":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("pt").setDisplay("Portugese");
					break;
				case "* BE":
					lc.addCoding().setSystem("http://hl7.org/fhir/ValueSet/languages").setCode("be").setDisplay("Belarusian");
					break;
				default:
					lc.addCoding().setCode(firstAdm.getLanguage());
				}
				pMimic.addCommunication().setLanguage(lc);
			}
		}
		
		
		// Give the patient a temporary UUID so that other resources in
		// the transaction can refer to it
		pMimic.setId(IdDt.newRandomUuid());
		
		return pMimic;
	}

}
