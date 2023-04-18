//
//  Created by JodliDev on 01.03.21.
//  Thanks to: https://betterprogramming.pub/build-a-chat-app-interface-with-swiftui-96609e605422
//

import SwiftUI

struct ChatBubble: View {
	enum BubblePosition {
		case left
		case right
	}
	let position: BubblePosition
	let color : Color
	let content: String
	init(position: BubblePosition, isNew: Bool, content: String) {
		self.content = content
		self.position = position
		if(position == .right) {
			self.color = Color("Secondary")
		}
		else if(isNew) {
			self.color = Color("Accent")
		}
		else {
			self.color = Color("PrimaryDark")
		}
	}
	
	var body: some View {
		HStack(spacing: 0 ) {
			Text(content)
				.padding(.all, 15)
				.foregroundColor(Color.white)
				.background(color)
				.clipShape(RoundedRectangle(cornerRadius: 18))
				.overlay(
					Image(systemName: "arrowtriangle.left.fill")
						.foregroundColor(color)
						.rotationEffect(Angle(degrees: position == .left ? -50 : -130))
						.offset(x: position == .left ? -5 : 5)
					,alignment: position == .left ? .bottomLeading : .bottomTrailing)
		}
		.padding(position == .left ? .leading : .trailing , 15)
		.padding(position == .right ? .leading : .trailing , 60)
		.frame(width: UIScreen.main.bounds.width, alignment: position == .left ? .leading : .trailing)
	}
}
