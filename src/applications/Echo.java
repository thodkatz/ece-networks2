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
    static public void execute(DatagramSocket socket, InetAddress hostAddress, int serverPort, int clientPort, String requestCode) {
        //System.out.println("\n--------------------Echo application--------------------");

        if (requestCode.equals("E0000")) {
            System.out.println("Delay: OFF");
        } 
        else {
            System.out.println("Delay: ON");
        }

        //DatagramSocket socket = null; 
        try {
            //socket = new DatagramSocket(clientPort);
            byte[] txbuffer = requestCode.getBytes();
            socket.setSoTimeout(3000);
            byte[] rxbuffer = new byte[1024];
            DatagramPacket sendPacket = new DatagramPacket(txbuffer, txbuffer.length, hostAddress, serverPort);
            DatagramPacket receivePacket= new DatagramPacket(rxbuffer, rxbuffer.length);

            //for(int i = 0; i<=4 ; i++) {
            byte[] data = new byte[1024]; 
            // ACTION
            socket.send(sendPacket);
            System.out.println("The request code is: "+ requestCode + "\nThe destination port is: " + serverPort + "\nMy listening port (clientPort): " + socket.getLocalPort());

            //requestCode = "E0000"; // disable server lag to respond
            //txbuffer = requestCode.getBytes();
            //sendPacket.setData(txbuffer, 0, txbuffer.length);

            long timeBefore = System.currentTimeMillis();
            System.out.println("My system time, when the request is sent, is: " + timeBefore);
            // LISTEN
            socket.receive(receivePacket);
            long timeAfter = System.currentTimeMillis();
            System.out.println("My system time, when the response received, is: " + timeAfter + " . So the time required to reveive a packet is: " + (timeAfter - timeBefore)/(float)1000 + " seconds"); 
            //System.out.println("The port that opened ithaki to send the request is : " + receivePacket.getPort() + " and the address of ithaki is: " + receivePacket.getAddress());
            data = receivePacket.getData();
            String message0 = new String(data, StandardCharsets.US_ASCII); // convert binary to ASCI
            System.out.println("Ithaki responded with: " + message0);
            //}

            // bracket are supposed to close the socket
            //socket.close();
            //socket.disconnect();
        }
        catch (Exception x) {
            System.out.println(x + ". Something went wrong about Echo application mode: delay");
        }
        finally {
            //socket.close(); // be sure to close the socket
        }
    }
}

// UDP TX/RX Echo application with delay
// static public void executeNoDelay(InetAddress hostAddress, int serverPort, int clientPort, String requestCode) {
//     try {
//         // send "E0000" to request no delay to the next packets 
//         DatagramSocket socket = new DatagramSocket(clientPort);  
//         socket.setSoTimeout(2500);
//         byte[] txbuffer = "E000".getBytes();
//         DatagramPacket sendPacket = new DatagramPacket(txbuffer, txbuffer.length, hostAddress, serverPort);
//         socket.send(sendPacket);
//         socket.close();

//         executeDelay(hostAddress, serverPort, clientPort, requestCode);
//     }
//     catch (Exception x) {
//         System.out.println(x + ". Something went wrong about Echo application mode: no delay");
//     }

// }
// }
