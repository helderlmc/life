package com.lifeapp.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.location.LocationListener;
import android.location.LocationManager;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.OnMapsSdkInitializedCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.net.PlacesClient;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

//import com.lifeapp.activity.databinding.ActivityCorridaBinding;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.lifeapp.R;
import com.lifeapp.config.ConfiguracaoFirebase;
import com.lifeapp.databinding.ActivityCorridaBinding;
import com.lifeapp.helper.Local;
import com.lifeapp.helper.UsuarioFirebase;
import com.lifeapp.model.Requisicao;
import com.lifeapp.model.Usuario;

import java.text.DecimalFormat;

public class CorridaActivity extends AppCompatActivity implements OnMapReadyCallback, OnMapsSdkInitializedCallback {
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 18;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private AppBarConfiguration appBarConfiguration;
    private ActivityCorridaBinding binding;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location lastKnownLocation;

    private boolean locationPermissionGranted;
    private PlacesClient placesClient;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private Usuario motorista;
    private Usuario passageiro;
    private LatLng localMotorista;
    private LatLng localPassageiro;
    private LatLng localDestino;
    private String idRequisicao;
    private Requisicao requisicao;
    Circle circulo;
    private DatabaseReference fireBaseRef;
    private Button buttonAceitarCorrida;
    private Marker marcadorMotorista;
    private Marker marcadorPassageiro;
    private Marker marcadorDestino;
    private String statusRequisicao;
    private boolean requisicaoAtiva;
    private FloatingActionButton fabRota;
    GeoQuery geoQuery;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapsInitializer.initialize(getApplicationContext(), MapsInitializer.Renderer.LATEST, this);
        binding = ActivityCorridaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

      //  setSupportActionBar(binding.toolbar);
       // binding.toolbar.setTitle("Iniciar Corrida");
      //  NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_corrida);
//        NavController navController = Navigation.findNavController(this, R.id.map);
//        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
//
//        binding.fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
        buttonAceitarCorrida = findViewById(R.id.buttonAceitarCorrida);

        fabRota = findViewById(R.id.fabRota);
        fabRota.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String status = statusRequisicao;
                if(status!= null && !status.isEmpty()){

                    String lat = "";
                    String lon = "";

                    switch (status){
                        case Requisicao.STATUS_VIAGEM:
                            lat = String.valueOf(requisicao.getDestino().getLatitude());
                            lon = String.valueOf(requisicao.getDestino().getLongitude());
                            break;

                        case Requisicao.STATUS_A_CAMINHO :
                            lat = String.valueOf(localPassageiro.latitude);
                            lon = String.valueOf(localPassageiro.longitude);
                            break;

                    }

                    Uri uri = Uri.parse("google.navigation:q="+lat+","+lon+"&mode=d");
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, uri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);


                }


            }
        });
        fireBaseRef = ConfiguracaoFirebase.getFirebaseDataBase();

        //placesClient = Places.createClient(this);

        fusedLocationProviderClient  = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map2);
        mapFragment.getMapAsync(this);

        if( getIntent().getExtras().containsKey("idRequisicao")
        && getIntent().getExtras().containsKey("motorista") ) {
            Bundle extras = getIntent().getExtras();
            motorista = (Usuario) extras.getSerializable("motorista");
            //lastKnownLocation = Location.CREATOR.;
            if(motorista!=null && motorista.getLatitude()!= null && motorista.getLongitude()!=null){
                localMotorista = new LatLng(Double.parseDouble(motorista.getLatitude()), Double.parseDouble(motorista.getLongitude()));
            }
            passageiro = (Usuario)extras.getSerializable("passageiro");

            idRequisicao =  extras.getString("idRequisicao");
            requisicaoAtiva = extras.getBoolean("requisicaoAtiva");
            statusRequisicao = extras.getString("status");
          //  verificaStatusRequisicao();

        }

    }

    private void verificaStatusRequisicao() {
        DatabaseReference requisicoes = fireBaseRef.child("requisicoes").child(idRequisicao);
        requisicoes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                requisicao = snapshot.getValue(Requisicao.class);
                if(requisicao != null){
                    passageiro = requisicao.getPassageiro();
                    localPassageiro = new LatLng(
                            Double.parseDouble(passageiro.getLatitude()),
                            Double.parseDouble(passageiro.getLongitude())
                    );
                    localDestino =  new LatLng(
                            Double.parseDouble(requisicao.getDestino().getLatitude()),
                            Double.parseDouble(requisicao.getDestino().getLongitude())
                    );
                    statusRequisicao = requisicao.getStatus();
                    alteraInterfaceStatusRequisicao(statusRequisicao);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void iniciarMonitoramentoCorrida(Usuario uOrigem, LatLng destino,  String status){


        DatabaseReference localUsuario = ConfiguracaoFirebase.getFirebaseDataBase()
                .child("local_usuario");
        GeoFire geoFire = new GeoFire(localUsuario);
         circulo = mMap.addCircle(
                new CircleOptions()
                        .center(destino)
                        .radius(25)
                        .fillColor(Color.argb(90,255,153,0))
                        .strokeColor(Color.argb(190,255,152,0))
        );


            geoQuery = geoFire.queryAtLocation(
                    new GeoLocation(destino.latitude, destino.longitude),
                    0.025
            );
            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(String key, GeoLocation location) {
                    if( key.equals(uOrigem.getId())){
                    //    Log.d("TESTE","MOTORISTA EM DENTRO DA AREA");

                        requisicao.setStatus(status);
                        requisicao.atualizarStatus();
                        circulo.setVisible(false);
                        circulo.remove();
                        if(marcadorPassageiro!=null)
                        marcadorPassageiro.remove();
                        if(marcadorDestino!=null)
                            marcadorDestino.remove();
                      //  buttonAceitarCorrida.setText("Viagem em progresso");
                        geoQuery.removeAllListeners();
                    }
                }

                @Override
                public void onKeyExited(String key) {
                    Log.d("onKeyExited","onKeyExited");
                }

                @Override
                public void onKeyMoved(String key, GeoLocation location) {
                    Log.d("onKeyMoved","onKeyMoved");
                }

                @Override
                public void onGeoQueryReady() {

                }

                @Override
                public void onGeoQueryError(DatabaseError error) {

                }
            });





    }

    private void centralizarMarcadores( ) {

        LatLngBounds.Builder builder =   LatLngBounds.builder();
        if(marcadorMotorista!=null)
        builder.include(marcadorMotorista.getPosition());
        if(marcadorPassageiro !=null)
        builder.include(marcadorPassageiro.getPosition());
        if(marcadorDestino !=null)
            builder.include(marcadorDestino.getPosition());

        LatLngBounds bounds = builder.build();
        int largura = getResources().getDisplayMetrics().widthPixels;
        int altura = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (largura* 0.20);
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,largura, altura, padding));

//        mMap.moveCamera(
//                CameraUpdateFactory.newLatLngZoom(localMotorista, 20)
//        );


    }

    private void adicionarMarcadorMotorista() {

        if(marcadorMotorista != null){
            marcadorMotorista.remove();
        }


        if(localMotorista!=null){

            marcadorMotorista = mMap.addMarker(
                    new MarkerOptions()
                            .position(localMotorista).title(motorista.getNome())
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.carro))
            );
        }
        else if(lastKnownLocation!=null){
            marcadorMotorista = mMap.addMarker(
                    new MarkerOptions()
                            .position(new LatLng(lastKnownLocation.getLatitude(),
                                    lastKnownLocation.getLongitude())).title(motorista.getNome())
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.carro))
            );
        }



//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
//                new LatLng(lastKnownLocation.getLatitude(),
//                        lastKnownLocation.getLongitude()), DEFAULT_ZOOM)
//        );


    }

    private void alteraInterfaceStatusRequisicao(String status){

        switch (status){
            case Requisicao.STATUS_AGUARDANDO :
                buttonAceitarCorrida.setText("Aceitar Corrida");
               // adicionarMarcadorMotorista();
                marcadorDestino =null;
                adicionarMarcadorPassageiro();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(localPassageiro, 20));
                break;

            case Requisicao.STATUS_A_CAMINHO :
                buttonAceitarCorrida.setText("A caminho do Passageiro");
                marcadorDestino =null;
                adicionarMarcadorMotorista();
                adicionarMarcadorPassageiro();
                centralizarMarcadores();
                LatLng destino = new LatLng(
                    Double.parseDouble( passageiro.getLatitude())  ,
                    Double.parseDouble( passageiro.getLongitude())
                );
                iniciarMonitoramentoCorrida(motorista, localPassageiro, Requisicao.STATUS_VIAGEM);
                fabRota.setVisibility(View.VISIBLE);
                break;

            case Requisicao.STATUS_VIAGEM:
                fabRota.setVisibility(View.VISIBLE);
                buttonAceitarCorrida.setText("A caminho do destino");
                marcadorPassageiro = null;
                adicionarMarcadorMotorista();
                adicionarMarcadorDestino();
                centralizarMarcadores();
                LatLng destinoViagem = new LatLng(
                        Double.parseDouble( requisicao.getDestino().getLatitude())  ,
                        Double.parseDouble( requisicao.getDestino().getLongitude())
                );
                iniciarMonitoramentoCorrida(motorista, destinoViagem, Requisicao.STATUS_FINALIZADA);

                break;

            case Requisicao.STATUS_FINALIZADA:
                fabRota.setVisibility(View.GONE);
                buttonAceitarCorrida.setText("A caminho do destino");
                marcadorPassageiro = null;
                marcadorDestino =null;
                adicionarMarcadorMotorista();
                centralizarMarcadores();
                requisicaoAtiva = false;
                float distancia = Local.calcularDistancia(localPassageiro, localDestino);
                float valor = distancia * 2;
                DecimalFormat decimal = new DecimalFormat("0.00");
                buttonAceitarCorrida.setText("Corrida Finalizada - R$ "+ decimal.format(valor));

                break;


            case Requisicao.STATUS_CANCELADA:
                requisicaoCancelada();
                break;
        }


    }


    private void requisicaoCancelada(){

        Toast.makeText(this,
                "Requisição foi cancelada pelo passageiro!",
                Toast.LENGTH_SHORT).show();

        startActivity(new Intent(CorridaActivity.this, RequisicoesActivity.class));

    }
    private void adicionarMarcadorPassageiro() {

        if(marcadorPassageiro != null){
            marcadorPassageiro.remove();
        }

        marcadorPassageiro = mMap.addMarker(
                new MarkerOptions()
                        .position(localPassageiro).title(passageiro.getNome())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.usuario))
        );
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
//                localPassageiro, DEFAULT_ZOOM));


    }

    private void adicionarMarcadorDestino() {

        if(marcadorDestino != null){
            marcadorDestino.remove();
        }
          LatLng  localDestino = new LatLng(
                  Double.parseDouble(requisicao.getDestino().getLatitude()),
                  Double.parseDouble(requisicao.getDestino().getLongitude())
          );
        marcadorDestino = mMap.addMarker(
                new MarkerOptions()
                        .position(localDestino).title("Destino")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.destino))
        );
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
//                localPassageiro, DEFAULT_ZOOM));


    }


    public void aceitarCorrida(View view){

        if(lastKnownLocation==null){
            if(localMotorista!=null){
                motorista.setLatitude(String.valueOf(localMotorista.latitude));
                motorista.setLongitude(String.valueOf(localMotorista.longitude));
            }

        }else{
            motorista.setLatitude(String.valueOf(lastKnownLocation.getLatitude()));
            motorista.setLongitude(String.valueOf(lastKnownLocation.getLongitude()));

        }

        requisicao = new Requisicao();
        requisicao.setId(idRequisicao);
        requisicao.setMotorista(motorista);
        requisicao.setStatus(Requisicao.STATUS_A_CAMINHO);
        requisicao.atualizar();


    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_corrida);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);

        }
        // object

        // mFusedLocationClient.getLastLocation().getResult().getLatitude()
        //Location local = mMap.getMyLocation();
//        LatLng meuLocal = new LatLng(mFusedLocationClient.getLastLocation().getResult().getLatitude(), mFusedLocationClient.getLastLocation().getResult().getLongitude());
        // Add a marker in Sydney and move the camera
        //  LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //  mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,18));
        //   mMap.moveCamera(CameraUpdateFactory.newLatLng(meuLocal));

        //   recuperarLocalizacaoUsuario();

//        Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
//        locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
//            @Override
//            public void onComplete(@NonNull Task<Location> task) {
//                if (task.isSuccessful()) {
//                    // Set the map's camera position to the current location of the device.
//                    Location lastKnownLocation = task.getResult();
//                    if (lastKnownLocation != null) {
//                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
//                                new LatLng(lastKnownLocation.getLatitude(),
//                                        lastKnownLocation.getLongitude()), 18));
//                    }
//                }
//            }
//        });
        recuperarLocalizacaoUsuario();
        getLocationPermission();
        recuperarLocalizacaoUsuarioNovo();
        verificaStatusRequisicao();
       // iniciarMonitoramentoCorrida(passageiro, motorista);


    }


    private void recuperarLocalizacaoUsuario() {

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                //recuperar latitude e longitude
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                localMotorista = new LatLng(latitude, longitude);
                UsuarioFirebase.atualizarDadosLocalizacao(latitude, longitude);
                motorista.setLatitude(String.valueOf(latitude));
                motorista.setLongitude(String.valueOf(longitude));
                requisicao.setMotorista(motorista);
                requisicao.atualizarLocalizacaoMotorista();
                alteraInterfaceStatusRequisicao(statusRequisicao);

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
                    10000,
                    10,
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
                          //  alteraInterfaceSatatusRequisicao(statusRequisicao);
                            if (lastKnownLocation != null) {
                                UsuarioFirebase.atualizarDadosLocalizacao(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
//                                marcadorMotorista= mMap.addMarker(
//                                        new MarkerOptions()
//                                                .position(new LatLng(lastKnownLocation.getLatitude(),
//                                                        lastKnownLocation.getLongitude())).title("Meu Local")
//                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.carro))
//                                );
//                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
//                                        new LatLng(lastKnownLocation.getLatitude(),
//                                                lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                //verificaStatusRequisicao();
                            }
                        } else {
                            //  Log.d(TAG, "Current location is null. Using defaults.");
                            //   Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }

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


    @Override
    public void onMapsSdkInitialized(@NonNull MapsInitializer.Renderer renderer) {
        switch (renderer) {
            case LATEST:
                Log.d("MapsDemo", "The latest version of the renderer is used.");
                break;
            case LEGACY:
                Log.d("MapsDemo", "The legacy version of the renderer is used.");
                break;
        }

    }

//    @Override
//    public void onSupportNavigateUp(@NonNull TaskStackBuilder builder) {
//        super.onCreateSupportNavigateUpTaskStack(builder);
//    }
}