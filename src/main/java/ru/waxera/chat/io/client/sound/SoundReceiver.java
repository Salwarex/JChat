package ru.waxera.chat.io.client.sound;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class SoundReceiver implements Runnable{
    private final AudioFormat format;
    private final DataLine.Info info;

    private DatagramSocket socket;
    private boolean mute;
    private boolean stopFlag;

    public SoundReceiver(AudioFormat format, int port){
        this.format = format;
        this.info = new DataLine.Info(SourceDataLine.class, format);

        if(!AudioSystem.isLineSupported(this.info)){
            System.err.println("Audio recording format is unsupported!");
            return;
        }
        this.mute = false;
        try{
            this.socket = new DatagramSocket(port);
        } catch (SocketException e) {
            System.err.println("Socket creating error" + e.toString());
        }
    }

    @Override
    public void run() {
        try(SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info)){
            line.open(format);
            line.start();

            System.out.println("Sound receiver process started..."); //LOG

            byte[] buffer = new byte[8192];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            while (!stopFlag && !Thread.currentThread().isInterrupted()){
                socket.receive(packet);
                byte[] audioData = new byte[packet.getLength()];
                System.arraycopy(packet.getData(), 0, audioData, 0, packet.getLength());

                line.write(audioData, 0, audioData.length);
            }

            line.drain();
            line.stop();
        }catch (LineUnavailableException | IOException e){
            System.err.println("Error: " + e.getMessage());
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }

        System.out.println("No more receiving data");
    }

    public void stop(){
        this.stopFlag = true;
        socket.close();
        Thread.currentThread().interrupt();
    }
}
