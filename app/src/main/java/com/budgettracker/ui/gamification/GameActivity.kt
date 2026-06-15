package com.budgettracker.ui.gamification

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.budgettracker.data.database.AppDatabase
import com.budgettracker.databinding.ActivityGameBinding
import com.budgettracker.util.GamificationManager
import com.budgettracker.util.SessionManager
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGameBinding
    private lateinit var db: AppDatabase
    private lateinit var sessionManager: SessionManager

    private var score = 0
    private var lives = 3
    private var currentAnswer = false
    private var timer: CountDownTimer? = null
    private var questionCount = 0
    private val maxQuestions = 10

    data class BudgetQuestion(val text: String, val answer: Boolean)

    private val questions = listOf(
        BudgetQuestion("You spent R500 on food. Your max food budget is R600. Good spending?", true),
        BudgetQuestion("You spent R1200 on entertainment but budgeted R400. Is this wise?", false),
        BudgetQuestion("Saving 20% of income monthly is a good habit?", true),
        BudgetQuestion("Buying daily coffee for R60/day adds up to over R1000/month?", true),
        BudgetQuestion("It's OK to go over your max budget goal every month?", false),
        BudgetQuestion("Tracking every expense helps you stay on budget?", true),
        BudgetQuestion("Paying only the minimum on credit cards saves money long-term?", false),
        BudgetQuestion("Having an emergency fund is important?", true),
        BudgetQuestion("Impulse buying is a good budgeting strategy?", false),
        BudgetQuestion("Setting spending categories helps control expenses?", true),
        BudgetQuestion("Spending more than you earn leads to debt?", true),
        BudgetQuestion("You should review your budget goals monthly?", true),
        BudgetQuestion("It's fine to never check your bank balance?", false),
        BudgetQuestion("Comparing prices before buying saves money?", true),
        BudgetQuestion("Eating out every day is usually cheaper than cooking at home?", false)
    ).shuffled()

    private var currentQuestionIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)
        sessionManager = SessionManager(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Budget Quiz Game 🎮"

        binding.btnStart.setOnClickListener {
            binding.cardIntro.visibility = View.GONE
            binding.cardGame.visibility = View.VISIBLE
            startGame()
        }

        binding.btnTrue.setOnClickListener { checkAnswer(true) }
        binding.btnFalse.setOnClickListener { checkAnswer(false) }
        binding.btnPlayAgain.setOnClickListener {
            binding.cardResult.visibility = View.GONE
            binding.cardIntro.visibility = View.VISIBLE
            resetGame()
        }
    }

    private fun resetGame() {
        score = 0
        lives = 3
        questionCount = 0
        currentQuestionIndex = 0
    }

    private fun startGame() {
        showQuestion()
    }

    private fun showQuestion() {
        if (questionCount >= maxQuestions || currentQuestionIndex >= questions.size) {
            endGame()
            return
        }

        val q = questions[currentQuestionIndex]
        currentAnswer = q.answer

        binding.tvQuestion.text = q.text
        binding.tvScore.text = "Score: $score"
        binding.tvLives.text = "❤️".repeat(lives)
        binding.tvProgress.text = "${questionCount + 1} / $maxQuestions"
        binding.progressQuiz.progress = ((questionCount.toFloat() / maxQuestions) * 100).toInt()

        binding.btnTrue.isEnabled = true
        binding.btnFalse.isEnabled = true
        binding.tvFeedback.visibility = View.GONE

        startTimer()
    }

    private fun startTimer() {
        timer?.cancel()
        binding.progressTimer.max = 100
        timer = object : CountDownTimer(10000, 100) {
            override fun onTick(remaining: Long) {
                binding.progressTimer.progress = ((remaining / 10000f) * 100).toInt()
            }
            override fun onFinish() {
                showFeedback(false, "⏰ Time's up!")
                loseLife()
            }
        }.start()
    }

    private fun checkAnswer(answer: Boolean) {
        timer?.cancel()
        binding.btnTrue.isEnabled = false
        binding.btnFalse.isEnabled = false

        if (answer == currentAnswer) {
            score += 10
            showFeedback(true, "✅ Correct! +10 points")
        } else {
            showFeedback(false, "❌ Wrong! The answer was ${if (currentAnswer) "TRUE" else "FALSE"}")
            loseLife()
        }
    }

    private fun showFeedback(correct: Boolean, msg: String) {
        binding.tvFeedback.text = msg
        binding.tvFeedback.setTextColor(
            if (correct) android.graphics.Color.parseColor("#4CAF50")
            else android.graphics.Color.parseColor("#F44336")
        )
        binding.tvFeedback.visibility = View.VISIBLE
        currentQuestionIndex++
        questionCount++

        binding.root.postDelayed({
            if (lives > 0) showQuestion()
            else endGame()
        }, 1500)
    }

    private fun loseLife() {
        lives--
        if (lives <= 0) {
            timer?.cancel()
            binding.root.postDelayed({ endGame() }, 1500)
        }
    }

    private fun endGame() {
        timer?.cancel()
        binding.cardGame.visibility = View.GONE
        binding.cardResult.visibility = View.VISIBLE

        val percentage = if (maxQuestions > 0) (score.toFloat() / (maxQuestions * 10)) * 100 else 0f
        val resultEmoji = when {
            percentage >= 80 -> "🏆"
            percentage >= 60 -> "🎯"
            percentage >= 40 -> "💪"
            else -> "📚"
        }
        val resultMsg = when {
            percentage >= 80 -> "Budget Master! You really know your finances!"
            percentage >= 60 -> "Great job! Keep improving your budget skills!"
            percentage >= 40 -> "Good effort! Practice makes perfect!"
            else -> "Keep learning about budgeting — you'll get better!"
        }

        binding.tvResultEmoji.text = resultEmoji
        binding.tvFinalScore.text = "Score: $score / ${maxQuestions * 10}"
        binding.tvResultMessage.text = resultMsg

        if (percentage >= 70) {
            val userId = sessionManager.getUserId()
            lifecycleScope.launch {
                GamificationManager.awardBadgeDirectly(userId, "GAME_WINNER", db.badgeDao(), db.userDao())
                runOnUiThread {
                    Toast.makeText(this@GameActivity, "🏅 Game Champion badge earned!", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}
