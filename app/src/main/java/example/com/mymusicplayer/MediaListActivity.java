package example.com.mymusicplayer;

        import android.app.ListActivity;
        import android.app.LoaderManager;
        import android.content.ComponentName;
        import android.content.Context;
        import android.content.CursorLoader;
        import android.content.Intent;
        import android.content.Loader;
        import android.content.ServiceConnection;
        import android.os.Bundle;
        import android.database.Cursor;
        import android.os.IBinder;
        import android.provider.MediaStore;
        import android.util.Log;
        import android.view.View;
        import android.widget.ListView;
        import android.widget.SimpleCursorAdapter;


public class MediaListActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    //CLass for songs list display and allows user to select their song.Fragment activity helps the loader methods.

    //**class to query the content provider
    //**implement the cursorLoader to perform cursor queries on a seperate thread to the main UI thread as recomended by Android Studio
    //**http://developer.android.com/guide/topics/providers/content-provider-basics.html " In actual code, however, you should do queries...
    //** ...asynchronously on a separate thread. One way to do this is to use the CursorLoader class, which is described in more detail in the  Loaders guide"

    //http://developer.android.com/guide/topics/ui/layout/listview.html

    private MyBoundService.MyBinder myService = null;
    SimpleCursorAdapter adapter;
    public int currentSongIndex = 0;


    String colsToDisplay[] = new String[] //selection
            {
                    MediaStore.Audio.AudioColumns.ARTIST,
                    MediaStore.Audio.AudioColumns.ALBUM,
            };

    int[] colResIds = new int[] //which columns go into which views
            {
                    android.R.id.text1,
                    android.R.id.text2
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(0, null, this);


        this.bindService(new Intent(this, MyBoundService.class), myServiceConnection, Context.BIND_AUTO_CREATE);

        adapter = new SimpleCursorAdapter(this, //create an empty list adapter to display
                android.R.layout.simple_list_item_2,
                null,
                colsToDisplay,
                colResIds,
                0);

        setListAdapter(adapter);


        Log.d("g54", "MediaListActivity - showing media");
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //**creates a cursor to query the content provider on a separate thread and returns the data in the cursor loader
        CursorLoader cursorLoader = new CursorLoader(this,
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                Constants.audioColumns,
                null,
                null,
                null);
        Log.d("g54", "MediaListActivity - query the content resolver and return the cursorLoader");
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        //**when the cursorLoader has loaded the data and the result is returned, swap the data into the cursor to display in the list
        Log.d("g54", "MediaListActivity - cursorLoader finished");
        adapter.swapCursor(cursor);
        Log.d("g54", "MediaListActivity - number of songs: "
                + adapter.getCount());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        //when the loader is reset the cursor is set to null as the data is now obsolete
        Log.d("g54", "MediaListActivity - cursorLoader onReset");
        adapter.swapCursor(null);
    }

    @Override
    protected void onListItemClick(ListView listview, View view, int position, long id) {
        //**user clicks the song to play. cursor gets the song position and sends it on bind to be prepared and played
        Log.d("g54", "MediaListActivity - on item click list");
        Cursor c = (Cursor) listview.getItemAtPosition(position);
        String songName = c.getString(c.getColumnIndex(Constants.audioColumns[3]));
        Log.d("g54", "MediaListActivity - onListItemClick() song is " + songName);
        int currentSongIndex = (int) position;
        Log.d("g54", "MMediaListActivity - onListItemClick() song index is " + currentSongIndex);
        myService.returnCurrentSong(currentSongIndex);
        myService.playFile();
    }

    public void onClickPause(View v) {
        myService.onPause();
    }

    public void onClickStop(View v) {
        //**on click Stop stop audio and un bind from the service
        myService.onStop();
        Log.d("g54", "Main Activity - on click stop");
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

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myService = (MyBoundService.MyBinder) service;
            Log.d("g54", "MediaListActivity - on Service my bound service");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            myService = null;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("g54", "MediaListActivity - onDestroy");

        if (myServiceConnection != null) {
            unbindService(myServiceConnection);
            myServiceConnection = null;
        }
    }


}

