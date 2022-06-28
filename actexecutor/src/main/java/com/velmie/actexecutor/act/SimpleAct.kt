package com.velmie.actexecutor.act

class SimpleAct(
    override val id: Id,
    override val actPolicy: ActPolicy = ActPolicy.DEFAULT,
    val actFunction: () -> Unit
) : Act
