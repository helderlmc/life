package com.lifeapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.AndroidException;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.lifeapp.R;
import com.lifeapp.config.ConfiguracaoFirebase;
import com.lifeapp.helper.UsuarioFirebase;
import com.lifeapp.model.Usuario;

public class CadastroActivity extends AppCompatActivity {

    private TextInputEditText campoNome, campoEmail, campoSenha;

    private Switch switchTipoUsuario;

    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        campoNome = findViewById(R.id.editCadastroNome);
        campoEmail = findViewById(R.id.editCadastroEmail);
        campoSenha = findViewById(R.id.editCadastroSenha);
        switchTipoUsuario = findViewById(R.id.switchTipoUsuario);

    }

   public void validarCadastroUsuario(View view){

        String txtNome = campoNome.getText().toString();
        String txtEmail = campoEmail.getText().toString();
        String txtSenha = campoSenha.getText().toString();

        if(!txtNome.isEmpty()){
            if(!txtEmail.isEmpty()){
                if(!txtSenha.isEmpty()){

                    Usuario usuario = new Usuario();
                    usuario.setNome(txtNome);
                    usuario.setEmail(txtEmail);
                    usuario.setSenha(txtSenha);
                    usuario.setTipo(verificaTipoUsuario());
                    Toast.makeText(CadastroActivity.this, txtNome + " - " + txtEmail + " - " + txtSenha, Toast.LENGTH_SHORT).show();
                    cadastrarUsuario(usuario);

                }else{
                    Toast.makeText(CadastroActivity.this, "Preencha a senha!", Toast.LENGTH_SHORT).show();

                }

            }else{
                Toast.makeText(CadastroActivity.this, "Preencha o email!", Toast.LENGTH_SHORT).show();

            }

        }else{
            Toast.makeText(CadastroActivity.this, "Preencha o nome!", Toast.LENGTH_SHORT).show();

        }


   }

    public void cadastrarUsuario(Usuario usuario) {

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(usuario.getEmail(), usuario.getSenha()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){

                    try {
                        String idUsuario = task.getResult().getUser().getUid();
                        usuario.setId(idUsuario);
                        usuario.salvar();

                        UsuarioFirebase.atualizarNomeUsuario(usuario.getNome());

                        if(verificaTipoUsuario()=="P"){
                            startActivity(new Intent(CadastroActivity.this, MapsActivity.class));
                            finish();
                            Toast.makeText(CadastroActivity.this, "Passageiro Cadastrado com Sucesso!", Toast.LENGTH_SHORT).show();
                        }else{
                            startActivity(new Intent(CadastroActivity.this, RequisicoesActivity.class));
                            finish();
                            Toast.makeText(CadastroActivity.this, "Motorista Cadastrado com Sucesso!", Toast.LENGTH_SHORT).show();

                        }


                    }catch (Exception e){
                        e.printStackTrace();

                    }


                }else{
                    String excecao = "";
                    try {
                        throw task.getException();
                    }catch (FirebaseAuthWeakPasswordException e){
                        excecao ="Digite uma senha mais forte";
                    }catch (FirebaseAuthInvalidCredentialsException e){
                        excecao ="Por favor, Digite um e-mail válido";
                    }catch (FirebaseAuthUserCollisionException e){
                        excecao ="Essa conta já foi cadastrada";
                    }catch (Exception e){
                        excecao ="Erro ao cadastrar usuário: " + e.getMessage();
                        e.printStackTrace();
                    }
                    Toast.makeText(CadastroActivity.this, excecao, Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    public String verificaTipoUsuario(){

       return switchTipoUsuario.isChecked() ?  "M" :  "P";

   }


}