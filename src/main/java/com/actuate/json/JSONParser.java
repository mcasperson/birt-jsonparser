/**
 * 
 */
package com.actuate.json;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Bhanley
 * 
 *         Class designed to be accessed via a BIRT scripted data source.
 *         Calling "loadData(...)" with a path to a JSON source will allow the
 *         data to be accessed in the Fetch method of the data set inside the
 *         report itself. To access data elements after the call to
 *         "loadData(...)" the fetch script can call "getData(...)" for each
 *         element of the array. The potential exists for the primary array to
 *         contain a secondary array of name-value pairs. A call to
 *         "getCount(...)" will determine if the key points to a primitive value
 *         or an array requiring iteration.
 * 
 *         Lastly, all values are returned as String representations. The can be
 *         converted to specific data formats on the report's embedded Data Set.
 * 
 */
public class JSONParser {
	private JSONObject sourceJSON = null;
	
	public void loadData(final String fullJSONDataPath) throws IOException
	{
		loadData(fullJSONDataPath, null);
	}

	/**
	 * Loads the JSON source into the class instance. Must be called by the DATA
	 * SOURCE onLoad method within the BIRT report.
	 * 
	 * @param fullJSONDataPath
	 *            path to the JSON data source
	 * @throws IOException
	 */
	public void loadData(final String fullJSONDataPath, final Map<String, String> headers) throws IOException {
		try {
			final String sJSONText = getJSONText(fullJSONDataPath, headers);
			sourceJSON = new JSONObject(sJSONText);
		} catch (final JSONException je) {

		} catch (final Exception ex) {

		}
	}

	/**
	 * Returns 1 of the key points to a primitive value, and a number greater
	 * than one (the value will equal the count of the array) should they key
	 * point to a secondary array from the JSON source. This count will also
	 * equate to the count of rows on the resultant data set. The value for
	 * "sKey" can implement XPath-style syntax (i.e. node1/node2/node3) to allow
	 * drilling to a nested array deep within the JSON object store
	 * 
	 * @param sKey
	 *            on the root of the JSON context
	 * @return
	 */
	public int getCount(final String sKey) {
		try {
			if (sourceJSON != null) {
				final Object value = getJSONObj(sKey, sourceJSON);
				if (value != null) {
					if (value instanceof JSONArray)
						return ((JSONArray) value).length();
					else
						return 1;
				} else
					return -1;
			} else
				return -1;
		} catch (final Exception ex) {

		}
		return -1;
	}

	/**
	 * Determines the key for nested Array access. Useful for determining
	 * dynamically build key values (like dates). The value for "sParent" can
	 * implement XPath-style syntax (i.e. node1/node2/node3) to allow drilling
	 * to a nested array deep within the JSON object store
	 * 
	 * @param sParent
	 * @param iPos
	 * @return
	 */
	public String getNameAt(final String sParent, final int iPos) {
		try {
			if (sourceJSON != null) {
				final Object value = getJSONObj(sParent, sourceJSON);
				if (value != null) {
					if (value instanceof JSONObject) {
						final JSONObject obj = (JSONObject) value;
						return (String) obj.names().get(iPos);
					} else {
						return "";
					}
				} else
					return "";
			} else
				return "";
		} catch (final JSONException je) {

		} catch (final Exception ex) {

		}
		return "";

	}

	/**
	 * Gets the number of JSON objects under a given key. The value for "sKey"
	 * can implement XPath-style syntax (i.e. node1/node2/node3) to allow
	 * drilling to a nested array deep within the JSON object store
	 * 
	 * @param sKey
	 * @return
	 */
	public int getChildCount(final String sKey) {
		try {
			if (sourceJSON != null) {
				final Object value = getJSONObj(sKey, sourceJSON);
				if (value != null) {
					if (value instanceof JSONArray)
						return ((JSONArray) value).length();
					else if (value instanceof JSONObject) {
						JSONObject obj = (JSONObject) value;
						return obj.names().length();
					} else {
						return 0;
					}
				} else
					return 0;
			} else
				return 0;
		} catch (final Exception ex) {

		}
		return 0;
	}

	/**
	 * Gets a primitive value from the JSON source. The value for "sKey" can
	 * implement XPath-style syntax (i.e. node1/node2/node3) to allow drilling
	 * to a nested array deep within the JSON object store.
	 * 
	 * @param sKey
	 * @return
	 * @throws IllegalArgumentException
	 *             if the key points to an array rather than a primitive value.
	 */
	public String getValue(final String sKey) throws IllegalArgumentException {
		try {
			if (sourceJSON != null) {
				final Object value = getJSONObj(sKey, sourceJSON);
				if (value != null) {
					if (value instanceof JSONArray)
						throw new IllegalArgumentException(
								"Key points to an array, targeted value is ambiguous.");
					else {
						return value.toString();
					}
				} else
					return "";
			} else
				return "";
		} catch (final Exception ex) {

		}
		return "";
	}

	/**
	 * Gets a primitive value from a nested array on the JSON source. This array
	 * should represent a set of "rows" on your Data Set. The value for
	 * "sParent" can implement XPath-style syntax (i.e. node1/node2/node3) to
	 * allow drilling to a nested array deep within the JSON object store.
	 * 
	 * @param sParent
	 *            is the value to use to get the array from the overall JSON
	 *            context.
	 * @param sKey
	 *            to retrieve the value for
	 * @param position
	 *            in the array
	 * @return
	 * @throws IllegalArgumentException
	 */
	public String getValue(final String sParent, final String sKey,
			final int position) throws IllegalArgumentException {
		try {
			if (sourceJSON != null) {
				final Object value = getJSONObj(sParent, sourceJSON);
				if (value != null) {
					if (value instanceof JSONArray) {
						final JSONArray array = (JSONArray) value;
						final JSONObject arrayValue = (JSONObject) array
								.get(position);
						if (sKey.contains("/")) {
							return getJSONObj(sKey, arrayValue).toString();
						} else
							return arrayValue.getString(sKey);
					} else {
						return value.toString();
					}
				} else
					return "";
			} else
				return "";
		} catch (final JSONException je) {

		} catch (final Exception ex) {

		}
		return "";
	}

	/**
	 * Gets a primitive value from multiple nested arrays on the JSON source.
	 * 
	 * @param arrayIndexes
	 *            A collection of mappings that identify the child array and the
	 *            array index. All items in this array, except for the last one,
	 *            need to reference a nested array. The last item in this
	 *            collection can reference a value.
	 * @param sKey
	 *            to retrieve the value for
	 * @return
	 * @throws IllegalArgumentException
	 */
	public String getValue(final JSONArrayIndex[] arrayIndexes,
			final String sKey) throws IllegalArgumentException {
		try {
			if (sourceJSON != null && arrayIndexes != null
					&& arrayIndexes.length != 0) {

				JSONObject lastArray = sourceJSON;

				for (int i = 0; i < arrayIndexes.length - 1; ++i) {
					final Object thisArray = getJSONObj(
							arrayIndexes[i].getKey(), lastArray);
					/*
					 * Every object returned up until the last one needs to be
					 * an array. If it isn't, there is a problem
					 */
					if (thisArray instanceof JSONArray) {
						final JSONArray thisArrayCasted = (JSONArray)thisArray;
						lastArray = (JSONObject)thisArrayCasted.get(arrayIndexes[i].getIndex());
					} else {
						return "";
					}
				}

				final Object value = getJSONObj(
						arrayIndexes[arrayIndexes.length - 1].getKey(),
						lastArray);
				if (value != null) {
					if (value instanceof JSONArray) {
						final JSONArray array = (JSONArray) value;
						final JSONObject arrayValue = (JSONObject) array
								.get(arrayIndexes[arrayIndexes.length - 1]
										.getIndex());
						if (sKey.contains("/")) {
							return getJSONObj(sKey, arrayValue).toString();
						} else
							return arrayValue.getString(sKey);
					} else {
						return value.toString();
					}
				} else
					return "";
			} else
				return "";
		} catch (final JSONException je) {

		} catch (final Exception ex) {

		}
		return "";
	}

	private Object getJSONObj(final String sPath, final JSONObject currentObj) {

		try {
			if (sPath.contains("/")) {
				return getJSONObj(
						(sPath.substring(sPath.indexOf('/') + 1)),
						(JSONObject) currentObj.get(sPath.substring(0,
								sPath.indexOf('/'))));
			} else
				return currentObj.get(sPath);
		} catch (final JSONException je) {

		} catch (final Exception ex) {
			System.out.println(ex.getMessage());
		}
		return null;
	}

	private String getJSONText(final String sPathToJSON, final Map<String, String> headers)
			throws FileNotFoundException {	

		try {
			final DefaultHttpClient httpclient = new DefaultHttpClient();
			
			httpclient.addRequestInterceptor(new HttpRequestInterceptor() {

                public void process(
                        final HttpRequest request,
                        final HttpContext context) throws HttpException, IOException {
                    if (!request.containsHeader("Accept-Encoding")) {
                        request.addHeader("Accept-Encoding", "gzip");
                    }
                    
                    if (headers != null)
                    {
                    	for (final String header : headers.keySet())
                    	{
                    		request.addHeader(header, headers.get(header));
                    	}
                    }
                }

            });

            httpclient.addResponseInterceptor(new HttpResponseInterceptor() {

                public void process(
                        final HttpResponse response,
                        final HttpContext context) throws HttpException, IOException {
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        Header ceheader = entity.getContentEncoding();
                        if (ceheader != null) {
                            HeaderElement[] codecs = ceheader.getElements();
                            for (int i = 0; i < codecs.length; i++) {
                                if (codecs[i].getName().equalsIgnoreCase("gzip")) {
                                    response.setEntity(
                                            new GzipDecompressingEntity(response.getEntity()));
                                    return;
                                }
                            }
                        }
                    }
                }

            });
			
			final HttpGet httpget = new HttpGet(sPathToJSON);
			final ResponseHandler<String> responseHandler = new BasicResponseHandler();
			return httpclient.execute(httpget, responseHandler);
		} catch (final ClientProtocolException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return "";
	}

}
