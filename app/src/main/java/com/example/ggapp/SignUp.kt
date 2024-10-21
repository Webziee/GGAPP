package com.example.ggapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class SignUp : AppCompatActivity() {

    // SignIn / SignUp UI elements
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
    private lateinit var redirectToSignInBtn: Button // NEW: Button to redirect to sign in

    private lateinit var progressBar: ProgressBar
    private lateinit var googleSignInButton: com.google.android.gms.common.SignInButton

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    // Google Sign-In
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Check if user is already signed in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            navigateToMainPage() // User is already signed in, navigate to the main page
        }

        // Initialize UI
        initializeViews()

        // Initialize Google Sign-In options
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Ensure this matches your Firebase project
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleSignInResult(task)
            } else {
                Toast.makeText(this, "Google Sign-In canceled", Toast.LENGTH_SHORT).show()
            }
        }

        // Sign-In and Sign-Up button listeners
        signup_buttom.setOnClickListener { signUpUser() }
        signin_button.setOnClickListener { signInUser() }
        googleSignInButton.setOnClickListener { signInWithGoogle() }

        // NEW: Redirect button listener
        redirectToSignInBtn.setOnClickListener { switchToSignIn() } // Switch back to sign-in page

        // Handle switching between Log In and Sign Up layouts
        signuptext.setOnClickListener { switchToSignUp() }
        signintext.setOnClickListener { switchToSignIn() }
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
        redirectToSignInBtn = findViewById(R.id.redirectToSignInBtn) // Initialize the new button

        googleSignInButton = findViewById(R.id.googleSignInButton)
        progressBar = findViewById(R.id.progressbar)
    }

    // Google Sign-In Functionality
    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        progressBar.visibility = View.VISIBLE
        googleSignInLauncher.launch(signInIntent)
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        progressBar.visibility = View.GONE
        try {
            val account = completedTask.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account?.idToken)
        } catch (e: ApiException) {
            Toast.makeText(this, "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        if (idToken != null) {
            progressBar.visibility = View.VISIBLE
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    progressBar.visibility = View.GONE
                    if (task.isSuccessful) {
                        navigateToMainPage()
                    } else {
                        showToast("Google authentication failed: ${task.exception?.message}")
                    }
                }
        }
    }

    // Sign Up with Email and Password
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
                        showToast("Registration successful!")
                        navigateToMainPage()
                    } else {
                        showToast("Registration failed: ${task.exception?.message}")
                    }
                }
        } else {
            showToast("Please fill in all fields correctly")
        }
    }

    // Sign In with Email and Password
    private fun signInUser() {
        val email = signinemail.text.toString().trim()
        val password = signinpassword.text.toString().trim()

        if (email.isNotEmpty() && password.isNotEmpty()) {
            progressBar.visibility = View.VISIBLE
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    progressBar.visibility = View.GONE
                    if (task.isSuccessful) {
                        navigateToMainPage()
                    } else {
                        showToast("Sign-in failed: ${task.exception?.message}")
                    }
                }
        } else {
            showToast("Please fill in both fields")
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
}
