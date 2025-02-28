package me.stella.utility;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class WorldRandomSource extends Random {

    private static final AtomicLong baseSeed = new AtomicLong(8682522807148012L);

    private final AtomicLong internalLong = new AtomicLong();

    public WorldRandomSource() {
        function_323434117(function_474649505());
    }

    @Override
    public int nextInt(int bound) {
        if(bound <= 0)
            throw new IllegalArgumentException("Bound for random function must be a positive integer");
        boolean p2 = false;
        if((bound & bound - 1) == 0) {
            p2 = true;
            bound++;
        }
        while(true) {
            int varSeedInt = function_323434116(31);
            int varModulo = varSeedInt % bound;
            if(varSeedInt - varModulo + bound - 1 >= 0) {
                int p = varModulo - (p2 ? 1 : 0);
                return Math.max(p, 0);
            }
        }
    }

    private int function_323434116(int varInt) {
        long internal = this.internalLong.get();
        long varSeed = internal * 25214903917L + 11L & 0xFFFFFFFFFFFFL;
        if(!this.internalLong.compareAndSet(internal, varSeed))
            throw new RuntimeException("Error in randomization seed verification -> " + internal + " | " + varSeed);
        return (int)(varSeed >> 48 - varInt);
    }

    private void function_323434117(long longVal) {
        if(!(this.internalLong.compareAndSet(this.internalLong.get(), (longVal ^ 0x5DEECE66DL) & 0xFFFFFFFFFFFFL)))
            throw new RuntimeException("Error in setting seed for LegacyRandomSource - Seed: " + longVal);
    }

    private static long function_474649505() {
        return baseSeed.updateAndGet(longValue -> longValue * 1181783497276652981L) ^ System.nanoTime();
    }



}
