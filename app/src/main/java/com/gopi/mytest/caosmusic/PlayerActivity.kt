package com.gopi.mytest.caosmusic

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.PorterDuff
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.gopi.mytest.caosmusic.databinding.ActivityPlayerBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.lang.IllegalStateException

class PlayerActivity: AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private var position: Int = 0
    private lateinit var mySongs: ArrayList<File>

    companion object {
        const val EXTRA_NAME = "song_name"
        var mediaPlayer: MediaPlayer? = null
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        if (binding.blast != null) {
            binding.blast.release()
        }
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Now Playing"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)


        if (null != mediaPlayer) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        }

        val bundle: Bundle? = intent.extras

        mySongs = bundle?.getSerializable("songs") as ArrayList<File>
        val songName = intent.getStringExtra("songname")
        position = bundle?.getInt("pos")

        binding.apply {
            txtSn.text = mySongs.get(position).name
            txtSn.isSelected = true
            playBtn.setOnClickListener {
                if (null != mediaPlayer && mediaPlayer!!.isPlaying) {
                    playBtn.setBackgroundResource(R.drawable.ic_play)
                    mediaPlayer?.pause()
                } else {
                    playBtn.setBackgroundResource(R.drawable.ic_pause)
                    binding.seekBar.progress = 0
                    mediaPlayer?.start()
                }
            }

        }
        val uri = Uri.parse(mySongs.get(position).toString())
        mediaPlayer = MediaPlayer.create(applicationContext, uri)
        mediaPlayer?.start()

        addbtnListeners()

        setComnpletionListener()

        addListeners()

        binding.seekBar.progressDrawable.setColorFilter(
            resources.getColor(R.color.colorPrimary),
            PorterDuff.Mode.MULTIPLY
        )
        binding.seekBar.thumb.setColorFilter(
            resources.getColor(R.color.colorPrimary),
            PorterDuff.Mode.SRC_IN
        )
        mediaPlayer?.let {
            val endTime = createTime(it.duration)
            binding.txtsStop.text = endTime
        }

        binding.seekBar.max = mediaPlayer?.duration ?: 0

        Handler().postDelayed(
            object : Runnable {
                override fun run() {
                    mediaPlayer?.let {
                        val currentTime = createTime(it.currentPosition)
                        binding.txtsStart.text = currentTime
                        Handler().postDelayed(this, 1000L)
                    }
                }
            }, 1000L
        )

        setAudioSession()

     }

    private fun setAudioSession() {
        val audioSessionId = mediaPlayer?.audioSessionId
        audioSessionId?.let {
            if (it != -1) {
                binding.blast.setAudioSessionId(it)
            }
        }
    }

    private fun addbtnListeners() {
        binding.btnNext.setOnClickListener {
            mediaPlayer?.stop()
            mediaPlayer?.reset()
            mediaPlayer?.release()
            position = if (position++ >= mySongs.size-1) 0 else position++
            val u = Uri.parse(mySongs[position].toString())
            mediaPlayer = MediaPlayer.create(applicationContext, u)

            binding.txtSn.text = mySongs[position].name
            mediaPlayer?.start()

            binding.playBtn.setBackgroundResource(R.drawable.ic_pause)
            startAnimation(binding.imageView)
            binding.seekBar.progress = mediaPlayer?.currentPosition ?: 0
            binding.seekBar.max = mediaPlayer?.duration ?: 0
            binding.txtsStop.text = createTime(mediaPlayer?.duration ?: 0)
            setAudioSession()
        }

        binding.btnPrev.setOnClickListener {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            position = if (position-- <= 0) mySongs.size-1 else position--

            val u = Uri.parse(mySongs[position].toString())
            mediaPlayer = MediaPlayer.create(applicationContext, u)

            binding.txtSn.text = mySongs[position].name

            mediaPlayer?.start()
            binding.playBtn.setBackgroundResource(R.drawable.ic_pause)
            startAnimation(binding.imageView)
            binding.seekBar.progress = mediaPlayer?.currentPosition ?: 0
            binding.seekBar.max = mediaPlayer?.duration ?: 0
            binding.txtsStop.text = createTime(mediaPlayer?.duration ?: 0)
            setAudioSession()
        }

        binding.btnff.setOnClickListener {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.seekTo(it.currentPosition + 10000)
                }
                binding.seekBar.progress = it.currentPosition
            }
        }

        binding.btnrew.setOnClickListener {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.seekTo(it.currentPosition - 10000)
                }
                binding.seekBar.progress = it.currentPosition
            }
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                mediaPlayer?.seekTo(p1)
                if (p1 == binding.seekBar.max) {
                    binding.btnNext.performClick()
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                //mediaPlayer?.seekTo(binding.seekBar.progress)
            }

        })
    }

    fun startAnimation(view: View) {
        val animator = ObjectAnimator.ofFloat(view, "rotation", 0F, 360F)
        animator.duration = 1000L
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(animator)
        animatorSet.start()
    }

    fun setComnpletionListener() {
        mediaPlayer?.setOnCompletionListener(object : MediaPlayer.OnCompletionListener {
            override fun onCompletion(p0: MediaPlayer?) {
                binding.btnNext.performClick()
            }
        });
    }

    fun addListeners() {
        lifecycleScope.launch {
            setSeekBarProgress()
        }
    }

    private suspend fun setSeekBarProgress() {
        mediaPlayer?.let { mediaPlayer ->
            val totalDuration = mediaPlayer.duration
            var currentPosition = 0
            while (currentPosition <= totalDuration) {
                delay(500L)
                try {
                    currentPosition = mediaPlayer.currentPosition
                    binding.seekBar.progress = currentPosition
                } catch (e: InterruptedException ) {
                    e.printStackTrace()
                } catch (e: IllegalStateException) {
                    e.printStackTrace()
                }

            }
        }
    }

    fun createTime(duration: Int): String {
        var time = ""
        val min = duration/1000/60
        val sec = duration/1000 % 60
        time = "$time$min:"

        if (sec< 10) {
            time += 0
        }
        time += sec

        return time
    }
}