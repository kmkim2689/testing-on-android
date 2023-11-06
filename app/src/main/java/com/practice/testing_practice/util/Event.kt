package com.practice.testing_practice.util

open class Event<out T>(private val content: T) {

    // use this class to make the livedata emit one-time event
    // livedata가 Error Resource를 발행한 경우, configuration change가 발생하였을 경우 다시 한번 error 데이터가 발행된다.
    // 이러한 오류를 막기 위하여 활용 -> null을 리턴

    var hasBeenHandled = false
        private set

    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    // return the content even if it has already been handled
    fun peekContent(): T = content
}