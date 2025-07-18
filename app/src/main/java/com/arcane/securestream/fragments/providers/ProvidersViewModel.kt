package com.arcane.securestream.fragments.providers

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arcane.securestream.models.Provider
import com.arcane.securestream.providers.Provider.Companion.providers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ProvidersViewModel : ViewModel() {

    private val _state = MutableStateFlow<State>(State.Loading)
    val state: Flow<State> = _state

    sealed class State {
        data object Loading : State()
        data class SuccessLoading(val providers: List<Provider>) : State()
        data class FailedLoading(val error: Exception) : State()
    }

    init {
        getProviders()
    }


    fun getProviders(language: String? = null) = viewModelScope.launch(Dispatchers.IO) {
        _state.emit(State.Loading)

        try {
            val providers = providers
                .filter { language == null || it.language == language }
                .sortedBy { it.name }
                .map {
                    Provider(
                        name = it.name,
                        logo = it.logo,
                        language = it.language,

                        provider = it,
                    )
                }

            _state.emit(State.SuccessLoading(providers))
        } catch (e: Exception) {
            Log.e("ProvidersViewModel", "getProviders: ", e)
            _state.emit(State.FailedLoading(e))
        }
    }
}