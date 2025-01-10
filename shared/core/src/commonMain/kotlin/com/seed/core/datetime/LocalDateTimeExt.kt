package com.seed.core.datetime

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

public fun LocalDateTime.Companion.now(): LocalDateTime =
    Clock.System.now().zonedLocalDateTime()

public fun Instant.zonedLocalDateTime(): LocalDateTime =
    toLocalDateTime(TimeZone.currentSystemDefault())