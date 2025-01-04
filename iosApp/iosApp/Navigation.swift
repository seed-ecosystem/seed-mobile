//
//  Navigation.swift
//  iosApp
//
//  Created by Mark Dubkov on 02.01.2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import Foundation

public enum MainTabBarRoute {
    case chats
    case settings
}

public enum AppRoute: Hashable {
    case chat(id: String)
}
