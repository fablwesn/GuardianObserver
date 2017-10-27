package com.fablwesn.www.guardianobserver;

/**
 * Class holding default values for the request
 */
class DefaultValues {
    /*        strings the url is built from     */
    final static String RAW_QUERY_URL_STRING_START = "http://content.guardianapis.com/search?";
    final static String RAW_QUERY_URL_STRING_PARAM_DATE_FROM = "from-date=";
    final static String RAW_QUERY_URL_STRING_PARAM_DATE_TO = "&to-date=";
    final static String RAW_QUERY_URL_STRING_MIDDLE = "&order-by=newest&show-fields=byline&type=article&page-size=30&";
    final static String RAW_QUERY_URL_STRING_PARAM_PAGE = "page=";
    final static String RAW_QUERY_URL_STRING_END = "&api-key=test";
    /*  request type */
    final static String REQUEST_METHOD_STRING = "GET";
    /* valid request response code */
    final static int REQUEST_VALID_RESPONSE_CODE_INT = 200;
    /* in milliseconds */
    final static int REQUEST_READ_TIMEOUT_INT = 10000;
    /* in milliseconds */
    final static int REQUEST_CONNECT_TIMEOUT_INT = 15000;

    /* AUTHOR DATE SEPARATOR */
    final static String LIST_AUTHOR_PUBLISHED_SEPARATOR = " | ";

    // The Guardian Web-API-Keys
    final static String API_KEY_RESPONSE = "response";
    final static String API_KEY_RESULTS = "results";
    final static String API_KEY_SECTION = "sectionName";
    final static String API_KEY_PUBLISHED_DATE = "webPublicationDate";
    final static String API_KEY_TOTAL_ARTICLES = "total";
    final static String API_KEY_TITLE = "webTitle";
    final static String API_KEY_WEBURL = "webUrl";
    final static String API_KEY_FIELDS = "fields";
    final static String API_KEY_BYLINE = "byline";
}
