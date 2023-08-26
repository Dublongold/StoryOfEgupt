package com.stor.storyofegupt.menuScreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import com.stor.storyofegupt.R

class MenuFragment(
    private val actionForPlay: () -> Boolean,
    private val actionForHelp: () -> Boolean
): Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<AppCompatButton>(R.id.helpButton).setOnClickListener {
            actionForHelp()
        }
        view.findViewById<AppCompatButton>(R.id.playButton).setOnClickListener {
            actionForPlay()
        }
    }
}