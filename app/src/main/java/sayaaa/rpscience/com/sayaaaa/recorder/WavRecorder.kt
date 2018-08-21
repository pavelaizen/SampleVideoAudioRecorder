package sayaaa.rpscience.com.sayaaaa.recorder

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import java.io.*


class WavRecorder(private val fileName: String) {
    companion object {
        private const val RECORDER_BPP = 16
        private const val TAG = "WavRecorder"
        const val AUDIO_RECORDER_FOLDER = "AudioRecorder"
        private const val AUDIO_RECORDER_TEMP_FILE = "record_temp.raw"
        private const val RECORDER_SAMPLERATE = 44100
        private const val RECORDER_CHANNELS: Int = AudioFormat.CHANNEL_IN_MONO
        private const val RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT
    }

    private var recorder: AudioRecord? = null
    private var job: Job? = null

    private val buffer: ByteArray by lazy {
        val bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
                RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING) * 3
        ByteArray(bufferSize)
    }
    private lateinit var tempFile: File

    fun startRecording(context: Context) {
        recorder = AudioRecord(MediaRecorder.AudioSource.MIC, RECORDER_SAMPLERATE, RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING, buffer.size)
                .also { recorder ->
                    recorder.recordingState.takeIf { it == AudioRecord.RECORDSTATE_STOPPED }?.let {
                        recorder.startRecording()
                        processRecording(context)
                        Log.v(TAG, "recording started")
                    }
                }
    }

    fun createTempFile(context: Context) {
        val tempDir = context.filesDir
        tempFile = File(tempDir, AUDIO_RECORDER_TEMP_FILE)
        if (tempFile.exists()) {
            tempFile.delete()
        }
        tempFile.createNewFile()
    }

    private fun processRecording(context: Context) {
        job = launch(CommonPool) {
            createTempFile(context)
            val os = FileOutputStream(tempFile)
            while (isActive) {
                recorder?.read(buffer, 0, buffer.size)?.takeIf { it > 0 && AudioRecord.ERROR_INVALID_OPERATION != it }?.let { read ->
                    try {
                        os.write(buffer)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            try {
                os.flush()
                os.close()
                Log.v(TAG, "recording stopped OK ${tempFile.length()}")
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }
        }

    }

    fun stopRecording() {
        job?.invokeOnCompletion {
            recorder?.apply {
                if (this.state == AudioRecord.RECORDSTATE_STOPPED) {
                    Log.v(TAG, "recorder object stop")
                    stop()
                }
                release()
                Log.v(TAG, "recorder released")
                launch {
                    copyWavFile()
                    Log.v(TAG, "wav file copy OK")
                }
            }
            recorder = null
        }
        job?.cancel()
        job = null

    }

    private fun copyWavFile() {
        File(fileName).apply {
            delete()
            createNewFile()
        }

        var out: FileOutputStream? = null
        var inputStream: FileInputStream? = null
        var totalAudioLen: Long = 0
        var totalDataLen = totalAudioLen + 36
        val longSampleRate = RECORDER_SAMPLERATE
        val channels = if (RECORDER_CHANNELS == AudioFormat.CHANNEL_IN_MONO) 1 else 2
        val byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels / 8

        val data = ByteArray(buffer.size)

        try {
            inputStream = FileInputStream(tempFile)
            out = FileOutputStream(fileName)
            totalAudioLen = inputStream.channel.size()
            totalDataLen = totalAudioLen + 36

            writeWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate.toLong(), channels, byteRate.toLong())

            while (inputStream.read(data) != -1) {
                out.write(data)
            }

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
            out?.close()
        }

    }

    @Throws(IOException::class)
    private fun writeWaveFileHeader(out: FileOutputStream, totalAudioLen: Long,
                                    totalDataLen: Long, longSampleRate: Long, channels: Int, byteRate: Long) {
        val header = ByteArray(44)

        header[0] = 'R'.toByte() // RIFF/WAVE header
        header[1] = 'I'.toByte()
        header[2] = 'F'.toByte()
        header[3] = 'F'.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = (totalDataLen shr 8 and 0xff).toByte()
        header[6] = (totalDataLen shr 16 and 0xff).toByte()
        header[7] = (totalDataLen shr 24 and 0xff).toByte()
        header[8] = 'W'.toByte()
        header[9] = 'A'.toByte()
        header[10] = 'V'.toByte()
        header[11] = 'E'.toByte()
        header[12] = 'f'.toByte() // 'fmt ' chunk
        header[13] = 'm'.toByte()
        header[14] = 't'.toByte()
        header[15] = ' '.toByte()
        header[16] = 16 // 4 bytes: size of 'fmt ' chunk
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // format = 1
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (longSampleRate and 0xff).toByte()
        header[25] = (longSampleRate shr 8 and 0xff).toByte()
        header[26] = (longSampleRate shr 16 and 0xff).toByte()
        header[27] = (longSampleRate shr 24 and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = (byteRate shr 8 and 0xff).toByte()
        header[30] = (byteRate shr 16 and 0xff).toByte()
        header[31] = (byteRate shr 24 and 0xff).toByte()
        header[32] = ((if (RECORDER_CHANNELS == AudioFormat.CHANNEL_IN_MONO)
            1
        else
            2) * 16 / 8).toByte() // block align
        header[33] = 0
        header[34] = RECORDER_BPP.toByte() // bits per sample
        header[35] = 0
        header[36] = 'd'.toByte()
        header[37] = 'a'.toByte()
        header[38] = 't'.toByte()
        header[39] = 'a'.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = (totalAudioLen shr 8 and 0xff).toByte()
        header[42] = (totalAudioLen shr 16 and 0xff).toByte()
        header[43] = (totalAudioLen shr 24 and 0xff).toByte()

        out.write(header, 0, 44)
    }


}