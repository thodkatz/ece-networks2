package applications;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class Echo {

    /*
     * UDP TX/RX Echo application with delay 
     *
     * WARNING: It doesn't close the DatagramSocket. You should do it manually if it is desired after the call of the function. 
     *
     * @param requestCode If request code is set to E000 then the execute will have no delay for the RX 
     */
    public static void execute(DatagramSocket socket, InetAddress hostAddress, int serverPort, String requestCode) {
        //System.out.println("\n--------------------Echo application--------------------");

        if (requestCode.equals("E0000")) System.out.println("Delay: OFF"); 
        else if (requestCode.length()>5) System.out.println("Mode: Temperature\nDelay: OFF");
        else System.out.println("Delay: OFF"); 
 
        byte[] txbuffer = requestCode.getBytes();
        byte[] rxbuffer = new byte[128];
        try {
            socket.setSoTimeout(3000);
            DatagramPacket sendPacket = new DatagramPacket(txbuffer, txbuffer.length, hostAddress, serverPort);
            DatagramPacket receivePacket= new DatagramPacket(rxbuffer, rxbuffer.length);

            // ACTION
            socket.send(sendPacket);
            System.out.println("The request code is: "+ requestCode + "\nThe destination port is: " + serverPort + "\nMy listening port (clientPort): " + socket.getLocalPort());
            long timeBefore = System.currentTimeMillis();
            //System.out.println("My system time, when the request is sent, is: " + timeBefore);
            
            // LISTEN
            socket.receive(receivePacket);
            long timeAfter = System.currentTimeMillis();
            System.out.println("The time required to reveive a packet is: " + (timeAfter - timeBefore)/(float)1000 + " seconds"); 
            //System.out.println("The port that opened ithaki to send the request is : " + receivePacket.getPort() + " and the address of ithaki is: " + receivePacket.getAddress());
            String message = new String(receivePacket.getData(), StandardCharsets.US_ASCII); // convert binary to ASCI
            System.out.println("Ithaki responded with: " + message);
        }
        catch (Exception x) {
            // x.printStackTrace(); // a more detailed diagnostic call
            System.out.println(x);
            System.out.println("Something went wrong about Echo application mode");
        }
    }
}
