package com.example.movielibrary.ui.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movielibrary.R;
import com.example.movielibrary.data.model.Movie;
import com.example.movielibrary.utils.DataStore;

import java.util.ArrayList;
import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {

    private Context context;
    private List<Movie> movies;
    private LayoutInflater layoutInflater;
    private MovieAdapterCallback callback;
    private DataStore dataStore;

    public MovieAdapter(Context context, List<Movie> movies, MovieAdapterCallback callback, int pageFunction) {
        this.context = context;
        dataStore = DataStore.getInstance();
        this.movies = movies;
        this.layoutInflater = LayoutInflater.from(context);
        this.callback = callback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("onCreateViewHolder: ", String.valueOf(parent.getResources().getResourceEntryName(parent.getId())));
        if (String.valueOf(parent.getResources().getResourceEntryName(parent.getId())).equals("myStories_recyclerView")) {
            View view = layoutInflater.inflate(R.layout.movie_item, parent, false);
            return new ViewHolder(view, 0);
        } else if (String.valueOf(parent.getResources().getResourceEntryName(parent.getId())).equals("allMovies_recyclerView")) {
            View view = layoutInflater.inflate(R.layout.movie_item, parent, false);
            return new ViewHolder(view, 1);
        }
        View view = layoutInflater.inflate(R.layout.movie_item, parent, false);
        return new ViewHolder(view, 0);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setData(position);
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView titleTextView;
        private TextView castTextView;
        private ImageView likeBtn;
        private TextView likeNoTextView;
        private int position;
        private int activity;

        ViewHolder(View itemView, int activity) {
            super(itemView);
            this.activity = activity;
            context = itemView.getContext();
            titleTextView = itemView.findViewById(R.id.title_tv);
            castTextView = itemView.findViewById(R.id.cast_tv);
            likeBtn = itemView.findViewById(R.id.favorite_tv);
        }

        public void setData(int position) {

            Movie movie = movies.get(position);
            this.position = position;
            titleTextView.setText(movie.getName());
            castTextView.setText(movie.getCast());
            for (Movie fav : dataStore.getFavoriteLists()) {
                if (movie.getId().equals(fav.getId())) {
                    likeBtn.setVisibility(View.VISIBLE);
                    break;
                }
                likeBtn.setVisibility(View.INVISIBLE);
            }

            itemView.setOnClickListener(view ->

            {
                callback.onShowDetail(position);
            });
        }
    }

    public interface MovieAdapterCallback {
        void onShowDetail(int position);
    }

}
