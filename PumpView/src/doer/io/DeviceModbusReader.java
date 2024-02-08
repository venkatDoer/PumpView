package doer.io;
// Developed by Venkat. Copyright 2014 Doer

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.HashMap;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import doer.pv.Configuration;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import net.wimpi.modbus.ModbusCoupler;
import net.wimpi.modbus.ModbusException;
import net.wimpi.modbus.io.ModbusSerialTransaction;
import net.wimpi.modbus.io.ModbusTCPTransaction;
import net.wimpi.modbus.msg.ReadCoilsRequest;
import net.wimpi.modbus.msg.ReadCoilsResponse;
import net.wimpi.modbus.msg.ReadInputRegistersRequest;
import net.wimpi.modbus.msg.ReadInputRegistersResponse;
import net.wimpi.modbus.msg.ReadMultipleRegistersRequest;
import net.wimpi.modbus.msg.ReadMultipleRegistersResponse;
import net.wimpi.modbus.msg.WriteCoilRequest;
import net.wimpi.modbus.msg.WriteMultipleRegistersRequest;
import net.wimpi.modbus.net.SerialConnection;
import net.wimpi.modbus.net.TCPMasterConnection;
import net.wimpi.modbus.procimg.Register;
import net.wimpi.modbus.procimg.SimpleRegister;
import net.wimpi.modbus.util.SerialParameters;

/* Implementation class for handling PID 500 PLC*/

public class DeviceModbusReader {
	
	// device changeable params
	private Integer devId = 1; 
	private Integer wCount = 2;
	private Boolean isLsbFirst = false;
	private String Protocol = null;
	private InetAddress ipaddr = null;
	private Integer ipport = 2200;
	
	// communication
	SerialConnection con1 = null;
	ModbusSerialTransaction transCoilRead = null;
	ModbusSerialTransaction transCoilWrite = null;
	ModbusSerialTransaction transInReg = null;
	ModbusSerialTransaction transHolRegRead = null;
	ModbusSerialTransaction transHolRegWrite = null;
	
	TCPMasterConnection con2 = null;
	ModbusTCPTransaction iptransCoilRead = null;
	ModbusTCPTransaction iptransCoilWrite = null;
	ModbusTCPTransaction iptransInReg = null;
	ModbusTCPTransaction iptransHolRegRead = null;
	ModbusTCPTransaction iptransHolRegWrite = null;
	
	ReadCoilsRequest icoil = null;
	WriteCoilRequest ocoil = null;
	ReadInputRegistersRequest ireq = null;
	ReadMultipleRegistersRequest mreq = null;
	WriteMultipleRegistersRequest wreq = null;
	
	// others
	private SerialParameters params = null;
	private String errMsg = null;
	private String resHexStr = null;
	
	private static HashMap<String, DeviceModbusReader> devModRdrList = new HashMap<String, DeviceModbusReader>();
	
	public static DeviceModbusReader getInstance(Integer devAdr, Integer wc, Boolean isLsbFirst, SerialParameters params, Boolean isCommonPort, String protocol, String ipAddress, Integer ipPort) throws Exception {
		if (devModRdrList.containsKey(params.getPortName()) && isCommonPort) {
			return devModRdrList.get(params.getPortName());
		} else {
			DeviceModbusReader tmpRdr = new DeviceModbusReader(devAdr, wc, isLsbFirst, params, protocol, ipAddress, ipPort);
			devModRdrList.put(params.getPortName(), tmpRdr);
			return tmpRdr;
		}
	}
	
	public static void clearList() {
		devModRdrList.clear();
	}
	
	public DeviceModbusReader(Integer devId, Integer wc, Boolean isLsbFirst, SerialParameters params, String protocol, String ipAddress, Integer ipPort) throws Exception{
		initialize(devId, params.getPortName(), params.getBaudRate(), params.getDatabits(), params.getStopbits(), params.getParity(), wc, isLsbFirst, protocol, ipAddress, ipPort);
	}
	
	/* function to initialize the device */
	public void initialize(String portName) throws Exception{
		initialize(devId, portName, 9600, 8, 1, 0, wCount, isLsbFirst,"RTU","",2200);
	}
	
	public void initialize(Integer deviceId, String portName, Integer baudRate, Integer dataBits, Integer stopBits, Integer parity, Integer wordCount, Boolean isLsbFirst, String protocol, String ipAddress, Integer ipPort) throws Exception {
		
		devId = deviceId;
		wCount = wordCount;
		this.isLsbFirst = isLsbFirst;
		Protocol = protocol;
		// serial port parameters
		params = new SerialParameters();
		params.setPortName(portName);
		params.setBaudRate(baudRate);
		params.setDatabits(dataBits);
		params.setParity(parity);
		params.setStopbits(stopBits);
		params.setEncoding("rtu");
		params.setEcho(false);
		
		// set master identifier
		ModbusCoupler.getReference().setUnitID(1);
		
		// open the connection for RTU Communication
		con1 = new SerialConnection(params);
		
		// Open the connection for TCP_IP Communication
		ipaddr = InetAddress.getByName(ipAddress);
		ipport = ipPort;
		con2 = new TCPMasterConnection(ipaddr);
        con2.setPort(ipport);
        
        
		try {
			if(Protocol.equals("RTU")) {
				con1.open();
			}else {
				con2.connect();
//				System.out.println("ip Connected");
			}
		} catch (UnsatisfiedLinkError le) {
			// copy the missing lib
			String javaDir = System.getProperty("java.home");
			String libFile = Configuration.APP_DIR + "\\lib\\doer\\rxtxSerial.dll";
			
			try {
				String cmd = "xcopy /YV  \"" + libFile + "\" \"" +  javaDir + "\\bin\"";
				Process p = Runtime.getRuntime().exec(cmd);
				BufferedReader er = new BufferedReader(new InputStreamReader(p.getErrorStream()));
				BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String tmpStr = "";
				String filesError = "";
				// check for any error or output
				
				Thread.sleep(1000);
				if (er.ready()) {
					while ((tmpStr = er.readLine()) != null) {
						filesError += tmpStr + "\n";
					}
					if (!filesError.isEmpty()) {
						throw new Exception(filesError);
					}
				}
				
				// ignore the output
				Thread.sleep(1000);
				if (in.ready()) {
					while ((tmpStr = in.readLine()) != null) {
						filesError += tmpStr + "\n";
					}
				}
				
				er.close();
				in.close();
				p.destroy();
				
				JOptionPane.showMessageDialog(new JDialog(), "It seems Java got updated recently. Hence, a file required to run this application has just been copied.\nPlease close this dialog and re-open the application", "Warning", JOptionPane.WARNING_MESSAGE);
				System.exit(-1);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(new JDialog(), "Error while copying the missing communication library:" + e.getMessage() + "\nYou have two options now.\n1.Run the application as administrator by righ clicking the icon in desktop to copy the required file automatically OR\n2.Note down the file mentioned below and manually copy it to the directory \"" + javaDir + "\\bin\"\n" + libFile, "Error", JOptionPane.ERROR_MESSAGE);
				System.exit(-1);
			}
		} catch (Exception e) {
			if(Protocol.equals("RTU")) {
				errMsg = params.getPortName().isEmpty()?"Port not yet configured for this device":"Error while opening connection to port" + e.getMessage()==null?"":":"+e.getMessage();
				throw new Exception(errMsg);
			}else {
				errMsg = ipAddress.isEmpty()?"IP Address not yet configured for this device":"Error while opening connection to IP Connection" + e.getMessage()==null?"":":"+e.getMessage();
				throw new Exception(errMsg);
			}
		}
		
		// prepare transactions
		transCoilRead = new ModbusSerialTransaction(con1);
		transCoilRead.setRetries(1);
		transCoilWrite = new ModbusSerialTransaction(con1);
		transCoilWrite.setRetries(1);
		transInReg = new ModbusSerialTransaction(con1);
		transInReg.setRetries(1);
		transHolRegRead = new ModbusSerialTransaction(con1);
		transHolRegRead.setRetries(1);
		transHolRegWrite = new ModbusSerialTransaction(con1);
		transHolRegWrite.setRetries(1);
		
		// prepare transactions
		iptransCoilRead = new ModbusTCPTransaction(con2);
		iptransCoilRead.setRetries(1);
		iptransCoilWrite = new ModbusTCPTransaction(con2);
		iptransCoilWrite.setRetries(1);
		iptransInReg = new ModbusTCPTransaction(con2);
		iptransInReg.setRetries(1);
		iptransHolRegRead = new ModbusTCPTransaction(con2);
		iptransHolRegRead.setRetries(1);
		iptransHolRegWrite = new ModbusTCPTransaction(con2);
		iptransHolRegWrite.setRetries(1);
		
		// prepare registers
		icoil = new ReadCoilsRequest(0, 1);
		ocoil = new WriteCoilRequest();
		
		
		ireq = new ReadInputRegistersRequest();
		ireq.setWordCount(wCount);
						
		mreq = new ReadMultipleRegistersRequest();
		mreq.setWordCount(wCount);
				
		wreq = new WriteMultipleRegistersRequest();
		
	}
	
	public void close() {
		con1.close();
		con2.close();
	}
	
	/* function to read input coil */
	public synchronized Boolean readCoil(Integer adr, String protocol) throws Exception {
		return readCoil(devId, adr, protocol);
	}
	public synchronized Boolean readCoil(Integer deviceId, Integer adr, String protocol) throws Exception {
		if(protocol.equals("RTU")) {
			if (adr >= 0) {
				icoil.setUnitID(deviceId);
				icoil.setReference(adr);
				icoil.setHeadless();
				transCoilRead.setRequest(icoil);
				
				try {
					transCoilRead.execute();
					return ((ReadCoilsResponse) transCoilRead.getResponse()).getCoilStatus(0);
				} catch (ModbusException e) {
					errMsg = "Error while executing transaction:" + transCoilRead.getRequest().getHexMessage() + " in port:" + params.getPortName() + ":" + e.getMessage();
					throw new Exception(errMsg);
				}
			} else {
				return false;
			}
		}else {
			if (adr >= 0) {
				icoil.setUnitID(deviceId);
				icoil.setReference(adr);
				iptransCoilRead.setRequest(icoil);
				
				try {
					iptransCoilRead.execute();
					return ((ReadCoilsResponse) iptransCoilRead.getResponse()).getCoilStatus(0);
				} catch (ModbusException e) {
					errMsg = "Error while executing transaction:" + iptransCoilRead.getRequest().getHexMessage() + " in IP Address:" + ipaddr + ":" + e.getMessage();
					throw new Exception(errMsg);
				}
			} else {
				return false;
			}
		}
	}
	
	/* function to write output coil */
	public synchronized void writeCoil(Integer adr, Boolean val, String protocol) throws Exception {
		writeCoil(devId, adr, val, protocol);
	}
	public synchronized void writeCoil(Integer deviceId, Integer adr, Boolean val, String protocol) throws Exception {
		if(protocol.equals("RTU")) {
			if (adr >= 0) {
				ocoil.setUnitID(deviceId);
				ocoil.setReference(adr);
				ocoil.setCoil(val);
				ocoil.setHeadless();
				transCoilWrite.setRequest(ocoil);
				
				try {
					transCoilWrite.execute();
				} catch (ModbusException e) {
					errMsg = "Error while executing transaction:" + transCoilWrite.getRequest().getHexMessage() + " in port:" + params.getPortName() + ":" + e.getMessage();
					throw new Exception(errMsg);
				}
			}
		}else {
			if (adr >= 0) {
				ocoil.setUnitID(deviceId);
				ocoil.setReference(adr);
				ocoil.setCoil(val);
				iptransCoilWrite.setRequest(ocoil);
				
				try {
					iptransCoilWrite.execute();
				} catch (ModbusException e) {
					errMsg = "Error while executing transaction:" + iptransCoilWrite.getRequest().getHexMessage() + " in ipAddress:" + ipaddr + ":" + e.getMessage();
					throw new Exception(errMsg);
				}
			}
		}
	}
	
	/* function to read current available data in input registers as singed integer  */
	public synchronized Integer readInputReg(Integer adr, String protocol) throws Exception {
		return readInputReg(devId, adr, protocol);
	}
	
	public synchronized Integer readInputReg(Integer deviceId, Integer adr, String protocol) throws Exception {
		if(protocol.equals("RTU")) {
			if (adr >= 0) {
				ireq.setUnitID(deviceId);
				ireq.setReference(adr);
				ireq.setHeadless();
				transInReg.setRequest(ireq);
				
				try {
					transInReg.execute();
				} catch (ModbusException e) {
					errMsg = "Error while executing transaction:" + transInReg.getRequest().getHexMessage() + " in port:" + params.getPortName() + ":" + e.getMessage();
					throw new Exception(errMsg);
				}
				
				try {
					if (wCount == 1) {
						return ((ReadInputRegistersResponse) transInReg.getResponse()).getRegisterValue(0);
					} else {
						resHexStr = ((ReadInputRegistersResponse) transInReg.getResponse()).getHexMessage().substring(9).replace(" ", "");
						// reverse lsb and msb if required
						if (isLsbFirst) {
							resHexStr = resHexStr.substring(4) + resHexStr.substring(0,4);
						}
						return ((int) Long.parseLong(resHexStr, 16));
					}
				} catch (NumberFormatException e) {
					errMsg = "Error reading response:" + e.getMessage();
					throw new Exception(errMsg);
				}
			} else {
				return 0;
			}
		}else {
			if (adr >= 0) {
				ireq.setUnitID(deviceId);
				ireq.setReference(adr);
				iptransInReg.setRequest(ireq);
				
				try {
					iptransInReg.execute();
				} catch (ModbusException e) {
					errMsg = "Error while executing transaction:" + iptransInReg.getRequest().getHexMessage() + " in IP Address:" + ipaddr + ":" + e.getMessage();
					throw new Exception(errMsg);
				}
				
				try {
					if (wCount == 1) {
						return ((ReadInputRegistersResponse) iptransInReg.getResponse()).getRegisterValue(0);
					} else {
						resHexStr = ((ReadInputRegistersResponse) iptransInReg.getResponse()).getHexMessage().substring(9).replace(" ", "");
						// reverse lsb and msb if required
						if (isLsbFirst) {
							resHexStr = resHexStr.substring(4) + resHexStr.substring(0,4);
						}
						return ((int) Long.parseLong(resHexStr, 16));
					}
				} catch (NumberFormatException e) {
					errMsg = "Error reading response:" + e.getMessage();
					throw new Exception(errMsg);
				}
			} else {
				return 0;
			}
		}
	}
	
	/* function to read current available data in input registers as float  */
	public synchronized Float readInputRegFloat(Integer adr, String protocol) throws Exception {
		return readInputRegFloat(devId, adr, protocol);
	}
	
	public synchronized Float readInputRegFloat(Integer deviceId, Integer adr, String protocol) throws Exception {
		if(protocol.equals("RTU")) {
			if (adr >= 0) {
				ireq.setUnitID(deviceId);
				ireq.setReference(adr);
				ireq.setHeadless();
				transInReg.setRequest(ireq);
				
				try {
					transInReg.execute();
				} catch (ModbusException e) {
					errMsg = "Error while executing transaction:" + transInReg.getRequest().getHexMessage() + " in port:" + params.getPortName() + ":" + e.getMessage();
					throw new Exception(errMsg);
				}
				
				try {
					resHexStr = ((ReadInputRegistersResponse) transInReg.getResponse()).getHexMessage().substring(9).replace(" ", "");
					// reverse lsb and msb if required
					if (isLsbFirst) {
						resHexStr = resHexStr.substring(4) + resHexStr.substring(0,4); 
					}
					return (Float.intBitsToFloat((int) Long.parseLong(resHexStr, 16))); // convert the 32 bits hex string to IE-754 float
				} catch (NumberFormatException e) {
					errMsg = "Error reading response:" + e.getMessage();
					throw new Exception(errMsg);
				}
			} else {
				return 0F;
			}
		}else {
			if (adr >= 0) {
				ireq.setUnitID(deviceId);
				ireq.setReference(adr);
				iptransInReg.setRequest(ireq);
				
				try {
					iptransInReg.execute();
				} catch (ModbusException e) {
					errMsg = "Error while executing transaction:" + iptransInReg.getRequest().getHexMessage() + " in IP Address:" + ipaddr + ":" + e.getMessage();
					throw new Exception(errMsg);
				}
				
				try {
					resHexStr = ((ReadInputRegistersResponse) iptransInReg.getResponse()).getHexMessage().substring(9).replace(" ", "");
					// reverse lsb and msb if required
					if (isLsbFirst) {
						resHexStr = resHexStr.substring(4) + resHexStr.substring(0,4); 
					}
					return (Float.intBitsToFloat((int) Long.parseLong(resHexStr, 16))); // convert the 32 bits hex string to IE-754 float
				} catch (NumberFormatException e) {
					errMsg = "Error reading response:" + e.getMessage();
					throw new Exception(errMsg);
				}
			} else {
				return 0F;
			}
		}
	}
	
	/* functions to read current available data in holding registers as signed integer */
	public synchronized Integer readHoldingReg(Integer adr, String protocol) throws Exception{
		return readHoldingReg(devId, adr, protocol);
	}

	public synchronized Integer readHoldingReg(Integer deviceId, Integer adr, String protocol) throws Exception {
		if(protocol.equals("RTU")) {
			if (adr >= 0) {
				mreq.setUnitID(devId);
				mreq.setReference(adr);
				mreq.setHeadless();
				transHolRegRead.setRequest(mreq);
				
				try {
					transHolRegRead.execute();
				} catch (ModbusException e) {
					errMsg = "Error while executing transaction:" + transHolRegRead.getRequest().getHexMessage() + " in port:" + params.getPortName() + ":" + e.getMessage();
					throw new Exception(errMsg);
				}
				
				try {
					if (wCount == 1) {
						return ((ReadMultipleRegistersResponse) transHolRegRead.getResponse()).getRegisterValue(0);
					} else {
						resHexStr = ((ReadMultipleRegistersResponse) transHolRegRead.getResponse()).getHexMessage().substring(9).replace(" ", "");
						// reverse lsb and msb if required
						if (isLsbFirst) {
							resHexStr = resHexStr.substring(4) + resHexStr.substring(0,4);
						}
						return ((int) Long.parseLong(resHexStr, 16));
					}
				} catch (NumberFormatException e) {
					errMsg = "Error reading response:" + e.getMessage();
					throw new Exception(errMsg);
				}
			} else {
				return 0;
			}
		}else {
			if (adr >= 0) {
				mreq.setUnitID(devId);
				mreq.setReference(adr);
				iptransHolRegRead.setRequest(mreq);
				
				try {
					iptransHolRegRead.execute();
				} catch (ModbusException e) {
					errMsg = "Error while executing transaction:" + iptransHolRegRead.getRequest().getHexMessage() + " in IP Address:" + ipaddr + ":" + e.getMessage();
					throw new Exception(errMsg);
				}
				
				try {
					if (wCount == 1) {
						return ((ReadMultipleRegistersResponse) iptransHolRegRead.getResponse()).getRegisterValue(0);
					} else {
						resHexStr = ((ReadMultipleRegistersResponse) iptransHolRegRead.getResponse()).getHexMessage().substring(9).replace(" ", "");
						// reverse lsb and msb if required
						if (isLsbFirst) {
							resHexStr = resHexStr.substring(4) + resHexStr.substring(0,4);
						}
						return ((int) Long.parseLong(resHexStr, 16));
					}
				} catch (NumberFormatException e) {
					errMsg = "Error reading response:" + e.getMessage();
					throw new Exception(errMsg);
				}
			} else {
				return 0;
			}
		}
	}
	
	/* function to read current available data in input registers as float  */
	public Float readHoldingRegFloat(Integer adr, String protocol) throws Exception{
		return readHoldingRegFloat(devId, adr, protocol);
	}
	public Float readHoldingRegFloat(Integer deviceId, Integer adr, String protocol) throws Exception {
		if(protocol.equals("RTU")) {
			if (adr >= 0) {
				mreq.setUnitID(deviceId);
				mreq.setReference(adr);
				mreq.setHeadless();
				transHolRegRead.setRequest(mreq);
				try {
					transHolRegRead.execute();
				} catch (ModbusException e) {
					errMsg = "Error while executing transaction:" + transHolRegRead.getRequest().getHexMessage() + " in port:" + params.getPortName() + ":" + e.getMessage();
					throw new Exception(errMsg);
				}
				
				try {
					resHexStr = ((ReadMultipleRegistersResponse) transHolRegRead.getResponse()).getHexMessage().substring(9).replace(" ", "");
					// reverse lsb and msb if required
					if (isLsbFirst) {
						resHexStr = resHexStr.substring(4) + resHexStr.substring(0,4);
					}
					return (Float.intBitsToFloat((int) Long.parseLong(resHexStr, 16))); // convert the 32 bits hex string to IE-754 float
				} catch (NumberFormatException e) {
					errMsg = "Error reading response:" + e.getMessage();
					throw new Exception(errMsg);
				}
			} else {
				return 0F;
			}
		}else {
			if (adr >= 0) {
				mreq.setUnitID(deviceId);
				mreq.setReference(adr);
				iptransHolRegRead.setRequest(mreq);
				try {
					iptransHolRegRead.execute();
				} catch (ModbusException e) {
					errMsg = "Error while executing transaction:" + iptransHolRegRead.getRequest().getHexMessage() + " in IP Address:" + ipaddr + ":" + e.getMessage();
					throw new Exception(errMsg);
				}
				
				try {
					resHexStr = ((ReadMultipleRegistersResponse) iptransHolRegRead.getResponse()).getHexMessage().substring(9).replace(" ", "");
					// reverse lsb and msb if required
					if (isLsbFirst) {
						resHexStr = resHexStr.substring(4) + resHexStr.substring(0,4);
					}
					return (Float.intBitsToFloat((int) Long.parseLong(resHexStr, 16))); // convert the 32 bits hex string to IE-754 float
				} catch (NumberFormatException e) {
					errMsg = "Error reading response:" + e.getMessage();
					throw new Exception(errMsg);
				}
			} else {
				return 0F;
			}
		}
		
	}
	
	/* function to write into a holding register */
	public synchronized void writeHoldingReg(Integer adr, Integer val, String protocol) throws Exception {
		writeHoldingReg(devId, adr, val, protocol);
	}
	public synchronized void writeHoldingReg(Integer deviceId, Integer adr, Integer val, String protocol) throws Exception {
		if(protocol.equals("RTU")) {
			if (adr >= 0) {
				// prepare request for the address to be written and write the data
				Register r[] = {null,null};
				r[0] = new SimpleRegister(val); // msb
				r[1] = new SimpleRegister(0);// lsb
		
				wreq.setUnitID(deviceId);
				wreq.setReference(adr);
				wreq.setRegisters(r);
				wreq.setHeadless();
				
				transHolRegWrite.setRequest(wreq);
				try {
					transHolRegWrite.execute();
				} catch (ModbusException e) {
					errMsg = "Error while executing transaction:" + transHolRegWrite.getRequest().getHexMessage() + " in port:" + params.getPortName() + ":" + e.getMessage();
					throw new Exception(errMsg);
				}
			}
		}else {
			if (adr >= 0) {
				// prepare request for the address to be written and write the data
				Register r[] = {null,null};
				r[0] = new SimpleRegister(val); // msb
				r[1] = new SimpleRegister(0);// lsb
		
				wreq.setUnitID(deviceId);
				wreq.setReference(adr);
				wreq.setRegisters(r);
				
				iptransHolRegWrite.setRequest(wreq);
				try {
					iptransHolRegWrite.execute();
				} catch (ModbusException e) {
					errMsg = "Error while executing transaction:" + iptransHolRegWrite.getRequest().getHexMessage() + " in IP Address" + ipaddr + ":" + e.getMessage();
					throw new Exception(errMsg);
				}
			}
		}
	}
}
