/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.uamp.model;

import android.annotation.TargetApi;
import android.media.MediaMetadata;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.example.android.uamp.ui.BaseActivity;
import com.example.android.uamp.utils.LogHelper;
import com.google.gson.annotations.SerializedName;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Utility class to get a list of MusicTrack's based on a server-side JSON
 * configuration.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MusicProvider{

    private static final String TAG = LogHelper.makeLogTag(MusicProvider.class);

    private static final String CATALOG_URL =
            //"http://api.soundcloud.com/tracks.json?client_id=4f0b007dd6be94f1098f30bcd1e1a809&q=chicago&limit=50";
            "http://api.soundcloud.com";
    //"http://storage.googleapis.com/automotive-media/music.json";

    public static final String CUSTOM_METADATA_TRACK_SOURCE = "__SOURCE__";

    //private static final String JSON_MUSIC = "music";
    private static final String JSON_TITLE = "title";//--
    private static final String JSON_ALBUM = "permalink";//--
    private static final String JSON_ARTIST = "permalink";//--
    private static final String JSON_GENRE = "genre";//--
    private static final String JSON_SOURCE = "stream_url";//--  remain
    private static final String JSON_IMAGE = "artwork_url";//--
    private static final String JSON_TRACK_NUMBER = "id"; //--
    private static final String JSON_TOTAL_TRACK_COUNT = "likes_count";//--
    private static final String JSON_DURATION = "duration";//--
    retrofit.Response<List<GitResult>> mainResponse;
    //retrofit.Response<List<GitResult>> responseRetriveMedia;
    /*private static final String JSON_MUSIC = "music";
    private static final String JSON_TITLE = "title";
    private static final String JSON_ALBUM = "album";
    private static final String JSON_ARTIST = "artist";
    private static final String JSON_GENRE = "genre";
    private static final String JSON_SOURCE = "source";
    private static final String JSON_IMAGE = "image";
    private static final String JSON_TRACK_NUMBER = "trackNumber";
    private static final String JSON_TOTAL_TRACK_COUNT = "totalTrackCount";
    private static final String JSON_DURATION = "duration";*/

    // Categorized caches for music track data:
    private ConcurrentMap<String, List<MediaMetadata>> mMusicListByGenre;
    private final ConcurrentMap<String, MutableMediaMetadata> mMusicListById;
    //SearchKeyword


    private final Set<String> mFavoriteTracks;



    enum State {
        NON_INITIALIZED, INITIALIZING, INITIALIZED
    }

    private volatile State mCurrentState = State.NON_INITIALIZED;

    public interface Callback {
        void onMusicCatalogReady(boolean success);
    }

    public MusicProvider() {
        mMusicListByGenre = new ConcurrentHashMap<>();
        mMusicListById = new ConcurrentHashMap<>();
        mFavoriteTracks = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    }

    /**
     * Get an iterator over the list of genres
     *
     * @return genres
     */
    public Iterable<String> getGenres() {
        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        return mMusicListByGenre.keySet();
    }

    /**
     * Get music tracks of the given genre
     *
     */
    public Iterable<MediaMetadata> getMusicsByGenre(String genre) {
        if (mCurrentState != State.INITIALIZED || !mMusicListByGenre.containsKey(genre)) {
            return Collections.emptyList();
        }
        return mMusicListByGenre.get(genre);
    }

    /**
     * Very basic implementation of a search that filter music tracks with title containing
     * the given query.
     *
     */
    public Iterable<MediaMetadata> searchMusicBySongTitle(String query) {
        return searchMusic(MediaMetadata.METADATA_KEY_TITLE, query);
    }

    /**
     * Very basic implementation of a search that filter music tracks with album containing
     * the given query.
     *
     */
    public Iterable<MediaMetadata> searchMusicByAlbum(String query) {
        return searchMusic(MediaMetadata.METADATA_KEY_ALBUM, query);
    }

    /**
     * Very basic implementation of a search that filter music tracks with artist containing
     * the given query.
     *
     */
    public Iterable<MediaMetadata> searchMusicByArtist(String query) {
        return searchMusic(MediaMetadata.METADATA_KEY_ARTIST, query);
    }


    Iterable<MediaMetadata> searchMusic(String metadataField, String query) {
        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        ArrayList<MediaMetadata> result = new ArrayList<>();
        query = query.toLowerCase(Locale.US);
        for (MutableMediaMetadata track : mMusicListById.values()) {
            if (track.metadata.getString(metadataField).toLowerCase(Locale.US)
                    .contains(query)) {
                result.add(track.metadata);
            }
        }
        return result;
    }


    /**
     * Return the MediaMetadata for the given musicID.
     *
     * @param musicId The unique, non-hierarchical music ID.
     */
    public MediaMetadata getMusic(String musicId) {
        return mMusicListById.containsKey(musicId) ? mMusicListById.get(musicId).metadata : null;
    }

    public synchronized void updateMusic(String musicId, MediaMetadata metadata) {
        MutableMediaMetadata track = mMusicListById.get(musicId);
        if (track == null) {
            return;
        }

        String oldGenre = track.metadata.getString(MediaMetadata.METADATA_KEY_GENRE);
        String newGenre = metadata.getString(MediaMetadata.METADATA_KEY_GENRE);

        track.metadata = metadata;

        // if genre has changed, we need to rebuild the list by genre
        if (!oldGenre.equals(newGenre)) {
            buildListsByGenre();
        }
    }

    public void setFavorite(String musicId, boolean favorite) {
        if (favorite) {
            mFavoriteTracks.add(musicId);
        } else {
            mFavoriteTracks.remove(musicId);
        }
    }

    public boolean isFavorite(String musicId) {
        return mFavoriteTracks.contains(musicId);
    }

    public boolean isInitialized() {
        return mCurrentState == State.INITIALIZED;
    }

    /**
     * Get the list of music tracks from a server and caches the track information
     * for future reference, keying tracks by musicId and grouping by genre.
     */
    public void retrieveMediaAsync(final Callback callback) {
        LogHelper.d(TAG, "retrieveMediaAsync called");
        if (mCurrentState == State.INITIALIZED) {
            // Nothing to do, execute callback immediately
            callback.onMusicCatalogReady(true);
            return;
        }

        // Asynchronously load the music catalog in a separate thread
        new AsyncTask<Void, Void, State>() {
            @Override
            protected State doInBackground(Void... params) {


                mainResponse =  run(CATALOG_URL);

                return mCurrentState;
            }

            @Override
            protected void onPostExecute(State current) {
                if (callback != null) {
                    callback.onMusicCatalogReady(current == State.INITIALIZED);
                }
            }
        }.execute();
    }

    private synchronized void buildListsByGenre() {
        ConcurrentMap<String, List<MediaMetadata>> newMusicListByGenre = new ConcurrentHashMap<>();

        for (MutableMediaMetadata m : mMusicListById.values()) {
            String genre = m.metadata.getString(MediaMetadata.METADATA_KEY_GENRE);//METADATA_KEY_YEAR--METADATA_KEY_GENRE
            if(genre != null && genre.length() > 1)
            {

            }
            else
            {
                genre = "Mix Genere";
            }
            List<MediaMetadata> list = newMusicListByGenre.get(genre);
            if (list == null) {
                list = new ArrayList<>();
                newMusicListByGenre.put(genre, list);
            }
            list.add(m.metadata);
        }
        mMusicListByGenre = newMusicListByGenre;
    }

    private synchronized void retrieveMedia() {
        try {
            if (mCurrentState == State.NON_INITIALIZED) {
                mCurrentState = State.INITIALIZING;

                Log.e("=====retrieveMedia=","---======url=======>>>"+CATALOG_URL);



                Log.e("=====retrieveMedia=","---======response==========>>>"+mainResponse);

                if(mainResponse !=null )
                {
                    Log.e("=====retrieveMedia=","---======responseRetriveMedia=----Not null==");

                    if (mainResponse != null) {
                        for (int j = 0; j < mainResponse.body().size(); j++) {
                            MediaMetadata item = buildFromJSON(mainResponse.body().get(j), "");
                            String musicId = item.getString(MediaMetadata.METADATA_KEY_MEDIA_ID);
                            Log.e("=====retrieveMedia="+j,"-------musicId-->"+musicId);
                            mMusicListById.put(musicId, new MutableMediaMetadata(musicId, item));
                        }
                        buildListsByGenre();
                    }
                    mCurrentState = State.INITIALIZED;
                }
                if (mainResponse == null) {
                    Log.e("=====retrieveMedia=","---======responseRetriveMedia=---- null==");
                    return;
                }

            }
        } catch (JSONException e) {
            LogHelper.e(TAG, e, "Could not retrieve music list");
        } finally {
            if (mCurrentState != State.INITIALIZED) {
                // Something bad happened, so we reset state to NON_INITIALIZED to allow
                // retries (eg if the network connection is temporary unavailable)
                mCurrentState = State.NON_INITIALIZED;
            }
        }
    }

    private MediaMetadata buildFromJSON(GitResult  json, String basePath) throws JSONException { //JSONObject json
        /*String title = json.getString(JSON_TITLE);
        String album = json.getString(JSON_ALBUM);
        String artist = json.getString(JSON_ARTIST);
        String genre = json.getString(JSON_GENRE);
        String source = json.getString(JSON_SOURCE);
        String iconUrl = json.getString(JSON_IMAGE);
        int trackNumber = json.getInt(JSON_TRACK_NUMBER);
        int totalTrackCount = json.getInt(JSON_TOTAL_TRACK_COUNT);
        int duration = json.getInt(JSON_DURATION) * 1000; // ms*/


        String title = json.getTitle();
        String album = json.getPermalink();
        String artist = json.getPermalink();
        String genre = json.getGenre();
        String source = json.getStream_url();
        String iconUrl = json.getArtwork_url();

        int trackNumber = 1,totalTrackCount = 1,duration = 1;
        if( json.getId() !=null)
        {
             trackNumber = Integer.parseInt( json.getId());
        }
        if( json.getLikes_count() !=null)
        {
             totalTrackCount = Integer.parseInt( json.getLikes_count());
        }
        if( json.getDuration() !=null)
        {
             duration = (Integer.parseInt( json.getDuration()) ); // for milisec; * 1000
        }




        LogHelper.d(TAG, "Found music track: ", json);

        // Media is stored relative to JSON file
        if (source==null || !source.startsWith("http") ) {
            //source = source +"?client_id=4f0b007dd6be94f1098f30bcd1e1a809";
          // source="https://api.soundcloud.com/tracks/135948945/stream?client_id=4f0b007dd6be94f1098f30bcd1e1a809";
          source ="http://storage.googleapis.com/automotive-media/Jazz_In_Paris.mp3";
        }
        if (iconUrl==null || !iconUrl.startsWith("http")) {
            iconUrl = "https://i1.sndcdn.com/artworks-000071463904-nhv9da-large.jpg";
        }
        else
        {
            if(iconUrl.contains("large.jpg"))
            {

                iconUrl = iconUrl.replaceAll("large.jpg","crop.jpg");
            }

        }

//        source ="http://storage.googleapis.com/automotive-media/Jazz_In_Paris.mp3";

       /* if (!iconUrl.startsWith("http")) {
            iconUrl = iconUrl;
        }*/
        // Since we don't have a unique ID in the server, we fake one using the hashcode of
        // the music source. In a real world app, this could come from the server.
        String id = String.valueOf(source+"?client_id=4f0b007dd6be94f1098f30bcd1e1a809".hashCode());

        Log.e("===source==="+source,"====id=="+id);
        // Adding the music source to the MediaMetadata (and consequently using it in the
        // mediaSession.setMetadata) is not a good idea for a real world music app, because
        // the session metadata can be accessed by notification listeners. This is done in this
        // sample for convenience only.
        return new MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_MEDIA_ID, id)
                .putString(CUSTOM_METADATA_TRACK_SOURCE, source+"?client_id=4f0b007dd6be94f1098f30bcd1e1a809")
                .putString(MediaMetadata.METADATA_KEY_ALBUM, album)
                .putString(MediaMetadata.METADATA_KEY_ARTIST, artist)
                .putLong(MediaMetadata.METADATA_KEY_DURATION, duration)
                .putString(MediaMetadata.METADATA_KEY_GENRE, genre)
                .putString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI, iconUrl)
                .putString(MediaMetadata.METADATA_KEY_TITLE, title)
                .putLong(MediaMetadata.METADATA_KEY_TRACK_NUMBER, trackNumber)
                .putLong(MediaMetadata.METADATA_KEY_NUM_TRACKS, totalTrackCount)
                .build();
    }

    /**
     * Download a JSON file from a server, parse the content and return the JSON
     * object.
     *
     * @return result JSONObject containing the parsed representation.
     */
    private JSONArray fetchJSONFromUrl(String urlString) {
        BufferedReader reader = null;
        try {
            URLConnection urlConnection = new URL(urlString).openConnection();
            reader = new BufferedReader(new InputStreamReader(
                    urlConnection.getInputStream(), "iso-8859-1"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            LogHelper.e(TAG, "---sucess----",sb.toString());

            return new JSONArray(sb.toString());
        } catch (Exception e) {
            LogHelper.e(TAG, "Failed to parse the json for media list", e);
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }



    retrofit.Response<List<GitResult>> run(String strurl)  {
       final String strResponse="";

       try

       {



           OkHttpClient okClient = new OkHttpClient();
           okClient.interceptors().add(new Interceptor() {
               @Override
               public Response intercept(Chain chain) throws IOException {
                   Response response = chain.proceed(chain.request());
                   Log.v("====Response===",response.toString());

                   return response;
               }
           });

           //tracks.json?client_id=4f0b007dd6be94f1098f30bcd1e1a809&q=chicago&limit=50

           Retrofit client = new Retrofit.Builder()
                   .baseUrl(CATALOG_URL)
                   .client(okClient)
                   .addConverterFactory(GsonConverterFactory.create())
                   .build();

           // http://api.soundcloud.com/tracks.json?client_id=4f0b007dd6be94f1098f30bcd1e1a809&q=chicago&limit=50

           GitApiInterface service = client.create(GitApiInterface.class);

           Log.e("=====Search keyword====","------BaseActivity.SEARCH_KEYWORD-------"+BaseActivity.SEARCH_KEYWORD);
           Call<List<GitResult>> call = service.getUsersNamedTom("4f0b007dd6be94f1098f30bcd1e1a809",""+ BaseActivity.SEARCH_KEYWORD,"195");


           call.enqueue(new retrofit.Callback<List<GitResult>>() {


               @Override
               public void onResponse(retrofit.Response<List<GitResult>> response) {
                   if (response.isSuccess()) {
                       // request successful (status code 200, 201)
                       mainResponse = response;
                       Log.e("=====onResponse====","------Sucess--1--"+response.body());

                       //GitResult result = response.body();
                       Log.e("=====onResponse====","------Sucess2--"+response.body().size());
                       for(int i=0; i < response.body().size(); i++)
                       {
                           Log.e("=====onResponse====","------title---"+response.body().get(i).getTitle());
                       }


                    //   responseRetriveMedia = response;
                    //String ss=   new Gson().toJson(result).toString();

                   }
                   else
                   {
                       //request not successful (like 400,401,403 etc)
                       //Handle errors
                       Log.e("=====onResponse====","------Fail-Else--->"+response.body());
                   }

                   retrieveMedia();

               }

               @Override
               public void onFailure(Throwable t) {
                Log.e("=====onFailure====","------Fail--");
               }
           });
           return mainResponse;
       }
       catch (Exception e)
       {
           e.printStackTrace();
           return null;
       }
    }



    public interface GitApiInterface {

        @GET("/tracks.json")
        Call<List<GitResult>> getUsersNamedTom(@Query("client_id") String client_id,@Query("q") String searchKey,@Query("limit") String strLimit);

        /*@POST("/user/create")
        Call<Item> createUser(@Body String name, @Body String email);

        @PUT("/user/{id}/update")
        Call<Item> updateUser(@Path("id") String id , @Body Item user);*/
    }

    public class GitResult
    {
            @SerializedName("title")
            String title;

        @SerializedName("permalink")
        String permalink;

        @SerializedName("genre")
        String genre;

        @SerializedName("stream_url")
        String stream_url;

        @SerializedName("artwork_url")
        String artwork_url;

        @SerializedName("id")
        String id;

        @SerializedName("likes_count")
        String likes_count;


        @SerializedName("duration")
        String duration;

        public void setPermalink(String permalink) {
            this.permalink = permalink;
        }

        public String getPermalink() {
            return permalink;
        }

        public void setGenre(String genre) {
            this.genre = genre;
        }

        public String getGenre() {
            return genre;
        }

        public void setStream_url(String stream_url) {
            this.stream_url = stream_url;
        }

        public String getStream_url() {
            return stream_url;
        }

        public void setArtwork_url(String artwork_url) {
            this.artwork_url = artwork_url;
        }

        public String getArtwork_url() {
            return artwork_url;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public void setLikes_count(String likes_count) {
            this.likes_count = likes_count;
        }

        public String getLikes_count() {
            return likes_count;
        }

        public void setDuration(String duration) {
            this.duration = duration;
        }

        public String getDuration() {
            return duration;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }
    }
}
