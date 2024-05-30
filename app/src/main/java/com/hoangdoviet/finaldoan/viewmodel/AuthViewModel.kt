package com.hoangdoviet.hoangfirebase.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.hoangdoviet.finaldoan.model.LoginUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.hoangdoviet.hoangfirebase.util.Resource
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val mFirestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    //_loginStatus: MutableLiveData giữ trạng thái đăng nhập được bọc trong một đối tượng Resource.
    //loginStatus: LiveData công khai để quan sát trạng thái đăng nhập.
    private val _loginStatus: MutableLiveData<Resource<LoginUiState>> = MutableLiveData()
    val loginStatus: LiveData<Resource<LoginUiState>> = _loginStatus

    private val _signupStatus: MutableLiveData<Resource<Pair<String, String>>> = MutableLiveData()
    val signupStatus: LiveData<Resource<Pair<String, String>>> = _signupStatus

    private val _resendPasswordStatus: MutableLiveData<Resource<String>> = MutableLiveData()
    val resendPasswordStatus: LiveData<Resource<String>> = _resendPasswordStatus
    private val _loginUiState = MutableLiveData<LoginUiState>()
    val loginUiState: LiveData<LoginUiState> get() = _loginUiState

    fun setLoginUiState(state: LoginUiState) {
        _loginUiState.value = state
    }

    fun login(email: String , password: String){
        viewModelScope.launch(Dispatchers.IO){
            try {
                _loginStatus.postValue(Resource.loading()) //Đặt trạng thái đăng nhập thành đang tải.
                mAuth.signInWithEmailAndPassword(email, password).await()
                // Get current user's ID and username.
                val currentUserUid = mAuth.currentUser?.uid!!
                val currentUsername = mAuth.currentUser?.displayName!!
                val currentUserRef = mFirestore.collection("User")
                    .document(currentUserUid)
                val currentUser: DocumentSnapshot = currentUserRef.get().await()
                val currentUserInfoExist = currentUser.exists()
                val loginUiState = LoginUiState(
                    username = currentUsername,
                    email = email,
                )
                _loginStatus.postValue(Resource.success(loginUiState))
            }catch (e: FirebaseAuthInvalidCredentialsException){
                _loginStatus.postValue(Resource.error("Email hoặc mật khẩu không hợp lệ."))
            }catch (e: FirebaseAuthInvalidUserException) {
                _loginStatus.postValue(Resource.error("Người dùng không tồn tại."))
            } catch (e: FirebaseNetworkException) {
                _loginStatus.postValue(Resource.error("Lỗi mạng."))
            } catch (e: Exception) {
                _loginStatus.postValue(Resource.error("Tài khoản không tồn tại."))
            }
        }
    }
    fun signup(username: String, email: String, password: String){
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _signupStatus.postValue(Resource.loading())

                // Register the new user with the provided email and password.
                mAuth.createUserWithEmailAndPassword(email, password).await()

                // Update the user's display name to the provided username.
                val currentUser = mAuth.currentUser!!
                val profileBuilder = UserProfileChangeRequest.Builder()
                val currentUserProfile = profileBuilder.setDisplayName(username).build()
                currentUser.updateProfile(currentUserProfile).await()
                val userFireBase = mFirestore.collection("User").document(currentUser.uid)
                userFireBase.set(LoginUiState(username, email))

                // Post the success status along with the username and email of the newly registered user.
                val signupState = Pair(username, email)
                _signupStatus.postValue(Resource.success(signupState))
            } catch (error: FirebaseAuthUserCollisionException) {
                // Post an error status if the provided email already exists in the system.
                _signupStatus.postValue(Resource.error("Email already exists."))
            }
        }
    }
    fun resendPassword(email: String){
        viewModelScope.launch(Dispatchers.IO) {
            _resendPasswordStatus.postValue(Resource.loading())
            mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    _resendPasswordStatus.postValue(Resource.success("Resend password send success."))
                }
                .addOnFailureListener {
                    _resendPasswordStatus.postValue(Resource.error("Resend password failed."))
                }
        }
    }


}
