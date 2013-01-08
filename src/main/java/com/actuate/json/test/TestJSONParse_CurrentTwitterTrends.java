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
public class TestJSONParse_CurrentTwitterTrends {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try{
			JSONParser theParser = new JSONParser();
			theParser.loadData("http://search.twitter.com/trends.json");
			
			System.out.println("Twitter Trends as of: " + theParser.getValue("as_of"));
			System.out.println("Number of Current Trends: " + theParser.getCount("trends") + "\n");
			
			for(int i = 0; i < theParser.getCount("trends"); i++){
				System.out.println(String.valueOf(i + 1) + ")  Trend: " + theParser.getValue("trends", "name", i));
				System.out.println("URL: " + theParser.getValue("trends", "url", i) + "\n");
				//System.out.println("---------------------------------------------------\n");
			}
		}catch (IOException ioe) {
			// TODO: handle exception
		}catch(Exception ex){
			
		}

	}

}
