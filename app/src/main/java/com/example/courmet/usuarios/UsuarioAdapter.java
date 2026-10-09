package com.example.courmet.usuarios;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.courmet.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter da lista de usuários (mesma ideia do ProdutoAdapter):
 * - getItemCount: quantos cartões existem;
 * - onCreateViewHolder: cria (infla) o layout de um cartão;
 * - onBindViewHolder: preenche um cartão com os dados de um usuário.
 */
public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder> {

    /** A Activity implementa esta interface para saber qual usuário foi tocado. */
    public interface OnUsuarioClickListener {
        void onUsuarioClick(Usuario usuario);
    }

    private final List<Usuario> itens = new ArrayList<>();
    private final OnUsuarioClickListener listener;

    public UsuarioAdapter(OnUsuarioClickListener listener) {
        this.listener = listener;
    }

    /** Troca os itens exibidos e manda a RecyclerView redesenhar a lista inteira. */
    public void setItens(List<Usuario> novosItens) {
        itens.clear();
        itens.addAll(novosItens);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_usuario, parent, false);
        return new UsuarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsuarioViewHolder holder, int position) {
        Usuario usuario = itens.get(position);

        holder.textIniciais.setText(usuario.getIniciais());
        holder.textNome.setText(usuario.getNome());
        holder.textEmail.setText(usuario.getEmail());

        // Telefone é opcional: esconde o TextView se estiver vazio
        String telefone = usuario.getTelefone();
        boolean temTelefone = telefone != null && !telefone.trim().isEmpty();
        holder.textTelefone.setText(temTelefone ? telefone : "");
        holder.textTelefone.setVisibility(temTelefone ? View.VISIBLE : View.GONE);

        // O cartão inteiro é clicável; avisamos a Activity qual usuário foi tocado
        holder.itemView.setOnClickListener(v -> listener.onUsuarioClick(usuario));
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    /** Guarda as views de um cartão, para não chamar findViewById a cada rolagem. */
    static class UsuarioViewHolder extends RecyclerView.ViewHolder {
        final TextView textIniciais;
        final TextView textNome;
        final TextView textEmail;
        final TextView textTelefone;

        UsuarioViewHolder(@NonNull View itemView) {
            super(itemView);
            textIniciais = itemView.findViewById(R.id.textIniciais);
            textNome = itemView.findViewById(R.id.textNome);
            textEmail = itemView.findViewById(R.id.textEmail);
            textTelefone = itemView.findViewById(R.id.textTelefone);
        }
    }
}
