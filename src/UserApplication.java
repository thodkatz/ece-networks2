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
import java.time.LocalDateTime;

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
        int serverPort = 38004;
        int clientPort = 48004;
        String requestCodeEcho = "E4047";
        String requestCodeImage = "M6495UDP=1024";
        String requestCodeSound = "A1275"; 
        String requestCodeCopter = "Q0952"; 
        String requestCodeVehicle = "V2444"; 
        
        DatagramSocket socket = new DatagramSocket(clientPort);       
        long timeBefore = 0;


        int flag =1; // control user input
        do {
        System.out.println("\nPlease enter a number (1-11). Available options are:\n1) Echo with delay\n2) Echo no delay\n3) Temperature\n4) Image\n5) Music\n6) Vehicle UDP\n7) Ithakicopter UDP\n8) Autopilot\n9) HTTPS TCP\n10) Ithakicopter TCP\n11) Vehicle TCP");
        String choiceApp = (new Scanner(System.in)).nextLine();
        //String choiceApp = "5";

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

        File info = new File("logs/echo_info_delay.txt");
        FileWriter writerInfo = new FileWriter(info);
        writerInfo.write("Info:\n" + "The request code is " + requestCodeEcho + "\n");
        writerInfo.write("Tic: " + LocalDateTime.now() + "\n");

        File fileSamples = new File("logs/echo_samples_delay.txt");
        FileWriter writerSamples = new FileWriter(fileSamples);
        
        File fileThroughput = new File("logs/echo_throughput_delay.txt");
        FileWriter writerThroughput = new FileWriter(fileThroughput);

        File fileRto = new File("logs/rto.txt");
        FileWriter writerRto = new FileWriter(fileRto);

        timeBefore = System.currentTimeMillis();
        long tic[] = new long[8];
        //long toc[] = new long[8];
        int cumsum[] = new int[8];  // cumulative sum
        float throughput[] = new float[8];  // worst case we need 8 elements to store data 
        int count8sec[] = new int[8]; // keep track how many 8 seconds have passed
        for(int i = 0; i<8; i++) {
            tic[i] = timeBefore + i*1000; // move per second
        }

        double rtts = 0;
        double rttd = 0;
        double rto = 0;
        int isFirst = 1;

        while ((System.currentTimeMillis() - timeBefore) < 60000 * 4){
            long rtt = Echo.execute(socket, hostAddress, serverPort, requestCodeEcho);
            writerSamples.write(rtt + "\n");

            // throughput moving average
            
             for(int i = 0; i<8; i++) {
                 //System.out.println("The element " + i + " has tic: " + tic[i]);
                 long toc = System.currentTimeMillis() - tic[i];
                 System.out.println("The element " + i + " has toc: " + toc);
                 if (toc < 8000 && toc > 0) { 
                     cumsum[i] += 32*8; // assume no timeouts during the measurements
                     System.out.println("Cumsum: " + cumsum[i]);
                 }
                 else if(toc > 8000){
                     count8sec[i]++;
                     tic[i] = count8sec[i]*8000 + timeBefore + i*1000;
                     throughput[i] = cumsum[i]/(float)8;
                     System.out.println("I will flush " + cumsum[i] + " cumsum");
                     System.out.println("The throughput is: " + throughput[i]);

                     writerThroughput.write(throughput[i]+ "\n");


                     cumsum[i] = 0; // let's start again for the next 8 seconds
                 }
             } 


             // Retransmission timeout
             
             // init values
             if (isFirst == 1) { 
                 rtts = rtt;
                 rttd = rtt/2;
                 rto = 1; // according to rfc
                 writerRto.write("RTT SRTT RTTd RTO\n");
             } 
             double temp = rtts;
             rtts = 0.875*temp + 0.125*rtt;   

             temp = rttd;
             rttd = 0.75*temp + 0.25*Math.abs(rtt - rtts);

             temp = rto;
             rto = rtts + 1.8*rttd;

             writerRto.write(rtt + " " + rtts + " " + rttd + " " + rto + "\n");

             System.out.println();
             isFirst = 0;
        }
        
        writerInfo.write("Toc: " + LocalDateTime.now());

        writerInfo.close();
        writerSamples.close();
        writerThroughput.close();
        writerRto.close();
        socket.close();
        break;

        case "2":
        input = new Scanner(new File("stamps/echo.txt"));
        while (input.hasNextLine())
        {
            System.out.println(input.nextLine());
        }
        Thread.sleep(1500); // pause a little bit to enjoy the view

        info = new File("logs/echo_info_no_delay.txt");
        writerInfo = new FileWriter(info);
        writerInfo.write("Info:\n" + "The request code is " + requestCodeEcho + "\n");
        writerInfo.write("Tic: " + LocalDateTime.now() + "\n");

        fileSamples = new File("logs/echo_samples_no_delay.txt");
        writerSamples = new FileWriter(fileSamples);

        fileThroughput = new File("logs/echo_throughput_no_delay.txt");
        writerThroughput = new FileWriter(fileThroughput);

        timeBefore = System.currentTimeMillis();
        tic = new long[8];
        //long toc[] = new long[8];
        cumsum = new int[8];  // cumulative sum
        throughput = new float[8];  // worst case we need 8 elements to store data 
        count8sec = new int[8];
        for(int i = 0; i<8; i++) {
            tic[i] = timeBefore + i*1000; // move per second
        }

        while ((System.currentTimeMillis() - timeBefore) < 60000 * 4){
            long value = Echo.execute(socket, hostAddress, serverPort, "E0000");
            writerSamples.write(value + "\n");

             // throughput moving average
             
             for(int i = 0; i<8; i++) {
                 //System.out.println("The element " + i + " has tic: " + tic[i]);
                 long toc = System.currentTimeMillis() - tic[i];
                 System.out.println("The element " + i + " has toc: " + toc);
                 if (toc < 8000 && toc > 0) { 
                     cumsum[i] += 32*8; // assume no timeouts during the measurements
                     System.out.println("Cumsum: " + cumsum[i]);
                 }
                 else if(toc > 8000){
                     count8sec[i]++;
                     tic[i] = count8sec[i]*8000 + timeBefore + i*1000;
                     throughput[i] = cumsum[i]/(float)8;
                     System.out.println("I will flush " + cumsum[i] + " cumsum");
                     System.out.println("The throughput is: " + throughput[i]);

                     writerThroughput.write(throughput[i]+ "\n");

                     cumsum[i] = 0; // let's start again for the next 8 seconds
                 }
             } 
             System.out.println();
        }

        writerInfo.write("Toc: " + LocalDateTime.now());

        writerInfo.close();
        writerSamples.close();
        writerThroughput.close();
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

        FileWriter writerTemp = new FileWriter(new File("logs/temp_info.txt")) ;
        writerTemp.write("Info Temperature app:\n" + LocalDateTime.now() + "\n");

        for (int i = 0; i < 1; i++) {
             Echo.execute(socket, hostAddress, serverPort, requestCodeEcho + "T00");
             System.out.println();
        }

        writerTemp.write(LocalDateTime.now() + "\n");
        writerTemp.close();
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

        for (int i = 0; i<4; i++) Echo.execute(socket, hostAddress, serverPort, requestCodeEcho);

        for (int i = 0; i < 1; i++) {
             Media.image(socket, hostAddress, serverPort, requestCodeImage + "CAM=PTZ");
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

        for (int i = 0; i<4; i++) Echo.execute(socket, hostAddress, serverPort, requestCodeEcho);


        String numAudioPackets = "500";
        String[] type = {"F", "T"};
        String[] encoding = {"AQ", ""};
        // assuming random choice of track
        String completeRequest = requestCodeSound + encoding[1] + type[1] + numAudioPackets;

        File infoMusic = new File("logs/music_info_" + encoding[1] + type[1] + ".txt");
        FileWriter writerInfoMusic = new FileWriter(infoMusic);
        writerInfoMusic.write(LocalDateTime.now() + "\n");

        Media.audio(socket, hostAddress, serverPort, completeRequest);
        System.out.println();

        writerInfoMusic.write(LocalDateTime.now() + "\n");
        writerInfoMusic.close();
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

        FileWriter writerCopter = new FileWriter(new File("logs/copter_info.txt")) ;
        writerCopter.write("Info Ithakicopter app:\n" + LocalDateTime.now() + "\n");
        writerCopter.write("MOTOR ALTITUDE TEMPERATURE PRESSURE");


        for (int i = 0; i<4; i++) Echo.execute(socket, hostAddress, serverPort, requestCodeEcho);

        while (System.in.available() == 0) {
            Copter.udpTelemetry(socket, hostAddress, serverPort, writerCopter);

        }

        writerCopter.write(LocalDateTime.now() + "\n");
        writerCopter.close();
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

        int lowerBound = 160;
        int higherBound = 190;
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
        //Socket socketCopter = new Socket(hostAddress, 38048);
        input = new Scanner(new File("stamps/copter_tcp.txt"));
        while (input.hasNextLine())
        {
            System.out.println(input.nextLine());
        }
        Thread.sleep(1500); // pause a little bit to enjoy the view

        for (int i = 0; i<4; i++) Echo.execute(socket, hostAddress, serverPort, requestCodeEcho);

        int target = 180;
        for (int i = 0; i<20; i++) {
        System.out.println(new String(Copter.tcpTelemetry(hostAddress, target)));
        }

        //socketCopter.close();
        break;

        case "11":
        /* --------------------------- Vehicle OBD TCP--------------------------- */
        input = new Scanner(new File("stamps/obd_tcp.txt"));
        while (input.hasNextLine())
        {
            System.out.println(input.nextLine());
        }
        Thread.sleep(1500); // pause a little bit to enjoy the view

        for (int i = 0; i<4; i++) Echo.execute(socket, hostAddress, serverPort, requestCodeEcho);

        Socket socketVehicle = new Socket(hostAddress, 29078);

        FileWriter writerVehicleInfo = new FileWriter(new File("logs/car_info.txt")) ;
        writerVehicleInfo.write("Info Vehicle app:\n" + LocalDateTime.now() + "\n");
        FileWriter writerVehicleData = new FileWriter(new File("logs/car_telemetry.txt"));

        timeBefore = System.currentTimeMillis();
        while ((System.currentTimeMillis() - timeBefore) < 60000 * 4){
            Obd.tcpTelemetry(socketVehicle, writerVehicleData);
        }

        writerVehicleInfo.write(LocalDateTime.now() + "\n");
        writerVehicleInfo.close();
        writerVehicleData.close();
        socketVehicle.close();
        break;

        case "12":
        Socket foo = new Socket(hostAddress, 38048);
        OutputStream out = foo.getOutputStream();
        InputStream in = foo.getInputStream();
        InputStreamReader isr = new InputStreamReader(in);
        BufferedReader bf = new BufferedReader(isr);
        ByteArrayOutputStream bis = new ByteArrayOutputStream();
        
        // if use readAllBytes the InputStream is closed
        // actually readNBytes is quite weird to be honest
        out.write("AUTO FLIGHTLEVEL=100 LMOTOR=100 RMOTOR=100 PILOT \r\n".getBytes());
        String data = new String();
        while ((data = bf.readLine()) != null) {
            bis.write((data + "\n").getBytes());
        }
        data = new String(bis.toByteArray(), StandardCharsets.US_ASCII);
        System.out.println(data);
        break;

        case "13":
            
        // Major difference between this code snippet and the above is the reposnse time! Actually it is all about null and ending stream!
        Socket foo1 = new Socket(hostAddress, 38048);
        OutputStream out1 = foo1.getOutputStream();
        InputStream in1 = foo1.getInputStream();
        InputStreamReader isr1 = new InputStreamReader(in1);
        BufferedReader bf1 = new BufferedReader(isr1);
        
        // if use readAllBytes the InputStream is closed
        // actually readNBytes is quite weird to be honest
        for (int i = 0; i < 4; i++) {
        out1.write("AUTO FLIGHTLEVEL=100 LMOTOR=100 RMOTOR=100 PILOT \r\n".getBytes());
        //String data1 = new String(in1.readNBytes(427), StandardCharsets.US_ASCII);
        //System.out.println(data1);
        //System.out.println(bf1.readLine());
        //System.out.println(bf1.readLine());
        
        //for (int i = 0; i < 40; i++) {
        //    System.out.println(bf1.readLine());
        //}
        // In general we have a lag when reading if we are in the end of the stream

        //while(bf1.readLine() != null) {System.out.println(bf1.readLine());} // warning bf read is called two times
        // waiting to encounter null isn't good. Too much lag. Waiting if the stream is closed or not
        
        // why we do this? Ithaki when establishing this connection first send some introductory info and then the actual telemetry. So you need to handle streams in a proper way!
        if (i==0) {
            for (int l = 0; l < 14; l++) { // after some tinkering we have the data
                System.out.println(bf1.readLine());
            }
        }
        else {
            System.out.println(bf1.readLine());
        }
        }


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
