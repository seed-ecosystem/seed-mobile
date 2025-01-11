package com.seed.main.presentation.chatlist.logic

import java.time.LocalDateTime
import kotlin.random.Random

fun generateRandomChats(): List<ChatListItem> {
    val messages = listOf(
        "Hey, how's it going?",
        "What are you up to?",
        "Don't forget the meeting tomorrow.",
        "Had a great time today!",
        "Let me know when you're free.",
        "See you soon!",
        "Thanks for your help.",
        "Got it, thanks.",
        "Looking forward to it!",
        "Can you call me back?",
        "Лидер консервативной партии Японии Наоки Хякута предложил запретить девушкам после 25 лет выходить замуж. По мнению чиновника, это поможет стимулировать рождаемость.",
        "Политик Наоки Хякута считает, что это подтолкнет девушек раньше становиться матерями.",
        "Лучше вообще весь стейт в кеше танстак квери держать",
        "Даже если бы эти функции были агеостикамт ничего бы это не решило))"
    )
    val titles = listOf(
        "John Doe",
        "Jane Smith",
        "Team Chat",
        "Project Updates",
        "Family Group",
        "Work Buddies",
        "Alice",
        "Bob",
        "Саша Сок",
        "Матвей Плохов",
        "Миша Кап",
        "Friends Forever",
        "Travel Plans"
    )

    return List(Random.nextInt(0, 50)) {
        ChatListItem(
            chatId = "${Random.nextInt(Int.MAX_VALUE)}",
            chatName = titles[Random.nextInt(titles.size)],
            lastSentMessageDateTime = LocalDateTime.now()
                .minusDays(Random.nextInt(30).toLong())
                .minusHours(Random.nextInt(24).toLong())
                .minusMinutes(Random.nextInt(60).toLong()),
            lastSentMessageText = messages[Random.nextInt(messages.size)]
        )
    }
}