package com.fablwesn.www.guardianobserver;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Adapter for the Recycler view, displays the results in a list and styles the views
 */
class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.MyViewHolder> {

    // list with {@link NewsObject}s
    private final List<NewsObject> newsList;

    NewsAdapter(List<NewsObject> list) {
        newsList = list;
    }

    /* onCreateViewHolder
    *   - assign the layout to use
    *******************************************/
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.news_item, parent, false);

        return new MyViewHolder(itemView);
    }

    /* onBindViewHolder
    *   - set correct data
    *******************************************/
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // Checking feed size to handle IndexOutOfBoundsException error
        if (position >= getItemCount())
            return;

        final NewsObject newsModel = newsList.get(position);

        // update text views
        holder.title.setText(newsModel.getTitle());
        holder.section.setText(newsModel.getSection());
        holder.details.setText(styleDetails(newsModel.getAuthor(), newsModel.getPublishedDate()));

        // set click listener opening the article's link
        holder.clickable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View listItemParent) {
                Uri newsArticleUri = Uri.parse(newsModel.getLink());

                listItemParent.getContext().startActivity(new Intent(Intent.ACTION_VIEW, newsArticleUri));
            }
        });
    }

    /* getItemCount
    *   - return the item quantity of the list
    *******************************************/
    @Override
    public int getItemCount() {
        return newsList.size();
    }

    /**
     * puts author and date into a textfield and styles it
     *
     * @param author    author of the article
     * @param published date the article was published
     * @return detail string including author and date
     */
    private String styleDetails(String author, String published) {
        if (author.isEmpty())
            return published;

        return author + DefaultValues.LIST_AUTHOR_PUBLISHED_SEPARATOR + published;
    }

    // single list item, get everything we need to display the results correctly
    class MyViewHolder extends RecyclerView.ViewHolder {

        final TextView title;
        final TextView section;
        final TextView details;
        final View clickable;

        MyViewHolder(View listItem) {
            super(listItem);
            // get views to change
            title = listItem.findViewById(R.id.title_list_text);
            section = listItem.findViewById(R.id.section_list_text);
            details = listItem.findViewById(R.id.details_list_text);
            clickable = listItem.findViewById(R.id.clickable_list_view);
        }
    }
}