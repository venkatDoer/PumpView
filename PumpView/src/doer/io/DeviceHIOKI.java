package doer.io;

/* TCP Communication for HIOKI Power Meter */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

public class DeviceHIOKI {

	// device changeable params
	private String Protocol = "TCP";
	private String ipaddr = null;
	private Integer ipport = 3300;
	
	// communication
	Socket socket = null;
	PrintWriter request = null;
	BufferedReader responce = null;
	
	// others
	private String command = null;
	private String devReading = null;
	private String errMsg = null;
	
	private static HashMap<String, DeviceHIOKI> devRdrList = new HashMap<String, DeviceHIOKI>();
	
	/* function to initialize the device */
	public static DeviceHIOKI getInstance (String ipAddress, Integer ipPort, String protocol, Boolean isCommonPort) throws Exception {
		
		if (devRdrList.containsKey(ipAddress) && isCommonPort) {
			return devRdrList.get(ipAddress);
		} else {
			DeviceHIOKI tmpRdr = new DeviceHIOKI(ipAddress, ipPort, protocol);
			devRdrList.put(ipAddress, tmpRdr);
			return tmpRdr;
		}
	}

	public DeviceHIOKI (String ipAddress, Integer ipPort,String protocol) throws Exception {
		Protocol = protocol;
		if(Protocol.equals("TCP")) {
			initialize(ipAddress, ipPort);
		}
	}
	public void initialize(String ipAddress, Integer ipPort) throws Exception {
		ipaddr = ipAddress;
		ipport = ipPort;
		try {
			socket = new Socket(ipaddr, ipport);
		} catch (IOException e) {
			errMsg = ipAddress.isEmpty()?"IP Address not yet configured for this device":"Error while opening connection to IP Connection" + e.getMessage()==null?"":":"+e.getMessage();
			e.printStackTrace();
			throw new Exception(errMsg);
		}
		
		// Output stream to send data to the device
        request = new PrintWriter(socket.getOutputStream(), true);

        // Input stream to receive data from the device
        responce = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}
	
	public void close() throws IOException {
		// Close the socket
        socket.close();
	}
	
	public String readValue(String command) throws IOException {
		request.println(command);

        // Receive data from the device
        return responce.readLine();
	}
}
