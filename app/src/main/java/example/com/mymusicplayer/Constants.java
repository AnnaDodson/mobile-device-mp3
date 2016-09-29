package example.com.mymusicplayer;

import android.provider.MediaStore;

/**
 * Created by itxad7 on 10/12/2015.
 */

public class Constants {

    //the Constants class contains database constants for querying the content provider


    static String[] audioColumns = new String[]{ //projection

            MediaStore.Audio.AudioColumns._ID,
            MediaStore.Audio.AudioColumns.ARTIST,
            MediaStore.Audio.AudioColumns.ALBUM,
            MediaStore.Audio.AudioColumns.TITLE,
            MediaStore.Audio.AudioColumns.DATA,
            MediaStore.Audio.AudioColumns.ALBUM_KEY,
            //MediaStore.Audio.AlbumColumns.ALBUM_ART
    };

    static String[] columnAlbum = new String[]{
            MediaStore.Audio.AlbumColumns.ALBUM_ART,
            MediaStore.Audio.AlbumColumns.ALBUM_KEY,};


}
