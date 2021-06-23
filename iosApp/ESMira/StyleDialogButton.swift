//
// Created by JodliDev on 04.11.20.
//

import Foundation
import SwiftUI

struct StyleDialogButton: ButtonStyle {
	func makeBody(configuration: Self.Configuration) -> some View {
		configuration.label
			.foregroundColor(Color("PrimaryDark"))
			.cornerRadius(40)
	}
}
