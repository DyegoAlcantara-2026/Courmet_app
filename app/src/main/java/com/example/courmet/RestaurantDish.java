package com.example.courmet;

import androidx.annotation.DrawableRes;

/**
 * Um prato do cardápio do restaurante.
 * Classe "modelo" simples: só guarda dados, sem lógica.
 */
public class RestaurantDish {

    public final String name;
    public final String description;
    // Preço em centavos (int) evita erros de arredondamento com double: R$ 48,00 = 4800
    public final int priceCents;
    @DrawableRes
    public final int imageRes;
    // Nome da seção do cardápio em que o prato aparece ("Entradas", "Pratos Principais"...)
    public final String section;

    public RestaurantDish(String name, String description, int priceCents,
                          @DrawableRes int imageRes, String section) {
        this.name = name;
        this.description = description;
        this.priceCents = priceCents;
        this.imageRes = imageRes;
        this.section = section;
    }
}
