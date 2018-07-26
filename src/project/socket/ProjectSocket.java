package project.socket;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ProjectSocket {

    ServerSocket myServerSocket;
    boolean ServerOn = true;

    public ProjectSocket() {
        try {
            myServerSocket = new ServerSocket(8888);
        } catch (IOException ioe) {
            System.out.println("Could not create server socket on port 8888. Quitting.");
            System.exit(-1);
        }

        Calendar now = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat(
                "E yyyy.MM.dd 'at' hh:mm:ss a zzz");
        System.out.println("It is now : " + formatter.format(now.getTime()));

        while (ServerOn) {
            try {
                Socket clientSocket = myServerSocket.accept();
                ClientServiceThread cliThread = new ClientServiceThread(clientSocket);
                cliThread.start();
            } catch (IOException ioe) {
                System.out.println("Exception found on accept. Ignoring. Stack Trace :");
                ioe.printStackTrace();
            }
        }
        try {
            myServerSocket.close();
            System.out.println("Server Stopped");
        } catch (Exception ioe) {
            System.out.println("Error Found stopping server socket");
            System.exit(-1);
        }
    }

    public static void main(String[] args) {
        new ProjectSocket();
    }

    class ClientServiceThread extends Thread {

        Socket myClientSocket;
        boolean m_bRunThread = true;
        public String[] listCommand = {"", "quit", "help"};

        public ClientServiceThread() {
            super();
        }

        ClientServiceThread(Socket s) {
            myClientSocket = s;
        }

        public void run() {

            System.out.println(
                    "Accepted Client Address - " + myClientSocket.getInetAddress().getHostName());
            try {
                DataInputStream dataInputStream = new DataInputStream(myClientSocket.getInputStream());

                // Membuat stream output untuk memberikan respon kepada client
                DataOutputStream dataOutputStream = new DataOutputStream(myClientSocket.getOutputStream());
                JSONArray array = new JSONArray();
                JSONObject send = new JSONObject();
                String output = "Command not registered\n";
                array.put(output);
                send.put("response", array);
                System.out.println(String.valueOf(send));
                dataOutputStream.writeUTF(String.valueOf(send));
                dataOutputStream.flush();
                while (m_bRunThread) {
                    try {
                        String input = dataInputStream.readUTF();

                        JSONObject jsonObject = new JSONObject(input);
                        String recieve = jsonObject.getString("message");//                        
//                        String output = listCommand(recieve);
                        System.out.println(input);
//                        JSONArray array = new JSONArray();
//                        JSONObject send = new JSONObject();
                        if (output.equalsIgnoreCase("quit")) {
                            m_bRunThread = false;
                            System.out.print("Stopping client thread for client : " + myClientSocket.getInetAddress().getHostName());
                        } else if (output.equalsIgnoreCase("Command not registered")) {
                        } else if (output.equalsIgnoreCase("help")) {
//                        output = "quit = for stop client session\n"
//                                +"end = for shutdown server\n"
//                                + "help = for show list command\n";  
                            array.put("quit = for stop client session\n");
                            array.put("end = for shutdown server\n");
                            array.put("help = for show list command\n");
//                        send.put("response", output);
//                            send.put(array);
                            send.put("response", array);
                            System.out.println(String.valueOf(send));
                            dataOutputStream.writeUTF(String.valueOf(send));
                            dataOutputStream.flush();
                        } else {
                            array.put(output);
                            send.put("response", array);
                            System.out.println(String.valueOf(send));
                            dataOutputStream.writeUTF(String.valueOf(send));
                            dataOutputStream.flush();

                        }
//                        System.out.println("client say : " + recieve);                   

                    } catch (EOFException e) {
                        System.out.println("Client " + myClientSocket.getInetAddress().getHostName() + " Disconnected");
                        m_bRunThread = false;
                    } catch (JSONException ex) {
                        Logger.getLogger(ProjectSocket.class.getName()).log(Level.SEVERE, null, ex);
                    }
//                    catch (JSONException ex) {
//                        Logger.getLogger(ProjectSocket.class.getName()).log(Level.SEVERE, null, ex);
//                    }
                }
            } catch (EOFException e) {
                e.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (JSONException ex) {
                Logger.getLogger(ProjectSocket.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    myClientSocket.close();
                    System.out.println("...Stopped");
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                } catch (NullPointerException e) {
                    System.out.println("Thread closed");
                }
            }
        }

        public String listCommand(String clientCommand) {
            String output = "";
            for (String listCommand : listCommand) {
                if (listCommand.equals(clientCommand)) {
                    output = listCommand;
                    break;
                } else {
                    output = "Command not registered";
                }
            }
            return output;
        }

    }
}
