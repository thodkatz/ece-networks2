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
    public static String udpTelemetry(DatagramSocket socket, InetAddress hostAddress, int serverPort) {
        // TX
        // open ithakicopter.jar


        //RX only
        byte[] rxbuffer = new byte[128];
        DatagramPacket receivePacket = new DatagramPacket(rxbuffer, rxbuffer.length);

        long timeBefore = System.currentTimeMillis();
        String telemetry = new String();
        try{
            socket.setSoTimeout(3000);
            socket.receive(receivePacket);
            telemetry = new String(rxbuffer, StandardCharsets.US_ASCII);
            //System.out.print("Time repsonse: " + (System.currentTimeMillis() - timeBefore)/(float)1000);
            System.out.println("Received data: " + telemetry);
        }
        catch (Exception x) {
            System.out.println(x);
            System.out.println("RX UDP ithakicopter failed");
        }
        return telemetry; 

    }

    public static String tcpTelemetry(Socket socket, int target) {
        String data = "";
        try {                                                                                                                
            InputStream in = socket.getInputStream(); 
            OutputStream out= socket.getOutputStream(); 
            BufferedReader bf = new BufferedReader(new InputStreamReader(in)); // wrapper on top of the wrapper as java docs recommends

            String command = "AUTO FLIGHTLEVEL=" + target + " LMOTOR=" + target + " RMOTOR=" + target +  " PILOT \r\n";
            //System.out.print("Request: " + command);
            out.write(command.getBytes());
            out.flush();

            data = bf.readLine();
            System.out.println(data);
        }
        catch (Exception x) {
            System.out.println(x);
            System.out.println("Oops... Ithakicopter TCP failed");

        }
        return data;
    }    

    public static void autopilot(DatagramSocket listen, InetAddress hostAddress, int serverPort, Socket send, int lowerBound, int higherBound) throws Exception{

        lowerBound = Math.min(lowerBound, higherBound);
        lowerBound = Math.max(lowerBound, higherBound);

        int target = (lowerBound + higherBound)/2;
        int motor = -1;

        OutputStream out= send.getOutputStream(); 

        System.out.println("AUTOPILOT: ON");
        System.out.println("You need to open ithakicopter.jar");
        System.out.println("Press Control-C to exit...");
        Thread.sleep(1000);
        for (;;) {
            if (motor<lowerBound || motor>higherBound) { 
                System.out.println("Now I will send and listen via TCP");
                String command = "AUTO FLIGHTLEVEL=" + target + " LMOTOR=" + target + " RMOTOR=" + target +  " PILOT \r\n";
                out.write(command.getBytes());
                out.flush();
                //tcpTelemetry(send, target);
                }

                System.out.println("Now I will listen via UDP");
                String telemetry = Copter.udpTelemetry(listen, hostAddress, serverPort);
                System.out.println(telemetry);
                //String[] tokens = telemetry.split("ALTITUDE=");
                String[] tokens = telemetry.split("LMOTOR=");
                //for (String i : tokens) {
                    //System.out.println(i);
                //}
                motor = Integer.parseInt(tokens[1].substring(0,3)); // get motor values 

                System.out.println(motor);
        }
    }

}
