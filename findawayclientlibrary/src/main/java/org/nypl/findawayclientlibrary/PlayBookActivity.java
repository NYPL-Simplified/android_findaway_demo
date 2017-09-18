package org.nypl.findawayclientlibrary;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.media.session.MediaControllerCompat;
import android.util.Log;

import io.audioengine.config.LogLevel;
import io.audioengine.mobile.AudioEngine;

import io.audioengine.exceptions.AudioEngineException;

import io.audioengine.mobile.persistence.DownloadEngine;
import io.audioengine.mobile.persistence.DownloadRequest;
import io.audioengine.mobile.persistence.DownloadType;

// TODO: what package should I include in gradle to get the ContentEngine?
import io.audioengine.mobile.sample.api.ContentEngine;
import io.audioengine.mobile.sample.api.model.Content;

import org.nypl.findawayclientlibrary.PlayBookFragment;

import java.util.List;


/**
 * Loads and exchanges fragments that are responsible for audio player UI
 * and audiobook-associated sub-screens.
 *
 * Communicates with the download portion of the sdk.
 *
 * Communicates with the audio playing portion of the sdk.  Acts as an intermediary between
 * the background player service and the front-end ui fragment.
 *
 * Created by daryachernikhova on 9/14/17.
 */
public class PlayBookActivity extends BaseActivity {
  // so can filter all log msgs belonging to my app
  private static final String APP_TAG = "FDLIB.";
  // so can do a search in log msgs for just this class's output
  private static final String TAG = APP_TAG + "PlayBookActivity";

  // corresponds to book resources path on the file system
  String bookId = null;

  PlayBookFragment playBookFragment = null;

  DownloadEngine mDownloadEngine;



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
            bookId = extras.getString("audiobook_id", null);
        }
      }

      if (bookId == null) {
        // handle me
      }

      Log.d(TAG, "bookId=" + bookId);
    }

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
    String sessionId = "139bbcd9-6426-474d-9b02-ed3fc438be66";
    AudioEngine.init(this, sessionId, LogLevel.VERBOSE);
  }


  /**
   * TODO: DownloadEngine needs the following setup: [...].
   */
  private void initDownloadEngine() {
    try {
      mDownloadEngine = AudioEngine.getDownloadEngine();
    } catch (AudioEngineException e) {
      // Call to getDownloadEngine will throw an exception if you have not previously
      // called init() on AudioEngine with a valid Context and Session.
    }
  }


  /**
   * TODO:  Hook up downloading audio functionality here.
   */
  private void downloadAudio() {
    this.initAudioEngine();

    this.initDownloadEngine();

    Content mContent;
    String sessionId, accountId, consumerId, mLicense;
    String mContentId;

    // darya: hardcoding mContentId to the fulfillmentId of the book NYPL bought through Bibliotheca
    mContentId = "102244";
    // darya: hardcoding mLicense to licenseId of the book NYPL bought through Bibliotheca
    mLicense = "57db1411afde9f3e7a3c041b";

    // To start downloading the audio files for the chapter supplied: A valid chapter is a combination of content (book) id, part number, and chapter number.
    // TODO: how to get the definitive list of chapters?  what does "part" mean?  what if I just want to download the entire book?
    DownloadRequest.Builder requestBuilder = DownloadRequest.builder().contentId(mContentId).licenseId(mLicense).part(mContent.getFirstChapter().partNumber)
            .chapter(mContent.getFirstChapter().chapterNumber);

    requestBuilder.type(DownloadType.SINGLE);

    // Audio files are downloaded and stored under the application's standard internal files directory. This directory is deleted when the application is removed.
    DownloadRequest request = requestBuilder.build();

    mDownloadEngine.download(request);


    /* Extra Things I Could Do:

    // To get a DownloadRequest for a specific download request id:
    // TODO:  What does this allow me to do?  What's the downloadRequest for?
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

  /* ------------------------------------ /PLAYBACK EVENT HANDLERS ------------------------------------- * /


  /* ------------------------------------ UTILITY METHIDS ------------------------------------- * /

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
