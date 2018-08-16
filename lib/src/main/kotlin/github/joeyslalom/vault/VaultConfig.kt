package github.joeyslalom.vault

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.vault.authentication.ClientAuthentication
import org.springframework.vault.authentication.LifecycleAwareSessionManager
import org.springframework.vault.authentication.TokenAuthentication
import org.springframework.vault.client.VaultClients
import org.springframework.vault.client.VaultEndpoint
import org.springframework.vault.config.AbstractVaultConfiguration
import org.springframework.vault.core.VaultTemplate
import org.springframework.vault.support.SslConfiguration

@Configuration
class VaultGenericConfig : AbstractVaultConfiguration() {

    @Value("\${vault.generic.host}")
    lateinit var vaultHost: String

    @Value("\${vault.generic.port}")
    lateinit var vaultPort: String

    @Value("\${vault.generic.token}")
    lateinit var vaultToken: String

    @Value("\${vault.generic.trust-store.path}")
    lateinit var trustStore: ClassPathResource

    @Value("\${vault.generic.trust-store.password}")
    lateinit var trustStorePassword: String


    override fun vaultEndpoint(): VaultEndpoint {
        return VaultEndpoint.create(vaultHost, vaultPort.toInt())
    }

    override fun clientAuthentication(): ClientAuthentication {
        return TokenAuthentication(vaultToken)
    }

    override fun sslConfiguration(): SslConfiguration {
        return SslConfiguration.forTrustStore(trustStore, trustStorePassword.toCharArray())
    }
}

@Configuration
class VaultTransitConfig {

    @Value("\${vault.encryption.key}")
    lateinit var encryptionKey: String

    @Value("\${vault.encryption.context}")
    lateinit var encryptionContext: String

    @Value("\${vault.encryption.context.key}")
    lateinit var encryptionContextKey: String

    @Value("\${vault.transit.host}")
    lateinit var transitHost: String

    @Value("\${vault.transit.port}")
    lateinit var transitPort: String

    @Value("\${vault.transit.token}")
    lateinit var transitToken: String

    @Bean
    fun transitService(factoryWrapper: AbstractVaultConfiguration.ClientFactoryWrapper,
                       taskScheduler: ThreadPoolTaskScheduler,
                       genericVaultTemplate: VaultTemplate): TransitService {
        val requestFactory = factoryWrapper.clientHttpRequestFactory
        val endpoint = VaultEndpoint.create(transitHost, transitPort.toInt())

        val restOperations = VaultClients.createRestTemplate(endpoint, requestFactory)
        val clientAuthentication = TokenAuthentication(transitToken)

        val sm = LifecycleAwareSessionManager(clientAuthentication, taskScheduler, restOperations)
        val transitVaultTemplate = VaultTemplate(endpoint, requestFactory, sm)

        val response = genericVaultTemplate.read(encryptionContext)
        val context = response.data!![encryptionContextKey] as String

        return TransitService(transitVaultTemplate.opsForTransit(), encryptionKey, context)
    }
}
