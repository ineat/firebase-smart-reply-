package com.ineat.firebase.natural.language

import android.os.Bundle
import android.util.Log
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.smartreply.FirebaseTextMessage
import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestionResult
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    /**
     * Créer un FirebaseTranslator
     * @param sourceLanguage langue dans laquelle votre message sera écrit
     * @param targetLanguage langue dans laquelle votre message sera traduit
     * @return Deferred<FirebaseTranslator> car la fonction peut prendre du temps dans le cas où elle a besoin de télécharger le modèle ML.
     */
    private suspend fun createTranslator(sourceLanguage: Int, targetLanguage: Int): FirebaseTranslator =
        withContext(Dispatchers.IO) {
            val options = FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(sourceLanguage)
                .setTargetLanguage(targetLanguage)
                .build()

            FirebaseNaturalLanguage.getInstance().getTranslator(options).apply {
                downloadModelIfNeeded().await()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        recycler.adapter = MessageRecyclerAdapter(MESSAGES)
        recycler.addItemDecoration(MarginItemDecoration(23.dp))

        // Démarrer
        launch(context = Dispatchers.IO) {
            val frenchEnglishTranslator = createTranslator(
                sourceLanguage = FirebaseTranslateLanguage.FR,
                targetLanguage = FirebaseTranslateLanguage.EN
            )
            val englishFrenchTranslator = createTranslator(
                sourceLanguage = FirebaseTranslateLanguage.EN,
                targetLanguage = FirebaseTranslateLanguage.FR
            )
            val smartReplies = getSmartReplies(frenchEnglishTranslator, englishFrenchTranslator)
            withContext(Dispatchers.Main) {
                displaySmartReplies(smartReplies)
            }
        }
    }

    @UiThread
    private fun displaySmartReplies(smartReplies: List<String>) {
        chips.removeAllViews()
        smartReplies
            .map { msg ->
                Chip(this@MainActivity).apply {
                    text = msg
                    setOnClickListener {
                        chips.removeAllViews()
                        MESSAGES.add(
                            Message(
                                isMe = true,
                                text = msg,
                                createAt = System.currentTimeMillis()
                            )
                        )
                        recycler.adapter?.notifyDataSetChanged()
                    }
                }
            }
            .forEach(chips::addView)
    }


    private suspend fun getSmartReplies(
        frenchEnglishTranslator: FirebaseTranslator,
        englishFrenchTranslator: FirebaseTranslator
    ): List<String> {

        suspend fun translateFrenchToEnglish(msg: String) = frenchEnglishTranslator.translate(msg).await()

        suspend fun translateEnglishToFrench(msg: String) = englishFrenchTranslator.translate(msg).await()

        val conversations = MESSAGES.map {
            val messageTranslated = translateFrenchToEnglish(it.text)
            when (it.isMe) {
                true -> FirebaseTextMessage.createForLocalUser(
                    messageTranslated,
                    it.createAt
                )
                false -> FirebaseTextMessage.createForRemoteUser(
                    messageTranslated,
                    it.createAt,
                    "1"
                )
            }
        }

        val smartReply = FirebaseNaturalLanguage.getInstance().smartReply
        val result = smartReply.suggestReplies(conversations).await()
        Log.d("","")
        return when (result.status) {
            SmartReplySuggestionResult.STATUS_SUCCESS -> result
                .suggestions
                .filter { it.confidence >= 0 }
                .sortedByDescending {
                    Log.d("sortedByDescending", it.text + " " + it.confidence)
                    it.confidence
                }
                .map { translateEnglishToFrench(it.text) }
            SmartReplySuggestionResult.STATUS_NOT_SUPPORTED_LANGUAGE -> {
                // Display exception
                emptyList()
            }
            else -> {
                // No replies
                emptyList()
            }
        }
    }

}
