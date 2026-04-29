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

// Overrides the nav bar to use the Background color on the add-study onboarding
// screens, matching Android's WelcomeScreenActivity which uses colorScheme.background
// there instead of the primary blue used everywhere else.
// iOS 16+: toolbarBackground modifier handles this natively.
// iOS 13–15: a UIViewControllerRepresentable reaches into the nav controller directly.
private struct NavBarBackgroundHelper: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> Impl { Impl() }
    func updateUIViewController(_: Impl, context: Context) {}

    final class Impl: UIViewController {
        override func viewWillAppear(_ animated: Bool) {
            super.viewWillAppear(animated)
            guard let bar = navigationController?.navigationBar else { return }
            let a = UINavigationBarAppearance()
            a.configureWithOpaqueBackground()
            a.backgroundColor = UIColor(named: "Background")
            let fg = UIColor(named: "onSurface") ?? .label
            a.titleTextAttributes      = [.foregroundColor: fg]
            a.largeTitleTextAttributes = [.foregroundColor: fg]
            bar.standardAppearance   = a
            bar.scrollEdgeAppearance = a
            bar.compactAppearance    = a
            bar.tintColor = fg
        }

        override func viewWillDisappear(_ animated: Bool) {
            super.viewWillDisappear(animated)
            guard let bar = navigationController?.navigationBar else { return }
            let a = UINavigationBarAppearance()
            a.configureWithOpaqueBackground()
            a.backgroundColor = UIColor(named: "PrimaryDark")
            a.titleTextAttributes      = [.foregroundColor: UIColor.white]
            a.largeTitleTextAttributes = [.foregroundColor: UIColor.white]
            bar.standardAppearance   = a
            bar.scrollEdgeAppearance = a
            bar.compactAppearance    = a
            bar.tintColor = .white
        }
    }
}

extension View {
    func esTextShadow() -> some View {
        self.modifier(ESTextShadowModifier())
    }

    @ViewBuilder
    func esBackgroundNavBar() -> some View {
        if #available(iOS 16.0, *) {
            self
                .toolbarBackground(Color("Background"), for: .navigationBar)
                .toolbarBackground(.visible, for: .navigationBar)
        } else {
            self.background(NavBarBackgroundHelper().frame(width: 0, height: 0))
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
