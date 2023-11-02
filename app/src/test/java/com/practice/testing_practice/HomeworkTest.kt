package com.practice.testing_practice

import com.google.common.truth.Truth.assertThat
import org.junit.Test


class HomeworkTest {

    @Test
    fun fib_inputZero_returnsZero() {
        val result = Homework.fib(0)
        assertThat(result).isEqualTo(0)
    }


    @Test
    fun fib_inputOne_returnsOne() {
        val result = Homework.fib(1)
        assertThat(result).isEqualTo(1)
    }


    @Test
    fun fib_inputTwo_returnsOne() {
        val result = Homework.fib(2)
        assertThat(result).isEqualTo(1)
    }

    // edge : 2(0과 1은 값이 정해져 있는 반면 2'부터' 별도로 계산이 필요
    @Test
    fun fib_inputThree_returnsTwo() {
        val result = Homework.fib(3)
        assertThat(result).isEqualTo(2)
    }

    @Test
    fun fib_input10_returns34() {
        val result = Homework.fib(10)
        assertThat(result).isEqualTo(34)
    }

    @Test
    fun checkBraces_inputOneOpen_returnsFalse() {
        val result = Homework.checkBraces("(abc")
        assertThat(result).isFalse()
    }

    @Test
    fun checkBraces_inputOneClose_returnsFalse() {
        val result = Homework.checkBraces("abc)")
        assertThat(result).isFalse()
    }

    // edge : 괄호의 순서가 변경된 경우
    @Test
    fun checkBraces_inputOnePairIncorrectlyOrdered_returnsFalse() {
        val result = Homework.checkBraces(")kmkim(")
        assertThat(result).isFalse()
    }

    @Test
    fun checkBraces_inputOneOpenTwoClose_returnsFalse() {
        val result = Homework.checkBraces("(kmkim))")
        assertThat(result).isFalse()
    }

    @Test
    fun checkBraces_inputCorrectOnePair_returnsTrue() {
        val result = Homework.checkBraces("(kmkim)")
        assertThat(result).isTrue()
    }

    @Test
    fun checkBraces_inputCorrectTwoPairs_returnsTrue() {
        val result = Homework.checkBraces("(k(mk)im)")
        assertThat(result).isTrue()
    }
}
