//
//  PermissionListInterface.swift
//  ESMira
//
//  Created by JodliDev on 01.02.22.
//

import SwiftUI
import sharedCode

protocol PermissionListProtocol {
	func alert(_: Alert)
	func next()
	func getStudy() -> Study
	func openAppSettings()
}
