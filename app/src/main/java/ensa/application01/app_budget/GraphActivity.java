package ensa.application01.app_budget;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import ensa.application01.app_budget.DataBase.DatabaseHelper;

/**
 * GraphActivity est une activité qui affiche des graphiques et des statistiques
 * sur les revenus et les dépenses de l'utilisateur. Elle permet de visualiser les données
 * par mois et par année, avec un graphique en camembert et des cartes récapitulatives.
 */
public class GraphActivity extends AppCompatActivity {

    private LinearLayout layoutOutgoings; // Layout pour afficher les dépenses
    private LinearLayout layoutIncomes; // Layout pour afficher les revenus
    private Spinner spinnerMonth; // Spinner pour sélectionner le mois
    private Spinner spinnerYear; // Spinner pour sélectionner l'année
    private PieChart pieChart; // Graphique en camembert pour visualiser les données
    private DatabaseHelper dbHelper; // Helper pour interagir avec la base de données

    private static final String TAG = "GraphActivity"; // Tag pour les logs
    private String[] years = {"2023", "2024", "2025"}; // Liste des années disponibles

    /**
     * Méthode appelée lors de la création de l'activité.
     * Initialise les vues, configure les spinners et met à jour l'interface utilisateur.
     *
     * @param savedInstanceState L'état enregistré de l'activité.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        // Initialisation des vues
        layoutOutgoings = findViewById(R.id.layoutOutgoings);
        layoutIncomes = findViewById(R.id.layoutIncomes);
        spinnerMonth = findViewById(R.id.spinnerMonth);
        spinnerYear = findViewById(R.id.spinnerYear);
        pieChart = findViewById(R.id.pieChart);

        dbHelper = new DatabaseHelper(this);

        // Configuration du spinner pour les mois
        String[] months = {"Janvier", "Février", "Mars", "Avril", "Mai", "Juin", "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"};
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);

        // Configuration du spinner pour les années
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);

        // Sélectionner l'année actuelle par défaut
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int yearPosition = Arrays.asList(years).indexOf(String.valueOf(currentYear));
        if (yearPosition != -1) {
            spinnerYear.setSelection(yearPosition);
        }

        // Gestion du changement de sélection pour le mois
        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateUI();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Gestion du changement de sélection pour l'année
        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateUI();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /**
     * Met à jour l'interface utilisateur en fonction du mois et de l'année sélectionnés.
     * Récupère les transactions de la base de données, affiche les cartes récapitulatives
     * et met à jour le graphique en camembert.
     */
    private void updateUI() {
        int month = spinnerMonth.getSelectedItemPosition() + 1; // Récupérer le mois sélectionné
        String year = years[spinnerYear.getSelectedItemPosition()]; // Récupérer l'année sélectionnée

        // Récupérer les transactions mensuelles par catégorie
        Cursor monthlyTransactions = dbHelper.getMonthlyTransactionsByCategory(String.format("%02d", month), year);

        if (monthlyTransactions == null) {
            return;
        }

        // Effacer les anciennes vues
        layoutOutgoings.removeAllViews();
        layoutIncomes.removeAllViews();

        // Remplir les revenus et les dépenses
        fillIncomes(monthlyTransactions);
        fillOutgoings(monthlyTransactions);

        // Mettre à jour le graphique en camembert
        updatePieChart(monthlyTransactions);

        monthlyTransactions.close();
    }

    /**
     * Remplit le layout des revenus avec des cartes récapitulatives pour chaque catégorie.
     *
     * @param monthlyTransactions Le curseur contenant les transactions mensuelles.
     */
    private void fillIncomes(Cursor monthlyTransactions) {
        Map<String, Double> categoryTotals = new HashMap<>(); // Total des revenus par catégorie

        if (monthlyTransactions.moveToFirst()) {
            do {
                String category = monthlyTransactions.getString(monthlyTransactions.getColumnIndex(DatabaseHelper.COLUMN_CATEGORY));
                String type = monthlyTransactions.getString(monthlyTransactions.getColumnIndex(DatabaseHelper.COLUMN_TYPE));
                double total = monthlyTransactions.getDouble(monthlyTransactions.getColumnIndex("total"));

                if (type.equals("Revenu")) {
                    // Mettre à jour le total pour cette catégorie
                    if (categoryTotals.containsKey(category)) {
                        categoryTotals.put(category, categoryTotals.get(category) + total);
                    } else {
                        categoryTotals.put(category, total);
                    }
                }
            } while (monthlyTransactions.moveToNext());
        }

        // Ajouter des cartes pour chaque catégorie de revenus
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            String category = entry.getKey();
            double total = entry.getValue();

            // Créer une carte
            View cardView = getLayoutInflater().inflate(R.layout.card_item, null);
            TextView tvCategoryTitle = cardView.findViewById(R.id.tvCategoryTitle);
            TextView tvTotalAmount = cardView.findViewById(R.id.tvTotalAmount);

            // Remplir la carte avec les données
            tvCategoryTitle.setText(category);
            tvTotalAmount.setText(String.format("%.2f DH", total));

            // Ajouter la carte au layout des revenus
            layoutIncomes.addView(cardView);
        }
    }

    /**
     * Remplit le layout des dépenses avec des cartes récapitulatives pour chaque catégorie.
     *
     * @param monthlyTransactions Le curseur contenant les transactions mensuelles.
     */
    private void fillOutgoings(Cursor monthlyTransactions) {
        Map<String, Double> categoryTotals = new HashMap<>(); // Total des dépenses par catégorie

        if (monthlyTransactions.moveToFirst()) {
            do {
                String category = monthlyTransactions.getString(monthlyTransactions.getColumnIndex(DatabaseHelper.COLUMN_CATEGORY));
                String type = monthlyTransactions.getString(monthlyTransactions.getColumnIndex(DatabaseHelper.COLUMN_TYPE));
                double total = monthlyTransactions.getDouble(monthlyTransactions.getColumnIndex("total"));

                if (type.equals("Dépense")) {
                    // Mettre à jour le total pour cette catégorie
                    if (categoryTotals.containsKey(category)) {
                        categoryTotals.put(category, categoryTotals.get(category) + total);
                    } else {
                        categoryTotals.put(category, total);
                    }
                }
            } while (monthlyTransactions.moveToNext());
        }

        // Ajouter des cartes pour chaque catégorie de dépenses
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            String category = entry.getKey();
            double total = entry.getValue();

            // Créer une carte
            View cardView = getLayoutInflater().inflate(R.layout.card_item, null);
            TextView tvCategoryTitle = cardView.findViewById(R.id.tvCategoryTitle);
            TextView tvTotalAmount = cardView.findViewById(R.id.tvTotalAmount);

            // Remplir la carte avec les données
            tvCategoryTitle.setText(category);
            tvTotalAmount.setText(String.format("%.2f DH", total));

            // Ajouter la carte au layout des dépenses
            layoutOutgoings.addView(cardView);
        }
    }

    /**
     * Met à jour le graphique en camembert avec les totaux des revenus et des dépenses.
     *
     * @param monthlyTransactions Le curseur contenant les transactions mensuelles.
     */
    private void updatePieChart(Cursor monthlyTransactions) {
        float totalRevenus = 0; // Total des revenus
        float totalDepenses = 0; // Total des dépenses

        if (monthlyTransactions.moveToFirst()) {
            do {
                String type = monthlyTransactions.getString(monthlyTransactions.getColumnIndex(DatabaseHelper.COLUMN_TYPE));
                double total = monthlyTransactions.getDouble(monthlyTransactions.getColumnIndex("total"));

                if (type.equals("Dépense")) {
                    totalDepenses += total;
                } else if (type.equals("Revenu")) {
                    totalRevenus += total;
                }
            } while (monthlyTransactions.moveToNext());
        }

        // Créer les entrées pour le graphique en camembert
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(totalDepenses, "Dépenses"));
        entries.add(new PieEntry(totalRevenus, "Revenus"));

        // Configurer le dataset pour le graphique
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(new int[]{Color.parseColor("#FF5733"), Color.parseColor("#33FF57")});
        dataSet.setValueTextSize(12f);

        // Créer les données du graphique
        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false); // Désactiver la description
        pieChart.setCenterTextSize(12f); // Taille du texte au centre
        pieChart.animateY(1000); // Animation du graphique
    }
}