package com.example.courmet;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.ColorRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Tela de acompanhamento do pedido.
 *
 * Não existe servidor ainda, então simulamos a entrega: a cada 5 segundos o veículo
 * anda no mapa e, no fim, o pedido passa para "Entregue".
 */
public class TrackingActivity extends AppCompatActivity {

    private static final long TICK_MS = 5_000;          // intervalo entre atualizações
    private static final float[] ROUTE_PROGRESS = {0.8f, 1f}; // posições do veículo a cada "tick"

    // Índices das etapas na lista
    private static final int STEP_OUT_FOR_DELIVERY = 2;
    private static final int STEP_DELIVERED = 3;

    private final List<TrackingStep> steps = new ArrayList<>();

    // Handler ligado à thread principal (UI): permite agendar código para "daqui a X ms"
    // com postDelayed(), sem travar a tela e sem criar threads.
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int tick = 0;

    private LinearLayout stepsContainer;
    private TrackingMapView mapView;

    /** Executado a cada TICK_MS: avança a simulação e, se não acabou, se reagenda. */
    private final Runnable simulationTick = new Runnable() {
        @Override
        public void run() {
            mapView.animateProgressTo(ROUTE_PROGRESS[tick]);
            tick++;
            if (tick < ROUTE_PROGRESS.length) {
                handler.postDelayed(this, TICK_MS);
            } else {
                markDelivered();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tracking);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        stepsContainer = findViewById(R.id.stepsContainer);
        mapView = findViewById(R.id.mapView);

        findViewById(R.id.buttonBack).setOnClickListener(v -> finish());
        findViewById(R.id.buttonChat).setOnClickListener(v ->
                Toast.makeText(this, R.string.tracking_coming_soon, Toast.LENGTH_SHORT).show());

        // TODO: receber o status real do pedido em vez de dados fixos
        steps.add(new TrackingStep(R.string.tracking_step_confirmed,
                getString(R.string.tracking_time_confirmed), TrackingStep.State.DONE));
        steps.add(new TrackingStep(R.string.tracking_step_preparing,
                getString(R.string.tracking_time_preparing), TrackingStep.State.DONE));
        steps.add(new TrackingStep(R.string.tracking_step_out_for_delivery,
                getString(R.string.tracking_in_transit), TrackingStep.State.CURRENT));
        steps.add(new TrackingStep(R.string.tracking_step_delivered,
                getString(R.string.tracking_expected), TrackingStep.State.PENDING));
        bindSteps();

        // Primeira atualização daqui a 5 segundos.
        handler.postDelayed(simulationTick, TICK_MS);
    }

    @Override
    protected void onDestroy() {
        // Muito importante: se o usuário sair da tela antes do fim, removemos os Runnables
        // pendentes. Senão eles rodariam depois, mexendo em Views de uma Activity morta
        // (e mantendo ela presa na memória).
        handler.removeCallbacks(simulationTick);
        super.onDestroy();
    }

    /** Fim da simulação: "Saiu para entrega" e "Entregue" ficam concluídas. */
    private void markDelivered() {
        TrackingStep outForDelivery = steps.get(STEP_OUT_FOR_DELIVERY);
        outForDelivery.state = TrackingStep.State.DONE;
        outForDelivery.time = getString(R.string.tracking_time_out_for_delivery);

        TrackingStep delivered = steps.get(STEP_DELIVERED);
        delivered.state = TrackingStep.State.DONE;
        delivered.time = getString(R.string.tracking_time_delivered);
        bindSteps();

        ((TextView) findViewById(R.id.textEtaLabel)).setText(R.string.tracking_delivered_label);
        ((TextView) findViewById(R.id.textEtaValue)).setText(R.string.tracking_delivered_value);
        Toast.makeText(this, R.string.tracking_delivered_toast, Toast.LENGTH_SHORT).show();
    }

    /** (Re)cria as linhas da linha do tempo a partir da lista de etapas. */
    private void bindSteps() {
        LayoutInflater inflater = LayoutInflater.from(this);
        stepsContainer.removeAllViews();
        for (int i = 0; i < steps.size(); i++) {
            TrackingStep step = steps.get(i);
            View row = inflater.inflate(R.layout.item_tracking_step, stepsContainer, false);

            TextView title = row.findViewById(R.id.textTitle);
            TextView time = row.findViewById(R.id.textTime);
            title.setText(step.titleRes);
            time.setText(step.time);

            // Bolinha
            View dot = row.findViewById(R.id.dot);
            ViewCompat.setBackgroundTintList(dot, ColorStateList.valueOf(color(dotColor(step.state))));

            // Linha de cima: escura se esta etapa já começou. A primeira etapa não tem.
            View lineTop = row.findViewById(R.id.lineTop);
            if (i == 0) {
                lineTop.setVisibility(View.INVISIBLE);
            } else {
                lineTop.setBackgroundColor(color(lineColor(step.state)));
            }

            // Linha de baixo: escura se a PRÓXIMA etapa já começou. A última não tem.
            View lineBottom = row.findViewById(R.id.lineBottom);
            if (i == steps.size() - 1) {
                lineBottom.setVisibility(View.INVISIBLE);
            } else {
                lineBottom.setBackgroundColor(color(lineColor(steps.get(i + 1).state)));
            }

            // Textos
            switch (step.state) {
                case DONE:
                    title.setTextColor(color(R.color.ink));
                    time.setTextColor(color(R.color.text_muted));
                    break;
                case CURRENT:
                    title.setTextColor(color(R.color.gold));
                    time.setTextColor(color(R.color.gold));
                    break;
                case PENDING:
                    title.setTextColor(color(R.color.text_hint));
                    time.setTextColor(color(R.color.text_muted));
                    break;
            }

            stepsContainer.addView(row);
        }
    }

    @ColorRes
    private static int dotColor(TrackingStep.State state) {
        switch (state) {
            case DONE:
                return R.color.ink;
            case CURRENT:
                return R.color.gold;
            default:
                return R.color.border;
        }
    }

    @ColorRes
    private static int lineColor(TrackingStep.State state) {
        return state == TrackingStep.State.PENDING ? R.color.border : R.color.ink;
    }

    private int color(@ColorRes int res) {
        return ContextCompat.getColor(this, res);
    }
}
