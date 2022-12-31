package net.ghiassy.bjjbell

import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuCompat
import net.ghiassy.bjjbell.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), MyDialogListener {

    private val TAG = this::class.java.simpleName

    private lateinit var mMediaPlayer: MediaPlayer
    private lateinit var binding: ActivityMainBinding

    private var mMinutes = 1
    private var mSeconds = 0
    private var mMinutesBreak = 1
    private var mSecondsBreak = 3

    private var mSender = 0
    private var isInfinity = true
    private var isTimerRunning = false
    private var mRoundNumber = 1

    private var mBreakCountDown: CountDownTimer? = null
    private var mCountDown: CountDownTimer? = null

    private var mTimeLeftInMillis: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        onInit()

        binding.btnStart.setOnClickListener(View.OnClickListener {
            binding.btnPause.isEnabled = true
            val milliseconds: Long = (((mMinutes * 60) + mSeconds) * 1000).toLong()
            startTimer(milliseconds)

        })

        binding.btnPause.setOnClickListener(View.OnClickListener {

            pauseButton()
        })

        binding.btnReset.setOnClickListener(View.OnClickListener {
            resetButton()
        })

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //adding lines between groups
        binding.toolbar.root.inflateMenu(R.menu.menu_item)
        val menuItem : MenuItem = menu!!.findItem(R.id.itm_infinity)
        menuItem.setChecked(isInfinity)
        MenuCompat.setGroupDividerEnabled(menu!!, true)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.itm_time -> {
                TimerDialog().show(supportFragmentManager, "Timer Fragment")
                mSender = R.id.itm_time
            }
            R.id.itm_breaktime -> {

                TimerDialog().show(supportFragmentManager, "Break Timer Fragment")
                mSender = R.id.itm_breaktime
            }
            R.id.itm_infinity -> {
                if (item.isChecked) {
                    isInfinity = false
                    item.setChecked(isInfinity)
                } else {
                    isInfinity = true
                    item.setChecked(isInfinity)
                }
                saveSettings()
            }
            R.id.itm_about-> {
                val intent = Intent(this, About::class.java)
                startActivity(intent)
            }

        }
        return super.onOptionsItemSelected(item)
    }

    private fun playAudio() {

        if (!mMediaPlayer.isPlaying)
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mMediaPlayer.start()
        Thread.sleep(1500)

    }

    //CallBack pattern. Implementation of TimerDialog listener to pass data between dialog and
    // activity
    override fun applyChange(minutes: Int, seconds: Int) {

        var mText = "Unknown"
        if (mSender == R.id.itm_time) {
            mMinutes = minutes
            mSeconds = seconds

            mText = "Round time set to: ${String.format("%02d", mMinutes)}:${
                String.format("%02d", mSeconds)
            }"

            Toast.makeText(applicationContext, mText, Toast.LENGTH_LONG).show()
            binding.txtTimer.text =
                "${String.format("%02d", mMinutes)}:${String.format("%02d", mSeconds)}"

            if (isTimerRunning) {
                mCountDown?.cancel()
                mCountDown = null
                resetButton()
            }
        } else if (mSender == R.id.itm_breaktime) {
            mMinutesBreak = minutes
            mSecondsBreak = seconds
            mText = "Break Time set to ${String.format("%02d", mMinutesBreak)}:${
                String.format(
                    "%02d",
                    mSecondsBreak
                )
            }"
        }
        saveSettings()
        Toast.makeText(applicationContext, mText, Toast.LENGTH_LONG).show()
    }

    private fun onInit() {

        binding.btnPause.isEnabled = false
        setSupportActionBar(binding.toolbar.root)
        getSupportActionBar()?.setDisplayShowTitleEnabled(false)
        getSupportActionBar()?.setIcon(R.mipmap.ic_launcher)

        mMediaPlayer = MediaPlayer.create(this, R.raw.ring_bell)

        //Keep screen awaken while the application is running
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)


        var sharedPre = applicationContext.getSharedPreferences(TAG, MODE_PRIVATE)

        mMinutes = sharedPre.getInt("mMinutes", 1)
        mSeconds = sharedPre.getInt("mSeconds", 0)
        mMinutesBreak = sharedPre.getInt("mMinutesBreak",0)
        mSecondsBreak = sharedPre.getInt("mSecondsBreak", 0)
        isInfinity = sharedPre.getBoolean("isInfinity",true)

        resetButton()

    }

    private fun startTimer(milliseconds: Long) {
        if (mCountDown != null)
            return

        mCountDown = object : CountDownTimer(milliseconds, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                isTimerRunning = true
                mTimeLeftInMillis = millisUntilFinished
                updateTimerText()
            }

            override fun onFinish() {
                playAudio()
                mRoundNumber++
                binding.txtRoundNumber.text = "Round $mRoundNumber"

                if (isInfinity == true) {

                    if (mSecondsBreak > 3) {
                        breakTimer()
                    } else {
                        resetButton()
                    }
                } else {
                    binding.btnPause.isEnabled = false
                    isTimerRunning = false
                    mCountDown = null
                }
            }
        }.start()
    }

    private fun breakTimer() {

        val milliseconds: Long = (((mMinutesBreak * 60) + mSecondsBreak) * 1000).toLong()
        binding.txtTimer.setTextColor(getColor(R.color.lightgreen))
        binding.btnPause.isEnabled = false
        mBreakCountDown = object : CountDownTimer(milliseconds, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                mTimeLeftInMillis = millisUntilFinished
                updateTimerText()
            }

            override fun onFinish() {
                playAudio()
                binding.txtTimer.setTextColor(getColor(R.color.red))
                binding.btnPause.isEnabled = true
                mCountDown?.start()
                mBreakCountDown = null
            }
        }.start()

    }

    private fun updateTimerText() {
        val minutes = (mTimeLeftInMillis / 1000).toInt() / 60
        val seconds = (mTimeLeftInMillis / 1000).toInt() % 60

        binding.txtTimer.text =
            "${String.format("%02d", minutes)}:${String.format("%02d", seconds)}"

    }

    private fun pauseButton() {

        if (isTimerRunning) {
            mCountDown?.cancel()
            mCountDown = null
            isTimerRunning = false
            return
        }
        startTimer(mTimeLeftInMillis)

    }

    private fun saveSettings() {
        var sharedPre = applicationContext.getSharedPreferences(TAG, MODE_PRIVATE).edit()
        sharedPre.putInt("mMinutes", mMinutes)
        sharedPre.putInt("mSeconds", mSeconds)
        sharedPre.putInt("mMinutesBreak", mMinutesBreak)
        sharedPre.putInt("mSecondsBreak", mSecondsBreak)
        sharedPre.putBoolean("isInfinity", isInfinity)
        sharedPre.apply()
    }

    private fun resetButton() {
        mRoundNumber = 1
        mCountDown?.cancel()
        mCountDown = null
        mBreakCountDown?.cancel()
        mBreakCountDown = null
        isTimerRunning = false


        binding.btnPause.isEnabled = false
        binding.txtTimer.setTextColor(getColor(R.color.red))
        binding.txtTimer.text =
            "${String.format("%02d", mMinutes)}:${String.format("%02d", mSeconds)}"

        binding.txtRoundNumber.text = "Round $mRoundNumber"

    }
}