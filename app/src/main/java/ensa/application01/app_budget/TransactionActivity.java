package ensa.application01.app_budget;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import ensa.application01.app_budget.DataBase.DatabaseHelper;
import ensa.application01.app_budget.Receiver.AlarmReceiver;

/**
 * TransactionActivity est une activité permettant à l'utilisateur d'ajouter une nouvelle transaction.
 * Elle gère la saisie des informations de la transaction (montant, type, catégorie, date, note)
 * et permet de programmer une alarme pour les transactions récurrentes.
 */
public class TransactionActivity extends AppCompatActivity {

    private EditText etAmount; // Champ de saisie pour le montant
    private RadioGroup rgType; // Groupe de boutons radio pour le type (Revenu ou Dépense)
    private EditText etDate; // Champ de saisie pour la date de la transaction
    private EditText etNote; // Champ de saisie pour la note
    private CheckBox cbAlarm; // Case à cocher pour activer/désactiver l'alarme
    private EditText etAlarmDate; // Champ de saisie pour la date de l'alarme
    private EditText etAlarmTime; // Champ de saisie pour l'heure de l'alarme
    private Button btnSave; // Bouton pour sauvegarder la transaction
    private EditText etCategory; // Champ de saisie pour la catégorie
    private DatabaseHelper dbHelper; // Helper pour interagir avec la base de données
    private List<String> categories; // Liste des catégories disponibles
    private String selectedCategory = ""; // Catégorie sélectionnée

    /**
     * Méthode appelée lors de la création de l'activité.
     * Initialise les vues, configure les écouteurs d'événements et charge les catégories.
     *
     * @param savedInstanceState L'état enregistré de l'activité.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);

        // Initialisation des vues
        etAmount = findViewById(R.id.et_amount);
        rgType = findViewById(R.id.rg_type);
        etDate = findViewById(R.id.et_date);
        etNote = findViewById(R.id.et_note);
        cbAlarm = findViewById(R.id.cb_alarm);
        etAlarmDate = findViewById(R.id.et_alarm_date);
        etAlarmTime = findViewById(R.id.et_alarm_time);
        btnSave = findViewById(R.id.btn_save);
        etCategory = findViewById(R.id.et_category);

        LinearLayout alarmFields = findViewById(R.id.alarm_fields);

        dbHelper = new DatabaseHelper(this);
        categories = dbHelper.getAllCategoriesAsList();

        // Définir la date actuelle par défaut
        etDate.setText(getCurrentDate());

        // Gestion du clic sur le champ de catégorie
        etCategory.setOnClickListener(v -> showCategoryDialog());

        // Gestion de l'activation/désactivation de l'alarme
        cbAlarm.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Log.d("TransactionActivity", "Alarme activée - Affichage des champs d'alarme");
                alarmFields.setVisibility(View.VISIBLE);
            } else {
                Log.d("TransactionActivity", "Alarme désactivée - Masquage des champs d'alarme");
                alarmFields.setVisibility(View.GONE);
            }
        });

        // Gestion du clic sur le bouton "Sauvegarder"
        btnSave.setOnClickListener(v -> {
            Log.d("TransactionActivity", "Bouton Sauvegarder cliqué");

            btnSave.setEnabled(false);

            // Validation des champs obligatoires
            if (etAmount.getText().toString().isEmpty() ||
                    rgType.getCheckedRadioButtonId() == -1 ||
                    etDate.getText().toString().isEmpty() ||
                    selectedCategory.isEmpty()) {
                Toast.makeText(TransactionActivity.this, "Veuillez remplir tous les champs obligatoires.", Toast.LENGTH_SHORT).show();
                btnSave.setEnabled(true);
                return;
            }

            // Validation du format de la date de transaction
            String transactionDate = etDate.getText().toString();
            if (!isValidDateFormat(transactionDate)) {
                Toast.makeText(this, "Format de date invalide. Utilisez dd/MM/yyyy.", Toast.LENGTH_SHORT).show();
                btnSave.setEnabled(true);
                return;
            }

            // Validation de la date réelle de transaction
            if (!isDateValid(transactionDate)) {
                Toast.makeText(this, "Date de transaction invalide. Veuillez entrer une date valide.", Toast.LENGTH_SHORT).show();
                btnSave.setEnabled(true);
                return;
            }

            boolean isAlarmEnabled = cbAlarm.isChecked();
            String alarmDate = etAlarmDate.getText().toString();
            String alarmTime = etAlarmTime.getText().toString();

            if (isAlarmEnabled) {
                // Validation du format de la date d'alarme
                if (!isValidDateFormat(alarmDate)) {
                    Toast.makeText(this, "Format de date d'alarme invalide. Utilisez dd/MM/yyyy.", Toast.LENGTH_SHORT).show();
                    btnSave.setEnabled(true);
                    return;
                }

                // Validation de la date réelle d'alarme
                if (!isDateValid(alarmDate)) {
                    Toast.makeText(this, "Date d'alarme invalide. Veuillez entrer une date valide.", Toast.LENGTH_SHORT).show();
                    btnSave.setEnabled(true);
                    return;
                }

                if (alarmDate.isEmpty() || alarmTime.isEmpty()) {
                    Toast.makeText(this, "Veuillez remplir la date et l'heure de l'alarme.", Toast.LENGTH_SHORT).show();
                    btnSave.setEnabled(true);
                    return;
                }

                if (!isAlarmDateTimeValid(alarmDate, alarmTime)) {
                    Toast.makeText(this, "La date et l'heure de l'alarme doivent être dans le futur.", Toast.LENGTH_SHORT).show();
                    btnSave.setEnabled(true);
                    return;
                }
            }

            double amount;
            try {
                amount = Double.parseDouble(etAmount.getText().toString());
            } catch (NumberFormatException e) {
                Toast.makeText(TransactionActivity.this, "Veuillez entrer un montant valide.", Toast.LENGTH_SHORT).show();
                btnSave.setEnabled(true);
                return;
            }

            int selectedId = rgType.getCheckedRadioButtonId();
            RadioButton selectedRadioButton = findViewById(selectedId);
            String type = selectedRadioButton.getText().toString();

            if (isAlarmEnabled) {
                scheduleAlarm(alarmDate, alarmTime, etNote.getText().toString());
            }

            ContentValues values = new ContentValues();
            values.put("type", type);
            values.put("amount", amount);
            values.put("category", selectedCategory);
            values.put("date", etDate.getText().toString());
            values.put("note", etNote.getText().toString());
            values.put("isAlarmEnabled", isAlarmEnabled ? 1 : 0);
            values.put("alarmDate", alarmDate);
            values.put("alarmTime", alarmTime);

            long newRowId = dbHelper.getWritableDatabase().insert("transactions", null, values);

            if (newRowId == -1) {
                Log.e("TransactionActivity", "Erreur lors de l'insertion de la transaction");
                Toast.makeText(this, "Erreur lors de la sauvegarde", Toast.LENGTH_SHORT).show();
            } else {
                Log.d("TransactionActivity", "Transaction insérée avec succès, ID: " + newRowId);
                Toast.makeText(this, "Transaction sauvegardée", Toast.LENGTH_SHORT).show();
            }

            Intent resultIntent = new Intent();
            resultIntent.putExtra("type", type);
            resultIntent.putExtra("amount", amount);
            resultIntent.putExtra("category", selectedCategory);
            resultIntent.putExtra("date", etDate.getText().toString());
            resultIntent.putExtra("note", etNote.getText().toString());
            resultIntent.putExtra("isAlarmEnabled", isAlarmEnabled);
            resultIntent.putExtra("alarmDate", alarmDate);
            resultIntent.putExtra("alarmTime", alarmTime);

            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    /**
     * Retourne la date actuelle au format "dd/MM/yyyy".
     *
     * @return La date actuelle sous forme de chaîne.
     */
    private String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }

    /**
     * Affiche une boîte de dialogue pour sélectionner une catégorie.
     */
    private void showCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sélectionner une catégorie");

        String[] categoryArray = categories.toArray(new String[0]);

        builder.setItems(categoryArray, (dialog, which) -> {
            selectedCategory = categoryArray[which];
            etCategory.setText(selectedCategory);
        });

        builder.setNegativeButton("Annuler", null);
        builder.show();
    }

    /**
     * Vérifie si la date et l'heure de l'alarme sont valides (dans le futur).
     *
     * @param alarmDate La date de l'alarme au format "dd/MM/yyyy".
     * @param alarmTime L'heure de l'alarme au format "HH:mm".
     * @return `true` si la date et l'heure sont valides, sinon `false`.
     */
    private boolean isAlarmDateTimeValid(String alarmDate, String alarmTime) {
        Calendar currentCalendar = Calendar.getInstance();

        Calendar alarmCalendar = Calendar.getInstance();
        String[] dateParts = alarmDate.split("/");
        String[] timeParts = alarmTime.split(":");

        alarmCalendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateParts[0]));
        alarmCalendar.set(Calendar.MONTH, Integer.parseInt(dateParts[1]) - 1);
        alarmCalendar.set(Calendar.YEAR, Integer.parseInt(dateParts[2]));
        alarmCalendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParts[0]));
        alarmCalendar.set(Calendar.MINUTE, Integer.parseInt(timeParts[1]));
        alarmCalendar.set(Calendar.SECOND, 0);

        return alarmCalendar.after(currentCalendar);
    }

    /**
     * Programme une alarme pour la transaction.
     *
     * @param alarmDate La date de l'alarme au format "dd/MM/yyyy".
     * @param alarmTime L'heure de l'alarme au format "HH:mm".
     * @param note      La note associée à la transaction.
     */
    private void scheduleAlarm(String alarmDate, String alarmTime, String note) {
        Calendar calendar = Calendar.getInstance();
        String[] dateParts = alarmDate.split("/");
        String[] timeParts = alarmTime.split(":");

        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateParts[0]));
        calendar.set(Calendar.MONTH, Integer.parseInt(dateParts[1]) - 1);
        calendar.set(Calendar.YEAR, Integer.parseInt(dateParts[2]));
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParts[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(timeParts[1]));
        calendar.set(Calendar.SECOND, 0);

        long alarmTimeInMillis = calendar.getTimeInMillis();

        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("alarmTitle", "Rappel de transaction");
        intent.putExtra("alarmMessage", note);

        int requestCode = (int) System.currentTimeMillis(); // ID unique basé sur l'horodatage
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTimeInMillis, pendingIntent);
            Log.d("TransactionActivity", "Alarme programmée avec succès pour : " + alarmDate + " " + alarmTime);
        } else {
            Log.e("TransactionActivity", "Erreur : AlarmManager est null");
        }
    }

    /**
     * Vérifie si une date est au format valide "dd/MM/yyyy".
     *
     * @param date La date à valider.
     * @return `true` si la date est au format valide, sinon `false`.
     */
    private boolean isValidDateFormat(String date) {
        String regex = "^(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[012])/(19|20)\\d\\d$";
        return date.matches(regex);
    }

    /**
     * Vérifie si une date est valide (existe dans le calendrier).
     *
     * @param date La date à valider.
     * @return `true` si la date est valide, sinon `false`.
     */
    private boolean isDateValid(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        dateFormat.setLenient(false); // Désactiver la tolérance pour des dates invalides

        try {
            dateFormat.parse(date); // Tenter de parser la date
            return true; // La date est valide
        } catch (Exception e) {
            return false; // La date est invalide
        }
    }
}