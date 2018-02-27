package com.kylezhudev.photoemojifier;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_STORAGE_PREMISSION = 1;
    private static final String FILE_PROVIDER_AUTHORITY = "com.kylezhudev.fileprovider";
    private static final String KEY_TEMP_PIC_PATH = "temporary-pic-path";

    @BindView(R.id.iv_image)
    ImageView mImageView;
    @BindView(R.id.btn_emojify)
    Button mEmojifyButton;
    @BindView(R.id.fab_save)
    FloatingActionButton mSaveFab;
    @BindView(R.id.fab_share)
    FloatingActionButton mShareFab;
    @BindView(R.id.fab_clear)
    FloatingActionButton mClearFab;


    private String mTempPhotoPath;
    private Bitmap mResultsBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Timber.plant(new Timber.DebugTree());

    }

    @OnClick(R.id.btn_emojify)
    public void emojifyMe(View view) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PREMISSION);
        } else {
            launchCamera();
        }
    }

    private void launchCamera() {

        Intent takePicIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePicIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = BitmapUtils.createTempImageFile(this);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (photoFile != null) {

                mTempPhotoPath = photoFile.getAbsolutePath();

                Uri photoUri = FileProvider.getUriForFile(this,
                        FILE_PROVIDER_AUTHORITY, photoFile);

                takePicIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);

                startActivityForResult(takePicIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Timber.d("Result Code: " + resultCode);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            processAndSetImage();
        } else {
            BitmapUtils.deleteImageFile(this, mTempPhotoPath);

        }
    }

    private void processAndSetImage() {
        mEmojifyButton.setVisibility(View.GONE);
        mSaveFab.setVisibility(View.VISIBLE);
        mShareFab.setVisibility(View.VISIBLE);
        mClearFab.setVisibility(View.VISIBLE);
        mResultsBitmap = BitmapUtils.resamplePic(this, mTempPhotoPath);


        mResultsBitmap = Emojifier.detectFacesAndOverlayEmoji(this, mResultsBitmap);

        if (mResultsBitmap != null) {
            mImageView.setImageBitmap(mResultsBitmap);
        } else {
            showEmojifyBtn();
            Timber.d("mResultBitmap is null");

        }


    }

    public void saveMe(View view) {
        BitmapUtils.deleteImageFile(this, mTempPhotoPath);
        BitmapUtils.saveImage(this, mResultsBitmap);
    }

    public void shareMe(View view) {
        BitmapUtils.deleteImageFile(this, mTempPhotoPath);
        BitmapUtils.saveImage(this, mResultsBitmap);
        BitmapUtils.shareImage(this, mTempPhotoPath);
    }

    public void clearImage(View view) {
        showEmojifyBtn();

        BitmapUtils.deleteImageFile(this, mTempPhotoPath);

    }

    private void showEmojifyBtn() {
        mImageView.setImageResource(0);
        mEmojifyButton.setVisibility(View.VISIBLE);
        mSaveFab.setVisibility(View.GONE);
        mShareFab.setVisibility(View.GONE);
        mClearFab.setVisibility(View.GONE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_STORAGE_PREMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    launchCamera();
                } else {
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();

                }
                break;
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(KEY_TEMP_PIC_PATH, mTempPhotoPath);
        super.onSaveInstanceState(outState);


    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(KEY_TEMP_PIC_PATH)) {
            mTempPhotoPath = savedInstanceState.getString(KEY_TEMP_PIC_PATH);
            Timber.d("Restore tempPhotoPath:" + mTempPhotoPath);


        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mTempPhotoPath != null) {
            mResultsBitmap = BitmapUtils.resamplePic(this, mTempPhotoPath);
            processAndSetImage();
        }

    }
}
