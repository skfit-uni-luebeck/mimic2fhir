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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import de.uzl.itcr.mimic2fhir.model.MAdmission;
import de.uzl.itcr.mimic2fhir.model.MCaregiver;
import de.uzl.itcr.mimic2fhir.model.MChartevent;
import de.uzl.itcr.mimic2fhir.model.MDiagnose;
import de.uzl.itcr.mimic2fhir.model.MLabevent;
import de.uzl.itcr.mimic2fhir.model.MNoteevent;
import de.uzl.itcr.mimic2fhir.model.MPatient;
import de.uzl.itcr.mimic2fhir.model.MPrescription;
import de.uzl.itcr.mimic2fhir.model.MProcedure;
import de.uzl.itcr.mimic2fhir.model.MTransfer;
import de.uzl.itcr.mimic2fhir.model.MWard;

/**
 * Connection, access and querys to postgresDB
 * @author Stefanie Ververs
 *
 */
public class ConnectDB {
	private Config configuration;	
	private Connection connection = null;
	
	/**
	 * Create new DB-Connection with Config-Object
	 * @param configuration
	 */
	public ConnectDB(Config configuration) {
		
		this.configuration = configuration;
		//Do some stuff to do DB-Connection..
		
		try {
			Class.forName("org.postgresql.Driver");

		} catch (ClassNotFoundException e) {

			e.printStackTrace();
			return;
		}

		this.connection = null;
		
		//Schema-Construction, if necessary:
		String schema = "";
		if(this.configuration.getSchemaPostgres() != null && this.configuration.getSchemaPostgres().length() > 0) {
			schema = "?currentSchema=" + this.configuration.getSchemaPostgres();
		}

		try {
			connection = DriverManager.getConnection(
			    	   "jdbc:postgresql://" + this.configuration.getPostgresServer() + ":" 
			    			   				+ this.configuration.getPortPostgres() + "/" 
			    			   				+ this.configuration.getDbnamePostgres() + schema,
			    			   				this.configuration.getUserPostgres(), 
			    			   				this.configuration.getPassPostgres());

		} catch (SQLException e) {
			e.printStackTrace();
			return;

		}
	}
	
	/**
	 * How many patients in MIMICIII.Patients?
	 * @return number of patients
	 */
	public int getNumberOfPatients() {
		String query = "SELECT COUNT(*) FROM PATIENTS";
		int count = 0;
		PreparedStatement statement;
		try {
			statement = connection.prepareStatement(query);
			ResultSet rs = statement.executeQuery();
			 while (rs.next()) {
					count = rs.getInt(1);
			 }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return count;
	}
	
	/**
	 * Get first patient in Mimic-Patients-Table
	 * @return filled MPatient-Object
	 */
	public MPatient getFirstPatient() {
		String query = "SELECT * FROM PATIENTS ORDER BY ROW_ID LIMIT 1";
		return getOnePatientFromDb(query);
	}

	/**
	 * Get patient by rowId
	 * @param rowId rowId of patient in patients-Table
	 * @return filled MPatient-Object
	 */
	public MPatient getPatientByRowId(int rowId) {
		String query = "SELECT * FROM PATIENTS WHERE ROW_ID = " + rowId;
		return getOnePatientFromDb(query);
	}
	
	private MPatient getOnePatientFromDb(String query) {
		
		PreparedStatement statement;
		try {
			statement = connection.prepareStatement(query);
			ResultSet rs = statement.executeQuery();
			
			if (rs.next()) {
					MPatient mPat = new MPatient();
					//SUBJECT_ID
					mPat.setPatientSubjectId(rs.getString(2));
					//DOB
					mPat.setBirthDate(rs.getDate(4));
					//GENDER
					mPat.setGender(rs.getString(3));
					//DOD
					mPat.setDeathDate(rs.getDate(5));
										
					//Admissions
					getPatientAdmissions(mPat);
					
					return mPat;
			 }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private void getPatientAdmissions(MPatient pat) {
		String query = "SELECT * FROM ADMISSIONS WHERE SUBJECT_ID = " + pat.getPatientSubjectId();
		PreparedStatement statement;
		try {
			statement = connection.prepareStatement(query);
			ResultSet rs = statement.executeQuery();
			
			while (rs.next()) {
					MAdmission mAdm = new MAdmission();
					mAdm.setAdmissionId(rs.getString(3));
					
					//Times
					mAdm.setAdmissionTime(rs.getDate(4));
					mAdm.setDischargeTime(rs.getDate(5));
					
					//Type
					mAdm.setAdmissionType(rs.getString(7));
					
					//DschLoc
					mAdm.setDischargeLocation(rs.getString(9));
					
					mAdm.setMaritalStatus(rs.getString(13));
					mAdm.setLanguage(rs.getString(11));
					mAdm.setReligion(rs.getString(12));
					mAdm.setAdmissionLocation(rs.getString(8));

					//Diagnoses
					getDiagnoses(pat.getPatientSubjectId(), mAdm);;
					
					//Procedures
					getProcedures(pat.getPatientSubjectId(), mAdm);
					
					//Chartevents
					getChartEvents(mAdm, pat.getPatientSubjectId());
					
					//Labevents
					getLabEvents(mAdm, pat.getPatientSubjectId());
					
					//Noteevents
					getNoteEvents(mAdm, pat.getPatientSubjectId());
										
					//Prescriptions
					getPrecriptions(mAdm, pat.getPatientSubjectId());
										
					//Transfers
					getTransfers(mAdm, pat.getPatientSubjectId());
					
					pat.addAdmission(mAdm);
			 }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void getChartEvents(MAdmission admission, String patientSubjId) {
		String query =  "SELECT C.SUBJECT_ID, C.HADM_ID, C.CHARTTIME, C.CGID, C.VALUE, C.VALUENUM, C.VALUEUOM, D.LABEL " +
						"FROM CHARTEVENTS C " +
					    "INNER JOIN D_ITEMS D ON C.ITEMID = D.ITEMID " + 
						"WHERE C.HADM_ID= " + admission.getAdmissionId();
		
		PreparedStatement statement;
		try {
			statement = connection.prepareStatement(query);
			ResultSet rs = statement.executeQuery();
			
			 while (rs.next()) { 
				 
				 //Value = null ausschließen -> kein Wert
				 if(rs.getObject(5) != null) {
				 
					 MChartevent event = new MChartevent();
					 
					 //Rekord-Datum
					 event.setRecordDate(rs.getDate(3));
					 
					 //CareGiver
					 event.setCareGiverId(rs.getInt(4));
					 
					 //Type (Item)
					 event.setMeasurementType(rs.getString(8));
					 
					 //Value + ValueNum
					 event.setValue(rs.getString(5));
					 if(rs.getObject(6) != null) {
						 event.setNumValue(rs.getDouble(6));
					 }
					 		 
					 //Unit
					 if(rs.getObject(7) != null) {
						 event.setUnit(rs.getString(7));
					 }
					 
					 admission.addEvent(event);
				 
				 }
			 }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void getLabEvents(MAdmission admission, String patientSubjId) {
		String query =  "SELECT L.SUBJECT_ID, L.HADM_ID, L.CHARTTIME, L.VALUE, L.VALUENUM, L.VALUEUOM, L.FLAG, D.LABEL, D.FLUID, D.LOINC_CODE " +
						"FROM LABEVENTS L " +
					    "INNER JOIN D_LABITEMS D ON L.ITEMID = D.ITEMID " + 
						"WHERE L.SUBJECT_ID = " + patientSubjId + " AND L.HADM_ID= " + admission.getAdmissionId();
		PreparedStatement statement;
		try {
			statement = connection.prepareStatement(query);
			ResultSet rs = statement.executeQuery();

			 while (rs.next()) { 
				 //Value = null ausschließen -> kein Wert
				 if(rs.getObject(4) != null) {
				 
					 MLabevent event = new MLabevent();
					 
					 //Rekord-Datum
					 event.setAcquisitionDate(rs.getDate(3));
					 				 
					 //Type (Item)
					 event.setMeasurementType(rs.getString(8));
					 
					 //Fluid 
					 event.setFluid(rs.getString(9));
					 
					 //Loinc-Code
					 if(rs.getObject(10) != null) {
						 event.setLoinc(rs.getString(10));
					 }
					 
					 //Value + ValueNum
					 event.setValue(rs.getString(4));
					 if(rs.getObject(5) != null) {
						 event.setNumValue(rs.getDouble(5));
					 }
					 		 
					 //Unit
					 if(rs.getObject(6) != null) {
						 event.setUnit(rs.getString(6));
					 }
					 
					 //Flag
					 //"delta" - might mean both, not considered
					 if(rs.getObject(7) != null && rs.getString(7) == "abnormal") {
						 event.setAbnormal(true);
					 }
					 
					 admission.addLabEvent(event);
				 
				 }
			 }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void getNoteEvents(MAdmission admission, String patientSubjId) {
		String query =  "SELECT * " +
						"FROM NOTEEVENTS " +
						"WHERE SUBJECT_ID = " + patientSubjId + " AND HADM_ID= " + admission.getAdmissionId();
		PreparedStatement statement;
		try {
			statement = connection.prepareStatement(query);
			ResultSet rs = statement.executeQuery();
			 while (rs.next()) { 
				 
				 boolean isError = rs.getString(10) == "1";
				 
				 MNoteevent event = new MNoteevent();
				 
				 event.setHasError(isError);

				 //Charttime (incl. date; 5) and Chartdate (4) - two columns..
				 if(rs.getObject(5) != null) {
					 event.setChartdate(rs.getDate(5));
				 }
				 else{
					 event.setChartdate(rs.getDate(4));
				 }

				 //might be null
				 event.setCaregiverId(rs.getInt(9));

				 event.setCategory(rs.getString(7));
				 event.setDescription(rs.getString(8));

				 event.setText(rs.getString(11));

				 admission.addNoteEvent(event);
				 
			 }
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void getDiagnoses(String patId, MAdmission adm) {
		String query = "SELECT *" + 
					"	FROM diagnoses_icd d" + 
					"   INNER JOIN d_icd_diagnoses i ON d.icd9_code = i.icd9_code" + 
					"   WHERE d.subject_id = " + patId + "AND d.hadm_id = " + adm.getAdmissionId() + 
					"   ORDER BY d.seq_num";
		PreparedStatement statement;
		try {
			statement = connection.prepareStatement(query);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
					MDiagnose mDiag = new MDiagnose();
					mDiag.setIcd9Code(rs.getString(5));
					mDiag.setShortTitle(rs.getString(8));
					mDiag.setLongTitle(rs.getString(9));
					mDiag.setSeqNumber(rs.getInt(4));

					adm.addDiagnose(mDiag);
			 }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void getProcedures(String patId, MAdmission adm) {
		String query = "SELECT *" + 
					"	FROM procedures_icd p" + 
					"   INNER JOIN d_icd_procedures i ON p.icd9_code = i.icd9_code" + 
					"   WHERE p.subject_id = " + patId + "AND p.hadm_id = " + adm.getAdmissionId() + 
					"   ORDER BY p.seq_num";
		PreparedStatement statement;
		try {
			statement = connection.prepareStatement(query);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
					MProcedure mProc = new MProcedure();
					mProc.setIcd9Code(rs.getString(5));
					mProc.setShortTitle(rs.getString(8));
					mProc.setLongTitle(rs.getString(9));
					mProc.setSeqNumber(rs.getInt(4));

					adm.addProcedure(mProc);
			 }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Get dictionary with all caregivers - Key: Id, Value: Caregiver-Object
	 * @return dictionary
	 */
	public HashMap<Integer,MCaregiver> getCaregivers()
	{
		String query = "SELECT * FROM caregivers";
		HashMap<Integer,MCaregiver> caregivers = new HashMap<Integer,MCaregiver>();
		
		PreparedStatement statement;
		try {
			statement = connection.prepareStatement(query);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
					MCaregiver cg = new MCaregiver();
					cg.setCaregiverId(rs.getInt(2));
					cg.setLabel(rs.getString(3));
					cg.setDescription(rs.getString(4));
	
					caregivers.put(cg.getCaregiverId(),cg);
			 }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return caregivers;
	}
	
	private void getPrecriptions(MAdmission admission, String patientSubjId) {
		String query =  "SELECT * " +
						"FROM PRESCRIPTIONS " +
						"WHERE SUBJECT_ID = " + patientSubjId + " AND HADM_ID= " + admission.getAdmissionId();
		PreparedStatement statement;
		try {
			statement = connection.prepareStatement(query);
			ResultSet rs = statement.executeQuery();
			 while (rs.next()) { 			 
				 MPrescription pres = new MPrescription();
				 
				 pres.setStart(rs.getDate(5));
				 pres.setEnd(rs.getDate(6));
				 
				 pres.setDrugtype(rs.getString(7));
				 pres.setDrug(rs.getString(8));
				 pres.setDrugNamePoe(rs.getString(9));
				 pres.setDrugNameGeneric(rs.getString(10));
				 
				 pres.setFormularyDrugCd(rs.getString(11));
				 pres.setGsn(rs.getString(12));
				 pres.setNdc(rs.getString(13));
				 
				 pres.setProdStrength(rs.getString(14));
				 pres.setDoseValRx(rs.getString(15));
				 pres.setDoseUnitRx(rs.getString(16));
				 
				 pres.setFormValDisp(rs.getString(17));
				 pres.setFormUnitDisp(rs.getString(18));
				 
				 pres.setRoute(rs.getString(19));
				 
				 admission.addPrescription(pres);

			 }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void getTransfers(MAdmission admission, String patientSubjId) {
		String query =  "SELECT * " +
						"FROM TRANSFERS " +
						"WHERE SUBJECT_ID = " + patientSubjId + " AND HADM_ID= " + admission.getAdmissionId();
		PreparedStatement statement;
		try {
			statement = connection.prepareStatement(query);
			ResultSet rs = statement.executeQuery();
			int index = 0;
			 while (rs.next()) {
				 index++;
				 MTransfer t = new MTransfer();
				 
				 t.setTransferId(rs.getInt(3) + "-" + index);
				 
				 t.setEventType(rs.getString(6));
				 
				 t.setPrevUnit(rs.getString(7));
				 t.setCurrUnit(rs.getString(8));
				 
				 t.setPrevWard(rs.getInt(9));
				 t.setCurrWard(rs.getInt(10));
				 
				 t.setIntime(rs.getDate(11));
				 t.setOuttime(rs.getDate(12));
				 
				 t.setLengthOfStay(rs.getDouble(13));
				
				 admission.addTransfer(t);

			 }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Get dictionary with all locations = wards, key = wardId, value: MWard-Object
	 * @return dictionary
	 */
	public HashMap<Integer, MWard> getLocations() {
		String query = "SELECT DISTINCT curr_wardid, curr_careunit FROM transfers";
		HashMap<Integer,MWard> wards = new HashMap<Integer,MWard>();
		
		PreparedStatement statement;
		try {
			statement = connection.prepareStatement(query);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
					MWard ward = new MWard();
					ward.setWardId(rs.getInt(1));
					ward.setCareUnit(rs.getString(2));
	
					wards.put(ward.getWardId(),ward);
			 }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return wards;
	}
}
