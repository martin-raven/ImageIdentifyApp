package com.essentials.martin.whatisit;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class StartingActivity extends AppCompatActivity {

    static final int REQUEST_GET_PHOTO = 2;
    static final int REQUEST_TAKE_PHOTO = 1;
    ImageView img_Back;
    Uri photoURI;
    Bitmap bm;
    Uri path;
    String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starting);
        img_Back= findViewById(R.id.Img_background);
        Button btn_camera=findViewById(R.id.btn_Camera);
        btn_camera.setOnClickListener(clickListener);
        Button btn_gallery=findViewById(R.id.btn_Gallery);
        btn_gallery.setOnClickListener(clickListener);
    }
    final View.OnClickListener clickListener = new View.OnClickListener() {
        public void onClick(final View v) {
            switch(v.getId()) {
                case R.id.btn_Camera:
                    //Inform the user the button1 has been clicked
                    Toast.makeText(getApplicationContext(), "Button1 clicked.", Toast.LENGTH_SHORT).show();
                    dispatchTakePictureIntent();
                    break;
                case R.id.btn_Gallery:
                    //Inform the user the button2 has been clicked
                    Toast.makeText(getApplicationContext(), "Button2 clicked.", Toast.LENGTH_SHORT).show();
                    galleryIntent();
                    break;
            }
        }
    };
    private File createImageFile() throws IOException {
        Log.d("Entered the ","createImageFile");
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
    private void dispatchTakePictureIntent() {
        Log.d("Entered the "," dispatchTakePictureIntent");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "com.essentials.martin.whatisit",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }
    private void galleryIntent()
    {
        Log.d("Entered the "," galleryIntent");
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"),REQUEST_GET_PHOTO);
    }
    private void onSelectFromGalleryResult(Intent data) {
        Log.d("Entered the "," FromGalleryResult");
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                Log.d("data uri ",data.getDataString());
                path=data.getData();
                img_Back.setImageBitmap(bm);
                uploadBitmap(bm);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    private void onTakePhotoResult(Intent data) {
        Log.d("Entered the "," takephotoresult"+String.valueOf(data));
        if (data == null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), photoURI);
                img_Back.setImageBitmap(bm);
                uploadBitmap(bm);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_GET_PHOTO)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_TAKE_PHOTO)
                onTakePhotoResult(data);
        }
    }
//    public class CloudCall extends AsyncTask<String,String,String> {
//
//
//        @Override
//        protected String doInBackground(String... params) {
////            ByteArrayOutputStream outStr = new ByteArrayOutputStream();
////            bm.compress(Bitmap.CompressFormat.JPEG, 100, outStr);
//            //String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
//            Log.d("IN C>CALL","Uploading file");
//            String response="";
//            try {
//                uploadBitmap(bm);
//                response = "";
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            Log.d("result is :",response);
//            return response;
//        }
//
//        @Override
//        protected void onPostExecute(String s) {
//            super.onPostExecute(s);
//            try {
//            }catch(Exception e){Log.d("Excep in Profile upda",String.valueOf(e));}
//        }
//    }
    public byte[] getFileDataFromDrawable(Bitmap bitmap) {
        Log.d("get data from bit","Uploading file");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        Log.d("get data from bit","Uploading file");
        return byteArrayOutputStream.toByteArray();
    }

    public void uploadBitmap(final Bitmap bitmap) {

        //getting the tag from the edittext
//        final String tags = editTextTags.getText().toString().trim();

        //our custom volley request
        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, "https://www.wolframcloud.com/objects/martinsiby/IdentifyImage",
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        try {
                            Log.d("IN UPLOAD","Uploading file");
                            JSONObject obj = new JSONObject(new String(response.data));
                            Log.d("Response",obj.toString());
                            Toast.makeText(getApplicationContext(), obj.getString("Result"), Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {

            /*
            * If you want to add more parameters with the image
            * you can do it here
            * here we have only one parameter with the image
            * which is tags
            * */
//            @Override
//            protected Map<String, String> getParams() throws AuthFailureError {
//                Map<String, String> params = new HashMap<>();
//                params.put("tags", tags);
//                return params;
//            }

            /*
            * Here we are passing image by renaming it with a unique name
            * */
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                long imagename = System.currentTimeMillis();
                params.put("image", new DataPart(imagename + ".png", getFileDataFromDrawable(bitmap)));
                return params;
            }
        };

        //adding the request to volley
        Volley.newRequestQueue(this).add(volleyMultipartRequest);
    }
}
