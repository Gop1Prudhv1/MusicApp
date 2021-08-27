package com.gopi.mytest.caosmusic

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class CustomAdapter (private val context: Context, val items: Array<String>): BaseAdapter(){

    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(p0: Int): Any {
        return Any()
    }

    override fun getItemId(p0: Int): Long {
        return 0L
    }

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val view = LayoutInflater.from(context).inflate(R.layout.music_list_item, null)
        val tv = view.findViewById<TextView>(R.id.tv_song_name)
        tv.isSelected = true
        tv.text = items[p0]
        return view
    }
}