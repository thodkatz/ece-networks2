// it is considered in general a bad practise to use asterisks to import all the
// classes
import applications.*;
import java.awt.Desktop;
import java.io.*;
import java.lang.Math.*;
import java.lang.System;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Scanner;
import javax.sound.sampled.*;

class UserApplication {

  public static void main(String[] args) throws Exception {

    printWelcome();

    // preamble
    byte[] hostIP = {(byte)155, (byte)207, (byte)18, (byte)208};
    InetAddress hostAddress = InetAddress.getByAddress(hostIP);

    String[] codes = WebScraping.getCodes();
    int clientPort = Integer.valueOf(codes[0]);
    int serverPort = Integer.valueOf(codes[1]);

    String requestCodeEcho = codes[2];
    String requestCodeImage = codes[3] + "UDP=1024";
    String requestCodeSound = codes[4];
    String requestCodeCopter = codes[5];
    String requestCodeVehicle = codes[6];

    DatagramSocket socket = new DatagramSocket(clientPort);

    // control user input
    int flag = 1;
    do {
      System.out.println(
          "\nPlease enter a number (1-11). Available options are:\n"
          + "1) Echo with delay\n"
          + "2) Echo no delay\n"
          + "3) Temperature\n"
          + "4) Image\n"
          + "5) Music\n"
          + "6) Vehicle UDP\n"
          + "7) Ithakicopter UDP\n"
          + "8) Autopilot\n"
          + "9) HTTPS TCP\n"
          + "10) Ithakicopter TCP\n"
          + "11) Vehicle TCP");

      Scanner in = new Scanner(System.in);
      String choiceApp = in.nextLine();
      in.close();

      switch (choiceApp) {
      case "1":

        /* ------------------- Echo with delay ------------------ */
        printASCII("src/ascii/echo.txt");

        try (FileWriter writerInfo =
                 new FileWriter(new File("logs/echo_info_delay.txt"))) {
          writerInfo.write("Info:\n"
                           + "The request code is " + requestCodeEcho + "\n");
          writerInfo.write("Tic: " + LocalDateTime.now() + "\n");

          Echo.telemetry(socket, hostAddress, serverPort, requestCodeEcho,
                         "delay.txt");

          writerInfo.write("Toc: " + LocalDateTime.now());
        } catch (Exception x) {
          System.out.println(x);
        }

        break;

      case "2":
        /* ------------------- Echo no delay ------------------ */
        printASCII("src/ascii/echo.txt");

        try (FileWriter writerInfo =
                 new FileWriter(new File("logs/echo_info_no_delay.txt"))) {

          writerInfo.write("Info:\n"
                           + "The request code is "
                           + "requestCodeEcho"
                           + "\n");
          writerInfo.write("Tic: " + LocalDateTime.now() + "\n");

          Echo.telemetry(socket, hostAddress, serverPort, "E0000",
                         "no_delay.txt");
          writerInfo.write("Toc: " + LocalDateTime.now());
        } catch (Exception x) {
          System.out.println(x);
        }

        break;

      case "3":
        /* --------------------- Temperature -------------------- */
        printASCII("src/ascii/temp.txt");

        try (FileWriter writerTemp =
                 new FileWriter(new File("logs/temp_info.txt"))) {
          writerTemp.write("Info Temperature app:\n" + requestCodeEcho + "\n" +
                           LocalDateTime.now() + "\n");

          for (int i = 0; i < 1; i++) {
            Echo.execute(socket, hostAddress, serverPort,
                         requestCodeEcho + "T00");
            System.out.println();
          }

          writerTemp.write(LocalDateTime.now() + "\n");
        } catch (Exception x) {
          System.out.println(x);
        }
        break;

      case "4":
        /* --------------------------- Image --------------------------- */
        printASCII("src/ascii/image.txt");

        String encodingImage = "CAM=PTZDIR=R";
        try (FileWriter writerImage =
                 new FileWriter(new File("logs/image_info_" + encodingImage))) {
          writerImage.write(encodingImage + "\n" + requestCodeImage + "\n" +
                            LocalDateTime.now() + "\n");

          for (int i = 0; i < 1; i++) {
            Media.image(socket, hostAddress, serverPort,
                        requestCodeImage + encodingImage);
            System.out.println();
          }
          
          writerImage.write(LocalDateTime.now() + "\n");
        } catch (Exception x) {
          System.out.println(x);
        }

        break;

      case "5":
        /* --------------------------- Audio --------------------------- */
        printASCII("src/ascii/audio.txt");

        String numAudioPackets = "999";
        String[] type = {"F", "T"};
        String[] encoding = {"AQ", ""};
        String completeRequest =
            requestCodeSound + encoding[0] + type[0] + numAudioPackets;

        File infoMusic =
            new File("logs/music_info_" + encoding[1] + type[0] + ".txt");
        FileWriter writerInfoMusic = new FileWriter(infoMusic);
        writerInfoMusic.write(requestCodeSound + "\nEncoding: " + encoding[1] +
                              "\nType: " + type[0] + LocalDateTime.now() +
                              "\n");

        Media.audio(socket, hostAddress, serverPort, completeRequest);
        System.out.println();

        writerInfoMusic.write(LocalDateTime.now() + "\n");
        writerInfoMusic.close();
        socket.close();
        break;

      case "6":
        /* ------------------- Vehicle OBD UDP ------------------ */
        printASCII("src/ascii/obd.txt");

        Obd.udpTelemetry(socket, hostAddress, serverPort, requestCodeVehicle);
        socket.close();
        break;

      case "7":
        /* ------------------ Ithakicopter UDP ------------------ */
        printASCII("src/ascii/copter.txt");

        socket = new DatagramSocket(48078);

        System.out.println(
            "For Ithakicopter UDP telemetry you need to open ithakicopter.jar");
        System.out.print("Did you open it? If yes press ENTER to continue");
        System.in.read();
        Thread.sleep(1000); // pause a bit to catch up with the user
        System.out.println("Press ENTER to exit");
        Thread.sleep(1000);

        FileWriter writerCopter =
            new FileWriter(new File("logs/copter_info.txt"));
        writerCopter.write("Info Ithakicopter app:\n" + LocalDateTime.now() +
                           "\n");
        writerCopter.write("MOTOR ALTITUDE TEMPERATURE PRESSURE");

        for (int i = 0; i < 4; i++)
          Echo.execute(socket, hostAddress, serverPort, requestCodeEcho);

        while (System.in.available() == 0) {
          Copter.udpTelemetry(socket, hostAddress, serverPort, writerCopter);
        }

        writerCopter.write(LocalDateTime.now() + "\n");
        writerCopter.close();
        socket.close();
        break;

      case "8":
        /* ---------------------- Autopilot --------------------- */
        printASCII("src/ascii/auto.txt");

        socket = new DatagramSocket(48078);
        Socket socketAuto = new Socket(hostAddress, 38048);

        int lowerBound = 160;
        int higherBound = 190;
        Copter.autopilot(socket, hostAddress, serverPort, socketAuto,
                         Math.min(200, Math.max(150, lowerBound)),
                         Math.min(200, Math.max(150, higherBound)));
        socketAuto.close();
        break;

      case "9":
        /* ---------------------- HTTPS TCP --------------------- */
        printASCII("src/ascii/https.txt");

        Socket httpsSocket = new Socket(hostAddress, 80);
        https(httpsSocket);
        httpsSocket.close();
        break;

      case "10":
        /* ------------------- IthakicopterTCP ------------------ */
        printASCII("src/ascii/copter_tcp.txt");

        for (int i = 0; i < 4; i++)
          Echo.execute(socket, hostAddress, serverPort, requestCodeEcho);

        int target = 180;
        for (int i = 0; i < 20; i++) {
          System.out.println(
              new String(Copter.tcpTelemetry(hostAddress, target)));
        }

        // socketCopter.close();
        break;

      case "11":
        /* ------------------- Vehicle OBD TCP ------------------ */
        printASCII("src/ascii/obd_tcp.txt");

        for (int i = 0; i < 4; i++)
          Echo.execute(socket, hostAddress, serverPort, requestCodeEcho);

        Socket socketVehicle = new Socket(hostAddress, 29078);

        FileWriter writerVehicleInfo =
            new FileWriter(new File("logs/car_info.txt"));
        writerVehicleInfo.write("Info Vehicle app:\n" + LocalDateTime.now() +
                                "\n");
        FileWriter writerVehicleData =
            new FileWriter(new File("logs/car_telemetry.txt"));

        long timeBefore = System.currentTimeMillis();
        float engineTime = 0;
        while (engineTime < 60 * 4) {
          engineTime = Obd.tcpTelemetry(socketVehicle, writerVehicleData);
          System.out.println("The engine run time is " + engineTime + "\n");
        }

        writerVehicleInfo.write(LocalDateTime.now() + "\n");
        writerVehicleInfo.close();
        writerVehicleData.close();
        socketVehicle.close();
        break;

      default:
        System.out.println(
            "Please provide a valid input. If you want to exit then press Control-C.\n");
        flag = 0;
      }
    } while (flag == 0);

    /* ------------------ Close UDP sockets ----------------- */
    if (!socket.isClosed()) {
      socket.close();
      System.out.println("\nShuting down UDP sockets...");
    }

    System.out.println(
        "\nx--------------------Hooray! Java application finished successfully!--------------------x");
  }

  /**
   * Print ASCII text
   * @param filePath The path of the file with the ASCII characters to be
   *     printed
   */
  private static void printASCII(String filePath) {
    try {
      Scanner input = new Scanner(new File(filePath));
      while (input.hasNextLine()) {
        System.out.println(input.nextLine());
      }
      Thread.sleep(1500); // pause a little bit to enjoy the view
    } catch (Exception x) {
      System.out.println(x);
    }
  }

  /**
   * Print welcome screen ASCII with CYAN color
   */
  private static void printWelcome() {
    // windows users may be not able to view colors on terminal
    final String ANSI_CYAN = "\u001B[36m";
    final String ANSI_RESET = "\u001B[0m";

    try {
      Scanner input = new Scanner(new File("src/ascii/welcome.txt"));
      while (input.hasNextLine()) {
        System.out.print(ANSI_CYAN); // add some color!
        System.out.print(input.nextLine());
        System.out.println(ANSI_RESET);
      }
      System.out.println();
      System.out.print("Press ENTER to continue");
      System.in.read(); // pause a little bit to enjoy the view

    } catch (Exception x) {
      System.out.println(x);
    }
  }

  private static void https(Socket socket) {
    try {
      InputStream in =
          socket.getInputStream(); // what I receive from the server
      OutputStream out = socket.getOutputStream(); // what i send to the server

      long timeBefore = System.currentTimeMillis();
      out.write(
          "GET /netlab/hello.html HTTP/1.0\r\nHost: ithaki.eng.auth.gr:80\r\n\r\n"
              .getBytes());

      byte[] inputBuffer = in.readAllBytes();
      String message = new String(inputBuffer, StandardCharsets.US_ASCII);
      System.out.println("Ithaki responded via TCP with: \n" + message);
      System.out.println(
          "Time response: " +
          (System.currentTimeMillis() - timeBefore) / (float)1000 + " seconds");
      socket.close();
    } catch (Exception x) {
      System.out.println(x + "TCP application failed");
    }
  }
}
