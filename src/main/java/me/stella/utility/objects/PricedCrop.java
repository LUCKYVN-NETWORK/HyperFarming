package me.stella.utility.objects;

public class PricedCrop {

    private final double perValue;
    private final int perCount;

    protected PricedCrop(double value, int count) {
        this.perValue = value;
        this.perCount = count;
    }

    public double getPerValue() {
        return perValue;
    }

    public int getPerCount() {
        return perCount;
    }

    public static PricedCrop build(double paramValue, int paramCount) {
        return new PricedCrop(paramValue, paramCount);
    }
}
