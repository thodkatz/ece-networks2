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


         // preamble
        byte[] clientIP = { (byte)192, (byte)168,  (byte)1, (byte)20};
        byte[] hostIP = { (byte)155, (byte)207,  (byte)18, (byte)208};
        InetAddress clientAddress = InetAddress.getByAddress(clientIP);
        InetAddress hostAddress = InetAddress.getByAddress(hostIP);
        int serverPort = 38011;
        int clientPort = 48011;
        String requestCodeEcho = "E0831";
        String requestCodeImage = "M6147UDP=1024";
        String requestCodeSound = "A0352"; 
        String requestCodeCopter = "Q6734"; 
        String requestCodeVehicle = "V0982"; 
        
        DatagramSocket socket = new DatagramSocket(clientPort);       


        int flag =1; // control user input
        do {
        System.out.println("Please enter a number (1-9) that correspond to the application you want to run. Available options are:\n1) Echo with delay\n2) Echo no delay\n3) Temperature\n4) Image\n5) Music\n6) Vehicle UDP\n7) Ithakicopter UDP\n8) Autopilot\n9) HTTPS TCP\n10) Ithakicopter TCP\n11) Vehicle TCP");
        String choiceApp = (new Scanner(System.in)).nextLine();

        //checkArguements(args); // check the validity of command line arguement
        

        switch (choiceApp) {
case "1":

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
        socket.close();
break;

case "2":
        input = new Scanner(new File("stamps/echo.txt"));
        while (input.hasNextLine())
        {
            System.out.println(input.nextLine());
        }
        Thread.sleep(1500); // pause a little bit to enjoy the view

        // no delay
        for (int i = 0; i < 5; i++) {
             Echo.execute(socket, hostAddress, serverPort, "E0000");
             System.out.println();
        }
        socket.close();
break;



case "3":
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
        socket.close();
break;

case "4":
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
        socket.close();
break;

case "5":
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
        socket.close();
break;

case "6":
        /* --------------------------- Vehicle OBD UDP--------------------------- */
        input = new Scanner(new File("stamps/obd.txt"));
        while (input.hasNextLine())
        {
            System.out.println(input.nextLine());
        }
        Thread.sleep(1500); // pause a little bit to enjoy the view

        Obd.udpTelemetry(socket, hostAddress, serverPort, requestCodeVehicle);
        socket.close();
break;

case "7":
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
            Copter.udpTelemetry(socket, hostAddress, serverPort);

        }
        socket.close();
break;

case "8":
        /* --------------------------- Autopilot --------------------------- */
        // The reasons probably this fails is because we keep the stream inactive and server probably close the connection, getting broken pipe
        socket = new DatagramSocket(48078);
        Socket socketAuto = new Socket(hostAddress, 38048);
        input = new Scanner(new File("stamps/auto.txt"));
        while (input.hasNextLine())
        {
            System.out.println(input.nextLine());
        }
        Thread.sleep(1500); // pause a little bit to enjoy the view

        int lowerBound = 170;
        int higherBound = 180;
        Copter.autopilot(socket, hostAddress, serverPort, socketAuto, Math.min(200, Math.max(150, lowerBound)), Math.min(200, Math.max(150, higherBound)));
        socketAuto.close();
break;



case "9":
        /* --------------------------- HTTPS TCP--------------------------- */
        Socket httpsSocket = new Socket(hostAddress, 80);
        input = new Scanner(new File("stamps/https.txt"));
        while (input.hasNextLine())
        {
            System.out.println(input.nextLine());
        }
        Thread.sleep(1500); // pause a little bit to enjoy the view

        https(httpsSocket);
        httpsSocket.close();
break;

case "10":
        /* --------------------------- Ithakicopter TCP--------------------------- */
        // can we use 38098? If we open the website we see that this is the port opened
        Socket socketCopter = new Socket(hostAddress, 38048);
        input = new Scanner(new File("stamps/copter_tcp.txt"));
        while (input.hasNextLine())
        {
            System.out.println(input.nextLine());
        }
        Thread.sleep(1500); // pause a little bit to enjoy the view

        int target = 180;
        for (int i = 0; i<10; i++) {
        Copter.tcpTelemetry(socketCopter, target);
        }

        socketCopter.close();
break;

case "11":
        /* --------------------------- Vehicle OBD TCP--------------------------- */
        Socket socketVehicle = new Socket(hostAddress, 29078);
        input = new Scanner(new File("stamps/obd_tcp.txt"));
        while (input.hasNextLine())
        {
            System.out.println(input.nextLine());
        }
        Thread.sleep(1500); // pause a little bit to enjoy the view

        for (int i = 0; i < 5; i++) {
            Obd.tcpTelemetry(socketVehicle);
        }
        socketVehicle.close();
break;


default:
    System.out.println("Please provide a valid input. If you want to exit then press Control-C.\n");
    flag = 0;
 
    } // end switch   
} while(flag == 0);


        /* --------------------------- Close UDP sockets --------------------------- */
        if (!socket.isClosed()) {
            socket.close();
            System.out.println("\nShuting down UDP sockets...");
        }


        System.out.println("\nx--------------------Hooray! Java application finished successfully!--------------------x");

    }
    

    private static void https(Socket socket) {
        try {
            InputStream in = socket.getInputStream(); // what I receive from the server
            OutputStream out = socket.getOutputStream(); // what i send to the server

            long timeBefore = System.currentTimeMillis();
            out.write("GET /netlab/hello.html HTTP/1.0\r\nHost: ithaki.eng.auth.gr:80\r\n\r\n".getBytes());

            byte[] inputBuffer = in.readAllBytes();
            String message = new String(inputBuffer, StandardCharsets.US_ASCII);
            System.out.println("Ithaki responded via TCP with: \n" + message);
            System.out.println("Time response: " + (System.currentTimeMillis() - timeBefore)/(float)1000 + " seconds");

            socket.close();
        }
        catch (Exception x) {
            System.out.println(x + "TCP application failed");
        }

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
