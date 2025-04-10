package com.example.criminalintent

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.criminalintent.crimedetail.CrimeDetailFragment
import org.junit.Assert.*

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CrimeDetailFragmentTest {

    private lateinit var scenario: FragmentScenario<CrimeDetailFragment>

    @Before
    fun setUp() {
        scenario = launchFragmentInContainer()
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun isHookedUpCheckBoxAndEditText() {
        onView(withId(R.id.crime_solved)).perform(click())
        onView(withId(R.id.crime_solved)).check(matches(isChecked()))

        scenario.onFragment { fragment ->
            assertTrue(fragment.crime.isSolved)
        }
        onView(withId(R.id.crime_solved)).perform(click())
        scenario.onFragment { fragment ->
            assertFalse(fragment.crime.isSolved)
        }

        onView(withId(R.id.crime_title)).perform(typeText("aba"))
        scenario.onFragment { fragment ->
            assertEquals("aba", fragment.crime.title)
        }

        onView(withId(R.id.crime_title)).perform(clearText())
        onView(withId(R.id.crime_title)).perform(typeText("zxc"))
        scenario.onFragment { fragment ->
            assertEquals("zxc", fragment.crime.title)
        }
    }
}