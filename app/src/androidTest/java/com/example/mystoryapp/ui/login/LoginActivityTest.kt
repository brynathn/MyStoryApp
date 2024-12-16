package com.example.mystoryapp.ui.login

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mystoryapp.R
import com.example.mystoryapp.ui.wrapEspressoIdlingResource
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.AdditionalMatchers.not

@RunWith(AndroidJUnit4::class)
class LoginActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

    @Test
    fun testLoginSuccess() {
        wrapEspressoIdlingResource {
            // Ensure email input is visible
            onView(withId(R.id.ed_login_email)).check(matches(isDisplayed()))
            onView(withId(R.id.ed_login_email)).perform(typeText("valid@example.com"), closeSoftKeyboard())

            // Ensure password input is visible
            onView(withId(R.id.ed_login_password)).check(matches(isDisplayed()))
            onView(withId(R.id.ed_login_password)).perform(typeText("validpassword"), closeSoftKeyboard())

            // Ensure login button is visible
            onView(withId(R.id.btn_login)).check(matches(isDisplayed()))
            onView(withId(R.id.btn_login)).perform(click())

            onView(withId(R.id.loadingContainer)).check(matches(not(isDisplayed())))

            onView(withId(R.id.rv_stories)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun testLoginFailure() {
        wrapEspressoIdlingResource {
            onView(withId(R.id.ed_login_email)).perform(typeText("invalid@example.com"), closeSoftKeyboard())
            onView(withId(R.id.ed_login_password)).perform(typeText("wrongpassword"), closeSoftKeyboard())

            onView(withId(R.id.btn_login)).check(matches(isDisplayed())).perform(click())

            onView(withId(R.id.loadingContainer)).check(matches(not(isDisplayed())))

            onView(withText(R.string.login_failed)).check(matches(isDisplayed()))
        }
    }
}

