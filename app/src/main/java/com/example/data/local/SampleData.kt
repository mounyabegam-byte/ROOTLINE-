package com.example.data.local

import com.example.data.local.entity.BadgeEntity
import com.example.data.local.entity.InterventionEntity
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.StudentEntity
import com.example.data.local.entity.StudyTaskEntity
import com.example.data.local.entity.SubjectGradeEntity

object SampleData {
    val initialStudents = listOf(
        StudentEntity(
            rollNumber = "21A91A0501",
            name = "Rahul Kumar",
            department = "CSE",
            year = 3,
            semester = 5,
            section = "A",
            email = "rahul.kumar@college.edu",
            phone = "+91 98765 43210",
            attendance = 68.0,
            internalMarks = 54.0,
            assignmentScore = 61.0,
            labScore = 59.0,
            quizScore = 56.0,
            previousGpa = 6.4,
            currentGpa = 6.2,
            predictedGpa = 6.8,
            backlogs = 2,
            engagementScore = 52.0,
            riskScore = 82,
            riskLevel = "HIGH",
            successScore = 62,
            cluster = "At Risk",
            advisorName = "Dr. S. K. Sharma"
        ),
        StudentEntity(
            rollNumber = "21A91A0502",
            name = "Priya Sharma",
            department = "CSE",
            year = 3,
            semester = 5,
            section = "A",
            email = "priya.sharma@college.edu",
            phone = "+91 98765 43211",
            attendance = 94.0,
            internalMarks = 89.0,
            assignmentScore = 96.0,
            labScore = 92.0,
            quizScore = 94.0,
            previousGpa = 8.9,
            currentGpa = 9.1,
            predictedGpa = 9.3,
            backlogs = 0,
            engagementScore = 95.0,
            riskScore = 12,
            riskLevel = "LOW",
            successScore = 94,
            cluster = "High Performers",
            advisorName = "Dr. S. K. Sharma"
        ),
        StudentEntity(
            rollNumber = "21A91A0503",
            name = "Arjun Verma",
            department = "CSE",
            year = 3,
            semester = 5,
            section = "B",
            email = "arjun.verma@college.edu",
            phone = "+91 98765 43212",
            attendance = 74.0,
            internalMarks = 65.0,
            assignmentScore = 70.0,
            labScore = 68.0,
            quizScore = 66.0,
            previousGpa = 7.1,
            currentGpa = 6.9,
            predictedGpa = 7.3,
            backlogs = 1,
            engagementScore = 68.0,
            riskScore = 46,
            riskLevel = "MEDIUM",
            successScore = 71,
            cluster = "Improvement Required",
            advisorName = "Prof. Ananya Kumar"
        ),
        StudentEntity(
            rollNumber = "21A91A0412",
            name = "Sneha Patel",
            department = "ECE",
            year = 3,
            semester = 5,
            section = "A",
            email = "sneha.patel@college.edu",
            phone = "+91 98765 43213",
            attendance = 88.0,
            internalMarks = 82.0,
            assignmentScore = 85.0,
            labScore = 86.0,
            quizScore = 80.0,
            previousGpa = 8.0,
            currentGpa = 8.2,
            predictedGpa = 8.5,
            backlogs = 0,
            engagementScore = 84.0,
            riskScore = 18,
            riskLevel = "LOW",
            successScore = 86,
            cluster = "Consistent Performers",
            advisorName = "Dr. R. Raman"
        ),
        StudentEntity(
            rollNumber = "21A91A0315",
            name = "Vikram Rao",
            department = "MECH",
            year = 3,
            semester = 5,
            section = "A",
            email = "vikram.rao@college.edu",
            phone = "+91 98765 43214",
            attendance = 62.0,
            internalMarks = 50.0,
            assignmentScore = 55.0,
            labScore = 54.0,
            quizScore = 48.0,
            previousGpa = 6.0,
            currentGpa = 5.8,
            predictedGpa = 6.3,
            backlogs = 3,
            engagementScore = 44.0,
            riskScore = 88,
            riskLevel = "HIGH",
            successScore = 54,
            cluster = "At Risk",
            advisorName = "Prof. K. Venkatesh"
        ),
        StudentEntity(
            rollNumber = "21A91A0210",
            name = "Ananya Gupta",
            department = "EEE",
            year = 3,
            semester = 5,
            section = "B",
            email = "ananya.gupta@college.edu",
            phone = "+91 98765 43215",
            attendance = 79.0,
            internalMarks = 75.0,
            assignmentScore = 80.0,
            labScore = 77.0,
            quizScore = 74.0,
            previousGpa = 7.3,
            currentGpa = 7.5,
            predictedGpa = 7.8,
            backlogs = 0,
            engagementScore = 76.0,
            riskScore = 26,
            riskLevel = "LOW",
            successScore = 79,
            cluster = "Consistent Performers",
            advisorName = "Dr. P. Narayan"
        )
    )

    val initialGrades = listOf(
        // Rahul's subjects
        SubjectGradeEntity(
            studentRollNumber = "21A91A0501",
            subjectName = "Computer Networks",
            subjectCode = "CS301",
            internalMarks = 55.0,
            assignmentMarks = 60.0,
            labMarks = 58.0,
            attendance = 68.0,
            overallScore = 60.0,
            credits = 4,
            aiStatus = "NEEDS_ATTENTION"
        ),
        SubjectGradeEntity(
            studentRollNumber = "21A91A0501",
            subjectName = "Machine Learning",
            subjectCode = "CS302",
            internalMarks = 72.0,
            assignmentMarks = 81.0,
            labMarks = 76.0,
            attendance = 84.0,
            overallScore = 77.0,
            credits = 4,
            aiStatus = "GOOD"
        ),
        SubjectGradeEntity(
            studentRollNumber = "21A91A0501",
            subjectName = "Java Programming",
            subjectCode = "CS303",
            internalMarks = 84.0,
            assignmentMarks = 88.0,
            labMarks = 86.0,
            attendance = 85.0,
            overallScore = 86.0,
            credits = 3,
            aiStatus = "GOOD"
        ),
        SubjectGradeEntity(
            studentRollNumber = "21A91A0501",
            subjectName = "DBMS",
            subjectCode = "CS304",
            internalMarks = 79.0,
            assignmentMarks = 83.0,
            labMarks = 80.0,
            attendance = 82.0,
            overallScore = 81.0,
            credits = 4,
            aiStatus = "GOOD"
        ),
        SubjectGradeEntity(
            studentRollNumber = "21A91A0501",
            subjectName = "Artificial Intelligence",
            subjectCode = "CS305",
            internalMarks = 88.0,
            assignmentMarks = 90.0,
            labMarks = 89.0,
            attendance = 88.0,
            overallScore = 89.0,
            credits = 3,
            aiStatus = "EXCELLENT"
        ),
        // Priya's subjects
        SubjectGradeEntity(
            studentRollNumber = "21A91A0502",
            subjectName = "Machine Learning",
            subjectCode = "CS302",
            internalMarks = 92.0,
            assignmentMarks = 98.0,
            labMarks = 95.0,
            attendance = 96.0,
            overallScore = 95.0,
            credits = 4,
            aiStatus = "EXCELLENT"
        ),
        SubjectGradeEntity(
            studentRollNumber = "21A91A0502",
            subjectName = "Computer Networks",
            subjectCode = "CS301",
            internalMarks = 88.0,
            assignmentMarks = 94.0,
            labMarks = 90.0,
            attendance = 92.0,
            overallScore = 91.0,
            credits = 4,
            aiStatus = "EXCELLENT"
        )
    )

    val initialInterventions = listOf(
        InterventionEntity(
            studentRollNumber = "21A91A0501",
            studentName = "Rahul Kumar",
            department = "CSE",
            type = "Remedial Classes & Attendance Plan",
            issue = "Low attendance (68%) + Declining internal marks (54%) + 2 Backlogs",
            actionPlan = "Assigned 1-on-1 tutoring in Computer Networks + weekly faculty tracking",
            assignedFaculty = "Prof. Ananya Kumar",
            deadline = "15 Oct 2026",
            status = "IN_PROGRESS",
            beforeRisk = 82,
            afterRisk = 54,
            beforeAttendance = 65.0,
            afterAttendance = 76.0,
            isResolved = false,
            notes = "Attending remedial lab sessions regularly; quiz score improved by 12%."
        ),
        InterventionEntity(
            studentRollNumber = "21A91A0315",
            studentName = "Vikram Rao",
            department = "MECH",
            type = "Academic Counseling",
            issue = "Critical risk: 3 backlogs and 62% attendance",
            actionPlan = "Remedial classes for Thermodynamics + Parent-faculty counseling meeting",
            assignedFaculty = "Prof. K. Venkatesh",
            deadline = "20 Oct 2026",
            status = "PROPOSED",
            beforeRisk = 88,
            afterRisk = 88,
            beforeAttendance = 62.0,
            afterAttendance = 62.0,
            isResolved = false,
            notes = "Scheduled introductory assessment test."
        ),
        InterventionEntity(
            studentRollNumber = "21A91A0503",
            studentName = "Arjun Verma",
            department = "CSE",
            type = "Assignment Remedial Program",
            issue = "Pending 3 assignments and 74% attendance",
            actionPlan = "Peer study partner assigned for DBMS + submission extension",
            assignedFaculty = "Dr. S. K. Sharma",
            deadline = "01 Oct 2026",
            status = "SUCCESSFUL",
            beforeRisk = 65,
            afterRisk = 46,
            beforeAttendance = 70.0,
            afterAttendance = 74.0,
            isResolved = true,
            notes = "Successfully completed pending assignments; risk dropped from 65% to 46%."
        )
    )

    val initialNotifications = listOf(
        NotificationEntity(
            studentRollNumber = "21A91A0501",
            title = "⚠️ Attendance Alert: Computer Networks",
            message = "Your attendance in Computer Networks has fallen to 68%. Minimum required is 75%.",
            type = "CRITICAL",
            timestamp = System.currentTimeMillis() - 3600_000 * 4
        ),
        NotificationEntity(
            studentRollNumber = "21A91A0501",
            title = "📚 Pending Assignment Reminder",
            message = "Database Systems Assignment #3 is due tomorrow at 11:59 PM.",
            type = "WARNING",
            timestamp = System.currentTimeMillis() - 3600_000 * 12
        ),
        NotificationEntity(
            studentRollNumber = "21A91A0501",
            title = "🎉 Performance Improvement!",
            message = "Your Machine Learning quiz score increased from 61% to 78%. Keep going!",
            type = "SUCCESS",
            timestamp = System.currentTimeMillis() - 3600_000 * 24
        ),
        NotificationEntity(
            studentRollNumber = "ALL",
            title = "📢 Mid-Term Examination Schedule",
            message = "Mid-term examinations will commence from 22 October 2026.",
            type = "INFO",
            timestamp = System.currentTimeMillis() - 3600_000 * 48
        )
    )

    val initialTasks = listOf(
        StudyTaskEntity(
            studentRollNumber = "21A91A0501",
            dayOfWeek = "Monday",
            orderIndex = 1,
            subject = "Computer Networks",
            taskDescription = "TCP/IP 3-Way Handshake & Flow Control Revision",
            durationMinutes = 60,
            priority = "HIGH",
            isCompleted = true
        ),
        StudyTaskEntity(
            studentRollNumber = "21A91A0501",
            dayOfWeek = "Tuesday",
            orderIndex = 2,
            subject = "Machine Learning",
            taskDescription = "Classification Algorithms & Decision Tree Tuning",
            durationMinutes = 90,
            priority = "MEDIUM",
            isCompleted = true
        ),
        StudyTaskEntity(
            studentRollNumber = "21A91A0501",
            dayOfWeek = "Wednesday",
            orderIndex = 3,
            subject = "DBMS",
            taskDescription = "SQL Joins, Normalization (3NF & BCNF) Practice",
            durationMinutes = 60,
            priority = "MEDIUM",
            isCompleted = false
        ),
        StudyTaskEntity(
            studentRollNumber = "21A91A0501",
            dayOfWeek = "Thursday",
            orderIndex = 4,
            subject = "Computer Networks",
            taskDescription = "Subnetting calculations & Packet Tracer Lab Exercise",
            durationMinutes = 120,
            priority = "HIGH",
            isCompleted = false
        ),
        StudyTaskEntity(
            studentRollNumber = "21A91A0501",
            dayOfWeek = "Friday",
            orderIndex = 5,
            subject = "Machine Learning",
            taskDescription = "Neural Networks Fundamentals - Forward & Backprop",
            durationMinutes = 60,
            priority = "HIGH",
            isCompleted = false
        ),
        StudyTaskEntity(
            studentRollNumber = "21A91A0501",
            dayOfWeek = "Saturday",
            orderIndex = 6,
            subject = "Full Stack Mini-Project",
            taskDescription = "REST API integration & Model evaluation report",
            durationMinutes = 120,
            priority = "MEDIUM",
            isCompleted = false
        ),
        StudyTaskEntity(
            studentRollNumber = "21A91A0501",
            dayOfWeek = "Sunday",
            orderIndex = 7,
            subject = "Weekly Assessment",
            taskDescription = "Attempt 30 MCQ mock quiz + Faculty review preparation",
            durationMinutes = 60,
            priority = "HIGH",
            isCompleted = false
        )
    )

    val initialBadges = listOf(
        BadgeEntity(
            studentRollNumber = "21A91A0501",
            title = "Attendance Champion",
            description = "Maintain 85%+ attendance in 3 consecutive subjects",
            iconEmoji = "🔥",
            points = 250,
            isUnlocked = true,
            unlockedDate = "Sep 28, 2026"
        ),
        BadgeEntity(
            studentRollNumber = "21A91A0501",
            title = "Assignment Master",
            description = "Submit 10 assignments on time",
            iconEmoji = "📝",
            points = 300,
            isUnlocked = false
        ),
        BadgeEntity(
            studentRollNumber = "21A91A0501",
            title = "Comeback Star",
            description = "Reduce academic risk score by over 20 points",
            iconEmoji = "⭐",
            points = 500,
            isUnlocked = true,
            unlockedDate = "Oct 02, 2026"
        ),
        BadgeEntity(
            studentRollNumber = "21A91A0501",
            title = "Coding Streak",
            description = "Practice 7 consecutive days of lab exercises",
            iconEmoji = "💻",
            points = 350,
            isUnlocked = true,
            unlockedDate = "Sep 20, 2026"
        ),
        BadgeEntity(
            studentRollNumber = "21A91A0501",
            title = "Top Performer",
            description = "Achieve 90%+ in Artificial Intelligence",
            iconEmoji = "🏆",
            points = 400,
            isUnlocked = true,
            unlockedDate = "Sep 15, 2026"
        )
    )
}
