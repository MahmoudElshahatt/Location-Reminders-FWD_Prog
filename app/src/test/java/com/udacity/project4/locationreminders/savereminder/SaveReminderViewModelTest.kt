package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
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

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Should insert API level 30
@Config(sdk = [Build.VERSION_CODES.P])
class SaveReminderViewModelTest {
    //TRYING TO EXECUTE EACH TASK INDEPENDENTLY USING Architecture Components.
    @get:Rule
    var coroutineRule = MainCoroutineRule()

    //DECLARING SOME VARIABLES LIKE FAKEDATA CLASS AND THE VIEWMODEL TO BE TESTED
    private lateinit var fakeReminders: FakeDataSource
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    //INITIALIZING THE VARIABLES BEFORE ANY TEST CASES
    @Before
    fun setUp() {
        fakeReminders = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(),
            fakeReminders
        )
    }

    //IN THE END OF TESTS I STOPPED THE DEPENDENCY INJECTION BY KOIN
    @After
    fun Clear() {
        stopKoin()
    }

    //SIMPLE FUNCTION TO CREATE A REMINDERITEM
    private fun createFakeDataItem(): ReminderDataItem {
        val item = ReminderDataItem(
            "Test title", "Test desc bla bla",
            "Test location", 30.0, 29.0
        )
        return item
    }

    //SIMPLE FUNCTION TO CREATE A INCORRECT REMINDERITEM TO SIMULATE ERROR
    private fun createIncorrectDataItem(): ReminderDataItem {
        val item = ReminderDataItem(
            "Test title", "Test desc bla bla",
            "", 30.0, 29.0
        )
        return item
    }

    //TEST ADDING INVALID DATA ITEM IN SAVEREMINDERVIEWMODEL
    @Test
    fun incorrectDataItemTest() = runBlockingTest {
        //HERE I AM ADDING INVALID DATA ITEM TO TEST validateEnteredData()
        val boolResult = saveReminderViewModel.validateEnteredData(createIncorrectDataItem())
        //IN CASE OF INVALID DATA ITEM THE RETURN VALUE IS FALSE FROM validateEnteredData() SO I AM CHECKING THAT
        MatcherAssert.assertThat(
            boolResult, CoreMatchers.`is`(false)
        )
    }

    //
    @Test
    fun loadingDataItemTest() {
        //MAKING SURE THAT THIS TASK IS RUNNING INDEPENDENTLY OF THE OTHER TEST
        coroutineRule.pauseDispatcher()
        //SAVING A REMINDER DATA ITEM
        saveReminderViewModel.saveReminder(createFakeDataItem())
        //MAKING SURE THAT LOADING AND SAVING IS DONE SUCCESSFULLY
        MatcherAssert.assertThat(saveReminderViewModel.showLoading.value, CoreMatchers.`is`(true))

    }
}