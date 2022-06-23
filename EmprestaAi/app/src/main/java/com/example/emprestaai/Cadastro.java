package com.example.emprestaai;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.firestore.FirebaseFirestore;


public class Cadastro extends AppCompatActivity {
    TextInputLayout tiNome, tiEmail, tiSenha;
    TextView tvEmailCadastrado;
    Button btnCadastro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        tiNome = (TextInputLayout) findViewById(R.id.tiNome);
        tiEmail = (TextInputLayout) findViewById(R.id.tiEmail);
        tiSenha = (TextInputLayout) findViewById(R.id.tiSenha);
        btnCadastro = (Button) findViewById(R.id.btnCadastro2);
        tvEmailCadastrado = (TextView) findViewById(R.id.tvEmailCadastrado);
        tvEmailCadastrado.setVisibility(View.GONE);

        btnCadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nome = tiNome.getEditText().getText().toString();
                String email = tiEmail.getEditText().getText().toString();
                String senha = tiSenha.getEditText().getText().toString();
                if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                    Toast.makeText(Cadastro.this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                } else {
                    CadastrarUsuario();
                }
            }});
    }

    private void CadastrarUsuario() {
        String nome = tiNome.getEditText().getText().toString();
        String email = tiEmail.getEditText().getText().toString();
        String senha = tiSenha.getEditText().getText().toString();

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,senha).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    SalvarDadosUsario();

                    Toast.makeText(Cadastro.this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Cadastro.this, Login.class);
                    startActivity(intent);
                    Cadastro.this.finish();
                }else{
                    String erro;
                    try{
                        throw task.getException();
                    }catch (FirebaseAuthWeakPasswordException e){
                        erro = "Digite uma senha de no mínimo 6 caracteres";
                    }catch (FirebaseAuthUserCollisionException e){
                        erro = "Conta já cadastrada";
                    }catch (FirebaseAuthInvalidCredentialsException e){
                        erro = "E-mail inválido";
                    }catch (Exception e){
                        erro = "Erro ao cadastrar usuário";
                    }
                    Toast.makeText(Cadastro.this, erro, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void SalvarDadosUsario() {
        String nome = tiNome.getEditText().getText().toString();
        FirebaseFirestore db = FirebaseFirestore.getInstance();


    }
}