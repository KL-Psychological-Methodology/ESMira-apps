//
//  Color+ESMira.swift
//  ESMira
//
//  Type-safe color constants matching the xcassets color definitions.
//  Use these instead of Color("string") to catch typos at compile time.
//

import SwiftUI

private struct ESTextShadowModifier: ViewModifier {
    @Environment(\.colorScheme) private var colorScheme
    func body(content: Content) -> some View {
        let shadowColor = colorScheme == .dark
            ? Color.white.opacity(0.18)
            : Color.black.opacity(0.18)
        return content.shadow(color: shadowColor, radius: 1, x: 1, y: 1)
    }
}

extension View {
    func esTextShadow() -> some View {
        self.modifier(ESTextShadowModifier())
    }

    /// Applies the app background color to the navigation bar for screens
    /// that should not show the gradient (e.g. the add-study onboarding flow).
    @ViewBuilder
    func esBackgroundNavBar() -> some View {
        if #available(iOS 16.0, *) {
            self
                .toolbarBackground(Color("Background"), for: .navigationBar)
                .toolbarBackground(.visible, for: .navigationBar)
        } else {
            self
        }
    }
}

extension Color {
    static let esPrimaryDark   = Color("PrimaryDark")
    static let esPrimaryLight  = Color("PrimaryLight")
    static let esAccent        = Color("Accent")
    static let esAccentLight   = Color("AccentLight")
    static let esSurface       = Color("Surface")
    static let esOnSurface     = Color("onSurface")
    static let esOutline       = Color("Outline")
    static let esListColor1    = Color("ListColor1")
    static let esListColor2    = Color("ListColor2")
    static let esBackground    = Color("Background")
}
