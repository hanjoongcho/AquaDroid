package io.github.z3r0c00l_2k.aquadroid

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.android.material.snackbar.Snackbar
import io.github.z3r0c00l_2k.aquadroid.fragments.BottomSheetFragment
import io.github.z3r0c00l_2k.aquadroid.helpers.AlarmHelper
import io.github.z3r0c00l_2k.aquadroid.helpers.SqliteHelper
import io.github.z3r0c00l_2k.aquadroid.utils.AppUtils
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private var totalIntake: Int = 0
    private var inTook: Int = 0
    private lateinit var sharedPref: SharedPreferences
    private lateinit var sqliteHelper: SqliteHelper
    private lateinit var dateNow: String
    private var notificStatus: Boolean = false
    private var selectedOption: Int? = null
    private var selectedOptionName: String? = null
    private var snackbar: Snackbar? = null
    private var doubleBackToExitPressedOnce = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPref = getSharedPreferences(AppUtils.USERS_SHARED_PREF, AppUtils.PRIVATE_MODE)
        sqliteHelper = SqliteHelper(this)

        totalIntake = sharedPref.getInt(AppUtils.TOTAL_INTAKE, 0)

        if (sharedPref.getBoolean(AppUtils.FIRST_RUN_KEY, true)) {
            startActivity(Intent(this, WalkThroughActivity::class.java))
            finish()
        } else if (totalIntake <= 0) {
            startActivity(Intent(this, InitUserInfoActivity::class.java))
            finish()
        }

        dateNow = AppUtils.getCurrentDate()!!

    }

    override fun onStart() {
        super.onStart()

        val outValue = TypedValue()
        applicationContext.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)

        notificStatus = sharedPref.getBoolean(AppUtils.NOTIFICATION_STATUS_KEY, true)
        val alarm = AlarmHelper()
        if (!alarm.checkAlarm(this) && notificStatus) {
            btnNotific.setImageDrawable(getDrawable(R.drawable.ic_bell))
            alarm.setAlarm(this, sharedPref.getInt(AppUtils.NOTIFICATION_FREQUENCY_KEY, 30).toLong())
        }

        if (notificStatus) {
            btnNotific.setImageDrawable(getDrawable(R.drawable.ic_bell))
        } else {
            btnNotific.setImageDrawable(getDrawable(R.drawable.ic_bell_disabled))
        }

        sqliteHelper.addAll(dateNow, 0, totalIntake)

        inTook = sqliteHelper.getIntook(dateNow)

        setWaterLevel(inTook, totalIntake)

        btnMenu.setOnLongClickListener {
            sharedPref.edit().apply {
                putBoolean(AppUtils.FIRST_RUN_KEY, true)
                apply()
                ActivityCompat.finishAffinity(this@MainActivity)
            }
            true
        }

        btnMenu.setOnClickListener {
            val bottomSheetFragment = BottomSheetFragment(this)
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
        }

        fabAdd.setOnClickListener {
            if (selectedOption != null && selectedOptionName != null) {
                if ((inTook * 100 / totalIntake) <= 140) {
                    if (sqliteHelper.addIntook(dateNow, selectedOption!!) > 0) {
                        inTook += selectedOption!!
                        setWaterLevel(inTook, totalIntake)

                        Snackbar.make(it, "Your water intake was saved...!!", Snackbar.LENGTH_SHORT).show()

                    }
                } else {
                    Snackbar.make(it, "You already achieved the goal", Snackbar.LENGTH_SHORT).show()
                }
                selectedOption = null
                selectedOptionName = null
                opAqua.background = getDrawable(outValue.resourceId)
                opCoffee.background = getDrawable(outValue.resourceId)
                opTea.background = getDrawable(outValue.resourceId)
                opSoftDrink.background = getDrawable(outValue.resourceId)
                opJuice.background = getDrawable(outValue.resourceId)
                opMilk.background = getDrawable(outValue.resourceId)
            } else {
                YoYo.with(Techniques.Shake)
                    .duration(700)
                    .playOn(cardView)
                Snackbar.make(it, "Please select an option", Snackbar.LENGTH_SHORT).show()
            }
        }

        btnNotific.setOnClickListener {
            notificStatus = !notificStatus
            sharedPref.edit().putBoolean(AppUtils.NOTIFICATION_STATUS_KEY, notificStatus).apply()
            if (notificStatus) {
                btnNotific.setImageDrawable(getDrawable(R.drawable.ic_bell))
                Snackbar.make(it, "Notification Enabled..", Snackbar.LENGTH_SHORT).show()
                alarm.setAlarm(this, sharedPref.getInt(AppUtils.NOTIFICATION_FREQUENCY_KEY, 30).toLong())
            } else {
                btnNotific.setImageDrawable(getDrawable(R.drawable.ic_bell_disabled))
                Snackbar.make(it, "Notification Disabled..", Snackbar.LENGTH_SHORT).show()
                alarm.cancelAlarm(this)
            }
        }

        btnStats.setOnClickListener {
            startActivity(Intent(this, StatsActivity::class.java))
        }


        opAqua.setOnClickListener {
            if (snackbar != null) {
                snackbar?.dismiss()
            }
            selectedOption = 200
            selectedOptionName = "Aqua"
            opAqua.background = getDrawable(R.drawable.option_select_bg)
            opCoffee.background = getDrawable(outValue.resourceId)
            opTea.background = getDrawable(outValue.resourceId)
            opSoftDrink.background = getDrawable(outValue.resourceId)
            opJuice.background = getDrawable(outValue.resourceId)
            opMilk.background = getDrawable(outValue.resourceId)

        }

        opCoffee.setOnClickListener {
            if (snackbar != null) {
                snackbar?.dismiss()
            }
            selectedOption = 150
            selectedOptionName = "Coffee"
            opAqua.background = getDrawable(outValue.resourceId)
            opCoffee.background = getDrawable(R.drawable.option_select_bg)
            opTea.background = getDrawable(outValue.resourceId)
            opSoftDrink.background = getDrawable(outValue.resourceId)
            opJuice.background = getDrawable(outValue.resourceId)
            opMilk.background = getDrawable(outValue.resourceId)

        }

        opTea.setOnClickListener {
            if (snackbar != null) {
                snackbar?.dismiss()
            }
            selectedOption = 150
            selectedOptionName = "Tea"
            opAqua.background = getDrawable(outValue.resourceId)
            opCoffee.background = getDrawable(outValue.resourceId)
            opTea.background = getDrawable(R.drawable.option_select_bg)
            opSoftDrink.background = getDrawable(outValue.resourceId)
            opJuice.background = getDrawable(outValue.resourceId)
            opMilk.background = getDrawable(outValue.resourceId)

        }

        opSoftDrink.setOnClickListener {
            if (snackbar != null) {
                snackbar?.dismiss()
            }
            selectedOption = 200
            selectedOptionName = "Soft Drink"
            opAqua.background = getDrawable(outValue.resourceId)
            opCoffee.background = getDrawable(outValue.resourceId)
            opTea.background = getDrawable(outValue.resourceId)
            opSoftDrink.background = getDrawable(R.drawable.option_select_bg)
            opJuice.background = getDrawable(outValue.resourceId)
            opMilk.background = getDrawable(outValue.resourceId)

        }

        opJuice.setOnClickListener {
            if (snackbar != null) {
                snackbar?.dismiss()
            }
            selectedOption = 200
            selectedOptionName = "Juice"
            opAqua.background = getDrawable(outValue.resourceId)
            opCoffee.background = getDrawable(outValue.resourceId)
            opTea.background = getDrawable(outValue.resourceId)
            opSoftDrink.background = getDrawable(outValue.resourceId)
            opJuice.background = getDrawable(R.drawable.option_select_bg)
            opMilk.background = getDrawable(outValue.resourceId)

        }

        opMilk.setOnClickListener {
            if (snackbar != null) {
                snackbar?.dismiss()
            }
            selectedOption = 50
            selectedOptionName = "Milk"
            opAqua.background = getDrawable(outValue.resourceId)
            opCoffee.background = getDrawable(outValue.resourceId)
            opTea.background = getDrawable(outValue.resourceId)
            opSoftDrink.background = getDrawable(outValue.resourceId)
            opJuice.background = getDrawable(outValue.resourceId)
            opMilk.background = getDrawable(R.drawable.option_select_bg)

        }

    }


    private fun setWaterLevel(inTook: Int, totalIntake: Int) {

        YoYo.with(Techniques.SlideInDown)
            .duration(500)
            .playOn(tvIntook)
        tvIntook.text = "$inTook"
        tvTotalIntake.text = "/$totalIntake ml"
        val progress = ((inTook / totalIntake.toFloat()) * 100).toInt()
        YoYo.with(Techniques.Pulse)
            .duration(500)
            .playOn(intakeProgress)
        intakeProgress.currentProgress = progress
        if ((inTook * 100 / totalIntake) > 140) {
            Snackbar.make(main_activity_parent, "You achieved the goal", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        Snackbar.make(
            this.window.decorView.findViewById(android.R.id.content),
            "Please click BACK again to exit",
            Snackbar.LENGTH_SHORT
        ).show()

        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 1000)
    }

}
