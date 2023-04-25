//
//  StudyDashboard.swift
//  ESMira
//
//  Created by JodliDev on 13.03.23.
//  Thanks to https://www.swiftpal.io/articles/how-to-create-a-grid-view-in-swiftui-for-ios-13
//

import SwiftUI

protocol FixedGridItem {
	associatedtype Content: View
	func fillsLine() -> Bool
	
	@ViewBuilder @MainActor var view: Content { get }
}

struct FixedGridView: View {
	
	private let columns: Int
	private var items: [[any FixedGridItem]] = []
	
	//list needs to be Any, so we can send empty closures (which we need to easily filter content via if)
	init(columns: Int, list: [Any]) {
		self.columns = columns
		self.setupList(list)
	}
	
	private mutating func setupList(_ list: [Any]) {
		var rowIndex = -1
		var columnIndex = 0
		
		for object in list {
			if let information = object as? any FixedGridItem {
				if(information.fillsLine()) {
					self.items.append([information])
					rowIndex += 1
					columnIndex = 0
				}
				else if columnIndex == 0 {
					self.items.append([information])
					rowIndex += 1
					columnIndex = (columnIndex + 1) % self.columns
				}
				else {
					self.items[rowIndex].append(information)
					columnIndex = (columnIndex + 1) % self.columns
				}
			}
			
			
		}
	}
	
	var body: some View {
		VStack(alignment: .leading, spacing: 0) {
			ForEach(0 ..< self.items.count, id: \.self) { i in
				HStack(spacing: 0) {
					let subList = self.items[i]
					ForEach(0 ..< subList.count, id: \.self) { i2 in
						let item = subList[i2]

						//We explicitly cast .view (of type View) to "any View", so the complier is able to see it as View. Then we we transform it to View using AnyView. SwiftUI is great!
						//TODO: Find out how to improve that...
						let view: any View = item.view
						AnyView(view)
							.frame(width: item.fillsLine() ? UIScreen.main.bounds.size.width : UIScreen.main.bounds.size.width / CGFloat(self.columns))
					}
				}
			}
		}
	}
}
