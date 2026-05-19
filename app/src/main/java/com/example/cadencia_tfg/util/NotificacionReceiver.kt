package com.example.cadencia_tfg.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationManager
import android.app.PendingIntent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import com.example.cadencia_tfg.R

class NotificacionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val intentAbrirApp = Intent(context, com.example.cadencia_tfg.ui.main.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intentAbrirApp,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, "canal_habitos")
            .setSmallIcon(R.drawable.outline_add_alert_24)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.logo))
            .setContentTitle("¡Mantén tu Cadencia!")
            .setContentText("No olvides revisar tus hábitos pendientes de hoy.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify(1001, builder.build())
            }
        } else {
            notificationManager.notify(1001, builder.build())
        }
    }
}