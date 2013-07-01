package com.udinic.sync_adapter_example.authentication;

import android.util.Log;

import com.google.gson.Gson;
import com.udinic.syncadapter_example_app.db.dao.TvShow;
import com.udinic.syncadapter_example_app.db.dao.User;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles the comminication with Parse.com
 *
 * User: udinic
 * Date: 3/27/13
 * Time: 3:30 AM
 */
public class ParseComServer implements ServerAuthenticate{

    private final static String APP_ID = "iRnc8I1X0du5q6HrJtZW0a5DlB0JcpOQbjA6chha";
    private final static String REST_API_KEY = "tv1xCdYKTwI3p205KHCn1yWpbVj2OHldV9cPZuNZ";

    @Override
    public User userSignUp(String name, String email, String pass, String authType) throws Exception {

        String url = "https://api.parse.com/1/users";

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);

        httpPost.addHeader("X-Parse-Application-Id",APP_ID);
        httpPost.addHeader("X-Parse-REST-API-Key", REST_API_KEY);
        httpPost.addHeader("Content-Type", "application/json");

        String user = "{\"username\":\"" + email + "\",\"password\":\"" + pass + "\",\"phone\":\"415-392-0202\"}";
        HttpEntity entity = new StringEntity(user);
        httpPost.setEntity(entity);

        try {
            HttpResponse response = httpClient.execute(httpPost);
            String responseString = EntityUtils.toString(response.getEntity());

            if (response.getStatusLine().getStatusCode() != 201) {
                ParseComError error = new Gson().fromJson(responseString, ParseComError.class);
                throw new Exception("Error creating user["+error.code+"] - " + error.error);
            }

            User createdUser = new Gson().fromJson(responseString, User.class);

            return createdUser;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public User userSignIn(String user, String pass, String authType) throws Exception {

        Log.d("udini", "userSignIn");

        DefaultHttpClient httpClient = new DefaultHttpClient();
        String url = "https://api.parse.com/1/login";

        String query = null;
        try {
            query = String.format("%s=%s&%s=%s", "username", URLEncoder.encode(user, "UTF-8"), "password", pass);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        url += "?" + query;

        HttpGet httpGet = new HttpGet(url);

        httpGet.addHeader("X-Parse-Application-Id", APP_ID);
        httpGet.addHeader("X-Parse-REST-API-Key", REST_API_KEY);

        HttpParams params = new BasicHttpParams();
        params.setParameter("username", user);
        params.setParameter("password", pass);
        httpGet.setParams(params);
//        httpGet.getParams().setParameter("username", user).setParameter("password", pass);

        try {
            HttpResponse response = httpClient.execute(httpGet);

            String responseString = EntityUtils.toString(response.getEntity());
            if (response.getStatusLine().getStatusCode() != 200) {
                ParseComError error = new Gson().fromJson(responseString, ParseComError.class);
                throw new Exception("Error signing-in ["+error.code+"] - " + error.error);
            }

            User loggedUser = new Gson().fromJson(responseString, User.class);
            return loggedUser;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<TvShow> getShows(String auth) throws Exception {

        Log.d("udini", "getShows auth["+auth+"]");

        DefaultHttpClient httpClient = new DefaultHttpClient();
        String url = "https://api.parse.com/1/classes/tvshows";

        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("X-Parse-Application-Id", APP_ID);
        httpGet.addHeader("X-Parse-REST-API-Key", REST_API_KEY);
        httpGet.addHeader("X-Parse-Session-Token", auth); // taken from https://parse.com/questions/how-long-before-the-sessiontoken-expires

        try {
            HttpResponse response = httpClient.execute(httpGet);

            String responseString = EntityUtils.toString(response.getEntity());
            Log.d("udini", "getShows> Response= " + responseString);

            if (response.getStatusLine().getStatusCode() != 200) {
                ParseComError error = new Gson().fromJson(responseString, ParseComError.class);
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
        httpPost.addHeader("X-Parse-Application-Id", APP_ID);
        httpPost.addHeader("X-Parse-REST-API-Key", REST_API_KEY);
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
                ParseComError error = new Gson().fromJson(responseString, ParseComError.class);
                throw new Exception("Error posting tv shows ["+error.code+"] - " + error.error);
            } else {
//                Log.d("udini", "Response string = " + responseString);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ParseComError implements Serializable {
        int code;
        String error;
    }

    private class TvShows implements Serializable {
        List<TvShow> results;
    }



//    public void putShows(User user, List<TvShow> showsToAdd) throws Exception {
//
//        Log.d("udini", "putShows");
//
//        DefaultHttpClient httpClient = new DefaultHttpClient();
//
//        HttpPost httpPost = new HttpPost("https://api.parse.com/1/batch");
//        httpPost.addHeader("X-Parse-Application-Id", APP_ID);
//        httpPost.addHeader("X-Parse-REST-API-Key", REST_API_KEY);
//        httpPost.addHeader("X-Parse-Session-Token", user.getSessionToken()); // taken from https://parse.com/questions/how-long-before-the-sessiontoken-expires
//        httpPost.addHeader("Content-Type", "application/json");
//
//        ParseComBatch batchOperation = new ParseComBatch();
//        batchOperation.requests = new ArrayList<ParseComBatchRequest>();
//        for (TvShow show : showsToAdd) {
////            show.ACL = userACL;
//            ParseComBatchRequest req = new ParseComBatchRequest();
//            req.method = HttpPost.METHOD_NAME;
//            req.path = "/1/classes/tvshows";
//            req.body = show;
//
//            batchOperation.requests.add(req);
//        }
//        String request = new Gson().toJson(batchOperation);
//        Log.d("udini", "Request = " + request);
//        httpPost.setEntity(new StringEntity(request,"UTF-8"));
//
//        try {
//            HttpResponse response = httpClient.execute(httpPost);
//            String responseString = EntityUtils.toString(response.getEntity());
//            if (response.getStatusLine().getStatusCode() != 200) {
//                ParseComError error = new Gson().fromJson(responseString, ParseComError.class);
//                throw new Exception("Error posting tv shows ["+error.code+"] - " + error.error);
//            } else {
//                Log.d("udini", "Response string = " + responseString);
//
//                List<String> successfulItems = new ArrayList<String>();
//                JSONArray array = new JSONArray(responseString);
//                for (int i=0; i<array.length(); i++) {
//                    try {
//                        JSONObject successObj = array.getJSONObject(i).getJSONObject("success");
//                        successfulItems.add(successObj.getString("objectId"));
//                    } catch (JSONException e) {
//                        Log.d("udini", "no success object [" + array.getJSONObject(i).toString() + "]. Skipping..");
//                    }
//                }
//
//                JSONObject acl = new JSONObject();
//                JSONObject aclEveryone = new JSONObject();
//                JSONObject aclMe = new JSONObject();
//                aclMe.put("read", true);
//                aclMe.put("write", true);
//                acl.put(user.getObjectId(), aclMe);
//                acl.put("*", aclEveryone);
//                JSONObject aclMain = new JSONObject();
//                aclMain.put("ACL", acl);
//
////                String userACL = "{'ACL':{'"+user.getObjectId()+"':{\"read\":true,\"write\":true},\"*\":{}}}";
//
//                HttpPost httpPostBatch = new HttpPost("https://api.parse.com/1/batch");
//                httpPostBatch.addHeader("X-Parse-Application-Id", APP_ID);
//                httpPostBatch.addHeader("X-Parse-REST-API-Key", REST_API_KEY);
//                httpPostBatch.addHeader("X-Parse-Session-Token", user.getSessionToken()); // taken from https://parse.com/questions/how-long-before-the-sessiontoken-expires
//                httpPostBatch.addHeader("Content-Type", "application/json");
//
//                JSONArray requests = new JSONArray();
//                for (String item : successfulItems) {
//                    JSONObject requestItem = new JSONObject();
//                    requestItem.put("method", HttpPut.METHOD_NAME);
//                    requestItem.put("path", "/1/classes/tvshows/"+ item);
//                    requestItem.put("body", aclMain);
//                    requests.put(requestItem);
//                }
//                JSONObject main = new JSONObject();
//                main.put("requests", requests);
//                String jsonToSend = main.toString().replace("\\/", "/").replace("\\\"","\"");
//                httpPostBatch.setEntity(new StringEntity(jsonToSend));
//                try {
//                    HttpResponse response2 = httpClient.execute(httpPostBatch);
//                    String responseString2 = EntityUtils.toString(response2.getEntity());
//                    if (response2.getStatusLine().getStatusCode() != 200) {
//                        ParseComError error = new Gson().fromJson(responseString2, ParseComError.class);
//                        throw new Exception("Error posting tv shows [" + error.code + "] - " + error.error);
//                    } else {
//                        Log.d("udini", "Response string = " + responseString2);
//                    }
//                } catch (Exception e) {
//                    Log.e("udini", "Exception", e);
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private class ParseComBatch implements Serializable {
//        List<ParseComBatchRequest> requests;
//    }
//
//    private class ParseComBatchRequest implements Serializable {
//        String method;
//        String path;
//        TvShow body;
//    }


}
