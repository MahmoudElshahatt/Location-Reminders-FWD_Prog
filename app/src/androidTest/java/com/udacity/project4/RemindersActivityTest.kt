package com.udacity.project4

import android.app.Activity
import android.app.Application
import android.os.Build
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {

    // Extended Koin Test - embed autoclose @after method to close Koin after every test

    //DECLARING OBJECTS
    private lateinit var app: Application
    private lateinit var reminderRepository: ReminderDataSource
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    //INITIALIZING THE REQUIRED CLASSES AND NEEDED VIEW MODELS AND REPOSITORY
    @Before
    fun setUp() {
        //STOPPING THE ORIGINAL KOIN
        stopKoin()
        //GETTING THE APPLICATION CONTEXT
        app = getApplicationContext()
        //DECLARING AND INITIALIZING A MODULE
        val myModule = module {
            // CREATING INSTANCE OF RemindersListViewModel
            viewModel {
                RemindersListViewModel(
                    app,
                    get() as ReminderDataSource
                )
            }
            // CREATING INSTANCE OF SaveReminderViewModel
            single {
                SaveReminderViewModel(
                    app,
                    get() as ReminderDataSource
                )
            }
            // CREATING INSTANCE OF RemindersLocalRepository AND DAO
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(app) }
        }
        //STARTING KOIN
        startKoin {
            modules(listOf(myModule))
        }
        //GETTING REAL REPOSITORY
        reminderRepository = get()

        //CLEARING DATA BEFORE TEST CASES
        runBlocking {
            reminderRepository.deleteAllReminders()
        }
    }


    // IDLING RESOURCES TELLS Espresso THAT THE APP IS IDLE OR BUSY.
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }


    // UNREGISTER OUR IDLING RESOURCES SO IT CAUSES NO MEMORY LEAK
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    //HERE I AM SIMULATING SAVING A REMINDER AND CHECKING THE TOAST MESSAGE
    @Test
    fun ReminderSavedToastTest() {
        // LAUNCHING RemindersActivity
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        // GETTING INSTANCE OF IT.
        val activity = getActivity(activityScenario)

        //SIMULATE SAVING A REMINDER IN  ADDREMINDERFRAGMENT AND SELECTLOCATIONFRAGMENT
        //CLICKING THE addReminderFAB TO NAVIGATE
        Espresso.onView(ViewMatchers.withId(R.id.addReminderFAB)).perform(ViewActions.click())
        //TYPING SOME DUMMY TEXTS IN THE EDITTEXTS
        Espresso.onView(ViewMatchers.withId(R.id.reminderTitle))
            .perform(ViewActions.typeText("My location"))
        Espresso.onView(ViewMatchers.withId(R.id.reminderDescription))
            .perform(ViewActions.typeText("My location Description"))
        Espresso.closeSoftKeyboard()
        Espresso.onView(ViewMatchers.withText("My location"))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withText("My location Description")).check(
            ViewAssertions.matches(ViewMatchers.isDisplayed())
        )
        //CLICKING SELECTLOCATION BUTTON TO NAVIGATE TO THE MAP FRAGMENT
        Espresso.onView(ViewMatchers.withId(R.id.selectLocation)).perform(ViewActions.click())
        //MAKING A MARKER ON THE MAP
        Espresso.onView(ViewMatchers.withId(R.id.map)).perform(ViewActions.longClick())
        //CLICKING SAVELOCATION BUTTON FROM SELECTLOCATIONFRAGMENT
        Espresso.onView(ViewMatchers.withId(R.id.saveLocation))
            .perform(ViewActions.click())
        //THEN CLICKING THE SAVEREMINDER BUTTON FROM ADDREMINDERFRAGMENT
        Espresso.onView(ViewMatchers.withId(R.id.saveReminder))
            .perform(ViewActions.click())
        // CHECKING IF TOAST WITH REMINDER SAVED ! IS SHOWN ON THE SCREEN
        Espresso.onView(ViewMatchers.withText(R.string.reminder_saved)).inRoot(
            RootMatchers.withDecorView(
                // matching toast token
                CoreMatchers.not(CoreMatchers.`is`(activity.window.decorView))
            )
        )

            //Here the Toast is displayed already but it gives me a failed test on Realme RMX1851 phone
            //but the test is working just fine.
            .check(
                ViewAssertions.matches(
                    // toast is displayed
                    ViewMatchers.isDisplayed()
                )
            )

        //CLOSING EVERY THING
        activityScenario.close()

    }

    // RETURNING INSTANCE OF ReminderActivity
    private fun getActivity(activityScenario: ActivityScenario<RemindersActivity>): Activity {
        lateinit var activity: Activity
        activityScenario.onActivity {
            activity = it
        }
        return activity
    }

    //HERE I AM TESTING THE SNACK BAR SHOWING ERROR please enter data
    @Test
    fun errorEnterTitleSnackBarTest() {
        // LAUNCHING RemindersActivity
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        // CLICKING ADDREMINDER TO NAVIGATE AND SAVE WITH NO DATA ENTERED
        Espresso.onView(ViewMatchers.withId(R.id.addReminderFAB)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.saveReminder)).perform(ViewActions.click())

        val message = app.getString(R.string.err_enter_title)
        // MAKING SURE THAT SNACK BAR IS SHOWN WITH THE RIGHT MESSAGE (please enter data)
        Espresso.onView(ViewMatchers.withText(message))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        //CLOSING THE SENARIO AFTER FINISHING
        activityScenario.close()
    }

}

