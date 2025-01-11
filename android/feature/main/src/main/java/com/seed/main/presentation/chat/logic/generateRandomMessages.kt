package com.seed.main.presentation.chat.logic

import java.time.LocalDateTime
import kotlin.random.Random

fun generateRandomMessages(): List<Message> {
    val authors = listOf("Alice", "Bob", "Charlie", "Diana", "Eve", "Frank")
    val messages = listOf(
        "Hello!",
        "How are you doing?",
        "Can we meet tomorrow?",
        "That's great to hear.",
        "I’ll let you know soon.",
        "Let’s catch up later.",
        "What’s the update?",
        "I’m running a bit late.",
        "Got it!",
        "Thanks a lot.",
        "У розектед иногда обзоры на редкие девайсы выходят прикольные",
        "make зачем great again",
        "зачем марков девочка шабачка?",
        "И это гуй уже multiplatform? Тогда волей не волей придется абстракцию строить. И только один шаг чтоб выделить отдельной либой, не так ли?",
        """
            Окей. Тогда ktor - виртуализация http клиента и сервера :)

            Я отталкиваюсь от java.awt (abstract widget toolkit) и hal(hardware abstraction layer) твой случае с воспроизведением звука где-то между ними.

            Виртуализация у меня больше соотноситься с железом и виртуальными машинами.

            Ладно. Это офтоп, если хочешь продолжить зови в флудилку, ок?
        """.trimIndent()
    )

    return List(Random.nextInt(0, 50)) {
        Message.OthersMessage(
            nonce = Random.nextInt(),
            authorName = authors[Random.nextInt(authors.size)],
            messageText = messages[Random.nextInt(messages.size)],
            dateTime = LocalDateTime.now()
                .minusDays(Random.nextInt(30).toLong())
                .minusHours(Random.nextInt(24).toLong())
                .minusMinutes(Random.nextInt(60).toLong())
        )
    }
}