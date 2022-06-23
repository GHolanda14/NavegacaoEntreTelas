package com.example.navegacaoentretelas;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.NumberKeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    ArrayList<Objeto> objetos;
    RecyclerView lista;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;
    Button btnAdd, btnEdit;
    EditText etId;
    int ADICIONAR = 1, EDITAR = 2, id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        id = 0;

        btnAdd = (Button) findViewById(R.id.btnAdd);
        btnEdit = (Button) findViewById(R.id.btnEdit);
        lista = (RecyclerView) findViewById(R.id.rvLista);
        etId = (EditText) findViewById(R.id.etId);
        lista.setHasFixedSize(true);

        objetos = new ArrayList<Objeto>();
        layoutManager = new LinearLayoutManager(this);
        lista.setLayoutManager(layoutManager);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Objetos")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            id = 0;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("msg", document.getId() + " => " + document.getData());
                                Objeto obj = new Objeto(document.getData().get("nome").toString(),
                                        document.getData().get("descricao").toString(),
                                        document.getId());
                                objetos.add(obj);
                                id++;
                            }
                            Log.d("msg","Executando onCreate()");
                            adapter = new ObjetoAdapter(MainActivity.this,objetos);
                            lista.setAdapter(adapter);
                        } else {
                            Log.w("msg", "Error getting documents.", task.getException());
                        }
                    }
                });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,com.example.navegacaoentretelas.GestorDeObjetos.class);
                intent.putExtra("posicao",id);
                intent.putExtra("requestCode",ADICIONAR);
                startActivityForResult(intent,ADICIONAR);
            }
        });

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,com.example.navegacaoentretelas.GestorDeObjetos.class);
                int idObjeto = Integer.parseInt(etId.getText().toString());
                if(idObjeto >= 0 && idObjeto < id){
                    intent.putExtra("posicao",idObjeto);
                    intent.putExtra("id",objetos.get(idObjeto).getId());
                    intent.putExtra("nome",objetos.get(idObjeto).getNome());
                    intent.putExtra("descricao",objetos.get(idObjeto).getDescricao());
                    intent.putExtra("requestCode",EDITAR);
                    startActivityForResult(intent,EDITAR);
                    return;
                    }
                Toast.makeText(MainActivity.this, "Id inválido!", Toast.LENGTH_SHORT).show();
            }
        });

        etId.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                btnEdit.setEnabled(true);
                if(etId.getText().toString().isEmpty()) {
                    btnEdit.setEnabled(false);
                }
                return false;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ADICIONAR){
            if(resultCode == RESULT_OK){
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                Map<String, Object> obj = new HashMap<>();
                obj.put("nome", data.getStringExtra("nome"));
                obj.put("descricao", data.getStringExtra("descricao"));

                db.collection("Objetos").add(obj).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Objeto objs = new Objeto(data.getStringExtra("nome"),
                                data.getStringExtra("descricao"),
                                documentReference.getId());
                        objetos.add(objs);
                        adapter.notifyItemInserted(id);
                        id++;
                        Toast.makeText(MainActivity.this, "Objeto adicionado com sucesso!", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Erro ao adicionar objeto", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }else if( requestCode == EDITAR){
            if(resultCode == RESULT_OK){
                String nome = data.getStringExtra("nome");
                String descricao = data.getStringExtra("descricao");

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference documentReference = db.collection("Objetos")
                        .document(data.getStringExtra("id"));

                documentReference.update("nome",nome,
                        "descricao",descricao)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                int posicao = data.getIntExtra("posicao",0);
                                Objeto obj = new Objeto(nome,descricao,data.getStringExtra("id"));
                                objetos.set(posicao,obj);
                                adapter.notifyItemChanged(posicao);
                                Toast.makeText(MainActivity.this, "Objeto atualizado!", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MainActivity.this, "Erro ao atualizar objeto", Toast.LENGTH_SHORT).show();
                            }
                        });

                ;
            }
        }
    }

}