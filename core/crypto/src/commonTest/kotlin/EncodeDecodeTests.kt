import com.seed.crypto.helpers.AesHelper
import com.seed.crypto.helpers.EncryptResult
import com.seed.crypto.randomAESKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class EncodeDecodeTests {

    @Test
    fun aesEncodeTest() {

        val key = randomAESKey()
        val baseText = "Some text"

        val encodeResult: EncryptResult = assertIs(
            AesHelper.encode(
                plainText = baseText,
                base64Key = key
            ).getOrNull()
        )

        val decodedResult: String = assertIs(
            AesHelper.decode(
                encryptedBase64 = encodeResult.encryptedContent,
                base64Iv = encodeResult.iv,
                base64Key = key,
            ).getOrNull()
        )

        assertEquals(
            expected = baseText,
            actual = decodedResult,
        )
    }
}