package ensa.application01.app_budget;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import ensa.application01.app_budget.DataBase.DatabaseHelper;

/**
 * MainActivity est l'activité principale de l'application.
 * Elle affiche un résumé des dépenses, des revenus et du solde, et permet de naviguer
 * vers d'autres activités pour ajouter des transactions, visualiser des graphiques,
 * consulter l'historique et gérer les catégories.
 */
public class MainActivity extends AppCompatActivity {

    private TextView tvExpenses; // Affiche le total des dépenses
    private TextView tvIncome; // Affiche le total des revenus
    private TextView tvBalance; // Affiche le solde (revenus - dépenses)
    private double totalExpenses = 0.0; // Total des dépenses
    private double totalIncome = 0.0; // Total des revenus
    private DatabaseHelper dbHelper; // Helper pour interagir avec la base de données

    /**
     * Méthode appelée lors de la création de l'activité.
     * Initialise les vues, configure les boutons et charge les totaux depuis la base de données.
     *
     * @param savedInstanceState L'état enregistré de l'activité.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this); // Initialisation du DatabaseHelper

        // Initialisation des vues
        tvExpenses = findViewById(R.id.tv_expenses);
        tvIncome = findViewById(R.id.tv_budget);
        tvBalance = findViewById(R.id.tv_balance);

        // Réinitialiser les totaux à partir de la base de données
        resetTotalsFromDatabase();

        // Initialisation des boutons
        Button btnAdd = findViewById(R.id.button_add_transaction);
        Button btnChart = findViewById(R.id.button_view_graph);
        Button btnHistory = findViewById(R.id.button_view_history);
        Button btnManageCategories = findViewById(R.id.button_manage_categories);

        // Gestion du clic sur le bouton "Ajouter une transaction"
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TransactionActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        // Gestion du clic sur le bouton "Voir le graphique"
        btnChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GraphActivity.class);
                intent.putExtra("expenses", totalExpenses);
                intent.putExtra("income", totalIncome);
                startActivity(intent);
            }
        });

        // Gestion du clic sur le bouton "Voir l'historique"
        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                intent.putExtra("history", getTransactionHistory()); // Récupérer l'historique depuis la base de données
                startActivity(intent);
            }
        });

        // Gestion du clic sur le bouton "Gérer les catégories"
        btnManageCategories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ManageCategoriesActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Ajoute une transaction à la base de données.
     *
     * @param type           Le type de la transaction (exemple : "Revenu" ou "Dépense").
     * @param amount         Le montant de la transaction.
     * @param category       La catégorie de la transaction (exemple : "Nourriture", "Transport").
     * @param date           La date de la transaction au format "dd/MM/yyyy".
     * @param note           Une note ou description facultative de la transaction.
     * @param isAlarmEnabled Indique si une alarme est activée pour cette transaction.
     * @param alarmDate      La date de l'alarme au format "dd/MM/yyyy".
     * @param alarmTime      L'heure de l'alarme au format "HH:mm".
     */
    private void addTransactionToDatabase(String type, double amount, String category, String date, String note, boolean isAlarmEnabled, String alarmDate, String alarmTime) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("type", type);
        values.put("amount", amount);
        values.put("category", category);
        values.put("date", date);
        values.put("note", note);
        values.put("isAlarmEnabled", isAlarmEnabled ? 1 : 0); // Convertir boolean en int
        values.put("alarmDate", alarmDate);
        values.put("alarmTime", alarmTime);

        db.insert("transactions", null, values);
        db.close();
    }

    /**
     * Méthode appelée lorsqu'une activité lancée avec startActivityForResult retourne un résultat.
     * Ajoute la transaction à la base de données et met à jour l'interface utilisateur.
     *
     * @param requestCode Le code de requête passé à startActivityForResult.
     * @param resultCode  Le code de résultat retourné par l'activité enfant.
     * @param data        L'intent contenant les données de la transaction.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String type = data.getStringExtra("type");
            double amount = data.getDoubleExtra("amount", 0.0);
            String category = data.getStringExtra("category");
            String date = data.getStringExtra("date");
            String note = data.getStringExtra("note");
            boolean isAlarmEnabled = data.getBooleanExtra("isAlarmEnabled", false);
            String alarmDate = data.getStringExtra("alarmDate");
            String alarmTime = data.getStringExtra("alarmTime");

            // Sauvegarder la transaction dans la base de données
            addTransactionToDatabase(type, amount, category, date, note, isAlarmEnabled, alarmDate, alarmTime);

            // Mettre à jour les totaux et l'interface utilisateur
            if (type != null) {
                if (type.equals("Revenu")) {
                    totalIncome += amount;
                } else if (type.equals("Dépense")) {
                    totalExpenses += amount;
                }
            }

            tvExpenses.setText(String.format("%.2f", totalExpenses));
            tvIncome.setText(String.format("%.2f", totalIncome));
            tvBalance.setText(String.format("%.2f", totalIncome - totalExpenses));
        }
    }

    /**
     * Récupère l'historique des transactions depuis la base de données.
     *
     * @return Une liste de chaînes représentant les transactions.
     */
    private ArrayList<String> getTransactionHistory() {
        ArrayList<String> history = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("transactions", null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                String type = cursor.getString(cursor.getColumnIndex("type"));
                double amount = cursor.getDouble(cursor.getColumnIndex("amount"));
                String category = cursor.getString(cursor.getColumnIndex("category"));
                String date = cursor.getString(cursor.getColumnIndex("date"));
                String note = cursor.getString(cursor.getColumnIndex("note"));
                history.add(type + ": " + String.format("%.2f", amount) + " (" + date + ")");
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return history;
    }

    /**
     * Réinitialise les totaux des dépenses et des revenus en les chargeant depuis la base de données.
     */
    private void resetTotalsFromDatabase() {
        totalExpenses = 0.0;
        totalIncome = 0.0;

        Cursor cursor = dbHelper.getReadableDatabase().query("transactions", null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                String type = cursor.getString(cursor.getColumnIndex("type"));
                double amount = cursor.getDouble(cursor.getColumnIndex("amount"));

                if (type.equals("Revenu")) {
                    totalIncome += amount;
                } else if (type.equals("Dépense")) {
                    totalExpenses += amount;
                }
            } while (cursor.moveToNext());
        }

        cursor.close();

        // Mettre à jour l'interface utilisateur
        tvExpenses.setText(String.format("%.2f", totalExpenses));
        tvIncome.setText(String.format("%.2f", totalIncome));
        tvBalance.setText(String.format("%.2f", totalIncome - totalExpenses));
    }
}