package doer.pv;
// Developed by Venkat. Copyright 2014 Doer

import java.util.HashMap;

import net.wimpi.modbus.ModbusCoupler;
import net.wimpi.modbus.ModbusException;
import net.wimpi.modbus.io.ModbusSerialTransaction;
import net.wimpi.modbus.msg.ReadInputRegistersRequest;
import net.wimpi.modbus.msg.ReadInputRegistersResponse;
import net.wimpi.modbus.msg.ReadMultipleRegistersRequest;
import net.wimpi.modbus.msg.ReadMultipleRegistersResponse;
import net.wimpi.modbus.msg.WriteMultipleRegistersRequest;
import net.wimpi.modbus.msg.WriteSingleRegisterRequest;
import net.wimpi.modbus.net.SerialConnection;
import net.wimpi.modbus.procimg.Register;
import net.wimpi.modbus.procimg.SimpleRegister;
import net.wimpi.modbus.util.SerialParameters;

/* Implementation class for handling pressure controller */

public class DevicePresCont {
	// device info
	private Integer wCount = 1;  // default
	SerialConnection con = null; //the connection
	ModbusSerialTransaction trans = null; //the transaction
	
	// serial port param
	private SerialParameters params = null;
	
	// others
	private String errMsg = null;
	
	/* function to initialize the device */
	
	public void initialize(String portName, Integer wordCount) throws Exception{
		if (portName.isEmpty()) {
			throw new Exception("Port is not configured yet");
		}
		
		// serial port parameters
		params = new SerialParameters();
		params.setPortName(portName);
		params.setBaudRate(9600); // 9600
		params.setDatabits(8);
		params.setParity("None");
		params.setStopbits(1);  // 1
		params.setEncoding("rtu");
		params.setEcho(false);
		
		wCount = wordCount;

		//Set master identifier
		ModbusCoupler.getReference().setUnitID(1);
		
		// Open the connection
		con = new SerialConnection(params);
		try {
			con.open();
		} catch (Exception e) {
			errMsg = "Error while opening connection to port:" + params.getPortName() + ":" + e.getMessage();
			throw new Exception(errMsg);
		}
		// Prepare a transaction
		trans = new ModbusSerialTransaction(con);
	}
	
	public void close() {
		con.close();
	}
	
	/* function to read from holding register */
	public Integer readHoldingData(Integer devId, Integer adr) throws Exception {
		
		ReadMultipleRegistersRequest req = null; //the request
		ReadMultipleRegistersResponse res = null; //the response
		
		req = new ReadMultipleRegistersRequest(adr-1, wCount);
		req.setUnitID(devId);
		req.setHeadless();
		
		trans.setRequest(req);
		
		try {
			trans.execute();
		} catch (ModbusException e) {
			errMsg = "Error while executing transaction:" + trans.getRequest().getHexMessage() + " in port:" + params.getPortName() + ":" + e.getMessage();
			throw new Exception(errMsg);
		}
		
		res = (ReadMultipleRegistersResponse) trans.getResponse();
		
		Integer intReading = 0;
		try {
			intReading = res.getRegisterValue(0);
		} catch (Exception e) {
			errMsg = "Error while reading integer values from hex message:" + trans.getRequest().getHexMessage() + " in port:" + params.getPortName() + ":" + e.getMessage();
			throw new Exception(errMsg);
		}
		return intReading;
	}

	/* function to write into a register */
	public void writeData(Integer devId, Integer adr, Integer val) throws Exception {
		WriteSingleRegisterRequest req = null; //the request
		
		// prepare request for the address to be written and write the data
		req = new WriteSingleRegisterRequest(adr-1, new SimpleRegister(val));
		req.setUnitID(devId);
		req.setHeadless();
		
		trans.setRequest(req);
		// Execute the transaction repeat times
		try {
			trans.execute();
		} catch (ModbusException e) {
			errMsg = "Error while executing transaction:" + trans.getRequest().getHexMessage() + " in port:" + params.getPortName() + ":" + e.getMessage();
			throw new Exception(errMsg);
		}
	}
}
