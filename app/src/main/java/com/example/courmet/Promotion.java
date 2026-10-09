package com.example.courmet;

import androidx.annotation.DrawableRes;

/**
 * Modelo de uma promoção: um prato com desconto.
 * Guardamos o preço em centavos (int) para evitar erros de arredondamento do double.
 */
public class Promotion {

    private final String name;
    private final String restaurant;
    private final String category;
    private final double rating;
    private final int oldPriceCents;
    private final int discountPercent;
    private final String deliveryTime;
    @DrawableRes
    private final int imageRes;

    public Promotion(String name, String restaurant, String category, double rating,
                     int oldPriceCents, int discountPercent, String deliveryTime,
                     @DrawableRes int imageRes) {
        this.name = name;
        this.restaurant = restaurant;
        this.category = category;
        this.rating = rating;
        this.oldPriceCents = oldPriceCents;
        this.discountPercent = discountPercent;
        this.deliveryTime = deliveryTime;
        this.imageRes = imageRes;
    }

    /** Preço com desconto, calculado a partir do preço antigo. Ex.: 8900 com 40% -> 5340. */
    public int getNewPriceCents() {
        return Math.round(oldPriceCents * (100 - discountPercent) / 100f);
    }

    public String getName() { return name; }
    public String getRestaurant() { return restaurant; }
    public String getCategory() { return category; }
    public double getRating() { return rating; }
    public int getOldPriceCents() { return oldPriceCents; }
    public int getDiscountPercent() { return discountPercent; }
    public String getDeliveryTime() { return deliveryTime; }
    @DrawableRes
    public int getImageRes() { return imageRes; }
}
