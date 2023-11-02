package com.practice.testing_practice

import com.google.common.truth.Truth.assertThat
import org.junit.Test


class RegistrationUtilTest {

    @Test
    fun `empty username returns false`() {
        val result = RegistrationUtil.validateRegistrationInput(
            userName = "",
            password = "123", // something valid... more than 2 digits
            confirmedPassword = "123" // should be valid
        ) // should return false

        assertThat(result).isFalse() // false여야 테스트 통과
    }

    @Test
    fun `valid username and correctly repeated password return true`() {
        val result = RegistrationUtil.validateRegistrationInput(
            userName = "kmkim", // something valid
            password = "123", // something valid... more than 2 digits
            confirmedPassword = "123" // should be valid
        ) // should return true

        assertThat(result).isTrue() // true여야 테스트 통과
    }

    @Test
    fun `username already exists returns false`() {
        val result = RegistrationUtil.validateRegistrationInput(
            userName = "Peter", // something invalid
            password = "123", // something valid... more than 2 digits
            confirmedPassword = "123" // should be valid
        ) // should return false

        assertThat(result).isFalse() // false여야 테스트 통과
    }

    @Test
    fun `empty password returns false`() {
        val result = RegistrationUtil.validateRegistrationInput(
            userName = "kmkim",
            password = "", // something invalid
            confirmedPassword = "" // should be invalid
        ) // should return false

        assertThat(result).isFalse() // false여야 테스트 통과
    }

    @Test
    fun `incorrectly repeated password returns false`() {
        val result = RegistrationUtil.validateRegistrationInput(
            userName = "kmkim",
            password = "133",
            confirmedPassword = "123" // should be invalid
        ) // should return false

        assertThat(result).isFalse() // false여야 테스트 통과
    }

    @Test
    fun `password less than two digits returns false`() {
        val result = RegistrationUtil.validateRegistrationInput(
            userName = "kmkim",
            password = "abc1", // something invalid
            confirmedPassword = "abc1" // should be invalid
        ) // should return false

        assertThat(result).isFalse() // false여야 테스트 통과
    }
}