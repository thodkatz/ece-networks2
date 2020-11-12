// it is considered in general a bad practise to use asterisks to import all the classes
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.lang.System;
import java.awt.Desktop;
import javax.sound.sampled.*;
import java.lang.Math.*;
import java.util.Arrays;
import java.util.Scanner;

import applications.*;

class UserApplication {

    // TODO create a script that is scraping from ithaki website the request code and the ports.

    public static void main (String[] args) throws Exception {


        // windows users may be not able to view colors on terminal
        final String ANSI_CYAN = "\u001B[36m";
        final String ANSI_RESET = "\u001B[0m";

        // print welcome text
        Scanner input = new Scanner(new File("stamps/welcome.txt"));
        while (input.hasNextLine())
        {
            System.out.print(ANSI_CYAN); // add some color!
            System.out.print(input.nextLine());
            System.out.println(ANSI_RESET);
        }
        System.out.println();
        System.out.print("Press ENTER to continue"); 
        System.in.read();// pause a little bit to enjoy the view

        checkArguements(args); // check the validity of command line arguement

        // preamble
        byte[] clientIP = { (byte)192, (byte)168,  (byte)1, (byte)20};
        byte[] hostIP = { (byte)155, (byte)207,  (byte)18, (byte)208};
        InetAddress clientAddress = InetAddress.getByAddress(clientIP);
        InetAddress hostAddress = InetAddress.getByAddress(hostIP);
        int serverPort = 38025;
        int clientPort = 48025;
        String requestCodeEcho = "E6633";
        String requestCodeImage = "M2940UDP=1024";
        String requestCodeSound = "A7687"; 
        String requestCodeCopter = "Q5647"; 
        String requestCodeVehicle = "V4303"; 
        
        DatagramSocket socket = new DatagramSocket(clientPort);

        /* --------------------------- Echo --------------------------- */
        input = new Scanner(new File("stamps/echo.txt"));
        while (input.hasNextLine())
        {
            System.out.println(input.nextLine());
        }
        Thread.sleep(1500); // pause a little bit to enjoy the view

        for (int i = 0; i < 5; i++) {
             Echo.execute(socket, hostAddress, serverPort, requestCodeEcho);
             System.out.println();
        }

        // no delay
        for (int i = 0; i < 5; i++) {
             Echo.execute(socket, hostAddress, serverPort, "E0000");
             System.out.println();
        }

        /* --------------------------- Temperature --------------------------- */
        input = new Scanner(new File("stamps/temp.txt"));
        while (input.hasNextLine())
        {
            System.out.println(input.nextLine());
        }
        Thread.sleep(1500); // pause a little bit to enjoy the view

        for (int i = 0; i < 5; i++) {
             Echo.execute(socket, hostAddress, serverPort, requestCodeEcho + "T00");
             System.out.println();
        }

        /* --------------------------- Image --------------------------- */
        input = new Scanner(new File("stamps/image.txt"));
        while (input.hasNextLine())
        {
            System.out.println(input.nextLine());
        }
        Thread.sleep(1500); // pause a little bit to enjoy the view

        for (int i = 0; i < 2; i++) {
             Media.image(socket, hostAddress, serverPort, requestCodeImage);
             System.out.println();
        }

        /* --------------------------- Audio --------------------------- */
        input = new Scanner(new File("stamps/audio.txt"));
        while (input.hasNextLine())
        {
            System.out.println(input.nextLine());
        }
        Thread.sleep(1500);

        String numAudioPackets = "200";
        String[] type = {"F", "T"};
        String[] encoding = {"AQ", ""};
        String completeRequest = requestCodeSound + encoding[0] + type[0] + numAudioPackets;
        Media.audio(socket, hostAddress, serverPort, completeRequest);
        System.out.println();

        /* --------------------------- Vehicle OBD UDP--------------------------- */
        input = new Scanner(new File("stamps/obd.txt"));
        while (input.hasNextLine())
        {
            System.out.println(input.nextLine());
        }
        Thread.sleep(1500); // pause a little bit to enjoy the view
        Obd.udpTelemetry(socket, hostAddress, serverPort, requestCodeVehicle);


        /* --------------------------- Ithakicopter UDP--------------------------- */
        socket = new DatagramSocket(48078);
        input = new Scanner(new File("stamps/copter.txt"));
        while (input.hasNextLine())
        {
            System.out.println(input.nextLine());
        }
        Thread.sleep(1500);

        System.out.println("For Ithakicopter UDP telemetry you need to open ithakicopter.jar");
        System.out.print("Did you open it? If yes press ENTER to continue");
        System.in.read();
        Thread.sleep(1000); // pause a bit to catch up with the user
        System.out.println("Press ENTER to exit");
        Thread.sleep(1000); 
        while (System.in.available() == 0) {
            Copter.udpTelemetry(socket, hostAddress, serverPort, requestCodeVehicle);

        }


        /* --------------------------- Close UDP sockets --------------------------- */
        if (!socket.isClosed()) {
            socket.close();
            System.out.println("\nShuting down UDP sockets...");
        }



        /* --------------------------- Vehicle OBD TCP--------------------------- */
        Socket tcpSocket = new Socket(hostAddress, 29078);
        input = new Scanner(new File("stamps/obd_tcp.txt"));
        while (input.hasNextLine())
        {
            System.out.println(input.nextLine());
        }
        Thread.sleep(1500); // pause a little bit to enjoy the view
        Obd.tcpTelemetry(tcpSocket);


        //System.out.println("\n--------------------Ithakicopter-UDP receive--------------------");

        //// open the jar app for the send

        //DatagramSocket socketCopter = new DatagramSocket(48078);
        //socketCopter.setSoTimeout(2500);

        //byte[] rxbufferCopter = new byte[128];
        //receivePacket.setData(rxbufferCopter, 0, rxbufferCopter.length);

        //for (int i=0; i<4;i++) {
        //    try {
        //        socketCopter.receive(receivePacket);
        //        String message = new String(receivePacket.getData(), StandardCharsets.US_ASCII); // convert binary to ASCI
        //        System.out.println("Ithaki responded with: " + message);
        //    }
        //    catch (Exception x) {
        //        System.out.println(x + ". Ithakicopter application failed");
        //    }
        
        //}
        //socketCopter.close();


        //System.out.println("\n--------------------TCP------------------------------");


        //try {
        //    serverPort = 80;
        //    Socket socketTCP = new Socket(hostAddress, serverPort); // establish connection
        //    socketTCP.setSoTimeout(3000);
        //    InputStream in = socketTCP.getInputStream(); // what I receive from the server
        //    OutputStream out = socketTCP.getOutputStream(); // what i send to the server

        //    out.write("GET /netlab/hello.html HTTP/1.0\r\nHost: ithaki.eng.auth.gr:80\r\n\r\n".getBytes());
        //    byte[] inputBuffer = in.readAllBytes();
        //    String message = new String(inputBuffer, StandardCharsets.US_ASCII);
        //    System.out.println("Ithaki responded via TCP with: " + message);

        //    socketTCP.close();
        //}
        //catch (Exception x) {
        //    System.out.println(x + "TCP application failed");
        //}


        //System.out.println("\n--------------------Ithakicopter-TCP------------------------------");

        //for (int i=0;i<5;i++){
        //    try {
        //        serverPort = 38048;
        //        Socket socketTCP = new Socket(hostAddress, serverPort); // establish connection
        //        //socketTCP.setSoTimeout(10000);
        //        InputStream in = socketTCP.getInputStream(); // what I receive from the server
        //        OutputStream out = socketTCP.getOutputStream(); // what i send to the server

        //        // if you want to repeat the process you need each time to redefine the socket. Loop the out.write and receive will not work as intended
        //        // server will respond at the moment of the request, so we need to send more than one to handle the lag between ithakicopter and server response
        //        out.write("AUTO FLIGHTLEVEL=200 LMOTOR=180 RMOTOR=180 PILOT \r\n".getBytes()); 
        //        out.write("AUTO FLIGHTLEVEL=200 LMOTOR=180 RMOTOR=180 PILOT \r\n".getBytes());
        //        out.write("AUTO FLIGHTLEVEL=200 LMOTOR=180 RMOTOR=180 PILOT \r\n".getBytes());
        //        System.out.println("Created TCP socket and set output stream... Waiting for response");
        //        timeBefore = System.currentTimeMillis();
        //        byte[] inputBuffer = in.readAllBytes();
        //        System.out.println("Ithaki TCP time response: " + (System.currentTimeMillis()-timeBefore)/(float)1000 + " seconds");
        //        String message = new String(inputBuffer, StandardCharsets.US_ASCII);
        //        System.out.println("Ithaki responded via TCP with: \n" + message);
        //        socketTCP.close();
        //    }
        //    catch (Exception x) {
        //        System.out.println(x + "Ithakicopter TCP application failed");
        //    }
        //}


        System.out.println("\n---------------------Vehicle Diagnostics TCP-------------------------------");
        
        String[] carHeader = {"01 1F\r", "01 0F\r", "01 11\r", "01 0C\r", "01 0D\r", "01 05\r"};
        serverPort = 29078;
            try {                                                                                                                
                Socket socketCar = new Socket(hostAddress, serverPort); // establish connection
                InputStream inCar = socketCar.getInputStream(); // what I receive from the server
                OutputStream outCar = socketCar.getOutputStream(); // what i send to the server
                BufferedReader in = new BufferedReader(new InputStreamReader(inCar)); // wrapper on top of the wrapper as java docs recommends

                for (int i = 0; i < carHeader.length; i++) {
                    outCar.write(carHeader[1].getBytes());
                    long timeBefore = System.currentTimeMillis();
                    System.out.println("Created TCP socket and set output stream... Waiting for response");

                    System.out.println("Header: " + carHeader[i]); 
                    String data = in.readLine();
                    System.out.println("Ithaki responded via TCP with: \n" + data);
                    System.out.println("Ithaki TCP time response: " + (System.currentTimeMillis()-timeBefore)/(float)1000 + " seconds");

                    // parse the received info
                    String byte1 = data.substring(6,8);
                    // how to convert hexadecimal string to int?
                    int first = Integer.decode(byte1);
                    System.out.print("Parsing the data: 1st byte: " + byte1);
                    String byte2 = "";
                    int second = 0;
                    if (data.length()>8) {
                        byte2 = data.substring(9,11);
                        second = Integer.decode(byte2);
                        System.out.print(", 2nd byte: " + byte2);
                    }
                    System.out.println();


                    // calculate formula
                    switch (i) {
                        case 0:
                            int engineRunTime = first*256 + second;
                            System.out.println("Engine run time: " + engineRunTime);
                            break;
                        case 1:
                            int intakeAirTemp = first - 40;
                            System.out.println("Intake Air Temperature: " + intakeAirTemp);
                            break;

                        case 2:
                            float throttlePos = (first*100)/(float)255;
                            System.out.println("Throttle position: " + throttlePos);
                            break;
                        case 3:
                            float engineRpm = ((first*256) + second)/(float)4;
                            System.out.println("Engine RPM: " + engineRpm);
                            break;
                        case 4:
                            int speed = first;
                            System.out.println("Vehicle speed: " + speed);
                            break;
                        case 5:
                            int coolantTemp = first -40;
                            System.out.println("Coolant Temperature: " + coolantTemp);
                            break;

                        default:
                            System.out.println("Something went wrong calculating formual for vehicle stats");
                    }

                    System.out.println();
                }
                socketCar.close();
                inCar.close();
                outCar.close();
            }
            catch (Exception x) {
                System.out.println(x); 
                System.out.println("Vehicle TCP application failed");
            }


        System.out.println("\nx--------------------Hooray! Java application finished successfully!--------------------x");

        }

    private static void checkArguements (String[] args) {
        if (args.length == 2) {
            System.out.println("Command line arguements: This is the first " + args[0] + " " + "and this is the second " + args[1]); 
        }
        String[] directionOptions = {"L", "D", "U", "R"};
        int flag = 0;
        for (String i : directionOptions) {
            if (i.equals(args[0])) {
                flag = 1;
                System.out.println("Direction: " + args[0]);
                break;
            }	
        }
        if (flag == 0) {
            System.out.println("Try again, wrong direction. Available options are: L, R, U, D");
            return;
        }
        if (Integer.parseInt(args[1])>=1 && Integer.parseInt(args[1])<=100) {
            System.out.println("How many times to repeat the movement of the camera on that direction? " + Integer.parseInt(args[1]));
        }
        else {
            System.out.println("Invalid input. Range should be 1-100");
            return;
        }  
    }

    }
