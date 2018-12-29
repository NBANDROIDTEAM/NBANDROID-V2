package ${escapeKotlinIdentifiers(packageName)}

import android.app.Service
import android.content.Intent
import android.os.IBinder

class ${className} : Service() {

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }
}
