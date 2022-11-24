package com.lifeapp.helper;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.lifeapp.activity.MapsActivity;
import com.lifeapp.activity.PassageiroActivity;
import com.lifeapp.activity.RequisicoesActivity;
import com.lifeapp.activity.TesteActivity;
import com.lifeapp.config.ConfiguracaoFirebase;
import com.lifeapp.model.Usuario;



public class UsuarioFirebase {


    public static FirebaseUser getUsuarioAtual(){
        FirebaseAuth usuario = ConfiguracaoFirebase.getFirebaseAutenticacao();
        return usuario.getCurrentUser();

    }

    public static Usuario getDadosUsuarioLogado(){
        FirebaseUser firebaseUSer = getUsuarioAtual();
        Usuario usuario = new Usuario();
        usuario.setId(firebaseUSer.getUid());
        usuario.setEmail(firebaseUSer.getEmail());
        usuario.setNome(firebaseUSer.getDisplayName());

        return usuario;

    }



    public static  boolean atualizarNomeUsuario(String nome){

        try {

            FirebaseUser user = UsuarioFirebase.getUsuarioAtual();
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                    .setDisplayName(nome)
                    .build();
            user.updateProfile(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(!task.isSuccessful()){
                           Log.d("Perfil", "Erro ao atualizar nome perfil");
                    }
                }
            });

            return true;

        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

    }

    public static  void redirecionaUsuarioLogado(Activity activity){

        FirebaseUser user = getUsuarioAtual();

        if(user!=null){
            DatabaseReference usariosRef = ConfiguracaoFirebase.getFirebaseDataBase().child("usuarios").child( getIdenticadorUsuario() );
            usariosRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {


                    Usuario usuario = snapshot.getValue(Usuario.class);
                    String tipoUSuario = usuario.getTipo();

                    if(tipoUSuario.equals("M")){
                        // Intent i = new Intent();
                        activity.startActivity( new Intent(activity, RequisicoesActivity.class));

                    }else{

                        activity.startActivity( new Intent(activity, PassageiroActivity .class));

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }



    }

    public static void atualizarDadosLocalizacao(double lat, double lng){

        DatabaseReference localUsuario = ConfiguracaoFirebase.getFirebaseDataBase()
                .child("local_usuario");
        GeoFire geoFire = new GeoFire(localUsuario);

        Usuario usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();

        geoFire.setLocation(
                usuarioLogado.getId(),
                new GeoLocation(lat, lng),
                new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        if(error != null ){
                            Log.d("Erro", "Erro ao salvar local!");
                        }
                    }
                });



    }

    public static String getIdenticadorUsuario(){
        return getUsuarioAtual().getUid();
    }



}
