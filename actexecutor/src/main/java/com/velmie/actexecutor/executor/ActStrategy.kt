package com.velmie.actexecutor.executor

/**
 *  Strategies that can be used for incoming events.
 *
 *  DEFAULT - Only one event can be stored at the same time.
 *  Save and execute only one event and ignore all new events.
 *  After event is executed, it becomes possible to add new event.
 *
 *  REPLACE - Only one event can be stored at the same time. Replace already added event and ignore all new events.
 *
 *  QUEUE - Several events can be stored at the same time. Add already added and new events to queue.
 */

enum class ActStrategy() {
    DEFAULT, REPLACE, QUEUE
}