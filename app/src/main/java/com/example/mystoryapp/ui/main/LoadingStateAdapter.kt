package com.example.mystoryapp.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mystoryapp.R

class LoadingStateAdapter(private val retry: () -> Unit) : LoadStateAdapter<LoadingStateAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_loading, parent, false)
        return ViewHolder(view, retry)
    }

    override fun onBindViewHolder(holder: ViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    class ViewHolder(itemView: View, retry: () -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val progressBar = itemView.findViewById<ProgressBar>(R.id.progress_bar_state)
        private val retryButton = itemView.findViewById<Button>(R.id.retry_button)
        private val errorMsg = itemView.findViewById<TextView>(R.id.error_msg)

        init {
            retryButton.setOnClickListener { retry() }
        }

        fun bind(loadState: LoadState) {
            progressBar.isVisible = loadState is LoadState.Loading
            retryButton.isVisible = loadState is LoadState.Error
            errorMsg.isVisible = loadState is LoadState.Error

            if (loadState is LoadState.Error) {
                errorMsg.text = loadState.error.localizedMessage ?: itemView.context.getString(R.string.default_error_message)
            } else {
                errorMsg.text = ""
            }
        }
    }
}

