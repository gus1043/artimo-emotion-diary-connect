package com.example.artimo_emotion_diary

import android.app.Activity
import android.content.Context
import android.content.res.AssetManager
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import java.io.IOException

import android.content.Intent


class EmojiAdapter(
    private val context: Context,
    private val imageList: List<String>,
    private val year: Int,
    private val month: Int,
    private val day: Int
) : RecyclerView.Adapter<EmojiAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_emoji, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageFileName = imageList[position]

        Log.d("EmojiAdapter", "$year, $month, $day")

        try {
            // 이미지 파일을 AssetManager를 사용하여 읽어옴
            val assetManager: AssetManager = context.assets
            val inputStream = assetManager.open(imageFileName)
            val drawable = Drawable.createFromStream(inputStream, null)
            holder.imageView.setImageDrawable(drawable)
            inputStream.close()

            // 이모지 별 감정 타이틀 매핑
            val imageToTitleMap = mapOf(
                "a_Happy.png" to "행복 가득한 하루였어요.",
                "b_Happy.png" to "행복 가득한 하루였어요.",
                "c_Love.png" to "사랑 가득한 하루를 보냈어요.",
                "d_Love.png" to "사랑에 빠진 듯한 하루였어요.",
                "e_Sad.png" to "조금 속상한 하루였어요.",
                "f_Sick.png" to "몸이나 마음이 조금 아팠어요.",
                "g_Sorrow.png" to "슬픔이 있었던 하루였어요.",
                "h_Depressed.png" to "우울한 하루를 보냈어요.",
                "i_Upset.png" to "속상한 일이 있었어요.",
                "j_Tears.png" to "눈물이 흐르는 일이 있었어요.",
                "k_Laugh.png" to "웃음 가득한 하루를 보냈어요.",
                "l_Surprise.png" to "깜짝 놀랄 만한 일이 있었어요.",
                "m_SadLaugh.png" to "웃픈 하루를 보냈어요.",
                "n_Wonderful.png" to "멋진 하루를 보냈어요.",
                "o_Joyful.png" to "유쾌한 하루를 보냈어요.",
                "p_Calm.png" to "마음이 편안해지는 하루였어요.",
                "q_Fun.png" to "재미있는 하루를 보냈군요?",
                "r_Unexpected.png" to "예상치 못한 일이 생겼어요.",
                "s_Upset.png" to "속상한 일이 있었어요.",
                "t_Unhappy.png" to "불만이 쌓였던 하루였군요.",
                "u_Embarrassed.png" to "당황스러운 하루였어요.",
                "v_Angry.png" to "화가 났던 날이었어요.",
                "w_Hard.png" to "정말 힘든 하루를 보냈군요.",
                "x_Shocking.png" to "충격적인 일이 있었어요.",
                "y_HardDay.png" to "오늘 하루 정말 힘들었어요.",
                "z_Angel.png" to "천사같은 하루를 보냈어요.",
                "z2_Demon.png" to "악마같은 하루를 보냈어요."
            )

            // 이미지를 클릭할 때의 동작 설정
            holder.imageView.setOnClickListener {
                val title = imageToTitleMap[imageFileName] ?: "기본 제목"
                val intent = Intent(context, WriteActivity::class.java).apply {
                    putExtra("emoji", imageFileName)
                    putExtra("title", title)
                    putExtra("YEAR", year)
                    putExtra("MONTH", month)
                    putExtra("DAY", day)
                }
                context.startActivity(intent)
                // 현재 Activity 종료
                if (context is Activity) {
                    (context as Activity).finish()
                    (context as Activity).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

                }
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }
}
