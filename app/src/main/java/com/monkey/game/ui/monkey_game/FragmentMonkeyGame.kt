package com.monkey.game.ui.monkey_game

import android.annotation.SuppressLint
import android.graphics.Point
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.monkey.game.R
import com.monkey.game.databinding.FragmentMonkeyGameBinding
import com.monkey.game.domain.SP
import com.monkey.game.ui.other.ViewBindingFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class FragmentMonkeyGame: ViewBindingFragment<FragmentMonkeyGameBinding>(FragmentMonkeyGameBinding::inflate) {
    private var moveScope = CoroutineScope(Dispatchers.Default)
    private val viewModel: MonkeyGameViewModel by viewModels()
    private val xy by lazy {
        val display = requireActivity().windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        Pair(size.x, size.y)
    }
    private val sp by lazy {
        SP(requireContext())
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtons()

        binding.home.setOnClickListener {
            findNavController().popBackStack(R.id.fragmentMain, false, false)
        }

        viewModel.playerXY.observe(viewLifecycleOwner) {
            binding.character.x = it.x
            binding.character.y = it.y
        }

        viewModel.symbols.observe(viewLifecycleOwner) {
            binding.symbolsLayout.removeAllViews()
            it.forEach { symbol ->
                val symbolView = ImageView(requireContext())
                symbolView.layoutParams = ViewGroup.LayoutParams(dpToPx(80), dpToPx(80))
                val symbolImg = when (symbol.value) {
                    1 -> R.drawable.symbol01
                    2 -> R.drawable.symbol03
                    3 -> R.drawable.symbol04
                    4 -> R.drawable.symbol05
                    5 -> R.drawable.symbol06
                    6 -> R.drawable.bomb
                    else -> R.drawable.life
                }
                symbolView.setImageResource(symbolImg)
                symbolView.x = symbol.x
                symbolView.y = symbol.y
                if (symbol.value == 7) {
                    symbolView.scaleY = 0.5f
                    symbolView.scaleX = 0.5f
                }
                binding.symbolsLayout.addView(symbolView)
            }
        }

        viewModel.lives.observe(viewLifecycleOwner) {
            binding.livesLayout.removeAllViews()
            repeat(it) {
                val liveView = ImageView(requireContext())
                liveView.layoutParams = LinearLayout.LayoutParams(dpToPx(20), dpToPx(20)).apply {
                    marginEnd = dpToPx(2)
                    marginStart = dpToPx(2)
                }
                liveView.setImageResource(R.drawable.life)
                binding.livesLayout.addView(liveView)
            }

            if (it == 0 && viewModel.gameState) {
                end()
            }
        }

        viewModel.scores.observe(viewLifecycleOwner) {
            binding.scores.text = it.toString()
        }

        lifecycleScope.launch {
            delay(20)
            if (viewModel.playerXY.value == null) {
                viewModel.initPlayer(xy.first, xy.second, binding.character.height)
            }
            if (viewModel.gameState && !viewModel.pauseState) {
                viewModel.start(
                    dpToPx(80),
                    xy.first,
                    16,
                    xy.second,
                    binding.character.width,
                    binding.character.height,
                    10
                )
            }
        }
    }

    private fun dpToPx(dp: Int): Int {
        val displayMetrics = resources.displayMetrics
        return (dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
    }

    private fun end() {
        lifecycleScope.launch(Dispatchers.Main) {
            viewModel.stop()
            viewModel.gameState = false
            if (viewModel.scores.value!! > sp.getBest()) {
                sp.setBest(viewModel.scores.value!!)
            }
            findNavController().navigate(
                FragmentMonkeyGameDirections.actionFragmentMonkeyGameToDialogGameEnding(
                    viewModel.scores.value!!
                )
            )
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setButtons() {
        binding.buttonLeft.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    moveScope.launch {
                        while (true) {
                            viewModel.playerMoveLeft(0f)
                            delay(2)
                        }
                    }
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    moveScope.cancel()
                    moveScope = CoroutineScope(Dispatchers.Default)
                    moveScope.launch {
                        while (true) {
                            viewModel.playerMoveLeft(0f)
                            delay(2)
                        }
                    }
                    true
                }

                else -> {
                    moveScope.cancel()
                    moveScope = CoroutineScope(Dispatchers.Default)
                    false
                }
            }
        }

        binding.buttonRight.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    moveScope.launch {
                        while (true) {
                            viewModel.playerMoveRight((xy.first - binding.character.width).toFloat())
                            delay(2)
                        }
                    }
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    moveScope.cancel()
                    moveScope = CoroutineScope(Dispatchers.Default)
                    moveScope.launch {
                        while (true) {
                            viewModel.playerMoveRight((xy.first - binding.character.width).toFloat())
                            delay(2)
                        }
                    }
                    true
                }

                else -> {
                    moveScope.cancel()
                    moveScope = CoroutineScope(Dispatchers.Default)
                    false
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.stop()
    }
}