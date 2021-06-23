//
// Created by JodliDev on 13.10.20.
//

import SwiftUI

class OrientationLockedController<Content: View>: UIHostingController<Content> {
	var appState: AppState
	
	override var supportedInterfaceOrientations: UIInterfaceOrientationMask {
		return appState.disableLandscape ? .portrait : .all
	}
	
	init(rootView: Content, appState: AppState) {
		self.appState = appState
		super.init(rootView: rootView)
	}
	
	@objc required dynamic init?(coder aDecoder: NSCoder) {
		fatalError("init(coder:) has not been implemented")
	}
}

