package ru.waxera.chat.io.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IoServer {
    private final ExecutorService pool;
    private final Set<Socket> connected;
    private final ServerSocket serverSocket;
    private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd.MM.yyyy");

    public IoServer(int port){
        this.pool = Executors.newVirtualThreadPerTaskExecutor();
        this.connected = new HashSet<>();
        try {
            System.out.println("Server is started on " + port);
            this.serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("Create server socket exception: " + e.toString());
            throw new RuntimeException(e);
        }
    }

    public void startAccepting(){
        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                pool.submit(() -> handleRequest(socket));
            }
        } catch (IOException e) {
            System.err.println("Start accepting: " + e.toString());
        }
    }

    private void stop(){
        try{
            this.serverSocket.close();
        }catch (IOException e){
            System.err.println("Server stop exception: " + e.toString());
        }
    }

    private void handleRequest(Socket socket){
        boolean joinAnnounced = false;
        try (socket) {
            DataInputStream in = new DataInputStream(socket.getInputStream());

            connected.add(socket);

            //init
            String nickname = in.readUTF();
            System.out.println(nickname + " connected!");
            broadcast("server@%s join!".formatted(nickname), null);
            //

            while(!socket.isClosed()){
                String message = in.readUTF();

                System.out.printf("[%s] %s > %s %n", LocalDateTime.now(ZoneId.systemDefault()).format(formatter), nickname, message);

                if("exit".equalsIgnoreCase(message)){
                    System.out.println(nickname + " disconnected!");
                    broadcast("server@%s left!".formatted(nickname), null);
                    break;
                }

                broadcast(nickname + "@" + message, socket);
            }
        } catch(SocketException ignored) {} catch (IOException e) {
            System.err.println("Handle request exception: " + e.toString());
        } finally {
            connected.remove(socket);
        }
    }

    private void broadcast(String message, Socket sender){
        List<Socket> toRemove = new ArrayList<>();
        for(Socket socket: this.connected){
            if(socket.isClosed()) {
                toRemove.add(socket);
                continue;
            }
            try{
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                out.writeUTF(message);
                out.flush();
            } catch (IOException e) {
                System.err.println("Broadcast exception: " + e.toString());
            }
        }
        for(Socket socket : toRemove){
            connected.remove(socket);
        }
    }
}
