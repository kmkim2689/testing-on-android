package com.practice.testing_practice

object RegistrationUtil {

    private val existingUsers = listOf("Peter", "Carl")

    /**
    * the input is not valid => Test Cases
     * if the username/password is empty
     * if the username is already taken
     * if the confirmed password is not the same as the real password
     * the password contains less than 2 digits
    */

    fun validateRegistrationInput(
        userName: String,
        password: String,
        confirmedPassword: String
    ): Boolean {
        if (userName.isEmpty() || password.isEmpty()) {
            return false
        }

        if (userName in existingUsers) {
            return false
        }

        if (password != confirmedPassword) {
            return false
        }

        if (password.count() { it.isDigit() } < 2) {
            return false
        }

        return true
    }
}