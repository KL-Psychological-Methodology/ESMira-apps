//
//  InformedConsent.swift
//  ESMira
//
//  Created by JodliDev on 13.07.21.
//

import SwiftUI
import sharedCode

class InformedConsentPermission : PermissionLine {
	init (_ listRoot: PermissionListProtocol, _ index: Int) {
		super.init(listRoot: listRoot, header: "informed_consent", desc: "informed_consent_desc", index: index, whatFor: nil)
	}
	
	override func getActionRow() -> AnyView {
		AnyView(Button("show_informed_consent") {
			self.listRoot.alert(
				Alert(title: Text("informed_consent"),
					  message: Text(self.listRoot.getStudy().informedConsentForm),
					  primaryButton: .default(Text("i_agree"), action: { self.complete() }),
					  secondaryButton: .cancel())
			)
			
		}.padding())
	}
}

