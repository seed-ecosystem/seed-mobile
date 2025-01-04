//
//  MainView.swift
//  iosApp
//
//  Created by Mark Dubkov on 02.01.2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI

struct MainView: View {
    
    @Binding var tabRoute: MainTabBarRoute
    
    var body: some View {
        TabView(selection: $tabRoute) {
            chatListPage.tag(MainTabBarRoute.chats)
            settingsPage.tag(MainTabBarRoute.settings)
            
        }
    }
}

private extension MainView {
    
    var chatListPage: some View {
        ChatListView()
            .tabItem { Label("Chats", systemImage: "bubble.left.and.bubble.right.fill") }
    }
    
    var settingsPage: some View {
        SettingsView()
            .tabItem { Label("Settings", systemImage: "gear") }
    }
}
