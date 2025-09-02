package ru.waxera.chat.io.core.command;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class CommandProcessor {
    private final Map<String, Consumer<Environment>> commandsMap;
    private final Set<String> interrupters;

    public CommandProcessor(){
        this.commandsMap = new HashMap<>();
        this.interrupters = new HashSet<>();
    }

    public void addInterrupter(String command, Consumer<Environment> action){
        add(command, action);
        interrupters.add(command);
    }

    public void add(String command, Consumer<Environment> action){
        if(!commandsMap.containsKey(command)){
            commandsMap.put(command, action);
        }
    }

    public void remove(String command){
        if(contains(command)) commandsMap.remove(command);
        if(interrupters.contains(command)) interrupters.remove(command);
    }

    public void execute(String command, Environment environment){
        //System.out.println("Command list: " + commandsMap.keySet() );

        if(contains(command)){
            commandsMap.get(command).accept(environment);
        }
    }

    public boolean executeInterrupt(String command, Environment environment){
        execute(command, environment);
        return interrupters.contains(command);
    }

    public boolean contains(String command){
        return commandsMap.containsKey(command);
    }
}
