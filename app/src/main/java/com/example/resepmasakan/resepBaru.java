package com.example.resepmasakan;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.resepmasakan.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.core.Tag;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class resepBaru extends AppCompatActivity {
    private ImageView g1;
    private EditText t1, t2;
    private Button b1;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ProgressDialog pd;
    private String id = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resep_baru);

        g1 = findViewById(R.id.gambar);
        t1 = findViewById(R.id.judul);
        t2 = findViewById(R.id.detail);
        b1 = findViewById(R.id.btnsimpan);

        pd = new ProgressDialog(resepBaru.this);
        pd.setTitle("Loading");
        pd.setMessage("Menyimpan....");
        g1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (t1.getText().length() > 0 && t2.getText().length() > 0) {
                    uploud(t1.getText().toString(), t2.getText().toString());
                } else {
                    Toast.makeText(getApplicationContext(), "Silahkan Masukkan data terlebih dahulu", Toast.LENGTH_SHORT).show();
                }
            }

        });
        Intent intent = getIntent();
        if (intent != null) {
            id = intent.getStringExtra("id");
            t1.setText(intent.getStringExtra("judul"));
            t2.setText(intent.getStringExtra("detail"));
            Glide.with(getApplicationContext()).load(intent.getStringExtra("gambar")).into(g1);
        }
    }

    private void selectImage(){
        final CharSequence[] items = {"Ambil Gambar", "Pilih dari Galeri","Batal"};
        AlertDialog.Builder builder = new AlertDialog.Builder(resepBaru.this);
        builder.setTitle(getString(R.string.app_name));
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setItems(items,((dialog, item) -> {
            if(items[item].equals("Ambil Gambar")){
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,10);
            }else if(items[item].equals("Pilih dari Galeri")){
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent,"Pilih Gambar"),20);
            }else if (items[item].equals("Batal")){
                dialog.dismiss();
            }
        }));
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 20 && resultCode == RESULT_OK && data !=null){
            final Uri path = data.getData();
            Thread thread = new Thread(() ->{
                try {
                    InputStream inputStream = getContentResolver().openInputStream(path);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    g1.post(()->{
                        g1.setImageBitmap(bitmap);
                    });
                }catch (IOException e){
                    e.printStackTrace();
                }
            });
            thread.start();
        }
        if(requestCode==10&&resultCode==RESULT_OK){
            final Bundle extras = data.getExtras();
            Thread thread = new Thread(()->{
                Bitmap bitmap = (Bitmap) extras.get("data");
                g1.post(()->{
                    g1.setImageBitmap(bitmap);
                });
            });
            thread.start();
        }
    }

    private void uploud(String judul, String detail){
        g1.setDrawingCacheEnabled(true);
        g1.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) g1.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        //upload
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference reference = storage.getReference("images").child("IMG"+new Date().getTime()+".jpeg");
        UploadTask uploadTask = reference.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
               if(taskSnapshot.getMetadata()!=null){
                   if (taskSnapshot.getMetadata().getReference()!=null){
                       taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                           @Override
                           public void onComplete(@NonNull Task<Uri> task) {
                               if (task.getResult()!=null){
                                   saveData(judul,detail, task.getResult().toString());
                               }else{
                                   Toast.makeText(getApplicationContext(), "GAGAL", Toast.LENGTH_SHORT).show();
                                   pd.dismiss();
                               }
                           }
                       });
                   }else {
                       Toast.makeText(getApplicationContext(),"GAGAL",Toast.LENGTH_SHORT).show();
                       pd.dismiss();
                   }
               }else{
                   Toast.makeText(getApplicationContext(), "GAGAL", Toast.LENGTH_SHORT).show();
                   pd.dismiss();
               }
            }
        });
    }

    private void saveData(String judul, String detail, String gambar){
        Map<String, Object> user = new HashMap<>();
        user.put("judul", judul);
        user.put("detail", detail);
        user.put("gambar", gambar);

        pd.show();
        if (id!=null){
            db.collection("users").document(id)
                    .set(user)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(getApplicationContext(), "BERHASIL", Toast.LENGTH_SHORT).show();
                                finish();
                            }else{
                                Toast.makeText(getApplicationContext(), "GAGAL!!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }else {
            db.collection("users")
                    .add(user)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Toast.makeText(getApplicationContext(), "BERHASIL", Toast.LENGTH_SHORT).show();
                            pd.dismiss();
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            pd.dismiss();
                        }
                    });
        }
    }
    private void realtime(){
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build();
        db.setFirestoreSettings(settings);
        db.collection("users")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                        if (e!=null){
                            Log.w(TAG,"Litstener error", e);

                        }
                        for (DocumentChange change : snapshot.getDocumentChanges()) {
                            if (change.getType() == DocumentChange.Type.ADDED) {
                                Log.d(TAG, "judul" +change.getDocument().getData());
                                Log.d(TAG,"detail"+change.getDocument().getData());
                                Log.d(TAG,"gambar"+change.getDocument().getData());
                            }
                            String source = snapshot.getMetadata().isFromCache() ?
                                    "local cache" : "server";
                            Log.d(TAG, "Data fetched from " + source);
                        }
                    }

                });

    }
}