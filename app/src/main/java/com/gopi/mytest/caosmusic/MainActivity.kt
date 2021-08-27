package com.gopi.mytest.caosmusic

import android.Manifest
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import java.io.File


class MainActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private var items: Array<String>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView( R.layout.layout_action_bar )

        listView = findViewById(R.id.songs_list_View)

        runtimePermission()

    }

    private fun runtimePermission() {
        Dexter.withContext(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                    displaySongs()
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<PermissionRequest>?,
                    p1: PermissionToken?
                ) {
                    p1?.continuePermissionRequest()
                }

            }).check()

    }

    fun findSongs(file: File): ArrayList<File> {
        var list: ArrayList<File> = ArrayList()

        file.listFiles()?.forEach { it ->
            if (it.isDirectory && it.isHidden.not()) {
                list.addAll(
                    findSongs(
                        it
                    )
                )
            } else {
                if (it.name.endsWith("mp3") || it.name.endsWith(".wav")) {
                    list.add(it)
                }
            }
        }
        return list
    }

    fun findSongs(): ArrayList<File> {
        val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DURATION
        )

        val cursor = this.managedQuery(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            null
        )
        val songs: MutableList<File> = ArrayList()
        while (cursor.moveToNext()) {
            val path = cursor.getString(3)

            songs.add(
                File(path)
            )
        }
        return songs as ArrayList<File>
    }

    private fun displaySongs() {
        val mySongs: ArrayList<File> =  findSongs()
        items = Array<String>(mySongs.size){ "it = $it" }

        var i = 0
        mySongs.forEach {
            items?.apply {
                this[i] = it.name.toString().replace(".mp3", "").replace(".wav", "")
                i++
            }
        }

        val adapter = CustomAdapter(this, items ?: Array<String>(1){"Unable to find Music"})
        listView.adapter = adapter
        listView.setOnItemClickListener { adapterView, view, i, l ->
            val songName: String = listView.getItemAtPosition(i).toString()
            startActivity(Intent(applicationContext, PlayerActivity::class.java).apply {
                putExtra("songs", mySongs)
                putExtra("songname", songName)
                putExtra("pos", i)
            })
        }
    }
}