package com.monkey.game.ui.first

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.monkey.game.R
import com.monkey.game.databinding.FragmentFirstBinding
import com.monkey.game.ui.other.ViewBindingFragment

class FragmentFirst : ViewBindingFragment<FragmentFirstBinding>(FragmentFirstBinding::inflate) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            play.setOnClickListener {
                findNavController().navigate(R.id.action_fragmentMain_to_fragmentMonkeyGame)
            }

            quit.setOnClickListener {
                requireActivity().finish()
            }
        }

        binding.privacyText.setOnClickListener {
            requireActivity().startActivity(
                Intent(
                    ACTION_VIEW,
                    Uri.parse("https://www.google.com")
                )
            )
        }
    }
}