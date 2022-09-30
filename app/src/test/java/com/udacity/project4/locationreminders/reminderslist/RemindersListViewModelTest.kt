package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//Should insert API level 30
@Config(sdk = [Build.VERSION_CODES.P])
class RemindersListViewModelTest {

    //TRYING TO EXECUTE EACH TASK INDEPENDENTLY USING Architecture Components.
    @get:Rule
    var coroutineRule = MainCoroutineRule()

    //DECLARING SOME VARIABLES LIKE FAKEDATA CLASS AND THE VIEWMODEL TO BE TESTED
    private lateinit var fakeReminders: FakeDataSource
    private lateinit var remindersViewModel: RemindersListViewModel

    //INITIALIZING THE VARIABLES BEFORE ANY TEST CASES
    @Before
    fun setUp() {
        fakeReminders = FakeDataSource()
        remindersViewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(),
            fakeReminders
        )
    }

    //IN THE END I STOPPED THE DEPENDENCY INJECTION BY KOIN
    @After
    fun Clear() {
        stopKoin()
    }

    //SIMULATING THE ERROR TO RETURN THE EXCEPTION ("Reminders not found")
    @Test
    fun returningErrorTest() = runBlockingTest {
        saveFakeReminderData()
        fakeReminders.returnError = true
        //CALLING THE LOADING FUNCTION AFTER TRIGGERING THE ERROR TO TEST
        remindersViewModel.loadReminders()
        //EQUALING THE VALUE OF THE SNACKBAR WITH ERROR MESSAGE
        MatcherAssert.assertThat(
            remindersViewModel.showSnackBar.value, CoreMatchers.`is`("Reminders not found")
        )
    }

    //THIS TEST IS ABOUT CHECKING loadReminders() WORKING CORRECTLY
    @Test
    fun checkLoadingTest() = runBlockingTest {
        //MAKING SURE THAT THIS TASK IS RUNNING INDEPENDENTLY OF THE OTHER TEST
        coroutineRule.pauseDispatcher()
        //SAVING MY DATA
        saveFakeReminderData()
        //CALLING THE FUNCTION
        remindersViewModel.loadReminders()
        //HERE I AM CHECKING THAT VARIABLE SHOWLOADING THAT IS HAS VALUE TRUE AFTER THE LOADING IS SUCCESSFUL
        MatcherAssert.assertThat(remindersViewModel.showLoading.value, CoreMatchers.`is`(true))
    }

    //SIMPLE UNCTION TO SAVE A REMINDER IN THE FAKEREMINDERS DATA SET
    private suspend fun saveFakeReminderData() {
        fakeReminders.saveReminder(
            ReminderDTO(
                "Test title", "Test desc bla bla",
                "Test location", 30.0, 29.0, "1"
            )
        )
    }

}
