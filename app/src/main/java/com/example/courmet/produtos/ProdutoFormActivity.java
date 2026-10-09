package com.example.courmet.produtos;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.courmet.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;

/**
 * Tela de formulário do CRUD: serve tanto para CRIAR quanto para EDITAR um produto.
 *
 * Como a tela sabe o modo? Pelo Intent que a abriu:
 *  - sem o extra EXTRA_PRODUTO_ID -> cadastro de um item novo;
 *  - com o extra EXTRA_PRODUTO_ID -> edição do item com aquele id.
 *
 * Exemplo de quem abre a tela para editar:
 *   Intent intent = new Intent(this, ProdutoFormActivity.class);
 *   intent.putExtra(ProdutoFormActivity.EXTRA_PRODUTO_ID, produto.getId());
 *   startActivity(intent);
 */
public class ProdutoFormActivity extends AppCompatActivity {

    /** Extra do Intent com o id do produto a editar. Ausente = cadastro de um novo produto. */
    public static final String EXTRA_PRODUTO_ID = "produto_id";

    /** Valor usado quando o extra não existe (o banco nunca gera id 0). */
    private static final long SEM_ID = 0;

    private ProdutoRepository repository;

    /** Id do produto em edição, ou SEM_ID no modo cadastro. */
    private long produtoId = SEM_ID;

    private TextInputLayout layoutNome;
    private TextInputLayout layoutPreco;
    private TextInputEditText inputNome;
    private TextInputEditText inputDescricao;
    private TextInputEditText inputPreco;
    private ChipGroup chipGroupCategoria;
    private MaterialButton buttonSalvar;
    private MaterialButton buttonExcluir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Desenha atrás das barras do sistema; o listener abaixo devolve esse espaço como padding.
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_produto_form);
        // Type.ime() = teclado: quando ele abre, o padding de baixo cresce e o conteúdo
        // pode rolar até o último campo, sem ficar escondido atrás do teclado.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        repository = new ProdutoRepository(this);

        layoutNome = findViewById(R.id.layoutNome);
        layoutPreco = findViewById(R.id.layoutPreco);
        inputNome = findViewById(R.id.inputNome);
        inputDescricao = findViewById(R.id.inputDescricao);
        inputPreco = findViewById(R.id.inputPreco);
        chipGroupCategoria = findViewById(R.id.chipGroupCategoria);
        buttonSalvar = findViewById(R.id.buttonSalvar);
        buttonExcluir = findViewById(R.id.buttonExcluir);
        TextView textTitulo = findViewById(R.id.textTitulo);

        // Lê o extra do Intent. O segundo parâmetro é o valor padrão caso o extra não exista.
        produtoId = getIntent().getLongExtra(EXTRA_PRODUTO_ID, SEM_ID);
        boolean modoEdicao = produtoId != SEM_ID;

        textTitulo.setText(modoEdicao ? R.string.produto_form_titulo_editar : R.string.produto_form_titulo_novo);
        buttonExcluir.setVisibility(modoEdicao ? View.VISIBLE : View.GONE);

        findViewById(R.id.buttonVoltar).setOnClickListener(v -> finish());
        buttonSalvar.setOnClickListener(v -> salvar());
        buttonExcluir.setOnClickListener(v -> confirmarExclusao());

        // Quando o usuário volta a um campo com erro, a mensagem some para ele corrigir.
        inputNome.setOnFocusChangeListener((v, temFoco) -> { if (temFoco) layoutNome.setError(null); });
        inputPreco.setOnFocusChangeListener((v, temFoco) -> { if (temFoco) layoutPreco.setError(null); });

        // savedInstanceState != null quando a tela foi recriada (ex.: girou o celular).
        // Nesse caso o Android já restaura o texto dos campos, então NÃO recarregamos
        // do banco (senão apagaríamos o que o usuário já tinha digitado).
        if (modoEdicao && savedInstanceState == null) {
            carregarProduto();
        }
    }

    /** Modo edição: busca o produto no banco e preenche os campos. */
    private void carregarProduto() {
        setBotoesHabilitados(false);
        // A busca é assíncrona: o código dentro da lambda (callback) só roda
        // quando o resultado chegar, já de volta na thread principal.
        repository.buscarPorId(produtoId, produto -> {
            // Se o usuário saiu da tela enquanto esperava, não mexemos mais nela.
            if (isFinishing() || isDestroyed()) return;

            if (produto == null) {
                Toast.makeText(this, R.string.produto_form_msg_nao_encontrado, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            inputNome.setText(produto.getNome());
            inputDescricao.setText(produto.getDescricao());
            chipGroupCategoria.check(produto.isBebida() ? R.id.chipBebida : R.id.chipComida);
            inputPreco.setText(formatarPreco(produto.getPreco()));
            setBotoesHabilitados(true);
        });
    }

    /** Valida os campos e, se estiver tudo certo, insere ou atualiza o produto. */
    private void salvar() {
        String nome = textoDe(inputNome);
        String descricao = textoDe(inputDescricao);
        Double preco = lerPreco(textoDe(inputPreco));

        // Validação: mostramos o erro embaixo do próprio campo (setError).
        // setError(null) remove um erro que tenha aparecido antes.
        boolean nomeValido = !TextUtils.isEmpty(nome);
        boolean precoValido = preco != null && preco > 0;
        layoutNome.setError(nomeValido ? null : getString(R.string.produto_form_erro_nome));
        layoutPreco.setError(precoValido ? null : getString(R.string.produto_form_erro_preco));

        // Não chamamos requestFocus() aqui: o listener de foco apagaria o erro na hora.
        if (!nomeValido || !precoValido) {
            return;
        }

        String categoria = chipGroupCategoria.getCheckedChipId() == R.id.chipBebida
                ? Produto.CATEGORIA_BEBIDA
                : Produto.CATEGORIA_COMIDA;

        // id == 0 -> o repositório faz INSERT; id > 0 -> faz UPDATE.
        Produto produto = new Produto(produtoId, nome, descricao, categoria, preco);

        // Desabilita os botões para evitar salvar duas vezes com toques repetidos.
        setBotoesHabilitados(false);
        buttonSalvar.setText(R.string.produto_form_salvando);

        repository.salvar(produto, ok -> {
            if (isFinishing() || isDestroyed()) return;

            if (ok) {
                Toast.makeText(this, R.string.produto_form_msg_salvo, Toast.LENGTH_SHORT).show();
                // finish() fecha esta tela e volta para a anterior (a lista),
                // que recarrega os dados no onResume().
                finish();
            } else {
                Toast.makeText(this, R.string.produto_form_msg_erro_salvar, Toast.LENGTH_SHORT).show();
                buttonSalvar.setText(R.string.produto_form_salvar);
                setBotoesHabilitados(true);
            }
        });
    }

    /** Pergunta antes de excluir: ações destrutivas sempre pedem confirmação. */
    private void confirmarExclusao() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.produto_form_dialogo_titulo)
                .setMessage(R.string.produto_form_dialogo_mensagem)
                // Botão negativo sem ação (null): só fecha o diálogo.
                .setNegativeButton(R.string.produto_form_dialogo_cancelar, null)
                .setPositiveButton(R.string.produto_form_dialogo_confirmar, (dialogo, botao) -> excluir())
                .show();
    }

    private void excluir() {
        setBotoesHabilitados(false);
        repository.excluir(produtoId, ok -> {
            if (isFinishing() || isDestroyed()) return;

            if (ok) {
                Toast.makeText(this, R.string.produto_form_msg_excluido, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, R.string.produto_form_msg_erro_excluir, Toast.LENGTH_SHORT).show();
                setBotoesHabilitados(true);
            }
        });
    }

    /** Liga/desliga os botões enquanto uma operação no banco está em andamento. */
    private void setBotoesHabilitados(boolean habilitado) {
        buttonSalvar.setEnabled(habilitado);
        buttonExcluir.setEnabled(habilitado);
        // O estilo usa uma cor fixa de fundo, então deixamos o botão "apagado" manualmente.
        float alpha = habilitado ? 1f : 0.6f;
        buttonSalvar.setAlpha(alpha);
        buttonExcluir.setAlpha(alpha);
    }

    /**
     * Converte o texto digitado em número. Aceita "12,50" e "12.50":
     * trocamos a vírgula por ponto porque Double.parseDouble só entende ponto.
     * Devolve null se o texto não for um número válido.
     */
    private static Double lerPreco(String texto) {
        if (TextUtils.isEmpty(texto)) return null;
        try {
            return Double.parseDouble(texto.replace(',', '.'));
        } catch (NumberFormatException e) {
            return null; // ex.: "1,2,3" ou só ","
        }
    }

    /** Mostra o preço no formato brasileiro, com vírgula: 12.5 -> "12,50". */
    private static String formatarPreco(double preco) {
        return String.format(Locale.forLanguageTag("pt-BR"), "%.2f", preco);
    }

    private static String textoDe(TextInputEditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString().trim();
    }
}
