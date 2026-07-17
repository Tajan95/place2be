package de.place2be.feature.onboarding

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingViewModelTest {
    @Test
    fun `first launch onboarding is visible until completion is stored`() {
        val store = FakeOnboardingCompletionStore(completed = false)
        val viewModel = OnboardingViewModel(store)

        assertTrue(viewModel.shouldShowOnFirstLaunch())

        viewModel.completeFirstLaunchOnboarding()

        assertTrue(store.completed)
        assertFalse(viewModel.shouldShowOnFirstLaunch())
    }

    @Test
    fun `completed onboarding does not appear again on a later app start`() {
        val store = FakeOnboardingCompletionStore(completed = true)
        val recreatedViewModel = OnboardingViewModel(store)

        assertFalse(recreatedViewModel.shouldShowOnFirstLaunch())
    }

    @Test
    fun `pages explain purpose map criteria age weighting and location`() {
        val viewModel = OnboardingViewModel(FakeOnboardingCompletionStore())
        val pages = viewModel.getPages()
        val completeText = pages.joinToString(separator = " ") { page ->
            listOf(page.eyebrow, page.title, page.body)
                .plus(page.highlights)
                .joinToString(separator = " ")
        }

        assertEquals(4, pages.size)
        assertTrue(completeText.contains("oeffentliche"))
        assertTrue(completeText.contains("Mock-Map"))
        assertTrue(completeText.contains("Vibes"))
        assertTrue(completeText.contains("Sicherheit"))
        assertTrue(completeText.contains("Erreichbarkeit"))
        assertTrue(completeText.contains("staerker"))
        assertTrue(completeText.contains("Standort"))
    }

    private class FakeOnboardingCompletionStore(
        var completed: Boolean = false,
    ) : OnboardingCompletionStore {
        override fun isCompleted(): Boolean = completed

        override fun markCompleted() {
            completed = true
        }
    }
}
