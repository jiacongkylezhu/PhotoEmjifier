package com.kylezhudev.photoemojifier;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BitmapUtils {

    private static final String FILE_PROVIDER_AUTHORITY = "com.kylezhudev.fileprovider";

    public static Bitmap resamplePic(Context context, String imgPath){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);

        int targetHeight = displayMetrics.heightPixels;
        int targetWidth = displayMetrics.widthPixels;

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imgPath, bitmapOptions);
        int photoHeight = bitmapOptions.outHeight;
        int photoWidth = bitmapOptions.outWidth;

        int scaleFactor = Math.min(photoWidth / targetWidth, photoHeight / targetHeight);

        bitmapOptions.inJustDecodeBounds = false;
        bitmapOptions.inSampleSize = scaleFactor;

        return BitmapFactory.decodeFile(imgPath);
    }

    public static File createTempImageFile(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" +timeStamp + "_";
        File storageDir = context.getExternalCacheDir();

        return File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
    }

    public static boolean deleteImageFile(Context context, String imagePath){
        File imageFile = new File(imagePath);

        boolean deleted = imageFile.delete();

        if(!deleted){
            String errorMessage = context.getString(R.string.file_error);
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();

        }

        return deleted;
    }

    private static void galleryAddPic(Context context, String imagePath){
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File imageFile = new File(imagePath);
        Uri contentUri = Uri.fromFile(imageFile);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    static String saveImage(Context context, Bitmap image){
        String savedImagePath = null;

        String timeStamp = new SimpleDateFormat("yyyyMMDD_HHmmss",
                Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";
        File storageDir = new File(
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES) + "/Emojify");
        boolean success = true;
        if(!storageDir.exists()){
            success = storageDir.mkdirs();
        }

        if(success){
            File imageFile = new File(storageDir, imageFileName);
            savedImagePath = imageFile.getAbsolutePath();
            try{
                OutputStream fileOut = new FileOutputStream(imageFile);
                image.compress(Bitmap.CompressFormat.JPEG, 100, fileOut);
                fileOut.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            galleryAddPic(context, savedImagePath);

            String savedMessage = context.getString(R.string.saved_message, savedImagePath);
            Toast.makeText(context, savedMessage, Toast.LENGTH_SHORT).show();
        }

        return savedImagePath;
    }

    public static void shareImage(Context context, String imagePath){
        File imageFile = new File(imagePath);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        Uri photoUri = FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, imageFile);
        shareIntent.putExtra(Intent.EXTRA_STREAM, photoUri);
        context.startActivity(shareIntent);
    }

}
