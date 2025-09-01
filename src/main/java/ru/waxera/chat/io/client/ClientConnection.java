package ru.waxera.chat.io.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientConnection {
    private Socket socket;
    private ExecutorService pool = Executors.newFixedThreadPool(2);
    private volatile boolean isClosed = false;
    private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public ClientConnection(String address, int port) {
        try{
            socket = new Socket(address, port);
            pool.submit(this::handleSubmit);
            pool.submit(this::handleAnswers);
        } catch (IOException e) {
            closeConnection();
            System.err.println("Connection create exception: " + e.toString());
        }
    }

    private void handleSubmit() {
        try(DataOutputStream out = new DataOutputStream(socket.getOutputStream());){
            Scanner scanner = new Scanner(System.in);
            out.writeUTF(IoClientRunner.getNickname());
            out.flush();

            while(true){
                String message = scanner.nextLine();
                out.writeUTF(message);
                if(message.equalsIgnoreCase("exit")){
                    closeConnection();
                    break;
                }
            }
        }
        catch(SocketException ignored) {}
        catch (IOException e){
            closeConnection();
            System.err.println("Handle submit exception: " +e.toString());
        }
    }

    public void closeConnection(){
        if (isClosed) return;
        isClosed = true;

        try{
            if(socket != null && !socket.isClosed()){
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Socket closing exception: " + e.toString());
        }
        pool.shutdownNow();
        IoClientRunner.resetConnection();
    }

    private void handleAnswers(){
        try(DataInputStream in = new DataInputStream(socket.getInputStream());){
            while(true){
                if(socket.isClosed()) break;
                String unformattedMessage = in.readUTF();
                String[] msgArr = unformattedMessage.split("@");
                if(msgArr.length != 2) continue;
                String nickname = msgArr[0];
                String message = msgArr[1];

                String time = LocalDateTime.now(ZoneId.systemDefault()).format(formatter);

                if(nickname.equals("server")){
                    System.out.printf("[%s] %s %n", time, message);
                }
                else{
                    System.out.printf("[%s] %s > %s %n", time, nickname, message);
                }

            }
        }
        catch(SocketException ignored) {}
        catch (IOException e) {
            System.err.println("Handle answers exception: " + e.toString());
            closeConnection();
        }
    }
}
