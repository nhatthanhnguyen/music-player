package com.thanh.musicplayer;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Song implements Serializable {
    private String id;
    private String songName;
    private String artistName;
    private String url;
    private String songImageUrl;
    private int length;
    private boolean selected;
}
