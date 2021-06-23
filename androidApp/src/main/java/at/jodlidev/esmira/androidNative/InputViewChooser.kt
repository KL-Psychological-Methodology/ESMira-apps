package at.jodlidev.esmira.androidNative

import android.content.Context
import at.jodlidev.esmira.sharedCode.data_structure.Input
import at.jodlidev.esmira.input_views.*

/**
 * Created by JodliDev on 19.05.2020.
 */
object InputViewChooser {
	fun getView(context: Context, type: Int): AndroidInputViewInterface {
		return when(Input.TYPES.values()[type]) {
			Input.TYPES.text -> TextElView(context)
			Input.TYPES.binary -> BinaryView(context)
			Input.TYPES.va_scale -> VaScaleView(context)
			Input.TYPES.likert -> LikertView(context)
			Input.TYPES.number -> NumberView(context)
			Input.TYPES.text_input -> TextInputView(context)
			Input.TYPES.time -> TimeView(context)
			Input.TYPES.time_old -> TimeOldView(context) //TODO: can be removed when Selinas study is done
			Input.TYPES.date -> DateView(context)
			Input.TYPES.date_old -> DateOldView(context) //TODO: can be removed when Selinas study is done
			Input.TYPES.list_single -> ListSingleView(context)
			Input.TYPES.list_multiple -> ListMultipleView(context)
			Input.TYPES.dynamic_input -> DynamicInputView(context)
			Input.TYPES.image -> ImageElView(context)
			Input.TYPES.video -> VideoView(context)
			else -> ErrorView(context)
		}
	}
}