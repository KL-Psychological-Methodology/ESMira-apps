//
//  Created by JodliDev on 26.11.20.
//

import Foundation
import SwiftUI

//Thanks to:
//https://github.com/bvankuik/UIViewRepresentableExamples/blob/master/UIViewRepresentableExamples/ScrollView/LegacyScrollView.swift
//https://stackoverflow.com/questions/64355967/how-to-find-a-swiftui-view-with-custom-tag-or-accessibility-identifier



enum LegacyScrollAction {
	case idle
	case toTop
	case toTag(tag: Int)
}
struct LegacyScrollTaggedView: UIViewRepresentable {
	//		let view: Content
	let tag: Int
	
	func makeUIView(context: Context) -> UIView {
		//			let parent = UIView()
		//			let child = UIHostingController(rootView: view)
		//			child.view.translatesAutoresizingMaskIntoConstraints = false
		//			child.view.frame = parent.bounds
		//
		//			parent.addSubview(child.view)
		//			return parent
		
		let uiView = UIView()
		uiView.tag = tag+1 //tag 0 seems to be the ScrollView itself. So we start counting at 1
		return uiView
	}
	
	func updateUIView(_ uiView: UIView, context: Context) {
		uiView.tag = tag+1
	}
}

struct LegacyScrollView<Content: View>: UIViewRepresentable {
	
	@Binding var action: LegacyScrollAction
	let axis: Axis
//	private let uiScrollView: UIScrollView
	
	private let uiScrollView = UIScrollView()
	private let content: UIView
	private let contentCreator: () -> Content
	
	func makeCoordinator() -> Coordinator {
		Coordinator(self)
	}
	
	func makeUIView(context: Context) -> UIScrollView {
		let uiView = self.uiScrollView
		
		content.translatesAutoresizingMaskIntoConstraints = false
		uiView.addSubview(content)
		uiView.didMoveToSuperview()
		

		let constraints: [NSLayoutConstraint]
		switch self.axis {
		case .horizontal:
			constraints = [
				content.leadingAnchor.constraint(equalTo: uiView.contentLayoutGuide.leadingAnchor),
				content.trailingAnchor.constraint(equalTo: uiView.contentLayoutGuide.trailingAnchor),
				content.topAnchor.constraint(equalTo: uiView.topAnchor),
				content.bottomAnchor.constraint(equalTo: uiView.bottomAnchor),
				content.heightAnchor.constraint(equalTo: uiView.heightAnchor)
			]
		case .vertical:
			constraints = [
				content.leadingAnchor.constraint(equalTo: uiView.leadingAnchor),
				content.trailingAnchor.constraint(equalTo: uiView.trailingAnchor),
				content.topAnchor.constraint(equalTo: uiView.contentLayoutGuide.topAnchor),
				content.bottomAnchor.constraint(equalTo: uiView.contentLayoutGuide.bottomAnchor),
				content.widthAnchor.constraint(equalTo: uiView.widthAnchor)
			]
		}
		uiView.addConstraints(constraints)
		
		
		uiView.refreshControl = UIRefreshControl()
		uiView.refreshControl?.addTarget(context.coordinator, action: #selector(Coordinator.handleRefreshControl), for: .valueChanged)
		
		return uiView
	}
	
	func updateUIView(_ uiView: UIScrollView, context: Context) {
		uiView.subviews[0].setNeedsLayout()
		uiView.subviews[0].layoutIfNeeded()
		uiView.subviews[0].updateConstraints()
		uiView.subviews[0].updateFocusIfNeeded()
//		context.coordinator.legacyScrollView.
		
//		_ = uiView.subviews.map { $0.removeFromSuperview() }
//		let content = UIHostingController(rootView: self.contentCreator()).view!
		
		
		
//		content.translatesAutoresizingMaskIntoConstraints = false
//		uiView.addSubview(content)
//
//		let constraints: [NSLayoutConstraint]
//		switch self.axis {
//		case .horizontal:
//			constraints = [
//				content.leadingAnchor.constraint(equalTo: uiView.contentLayoutGuide.leadingAnchor),
//				content.trailingAnchor.constraint(equalTo: uiView.contentLayoutGuide.trailingAnchor),
//				content.topAnchor.constraint(equalTo: uiView.topAnchor),
//				content.bottomAnchor.constraint(equalTo: uiView.bottomAnchor),
//				content.heightAnchor.constraint(equalTo: uiView.heightAnchor)
//			]
//		case .vertical:
//			constraints = [
//				content.leadingAnchor.constraint(equalTo: uiView.leadingAnchor),
//				content.trailingAnchor.constraint(equalTo: uiView.trailingAnchor),
//				content.topAnchor.constraint(equalTo: uiView.contentLayoutGuide.topAnchor),
//				content.bottomAnchor.constraint(equalTo: uiView.contentLayoutGuide.bottomAnchor),
//				content.widthAnchor.constraint(equalTo: uiView.widthAnchor)
//			]
//		}
//		uiView.addConstraints(constraints)
		
		
		
//		print("update")
//		switch self.action {
//			case .idle:
//				break
//			case .toTop:
//				uiView.scrollRectToVisible(CGRect(x: 0, y: 0, width: 1, height: 1), animated: true)
//				DispatchQueue.main.async {
//					self.action = .idle
//				}
//			case .toTag(let tag):
//				if let view = uiView.viewWithTag(tag+1) { //tag 0 seems to be the ScrollView itself. So we start counting at 1
//					if let origin = view.superview {
//						let childStartPoint = origin.convert(view.frame.origin, to: uiView)
//						uiView.scrollRectToVisible(CGRect(x: 0, y: childStartPoint.y, width: 1, height: uiView.frame.height), animated: true)
//	//					view.layer.borderWidth = 10
//	//					view.layer.borderColor = UIColor.red.cgColor
//	//					view.alpha = 0.0;
//	//					UIView.animate(withDuration: 0.5,
//	//								   delay: 0.0,
//	//								   options: [.curveEaseInOut, .autoreverse, .repeat],
//	//								   animations: { [weak view] in view?.alpha = 1.0 },
//	//								   completion: { [weak view] _ in view?.alpha = 0.0 })
//					}
//				}
//				DispatchQueue.main.async {
//					self.action = .idle
//				}
//		}
	}
	
	class Coordinator: NSObject {
		let legacyScrollView: LegacyScrollView
		
		init(_ legacyScrollView: LegacyScrollView) {
			self.legacyScrollView = legacyScrollView
		}
		
		@objc func handleRefreshControl(sender: UIRefreshControl) {
			// handle the refresh event
			print("refresh")
			sender.endRefreshing()
		}
	}
	
	init(action: Binding<LegacyScrollAction>, @ViewBuilder content: @escaping () -> Content) {
		self.init(axis: .vertical, action: action, content: content)
	}
	init(axis: Axis, action: Binding<LegacyScrollAction>, @ViewBuilder content: @escaping () -> Content) {
		self.contentCreator = content
		self.content = UIHostingController(rootView: content()).view
//		self.content.translatesAutoresizingMaskIntoConstraints = false
		
		self.axis = axis
		self._action = action
//		self.uiScrollView = UIScrollView()
		
		
//		let hosting = UIHostingController(rootView: content())
//		hosting.view.translatesAutoresizingMaskIntoConstraints = false
//
//		self.uiScrollView.addSubview(hosting.view)
//
//		let constraints: [NSLayoutConstraint]
//		switch self.axis {
//		case .horizontal:
//			constraints = [
//				hosting.view.leadingAnchor.constraint(equalTo: self.uiScrollView.contentLayoutGuide.leadingAnchor),
//				hosting.view.trailingAnchor.constraint(equalTo: self.uiScrollView.contentLayoutGuide.trailingAnchor),
//				hosting.view.topAnchor.constraint(equalTo: self.uiScrollView.topAnchor),
//				hosting.view.bottomAnchor.constraint(equalTo: self.uiScrollView.bottomAnchor),
//				hosting.view.heightAnchor.constraint(equalTo: self.uiScrollView.heightAnchor)
//			]
//		case .vertical:
//			constraints = [
//				hosting.view.leadingAnchor.constraint(equalTo: self.uiScrollView.leadingAnchor),
//				hosting.view.trailingAnchor.constraint(equalTo: self.uiScrollView.trailingAnchor),
//				hosting.view.topAnchor.constraint(equalTo: self.uiScrollView.contentLayoutGuide.topAnchor),
//				hosting.view.bottomAnchor.constraint(equalTo: self.uiScrollView.contentLayoutGuide.bottomAnchor),
//				hosting.view.widthAnchor.constraint(equalTo: self.uiScrollView.widthAnchor)
//			]
//		}
//		self.uiScrollView.addConstraints(constraints)
	}
}
