package com.example.mystoryapp.ui

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText

class CustomPasswordEditText(context: Context, attrs: AttributeSet?) : AppCompatEditText(context, attrs) {

    fun validatePassword(): Boolean {
        return if ((text?.length ?: 0) < 8) {
            error = "Password must be at least 8 characters"
            false
        } else {
            true
        }
    }
}

