package sayaaa.rpscience.com.sayaaaa

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.Menu
import android.view.MenuItem
import android.view.SurfaceView
import android.view.View
import android.widget.TextView
import sayaaa.rpscience.com.sayaaaa.recorder.CameraRecorderModel
import sayaaa.rpscience.com.sayaaaa.views.HeartBeatView

class MainActivity : BaseActivity() {
    private lateinit var cameraRecorderModel: CameraRecorderModel
    private lateinit var surfaceView: SurfaceView
    lateinit var textView : TextView
    lateinit var heartBeatView: HeartBeatView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logger.createSession()
        setContentView(R.layout.activity_main)
        cameraRecorderModel = ViewModelProviders.of(this).get(CameraRecorderModel::class.java)
        surfaceView = findViewById<SurfaceView>(R.id.surfaceView)
        startCamera()
        ContextCompat.startForegroundService(this, Intent(this, RecordingService::class.java).setAction(RecordingService.START_RECORDING))
        findViewById<View>(R.id.stop).setOnClickListener {
            finish()
        }
        textView = findViewById(R.id.textView)
        heartBeatView = findViewById(R.id.heartBeatView)
        cameraRecorderModel.heartbeatData.observe(this, Observer { pair: Pair<ImageProcessing.TYPE, Int>? ->
            pair?.let {
                textView.setTextColor(pair.first.color)
                textView.text = pair.second.toString()
                heartBeatView.pumpAnimation(pair.second)
            }
            getString(R.string.app_name)

        })
    }

    override fun onPause() {
        super.onPause()
        stopCamera()
        ContextCompat.startForegroundService(this, Intent(this, RecordingService::class.java).setAction(RecordingService.STOP_RECORDING))
        finish()
    }

    fun startCamera(){
        cameraRecorderModel.start(surfaceView, this)
    }

    fun stopCamera(){
        cameraRecorderModel.stop()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }



    companion object {

        // Used to load the 'native-lib' library on application startup.
//        init {
//            System.loadLibrary("native-lib")
//        }
    }
}
