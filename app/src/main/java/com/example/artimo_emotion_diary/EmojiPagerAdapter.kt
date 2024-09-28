package com.example.artimo_emotion_diary

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Log

class EmojiPagerAdapter(
    private val context: Context,
    private val imageList: List<String>,
    private val year: Int,
    private val month: Int,
    private val day: Int
) :
    RecyclerView.Adapter<EmojiPagerAdapter.ImageViewHolder>() {

    private val imagesPerPage = 20 // 4열 * 5행
    private val pages: List<List<String>> = imageList.chunked(imagesPerPage)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_emoji_page, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val pageImages = pages[position]
        Log.d("EmojiPagerAdapter", "Page $position: $pageImages")
        holder.recyclerView.layoutManager = GridLayoutManager(context, 4) // 4열 설정
        holder.recyclerView.adapter = EmojiAdapter(context, pageImages, year, month, day)
    }

    override fun getItemCount(): Int {
        return pages.size
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recyclerView: RecyclerView = itemView.findViewById(R.id.pageRecyclerView)
    }
}
