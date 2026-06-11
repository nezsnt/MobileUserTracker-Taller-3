package com.example.taller3.util

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.example.taller3.view.AuthState
import com.example.taller3.view.AuthViewModel
import com.example.taller3.R

@Composable
fun ButtonShared(text: String, action : ()->Unit ) {
    Button(
        onClick = action, modifier = Modifier.fillMaxWidth(),
        colors = ButtonColors(
            contentColor = Color.White,
            containerColor = colorResource(R.color.Rojo),
            disabledContainerColor = Color.Gray,
            disabledContentColor = Color.Gray
        )
    ) {
        Text(text)
    }
}
fun validateLoginForm(model: AuthViewModel,state: AuthState):Boolean{
    if (state.email.isEmpty()){ model.updateEmailError("Email is empty")
        return false
    }else{model.updateEmailError("")}
    if(!validEmailAddress(state.email)){model.updateEmailError("Not a valid address")
        return false
    }else{model.updateEmailError("")}
    if(state.password.isEmpty()) {model.updatePasswordError("Password is empty")
        return false
    }else{model.updatePasswordError("")}
    if(state.password.length < 6) {model.updatePasswordError("Password is too short")
        return false
    }else{model.updatePasswordError("")}
    return true
}

fun validateRegisterForm(model: AuthViewModel, state: AuthState): Boolean {
    if (state.name.isEmpty()) {
        model.updateNameError("Name is required")
        return false
    } else {
        model.updateNameError("")
    }
    if (state.lastname.isEmpty()) {
        model.updateLastnameError("Last name is required")
        return false
    } else {
        model.updateLastnameError("")
    }
    if (state.idNumber.isEmpty()) {
        model.updateIdError("ID is required")
        return false
    } else {
        model.updateIdError("")
    }
    if (state.email.isEmpty()) {
        model.updateEmailError("Email is empty")
        return false
    } else if (!validEmailAddress(state.email)) {
        model.updateEmailError("Not a valid address")
        return false
    } else {
        model.updateEmailError("")
    }
    if (state.password.isEmpty()) {
        model.updatePasswordError("Password is empty")
        return false
    } else if (state.password.length < 6) {
        model.updatePasswordError("Password is too short")
        return false
    } else {
        model.updatePasswordError("")
    }
    val isLoginValid = validateLoginForm(model, state)

    return isLoginValid
}

fun validEmailAddress(email:String):Boolean{
    val regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$"
    return email.matches(regex.toRegex())
}