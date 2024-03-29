package applications;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Obd {

  private static String[] header = {"01 1F", "01 0F", "01 11",
                                    "01 0C", "01 0D", "01 05"};

  public static void udpTelemetry(DatagramSocket socket,
                                  InetAddress hostAddress, int serverPort,
                                  String requestCode) {

    byte[] rxbuffer = new byte[16];
    DatagramPacket receivePacket =
        new DatagramPacket(rxbuffer, rxbuffer.length);

    for (int i = 0; i < header.length; i++) {
      // TX
      String completeCode = (requestCode + "OBD=" + header[i]);
      byte[] txbuffer = completeCode.getBytes();
      DatagramPacket sendPacket = new DatagramPacket(txbuffer, txbuffer.length,
                                                     hostAddress, serverPort);
      System.out.println("Complete request: " + completeCode);
      try {
        socket.send(sendPacket);
      } catch (Exception x) {
        x.printStackTrace();
      }
      long timeBefore = System.currentTimeMillis();

      // RX
      try {
        socket.setSoTimeout(3000);
        socket.receive(receivePacket);
        String message = new String(rxbuffer, StandardCharsets.US_ASCII);
        System.out.println("Ithaki responded via UDP with: " + message);
        System.out.println("Ithaki UDP time response: " +
                           (System.currentTimeMillis() - timeBefore) /
                               (float)1000 +
                           " seconds");

        int[] values = parser(message);
        formula(values[0], values[1], header[i]);
      } catch (Exception x) {
        x.printStackTrace();
      }
    }
  }

  public static float tcpTelemetry(Socket socket, FileWriter writerVehicle) {
    float engineTime = 0;

    try {
      InputStream in = socket.getInputStream();
      OutputStream out = socket.getOutputStream();
      BufferedReader bf = new BufferedReader(new InputStreamReader(in));

      for (int i = 0; i < header.length; i++) {
        out.write((header[i] + "\r").getBytes());
        // out.flush();
        long timeBefore = System.currentTimeMillis();
        System.out.println(
            "Created TCP socket and set output stream... Waiting for response");

        System.out.println("Header: " + header[i]);
        String data = bf.readLine();
        System.out.println("Ithaki responded via TCP with: " + data);
        System.out.println("Ithaki TCP time response: " +
                           (System.currentTimeMillis() - timeBefore) /
                               (float)1000 +
                           " seconds");

        int[] values = parser(data);
        float value = formula(values[0], values[1], header[i]);
        writerVehicle.write(value + " ");
        if (header[i] == "01 1F")
          engineTime = value;
      }

      writerVehicle.write("\n");
    } catch (Exception x) {
      x.printStackTrace();
    }

    return engineTime;
  }

  private static float formula(int first, int second, String header) {
    float value = 0;
    switch (header) {
    case "01 1F":
      int engineRunTime = first * 256 + second;
      System.out.println("Engine run time: " + engineRunTime);
      value = engineRunTime;
      break;

    case "01 0F":
      int intakeAirTemp = first - 40;
      System.out.println("Intake Air Temperature: " + intakeAirTemp);
      value = intakeAirTemp;
      break;

    case "01 11":
      float throttlePos = (first * 100) / (float)255;
      System.out.println("Throttle position: " + throttlePos);
      value = throttlePos;
      break;

    case "01 0C":
      float engineRpm = ((first * 256) + second) / (float)4;
      System.out.println("Engine RPM: " + engineRpm);
      value = engineRpm;
      break;

    case "01 0D":
      int speed = first;
      System.out.println("Vehicle speed: " + speed);
      value = speed;
      break;

    case "01 05":
      int coolantTemp = first - 40;
      System.out.println("Coolant Temperature: " + coolantTemp);
      value = coolantTemp;
      break;

    default:
      System.out.println(
          "Something went wrong calculating formual for vehicle stats");
    }
    System.out.println();
    return value;
  }

  private static int[] parser(String data) {
    String byte1 = data.substring(6, 8);
    int first = Integer.parseInt(byte1, 16);
    String byte2 = "";
    int second = 0;
    if (data.length() > 8) {
      byte2 = data.substring(9, 11);
      second = Integer.parseInt(byte2, 16);
    }
    System.out.println();

    int[] temp = {first, second};
    return temp;
  }
}
