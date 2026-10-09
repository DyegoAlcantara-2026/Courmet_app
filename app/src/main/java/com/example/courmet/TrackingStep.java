package com.example.courmet;

import androidx.annotation.StringRes;

/** Uma etapa da linha do tempo do pedido (ex.: "Preparando", 12:55). */
public class TrackingStep {

    /** Estado da etapa: define as cores da bolinha, da linha e dos textos. */
    public enum State {
        DONE,     // concluída: preta
        CURRENT,  // em andamento: dourada
        PENDING   // ainda não aconteceu: cinza claro
    }

    @StringRes
    public final int titleRes;
    // Horário e estado mudam durante a simulação, por isso não são "final".
    public String time;
    public State state;

    public TrackingStep(@StringRes int titleRes, String time, State state) {
        this.titleRes = titleRes;
        this.time = time;
        this.state = state;
    }
}
