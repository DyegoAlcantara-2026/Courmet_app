package com.example.courmet;

import androidx.annotation.StringRes;

/** Uma linha do pedido. Valores em centavos para evitar erros de arredondamento. */
public final class OrderItem {

    @StringRes
    public final int nameRes;
    public final int quantity;
    public final long unitPriceCents;

    public OrderItem(@StringRes int nameRes, int quantity, long unitPriceCents) {
        this.nameRes = nameRes;
        this.quantity = quantity;
        this.unitPriceCents = unitPriceCents;
    }

    public long totalCents() {
        return unitPriceCents * quantity;
    }
}
