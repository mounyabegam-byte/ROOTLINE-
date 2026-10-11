package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.StudentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY riskScore DESC")
    fun getAllStudents(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE rollNumber = :rollNumber LIMIT 1")
    fun getStudentByRoll(rollNumber: String): Flow<StudentEntity?>

    @Query("SELECT * FROM students WHERE rollNumber = :rollNumber LIMIT 1")
    suspend fun getStudentByRollDirect(rollNumber: String): StudentEntity?

    @Query("SELECT * FROM students WHERE riskLevel = 'HIGH'")
    fun getHighRiskStudents(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE department = :dept")
    fun getStudentsByDepartment(dept: String): Flow<List<StudentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<StudentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity)

    @Update
    suspend fun updateStudent(student: StudentEntity)

    @Query("DELETE FROM students WHERE rollNumber = :rollNumber")
    suspend fun deleteStudent(rollNumber: String)

    @Query("SELECT COUNT(*) FROM students")
    suspend fun getCount(): Int
}
