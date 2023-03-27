package org.hildan.github.graphql.scalars

import com.expediagroup.graphql.client.converter.*
import java.time.*
import java.time.format.*
import java.util.Base64

class Base64Converter : ScalarConverter<ByteArray> {
    override fun toScalar(rawValue: Any): ByteArray = Base64.getDecoder().decode(rawValue.toString())
    override fun toJson(value: ByteArray): Any = Base64.getEncoder().encodeToString(value)
}

class LocalDateConverter : ScalarConverter<LocalDate> {
    override fun toScalar(rawValue: Any): LocalDate = LocalDate.parse(rawValue.toString())
    override fun toJson(value: LocalDate): Any = value.format(DateTimeFormatter.ISO_DATE)
}

class LocalDateTimeConverter : ScalarConverter<LocalDateTime> {
    override fun toScalar(rawValue: Any): LocalDateTime = LocalDateTime.parse(rawValue.toString())
    override fun toJson(value: LocalDateTime): Any = value.format(DateTimeFormatter.ISO_DATE_TIME)
}

class InstantConverter : ScalarConverter<Instant> {
    override fun toScalar(rawValue: Any): Instant = Instant.parse(rawValue.toString())
    override fun toJson(value: Instant): Any = value.toString()
}

@JvmInline
value class GitObjectID(val graphqlValue: String)

class GitObjectIDConverter : ScalarConverter<GitObjectID> {
    override fun toScalar(rawValue: Any): GitObjectID = GitObjectID(rawValue.toString())
    override fun toJson(value: GitObjectID): Any = value.graphqlValue
}

@JvmInline
value class GitRefname(val graphqlValue: String)

class GitRefnameConverter : ScalarConverter<GitRefname> {
    override fun toScalar(rawValue: Any): GitRefname = GitRefname(rawValue.toString())
    override fun toJson(value: GitRefname): Any = value.graphqlValue
}

@JvmInline
value class GitSSHRemote(val graphqlValue: String)

class GitSSHRemoteConverter : ScalarConverter<GitSSHRemote> {
    override fun toScalar(rawValue: Any): GitSSHRemote = GitSSHRemote(rawValue.toString())
    override fun toJson(value: GitSSHRemote): Any = value.graphqlValue
}

@JvmInline
value class HtmlContent(val graphqlValue: String)

class HtmlContentConverter : ScalarConverter<HtmlContent> {
    override fun toScalar(rawValue: Any): HtmlContent = HtmlContent(rawValue.toString())
    override fun toJson(value: HtmlContent): Any = value.graphqlValue
}
