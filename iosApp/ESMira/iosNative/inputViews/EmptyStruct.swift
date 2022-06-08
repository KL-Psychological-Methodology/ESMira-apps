//
//  EmptyStruct.swift
//  ESMira
//
//  Created by JodliDev on 31.05.22.
//

import Foundation
import SwiftUI
import sharedCode

struct EmptyStruct: View {
	@ObservedObject var viewModel: InputViewModel
	
	var body: some View {
		EmptyView()
		.onAppear {
			self.viewModel.isReady = true
		}
	}
}
