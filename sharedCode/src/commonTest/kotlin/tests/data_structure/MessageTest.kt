package tests.data_structure

import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.data_structure.*
import kotlin.test.*

/**
 * Created by JodliDev on 31.03.2022.
 */
class MessageTest : BaseDataStructureTest() {
	private val testMessage = "I'm your space stepmom!"
	
	@Test
	fun save() {
		val message = createMessage()
		message.save()
		mockTools.assertSqlWasSaved(Message.TABLE, Message.KEY_CONTENT, testMessage)
	}
	
	@Test
	fun markAsRead() {
		val message = createMessage()
		message.markAsRead()
		mockTools.assertSqlWasUpdated(Message.TABLE, Message.KEY_IS_NEW, false)
	}
	
	@Test
	fun addMessage() {
		Message.addMessage(getBaseStudyId(), testMessage, NativeLink.getNowMillis(), true)
		mockTools.assertSqlWasSaved(Message.TABLE, Message.KEY_CONTENT, testMessage)
		mockTools.assertSqlWasSaved(Message.TABLE, Message.KEY_FROM_SERVER, true)
		mockTools.assertSqlWasSaved(Message.TABLE, Message.KEY_IS_NEW, true)
		
		Message.addMessage(getBaseStudyId(), testMessage, NativeLink.getNowMillis(), false)
		mockTools.assertSqlWasSaved(Message.TABLE, Message.KEY_CONTENT, testMessage)
		mockTools.assertSqlWasSaved(Message.TABLE, Message.KEY_FROM_SERVER, false)
		mockTools.assertSqlWasSaved(Message.TABLE, Message.KEY_IS_NEW, false)
	}
}