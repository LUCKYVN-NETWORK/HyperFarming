package me.stella.utility.objects;

public class LegacyDataWrapper {

    private final int id;
    private final byte data;

    protected LegacyDataWrapper(int blockID, byte stateData) {
        this.id = blockID;
        this.data = stateData;
    }

    public int getId() {
        return id;
    }

    public byte getData() {
        return data;
    }

    public static LegacyDataWrapper build(int blockID, byte stateData) {
        return new LegacyDataWrapper(blockID, stateData);
    }

}
