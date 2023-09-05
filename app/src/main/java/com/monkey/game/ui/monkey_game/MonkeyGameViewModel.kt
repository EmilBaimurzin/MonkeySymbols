package com.monkey.game.ui.monkey_game

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monkey.game.core.XY
import com.monkey.game.core.XYIMpl
import com.monkey.game.core.library.random
import com.monkey.game.domain.Symbol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MonkeyGameViewModel : ViewModel() {
    var gameState = true
    var pauseState = false
    private var gameScope = CoroutineScope(Dispatchers.Default)
    private val _playerXY = MutableLiveData<XY>()
    val playerXY: LiveData<XY> = _playerXY

    private val _symbols = MutableLiveData<List<Symbol>>(emptyList())
    val symbols: LiveData<List<Symbol>> = _symbols

    private val _lives = MutableLiveData(3)
    val lives: LiveData<Int> = _lives

    private val _scores = MutableLiveData(0)
    val scores: LiveData<Int> = _scores

    var canFall = true

    fun start(
        symbolSize: Int,
        maxX: Int,
        fallDelay: Long,
        maxY: Int,
        playerWidth: Int,
        playerHeight: Int,
        distance: Int
    ) {
        canFall = true
        gameScope = CoroutineScope(Dispatchers.Default)
        generateSymbols(symbolSize, maxX)

        letItemsFall(fallDelay, maxY, symbolSize, playerWidth, playerHeight, distance)
    }

    fun stop() {
        gameScope.cancel()
    }

    private fun generateSymbols(symbolSize: Int, maxX: Int) {
        gameScope.launch {
            while (true) {
                delay(1200)
                canFall = false
                val currentList = _symbols.value!!.toMutableList()
                val randomValue = if (1 random 50 == 1) 7 else 1 random 6
                currentList.add(
                    Symbol(
                        y = 0f - symbolSize,
                        x = (0 random (maxX - symbolSize)).toFloat(),
                        value = randomValue
                    )
                )
                _symbols.postValue(currentList)
                canFall = true
            }
        }
    }

    private fun letItemsFall(
        fallDelay: Long,
        maxY: Int,
        symbolSize: Int,
        playerWidth: Int,
        playerHeight: Int,
        distance: Int
    ) {
        gameScope.launch {
            while (true) {
                delay(fallDelay)
                if (canFall) {
                    _symbols.postValue(
                        moveSomethingDown(
                            maxY,
                            symbolSize,
                            symbolSize,
                            playerWidth,
                            playerHeight,
                            _symbols.value!!.toMutableList(),
                            { value ->
                                value as Symbol
                                if (value.value == 7) {
                                    if (_lives.value!! + 1 <= 3) {
                                        _lives.postValue(_lives.value!! + 1)
                                    }
                                } else {
                                    if (value.value < 6) {
                                        _scores.postValue(_scores.value!! + 10)
                                    } else {
                                        _scores.postValue(_scores.value!! - 50)
                                        _lives.postValue(_lives.value!! - 1)
                                    }
                                }
                            }, {},
                            distance = distance
                        ).toList() as List<Symbol>
                    )
                }
            }
        }
    }

    private suspend fun moveSomethingDown(
        maxY: Int,
        objHeight: Int,
        objWidth: Int,
        playerWidth: Int,
        playerHeight: Int,
        oldList: MutableList<XY>,
        onIntersect: ((XY) -> Unit) = {},
        onOutOfScreen: ((XY) -> Unit) = {},
        distance: Int
    ): MutableList<XY> {
        return suspendCoroutine { cn ->
            val newList = mutableListOf<XY>()
            oldList.forEach { obj ->
                if (obj.y <= maxY) {
                    val objX = obj.x.toInt()..obj.x.toInt() + objWidth
                    val objY = obj.y.toInt()..obj.y.toInt() + objHeight
                    val playerX =
                        _playerXY.value!!.x.toInt().._playerXY.value!!.x.toInt() + playerWidth
                    val playerY =
                        _playerXY.value!!.y.toInt().._playerXY.value!!.y.toInt() + playerHeight
                    if (playerX.any { it in objX } && playerY.any { it in objY }) {
                        onIntersect.invoke(obj)
                    } else {
                        obj.y = obj.y + distance
                        obj.x = obj.x
                        newList.add(obj)
                    }
                } else {
                    onOutOfScreen.invoke(obj)
                }
            }
            cn.resume(newList)
        }
    }

    fun initPlayer(x: Int, y: Int, playerWidth: Int) {
        _playerXY.postValue(
            XYIMpl(
                x = x / 2 - (playerWidth.toFloat() / 2),
                y = y - playerWidth.toFloat()
            )
        )
    }

    fun playerMoveLeft(limit: Float) {
        if (_playerXY.value!!.x - 4f > limit) {
            _playerXY.postValue(XYIMpl(_playerXY.value!!.x - 4, _playerXY.value!!.y))
        }
    }

    fun playerMoveRight(limit: Float) {
        if (_playerXY.value!!.x + 4f < limit) {
            _playerXY.postValue(XYIMpl(_playerXY.value!!.x + 4, _playerXY.value!!.y))
        }
    }

    override fun onCleared() {
        super.onCleared()
        stop()
    }
}