package at.jodlidev.esmira

import android.text.*
import net.nightwhistler.htmlspanner.HtmlSpanner
import net.nightwhistler.htmlspanner.handlers.AlignmentHandler
import net.nightwhistler.htmlspanner.handlers.NewLineHandler

/**
 * Created by JodliDev on 24.06.2021.
 */

//Thanks to: https://medium.com/ackee/how-to-make-bulletproof-bullet-lists-in-textview-223c54fb21e6
//class ImprovedBulletSpan(
//	val bulletRadius: Int = STANDARD_BULLET_RADIUS,
//	val gapWidth: Int = STANDARD_GAP_WIDTH,
//	val color: Int = STANDARD_COLOR
//) : LeadingMarginSpan {
//
//	companion object {
//		// Bullet is slightly bigger to avoid aliasing artifacts on mdpi devices.
//		private const val STANDARD_BULLET_RADIUS = 4
//		private const val STANDARD_GAP_WIDTH = 2
//		private const val STANDARD_COLOR = 0
//	}
//
//	private var mBulletPath: Path? = null
//
//	override fun getLeadingMargin(first: Boolean): Int {
//		return 2 * bulletRadius + gapWidth
//	}
//
//	override fun drawLeadingMargin(
//		canvas: Canvas, paint: Paint, x: Int, dir: Int,
//		top: Int, baseline: Int, bottom: Int,
//		text: CharSequence, start: Int, end: Int,
//		first: Boolean,
//		layout: Layout?
//	) {
//		if ((text as Spanned).getSpanStart(this) == start) {
//			val style = paint.style
//			paint.style = Paint.Style.FILL
//
//			val yPosition = if (layout != null) {
//				val line = layout.getLineForOffset(start)
//				layout.getLineBaseline(line).toFloat() - bulletRadius * 2f
//			} else {
//				(top + bottom) / 2f
//			}
//
//			val xPosition = (x + dir * bulletRadius).toFloat()
//
//			if (canvas.isHardwareAccelerated) {
//				if (mBulletPath == null) {
//					mBulletPath = Path()
//					mBulletPath!!.addCircle(0.0f, 0.0f, bulletRadius.toFloat(), Path.Direction.CW)
//				}
//
//				canvas.save()
//				canvas.translate(xPosition, yPosition)
//				canvas.drawPath(mBulletPath!!, paint)
//				canvas.restore()
//			} else {
//				canvas.drawCircle(xPosition, yPosition, bulletRadius.toFloat(), paint)
//			}
//
//			paint.style = style
//		}
//	}
//}
//
//class OwnTagHandler : Html.TagHandler {
//	/**
//	 * Helper marker class. Idea stolen from [Html.fromHtml] implementation
//	 */
//	class Bullet
//
//	override fun handleTag(opening: Boolean, tag: String, output: Editable, xmlReader: XMLReader) {
//		if(tag == "li") {
//			if(opening) {
//				output.setSpan(
//					Bullet(),
//					output.length,
//					output.length,
//					Spannable.SPAN_INCLUSIVE_EXCLUSIVE
//				)
//			}
//			else {
//				output.append("\n")
//				val lastMark = output.getSpans(0, output.length, Bullet::class.java).lastOrNull()
//				lastMark?.let {
//					val start = output.getSpanStart(it)
//					output.removeSpan(it)
//					if(start < output.length) {
//						output.setSpan(
//							BulletSpan(),
//							start,
//							output.length,
//							Spanned.SPAN_INCLUSIVE_EXCLUSIVE
//						)
//					}
//				}
//			}
//		}
//		else if(tag === "ppp" && opening) {
//			println(output)
//			println("---")
//			output.append("\n")
//		}
//	}
//}
//
//class HtmlHandler {
//	companion object {
//		private var dp3: Int = 0
//		private var dp8: Int = 0
//		fun init(context: Context) {
//			dp3 = dip(3, context)
//			dp8 = dip(8, context)
//		}
//		fun get(html: String): Spanned {
//			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
//				return HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT)
//
//			val newHtml = html
//				.replace("<div>", "<ppp>", true)
//				.replace("</div>", "</ppp>", true)
////			val newHtml = html
//			println(newHtml)
//
//			val r = HtmlCompat.fromHtml(
//				newHtml,
//				HtmlCompat.FROM_HTML_MODE_COMPACT,
//				null,
//				OwnTagHandler())
//
//			val spannableBuilder = SpannableStringBuilder(r)
//			val bulletSpans = spannableBuilder.getSpans(0, spannableBuilder.length, BulletSpan::class.java)
//			bulletSpans.forEach {
//				val start = spannableBuilder.getSpanStart(it)
//				val end = spannableBuilder.getSpanEnd(it)
//				spannableBuilder.removeSpan(it)
//				spannableBuilder.setSpan(
//					ImprovedBulletSpan(bulletRadius = dp3, gapWidth = dp8),
//					start,
//					end,
//					Spanned.SPAN_INCLUSIVE_EXCLUSIVE
//				)
//			}
//			return r;
////			return HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT)
//
//		}
//		private fun dip(dp: Int, context: Context): Int {
//			return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), context.resources.displayMetrics).toInt()
//		}
//	}
//}


class HtmlHandler {
	companion object {
		private lateinit var htmlSpanner: HtmlSpanner
		fun fromHtml(html: String): Spanned {
			if(!this::htmlSpanner.isInitialized) {
				htmlSpanner = HtmlSpanner()
				htmlSpanner.registerHandler("div", AlignmentHandler(NewLineHandler(1)))
			}
			
			return htmlSpanner.fromHtml(html).trim() as Spannable
		}
	}
}