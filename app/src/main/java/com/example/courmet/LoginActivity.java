package com.example.courmet;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.courmet.usuarios.UsuarioFormActivity;
import com.example.courmet.usuarios.UsuarioRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout layoutEmail;
    private TextInputLayout layoutPassword;
    private TextInputEditText inputEmail;
    private TextInputEditText inputPassword;
    private MaterialButton buttonLogin;

    private UsuarioRepository usuarioRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        usuarioRepository = new UsuarioRepository(this);

        layoutEmail = findViewById(R.id.layoutEmail);
        layoutPassword = findViewById(R.id.layoutPassword);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        buttonLogin = findViewById(R.id.buttonLogin);

        buttonLogin.setOnClickListener(v -> attemptLogin());
        findViewById(R.id.buttonForgotPassword).setOnClickListener(v -> showNotImplemented());
        findViewById(R.id.buttonGoogle).setOnClickListener(v -> showNotImplemented());
        findViewById(R.id.buttonApple).setOnClickListener(v -> showNotImplemented());
        // "Cadastre-se": abre o formulário de usuário SEM o extra de id -> modo "Criar conta"
        findViewById(R.id.buttonSignUp).setOnClickListener(v ->
                startActivity(new Intent(this, UsuarioFormActivity.class)));
    }

    private void attemptLogin() {
        String email = textOf(inputEmail);
        String password = textOf(inputPassword);

        boolean emailValid = Patterns.EMAIL_ADDRESS.matcher(email).matches();
        boolean passwordValid = !TextUtils.isEmpty(password);

        layoutEmail.setError(emailValid ? null : getString(R.string.login_error_email));
        layoutPassword.setError(passwordValid ? null : getString(R.string.login_error_password));

        if (!emailValid || !passwordValid) {
            return;
        }

        // Confere e-mail e senha no banco (em segundo plano; a resposta chega no callback)
        buttonLogin.setEnabled(false);
        usuarioRepository.autenticar(email, password, usuario -> {
            if (isFinishing() || isDestroyed()) return;
            buttonLogin.setEnabled(true);

            if (usuario == null) {
                layoutPassword.setError(getString(R.string.login_error_credentials));
                return;
            }

            // Intent explícita: dizemos exatamente qual Activity abrir (a Home),
            // e mandamos o nome do usuário como "extra" para a saudação.
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(MainActivity.EXTRA_NOME_USUARIO, usuario.getNome());
            startActivity(intent);
            // finish() remove o Login da pilha (back stack). Assim, ao apertar
            // "Voltar" na Home, o usuário sai do app em vez de voltar ao Login.
            finish();
        });
    }

    private void showNotImplemented() {
        Toast.makeText(this, R.string.not_implemented, Toast.LENGTH_SHORT).show();
    }

    private static String textOf(TextInputEditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }
}
