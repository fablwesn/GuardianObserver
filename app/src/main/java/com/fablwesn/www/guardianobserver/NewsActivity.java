package com.fablwesn.www.guardianobserver;

import android.app.LoaderManager;
import android.content.Loader;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.WindowManager;

import com.fablwesn.www.guardianobserver.databinding.ActivityNewsBinding;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class NewsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<NewsObject>> {

    // Binding of the layout to avoid using findViewById for performance
    private ActivityNewsBinding layout;
    // used for saving list scroll position
    private Bundle recyclerStateBundle;
    // adapter used for the list
    private NewsAdapter recyclerViewAdapter;
    // loader manager used
    private LoaderManager loaderManager;

    // list containing all news to display
    private List<NewsObject> displayedNewsList;
    // final url used for requesting
    private URL requestUrl;

    // page of the request to display
    private int page = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layout = DataBindingUtil.setContentView(this, R.layout.activity_news);
        // set fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        // Get a reference to the LoaderManager, in order to interact with loaders.
        loaderManager = getLoaderManager();
        // prepare the recycler view
        layout.listRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        layout.listRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //prepare the swipe-refresh-layout for refreshing data
        prepareSwipeRefresh();

        // check for network connectivity
        // if there is none inform the user and return
        if (!NewsUtils.isNetworkAvailable(NewsActivity.this)) {
            informUser(getResources().getString(R.string.inform_no_network));
            return;
        }

        // if there was a problem with creating the request url, inform the user and return early
        if (NewsUtils.buildUrl(getTodaysDate(), getYesterdaysDate(), page) == null) {
            informUser(getResources().getString(R.string.inform_build_url));
            return;
        }

        // if the app recreates (orientation change) get the last page viewed
        if (savedInstanceState != null)
            page = savedInstanceState.getInt(getResources().getString(R.string.extra_key_page));

        // if the current page being displayed is the first one, hide the previous page button
        if (page != 1)
            layout.buttonPrevPage.setVisibility(View.VISIBLE);

        // build url with all available data
        requestUrl = NewsUtils.buildUrl(getTodaysDate(), getYesterdaysDate(), page);

        // start loading
        loaderManager.initLoader(0, null, this);
    }

    /* onSaveInstanceState
       *   - save current page the user was viewing
       **********************************************************************/
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt(getResources().getString(R.string.extra_key_page), page);
    }

    /* onPause
        *   - saves recycler view scroll position (on screen orientation change for example)
        **********************************************************************/
    @Override
    protected void onPause() {
        super.onPause();
        // save RecyclerView state
        recyclerStateBundle = new Bundle();
        if (recyclerViewAdapter != null) {
            Parcelable listState = layout.listRecyclerView.getLayoutManager().onSaveInstanceState();
            recyclerStateBundle.putParcelable(getResources().getString(R.string.extra_key_recycler_state), listState);
        }

        if (!NewsUtils.isNetworkAvailable(NewsActivity.this))
            layout.listRecyclerView.setVisibility(View.INVISIBLE);
        else
            layout.listRecyclerView.setVisibility(View.VISIBLE);
    }

    /* onResume
    *   - retrieve previous recycler view scroll position (on screen orientation change for example)
    **********************************************************************/
    @Override
    protected void onResume() {
        super.onResume();

        // restore RecyclerView state
        if (recyclerStateBundle != null) {
            if (recyclerViewAdapter != null) {
                Parcelable listState = recyclerStateBundle.getParcelable(getResources().getString(R.string.extra_key_recycler_state));
                layout.listRecyclerView.getLayoutManager().onRestoreInstanceState(listState);
            }
        }

        if (!NewsUtils.isNetworkAvailable(NewsActivity.this))
            layout.listRecyclerView.setVisibility(View.INVISIBLE);
        else
            layout.listRecyclerView.setVisibility(View.VISIBLE);
    }

    /* onCreateLoader
    *   - called when a new Loader needs to be created
    **********************************************************************/
    @Override
    public Loader<List<NewsObject>> onCreateLoader(int i, Bundle bundle) {
        return new NewsLoader(NewsActivity.this, requestUrl);
    }

    /* onLoadFinished
     *  - update views accordingly
     *  - update list adapter
     *********************************************************************/
    @Override
    public void onLoadFinished(Loader<List<NewsObject>> loader, List<NewsObject> news) {

        // Hide progress indicator because the data has been loaded
        layout.circularProgressionBar.setVisibility(View.GONE);
        layout.swipeRefreshLayout.setRefreshing(false);

        // check for connection in case it broke off while loading and return in case it did while informing the user
        if (!NewsUtils.isNetworkAvailable(NewsActivity.this)) {
            informUser(getResources().getString(R.string.inform_network_lost));
            layout.listRecyclerView.setVisibility(View.INVISIBLE);
            return;
        }

        // Clear the adapter of previous news data
        clearNewsAdapter();

        // If there is a valid list of {@link NewsObject}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (news != null && !news.isEmpty()) {
            // show/hide appropriate views
            layout.listRecyclerView.setVisibility(View.VISIBLE);
            layout.buttonNextPage.setVisibility(View.VISIBLE);
            layout.emptyText.setVisibility(View.INVISIBLE);
            layout.swipeRefreshLayout.setRefreshing(false);

            //create and adapter
            recyclerViewAdapter = new NewsAdapter(news);
            layout.listRecyclerView.setAdapter(recyclerViewAdapter);

            // update header text
            layout.headerText.setText(getResources().getString(R.string.header_title, NewsUtils.totalArticles));
        }
        // if empty, show/hide views and inform the user
        else {
            layout.listRecyclerView.setVisibility(View.INVISIBLE);
            informUser(getResources().getString(R.string.inform_no_results));
            layout.emptyText.setVisibility(View.VISIBLE);
            layout.buttonNextPage.setVisibility(View.INVISIBLE);
        }
    }


    /* onLoaderReset
    *   - clears the adapter to make room for new data
    *************************************************************/
    @Override
    public void onLoaderReset(Loader<List<NewsObject>> loader) {
        clearNewsAdapter();
    }


    /*                                   private functions                                        */
    /*                                   ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯                                        */

    /**
     * sets an onRefreshListener for the user to update the view
     */
    private void prepareSwipeRefresh() {
        layout.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (NewsUtils.isNetworkAvailable(NewsActivity.this)) {
                    layout.listRecyclerView.setVisibility(View.VISIBLE);
                    // go back to the first page
                    page = 1;
                    //build url and start loading new data
                    requestUrl = NewsUtils.buildUrl(getTodaysDate(), getYesterdaysDate(), page);

                    // start loading
                    if (getLoaderManager().getLoader(0) == null)
                        loaderManager.initLoader(0, null, NewsActivity.this);
                    else
                        loaderManager.restartLoader(0, null, NewsActivity.this);

                    layout.buttonPrevPage.setVisibility(View.INVISIBLE);
                } else {
                    informUser(getResources().getString(R.string.inform_no_network));
                    layout.swipeRefreshLayout.setRefreshing(false);
                    layout.listRecyclerView.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    /**
     * get's today's date on the device
     */
    private String getTodaysDate() {
        // get the current to date to look for news till today
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        return formatter.format(Calendar.getInstance().getTime());
    }

    /**
     * get's yesterday's date on the device
     */
    private String getYesterdaysDate() {
        // get yesterday's date to look for news from yesterday
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);

        return formatter.format(cal.getTime());
    }

    /**
     * equivalent of list's clear() for the recycler view
     */
    private void clearNewsAdapter() {
        if (displayedNewsList != null) {
            displayedNewsList.clear();
            recyclerViewAdapter.notifyDataSetChanged();
        }
    }

    /**
     * hides the loading indicator and displays a message explaining why the list couldn't be loaded
     *
     * @param message message to be displayed
     */
    private void informUser(String message) {
        // Hide progress indicator
        layout.circularProgressionBar.setVisibility(View.GONE);
        // set text
        layout.emptyText.setVisibility(View.VISIBLE);
        layout.emptyText.setText(message);
    }


    /*                                  xml onClick methods                                       */
    /*                                  ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯                                       */

    /**
     * next button on the bottom {@link NewsActivity}
     *
     * @param nextButton button on the bottom right
     */
    public void nextPage(View nextButton) {
        if (NewsUtils.isNetworkAvailable(NewsActivity.this)) {
            layout.listRecyclerView.setVisibility(View.VISIBLE);
            // increment the page
            page += 1;
            // if we are above the first page, display the previous button
            if (page > 1)
                layout.buttonPrevPage.setVisibility(View.VISIBLE);
            // build new url
            requestUrl = NewsUtils.buildUrl(getTodaysDate(), getYesterdaysDate(), page);
            // display loading indicator
            layout.circularProgressionBar.setVisibility(View.VISIBLE);
            // start loading
            loaderManager.restartLoader(0, null, NewsActivity.this);
        } else {
            informUser(getResources().getString(R.string.inform_no_network));
            layout.listRecyclerView.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * previous button on the bottom {@link NewsActivity}
     *
     * @param nextButton button on the bottom left
     */
    public void prevPage(View nextButton) {
        if (NewsUtils.isNetworkAvailable(NewsActivity.this)) {
            layout.listRecyclerView.setVisibility(View.VISIBLE);
            // decrement the page
            page -= 1;
            // if we are on page 1 afterwards (or below), hide the previous button
            if (page <= 1)
                layout.buttonPrevPage.setVisibility(View.INVISIBLE);

            // build new url
            requestUrl = NewsUtils.buildUrl(getTodaysDate(), getYesterdaysDate(), page);
            // display loading indicator
            layout.circularProgressionBar.setVisibility(View.VISIBLE);
            // start loading
            loaderManager.restartLoader(0, null, NewsActivity.this);
        } else {
            informUser(getResources().getString(R.string.inform_no_network));
            layout.listRecyclerView.setVisibility(View.INVISIBLE);
        }
    }
}
