package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.StudyTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyTaskDao {
    @Query("SELECT * FROM study_tasks WHERE studentRollNumber = :rollNumber ORDER BY orderIndex ASC")
    fun getTasksForStudent(rollNumber: String): Flow<List<StudyTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<StudyTaskEntity>)

    @Update
    suspend fun updateTask(task: StudyTaskEntity)

    @Query("UPDATE study_tasks SET isCompleted = :completed WHERE id = :id")
    suspend fun setTaskCompleted(id: Int, completed: Boolean)

    @Query("DELETE FROM study_tasks WHERE studentRollNumber = :rollNumber")
    suspend fun clearTasks(rollNumber: String)
}
