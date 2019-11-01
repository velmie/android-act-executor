package com.velmie.actexecutor.act

import com.velmie.actexecutor.act.Act
import com.velmie.actexecutor.act.Id

class SimpleAct(override val id: Id, val actFunction: () -> Unit) : Act
