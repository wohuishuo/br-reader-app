package com.bookrealm.reader.data.remote.dto

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadingInteractionDtoTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Test
    fun paragraphInteraction_shouldMatchBackendContract() {
        val payload = """
            {
              "paragraphId": 2,
              "marks": [
                {
                  "id": 10,
                  "userId": 1,
                  "bookId": 1,
                  "chapterId": 1,
                  "paragraphId": 2,
                  "paragraphSeq": 2,
                  "markType": "note",
                  "note": "重要"
                }
              ],
              "comments": [
                {
                  "id": 20,
                  "userId": 1,
                  "bookId": 1,
                  "chapterId": 1,
                  "paragraphId": 2,
                  "paragraphSeq": 2,
                  "content": "这是一条段评",
                  "likeCount": 3,
                  "likedByMe": true
                }
              ]
            }
        """.trimIndent()

        val dto = json.decodeFromString<ParagraphInteractionDto>(payload)

        assertEquals(2L, dto.paragraphId)
        assertEquals("重要", dto.marks.single().note)
        assertEquals("这是一条段评", dto.comments.single().content)
        assertEquals(3L, dto.comments.single().likeCount)
        assertTrue(dto.comments.single().likedByMe)
    }

    @Test
    fun saveCommentRequest_shouldUseBackendFieldNames() {
        val request = SaveCommentRequest(
            userId = 1,
            bookId = 1,
            chapterId = 1,
            paragraphId = 2,
            content = "公开段评",
        )

        val encoded = json.encodeToString(request)

        assertTrue(encoded.contains("\"userId\":1"))
        assertTrue(encoded.contains("\"paragraphId\":2"))
        assertTrue(encoded.contains("\"content\":\"公开段评\""))
    }
}
