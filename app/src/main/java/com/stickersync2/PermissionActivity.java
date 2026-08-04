package com.stickersync2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int MANAGE_EXTERNAL_STORAGE_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.permission_layout);

        Button btnStart = findViewById(R.id.btnStart);
        btnStart.setOnClickListener(v -> checkAndRequestPermissions());
    }

    private void checkAndRequestPermissions() {
        // Check if we have the required permissions
        boolean hasRead = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        boolean hasWrite = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                requestManageExternalStoragePermission();
                return;
            }
        }

        if (hasRead && hasWrite) {
            startMainActivity();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        }
    }

    private void requestManageExternalStoragePermission() {
        try {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.addCategory("android.intent.category.DEFAULT");
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, MANAGE_EXTERNAL_STORAGE_REQUEST_CODE);
        } catch (Exception e) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            startActivityForResult(intent, MANAGE_EXTERNAL_STORAGE_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MANAGE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    startMainActivity();
                } else {
                    Toast.makeText(this, "Permission required to access all files", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startMainActivity();
            } else {
                Toast.makeText(this, "Storage permissions are required", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}