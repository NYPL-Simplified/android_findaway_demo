package org.nypl.findawaysdkdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import org.nypl.findawayclientlibrary.BaseFragment;


/**
 * Handles the UI functionality of displaying the audiobook playback screen.
 *
 * Created by daryachernikhova on 9/14/17.
 */
public final class SelectBookFragment extends BaseFragment {
  // so can filter all log msgs belonging to my app
  private final String APP_TAG = "ABLD.";
  // so can do a search in log msgs for just this class's output
  private final String TAG = APP_TAG + "SelectBookFragment";


  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    // do not call super, as it returns a null, prevents onViewCreated from auto-running, and prevents getView() from working later in the code.
    //return super.onCreateView(inflater, container, savedInstanceState);

    return inflater.inflate(R.layout.fragment_select_book, container, false);
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

    // Whichever example book's checkbox is clicked, pass that book into the new book-playing view we're going to open.
    final CheckBox checkBox1 = (CheckBox) view.findViewById(R.id.select_book_1);
    final CheckBox checkBox2 = (CheckBox) view.findViewById(R.id.select_book_2);

    View.OnClickListener onClickListener = new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Log.d(TAG, "onClick");

        if (v.getId() == R.id.select_book_1) {
          // clicking this checkbox checks it
          checkBox1.setChecked(true);

          // uncheck the other checkbox
          checkBox2.setChecked(false);

          callbackActivity.onBookSelected("21_gun_salute");
        }
        if (v.getId() == R.id.select_book_2) {
          // clicking this checkbox checks it
          checkBox2.setChecked(true);

          // uncheck the other checkbox
          checkBox1.setChecked(false);

          callbackActivity.onBookSelected("the_other_book");
        }
      }
    };

    checkBox1.setOnClickListener(onClickListener);
    checkBox2.setOnClickListener(onClickListener);
  }


}
