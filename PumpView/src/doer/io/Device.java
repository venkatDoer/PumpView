package doer.io;

import java.text.DecimalFormat;
import java.util.HashMap;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import net.wimpi.modbus.util.SerialParameters;

public class Device {
	// class to perform operations on device settings table
	private String devName = "";
	private Integer devAdr = 1;
	private String devType = "";
	private Integer wc = 2;
	private SerialParameters params = null;
	private String ipCmd = "";
	private String endianness = "";
	private Boolean isInUse = true;
	private Boolean isCommonPort = true;
	private Boolean initError = false;
	
	//TCP_IP Parameters
	private String ipAddress = null;
	private Integer ipPort = 2200;
	private String protocol = "RTU";
	
	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public Integer getIpPort() {
		return ipPort;
	}

	public void setIpPort(Integer ipPort) {
		this.ipPort = ipPort;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	
	
	// device handlers
	private DeviceSerialReader devSerRdr = null;
	private DeviceModbusReader devModRdr = null;
	private DeviceHIOKI devHIOKI = null;
	
	// other variables
	String strReading = "";
	String strReading2 = "";
	String strReading3 = "";
	String[] strAry = null;
	Float floatReading = 0F;
	DecimalFormat decFormat = new DecimalFormat();
	ScriptEngineManager jsMgr = new ScriptEngineManager();
	ScriptEngine jsEng = jsMgr.getEngineByName("JavaScript");
	HashMap<String, HashMap<String, Integer>> controlList = null;
	
	// constructor (note: ipCmd is required only for serial device)
	public Device(String devName, Integer devAdr, String devType, Integer wc, String endianness, SerialParameters serParams, String ipCmd, Boolean isInUse, Boolean isCommonPort, String Protocol, String ipAddress, Integer ipPort) {
		this.setDevName(devName);
		this.setDevAdr(devAdr);
		this.setDevType(devType);
		this.setWc(wc);
		this.setEndianness(endianness);
		this.setParams(serParams);
		this.setIpCmd(ipCmd);
		this.setIsInUse(isInUse);
		this.setIsCommonPort(isCommonPort);
		this.setProtocol(Protocol);
		this.setIpAddress(ipAddress);
		this.setIpPort(ipPort);
		this.initError = false;
		controlList = new HashMap<String, HashMap<String, Integer>>();
	}

	public String getDevName() {
		return devName;
	}

	public void setDevName(String devName) {
		this.devName = devName;
	}

	public Integer getDevAdr() {
		return devAdr;
	}

	public void setDevAdr(Integer devAdr) {
		this.devAdr = devAdr;
	}

	public String getDevType() {
		return devType;
	}

	public void setDevType(String devType) {
		this.devType = devType;
	}

	public SerialParameters getParams() {
		return params;
	}

	public void setParams(SerialParameters params) {
		this.params = params;
	}

	public Integer getWc() {
		return wc;
	}

	public void setWc(Integer wc) {
		this.wc = wc;
	}
	
	public String getEndianness() {
		return endianness;
	}
	
	public void setEndianness(String endianness) {
		this.endianness = endianness;
	}
	
	public String getIpCmd() {
		return ipCmd;
	}

	public void setIpCmd(String ipCmd) {
		this.ipCmd = ipCmd;
	}
	
	public Boolean isInitError() {
		return this.initError;
	}
	
	// function to initialize device
	public void initDevice() throws Exception {
		this.initError = true;
		
		// close the connection if already open
		// System.out.println("Begin Init dev type " + devType);
		// initialize device based on its type
		if (devType.equals("S")) { // 1. serial device
			devSerRdr = DeviceSerialReader.getInstance(params, isCommonPort);
		} else if (devType.equals("M")) { // 2. modbus device
			if(devName.equals("HIOKI Power Meter")) {
				devHIOKI = DeviceHIOKI.getInstance(ipAddress, ipPort, protocol, isCommonPort);
			}else {
				devModRdr = DeviceModbusReader.getInstance(devAdr, wc, endianness.equals("LSB First"), params, isCommonPort, protocol, ipAddress, ipPort);
			}
		} else  {
			throw new Exception("Invalid device type '" + devType + "' for device:" + devName);
		}
		
		// System.out.println("Init dev type " + devType);
		
		this.initError = false;
	}
	
	// function to close device comm
	public void closeDevice() throws Exception {
		// close the connection if already open
		if (devSerRdr != null) {
			devSerRdr.closePort();
		} else if (devModRdr != null) {
			devModRdr.close();
		} else if (devHIOKI != null) {
			devHIOKI.close();
		}
	}
	
	// function to return register of given control param
	public Integer getControlRegister(String station, String param) throws Exception {
		if (controlList.containsKey(station)) {
			if (controlList.containsKey(station)) {
				return controlList.get(station).get(param);
			} else {
				throw new Exception("Output <" + station + "> not found");
			}
		} else {
			throw new Exception("Parameter <" + param + "> not found");
		}
	}
	
	// function to read value for a register
	public synchronized Boolean readCoil(Integer adr) throws Exception {
		// System.out.println("getting coil " + adr + " with " + devModRdr.readCoil(devAdr, adr));
		return devModRdr.readCoil(devAdr, adr, protocol);
	}
	
	/* function to write output coil */
	public synchronized void writeCoil(Integer adr, Boolean val) throws Exception {
		//System.out.println("writing coil " + adr + " to " + val);
		devModRdr.writeCoil(devAdr, adr, val, protocol);
	}
	
	/* function to write into a holding register */
	public synchronized void writeHoldingReg(Integer adr, Integer val) throws Exception {
		devModRdr.writeHoldingReg(devAdr, adr, val, protocol);
	}
	
	// function to read value for a register
	public synchronized String readParam(Parameter param) throws Exception {
		// 1. read device reading
		if (devType.equals("M")) { // modbus device
			// System.out.println("reading from " + devModRdr + ",dev id:" + devAdr + " " + param.getParamRegType() + " adr:" + param.getParamAdr());
			if (devName.equals("HIOKI Power Meter")) {
				// write and read pased on requested param
				String paramName = param.getParamName();
				if (paramName.contains("3 Ph") && !paramName.contains("Frequency")) {
					if (paramName.equals("Current 3 Ph")) {
						strReading = devHIOKI.readValue(":MEAS? I0");
					} else if (paramName.equals("Voltage 3 Ph")) {
						strReading = devHIOKI.readValue(":MEAS? UFND0");
					} else if (paramName.equals("Power 3 Ph")) {
						strReading = devHIOKI.readValue(":MEAS? P0");
					} else {
						throw new Exception("Param not found <" + paramName + ">");
					}
					try {
						strReading = strReading.substring(strReading.indexOf(" ")+1).trim();
					} catch (Exception ne) {
						throw new Exception("HIOKI Power Meter Error:Unable to understand " + paramName + " reading:" + strReading);
					}
				} else { // single phase
					if (paramName.equals("Current")) {
						strReading = devHIOKI.readValue(":MEAS? I1");
					} else if (paramName.equals("Voltage")) {
						strReading = devHIOKI.readValue(":MEAS? UFND1");
					} else if (paramName.equals("Power")) {
						strReading = devHIOKI.readValue(":MEAS? P1");
					} else if (paramName.contains("Frequency")) { // 1 or 3 phase
						strReading = devHIOKI.readValue(":MEAS? FREQU1");
					} else {
						throw new Exception("Param not found <" + paramName + ">");
					}
					try {
						strReading = strReading.substring(strReading.indexOf(" ")+1).trim();
					} catch (Exception ne) {
						throw new Exception("HIOKI Power Meter Error:Unable to understand " + paramName + " reading:" + strReading);
					}
				}
				// GENERIC IMPLEMENTATION
				floatReading = Math.abs(Float.parseFloat(strReading));
			}
			else if (param.getParamRegType().equals("Coil")) {
				return devModRdr == null ? "false" : devModRdr.readCoil(devAdr, param.getParamAdr(),protocol) ? "true" : "false";
			} else if (param.getParamRegType().equals("Input")) {
					floatReading = (devModRdr.readInputReg(devAdr, Integer.valueOf(param.getParamAdr()),protocol))/1.0F;
			} else if (param.getParamRegType().equals("Holding")) {
				floatReading = (devModRdr.readHoldingReg(devAdr, Integer.valueOf(param.getParamAdr()),protocol))/1.0F;
			} else if (param.getParamRegType().equals("Input Float")) {
				floatReading = devModRdr.readInputRegFloat(devAdr, Integer.valueOf(param.getParamAdr()),protocol);
			} else if (param.getParamRegType().equals("Holding Float")){
				floatReading = devModRdr.readHoldingRegFloat(devAdr, Integer.valueOf(param.getParamAdr()),protocol);
			} else {
				throw new Exception("Invalid register type:" + param.getParamRegType());	
			}
		} else if (devType.equals("S")) { // 1. serial device, register NA
			// SERIAL DEVICE SPECIAL HANDLING SINCE THIS IMPLEMENTATION IS NOT 100% GENERIC FOR SERIAL DEVICES
			if (devName.equals("Speed Tester")) { // 1. magtrol
				strReading = devSerRdr.writeAndReadData(ipCmd==null?"":ipCmd, "+");
				if (!strReading.contains("+")) {
					throw new Exception("Speed Tester Error:Unable to understand reading:" + strReading);
				} else {
					strReading = strReading.substring(strReading.indexOf("+")+1).trim();
				}
			} else if (devName.equals("AAROHI Meter")) {
				// write and read pased on requested param
				String paramName = param.getParamName();
				if (paramName.contains("3 Ph") && !paramName.contains("Frequency")) { // 3 phase needs additional logic
					if (paramName.equals("Current 3 Ph")) {
						// find average
						strReading = devSerRdr.writeAndReadData("A1", "+");
						strReading2 = devSerRdr.writeAndReadData("A2", "+");
						strReading3 = devSerRdr.writeAndReadData("A3", "+");
						try {
							strReading = strReading.substring(strReading.indexOf("+")+1).trim();
							strReading2 = strReading2.substring(strReading2.indexOf("+")+1).trim();
							strReading3 = strReading3.substring(strReading3.indexOf("+")+1).trim();
							// average
							strReading = String.valueOf(((Float.valueOf(strReading) + Float.valueOf(strReading2) + Float.valueOf(strReading3))/3F));
						} catch (Exception ne) {
							throw new Exception("AAROHI Meter Error:Unable to understand current reading:" + strReading + "," + strReading2 + "," + strReading3);
						}
					} else if (paramName.equals("Voltage 3 Ph")) {
						// find average
						strReading = devSerRdr.writeAndReadData("v1", "+");
						strReading2 = devSerRdr.writeAndReadData("v2", "+");
						strReading3 = devSerRdr.writeAndReadData("v3", "+");
						try {
							strReading = strReading.substring(strReading.indexOf("+")+1).trim();
							strReading2 = strReading2.substring(strReading2.indexOf("+")+1).trim();
							strReading3 = strReading3.substring(strReading3.indexOf("+")+1).trim();
							// average
							strReading = String.valueOf(((Float.valueOf(strReading) + Float.valueOf(strReading2) + Float.valueOf(strReading3))/3F));
						} catch (Exception ne) {
							throw new Exception("AAROHI Meter Error:Unable to understand voltage reading:" + strReading + "," + strReading2 + "," + strReading3);
						}
					} else if (paramName.equals("Power 3 Ph")) {
						// find sum
						strReading = devSerRdr.writeAndReadData("W1", "+");
						strReading2 = devSerRdr.writeAndReadData("W2", "+");
						strReading3 = devSerRdr.writeAndReadData("W3", "+");
						try {
							strReading = strReading.substring(strReading.indexOf("+")+1).trim();
							strReading2 = strReading2.substring(strReading2.indexOf("+")+1).trim();
							strReading3 = strReading3.substring(strReading3.indexOf("+")+1).trim();
							// sum
							strReading = String.valueOf(((Float.valueOf(strReading) + Float.valueOf(strReading2) + Float.valueOf(strReading3))));
						} catch (Exception ne) {
							throw new Exception("AAROHI Meter Error:Unable to understand voltage reading:" + strReading + "," + strReading2 + "," + strReading3);
						}
					} else {
						throw new Exception("Param not found <" + paramName + ">");
					}
				} else { // single phase
					if (paramName.equals("Current")) {
						strReading = devSerRdr.writeAndReadData("A1", "+");
					} else if (paramName.equals("Voltage")) {
						strReading = devSerRdr.writeAndReadData("V1", "+");
					} else if (paramName.equals("Power")) {
						strReading = devSerRdr.writeAndReadData("W1", "+");
					} else if (paramName.contains("Frequency")) { // single or 3 ph freq
						strReading = devSerRdr.writeAndReadData("H1", "+");
					} else if (paramName.equals("Speed")) {
						strReading = devSerRdr.writeAndReadData("R1", "+");
					} else if (paramName.equals("Pressure A")) {
						strReading = devSerRdr.writeAndReadData("P1", "+");
					} else if (paramName.equals("Vacuum A")) {
						strReading = "0+0";
					} else if (paramName.equals("Flow A")) {
						strReading = devSerRdr.writeAndReadData("F1", "+");
					} else {
						throw new Exception("Param not found <" + paramName + ">");
					}
					if (!strReading.contains("+") && !strReading.contains("-")) {
						throw new Exception("AAROHI Meter Error:Unable to understand " + paramName.toLowerCase() + " reading:" + strReading);
					} else if (strReading.contains("+")) {
						strReading = strReading.substring(strReading.indexOf("+")+1).trim();
					} else {
						strReading = "0";
					}
				}
		    } else if (devName.equals("HIOKI Power Meter")) {
				// write and read pased on requested param
				String paramName = param.getParamName();
				if (paramName.contains("3 Ph") && !paramName.contains("Frequency")) {
					if (paramName.equals("Current 3 Ph")) {
						strReading = devSerRdr.writeAndReadData(":MEAS? I0", "I0");
					} else if (paramName.equals("Voltage 3 Ph")) {
						strReading = devSerRdr.writeAndReadData(":MEAS? UFND0", "UFND0");
					} else if (paramName.equals("Power 3 Ph")) {
						strReading = devSerRdr.writeAndReadData(":MEAS? P0", "P0");
					} else {
						throw new Exception("Param not found <" + paramName + ">");
					}
					try {
						strReading = strReading.substring(strReading.indexOf(" ")+1).trim();
					} catch (Exception ne) {
						throw new Exception("HIOKI Power Meter Error:Unable to understand " + paramName + " reading:" + strReading);
					}
				} else { // single phase
					if (paramName.equals("Current")) {
						strReading = devSerRdr.writeAndReadData(":MEAS? I1", "I1");
					} else if (paramName.equals("Voltage")) {
						strReading = devSerRdr.writeAndReadData(":MEAS? UFND1", "UFND1");
					} else if (paramName.equals("Power")) {
						strReading = devSerRdr.writeAndReadData(":MEAS? P1", "P1");
					} else if (paramName.contains("Frequency")) { // 1 or 3 phase
						strReading = devSerRdr.writeAndReadData(":MEAS? FREQU1", "FREQU1");
					} else {
						throw new Exception("Param not found <" + paramName + ">");
					}
					try {
						strReading = strReading.substring(strReading.indexOf(" ")+1).trim();
					} catch (Exception ne) {
						throw new Exception("HIOKI Power Meter Error:Unable to understand " + paramName + " reading:" + strReading);
					}
				}
		    } else if (devName.equals("FREHNIG Flow Meter")) {
				strReading = devSerRdr.writeAndReadData(ipCmd, " ");
				System.out.println("Available flow reading " + strReading);
				try {
				// parse and get the value F1 BB F2 DD
				strAry = strReading.split(" ");
				strReading = String.valueOf(Float.valueOf(Integer.parseInt(strAry[0], 16)) * Float.valueOf(Integer.parseInt(strAry[2], 16)));
				} catch (Exception e) {
					throw new Exception("FREHNIG Flow Meter Error:Unable to understand reading:" + strReading);
				}
			}
			// END OF SPECIAL HANDLING 
			
			// GENERIC IMPLEMENTATION
			floatReading = Math.abs(Float.parseFloat(strReading));
		}
		
		// 2. apply conversion formula
		if (!param.getConvFactor().isEmpty()) {
			floatReading = Float.parseFloat(jsEng.eval(floatReading + param.getConvFactor()).toString());
		}
		
		// 3. format
		decFormat.applyPattern(param.getFormatText());
		return (decFormat.format(floatReading)); 
	}
	
	public Boolean getIsInUse() {
		return isInUse;
	}

	public void setIsInUse(Boolean isInUse) {
		this.isInUse = isInUse;
	}
	
	public Boolean getIsCommonPort() {
		return isCommonPort;
	}

	public void setIsCommonPort(Boolean isCommonPort) {
		this.isCommonPort = isCommonPort;
	}
}
