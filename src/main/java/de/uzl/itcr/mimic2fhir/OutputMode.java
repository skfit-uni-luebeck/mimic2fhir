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
package de.uzl.itcr.mimic2fhir;

/**
 * Output-Mode: Where shall the data go?
 * -CONSOLE: Print to console
 * -FILE: Print to xml-Files
 * -BOTH: Console and file
 * -SERVER: Push to a Fhir server
 * @author Stefanie Ververs
 *
 */
public enum OutputMode {
	PRINT_CONSOLE,
	PRINT_FILE,
	PRINT_BOTH,
	PUSH_SERVER
}
