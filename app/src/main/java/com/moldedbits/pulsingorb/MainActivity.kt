package com.moldedbits.pulsingorb

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.moldedbits.pulsingorb.widget.FireFlies

class MainActivity : AppCompatActivity() {

    var fireFlies: FireFlies? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fireFlies = findViewById(R.id.flies) as FireFlies
        fireFlies?.setOnClickListener(
                {fireFlies?.showPairing()}
        )
    }
}
