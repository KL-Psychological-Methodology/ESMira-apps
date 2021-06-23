//
//  SceneDelegate.swift
//  ESMira
//
//  Created by JodliDev on 30.04.20.
//

import UIKit
import SwiftUI
import sharedCode


class SceneDelegate: UIResponder, UIWindowSceneDelegate {
	var window: UIWindow?
	
	
	
	func scene(_ scene: UIScene, willConnectTo session: UISceneSession, options connectionOptions: UIScene.ConnectionOptions) {
		// Use this method to optionally configure and attach the UIWindow `window` to the provided UIWindowScene `scene`.
		// If using a storyboard, the `window` property will automatically be initialized and attached to the scene.
		// This delegate does not imply the connecting scene or session are new (see `application:configurationForConnectingSceneSession` instead).
		
		
		let appDelegate = UIApplication.shared.delegate as! AppDelegate
		let appState = appDelegate.appState
		
		
		
		// Create the SwiftUI view that provides the window contents.
		let contentView = ContentView().environmentObject(appState)
		
		// Use a UIHostingController as window root view controller.
		if let windowScene = scene as? UIWindowScene {
			let window = UIWindow(windowScene: windowScene)
//			window.rootViewController = UIHostingController(rootView: contentView)
			window.rootViewController = OrientationLockedController(rootView: contentView, appState: appState)
			self.window = window
			window.makeKeyAndVisible()
		}
	}
	
	func scene(_ scene: UIScene, openURLContexts URLContexts: Set<UIOpenURLContext>) {
		let appDelegate = UIApplication.shared.delegate as! AppDelegate
		let appState = appDelegate.appState
		
		if let url = URLContexts.first?.url {
			print(url.absoluteString)
			let urlData = QrInterpreter().check(s: url.absoluteString)
			if(urlData != nil) {
				appState.connectData = urlData
				appState.addStudyOpened = true
			}
		}
	}
	
	
	func sceneDidDisconnect(_ scene: UIScene) {
		// Called as the scene is being released by the system.
		// This occurs shortly after the scene enters the background, or when its session is discarded.
		// Release any resources associated with this scene that can be re-created the next time the scene connects.
		// The scene may re-connect later, as its session was not neccessarily discarded (see `application:didDiscardSceneSessions` instead).
	}
	
	func sceneDidBecomeActive(_ scene: UIScene) {
		let appDelegate = UIApplication.shared.delegate as! AppDelegate
		let appState = appDelegate.appState
		
		let defaults = UserDefaults.standard
		var timeChanged = false
		var bootHappened = false
		
		//Check if timezone changed:
		//TODO: untested
		
		let newTimezone = TimeZone.current.secondsFromGMT()
		
		if(defaults.object(forKey: "daylightSaving") == nil) {
			defaults.set(newTimezone, forKey: "timezone")
		}
		else if(defaults.integer(forKey: "timezone") != newTimezone) {
			ErrorBox.Companion().log(title:"Reschedule", msg: "Detected changed timezone...")
			defaults.set(newTimezone, forKey: "timezone")
			timeChanged = true
		}
		
		
		//Check if daylight saving changed:
		//TODO: untested
		
		let newDaylightSaving = TimeZone.current.isDaylightSavingTime(for: Date())
		
		if(defaults.object(forKey: "daylightSaving") == nil) {
			defaults.set(newDaylightSaving, forKey: "daylightSaving")
		}
		else if(defaults.bool(forKey: "daylightSaving") != newDaylightSaving) {
			ErrorBox.Companion().log(title:"Reschedule", msg: "Detected changed daylight saving time...")
			defaults.set(newDaylightSaving, forKey: "daylightSaving")
			timeChanged = true
		}
		
		
		//Check if reboot happened:
		//Thanks to: https://stackoverflow.com/questions/36203662/getting-system-uptime-in-ios-swift
		//TODO: untested
//		var tv = timeval()
//		var tvSize = MemoryLayout<timeval>.size
//		let err = sysctlbyname("kern.boottime", &tv, &tvSize, nil, 0)
//		if(err != 0 && tvSize != MemoryLayout<timeval>.size) {
//			let newLastReboot = Double(tv.tv_sec) + Double(tv.tv_usec) / 1_000_000.0
//
//			if(defaults.object(forKey: "lastReboot") == nil) {
//				defaults.set(newLastReboot, forKey: "lastReboot")
//			}
//			else if(defaults.double(forKey: "lastReboot") - newLastReboot > 1) { //1: to correct inaccuracies
//				ErrorBox.Companion().log(title:"Reschedule", msg: "Detected reboot...")
//				defaults.set(newLastReboot, forKey: "lastReboot")
//				bootHappened = true
//			}
//		}
		
		if(timeChanged || bootHappened) { //TODO: untested
			Scheduler().reactToBootOrTimeChange(timeChanged: timeChanged)
		}
		
		
		//Booting app:
		
		DbLogic().startupApp()
		
		if(DbLogic().hasNewErrors()) {
			appState.openScreen = .errorReport
		}
		if(UserDefaults.standard.bool(forKey: DialogOpener.KEY_HAS_DIALOG)) {
			let defaults = UserDefaults.standard
			defaults.set(false, forKey: DialogOpener.KEY_HAS_DIALOG)
			appState.showDialog(title: defaults.string(forKey: DialogOpener.KEY_DIALOG_TITLE) ?? "", msg: defaults.string(forKey: DialogOpener.KEY_DIALOG_MSG) ?? "")
		}
        
        UIScrollView.appearance().keyboardDismissMode = .onDrag
	}
	
	func sceneWillResignActive(_ scene: UIScene) {
		// Called when the scene will move from an active state to an inactive state.
		// This may occur due to temporary interruptions (ex. an incoming phone call).
	}
	
	func sceneWillEnterForeground(_ scene: UIScene) {
		// Called as the scene transitions from the background to the foreground.
		// Use this method to undo the changes made on entering the background.
	}
	
	func sceneDidEnterBackground(_ scene: UIScene) {
		// Called as the scene transitions from the foreground to the background.
		// Use this method to save data, release shared resources, and store enough scene-specific state information
		// to restore the scene back to its current state.
	}
}

