@file:Suppress("DEPRECATION")

package sayaaa.rpscience.com.sayaaaa.recorder

import android.app.Activity
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.graphics.ImageFormat
import android.hardware.Camera
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import sayaaa.rpscience.com.sayaaaa.ImageProcessing
import sayaaa.rpscience.com.sayaaaa.logger
import sayaaa.rpscience.com.sayaaaa.toHexString

class CameraRecorderModel : ViewModel() {
    private lateinit var previewHolder: SurfaceHolder
    private var camera: Camera? = null
    private var displayOrientation: Int = 0
    val heartbeatData = MutableLiveData<Pair<ImageProcessing.TYPE, Int>>()
    private val imageProcessing = ImageProcessing { type: ImageProcessing.TYPE, value: Int ->
        heartbeatData.postValue(type to value)
    }
    private val previewCallback = Camera.PreviewCallback { data, camera ->
        Log.v("fafafa", "" + data.size + " " + data.toHexString())
        val size = camera.parameters.previewSize
        camera.parameters.previewFormat
        val width = size.width
        val height = size.height
        logger.writeVideoRecord(data, width, height)
        logger.saveJpeg(data, width, height)
        launch(CommonPool) {
            imageProcessing.processFrame(data, camera)
        }
    }

    private val surfaceCallback = object : SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder?) {
            camera?.apply {
                setPreviewDisplay(previewHolder)
                setPreviewCallback(previewCallback)
            }
        }

        override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
            camera?.parameters?.let { parameters ->
                parameters.flashMode = Camera.Parameters.FLASH_MODE_TORCH
                val size = getSmallestPreviewSize(width, height, parameters)
                if (size != null) {
                    parameters.setPreviewSize(size.width, size.height)
                    Log.d("fafafa", "Using width=" + size.width + " height=" + size.height)
                }
                parameters.previewFormat = ImageFormat.NV21
                camera?.parameters = parameters
            }
            camera?.setDisplayOrientation(displayOrientation)
            camera?.startPreview()
        }

        override fun surfaceDestroyed(holder: SurfaceHolder?) {

        }


    }

    fun start(surfaceView: SurfaceView, activity: Activity) {
        displayOrientation = getCameraDisplayOrientation(activity, 0)
        previewHolder = surfaceView.holder
        previewHolder.addCallback(surfaceCallback)
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        camera = Camera.open()
    }

    fun stop() {
        camera?.let {
            it.setPreviewCallback(null)
            it.stopPreview()
            it.release()
        }
        camera = null
    }

    private companion object {
        private fun getSmallestPreviewSize(width: Int, height: Int, parameters: Camera.Parameters): Camera.Size? {
            var result: Camera.Size? = null

            for (size in parameters.supportedPreviewSizes) {
                if (size.width <= width && size.height <= height) {
                    if (result == null) {
                        result = size
                    } else {
                        val resultArea = result.width * result.height
                        val newArea = size.width * size.height

                        if (newArea < resultArea) result = size
                    }
                }
            }

            return result
        }

        fun getCameraDisplayOrientation(activity: Activity, cameraId: Int): Int {
            val info = android.hardware.Camera.CameraInfo()
            android.hardware.Camera.getCameraInfo(cameraId, info)
            val rotation = activity.windowManager.defaultDisplay.rotation
            var degrees = 0
            when (rotation) {
                Surface.ROTATION_0 -> degrees = 0
                Surface.ROTATION_90 -> degrees = 90
                Surface.ROTATION_180 -> degrees = 180
                Surface.ROTATION_270 -> degrees = 270
            }

            var result: Int
            //int currentapiVersion = android.os.Build.VERSION.SDK_INT;
            // do something for phones running an SDK before lollipop
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = (info.orientation + degrees) % 360
                result = (360 - result) % 360 // compensate the mirror
            } else { // back-facing
                result = (info.orientation - degrees + 360) % 360
            }
            return result
        }
    }

}