package com.chris.mapsactivity.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import com.chris.mapsactivity.R;
import com.chris.mapsactivity.config.ConfiguracaoFirebase;
import com.chris.mapsactivity.helper.UsuarioFirebase;
import com.chris.mapsactivity.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class CadastroActivity extends AppCompatActivity {
    private TextInputEditText campoNome, campoEmail, campoSenha;
    private Switch switchTipoUsuario;
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        //iniciar Componentes
        campoNome = findViewById(R.id.editCadastroNome);
        campoEmail = findViewById(R.id.editCadastroEmail);
        campoSenha = findViewById(R.id.editCadastroSenha);
        switchTipoUsuario = findViewById(R.id.switchTipoUsuario);
    }
    public void validarCadastroUsuario(View view){

        String textoNome = campoNome.getText().toString();
        String textoEmail = campoEmail.getText().toString();
        String textoSenha = campoSenha.getText().toString();

        if(!textoNome.isEmpty()){//verifica Nome
            if(!textoEmail.isEmpty()){//verifica Email
                if(!textoSenha.isEmpty()){//verifica Senha

                    Usuario usuario = new Usuario();
                    usuario.setNome(textoNome);
                    usuario.setEmail(textoEmail);
                    usuario.setSenha(textoSenha);

                    //switch
                    usuario.setTipo(verificaTipoUsuario());
                    cadastrarUsuario(usuario);

                }else{
                    Toast.makeText(CadastroActivity.this,"Preencha o campo senha!",Toast.LENGTH_SHORT).show();
                }

            }else{
                Toast.makeText(CadastroActivity.this,"Preencha o campo E-Mail!",Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(CadastroActivity.this,"Preencha o campo nome!",Toast.LENGTH_SHORT).show();
        }

    }
    //metodo para cadastro do usuario apos as verificações
    public void cadastrarUsuario(Usuario usuario){

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(
              usuario.getEmail(),
              usuario.getSenha()
        ).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    try {

                        String idUsuario = task.getResult().getUser().getUid();
                        usuario.setId(idUsuario);
                        usuario.salvar();

                        //Atualizar nome no UserProfile
                        UsuarioFirebase.atualizarNomeUsuario(usuario.getNome());


                        //Redireciona o usuario com base no tipo passageiro ou motorista
                        //se o usuario for passageiro chama a activity maps
                        //senão chama a activity requisições
                        if(verificaTipoUsuario() == "P"){
                            startActivity(new Intent(CadastroActivity.this, PassageiroActivity.class));
                            finish();
                            Toast.makeText(CadastroActivity.this,"Sucesso ao cadastrar Passageiro",Toast.LENGTH_SHORT).show();

                        }else{
                            startActivity(new Intent(CadastroActivity.this, RequisicoesActivity.class));
                            finish();
                            Toast.makeText(CadastroActivity.this,"Sucesso ao cadastrar Motorista",Toast.LENGTH_SHORT).show();

                        }

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else{
                   String excecao = "";
                   try {
                       throw task.getException();

                   } catch (FirebaseAuthWeakPasswordException e) {
                       excecao = "Digite uma senha mais forte";

                   } catch (FirebaseAuthInvalidCredentialsException e){
                       excecao = "Digite um email valido";
                   } catch (FirebaseAuthUserCollisionException e){
                       excecao = "Conta já cadastrada";

                   }catch (Exception e){
                       excecao = "Erro ao cadastrar usuario: "+ e.getMessage();
                       e.printStackTrace();
                   }
                   Toast.makeText(CadastroActivity.this,excecao, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    //metodo para verificar o tipo de usuario motorista ou passageiro
    public String verificaTipoUsuario(){
        return switchTipoUsuario.isChecked() ? "M":"P";
    }
}