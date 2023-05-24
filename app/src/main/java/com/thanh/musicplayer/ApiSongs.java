package com.thanh.musicplayer;

import java.util.List;

public class ApiSongs {
    public static final List<Song> songs = List.of(
            new Song("1", "Tự sự", "Orange", "https://res.cloudinary.com/dbk0cmzcb/video/upload/v1672369716/prxgst9115azlrutb9vv.mp3", "https://i.scdn.co/image/ab67616d00001e0282085c362d59e4aeac21f44b", 217, false),
            new Song("2", "Em hát ai nghe", "Orange", "https://res.cloudinary.com/dbk0cmzcb/video/upload/v1672369881/jzwmhdisrakfujnfcrjb.mp3", "https://i.scdn.co/image/ab67616d00001e02f60317baffa0e419fd8e24dc", 331, false),
            new Song("3", "Blue Tequila", "Táo", "https://res.cloudinary.com/dbk0cmzcb/video/upload/v1672370135/wzeteonza3mhwz6sor2w.mp3", "https://i.scdn.co/image/ab67616d00001e024f68a75b0f7b7ffcb2ce88a2", 202, false),
            new Song("4", "25", "Táo", "https://res.cloudinary.com/dbk0cmzcb/video/upload/v1672370221/cwjgry7aktbrbs6b8jsd.mp3", "https://i.scdn.co/image/ab67616d00001e0229939a465945472f76b7e35d", 300, false),
            new Song("5", "Tương Tư", "Táo", "https://res.cloudinary.com/dbk0cmzcb/video/upload/v1672370275/hz95nvzpsybf37bt54qo.mp3", "https://i.scdn.co/image/ab67616d00001e02c0fb9ad617485ae2343b3fee", 225, false)
    );

    public static Song findSongById(String id) {
        for (Song song : songs) {
            if (song.getId().equals(id)) {
                return song;
            }
        }
        return null;
    }

    public static void toggleSelected(String id) {
        for (Song song : songs) {
            song.setSelected(song.getId().equals(id));
        }
    }

    public static Song skipToNext(String id, int repeatMode) {
        int i;
        for (i = 0; i < songs.size(); ++i) {
            if (songs.get(i).getId().equals(id)) {
                break;
            }
        }
        if (repeatMode == 1) {
            if (i == songs.size() - 1) {
                return songs.get(0);
            }
            return songs.get(i + 1);
        }
        if (i == songs.size() - 1) {
            return null;
        }
        return songs.get(i + 1);
    }

    public static Song skipToPrevious(String id, int repeatMode) {
        int i;
        for (i = 0; i < songs.size(); ++i) {
            if (songs.get(i).getId().equals(id)) {
                break;
            }
        }
        if (repeatMode == 1) {
            if (i == 0) {
                return songs.get(songs.size() - 1);
            }
            return songs.get(i - 1);
        }
        if (i == 0) {
            return null;
        }
        return songs.get(i - 1);
    }

    public static Song skipShuffle() {
        int i = Utils.generateRandomNumberInRange(songs.size());
        return songs.get(i);
    }
}
