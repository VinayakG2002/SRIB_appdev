package com.example.imageclick;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE=1;

    private Button mCaptureBtn;
    private GridView mImageGrid;

    private ImageAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCaptureBtn=findViewById(R.id.capture_btn);
        mImageGrid= findViewById(R.id.image_grid);

        mAdapter = new ImageAdapter(this);

        mImageGrid.setAdapter(mAdapter);
        mCaptureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });
    }

    private void dispatchTakePictureIntent(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(takePictureIntent,REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode== REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            String imageUri = saveImageToSharedPreferences(imageBitmap);

            mAdapter.addImage(imageUri);
            mAdapter.notifyDataSetChanged();
        }
    }

    private String saveImageToSharedPreferences(Bitmap imageBitmap){
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        int nextImageIndex = sharedPreferences.getInt("nextImageIndex",0);
        String imageUri = "image_"  + nextImageIndex+ ".png";
        try{
            FileOutputStream fos = openFileOutput(imageUri, Context.MODE_PRIVATE);
            imageBitmap.compress(Bitmap.CompressFormat.PNG,100,fos);
            fos.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("nextImageIndex",nextImageIndex+1);
        editor.apply();

        return imageUri;
    }



    private static class ImageAdapter extends BaseAdapter{

        private final Context mContext;
        private final List<String> mImageUris;

        private ImageAdapter(Context mContext) {
            this.mContext = mContext;
            mImageUris = new ArrayList<>();

            File[] files = mContext.getFilesDir().listFiles();
            if(files != null){
                for(File file: files){
                    if(file.isFile() && file.getName().startsWith("image_")){
                        mImageUris.add(file.getName());
                    }
                }
            }
        }

        public void addImage(String imageUri){
            mImageUris.add(imageUri);
        }

        @Override
        public int getCount() {
            return mImageUris.size();
        }

        @Override
        public Object getItem(int position) {
            return mImageUris.get(position);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;

            if(convertView== null){
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(250,250));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }else{
                imageView= (ImageView) convertView;
            }

            String imageUri= mImageUris.get(position);
            try{
                FileInputStream fis = mContext.openFileInput(imageUri);
                Bitmap bitmap= BitmapFactory.decodeStream(fis);
                imageView.setImageBitmap(bitmap);
                fis.close();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return imageView;
        }
    }
}