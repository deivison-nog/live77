package com.live77.ui.channels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.live77.data.M3uParser
import com.live77.model.Channel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChannelListViewModel : ViewModel() {

    companion object {
        private const val PLAYLIST_URL = "https://ontvadmin.info85.com.br/files/lista.m3u"
    }

    private val _channels = MutableLiveData<List<Channel>>()
    val channels: LiveData<List<Channel>> = _channels

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadChannels() {
        _loading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    M3uParser.fetchAndParse(PLAYLIST_URL)
                }
                _channels.value = result
            } catch (e: Exception) {
                _error.value = "Erro ao carregar canais: ${e.message}"
                _channels.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }
}
