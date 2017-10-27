package com.fablwesn.www.guardianobserver;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.net.URL;
import java.util.List;

/**
 * Loader class to fetch the search query results from the guardian's Web API
 */
public class NewsLoader extends AsyncTaskLoader<List<NewsObject>> {

    // url the load bases upon
    private URL url;

    /* Class constructor */
    NewsLoader(Context context, URL url) {
        super(context);
        this.url = url;
    }

    /* onStartLoading
    *   - starts the loading task
    ***************************************/
    @Override
    protected void onStartLoading() {
        // start the load
        forceLoad();
    }


    /* loadInBackground
    *   - background thread
    ***************************************/
    @Override
    public List<NewsObject> loadInBackground() {
        // return early if empty
        if (url == null) {
            return null;
        }

        // Perform network request, parse the response, and extract a list of news
        // matching search criteria
        return NewsUtils.fetchSearchResults(url);
    }
}
