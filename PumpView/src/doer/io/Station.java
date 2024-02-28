package doer.io;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import doer.pv.Configuration;
import net.wimpi.modbus.util.SerialParameters;

public class Station {
	// attributes
	public String stationName = "";
	public Integer stationIdx = 0;
	public String lastUsedPumpSNo = "";
	public String lastUsedMotSNo = "";
	public Integer lastUsedTestSNo = 0;
	public String scannedPumpSNo = "";
	public Boolean testInProg = false;
	private String lastReadError = "";
	
	// pump attached to this output
	public Integer testStartRow = 0;
	public String curPumpTypeId = "";
	public String curPumpType = "";
	public String curPumpDesc = "";
	public String curPumpPhase = "";
	public String curPumpFlowUnit = "";
	public String curHdUnit = "";
	public String curPrOPSt = "Station1";
	public String curFlowOPSt = "Station1";
	public String curPrOP = "A";
	public String curFlowOP = "A";
	public Integer curTypeTestFreq = 0;
	public String curAutoValveType = "H";
	public String curOtherVoltsDis = "N";
	public ArrayList<String> voltList = new ArrayList<String>(); // voltages related to this pump type
	public ArrayList<String> testListRoutine = new ArrayList<String>();
	public ArrayList <String> testListType = new ArrayList<String>();
	public LinkedHashMap <String, String> testList = new LinkedHashMap<String, String>();
	public LinkedHashMap <String, String> testListFromName = new LinkedHashMap<String, String>();
	public LinkedHashMap <String, Float> testListHead = new LinkedHashMap<String, Float>();
	
	// test related
	public Boolean nextNewSet = false;
	public Boolean noChangeSNo = false;
	public Boolean reTest = false;
	public String nextTestType = "";
	public String nextTestCode = "";
	public String nextRatedVolt = "";
	public Integer noRatedVolt = 0;
	
	// devices of this output
	private LinkedHashMap<String, Parameter> paramList = null;
	private LinkedHashMap<String, Device> devList = null;
	public HashMap<String, String> devHeadData = null;
	public HashMap<String, String> devPowerData = null;
	public HashMap<String, String> devSpeedData = null;
	private Parameter curParam = null;
	
	public Station(String station) {
		this.stationName = station;
		this.stationIdx = Integer.valueOf(station.substring(7))-1;
		paramList = new LinkedHashMap<String, Parameter>();
		devList = new LinkedHashMap<String, Device>();
		devHeadData = new HashMap<String, String>();
		devPowerData = new HashMap<String, String>();
		devSpeedData = new HashMap<String, String>();
	}
	
	public Station() {
		paramList = new LinkedHashMap<String, Parameter>();
		devList = new LinkedHashMap<String, Device>();
	}
	
	// function to refresh devices and related params
	public void refreshDevices() throws Exception {
		MyResultSet res = null;
		try {
			// clear existing list
			if (devList.size() > 0) {
				paramList.clear();
				// close devices properly before clearing device list
				closeDevices();
				devList.clear();
			}
				
			// refresh device settings
			Database db = Database.getInstance();
			
			// parameters
			res = db.executeQuery("select a.*, b.* from DEVICE a, DEVICE_PARAM b where a.dev_id = b.dev_id and a.line ='" + Configuration.LINE_NAME + "' and a.station_no = '" + stationName + "' and a.is_in_use='true' order by a.dev_id, b.param_name");
			String curParamNm = "";
			while (res.next()) {
				curParamNm = res.getString("param_name");
				// System.out.println("Inserting param " + curParamNm);
				paramList.put(curParamNm, new Parameter(curParamNm, res.getString("dev_name"), res.getInt("param_adr"), res.getString("conv_factor"),res.getString("format_text"),res.getString("reg_type")));
				// add device if not exist already
				if (!devList.containsKey(res.getString("dev_name"))) {
					devList.put(res.getString("dev_name"), new Device(res.getString("dev_name"), res.getInt("dev_adr"), res.getString("dev_type"), res.getInt("wc"), res.getString("endianness"), new SerialParameters(res.getString("dev_port"),res.getInt("baud_rt"), res.getInt("fc"), res.getInt("fc"), res.getInt("data_bits"), res.getInt("stop_bits"), res.getInt("parity"), false, 2000), res.getString("ip_cmd"), res.getString("is_in_use").equals("true"), res.getString("is_common_port").equals("true"), res.getString("comm_protocol"),res.getString("ip_address"),Integer.parseInt(res.getString("ip_port"))));
				} // if
				if((res.getString("dev_name")).equals("VFD")){
					Configuration.IS_VFD = res.getString("is_in_use").equals("true");
				}
			}
			
		} catch (SQLException se) {
			throw new SQLException("Error loading device list:" + se.getMessage());
		}
	}
	
	// function to get list of devices
	public LinkedHashMap<String, Device>  getDevList() {
		return devList;
	}
		
	// function to get a dev of given name
	public Device getDevice(String devName) throws Exception{
		if (devList.containsKey(devName)) {
			return devList.get(devName);
		} else {
			throw new Exception("Device <" + devName + "> not found");
		}
	}
	
	// function to initialize devices
	public void initDevices() throws Exception {
		String errMsg = "";
		for (Device tmpDev : devList.values()) {
			try {
				tmpDev.initDevice();
			} catch (Exception e) {
				errMsg += tmpDev.getDevName() + ":" + e.getMessage() + "\n";
			}
		}
		if (!errMsg.isEmpty()) {
			throw new Exception("Below error occured while initializing devices\n" + errMsg);
		}
	}
	
	// function to close communication with all devices
	public void closeDevices() throws Exception {
		String errMsg = "";
		for (Device tmpDev : devList.values()) {
			try {
				tmpDev.closeDevice();
			} catch (Exception e) {
				errMsg += tmpDev.getDevName() + ":" + e.getMessage() + "\n";
			}
		}
		if (!errMsg.isEmpty()) {
			throw new Exception("Below error occured while closing devices\n" + errMsg);
		}
	}
	
	// function to get current reading of given parameter
	public String readParamValue(String param) throws Exception {
		return readParamValue(param, 0);
	}
	
	public String readParamValue(String param, Integer idx) throws Exception {
		if (isParamExist(param)) {
			curParam = paramList.get(param);
			return devList.get(curParam.getDevName()).readParam(curParam);
		} else {
			throw new Exception("Parameter not found");
		}
	}
	
	// function to get error message if any out of last reading
	public String getLastReadError() {
		return lastReadError;
	}
	
	// function to write the value into given parameter
	// note: this function can be created with different param type to accomodate writing coils, etc..
	public void writeParamValue(String param, Integer val) throws Exception {
		if (isParamExist(param)) {
			devList.get(paramList.get(param).getDevName()).writeHoldingReg(paramList.get(param).getParamAdr(), val);
		}
	}
	
	public void writeParamValue(String param, Boolean val) throws Exception {
		if (isParamExist(param)) {
			devList.get(paramList.get(param).getDevName()).writeCoil(paramList.get(param).getParamAdr(), val);
		}
	}
	
	// function to check availability of parameter in this station
	public Boolean isParamExist(String param) {
		return paramList.containsKey(param) && paramList.get(param).getParamAdr() >= 0;
	}
}
