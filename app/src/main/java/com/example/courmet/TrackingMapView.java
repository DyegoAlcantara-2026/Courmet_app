package com.example.courmet;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

/**
 * "Mapa" simplificado desenhado com Canvas: duas ruas bege, a rota dourada já percorrida,
 * o pino do restaurante e o veículo de entrega.
 *
 * As posições são frações da largura/altura da View (0 a 1), assim o desenho se adapta
 * a qualquer tamanho de tela.
 */
public class TrackingMapView extends View {

    // Ruas: {x inicial, y inicial, x final, y final}
    private static final float[] ROAD_MAIN = {0.12f, 0.28f, 0.78f, 0.84f};
    private static final float[] ROAD_CROSS = {0.38f, 0.09f, 0.20f, 1.05f};

    // A rota vai do restaurante até o cliente, em cima da rua principal.
    private static final float START_X = 0.23f, START_Y = 0.373f;
    private static final float END_X = 0.72f, END_Y = 0.79f;

    private final Paint roadPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint routePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Drawable pinIcon;
    private final Drawable vehicleIcon;
    private final float dp;

    // Quanto da rota o entregador já percorreu (0 = no restaurante, 1 = chegou).
    private float progress = 0.6f;
    private ValueAnimator animator;

    public TrackingMapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        dp = getResources().getDisplayMetrics().density;

        roadPaint.setColor(0xFFE3DBD0); // bege das ruas
        roadPaint.setStrokeWidth(4 * dp);
        roadPaint.setStrokeCap(Paint.Cap.ROUND);

        routePaint.setColor(ContextCompat.getColor(context, R.color.gold));
        routePaint.setStrokeWidth(6 * dp);
        routePaint.setStrokeCap(Paint.Cap.ROUND);

        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(2 * dp);

        pinIcon = ContextCompat.getDrawable(context, R.drawable.ic_tracking_pin);
        vehicleIcon = ContextCompat.getDrawable(context, R.drawable.ic_tracking_vehicle);

        setBackgroundColor(ContextCompat.getColor(context, R.color.surface));
    }

    /** Move o veículo até a nova posição com uma animação suave. */
    public void animateProgressTo(float target) {
        if (animator != null) {
            animator.cancel();
        }
        animator = ValueAnimator.ofFloat(progress, target);
        animator.setDuration(1200);
        animator.addUpdateListener(a -> {
            progress = (float) a.getAnimatedValue();
            invalidate(); // pede para o Android chamar onDraw() de novo
        });
        animator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        // A View saiu da tela (ex.: Activity destruída): paramos a animação.
        if (animator != null) {
            animator.cancel();
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();

        // 1) Ruas
        drawLine(canvas, ROAD_MAIN, w, h, roadPaint);
        drawLine(canvas, ROAD_CROSS, w, h, roadPaint);

        // 2) Rota percorrida: do restaurante até a posição atual do veículo
        float startX = START_X * w, startY = START_Y * h;
        float vehicleX = (START_X + (END_X - START_X) * progress) * w;
        float vehicleY = (START_Y + (END_Y - START_Y) * progress) * h;
        canvas.drawLine(startX, startY, vehicleX, vehicleY, routePaint);

        // 3) Pino do restaurante: círculo branco com borda preta e ícone
        float pinRadius = 11 * dp;
        fillPaint.setColor(ContextCompat.getColor(getContext(), R.color.surface));
        canvas.drawCircle(startX, startY, pinRadius, fillPaint);
        strokePaint.setColor(ContextCompat.getColor(getContext(), R.color.ink));
        canvas.drawCircle(startX, startY, pinRadius, strokePaint);
        drawIcon(canvas, pinIcon, startX, startY, 13 * dp);

        // 4) Veículo: círculo dourado com o ícone branco
        fillPaint.setColor(ContextCompat.getColor(getContext(), R.color.gold));
        canvas.drawCircle(vehicleX, vehicleY, 14 * dp, fillPaint);
        drawIcon(canvas, vehicleIcon, vehicleX, vehicleY, 15 * dp);
    }

    private void drawLine(Canvas canvas, float[] line, float w, float h, Paint paint) {
        canvas.drawLine(line[0] * w, line[1] * h, line[2] * w, line[3] * h, paint);
    }

    /** Desenha um ícone (vetor) centralizado em (cx, cy). */
    private void drawIcon(Canvas canvas, @Nullable Drawable icon, float cx, float cy, float size) {
        if (icon == null) return;
        int half = Math.round(size / 2);
        icon.setBounds(Math.round(cx) - half, Math.round(cy) - half,
                Math.round(cx) + half, Math.round(cy) + half);
        icon.draw(canvas);
    }
}
