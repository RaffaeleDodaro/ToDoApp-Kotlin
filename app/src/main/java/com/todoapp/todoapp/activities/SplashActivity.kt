package com.todoapp.todoapp.activities

import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.TextView
import com.todoapp.todoapp.R
import com.todoapp.todoapp.firebase.FirestoreClass

class SplashActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        //nascondo la status bar
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        //modifico il font dentro la splashscreen
        val tf:Typeface = Typeface.createFromAsset(assets,"Bulletto Killa.ttf")
        findViewById<TextView>(R.id.tv_app_name).typeface=tf
        findViewById<TextView>(R.id.tv_motivational_phrases).text = chooseMotivationalPhrase()


        //eseguo la splashscreen per 2 sec
        Handler(Looper.getMainLooper()).postDelayed(
            {
                val currentUserID= FirestoreClass().getCurrentUserId()
                if(currentUserID.isNotEmpty()) // se nel dispositivo c'e' gia' un account dell'utente, fa il login auto
                    startActivity(Intent(this, MainActivity::class.java))
                else //altrimenti sign in o signup
                    startActivity(Intent(this, IntroActivity::class.java))
                finish() //Call this when your activity is done and should be closed.
            },2500)
    }

    private fun chooseMotivationalPhrase(): String {
        val phrases=ArrayList<String>()
        phrases.add("Per ogni impresa di successo, c’è qualcuno, che in passato, ha preso una decisione coraggiosa.\nPeter Drucker.")
        phrases.add("Fai quello che puoi, con quello che hai, nel posto in cui sei.\nTheodore Roosevelt.")
        phrases.add("Tutte le cose sono difficili prima di diventare facili.\nTheodore Roosevelt.")
        phrases.add("Riunirsi insieme significa iniziare; rimanere insieme significa progredire; lavorare insieme significa avere successo.\nHenry Ford.")
        phrases.add("L’immaginazione è più importante della conoscenza.\nAlbert Einstein")
        phrases.add("Se riesci a fare le cose bene, cerca di farle meglio. Sii audace, sii il primo, sii differente, sii giusto.\nAnita Roddick.")
        phrases.add("Il cambiamento è la legge della vita. Quelli che guardano solo al passato o al presente, sicuramente perderanno il futuro.\nJ.F. Kennedy.")
        phrases.add("Ci sono due scelte fondamentali da fare nella vita: accettare le condizioni preesistenti o accettare la responsabilità di cambiarle.\nDenis Waitley.")
        phrases.add("Tra vent’anni non sarete delusi delle cose che avete fatto ma da quelle che non avete fatto. Allora levate l’ancora, abbandonate i porti sicuri, " +
                    "catturate il vento nelle vostre vele. Esplorate. Sognate. Scoprite.\n Mark Twain.")
        phrases.add("C’è sempre una via per farlo in modo migliore. Trovala.\nThomas Edison.")
        phrases.add("Non potrai mai raggiungere un reale successo a meno che tu non ami ciò che stai facendo.\nDale Carnegie.")

        return phrases.random()

    }
}