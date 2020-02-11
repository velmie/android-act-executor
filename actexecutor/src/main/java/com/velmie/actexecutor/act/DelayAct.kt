package com.velmie.actexecutor.act

private const val DEFAULT_DELAY_IN_MILLIS = 200

class DelayAct(
    override val id: Id,
    val delay: Int = DEFAULT_DELAY_IN_MILLIS,
    val actFunction: () -> Unit
) : Act
