//
//  ChatListView.swift
//  iosApp
//
//  Created by Mark Dubkov on 02.01.2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI

struct ChatPreview: Identifiable {
    let id: String
    let chatName: String
    let chatPreview: String
}

class ChatListViewModel: ObservableObject {
    @Published var chats: [ChatPreview] = (0..<50).map {
        ChatPreview(
            id: "\($0)",
            chatName: "Chat #\($0 + 1)",
            chatPreview: "Chat preview long long long long long long message"
        )
    }
}

struct ChatListView: View {
    
    @EnvironmentObject private var controller: NavigationController
    
    @StateObject private var model: ChatListViewModel = .init()
    
    var body: some View {
        List($model.chats) { chat in
            Button(action: {
                controller.push(.chat(id: ""))
            }, label: {
                HStack {
                    VStack(alignment: .leading) {
                        Text(chat.wrappedValue.chatName)
                            .font(.title2)
                            .foregroundStyle(.black)
                        Text(chat.wrappedValue.chatPreview)
                            .font(.caption)
                            .foregroundStyle(.gray)
                        
                    }
                }
            })
        }
        .listStyle(.inset)
    }
}
