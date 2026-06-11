package com.example.taller3.view

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.taller3.model.User
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.messaging
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


data class AuthState(
    val email: String = "",
    val password: String = "",
    val emailError: String = "",
    val passwordError: String = "",
    val name: String = "",
    val lastname: String = "",
    val idNumber: String = "",
    val imageUri: Uri? = null,
    val nameError: String = "",
    val lastnameError: String = "",
    val idError: String = "",
    val available: Boolean = false
)

class AuthViewModel: ViewModel(){
    private val _authState = MutableStateFlow<AuthState>(AuthState())
    val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val storage = FirebaseStorage.getInstance()
    val authState = _authState.asStateFlow()

    fun updateEmail(newValue : String){
        _authState.update { it.copy(email=newValue) }
    }
    fun updatePassword(newValue : String){
        _authState.update { it.copy(password =newValue) }
    }
    fun updateEmailError(newValue : String){
        _authState.update { it.copy(emailError=newValue) }
    }
    fun updatePasswordError(newValue : String){
        _authState.update { it.copy(passwordError= newValue) }
    }
    fun updateName(newValue: String) {
        _authState.update { it.copy(name = newValue) }
    }
    fun updateLastname(newValue: String) {
        _authState.update { it.copy(lastname = newValue) }
    }
    fun updateIdNumber(newValue: String) {
        _authState.update { it.copy(idNumber = newValue) }
    }
    fun updateImageUri(newValue: Uri?) {
        _authState.update { it.copy(imageUri = newValue) }
    }
    fun updateNameError(newValue: String) {
        _authState.update { it.copy(nameError = newValue) }
    }
    fun updateLastnameError(newValue: String) {
        _authState.update { it.copy(lastnameError = newValue) }
    }
    fun updateIdError(newValue: String) {
        _authState.update { it.copy(idError = newValue) }
    }
    fun updateAvailable(newValue: Boolean){
        _authState.update {it.copy(available = newValue)}
    }
    fun register(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val state = authState.value
        if (state.email.isEmpty() || state.password.isEmpty()) {
            onError("Email and password cannot be empty")
            return
        }
        auth.createUserWithEmailAndPassword(state.email, state.password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser

                    firebaseUser?.let { userAuth ->
                        val upcrb = UserProfileChangeRequest.Builder()
                        upcrb.setDisplayName("${state.name} ${state.lastname}")
                        userAuth.updateProfile(upcrb.build())

                        val newUser = User(
                            name = state.name,
                            lastname = state.lastname,
                            id = state.idNumber,
                            email = state.email,
                            latitude = 0.0,
                            longitude = 0.0,
                            available = false
                        )

                        if (state.imageUri != null) {
                            val storageRef = storage.getReference("profile_pictures/${userAuth.uid}.jpg")
                            storageRef.putFile(state.imageUri)
                                .addOnSuccessListener {
                                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                                        saveUserToDatabase(newUser, userAuth.uid, uri.toString(), onSuccess, onError)
                                    }
                                }
                                .addOnFailureListener { e ->
                                    onError(e.message ?: "Error uploading image")
                                }
                        }
                    }
                } else {
                    onError(task.exception?.message ?: "Authentication Failed")
                }
            }
    }
    private fun saveUserToDatabase(user: User, uid: String, imageUrl: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val finalUser = user.copy(
            uid = uid,
            profilePictureUrl = imageUrl,
            available = false
        )
        val myRef = database.getReference("users/$uid")
        myRef.setValue(finalUser)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Database Error") }
    }
    fun logOut() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid != null) {
            FirebaseDatabase.getInstance().getReference("users/$uid/fcmToken").removeValue()
                .addOnCompleteListener {
                    FirebaseAuth.getInstance().signOut()
                }
        } else {
            FirebaseAuth.getInstance().signOut()
        }
    }

    fun fetchInitialStatus() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            FirebaseDatabase.getInstance().getReference("users/$uid/available")
                .get()
                .addOnSuccessListener { snapshot ->
                    val available = snapshot.getValue(Boolean::class.java) ?: false
                    updateAvailable(available)
                }
        }
    }
    fun toggleAvailability() {
        val newState = !_authState.value.available
        updateAvailable(newState)
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            FirebaseDatabase.getInstance().getReference("users/$uid/available").setValue(newState)
        }
    }
    fun saveDeviceTokenAutomatically() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        Firebase.messaging.token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                FirebaseDatabase.getInstance().getReference("users/$uid/fcmToken").setValue(token)
            }
        }
    }
}