package com.jr.ghoul

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.jr.ghoul.SplashActivity
import com.jr.ghoul.dbhandler.DbUtils

class SplashActivity : AppCompatActivity() {
    private val handler by lazy { Handler() }
    private val runnable by lazy {
        Runnable {
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }
    }

    public override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        supportActionBar?.hide()
        DbUtils.attachDB(this)
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed(runnable, 2000)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
    }
}