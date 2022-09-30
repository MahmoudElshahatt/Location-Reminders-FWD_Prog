package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
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
import org.mockito.Mockito


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest() {

    //DECLARING THE REPOSITORY AND APPLICATION
    private lateinit var reminderRepository: ReminderDataSource
    private lateinit var app: Application

    // AN IDLING RESOURCE THAT WAITS FOR DATA BINDING TO REPORT IDLE STATUS FOR ALL DATA BINDING
    // LAYOUTS
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    //INITIALIZING THE APPLICATION CONTEXT AND NEEDED VIEW MODELS AND REPOSITORY
    @Before
    fun setUp() {
        //STOPPING THE ORIGINAL KOIN
        stopKoin()

        // INITIALIZING THE CONTEXT OBJECT
        app = getApplicationContext()
        //DECLARING AND INITIALIZING A MODULE
        val modulee = module {
            // CREATING INSTANCE OF RemindersListViewModel
            viewModel {

                RemindersListViewModel(app, get() as ReminderDataSource)
            }
            // CREATING INSTANCE OF SaveReminderViewModel
            single {
                SaveReminderViewModel(app, get() as ReminderDataSource)
            }
            // CREATING INSTANCE OF RemindersLocalRepository
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            //CREATING THE DAO
            single { LocalDB.createRemindersDao(app) }
        }

        //STARTING KOIN
        startKoin {
            modules(listOf(modulee))
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

    // UNREGISTER OUR IDLING RESOURCES
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    //HERE I AM TESTING THE NODATA MESSAGE THAT APPEAR IF NO DATA IS FOUNDED
    @Test
    fun NoDataDisplayedTest() {
        //LAUNCHING OUR FRAGMENT
        val fragmentScenario =
            launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(fragmentScenario)
        // MAKING SURE THAT OUR MASSAGE APPEAR CORRECTLY
        Espresso.onView(ViewMatchers.withText(R.string.no_data))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

    }

    //HERE I AM TESTING the NAVIGATION FROM  ReminderListFragment TO AddReminderFragment
    @Test
    fun navigatingToAddReminderFragmentTest() {
        // OPENING THE FIRST FRAG ReminderListFragment
        val fragmentScenario =
            launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(fragmentScenario)

        //CREATING MOCK OBJECT WITH OUR NAVCONTROLLER WITH THE FRAGMENT
        val navController = Mockito.mock(NavController::class.java)

        //GIVING THE NAVCONTROLLER TO OUR FRAGMENT
        fragmentScenario.onFragment { Navigation.setViewNavController(it.view!!, navController) }

        // SIMULATE CLICKING THE BUTTON TO NAVIGATE
        Espresso.onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())

        //VERIFY THAT THE NAVIGATION IS DONE FROM ReminderListFragment TO AddReminderFragment
        Mockito.verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

}
