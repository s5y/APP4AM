package com.app4am.app4am;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.MenuItem;


public class TopicIntroductionActivity extends ActionBarActivity
        implements TopicIntroductionFragment.OnFragmentInteractionListener {

    private static final String BUNDLE_KEY_TOPIC_INTRODUCTION_ACTIVITY_IS_FRESH_LOAD = "bundle_key_topic_introduction_fragment_isFreshLoad";
    private static final String FRAG_TAG = "TOPIC_INTRODUCTION_FRAGMENT_TAG";
    private boolean isFreshLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_introduction);
    }

    /**
     * This method is called after {@link #onStart} when the activity is
     * being re-initialized from a previously saved state, given here in
     * <var>savedInstanceState</var>.  Most implementations will simply use {@link #onCreate}
     * to restore their state, but it is sometimes convenient to do it here
     * after all of the initialization has been done or to allow subclasses to
     * decide whether to use your default implementation.  The default
     * implementation of this method performs a restore of any view state that
     * had previously been frozen by {@link #onSaveInstanceState}.
     * <p/>
     * <p>This method is called between {@link #onStart} and
     * {@link #onPostCreate}.
     *
     * @param savedInstanceState the data most recently supplied in {@link #onSaveInstanceState}.
     * @see #onCreate
     * @see #onPostCreate
     * @see #onResume
     * @see #onSaveInstanceState
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        isFreshLoad = savedInstanceState.getBoolean(BUNDLE_KEY_TOPIC_INTRODUCTION_ACTIVITY_IS_FRESH_LOAD);
    }

    @Override
    protected void onResume() {
        super.onResume();

        /**
         * If the fragment stack were not empty,
         * the slide out animation of the TopicIntroductionFragment would not work properly
         * after an orientation change.
         */
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        Fragment fragment = supportFragmentManager.findFragmentByTag(FRAG_TAG);
        if (fragment != null) {
            supportFragmentManager.popBackStackImmediate();
        }

        // Add topic introduction fragment.
        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        if (isFreshLoad) {
            // A slide in animation should be performed.
            fragmentTransaction.setCustomAnimations(R.anim.topic_introduction_slide_in_bottom,
                    0,
                    0,
                    R.anim.topic_introduction_slide_out_bottom);
            isFreshLoad = false;
        } else {
            // Skip the slide in animation.
            fragmentTransaction.setCustomAnimations(0,
                    0,
                    0,
                    R.anim.topic_introduction_slide_out_bottom);
        }
        fragmentTransaction.replace(R.id.topic_introduction_container, TopicIntroductionFragment.newInstance("", ""), FRAG_TAG);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(BUNDLE_KEY_TOPIC_INTRODUCTION_ACTIVITY_IS_FRESH_LOAD, isFreshLoad);
    }


    @Override
    public void onBackPressed() {
        // Delay execution of the super.onBackPressed() after transition animation completed.
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        supportFragmentManager.popBackStack();
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.topic_introduction, menu);
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when fragment exited.
     */
    @Override
    public void onFragmentExit() {
        super.onBackPressed();
    }


}
