package com.udacity.project4.locationreminders.data.local

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    //DECLARING THE DATABASE
    private lateinit var reminderDB: RemindersDatabase

    //TRYING TO EXECUTE EACH TASK INDEPENDENTLY USING Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    //INITIALIZING THE DATABASE IN MEMORY FOR TESTING PURPOSES BEFORE ANY TEST CASES
    @Before
    fun setUp() {
        reminderDB = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    //HERE I AM TESTING THE INSERTING AND RETRIEVING
    @Test
    fun insertReminderTest() = runBlockingTest {

        val reminder = ReminderDTO(
            "Test title", "Test desc bla bla",
            "Test location", 30.0, 29.0
        )
        //ADDING DATA
        reminderDB.reminderDao().saveReminder(reminder)
        //RETRIEVING THE DATA AGAIN
        val dataList = reminderDB.reminderDao().getReminders()
//CHECKING THAT THE REMINDER THAT I ADDED EQUALS RETRIEVED REMINDER
        assertThat(dataList[0], CoreMatchers.`is`(reminder))

    }

    //IN THE END I CLOSED THE DATABASE
    @After
    fun Clear() = reminderDB.close()
}