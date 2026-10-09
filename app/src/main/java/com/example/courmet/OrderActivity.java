package com.example.courmet;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class OrderActivity extends AppCompatActivity {

    // TODO: receber o pedido real (carrinho) em vez de dados fixos
    private static final List<OrderItem> ITEMS = Arrays.asList(
            new OrderItem(R.string.order_item_omakase, 1, 180_00),
            new OrderItem(R.string.order_item_gyoza, 1, 48_00)
    );
    private static final long DELIVERY_FEE_CENTS = 0;
    private static final String CARD_LAST_DIGITS = "4829";

    private final NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        findViewById(R.id.buttonBack).setOnClickListener(v -> finish());
        findViewById(R.id.buttonChangePayment).setOnClickListener(v ->
                Toast.makeText(this, R.string.order_coming_soon, Toast.LENGTH_SHORT).show());

        TextView paymentCard = findViewById(R.id.textPaymentCard);
        paymentCard.setText(getString(R.string.order_payment_card, CARD_LAST_DIGITS));

        bindItems();
        bindTotals();
    }

    private void bindItems() {
        LinearLayout container = findViewById(R.id.itemsContainer);
        LayoutInflater inflater = LayoutInflater.from(this);
        container.removeAllViews();
        for (OrderItem item : ITEMS) {
            View row = inflater.inflate(R.layout.item_order_item, container, false);
            ((TextView) row.findViewById(R.id.textQuantity))
                    .setText(getString(R.string.order_item_quantity, item.quantity));
            ((TextView) row.findViewById(R.id.textName)).setText(item.nameRes);
            ((TextView) row.findViewById(R.id.textPrice)).setText(format(item.totalCents()));
            container.addView(row);
        }
    }

    private void bindTotals() {
        long subtotal = 0;
        for (OrderItem item : ITEMS) {
            subtotal += item.totalCents();
        }
        long total = subtotal + DELIVERY_FEE_CENTS;

        ((TextView) findViewById(R.id.textSubtotal)).setText(format(subtotal));

        TextView deliveryFee = findViewById(R.id.textDeliveryFee);
        if (DELIVERY_FEE_CENTS == 0) {
            deliveryFee.setText(R.string.order_delivery_free);
            deliveryFee.setTextColor(ContextCompat.getColor(this, R.color.gold));
        } else {
            deliveryFee.setText(format(DELIVERY_FEE_CENTS));
        }

        ((TextView) findViewById(R.id.textTotal)).setText(format(total));

        MaterialButton checkout = findViewById(R.id.buttonCheckout);
        checkout.setText(getString(R.string.order_checkout, format(total)));
        checkout.setOnClickListener(v -> {
            Toast.makeText(this, R.string.order_sent, Toast.LENGTH_SHORT).show();
            // Pedido enviado: abre o acompanhamento e tira o carrinho da pilha,
            // para o "Voltar" do acompanhamento não reabrir um pedido já finalizado.
            startActivity(new Intent(this, TrackingActivity.class));
            finish();
        });
    }

    /** Formata centavos em reais, trocando o espaço não separável por um espaço comum. */
    private String format(long cents) {
        return currency.format(cents / 100.0).replace(' ', ' ');
    }
}
