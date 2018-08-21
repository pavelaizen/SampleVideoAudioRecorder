package sayaaa.rpscience.com.sayaaaa

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.sync.Mutex
import kotlinx.coroutines.experimental.sync.withLock
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.suspendCoroutine

abstract class BaseActivity : AppCompatActivity() {
    private var permissionContinuation: Continuation<MutableList<Pair<String, Int>>>? = null
    private val lock = Mutex()

    suspend fun requestPermissions(vararg permissions: String): MutableList<Pair<String, Int>> = applicationContext.let { context ->
        val listOfNotGrantedPermissions = mutableListOf<String>()
        val listOfGrantedPermissions = mutableListOf<String>()
        for (permission in permissions) {
            listOfNotGrantedPermissions.takeIf { ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED }
                    ?.add(permission) ?: listOfGrantedPermissions.add(permission)
        }
        val listOfRequestedPermissions = if (listOfNotGrantedPermissions.size > 0) {
            lock.withLock {
                suspendCoroutine<MutableList<Pair<String, Int>>> {
                    permissionContinuation = it
                    ActivityCompat.requestPermissions(this, listOfNotGrantedPermissions.toTypedArray(), PERMISSION_REQUEST)
                }.also { permissionContinuation = null }
            }
        } else mutableListOf()

        listOfGrantedPermissions.mapTo(listOfRequestedPermissions) { Pair(it, PackageManager.PERMISSION_GRANTED) }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST) {
            val requestedPermissions = permissions.indices.mapTo(mutableListOf()) { Pair(permissions[it], grantResults[it]) }
            permissionContinuation?.resume(requestedPermissions)
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    companion object {
        private const val PERMISSION_REQUEST = 12345
    }


}

suspend fun Context.requestPermissions(vararg permissions: String): MutableList<Pair<String, Int>> =
        (this as BaseActivity).requestPermissions(*permissions)

fun Context.requestPermissionsLive(vararg permissions: String): LiveData<List<Pair<String, Int>>> = MutableLiveData<List<Pair<String, Int>>>().also {
    launch(UI) {
        it.postValue(requestPermissions(*permissions))
    }
}


fun List<Pair<String, Int>>.isAllGranted(): Boolean = this.all { it.second == PackageManager.PERMISSION_GRANTED }