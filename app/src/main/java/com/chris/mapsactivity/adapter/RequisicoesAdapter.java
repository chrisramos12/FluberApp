package com.chris.mapsactivity.adapter;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chris.mapsactivity.R;
import com.chris.mapsactivity.model.Requisicao;
import com.chris.mapsactivity.model.Usuario;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class RequisicoesAdapter  extends RecyclerView.Adapter<RequisicoesAdapter.MyViewHolder> {

    private List<Requisicao>requisicoes;
    private Context context;
    private Usuario motorista;


    public RequisicoesAdapter(List<Requisicao> requisicoes, Context context, Usuario motorista) {
        this.requisicoes = requisicoes;
        this.context = context;
        this.motorista = motorista;
    }

    @Override
    public MyViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_requisicoes,parent,false );
        return new MyViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Requisicao requisicao = requisicoes.get(position);
        Usuario passageiro = requisicao.getPassageiro();



        holder.nome.setText(passageiro.getNome());
        holder.destino.setText(passageiro.getLatitude());
        holder.destino.setVisibility(View.INVISIBLE);
        String destinoAdress = holder.destino.getText().toString();


        holder.origem.setText(passageiro.getLongitude());
        holder.distancia.setText("1km aproximadamente");
    }

    @Override
    public int getItemCount() {
        return requisicoes.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView nome, distancia, origem, destino;
        public MyViewHolder(View itemView){
            super(itemView);

            nome = itemView.findViewById(R.id.textRequisicaoNome);
            origem = itemView.findViewById(R.id.textRequisicaoOrigem);
            destino = itemView.findViewById(R.id.textRequisicaoDestino);
            distancia = itemView.findViewById(R.id.textRequisicaoDistancia);
        }

    }



}
