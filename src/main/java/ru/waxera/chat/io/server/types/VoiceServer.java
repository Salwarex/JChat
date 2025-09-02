package ru.waxera.chat.io.server.types;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

public class VoiceServer implements Server, Runnable{

    private final Set<InetSocketAddress> connected;
    private final DatagramSocket voiceSocket;

    public VoiceServer(int port){
        this.connected = new HashSet<>();
        try {
            System.out.println("Voice server started on " + port);
            this.voiceSocket = new DatagramSocket(port);
        } catch (IOException e) {
            System.err.println("Create server socket exception: " + e.toString());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        byte[] buffer = new byte[8192];
        try {
            while (!voiceSocket.isClosed()) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                voiceSocket.receive(packet);
                InetSocketAddress clientAddress = new InetSocketAddress(packet.getAddress(), 41456);

                if(!connected.contains(clientAddress)){
                    connected.add(clientAddress);
                    System.out.println("New voice chat member: " + clientAddress);
                }

                System.out.println("Handling data...");

                broadcast(packet, clientAddress);
            }
        } catch (IOException e) {
            System.err.println("Start accepting: " + e.toString());
        }
    }

    private void broadcast(DatagramPacket packet, InetSocketAddress sender){
        byte[] data = packet.getData();
        int length = packet.getLength();
        Set<InetSocketAddress> toRemove = new HashSet<>();

        for(InetSocketAddress client : connected){
            if(client.equals(sender)) continue;

            try{
                DatagramPacket broadcastPacket = new DatagramPacket(data, length, client.getAddress(), client.getPort());
                voiceSocket.send(broadcastPacket);
            } catch (IOException e) {
                System.err.println("Can't send audio to "+ client + ": " + e.getMessage());
                toRemove.add(client);
            }
        }
        connected.removeAll(toRemove);
    }

    @Override
    public void stop() {
        voiceSocket.close();
        System.out.println("VoiceServer stopped.");
    }
}
