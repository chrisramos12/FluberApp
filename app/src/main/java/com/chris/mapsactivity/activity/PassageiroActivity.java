package com.chris.mapsactivity.activity;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import com.chris.mapsactivity.R;
import com.chris.mapsactivity.config.ConfiguracaoFirebase;
import com.chris.mapsactivity.databinding.ActivityPassageiroBinding;
import com.chris.mapsactivity.helper.UsuarioFirebase;
import com.chris.mapsactivity.model.Destino;
import com.chris.mapsactivity.model.Requisicao;
import com.chris.mapsactivity.model.Usuario;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PassageiroActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;

    private EditText editDestino;

    private LinearLayout linearLayoutDestino;
    private Button buttonChamarUber;
    private boolean uberChamado = false;


    private ActivityPassageiroBinding binding;
    private FirebaseAuth autenticacao;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private LatLng localPassageiro;
    private DatabaseReference firebaseRef;
    private Requisicao requisicao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPassageiroBinding.inflate(getLayoutInflater());


        inicializarComponentes();


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

        //recuperar localiza????o do usuario
        recuperarLocalizacaoUsuario();
        // Add a marker in Sydney and move the camera

    }
    private void verificaStatusRequisicao(){

        Usuario usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();
        DatabaseReference requisicoes = firebaseRef.child("requisicoes");
        Query requisicaoPesquisa = requisicoes.orderByChild("passageiro/id")
                .equalTo( usuarioLogado.getId() );

        requisicaoPesquisa.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                List<Requisicao> lista = new ArrayList<>();
                for( DataSnapshot ds: dataSnapshot.getChildren() ){
                    lista.add( ds.getValue( Requisicao.class ) );
                }

                Collections.reverse(lista);
                if(lista!=null && lista.size()>0){

                    requisicao = lista.get(0);

                    switch (requisicao.getStatus()){
                        case Requisicao.STATUS_AGUARDANDO :
                            linearLayoutDestino.setVisibility( View.GONE );
                            buttonChamarUber.setText("Cancelar Uber");
                            uberChamado = true;
                            break;
                        case Requisicao.STATUS_A_CAMINHO:
                            linearLayoutDestino.setVisibility( View.GONE );
                            buttonChamarUber.setText("Cancelar Uber");
                            uberChamado = true;
                            break;

                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
        public void chamarUber(View view){

            if(!uberChamado){

                String enderecoDestino = editDestino.getText().toString();

                if(!enderecoDestino.equals("")||enderecoDestino!=null){

                    Address addreeDestino = recuperarEndereco( enderecoDestino);
                    if (addreeDestino != null){

                        Destino destino = new Destino();
                        destino.setCidade( addreeDestino.getSubAdminArea() );
                        destino.setCep(addreeDestino.getPostalCode());
                        destino.setBairro(addreeDestino.getSubLocality());
                        destino.setRua(addreeDestino.getThoroughfare());
                        destino.setNumero(addreeDestino.getFeatureName());

                        destino.setLatitude(String.valueOf(addreeDestino.getLatitude()));
                        destino.setLongitude(String.valueOf(addreeDestino.getLongitude()));

                        StringBuilder mensagem = new StringBuilder();
                        mensagem.append("Cidade: " + destino.getCidade());
                        mensagem.append("\nRua: " + destino.getRua());
                        mensagem.append("\nBairro: " + destino.getBairro());
                        mensagem.append("\nNumero: " + destino.getNumero());
                        mensagem.append("\nCep: " + destino.getCep());

                        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                                .setTitle("Confirme seu endere??o!")
                                .setMessage(mensagem)
                                .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        //salvar requisi????o
                                        salvarRequisicao(destino);
                                        uberChamado=true;

                                    }
                                }).setNegativeButton("cancelar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                });
                        AlertDialog dialog = builder.create();
                        dialog.show();

                    }else{

                        Toast.makeText(this, "Informe o endere??o de destino!", Toast.LENGTH_SHORT).show();

                    }
                }

            }else{

                uberChamado = false;

            }


    }

    private void salvarRequisicao(Destino destino){

        Requisicao requisicao = new Requisicao();
        requisicao.setDestino(destino);

        Usuario usuarioPassageiro = UsuarioFirebase.getDadosUsuarioLogado();
        usuarioPassageiro.setLatitude(String.valueOf(localPassageiro.latitude));
        usuarioPassageiro.setLongitude(String.valueOf(localPassageiro.longitude));


        requisicao.setPassageiro(usuarioPassageiro);
        requisicao.setStatus(Requisicao.STATUS_AGUARDANDO);
        requisicao.salvar();

        linearLayoutDestino.setVisibility(View.GONE);
        buttonChamarUber.setText("Cancelar");

    }
    private Address recuperarEndereco(String endereco){

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> listaEndereco = geocoder.getFromLocationName(endereco,1);
            if (listaEndereco != null && listaEndereco.size()>0){

                Address address = listaEndereco.get(0);

                return address;

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }


    private void recuperarLocalizacaoUsuario() {

            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {

                    //recuperar latitude e longitude
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    localPassageiro = new LatLng(latitude, longitude);

                    mMap.clear();
                    mMap.addMarker(
                            new MarkerOptions()
                                    .position(localPassageiro)
                                    .title("Meu Local")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.usuario))
                    );
                    mMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(localPassageiro, 17)
                    );

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
            if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        10000,
                        10,
                        locationListener
                );
            }
 }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
    public void inicializarComponentes(){
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);


        //inicializar componentes
        editDestino=findViewById(R.id.editDestino);
        linearLayoutDestino = findViewById(R.id.linearLayoutDestino);
        buttonChamarUber = findViewById(R.id.buttonChamarUber);

        //Configura????es iniciais
        firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        verificaStatusRequisicao();


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }




}