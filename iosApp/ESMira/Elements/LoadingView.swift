//
// Created by JodliDev on 08.06.20.
//

import SwiftUI

enum LoadingState {
	case hidden
	case loading
	case success
	case error
}

struct LoadingSpinner: UIViewRepresentable {
	@Binding var isAnimating: Bool
	let style: UIActivityIndicatorView.Style
	
	func makeUIView(context: UIViewRepresentableContext<LoadingSpinner>) -> UIActivityIndicatorView {
		return UIActivityIndicatorView(style: style)
	}
	
	func updateUIView(_ uiView: UIActivityIndicatorView, context: UIViewRepresentableContext<LoadingSpinner>) {
		isAnimating ? uiView.startAnimating() : uiView.stopAnimating()
	}
}

struct LoadingView<SheetContent: View>: ViewModifier {
	@EnvironmentObject var appState: AppState
	@Binding var state: LoadingState
	var blocking = false
	var onShowing: (() -> Void)? = nil
	var onCancel: (() -> Void)? = nil
	let message: () -> SheetContent
	
	func blockingBody(_ content: Content) -> some View {
		return ZStack(alignment: self.blocking ? .center : .bottomLeading) {
			content
				.disabled(self.state != .hidden)
				.blur(radius: self.state != .hidden ? 3 : 0)
			
			if(self.state == .error) {
				VStack(alignment: .center) {
					HStack {
						Image(systemName: "xmark.octagon.fill").foregroundColor(.red)
						self.message().font(.system(size: 12))
					}
					
					HStack {
						Spacer()
						Button(action: {
							self.state = .hidden
						}) {
							Text("close")
						}
					}
				}
				.padding()
				.background(Color.init(red: 0.7, green: 0.7, blue: 0.7))
				.cornerRadius(20)
				.transition(.opacity)
			}
			else if(self.state != .hidden) {
				VStack(alignment: .center) {
					Spacer()
					if(self.state == .loading) {
						Text("info_loading")
						LoadingSpinner(isAnimating: .constant(true), style: .large)
					}
					else if(self.state == .success) {
						HStack {
							Image(systemName: "checkmark.circle.fill").foregroundColor(.green)
							self.message()
						}
					}
					
					Spacer()
					
					
					HStack {
						Spacer()
						Button(action: {
							if(self.onCancel != nil) {
								self.onCancel?()
							}
							self.state = .hidden
						}) {
							Text(self.state == .loading ? "cancel" : "close")
						}
					}
				}
				.padding()
				.frame(width: 200, height: 200)
				.background(Color.init(red: 0.7, green: 0.7, blue: 0.7))
				.cornerRadius(20)
				.transition(.opacity)
				.onAppear {
					if(self.onShowing != nil) {
						self.onShowing?()
					}
				}
			}
		}
	}
	
	func unblockingBody(_ content: Content) -> some View {
		return ZStack(alignment: self.blocking ? .center : .bottomLeading) {
			content
			if(self.state != .hidden) {
				HStack {
					if(self.state == .loading) {
						LoadingSpinner(isAnimating: .constant(true), style: .large)
					}
					else if(self.state == .success) {
						Image(systemName: "checkmark.circle.fill").foregroundColor(.green)
					}
					else if(self.state == .error) {
						Image(systemName: "xmark.octagon.fill").foregroundColor(.red)
					}
					self.message()
				}
				.onAppear {
					if(self.onShowing != nil) {
						self.onShowing?()
					}
				}
			}
		}
	}
	
	func body(content: Content) -> some View {
		return Group {
			if(self.blocking) {
				blockingBody(content)
			}
			else {
				unblockingBody(content)
			}
		}
		
//		ZStack(alignment: self.blocking ? .center : .bottomLeading) {
//			if(self.blocking) {
//				content
//					.disabled(self.state != .hidden)
//					.blur(radius: self.state != .hidden ? 3 : 0)
//
//				if(self.state == .error) {
//					VStack(alignment: .center) {
//						HStack {
//							Image(systemName: "xmark.octagon.fill").foregroundColor(.red)
//							self.message().font(.system(size: 12))
//						}
//
//						HStack {
//							Spacer()
//							Button(action: {
//								self.state = .hidden
//							}) {
//								Text("close")
//							}
//						}
//					}
//					.padding()
//					.background(Color.init(red: 0.7, green: 0.7, blue: 0.7))
//					.cornerRadius(20)
//					.transition(.opacity)
//				}
//				else if(self.state != .hidden) {
//					VStack(alignment: .center) {
//						Spacer()
//						if(self.state == .loading) {
//							Text("info_loading")
//							LoadingSpinner(isAnimating: .constant(true), style: .large)
//						}
//						else if(self.state == .success) {
//							HStack {
//								Image(systemName: "checkmark.circle.fill").foregroundColor(.green)
//								self.message()
//							}
//						}
//
//						Spacer()
//
//
//						HStack {
//							Spacer()
//							Button(action: {
//								if(self.onCancel != nil) {
//									self.onCancel?()
//								}
//								self.state = .hidden
//							}) {
//								Text(self.state == .loading ? "cancel" : "close")
//							}
//						}
//					}
//						.padding()
//						.frame(width: 200, height: 200)
//						.background(Color.init(red: 0.7, green: 0.7, blue: 0.7))
//						.cornerRadius(20)
//						.transition(.opacity)
//				}
//			}
//			else {
//				content
//				if(self.state != .hidden) {
//					HStack {
//						if(self.state == .loading) {
//							LoadingSpinner(isAnimating: .constant(true), style: .large)
//						}
//						else if(self.state == .success) {
//							Image(systemName: "checkmark.circle.fill").foregroundColor(.green)
//						}
//						else if(self.state == .error) {
//							Image(systemName: "xmark.octagon.fill").foregroundColor(.red)
//						}
//						self.message()
//					}
//				}
//
//			}
//		}
//		.onAppear {
//			if(self.onShowing != nil) {
//				self.onShowing?()
//			}
//		}
	}
}
extension View {
	func customLoader(
		isShowing: Binding<LoadingState>,
		blocking: Bool = true,
		onShowing: (() -> Void)? = nil,
		onCancel: (() -> Void)? = nil,
		message: String = ""
	) -> some View {
		self.modifier(LoadingView(state: isShowing, blocking: blocking, onShowing: onShowing, onCancel: onCancel, message: {Text(message)}))
	}
}
