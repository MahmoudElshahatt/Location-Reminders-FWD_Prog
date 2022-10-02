package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {
    //MAKING A VARIABLE TO SIMULATE ERRORS
    var returnError: Boolean = false

    //MAKING MY FAKE DATA
    val remindersList = mutableListOf<ReminderDTO>()

    //SIMULATE GETTING REMINDERS WHEN ERROR OR SUCCESS
    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return try {
            if (returnError) {
                //IF THERE IS NO REMINDERS FOUND
                throw Exception("Reminders are unable to get retrieved")
            } else {
                //SAVING THE DATA SUCCESSFULLY
                Result.Success(ArrayList(remindersList))
            }
        } catch (ex: Exception) {
            Result.Error(ex.localizedMessage)
        }

    }

    //SIMULATING SAVING THE FAKE DATA
    override suspend fun saveReminder(reminder: ReminderDTO) {
        remindersList.add(reminder)
    }

    //SIMULATING GETTING THE FAKE DATA
    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        return try {
            val reminder = remindersList.find { id == it.id }
            if (returnError || reminder == null) {
                //IF REMINDER IS NOT FOUND OR I TRIGGERED RETURN ERROR VARIABLE THROW EXCEPTION
                throw Exception("Not found reminder: $id")
            } else {
                //IF REMINDER IS FOUND ADD IT TO THE RESULT.SUCCESS
                Result.Success(reminder)
            }
        } catch (ex: Exception) {
            Result.Error(ex.localizedMessage)
        }
    }

    //SIMULATING DELETING THE FAKE DATA
    override suspend fun deleteAllReminders() {
        remindersList.clear()
    }


}