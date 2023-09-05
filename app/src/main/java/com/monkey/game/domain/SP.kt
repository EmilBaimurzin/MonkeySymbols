package com.monkey.game.domain

import android.content.Context

class SP(context: Context) {
    private val sp = context.getSharedPreferences("SP", Context.MODE_PRIVATE)

    fun getBest(): Int = sp.getInt("BEST", 0)
    fun setBest(best: Int) = sp.edit().putInt("BEST", best).apply()
}