import com.seed.crypto.helpers.AesDecodingHelper
import com.seed.crypto.helpers.AesEncodingHelper
import org.junit.Test

class AesEncodingHelperTest {
	@Test
	fun encodeAndDecode() {
//		val encoder = AesEncodingHelper()
//
//		val base64Key = "bHNrYWRqYWtqc2RhYWFhZw=="
//
//		val result = encoder.encrypt(
//			plainText = "meow lol kek",
//			base64Key = base64Key
//		)
//
//		println(result)
//
//		assert(result.isSuccess)
//
//		val decoder = AesDecodingHelper()
//
//		result.getOrNull()?.let {
//			val decoded = decoder.decode(
//				encryptedBase64 = it.encryptedContent,
//				base64Iv = it.iv,
//				base64Key = base64Key,
//			)
//
//			println("decoded stuff: $decoded")
//		}

//		decodeOptions: DecodeOptions(content=q3UNmg+Y2gILVXL9Z2r/QnvMy24aXE9FSogYW/gvB7oKHxKskLU066nZkR3I1vkCMN+Pljfcniagn94=, contentIv=7Vpp/AF6q9JsD6gh, signature=oYUhTie3l9mzijiOuS256cs3qqpLjMH4EVHLSukJjzY=, key=/uwFt2yxHi59l26H9V8VTN3Kq+FtRewuWNfz1TNVcnM=)

//		{
//			"nonce": 220,
//			"chatId": "bHKhl2cuQ01pDXSRaqq/OMJeDFJVNIY5YuQB2w7ve+c=",
//			"signature": "Dz5eFEmHj7EP/+U0NIt3YyKqPNcv4S0itS6yDtQZklE=",
//			"content": "q8tfvl4DmdLkof/sUw3eidLHm17/czm7IZHj3aXb/gqYv12HYc4jiVw4NNcKYc6kxtVPSt45NtNroZMcZSI0mGU=",
//			"contentIV": "5xPwf0+YFFfCd/JC"
//		},


		val decoder = AesDecodingHelper()
		val result = decoder.decode(
			encryptedBase64 = "q8tfvl4DmdLkof/sUw3eidLHm17/czm7IZHj3aXb/gqYv12HYc4jiVw4NNcKYc6kxtVPSt45NtNroZMcZSI0mGU=",
			base64Iv = "5xPwf0+YFFfCd/JC",
			base64Key = "/uwFt2yxHi59l26H9V8VTN3Kq+FtRewuWNfz1TNVcnM="
		)

		println(result)
	}
}