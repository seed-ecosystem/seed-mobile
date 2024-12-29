import SwiftUI
import Umbrella

struct ContentView: View {

    @State private var showContent = false
    var body: some View {
        VStack(spacing: 8) {
          Text("Module B: \(helloKotlin())")
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .center)
        .padding()
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
