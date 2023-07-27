//
// Created by JodliDev on 25.07.23.
// Thanks to https://stackoverflow.com/a/65780031/10423612
//

import Foundation
import SwiftUI

class ProgressBarViewModel: ObservableObject {
	@Published var fractionCompleted: Double
	let progress: Progress
	private var observer: NSKeyValueObservation!
	
	init(_ progress: Progress) {
		self.progress = progress
		self.fractionCompleted = progress.fractionCompleted
		observer = progress.observe(\.completedUnitCount) { [weak self] (sender, _) in
			self?.fractionCompleted = sender.fractionCompleted
		}
	}
}

struct CustomProgressBarView: View {
	@ObservedObject private var vm: ProgressBarViewModel
	let showUnitCount: Bool
	
	init(_ progress: Progress, showUnitCount: Bool = true) {
		self.vm = ProgressBarViewModel(progress)
		self.showUnitCount = showUnitCount
	}
	
	var body: some View {
		VStack(alignment: .center) {
			Text("\(Int(vm.fractionCompleted * 100))%")
			ZStack {
				RoundedRectangle(cornerRadius: 2)
					.foregroundColor(Color(UIColor.systemGray5))
					.frame(height: 4)
				GeometryReader { metrics in
					RoundedRectangle(cornerRadius: 2)
						.foregroundColor(.blue)
						.frame(width: metrics.size.width * CGFloat(vm.fractionCompleted))
				}
			}.frame(height: 4)
			if(self.showUnitCount) {
				Text("\(vm.progress.completedUnitCount) of \(vm.progress.totalUnitCount)")
					.font(.footnote)
					.foregroundColor(.gray)
			}
		}
	}
}
