package org.nypl.findawayclientlibrary;


import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.media.session.MediaControllerCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import io.audioengine.mobile.Content;
import io.audioengine.mobile.config.LogLevel;
import io.audioengine.mobile.AudioEngine;
import io.audioengine.mobile.AudioEngineException;

import io.audioengine.mobile.DownloadEvent;
import io.audioengine.mobile.DownloadStatus;

import io.audioengine.mobile.persistence.DeleteRequest;
import io.audioengine.mobile.persistence.DownloadEngine;
import io.audioengine.mobile.persistence.DownloadRequest;
import io.audioengine.mobile.persistence.DownloadType;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;




/**
 * Loads and exchanges fragments that are responsible for audio player UI
 * and audiobook-associated sub-screens.
 *
 * Communicates with the download portion of the sdk.
 *
 * Communicates with the audio playing portion of the sdk.  Acts as an intermediary between
 * the background player service and the front-end ui fragment.
 *
 * TODO:  Figure out audio playback.
 *
 * NOTE:  Saw this error, have not been able to duplicate it since.  Might have been an emulator glitch:
 * E/AudioFlinger: not enough memory for AudioTrack size=131296
 *
 * 10-24 18:31:34.945 11861-11861/org.nypl.findawaysdkdemo E/AndroidRuntime: FATAL EXCEPTION: main
 * Process: org.nypl.findawaysdkdemo, PID: 11861
 * java.lang.RuntimeException: Unable to start activity ComponentInfo{org.nypl.findawaysdkdemo/org.nypl.findawayclientlibrary.PlayBookActivity}: java.lang.NullPointerException: Attempt to invoke virtual method 'void android.view.View.setOnClickListener(android.view.View$OnClickListener)' on a null object reference
 * at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:2665)
 * at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:2726)
 * at android.app.ActivityThread.-wrap12(ActivityThread.java)
 * at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1477)
 * at android.os.Handler.dispatchMessage(Handler.java:102)
 * at android.os.Looper.loop(Looper.java:154)
 * at android.app.ActivityThread.main(ActivityThread.java:6119)
 * at java.lang.reflect.Method.invoke(Native Method)
 * at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:886)
 * at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:776)
 * Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'void android.view.View.setOnClickListener(android.view.View$OnClickListener)' on a null object reference
 * at org.nypl.findawayclientlibrary.PlayBookActivity.onCreate(PlayBookActivity.java:155)
 * at android.app.Activity.performCreate(Activity.java:6679)
 * at android.app.Instrumentation.callActivityOnCreate(Instrumentation.java:1118)
 * at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:2618)
 * at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:2726) 
 * at android.app.ActivityThread.-wrap12(ActivityThread.java) 
 * at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1477) 
 * at android.os.Handler.dispatchMessage(Handler.java:102) 
 * at android.os.Looper.loop(Looper.java:154) 
 * at android.app.ActivityThread.main(ActivityThread.java:6119) 
 * at java.lang.reflect.Method.invoke(Native Method) 
 * at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:886) 
 * at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:776) 
 * 10-24 18:31:34.948 1735-2207/system_process W/ActivityManager:   Force finishing activity org.nypl.findawaysdkdemo/org.nypl.findawayclientlibrary.PlayBookActivity
 *
 * Created by daryachernikhova on 9/14/17.
 */
public class PlayBookActivity extends BaseActivity implements View.OnClickListener, View.OnLongClickListener, Observer<DownloadEvent> {
  // so can filter all log msgs belonging to my app
  private static final String APP_TAG = "FDLIB.";
  // so can do a search in log msgs for just this class's output
  private static final String TAG = APP_TAG + "PlayBookActivity";

  // corresponds to book resources path on the file system
  //String bookId = null;

  PlayBookFragment playBookFragment = null;

  DownloadEngine downloadEngine;
  private DownloadRequest downloadRequest;

  // follows all download engine events
  private Subscription eventsSubscription;


  // Kevin's
  //String sessionId = "964b5796-c67f-492d-8024-a69f3ba9be53";
  // mine:
  String sessionId = "139bbcd9-6426-474d-9b02-ed3fc438be66";

  // Kevin's test value
  // String contentId = "83380";
  // Darya's value keyed to the fulfillmentId of the book NYPL bought through Bibliotheca
  String contentId = "102244";

  // Kevin's test value
  //String license = "5744a7b7b692b13bf8c06865";
  // Darya's value keyed to licenseId of the book NYPL bought through Bibliotheca
  //String license = "57db1411afde9f3e7a3c041b";
  String license = "580e7da000deff122d48a1c9";

  // the part we're downloading right now
  int part = 0;

  // the chapter we're downloading right now
  int chapter = 1;



  /* ---------------------------------- LIFECYCLE METHODS ----------------------------------- */

  /**
   * Loads player UI fragment.
   * Triggers the creation of media session-holding objects.
   *
   * We are going to play the music in the AudioService class, but control it from the Activity class,
   * where the application's user interface operates.  To accomplish this, we need bind to the Service class.
   * This used to be done manually, but is now handled automagically by the MediaSession system.
   *
   * @param savedInstanceState
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.playback_activity_main);

    // change the title displayed in the top bar from the app name (default) to something indicative
    getSupportActionBar().setTitle("Audio Player UI Here");

    // Check that the activity is using the layout version with the fragment_container FrameLayout
    if (findViewById(R.id.fragment_container) != null) {

      // If we're being restored from a previous state, we don't need to do anything,
      // return or else we could end up with overlapping fragments.
      if (savedInstanceState != null) {
        return;
      }

      // Create a new Fragment that offers the user a player interface with media control buttons.
      playBookFragment = new PlayBookFragment();

      // In case this activity was started with special instructions from an
      // Intent, pass the Intent's extras to the fragment as arguments
      playBookFragment.setArguments(getIntent().getExtras());

      // Add the first fragment to the 'fragment_container' view component.
      // NOTE:  Because the fragment has been added to the FrameLayout container at runtime
      // instead of defining it in the activity's layout with a <fragment> element
      // the activity can remove the fragment and replace it with a different one later on in the code.
      this.loadFragment(playBookFragment, R.id.fragment_container, false);

    } else {
      // layout doesn't allow fragment loading
      throw new IllegalStateException("Missing layout container with id 'fragment_container'. Cannot continue.");
    }


    // Only update from the intent if we are not recreating from a config change:
    if (savedInstanceState == null) {
      // read the passed-in book id later on
      Intent intent = getIntent();
      if (intent != null) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            //bookId = extras.getString("audiobook_id", null);
        }
      }
    }


    // one-time call to start up the AudioEngine service
    this.initAudioEngine();

    // ask the AudioEngine to start a DownloadEngine
    this.initDownloadEngine();

  }// onCreate



  /**
   * Stop the media playback when this activity is destroyed, and clean up resources.
   *
   * TODO:  what sdk calls should be made here?
   */
  @Override
  protected void onDestroy() {
    super.onDestroy();
  }


  /**
   * If this activity is not currently hopping, it shouldn't be listening to events.
   */
  @Override
  protected void onPause() {
    super.onPause();

    if (!eventsSubscription.isUnsubscribed()) {
      eventsSubscription.unsubscribe();
    }
  }


  /**
   * Called either after onCreate()-onStart(), or after the activity is restored after
   * having been paused.  Resume listening to download events while activity is in foreground.
   */
  @Override
  protected void onResume() {
    super.onResume();

    // a stream of _all_ download events for the supplied content id
    // the onCompleted(), onError() and onNext() methods are the ones implemented in the activity itself.
    eventsSubscription = downloadEngine.events(contentId).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(this);

    // a stream of just download status changes for the supplied content id
    downloadEngine.getStatus(contentId).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).take(1).subscribe(new Observer<DownloadStatus>() {

      @Override
      public void onCompleted() {

      }

      @Override
      public void onError(Throwable e) {

      }

      @Override
      public void onNext(DownloadStatus downloadStatus) {
        if (downloadStatus == DownloadStatus.DOWNLOADED) {
          if (playBookFragment != null) {
            playBookFragment.redrawDownloadButton(getResources().getString(R.string.delete));
          } else {
            Toast.makeText(getApplicationContext(), R.string.delete, Toast.LENGTH_LONG).show();
          }
        } else {
          if (downloadStatus == DownloadStatus.QUEUED || downloadStatus == DownloadStatus.DOWNLOADING) {
            if (playBookFragment != null) {
              playBookFragment.redrawDownloadButton(getResources().getString(R.string.pause));
            } else {
              Toast.makeText(getApplicationContext(), R.string.pause, Toast.LENGTH_LONG).show();
            }
          } else {
            if (downloadStatus == DownloadStatus.PAUSED || downloadStatus == DownloadStatus.NOT_DOWNLOADED) {
              if (playBookFragment != null) {
                playBookFragment.redrawDownloadButton(getResources().getString(R.string.download));
              } else {
                Toast.makeText(getApplicationContext(), R.string.download, Toast.LENGTH_LONG).show();
              }
            }
          }
        }
      }
    }); //downloadEngine.status.subscribe


    downloadEngine.getProgress(contentId).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).take(1).subscribe(new Observer<Integer>() {

      @Override
      public void onCompleted() {

        Log.d(TAG, "Initial progress complete.");
      }

      @Override
      public void onError(Throwable e) {

        Log.d(TAG, "Initial progress error: " + e.getMessage());
      }

      @Override
      public void onNext(Integer progress) {

        Log.d(TAG, "Got initial progress " + progress);

        if (playBookFragment != null) {
          playBookFragment.redrawProgress(progress, 0);
        }
      }
    }); //downloadEngine.progress.subscribe

  }


  /**
   * TODO:  What sdk calls need to be made when the player is being restored?
   *
   * Called after onStart() and before onPostCreate(), and only if the activity is being
   * re-initialized from a previously saved state.
   * Don't need to check if Bundle is null, unlike you do in onCreate().
   * NOTE:  Call savedInstanceState.get[...]() methods after calling super().
   *
   * @param savedInstanceState
   */
  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    // always call first, so can restore view hierarchy
    super.onRestoreInstanceState(savedInstanceState);
  }


  /**
   * TODO:  what sdk methods clean up the scene, but let the player to keep playing?
   *
   * Called on activity stop, attempts to save activity state into memory,
   * so it can be reconstituted if the activity is restored.
   * NOTE:  Always call the superclass implementation, so it can save the state of the view hierarchy.
   * NOTE:  In order for the Android system to restore the state of the views in your activity,
   * each view must have a unique ID, supplied by the android:id attribute.
   * NOTE:  Call savedInstanceState.put[...]() methods before calling super().
   *
   * @param savedInstanceState
   */
  @Override
  protected void onSaveInstanceState(Bundle savedInstanceState) {
    super.onSaveInstanceState(savedInstanceState);
  }


  /**
   * Start the AudioService instance when the Activity instance starts.
   * Pass the song list we've assembled to the AudioService.
   *
   *
   */
  @Override
  protected void onStart() {
    super.onStart();
    Log.d(TAG, "Activity.onStart");

  }// onStart


  /**
   * TODO:  what sdk methods clean up the scene, but let the player to keep playing?
   *
   */
  @Override
  protected void onStop() {
    super.onStop();
  }


  /* ---------------------------------- /LIFECYCLE METHODS ----------------------------------- */



  /* ------------------------------------ PLAYBACK EVENT HANDLERS ------------------------------------- */


  /**
   * TODO: AudioEngine needs the following setup: [...].
   */
  private void initAudioEngine() {
    try {
      AudioEngine.init(this, sessionId, LogLevel.VERBOSE);
    } catch (Exception e) {
      Log.e(TAG, "Error getting download engine: " + e.getMessage());
      e.printStackTrace();
    }
  }


  /**
   * TODO: DownloadEngine needs the following setup: [...].
   */
  private void initDownloadEngine() {
    try {
      downloadEngine = AudioEngine.getInstance().getDownloadEngine();
    } catch (AudioEngineException e) {
      // Call to getDownloadEngine will throw an exception if you have not previously
      // called init() on AudioEngine with a valid Context and Session.
      Log.e(TAG, "Error getting download engine: " + e.getMessage());
      e.printStackTrace();
    } catch (Exception e) {
      Log.e(TAG, "Error getting download engine: " + e.getMessage());
      e.printStackTrace();
    }
  }


  /**
   * Make a download request, and ask the download engine to fulfill it.
   */
  private void downloadAudio() {

    // To start downloading the audio files for the chapter supplied: A valid chapter is a combination of content (book) id, part number, and chapter number.
    // NOTE: Get the list of chapters with their durations (in milliseconds) when call the get_audiobook
    // in the REST api.  The response to get_audiobook returns three sections, "active_products", "inactive_products",
    // and "audiobook".  You then use the license id from "active_products" to call checkout, and use the "chapters"
    // list from "audiobook" to make the download requests in the android sdk.  Here's some api info:
    // http://developer.audioengine.io/api/v4/class-docs/#/definitions/Audiobook .

    // NOTE:  DownloadType.TO_END gets book from specified chapter to the end of the book.
    // DownloadType.TO_END_WRAP gets from specified chapter to the end of the book, then wraps around and
    // gets all the beginning chapters, too.
    // DownloadType.SINGLE gets just that chapter.
    // The system does skip any chapters that are already downloaded.  So, if we need to re-download a chapter,
    // we'd have to delete it first then call download.

    Log.e(TAG, "before making downloadRequest, part=" + part + ", chapter=" + chapter);
    downloadRequest = DownloadRequest.builder().contentId(contentId).part(part).chapter(chapter).licenseId(license).type(DownloadType.TO_END_WRAP).build();
    Log.e(TAG, "after making downloadRequest, part=" + part + ", chapter=" + chapter);

    try {
      // Audio files are downloaded and stored under the application's standard internal files directory. This directory is deleted when the application is removed.
      Log.e(TAG, "before downloadEngine.download \n\n\n");
      downloadEngine.download(downloadRequest);
      Log.e(TAG, "after downloadEngine.download \n\n\n");
    } catch (Exception e) {
      Log.e(TAG, "Error getting download engine: " + e.getMessage());
      e.printStackTrace();
    }


    /* Extra Things I Could Do according to docs:

    // To get a DownloadRequest for a specific download request id:
    // TODO:  What does this allow me to do?
    // TODO:  Where would I get the id?  Where is the method defined?
    String requestId = "123";
    DownloadRequest downloadRequest(requestId);

    // To get all DownloadRequests:
    List<DownloadRequest> downloadRequests();

    // To pause the download for the supplied content keeping existing progress.  pauseAll() to pause all requests
    // TODO: where is the method defined?
    void pause(DownloadRequest request);

    // To cancel the download for the supplied content, removing any existing progress.  similarly, cancelAll().
    void cancel(DownloadRequest request);

    // To delete the audio files for a piece of content from the device.
    void delete(DeleteRequest request)

    // Get the download status for a book overall
    Observable<DownloadStatus> getStatus(String contentId) throws ContentNotFoundException

    // Get the download status for a specific chapter of a book
    Observable<DownloadStatus> getStatus(String contentId, Integer part, Integer chapter) throws ChapterNotFoundException

    // Get the download progress of a book as a percentage from 0 to 100
    Observable<Integer> getProgress(String contentId) throws ContentNotFoundException

    // Get the download progress of a chapter as a percentage from 0 to 100
    Observable<Integer> getProgress(String contentId, Integer part, Integer chapter) throws ChapterNotFoundException

    // Currently, you are not able to request a download for chapters from 2 different books so these are essentialy the same.
    // TODO: So, to download more than one book, I'll need to have a download queue?
    // Subscribe to all events for a given content id or request id
    Observable<DownloadEvent> events(String contentId / request id)

    */

  }


  @Override
  public void onClick(View view) {
    if (view.getId() == R.id.download_button) {

      Button downloadButton = (Button) view;

      if (downloadButton.getText().equals(getString(R.string.download))) {
        downloadAudio();

      } else if (downloadButton.getText().equals(getString(R.string.pause))) {

        downloadEngine.pause(downloadRequest);

      } else if (downloadButton.getText().equals(getString(R.string.resume))) {

        downloadEngine.download(downloadRequest);

      } else if (downloadButton.getText().equals(getString(R.string.delete))) {

        downloadEngine.delete(DeleteRequest.builder().contentId(contentId).build());
      }
    }
  }


  @Override
  public void onCompleted() {
    // ignore
  }


  @Override
  public void onError(Throwable e) {
    Log.e(TAG, "There was an error in the download process: " + e.getMessage());
    e.printStackTrace();
  }


  @Override
  public boolean onLongClick(View view) {

    if (view.getId() == R.id.download_button) {

      Button downloadButton = (Button) view;

      if (downloadButton.getText().equals(getString(R.string.pause))) {

        downloadEngine.cancel(downloadRequest);

        return true;
      }
    }

    return false;
  }


  /**
   * Catches DownloadEngine events.
   *
   * Download events are described here:  http://developer.audioengine.io/sdk/android/v7/download-engine .
   * @param downloadEvent
   */
  @Override
  public void onNext(DownloadEvent downloadEvent) {

    File filesDir = getFilesDir();
    if (filesDir.exists()) {
      Log.d(TAG, "filesDir.getAbsolutePath=" + filesDir.getAbsolutePath());
      String[] filesList = filesDir.list();
      Log.d(TAG, "filesDir.filesList=" + filesList.length);
    }

    String sharedPrefsPath = "shared_prefs/";
    File sharedPrefsDir = new File(getFilesDir(), "../" + sharedPrefsPath);
    if (sharedPrefsDir.exists()) {
      Log.d(TAG, "sharedPrefsDir.getAbsolutePath=" + sharedPrefsDir.getAbsolutePath());
      String[] filesList = sharedPrefsDir.list();
      Log.d(TAG, "sharedPrefsDir.filesList=" + filesList.length);
    }

    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
      File externalFilesDir = getExternalFilesDir(null);
      if (externalFilesDir.exists()) {
        Log.d(TAG, "externalFilesDir.getAbsolutePath=" + externalFilesDir.getAbsolutePath());
        String[] externalFilesList = externalFilesDir.list();
        Log.d(TAG, "externalFilesDir.externalFilesList=" + externalFilesList.length);
      }
    }


    Log.d(TAG, "downloadEvent.chapter=" + downloadEvent.chapter);
    Log.d(TAG, "downloadEvent.chapterPercentage=" + downloadEvent.chapterPercentage);
    Log.d(TAG, "downloadEvent.content=" + downloadEvent.content);
    Log.d(TAG, "downloadEvent.contentPercentage=" + downloadEvent.contentPercentage);
    Log.d(TAG, "downloadEvent.toString=" + downloadEvent.toString());

    if (downloadEvent.isError()) {

      // TODO:  getting E/SQLiteLog: (1) no such table: listenedEvents
      // don't think it's related to the download error.

      // NOTE:  if I use the wrong license to init AudioEngine with, I get download error, with message:
      // Download Event e6c50396-904a-4511-a5c0-acfbf9573401: 31051
      // and code 31051, which corresponds to HTTP_ERROR (see this api page for all error codes:
      // http://developer.audioengine.io/sdk/android/v7/download-engine ).
      // and also the chapter object is all nulled when onNext isError.
      // The downloadEvent stack trace is not helpful, but you can see helpful info in the stack trace
      // that's thrown from the findaway internal sdk code:
      // 10-30 19:45:33.548 8316-8316/org.nypl.findawaysdkdemo E/FDLIB.PlayBookActivity: before making downloadRequest, part=0, chapter=1
      // 10-30 19:45:33.548 8316-8316/org.nypl.findawaysdkdemo I/System.out: Sending AutoValue_DownloadRequest to onNext. Observers? true
      // 10-30 19:45:33.549 8316-8378/org.nypl.findawaysdkdemo D/OkHttp: --> POST https://api.findawayworld.com/v4/audiobooks/83380/playlists http/1.1
      // 10-30 19:45:33.549 8316-8378/org.nypl.findawaysdkdemo D/OkHttp: Content-Type: application/json; charset=UTF-8
      // 10-30 19:45:33.549 8316-8378/org.nypl.findawaysdkdemo D/OkHttp: Content-Length: 71
      // 10-30 19:45:33.549 8316-8378/org.nypl.findawaysdkdemo D/OkHttp: --> END POST
      // 10-30 19:45:33.550 1455-1482/? W/audio_hw_generic: Not supplying enough data to HAL, expected position 3501424 , only wrote 3501360
      // 10-30 19:45:33.595 8316-8378/org.nypl.findawaysdkdemo D/OkHttp: <-- 400 Bad Request https://api.findawayworld.com/v4/audiobooks/83380/playlists (45ms)
      // and some nicer stack trace, coming from the findaway sdk:
      // 10-30 19:54:28.605 13497-15009/org.nypl.findawaysdkdemo W/System.err:     at io.audioengine.mobile.persistence.Download.getPlaylist(Download.java:649)

      Toast.makeText(this, "Download error occurred: " + downloadEvent.getMessage(), Toast.LENGTH_LONG).show();

      Log.e(TAG, "downloadEvent.getMessage=" + downloadEvent.getMessage());
      Log.e(TAG, "downloadEvent.getCause=", downloadEvent.getCause());
      Log.e(TAG, "downloadEvent.code=" + downloadEvent.code);


      Log.e(TAG, "downloadEvent.getStackTrace:");
      StackTraceElement[] elements = Thread.currentThread().getStackTrace();
      for (int i = 0; i < elements.length; i++) {
        Log.e("Test", String.format("stack element[%d]: %s", i, elements[i]));
      }

      // NOTE:  The error sending is being re-worked by Findaway, and might change.  Here's the
      // description of how it currently works:
      // "Currently, if the license you supply is not found you'll get a AUDIO_NOT_FOUND.
      // If the license is found but not valid for the requested content you'll get an HTTP_ERROR.
      // If the license does not match the checkout you'll get a FORBIDDEN.
      // If the license is valid but not actually checked out you'll get a HTTP_ERROR
      // Lastly, HTTP_ERROR is currently the catch all. So, 500's on our end will result in that error code as well."
      // All codes listed here:  http://developer.audioengine.io/sdk/android/v7/download-engine .
      if (DownloadEvent.HTTP_ERROR.equals(downloadEvent.code)) {
        // decide if want to re-try downloading or throw a "sorry" message to user.
        Log.e(TAG, "DownloadEvent.HTTP_ERROR");
      } else if (DownloadEvent.FORBIDDEN.equals(downloadEvent.code)) {
        Log.e(TAG, "DownloadEvent.FORBIDDEN");
      } else if (DownloadEvent.ERROR_DOWNLOADING_FILE.equals(downloadEvent.code)) {
        Log.e(TAG, "DownloadEvent.ERROR_DOWNLOADING_FILE");
      }

    } else {
      if (downloadEvent.code.equals(DownloadEvent.DOWNLOAD_STARTED)) {

        Toast.makeText(this, getString(R.string.downloadStarted), Toast.LENGTH_SHORT).show();

        if (playBookFragment != null) {
          playBookFragment.redrawDownloadButton(getResources().getString(R.string.pause));
        }
      } else if (downloadEvent.code.equals(DownloadEvent.DOWNLOAD_PAUSED)) {

        Toast.makeText(this, getString(R.string.downloadPaused), Toast.LENGTH_SHORT).show();
        if (playBookFragment != null) {
          playBookFragment.redrawDownloadButton(getResources().getString(R.string.resume));
        }

      } else if (downloadEvent.code.equals(DownloadEvent.DOWNLOAD_CANCELLED)) {

        Toast.makeText(this, getString(R.string.downloadCancelled), Toast.LENGTH_SHORT).show();
        resetProgress();
        if (playBookFragment != null) {
          playBookFragment.redrawDownloadButton(getResources().getString(R.string.download));
        }

      } else if (downloadEvent.code.equals(DownloadEvent.CHAPTER_DOWNLOAD_COMPLETED)) {

        Toast.makeText(this, getString(R.string.chapterDownloaded, downloadEvent.chapter.friendlyName()), Toast.LENGTH_SHORT).show();

      } else if (downloadEvent.code.equals(DownloadEvent.CONTENT_DOWNLOAD_COMPLETED)) {

        Toast.makeText(this, getString(R.string.downloadComplete), Toast.LENGTH_SHORT).show();
        if (playBookFragment != null) {
          playBookFragment.redrawDownloadButton(getResources().getString(R.string.delete));
        }

      } else if (downloadEvent.code.equals(DownloadEvent.DELETE_COMPLETE)) {

        Toast.makeText(this, getString(R.string.deleteComplete), Toast.LENGTH_SHORT).show();
        resetProgress();
        if (playBookFragment != null) {
          playBookFragment.redrawDownloadButton(getResources().getString(R.string.download));
        }

      } else if (downloadEvent.code.equals(DownloadEvent.DOWNLOAD_PROGRESS_UPDATE)) {

        setProgress(downloadEvent);

      } else {

        Log.w(TAG, "Unknown download event: " + downloadEvent.code);
      }
    }
  }



  /**
   * TODO:  Hook up playing audio functionality here.
   */
  private void playAudio() {
  }


  /**
   * TODO:  What connections do we have to the Findaway player, to be able to implement media controls
   * (play/pause/rewind X seconds, ff X seconds, seek bar, playback speed, etc.)?
   *
   */
  private void scheduleSeekbarUpdate() {
  }

  /* ------------------------------------ /PLAYBACK EVENT HANDLERS ------------------------------------- */


  /* ------------------------------------ UI METHODS ------------------------------------- */

    private void setProgress(DownloadEvent downloadEvent) {
      if (playBookFragment != null) {
        playBookFragment.redrawProgress(downloadEvent);
      }
    }

    private void resetProgress() {
      if (playBookFragment != null) {
        playBookFragment.resetProgress();
      }
    }


  /* ------------------------------------ /UI METHODS ------------------------------------- */


  /* ------------------------------------ UTILITY METHODS ------------------------------------- */

  /**
   * TODO: Do I need to manage app permissions manually, or will the Findaway sdk do that for me?
   * If it does it for me, where, what permissions, what reactions do I need to handle in the calling code?
   */
  private boolean checkAppPermissions() {
    return true;
  }


  /**
   * TODO:  How to hook up my UI buttons to Findaway MediaController?
   *
   * Wrapper around getSupportMediaController(), so we can call it from a child fragment.
   * @return  MediaControllerCompat that provides media control onscreen buttons.
   */
  public MediaControllerCompat getController() {
    return getSupportMediaController();
  }


  /* ------------------------------------ /UTILITY METHODS ------------------------------------- */


}
