//
//  NotificationsPermission.swift
//  ESMira
//
//  Created by JodliDev on 13.07.21.
//

import SwiftUI
import sharedCode

class NotificationsPermission : PermissionLine {
	init (_ listRoot: PermissionListProtocol, _ index: Int) {
		super.init(listRoot: listRoot, header: "notifications", desc: "notification_permission_check", index: index, whatFor: "notification_setup_desc")
	}
	
	override func getActionRow() -> AnyView {
		AnyView(Button("enable_notifications") {
			Notifications.authorize { success in
				if(success) {
					self.complete()
				}
				else {
					self.fail()
				}
			}
		}.padding())
	}
	
	
	override func getErrorRow() -> AnyView {
		AnyView(
			VStack {
				Text("ios_dialogDesc_notifications_disabled")
				Button("open_settings") {
					self.listRoot.openAppSettings()
					
				}.padding()
				
			})
	}
}

