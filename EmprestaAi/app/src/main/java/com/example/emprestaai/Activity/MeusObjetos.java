package com.example.emprestaai.Activity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emprestaai.Adapter.ObjetoAdapter;
import com.example.emprestaai.DAO.ObjetoDAO;
import com.example.emprestaai.DAO.PedidoDAO;
import com.example.emprestaai.Model.Objeto;
import com.example.emprestaai.Model.Pedido;
import com.example.emprestaai.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class MeusObjetos extends AppCompatActivity implements ObjetoAdapter.ItemClicado {
    ArrayList<Objeto> objetos;
    RecyclerView lista;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;
    TextView tvObjeto;
    FloatingActionButton fabAdd, fabPesquisar, fabPedidos, fabSolicitacoes;
    int ADD = 1, VISUALIZAR=2, EXCLUIR = 3, EDITAR = 4, PEDIR = 5, SOLICITADO = 6;
    String idObjeto;
    String DONO_ATUAL, ID_DONO_ATUAL;
    ObjetoDAO objetoDAO;
    PedidoDAO pedidoDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meus_objetos);

        Intent intent = getIntent();

        tvObjeto = (TextView) findViewById(R.id.tvObjeto);
        tvObjeto.setVisibility(View.GONE);
        lista = (RecyclerView) findViewById(R.id.rvPedidos);
        fabAdd = (FloatingActionButton) findViewById(R.id.add);
        fabPesquisar = (FloatingActionButton) findViewById(R.id.pesquisar);
        fabPedidos = (FloatingActionButton) findViewById(R.id.meusPedidos);
        fabSolicitacoes = (FloatingActionButton) findViewById(R.id.solicitacoes);
        lista.setHasFixedSize(true);

        DONO_ATUAL = intent.getStringExtra("donoAtual");
        ID_DONO_ATUAL = intent.getStringExtra("idDonoAtual");
        layoutManager = new LinearLayoutManager(this);
        lista.setLayoutManager(layoutManager);

        objetos = new ArrayList<Objeto>();
        objetoDAO = new ObjetoDAO(com.example.emprestaai.Activity.MeusObjetos.this);
        Cursor cursor = objetoDAO.procurarObjetosDono(ID_DONO_ATUAL);

        //Carregando meus objetos do banco
        if(cursor.getCount() == 0){
            isListavazia();
        }else{
            while(cursor.moveToNext()){
                Objeto obj = new Objeto(Integer.toString(cursor.getInt(0)),
                        DONO_ATUAL,
                        cursor.getString(2),
                        cursor.getString(3),
                        getImage(cursor.getBlob(4)));
                objetos.add(obj);
            }
        }
        cursor.close();

        adapter = new ObjetoAdapter(this,objetos);
        lista.setAdapter(adapter);
        //Todo: Solicitar ou recusar pedidos

        fabPesquisar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(MeusObjetos.this, PesquisarObjetos.class);
                intent1.putExtra("idDonoAtual",ID_DONO_ATUAL);
                startActivityForResult(intent1,PEDIR);
            }
        });

        fabPedidos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(MeusObjetos.this, ListaPedidos.class);
                intent1.putExtra("idDonoAtual",ID_DONO_ATUAL);
                startActivityForResult(intent1,PEDIR);
            }
        });

        fabSolicitacoes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent1 = new Intent(MeusObjetos.this, Solicitacoes.class);
                //startActivity(intent1);
            }
        });
        fabSolicitacoes.setVisibility(View.GONE);

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(MeusObjetos.this, NovoObjeto.class);
                startActivityForResult(intent1,ADD);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD) {
            if (resultCode == RESULT_OK) {
               idObjeto = objetoDAO.addObjeto(ID_DONO_ATUAL,
                        data.getStringExtra("nome"),
                        data.getStringExtra("status"),
                        data.getByteArrayExtra("imagem"));
                if(!idObjeto.equals("-1")) {
                    Objeto obj = new Objeto(idObjeto,
                            DONO_ATUAL,
                            data.getStringExtra("nome"),
                            data.getStringExtra("status"),
                            getImage(data.getByteArrayExtra("imagem")));
                    objetos.add(obj);
                    adapter.notifyItemInserted(objetos.size() - 1);
                    isListavazia();
                }
            }
        }else if(requestCode == VISUALIZAR){
            if(resultCode == EXCLUIR){
                idObjeto = data.getStringExtra("idObjeto");
                int posi = getIndexObj();

                objetos.remove(posi);
                adapter.notifyItemRemoved(posi);
                Toast.makeText(this, "Objeto excluído com sucesso", Toast.LENGTH_SHORT).show();
                isListavazia();
            }else if(resultCode == EDITAR){
                idObjeto = data.getStringExtra("idObjeto");
                int posi = getIndexObj();

                objetoDAO.updateObjeto(idObjeto,
                        ID_DONO_ATUAL,
                        data.getStringExtra("nome"),
                        data.getStringExtra("status"));

                Objeto obj = new Objeto(idObjeto,
                        DONO_ATUAL,
                        data.getStringExtra("nome"),
                        data.getStringExtra("status"),
                        getImage(data.getByteArrayExtra("imagem")));
                objetos.set(posi,obj);
                adapter.notifyItemChanged(posi);
            }
        }else if(requestCode == PEDIR){
            if (resultCode == SOLICITADO){
                pedidoDAO = new PedidoDAO(com.example.emprestaai.Activity.MeusObjetos.this);
                idObjeto = data.getStringExtra("idObjeto");
                String idPedido = pedidoDAO.addPedido(idObjeto,ID_DONO_ATUAL,
                        data.getStringExtra("dono"),
                        data.getStringExtra("periodo"),
                        data.getStringExtra("local"));

                if(!idPedido.equals("-1")) {
                    Objeto obj = new Objeto(idObjeto,
                            data.getStringExtra("dono"),
                            data.getStringExtra("nome"),
                            data.getStringExtra("status"),
                            getImage(data.getByteArrayExtra("imagem")));
                    Pedido pedido = new Pedido(idPedido, obj, data.getStringExtra("periodo"),data.getStringExtra("local"), ID_DONO_ATUAL);
                    fabPedidos.callOnClick();
                }
            }
        }
    }

    @Override
    public void onItemClicked(int posicao, ArrayList<Objeto> objetos) {
        Intent intent = new Intent(MeusObjetos.this, VisualizarObjeto.class);
        Objeto obj = this.objetos.get(posicao);
        intent.putExtra("donoAtual", DONO_ATUAL);
        intent.putExtra("idObjeto",obj.getIdObjeto());
        intent.putExtra("idDonoAtual",ID_DONO_ATUAL);
        intent.putExtra("nome",obj.getNome());
        intent.putExtra("status",obj.getStatus());
        intent.putExtra("imagem",getBytes(obj.getImagem()));
        startActivityForResult(intent,VISUALIZAR);
    }
    public void isListavazia(){
        if (objetos.isEmpty()) {
            lista.setVisibility(View.GONE);
            tvObjeto.setVisibility(View.VISIBLE);
        } else {
            lista.setVisibility(View.VISIBLE);
            tvObjeto.setVisibility(View.GONE);
        }
    }

    public Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }
    public byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    public int getIndexObj(){
        for(Objeto o : objetos){
            if(o.getIdObjeto().equals(idObjeto)){
                return objetos.indexOf(o);
            }
        }
        return 0;
    }
}