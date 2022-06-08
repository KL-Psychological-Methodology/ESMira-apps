//
//  PermissionsLine.swift
//  ESMira
//
//  Created by JodliDev on 13.07.21.

import SwiftUI
import sharedCode

class PermissionLine : ObservableObject {
	let header: String
	let desc: String
	let index: Int
	let whatFor: String?
	
	let listRoot: PermissionListProtocol
	@Published
	var isFailed: Bool = false
	@Published
	var isCompleted: Bool = false
	@Published
	var isEnabled: Bool = false
	@Published
	var isCurrent: Bool = false
	
	init(listRoot: PermissionListProtocol, header: String, desc: String, index: Int, whatFor: String?) {
		self.listRoot = listRoot
		self.header = header
		self.desc = desc
		self.whatFor = whatFor
		self.index = index
	}
	func getActionRow() -> AnyView {
		return AnyView(Spacer())
	}
	func getErrorRow() -> AnyView {
		return AnyView(Spacer())
	}
	
	func enable() -> Void {
		self.isEnabled = true
		self.isCurrent = true
	}
	func complete() -> Void {
		self.isCompleted = true
		if(self.isCurrent) {
			self.isCurrent = false
			self.listRoot.next()
		}
	}
	func fail() {
		self.isFailed = true
		if(self.isCurrent) {
			self.isCurrent = false
			self.listRoot.next()
		}
	}
}
