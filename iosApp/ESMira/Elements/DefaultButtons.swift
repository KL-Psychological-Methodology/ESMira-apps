//
//  File.swift
//  ESMira
//
//  Created by Karl Landsteiner Privatuniversität on 25.04.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation
import SwiftUI

let ESMiraButtonCornerRadius: CGFloat = 16

extension ColorScheme {
	var cardShadowColor: Color {
		self == .dark
			? Color(.sRGBLinear, white: 1, opacity: 0.35)
			: Color(.sRGBLinear, white: 0, opacity: 0.25)
	}
}

private struct ESMiraTextShadowModifier: ViewModifier {
	@Environment(\.colorScheme) private var colorScheme
	func body(content: Content) -> some View {
		let shadowColor = colorScheme == .dark
			? Color.white.opacity(0.18)
			: Color.black.opacity(0.18)
		return content.shadow(color: shadowColor, radius: 1, x: 1, y: 1)
	}
}

extension View {
	func ESMiraTextShadow() -> some View {
		self.modifier(ESMiraTextShadowModifier())
	}
}

struct NavigationLinkModifier: ViewModifier {
	func body(content: Content) -> some View {
		content
			.foregroundColor(Color("onSurface"))
			.padding(.horizontal, 16)
			.padding(.vertical, 10)
			.background(Color("Surface"))
			.clipShape(RoundedRectangle(cornerRadius: ESMiraButtonCornerRadius))
	}
}

extension NavigationLink {
	func defaultDesign() -> some View {
		return modifier(NavigationLinkModifier())
	}
}

struct DefaultButton: View {
	let label: String
	let maxWidth: CGFloat?
	let disabled: Bool
	let action: () -> Void
	init(_ label: String, maxWidth: CGFloat? = nil, disabled: Bool = false, action: @escaping () -> Void) {
		self.label = label
		self.maxWidth = maxWidth
		self.disabled = disabled
		self.action = action
	}
	
	var body: some View {
		Button(action: self.action) {
			Text(NSLocalizedString(self.label, comment: ""))
				.bold()
				.frame(maxWidth: self.maxWidth)
				.foregroundColor(Color("onSurface"))
		}
			.opacity(self.disabled ? 0.3 : 1)
			.disabled(self.disabled)
			.padding(.horizontal, 16)
			.padding(.vertical, 10)
			.background(Color("Surface"))
			.clipShape(RoundedRectangle(cornerRadius: ESMiraButtonCornerRadius))
	}
}


struct DefaultIconButton: View {
	let icon: String
	let label: String
	let maxWidth: CGFloat?
	let disabled: Bool
	let action: () -> Void
	init(icon: String, label: String, maxWidth: CGFloat? = nil, disabled: Bool = false, action: @escaping () -> Void) {
		self.icon = icon
		self.label = label
		self.maxWidth = maxWidth
		self.disabled = disabled
		self.action = action
	}
	
	var body: some View {
		Button(action: self.action) {
			HStack {
				Image(systemName: self.icon)
				Text(NSLocalizedString(self.label, comment: "")).bold()
			}
			.frame(maxWidth: self.maxWidth)
			.foregroundColor(Color("onSurface"))
		}
			.opacity(self.disabled ? 0.3 : 1)
			.disabled(self.disabled)
			.padding(.horizontal, 16)
			.padding(.vertical, 10)
			.background(Color("Surface"))
			.clipShape(RoundedRectangle(cornerRadius: ESMiraButtonCornerRadius))
	}
}

struct DefaultIconRightButton: View {
	let icon: String
	let label: String
	let maxWidth: CGFloat?
	let action: () -> Void
	init(icon: String, label: String, maxWidth: CGFloat? = nil, action: @escaping () -> Void) {
		self.icon = icon
		self.label = label
		self.maxWidth = maxWidth
		self.action = action
	}
	
	var body: some View {
		Button(action: self.action) {
			HStack {
				Text(NSLocalizedString(self.label, comment: "")).bold()
				Image(systemName: self.icon)
			}
			.frame(maxWidth: self.maxWidth)
			.foregroundColor(Color("onSurface"))
		}
			.padding(.horizontal, 16)
			.padding(.vertical, 10)
			.background(Color("Surface"))
			.clipShape(RoundedRectangle(cornerRadius: ESMiraButtonCornerRadius))
	}
}
