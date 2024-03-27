package com.washcloud.consoleapplication.utils

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutlinedInputField(
    value: String,
    onValueChange: (String) -> Unit,
    hintText: String,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorValue: String? = null,
    hintTextSize: TextUnit? = null,
    colors: TextFieldColors? = null,
    cornerRadius: Float? = null,
) {
    OutlinedTextField(
        value = value,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        placeholder = {
            Text(
                text = hintText,
                color = hints,
                fontSize = hintTextSize?:16.sp,
                style = MaterialTheme.typography.headlineMedium
            )
        },
//        supportingText = {
//            if (isError) {
//                Row(
//                    modifier = Modifier.padding(top = 2.dp)
//                ) {
//                    Icon(
//                        ImageVector.vectorResource(id = R.drawable.ic_error_outline),
//                        "error",
//                        tint = MaterialTheme.colorScheme.error
//                    )
//                    Spacer(modifier = Modifier.width(2.dp))
//                    Text(
//                        modifier = Modifier.fillMaxWidth(),
//                        text = errorValue ?: "",
//                        color = MaterialTheme.colorScheme.error,
//                        style = MaterialTheme.typography.headlineMedium,
//                        fontSize = 12.sp
//                    )
//                }
//            }
//        },
        isError = isError,
        shape = RoundedCornerShape(cornerRadius?.dp?:5.dp),
        modifier = modifier,
        colors = colors ?: OutlinedTextFieldDefaults.colors(
            focusedBorderColor = secondaryColor,
            unfocusedBorderColor = borderColor,
            focusedTextColor = primaryDark,
            cursorColor = primaryDark
        ),
        singleLine = true,
        onValueChange = onValueChange,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        textStyle = MaterialTheme.typography.headlineMedium
    )
}
