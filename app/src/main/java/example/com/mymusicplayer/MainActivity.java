package example.com.mymusicplayer;

        import android.content.ComponentName;
        import android.content.Context;
        import android.content.Intent;
        import android.content.ServiceConnection;
        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.os.IBinder;
        import android.os.Bundle;
        import android.support.v7.app.AppCompatActivity;
        import android.util.Log;
        import android.view.View;
        import android.widget.ImageView;


public class MainActivity extends AppCompatActivity {

    private MyBoundService.MyBinder myService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("g54", "Main Activity - setContentView()");

        this.bindService(new Intent(this, MyBoundService.class), myServiceConnection, Context.BIND_AUTO_CREATE);

        Bitmap currentSongArtwork = BitmapFactory.decodeResource(getResources(), R.drawable.defaultartwork);
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageBitmap(currentSongArtwork);
    }

    //Open List view to view songs
    public void onClickShowList(View v) {
        Intent intent = new Intent(MainActivity.this, MediaListActivity.class); //create intent to open a new activity to show song list
        startActivity(intent); //send intent and open new activity
        Log.d("g54", "Main Activity - on click to view artists/albums");
    }

    public void onClickStop(View v) {
        //**on click Stop stop audio and un bind from the service
        myService.onStop();
        Log.d("g54", "Main Activity - on click stop");
    }

    public void onClickPause(View v) {
        //*onPause and play button so the user has to un pause before they can play
        myService.onPause();
    }

    public void onClickSkipForward(View v) {
        myService.skipForward();
        Log.d("g54", "MainActivity - onClickSkipForward()");
    }

    public void onClickSkipBackward(View v) {
        myService.skipBackward();
        Log.d("g54", "MainActivity - onClickSkipBackward()");
    }

    private ServiceConnection myServiceConnection = new ServiceConnection() {
        //**end the service gracefully and un bind from the service only when the user has finished with the application
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myService = (MyBoundService.MyBinder) service;
            Log.d("g54", "MainActivity - on ServiceConnected - my bound service");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            myService = null;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("g54", "Main Activity - onDestroy");

        if (myServiceConnection != null) {
            unbindService(myServiceConnection);
            myServiceConnection = null;

        }
    }

}
