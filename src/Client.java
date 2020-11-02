import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

class Client {
		public static void main (String[] args) throws Exception {
				System.out.println("Java socket programming assignment is inititated..."); // just to get it going. Treat light weights as heavy and then the heavy will become light.

				if (args.length == 2) {
					System.out.println("This is the first arguement " + args[0] + " " + "and this is the second arguement " + args[1]); 
				}

				
				// INIT 
				// TODO create a script that is scraping from ithaki website the request code and the ports.
				// DatagramSocket sendSocket = new DatagramSocket();
				//byte[] hostIP = { (byte)155, (byte)207,  (byte)18, (byte)208};
				//InetAddress hostAddress = InetAddress.getByAddress(hostIP);
				DatagramSocket sendSocket = new DatagramSocket(48013); // bypassing port forwarding. Binding the sendSocket to a specified socket
				String requestCode ="E0984";
				byte[] txbuffer = requestCode.getBytes();
				int serverPort = 38013;
				byte[] hostIP = { (byte)155, (byte)207,  (byte)18, (byte)208};
				InetAddress hostAddress = InetAddress.getByAddress(hostIP);
				// alternative InetAddress hostAddress = InetAddress.getByName("ithaki.eng.auth.gr") (doesn't worked)
				DatagramPacket sendPacket = new DatagramPacket(txbuffer, txbuffer.length, hostAddress, serverPort);

				//int clientPort = 48013;
				//DatagramSocket receiveSocket = new DatagramSocket(clientPort);
				//receiveSocket.setSoTimeout(3000);
				sendSocket.setSoTimeout(3000);
				byte[] rxbuffer = new byte[2048];
				DatagramPacket receivePacket= new DatagramPacket(rxbuffer, rxbuffer.length);

				System.out.println("Echo application bypassing port forwarding...");
				byte[] data = new byte[1024]; 
				for(int i = 0; i<=4 ; i++) {
					// ACTION
					sendSocket.send(sendPacket);
					System.out.println("The port that I opened to talk to ithaki is: " + sendSocket.getLocalPort() + " and my address is: " + sendSocket.getLocalAddress());
					// LISTEN
					sendSocket.receive(receivePacket);
					System.out.println("The port that opened ithaki to send the request is :" + receivePacket.getPort() + " and the address of ithaki is: " + receivePacket.getAddress());
					data = receivePacket.getData();
					String message0 = new String(data, StandardCharsets.US_ASCII); // convert binary to ASCI
					System.out.println("Ithaki responded with: " + message0);
				}

				System.out.println("Trying to talk to ithaki to the port that is opened from the previous talk..");
				serverPort = 58013;
				sendPacket.setPort(serverPort);
				sendSocket.send(sendPacket);
				// where should I listen? sendSocket.set listening port
				sendSocket.receive(receivePacket);
				data = receivePacket.getData();
				String message0 = new String(data, StandardCharsets.US_ASCII); // convert binary to ASCI
				System.out.println("Ithaki responded with: " + message0);
		}
}
