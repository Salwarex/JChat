package ru.waxera.chat.io.client;

import ru.waxera.chat.io.client.sound.ApplicationAudioFormat;
import ru.waxera.chat.io.client.sound.SoundReader;
import ru.waxera.chat.io.client.sound.SoundReceiver;
import ru.waxera.chat.io.core.command.Environment;

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
    private final String address;
    private final int port;
    private Socket socket;
    private ExecutorService pool = Executors.newFixedThreadPool(3);
    private volatile boolean isClosed = false;
    private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private SoundReader reader = null;
    private SoundReceiver receiver = null;
    private boolean voiceMode = false;

    public ClientConnection(String address, int port) {
        this.address = address;
        this.port = port;
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

                char first = message.charAt(0);
                if(!voiceMode){
                    out.writeUTF(message);
                }
                else {
                    if(first != '/') {
                        System.out.println("[VOICE ROOM] You can't send default messages in the voice room!");
                        continue;
                    }
                }

                if(first == '/'){
                    String commandKey = message.substring(1);
                    Environment environment = new Environment();
                    environment.add("connection", this);
                    IoClientRunner.getCommandProcessor().execute(commandKey, environment);
                }
            }
        }
        catch(SocketException ignored) {}
        catch (IOException e){
            closeConnection();
            System.err.println("Handle submit exception: " +e.toString());
        }
    }

    void stopVoiceMode() {
        if(voiceMode){
            reader.stop();
            receiver.stop();
            voiceMode = false;
        }
        else{
            System.out.println("[VOICE ROOM] You are not in the voice room!");
        }
    }

    void startVoiceMode() {
        if(!voiceMode){
            reader = new SoundReader(ApplicationAudioFormat.getInstance(), address, port);
            receiver = new SoundReceiver(ApplicationAudioFormat.getInstance(), 41456);
            pool.submit(reader);
            pool.submit(receiver);
            voiceMode = true;
        }
        else{
            System.out.println("[VOICE ROOM] You are already in the voice room!");
        }

    }

    void closeConnection(){
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
                if(socket.isClosed()) { System.err.println("socket is closed!"); break; }
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
            System.err.println("EOFE");
        }
        catch(SocketException ignored) {}
        catch (IOException e) {
            System.err.println("Handle answers exception: " + e.toString());
            closeConnection();
        }
    }
}
