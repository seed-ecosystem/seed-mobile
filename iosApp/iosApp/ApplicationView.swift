//
//  ApplicationView.swift
//  iosApp
//
//  Created by Mark Dubkov on 02.01.2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI

public final class NavigationController: ObservableObject {
    
    @Published var path: [AppRoute] = []
    
    init() { }
    
    public func replaceAll(_ route: AppRoute) {
        path = []
    }
    
    public func replaceAll(_ routes: [AppRoute]) {
        guard routes.count >= 1 else { return }

        if routes.count >= 2 {
            path = Array(routes[1..<routes.count])
        } else {
            path = []
        }
    }
    
    public func push(_ route: AppRoute) {
        path += [route]
    }
    
    public func pop() {
        path.removeLast()
    }
    
    public func popTo(route: AppRoute) {
        guard let index = path.firstIndex(where: { $0 == route }) else { return }
        path = Array(path[0..<index])
    }
}


struct ApplicationView: View {
    
    @StateObject private var controller: NavigationController = .init()
    
    @State private var tabRoute: MainTabBarRoute = .chats
    
    var body: some View {
        NavigationStack(path: $controller.path) {
            MainView(tabRoute: $tabRoute)
                .environmentObject(controller)
                .navigationDestination(for: AppRoute.self) { route in
                    view(for: route)
                        .environmentObject(controller)
                }
        }
    }
}

private extension ApplicationView {
    
    @ViewBuilder
    func view(for route: AppRoute) -> some View {
        switch route {
        case .chat:
            ChatView()
        }
    }
}
