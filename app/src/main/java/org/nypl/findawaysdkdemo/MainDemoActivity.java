package org.nypl.findawaysdkdemo;

import android.content.Intent;
import android.os.Bundle;

import org.nypl.findawayclientlibrary.BaseActivity;
import org.nypl.findawayclientlibrary.PlayBookActivity;


public class MainDemoActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // change the title displayed in the top bar from the app name (default) to something indicative
        getSupportActionBar().setTitle("Select A Book");

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create a new Fragment that offers the user a selection of books to be played.
            SelectBookFragment selectBookFragment = new SelectBookFragment();

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            selectBookFragment.setArguments(getIntent().getExtras());

            // Add the first fragment to the 'fragment_container' view component.
            // NOTE:  Because the fragment has been added to the FrameLayout container at runtime
            // instead of defining it in the activity's layout with a <fragment> element
            // the activity can remove the fragment and replace it with a different one later on in the code.
            this.loadFragment(selectBookFragment, R.id.fragment_container, false);

        } else {
            // layout doesn't allow fragment loading
            throw new IllegalStateException("Missing layout container with id 'fragment_container'. Cannot continue.");
        }
    }


    /**
     * Calls the activity to play an audiobook, and passes it the id of the
     * audiobook to play.
     *
     * @param demoBookId
     */
    public void onBookSelected(String demoBookId) {
        Intent intent = new Intent(this, PlayBookActivity.class);
        intent.putExtra("audiobook_id", demoBookId);
        startActivity(intent);
    }

}
