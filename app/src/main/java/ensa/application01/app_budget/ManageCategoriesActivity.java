package ensa.application01.app_budget;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ensa.application01.app_budget.DataBase.DatabaseHelper;
import ensa.application01.app_budget.adapter.CategoryAdapter;

/**
 * ManageCategoriesActivity est une activité permettant de gérer les catégories de transactions.
 * Elle permet d'ajouter de nouvelles catégories et d'afficher la liste des catégories existantes.
 */
public class ManageCategoriesActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper; // Helper pour interagir avec la base de données
    private EditText etCategoryName; // Champ de saisie pour le nom de la catégorie
    private RecyclerView recyclerViewCategories; // RecyclerView pour afficher les catégories
    private CategoryAdapter adapter; // Adapteur pour le RecyclerView
    private ArrayList<String> categories; // Liste des catégories

    /**
     * Méthode appelée lors de la création de l'activité.
     * Initialise les vues, configure le RecyclerView et charge les catégories existantes.
     *
     * @param savedInstanceState L'état enregistré de l'activité.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_categories);

        // Initialisation du DatabaseHelper
        dbHelper = new DatabaseHelper(this);

        // Liaison des vues aux IDs
        etCategoryName = findViewById(R.id.et_category_name);
        recyclerViewCategories = findViewById(R.id.recyclerView_categories);
        Button btnAddCategory = findViewById(R.id.btn_add_category);

        // Initialisation de la liste des catégories
        categories = new ArrayList<>();

        // Configuration du RecyclerView
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CategoryAdapter(this, categories, dbHelper, this::loadCategories);
        recyclerViewCategories.setAdapter(adapter);

        // Charger les catégories existantes
        loadCategories();

        // Gestion du clic sur le bouton "Ajouter une catégorie"
        btnAddCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String categoryName = etCategoryName.getText().toString().trim();
                if (!categoryName.isEmpty()) {
                    // Ajouter la catégorie à la base de données
                    long result = dbHelper.addCategory(categoryName);
                    if (result != -1) {
                        Toast.makeText(ManageCategoriesActivity.this, "Catégorie ajoutée", Toast.LENGTH_SHORT).show();
                        loadCategories(); // Recharger la liste des catégories
                        etCategoryName.setText(""); // Vider le champ de saisie
                    } else {
                        Toast.makeText(ManageCategoriesActivity.this, "Erreur : Catégorie déjà existante", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ManageCategoriesActivity.this, "Veuillez entrer un nom de catégorie", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Charge les catégories depuis la base de données et met à jour l'adaptateur du RecyclerView.
     */
    private void loadCategories() {
        categories.clear(); // Vider la liste actuelle
        categories.addAll(dbHelper.getAllCategoriesAsList()); // Ajouter les catégories depuis la base de données
        adapter.notifyDataSetChanged(); // Notifier l'adaptateur du changement
    }
}