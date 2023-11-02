package com.practice.testing_practice

import android.content.Context

class ResourceComparer {

    // test에 context를 수반해야 하므로 androidTest 디렉토리에 테스트 클래스 제작
    fun isEqual(context: Context, resId: Int, string: String): Boolean {
        return context.getString(resId) == string
    }
}