/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.app4am.app4am;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.Toast;


import com.app4am.app4am.test.Cheeses;

import java.util.List;

/**
 * A sample which shows how to use {@link android.support.v4.widget.SwipeRefreshLayout} within a
 * {@link android.support.v4.app.ListFragment} to add the 'swipe-to-refresh' gesture to a
 * {@link android.widget.ListView}. This is provided through the provided re-usable
 * {@link SwipeRefreshListFragment} class.
 * <p/>
 * <p>To provide an accessible way to trigger the refresh, this app also provides a refresh
 * action item. This item should be displayed in the Action Bar's overflow item.
 * <p/>
 * <p>In this sample app, the refresh updates the ListView with a random set of new items.
 * <p/>
 * <p>This sample also provides the functionality to change the colors displayed in the
 * {@link android.support.v4.widget.SwipeRefreshLayout} through the options menu. This is meant to
 * showcase the use of color rather than being something that should be integrated into apps.
 */
public class MainTopicListFragment extends SwipeRefreshListFragment {

    private static final String LOG_TAG = MainTopicListFragment.class.getSimpleName();

    private static final int LIST_ITEM_COUNT = 20;
    /**
     * Respond to the user's selection of the Refresh action item. Start the SwipeRefreshLayout
     * progress bar, then initiate the background task that refreshes the content.
     * <p/>
     * <p>A color scheme menu item used for demonstrating the use of SwipeRefreshLayout's color
     * scheme functionality. This kind of menu item should not be incorporated into your app,
     * it just to demonstrate the use of color. Instead you should choose a color scheme based
     * off of your application's branding.
     */
    boolean checkedState = false;
    private View mHeaderView = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Notify the system to allow an options menu for this fragment.
        setHasOptionsMenu(true);
    }

    // BEGIN_INCLUDE (setup_refresh_menu_listener)

    // BEGIN_INCLUDE (setup_views)
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Change the colors displayed by the SwipeRefreshLayout by providing it with 4
        // color resource ids
        setColorScheme(R.color.color_scheme_1_1, R.color.color_scheme_1_2,
                R.color.color_scheme_1_3, R.color.color_scheme_1_4);

        // Attach topic banner.
        getListView().addHeaderView(mHeaderView);

        getListView().setDividerHeight((int) getResources().getDimension(R.dimen.topic_list_divider_height));
        getListView().setCacheColorHint(0);
//        getListView().setDrawingCacheBackgroundColor(getResources().getColor(android.R.color.transparent));
        /**
         * Create an ArrayAdapter to contain the data for the ListView. Each item in the ListView
         * uses the system-defined simple_list_item_1 layout that contains one TextView.
         */
        ListAdapter adapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.topic_list_item,
                R.id.tv_topic_title,
                Cheeses.randomList(LIST_ITEM_COUNT));

        // Set the adapter between the ListView and its backing data.
        setListAdapter(adapter);

        // BEGIN_INCLUDE (setup_refreshlistener)
        /**
         * Implement {@link android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener}. When users do the "swipe to
         * refresh" gesture, SwipeRefreshLayout invokes
         * {@link android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener#onRefresh onRefresh()}. In
         * {@link android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener#onRefresh onRefresh()}, call a method that
         * refreshes the content. Call the same method in response to the Refresh action from the
         * action bar.
         */
        setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(LOG_TAG, "onRefresh called from SwipeRefreshLayout");

                initiateRefresh();
            }
        });
        // END_INCLUDE (setup_refreshlistener)
    }
    // END_INCLUDE (setup_views)

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
//        inflater.inflate(R.menu.main_menu, menu);
    }
    // END_INCLUDE (setup_refresh_menu_listener)

    // BEGIN_INCLUDE (initiate_refresh)

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_search:
                Toast.makeText(getActivity(), "frag action_search", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_catalog:
                item.setChecked(true);
                checkedState = !checkedState;
                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
                Toast.makeText(getActivity(), "frag action_catalog", Toast.LENGTH_SHORT).show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
    // END_INCLUDE (initiate_refresh)

    // BEGIN_INCLUDE (refresh_complete)

    /**
     * By abstracting the refresh process to a single method, the app allows both the
     * SwipeGestureLayout onRefresh() method and the Refresh action item to refresh the content.
     */
    private void initiateRefresh() {
        Log.i(LOG_TAG, "initiateRefresh");

        /**
         * Execute the background task, which uses {@link android.os.AsyncTask} to load the data.
         */
        new DummyBackgroundTask().execute();
    }

    /**
     * When the AsyncTask finishes, it calls onRefreshComplete(), which updates the data in the
     * ListAdapter and turns off the progress bar.
     */
    private void onRefreshComplete(List<String> result) {
        Log.i(LOG_TAG, "onRefreshComplete");

        // Remove all items from the ListAdapter, and then replace them with the new items
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) getListAdapter();
        adapter.clear();
        for (String cheese : result) {
            adapter.add(cheese);
        }

        // Stop the refreshing indicator
        setRefreshing(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mHeaderView = inflater.inflate(R.layout.topic_list_header, null);
        return view;
    }
    // END_INCLUDE (refresh_complete)

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.action_catalog);
        if (item != null) {
            boolean checked = item.isChecked();
            Log.d("checked", "state: " + checked + ", internal checked: " + checkedState);
            item.setChecked(checkedState);
        }
    }

    /**
     * Dummy {@link android.os.AsyncTask} which simulates a long running task to fetch new cheeses.
     */
    private class DummyBackgroundTask extends AsyncTask<Void, Void, List<String>> {

        static final int TASK_DURATION = 3 * 1000; // 3 seconds

        @Override
        protected List<String> doInBackground(Void... params) {
            // Sleep for a small amount of time to simulate a background-task
            try {
                Thread.sleep(TASK_DURATION);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Return a new random list of cheeses
            return Cheeses.randomList(LIST_ITEM_COUNT);
        }

        @Override
        protected void onPostExecute(List<String> result) {
            super.onPostExecute(result);

            // Tell the Fragment that the refresh has completed
            onRefreshComplete(result);
        }

    }

}
