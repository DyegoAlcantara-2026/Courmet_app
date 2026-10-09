package com.example.courmet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.courmet.produtos.ProdutoListActivity;
import com.example.courmet.usuarios.UsuarioListActivity;

/**
 * Home do app: aberta logo depois do login.
 * Funciona como um "hub" de navegação: cada cartão abre outra tela.
 */
public class MainActivity extends AppCompatActivity {

    /** Extra (String) com o nome do usuário logado, para a saudação. */
    public static final String EXTRA_NOME_USUARIO = "nome_usuario";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Desenha o app "de ponta a ponta" (atrás das barras do sistema)...
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        // ...e aplica um padding do tamanho das barras para nada ficar escondido.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        // Cada cartão abre uma Activity com uma Intent explícita
        // (new Intent(origem, Destino.class)). startActivity() coloca a nova tela
        // no topo da pilha (back stack); o "Voltar" dela (finish()) volta para cá.
        // Saudação com o nome de quem fez login (enviado pelo LoginActivity como "extra")
        String nome = getIntent().getStringExtra(EXTRA_NOME_USUARIO);
        if (nome != null && !nome.trim().isEmpty()) {
            String primeiroNome = nome.trim().split("\\s+")[0];
            ((TextView) findViewById(R.id.textGreeting)).setText(getString(R.string.home_greeting_name, primeiroNome));
        }

        bindCard(R.id.cardPromotions, R.drawable.ic_home_tag,
                R.string.home_promotions_title, R.string.home_promotions_subtitle,
                v -> startActivity(new Intent(this, PromotionsActivity.class)));

        bindCard(R.id.cardRestaurant, R.drawable.ic_home_store,
                R.string.home_restaurant_title, R.string.home_restaurant_subtitle,
                v -> startActivity(new Intent(this, RestaurantActivity.class)));

        bindCard(R.id.cardUsers, R.drawable.ic_home_users,
                R.string.home_users_title, R.string.home_users_subtitle,
                v -> startActivity(new Intent(this, UsuarioListActivity.class)));

        bindCard(R.id.cardMenu, R.drawable.ic_home_menu,
                R.string.home_menu_title, R.string.home_menu_subtitle,
                v -> startActivity(new Intent(this, ProdutoListActivity.class)));

        bindCard(R.id.cardOrder, R.drawable.ic_home_bag,
                R.string.home_order_title, R.string.home_order_subtitle,
                v -> startActivity(new Intent(this, OrderActivity.class)));

        bindCard(R.id.cardSupport, R.drawable.ic_home_help,
                R.string.home_support_title, R.string.home_support_subtitle,
                v -> startActivity(new Intent(this, SupportActivity.class)));

        findViewById(R.id.buttonLogout).setOnClickListener(v -> logout());
    }

    /** Preenche um cartão (item_home_card) com ícone, textos e ação de clique. */
    private void bindCard(@IdRes int cardId, @DrawableRes int icon, @StringRes int title,
                          @StringRes int subtitle, View.OnClickListener onClick) {
        View card = findViewById(cardId);
        ((ImageView) card.findViewById(R.id.imageIcon)).setImageResource(icon);
        ((TextView) card.findViewById(R.id.textTitle)).setText(title);
        ((TextView) card.findViewById(R.id.textSubtitle)).setText(subtitle);
        card.setOnClickListener(onClick);
    }

    /** Volta para o Login, apagando todo o histórico de telas. */
    private void logout() {
        Intent intent = new Intent(this, LoginActivity.class);
        // FLAG_ACTIVITY_NEW_TASK + FLAG_ACTIVITY_CLEAR_TASK: limpa a pilha inteira
        // e deixa o Login como única tela. Assim, apertar "Voltar" no Login
        // fecha o app em vez de voltar para a Home de um usuário que já saiu.
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
