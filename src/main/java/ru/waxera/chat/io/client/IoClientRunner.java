package ru.waxera.chat.io.client;

import java.util.Scanner;

public class IoClientRunner {

    static String nickname = null;
    static ClientConnection connection = null;

    public static void main(String[] args) {
        menu();
    }

    static void resetConnection(){
        connection = null;
        menu();
    }

    private static void menu(){
        Scanner scanner = new Scanner(System.in);
        while(true){
            if(nickname == null){
                System.out.print("Please, type your chat nickname: ");
                nickname = scanner.nextLine();
                System.out.println("Good! Now your nickname is " + nickname);
            }
            if(connection == null){
                System.out.print("Chat socket address (IP:port): ");
                String address = scanner.nextLine();
                String[] splitAddress = address.split(":");
                if(splitAddress.length != 2) {
                    System.err.println("Wrong address!");
                    continue;
                }
                int port = 0;
                try{
                    port = Integer.parseInt(splitAddress[1]);
                } catch (NumberFormatException e) {
                    System.err.println("Wrong address (can't parse port)!");
                    continue;
                }
                connection = new ClientConnection(splitAddress[0], port);
            }
        }
    }

    public static String getNickname(){
        return nickname;
    }
}
