package applications;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.Socket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.io.ByteArrayOutputStream;

public class Copter {
    public static void udpTelemetry(DatagramSocket socket, InetAddress hostAddress, int serverPort) {
        // TX
        // open ithakicopter.jar


        //RX only
        byte[] rxbuffer = new byte[128];
        DatagramPacket receivePacket = new DatagramPacket(rxbuffer, rxbuffer.length);

        long timeBefore = System.currentTimeMillis();
        try{
            socket.setSoTimeout(3000);
            socket.receive(receivePacket);
            System.out.print("Time repsonse: " + (System.currentTimeMillis() - timeBefore));
            System.out.println(". Received data: " + new String(rxbuffer, StandardCharsets.US_ASCII));
        }
        catch (Exception x) {
            System.out.println(x);
            System.out.println("RX UDP ithakicopter failed");
        }

    }

    public static void tcpTelemetry(Socket socket) {
        try {                                                                                                                
            InputStream in = socket.getInputStream(); 
            OutputStream out= socket.getOutputStream(); 
            BufferedReader bf = new BufferedReader(new InputStreamReader(in)); // wrapper on top of the wrapper as java docs recommends

            out.write("AUTO FLIGHTLEVEL=200 LMOTOR=180 RMOTOR=180 PILOT \r\n".getBytes());
            out.flush();
            long timeBefore = System.currentTimeMillis();
            //System.out.println("Created TCP socket and set output stream... Waiting for response");

            System.out.println(bf.readLine());
            //ByteArrayOutputStream completeData = new ByteArrayOutputStream();
            //String data = ""; 
            //while ((data = bf.readLine()) != null) {
            //        completeData.write((data + "\n").getBytes());
            //}
            //byte[] dataByte = completeData.toByteArray();
            //System.out.println("Ithaki responded via TCP with: \n" + new String(dataByte, StandardCharsets.US_ASCII));
            //System.out.println("Ithaki TCP time response: " + (System.currentTimeMillis()-timeBefore)/(float)1000 + " seconds");
            //completeData.close();
        }
        catch (Exception x) {
            System.out.println(x);
            System.out.println("Oops... Ithakicopter TCP failed");

        }
    }    

    public static void autopilot(Socket socket, int lowerBound, int higherBound) {

        lowerBound = Math.min(lowerBound, higherBound);
        lowerBound = Math.max(lowerBound, higherBound);

        // TX
        // Due to tcp overhead of ithakicopter connection, we will try to send tcp and receive by udp. Requirement to open the jar file
        // In other words, use tcp to send and get feedback from jar. UDP telemetry feels snappy. Or no?
        // RX

          try {                                                                                                                
            InputStream in = socket.getInputStream(); 
            OutputStream out= socket.getOutputStream(); 
            BufferedReader bf = new BufferedReader(new InputStreamReader(in)); // wrapper on top of the wrapper as java docs recommends


            int target = (higherBound + lowerBound)/2; // init value
            int level = target;
            int feedback = target;
            for (;;) {
                do {
                    String command = "AUTO FLIGHTLEVEL=" + level + "LMOTOR=" + level + " RMOTOR=" + level + " PILOT \r\n"; 
                    out.write(command.getBytes());

                    ByteArrayOutputStream completeData = new ByteArrayOutputStream();
                    String data = ""; 
                    while ((data = bf.readLine()) != null) {
                        completeData.write((data + "\n").getBytes());
                    }
                    byte[] dataByte = completeData.toByteArray();

                    // parse string
                    String[] tokens = (new String(dataByte, StandardCharsets.US_ASCII)).split("ALTITUDE=");
                    for (String t : tokens) {
                    System.out.println(t);
                    System.out.println("I PRINTED TOKEN");
                    }
                    feedback = Integer.parseInt(tokens[2].substring(0,3)); // get the altitude
                    System.out.println("My current feedback: " + feedback);
                } while (Integer.compare(feedback, target) != 0);
            }
        }
        catch (Exception x) {
            System.out.println(x);
            System.out.println("Oops... Ithakicopter TCP failed");

        }      
    }

}
