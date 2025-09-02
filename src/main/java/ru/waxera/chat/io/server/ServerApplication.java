package ru.waxera.chat.io.server;

import ru.waxera.chat.io.core.command.CommandProcessor;
import ru.waxera.chat.io.core.command.TypeMismatchException;
import ru.waxera.chat.io.server.types.ChatServer;
import ru.waxera.chat.io.server.types.Server;
import ru.waxera.chat.io.server.types.VoiceServer;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerApplication {
    private static Server chatServer;
    private static Server voiceServer;
    private static CommandProcessor commandProcessor;
    private final static ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor();

    public static void main(String[] args) {
        setCommandProcessor();

        String portStr;
        int port = -1;

        Scanner scanner = new Scanner(System.in);
        while(port == -1){
            System.err.print("Please specify the port to chat server (TCP & UDP): ");
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

        chatServer = new ChatServer(port, pool);
        voiceServer = new VoiceServer(port);
        pool.submit((Runnable) chatServer);
        pool.submit((Runnable) voiceServer);

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.out.println("Server stopped by interruption.");
            Thread.currentThread().interrupt();
        } finally {
            pool.shutdown();
        }
    }

    private static void setCommandProcessor(){
        commandProcessor = new CommandProcessor();
        commandProcessor.addInterrupter("exit", (e) -> {
            if(e.get("server") instanceof ChatServer server
                    && e.get("nickname") instanceof String nickname){
                server.serverMessage("%s left!".formatted(nickname));
                System.err.println("Executing exit");
            }
            else{
                throw new TypeMismatchException("One of the types of the environment variable does not match the predicate.");
            }
        });

        commandProcessor.add("call", (e) -> {
            if(e.get("server") instanceof ChatServer server
                    && e.get("nickname") instanceof String nickname){
                server.serverMessage("%s joined to the voice room!".formatted(nickname));

            }
            else{
                throw new TypeMismatchException("One of the types of the environment variable does not match the predicate (/call command).");
            }
        });

        commandProcessor.add("bye", (e) -> {
            if(e.get("server") instanceof ChatServer server
                    && e.get("nickname") instanceof String nickname){
                server.serverMessage("%s left from the voice room!".formatted(nickname));
            }
            else{
                throw new TypeMismatchException("One of the types of the environment variable does not match the predicate (/bye command).");
            }
        });
    }

    public static CommandProcessor getCommandProcessor(){
        return commandProcessor;
    }
}
