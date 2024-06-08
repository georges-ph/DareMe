package ga.jundbits.dareme.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.GenericTypeIndicator;

import java.util.Map;

import ga.jundbits.dareme.R;
import ga.jundbits.dareme.Utils.FirebaseHelper;

public class HelpActivity extends AppCompatActivity {

    private Toolbar helpToolbar;
    private TextView helpGoogleFormsText, helpWhatsAppText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        initVars();
        setupToolbar();
        loadData();

    }

    private void initVars() {

        helpToolbar = findViewById(R.id.help_toolbar);
        helpGoogleFormsText = findViewById(R.id.help_google_forms_text);
        helpWhatsAppText = findViewById(R.id.help_whatsapp_text);

    }

    private void setupToolbar() {

        setSupportActionBar(helpToolbar);
        getSupportActionBar().setTitle(getString(R.string.help));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void loadData() {

        FirebaseHelper.databaseReference("contact").get().addOnSuccessListener(this, dataSnapshot -> {

            Map<String, String> contact = dataSnapshot.getValue(new GenericTypeIndicator<Map<String, String>>() {});

            String googleForms = contact.get("google_forms");
            String whatsApp = contact.get("whatsapp");

            helpGoogleFormsText.setOnClickListener(v -> {

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(googleForms));
                startActivity(intent);

            });

            helpWhatsAppText.setOnClickListener(v -> {

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(whatsApp));
                startActivity(intent);

            });

        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return false;

    }

}
