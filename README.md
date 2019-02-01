# mimic2fhir 
## Introduction
mimic2fhir converts data from mimic-iii database (PostgreSQL) to HL7 FHIR resources.
To get access to MIMIC-III DB, you have to complete a privacy course, see https://mimic.physionet.org/gettingstarted/access/ for more information. 

## How to use
Bundles are created per patient admission/encounter. If the number of resources in a bundle exceeds 15000, a new bundle will be created. This limits the bundle size to ~20MB.
Resource bundles can be 
- printed to console
- saved as xml file
- pushed to a fhir server

by setting the "outputMode": 
```sh
app.setOutputMode(OutputMode.PRINT_FILE);
```
The parameter "topPatients" allows to limit the number of loaded patients; 0 means all patients. Transforming always starts with Patient 1.
```sh
app.setTopPatients(100);
```
A [RabbitMQ server](https://www.rabbitmq.com/) is required to run on localhost. 
Please note: Performance is highly dependent on the following and might be quite low:
- database partitioning and indexing for table chartevents (by HADM_ID)
- server performance (if pushed to a server)

We recommend starting with a low number of patients and with saving to xml files to check the database performance.   
 
### Example main method:
```sh
public static void main( String[] args )
    {   	
    	//Add server and config data..
    	Config configObj = new Config();
    	
    	//Postgres
    	configObj.setPassPostgres("postgres");
    	configObj.setPortPostgres("5432");
    	configObj.setUserPostgres("postgres");
    	configObj.setPostgresServer("localhost");
    	configObj.setDbnamePostgres("mimic");
    	configObj.setSchemaPostgres("mimiciii");
    	
    	//Fhir
    	configObj.setFhirServer("http://yourfhirserver.com/public/base/");
    	configObj.setFhirxmlFilePath("D:\\MimicOutput");
    	
    	Mimic2Fhir app = new Mimic2Fhir();
    	app.setConfig(configObj);
    	app.setOutputMode(OutputMode.PRINT_FILE);
    	app.setTopPatients(100);
    	app.start();

	}
```
## Citation
```
@conference {1101,
	title = {Konvertierung von MIMIC-III-Daten zu FHIR},
	booktitle = {GMDS},
	year = {2018},
	address = {Osnabr{\"u}ck},
	doi = {10.3205/18gmds018},
	url = {https://www.egms.de/static/en/meetings/gmds2018/18gmds018.shtml},
	author = {Stefanie Ververs and Ulrich, Hannes and Kock, Ann-Kristin and Ingenerf, Josef}
}
```

## License
This source code is licensed under Apache License 2.0.

