package applications;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.Socket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class Obd {

    private static String[] header = {"01 1F", "01 0F", "01 11", "01 0C", "01 0D", "01 05"};


    public static void udpTelemetry(DatagramSocket socket, InetAddress hostAddress, int serverPort, String requestCode) {

        byte[] rxbuffer = new byte[16];
        DatagramPacket receivePacket = new DatagramPacket(rxbuffer, rxbuffer.length);

        for (int i = 0; i < header.length; i++) {

            // TX
            String completeCode = (requestCode + "OBD=" + header[i]);
            byte[] txbuffer = completeCode.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(txbuffer, txbuffer.length, hostAddress, serverPort);
            System.out.println("Complete request: " + completeCode);
            try {
                socket.send(sendPacket);	
            }
            catch (Exception x) {
                // x.printStackTrace(); // a more detailed diagnostic call
                System.out.println(x);
                System.out.println("OBD vehicle application TX failed");
            }
            long timeBefore = System.currentTimeMillis();

            // RX
            try{
                socket.setSoTimeout(3000);
                socket.receive(receivePacket);
                String message = new String(rxbuffer, StandardCharsets.US_ASCII);
                System.out.println("Ithaki responded via UDP with: " + message);
                System.out.println("Ithaki UDP time response: " + (System.currentTimeMillis()-timeBefore)/(float)1000 + " seconds");

                int[] values = parser(message);
                formula(values[0], values[1], header[i]);
            }
            catch (Exception x) {
                System.out.println(x);
                System.out.println("RX UDP vehicle failed");
            }
        }

    }

    public static void tcpTelemetry(Socket socket) {

        try {                                                                                                                
            InputStream in = socket.getInputStream(); 
            OutputStream out = socket.getOutputStream(); 
            BufferedReader bf = new BufferedReader(new InputStreamReader(in)); // wrapper on top of the wrapper as java docs recommends

            for (int i = 0; i < header.length; i++) {
                out.write((header[i] + "\r").getBytes());
                //out.flush();
                long timeBefore = System.currentTimeMillis();
                System.out.println("Created TCP socket and set output stream... Waiting for response");

                System.out.println("Header: " + header[i]); 
                String data = bf.readLine();
                System.out.println("Ithaki responded via TCP with: " + data);
                System.out.println("Ithaki TCP time response: " + (System.currentTimeMillis()-timeBefore)/(float)1000 + " seconds");
                
                int[] values = parser(data);
                formula(values[0], values[1], header[i]);
            }
        }
        catch (Exception x) {
            System.out.println(x);
            System.out.println("Oops... Vehicle OBD TCP failed");

        }
    }

    private static void formula(int first, int second, String header) {
        switch (header) {
            case "01 1F":
                int engineRunTime = first*256 + second;
                System.out.println("Engine run time: " + engineRunTime);
                break;
            case "01 0F":
                int intakeAirTemp = first - 40;
                System.out.println("Intake Air Temperature: " + intakeAirTemp);
                break;

            case "01 11":
                float throttlePos = (first*100)/(float)255;
                System.out.println("Throttle position: " + throttlePos);
                break;
            case "01 0C":
                float engineRpm = ((first*256) + second)/(float)4;
                System.out.println("Engine RPM: " + engineRpm);
                break;
            case "01 0D":
                int speed = first;
                System.out.println("Vehicle speed: " + speed);
                break;
            case "01 05":
                int coolantTemp = first -40;
                System.out.println("Coolant Temperature: " + coolantTemp);
                break;

            default:
                System.out.println("Something went wrong calculating formual for vehicle stats");

        } 
        System.out.println();
    }

    private static int[] parser(String data) {
        String byte1 = data.substring(6,8);
        // how to convert hexadecimal string to int?
        int first = Integer.parseInt(byte1, 16);
        System.out.print("Parsing the data: 1st byte: " + byte1 + " and as an integer: " + first);
        String byte2 = "";
        int second = 0;
        if (data.length()>8) {
            byte2 = data.substring(9,11);
            second = Integer.parseInt(byte2, 16);
            System.out.print(", 2nd byte: " + byte2 + " and as an integer: " + second);
        }
        System.out.println();

        int[] temp = {first, second};
        return temp;
    }
}
