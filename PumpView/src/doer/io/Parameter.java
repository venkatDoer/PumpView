package doer.io;

import java.util.ArrayList;

public class Parameter {
	// class to perform operations on parameter settings table
	private String paramName = "";
	private String devName = "";
	private Integer paramAdr = 0;
	private String convFactor = "";
	private String formatText = "";
	private String paramRegType = "";
	private ArrayList<String> paramValue = new ArrayList<String>(); // captured from device / calculated / manually entered 
	
	public Parameter() {
		
	}
	public Parameter(String paramName, String devName, Integer paramAdr, String convFactor, String formatText, String paramRegType) {
		this.setParamName(paramName);
		this.setDevName(devName);
		this.setParamAdr(paramAdr);
		this.setConvFactor(convFactor);
		this.setFormatText(formatText);
		this.setParamRegType(paramRegType);
	}

	public String getParamName() {
		return paramName;
	}

	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	public String getDevName() {
		return devName;
	}

	public void setDevName(String devName) {
		this.devName = devName;
	}

	public Integer getParamAdr() {
		return paramAdr;
	}

	public void setParamAdr(Integer paramAdr) {
		this.paramAdr = paramAdr;
	}

	public String getConvFactor() {
		return convFactor;
	}

	public void setConvFactor(String convFactor) {
		this.convFactor = convFactor;
	}

	public String getFormatText() {
		return formatText;
	}

	public void setFormatText(String formatText) {
		this.formatText = formatText;
	}

	public String getParamRegType() {
		return paramRegType;
	}

	public void setParamRegType(String paramRegType) {
		this.paramRegType = paramRegType;
	}
	
	public String getParamValue() {
		return paramValue.get(0);
	}
	
	public String getParamValue(int idx) {
		return paramValue.get(idx);
	}
	
	public Integer getParamCount() {
		return paramValue.size();
	}
	
	public void clearParamValue() {
		paramValue.clear();
	}

	public void setParamValue(String paramValue) {
		this.paramValue.clear();
		this.paramValue.add(paramValue);
	}
	
	public void setParamValue(String paramValue, int idx) {
		if (this.paramValue.size() > idx) {
			this.paramValue.remove(idx);
		}
		this.paramValue.add(idx, paramValue);
	}
}
