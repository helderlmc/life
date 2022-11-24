package com.lifeapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;

import com.google.android.libraries.places.api.Places;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
//import com.lifeapp.BuildConfig;
import com.lifeapp.R;
import com.lifeapp.config.ConfiguracaoFirebase;
import com.lifeapp.helper.Permissoes;
import com.lifeapp.helper.UsuarioFirebase;

public class MainActivity extends AppCompatActivity {


    private FirebaseAuth autenticacao;

    private String[] permissoes = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
          //  Manifest.permission.INTERNET,
          //  Manifest.permission.ACCESS_COARSE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
      //  ActivityCompat.requestPermissions(activity, novasPermissoes, requestCode );
        Permissoes.validarPermissoes(permissoes, this, 1);

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
      //  autenticacao.signOut();
        final String apiKey = "AIzaSyBXhi9BvD1HD1BGA-iuP5Xl8xF1vqBwxVY";

//        if (apiKey.equals("")) {
//            Toast.makeText(this, getString(R.string.error_api_key), Toast.LENGTH_LONG).show();
//            return;
//        }

        // Setup Places Client
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }


    }

    public void abrirTelaLogin(View view){
        startActivity( new Intent(this, LoginActivity.class));


    }

    public void abrirTelaCadastro(View view){
        startActivity( new Intent(this, CadastroActivity.class));


    }

    @Override
    protected void onStart() {
        super.onStart();
        UsuarioFirebase.redirecionaUsuarioLogado(MainActivity.this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for(int permissaoResultado : grantResults){
            if(permissaoResultado == PackageManager.PERMISSION_DENIED){
                alertaValidacaoPermissao();
            }

        }
    }

    private void alertaValidacaoPermissao(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões Negadas");
        builder.setMessage("Para utilizar o app é necessário aceitar as permissões");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }



}