package ru.waxera.chat.io.client;

import ru.waxera.chat.io.core.command.CommandProcessor;
import ru.waxera.chat.io.core.command.TypeMismatchException;

import java.util.Scanner;

public class IoClientRunner {

    static String nickname = null;
    static ClientConnection connection = null;
    private static CommandProcessor commandProcessor;

    public static void main(String[] args) {
        setCommandProcessor();
        menu();
    }

    private static void setCommandProcessor(){
        commandProcessor = new CommandProcessor();
        commandProcessor.add("exit", (e) -> {
            if(e.get("connection") instanceof ClientConnection clientConnection){
                clientConnection.closeConnection();
                System.out.println("You have been left from the chat server!");
            }
            else{
                throw new TypeMismatchException("The type of the environment variable \"connection\" does not match the predicate.");
            }
        });

        commandProcessor.add("call", (e) -> {
            if(e.get("connection") instanceof ClientConnection clientConnection){
                clientConnection.startVoiceMode();
            }
            else{
                throw new TypeMismatchException("The type of the environment variable \"connection\" does not match the predicate.");
            }
        });

        commandProcessor.add("bye", (e) -> {
            if(e.get("connection") instanceof ClientConnection clientConnection){
                clientConnection.stopVoiceMode();
            }
            else{
                throw new TypeMismatchException("The type of the environment variable \"connection\" does not match the predicate.");
            }
        });
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

    public static CommandProcessor getCommandProcessor(){
        return commandProcessor;
    }

    public static String getNickname(){
        return nickname;
    }
}
