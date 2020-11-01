import java.io.*;
import java.net.*;

class Client {
		public static void main (String[] args) throws Exception {
				System.out.println("Hello world"); // just to get it going. Treat light weights as heavy and then the heavy will become light.

				if (args.length == 2) {
					System.out.println("This is the first arguement " + args[0] + " " + "and this is the second arguement " + args[1]); // just to get it going. Treat light weights as heavy and then the heavy will become light.
				}

				DatagramSocket s = new DatagramSocket();
				String packetData ="E4653";
				byte[] txbuffer = packetData.getBytes();
				int serverPort = 38026;
				byte[] hostIP = { (byte)155, (byte)207,  (byte)18, (byte)208};
				InetAddress hostAddress = InetAddress.getByAddress(hostIP);
				DatagramPacket p = new DatagramPacket(txbuffer, txbuffer.length, hostAddress, serverPort);
				//s.send(p);


				int clientPort = 48026;
				DatagramSocket r = new DatagramSocket(clientPort);
				r.setSoTimeout(2000);
				byte[] rxbuffer = new byte[2048];
				DatagramPacket q = new DatagramPacket(rxbuffer, rxbuffer.length);

				for(int c = 0; c<5 ; c++) {
						s.send(p);
						r.receive(q);
				}


		}
}
