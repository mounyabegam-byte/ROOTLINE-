package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.BadgeDao
import com.example.data.local.dao.InterventionDao
import com.example.data.local.dao.NotificationDao
import com.example.data.local.dao.StudentDao
import com.example.data.local.dao.StudyTaskDao
import com.example.data.local.dao.SubjectGradeDao
import com.example.data.local.entity.BadgeEntity
import com.example.data.local.entity.InterventionEntity
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.StudentEntity
import com.example.data.local.entity.StudyTaskEntity
import com.example.data.local.entity.SubjectGradeEntity

@Database(
    entities = [
        StudentEntity::class,
        SubjectGradeEntity::class,
        InterventionEntity::class,
        NotificationEntity::class,
        StudyTaskEntity::class,
        BadgeEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun subjectGradeDao(): SubjectGradeDao
    abstract fun interventionDao(): InterventionDao
    abstract fun notificationDao(): NotificationDao
    abstract fun studyTaskDao(): StudyTaskDao
    abstract fun badgeDao(): BadgeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "edupulse_analytics.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
