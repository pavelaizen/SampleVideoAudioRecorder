package sayaaa.rpscience.com.sayaaaa

import android.content.Intent
import android.os.Bundle

class PermissionActivity : BaseActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.permissions)
        requestPermissionsLive(android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA).observe(this, android.arch.lifecycle.Observer { permissions->
            permissions?.isAllGranted()?.let {allGranted->
                if (allGranted){
                    startActivity(Intent(this@PermissionActivity, StartActivity::class.java))
                    finish()
                }

            }
        })
    }
}
