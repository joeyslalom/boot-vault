package github.joeyslalom.bootvault.rest

import github.joeyslalom.bootvault.rest.api.StsApi
import github.joeyslalom.bootvault.rest.api.TransitApi
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.vault.core.VaultOperations

@Controller
class VaultController {
}

@Controller
class StsController : StsApi

@Controller
class TransitController(private val vaultOpts: VaultOperations) : TransitApi {
    private val log = LoggerFactory.getLogger(TransitController::class.java)

    override fun getListKeys(): ResponseEntity<List<String>> = ResponseEntity.ok(vaultOpts.opsForTransit().keys)
}