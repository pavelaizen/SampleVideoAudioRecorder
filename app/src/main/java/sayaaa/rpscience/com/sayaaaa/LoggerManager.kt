package sayaaa.rpscience.com.sayaaaa

import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.os.Environment
import android.util.Log
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.launch
import java.io.ByteArrayOutputStream
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
    private var jpgCounter = 0


    private var infoFile = File("")

    private val parsingDataActor = actor<VideoRecord>(capacity = Channel.UNLIMITED) {
        for (record in channel) {
            videoFile.appendBytes(record.data)
            infoFile.appendText("t${record.timestamp}s${record.data.size}w${record.width}h${record.height}\n")
        }
    }
    private val jpegActor = actor<JpegRecord>(capacity = Channel.UNLIMITED) {
        for (jpegRecord in channel) {
            val (video, width, height) = jpegRecord
            val jpeg = NV21toJPEG(video, width, height, 100)
            jpgCounter++
            val index = jpgCounter.toString().padStart(4, '0')
            val jpegFile = File(sessionDir, "image_$index.jpg")
            jpegFile.appendBytes(jpeg)

        }
    }


    fun writeVideoRecord(video: ByteArray, width: Int, height: Int) {
        launch {
            parsingDataActor.send(VideoRecord(System.currentTimeMillis(), width, height, video))
        }
    }

    fun saveJpeg(video: ByteArray, width: Int, height: Int){
        launch {
           jpegActor.send(JpegRecord(video, width, height))
        }
    }

    private fun NV21toJPEG(nv21: ByteArray, width: Int, height: Int, quality: Int): ByteArray {
        val out = ByteArrayOutputStream()
        val yuv = YuvImage(nv21, ImageFormat.NV21, width, height, null)
        yuv.compressToJpeg(Rect(0, 0, width, height), quality, out)
        return out.toByteArray()
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
    private data class JpegRecord(val video: ByteArray, val width: Int, val height: Int)
}

typealias logger = LoggerManager