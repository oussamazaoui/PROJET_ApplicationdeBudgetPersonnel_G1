package ensa.application01.app_budget;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import ensa.application01.app_budget.DataBase.DatabaseHelper;
import ensa.application01.app_budget.adapter.TransactionAdapter;
import ensa.application01.app_budget.module.Transaction;

/**
 * HistoryActivity est une activité qui affiche l'historique des transactions.
 * Elle permet de filtrer les transactions par type, catégorie et date.
 */
public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerViewHistory; // RecyclerView pour afficher les transactions
    private DatabaseHelper dbHelper; // Helper pour interagir avec la base de données
    private TransactionAdapter adapter; // Adapteur pour le RecyclerView
    private Spinner spinnerTypeFilter; // Spinner pour filtrer par type
    private Spinner spinnerCategoryFilter; // Spinner pour filtrer par catégorie
    private Button btnDateFilter; // Bouton pour filtrer par date
    private Button btnResetFilters; // Bouton pour réinitialiser les filtres
    private String selectedType = "Tous"; // Filtre par type sélectionné
    private String selectedCategory = "Toutes"; // Filtre par catégorie sélectionné
    private String selectedDate = ""; // Filtre par date sélectionné

    /**
     * Méthode appelée lors de la création de l'activité.
     * Initialise les vues, configure les spinners et charge l'historique des transactions.
     *
     * @param savedInstanceState L'état enregistré de l'activité.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Initialisation des vues
        recyclerViewHistory = findViewById(R.id.recyclerView_history);
        dbHelper = new DatabaseHelper(this);

        // Configuration du RecyclerView
        adapter = new TransactionAdapter(new ArrayList<>());
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewHistory.setAdapter(adapter);

        // Initialisation des spinners et boutons
        spinnerTypeFilter = findViewById(R.id.spinner_type_filter);
        spinnerCategoryFilter = findViewById(R.id.spinner_category_filter);
        btnDateFilter = findViewById(R.id.btn_date_filter);
        btnResetFilters = findViewById(R.id.btn_reset_filters);

        // Configurer les spinners
        setupTypeFilter();
        setupCategoryFilter();

        // Gestion du filtre par date
        btnDateFilter.setOnClickListener(v -> showDatePickerDialog());

        // Réinitialiser les filtres
        btnResetFilters.setOnClickListener(v -> resetFilters());

        // Charger l'historique des transactions
        loadTransactionHistory();
    }

    /**
     * Configure le spinner pour filtrer les transactions par type.
     */
    private void setupTypeFilter() {
        List<String> types = new ArrayList<>();
        types.add("Tous"); // Option pour afficher tous les types
        types.add("Revenu");
        types.add("Dépense");

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTypeFilter.setAdapter(typeAdapter);

        // Gestion du changement de sélection pour le type
        spinnerTypeFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedType = parent.getItemAtPosition(position).toString();
                loadTransactionHistory();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /**
     * Configure le spinner pour filtrer les transactions par catégorie.
     */
    private void setupCategoryFilter() {
        List<String> categories = dbHelper.getAllCategoriesAsList();
        categories.add(0, "Toutes"); // Option pour afficher toutes les catégories

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoryFilter.setAdapter(categoryAdapter);

        // Gestion du changement de sélection pour la catégorie
        spinnerCategoryFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = parent.getItemAtPosition(position).toString();
                loadTransactionHistory();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /**
     * Affiche un DatePickerDialog pour sélectionner une date de filtrage.
     */
    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                    loadTransactionHistory();
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    /**
     * Réinitialise tous les filtres et recharge l'historique des transactions.
     */
    private void resetFilters() {
        selectedType = "Tous";
        selectedCategory = "Toutes";
        selectedDate = "";

        spinnerTypeFilter.setSelection(0);
        spinnerCategoryFilter.setSelection(0);
        loadTransactionHistory();
    }

    /**
     * Charge l'historique des transactions en fonction des filtres sélectionnés.
     */
    private void loadTransactionHistory() {
        List<Transaction> transactionHistory = getTransactionHistory(selectedType, selectedCategory, selectedDate);
        adapter.updateData(transactionHistory);
    }

    /**
     * Récupère l'historique des transactions en fonction des filtres appliqués.
     *
     * @param typeFilter     Le filtre par type (exemple : "Revenu", "Dépense").
     * @param categoryFilter Le filtre par catégorie (exemple : "Nourriture", "Transport").
     * @param dateFilter     Le filtre par date (exemple : "01/01/2023").
     * @return Une liste de transactions filtrées.
     */
    private List<Transaction> getTransactionHistory(String typeFilter, String categoryFilter, String dateFilter) {
        List<Transaction> history = new ArrayList<>();
        Cursor cursor = dbHelper.getAllTransactions();

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    do {
                        String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                        String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
                        String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));

                        // Appliquer les filtres
                        boolean matchesType = typeFilter.equals("Tous") || type.equals(typeFilter);
                        boolean matchesCategory = categoryFilter.equals("Toutes") || category.equals(categoryFilter);
                        boolean matchesDate = dateFilter.isEmpty() || date.equals(dateFilter);

                        if (matchesType && matchesCategory && matchesDate) {
                            double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
                            String note = cursor.getString(cursor.getColumnIndexOrThrow("note"));
                            boolean isAlarmEnabled = cursor.getInt(cursor.getColumnIndexOrThrow("isAlarmEnabled")) == 1;
                            String alarmDate = cursor.getString(cursor.getColumnIndexOrThrow("alarmDate"));
                            String alarmTime = cursor.getString(cursor.getColumnIndexOrThrow("alarmTime"));

                            history.add(new Transaction(type, amount, category, date, note, isAlarmEnabled, alarmDate, alarmTime));
                        }
                    } while (cursor.moveToNext());
                }
            } finally {
                cursor.close();
            }
        }

        return history;
    }

    /**
     * Méthode appelée lorsque l'activité reprend.
     * Recharge l'historique des transactions.
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadTransactionHistory();
    }
}