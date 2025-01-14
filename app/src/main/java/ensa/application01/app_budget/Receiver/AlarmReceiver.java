package ensa.application01.app_budget.Receiver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;

import ensa.application01.app_budget.R;

/**
 * AlarmReceiver est un BroadcastReceiver qui gère les alarmes et affiche des notifications
 * lorsque l'alarme est déclenchée. Il est utilisé pour rappeler à l'utilisateur des transactions
 * planifiées ou d'autres événements liés à l'application.
 */
public class AlarmReceiver extends BroadcastReceiver {

    /**
     * Méthode appelée lorsque l'alarme est déclenchée.
     * Cette méthode crée et affiche une notification à l'utilisateur.
     *
     * @param context Le contexte de l'application.
     * @param intent  L'intent contenant les informations de l'alarme, telles que le titre et le message.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        // Récupérer le titre et le message de l'alarme depuis l'intent
        String alarmTitle = intent.getStringExtra("alarmTitle");
        String alarmMessage = intent.getStringExtra("alarmMessage");

        // Créer un canal de notification (nécessaire pour Android 8.0 et supérieur)
        String channelId = "alarm_channel";
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Vérifier si la version d'Android est Oreo (8.0) ou supérieure
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Alarms", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        // Créer la notification
        Notification notification = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_alarm) // Icône de la notification
                .setContentTitle(alarmTitle) // Titre de la notification
                .setContentText(alarmMessage) // Message de la notification
                .setPriority(NotificationCompat.PRIORITY_DEFAULT) // Priorité de la notification
                .build();

        // Utiliser un ID de notification unique pour éviter les conflits
        int notificationId = (int) System.currentTimeMillis(); // ID unique basé sur l'horodatage
        notificationManager.notify(notificationId, notification); // Afficher la notification
    }
}