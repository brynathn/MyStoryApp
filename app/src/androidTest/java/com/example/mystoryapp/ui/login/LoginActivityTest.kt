package com.example.mystoryapp.ui.login

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mystoryapp.R
import com.example.mystoryapp.UserPreferences
import com.example.mystoryapp.di.dataStore
import com.example.mystoryapp.ui.wrapEspressoIdlingResource
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class LoginActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

    private lateinit var userPreferences: UserPreferences

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dataStore = UserPreferences.getInstance(context.dataStore)

        userPreferences = dataStore

        runBlocking {
            userPreferences.clearUserToken()
        }
    }

    @Test
    fun testALoginSuccess_whenAlreadyLoggedIn() {
        wrapEspressoIdlingResource {
            onView(withId(R.id.rv_stories)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun testBLoginSuccess_whenNotLoggedIn() {
        wrapEspressoIdlingResource {
            onView(withId(R.id.ed_login_email)).perform(typeText("bry@gmail.com"), closeSoftKeyboard())
            onView(withId(R.id.ed_login_password)).perform(typeText("bryan8899"), closeSoftKeyboard())

            onView(withId(R.id.btn_login)).perform(click())

            Thread.sleep(8000)

            onView(withId(R.id.rv_stories)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun testZLogout() {
        wrapEspressoIdlingResource {
            onView(withId(R.id.rv_stories)).check(matches(isDisplayed()))

            openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext())

            onView(withText(R.string.logout)).perform(click())

            onView(withId(R.id.ed_login_email)).check(matches(isDisplayed()))
        }
    }
}







