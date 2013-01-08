/**
 * 
 */
package com.actuate.json.test;

import java.io.IOException;

import com.actuate.json.JSONParser;

/**
 * @author Bhanley
 *
 */
public class TestJSONParse_BIRTJobs {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try{
			JSONParser theParser = new JSONParser();
			theParser.loadData("http://pipes.yahoo.com/pipes/pipe.run?_id=197eaecdb0cf640780e5c4da773f15a2&_render=json");
			
			System.out.println("Number of jobs found: " + theParser.getChildCount("value/items") + "\n");
			
			for(int i = 0; i < theParser.getChildCount("value/items"); i++){
				System.out.println(String.valueOf(i + 1) + ")  " + theParser.getValue("value/items", "description", i));
				System.out.println("URL: " + theParser.getValue("value/items", "link", i));
				System.out.println("Job Function: " + theParser.getValue("value/items", "g:job_function", i));
				System.out.println("Job Type: " + theParser.getValue("value/items", "g:job_type", i));
				System.out.println("Location: ");
				System.out.println("\t" +  theParser.getValue("value/items", "g:location/city", i) + 
						", " + theParser.getValue("value/items", "g:location/state", i) + 
						", " + theParser.getValue("value/items", "g:location/postal", i) +
						"  " + theParser.getValue("value/items", "g:location/country", i) + "\n");
			}
		}catch (IOException ioe) {
			// TODO: handle exception
		}catch(Exception ex){
			
		}
	}
}
