package com.actuate.json.test;

import java.io.IOException;

import com.actuate.json.JSONParser;

public class TestJSONParse_DailyTwitterTrends {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try{
			JSONParser theParser = new JSONParser();
			theParser.loadData("http://search.twitter.com/trends/daily.json");
			
			System.out.println("Twitter Trends as of: " + theParser.getValue("as_of"));
			System.out.println("Number of Trend Groups: " + theParser.getChildCount("trends") + "\n");
			
			for(int i = 0; i < theParser.getChildCount("trends"); i++){
				String sGroup = theParser.getNameAt("trends", i);
				System.out.println(String.valueOf(i + 1) + ")  Group: " + sGroup);
				
				for(int y = 0; y < theParser.getCount("trends/".concat(sGroup)); y++){
					System.out.println("\tName: " + theParser.getValue("trends/".concat(sGroup), "name", y));
					System.out.println("\tQuery: " + theParser.getValue("trends/".concat(sGroup), "query", y) + "\n");
				}
				
				System.out.println("---------------------------------------------------\n");
			}
		}catch (IOException ioe) {
			// TODO: handle exception
		}catch(Exception ex){
			
		}

	}

}
