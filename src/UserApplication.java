import applications.*;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.System;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Scanner;

class UserApplication {

  public static void main(String[] args) {

    printWelcome();

    String[] codes = WebScraping.getCodes();
    int clientPort = Integer.valueOf(codes[0]);
    int serverPort = Integer.valueOf(codes[1]);

    String requestCodeEcho = codes[2];
    String requestCodeImage = codes[3] + "UDP=1024";
    String requestCodeSound = codes[4];
    String requestCodeCopter = codes[5];
    String requestCodeVehicle = codes[6];

    byte[] hostIP = {(byte)155, (byte)207, (byte)18, (byte)208};
    InetAddress hostAddress = null;
    DatagramSocket socket = null;

    try {
      hostAddress = InetAddress.getByAddress(hostIP);
      socket = new DatagramSocket(clientPort);
    } catch (Exception x) {
      x.printStackTrace();
    }

    Scanner in = new Scanner(System.in);

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
      String choiceApp = in.nextLine();

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
          x.printStackTrace();
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
          x.printStackTrace();
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
          x.printStackTrace();
        }
        break;

      case "4":
        /* --------------------------- Image --------------------------- */
        printASCII("src/ascii/image.txt");

        String encodingImage = "";
        System.out.println("Enter 0 for CAM and 1 for PTZ: ");
        try {
          int userInput = in.nextInt();
          if (Integer.valueOf(userInput) == 1) {
            encodingImage = "CAM=PTZ";
          } else {
            encodingImage = "CAM=FIX";
          }
        } catch (Exception x) {
          x.printStackTrace();
        }

        try (FileWriter writerImage = new FileWriter(
                 new File("logs/image_info_" + encodingImage + ".txt"))) {
          writerImage.write(encodingImage + "\n" + requestCodeImage + "\n" +
                            LocalDateTime.now() + "\n");

          for (int i = 0; i < 1; i++) {
            Media.image(socket, hostAddress, serverPort,
                        requestCodeImage + encodingImage);
            System.out.println();
          }

          writerImage.write(LocalDateTime.now() + "\n");
        } catch (Exception x) {
          x.printStackTrace();
        }

        break;

      case "5":
        /* --------------------------- Audio --------------------------- */
        printASCII("src/ascii/audio.txt");

        String numAudioPackets = "999";

        // song
        String type = "F";

        // tone
        // String type = "T";

        // AQDPCM modulation
        // String encoding = "AQ";

        // DPCM modulation
        String encoding = "";

        // choose song L00 - L??
        String songID = "L02";

        File infoMusic =
            new File("logs/music_info_" + encoding + type + ".txt");
        try (FileWriter writerInfoMusic = new FileWriter(infoMusic)) {
          writerInfoMusic.write(requestCodeSound + "\nEncoding: " + encoding +
                                "\nType: " + type + LocalDateTime.now() + "\n");

          Media.audio(socket, hostAddress, serverPort, encoding, type,
                      numAudioPackets, songID, requestCodeSound);
          System.out.println();

          writerInfoMusic.write(LocalDateTime.now() + "\n");
          writerInfoMusic.close();
        } catch (Exception x) {
          x.printStackTrace();
        }

        break;

      case "6":
        /* ------------------- Vehicle OBD UDP ------------------ */
        printASCII("src/ascii/obd.txt");
        Obd.udpTelemetry(socket, hostAddress, serverPort, requestCodeVehicle);
        break;

      case "7":
        /* ------------------ Ithakicopter UDP ------------------ */
        printASCII("src/ascii/copter.txt");

        try {
          socket = new DatagramSocket(48078);
        } catch (Exception x) {
          x.printStackTrace();
        }

        copterWelcome();

        try (FileWriter writerCopter =
                 new FileWriter(new File("logs/copter_info.txt"))) {
          writerCopter.write("Info Ithakicopter app:\n" + LocalDateTime.now() +
                             "\n");
          writerCopter.write("MOTOR ALTITUDE TEMPERATURE PRESSURE");

          while (System.in.available() == 0) {
            Copter.udpTelemetry(socket, hostAddress, serverPort, writerCopter);
          }

          writerCopter.write(LocalDateTime.now() + "\n");
        } catch (Exception x) {
          x.printStackTrace();
        }
        break;

      case "8":
        /* ---------------------- Autopilot --------------------- */
        printASCII("src/ascii/auto.txt");

        try (Socket socketAuto = new Socket(hostAddress, 38048)) {
          socket = new DatagramSocket(48078);
          int lowerBound = 160;
          int higherBound = 190;
          Copter.autopilot(socket, hostAddress, serverPort, socketAuto,
                           Math.min(200, Math.max(150, lowerBound)),
                           Math.min(200, Math.max(150, higherBound)));
        } catch (Exception x) {
          x.printStackTrace();
          ;
        }

        break;

      case "9":
        /* ---------------------- HTTPS TCP --------------------- */
        printASCII("src/ascii/https.txt");

        try (Socket httpsSocket = new Socket(hostAddress, 80)) {
          https(httpsSocket);
        } catch (Exception x) {
          x.printStackTrace();
        }

        break;

      case "10":
        /* ------------------- IthakicopterTCP ------------------ */
        printASCII("src/ascii/copter_tcp.txt");

        int target = 180;
        for (int i = 0; i < 10; i++) {
          System.out.println(
              new String(Copter.tcpTelemetry(hostAddress, target)));
        }

        break;

      case "11":
        /* ------------------- Vehicle OBD TCP ------------------ */
        printASCII("src/ascii/obd_tcp.txt");

        try (Socket socketVehicle = new Socket(hostAddress, 29078);
             FileWriter writerVehicleInfo =
                 new FileWriter(new File("logs/car_info.txt"));
             FileWriter writerVehicleData =
                 new FileWriter(new File("logs/car_telemetry.txt"))) {

          writerVehicleInfo.write("Info Vehicle app:\n" + LocalDateTime.now() +
                                  "\n");

          final int minutes = 2;
          final int secondsPerMinute = 60;
          final int timeInterval = minutes * secondsPerMinute;
          float engineTime = 0;
          while (engineTime < timeInterval) {
            engineTime = Obd.tcpTelemetry(socketVehicle, writerVehicleData);
            System.out.println("The engine run time is " + engineTime + "\n");
          }

          writerVehicleInfo.write(LocalDateTime.now() + "\n");
        } catch (Exception x) {
          x.printStackTrace();
        }

        break;

      default:
        System.out.println(
            "Please provide a valid input. If you want to exit then press Control-C.\n");
        flag = 0;
      }
    } while (flag == 0);

    /* ------------------ Close streams ----------------- */
    if (socket != null) {
      try {
        socket.close();
        in.close();
      } catch (Exception x) {
        x.printStackTrace();
      }
    }
    System.out.println("\nShuting down UDP sockets...");

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
      x.printStackTrace();
    }
  }

  private static void copterWelcome() {
    System.out.println(
        "For Ithakicopter UDP telemetry you need to open ithakicopter.jar");
    System.out.print("Did you open it? If yes press ENTER to continue");
    try {
      System.in.read();
      Thread.sleep(1000); // pause a bit to catch up with the user
      System.out.println("Press ENTER to exit");
      Thread.sleep(1000);
    } catch (Exception x) {
      x.printStackTrace();
    }
  }

  /**
   * Print welcome screen ASCII with CYAN color
   */
  private static void printWelcome() {
    // windows users may be not able to view colors on terminal
    final String ANSI_CYAN = "\u001B[36m";
    final String ANSI_RESET = "\u001B[0m";

    try (Scanner input = new Scanner(new File("src/ascii/welcome.txt"))) {
      while (input.hasNextLine()) {
        System.out.print(ANSI_CYAN); // add some color!
        System.out.print(input.nextLine());
        System.out.println(ANSI_RESET);
      }
      System.out.println();
      System.out.print("Press ENTER to continue");
      System.in.read(); // pause a little bit to enjoy the view

    } catch (Exception x) {
      x.printStackTrace();
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
      x.printStackTrace();
    }
  }
}
