package ensa.application01.app_budget.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ensa.application01.app_budget.R;
import ensa.application01.app_budget.module.Transaction;

/**
 * TransactionAdapter est un adapteur pour afficher une liste de transactions dans un RecyclerView.
 * Il lie les données des transactions aux vues correspondantes dans l'interface utilisateur.
 */
public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactionList; // Liste des transactions à afficher

    /**
     * Constructeur de TransactionAdapter.
     *
     * @param transactionList La liste des transactions à afficher.
     */
    public TransactionAdapter(List<Transaction> transactionList) {
        this.transactionList = transactionList;
    }

    /**
     * Crée une nouvelle instance de TransactionViewHolder.
     *
     * @param parent   Le ViewGroup dans lequel la nouvelle vue sera ajoutée.
     * @param viewType Le type de vue (non utilisé ici).
     * @return Une nouvelle instance de TransactionViewHolder.
     */
    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    /**
     * Lie les données d'une transaction à une vue (ViewHolder).
     *
     * @param holder   Le ViewHolder à mettre à jour.
     * @param position La position de l'élément dans la liste.
     */
    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);

        // Afficher les détails de la transaction dans les TextView correspondants
        holder.textViewType.setText("Type: " + transaction.getType());
        holder.textViewAmount.setText("Montant: " + String.format("%.2f", transaction.getAmount()));
        holder.textViewCategory.setText("Catégorie: " + transaction.getCategory());
        holder.textViewDate.setText("Date: " + transaction.getDate());
        holder.textViewNote.setText("Note: " + transaction.getNote());

        // Afficher l'état de l'alarme
        if (transaction.isAlarmEnabled()) {
            holder.textViewAlarm.setText("Alarme: Activée à " + transaction.getAlarmDate() + " " + transaction.getAlarmTime());
        } else {
            holder.textViewAlarm.setText("Alarme: Désactivée");
        }
    }

    /**
     * Retourne le nombre total d'éléments dans la liste.
     *
     * @return Le nombre de transactions dans la liste.
     */
    @Override
    public int getItemCount() {
        return transactionList != null ? transactionList.size() : 0;
    }

    /**
     * Met à jour la liste des transactions et notifie l'adapteur du changement.
     *
     * @param newTransactionList La nouvelle liste de transactions.
     */
    public void updateData(List<Transaction> newTransactionList) {
        transactionList.clear();
        transactionList.addAll(newTransactionList);
        notifyDataSetChanged();
    }

    /**
     * TransactionViewHolder est une classe interne qui représente une vue pour un élément de transaction.
     */
    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView textViewType; // Affiche le type de transaction
        TextView textViewAmount; // Affiche le montant de la transaction
        TextView textViewCategory; // Affiche la catégorie de la transaction
        TextView textViewDate; // Affiche la date de la transaction
        TextView textViewNote; // Affiche la note de la transaction
        TextView textViewAlarm; // Affiche l'état de l'alarme

        /**
         * Constructeur de TransactionViewHolder.
         *
         * @param itemView La vue associée au ViewHolder.
         */
        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewType = itemView.findViewById(R.id.textView_type);
            textViewAmount = itemView.findViewById(R.id.textView_amount);
            textViewCategory = itemView.findViewById(R.id.textView_category);
            textViewDate = itemView.findViewById(R.id.textView_date);
            textViewNote = itemView.findViewById(R.id.textView_note);
            textViewAlarm = itemView.findViewById(R.id.textView_alarm);
        }
    }
}