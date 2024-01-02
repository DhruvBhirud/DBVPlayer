package com.db.dbvplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSION = 123;
    BottomNavigationView bottomNav;
    static ArrayList<VideoFiles> videoFiles = new ArrayList<>();
    static ArrayList<String> folderList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNav = findViewById(R.id.bottomNavView);
        permission();
        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId())
                {
                    case R.id.folderList:
                        //Toast.makeText(MainActivity.this, "Folder", Toast.LENGTH_SHORT).show();
                        FragmentTransaction fragmentTransaction = getSupportFragmentManager()
                                .beginTransaction();
                        fragmentTransaction.replace(R.id.mainFreagment, new FolderFragment());
                        fragmentTransaction.commit();
                        item.setChecked(true);
                        break;
                    case R.id.filesList:
                        //Toast.makeText(MainActivity.this,"Files", Toast.LENGTH_SHORT).show();
                        FragmentTransaction fragmentTransaction2 = getSupportFragmentManager()
                                .beginTransaction();
                        fragmentTransaction2.replace(R.id.mainFreagment, new FilesFragment());
                        fragmentTransaction2.commit();
                        item.setChecked(true);
                        break;
                }
                return false;
            }
        });
    }

    private void permission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION);
        }
        else
        {
            //Toast.makeText(this,"Permission Granted", Toast.LENGTH_SHORT).show();
            videoFiles = getAllVideos(this);
            FragmentTransaction fragmentTransaction = getSupportFragmentManager()
                    .beginTransaction();
            fragmentTransaction.replace(R.id.mainFreagment, new FolderFragment());
            fragmentTransaction.commit();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                //Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                videoFiles = getAllVideos(this);
                FragmentTransaction fragmentTransaction = getSupportFragmentManager()
                        .beginTransaction();
                fragmentTransaction.replace(R.id.mainFreagment, new FolderFragment());
                fragmentTransaction.commit();
            }
            else
            {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION);
            }
        }
    }

    public ArrayList<VideoFiles> getAllVideos(Context context)
    {
        ArrayList<VideoFiles> tempVideoFiles = new ArrayList<>();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.DISPLAY_NAME
        };
        Cursor cursor = context.getContentResolver().query(uri, projection,
                null,null,null);
        if (cursor != null)
        {
            while (cursor.moveToNext())
            {
                String id = cursor.getString(0);
                String path = cursor.getString(1);
                String title = cursor.getString(2);
                String size = cursor.getString(3);
                String dateAdded = cursor.getString(4);
                String duration = cursor.getString(5);
                String fileName = cursor.getString(6);
                VideoFiles videoFiles = new VideoFiles(id, path, title, fileName, size,
                        dateAdded, duration);
                Log.e("path", path);
                int slashFirstIndex = path.lastIndexOf("/");
                String subString = path.substring(0, slashFirstIndex);
                if (!folderList.contains(subString)) {
                    folderList.add(subString);
                }
                    tempVideoFiles.add(videoFiles);
                }
            cursor.close();
        }
        return tempVideoFiles;
    }
}