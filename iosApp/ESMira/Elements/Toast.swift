//
// Created by JodliDev on 13.08.20.
//

import Foundation
import SwiftUI

struct Toast<Presenting, Content>: View where Presenting: View, Content: View {
	@Binding var isPresented: Bool
	let presenter: () -> Presenting
	let content: () -> Content
	let delay: TimeInterval = 3
	
	var body: some View {
		if self.isPresented {
			DispatchQueue.main.asyncAfter(deadline: .now() + self.delay) {
				withAnimation {
					self.isPresented = false
				}
			}
		}
		
		return GeometryReader { geometry in
			ZStack(alignment: .bottom) {
				self.presenter()
				
				Button(action: {
					self.isPresented = false
				}) {
					ZStack(alignment: .center) {
						Capsule()
							.fill(Color.gray)
						self.content()
							.padding(.horizontal)
					}
				}
					.frame(width: geometry.size.width / 1.25, height: 50)
					.opacity(self.isPresented ? 0.9 : 0)
					.foregroundColor(Color.white)
					.padding(.bottom)
			}
		}
	}
}

extension View {
	func toast<Content>(isPresented: Binding<Bool>, content: @escaping () -> Content) -> some View where Content: View {
		Toast(
			isPresented: isPresented,
			presenter: { self },
			content: content
		)
	}
}
