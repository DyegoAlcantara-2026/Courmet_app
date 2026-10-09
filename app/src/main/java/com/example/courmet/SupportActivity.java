package com.example.courmet;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SupportActivity extends AppCompatActivity {

    private static final long CHEVRON_ANIM_MS = 200L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_support);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        findViewById(R.id.buttonBack).setOnClickListener(v -> finish());

        setupFaq();
        setupContacts();
    }

    /** Infla um cartão expansível para cada pergunta; o primeiro começa aberto. */
    private void setupFaq() {
        ViewGroup container = findViewById(R.id.containerFaq);
        String[] questions = getResources().getStringArray(R.array.support_faq_questions);
        String[] answers = getResources().getStringArray(R.array.support_faq_answers);
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < questions.length; i++) {
            View card = inflater.inflate(R.layout.item_support_faq, container, false);
            TextView question = card.findViewById(R.id.textQuestion);
            TextView answer = card.findViewById(R.id.textAnswer);
            ImageView chevron = card.findViewById(R.id.imageChevron);

            question.setText(questions[i]);
            answer.setText(answers[i]);
            setExpanded(answer, chevron, i == 0, false);

            card.setOnClickListener(v ->
                    setExpanded(answer, chevron, answer.getVisibility() != View.VISIBLE, true));
            container.addView(card);
        }
    }

    private void setExpanded(TextView answer, ImageView chevron, boolean expanded, boolean animate) {
        answer.setVisibility(expanded ? View.VISIBLE : View.GONE);
        float rotation = expanded ? 180f : 0f;
        if (animate) {
            chevron.animate().rotation(rotation).setDuration(CHEVRON_ANIM_MS).start();
        } else {
            chevron.setRotation(rotation);
        }
    }

    private void setupContacts() {
        bindContact(R.id.contactChat, R.drawable.ic_support_chat,
                R.string.support_contact_chat_title, R.string.support_contact_chat_subtitle,
                v -> Toast.makeText(this, R.string.support_coming_soon, Toast.LENGTH_SHORT).show());

        bindContact(R.id.contactEmail, R.drawable.ic_support_mail,
                R.string.support_contact_email_title, R.string.support_contact_email_address,
                v -> openIntent(new Intent(Intent.ACTION_SENDTO,
                        Uri.parse("mailto:" + getString(R.string.support_contact_email_address)))));

        bindContact(R.id.contactPhone, R.drawable.ic_support_phone,
                R.string.support_contact_phone_title, R.string.support_contact_phone_number,
                v -> openIntent(new Intent(Intent.ACTION_DIAL,
                        Uri.parse("tel:" + getString(R.string.support_contact_phone_dial)))));
    }

    private void bindContact(int cardId, @DrawableRes int icon, @StringRes int title,
                             @StringRes int subtitle, View.OnClickListener listener) {
        View card = findViewById(cardId);
        ((ImageView) card.findViewById(R.id.imageIcon)).setImageResource(icon);
        ((TextView) card.findViewById(R.id.textTitle)).setText(title);
        ((TextView) card.findViewById(R.id.textSubtitle)).setText(subtitle);
        card.setOnClickListener(listener);
    }

    private void openIntent(Intent intent) {
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.support_no_app, Toast.LENGTH_SHORT).show();
        }
    }
}
