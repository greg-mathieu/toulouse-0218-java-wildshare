package fr.wildcodeschool.wildshare;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class AddItem extends AppCompatActivity {

    ImageView mImgChoose;
    EditText mItemName;
    EditText mItemDesc;
    String mLink;
    String mUrlSave;
    String mItemModifKey;
    private Uri mUri = null;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mDatabaseReferenceU;
    private FirebaseDatabase mDatabase;
    private FirebaseAuth mAuth;
    private StorageReference mStorageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        mItemName = findViewById(R.id.et_newItemName);
        mItemDesc = findViewById(R.id.et_description);
        final EditText edLink = findViewById(R.id.editText_link);
        Button btnCamera = findViewById(R.id.button_camera);
        Button btnGallery = findViewById(R.id.button_gallery);
        Button btnLink = findViewById(R.id.button_link);
        Button btnAddItem = findViewById(R.id.b_addToData);
        final Button btnOK = findViewById(R.id.button_ok);
        mImgChoose = findViewById(R.id.iv_img_choose);

        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mDatabaseReferenceU = FirebaseDatabase.getInstance().getReference();
        mStorageReference = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        final String itemNameValue = getIntent().getStringExtra("itemName");
        mItemName.setText(itemNameValue);

        final DatabaseReference itemRef = mDatabase.getReference("Item");
        itemRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (final DataSnapshot itemDataSnapshot : dataSnapshot.getChildren()) {

                    final String itemName = itemDataSnapshot.child("name").getValue(String.class);

                    if (itemName.equals(itemNameValue)) {

                        final ItemModel itemModel = itemDataSnapshot.getValue(ItemModel.class);

                        mItemDesc.setText(itemModel.getDescription());

                        Glide.with(AddItem.this).load(itemModel.getImage()).apply(RequestOptions.circleCropTransform()).into(mImgChoose);
                        mUrlSave = itemModel.getImage();
                        mItemModifKey = itemDataSnapshot.getKey();
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        Button cancel = findViewById(R.id.b_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intenthome = new Intent(AddItem.this, HomeActivity.class);
                startActivity(intenthome);
                finish();
            }
        });

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());
                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                mUri = CameraUtils.getOutputMediaFileUri(AddItem.this);
                takePicture.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
                startActivityForResult(takePicture, 0);
            }
        });

        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
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
                mLink = edLink.getText().toString();
                Glide.with(AddItem.this).load(mLink).into(mImgChoose);
                edLink.setVisibility(View.GONE);
                btnOK.setVisibility(View.GONE);
            }
        });

        btnAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String itemName = mItemName.getText().toString();
                String itemDesc = mItemDesc.getText().toString();
                if (itemName.isEmpty() || (itemDesc.isEmpty())) {
                    Toast.makeText(AddItem.this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
                } else {
                    saveItemModel();
                }
            }
        });
    }

    private void saveItemModel() {
        final String name = mItemName.getText().toString();
        final String description = mItemDesc.getText().toString();
        final FirebaseUser user = mAuth.getCurrentUser();
        final String ownerId = user.getUid().toString();
        mDatabaseReference = mDatabase.getReference("Item");
        mDatabaseReferenceU = mDatabase.getReference("User");
        final String itemKey = mDatabaseReference.push().getKey();

        if (mItemModifKey == null) {
            mItemModifKey = itemKey;
        }
        if (mUri == null) {
            String image;
            if (mLink != null) {
                image = mLink;
            } else {
                image = mUrlSave;
            }
            ItemModel itemModel = new ItemModel(name, image, description, ownerId);
            mDatabaseReference.child(mItemModifKey).setValue(itemModel);
            mDatabaseReferenceU.child(user.getUid()).child("Item").child(mItemModifKey).setValue("0");

            Intent intent = new Intent(AddItem.this, HomeActivity.class);
            startActivity(intent);
        } else {
            StorageReference filePath = mStorageReference.child("itemPicture").child(mUri.getLastPathSegment());
            filePath.putFile(mUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    String image = downloadUrl.toString();
                    ItemModel itemModel = new ItemModel(name, image, description, ownerId);
                    mDatabaseReference.child(mItemModifKey).setValue(itemModel);
                    mDatabaseReferenceU.child(user.getUid()).child("Item").child(mItemModifKey).setValue("0");

                    Intent intent = new Intent(AddItem.this, HomeActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch (requestCode) {
            case 0:
                if (resultCode == RESULT_OK) {
                    mImgChoose.setImageURI(mUri);
                }
                break;
            case 1:
                if (resultCode == RESULT_OK) {
                    mUri = imageReturnedIntent.getData();
                    mImgChoose.setImageURI(mUri);
                }
                break;
        }
    }

}