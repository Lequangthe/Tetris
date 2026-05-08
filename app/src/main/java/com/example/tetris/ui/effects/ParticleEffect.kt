package com.example.tetris.ui.effects

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import kotlin.random.Random

class ParticleEffect {

    /**
     * Tạo hiệu ứng hạt nổ từ một View.
     */
    fun showExplosionParticles(anchorView: View, context: Context, color: Int = Color.YELLOW) {
        val rootLayout = anchorView.rootView as? ViewGroup ?: return
        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)
        
        val centerX = location[0] + anchorView.width / 2
        val centerY = location[1] + anchorView.height / 2

        val colors = listOf(Color.RED, Color.rgb(255, 165, 0), Color.YELLOW)

        repeat(30) {
            val particle = View(context)
            val size = Random.nextInt(10, 30)
            val params = FrameLayout.LayoutParams(size, size)
            particle.layoutParams = params
            
            val shape = GradientDrawable()
            shape.shape = GradientDrawable.OVAL
            shape.setColor(colors.random())
            particle.background = shape

            particle.x = centerX.toFloat()
            particle.y = centerY.toFloat()
            
            rootLayout.addView(particle)

            val angle = Random.nextDouble(0.0, 2 * Math.PI)
            val distance = Random.nextInt(100, 400)
            val targetX = centerX + Math.cos(angle) * distance
            val targetY = centerY + Math.sin(angle) * distance

            particle.animate()
                .x(targetX.toFloat())
                .y(targetY.toFloat())
                .alpha(0f)
                .setDuration(500)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        rootLayout.removeView(particle)
                    }
                })
                .start()
        }
    }

    /**
     * Tạo hiệu ứng flash trắng toàn màn hình.
     */
    fun showLightningFlash(activity: Activity) {
        val rootLayout = activity.window.decorView as? ViewGroup ?: return
        val flashView = View(activity)
        flashView.setBackgroundColor(Color.WHITE)
        val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        flashView.layoutParams = params
        flashView.alpha = 0.8f
        
        rootLayout.addView(flashView)

        ObjectAnimator.ofFloat(flashView, View.ALPHA, 0.8f, 0f).apply {
            duration = 100
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    rootLayout.removeView(flashView)
                }
            })
            start()
        }
    }
}
