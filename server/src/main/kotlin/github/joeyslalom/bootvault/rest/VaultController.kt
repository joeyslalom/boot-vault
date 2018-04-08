package github.joeyslalom.bootvault.rest

import github.joeyslalom.bootvault.rest.api.StsApi
import github.joeyslalom.bootvault.rest.api.TransitApi
import org.springframework.stereotype.Controller

@Controller
class VaultController {
}

@Controller
class StsController : StsApi

@Controller
class TransitController : TransitApi