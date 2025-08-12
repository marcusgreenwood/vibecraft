package com.vibecraft.config;

import java.util.Random;

public class ExplosionConfig {

    private static float fixedMultiplier = -1;
    private static int minMultiplier = 2;
    private static int maxMultiplier = 20;
    private static boolean useRandom = true;

    private static final Random random = new Random();

    public static void setMultiplier(float multiplier) {
        fixedMultiplier = multiplier;
        useRandom = false;
    }

    public static void setRandomMultiplier(int min, int max) {
        minMultiplier = min;
        maxMultiplier = max;
        useRandom = true;
    }

    public static void resetMultiplier() {
        useRandom = true;
        minMultiplier = 2;
        maxMultiplier = 20;
    }

    public static float computeMultiplier() {
        if (useRandom) {
            if (minMultiplier >= maxMultiplier) {
                return minMultiplier;
            }
            return minMultiplier + random.nextInt(maxMultiplier - minMultiplier + 1);
        } else {
            return fixedMultiplier;
        }
    }

    public static String getCurrentConfig() {
        if (useRandom) {
            return "Random(" + minMultiplier + "x - " + maxMultiplier + "x)";
        } else {
            return String.format("Fixed(%.2fx)", fixedMultiplier);
        }
    }
}