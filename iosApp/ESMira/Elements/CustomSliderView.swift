//
//  Created by JodliDev on 18.11.20.
//

import Foundation
import SwiftUI


//Thanks to: https://stackoverflow.com/questions/58286350/how-to-create-custom-slider-by-using-swiftui
struct CustomSliderView: View {
	let maxValue: Float
	
	@Binding var value: String
	@State var percentage: Float
	@State var color: Color
	
	init(value: Binding<String>, maxValue: Int = 100) {
		self.maxValue = Float(maxValue)
		self._value = value
		self._percentage = State(initialValue: (Float(value.wrappedValue) ?? Float(maxValue) / 2) / Float(maxValue))
		self._color = State(initialValue: value.wrappedValue.isEmpty ? .clear : .accentColor)
	}
	
	var body: some View {
		GeometryReader { geometry in
			ZStack(alignment: .leading) {
				Rectangle()
					.foregroundColor(Color("PrimaryDark"))
					.frame(width: geometry.size.width, height: 5)
				Group {
					Rectangle()
						.foregroundColor(self.color)
						.frame(width: 20, height: 20)
						.cornerRadius(10)
				}
				.offset(x: geometry.size.width * CGFloat(self.percentage) - 10)
			}.frame(height: 20)
			.gesture(DragGesture(minimumDistance: 0)
				.onChanged({ value in
					self.percentage = min(max(Float(1) / self.maxValue, Float(value.location.x / geometry.size.width)), 1)
					self.value = String(Int(round(maxValue * self.percentage)))
					self.color = .accentColor
				}))
		}.frame(height: 20)
	}
}
