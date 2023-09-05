package com.monkey.game.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.monkey.game.R
import com.monkey.game.core.library.ViewBindingDialog
import com.monkey.game.databinding.DialogGameEndingBinding
import com.monkey.game.domain.SP

class DialogGameEnding: ViewBindingDialog<DialogGameEndingBinding>(DialogGameEndingBinding::inflate)  {
    private val args: DialogGameEndingArgs by navArgs()
    private val sp by lazy {
        SP(requireContext())
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext(), R.style.Dialog_No_Border)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog!!.setCancelable(false)
        dialog!!.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                findNavController().popBackStack(R.id.fragmentMain, false, false)
                true
            } else {
                false
            }
        }
        binding.scores.text = args.scores.toString()
        binding.bestScores.text = sp.getBest().toString()

        binding.home.setOnClickListener {
            findNavController().popBackStack(R.id.fragmentMain, false, false)
        }
        binding.restart.setOnClickListener {
            findNavController().popBackStack(R.id.fragmentMain, false, false)
            findNavController().navigate(R.id.action_fragmentMain_to_fragmentMonkeyGame)
        }
    }
}