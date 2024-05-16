package android.boot.common.pkg

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader

suspend fun sysInstall(apkFilePath: String): Result<String> {
    return runBlocking(Dispatchers.IO) {
        var outputStream: DataOutputStream? = null
        var reader: BufferedReader? = null
        var result: Result<String>? = null
        try {
            val cmd = "pm install -r $apkFilePath \n"
            val runtime = Runtime.getRuntime().exec(cmd)
            outputStream = DataOutputStream(runtime.outputStream)
            outputStream.run {
                write(cmd.toByteArray(Charsets.UTF_8))
                flush()
                writeBytes("exit\n")
                flush()
                runtime.waitFor()
            }

            reader = BufferedReader(InputStreamReader(runtime.errorStream))
            val cmdRet = reader.readText()
            Log.i("sysInstall", cmdRet)

            if (cmdRet.contains("failure", true)) {
                result = Result.failure(Throwable(cmdRet))
            } else {
                result = Result.success("Install successful")
            }
        } catch (e: Exception) {
            result = Result.failure(e)
        } finally {
            outputStream?.close()
            reader?.close()
        }
        result ?: Result.failure(Throwable("Unknown sysInstall error"))
    }
}