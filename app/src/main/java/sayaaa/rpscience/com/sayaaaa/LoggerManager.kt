package sayaaa.rpscience.com.sayaaaa

import android.os.Environment
import android.util.Log
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object LoggerManager {
    private const val VIDEO_FILENAME = "VideoData.vid"
    private const val AUDIO_FILENAME = "AudioData.wav"
    private const val INFO_FILENAME = "session_info.log"
    private val appDir = File(Environment.getExternalStorageDirectory().absolutePath + "/SayAAAA")
    private var sessionDir: File? = null

    private var videoFile: File  = File("")


    private var infoFile = File("")

    private val parsingDataActor = actor<VideoRecord>(capacity = Channel.UNLIMITED) {
        for (record in channel) {
            videoFile.appendBytes(record.data)
            infoFile.appendText("t${record.timestamp}s${record.data.size}w${record.width}h${record.height}\n")
        }
    }


    fun writeVideoRecord(video: ByteArray, width: Int, height: Int) {
        launch {
            parsingDataActor.send(VideoRecord(System.currentTimeMillis(), width, height, video))
        }
    }

    fun getAudioRecordFile(): File {
        return File(sessionDir, AUDIO_FILENAME)
    }

    fun createSession() {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        val time = dateFormat.format(Date())
        sessionDir = File(appDir.absolutePath + "/Session-$time").also { it.mkdirs() }
        Log.v("fafafa", "Session created ${sessionDir?.absolutePath}")
        videoFile = File(sessionDir, VIDEO_FILENAME)
        infoFile = File(sessionDir, INFO_FILENAME)

    }

    private class VideoRecord(val timestamp: Long, val width: Int, val height: Int, val data: ByteArray)
}

typealias logger = LoggerManager