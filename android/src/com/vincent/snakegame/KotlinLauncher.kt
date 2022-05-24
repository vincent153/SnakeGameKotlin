package com.vincent.snakegame

import android.os.Bundle
import android.util.Log
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration

class KotlinLauncher : AndroidApplication(){
    val TAG = "KotlinLauncher"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG,"launch from kotlin")
        var config = AndroidApplicationConfiguration()
        initialize(SnakeGameKT(), config)
    }
}