package github.joeyslalom.vault

import org.springframework.vault.core.env.LeaseAwareVaultPropertySource
import org.springframework.vault.core.lease.SecretLeaseContainer
import org.springframework.vault.core.lease.domain.RequestedSecret
import org.springframework.vault.core.util.PropertyTransformers


fun rotatingPropertySource(stsPath: String,
                           secretLeaseContainer: SecretLeaseContainer,
                           prefix: String): LeaseAwareVaultPropertySource {
    val secret = RequestedSecret.rotating(stsPath)
    val propertyTransformer = PropertyTransformers.propertyNamePrefix(prefix)
    return LeaseAwareVaultPropertySource(stsPath, secretLeaseContainer, secret, propertyTransformer)
}
