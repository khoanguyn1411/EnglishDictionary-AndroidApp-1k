package com.duanthivien1k.tudientienganh;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.duanthivien1k.adapter.TuScanDuocAdapter;
import com.duanthivien1k.tudientienganh.databinding.ActivityChenHinhBinding;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;


public class ChenHinhActivity extends AppCompatActivity {
    ActivityChenHinhBinding binding;

    ListView lvTuScanDuoc;
    TuScanDuocAdapter tuScanDuocAdapter;

    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 400;
    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private static final int IMAGE_PICK_CAMERA_CODE = 1001;
    String cameraPermission[];
    String storagePermission[];

    Uri image_uri;
    DatabaseHelper myDatabaseHelper = new DatabaseHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChenHinhBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        addCameraPermission();

        addControlsLV();

        addControls();

        addEventsClickLV();

    }




    //region X??? l?? l???m h??nh t??? th?? vi???n + l???y h??nh t??? camera
    private void addControls() {
        binding.btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkCameraPermission()){
                    requestCameraPermission();
                }
                else {
                    //permission allowed, take pickture
                    pickCamera();
                }
            }
        });
        binding.btnGalery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // galery option clicked
                if(!checkStoragePermission()){
                    requestStoragePermission();
                }
                else {
                    //permission allowed, take pickture
                    pickGallery();
                }
            }
        });

    }

    private void pickGallery() {
        // intent ????? l???m ???nh t??? th?? vi???n h??nh
        Intent intent = new Intent(Intent.ACTION_PICK);
        // set intent type to image
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }

    private void pickCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"NewPic");// t??n c???a b???c h ??nh
        values.put(MediaStore.Images.Media.DESCRIPTION,"Image to text");// m?? t???
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(cameraIntent,IMAGE_PICK_CAMERA_CODE);
    }
    //endregion
    //region Ki???m tra permission c???a camera v?? storage
    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,storagePermission,STORAGE_REQUEST_CODE);

    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==(PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,cameraPermission,CAMERA_REQUEST_CODE);
    }
    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) ==(PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==(PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }
    //endregion
    //region Ki???m tra permission 1 l???n n???a ????? l???y h??nh t??? camera v?? storage
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE:
                if(grantResults.length>0){
                    boolean cameraAccepted = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[0]==
                            PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && writeStorageAccepted){
                        pickCamera();
                    }
                    else {
                        Toast.makeText(this,"Permission denied",Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case STORAGE_REQUEST_CODE:
                if(grantResults.length>0){

                    boolean writeStorageAccepted = grantResults[0]==
                            PackageManager.PERMISSION_GRANTED;
                    if(writeStorageAccepted){
                        pickGallery();
                    }
                    else {
                        Toast.makeText(this,"Permission denied",Toast.LENGTH_SHORT).show();
                    }
                }
                break;

        }
    }
    //endregion
    //region X??? l?? c???t h??nh v?? ?????c ch??? t??? h??nh ???????c c???t
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode==IMAGE_PICK_GALLERY_CODE){
                // l???y h??nh t??? th?? vi???n h??nh r???i c???t n??
                CropImage.activity(data.getData())
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this);
            }
            if(requestCode==IMAGE_PICK_CAMERA_CODE){
                // l???y h??nh t??? camera r???i c???t n??
                CropImage.activity(image_uri).setGuidelines(CropImageView.Guidelines.ON)
                        .start(this);
            }
        }
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK) {
                Uri resultUri = result.getUri(); // l???y link h??nh
                binding.imgHinhAnh.setImageURI(resultUri);

                BitmapDrawable bitmapDrawable = (BitmapDrawable) binding.imgHinhAnh.getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();

                TextRecognizer recognizer = new TextRecognizer.Builder(getApplicationContext()).build();

                if(!recognizer.isOperational()){
                    Toast.makeText(this,"Error", Toast.LENGTH_SHORT).show();
                }
                else {
                    Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<TextBlock> items = recognizer.detect(frame);

                    String sb ="";
                    String sb1 ="";
                    //region L???y ch??? trong h??nh ra r???i add v?? listview

                    tuScanDuocAdapter.clear();
                    lvTuScanDuoc.invalidateViews();
                    lvTuScanDuoc.refreshDrawableState();

                    for(int i =0;i<items.size();i++)
                    {
                        TextBlock myItem = items.valueAt(i);
                        sb+=myItem.getValue();
                        sb+=" ";
                    }
                    if(sb =="")
                    {
                        Toast.makeText(this,"H??nh ???nh b???n ch???n kh??ng ch???a t???." + "\n" +
                                        "Vui l??ng ch???n ???nh kh??c"
                                ,Toast.LENGTH_LONG).show();
                    }
                    else {
                        sb1 = sb.replaceAll("\n"," ");
                        String [] arrTu =sb1.toString().split(" ");
                        for(int i =0;i<arrTu.length;i++)
                        {
                            tuScanDuocAdapter.add(arrTu[i]);
                        }
                    }
                    //endregion
                }
            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this,"" + error,Toast.LENGTH_SHORT).show();
            }
        }

    }

    //endregion
    //region Cho ph??p m??? storage v?? camera
    protected void addCameraPermission() {
        // 21p38

        cameraPermission = new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    }
    //endregion

    //region GetMeaning
    public void getMeaning(String word){
        String answer=myDatabaseHelper.getMeaning(word);
        String noanswer="WORD NOT FOUND\nPLEASE SEARCH AGAIN";
        Intent intent=new Intent(ChenHinhActivity.this,DetailActivity.class);
        try {
            if(answer==null){
                intent.putExtra("word",word);
                intent.putExtra("nodetail",noanswer);
               // binding.txtTuTimDuoc.setText("");
                startActivity(intent);
            }else {
                intent.putExtra("word",word);
                intent.putExtra("detail",answer);
               // binding.txtTuTimDuoc.setText("");
                startActivity(intent);
            }
        }catch (Exception e){
        }
    }
    //endregion

    //region X??? l?? add t??? v??o listview
    private void addControlsLV() {
        lvTuScanDuoc = findViewById(R.id.lvTuDuocScan);
        tuScanDuocAdapter = new TuScanDuocAdapter(ChenHinhActivity.this,R.layout.tuscanduoc_list_item);
        lvTuScanDuoc.setAdapter(tuScanDuocAdapter);
    }
    private void addEventsClickLV() {
       binding.lvTuDuocScan.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String word = parent.getItemAtPosition(position).toString().toLowerCase();
                Log.d("word", "onItemClick: "+word);
                getMeaning(word);
            }
        });
    }
    //endregion


}
