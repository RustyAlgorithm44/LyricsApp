package com.guruguhan.lyricsapp.ui

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import com.guruguhan.lyricsapp.R
import com.guruguhan.lyricsapp.databinding.ItemLanguageInputBinding
import java.util.*

data class LyricsData(var language: String, var lyrics: String, var otherLanguage: String = "")

class LanguageLyricsAdapter(
    private val context: Context,
    val items: MutableList<LyricsData>,
    private val allLanguages: List<String>,
    private val onStartDrag: (RecyclerView.ViewHolder) -> Unit
) : RecyclerView.Adapter<LanguageLyricsAdapter.ViewHolder>() {

    private val payloadUpdate = "PAYLOAD_UPDATE"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLanguageInputBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        // Re-binding is now fast enough that we don't need complex payload logic.
        // Just call bind to ensure the view is always in the correct state.
        holder.bind()
    }

    override fun getItemCount(): Int = items.size

    fun getUsedLanguages(): Set<String> {
        return items.mapNotNull {
            val lang = if (it.language == "Other") it.otherLanguage else it.language
            if (lang.isNotBlank()) lang else null
        }.toSet()
    }

    private fun onLanguageChanged(position: Int) {
        // Notify all other items that their spinner list might need to change
        for (i in 0 until itemCount) {
            if (i != position) {
                notifyItemChanged(i, payloadUpdate)
            }
        }
    }

    inner class ViewHolder(private val binding: ItemLanguageInputBinding) : RecyclerView.ViewHolder(binding.root) {

        private val lyricsTextWatcher: TextWatcher
        private val otherLangTextWatcher: TextWatcher
        private val spinnerListener: AdapterView.OnItemSelectedListener

        init {
            lyricsTextWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                        items[bindingAdapterPosition].lyrics = s.toString()
                    }
                }
                override fun afterTextChanged(s: Editable?) {}
            }

            otherLangTextWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                        items[bindingAdapterPosition].otherLanguage = s.toString().trim()
                        onLanguageChanged(bindingAdapterPosition)
                    }
                }
                override fun afterTextChanged(s: Editable?) {}
            }

            spinnerListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                    if (bindingAdapterPosition == RecyclerView.NO_POSITION) return
                    val selectedLanguage = parent?.getItemAtPosition(pos) as? String ?: return
                    val currentItem = items[bindingAdapterPosition]
                    if (currentItem.language == selectedLanguage) return

                    currentItem.language = selectedLanguage
                    if (selectedLanguage == "Other") {
                        binding.otherLanguageLayout.visibility = View.VISIBLE
                    } else {
                        binding.otherLanguageLayout.visibility = View.GONE
                        currentItem.otherLanguage = ""
                    }
                    onLanguageChanged(bindingAdapterPosition)
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            binding.dragHandle.setOnTouchListener { _, event ->
                if (event.actionMasked == MotionEvent.ACTION_DOWN) { onStartDrag(this) }
                false
            }
            binding.removeLanguageButton.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    items.removeAt(bindingAdapterPosition)
                    notifyItemRemoved(bindingAdapterPosition)
                    onLanguageChanged(-1) // Update all remaining spinners
                }
            }
        }

        fun bind() {
            // 1. Remove all potentially looping listeners
            binding.languageSpinner.onItemSelectedListener = null
            binding.otherLanguageInput.removeTextChangedListener(otherLangTextWatcher)
            binding.inputLyrics.removeTextChangedListener(lyricsTextWatcher)

            // 2. Update all views with current data
            val item = items[bindingAdapterPosition]
            binding.inputLyrics.setText(item.lyrics)

            val usedLanguages = getUsedLanguages()
            val currentLanguage = if (item.language == "Other") item.otherLanguage else item.language
            val availableLanguages = allLanguages.filter { lang -> !usedLanguages.contains(lang) || lang == "Other" || lang == currentLanguage }
            val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, availableLanguages)
            binding.languageSpinner.adapter = adapter

            val selectionPosition = availableLanguages.indexOf(item.language)
            if (selectionPosition != -1) {
                binding.languageSpinner.setSelection(selectionPosition, false)
            }

            if (item.language == "Other") {
                binding.otherLanguageLayout.visibility = View.VISIBLE
                binding.otherLanguageInput.setText(item.otherLanguage)
            } else {
                binding.otherLanguageLayout.visibility = View.GONE
            }

            // 3. Re-attach listeners
            binding.inputLyrics.addTextChangedListener(lyricsTextWatcher)
            binding.otherLanguageInput.addTextChangedListener(otherLangTextWatcher)
            binding.languageSpinner.post { binding.languageSpinner.onItemSelectedListener = spinnerListener }
        }
    }
}