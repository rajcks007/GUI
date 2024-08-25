import com.fazecast.jSerialComm.SerialPort;

public class SerialNetwork {
	
	static final int nPortsMax = 10;
	static public boolean isConnected = false;
	static SerialPort mPort;
	static private String mPortName = null;
	
	//public static final byte pType_consoleText = 0x33;
	public static final byte[] error = {(byte) 0xff};
		
	// function to get available serial ports and return the names
	static String[] getCommPorts() {
		SerialPort commPorts[] = SerialPort.getCommPorts();
		int tPorts = commPorts.length;
		String[] portNames = new String[tPorts];
		for (int i=0; i< tPorts; i++ ) {
			portNames[i] = commPorts[i].getSystemPortName();
		}
		return portNames;
	}
	
	//function to connect a given port to GUI 
	static boolean connectPort(String portName) {
		try {
			mPort = SerialPort.getCommPort(portName);
			mPort.setComPortParameters(115200, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY, false);
        	if (mPort.openPort() == false) {
        		if(DisplayFrame.DEBUG){
        		System.out.printf("*** error open %s\n", portName);
        		}
        		return false;
        	}
        	else {
        	     isConnected = true;
                 mPortName = portName;
        	}
        } catch (Exception ex) {
        	System.out.println(ex.toString());
        
		}
		return isConnected;
	}

	//function to disconnect the port
	static void disconnectPort() {
		if (isConnected == true) {
			mPort.closePort();
			isConnected = false;
			mPortName = null;
		}
	}
	
	//function to return the name of connected port
	static String getConnectionName() {
		return mPortName;
	}
	
	// Consol Print
	static String ReadString() {
		String recv_str;
		byte[] c_arr;
		int txsize;
		
		recv_str = null;
		
			try {
				if ((txsize = mPort.bytesAvailable())>0) {
					c_arr = new byte[txsize];
					mPort.readBytes(c_arr, txsize);
					recv_str = new String(c_arr);
					System.out.printf("\nReadString: %s",recv_str);
						
				}

			}
				catch(Exception ex){
					System.out.println(ex.toString());
				}
		
		return recv_str;
	}
	
	static byte[] recvSerial() {
			if(!isConnected) {
			return error;
		}

		long bytesToRead = 2;
		byte[] temp = new byte[1];
		
		
		mPort.readBytes(temp,bytesToRead); 
		byte[] data = new byte[(int) bytesToRead];
		
		mPort.readBytes(data, bytesToRead);	 
				 System.out.printf("data:");
		
		return data;
	}
	
	
}
