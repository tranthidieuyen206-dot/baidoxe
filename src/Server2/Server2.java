/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Server2;

import java.net.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.Color;
import java.awt.Font;
import javax.swing.border.TitledBorder;
import java.util.Hashtable;
import java.net.*;
import java.io.*;
import java.lang.Thread;
import java.util.Hashtable;
import java.util.Vector;

public class Server2 extends JFrame {

    private JFrame mainFrm;
    private JPanel jCPane;
    private JScrollPane scroll;
    static JTextArea display;
    int counter;
    ObjectOutputStream output;
    ObjectInputStream input;
    ServerSocket server;
    Socket client, connection;
    String serverName;
    String type;
    int pos;
    RountingTable rount;
    int currentCircle;
    static String MESSAGE, replyMessage;
    Hashtable hash;
    DataOutputStream out;
    BufferedReader in;
    Database db1, db;
    ProcessData data, dt;

    public Server2() {
        JFrame mainFrm = new JFrame("Server 2");
        mainFrm.setSize(400, 400);

        jCPane = new JPanel();
        jCPane.setLayout(null);

        scroll = new JScrollPane();
        scroll.setBounds(new Rectangle());
        display = new JTextArea();
        display.setBounds(new Rectangle(10, 10, 370, 345));
        scroll.setViewportView(display);
        scroll.setBounds(new Rectangle(10, 10, 370, 345));
        scroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.blue, 2), "SERVER 2", TitledBorder.CENTER, TitledBorder.CENTER, new Font("Dialog", Font.BOLD, 12), Color.blue));

        jCPane.add(scroll);
        mainFrm.add(jCPane);
        mainFrm.setVisible(true);
        mainFrm.setResizable(false);
    }

    public static class sv2 implements Runnable {

        int counter;
        ObjectOutputStream output;
        ObjectInputStream input;
        ServerSocket server;
        Socket client, connection;
        String serverName;
        String type;
        int pos;
        RountingTable rount;
        int currentCircle;
        static String MESSAGE, replyMessage;
        Hashtable hash;
        DataOutputStream out;
        BufferedReader in;
        Database db1, db;
        ProcessData data, dt;

        sv2() {
            //this.q = q; 
            new Thread(this, "sv2").start();
        }

        public void handler(Socket newSocket, String serverName, int pos, int curr, Hashtable hash) {
            client = newSocket;
            this.serverName = serverName;
            rount = new RountingTable();
            this.pos = pos;
            this.currentCircle = curr;
            MESSAGE = "";
            this.hash = hash;
        }

        public void runServer() {
            try {
                String destName = client.getInetAddress().getHostName();
                int destPort = client.getPort();
                display.append("Chap nhan ket noi tu " + destName + " tai cong " + destPort + ".\n");
                BufferedReader inStream = new BufferedReader(new InputStreamReader(client.getInputStream()));
                DataOutputStream outStream = new DataOutputStream(client.getOutputStream());
                boolean finished = false;
                {
                    // lay goi tin nhan duoc
                    String inLine = inStream.readLine();
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
                    String jeton;
                    display.append("Thong tin nhan duoc :" + "\n" + "start: " + st + "\n" + "jeton: " + je + "\n" + "lamport: " + lamport + "\n" + "servername: " + name + "\n"
                            + "type: " + type + "\n" + "action: " + action + "\n" + "vong dk: " + circle + "\n" + "thong diep: " + message + "\n");
                    int start = Integer.parseInt(st);
                    int act = Integer.parseInt(action);
                    String t = "", rev;

                    if (act == 4) {
                        rev = je;
                        int po = pos + 9;
                        try {
                            rev = je.substring(1, po);
                        } catch (Exception ex) {
                        }
                        t = rev;
                    } else if (act == 3) {
                        try {
                            t = je.substring(0, pos - 1);
                        } catch (Exception ex) {
                        }

                        jeton = je;
                        t += "1";
                        try {
                            t += jeton.substring(pos);
                        } catch (Exception ex) {
                        }
                    } else if (act == 2) {
                        try {
                            t = je.substring(0, pos - 1);
                        } catch (Exception ex) {
                        }

                        jeton = je;
                        t += "1";
                        try {
                            t += jeton.substring(pos);
                        } catch (Exception ex) {
                        }
                    } else if (act == 1) {
                        try {
                            t = je.substring(0, pos - 1);
                        } catch (Exception ex) {
                        }

                        jeton = je;
                        t += "1";
                        try {
                            t += jeton.substring(pos);
                        } catch (Exception ex) {
                        }
                    }
                    int vt = pos;
                    if (vt > rount.max - 1) {
                        vt = 0;
                    }

                    //  xu ly thong tin Synchronymed va ket thuc vong tron ao

                    if (type.equals("Synchronymed") && (start == 4)) {
                        display.append("Hoan tat giao dich. Ket thuc vong tron ao.\n\n");
                    }

                    //xu ly thong tin updated va quay vong

                    if (type.equals("Updated") && (start == 4)) {
                        int stt = start;
                        display.append("Ket thuc qua trinh cap nhat, kiem tra dong bo hoa TT va Quay vong nguoc.\n\n");
                        stt = 1;
                        act += 1;
                        try {
                            int tam = pos - 2;
                            if (tam < 0) {
                                tam = 2;
                            }
                            if (t.charAt(tam) == '0') {
                                display.append("\nServer" + (tam + 1) + " bi su co do jeton nhan duoc la: " + t + ".\n");
                                tam--;
                            }
                            if (tam < 0) {
                                tam = 2;
                            }
                            Connect co = new Connect(rount.table[tam].destination, rount.table[tam].port, rount.table[tam].name);
                            co.connect();
                            String replyServerMessage = "@$" + stt + "|" + t + "|" + lamport + "|" + rount.table[pos - 1].name + "|" + "Synchronymed" + "|" + act + "|" + circle + "$$" + message + "$@";
                            co.requestServer(replyServerMessage);
                            co.shutdown();
                        } catch (Exception Ex) {
                        }

                    }

                    //  xu ly thong tin temped va quay vong

                    if (type.equals("Temped") && (start == 4)) {
                        int stt = start;
                        display.append("Ket thuc tao bang tam, cap nhat CSDL chinh Quay vong nguoc.\n\n");
                        stt = 1;
                        act += 1;
                        try {
                            Connect co = new Connect(rount.table[vt].destination, rount.table[vt].port, rount.table[vt].name);
                            co.connect();
                            co.requestServer("@$" + stt + "|" + t + "|" + lamport + "|" + rount.table[pos - 1].name + "|" + "Updated" + "|" + act + "|" + circle + "$$" + message + "$@");
                            co.shutdown();
                        } //bi su co
                        catch (Exception ex) {
                            display.append("\n" + rount.table[vt].name + ": bi su co, hien khong lien lac duoc.\n\n");
                            vt++;
                            if (vt > rount.max - 1) {
                                vt = 0;
                            }

                            Connect con = new Connect(rount.table[vt].destination, rount.table[vt].port, rount.table[vt].name);
                            con.connect();
                            con.requestServer("@$" + stt + "|" + t + "|" + lamport + "|" + rount.table[pos - 1].name + "|" + "Updated" + "|" + act + "|" + circle + "$$" + message + "$@");
                            con.shutdown();
                        }
                    }


                    //quay vong nguoc lai cua thong diep locked
                    if (type.equals("Locked") && (start == 4)) {
                        int stt = start;
                        display.append("Ket thuc khoa truong du lieu, tao bang tam va Quay vong nguoc.\n\n");
                        stt = 1;
                        act += 1;
                        try {
                            int tam = pos - 2;
                            if (tam < 0) {
                                tam = 2;
                            }
                            if (t.charAt(tam) == '0') {
                                display.append("\nServer" + (tam + 1) + " bi su codo jeton nhan duoc la: " + t + ".\n\n");
                                tam--;
                            }
                            if (tam < 0) {
                                tam = 2;
                            }
                            Connect co = new Connect(rount.table[tam].destination, rount.table[tam].port, rount.table[tam].name);
                            co.connect();
                            String replyServerMessage = "@$" + stt + "|" + t + "|" + lamport + "|" + rount.table[pos - 1].name + "|" + "Temped" + "|" + act + "|" + circle + "$$" + message + "$@";
                            co.requestServer(replyServerMessage);
                            co.shutdown();
                        } catch (Exception Ex) {
                        }
                    }

                    //    	 xu ly thong tin tu client
                    if (start == 0) {
                        start++;
                        replyMessage = "Da thuc hien thanh cong.";
                        db1 = new Database();
                        dt = new ProcessData(message);

                        if (message.endsWith("VIEW")) {
                            db1 = new Database();
                            replyMessage = db1.getData();
                        }
                        if ((message.endsWith("SET")) && (!db1.isEmpty(dt.getPos()))) {
                            replyMessage = "Vi tri da co xe goi.";
                        }
                        if ((message.endsWith("DEL")) && (db1.isEmpty(dt.getPos()))) {
                            replyMessage = "Khong co xe tai vi tri nay.";
                        }


                        for (int i = 0; i < replyMessage.length(); ++i) {
                            outStream.write((byte) replyMessage.charAt(i));
                        }
                        display.append("Reply: " + replyMessage + "\n");
                        display.append("Thuc hien khoa truong DL. Chuyen thong diep.\n\n");
                        try {
                            Connect co = new Connect(rount.table[vt].destination, rount.table[vt].port, rount.table[vt].name);
                            co.connect();
                            co.requestServer("@$" + start + "|" + t + "|" + lamport + "|" + rount.table[pos - 1].name + "|" + "Locked" + "|" + act + "|" + circle + "$$" + message + "$@");
                            co.shutdown();
                        } //bi su co
                        catch (Exception ex) {
                            display.append("\n" + rount.table[vt].name + ": bi su co, hien khong lien lac duoc.\n\n");
                            vt++;
                            if (vt > rount.max - 1) {
                                vt = 0;
                            }

                            Connect con = new Connect(rount.table[vt].destination, rount.table[vt].port, rount.table[vt].name);
                            con.connect();
                            con.requestServer("@$" + start + "|" + t + "|" + lamport + "|" + rount.table[pos - 1].name + "|" + "Locked" + "|" + act + "|" + circle + "$$" + message + "$@");
                            con.shutdown();
                        }
                    }

                    // xu ly thong tin locked
                    if (type.equals("Locked") && (start != 4)) {
                        display.append("Chuyen thong diep, thuc hien khoa truong DL.\n\n");
                        start++;
                        try {
                            Connect co = new Connect(rount.table[vt].destination, rount.table[vt].port, rount.table[vt].name);
                            co.connect();
                            co.requestServer("@$" + start + "|" + t + "|" + lamport + "|" + rount.table[pos - 1].name + "|" + "Locked" + "|" + action + "|" + circle + "$$" + message + "$@");
                            co.shutdown();
                        } //bi su co
                        catch (Exception ex) {
                            display.append("\n" + rount.table[vt].name + ": bi su co, hien khong lien lac duoc.\n\n");
                            vt++;
                            if (vt > rount.max - 1) {
                                vt = 0;
                            }

                            Connect con = new Connect(rount.table[vt].destination, rount.table[vt].port, rount.table[vt].name);
                            con.connect();
                            con.requestServer("@$" + start + "|" + t + "|" + lamport + "|" + rount.table[pos - 1].name + "|" + type + "|" + action + "|" + circle + "$$" + message + "$@");
                            con.shutdown();
                        }
                    }

                    //    	 Xu ly thong diep temp
                    if (type.equals("Temped") && (start != 4)) {
                        display.append("Chuyen thong diep, thuc hien tao bang tam CSDL.\n\n");
                        start++;
                        try {
                            int tam = pos - 2;
                            if (tam < 0) {
                                tam = 2;
                            }
                            if (t.charAt(tam) == '0') {
                                display.append("\nServer" + (tam + 1) + " bi su co do jeton nhan duoc la: " + t + ".\n\n");
                                tam--;
                            }
                            if (tam < 0) {
                                tam = 2;
                            }
                            Connect co = new Connect(rount.table[tam].destination, rount.table[tam].port, rount.table[tam].name);
                            co.connect();
                            String replyServerMessage = "@$" + start + "|" + t + "|" + lamport + "|" + rount.table[pos - 1].name + "|" + type + "|" + act + "|" + circle + "$$" + message + "$@";
                            co.requestServer(replyServerMessage);
                            co.shutdown();
                        } catch (Exception Ex) {
                        }
                    }

                    //         xu ly thong tin update
                    if (type.equals("Updated") && (start != 4)) {
                        display.append("Chuyen thong diep, thuc hien cap nhat bang chinh CSDL.\n\n");
                        start++;
                        try {
                            Connect co = new Connect(rount.table[vt].destination, rount.table[vt].port, rount.table[vt].name);
                            co.connect();
                            co.requestServer("@$" + start + "|" + t + "|" + lamport + "|" + rount.table[pos - 1].name + "|" + type + "|" + action + "|" + circle + "$$" + message + "$@");
                            co.shutdown();
                        } //bi su co
                        catch (Exception ex) {
                            display.append("\n" + rount.table[vt].name + ": bi su co, hien khong lien lac duoc.\n\n");
                            vt++;
                            if (vt > rount.max - 1) {
                                vt = 0;
                            }

                            Connect con = new Connect(rount.table[vt].destination, rount.table[vt].port, rount.table[vt].name);
                            con.connect();
                            con.requestServer("@$" + start + "|" + t + "|" + lamport + "|" + rount.table[pos - 1].name + "|" + type + "|" + action + "|" + circle + "$$" + message + "$@");
                            con.shutdown();
                        }
                    }//dong if

                    //      	 Xu ly thong diep synchronym
                    if (type.equals("Synchronymed") && (start != 4)) {
                        display.append("Chuyen thong diep, kiem tra qua trinh dong bo hoa cac tien trinh.\n\n");
                        start++;
                        try {
                            int tam = pos - 2;
                            if (tam < 0) {
                                tam = 2;
                            }
                            Connect co = new Connect(rount.table[tam].destination, rount.table[tam].port, rount.table[tam].name);
                            co.connect();
                            String replyServerMessage = "@$" + start + "|" + t + "|" + lamport + "|" + rount.table[pos - 1].name + "|" + type + "|" + action + "|" + circle + "$$" + message + "$@";
                            co.requestServer(replyServerMessage);
                            co.shutdown();
                        } catch (Exception Ex) {
                        }
                    } //dong if

                    outStream.write(13);
                    outStream.write(10);
                    outStream.flush();
                }
            } catch (Exception e) {
            }
        }

        @Override
        public void run() {
            int currentCircle = 0;
            int loop = 1;
            sv2 apps = new sv2();
            Server2 app;
            Hashtable hash = new Hashtable();
            try {

                GetState gs = new GetState("Server2");
                gs.getCurrentCircle();
                gs.sendUpdate("127.0.0.1", 2003, "Server2");

                ServerSocket server = new ServerSocket(2002);
                while (true) {
                    int localPort = server.getLocalPort();
                    display.append("Server 2 is listening on port " + localPort + ".\n");
                    Socket client = server.accept();
                    apps.handler(client, "Server2", 2, currentCircle, hash);
                    apps.runServer();
                    ProcessData data = new ProcessData(MESSAGE);
                    Database db = new Database();
                    boolean ktradb = db.querySQL(data.getPos(), data.getNum(), data.getType(), data.getColor());
                    if (ktradb == true) {
                        if (data.getAct().equalsIgnoreCase("SET")) {
                            db.insertData(data.getPos(), data.getNum(), data.getType(), data.getColor(), data.getTime());
                        } else if (data.getAct().equalsIgnoreCase("DEL")) {
                            db.delData(data.getPos());
                        }
                    }
                    currentCircle++;
                    hash.put(String.valueOf(currentCircle), MESSAGE);

                }
            } catch (IOException e) {
            }
        }
    }

    

    public static void main(String args[]) {
        Hashtable hash = new Hashtable();
        Server2 app = new Server2();
        app.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        sv2 sv2s = new sv2();
    }
}
