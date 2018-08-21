package sayaaa.rpscience.com.sayaaaa

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import sayaaa.rpscience.com.sayaaaa.recorder.WavRecorder

class RecordingService : Service() {
    private var recorder: WavRecorder? = null

    override fun onBind(bindIntent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            START_RECORDING -> {
                val notification = buildServiceNotification(applicationContext, false, "Listening...", CHANNEL_ID)
                startForeground(NOTIFICATION_ID, notification)
                startRecording()
            }
            STOP_RECORDING -> {
                stopRecording()
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        stopRecording()
        super.onDestroy()
    }

    private fun startRecording() {
        recorder = WavRecorder(logger.getAudioRecordFile().absolutePath).also { it.startRecording(applicationContext) }
    }

    private fun stopRecording() {
        recorder?.stopRecording()
        recorder = null
    }

    private fun buildServiceNotification(context: Context, isAudible: Boolean, message: CharSequence, channelId: String): Notification {
        val bigTextStyle = NotificationCompat.BigTextStyle()
        bigTextStyle.setBigContentTitle("Recording")
        bigTextStyle.bigText(message)
        val openPendingIntent = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), 0)
        val builder = NotificationCompat.Builder(context, channelId)
        builder.setContentIntent(openPendingIntent)
                .setOngoing(true)
                .setSmallIcon(R.drawable.alart_icon_doctor)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentTitle("Recording")
                .setContentText(message)
                .setStyle(bigTextStyle)
        if (isAudible) {
            builder.setDefaults(Notification.DEFAULT_LIGHTS)
            val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            if (defaultUri != null) {
                builder.setSound(defaultUri)
            }
        }
        return builder.build()
    }

    companion object {
        private const val NOTIFICATION_ID = 7562
        const val CHANNEL_ID = "sayaaa.rpscience.com.sayaaaa.CHANNEL_ID_FOREGROUND"
        const val START_RECORDING = "start_recording"
        const val STOP_RECORDING = "stop_recording"
    }
}