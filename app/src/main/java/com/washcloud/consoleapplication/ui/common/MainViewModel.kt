package com.washcloud.consoleapplication.ui.common

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class MainViewModel @Inject constructor() : ViewModel() {
    private val _stack = MutableStateFlow<MutableList<SelectedView>?>(mutableListOf(SelectedView.Ad2Form))
    val stack = _stack.asStateFlow()

    fun addToStack(g: SelectedView){
        var list = stack.value
        if (list != null) {
            for (i in list.size - 1 downTo 0){
                if(list[i].javaClass == g.javaClass){
                    list.removeAt(i)
                }
            }
        }
        list?.add(g)
        _stack.value = list
    }

    fun popStack(){
        val list = stack.value
        list?.removeAt(list.size-1)
        _stack.value = list
    }

    fun resetStack(){
        val list = stack.value
        list?.clear()
        list?.add(SelectedView.Ad2Form)
        _stack.value = list
    }

    fun getStackTop(): SelectedView{
        var list = stack.value
        return list!![list.size-1]
    }
}