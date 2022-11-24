package com.lifeapp.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.OnMapsSdkInitializedCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
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
import com.lifeapp.config.ConfiguracaoFirebase;
import com.lifeapp.databinding.ActivityPassageiroBinding;
import com.lifeapp.helper.Local;
import com.lifeapp.helper.UsuarioFirebase;
import com.lifeapp.model.Destino;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import androidx.activity.result.ActivityResult;
//import com.lifeapp.activity.databinding.ActivityPassageiroBinding;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AddressComponent;
import com.google.android.libraries.places.api.model.AddressComponents;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.lifeapp.model.Requisicao;
import com.lifeapp.model.Usuario;

public class PassageiroActivity extends AppCompatActivity implements OnMapReadyCallback, OnMapsSdkInitializedCallback {

    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 18;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;


    private GoogleMap mMap;
    private FirebaseAuth autenticacao;

    private AppBarConfiguration appBarConfiguration;
    private ActivityPassageiroBinding binding;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location lastKnownLocation;

    private boolean locationPermissionGranted;

    private Requisicao requisicao;
    private DatabaseReference fireBaseRef;
    private boolean cancelarUber = false;
    private LinearLayout linearLayoutDestino;
    private Button buttonChamarMotorista;
    private EditText editDestino;
    private PlacesClient placesClient;
    private LatLng localPassageiro;
    private Usuario passageiro;
    private String statusRequisicao;
    private Destino destino;
    private LatLng localDestino;
    private Marker marcadorMotorista;
    private Marker marcadorPassageiro;
    private Marker marcadorDestino;
    private Usuario motorista;
    private LatLng localMotorista;

    // [START maps_solutions_android_autocomplete_define]
    private final ActivityResultLauncher<Intent> startAutocomplete = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            (ActivityResultCallback<ActivityResult>) result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent intent = result.getData();
                    if (intent != null) {
                        Place place = Autocomplete.getPlaceFromIntent(intent);

                        // Write a method to read the address components from the Place
                        // and populate the form with the address components
                      //  Log.d(TAG, "Place: " + place.getAddressComponents());
                        fillInAddress(place);
                    }
                } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                    // The user canceled the operation.
                 //   Log.i(TAG, "User canceled autocomplete");
                }
            });
    // [END maps_solutions_android_autocomplete_define]



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapsInitializer.initialize(getApplicationContext(), MapsInitializer.Renderer.LATEST, this);


        binding = ActivityPassageiroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        //  binding.fab.hide();
        binding.toolbar.setTitle("Iniciar nova Viagem");
//        binding = ActivityPassageiroBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
//
//        setSupportActionBar(binding.toolbar);


        //  NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_passageiro);
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

        placesClient = Places.createClient(this);

        editDestino = findViewById(R.id.editDestino);
      //  editDestino.setOnClickListener(v -> startAutocompleteIntent());

        linearLayoutDestino = findViewById(R.id.blocoDestino);

        buttonChamarMotorista = findViewById(R.id.button6);

        fireBaseRef = ConfiguracaoFirebase.getFirebaseDataBase();

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        fusedLocationProviderClient  = LocationServices.getFusedLocationProviderClient(this);



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        verificaStatusRequisicao();


    }

    private void verificaStatusRequisicao(){

        Usuario usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();
        DatabaseReference requisicoes = fireBaseRef.child("requisicoes");
        Query requisicaoPesquisa = requisicoes.orderByChild("passageiro/id").equalTo(usuarioLogado.getId());
      //  requisicaoPesquisa.orderByChild("status").equalTo("aguardando");
        requisicaoPesquisa.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                for(DataSnapshot ds : snapshot.getChildren()){
                    requisicao = ds.getValue(Requisicao.class);
                }

               //Collections.reverse(lista);
                if(requisicao!=null){

                    if(requisicao != null) {
                        if (!requisicao.getStatus().equals(Requisicao.STATUS_ENCERRADA)) {


                            passageiro = requisicao.getPassageiro();
                            localPassageiro = new LatLng(
                                    Double.parseDouble(passageiro.getLatitude()),
                                    Double.parseDouble(passageiro.getLongitude())
                            );
                            localDestino = new LatLng(
                                    Double.parseDouble(requisicao.getDestino().getLatitude()),
                                    Double.parseDouble(requisicao.getDestino().getLongitude())
                            );
                            statusRequisicao = requisicao.getStatus();
                            destino = requisicao.getDestino();
                            if (requisicao.getMotorista() != null) {

                                motorista = requisicao.getMotorista();
                                localMotorista = new LatLng(
                                        Double.parseDouble(motorista.getLatitude()),
                                        Double.parseDouble(motorista.getLongitude())
                                );

                            }
                            alteraInterfaceStatusRequisicao(statusRequisicao);
                    }
                    }


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void alteraInterfaceStatusRequisicao(String status) {

        if(status!=null && !status.isEmpty()) {
            cancelarUber =false;
            switch (status) {
                case Requisicao.STATUS_AGUARDANDO:
                    requisicaoAguardando();
                    break;
                case Requisicao.STATUS_A_CAMINHO:
                    requisicaoACaminho();
                    break;

                case Requisicao.STATUS_VIAGEM:
                    requisicaoViagem();

                    break;

                case Requisicao.STATUS_FINALIZADA:
                    requisicaoFinalizada();
                    break;
                case Requisicao.STATUS_CANCELADA:
                    requisicaoCancelada();
                    break;



            }
        }else{
            adicionaMarcadorPassageiro(localPassageiro, "Seu Local");
            centralizarMarcador(localPassageiro);
        }
    }

    private void adicionaMarcadorPassageiro(LatLng localizacao, String titulo){

        if(marcadorPassageiro != null){
            marcadorPassageiro.remove();
        }

        marcadorPassageiro = mMap.addMarker(
                new MarkerOptions()
                        .position(localizacao)
                        .title(titulo)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.usuario))
        );
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
//                localPassageiro, DEFAULT_ZOOM));


    }

    private void adicionaMarcadorMotorista(LatLng localizacao, String titulo){

        if( marcadorMotorista != null )
            marcadorMotorista.remove();

        marcadorMotorista = mMap.addMarker(
                new MarkerOptions()
                        .position(localizacao)
                        .title(titulo)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.carro))
        );

    }

    private void adicionaMarcadorDestino(LatLng localizacao, String titulo){

        if( marcadorPassageiro != null )
            marcadorPassageiro.remove();

        if( marcadorDestino != null )
            marcadorDestino.remove();

        marcadorDestino = mMap.addMarker(
                new MarkerOptions()
                        .position(localizacao)
                        .title(titulo)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.destino))
        );

    }


    private void centralizarMarcador(LatLng local){
        mMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(local, 20)
        );
    }

    private void centralizarDoisMarcadores(Marker marcador1, Marker marcador2){

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        builder.include( marcador1.getPosition() );
        builder.include( marcador2.getPosition() );

        LatLngBounds bounds = builder.build();

        int largura = getResources().getDisplayMetrics().widthPixels;
        int altura = getResources().getDisplayMetrics().heightPixels;
        int espacoInterno = (int) (largura * 0.20);

        mMap.moveCamera(
                CameraUpdateFactory.newLatLngBounds(bounds,largura,altura,espacoInterno)
        );

    }

    private void requisicaoCancelada(){

        linearLayoutDestino.setVisibility( View.VISIBLE );
        buttonChamarMotorista.setText("Chamar Uber");
        cancelarUber = false;

    }

    private void requisicaoAguardando(){
        linearLayoutDestino.setVisibility(View.GONE);
        buttonChamarMotorista.setText("Cancelar Uber");
        cancelarUber = true;

        adicionaMarcadorPassageiro(localPassageiro, passageiro.getNome());
        centralizarMarcador(localPassageiro);
    }

    private void requisicaoACaminho(){

        linearLayoutDestino.setVisibility(View.GONE);
        buttonChamarMotorista.setText("Motorista a caminho");
        buttonChamarMotorista.setEnabled(false);
      //  motoristaChamado = true;
        adicionaMarcadorMotorista(localMotorista, motorista.getNome());
        adicionaMarcadorPassageiro(localPassageiro, passageiro.getNome());
        centralizarDoisMarcadores(marcadorMotorista, marcadorPassageiro);

    }



    private void requisicaoViagem(){

        linearLayoutDestino.setVisibility(View.GONE);
        buttonChamarMotorista.setText("A caminho do destino");
        buttonChamarMotorista.setEnabled(false);
        adicionaMarcadorPassageiro(localPassageiro, passageiro.getNome());

        adicionaMarcadorDestino(localDestino,"Destino");
        centralizarDoisMarcadores(marcadorMotorista,marcadorDestino );
        marcadorPassageiro.remove();
    }

    private void requisicaoFinalizada(){
        linearLayoutDestino.setVisibility(View.GONE);
        buttonChamarMotorista.setText("Corrida Finalizada");
        buttonChamarMotorista.setEnabled(false);
        adicionaMarcadorDestino(localDestino,"Destino");
        centralizarMarcador(localDestino);
        //Calcular distancia
        float distancia = Local.calcularDistancia(localPassageiro, localDestino);
        float valor = distancia * 8;
        DecimalFormat decimal = new DecimalFormat("0.00");
        String resultado = decimal.format(valor);

        buttonChamarMotorista.setText("Corrida finalizada - R$ " + resultado);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Total da Viagem")
                .setMessage("Sua viagem ficou: R$ " + resultado)
                .setCancelable(false)
                .setNegativeButton("Encerrar viagem", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        requisicao.setStatus(Requisicao.STATUS_ENCERRADA);
                        requisicao.atualizarStatus();
                        finish();
                        startActivity(new Intent((getIntent())));


                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        //  NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_passageiro);
        NavController navController = Navigation.findNavController(this, R.id.map);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
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

        getLocationPermission();
        recuperarLocalizacaoUsuarioNovo();
        recuperarLocalizacaoUsuario();

    }

    public void chamarMotorista(View view){


        if(cancelarUber){
            requisicao.setStatus(Requisicao.STATUS_CANCELADA);
            requisicao.atualizarStatus();
            //cancelarUber = false;


        }else{


            String enderecoDestino = editDestino.getText().toString();
            if(enderecoDestino !=null && !enderecoDestino.isEmpty()){

                Address endDestino = enderecoDestino(enderecoDestino);
                if(endDestino!=null){
                    Destino destino = new Destino();
                    destino.setCidade(endDestino.getAdminArea());
                    destino.setCep(endDestino.getPostalCode());
                    destino.setBairro(endDestino.getSubLocality());
                    destino.setRua(endDestino.getThoroughfare());
                    destino.setNumero(endDestino.getFeatureName());
                    destino.setLatitude(String.valueOf(endDestino.getLatitude()));
                    destino.setLongitude(String.valueOf(endDestino.getLongitude()));

                    StringBuilder mensagem = new StringBuilder();
                    mensagem.append("cidade: " + destino.getCidade());
                    mensagem.append("\nRua: " + destino.getRua());
                    mensagem.append("\nBairro: " + destino.getBairro());
                    mensagem.append("\nNumero: " + destino.getNumero());
                    mensagem.append("\nCep: " + destino.getCep());

                    AlertDialog.Builder builder = new AlertDialog.Builder(this)
                            .setTitle("Confirme seu endereço!")
                            .setMessage(mensagem)
                            .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    salvarRequisicao(destino);

                                }
                            })
                            .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                        }
                                    }
                            );
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }

            }else{
                Toast.makeText(this, "Informe o endereço de destino!", Toast.LENGTH_SHORT).show();
            }


        }


    }

    private void salvarRequisicao(Destino destino) {

        Requisicao requisicao = new Requisicao();
        requisicao.setDestino(destino);
        Usuario usuarioPassageiro = UsuarioFirebase.getDadosUsuarioLogado();
        usuarioPassageiro.setLatitude(String.valueOf(lastKnownLocation.getLatitude()));
        usuarioPassageiro.setLongitude(String.valueOf(lastKnownLocation.getLongitude()));
        requisicao.setPassageiro(usuarioPassageiro);
        requisicao.setStatus(Requisicao.STATUS_AGUARDANDO);
        requisicao.salvar();

        linearLayoutDestino.setVisibility(View.GONE);
        buttonChamarMotorista.setText("Cancelar Motorista");
        //cancelarUber = true;

    }

    private Address enderecoDestino(String endereco){

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> listaEnderecos = geocoder.getFromLocationName(endereco, 1);
            if(listaEnderecos!=null && !listaEnderecos.isEmpty()){
                Address adreess = listaEnderecos.get(0);
//                double lat = adreess.getLatitude();
//                double lon = adreess.getLongitude();
                return adreess;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void inicializarComponente(){

     //   editDestino = findViewById(R.id.editDestino);

    }

    private void recuperarLocalizacaoUsuario() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                LatLng meuLocal = new LatLng(latitude, longitude);
                UsuarioFirebase.atualizarDadosLocalizacao(latitude, longitude);
                alteraInterfaceStatusRequisicao(statusRequisicao);

              if(statusRequisicao != null && !statusRequisicao.isEmpty()){
                if(statusRequisicao.equals(Requisicao.STATUS_VIAGEM)
                || (statusRequisicao.equals(Requisicao.STATUS_FINALIZADA))){

                    locationManager.removeUpdates(locationListener);
                }else{

                    if (ActivityCompat.checkSelfPermission(PassageiroActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                10000,
                                10,
                                locationListener
                        );
                    }

                }

              }
//                mMap.clear();
//
//                mMap.addMarker(
//                        new MarkerOptions()
//                                .position(meuLocal).title("Meu Local")
//                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.usuario))
//                );
//                mMap.moveCamera(
//                        CameraUpdateFactory.newLatLngZoom(meuLocal, 18)
//                );

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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED  && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
            mMap.setMyLocationEnabled(true);
           // return;
            locationManager.requestLocationUpdates(
                    locationManager.GPS_PROVIDER,
                    10000,
                    10,
                    locationListener
            );
        }
       // PermissionUtils.requestLocationPermissions(this, LOCATION_PERMISSION_REQUEST_CODE, true)
        //locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 10000, 10, locationListener);
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
//                                mMap.addMarker(
//                                        new MarkerOptions()
//                                                .position(new LatLng(lastKnownLocation.getLatitude(),
//                                                        lastKnownLocation.getLongitude())).title("Meu Local")
//                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.usuario))
//                                );
//                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
//                                        new LatLng(lastKnownLocation.getLatitude(),
//                                                lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                UsuarioFirebase.atualizarDadosLocalizacao(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
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



    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }



    private void startAutocompleteIntent() {

        // Set the fields to specify which types of place data to
        // return after the user has made a selection.
        List<Place.Field> fields = Arrays.asList(Place.Field.ADDRESS_COMPONENTS,
                Place.Field.LAT_LNG, Place.Field.VIEWPORT);

        // Build the autocomplete intent with field, country, and type filters applied
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .setCountry("BR")
                .setTypeFilter(TypeFilter.ADDRESS)
                .build(this);
        startAutocomplete.launch(intent);
    }

    private void fillInAddress(Place place) {
        AddressComponents components = place.getAddressComponents();
        StringBuilder address1 = new StringBuilder();
        StringBuilder postcode = new StringBuilder();

        // Get each component of the address from the place details,
        // and then fill-in the corresponding field on the form.
        // Possible AddressComponent types are documented at https://goo.gle/32SJPM1
        if (components != null) {
            for (AddressComponent component : components.asList()) {
                String type = component.getTypes().get(0);
                switch (type) {
                    case "street_number": {
                        address1.insert(0, component.getName());
                        break;
                    }

                    case "route": {
                        address1.append(" ");
                        address1.append(component.getShortName());
                        break;
                    }

                    case "postal_code": {
                        postcode.insert(0, component.getName());
                        break;
                    }

                    case "postal_code_suffix": {
                        postcode.append("-").append(component.getName());
                        break;
                    }

//                    case "locality":
//                        cityField.setText(component.getName());
//                        break;
//
//                    case "administrative_area_level_1": {
//                        stateField.setText(component.getShortName());
//                        break;
//                    }
//
//                    case "country":
//                        countryField.setText(component.getName());
//                        break;
                }
            }
        }
        editDestino.setText(address1.toString());
      //  address1Field.setText(address1.toString());
       // postalField.setText(postcode.toString());

        // After filling the form with address components from the Autocomplete
        // prediction, set cursor focus on the second address line to encourage
        // entry of sub-premise information such as apartment, unit, or floor number.
       // address2Field.requestFocus();

        // Add a map for visual confirmation of the address
       // showMap(place);
    }
}