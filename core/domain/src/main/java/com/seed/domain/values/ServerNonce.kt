package com.seed.domain.values

data class ServerNonce(
	val value: Int,
) {
	operator fun plus(nonce: ServerNonce): ServerNonce {
		return ServerNonce(nonce.value + this.value)
	}

	operator fun plus(int: Int): ServerNonce {
		return ServerNonce(this.value + int)
	}

	operator fun compareTo(nonce: ServerNonce): Int {
		return this.value.compareTo(nonce.value)
	}

	operator fun compareTo(nonce: Int): Int {
		return this.value.compareTo(nonce)
	}

	operator fun rangeTo(that: ServerNonce): IntRange {
		return IntRange(this.value, that.value)
	}
}
