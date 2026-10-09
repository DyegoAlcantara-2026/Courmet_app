package com.example.courmet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Tela de Promoções: banner, chips de categoria e uma lista (RecyclerView) de pratos com desconto.
 * Os dados são fixos (estáticos) só para demonstração.
 */
public class PromotionsActivity extends AppCompatActivity {

    // Mesma chave que a RestaurantActivity lê (RestaurantActivity.EXTRA_RESTAURANT_NAME).
    // Usamos uma cópia local para esta tela não depender diretamente daquela classe.
    private static final String EXTRA_RESTAURANT_NAME = RestaurantActivity.EXTRA_RESTAURANT_NAME;

    // Todas as promoções; a lista exibida é um filtro desta
    private final List<Promotion> allPromotions = new ArrayList<>();

    private PromotionAdapter adapter;
    private TextView textEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_promotions);

        // Edge-to-edge: afasta o conteúdo da barra de status e da barra de navegação
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        // Botão de filtro ainda não tem função
        findViewById(R.id.buttonFilter).setOnClickListener(v ->
                Toast.makeText(this, R.string.promotions_coming_soon, Toast.LENGTH_SHORT).show());

        loadPromotions();

        // RecyclerView + Adapter: ao tocar num cartão, abre o restaurante daquele prato
        RecyclerView recycler = findViewById(R.id.recyclerPromotions);
        adapter = new PromotionAdapter(this::openRestaurant);
        recycler.setAdapter(adapter);
        textEmpty = findViewById(R.id.textEmpty);

        // Sempre que outro chip é marcado, refazemos o filtro
        ChipGroup chipGroup = findViewById(R.id.chipGroupCategories);
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                applyFilter(checkedIds.get(0));
            }
        });
        applyFilter(chipGroup.getCheckedChipId());
    }

    /** Cria a lista fixa de promoções. A categoria usa o mesmo texto dos chips. */
    private void loadPromotions() {
        String pasta = getString(R.string.promotions_category_pasta);
        String meat = getString(R.string.promotions_category_meat);
        String japanese = getString(R.string.promotions_category_japanese);
        String seafood = getString(R.string.promotions_category_seafood);

        allPromotions.add(new Promotion("Risoto de Funghi Porcini", "Le Petit Paris", pasta,
                4.9, 8900, 40, "30-40 min", R.drawable.img_promo_risoto));
        allPromotions.add(new Promotion("Salmão Grelhado ao Molho de Maracujá", "Omakase Sushi Bar", japanese,
                5.0, 7200, 30, "25-35 min", R.drawable.img_promo_salmao));
        allPromotions.add(new Promotion("Kobe Luxury Beef", "Brasa Nobre", meat,
                5.0, 18900, 25, "30-45 min", R.drawable.img_promo_kobe));
        allPromotions.add(new Promotion("Combinado Omakase (12 pçs)", "Omakase Sushi Bar", japanese,
                5.0, 18000, 20, "30-40 min", R.drawable.img_promo_sushi));
        allPromotions.add(new Promotion("Vieiras Seladas na Manteiga", "Le Petit Paris", seafood,
                4.8, 11800, 35, "25-35 min", R.drawable.img_promo_vieiras));
        allPromotions.add(new Promotion("Guioza de Wagyu", "Omakase Sushi Bar", japanese,
                4.9, 4800, 15, "20-30 min", R.drawable.img_promo_guioza));
    }

    /** Mostra só as promoções da categoria do chip marcado ("Todos" mostra tudo). */
    private void applyFilter(int checkedChipId) {
        List<Promotion> filtered = new ArrayList<>();

        if (checkedChipId == R.id.chipAll) {
            filtered.addAll(allPromotions);
        } else {
            // O texto do chip ("Massas", "Carnes"...) é a categoria procurada
            TextView chip = findViewById(checkedChipId);
            String category = chip.getText().toString();
            for (Promotion promotion : allPromotions) {
                if (promotion.getCategory().equals(category)) {
                    filtered.add(promotion);
                }
            }
        }

        adapter.setItems(filtered);
        textEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    /** Abre a tela do restaurante, enviando o nome dele como "extra" da Intent. */
    private void openRestaurant(Promotion promotion) {
        Intent intent = new Intent(this, RestaurantActivity.class);
        intent.putExtra(EXTRA_RESTAURANT_NAME, promotion.getRestaurant());
        startActivity(intent);
    }
}
