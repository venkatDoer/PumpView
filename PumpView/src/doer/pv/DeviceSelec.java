package doer.pv;
// Developed by Venkat. Copyright 2014 Doer

import java.sql.SQLException;
import java.util.HashMap;

import doer.io.Database;
import doer.io.MyResultSet;
import net.wimpi.modbus.ModbusCoupler;
import net.wimpi.modbus.ModbusException;
import net.wimpi.modbus.io.ModbusSerialTransaction;
import net.wimpi.modbus.msg.ReadCoilsRequest;
import net.wimpi.modbus.msg.ReadCoilsResponse;
import net.wimpi.modbus.msg.ReadInputRegistersRequest;
import net.wimpi.modbus.msg.ReadInputRegistersResponse;
import net.wimpi.modbus.msg.WriteCoilRequest;
import net.wimpi.modbus.msg.WriteMultipleRegistersRequest;
import net.wimpi.modbus.net.SerialConnection;
import net.wimpi.modbus.procimg.Register;
import net.wimpi.modbus.procimg.SimpleRegister;
import net.wimpi.modbus.util.SerialParameters;

/* Implementation class for handling SELEC PLC*/

public class DeviceSelec {
	// default attributes
	private Integer devId = 0;
	private String devPort = "";
	private Integer wCount = 2;  // default
	private SerialParameters params = null;
	private SerialConnection con = null; //the connection
	private ModbusSerialTransaction trans = null; //the transaction
	private ModbusSerialTransaction transHolReg = null;
	private ReadInputRegistersRequest req = new ReadInputRegistersRequest(0, wCount); //the request
	private ReadInputRegistersResponse res = null; //the response
	ReadCoilsRequest icoil = null;
	WriteCoilRequest ocoil = null;
	ModbusSerialTransaction transCoil = null;
	
	private String errMsg = null;
	
	// config attributes
	private String stationId = "Output1"; // panel output
	private HashMap<String, Integer> paramList = new HashMap<String, Integer>();
	private HashMap<String, Integer> errCorList = new HashMap<String, Integer>();
	
	/* function to load device config */
	public void setStation() throws Exception {
		// load config from db
		MyResultSet res = null;
		try {
			Database db = Database.getInstance();
			
			res = db.executeQuery("select a.*, b.* from DEVICE a join DEVICE_PARAM b on b.dev_id=a.dev_id where a.line='" + Configuration.LINE_NAME + "' and a.dev_name='PLC' and a.is_in_use='true'");
			paramList.clear();
			errCorList.clear();
			while (res.next()) {
				paramList.put(res.getString("station_no")+ " " + res.getString("param_name"), res.getInt("param_adr"));
				errCorList.put(res.getString("station_no")+ " " + res.getString("param_name"), res.getInt("err_cor"));
				devId = res.getInt("dev_adr"); 
				devPort = res.getString("dev_port");
			}
		} catch (SQLException se) {
			throw new Exception("Error loading device settings:" + se.getMessage());
		}
	}
	
	
	/* function to initialize the device communication */
	public void initDevCom() throws Exception {
		if (devPort.isEmpty()) {
			throw new Exception("Port is not configured yet");
		}
		
		// serial port parameters
		params = new SerialParameters();
		params.setPortName(devPort);
		params.setBaudRate(19200);
		params.setDatabits(8);
		params.setParity("None");
		params.setStopbits(2);
		params.setEncoding("rtu");
		params.setEcho(false);
		
		//Set master identifier
		ModbusCoupler.getReference().setUnitID(1);
		
		// Open the connection & transaction
		con = new SerialConnection(params);
		try {
			con.open();
		} catch (Exception e) {
			throw new Exception("Error opening connection to " + params.getPortName() + ":" + e.getMessage());
		}
		trans = new ModbusSerialTransaction(con);
		
		transHolReg = new ModbusSerialTransaction(con);
		transCoil = new ModbusSerialTransaction(con);
		transCoil.setRetries(0);
		
		icoil = new ReadCoilsRequest(0, 1);
		icoil.setHeadless();
		ocoil = new WriteCoilRequest();
		ocoil.setHeadless();
	}
	
	/* function to close the device communication */
	public void closeCom(){
		if (con != null && con.isOpen()) {
			con.close();
		}
	}
	
	/* function to check device is in use or not */
	public Boolean isDevInUse() {
		return paramList.size() > 0;
	}
	
	/* functions to read values for device parameters */
	public Float getPressure(String stationId) throws Exception {
		return readData(paramList.get(stationId + " " + "Pressure")) * 10 + errCorList.get(stationId + " " + "Pressure");
	}
	public Float getVacuum(String stationId) throws Exception {
		return readData(paramList.get(stationId + " " + "Vacuum")) + errCorList.get(stationId + " " + "Vacuum");
	}
	public Float getFlow(String stationId) throws Exception {
		return readData(paramList.get(stationId + " " + "Flow")) + errCorList.get(stationId + " " + "Flow");
	}
	public Float getSelfPrimeTime(String stationId) throws Exception {
		return readData(paramList.get(stationId + " " + "Self Priming Time"));
	}
	public synchronized Boolean getCoil(String stationId, String coilName) throws Exception {
		return readCoil(paramList.get(stationId + " " + coilName));
	}
	public synchronized void setCoil(String stationId, String coilName, Boolean val) throws Exception {
		writeCoil(devId, paramList.get(stationId + " " + coilName), val);
	}
	
	/* function to read all available readings of this device */
	public synchronized HashMap<String, Float> readAllParams(String stationId) throws Exception {
		HashMap<String, Float> allData = new HashMap<String, Float>();
		allData.put("Pressure", getPressure(Configuration.OP_PRESSURE_VAC_OUTPUT.get(stationId)));
		allData.put("Vacuum", getVacuum(Configuration.OP_PRESSURE_VAC_OUTPUT.get(stationId)));
		allData.put("Flow", getFlow(Configuration.OP_FLOW_OUTPUT.get(stationId)));
		allData.put("Self Priming Time", getSelfPrimeTime(Configuration.OP_PRESSURE_VAC_OUTPUT.get(stationId)));
		return allData;
	}
	
	/* function to read data from list of addresses for given device */
	public Float readData(Integer adr) throws Exception {
		// create request
		req.setUnitID(devId);
		req.setWordCount(wCount);
		req.setReference(adr);
		req.setHeadless();
		trans.setRequest(req);
			
		// execute transaction
		try {
			trans.execute();
		} catch (ModbusException e) {
			throw new Exception("Error executing transaction:" + trans.getRequest().getHexMessage() + " in port:" + params.getPortName() + ":" + e.getMessage());
		}
			
		res = (ReadInputRegistersResponse) trans.getResponse();
		
		// convert the 32 bits hex string to long, then float
		Float floatReading = 0F;
		try {
			String resHexStr = res.getHexMessage().substring(9).replace(" ", "");
			resHexStr = resHexStr.substring(4) + resHexStr.substring(0,4); // workaround: reverse the bytes
			if(resHexStr.startsWith("f")) {
				Integer intBits = (int) Long.parseLong(resHexStr, 16);
				floatReading = intBits.floatValue()/100;
			} else {
				Long longBits = Long.valueOf(resHexStr, 16);
				floatReading = longBits.floatValue()/100;
			}
		} catch (NumberFormatException ne) {
			// ignore
		}
		return floatReading;
	}
	
	public synchronized void setSuctionLift(String station, Integer val) throws Exception {
		if (paramList.containsKey(station + " " + "Set Suction Lift")) {
			writeHoldingReg(paramList.get(station + " " + "Set Suction Lift"), val);
		}
	}
	
	public synchronized void setResult(String station, Integer val) throws Exception {
		writeHoldingReg(paramList.get(station + " " + "Set Result"), val);
	}
	
	public synchronized void setSNo(String station, Integer val) throws Exception {
		writeHoldingReg(paramList.get(station + " " + "Set SNo"), val);
	}
	
	public synchronized void setCurTest(String station, Integer val) throws Exception {
		writeHoldingReg(paramList.get(station + " " + "Set Current Test"), val);
	}
	
	public synchronized void setTotTest(String station, Integer val) throws Exception {
		writeHoldingReg(paramList.get(station + " " + "Set Total Test"), val);
	}
	
	/* function to write into a holding register */
	public synchronized void writeHoldingReg(Integer adr, Integer val) throws Exception {
		writeHoldingReg(devId, adr, val);
	}
	public synchronized void writeHoldingReg(Integer deviceId, Integer adr, Integer val) throws Exception {
		WriteMultipleRegistersRequest req = null; //the request
		
		Register r[] = {null,null};
		r[0] = new SimpleRegister(val); // msb
		r[1] = new SimpleRegister(0);// lsb
		//r[2] = new SimpleRegister (-1); // resl

		// prepare request for the address to be written and write the data
		req = new WriteMultipleRegistersRequest(adr, r);
		req.setUnitID(deviceId);
		req.setHeadless();
		
		transHolReg.setRequest(req);
		// Execute the transaction repeat times
		try {
			transHolReg.execute();
		} catch (ModbusException e) {
			errMsg = "Error while executing transaction:" + transHolReg.getRequest().getHexMessage() + " in port:" + params.getPortName() + ":" + e.getMessage();
			throw new Exception(errMsg);
		}
	}
	
	/* function to read input coil */
	public synchronized Boolean readCoil(Integer adr) throws Exception {
		return readCoil(devId, adr);
	}
	public synchronized Boolean readCoil(Integer deviceId, Integer adr) throws Exception {
		icoil.setUnitID(deviceId);
		icoil.setReference(adr);
		transCoil.setRequest(icoil);
		
		try {
			transCoil.execute();
			return ((ReadCoilsResponse) transCoil.getResponse()).getCoilStatus(0);
		} catch (ModbusException e) {
			errMsg = "Error while executing transaction:" + transCoil.getRequest().getHexMessage() + " in port:" + params.getPortName() + ":" + e.getMessage();
			throw new Exception(errMsg);
		}
	}
	
	/* function to write output coil */
	public synchronized void writeCoil(Integer adr, Boolean val) throws Exception {
		writeCoil(devId, adr, val);
	}
	public synchronized void writeCoil(Integer deviceId, Integer adr, Boolean val) throws Exception {
		ocoil.setUnitID(deviceId);
		ocoil.setReference(adr);
		ocoil.setCoil(val);
		transCoil.setRequest(ocoil);
		
		try {
			transCoil.execute();
		} catch (ModbusException e) {
			errMsg = "Error while executing transaction:" + transCoil.getRequest().getHexMessage() + " in port:" + params.getPortName() + ":" + e.getMessage();
			throw new Exception(errMsg);
		}
	}
}