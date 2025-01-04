import SwiftUI
import Umbrella

struct ContentView: View {

    var body: some View {
        VStack(spacing: 8) {
          Text("Module B: \(helloKotlin())")
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .center)
        .padding()
    }
}

#Preview {
    ContentView()
}
