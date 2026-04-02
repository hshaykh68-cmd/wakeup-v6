package com.wakeup.app.data.mission

import com.wakeup.app.domain.model.MissionData
import com.wakeup.app.domain.model.MissionDifficulty
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for MissionChallenge.validate() implementations.
 * Tests all mission types and difficulties to catch validation regressions.
 */
class MissionValidationTest {

    // ==================== MATH MISSION TESTS ====================

    @Test
    fun `MathMission validate accepts correct exact answer`() {
        val mission = MathMission(MissionDifficulty.EASY)
        val data = MissionData("5 + 3 = ?", "8", type = MissionType.MATH)

        assertTrue(mission.validate("8", data))
    }

    @Test
    fun `MathMission validate rejects incorrect answer`() {
        val mission = MathMission(MissionDifficulty.EASY)
        val data = MissionData("5 + 3 = ?", "8", type = MissionType.MATH)

        assertFalse(mission.validate("7", data))
        assertFalse(mission.validate("9", data))
    }

    @Test
    fun `MathMission validate trims whitespace from input`() {
        val mission = MathMission(MissionDifficulty.EASY)
        val data = MissionData("5 + 3 = ?", "8", type = MissionType.MATH)

        assertTrue(mission.validate("  8  ", data))
        assertTrue(mission.validate("8 ", data))
        assertTrue(mission.validate(" 8", data))
    }

    @Test
    fun `MathMission validate handles negative answers`() {
        val mission = MathMission(MissionDifficulty.EASY)
        val data = MissionData("5 - 10 = ?", "-5", type = MissionType.MATH)

        assertTrue(mission.validate("-5", data))
        assertFalse(mission.validate("5", data))
    }

    @Test
    fun `MathMission generate produces valid equation for EASY`() {
        val mission = MathMission(MissionDifficulty.EASY)
        val data = mission.generate()

        assertEquals(MissionType.MATH, data.type)
        assertTrue(data.question.contains("+"))
        assertTrue(data.answer.toIntOrNull() != null)
    }

    @Test
    fun `MathMission generate produces valid equation for MEDIUM`() {
        val mission = MathMission(MissionDifficulty.MEDIUM)
        val data = mission.generate()

        assertEquals(MissionType.MATH, data.type)
        assertTrue(data.question.contains("×"))
        assertTrue(data.answer.toIntOrNull() != null)
    }

    @Test
    fun `MathMission generate produces valid equation for HARD`() {
        val mission = MathMission(MissionDifficulty.HARD)
        val data = mission.generate()

        assertEquals(MissionType.MATH, data.type)
        assertTrue(data.question.contains("²"))
        assertTrue(data.answer.toIntOrNull() != null)
    }

    // ==================== MEMORY MISSION TESTS ====================

    @Test
    fun `MemoryMission validate accepts correct pattern`() {
        val mission = MemoryMission(MissionDifficulty.EASY)
        val data = MissionData(
            question = "Remember: 1 - 2 - 3 - 4",
            answer = "1,2,3,4",
            pattern = listOf(1, 2, 3, 4),
            type = MissionType.MEMORY
        )

        assertTrue(mission.validate("1,2,3,4", data))
    }

    @Test
    fun `MemoryMission validate accepts pattern with spaces`() {
        val mission = MemoryMission(MissionDifficulty.EASY)
        val data = MissionData(
            question = "Remember: 1 - 2 - 3 - 4",
            answer = "1,2,3,4",
            pattern = listOf(1, 2, 3, 4),
            type = MissionType.MEMORY
        )

        assertTrue(mission.validate("1, 2, 3, 4", data))
        assertTrue(mission.validate(" 1 , 2 , 3 , 4 ", data))
    }

    @Test
    fun `MemoryMission validate rejects wrong pattern`() {
        val mission = MemoryMission(MissionDifficulty.EASY)
        val data = MissionData(
            question = "Remember: 1 - 2 - 3 - 4",
            answer = "1,2,3,4",
            pattern = listOf(1, 2, 3, 4),
            type = MissionType.MEMORY
        )

        assertFalse(mission.validate("1,2,3,5", data))
        assertFalse(mission.validate("4,3,2,1", data))
    }

    @Test
    fun `MemoryMission validate rejects incomplete pattern`() {
        val mission = MemoryMission(MissionDifficulty.EASY)
        val data = MissionData(
            question = "Remember: 1 - 2 - 3 - 4",
            answer = "1,2,3,4",
            pattern = listOf(1, 2, 3, 4),
            type = MissionType.MEMORY
        )

        assertFalse(mission.validate("1,2,3", data))
    }

    @Test
    fun `MemoryMission validate handles invalid number gracefully`() {
        val mission = MemoryMission(MissionDifficulty.EASY)
        val data = MissionData(
            question = "Remember: 1 - 2 - 3 - 4",
            answer = "1,2,3,4",
            pattern = listOf(1, 2, 3, 4),
            type = MissionType.MEMORY
        )

        assertFalse(mission.validate("a,b,c,d", data))
    }

    @Test
    fun `MemoryMission generate produces pattern of correct length for EASY`() {
        val mission = MemoryMission(MissionDifficulty.EASY)
        val data = mission.generate()

        assertEquals(4, data.pattern.size)
        assertTrue(data.pattern.all { it in 1..4 })
    }

    @Test
    fun `MemoryMission generate produces pattern of correct length for MEDIUM`() {
        val mission = MemoryMission(MissionDifficulty.MEDIUM)
        val data = mission.generate()

        assertEquals(6, data.pattern.size)
        assertTrue(data.pattern.all { it in 1..4 })
    }

    @Test
    fun `MemoryMission generate produces pattern of correct length for HARD`() {
        val mission = MemoryMission(MissionDifficulty.HARD)
        val data = mission.generate()

        assertEquals(8, data.pattern.size)
        assertTrue(data.pattern.all { it in 1..4 })
    }

    // ==================== TYPING MISSION TESTS ====================

    @Test
    fun `TypingMission validate accepts exact phrase match`() {
        val mission = TypingMission(MissionDifficulty.EASY)
        val data = MissionData(
            question = "Type exactly:",
            answer = "Time to wake up and seize the day",
            type = MissionType.TYPING
        )

        assertTrue(mission.validate("Time to wake up and seize the day", data))
    }

    @Test
    fun `TypingMission validate is case insensitive`() {
        val mission = TypingMission(MissionDifficulty.EASY)
        val data = MissionData(
            question = "Type exactly:",
            answer = "Time to wake up and seize the day",
            type = MissionType.TYPING
        )

        assertTrue(mission.validate("TIME TO WAKE UP AND SEIZE THE DAY", data))
        assertTrue(mission.validate("time to wake up and seize the day", data))
        assertTrue(mission.validate("TiMe To WaKe Up AnD sEiZe ThE dAy", data))
    }

    @Test
    fun `TypingMission validate trims whitespace`() {
        val mission = TypingMission(MissionDifficulty.EASY)
        val data = MissionData(
            question = "Type exactly:",
            answer = "Time to wake up and seize the day",
            type = MissionType.TYPING
        )

        assertTrue(mission.validate("  Time to wake up and seize the day  ", data))
    }

    @Test
    fun `TypingMission validate rejects wrong phrase`() {
        val mission = TypingMission(MissionDifficulty.EASY)
        val data = MissionData(
            question = "Type exactly:",
            answer = "Time to wake up and seize the day",
            type = MissionType.TYPING
        )

        assertFalse(mission.validate("Time to wake up", data))
        assertFalse(mission.validate("wrong phrase", data))
    }

    @Test
    fun `TypingMission generate produces phrase from correct pool for EASY`() {
        val mission = TypingMission(MissionDifficulty.EASY)
        val easyPhrases = listOf(
            "Time to wake up and seize the day",
            "Rise and shine, it's a beautiful morning",
            "Success begins with getting out of bed"
        )

        // Generate multiple times to verify pool
        repeat(10) {
            val data = mission.generate()
            assertTrue(
                "Generated phrase should be from easy pool",
                easyPhrases.any { data.answer.equals(it, ignoreCase = true) }
            )
        }
    }

    // ==================== SHAKE MISSION TESTS ====================

    @Test
    fun `ShakeMission validate accepts exact shake count`() {
        val mission = ShakeMission(MissionDifficulty.EASY)
        val data = MissionData(
            question = "Shake your phone 20 times in 15 seconds!",
            answer = "20",
            type = MissionType.SHAKE,
            metadata = mapOf("timeLimit" to "15")
        )

        assertTrue(mission.validate("20", data))
    }

    @Test
    fun `ShakeMission validate accepts higher shake count`() {
        val mission = ShakeMission(MissionDifficulty.EASY)
        val data = MissionData(
            question = "Shake your phone 20 times in 15 seconds!",
            answer = "20",
            type = MissionType.SHAKE
        )

        assertTrue(mission.validate("25", data))
        assertTrue(mission.validate("50", data))
    }

    @Test
    fun `ShakeMission validate rejects insufficient shakes`() {
        val mission = ShakeMission(MissionDifficulty.EASY)
        val data = MissionData(
            question = "Shake your phone 20 times in 15 seconds!",
            answer = "20",
            type = MissionType.SHAKE
        )

        assertFalse(mission.validate("19", data))
        assertFalse(mission.validate("15", data))
        assertFalse(mission.validate("0", data))
    }

    @Test
    fun `ShakeMission validate handles invalid input`() {
        val mission = ShakeMission(MissionDifficulty.EASY)
        val data = MissionData(
            question = "Shake your phone 20 times in 15 seconds!",
            answer = "20",
            type = MissionType.SHAKE
        )

        assertFalse(mission.validate("abc", data))
        assertFalse(mission.validate("", data))
    }

    @Test
    fun `ShakeMission generate produces correct requirements for EASY`() {
        val mission = ShakeMission(MissionDifficulty.EASY)
        val data = mission.generate()

        assertEquals("20", data.answer)
        assertEquals("15", data.metadata["timeLimit"])
    }

    @Test
    fun `ShakeMission generate produces correct requirements for MEDIUM`() {
        val mission = ShakeMission(MissionDifficulty.MEDIUM)
        val data = mission.generate()

        assertEquals("35", data.answer)
        assertEquals("12", data.metadata["timeLimit"])
    }

    @Test
    fun `ShakeMission generate produces correct requirements for HARD`() {
        val mission = ShakeMission(MissionDifficulty.HARD)
        val data = mission.generate()

        assertEquals("50", data.answer)
        assertEquals("10", data.metadata["timeLimit"])
    }

    // ==================== MISSION FACTORY TESTS ====================

    @Test
    fun `MissionFactory creates correct mission types`() {
        assertTrue(MissionFactory.createMission(MissionType.MATH, MissionDifficulty.EASY) is MathMission)
        assertTrue(MissionFactory.createMission(MissionType.MEMORY, MissionDifficulty.EASY) is MemoryMission)
        assertTrue(MissionFactory.createMission(MissionType.TYPING, MissionDifficulty.EASY) is TypingMission)
        assertTrue(MissionFactory.createMission(MissionType.SHAKE, MissionDifficulty.EASY) is ShakeMission)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `MissionFactory throws for PHOTO mission`() {
        MissionFactory.createMission(MissionType.PHOTO, MissionDifficulty.EASY)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `MissionFactory throws for BARCODE mission`() {
        MissionFactory.createMission(MissionType.BARCODE, MissionDifficulty.EASY)
    }

    @Test
    fun `MissionFactory preserves difficulty level`() {
        val easyMath = MissionFactory.createMission(MissionType.MATH, MissionDifficulty.EASY)
        val mediumMath = MissionFactory.createMission(MissionType.MATH, MissionDifficulty.MEDIUM)
        val hardMath = MissionFactory.createMission(MissionType.MATH, MissionDifficulty.HARD)

        assertEquals(MissionDifficulty.EASY, easyMath.difficulty)
        assertEquals(MissionDifficulty.MEDIUM, mediumMath.difficulty)
        assertEquals(MissionDifficulty.HARD, hardMath.difficulty)
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    fun `all mission types return correct type identifier`() {
        assertEquals(MissionType.MATH, MathMission(MissionDifficulty.EASY).type)
        assertEquals(MissionType.MEMORY, MemoryMission(MissionDifficulty.EASY).type)
        assertEquals(MissionType.TYPING, TypingMission(MissionDifficulty.EASY).type)
        assertEquals(MissionType.SHAKE, ShakeMission(MissionDifficulty.EASY).type)
    }

    @Test
    fun `all mission types return non-empty description`() {
        assertTrue(MathMission(MissionDifficulty.EASY).getDescription().isNotBlank())
        assertTrue(MemoryMission(MissionDifficulty.EASY).getDescription().isNotBlank())
        assertTrue(TypingMission(MissionDifficulty.EASY).getDescription().isNotBlank())
        assertTrue(ShakeMission(MissionDifficulty.EASY).getDescription().isNotBlank())
    }

    @Test
    fun `MathMission validate with empty input returns false`() {
        val mission = MathMission(MissionDifficulty.EASY)
        val data = MissionData("5 + 3 = ?", "8", type = MissionType.MATH)

        assertFalse(mission.validate("", data))
    }

    @Test
    fun `MemoryMission validate with empty input returns false`() {
        val mission = MemoryMission(MissionDifficulty.EASY)
        val data = MissionData(
            question = "Remember: 1 - 2 - 3 - 4",
            answer = "1,2,3,4",
            pattern = listOf(1, 2, 3, 4),
            type = MissionType.MEMORY
        )

        assertFalse(mission.validate("", data))
    }

    @Test
    fun `ShakeMission validate defaults to requiring 20 shakes when answer invalid`() {
        val mission = ShakeMission(MissionDifficulty.EASY)
        val data = MissionData(
            question = "Shake your phone!",
            answer = "invalid",
            type = MissionType.SHAKE
        )

        // When answer can't be parsed, default should be 20
        assertFalse(mission.validate("15", data))
        assertFalse(mission.validate("19", data))
    }
}
