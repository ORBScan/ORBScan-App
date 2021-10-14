package com.example.orbscantemplate;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CaptureImage extends AppCompatActivity {

    public static final int CAMERA_REQUEST_CODE = 102;
    public static final int GALLERY_REQUEST_CODE = 105;
    ImageView captureDisplayImg;
    ImageButton cameraBtn, galleryBtn;
    Button imgToPDFBtn;
    String currentPhotoPath;
    StorageReference storageReference;

    public static final int CAMERA_PERM_CODE = 101;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0,0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_image);

        captureDisplayImg = findViewById(R.id.imageDisplay);
        cameraBtn = findViewById(R.id.cameraButton);
        galleryBtn = findViewById(R.id.galleryButton);
        imgToPDFBtn = findViewById(R.id.convertToPDFButton);
        storageReference = FirebaseStorage.getInstance().getReference();

        //captureDisplayImg.setVisibility(View.INVISIBLE);

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askCameraPermissions();
            }
        });

        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(gallery, GALLERY_REQUEST_CODE);
            }
        });
    }

    private void askCameraPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        }
        else {
            dispatchTakePictureIntent();
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_PERM_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                dispatchTakePictureIntent();
            }
            else {
                Toast.makeText(getApplicationContext(), "Camera permission is required.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CAMERA_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){
                File f = new File(currentPhotoPath);
                captureDisplayImg.setImageURI(Uri.fromFile(f));
                Log.d("tag","Absolute URL of image is: " + Uri.fromFile(f));

                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(f);
                mediaScanIntent.setData(contentUri);
                this.sendBroadcast(mediaScanIntent);

                imgToPDFBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                boolean converted = imageToPDF(f.getName(), null);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!converted){
                                            Toast.makeText(getApplicationContext(), "Fail", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        });
                        thread.start();
                    }
                });

                uploadImageToFirebase(f.getName(), contentUri);
            }
        }

        if (requestCode == GALLERY_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){
                Uri contentUri = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME};
                Cursor cursor = getContentResolver().query(contentUri, filePathColumn,
                        null, null);
                cursor.moveToFirst();
                int filePath = cursor.getColumnIndex(filePathColumn[0]);
                int fileName = cursor.getColumnIndex(filePathColumn[1]);
                String path = cursor.getString(filePath);
                String name = cursor.getString(fileName);
                cursor.close();
                //captureDisplayImg.setImageURI(Uri.parse(path));


                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "ORBScan_" + timeStamp + "." + getFileExt(contentUri);
                Log.d("tag","onActivityResult Gallery Image URI: " + name);
                Log.d("tag","Gallery Image URI: " + path);
                captureDisplayImg.setImageURI(contentUri);

                //Toast.makeText(getApplicationContext(), "File name: "+contentUri, Toast.LENGTH_LONG).show();

                imgToPDFBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                boolean converted = imageToPDF(path, name);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!converted){
                                            Toast.makeText(getApplicationContext(), "Fail", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        });
                        thread.start();
                    }
                });

                uploadImageToFirebase(imageFileName, contentUri);
            }
        }
    }

    private boolean imageToPDF(String path, String name){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Bitmap bitmap = BitmapFactory.decodeFile(String.valueOf(path));
            PdfDocument pdfDocument = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(
                    1920, 1080, 0).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
            page.getCanvas().drawBitmap(bitmap, 0, 0, null);
            pdfDocument.finishPage(page);
            /*String changeName = getWithoutExtension(fileName);
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            String newPath = storageDir + "/" + changeName + ".pdf";
            File file = new File(newPath);
            try {
                pdfDocument.writeTo(new FileOutputStream(file));
                MediaScannerConnection.scanFile(getApplicationContext(),
                        new String[]{file.toString()}, null, null);
            } catch (Exception e) {
                e.getStackTrace();
            }*/
            pdfDocument.close();
            return false;
        }
        else {
            return false;
        }
    }

    private String getWithoutExtension(String name) {
        return name.substring(0, name.lastIndexOf("."));
    }

    private void uploadImageToFirebase(String imageFileName, Uri contentUri) {
        final StorageReference image = storageReference.child("pictures/" + imageFileName);
        image.putFile(contentUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                image.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.d("tag", "onSuccess: Upload image URL is " + uri.toString());
                    }
                });
                Toast.makeText(CaptureImage.this, "Upload successfully", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CaptureImage.this, "Upload failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getFileExt(Uri contentUri) {
        ContentResolver c =getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(c.getType(contentUri));
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "ORBScan_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        //Toast.makeText(this, "Camera pressed", Toast.LENGTH_SHORT).show();
        File storageDir2 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File...
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }
}