package ru.waxera.chat.io.client.sound;

import javax.sound.sampled.*;
import java.io.*;
import java.net.*;

public class SoundReader implements Runnable{
    private String address;
    private int port;

    private boolean allowFlag = false;
    private boolean stopFlag = false;

    private final AudioFormat format;
    private final DataLine.Info info;

    private DatagramSocket socket;

    public SoundReader(AudioFormat format, String address, int port){
        this.address = address;
        this.port = port;
        this.format = format;
        this.info = new DataLine.Info(TargetDataLine.class, format);

        if(!AudioSystem.isLineSupported(this.info)){
            System.err.println("Audio recording format is unsupported!");
            return;
        }
        this.allowFlag = true;
        try{
            this.socket = new DatagramSocket();
        } catch (SocketException e) {
            System.err.println("Socket creating error" + e.toString());
        }

    }

    @Override
    public void run() {
        if(!allowFlag){
            System.err.println("You have not been joined to voice room!");
            return;
        }
        try(TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info)){
            line.open(format);
            line.start();

            System.out.println("You joined to the voice room!");

            byte[] buffer = new byte[640];
            InetAddress inetAddress = InetAddress.getByName(this.address);
            int bytesRead;

            while(!stopFlag && !Thread.currentThread().isInterrupted()){
                bytesRead = line.read(buffer, 0, buffer.length);
                DatagramPacket packet = new DatagramPacket(buffer, bytesRead, inetAddress, port);
                socket.send(packet);
            }

            System.out.println("You left from the voice room!");
            socket.close();
        } catch (LineUnavailableException | UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop(){
        this.stopFlag = true;
    }

//    private void saveAudioFile(byte[] audioData){
//        try(AudioInputStream audioStream = new AudioInputStream(
//                new ByteArrayInputStream(audioData), format, audioData.length / format.getFrameSize())){
//            File file = new File("recorded.wav");
//            AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, file);
//            System.out.println("Your voice saved to " + file.getAbsolutePath());
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

}
