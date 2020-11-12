package applications;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.awt.Desktop;
import java.util.Arrays;
import javax.sound.sampled.*;

public class Media {
    public static void image(DatagramSocket socket, InetAddress hostAddress, int serverPort, String requestCode) throws IOException {

        //if (numImage != (Integer.parseInt(args[1])-1)){
        //    continue; // readjust the camera as many time is requested via the command line arguement. Print only the result, the last request
        //}

        byte[] txbufferImage = requestCode.getBytes();
        byte[] rxbufferImage = new byte[1024];
        ByteArrayOutputStream bufferImage = new ByteArrayOutputStream();
        int countPackets = 0;
        long timeBefore = System.currentTimeMillis();
        long timeBeforePerPacket = System.currentTimeMillis();
        //System.out.println("My system time, when the request is sent, is: " + timeBefore);

        System.out.println("The request code is " + requestCode);
        DatagramPacket sendPacket = new DatagramPacket(txbufferImage, txbufferImage.length, hostAddress, serverPort);
        DatagramPacket receivePacket= new DatagramPacket(rxbufferImage, rxbufferImage.length);

        try {
            socket.send(sendPacket);	
        }
        catch (Exception x) {
            // x.printStackTrace(); // a more detailed diagnostic call
            System.out.println(x);
            System.out.println("Image application TX failed");
        }
        System.out.println("I am sleeping... Camera needs time to readjust");
        //Thread.sleep(5000); // sleep in order for the camera to readjust	

outerloop:
        try {
            socket.setSoTimeout(3000);
            for (;;) {
                socket.receive(receivePacket); // blocking command
                countPackets++;

                long timeAfterPerPacket = System.currentTimeMillis();
                System.out.println("The time required to reveive a packet is: " + (timeAfterPerPacket - timeBeforePerPacket)/(float)1000 + " seconds"); 
                timeBeforePerPacket = System.currentTimeMillis();

                System.out.println("Packet No" + countPackets + ". Length of data: " + rxbufferImage.length + ". The received bytes in hexadecimal format are:");
                for (int i = 0; i<rxbufferImage.length; i++) {
                    System.out.print(String.format("%02X", rxbufferImage[i])); // convert bytes to hexa string 

                    bufferImage.write(rxbufferImage[i]); // dynamic byte allocation
                    if ((String.format("%02X", rxbufferImage[i]).equals("D9")) && (i!=0)) {
                        if ((String.format("%02X", rxbufferImage[i-1]).equals("FF"))) {
                            break outerloop; // stop writing when EOF (OxFFD9 delimiter)
                        }
                    }
                }
                System.out.println();
            }
        }	
        catch (Exception x) {
            // x.printStackTrace(); // a more detailed diagnostic call
            System.out.println(x);
            System.out.println("Image application RX failed");
        }
        long timeAfter = System.currentTimeMillis(); // get the time when the image is received in bytes

        // logs for the received byte content
        System.out.println("\nComplete byte content of the image file in hexadecimal format:");
        byte[] completeDataImage = bufferImage.toByteArray();
        for (byte i : completeDataImage) {
            System.out.print(String.format("%02X", i)); // print hexadecimal the content of the byte array
        }
        System.out.println("\n\nTotal number of packages: " + (countPackets));
        System.out.println("How many Kbytes is the image? " + completeDataImage.length/(float)1000);

        // save image to a file
        File imageFile = new File("../media/image/sandbox/ithaki_image.jpg");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imageFile);
            fos.write(completeDataImage);
            System.out.println("File has been written successfully");
        }
        catch (Exception x) {
            // x.printStackTrace(); // 
            System.out.println("Image application error when writing the file:");
        }
        finally {
            fos.close(); // close the OutputStream
        }

        // what time is o'clock?
        System.out.println("Total amount of time to receive a frame: " + (timeAfter - timeBefore)/(float)1000 + " seconds");
        timeAfter = System.currentTimeMillis(); // get the time when the file is ready
        System.out.println("Total amount of time to receive and write a frame in a .jpg file: " + (timeAfter - timeBefore)/(float)1000 + " seconds");

        // open file image
        Desktop desktop = Desktop.getDesktop();
        if (imageFile.exists()) {
            //desktop.open(imageFile);
        }
    }

    public static void audio(DatagramSocket socket, InetAddress hostAddress, int serverPort, String requestCode) {

        // parsing the requestCode
        // expecting requestCode: AXXXX + ("AQ"" or "") + ("T" or "F") + numAudioPackets
        String encoding = "";
        String type = "F";
        String numAudioPackets = "";
        if (requestCode.length() == 11) {
            encoding = "AQ";
            type = requestCode.substring(7, 8); 
            numAudioPackets = requestCode.substring(8, 11);
        }
        else {
            type = requestCode.substring(5, 6);
            numAudioPackets = requestCode.substring(6, 9);
        }
        System.out.print("Encoding: " + encoding + ". Type: " + type + ". Number of packets: " + numAudioPackets);

        // TX
        byte[] txbufferSound = requestCode.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(txbufferSound, txbufferSound.length, hostAddress, serverPort);
        try {
            socket.send(sendPacket);
        }
        catch (Exception x) {
            // x.printStackTrace(); // a more detailed diagnostic call
            System.out.println(x);
            System.out.println("Audio application TX failed");
        }

        // RX
        byte[] dataSound = new byte[128];
        DatagramPacket receivePacket= new DatagramPacket(dataSound, dataSound.length);
        ByteArrayOutputStream bufferSound = new ByteArrayOutputStream();
        int countPackets = 0;
        int packetsSize = 0;
        long timeBefore = System.currentTimeMillis();
        long timeBeforePerPacket = System.currentTimeMillis();
        try {
            socket.setSoTimeout(3000);
            for (int l = 0; l < Integer.parseInt(numAudioPackets); l++) {
                timeBeforePerPacket = System.currentTimeMillis();
                socket.receive(receivePacket);
                countPackets++;
                long timeAfterPerPacket = System.currentTimeMillis();
                System.out.println("The time required to reveive a packet is: " + (timeAfterPerPacket - timeBeforePerPacket)/(float)1000 + " seconds"); 

                packetsSize += dataSound.length;
                System.out.println("Packet No" + countPackets + ". Length of data: " + dataSound.length);

                if (encoding.equals("")) {
                    // DPCM
                    bufferSound.write(dpcm(dataSound));
                }
                else if (encoding.equals("AQ")) {
                    // AQ-DPCM
                    bufferSound.write(adpcm(dataSound));
                }
                else {
                    System.out.println("This is not a valid request code");
                }
                System.out.println();
            }
        }
        catch (Exception x) {
            System.out.println(x);
            System.out.println("Receiving/writing the audio data failed");
        }

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
        if (encoding.equals("AQ")) {                                             
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
            System.out.println(x); 
            System.out.println("Sound playing failed");
        }

        // save music to file
        try{
            ByteArrayInputStream bufferSoundInput = new ByteArrayInputStream(completeDataSound);
            AudioInputStream streamSoundInput = new AudioInputStream(bufferSoundInput, modulationPCM, completeDataSound.length / modulationPCM.getFrameSize());
            AudioSystem.write(streamSoundInput, AudioFileFormat.Type.WAVE, new File("../media/music/sandbox/track.wav"));
            System.out.println("Sound file creation success");
        }
        catch (Exception x) {
            System.out.println(x); 
            System.out.println("Sound file creation failed");
        }
    }

    private static byte[] dpcm(byte[] dataSound) {
        ByteArrayOutputStream bufferSound = new ByteArrayOutputStream();
        int init = 0;
        int coeffDPCM = 2; // Trials: for 100 is pure noise, 4 good, 10 bad. I think below 4 you are good. In general it shouldn't 
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
            try {
                bufferSound.write(decodedSound);
            }
            catch (Exception x) {
                System.out.println(x);
                System.out.println("Decoding DPCM failed");
            }
        }
            return bufferSound.toByteArray(); 
        
    }

    private static byte[] adpcm(byte[] dataSound) {

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

        ByteArrayOutputStream bufferSound = new ByteArrayOutputStream();
        int init = meanSigned; // in DPCM we don't know the init value, we assume zero. But here we have data in the header.
        for (int i = 3; i<dataSound.length; i++) {
            // the sample may be bigger than byte. So you will need 16 bit encoding and store each int to 2 bytes.
            System.out.print("Input: " + String.format("%02X", dataSound[i]) + ", "); // print hexadecimal the content of the byte array                       

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
            try {
                bufferSound.write(decodedSound);
            }
            catch (Exception x) {
                System.out.println(x);
                System.out.println("Decoding DPCM failed");
            }
        }
        return bufferSound.toByteArray();
    }
}
