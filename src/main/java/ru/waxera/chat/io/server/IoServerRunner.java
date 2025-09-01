package ru.waxera.chat.io.server;

import java.util.Scanner;

public class IoServerRunner {
    private static IoServer server;

    public static void main(String[] args) {
        String portStr;
        int port = -1;

        System.err.print("Please specify the port to chat server: ");
        Scanner scanner = new Scanner(System.in);
        while(port == -1){
            portStr = scanner.nextLine();
            if(portStr.isEmpty()) {
                System.err.println("You did not specify the port.");
                return;
            }
            try{
                port = Integer.parseInt(portStr);
            } catch (NumberFormatException e) {
                System.err.println("You specified a wrong value.");
            }
        }

        server = new IoServer(port);
        server.startAccepting();
    }
}
