package com.example.tetris.ui

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tetris.R
import com.example.tetris.logic.BonusManager
import com.example.tetris.logic.BonusType
import com.example.tetris.ui.effects.ParticleEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BonusShowcaseActivity : AppCompatActivity() {

    private lateinit var tvScore: TextView
    private lateinit var tvLines: TextView
    private lateinit var rvBonuses: RecyclerView
    private lateinit var btnRandomBonus: Button

    private var currentScore = 0
    private var currentLines = 0
    private val bonusManager = BonusManager()
    private val particleEffect = ParticleEffect()
    
    // Giả lập board 20x10
    private val board = Array(20) { Array(10) { 0L } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bonus_showcase)

        tvScore = findViewById(R.id.tvScore)
        tvLines = findViewById(R.id.tvLines)
        rvBonuses = findViewById(R.id.rvBonuses)
        btnRandomBonus = findViewById(R.id.btnRandomBonus)

        rvBonuses.layoutManager = LinearLayoutManager(this)
        rvBonuses.adapter = BonusAdapter(BonusType.values().toList()) { type, view ->
            activateBonus(type, view)
        }

        btnRandomBonus.setOnClickListener {
            val randomType = BonusType.values().random()
            // Tìm view tương ứng trong RecyclerView (nếu có thể) hoặc chỉ kích hoạt chung
            activateBonus(randomType, btnRandomBonus)
        }
    }

    private fun activateBonus(type: BonusType, targetView: View) {
        lifecycleScope.launch {
            // 1. Hiển thị hiệu ứng
            showEffectAnimation(type, targetView)

            // 2. Kích hoạt logic qua BonusManager
            val bonusPoints = bonusManager.activateBonus(type, board) {
                // Callback khi hiệu ứng logic xong
            }

            // 3. Cập nhật UI
            currentScore += bonusPoints
            currentLines += type.extraLines
            tvScore.text = "SCORE: $currentScore"
            tvLines.text = "LINES: $currentLines"
        }
    }

    private suspend fun showEffectAnimation(type: BonusType, view: View) {
        when (type.effect) {
            com.example.tetris.logic.BonusEffect.EXPLOSION -> {
                // Rung nhẹ + phóng to/thu nhỏ
                val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.2f, 1f)
                val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.2f, 1f)
                ObjectAnimator.ofPropertyValuesHolder(view, scaleX, scaleY).setDuration(300).start()
                
                // Hiệu ứng particle
                particleEffect.showExplosionParticles(view, this@BonusShowcaseActivity)
                delay(300)
            }
            com.example.tetris.logic.BonusEffect.LIGHTNING_STRIKE -> {
                // Flash trắng toàn màn hình
                particleEffect.showLightningFlash(this@BonusShowcaseActivity)
                delay(100)
            }
            com.example.tetris.logic.BonusEffect.MONO_CHANGE -> {
                view.animate().alpha(0.5f).setDuration(200).withEndAction {
                    view.animate().alpha(1f).setDuration(200).start()
                }.start()
                delay(400)
            }
            com.example.tetris.logic.BonusEffect.GHOST_SWAP -> {
                view.animate().scaleX(0.8f).scaleY(0.8f).alpha(0.3f).setDuration(500).withEndAction {
                    view.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(500).start()
                }.start()
                delay(1000)
            }
            com.example.tetris.logic.BonusEffect.COLOR_PURGE -> {
                view.animate().rotation(360f).setDuration(500).withEndAction {
                    view.rotation = 0f
                }.start()
                delay(500)
            }
        }
    }

    inner class BonusAdapter(
        private val bonuses: List<BonusType>,
        private val onClick: (BonusType, View) -> Unit
    ) : RecyclerView.Adapter<BonusAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val icon: ImageView = view.findViewById(R.id.ivBonusIcon)
            val name: TextView = view.findViewById(R.id.tvBonusName)
            val desc: TextView = view.findViewById(R.id.tvBonusDesc)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bonus, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val bonus = bonuses[position]
            holder.icon.setImageResource(bonus.iconRes)
            holder.name.text = bonus.displayName
            holder.desc.text = "Điểm: +${bonus.bonusScore}, Dòng: +${bonus.extraLines}"
            holder.itemView.setOnClickListener { onClick(bonus, holder.itemView) }
        }

        override fun getItemCount() = bonuses.size
    }
}
