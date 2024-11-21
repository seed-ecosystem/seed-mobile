import com.seed.crypto.helpers.AesDecodingHelper
import com.seed.crypto.helpers.AesEncodingHelper
import org.junit.Test

class AesEncodingHelperTest {
	@Test
	fun encodeAndDecode() {
		val encoder = AesEncodingHelper()

		val base64Key = "bHNrYWRqYWtqc2RhYWFhZw=="

		val result = encoder.encrypt(
			plainText = "meow lol kek",
			base64Key = base64Key
		)

		println(result)

		assert(result.isSuccess)

		val decoder = AesDecodingHelper()

		result.getOrNull()?.let {
			val decoded = decoder.decode(
				encryptedBase64 = it.encryptedContent,
				base64Iv = it.iv,
				base64Key = base64Key,
			)

			println("decoded stuff: $decoded")
		}
	}
}