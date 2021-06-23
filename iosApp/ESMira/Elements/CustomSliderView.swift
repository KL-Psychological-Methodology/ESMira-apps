//
//  Created by JodliDev on 18.11.20.
//

import Foundation
import SwiftUI


//Thanks to: https://stackoverflow.com/questions/58286350/how-to-create-custom-slider-by-using-swiftui
struct CustomSliderView: View {
	
	@Binding var value: String
	@State var percentage: Float
	@State var color: Color = .clear
	
	init(value: Binding<String>) {
		self._value = value
		self._percentage = State(initialValue: Float(value.wrappedValue) ?? 50)
	}
	
	var body: some View {
		GeometryReader { geometry in
			ZStack(alignment: .leading) {
				Rectangle()
					.foregroundColor(.gray)
					.frame(width: geometry.size.width, height: 10)
				Group {
				Rectangle()
					.foregroundColor(color)
					.frame(width: 20, height: 20)
					.cornerRadius(10)
				}
				.offset(x: geometry.size.width * CGFloat(self.percentage / 100) - 10)
			}.frame(height: 20)
			.gesture(DragGesture(minimumDistance: 0)
						.onChanged({ value in
							// TODO: - maybe use other logic here
							self.percentage = min(max(0, Float(round(value.location.x / geometry.size.width * 100))), 100)
							self.value = String(Int(self.percentage))
							self.color = .accentColor
						}))
		}.frame(height: 20)
	}
}
