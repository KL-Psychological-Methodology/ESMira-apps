//
//  PermissionsLine.swift
//  ESMira
//
//  Created by JodliDev on 13.07.21.

import SwiftUI
import sharedCode


struct PermissionLineView: View {
	@ObservedObject
	var lineData: PermissionLine
	
	let listRoot: PermissionListProtocol
	
	var body: some View {
		return VStack(alignment: .leading) {
			HStack {
				Text("\(self.lineData.index). \(NSLocalizedString(self.lineData.header, comment: ""))").font(.system(size: 22))
				if(self.lineData.whatFor != nil) {
					Spacer()
					Button("what_for") {
						self.listRoot.alert(
							Alert(title: Text("what_for"), message: Text(NSLocalizedString(self.lineData.whatFor!, comment: "")), dismissButton: .default(Text("ok_")))
						)
					}
				}
				Spacer()
				if(self.lineData.isFailed) {
					Image(systemName: "xmark.circle.fill").foregroundColor(Color.red)
				}
				else if(self.lineData.isCompleted) {
					Image(systemName: "checkmark.circle.fill").foregroundColor(Color.green)
				}
			}
			if(self.lineData.isEnabled) {
				if(self.lineData.isFailed) {
					self.lineData.getErrorRow()
				}
				else if(!self.lineData.isCompleted) {
					Text(NSLocalizedString(self.lineData.desc, comment: "")).padding(.vertical)
					self.lineData.getActionRow()
				}
			}
		}
		.opacity(self.lineData.isFailed || self.lineData.isCompleted || self.lineData.isEnabled ? 1 : 0.3)
	}
}
