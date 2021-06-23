//
//  Created by JodliDev on 02.12.20.
//

//Thanks to:
//https://github.com/bvankuik/UIViewRepresentableExamples/blob/master/UIViewRepresentableExamples/ScrollView/LegacyScrollView.swift
//https://swiftui-lab.com/a-powerful-combo/
//https://stackoverflow.com/questions/64355967/how-to-find-a-swiftui-view-with-custom-tag-or-accessibility-identifier

import SwiftUI

extension View {
	func scrollAction(_ action: Binding<ScrollAction>) -> some View {
		return ScrollViewWrapper(action: action) { self }
	}
	func uiTag(_ tag: Int) -> some View {
		return ZStack {
			TaggedView(tag: tag).allowsHitTesting(false)
			self
		}
//		self.overlay(TaggedView(tag: tag).allowsHitTesting(false))
	}
}



enum ScrollAction {
	case idle
	case toTop
	case toTag(tag: Int) //scrolls to an element that is marked with .uiTag()
}

struct TaggedView: UIViewRepresentable {
	let tag: Int
	
	func makeUIView(context: Context) -> UIView {
		let uiView = UIView()
		uiView.isUserInteractionEnabled = false
		uiView.tag = tag+1 //tag 0 seems to be the ScrollView itself. So we start counting at 1
		return uiView
	}
	
	func updateUIView(_ uiView: UIView, context: Context) {
		uiView.tag = tag+1
	}
}






struct ScrollTaggedView<Content: View>: UIViewRepresentable {
	let tag: Int
	let view: Content
	let child: UIHostingController<Content>
	
	func makeUIView(context: Context) -> UIView {
		let uiView = UIView()
		uiView.tag = tag+1 //tag 0 seems to be the ScrollView itself. So we start counting at 1
		
		uiView.addSubview(child.view)
		child.view.sizeToFit()
		return uiView
	}
	
	func updateUIView(_ uiView: UIView, context: Context) {
		uiView.tag = tag+1
	}
	
	init(tag: Int, @ViewBuilder content: () -> Content) {
		self.tag = tag
		self.view = content()
		self.child = UIHostingController(rootView: view)
	}
}



struct ScrollViewWrapper<Content>: View where Content : View {
	var action: Binding<ScrollAction>
	let content: () -> Content
	
	init(action: Binding<ScrollAction>, @ViewBuilder content: @escaping () -> Content) {
		self.action = action
		self.content = content
	}
	
	var body: some View {
		ScrollViewRepresentable(action: action, content: self.content())
	}
}

struct ScrollViewRepresentable<Content>: UIViewControllerRepresentable where Content: View {
	typealias UIViewControllerType = ScrollViewUIHostingController<Content>
	
	@Binding var action: ScrollAction
	let content: Content
	
	func makeUIViewController(context: UIViewControllerRepresentableContext<ScrollViewRepresentable<Content>>) -> ScrollViewUIHostingController<Content> {
		return ScrollViewUIHostingController(action: self.$action, rootView: self.content)
	}
	
	func updateUIViewController(_ uiViewController: ScrollViewUIHostingController<Content>, context: UIViewControllerRepresentableContext<ScrollViewRepresentable<Content>>) {
		uiViewController.rootView = self.content
		uiViewController.updateScroll(action: self.action)
	}
}

class ScrollViewUIHostingController<Content>: UIHostingController<Content> where Content : View {
	var action: Binding<ScrollAction>
	
	var ready = false
	var scrollView: UIScrollView? = nil
	
	init(action: Binding<ScrollAction>, rootView: Content) {
		self.action = action
		super.init(rootView: rootView)
	}
	
	@objc required dynamic init?(coder aDecoder: NSCoder) {
		fatalError("init(coder:) has not been implemented")
	}
	
	override func viewDidAppear(_ animated: Bool) {
		// observer is added from viewDidAppear, in order to
		// make sure the SwiftUI view is already in place
		if ready { return } // avoid running more than once
		
		ready = true
		
		self.scrollView = findUIScrollView(view: self.view)
		
		self.scrollView?.addObserver(self, forKeyPath: #keyPath(UIScrollView.contentOffset), options: [.old, .new], context: nil)
		
//		self.scroll(position: self.offset.wrappedValue, animated: false)
		super.viewDidAppear(animated)
	}
	
	override func observeValue(forKeyPath keyPath: String?, of object: Any?, change: [NSKeyValueChangeKey : Any]?, context: UnsafeMutableRawPointer?) {
		if keyPath == #keyPath(UIScrollView.contentOffset) {
			
			DispatchQueue.main.async {
				self.action.wrappedValue = .idle
			}
		}
	}
	
	func updateScroll(action: ScrollAction) {
		if let uiView = self.scrollView {
			switch action {
				case .idle:
					break
				case .toTop:
					uiView.scrollRectToVisible(CGRect(x: 0, y: 0, width: 1, height: 1), animated: true)
				case .toTag(let tag):
					if let view = uiView.viewWithTag(tag+1) { //tag 0 seems to be the ScrollView itself. So we start counting at 1
						if let origin = view.superview {
							let childStartPoint = origin.convert(view.frame.origin, to: uiView)
							uiView.scrollRectToVisible(CGRect(x: 0, y: childStartPoint.y, width: 1, height: uiView.frame.height), animated: true)
							
							view.alpha = 0.8;
							view.layer.borderWidth = 8
							view.layer.borderColor = UIColor.red.cgColor
							UIView.animate(withDuration: 1,
										   delay: 0.5,
										   options: [.curveEaseInOut],
										   animations: { [weak view] in view?.alpha = 0 })
						}
					}
				}
		}
	}
	
	func findUIScrollView(view: UIView?) -> UIScrollView? {
		if view?.isKind(of: UIScrollView.self) ?? false {
			return (view as? UIScrollView)
		}
		
		for v in view?.subviews ?? [] {
			if let vc = findUIScrollView(view: v) {
				return vc
			}
		}
		
		return nil
	}
	
	deinit {
		self.scrollView?.removeObserver(self, forKeyPath: #keyPath(UIScrollView.contentOffset))
	}
}
