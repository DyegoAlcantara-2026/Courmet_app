package com.example.courmet.usuarios;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Tela de formulário de usuário: serve tanto para CRIAR CONTA quanto para EDITAR um usuário.
 *
 * Como a tela sabe o modo? Pelo Intent que a abriu:
 *  - sem o extra EXTRA_USUARIO_ID -> cadastro (criar conta, vindo do Login);
 *  - com o extra EXTRA_USUARIO_ID -> edição do usuário com aquele id.
 *
 * Exemplo de quem abre a tela para editar:
 *   Intent intent = new Intent(this, UsuarioFormActivity.class);
 *   intent.putExtra(UsuarioFormActivity.EXTRA_USUARIO_ID, usuario.getId());
 *   startActivity(intent);
 */
public class UsuarioFormActivity extends AppCompatActivity {

    /** Extra com o id do usuário a editar. Ausente = cadastro de nova conta. */
    public static final String EXTRA_USUARIO_ID = "usuario_id";

    /** Valor usado quando o extra não existe (o banco nunca gera id 0). */
    private static final long SEM_ID = 0;

    /** Tamanho mínimo da senha. */
    private static final int SENHA_MINIMO = 6;

    private UsuarioRepository repository;

    /** Id do usuário em edição, ou SEM_ID no modo cadastro. */
    private long usuarioId = SEM_ID;
    private boolean modoEdicao;

    private TextInputLayout layoutNome;
    private TextInputLayout layoutEmail;
    private TextInputLayout layoutSenha;
    private TextInputLayout layoutConfirmarSenha;
    private TextInputEditText inputNome;
    private TextInputEditText inputEmail;
    private TextInputEditText inputTelefone;
    private TextInputEditText inputSenha;
    private TextInputEditText inputConfirmarSenha;
    private MaterialButton buttonSalvar;
    private MaterialButton buttonExcluir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Desenha atrás das barras do sistema; o listener abaixo devolve esse espaço como padding.
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_usuario_form);
        // Type.ime() = teclado: quando ele abre, o padding de baixo cresce e o conteúdo
        // pode rolar até o último campo, sem ficar escondido atrás do teclado.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        repository = new UsuarioRepository(this);

        layoutNome = findViewById(R.id.layoutNome);
        layoutEmail = findViewById(R.id.layoutEmail);
        layoutSenha = findViewById(R.id.layoutSenha);
        layoutConfirmarSenha = findViewById(R.id.layoutConfirmarSenha);
        inputNome = findViewById(R.id.inputNome);
        inputEmail = findViewById(R.id.inputEmail);
        inputTelefone = findViewById(R.id.inputTelefone);
        inputSenha = findViewById(R.id.inputSenha);
        inputConfirmarSenha = findViewById(R.id.inputConfirmarSenha);
        buttonSalvar = findViewById(R.id.buttonSalvar);
        buttonExcluir = findViewById(R.id.buttonExcluir);
        TextView textTitulo = findViewById(R.id.textTitulo);

        // Lê o extra do Intent. O segundo parâmetro é o valor padrão caso o extra não exista.
        usuarioId = getIntent().getLongExtra(EXTRA_USUARIO_ID, SEM_ID);
        modoEdicao = usuarioId != SEM_ID;

        // Ajusta textos e elementos visíveis conforme o modo.
        textTitulo.setText(modoEdicao ? R.string.usuario_form_titulo_editar : R.string.usuario_form_titulo_criar);
        buttonSalvar.setText(modoEdicao ? R.string.usuario_form_salvar : R.string.usuario_form_criar);
        findViewById(R.id.textSubtitulo).setVisibility(modoEdicao ? View.GONE : View.VISIBLE);
        findViewById(R.id.grupoEntrar).setVisibility(modoEdicao ? View.GONE : View.VISIBLE);
        buttonExcluir.setVisibility(modoEdicao ? View.VISIBLE : View.GONE);
        if (modoEdicao) {
            // Texto de ajuda: aparece embaixo do campo enquanto não houver erro.
            layoutSenha.setHelperText(getString(R.string.usuario_form_senha_ajuda_editar));
        }

        findViewById(R.id.buttonVoltar).setOnClickListener(v -> finish());
        // "Já tem uma conta? Entrar": basta fechar esta tela para voltar ao Login.
        findViewById(R.id.buttonEntrar).setOnClickListener(v -> finish());
        buttonSalvar.setOnClickListener(v -> salvar());
        buttonExcluir.setOnClickListener(v -> confirmarExclusao());

        // Quando o usuário volta a um campo com erro, a mensagem some para ele corrigir.
        limparErroAoFocar(inputNome, layoutNome);
        limparErroAoFocar(inputEmail, layoutEmail);
        limparErroAoFocar(inputSenha, layoutSenha);
        limparErroAoFocar(inputConfirmarSenha, layoutConfirmarSenha);

        // savedInstanceState != null quando a tela foi recriada (ex.: girou o celular).
        // Nesse caso o Android já restaura o texto dos campos, então NÃO recarregamos
        // do banco (senão apagaríamos o que o usuário já tinha digitado).
        if (modoEdicao && savedInstanceState == null) {
            carregarUsuario();
        }
    }

    /** Modo edição: busca o usuário no banco e preenche os campos (a senha nunca é carregada). */
    private void carregarUsuario() {
        setBotoesHabilitados(false);
        // A busca é assíncrona: o código dentro da lambda (callback) só roda
        // quando o resultado chegar, já de volta na thread principal.
        repository.buscarPorId(usuarioId, usuario -> {
            // Se o usuário saiu da tela enquanto esperava, não mexemos mais nela.
            if (isFinishing() || isDestroyed()) return;

            if (usuario == null) {
                Toast.makeText(this, R.string.usuario_form_msg_nao_encontrado, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            inputNome.setText(usuario.getNome());
            inputEmail.setText(usuario.getEmail());
            inputTelefone.setText(usuario.getTelefone());
            setBotoesHabilitados(true);
        });
    }

    /** Valida os campos e, se estiver tudo certo, cria ou atualiza o usuário. */
    private void salvar() {
        String nome = textoDe(inputNome);
        String email = textoDe(inputEmail);
        String telefone = textoDe(inputTelefone);
        // A senha não leva trim(): espaços fazem parte da senha.
        String senha = inputSenha.getText() == null ? "" : inputSenha.getText().toString();
        String confirmacao = inputConfirmarSenha.getText() == null ? "" : inputConfirmarSenha.getText().toString();

        // Validação: mostramos o erro embaixo do próprio campo (setError).
        // setError(null) remove um erro que tenha aparecido antes.
        boolean nomeValido = !TextUtils.isEmpty(nome);
        // Patterns.EMAIL_ADDRESS é uma expressão regular pronta do Android para e-mails.
        boolean emailValido = Patterns.EMAIL_ADDRESS.matcher(email).matches();

        // No cadastro a senha é obrigatória. Na edição, senha vazia = manter a atual;
        // se o usuário digitou algo, valem as mesmas regras do cadastro.
        boolean vaiTrocarSenha = !modoEdicao || !senha.isEmpty() || !confirmacao.isEmpty();
        boolean senhaValida = !vaiTrocarSenha || senha.length() >= SENHA_MINIMO;
        boolean confirmacaoValida = !vaiTrocarSenha || senha.equals(confirmacao);

        layoutNome.setError(nomeValido ? null : getString(R.string.usuario_form_erro_nome));
        layoutEmail.setError(emailValido ? null : getString(R.string.usuario_form_erro_email));
        layoutSenha.setError(senhaValida ? null : getString(R.string.usuario_form_erro_senha));
        layoutConfirmarSenha.setError(confirmacaoValida ? null : getString(R.string.usuario_form_erro_confirmar));

        // Não chamamos requestFocus() aqui: o listener de foco apagaria o erro na hora.
        if (!nomeValido || !emailValido || !senhaValida || !confirmacaoValida) {
            return;
        }

        // id == 0 -> o repositório faz INSERT; id > 0 -> faz UPDATE.
        Usuario usuario = new Usuario(usuarioId, nome, email, telefone);
        // null = "não mexer na senha" (só acontece na edição com o campo em branco).
        String senhaParaSalvar = vaiTrocarSenha ? senha : null;

        // Desabilita os botões para evitar salvar duas vezes com toques repetidos.
        setBotoesHabilitados(false);
        buttonSalvar.setText(R.string.usuario_form_salvando);

        repository.salvar(usuario, senhaParaSalvar, resultado -> {
            if (isFinishing() || isDestroyed()) return;

            // O switch trata cada valor possível do enum ResultadoSalvar.
            switch (resultado) {
                case OK:
                    Toast.makeText(this,
                            modoEdicao ? R.string.usuario_form_msg_atualizado : R.string.usuario_form_msg_criado,
                            Toast.LENGTH_SHORT).show();
                    // finish() fecha esta tela e volta para a anterior (Login ou lista).
                    finish();
                    return;
                case EMAIL_EM_USO:
                    layoutEmail.setError(getString(R.string.usuario_form_erro_email_em_uso));
                    break;
                default: // ERRO
                    Toast.makeText(this, R.string.usuario_form_msg_erro_salvar, Toast.LENGTH_SHORT).show();
                    break;
            }
            // Não deu certo: devolve o botão ao normal para o usuário tentar de novo.
            buttonSalvar.setText(modoEdicao ? R.string.usuario_form_salvar : R.string.usuario_form_criar);
            setBotoesHabilitados(true);
        });
    }

    /** Pergunta antes de excluir: ações destrutivas sempre pedem confirmação. */
    private void confirmarExclusao() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.usuario_form_dialogo_titulo)
                .setMessage(R.string.usuario_form_dialogo_mensagem)
                // Botão negativo sem ação (null): só fecha o diálogo.
                .setNegativeButton(R.string.usuario_form_dialogo_cancelar, null)
                .setPositiveButton(R.string.usuario_form_dialogo_confirmar, (dialogo, botao) -> excluir())
                .show();
    }

    private void excluir() {
        setBotoesHabilitados(false);
        repository.excluir(usuarioId, ok -> {
            if (isFinishing() || isDestroyed()) return;

            if (ok) {
                Toast.makeText(this, R.string.usuario_form_msg_excluido, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, R.string.usuario_form_msg_erro_excluir, Toast.LENGTH_SHORT).show();
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

    /** Ao entrar no campo, apaga o erro dele (o texto de ajuda, se houver, volta a aparecer). */
    private static void limparErroAoFocar(TextInputEditText campo, TextInputLayout layout) {
        campo.setOnFocusChangeListener((v, temFoco) -> { if (temFoco) layout.setError(null); });
    }

    private static String textoDe(TextInputEditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString().trim();
    }
}
