package com.monkey.game.domain

import com.monkey.game.core.XY

data class Symbol(
    override var x: Float,
    override var y: Float,
    val value: Int
) : XY