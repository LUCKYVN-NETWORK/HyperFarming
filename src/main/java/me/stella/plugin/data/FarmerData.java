package me.stella.plugin.data;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FarmerData {

    private static final Class<?> farmerDataClass = FarmerData.class;
    private int limit;
    private long blocksBroken;
    private int WHEAT;
    private int CARROT;
    private int POTATO;
    private int BEETROOT;
    private int MELON;
    private int PUMPKIN;
    private int COCOA;
    private int SUGAR_CANE;
    private int CACTUS;

    public static Set<String> getDataTypes() {
        Set<String> fields = new HashSet<>();
        for(Field dataField:  farmerDataClass.getDeclaredFields()) {
            if(dataField.getName().equals("limit") || dataField.getName().equals("farmerDataClass") || dataField.getName().equals("blocksBroken"))
                continue;
            fields.add(dataField.getName());

        }
        return fields;
    }

    public FarmerData(int limit, Map<String, Integer> storageData, long broken) {
        this.limit = limit;
        this.blocksBroken = broken;
        try {
            storageData.forEach((type, amount) -> {
                try {
                    Field dataField = farmerDataClass.getDeclaredField(type);
                    dataField.setAccessible(true);
                    dataField.set(this, amount);
                } catch(Exception err2) { err2.printStackTrace(); }
            });
        } catch(Exception err) { err.printStackTrace(); }
    }

    public FarmerData(int limit) {
        this.limit = limit;
        try {
            for(Field dataField:  farmerDataClass.getDeclaredFields()) {
                if(dataField.getName().equals("limit") || dataField.getName().equals("farmerDataClass") || dataField.getName().equals("blocksBroken"))
                    continue;
                dataField.setAccessible(true);
                dataField.set(this, 0);
            }
        } catch(Exception err) { err.printStackTrace(); }
    }

    public int getLimit() {
        return this.limit;
    }

    public void setLimit(int param) {
        this.limit = param;
    }

    public long getBlocksBroken() {
        return this.blocksBroken;
    }

    public void breakBlock() {
        this.blocksBroken++;
    }

    public Map<String, Integer> exportData() {
        Map<String, Integer> exported = new HashMap<>();
        try {
            for(Field dataField: farmerDataClass.getDeclaredFields()) {
                if(dataField.getName().equals("limit") || dataField.getName().equals("farmerDataClass") || dataField.getName().equals("blocksBroken"))
                    continue;
                dataField.setAccessible(true);
                exported.put(dataField.getName(), (int) dataField.get(this));
            }
        } catch(Exception err) { err.printStackTrace(); }
        return exported;
    }

    public boolean isFull(String type) {
        try {
            Field fieldType = farmerDataClass.getDeclaredField(type);
            fieldType.setAccessible(true);
            return ((int)fieldType.get(this)) >= this.limit;
        } catch(Exception err) { err.printStackTrace(); }
        return true;
    }

    public int getData(String type) {
        try {
            Field fieldType = farmerDataClass.getDeclaredField(type);
            fieldType.setAccessible(true);
            return ((int)fieldType.get(this));
        } catch(Exception err) { err.printStackTrace(); }
        return 0;
    }

    public int increase(String type, int amount) {
        if(isFull(type))
            return amount;
        try {
            Field fieldType = farmerDataClass.getDeclaredField(type);
            fieldType.setAccessible(true);
            int balance = ((int)fieldType.get(this));
            int threshold = (this.limit - balance);
            if(threshold >= amount) {
                balance += amount;
                amount = 0;
            } else {
                amount -= threshold;
                balance += threshold;
            }
            fieldType.set(this, balance);
            return amount;
        } catch(Exception err) { err.printStackTrace(); }
        return amount;
    }

    public int decrease(String type, int amount) {
        if(getData(type) == 0)
            return 0;
        try {
            Field fieldType = farmerDataClass.getDeclaredField(type);
            fieldType.setAccessible(true);
            int balance = ((int)fieldType.get(this));
            if(amount >= balance) {
                amount = balance;
                balance = 0;
            } else
                balance -= amount;
            fieldType.set(this, balance);
            return amount;
        } catch(Exception err) { err.printStackTrace(); }
        return 0;
    }

    public void set(String type, int amount) {
        try {
            Field fieldType = farmerDataClass.getDeclaredField(type);
            fieldType.setAccessible(true);
            int cap = Math.min(amount, this.limit);
            fieldType.set(this, cap);
        } catch(Exception err) { err.printStackTrace(); }
    }

}
