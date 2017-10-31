package org.nypl.findawayclientlibrary;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import io.audioengine.mobile.DownloadEvent;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentManager;
//import android.support.v4.app.FragmentTransaction;


/**
 * Loads the UI with media controls, audiobook metadata s.a. title, author, cover image,
 * and on-screen playback settings.
 *
 * Responds to user click events that affect audio playback, but asks the calling activity
 * to communicate with the audio player service.
 *
 * Created by daryachernikhova on 7/21/17.
 */
public class PlayBookFragment extends BaseFragment {
  // so can filter all log msgs belonging to my app
  private final String APP_TAG = "FDLIB.";

  // so can do a search in log msgs for just this class's output
  private final String TAG = APP_TAG + "PlayBookFragment";

  private Button downloadButton;
  private ProgressBar downloadProgress;
  private TextView chapterPercentage, contentPercentage;

  private View fragmentView = null;

  //private SeekBar mSeekbar;

  // non-user-interactive, usually used to show download progress
  //private ProgressBar mLoading;
  //private Drawable mPauseDrawable;
  //private Drawable mPlayDrawable;
  //private ImageView mBackgroundImage;
  //private String mCurrentArtUrl;


  /* ---------------------------------- LIFECYCLE METHODS ----------------------------------- */
  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    // do not call super, as it returns a null, prevents onViewCreated from auto-running, and prevents getView() from working later in the code.
    //return super.onCreateView(inflater, container, savedInstanceState);

    return inflater.inflate(R.layout.fragment_play_book, container, false);
  }


  /**
   * Initializes the view, adding onClick events to the view child elements.
   *
   * Called immediately after onCreateView() has returned, but before any saved state has been restored in to the view.
   * View hierarchy has been completely created, but not attached to its parent at this point.
   * NOTE: Will not called automatically if you are returning null or super.onCreateView() from onCreateView().
   * NOTE: Can get the fragment view anywhere in the class by using getView() once onCreateView() has been executed successfully.
   *
   * @param view
   * @param savedInstanceState
   */
  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    // save the view handle, for future convenience
    fragmentView = view;

    initializeControlsUI(view);
  }


  /**
   * Hooking up media controls to UI play/pause/etc. buttons goes here.
   *
   * @param view
   */
  private void initializeControlsUI(View view) {
    // set up the UI elements that will give download info
    downloadButton = (Button) fragmentView.findViewById(R.id.download_button);
    downloadProgress = (ProgressBar) fragmentView.findViewById(R.id.download_progress);
    chapterPercentage = (TextView) fragmentView.findViewById(R.id.chapterPercentage);
    contentPercentage = (TextView) fragmentView.findViewById(R.id.contentPercentage);

    downloadButton.setOnClickListener((View.OnClickListener) callbackActivity);
    downloadButton.setOnLongClickListener((View.OnLongClickListener) callbackActivity);


  }// initializeControlsUI



  /**
   * Change the message on the download button, letting the user know where we are in the downloading progress.
   */
  public void redrawDownloadButton(String newText) {
    downloadButton.setText(newText);
  }


  /**
   * Update the progress bar to reflect where we are in the downloading.
   */
  public void redrawProgress(DownloadEvent downloadEvent) {
    this.redrawProgress(downloadEvent.contentPercentage, downloadEvent.chapterPercentage);
  }


  public void redrawProgress(Integer primaryProgress, Integer secondaryProgress) {
    downloadProgress.setProgress(primaryProgress);
    downloadProgress.setSecondaryProgress(secondaryProgress);
    contentPercentage.setText(getString(R.string.contentPercentage, primaryProgress));
    chapterPercentage.setText(getString(R.string.chapterPercentage, secondaryProgress));
  }


  public void resetProgress() {
    this.redrawProgress(0, 0);
  }

  /* ---------------------------------- /LIFECYCLE METHODS ----------------------------------- */

  /* ------------------------------------ NAVIGATION EVENT HANDLERS ------------------------------------- */

  /* ------------------------------------ /NAVIGATION EVENT HANDLERS ------------------------------------- */



  /* ------------------------------------ PLAYBACK EVENT HANDLERS ------------------------------------- */


  /* ------------------------------------ /PLAYBACK EVENT HANDLERS ------------------------------------- */

}
