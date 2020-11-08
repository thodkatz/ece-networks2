import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.lang.System;
import java.awt.Desktop;
import javax.sound.sampled.*;
import java.lang.Math.*;

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
        DatagramSocket sendSocket = new DatagramSocket(48044, clientAddress); 
        String requestCode = "E1323";
        byte[] txbuffer = requestCode.getBytes();
        int serverPort = 38044;
        byte[] hostIP = { (byte)155, (byte)207,  (byte)18, (byte)208};
        InetAddress hostAddress = InetAddress.getByAddress(hostIP);
        DatagramPacket sendPacket = new DatagramPacket(txbuffer, txbuffer.length, hostAddress, serverPort);
        sendSocket.setSoTimeout(2500);
        byte[] rxbuffer = new byte[1024];
        DatagramPacket receivePacket= new DatagramPacket(rxbuffer, rxbuffer.length);

        System.out.println("\n--------------------Echo application--------------------");
        for(int i = 0; i<=4 ; i++) {
            byte[] data = new byte[1024]; 
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
        requestCode = "E1323T00";
        txbuffer = requestCode.getBytes();
        sendPacket.setData(txbuffer, 0, txbuffer.length);
        for(int i = 0; i<=4 ; i++) {
            byte[] data = new byte[1024]; 
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
            //requestCode = "M9621CAM=PTZUDP=1024DIR=" + args[0];
            requestCode = "M2893UDP=1024";
            //requestCode = "M2973CAM=PTZUDP=1024";
            System.out.println("The request code is " + requestCode);
            txbuffer = requestCode.getBytes();
            sendPacket.setData(txbuffer, 0, txbuffer.length);
            sendSocket.send(sendPacket);	
            System.out.println("I am sleeping... Camera needs time to readjust");
            //Thread.sleep(5000); // sleep in order for the camera to readjust	

            //if (numImage != (Integer.parseInt(args[1])-1)){
            //    continue; // readjust the camera as many time is requested via the command line arguement. Print only the result, the last request
            //}

            ByteArrayOutputStream bufferImage = new ByteArrayOutputStream();
            int countPackets = 0;
            long timeBefore = System.currentTimeMillis();
            long timeBeforePerPacket = System.currentTimeMillis();
            //System.out.println("My system time, when the request is sent, is: " + timeBefore);
outerloop:
            for (;;) {
                try {
                    sendSocket.receive(receivePacket);
                    countPackets++;

                    long timeAfterPerPacket = System.currentTimeMillis();
                    System.out.println("The time required to reveive a packet is: " + (timeAfterPerPacket - timeBeforePerPacket)/(float)1000 + " seconds"); 
                    timeBeforePerPacket = System.currentTimeMillis();

                    byte[] dataImage = receivePacket.getData();
                    System.out.println("Packet No" + countPackets + ". Length of data: " + dataImage.length + ". The received bytes in hexadecimal format are:");
                    for (int i = 0; i<dataImage.length; i++) {
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
                }
                catch (Exception x) {
                    System.out.println(x);
                    break;
                }
            }	
            long timeAfter = System.currentTimeMillis(); // get the time when the image is received in bytes

            System.out.println("\nComplete byte content of the image file in hexadecimal format:");
            byte[] completeDataImage = bufferImage.toByteArray();
            for (byte i : completeDataImage) {
                String hexa = String.format("%02X", i); // print hexadecimal the content of the byte array
                System.out.print(hexa);
            }

            System.out.println("\n\nTotal number of packages: " + (countPackets));
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
        String numAudioPackets = "100";
        String typeModulation = "AQ"; // TODO: command line arguement
        //String typeModulation = ""; 
        requestCode = "A6381" + typeModulation + "F" + numAudioPackets;
        txbuffer = requestCode.getBytes();
        sendPacket.setData(txbuffer, 0, txbuffer.length);
        sendSocket.send(sendPacket);

        int bufferLength = 0;
        if (typeModulation.equals("")) {
            bufferLength = 128;
        }
        else if (typeModulation.equals("AQ")) {
            bufferLength = 132;
        }
        else {
            System.out.println("Invalid request code regarding audio application");
        }

        byte[] rxbufferSound = new byte[bufferLength];
        receivePacket.setData(rxbufferSound, 0, rxbufferSound.length);

        ByteArrayOutputStream bufferSound = new ByteArrayOutputStream(); // capture the audio in a buffer way (elastic buffer)
        int countPackets = 0; // total amount of packets received
        int packetsSize = 0; // total amount of bytes received
        long timeBefore = System.currentTimeMillis();
        long timeBeforePerPacket = System.currentTimeMillis();
        for(int k = 0; k<Integer.parseInt(numAudioPackets); k++) {
            try {
                sendSocket.receive(receivePacket);
                countPackets++;

                long timeAfterPerPacket = System.currentTimeMillis();
                System.out.println("The time required to reveive a packet is: " + (timeAfterPerPacket - timeBeforePerPacket)/(float)1000 + " seconds"); 
                timeBeforePerPacket = System.currentTimeMillis();

                byte[] dataSound  = receivePacket.getData();
                packetsSize += dataSound.length;
                System.out.println("Packet No" + countPackets + ". Length of data: " + dataSound.length);

                int init = 0;
                int coeffDPCM = 2; // Trials: for 100 is pure noise, 4 good, 10 bad. I think below 4 you are good 
                if (typeModulation.equals("")) {
                    // DPCM
                    for (int i = 0; i<dataSound.length; i++) {

                        String hexa = String.format("%02X", dataSound[i]); // print hexadecimal the content of the byte array
                        System.out.print("Input: decimal: " + dataSound[i] + ", unsigned: " + Byte.toUnsignedInt(dataSound[i])  + " and the hexa: " + hexa + ", ");
                        // get nibbles
                        int maskLow = 0x0F;
                        int maskHigh = 0xF0;
                        int nibbleLow = dataSound[i] & maskLow; // D[i] = x[i] - x[i-1]
                        int nibbleHigh = (dataSound[i] & maskHigh)>>4; // D[i-1] = x[i-1] - x[i-2]
                       
                        // get samples
                        int sampleFirst = init + (nibbleHigh - 8)*coeffDPCM;
                        int sampleSecond = sampleFirst + (nibbleLow - 8)*coeffDPCM;
                        System.out.print("Masks high and low: " + maskHigh + ", " + maskLow + ". Masks in hex: " + String.format("%02X", maskHigh) +", " + String.format("%02X", maskLow) + ". Result of mask: " + String.format("%02X", nibbleHigh) + ", " + String.format("%02X", nibbleLow) + ". Nibbles high and low: " + nibbleHigh + ", " + nibbleLow + ", so the actual differences are: " + (nibbleHigh-8) +", " + (nibbleLow-8) + " and samples: " + sampleFirst + ", " + sampleSecond);
                        init = sampleSecond;

                        // check range
                        int max8 = (int)(Math.pow(2,7)) - 1;
                        int min8 = -(int)(Math.pow(2,7));
                        int[] samples = {sampleFirst, sampleSecond};
                        for (int j=0; j< samples.length; j++) {
                            if (samples[j]>max8) samples[j] = max8;
                            else if (samples[j]<min8) samples[j] = min8;
                        }               
                        
                        // write to buffer
                        byte[] decodedSound = new byte[2];
                        decodedSound[0] = (byte)sampleFirst;
                        decodedSound[1] = (byte)sampleSecond;
                        System.out.println(". Output: " + String.format("%02X", decodedSound[0]) + String.format("%02X", decodedSound[1]));
                        bufferSound.write(decodedSound);

                    }
                }
                else if (typeModulation.equals("AQ")) {
                    // AQ-DPCM

                    // get the header first
                    int mean = (Byte.toUnsignedInt(dataSound[1])<<8 | Byte.toUnsignedInt(dataSound[0])); // be sure to not preserve the byte sign
                    int meanSigned = (dataSound[1]<<8 | dataSound[0]); // this is wrong. Not sure though?
                    System.out.println("dataSound[1]: " + String.format("%02X", dataSound[1]) + ", dataSound[1]<<8: " + String.format("%02X", (Byte.toUnsignedInt(dataSound[1]))<<8));
                    System.out.println("The MSB of mean is " + String.format("%02X", dataSound[1]) + " and the LSB of the mean is "+ String.format("%02X", dataSound[0]) + ". The mean is " + mean + " and signed " + meanSigned + " and in hex unsigned: " + String.format("%02X", mean) + " and signed " + String.format("%02X", meanSigned));
                    int step = (Byte.toUnsignedInt(dataSound[3])<<8 | Byte.toUnsignedInt(dataSound[2]));
                    System.out.println("The MSB of step is " + String.format("%02X", dataSound[3]) + " and the LSB of the step is " + String.format("%02X", dataSound[2]) + ". The step is " + step + " and in hex: " + String.format("%02X", step));
                    // step should be unsigned? we are safe since it is int and the value is 2 bytes maximum. Int preserves sign
                    
                    //int mean = 256*dataSound[1] + dataSound[0];
                    //int step = 256*dataSound[3] + dataSound[2];
                    //System.out.print((short)(256*Byte.toUnsignedInt(dataSound[1]) + Byte.toUnsignedInt(dataSound[0])));
                    //System.out.println(", "+ (short)((256*Byte.toUnsignedInt(dataSound[3]) + Byte.toUnsignedInt(dataSound[2]))));

                    init = meanSigned; // in DPCM we don't know the init value, we assume zero. But here we have data in the header.
                    for (int i = 3; i<dataSound.length; i++) {
                        // the sample may be bigger than byte. So you will need 16 bit encoding and store each int to 2 bytes.
                        String hexa = String.format("%02X", dataSound[i]); // print hexadecimal the content of the byte array
                        System.out.print("Input: " + hexa + ", ");                       
                        // get nibbles                                                            
                        int maskLow = 0x0F;
                        int maskHigh = 0xF0;
                        int nibbleLow = (dataSound[i] & maskLow); // D[i] = x[i] - x[i-1], should be unsigned
                        int nibbleHigh = (dataSound[i] & maskHigh)>>4; // D[i-1] = x[i-1] - x[i-2], should be unsigned

                        // get samples (implement recursive formula)
                        int sampleFirst = init + step*(nibbleHigh - 8);
                        int sampleSecond = sampleFirst + step*(nibbleLow - 8);
                        System.out.print("Masks high and low: " + maskHigh + ", " + maskLow + ". Masks in hex: " + String.format("%02X", maskHigh) +", " + String.format("%02X", maskLow) + ". Result of mask: " + String.format("%02X", nibbleHigh) + ", " + String.format("%02X", nibbleLow) + ". Nibbles high and low: " + nibbleHigh + ", " + nibbleLow + ", so the actual differences are: " + (nibbleHigh-8)*step +", " + (nibbleLow-8)*step + " and samples: " + sampleFirst + ", " + sampleSecond);
                        init = sampleSecond;

                        // check range
                        int max16 = (int)(Math.pow(2,15)) - 1;
                        int min16 = -(int)(Math.pow(2,15));
                        int[] samples = {sampleFirst, sampleSecond};
                        for (int j=0; j<samples.length; j++) {
                            if (samples[j]>max16) samples[j] = max16;
                            else if (samples[j]<min16)  samples[j] = min16; 
                        }
                        System.out.print(". The actual samples due to 16-bit restriction are: " + samples[0] + " and " + samples[1] + " and in hex format: " + String.format("%02X", samples[0]) + ", " + String.format("%02X", samples[1]) + ". In short " + (short)samples[0] + ", " + (short)samples[1] + " and in hex format as a short: " + String.format("%02X", (short)samples[0]) + ", " + String.format("%02X", (short)samples[1]));

                        // write to buffer
                        byte[] decodedSound = new byte[4];
                        decodedSound[0] = (byte)(samples[0]>>8); // MSB of sample 15-8
                        decodedSound[1] = (byte)samples[0]; // LSB of sample 7-0
                        decodedSound[2] = (byte)(samples[1]>>8);
                        decodedSound[3] = (byte)samples[1];
                        System.out.println(". Output: First sample " + String.format("%02X", decodedSound[0]) + String.format("%02X", decodedSound[1]) + " second sample: " + String.format("%02X", decodedSound[2]) + String.format("%02X", decodedSound[3]));
                        bufferSound.write(decodedSound);
                    }
                }
                else {
                    System.out.println("This is not a valid request code");
                }
                System.out.println();
            }
            catch (Exception x) {
                System.out.println(x + ". Receiving, writing the audio data failed");
                break;
            }
        }

        sendSocket.close();
        long timeAfter = System.currentTimeMillis();

        System.out.println("\nComplete byte content of the sound file in hexadecimal format:");
        byte[] completeDataSound = bufferSound.toByteArray();
        for (byte i : completeDataSound) {
            String hexa = String.format("%02X", i); // print hexadecimal the content of the byte array
            System.out.print(hexa);
        }

        System.out.println("\n\nTotal number of packages: " + (countPackets));
        System.out.println("How many Kbytes is the sound? " + completeDataSound.length/(float)1000 + "\nHow many Kbytes is the data that was actually sent? " + packetsSize/(float)1000);
        System.out.println("Total amount of time to receive sound data: " + (timeAfter - timeBefore)/(float)1000 + " seconds");

        boolean isBigEndian = false; // only in 16 bit samples does matter. In AQ-DPCM we use 16 bit encoding
        int encodingBits = 8;
		if (typeModulation.equals("AQ")) {                                             
            isBigEndian = true;
            encodingBits = 16;
		}
		AudioFormat modulationPCM = new AudioFormat(8000, encodingBits, 1, true, isBigEndian);
		// play sound
		try {
            SourceDataLine outputAudio = AudioSystem.getSourceDataLine(modulationPCM);
            //outputAudio.open(modulationPCM, 3200);
            outputAudio.open(modulationPCM);
            outputAudio.start();

            System.out.println("Getting ready to hear some music?");
            Thread.sleep(2000);
            System.out.print("In 3");
            Thread.sleep(1000);
            System.out.print(", 2");
            Thread.sleep(1000);
            System.out.println(", 1...");
            Thread.sleep(500);
            System.out.println("GOOOOOO");
            Thread.sleep(500);
            outputAudio.write(completeDataSound, 0, completeDataSound.length);
            outputAudio.stop();
            outputAudio.close();
            System.out.println("\nSound application success!");
        }
        catch (Exception x) {
            System.out.println(x + ". Sound playing failed");
        }

        // save music to file
        try{
            ByteArrayInputStream bufferSoundInput = new ByteArrayInputStream(completeDataSound);
            AudioInputStream streamSoundInput = new AudioInputStream(bufferSoundInput, modulationPCM, completeDataSound.length / modulationPCM.getFrameSize());
            AudioSystem.write(streamSoundInput, AudioFileFormat.Type.WAVE, new File("../media/music/sandbox/track.wav"));
            System.out.println("Sound file creation success");
        }
        catch (Exception x) {
            System.out.println(x + ". Sound file creation failed");
        }

        System.out.println("\n--------------------Ithakicopter-UDP--------------------");

        DatagramSocket socketCopter = new DatagramSocket(48048);
        socketCopter.setSoTimeout(2500);

        byte[] rxbufferCopter = new byte[64];
        receivePacket.setData(rxbufferCopter, 0, rxbufferCopter.length);

        for (int i=0; i<4;i++) {
            try {
                socketCopter.receive(receivePacket);
                String message = new String(receivePacket.getData(), StandardCharsets.US_ASCII); // convert binary to ASCI
                System.out.println("Ithaki responded with: " + message);
                socketCopter.close();
            }
            catch (Exception x) {
                System.out.println(x + ". Ithakicopter application failed");
            }
        
        }


        System.out.println("\n--------------------TCP------------------------------");


        try {
            serverPort = 80;
            Socket socketTCP = new Socket(hostAddress, serverPort); // establish connection
            socketTCP.setSoTimeout(3000);
            InputStream in = socketTCP.getInputStream(); // what I receive from the server
            OutputStream out = socketTCP.getOutputStream(); // what i send to the server

            out.write("GET /netlab/hello.html HTTP/1.0\r\nHost: ithaki.eng.auth.gr:80\r\n\r\n".getBytes());
            byte[] inputBuffer = in.readAllBytes();
            String message = new String(inputBuffer, StandardCharsets.US_ASCII);
            System.out.println("Ithaki responded via TCP with: " + message);

            socketTCP.close();
        }
        catch (Exception x) {
            System.out.println(x + "TCP application failed");
        }


        System.out.println("\n--------------------Ithakicopter-TCP------------------------------");

        try {

            serverPort = 38048;
            Socket socketTCP = new Socket(hostAddress, serverPort); // establish connection
            //socketTCP.setSoTimeout(10000);
            for (int i=0;i<5;i++){
            InputStream in = socketTCP.getInputStream(); // what I receive from the server
            OutputStream out = socketTCP.getOutputStream(); // what i send to the server
            out.write("AUTO FLIGHTLEVEL=200 LMOTOR=180 RMOTOR=180 PILOT \r\n".getBytes());
            out.write("AUTO FLIGHTLEVEL=200 LMOTOR=180 RMOTOR=180 PILOT \r\n".getBytes());
            out.write("AUTO FLIGHTLEVEL=200 LMOTOR=180 RMOTOR=180 PILOT \r\n".getBytes());
            out.write("AUTO FLIGHTLEVEL=200 LMOTOR=180 RMOTOR=180 PILOT \r\n".getBytes());
            out.write("AUTO FLIGHTLEVEL=200 LMOTOR=180 RMOTOR=180 PILOT \r\n".getBytes());
            out.write("AUTO FLIGHTLEVEL=200 LMOTOR=180 RMOTOR=180 PILOT \r\n".getBytes());
            out.write("AUTO FLIGHTLEVEL=200 LMOTOR=180 RMOTOR=180 PILOT \r\n".getBytes());
            byte[] inputBuffer = in.readAllBytes();
            String message = new String(inputBuffer, StandardCharsets.US_ASCII);
            System.out.println("Ithaki responded via TCP with: \n" + message);
        }
        socketTCP.close();
        }
        catch (Exception x) {
            System.out.println(x + "Ithakicopter TCP application failed");
        }


        System.out.println("\nx--------------------END OF JAVA APPLICATION--------------------x");
        }
    }
