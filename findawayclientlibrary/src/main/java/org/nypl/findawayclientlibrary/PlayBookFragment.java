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
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

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

  private ImageView mSkipPrev;
  private ImageView mSkipNext;
  private ImageView mPlayPause;
  private TextView mStart;
  private TextView mEnd;

  private SeekBar mSeekbar;

  // non-user-interactive, usually used to show download progress
  //private ProgressBar mLoading;
  private View mControllers;
  private Drawable mPauseDrawable;
  private Drawable mPlayDrawable;
  private ImageView mBackgroundImage;
  private String mCurrentArtUrl;


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

    initializeControlsUI(view);

    initializeTOC(view);
  }


  /**
   * TODO: Hooking up media controls to UI play/pause/etc. buttons goes here.
   *
   * @param view
   */
  private void initializeControlsUI(View view) {
    //mBackgroundImage = (ImageView) view.findViewById(R.id.background_image);
    mPauseDrawable = ContextCompat.getDrawable(getActivity(), R.drawable.ic_pause_circle_filled_black_24dp);
    mPlayDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_play_circle_outline_black_24dp);
    mControllers = view.findViewById(R.id.bottom_nav_bar_media_controls);

    mPlayPause.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        PlaybackStateCompat state = callbackActivity.getController().getPlaybackState();
        if (state != null) {
          MediaControllerCompat.TransportControls controls =
                  callbackActivity.getController().getTransportControls();
          switch (state.getState()) {
            case PlaybackStateCompat.STATE_PLAYING: // fall through
            case PlaybackStateCompat.STATE_BUFFERING:
              controls.pause();
              break;
            case PlaybackStateCompat.STATE_PAUSED:
            case PlaybackStateCompat.STATE_STOPPED:
              controls.play();
              break;
            default:
              //LogHelper.d(TAG, "onClick with state ", state.getState());
          }
        }
      }
    });

    mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mStart.setText(DateUtils.formatElapsedTime(progress / 1000));
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
        // stopSeekbarUpdate();
      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        callbackActivity.getController().getTransportControls().seekTo(seekBar.getProgress());
        // scheduleSeekbarUpdate();
      }
    });

  }// initializeControlsUI


  /**
   * TODO:  Reading returned metadata and hooking up book info to UI goes here.
   *
   * @param view
   */
  private void initializeTOC(View view) {

  }


  /* ---------------------------------- /LIFECYCLE METHODS ----------------------------------- */

  /* ------------------------------------ NAVIGATION EVENT HANDLERS ------------------------------------- */

  /* ------------------------------------ /NAVIGATION EVENT HANDLERS ------------------------------------- */



  /* ------------------------------------ PLAYBACK EVENT HANDLERS ------------------------------------- */


  /* ------------------------------------ /PLAYBACK EVENT HANDLERS ------------------------------------- */

}
