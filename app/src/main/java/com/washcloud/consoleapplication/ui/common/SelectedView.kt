package com.washcloud.consoleapplication.ui.common

sealed class SelectedView{
    object LoginForm: SelectedView()
    object Ad2Form: SelectedView()
    object HelpForm: SelectedView()
    object DriverLoginForm: SelectedView()
    object PickUpView: SelectedView()
}
