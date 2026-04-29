import UIKit

/// Manages the user's forced colour-scheme preference.
/// Uses UIWindow.overrideUserInterfaceStyle (iOS 13+) so no iOS 14 APIs are needed.
final class ThemeManager: ObservableObject {
    static let shared = ThemeManager()

    /// 0 = follow system, 1 = force light, 2 = force dark
    @Published var themeOverride: Int {
        didSet {
            UserDefaults.standard.set(themeOverride, forKey: "themeOverride")
            applyToWindow()
        }
    }

    private init() {
        themeOverride = UserDefaults.standard.integer(forKey: "themeOverride")
    }

    func applyToWindow() {
        guard
            let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
            let window = windowScene.windows.first
        else { return }
        switch themeOverride {
        case 1: window.overrideUserInterfaceStyle = .light
        case 2: window.overrideUserInterfaceStyle = .dark
        default: window.overrideUserInterfaceStyle = .unspecified
        }
    }
}
