package com.example.mystoryapp.ui

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.example.mystoryapp.R

class CustomPasswordEditText(context: Context, attrs: AttributeSet?) : AppCompatEditText(context, attrs) {

    fun validatePassword(): Boolean {
        return if ((text?.length ?: 0) < 8) {
            error =  context.getString(R.string.invalid_password)
            false
        } else {
            true
        }
    }
}

