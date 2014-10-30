package com.app4am.app4am;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        SwipeRefreshFragmentInterface {

    public static final String APPLICATION_VND_ANDROID_PACKAGE_ARCHIVE = "application/vnd.android.package-archive";
    //    public final String TAG = MainActivity.class.getSimpleName();
    public static final String DIALOG = "dialog";
    private static final String BUNDLE_KEY_MAIN_ACTIVITY_IS_PROMPT = "bundle_key_main_activity_isPrompt";
    private static final String BUNDLE_KEY_MAIN_ACTIVITY_DOWNLOAD_REFERENCE = "bundle_key_main_activity_downloadReference";

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;


    /**
     * Main pager adapter for both 1st & 2nd page.
     */
    private MainPagerAdapter mMainPagerAdapter;

    /**
     * View pager for swipe switching.
     */
    private ViewPager mViewPager;


    /**
     * Receive new version information.
     */
    private CheckUpdateReceiver mCheckUpdateReceiver = new CheckUpdateReceiver();

    /**
     * Prompt for startCrawler.
     */
    private boolean isPrompt = false;

    /**
     * Reference id for downloading package.
     */
    private long downloadReference = 0;


    /**
     * Temporal workaround crawler.
     */
    private CrawlerFragment mCrawlerFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCrawlerFragment = (CrawlerFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);

        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        mViewPager = (ViewPager) findViewById(R.id.pager);

        mMainPagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mMainPagerAdapter);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        /**
         * Load the main news list.
         */
//        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        CrawlerFragment fragment = CrawlerFragment.newInstance("", "");
//        transaction.replace(R.id.container2, fragment);
//        transaction.commit();

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mCheckUpdateReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(CheckUpdateReceiver.ACTION_UPDATE_AVAILABLE);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(mCheckUpdateReceiver, intentFilter);
        intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(mCheckUpdateReceiver, intentFilter);

        if (!isPrompt) {
            /**
             * Check for startCrawler.
             * TODO: Retrieving versionCode/versionName.
             */
            CheckUpdateIntentService.startActionCheckUpdate(this, "1", "0.1.alpha");
            isPrompt = true;
        }

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
        isPrompt = savedInstanceState.getBoolean(BUNDLE_KEY_MAIN_ACTIVITY_IS_PROMPT);
        downloadReference = savedInstanceState.getLong(BUNDLE_KEY_MAIN_ACTIVITY_DOWNLOAD_REFERENCE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(BUNDLE_KEY_MAIN_ACTIVITY_IS_PROMPT, isPrompt);
        outState.putLong(BUNDLE_KEY_MAIN_ACTIVITY_DOWNLOAD_REFERENCE, downloadReference);
    }


    // Since this is an object collection, use a FragmentStatePagerAdapter,
    // and NOT a FragmentPagerAdapter.
    public static class MainPagerAdapter extends FragmentStatePagerAdapter {

        private final Class[] FRAGMENT_TYPE_ARRAY =
                new Class[]{
                        MainTopicListFragment.class,
                        LatestNewsListFragment.class
                };

        private int mPosition = -1;

        public MainPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int index) {
            Class clazz = FRAGMENT_TYPE_ARRAY[index];
            Class fragmentClass = clazz.asSubclass(SwipeRefreshListFragment.class);

            SwipeRefreshListFragment fragment = null;
            try {
                fragment = (SwipeRefreshListFragment) fragmentClass.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            if (fragment != null) {
                Bundle args = new Bundle();
                args.putInt(SwipeRefreshFragmentInterface.FRAGMENT_POSITION, index);
                fragment.setArguments(args);
            }
            return fragment;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            if (mPosition != position) {
                mPosition = position;
                Log.d(MainPagerAdapter.class.getSimpleName(),
                        "MainPagerAdapter#setPrimaryItem invoked. (" + Integer.toString(position) + ")");
            }
        }

        @Override
        public int getCount() {
            return FRAGMENT_TYPE_ARRAY.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "OBJECT " + (position + 1);
        }
    }

    @Override
    public void onRefreshRequest(SwipeRefreshListFragment fragment) {
        mCrawlerFragment.stopCrawler();
        mCrawlerFragment.startCrawler(new CrawlerFragment.CrawlerTaskBase(fragment));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // startCrawler the main content by replacing fragments
        // TODO: side menu function
        Toast.makeText(this, "Option: " + position + " selected.", Toast.LENGTH_SHORT).show();
        if (mViewPager != null) {
            mCrawlerFragment.stopCrawler();
            mViewPager.setCurrentItem(position, true);
        }
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
//        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    public class CheckUpdateReceiver extends BroadcastReceiver {

        public static final String ACTION_UPDATE_AVAILABLE = "com.app4am.app4am.action.UPDATE_AVAILABLE";


        public CheckUpdateReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            // This method is called when the BroadcastReceiver is receiving
            // an Intent broadcast.
            checkUpdate(intent);
        }
    }


    /**
     * Process the startCrawler message from the intent service.
     *
     * @param intent The messages from the service.
     */
    private void checkUpdate(Intent intent) {
        if (intent.getAction().equals(CheckUpdateReceiver.ACTION_UPDATE_AVAILABLE)) {
            /**
             *  Update available.
             *  Prompt the user for the latest version.
             */
            int versionCode = intent.getIntExtra(CheckUpdateIntentService.BUNDLE_KEY_PACKAGE_VERSION_CODE, -1);
            String versionName = intent.getStringExtra(CheckUpdateIntentService.BUNDLE_KEY_PACKAGE_VERSION_NAME);
            String packageDownloadUri = "http://192.168.1.106/~dino/app-release.apk";

            DialogFragment dialogFragment = UpdateDialogFragment.newInstance(versionCode, versionName, packageDownloadUri);
            dialogFragment.show(getSupportFragmentManager(), DIALOG);

        } else if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
            //check if the broadcast message is for our enqueue download
            long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (downloadReference == referenceId) {
                Uri uriForDownloadedFile = getDownloadPackageUri(referenceId);


                //start the installation of the latest version
                Intent installIntent = new Intent(Intent.ACTION_VIEW);
                installIntent.setDataAndType(uriForDownloadedFile, APPLICATION_VND_ANDROID_PACKAGE_ARCHIVE);
                installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(installIntent);
            }
        }

    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private Uri getDownloadPackageUri(long referenceId) {
        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return downloadManager.getUriForDownloadedFile(referenceId);
        } else {
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(referenceId);
            Cursor cursor = downloadManager.query(query);
            return Uri.parse(cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)));
        }
    }

    private void downloadUpdate(String packageDownloadUriString) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            /**
             * Start downloading the file using the download manager.
             */
            DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            Uri packageDownloadUri = Uri.parse(packageDownloadUriString);
            DownloadManager.Request request = new DownloadManager.Request(packageDownloadUri);
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
            request.setAllowedOverRoaming(false);
            // TODO: package name
            request.setTitle("My Android App Download");
            request.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, "app-release.apk");
            downloadReference = downloadManager.enqueue(request);
        } else {
            // TODO: Redirect to the project web page.

        }
    }

    /**
     * The type Update dialog fragment.
     */
    public static class UpdateDialogFragment extends DialogFragment {

        public static UpdateDialogFragment newInstance(int versionCode, String versionName, String packageDownloadUriString) {
            UpdateDialogFragment updateDialogFragment = new UpdateDialogFragment();
            Bundle args = new Bundle();
            args.putInt(CheckUpdateIntentService.BUNDLE_KEY_PACKAGE_VERSION_CODE, versionCode);
            args.putString(CheckUpdateIntentService.BUNDLE_KEY_PACKAGE_VERSION_NAME, versionName);
            args.putString(CheckUpdateIntentService.BUNDLE_KEY_PACKAGE_DOWNLOAD_URI, packageDownloadUriString);
            updateDialogFragment.setArguments(args);
            return updateDialogFragment;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Bundle args = getArguments();

            /**
             * Create hint with version information.
             */
            int versionCode = args.getInt(CheckUpdateIntentService.BUNDLE_KEY_PACKAGE_VERSION_CODE);
            String versionName = args.getString(CheckUpdateIntentService.BUNDLE_KEY_PACKAGE_VERSION_NAME);

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(getString(R.string.version_update_hint1));
            stringBuilder.append(versionName);
            if (versionCode != -1) {
                stringBuilder.append('(');
                stringBuilder.append(Integer.toString(versionCode));
                stringBuilder.append(')');
            }
            stringBuilder.append(getString(R.string.version_update_hint2));
            String updateMessage = stringBuilder.toString();

            /**
             * Build the alert dialog.
             */
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(updateMessage)
                    .setPositiveButton(getString(R.string.confirm_update), new DialogInterface.OnClickListener() {
                        //if the user agrees to upgrade
                        public void onClick(DialogInterface dialog, int id) {
                            /**
                             * Start downloading the file using the download manager.
                             */
                            Bundle args = getArguments();
                            String packageDownloadUriString = args.getString(CheckUpdateIntentService.BUNDLE_KEY_PACKAGE_DOWNLOAD_URI);
                            ((MainActivity) getActivity()).downloadUpdate(packageDownloadUriString);
                        }
                    })
                    .setNegativeButton(getString(R.string.remind_me_later), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
            //show the alert message
            return builder.create();
        }
    }


}
