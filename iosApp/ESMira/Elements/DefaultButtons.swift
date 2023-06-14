//
//  File.swift
//  ESMira
//
//  Created by Karl Landsteiner Privatuniversität on 25.04.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation
import SwiftUI

struct NavigationLinkModifier: ViewModifier {
	func body(content: Content) -> some View {
		content
			.foregroundColor(Color("PrimaryDark"))
			.padding()
			.background(Color("Surface"))
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
				.foregroundColor(Color("PrimaryDark"))
		}
			.opacity(self.disabled ? 0.3 : 1)
			.disabled(self.disabled)
			.padding()
			.background(Color("Surface"))
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
			.foregroundColor(Color("PrimaryDark"))
		}
			.opacity(self.disabled ? 0.3 : 1)
			.disabled(self.disabled)
			.padding()
			.background(Color("Surface"))
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
			.foregroundColor(Color("PrimaryDark"))
		}
			.padding()
			.background(Color("Surface"))
	}
}
