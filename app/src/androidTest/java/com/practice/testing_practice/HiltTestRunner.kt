package com.practice.testing_practice

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
        // 본래 className에는 실제 앱애서 사용하는 Application 클래스명을 할당
        // 하지만, HiltTestApplication의 클래스명을 전달함
    }
}