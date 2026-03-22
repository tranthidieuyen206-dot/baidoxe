package Server1;

import java.net.*;
import java.io.*;
import java.util.Hashtable;

public class NewServer1 {

    public static void log(String msg) {
        System.out.println("[Server1] " + msg);
    }

    // ================= SERVER HANDLER =================
    public static class Sv1 implements Runnable {

        Socket client;
        String serverName;
        int pos;
        int currentCircle;
        Hashtable<String, String> hash;

        RountingTable rount;
        static String MESSAGE = "";

        public Sv1(Socket client, String serverName, int pos, int curr, Hashtable<String, String> hash) {
            this.client = client;
            this.serverName = serverName;
            this.pos = pos;
            this.currentCircle = curr;
            this.hash = hash;
            this.rount = new RountingTable();

            new Thread(this).start();
        }

        @Override
        public void run() {
            runServer();
        }

        public void runServer() {
            try {
                String destName = client.getInetAddress().getHostName();
                int destPort = client.getPort();
                log("Connected from " + destName + ":" + destPort);

                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                DataOutputStream out = new DataOutputStream(client.getOutputStream());

                String inLine = in.readLine();
                MessageProcess re = new MessageProcess(inLine);

                String st = re.getStart();
                String je = re.getJeton();
                String lamport = re.getLamport();
                String name = re.getServerName();
                String type = re.getType();
                String action = re.getAction();
                String circle = re.getNumCircle();
                String message = re.getMessage();

                MESSAGE = message;

                log("Received:");
                log("start=" + st + ", type=" + type + ", action=" + action + ", msg=" + message);

                int start = Integer.parseInt(st);
                int act = Integer.parseInt(action);

                // ================= CLIENT REQUEST =================
                if (start == 0) {
                    start++;
                    Database db = new Database();
                    ProcessData dt = new ProcessData(message);

                    String reply = "OK";

                    if (message.endsWith("VIEW")) {
                        reply = db.getData();
                    }
                    if (message.endsWith("SET") && !db.isEmpty(dt.getPos())) {
                        reply = "Position occupied";
                    }
                    if (message.endsWith("DEL") && db.isEmpty(dt.getPos())) {
                        reply = "No data to delete";
                    }

                    out.writeBytes(reply + "\n");
                    log("Reply: " + reply);

                    forwardMessage(start, je, lamport, type, act, circle, message);
                }

                // ================= FORWARD LOGIC =================
                else {
                    log("Forwarding message...");
                    start++;
                    forwardMessage(start, je, lamport, type, act, circle, message);
                }

                out.flush();
                client.close();

            } catch (Exception e) {
                log("Error: " + e.getMessage());
            }
        }

        private void forwardMessage(int start, String jeton, String lamport,
                                    String type, int action, String circle, String message) {
            try {
                int next = pos;
                if (next >= rount.max) next = 0;

                Connect co = new Connect(
                        rount.table[next].destination,
                        rount.table[next].port,
                        rount.table[next].name
                );

                co.connect();

                String msg = "@$" + start + "|" + jeton + "|" + lamport + "|" +
                        serverName + "|" + type + "|" + action + "|" + circle +
                        "$$" + message + "$@";

                co.requestServer(msg);
                co.shutdown();

                log("Forwarded to " + rount.table[next].name);

            } catch (Exception e) {
                log("Forward failed: " + e.getMessage());
            }
        }
    }

    // ================= MAIN TCP SERVER =================
    public static class ServerCore implements Runnable {

        int port;
        Hashtable<String, String> hash = new Hashtable<>();

        public ServerCore(int port) {
            this.port = port;
        }

        @Override
        public void run() {
            try {
                ServerSocket server = new ServerSocket(port);
                log("Server listening on port " + port);

                int currentCircle = 0;

                while (true) {
                    Socket client = server.accept();

                    new Sv1(client, "Server1", 1, currentCircle, hash);

                    currentCircle++;
                }

            } catch (IOException e) {
                log("Server error: " + e.getMessage());
            }
        }
    }

    // ================= LAMPORT (UDP MULTICAST) =================
    public static class GetLamports implements Runnable {

        @Override
        public void run() {
            int lp = 0;

            try {
                MulticastSocket socket = new MulticastSocket(5432);
                InetAddress group = InetAddress.getByName("224.0.0.0");
                socket.joinGroup(group);

                byte[] buffer = new byte[65535];

                log("Lamport service started...");

                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    String msg = new String(packet.getData(), 0, packet.getLength());
                    log("Lamport received: " + msg);

                    if (msg.startsWith("#")) {
                        lp++;
                        byte[] send = String.valueOf(lp).getBytes();

                        DatagramPacket response = new DatagramPacket(
                                send, send.length, packet.getAddress(), packet.getPort()
                        );

                        socket.send(response);
                    }
                }

            } catch (Exception e) {
                log("Lamport error: " + e.getMessage());
            }
        }
    }

    // ================= MAIN =================
    public static void main(String[] args) {

        log("Starting Server1 backend...");
        
        Database db = new Database();
        db.getData();
        log("get data");

        // TCP Server
        new Thread(new ServerCore(2001)).start();

        // Lamport service
        new Thread(new GetLamports()).start();
    }
}