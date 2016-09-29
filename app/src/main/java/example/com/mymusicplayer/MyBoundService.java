package example.com.mymusicplayer;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by itxad7 on 09/12/2015.
 */

public class MyBoundService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    MediaPlayer myMediaPlayer;
    MainActivity myMainActivity = null;
    private final IBinder binder = new MyBinder();
    public int currentSongIndex = 0;
    private String currentSongUri;
    private String currentSongTitle;
    private String currentSongArtist;
    public ArrayList<String> mediaList;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("g54", "MyBoundService - onCreate() service onCreate method called");
        myMainActivity = new MainActivity();
        mediaList = new ArrayList<>();
        myMediaPlayer = new MediaPlayer();
        myMediaPlayer.setOnPreparedListener(this);
        myMediaPlayer.setOnCompletionListener(this);
        Log.d("g54", "MyBoundService - onCreate() song index is: " + currentSongIndex);
        mediaContent();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("g45", "MyBoundService - onBind return binder");
        return binder;
    } //on bind from Main Activity on create


    public class MyBinder extends Binder {

        public void playFile() {
            Log.d("g45", "MyBoundService.MyBinder - playFile");
            MyBoundService.this.playAFile();
            Log.d("g54", "onPlay play");
        }

        public void onStop() {
            MyBoundService.this.onStop();
            Log.d("g54", "MyBoundService.MyBinder - onStop stop");
        }

        public void onStart() {
            MyBoundService.this.onStart();
            Log.d("g54", "MyBoundService.MyBinder - on prepared media service play");
        }

        public void onPause() {
            MyBoundService.this.onPause();
        }

        public void skipForward() {
            MyBoundService.this.onSkipForward(currentSongIndex);
            Log.d("g54", "MyBoundService.MyBinder - skipForward() send to onSkipForward() with currentSongIndex: " + currentSongIndex);
        }

        public void skipBackward() {
            MyBoundService.this.onSkipBackward(currentSongIndex);
            Log.d("g54", "MyBoundService.MyBinder - skipBackward()send to onSkipBackward() with currentSongIndex: " + currentSongIndex);
        }

        public void returnCurrentSong(int songIndex) {
            //**sets the current song list index number, used when user selects a song
            setNewSong(songIndex); //send the new index to update the song information
        }
    }


    private void setNewSong(int currentSongIndex) {
        setCurrentSongIndex(currentSongIndex); //set the new index
        Log.d("g54", "MyBoundService - setNewSong() song index is: " + currentSongIndex);
        retrieveCurrentSongData(currentSongIndex); //query the content provider to get the new song details. this sets the uri and song title
    }

    public int setCurrentSongIndex(int songIndex) {
        //**sets the current song list index number
        currentSongIndex = songIndex;
        Log.d("g54", "MyBoundService - setCurrentSongIndex() song index is: " + currentSongIndex);
        return currentSongIndex;
    }

    public String setCurrentSongUri(String songUri) {
        //**sets the current song uri to play
        currentSongUri = songUri;
        Log.d("g54", "MyBoundService - setCurrentSongUri() song uri is: " + currentSongUri);
        return currentSongUri;
    }

    public String setCurrentSongTitle(String SongTitle) {
        //**sets the current song Title used for notifications
        currentSongTitle = SongTitle;
        Log.d("g54", "MyBoundService - setCurrentSongTitle() song Title is: " + currentSongTitle);
        return currentSongTitle;
    }

    public String setCurrentSongArtist(String songArtist) {
        //**sets the current song Artist. used for notifications
        currentSongArtist = songArtist;
        Log.d("g54", "MyBoundService - setCurrentSongTitle() song Title is: " + currentSongArtist);
        return currentSongArtist;
    }


    public int onStartCommand(Intent intent, int flags, int startId) {
        //**Step 1 - on start command send the audio to the on prepared state. audio can only play when it's prepared
        myMediaPlayer = new MediaPlayer();
        myMediaPlayer.setOnPreparedListener(this);
        Log.d("g54", "set on prepared listener");
        return 0;
    }

    public void onStart() {
        //**Step 2 - only once audio is in prepared state can it be played so it does not block the main thread and cause delay
        onPrepared(myMediaPlayer);
        Log.d("g54", "MyBoundService - onStart() on prepared media service play");
    }

    public void onPrepared(MediaPlayer myMediaPlayer) {
        //**Step 3 -waits for the on prepared state message before sending to play audio
        myMediaPlayer.start();
        showNotification();
        Log.d("g54", "MyBoundService - onPrepared()  media service play");
    }

    public void onStop() {
        //**release the music player once stopped to save resources. will need to be send for audio prepared state before resuming playback
        myMediaPlayer.stop();
        myMediaPlayer.release();
        Log.d("g54", "MyBoundService - onStop() stop");
    }

    public void onPause() {
        //*//**on click play, check if media player is active (was paused) if so resume current song or send to play which will play default song
        AudioManager manager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        if (manager.isMusicActive()) {
            Log.d("g54", "MyBoundService - checkPause() if music player was paused, resume");
            myMediaPlayer.pause();
        } else {
            myMediaPlayer.start();
            Log.d("g54", "MyBoundService - onPause() pause");
        }
    }

    private void showNotification() {
        NotificationCompat.Builder mediaNotification =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.defaultartwork)
                        .setContentTitle(currentSongTitle)
                        .setContentText(currentSongArtist);

        Intent resultIntent = new Intent(this, MainActivity.class);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mediaNotification.setContentIntent(resultPendingIntent);

        NotificationManager mediaNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int mID = 0;
        mediaNotificationManager.notify(mID, mediaNotification.build());
    }

    private void musicPlaying() {
        //**if the user selects a new song from the list or skips forward/backward this method stops the current music player
        AudioManager manager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        if (manager.isMusicActive()) {
            myMediaPlayer.stop();
            Log.d("g54", "MyBoundService - musicPlaying() check if music is playing");
        }
    }

    public void playAFile() {
        //**gets the file path for the users selected song. Sends new song to prepared state to starts playing audio
        Log.d("g54", "MyBoundService - playAfile() method called");
        Log.d("g54", "MyBoundService playAFile() current song title: " + currentSongTitle);
        Log.d("g54", "MyBoundService playAFile() current song uri: " + currentSongUri);
        Log.d("g54", "MyBoundService playAFile() current song index: " + currentSongIndex);
        setNewSong(currentSongIndex); //gets the new song to be played
        musicPlaying();
        myMediaPlayer = new MediaPlayer(); //instantiate the new media player
        myMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        myMediaPlayer.setOnPreparedListener(this);
        myMediaPlayer.setOnCompletionListener(this);
        try {
            myMediaPlayer.setDataSource(getApplicationContext(), Uri.parse(currentSongUri));
        } catch (IOException e) {
            Log.d("g54", "MyBoundService playAFile() printStackTrace " + currentSongTitle);
            e.printStackTrace();
            myMediaPlayer = new MediaPlayer();
            setNewSong(0);
            Toast.makeText(getApplicationContext(), "Song not Found", Toast.LENGTH_SHORT).show();
        }
        myMediaPlayer.prepareAsync(); //takes media file path and sends it to be prepared ready to play
    }

    public void onSkipForward(int currentSongIndex) {
        //**skips the track to the next song
        if (currentSongIndex < mediaList.size() - 1) { //if current song index is less than the list of songs by at least one
            int newCurrentSongIndex = currentSongIndex + 1;
            setNewSong(newCurrentSongIndex);
            playAFile();
            Log.d("g54", "MyBoundService onSkipForward() start playing next song in the list: " + newCurrentSongIndex);
        } else { //if current song index is anything else, play the first song in the list
            int newCurrentSongIndex = 0;
            setNewSong(newCurrentSongIndex);
            playAFile();
            Log.d("g54", "MyBoundService onSkipForward() end of the song list so play first song: " + newCurrentSongIndex);
        }
    }

    public void onSkipBackward(int currentSongIndex) {
        //**skips the track to the previous song
        Log.d("g54", "MyBoundService onSkipBackward() current song index: " + currentSongIndex);
        if (currentSongIndex < mediaList.size() - 1) { //if current song index is less than the list of songs by at least one
            if (currentSongIndex > 0) {
                int newCurrentSongIndex = currentSongIndex - 1;
                setNewSong(newCurrentSongIndex);
                playAFile();
                Log.d("g54", "MyBoundService onSkipBackward() start playing next song in the list, new song index: " + newCurrentSongIndex);
            }
        } else { //if current song index is anything else, play the first song in the list
            int newCurrentSongIndex = (mediaList.size() - 1);
            setCurrentSongIndex(newCurrentSongIndex);
            playAFile();
            Log.d("g54", "MyBoundService onSkipBackward() end of the song list so play first song, new song index: " + newCurrentSongIndex);
        }
    }

    @Override
    public void onCompletion(MediaPlayer myMediaPlayer) {
        //**Android Media Player method to listen for when the media track finishes, the following method is executed. Move onto the next song in the list
        Log.d("g54", "MyBoundService onCompletion() end of the song");
        onSkipForward(currentSongIndex);
    }

    public void retrieveCurrentSongData(int currentSongIndex) {
        //**query the content provider for song uri at the position in the list to play and title to display
        Log.d("g54mdp", "MyBoundService - retrieveCurrentSongData() current song index: " + currentSongIndex);
        Cursor mediaCursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                Constants.audioColumns,
                null,
                null,
                null);
        try {
            if (mediaCursor != null && mediaCursor.getCount() > 0) {
                mediaCursor.moveToPosition(currentSongIndex);
                {
                    String songTitle = mediaCursor.getString(mediaCursor.getColumnIndex(Constants.audioColumns[3]));
                    String songArtist = mediaCursor.getString(mediaCursor.getColumnIndex(Constants.audioColumns[1]));
                    String songUri = mediaCursor.getString(mediaCursor.getColumnIndex(Constants.audioColumns[4]));
                    String albumKey = mediaCursor.getString(mediaCursor.getColumnIndex(Constants.audioColumns[5]));
                    Log.d("g54", "MyBoundServicesong retrieveCurrentSongData() songTitle is  " + songTitle);
                    Log.d("g54", "MyBoundServicesong retrieveCurrentSongData() songUri is  " + songUri);
                    Log.d("g54", "MyBoundServicesong retrieveCurrentSongData() albumKey is" + albumKey);
                    Log.d("g54", "MyBoundServicesong retrieveCurrentSongData() songArtist is" + songArtist);
                    setCurrentSongUri(songUri);
                    setCurrentSongTitle(songTitle);
                    setCurrentSongArtist(songArtist);
                    mediaCursor.close();
                }
            }
        } catch (UnsupportedOperationException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Songs not Found", Toast.LENGTH_SHORT).show();
        }
    }


    public ArrayList<String> mediaContent() {
        Log.d("g54mdp", "MediaContent - mediaContent()");
        //**queries the content provider to return a list of the songs on the device. Then the index number can be used to query the song data
        Cursor mediaCursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                Constants.audioColumns,
                null,
                null,
                null);
        try {
            if (mediaCursor != null && mediaCursor.getCount() > 0) {
                mediaCursor.moveToFirst();
                {
                    do {
                        String pathName = (mediaCursor.getString(mediaCursor.getColumnIndex(Constants.audioColumns[4])));
                        String songTitle = mediaCursor.getString(mediaCursor.getColumnIndex(Constants.audioColumns[3]));
                        mediaList.add(pathName);
                        Log.d("g54mdp", "MediaContent - mediaContent() songTitle: " + songTitle);
                        Log.d("g54mdp", "MediaContent - mediaContent() list size: " + mediaList.size());
                    }
                    while (mediaCursor.moveToNext());
                }
                mediaCursor.close();
            }
        } catch (UnsupportedOperationException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Songs not Found", Toast.LENGTH_SHORT).show();
        }
        return mediaList;
    }


}
