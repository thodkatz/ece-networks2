package applications;

import java.awt.Desktop;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import javax.sound.sampled.*;

public class Media {

  private static String pathFileImage = "./media/image/ithaki_image.jpg";
  private static String pathFileSound = "./media/music/track.wav";

  public static void image(DatagramSocket socket, InetAddress hostAddress,
                           int serverPort, String requestCode) {
    byte[] txbufferImage = requestCode.getBytes();
    byte[] rxbufferImage = new byte[1024];
    int countPackets = 0;
    long timeBefore = System.currentTimeMillis();

    System.out.println("The request code is " + requestCode);
    DatagramPacket sendPacket = new DatagramPacket(
        txbufferImage, txbufferImage.length, hostAddress, serverPort);
    DatagramPacket receivePacket =
        new DatagramPacket(rxbufferImage, rxbufferImage.length);

    // TX
    try {
      socket.send(sendPacket);
      if (requestCode.contains("DIR")) {
        System.out.println("I am sleeping... Camera needs time to readjust");
        Thread.sleep(5000); // sleep in order for the camera to readjust
      }
    } catch (Exception x) {
      x.printStackTrace();
    }

    // RX
    ByteArrayOutputStream bufferImage = new ByteArrayOutputStream();
  outerloop:
    try {
      socket.setSoTimeout(3000);
      for (;;) {
        socket.receive(receivePacket); // blocking command
        countPackets++;

        for (int i = 0; i < rxbufferImage.length; i++) {
          // System.out.print(String.format("%02X", rxbufferImage[i]));
          bufferImage.write(rxbufferImage[i]); // dynamic byte allocation
          if ((String.format("%02X", rxbufferImage[i]).equals("D9")) &&
              (i != 0)) {
            if ((String.format("%02X", rxbufferImage[i - 1]).equals("FF"))) {
              break outerloop; // stop writing when EOF (OxFFD9 delimiter)
            }
          }
        }
      }
    } catch (Exception x) {
      x.printStackTrace();
    }

    byte[] completeDataImage = bufferImage.toByteArray();
    imageInfo(completeDataImage, countPackets, timeBefore);

    // save image to a file
    saveImage(completeDataImage, pathFileImage);

    // openImage(pathFileImage);
  }

  public static void audio(DatagramSocket socket, InetAddress hostAddress,
                           int serverPort, String encoding, String type,
                           String numAudioPackets, String songID,
                           String requestCodeSound) {

    String completeRequest =
        requestCodeSound + encoding + type + numAudioPackets;
    System.out.println("The request code: " + completeRequest);

    // TX
    byte[] txbufferSound = (songID + completeRequest).getBytes();
    DatagramPacket sendPacket = new DatagramPacket(
        txbufferSound, txbufferSound.length, hostAddress, serverPort);
    try {
      socket.send(sendPacket);
    } catch (Exception x) {
      x.printStackTrace();
    }

    // RX
    byte[] dataSound = new byte[128];
    DatagramPacket receivePacket =
        new DatagramPacket(dataSound, dataSound.length);
    ByteArrayOutputStream bufferSound = new ByteArrayOutputStream();
    int countPackets = 0;
    int packetSize = 0;
    long timeBefore = System.currentTimeMillis();

    try (FileWriter writerDiffSamples = new FileWriter(
             new File("logs/" + encoding + type + "diff_samples.txt"));
         FileWriter writerSamples = new FileWriter(
             new File("logs/" + encoding + type + "samples.txt"));
         FileWriter writerMean =
             new FileWriter(new File("logs/aqdpcm_mean.txt"));
         FileWriter writerStep =
             new FileWriter(new File("logs/aqdpcm_step.txt"))) {
      socket.setSoTimeout(3000);
      for (int l = 0; l < Integer.valueOf(numAudioPackets); l++) {
        socket.receive(receivePacket);
        countPackets++;
        packetSize += dataSound.length;

        if (encoding.equals("")) {
          bufferSound.write(dpcm(dataSound, writerDiffSamples, writerSamples));
        } else if (encoding.equals("AQ")) {
          bufferSound.write(adpcm(dataSound, writerDiffSamples, writerSamples,
                                  writerMean, writerStep));
        } else {
          System.out.println("This is not a valid request code");
        }
      }
    } catch (Exception x) {
      x.printStackTrace();
    }

    long timeAfter = System.currentTimeMillis();
    byte[] completeDataSound = bufferSound.toByteArray();
    musicInfo(completeDataSound, timeBefore, timeAfter, countPackets,
              packetSize);

    // only in 16 bit samples does matter. In
    // AQ-DPCM we use 16 bit encoding
    boolean isBigEndian = false;
    int encodingBits = 8;
    if (encoding.equals("AQ")) {
      isBigEndian = true;
      encodingBits = 16;
    }

    AudioFormat modulationPCM =
        new AudioFormat(8000, encodingBits, 1, true, isBigEndian);
    // play sound
    playMusic(completeDataSound, modulationPCM);

    // save music to file
    saveMusic(completeDataSound, modulationPCM);
  }

  private static void openImage(String fileImage) {
    Desktop desktop = Desktop.getDesktop();
    File imageFile = new File(pathFileImage);
    if (imageFile.exists()) {
      try {
        desktop.open(imageFile);
      } catch (Exception x) {
        x.printStackTrace();
      }
    }
  }

  private static void imageInfo(byte[] completeDataImage, int countPackets,
                                long timeBefore) {
    // printImageHex(completeDataImage);
    System.out.println("\nTotal number of packages: " + (countPackets));
    System.out.println("How many Kbytes is the image? " +
                       completeDataImage.length / (float)1000);

    System.out.println("Total amount of time to receive a frame: " +
                       (System.currentTimeMillis() - timeBefore) / (float)1000 +
                       " seconds");
    System.out.println(
        "Total amount of time to receive and write a frame in a .jpg file: " +
        (System.currentTimeMillis() - timeBefore) / (float)1000 + " seconds");
  }

  /**
   * For deubgging purposes print bytes to hexadecimal
   * @param completeData The byte array to be printed as hexadecimal
   */
  private static void printByteHex(byte[] completeData) {
    System.out.println(
        "\nComplete byte content of the data file in hexadecimal format:");
    for (byte i : completeData) {
      System.out.print(String.format("%02X", i));
    }
  }

  private static void musicInfo(byte[] completeDataSound, long timeBefore,
                                long timeAfter, int countPackets,
                                int packetSize) {

    // printByteHex(completeDataSound);

    System.out.println("\n\nTotal number of packages: " + (countPackets));
    System.out.println(
        "How many Kbytes is the sound? " +
        completeDataSound.length / (float)1000 +
        "\nHow many Kbytes is the data that was actually sent? " +
        packetSize / (float)1000);
    System.out.println("Total amount of time to receive sound data: " +
                       (timeAfter - timeBefore) / (float)1000 + " seconds");
  }

  private static void saveImage(byte[] completeDataImage,
                                String pathFileImage) {
    try (FileOutputStream fos = new FileOutputStream(new File(pathFileImage))) {
      fos.write(completeDataImage);
      System.out.println("File has been written successfully");
    } catch (Exception x) {
      x.printStackTrace();
    }
  }

  private static void saveMusic(byte[] completeDataSound,
                                AudioFormat modulationPCM) {
    try (AudioInputStream streamSoundInput = new AudioInputStream(
             new ByteArrayInputStream(completeDataSound), modulationPCM,
             completeDataSound.length / modulationPCM.getFrameSize())) {
      AudioSystem.write(streamSoundInput, AudioFileFormat.Type.WAVE,
                        new File(pathFileSound));
      System.out.println("Sound file creation success");
    } catch (Exception x) {
      x.printStackTrace();
    }
  }

  private static void playMusic(byte[] completeDataSound,
                                AudioFormat modulationPCM) {
    try (SourceDataLine outputAudio =
             AudioSystem.getSourceDataLine(modulationPCM)) {
      // outputAudio.open(modulationPCM, 3200);
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
      System.out.println("Listening...");
      Thread.sleep(500);
      outputAudio.write(completeDataSound, 0, completeDataSound.length);
      outputAudio.stop();
      System.out.println("\nSound application success!");
    } catch (Exception x) {
      x.printStackTrace();
    }
  }

  private static byte[] dpcm(byte[] dataSound, FileWriter writerDiffSamples,
                             FileWriter writerSamples) {

    ByteArrayOutputStream bufferSound = new ByteArrayOutputStream();
    int init = 0;
    int step = 1;

    for (int i = 0; i < dataSound.length; i++) {
      // get nibbles
      int maskLow = 0x0F;
      int maskHigh = 0xF0;

      // D[i] = x[i] - x[i-1]
      int nibbleLow = dataSound[i] & maskLow;

      // D[i-1] = x[i-1] - x[i-2]
      int nibbleHigh = (dataSound[i] & maskHigh) >> 4;

      // differences
      int diffHigh = (nibbleHigh - 8) * step;
      int diffLow = (nibbleLow - 8) * step;

      // get samples
      int sampleFirst = init + diffHigh;
      int sampleSecond = sampleFirst + diffLow;
      init = sampleSecond;

      // clipping
      int[] samples = {sampleFirst, sampleSecond};
      clipping(samples);

      // write to buffer
      byte[] decodedSound = new byte[2];
      decodedSound[0] = (byte)sampleFirst;
      decodedSound[1] = (byte)sampleSecond;

      try {
        bufferSound.write(decodedSound);
        writerDiffSamples.write(diffHigh + "\n" + diffLow + "\n");
        writerSamples.write(samples[0] + "\n" + samples[1] + "\n");
      } catch (Exception x) {
        x.printStackTrace();
      }
    }

    try {
      bufferSound.close();
    } catch (Exception x) {
      x.printStackTrace();
    }

    return bufferSound.toByteArray();
  }

  private static byte[] adpcm(byte[] dataSound, FileWriter writerDiffSamples,
                              FileWriter writerSamples, FileWriter writerMean,
                              FileWriter writerStep) {

    // get the header first
    int meanSigned = (dataSound[1] << 8 | dataSound[0]);
    int step = (Byte.toUnsignedInt(dataSound[3]) << 8 |
                Byte.toUnsignedInt(dataSound[2]));
    System.out.println(meanSigned);

    try {
      writerMean.write(meanSigned + "\n");
      writerStep.write(step + "\n");
    } catch (Exception x) {
      x.printStackTrace();
    }

    ByteArrayOutputStream bufferSound = new ByteArrayOutputStream();
    // in DPCM we don't know the init value, we assume
    // zero. But here we have data in the header.
    int init = meanSigned;
    for (int i = 3; i < dataSound.length; i++) {
      // get nibbles
      int maskLow = 0x0F;
      int maskHigh = 0xF0;

      // D[i] = x[i] - x[i-1], should be unsigned
      int nibbleLow = (dataSound[i] & maskLow);

      // D[i-1] = x[i-1] - x[i-2], should be unsigned
      int nibbleHigh = (dataSound[i] & maskHigh) >> 4;

      // differences
      int diffHigh = (nibbleHigh - 8) * step;
      int diffLow = (nibbleLow - 8) * step;

      // get samples (implement recursive formula)
      int sampleFirst = init + diffHigh;
      int sampleSecond = sampleFirst + diffLow;
      init = sampleSecond;

      // cliping
      int[] samples = {sampleFirst, sampleSecond};
      clipping(samples);

      // write data to files
      try {
        writerDiffSamples.write(diffHigh + "\n" + diffLow + "\n");
        writerSamples.write(samples[0] + "\n" + samples[1] + "\n");
      } catch (Exception x) {
        x.printStackTrace();
      }

      // write to buffer
      byte[] decodedSound = new byte[4];
      decodedSound[0] = (byte)(samples[0] >> 8); // MSB of sample 15-8
      decodedSound[1] = (byte)samples[0];        // LSB of sample 7-0
      decodedSound[2] = (byte)(samples[1] >> 8);
      decodedSound[3] = (byte)samples[1];

      try {
        bufferSound.write(decodedSound);
      } catch (Exception x) {
        x.printStackTrace();
      }
    }

    return bufferSound.toByteArray();
  }

  private static void clipping(int[] samples) {
    int max16 = (int)(Math.pow(2, 15)) - 1;
    int min16 = -(int)(Math.pow(2, 15));
    for (int j = 0; j < samples.length; j++) {
      if (samples[j] > max16)
        samples[j] = max16;
      else if (samples[j] < min16)
        samples[j] = min16;
    }
  }
}
