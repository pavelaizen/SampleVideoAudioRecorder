package sayaaa.rpscience.com.sayaaaa

import android.content.Intent
import android.os.Bundle
import android.view.View

class StartActivity : BaseActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.start)
        findViewById<View>(R.id.start).setOnClickListener {
            startActivity(Intent(this@StartActivity, MainActivity::class.java))
        }
    }
}
