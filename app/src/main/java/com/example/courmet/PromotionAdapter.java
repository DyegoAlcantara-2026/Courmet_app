package com.example.courmet;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter da lista de promoções (mesma ideia do ProdutoAdapter):
 * getItemCount diz quantos itens existem, onCreateViewHolder infla o layout de um cartão
 * e onBindViewHolder preenche o cartão com os dados de uma posição.
 */
public class PromotionAdapter extends RecyclerView.Adapter<PromotionAdapter.PromotionViewHolder> {

    /** A Activity implementa esta interface para saber qual promoção foi tocada. */
    public interface OnPromotionClickListener {
        void onPromotionClick(Promotion promotion);
    }

    private final List<Promotion> items = new ArrayList<>();
    private final OnPromotionClickListener listener;

    // Formata 53.4 como "R$ 53,40" (moeda brasileira)
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    public PromotionAdapter(OnPromotionClickListener listener) {
        this.listener = listener;
    }

    /** Troca os itens exibidos (usado pelo filtro de categoria) e redesenha a lista. */
    public void setItems(List<Promotion> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PromotionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_promotion, parent, false);
        return new PromotionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PromotionViewHolder holder, int position) {
        Promotion promotion = items.get(position);

        holder.imagePhoto.setImageResource(promotion.getImageRes());
        holder.textBadge.setText(holder.itemView.getContext().getString(
                R.string.promotions_discount_badge, promotion.getDiscountPercent()));
        holder.textName.setText(promotion.getName());
        holder.textRating.setText(String.format(Locale.US, "%.1f", promotion.getRating()));
        holder.textRestaurant.setText(promotion.getRestaurant());
        holder.textDeliveryTime.setText(promotion.getDeliveryTime());

        holder.textOldPrice.setText(holder.itemView.getContext().getString(
                R.string.promotions_old_price, formatCents(promotion.getOldPriceCents())));
        holder.textNewPrice.setText(holder.itemView.getContext().getString(
                R.string.promotions_new_price, formatCents(promotion.getNewPriceCents())));

        // Risca o preço antigo: liga a "flag" de tachado sem apagar as outras flags do Paint
        holder.textOldPrice.setPaintFlags(
                holder.textOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        // O cartão inteiro é clicável
        holder.itemView.setOnClickListener(v -> listener.onPromotionClick(promotion));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** Converte centavos em texto de moeda. Ex.: 8900 -> "R$ 89,00". */
    private String formatCents(int cents) {
        // O formatador usa um espaço "inquebrável" ( ) depois do R$; trocamos por um espaço normal
        return currencyFormat.format(cents / 100.0).replace(' ', ' ');
    }

    /** Guarda as views de um cartão para não chamar findViewById a cada rolagem. */
    static class PromotionViewHolder extends RecyclerView.ViewHolder {
        final ImageView imagePhoto;
        final TextView textBadge;
        final TextView textName;
        final TextView textRating;
        final TextView textRestaurant;
        final TextView textOldPrice;
        final TextView textNewPrice;
        final TextView textDeliveryTime;

        PromotionViewHolder(@NonNull View itemView) {
            super(itemView);
            imagePhoto = itemView.findViewById(R.id.imagePhoto);
            textBadge = itemView.findViewById(R.id.textBadge);
            textName = itemView.findViewById(R.id.textName);
            textRating = itemView.findViewById(R.id.textRating);
            textRestaurant = itemView.findViewById(R.id.textRestaurant);
            textOldPrice = itemView.findViewById(R.id.textOldPrice);
            textNewPrice = itemView.findViewById(R.id.textNewPrice);
            textDeliveryTime = itemView.findViewById(R.id.textDeliveryTime);
        }
    }
}
