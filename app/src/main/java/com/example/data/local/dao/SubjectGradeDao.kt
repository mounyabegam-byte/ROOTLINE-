package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.SubjectGradeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectGradeDao {
    @Query("SELECT * FROM subject_grades WHERE studentRollNumber = :rollNumber ORDER BY overallScore ASC")
    fun getGradesForStudent(rollNumber: String): Flow<List<SubjectGradeEntity>>

    @Query("SELECT * FROM subject_grades WHERE studentRollNumber = :rollNumber")
    suspend fun getGradesForStudentDirect(rollNumber: String): List<SubjectGradeEntity>

    @Query("SELECT * FROM subject_grades")
    fun getAllGrades(): Flow<List<SubjectGradeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrades(grades: List<SubjectGradeEntity>)

    @Update
    suspend fun updateGrade(grade: SubjectGradeEntity)

    @Query("DELETE FROM subject_grades WHERE studentRollNumber = :rollNumber")
    suspend fun deleteGradesForStudent(rollNumber: String)
}
