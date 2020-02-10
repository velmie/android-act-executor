package com.velmie.actexecutor.act

class SimpleAct(override val id: Id, override val delay: Int, val actFunction: () -> Unit) : Act
