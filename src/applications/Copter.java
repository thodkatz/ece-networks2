package applications;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Copter {
  public static String udpTelemetry(DatagramSocket socket,
                                    InetAddress hostAddress, int serverPort,
                                    FileWriter writerCopter) {
    // TX
    // open ithakicopter.jar

    // RX only
    byte[] rxbuffer = new byte[128];
    DatagramPacket receivePacket =
        new DatagramPacket(rxbuffer, rxbuffer.length);

    long timeBefore = System.currentTimeMillis();
    String telemetry = new String();
    try {
      socket.setSoTimeout(3000);
      socket.receive(receivePacket);
      telemetry = new String(rxbuffer, StandardCharsets.US_ASCII);
      // System.out.print("Time repsonse: " + (System.currentTimeMillis() -
      // timeBefore)/(float)1000);
      System.out.println("Received data via UDP: " + telemetry);

      String[] tokensMotor = telemetry.split("LMOTOR=");
      String[] tokensAltitude = telemetry.split("ALTITUDE=");
      String[] tokensTemp = telemetry.split("TEMPERATURE=");
      String[] tokensPress = telemetry.split("PRESSURE=");
      writerCopter.write(tokensMotor[1].substring(0, 3) + " ");
      writerCopter.write(tokensAltitude[1].substring(0, 3) + " ");
      writerCopter.write(tokensTemp[1].substring(1, 6) + " ");
      writerCopter.write(tokensPress[1].substring(0, 7) + "\n");
    } catch (Exception x) {
      System.out.println(x);
      System.out.println("RX UDP ithakicopter failed");
    }
    return telemetry;
  }

  public static String tcpTelemetry(InetAddress hostAddress, int target) {
    String telemetry = "";
    Socket socket = new Socket();
    try {
      socket = new Socket(hostAddress, 38048);
      InputStream in = socket.getInputStream();
      OutputStream out = socket.getOutputStream();
      BufferedReader bf = new BufferedReader(new InputStreamReader(
          in)); // wrapper on top of the wrapper as java docs recommends
      ByteArrayOutputStream bos = new ByteArrayOutputStream();

      String command = "AUTO FLIGHTLEVEL=" + target + " LMOTOR=" + target +
                       " RMOTOR=" + target + " PILOT \r\n";
      // System.out.print("Request: " + command);
      out.write(command.getBytes());
      out.flush();

      // in.skipNBytes(427);
      for (int i = 0; i < 14; i++) {
        bos.write((bf.readLine() + "\n").getBytes());
      }
      String data = new String(bos.toByteArray(), StandardCharsets.US_ASCII);
      // System.out.println("Received data via TCP: " + data);

      String[] tokens = data.split("\n");
      // take only the useful data and skip the info ithaki sent
      telemetry = tokens[13];
    } catch (Exception x) {
      System.out.println(x);
      System.out.println("Oops... Ithakicopter TCP failed");
    }
    try {
      socket.close();
    } catch (Exception x) {
      System.out.println(x);
      System.out.println("Failed to close socket for ithakicopter TCP");
    }
    return telemetry;
  }

  /*
   * tcpTelemetry function for the TX and udpTelemetry for RX. The way that
   * these two functions are implemented force the autopilot to be used with a
   * combination of these two. We want to send a command only if it is needed
   * and we want to listen all the time to get feedback.
   *
   */
  public static void autopilot(DatagramSocket listen, InetAddress hostAddress,
                               int serverPort, Socket send, int lowerBound,
                               int higherBound) {

    lowerBound = Math.min(lowerBound, higherBound);
    higherBound = Math.max(lowerBound, higherBound);

    int target = (lowerBound + higherBound) / 2;
    int motor = -1;

    try {
      System.out.println("AUTOPILOT: ON");
      System.out.println(
          "You need to open ithakicopter.jar. Press ENTER to continue...");
      System.in.read();
      System.out.println("Press Control-C to exit...");
      Thread.sleep(1000);
      for (;;) {
        if ((motor < (lowerBound)) || (motor > (higherBound))) {
          System.out.println("Send packet. Readjust...");
          tcpTelemetry(hostAddress, target);
        }

        String telemetry =
            Copter.udpTelemetry(listen, hostAddress, serverPort, null);
        String[] tokens = telemetry.split("LMOTOR=");
        motor = Integer.parseInt(tokens[1].substring(0, 3)); // get motor values

        System.out.println("Parsed motor values: " + motor);
      }
    } catch (Exception x) {
      System.out.println(x);
      System.out.println("AUTOPILOT failed");
    }
  }
}
