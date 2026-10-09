package com.example.courmet.usuarios;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.courmet.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Tela "Usuários": lista as contas cadastradas (o "R" do CRUD).
 * Daqui abrimos o formulário para criar um usuário novo ou editar um existente.
 */
public class UsuarioListActivity extends AppCompatActivity {

    private UsuarioRepository repository;
    private UsuarioAdapter adapter;
    private RecyclerView recyclerUsuarios;
    private View layoutVazio;
    private TextView textVazioTitulo;
    private TextView textVazioDica;
    private TextView textContagem;
    private EditText inputBusca;

    // Todos os usuários vindos do banco; a busca filtra esta lista em memória
    private final List<Usuario> todosUsuarios = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Desenha o app atrás das barras do sistema; o padding abaixo evita que o conteúdo fique por baixo delas
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_usuario_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        repository = new UsuarioRepository(this);

        textContagem = findViewById(R.id.textContagem);
        findViewById(R.id.buttonBack).setOnClickListener(v -> finish());

        configurarLista();
        configurarBusca();

        // Botão "Novo usuário": abre o formulário SEM extra -> modo cadastro
        findViewById(R.id.fabNovo).setOnClickListener(v ->
                startActivity(new Intent(this, UsuarioFormActivity.class)));
    }

    /**
     * onResume() roda sempre que a tela volta a ficar visível — inclusive ao voltar do
     * formulário depois de salvar/excluir. Recarregando aqui, a lista fica sempre atualizada.
     */
    @Override
    protected void onResume() {
        super.onResume();
        carregarUsuarios();
    }

    private void configurarLista() {
        recyclerUsuarios = findViewById(R.id.recyclerUsuarios);
        layoutVazio = findViewById(R.id.layoutVazio);
        textVazioTitulo = findViewById(R.id.textVazioTitulo);
        textVazioDica = findViewById(R.id.textVazioDica);

        // O LayoutManager define o "formato" da lista: aqui, um item embaixo do outro
        recyclerUsuarios.setLayoutManager(new LinearLayoutManager(this));

        // Ao tocar num cartão: abre o formulário COM o id -> modo edição
        adapter = new UsuarioAdapter(usuario -> {
            Intent intent = new Intent(this, UsuarioFormActivity.class);
            intent.putExtra(UsuarioFormActivity.EXTRA_USUARIO_ID, usuario.getId());
            startActivity(intent);
        });
        recyclerUsuarios.setAdapter(adapter);
    }

    private void configurarBusca() {
        inputBusca = findViewById(R.id.inputBusca);
        // O TextWatcher avisa a cada letra digitada; só usamos o "depois da mudança"
        inputBusca.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                aplicarFiltro();
            }
        });
    }

    /**
     * Pede a lista ao repositório. A consulta roda em segundo plano e o resultado chega
     * no callback, já na thread principal (então podemos mexer na tela).
     */
    private void carregarUsuarios() {
        repository.listar(usuarios -> {
            if (isFinishing() || isDestroyed()) return; // a tela já foi fechada

            todosUsuarios.clear();
            todosUsuarios.addAll(usuarios);

            // <plurals>: o Android escolhe "1 usuário" ou "N usuários" conforme o número.
            // O primeiro "total" escolhe a forma; o segundo substitui o %d no texto.
            int total = todosUsuarios.size();
            textContagem.setText(getResources().getQuantityString(
                    R.plurals.usuario_list_count, total, total));

            aplicarFiltro();
        });
    }

    /** Mostra só os usuários cujo nome ou e-mail contém o texto digitado na busca. */
    private void aplicarFiltro() {
        String termo = inputBusca.getText() == null ? ""
                : inputBusca.getText().toString().trim().toLowerCase(Locale.ROOT);

        List<Usuario> filtrados = new ArrayList<>();
        for (Usuario u : todosUsuarios) {
            if (termo.isEmpty() || contem(u.getNome(), termo) || contem(u.getEmail(), termo)) {
                filtrados.add(u);
            }
        }
        adapter.setItens(filtrados);

        // Lista vazia: diferenciamos "ninguém cadastrado" de "a busca não achou nada"
        boolean vazia = filtrados.isEmpty();
        if (vazia) {
            boolean semCadastro = todosUsuarios.isEmpty();
            textVazioTitulo.setText(semCadastro
                    ? R.string.usuario_list_empty : R.string.usuario_list_no_results);
            textVazioDica.setText(semCadastro
                    ? R.string.usuario_list_empty_hint : R.string.usuario_list_no_results_hint);
        }
        layoutVazio.setVisibility(vazia ? View.VISIBLE : View.GONE);
        recyclerUsuarios.setVisibility(vazia ? View.GONE : View.VISIBLE);
    }

    /** true se "texto" contém "termo", ignorando maiúsculas/minúsculas. */
    private static boolean contem(String texto, String termo) {
        return texto != null && texto.toLowerCase(Locale.ROOT).contains(termo);
    }
}
