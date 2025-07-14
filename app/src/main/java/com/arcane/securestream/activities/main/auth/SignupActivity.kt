package com.arcane.securestream.activities.main.auth

import android.content.Intent
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
import com.arcane.securestream.databinding.ActivitySignupBinding
import com.arcane.securestream.R
import com.arcane.securestream.activities.main.MainMobileActivity

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Set click listeners
        binding.btnSignup.setOnClickListener {
            registerUser()
        }

        binding.btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }

        binding.tvLogin.setOnClickListener {
            finish() // Return to LoginActivity
        }
    }

    private fun registerUser() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

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

        if (password != confirmPassword) {
            binding.tilConfirmPassword.error = getString(R.string.password_mismatch)
            return
        } else {
            binding.tilConfirmPassword.error = null
        }

        // Show progress
        showProgressBar(true)

        // Create user with email and password
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                showProgressBar(false)
                if (task.isSuccessful) {
                    // Sign up successful - Log out the user immediately
                    FirebaseAuth.getInstance().signOut()

                    Toast.makeText(
                        baseContext,
                        "Account created successfully! Please log in.",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Redirect to LoginActivity
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    // If sign up fails, display a message to the user.
                    Toast.makeText(
                        baseContext,
                        getString(R.string.signup_error),
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
                        getString(R.string.signup_error),
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

// Result returned from launching the Intent from GoogleSignIn