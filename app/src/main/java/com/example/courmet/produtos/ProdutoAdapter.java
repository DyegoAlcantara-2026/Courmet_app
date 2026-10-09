package com.example.courmet.produtos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.courmet.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter da lista de produtos.
 *
 * Como a RecyclerView funciona, em resumo:
 * - A RecyclerView só sabe rolar e reciclar views; ela pergunta tudo ao Adapter.
 * - O Adapter diz quantos itens existem (getItemCount), cria o layout de um item
 *   (onCreateViewHolder) e preenche esse layout com os dados de uma posição (onBindViewHolder).
 * - O ViewHolder guarda as referências às views de um item, para não chamar
 *   findViewById toda vez que um item é reaproveitado ao rolar.
 */
public class ProdutoAdapter extends RecyclerView.Adapter<ProdutoAdapter.ProdutoViewHolder> {

    /** Quem usa o Adapter (a Activity) implementa esta interface para saber qual item foi tocado. */
    public interface OnProdutoClickListener {
        void onProdutoClick(Produto produto);
    }

    private final List<Produto> itens = new ArrayList<>();
    private final OnProdutoClickListener listener;

    // Formata 180.0 como "R$ 180,00" (moeda brasileira)
    private final NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    public ProdutoAdapter(OnProdutoClickListener listener) {
        this.listener = listener;
    }

    /** Troca os itens exibidos e manda a RecyclerView redesenhar a lista inteira. */
    public void setItens(List<Produto> novosItens) {
        itens.clear();
        itens.addAll(novosItens);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProdutoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // "Infla" o XML item_produto e transforma em objetos View
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_produto, parent, false);
        return new ProdutoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProdutoViewHolder holder, int position) {
        Produto produto = itens.get(position);

        holder.textNome.setText(produto.getNome());
        holder.textPreco.setText(formatoMoeda.format(produto.getPreco()));
        holder.textCategoria.setText(produto.isBebida()
                ? R.string.produto_list_badge_bebida
                : R.string.produto_list_badge_comida);

        // Descrição é opcional: esconde o TextView se estiver vazia
        String descricao = produto.getDescricao();
        boolean temDescricao = descricao != null && !descricao.trim().isEmpty();
        holder.textDescricao.setText(temDescricao ? descricao : "");
        holder.textDescricao.setVisibility(temDescricao ? View.VISIBLE : View.GONE);

        // O cartão inteiro é clicável; avisamos a Activity qual produto foi tocado
        holder.itemView.setOnClickListener(v -> listener.onProdutoClick(produto));
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    /** Guarda as views de um cartão (um item da lista). */
    static class ProdutoViewHolder extends RecyclerView.ViewHolder {
        final TextView textNome;
        final TextView textPreco;
        final TextView textCategoria;
        final TextView textDescricao;

        ProdutoViewHolder(@NonNull View itemView) {
            super(itemView);
            textNome = itemView.findViewById(R.id.textNome);
            textPreco = itemView.findViewById(R.id.textPreco);
            textCategoria = itemView.findViewById(R.id.textCategoria);
            textDescricao = itemView.findViewById(R.id.textDescricao);
        }
    }
}
