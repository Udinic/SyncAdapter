package com.udinic.sync_adapter_example_app.syncadapter;

import android.util.Log;

import com.google.gson.Gson;
import com.udinic.sync_adapter_example.authentication.ParseComServer;
import com.udinic.sync_adapter_example_app.db.dao.TvShow;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import static com.udinic.sync_adapter_example.authentication.ParseComServer.getAppParseComHeaders;

/**
 * This class is intended to encapsulate all the actions against Parse.com.
 *
 * Created by Udini on 7/6/13.
 */
public class ParseComServerAccessor {
    public List<TvShow> getShows(String auth) throws Exception {

        Log.d("udini", "getShows auth[" + auth + "]");

        DefaultHttpClient httpClient = new DefaultHttpClient();
        String url = "https://api.parse.com/1/classes/tvshows";

        HttpGet httpGet = new HttpGet(url);
        for (Header header : getAppParseComHeaders()) {
            httpGet.addHeader(header);
        }
        httpGet.addHeader("X-Parse-Session-Token", auth); // taken from https://parse.com/questions/how-long-before-the-sessiontoken-expires

        try {
            HttpResponse response = httpClient.execute(httpGet);

            String responseString = EntityUtils.toString(response.getEntity());
            Log.d("udini", "getShows> Response= " + responseString);

            if (response.getStatusLine().getStatusCode() != HttpURLConnection.HTTP_OK) {
                ParseComServer.ParseComError error = new Gson().fromJson(responseString, ParseComServer.ParseComError.class);
                throw new Exception("Error retrieving tv shows ["+error.code+"] - " + error.error);
            }

            TvShows shows = new Gson().fromJson(responseString, TvShows.class);
            return shows.results;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ArrayList<TvShow>();
    }

    public void putShow(String authtoken, String userId, TvShow showToAdd) throws Exception {

        Log.d("udinic", "putShow ["+showToAdd.name+"]");

        DefaultHttpClient httpClient = new DefaultHttpClient();
        String url = "https://api.parse.com/1/classes/tvshows";

        HttpPost httpPost = new HttpPost(url);

        for (Header header : getAppParseComHeaders()) {
            httpPost.addHeader(header);
        }
        httpPost.addHeader("X-Parse-Session-Token", authtoken); // taken from https://parse.com/questions/how-long-before-the-sessiontoken-expires
        httpPost.addHeader("Content-Type", "application/json");

        JSONObject tvShow = new JSONObject();
        tvShow.put("name", showToAdd.name);
        tvShow.put("year", showToAdd.year);

        // Creating ACL JSON object for the current user
        JSONObject acl = new JSONObject();
        JSONObject aclEveryone = new JSONObject();
        JSONObject aclMe = new JSONObject();
        aclMe.put("read", true);
        aclMe.put("write", true);
        acl.put(userId, aclMe);
        acl.put("*", aclEveryone);
        tvShow.put("ACL", acl);

        String request = tvShow.toString();
        Log.d("udinic", "Request = " + request);
        httpPost.setEntity(new StringEntity(request,"UTF-8"));

        try {
            HttpResponse response = httpClient.execute(httpPost);
            String responseString = EntityUtils.toString(response.getEntity());
            if (response.getStatusLine().getStatusCode() != 201) {
                ParseComServer.ParseComError error = new Gson().fromJson(responseString, ParseComServer.ParseComError.class);
                throw new Exception("Error posting tv shows ["+error.code+"] - " + error.error);
            } else {
//                Log.d("udini", "Response string = " + responseString);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class TvShows implements Serializable {
        List<TvShow> results;
    }

}
