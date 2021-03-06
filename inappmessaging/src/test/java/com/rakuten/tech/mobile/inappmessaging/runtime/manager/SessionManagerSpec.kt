package com.rakuten.tech.mobile.inappmessaging.runtime.manager

import android.content.Context
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.AppStartEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.PurchaseSuccessfulEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.ValidTestMessage
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.*
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.config.ConfigResponseData
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.config.ConfigResponseEndpoints
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.SessionManager.onSessionUpdate
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.ExecutionException

/**
 * Test class for SessionManager.
 */
@RunWith(RobolectricTestRunner::class)
class SessionManagerSpec : BaseTest() {

    private var configResponseData = Mockito.mock(ConfigResponseData::class.java)
    private var endpoints = Mockito.mock(ConfigResponseEndpoints::class.java)
    private val message = ValidTestMessage()

    @Before
    override fun setup() {
        super.setup()
        LocalEventRepository.instance().clearEvents()
    }

    @Test
    @Throws(ExecutionException::class, InterruptedException::class)
    fun `should start ping message mixer on log in successful`() {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(
                ApplicationProvider.getApplicationContext<Context>().contentResolver,
                Settings.Secure.ANDROID_ID,
                "test_device_id")
        InAppMessaging.initialize(ApplicationProvider.getApplicationContext(), true)
        When calling configResponseData.rollOutPercentage itReturns 100
        When calling configResponseData.endpoints itReturns endpoints
        ConfigResponseRepository.instance().addConfigResponse(configResponseData)
        onSessionUpdate()
        WorkManager.getInstance(ApplicationProvider.getApplicationContext())
                .getWorkInfosByTag(MESSAGE_MIXER_PING_WORKER).get().shouldHaveSize(1)
    }

    @Test
    fun `should clear repositories with null event`() {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(
                ApplicationProvider.getApplicationContext<Context>().contentResolver,
                Settings.Secure.ANDROID_ID,
                "test_device_id")
        InAppMessaging.initialize(ApplicationProvider.getApplicationContext(), true)
        When calling configResponseData.rollOutPercentage itReturns 0

        addTestData()
        ConfigResponseRepository.instance().addConfigResponse(configResponseData)

        onSessionUpdate()
        verifyTestData(0)
    }

    @Test
    fun `should clear repositories with valid event`() {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(
                ApplicationProvider.getApplicationContext<Context>().contentResolver,
                Settings.Secure.ANDROID_ID,
                "test_device_id")
        InAppMessaging.initialize(ApplicationProvider.getApplicationContext(), true)
        When calling configResponseData.rollOutPercentage itReturns 0

        addTestData()
        ConfigResponseRepository.instance().addConfigResponse(configResponseData)

        onSessionUpdate(PurchaseSuccessfulEvent())
        verifyTestData(1)
    }

    @Test
    fun `should clear repositories with valid persistent event`() {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(
                ApplicationProvider.getApplicationContext<Context>().contentResolver,
                Settings.Secure.ANDROID_ID,
                "test_device_id")
        InAppMessaging.initialize(ApplicationProvider.getApplicationContext(), true)
        When calling configResponseData.rollOutPercentage itReturns 0

        addTestData()
        ConfigResponseRepository.instance().addConfigResponse(configResponseData)

        onSessionUpdate(AppStartEvent())
        verifyTestData(0)
    }

    private fun addTestData() {
        // Add messages
        val messageList = ArrayList<Message>()
        messageList.add(message)
        PingResponseMessageRepository.instance().replaceAllMessages(messageList)
        ReadyForDisplayMessageRepository.instance().replaceAllMessages(messageList)
        LocalDisplayedMessageRepository.instance().addMessage(message)
        LocalOptedOutMessageRepository.instance().addMessage(message)
    }

    private fun verifyTestData(expected: Int) {
        PingResponseMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(0)
        ReadyForDisplayMessageRepository.instance().getAllMessagesCopy()shouldHaveSize(0)
        LocalDisplayedMessageRepository.instance().numberOfTimesDisplayed(message) shouldBeEqualTo 0
        LocalOptedOutMessageRepository.instance().hasMessage(message.getCampaignId()).shouldBeFalse()
        LocalEventRepository.instance().getEvents().shouldHaveSize(expected)
    }

    companion object {
        private const val MESSAGE_MIXER_PING_WORKER = "iam_message_mixer_worker"
    }
}
