package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.InterventionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InterventionDao {
    @Query("SELECT * FROM interventions ORDER BY createdAt DESC")
    fun getAllInterventions(): Flow<List<InterventionEntity>>

    @Query("SELECT * FROM interventions WHERE studentRollNumber = :rollNumber ORDER BY createdAt DESC")
    fun getInterventionsForStudent(rollNumber: String): Flow<List<InterventionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIntervention(intervention: InterventionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInterventions(interventions: List<InterventionEntity>)

    @Update
    suspend fun updateIntervention(intervention: InterventionEntity)

    @Query("DELETE FROM interventions WHERE id = :id")
    suspend fun deleteIntervention(id: Int)
}
