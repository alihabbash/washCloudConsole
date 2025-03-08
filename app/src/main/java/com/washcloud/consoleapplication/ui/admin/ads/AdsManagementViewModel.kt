package com.washcloud.consoleapplication.ui.admin.ads

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdsManagementViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val _adsList = MutableStateFlow<List<Uri>>(emptyList())
    val adsList: StateFlow<List<Uri>> = _adsList
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)
    fun addFile(uri: Uri) {
        viewModelScope.launch {
            val updatedList = _adsList.value.toMutableList()
            updatedList.add(uri)
            _adsList.value = updatedList
            saveAdsList(updatedList)
        }
    }

    fun removeFile(uri: Uri) {
        viewModelScope.launch {
            val updatedList = _adsList.value.toMutableList()
            updatedList.remove(uri)
            _adsList.value = updatedList
            saveAdsList(updatedList)
        }
    }

    private fun saveAdsList(adsList: List<Uri>) {
        val adsString = adsList.joinToString(",") { it.toString() }
        sharedPreferences.edit().putString("ADS_ARRAY", adsString).apply()
    }

    init {
        loadAdsList()
    }

    private fun loadAdsList() {

        val adsString: Set<String>? = try {
            sharedPreferences.getStringSet("ADS_ARRAY", emptySet())
        } catch (e: Exception) {
            Log.e("MainAdViewModel", "Error retrieving ADS_ARRAY from SharedPreferences", e)
            emptySet()
        }
        adsString?.let {
            val adsUris = it.map { uriString -> Uri.parse(uriString) }
            _adsList.value = adsUris
        }
    }

    fun saveAds() {
        viewModelScope.launch {
            val editor = sharedPreferences.edit()
            val uriStrings = _adsList.value.map { it.toString() }
            editor.putStringSet("ADS_ARRAY", uriStrings.toSet())
            editor.apply()
        }
    }
}