package com.thanh.musicplayer;

public class Utils {
    public static String secToTime(int sec) {
        int second = sec % 60;
        int minute = sec / 60;
        if (minute >= 60) {
            int hour = minute / 60;
            minute %= 60;
            return String.format("%s:%s:%s", hour < 10 ? "0" + hour : hour,
                    minute < 10 ? "0" + minute : minute,
                    second < 10 ? "0" + second : second);
        }
        return String.format("%s:%s", minute < 10 ? "0" + minute : minute,
                second < 10 ? "0" + second : second);
    }
}
