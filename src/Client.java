import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.lang.System;
import java.awt.Desktop;
import javax.sound.sampled.*;

class Client {
		public static void main (String[] args) throws Exception {
				System.out.println("Java socket programming assignment is inititated...\n"); // just to get it going. Treat light weights as heavy and then the heavy will become light.

				if (args.length == 2) {
					System.out.println("This is the first arguement " + args[0] + " " + "and this is the second arguement " + args[1]); 
				}
				String[] directionOptions = {"L", "D", "U", "R"};
				int flag = 0;
				for (String i : directionOptions) {
					if (i.equals(args[0])) {
						flag = 1;
						System.out.println("Valid direction: " + args[0]);
						break;
					}	
				}
				if (flag == 0) {
					System.out.println("Try again, wrong direction. Available options are: L, R, U, D");
					return;
				}
				if (Integer.parseInt(args[1])>=1 && Integer.parseInt(args[1])<=100) {
					System.out.println("How many times? " + Integer.parseInt(args[1]) + ". Valid option");
				}
				else {
					System.out.println("Invalid input. Range should be 1-100");
					return;
				}

				// INIT 
				// TODO create a script that is scraping from ithaki website the request code and the ports.
				byte[] clientIP = { (byte)192, (byte)168,  (byte)1, (byte)20};
				InetAddress clientAddress = InetAddress.getByAddress(clientIP);
				DatagramSocket sendSocket = new DatagramSocket(48006, clientAddress); 
				String requestCode = "E8101";
				byte[] txbuffer = requestCode.getBytes();
				int serverPort = 38006;
				byte[] hostIP = { (byte)155, (byte)207,  (byte)18, (byte)208};
				InetAddress hostAddress = InetAddress.getByAddress(hostIP);
				DatagramPacket sendPacket = new DatagramPacket(txbuffer, txbuffer.length, hostAddress, serverPort);
				sendSocket.setSoTimeout(3000);
				byte[] rxbuffer = new byte[1024];
				DatagramPacket receivePacket= new DatagramPacket(rxbuffer, rxbuffer.length);

				System.out.println("\n--------------------Echo application--------------------");
				byte[] data = new byte[1024]; 
				for(int i = 0; i<=4 ; i++) {
					try {
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
					catch (Exception x) {
						System.out.println(x);
						break;
					}
				}


				System.out.println("\n--------------------Temperature measurements--------------------");
				requestCode = "E8101T00";
				txbuffer = requestCode.getBytes();
				sendPacket.setData(txbuffer, 0, txbuffer.length);
				for(int i = 0; i<=4 ; i++) {
					try {
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
					catch (Exception x) {
						System.out.println(x);
						break;
					}
				}			

				//String[] imageRequestOptions = {"CAM=FIX", "CAM=PTZ", "CAM=PTZDIR=L", "CAM=PTZDIR=U", "CAM=PTZDIR=D", "CAM=PTZDIR=R"};
				//String[] directionOptions = {"R", "R", "L", "L", "L", "L", "R", "R","U", "U","D", "D", "D","D","U", "U"};
				//int numImage = 0;
				//for (String dir : directionOptions) {
				//	numImage++;
				for(int numImage = 0; numImage<Integer.parseInt(args[1]); numImage++) {

					System.out.println("\n--------------------Image application---------------------");
					requestCode = "M1624CAM=PTZUDP=1024DIR=" + args[0];
					//requestCode = "M4197CCAM=PTZUDP=1024DIR=R";
					System.out.println("The request code is " + requestCode);
					txbuffer = requestCode.getBytes();
					sendPacket.setData(txbuffer, 0, txbuffer.length);
					sendSocket.send(sendPacket);	
					System.out.println("I am sleeping... Camera needs time to readjust");
					Thread.sleep(5000); // sleep in order for the camera to readjust	

					if (numImage != (Integer.parseInt(args[1])-1)){
							continue; // readjust the camera as many time is requested via the command line arguement. Print only the result, the last request
					}

					ByteArrayOutputStream bufferImage = new ByteArrayOutputStream();
					int countPackets = 0;
					long timeBefore = System.currentTimeMillis();
					long timeBeforePerPacket = System.currentTimeMillis();
					//System.out.println("My system time, when the request is sent, is: " + timeBefore);
					outerloop:
					for (;;) {
						try {
							sendSocket.receive(receivePacket);
					
							long timeAfterPerPacket = System.currentTimeMillis();
							System.out.println("The time required to reveive a packet is: " + (timeAfterPerPacket - timeBeforePerPacket)/(float)1000 + " seconds"); 
							timeBeforePerPacket = System.currentTimeMillis();
					
							System.out.println("Packet No" + countPackets + ". The received bytes in hexadecimal format are:");
							byte[] dataImage = receivePacket.getData();
							for (int i = 0; i<receivePacket.getData().length; i++) {
									String hexa = String.format("%02X", dataImage[i]); // convert bytes to hexa string 
									System.out.print(hexa);
									bufferImage.write(dataImage[i]);
									if ((String.format("%02X", dataImage[i]).equals("D9")) && (i!=0)) {
										if ((String.format("%02X", dataImage[i-1]).equals("FF"))) {
											break outerloop; // stop writing when EOF
										}
									}
							}
							//bufferImage.write(receivePacket.getData()); // This is way more efficient though
							System.out.println();
							countPackets++;
						}
						catch (Exception x) {
								System.out.println(x);
								break;
						}
					}	
					long timeAfter = System.currentTimeMillis(); // get the time when the image is received in bytes
					
					System.out.println("Complete byte content of the image file in hexadecimal format:");
					byte[] completeDataImage = bufferImage.toByteArray();
					for (byte i : completeDataImage) {
						String hexa = String.format("%02X", i); // print hexadecimal the content of the byte array
						System.out.print(hexa);
					}

					System.out.println("\n\nTotal number of packages: " + (countPackets-1));
					System.out.println("How many Kbytes is the image? " + completeDataImage.length/(float)1000);
					
					//File imageFile = new File("../media/image/sandbox/ithaki_image_PTZ_No" + numImage + ".jpg");
					File imageFile = new File("../media/image/sandbox/ithaki_image_PTZ.jpg");
					FileOutputStream fos = null;
					try {
							fos = new FileOutputStream(imageFile);
							fos.write(completeDataImage);
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
					System.out.println("Total amount of time to receive a frame: " + (timeAfter - timeBefore)/(float)1000 + " seconds");
					timeAfter = System.currentTimeMillis(); // get the time when the file is ready
					System.out.println("Total amount of time to receive and write a frame in a .jpg file: " + (timeAfter - timeBefore)/(float)1000 + " seconds");
					Desktop desktop = Desktop.getDesktop();
					if (imageFile.exists()) {
						//desktop.open(imageFile);
					}
				}
				


				System.out.println("\n--------------------Audio application--------------------");
				requestCode = "A8002";
				txbuffer = requestCode.getBytes();
				sendPacket.setData(txbuffer, 0, txbuffer.length);
				sendSocket.send(sendPacket);	
				
				ByteArrayOutputStream bufferSound = new ByteArrayOutputStream(); // capture the audio in a buffer way

				int countPackets = 0;
				long timeBefore = System.currentTimeMillis();
				long timeBeforePerPacket = System.currentTimeMillis();
				for(;;) {
						try {
							sendSocket.receive(receivePacket);
	
							long timeAfterPerPacket = System.currentTimeMillis();
							System.out.println("The time required to reveive a packet is: " + (timeAfterPerPacket - timeBeforePerPacket)/(float)1000 + " seconds"); 
							timeBeforePerPacket = System.currentTimeMillis();
					
							System.out.println("Packet No" + countPackets + ". The received bytes in hexadecimal format are:");
							byte[] dataSound  = receivePacket.getData();
							bufferSound.write(dataSound);
						}
						catch (Exception x) {
							System.out.println(x + "or ithaki finished sending audio packets");
							break;
						}
						System.out.println();
						countPackets++;
				}

				System.out.println("Complete byte content of the image file in hexadecimal format:");
				byte[] completeDataSound = bufferSound.toByteArray();
				for (byte i : completeDataSound) {
					String hexa = String.format("%02X", i); // print hexadecimal the content of the byte array
					System.out.print(hexa);
				}

				try {
					AudioFormat modulationPCM = new AudioFormat(8000, 16, 1, true, false);
					SourceDataLine outputAudio = AudioSystem.getSourceDataLine(modulationPCM);
					//outputAudio.open(modulationPCM, 3200);
					outputAudio.open(modulationPCM);
					outputAudio.start();

					outputAudio.write(completeDataSound, 0, completeDataSound.length);
					outputAudio.stop();
					outputAudio.close();
					System.out.println("Sound application success!");

				}
				catch (Exception x) {
					System.out.println(x + ". Sound playing failed");
				}


				System.out.println("\n--------------------END OF JAVA APPLICATION--------------------");
		}
}
