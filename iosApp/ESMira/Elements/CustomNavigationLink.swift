////
////  CustomNavigationLink.swift
////  ESMira
////
////  Created by JodliDev on 05.04.23.
////
//
////Thanks to: https://gist.github.com/Arutyun2312/a0dab7eecaa84bde99c435fecae76274
//// TLDR: SwiftUI is a nightmare to work with. Not because it is not a good framework. But because it is just not production ready even though Apple claims otherwise...
//// Longer rant:
//// NavigationLink is extremely buggy. The moment you use them with a state variable, screens start to pop back for no reason.
//// It starts with the fact that you need to be extremely careful where you define its state. If it is defined anywhere else than in the environment variable,
//// then its state will be forgotten as soon as the views are recalulcated (switching between apps or changing screen orientation).
//// So having a global handler that saves all states in combination with hidden navigationlinks in the global view seem to solve the issue.
//// UNTIL you have more than 3 Navigation Links somewhere.
//// Then things become weird. Things that are completely unreated to navigation (or state) can cause the screen to instantly popping back.
//// The whole situation is made even more fun when you account for different iOS versions.
//// Its works in iOS 13? Great. It will instantly pop back in iOS 14. You fixed it for iOS 14? Well it now wont work for iOS 13 anymore. Found a solution for both? iOS 14.5 wont work anymore.
//// My assumption is, that the whole issue is rooted in some memory optimization system that makes SwiftUI forget or ignore its State variables
//// So what is the solution? Honestly I have no idea.
//// This implementation is by a user who posted it here: https://forums.swift.org/t/14-5-beta3-navigationlink-unexpected-pop/45279/44
//// So far it works like a charm so I will stick with it and from now on I will avoid the native NavigationLink implementation like the plague I guess...
//
//import SwiftUI
//
//struct NavigationLink: View {
//	fileprivate init<T: View>(body: T) {
//		self.body = .init(body)
//	}
//
//	let body: AnyView
//}
//class NavigationLinkState: ObservableObject {
//	@Published var isActive = false
//}
//
//private struct NavigationLinkImpl<Destination: View, Label: View>: View {
//	let destination: () -> Destination?
//	
//	// We need to make sure changes to isActive trigger a view recalculation, so updateUIViewController() is called.
//	// Just using a @State variable does not work (I assume because noe views are bound to it)
//	// But using an ObservableObject seems to work
//	@StateObject private var obj = NavigationLinkState()
//	
//	@ViewBuilder let label: () -> Label
//
//	var body: some View {
//		NavigationLinkImpl1(destination: destination, isActive: self.$obj.isActive, label: label)
//	}
//}
//
//private var navs: [String: UINavigationController] = [:]
//private struct NavigationLinkImpl1<Destination: View, Label: View>: View {
//	let destination: () -> Destination
//	@Binding var isActive: Bool
//	@ViewBuilder let label: () -> Label
//	@State var model = Impl.Model()
//	@Environment(\.navigationID) var navigationID
//
//	var body: some View {
//		if navigationID == EnvironmentValues.NavigationViewKey.defaultValue {
//			Text("Navigation View not detected")
//		} else {
//			Button(action: action, label: label)
//				.overlay(Impl(isActive: $isActive, createDestination: destination, model: model, navigationID: navigationID).frame(width: 0, height: 0))
//		}
//	}
//
//	struct Impl: UIViewControllerRepresentable {
//		typealias UIViewControllerType = UIViewController
//
//		@Binding var isActive: Bool
//		let createDestination: () -> Destination
//		let model: Model
//		let navigationID: String
//
//		func makeUIViewController(context: Context) -> UIViewControllerType {
//			let controller = UIViewController()
//			DispatchQueue.main.async { _ = obtainNav(controller) }
//			return controller
//		}
//
//		func updateUIViewController(_ controller: UIViewController, context: Context) {
//			guard let nav = obtainNav(controller) else { return }
//			if isActive {
//				model.push(createDestination: createDestination)
//			} else {
//				model.pop()
//			}
//			if isActive, model.destination.map(nav.viewControllers.contains) != true { // detect pop
//				DispatchQueue.main.async {
//					isActive = false
//					model.pop()
//				}
//			}
//		}
//
//		func obtainNav(_ controller: UIViewController) -> UINavigationController? {
//			guard let nav = model.nav ?? navs[navigationID] ?? controller.navigationController else { return nil }
//			model.nav = nav
//			navs[navigationID] = nav
//			return nav
//		}
//
//		final class Model: ObservableObject {
//			@Published var nav: UINavigationController!
//			private(set) var destination: UIViewController?
//
//			func push(createDestination: () -> Destination) {
//				if destination == nil {
//					let dest = UIHostingController<Destination>(rootView: createDestination())
//					nav.pushViewController(dest, animated: true)
//					destination = dest
//				}
//			}
//
//			func pop() {
//				if let dest = destination, let i = nav?.viewControllers.lastIndex(of: dest) {
//					var views = nav.viewControllers
//					views.remove(at: i)
//					nav.setViewControllers(views, animated: true)
//				}
//				destination = nil
//			}
//
//			deinit { // deinit is not always called in main thread
//				guard let nav = nav, let destination = destination else { return }
//				DispatchQueue.main.async {
//					if let i = nav.viewControllers.lastIndex(of: destination) {
//						var views = nav.viewControllers
//						views.remove(at: i)
//						nav.setViewControllers(views, animated: true)
//					}
//				}
//			}
//		}
//	}
//
//	func action() {
//		if isActive { // supposed to be false, but pop wasn't detected. Therefore force push
//			guard model.nav != nil else { return } // nav hasn't been inited
//			model.pop()
//			model.push(createDestination: destination)
//		} else {
//			isActive = true
//		}
//	}
//}
//
//struct NavigationView<Content: View>: View {
//	@ViewBuilder var content: () -> Content
//	@State var id = ProcessInfo.processInfo.globallyUniqueString
//
//	var body: some View {
//		SwiftUI.NavigationView {
//			content()
//				.overlay(NavigationLink(destination: EmptyView()) {}) // ensure nav can be found
//		}
//		.environment(\.navigationID, id)
//		.onDisappear { navs[id] = nil }
//	}
//}
//
//extension EnvironmentValues {
//	struct NavigationViewKey: EnvironmentKey {
//		static var defaultValue = ""
//	}
//
//	fileprivate var navigationID: NavigationViewKey.Value {
//		get { self[NavigationViewKey.self] }
//		set { self[NavigationViewKey.self] = newValue }
//	}
//}
//
//extension NavigationLink {
//	init<Destination: View, Label: View>(destination: @autoclosure @escaping () -> Destination, @ViewBuilder label: @escaping () -> Label) {
//		self.init(body: NavigationLinkImpl(destination: destination, label: label))
//	}
//
//	init<Destination: View, Label: View>(destination: @autoclosure @escaping () -> Destination, isActive: Binding<Bool>, @ViewBuilder label: @escaping () -> Label) {
//		self.init(body: NavigationLinkImpl1(destination: destination, isActive: isActive, label: label))
//	}
//
//	init<Destination: View>(_ text: String, destination: @autoclosure @escaping () -> Destination, isActive: Binding<Bool>) {
//		self.init(destination: destination(), isActive: isActive) { Text(text) }
//	}
//
//	init<Destination: View>(_ text: String, destination: @autoclosure @escaping () -> Destination) {
//		self.init(destination: destination()) { Text(text) }
//	}
//
//	init<Destination: View>(destination: @autoclosure @escaping () -> Destination, isActive: Binding<Bool>) {
//		self.init(destination: destination(), isActive: isActive) {}
//	}
//}
