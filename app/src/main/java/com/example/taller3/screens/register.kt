package com.example.taller3.screens

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.taller3.view.AuthViewModel
import com.example.taller3.R
import com.example.taller3.navigation.AppScreens
import com.example.taller3.util.ButtonShared
import com.example.taller3.util.validateRegisterForm


@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Register(controller: NavController, viewModel: AuthViewModel = viewModel()) {

    val state by viewModel.authState.collectAsState()
    val context = LocalContext.current
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        viewModel.updateImageUri(uri)
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Register", color = Color.White,fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = { controller.navigate(AppScreens.authentication.name) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.RojoOscuro)
                )
            )
        }
    ) { values ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(values),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(top = 16.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(text = "Profile Picture", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {

                        if (state.imageUri != null) {

                            AsyncImage(
                                model = state.imageUri,
                                contentDescription = "Profile Image",
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(20.dp)),
                                contentScale = ContentScale.Crop
                            )
                            ButtonShared("Change Image")  { imagePicker.launch("image/*") }

                        } else {

                            ButtonShared("Select Image") {
                                imagePicker.launch("image/*")
                            }
                        }
                    }

                    Text(text = "First Name", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = state.name,
                        onValueChange = { viewModel.updateName(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Enter your first name") },
                        isError = state.nameError.isNotEmpty(),
                        supportingText = {
                            if (state.nameError.isNotEmpty())
                                Text(text = state.nameError, color = colorResource(R.color.RojoOscuro))
                        }
                    )

                    Text(text = "Last Name", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = state.lastname,
                        onValueChange = { viewModel.updateLastname(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Enter your last name") },
                        isError = state.lastnameError.isNotEmpty(),
                        supportingText = {
                            if (state.lastnameError.isNotEmpty())
                                Text(text = state.lastnameError, color = colorResource(R.color.RojoOscuro))
                        }
                    )

                    Text(text = "ID Number", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = state.idNumber,
                        onValueChange = { viewModel.updateIdNumber(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Enter your ID number") },
                        isError = state.idError.isNotEmpty(),
                        supportingText = {
                            if (state.idError.isNotEmpty())
                                Text(text = state.idError, color = colorResource(R.color.RojoOscuro))
                        }
                    )

                    Text(text = "Email", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = { viewModel.updateEmail(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Enter your email") },
                        isError = state.emailError.isNotEmpty(),
                        supportingText = {
                            if (state.emailError.isNotEmpty()) {
                                Text(text = state.emailError, color = colorResource(R.color.RojoOscuro))
                            }
                        }
                    )

                    Text(text = "Password", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = { viewModel.updatePassword(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Enter your password") },
                        visualTransformation = PasswordVisualTransformation(),
                        isError = state.passwordError.isNotEmpty(),
                        supportingText = {
                            if (state.passwordError.isNotEmpty()) {
                                Text(text = state.passwordError, color = colorResource(R.color.RojoOscuro))
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    ButtonShared(text = "Register") {
                        var isValid = validateRegisterForm(viewModel, state)
                        if (state.imageUri == null) {
                            Toast.makeText(context, "Please select a profile picture", Toast.LENGTH_SHORT).show()
                            isValid= false
                        }
                        if (isValid) {
                            viewModel.register(
                                onSuccess = {
                                    controller.navigate(AppScreens.home.name)
                                },
                                onError = { errorMessage ->
                                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    }
                }
            }
        }

    }

}