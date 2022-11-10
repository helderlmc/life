package com.lifeapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.lifeapp.R;
import com.lifeapp.config.ConfiguracaoFirebase;
import com.lifeapp.helper.UsuarioFirebase;
import com.lifeapp.model.Usuario;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText campoEmail, campoSenha;
    private FirebaseAuth autenticacao;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        campoEmail = findViewById(R.id.editLoginEmail);
        campoSenha = findViewById(R.id.editLoginSenha);
    }


    public void validarLoginUsuario (View view){

        String textoEmail = campoEmail.getText().toString();
        String textoSenha = campoSenha.getText().toString();

        if(!textoEmail.isEmpty()){
            if(!textoSenha.isEmpty()){

                Usuario usuario = new Usuario();
                usuario.setEmail(textoEmail);
                usuario.setSenha(textoSenha);

                logarUsuario( usuario);

            }else{
                Toast.makeText(LoginActivity.this, "Preencha o Email!", Toast.LENGTH_SHORT).show();
            }

        }else{
           Toast.makeText(LoginActivity.this, "Preencha a Senha!", Toast.LENGTH_SHORT).show();
        }


    }

    public void logarUsuario(Usuario usuario){

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.signInWithEmailAndPassword(usuario.getEmail(), usuario.getSenha()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){


                    UsuarioFirebase.redirecionaUsuarioLogado(LoginActivity.this);


                }else{
                    String excecao= "";
                    try {
                        throw task.getException();
                    }catch (FirebaseAuthInvalidUserException e){
                         excecao = "Usuário não está cadastrado";

                    }catch (FirebaseAuthInvalidCredentialsException e){
                        excecao = "Email ou senha não existe  ";

                    }catch (Exception e){
                        excecao = "Erro ao tentar realizar login";

                    }
                    Toast.makeText(LoginActivity.this, excecao, Toast.LENGTH_SHORT).show();

                }
            }
        });

    }


}