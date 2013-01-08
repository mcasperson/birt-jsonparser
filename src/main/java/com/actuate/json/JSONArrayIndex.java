package com.actuate.json;

/**
 * A data structure used to identify an item in a JSON array
 * @author Matthew Casperson
 */
final public class JSONArrayIndex {
	/**
	 * The index in the array identified by the key
	 */
	private final int index;
	/**
	 * The key that identifies the array
	 */
	private final String key;
	
	/**
	 * @param index The index in the array identified by the key
	 * @param key The key that identifies the array
	 */
	public JSONArrayIndex(final int index, final String key)
	{
		this.index = index;
		this.key = key;
	}
	
	/**
	 * @return The index in the array identified by the key
	 */
	final public int getIndex() {return index;}
	
	/**
	 * @return The key that identifies the array
	 */
	final public String getKey() {return key;}
}
