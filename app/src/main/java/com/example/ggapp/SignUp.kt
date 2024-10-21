package com.example.ggapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.goergesgraceapp.ExplorePage
import com.google.android.material.textfield.TextInputEditText

class SignUp : AppCompatActivity() {

    // signIn
    private lateinit var signinlayout: LinearLayout
    private lateinit var signintext: TextView
    private lateinit var signinemail: TextInputEditText
    private lateinit var signinpassword: TextInputEditText
    private lateinit var signin_button: Button

    // signUp
    private lateinit var signuplayout: LinearLayout
    private lateinit var signuptext: TextView
    private lateinit var signupemail: TextInputEditText
    private lateinit var signuppassword: TextInputEditText
    private lateinit var signupconfirmpassword: TextInputEditText
    private lateinit var signup_buttom: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Force light mode for all API levels
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Set the content to fullscreen, hiding the status bar
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                )

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        // Enable edge-to-edge rendering
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Hide the status bar and navigation bar
        hideSystemUI()

        // signIn
        signinlayout = findViewById(R.id.SignInLayout)
        signintext = findViewById(R.id.signInText)
        signinemail = findViewById(R.id.signInEmail)
        signinpassword = findViewById(R.id.signInPassword)
        signin_button = findViewById(R.id.signInBtn)

        // signUp
        signuplayout = findViewById(R.id.signUpLayout)
        signuptext = findViewById(R.id.signUpText)
        signupemail = findViewById(R.id.signUpEmail)
        signuppassword = findViewById(R.id.signUpPassword)
        signupconfirmpassword = findViewById(R.id.signUpConfirmPassword)
        signup_buttom = findViewById(R.id.signUpBtn)

        // Handle switching between Log In and Sign Up layouts
        signuptext.setOnClickListener {
            // Switch to Sign Up layout
            signuplayout.visibility = View.VISIBLE
            signinlayout.visibility = View.GONE
            signin_button.visibility = View.GONE
            signup_buttom.visibility = View.VISIBLE
            signuptext.visibility = View.GONE
            signintext.visibility = View.VISIBLE
        }

        signintext.setOnClickListener {
            // Switch to Sign In layout
            signuplayout.visibility = View.GONE
            signinlayout.visibility = View.VISIBLE
            signin_button.visibility = View.VISIBLE
            signup_buttom.visibility = View.GONE
            signuptext.visibility = View.VISIBLE
            signintext.visibility = View.GONE
        }

        signup_buttom.setOnClickListener {
            val Openexplorepage = Intent(this, MainActivity::class.java)
            startActivity(Openexplorepage)
        }

        signin_button.setOnClickListener {
            val Openexplorepage = Intent(this, MainActivity::class.java)
            startActivity(Openexplorepage)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBarsInsets.left, systemBarsInsets.top, systemBarsInsets.right, systemBarsInsets.bottom)
            insets
        }
    }

    private fun hideSystemUI() {
        window.decorView.apply {
            systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }
}

