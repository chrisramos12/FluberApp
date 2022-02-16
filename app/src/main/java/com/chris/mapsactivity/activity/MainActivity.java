package com.chris.mapsactivity.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import com.chris.mapsactivity.R;
import com.chris.mapsactivity.config.ConfiguracaoFirebase;
import com.chris.mapsactivity.helper.UsuarioFirebase;
import com.chris.mapsactivity.helper.permissoes;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth autenticacao;
    private String[] Permissoes = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private String[] netPermissao = new String[]{
            Manifest.permission.LOCATION_HARDWARE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Remover action bar (nome do aplicativo em cima
        getSupportActionBar().hide();

        //Validar Permissoes
        permissoes.validarPermissoes(Permissoes,this,1);


        //autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
       // autenticacao.signOut();
    }
    public void abrirTelaLogin(View view){
        startActivity(new Intent(this,LoginActivity.class));
    }
    public void abrirTelaCadastro(View view){
        startActivity(new Intent(this,CadastroActivity.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        UsuarioFirebase.redirecionaUsuarioLogado(MainActivity.this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int permissaoResultado : grantResults){
            if(permissaoResultado== PackageManager.PERMISSION_DENIED){
                alertaValidacaoPermissao();
            }
        }
    }
    private void alertaValidacaoPermissao(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões Negadas");
        builder.setMessage("Para utilizar o app é necessário aceitar as permissões!");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }
}