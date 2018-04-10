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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * Configuration for the transformation
 * Contains Server, DB and other access information for mimiciii-postgres-db as well as fhir server
 * @author Stefanie Ververs
 *
 */
public class Config {
	//private: FHIR-options
	private String fhirServer;
	private String fhirxmlFilePath;
	
	//private: FHIR-Server-Auth
	private String ldapUser;
	private String ldapPwd;
	private String token;
	private String authServer;
	
	private boolean authRequired;
	
	//private: POSTGRES-options
	private String postgresServer;
	private String userPostgres;
	private String passPostgres;
	private String portPostgres;
	private String schemaPostgres;
	private String dbnamePostgres;
	
	/**
	 * Path for FHIR-Server
	 * @return FHIRServer-Path
	 */
	public String getFhirServer() {
		return fhirServer;
	}
	
	/**
	 * Set FHIR-Server
	 * @param fhirServer FHIR-Server to be used
	 */
	public void setFhirServer(String fhirServer) {
		this.fhirServer = fhirServer;
	}
	
	/**
	 * Get LdapUser for Token-Request
	 * @return LdapUser
	 */
	public String getLdapUser() {
		return ldapUser;
	}

	/**
	 * Set LdapUser for Token-Request
	 * @param ldapUser
	 */
	public void setLdapUser(String ldapUser) {
		this.ldapUser = ldapUser;
	}

	/**
	 * Get LdapPassword for Token-Request
	 * @return LdapPassword
	 */
	public String getLdapPwd() {
		return ldapPwd;
	}

	/**
	 * Set LdapPassword for Token-Request
	 * @param ldapPwd
	 */
	public void setLdapPwd(String ldapPwd) {
		this.ldapPwd = ldapPwd;
	}

	/**
	 * Get Bearer-Token for Auth for this config
	 * @return Bearer-Token
	 */
	public String getToken() {
		if(token == null || token.length() == 0)
		{
			token = getTokenFromAuthServer();
		}
		return token;
	}

	/**
	 * Set BearerToken for Auth, if received otherwise
	 * @param token
	 */
	public void setToken(String token) {
		this.token = token;
	}

	/**
	 * Get ServerPath for Token-Request
	 * @return serverPath
	 */
	public String getAuthServer() {
		return authServer;
	}

	/**
	 * Set ServerPath for Token-Request
	 * @param authServer
	 */
	public void setAuthServer(String authServer) {
		this.authServer = authServer;
	}

	/**
	 * Does the Fhir-Server require authentication?
	 * @return
	 */
	public boolean isAuthRequired() {
		return authRequired;
	}

	/**
	 * Set if the Fhir-Server requires authentication
	 * @param authRequired
	 */
	public void setAuthRequired(boolean authRequired) {
		this.authRequired = authRequired;
	}

	/**
	 * Get Postgres-DB-Server (e.g. localhost)
	 * @return Postgres-DB-Server
	 */
	public String getPostgresServer() {
		return postgresServer;
	}
	
	/**
	 * Set Postgres-DB-Server (e.g. localhost)
	 * @param postgresServer Server where postgres is installed
	 */
	public void setPostgresServer(String postgresServer) {
		this.postgresServer = postgresServer;
	}
	
	/**
	 * Postgres-DB-User (typically postgres)
	 * @return Postgres-DB-User
	 */
	public String getUserPostgres() {
		return userPostgres;
	}
	
	/**
	 * Set Postgres-DB-User (typically postgres)
	 * @param userPostgres Postgres-DB-user
	 */
	public void setUserPostgres(String userPostgres) {
		this.userPostgres = userPostgres;
	}
	
	/**
	 * Postgres-DB-password for user
	 * @return Postgres-DB-password for user
	 */
	public String getPassPostgres() {
		return passPostgres;
	}
	
	/**
	 * Set Postgres-DB-password for user
	 * @param passPostgres Postgres-DB-password for user
	 */
	public void setPassPostgres(String passPostgres) {
		this.passPostgres = passPostgres;
	}
	
	/**
	 * Postgres-DB Port
	 * @return Postgres-DB Port
	 */
	public String getPortPostgres() {
		return portPostgres;
	}
	
	/**
	 * Set Postgres-DB Port
	 * @param portPostgres Postgres-DB Port 
	 */
	public void setPortPostgres(String portPostgres) {
		this.portPostgres = portPostgres;
	}
	
	/**
	 * Path (only folder) where FHIR-Bundle-XML is saved
	 * Name will be "bundle#.xml"
	 * @return Path
	 */
	public String getFhirxmlFilePath() {
		return fhirxmlFilePath;
	}
	
	/**
	 * Set Path (only folder) where FHIR-Bundle-XML should be saved
	 * @param fhirxmlFilePath the path (only folder)
	 */
	public void setFhirxmlFilePath(String fhirxmlFilePath) {
		this.fhirxmlFilePath = fhirxmlFilePath;
	}
	
	/**
	 * Get Mimic-Database-Name (e.g. mimic)
	 * @return DB-Name
	 */
	public String getDbnamePostgres() {
		return dbnamePostgres;
	}
	
	/**
	 * Set Mimic-Database-Name (e.g. mimic)
	 * @param dbnamePostgres Mimic-DB-Name
	 */
	public void setDbnamePostgres(String dbnamePostgres) {
		this.dbnamePostgres = dbnamePostgres;
	}
	
	/**
	 * Get Mimic-Database-Schema-Name (e.g. mimiciii)
	 * @return DB-Schema
	 */
	public String getSchemaPostgres() {
		return schemaPostgres;
	}
	
	/**
	 * Set Mimic-Database-Schema-Name (e.g. mimiciii) (if used)
	 * @param schemaPostgres DB-Schema
	 */
	public void setSchemaPostgres(String schemaPostgres) {
		this.schemaPostgres = schemaPostgres;
	}
	
	
	private String getTokenFromAuthServer() {
		String token = "";
		
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(this.authServer);
		CloseableHttpResponse response = null;
		try {
			//Datagram
			StringEntity input = new StringEntity("{\"user\":\"" + this.ldapUser + "\",\"password\":\"" + this.ldapPwd + "\"}");

			input.setContentType("application/json");
			httpPost.setEntity(input);
			
			//POST 
			response = httpclient.execute(httpPost);
		    	    
			//Response -> JSON Object -> get Token
			HttpEntity entity2 = response.getEntity();
			String jsonResponse = EntityUtils.toString(entity2);
			InputStream is = new ByteArrayInputStream( jsonResponse.getBytes() );
			
			JsonReader jsonReader = Json.createReader(is);
			JsonObject tokenObject = jsonReader.readObject();
			jsonReader.close();
			
			//Token in form "Bearer <encodedTokenStuff>" -> we need only second part
			token = tokenObject.getJsonObject("data").getString("token").split(" ")[1];
			
		    EntityUtils.consume(entity2);
		}
		catch(Exception ex){
			System.out.println(ex.getMessage());
		}
		finally {
			if(response != null) {
				try {
					response.close();
				}
				catch(Exception ex)
				{}
			}
		}
		return token;
	}
}
