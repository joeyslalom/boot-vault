package github.joeyslalom.aws

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.sns.AmazonSNSAsync
import com.amazonaws.services.sns.AmazonSNSAsyncClientBuilder
import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder
import github.joeyslalom.vault.rotatingPropertySource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.Environment
import org.springframework.vault.core.lease.SecretLeaseContainer
import javax.annotation.PostConstruct

@Configuration
class AwsConfig {
    @Value("\${aws.account.id}")
    lateinit var accountId: String
    @Value("\${aws.region}")
    lateinit var region: String

    @Bean
    fun awsInfo() = Ec2Info(accountId, region)
}


const val STS_PREFIX_S3 = "sts.s3."
const val STS_PREFIX_SQS = "sts.sqs."
const val STS_PREFIX_SNS = "sts.sns."


@Configuration
class AwsS3Config {

    @Autowired
    lateinit var env: ConfigurableEnvironment

    @Autowired
    lateinit var secretLeaseContainer: SecretLeaseContainer

    @Value("\${vault.stspath.s3}")
    lateinit var stsPath: String

    @Bean
    fun amazonS3(awsInfo: Ec2Info, @Qualifier("s3") provider: SessionCredentialsProvider): AmazonS3 {
        return AmazonS3ClientBuilder.standard()
                .withRegion(awsInfo.region)
                .withCredentials(provider)
                .build()
    }

    @Bean
    @Qualifier("s3")
    fun sessionCredentialsProviderS3(env: Environment) = SessionCredentialsProvider {
        StsCredentials.of(env, STS_PREFIX_S3)
    }

    // Add the LeaseAwareVaultPropertySources to the PropertySource list
    @PostConstruct
    fun init() {
        env.propertySources.addFirst(rotatingPropertySource(stsPath, secretLeaseContainer, STS_PREFIX_S3))
    }

    companion object {

        @Bean
        fun placeHolderConfigurer(): PropertySourcesPlaceholderConfigurer {
            return PropertySourcesPlaceholderConfigurer()
        }
    }
}

@Configuration
class AwsSnsConfig {

    @Autowired
    lateinit var env: ConfigurableEnvironment

    @Autowired
    lateinit var secretLeaseContainer: SecretLeaseContainer

    @Value("\${vault.stspath.sns}")
    lateinit var stsPath: String

    @Bean
    fun amazonSns(awsInfo: Ec2Info, @Qualifier("sns") provider: SessionCredentialsProvider): AmazonSNSAsync {
        return AmazonSNSAsyncClientBuilder.standard()
                .withCredentials(provider)
                .withRegion(awsInfo.region)
                .build()
    }

    @Bean
    @Qualifier("sns")
    fun sessionCredentialsProviderSns(env: Environment) = SessionCredentialsProvider {
        StsCredentials.of(env, STS_PREFIX_SNS)
    }

    // Add the LeaseAwareVaultPropertySources to the PropertySource list
    @PostConstruct
    fun init() {
        env.propertySources.addFirst(rotatingPropertySource(stsPath, secretLeaseContainer, STS_PREFIX_SNS))
    }

    companion object {

        @Bean
        fun placeHolderConfigurer(): PropertySourcesPlaceholderConfigurer {
            return PropertySourcesPlaceholderConfigurer()
        }
    }
}


@Configuration
class AwsSqsConfig {

    @Autowired
    lateinit var env: ConfigurableEnvironment

    @Autowired
    lateinit var secretLeaseContainer: SecretLeaseContainer

    @Value("\${vault.stspath.sqs}")
    lateinit var stsPath: String

    @Bean
    fun amazonSqs(awsInfo: Ec2Info, @Qualifier("sqs") provider: SessionCredentialsProvider): AmazonSQSAsync {
        return AmazonSQSAsyncClientBuilder.standard()
                .withRegion(awsInfo.region)
                .withCredentials(provider)
                .build()
    }

    // Connect Spring Environment to the AWSCredentialsProvider
    @Bean
    @Qualifier("sqs")
    fun sessionCredentialsProviderSqs(env: Environment) = SessionCredentialsProvider {
        StsCredentials.of(env, STS_PREFIX_SQS)
    }


    // Add the LeaseAwareVaultPropertySources to the PropertySource list
    @PostConstruct
    fun init() {
        env.propertySources.addFirst(rotatingPropertySource(stsPath, secretLeaseContainer, STS_PREFIX_SQS))
    }

    companion object {

        @Bean
        fun placeHolderConfigurer(): PropertySourcesPlaceholderConfigurer {
            return PropertySourcesPlaceholderConfigurer()
        }
    }
}
