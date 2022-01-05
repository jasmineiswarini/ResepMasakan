package com.example.resepmasakan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.DragStartHelper;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.resepmasakan.adapter.UserAdapter;
import com.example.resepmasakan.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private FloatingActionButton plus;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RecyclerView recyclerView;;
    private List<User> list = new ArrayList<>();
    private UserAdapter userAdapter;
    private ProgressDialog pg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView =findViewById(R.id.tv_tampil);
        pg = new ProgressDialog(MainActivity.this);
        pg.setTitle("Loading...");
        pg.setMessage("Mengambil data...");

        userAdapter = new UserAdapter(getApplicationContext(), list);
        userAdapter.setDialog(new UserAdapter.Dialog() {
            @Override
            public void onClick(int pos) {
                final CharSequence[] dialogItem = {"Edit", "Hapus","Tampilkan"};
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setItems(dialogItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        switch (i){
                            case 0:
                                Intent intent = new Intent(getApplicationContext(), resepBaru.class);
                                intent.putExtra("id", list.get(pos).getId());
                                intent.putExtra("judul",list.get(pos).getJudul());
                                intent.putExtra("detail", list.get(pos).getDetail());
                                intent.putExtra("gambar",list.get(pos).getGambar());
                                startActivity(intent);
                                break;
                            case 1 :
                                deleteData(list.get(pos).getId(), list.get(pos).getGambar());
                                break;
                            case 2 :
                                Intent intent1 = new Intent(getApplicationContext(), detailResep.class);
                                intent1.putExtra("id", list.get(pos).getId());
                                intent1.putExtra("judul", list.get(pos).getJudul());
                                intent1.putExtra("detail", list.get(pos).getDetail());
                                intent1.putExtra("gambar",list.get(pos).getGambar());
                                startActivity(intent1);
                        }
                    }
                });
                dialog.show();

            }

        });
        RecyclerView.LayoutManager layoutManager= new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        RecyclerView.ItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(decoration);
        recyclerView.setAdapter(userAdapter);


        plus = findViewById(R.id.btn_tambah);
        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,resepBaru.class));
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        getData();
    }

    private void getData(){
        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        list.clear();
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document : task.getResult()){
                                User user = new User(document.getString("judul"), document.getString("detail"), document.getString("gambar"));
                                user.setId(document.getId());
                                list.add(user);
                            }
                            userAdapter.notifyDataSetChanged();
                        }else{
                            Toast.makeText(getApplicationContext(), "DATA GAGAL DI AMBIL", Toast.LENGTH_SHORT).show();
                        }pg.dismiss();
                    }
                });
    }
    private void deleteData(String id, String gambar){
        db.collection("users").document(id)
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()){
                            pg.dismiss();
                            Toast.makeText(getApplicationContext(), "DATA GAGAL DIHAPUS", Toast.LENGTH_SHORT).show();
                        }else{
                            FirebaseStorage.getInstance().getReferenceFromUrl(gambar).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    pg.dismiss();
                                    getData();
                                }
                            });
                        }

                    }
                });

    }

}