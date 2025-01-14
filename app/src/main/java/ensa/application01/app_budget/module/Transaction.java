package ensa.application01.app_budget.module;

/**
 * La classe Transaction représente une transaction financière.
 * Elle contient des informations telles que le type, le montant, la catégorie, la date, une note,
 * ainsi que des détails sur l'alarme associée (si activée).
 */
public class Transaction {
    private String type; // Type de la transaction (exemple : "Revenu" ou "Dépense")
    private double amount; // Montant de la transaction
    private String category; // Catégorie de la transaction (exemple : "Nourriture", "Transport")
    private String date; // Date de la transaction au format "dd/MM/yyyy"
    private String note; // Note ou description facultative de la transaction
    private boolean isAlarmEnabled; // Indique si une alarme est activée pour cette transaction
    private String alarmDate; // Date de l'alarme au format "dd/MM/yyyy"
    private String alarmTime; // Heure de l'alarme au format "HH:mm"

    /**
     * Constructeur de la classe Transaction.
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
    public Transaction(String type, double amount, String category, String date, String note, boolean isAlarmEnabled, String alarmDate, String alarmTime) {
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.note = note;
        this.isAlarmEnabled = isAlarmEnabled;
        this.alarmDate = alarmDate;
        this.alarmTime = alarmTime;
    }

    /**
     * Retourne le type de la transaction.
     *
     * @return Le type de la transaction (exemple : "Revenu" ou "Dépense").
     */
    public String getType() {
        return type;
    }

    /**
     * Retourne le montant de la transaction.
     *
     * @return Le montant de la transaction.
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Retourne la catégorie de la transaction.
     *
     * @return La catégorie de la transaction (exemple : "Nourriture", "Transport").
     */
    public String getCategory() {
        return category;
    }

    /**
     * Retourne la date de la transaction.
     *
     * @return La date de la transaction au format "dd/MM/yyyy".
     */
    public String getDate() {
        return date;
    }

    /**
     * Retourne la note ou description de la transaction.
     *
     * @return La note ou description facultative de la transaction.
     */
    public String getNote() {
        return note;
    }

    /**
     * Indique si une alarme est activée pour cette transaction.
     *
     * @return `true` si une alarme est activée, sinon `false`.
     */
    public boolean isAlarmEnabled() {
        return isAlarmEnabled;
    }

    /**
     * Retourne la date de l'alarme.
     *
     * @return La date de l'alarme au format "dd/MM/yyyy".
     */
    public String getAlarmDate() {
        return alarmDate;
    }

    /**
     * Retourne l'heure de l'alarme.
     *
     * @return L'heure de l'alarme au format "HH:mm".
     */
    public String getAlarmTime() {
        return alarmTime;
    }
}