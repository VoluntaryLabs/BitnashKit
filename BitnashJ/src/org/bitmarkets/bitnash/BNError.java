package org.bitmarkets.bitnash;

import java.util.Arrays;

public class BNError extends BNObject {
	Number insufficientValue;
	String description;
	
	public BNError() {
		super();
		bnSlotNames.addAll(Arrays.asList("insufficientValue", "description"));
	}
	
	public Number getInsufficientValue() {
		return insufficientValue;
	}
	
	public void setInsufficientValue(Number insufficientValue) {
		this.insufficientValue = insufficientValue;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
}
