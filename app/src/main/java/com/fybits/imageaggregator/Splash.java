package com.fybits.imageaggregator;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.GridView;
import android.widget.ImageView;

import java.io.IOException;


public class Splash extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_image);

        int index = getIntent().getExtras().getInt("imageIndex");
        //GridView grid = (GridView)getParent().findViewById(R.id.grid);


        String imageUri = getIntent().getExtras().getString("imageUri");
        Bitmap image = null;
        try {
            image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(imageUri));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ImageView splash = (ImageView)findViewById(R.id.splashImage);

        splash.setImageBitmap(image);
    }
}
