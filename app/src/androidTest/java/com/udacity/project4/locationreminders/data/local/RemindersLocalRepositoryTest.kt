package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {
    //TRYING TO EXECUTE EACH TASK INDEPENDENTLY USING Architecture Components.

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    //DECLARING SOME VARIABLES LIKE DATABASE AND THE REPOSITRY TO TEST IT
    private lateinit var reminderDB: RemindersDatabase
    private lateinit var reminderRepository: RemindersLocalRepository

    //INITIALIZING THE DATABASE IB MEMORY FOR TESTING PURPOSES BEFORE ANY TEST CASES AND THE REPOSITORY
    @Before
    fun setup() {
        reminderDB = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()

        reminderRepository = RemindersLocalRepository(reminderDB.reminderDao())
    }

    //IN THE END I STOPPED THE DEPENDENCY INJECTION BY KOIN
    @After
    fun Clear() = reminderDB.close()

    //HERE I AM TESTING THE INSERTING AND RETRIEVING IN THE REPOSITORY
    @Test
    fun insertDataAndRetrieve() = runBlocking {
        //MAKING A REMINDER
        val reminder = ReminderDTO(
            "Test title", "Test desc bla bla",
            "Test location", 30.0, 29.0
        )
        //SAVING THE REMINDER IN THE REPO THAT SAVE IT IN DB
        reminderRepository.saveReminder(reminder)
        //RETRIEVEING THE REMINDER BY ID
        val result = reminderRepository.getReminder(reminder.id)
        result as Result.Success
        //CHECKING THAT RETRIEVED REMINDER IS NOT NULL
        assertThat(result.data != null, `is`(true))
        //CHECKING THAT RETRIEVED REMINDER IS EQUALING THE REMINDER THAT I ADDED
        assertThat(result.data, `is`(reminder))

    }

    //HERE I AM TESTING THAT NO DATA IN TH DATABASE BY CALLING REPOSITORY FUNCTION getReminder(reminder.id)
    @Test
    fun noDataFoundTest() = runBlocking {
        //MAKING A REMINDER
        val reminder = ReminderDTO(
            "Test title", "Test desc bla bla",
            "Test location", 30.0, 29.0
        )
        //GETTING THE RESULT FROM THE getReminder(reminder.id) WITHOUT ADDING ANY DATA TO THE DATABASE
        //TO SIMULATE NO REMINDER IS FOUNDED IN THE DATABASE
        val result = reminderRepository.getReminder(reminder.id)
        //THE RETURN IS RESULT.ERROR BECAUSE I DID NOT THE REMINDER IN THE FIRST PLACE
        assertThat(result is Result.Error, `is`(true))
        result as Result.Error
        //CHECKING THE RETURNED ERROR MESSAGE
        assertThat(result.message, `is`("Reminder not found!"))
    }

    //SIMULATING THE EMPTY LIST TEST HERE
    @Test
    fun remindersEmptyListTest() = runBlocking {
        val reminder = ReminderDTO(
            "Test title", "Test desc bla bla",
            "Test location", 30.0, 29.0
        )
        //ADDING THE REMINDER TO THE LIST
        reminderRepository.saveReminder(reminder)
        //DELETING ALL REMINDERS IN THE LIST AND DATABASE
        reminderRepository.deleteAllReminders()

        //GETTING THE RESULT FROM GETTING THE DATA
        val result = reminderRepository.getReminders()

        assertThat(result is Result.Success, `is`(true))

        //CHECKING IF RETRIEVED DATA OF RESULT IS AN EMPTY LIST
        result as Result.Success
        assertThat(result.data, `is`(emptyList()))
    }
}