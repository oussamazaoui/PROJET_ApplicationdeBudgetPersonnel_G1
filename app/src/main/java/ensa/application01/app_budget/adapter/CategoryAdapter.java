package ensa.application01.app_budget.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ensa.application01.app_budget.DataBase.DatabaseHelper;
import ensa.application01.app_budget.R;

/**
 * CategoryAdapter est un adapteur pour afficher une liste de catégories dans un RecyclerView.
 * Il permet également de supprimer des catégories via un bouton de suppression.
 */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private Context context; // Contexte de l'application
    private List<String> categories; // Liste des catégories à afficher
    private DatabaseHelper dbHelper; // Helper pour interagir avec la base de données
    private Runnable onCategoryDeleted; // Callback exécuté après la suppression d'une catégorie

    /**
     * Constructeur de CategoryAdapter.
     *
     * @param context          Le contexte de l'application.
     * @param categories       La liste des catégories à afficher.
     * @param dbHelper         L'instance de DatabaseHelper pour interagir avec la base de données.
     * @param onCategoryDeleted Callback exécuté après la suppression d'une catégorie.
     */
    public CategoryAdapter(Context context, List<String> categories, DatabaseHelper dbHelper, Runnable onCategoryDeleted) {
        this.context = context;
        this.categories = categories;
        this.dbHelper = dbHelper;
        this.onCategoryDeleted = onCategoryDeleted;
    }

    /**
     * Crée une nouvelle instance de CategoryViewHolder.
     *
     * @param parent   Le ViewGroup dans lequel la nouvelle vue sera ajoutée.
     * @param viewType Le type de vue (non utilisé ici).
     * @return Une nouvelle instance de CategoryViewHolder.
     */
    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    /**
     * Lie les données d'une catégorie à une vue (ViewHolder).
     *
     * @param holder   Le ViewHolder à mettre à jour.
     * @param position La position de l'élément dans la liste.
     */
    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        String categoryName = categories.get(position);
        holder.textViewCategoryName.setText(categoryName);

        // Gestion du clic sur le bouton de suppression
        holder.btnDeleteCategory.setOnClickListener(v -> {
            // Afficher une boîte de dialogue de confirmation
            new AlertDialog.Builder(context)
                    .setTitle("Supprimer la catégorie")
                    .setMessage("Êtes-vous sûr de vouloir supprimer la catégorie \"" + categoryName + "\" ?")
                    .setPositiveButton("Oui", (dialog, which) -> {
                        // Supprimer la catégorie de la base de données
                        dbHelper.deleteCategoryByName(categoryName);

                        // Supprimer la catégorie de la liste
                        categories.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, categories.size());

                        // Recharger les catégories
                        onCategoryDeleted.run();

                        Toast.makeText(context, "Catégorie supprimée : " + categoryName, Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Non", null)
                    .show();
        });
    }

    /**
     * Retourne le nombre total d'éléments dans la liste.
     *
     * @return Le nombre de catégories dans la liste.
     */
    @Override
    public int getItemCount() {
        return categories.size();
    }

    /**
     * CategoryViewHolder est une classe interne qui représente une vue pour un élément de catégorie.
     */
    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView textViewCategoryName; // Affiche le nom de la catégorie
        Button btnDeleteCategory; // Bouton pour supprimer la catégorie

        /**
         * Constructeur de CategoryViewHolder.
         *
         * @param itemView La vue associée au ViewHolder.
         */
        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewCategoryName = itemView.findViewById(R.id.textView_category_name);
            btnDeleteCategory = itemView.findViewById(R.id.btn_delete_category);
        }
    }
}