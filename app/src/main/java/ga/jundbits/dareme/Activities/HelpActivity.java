package ga.jundbits.dareme.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import ga.jundbits.dareme.R;

public class HelpActivity extends AppCompatActivity {

    private Toolbar helpToolbar;
    private TextView helpGoogleFormsText, helpWhatsAppText;

    private FirebaseFirestore firebaseFirestore;

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

        firebaseFirestore = FirebaseFirestore.getInstance();

    }

    private void setupToolbar() {

        setSupportActionBar(helpToolbar);
        getSupportActionBar().setTitle(getString(R.string.help));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void loadData() {

        firebaseFirestore.collection(getString(R.string.app_name_no_spaces)).document("AppCollections")
                .collection("App").document("Contact")
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        final String googleForms = documentSnapshot.getString("google_forms");
                        final String whatsApp = documentSnapshot.getString("whatsapp");

                        helpGoogleFormsText.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse(googleForms));
                                startActivity(intent);

                            }
                        });

                        helpWhatsAppText.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse(whatsApp));
                                startActivity(intent);

                            }
                        });

                    }
                });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            default:
                return false;

            case android.R.id.home:
                finish();
                return true;

        }

    }

}
