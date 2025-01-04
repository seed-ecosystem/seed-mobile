//
//  SettingsView.swift
//  iosApp
//
//  Created by Mark Dubkov on 02.01.2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI

class SettingsViewModel: ObservableObject {
    
    @Published var name: String = "Unknown"
}

struct SettingsView: View {
    
    @StateObject private var model: SettingsViewModel = .init()
    
    var body: some View {
        Form {
            Section("User info") {
                TextField("Displaying name", text: $model.name)
            }
        }
    }
}
