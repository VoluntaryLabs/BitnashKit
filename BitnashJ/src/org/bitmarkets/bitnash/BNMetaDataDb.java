package org.bitmarkets.bitnash;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Scanner;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.common.io.Files;

public class BNMetaDataDb {
	static BNMetaDataDb shared; 
	
	public static BNMetaDataDb shared() {
		if (shared == null) {
			shared = new BNMetaDataDb();
		}
		
		return shared;
	}
	
	String path;
	
	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		File dir = new File(path);
		
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				throw new RuntimeException("Unable to create BNMetaDataDb at path: " + path);
			}
		}
		
		this.path = path;
	}
	
	public void readToBnObject(BNObject bnObject) {
		File metaDataFile = metaDataFileFor(bnObject);
		
		if (metaDataFile.exists()) {
			try {
				System.err.println("READ: " + metaDataFile.getPath());
				
				String contents = new Scanner(metaDataFile, "UTF-8").useDelimiter("\\Z").next();
				JSONParser parser = new JSONParser();
				bnObject.setMetaData((JSONObject)parser.parse(contents));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public void writeFromBnObject(BNObject bnObject) {
		JSONObject metaData = bnObject.getMetaData();
		
		if (metaData.size() > 0) {
			try {
				File metaDataFile = metaDataFileFor(bnObject);
				
System.err.println("WRITE: " + metaDataFile.getPath());
				
				Files.write(bnObject.getMetaData().toJSONString().getBytes(Charset.forName("UTF-8")), metaDataFile);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		else
		{
			File metaDataFile = metaDataFileFor(bnObject);
			
			if (metaDataFile.exists()) {
				System.err.println("DELETE: " + metaDataFile.getPath());
				
				metaDataFile.delete();
			}
		}
	}
	
	File metaDataFileFor(BNObject bnObject) {
		return new File(path + "/" + bnObject.getClass().getSimpleName() + "." + bnObject.id()); 
	}
	
}
