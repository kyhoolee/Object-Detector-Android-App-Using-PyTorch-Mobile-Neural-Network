package com.tckmpsi.objectdetectordemo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {
    private static int RESULT_LOAD_IMAGE = 1;
    private static int REQUEST_IMAGE_CAPTURE = 2;

    private static final String TAG = "Resnet101";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonLoadImage = (Button) findViewById(R.id.button);
        Button detectButton = (Button) findViewById(R.id.detect);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        buttonLoadImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                TextView textView = findViewById(R.id.result_text);
                textView.setText("");


//                Intent i = new Intent(
////                        Intent.ACTION_PICK,
////                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
////
////                startActivityForResult(i, RESULT_LOAD_IMAGE);

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }

            }
        });

        detectButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                Bitmap bitmap = null;
                Module module = null;

                //Getting the image from the image view
                ImageView imageView = (ImageView) findViewById(R.id.image);

                try {
                    //Read the image as Bitmap
                    bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();

                    //Here we reshape the image into 400*400
                    bitmap = Bitmap.createScaledBitmap(bitmap, 400, 400, true);

                    Runtime runtime = Runtime.getRuntime();

                    long before = runtime.totalMemory() - runtime.freeMemory();

                    ActivityManager.MemoryInfo b = getAvailableMemory();
                    Log.i(TAG, "Before-mem:: " + b.availMem + " -- " + b.totalMem);
                    //Loading the model file.
                    ///home/kylee/work/projects/8_tf_optimize/2_pytorch_tutorial/finetuned_quantized_model.pt
                    String f = fetchModelFile(MainActivity.this, "mobilenet_v2_traced.pt");
                    Log.i(TAG, "File:: " + f);
                    module = Module.load(f);

                    long after = runtime.totalMemory() - runtime.freeMemory();
                    Log.i(TAG, "test memory: " + (after - before));

                    ActivityManager.MemoryInfo a = getAvailableMemory();
                    Log.i(TAG, "After-mem:: " + a.availMem + " -- " + a.totalMem);
                    Log.i(TAG, "Diff-mem:: " + (b.availMem - a.availMem));

                    // Resnet-152
                    // Diff-mem:: 249278464
                    // Diff-mem:: 205574144
                } catch (IOException e) {
                    finish();
                }

                //Input Tensor
                float[] TORCHVISION_NORM_MEAN_RGB = new float[]{0.485f, 0.456f, 0.406f};
                float[] TORCHVISION_NORM_STD_RGB = new float[]{0.229f, 0.224f, 0.225f};

                //(0.4914, 0.4822, 0.4465), (0.247, 0.243, 0.261)
                final Tensor input = TensorImageUtils.bitmapToFloat32Tensor(
                        bitmap,
                        TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
                        TensorImageUtils.TORCHVISION_NORM_STD_RGB
                );

                //Calling the forward of the model to run our input
                long startTime =   System.currentTimeMillis();
                final Tensor output = module.forward(IValue.from(input)).toTensor();
                long processed = System.currentTimeMillis() - startTime;
                Log.i(TAG, "Done: " + processed);


                final float[] score_arr = output.getDataAsFloatArray();

                // Fetch the index of the value with maximum score
                float max_score = -Float.MAX_VALUE;
                int ms_ix = -1;
                for (int i = 0; i < score_arr.length; i++) {
                    if (score_arr[i] > max_score) {
                        max_score = score_arr[i];
                        ms_ix = i;
                    }
                }

                Log.i(TAG, "RESULT:: " + ms_ix);

                //Fetching the name from the list based on the index
                String detected_class = ModelClasses.MODEL_CLASSES[ms_ix];//ModelClasses.MODEL_CLASSES[ms_ix];

                //Writing the detected class in to the text view of the layout
                TextView textView = findViewById(R.id.result_text);
                textView.setText(detected_class);


            }
        });

    }


    // Get a MemoryInfo object for the device's current memory status.
    private ActivityManager.MemoryInfo getAvailableMemory() {
        ActivityManager activityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        return memoryInfo;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //This functions return the selected image from gallery
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ImageView imageView = (ImageView) findViewById(R.id.image);
            imageView.setImageBitmap(imageBitmap);
        }


        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            ImageView imageView = (ImageView) findViewById(R.id.image);
            imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));

            //Setting the URI so we can read the Bitmap from the image
            imageView.setImageURI(null);
            imageView.setImageURI(selectedImage);


        }


    }

    public static String fetchModelFile(Context context, String modelName) throws IOException {
        File file = new File(context.getFilesDir(), modelName);
        if (file.exists() && file.length() > 0) {
            Log.i(TAG, "Done read file:: " + file.getAbsolutePath());
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(modelName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }

}
