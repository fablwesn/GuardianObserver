package com.fablwesn.www.guardianobserver;

/**
 * model class representing a single news object
 */
class NewsObject {

    // declaration of String constants used by the class
    static final String NO_HEADLINE_STRING = "no headline";
    static final String NO_SECTION_STRING = "general";
    static final String AUTHOR_PREFIX = "by ";

    /**
     * new's title
     */
    private final String title;

    /**
     * new's author
     */
    private final String author;

    /**
     * new's section
     */
    private final String section;

    /**
     * new's publishedDate date
     */
    private final String publishedDate;

    /**
     * new's web link
     */
    private final String link;

    /* Constructor */
    NewsObject(String title, String author, String section, String publishedDate, String link) {
        this.title = title;
        this.author = author;
        this.section = section;
        this.publishedDate = publishedDate;
        this.link = link;
    }

    /*
    Getters
     */

    String getTitle() {
        return title;
    }

    String getAuthor() {
        return author;
    }

    String getSection() {
        return section;
    }

    String getPublishedDate() {
        return publishedDate;
    }

    String getLink() {
        return link;
    }
}
