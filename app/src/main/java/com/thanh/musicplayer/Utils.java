package com.thanh.musicplayer;

import java.util.Random;

public class Utils {
    public static String formatTime(int milliseconds) {
        long seconds = (milliseconds / 1000) % 60;
        long minutes = (milliseconds / (1000 * 60)) % 60;
        long hours = (milliseconds / (1000 * 60 * 60)) % 24;

        if (hours == 0) {
            return String.format("%02d:%02d", minutes, seconds);
        }
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static int generateRandomNumberInRange(int n) {
        Random random = new Random();
        return random.nextInt(n);
    }
}
