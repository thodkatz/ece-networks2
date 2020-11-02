import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.lang.System;

class Client {
		public static void main (String[] args) throws Exception {
				System.out.println("Java socket programming assignment is inititated...\n"); // just to get it going. Treat light weights as heavy and then the heavy will become light.

				if (args.length == 2) {
					System.out.println("This is the first arguement " + args[0] + " " + "and this is the second arguement " + args[1]); 
				}

				// INIT 
				// TODO create a script that is scraping from ithaki website the request code and the ports.
				// DatagramSocket sendSocket = new DatagramSocket();
				byte[] clientIP = { (byte)192, (byte)168,  (byte)1, (byte)20};
				InetAddress clientAddress = InetAddress.getByAddress(clientIP);
				DatagramSocket sendSocket = new DatagramSocket(48032, clientAddress); 
				String requestCode ="E7415";
				byte[] txbuffer = requestCode.getBytes();
				int serverPort = 38032;
				byte[] hostIP = { (byte)155, (byte)207,  (byte)18, (byte)208};
				InetAddress hostAddress = InetAddress.getByAddress(hostIP);
				// alternative InetAddress hostAddress = InetAddress.getByName("ithaki.eng.auth.gr") (doesn't worked)
				DatagramPacket sendPacket = new DatagramPacket(txbuffer, txbuffer.length, hostAddress, serverPort);
				
				//int clientPort = 48013;
				//DatagramSocket receiveSocket = new DatagramSocket(clientPort);
				//receiveSocket.setSoTimeout(3000);
				sendSocket.setSoTimeout(4000);
				byte[] rxbuffer = new byte[2048];
				DatagramPacket receivePacket= new DatagramPacket(rxbuffer, rxbuffer.length);

				System.out.println("Echo application...\n");
				byte[] data = new byte[1024]; 
				for(int i = 0; i<=4 ; i++) {
					// ACTION
					sendSocket.send(sendPacket);
					System.out.println("The port that I opened to talk to ithaki is: " + sendSocket.getLocalPort() + " and my local address is: " + sendSocket.getLocalAddress());
					long timeBefore = System.currentTimeMillis();
					System.out.println("My system time, when the request is sent,is: " + timeBefore);
					// LISTEN
					sendSocket.receive(receivePacket);
					long timeAfter = System.currentTimeMillis();
					System.out.println("My system time, when the response received, is: " + timeAfter + " . So the time required to reveive a packet is: " + (timeAfter - timeBefore)/(float)1000 + " seconds"); 
					System.out.println("The port that opened ithaki to send the request is : " + receivePacket.getPort() + " and the address of ithaki is: " + receivePacket.getAddress());
					data = receivePacket.getData();
					String message0 = new String(data, StandardCharsets.US_ASCII); // convert binary to ASCI
					System.out.println("Ithaki responded with: " + message0);
				}


				System.out.println("\nTemperature measurements...");
		}
}
