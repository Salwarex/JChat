package ru.waxera.chat.io.core.command;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Map<String, Object> objects;

    public Environment(){
        this.objects = new HashMap<>();
    }

    public void add(String key, Object object){
        if(!contains(key)){
            objects.put(key, object);
        }
    }

    public void remove(String key){
        if(contains(key)){
            objects.remove(key);
        }
    }

    public Object get(String key){
        return objects.getOrDefault(key, null);
    }

    public boolean contains(String key){
        return objects.containsKey(key);
    }
}
