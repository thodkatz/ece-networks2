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
				DatagramSocket sendSocket = new DatagramSocket(48008); // bypassing port forwarding
				String requestCode ="E6470";
				byte[] txbuffer = requestCode.getBytes();
				int serverPort = 38008;
				byte[] hostIP = { (byte)155, (byte)207,  (byte)18, (byte)208};
				InetAddress hostAddress = InetAddress.getByAddress(hostIP);
				DatagramPacket sendPacket = new DatagramPacket(txbuffer, txbuffer.length, hostAddress, serverPort);

				int clientPort = 48008;
				DatagramSocket receiveSocket = new DatagramSocket(clientPort);
				receiveSocket.setSoTimeout(3000);
				byte[] rxbuffer = new byte[2048];
				DatagramPacket receivePacket= new DatagramPacket(rxbuffer, rxbuffer.length);

				System.out.println("Echo application...");
				byte[] data = new byte[1024]; 
				for(int i = 0; i<=4 ; i++) {
					// ACTION
					sendSocket.send(sendPacket);
					// LISTEN
					receiveSocket.receive(receivePacket);
					data = receivePacket.getData();
					String message0 = new String(data, StandardCharsets.US_ASCII); // convert binary to ASCI
					System.out.println("Ithaki responded with: " + message0);
				}

				// bypassing the port forwarding. But how? We know that ithaki send packets to a specific port. How ithaki can respond to the port automatically opens when sending packet.
				System.out.println("Echo application but without port forwarding");
				sendSocket.setSoTimeout(3000);
				for(int i = 0; i<=4 ; i++) {
					// ACTION
					sendSocket.send(sendPacket);
					data = sendPacket.getData();
					String message1 = new String(data, StandardCharsets.US_ASCII);
					System.out.println("The info of the send packet is: " + message1);
					// LISTEN
					sendSocket.receive(receivePacket);
					data = receivePacket.getData();
					String message2 = new String(data, StandardCharsets.US_ASCII); // convert binary to ASCI
					System.out.println("Ithaki responded with: " + message2);
				}
		}
}
