package fr.wildcodeschool.wildshare;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

public class ProfilActivity extends AppCompatActivity {

    Button btnCamera;
    Button btnGallery;
    Button btnLink;
    Button btnOK;
    EditText edLink;
    ImageView imgProfilPic;
    EditText editName;
    Button btnValidModif;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);


        edLink = findViewById(R.id.editText_linkP);
        btnCamera = findViewById(R.id.button_cameraP);
        btnGallery = findViewById(R.id.button_galleryP);
        btnLink = findViewById(R.id.button_linkP);
        btnOK = findViewById(R.id.button_okP);
        imgProfilPic = findViewById(R.id.imageView_profilPic);
        editName = findViewById(R.id.editText_enterName);
        btnValidModif = findViewById(R.id.button_validModif);


        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePicture, 0);
            }
        });

        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create intent to Open Image applications like Gallery, Google Photos
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                // Start the Intent
                startActivityForResult(galleryIntent, 1);
            }
        });

        btnLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edLink.setVisibility(View.VISIBLE);
                btnOK.setVisibility(View.VISIBLE);

            }
        });

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String link = edLink.getText().toString();
                Glide.with(ProfilActivity.this).load(link) .into(imgProfilPic);
            }
        });

        btnValidModif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String valueName = editName.getText().toString();
                if (valueName.isEmpty()){
                    Toast.makeText(ProfilActivity.this, "Enter a name", Toast.LENGTH_SHORT).show();
                }
                else {
                    Intent intentHome = new Intent(ProfilActivity.this, HomeActivity.class);
                    startActivity(intentHome);
                }
            }
        });



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch(requestCode) {
            case 0:
                if(resultCode == RESULT_OK) {
                    Bitmap bitmap = (Bitmap) imageReturnedIntent.getExtras().get("data");
                    imgProfilPic.setImageBitmap(bitmap);
                }
                break;
            case 1:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = imageReturnedIntent.getData();
                    imgProfilPic.setImageURI(selectedImage);
                }
                break;
        }
    }
}