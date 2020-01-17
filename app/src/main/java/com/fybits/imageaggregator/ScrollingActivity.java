package com.fybits.imageaggregator;

import android.app.Application;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;

import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.JsonReader;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;


public class ScrollingActivity extends AppCompatActivity {
// SECRET 56622572cd99f75b9bd13ae67be4f69c275a0f4a


    public Uri getImageUri(Context context, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "splahImage", null);
        return Uri.parse(path);
    }



    private GridView.OnItemClickListener gridviewOnItemClickListener = new GridView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            Toast.makeText(getApplicationContext(), position+"", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(),  Splash.class);

            Bitmap image = (Bitmap)parent.getAdapter().getItem(position);
            intent.putExtra("imageUri", getImageUri(getApplicationContext(), image).toString());
            startActivity(intent);
        }
    };

    public Bitmap LoadImage(String url) {
        Bitmap bm = null;
        InputStream is = null;
        BufferedInputStream bis = null;
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestProperty("Authorization", "Client-ID 26c696940e834e3");

            is = conn.getInputStream();
            bis = new BufferedInputStream(is, 8192);

            conn.connect();

            bm = BitmapFactory.decodeStream(bis);

            is.close();
            bis.close();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bm;
    }


    public void LoadImages() {
        final ProgressBar spinner = (ProgressBar)findViewById(R.id.pb_spinner);
        spinner.setVisibility(View.VISIBLE);
        final Handler handler = new Handler();
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Looper.prepare();
                    String url = "https://api.imgur.com/3/gallery/hot/top/day/1?showViral=true&mature=true&album_previews=false";
                    //String url =  "https://api.imgur.com/3/gallery/album/sYQ5QfN";
                    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                    conn.setRequestProperty("Authorization", "Client-ID 26c696940e834e3");

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    JSONObject obj = new JSONObject(reader.readLine());
                    JSONArray data = obj.getJSONArray("data");
                    final ArrayList<Bitmap> imgs = new ArrayList<>();

                    Toast.makeText(getBaseContext(), data.length()+"", Toast.LENGTH_SHORT).show();
                    for (int j = 0; j < data.length(); j++) {
                        if (data.getJSONObject(j).getBoolean("is_album")) {
                            final JSONArray images = data.getJSONObject(j).getJSONArray("images");

                            for (int i = 0; i < images.length(); i++) {
                                if (images.getJSONObject(i).getString("type").equals("image/png")) {
                                    String link = images.getJSONObject(i).getString("link");
                                    imgs.add(LoadImage(link));
                                }
                            }
                        } else {
                            if (data.getJSONObject(j).getString("type").equals("image/png")) {
                                String link = data.getJSONObject(j).getString("link");
                                imgs.add(LoadImage(link));
                            }
                        }
                    }
                    spinner.setVisibility(View.INVISIBLE);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getBaseContext(), imgs.size()+"", Toast.LENGTH_SHORT).show();
                            MyGridView grid = (MyGridView)findViewById(R.id.grid);
                            grid.setOnItemClickListener(gridviewOnItemClickListener);
                            Bitmap[] array = new Bitmap[imgs.size()];
                            imgs.toArray(array);
                            grid.setAdapter(new ImageAdapter(getApplicationContext(), array));
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                grid.setNestedScrollingEnabled(false);
                            }
                        }
                    });


                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
        thread.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        ProgressBar spinner = (ProgressBar)findViewById(R.id.pb_spinner);
        spinner.setVisibility(View.INVISIBLE);

        LoadImages();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Updating", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                LoadImages();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
