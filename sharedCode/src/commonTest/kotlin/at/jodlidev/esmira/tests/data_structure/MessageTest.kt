package tests.data_structure

import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.data_structure.*
import BaseCommonTest
import kotlin.test.*

/**
 * Created by JodliDev on 31.03.2022.
 */
class MessageTest : BaseCommonTest() {
	private val testMessage = "I'm your space stepmom!"
	
	@Test
	fun save() {
		val message = createMessage()
		message.save()
		assertSqlWasSaved(Message.TABLE, Message.KEY_CONTENT, testMessage)
	}
	
	@Test
	fun markAsRead() {
		val message = createMessage()
		message.markAsRead()
		assertSqlWasUpdated(Message.TABLE, Message.KEY_IS_NEW, 0)
	}
	
	@Test
	fun addMessage() {
		Message.addMessage(getBaseStudyId(), testMessage, NativeLink.getNowMillis(), true)
		assertSqlWasSaved(Message.TABLE, Message.KEY_CONTENT, testMessage)
		assertSqlWasSaved(Message.TABLE, Message.KEY_FROM_SERVER, 1)
		assertSqlWasSaved(Message.TABLE, Message.KEY_IS_NEW, 1)
		
		Message.addMessage(getBaseStudyId(), testMessage, NativeLink.getNowMillis(), false)
		assertSqlWasSaved(Message.TABLE, Message.KEY_CONTENT, testMessage)
		assertSqlWasSaved(Message.TABLE, Message.KEY_FROM_SERVER, 0)
		assertSqlWasSaved(Message.TABLE, Message.KEY_IS_NEW, 0)
	}
}