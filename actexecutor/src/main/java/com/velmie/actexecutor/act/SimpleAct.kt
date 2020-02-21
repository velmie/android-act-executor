package com.velmie.actexecutor.act

class SimpleAct(override val id: Id, val actFunction: () -> Unit) : Act
