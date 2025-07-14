package com.arcane.securestream.activities.main.auth

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.arcane.securestream.R
import com.arcane.securestream.activities.main.MainMobileActivity
import com.arcane.securestream.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        private const val RC_SIGN_IN = 9001
        private const val PREF_NAME = "LoginPrefs"
        private const val KEY_REMEMBER = "rememberMe"
        private const val KEY_EMAIL = "email"
        private const val KEY_PASSWORD = "password"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE)

        // Check if user is already logged in
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainMobileActivity::class.java))
            finish()
        }

        // Check if remember me was checked before
        if (sharedPreferences.getBoolean(KEY_REMEMBER, false)) {
            binding.etEmail.setText(sharedPreferences.getString(KEY_EMAIL, ""))
            binding.etPassword.setText(sharedPreferences.getString(KEY_PASSWORD, ""))
            binding.cbRememberMe.isChecked = true
        }

        // Set click listeners
        binding.btnLogin.setOnClickListener {
            loginUser()
        }

        binding.btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }

        binding.tvSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

    }

    private fun loginUser() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // Validate inputs
        if (!isValidEmail(email)) {
            binding.tilEmail.error = getString(R.string.email_invalid)
            return
        } else {
            binding.tilEmail.error = null
        }

        if (password.length < 6) {
            binding.tilPassword.error = getString(R.string.password_short)
            return
        } else {
            binding.tilPassword.error = null
        }

        // Show progress
        showProgressBar(true)

        // Authenticate with Firebase
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                showProgressBar(false)
                if (task.isSuccessful) {
                    // Save credentials if "Remember me" is checked
                    if (binding.cbRememberMe.isChecked) {
                        val editor = sharedPreferences.edit()
                        editor.putBoolean(KEY_REMEMBER, true)
                        editor.putString(KEY_EMAIL, email)
                        editor.putString(KEY_PASSWORD, password)
                        editor.apply()
                    } else {
                        // Clear saved credentials
                        val editor = sharedPreferences.edit()
                        editor.clear()
                        editor.apply()
                    }

                    // Sign in success, update UI with the signed-in user's information
                    startActivity(Intent(this, MainMobileActivity::class.java))
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(
                        baseContext,
                        getString(R.string.login_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed
                Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        showProgressBar(true)
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                showProgressBar(false)
                if (task.isSuccessful) {
                    // Sign in success
                    startActivity(Intent(this, MainMobileActivity::class.java))
                    finish()
                } else {
                    // Sign in fails
                    Toast.makeText(
                        baseContext,
                        getString(R.string.login_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun showProgressBar(show: Boolean) {
        binding.progressContainer.visibility = if (show) View.VISIBLE else View.GONE
    }
}