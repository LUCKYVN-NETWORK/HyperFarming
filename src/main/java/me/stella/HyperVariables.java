package me.stella;

import java.util.*;
import java.util.function.Function;

public class HyperVariables {

    private final static Map<Class<?>, Object> variables = new HashMap<>();
    private final static Map<Class<?>, Set<Object>> variableSet = new HashMap<>();

    public static void inject(Class<?> clazz, Object object) {
        if(!(object.getClass().isAssignableFrom(clazz) || clazz.isInstance(object)))
            throw new RuntimeException("Incompatible item type! Unable to inject variables!");
        variables.put(clazz, object);
    }

    public static void inject(Class<?> clazz, Collection<Object> objects) {
        if(!variableSet.containsKey(clazz))
            variableSet.put(clazz, new HashSet<>());
        for(Object obj: objects) {
            if(!obj.getClass().isAssignableFrom(clazz))
                throw new RuntimeException("Unable to inject variable!");
            variableSet.get(clazz).add(obj);
        }
    }

    public static void injectToCollection(Class<?> clazz, Object object) {
        if(!object.getClass().isAssignableFrom(clazz))
            throw new RuntimeException("Incompatible item type! Unable to inject variables!");
        if(!variableSet.containsKey(clazz))
            variableSet.put(clazz, new HashSet<>());
        variableSet.get(clazz).add(object);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> clazz) {
        if(!variables.containsKey(clazz))
            throw new RuntimeException("Unable to find assigned variable!");
        T out = (T) variables.get(clazz);
        assert (out != null); return out;
    }

    public static void delete(Class<?> clazz) {
        variables.remove(clazz);
    }

    public static void deleteFromCollection(Class<?> clazz, Object object) {
        variableSet.getOrDefault(clazz, new HashSet<>()).remove(object);
    }

    @SuppressWarnings("unchecked")
    public static <T> Set<T> get(Class<T> clazz, Function<T, Boolean> filter) {
        if(!variableSet.containsKey(clazz))
            return new HashSet<>();
        Set<Object> storedObjs = variableSet.get(clazz);
        Set<T> output = new HashSet<>();
        for(Object obj: storedObjs) {
            if(filter.apply((T)obj))
                output.add((T)obj);
        }
        return output;
    }

}
