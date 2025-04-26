package vcmsa.projects.budgetbuddy

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import vcmsa.projects.budgetbuddy.databinding.FragmentLoginBinding

class Login : AppCompatActivity() {
    private lateinit var binding: FragmentLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = FragmentLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()
        setContentView(R.layout.fragment_login)

        binding.tvGoToRegister.setOnClickListener {
            val i = Intent(this, Register::class.java).apply {  }
            startActivity(i)
        }

        binding.btnLogin.setOnClickListener {
            val i = Intent(this, Hom::class.java).apply {  }
            startActivity(i)
        }
    }
}