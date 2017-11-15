package com.ardaozceviz.weather.view.activities

import android.os.Bundle
import android.util.Log
import com.ardaozceviz.weather.R
import com.ardaozceviz.weather.controller.LocalForecastData
import com.ardaozceviz.weather.model.ForecastDataModel
import com.ardaozceviz.weather.model.TAG_A_MAIN
import com.ardaozceviz.weather.model.isErrorExecuted
import com.ardaozceviz.weather.view.UserInterface


class MainActivity : BaseActivity() {
    private var storedForecastData: ForecastDataModel? = null
    private lateinit var userInterface: UserInterface

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG_A_MAIN, "onCreate() is executed.")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        userInterface = UserInterface(this)
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG_A_MAIN, "onResume() is executed.")
        storedForecastData = LocalForecastData(this).retrieve()
        if (storedForecastData != null) {
            Log.d(TAG_A_MAIN, "onResume() storedForecastData: $storedForecastData.")
            userInterface.updateUI(storedForecastData!!, false)
        }
        if (!isErrorExecuted) {
            userInterface.initialize()
        }
        userInterface.stopSwipeRefresh()
    }

    override fun onPause() {
        super.onPause()
        isErrorExecuted = false
    }
}

