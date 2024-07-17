package com.washcloud.consoleapplication.ui.common

sealed class SelectedView{
    object LoginForm: SelectedView()
    object Ad2Form: SelectedView()
    object HelpForm: SelectedView()
    object DriverLoginForm: SelectedView()
    object PickUpView: SelectedView()
    object DropOffView: SelectedView()
    object SelectLockerView: SelectedView()
    object AdminLogInView: SelectedView()
    object AdminMenuView: SelectedView()
    object PCSettingsScreen: SelectedView()
    object SubAdminSettingsScreen: SelectedView()
    object ChangePasswordScreen: SelectedView()
    object UpdatePhoneNumberScreen: SelectedView()
    object AdsManagementScreen: SelectedView()
    object AdminLockerScreen: SelectedView()
    fun copy(): SelectedView{
        return this
    }
}
