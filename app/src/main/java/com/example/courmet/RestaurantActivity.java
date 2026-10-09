package com.example.courmet;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Tela de detalhes de um restaurante: capa, informações e cardápio. */
public class RestaurantActivity extends AppCompatActivity {

    /** Extra opcional (String) para trocar o nome exibido no título. */
    public static final String EXTRA_RESTAURANT_NAME = "restaurant_name";

    // "Carrinho" em memória: some quando a tela é fechada (não há banco de dados)
    private int cartCount = 0;
    private int cartTotalCents = 0;
    private boolean favorite = false;

    private String restaurantName;
    private MaterialButton buttonViewOrder;
    private ImageButton buttonFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_restaurant);

        // A foto é escura: ícones da status bar em branco (só nesta tela)
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightStatusBars(false);

        applyInsets();

        // Nome vindo de outra tela (ex.: Promoções) ou o padrão
        restaurantName = getIntent().getStringExtra(EXTRA_RESTAURANT_NAME);
        if (restaurantName == null || restaurantName.isEmpty()) {
            restaurantName = getString(R.string.restaurant_default_name);
        }
        ((TextView) findViewById(R.id.textRestaurantName)).setText(restaurantName);

        buttonViewOrder = findViewById(R.id.buttonViewOrder);
        buttonFavorite = findViewById(R.id.buttonFavorite);

        findViewById(R.id.buttonBack).setOnClickListener(v -> finish());
        buttonFavorite.setOnClickListener(v -> toggleFavorite());
        findViewById(R.id.buttonShare).setOnClickListener(v -> share());
        buttonViewOrder.setOnClickListener(v ->
                startActivity(new Intent(this, OrderActivity.class)));

        setupMenu(createDishes());
    }

    /**
     * Edge-to-edge: a foto desenha atrás da status bar, então só os botões
     * descem (margem do topo) e a raiz ganha padding embaixo/nas laterais.
     */
    private void applyInsets() {
        View buttons = findViewById(R.id.layoutHeroButtons);
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) buttons.getLayoutParams();
        int baseTopMargin = lp.topMargin;

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, 0, bars.right, bars.bottom);
            lp.topMargin = baseTopMargin + bars.top;
            buttons.setLayoutParams(lp);
            return insets;
        });
    }

    /** Dados fixos do cardápio (em um app real viriam de uma API ou banco). */
    private List<RestaurantDish> createDishes() {
        String starters = getString(R.string.restaurant_section_starters);
        String mains = getString(R.string.restaurant_section_mains);

        List<RestaurantDish> dishes = new ArrayList<>();
        dishes.add(new RestaurantDish("Guioza de Wagyu",
                "Massa fina artesanal recheada com Wagyu selado e especiarias asiáticas (4 unidades).",
                4800, R.drawable.img_restaurant_gyoza, starters));
        dishes.add(new RestaurantDish("Tartare de Salmão & Trufas",
                "Salmão fresco finamente cortado com raspas de trufas negras e azeite artesanal.",
                6200, R.drawable.img_restaurant_tartare, starters));
        dishes.add(new RestaurantDish("Omakase Premium (12 pçs)",
                "Seleção do Chef de sushis exóticos com peixes da estação e ovas de ostra.",
                18000, R.drawable.img_restaurant_omakase, mains));
        dishes.add(new RestaurantDish("Nigiri Selection (8 pçs)",
                "Nigiris de atum, salmão e peixe branco maçaricado com toque de yuzu.",
                9600, R.drawable.img_restaurant_omakase, mains));
        dishes.add(new RestaurantDish("Wagyu Tataki",
                "Fatias de Wagyu levemente seladas com ponzu cítrico e cebolinha.",
                12800, R.drawable.img_restaurant_tartare, mains));
        return dishes;
    }

    /** Infla um título sempre que a seção muda e um cartão para cada prato. */
    private void setupMenu(List<RestaurantDish> dishes) {
        ViewGroup container = findViewById(R.id.containerMenu);
        LayoutInflater inflater = LayoutInflater.from(this);
        String currentSection = null;

        for (RestaurantDish dish : dishes) {
            if (!dish.section.equals(currentSection)) {
                currentSection = dish.section;
                TextView title = (TextView) inflater.inflate(
                        R.layout.item_restaurant_section, container, false);
                title.setText(currentSection);
                container.addView(title);
            }

            View card = inflater.inflate(R.layout.item_restaurant_dish, container, false);
            ((TextView) card.findViewById(R.id.textDishName)).setText(dish.name);
            ((TextView) card.findViewById(R.id.textDishDescription)).setText(dish.description);
            ((TextView) card.findViewById(R.id.textDishPrice)).setText(formatPrice(dish.priceCents));
            ((ImageView) card.findViewById(R.id.imageDish)).setImageResource(dish.imageRes);
            card.setOnClickListener(v -> addToCart(dish));
            container.addView(card);
        }
    }

    private void addToCart(RestaurantDish dish) {
        cartCount++;
        cartTotalCents += dish.priceCents;
        updateCartBar();
    }

    /** Mostra a barra "Ver pedido" só quando há pelo menos 1 item. */
    private void updateCartBar() {
        if (cartCount == 0) {
            buttonViewOrder.setVisibility(View.GONE);
            return;
        }
        // getQuantityString escolhe "1 item" ou "N itens" (plurals)
        String items = getResources().getQuantityString(
                R.plurals.restaurant_cart_items, cartCount, cartCount);
        buttonViewOrder.setText(getString(R.string.restaurant_cart_button,
                items, formatPrice(cartTotalCents)));
        buttonViewOrder.setVisibility(View.VISIBLE);
    }

    private void toggleFavorite() {
        favorite = !favorite;
        buttonFavorite.setImageResource(favorite
                ? R.drawable.ic_restaurant_heart_filled
                : R.drawable.ic_restaurant_heart);
        buttonFavorite.setContentDescription(getString(favorite
                ? R.string.restaurant_favorite_remove
                : R.string.restaurant_favorite_add));
        Toast.makeText(this, favorite
                ? R.string.restaurant_favorite_added
                : R.string.restaurant_favorite_removed, Toast.LENGTH_SHORT).show();
    }

    /** Abre o seletor do Android com os apps que sabem compartilhar texto. */
    private void share() {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_TEXT, getString(R.string.restaurant_share_text, restaurantName));
        startActivity(Intent.createChooser(send, getString(R.string.restaurant_share)));
    }

    /** 4800 -> "R$ 48,00". O formatador usa espaço não separável; trocamos por espaço normal. */
    private static String formatPrice(int cents) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        return format.format(cents / 100.0)
                .replace(' ', ' ')
                .replace(' ', ' ');
    }
}
