//
//  ChatView.swift
//  iosApp
//
//  Created by Mark Dubkov on 02.01.2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI
import ExyteChat

private enum Static {
    
    static let user1: ExyteChat.User = .init(
        id: "user1",
        name: "y9san9",
        avatarURL: nil,
        isCurrentUser: false
    )
    
    static let user2: ExyteChat.User = .init(
        id: "user2",
        name: "kramlex",
        avatarURL: nil,
        isCurrentUser: true
    )
    
    static let user3: ExyteChat.User = .init(
        id: "user3",
        name: "demn",
        avatarURL: nil,
        isCurrentUser: false
    )
}

final class ChatViewModel {
    
    @Published private(set) var messages: [ExyteChat.Message] = [
        .init(id: "1", user: Static.user1, text: "Sosite"),
        .init(id: "2", user: Static.user1, text: "Ya delayu seed"),
        .init(id: "3", user: Static.user1, text: "Z~O~V"),
        .init(id: "4", user: Static.user3, text: "Ya tozhe"),
        .init(id: "5", user: Static.user3, text: "Prodayu narkotiki"),
        .init(id: "6", user: Static.user2, text: "I ya tozhe"),
    ]
    
    func didSendMessage(_ message: ExyteChat.DraftMessage) {
        
    }
}

struct ChatView: View {
    
    @State private var model: ChatViewModel = .init()
    
    var body: some View {
        ExyteChat.ChatView(
            messages: model.messages,
            chatType: .conversation,
            didSendMessage: model.didSendMessage,
            inputViewBuilder: { textBinding, attachments, inputViewState, inputViewStyle, inputViewActionClosure, dismissKeyboardClosure in
                Group {
                    switch inputViewStyle {
                    case .message: // input view on chat screen
                        VStack {
                            HStack {
                                Button("Send") { inputViewActionClosure(.send) }
//                                Button("Attach") { inputViewActionClosure(.photo) }
                            }
                            TextField("Write your message", text: textBinding)
                        }
                    case .signature: // input view on photo selection screen
                        VStack {
                            HStack {
                                Button("Send") { inputViewActionClosure(.send) }
                            }
                            TextField("Compose a signature for photo", text: textBinding)
                                .background(Color.green)
                        }
                    }
                }
            }
        )
    }
}
