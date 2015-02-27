package de.nnscr.attendance;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AsyncMessage extends AsyncTask<String, String, String> {
    protected MessageCallback callback;

    public AsyncMessage(MessageCallback callback) {
        this.callback = callback;
    }

    @Override
    protected String doInBackground(String... uri) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;
        String responseString = null;
        try {
            if (uri.length > 1) {
                HttpPost res = new HttpPost(uri[0]);

                List<NameValuePair> listPayload = new ArrayList<NameValuePair>();
                listPayload.add(new BasicNameValuePair("payload", uri[1]));

                res.setEntity(new UrlEncodedFormEntity(listPayload));

                response = httpclient.execute(res);
            } else {
                HttpGet res = new HttpGet(uri[0]);

                response = httpclient.execute(res);
            }

            StatusLine statusLine = response.getStatusLine();
            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                responseString = out.toString();
                out.close();
            } else{
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (IOException e) {
            //TODO Handle problems..
            return "EXCEPTION! " + e.getMessage();
        }

        return responseString;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        try {
            this.callback.onResult(parseJSON(s));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected JSONObject parseJSON(String payload) {
        try {
            return new JSONObject(payload);
        } catch (JSONException e) {
            return null;
        }
    }
}
