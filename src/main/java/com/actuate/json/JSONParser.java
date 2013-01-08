/**
 * 
 */
package com.actuate.json;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Bhanley
 *
 * Class designed to be accessed via a BIRT scripted data source.  Calling 
 * "loadData(...)" with a path to a JSON source will allow the data to be
 * accessed in the Fetch method of the data set inside the report itself.
 * To access data elements after the call to "loadData(...)" the fetch
 * script can call "getData(...)" for each element of the array.  The potential 
 * exists for the primary array to contain a secondary array of name-value pairs.
 * A call to "getCount(...)" will determine if the key points to a primitive value 
 * or an array requiring iteration.
 * 
 * Lastly, all values are returned as String representations.  The can be converted
 * to specific data formats on the report's embedded Data Set.
 * 
 */
public class JSONParser {
	private JSONObject sourceJSON = null;
	
	/**
	 * Loads the JSON source into the class instance.  Must be called by the DATA SOURCE
	 * onLoad method within the BIRT report.
	 * 
	 * @param fullJSONDataPath path to the JSON data source
	 * @throws IOException
	 */
	public void loadData(String fullJSONDataPath) throws IOException{
		try{
			String sJSONText = getJSONText(fullJSONDataPath);
			sourceJSON = new JSONObject(sJSONText);
		}catch(JSONException je){
			
		}catch(Exception ex){
			
		}			
	}
	
	/**
	 * Returns 1 of the key points to a primitive value, and a number greater than one 
	 * (the value will equal the count of the array) should they key point to a secondary
	 * array from the JSON source.  This count will also equate to the count of rows on 
	 * the resultant data set.  The value for "sKey" can implement XPath-style syntax
	 * (i.e. node1/node2/node3) to allow drilling to a nested array deep within the JSON object store	
	 * 
	 * @param sKey on the root of the JSON context
	 * @return
	 */
	public int getCount(String sKey) {
		try{
			if(sourceJSON != null){
				Object value = getJSONObj(sKey, sourceJSON);
				if(value != null){
					if(value instanceof JSONArray) return ((JSONArray) value).length();
					else return 1;
				}
				else
					return -1;
			}
			else
				return -1;
		}catch(Exception ex){
			
		}
		return -1;
	}
	
	/**
	 * Determines the key for nested Array access.  Useful for determining
	 * dynamically build key values (like dates).  The value for "sParent" can implement XPath-style syntax
	 * (i.e. node1/node2/node3) to allow drilling to a nested array deep within the JSON object store
	 * 
	 * @param sParent
	 * @param iPos
	 * @return
	 */
	public String getNameAt(String sParent, int iPos) {
		try{
			if(sourceJSON != null){
				Object value = getJSONObj(sParent, sourceJSON);
				if(value != null){
					if(value instanceof JSONObject){
						JSONObject obj = (JSONObject)value;
						return (String)obj.names().get(iPos);
					}
					else{
						return "";
					}
				}
				else
					return "";
			}
			else
				return "";
		}catch(JSONException je){
			
		}catch(Exception ex){
			
		}
		return "";
		
	}
	
	/**
	 * Gets the number of JSON objects under a given key.  The value for "sKey" can implement XPath-style syntax
	 * (i.e. node1/node2/node3) to allow drilling to a nested array deep within the JSON object store	
	 * 
	 * @param sKey
	 * @return
	 */
	public int getChildCount(String sKey){
		try{
			if(sourceJSON != null){
				Object value = getJSONObj(sKey, sourceJSON);
				if(value != null){
					if(value instanceof JSONArray) return ((JSONArray) value).length();
					else if(value instanceof JSONObject){
						JSONObject obj = (JSONObject)value;
						return obj.names().length();
					}
					else{
						return 0;
					}
				}
				else
					return 0;
			}
			else
				return 0;
		}catch(Exception ex){
			
		}
		return 0;
	}
	
	/**
	 * Gets a primitive value from the JSON source.  The value for "sKey" can implement XPath-style syntax
	 * (i.e. node1/node2/node3) to allow drilling to a nested array deep within the JSON object store.
	 * 
	 * @param sKey
	 * @return
	 * @throws IllegalArgumentException if the key points to an array rather than
	 * a primitive value.
	 */
	public String getValue(String sKey) throws IllegalArgumentException {
		try{
			if(sourceJSON != null){
				Object value = getJSONObj(sKey, sourceJSON);
				if(value != null){
					if(value instanceof JSONArray) throw new IllegalArgumentException("Key points to an array, targeted value is ambiguous.");
					else{
						return value.toString();
					}
				}
				else
					return "";
			}
			else
				return "";
		}catch(Exception ex){
			
		}
		return "";
	}
	
	/**
	 * Gets a primitive value from a nested array on the JSON source.  This array should represent
	 * a set of "rows" on your Data Set.  The value for "sParent" can implement XPath-style syntax
	 * (i.e. node1/node2/node3) to allow drilling to a nested array deep within the JSON object store.
	 * 
	 * @param sParent is the value to use to get the array from the overall JSON context.
	 * @param sKey to retrieve the value for
	 * @param position in the array
	 * @return
	 * @throws IllegalArgumentException
	 */
	public String getValue(String sParent, String sKey, int position) throws IllegalArgumentException {
		try{
			if(sourceJSON != null){
				Object value = getJSONObj(sParent, sourceJSON);
				if(value != null){
					if(value instanceof JSONArray){
						JSONArray array = (JSONArray)value;
						JSONObject arrayValue = (JSONObject) array.get(position);
						if(sKey.contains("/")){
							return (String)getJSONObj(sKey, arrayValue);
						}
						else return arrayValue.getString(sKey);
					}
					else{
						return value.toString();
					}
				}
				else
					return "";
			}
			else
				return "";
		}catch(JSONException je){
			
		}catch(Exception ex){
			
		}
		return "";
	}
	
	private Object getJSONObj(String sPath, JSONObject currentObj){
		
		try{
			if(sPath.contains("/")){
				return getJSONObj((sPath.substring(sPath.indexOf('/') + 1)), (JSONObject)currentObj.get(sPath.substring(0, sPath.indexOf('/'))));
			}
			else return currentObj.get(sPath);
		}catch(JSONException je){
			
		}catch(Exception ex){
			System.out.println(ex.getMessage());
		}
		return null;
	}
	
	private String getJSONText(String sPathToJSON) throws FileNotFoundException {

		try {
			DefaultHttpClient httpclient = new DefaultHttpClient();
	        HttpGet httpget = new HttpGet(sPathToJSON);
	        ResponseHandler responseHandler = new BasicResponseHandler();    
	        return (String)httpclient.execute(httpget, responseHandler);        
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";	
	}
	
	
}
