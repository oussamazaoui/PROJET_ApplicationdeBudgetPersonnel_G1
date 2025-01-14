package ensa.application01.app_budget.DataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "budget.db";
    private static final int DATABASE_VERSION = 2;
    private static final String TAG = "DatabaseHelper";

    // Noms des tables et colonnes
    public static final String TABLE_TRANSACTIONS = "transactions";
    public static final String TABLE_CATEGORIES = "categories";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_NOTE = "note";
    public static final String COLUMN_IS_ALARM_ENABLED = "isAlarmEnabled";
    public static final String COLUMN_ALARM_DATE = "alarmDate";
    public static final String COLUMN_ALARM_TIME = "alarmTime";
    public static final String COLUMN_CATEGORY_NAME = "name";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            Log.d(TAG, "Création de la base de données");

            // Créer la table transactions
            String createTableQuery = "CREATE TABLE " + TABLE_TRANSACTIONS + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_TYPE + " TEXT,"
                    + COLUMN_AMOUNT + " REAL,"
                    + COLUMN_CATEGORY + " TEXT,"
                    + COLUMN_DATE + " TEXT,"
                    + COLUMN_NOTE + " TEXT,"
                    + COLUMN_IS_ALARM_ENABLED + " INTEGER,"
                    + COLUMN_ALARM_DATE + " TEXT,"
                    + COLUMN_ALARM_TIME + " TEXT,"
                    + "UNIQUE(" + COLUMN_TYPE + ", " + COLUMN_AMOUNT + ", " + COLUMN_CATEGORY + ", " + COLUMN_DATE + ", " + COLUMN_NOTE + ")"
                    + ")";
            db.execSQL(createTableQuery);
            Log.d(TAG, "Table transactions créée");

            // Créer la table categories
            String createCategoriesTable = "CREATE TABLE " + TABLE_CATEGORIES + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_CATEGORY_NAME + " TEXT UNIQUE"
                    + ")";
            db.execSQL(createCategoriesTable);
            Log.d(TAG, "Table categories créée");

            // Insérer des catégories par défaut
            db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (" + COLUMN_CATEGORY_NAME + ") VALUES ('Salaire')");
            db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (" + COLUMN_CATEGORY_NAME + ") VALUES ('Nourriture')");
            db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (" + COLUMN_CATEGORY_NAME + ") VALUES ('Transport')");
            db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (" + COLUMN_CATEGORY_NAME + ") VALUES ('Loisirs')");
            Log.d(TAG, "Catégories par défaut insérées");
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la création de la base de données", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            Log.d(TAG, "Mise à jour de la base de données de la version " + oldVersion + " à " + newVersion);

            // Supprimer les tables existantes et les recréer
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
            Log.d(TAG, "Anciennes tables supprimées");

            onCreate(db);
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la mise à jour de la base de données", e);
        }
    }

    // Récupérer toutes les transactions
    public Cursor getAllTransactions() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(
                    TABLE_TRANSACTIONS,
                    null,
                    null,
                    null,
                    null,
                    null,
                    COLUMN_ID + " DESC"
            );
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la récupération des transactions", e);
        }
        return cursor;
    }

    // Ajouter une catégorie
    public long addCategory(String name) {
        SQLiteDatabase db = getWritableDatabase();
        long result = -1;
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_CATEGORY_NAME, name);

            result = db.insert(TABLE_CATEGORIES, null, values);

            if (result == -1) {
                Log.e(TAG, "Erreur lors de l'ajout de la catégorie : " + name);
            } else {
                Log.d(TAG, "Catégorie ajoutée avec succès : " + name);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'ajout de la catégorie", e);
        }
        return result;
    }

    // Supprimer une catégorie par son nom
    public int deleteCategoryByName(String name) {
        SQLiteDatabase db = getWritableDatabase();
        int result = 0;
        try {
            result = db.delete(TABLE_CATEGORIES, COLUMN_CATEGORY_NAME + " = ?", new String[]{name});
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la suppression de la catégorie", e);
        }
        return result;
    }

    // Récupérer toutes les catégories
    public Cursor getAllCategories() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_CATEGORIES, null, null, null, null, null, null);
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la récupération des catégories", e);
        }
        return cursor;
    }

    // Récupérer toutes les catégories sous forme de liste
    public List<String> getAllCategoriesAsList() {
        List<String> categories = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = getAllCategories();
            if (cursor.moveToFirst()) {
                do {
                    categories.add(cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY_NAME)));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la récupération des catégories sous forme de liste", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return categories;
    }

    // Récupérer les transactions par mois et année
    public Cursor getMonthlyTransactionsByCategory(String month, String year) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            String query = "SELECT " + COLUMN_CATEGORY + ", " + COLUMN_TYPE + ", SUM(" + COLUMN_AMOUNT + ") AS total, COUNT(*) AS count " +
                    "FROM " + TABLE_TRANSACTIONS + " " +
                    "WHERE substr(" + COLUMN_DATE + ", 4, 2) = ? AND substr(" + COLUMN_DATE + ", 7, 4) = ? " +
                    "GROUP BY " + COLUMN_CATEGORY + ", " + COLUMN_TYPE;
            cursor = db.rawQuery(query, new String[]{month, year});
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la récupération des transactions mensuelles par catégorie", e);
        }
        return cursor;
    }
}