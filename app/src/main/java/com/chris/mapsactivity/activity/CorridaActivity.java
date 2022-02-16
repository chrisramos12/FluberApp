package com.chris.mapsactivity.activity;

import static com.chris.mapsactivity.R.menu.menu_main;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.chris.mapsactivity.R;
import com.chris.mapsactivity.config.ConfiguracaoFirebase;
import com.chris.mapsactivity.databinding.ActivityCorridaBinding;
import com.chris.mapsactivity.databinding.ActivityPassageiroBinding;
import com.chris.mapsactivity.model.Destino;
import com.chris.mapsactivity.model.Requisicao;
import com.chris.mapsactivity.model.Usuario;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;


public class CorridaActivity<OnCreateOptionsMenu>  extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private LatLng localMotorista;
    private LatLng localPassageiro;
    private LatLng localDestino;

    private FirebaseAuth autenticacao;
    private DatabaseReference firebaseRef;
    private ActivityCorridaBinding binding;

    private Marker marcadorMotorista;
    private Marker marcadorPassageiro;
    private Marker marcadorDestino;
    private Button buttonAceitarCorrida;
    private Usuario motorista;
    private Usuario passageiro;
    private Destino destino;
    private String idRequisicao;
    private Requisicao requisicao;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setSupportActionBar(binding.toolbar);

        inicializarComponentes();

        if (getIntent().getExtras().containsKey("idRequisicao") && getIntent()
                .getExtras().containsKey("motorista")){

            Bundle extras = getIntent().getExtras();
            motorista = (Usuario) extras.getSerializable("motorista");
            idRequisicao = extras.getString("idRequisicao");
            verificaStatusRequisicao();
        }
    }

    private void verificaStatusRequisicao() {

        DatabaseReference requisicoes = firebaseRef.child("requisicoes")
                .child(idRequisicao);
        requisicoes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //Recupera requisicao
                requisicao = dataSnapshot.getValue(Requisicao.class);
                passageiro = requisicao.getPassageiro();
                destino = requisicao.getDestino();

                localPassageiro = new LatLng(
                        Double.parseDouble(passageiro.getLatitude()),
                        Double.parseDouble(passageiro.getLongitude())


                );
                localDestino = new LatLng(
                        Double.parseDouble(destino.getLatitude()),
                        Double.parseDouble(destino.getLongitude())
                );
                switch (requisicao.getStatus()){
                    case Requisicao.STATUS_AGUARDANDO:

                        requisicaoAguardando();
                        break;

                    case Requisicao.STATUS_A_CAMINHO:
                        requisicaoACaminho();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void requisicaoAguardando(){

        buttonAceitarCorrida.setText("Aceitar corrida");
        adicionaMarcadorPassageiro(localPassageiro,passageiro.getNome());
        adicionaMarcadorDestino(localDestino,destino.getRua()+" "+destino.getNumero()+" "+destino.getBairro());
        centralizarDoisMarcadores(marcadorPassageiro,marcadorDestino);
    }
    private void requisicaoACaminho(){

        buttonAceitarCorrida.setText("Ao encontro do passageiro");
        recuperarLocalizacaoUsuario();
        adicionaMarcadorPassageiro(localPassageiro,passageiro.getNome());
//        centralizarDoisMarcadores(marcadorMotorista, marcadorPassageiro);




    }
//    private void adicionaMarcadorMotorista(LatLng localizacao, String titulo){
//        if (marcadorMotorista != null)
//            marcadorMotorista.remove();
//        marcadorMotorista=mMap.addMarker(
//                new MarkerOptions()
//                        .position(localizacao)
//                        .title(titulo)
//                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.carro))
//        );
//        mMap.moveCamera(
//                CameraUpdateFactory.newLatLngZoom(localizacao, 17)
//        );
//
//    }
    private void adicionaMarcadorPassageiro(LatLng localizacao, String pax){
        if (marcadorPassageiro != null)
            marcadorPassageiro.remove();
        marcadorPassageiro=mMap.addMarker(
                new MarkerOptions()
                        .position(localizacao)
                        .title(pax)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.usuario))
        );
//        mMap.moveCamera(
//                CameraUpdateFactory.newLatLngZoom(localizacao, 13)
//        );

    }
    private void adicionaMarcadorDestino(LatLng localizacao, String pax){
        if (marcadorDestino != null)
            marcadorDestino.remove();
        marcadorDestino=mMap.addMarker(
                new MarkerOptions()
                        .position(localizacao)
                        .title(pax)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.destino))
        );
//        mMap.moveCamera(
//                CameraUpdateFactory.newLatLngZoom(localizacao, 13)
//        );

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.menuSair :
                autenticacao.signOut();
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
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

        //recuperar localização do usuario
        //recuperarLocalizacaoUsuario();
        // Add a marker in Sydney and move the camera
//        adicionaMarcadorPassageiro(localPassageiro, passageiro.getNome());

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
                if (marcadorMotorista!= null)
                    marcadorMotorista.remove();

                mMap.clear();
               marcadorMotorista = mMap.addMarker(
                        new MarkerOptions()
                                .position(localMotorista)
                                .title(motorista.getNome())
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.carro))
              );
                mMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(localMotorista, 17)
                );
                adicionaMarcadorPassageiro(localPassageiro,passageiro.getNome());
                centralizarDoisMarcadores(marcadorPassageiro, marcadorMotorista);

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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    500,
                    5,
                    locationListener
            );
        }


    }
    private void centralizarDoisMarcadores(Marker marcador1, Marker marcador2){

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(marcador1.getPosition());
        builder.include(marcador2.getPosition());

        LatLngBounds bounds = builder.build();

        int largura = getResources().getDisplayMetrics().widthPixels;
        int altura = getResources().getDisplayMetrics().heightPixels;
        int espacoInterno = (int)(largura*0.20);

        mMap.moveCamera(
                CameraUpdateFactory.newLatLngBounds(
                        bounds, largura, altura, espacoInterno

                )
        );

    }

    public void aceitarCorrida(View view) {

        //configurar requisicao
        requisicao = new Requisicao();
        requisicao.setId(idRequisicao);
        requisicao.setMotorista(motorista);
        requisicao.setStatus(Requisicao.STATUS_A_CAMINHO);

        requisicao.atualizar();


    }
    //geocoder
    public void geocoder(){

    }

    private void inicializarComponentes(){

        //inicializar componentes
        setContentView(R.layout.activity_corrida);
        firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();



        buttonAceitarCorrida = findViewById(R.id.buttonAceitarCorrida);
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


}