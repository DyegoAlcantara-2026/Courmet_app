package com.example.courmet.produtos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.courmet.R;
import com.google.android.material.chip.ChipGroup;

import java.util.Objects;

/**
 * Tela "Cardápio": lista as comidas e bebidas cadastradas (o "R" do CRUD).
 * Daqui o usuário abre o formulário para criar um item novo ou editar um existente.
 */
public class ProdutoListActivity extends AppCompatActivity {

    private ProdutoRepository repository;
    private ProdutoAdapter adapter;
    private RecyclerView recyclerProdutos;
    private View layoutVazio;

    // Filtro atual: null = todos; senão Produto.CATEGORIA_COMIDA ou CATEGORIA_BEBIDA
    private String categoriaSelecionada = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Desenha o app atrás das barras do sistema; o padding abaixo evita que o conteúdo fique por baixo delas
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_produto_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        repository = new ProdutoRepository(this);

        findViewById(R.id.buttonBack).setOnClickListener(v -> finish());

        configurarLista();
        configurarFiltros();

        // Botão "Novo item": abre o formulário SEM extra -> modo cadastro
        findViewById(R.id.fabNovo).setOnClickListener(v ->
                startActivity(new Intent(this, ProdutoFormActivity.class)));
    }

    /**
     * onResume() roda sempre que a tela volta a ficar visível — inclusive depois que o
     * usuário salva ou exclui algo no formulário e aperta voltar. Por isso recarregamos aqui
     * (e não só no onCreate): assim a lista sempre mostra os dados mais recentes do banco.
     */
    @Override
    protected void onResume() {
        super.onResume();
        carregarProdutos();
    }

    private void configurarLista() {
        recyclerProdutos = findViewById(R.id.recyclerProdutos);
        layoutVazio = findViewById(R.id.layoutVazio);

        // O LayoutManager define o "formato" da lista: aqui, um item embaixo do outro
        recyclerProdutos.setLayoutManager(new LinearLayoutManager(this));

        // Ao tocar num cartão: abre o formulário COM o id -> modo edição
        adapter = new ProdutoAdapter(produto -> {
            Intent intent = new Intent(this, ProdutoFormActivity.class);
            intent.putExtra(ProdutoFormActivity.EXTRA_PRODUTO_ID, produto.getId());
            startActivity(intent);
        });
        recyclerProdutos.setAdapter(adapter);
    }

    private void configurarFiltros() {
        ChipGroup chipGroup = findViewById(R.id.chipGroupFiltro);
        // Chamado quando o usuário troca o chip marcado (sempre há exatamente um)
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chipComidas) {
                categoriaSelecionada = Produto.CATEGORIA_COMIDA;
            } else if (id == R.id.chipBebidas) {
                categoriaSelecionada = Produto.CATEGORIA_BEBIDA;
            } else {
                categoriaSelecionada = null; // "Todos"
            }
            carregarProdutos();
        });
    }

    /**
     * Pede a lista ao repositório. A consulta ao banco roda em segundo plano e o resultado
     * chega depois, no callback (já na thread principal, então podemos mexer na tela).
     */
    private void carregarProdutos() {
        // Guarda o filtro usado neste pedido para ignorar respostas "atrasadas"
        // caso o usuário troque de chip antes do banco responder.
        final String categoriaPedida = categoriaSelecionada;
        repository.listar(categoriaPedida, produtos -> {
            if (isFinishing() || isDestroyed()) return; // a tela já foi fechada
            if (!Objects.equals(categoriaPedida, categoriaSelecionada)) return;

            adapter.setItens(produtos);
            boolean vazia = produtos.isEmpty();
            layoutVazio.setVisibility(vazia ? View.VISIBLE : View.GONE);
            recyclerProdutos.setVisibility(vazia ? View.GONE : View.VISIBLE);
        });
    }
}
