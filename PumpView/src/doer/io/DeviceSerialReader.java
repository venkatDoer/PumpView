package doer.io;

import java.util.HashMap;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import net.wimpi.modbus.util.SerialParameters;

public class DeviceSerialReader {

	private String portName = "";
	private SerialPort serPort = null;
	private String devReading = "";
	private SerialPortHandler devHlr = null;
	private boolean initialized = false;

	public DeviceSerialReader() {
	}

	private static HashMap<String, DeviceSerialReader> devSetRdrList = new HashMap<String, DeviceSerialReader>();
	
	public static DeviceSerialReader getInstance(SerialParameters params, Boolean isCommonPort) throws Exception {
		if (devSetRdrList.containsKey(params.getPortName()) && isCommonPort) {
			return devSetRdrList.get(params.getPortName());
		} else {
			DeviceSerialReader tmpRdr = new DeviceSerialReader(params);
			devSetRdrList.put(params.getPortName(), tmpRdr);
			return tmpRdr;
		}
	}
	
	public static void clearList() {
		devSetRdrList.clear();
	}
	
	public DeviceSerialReader(SerialParameters params) throws Exception {
		initPort(params.getPortName(), params.getBaudRate(), params.getDatabits(), params.getStopbits(), params.getParity(), params.getFlowControlIn());
	}
	
	public String getDevReading() {
		return devReading;
	}
	
	public boolean isInitialized() {
		return initialized;
	}
	
	public void initPort(String portName) throws Exception {
		initPort(portName, 9600, 8, 1, 0, 0);
	}
	
	public void initPort(String portNm, Integer baudRate, Integer dataBits, Integer stopBits, Integer parity, Integer flowControl) throws Exception {
		this.portName = portNm;
		try {
			/* open the port */ 
			serPort = new SerialPort(portName);
			serPort.openPort();
		} catch (SerialPortException e) {
			throw new Exception((portName.isEmpty()?"Port not yet configured for this device":"Error while opening connection to port" + e.getMessage()==null?"":":"+e.getMessage()));
		}

		// set port parameters
		try {
			serPort.setParams(baudRate, dataBits, stopBits, parity);
			serPort.setFlowControlMode(flowControl);
			// others
			serPort.setEventsMask(SerialPort.MASK_RXCHAR); 
		} catch (SerialPortException e) {
			serPort.closePort();
			throw new Exception("Unable to set port parms for port:" + portName + ":" + e.getMessage());
		}
		
		/* Attach the port to device listener */ 
		try {
			devHlr = new SerialPortHandler();
			serPort.addEventListener(devHlr);
		} catch (SerialPortException e) {
			serPort.closePort();
			e.printStackTrace();
			throw new Exception("Unable to attach handler:" + portName + ":" + e.getMessage());
		}
		
		initialized = true;
	}

	public void closePort() throws Exception {
		try {
			serPort.closePort();
		} catch (SerialPortException e) {
			throw new Exception("Error closing port:" + portName + e.getMessage());
		}
	}
	
	public String writeAndReadData(String inputCmd, String desiredStr) throws Exception {
		try {
			devReading = "";
			// serPort.writeString(inputCmd + "\r\n"); // need this for freenigh flow meter
			serPort.writeString(inputCmd);
			// give enough room to get device handler fired and wait until to get desired reading
			int noOfTries = 0;
			do {
				Thread.sleep(150);
				++noOfTries;
				System.out.println("Available reading:" + devReading);
			} while (noOfTries <= 3 && !devReading.contains(desiredStr));
		} catch (Exception e) {
			throw new Exception("Error reading data from port:" + portName + ":" + e.getMessage());
		}
		return devReading;
	}
	
	/* public String writeAndReadData(String inputCmd, String desiredStr) throws Exception {
		return writeAndReadData(inputCmd, desiredStr, 0);
	}

	public String writeAndReadData(String inputCmd, String desiredStr, Integer noOfWords) throws Exception {
		try {
			devReading = "";
			serPort.writeString(inputCmd);
			// give enough room to get device handler fired and wait until to get desired reading
			int noOfTries = 0;
			do {
				Thread.sleep(150);
				++noOfTries;
			} while (noOfTries <= 5 && ((!desiredStr.isEmpty() && !devReading.contains(desiredStr)) || (noOfWords > 0 && devReading.split(" ").length != noOfWords)));
		} catch (Exception e) {
			throw new Exception("Error reading data from port:" + portName + ":" + e.getMessage());
		}
		return devReading;
	} */
	
	/* serial port data event handler */
	private class SerialPortHandler implements SerialPortEventListener {
		@Override
		public void serialEvent(SerialPortEvent event) {
			// read data if available
			if (event.isRXCHAR() && event.getEventValue() > 0) {
					try {
						devReading += serPort.readString(event.getEventValue());
					} catch (SerialPortException e) {
						JOptionPane.showMessageDialog(new JDialog(), "Error reading data from port:" + portName + e.getMessage());
					}
			}
		}
	} // inner class
}



    


    

