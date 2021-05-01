package applications;

import java.io.File;
import java.io.FileWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class Echo {

  /*
   * UDP TX/RX Echo application with delay
   *
   * WARNING: It doesn't close the DatagramSocket. You should do it manually if
   * it is desired after the call of the function.
   *
   * @param requestCode If request code is set to E000 then the execute will
   *     have no delay for the RX
   */
  public static long execute(DatagramSocket socket, InetAddress hostAddress,
                             int serverPort, String requestCode) {
    System.out.println(
        "\n--------------------Echo application--------------------");

    if (requestCode.equals("E0000"))
      System.out.println("Delay: OFF");
    else if (requestCode.length() > 5)
      System.out.println("Mode: Temperature\nDelay: OFF");
    else
      System.out.println("Delay: ON");

    byte[] txbuffer = requestCode.getBytes();
    byte[] rxbuffer = new byte[64];
    long diff = 0;
    try {
      socket.setSoTimeout(3000);
      DatagramPacket sendPacket = new DatagramPacket(txbuffer, txbuffer.length,
                                                     hostAddress, serverPort);
      DatagramPacket receivePacket =
          new DatagramPacket(rxbuffer, rxbuffer.length);

      // ACTION
      socket.send(sendPacket);
      System.out.println(
          "The request code is: " + requestCode +
          "\nThe destination port is: " + serverPort +
          "\nMy listening port (clientPort): " + socket.getLocalPort());
      long timeBefore = System.currentTimeMillis();

      // LISTEN
      socket.receive(receivePacket);
      long timeAfter = System.currentTimeMillis();
      diff = timeAfter - timeBefore;
      System.out.println("The time required to reveive a packet is: " + diff +
                         " milliseconds");
      String message =
          new String(receivePacket.getData(), StandardCharsets.US_ASCII);
      System.out.println("Ithaki responded with: " + message);
    } catch (Exception x) {
      x.printStackTrace();
    }
    return diff;
  }

  /**
   * For a specific time interval send and receive multiple echo packets
   * and 1) calculate throughput with moving average (8 seconds window)
   * ans 2) calculate Retransmission Timeout
   *
   */
  public static void telemetry(DatagramSocket socket, InetAddress hostAddress,
                               int serverPort, String requestCode,
                               String fileSuffix) {

    FileWriter fileRto = null;
    try (FileWriter fileSamples =
             new FileWriter(new File("logs/echo_samples_" + fileSuffix));
         FileWriter fileThroughput =
             new FileWriter(new File("logs/echo_throughput_" + fileSuffix))) {

      if (!requestCode.equals("E0000"))
        fileRto = new FileWriter(new File("logs/rto.txt"));

      // keep track how many 8 seconds have passed
      int count8sec[] = new int[8];

      float throughput[] = new float[8];

      int cumulativeSum[] = new int[8];

      long tic[] = new long[8];
      long timeBefore = System.currentTimeMillis();
      for (int i = 0; i < 8; i++) {
        tic[i] = timeBefore + i * 1000; // move per second
      }

      int isFirst = 1;
      double rtts = 0;
      double rttd = 0;
      double rto = 0;

      final int minutes = 2;
      final int seconds = 60;
      int timeInterval = seconds * 1000 * minutes;
      while ((System.currentTimeMillis() - timeBefore) < timeInterval) {
        long rtt = Echo.execute(socket, hostAddress, serverPort, requestCode);
        fileSamples.write(rtt + "\n");

        // throughput moving average
        throughputCalc(count8sec, throughput, cumulativeSum, tic, timeBefore,
                       fileThroughput);

        // Retransmission timeout
        if (!requestCode.equals("E0000")) {
          rto(isFirst, rtt, rtts, rttd, rto, fileRto);
          isFirst = 0;
        }
      }
    } catch (Exception x) {
      x.printStackTrace();
    } finally {
      if (fileRto != null) {
        try {
          fileRto.close();
        } catch (Exception x) {
          x.printStackTrace();
        }
      }
    }
  }

  private static void rto(int isFirst, double rtt, double rtts, double rttd,
                          double rto, FileWriter fileRto) throws Exception {
    // init values
    if (isFirst == 1) {
      rtts = rtt;
      rttd = rtt / 2;
      rto = 1; // according to rfc
      fileRto.write("RTT SRTT RTTd RTO\n");
    }

    // TODO: Fix magic numbers
    double temp = rtts;
    rtts = 0.875 * temp + 0.125 * rtt;

    temp = rttd;
    rttd = 0.75 * temp + 0.25 * Math.abs(rtt - rtts);

    rto = rtts + 1.8 * rttd;
    fileRto.write(rtt + " " + rtts + " " + rttd + " " + rto + "\n");

    System.out.println();
  }

  private static void throughputCalc(int[] count8sec, float[] throughput,
                                     int[] cumulativeSum, long[] tic,
                                     long timeBefore, FileWriter fileThroughput)
      throws Exception {
    for (int i = 0; i < 8; i++) {
      long toc = System.currentTimeMillis() - tic[i];
      System.out.println("The element " + i + " has toc: " + toc);
      if (toc < 8000 && toc > 0) {
        // assume no timeouts during the measurements
        cumulativeSum[i] += 32 * 8;
        System.out.println("Cumsum: " + cumulativeSum[i]);
      } else if (toc > 8000) {
        count8sec[i]++;
        tic[i] = count8sec[i] * 8000 + timeBefore + i * 1000;
        throughput[i] = cumulativeSum[i] / (float)8;
        fileThroughput.write(throughput[i] + "\n");

        System.out.println("I will flush " + cumulativeSum[i] + " cumsum");
        System.out.println("The throughput is: " + throughput[i]);

        cumulativeSum[i] = 0; // let's start again for the next 8 seconds
      }
    }
  }
}
