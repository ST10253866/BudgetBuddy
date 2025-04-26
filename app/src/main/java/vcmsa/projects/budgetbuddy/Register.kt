package vcmsa.projects.budgetbuddy

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import vcmsa.projects.budgetbuddy.databinding.FragmentRegisterBinding

class Register : AppCompatActivity() {
    private lateinit var binding: FragmentRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = FragmentRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            val i = Intent(this, Login::class.java).apply {  }
            startActivity(i)//maybe dont make this redirect idk...maybe just make it make acc
        }

        binding.tvGoToLogin.setOnClickListener {
            val i = Intent(this, Login::class.java).apply {  }
            startActivity(i)
        }
    }
}