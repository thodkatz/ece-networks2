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
				byte[] clientIP = { (byte)192, (byte)168,  (byte)1, (byte)20};
				InetAddress clientAddress = InetAddress.getByAddress(clientIP);
				DatagramSocket sendSocket = new DatagramSocket(48010, clientAddress); 
				String requestCode = "E8461";
				byte[] txbuffer = requestCode.getBytes();
				int serverPort = 38010;
				byte[] hostIP = { (byte)155, (byte)207,  (byte)18, (byte)208};
				InetAddress hostAddress = InetAddress.getByAddress(hostIP);
				DatagramPacket sendPacket = new DatagramPacket(txbuffer, txbuffer.length, hostAddress, serverPort);
				sendSocket.setSoTimeout(6000);
				byte[] rxbuffer = new byte[128];
				DatagramPacket receivePacket= new DatagramPacket(rxbuffer, rxbuffer.length);

				System.out.println("Echo application...\n");
				byte[] data = new byte[1024]; 
				for(int i = 0; i<=4 ; i++) {
					// ACTION
					sendSocket.send(sendPacket);
					System.out.println("The request code is: "+ requestCode + "\nThe destination port is: " + serverPort + "\nThe port that I opened to listen from ithaki is: " + sendSocket.getLocalPort() + "\nMy local address is: " + sendSocket.getLocalAddress());

					requestCode = "E0000"; // disable server lag to respond
					txbuffer = requestCode.getBytes();
					sendPacket.setData(txbuffer, 0, txbuffer.length);

					long timeBefore = System.currentTimeMillis();
					System.out.println("My system time, when the request is sent, is: " + timeBefore);
					// LISTEN
					sendSocket.receive(receivePacket);
					long timeAfter = System.currentTimeMillis();
					System.out.println("My system time, when the response received, is: " + timeAfter + " . So the time required to reveive a packet is: " + (timeAfter - timeBefore)/(float)1000 + " seconds"); 
					//System.out.println("The port that opened ithaki to send the request is : " + receivePacket.getPort() + " and the address of ithaki is: " + receivePacket.getAddress());
					data = receivePacket.getData();
					String message0 = new String(data, StandardCharsets.US_ASCII); // convert binary to ASCI
					System.out.println("Ithaki responded with: " + message0);
				}


				System.out.println("\nTemperature measurements...");
				requestCode = "E8461T00";
				txbuffer = requestCode.getBytes();
				sendPacket.setData(txbuffer, 0, txbuffer.length);
				for(int i = 0; i<=4 ; i++) {
					// ACTION
					sendSocket.send(sendPacket);
					//System.out.println("The port that I opened to talk to ithaki is: " + sendSocket.getLocalPort() + " and my local address is: " + sendSocket.getLocalAddress());
					long timeBefore = System.currentTimeMillis();
					System.out.println("My system time, when the request is sent, is: " + timeBefore);
					// LISTEN
					sendSocket.receive(receivePacket);
					long timeAfter = System.currentTimeMillis();
					System.out.println("My system time, when the response received, is: " + timeAfter + " . So the time required to reveive a packet is: " + (timeAfter - timeBefore)/(float)1000 + " seconds"); 
					//System.out.println("The port that opened ithaki to send the request is : " + receivePacket.getPort() + " and the address of ithaki is: " + receivePacket.getAddress());
					data = receivePacket.getData();
					String message0 = new String(data, StandardCharsets.US_ASCII); // convert binary to ASCI
					System.out.println("Ithaki responded with: " + message0);
				}			


				System.out.println("\nImage application...");
				requestCode = "M1501";
				txbuffer = requestCode.getBytes();
				sendPacket.setData(txbuffer, 0, txbuffer.length);
				sendSocket.send(sendPacket);	
				long timeBefore = System.currentTimeMillis();
				System.out.println("My system time, when the request is sent, is: " + timeBefore);
			
				ByteArrayOutputStream dataImage = new ByteArrayOutputStream();
				int countPackets = 0;
				for (;;) {
						try {
							sendSocket.receive(receivePacket);

							long timeAfter = System.currentTimeMillis();
							System.out.println("My system time, when the response received, is: " + timeAfter + " . So the time required to reveive a packet is: " + (timeAfter - timeBefore)/(float)1000 + " seconds"); 
							timeBefore = System.currentTimeMillis();

							System.out.println("Packet No" + countPackets + ". The received bytes in hexadecimal format are:");
							byte[] dataByte = receivePacket.getData();
							for (int i = 0; i<receivePacket.getData().length; i++) {
									String hexa = String.format("%02X", dataByte[i]); // convert bytes to hexa string 
									System.out.print(hexa);
									dataImage.write(dataByte[i]);
									if ((String.format("%02X", dataByte[i]).equals("D9")) && (i!=0)) {
										if ((String.format("%02X", dataByte[i-1]).equals("FF"))) {
											break; // stop writing when EOF
										}
									}
							}
							//dataImage.write(receivePacket.getData()); // This is way more efficient though
							System.out.println();
							countPackets += 1;
						}
						catch (Exception x) {
								System.out.println(x + ". Probably all the requested packets for the image has been sent. Total number of packages: " + (countPackets-1));
								break;
						}
				}	

				byte[] dataImageBytes = dataImage.toByteArray();
				for (byte i : dataImageBytes) {
					String hexa = String.format("%02X", i); // print hexadecimal the content of the byte array
					System.out.print(hexa);
				}

				System.out.println("\nHow many bytes is the image? " + dataImageBytes.length);
				
				File imageFile = new File("ithaki_image.jpg");
				FileOutputStream fos = null;
				try {
						fos = new FileOutputStream(imageFile);
						fos.write(dataImageBytes);
						System.out.println("File has been written successfully");
				}
				catch (Exception x) {
						System.out.println("Image application error when writing the file:" + x);
				}
				finally {
						if (fos != null) {
								fos.close(); // close the OutputStream
						}
				}


				System.out.println("\nAudio application");
				

		}
}
