package com.gpp.gpp.home;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gpp.gpp.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;

import static java.lang.Long.valueOf;

public class picratings extends AppCompatActivity {

    private TextView tv;
    ProgressDialog progressDialog;
    RatingBar rb1,rb2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picratings);



        tv = (TextView)findViewById(R.id.tv);
        rb1 =(RatingBar)findViewById(R.id.drywet);
        rb2 =(RatingBar)findViewById(R.id.plastic);
        //String [] image_name_A= getIntent().getStringArrayExtra("image_name");
        //String image_name=image_name_A[0];
        //String location=image_name_A[1];
        String image_name=getIntent().getStringExtra("image");
        String location=getIntent().getStringExtra("loc");

        Toast.makeText(this, image_name+" "+location, Toast.LENGTH_SHORT).show();
        new Ronak_Api().execute("http://192.168.137.1:5000/analyse?location="+location+"&image="+image_name);
      /*  new Ronak_Api().execute("http://192.168.137.1:5000/analyse?location="+location+"&image=1");*/

    }

    class Ronak_Api extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(picratings.this);
            progressDialog.setMessage("Please Wait Analyzing the Image");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... urls) {
            HttpURLConnection connection=null;

            BufferedReader reader=null;

            try {
                URL url = new URL(urls[0]);
                connection= (HttpURLConnection)url.openConnection();
                connection.connect();


                Authenticator.setDefault(new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication("ron", "pycharm".toCharArray());
                    }
                });


                InputStream stream= connection.getInputStream();
                reader=new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer= new StringBuffer();
                String line="";
                while((line=reader.readLine())!=null){
                    buffer.append(line);
                }
                return buffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                if (connection!=null){
                    connection.disconnect();
                }

                try {
                    if (reader!=null){
                        reader.close();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            String rating_d=null;
            String rating_p=null;
            try {
                JSONObject f_rsult= new JSONObject(result);
                rating_d=f_rsult.getString("ratings dry wet");
                rating_p=f_rsult.getString("ratings plastic");
            } catch (JSONException e) {
                e.printStackTrace();
            }
           //* tv.setText("Plastic: "+rating_d+"\nDry Wet "+rating_p);*//*
            rb1.setRating(valueOf(rating_d));
            rb2.setRating(valueOf(rating_p));
        }
    }
}