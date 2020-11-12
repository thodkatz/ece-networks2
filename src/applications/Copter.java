package applications;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class Copter {
    public static void udpTelemetry(DatagramSocket socket, InetAddress hostAddress, int serverPort, String requestCode) {
        // TX
        // open ithakicopter.jar
       
        
        //RX only
        byte[] rxbuffer = new byte[128];
        DatagramPacket receivePacket = new DatagramPacket(rxbuffer, rxbuffer.length);

        try{
            socket.setSoTimeout(3000);
            socket.receive(receivePacket);
            System.out.println("Ithaki responded with: " + new String(rxbuffer, StandardCharsets.US_ASCII));
        }
        catch (Exception x) {
            System.out.println(x);
            System.out.println("RX UDP ithakicopter failed");
        }

    }

    public static void tcpTelemetry(DatagramSocket socket, InetAddress hostAddress, int serverPort, String requestCode) {

    }
}
