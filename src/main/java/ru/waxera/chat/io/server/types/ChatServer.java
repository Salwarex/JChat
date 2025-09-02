package ru.waxera.chat.io.server.types;

import ru.waxera.chat.io.core.command.Environment;
import ru.waxera.chat.io.server.ServerApplication;

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

public class ChatServer implements Server, Runnable{
    private final ExecutorService pool;
    private final Set<Socket> connected;
    private final ServerSocket serverSocket;
    private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd.MM.yyyy");

    public ChatServer(int port, ExecutorService pool){
        this.pool = pool;
        this.connected = new HashSet<>();
        try {
            System.out.println("Server is started on " + port);
            this.serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("Create server socket exception: " + e.toString());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run(){
        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                pool.submit(() -> handleChat(socket));
            }
        } catch (IOException e) {
            System.err.println("Start accepting: " + e.toString());
        }
    }

    @Override
    public void stop(){
        try{
            this.serverSocket.close();
        }catch (IOException e){
            System.err.println("Server stop exception: " + e.toString());
        }
    }

    private void handleChat(Socket socket){
        String nickname = "";
        try (socket) {
            DataInputStream in = new DataInputStream(socket.getInputStream());

            connected.add(socket);

            //init
            nickname = in.readUTF();
            serverMessage("%s join!".formatted(nickname));
            //

            while(!socket.isClosed()){
                String message = in.readUTF();

                System.out.printf("[CLIENT %s] %s > %s %n", LocalDateTime.now(ZoneId.systemDefault()).format(formatter), nickname, message);

                char first = message.charAt(0);
                if(first == '/'){
                    String commandKey = message.substring(1);

                    Environment environment = new Environment();
                    environment.add("server", this);
                    environment.add("nickname", nickname);

                    boolean interrupt = ServerApplication.getCommandProcessor().executeInterrupt(commandKey, environment);

                    if(interrupt) break;
                    else continue;
                }

                broadcast(nickname + "@" + message, socket);
            }
        } catch(SocketException ignored) {} catch (IOException e) {
            System.err.println("Handle request exception: " + e.toString());
        } finally {
            connected.remove(socket);
            System.out.println(nickname + " disconnected!");
            broadcast("server@%s left!".formatted(nickname), null);
        }
    }

    public void serverMessage(String message){
        System.out.printf("[SERVER %s] %s %n", LocalDateTime.now(ZoneId.systemDefault()).format(formatter), message);
        this.broadcast("server@" + message, null);
    }

    private synchronized void broadcast(String message, Socket sender){
        Set<Socket> toRemove = new HashSet<>();
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
        connected.removeAll(toRemove);
    }
}
