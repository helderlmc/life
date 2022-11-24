package com.lifeapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lifeapp.R;
import com.lifeapp.adapter.RequisicoesAdapter;
import com.lifeapp.config.ConfiguracaoFirebase;
import com.lifeapp.databinding.ActivityPassageiroBinding;
import com.lifeapp.databinding.ActivityRequisicoesBinding;
import com.lifeapp.helper.RecyclerItemClickListener;
import com.lifeapp.helper.UsuarioFirebase;
import com.lifeapp.model.Requisicao;
import com.lifeapp.model.Usuario;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
//import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class RequisicoesActivity extends AppCompatActivity {

    private FirebaseAuth autenticacao;
    private DatabaseReference fireBaseRef;
    private ActivityRequisicoesBinding binding;

    private RecyclerView recyclerRequisicoes;
    private TextView textResultado;
    private List<Requisicao> listaRequisicoes = new ArrayList<>();
    private RequisicoesAdapter adapter;
    private Usuario motorista;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location lastKnownLocation;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private boolean locationPermissionGranted = true;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requisicoes);
      //  binding = ActivityRequisicoesBinding.inflate(getLayoutInflater());
      //  setContentView(binding.getRoot());
     //   setSupportActionBar(binding.toolbar);
     //   binding.setTitle("Requisições");
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        fireBaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        recyclerRequisicoes = findViewById(R.id.recyclerRequisicoes);
        textResultado = findViewById(R.id.textView3);

        motorista = UsuarioFirebase.getDadosUsuarioLogado();
        adapter = new RequisicoesAdapter(listaRequisicoes, getApplicationContext(), motorista);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerRequisicoes.setLayoutManager(layoutManager);
        recyclerRequisicoes.setHasFixedSize(true);
        recyclerRequisicoes.setAdapter(adapter);
        fusedLocationProviderClient  = LocationServices.getFusedLocationProviderClient(this);
       // RecyclerView.LayoutManager layoutManager = new LinearLayoutManager();


        recuperarRequisicoes();
        getLocationPermission();
        recuperarLocalizacaoUsuario();
        recuperarLocalizacaoUsuarioNovo();

        adicionaEventoCliqueRecyclerView();
    }
    private void adicionaEventoCliqueRecyclerView(){

        recyclerRequisicoes.addOnItemTouchListener(
                new RecyclerItemClickListener(getApplicationContext(),
                        recyclerRequisicoes,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                Requisicao requisicao = listaRequisicoes.get(position);
                                // recuperarLocalizacaoUsuarioNovo();
                                abrirTelaCorrida(requisicao.getId(), requisicao.getStatus(), motorista, requisicao.getPassageiro(), true);
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                            }
                        }));

    }


        private void abrirTelaCorrida(String idRequisicao, String status, Usuario motorista, Usuario passageiro, boolean requisicaoAtiva){
            Intent i = new Intent(RequisicoesActivity.this, CorridaActivity.class );
            i.putExtra("idRequisicao", idRequisicao);
            i.putExtra("motorista", motorista);
            i.putExtra("passageiro", passageiro);
            i.putExtra("requisicaoAtiva", requisicaoAtiva);
            i.putExtra("status", status);
            startActivity(i);

        }

    @Override
    protected void onStart() {
        super.onStart();
        verificaStatusRequisicao();
    }

    private void verificaStatusRequisicao(){

        Usuario usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        DatabaseReference requisicao = firebaseRef.child("requisicoes");
        Query requisicoesPesquisa = requisicao.orderByChild("motorista/id")
                .equalTo(usuarioLogado.getId());
        requisicoesPesquisa.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    Requisicao requisicao = ds.getValue(Requisicao.class);
                    if(requisicao.getStatus().equals(Requisicao.STATUS_A_CAMINHO)
                    || requisicao.getStatus().equals(Requisicao.STATUS_VIAGEM)
                    || requisicao.getStatus().equals(Requisicao.STATUS_FINALIZADA)){
                        motorista = requisicao.getMotorista();
                        abrirTelaCorrida(requisicao.getId(), requisicao.getStatus(), motorista, requisicao.getPassageiro(), true);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void recuperarLocalizacaoUsuario() {

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                //recuperar latitude e longitude
                String latitude = String.valueOf(location.getLatitude());
                String longitude = String.valueOf(location.getLongitude());

                //Atualizar GeoFire
                UsuarioFirebase.atualizarDadosLocalizacao(
                        location.getLatitude(),
                        location.getLongitude()
                );

                if( !latitude.isEmpty() && !longitude.isEmpty() ){
                    motorista.setLatitude(latitude);
                    motorista.setLongitude(longitude);

                   // adicionaEventoCliqueRecyclerView();
                    locationManager.removeUpdates(locationListener);
                    adapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        //Solicitar atualizações de localização
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0,
                    0,
                    locationListener
            );
        }


    }

    private void recuperarLocalizacaoUsuarioNovo() {
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                motorista.setLatitude(String.valueOf(lastKnownLocation.getLatitude()));
                                motorista.setLongitude(String.valueOf(lastKnownLocation.getLongitude()));
                                adapter.notifyDataSetChanged();
                                UsuarioFirebase.atualizarDadosLocalizacao(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
//                                mMap.addMarker(
//                                        new MarkerOptions()
//                                                .position(new LatLng(lastKnownLocation.getLatitude(),
//                                                        lastKnownLocation.getLongitude())).title("Meu Local")
//                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.usuario))
//                                );
//                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
//                                        new LatLng(lastKnownLocation.getLatitude(),
//                                                lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }
                        } else {
                            //  Log.d(TAG, "Current location is null. Using defaults.");
//                            //   Log.e(TAG, "Exception: %s", task.getException());
//                            mMap.moveCamera(CameraUpdateFactory
//                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
//                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }

    }


    private void recuperarRequisicoes(){
        DatabaseReference requisicoes = fireBaseRef.child("requisicoes");
        Query requisicaoPesquisa = requisicoes.orderByChild("status").equalTo(Requisicao.STATUS_AGUARDANDO);

        requisicaoPesquisa.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getChildrenCount()>0){
                    textResultado.setVisibility(View.GONE);
                    recyclerRequisicoes.setVisibility(View.VISIBLE);
                    listaRequisicoes.clear();
                    for(DataSnapshot ds : snapshot.getChildren()){
                        Requisicao requisicao = ds.getValue(Requisicao.class);
                        listaRequisicoes.add(requisicao);
                    }

                }else{

                    textResultado.setVisibility(View.VISIBLE);
                    recyclerRequisicoes.setVisibility(View.GONE);

                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public boolean   onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.menuSair:
                autenticacao.signOut();
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);

    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }
}