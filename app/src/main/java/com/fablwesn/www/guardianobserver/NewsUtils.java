package com.fablwesn.www.guardianobserver;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Utility class containing all helper methods to request, receive and manipulate a response from the Guardian WebApi
 */
class NewsUtils {


    //Tag for logging
    private static final String LOG_TAG = NewsUtils.class.getName();
    //Error message for http request
    private static final String ERROR_HTTP = "Problem making the HTTP request.";
    //Error message for error response code
    private static final String ERROR_RESPONSE = "Server Error! Response Code: ";
    //Error message for problems with retrieved json
    private static final String ERROR_JSON = "Problem retrieving the results.";
    //Error message for parsing
    private static final String ERROR_PARSING = "Problem parsing the results. Please inform feedback@fablwesn.com";
    // Log error message when building the url failed
    private static final String ERROR_LOG_BUILDING_URL = "Problem building the URL. Please inform feedback@fablwesn.com or try a different query";
    // Log error message when formatting the date failed
    private static final String ERROR_LOG_FORMATTING_DATE = "An exception was encountered while trying to parse a date";
    // stores the total articles found for the query
    static int totalArticles = 0;

    /**
     * check's if the device has internet connectivity
     *
     * @param context current context
     * @return true if there is network access, false if the device is offline
     */
    static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * create the needed data for the list view
     *
     * @param url request url
     * @return list containing {@link NewsObject} objects from requested search query
     */
    static List<NewsObject> fetchSearchResults(URL url) {
        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, ERROR_HTTP, e);
            jsonResponse = "";
        }

        // Return the list of {@link NewsObject}s
        return parseResultsJson(jsonResponse);
    }

    /**
     * Make a HTTP request to the given URL and return a String containing the json response
     *
     * @param url url to get the response from
     * @return string containing the response as json
     * @throws IOException closing the input stream could throw an IOException
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        // try connecting to the url
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(DefaultValues.REQUEST_READ_TIMEOUT_INT);
            urlConnection.setConnectTimeout(DefaultValues.REQUEST_CONNECT_TIMEOUT_INT);
            urlConnection.setRequestMethod(DefaultValues.REQUEST_METHOD_STRING);
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == DefaultValues.REQUEST_VALID_RESPONSE_CODE_INT) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, ERROR_RESPONSE + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, ERROR_JSON, e);
        } finally {
            // close the connection if we didn't get the response we needed
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        // return the response string
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole (validated) JSON response from the server
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Reads JSONString and extracts relevant data from it
     *
     * @param JSONString - result of the previous http query parsed into String format
     * @return List<NewsObject> a list of news objects
     */
    static private List<NewsObject> parseResultsJson(String JSONString) {
        List<NewsObject> newsArticleList = new ArrayList<>();

        String headline = NewsObject.NO_HEADLINE_STRING,
                author = "",
                section = NewsObject.NO_SECTION_STRING,
                timePublished = "",
                articleLink = "";

        try {
            // convert String to a JSONObject
            JSONObject jsonObject = new JSONObject(JSONString);

            // extract: 1) "response" JSONObject and 2) "response" JSONArray
            // check whether "response" JSONObject -> "response" JSONArray are available at all
            // if true - the parsing continues, else - we return an empty ArrayList, as there actually is no data to display
            if (jsonObject.getJSONObject(DefaultValues.API_KEY_RESPONSE).has(DefaultValues.API_KEY_RESULTS)) {
                JSONArray resultsArray = jsonObject.getJSONObject(DefaultValues.API_KEY_RESPONSE).getJSONArray(DefaultValues.API_KEY_RESULTS);
                totalArticles = Integer.valueOf(jsonObject.getJSONObject(DefaultValues.API_KEY_RESPONSE).getString(DefaultValues.API_KEY_TOTAL_ARTICLES));

                // Loop through each item in the array
                // Get NewsObject JSONObject at position i
                int item;
                for (item = 0; item < resultsArray.length(); item++) {
                    JSONObject newsArticleInfo = resultsArray.getJSONObject(item);

                    // extract "sectionName" for the section NewsObject belongs to
                    if (newsArticleInfo.has(DefaultValues.API_KEY_SECTION))
                        section = newsArticleInfo.getString(DefaultValues.API_KEY_SECTION);


                    // extract "webPublicationDate" for the publishing time of NewsObject
                    String timeUnformatted;
                    if (newsArticleInfo.has(DefaultValues.API_KEY_PUBLISHED_DATE)) {
                        timeUnformatted = newsArticleInfo.getString(DefaultValues.API_KEY_PUBLISHED_DATE); // format in the JSON response "YYYY-MM-DDTHH:MM:SSZ"
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH); // convert the String into Date
                        try {
                            Date date = format.parse(timeUnformatted);
                            timePublished = (String) android.text.format.DateFormat.format("MMM" + " " + "dd" + ", " + "HH:mm", date); // format the date and cast it to String again
                        } catch (ParseException pe) {
                            Log.e(LOG_TAG, ERROR_LOG_FORMATTING_DATE + pe);
                        }

                    }

                    // extract "webTitle" for the headline of NewsObject
                    if (newsArticleInfo.has(DefaultValues.API_KEY_TITLE)) {
                        headline = newsArticleInfo.getString(DefaultValues.API_KEY_TITLE);
                        // some headline strings have format: "headline | author", we discard the author part
                        if (headline.contains("|"))
                            headline = headline.substring(0, headline.indexOf("|") - 1);
                    }

                    // extract "webUrl" for the Url link of NewsObject
                    if (newsArticleInfo.has(DefaultValues.API_KEY_WEBURL)) {
                        articleLink = newsArticleInfo.getString(DefaultValues.API_KEY_WEBURL);
                    }

                    // extract "byline" for the author of NewsObject
                    if (newsArticleInfo.getJSONObject(DefaultValues.API_KEY_FIELDS).has(DefaultValues.API_KEY_BYLINE))
                        author = NewsObject.AUTHOR_PREFIX + shortenedAuthor(newsArticleInfo.getJSONObject(DefaultValues.API_KEY_FIELDS).getString(DefaultValues.API_KEY_BYLINE));


                    // create NewsObject object from the extracted data
                    NewsObject newsArticle = new NewsObject(
                            headline,
                            author,
                            section,
                            timePublished,
                            articleLink
                    );

                    // add the object to List
                    newsArticleList.add(newsArticle);
                }
            }
        } catch (JSONException exc_04) {
            Log.e(LOG_TAG, ERROR_PARSING + exc_04);
        }
        // return result of the method
        return newsArticleList;
    }

    /**
     * shortens the author string, in case more than one is given
     *
     * @param author raw String
     * @return shortened String
     */
    private static String shortenedAuthor(String author) {
        // some contain multiple authors separated by ',', only show one of them
        if (author.contains(","))
            author = author.substring(0, author.indexOf(",") - 1);
        return author;
    }

    /**
     * build the url for requesting
     *
     * @param dateToday     today's date for the to-date param
     * @param dateYesterday yesterday's date for the from-date param
     * @param page          page to display
     * @return request url
     */
    static URL buildUrl(String dateToday, String dateYesterday, int page) {
        String url =
                // build the string url puzzle
                DefaultValues.RAW_QUERY_URL_STRING_START +
                        DefaultValues.RAW_QUERY_URL_STRING_PARAM_DATE_FROM + dateYesterday.trim() +
                        DefaultValues.RAW_QUERY_URL_STRING_PARAM_DATE_TO + dateToday.trim() +
                        DefaultValues.RAW_QUERY_URL_STRING_MIDDLE +
                        DefaultValues.RAW_QUERY_URL_STRING_PARAM_PAGE + page +
                        DefaultValues.RAW_QUERY_URL_STRING_END;

        // create url and return null on fail
        try {
            return new URL(url);

        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, ERROR_LOG_BUILDING_URL, e);
            return null;
        }
    }
}
