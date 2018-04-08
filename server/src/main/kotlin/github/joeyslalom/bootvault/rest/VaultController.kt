package github.joeyslalom.bootvault.rest

import github.joeyslalom.bootvault.rest.api.StsApi
import github.joeyslalom.bootvault.rest.api.TransitApi
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.vault.core.VaultOperations
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody

@Controller
class VaultController {
}

@Controller
class StsController : StsApi

@Controller
class TransitController(private val vaultOps: VaultOperations) : TransitApi {
    private val transitOps = vaultOps.opsForTransit()
    private val log = LoggerFactory.getLogger(TransitController::class.java)

    override fun getListKeys(): ResponseEntity<List<String>> = ResponseEntity.ok(transitOps.keys)

    override fun putEncrypt(@PathVariable key: String, @RequestBody plainText: String): ResponseEntity<String> =
            ResponseEntity.ok(transitOps.encrypt(key, plainText))

    override fun putDecrypt(@PathVariable key: String, @RequestBody cipherText: String): ResponseEntity<String> =
            ResponseEntity.ok(transitOps.decrypt(key, cipherText))
}