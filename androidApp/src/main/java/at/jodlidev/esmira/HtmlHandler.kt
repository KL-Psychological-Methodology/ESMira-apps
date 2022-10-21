package at.jodlidev.esmira

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.*
import android.util.Base64
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import at.jodlidev.esmira.sharedCode.nativeAsync
import at.jodlidev.esmira.sharedCode.onUIThread
import net.nightwhistler.htmlspanner.HtmlSpanner
import net.nightwhistler.htmlspanner.TagNodeHandler
import net.nightwhistler.htmlspanner.handlers.*
import org.htmlcleaner.TagNode
import java.net.URL


/**
 * Created by JodliDev on 24.06.2021.
 */


class HtmlHandler {
	class OwnAlignmentHandler(wrapHandler: TagNodeHandler) : AlignmentHandler(wrapHandler) {
		override fun handleTagNode(node: TagNode?, builder: SpannableStringBuilder, start: Int, end: Int) {
			val align = node?.getAttributeByName("style")
			if(align != null) {
				val f = Regex(".*text-align:\\s*([^;]+)(;.*|)$").find(align)
				println(f)
				if(f != null) {
					val (value) = f.destructured
					node.setAttribute("align", value)
				}
			}
			
			super.handleTagNode(node, builder, start, end)
		}
	}
	class SpannableHandler(private val getSpan: () -> CharacterStyle) : TagNodeHandler() {
		override fun handleTagNode(node: TagNode?, builder: SpannableStringBuilder, start: Int, end: Int) {
			builder.setSpan(getSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
		}
		
	}
	class OwnImageHandler(private val el: TextView?, private val context: Context) : ImageHandler() {
		override fun handleTagNode(node: TagNode, builder: SpannableStringBuilder, start: Int, end: Int) {
			val src = node.getAttributeByName("src")
			builder.append("\uFFFC")
			if(src.startsWith("data")) {
				val split = src.indexOf(",", 0, true)
				val imageBytes = Base64.decode(src.substring(split), Base64.DEFAULT)
				val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
				builder.setSpan(ImageSpan(context, bitmap), start, end+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
			}
			else if(el != null) {
				nativeAsync {
					try {
						val bitmap = BitmapFactory.decodeStream(URL(src).openConnection().getInputStream())
						builder.setSpan(ImageSpan(context, bitmap), start, end+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
						
						onUIThread {
							el.text = builder
						}
					}
					catch(e: Throwable) {
						e.printStackTrace()
					}
				}
			}
		}
	}
	
	companion object {
		private lateinit var htmlSpanner: HtmlSpanner
		
		fun init() {
			if(!this::htmlSpanner.isInitialized) {
				htmlSpanner = HtmlSpanner()
				
				htmlSpanner.registerHandler("div", OwnAlignmentHandler(NewLineHandler(1)))
				htmlSpanner.registerHandler("u", SpannableHandler{UnderlineSpan()})
				htmlSpanner.registerHandler("s", SpannableHandler{StrikethroughSpan()})
				htmlSpanner.registerHandler("mark", SpannableHandler{BackgroundColorSpan(Color.YELLOW)})
				htmlSpanner.registerHandler("em", ItalicHandler()) //is bold in library for some reason
				htmlSpanner.registerHandler("strong", BoldHandler()) //is italic in library for some reason
			}
		}
		fun fromHtml(html: String, el: TextView): Spanned {
			init()
			htmlSpanner.registerHandler("img", OwnImageHandler(el, el.context))
			val r = this.fromHtml(html)
			htmlSpanner.unregisterHandler("img") //prevent memory leaks
			return r
		}
		fun fromHtml(html: String, context: Context): Spanned {
			init()
			htmlSpanner.registerHandler("img", OwnImageHandler(null, context))
			val r = this.fromHtml(html)
			htmlSpanner.unregisterHandler("img") //prevent memory leaks
			return r
		}
		fun fromHtml(html: String): Spanned {
			init()
			return htmlSpanner.fromHtml(html).trim() as Spannable
		}
		fun setHtml(html: String, el: TextView) {
			el.text = fromHtml(html, el)
			el.movementMethod = LinkMovementMethod.getInstance()
		}
		
		@Composable
		fun HtmlText(html: String) {
			AndroidView(factory = { context ->
				TextView(context).apply {
					text = fromHtml(html, this)
					movementMethod = LinkMovementMethod.getInstance()
				}
			})
		}
	}
}