package com.example.movielibrary.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.movielibrary.R;
import com.example.movielibrary.data.model.Comment;
import com.example.movielibrary.data.model.FavoriteList;
import com.example.movielibrary.data.model.Movie;
import com.example.movielibrary.data.model.ResultAPI;
import com.example.movielibrary.data.model.User;
import com.example.movielibrary.data.model.WatchList;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class NetworkHelper {
    private static final String TAG = "NetworkHelper";
    private static NetworkHelper instance = null;
    private Context context;
    private Gson gson = new Gson();
    private RequestQueue requestQueue;
    private String appId;
    private String apiKey;
    private String hostUrl;

    private NetworkHelper(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);
        this.appId = "yf4CVcMLRwKVoY1cAOLtBJy5jm9MeIdKaNCIbvrw";
        this.apiKey = "F1jlC6Ij9gGwLKJrWNZ8ETlo02Pk3lv69pDY7dGX";
        this.hostUrl = "https://parseapi.back4app.com";
    }

    public static NetworkHelper getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkHelper(context);
        }
        return instance;
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo net = cm.getActiveNetworkInfo();
        return ((net != null) && net.isConnected());
    }

    private void printVolleyErrorDetails (VolleyError error) {
        NetworkResponse errResponse = (error != null) ? error.networkResponse : null;
        int statusCode = 0;
        String data = "";
        if (errResponse != null) {
            statusCode = errResponse.statusCode;
            byte[] bytes = errResponse.data;
            data = (bytes != null) ? new String(bytes, StandardCharsets.UTF_8) : "";
        }

        Log.e(TAG, "Volley error with status code " + statusCode + " received with this message: " + data);
    }

    public void signupUser(final User user, final ResultListener<User> listener) {
        if (!isNetworkConnected()) {
            Log.d(TAG, "network error: ");
            Error error = new Error(context.getString(R.string.network_connection_error));
            listener.onResult(new Result<User>(null, null, error));
            return;
        }

        String url = hostUrl + "/users";
        String userJson = null;
        try {
            userJson = gson.toJson(user);
            Log.d(TAG, "signupUser: " + userJson);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Error error = new Error(context.getString(R.string.network_json_error));
            listener.onResult(new Result<User>(null, null, error));
            return;
        }

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (TextUtils.isEmpty(response)) {
                    Error error = new Error(context.getString(R.string.network_general_error));
                    listener.onResult(new Result<User>(null, null, error));
                    return;
                }

                User resultUser = null;
                try {
                    resultUser = gson.fromJson(response, new TypeToken<User>(){}.getType());
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    Error error = new Error(context.getString(R.string.network_json_error));
                    listener.onResult(new Result<User>(null, null, error));
                    return;
                }

                listener.onResult(new Result<User>(resultUser, null, null));
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                printVolleyErrorDetails(error);
                Error err = new Error(context.getString(R.string.network_general_error));
                listener.onResult(new Result<User>(null, null, err));
            }
        };

        final String jsonStr = userJson;
        StringRequest request = new StringRequest(Request.Method.POST, url, responseListener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("X-Parse-Application-Id", appId);
                headers.put("X-Parse-REST-API-Key", apiKey);
                headers.put("X-Parse-Revocable-Session", "1");
                return headers;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                return jsonStr.getBytes(StandardCharsets.UTF_8);
            }
        };
        requestQueue.add(request);
    }

    public void signInUser(final User user, final ResultListener<User> listener) {
        if (!isNetworkConnected()) {
            Error error = new Error(context.getString(R.string.network_connection_error));
            listener.onResult(new Result<User>(null, null, error));
            return;
        }

        String url = hostUrl + "/login?username=" + user.getUsername() + "&password=" + user.getPassword();

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "User signIn response: " + response);
                if (TextUtils.isEmpty(response)) {
                    Error error = new Error(context.getString(R.string.network_general_error));
                    listener.onResult(new Result<User>(null, null, error));
                    return;
                }

                User resultUser = null;
                try {
                    resultUser = gson.fromJson(response, new TypeToken<User>(){}.getType());
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    Error error = new Error(context.getString(R.string.network_json_error));
                    listener.onResult(new Result<User>(null, null, error));
                    return;
                }

                listener.onResult(new Result<User>(resultUser, null, null));
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                printVolleyErrorDetails(error);
                Error err = new Error(context.getString(R.string.signInError));

                listener.onResult(new Result<User>(null, null, err));
            }
        };

        StringRequest request = new StringRequest(Request.Method.GET, url, responseListener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("X-Parse-Application-Id", appId);
                headers.put("X-Parse-REST-API-Key", apiKey);
                headers.put("X-Parse-Revocable-Session", "1");
                return headers;
            }
        };
        requestQueue.add(request);
    }

    public void getMovie(final String sessionToken, final ResultListener<Movie> listener) {
        if (!isNetworkConnected()) {
            Error error = new Error(context.getString(R.string.network_connection_error));
            listener.onResult(new Result<Movie>(null, null, error));
            return;
        }

        String url = hostUrl + "/classes/Movie";

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (TextUtils.isEmpty(response)) {
                    Error error = new Error(context.getString(R.string.network_general_error));
                    listener.onResult(new Result<Movie>(null, null, error));
                    return;
                }

                ResultAPI<Movie> resultStr = null;
                try {
                    resultStr = gson.fromJson(response, new TypeToken<ResultAPI<Movie>>(){}.getType());
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    Error error = new Error(context.getString(R.string.network_json_error));
                    listener.onResult(new Result<Movie>(null, null, error));
                    return;
                }

                listener.onResult(new Result<Movie>(null, resultStr.getResults(), null));
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                printVolleyErrorDetails(error);
                Error err = new Error(context.getString(R.string.network_general_error));
                listener.onResult(new Result<Movie>(null, null, err));
            }
        };

        StringRequest request = new StringRequest(Request.Method.GET, url, responseListener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("X-Parse-Application-Id", appId);
                headers.put("X-Parse-REST-API-Key", apiKey);
                headers.put("X-Parse-Session-Token", sessionToken);
                return headers;
            }
        };
        requestQueue.add(request);
    }

    public void addMovie(final Movie movie, final String sessionToken, final ResultListener<Movie> listener) {
        if (!isNetworkConnected()) {
            Error error = new Error(context.getString(R.string.network_connection_error));
            listener.onResult(new Result<Movie>(null, null, error));
            return;
        }

        String url = hostUrl + "/classes/Movie";
        String stdJson = null;
        try {
            stdJson = gson.toJson(movie);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Error error = new Error(context.getString(R.string.network_json_error));
            listener.onResult(new Result<Movie>(null, null, error));
            return;
        }

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (TextUtils.isEmpty(response)) {
                    Error error = new Error(context.getString(R.string.network_general_error));
                    listener.onResult(new Result<Movie>(null, null, error));
                    return;
                }

                Movie resultStr = null;
                try {
                    resultStr = gson.fromJson(response, new TypeToken<Movie>(){}.getType());
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    Error error = new Error(context.getString(R.string.network_json_error));
                    listener.onResult(new Result<Movie>(null, null, error));
                    return;
                }

                listener.onResult(new Result<Movie>(resultStr, null, null));
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                printVolleyErrorDetails(error);
                Error err = new Error(context.getString(R.string.network_general_error));
                listener.onResult(new Result<Movie>(null, null, err));
            }
        };

        final String jsonStr = stdJson;
        StringRequest request = new StringRequest(Request.Method.POST, url, responseListener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("X-Parse-Application-Id", appId);
                headers.put("X-Parse-REST-API-Key", apiKey);
                headers.put("X-Parse-Session-Token", sessionToken);
                return headers;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                return jsonStr.getBytes(StandardCharsets.UTF_8);
            }
        };
        requestQueue.add(request);
    }

    public void deleteMovie(final String movieId, final String sessionToken, final ResultListener<Movie> listener) {
        if (!isNetworkConnected()) {
            Error error = new Error(context.getString(R.string.network_connection_error));
            listener.onResult(new Result<>(null, null, error));
            return;
        }

        String url = hostUrl + "/classes/Movie/" + movieId;

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Story delete response: " + response);
                if (TextUtils.isEmpty(response)) {
                    Error error = new Error(context.getString(R.string.network_general_error));
                    listener.onResult(new Result<>(null, null, error));
                    return;
                }

                Movie resultStr = null;
                try {
                    resultStr = gson.fromJson(response, new TypeToken<Movie>(){}.getType());
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    Error error = new Error(context.getString(R.string.network_json_error));
                    listener.onResult(new Result<>(null, null, error));
                    return;
                }

                listener.onResult(new Result<>(resultStr, null, null));
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                printVolleyErrorDetails(error);
                Error err = new Error(context.getString(R.string.network_general_error));
                listener.onResult(new Result<>(null, null, err));
            }
        };

        StringRequest request = new StringRequest(Request.Method.DELETE, url, responseListener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("X-Parse-Application-Id", appId);
                headers.put("X-Parse-REST-API-Key", apiKey);
                headers.put("X-Parse-Session-Token", sessionToken);
                return headers;
            }
        };
        requestQueue.add(request);
    }

    public void updateMovie(final Movie movie, final String sessionToken, final ResultListener<Movie> listener) {
        if (!isNetworkConnected()) {
            Error error = new Error(context.getString(R.string.network_connection_error));
            listener.onResult(new Result<>(null, null, error));
            return;
        }

        String url = hostUrl + "/classes/Movie/" + movie.getId();
        String strJson = null;
        try {
            strJson = gson.toJson(movie);
        } catch (Exception ex) {
            ex.printStackTrace();
            Error error = new Error(context.getString(R.string.network_json_error));
            listener.onResult(new Result<>(null, null, error));
            return;
        }

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Movie update response: " + response);
                if (TextUtils.isEmpty(response)) {
                    Error error = new Error(context.getString(R.string.network_general_error));
                    listener.onResult(new Result<>(null, null, error));
                    return;
                }

                Movie resultStr = null;
                try {
                    resultStr = gson.fromJson(response, new TypeToken<Movie>() {
                    }.getType());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Error error = new Error(context.getString(R.string.network_json_error));
                    listener.onResult(new Result<>(null, null, error));
                    return;
                }

                listener.onResult(new Result<>(resultStr, null, null));
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                printVolleyErrorDetails(error);
                Error err = new Error(context.getString(R.string.network_general_error));
                listener.onResult(new Result<>(null, null, err));
            }
        };

        final String jsonStr = strJson;
        StringRequest request = new StringRequest(Request.Method.PUT, url, responseListener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("X-Parse-Application-Id", appId);
                headers.put("X-Parse-REST-API-Key", apiKey);
                headers.put("X-Parse-Session-Token", sessionToken);
                return headers;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                return jsonStr.getBytes(StandardCharsets.UTF_8);
            }
        };
        requestQueue.add(request);
    }


    public void getComment(final String movieId, final String sessionToken, final ResultListener<Comment> listener) {
        if (!isNetworkConnected()) {
            Error error = new Error(context.getString(R.string.network_connection_error));
            listener.onResult(new Result<Comment>(null, null, error));
            return;
        }

        String url = hostUrl + "/classes/Comment?where={\"movieId\":\""+ movieId+"\"}";

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (TextUtils.isEmpty(response)) {
                    Error error = new Error(context.getString(R.string.network_general_error));
                    listener.onResult(new Result<Comment>(null, null, error));
                    return;
                }

                ResultAPI<Comment> resultStr = null;
                try {
                    resultStr = gson.fromJson(response, new TypeToken<ResultAPI<Comment>>(){}.getType());
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    Error error = new Error(context.getString(R.string.network_json_error));
                    listener.onResult(new Result<Comment>(null, null, error));
                    return;
                }

                listener.onResult(new Result<Comment>(null, resultStr.getResults(), null));
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                printVolleyErrorDetails(error);
                Error err = new Error(context.getString(R.string.network_general_error));
                listener.onResult(new Result<Comment>(null, null, err));
            }
        };

        StringRequest request = new StringRequest(Request.Method.GET, url, responseListener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("X-Parse-Application-Id", appId);
                headers.put("X-Parse-REST-API-Key", apiKey);
                headers.put("X-Parse-Session-Token", sessionToken);
                return headers;
            }
        };
        requestQueue.add(request);
    }

    public void addComment(final Comment comment, final String sessionToken, final ResultListener<Comment> listener) {
        if (!isNetworkConnected()) {
            Error error = new Error(context.getString(R.string.network_connection_error));
            listener.onResult(new Result<Comment>(null, null, error));
            return;
        }

        String url = hostUrl + "/classes/Comment";
        String stdJson = null;
        try {
            stdJson = gson.toJson(comment);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Error error = new Error(context.getString(R.string.network_json_error));
            listener.onResult(new Result<Comment>(null, null, error));
            return;
        }

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (TextUtils.isEmpty(response)) {
                    Error error = new Error(context.getString(R.string.network_general_error));
                    listener.onResult(new Result<Comment>(null, null, error));
                    return;
                }

                Comment resultStr = null;
                try {
                    resultStr = gson.fromJson(response, new TypeToken<Comment>(){}.getType());
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    Error error = new Error(context.getString(R.string.network_json_error));
                    listener.onResult(new Result<Comment>(null, null, error));
                    return;
                }

                listener.onResult(new Result<Comment>(resultStr, null, null));
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                printVolleyErrorDetails(error);
                Error err = new Error(context.getString(R.string.network_general_error));
                listener.onResult(new Result<Comment>(null, null, err));
            }
        };

        final String jsonStr = stdJson;
        StringRequest request = new StringRequest(Request.Method.POST, url, responseListener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("X-Parse-Application-Id", appId);
                headers.put("X-Parse-REST-API-Key", apiKey);
                headers.put("X-Parse-Session-Token", sessionToken);
                return headers;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                return jsonStr.getBytes(StandardCharsets.UTF_8);
            }
        };
        requestQueue.add(request);
    }

    public void getWatchlist(final String userId, final String sessionToken, final ResultListener<WatchList> listener) {
        if (!isNetworkConnected()) {
            Error error = new Error(context.getString(R.string.network_connection_error));
            listener.onResult(new Result<WatchList>(null, null, error));
            return;
        }

        String url = hostUrl + "/classes/Watchlist?where={\"userId\":\""+ userId+"\"}";

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (TextUtils.isEmpty(response)) {
                    Error error = new Error(context.getString(R.string.network_general_error));
                    listener.onResult(new Result<WatchList>(null, null, error));
                    return;
                }

                ResultAPI<WatchList> resultStr = null;
                try {
                    resultStr = gson.fromJson(response, new TypeToken<ResultAPI<WatchList>>(){}.getType());
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    Error error = new Error(context.getString(R.string.network_json_error));
                    listener.onResult(new Result<WatchList>(null, null, error));
                    return;
                }

                listener.onResult(new Result<WatchList>(null, resultStr.getResults(), null));
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                printVolleyErrorDetails(error);
                Error err = new Error(context.getString(R.string.network_general_error));
                listener.onResult(new Result<WatchList>(null, null, err));
            }
        };

        StringRequest request = new StringRequest(Request.Method.GET, url, responseListener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("X-Parse-Application-Id", appId);
                headers.put("X-Parse-REST-API-Key", apiKey);
                headers.put("X-Parse-Session-Token", sessionToken);
                return headers;
            }
        };
        requestQueue.add(request);
    }

    public void addWatchlist(final WatchList watchList, final String sessionToken, final ResultListener<WatchList> listener) {
        if (!isNetworkConnected()) {
            Error error = new Error(context.getString(R.string.network_connection_error));
            listener.onResult(new Result<WatchList>(null, null, error));
            return;
        }

        String url = hostUrl + "/classes/Watchlist";
        String stdJson = null;
        try {
            stdJson = gson.toJson(watchList);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Error error = new Error(context.getString(R.string.network_json_error));
            listener.onResult(new Result<WatchList>(null, null, error));
            return;
        }

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (TextUtils.isEmpty(response)) {
                    Error error = new Error(context.getString(R.string.network_general_error));
                    listener.onResult(new Result<WatchList>(null, null, error));
                    return;
                }

                WatchList resultStr = null;
                try {
                    resultStr = gson.fromJson(response, new TypeToken<WatchList>(){}.getType());
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    Error error = new Error(context.getString(R.string.network_json_error));
                    listener.onResult(new Result<WatchList>(null, null, error));
                    return;
                }

                listener.onResult(new Result<WatchList>(resultStr, null, null));
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                printVolleyErrorDetails(error);
                Error err = new Error(context.getString(R.string.network_general_error));
                listener.onResult(new Result<WatchList>(null, null, err));
            }
        };

        final String jsonStr = stdJson;
        StringRequest request = new StringRequest(Request.Method.POST, url, responseListener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("X-Parse-Application-Id", appId);
                headers.put("X-Parse-REST-API-Key", apiKey);
                headers.put("X-Parse-Session-Token", sessionToken);
                return headers;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                return jsonStr.getBytes(StandardCharsets.UTF_8);
            }
        };
        requestQueue.add(request);
    }

    public void getFavoritelist(final String userId, final String sessionToken, final ResultListener<FavoriteList> listener) {
        if (!isNetworkConnected()) {
            Error error = new Error(context.getString(R.string.network_connection_error));
            listener.onResult(new Result<FavoriteList>(null, null, error));
            return;
        }

        String url = hostUrl + "/classes/Favoritelist?where={\"userId\":\""+ userId+"\"}";

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (TextUtils.isEmpty(response)) {
                    Error error = new Error(context.getString(R.string.network_general_error));
                    listener.onResult(new Result<FavoriteList>(null, null, error));
                    return;
                }

                ResultAPI<FavoriteList> resultStr = null;
                try {
                    resultStr = gson.fromJson(response, new TypeToken<ResultAPI<FavoriteList>>(){}.getType());
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    Error error = new Error(context.getString(R.string.network_json_error));
                    listener.onResult(new Result<FavoriteList>(null, null, error));
                    return;
                }

                listener.onResult(new Result<FavoriteList>(null, resultStr.getResults(), null));
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                printVolleyErrorDetails(error);
                Error err = new Error(context.getString(R.string.network_general_error));
                listener.onResult(new Result<FavoriteList>(null, null, err));
            }
        };

        StringRequest request = new StringRequest(Request.Method.GET, url, responseListener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("X-Parse-Application-Id", appId);
                headers.put("X-Parse-REST-API-Key", apiKey);
                headers.put("X-Parse-Session-Token", sessionToken);
                return headers;
            }
        };
        requestQueue.add(request);
    }

    public void addFavoritelist(final FavoriteList favoriteList, final String sessionToken, final ResultListener<FavoriteList> listener) {
        if (!isNetworkConnected()) {
            Error error = new Error(context.getString(R.string.network_connection_error));
            listener.onResult(new Result<FavoriteList>(null, null, error));
            return;
        }

        String url = hostUrl + "/classes/Favoritelist";
        String stdJson = null;
        try {
            stdJson = gson.toJson(favoriteList);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Error error = new Error(context.getString(R.string.network_json_error));
            listener.onResult(new Result<FavoriteList>(null, null, error));
            return;
        }

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (TextUtils.isEmpty(response)) {
                    Error error = new Error(context.getString(R.string.network_general_error));
                    listener.onResult(new Result<FavoriteList>(null, null, error));
                    return;
                }

                FavoriteList resultStr = null;
                try {
                    resultStr = gson.fromJson(response, new TypeToken<FavoriteList>(){}.getType());
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    Error error = new Error(context.getString(R.string.network_json_error));
                    listener.onResult(new Result<FavoriteList>(null, null, error));
                    return;
                }

                listener.onResult(new Result<FavoriteList>(resultStr, null, null));
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                printVolleyErrorDetails(error);
                Error err = new Error(context.getString(R.string.network_general_error));
                listener.onResult(new Result<FavoriteList>(null, null, err));
            }
        };

        final String jsonStr = stdJson;
        StringRequest request = new StringRequest(Request.Method.POST, url, responseListener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("X-Parse-Application-Id", appId);
                headers.put("X-Parse-REST-API-Key", apiKey);
                headers.put("X-Parse-Session-Token", sessionToken);
                return headers;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                return jsonStr.getBytes(StandardCharsets.UTF_8);
            }
        };
        requestQueue.add(request);
    }

}
