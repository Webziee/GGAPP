package com.guesthouse.ggapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import androidx.biometric.BiometricManager
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes

class SignUp : AppCompatActivity() {

    // UI elements
    private lateinit var signinlayout: LinearLayout
    private lateinit var signintext: TextView
    private lateinit var signinemail: TextInputEditText
    private lateinit var signinpassword: TextInputEditText
    private lateinit var signin_button: Button

    private lateinit var signuplayout: LinearLayout
    private lateinit var signuptext: TextView
    private lateinit var signupemail: TextInputEditText
    private lateinit var signuppassword: TextInputEditText
    private lateinit var signupconfirmpassword: TextInputEditText
    private lateinit var signup_buttom: Button
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    private lateinit var progressBar: ProgressBar
    private lateinit var googleSignInButton: com.google.android.gms.common.SignInButton

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    // Google Sign-In
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hide the status bar and make the activity fullscreen
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        actionBar?.hide() // Hide the action bar if it exists

        setContentView(R.layout.activity_sign_up)

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Initialize UI
        initializeViews()

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Update this to match your Web Client ID
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            progressBar.visibility = View.GONE
            val data = result.data
            if (result.resultCode == RESULT_OK && data != null) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                handleSignInResult(task)
            } else {
                Log.w("GoogleSignIn", "Sign-in canceled or no data returned.")
                Toast.makeText(this, "Sign-in was canceled. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }



        // Set up button listeners
        signup_buttom.setOnClickListener { signUpUser() }
        signin_button.setOnClickListener { signInUser() }
        googleSignInButton.setOnClickListener { signInWithGoogle() }

        // Switch between Sign In and Sign Up layouts
        signuptext.setOnClickListener { switchToSignUp() }
        signintext.setOnClickListener { switchToSignIn() }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d("Authentication", "User is already signed in: ${currentUser.email}")
            setupBiometricPrompt()
        } else {
            Log.e("Authentication", "No user signed in")
        }
    }

    private fun initializeViews() {
        signinlayout = findViewById(R.id.SignInLayout)
        signintext = findViewById(R.id.signInText)
        signinemail = findViewById(R.id.signInEmail)
        signinpassword = findViewById(R.id.signInPassword)
        signin_button = findViewById(R.id.signInBtn)

        signuplayout = findViewById(R.id.signUpLayout)
        signuptext = findViewById(R.id.signUpText)
        signupemail = findViewById(R.id.signUpEmail)
        signuppassword = findViewById(R.id.signUpPassword)
        signupconfirmpassword = findViewById(R.id.signUpConfirmPassword)
        signup_buttom = findViewById(R.id.signUpBtn)

        googleSignInButton = findViewById(R.id.googleSignInButton)
        progressBar = findViewById(R.id.progressbar)
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        progressBar.visibility = View.VISIBLE
        googleSignInLauncher.launch(signInIntent)
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            if (account != null) {
                firebaseAuthWithGoogle(account.idToken!!)
            } else {
                Toast.makeText(this, "Sign-in failed: Account not found.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            Log.e("GoogleSignIn", "API Exception: ${e.statusCode}, ${e.localizedMessage}")
            when (e.statusCode) {
                GoogleSignInStatusCodes.SIGN_IN_CANCELLED -> {
                    Toast.makeText(this, "Sign-in canceled.", Toast.LENGTH_SHORT).show()
                }
                GoogleSignInStatusCodes.NETWORK_ERROR -> {
                    Toast.makeText(this, "Network error. Please try again.", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(this, "Sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun firebaseAuthWithGoogle(idToken: String) {
        progressBar.visibility = View.VISIBLE
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                progressBar.visibility = View.GONE
                if (task.isSuccessful) {
                    val userEmail = auth.currentUser?.email
                    Log.d("GoogleSignIn", "User signed in with email: $userEmail")
                    navigateToMainPage()
                } else {
                    Log.e("FirebaseAuth", "Sign-in failed: ${task.exception?.message}")
                    showToast("Sign-in failed: ${task.exception?.message}")
                }
            }
    }

    private fun signUpUser() {
        val email = signupemail.text.toString().trim()
        val password = signuppassword.text.toString().trim()
        val confirmPassword = signupconfirmpassword.text.toString().trim()

        if (email.isNotEmpty() && password.isNotEmpty() && password == confirmPassword) {
            progressBar.visibility = View.VISIBLE
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    progressBar.visibility = View.GONE
                    if (task.isSuccessful) {
                        showToast("Sign-up successful.")
                        navigateToMainPage()
                    } else {
                        showToast("Sign-up failed: ${task.exception?.message}")
                    }
                }
        } else {
            showToast("Please fill all fields correctly.")
        }
    }

    private fun signInUser() {
        val email = signinemail.text.toString().trim()
        val password = signinpassword.text.toString().trim()

        if (email.isNotEmpty() && password.isNotEmpty()) {
            progressBar.visibility = View.VISIBLE
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    progressBar.visibility = View.GONE
                    if (task.isSuccessful) {
                        val userEmail = auth.currentUser?.email
                        Log.d("SignIn", "User signed in with email: $userEmail")
                        navigateToMainPage()
                    } else {
                        showToast("Sign-in failed: ${task.exception?.message}")
                    }
                }
        } else {
            showToast("Please fill all fields.")
        }
    }

    private fun navigateToMainPage() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun switchToSignUp() {
        signuplayout.visibility = View.VISIBLE
        signinlayout.visibility = View.GONE
        signin_button.visibility = View.GONE
        signup_buttom.visibility = View.VISIBLE
        signuptext.visibility = View.GONE
        signintext.visibility = View.VISIBLE
    }

    private fun switchToSignIn() {
        signuplayout.visibility = View.GONE
        signinlayout.visibility = View.VISIBLE
        signin_button.visibility = View.VISIBLE
        signup_buttom.visibility = View.GONE
        signuptext.visibility = View.VISIBLE
        signintext.visibility = View.GONE
    }

    private fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(this)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Toast.makeText(this, "No biometric hardware available.", Toast.LENGTH_SHORT).show()
                false
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Toast.makeText(this, "No biometrics enrolled.", Toast.LENGTH_SHORT).show()
                false
            }
            else -> false
        }
    }

    private fun setupBiometricPrompt() {
        if (!isBiometricAvailable()) return

        val executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                Log.d("BiometricAuth", "Authentication successful!")
                navigateToMainPage()
            }

            override fun onAuthenticationFailed() {
                Toast.makeText(this@SignUp, "Authentication failed.", Toast.LENGTH_SHORT).show()
            }
        })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Login")
            .setSubtitle("Log in using your biometric credentials")
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
