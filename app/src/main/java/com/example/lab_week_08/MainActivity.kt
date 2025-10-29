package com.example.lab_week_08

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
// import androidx.work.Constraints // DIHAPUS
import androidx.work.Data
// import androidx.work.NetworkType // DIHAPUS
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo // DIPERBARUI: Ditambahkan untuk cek status
import androidx.work.WorkManager
import com.example.lab_week_08.service.NotificationService
import com.example.lab_week_08.worker.FirstWorker
import com.example.lab_week_08.worker.SecondWorker

class MainActivity : AppCompatActivity() {
    private val workManager = WorkManager.getInstance(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // KODE BARU: Meminta izin notifikasi untuk Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED) {

                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }

        // DIHAPUS: Batasan jaringan dihapus agar worker bisa langsung jalan
        // val networkConstraints = Constraints.Builder()
        //     .setRequiredNetworkType(NetworkType.CONNECTED)
        //     .build()

        val id = "001"
        val firstRequest = OneTimeWorkRequest
            .Builder(FirstWorker::class.java)
            // .setConstraints(networkConstraints) // DIHAPUS
            .setInputData(getIdInputData(FirstWorker
                .INPUT_DATA_ID, id)
            ).build()
        //This request is created for the SecondWorker class
        val secondRequest = OneTimeWorkRequest
            .Builder(SecondWorker::class.java)
            // .setConstraints(networkConstraints) // DIHAPUS
            .setInputData(getIdInputData(SecondWorker
                .INPUT_DATA_ID, id)
            ).build()

        //Sets up the process sequence from the work manager instance
        //Here it starts with FirstWorker, then SecondWorker
        workManager.beginWith(firstRequest)
            .then(secondRequest)
            .enqueue()

        workManager.getWorkInfoByIdLiveData(firstRequest.id)
            .observe(this) { info ->
                // DIPERBARUI: Menggunakan pengecekan SUCCEEDED
                if (info != null && info.state == WorkInfo.State.SUCCEEDED) {
                    showResult("First process is done")
                }
            }

        workManager.getWorkInfoByIdLiveData(secondRequest.id)
            .observe(this) { info ->
                // DIPERBARUI: Menggunakan pengecekan SUCCEEDED
                if (info != null && info.state == WorkInfo.State.SUCCEEDED) {
                    launchNotificationService()
                }
            }
    }
    private fun getIdInputData(idKey: String, idValue: String) =
        Data.Builder()
            .putString(idKey, idValue)
            .build()
    //Show the result as toast
    private fun showResult(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // KODE BARU: Meluncurkan NotificationService
    private fun launchNotificationService() {
        //Observe if the service process is done or not
        //If it is, show a toast with the channel ID in it
        NotificationService.trackingCompletion.observe(
            this) { Id ->
            showResult("Process for Notification Channel ID $Id is done!")
        }

        //Create an Intent to start the NotificationService
        //An ID of "001" is also passed as the notification channel ID
        val serviceIntent = Intent(this,
            NotificationService::class.java).apply {
            putExtra(EXTRA_ID, "001")
        }

        //Start the foreground service through the Service Intent
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    // KODE BARU: Companion object untuk EXTRA_ID
    companion object{
        const val EXTRA_ID = "Id"
    }
}