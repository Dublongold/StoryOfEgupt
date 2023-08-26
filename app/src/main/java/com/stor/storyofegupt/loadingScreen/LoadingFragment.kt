package com.stor.storyofegupt.loadingScreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.stor.storyofegupt.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoadingFragment: Fragment() {

    lateinit var tv: TextView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_loading, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv = view.findViewById(R.id.loadingText)

        viewLifecycleOwner.lifecycleScope.launch {
            while(!isDetached) {
                if(tv.text.endsWith("...")) {
                    tv.text = tv.text.substring(0, tv.text.length - 3)
                }
                else {
                    tv.text = tv.text.toString() + '.'
                }
                delay(333)
            }
        }
    }
}