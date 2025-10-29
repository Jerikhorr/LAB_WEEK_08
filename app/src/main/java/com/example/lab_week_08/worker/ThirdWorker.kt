package com.example.lab_week_08.worker

import android.content.Context
import androidx.work.Data
import androidx.work.ListenableWorker.Result
import androidx.work.Worker
import androidx.work.WorkerParameters

class ThirdWorker(
    context: Context, workerParams: WorkerParameters
) : Worker(context, workerParams) {
    override fun doWork(): Result {
        // Dapatkan parameter input (jika ada)
        val id = inputData.getString(INPUT_DATA_ID)

        // Tidurkan proses selama 2 detik
        Thread.sleep(2000L)

        // Buat output
        val outputData = Data.Builder()
            .putString(OUTPUT_DATA_ID, id)
            .build()

        // Kembalikan status sukses
        return Result.success(outputData)
    }
    companion object {
        const val INPUT_DATA_ID = "inId"
        const val OUTPUT_DATA_ID = "outId"
    }
}