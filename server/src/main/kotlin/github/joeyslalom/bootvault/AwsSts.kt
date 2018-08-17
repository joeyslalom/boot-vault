package github.joeyslalom.bootvault

import com.amazonaws.services.sns.AmazonSNSAsync
import github.joeyslalom.aws.AwsConfig
import github.joeyslalom.aws.AwsSnsConfig
import github.joeyslalom.vault.VaultGenericConfig
import github.joeyslalom.vault.VaultTransitConfig
import org.springframework.context.annotation.Import
import org.springframework.stereotype.Component


@Component
@Import(value = [VaultGenericConfig::class, VaultTransitConfig::class, AwsConfig::class, AwsSnsConfig::class])
class SnsClient(private val amazonSnsAsync: AmazonSNSAsync) {
    fun publish(topicArn: String, message: String): String = amazonSnsAsync.publish(topicArn, message).messageId
}
