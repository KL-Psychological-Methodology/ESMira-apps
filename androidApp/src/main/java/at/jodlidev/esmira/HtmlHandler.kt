package at.jodlidev.esmira

import android.content.Context
import android.graphics.Color
import android.text.Layout
import android.text.Spanned
import android.text.style.*
import android.widget.TextView
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import io.noties.markwon.*
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.core.spans.LastLineSpacingSpan
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.html.CssInlineStyleParser
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.html.HtmlTag
import io.noties.markwon.html.tag.SimpleTagHandler
import io.noties.markwon.image.ImagesPlugin
import io.noties.markwon.image.network.OkHttpNetworkSchemeHandler
import io.noties.markwon.linkify.LinkifyPlugin
import org.commonmark.node.Heading


/**
 * Created by JodliDev on 24.06.2021.
 */

class HtmlHandler {
	companion object {
		private fun getParser(context: Context): Markwon {
			return Markwon.builder(context)
				.usePlugin(object: AbstractMarkwonPlugin() {
					override fun configureTheme(builder: MarkwonTheme.Builder) {
						builder
							.headingBreakHeight(0)
					}
				})
				.usePlugin(object:AbstractMarkwonPlugin() {
					override fun configureSpansFactory(builder:  MarkwonSpansFactory.Builder) {
						builder.prependFactory(Heading::class.java) { _, _ -> LastLineSpacingSpan(50) }
					}
				})
				.usePlugin(HtmlPlugin.create { plugin ->
					plugin.addHandler(object: SimpleTagHandler() {
						override fun supportedTags(): MutableCollection<String> {
							return arrayListOf("mark")
						}
						override fun getSpans(configuration: MarkwonConfiguration, renderProps: RenderProps, tag: HtmlTag): Any {
							return BackgroundColorSpan(Color.YELLOW)
						}
					})
					plugin.addHandler(object: SimpleTagHandler() {
						//Thanks to: https://github.com/noties/Markwon/issues/94
						private val styleParser = CssInlineStyleParser.create()
						override fun supportedTags(): MutableCollection<String> {
							return arrayListOf("div")
						}
						override fun getSpans(configuration: MarkwonConfiguration, renderProps: RenderProps, tag: HtmlTag): Any? {
							val style = tag.attributes()["style"]
								?: return null
							
							for(property in styleParser.parse(style)) {
								if(property.key() == "text-align") {
									println(property.value())
									return AlignmentSpan.Standard(when(property.value()) {
										"left" -> Layout.Alignment.ALIGN_NORMAL
										"center" -> Layout.Alignment.ALIGN_CENTER
										"right" -> Layout.Alignment.ALIGN_OPPOSITE
										else -> Layout.Alignment.ALIGN_NORMAL
									})
								}
							}
							return null
						}
					})
//					plugin.addHandler(plugin.getHandler("p")!!)
					
//					plugin.addHandler(object: HeadingHandler() {
//						override fun getSpans(configuration: MarkwonConfiguration, renderProps: RenderProps, tag: HtmlTag): Any {
//							val span = super.getSpans(configuration, renderProps, tag)
//							val factory = SpanFactory()
//							return LastLineSpacingSpan(50)
//						}
//					})
				})
				.usePlugin(ImagesPlugin.create { plugin ->
					plugin.addSchemeHandler(OkHttpNetworkSchemeHandler.create())
				})
				.usePlugin(StrikethroughPlugin.create())
				.usePlugin(LinkifyPlugin.create())
//				.usePlugin(SoftBreakAddsNewLinePlugin.create())
				.build()
		}
		fun fromHtml(html: String, el: TextView): Spanned {
			val parser = getParser(el.context)
			return parser.toMarkdown(html)
		}
		fun fromHtml(html: String, context: Context): Spanned {
			val parser = getParser(context)
			return parser.toMarkdown(html)
		}
		fun setHtml(html: String, el: TextView) {
			val parser = getParser(el.context)
			parser.setMarkdown(el, html)
		}

		@Composable
		fun HtmlText(html: String, modifier: Modifier = Modifier) {
			val color = LocalContentColor.current
			Column(modifier = modifier) {
//				AndroidView(factory = { context ->
//					TextView(context).apply {
//						this.setTextColor(color.toArgb())
//						this.text = html
//					}
//				})
				AndroidView(factory = { context ->
					TextView(context).apply {
						this.setTextColor(color.toArgb())
						setHtml(html, this)
					}
				})
			}
		}
	}
}