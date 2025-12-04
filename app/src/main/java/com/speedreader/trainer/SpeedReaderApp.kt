package com.speedreader.trainer

import android.app.Application
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SpeedReaderApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize PDFBox for PDF parsing
        PDFBoxResourceLoader.init(applicationContext)
    }
}

