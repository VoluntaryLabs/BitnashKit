package org.bitmarkets.bitnash;

public class BNString {
	public static String capitalized(String aString) {
		return aString.substring(0, 1).toUpperCase() + aString.substring(1);
	}
}
