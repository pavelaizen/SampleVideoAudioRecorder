package sayaaa.rpscience.com.sayaaaa

import android.annotation.TargetApi
import android.app.Activity
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.support.v4.app.NotificationManagerCompat


class MainApp : Application() {
    override fun onCreate() {
        super.onCreate()
        prepareChannel(this, RecordingService.CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_LOW)
    }

    @TargetApi(26)
    private fun prepareChannel(context: Context, id: String, importance: Int) {
        val appName = context.getString(R.string.app_name)
        val description = context.getString(R.string.notifications_channel_description)
        val nm = context.getSystemService(Activity.NOTIFICATION_SERVICE) as? NotificationManager

        var nChannel: NotificationChannel? = nm?.getNotificationChannel(id)

        if (nChannel == null) {
            nChannel = NotificationChannel(id, appName, importance)
            nChannel.description = description
            nm?.createNotificationChannel(nChannel)
        }
    }
}