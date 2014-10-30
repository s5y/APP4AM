package com.app4am.app4am;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;


/**
 * A simple {@link Fragment} subclass.
 */
public class CrawlerFragment extends Fragment {
    private static final String LOG_TAG = CrawlerFragment.class.getSimpleName();

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String BUNDLE_KEY_CRAWLER_FRAGMENT_IS_CRAWLER_RUNNING = "bundle_key_crawler_fragment_isCrawlerRunning";


    private boolean isCrawlerRunning = false;

    private Crawler mCrawler;


    public CrawlerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_crawler, container, false);
        WebView webView = (WebView) view.findViewById(R.id.webView);
        mCrawler = new Crawler(webView);
        if (savedInstanceState != null) {
            isCrawlerRunning = savedInstanceState.getBoolean(BUNDLE_KEY_CRAWLER_FRAGMENT_IS_CRAWLER_RUNNING);
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(BUNDLE_KEY_CRAWLER_FRAGMENT_IS_CRAWLER_RUNNING, isCrawlerRunning);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isCrawlerRunning) {
            mCrawler.startLoading(getString(R.string.uri_front_page));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isCrawlerRunning) {
            Log.d(LOG_TAG, "CrawlerFragment#onPause() invoked.");
            mCrawler.stopLoading();
        }
    }

    public void onParsingFinished(String uri) {
        mTask.taskFinishedOrCancelled();
        mTask = null;
        isCrawlerRunning = false;
    }

    private CrawlerTaskBase mTask = null;

    public static class CrawlerTaskBase {
        private final SwipeRefreshListFragment mSwipeRefreshListFragment;

        public CrawlerTaskBase(SwipeRefreshListFragment swipeRefreshListFragment) {
            this.mSwipeRefreshListFragment = swipeRefreshListFragment;
        }

        public void taskFinishedOrCancelled() {
            mSwipeRefreshListFragment.setRefreshing(false);
        }
    }

    public void startCrawler(CrawlerTaskBase crawlerTaskBase) {
        if (isCrawlerRunning == false) {
            isCrawlerRunning = true;
            mTask = crawlerTaskBase;
            mCrawler.startLoading(getString(R.string.uri_front_page));
        }
    }

    public void stopCrawler() {
        if (isCrawlerRunning) {
            mCrawler.stopLoading();
        }
    }

    /**
     * Created by dino on 2014/10/22.
     */
    public class Crawler {
        private static final String JAVASCRIPT_OBJECT_ANDROID = "Android";

        private static final String mScriptParse = "javascript:\n" +
                "        var home = $(\"#home\");\n" +
                "        var rowLogo = home.find(\".row.logo\").find(\"img\").attr(\"src\");\n" +
                "        var rowPicture = home.find(\".row.picture\").find(\"img\").attr(\"src\");\n" +
                "        window.Android.setRecordInfo(rowLogo, rowPicture);\n" +
                "        var newsList = home.find(\".bg-warning.eachNews\");\n" +
                "        newsList.each(function(idx, val) {\n" +
                "              var v = $(val);\n" +
                "              var newsKey=v.attr(\"data-news\");\n" +
                "              var headLine=v.find(\"h3\").html();\n" +
                "              window.Android.setRecordData(idx, newsKey, headLine);\n" +
                "        });\n" +
                "        delete newsList;\n" +
                "        delete rowPicture;\n" +
                "        delete rowLogo;\n" +
                "        delete home;\n";

        private static final String mScriptCheck = "javascript:\n" +
                "        window.Android.setRecordCount($(\"#home\").find(\".bg-warning.eachNews\").length);";
        private static final long DELAY_PARSE_TIME = 1000;


        private WebView mWebView;

        private Handler mHandler;

        private boolean isParsing = false;

        private int mRecordCount = 0;

        private int mParsedRecordCount;

        /**
         * Called when the activity is first created.
         */
        public Crawler(WebView webView) {
            mWebView = webView;

            mHandler = new Handler(mWebView.getContext().getMainLooper());

            mWebView.setWebChromeClient(new WebChromeClient() {
                /**
                 * Tell the host application the current progress of loading a page.
                 *
                 * @param view        The WebView that initiated the callback.
                 * @param newProgress Current page loading progress, represented by
                 */
                @Override
                public void onProgressChanged(WebView view, int newProgress) {
                    super.onProgressChanged(view, newProgress);
                    if (newProgress >= 100) {
                        /**
                         * Instead of using {@link android.webkit.WebViewClient#onPageFinished},
                         * detect the page state with the loading progress.
                         */
                        triggerParseAction();
                    }
                }
            });
            mWebView.getSettings().setJavaScriptEnabled(true);

            mWebView.addJavascriptInterface(new JavaScriptHandler(), JAVASCRIPT_OBJECT_ANDROID);
        }

        public void startLoading(String uri) {
            mWebView.loadUrl(uri);
        }

        public void stopLoading() {
            mWebView.stopLoading();
            isParsing = false;
            onParsingFinished(mWebView.getUrl().toString());
        }


        private void triggerParseAction() {
            if (!isParsing) {
                Log.d(LOG_TAG, "Start parsing.");
                isParsing = true;
                mRecordCount = 0;
                checkPageState();
            }
        }

        private void checkPageState() {
            mHandler.postDelayed(new Runnable() {

                /**
                 * Starts executing the active part of the class' code. This method is
                 * called when a thread is started that has been created with a class which
                 * implements {@code Runnable}.
                 */
                @Override
                public void run() {
                    mWebView.loadUrl(mScriptCheck);
                }

            }, DELAY_PARSE_TIME);
        }

        private void javascriptRecordCount(int count) {
            if (count == 0) {
                checkPageState();
                return;
            }

            if (mRecordCount == 0) {
                /**
                 * 1st detect news data
                 */
                mRecordCount = count;
                checkPageState();
                return;
            }

            if (mRecordCount != count) {
                /**
                 * Unstable.
                 */
                mRecordCount = count;

                checkPageState();
                return;
            }
            Log.d(LOG_TAG, "All news records were loaded. (total: " + Integer.toString(mRecordCount) + ")");

            /**
             * Stable now, start parsing.
             */
            mHandler.postDelayed(new Runnable() {
                /**
                 * Starts executing the active part of the class' code. This method is
                 * called when a thread is started that has been created with a class which
                 * implements {@code Runnable}.
                 */
                @Override
                public void run() {
                    mParsedRecordCount = 0;
                    mWebView.loadUrl(mScriptParse);

                }
            }, DELAY_PARSE_TIME);
        }

        private void javascriptRecordInfo(final String logoUri, final String pictureUri) {
            Log.d(LOG_TAG, logoUri + " - " + pictureUri);
        }

        private void javascriptRecordData(final int index, final String key, final String headline) {
            Log.d(LOG_TAG, Integer.toString(index) + ":" + key + ":" + headline);
            ++mParsedRecordCount;
            if (mParsedRecordCount >= mRecordCount) {
                mHandler.post(new Runnable() {
                    /**
                     * Starts executing the active part of the class' code. This method is
                     * called when a thread is started that has been created with a class which
                     * implements {@code Runnable}.
                     */
                    @Override
                    public void run() {
                        if (isParsing) {
                            isParsing = false;
                            onParsingFinished(mWebView.getUrl().toString());
                        }
                    }
                });
            }
        }

        /**
         * JavaScriptHandler
         */
        public class JavaScriptHandler {

            @JavascriptInterface
            public void setRecordCount(final int count) {
                javascriptRecordCount(count);
            }

            @JavascriptInterface
            public void setRecordInfo(final String logoUri, final String pictureUri) {
                javascriptRecordInfo(logoUri, pictureUri);
            }

            @JavascriptInterface
            public void setRecordData(final int index, final String key, final String headline) {
                javascriptRecordData(index, key, headline);
            }

        }

    }
}
