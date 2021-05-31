package systems.brn.gpsgame1

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.net.URL


class LoginActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private val sharedPrefFile = "MyConfig"
    private val url: String = "http://10.10.10.52/checklogin.php"
    private var client = OkHttpClient.Builder().connectionSpecs(listOf(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS)).build()
    private val request = Request.Builder().url(URL(url)).build()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        sharedPreferences = this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val mBtnLogin = this.findViewById(R.id.signin) as Button
        mBtnLogin.setOnClickListener {
            val email = findViewById<EditText>(R.id.email)
            val password = findViewById<EditText>(R.id.password)
            with(sharedPreferences.edit()) {
                putString("email", email.text.toString())
                putString("password", password.text.toString())
                putInt("configured", 1)
                apply()
            }
            val response: Response = client.newCall(request).execute()
            val stringresponse: String? = response.body?.string()
            if (stringresponse == "OK") {
                Toast.makeText(this, "Permission granted, run the main app", Toast.LENGTH_LONG).show()
                val intent = Intent(this, MapsActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Login failed", Toast.LENGTH_LONG).show()
            }
        }
    }
}