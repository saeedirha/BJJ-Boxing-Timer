package net.ghiassy.bjjbell

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.util.Linkify
import android.view.View
import android.widget.TextView
import android.widget.Toast

class About : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val mTextView: TextView = findViewById(R.id.txtLink)
        Linkify.addLinks(mTextView, Linkify.ALL)
        mTextView.setLinkTextColor(getColor(R.color.light_blue))

    }
}